# Contract: Requirement Verification Matrix

| Requirement | Automated verification | Human gate |
|---|---|---|
| FR-001 | Parse/inventory all eleven files; assert 37 declared types and every source element mapped once | Review retained empty `proto` namespace |
| FR-002 | Package dependency graph test against `requires` edges | Approve aggregate JPMS module variance |
| FR-003 | Manifest completeness plus reflection and signature snapshot tests | Approve deferred enforcement entries |
| FR-004 | Nullness, immutable collection, defensive byte ownership, one-of, bound, and inheritance fixtures | Review constraints not enforceable on interfaces |
| FR-005 | Compile a `Page<T>` implementation/consumer and inspect `CompletionStage` signatures | Review async error documentation |
| FR-006 | Compile `MethodDescriptor`; assert no gRPC/protobuf dependency and only `package-info` for proto | None beyond API review |
| FR-007 | Compile native-token/HBAR/token generic and enum fixtures | Confirm source `double` remains unresolved, not changed |
| FR-008 | Compile all identifier variants, nodes, constants, and `LedgerFactory`; run invariant fixtures | Approve finality and factory mappings |
| FR-009 | Compile setting specializations and registry contract; scan for registry state/provider loading | Approve registry abstraction |
| FR-010 | Compile key hierarchy/factory/format operations; forbidden bytecode/dependency scan | Mandatory key API and secret-handling review |
| FR-011 | Exhaustive Authority switch; invalid threshold tests; negative private-key compilation fixture | Mandatory Authority security review |
| FR-012 | Public API signature allowlist and consumer-only module-path compilation | Java API review |
| FR-013 | Bytecode/import scan for forbidden operational packages and unsupported-operation placeholders | Scope audit |
| FR-014 | Public-signature dependency allowlist; secret-pattern scan | Dependency and security approval |
| FR-015 | Two fresh generations plus checked-in tree comparison | Review every generated diff |
| FR-016 | Javadoc lint and source-link/constraint/question inventory assertions | Documentation review |
| FR-017 | Test-to-requirement metadata audit with no missing FR IDs | Approve any future test deferral |

## Positive Consumer Fixtures

At least one fixture imports each exported package. Combined fixtures implement or consume `Page`,
native-token generics, the address hierarchy, settings, key operations, Authority pattern matching,
method descriptors, and both token enums using only the API artifact and Java/JSpecify compile-time
metadata.

## Negative Fixtures

Compilation or structural tests must reject or identify:

- private key used as an Authority leaf
- an Authority implementation outside the sealed variants
- empty Authority children and threshold below 1 or above child count
- EVM/IP byte arrays of the wrong length where concrete construction exists
- more than one AccountId/ContractId selector where concrete construction exists
- null non-null fields, null collections, and null collection elements
- mutation through retained input arrays/collections or returned arrays/collections
- internal, generator, test, crypto-provider, transport, codec, and unapproved dependency types in
  public signatures
- generated methods whose only body throws `UnsupportedOperationException`

## Mandatory Reviews Before Acceptance

1. Java API mapping review by an accountable maintainer.
2. Key and Authority security review by an accountable security-capable maintainer.
3. Compatibility-baseline review of the complete generated signature snapshot.
4. Review and explicit approval of every manifest entry marked `deferred-enforcement`.
5. Review of generated diffs against authoritative source revisions.
