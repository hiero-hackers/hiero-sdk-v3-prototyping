## Why

To interact with the Hedera consensus nodes and mirror nodes, the SDK needs to encode and decode Protobuf payloads and send them via gRPC. We need to integrate the Hedera protobuf definitions and implement the grpc transport channels exactly as they were done in the previous SDK, respecting the environment differences between Node and Web.

## What Changes

- Copy all `.proto` files from `sdk-ts/src/base/internal/proto-src` into `hiero-sdk-v3-ts/src/base/internal/proto-src`.
- Add `protobufjs` to compile the `.proto` files into static TypeScript definitions inside `hiero-sdk-v3-ts/src/base/internal/proto/`.
- Implement `Channel`, `NodeChannel`, and `WebChannel` inside `src/base/internal/channel/` following the exact architectural pattern used in `temp/hiero-sdk-js`, using `@grpc/grpc-js` for Node.js and `fetch` with gRPC-Web framing for Web.

## Capabilities

### New Capabilities

### Modified Capabilities

## Impact

- Equips the SDK with the compiled Hedera data structures and the transport layer required to send transactions and queries to the network, leveraging the battle-tested multi-channel approach of the V2 SDK.
