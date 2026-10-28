import { describe, it, expect } from "vitest";
import { GenericNativeToken } from "../../src/base/internal/nativeToken-impl.js";
import type { NativeTokenUnit } from "../../src/base/index.js";

// ---------------------------------------------------------------------------
// Test fixtures
// ---------------------------------------------------------------------------

const TINYBAR: NativeTokenUnit = Object.freeze({ symbol: "tℏ", baseUnitFactor: 1n });
const HBAR: NativeTokenUnit = Object.freeze({ symbol: "ℏ", baseUnitFactor: 100_000_000n });
const KILOBAR: NativeTokenUnit = Object.freeze({ symbol: "kℏ", baseUnitFactor: 100_000_000_000n });

// ---------------------------------------------------------------------------
// Immutability
// ---------------------------------------------------------------------------

describe("NativeToken immutability", () => {
  it("amount is immutable (cannot be reassigned)", () => {
    const t = GenericNativeToken.create(5n, HBAR);
    expect(() => {
      // @ts-expect-error: readonly
      t.amount = 99n;
    }).toThrow();
  });

  it("unit is immutable (cannot be reassigned)", () => {
    const t = GenericNativeToken.create(5n, HBAR);
    expect(() => {
      // @ts-expect-error: readonly
      t.unit = TINYBAR;
    }).toThrow();
  });

  it("instance is frozen", () => {
    const t = GenericNativeToken.create(5n, HBAR);
    expect(Object.isFrozen(t)).toBe(true);
  });
});

// ---------------------------------------------------------------------------
// Unit conversion
// ---------------------------------------------------------------------------

describe("NativeToken.to() unit conversion", () => {
  it("converts from HBAR to TINYBAR: 1 ℏ = 100_000_000 tℏ", () => {
    const t = GenericNativeToken.create(1n, HBAR);
    const result = t.to(TINYBAR);
    expect(result.amount).toBe(100_000_000n);
    expect(result.unit).toBe(TINYBAR);
  });

  it("converts from TINYBAR to HBAR: 100_000_000 tℏ = 1 ℏ", () => {
    const t = GenericNativeToken.create(100_000_000n, TINYBAR);
    const result = t.to(HBAR);
    expect(result.amount).toBe(1n);
    expect(result.unit).toBe(HBAR);
  });

  it("converts from HBAR to KILOBAR: 1000 ℏ = 1 kℏ", () => {
    const t = GenericNativeToken.create(1000n, HBAR);
    const result = t.to(KILOBAR);
    expect(result.amount).toBe(1n);
    expect(result.unit).toBe(KILOBAR);
  });

  it("converts from KILOBAR to TINYBAR: 1 kℏ = 100_000_000_000 tℏ", () => {
    const t = GenericNativeToken.create(1n, KILOBAR);
    const result = t.to(TINYBAR);
    expect(result.amount).toBe(100_000_000_000n);
    expect(result.unit).toBe(TINYBAR);
  });

  it("to() same unit is a no-op in value", () => {
    const t = GenericNativeToken.create(42n, HBAR);
    const result = t.to(HBAR);
    expect(result.amount).toBe(42n);
    expect(result.unit).toBe(HBAR);
  });

  it("to() returns a new instance (not same reference)", () => {
    const t = GenericNativeToken.create(1n, HBAR);
    const result = t.to(HBAR);
    expect(result).not.toBe(t);
  });

  it("to() rejects null targetUnit", () => {
    const t = GenericNativeToken.create(1n, HBAR);
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    expect(() => t.to(null as any)).toThrow();
  });
});

// ---------------------------------------------------------------------------
// toBaseUnits()
// ---------------------------------------------------------------------------

describe("NativeToken.toBaseUnits()", () => {
  it("1 HBAR in tinybars = 100_000_000", () => {
    const t = GenericNativeToken.create(1n, HBAR);
    expect(t.toBaseUnits()).toBe(100_000_000n);
  });

  it("5 TINYBAR in base units = 5", () => {
    const t = GenericNativeToken.create(5n, TINYBAR);
    expect(t.toBaseUnits()).toBe(5n);
  });

  it("0 HBAR in tinybars = 0", () => {
    const t = GenericNativeToken.create(0n, HBAR);
    expect(t.toBaseUnits()).toBe(0n);
  });

  it("handles amounts exceeding Number.MAX_SAFE_INTEGER (50B HBAR = 5e18 tinybars)", () => {
    // 50,000,000,000 HBAR * 100_000_000 tinybars/HBAR = 5_000_000_000_000_000_000
    const t = GenericNativeToken.create(50_000_000_000n, HBAR);
    expect(t.toBaseUnits()).toBe(5_000_000_000_000_000_000n);
  });

  it("negative amount gives negative base units", () => {
    const t = GenericNativeToken.create(-3n, HBAR);
    expect(t.toBaseUnits()).toBe(-300_000_000n);
  });
});

// ---------------------------------------------------------------------------
// Runtime validation
// ---------------------------------------------------------------------------

describe("NativeToken constructor validation", () => {
  it("rejects null amount", () => {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    expect(() => GenericNativeToken.create(null as any, HBAR)).toThrow();
  });

  it("rejects undefined amount", () => {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    expect(() => GenericNativeToken.create(undefined as any, HBAR)).toThrow();
  });

  it("rejects null unit", () => {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    expect(() => GenericNativeToken.create(1n, null as any)).toThrow();
  });

  it("rejects undefined unit", () => {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    expect(() => GenericNativeToken.create(1n, undefined as any)).toThrow();
  });

  it("rejects non-bigint amount (number)", () => {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    expect(() => GenericNativeToken.create(1 as any, HBAR)).toThrow();
  });

  it("rejects unit with baseUnitFactor = 0", () => {
    const badUnit: NativeTokenUnit = { symbol: "bad", baseUnitFactor: 0n };
    expect(() => GenericNativeToken.create(1n, badUnit)).toThrow();
  });

  it("rejects unit with negative baseUnitFactor", () => {
    const badUnit: NativeTokenUnit = { symbol: "bad", baseUnitFactor: -1n };
    expect(() => GenericNativeToken.create(1n, badUnit)).toThrow();
  });
});
