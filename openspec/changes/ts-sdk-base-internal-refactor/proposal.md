## Why

To enforce a strict physical boundary between public API definitions (interfaces, types, enums) and their concrete implementations, we want to move all private implementations into a dedicated `internal/` directory within `src/base`.

## What Changes

- Move the `crypto/` implementation folder to `src/base/internal/crypto/`.
- Extract the `DefaultHttpClient` implementation from `src/base/Http.ts` and move it to `src/base/internal/http/DefaultHttpClient.ts`.
- Update the public factory methods in `src/base/Keys.ts` and `src/base/Http.ts` to import from the `internal/` directory.

## Capabilities

### New Capabilities

### Modified Capabilities

## Impact

- Improves code organization and clearly delineates the SDK's public contract from its private internals.
- No changes to the public API surface.
