# Research: Java Common Code Generation

## Decision 1: Implement the first generator as a standalone Java tooling module

**Decision**: Build the first code-generation slice as a dedicated module under
`tools/spec-codegen-java/`.

**Rationale**: The feature targets Java output first, the repository already
contains extensive Java API guidance, and the new module isolates build
infrastructure from the documentation-first root.

**Alternatives considered**:

- **Root-level build for the whole repo**: Rejected because this repository is
  intentionally not a shippable SDK and should not become build-centric by
  default.
- **One-off shell or Python scripts**: Rejected because they would be faster to
  start but weaker as a long-term foundation for strongly typed parsing and
  rendering.

## Decision 2: Parse only the fenced `## API Schema` block in this slice

**Decision**: Limit parsing in the first slice to the fenced `## API Schema`
block inside `spec/base/common.md`.

**Rationale**: The schema block is the authoritative API input. The surrounding
Markdown remains valuable documentation, but broad Markdown parsing is not
needed to prove the generator architecture.

**Alternatives considered**:

- **Parse the entire Markdown document**: Rejected for the first slice because
  it expands scope without helping prove schema-to-code translation.
- **Hardcode the `Page<$$T>` type without schema parsing**: Rejected because it
  would bypass the central technical risk of the feature.

## Decision 3: Use ANTLR 4 for schema parsing

**Decision**: Model the meta-language grammar with ANTLR 4 and generate a Java
parser for the fenced schema block.

**Rationale**: The meta-language already has enough structure around
annotations, abstractions, generics, methods, and type expressions that a real
grammar provides better determinism and extensibility than line-by-line parsing.

**Alternatives considered**:

- **Regex or line-oriented parsing**: Rejected because annotations, nested type
  expressions, and future namespaces will become brittle quickly.
- **Custom hand-written parser**: Rejected for the first slice because ANTLR
  gives clearer grammar ownership and better future reuse.

## Decision 4: Normalize parsed output into a language-neutral intermediate representation

**Decision**: Convert parser output into an intermediate representation (IR)
before Java rendering.

**Rationale**: The IR keeps source semantics independent from the Java emitter,
lets the pipeline validate completeness before code emission, and creates the
right foundation for later languages.

**Alternatives considered**:

- **Render Java directly from the parse tree**: Rejected because it tightly
  couples parsing and emission and makes later multi-language support harder.
- **Use only the raw parse tree as the contract**: Rejected because a normalized
  model is easier to validate, test, and diff.

## Decision 5: Map `abstraction Page<$$T>` to a Java interface with getter-style field accessors

**Decision**: Emit `Page<T>` as a Java `interface` with getter-style accessors
for fields (`getData()`, `getSize()`, `getPageIndex()`), retain boolean methods
(`hasNext()`, `isFirst()`), and map async methods to
`CompletionStage<Page<T>>`.

**Rationale**: The Java guidance explicitly leaves `abstraction` open between
interface and abstract class, and interfaces are sufficient for this first
immutable API surface. Getter-style accessors follow the guide's field-accessor
discussion and make abstractions feel familiar to Java consumers, while
`CompletionStage` is the documented mapping for `@@async`.

**Alternatives considered**:

- **Abstract class**: Rejected because `Page<T>` does not need constructor
  enforcement or shared final behavior in the first slice.
- **Record-style field accessors (`data()`, `size()`, `pageIndex()`)**:
  rejected because record accessors are a better fit for concrete immutable
  types than for an abstraction-first Java API surface.

## Decision 6: Preserve `@@throws(mirror-node-error)` in IR and generated Javadoc, not as a generated exception type in this slice

**Decision**: Keep `@@throws` metadata in the IR and surface it in generated
documentation for async methods, but do not generate a concrete exception class
from `spec/base/common.md` in this slice.

**Rationale**: The source spec references `mirror-node-error` without declaring
an error type in `common.md`. Async methods cannot declare checked exceptions on
`CompletionStage` directly, so preserving the contract in metadata and Javadoc
is sufficient for the first slice without inventing cross-namespace exception
types too early.

**Alternatives considered**:

- **Generate a placeholder `MirrorNodeException` immediately**: Rejected
  because the exception is not declared in the scoped input spec and would
  introduce an under-specified cross-namespace type.
- **Drop `@@throws` completely in output**: Rejected because the constitution
  forbids silently losing source semantics.

## Decision 7: Use golden-file verification as the primary proof of deterministic output

**Decision**: Store approved Java output fixtures under the generator module's
test resources and compare generated output against them in automated tests.

**Rationale**: Golden verification makes the first slice auditable, easy to
review, and stable enough to expand later.

**Alternatives considered**:

- **Assert on fragments only**: Rejected because it would miss formatting or
  ordering drift.
- **Manual review only**: Rejected because the feature must detect drift
  automatically.
