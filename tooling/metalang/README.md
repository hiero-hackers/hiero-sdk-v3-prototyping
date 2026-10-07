# metalang — grammar, parser and validator for the V3 API meta-language (prototype)

This multi-module Maven build turns the language-agnostic meta-language defined in
[`guidelines/api-guideline.md`](../../guidelines/api-guideline.md) into something machines can check
deterministically. It is the foundation for the planned follow-up tools (per-language code generation,
API conformance checks of existing SDKs, per-language exceptions, generated contract tests).

Status: **prototype**. The scope is grammar + parser + semantic model + validator, generators for Java,
TypeScript and Rust (API and tests) and conformance checks of projects against the specs.

## Quick start

Requires the JDK pinned in [`.sdkmanrc`](.sdkmanrc) next to this file; Maven comes from the wrapper `./mvnw` in
this directory. Activate the JDK with `sdk env` here (not needed with `sdkman_auto_env=true`) — on an older JDK the
build fails with `Unsupported major.minor version 69.0`. See the
[toolchain section of the main README](../../README.md#toolchain).

All commands are meant to be run from the **repository root** (`hiero-sdk-v3-prototyping/`) and can be copied
1:1 — except the Maven ones: `./mvnw` refers to the wrapper of the build it belongs to, so run it from
`tooling/metalang/` (the spec tooling) or from `sdk-java/` (the Java SDK). Each of the two carries its own
wrapper and its own `.sdkmanrc`.

### Build

```bash
./mvnw verify
```

This runs all tests and creates the self-contained CLI jar `tooling/metalang/target/metalang-0.1.0-SNAPSHOT-cli.jar`.
For a quick build without tests:

```bash
./mvnw -q package -DskipTests
```

### Modules

| Module | Content |
|---|---|
| `metalang-core` | Grammar, parser, AST, semantic and linked model, default instances, validator and rule catalog, and what every generator shares (`GeneratedFile`, `GeneratedOutput`, `Constraints`, `IntegerRange`, `RegexSamples`, `SpecFolders`, `check.ApiDifference`), and the TCK bindings (`tck`: parser, resolver, converter catalogue, reader of the TCK test specifications, coverage check). Its test-jar holds the shared test helpers (`TestSpecs`) and test resources (`rule-fixtures`, `model-golden`). |
| `metalang-java` | Java generator (`generator.java`: API, Maven project, JUnit tests) and Java conformance check (`check.java`), and the Java TCK server generator (`JavaTckGenerator`). The generated modules depend on the hand-written support module `sdk-java/support`, the hand-written TCK runtime `sdk-java/tck/runtime` implements the generated TCK contract. |
| `metalang-typescript` | TypeScript generator (`generator.ts`: npm workspace, API, `node:test` tests) and TypeScript conformance check (`check.ts`). The generated packages depend on the hand-written support package `sdk-ts/support`. |
| `metalang-rust` | Rust generator (`generator.rust`: Cargo workspace, API, integration tests) and Rust conformance check (`check.rust` with the `rs-api` program, a resource). The support files are copied from `guidelines/rust-files`. |
| `metalang-go` | Go generator (`generator.go`: Go module, API). It is the newest generator and still incomplete: it emits the module, one package per namespace and the declared types; namespace functions, methods and generated tests are still missing, and there is no Go conformance check yet. |
| `metalang-cli` | The command line tool (`MetaLangCli`) on top of all modules; builds the self-contained jar `tooling/metalang/target/metalang-0.1.0-SNAPSHOT-cli.jar`. |

A language generator depends only on `metalang-core`; a new language gets its own module next to them and is wired
into `metalang-cli`.

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

Generates the Java API into `sdk-java/generated`. The directory is under version control, so that
every change of the specs or the generator shows up as a diff of the generated code. Files are only rewritten if their
content changes, and generated files that are no longer produced (e.g. of a removed or deferred type) are deleted;
files without the generator's header line are never touched. `--output` takes any directory.

```bash
java -jar tooling/metalang/target/metalang-0.1.0-SNAPSHOT-cli.jar generate --language=java --fail-on=never --config=sdk-java/generator.properties --output=sdk-java/generated spec
```

Without `--fail-on=never` nothing is generated as long as the specs have validation errors. See
[Java generator](#java-generator) for what is generated. `--show-deferred` lists the types that are not
generated yet and why; `--show-untested` lists the [generated tests](#generated-tests) that cannot be generated
because their values cannot be built (the summary line is always printed).

`sdk-java/generated` is a Maven project: a parent `pom.xml` with one sub-module per Java module, each built into its own
JAR plus a Javadoc JAR. It needs JDK 25 (`sdk env` in `sdk-java`, see
[`sdk-java/.sdkmanrc`](../../sdk-java/.sdkmanrc)) and is built with
`-Xlint:all -Werror` for the code and doclint `all,-missing` with `failOnWarnings` for the Javadoc. The base module
depends on the hand-written support types (`sdk-java/support`, artifact `hiero-sdk-support`), so install them first:

```bash
./mvnw -f support install
./mvnw -f protobuf install
./mvnw -f generated/pom.xml package -DskipTests
```

The JARs are then in `sdk-java/generated/<module>/target/`; the build output is ignored by git.

Every module also contains the [generated tests](#generated-tests) (`src/test/java`, JUnit). They fail for every method
that is not implemented yet, so a build with tests shows the implementation status; `-Dmaven.test.failure.ignore=true`
runs all modules even if tests fail:

```bash
./mvnw -f generated/pom.xml test -Dmaven.test.failure.ignore=true
```

### Generate the TypeScript API

Generates the TypeScript API into `sdk-ts/generated` (an npm workspace, also under version control); see
[TypeScript generator](#typescript-generator):

```bash
java -jar tooling/metalang/target/metalang-0.1.0-SNAPSHOT-cli.jar generate --language=ts --fail-on=never --config=sdk-ts/generator.properties --output=sdk-ts/generated spec
```

Build and test it with Node.js 22 (`npm install` once; `node_modules`, `dist` and `package-lock.json` are ignored by
git). `npm install` in `sdk-ts` links the hand-written support package `sdk-ts/support` into the workspace, and `tsc --build`
builds it first. As for Java, the tests of methods that are not implemented yet fail:

```bash
npm --prefix sdk-ts install && npm --prefix sdk-ts test --workspaces
```

### Generate the Go API

One Go module for everything, one package per namespace, see [Go generator](#go-generator):

```bash
java -jar tooling/metalang/target/metalang-0.1.0-SNAPSHOT-cli.jar generate --language=go --fail-on=never --config=sdk-go/generator.properties --output=sdk-go/generated spec
```

Check it with the Go toolchain (Go 1.27 or newer). The generator emits no method bodies yet, so there is nothing
to run; what has to hold is that the module builds, vets clean and is already formatted:

```bash
cd sdk-go/generated && go build ./... && go vet ./... && gofmt -l .
```

`gofmt -l` has to print nothing: the generator produces the alignment `gofmt` would, because nothing in the build
may depend on the Go toolchain being installed.

Declarations the generator cannot express in Go yet are left out instead of emitted as code that does not
compile, and `--show-deferred` lists them with the reason. On the current specs nothing is deferred.

### Generate the Rust API

One crate per spec folder in a Cargo workspace, see [Rust generator](#rust-generator):

```bash
java -jar tooling/metalang/target/metalang-0.1.0-SNAPSHOT-cli.jar generate --language=rust --fail-on=never --config=sdk-rust/generator.properties --output=sdk-rust/generated spec
```

Build and test it with Cargo (Rust 1.85 or newer; `target` and `Cargo.lock` are ignored by git). As for Java and
TypeScript, the tests of methods that are not implemented yet fail:

```bash
cargo test --manifest-path sdk-rust/generated/Cargo.toml --no-fail-fast
```

### Check a project against the specs

Checks whether a Java project provides the API that the generator derives from the specs — for the generated code
itself (is `sdk-java/generated` up to date?) or for an implementation that started from it:

```bash
java -jar tooling/metalang/target/metalang-0.1.0-SNAPSHOT-cli.jar check --language=java --fail-on=never --config=sdk-java/generator.properties --project=sdk-java/generated spec
```

The command generates the expected API in memory and compares it **structurally** with the `.java` files below
`--project` (build output in `target` directories excluded). The sources are only parsed with the JDK compiler tree
API, not compiled, so the project's dependencies are not needed. It prints every difference with file and line and
exits with 1 if there is one. See [Conformance check](#conformance-check) for what is compared.

For TypeScript (`--language=ts`) the sources below `<project>/packages/*/src` are read with the TypeScript compiler
API; it is taken from `<project>/node_modules/typescript` or from `--typescript=<dir>`, and `node` must be on the path:

```bash
java -jar tooling/metalang/target/metalang-0.1.0-SNAPSHOT-cli.jar check --language=ts --fail-on=never --config=sdk-ts/generator.properties --project=sdk-ts/generated spec
```

For Rust (`--language=rust`) the crates below `--project` (every `Cargo.toml` with a package and a `src/lib.rs`) are
read with `rs-api`, a small Rust program based on `syn` that the tool builds once with Cargo (`cargo` on the path or
`--cargo=<executable>`; the first build downloads `syn` from crates.io):

```bash
java -jar tooling/metalang/target/metalang-0.1.0-SNAPSHOT-cli.jar check --language=rust --fail-on=never --config=sdk-rust/generator.properties --project=sdk-rust/generated spec
```

### Generate the TCK server and check its coverage

The bindings in `tooling/tck/bindings` map the methods of the [Hiero TCK](https://github.com/hiero-ledger/hiero-sdk-tck) to the
API (see [`tck-binding.md`](../../docs/tck-binding.md)). `tck generate --language=java` generates two Maven projects:

- `<output>/contract` (`hiero-sdk-tck-contract`): the contract with the runtime — interfaces and records only. The
  `Converters` interface is derived from the converter catalogue; `TckRuntime` declares JSON access, execution and the
  JSON-RPC server.
- `<output>/server` (`hiero-sdk-tck`): the server generated from the bindings. It is compiled against the generated
  API and the contract only; the hand-written runtime `sdk-java/tck/runtime` (`hiero-sdk-tck-runtime`) implements the
  contract, is found with the `ServiceLoader` and is only a runtime dependency.

`tck generate --language=ts` generates the same two parts as npm packages, `<output>/contract`
(`@hiero/tck-contract`) and `<output>/server` (`@hiero/tck-server`). The server is compiled against the generated API
and the contract only and loads the hand-written runtime `sdk-ts/tck/runtime` (`@hiero/tck-runtime`) with a dynamic
`import`. The packages reference the projects of the generated API workspace, by default the directory `ts` next to
the output (`--api=<dir>` selects another one).

`tck check` compares the bindings with the test specifications of a TCK clone:

```bash
java -jar tooling/metalang/target/metalang-0.1.0-SNAPSHOT-cli.jar tck generate --language=java --fail-on=never --config=sdk-java/generator.properties --bindings=tck/bindings --output=sdk-java/tck/generated spec
java -jar tooling/metalang/target/metalang-0.1.0-SNAPSHOT-cli.jar tck generate --language=ts --fail-on=never --config=sdk-ts/generator.properties --bindings=tck/bindings --output=sdk-ts/tck/generated spec
java -jar tooling/metalang/target/metalang-0.1.0-SNAPSHOT-cli.jar tck check --fail-on=never --bindings=tck/bindings --tck=../hiero-sdk-tck/docs/test-specifications spec
```

Both stop at errors of the bindings (`tck.*` diagnostics). `tck check` prints every parameter or result of a bound
method that has neither a binding nor an `unsupported` declaration, and the unbound methods; it exits with 1 if there
is a finding. To build and start the server (JDK 25, `sdk env`):

```bash
./mvnw -f support install
./mvnw -f protobuf install
./mvnw -f generated install -DskipTests
./mvnw -f tck/generated/contract install
./mvnw -f tck/runtime install
./mvnw -f tck/generated/server clean package
java -jar sdk-java/tck/generated/server/target/hiero-sdk-tck-0.1.0-SNAPSHOT.jar
```

`clean` matters for the server: an incremental `package` keeps the `Class-Path` of the previous
manifest, so a newly added dependency lands in `target/lib` but not on the class path.

The server listens on port 8544 (the TCK default) or on the port given as argument; its dependencies are copied to
`target/lib` next to the jar.

The TypeScript server is built in the npm workspace `sdk-ts` (`sdk-ts/package.json`), which contains all TypeScript
modules — the generated API, the support package, the TCK contract and server, and the runtime — because a package
outside a workspace root cannot resolve its dependencies (Node.js 22):

```bash
cd sdk-ts
npm install
npm run build:tck-ts
node tck/generated/server/dist/main.js
```

`tooling/tck/run-tck.sh java|ts` starts a server and runs the TCK against it on a local Solo network (see
[`tck-binding.md`](../../docs/tck-binding.md), "Running the TCK").

### List all rules

```bash
java -jar tooling/metalang/target/metalang-0.1.0-SNAPSHOT-cli.jar rules
```

## Pipeline

```
spec/*.md
  │ source/      MarkdownSchemaExtractor  "## API Schema" and "## Default Instances" code blocks + skeleton checks
  │                                        (doc.*)
  │ parser/      ANTLR4 grammar → parse tree → immutable AST (ast/, records + sealed interfaces)
  │ semantic/    SpecModel: namespaces, type index, name resolution (local / requires / wildcard / qualified)
  │ model/       LinkedModel: every reference resolved, effective members with substituted type arguments,
  │                           default instances resolved (InstanceResolver)
  │ validation/  Validator: one Check per guideline topic, every finding refers to a stable Rule id
  ▼
ValidationReport (sorted diagnostics) → CLI (text / JSON / summary)
```

- **Grammar:** [`MetaLang.g4`](metalang-core/src/main/antlr4/org/hiero/sdk/v3/metalang/grammar/MetaLang.g4) is the
  machine-readable definition of the language. It is not newline-sensitive; comments go to a hidden channel and
  the comment lines directly above a declaration (plus a trailing comment on its line) become its documentation.
- **AST:** the ANTLR parse tree is only used inside `AstBuilder`. Everything after that works on immutable records
  with source locations that point into the original Markdown file.
- **Rules:** [`Rule`](metalang-core/src/main/java/org/hiero/sdk/v3/metalang/diagnostic/Rule.java) is the catalog of all checks
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
- **Maven project** (`MavenGenerator`): a parent `pom.xml` (packaging `pom`, artifactId `hiero-sdk`) with one
  sub-module per Java module (artifactId `hiero-<spec folder>`, e.g. `hiero-consensus-node-client`) that depends on the
  modules its `module-info.java` requires, on jspecify and (test scope) on JUnit Jupiter. The parent sets Java 25 and
  UTF-8, manages the jspecify version and the JUnit BOM, pins every plugin (clean, resources, compiler, surefire, jar, javadoc, install), compiles with
  `-Xlint:all -Werror` and attaches a Javadoc JAR to every module (doclint `all,-missing`, warnings fail the build).
  groupId and version come from the configuration (`java.groupId`, default `org.hiero.sdk`; `java.version`, default
  `0.1.0-SNAPSHOT`).
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
  `java.groupId` and `java.version` set the Maven coordinates; `java.interfaces` lists abstractions that become
  interfaces although they have attributes — e.g. to keep one of two
  abstractions of a type a class. Unknown keys, malformed names, unknown types, non-abstractions and abstractions with
  a `@@finalMethod` are errors.
- **Protobuf** (`ProtobufFiles`): `java.protobuf` lists the spec folders whose module needs the protobuf messages.
  Such a module gets a dependency on `hiero-sdk-protobuf` (the hand-written module `sdk-java/protobuf`, which
  compiles the vendored definitions in `/tooling/protobuf`) and a plain `requires org.hiero.sdk.protobuf` — deliberately
  **not** `requires transitive`: the wire format is an implementation detail, and the protobuf module exports its
  packages only to the modules named here. See `tooling/protobuf/README.md`.
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
  `CompletionStage<T>` is invariant in Java. Today nothing of the specs is deferred.
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
- **Support types** (`SupportFiles`): classes the generated API uses but the specs do not declare — `@ThreadSafe`
  (`org.hiero.sdk.annotation`) and the streaming types `HieroStream`, `StreamItem`, `HieroPublisher`,
  `HieroSubscription` (`org.hiero.sdk.common`). They are not generated: they are the hand-written Maven module
  `sdk-java/support` (artifact `hiero-sdk-support`, JPMS module `org.hiero.sdk.support`). When a generated declaration
  needs them, the base module that all other modules require declares `requires transitive org.hiero.sdk.support` and
  the Maven dependency. Install the module before building the generated code (`./mvnw -f support install`).
- **Streaming**: `@@streaming T m()` returns `HieroStream<T>`, `streamResult<T>` is `StreamItem<T>`; the errors of a
  streaming method are documented as "The stream ends with `X` if it fails." (they are thrown by the iterator).
- **Thread safety** (`ThreadSafeGenerator`): `@@threadSafe[(group)]` becomes the SDK annotation
  `@ThreadSafe(group = "...")` (`RetentionPolicy.CLASS`: kept in the JARs for readers, IDEs and static analysis, not
  evaluated at runtime). The annotation is generated once, in the package `org.hiero.sdk.annotation` of the module
  that all modules using `@@threadSafe` require, and exported. It is put on types, methods, accessors and setters;
  members of a type that is annotated as a whole are not annotated again. Implementations carry it too: a class,
  record or enum that implements a thread-safe type is annotated as a whole, and implementations of thread-safe
  methods and setters are annotated. Generated state is really thread-safe: immutable attributes are `final`, mutable
  attributes of a thread-safe class or with `@@threadSafe` are `volatile` (accessor and setter read or replace the
  whole value, collections and arrays as copies). The behaviour of thread-safe methods is implemented by hand.
- **Deprecation**: `@@deprecated` elements get `@Deprecated` and an `@deprecated` Javadoc tag. Its text is the
  paragraph of the documentation that mentions the deprecation (moved out of the description; repeated at the setter
  of a deprecated attribute), otherwise "Retained for compatibility; do not use it in new code."
  The validator warns about `@@deprecated` elements without such a paragraph (`doc.deprecated-without-reason`).
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
  `java.time`, `seconds`/`duration` → `Duration`, `type<T>` → `Class<? extends T>`, `ANY` → `Object`,
  `streamResult<T>` → `StreamItem<T>`. Java keywords used as names get a trailing `_`. Function types: see above.
- **Integer ranges** (`JavaIntegers`, `JavaConstraints`): where the Java type is wider than the meta-language type
  (`uint8`/`uint16`/`uint32`, odd widths like `int24`, `int128`/`int256` as `BigInteger`), constructors and setters check
  the range of the type (`port must be between 0 and 65535`). `uint64` has no range check (every `long` is a valid
  unsigned value); its `@@min`/`@@max` are compared with `Long.compareUnsigned`.
- **Tests**: see [Generated tests](#generated-tests).

Everything the specs declare is generated; what can still be deferred are declarations with unresolved types, function
types that need an interface with type variables, and clashing names.

## TypeScript generator

`generator/ts` maps the model to TypeScript as described in
[`guidelines/api-best-practices-ts.md`](../../guidelines/api-best-practices-ts.md):

- **npm workspace** (`TsProjectGenerator`): one package per spec folder (`packages/<folder>`, name
  `<ts.scope>/<folder>`), one subpath export per namespace (`@hiero/base/ledger`), dependencies and project references
  to the packages of the used namespaces, a strict `tsconfig.base.json`, pinned `typescript` (6.0.3, the last version
  with the JavaScript compiler API) and `@types/node`. Configuration: `sdk-ts/generator.properties` (`ts.scope`,
  `ts.version`).
- **Types** (`TsTypeGenerator`): abstractions become interfaces (static methods: functions of a namespace with the
  name of the interface; sealed abstractions: the union of the permitted classes), complex types classes with
  `#private` fields, getters/setters and a constructor that takes one object with all attributes, enums classes with a
  `static readonly` instance per value. Constructors and setters check `null` (`TypeError`), integer ranges (also that a
  `number` is an integer) and validation annotations (`RangeError`) and copy `bytes`, collections and dates.
- **Namespaces** (`TsNamespaceGenerator`): `functions.ts` (overloads as TypeScript overload signatures),
  `constants.ts`, `errors.ts` (one `Error` subclass per error id, placed like the Java exceptions), `index.ts`.
- **Imports** (`TsImports`): relative within a package, the namespace subpath between packages, `import type` for
  names used only as types, aliases for clashing names, only names that the file uses.
- **Support types**: `Duration`, `StreamItem`, `AbstractConstructor` are not generated: they are the hand-written
  package `sdk-ts/support` (`<scope>/support`). Every generated package whose code imports from it declares the
  dependency and a project reference; the workspace lists it as workspace and builds it first. Its location relative
  to the workspace is `ts.support` (default `../../sdk-ts/support`).
- **Protobuf**: `ts.protobuf` lists the spec folders whose package needs the protobuf messages. The package gets a
  dependency on `@bufbuild/tooling/protobuf`, and the workspace `.gitignore` ignores `packages/*/src/internal/proto/` — the
  messages are build output, generated by `npm run gen:proto-ts`. That subpath is deliberately **absent** from the
  package's `exports` map, which is what makes it unreachable from outside the package. See `tooling/protobuf/README.md`.
  `type<T>` is `AbstractConstructor<T>`, any class object whose prototype is a `T`
  (also abstract classes and enum classes with their private constructor); a primitive is represented by its
  wrapper class (`type<string>` → `AbstractConstructor<String>`).
- **Tests** (`TsTestGenerator`, `TsSamples`): the same contract as the Java tests for the Node.js test runner, with a
  test name that says what is checked; values from the default instances first. Number types also get a fraction test.
  Tests whose values cannot be built become `test.todo` entries.

## Rust generator

`generator/rust` maps the model to Rust as described in
[`guidelines/api-best-practices-rust.md`](../../guidelines/api-best-practices-rust.md):

- **Cargo workspace** (`RustProjectGenerator`): one crate per spec folder (`crates/<folder>`, package
  `<rust.cratePrefix>-<folder>`), path dependencies on the crates of the required folders, and only the libraries the
  crate's code uses (`chrono`, `rust_decimal`, `uuid`, `ethnum`, `regex`, `futures-core`, versions in the workspace
  manifest). Configuration: `sdk-rust/generator.properties` (`rust.cratePrefix`, `rust.version`).
- **Protobuf**: `rust.protobuf` lists the spec folders whose crate needs the protobuf messages, `rust.protobufRoot`
  the vendored definitions relative to a crate directory (default `../../../../protobuf/consensus-node`). The crate
  gets `prost`, a `build.rs` that compiles the definitions into `OUT_DIR` with `prost-build` and a `protoc` from
  `protoc-bin-vendored` (so no protobuf installation is needed), and a `src/proto.rs` that includes the result.
  `lib.rs` declares it as `mod proto;` **without** `pub`: crate-private, so the wire format is no part of the API.
  See `tooling/protobuf/README.md`.
- **Planning** (`RustContext`): the Rust form of every type (`RustType`), the type parameters that are erased because
  the specs use them with `ANY` (Rust has no wildcards; the parameter becomes its bound as `Arc<dyn Trait>`), the
  `$$Self` parameters (Rust's `Self`), the names of overloads (`_with_<parameters>`), the error types and error enums,
  and what each type can derive (`Copy`, `PartialEq`, `Eq`, `Hash`, `Clone`).
- **Types** (`RustTypeGenerator`): structs with private fields, a checking `new`, getters, setters and inherent
  methods, and an implementation of every trait they inherit (delegating where the signatures match, converting
  narrowed and erased attributes); traits (statics in `impl dyn Trait`, final methods in a `<Trait>Ext` extension
  trait); enums for sealed abstractions; enums with `const fn` attributes, `Display` and `FromStr`.
- **Namespaces** (`RustNamespaceGenerator`): `mod.rs` with the private type modules and their re-exports, modules
  for namespace prefixes of the crate (`consensusnode`), `functions.rs`, `constants.rs` (`const` or `LazyLock`),
  `errors.rs` (error structs and error enums).
- **Imports** (`RustImports`): `use` declarations for the items a file uses, aliases for clashing names (also with
  the prelude), traits imported as `_` for their methods, full paths for library types.
- **Support files**: `BoxFuture`/`BoxStream`, `StreamItem`, `InvalidArgumentError` and `is_absolute_url` from
  `guidelines/rust-files`, copied 1:1 into `<crate>::support` of the crate all crates require.
- **Tests** (`RustTestGenerator`, `RustSamples`): one integration test per crate (`tests/api`, one module per type and
  namespace) with the contract that the Rust type system does not already guarantee (no null, copy or exact integer
  range tests): constructor values, validation boundaries, setters, every method and function (async ones run to
  completion with a small `block_on`), enum constants, value equality and `Send + Sync`. Values come from the default
  instances first; a trait without implementation is represented by a test double, a struct of the test file that
  implements the trait and its supertraits. Tests whose values cannot be built are ignored tests.

The generated code compiles without warnings (also with `cargo clippy`); the tests of `metalang-rust` build every
generated workspace with `RUSTFLAGS=-D warnings` and a shared target directory (`metalang-rust/target/cargo`).

The language-neutral parts — value constraints (`Constraints`), integer ranges (`IntegerRange`), pattern samples
(`RegexSamples`), spec folders (`SpecFolders`) — live in the `generator` package of `metalang-core` and are shared by all languages.

## Go generator

`generate --language=go` (`generator.go.GoGenerator`) writes **one Go module** for all specs. Unlike the Java,
TypeScript and Rust generators, a spec folder is not an artifact boundary: Go has no per-folder unit, so the
module holds one package per namespace, in a directory per namespace segment
(`consensusnode.transactions.accounts` → `consensusnode/transactions/accounts`).

| Spec | Go |
|---|---|
| namespace | package, with its `## Description` as the package doc in `doc.go` |
| complex type | struct with unexported fields, `New<Type>` constructor and value-receiver getters |
| abstraction | interface with a getter per attribute; supertypes are embedded |
| `@@sealed` abstraction | interface with an unexported marker method the subtypes implement |
| enum without attributes | defined `int` type, typed constants, `String`, `<T>Values`, `<T>ValueOf` |
| enum with attributes | struct with package-level `var` values, because Go has no constant struct |

Configured by `sdk-go/generator.properties` (`go.module`, `go.version`). The mapping follows
[`guidelines/api-best-practices-go.md`](../../guidelines/api-best-practices-go.md); what the Go type system
cannot express is written down in its "Generics, self types and `ANY`" section.

Two properties are checked by the generated module itself, and nothing in the build depends on a Go toolchain
being installed:

- `go build ./...` and `go vet ./...` are clean. A declaration the generator cannot express is **left out** and
  reported by `--show-deferred`, rather than emitted as code that does not compile.
- `gofmt -l` prints nothing: `GoFormat` produces the column alignment `gofmt` would, at the places where the
  generator knows it is emitting a declaration list.

Still missing: namespace functions, methods on types, setters, errors, `@@async`/`@@streaming` signatures,
generated tests, the protobuf wiring (`internal/proto`), the Go conformance check (`check --language=go` is
rejected) and the Go TCK part.

## Generated tests

`TestGenerator` writes a JUnit test class into `src/test/java` of the module, in the package of the tested type: one
per record, class and enum (`<Type>Test`), one per abstraction with static methods and one per factory class
(`<Namespace>FactoryTest`). The tests check the contract of the specs, both what the generator implements and what
humans implement later:

| Area | What the tests check |
|---|---|
| Constructors | Valid values create an object whose accessors return them; the constructor without the `@@default` attributes sets the defaults; `null` for a non-nullable value throws `NullPointerException`. |
| Validation | For every validation annotation the boundary values are accepted and the values just outside rejected with `IllegalArgumentException`: `@@min`/`@@max` (with `Math.nextDown`/`nextUp` for `double`, `minusNanos(1)` for durations), the range of the integer type (`int8`…`int256`, `uint8`…`uint256`; boundaries only where the Java type can hold them), `@@minLength`/`@@maxLength`, `@@minSize`/`@@maxSize`, `@@pattern` (a string the pattern rejects), `@@urlPattern`. |
| Copies | A collection or `bytes` value passed to a constructor or setter is copied (changing it afterwards does not change the object); returned collections are unmodifiable, returned arrays are copies. |
| Setters | Return the object, change the value, accept (`@@nullable`) or reject `null`, check the validation annotations and keep the old value when they reject one; the initial value of attributes the constructor does not take. |
| Value semantics | Records and immutable classes whose attributes compare by value: equal values give equal objects and hash codes. |
| Methods | Every method can be called with valid arguments, returns a value (not `null` unless `@@nullable`), and throws at most the errors of its `@@throws`. |
| Factory methods | Return objects for valid arguments, reject `null` for non-nullable parameters, check the validation annotations and integer ranges of the parameters. |

Method bodies are stubs, so the method and factory tests fail until the methods are implemented ("Not implemented
yet: …"); everything else passes against the generated code.

The values come from `JavaSamples`, deterministically. A type with a [default instance](#default-instances) always uses
it — the specs define the values tests must use (e.g. a real key). Otherwise: literals within the range and constraints
of the type, strings
for a `@@pattern` from `RegexSamples` (verified with `java.util.regex`), the first non-deprecated enum constant, records
and classes created with their constructor (`null` for nullable attributes). An abstraction is represented by a
generated concrete subtype from a module the test's module requires (non-generic first, then enums, records, classes),
otherwise by one of its static methods or a namespace-level function that return it (e.g.
`TransactionId.generateTransactionId(…)`, `ClientFactory.createClient(networkSettings, operatorAccount)`; without
`@@throws` and with fewer parameters first). A record or class whose constructor needs a value that cannot be built
(`HieroClient` needs a `TransactionSigner`) is created with such a factory function as well. Tests that use a value
of a factory fail until the factory is implemented. For values the object only stores (constructor and setter arguments) a test double is the last
resort: an anonymous subclass whose methods throw. Method arguments never use test doubles, because the
implementation may call them. A test whose values cannot be built is not generated and listed in the comment of the
test class and by `generate` (today: two values with a generic class literal or a list of generic abstractions).
Every test and test class has a `@DisplayName` that describes it (`port: rejects a value above the maximum (65536)`).

## Default instances

The optional `## Default Instances` section of a spec (see "Default instances" in `guidelines/api-guideline.md`)
records the standard way to obtain an instance of a type through the API:
`instance TransactionSigner = DEFAULT(HieroClient<ANY>).transactionSigner`. The section has its own start rule in the
grammar (`instances`) and uses the namespace and imports of the file's API schema. `InstanceResolver` resolves it into
`LinkedModel.instances()`: types, functions, static methods, methods, attributes, constants and enum constants, the
overload of a call (by the names and types of its named arguments), and the type of every value. Rules:
`instance.invalid` (cannot be resolved, wrong type, duplicate, type of another namespace), `instance.cycle` (default
instances that depend on each other through `DEFAULT`), and the warning `instance.missing` for every type used by an
attribute or parameter that cannot be obtained at all — no default instance, no concrete subtype, no factory function
or static method (a fixed point over the model). The specs define default instances for keys (a real ED25519 key),
accounts, addresses, the testnet configuration, the client and its signer; the remaining warnings are
`ExchangeRate`, `MirrorNodeHttpClient` and the protocol types that are not specified yet.

## Conformance check

`metalang check` (`check.java.JavaConformance`) answers: does a project still provide the API of the specs? The project
is matched by declaration, not by text or file: types by qualified name, members by name and erased parameter types.
Names are resolved through the package, the imports (single and on demand), `java.lang` and enclosing types, so a
changed import style or a moved file makes no difference.

| Must match exactly (for every expected declaration) | Allowed (additive or implementation detail) |
|---|---|
| Kind of type (class, interface, enum, record, annotation) | Additional files, types, members, constructors |
| Modifiers (`public`, `protected`, `static`, `final`, `sealed`, `non-sealed`; `abstract` of types) | Method bodies; abstract, `default` or implemented methods |
| Type parameters with bounds, superclass, record components | Additional implemented interfaces and permitted subtypes |
| Member declaration: modifiers, type parameters, result and parameter types with nullness (`@Nullable`), `throws`, value of a constant | Additional enum constants, annotations, `requires` and `exports` |
| Implemented interfaces, permitted subtypes, enum constants, annotations (`@Deprecated`, `@ThreadSafe`, `@FunctionalInterface`, `@NullMarked`) | Private members, package-private fields, imports, formatting, comments and documentation |
| Module: `requires` with `transitive`/`static`, `exports`, annotations | `@Override`, `@SuppressWarnings`, `@Serial`, `@SafeVarargs`; implementation modifiers (`synchronized`, `volatile`, …) |

The canonical and compact constructors of records and the constructors of enums are implied and not compared. Only
`.java` files of `src/main/java` are expected (the [generated tests](#generated-tests) are no part of the API); the Maven `pom.xml` files are build configuration that an implementation changes anyway.
A test (`JavaConformanceTest`) runs the check for `sdk-java/generated`, so a spec or generator change without
regeneration fails the build.

### TypeScript

`metalang check --language=ts` (`check.ts.TsConformance`) runs `ts-api.mjs` with the TypeScript compiler API on the
generated and on the project's sources (parsing only) and compares the declarations: a declaration belongs to the
namespace of its directory (`packages/<folder>/src/<namespace>`), type names are resolved through the imports to
`<folder>:<namespace>#<Name>`, a getter counts as `readonly` property, `ReadonlyArray<T>` as `readonly T[]`, and unions
are compared without order. Every expected declaration must exist with its kind (class, interface, namespace, type,
function, const), type parameters and supertypes; every expected member (property with `readonly`, optional and type,
method and constructor signatures, functions of a namespace) must exist with its signature. Additional files,
declarations, members, overloads and supertypes, `#private` members and method bodies are allowed. A test runs the
check for `sdk-ts/generated` when TypeScript is installed there.

### Rust

`metalang check --language=rust` (`check.rust.RustConformance`) runs `rs-api` (Rust, `syn`; the source is a resource
of `metalang-rust`, built once per version into the temporary directory) on the generated and on the project's crates
(parsing only). Every item is identified by its shortest public path (`hiero_base::ledger::AccountId`, through
`pub use` re-exports and globs), and every type in a signature is resolved through the imports to that path or to the
full path of an external item, so file layout and import style make no difference. Every expected item must exist
with its kind (struct, enum, trait, fn, const, static, type) and type parameters (or signature/type); every expected
implemented trait (also derived ones, `Clone`, `PartialEq`, ...) and supertrait must exist; every expected member
(inherent method, associated function of `impl dyn Trait`, trait method, enum variant, public field) must exist with
its signature. Additional crates, items, members, trait implementations and method bodies are allowed. A test runs the
check for `sdk-rust/generated` when Cargo is installed.

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

Writing the Go generator corrected four decisions in `guidelines/api-best-practices-go.md`, each forced by the Go
compiler rather than by taste:

- **No inherited state.** The guideline had an abstraction's attributes live in an unexported base struct that
  subtypes embed. An unexported type cannot be embedded from another package, and the specs inherit across
  namespaces, so a concrete struct now carries all its effective attributes itself.
- **A self type is dropped.** `$$Self extends Transaction<$$Self, ...>` has no Go form, and substituting its bound
  for a wildcard would expand forever. Dropping it is also what makes a field of type `Transaction<ANY, ANY>`
  nameable at all — without it, 67 of the declarations could not be generated.
- **A concrete type parameter bound is widened to `any`.** Go has no subtyping for structs, so a type set of one
  struct admits that struct only; the lost constraint is named in the generated doc comment.
- **Maps are copied like slices.** The guideline copied `bytes` and slices in and out; a map is a reference type
  too, and without the copy an `@@immutable` map attribute is immutable in name only.

## Current findings on `spec/`

`validate --summary --min-severity=warning spec` reports 6 errors (no syntax errors), all `collection.nullable` on
update transactions and `@@oneOf` payloads, where `null` currently means "not set" — this conflicts with
the guideline rule "never define nullable collections" and needs a design decision (tracked in `docs/TODO.md` → Meta-language).

## Tests

`./mvnw verify` runs about 750 tests (the TypeScript tests that need Node.js and an
installed TypeScript, and the Rust tests that need Cargo are skipped without them); JaCoCo fails the build of **each
module** below 95 % line / 90 % branch coverage (generated ANTLR code excluded). The tests live in the module of the code they test: parser, model and validator tests in
`metalang-core`, generator and check tests in `metalang-java` / `metalang-typescript` / `metalang-rust`, the command tests
(`*CommandTest`, `MetaLangCliTest`) in `metalang-cli`. The cross-JVM determinism tests start the generator of their
module (`GenerateMain` in the test sources). Besides unit tests per component, the suite contains these systematic
checks:

| Test | What it guarantees |
|---|---|
| `RuleFixturesTest` | Every rule of the `Rule` catalog has a fixture directory `metalang-core/src/test/resources/rule-fixtures/<rule-id>/` that produces **exactly** the expected set of rule ids (`*.ml` = schema wrapped into the spec skeleton, `*.md` = raw Markdown, optional `also.txt` = further expected ids). A new rule without fixture fails the build. |
| `GuidelineExamplesTest` | Every meta-language code block of `guidelines/api-guideline.md` parses (as-is, in a namespace, in a type body, or as type expressions). Non-meta-language blocks are listed with a reason; stale entries fail the test. |
| `GrammarEdgeCasesTest` | ~60 boundary cases of the grammar: keywords as names, nested generics, comments everywhere, literal formats, CRLF, and constructs that must be rejected — always with a diagnostic, never an exception. |
| `MarkdownEdgeCasesTest` | CommonMark fences (longer fences, indentation, info strings), ATX headings, CRLF, Unicode. |
| `RobustnessTest` | Seeded random mutations and every prefix of every real spec never crash the tool and never report a location outside the document; results do not depend on document order; AST locations point at the element; the textual form of every type and literal parses back to itself. |
| `RepositorySpecsTest` | All specs under `spec/` are free of syntax errors and the report is deterministic. |
| `ModelCommandTest` | `metalang model` output for the example specs in `metalang-core/src/test/resources/model-golden/spec` equals the golden file `model-golden/model.json` byte for byte; filters, exit codes and determinism on the real specs. After an intended change, regenerate the golden file (command in the test's Javadoc). |
| `GeneratedOutputTest` | Writing into a version-controlled output directory: new files and directories, unchanged files are not rewritten, stale generated files and the directories they leave empty are deleted, hand-written and binary files and other empty directories are kept. |
| `FunctionTypeTest` | Mapping of every function shape to `java.util.function` with wrapper types and `@Nullable` arguments, generated functional interfaces (shared per function type, varargs), placement across modules with an implementation in another package, and every deferral reason (type variables, name clashes, no home). All cases are compiled. |
| `FactoryGeneratorTest` | Class name rule, static methods (overloads, generics with renamed type variables, varargs, `@@async @@nullable`, errors), compiled and called, and the deferral of functions (type not generated, no Java mapping, clashing class name). |
| `SupportFilesTest` | The support module provides and exports the packages the generator imports from, streaming methods and `streamResult` map to `HieroStream`/`StreamItem`, the support module is only required when used, and at runtime the push adapter delivers only the requested items, waits without polling, caps an overflowing demand, cancels and rejects a non-positive demand. |
| `ThreadSafeTest` | The `@ThreadSafe` annotation (generated once in the module all users require, exported, `RetentionPolicy.CLASS`, not visible via reflection), annotations on members, types and implementations, `volatile` for mutable thread-safe state (checked via reflection), and the error without common module. |
| `MavenGeneratorTest` | The parent and module `pom.xml` files are well-formed XML with the generator marker, list one sub-module per Java module, depend on the required modules and jspecify, pin every plugin, attach Javadoc JARs, and use the configured groupId and version (invalid values are rejected). |
| `ConstantsGeneratorTest` | Class name rule, basic and struct-literal constants (record, class, `null` and `@@default` filling, `@Deprecated`), loading the compiled constants, and every deferral reason (type not generated, abstraction, missing value, clashing class name). |
| `ExceptionGeneratorTest` | Exception names, standard mapping (checked/unchecked), placement across modules (shortest namespace, transitive requires), errors without common module or with clashing names, `throws` clauses and async documentation, and the runtime behaviour of a generated exception (single constructor, message check, cause). |
| `ClassGeneratorTest` | Which abstraction becomes an abstract class or an interface (attributes, enums, interfaces, multiple inheritance with *none* as result, upward propagation, configuration, `@@finalMethod`), configuration errors, covariant `@@async` overrides, generated classes (state, constructors with `super(...)`, `$$Self` setters, covariant setter overrides, narrowed nullability, defaults, value classes, sealed hierarchies) and their **runtime behaviour** (checks, defensive copies, chained setters, equality). |
| `InterfaceGeneratorTest` | Generated interfaces (accessors, setters returning the self type, abstract and static methods, renamed type variables), records and enums implementing generic interfaces with wrapper types and `@Override`, nullability narrowing, `sealed`/`non-sealed`, supertypes as comment, and every deferral reason. Every case is compiled with `-Xlint:all -Werror`. |
| `RecordGeneratorTest` | Which types become records (inherited attributes, extended types, inherited `@@finalMethod`, type arguments of supertypes), deferral (transitive, through methods, bounds, wildcards; unmapped types), generated source details, and the **runtime behaviour** of the golden records: they are compiled in-process and called (null checks, every constraint, `URI` check, defensive copies, default constructor, `bytes` equality, method stubs). |
| `JavaGeneratorTest` | Golden files for the example specs (`metalang-java/src/test/resources/generator-golden/java`), module/package rules and the three structural errors, Markdown comment escaping, and: the modules generated for **all real specs compile** with `-Xlint:all -Werror` through the module system, and their **generated tests compile and run** — every failure is a method that is not implemented yet. |
| `TestGeneratorTest` | The generated tests of small specs: their content (test names, boundary values, null and copy tests, setters, methods, factories, values of every type, subtypes, factory methods and test doubles, only types of required modules, what cannot be tested), and their **execution**: they are compiled with `-Xlint:all -Werror` and run with the JUnit platform; all pass against the generated code, the method tests fail only because of the stubs, and they **detect mutations** of the generated code (a removed range check, copy or validation). |
| `TestValuesTest` | Test values and generated tests for edge cases (`ANY`, wildcards, sets of enums, unreachable `@@minSize`, functions, deprecated members, nullable parameters, default instances with factory calls); the result is compiled and run. |
| `JavaIntegersTest`, `RegexSamplesTest` | Ranges, range checks and literals of all integer types; accepted and rejected strings for patterns, never a wrong one. |
| `TsGeneratorTest`, `TsTestGeneratorTest` | The TypeScript workspace (packages, exports, dependencies, project references, the support package), every type mapping (classes, interfaces, enum classes, sealed unions, narrowing, constants, overloaded functions, errors), deferral, configuration, determinism across JVM runs, the generated tests (names, boundaries, copies, setters, methods, todo entries), and — with Node.js and TypeScript installed (`npm install` in `sdk-ts`) — that **all real specs compile** with the strict configuration and their tests only fail for stubs, and that the tests **detect mutations**. |
| `TsEdgeCasesTest` | Edge cases of the TypeScript generator, built and run with Node.js: every builtin type as value (`uuid`, `decimal`, `seconds`, `type<T>`, `streamResult`), sealed unions, narrowed attributes, reserved and global names, values that cannot be built (`test.todo`), subtypes, factory methods, default instances with constants, bounds of type parameters for `ANY` arguments, and string escaping. |
| `TsConformanceTest` | Comparison of TypeScript APIs (additions allowed, every kind of difference), a missing TypeScript installation, and — with TypeScript installed — implemented and outdated projects and **`sdk-ts/generated` provides the API of the current specs**. |
| `RustGeneratorTest` | The Cargo workspace (crates, manifests with the used libraries, module tree, re-exports, support module), configuration and structural errors, every mapping of a feature spec (enums with attributes, traits with async/streaming/errors/statics/final methods, `$$Self`, erased `ANY` parameters, sealed enums, overload names, keywords, validation, constants, error enums), and — with Cargo — that the feature spec and **all real specs compile without warnings** and their tests only fail for stubs; determinism across JVM runs. |
| `RustTestGeneratorTest` | The generated tests (names, boundaries, setters, methods, enums, test doubles, ignored tests) and their execution with Cargo: they pass against the generated code, the method tests fail only because of the stubs, and they **detect mutations** (a removed range, length or setter check). |
| `RustEdgeCasesTest` | Edge cases built and run with Cargo: values of every kind, values that cannot be built, subtypes, factories and default instances of every expression kind, conversions (`From` for subtypes, narrowed and erased attributes), visibility across crates and deferred types, name clashes with aliases, literals of every kind. |
| `RustConformanceTest` | Comparison of Rust APIs (additions allowed, every kind of difference), and — with Cargo — implemented (with other imports), outdated and unparsable projects, a missing or failing Cargo, and **`sdk-rust/generated` provides the API of the current specs**. |
| `InstanceResolverTest` | Every kind of default-instance expression (construction with generic arguments, functions of other namespaces, static methods, method calls, attributes, constants, enum constants, lists, bytes), overload selection, and every error message. Rule fixtures cover `instance.invalid`, `instance.cycle` and `instance.missing`. |
| `JavaApiTest` | Reading the API of Java sources: name resolution (imports, wildcards, same package, `java.lang`, nested types, type variables), nullness including `String @Nullable []` vs. `@Nullable String[]`, implicit modifiers of interfaces, records, enums and nested types, ignored implementation details, module declarations, parse errors, duplicate types and skipped build output. |
| `JavaApiComparisonTest` | Implementations and additions are accepted; every kind of difference (missing module/type/member, kind, modifiers, type parameters, superclass, interfaces, permits, record components, enum constants, annotations, member declarations, `requires`/`exports`) is reported with file and line. |
| `JavaConformanceTest` | The generated code and an implementation of it conform, a project that was not updated after a spec change does not, and **`sdk-java/generated` provides the API of the current specs** (fails if it was not regenerated). |
| `CheckCommandTest` | `metalang check`: success and differences with exit codes, configuration, invalid specs and configurations, usage errors. |
| `LinkedRepositorySpecsTest` | Linking all real specs leaves no unresolved reference, every declared type exists, self types are substituted (`Transaction`, `NativeToken`), and linking is deterministic. |

## Known limitations

- **Generic bounds** are checked on the head type only (`G<$$T extends B>` used as `G<Other>`); arguments that are
  generic parameters are not checked.
- **Error positions** can be one token late when the input so far is a valid prefix of another construct
  (e.g. `b int8` is the start of a method declaration).
- **Nesting** of brackets is limited to 100 levels (`syntax.nesting-too-deep`) to keep the recursive parser and
  validator away from stack exhaustion.
- **Markdown:** only ATX headings (`## ...`) are recognized, not setext headings (`---` underlines).
- **Rust:** a type parameter that the specs use with `ANY` is erased everywhere (`HieroClient`, `dyn NativeToken`):
  concrete types keep their exact types in their inherent methods, but code that works with trait objects gets
  erased values (`Response<Arc<dyn Receipt>>`). `type<T>` is a `TypeId`, `@@default` values are no default arguments
  (Rust has none), and `@@oneOf` is not checked (as in Java and TypeScript).
- **Lexical scope:** identifiers are ASCII; number literals support digits, `_`, sign and decimals, but no exponent
  or hex notation. `@@pattern` values are evaluated with Java regular expressions (`find` semantics).
