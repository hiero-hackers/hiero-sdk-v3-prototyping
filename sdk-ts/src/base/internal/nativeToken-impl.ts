import { NativeToken, type NativeTokenUnit } from '../nativeToken.js';

/**
 * Concrete implementation of NativeToken used internally and for general testing.
 */
export class GenericNativeToken<Unit extends NativeTokenUnit> extends NativeToken<GenericNativeToken<Unit>, Unit> {
    constructor(amount: bigint | number, unit: Unit) {
        super(amount, unit);
    }

    protected create(amount: bigint, unit: Unit): GenericNativeToken<Unit> {
        return new GenericNativeToken(amount, unit);
    }
}
