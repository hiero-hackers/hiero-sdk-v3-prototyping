import { describe, expectTypeOf, it } from "vitest";
import { NativeToken, NativeTokenUnit } from "../src/index.js";
import { OtherUnit, TestToken, TestUnit } from "./test-token.js";

describe("NativeToken public types", () => {
  const token = new TestToken(1n, TestUnit.BASE);

  it("should type int64 values as bigint", () => {
    expectTypeOf(token.amount).toEqualTypeOf<bigint>();
    expectTypeOf(token.toBaseUnits).returns.toEqualTypeOf<bigint>();
    expectTypeOf(TestUnit.BASE.baseUnitFactor).toEqualTypeOf<bigint>();
    expectTypeOf(TestUnit.BASE.symbol).toEqualTypeOf<string>();
  });

  it("should type the unit and conversion result with the concrete types", () => {
    expectTypeOf(token.unit).toEqualTypeOf<TestUnit>();
    expectTypeOf(token.to(TestUnit.CENT)).toEqualTypeOf<TestToken>();
    expectTypeOf<TestToken>().toExtend<NativeToken<TestToken, TestUnit>>();
  });

  it("should reject a unit of another token", () => {
    expectTypeOf<OtherUnit>().not.toExtend<TestUnit>();
    // @ts-expect-error units of different tokens are not interchangeable
    token.to(OtherUnit.BASE);
  });

  it("should keep state read-only and construction protected", () => {
    // @ts-expect-error amount has no setter
    token.amount = 2n;
    // @ts-expect-error unit has no setter
    token.unit = TestUnit.CENT;
    // @ts-expect-error baseUnitFactor has no setter
    TestUnit.BASE.baseUnitFactor = 2n;
    // @ts-expect-error amount must be a bigint
    new TestToken(1, TestUnit.BASE);
    // @ts-expect-error create is protected
    token.create(1n, TestUnit.BASE);
    // @ts-expect-error NativeTokenUnit is abstract
    new NativeTokenUnit("t", 1n);
  });
});
