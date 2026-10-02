# metalang — grammar, parser and validator for the V3 API meta-language (prototype)

This module turns the language-agnostic meta-language defined in
[`guidelines/api-guideline.md`](../../guidelines/api-guideline.md) into something machines can check
deterministically. It is the foundation for the planned follow-up tools (per-language code generation,
API conformance checks of existing SDKs, per-language exceptions, generated contract tests).

Status: **prototype**. The scope is grammar + parser + semantic model + validator.

## Quick start

Requires Java 21+ and Maven.

```bash
mvn -f tooling/metalang/pom.xml verify
```

```bash
java -jar tooling/metalang/target/metalang-0.1.0-SNAPSHOT-cli.jar validate spec
```

```bash
java -jar tooling/metalang/target/metalang-0.1.0-SNAPSHOT-cli.jar validate --summary --min-severity=warning spec
```

```bash
java -jar tooling/metalang/target/metalang-0.1.0-SNAPSHOT-cli.jar rules
```

`validate` options: `--min-severity=error|warning|info` (what is printed), `--fail-on=error|warning|info|never`
(exit code 1 if a finding at or above this severity exists; default `error`), `--format=text|json`, `--summary`.
Output is sorted and contains paths relative to the given directory, so it is byte-for-byte reproducible.

## Pipeline

```
spec/*.md
  │ source/      MarkdownSchemaExtractor  "## API Schema" code block + skeleton checks (doc.*)
  │ parser/      ANTLR4 grammar → parse tree → immutable AST (ast/, records + sealed interfaces)
  │ semantic/    SpecModel: namespaces, type index, name resolution (local / requires / wildcard / qualified)
  │ validation/  Validator: one Check per guideline topic, every finding refers to a stable Rule id
  ▼
ValidationReport (sorted diagnostics) → CLI (text / JSON / summary)
```

- **Grammar:** [`MetaLang.g4`](src/main/antlr4/org/hiero/sdk/v3/metalang/grammar/MetaLang.g4) is the
  machine-readable definition of the language. It is not newline-sensitive; comments go to a hidden channel and
  the comment lines directly above a declaration (plus a trailing comment on its line) become its documentation.
- **AST:** the ANTLR parse tree is only used inside `AstBuilder`. Everything after that works on immutable records
  with source locations that point into the original Markdown file.
- **Rules:** [`Rule`](src/main/java/org/hiero/sdk/v3/metalang/diagnostic/Rule.java) is the catalog of all checks
  (id, severity, description, guideline section). Rule ids are stable; later tools (e.g. per-language exception
  files) will reference them.

## Lenient grammar: syntax variants found in the specs

The existing specs use a few constructs the guideline does not define. Rejecting them as syntax errors would make
the tool useless on today's specs, so the grammar accepts this **closed** set of variants. The validator reports
each of them under its own rule id. Everything else is a hard `syntax.error`.

| Variant | Example | Rule |
|---|---|---|
| `type` keyword before a complex type | `type NodeBody { ... }` | `syntax.type-keyword` |
| Bound on a generic argument at the use site (reported as **error**) | `PackedTransaction<$$R extends Receipt, ...> pack(...)` | `syntax.use-site-bound` |
| Trailing return type | `copy(): Copyable` | `syntax.trailing-return-type` |
| Method without return type (not allowed inside enums because it is ambiguous there) | `unsubscribe()` | `syntax.missing-return-type` |
| Annotation written inside a comment | `// @@throws(unknown-node-error) if ...` | `syntax.annotation-in-comment` |

## Open questions for the guideline

Building the grammar surfaced decisions that only the spec authors can make. The tool currently reports these
cases but does not resolve them:

1. **Enum attribute values:** enums may have `@@immutable` attributes (e.g. `HbarUnit.symbol`), but there is no
   syntax to assign them per value; specs put the values in comments (`enum.unassignable-fields`).
Resolved so far (now part of the guideline and enforced by the validator):

- Generic methods: `ReturnType name<$$T extends B>(...)`. `@@static` methods and namespace functions may be generic;
  generic instance methods must be `@@finalMethod` (not overridable), because overridable generic methods cannot be
  mapped to Go, C++ and Rust. `type<T>` is a typed type token. See "Generic methods" in the guideline.
- Attaching a function to a type from outside (`@@static EvmAddress EvmAddress.fromString(...)`) is not part of the
  language (syntax error); `@@static` methods of a type are declared inside the type.
- Namespace-level functions are allowed if they are `@@static` (`function.not-static` otherwise; they cannot be
  `@@streaming` or `@@threadSafe`).
- `@@name()` is tolerated for annotations without arguments but should be written `@@name`
  (`annotation.empty-parentheses`, warning).
- `@@threadSafe[(group)]` on a complex type, abstraction or enum makes all methods declared by the type thread-safe
  (see "Thread safety on types" in the guideline); a method-level `@@threadSafe` inside such a type is reported as
  `annotation.redundant-thread-safe`.
- `@@minSize`/`@@maxSize` constrain `list`, `set`, `map`, `bytes` and varargs; `@@minLength`/`@@maxLength` are for
  `string` only. Wrong usage is reported as `annotation.value-type` with a hint to the matching annotation.

## Current findings on `spec/`

`validate --summary --min-severity=warning spec` reports 7 errors and 43 warnings (no syntax errors). All
errors are `@@nullable` collections (`collection.nullable`) on update transactions and one mirror-node type, where
`null` currently means "leave unchanged" — this conflicts with the guideline rule "never define nullable collections"
and needs a design decision.

## Tests

`mvn verify` runs about 185 tests. JaCoCo fails the build below 95 % line / 90 % branch coverage (generated
ANTLR code excluded). Besides unit tests per component and positive/negative tests per rule, the
`RepositorySpecsTest` checks the real repository content: every spec under `spec/` must be free of syntax errors,
every `namespace` example of the guideline must parse, all reported locations must exist, and the report must be
deterministic.
