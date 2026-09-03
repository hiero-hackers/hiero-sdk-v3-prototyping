export const HEDERA_MAINNET_IDENTIFIER = "hedera-mainnet";
export const HEDERA_TESTNET_IDENTIFIER = "hedera-testnet";
export var HbarUnitSymbol;
(function (HbarUnitSymbol) {
    HbarUnitSymbol["TINYBAR"] = "t\u210F";
    HbarUnitSymbol["MICROBAR"] = "\u03BC\u210F";
    HbarUnitSymbol["MILLIBAR"] = "m\u210F";
    HbarUnitSymbol["HBAR"] = "\u210F";
})(HbarUnitSymbol || (HbarUnitSymbol = {}));
export const TINYBAR = { symbol: HbarUnitSymbol.TINYBAR, baseUnitFactor: 1n };
export const MICROBAR = { symbol: HbarUnitSymbol.MICROBAR, baseUnitFactor: 100n };
export const MILLIBAR = { symbol: HbarUnitSymbol.MILLIBAR, baseUnitFactor: 100000n };
export const HBAR = { symbol: HbarUnitSymbol.HBAR, baseUnitFactor: 100000000n };
