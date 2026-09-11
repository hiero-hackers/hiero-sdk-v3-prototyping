## Context

The `grpc.md` spec provides a placeholder for `MethodDescriptor`, leaving the implementation to the runtime environment. We will follow the previous SDK's architecture for handling gRPC channels across different environments.

## Goals / Non-Goals

**Goals:**
- Use `protobufjs` to statically compile the Hedera `.proto` definitions.
- Implement the transport layer using `Channel`, `NodeChannel`, and `WebChannel` exactly like the previous SDK.

**Non-Goals:**
- Inventing new abstractions like `GrpcWebClient`. We will strictly adhere to the `Channel` hierarchy established in the V2 SDK.

## Decisions

### 1. Protobuf Compilation
**Decision:** We will use `pbjs` and `pbts` from `protobufjs-cli` to compile the `.proto` files into a static `proto.js` and `proto.d.ts` module.

### 2. Channel Architecture
**Decision:** We will implement an internal `channel` directory with:
- `Channel.ts`: Abstract base class holding references to compiled proto services.
- `NodeChannel.ts`: Implementation using `@grpc/grpc-js`.
- `WebChannel.ts`: Implementation using browser `fetch` and gRPC-Web framing.
This mirrors the V2 SDK directly, retaining compatibility and proven behavior for each environment.
