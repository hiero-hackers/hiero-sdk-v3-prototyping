# Java Base API Final Conformance Report

**Feature**: `001-java-base-api`  
**Result**: PASS  
**Verified**: 2026-08-05  
**Source revision**: `935e02a3fabc58d06045f0f84913e853849bc57f`  
**Generator**: `org.hiero.sdk.v3:spec-codegen-java:0.1.0-SNAPSHOT`

## Final Artifact Evidence

- Generated Java compilation units: 62
- Reviewed public top-level types: 49
- Signature baseline SHA-256: `a7f92e18f61df40d874016e6cb81a160efddd246c1d2ac11371f904572f432b3`
- Generation manifest SHA-256: `b5f6ba16837e0bcd00234b1eac79c58d4911028db21acf26d56ceb62a309ac58`
- Validated source namespaces: 11
- Final reactor: 22 generator tests and 23 API contract tests, zero failures or errors
- Clean regeneration: two fresh output trees, checked-in source tree, and manifests matched byte-for-byte
- Accountable Java API and security approvals: recorded in `api-review.md` and `security-review.md`

The negative consumer fixtures intentionally produced compiler diagnostics for private-key
Authority use, external Authority variants, invalid ledger inheritance, and invalid generic bounds.
Their rejection is a passing result.

## Functional Requirements

| Requirement | Result | Primary evidence |
|---|---|---|
| FR-001 | PASS | Eleven-namespace parser inventory and complete source-element mapping validation |
| FR-002 | PASS | JPMS export, namespace dependency, and split-package checks |
| FR-003 | PASS | Generated API, manifest mapping inventory, reflection checks, and reviewed signature snapshot |
| FR-004 | PASS | Null, collection, byte ownership, one-of, bounds, hierarchy, and invariant tests |
| FR-005 | PASS | `Page<T>` consumer and `CompletionStage` contract tests |
| FR-006 | PASS | Method descriptor consumer, reserved proto package, and forbidden transport dependency checks |
| FR-007 | PASS | Native-token, HBAR, exchange-rate, and token enum compilation fixtures |
| FR-008 | PASS | Ledger identifiers, addresses, nodes, constants, factories, and invariant tests |
| FR-009 | PASS | Network setting/profile/registry contracts and no-state checks |
| FR-010 | PASS | Key hierarchy and body-free operation contracts plus security scans |
| FR-011 | PASS | Sealed Authority variants, exhaustive consumer, private-key rejection, and threshold tests |
| FR-012 | PASS | Public-only JPMS artifact, API-only consumer fixtures, and reviewed 49-type baseline |
| FR-013 | PASS | Operational bytecode, dependency, provider-loading, and unsupported-body scans |
| FR-014 | PASS | Public dependency allowlist, secret scans, and accountable security approval |
| FR-015 | PASS | Deterministic renderer, staged replacement, manifest hashes, and clean regeneration test |
| FR-016 | PASS | Javadoc build, generated provenance headers, and retained-question inventory |
| FR-017 | PASS | Automated requirement-to-test metadata audit and final accountable reviews |

## Success Criteria

| Criterion | Result | Primary evidence |
|---|---|---|
| SC-001 | PASS | Complete inventory and documentation/question audits |
| SC-002 | PASS | Positive consumers cover every exported namespace |
| SC-003 | PASS | Public signatures contain no internal or unapproved implementation types |
| SC-004 | PASS | Negative fixtures cover concrete one-of, sealed, non-empty, threshold, range, and length constraints |
| SC-005 | PASS | FR-001 through FR-017 each map to automated verification |
| SC-006 | PASS | Fresh generations and checked-in output are byte-identical |
| SC-007 | PASS | No network, storage, retry, scheduler, transport, codec, provider, or crypto implementation found |
| SC-008 | PASS | T065 Java API and T066 security approvals explicitly recorded |

## Approved Deferrals

The final manifest contains five reviewed entries: `common#Page.data` under DE-002,
`common#Page.next():Page<$$T>` under DE-004, `keys#Key` and `keys#Key.bytes` under DE-003,
and `keys#namespace.generatePrivateKey(KeyAlgorithm):PrivateKey` under DE-005. No unapproved
deferred-enforcement entry exists. Provider implementations remain responsible for the TCK and
security obligations attached to those decisions.

## Commands

The final state passed:

```bash
./mvnw -Pvalidate-java-base-api verify
./mvnw -Pgenerate-java-base-api generate-sources
./mvnw verify
```
