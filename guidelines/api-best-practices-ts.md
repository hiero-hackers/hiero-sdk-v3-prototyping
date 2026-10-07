# TypeScript API Implementation Guideline

This document translates the [language-agnostic API meta-definition](api-guideline.md) into TypeScript. For using
the resulting API from JavaScript see the [JavaScript guideline](api-best-practices-js.md).

**This guideline is normative, and it is derived from mainstream TypeScript practice — not from what the generator
currently emits.** The generator (`tooling/metalang`, `metalang generate --language=ts`) is expected to follow it;
where it does not yet, the section says so in a **Generator gap** note and
[Generator gaps](#generator-gaps-summary) lists them all.

## Where these rules come from

The rules below are grounded in the published guidance the TypeScript ecosystem actually follows, not in taste:

- the [Azure SDK for JavaScript/TypeScript Design Guidelines](https://azure.github.io/azure-sdk/typescript_design.html) —
  the closest published analogue to this project: a generated, multi-package, strongly typed SDK;
- the [Google TypeScript Style Guide](https://google.github.io/styleguide/tsguide.html);
- the TypeScript community consensus on enumerations, discriminated unions and tree-shaking, and the platform
  standards for cancellation (`AbortSignal`) and iteration (`AsyncIterable`).

Where this project deviates from that guidance on purpose, the section states the reason. Three deviations are
forced by the meta-definition rather than chosen:

1. The API is consumed from plain JavaScript as well, so values are validated at the API boundary instead of being
   trusted the way an internal TypeScript module could be.
2. The meta-definition declares attributes, constraints and errors; the TypeScript shape has to carry all of them.
3. The same API exists in Java, Rust and Go, so a name or a concept cannot be renamed for TypeScript taste alone.

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

Three consequences:

- `src/internal/…` is **deliberately absent** from the map. This is how the generated protobuf code is kept out of
  the public API (see the "Protobuf is never public API" table in [CLAUDE.md](../CLAUDE.md)) — the wire format
  changes with every network release, and exposing it would make every protocol change a breaking change.
- Adding a namespace means adding a subpath; removing one is a breaking change of the package.
- **There should also be a root export.** Azure's guidelines ask that the types a consumer most likely needs are a
  top-level export, and that is what a reader expects from `@hiero/base`. Subpaths stay, for the consumers who want
  to import narrowly; the root is the entry point for everyone else. Because the packages are ESM and side-effect
  free, a root barrel does not defeat tree-shaking — mark it with `"sideEffects": false` so bundlers can prove it.

> **Generator gap — no root export.** `import … from "@hiero/base"` fails with `ERR_PACKAGE_PATH_NOT_EXPORTED`;
> only namespace subpaths resolve. A generated root `index.ts` re-exporting every namespace, plus
> `"sideEffects": false`, would close it.

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

## Validation at the boundary

The mainstream TypeScript rule is **"parse, don't validate" at trust boundaries, and trust the types everywhere
else**: validate what enters the program from outside, never re-check an internal call. A published SDK has an
unusually wide boundary — every public entry point is one, because the caller may be plain JavaScript where none of
the static types exist.

So: **validate once, at the public entry point; never again inside.**

What is checked there:

| Check | Error |
|---|---|
| the value is present (not `undefined`, not `null` where a value is required) | `TypeError` |
| the value has the right **type** (`typeof`, `instanceof`) | `TypeError` |
| the integer range of the declared type, and `Number.isInteger` for a `number` | `RangeError` |
| the validation annotations (`@@min`, `@@pattern`, …) | `RangeError` |

> **Generator gap — the type is not checked.** The generated code verifies presence and range, but nothing verifies
> that a value *is* a `bigint`, a `string` or a `Uint8Array`. Because a range comparison coerces, both
> `{ num: 1001 }` and `{ num: "1001" }` pass for a `uint64` attribute and are stored as given. TypeScript callers
> are covered by the compiler; JavaScript callers get silent corruption. A `typeof`/`instanceof` check per attribute
> closes it.

What validation must **not** become: re-checking inside the implementation where the invariant already holds, or
catching and swallowing errors. An error from the SDK is a bug in the calling code and has to reach it.

Hand-written checks are used rather than a schema library (`zod` and friends): a published SDK should not force a
validation dependency on every consumer, and the constraints come from the meta-definition, which is already the
single source the generator reads.

## Absent values: `undefined` and `null`

TypeScript has two absent values, and the ecosystem does not mandate one —
[Google's style guide](https://google.github.io/styleguide/tsguide.html) says plainly that "there is no general
guidance to prefer one over the other", while recommending **optional fields and parameters (`?`) over an explicit
`| undefined`**. The meta-definition has exactly one absent value (`@@nullable`), so the mapping has to choose.

The rule that follows from both:

- **`undefined` means "not provided".** An optional attribute or parameter is declared with `?`, which is the
  idiomatic TypeScript form and is what a caller gets when they omit it.
- **`null` means "explicitly absent"**, and is used where the API has to tell "not provided" from "deliberately
  cleared". The specs rely on exactly this distinction — an update transaction leaves a field untouched when it is
  not provided, and clears it with a sentinel — so `null` earns its place here instead of being an accident of
  history.
- A `@@nullable` attribute is therefore `T | null`, declared optional (`?`) in the position where it may be
  omitted. Reading it back always yields `T | null`, never `undefined`: a read is unambiguous even though a write
  has two ways to say "absent".
- **Collections are never absent because they are empty** — an empty array, set or map is returned instead.
- **Never put `| null` or `| undefined` into a type alias.** Nullability belongs at the place the type is used, not
  in the name — the one hard rule Google's guide states on the subject.

```ts
interface AccountIdInit {
    readonly shard: bigint;
    readonly realm: bigint;
    readonly checksum: string;
    readonly num?: bigint | null;          // may be omitted, may be explicitly null
}

accountId.num;   // bigint | null — never undefined
```

`exactOptionalPropertyTypes` is on, so TypeScript distinguishes "omitted" from "explicitly `undefined`". The
runtime normalises both to `null`, because a JavaScript caller has no such distinction.

## Data types and behavioural types

This is the one place where the published guidance is unambiguous and this project's generated code currently is
not. Both of the style guides that matter here say the same thing:

- Azure's SDK guidelines: *"Prefer interface types to class types. JavaScript is fundamentally a duck-typed
  language"*, and *"Declare parameters as interface types over class types whenever possible."*
- Google's style guide: *"Use interfaces to define structural types, not classes."*

The reasons are concrete, not stylistic: a class emits runtime code that a bundler cannot tree-shake away, it
forces one nominal implementation on a structurally typed language, and — decisively for an SDK — a class with
`#private` fields is **opaque**. Structural equality, `JSON.stringify`, deep-equality assertions and
`console.log` all stop working on it, because the state is not in own properties.

The specs split cleanly along this line. Of 235 non-enum types, **137 (58 %) declare no instance method at all** —
receipts, addresses, authorities, node and network descriptions. The remaining 98 (transactions, queries, clients,
`Page`) carry behaviour.

### Data types → readonly interfaces, built by a factory function

A type with attributes and no behaviour is a `readonly` interface, created by an exported factory function that
validates and freezes:

```ts
export interface ConsensusNode {
    readonly ip: IpAddress;
    readonly port: number;
    readonly account: AccountId;
}

/**
 * Creates a `ConsensusNode`.
 *
 * @throws TypeError if a required attribute is missing or of the wrong type
 * @throws RangeError if an attribute violates its constraints
 */
export function consensusNode(init: ConsensusNode): ConsensusNode {
    requirePort(init.port);
    // ... the remaining checks
    return Object.freeze({ ...init });
}
```

What this buys, all of it for free and none of it available with a class:

| | readonly interface | class with `#private` |
|---|---|---|
| structural equality (`deepStrictEqual`, test assertions) | works | **broken** — compares two empty objects |
| `JSON.stringify` | works | `{}` |
| `console.log` / `util.inspect` | shows the values | `ConsensusNode {}` |
| spread to derive a changed copy (`{ ...node, port: 1 }`) | works | not possible |
| tree-shaking | type is erased, factory is a plain function | class survives bundling |
| duck typing by the caller | works | requires the SDK's own instance |

> **Generator gap — every type is a class.** The generator emits a `class` with `#private` fields, pass-through
> getters and `Object.freeze(this)` for *all* 235 types. For the 137 pure-data ones this is the wrong shape, and it
> is the single cause of four problems recorded elsewhere in this guide: no value equality, no useful debug output,
> no JSON form, and broken deep-equality in tests. Changing it is a breaking API change and the largest open item
> for the TypeScript generator.

### Behavioural types → classes

A type that has methods is a `class`. That is what classes are for, and the guidance against them is about data
carriers, not behaviour:

```ts
export class AccountCreateTransaction implements Transaction<AccountCreateReceipt> {

    readonly #key: Authority;
    #memo: string | null;

    constructor(init: AccountCreateTransactionInit) { /* validate */ }

    get memo(): string | null { return this.#memo; }
    set memo(value: string | null) { this.#memo = requireMemo(value); }

    pack(payer: Account, nodes: readonly AccountId[]): PackedTransaction<AccountCreateReceipt> { /* ... */ }
}
```

- The constructor takes **one init object**, not a positional list. It is named, order-independent and allows
  omissions, which is why TypeScript needs no builder pattern.
- `#private` fields for state, `readonly` for what never changes.
- A pure accessor over a `#private` field is acceptable here because the field has to be private; for a data type
  it is not, because the field should not have been private in the first place. Google's guide allows accessors but
  warns that trivial pass-through accessors are not worth their cost.

### Properties, not `getX()`

An attribute is read as a **property** — `node.port`, never `node.getPort()`. The published package has no
`getPort`, and a JavaScript caller sees the same shape; see
[Reading and changing attributes](api-best-practices-js.md#reading-and-changing-attributes).

A mutable attribute is a property **setter** that runs the same checks as the constructor. A setter returns
nothing: an assignment is not an expression, so there is no chaining, and the self type of the meta-definition
(`$$Self`), which exists so Java setters can chain, has no role in TypeScript.

### Immutability

`readonly` is the primary mechanism and costs nothing at runtime. `Object.freeze` is the runtime backstop for
JavaScript callers, for whom `readonly` does not exist.

Freeze **once, when the value is complete**: in the factory function of a data type, or at the end of the
constructor of a class that has no subtypes. A class that is extended must not freeze — only the most derived
constructor knows when the instance is done.

Be precise about what freezing buys, because it is easy to over-read:

| | frozen value |
|---|---|
| adding a property, replacing a public own property | `TypeError` |
| writing a `#private` field from inside the class | **works** — `Object.freeze` does not cover private fields |
| mutating an object a field points to | works — `freeze` is shallow |
| a `Set` or `Map` held in a field | **not frozen** — freezing does nothing to their contents |

So freezing is tamper resistance against callers, not what makes `@@immutable` true. That comes from `readonly`,
from the absence of a setter, and from copying.

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

export interface Address extends BaseAddress {
    readonly num: bigint;           // always set on Address
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

- The public API uses `readonly T[]`, `ReadonlySet<T>` and `ReadonlyMap<K, V>` and never the mutable interfaces.
  `readonly T[]` and `ReadonlyArray<T>` mean the same thing; the shorthand is the more common form in current
  TypeScript and reads better in a signature.
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
  are signatures. This is the idiomatic form — Azure's guidelines ask for interface types precisely so that any
  object of the right shape is accepted.
- A concrete type states the relationship with `implements`; TypeScript checks it structurally, so a value
  satisfies an abstraction as soon as it has the right members, whether or not it says so.
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

  This is the one place where `namespace` is used, a construct otherwise avoided in ESM TypeScript; declaration
  merging with an interface is the only way to hang a function off a type name.

### Sealed abstractions are discriminated unions

A `@@sealed` abstraction is a **union with a literal discriminant**, which is the TypeScript idiom for a closed set
of variants and the only form that gives compile-time exhaustiveness:

```ts
export type Authority =
    | ({ readonly kind: "publicKey" } & PublicKeyAuthority)
    | ({ readonly kind: "contract" } & ContractAuthority)
    | ({ readonly kind: "list" } & AuthorityList);

function describe(authority: Authority): string {
    switch (authority.kind) {
        case "publicKey": return "key";
        case "contract":  return "contract";
        case "list":      return "threshold list";
        default:          return assertNever(authority);
    }
}

function assertNever(value: never): never {
    throw new TypeError(`Unhandled Authority: ${JSON.stringify(value)}`);
}
```

The discriminant is what makes `assertNever` work: adding a variant turns the `default` branch into a compile
error at every switch, instead of a silent fallthrough found in production.

A union of classes narrowed with `instanceof` also type-checks, but it is weaker: `instanceof` fails across
realms and across two copies of the package in one bundle, it forces every variant to be a class, and it is
unavailable to a plain-data variant.

> **Generator gap — no discriminant.** The generator emits `export type Authority = PublicKeyAuthority |
> ContractAuthority | AuthorityList` and the variants are classes, so callers have to use `instanceof`. Adding a
> literal discriminant to each variant would make the union exhaustively checkable and work for plain-data types.
> The discriminant has to come from the meta-definition, so this is a specification change, not only a generator
> change.

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

**Do not use TypeScript's `enum`.** The consensus across the ecosystem — and Azure's guidelines for `const enum`
specifically — is that it compiles to surprising JavaScript, creates a nominal value space where a plain string is
rejected, and resists tree-shaking. The modern replacements are a **union of string literals** when only the type
is needed, and an **`as const` object** when the values are also needed at runtime.

The specs split by what the enum carries. Of 16 enums, **9 carry neither attributes nor methods**, 5 carry
attributes and 4 carry methods.

**Plain enum (9 of 16)** — an `as const` object plus the union derived from it. Zero runtime machinery beyond a
frozen record, and a plain string is assignable:

```ts
export const TokenType = {
    FUNGIBLE_COMMON: "FUNGIBLE_COMMON",
    NON_FUNGIBLE_UNIQUE: "NON_FUNGIBLE_UNIQUE",
} as const;

export type TokenType = (typeof TokenType)[keyof typeof TokenType];

export const tokenTypeValues: readonly TokenType[] = Object.freeze(Object.values(TokenType));
```

A value is its own name, so `String(tokenType)`, `JSON.stringify`, equality and `switch` all work with no help, and
`valueOf` collapses into a membership test.

**Enum with attributes (5 of 16)** — the same shape, with a frozen record per value:

```ts
export const KeyFormat = {
    PKCS8_WITH_DER: { name: "PKCS8_WITH_DER", container: "PKCS8", encoding: "DER" },
    SPKI_WITH_DER:  { name: "SPKI_WITH_DER",  container: "SPKI",  encoding: "DER" },
} as const;

export type KeyFormat = (typeof KeyFormat)[keyof typeof KeyFormat];
```

**Enum with methods (4 of 16)** — the methods become functions of the namespace that take the value
(`decode(format, value)`), rather than methods on a class. This keeps the values plain data and the behaviour
tree-shakeable.

In all three forms, values are compared with `===`, `Object.values` enumerates them in declaration order, and a
`switch` over the union is exhaustively checkable with `assertNever`.

> **Generator gap — enums are classes.** The generator emits a class with a `private constructor`, one
> `static readonly` instance per value, and `name`/`values()`/`valueOf()`/`toString()`. It works, but it is the
> shape the ecosystem moved away from: the values are opaque objects, a plain string is not assignable, and nothing
> tree-shakes. The 9 plain enums could move to `as const` with no loss of expressiveness.

## Methods and functions

- Namespace-level functions are exported functions of the namespace module. There is no factory class — a module is
  already the namespace, and standalone functions are what tree-shakes.
- **An options bag carries the optional parameters.** The idiom across the ecosystem, and a rule in Azure's
  guidelines, is: required parameters positionally, everything else in one options object as the last parameter,
  named `<MethodName>Options`:

```ts
export interface SubmitOptions {
    readonly abortSignal?: AbortSignal;
    readonly maxAttempts?: number;
}

submit(client: HieroClient, options?: SubmitOptions): Promise<Response>;
```

  This keeps a call site readable, lets options be added without a breaking change, and gives cancellation one
  consistent place to live.
- **Overloads are a last resort.** TypeScript allows several signatures over one implementation, and the generator
  uses them where the specs declare overloads, but an options bag or a differently named function is usually the
  better API: an overload set has to be disambiguated at runtime, and JavaScript callers get no help from the
  compiler.

> **Generator gap — no options bag.** The generator maps a spec overload set to TypeScript overload signatures,
> because the meta-definition models overloads and has no notion of an options parameter. Introducing one is a
> specification-level change.

## Asynchronous methods

An `@@async` method returns `Promise<T>` (`Promise<T | null>` for a `@@nullable` result). Errors reject the
promise; they are not returned.

```ts
execute(request: HttpRequest, options?: ExecuteOptions): Promise<HttpResponse>;
```

There is **no synchronous alternative**. Java offers one (`CompletionStage` plus a blocking `…Sync` method) because
a thread can block; JavaScript has no blocking wait on the event loop, so an asynchronous operation is
asynchronous for every caller.

### Cancellation is `AbortSignal`

A `Promise` cannot be cancelled, and the platform answered this years ago: **`AbortController` / `AbortSignal` is
the standard cancellation mechanism**, used by `fetch`, by `addEventListener`, throughout the Node.js core API, and
required by Azure's SDK guidelines on *every* asynchronous call. There is no reason for this SDK to invent
anything:

```ts
const controller = new AbortController();
setTimeout(() => controller.abort(), 5_000);

await transaction.submit(client, { abortSignal: controller.signal });
```

- The signal is a property of the options bag, never a positional parameter.
- An aborted operation rejects with the signal's reason, which is an `AbortError` (a `DOMException` with
  `name === "AbortError"`) unless the caller gave `abort()` a reason of its own.
- An implementation checks `signal.aborted` before starting work and registers an `abort` listener for work already
  in flight; it must not leave the listener attached afterwards.

> **Generator gap — no cancellation at all.** No generated signature takes an `AbortSignal`, because the
> meta-definition does not model one. `@@async` would have to imply an options parameter carrying the signal, which
> is a specification change affecting every language — but TypeScript should not be the one to invent a private
> solution in the meantime.

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

The ecosystem agrees on a small, concrete pattern for library errors: subclass `Error`, set `name`, chain with
`cause`, put domain data in `readonly` fields rather than in the message, and keep the hierarchy shallow — one base
error plus a handful of specific ones.

- A value outside its domain is a **`RangeError`**; a missing or wrongly typed value is a **`TypeError`**. Reusing
  the built-ins for these two cases is deliberate: every JavaScript developer already knows them, and Google's style
  guide asks that only subclasses of `Error` are ever thrown.
- Every other error identifier of the specs becomes an `Error` subclass in the `errors.ts` of the namespace that
  declares it — `not-found-error` → `NotFoundError`:

```ts
export class NotFoundError extends Error {

    /** A stable identifier for this kind of failure. */
    readonly code = "NOT_FOUND";

    constructor(message: string, options?: ErrorOptions) {
        super(message, options);
        this.name = "NotFoundError";
    }
}
```

- **Set `name`.** It is what appears in a stack trace and in `console.log`.
- **Use `cause`.** `new ConnectionError("submit failed", { cause: ioError })` keeps the original error instead of
  flattening it into a string.
- **Carry a stable `code`.** `instanceof` is the natural check, but it fails across realms and when two copies of
  the package end up in one bundle; a string `code` always works and is what service SDKs expose for exactly this
  reason. The message is for humans and is not part of the API contract — the class and the `code` are.
- **Domain data goes in `readonly` fields**, not in the message text.
- Document errors with `@throws`; for an async method, say with which error the promise rejects.

> **Generator gap — no `code`.** Generated error classes set `name` and accept `cause`, but carry no `code` and no
> domain fields. The error identifiers of the meta-definition are already stable strings, so the `code` is
> available without any specification change.

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

The specs require value semantics in at least one place — `spec/base/authority.md` calls `Authority` a "value type
with structural equality … two Authorities are equal iff their trees match" — and today the TypeScript SDK does not
provide it:

```ts
const a = new AccountId({ shard: 0n, realm: 0n, checksum: "", num: 1001n });
const b = new AccountId({ shard: 0n, realm: 0n, checksum: "", num: 1001n });

a === b;                      // false
new Set([a, b]).size;         // 2
new Map([[a, "x"]]).get(b);   // undefined
```

**The fix is not an `equals` method; it is the shape.** A value whose state lives in `#private` fields is opaque to
everything JavaScript offers — structural comparison, `JSON.stringify`, deep-equality assertions, debug output. A
value that is a plain frozen object is transparent to all of them:

| | `readonly` interface | class with `#private` |
|---|---|---|
| `assert.deepStrictEqual(a, b)` | correct | **passes for different values** |
| `JSON.stringify` | the attributes | `{}` |
| `structuredClone` | works | throws |
| equality by hand | `deepStrictEqual`, or compare fields | needs an `equals` the SDK must write |

So [Data types and behavioural types](#data-types-and-behavioural-types) is the answer to this section as well: the
137 pure-data types become readonly interfaces and get value semantics for free; the behavioural types keep their
classes, where identity is the right semantics anyway.

Two things remain true in either shape, and have to be documented for callers:

- **`Map` and `Set` always key by identity.** JavaScript has no value-keyed collection, so a `map<Address, int64>`
  behaves differently from a Java `Map<Address, Long>` whatever the value shape is. Callers key by a string form
  instead.
- **An `equals` method is still worth having** on the few types a caller naturally compares, and the generated
  tests already prefer one when it exists. `Duration` in `@hiero/support` shows the shape.

> **Generator gap.** No generated type has structural equality, and the `Authority` contract of the specs is
> therefore unmet in TypeScript. See the gap in
> [Data types and behavioural types](#data-types--readonly-interfaces-built-by-a-factory-function).

## Debug representation

A plain frozen object prints itself: `console.log`, `util.inspect` and `JSON.stringify` all show the attributes
with no work from the SDK. That is the main everyday benefit of the data-type shape above, and the reason this
section is short.

Where a type is a class — the behavioural ones — add a `toString()`:

- a stable, readable form (`AccountCreateTransaction[memo=…, maxFee=…]`);
- **never key material or other sensitive data**, because a string representation ends up in log files;
- for a byte array only its length (`signature=byte[64]`), never the content.

> **Generator gap — nothing prints.** Only enums get a `toString()`; every other generated class shows as
> `ConsensusNode {}`. The "never print sensitive data" rule additionally cannot be generated today: the
> meta-definition has no annotation marking an attribute as sensitive (the known ones are `async`, `default`,
> `deprecated`, `finalMethod`, `finalType`, `immutable`, `max`, `maxLength`, `maxSize`, `min`, `minLength`,
> `minSize`, `nullable`, `oneOf`, `oneOrNoneOf`, `override`, `pattern`, `sealed`, `static`, `streaming`,
> `threadSafe`, `throws`, `urlPattern`). Either a `@@sensitive` annotation is added, or the conservative rule
> applies: a `bytes` attribute is always printed as its length only.

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
| `equals`/`hashCode` | structural comparison of plain data objects — see [Identity and equality](#identity-and-equality) |
| the builder pattern | an init object or an options bag: named, order-independent, allows omissions |
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

## Generator gaps (summary)

The generator does not implement this guideline yet. Ordered by what it costs to close:

| Gap | Where | Scope |
|---|---|---|
| runtime checks do not verify the **type** of a value | [Validation](#validation-at-the-boundary) | `TsConstraints` only — small, and it closes a silent-corruption bug |
| error classes carry no stable **`code`** | [Errors](#errors) | generator only; the error identifiers already exist |
| no **root export**, no `"sideEffects": false` | [What is public API](#what-is-public-api) | generator only |
| no **`toString()`** on classes | [Debug representation](#debug-representation) | generator only, if the conservative bytes rule is accepted |
| plain **enums are classes** rather than `as const` objects | [Enumerations](#enumerations) | generator only for the 9 plain enums; breaking for callers |
| **data types are classes** rather than readonly interfaces | [Data types](#data-types-and-behavioural-types) | large, breaking — but it is what gives value equality, JSON, `structuredClone` and useful logging at once |
| sealed unions have **no discriminant** | [Sealed abstractions](#sealed-abstractions-are-discriminated-unions) | needs the meta-definition to carry one |
| no **`AbortSignal`** on asynchronous calls | [Cancellation](#cancellation-is-abortsignal) | needs the meta-definition to model an options parameter |
| overloads instead of an **options bag** | [Methods](#methods-and-functions) | needs the meta-definition to model one |

The first four are generator-local and do not change the API shape. The rest change what callers see, and the last
three need a decision in the meta-definition first, because they affect every language.

## Questions & Comments

- **How far should the data-type change go?** Turning the 137 pure-data types into readonly interfaces fixes value
  equality, JSON, `structuredClone`, deep-equality in tests and logging in one step, and is what the published
  guidance recommends. It is also a breaking change and removes the one place where validation currently lives (the
  constructor), which would move to a factory function per type. Worth doing before there are consumers; expensive
  afterwards. — open

- **Does the meta-definition get an options parameter for `@@async`?** Without one there is no place for an
  `AbortSignal`, and TypeScript either has no cancellation or invents a private mechanism. It affects Java, Rust
  and Go as well, so it is a meta-definition question, not a TypeScript one. — open

- **Does the meta-definition get a `@@sensitive` annotation?** Without it a generated `toString()` cannot know
  which attribute must not be printed, and the conservative rule (never print the content of a `bytes` attribute)
  is the only safe option. — open

- **No SPI mapping.** The specs have `consensusnode.transactions.spi` so that custom services and transaction types
  can be added, and the Java guideline maps this to `ServiceLoader`. TypeScript has no discovery mechanism; an
  explicit registry that the application fills is the likely answer, but it is not specified. — open

- **No logging guidance.** The Java guideline prescribes `System.Logger` so consumers can plug in a backend.
  JavaScript has no equivalent facade. Should the SDK log at all, and if so through an injectable interface? — open

- **Pagination does not follow the ecosystem shape.** The specs model `Page<$$T>` with `next()`/`first()`. The
  established form for a JavaScript SDK is an async iterable with a `byPage()` view and a continuation token, so
  that `for await (const item of client.list())` just works. Changing it is a specification question. — open
