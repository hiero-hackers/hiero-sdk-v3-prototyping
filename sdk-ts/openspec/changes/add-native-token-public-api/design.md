## Design

### Module Layout

```
sdk-ts/
  src/
    base/
      nativeToken.ts           ← public: NativeTokenUnit interface + NativeToken abstract class
      internal/
        nativeToken-impl.ts    ← internal: GenericNativeToken<Unit> concrete implementation
      index.ts                 ← re-exports all public types
    index.ts                   ← top-level re-export
  tests/
    contract/
      NativeTokenPublicApiShape.test.ts  ← public type shapes and generics
      NativeTokenContract.test.ts        ← behavioral contract tests
```

### Public API Surface

```typescript
// src/base/nativeToken.ts

export interface NativeTokenUnit {
  readonly symbol: string;
  readonly baseUnitFactor: bigint;
}

export abstract class NativeToken<
  Self extends NativeToken<Self, Unit>,
  Unit extends NativeTokenUnit
> {
  readonly amount: bigint;
  readonly unit: Unit;

  protected constructor(amount: bigint, unit: Unit);

  to(targetUnit: Unit): Self;
  toBaseUnits(): bigint;
}
```

### Internal Implementation

```typescript
// src/base/internal/nativeToken-impl.ts

class GenericNativeToken<Unit extends NativeTokenUnit>
  extends NativeToken<GenericNativeToken<Unit>, Unit> {

  static create<U extends NativeTokenUnit>(amount: bigint, unit: U): GenericNativeToken<U>;
}
```

### Key Decisions

| Decision | Rationale |
|---|---|
| `amount: bigint` | `int64` maps to `bigint`; 50B HBAR = 5×10¹⁸ tinybars > `Number.MAX_SAFE_INTEGER` |
| Abstract class over interface | Allows shared `to()` / `toBaseUnits()` logic; consumers subclass for concrete tokens |
| `Object.freeze()` in constructor | Enforces runtime immutability for the public contract |
| Keep `GenericNativeToken` internal | Public API exposes only abstract types; internal factory used for unit tests |
