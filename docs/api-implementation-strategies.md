# API implementation with OpenSpec and deterministic generation

This repository explores two ways to turn shared specifications into SDK APIs:

- **OpenSpec guides feature development:** a developer or coding assistant implements the API using requirements,
  design decisions, tasks, and tests.
- **Deterministic generation automates API translation:** a generator translates formal schemas into repeatable
  language-specific APIs and tests, while developers implement the remaining behavior.

The generator strategy described here refers to `deterministic-generator` at commit
`eff4522813c0a5811344ec1162766e639d7c6d84`. The OpenSpec strategy describes implementing a shared contract through
SDK-specific requirements, design decisions, implementation tasks, and verification.

## What implementing the API includes

An SDK feature usually needs four kinds of work:

| Work | Example |
| --- | --- |
| Public declarations | Types, methods, parameters, return types, and exports |
| Local behavior | Validation, parsing, formatting, conversions, and ownership of mutable data |
| Service behavior | Network retrieval, retries, signing, and resource management where the feature requires them |
| Verification | Contract tests, behavioral tests, consumer usage, and integration checks |

The two strategies distribute this work differently. OpenSpec coordinates it through a feature plan. The generator
automates the parts its formal inputs and language mappings can express. Both still need decisions about intended
behavior and evidence that the resulting SDK satisfies it.

## Strategy 1 OpenSpec guided implementation

```text
Shared requirements -> Proposal and design -> Tasks -> Code and tests -> Verification
```

1. **Read the shared contract.** Identify required behavior, public API structure, errors, and exclusions.
2. **Create the implementation plan.** Record the proposal, requirements, language-specific decisions, and tasks in
   the SDK's OpenSpec project. Reference the shared source and revision.
3. **Implement the API and behavior.** A developer or assistant chooses idiomatic types and writes the code.
4. **Verify the result.** Test the required scenarios, public signatures, consumer usage, and relevant build checks.
   Validate the OpenSpec documents separately.
5. **Accept the change.** Review the result and archive it when the project's instructions permit.

The main artifacts are `proposal.md`, `specs/<capability>/spec.md`, `design.md`, and `tasks.md`, subject to the
project's conventions. The CLI supplies guidance and structural validation; assistant skills can write artifacts
and implement tasks.

This approach allows feature-specific judgment and language-specific design. Consistency across SDKs depends on
preserving shared requirements and reviewing their mappings and tests. Repeating the workflow does not guarantee
identical source code.

### What the team maintains

The team maintains shared requirements, SDK-specific artifacts, implementation code, and tests. For each required
scenario, it records how the SDK expresses the behavior and which test verifies it. An assistant can draft these
artifacts and implement tasks, while review checks that the interpretation matches the contract.

For example, "immutable data" needs a concrete ownership decision. Returning a read-only view of a caller-owned
mutable collection may allow later source mutations to change stored state. The design and tests must establish the
guarantee promised by the requirement.

When requirements change, update the affected specifications, design, tasks, implementation, and tests together.
The task checklist helps coordinate this work, but checked tasks are not a substitute for verification evidence.

## Strategy 2 Deterministic generation

```text
Formal schemas and test definitions -> Validate -> Generate APIs and tests
                                   -> Implement remaining behavior -> Verify
```

1. **Update the formal inputs.** Define the API under `spec/`, including supported annotations and applicable test
   definitions. Record language-specific mapping choices in generator configuration.
2. **Validate the schemas.** The `metalang` tooling parses declarations, resolves references, and checks rules.
3. **Generate the API and tests.** The generator applies predefined language mappings. The same versioned inputs
   and tooling produce repeatable output.
4. **Implement remaining behavior.** Developers fill behavior that generation cannot derive in the appropriate
   implementation modules, while preserving the generated API contract.
5. **Verify behavior and conformance.** Run tests and use `metalang check`, where supported, to detect differences
   between the implementation's public API and the expected declarations.

The branch generates structural contract tests and also translates formal behavioral tests into Java and TypeScript.
Generator, test, and conformance support varies by language. See the branch's
[tooling documentation](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/eff4522813c0a5811344ec1162766e639d7c6d84/tooling/metalang/README.md).

**Generated output is a reference API, not a complete SDK.** Behavior that cannot be derived remains in method stubs.
The Java project was initially generated into `sdk-java/generated`, copied into `sdk-java/impl`, and then maintained
by hand. Later specification changes are regenerated, compared, and carried into `impl` manually. Automating that
update path remains an open question in the
[implementation runbook](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/eff4522813c0a5811344ec1162766e639d7c6d84/docs/rebuilding-the-spike.md#1-how-sdk-javaimpl-came-about).

### What the team maintains

The team maintains the formal schemas, language generators, generator configuration, support modules, test
definitions, and actual SDK implementations. A language rule that repeats across many features can be encoded once
in its generator and applied consistently.

Some local behavior can also be derived, such as storage, accessors, defensive copies, and supported validation
constraints. Arbitrary parsing algorithms, remote retrieval, and other behavior described only in prose require
implementation beyond those generated rules.

Generated modules have a clear ownership rule: regenerate them from their versioned inputs rather than editing
their output. Handwritten support and implementation modules are maintained separately. The branch documents this
separation in
[ADR 0007](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/eff4522813c0a5811344ec1162766e639d7c6d84/docs/adr/0007-separate-generated-and-hand-written-modules.md).

Determinism means repeatable output for the same relevant inputs, configuration, and tooling. It does not establish
that the mapping is correct or that the specified behavior is complete. A repeatable mistake remains a mistake.

## Comparison

| Aspect | OpenSpec guided implementation | Deterministic generation |
| --- | --- | --- |
| Main input | Requirements, scenarios, design, and tasks | Formal API schemas, test definitions, and configuration |
| API translation | Performed by a developer or assistant | Performed by predefined generator rules |
| Language choices | Recorded and implemented per feature | Encoded in generators and configuration |
| Runtime behavior | Implemented as part of the feature | Implemented where generation cannot derive it |
| Tests | Written from requirements and scenarios | Generated where supported, with additional tests as needed |
| Consistency | Shared requirements, tests, and review | Repeatable mappings, generated tests, and conformance checks |
| Main investment | Planning and implementation per feature and language | Generator tooling plus implementation and maintenance |
| Updating a feature | Reconcile artifacts, code, and tests | Regenerate, reconcile the implementation, and verify conformance |

OpenSpec is useful for capturing intent, resolving decisions, and tracking acceptance. Generation is useful for
repeated API patterns and enforcing mappings across languages. A generator defect can affect many generated APIs;
independent behavioral and consumer checks remain valuable.

## Pros and cons

### OpenSpec guided implementation

| Pros | Cons |
| --- | --- |
| Makes requirements, decisions, scope, and progress explicit | Requires keeping documents, tasks, code, and tests synchronized |
| Handles complex behavior that is difficult to express formally | Developers or assistants must translate requirements into code |
| Allows idiomatic design for each language | Different SDK teams can interpret the same requirement differently |
| Supports incremental implementation and review | Structural validation still needs behavioral tests and review |
| Can be adopted without building a generator | Implementation and test-writing effort repeats across languages |

**Where the advantages matter:** OpenSpec makes a feature's reasoning reviewable before and during implementation.
This is useful when behavior needs discussion, several designs are plausible, or a feature includes networking and
other behavior that requires judgment. A focused change can be delivered without first extending a formal language
or writing another generator rule.

**Where the costs appear:** Each SDK still needs a correct translation of the requirements and meaningful tests.
For example, two teams may choose different error timing or ownership rules while both believing they followed the
same prose. Shared examples, explicit mappings, and scenario coverage reduce that risk. Documents also need upkeep:
an outdated design or checked task can give reviewers an inaccurate picture of the implementation.

### Deterministic generation

| Pros | Cons |
| --- | --- |
| Produces repeatable output from the same versioned inputs | Requires substantial generator development and maintenance |
| Applies consistent language mappings across the API | New language features or exceptions may require generator changes |
| Generates contract tests and supported behavioral tests | Generated tests cover what the schema and test language express |
| Conformance checks detect public API drift | Conformance does not establish correct runtime behavior |
| Shared specification changes can reach several languages | Generator defects can affect multiple SDKs simultaneously |
| Reduces repetitive API and test scaffolding | Behavior the generator cannot derive still requires implementation |

For the referenced generator branch, carrying regenerated changes into the handwritten Java implementation remains
manual, as described in the implementation runbook above. Plan for that maintenance work alongside generation.

**Where the advantages matter:** A generator is useful when many declarations follow the same rules. A correction to
an established mapping can be applied across the generated API, and regenerated diffs show the effect of a schema or
tooling change. Generated validation and ownership tests reduce repetitive work and check the same kinds of boundary
cases across supported targets.

**Where the costs appear:** Every new construct needs a defined mapping, diagnostics, generator support, and tests
for the affected languages. An unsupported construct may require tooling work before a feature can be generated.
Mapping changes can affect a large API surface, so generator review and regression coverage become important.
The benefit also depends on keeping handwritten implementations aligned with regenerated references.

If both the implementation and its generated tests rely on the same incorrect mapping, they can agree while missing
the intended behavior. Keep independent examples, published test vectors, consumer checks, and integration tests
where those provide meaningful additional evidence.

## What the tests establish

The branch generates tests, but the available evidence depends on which kind of test is being run:

| Verification | OpenSpec guided implementation | Deterministic generation |
| --- | --- | --- |
| Document or schema validation | Checks the OpenSpec requirement structure | Parses schemas, resolves references, and checks supported semantic rules |
| Structural contract tests | Written for the feature or reused from existing checks | Generated for supported constructors, constraints, ownership rules, and other contracts |
| Behavioral tests | Written from the required scenarios | Generated from formal test definitions where supported, plus additional tests as needed |
| Public API conformance | API shape tests and consumer compilation can verify the intended mapping | `metalang check` compares expected public declarations with the implementation where supported |
| Integration behavior | Tested against real dependencies when the feature requires it | Also needs relevant integration evidence; generated API declarations alone cannot provide it |

For example, a generated range test can check that a value just outside a declared bound is rejected. A formal
behavioral test can describe a particular input and expected result for a parser. A conformance check can establish
that the parser method exists with the expected signature. These are separate observations.

The referenced branch translates formal behavioral test blocks into Java and TypeScript tests. Its test and
conformance capabilities vary by target language, so one language's coverage should not be assumed for another.
See [generated tests and test definitions](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/eff4522813c0a5811344ec1162766e639d7c6d84/tooling/metalang/README.md#generated-tests).

Method tests can fail against generated stubs because behavior is still missing. That failure is evidence of
unfinished implementation. Generating a test, compiling it, and passing it against the real implementation are
different milestones.

## Pagination example

Both strategies start from the same behavior: a page exposes ordered immutable data, supports position inspection,
and provides asynchronous navigation with defined failures.

With **OpenSpec**, the implementation team records the language mapping, implements the page abstraction, and writes
tests for state, navigation, and errors.

With **deterministic generation**, the schema drives the page declarations and applicable generated tests. Developers
still supply concrete retrieval behavior and verify that it satisfies the contract.

In both cases, a correct method signature alone does not prove that navigation returns the correct page or reports
the correct failure.

Useful acceptance checks for this feature include:

- Reading page state preserves the declared values and ordering.
- The implementation protects data according to its documented ownership guarantee.
- Position inspection does not trigger another retrieval.
- Navigation completes with the expected page from the same operation.
- Retrieval failures reach the caller through the documented asynchronous result.

With OpenSpec, the team implements and verifies these observations through its feature tasks. With generation,
supported schema rules and formal tests supply part of that work; concrete navigation still needs implementation
and appropriate behavioral verification.

## How later changes are handled

The examples below illustrate maintenance work rather than changes currently requested for this repository.

### A new validation constraint

Suppose the shared contract introduces a supported minimum-value constraint.

With **OpenSpec**, record the requirement and its boundary scenarios, add the check in each implementation, and
verify accepted and rejected values. Review each language mapping for range and error differences.

With **generation**, encode the constraint in the schema and regenerate. If the annotation is already supported,
the generator can produce the corresponding checks and boundary tests. Carry the changes into the real
implementation and run its tests. If the annotation is unsupported, extend and test the generator first.

### A new parsing operation

Suppose a feature adds a method that parses text into a value.

With **OpenSpec**, define the accepted syntax, invalid inputs, errors, and output behavior. Choose an implementation,
write scenario tests, and implement it in the relevant SDKs.

With **generation**, the schema can produce the method declaration and applicable contract checks. Formal test
definitions can provide shared input/output cases where the test generator supports them. The parsing algorithm
still needs implementation unless a generator rule explicitly supplies it.

### A correction to a language mapping

With **OpenSpec**, revise the recorded mapping and update affected SDK types, tests, and consumer examples. Search
for other features that use the same convention so the correction remains consistent.

With **generation**, correct the mapping rule or configuration, add a regression check, and regenerate the affected
targets. Review the wider output diff and reconcile affected handwritten implementations. The correction is reusable,
but its impact can extend beyond the original feature.

## Choosing an approach

These are practical fit considerations, not measured performance or cost claims:

| Situation | Practical fit |
| --- | --- |
| Requirements are still being clarified | Use OpenSpec to capture decisions and scenarios before committing to mappings. |
| A feature has complex runtime behavior | Use explicit design and implementation tasks; generation can still provide declarations and tests. |
| Many SDKs repeat stable API patterns | Generation can reduce repeated translation work once the mappings are supported. |
| A new language target is being introduced | Account for generator, support-module, test, and implementation work for that language. |
| A project has few features and limited tooling capacity | A direct implementation workflow may require less initial infrastructure. |
| API consistency must be checked repeatedly | Generated references and conformance checks can strengthen ongoing review. |

The amount of reusable mapping work matters more than declaration count alone. A large API with many exceptions may
still need extensive generator and implementation work. A smaller API with highly regular constraints may benefit
from generation. Neither strategy removes the need for a well-defined shared contract.

## A possible combined workflow

The following proposed workflow combines feature planning with generated contracts:

1. Use **OpenSpec** to define scope, resolve decisions, and plan acceptance.
2. Update the **formal schemas and test definitions** required by that plan.
3. Use the **generator** to produce consistent APIs and applicable tests.
4. Implement the remaining behavior in the **implementation modules**.
5. Run **behavioral tests, consumer checks, and API conformance checks**.
6. Accept and archive the OpenSpec change according to the project's conventions.

Keep the responsibilities explicit: OpenSpec artifacts explain intent and acceptance; formal schemas define the
API structure used by generation; generators define language mappings; implementation modules supply remaining
behavior. Record source revisions and reconcile shared inputs when they change instead of treating diverging copies
as independent authorities.

For a combined feature review, check that:

- The OpenSpec plan and formal schema describe the same scope and public contract.
- Generated output is reproducible and its diff contains the expected API changes.
- Supported generated tests are run against the implementation, with omitted coverage identified.
- Handwritten behavior and consumer usage satisfy the scenarios, including errors and boundaries.
- API conformance, build checks, and relevant behavioral tests pass.
- Remaining tasks and archive status accurately reflect the feature's acceptance state.

OpenSpec validation checks the specification documents. API conformance checks the public declarations. Tests and
consumer verification establish whether the implemented behavior works.
