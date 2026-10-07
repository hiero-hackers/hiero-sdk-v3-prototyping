# TypeScript API Implementation Guideline

This document translates the [language-agnostic API meta-definition](api-guideline.md) into TypeScript. The
generator of the spec tooling (`tooling/metalang`, `metalang generate --language=ts`) implements exactly these rules;
the generated workspace `generated/ts` is the reference. For plain JavaScript see the
[JavaScript guideline](api-best-practices-js.md).

Every rule below describes what the generator produces today and can be checked against `generated/ts`. Where a
question is still open, it is listed under [Questions & Comments](#questions--comments) rather than answered by
invention.

## Runtime and tooling

- Target: ES2022 modules (`"type": "module"`), current Node.js LTS (22) and evergreen browsers. The API itself uses no
  Node.js APIs.
- TypeScript 6 with a strict configuration: `strict`, `exactOptionalPropertyTypes`, `noUncheckedIndexedAccess`,
  `noImplicitReturns`, `noUnusedLocals`, `noFallthroughCasesInSwitch`, `verbatimModuleSyntax` (type-only imports use
  `import type`), `isolatedModules`, `declaration`, `composite`. The generator writes this as `tsconfig.base.json`
  of the workspace; every package extends it.
- One npm package per spec folder (`@hiero/base`, `@hiero/consensus-node-client`, ...) in one npm workspace; the
  packages depend on the packages of the namespaces they use and are built with project references
  (`tsc --build`).

## Packages, namespaces and the public API

### Layout

One directory per namespace (`src/consensusnode/transactions`), one file per type, plus the files that hold the
other declarations of the namespace:

| File | Content |
|---|---|
| `<Type>.ts` | one complex type, abstraction or enum per file, named after it |
| `functions.ts` | the namespace-level functions (`@@static` functions of the namespace) |
| `constants.ts` | the namespace-level constants |
| `errors.ts` | the `Error` subclasses of the error identifiers declared in the namespace |
| `index.ts` | re-exports everything of the namespace; carries the `## Description` of the spec as its module comment |
| `<Type>.test.ts` | the generated tests, next to the type they test |

A file exists only when the namespace has something to put in it.

### Namespace to subpath

Every namespace is a **subpath export** of its package, with the namespace path as the subpath:

```ts
import { AccountId, TransactionId } from "@hiero/base/ledger";
import { createClient } from "@hiero/consensus-node-client/consensusnode/client";
```

A sub-namespace is a sub-subpath (`ledger.config` → `@hiero/base/ledger/config`). Inside a package, a namespace
imports another namespace of the same package by relative path (`../ledger/AccountId.js`, always with the `.js`
extension that `NodeNext` resolution requires); across packages it imports the subpath (`@hiero/base/ledger`).

### What is public API

The `exports` map of the `package.json` **is** the public API boundary — the TypeScript counterpart of the JPMS
`module-info.java` of the Java SDK. A path that is not listed cannot be imported by a consumer, no matter that the
file exists in `dist`:

```json
"exports": {
  "./ledger":        { "types": "./dist/ledger/index.d.ts",        "default": "./dist/ledger/index.js" },
  "./ledger/config": { "types": "./dist/ledger/config/index.d.ts", "default": "./dist/ledger/config/index.js" }
}
```

Two consequences the SDK relies on:

- `src/internal/…` is **deliberately absent** from the map. This is how the generated protobuf code is kept out of
  the public API (see the "Protobuf is never public API" table in [CLAUDE.md](../CLAUDE.md)) — the wire format
  changes with every network release, and exposing it would make every protocol change a breaking change.
- Adding a namespace means adding a subpath; removing one is a breaking change of the package.

## Type mapping

| Meta-language | TypeScript | Notes |
|---|---|---|
| `string` | `string` | |
| `int8` … `int32`, `uint8` … `uint32` | `number` | every value is a safe integer |
| `int64`, `uint64` and wider | `bigint` | `5n`; the rule is *more than 32 bits* |
| `double` | `number` | |
| `decimal` | `string` | a decimal number as text (JavaScript has no decimal type) |
| `bool` | `boolean` | |
| `bytes` | `Uint8Array` | copied on the way in and out |
| `list<T>` | `ReadonlyArray<T>` | stored as a frozen copy |
| `set<T>` | `ReadonlySet<T>` | copied on the way in and out |
| `map<K, V>` | `ReadonlyMap<K, V>` | copied on the way in and out; **keyed by identity**, see [Identity and equality](#identity-and-equality) |
| `date`, `time`, `dateTime`, `zonedDateTime` | `Date` | copied; `Temporal` once it is available everywhere |
| `duration`, `seconds` | `Duration` | support class (immutable, milliseconds) |
| `uuid` | `string` | |
| `type<T>` | `AbstractConstructor<T>` | the class object, e.g. `AccountCreateTransaction`; `String` for `type<string>` |
| `ANY` | `unknown` | as a type argument: the bound of the type parameter |
| `function<R name(p: T)>` | `(p: T) => R` | |
| `streamResult<T>` | `StreamItem<T>` | `{ ok: true, value }` or `{ ok: false, error }` |

Unlike Java, TypeScript needs no second mapping for nullable values: there are no primitives, so `@@nullable int32`
is `number | null` and not a different carrier type.

### Integers

JavaScript numbers and `bigint` hold values outside of the meta-language types (`300` for a `uint8`, `1.5` for an
`int32`, `2n ** 70n` for a `uint64`). Constructors, setters and functions therefore check the range of every integer
type **and** that a `number` is an integer, and throw a `RangeError`:

```ts
if (!Number.isInteger(port) || port < 0 || port > 65535) {
    throw new RangeError("port must be an integer between 0 and 65535");
}
```

For a `bigint` only the range is checked — a `bigint` is an integer by construction:

```ts
if (value < -9223372036854775808n || value > 9223372036854775807n) {
    throw new RangeError("maxBackoff must be an integer between -9223372036854775808 and 9223372036854775807");
}
```

The split at 32 bits is deliberate: `number` is exact up to 2^53, so a `uint32` is safe, and keeping the small types
as `number` avoids a `bigint` in the ergonomics of everyday values like a port or a count. Everything above
32 bits is a `bigint`, which is exact at any width and is what the meta-language's `int64`/`uint256` demand.

### Function types

A function type maps to a TypeScript function type; the names of the function and its parameters carry over into the
signature but are not part of the type:

```
// Meta-language
subscribe(callback: function<void onEvent(event: Event)>)
```

```ts
subscribe(callback: (event: Event) => void): void;
```

TypeScript needs no counterpart of Java's `@FunctionalInterface` and no distinction between a standard and a custom
interface: a structural function type is the type. A `@@nullable` parameter or result becomes `| null` as everywhere
else.

### Runtime type information

`type<T>` becomes `AbstractConstructor<T>` from `@hiero/support` — the class object itself, which in JavaScript is a
value and can be passed around:

```ts
import type { AbstractConstructor } from "@hiero/support";

export function getResponse<ReceiptT extends Receipt>(transactionId: TransactionId,
    transactionType: AbstractConstructor<Transaction<ReceiptT, any>>,
    client: HieroClient<NativeTokenUnit>): Response<ReceiptT>;
```

For the primitive types the constructor of the wrapper object is used (`type<string>` → `String`), because that is
the only value JavaScript has for them.

## Defensive implementation

The API is called from TypeScript **and** from plain JavaScript, where none of the static types exist. Every value
that crosses the public API boundary is therefore checked at runtime, not only in the type system:

- **Null and `undefined`** are rejected for every non-nullable value, with a `TypeError`.
- **Integer ranges and validation annotations** are checked in constructors, setters and functions, with a
  `RangeError`.
- **`bytes`, sets, maps and dates are copied** on the way in and on the way out; arrays are frozen. A caller must
  not be able to reach the state of a value object through a reference it kept or received.
- **Instances are frozen** (`Object.freeze(this)`) so that a caller cannot add or replace properties.

What defensive implementation does *not* mean: validating again in internal code where the invariant is already
established, or catching and swallowing errors. An error from the SDK is a programming error of the caller and must
reach them.

## Null handling

- `@@nullable` is `T | null`.
- **`undefined` is never part of the API.** A method never returns it, and a `null` in the API always means `null`.
- An **omitted optional property** and an explicit `undefined` are both normalised to `null`. This is what makes the
  init object (see [Complex types](#complex-types)) usable: `new AccountId({ shard, realm, checksum, num })` leaves
  `evmAddress` and `alias` absent, and they become `null`.
- A **non-nullable value that is `null` or `undefined`** is rejected with a `TypeError`, including when a JavaScript
  caller passes it where the type system would have caught it.
- **Collections are never `null`** because of the annotation alone — an empty array, set or map is returned instead.
  (The specs currently contain six attributes that are both a collection and `@@nullable`; the validator reports
  them as `collection.nullable` errors and they are expected to go away.)

```ts
const num = init.num === undefined ? null : init.num;     // nullable: undefined becomes null
if (num !== null) { /* range and annotation checks */ }

const port = init.port;                                   // not nullable: both are rejected
if (port === null || port === undefined) {
    throw new TypeError("port must not be null");
}
```

`exactOptionalPropertyTypes` is on, so in TypeScript a caller may **omit** an optional property but may not pass
`undefined` for it explicitly. The runtime check for `undefined` is there for JavaScript callers, for whom the
compiler flag does nothing.

## Complex types

A complex type is a `class`:

- every attribute is a `#private` field with a getter; a mutable attribute also has a setter, in property syntax
  (`transaction.memo = "x"`);
- the constructor takes **one object with all attributes** — the TypeScript form of a struct literal. Nullable
  attributes and attributes with `@@default` are optional properties;
- constructor and setters check `null`, the integer ranges and the validation annotations, and copy the values that
  need copying;
- a concrete supertype is the superclass (`extends`), abstractions are implemented (`implements`).

```ts
export class ConsensusNode {

    readonly #ip: IpAddress;
    readonly #port: number;
    readonly #account: AccountId;

    /**
     * Creates a new `ConsensusNode`.
     *
     * @param init - the attributes
     * @throws TypeError if a required attribute is `null`
     * @throws RangeError if an attribute violates its constraints
     */
    constructor(init: {
        readonly ip: IpAddress;
        readonly port: number;
        readonly account: AccountId;
    }) {
        const ip = init.ip;
        if (ip === null || ip === undefined) {
            throw new TypeError("ip must not be null");
        }
        this.#ip = ip;
        const port = init.port;
        if (port === null || port === undefined) {
            throw new TypeError("port must not be null");
        }
        if (!Number.isInteger(port) || port < 0 || port > 65535) {
            throw new RangeError("port must be an integer between 0 and 65535");
        }
        this.#port = port;
        // ...
        Object.freeze(this);
    }

    get port(): number {
        return this.#port;
    }
}

const node = new ConsensusNode({ ip, port: 50211, account });
```

The init object replaces the builder pattern that Java needs: it is named, order-independent, and optional
attributes are simply left out. There is no separate builder type.

### Accessors and setters

An attribute is read through a **getter with the attribute's name** — `node.port`, not `node.getPort()`. The
published package has no `getPort`, so this is the form a JavaScript caller sees as well; the
[JavaScript guideline](api-best-practices-js.md#accessors) states the same rule.

A mutable attribute (one without `@@immutable`) additionally gets a **property setter** that runs the same checks as
the constructor:

```ts
get maxAttempts(): number | null {
    return this.#maxAttempts;
}

set maxAttempts(value: number | null) {
    if (value !== null) {
        if (!Number.isInteger(value) || value < -2147483648 || value > 2147483647) {
            throw new RangeError("maxAttempts must be an integer between -2147483648 and 2147483647");
        }
    }
    this.#maxAttempts = value;
}
```

Unlike Java, a setter does **not** return the object: assignment is not an expression that can be chained. The
meta-language's self type (`$$Self extends Transaction<…, $$Self>`), which exists so that Java setters can chain
across an inheritance chain, therefore carries no weight in TypeScript.

### Freezing and immutability

`Object.freeze(this)` is called at the end of the constructor of a class that **has no subtypes**. A class that is
extended does not freeze: freezing happens once, when the instance is complete, and only the most derived
constructor knows when that is.

What freezing does and does not do is worth being precise about, because it is easy to over-read:

| | frozen instance |
|---|---|
| adding a property (`obj.extra = 1`) | `TypeError` |
| replacing a public own property | `TypeError` |
| writing a `#private` field from inside the class | **works** — `Object.freeze` does not cover private fields |
| mutating an object a field points to | works — `freeze` is shallow |

So freezing is **tamper resistance against callers**, not the mechanism that implements `@@immutable`. Immutability
of attributes comes from the fields being `#private` with no setter, and from the copying rules below. That is also
why a class with mutable attributes is still frozen: its setters keep working.

### Copying

| Meta-language type | On the way in | On the way out |
|---|---|---|
| `bytes` | `value.slice()` | `value.slice()` |
| `list<T>` | `Object.freeze([...value])` | the stored array |
| `set<T>` | `new Set(value)` | `new Set(value)` |
| `map<K, V>` | `new Map(value)` | `new Map(value)` |
| `date`, `time`, `dateTime`, `zonedDateTime` | `new Date(value.getTime())` | `new Date(value.getTime())` |
| everything else | stored as given | returned as held |

An array is the only one copied on just one side: the stored array is already frozen, so handing it out is safe. A
`Uint8Array` cannot take its place — `Object.freeze` on a typed array with elements throws
(`Cannot freeze array buffer views with elements`) — so `bytes` is copied on both sides. `Set`, `Map` and `Date`
have no frozen form at all and are copied on both sides as well.

A nullable value is only copied when it is not `null`: `alias === null ? null : alias.slice()`.

### Inheritance and nullability narrowing

A subtype that narrows an inherited `@@nullable` attribute to non-nullable (`@@override`) simply declares the
narrower type. TypeScript's structural typing accepts it, because `bigint` is assignable to `bigint | null`:

```ts
export interface BaseAddress {
    readonly num: bigint | null;    // optional on the abstraction
}

export class Address implements BaseAddress {
    readonly #num: bigint;          // always set on Address
    get num(): bigint { return this.#num; }
}
```

No `override` keyword and no repetition of the wider type is needed — the difference to Java, where the accessor
must keep the identical reference type (`Long`) and only the nullability annotation may be narrowed.

## Attribute annotations

| Annotation | Generated check | Thrown |
|---|---|---|
| `@@nullable` | the type becomes `T \| null`; the null check is skipped and all other checks are guarded by `if (value !== null)` | — |
| *(not nullable)* | `value === null \|\| value === undefined` | `TypeError` |
| *(integer type)* | range of the type, plus `Number.isInteger` for a `number` | `RangeError` |
| `@@default(v)` | the property becomes optional; `init.x === undefined ? v : init.x` | — |
| `@@min(n)` / `@@max(n)` | `value < n` / `value > n` | `RangeError` |
| `@@minLength(n)` / `@@maxLength(n)` | `value.length < n` / `> n` (strings) | `RangeError` |
| `@@minSize(n)` / `@@maxSize(n)` | the size of a list, set, map or `Uint8Array` | `RangeError` |
| `@@pattern(re)` | `!new RegExp(re).test(value)` | `RangeError` |
| `@@urlPattern` | `!isAbsoluteUrl(value)` | `RangeError` |
| `@@immutable` | no setter is generated | — |
| `@@deprecated` | a `@deprecated` tag in the TSDoc comment | — |

Two of these deserve a note:

**`@@default` is only applied to an omitted value.** Passing `null` explicitly for a non-nullable attribute with a
default is still a `TypeError`; the default is not a null-replacement:

```ts
const declineReward = init.declineReward === undefined ? false : init.declineReward;
if (declineReward === null || declineReward === undefined) {
    throw new TypeError("declineReward must not be null");
}
```

**`@@urlPattern` is not a regular expression.** It delegates to the `URL` constructor, so the SDK inherits the
platform's WHATWG URL parsing instead of a hand-written expression — the same decision the Java guideline makes with
`java.net.URI`. The helper is added once to every file that needs it:

```ts
function isAbsoluteUrl(value: string): boolean {
    try {
        return new URL(value).host !== "";
    } catch {
        return false;
    }
}
```

`new URL(value)` throws for a relative URL, and the `host` check rejects the absolute forms without one (`file:`,
`data:`).

## Collections

- The public API uses `ReadonlyArray<T>`, `ReadonlySet<T>` and `ReadonlyMap<K, V>` and never the mutable interfaces.
- **`Readonly*` is a compile-time type only.** It erases at runtime, and a JavaScript caller gets a plain `Array`.
  The runtime guarantee for a list is `Object.freeze`; for a set and a map it is the copy on the way out.
- A collection is **never `null`** because it is empty — an empty collection is returned instead.
- Elements are **never `null`**: the meta-language has no nullable element type, so neither has the mapping.
- A collection passed in is copied before it is stored, and a collection handed out must not be a reference into the
  state (see [Copying](#copying)).

Unlike the Java guideline, there is no concurrency dimension here: JavaScript runs an object on one thread, so there
is no `CopyOnWriteArrayList` counterpart and no `ConcurrentModificationException` to defend against.

## Abstractions

- An abstraction is an `interface`: attributes are `readonly` properties (mutable ones without `readonly`), methods
  are signatures.
- A concrete type states the relationship with `implements`; TypeScript checks it structurally, so a type satisfies
  an abstraction as soon as it has the right members.
- `@@static` methods of an abstraction become functions in an `export namespace` of the **same name as the
  interface**. TypeScript merges the two declarations, so the call reads like a static method and the name stays a
  type at the same time:

```ts
export interface TransactionId { /* attributes and instance methods */ }

export namespace TransactionId {
    export function generateTransactionId(accountId: Address): TransactionId { /* ... */ }
    export function fromString(transactionId: string): TransactionId { /* ... */ }
}

const id: TransactionId = TransactionId.generateTransactionId(payer);
```

  This is the one place where the generated code uses `namespace`, a construct otherwise avoided in ESM TypeScript;
  declaration merging with an interface is the only way to hang a function off a type name.
- A **sealed abstraction** (`@@sealed(A, B)`) is the union of its permitted types, and the variants are
  distinguished with `instanceof`:

```ts
export type Authority = PublicKeyAuthority | ContractAuthority | AuthorityList;
```

A union is exhaustively checkable: a `switch` over `instanceof` with a `never`-typed default makes the compiler
reject a missing variant. This is the closest TypeScript equivalent of Java's `sealed … permits`.

## Generics

- Type parameters lose the `$$` prefix and keep their bounds: `Transaction<$$Receipt extends Receipt, …>` becomes
  `Transaction<ReceiptT extends Receipt, …>`. A name that would collide with a declared type gets a `T` suffix, as
  in Java, so that the parameter does not shadow the type.
- TypeScript has no wildcard, and it checks the bound of every type argument, so `ANY` as a **type argument** has to
  name a type. Which one depends on the parameter it fills:

  | The parameter | `ANY` becomes | Example |
  |---|---|---|
  | has a bound that names no type variable | that bound | `Network<ANY>` → `Network<NativeTokenUnit>` |
  | has a bound that names type variables (a **self type**) | `any` | `NativeToken<ANY, ANY>` → `NativeToken<any, NativeTokenUnit>` |
  | has no bound | `unknown` | `Box<ANY>` → `Box<unknown>` |

  The middle row is the interesting one: `$$Self extends NativeToken<$$Self, $$Unit>` is F-bounded, so substituting
  its bound would expand forever. `any` is the escape hatch, and it is the one place in the generated API where type
  safety is given up — deliberately and visibly.
- As a **standalone** type, `ANY` is `unknown`, not `any`, so that a caller has to narrow before using the value.
- A generic method keeps its own type parameters: TypeScript has no restriction comparable to Java's, where a
  generic instance method has to be `@@finalMethod`. `@@finalMethod` itself has no mapping — TypeScript cannot
  forbid an override, so it is documentation only.

## Enumerations

An enum is a class with one `static readonly` instance per value, so that values can carry attributes and methods
and the enum can implement interfaces. TypeScript's own `enum` is not used: it cannot do either, and
`isolatedModules` restricts it further.

```ts
export class KeyFormat {

    static readonly PKCS8_WITH_DER: KeyFormat = new KeyFormat("PKCS8_WITH_DER", KeyContainer.PKCS8, KeyEncoding.DER);
    static readonly SPKI_WITH_DER: KeyFormat = new KeyFormat("SPKI_WITH_DER", KeyContainer.SPKI, KeyEncoding.DER);

    readonly #name: string;

    private constructor(name: string, /* attributes */) {
        this.#name = name;
        Object.freeze(this);
    }

    /** Returns the name of the constant. */
    get name(): string { return this.#name; }

    get container(): KeyContainer { /* ... */ }

    /** Returns all constants in declaration order. */
    static values(): ReadonlyArray<KeyFormat> {
        return Object.freeze([KeyFormat.PKCS8_WITH_DER, KeyFormat.SPKI_WITH_DER]);
    }

    /** Returns the constant with the given name. @throws RangeError if there is none */
    static valueOf(name: string): KeyFormat { /* ... */ }

    toString(): string { return this.#name; }
}
```

- The constructor is `private`, so the set of values is closed.
- Every value is frozen, and `values()` returns a frozen array.
- `name` is the identifier as the spec writes it (`PKCS8_WITH_DER`), and `toString()` returns it — an enum value is
  therefore readable in a template string and in `console.log`, which a complex type is not (see
  [Debug representation](#debug-representation)).
- Because the values are objects, comparison is by identity (`format === KeyFormat.SPKI_WITH_DER`), which is correct
  here: there is exactly one instance per value.

## Methods and functions

- Methods with the same name are **overloads**: one signature per overload and one implementation signature.
  TypeScript has no overloading by dispatch, so the implementation must accept the union of the parameter types.
- Namespace-level functions are exported functions of the namespace module, in `functions.ts`. There is no factory
  class as in Java — a module is already the namespace.
- `@@async` returns `Promise<T>`, `@@streaming` returns `AsyncIterable<T>`; see the two sections below.

## Asynchronous methods

An `@@async` method returns `Promise<T>` (`Promise<T | null>` for a `@@nullable` result). Errors reject the promise;
they are not returned.

```ts
execute(request: HttpRequest): Promise<HttpResponse>;
```

There is **no synchronous alternative**. Java offers one (`CompletionStage` plus a blocking `…Sync` method) because
a thread can block; JavaScript has no blocking wait on the event loop, so an asynchronous operation is asynchronous
for every caller.

A `Promise` also has no cancellation. How a long-running call is aborted is an open question — see
[Questions & Comments](#questions--comments).

## Streaming

A `@@streaming` method returns `AsyncIterable<T>`. The consumer uses `for await`, and leaving the loop cancels the
stream:

```ts
for await (const item of client.subscribeTopic(topicId)) {
    if (!item.ok) {
        log.warn("bad message", item.error);
        continue;
    }
    use(item.value);
}
```

- **Per-item errors** use `streamResult<T>` → `StreamItem<T>`, the discriminated union `{ ok: true, value }` /
  `{ ok: false, error }`. The stream continues after such an item.
- **Stream-level errors** reject the iteration: the `for await` throws, and the loop ends.
- **Cancellation** is `break`, `return` or a `throw` out of the loop. All three call the iterator's `return()`, which
  is where the implementation releases the connection — so an implementation of a streaming method must implement
  `return()`, not only `next()`.
- `AsyncIterable` carries **backpressure** by construction: the producer only advances when the consumer calls
  `next()` again. This is why the pull form is the only one generated, where Java needs a pull interface plus a
  push adapter over `Flow`.

## Errors

- `@@throws(illegal-format)` and `invalid-argument-error` map to `RangeError`, the built-in error for a value
  outside its domain. The runtime checks of the attribute annotations use the same type.
- A `null` where a value is required is a `TypeError`, not a `RangeError`.
- Every other error identifier becomes an `Error` subclass in the `errors.ts` of the namespace that declares it —
  `not-found-error` → `NotFoundError` — with the constructor `(message: string, options?: ErrorOptions)` and
  `this.name` set:

```ts
export class PaginationError extends Error {

    constructor(message: string, options?: ErrorOptions) {
        super(message, options);
        this.name = "PaginationError";
    }
}
```

  `options` carries the `cause`, which is how an error chain is built in JavaScript. Setting `name` matters because
  it is what appears in a stack trace and in `console.log`.
- Synchronous methods document their errors with `@throws`; asynchronous ones describe with which error the promise
  rejects.

## Constants

Constants are exported `const` values of the namespace module, in `constants.ts`. A constant whose value is a struct
literal becomes a constructor call with **all** attributes written out — including the ones the literal omits, which
appear as `null` or as their `@@default`:

```ts
export const ZERO_ADDRESS: Address = new Address({ shard: 0n, realm: 0n, checksum: "", num: 0n });

export const ZERO_ACCOUNT_ID: AccountId =
    new AccountId({ shard: 0n, realm: 0n, checksum: "", num: 0n, evmAddress: null, alias: null });
```

The constant is not frozen separately: the value object freezes itself.

## Identity and equality

**Value objects of the SDK currently have no value equality.** Two instances built from the same attributes are
different objects, and JavaScript compares objects by identity:

```ts
const a = new AccountId({ shard: 0n, realm: 0n, checksum: "", num: 1001n });
const b = new AccountId({ shard: 0n, realm: 0n, checksum: "", num: 1001n });

a === b                      // false
new Set([a, b]).size         // 2
new Map([[a, "x"]]).get(b)   // undefined
```

Three consequences that users of the API have to know about:

- A `map<K, V>` and a `set<T>` over a value type are **keyed by identity**. `ReadonlyMap<Address, bigint>` does not
  behave like a Java `Map<Address, Long>`: a lookup only finds the entry if the caller passes the very object that
  was put in. The meta-language says nothing about this, and the mapping inherits the JavaScript semantics.
- `assert.deepStrictEqual` is **not** a substitute. `#private` fields are not own properties, so deep equality sees
  an empty object and two instances with *different* values compare as equal. The generated tests therefore use a
  helper that prefers an `equals` method and otherwise falls back to identity.
- The spec of [`authority.md`](../spec/base/authority.md) requires an `Authority` to be a "value type with
  structural equality … two Authorities are equal iff their trees match". **TypeScript does not provide that
  today.**

How to close this is an open design question, not a documentation gap — see
[Questions & Comments](#questions--comments).

## Debug representation

Enums have a `toString()` that returns the name of the value. **Complex types have none**, and the JavaScript
defaults are of no use for them, because the state lives in `#private` fields:

```ts
String(accountId)          // "[object Object]"
JSON.stringify(accountId)  // "{}"
util.inspect(accountId)    // "AccountId {}"
```

Where a spec declares a `toString()` (as `BaseAddress` and `TransactionId` do), the SDK has a readable form; where it
does not, printing an SDK object in a log yields nothing. Whether the generator should add one — and with it the
rules the Java guideline states, above all **never print key material or other sensitive data, and print only the
length of a byte array** — is open, see [Questions & Comments](#questions--comments).

## Documentation and deprecation

- The comments of the specs and the `## Description` sections become TSDoc comments. They are written for the users
  of the API and never refer to the specs, the meta-language or an ADR.
- The generator adds the block tags `@param`, `@returns`, `@throws` and `@deprecated`. An error is documented with
  the TypeScript error type it maps to, never with the meta-language error identifier.
- `@@deprecated` becomes `@deprecated`. Its text is the paragraph of the element's documentation that mentions the
  deprecation; that paragraph is removed from the description so it is not said twice. If the documentation does not
  explain the deprecation, the tag reads "Retained for compatibility; do not use it in new code." — so every
  deprecated element should say why it is deprecated and what replaces it.
- Deprecation is documentation only; it never changes behaviour.

## Thread safety

JavaScript runs an object on one thread (workers do not share objects), so `@@threadSafe` has no mapping. There is
nothing to generate and nothing for an implementation to do.

## What TypeScript does not need

Several sections of the [Java guideline](api-best-practices-java.md) have no counterpart here. Recording why is
worth more than leaving them out silently:

| Java | TypeScript |
|---|---|
| the `final` keyword | `readonly` for properties, `#private` for fields, `Object.freeze` for instances |
| `equals`/`hashCode` | no language-level equality to implement — see [Identity and equality](#identity-and-equality) |
| the builder pattern | the init object of the constructor is named, order-independent and allows omissions |
| factory classes per namespace | a module is the namespace; functions are exported from it |
| avoid Lombok | nothing comparable exists |
| avoid `var` | `const` by default, `let` where a rebinding is needed; `var` is not used |
| JPMS `module-info.java` | the `exports` map of the `package.json`, see [What is public API](#what-is-public-api) |
| `CopyOnWriteArrayList` and friends | single-threaded; no concurrent collections |

## Support types

`Duration`, `StreamItem` and `AbstractConstructor` are hand-written in the package [`@hiero/support`](../sdk-ts/support)
(`sdk-ts/support`). The generated code imports them from there (`import type { Duration } from "@hiero/support"`), and
every generated package that does so declares the dependency. Per
[ADR-0007](../docs/adr/0007-separate-generated-and-hand-written-modules.md) a module is either generated or
hand-written, never both, so a type the API needs but no spec declares belongs here.

## Tests

The generator writes `<Type>.test.ts` next to every class and enum and `functions.test.ts` next to the functions of a
namespace, for the Node.js test runner (`node:test`, `node:assert`). They check the contract of the specs: values,
`null`, ranges, validation annotations, copies, setters, that every method can be called, and the enum constants.
Tests whose values cannot be built are `test.todo` entries.

Because value objects have no equality (see [Identity and equality](#identity-and-equality)), the generated tests
compare with a helper rather than with `deepStrictEqual`:

```ts
/** Asserts that two values are equal: by `equals` if they have it, by content if they are arrays, bytes, sets,
    maps or dates, otherwise identical. */
function assertValue(expected: unknown, actual: unknown): void { /* ... */ }
```

Hand-written tests follow the same structure as the Java ones — one behaviour per test, given/when/then, and the
error cases next to the happy path.

## Questions & Comments

- **Value equality is unresolved, and one spec already requires it.** `spec/base/authority.md` specifies structural
  equality for `Authority`, and the generated test helper already looks for an `equals` method, but nothing provides
  one. Options: generate `equals(other: unknown): boolean` on every value type; put an `Equatable` interface into
  `@hiero/support` and generate the implementation; or declare `equals` in the specs so that every language gets it
  from the same source. Until this is decided, `map<K, V>` and `set<T>` over a value type behave differently in
  TypeScript than in Java and Rust. — open

- **No `toString()` on complex types.** Enums have one, classes do not, and `#private` fields make the JavaScript
  defaults useless. If one is generated, the rules of the Java guideline should apply unchanged: a stable
  `ClassName[a=1, b=2]` form, never sensitive data, only the length of a byte array. — open

- **Cancellation of an `@@async` method.** A `Promise` cannot be cancelled. Should asynchronous methods take an
  `AbortSignal`, should the SDK expose its own cancellation handle, or is cancellation out of scope for the
  request/response calls? Streaming already has an answer (`break` and the iterator's `return()`). — open

- **No SPI mapping.** The specs have `consensusnode.transactions.spi` so that custom services and transaction types
  can be added, and the Java guideline maps this to `ServiceLoader` plus `provides`/`uses`. TypeScript has no
  discovery mechanism; a registry that an application fills explicitly is the likely answer, but it is not
  specified. — open

- **No logging guidance.** The Java guideline prescribes `System.Logger`, so that consumers can plug in a backend.
  JavaScript has no equivalent facade. Should the SDK log at all, and if so through an injectable interface? — open

