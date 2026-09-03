import { NetworkSetting } from './LedgerConfig.js';
import { NativeTokenUnit, NativeToken } from './NativeToken.js';
export declare const HEDERA_MAINNET_IDENTIFIER = "hedera-mainnet";
export declare const HEDERA_TESTNET_IDENTIFIER = "hedera-testnet";
export interface HederaNetworkSetting extends NetworkSetting {
}
export declare enum HbarUnitSymbol {
    TINYBAR = "t\u210F",
    MICROBAR = "\u03BC\u210F",
    MILLIBAR = "m\u210F",
    HBAR = "\u210F"
}
export interface HbarUnit extends NativeTokenUnit {
    symbol: HbarUnitSymbol;
}
export declare const TINYBAR: HbarUnit;
export declare const MICROBAR: HbarUnit;
export declare const MILLIBAR: HbarUnit;
export declare const HBAR: HbarUnit;
export interface HbarAmount extends NativeToken<HbarAmount, HbarUnit> {
}
