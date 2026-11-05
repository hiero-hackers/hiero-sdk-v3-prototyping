import { NativeToken, NativeTokenUnit } from "../src/index.js";

/** A test unit of a fictional token; `brand` keeps it nominally distinct from other units. */
export class TestUnit extends NativeTokenUnit {
  static readonly BASE: TestUnit = new TestUnit("t", 1n);
  static readonly CENT: TestUnit = new TestUnit("c", 100n);
  static readonly WHOLE: TestUnit = new TestUnit("T", 100_000_000n);

  private readonly brand = "test";

  constructor(symbol: string, baseUnitFactor: bigint) {
    super(symbol, baseUnitFactor);
  }
}

/** A test token over {@link TestUnit}. */
export class TestToken extends NativeToken<TestToken, TestUnit> {
  constructor(amount: bigint, unit: TestUnit) {
    super(amount, unit);
  }

  protected override create(amount: bigint, unit: TestUnit): TestToken {
    return new TestToken(amount, unit);
  }
}

/** A unit of a different fictional token. */
export class OtherUnit extends NativeTokenUnit {
  static readonly BASE: OtherUnit = new OtherUnit("o", 1n);

  private readonly brand = "other";

  constructor(symbol: string, baseUnitFactor: bigint) {
    super(symbol, baseUnitFactor);
  }
}
