# Test Language Guideline

This document defines the **test language**: a small, language-neutral language for writing tests inside a spec file,
from which every target language gets deterministically generated unit tests. It is the counterpart of
[`api-guideline.md`](api-guideline.md) for tests: the API guideline defines how an API is *described*, this document
defines how its behaviour is *checked*.

> **Status: draft.** Nothing of this document is implemented yet. The analysis that led to it is
> [`docs/spec-defined-tests.md`](../docs/spec-defined-tests.md). Open decisions are marked in the text and collected
> under [Questions & Comments](#questions--comments).

## Goals and limits

- **Write a test once, get it in every language.** A test in a spec is translated into an idiomatic unit test of
  every target language (JUnit, `node:test`, `#[test]`, `go test`, pytest, …). The generated test is a normal test of
  that language: it runs in the IDE, it can be debugged, and its failure report names the spec test.
- **The same test means the same thing everywhere.** That is the hard part. Strings, integers, division and equality
  behave differently in Java, TypeScript, Rust, Go and Python. This document therefore defines the **semantics** of
  every construct, not only its syntax, and a generator has to reproduce that semantics — where the target language
  behaves differently, through a helper of its test support module (see
  [Translation into the languages](#translation-into-the-languages)).
- **Checked before any language exists.** Every test is parsed and type-checked against the API model by the spec
  validator. A test that calls a method that does not exist, passes an argument of the wrong type or expects an error
  the method does not declare is a validation error in the spec build.
- **A closed language.** The language has variables, arithmetic, conditions, loops and a fixed list of built-in
  functions. It has **no user-defined functions, no mutable variables, no `while` and no access to anything outside
  the API** (files, clock, randomness, environment). What does not fit is written by hand as a test of the SDK of one
  language.
- **Unit tests only.** A test checks local behaviour — parsing, formatting, encoding, conversion, validation, signing —
  and must not need a network. Network behaviour is tested by the Hiero TCK and by the scenarios of
  [`testing-guideline.md`](testing-guideline.md).

## Where tests live

Tests are written in the `## Testing` section of a spec, in one or more code blocks of the info string `tests`.
Prose around the blocks is allowed and is not part of the tests.

````markdown
## Testing

Parsing of account ids.

```tests
test "fromString reads shard, realm and number" {
    let id = AccountId.fromString(value: "0.0.1234")
    assertEquals(expected: 1234, actual: id.num)
}
```
````

Like the `## Default Instances for Testing` block, a `tests` block uses the namespace and the imports of the file's API schema; it
has no `namespace` or `requires` statement of its own. Types of other namespaces are referenced as in the schema:
imported, or qualified to resolve a name collision.

A test may use the API of every namespace the file imports, and the default instances of every type
(`DEFAULT(PrivateKey)`). Tests should test the types of their own spec; using other types to *obtain* values is
expected.

## Tests

```
test "name" {
    statements
}
```

- **Name.** A string that describes the behaviour in one sentence, written like a method comment: `"fromString
  rejects text that is no account id"`. It is the display name of the generated test.
- **Id.** `<namespace>/<name in kebab-case>`, e.g. `ledger/fromstring-rejects-text-that-is-no-account-id`. Ids are
  unique per namespace (`test.duplicate-id`). Generators derive the test's identifier from the name (`fromStringRejects…`
  in Java, `from_string_rejects_…` in Rust and Python, `TestFromStringRejects…` in Go); an identifier that becomes
  unusable in a language (too long, collides) is shortened by the generator, never by the author.
- **Annotations** precede `test`:

| Annotation | Meaning |
|---|---|
| `@@deprecated("reason")` | The test still runs but is scheduled for removal, e.g. together with a deprecated method. |
| `@@disabled("reason")` | The test is generated but not run (`@Disabled`, `test.skip`, `#[ignore]`, `t.Skip`, `pytest.mark.skip`). The reason is mandatory. |

A test passes when all its statements run without a failed assertion and without an unexpected error.

## Statements

| Statement | Meaning |
|---|---|
| `let name = expression` | Evaluates the expression once and binds the value to `name`. The binding cannot change; a name cannot be bound twice in a test, also not in a nested block (`test.duplicate-name`). The type is inferred. |
| `let name: Type = expression` | The same with a declared type, for a value whose type is not obvious or must be a supertype. |
| `call(…)` | Calls a function or method and ignores its result; typically an [assertion](#assertions) such as `assertEquals(expected: 1234, actual: id.num)`. |
| `@@expectThrows(error-id) call(…)` | Fails the test unless the call fails with the error `error-id`, see [Errors](#errors). |
| `if condition { … } else if condition { … } else { … }` | Runs one of the blocks. |
| `for name in expression { … }` | Runs the block once per element of a list or a set (in an unspecified order, so the block must not depend on it); `range(start: 0, end: 10)` gives a list of integers. |

There is no assignment, no `while`, no `break`/`continue` and no `return`. A loop always ends: it iterates a finite
collection, and the collection is computed before the first iteration.

`for` is for checks over the elements of a value (`for key in keys { … }`); it does not create separate test cases.

### Assertions

Checks are not statements of the language but functions of the namespace `testing`, like the other
[built-in functions](#built-in-functions): `assertEquals`, `assertNotEquals`, `assertTrue`, `assertFalse`,
`assertNull`, `assertNotNull`, `assertInstanceOf`, `assertApprox` and `fail`. Each has an optional `message` for the
failure report. They are declared, type-checked and called like every other function, and every generator maps each
of them onto one assertion of its test framework.

```
let id = AccountId.fromString(value: "0.0.1234")
assertEquals(expected: 0, actual: id.shard)
assertNull(actual: id.evmAddress, message: "an id by number has no EVM address")
assertTrue(condition: length(value: id.toString()) > 4)
```

The one check that is no function is the expectation of an error: the annotation `@@expectThrows(error-id)` on a
call statement. As a function it would need the call as a function value, because the arguments of a function are
evaluated before the function runs and the error would fail the test before the assertion could catch it. The
meta-language has function types, but the test language would need a lambda syntax to create one. An annotation needs
no new syntax: it is written like the annotations of the API, and its error id like the one of `@@throws`.

```
@@expectThrows(illegal-format)
AccountId.fromString(value: "account")
```

## Expressions

The test language extends the expression language of the default instances (see "Default instances" in
[`api-guideline.md`](api-guideline.md)): everything that is valid there is valid here, with the same meaning.

| Expression | Meaning |
|---|---|
| `DEFAULT`, `DEFAULT(Type)` | The default instance of the expected type or of the given type. |
| `Type{attribute: value, ...}` | Creates a complex type. |
| `function(parameter: value, ...)`, `Type.method(...)`, `value.method(...)` | Calls a namespace function, a `@@static` method or a method. Arguments are **always named**. |
| `value.attribute` | Reads an attribute. |
| `Enum.VALUE`, `CONSTANT` | Enum constants and constants. |
| `"text"`, `42`, `-7`, `1.5`, `true`, `false`, `null` | Literals. |
| `[a, b, c]` | A list (or a set, where a set is expected). |
| `{key: value, ...}` | A map. Keys are literals or constants. |
| `name` | A variable bound with `let` or `for`. |
| `a[i]` | The element of a list at index `i`, or the value of a map for key `i`. |
| `a op b`, `-a` | Operators, see below. Negation of a `bool` is the function `not(value: a)`. |
| `(a)` | Grouping. |

The [built-in functions](#built-in-functions) are functions of their own namespace and are called like any other
function.

### Operators

Precedence, from the highest to the lowest; operators of the same line are left-associative:

```
highest   a.b   a.m(…)   a[i]
          -a
          a * b   a / b   a % b
          a + b   a - b
          a < b   a <= b   a > b   a >= b
          a == b   a != b   a is null   a is not null   a is Type   a is not Type
          a && b
lowest    a || b
```

Operands and results:

| Operator | Operands | Result |
|---|---|---|
| `-a` | a number | a number of the same kind |
| `a * b`, `a / b`, `a % b` | two numbers of the same kind | a number of the same kind |
| `a + b`, `a - b` | two numbers of the same kind | a number of the same kind |
| `a + b` | two strings, two `bytes` or two lists | the concatenation, of the same type |
| `a < b`, `a <= b`, `a > b`, `a >= b` | two numbers of the same kind, or two values of the same type out of `date`, `time`, `dateTime`, `seconds`, `duration` | `bool` |
| `a == b`, `a != b` | two values of compatible types (see [Equality](#equality)) | `bool` |
| `a is null`, `a is not null` | a value of a nullable type | `bool` |
| `a is Type`, `a is not Type` | a value of a supertype of `Type` | `bool` |

The logical operators `&&` and `||` take two `bool` values and evaluate their right operand only if needed. There is
no conditional operator (`a ? b : c`); use `if`.

## Values

### Types

Values have the types of the API guideline. The test language adds no own types except:

- **Integer and decimal literals have no fixed width.** `1234` is an integer literal that fits every integer type
  that can hold its value; `1.5` fits `double` and `decimal`. A literal that fits no expected type is an error
  (`test.out-of-range`).

## Semantics

This section is binding for generators: a generated test must behave exactly as described here, whatever the target
language does natively.

### Evaluation order

Expressions are evaluated from left to right, **arguments in the order in which they are written** — not in the order
of the parameters. `f(b: x(), a: y())` calls `x()` before `y()`. A generator for a language that passes arguments by
position binds such arguments to temporary variables first, if the order differs and an argument has an effect.

### Integers

- **Arithmetic is exact.** `+`, `-`, `*` on integers never overflow: the test language computes with unlimited
  precision. A value is checked against its type only where it leaves the test language: as an argument of an API
  call, as an attribute of a `Type{…}`. A value that does not fit the parameter's type fails the test (statically,
  where the validator can compute it: `test.out-of-range`).
- **Division truncates towards zero**: `-7 / 2 == -3`. `%` has the sign of the dividend: `-7 % 2 == -1`. (Java, Rust,
  Go, C++ and TypeScript `bigint` behave like this; Python's `//` and `%` do not.)
- **Division by zero fails the test.**
- Integers of different API types (`int32`, `uint64`, …) mix freely; an integer does not mix with `double` or
  `decimal` without a conversion (`toDouble`, `toDecimal`).

### `double` and `decimal`

- `double` follows IEEE 754 binary64. `==` on `double` is exact; use `assertApprox(expected: a, actual: b, tolerance: t)` or
  `approx(actual: b, expected: a, tolerance: t)` for computed values.
  `NaN` is not equal to itself; a test that needs `NaN` checks it with `isNaN(value: a)`.
- `decimal` is exact, with unlimited precision. Division of decimals is only allowed through
  `divide(dividend: a, divisor: b, scale: n)`, which rounds half-even to `scale` digits; `/` on decimals is an error (`test.invalid`).

### Strings

- A string is a sequence of **Unicode code points**. `length`, indexes and `substring` count code points — not UTF-16
  units (Java, JavaScript), not bytes (Rust, Go). `length(value: "ℏ") == 1`.
- `==` compares code point by code point. There is no Unicode normalization, no locale, no case folding.
- String literals use the escapes `\"`, `\\`, `\n`, `\t` and `\u{1F600}` (a code point in hex).

### `bytes`

- `bytes` values compare element by element.
- Values: `hex(text: "0a1b")` (case-insensitive, whitespace between bytes allowed: `hex(text: "0a 1b")`),
  `base64(text: "Chs=")`, `utf8(text: "text")`, or a list of integers `[10, 27]` where `bytes` is expected. Each byte is between 0 and 255.

### Equality

`a == b` is **value equality** in every case. It is never identity, and the test language has no identity operator:
the meta-language has no notion of object identity, and several target languages have none for these types.

| Type | Equal when |
|---|---|
| numbers, `bool`, `string`, `bytes`, `uuid`, date and time types, durations | the values are equal as described above |
| enum constants | they are the same constant |
| `list<T>` | same size, and equal elements in the same order |
| `set<T>` | same size, and every element of one is equal to an element of the other |
| `map<K, V>` | same keys, and equal values per key |
| complex types | the same concrete type, and all attributes (including inherited ones) are equal |
| `null` | `null == null`; `null` is equal to no other value |

The two operands must have compatible types: one must be assignable to the other (`test.incomparable`). Comparing an
`AccountId` with a `string` is an error, not `false`.

Generators must not translate `==` into the target's `==` operator where that operator means identity (objects in
Java, JavaScript and TypeScript) or does not compare contents (Go slices and maps).

### `null`

- An expression of a `@@nullable` type cannot be used as the operand of an operator other than `==`, `!=` and `is`,
  as the receiver of a method call or attribute access, or as an argument of a non-nullable parameter
  (`test.nullable`).
- **Narrowing:** after `assertNotNull(actual: x)`, the variable `x` has the non-null type in the rest of its block. The
  same holds inside `if x is not null { … }`. Narrowing applies to variables only, not to expressions like `a.b`;
  bind such an expression with `let` first.

### Errors

- A call statement annotated with `@@expectThrows(error-id)` passes if the call fails with `error-id`, and fails the
  test if it completes normally or fails with another error. The annotation is allowed on call statements only — not
  on `let`, `if` or `for` (`test.invalid`) — and at most once per statement. The error must be declared by at least one
  call in the statement (`@@throws`) — otherwise the validator reports
  `test.undeclared-error`.
- Every other error, anywhere in the test, fails the test with that error.
- An error that the test language itself raises (division by zero, an index out of range, a value out of range for a
  parameter, a failed assertion) is always a test failure and cannot be expected with `@@expectThrows`.

### `@@async` and `@@streaming`

- A call of an `@@async` method is awaited implicitly; the expression has the type of the result. A test that contains
  such a call becomes an asynchronous test where the language needs that.
- A `@@streaming` method cannot be called in a test yet (`test.invalid`). — *open, see
  [Questions & Comments](#questions--comments).*

## Built-in functions

The built-in functions — the [assertions](#assertions) and the helpers for strings, bytes, collections and numbers —
are namespace-level functions of the namespace `testing`, declared in the meta-language like any API. Every `tests` block imports them implicitly (`requires {*} from testing`), and they are called like every
other function: unqualified, with **named arguments** — `length(value: publicKey.bytes)`. Where a name of the spec's
own namespace or of an import collides with a built-in function, the call is qualified (`testing.length(value: s)`);
an unqualified call that fits both is an error (`test.ambiguous-call`).

The namespace is **closed**: a function that is not declared here does not exist, and adding one requires a change of
this document and of every generator. The namespace exists only in tests; generators implement it in the test support
module of their language and never generate it into the SDK.

What the declarations do not express, and what holds for all of them:

- An assertion reports the expected and the actual value and its `message` when it fails. Equality in
  `assertEquals` and `assertNotEquals` is the value equality of [Equality](#equality).
- Positions and lengths count code points for `string`, bytes for `bytes` and elements for lists (see
  [Strings](#strings)).
- A position out of range, an integer argument that does not fit its parameter type (see [Integers](#integers)) and
  text that `hex`, `base64` or `toInt` cannot read fail the test. They are not errors in the sense of `@@throws` and
  cannot be expected with `@@expectThrows`.

```
namespace testing

// --- assertions ------------------------------------------------------------------------------

// Fails the test unless actual is equal to expected (value equality).
@@static void assertEquals<$$T>(@@nullable expected: $$T, @@nullable actual: $$T, @@nullable message: string)

// Fails the test if actual is equal to unexpected (value equality).
@@static void assertNotEquals<$$T>(@@nullable unexpected: $$T, @@nullable actual: $$T, @@nullable message: string)

// Fails the test unless the condition is true.
@@static void assertTrue(condition: bool, @@nullable message: string)

// Fails the test unless the condition is false.
@@static void assertFalse(condition: bool, @@nullable message: string)

// Fails the test unless the value is null.
@@static void assertNull<$$T>(@@nullable actual: $$T, @@nullable message: string)

// Fails the test if the value is null.
@@static void assertNotNull<$$T>(@@nullable actual: $$T, @@nullable message: string)

// Fails the test unless actual is an instance of the expected type.
@@static void assertInstanceOf<$$T, $$V>(expected: type<$$T>, @@nullable actual: $$V, @@nullable message: string)

// Fails the test unless actual differs from expected by at most tolerance.
@@static void assertApprox(expected: double, actual: double, tolerance: double, @@nullable message: string)

// Fails the test unconditionally.
@@static void fail(message: string)

// --- booleans --------------------------------------------------------------------------------

// The negation of the value.
@@static bool not(value: bool)

// --- strings ---------------------------------------------------------------------------------

// Number of code points of the string.
@@static int64 length(value: string)

// The code points from start (inclusive) to end (exclusive).
@@static string substring(value: string, start: int64, end: int64)

// Whether the string contains the part.
@@static bool contains(value: string, part: string)

// Whether the string starts with the prefix.
@@static bool startsWith(value: string, prefix: string)

// Whether the string ends with the suffix.
@@static bool endsWith(value: string, suffix: string)

// The string repeated count times, for boundary tests.
@@static string repeat(value: string, count: int64)

// --- bytes -----------------------------------------------------------------------------------

// Number of bytes.
@@static int64 length(value: bytes)

// The bytes from start (inclusive) to end (exclusive).
@@static bytes slice(value: bytes, start: int64, end: int64)

// Whether the bytes contain the sequence part.
@@static bool contains(value: bytes, part: bytes)

// Whether the bytes start with the prefix.
@@static bool startsWith(value: bytes, prefix: bytes)

// Whether the bytes end with the suffix.
@@static bool endsWith(value: bytes, suffix: bytes)

// The bytes repeated count times.
@@static bytes repeat(value: bytes, count: int64)

// Bytes from hex text: case-insensitive, whitespace between bytes allowed.
@@static bytes hex(text: string)

// Bytes from Base64 text (RFC 4648, with padding).
@@static bytes base64(text: string)

// The UTF-8 encoding of the text.
@@static bytes utf8(text: string)

// Lowercase hex of the bytes, without separators.
@@static string toHex(value: bytes)

// --- collections -----------------------------------------------------------------------------

// Number of elements.
@@static int64 length<$$T>(value: list<$$T>)

// Number of elements.
@@static int64 length<$$T>(value: set<$$T>)

// Number of entries.
@@static int64 length<$$K, $$V>(value: map<$$K, $$V>)

// The elements from start (inclusive) to end (exclusive).
@@static list<$$T> slice<$$T>(value: list<$$T>, start: int64, end: int64)

// Whether the list contains an element equal to the given one.
@@static bool contains<$$T>(value: list<$$T>, element: $$T)

// Whether the set contains an element equal to the given one.
@@static bool contains<$$T>(value: set<$$T>, element: $$T)

// Whether the map contains the key.
@@static bool containsKey<$$K, $$V>(value: map<$$K, $$V>, key: $$K)

// The keys of the map, in an unspecified order.
@@static list<$$K> keys<$$K, $$V>(value: map<$$K, $$V>)

// The values of the map, in an unspecified order.
@@static list<$$V> values<$$K, $$V>(value: map<$$K, $$V>)

// The integers from start (inclusive) to end (exclusive).
@@static list<int64> range(start: int64, end: int64)

// --- numbers ---------------------------------------------------------------------------------

// The absolute value.
@@static int256 abs(value: int256)

// The absolute value.
@@static double abs(value: double)

// The absolute value.
@@static decimal abs(value: decimal)

// The smaller of the two values.
@@static int256 min(a: int256, b: int256)

// The smaller of the two values.
@@static double min(a: double, b: double)

// The smaller of the two values.
@@static decimal min(a: decimal, b: decimal)

// The larger of the two values.
@@static int256 max(a: int256, b: int256)

// The larger of the two values.
@@static double max(a: double, b: double)

// The larger of the two values.
@@static decimal max(a: decimal, b: decimal)

// Whether actual differs from expected by at most tolerance.
@@static bool approx(actual: double, expected: double, tolerance: double)

// Whether the value is NaN.
@@static bool isNaN(value: double)

// The quotient, rounded half-even to scale digits after the point.
@@static decimal divide(dividend: decimal, divisor: decimal, scale: int32)

// --- conversions -----------------------------------------------------------------------------

// Decimal text without grouping, e.g. "-1234".
@@static string toString(value: int256)

// Decimal text without grouping, e.g. "0.5".
@@static string toString(value: decimal)

// Parses decimal text; text that is no integer fails the test.
@@static int256 toInt(text: string)

// The nearest double.
@@static double toDouble(value: int256)

// The nearest double.
@@static double toDouble(value: decimal)

// The exact value.
@@static decimal toDecimal(value: int256)

// The exact value.
@@static decimal toDecimal(value: double)
```

## Validation

The spec validator parses and checks every `tests` block. The rules (all ERROR unless noted):

| Rule | Reported when |
|---|---|
| `syntax.error` | the block cannot be parsed |
| `test.invalid` | an expression does not resolve: unknown method, attribute or variable, missing or unknown argument, no matching overload, wrong type |
| `test.duplicate-id` | two tests of one namespace have the same id |
| `test.duplicate-name` | a variable is bound twice in a test |
| `test.undeclared-error` | `@@expectThrows` names an error that no call in the statement declares |
| `test.nullable` | a nullable value is used where it may not be (see [`null`](#null)) |
| `test.incomparable` | `==`/`!=` on incompatible types |
| `test.out-of-range` | a constant value does not fit its type |
| `test.ambiguous-call` | an unqualified call fits a built-in function and a function of the spec's namespaces |
| `test.empty` (WARNING) | a test calls no assertion function and has no `@@expectThrows` |

## Translation into the languages

What every generator has to guarantee:

- **Determinism.** The same specs and the same generator produce byte-identical test files. Tests are emitted in the
  order of the spec; nothing depends on hash order, time or environment.
- **One generated test per spec test.** The generated test carries the spec test's id
  (as display name, doc comment or tag), so a failure can be traced back to the spec.
- **The semantics above, not the target's.** Where the target language behaves differently, the generated code calls
  a helper of the language's **test support module** instead of inlining a workaround: code-point string functions,
  exact integer arithmetic with range checks, value equality of sets and of Go structs. These modules are written by
  hand, once per language, and the generated tests depend on them — the same arrangement as the support modules of
  the API ([ADR-0007](../docs/adr/0007-separate-generated-and-hand-written-modules.md)).
- **Assertions map onto the test framework.** `assertEquals` becomes `assertEquals` in JUnit,
  `assert.deepStrictEqual` in `node:test`, `assert_eq!` in Rust, a comparison with `t.Errorf` in Go, `assert a == b`
  in pytest — through a support-module helper where the native equality differs from [Equality](#equality).
- **Idiomatic output.** Where the target's native behaviour already equals the defined semantics, the generated code
  uses it directly (`assertEquals` on Java records, `assert_eq!` on Rust structs, `==` on Python dataclasses), so that
  most generated tests read like hand-written ones.

How each construct is written in a language — test framework, assertions, async tests, the name of the support module
— is part of each language's `api-best-practices-<language>.md`, like the mapping of the API.

## Example

An excerpt of `spec/base/keys.md` with a `## Testing` section. The tests use `DEFAULT(PrivateKey)` and
`DEFAULT(PublicKey)`, which are not test-language constructs of their own: they are the default instances that the same
spec declares in its `## Default Instances for Testing` section, shown here for context.

Every expected value comes from the spec itself: the raw bytes of the default key are those of its default instance,
and the PKCS#8 DER encoding is the one listed under "Key examples" in `keys.md`. A value the spec does not state — the
public key derived from the private key, a signature — is not written as a literal; the tests check the properties the
API promises instead (a signature verifies, a key survives a round trip).

````markdown
## Default Instances for Testing

```
// An ED25519 key with known bytes, so that tests can check encodings against fixed values.
instance PrivateKey = createPrivateKey(algorithm: KeyAlgorithm.ED25519, encoding: ByteImportEncoding.HEX,
        value: "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c")

// The public key of the default private key.
instance PublicKey = DEFAULT(PrivateKey).createPublicKey()
```

## Testing

```tests
test "the default private key has its raw bytes" {
    let key = DEFAULT(PrivateKey)
    assertEquals(expected: hex(text: "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c"),
            actual: key.bytes)
    assertEquals(expected: KeyAlgorithm.ED25519, actual: key.algorithm)
    assertEquals(expected: KeyType.PRIVATE, actual: key.type)
}

test "the default private key encodes as PKCS#8 DER" {
    assertEquals(
            expected: hex(text: "302e020100300506032b657004220420d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c"),
            actual: DEFAULT(PrivateKey).toBytes(container: KeyFormat.PKCS8_WITH_DER))
}

test "the public key of an Ed25519 key is a public Ed25519 key of 32 bytes" {
    let publicKey = DEFAULT(PrivateKey).createPublicKey()
    assertEquals(expected: KeyType.PUBLIC, actual: publicKey.type)
    assertEquals(expected: KeyAlgorithm.ED25519, actual: publicKey.algorithm)
    assertEquals(expected: 32, actual: length(value: publicKey.bytes))
    assertEquals(expected: publicKey, actual: createPublicKey(algorithm: KeyAlgorithm.ED25519, rawBytes: publicKey.bytes))
}

test "a signature verifies with the public key and only for its message" {
    let signature = DEFAULT(PrivateKey).sign(message: utf8(text: "hello"))
    assertEquals(expected: 64, actual: length(value: signature))
    assertEquals(expected: signature, actual: DEFAULT(PrivateKey).sign(message: utf8(text: "hello")),
            message: "Ed25519 signatures are deterministic")
    assertTrue(condition: DEFAULT(PublicKey).verify(message: utf8(text: "hello"), signature: signature))
    assertFalse(condition: DEFAULT(PublicKey).verify(message: utf8(text: "hello!"), signature: signature))
}

test "a key survives every format that supports its type" {
    let key = DEFAULT(PrivateKey)
    for format in [KeyFormat.PKCS8_WITH_DER, KeyFormat.SPKI_WITH_DER, KeyFormat.PKCS8_WITH_PEM,
            KeyFormat.SPKI_WITH_PEM] {
        if not(value: format.supportsType(type: KeyType.PRIVATE)) {
            @@expectThrows(illegal-format)
            key.toBytes(container: format)
        } else if format.encoding == KeyEncoding.DER {
            assertEquals(expected: key, actual: createPrivateKey(container: format, value: key.toBytes(container: format)))
        } else {
            let pem = key.toString(container: format)
            assertTrue(condition: startsWith(value: pem, prefix: "-----BEGIN PRIVATE KEY-----"))
            assertEquals(expected: key, actual: createPrivateKey(container: format, value: pem))
        }
    }
}

test "a hex key of the wrong length is rejected" {
    for hexText in ["d3671a1e", repeat(value: "ab", count: 33)] {
        @@expectThrows(illegal-format)
        createPrivateKey(algorithm: KeyAlgorithm.ED25519, encoding: ByteImportEncoding.HEX, value: hexText)
    }
}
```
````

## Questions & Comments

- **Table-driven tests.** A test repeated over rows of values, each row its own generated test case (a parameterized
  test, a subtest), is postponed. Until then a `for` loop inside a test covers several values, but stops at the first
  failing one. — open
- **Mutable variables.** Without assignment a loop cannot accumulate (a sum, a counter). The built-in functions cover
  the common cases; if real tests need more, a `var` with assignment would be the next step — at the cost of
  closures over mutable state in some languages. — open
- **Streaming.** How a test consumes a `@@streaming` result is open: a built-in function cannot take one, because a
  stream is no type of the meta-language but an annotation of a method. A language construct (e.g. "the first n
  items, with a timeout") would be needed. — open
- **Integer parameters of the built-in functions.** The meta-language has no integer type of unlimited precision, so
  the declarations use `int64` for positions and `int256` for arithmetic; a `uint256` value above the `int256` range
  cannot be passed to `abs`, `min` or `max`. — open
- **Verbosity of named arguments.** `hex(text: "…")` and `length(value: s)` are longer than positional calls, in
  exchange for one calling convention for API and built-in functions. — open
- **Date and time literals.** There is no literal for `date`, `dateTime`, `duration` yet; candidates are ISO 8601
  strings with a built-in (`date(text: "2026-10-15")`, `duration(text: "PT1.5S")`). — open
- **Equality of complex types.** "All attributes are equal" presumes that every generated type implements value
  equality. Abstractions without a sealed set of subtypes, and types whose API defines its own `equals`, need a
  decision. — open
- **Relation to `## Examples`.** Examples written as tests would be checked instead of drifting; they would lose their
  narrative form. — open
- **The SPKI examples in `spec/base/keys.md` do not belong to the default key.** The SPKI DER and PEM examples show a
  different key than the one derived from the default private key, and the DER example is three bytes short (41
  instead of 44). Once fixed, they could serve as the expected public key in the tests. — open, to be fixed in
  `keys.md`
- **Where do known answers come from?** A literal expected value needs a source a reviewer can check: the spec itself
  (as above), or a published test vector (RFC 8032 for Ed25519, for example). Values computed with a tool by the
  author are not reviewable. Should the guideline require a source comment for every known-answer literal? — open
