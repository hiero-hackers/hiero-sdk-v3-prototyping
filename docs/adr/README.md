# Architecture Decision Records

This directory collects the Architecture Decision Records (ADRs) for the V3 SDK
specification. Each ADR captures **one** architecturally significant decision
with its motivation, the alternatives considered, and the trade-offs accepted.

ADRs are sequentially numbered (`NNNN-kebab-case-title.md`) and are never
deleted — superseded ADRs link forward to the decision that replaces them.

## Index

| # | Title | Status |
|---|---|---|
| [0001](0001-transaction-and-packed-transaction-split.md) | Split the transaction API into `Transaction` and `PackedTransaction` | Accepted |
| [0002](0002-defer-paid-query-payer-customization.md) | Defer payer customization for paid queries | Accepted |
| [0003](0003-three-level-address-hierarchy-with-nullability-narrowing.md) | Three-level address hierarchy with nullability narrowing | Accepted |
| [0004](0004-authority-authorization-sum-type.md) | Model HAPI authorization keys as an `Authority` sum type | Proposed |
| [0005](0005-schedule-service-reuses-transaction-model.md) | Model the schedule service on the existing transaction model, without schedule-specific types | Accepted |
| [0006](0006-generic-methods-final-or-static.md) | Generic methods are allowed only as `@@finalMethod` instance methods or as `@@static` methods | Proposed |
| [0007](0007-separate-generated-and-hand-written-modules.md) | Every module is either completely generated or completely hand-written | Accepted |
| [0008](0008-one-folder-per-target-language.md) | One folder per target language: configuration, hand-written and generated modules together | Accepted |
