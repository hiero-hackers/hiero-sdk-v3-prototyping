import { requireInt64 } from "./int64.js";
import { NativeTokenUnit } from "./native-token-unit.js";

/**
 * An immutable amount of a network's native token, expressed in a given unit.
 *
 * Concrete tokens extend this class and implement {@link NativeToken.create} so that {@link NativeToken.to} returns
 * the concrete token type.
 *
 * @typeParam Self - the concrete token type returned by conversions
 * @typeParam Unit - the unit type of the token
 */
export abstract class NativeToken<Self extends NativeToken<Self, Unit>, Unit extends NativeTokenUnit> {
  readonly #amount: bigint;
  readonly #unit: Unit;

  /**
   * Creates a token amount.
   *
   * @param amount - the amount expressed in `unit`; a 64-bit signed integer
   * @param unit - the unit `amount` is expressed in
   * @throws TypeError if `amount` is not a `bigint` or `unit` is not a {@link NativeTokenUnit}
   * @throws RangeError if `amount` is outside the signed 64-bit range
   */
  protected constructor(amount: bigint, unit: Unit) {
    requireInt64(amount, "amount");
    if (!(unit instanceof NativeTokenUnit)) {
      throw new TypeError("unit must be a NativeTokenUnit");
    }
    this.#amount = amount;
    this.#unit = unit;
  }

  /**
   * The amount, expressed in {@link NativeToken.unit}.
   */
  get amount(): bigint {
    return this.#amount;
  }

  /**
   * The unit {@link NativeToken.amount} is expressed in.
   */
  get unit(): Unit {
    return this.#unit;
  }

  /**
   * Converts this amount to another unit of the same token.
   *
   * The conversion is exact: it never rounds or truncates. This instance is not modified; a new token is always
   * returned, including when `targetUnit` is the current unit.
   *
   * @param targetUnit - the unit to convert to
   * @returns a new token of the concrete type holding the converted amount
   * @throws RangeError if the amount cannot be expressed as a whole number of `targetUnit`, or if the converted
   *   amount is outside the signed 64-bit range
   */
  to(targetUnit: Unit): Self {
    const baseUnits = this.toBaseUnits();
    const factor = targetUnit.baseUnitFactor;
    if (baseUnits % factor !== 0n) {
      throw new RangeError(
        `${this.#amount.toString()} ${this.#unit.symbol} cannot be expressed exactly in ${targetUnit.symbol}`,
      );
    }
    return this.create(baseUnits / factor, targetUnit);
  }

  /**
   * Returns the total amount expressed in base (smallest) units.
   *
   * @returns `amount * unit.baseUnitFactor`
   */
  toBaseUnits(): bigint {
    return this.#amount * this.#unit.baseUnitFactor;
  }

  /**
   * Creates a new instance of the concrete token type. Called by {@link NativeToken.to}.
   *
   * @param amount - the amount expressed in `unit`
   * @param unit - the unit of the new token
   * @returns a new token of the concrete type
   */
  protected abstract create(amount: bigint, unit: Unit): Self;
}
