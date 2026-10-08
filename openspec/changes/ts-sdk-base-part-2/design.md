## Context

We are implementing the remainder of `spec/base` which focuses heavily on transport primitives (`http.md`, `grpc.md`) and network configurations.

## Goals / Non-Goals

**Goals:**
- Provide precise TypeScript abstractions for the missing interfaces.

**Non-Goals:**
- Implementing the actual underlying HTTP or GRPC transport clients (e.g. `fetch` wrappers). We are just defining the abstractions.

## Decisions

### 1. Abstracting Transport Clients
**Decision:** We will strictly type `HttpClient` and `HttpRequest/Response` without importing external library types to keep the layer "payload-agnostic and semantics-free" as per `http.md`.
