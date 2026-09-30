## Why

Applications need a consistent way to understand the current exchange value of a network's native token and whether
that information is still valid. A shared definition prevents different SDKs from presenting conflicting behavior to
their users.

## What Changes

- Establish a shared exchange-rate capability for supported SDKs.
- Make the exchange value and its validity consistently available to SDK users.
- Provide a common basis for SDK-specific exchange-rate work.

## Capabilities

### New Capabilities

- `exchange-rate`: Defines how SDK users access exchange-rate information and determine whether it is still valid.

### Modified Capabilities

## Impact

- SDK users receive consistent exchange-rate behavior across supported SDKs.
- SDK teams receive a shared scope for their exchange-rate changes.
- Existing applications can use the validity information to avoid relying on expired exchange rates.
