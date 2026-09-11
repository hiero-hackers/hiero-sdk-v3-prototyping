## Context

Testing network transport logic (like gRPC and HTTP) can often bleed into integration testing if not carefully scoped. We will strictly write **unit tests only**.

## Goals / Non-Goals

**Goals:**
- Write pure unit tests for all `src/base` typescript files.
- Validate `WebChannel` gRPC-Web binary framing algorithms using mocked inputs.
- Validate `NodeChannel` gRPC-js integration using mocked clients.

**Non-Goals:**
- No End-to-End (E2E) testing against actual Hedera network endpoints.
- No integration testing across layers. All dependencies (like `fetch` or `@grpc/grpc-js`) will be stubbed or mocked.

## Decisions

### 1. Pure Mocking Strategies
**Decision:** 
- For `WebChannel` and `DefaultHttpClient`, we will intercept `global.fetch` using `vi.spyOn` or `vi.fn()` to simulate binary responses and network errors instantaneously.
- For `NodeChannel`, we will use `vi.mock('@grpc/grpc-js')` to intercept unary request executions and verify the exact bytes being framed and passed.
