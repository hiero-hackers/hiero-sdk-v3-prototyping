## 1. Setup Protobuf Definitions

## 1. Setup Protobuf Definitions

- [x] 1.1 Copy all `.proto` files from `sdk-ts/src/base/internal/proto-src` to `hiero-sdk-v3-ts/src/base/internal/proto-src`.
- [x] 1.2 Install `protobufjs`, `protobufjs-cli`, and `@grpc/grpc-js` as dependencies in `hiero-sdk-v3-ts`.
- [x] 1.3 Add a script to `package.json` to compile the `.proto` files to `src/base/internal/proto/proto.js` and `proto.d.ts`.
- [x] 1.4 Execute the compilation script.

## 2. Implement Channels

- [x] 2.1 Implement `src/base/internal/channel/Channel.ts` mirroring the V2 SDK's abstract class.
- [x] 2.2 Implement `src/base/internal/channel/NodeChannel.ts` using `@grpc/grpc-js` mimicking the V2 SDK.
- [x] 2.3 Implement `src/base/internal/channel/WebChannel.ts` using `fetch` mimicking the V2 SDK's gRPC-Web framing logic.

## 3. Update Public API and Integration

- [x] 3.1 Export `MethodDescriptor` in `src/base/Grpc.ts` (already done, but verify compliance).
- [x] 3.2 Verify the `Proto.ts` placeholder exports the compiled `.proto` definitions or leave them internal to the channel if preferred by the SDK design.
- [x] 3.3 Run `npx tsc` to verify the channels compile successfully.
