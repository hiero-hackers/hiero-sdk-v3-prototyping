# Tests defined in the specs: analysis

Can a spec file (`spec/*.md`) define tests in a small, language-neutral DSL, from which every language gets
deterministically generated unit tests? This document collects what the repository already has, what exists elsewhere,
what unit tests look like in the target languages, and sketches a DSL that fits. It is an analysis, not a decision.

## Summary

- **The repository already generates ~7,100 unit tests** (2,373 Java, 3,092 TypeScript, 1,667 Rust) — but all of them
  are **structural**: they are derived from the API schema and check constructors, validation bounds, copies, setters
  and that a method can be called. None of them knows an *expected value*. `AccountId.fromString("0.0.1234")` is
  called, but nothing checks that the result has `num == 1234`.
- **The specs already contain a small expression language**: the `## Default Instances for Testing` section
  (`instance PublicKey = DEFAULT(PrivateKey).createPublicKey()`) has calls with named arguments, struct literals,
  enum constants, attribute access and literals. It is parsed, type-checked against the model and already translated
  into Java, TypeScript and Rust by the test generators.
- **Behaviour with expected values is specified nowhere in executable form.** The `## Testing` section of the testing
  guideline holds prose Given/When/Then scenarios against a Solo network, and exactly one spec
  (`spec/consensus-node-client/queries-accounts.md`) uses it. `## Examples` is illustrative pseudo-code.
- **The closest prior art is Smithy's protocol compliance tests**: test cases are declared in the model, next to the
  API, and every SDK generator turns them into tests of its own language. They are limited to one fixed shape (input
  values -> expected HTTP message) and bound to Smithy, so they are a model for the idea, not a tool to adopt. Shared
  test-vector suites (Wycheproof and others) and script DSLs such as the WebAssembly `.wast` format show the same idea
  from the data side. No existing tool covers this use case.
- **Recommendation:** a `tests` code block in the `## Testing` section, whose expressions extend the default instance
  expressions with statements, operators and a closed list of built-in functions. That reuses parser, resolver and
  the three existing literal/expression renderers, and the validator can type-check every test against the model before
  any language exists. Tests are offline unit tests only; start with Java. The language is defined
  in [`guidelines/test-language.md`](../guidelines/test-language.md).

## What exists in this repository

### Generated contract tests (structural)

`metalang generate` writes tests next to the generated API for Java (JUnit 5), TypeScript (`node:test`) and Rust
(`#[test]` in `tests/`); Go and Python generate none yet. What they check, per `tooling/metalang/README.md`
("Generated tests"):

| Area | Checked | Source of the expectation |
|---|---|---|
| Constructors | values are returned by the accessors; defaults applied; `null` rejected | the schema |
| Validation | boundary values of `@@min`/`@@max`, integer ranges, `@@pattern`, sizes accepted, values just outside rejected | the annotations |
| Copies | collections and `bytes` are copied in and out | the immutability rules |
| Setters | return the object, change the value, keep the old value on rejection | the schema |
| Value semantics | equal values give equal objects and hash codes | the schema |
| Methods | can be called with valid arguments, return non-`null`, throw only declared errors | signature + `@@throws` |

Every expectation is derived from the *shape* of the API. The generators cannot know that `fromString("0.0.1234")`
yields `num == 1234`, that the PKCS#8 encoding of the default key has particular bytes, or that 1 ℏ is 100,000,000 tℏ.
That is the behaviour a spec author knows and currently writes, at best, as prose or pseudo-code.

Example of what is generated today (Rust, `sdk-rust/generated/crates/base/tests/api/ledger/account_id.rs`):

```rust
/// fromString(value) can be called
#[test]
fn from_string_value_can_be_called() {
    let _ = AccountId::from_string(value_value());
}
```

### `## Default Instances for Testing` — an expression DSL that already works

Four specs declare default instances (see "Default instances" in `guidelines/api-guideline.md`):

```
instance PrivateKey = createPrivateKey(algorithm: KeyAlgorithm.ED25519, encoding: ByteImportEncoding.HEX,
        value: "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c")
instance PublicKey = DEFAULT(PrivateKey).createPublicKey()
instance ConsensusNode = ConsensusNode{ip: IpAddress{bytes: [34, 94, 106, 61]}, port: 50211, account: DEFAULT}
```

What the tooling already does with it:

- **Parsing and resolution** (`ast.Instance`, `model.InstanceResolver`): calls of namespace functions, `@@static`
  methods and instance methods with **named arguments**, overload selection by names and types, struct literals,
  attribute access, enum constants, constants, list literals, `DEFAULT(Type)`.
- **Validation** (`instance.invalid`, `instance.cycle`, `instance.missing`): unknown calls, wrong types and cycles are
  errors at spec level — before any language is involved.
- **Translation** into Java, TypeScript and Rust by the test generators, which use default instances before any other
  way of obtaining a value.

This is most of what a test DSL needs on the *action* side. What is missing is the *assertion* side.

### `## Testing` and the testing guideline (network scenarios, prose)

`guidelines/testing-guideline.md` puts a `## Testing` section into the spec skeleton (the validator knows the section
and its order) and defines **Given / When / Then scenarios with stable ids** (`queries.accounts/balance-of-operator-succeeds`)
that run against a real Solo network. Each language translates the prose into its own test by hand. Only
`spec/consensus-node-client/queries-accounts.md` has such a section.

The scenarios are the right *content* for integration tests, but in prose they cannot be generated, checked against
the model, or kept in sync when the API changes.

### `## Examples`

Illustrative pseudo-code in a Java-like notation (`AccountInfo info = response.value;`). Not parsed, not executed —
and it drifts: nothing notices when an example stops matching the schema.

### The TCK binding (network behaviour, cross-language)

`docs/tck-binding.md` binds the methods of the Hiero TCK to the API; servers are generated for Java and TypeScript. The
TCK covers network behaviour through a JSON-RPC server. It does not cover local behaviour (parsing, encoding,
conversion), and its test cases live in the TCK repository, not in the specs.

### The gap

| Kind of test | Today |
|---|---|
| Shape of the API (constructors, bounds, copies) | generated, Java / TS / Rust |
| **Local behaviour with expected values** (parse, format, encode, convert, validate a concrete value) | **nowhere executable** |
| Network behaviour, cross-language | Hiero TCK through the generated TCK servers |
| Network behaviour per spec | prose Given/When/Then in one spec, implemented by hand |

## Prior art outside the repository

| Approach | Where the tests live | How each language gets them | Deterministic per language? |
|---|---|---|---|
| **Smithy protocol compliance tests** (`httpRequestTests`, `httpResponseTests`) | traits in the model, next to the operation they test | generated at build time or loaded at runtime by each SDK generator; the spec names generating JUnit cases as the Java example | **yes** — closest to this proposal |
| **Shared test vectors** (Wycheproof for cryptography; similar JSON/textproto suites for URL parsing, JSON Schema, CEL) | data files (JSON with a schema) | a hand-written runner per language iterates the vectors | the data yes, the runner by hand |
| **Script DSLs** (WebAssembly spec tests, `.wast`: `assert_return`, `assert_trap`) | test scripts in a minimal command language | each implementation interprets the script | yes, via an interpreter instead of generated code |
| **Gherkin / Cucumber** | `.feature` files in prose (Given/When/Then) | step definitions written by hand per language | no — prose steps need hand-written glue |
| **Doctests** (Python `doctest`, Rust doc tests) | examples in documentation | executed by the language's own tooling | single language only |
| **Conformance servers** (Hiero TCK, protobuf conformance) | a central test repository | a server/adapter per language under test | the tests yes, the adapter by hand or generated |
| **Property/schema-based** (e.g. Hypothesis, OpenAPI-driven fuzzers) | generators of inputs, invariants | a framework per language | no fixed expected values |

Lessons for this repository:

1. **Put tests next to the declaration they test**, in the model — Smithy does exactly that, and it is what keeps a
   test in sync with the API (renaming a parameter breaks the test at validation time, not at runtime).
2. **Keep the vocabulary closed and small.** `.wast` gets away with a handful of assertion commands; Gherkin's open
   prose is what forces hand-written step definitions. A closed vocabulary is the precondition for deterministic code
   generation.
3. **Prefer known-answer vectors for cryptography and encodings.** Wycheproof's value is fixed inputs with fixed
   expected outputs, including invalid ones. Key import/export (`spec/base/keys.md`) is exactly such a domain.
4. **Generated code beats a runtime interpreter here**: the repository already generates idiomatic tests per language,
   and generated tests read like hand-written ones in a failure report.

## What unit tests look like in the target languages

The generators already use the first three columns' frameworks; Go and Python generate no tests yet.

| | Java | TypeScript | Rust | Go | Python | C++ | Swift |
|---|---|---|---|---|---|---|---|
| Framework | JUnit 5 (used by the generator) | `node:test` (used by the generator); Vitest/Jest common | built-in `#[test]`, integration tests in `tests/` (used by the generator) | built-in `testing`, `go test` | `pytest` (de facto) or `unittest` | GoogleTest or Catch2 | Swift Testing (`@Test`) or XCTest |
| A test | `@Test void name()` in a class, `@DisplayName` | `test("name", () => …)` in `describe` | `#[test] fn name()` | `func TestName(t *testing.T)` | `def test_name():` | `TEST(Suite, Name)` | `@Test func name()` |
| Equality | `assertEquals` / AssertJ | `assert.strictEqual`, `deepStrictEqual` | `assert_eq!` | `if got != want { t.Errorf(…) }` | `assert a == b` | `EXPECT_EQ` | `#expect(a == b)` |
| Expected error | `assertThrows(Type.class, …)` | `assert.throws(…, Type)` | `Result` matched with `assert!(matches!(…))`; `#[should_panic]` for panics | `errors.Is` / `errors.As` on the returned `error` | `with pytest.raises(Type):` | `EXPECT_THROW(stmt, Type)` | `#expect(throws: Type.self) { … }` |
| Table-driven | `@ParameterizedTest` + sources | a loop over cases, one `test` each | a loop, or a macro per case | the idiom: a slice of cases, `t.Run` per case | `@pytest.mark.parametrize` | `TEST_P` / `INSTANTIATE_TEST_SUITE_P` | `@Test(arguments: …)` |
| Async | `CompletableFuture.join()` | `async` test function | an executor (`block_on` in the generated helpers, or `#[tokio::test]`) | blocking call with `context.Context` | `async def` test with a plugin, or `asyncio.run` | blocking or coroutine helpers | `async` test function |
| Location | `src/test/java`, same package | `*.test.ts` next to the source | `tests/` per crate | `_test.go` next to the source | `tests/` or next to the package | a test target | a test target |

The differences a generator has to bridge, independent of syntax:

- **Error identity.** The specs name errors (`@@throws(illegal-format)`); each language maps an id to an exception
  type, an error class, an `error` value or a `Result` variant. The test DSL must assert *ids*, never types, and every
  language's mapping (already defined in its best-practices guide) decides the check.
- **Numbers.** `uint64` is `long` in Java (unsigned helpers), `bigint` in TypeScript, `u64` in Rust, `int` in Python;
  literals in tests need the same per-language rendering the generators already have (`JavaLiterals`,
  `TsLiterals`, …).
- **Equality.** Value equality is native for records/dataclasses/`PartialEq`; Go structs with slices are not
  comparable with `==`; `bytes` compare with `Arrays.equals`, `deepStrictEqual`, `==` on slices of `u8`,
  `bytes.Equal`. Floating point needs a tolerance or exact literals.
- **Async.** `@@async` methods must be awaited in the language's way; Go blocks, Python awaits, Java joins.
- **Names.** A test name has to become an identifier (`should…` in Java, snake case in Rust and Python, `TestX` in Go)
  and stay unique.

## Sketch of a test DSL

> The language that followed from this sketch is defined in
> [`guidelines/test-language.md`](../guidelines/test-language.md). It goes beyond the sketch: it adds arithmetic,
> `if`/`else`, `for` and a closed list of built-in functions, and defines their semantics for every language (see
> "Scope" below).

A `tests` code block in the `## Testing` section, like the `bindings` blocks of the TCK binding. Its expressions are the
default instance expressions; the new part is a closed set of statements.

```tests
// AccountId parses its text form.
test "fromString reads shard, realm and number" {
    let id = AccountId.fromString(value: "0.0.1234")
    expect id.shard == 0
    expect id.realm == 0
    expect id.num == 1234
    expect id.evmAddress is null
}

test "fromString rejects text that is no account id" {
    expect AccountId.fromString(value: "account") throws illegal-format
}

// Known-answer test for the default key of keys.md.
test "the default key has its PKCS#8 DER encoding" {
    expect DEFAULT(PrivateKey).toBytes(container: KeyFormat.PKCS8_WITH_DER)
        == hex("302e020100300506032b657004220420d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c")
}

// Table-driven: one generated test per row.
test "fromString reads the account number" for (text, number) in [
    ("0.0.1", 1),
    ("0.0.1234", 1234),
    ("1.2.3", 3),
] {
    expect AccountId.fromString(value: text).num == number
}
```

| Statement | Meaning |
|---|---|
| `test "name" { … }` | one test; the name becomes the test's display name and, converted, its identifier |
| `test "name" for (a, b) in [ … ] { … }` | one test per row (parameterized test in Java/Python/Swift, a loop with subtests in Go); postponed in the test language |
| `let x = <expression>` | binds a value; expressions are the default instance expressions (calls with named arguments, `Type{…}`, `DEFAULT`, enum constants, attribute access, literals) |
| `expect <a> == <b>` / `!=` | value equality, with the language's equality for bytes, collections and records |
| `expect <a> is null` / `is not null` | nullable results |
| `expect <a> is <Type>` | the subtype of an abstraction or sealed type |
| `expect <call> throws <error-id>` | the call fails with an error the spec declares in `@@throws` |
| `hex("…")`, `[1, 2, 3]` | bytes |

Rules that make it checkable and deterministic:

- **A closed vocabulary.** No user functions, no variables that change, no `while`; the built-in functions are a
  fixed list. A closed vocabulary is what lets every generator translate every test; anything more belongs in
  hand-written tests. (The sketch started without any control flow; the test language now has `if` and `for`, see
  "Scope" below.)
- **Every expression is resolved against the model** like a default instance: unknown methods, wrong argument names
  and type mismatches are validation errors (`test.invalid`), so a test breaks when the API changes, in the spec
  build.
- **`throws` only accepts error ids the called method declares** (`test.undeclared-error`), so the test and the
  `@@throws` contract cannot drift apart.
- **`@@async` calls are awaited implicitly**; a test that calls one becomes an async test where the language needs
  that.
- **`==` is value equality, never identity, and there is no identity operator.** The meta-language has no notion
  of object identity, and several targets have none either: Rust and Go values are moved or copied, so "the same
  instance" is a pointer comparison (`std::ptr::eq`, `==` on pointers) that only exists for some shapes. The one place
  where identity is part of the API contract — a setter returns the object it was called on — follows from the schema
  and is already tested by the structural tests (`assertSame` in Java). Generators must not translate `==` to the
  target's `==` operator: in Java and JavaScript that is identity for objects. It becomes `assertEquals`,
  `deepStrictEqual`, `assert_eq!`, `reflect.DeepEqual`/`bytes.Equal`, Python `==`.
- **Stable ids.** A test id is `<namespace>/<test-name-in-kebab-case>`, like the scenario ids of the testing guideline,
  so a test can be referenced, skipped or deprecated across languages.

### Unit tests only

Spec tests check local behaviour and need no network. Network scenarios (the Given/When/Then prose of the testing
guideline) would need fixtures per language — a client, a funded account, clean-up of network state — and a separate
integration suite in every build; that is out of scope. Network behaviour stays with the Hiero TCK and the
hand-written scenarios.

### Mapping one statement to the languages

`expect AccountId.fromString(value: "0.0.1234").num == 1234`:

| Language | Generated |
|---|---|
| Java | `assertEquals(Long.valueOf(1234L), AccountId.fromString("0.0.1234").num());` |
| TypeScript | `assert.strictEqual(AccountId.fromString("0.0.1234").num, 1234n);` |
| Rust | `assert_eq!(AccountId::from_string("0.0.1234").num(), Some(1234));` |
| Go | `if got := mustAccountIDFromString(t, "0.0.1234").Num(); got == nil \|\| *got != 1234 { t.Errorf(…) }` |
| Python | `assert AccountId.from_string(value="0.0.1234").num == 1234` |

`expect AccountId.fromString(value: "account") throws illegal-format`:

| Language | Generated |
|---|---|
| Java | `assertThrows(IllegalArgumentException.class, () -> AccountId.fromString("account"));` |
| TypeScript | `assert.throws(() => AccountId.fromString("account"), RangeError);` |
| Rust | `assert!(matches!(AccountId::from_string("account"), Err(IllegalFormatError { .. })));` |
| Go | `if _, err := ledger.AccountIDFromString("account"); !errors.Is(err, ledger.ErrIllegalFormat) { … }` |
| Python | `with pytest.raises(ValueError): AccountId.from_string(value="account")` |

(The error types follow each language's existing mapping of error ids; the table illustrates the shape, not final
names.)

## What makes this harder than it looks

- **Generated tests fail until the API is implemented.** Like the existing method tests, every behavioural test is red
  against the stubs. That is the point — the tests are the acceptance criteria an implementation has to meet — but the
  build must keep distinguishing "not implemented yet" from "wrong".
- **Go and Python generate no tests yet.** Their generators emit methods and functions, but none of the test
  infrastructure the Java, TypeScript and Rust generators share; that has to come first.
- **Hidden equality semantics.** `expect a == b` on an abstraction or a type with a `bytes` attribute needs a defined
  meaning per language; the records/dataclasses are fine, Go structs with slices are not comparable with `==`.
- **Overloads in Go** become distinct function names; the generator has to resolve a call to the right one — the same
  resolution the Go generator will need for its methods anyway.
- **Literal fidelity.** `uint64` maximum values, decimals and timestamps need exact per-language literals; the existing
  literal renderers cover most of it, timestamps and durations are not covered yet.
- **Scope.** Every construct beyond calls and comparisons has to be translated into every language *with the same
  meaning*, and that is where the languages differ: integer overflow (Java wraps, Rust panics in debug builds,
  TypeScript `bigint` and Python `int` are unbounded), integer division (Python's `//` rounds down, the others
  towards zero), string length (UTF-16 units in Java and JavaScript, bytes in Rust and Go, code points in Python),
  equality of collections and structs. Each construct added makes the language more useful and every generator more
  expensive, and an undefined detail makes a test mean different things in different languages without anyone
  noticing. The decision: arithmetic, `if`/`else`, `for` and basic functions for strings, bytes and numbers are part of
  the language, because the intended tests need them — but each of them is defined once, exactly, in
  `guidelines/test-language.md`, and a generator reproduces that definition through a hand-written test support
  module where its language behaves differently. User-defined functions, mutable variables and `while` stay out; the
  list of built-in functions stays closed.

## Recommendation and next steps

No existing tool covers this: the prior art either is bound to its own model language (Smithy), keeps the tests as
data with a hand-written runner (test vectors, TCK) or needs hand-written glue (Gherkin). Most of the building blocks
exist in this repository, so the language is built here.

1. **The guideline** — done as a draft: [`guidelines/test-language.md`](../guidelines/test-language.md) defines
   syntax, semantics, built-in functions and validation rules.
2. **Grammar and resolver** in `metalang-core`: a `tests` entry rule in `MetaLang.g4` that reuses the existing
   `expression` rule, new AST nodes, and a type checker built on `InstanceResolver` (scoped variables, operators,
   built-in functions, narrowing of nullable variables); the `test.*` validation rules with rule fixtures.
3. **One generator first**: Java, where expressions and literals are most complete (`JavaSamples`, `JavaLiterals`),
   then TypeScript and Rust, which share the existing test infrastructure. Go and Python once they generate tests at
   all.
4. **Seed the specs with known-answer tests where the behaviour is fixed and checkable offline**: key import/export of
   `spec/base/keys.md` (the default key already has known bytes), `AccountId`/`Address` text forms and checksums,
   native-token unit conversion, `EvmAddress` parsing.
5. **No network tests in the specs.** Network behaviour stays with the Hiero TCK and the scenarios of the testing
   guideline.

## Questions & Comments

- **One block per spec or tests next to each type?** Smithy attaches tests to the operation they test. A `tests`
  block per spec is simpler to parse and to review; per-declaration tests would make "every method has a test"
  checkable. — open
- **Should the DSL replace `## Examples`?** Examples written as tests would be checked and executed instead of drifting.
  They would also lose the narrative style that makes them good documentation. — open
- **Shared test vectors as data?** For cryptography a Wycheproof-style vector file (many rows, one runner) may be better
  than many DSL tests; the `for … in [ … ]` form covers small tables, a large table would bloat the spec. — open
