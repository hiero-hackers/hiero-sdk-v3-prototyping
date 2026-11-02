#!/usr/bin/env python3
"""Probes how a running Mirror Node evaluates the conditions of its REST query parameters.

The OpenAPI definition (mirror-node/openapi.yml) describes the conditions of a parameter only by a regular
expression ("gte:0.0.5"). This script sends every parameter that accepts conditions a fixed matrix of variants -
single operators, repeated values, ranges, redundant and contradictory bounds, combinations, and boundary values -
and checks the answer: the HTTP status, and for the parameters whose value appears in the returned items, whether
the items satisfy the condition and which semantics of repeated values (IN or AND) the server applied.

Read-only GET requests against a public Mirror Node (default: Hedera testnet). Standard library only.

usage: probe-mirror-node-conditions.py [--base URL] [--json FILE] [--markdown FILE] [--only ENDPOINT_SUBSTRING]
"""

import argparse
import datetime
import json
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from decimal import Decimal, InvalidOperation

DEFAULT_BASE = "https://testnet.mirrornode.hedera.com/api/v1"
PAUSE = 0.08  # seconds between requests, to stay below the rate limit of the public mirror nodes

# --- HTTP ------------------------------------------------------------------------------------------

REQUESTS = 0


def get(base, path_and_query):
    """GET a path below the API base; returns (status, json or None, error message or None)."""
    global REQUESTS
    url = base + path_and_query
    for attempt in range(6):
        REQUESTS += 1
        time.sleep(PAUSE)
        try:
            with urllib.request.urlopen(url, timeout=30) as response:
                return response.status, json.load(response), None
        except urllib.error.HTTPError as error:
            if error.code == 429:
                time.sleep(2 ** attempt)
                continue
            try:
                body = json.load(error)
                message = "; ".join(m.get("message", "") + (f" ({m['detail']})" if m.get("detail") else "")
                                    for m in body.get("_status", {}).get("messages", []))
            except Exception:  # noqa: BLE001 - the body of an error is not always JSON
                message = ""
            return error.code, None, message
        except (urllib.error.URLError, TimeoutError) as error:
            if attempt == 5:
                return 0, None, str(error)
            time.sleep(2 ** attempt)
    return 429, None, "rate limited"


def query(param, conditions):
    return "&".join(f"{param}={urllib.parse.quote(c, safe=':.')}" for c in conditions)


# --- value kinds -----------------------------------------------------------------------------------

def entity_num(text):
    """The comparable value of an entity id ("0.0.123", "123")."""
    return int(str(text).split(".")[-1])


def timestamp_value(text):
    return Decimal(str(text))


def int_value(text):
    return int(str(text))


def hex_value(text):
    return int(str(text), 16) if str(text) else 0


class Kind:
    """How values of a parameter are written and compared."""

    def __init__(self, name, render, compare, boundaries):
        self.name = name
        self.render = render  # comparable value -> REST text
        self.compare = compare  # item value -> comparable value
        self.boundaries = boundaries  # [(label, REST text)] for the syntax checks


ENTITY = Kind("entity id", lambda v: f"0.0.{v}", entity_num, [
    ("num only", "5"), ("realm.num", "0.5"), ("shard.realm.num", "0.0.5"), ("zero", "0.0.0"),
    ("other shard", "1.0.5"), ("10-digit num", "0.0.9999999999"), ("11-digit num", "0.0.99999999999"),
    ("leading zero", "0.0.05"), ("negative", "0.0.-5"), ("checksum", "0.0.5-vfmkw"),
    ("EVM address", "0x00000000000000000000000000000000000003e8"), ("blank", ""), ("operator only", "eq:"),
    ("upper-case operator", "EQ:0.0.5"), ("unknown operator", "foo:0.0.5"), ("space", " 0.0.5"),
])
TIMESTAMP = Kind("timestamp", lambda v: str(v), timestamp_value, [
    ("seconds", "1700000000"), ("1 nano digit", "1700000000.1"), ("9 nano digits", "1700000000.000000001"),
    ("10 nano digits", "1700000000.0000000001"), ("zero", "0"), ("11-digit seconds", "17000000000"),
    ("negative", "-1"), ("trailing dot", "1700000000."), ("exponent", "1.7e9"), ("ISO 8601", "2023-11-14T22:13:20Z"),
    ("blank", ""), ("operator only", "eq:"), ("upper-case operator", "EQ:1700000000"),
    ("unknown operator", "foo:1700000000"),
])
INTEGER = Kind("integer", lambda v: str(v), int_value, [
    ("zero", "0"), ("one", "1"), ("int32 max", "2147483647"), ("int64 max", "9223372036854775807"),
    ("int64 max + 1", "9223372036854775808"), ("20 digits", "99999999999999999999"), ("negative", "-1"),
    ("decimal", "1.5"), ("hex", "0x10"), ("blank", ""), ("operator only", "eq:"), ("upper-case operator", "EQ:1"),
    ("unknown operator", "foo:1"),
])
BALANCE = Kind("balance", lambda v: str(v), int_value, [
    ("zero", "0"), ("10 digits", "9999999999"), ("11 digits", "10000000000"), ("negative", "-1"),
    ("decimal", "1.5"), ("blank", ""), ("unknown operator", "foo:1"),
])
HEX = Kind("hex", lambda v: f"0x{v:064x}", hex_value, [
    ("0x1", "0x1"), ("1 without 0x", "1"), ("64 digits", "0x" + "f" * 64), ("65 digits", "0x" + "f" * 65),
    ("not hex", "0xzz"), ("blank", ""), ("unknown operator", "foo:0x1"),
])
HASH = Kind("hash", lambda v: v, lambda v: v, [
    ("64 digits", "0x" + "a" * 64), ("96 digits", "0x" + "a" * 96), ("without 0x", "a" * 64), ("63 digits", "0x" + "a" * 63),
    ("ne operator", "ne:0x" + "a" * 64), ("gt operator", "gt:0x" + "a" * 64),
])


# --- targets ---------------------------------------------------------------------------------------

def involved(transaction):
    """The accounts a transaction involves: its transfers and its payer (a list: one of them must match)."""
    accounts = {transaction["transaction_id"].split("-")[0]}
    for transfer in transaction.get("transfers", []) + transaction.get("token_transfers", []):
        accounts.add(transfer["account"])
    for transfer in transaction.get("nft_transfers", []):
        accounts.update(a for a in (transfer.get("sender_account_id"), transfer.get("receiver_account_id")) if a)
    return sorted(accounts)


def path(item, *keys):
    """item[k1][k2]..., None if absent."""
    for key in keys:
        if not isinstance(item, dict):
            return None
        item = item.get(key)
    return item


class Target:
    """One query parameter of one endpoint."""

    def __init__(self, endpoint, param, kind, documented, collection=None, field=None, extra="",
                 note="", population=""):
        self.endpoint = endpoint  # with {placeholders}
        self.param = param
        self.kind = kind
        self.documented = documented  # the operators of the OpenAPI pattern
        self.collection = collection  # the list in the response, None if the value is not an item field
        self.field = field  # item -> value of the parameter, None: status and count only
        self.extra = extra  # further query parameters of every request
        self.note = note
        self.population = population  # a condition of the baseline only, when the values are rare in the list


ALL_SIX = "eq ne gt gte lt lte"
NO_NE = "eq gt gte lt lte"

TARGETS = [
    Target("/accounts", "account.id", ENTITY, ALL_SIX, "accounts", lambda i: i["account"]),
    Target("/accounts", "account.balance", BALANCE, ALL_SIX, "accounts", lambda i: path(i, "balance", "balance")),
    Target("/accounts/{txAccount}", "timestamp", TIMESTAMP, ALL_SIX, "transactions",
           lambda i: i["consensus_timestamp"], note="filters the transactions of the account"),
    Target("/accounts/{nftAccount}/nfts", "token.id", ENTITY, ALL_SIX, "nfts", lambda i: i["token_id"]),
    Target("/accounts/{nftAccount}/nfts", "serialnumber", INTEGER, NO_NE, "nfts", lambda i: i["serial_number"],
           note="the OpenAPI definition requires token.id with serialnumber"),
    Target("/accounts/{nftAccount}/nfts", "serialnumber", INTEGER, NO_NE, "nfts", lambda i: i["serial_number"],
           extra="token.id={nftAccountToken}", note="with token.id=<one token>"),
    Target("/accounts/{nftSpenderAccount}/nfts", "spender.id", ENTITY, ALL_SIX, "nfts", lambda i: i.get("spender"),
           population="spender.id=gte:0.0.1", note="baseline: the NFTs that have a spender"),
    Target("/accounts/{rewardAccount}/rewards", "timestamp", TIMESTAMP, ALL_SIX, "rewards", lambda i: i["timestamp"]),
    Target("/accounts/{tokensAccount}/tokens", "token.id", ENTITY, ALL_SIX, "tokens", lambda i: i["token_id"]),
    Target("/accounts/{airdropSender}/airdrops/outstanding", "receiver.id", ENTITY, ALL_SIX, "airdrops",
           lambda i: i["receiver_id"]),
    Target("/accounts/{airdropTokenSender}/airdrops/outstanding", "token.id", ENTITY, ALL_SIX, "airdrops",
           lambda i: i["token_id"]),
    Target("/accounts/{airdropSerialSender}/airdrops/outstanding", "serialnumber", INTEGER, NO_NE, "airdrops",
           lambda i: i.get("serial_number") or 0,
           note="an airdrop of a fungible token has no serial number; it counts as 0"),
    Target("/accounts/{airdropReceiver}/airdrops/pending", "sender.id", ENTITY, ALL_SIX, "airdrops",
           lambda i: i["sender_id"]),
    Target("/accounts/{pendingTokenReceiver}/airdrops/pending", "token.id", ENTITY, ALL_SIX, "airdrops",
           lambda i: i["token_id"]),
    Target("/accounts/{pendingSerialReceiver}/airdrops/pending", "serialnumber", INTEGER, NO_NE, "airdrops",
           lambda i: i.get("serial_number") or 0,
           note="an airdrop of a fungible token has no serial number; it counts as 0"),
    Target("/accounts/{cryptoAllowanceAccount}/allowances/crypto", "spender.id", ENTITY, ALL_SIX, "allowances",
           lambda i: i["spender"]),
    Target("/accounts/{tokenAllowanceAccount}/allowances/tokens", "spender.id", ENTITY, ALL_SIX, "allowances",
           lambda i: i["spender"]),
    Target("/accounts/{tokenAllowanceAccount}/allowances/tokens", "token.id", ENTITY, ALL_SIX, "allowances",
           lambda i: i["token_id"]),
    Target("/accounts/{tokenAllowanceAccount}/allowances/tokens", "token.id", ENTITY, ALL_SIX, "allowances",
           lambda i: i["token_id"], extra="spender.id={tokenAllowanceSpender}", note="with spender.id=<one spender>"),
    Target("/accounts/{nftAllowanceAccount}/allowances/nfts", "account.id", ENTITY, ALL_SIX, "allowances",
           lambda i: i.get("spender")),
    Target("/accounts/{nftAllowanceAccount}/allowances/nfts", "token.id", ENTITY, ALL_SIX, "allowances",
           lambda i: i["token_id"]),
    Target("/accounts/{txAccount}/hooks/1/storage", "key", HEX, "eq gt gte lt lte", "storage", lambda i: i["key"]),
    Target("/accounts/{txAccount}/hooks/1/storage", "timestamp", TIMESTAMP, ALL_SIX, "storage", None),
    Target("/balances", "account.id", ENTITY, ALL_SIX, "balances", lambda i: i["account"],
           note="the OpenAPI pattern of this parameter has no operators"),
    Target("/balances", "account.balance", BALANCE, ALL_SIX, "balances", lambda i: i["balance"]),
    Target("/balances", "timestamp", TIMESTAMP, ALL_SIX, "balances", None, note="selects the balance snapshot"),
    Target("/blocks", "block.number", INTEGER, NO_NE, "blocks", lambda i: i["number"]),
    Target("/blocks", "timestamp", TIMESTAMP, ALL_SIX, "blocks", None),
    Target("/contracts", "contract.id", ENTITY, ALL_SIX, "contracts", lambda i: i["contract_id"]),
    Target("/contracts/{contract}", "timestamp", TIMESTAMP, ALL_SIX, None, None),
    Target("/contracts/{contract}/results", "timestamp", TIMESTAMP, ALL_SIX, "results", lambda i: i["timestamp"]),
    Target("/contracts/{contract}/results", "block.number", INTEGER, "eq", "results", None,
           note="the items carry no block number"),
    Target("/contracts/{contract}/results", "block.hash", HASH, "eq", "results", None,
           note="the items carry no block hash"),
    Target("/contracts/{contract}/results", "transaction.index", INTEGER, "(none)", "results", None,
           extra="block.number={blockNumber}", note="documented as a plain integer, needs block.number"),
    Target("/contracts/{contract}/state", "slot", HEX, "eq gt gte lt lte", "state", lambda i: i["slot"]),
    Target("/contracts/{contract}/state", "timestamp", TIMESTAMP, NO_NE, "state", None),
    Target("/contracts/results", "timestamp", TIMESTAMP, ALL_SIX, "results", lambda i: i["timestamp"]),
    Target("/contracts/results", "block.number", INTEGER, "eq", "results", None,
           note="the items carry no block number"),
    Target("/contracts/results/{contractTransaction}/actions", "index", INTEGER, ALL_SIX, "actions",
           lambda i: i["index"]),
    Target("/contracts/{contract}/results/logs", "index", INTEGER, NO_NE, "logs", lambda i: i["index"],
           extra="timestamp={logTimestamp}", note="index needs timestamp=<one timestamp>"),
    Target("/contracts/{contract}/results/logs", "timestamp", TIMESTAMP, ALL_SIX, "logs", lambda i: i["timestamp"]),
    Target("/contracts/results/logs", "timestamp", TIMESTAMP, ALL_SIX, "logs", lambda i: i["timestamp"]),
    Target("/contracts/results/logs", "transaction.hash", HASH, "eq", "logs", lambda i: i["transaction_hash"]),
    Target("/network/exchangerate", "timestamp", TIMESTAMP, ALL_SIX, None, None),
    Target("/network/fees", "timestamp", TIMESTAMP, ALL_SIX, "fees", None),
    Target("/network/nodes", "file.id", ENTITY, ALL_SIX, "nodes", lambda i: i["file_id"]),
    Target("/network/nodes", "node.id", INTEGER, NO_NE, "nodes", lambda i: i["node_id"]),
    Target("/network/registered-nodes", "registerednode.id", INTEGER, NO_NE, "registered_nodes",
           lambda i: i["registered_node_id"]),
    Target("/network/supply", "timestamp", TIMESTAMP, ALL_SIX, None, None),
    Target("/schedules", "schedule.id", ENTITY, ALL_SIX, "schedules", lambda i: i["schedule_id"]),
    Target("/schedules", "account.id", ENTITY, ALL_SIX, "schedules", lambda i: i["creator_account_id"],
           note="assumed to filter the creator"),
    Target("/transactions", "timestamp", TIMESTAMP, ALL_SIX, "transactions", lambda i: i["consensus_timestamp"]),
    Target("/transactions", "account.id", ENTITY, ALL_SIX, "transactions", lambda i: involved(i),
           note="checked against the accounts of the transfers: an item matches if one of them does (so ne is "
                "always satisfied; see the analysis)"),
    Target("/topics/{topic}/messages", "timestamp", TIMESTAMP, ALL_SIX, "messages",
           lambda i: i["consensus_timestamp"]),
    Target("/topics/{topic}/messages", "sequencenumber", INTEGER, "(none)", "messages",
           lambda i: i["sequence_number"], note="documented as a plain integer"),
    Target("/tokens", "token.id", ENTITY, ALL_SIX, "tokens", lambda i: i["token_id"]),
    Target("/tokens", "account.id", ENTITY, ALL_SIX, "tokens", None, note="the tokens associated with the account"),
    Target("/tokens/{fungibleToken}", "timestamp", TIMESTAMP, "eq lt lte", None, None),
    Target("/tokens/{fungibleToken}/balances", "account.id", ENTITY, ALL_SIX, "balances", lambda i: i["account"]),
    Target("/tokens/{fungibleToken}/balances", "account.balance", BALANCE, ALL_SIX, "balances",
           lambda i: i["balance"]),
    Target("/tokens/{fungibleToken}/balances", "timestamp", TIMESTAMP, ALL_SIX, "balances", None),
    Target("/tokens/{nftToken}/nfts", "account.id", ENTITY, ALL_SIX, "nfts", lambda i: i["account_id"]),
    Target("/tokens/{nftToken}/nfts", "serialnumber", INTEGER, NO_NE, "nfts", lambda i: i["serial_number"]),
    Target("/tokens/{serialToken}/nfts/{nftSerial}/transactions", "timestamp", TIMESTAMP, ALL_SIX, "transactions",
           lambda i: i["consensus_timestamp"]),
]


# --- fixtures --------------------------------------------------------------------------------------

def payers(base, transaction_type, limit=50):
    """The payers and entities of recent successful transactions of a type."""
    _, body, _ = get(base, f"/transactions?transactiontype={transaction_type}&result=success&limit={limit}")
    transactions = (body or {}).get("transactions", [])
    payer = list(dict.fromkeys(t["transaction_id"].split("-")[0] for t in transactions))
    entity = list(dict.fromkeys(t["entity_id"] for t in transactions if t.get("entity_id")))
    return payer, entity


def pages_of_payers(base, transaction_type, pages):
    """The payers of the recent successful transactions of a type, from several pages."""
    found, url = [], f"/transactions?transactiontype={transaction_type}&result=success&limit=100"
    for _ in range(pages):
        _, body, _ = get(base, url)
        found += [t["transaction_id"].split("-")[0] for t in (body or {}).get("transactions", [])]
        url = ((body or {}).get("links") or {}).get("next")
        if not url:
            break
        url = url.removeprefix("/api/v1")
    return list(dict.fromkeys(found))


def best(base, candidates, path_template, collection, distinct, at_most=8):
    """The candidate whose list has the most distinct values (at least 3), checking at most a few candidates."""
    found, found_count = None, 2
    for candidate in candidates[:at_most]:
        _, body, _ = get(base, path_template.format(candidate) + ("&" if "?" in path_template else "?") + "limit=100")
        items = (body or {}).get(collection, [])
        count = len({distinct(i) for i in items})
        if count > found_count:
            found, found_count = candidate, count
            if count >= 20:
                break
    return found


def discover(base):
    """Finds entities with enough data for every path parameter, on any network."""
    fixtures = {}

    def put(key, value):
        if value is not None:
            fixtures[key] = str(value)

    transfer_payers, _ = payers(base, "CRYPTOTRANSFER", 100)
    put("txAccount", best(base, transfer_payers, "/accounts/{}", "transactions", lambda i: i["consensus_timestamp"]))
    put("tokensAccount", best(base, transfer_payers, "/accounts/{}/tokens", "tokens", lambda i: i["token_id"]))
    # NFTs: a token with many NFTs and holders, an account holding NFTs of several tokens
    mint_payers, minted = payers(base, "TOKENMINT", 100)
    nft_tokens, fungible_tokens = [], []
    for token in minted[:15]:
        _, info, _ = get(base, f"/tokens/{token}")
        (nft_tokens if (info or {}).get("type") == "NON_FUNGIBLE_UNIQUE" else fungible_tokens).append(token)
    put("nftToken", best(base, nft_tokens, "/tokens/{}/nfts", "nfts", lambda i: i["account_id"]))
    put("fungibleToken", best(base, fungible_tokens, "/tokens/{}/balances", "balances", lambda i: i["account"]))
    holders, spenders = [], []
    if "nftToken" in fixtures:
        _, nfts, _ = get(base, f"/tokens/{fixtures['nftToken']}/nfts?limit=100")
        for nft in (nfts or {}).get("nfts", []):
            holders.append(nft["account_id"])
            if nft.get("spender"):
                spenders.append(nft["account_id"])
    holders = sorted(set(holders), key=holders.count, reverse=True) + mint_payers
    put("nftAccount", best(base, holders, "/accounts/{}/nfts", "nfts", lambda i: i["token_id"]))
    if "nftAccount" in fixtures:
        _, nfts, _ = get(base, f"/accounts/{fixtures['nftAccount']}/nfts?limit=100")
        tokens = [n["token_id"] for n in (nfts or {}).get("nfts", [])]
        put("nftAccountToken", max(set(tokens), key=tokens.count) if tokens else None)
    # allowances
    approvers, _ = payers(base, "CRYPTOAPPROVEALLOWANCE", 100)
    put("cryptoAllowanceAccount", best(base, approvers, "/accounts/{}/allowances/crypto", "allowances",
                                       lambda i: i["spender"], 15))
    put("tokenAllowanceAccount", best(base, approvers, "/accounts/{}/allowances/tokens", "allowances",
                                      lambda i: i["spender"], 15))
    put("nftAllowanceAccount", best(base, approvers, "/accounts/{}/allowances/nfts", "allowances",
                                    lambda i: (i["spender"], i["token_id"]), 15))
    # an owner whose NFTs have several spenders (only an approval of single serials sets the spender of an NFT)
    owners = list(dict.fromkeys(spenders + approvers + pages_of_payers(base, "CRYPTOAPPROVEALLOWANCE", 5)))
    put("nftSpenderAccount", best(base, owners, "/accounts/{}/nfts?spender.id=gte:0.0.1", "nfts",
                                  lambda i: i["spender"], 60))
    # an NFT with a long history: one that was transferred recently, not one that was just minted
    transferred = {}
    url = "/transactions?transactiontype=CRYPTOTRANSFER&result=success&limit=100"
    for _ in range(30):
        _, body, _ = get(base, url)
        for transaction in (body or {}).get("transactions", []):
            for transfer in transaction.get("nft_transfers") or []:
                key = (transfer["token_id"], transfer["serial_number"])
                transferred[key] = transferred.get(key, 0) + 1
        url = ((body or {}).get("links") or {}).get("next")
        if not url:
            break
        url = url.removeprefix("/api/v1")
    candidates = sorted(transferred, key=transferred.get, reverse=True)
    serial = best(base, [f"{token}/nfts/{number}" for token, number in candidates], "/tokens/{}/transactions",
                  "transactions", lambda i: i["consensus_timestamp"], 15)
    if serial:
        put("serialToken", serial.split("/")[0])
        put("nftSerial", serial.split("/")[2])
    if "tokenAllowanceAccount" in fixtures:
        _, allowances, _ = get(base, f"/accounts/{fixtures['tokenAllowanceAccount']}/allowances/tokens?limit=100")
        spender_of = [a["spender"] for a in (allowances or {}).get("allowances", [])]
        put("tokenAllowanceSpender", max(set(spender_of), key=spender_of.count) if spender_of else None)
    # airdrops: a sender with outstanding airdrops, a receiver with pending ones
    airdrop_payers, _ = payers(base, "TOKENAIRDROP", 100)
    outstanding_of = {}
    for sender in airdrop_payers[:15]:
        _, outstanding, _ = get(base, f"/accounts/{sender}/airdrops/outstanding?limit=100")
        outstanding_of[sender] = (outstanding or {}).get("airdrops", [])

    def most(candidates, items_of, distinct):
        counts = {c: len({distinct(i) for i in items_of[c]}) for c in candidates}
        winner = max(counts, key=counts.get, default=None)
        return winner if winner is not None and counts[winner] >= 3 else None

    put("airdropSender", most(outstanding_of, outstanding_of, lambda i: i["receiver_id"]))
    put("airdropTokenSender", most(outstanding_of, outstanding_of, lambda i: i["token_id"]))
    put("airdropSerialSender", most(outstanding_of, outstanding_of, lambda i: i.get("serial_number")))
    receivers = [a["receiver_id"] for items in outstanding_of.values() for a in items]
    pending_of = {}
    for receiver in sorted(set(receivers), key=receivers.count, reverse=True)[:15]:
        _, pending, _ = get(base, f"/accounts/{receiver}/airdrops/pending?limit=100")
        pending_of[receiver] = (pending or {}).get("airdrops", [])
    put("airdropReceiver", most(pending_of, pending_of, lambda i: i["sender_id"]))
    put("pendingTokenReceiver", most(pending_of, pending_of, lambda i: i["token_id"]))
    put("pendingSerialReceiver", most(pending_of, pending_of, lambda i: i.get("serial_number")))
    # a staked account with staking rewards
    _, accounts, _ = get(base, "/accounts?limit=100&account.balance=gt:100000000000&order=asc")
    staked = [a["account"] for a in (accounts or {}).get("accounts", []) if a.get("staked_node_id") is not None]
    put("rewardAccount", best(base, staked, "/accounts/{}/rewards", "rewards", lambda i: i["timestamp"]))
    # contracts and topics
    _, contracts = payers(base, "CONTRACTCALL", 100)
    contracts = [c for c in contracts[:15] if get(base, f"/contracts/{c}")[0] == 200]  # not a system contract
    put("contract", best(base, contracts, "/contracts/{}/results/logs", "logs", lambda i: i["timestamp"]))
    _, topics = payers(base, "CONSENSUSSUBMITMESSAGE", 100)
    put("topic", best(base, topics, "/topics/{}/messages", "messages", lambda i: i["sequence_number"]))
    if "contract" in fixtures:
        _, results, _ = get(base, f"/contracts/{fixtures['contract']}/results?limit=1")
        for result in (results or {}).get("results", []):
            fixtures["contractTransaction"] = result["hash"]
            # the items of the list have no block; the result of the transaction has
            _, detail, _ = get(base, f"/contracts/results/{result['hash']}")
            if detail:
                fixtures["blockNumber"] = str(detail["block_number"])
                fixtures["blockHash"] = detail["block_hash"]
                fixtures["transactionIndex"] = str(detail.get("transaction_index"))
        _, logs, _ = get(base, f"/contracts/{fixtures['contract']}/results/logs?limit=100")
        timestamps = {}
        for log in (logs or {}).get("logs", []):
            timestamps[log["timestamp"]] = timestamps.get(log["timestamp"], 0) + 1
        if timestamps:
            fixtures["logTimestamp"] = max(timestamps, key=timestamps.get)
    # the actions of a contract call with several actions
    if "contract" in fixtures:
        _, results, _ = get(base, f"/contracts/{fixtures['contract']}/results?limit=10")
        hashes = [r["hash"] for r in (results or {}).get("results", [])]
        put("contractTransaction", best(base, hashes, "/contracts/results/{}/actions", "actions",
                                        lambda i: i["index"]) or fixtures.get("contractTransaction"))
    return fixtures


def resolve(text, fixtures):
    for key, value in fixtures.items():
        text = text.replace("{" + key + "}", value)
    return text


# --- semantics -------------------------------------------------------------------------------------

def satisfies(value, conditions, kind, repeated_eq):
    """Whether a comparable value satisfies the conditions; repeated_eq is "IN" or "AND"."""
    if value is None:
        return False
    eqs = []
    for condition in conditions:
        operator, _, text = condition.partition(":")
        if not text:
            operator, text = "eq", condition
        other = kind.compare(text)
        if operator == "eq":
            eqs.append(value == other)
        elif operator == "ne" and value == other:
            return False
        elif operator == "gt" and not value > other:
            return False
        elif operator == "gte" and not value >= other:
            return False
        elif operator == "lt" and not value < other:
            return False
        elif operator == "lte" and not value <= other:
            return False
    if not eqs:
        return True
    return any(eqs) if repeated_eq == "IN" else all(eqs)


def variants(lo, mid, hi, kind):
    r = kind.render
    return [
        ("bare value", [r(mid)]),
        ("eq", [f"eq:{r(mid)}"]),
        ("ne", [f"ne:{r(mid)}"]),
        ("gt", [f"gt:{r(mid)}"]),
        ("gte", [f"gte:{r(mid)}"]),
        ("lt", [f"lt:{r(mid)}"]),
        ("lte", [f"lte:{r(mid)}"]),
        ("two eq", [f"eq:{r(lo)}", f"eq:{r(hi)}"]),
        ("two bare values", [r(lo), r(hi)]),
        ("same eq twice", [f"eq:{r(mid)}", f"eq:{r(mid)}"]),
        ("two ne", [f"ne:{r(lo)}", f"ne:{r(hi)}"]),
        ("range gte+lte", [f"gte:{r(lo)}", f"lte:{r(hi)}"]),
        ("range gt+lt", [f"gt:{r(lo)}", f"lt:{r(hi)}"]),
        ("point gte+lte", [f"gte:{r(mid)}", f"lte:{r(mid)}"]),
        ("redundant lower gt+gte", [f"gt:{r(lo)}", f"gte:{r(mid)}"]),
        ("redundant upper lt+lte", [f"lt:{r(hi)}", f"lte:{r(mid)}"]),
        ("two gte", [f"gte:{r(lo)}", f"gte:{r(mid)}"]),
        ("contradictory gt+lt", [f"gt:{r(hi)}", f"lt:{r(lo)}"]),
        ("empty gt+lt same", [f"gt:{r(mid)}", f"lt:{r(mid)}"]),
        ("eq + range", [f"eq:{r(lo)}", f"eq:{r(mid)}", f"gte:{r(mid)}"]),
        ("eq + ne", [f"eq:{r(lo)}", f"eq:{r(mid)}", f"ne:{r(mid)}"]),
        ("range + ne", [f"gte:{r(lo)}", f"lte:{r(hi)}", f"ne:{r(mid)}"]),
        ("all: eq*3 + gte + lt + ne", [f"eq:{r(lo)}", f"eq:{r(mid)}", f"eq:{r(hi)}", f"gte:{r(lo)}",
                                        f"lt:{r(hi)}", f"ne:{r(lo)}"]),
    ]


def comparable(target, item):
    """The comparable values of the parameter in an item: one, or several (e.g. the accounts of a transaction)."""
    raw = target.field(item)
    raw = raw if isinstance(raw, list) else [raw]
    return [target.kind.compare(v) for v in raw if v is not None]


def classify(target, conditions, items, page_complete, baseline):
    """The verdict for the items of an accepted request.

    The items must satisfy the conditions; and if the answer has no further page, every value of the unfiltered
    first page that satisfies them must be among the items (those entities exist). Both are checked for the two
    readings of repeated eq values: IN (any of them) and AND (all of them).
    """
    if target.field is None or items is None:
        return "accepted (not checked)"
    values = [comparable(target, item) for item in items]  # per item: the values, one of them must match
    returned = {v for item in values for v in item}
    verdicts = []
    for semantics in ("IN", "AND"):
        if any(not any(satisfies(v, conditions, target.kind, semantics) for v in item) for item in values):
            continue  # an item violates the conditions under this reading
        # an item with several values (the accounts of a transaction) says nothing about the others
        if page_complete and baseline is not None and all(len(item) <= 1 for item in values):
            expected = {v for v in baseline if v is not None and satisfies(v, conditions, target.kind, semantics)}
            if not expected <= returned:
                continue  # existing matching items are missing
        verdicts.append(semantics)
    eqs = [c for c in conditions if c.startswith("eq:") or ":" not in c]
    multi_eq = len(eqs) > 1
    if not verdicts and multi_eq and len(set(eqs)) > 1:
        # does the server apply only one of the eq conditions?
        for which, kept in (("last", eqs[-1]), ("first", eqs[0])):
            reduced = [c for c in conditions if c not in eqs or c == kept]
            if all(any(satisfies(v, reduced, target.kind, "IN") for v in item) for item in values) and (
                    not page_complete or baseline is None
                    or {v for v in baseline if satisfies(v, reduced, target.kind, "IN")} <= returned):
                return f"ONLY {which.upper()} EQ"
    if not verdicts:
        unfiltered = baseline is not None and returned and returned >= {v for v in baseline if v is not None}
        return "IGNORED" if unfiltered else "WRONG"
    if not multi_eq:
        return "ok"
    if verdicts == ["IN", "AND"]:
        return "ok (IN = AND here)"
    return "ok, " + verdicts[0]


# --- probe -----------------------------------------------------------------------------------------

def probe_target(base, target, fixtures, log):
    endpoint = resolve(target.endpoint, fixtures)
    extra = resolve(target.extra, fixtures)
    if "{" in endpoint or "{" in extra:
        return {"skipped": "no fixture for " + target.endpoint}
    prefix = endpoint + "?" + (extra + "&" if extra else "")
    result = {"endpoint": target.endpoint, "resolved": endpoint, "param": target.param, "extra": target.extra,
              "kind": target.kind.name,
              "documented": target.documented, "note": target.note, "variants": [], "boundaries": []}
    # the baseline: the items without condition (the first page of 100)
    population = target.population + "&" if target.population else ""
    status, body, message = get(base, prefix + population + ("limit=100" if target.collection else ""))
    baseline, complete = None, False
    if status == 200 and target.collection and body is not None:
        items = body.get(target.collection, [])
        complete = not (body.get("links") or {}).get("next")
        if target.field:
            baseline = [v for item in items for v in comparable(target, item)]
    result["baseline"] = {"status": status, "message": message, "items": len(baseline or []) if baseline is not None
                          else None, "complete": complete}
    distinct = sorted({v for v in (baseline or []) if v is not None})
    if len(distinct) >= 3:
        lo, mid, hi = distinct[len(distinct) // 4], distinct[len(distinct) // 2], distinct[3 * len(distinct) // 4]
        if lo == mid or mid == hi:
            lo, mid, hi = distinct[0], distinct[len(distinct) // 2], distinct[-1]
    else:
        lo, mid, hi = sample_values(base, target, fixtures)
        if target.field is not None and baseline is not None:
            result["baseline"]["insufficient"] = f"{len(distinct)} distinct value(s)"
    result["samples"] = [str(lo), str(mid), str(hi)]
    for name, conditions in variants(lo, mid, hi, target.kind):
        # single objects accept no limit (they reject unknown parameters)
        page = "limit=100&" if target.collection else ""
        status, body, message = get(base, prefix + page + query(target.param, conditions))
        items = body.get(target.collection) if status == 200 and target.collection and body else None
        page_complete = status == 200 and body is not None and not (body.get("links") or {}).get("next")
        verdict = classify(target, conditions, items, page_complete, baseline) if status == 200 else "error"
        if status == 200 and baseline is not None and len(distinct) < 3 and target.field is not None:
            verdict = "accepted (too little data)"
        values = [str(target.field(i)) for i in items][:12] if target.field and isinstance(items, list) else None
        result["variants"].append({"name": name, "conditions": conditions, "status": status, "message": message,
                                   "items": len(items) if isinstance(items, list) else None, "verdict": verdict,
                                   "values": values, "complete": page_complete})
        log(f"  {target.param:18} {name:28} {status} {verdict} {message or ''}")
    for label, text in target.kind.boundaries:
        status, body, message = get(base, prefix + query(target.param, [text]))
        items = body.get(target.collection) if status == 200 and target.collection and body else None
        result["boundaries"].append({"name": label, "value": text, "status": status, "message": message,
                                     "items": len(items) if isinstance(items, list) else None})
    # the limit of conditions per parameter
    for count in (100, 101):
        conditions = [f"ne:{target.kind.render(v)}" if target.kind not in (HASH,) else f"eq:{lo}"
                      for v in sample_series(target.kind, count)]
        status, _, message = get(base, prefix + query(target.param, conditions))
        result["boundaries"].append({"name": f"{count} conditions", "value": f"{count} x ne", "status": status,
                                     "message": message, "items": None})
    return result


def sample_values(base, target, fixtures):
    """Values for a parameter whose value does not appear in the items (or with too few items)."""
    if target.kind is TIMESTAMP:
        _, body, _ = get(base, "/transactions?limit=3&order=desc")
        stamps = sorted(timestamp_value(t["consensus_timestamp"]) for t in (body or {}).get("transactions", []))
        if len(stamps) == 3:
            return stamps[0], stamps[1], stamps[2]
        now = Decimal(int(time.time()))
        return now - 7200, now - 3600, now
    if target.kind is ENTITY:
        return 2, 98, 800
    if target.kind is HEX:
        return 1, 2, 3
    if target.param == "block.hash" and "blockHash" in fixtures:
        return "0x" + "1" * 96, fixtures["blockHash"], "0x" + "f" * 96
    if target.kind is HASH:
        return "0x" + "1" * 64, fixtures.get("contractTransaction", "0x" + "2" * 64), "0x" + "f" * 64
    if target.param == "block.number" and "blockNumber" in fixtures:
        number = int(fixtures["blockNumber"])
        return number - 1, number, number + 1
    if target.param == "transaction.index" and fixtures.get("transactionIndex", "None") != "None":
        index = int(fixtures["transactionIndex"])
        return max(index - 1, 0), index, index + 1
    return 1, 2, 3


def sample_series(kind, count):
    if kind is TIMESTAMP:
        return [Decimal(1600000000 + i) for i in range(count)]
    if kind is HASH:
        return ["0x%064x" % i for i in range(count)]
    return list(range(1, count + 1))


def probe_general(base, fixtures, log):
    """limit, order and the pagination of conditions."""
    checks = []

    def check(name, path_and_query, inspect=None):
        status, body, message = get(base, path_and_query)
        detail = inspect(body) if inspect and body is not None else ""
        checks.append({"name": name, "request": path_and_query, "status": status, "message": message,
                       "detail": detail})
        log(f"  {name:40} {status} {message or ''} {detail}")

    count = lambda key: (lambda b: f"{len(b.get(key, []))} item(s)")
    for limit in ("0", "1", "100", "101", "-1", "abc"):
        check(f"limit={limit}", f"/network/nodes?limit={limit}", count("nodes"))
    for order in ("asc", "desc", "ASC", "foo"):
        check(f"order={order}", f"/network/nodes?order={order}",
              lambda b: "node ids " + str([n["node_id"] for n in b.get("nodes", [])]))
    for limit in ("100", "101", "1000"):
        check(f"limit={limit} with many items", f"/accounts?limit={limit}", count("accounts"))
    check("repeated limit", "/network/nodes?limit=1&limit=2", count("nodes"))
    check("repeated order", "/network/nodes?order=asc&order=desc",
          lambda b: "node ids " + str([n["node_id"] for n in b.get("nodes", [])]))
    check("unknown parameter", "/network/nodes?foo=1", count("nodes"))

    # do the conditions survive the pagination? range + ne with a page size of 2
    status, body, message = get(base, "/accounts?limit=2&account.id=gte:0.0.3&account.id=lte:0.0.20"
                                      "&account.id=ne:0.0.4&account.id=ne:0.0.5")
    pages, ids, nexts = 0, [], []
    while status == 200 and body is not None and pages < 12:
        pages += 1
        ids += [entity_num(a["account"]) for a in body.get("accounts", [])]
        link = (body.get("links") or {}).get("next")
        if not link:
            break
        nexts.append(link)
        status, body, message = get(base, link.replace("/api/v1", "", 1))
    expected = [n for n in range(3, 21) if n not in (4, 5)]
    checks.append({"name": "conditions across pages (account.id gte+lte+2 ne, limit=2)",
                   "request": "/accounts?limit=2&account.id=gte:0.0.3&account.id=lte:0.0.20&account.id=ne:0.0.4"
                              "&account.id=ne:0.0.5",
                   "status": status, "message": message,
                   "detail": f"{pages} page(s), ids {ids}, {'as expected' if ids == expected else 'expected ' + str(expected)}"
                             + (f"; first next link: {nexts[0]}" if nexts else "")})
    log("  " + checks[-1]["detail"])

    # /tokens?account.id=<account> against the token relationships of the account
    if fixtures.get("tokensAccount"):
        account = fixtures["tokensAccount"]
        _, by_filter, _ = get(base, f"/tokens?account.id={account}&limit=100")
        _, relations, _ = get(base, f"/accounts/{account}/tokens?limit=100")
        filtered = {t["token_id"] for t in (by_filter or {}).get("tokens", [])}
        related = {t["token_id"] for t in (relations or {}).get("tokens", [])}
        complete = not ((by_filter or {}).get("links") or {}).get("next") and \
            not ((relations or {}).get("links") or {}).get("next")
        checks.append({"name": "/tokens?account.id=X vs. /accounts/X/tokens", "request": f"/tokens?account.id={account}",
                       "status": 200, "message": None,
                       "detail": f"{len(filtered)} vs. {len(related)} token(s), "
                                 + ("equal" if filtered == related else
                                    f"{len(filtered - related)} only in /tokens, {len(related - filtered)} only in the "
                                    "relationships") + ("" if complete else " (first pages only)")})
        log("  " + checks[-1]["detail"])

    # two parameters at once
    check("account.id range + account.balance", "/accounts?account.id=gte:0.0.2&account.id=lte:0.0.50"
          "&account.balance=gt:0&limit=100",
          lambda b: f"{len(b.get('accounts', []))} item(s), all balances > 0: "
                    f"{all(path(a, 'balance', 'balance') > 0 for a in b.get('accounts', []))}")
    return checks


# --- report ----------------------------------------------------------------------------------------

def markdown(report):
    lines = [
        "# Mirror Node query conditions: probe results",
        "",
        f"Generated by `tooling/openapi/probe-mirror-node-conditions.py` on {report['date']} against "
        f"`{report['base']}` (Mirror Node {report['version']}; the pinned OpenAPI definition is "
        f"{report['pinned']}), {report['requests']} requests. Do not edit by hand; the analysis is in "
        "[mirror-node-query-conditions.md](mirror-node-query-conditions.md).",
        "",
        "Fixtures are found by the script on the network (the busiest entities of recent transactions), so they differ "
        "between runs.",
        "",
        "Verdicts: `ok` - the items satisfy the conditions (and, where the unfiltered first page was complete, "
        "are exactly the expected ones); `ok, IN` / `ok, AND` - repeated `eq` values were combined with OR / AND; "
        "`ok (IN = AND here)` - both readings give the same items; `ONLY LAST EQ` / `ONLY FIRST EQ` - of several eq "
        "values only one was applied; `IGNORED` - the condition had no effect; "
        "`WRONG` - items violate the condition; `error` - HTTP status other than 200; `accepted (not checked)` - "
        "200, but the value of the parameter is not part of the returned items.",
        "",
        "Fixtures: " + ", ".join(f"`{k}` = `{v}`" for k, v in sorted(report["fixtures"].items())),
        "",
        "## General",
        "",
        "| Check | Status | Result |",
        "|---|---|---|",
    ]
    for check in report["general"]:
        lines.append(f"| {check['name']} | {check['status']} | {esc(check['message'] or check['detail'])} |")
    names = [v[0] for v in variants(1, 2, 3, INTEGER)]
    lines += ["", "## Combinations per parameter", "",
              "One column per parameter; the samples `lo < mid < hi` are taken from the unfiltered first page.", ""]
    targets = [t for t in report["targets"] if "skipped" not in t]
    for group_start in range(0, len(targets), 6):
        group = targets[group_start:group_start + 6]
        lines.append("| Variant | " + " | ".join(label(t) for t in group) + " |")
        lines.append("|---" * (len(group) + 1) + "|")
        lines.append("| documented operators | " + " | ".join(t["documented"] for t in group) + " |")
        lines.append("| samples lo, mid, hi | " + " | ".join(esc(", ".join(t["samples"])) for t in group) + " |")
        lines.append("| unfiltered first page | " + " | ".join(baseline_text(t) for t in group) + " |")
        for name in names:
            cells = []
            for t in group:
                v = next(x for x in t["variants"] if x["name"] == name)
                cells.append(cell(v))
            lines.append(f"| {name} | " + " | ".join(cells) + " |")
        lines.append("")
    lines += ["## Boundary values per parameter", ""]
    for t in targets:
        lines.append(f"**{label(t)}** ({t['kind']})" + (f" - {t['note']}" if t["note"] else ""))
        lines.append("")
        lines.append("| Value | Status | Result |")
        lines.append("|---|---|---|")
        for b in t["boundaries"]:
            lines.append(f"| {b['name']} `{esc(b['value'])}` | {b['status']} | "
                         f"{esc(b['message']) if b['message'] else (str(b['items']) + ' item(s)' if b['items'] is not None else 'ok')} |")
        lines.append("")
    skipped = [t for t in report["targets"] if "skipped" in t]
    if skipped:
        lines += ["## Not probed", ""] + [f"- {t['skipped']}" for t in skipped] + [""]
    return "\n".join(lines)


def label(t):
    return f"`{t['endpoint']}` `{t['param']}`" + (f" (with `{t['extra']}`)" if t.get("extra") else "")


def baseline_text(t):
    b = t["baseline"]
    if b["status"] != 200:
        return f"{b['status']} {esc(b['message'] or '')}"
    if b["items"] is None:
        return "not checked"
    return f"{b['items']} item(s){'' if b['complete'] else '+'}" + (f", {b['insufficient']}" if b.get("insufficient") else "")


def cell(v):
    if v["status"] != 200:
        return f"**{v['status']}** {esc(v['message'] or '')}"
    if v["verdict"] in ("IGNORED", "WRONG") or v["verdict"].startswith("ONLY"):
        return f"**{v['verdict']}** ({v['items']})"
    return f"{v['verdict']} ({v['items']})" if v["items"] is not None else v["verdict"]


def esc(text):
    return str(text).replace("|", "\\|").replace("\n", " ")


def main():
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--base", default=DEFAULT_BASE)
    parser.add_argument("--json")
    parser.add_argument("--markdown")
    parser.add_argument("--only", default="")
    args = parser.parse_args()
    log = lambda text: print(text, flush=True)
    _, spec, _ = (0, None, None)
    version = "unknown"
    try:
        with urllib.request.urlopen(args.base + "/docs/openapi.yml", timeout=30) as response:
            for line in response.read().decode().splitlines():
                if line.strip().startswith("version:"):
                    version = line.split(":", 1)[1].strip()
                    break
    except Exception:  # noqa: BLE001 - the version is informational
        pass
    fixtures = discover(args.base)
    log(f"Mirror Node {version}, fixtures {fixtures}")
    report = {"base": args.base, "version": version, "pinned": pinned_version(),
              "date": datetime.date.today().isoformat(), "fixtures": fixtures, "targets": []}
    for target in TARGETS:
        if args.only and args.only not in target.endpoint:
            continue
        log(f"{target.endpoint} {target.param}")
        report["targets"].append(probe_target(args.base, target, fixtures, log))
    log("general")
    report["general"] = probe_general(args.base, fixtures, log)
    report["requests"] = REQUESTS
    if args.json:
        with open(args.json, "w", encoding="utf-8") as file:
            json.dump(report, file, indent=1, default=str)
    if args.markdown:
        with open(args.markdown, "w", encoding="utf-8") as file:
            file.write(markdown(report) + "\n")
    log(f"{REQUESTS} requests")


def pinned_version():
    try:
        with open(__file__.rsplit("/", 1)[0] + "/mirror-node/openapi.yml", encoding="utf-8") as file:
            for line in file:
                if line.strip().startswith("version:"):
                    return line.split(":", 1)[1].strip()
    except OSError:
        pass
    return "unknown"


if __name__ == "__main__":
    sys.exit(main())
