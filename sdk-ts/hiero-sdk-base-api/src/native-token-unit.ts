import { requireInt64 } from "./int64.js";

/**
 * A unit of a native token, either the smallest indivisible unit or a named multiple of it.
 *
 * Concrete units extend this class, typically exposing their instances as `static readonly` members. Because
 * TypeScript compares classes structurally, a concrete unit class should declare a nominal member (for example a
 * `private readonly` brand field) so that units of different tokens cannot be mixed at compile time.
 */
export abstract class NativeTokenUnit {
  readonly #symbol: string;
  readonly #baseUnitFactor: bigint;

  /**
   * Creates a unit.
   *
   * @param symbol - the display symbol of the unit (for example `"ℏ"`)
   * @param baseUnitFactor - the number of base units contained in one of this unit; a positive 64-bit integer
   * @throws TypeError if `symbol` is not a string or `baseUnitFactor` is not a `bigint`
   * @throws RangeError if `baseUnitFactor` is not greater than zero or is outside the signed 64-bit range
   */
  protected constructor(symbol: string, baseUnitFactor: bigint) {
    if (typeof symbol !== "string") {
      throw new TypeError(`symbol must be a string, but was ${typeof symbol}`);
    }
    requireInt64(baseUnitFactor, "baseUnitFactor");
    if (baseUnitFactor <= 0n) {
      throw new RangeError(`baseUnitFactor must be greater than zero, but was ${baseUnitFactor.toString()}`);
    }
    this.#symbol = symbol;
    this.#baseUnitFactor = baseUnitFactor;
  }

  /**
   * The display symbol of this unit.
   */
  get symbol(): string {
    return this.#symbol;
  }

  /**
   * The number of base (smallest) units contained in one of this unit.
   */
  get baseUnitFactor(): bigint {
    return this.#baseUnitFactor;
  }
}
