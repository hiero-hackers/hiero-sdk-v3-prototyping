# Key and Authority Security Review

**Feature**: `001-java-base-api`  
**Scope**: Threat analysis for the implementation-free Java key and Authority public API  
**Prepared**: 2026-08-05  
**Approval status**: Approved by accountable security reviewer at T066

## Assets and Security Objectives

- Private key bytes, signatures, imported key text, and provider diagnostics must not be disclosed.
- `Authority` must never accept `PrivateKey` as an authorization leaf.
- Generated concrete values must own mutable inputs and preserve structural invariants.
- The API artifact must contain no cryptographic, parsing, encoding, provider-loading, registry,
  transport, network, storage, scheduling, or serialization implementation.
- Generation diagnostics and provenance must contain only repository-relative paths and
  non-sensitive deterministic metadata.

## Trust Boundaries

| Boundary | Trusted input | Untrusted or deferred behavior |
|---|---|---|
| Specification to generator | Reviewed `spec/base`, guidelines, ADR, config | Malformed syntax, unsupported annotations, unresolved references, mapping omissions |
| Generator to checked-in API | Validated AST, approved mappings, deterministic renderer | Filesystem failures and stale generated files |
| API to provider | Static interfaces, sealed Authority variants, concrete value invariants | Provider cryptography, key storage, byte copying, redaction, timing, and thread safety |
| Consumer to concrete values | Constructor arguments validated by generated classes | Malicious nulls, mutable arrays/lists, invalid thresholds and selectors |
| Build to provenance | Git revision and repository-relative input/output hashes | Host paths, usernames, environment data, timestamps, and secret-like values |

## Threats, Abuse Cases, and Mitigations

| ID | Threat or abuse case | Mitigation in this feature | Verification evidence |
|---|---|---|---|
| TH-001 | A private key is used directly as an Authority leaf | `Authority` is sealed to three variants; `PublicKeyAuthority` accepts only `PublicKey`; no generic `Key` or `PrivateKey` constructor/factory path exists | Negative compilation fixture and sealed-permits reflection test |
| TH-002 | Mutable Authority children change after validation | `AuthorityList` uses `List.copyOf`, rejects empty children, and enforces `1 <= threshold <= size` | Construction and mutation tests |
| TH-003 | Key bytes leak through generated implementations | Key types remain interfaces; generated code contains no key-byte storage, custom diagnostics, or crypto bodies | Bytecode/source scan and manifest DE-003 entry |
| TH-004 | Provider implementations return mutable key arrays | Interface Javadocs require defensive copies; enforcement is explicitly deferred under approved DE-003 | Manifest assertion and future provider security TCK |
| TH-005 | Exceptions, fixtures, manifests, or logs contain secret material | Fixtures contain no real key material; manifest rejects secret-like metadata; diagnostics are repository-relative and never print parsed values | Secret-pattern scans and manifest tests |
| TH-006 | API silently selects a crypto provider or algorithm implementation | `KeyFactory` and operation contracts are body-free interfaces; no service loader, provider dependency, or implementation artifact exists | Dependency and bytecode/source scans |
| TH-007 | Enum decoding methods introduce parser/codec behavior | Operational enum methods map to body-free `KeyFormatOperations` with explicit enum parameters | Public signature and no-body tests |
| TH-008 | Authority can be extended outside the reviewed set | Java sealed interface lists only generated public variants in the same named module | Negative external-variant compilation fixture |
| TH-009 | Generator replaces valid output after malformed or unmapped input | Parse, resolution, annotation, mapping-completeness, rendering, and collision checks finish before staged replacement | Generator failure-path and atomic-tree tests |
| TH-010 | Build provenance leaks host or secret data | Manifest fields are fixed, sorted, repository-relative, SHA-256 based, and scanned for secret-like assignments | Determinism, path, host-metadata, and secret tests |

## Residual Risks and Deferred Enforcement

- Provider implementations can violate byte copy-in/copy-out, diagnostic redaction, constant-time
  behavior, algorithm validation, key destruction, and secure storage requirements. DE-003 and
  DE-005 require provider security TCKs and a separate operational security review.
- Java interfaces cannot enforce immediate asynchronous return, immutable provider collections,
  or thread safety. The manifest identifies these obligations; this API claims no provider
  implementation conformance.
- Public direct construction of Authority records is an approved compatibility variance. Concrete
  constructors enforce all representable invariants, but application policy still decides whether
  a structurally valid Authority is acceptable.
- Public record-generated `toString()` for `PublicKeyAuthority` delegates to the provider-defined
  `PublicKey.toString()`. Providers must redact key bytes; this cannot be enforced by the API-only
  artifact and remains covered by DE-003.

## Required T066 Review Evidence

- Passing positive and negative Authority compilation fixtures.
- Passing threshold, immutability, sealed-variant, and recursive equality tests.
- `KeySecurityContractTest` results for signatures, deferred ownership, secret scans, forbidden
  dependencies, and absence of operational bytecode/default methods.
- Final generated manifest and public-signature snapshot.
- Clean-regeneration evidence and final dependency tree.
- Confirmation that no private-key Authority path, provider loading, cryptographic body, secret
  fixture, or unapproved deferred-enforcement item exists.

## Accountable Security Approval (T066)

| Reviewer | Decision | Date | Reviewed revision | Conditions |
|---|---|---|---|---|
| `housniabdellatif` | Approved | 2026-08-05 | `935e02a3fabc58d06045f0f84913e853849bc57f` | Explicit human approval of key handling, sealed Authority structure, secret-safety controls, operational exclusions, and documented residual risks; provider obligations remain gated by DE-003 and DE-005 TCKs |
