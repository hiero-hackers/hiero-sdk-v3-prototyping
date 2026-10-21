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
