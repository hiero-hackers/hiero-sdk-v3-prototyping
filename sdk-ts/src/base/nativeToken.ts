/**
 * A unit of a native token — either the smallest indivisible unit or a named multiple of it.
 */
export interface NativeTokenUnit {
  /** Display symbol of the unit (e.g. "ℏ"). */
  readonly symbol: string;
  /** Number of base (smallest) units contained in one of this unit. */
  readonly baseUnitFactor: bigint;
}

/**
 * An amount of a network's native token expressed in a given unit.
 *
 * @typeParam Self - The concrete token type (enables `to()` to return the exact subtype).
 * @typeParam Unit - The unit type used by this token.
 */
export abstract class NativeToken<
  Self extends NativeToken<Self, Unit>,
  Unit extends NativeTokenUnit,
> {
  /** Amount expressed in `unit`. */
  readonly amount: bigint;
  /** The unit that `amount` is expressed in. */
  readonly unit: Unit;

  protected constructor(amount: bigint, unit: Unit) {
    if (amount === null || amount === undefined) {
      throw new Error("NativeToken: amount must not be null or undefined");
    }
    if (unit === null || unit === undefined) {
      throw new Error("NativeToken: unit must not be null or undefined");
    }
    if (typeof amount !== "bigint") {
      throw new Error("NativeToken: amount must be a bigint (integer)");
    }
    if (unit.baseUnitFactor <= 0n) {
      throw new Error(
        "NativeToken: unit.baseUnitFactor must be a positive value",
      );
    }
    this.amount = amount;
    this.unit = unit;
    Object.freeze(this);
  }

  /**
   * Creates a new instance of the concrete token type with the given amount and unit.
   * Subclasses must implement this to enable `to()` to return the correct concrete type.
   */
  protected abstract create(amount: bigint, unit: Unit): Self;

  /**
   * Converts this token amount to the given target unit.
   * The conversion is: `newAmount = this.amount * this.unit.baseUnitFactor / targetUnit.baseUnitFactor`
   */
  to(targetUnit: Unit): Self {
    if (targetUnit === null || targetUnit === undefined) {
      throw new Error("NativeToken.to: targetUnit must not be null or undefined");
    }
    const baseUnits = this.amount * this.unit.baseUnitFactor;
    const converted = baseUnits / targetUnit.baseUnitFactor;
    return this.create(converted, targetUnit);
  }

  /**
   * Returns the total amount expressed in base (smallest) units.
   * Result: `this.amount * this.unit.baseUnitFactor`
   */
  toBaseUnits(): bigint {
    return this.amount * this.unit.baseUnitFactor;
  }
}
