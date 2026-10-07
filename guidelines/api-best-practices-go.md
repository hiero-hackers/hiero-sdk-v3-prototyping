# Go API Implementation Guideline

This document translates the [language-agnostic API meta-definition](api-guideline.md) into Go.

Unlike the Java, TypeScript and Rust guidelines, **no generator implements this yet**. The document is written
first, as the specification a future `metalang generate --language=go` has to follow — the same order the other
three took. Where the meta-definition already records a Go decision, this guideline carries it over and says so.

Go differs from the other three targets in ways that reshape the API surface rather than only its spelling: there
are no exceptions, no method overloading, no inheritance, and no self types. Every such point below is a decision
with its consequence stated, not a translation.

## Module and packages

- Go 1.27 or newer: `iter.Seq2` for streaming (1.23) and generic methods on concrete types (1.27). The API uses
  the standard library and, where a spec type has no standard equivalent, one small widely used module per concern — `github.com/google/uuid` (`uuid`) and
  `github.com/shopspring/decimal` (`decimal`).
- One Go module for the generated API; its path comes from the generator configuration (`go.module` in
  `sdk-go/generator.properties`), because a module path is a repository URL and the specs cannot know it.
- One package per namespace, in a directory per namespace segment: `consensusnode.transactions.accounts` becomes
  `consensusnode/transactions/accounts`, package `accounts`. Package names are lowercase with no underscores, so
  `nativeToken` becomes `nativetoken`.
- A spec folder is **not** a package boundary in Go — there is no artifact per folder as in Maven, npm or Cargo.
  One module holds all namespaces; the folder only groups directories.

```go
import (
    "github.com/hiero-ledger/hiero-sdk-go/ledger"
    "github.com/hiero-ledger/hiero-sdk-go/consensusnode/client"
)
```

## Names

Go's convention is not a mechanical case conversion: initialisms stay fully capitalised. The generator therefore
needs the standard initialism list (`ID`, `URL`, `API`, `HTTP`, `JSON`, `IP`, `EVM`, `RPC`, `DER`, `PEM`,
`ECDSA`, `PKCS8`, `SPKI`, …), and a name is built by splitting the meta-language identifier into words and
capitalising each: a word in that list stays upper case, every other word is capitalised.

| Meta-language | Go |
|---|---|
| `AccountId` (type) | `AccountID` |
| `transactionId` (attribute, method, parameter) | `TransactionID` exported, `transactionID` as a field |
| `evmAddress` | `EVMAddress` |
| `restBaseUrl` | `RESTBaseURL` |
| `type` (a Go keyword) | `type_`; the same for `func`, `range`, `select`, `chan`, `go`, `map` |
| `nativeToken` (namespace) | `nativetoken` (package) |
| `ED25519` (enum value) | `KeyAlgorithmEd25519` — a constant carries its type's name; `Ed25519` follows the
  standard library's `crypto/ed25519`, while a true initialism stays upper case (`KeyAlgorithmECDSA`) |
| `ZERO_ADDRESS` (constant) | `ZeroAddress` — Go has no SCREAMING_CASE |
| `not-found-error` (error) | `ErrNotFound` (sentinel) or `NotFoundError` (type) |
| `$$Receipt` (type parameter) | `Receipt`, or `ReceiptT` if a type has that name |

Everything the specs declare is exported; everything the generator adds for state is unexported.

## Type mapping

| Meta-language | Go | Notes |
|---|---|---|
| `string` | `string` | |
| `int8` … `int64`, `uint8` … `uint64` | `int8` … `int64`, `uint8` … `uint64` | |
| other widths (`int24`) | the next wider type (`int32`) | constructors and setters check the range |
| `int128`, `int256`, `uint128`, `uint256` | `*big.Int` | Go has no wider fixed integer |
| `double` | `float64` | |
| `decimal` | `decimal.Decimal` | third-party; deferred until `go.mod` carries requirements |
| `bool` | `bool` | |
| `bytes` | `[]byte` | copied in and out, see [Structs](#structs) |
| `list<T>` | `[]T` | |
| `set<T>` | `map[T]struct{}` | `[]T` when `T` is not comparable |
| `map<K, V>` | `map[K]V` | Go has no map with an incomparable key; such a declaration is deferred |
| `date`, `time`, `dateTime`, `zonedDateTime` | `time.Time` | the four differ only in documentation |
| `duration`, `seconds` | `time.Duration` | |
| `uuid` | `uuid.UUID` | third-party; deferred until `go.mod` carries requirements |
| `type<T>` | `reflect.Type` | |
| `ANY` | `any` | as a type *argument* it resolves to the bound, see [Generics](#generics-self-types-and-any) |
| abstraction `T` | `T` (an interface) | |
| sealed abstraction `T` | `T` (an interface with an unexported method) | see [Sealed abstractions](#sealed-abstractions) |
| `function<R name(p: T)>` | `func(T) R` | |
| `streamResult<T>` | `iter.Seq2[T, error]` | recorded in the meta-definition |
| `@@nullable T` | `*T` | except for interfaces, slices and maps, whose zero value is already `nil` |

## Structs

A type with attributes is a struct with **unexported fields**, a constructor and getters. Unexported fields are
what makes `@@immutable` real in Go: a value handed to a caller cannot be changed from outside its package.

```go
// AccountID identifies an account on the ledger.
type AccountID struct {
    shard      uint64
    realm      uint64
    checksum   string
    num        *uint64
    evmAddress *EVMAddress
}

// NewAccountID creates an AccountID.
func NewAccountID(shard, realm uint64, checksum string, num *uint64, evmAddress *EVMAddress) (AccountID, error)

// Num returns the account number, or nil when the account is addressed by an alias.
func (a AccountID) Num() *uint64 { return a.num }
```

- The constructor is `New<Type>` and returns `(T, error)` when the type has validation annotations
  (`@@min`, `@@pattern`, a non-null requirement), otherwise plain `T`.
- Getters are value receivers with the attribute's name; there is no `Get` prefix in Go.
- `bytes`, slices and maps are copied in the constructor and in the getter, so a caller cannot reach the
  state. All three are reference types in Go; without the copy an `@@immutable` attribute of such a type
  would be immutable in name only.
- A mutable attribute gets `Set<Name>` on a pointer receiver; see [Setters and chaining](#setters-and-chaining).

## Interfaces

An `abstraction` is an interface. Its attributes become getter methods on the interface; the state lives in a
struct that concrete types embed.

```go
type BaseAddress interface {
    Shard() uint64
    Realm() uint64
    Checksum() string
    Num() *uint64
    ValidateChecksum(network Network) bool
    String() string
}
```

`extends` is interface embedding:

```go
type EvmCapableAddress interface {
    BaseAddress
    EVMAddress() *EVMAddress
}
```

**The state is not inherited.** An earlier version of this guideline gave an abstraction with attributes an
unexported base struct that every subtype embeds. That does not work: an unexported type cannot be embedded from
another package, and the specs inherit across namespaces throughout (`consensusnode.transactions.accounts`
extends `consensusnode.transactions`). Exporting the base struct instead would put a type into the public API
that no spec declares, only to carry fields that have to stay unexported.

Decision: a concrete struct carries **all** its effective attributes — its own and the inherited ones — as its
own unexported fields, with its own constructor and getters:

```go
type ContractID struct {
    shard      uint64        // inherited from BaseAddress
    realm      uint64        // inherited from BaseAddress
    evmAddress *EVMAddress
}
```

Go has no abstract method and no struct inheritance: an interface declares, a struct implements, and structural
typing makes the struct satisfy the interface without saying so. The cost is that the field list of a subtype
repeats its supertype's — which is invisible to callers and costs a generator nothing.

## Setters and chaining

The meta-language uses a self type so that setters chain across an inheritance chain:
`Transaction<$$Receipt, $$Self>` has `setMemo(...) $$Self`. **Go has no self type and no covariant returns.**

Decision: setters are generated into **each concrete struct** and return that concrete pointer type; the
abstraction's interface declares only the getters.

```go
func (t *AccountCreateTransaction) SetMemo(memo *string) *AccountCreateTransaction
```

Consequence, stated plainly: chaining works on a concrete type, which is the common case, and does **not** work
through a value of the interface type. A caller holding a `Transaction` has to type-assert before chaining. The
alternative — a type parameter `[S any]` threaded through every abstraction — infects every signature in the API
for a gain only the chaining syntax sees, and was rejected for that reason.

## Generics, self types and `ANY`

Go has type parameters but neither wildcards, nor variance, nor subtyping for structs. Three rules follow, and
together they are what makes the generic types of the specs expressible in Go at all.

**A self type is dropped.** `$$Self extends Transaction<$$Self, $$Receipt>` is F-bounded: its bound mentions the
parameter itself. It exists so that a setter can return the concrete type, and [Setters and
chaining](#setters-and-chaining) already decided that a Go setter returns the concrete pointer type instead. The
parameter therefore carries nothing a Go signature could use, and the generated declaration does not have it:

| Meta-language | Go |
|---|---|
| `abstraction Transaction<$$Receipt extends Receipt, $$Self extends Transaction<$$Self, $$Receipt>>` | `type Transaction[ReceiptT Receipt] interface` |
| a field of type `Transaction<ANY, ANY>` | `Transaction[Receipt]` |

**A wildcard argument becomes the bound of the parameter it fills.** Go has to name a type: `Page<ANY>` is
`Page[any]` when the parameter is unbounded, and `Holder<ANY>` is `Holder[Unit]` when the parameter is bounded by
`Unit`. Without the rule above this would not terminate — substituting a self type's own bound expands forever —
which is the second reason the self type goes.

**A concrete bound is widened to `any`.** `$$B extends Base`, where `Base` is a struct rather than an
abstraction, has no Go constraint: a type set of one struct admits exactly that struct, because Go has no
subtyping for structs. The parameter is generated as `any` and the declaration's doc comment says which
constraint was lost, so it is visible in the API and not only in the specs.

**A type parameter never shadows a declared type.** `$$Receipt` would render as `Receipt`, and
`type Response[Receipt Receipt]` is rejected with *cannot use a type parameter as constraint*; such a name gets a
`T` suffix (`Response[ReceiptT Receipt]`).

## Sealed abstractions

A `@@sealed` abstraction is an interface with one unexported marker method. Only types in the declaring package
can implement it, which is Go's equivalent of a sealed hierarchy — enforced by the compiler.

```go
type Authority interface {
    isAuthority()
}

func (PublicKeyAuthority) isAuthority() {}
func (ContractAuthority) isAuthority()  {}
func (AuthorityList) isAuthority()      {}
```

Exhaustiveness is not checked by the compiler, so a type switch over a sealed abstraction ends in a `default` that
returns an error rather than panicking.

## Enumerations

Go has no enum. Which of two shapes is generated depends on whether the enum declares attributes.

**Without attributes** — a defined integer type, typed constants and `String()`:

```go
type KeyAlgorithm int

const (
    KeyAlgorithmED25519 KeyAlgorithm = iota
    KeyAlgorithmECDSA
)

func (k KeyAlgorithm) String() string
func KeyAlgorithmValues() []KeyAlgorithm
func KeyAlgorithmValueOf(name string) (KeyAlgorithm, error)
```

**With attributes** — a struct with unexported fields and package-level values, because Go has no constant struct:

```go
type KeyFormat struct {
    name      string
    container KeyContainer
    encoding  KeyEncoding
}

var (
    KeyFormatPKCS8WithDER = KeyFormat{"PKCS8_WITH_DER", KeyContainerPKCS8, KeyEncodingDER}
    KeyFormatSPKIWithDER  = KeyFormat{"SPKI_WITH_DER", KeyContainerSPKI, KeyEncodingDER}
)
```

Such values are variables, not constants, so the generator must not expose anything that lets a caller reassign
them — the fields stay unexported and the type is used by value.

## Methods, and the absence of overloading

Go has no overloading, so an overload set has to become distinct names. The rule:

- the overload the specs document as the preferred form keeps the bare name;
- every other overload is suffixed with what distinguishes it, as `From<Distinguishing>`.

For `KeysFactory`, whose five `createPrivateKey` overloads are the worst case in the current specs:

| Meta-language | Go |
|---|---|
| `createPrivateKey(value: string)` | `CreatePrivateKey(value string)` |
| `createPrivateKey(container: KeyFormat, value: bytes)` | `CreatePrivateKeyFromFormattedBytes` |
| `createPrivateKey(container: KeyFormat, value: string)` | `CreatePrivateKeyFromFormattedString` |
| `createPrivateKey(algorithm, rawBytes: bytes)` | `CreatePrivateKeyFromRawBytes` |
| `createPrivateKey(algorithm, encoding, value: string)` | `CreatePrivateKeyFromEncodedString` |

Namespace-level functions are package-level functions. Factory types (`KeysFactory`, `ClientFactory`) have no Go
equivalent and disappear: their methods become package-level functions in the namespace's package.

**Generic methods.** Go 1.27 allows a method of a concrete type to declare its own type parameters; an interface
method still may not (`interface method must have no type parameters`). The meta-definition records exactly this
split, and it is what the generator follows:

| A `@@finalMethod` generic method `$$T convert<$$T>(x: X)` on | Go |
|---|---|
| a concrete type `Obj` | a generic method: `func (o Obj) Convert[T any](x X) T` |
| an abstraction `Obj` | a package-level generic function named `<TypeName><MethodName>`, instance first: `func ObjConvert[T any](o Obj, x X) T` |

Both forms were verified against Go 1.27.1.

## Errors

Go has no exceptions. **Every method that declares `@@throws` returns `(T, error)`; a method that declares none
returns only its value.** That keeps getters clean and makes the fallible surface visible in the signature, which
is the whole point of the Go convention.

An error identifier becomes a sentinel when it carries no data, and a type when it does:

```go
var ErrNotFound = errors.New("not found")

type IllegalArgumentError struct {
    Parameter string
    Reason    string
}

func (e *IllegalArgumentError) Error() string { return ... }
```

Callers use `errors.Is` for sentinels and `errors.As` for typed errors; the generator never returns a bare
`errors.New` string for an identifier the specs name. Panics are reserved for programming errors that no caller
can handle — never for a condition a spec declares.

## Asynchronous methods

`@@async` has no future type in Go. An asynchronous method becomes a **blocking** method whose first parameter is
a `context.Context`:

```go
func (p *PackedTransaction) Submit(ctx context.Context, client Client) (Response, error)
```

The caller decides on concurrency with a goroutine, and cancellation and deadlines travel in the context. This is
the idiom; returning a channel would be an unidiomatic imitation of a future and is not generated.

## Streaming

`@@streaming` returns `iter.Seq2[T, error]`, as the meta-definition records. The range-over-func form gives the
caller a `for … range` loop and cancellation by `break`:

```go
for item, err := range client.SubscribeTopic(ctx, topicID) {
    if err != nil {
        return err
    }
    use(item)
}
```

A streaming method that does not use `streamResult` still yields `(T, error)`; the error is then terminal.

## Constants

Untyped constants become `const`. A constant of a struct type becomes `var`, because Go has no constant struct —
the same compromise as the enum values above, with the same consequence.

## Documentation and deprecation

A doc comment starts with the identifier it documents (`// AccountID identifies …`), as `go doc` expects. The
`## Description` of a spec and the comment above a declaration become that comment. `@@deprecated` adds a
paragraph beginning `Deprecated:`, the form `go vet` and the editors recognise.

## Thread safety

Go has no annotation for it. `@@threadSafe` becomes a sentence in the doc comment. Structs with unexported fields
and value receivers are safe to share by construction; where a type holds mutable state the generated
documentation says which methods may be called concurrently.

## Support package

Types the generated API uses but no spec declares live in a hand-written `support` package — the Go counterpart of
`sdk-java/support` and `sdk-ts/support`, under the same rule as [ADR-0007](../docs/adr/0007-separate-generated-and-hand-written-modules.md):
a module is either generated or hand-written, never both.

## Protobuf

The protobuf messages are generated from the vendored definitions in [`/protobuf`](../protobuf) with
`protoc-gen-go`, into **`internal/proto`**. They are not part of the public API, and in Go that is enforced by the
toolchain rather than by convention: a package under `internal/` can only be imported from within the subtree
rooted at its parent directory. Of the four target languages this is the strongest mechanism — Java needs a
qualified `exports … to …`, Rust a non-`pub` module, TypeScript an `exports` map.

## Tests

Generated tests are `_test.go` files next to the type they test, using the standard `testing` package and
table-driven cases. They follow the same contract as the Java, TypeScript and Rust tests: construct from the
`## Default Instances` of the spec, check the declared constraints, and fail for a method that is not implemented
yet.

## Questions & Comments

- **`decimal` and `uuid` need a third-party module.** The mapping table names `decimal.Decimal` and `uuid.UUID`,
  but the generator writes no `require` into `go.mod` and pins no versions, so a declaration using either is
  deferred rather than generated into a module that would not build. No spec uses them today. Should the
  generator manage module requirements, or should the support package wrap both so the generated API depends only
  on it? — open
- **The module path is not knowable from the specs.** `go.module` has to be configured, and it changes when the
  generated code moves repository. Should the generated module carry a `/v3` major-version suffix from the start,
  given that this is the third generation of the SDKs? — open

- **Chaining through an abstraction is lost.** The decision under [Setters and chaining](#setters-and-chaining)
  trades it for signatures that stay readable. If the enterprise layer turns out to chain through
  `Transaction` values often, the trade should be revisited before the generator is written, not after. — open

- **`set<T>` has no good mapping.** `map[T]struct{}` is the idiom but reads poorly in a public API, and it cannot
  hold a non-comparable element. Whether the specs should narrow `set<T>` to comparable element types is a question
  for the meta-definition, not for Go alone. — open

- **The four spec problems the Java and TypeScript spikes found apply here too** (see the `## Questions &
  Comments` of `spec/consensus-node-client/client.md` and `transactions.md`): a client that cannot reach its own
  network, a response that cannot query anything, `generateTransactionId` taking the wrong address type, and a
  status enum without `SUCCESS`. Go cannot work around the first two with a registry as cleanly as the other
  languages did, because a Go struct has no identity to key on once it is copied by value. — open
