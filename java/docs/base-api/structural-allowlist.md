# Structural Implementation Allowlist

The API artifact may contain method bodies only for deterministic, side-effect-free mechanics that establish or
preserve a public value. Each body must derive its result solely from parameters and already validated fields.

## Allowed bodies

- Null, non-negative range, uint16 port, exact byte-length, one-of selector, non-empty collection, and threshold checks.
- Defensive `byte[]` cloning and immutable `List.copyOf` / `Set.copyOf` snapshots.
- Field accessors, record-generated equality/hash/string behavior, and explicit structural equality/hashing for
  inheritance- or array-based values.
- `Authority` construction that wraps public keys/contracts or creates a validated threshold tree.
- `AccountId.fromEvmAddress`, `ContractId.fromEvmAddress`, and `IpAddress.fromBytes` because they only validate and
  construct fields.
- Canonical representations that require no external interpretation: numeric shard/realm/selector strings, checksum
  suffix concatenation, IPv4 dotted-quad formatting, key-alias base32-hex formatting, safe key diagnostics, and fixed
  constants/unit metadata.
- Private constructors for namespace anchors and constants containers.

## Forbidden bodies

The API must not implement parsing, checksums, cryptography, encoding/decoding, token conversion, clock observation,
randomness, transaction-id generation, mutable registries, provider discovery/selection, I/O, persistence, scheduling,
retries, transports, protobuf handling, network calls, or service/business logic. It must not use
`UnsupportedOperationException` placeholders; deferred operations remain abstract or interface methods.

Future implementation/provider modules may implement body-free companions and abstract behavioral methods. They must
depend on this API and must not require public API source edits or leak provider classes through public signatures.
