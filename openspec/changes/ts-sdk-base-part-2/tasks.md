## 1. Network Settings and Ledger Config

- [x] 1.1 Implement `NetworkSetting` from `ledger-config.md` in `src/base/LedgerConfig.ts` (Requires stubbing `Network`, `ConsensusNode`, `MirrorNode`).
- [x] 1.2 Implement `HederaNetworkSetting`, `HbarUnit`, `Hbar` from `hedera.md` and `SoloNetworkSetting` from `solo.md` in `src/base/Hedera.ts` and `src/base/Solo.ts`.

## 2. Transport Abstractions

- [x] 2.1 Implement `HttpClient`, `HttpRequest`, `HttpResponse` from `http.md` in `src/base/Http.ts`.
- [x] 2.2 Implement `MethodDescriptor` from `grpc.md` in `src/base/Grpc.ts`.
- [x] 2.3 Create `src/base/Proto.ts` with placeholder proto definitions as per `proto.md`.

## 3. Integration & Testing

- [x] 3.1 Update `src/base/index.ts` to export these new primitives while adhering to the encapsulation rules.
- [x] 3.2 Run `tsc` and ensure everything compiles successfully.
