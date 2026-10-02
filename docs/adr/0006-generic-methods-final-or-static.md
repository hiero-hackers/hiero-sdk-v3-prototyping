# ADR-0006: Generic methods are allowed only as `@@finalMethod` instance methods or as `@@static` methods

**Status:** Proposed
**Date:** 2026-10-02

## Context

The V3 API is specified once in the language-agnostic meta-language (`guidelines/api-guideline.md`) and must be
translatable deterministically into Java, JavaScript/TypeScript, Go, Rust, Python, C++ and Swift. Until now the
meta-language could declare generic type parameters only on types (`Factory<$$Product>`), not on methods.

### The specs already needed generic methods

Without a syntax for them, spec authors worked around the gap by writing bounds where a type parameter is *used*,
e.g. `PackedTransaction<$$Receipt extends Receipt, $$Transaction extends Transaction<$$Receipt>> pack(...)` in
`Transaction`, and `@@static Response<$$Receipt> getResponse(...)` with an undeclared `$$Receipt`. The prototype
validator (`tooling/metalang`) reported 24 such use-site bounds and one undeclared generic parameter, all in
`spec/consensus-node-client/transactions.md`. Analysing them showed three different needs: repeated bounds of the
type's own parameters, a "self type" that depends on the object (now solved with a type parameter on the type,
`spec/consensus-node-client/transactions.md:134`, following the existing pattern in `spec/base/native-token.md:20`),
and one genuine per-call generic method (`getResponse`, `spec/consensus-node-client/transactions.md:258`).

### Overridable generic methods are not portable

An unrestricted generic method can be declared on an abstraction and overridden by every subtype. Three target
languages cannot express that without runtime casts:

- **Go:** "Interface methods cannot declare type parameters" (Go spec, *Interface types*), and since Go 1.27, which
  added generic methods on concrete types, "interface methods [cannot] be implemented by generic methods" (Go 1.27
  release notes).
- **C++:** "A member function template cannot be virtual" (cppreference, *Member templates*).
- **Rust:** a method with type parameters is not dispatchable; it stays in a dyn-compatible trait only with
  `where Self: Sized`, i.e. it cannot be called on `dyn Trait` (Rust Reference, *Traits — Dyn compatibility*).

All three only fail for *dispatch* on the generic method. Static generic functions and non-dispatched generic methods
are expressible in every target language.

## Decision

We allow generic methods in exactly two forms, with the syntax `ReturnType name<$$T extends B>(...)`:

1. **`@@finalMethod` instance methods** — `@@finalMethod $$T convert<$$T>(x: X)`. The new annotation
   `@@finalMethod` means: the declaring type implements the method and subtypes must not override it.
2. **`@@static` methods and namespace-level functions** — `@@static $$T convert<$$T>(target: Obj, x: X)`.

If the behaviour must differ per subtype, the type parameter belongs on the type (`abstraction Codec<$$T>`), which is
portable to all target languages. A per-call generic method whose implementation differs per subtype is deliberately
not expressible.

Supporting rules: bounds are declared only where a type parameter is declared (use-site bounds are errors); a method's
type parameters must not shadow those of its type; `type<T>` is added as a typed type token.

Because `@@finalMethod` requires an implementation in the declaring type, an abstraction that declares one maps to an
**abstract class** in Java/Kotlin/C# (Java interface methods may not be declared `final`, JLS §9.4). Therefore a type
must not inherit `@@finalMethod` methods from two unrelated abstractions.

**Rejected alternatives:**

- *Allow overridable generic methods; lower them in Go/C++/Rust to an erased core method plus a casting generic
  wrapper.* Rejected: the erased method would become hidden API surface that the conformance checker must know,
  type errors move from compile time to runtime in three languages, and the API shape differs per language.
- *Forbid generic instance methods entirely (only `@@static`).* Rejected: languages that support them lose the
  method-call form for no portability gain, since `@@finalMethod` methods are portable.

## Consequences

### Positive

- Every allowed form has a compile-time-typed mapping in all target languages; no runtime casts are needed
  (mapping table in `guidelines/api-guideline.md:357` ff.).
- The rules are mechanically checkable: the validator reports `generic.method-not-final`,
  `method.final-overridden`, `method.final-multiple-inheritance` and `syntax.use-site-bound`
  (`tooling/metalang/src/main/java/org/hiero/sdk/v3/metalang/diagnostic/Rule.java:42`, `:114`–`:118`).
- Relaxing the rule later (e.g. allowing overridable generic methods) would not break existing specs; tightening it
  would.

### Negative

- Designers must choose between a type parameter on the type and a non-overridable generic method; some designs
  natural in Java (overridable generic methods) cannot be expressed.
- In Java/Kotlin/C#, an abstraction with a `@@finalMethod` becomes an abstract class, which costs the single
  superclass slot of its implementations.
- TypeScript has no `final` modifier (TypeScript handbook, *Classes*; feature request closed as "Won't Fix",
  microsoft/TypeScript#8306). There, `@@finalMethod` is only a documented contract that the future API conformance
  checker has to enforce.
- Go needs two mappings: native generic methods on concrete types (Go 1.27+) and package-level functions
  `<TypeName><MethodName>` for abstractions.

### Follow-ups

- Add the Go, C++, TypeScript, Python and Swift mappings to their best-practice guides once those guides exist
  (Java and Rust are documented: `guidelines/api-best-practices-java.md:349`, `guidelines/api-best-practices-rust.md`).
- The API conformance checker must detect overrides of `@@finalMethod` methods in TypeScript (and in Java/Kotlin
  implementations that bypass the abstract class).
- Revisit if Go, C++ or Rust gain dispatchable generic methods on interfaces/virtual functions/trait objects; a
  change in only one of them is not sufficient.

---

**References:**

- `guidelines/api-guideline.md:48` (`type<TYPE>`), `:357` (*Generic methods*), `:586` (`@@finalMethod`)
- `guidelines/api-best-practices-java.md:349`; `guidelines/api-best-practices-rust.md` (*Generic Methods and
  `@@finalMethod`*)
- `spec/consensus-node-client/transactions.md:134`, `:189`, `:258`; `spec/base/native-token.md:20`
- `tooling/metalang/src/main/antlr4/org/hiero/sdk/v3/metalang/grammar/MetaLang.g4:79`–`:92`;
  `tooling/metalang/src/main/java/org/hiero/sdk/v3/metalang/validation/GenericMethodCheck.java`
- The Go Programming Language Specification, *Interface types* and *Method declarations*,
  https://go.dev/ref/spec (accessed 2026-10-02)
- Go 1.27 Release Notes, *Generic methods*, https://go.dev/doc/go1.27 (accessed 2026-10-02)
- cppreference, *Member templates*, https://en.cppreference.com/w/cpp/language/member_template
  (accessed 2026-10-02)
- The Rust Reference, *Traits — Dyn compatibility*, https://doc.rust-lang.org/reference/items/traits.html
  (accessed 2026-10-02)
- The Java Language Specification, Java SE 21, §9.4 *Method Declarations*,
  https://docs.oracle.com/javase/specs/jls/se21/html/jls-9.html (accessed 2026-10-02)
- TypeScript Handbook, *Classes*, https://www.typescriptlang.org/docs/handbook/2/classes.html; microsoft/TypeScript#8306
  "Support final classes (non-subclassable)", https://github.com/microsoft/TypeScript/issues/8306 (accessed 2026-10-02)
