import { describe, expect, it } from "vitest";
import { NativeToken, NativeTokenUnit } from "../src/index.js";
import { OtherUnit, TestToken, TestUnit } from "./test-token.js";

const INT64_MAX = 2n ** 63n - 1n;
const INT64_MIN = -(2n ** 63n);

describe("NativeTokenUnit", () => {
  it("should expose its symbol and base unit factor", () => {
    const unit = new TestUnit("ℏ", 100_000_000n);

    expect(unit.symbol).toBe("ℏ");
    expect(unit.baseUnitFactor).toBe(100_000_000n);
    expect(unit).toBeInstanceOf(NativeTokenUnit);
  });

  it("should allow an empty symbol and the int64 maximum factor", () => {
    const unit = new TestUnit("", INT64_MAX);
    expect(unit.symbol).toBe("");
    expect(unit.baseUnitFactor).toBe(INT64_MAX);
  });

  it.each([null, undefined, 1])("should reject a non-string symbol %#", (symbol) => {
    expect(() => new TestUnit(symbol as unknown as string, 1n)).toThrow(TypeError);
  });

  it.each([1, "1", null, undefined])("should reject a non-bigint factor %#", (factor) => {
    expect(() => new TestUnit("t", factor as unknown as bigint)).toThrow(TypeError);
  });

  it.each([0n, -1n, INT64_MIN, INT64_MAX + 1n])("should reject the factor %s", (factor) => {
    expect(() => new TestUnit("t", factor)).toThrow(RangeError);
  });

  it("should not allow reassigning its state", () => {
    const unit = new TestUnit("t", 1n);

    expect(() => {
      (unit as { symbol: string }).symbol = "x";
    }).toThrow(TypeError);
    expect(() => {
      (unit as { baseUnitFactor: bigint }).baseUnitFactor = 2n;
    }).toThrow(TypeError);
    expect(unit.symbol).toBe("t");
    expect(unit.baseUnitFactor).toBe(1n);
  });
});

describe("NativeToken state", () => {
  it("should expose its amount and unit", () => {
    const token = new TestToken(42n, TestUnit.CENT);

    expect(token.amount).toBe(42n);
    expect(token.unit).toBe(TestUnit.CENT);
    expect(token).toBeInstanceOf(NativeToken);
  });

  it("should accept negative amounts and the int64 boundaries", () => {
    expect(new TestToken(-5n, TestUnit.BASE).amount).toBe(-5n);
    expect(new TestToken(INT64_MIN, TestUnit.BASE).amount).toBe(INT64_MIN);
    expect(new TestToken(INT64_MAX, TestUnit.BASE).amount).toBe(INT64_MAX);
  });

  it.each([1, "1", null, undefined])("should reject a non-bigint amount %#", (amount) => {
    expect(() => new TestToken(amount as unknown as bigint, TestUnit.BASE)).toThrow(TypeError);
  });

  it.each([INT64_MIN - 1n, INT64_MAX + 1n])("should reject the amount %s", (amount) => {
    expect(() => new TestToken(amount, TestUnit.BASE)).toThrow(RangeError);
  });

  it.each([null, undefined, { symbol: "t", baseUnitFactor: 1n }])("should reject a non-unit %#", (unit) => {
    expect(() => new TestToken(1n, unit as unknown as TestUnit)).toThrow(TypeError);
  });

  it("should not allow reassigning its state", () => {
    const token = new TestToken(1n, TestUnit.BASE);

    expect(() => {
      (token as { amount: bigint }).amount = 2n;
    }).toThrow(TypeError);
    expect(() => {
      (token as { unit: TestUnit }).unit = TestUnit.CENT;
    }).toThrow(TypeError);
    expect(token.amount).toBe(1n);
    expect(token.unit).toBe(TestUnit.BASE);
  });
});

describe("NativeToken.toBaseUnits", () => {
  it.each([
    [2n, TestUnit.WHOLE, 200_000_000n],
    [3n, TestUnit.CENT, 300n],
    [7n, TestUnit.BASE, 7n],
    [-4n, TestUnit.CENT, -400n],
    [0n, TestUnit.WHOLE, 0n],
  ])("should convert %s to base units", (amount, unit, expected) => {
    expect(new TestToken(amount, unit).toBaseUnits()).toBe(expected);
  });
});

describe("NativeToken.to", () => {
  it("should convert exactly to a smaller unit", () => {
    const converted = new TestToken(2n, TestUnit.WHOLE).to(TestUnit.BASE);

    expect(converted.amount).toBe(200_000_000n);
    expect(converted.unit).toBe(TestUnit.BASE);
  });

  it("should convert exactly to a larger unit", () => {
    const converted = new TestToken(300n, TestUnit.BASE).to(TestUnit.CENT);

    expect(converted.amount).toBe(3n);
    expect(converted.unit).toBe(TestUnit.CENT);
  });

  it("should convert negative amounts exactly", () => {
    expect(new TestToken(-300n, TestUnit.BASE).to(TestUnit.CENT).amount).toBe(-3n);
  });

  it.each([150n, -150n, 1n])("should reject the inexact conversion of %s", (amount) => {
    expect(() => new TestToken(amount, TestUnit.BASE).to(TestUnit.CENT)).toThrow(RangeError);
  });

  it("should reject a converted amount outside the int64 range", () => {
    expect(() => new TestToken(INT64_MAX, TestUnit.CENT).to(TestUnit.BASE)).toThrow(RangeError);
  });

  it("should return a new instance of the concrete type without modifying the source", () => {
    const source = new TestToken(5n, TestUnit.CENT);
    const same = source.to(TestUnit.CENT);
    const converted = source.to(TestUnit.BASE);

    expect(converted).toBeInstanceOf(TestToken);
    expect(same).toBeInstanceOf(TestToken);
    expect(same).not.toBe(source);
    expect(same.amount).toBe(5n);
    expect(source.amount).toBe(5n);
    expect(source.unit).toBe(TestUnit.CENT);
  });

  it("should round-trip through base units", () => {
    const token = new TestToken(12n, TestUnit.WHOLE);
    expect(token.to(TestUnit.BASE).to(TestUnit.WHOLE).amount).toBe(12n);
    expect(token.to(TestUnit.CENT).toBaseUnits()).toBe(token.toBaseUnits());
  });
});

describe("Unit separation", () => {
  it("should keep foreign units distinct at runtime", () => {
    expect(OtherUnit.BASE).not.toBeInstanceOf(TestUnit);
  });
});
