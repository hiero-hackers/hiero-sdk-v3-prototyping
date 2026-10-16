# Authority API

## Description

An `Authority` describes *who or what may act* on an entity — sign for it, update it, mint, and so
on. Accounts, tokens, topics and other entities are guarded by an `Authority`. It is an immutable,
recursive value that is satisfied in one of three ways; composed, it forms a **multi-signature
(m-of-n)** structure:

- **`PublicKeyAuthority`** — satisfied by a signature from a single `PublicKey`.
- **`ContractAuthority`** — satisfied not by a signature but by the named smart **contract** being
  the active executing frame. With `delegatable = false` the contract must be the direct caller;
  with `delegatable = true` the authority may also be exercised via `delegatecall`. This is how,
  e.g., a token's `supplyAuthority` can be a contract so that only that contract's logic may mint.
- **`AuthorityList`** — a composition of child authorities with a `threshold`: at least
  `threshold` of the `children` must be satisfied. **n-of-n** ("all must sign") is simply
  `threshold == children.size()`; **m-of-n** is any smaller threshold. Children are themselves
  `Authority`s, so an account can be guarded by "Alice **and** (2-of-3 of {Bob, Carol, contract})".

Create authorities with the factory methods `of(...)`, `ofContract(...)` and `ofDelegatable(...)`.
An `Authority` received from the network (e.g. in account or token information) can be inspected
by matching on its three variants; the set of variants is closed. A private key is never part of an
`Authority` — only public information is.

## Design Notes

This namespace defines the **authorization model** for the SDK: the structure that decides *who or
what may act* on an entity (sign for it, update it, mint, …). It is the V3 replacement for the
`PublicKey` / `list<PublicKey>` placeholder used across the transaction, query, and mirror-node
specs, and corresponds to HAPI's `Key` message. The design and the alternatives it rejects are
recorded in [ADR-0004](../../docs/adr/0004-authority-authorization-sum-type.md).

- `PublicKeyAuthority` wraps a [`PublicKey`](keys.md); it deliberately *wraps* rather than *is* a
  `PublicKey` (see below).
- `ContractAuthority.delegatable = false` maps to HAPI's `ContractID`; `delegatable = true` maps to
  `DelegatableContractID`.

### Design rules (see ADR-0004)

- **Composition, not inheritance.** The leaves *wrap* `PublicKey` and `ContractId` rather than
  those types extending `Authority`. This keeps the dependency one-directional
  (`authority → {keys, ledger}`) and lets every variant be co-located so the type can be
  `@@sealed`. The cost, accepted deliberately, is that a bare `PublicKey` / `ContractId` cannot be
  passed where an `Authority` is expected — callers go through the factories, and the read side
  reads `.publicKey` / `.contractId` off the matched variant.
- **A private key is never an `Authority`.** Only public information appears in an authorization
  requirement; [`PrivateKey`](keys.md) has no wrapper here, so it is structurally excluded.
- **No in-band sentinels.** Every distinct meaning is its own type/variant — never a magic value
  or overloaded `null`. `threshold` is therefore always set and always meaningful (n-of-n is a
  real value, not `null`); an empty `AuthorityList` is forbidden (`@@minSize(1)`). "Remove this
  key" is an *operation* on the write side, not an `Authority` value, and is modelled separately
  (a future `KeyUpdate` type — see ADR-0004 follow-ups), not by an empty list.
- **Construction goes through the factories** (`of` / `ofContract` / `ofDelegatable`). They are the
  documented, forward-compatible path; the variants are public so that a received `Authority`
  can be destructured by pattern matching.
- **`@@sealed`** closes the set of variants so consumers can match exhaustively. The annotation is
  not yet part of the meta-language — it is introduced together with this namespace; see ADR-0004.

## API Schema

```
namespace authority
requires {PublicKey} from keys
requires {ContractId} from ledger

// Authorization requirement (HAPI `Key`). Pure data sum type — no behavior methods. Immutable
// value type with structural equality (provided idiomatically per language: Java records,
// Rust derive(PartialEq), …): two Authorities are equal iff their trees match. The set of
// variants (PublicKeyAuthority, ContractAuthority, AuthorityList) is closed, so consumers can
// match exhaustively.
@@sealed(PublicKeyAuthority, ContractAuthority, AuthorityList) abstraction Authority {}

// Leaf: satisfied by a signature from this public key.
@@finalType
PublicKeyAuthority extends Authority {
    @@immutable publicKey: PublicKey
}

// Leaf: satisfied by the named contract being the active executing frame (no signature).
@@finalType
ContractAuthority extends Authority {
    @@immutable contractId: ContractId
    @@immutable @@default(false) delegatable: bool   // false: the contract must be the direct caller (HAPI ContractID); true: also usable via delegatecall (HAPI DelegatableContractID)
}

// Composition: at least `threshold` of `children` must be satisfied.
// n-of-n is `threshold == children.size()`; m-of-n is any smaller threshold.
@@finalType
AuthorityList extends Authority {
    @@immutable @@minSize(1) children: list<Authority>   // never empty
    @@immutable @@min(1) threshold: int32                    // must be between 1 and children.size() (inclusive)
}

// --- factories (the blessed construction path; leaves are wrapped internally) ---

// n-of-n composition ("all must sign"): threshold is set to children.size().
@@static Authority of(children: Authority...)

// m-of-n composition (multi-signature): at least `threshold` of `children` must be satisfied.
@@static Authority of(@@min(1) threshold: int32, children: Authority...)

// Single-key leaf.
@@static Authority of(publicKey: PublicKey)

// Plain contract leaf (HAPI ContractID).
@@static Authority ofContract(contractId: ContractId)

// Delegatable contract leaf (HAPI DelegatableContractID): authority usable via delegatecall.
@@static Authority ofDelegatable(contractId: ContractId)
```

## Default Instances for Testing

```
// The authority of a single key: the default public key.
instance Authority = PublicKeyAuthority{publicKey: DEFAULT}

// Three distinct Ed25519 public keys: the public keys of RFC 8032, section 7.1 (TEST 1, TEST 2, TEST 3).
values PUBLIC_KEYS: list<PublicKey> = [
    keys.createPublicKey(algorithm: keys.KeyAlgorithm.ED25519,
            rawBytes: hex(text: "d75a980182b10ab7d54bfed3c964073a0ee172f3daa62325af021a68f707511a")),
    keys.createPublicKey(algorithm: keys.KeyAlgorithm.ED25519,
            rawBytes: hex(text: "3d4017c3e843895a92b70aa74d1b7ebc9c982ccf2ec4968cc0cd55f12af4660c")),
    keys.createPublicKey(algorithm: keys.KeyAlgorithm.ED25519,
            rawBytes: hex(text: "fc51cd8e6218a1a38da47ed00230f0580816ed13ba3303ac5deb911548908025"))
]
```

## Examples

### Build authorities

```
PublicKey alice = ...;
PublicKey bob   = ...;
PublicKey carol = ...;
ContractId dao  = ...;

// single key
Authority single = Authority.of(alice);

// all-of (3-of-3)
Authority allOf = Authority.of(
    Authority.of(alice), Authority.of(bob), Authority.of(carol));

// 2-of-3 multi-signature
Authority twoOfThree = Authority.of(2,
    Authority.of(alice), Authority.of(bob), Authority.of(carol));

// nested: Alice AND (2-of-3 of {Bob, Carol, DAO contract})
Authority nested = Authority.of(
    Authority.of(alice),
    Authority.of(2,
        Authority.of(bob),
        Authority.of(carol),
        Authority.ofContract(dao)));
```

### Inspect a received authority (read side)

```
// `key` comes typed as Authority from e.g. an account / token info query.
String describe(Authority e) {
    switch (e) {                                          // exhaustive via @@sealed
        case PublicKeyAuthority k         -> return "key " + k.publicKey.toString(...);
        case ContractAuthority c          -> return (c.delegatable ? "contract* " : "contract ")
                                                       + c.contractId.toString();
        case AuthorityList l              -> return l.threshold + "-of-" + l.children.size()
                                                       + " [" + l.children.map(describe).join(", ") + "]";
    }
}
```

## Testing

Tests of the factories and of the equality of authorities, written as given / when / then. The comment above each test
names its source. A test can pass only one value to a varargs parameter, so the compositions built by the factories
have one child; compositions of several children are built with `AuthorityList{…}`.

```tests
// Source: this spec (of(publicKey) is the single-key leaf); the Java, Go, Python and C++ SDKs (a public key is a key).
test "a single key becomes a public key authority of that key" {
    // given
    let publicKey = PUBLIC_KEYS[0]
    let leaf: Authority = PublicKeyAuthority{publicKey: publicKey}

    // when
    let authority = of(publicKey: publicKey)

    // then
    assertInstanceOf(expected: PublicKeyAuthority, actual: authority)
    assertEquals(expected: leaf, actual: authority)
}

// Source: this spec (n-of-n: threshold is set to children.size()); the Java, JavaScript, Go and Python SDKs (a key list
// without a threshold requires all of its keys).
test "a composition without a threshold requires all of its children" {
    // given
    let child = of(publicKey: PUBLIC_KEYS[0])
    let allOfOne: Authority = AuthorityList{children: [child], threshold: 1}

    // when
    let authority = of(children: child)

    // then
    assertInstanceOf(expected: AuthorityList, actual: authority)
    assertEquals(expected: allOfOne, actual: authority)
}

// Source: this spec (m-of-n composition); the Go, Python, JavaScript and C++ SDKs (a key list keeps its threshold).
test "a composition with a threshold keeps its threshold and its child" {
    // given
    let child = of(publicKey: PUBLIC_KEYS[0])
    let oneOfOne: Authority = AuthorityList{children: [child], threshold: 1}

    // when
    let authority = of(threshold: 1, children: child)

    // then
    assertEquals(expected: oneOfOne, actual: authority)
}

// Source: this spec (ofContract maps to HAPI ContractID, ofDelegatable to DelegatableContractID); the Go, Python and C++
// SDKs (a delegatable contract id becomes a key of its own kind).
test "a contract leaf is delegatable only when created as delegatable" {
    // given
    let contractId = DEFAULT(ContractId)
    let plainLeaf: Authority = ContractAuthority{contractId: contractId, delegatable: false}
    let delegatableLeaf: Authority = ContractAuthority{contractId: contractId, delegatable: true}

    // when
    let plain = ofContract(contractId: contractId)
    let delegatable = ofDelegatable(contractId: contractId)

    // then
    assertEquals(expected: plainLeaf, actual: plain)
    assertEquals(expected: delegatableLeaf, actual: delegatable)
    assertNotEquals(unexpected: plain, actual: delegatable)
}

// Source: this spec (@@default(false) delegatable).
test "a contract authority is not delegatable by default" {
    // when
    let authority = ContractAuthority{contractId: DEFAULT(ContractId)}

    // then
    assertFalse(condition: authority.delegatable)
    assertEquals(expected: ofContract(contractId: DEFAULT(ContractId)), actual: authority)
}

// Source: this spec (structural equality: two authorities are equal iff their trees match); the Python and C++ SDKs
// (equality of contract ids and key lists).
test "authorities are equal when their trees match" {
    // given
    let alice = of(publicKey: PUBLIC_KEYS[0])
    let bob = of(publicKey: PUBLIC_KEYS[1])
    let carol = of(publicKey: PUBLIC_KEYS[2])
    let dao = ofContract(contractId: DEFAULT(ContractId))

    // when
    let nested = AuthorityList{children: [alice, AuthorityList{children: [bob, carol, dao], threshold: 2}], threshold: 2}
    let same = AuthorityList{children: [of(publicKey: PUBLIC_KEYS[0]),
            AuthorityList{children: [of(publicKey: PUBLIC_KEYS[1]), of(publicKey: PUBLIC_KEYS[2]),
                    ofContract(contractId: DEFAULT(ContractId))], threshold: 2}], threshold: 2}

    // then
    assertEquals(expected: same, actual: nested)
    assertEquals(expected: of(publicKey: PUBLIC_KEYS[0]), actual: alice)
    assertNotEquals(unexpected: bob, actual: alice)
    assertNotEquals(unexpected: AuthorityList{children: [alice, bob], threshold: 1},
            actual: AuthorityList{children: [alice, bob], threshold: 2})
    assertNotEquals(unexpected: AuthorityList{children: [bob, alice], threshold: 2},
            actual: AuthorityList{children: [alice, bob], threshold: 2})
    assertNotEquals(unexpected: of(threshold: 1, children: alice), actual: alice)
}

// Source: this spec (@@minSize(1) children: an empty AuthorityList is forbidden, "remove this key" is no Authority).
test "a composition without children is rejected" {
    // when / then
    @@expectThrows(invalid-argument-error)
    AuthorityList{children: [], threshold: 1}
}

// Source: this spec (@@min(1) threshold of AuthorityList).
test "a composition with threshold 0 is rejected" {
    // when / then
    @@expectThrows(invalid-argument-error)
    AuthorityList{children: [of(publicKey: PUBLIC_KEYS[0])], threshold: 0}
}

// Source: this spec (@@min(1) threshold of the m-of-n factory of(threshold, children)).
test "the m-of-n factory rejects threshold 0" {
    // given
    let child = of(publicKey: PUBLIC_KEYS[0])

    // when / then
    @@expectThrows(invalid-argument-error)
    of(threshold: 0, children: child)
}
```

## Questions & Comments

- **`@@sealed` is forthcoming.** This namespace is written against it; a separate follow-up adds
  `@@sealed` to [`api-guideline.md`](../../guidelines/api-guideline.md) with the per-language
  mappings (Java `sealed`, Rust `enum`, TypeScript discriminated union). Until then the closed set
  is a convention. See [ADR-0004](../../docs/adr/0004-authority-authorization-sum-type.md).

- **Distinct from `keys.Key`.** [`keys.Key`](keys.md) models cryptographic *material* (`bytes`,
  `algorithm`, `type`); `Authority` is the logical *authorization* construct. They are separate
  hierarchies on purpose — `PublicKeyAuthority` wraps a `keys.PublicKey`.

- **Name is a coinage; `Endorsement` is the documented alternative.** No major ledger names the
  account-authorization construct "authority" (Bitcoin/Solana: "multisig"; Safe: "owners" +
  "threshold"; HAPI/V2: `Key`) — none of those terms can name a root that also covers a single key
  or a contract. The rationale and the `Endorsement` runner-up are in ADR-0004.

- **Building m-of-n from public keys is verbose** (`of(2, of(alice), of(bob), of(carol))`) because
  bare-type usability was surrendered. Convenience overloads taking `PublicKey...` directly
  (`of(2, alice, bob, carol)`) could reduce this, but a single-arg `PublicKey` would be ambiguous
  with the `of(publicKey)` leaf factory, so they are deferred until the ergonomics are judged
  against real usage.

- **Factory discoverability.** A developer looking for multi-signature support searches "multisig"
  / "threshold", not "authority". The keyword is carried in the doc text and on the m-of-n
  factory comment; renaming `of(threshold, …)` to an explicit `multiSig(…)` / `threshold(…)`
  factory is an open option (ADR-0004 rejects naming the *root type* `MultiSig`, not naming a
  factory).

- **Key clearing is not modelled here.** Removing a key (HIP-540, signalled on the wire by an empty
  `KeyList`) is a write-side *operation*, not an `Authority` value. It is deferred to a future
  `KeyUpdate` type on the update transactions — see ADR-0004.

- **Wire round-trip edge.** A redundant on-chain `ThresholdKey(n, n-children)` read back as an
  `AuthorityList` with `threshold == children.size()` canonicalises to the `KeyList` wire form on
  write-back — semantically identical, different bytes.

- **No byte encoding of an `Authority`.** There is no `toBytes()` / `fromBytes(bytes)` for the HAPI
  `Key` protobuf form, so key lists and threshold keys cannot be exchanged in serialized form; a single
  public key round-trips through `PublicKey.toBytes(...)`, an m-of-n does not. The TCK passes and
  expects keys as hex of the protobuf `Key` (`generateKey` with `keyList` / `thresholdKey`, the `key` of
  `createAccount` and `getAccountInfo`, see [`tck-binding.md`](../../docs/tck-binding.md)). Should
  `Authority` get `toBytes()` and `@@static fromBytes(bytes)`, and how do `ContractAuthority` and the
  delegatable contract form map to it?

- **The upper bound of `threshold` cannot be expressed.** `AuthorityList.threshold` and the `threshold` parameter of
  `of(threshold, children)` carry `@@min(1)`, but "at most `children.size()`" relates two values, and the
  meta-language has no annotation for that. Implementations therefore do not reject a threshold above the number of
  children (the network does), and the generated tests even expect `of(2147483647, child)` to succeed. Open: a
  cross-field constraint in the meta-language, or a documented "not checked by the SDK".
