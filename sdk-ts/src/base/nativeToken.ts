/**
 * A unit of a native token, either the smallest indivisible unit or a named multiple of it.
 */
export interface NativeTokenUnit {
    /**
     * Display symbol of the unit (e.g. "ℏ").
     */
    readonly symbol: string;

    /**
     * Number of base (smallest) units contained in one of this unit.
     */
    readonly baseUnitFactor: bigint | number;
}

/**
 * An amount of a network's native token, expressed in a given unit.
 *
 * @typeParam Self - The concrete token type, so `to(...)` returns that exact type rather than the abstraction.
 * @typeParam Unit - The unit type associated with this token amount.
 */
export abstract class NativeToken<Self extends NativeToken<Self, Unit>, Unit extends NativeTokenUnit> {
    readonly #amount: bigint;
    readonly #unit: Unit;

    /**
     * Creates an immutable native token amount.
     *
     * @param amount - The amount expressed in `unit` (integer).
     * @param unit - The unit `amount` is expressed in.
     * @throws TypeError if `amount` or `unit` is null or undefined, or not an integer.
     * @throws RangeError if `unit.baseUnitFactor` is less than or equal to 0.
     */
    protected constructor(amount: bigint | number, unit: Unit) {
        if (amount === undefined || amount === null) {
            throw new TypeError('Token amount must not be null or undefined');
        }
        if (typeof amount !== 'bigint' && typeof amount !== 'number') {
            throw new TypeError('Token amount must be a number or bigint');
        }
        if (typeof amount === 'number') {
            if (!Number.isInteger(amount)) {
                throw new TypeError('Token amount must be an integer');
            }
            if (!Number.isSafeInteger(amount)) {
                throw new RangeError('Token amount number exceeds safe integer range; use bigint instead');
            }
        }
        if (unit === undefined || unit === null) {
            throw new TypeError('Token unit must not be null or undefined');
        }
        if (unit.baseUnitFactor === undefined || unit.baseUnitFactor === null) {
            throw new TypeError('Token unit baseUnitFactor must not be null or undefined');
        }

        const factor = BigInt(unit.baseUnitFactor);
        if (factor <= 0n) {
            throw new RangeError('Token unit baseUnitFactor must be positive');
        }

        this.#amount = BigInt(amount);
        this.#unit = unit;
        Object.freeze(this);
    }

    /**
     * The amount expressed in `unit`.
     */
    get amount(): bigint {
        return this.#amount;
    }

    /**
     * The unit `amount` is expressed in.
     */
    get unit(): Unit {
        return this.#unit;
    }

    /**
     * Total amount expressed in base (smallest) units.
     *
     * @returns Total amount in base units as a 64-bit integer (`bigint`).
     */
    toBaseUnits(): bigint {
        return this.#amount * BigInt(this.#unit.baseUnitFactor);
    }

    /**
     * Converts this amount to a different unit of the same token.
     *
     * @param targetUnit - The unit to convert this amount to.
     * @returns A new instance of `Self` mathematically converted to `targetUnit`.
     * @throws TypeError if `targetUnit` is null or undefined.
     * @throws RangeError if `targetUnit.baseUnitFactor` is less than or equal to 0.
     */
    to(targetUnit: Unit): Self {
        if (targetUnit === undefined || targetUnit === null) {
            throw new TypeError('Target unit must not be null or undefined');
        }
        if (targetUnit.baseUnitFactor === undefined || targetUnit.baseUnitFactor === null) {
            throw new TypeError('Target unit baseUnitFactor must not be null or undefined');
        }

        const targetFactor = BigInt(targetUnit.baseUnitFactor);
        if (targetFactor <= 0n) {
            throw new RangeError('Target unit baseUnitFactor must be positive');
        }

        const baseUnits = this.toBaseUnits();
        const convertedAmount = baseUnits / targetFactor;
        return this.create(convertedAmount, targetUnit);
    }

    /**
     * Factory method implemented by concrete token subclasses to produce a new instance of `Self`.
     *
     * @param amount - The converted amount in target unit.
     * @param unit - The target unit.
     */
    protected abstract create(amount: bigint, unit: Unit): Self;
}
