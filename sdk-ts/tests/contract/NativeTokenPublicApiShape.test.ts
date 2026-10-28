import { describe, it, expectTypeOf } from "vitest";
import { NativeToken, type NativeTokenUnit } from "../../src/base/index.js";

describe("NativeTokenUnit public shape", () => {
  it("has readonly symbol: string", () => {
    expectTypeOf<NativeTokenUnit>().toHaveProperty("symbol").toEqualTypeOf<string>();
  });

  it("has readonly baseUnitFactor: bigint", () => {
    expectTypeOf<NativeTokenUnit>().toHaveProperty("baseUnitFactor").toEqualTypeOf<bigint>();
  });
});

describe("NativeToken public shape", () => {
  // Concrete subclass for type testing
  class TestUnit implements NativeTokenUnit {
    readonly symbol: string;
    readonly baseUnitFactor: bigint;
    constructor(symbol: string, factor: bigint) {
      this.symbol = symbol;
      this.baseUnitFactor = factor;
    }
  }

  class TestToken extends NativeToken<TestToken, TestUnit> {
    constructor(amount: bigint, unit: TestUnit) {
      super(amount, unit);
    }
    protected create(amount: bigint, unit: TestUnit): TestToken {
      return new TestToken(amount, unit);
    }
  }

  it("has readonly amount: bigint", () => {
    expectTypeOf<TestToken>().toHaveProperty("amount").toEqualTypeOf<bigint>();
  });

  it("has readonly unit of the correct Unit type", () => {
    expectTypeOf<TestToken>().toHaveProperty("unit").toEqualTypeOf<TestUnit>();
  });

  it("to() returns the concrete Self type", () => {
    expectTypeOf<TestToken>().toHaveProperty("to").toBeFunction();
    const unit = new TestUnit("T", 100n);
    const token = new TestToken(5n, unit);
    expectTypeOf(token.to(unit)).toEqualTypeOf<TestToken>();
  });

  it("toBaseUnits() returns bigint", () => {
    expectTypeOf<TestToken>().toHaveProperty("toBaseUnits").toBeFunction();
    const unit = new TestUnit("T", 100n);
    const token = new TestToken(5n, unit);
    expectTypeOf(token.toBaseUnits()).toEqualTypeOf<bigint>();
  });

  it("concrete subclass inherits from NativeToken", () => {
    const unit = new TestUnit("T", 100n);
    const token = new TestToken(5n, unit);
    expectTypeOf(token).toMatchTypeOf<NativeToken<TestToken, TestUnit>>();
  });
});
