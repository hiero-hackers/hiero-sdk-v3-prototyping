# JavaScript API Implementation Guideline

This document translates the [language-agnostic API meta-definition](api-guideline.md) into JavaScript.

JavaScript and TypeScript share one runtime and, today, one SDK: the generated workspace `generated/ts` is written
in TypeScript and published as JavaScript (`dist/*.js`) with type declarations next to it (`dist/*.d.ts`). **A
JavaScript caller uses exactly those objects**, so this guideline and the
[TypeScript guideline](api-best-practices-ts.md) must agree on everything the runtime can observe. Where a rule is
identical, this document states it briefly and links there; where JavaScript needs something TypeScript gets from
the compiler, it is spelled out here.

Every rule below can be checked against the published package. The examples in this document were run against
`generated/ts/packages/base/dist`.

## Scope

Two kinds of JavaScript are in scope:

1. **Calling the SDK from JavaScript.** What the API looks like without the type layer, and which of its guarantees
   are enforced at runtime rather than by the compiler.
2. **Writing a plain-JavaScript implementation** of the same specs, should one ever be needed. The rules below
   produce an API that is indistinguishable, from a caller's side, from the generated one.

What is *not* in scope: a JavaScript dialect of the API with different names or shapes. The meta-language fixes the
API surface, and two implementations of the same spec in the same runtime must not differ in it.

## Runtime compatibility

Target current, widely used runtimes: modern evergreen browsers and the current active Node.js LTS (22). Very old
runtimes can be ignored. This guideline assumes ES2022: `class` fields, `#private` fields, `Object.hasOwn`,
top-level `await` in modules, and `fetch` in the runtime. If `fetch` is missing, inject a fetch-like client at the
application layer rather than polyfilling inside the SDK.

The SDK is distributed as **ES modules** (`"type": "module"`). CommonJS is not supported.

## Modules, namespaces and the public API

A namespace is a **directory** with an `index.js` that re-exports everything of the namespace, not a single flat
file. This mirrors the published package, where a namespace is a subpath export:

```javascript
import { AccountId, ConsensusNode } from "@hiero/base/ledger";
import { KeyFormat } from "@hiero/base/keys";
```

| File | Content |
|---|---|
| `<Type>.js` | one class per file, named after it |
| `functions.js` | the namespace-level functions |
| `constants.js` | the namespace-level constants |
| `errors.js` | the `Error` subclasses of the namespace |
| `index.js` | re-exports the namespace |

The `exports` map of the `package.json` is the public API boundary: a path that is not listed cannot be imported,
even though the file exists. `internal/…` is deliberately absent — that is how the protobuf code is kept out of the
API. See [What is public API](api-best-practices-ts.md#what-is-public-api).

Relative imports inside a package always carry the `.js` extension, which ES module resolution requires:

```javascript
import { AccountId } from "../ledger/AccountId.js";
```

Shared helpers that no spec declares (validation, see below) live in one base module, e.g.
[`lang-base.js`](js-files/lang-base.js).

## Type mapping

JavaScript has no type annotations, so this table says which **runtime value** carries a meta-language type. It is
the same mapping as [the TypeScript one](api-best-practices-ts.md#type-mapping) with the static types erased.

| Meta-language | JavaScript value | Notes |
|---|---|---|
| `string` | `string` | |
| `int8` … `int32`, `uint8` … `uint32` | `number` | an integer, range-checked |
| `int64`, `uint64` and wider | `bigint` | `5n`; the rule is *more than 32 bits* |
| `double` | `number` | |
| `decimal` | `string` | a decimal number as text — JavaScript has no exact decimal type, and a third-party one would become part of the API |
| `bool` | `boolean` | |
| `bytes` | `Uint8Array` | copied in and out |
| `list<T>` | frozen `Array` | `Object.freeze([...value])` |
| `set<T>` | `Set` | copied in and out |
| `map<K, V>` | `Map` | copied in and out; **keyed by identity**, see [Identity and equality](#identity-and-equality) |
| `date`, `time`, `dateTime`, `zonedDateTime` | `Date` | copied in and out; `Temporal` once it is available everywhere |
| `duration`, `seconds` | `Duration` | support class from `@hiero/support` (immutable, milliseconds) |
| `uuid` | `string` | |
| `type<T>` | the class object | e.g. `AccountCreateTransaction`; `String`, `Number`, `Boolean`, `BigInt` for the primitives |
| `ANY` | any value | the caller has to know what it is; document it |
| `function<R name(p: T)>` | a function | |
| `streamResult<T>` | `StreamItem` | `{ ok: true, value }` or `{ ok: false, error }` |

The four open questions the earlier version of this table carried are answered by the TypeScript mapping, because
both produce the same runtime values: `decimal` is a string, a `list` is a frozen array, `date` and `zonedDateTime`
are both `Date`, and the integer split is at 32 bits.

### Numeric types

- `number` is exact only up to 2^53. Everything wider than 32 bits is therefore a `bigint`, and the two do not mix:
  `1n + 1` throws a `TypeError`.
- A `number` that stands for an integer type must be checked with `Number.isInteger` **and** against the range of
  the type; a `bigint` only against the range.
- `bigint` literals carry the `n` suffix (`0n`, `1001n`). A caller coming from JSON has to convert explicitly —
  `JSON.parse` never produces a `bigint`.

```javascript
if (!Number.isInteger(port) || port < 0 || port > 65535) {
    throw new RangeError("port must be an integer between 0 and 65535");
}
```

### Function types

A function type is a plain function. There is nothing to declare; document the shape in JSDoc:

```javascript
/**
 * @param {function(Event): void} callback - called for every event
 */
subscribe(callback) { /* ... */ }
```

### Runtime type information

`type<T>` is the class object itself — in JavaScript a class is a value and can be passed and compared:

```javascript
const response = getResponse(transactionId, AccountCreateTransaction, client);
```

For the primitives the wrapper constructor stands in (`type<string>` → `String`), because that is the only value
JavaScript has for them.

## Defensive implementation

JavaScript has no compiler to reject a wrong call, so **every value that crosses the public API boundary is checked
at runtime**. This is not optional hardening; it is the only line of defence the API has:

- **`null` and `undefined`** are rejected for every non-nullable value, with a `TypeError`.
- **Types** are checked where a wrong type would corrupt state silently (`typeof value !== "string"`).
- **Integer ranges and the validation annotations** are checked, with a `RangeError`.
- **`bytes`, sets, maps and dates are copied** in and out; arrays are frozen. A caller must not reach the state of a
  value object through a reference it kept or received.
- **Instances are frozen** so that a caller cannot add or replace properties.

What it does *not* mean: re-validating inside the implementation where the invariant is already established, or
catching and swallowing errors. An error from the SDK is a bug in the calling code and must reach it.

### Validation helpers

The checks are repetitive, so they live in one module, [`js-files/lang-base.js`](js-files/lang-base.js), and every
namespace imports what it needs:

```javascript
import { requireNonNull, requireNonNullString, requireNullableString } from "./lang-base.js";
```

| Helper | Accepts |
|---|---|
| `requireDefined(value, name)` | anything but `undefined` |
| `requireNonNull(value, name)` | anything but `null` and `undefined` |
| `requireNonNull<Type>(value, name)` | that type, and nothing else |
| `requireNullable<Type>(value, name)` | that type or `null` |

Each throws a `TypeError` whose message names the parameter, and each returns the validated value so it can be used
in an assignment:

```javascript
this.#name = requireNonNullString(name, "name");
```

## Null and undefined handling

JavaScript has two absent values and the meta-language has one. The rules:

- **`null` is the absent value of the API.** A method never returns `undefined`, and `@@nullable` means "may be
  `null`".
- **`undefined` is rejected** wherever a value is expected — for non-nullable *and* nullable parameters alike. It
  almost always means a typo or a forgotten argument, and silently treating it as `null` would hide that.
- **The one exception is an omitted value**, and it is deliberate: in the init object of a constructor (see
  [Objects](#objects)) and for a parameter with `@@default`, a property that is absent — which reads as `undefined` —
  means "not given". It becomes `null` for a `@@nullable` attribute and the default value for `@@default`. This is
  what the generated constructors do, and a JavaScript caller can rely on it:

```javascript
// verified against the published package
new AccountId({ shard: 0n, realm: 0n, checksum: "", num: 1001n }).evmAddress   // null, the property was omitted
new AccountId({ shard: 0n, realm: 0n, checksum: "", num: 1n, alias: undefined }).alias   // null, same thing
new AccountId({ shard: 0n, realm: 0n, checksum: "", num: undefined })          // TypeError: num must not be null
```

- **Collections are never `null`** because they are empty — return an empty array, `Set` or `Map`. To clear a
  collection, provide a `clear<Name>()` method rather than accepting `null` as a signal.
- **Non-nullable returns are never `null`.** If the value cannot be produced, throw.
- **Document nullability in JSDoc**: `@param {string | null}`, `@returns {string | null}`.

## Objects

A complex type is a `class` with `#private` fields, a constructor that takes **one init object**, and property
accessors:

```javascript
export class ConsensusNode {

    #ip;
    #port;
    #account;

    /**
     * Creates a ConsensusNode.
     *
     * @param {{ ip: IpAddress, port: number, account: AccountId }} init - the attributes
     * @throws {TypeError} if a required attribute is null or undefined
     * @throws {RangeError} if an attribute violates its constraints
     */
    constructor(init) {
        requireNonNull(init, "init");
        this.#ip = requireNonNull(init.ip, "ip");
        const port = requireNonNullNumber(init.port, "port");
        if (!Number.isInteger(port) || port < 0 || port > 65535) {
            throw new RangeError("port must be an integer between 0 and 65535");
        }
        this.#port = port;
        this.#account = requireNonNull(init.account, "account");
        Object.freeze(this);
    }

    /** @returns {number} the port of the node */
    get port() {
        return this.#port;
    }
}

const node = new ConsensusNode({ ip, port: 50211, account });
```

The init object is named, order-independent and allows omissions, which is why neither JavaScript nor TypeScript
needs the builder pattern that the Java guideline describes.

### Accessors

**An attribute is read through a property getter named after the attribute** — `node.port` — not through
`getPort()`. This is not a style preference: it is what the published package provides, and a `getPort()` does not
exist there.

```javascript
node.port          // 50211
node.getPort       // undefined
```

A mutable attribute (one without `@@immutable`) gets a property **setter** that repeats the checks of the
constructor:

```javascript
get memo() {
    return this.#memo;
}

set memo(value) {
    this.#memo = requireNullableString(value, "memo");
}
```

A setter returns nothing — an assignment is not an expression that can be chained, so the self type of the
meta-language (`$$Self`), which exists so that Java setters can chain, has no role in JavaScript.

Use `#private` fields for all internal state. Do not use an underscore prefix (`_field`): it is a convention, not a
boundary, and `#` is enforced by the language.

### Freezing

Call `Object.freeze(this)` at the end of the constructor of a class that **has no subtypes**. A class that is
extended does not freeze — freezing happens once, when the instance is complete, and only the most derived
constructor knows when that is.

Be precise about what it buys:

| | frozen instance |
|---|---|
| adding a property (`obj.extra = 1`) | `TypeError` |
| replacing a public own property | `TypeError` |
| assigning to a getter-only property | `TypeError` |
| writing a `#private` field from inside the class | **works** — `Object.freeze` does not cover private fields |
| mutating an object a field points to | works — `freeze` is shallow |
| a `Set` or `Map` held in a field | **not frozen** — `Object.freeze` does nothing to their contents |

So freezing is tamper resistance against callers, not the mechanism behind `@@immutable`. Immutability comes from
the fields being `#private` without a setter, and from copying. It is also why a class *with* setters is still
frozen: its setters keep working.

### Copying

| Meta-language type | On the way in | On the way out |
|---|---|---|
| `bytes` | `value.slice()` | `value.slice()` |
| `list<T>` | `Object.freeze([...value])` | the stored array |
| `set<T>` | `new Set(value)` | `new Set(value)` |
| `map<K, V>` | `new Map(value)` | `new Map(value)` |
| `date` … | `new Date(value.getTime())` | `new Date(value.getTime())` |
| everything else | stored as given | returned as held |

An array is the only one copied on one side only, because the stored one is frozen. `Uint8Array`, `Set`, `Map` and
`Date` have no frozen form — `Object.freeze` on a typed array with elements even throws — so they are copied on
both sides. A nullable value is copied only when it is not `null`.

### Inheritance and nullability narrowing

A subtype that narrows an inherited `@@nullable` attribute (`@@override`) enforces the narrower contract in its own
constructor and documents it on its getter. The storage stays in the parent; the child does not shadow it:

```javascript
// Parent — the getter contract allows null.
class Identifier {
    #num;

    /** @param {bigint | null} num */
    constructor(num) {
        this.#num = requireNullableBigInt(num, "num");
    }

    /** @returns {bigint | null} the numeric id, or null if not assigned */
    get num() {
        return this.#num;
    }
}

// Child — narrowed: num is guaranteed non-null on every instance.
class NumericIdentifier extends Identifier {

    /** @param {bigint} num */
    constructor(num) {
        super(requireNonNullBigInt(num, "num"));
        Object.freeze(this);
    }

    /** @returns {bigint} the numeric id (never null on this type) */
    get num() {
        return super.num;
    }
}
```

Rules:

1. JSDoc the child's getter with the non-null type (`{bigint}`, not `{bigint | null}`).
2. Enforce the narrowing in the child's constructor **before** delegating to `super(...)`. The parent cannot know
   about the stricter contract.
3. Do not re-declare the field in the child. The narrowing is a contract on the getter, not new storage.
4. Narrowing is scoped to nullability. Do not invent narrowings of `@@max`, `@@maxLength` or other constraints —
   the meta-language does not have them.

## Attribute annotations

| Annotation | Runtime check | Thrown |
|---|---|---|
| *(not nullable)* | `value === null \|\| value === undefined` | `TypeError` |
| `@@nullable` | `undefined` rejected, `null` allowed; all other checks guarded by `value !== null` | `TypeError` |
| *(integer type)* | the range of the type, plus `Number.isInteger` for a `number` | `RangeError` |
| `@@default(v)` | an omitted value becomes `v` | — |
| `@@min(n)` / `@@max(n)` | `value < n` / `value > n` | `RangeError` |
| `@@minLength(n)` / `@@maxLength(n)` | `value.length` (strings) | `RangeError` |
| `@@minSize(n)` / `@@maxSize(n)` | the size of an array, `Set`, `Map` or `Uint8Array` | `RangeError` |
| `@@pattern(re)` | `!new RegExp(re).test(value)` | `RangeError` |
| `@@urlPattern` | the `URL` constructor, see below | `RangeError` |
| `@@immutable` | no setter | — |
| `@@deprecated` | a JSDoc `@deprecated` tag | — |

The checks belong in the constructor **and** in the setter. Calling the setter from the constructor is the simplest
way not to write them twice.

**`@@urlPattern` is not a regular expression.** Delegate to the platform's URL parser, so the SDK inherits WHATWG
parsing instead of a hand-written expression:

```javascript
function isAbsoluteUrl(value) {
    try {
        return new URL(value).host !== "";
    } catch {
        return false;
    }
}
```

`new URL(value)` throws for a relative URL, and the `host` check rejects the absolute forms that have none (`file:`,
`data:`).

## Collections

- A collection in the public API is an `Array`, a `Set` or a `Map` — never a custom collection type.
- **A returned array is frozen**; a returned `Set`, `Map` or `Date` is a copy. JavaScript has no read-only view of a
  `Set` or a `Map`, so a copy is the only honest option.
- A collection is **never `null`** because it is empty.
- Elements are **never `null`**: the meta-language has no nullable element type.
- A collection passed in is copied before it is stored.
- A mutable collection attribute replaces its *content*, never the array object, if anything else may hold a
  reference to it.

There is no concurrency dimension: JavaScript runs an object on one thread, so the Java guideline's
`CopyOnWriteArrayList` has no counterpart and there is no concurrent modification to defend against.

## Abstractions

JavaScript has no interfaces. An abstraction is therefore **a documented contract, not a declaration**:

- Document it with JSDoc (`@interface`, and `@implements` on the implementing classes) so that editors and
  type-checkers can see it.
- Do not generate an empty base class just to have something to extend. A caller never needs `instanceof` against
  an abstraction — duck typing is how JavaScript satisfies a contract.
- `@@static` methods of an abstraction become functions exported under the name of the abstraction, so the call
  reads the same as in TypeScript: `TransactionId.generateTransactionId(accountId)`.
- A **sealed** abstraction (`@@sealed(A, B)`) is the set of its permitted classes. Distinguish them with
  `instanceof`, and end the chain with a `throw` rather than a silent fallthrough — JavaScript cannot check
  exhaustiveness:

```javascript
function describe(authority) {
    if (authority instanceof PublicKeyAuthority) { return "key"; }
    if (authority instanceof ContractAuthority) { return "contract"; }
    if (authority instanceof AuthorityList) { return "list"; }
    throw new TypeError(`Unknown Authority: ${authority}`);
}
```

Generic type parameters have no runtime form. Document them with JSDoc `@template` where it helps a type-checker;
there is nothing to enforce.

## Enumerations

An enum is a class with one `static` instance per value — the same shape the TypeScript generator produces, so that
both present the same values:

```javascript
export class KeyFormat {

    static PKCS8_WITH_DER = new KeyFormat("PKCS8_WITH_DER", KeyContainer.PKCS8, KeyEncoding.DER);
    static SPKI_WITH_DER = new KeyFormat("SPKI_WITH_DER", KeyContainer.SPKI, KeyEncoding.DER);

    #name;

    constructor(name /* , attributes */) {
        this.#name = name;
        Object.freeze(this);
    }

    /** @returns {string} the name of the constant */
    get name() {
        return this.#name;
    }

    /** @returns {KeyFormat[]} all constants in declaration order */
    static values() {
        return Object.freeze([KeyFormat.PKCS8_WITH_DER, KeyFormat.SPKI_WITH_DER]);
    }

    /**
     * @param {string} name
     * @returns {KeyFormat} the constant with that name
     * @throws {RangeError} if there is none
     */
    static valueOf(name) {
        const value = KeyFormat.values().find(v => v.name === name);
        if (value === undefined) {
            throw new RangeError(`No constant KeyFormat.${name}`);
        }
        return value;
    }

    toString() {
        return this.#name;
    }
}
```

- `toString()` returns the name, so a value reads correctly in a template string and in a log:
  `` `${KeyFormat.PKCS8_WITH_DER}` `` is `"PKCS8_WITH_DER"`.
- Comparison is by identity (`format === KeyFormat.SPKI_WITH_DER`), which is correct: there is exactly one instance
  per value.
- `Object.freeze(KeyFormat)` after the class body **does** seal the set: the static fields are initialised before
  it runs, and a later `KeyFormat.CUSTOM = …` then throws a `TypeError` (modules are strict mode). The generated
  TypeScript does not do this — the `private constructor` already closes the set for anyone using the types — so a
  hand-written implementation should not do it either, to keep both forms identical.

## Methods and functions

- Namespace-level functions are exported functions of the namespace module. There is no factory class — a module is
  already the namespace.
- **JavaScript has no overloading.** Where the specs declare several methods with the same name, there is one
  function that dispatches on the arguments it actually received. Dispatch on **arity first, then type**, and reject
  anything that matches no overload:

```javascript
/**
 * @param {KeyFormat | string} formatOrValue
 * @param {string} [value]
 * @returns {PrivateKey}
 */
export function createPrivateKey(formatOrValue, value) {
    if (arguments.length === 1 && typeof formatOrValue === "string") {
        return fromDer(formatOrValue);
    }
    if (arguments.length === 2 && formatOrValue instanceof KeyFormat) {
        return fromFormat(formatOrValue, requireNonNullString(value, "value"));
    }
    throw new TypeError("createPrivateKey(value) or createPrivateKey(format, value)");
}
```

  Do not use a default parameter value to fake an overload when the overloads differ in meaning — a caller who
  passes `undefined` by accident would silently get the wrong one.

## Asynchronous methods

An `@@async` method returns a `Promise`. Errors reject the promise; they are not returned.

```javascript
/**
 * @param {HttpRequest} request
 * @returns {Promise<HttpResponse>} rejects with ConnectionError or TimeoutError
 */
execute(request) { /* ... */ }
```

There is **no synchronous alternative**. JavaScript cannot block, so an asynchronous operation is asynchronous for
every caller. A `Promise` also cannot be cancelled — see [Questions & Comments](#questions--comments).

An async method must never *also* throw synchronously: validate inside the async function (or return
`Promise.reject(...)`), so a caller only has to handle one failure path.

## Streaming

A `@@streaming` method returns an **async iterable**. The consumer uses `for await`, and leaving the loop cancels
the stream:

```javascript
for await (const item of client.subscribeTopic(topicId)) {
    if (!item.ok) {
        console.warn("bad message", item.error);
        continue;
    }
    use(item.value);
}
```

- **Per-item errors** use `streamResult<T>` → a `StreamItem`, the tagged object `{ ok: true, value }` /
  `{ ok: false, error }`. The stream continues after such an item.
- **Stream-level errors** make the `for await` throw, and the iteration ends.
- **Cancellation** is `break`, `return` or a `throw` out of the loop. All three call the iterator's `return()`,
  which is where an implementation releases the connection — so implement `return()`, not only `next()`. (Verified:
  all three paths invoke it.)
- Backpressure is built in: the producer only advances when the consumer asks for the next item.

An async generator (`async function*`) gets `return()` for free and is the simplest correct implementation.

## Errors

- A value outside its domain is a **`RangeError`**; this is what `@@throws(illegal-format)` and
  `invalid-argument-error` map to, and what the attribute checks throw.
- A missing or wrongly typed value is a **`TypeError`**.
- Every other error identifier is an `Error` subclass in the `errors.js` of the namespace that declares it —
  `not-found-error` → `NotFoundError`:

```javascript
export class PaginationError extends Error {

    /**
     * @param {string} message - the description of the problem
     * @param {ErrorOptions} [options] - the cause of the problem, if any
     */
    constructor(message, options) {
        super(message, options);
        this.name = "PaginationError";
    }
}
```

  Setting `name` matters: it is what appears in a stack trace and in `console.log`. `options.cause` is how an error
  chain is built — use it instead of concatenating messages.
- Never throw a bare string or a plain object; only `Error` instances carry a stack.
- Document errors in JSDoc with `@throws`, and for an async method say with which error the promise rejects.

## Constants

Constants are exported `const` bindings of the namespace module, in `constants.js`:

```javascript
export const ZERO_ADDRESS = new Address({ shard: 0n, realm: 0n, checksum: "", num: 0n });
```

A constant whose value is a struct literal is a constructor call with all attributes written out; the ones the
literal omits are passed as `null` or get their `@@default`. The constant needs no `Object.freeze` — the value
object freezes itself.

## Identity and equality

**Value objects have no value equality.** Two instances built from the same attributes are different objects, and
JavaScript compares objects by identity:

```javascript
const a = new AccountId({ shard: 0n, realm: 0n, checksum: "", num: 1001n });
const b = new AccountId({ shard: 0n, realm: 0n, checksum: "", num: 1001n });

a === b                      // false
new Set([a, b]).size         // 2
new Map([[a, "x"]]).get(b)   // undefined
```

Three consequences:

- A `map<K, V>` and a `set<T>` over a value type are **keyed by identity**. A lookup only finds the entry if the
  caller passes the very object that was put in. This differs from Java and Rust, where the same spec gives value
  semantics.
- **Deep-equality assertions do not work** on these objects. `#private` fields are not own properties — 
  `Object.getOwnPropertyNames(node)` is `[]` — so a structural comparison sees two empty objects and reports
  *different* values as equal. Compare with an `equals` method where one exists, otherwise by identity or by a
  string form.
- The spec of [`authority.md`](../spec/base/authority.md) requires `Authority` to be a value type with structural
  equality. JavaScript does not provide that today.

Where a type needs value equality, give it an `equals(other)` that checks `instanceof` and then compares attribute
by attribute. How this should be solved across the SDK is open — see [Questions & Comments](#questions--comments).

## Debug representation

The JavaScript defaults show nothing for these objects, because the state is in `#private` fields:

```javascript
String(node)          // "[object Object]"
JSON.stringify(node)  // "{}"
util.inspect(node)    // "ConsensusNode {}"
```

A type that the specs give a `toString()` prints properly (`` `${accountId}` `` is `"0.0.1001"`), and every enum
has one. For everything else, printing an SDK object in a log yields nothing useful.

When adding a `toString()`, follow the rules of the [Java guideline](api-best-practices-java.md#implementing-tostring):
a stable `ClassName[a=1, b=2]` form, **never** key material or other sensitive data, and for a byte array only its
length (`value=byte[32]`) — a string representation ends up in log files.

Do not implement `toJSON()` to expose internal state: it would make the wire shape of the object part of the API.

## Documentation and deprecation

- Public API documentation is **JSDoc**, written for the users of the API. It never refers to the specs, the
  meta-language or an ADR.
- JSDoc carries the type information that the language does not: `@param {string | null}`, `@returns`, `@throws`,
  `@template`. An editor and `tsc --checkJs` read it, so it is worth keeping accurate.
- `@@deprecated` becomes the JSDoc `@deprecated` tag, on **every** element that exposes the deprecated attribute —
  the getter and, if mutable, the setter. Its text is the explanation and the replacement from the spec prose; if
  the spec does not explain it, write "Retained for compatibility; do not use it in new code."
- Deprecation is documentation and tooling metadata. It must never add a runtime warning or change behaviour.

```javascript
class Client {
    /**
     * @deprecated Use connect() instead.
     */
    connectLegacy() { /* ... */ }
}
```

## Thread safety

JavaScript runs an object on one thread — workers do not share objects, they pass copies — so `@@threadSafe` has no
mapping and nothing to implement.

Note that *single-threaded* is not the same as *free of interleaving*: an `await` is a suspension point, and state
can change between two `await`s in the same function. A method that reads state, awaits, and then writes it back
has to re-read, exactly as it would in any other language.

## What JavaScript does not need

| Java | JavaScript |
|---|---|
| the `final` keyword | `#private` fields, getters without setters, `Object.freeze` |
| `equals`/`hashCode` | nothing at the language level — see [Identity and equality](#identity-and-equality) |
| the builder pattern | the init object of the constructor |
| factory classes per namespace | a module is the namespace |
| interfaces | a documented contract and duck typing |
| generics | no runtime form; JSDoc `@template` for the tooling |
| checked exceptions | every error is unchecked |
| `CopyOnWriteArrayList` and friends | single-threaded; no concurrent collections |
| Lombok | nothing comparable exists |

Also avoid, specifically in this codebase: `var` (use `const`, and `let` only where a rebinding is needed),
underscore-prefixed "private" fields, `==` (always `===`), and adding properties to objects the SDK did not create.

## Testing

Tests use the Node.js test runner (`node:test`, `node:assert`), one test per behaviour, structured given / when /
then. For every public element, cover: the value round-trip, `null` and `undefined` rejection, the boundaries of
every range and validation annotation, that copies are copies, that setters validate, and the error cases next to
the happy path.

Because value objects have no equality, do not compare them with a deep-equality assertion (see
[Identity and equality](#identity-and-equality)). Compare with `equals` where it exists, by identity otherwise, or
attribute by attribute.

The external [Hiero SDK TCK](https://github.com/hiero-ledger/hiero-sdk-tck) runs against the SDK as a whole and is
the cross-SDK consistency check; it does not replace unit tests.

## Questions & Comments

- **Is a separate plain-JavaScript SDK intended at all?** Today there is one JS/TS SDK: TypeScript sources,
  published as JavaScript. This guideline is written so that both readings work — rules for calling the generated
  package from JavaScript, and rules for a hand-written implementation of the same specs. If a separate SDK is
  never going to exist, the second half could be dropped and this document would become a consumer guide. — open

- **Value equality is unresolved.** `spec/base/authority.md` requires structural equality for `Authority`, and
  nothing provides it. Whatever is decided for
  [TypeScript](api-best-practices-ts.md#questions--comments) applies here unchanged, since it is the same runtime
  and the same objects. — open

- **No `toString()` on complex types.** Enums have one and some spec types declare one; the rest print as
  `[object Object]`. Same question as in the TypeScript guideline. — open

- **Cancellation of an `@@async` method.** A `Promise` cannot be cancelled. `AbortSignal` is the platform answer,
  but the specs do not model it. Streaming already has an answer (`break` and the iterator's `return()`). — open

- **The validation helpers reject `undefined` everywhere.** `requireNullableString` and its siblings throw for
  `undefined` even for a `@@nullable` value, which is right for a positional parameter but not for an omitted
  property of an init object. A helper for that case (`optional(init.num, null)`) would make the distinction
  explicit instead of leaving it to each constructor. — open
