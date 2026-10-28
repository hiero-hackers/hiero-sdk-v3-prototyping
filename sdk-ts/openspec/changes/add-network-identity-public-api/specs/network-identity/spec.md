## Purpose

TypeScript contract mapping for the language-neutral `add-network-identity-public-api` specification.
All behavioral requirements from the common spec are preserved; this document adds only TypeScript-specific
type mappings and constraints.

## Type Mappings

| Common type    | TypeScript type      | Notes |
|----------------|----------------------|-------|
| `bytes`        | `Uint8Array`         | Defensive copy on construction and on every read |
| `string?`      | `string \| null`     | `null` represents intentional absence; `undefined` is never used |
| `NativeTokenUnit` | `NativeTokenUnit` | Imported from `add-native-token-public-api` |

## ADDED Requirements

### Requirement: Immutable network identity state

The SDK SHALL expose a `Network<Unit extends NativeTokenUnit>` interface with readonly `id: Uint8Array`,
`name: string | null`, and `nativeTokenUnit: Unit`. No property may be externally mutated.

#### Scenario: Network identity state is observed

- **Given** a `Network` instance created via `createNetwork`.
- **When** a caller reads `id`, `name`, and `nativeTokenUnit`.
- **Then** the caller receives an immutable `Uint8Array`, a `string | null`, and the `NativeTokenUnit` respectively, without being able to modify the stored state.

### Requirement: Defensive copy of id bytes

The SDK SHALL defensively copy the `id` byte array on construction and return a fresh copy on every read,
so that mutation of the caller's array cannot affect the stored identity, and vice versa.

#### Scenario: Input byte array is mutated after construction

- **Given** a `Network` created from a byte array `inputId`.
- **When** a caller mutates `inputId` after construction.
- **Then** `network.id` still returns the original bytes, unaffected.

#### Scenario: Output byte array is mutated after read

- **Given** a `Network` instance.
- **When** a caller reads `network.id` and mutates the returned array.
- **Then** a subsequent read of `network.id` still returns the original bytes, unaffected.

### Requirement: Null for absent name

The SDK SHALL represent an absent network name as `null`, never `undefined`.

#### Scenario: Network is created without a name

- **Given** a `Network` created with no name.
- **When** a caller reads `network.name`.
- **Then** the value is `null`.

#### Scenario: Undefined name is rejected

- **Given** a call to `createNetwork` with `undefined` as the name.
- **When** the factory runs.
- **Then** an error is thrown and no instance is created.
