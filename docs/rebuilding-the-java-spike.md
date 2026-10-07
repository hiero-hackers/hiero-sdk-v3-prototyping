# Rebuilding the Java spike

A runbook for recreating the `createAccount` vertical slice in `generated/java` after a regeneration
destroyed it. Written to be executed by an agent; every claim here was verified against a running
Solo network.

**Why this exists:** `metalang generate --language=java` overwrites every file carrying the generator
header, so all hand-filled method bodies are gone. The hand-written files in the `internal` packages
have no header and survive — orphaned, and the module stops compiling. The spike is the only thing
that proves the V3 API can actually reach a network, so it has to come back.

## 0. First: try to restore it, do not retype it

The spike is in git. Recreating it from scratch is the last resort.

```bash
git log --oneline --diff-filter=A -- 'generated/java/*/ClientRuntime.java'   # 3caad63 "Durchstich :)"
git checkout <commit> -- generated/java/org.hiero.base generated/java/org.hiero.consensus.node.client
```

Then jump to step 4 and build. If the specs or the generator changed in between, the restored
generated files will be stale — in that case restore only the `internal` packages (they are pure
hand-written code and independent of the generator) and refill the stubs listed in step 3.

## 1. Preconditions

```bash
sdk env                                  # JDK 25, see .sdkmanrc
solo one-shot single deploy              # a local Solo network
```

The protobuf wiring is **not** part of the spike: the generators own it (`java.protobuf` in
`sdk-java/generator.properties`). After a regeneration the dependency on `hiero-sdk-protobuf` and
`requires org.hiero.sdk.protobuf` are already there. What is missing is only gRPC and BouncyCastle —
step 4.

## 2. The hand-written files

Non-exported helper packages inside the generated modules. None carry a generator header.

| File | Responsibility |
|---|---|
| `org.hiero.base/…/keys/internal/Oids.java` | the three ASN.1 OIDs everything dispatches on |
| `…/keys/internal/Pem.java` | PEM envelope ↔ DER |
| `…/keys/internal/Secp256k1.java` | curve, Keccak-256, RFC-6979 signer, fixed-length encoding |
| `…/keys/internal/Ed25519PrivateKey.java` / `Ed25519PublicKey.java` | Ed25519 over BouncyCastle, PKCS#8/SPKI |
| `…/keys/internal/EcdsaPrivateKey.java` / `EcdsaPublicKey.java` | ECDSA secp256k1, same formats |
| `…/keys/internal/KeyCodec.java` | reads PKCS#8/SPKI, dispatches on the OID — never on key length |
| `…/ledger/internal/DefaultTransactionId.java` | the only concrete `TransactionId` + its generator |
| `…/nativeToken/internal/DefaultExchangeRate.java` | the only concrete `ExchangeRate` |
| `org.hiero.consensus.node.client/…/client/internal/Protobuf.java` | API types ↔ HAPI messages |
| `…/client/internal/Grpc.java` | `MethodDescriptor` from service+method name, unary call |
| `…/client/internal/ClientRuntime.java` | channels, node selection, submit, receipt polling, the two registries |
| `…/client/internal/DefaultPackedTransaction.java` | the only concrete `PackedTransaction` |
| `…/transactions/HapiTransactionStatus.java` | one constant per HAPI `ResponseCodeEnum` value, derived from the vendored proto |

`HapiTransactionStatus` is mechanical — regenerate it instead of typing it:

```bash
python3 - <<'EOF'
import re
src = open('protobuf/consensus-node/services/response_code.proto').read()
body = src[src.index('enum ResponseCodeEnum'):]; body = body[:body.index('\n}')]
seen, out = set(), []
for name, num in re.findall(r'^\s*([A-Z][A-Z0-9_]*)\s*=\s*(\d+)\s*;', body, re.M):
    if name not in seen: seen.add(name); out.append((name, int(num)))
print(len(out), 'constants, SUCCESS =', dict(out)['SUCCESS'])
EOF
```

## 3. The generated stubs to fill

Fill the body, change nothing else — that is what keeps `metalang check` green (it allows method
bodies and additional types, not changed declarations).

| Type | Methods |
|---|---|
| `keys/KeysFactory` | all 12 (`generate*`, `create*`) |
| `keys/PrivateKey`, `PublicKey` | nothing — the `internal` subclasses override them |
| `keys/KeyFormat`, `KeyEncoding`, `KeyContainer`, `ByteImportEncoding` | `supportsType`, `decode` |
| `ledger/AccountId` | `fromString`, `toString`, `toStringWithChecksum`, `fromEvmAddress`, `validateChecksum` |
| `ledger/IpAddress` | `fromString`, `toString`, `fromBytes` |
| `ledger/TransactionId` | `generateTransactionId` |
| `hedera/Hbar` | `to`, `toBaseUnits`, `toTinybars` |
| `authority/AuthorityFactory` | all 5 |
| `consensusnode/client/ClientFactory` | both `createClient` |
| `consensusnode/transactions/Response` | `queryReceipt` |
| `transactions/accounts/AccountCreateTransaction` | `signWithOperator` only — the other 8 overrides stay stubs, the TCK never calls them |

## 4. Build and verify

`org.hiero.base/pom.xml` needs `org.bouncycastle:bcprov-jdk18on:1.83` and
`requires org.bouncycastle.provider;`. `org.hiero.consensus.node.client/pom.xml` needs `io.grpc:grpc-api`
and `grpc-protobuf-lite` (compile) plus `grpc-netty-shaded` (runtime), each with the six exclusions
(`jsr305`, `error_prone_annotations`, `checker-qual`, `j2objc-annotations`,
`com.google.android:annotations`, `animal-sniffer-annotations`) — without them JPMS breaks.

```bash
./mvnw -f sdk-java/support install
./mvnw -f sdk-java/protobuf install
./mvnw -f generated/java install -DskipTests
./mvnw -f generated/java-tck/contract install
./mvnw -f tck/runtime/java install
./mvnw -f generated/java-tck/server clean package          # clean: see trap 11
TCK_DIR=../hiero-sdk-tck tck/run-tck.sh java src/tests/crypto-service/test-account-create-transaction.ts
```

**Done when:** 34 of 42 tests pass, and the conformance check still reports *provides the API of the
specs*:

```bash
java -jar tooling/metalang/target/metalang-*-cli.jar check --language=java --fail-on=never \
     --config=sdk-java/generator.properties --project=generated/java spec
```

`--fail-on=never` is required: the specs carry 6 pre-existing `collection.nullable` errors, and
without it the check refuses to run at all.

`TCK_SERVER_LOG=debug` prints the stack trace of every failed call; the summary at the end of a run
lists the distinct causes.

## 5. Traps

These cost time the first time. Each is a real failure that was observed, not a precaution.

1. **`KeysFactory.createPrivateKey` must throw `IllegalArgumentException`** when the bytes are not
   PKCS#8. `JavaConverters.key()` catches exactly that to fall back to the public-key branch; any
   other exception makes a public key unparseable and every `createAccount` fails.
2. **The receipt status must report `code() == 22`.** `JavaTckRuntime` compares against a hard-coded
   `SUCCESS = 22` because `BasicTransactionStatus` has no such constant. `statusJson` uses
   `Enum.name()`, so the status must be an enum whose names match HAPI — hence `HapiTransactionStatus`.
3. **`AccountCreateReceipt` null-checks `exchangeRate` and `nextExchangeRate`.** `queryReceipt()` has
   to supply real values; `null` throws inside the constructor.
4. **`HieroClient` cannot reach its network.** It is a record of operator, `Network` and signer, and
   `Network` carries no `ConsensusNode`. The `NetworkSetting` arrives at `ClientFactory` and ends
   there. Bridge: a registry in `ClientRuntime` keyed by the client instance.
5. **`Response` cannot query anything.** It is a record holding only a `TransactionId`. Bridge: a
   second registry `TransactionId → ClientRuntime`, filled on submit.
6. **`TransactionId.generateTransactionId` takes an `Address`**, but `TransactionId` holds an
   `AccountId` — convert shard/realm/num.
7. **Back-date the valid start by ~10 s** and keep it strictly increasing per process, otherwise the
   node answers `INVALID_TRANSACTION_START`.
8. **Use `Transaction.signedTransactionBytes`**, not `bodyBytes` + `sigMap`: those fields are
   `[deprecated = true]` in the proto and `-Xlint:all -Werror` turns their builders into errors. The
   signed bytes are the serialized `TransactionBody` in both forms.
9. **Set the defaults HAPI requires** — the TCK sends only what a test needs. Without them a bare
   `createAccount` is rejected: `transactionFee` 2 ℏ, `autoRenewPeriod` 90 days,
   `transactionValidDuration` 120 s.
10. **Poll the receipt.** One query is almost always too early. Retry on `UNKNOWN`,
    `RECEIPT_NOT_FOUND`, `BUSY`, `PLATFORM_NOT_ACTIVE` until a timeout, and check the precheck code of
    the `TransactionResponse` before polling at all.
11. **`generated/java-tck/server` needs `clean package`.** An incremental `package` keeps the
    `Class-Path` of the previous manifest: a newly added dependency lands in `target/lib` but not on
    the class path, and the server dies with `NoClassDefFoundError`.
12. **`exports …internal;` must be unqualified.** A qualified `exports … to <module>` warns that the
    target module is not in the graph, and `generated/java` compiles with `-Werror`.
13. **gRPC is an automatic module.** `module-info.java` of the client module needs
    `@SuppressWarnings("requires-automatic")`, again because of `-Werror`.
14. **Javadoc is checked.** `-Xlint:all -Werror` plus doclint `all,-missing` with `failOnWarnings`:
    every new type and method needs well-formed documentation, including in non-exported packages.

## 6. What the spike is not

It is a measurement, not a design. The registries in trap 4 and 5 are workarounds for spec problems
recorded in the `## Questions & Comments` of `spec/consensus-node-client/client.md` and
`transactions.md`. ADR-0007 still leaves open where an implementation of the API may live; until that
is decided, editing `generated/java` in place is a deliberate, temporary exception — see
"The AccountCreateTransaction spike" in `CLAUDE.md`.

The 8 failing tests are API gaps, not bugs: `Authority` has no `toBytes`/`fromBytes` (key lists,
threshold keys), `PublicKey` has no EVM address, and a precheck rejection surfaces as `-32603`
instead of `-32001` with a status.
