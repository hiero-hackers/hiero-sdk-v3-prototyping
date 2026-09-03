## Why

To fully complete the TS SDK's foundational `base` implementation, we need to map the remaining networking, transport, and Hedera-specific configuration primitives defined in `spec/base/`. 

## What Changes

- Implement `ledger-config.md`: `NetworkSetting`, `Network`, `ConsensusNode`, `MirrorNode`
- Implement `hedera.md` & `solo.md`: `HederaNetworkSetting`, `HbarUnit`, `Hbar`, `SoloNetworkSetting`
- Implement `http.md` & `grpc.md`: `HttpClient`, `HttpRequest`, `HttpResponse`, `MethodDescriptor`

## Capabilities

### New Capabilities

### Modified Capabilities

## Impact

- Closes the final gaps in the `spec/base` implementation.
