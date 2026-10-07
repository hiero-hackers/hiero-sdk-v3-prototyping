/**
 * Converts between the API types and the protobuf messages of the Hiero API.
 *
 * Not public API — the directory is absent from the `exports` map of the package.
 */
import { create } from "@bufbuild/protobuf";
import { AuthorityList, ContractAuthority, PublicKeyAuthority, type Authority } from "@hiero/base/authority";
import { KeyAlgorithm, type PublicKey } from "@hiero/base/keys";
import { AccountId, type TransactionId } from "@hiero/base/ledger";
import type { ExchangeRate } from "@hiero/base/nativeToken";
import {
    AccountIDSchema, KeyListSchema, KeySchema, SignaturePairSchema, ThresholdKeySchema, TransactionIDSchema,
    type AccountID, type Key, type SignaturePair, type TransactionID,
} from "./proto/services/basic_types_pb.js";
import { DurationSchema } from "./proto/services/duration_pb.js";
import { TimestampSchema } from "./proto/services/timestamp_pb.js";
import type { ExchangeRateSet } from "./proto/services/exchange_rate_pb.js";
import type { ResponseCodeEnum } from "./proto/services/response_code_pb.js";
import { HapiTransactionStatus } from "../consensusnode/transactions/HapiTransactionStatus.js";

/** Converts an account id. */
export function accountIdToProto(accountId: AccountId): AccountID {
    const message = create(AccountIDSchema, { shardNum: accountId.shard, realmNum: accountId.realm });
    if (accountId.num !== null) {
        message.account = { case: "accountNum", value: accountId.num };
    } else {
        const alias = accountId.evmAddress === null ? accountId.alias : accountId.evmAddress.bytes;
        if (alias === null) {
            throw new RangeError("The account id has neither a number nor an alias");
        }
        message.account = { case: "alias", value: alias };
    }
    return message;
}

/** Converts an account id back; only the numeric form occurs in a receipt. */
export function accountIdFromProto(message: AccountID): AccountId {
    return new AccountId({
        shard: message.shardNum,
        realm: message.realmNum,
        checksum: "",
        num: message.account.case === "accountNum" ? message.account.value : 0n,
    });
}

/** Converts a transaction id; only `validStart` is needed, which the public interface provides. */
export function transactionIdToProto(transactionId: TransactionId): TransactionID {
    const millis = transactionId.validStart.getTime();
    return create(TransactionIDSchema, {
        accountID: accountIdToProto(transactionId.accountId),
        transactionValidStart: create(TimestampSchema, {
            seconds: BigInt(Math.floor(millis / 1000)),
            nanos: (millis % 1000) * 1_000_000,
        }),
        nonce: transactionId.nonce ?? 0,
    });
}

/** A protobuf duration of whole seconds. */
export function durationToProto(seconds: number) {
    return create(DurationSchema, { seconds: BigInt(Math.trunc(seconds)) });
}

/** Converts an authority into the HAPI key structure. */
export function authorityToProto(authority: Authority): Key {
    if (authority instanceof PublicKeyAuthority) {
        return publicKeyToProto(authority.publicKey);
    }
    if (authority instanceof AuthorityList) {
        const keys = create(KeyListSchema, { keys: authority.children.map(authorityToProto) });
        return authority.threshold === authority.children.length
            ? create(KeySchema, { key: { case: "keyList", value: keys } })
            : create(KeySchema, {
                key: { case: "thresholdKey", value: create(ThresholdKeySchema, { threshold: authority.threshold, keys }) },
            });
    }
    if (authority instanceof ContractAuthority) {
        throw new RangeError("Not implemented yet: a contract authority in a transaction");
    }
    throw new RangeError("Unknown authority");
}

/** Converts a public key into the HAPI key structure. */
export function publicKeyToProto(publicKey: PublicKey): Key {
    const raw = publicKey.toRawBytes();
    return create(KeySchema, {
        key: publicKey.algorithm === KeyAlgorithm.ECDSA
            ? { case: "ECDSASecp256k1", value: raw }
            : { case: "ed25519", value: raw },
    });
}

/** Builds the signature pair of a signature, with the full public key as the prefix. */
export function signaturePair(publicKey: PublicKey, signature: Uint8Array): SignaturePair {
    return create(SignaturePairSchema, {
        pubKeyPrefix: publicKey.toRawBytes(),
        signature: publicKey.algorithm === KeyAlgorithm.ECDSA
            ? { case: "ECDSASecp256k1", value: signature }
            : { case: "ed25519", value: signature },
    });
}

/** Converts a response code into a transaction status. */
export function statusFromProto(code: ResponseCodeEnum): HapiTransactionStatus {
    return HapiTransactionStatus.ofCode(code);
}

/** The current and the next rate of an exchange rate set. */
export function exchangeRates(rates: ExchangeRateSet | undefined): readonly [ExchangeRate, ExchangeRate] {
    return [rate(rates?.currentRate), rate(rates?.nextRate)];
}

function rate(value: { expirationTime?: { seconds: bigint } | undefined; hbarEquiv: number; centEquiv: number }
    | undefined): ExchangeRate {
    const seconds = value?.expirationTime?.seconds ?? 0n;
    const hbarEquiv = value?.hbarEquiv ?? 0;
    const cents = hbarEquiv === 0 ? 0 : (value?.centEquiv ?? 0) / hbarEquiv;
    const expirationTime = new Date(Number(seconds) * 1000);
    return { expirationTime, exchangeRateInUsdCents: cents, isExpired: () => expirationTime.getTime() < Date.now() };
}
