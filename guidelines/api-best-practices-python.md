# Python API Implementation Guideline

This document translates the [language-agnostic API meta-definition](api-guideline.md) into Python.

Like the Go guideline, it was written before the generator, as the specification `metalang generate --language=python`
follows; writing the generator then corrected it where the interpreter or mypy disagreed. What the generator emits today
is listed in [What the generator does today](#what-the-generator-does-today); everything else is the target its next
steps have to reach.

Python differs from the statically typed targets in one fundamental way: **the type system is a checker, not the
compiler.** Nothing stops a caller from passing a `str` where an `int` is declared. The generated API is therefore
written for two readers at once — the static checker (mypy `--strict`, pyright) and the runtime — and a decision below
says which of the two enforces a rule.

## Version and packaging

- **Python 3.12 or newer.** The API uses PEP 695 generics (`class Page[T]:`), `type` aliases and
  `typing.override`. Nothing outside the standard library is required at runtime.
- **One distribution, `hiero-sdk`**, with the import package **`hiero_sdk`**. The [Hiero namespace
  guidelines](https://github.com/hiero-ledger/governance/blob/main/rules-and-guidelines/namespaces.md) make the
  `hiero-` prefix mandatory on PyPI, which has no namespaces. A spec folder is no artifact boundary — the same
  decision as for Go: Python has no cheap way to split one import package over several distributions without
  namespace packages, and they would gain nothing here.
- One **subpackage per namespace**, one directory per segment: `consensusnode.transactions.accounts` becomes
  `hiero_sdk.consensusnode.transactions.accounts`. Segments are converted to `snake_case` (`nativeToken` →
  `native_token`).
- **One private module per type** (`_account_id.py`); the package's `__init__.py` re-exports every type and lists
  it in `__all__`. The leading underscore keeps the module layout out of the public API, and a type module can never
  collide with a subpackage of the same name.
- The distribution ships `py.typed` (PEP 561), so type checkers use the inline annotations.

```python
from hiero_sdk.ledger import AccountId
from hiero_sdk.consensusnode.client import HieroClient
```

## Names

PEP 8 decides; the meta-language name is split into words and joined again.

| Meta-language | Python |
|---|---|
| `AccountId` (type) | `AccountId` — types keep their spelling |
| `transactionId` (attribute, method, parameter) | `transaction_id` |
| `restBaseUrl` | `rest_base_url` |
| `ED25519` (enum value), `ZERO_ADDRESS` (constant) | unchanged |
| `nativeToken` (namespace) | `native_token` (package) |
| `from`, `import`, `class`, `lambda`, … (keywords) | `from_`, `import_`, … — a trailing underscore, as PEP 8 recommends |
| `not-found-error` (error) | `NotFoundError` |
| `$$Receipt` (type parameter) | `Receipt`, or `ReceiptT` if a declared type has that name |

`type` and `match` are soft keywords and stay valid identifiers.

## Type mapping

| Meta-language | Python | Notes |
|---|---|---|
| `string` | `str` | |
| every integer width | `int` | Python integers are unbounded; the declared range is a constraint |
| `double` | `float` | |
| `decimal` | `decimal.Decimal` | |
| `bool` | `bool` | |
| `bytes` | `bytes` | immutable, so no copy is needed |
| `list<T>` | `tuple[T, ...]` | immutable, see [Value types](#value-types) |
| `set<T>` | `frozenset[T]` | |
| `map<K, V>` | `collections.abc.Mapping[K, V]` | read-only view in the signature |
| `date`, `time` | `datetime.date`, `datetime.time` | |
| `dateTime` | `datetime.datetime` | naive |
| `zonedDateTime` | `datetime.datetime` | aware; the documentation says so |
| `duration`, `seconds` | `datetime.timedelta` | |
| `uuid` | `uuid.UUID` | |
| `type<T>` | `type[T]` | |
| `ANY` | `object` | as a type *argument* it becomes `typing.Any`, see [Generics](#generics-self-types-and-any) |
| abstraction `T` | `T` (a `typing.Protocol`) | see [Abstractions](#abstractions) |
| sealed abstraction `T` | `type T = A \| B \| …` | see [Sealed abstractions](#sealed-abstractions) |
| `function<R name(p: T)>` | `collections.abc.Callable[[T], R]` | |
| `streamResult<T>` | `StreamItem[T]` of the support package | see [Streaming](#streaming) |
| `@@nullable T` | `T \| None` | |

## Value types

A concrete type is a **dataclass**: `@dataclass(frozen=True, slots=True, kw_only=True)`.

```python
@dataclass(frozen=True, slots=True, kw_only=True)
class AccountId:
    """An account on the ledger."""

    shard: int
    realm: int
    checksum: str
    num: int | None = None
    evm_address: EvmAddress | None = None
```

- **`frozen`** makes `@@immutable` real at runtime: assigning an attribute raises `FrozenInstanceError`.
- **`kw_only`** makes every argument a keyword argument — the Python form of the named init objects of the
  TypeScript API, and the reason the order of attributes never matters for a caller.
- **`slots`** keeps instances small and rejects attributes the type does not declare.
- A `@@nullable` attribute defaults to `None`; `@@default(v)` becomes the default value. A collection defaults to
  the empty immutable collection (`()`, `frozenset()`).
- **A type with a mutable attribute is not frozen.** Python cannot freeze some attributes of a dataclass and not
  others. Because a frozen dataclass cannot inherit from a non-frozen one (nor the other way round), the decision is
  made per inheritance tree: a tree of concrete types is frozen when none of them has a mutable attribute.
- **Collections are immutable types** (`tuple`, `frozenset`), so a value handed out cannot be changed through the
  reference. `Mapping` is a read-only *view* in the signature only; the dictionary behind it is shared.
- An attribute's documentation is an attribute docstring — a string literal directly below the attribute, which
  documentation tools and editors show.

**Inheritance between concrete types is nominal**: `PaidQueryResponse[T]` is a subclass of `QueryResponse[T]`,
declares only its own attributes and overrides the ones it narrows. A type may extend at most one concrete type.

## Imports and import cycles

Every module starts with `from __future__ import annotations`, so annotations are never evaluated at runtime. A type of
the SDK that only appears in annotations is imported under `if TYPE_CHECKING:` — the type checker sees it, the
interpreter never imports it — which is what lets two types refer to each other across modules without an import
cycle. A type the class statement itself evaluates is imported at runtime, from its private module: a base class
**including its type arguments** (`Query[Result, QueryResponse[Result]]` is evaluated when the class is created), an
enum value, a default value. The standard library modules with lowercase names are imported under an alias
(`import datetime as _dt`), because an attribute of the same name would shadow the module in the class body.

## Abstractions

An `abstraction` is a **`typing.Protocol`**: attributes become read-only properties (mutable ones plain attributes),
and `extends` between abstractions is protocol inheritance.

```python
class BaseAddress(Protocol):
    """An address of an entity on the ledger."""

    @property
    def shard(self) -> int: ...

    @property
    def realm(self) -> int: ...


class EvmCapableAddress(BaseAddress, Protocol):
    @property
    def evm_address(self) -> EvmAddress | None: ...
```

**A concrete type does not name the abstractions it implements; it satisfies them structurally.** The obvious
alternative — an `abc.ABC` with abstract properties that the dataclass inherits — does not work: the dataclass
machinery takes an inherited property as the *default value* of the attribute of the same name, and a property on the
base class intercepts the assignment in `__init__`. This is the lesson Go taught as well: the state is not inherited,
the concrete type carries all its effective attributes itself.

Consequences: `isinstance(x, BaseAddress)` needs `@runtime_checkable` and only checks the presence of the members;
the static checker verifies conformance where a value is assigned to the protocol type, not at the declaration.

An abstraction extending a concrete type has no Python form and is deferred.

## Sealed abstractions

A `@@sealed(A, B, …)` abstraction is a **type alias of the union** of the subtypes the annotation lists:

```python
type Authority = PublicKeyAuthority | ContractAuthority | AuthorityList
```

A `match` over it is exhaustive for the type checker (`typing.assert_never` in the last case), which is what sealing is
for.

The alias carries **no type parameters**, even when the abstraction is generic: a permitted subtype binds them (a
transaction binds its receipt and its self type), so the union of the subtypes is already the closed set. Shared
members need no separate protocol either — mypy resolves an attribute on a union when every member has it:

```python
type TokenMintTransaction = TokenMintFungibleTransaction | TokenMintNftTransaction

def target_token(mint: TokenMintTransaction) -> Address:
    return mint.token_id          # every variant has it
```

## Enumerations

An enum is an `enum.Enum`. The `valueOf` of the other languages is the lookup by name, `KeyAlgorithm["ED25519"]`, and
`values()` is iteration over the class — both shapes below support them. **Without attributes** the value of a member
is its name:

```python
class KeyAlgorithm(Enum):
    ED25519 = "ED25519"
    ECDSA = "ECDSA"
```

**With attributes** the value is a tuple of the name and the attributes, unpacked by `__init__` and read through
properties:

```python
class KeyFormat(Enum):
    PKCS8_WITH_DER = ("PKCS8_WITH_DER", KeyContainer.PKCS8, KeyEncoding.DER)

    def __init__(self, value_name: str, container: KeyContainer, encoding: KeyEncoding) -> None:
        self._container = container
        self._encoding = encoding

    @property
    def container(self) -> KeyContainer:
        return self._container
```

The name is part of the value because `Enum` treats two members with equal values as aliases of one member — two
statuses with the same attributes would otherwise collapse into one.

## Generics, self types and `ANY`

Python 3.12 declares type parameters in the class header (PEP 695) and infers their variance.

- **A bound is a bound**: `$$Receipt extends Receipt` is `[Receipt: Receipt]`. Unlike Go, Python can bound a
  parameter by a concrete class.
- **A bound cannot mention another type parameter.** `Query<Result, ResponseT extends QueryResponse<Result>>` has no
  Python form: a bound is not generic, so mypy rejects `[Result, ResponseT: QueryResponse[Result]]` with *Name
  "Result" is not defined*. The variables in such a bound become `Any` (`ResponseT: QueryResponse[Any]`) and the
  docstring says which constraint was lost — the same trade Go makes for its widened bounds.
- **A self type is dropped**, as in Go: `$$Self extends Transaction<$$Self, …>` exists only so that a setter can return
  the concrete type, and Python has `typing.Self` for exactly that.
- **A wildcard argument is `typing.Any`**: `Page<ANY>` is `Page[Any]`. A frozen dataclass with read-only attributes
  would be covariant and accept `Page[object]`, but a type with a mutable attribute is invariant, and `Any` is the one
  form that is right for both.
- **A type parameter never shadows a declared type**: such a name gets a `T` suffix.

## Methods and overloading

- Methods are instance methods with `snake_case` names; namespace-level functions are module-level functions of the
  namespace's package; factory types (`KeysFactory`) disappear into those functions.
- **Overloads** become one function with `typing.overload` declarations and keyword-only parameters where the
  overloads differ by meaning rather than by type, so that `create_private_key(value)` and
  `create_private_key(container=…, value=…)` stay one name — Python's idiom, unlike Go's distinct names.
- A setter of a mutable attribute is the attribute itself (plain assignment) on a non-frozen dataclass; the chaining
  form `with_memo(...) -> Self` is added only where the specs chain.

> **What the generator produces.** The overload implementation takes `*args: Any, **kwargs: Any` and returns the
> union of what the overloads return, which is the signature mypy accepts for every arm; keyword-only parameters per
> overload are not derived from the specs. A member named like a builtin - an attribute `bytes` - shadows it inside
> the class body, so annotations of that type are written as `_builtins.bytes`. A method body is
> `raise NotImplementedError("Not implemented yet: Type.method")`, the counterpart of the stubs the other generators
> emit.

## Errors

An error identifier becomes an exception class: `not-found-error` → `class NotFoundError(HieroError)`. Standard
conditions map to the built-ins: an illegal argument is `ValueError`, a missing value `TypeError`. `@@throws` is
documented in the docstring (`Raises:`), since Python has no checked exceptions.

## Asynchronous methods

`@@async` is `async def`, returning the awaited value. There is no synchronous twin: a caller without an event loop
uses `asyncio.run`. The declared `Cancellation` of `spec/base/http.md` wraps `asyncio` cancellation — task
cancellation raises `asyncio.CancelledError` into the awaiting coroutine — in the same way the Go binding wraps
`context.Context`.

## Streaming

`@@streaming` returns `collections.abc.AsyncIterator[T]`, consumed with `async for`; breaking out of the loop closes the
stream. `streamResult<T>` is `StreamItem[T]` (a value or an exception per item) from the hand-written support package.

## Constants

A constant is a module-level `typing.Final` in the namespace's package. Generated in a later step.

## Documentation and deprecation

The `## Description` of a spec becomes the package docstring, the comment above a declaration its docstring, written
in reStructuredText-compatible prose. `@@deprecated` adds a `.. deprecated::` paragraph; `warnings.deprecated`
(PEP 702) needs Python 3.13 and is used once the minimum version allows it.

## Thread safety

Python has no annotation for it. `@@threadSafe` becomes a sentence in the docstring. A frozen dataclass is safe to
share by construction.

## Support package

Types the generated API uses but no spec declares (`StreamItem`) live in a hand-written support distribution
(`hiero-sdk-support`, package `hiero_sdk_support`) — the Python counterpart of `sdk-java/support` and `sdk-ts/support`,
under [ADR-0007](../docs/adr/0007-separate-generated-and-hand-written-modules.md). Not needed yet: no generated type
uses one.

## Protobuf

The protobuf messages are generated from [`/tooling/protobuf`](../tooling/protobuf) into **`hiero_sdk._internal.proto`**.
The leading underscore is Python's only mechanism for "not public API" — a convention, not enforced — and `__all__`
does not list it.

## Tests

Generated tests (a later step) are `pytest` modules next to the package, under the same contract as the other
languages: construct from the `## Default Instances for Testing`, check the declared constraints, and fail for a method that is
not implemented yet.

## What the generator does today

`metalang generate --language=python` emits the distribution (`pyproject.toml`, `py.typed`), one package per
namespace, the declared types - dataclasses for concrete types, protocols for abstractions, union aliases for sealed
abstractions and both enum shapes - and their methods and functions: a protocol declares a member with `...`, a class
raises `NotImplementedError`, `@@async` is `async def`, `@@streaming` returns `AsyncIterator[T]`, a `@@static` method
is a `@staticmethod` and a namespace function a module-level function in `_functions.py`, re-exported by the package.
An overload set stays one name, with `typing.overload` declarations and one implementation. Constants, errors, tests,
the protobuf wiring and the TCK part are not generated yet. A declaration the generator cannot express is left out and reported by `--show-deferred`, so
`sdk-python/generated` always imports and passes `mypy --strict`; on the current specs nothing is deferred. The import
order and the blank lines follow isort/ruff, and `pyproject.toml` sets ruff's line length to 120.

## Questions & Comments

- **Constraints are not checked at runtime yet.** `@@min`, `@@max`, `@@pattern`, the integer range of an `int32`
  and the non-null requirement are visible to the type checker only as far as the type expresses them. A
  `__post_init__` that checks them is the obvious place; it is not generated yet (`@@maxBytes` would compare
  `len(value.encode("utf-8"))`, `@@maxLength` `len(value)`). — open
- **`Mapping` is a view, not a copy.** A caller keeps the dictionary it passed and can change it afterwards. A
  `types.MappingProxyType` over a copy would close that, at the cost of a less familiar runtime type. — open
- **One distribution or one per spec folder?** One distribution mirrors Go; one per folder (`hiero-base`,
  `hiero-consensus-node-client`, …) would mirror Maven, npm and Cargo and let an application depend on less. — open
- **Long lines in docstrings.** Prose is wrapped, but tables, lists and code blocks of the specs are kept verbatim,
  so `ruff check` reports `E501` for a few dozen docstring lines. Wrapping them would break the Markdown; exempting
  docstrings in the ruff configuration hides genuinely long lines elsewhere. — open
- **Protocols are structural.** A concrete type that drifts from an abstraction it is meant to implement is only
  noticed where a value is assigned to the abstraction. A generated test that assigns every concrete type to each of
  its abstractions would catch it at build time. — open
