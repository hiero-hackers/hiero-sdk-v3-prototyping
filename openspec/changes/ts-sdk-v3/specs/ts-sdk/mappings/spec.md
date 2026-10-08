## Purpose
Defines the strict requirements for how language-agnostic meta-language constructs from the V3 SDK spec map directly into pure TypeScript implementations.

## ADDED Requirements

### Requirement: Immutability mapping
The SDK SHALL map `@@immutable` properties to `readonly` fields or accessor-only getter properties in TypeScript, ensuring values cannot be mutated after instantiation.

#### Scenario: Object construction
- **WHEN** an object with immutable properties is instantiated
- **THEN** the properties are read-only and attempts to reassign them throw a TypeScript compiler error

### Requirement: Discriminated union mapping
The SDK SHALL map the `@@sealed` and `@@oneOf` annotations into idiomatic TypeScript discriminated unions, allowing for exhaustive switch/pattern matching on types.

#### Scenario: Pattern matching sealed types
- **WHEN** a consumer evaluates a sealed type variable in a switch statement
- **THEN** TypeScript enforces exhaustive checking across all variants

### Requirement: Stream mapping
The SDK SHALL map `@@streaming` methods to AsyncGenerators or standard async iterators (`AsyncIterable<streamResult<T>>`), rather than arrays or promises, ensuring pull-based iteration.

#### Scenario: Streaming async iteration
- **WHEN** a consumer invokes a streaming method
- **THEN** the consumer can iterate through elements using `for await...of` without buffering the entire result set in memory

### Requirement: Numeric precision mapping
The SDK SHALL map 64-bit integers (`int64`, `uint64`) to `BigInt` to prevent safe-integer precision loss in JS.

#### Scenario: Passing 64-bit values
- **WHEN** a numeric value exceeding `Number.MAX_SAFE_INTEGER` is passed to the SDK
- **THEN** the SDK preserves precision through `BigInt` representation
