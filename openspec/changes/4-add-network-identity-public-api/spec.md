## Purpose

Define the language-neutral contract for network identity, ensuring all SDKs share a consistent model for tracking a network's ledger ID, its name, and its native token unit. SDK-specific OpenSpec changes use this document as their behavioral starting point and add only language mapping requirements.

## ADDED Requirements

### Requirement: Immutable network identity state

The SDK SHALL expose a `Network<$$Unit>` type containing immutable `id`, `name`, and `nativeTokenUnit` values. The `$$Unit` generic parameter MUST extend `NativeTokenUnit` (as defined in the `native-token` capability).

#### Scenario: Network identity state is observed

- **Given** an instantiated network identity object.
- **When** a caller reads the network state.
- **Then** the caller receives the byte array `id`, the optional string `name`, and the `NativeTokenUnit` instance without being able to modify the network's stored state.

## Language-Neutral API Schema

```text
namespace ledger
requires {NativeTokenUnit} from nativeToken

// Represents the identity of a network, including its ledger ID, name, and the native token unit it uses.
Network<$$Unit extends NativeTokenUnit> {
    @@immutable id: bytes // identifier of the network
    @@immutable @@nullable name: string // human readable name of the network
    @@immutable nativeTokenUnit: $$Unit
}
```
