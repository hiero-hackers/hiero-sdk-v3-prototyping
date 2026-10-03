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

Generates the Java API into `generated/java` at the repository root. The directory is under version control, so that
every change of the specs or the generator shows up as a diff of the generated code. Files are only rewritten if their
content changes, and generated files that are no longer produced (e.g. of a removed or deferred type) are deleted;
files without the generator's header line are never touched. `--output` takes any directory.

```bash
java -jar tooling/metalang/target/metalang-0.1.0-SNAPSHOT-cli.jar generate --language=java --fail-on=never --config=sdk-java/generator.properties --output=generated/java spec
```

Without `--fail-on=never` nothing is generated as long as the specs have validation errors. See
[Java generator](#java-generator) for what is generated. `--show-deferred` lists the types that are not
generated yet and why.

To check the result with a JDK 25 (`javac`/`javadoc` of JDK 25 on the `PATH`; the jspecify jar is in the local Maven
repository after the build), compile all generated modules and render their Markdown Javadoc:

```bash
javac -Xlint:all -Werror --release 25 --module-source-path "generated/java/*/src/main/java" --module-path ~/.m2/repository/org/jspecify/jspecify/1.0.0/jspecify-1.0.0.jar -d tooling/metalang/target/generated/classes $(find generated/java -name '*.java')
```

```bash
javadoc -Xdoclint:all,-missing -quiet --module-source-path "generated/java/*/src/main/java" --module-path ~/.m2/repository/org/jspecify/jspecify/1.0.0/jspecify-1.0.0.jar -d tooling/metalang/target/generated/apidocs --module $(ls generated/java | paste -sd, -)
```

Class files and the rendered Javadoc are build output and stay in `tooling/metalang/target`; the Javadoc is then in
`tooling/metalang/target/generated/apidocs/index.html` (only packages that contain
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
- **`@NullMarked` modules**: every `module-info.java` is annotated with jspecify's `@NullMarked`, so unannotated types
  are non-null — the default of the meta-language. Only `@@nullable` declarations get `@Nullable`; `@NonNull` is never
  generated (it would be about 700 annotations, see "Prefer `@NullMarked` and `@Nullable`" in the Java guide).
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
  references checked with `Objects.requireNonNull`), an accessor per attribute (`symbol()`, `@Override` if it
  implements an interface accessor), Markdown Javadoc from the declaration comments, `@Deprecated`. **Methods** are
  generated with their full signature (`@@async` → `CompletionStage<T>`, generics, varargs, nullness annotations), but
  their behaviour is only described in the spec, so the body throws `UnsupportedOperationException` for now;
  inherited methods get `@Override`.
- **Accessors and setters** (all kinds of types): attribute `name` → accessor `name()` (no `get` prefix, so records,
  enums and classes can implement the same interface); a mutable attribute also gets `setName(value)`, which returns
  the object (in interfaces the `$$Self` type parameter, otherwise the interface). Attribute names that clash with
  `Object` methods (`hashCode`, `wait`, …) or, in enums, `Enum` methods (`name`, `ordinal`, …) are reserved
  (`naming.reserved`, see "Reserved names" in the guideline) and not generated.
- **Abstract class or interface** (see "Abstractions: Abstract Classes or Interfaces" in the Java guide): an
  abstraction with attributes (own or inherited) or a `@@finalMethod` becomes an abstract class, the others become
  interfaces. An abstraction with attributes stays an interface if an enum or an interface extends it, or if a type
  extends it together with another class — then *none* of the involved abstractions becomes a class (Java has no
  multiple inheritance; the result does not depend on the order of `extends`). The rule is applied until nothing
  changes; supertypes of interfaces are interfaces.
- **Generator configuration** (`--config=<file>`, `sdk-java/generator.properties` for this repository):
  `java.interfaces` lists abstractions that become interfaces although they have attributes — e.g. to keep one of two
  abstractions of a type a class. Unknown keys, malformed names, unknown types, non-abstractions and abstractions with
  a `@@finalMethod` are errors.
- **Interfaces** (`InterfaceGenerator`): abstract accessors and setters for the declared attributes, abstract
  methods, `@@static` methods as stubs, `extends` for supertypes (which must be abstractions), `@@sealed(A, B)` →
  `sealed interface … permits A, B` (permitted types in the same module), `non-sealed` for sub-interfaces of a sealed
  interface.
- **Classes** (`ClassGenerator`), abstract and concrete: the class stores the attributes its superclass does not
  store (`private final` for `@@immutable`), has an explicit constructor (`protected` for abstract classes) that
  takes the attributes without other initial value, passes the superclass attributes to `super(...)`, checks
  (`requireNonNull`, validation annotations) and copies collections/arrays; a second constructor without immutable
  `@@default` attributes; accessors `name()`; setters `setName(value)` with the same checks that return the object
  (`$$Self` with `@SuppressWarnings("unchecked")` cast, otherwise covariant overrides in subclasses so chains keep the
  concrete type); abstract/`final` methods in abstract classes, stubs for everything a concrete class must implement;
  `toString` (`Name[a=..., ...]`, arrays by length) and, for value classes (all attributes immutable), `equals` and
  `hashCode`. `final` for `@@finalType` and for leaves of sealed hierarchies, otherwise `non-sealed` below a sealed
  type.
- **Implementations keep the Java types of what they implement**: an attribute or method declared with `$$T`, or an
  attribute that is `@@nullable` in a supertype, uses the wrapper class (`Long`) in all implementations, also where a
  primitive would be possible (`LongHolder extends Holder<int64>`, nullability narrowed with `@@override`).
- **Type variables** drop `$$`; if the name is also a spec or `java.lang` type, `T` is appended
  (`$$Receipt extends Receipt` → `ReceiptT extends Receipt`).
- **Records** (`RecordGenerator`): a complex type becomes a `record` if it is no abstraction, has at least one
  attribute, all its attributes (inherited ones included) are `@@immutable`, it extends no class (neither a concrete
  type nor an abstraction that becomes an abstract class) and no type extends it — records are final and cannot
  extend classes; the other complex types become classes. Types without attributes never become records. Generated:
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
- **Deferred types**: a type is only generated once every type it refers to (attributes, methods, bounds, the
  superclass, for interfaces all supertypes, permitted subtypes) is generated as well and all its types have a Java
  mapping — otherwise it is **deferred** (`generate --show-deferred`) instead of breaking the compilation. The plan is
  a greatest fixed point, so types that refer to each other are generated together. Records, enums and classes
  implement only generated interfaces; other interfaces are written as a comment. An `@@async` method that overrides
  an inherited one with another return type is deferred too: the meta-language allows the covariant return type, but
  `CompletionStage<T>` is invariant in Java. Today 336 files are generated (enums, records, interfaces, abstract and concrete classes,
  constants and factory classes, exceptions); 2 declarations are deferred: `TopicService` and its factory method
  `createService` (`@@streaming` is not mapped yet).
- **Constants** (`ConstantsGenerator`): the constants of a namespace become `public static final` fields of a
  `final` class named after the last namespace segment (`ledger` → `LedgerConstants`) with a private constructor.
  Struct literals become constructor calls (entries in constructor order, missing `@@nullable` → `null`, missing
  `@@default` → the default). A constant whose type is not generated or whose value has no Java form is deferred on its
  own (`--show-deferred` lists it with the qualified constant name); a clash of the class name with a type defers all
  constants of the namespace. Today: `LedgerConstants` (`ZERO_ADDRESS`, `ZERO_ACCOUNT_ID`, `ZERO_CONTRACT_ID`),
  `HederaConstants`, `SoloConstants`.
- **Factories** (`FactoryGenerator`): the namespace-level functions (always `@@static`) become `public static`
  methods of a `final` class named after the last namespace segment (`keys` → `KeysFactory`, `mirrornode.account` →
  `AccountFactory`) with a private constructor — analogous to the constants class. A function that refers to a type
  that is not generated or has no Java form is deferred on its own (reported as `namespace.name(parameter types)`); a
  clash of the class name with a type defers all functions of the namespace. Today 21 factory classes, e.g.
  `KeysFactory`, `HttpFactory`, `ClientFactory`, `MirrornodeFactory`.
- **Function types** (`JavaTypes`, `FunctionInterfaceGenerator`): mapped by shape (number of parameters; `void`,
  `bool` or another result) to `Runnable`, `Supplier`, `Consumer`, `Predicate`, `Function`, `BiConsumer`,
  `BiPredicate`, `BiFunction` — always the generic interfaces with wrapper types, `@@nullable` parameters as
  `@Nullable` type arguments. Three or more parameters or varargs get one generated `@FunctionalInterface` per function
  type (`onMessage` → `OnMessageFunction`), placed like the exception classes so that overriding methods in other
  packages use the same type. Function types with type variables in such an interface, unplaceable ones and name
  clashes defer the declarations that use them. The current specs use no function types.
- **Exceptions** (`ExceptionGenerator`, `JavaExceptions`): error identifiers of `@@throws` with a JDK equivalent use
  it (`not-found-error` → `NoSuchElementException`, `illegal-format` → `IllegalArgumentException`, `timeout-error` →
  `TimeoutException`, `io-error` → `IOException`, …); every other identifier gets a `final` unchecked exception class
  (`client-closed-error` → `ClientClosedException`) with exactly one constructor `(String message, @Nullable Throwable
  cause)`. The class is placed in the package of a namespace that uses the identifier and whose module all other
  using modules require, preferring the shortest namespace (`service-error` → `org.hiero.enterprise.service`); if no
  such module exists or the name clashes with a type, generation fails. Synchronous methods document errors with
  `@throws` and declare checked ones with `throws`; `@@async` methods describe with which exceptions the returned stage
  completes exceptionally. Today: `ClientClosedException`, `ConnectionException` (`org.hiero.http`),
  `MirrorNodeException` (`org.hiero.mirrornode`), `PaginationException` (`org.hiero.common`, thrown by `Page`),
  `ServiceException` (`org.hiero.enterprise.service`).
- **Type mapping** (`JavaTypes`): `intX`/`uintX` → `byte`/`short`/`int`/`long`/`BigInteger` by width; `uint8`/`uint16`/`uint32` use the next
  wider type (`short`/`int`/`long`) because Java integers are signed, `uint64` stays `long`, primitives
  unless nullable or a type argument, `bytes` → `byte[]`, collections → `List`/`Set`/`Map`, time types →
  `java.time`, `seconds`/`duration` → `Duration`, `type<T>` → `Class<? extends T>`, `ANY` → `Object`. Not mapped
  yet (the declaration is deferred): `streamResult`, `@@streaming`. Java keywords used as names
  get a trailing `_`. Function types: see above.

Not generated yet: `@@streaming` methods and `streamResult<T>`.

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

`mvn verify` runs about 575 tests; JaCoCo fails the build below 95 % line / 90 % branch coverage (generated ANTLR code
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
| `GeneratedOutputTest` | Writing into a version-controlled output directory: new files and directories, unchanged files are not rewritten, stale generated files and the directories they leave empty are deleted, hand-written and binary files and other empty directories are kept. |
| `FunctionTypeTest` | Mapping of every function shape to `java.util.function` with wrapper types and `@Nullable` arguments, generated functional interfaces (shared per function type, varargs), placement across modules with an implementation in another package, and every deferral reason (type variables, name clashes, no home). All cases are compiled. |
| `FactoryGeneratorTest` | Class name rule, static methods (overloads, generics with renamed type variables, varargs, `@@async @@nullable`, errors), compiled and called, and the deferral of functions (type not generated, no Java mapping, clashing class name). |
| `ConstantsGeneratorTest` | Class name rule, basic and struct-literal constants (record, class, `null` and `@@default` filling, `@Deprecated`), loading the compiled constants, and every deferral reason (type not generated, abstraction, missing value, clashing class name). |
| `ExceptionGeneratorTest` | Exception names, standard mapping (checked/unchecked), placement across modules (shortest namespace, transitive requires), errors without common module or with clashing names, `throws` clauses and async documentation, and the runtime behaviour of a generated exception (single constructor, message check, cause). |
| `ClassGeneratorTest` | Which abstraction becomes an abstract class or an interface (attributes, enums, interfaces, multiple inheritance with *none* as result, upward propagation, configuration, `@@finalMethod`), configuration errors, covariant `@@async` overrides, generated classes (state, constructors with `super(...)`, `$$Self` setters, covariant setter overrides, narrowed nullability, defaults, value classes, sealed hierarchies) and their **runtime behaviour** (checks, defensive copies, chained setters, equality). |
| `InterfaceGeneratorTest` | Generated interfaces (accessors, setters returning the self type, abstract and static methods, renamed type variables), records and enums implementing generic interfaces with wrapper types and `@Override`, nullability narrowing, `sealed`/`non-sealed`, supertypes as comment, and every deferral reason. Every case is compiled with `-Xlint:all -Werror`. |
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
