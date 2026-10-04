# TypeScript API Implementation Guideline

This document translates the [language-agnostic API meta-definition](api-guideline.md) into TypeScript. The
generator of the spec tooling (`tooling/metalang`, `metalang generate --language=ts`) implements exactly these rules;
the generated workspace `generated/ts` is the reference. For plain JavaScript see the
[JavaScript guideline](api-best-practices-js.md).

## Runtime and tooling

- Target: ES2022 modules (`"type": "module"`), current Node.js LTS (22) and evergreen browsers. The API itself uses no
  Node.js APIs.
- TypeScript 6 with a strict configuration: `strict`, `exactOptionalPropertyTypes`, `noUncheckedIndexedAccess`,
  `noImplicitReturns`, `noUnusedLocals`, `verbatimModuleSyntax` (type-only imports use `import type`),
  `isolatedModules`, `declaration`, `composite`.
- One npm package per spec folder (`@hiero/base`, `@hiero/consensus-node-client`, ...) in one npm workspace; the
  packages depend on the packages of the namespaces they use and are built with project references
  (`tsc --build`).
- One directory per namespace (`src/consensusnode/transactions`), one file per type, `functions.ts`, `constants.ts`
  and `errors.ts` for the other declarations of the namespace, and an `index.ts` that re-exports everything. Every
  namespace is a subpath export of its package:

```ts
import { AccountId, TransactionId } from "@hiero/base/ledger";
import { createClient } from "@hiero/consensus-node-client/consensusnode/client";
```

## Type mapping

| Meta-language | TypeScript | Notes |
|---|---|---|
| `string` | `string` | |
| `int8` … `int32`, `uint8` … `uint32` | `number` | all values are safe integers |
| `int64`, `uint64`, wider integers | `bigint` | `5n` |
| `double` | `number` | |
| `decimal` | `string` | a decimal number as text (no decimal type in JavaScript) |
| `bool` | `boolean` | |
| `bytes` | `Uint8Array` | copied on the way in and out |
| `list<T>` | `ReadonlyArray<T>` | stored as frozen copy |
| `set<T>` | `ReadonlySet<T>` | copied on the way in and out |
| `map<K, V>` | `ReadonlyMap<K, V>` | copied on the way in and out |
| `date`, `time`, `dateTime`, `zonedDateTime` | `Date` | copied; `Temporal` once it is available everywhere |
| `duration`, `seconds` | `Duration` | support class (immutable, milliseconds) |
| `uuid` | `string` | |
| `type<T>` | `AbstractConstructor<T>` | the class object, e.g. `AccountCreateTransaction`; `String` for `type<string>` |
| `ANY` | `unknown` | as type argument: the bound of the type parameter |
| `function<R name(p: T)>` | `(p: T) => R` | |
| `streamResult<T>` | `StreamItem<T>` | `{ ok: true, value }` or `{ ok: false, error }` |

### Integers

JavaScript numbers and `bigint` hold values outside of the meta-language types (`300` for a `uint8`, `1.5` for an
`int32`, `2n ** 70n` for a `uint64`). Constructors, setters and functions therefore check the range of every integer
type and that a `number` is an integer, and throw a `RangeError`:

```ts
if (!Number.isInteger(port) || port < 0 || port > 65535) {
    throw new RangeError("port must be an integer between 0 and 65535");
}
```

## Null handling

- `@@nullable` is `T | null`. `undefined` is never part of the API: a method never returns it, and a value of
  `undefined` is treated like a missing value.
- A non-nullable value that is `null` (or `undefined`, from JavaScript callers) is rejected with a `TypeError`.

## Complex types

A complex type is a `class`:

- every attribute is a `#private` field with a getter; a mutable attribute also has a setter (property syntax:
  `transaction.memo = "x"`);
- the constructor takes **one object with all attributes** — the TypeScript form of a struct literal. Nullable
  attributes and attributes with `@@default` are optional properties (omitted: `null` or the default);
- constructor and setters check `null`, the integer ranges and the validation annotations (`RangeError`), copy
  `bytes`, collections and dates, and freeze arrays; getters return copies of `bytes`, sets, maps and dates;
- instances of a class without subtypes are frozen (`Object.freeze(this)`);
- a concrete supertype is the superclass (`extends`), abstractions are implemented (`implements`).

```ts
export class ConsensusNode {
    readonly #ip: IpAddress;
    readonly #port: number;
    readonly #account: AccountId;

    constructor(init: { readonly ip: IpAddress; readonly port: number; readonly account: AccountId }) {
        // null checks, range checks, copies ...
        Object.freeze(this);
    }

    get port(): number {
        return this.#port;
    }
}

const node = new ConsensusNode({ ip, port: 50211, account });
```

A narrowed attribute (`@@override` of a nullable attribute) is checked in the constructor of the subclass and its
getter is overridden with the narrower type (`override get num(): bigint`).

## Abstractions

- An abstraction is an `interface`: attributes are `readonly` properties (mutable ones without `readonly`), methods are
  signatures.
- `@@static` methods are functions of a namespace with the name of the interface:
  `TransactionId.generateTransactionId(accountId)`.
- A sealed abstraction (`@@sealed(A, B)`) is the union of its permitted types —
  `export type Authority = PublicKeyAuthority | ContractAuthority | AuthorityList` — distinguished with `instanceof`.
- Type parameters keep their bounds; `ANY` as type argument becomes the bound (`Network<NativeTokenUnit>`), because
  TypeScript checks the bound of every argument.

## Enumerations

An enum is a class with one `static readonly` instance per value, so that values can have attributes and methods and
the enum can implement interfaces:

```ts
export class KeyFormat {
    static readonly PKCS8_WITH_DER: KeyFormat = new KeyFormat("PKCS8_WITH_DER", KeyContainer.PKCS8, KeyEncoding.DER);

    get name(): string { ... }
    get container(): KeyContainer { ... }
    static values(): ReadonlyArray<KeyFormat> { ... }
    static valueOf(name: string): KeyFormat { ... }   // RangeError for an unknown name
}
```

## Methods and functions

- Methods with the same name are overloads: one signature per overload and one implementation.
- `@@async` returns `Promise<T>` (`Promise<T | null>` for `@@nullable`); errors reject the promise.
- `@@streaming` returns `AsyncIterable<T>`; `for await` consumes it, `break` cancels it. Per-item errors use
  `StreamItem<T>`.
- Namespace-level functions are exported functions of the namespace module.

## Errors

- `@@throws(illegal-format)` and `invalid-argument-error` are `RangeError`.
- Every other error identifier is an `Error` subclass, `not-found-error` → `NotFoundError`, with the constructor
  `(message: string, options?: ErrorOptions)` (for the `cause`). It is declared in a namespace of a package that all
  packages using it require.
- Synchronous methods document their errors with `@throws`; asynchronous ones describe with which error the promise
  rejects.

## Constants

Constants are exported `const` values of the namespace module: `export const ZERO_ACCOUNT_ID: AccountId = ...`.

## Documentation and deprecation

- Comments of the specs become TSDoc comments; `@@deprecated` becomes `@deprecated` with the explanation of the specs.
- Deprecation is documentation only; it never changes the behaviour.

## Thread safety

JavaScript runs an object on one thread (workers do not share objects), so `@@threadSafe` has no mapping.

## Support types

`Duration`, `StreamItem` and `AbstractConstructor` are hand-written in the package [`@hiero/support`](../sdk-ts/support)
(`sdk-ts/support`). The generated code imports them from there (`import type { Duration } from "@hiero/support"`), and
every generated package that does so declares the dependency.

## Tests

The generator writes `<Type>.test.ts` next to every class and enum and `functions.test.ts` next to the functions of a
namespace, for the Node.js test runner (`node:test`, `node:assert`). They check the contract of the specs: values,
`null`, ranges, validation annotations, copies, setters, that every method can be called, and enum constants. Tests
whose values cannot be built are `test.todo` entries.
