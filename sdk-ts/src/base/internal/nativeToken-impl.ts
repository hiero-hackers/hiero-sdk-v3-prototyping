import { NativeToken, type NativeTokenUnit } from "../nativeToken.js";

/**
 * Internal generic concrete implementation of NativeToken.
 * Used for unit tests and factory creation — not part of the public API.
 */
export class GenericNativeToken<
  Unit extends NativeTokenUnit,
> extends NativeToken<GenericNativeToken<Unit>, Unit> {
  constructor(amount: bigint, unit: Unit) {
    super(amount, unit);
  }

  protected create(amount: bigint, unit: Unit): GenericNativeToken<Unit> {
    return new GenericNativeToken(amount, unit);
  }

  /**
   * Factory method to create a GenericNativeToken instance.
   */
  static create<U extends NativeTokenUnit>(
    amount: bigint,
    unit: U,
  ): GenericNativeToken<U> {
    return new GenericNativeToken(amount, unit);
  }
}
