# Java Base API Mapping Review

**Feature**: `001-java-base-api`

**Review gate**: T012 pre-implementation mapping approval

**Status**: Pending accountable Java API maintainer approval

**Prepared**: 2026-08-04

This record separates planning from approval. Its decision tables describe the complete set of
pre-implementation Java mapping variances and deferred-enforcement classes. Preparing this record
does not approve them, and an AI agent cannot complete the accountable approval below.

## Approval Scope

Approval authorizes only the prototype mappings in this document. It does not approve generated
source, final signatures, security behavior, or individual generated `deferred-enforcement`
manifest entries. T065 must review the realized API and approve or reject every such final manifest
entry; T066 independently gates the security review.

## Mapping Variances

| ID | Planned Java mapping | Reason and preserved source semantics | Primary evidence | Decision |
|---|---|---|---|---|
| MV-001 | Use package root `org.hiero.sdk.v3` rather than guideline example `org.hiero` | Isolates the V3 prototype without changing namespace relationships | `contracts/public-api.md` package mapping; `research.md` Decision 3 | Pending |
| MV-002 | Publish one JPMS module, `org.hiero.sdk.v3.base`, exporting eleven normalized namespace packages | Keeps one coherent base artifact; package dependency tests preserve source dependency directions | `plan.md` structure decision; `research.md` Decision 2 | Pending |
| MV-003 | Represent namespace/static and structural-value operations as body-free companion interfaces | Java cannot express abstract static operations; signatures, inputs, outputs, errors, and generic relationships remain represented without fake bodies or provider loading | `contracts/public-api.md` companion contracts; `research.md` Decision 7 | Pending |
| MV-004 | Move operational enum methods to `KeyFormatOperations`, passing the enum value explicitly | Java enum methods require implementations; the companion preserves decoding/support contracts without adding decoding behavior | `contracts/public-api.md` `KeyFormatOperations`; `research.md` Decision 7 | Pending |
| MV-005 | Use sealed structural ledger interfaces, generated final value variants, concrete sentinels, and ledger companions | Preserves source finality, exhaustive structure, typed zero constants, and abstract parsing/formatting/checksum contracts | `contracts/public-api.md` ledger mappings; `research.md` Decision 8 | Pending |
| MV-006 | Represent registry operations with body-free `NetworkSettingRegistry` and no global registry instance | Preserves register/lookup signatures and not-found errors without state, service loading, or provider selection | `contracts/public-api.md` registry contract; `research.md` Decision 7 | Pending |
| MV-007 | Permit direct public construction of the three generated Authority variants while retaining `AuthorityFactory` | Public variants are needed for exhaustive matching; the sealed set, no-private-key rule, non-empty children, and threshold invariant remain enforced | ADR-0004; `contracts/public-api.md` Authority mapping; `data-model.md` Authority section | Pending |

No additional Java mapping variance is authorized by this review. A new variance requires an
amended review and accountable approval before implementation.

## Deferred Enforcement Classes

Each generated deferred item must reference exactly one approved ID below plus its source element,
source location, affected requirements, and provider-verification obligation. An item that cannot
be classified here fails generation pending amended approval.

| ID | Affected contract surface | Deferred enforcement | Required current artifact behavior | Later verification | Decision |
|---|---|---|---|---|---|
| DE-001 | Provider-owned interfaces, including `Page`, native-token/HBAR abstractions, network-setting abstractions, and key interfaces | Concrete implementations cannot be forced by an interface to retain non-null immutable field observations | Emit explicit JSpecify annotations and source-linked Javadocs; never claim constructor enforcement | Provider TCK verifies null rejection, immutable observations, and applicable source constraints | Pending |
| DE-002 | `Page.data`, `NetworkSetting.getConsensusNodes`, and `NetworkSetting.getMirrorNodes` | Interfaces cannot enforce copy-in/copy-out or immutable non-null collection snapshots | Specify non-null collections, no null elements, and immutable snapshot ownership in signatures/Javadocs and manifest entries | Provider TCK tests retained inputs, returned collections, mutation attempts, and null elements | Pending |
| DE-003 | `Key`, `PublicKey`, and `PrivateKey` raw-byte and diagnostic behavior | Interfaces cannot enforce defensive byte ownership or provider-defined `toString()`/exception redaction | Expose copied-byte requirements, prohibit generated secret rendering, and include no key material in fixtures, diagnostics, or manifests | Security-capable provider TCK verifies copy-in/copy-out and secret-safe diagnostics | Pending |
| DE-004 | `Page.first()` and `Page.next()` | A return type cannot enforce immediate non-blocking return, execution policy, or exceptional-completion behavior | Return non-null `CompletionStage<Page<T>>`; document mirror-node exceptional completion and unspecified executor/cancellation policy | Provider async TCK verifies non-blocking invocation and declared terminal errors | Pending |
| DE-005 | `AuthorityFactory`, `KeyFactory`, `KeyFormatOperations`, `LedgerFactory`, `LedgerOperations`, `NativeTokenOperations`, `NetworkSettingRegistry`, and operational methods on provider interfaces | Parsing, formatting, checksums, conversion, key generation/import/export, signing, verification, expiration, and registry semantics have no implementation in this API-only feature | Generate body-free contracts with complete source signatures/errors; prohibit default methods, provider loading, state, and unsupported-operation bodies | Later operational features supply implementation tests, security review, and golden vectors where applicable | Pending |

## Enforcement Required In This Feature

The following are not approved for deferral when a generated concrete value boundary exists:

- non-null constructor inputs and immutable collection snapshots
- defensive copy-in/copy-out for arrays and content-based equality/hashing
- EVM and IP byte lengths
- AccountId and ContractId selector `oneOf` constraints
- Authority sealed variants, non-empty children, threshold bounds, and private-key exclusion
- unsigned/range checks representable at generated constructor boundaries
- source finality, override nullability narrowing, constants, and enum metadata
- deterministic diagnostics, generated headers, source mappings, and manifest/output hashes

Failure to enforce one of these concrete-boundary rules is a conformance defect, not a deferred
item.

## Maintainer Checklist

- [ ] Every MV entry preserves the authoritative source contract without adding operational behavior.
- [ ] Every DE entry is unavoidable at an implementation-free interface boundary.
- [ ] No provider loader, registry state, crypto implementation, parser, codec, network behavior, or fake unsupported method is authorized.
- [ ] Concrete values retain all immediately enforceable invariants listed above.
- [ ] T065 will review the generated surface and every realized deferred manifest entry separately.
- [ ] T066 remains an independent security approval gate.

## Accountable Approval

| Role | Name | Decision | Date | Scope or conditions |
|---|---|---|---|---|
| Java API maintainer | Pending | Pending | Pending | Must approve or reject MV-001 through MV-007 and DE-001 through DE-005 |

An approval must be an explicit human decision. The agent preparing or implementing this feature
must not fill this row on its own.

## Final Generated Review (T065)

| Reviewer | Decision | Date | Generated revision | Signature baseline | Deferred manifest entries | Conditions |
|---|---|---|---|---|---|---|
| Pending | Pending | Pending | Pending | Pending | Pending | Review occurs only after T064 |
