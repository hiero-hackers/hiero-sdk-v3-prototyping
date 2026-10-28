## Design

### Module Layout

```
sdk-ts/
  src/
    base/
      ledger.ts                ← public: Network<Unit> interface + createNetwork factory
      internal/
        ledger-impl.ts         ← internal: NetworkImpl concrete implementation
      index.ts                 ← re-exports all public types (extends existing)
    index.ts                   ← top-level re-export (extends existing)
  tests/
    contract/
      PublicApiShape.test.ts           ← public type shapes and generics
      NetworkContract.test.ts          ← behavioral contract tests
```

### Public API Surface

```typescript
// src/base/ledger.ts

import type { NativeTokenUnit } from "./nativeToken.js";

export interface Network<Unit extends NativeTokenUnit> {
  /** Ledger ID bytes — fresh defensive copy on every read. */
  readonly id: Uint8Array;
  /** Human-readable network name, or null if not set. */
  readonly name: string | null;
  /** The native token unit used by this network. */
  readonly nativeTokenUnit: Unit;
}

export function createNetwork<Unit extends NativeTokenUnit>(
  id: Uint8Array,
  name: string | null,
  nativeTokenUnit: Unit,
): Network<Unit>;
```

### Internal Implementation

```typescript
// src/base/internal/ledger-impl.ts

class NetworkImpl<Unit extends NativeTokenUnit> implements Network<Unit> {
  // Stores a defensive copy of id
  // Returns a fresh copy on every get
  // Validates null (not undefined) for name
}
```

### Key Decisions

| Decision | Rationale |
|---|---|
| `id` as `Uint8Array` with defensive copy | `bytes` maps naturally to `Uint8Array`; defensive copy prevents external mutation |
| `name: string \| null` | `null` for intentional absence; `undefined` never appears in public API |
| Interface + factory pattern | Hides `NetworkImpl` details; factory is the single construction point |
| Validation in factory | Fail fast on invalid inputs before any state is stored |
