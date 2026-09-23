# Requirement Verification Matrix

| OpenSpec requirement | Automated verification | Accountable review |
|---|---|---|
| Complete base specification coverage | `TraceabilityContractTest.shouldAccountForEveryBaseSpecification` | `mapping-matrix.md` declaration/member inventory |
| Limited structural value implementation | `StructuralValueContractTest`; `BoundaryContractTest.shouldKeepOperationalContractsBodyFree` | `structural-allowlist.md` |
| Operational implementation deferred | `BoundaryContractTest.shouldRejectOperationalImplementationPatterns` | API/security approval in `conformance-report.md` |
| Idiomatic Java contract mapping | `ConsumerCompilationContractTest.shouldCompileRepresentativeConsumer` | MV-001 through MV-013 |
| Immutable and null-safe public values | `StructuralValueContractTest` | API design review |
| Stable API and implementation boundaries | `ApiShapeContractTest` | dependency review |
| Security-sensitive contracts declarative | `BoundaryContractTest.shouldExcludePrivateKeysAndProviders` | key/authority mapping review |
| Async contracts preserve completion/error semantics | `ApiShapeContractTest.shouldExposeAsyncContractsWithoutExecutionInfrastructure` | common mapping entry |
| Reviewed and traceable direct maintenance | `TraceabilityContractTest`; signature SHA-256 assertion | direct-maintenance rule |
| Source questions remain unresolved | `TraceabilityContractTest.shouldRetainSourceQuestions` | retained-question inventory |
| Conformance independently verifiable | entire Maven `verify` suite | final conformance report |
