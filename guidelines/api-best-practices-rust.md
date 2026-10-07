# Rust API Implementation Guideline

This document translates the [language-agnostic API meta-definition](api-guideline.md) into Rust. The generator of
the spec tooling (`tooling/metalang`, `metalang generate --language=rust`) implements exactly these rules; the
generated workspace `sdk-rust/generated` is the reference.

## Crates and modules

- Edition 2024, Rust 1.85 or newer. The API uses the standard library and a few small, widely used crates:
  `chrono` (dates), `rust_decimal` (`decimal`), `uuid`, `ethnum` (256-bit integers), `regex` (`@@pattern`) and
  `futures-core` (streams). A crate only depends on the libraries its code uses.
- One crate per spec folder (`hiero-base`, `hiero-consensus-node-client`, ...) in one Cargo workspace; a crate depends
  on the crates of the folders it requires.
- One module per namespace (`hiero_base::ledger`, `hiero_consensus_node_client::consensusnode::transactions`); every
  type lives in a private module of its own file (`ledger/account_id.rs`) and is re-exported by the namespace module,
  so its path is always `<crate>::<namespace>::<Type>`. Functions, constants and errors of a namespace are in
  `functions.rs`, `constants.rs` and `errors.rs`, re-exported the same way.

```rust
use hiero_base::ledger::{AccountId, TransactionId};
use hiero_consensus_node_client::consensusnode::client::create_client;
```

## Names

| Meta-language | Rust |
|---|---|
| `AccountId` (type) | `AccountId` |
| `transactionId` (attribute, method, parameter) | `transaction_id` |
| `type` (a Rust keyword) | `r#type`; `self`, `super`, `crate` get a trailing `_` |
| `nativeToken` (namespace) | `native_token` (module) |
| `ED25519` (enum value) | `Ed25519` (variant) |
| `ZERO_ADDRESS` (constant) | `ZERO_ADDRESS` |
| `not-found-error` (error) | `NotFoundError` |
| `$$Receipt` (type parameter) | `Receipt`, or `ReceiptT` if a type has this name |

## Type mapping

| Meta-language | Rust | Notes |
|---|---|---|
| `string` | `String` | getters return `&str` |
| `int8` … `int128`, `uint8` … `uint128` | `i8` … `i128`, `u8` … `u128` | the smallest type that holds all values |
| `int256`, `uint256` | `ethnum::I256`, `ethnum::U256` | |
| other widths (`int24`) | the next wider type (`i32`) | constructors and setters check the range |
| `double` | `f64` | |
| `decimal` | `rust_decimal::Decimal` | |
| `bool` | `bool` | |
| `bytes` | `Vec<u8>` | getters return `&[u8]` |
| `list<T>` | `Vec<T>` | getters return `&[T]` |
| `set<T>` | `HashSet<T>` | `Vec<T>` if `T` cannot be hashed (a trait object) |
| `map<K, V>` | `HashMap<K, V>` | `Vec<(K, V)>` if `K` cannot be hashed |
| `date`, `time`, `dateTime` | `chrono::NaiveDate`, `NaiveTime`, `NaiveDateTime` | |
| `zonedDateTime` | `chrono::DateTime<chrono::FixedOffset>` | |
| `duration`, `seconds` | `std::time::Duration` | |
| `uuid` | `uuid::Uuid` | |
| `type<T>` | `std::any::TypeId` | `TypeId::of::<AccountCreateTransaction>()` |
| `ANY` | `Arc<dyn Any + Send + Sync>` | as type argument: see [Type parameters](#type-parameters) |
| abstraction `T` | `Arc<dyn T>` | shared, cheap to clone, usable across threads |
| sealed abstraction `T` | `T` (an enum) | see [Sealed abstractions](#sealed-abstractions) |
| `function<R name(p: T)>` | `Arc<dyn Fn(T) -> R + Send + Sync>` | |
| `streamResult<T>` | `StreamItem<T>` | `Result<T, Box<dyn Error + Send + Sync>>` |
| `@@nullable T` | `Option<T>` | getters return `Option<&T>` (or `Option<T>` for `Copy` values) |

Parameters are owned values (`String`, `Vec<T>`); varargs (`T...`) are a `Vec<T>`.

## Structs

A complex type that is no abstraction is a `struct` with private fields:

- `new(...)` takes all attributes in the order of the specs (inherited ones first). Nullable attributes are
  `Option`s, attributes with `@@default` are passed like all others (Rust has no default arguments).
- If an attribute has a validation annotation (`@@min`, `@@max`, `@@minLength`, `@@maxLength`, `@@minSize`,
  `@@maxSize`, `@@pattern`, `@@urlPattern`) or an integer type without exact Rust type, `new` returns
  `Result<Self, InvalidArgumentError>`, otherwise `Self`.
- Every attribute has a getter with the name of the attribute (`shard()`); values that are `Copy` are returned by
  value, strings, bytes and lists as slices, shared values (`Arc`) as clone, everything else by reference.
- A mutable attribute has a setter `set_<name>` that returns `&mut Self` for chaining, or
  `Result<&mut Self, InvalidArgumentError>` if it checks the value (the value is not changed then).
- Methods are inherent methods; `@@async` methods are `async fn`, `@@streaming` methods return `BoxStream`.
- A struct derives `Debug`, `Clone`, `PartialEq`, `Eq` and `Hash` where all its attributes allow it (a trait object or
  a function value has no equality; a function value has no `Debug`, then `Debug` is implemented without it).
- `string toString()` is `std::fmt::Display` (so `to_string()` works).
- A struct that extends another struct converts to it with `From` (Rust has no struct inheritance).

```rust
let id = AccountId::new(0, 0, String::new(), Some(1001), None, None);
let num: Option<u64> = id.num();
```

The Rust type system already guarantees what Java and TypeScript check at runtime: a value is never `null` unless it
is an `Option`, integers have their range, and ownership makes copies of collections unnecessary.

## Traits

An abstraction is a trait with the supertraits `Debug + Send + Sync` (and `Display` if it declares `toString()`):

- attributes are getters (and setters for mutable ones), methods are trait methods;
- `@@async` methods return `BoxFuture<'_, T>` (a boxed future, so that the trait stays usable as `dyn Trait`),
  `@@streaming` methods return `BoxStream<'_, T>`;
- methods that are generic or use `Self` get `where Self: Sized`, so that the other methods stay callable on
  `dyn Trait`;
- `@@static` methods are associated functions of the trait object: `<dyn TransactionId>::from_string(text)`;
- `@@finalMethod` methods are default methods of an extension trait `<Trait>Ext` with a blanket implementation for
  every implementation of the trait, so that they cannot be overridden.

A struct that implements a trait has its members as inherent methods (with the concrete types of the struct) and
implements the trait with the erased types of the trait (see below); where both signatures are the same, the trait
implementation calls the inherent method.

### Type parameters

Type parameters remain type parameters (without bounds on the type; bounds of method type parameters are kept), with
two exceptions:

- A `$$Self` parameter (bounded by the type itself, `NativeToken<$$Self extends NativeToken<$$Self, $$Unit>, ...>`)
  is Rust's `Self`.
- A parameter that the specs use with `ANY` (`HieroClient<ANY>`, `NativeToken<ANY, ANY>`) is **erased**: Rust has no
  wildcards, so the parameter is replaced by its bound as trait object (`Arc<dyn NativeTokenUnit>`), or by
  `Arc<dyn Any + Send + Sync>` without bound, and the type has one parameter less (`HieroClient`, `dyn NativeToken`).

Concrete types keep their concrete types in their inherent methods (`Hbar::unit() -> HbarUnit`,
`AccountCreateTransaction::sign_with_operator_and_submit -> Response<AccountCreateReceipt>`); the trait
implementation returns the erased form (`NativeToken::unit -> Arc<dyn NativeTokenUnit>`).

## Inheritance and Nullability Narrowing

Rust has no class inheritance; the meta-language's parent / child relationship is mapped to traits and structs. The
[Narrowing inherited nullability](api-guideline.md#narrowing-inherited-nullability) rule — where a child re-declares
an inherited `@@nullable` field as non-`@@nullable` via `@@override` — maps naturally onto this split:

- The trait getter returns `Option<T>` (the parent's `@@nullable` contract).
- The struct stores the value as `T` and its inherent getter returns `T` (the child's narrowed contract).
- The trait implementation wraps the value in `Some(...)`.

```text
// Meta-language
abstraction Identifier {
    @@immutable @@nullable num: uint64
}

@@finalType
NumericIdentifier extends Identifier {
    @@immutable @@override num: uint64
}
```

```rust
pub trait Identifier: Debug + Send + Sync {
    fn num(&self) -> Option<u64>;
}

pub struct NumericIdentifier {
    num: u64,
}

impl NumericIdentifier {
    pub fn num(&self) -> u64 {
        self.num
    }
}

impl Identifier for NumericIdentifier {
    fn num(&self) -> Option<u64> {
        Some(self.num())
    }
}

let id = NumericIdentifier::new(42);
let value: u64 = id.num();                                // the inherent getter: no Option
let through_trait: Option<u64> = Identifier::num(&id);   // the trait: Option
```

## Sealed abstractions

A sealed abstraction (`@@sealed(A, B)`) is an enum with one variant per permitted type, which matches exhaustively;
each permitted type converts into it with `From`. Attributes and methods of the sealed abstraction are inherent
methods of the enum.

```rust
pub enum Authority {
    PublicKeyAuthority(PublicKeyAuthority),
    ContractAuthority(ContractAuthority),
    AuthorityList(AuthorityList),
}

let authority = Authority::from(PublicKeyAuthority::new(key));
```

## Enumerations

An enum is a Rust `enum` with unit variants that derives `Debug`, `Clone`, `Copy`, `PartialEq`, `Eq` and `Hash`.
`name()` returns the name in the specs, `values()` all constants, `Display` writes the name and `FromStr` parses it
(an unknown name is an `InvalidArgumentError`).

Rust enum variants cannot carry per-variant constant fields. An [enum attribute list](api-guideline.md#enumerations)
therefore maps to one accessor method per attribute that matches on `self` (a `const fn` where the type allows it);
the values come from the arguments of each meta-language enum value:

```
// Meta-language
enum KeyAlgorithm(keySize: int32) {
    ED25519(32)
    ECDSA_SECP256K1(33)
}
```

```rust
pub enum KeyAlgorithm {
    Ed25519,
    EcdsaSecp256k1,
}

impl KeyAlgorithm {
    pub const fn key_size(&self) -> i32 {
        match self {
            KeyAlgorithm::Ed25519 => 32,
            KeyAlgorithm::EcdsaSecp256k1 => 33,
        }
    }
}
```

## Methods and functions

- Rust has no overloading: of the overloads of a method or function, the one with the fewest parameters keeps the
  name, the others get the names of their parameters: `create_private_key(value)`,
  `create_private_key_with_algorithm_and_encoding_and_value(algorithm, encoding, value)`. An overriding method keeps
  the name of the method it overrides.
- Namespace functions are free functions of the namespace module; `@@async` functions are `async fn`.
- A generic method (`$$T get<$$T extends Receipt>()`) is a generic Rust method with the bound as trait bound
  (`fn get<T: Receipt>(&self) -> T`).

## Errors

- `@@throws(e)` makes a method return `Result<T, E>`.
- `illegal-format` and `invalid-argument-error` are the support type `InvalidArgumentError`.
- Every other error identifier is a struct implementing `std::error::Error` (`not-found-error` → `NotFoundError`,
  with `new(message)`, `with_source(message, source)` and `message()`), declared in a namespace of a crate that all
  crates using it require.
- A method with several error types returns an error enum with one variant per error,
  `HttpClientExecuteError::Timeout(TimeoutError)`, which converts from each error with `From` (so `?` works).

## Constants

Constants of integer, `f64`, `bool`, `string` (`&str`), duration and enum type are `pub const`; all others are
`pub static NAME: LazyLock<T>` (initialised on first use).

## Documentation and deprecation

- Comments of the specs become `///` documentation (module documentation `//!` for namespaces); code blocks are
  marked as `text` so that rustdoc does not compile them.
- `@@deprecated` becomes `#[deprecated(note = "...")]` with the explanation of the specs.

## Thread safety

Every generated type is `Send + Sync`: traits require it of their implementations, trait objects and function values
are `Arc<dyn ... + Send + Sync>`. `@@threadSafe` therefore needs no further mapping; the generated tests check
`Send + Sync` for every type.

## Support types

`BoxFuture`, `BoxStream`, `StreamItem`, `InvalidArgumentError` and the URL check of `@@urlPattern` have a single
source, [`rust-files`](rust-files), which the generator copies 1:1 into the module `support` of the crate that all
crates require (`hiero_base::support`).

## Tests

The generator writes one integration test per crate (`tests/api`, run with `cargo test`) with one module per type and
namespace. The tests check the contract of the specs as far as the type system does not already guarantee it: the
values of the constructor, the boundaries of the validation annotations (`InvalidArgumentError`), setters, that every
method and function can be called (`@@async` ones are run to completion), enum constants, value equality and
`Send + Sync`. Methods are `todo!()` until they are implemented, so their tests fail until then; tests whose values
cannot be built are ignored tests.
