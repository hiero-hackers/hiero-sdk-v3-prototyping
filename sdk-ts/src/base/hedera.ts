import type { NetworkSetting } from './ledgerConfig.js';
import { BaseNativeTokenUnit, BaseNativeToken } from './internal/nativeToken-impl.js';

export const HEDERA_MAINNET_IDENTIFIER = "hedera-mainnet";
export const HEDERA_TESTNET_IDENTIFIER = "hedera-testnet";

/**
 * Represents Hedera specific network settings.
 * @see {@link file://../../spec/base/hedera.md}
 */
export interface HederaNetworkSetting extends NetworkSetting {}

/**
 * Definition of the different units of HBAR, the native token of the Hedera network.
 * Inherits symbol and baseUnitFactor from NativeTokenUnit.
 * @see {@link file://../../spec/base/hedera.md}
 */
export class HbarUnit extends BaseNativeTokenUnit {
    static readonly TINYBAR = new HbarUnit('tℏ', 1n);
    static readonly MICROBAR = new HbarUnit('μℏ', 100n);
    static readonly MILLIBAR = new HbarUnit('mℏ', 100_000n);
    static readonly HBAR = new HbarUnit('ℏ', 100_000_000n);
    static readonly KILOBAR = new HbarUnit('kℏ', 100_000_000_000n);
    static readonly MEGABAR = new HbarUnit('Mℏ', 100_000_000_000_000n);
    static readonly GIGABAR = new HbarUnit('Gℏ', 100_000_000_000_000_000n);

    private constructor(symbol: string, baseUnitFactor: bigint) {
        super(symbol, baseUnitFactor);
    }
}

/**
 * HBAR, the native token of the Hedera network.
 * @see {@link file://../../spec/base/hedera.md}
 */
export class Hbar extends BaseNativeToken<Hbar, HbarUnit> {
    constructor(amount: bigint, unit: HbarUnit = HbarUnit.HBAR) {
        super(amount, unit);
        Object.freeze(this);
    }

    /**
     * Convert this amount to a different unit of HBAR.
     * @param targetUnit - The target HbarUnit.
     * @returns A new Hbar instance in the target unit.
     */
    to(targetUnit: HbarUnit): Hbar {
        const tinybars = this.toBaseUnits();
        // Uses integer division to prevent fractional tinybars as they are indivisible base units.
        return new Hbar(tinybars / targetUnit.baseUnitFactor, targetUnit);
    }

    /**
     * Total amount in tinybars (the base unit of HBAR); equivalent to toBaseUnits().
     * @returns The total amount expressed strictly in tinybars.
     */
    toTinybars(): bigint {
        return this.toBaseUnits();
    }
}
