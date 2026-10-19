# TCK failures caused by checks of the SDK itself

The Hiero TCK sends many requests with invalid or missing values and expects the **network** to reject them with a
specific status (e.g. `MEMO_TOO_LONG`). The V3 API rejects many of these values **in the SDK**, before anything is
sent: a required attribute is missing, a validation annotation is violated, or the value cannot be represented by the
type of the API at all. The TCK test then fails, although the SDK behaves as specified.

This document lists these failures of the Java SDK (`sdk-java/impl`) and groups them, as the basis for a decision on how
to treat them (see [Decision](#decision)). About 100 TCK tests are affected.

**Source:** full TCK runs against a local Solo network on 2026-10-19 (hiero-sdk-tck revision `a3ea777`, 48 of 65 TCK
methods bound). The run used for the details was disturbed by an unstable host (Solo restarted its port-forward to the
consensus node several times), so cases from files that did not reach the check are taken from the list of causes of
the previous full run of the same day; for those the expected status was not recorded (`?`). The numbers are therefore
approximate.

## 1. Required attribute missing (~47 tests)

The TCK omits an id or a value and expects the network status. In V3 the attribute is not `@@nullable`, so the SDK
rejects the request.

| Missing attribute | Methods | TCK expects | Tests |
|---|---|---|---|
| `tokenId` | burn, mint, info, freeze, grant/revoke KYC, pause, wipe | `INVALID_TOKEN_ID` | 10 |
| `accountId` | getAccountInfo, updateAccount, KYC, freeze, createNode | `INVALID_ACCOUNT_ID`, `ACCOUNT_ID_DOES_NOT_EXIST`, `INVALID_NODE_ACCOUNT_ID` | 7 |
| `fileId` | append, update, delete, contents, info | `INVALID_FILE_ID` | 5 |
| `scheduleId` | delete, sign, info | `INVALID_SCHEDULE_ID` | 4 |
| `amount` | mint, wipe | ? | 4 |
| `topicId` | info, update | `INVALID_TOPIC_ID` | 2 |
| `tokenIds` | associate, dissociate | ? | 2 |
| `port` (service endpoint) | createNode | `INVALID_ENDPOINT` | 2 |
| `deleteAccountId`, `transferAccountId` | deleteAccount | `ACCOUNT_ID_DOES_NOT_EXIST` | 2 |
| `name`, `symbol` | createToken | `MISSING_TOKEN_NAME`, `MISSING_TOKEN_SYMBOL` | 2 |
| `key` | createAccount | `KEY_REQUIRED` | 1 |
| `transactionId` | getTransactionReceipt | `INVALID_TRANSACTION_ID` | 1 |
| `scheduledTransaction` | createSchedule | `INVALID_TRANSACTION` | 1 |
| `gossipCaCertificate` | createNode | `INVALID_GOSSIP_CA_CERTIFICATE` | 1 |

## 2. Validation annotations (~22 tests)

An annotation of the spec rejects the value in the SDK.

| Check (spec) | Attributes | TCK expects | Tests |
|---|---|---|---|
| `@@maxBytes(100)` | memo of account, file, topic, token, schedule | `MEMO_TOO_LONG` | 9 |
| `@@maxBytes(100)` | token `name`, `symbol` | `TOKEN_NAME_TOO_LONG`, `TOKEN_SYMBOL_TOO_LONG` | 4 |
| `@@min(1)` | `maxSupply` | `INVALID_TOKEN_MAX_SUPPLY` | 4 |
| `@@min(1)` | `amount` of mint, wipe | ? | 2 |
| an IPv4 address has 4 bytes (`IpAddress`) | endpoint of createNode | `INVALID_IPV4_ADDRESS` | 2 |
| `@@oneOrNoneOf(stakedAccountId, stakedNodeId)` | createAccount | **success**: as in V2 the value set last wins (`stakedNodeId`) | 1 |

The staking case is special: it does not expect a network status but the V2 behaviour "the last setter wins", which V3
rules out deliberately.

## 3. Point in time out of range (~19 tests)

The TCK sends extreme points in time (seconds close to the maximum of int64). A V3 `zonedDateTime` (Java: `Instant`)
cannot represent them, so the conversion of the TCK value fails before any check of the API.

| Attributes | TCK expects | Tests |
|---|---|---|
| `expirationTime` of account update, token create/update, topic update | `INVALID_EXPIRATION_TIME` | ~12 |
| `expirationTime` of file create/update | `AUTORENEW_DURATION_NOT_IN_RANGE` | 7 |

## 4. Number out of range (~9 tests)

The TCK sends values beyond the target type (e.g. `decimals` larger than int32, amounts larger than int64); the
conversion fails with an overflow.

| Attributes | TCK expects | Tests |
|---|---|---|
| `decimals` (createToken) | `INVALID_TOKEN_DECIMALS` | 3 |
| amounts of mint, wipe, createToken | ? | ~6 |

## 5. Value not representable in V3 (2 tests)

`TokenAssociateTransaction.tokens` and `TokenDissociateTransaction.tokens` are a `set` in the spec, because duplicates
are rejected by the protocol and the order is irrelevant. A duplicate token id therefore disappears when the request is
built, and the transaction succeeds. The TCK expects `TOKEN_ID_REPEATED_IN_TOKEN_LIST`.

## Not counted here

- **`grpcCertificateHash`** (41 tests, `createNode`) is a gap of the spec, not a check of an invalid input: the
  attribute is optional in the protocol but required in V3, so valid `createNode` requests fail as well.
- **Empty key list** to remove a key (8 tests, token and topic update) is a gap of the spec: "remove a key" is a
  separate, not yet specified operation (ADR-0004), an empty `AuthorityList` is not allowed.

## Decision

The groups are of different kinds:

- **Groups 1 and 2** are deliberate decisions of V3: reject early, in the SDK. Options:
  - (a) a documented list of deviations that the test report classifies as expected;
  - (b) the bindings translate the local error into the status the TCK expects;
  - (c) relax the API.

  Option (a) keeps the principle of V3 and makes the deviation visible. Option (b) would only simulate conformance;
  option (c) gives up a principle of V3.
- **Groups 3 and 4** are no deliberate checks: the TCK value does not fit the type of the API. Only a documented
  deviation is possible here.
- **Group 5 and the staking case** test the behaviour of V2 itself, not a network status.

Open, not decided yet.
