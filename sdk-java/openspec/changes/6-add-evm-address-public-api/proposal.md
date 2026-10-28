## Why

Applications that interact with EVM-compatible networks need consistent address behavior across supported SDKs.
Without a shared definition, users can encounter different results when exchanging address information between SDKs
and external tools.

## What Changes

- Establish a shared EVM-address capability for supported SDKs.
- Make creating and displaying EVM addresses consistent for SDK users.
- Provide consistent handling when supplied address information is not valid.

## Capabilities

### New Capabilities

- `evm-address`: Defines how SDK users create, retain, and display EVM addresses and how invalid input is reported.

### Modified Capabilities

## Impact

- SDK users receive consistent EVM-address behavior across supported SDKs.
- Applications can exchange EVM addresses with less ambiguity.
- SDK teams receive a shared scope for their EVM-address changes.
