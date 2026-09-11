import { NetworkSetting } from './LedgerConfig.js';
import { NativeTokenUnit, NativeToken } from './NativeToken.js';

export const HEDERA_MAINNET_IDENTIFIER = "hedera-mainnet";
export const HEDERA_TESTNET_IDENTIFIER = "hedera-testnet";

export interface HederaNetworkSetting extends NetworkSetting {}

export enum HbarUnitSymbol {
  TINYBAR = 'tℏ',
  MICROBAR = 'μℏ',
  MILLIBAR = 'mℏ',
  HBAR = 'ℏ'
}

export interface HbarUnit extends NativeTokenUnit {
  symbol: HbarUnitSymbol;
}

export const TINYBAR: HbarUnit = { symbol: HbarUnitSymbol.TINYBAR, baseUnitFactor: 1n };
export const MICROBAR: HbarUnit = { symbol: HbarUnitSymbol.MICROBAR, baseUnitFactor: 100n };
export const MILLIBAR: HbarUnit = { symbol: HbarUnitSymbol.MILLIBAR, baseUnitFactor: 100_000n };
export const HBAR: HbarUnit = { symbol: HbarUnitSymbol.HBAR, baseUnitFactor: 100_000_000n };

export interface HbarAmount extends NativeToken<HbarAmount, HbarUnit> {
}
