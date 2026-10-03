# metalang — grammar, parser and validator for the V3 API meta-language (prototype)

This module turns the language-agnostic meta-language defined in
[`guidelines/api-guideline.md`](../../guidelines/api-guideline.md) into something machines can check
deterministically. It is the foundation for the planned follow-up tools (per-language code generation,
API conformance checks of existing SDKs, per-language exceptions, generated contract tests).

Status: **prototype**. The scope is grammar + parser + semantic model + validator.

## Quick start

Requires Java 21+ and Maven. All commands are meant to be run from the **repository root**
(`hiero-sdk-v3-prototyping/`) and can be copied 1:1.

### Build

```bash
mvn -f tooling/metalang/pom.xml verify
```

This runs all tests and creates the self-contained CLI jar `tooling/metalang/target/metalang-0.1.0-SNAPSHOT-cli.jar`.
For a quick build without tests:

```bash
mvn -f tooling/metalang/pom.xml -q package -DskipTests
```

### Validate the specs

All findings of all specs under `spec/`:

```bash
java -jar tooling/metalang/target/metalang-0.1.0-SNAPSHOT-cli.jar validate spec
```

Only the number of findings per rule (errors and warnings):

```bash
java -jar tooling/metalang/target/metalang-0.1.0-SNAPSHOT-cli.jar validate --summary --min-severity=warning spec
```

Only errors, as JSON:

```bash
java -jar tooling/metalang/target/metalang-0.1.0-SNAPSHOT-cli.jar validate --min-severity=error --format=json spec
```

`validate` options: `--min-severity=error|warning|info` (what is printed), `--fail-on=error|warning|info|never`
(exit code 1 if a finding at or above this severity exists; default `error`), `--format=text|json`, `--summary`.
Output is sorted and contains paths relative to the given directory, so it is byte-for-byte reproducible.

### Show the linked model

The `model` command prints the [linked model](#linked-model-input-for-generators) as JSON: every type with its
resolved supertypes, effective fields and methods (inherited members included, type arguments substituted,
`declaredIn` shows where a member comes from), enum attributes and values, functions and constants.

The complete model of all specs, written to a file:

```bash
java -jar tooling/metalang/target/metalang-0.1.0-SNAPSHOT-cli.jar model --fail-on=never spec > spec-model.json
```

A single namespace (including its sub-namespaces), e.g. everything about transactions:

```bash
java -jar tooling/metalang/target/metalang-0.1.0-SNAPSHOT-cli.jar model --fail-on=never --namespace=consensusnode.transactions spec
```

A single type, e.g. `AccountCreateTransaction` with all members inherited from `Transaction`:

```bash
java -jar tooling/metalang/target/metalang-0.1.0-SNAPSHOT-cli.jar model --fail-on=never --type=consensusnode.transactions.accounts.AccountCreateTransaction spec
```

The model is always printed. Without `--fail-on=never` the command ends with exit code 1 as long as the specs have
validation errors (`"errors"` in the JSON shows how many), so a pipeline does not silently continue with a broken
model. The output is deterministic: a diff of two runs shows exactly how a spec change affects the model.

### Generate the Java API

Generates the Java API into `tooling/metalang/target/generated/java` (ignored by git):

```bash
java -jar tooling/metalang/target/metalang-0.1.0-SNAPSHOT-cli.jar generate --language=java --fail-on=never --output=tooling/metalang/target/generated/java spec
```

Without `--fail-on=never` nothing is generated as long as the specs have validation errors. See
[Java generator](#java-generator) for what is generated. `--show-deferred` lists the record types that are not
generated yet and why.

To check the result with a JDK 25 (`javac`/`javadoc` of JDK 25 on the `PATH`; the jspecify jar is in the local Maven
repository after the build), compile all generated modules and render their Markdown Javadoc:

```bash
javac -Xlint:all -Werror --release 25 --module-source-path "tooling/metalang/target/generated/java/*/src/main/java" --module-path ~/.m2/repository/org/jspecify/jspecify/1.0.0/jspecify-1.0.0.jar -d tooling/metalang/target/generated/classes $(find tooling/metalang/target/generated/java -name '*.java')
```

```bash
javadoc -Xdoclint:all,-missing -quiet --module-source-path "tooling/metalang/target/generated/java/*/src/main/java" --module-path ~/.m2/repository/org/jspecify/jspecify/1.0.0/jspecify-1.0.0.jar -d tooling/metalang/target/generated/apidocs --module $(ls tooling/metalang/target/generated/java | paste -sd, -)
```

The rendered Javadoc is then in `tooling/metalang/target/generated/apidocs/index.html` (only packages that contain
generated types are exported and therefore documented).

### List all rules

```bash
java -jar tooling/metalang/target/metalang-0.1.0-SNAPSHOT-cli.jar rules
```

## Pipeline

```
spec/*.md
  │ source/      MarkdownSchemaExtractor  "## API Schema" code block + skeleton checks (doc.*)
  │ parser/      ANTLR4 grammar → parse tree → immutable AST (ast/, records + sealed interfaces)
  │ semantic/    SpecModel: namespaces, type index, name resolution (local / requires / wildcard / qualified)
  │ model/       LinkedModel: every reference resolved, effective members with substituted type arguments
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

## Linked model (input for generators)

`LinkedModel.of(report.model())` turns the semantic model into a fully resolved model:

- **Resolved types** (`model.Type`): `BasicType` (`list<…>`, `map<…>`, `type<…>`, …), `DeclaredType` (identified by
  its `QualifiedName`, e.g. `consensusnode.transactions.Transaction`, plus type arguments), `TypeVariable` (with its
  owning type or method), `WildcardType`, `AnyType`, `VoidType`, `FunctionType`. Types are values: `equals` means
  "same type". `UnresolvedType` only appears in specs with validation errors — generators must not run on those.
- **Definitions**: `ComplexTypeDefinition`, `EnumDefinition` (attribute list and values with their arguments),
  `FunctionDefinition`, `ConstantDefinition`; documentation, annotations and source locations are kept.
- **Effective members**: `fields()` / `methods()` contain own and inherited members with the type arguments of the
  supertypes substituted (also across several levels), e.g. `AccountCreateTransaction.pack(...)` returns
  `PackedTransaction<AccountCreateReceipt, AccountCreateTransaction>` and `Hbar.to(...)` returns `Hbar`. Inherited
  members come first (in `extends` order), an override takes the position of the inherited member, `@@static`
  methods are not inherited. `declaredFields()` / `declaredMethods()` contain only what the type itself declares.

The validator uses the linked model as well (override and enum-attribute type checks with substitution).

## Java generator

First increment of the Java mapping (`generator/java`, rules from `guidelines/api-best-practices-java.md`):

- **One JPMS module per spec folder**: `spec/consensus-node-client/` becomes the module `org.hiero.consensus.node.client`
  (folder name with `-` replaced by `.`), laid out like a Maven module (`<module>/src/main/java/`).
- **One package per namespace**: `consensusnode.transactions` becomes `org.hiero.consensusnode.transactions`.
- **`module-info.java`**: `requires transitive` for every module whose namespaces are used (specs only contain public
  API, so these types are part of the module's API); `requires static transitive org.jspecify` (the nullness
  annotations are part of the exported API, otherwise `javac -Xlint:exports` warns); one `exports` per package. As
  long as a package contains no generated types, its `exports` is written as a comment — the module system rejects
  exporting a package that only has a `package-info.java`.
- **`package-info.java`**: the `## Description` of the spec file(s) as **Markdown Javadoc** (`///`, JEP 467); no
  `package-info.java` is generated for a namespace without description. The generated code therefore requires
  Java 23+; the SDK targets **Java 25**.
- **API documentation is written for SDK users**: the generator adds no spec file names, line numbers or
  meta-language terms. Spec-author content belongs in `## Design Notes` / `## Questions & Comments`, which are not
  copied; `doc.internal-reference` (warning) flags descriptions and declaration comments that refer to spec files,
  `@@annotations`, ADRs or TODOs.
- The generator refuses (with a list of all problems) to generate if a namespace is spread over several spec folders
  (split package), a spec file is not inside a folder, or the folders depend on each other in a cycle.
- Output is deterministic; every file starts with a "Generated ... Do not edit." line.

- **Enums**: values with their attribute arguments as typed Java literals (`(byte) 1`, `1L`, `new BigInteger("…")`,
  `Duration.ofSeconds(…)`, `Kind.A`, `List.of(…)`), `private final` fields set by the constructor (non-nullable
  references checked with `Objects.requireNonNull`), a getter per attribute (`getSymbol()`), Markdown Javadoc from the
  declaration comments, `@Deprecated`. **Methods** are generated with their full signature (`@@async` →
  `CompletionStage<T>`, generics, varargs, nullness annotations), but their behaviour is only described in the spec,
  so the body throws `UnsupportedOperationException` for now. `implements` of an abstraction is written as a comment
  until abstractions are generated.
- **Records** (`RecordGenerator`): a complex type becomes a `record` if it is no abstraction, has at least one
  attribute, all its attributes (inherited ones included) are `@@immutable`, it extends no complex type, inherits no
  `@@finalMethod` (that supertype becomes an abstract class) and no type extends it — records are final and cannot
  extend classes. Types without attributes never become records. Generated:
  - components = effective attributes (inherited first), documented with `@param`;
  - a compact constructor with `Objects.requireNonNull` for non-nullable references, `List/Set/Map.copyOf` for
    collections, `clone()` for `bytes`, and the checks of `@@min`/`@@max`/`@@minLength`/`@@maxLength`/`@@minSize`/
    `@@maxSize`/`@@pattern` (Java regex, `find` semantics like the validator)/`@@urlPattern` (`java.net.URI`, absolute
    with host) throwing `IllegalArgumentException`;
  - a second constructor without the `@@default` attributes;
  - `bytes` components: accessors return copies, `equals`/`hashCode` compare the array content (records would
    compare the references), and `toString` prints only the length (`byte[32]`), because the content may be key
    material or a large body;
  - `@@deprecated` attributes: an explicit accessor with `@Deprecated` (on the component it only causes a javac
    warning);
  - methods as stubs (see Enums); `toString()`/`hashCode()`/`equals(ANY)` get `@Override`.

  A record is only generated once every type it refers to (attributes, methods, bounds) is generated as well;
  otherwise it is **deferred** (`generate --show-deferred`). Today 23 records are generated and 110 deferred — most
  of them wait for abstractions (`Authority`, `TransactionStatus`, `NativeTokenUnit`, …) or for types that refer to
  them (`AccountId` → `Network`).
- **Type mapping** (`JavaTypes`): `intX`/`uintX` → `byte`/`short`/`int`/`long`/`BigInteger` by width, primitives
  unless nullable or a type argument, `bytes` → `byte[]`, collections → `List`/`Set`/`Map`, time types →
  `java.time`, `seconds`/`duration` → `Duration`, `type<T>` → `Class<? extends T>`, `ANY` → `Object`. Not mapped
  yet (reported as generation problem): function types, `streamResult`, `@@streaming`. Java keywords used as names
  get a trailing `_`.

Not generated yet: complex types, abstractions, constants, namespace-level functions.

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

## Guideline decisions driven by the tooling

Building the grammar surfaced gaps in the guideline. Resolved so far (now part of the guideline and enforced by the
validator):

- Enum attributes are declared in an attribute list after the enum name and assigned positionally per value:
  `enum HbarUnit(symbol: string, baseUnitFactor: int64) extends NativeTokenUnit { TINYBAR("tℏ", 1) }`. Inherited
  attributes must be listed with the same type; `enum.*` rules check counts and literal types.
- `...` is no valid syntax in enums; an incomplete enum is marked with a comment.
- Spec skeleton: `## Description` → optional `## Design Notes` (rationale for spec authors, not part of the API
  docs) → `## API Schema` → `## Examples` → `## Testing` → `## Questions & Comments`.
- `duration` is a basic type with millisecond precision (for e.g. HTTP timeouts); `seconds` stays whole-second.

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

`validate --summary --min-severity=warning spec` reports 6 errors (no syntax errors), all `collection.nullable` on
update transactions and `@@oneOf` payloads, where `null` currently means "not set" — this conflicts with
the guideline rule "never define nullable collections" and needs a design decision (tracked in `TODO.md` → Meta-language).

## Tests

`mvn verify` runs about 520 tests; JaCoCo fails the build below 95 % line / 90 % branch coverage (generated ANTLR code
excluded). Besides unit tests per component, the suite contains these systematic checks:

| Test | What it guarantees |
|---|---|
| `RuleFixturesTest` | Every rule of the `Rule` catalog has a fixture directory `src/test/resources/rule-fixtures/<rule-id>/` that produces **exactly** the expected set of rule ids (`*.ml` = schema wrapped into the spec skeleton, `*.md` = raw Markdown, optional `also.txt` = further expected ids). A new rule without fixture fails the build. |
| `GuidelineExamplesTest` | Every meta-language code block of `guidelines/api-guideline.md` parses (as-is, in a namespace, in a type body, or as type expressions). Non-meta-language blocks are listed with a reason; stale entries fail the test. |
| `GrammarEdgeCasesTest` | ~60 boundary cases of the grammar: keywords as names, nested generics, comments everywhere, literal formats, CRLF, and constructs that must be rejected — always with a diagnostic, never an exception. |
| `MarkdownEdgeCasesTest` | CommonMark fences (longer fences, indentation, info strings), ATX headings, CRLF, Unicode. |
| `RobustnessTest` | Seeded random mutations and every prefix of every real spec never crash the tool and never report a location outside the document; results do not depend on document order; AST locations point at the element; the textual form of every type and literal parses back to itself. |
| `RepositorySpecsTest` | All specs under `spec/` are free of syntax errors and the report is deterministic. |
| `ModelCommandTest` | `metalang model` output for the example specs in `src/test/resources/model-golden/spec` equals the golden file `model-golden/model.json` byte for byte; filters, exit codes and determinism on the real specs. After an intended change, regenerate the golden file (command in the test's Javadoc). |
| `RecordGeneratorTest` | Which types become records (inherited attributes, extended types, inherited `@@finalMethod`, type arguments of supertypes), deferral (transitive, through methods, bounds, wildcards; unmapped types), generated source details, and the **runtime behaviour** of the golden records: they are compiled in-process and called (null checks, every constraint, `URI` check, defensive copies, default constructor, `bytes` equality, method stubs). |
| `JavaGeneratorTest` | Golden files for the example specs (`generator-golden/java`), module/package rules and the three structural errors, Markdown comment escaping, and: the modules generated for **all real specs compile** with `-Xlint:all -Werror` through the module system. |
| `LinkedRepositorySpecsTest` | Linking all real specs leaves no unresolved reference, every declared type exists, self types are substituted (`Transaction`, `NativeToken`), and linking is deterministic. |

## Known limitations

- **Generic bounds** are checked on the head type only (`G<$$T extends B>` used as `G<Other>`); arguments that are
  generic parameters are not checked.
- **Error positions** can be one token late when the input so far is a valid prefix of another construct
  (e.g. `b int8` is the start of a method declaration).
- **Nesting** of brackets is limited to 100 levels (`syntax.nesting-too-deep`) to keep the recursive parser and
  validator away from stack exhaustion.
- **Markdown:** only ATX headings (`## ...`) are recognized, not setext headings (`---` underlines).
- **Lexical scope:** identifiers are ASCII; number literals support digits, `_`, sign and decimals, but no exponent
  or hex notation. `@@pattern` values are evaluated with Java regular expressions (`find` semantics).
