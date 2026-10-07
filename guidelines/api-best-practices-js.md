# Using the SDK from JavaScript

The Hiero SDK V3 for JavaScript and TypeScript is **one SDK**: the generated workspace `sdk-ts/generated` is written in
TypeScript and published as JavaScript (`dist/*.js`) with type declarations beside it (`dist/*.d.ts`). There is no
separate plain-JavaScript implementation and none is planned.

This document is therefore a **consumer guide**: what the API looks like from JavaScript, which of its guarantees
hold at runtime rather than in the type system, and where JavaScript semantics make it behave differently from the
Java or Rust SDK. How the API is *built* is the subject of the
[TypeScript guideline](api-best-practices-ts.md); how it is *specified* is the
[language-agnostic meta-definition](api-guideline.md).

Every example below was run against the published package.

## Requirements

- An **ES module** project (`"type": "module"` or `.mjs`). The SDK is ESM-only; there is no CommonJS build, so
  `require()` does not work.
- Node.js 22 (current LTS) or a modern evergreen browser. The API itself uses no Node.js APIs.
- The runtime must support `bigint`, `#private` class fields and `for await`. Anything from the last few years does.

## Importing

A namespace of the SDK is a **subpath** of its package. There is no root export, and nothing below it can be
reached:

```javascript
import { AccountId, ConsensusNode, IpAddress } from "@hiero/base/ledger";
import { KeyFormat } from "@hiero/base/keys";
import { createClient } from "@hiero/consensus-node-client/consensusnode/client";
```

```javascript
import "@hiero/base";                            // ERR_PACKAGE_PATH_NOT_EXPORTED
import "@hiero/base/internal/keys";              // ERR_PACKAGE_PATH_NOT_EXPORTED
import "@hiero/base/dist/ledger/AccountId.js";   // ERR_PACKAGE_PATH_NOT_EXPORTED
```

This is not a convention — the `exports` map of the `package.json` is enforced by the module resolver. Two things
follow:

- **Import the namespace, not the file.** The path mirrors the namespace of the specs: `ledger.config` is
  `@hiero/base/ledger/config`.
- **`internal/` is unreachable on purpose.** It holds the protobuf code, whose shape follows the network protocol
  and changes with every network release. It is not part of the API, and no supported workaround exposes it.

The packages are `@hiero/base`, `@hiero/consensus-node-client`, `@hiero/consensus-node-admin-client`,
`@hiero/mirror-node-client` and `@hiero/enterprise`, plus `@hiero/support` for the few helper types below.

## The values you work with

| Meta-language | What you get in JavaScript |
|---|---|
| `string`, `uuid`, `decimal` | `string` — a `decimal` is a decimal number as text |
| `int8` … `int32`, `uint8` … `uint32` | `number`, always an integer |
| `int64`, `uint64` and wider | `bigint` |
| `double` | `number` |
| `bool` | `boolean` |
| `bytes` | `Uint8Array` |
| `list<T>` | a frozen `Array` |
| `set<T>`, `map<K, V>` | `Set`, `Map` |
| `date`, `time`, `dateTime`, `zonedDateTime` | `Date` |
| `duration`, `seconds` | `Duration` from `@hiero/support` |
| `type<T>` | the class object itself, e.g. `AccountCreateTransaction` |
| `function<…>` | a function |
| `streamResult<T>` | `{ ok: true, value }` or `{ ok: false, error }` |

### Numbers and bigint

The split is at 32 bits, because a `number` is only exact up to 2^53. Everything wider is a `bigint`, and the two
do not mix — `1n + 1` throws a `TypeError`:

```javascript
new AccountId({ shard: 0n, realm: 0n, checksum: "", num: 1001n });   // bigint: uint64 fields
new ConsensusNode({ ip, port: 50211, account });                     // number: uint16 field
```

A value that arrives as JSON is never a `bigint` — `JSON.parse` has no notion of one — so convert explicitly
(`BigInt(json.num)`) before handing it to the SDK.

Passing a `number` or a string where a `bigint` is expected is rejected with a `TypeError`:

```javascript
new AccountId({ shard: 0n, realm: 0n, checksum: "", num: 1001 });
// TypeError: num must be a bigint
```

The SDK checks the runtime type of every built-in value at the API boundary, which is what protects a JavaScript
caller where TypeScript callers are protected by the compiler. Declared types are not checked this way — a value
of the right shape is accepted whoever built it.

### Support types

`@hiero/support` holds the few types the API needs that no spec declares:

```javascript
import { Duration } from "@hiero/support";

const timeout = Duration.ofSeconds(30);
timeout.toMillis();          // 30000
timeout.equals(other);       // value equality — Duration has it
`${timeout}`;                // readable
```

`StreamItem` is the per-item result of a stream (see [Streaming](#streaming)). `AbstractConstructor` is a
type-level helper only and has no runtime form.

## Creating objects

A constructor takes **one object with all attributes**, not a positional argument list:

```javascript
const node = new ConsensusNode({ ip, port: 50211, account });
```

- The order does not matter, and every attribute is named at the call site.
- **Optional attributes may be omitted**: a `@@nullable` attribute becomes `null`, an attribute with a default
  gets its default value.
- Everything else is **required**. Omitting it, or passing `null` or `undefined`, throws a `TypeError`.
- Values are **validated immediately**, not at first use. A bad value fails where it is created, which is where the
  stack trace is useful.

```javascript
new AccountId({ shard: 0n, realm: 0n, checksum: "", num: 1001n }).evmAddress;
// null — the property was omitted

new ConsensusNode({ ip, port: 70000, account });
// RangeError: port must be an integer between 0 and 65535

new ConsensusNode({ ip, account });
// TypeError: port must not be null
```

## Reading and changing attributes

An attribute is a **property**, read by its name:

```javascript
node.port;        // 50211
node.getPort;     // undefined — there are no getX() methods
```

An attribute the specs do not mark `@@immutable` can be assigned, and the assignment is validated exactly like the
constructor:

```javascript
transaction.memo = "payment";
transaction.maxAttempts = -1;     // RangeError, if the spec constrains it
```

Everything else is read-only, and the instances are frozen:

```javascript
node.port = 1;          // TypeError — the property has no setter and the object is frozen
node.extra = "x";       // TypeError — you cannot add properties either
```

An assignment chain (`tx.setMemo("x").setFee(f)`) does not exist in JavaScript; assign one attribute per statement.

## What the API hands back

The SDK never hands out a reference into its own state:

| You receive | Property |
|---|---|
| a `Uint8Array` | a **copy** — modifying it does not change the object |
| an `Array` | the stored array, **frozen** — `push` throws in strict mode |
| a `Set`, a `Map`, a `Date` | a **copy** |
| anything else | the stored value, which is itself immutable |

```javascript
const alias = accountId.alias;
alias[0] = 0;                   // allowed — it is your copy, the AccountId is unchanged

const children = authorityList.children;
children.push(x);               // TypeError: Cannot add property, object is not extensible
```

A collection is **never `null`** because it is empty — you get an empty array, `Set` or `Map` — and its elements are
never `null`.

## null and undefined

- **`null` is the absent value.** A method never returns `undefined`.
- **`undefined` is rejected** wherever a value is expected. It almost always means a typo or a forgotten argument.
- The exception is an **omitted** attribute of a constructor's init object, where `undefined` and absence mean the
  same thing and become `null` or the attribute's default.

```javascript
new AccountId({ shard: 0n, realm: 0n, checksum: "", num: 1n, alias: undefined }).alias;   // null
new AccountId({ shard: 0n, realm: 0n, checksum: "", num: undefined });                   // TypeError
```

## Errors

| You see | When |
|---|---|
| `TypeError` | a required value is missing, `null`, `undefined`, or of the wrong type |
| `RangeError` | a value is outside its allowed range, length, size or pattern |
| a named `Error` subclass | a domain error the specs declare, e.g. `NotFoundError`, `PaginationError` |

The named errors come from the namespace that declares them and carry their name:

```javascript
import { PaginationError } from "@hiero/base/common";

try {
    await page.next();
} catch (error) {
    if (error instanceof PaginationError) {
        console.warn(error.name, error.message, error.cause);
    }
    throw error;
}
```

`error.cause` carries the underlying error where there is one — prefer it over parsing messages. Messages are for
humans and are not part of the API contract; the error **class** is.

## Enumerations

An enum value is an object, and the enum class holds one instance per value:

```javascript
import { KeyFormat } from "@hiero/base/keys";

KeyFormat.PKCS8_WITH_DER;              // the value
KeyFormat.PKCS8_WITH_DER.name;         // "PKCS8_WITH_DER"
`${KeyFormat.PKCS8_WITH_DER}`;         // "PKCS8_WITH_DER" — toString returns the name
KeyFormat.values();                    // every value, in declaration order (frozen)
KeyFormat.valueOf("SPKI_WITH_DER");    // the value with that name
KeyFormat.valueOf("NOPE");             // RangeError
```

Compare values with `===`. There is exactly one instance per value, so identity comparison is correct and
`switch` works:

```javascript
switch (format) {
    case KeyFormat.PKCS8_WITH_DER: return readDer(bytes);
    case KeyFormat.PKCS8_WITH_PEM: return readPem(text);
    default: throw new RangeError(`Unsupported format: ${format}`);
}
```

Do not compare `format.name` unless you really mean the string — the name is documentation, the value is the API.

## Sealed types

Where the specs declare a closed set of variants, you get the set of classes and distinguish them with
`instanceof`:

```javascript
import { PublicKeyAuthority, ContractAuthority, AuthorityList } from "@hiero/base/authority";

function describe(authority) {
    if (authority instanceof PublicKeyAuthority) { return "key"; }
    if (authority instanceof ContractAuthority)  { return "contract"; }
    if (authority instanceof AuthorityList)      { return "threshold list"; }
    throw new TypeError(`Unknown Authority: ${authority}`);
}
```

JavaScript cannot check that you covered every variant — TypeScript can, which is one reason to run a type-checker
over your JavaScript (see [Type information](#type-information-in-javascript)). End the chain with a `throw`, not a
silent fallthrough, so a new variant surfaces instead of being ignored.

Abstractions that are *not* sealed have no runtime representation at all: they are interfaces in TypeScript and
vanish when compiled. Do not write `instanceof BaseAddress` — check for the property or method you need, or use
the concrete class.

## Methods with several forms

Where the specs declare several overloads of a method, TypeScript sees several signatures but JavaScript sees
**one function**. Pass the arguments of exactly one of the documented forms:

```javascript
client.close();                        // one form
client.close(Duration.ofSeconds(5));   // another
```

Passing a combination that matches no form is rejected rather than guessed. Because there is no compiler to help,
this is one of the places where reading the `.d.ts` or the generated documentation pays off.

## Asynchronous calls

An asynchronous operation returns a `Promise`, and failures reject it:

```javascript
const response = await transaction.signWithOperatorAndSubmit(client);
const receipt = await response.queryReceipt();
```

- There is **no synchronous variant**. JavaScript cannot block, so every asynchronous operation stays
  asynchronous.
- A `Promise` **cannot be cancelled.** If you need to stop waiting, stop awaiting — but the operation itself keeps
  running. How cancellation should work is still open, see [Questions & Comments](#questions--comments).
- Always `await` or `.catch()` a returned promise. An unhandled rejection terminates the Node.js process by
  default.

## Streaming

A streaming operation returns an async iterable. Consume it with `for await`:

```javascript
for await (const item of topicService.subscribe(topicId)) {
    if (!item.ok) {
        console.warn("skipping bad message", item.error);
        continue;
    }
    handle(item.value);
}
```

- **Per-item errors** arrive as `{ ok: false, error }` and the stream continues. That is what `streamResult` in the
  specs means: one bad item does not end the subscription.
- **Stream-level errors** make the `for await` throw, and the loop ends.
- **Leaving the loop cancels the stream.** `break`, `return` and a thrown error all release the underlying
  connection, so there is nothing else to close:

```javascript
for await (const item of topicService.subscribe(topicId)) {
    if (done(item)) {
        break;          // the subscription is closed here
    }
}
```

- The producer only advances when you ask for the next item, so a slow consumer does not build up a backlog.

## Constants

Constants are plain exports of their namespace:

```javascript
import { ZERO_ADDRESS, ZERO_ACCOUNT_ID } from "@hiero/base/ledger";
```

They are ordinary SDK values and therefore immutable like any other.

## Identity and equality

**This is the sharpest difference from the Java and Rust SDKs.** The value types of the SDK have no value equality;
JavaScript compares objects by identity:

```javascript
const a = new AccountId({ shard: 0n, realm: 0n, checksum: "", num: 1001n });
const b = new AccountId({ shard: 0n, realm: 0n, checksum: "", num: 1001n });

a === b;                      // false
new Set([a, b]).size;         // 2
new Map([[a, "x"]]).get(b);   // undefined
```

What this means for your code:

- **Do not use SDK value objects as `Map` keys or `Set` members** when you want value semantics. Key by a string
  form instead — `` `${accountId}` `` for the types that have one — or by the attributes you care about.
- **Deep-equality assertions do not work on them.** The state lives in `#private` fields, which are not own
  properties: `Object.getOwnPropertyNames(node)` is `[]`, so a structural comparison sees two empty objects and
  reports *different* values as equal. This silently makes tests pass that should fail.
- Some types do have an `equals` — `Duration` from `@hiero/support` does — and some have a `toString` you can
  compare. Check before relying on either.

```javascript
// not this
assert.deepStrictEqual(actual, expected);          // passes even when the values differ

// this
assert.strictEqual(`${actual}`, `${expected}`);    // for types with a toString
assert.strictEqual(actual.num, expected.num);      // otherwise, attribute by attribute
```

Whether the SDK should provide value equality is open — see [Questions & Comments](#questions--comments).

## Printing and debugging

The JavaScript defaults show nothing useful for SDK objects, for the same reason: the state is private.

```javascript
String(node);          // "[object Object]"
JSON.stringify(node);  // "{}"
console.log(node);     // ConsensusNode {}
```

Types the specs give a `toString()` print properly, and every enum does:

```javascript
`${accountId}`;                   // "0.0.1001"
`${KeyFormat.PKCS8_WITH_DER}`;    // "PKCS8_WITH_DER"
```

For everything else, log the attributes you care about rather than the object. And note that `JSON.stringify`
produces `{}` — SDK objects are **not** serialisable to JSON, by design: their wire form is the protocol's
business, not the API's.

## Type information in JavaScript

The package ships `.d.ts` declarations next to the JavaScript, so a JavaScript project gets the full type
information without adopting TypeScript:

- Editors use them for completion, parameter hints and documentation out of the box.
- Adding `// @ts-check` at the top of a file, or `"checkJs": true` in a `jsconfig.json`, makes the type-checker
  verify your JavaScript against them — including the exhaustiveness of a `switch` over a sealed type and the
  overload forms described above.
- Describe your own values with JSDoc (`@param {bigint | null}`, `@returns`, `@throws`) so the checker can follow
  them.

This is the cheapest way to get back most of what the type system would give you, and it costs no build step.

## Concurrency

JavaScript runs your code on one thread — workers do not share objects — so no SDK object needs synchronisation and
`@@threadSafe` in the specs has nothing to map to.

Single-threaded is **not** the same as free of interleaving. Every `await` is a suspension point, and other code
runs while you wait. If you read SDK state, await something, and then act on what you read, re-read it afterwards:

```javascript
// the value may be stale after the await
const fee = transaction.maxTransactionFee;
await somethingElse();
use(transaction.maxTransactionFee);     // read again, not `fee`
```

## Testing code that uses the SDK

- The Node.js test runner (`node:test`, `node:assert`) needs no dependency and is what the SDK's own generated
  tests use.
- Do not compare SDK values with a deep-equality assertion — see [Identity and equality](#identity-and-equality).
- Assert on the error **class**, not on the message: `assert.throws(() => …, RangeError)`.
- Integration tests that need a network run against a local [Solo](https://github.com/hiero-ledger/solo) network;
  the cross-SDK behaviour is checked by the external
  [Hiero SDK TCK](https://github.com/hiero-ledger/hiero-sdk-tck), not by your tests.

## Questions & Comments

- **This guide describes the shape the SDK has today.** The
  [TypeScript guideline](api-best-practices-ts.md#generator-gaps-summary) now specifies a different one for pure
  data types — plain frozen objects instead of classes with private fields — which would make structural equality,
  `JSON.stringify`, `structuredClone` and readable `console.log` work. When that lands, the sections on
  [identity](#identity-and-equality) and [printing](#printing-and-debugging) here get simpler, and importing from
  the package root will work. — open

- **Value equality is unresolved.** `spec/base/authority.md` requires `Authority` to be a value type with
  structural equality, and nothing in the JavaScript/TypeScript SDK provides it, so `Map`, `Set` and equality
  assertions behave differently here than in Java and Rust. Whatever is decided for
  [TypeScript](api-best-practices-ts.md#questions--comments) applies unchanged — it is the same objects. — open

- **Most types have no `toString()`.** Enums have one and some spec types declare one; everything else prints as
  `[object Object]`, which makes logging and debugging harder than it should be. — open

- **Cancelling an asynchronous call is not possible.** `AbortSignal` is the platform answer, but the specs do not
  model it. Streaming already has an answer (leaving the loop). — open

- **`guidelines/js-files/lang-base.js`** was written for a plain-JavaScript implementation of the SDK. Now that
  none is planned, it has no consumer: it should either be removed or re-purposed. — open
