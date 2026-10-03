# GRPC API

This section defines the GRPC API.

## Description

The SDK communicates with the consensus nodes of a network via gRPC and Protobuf. This package holds
the gRPC-related types that are part of the public API, such as `MethodDescriptor`, which identifies
a single gRPC method; it is mainly relevant when extending the SDK with custom services or
transaction types.

## Design Notes

`MethodDescriptor` is a minimal placeholder to express the SPI dependency on gRPC. Concrete
transport-layer details are language and runtime specific.

## API Schema

```
namespace grpc

// Identifies a gRPC method by the name of its service and the name of the method.
abstraction MethodDescriptor {
    @@immutable serviceName: string // name of the gRPC service
    @@immutable methodName: string // name of the method within the service
}
```

## Questions & Comments
