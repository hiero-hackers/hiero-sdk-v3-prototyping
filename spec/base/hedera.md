# Hedera API

## Description

## API Schema — Abstraction

```
namespace hedera
requires {NetworkSetting} from ledger.config
requires {NativeToken, NativeTokenUnit} from nativeToken

constant HEDERA_MAINNET_IDENTIFIER:string = "hedera-mainnet" // identifier for the Hedera mainnet
constant HEDERA_TESTNET_IDENTIFIER:string = "hedera-testnet" // identifier for the Hedera testnet

HederaNetworkSetting extends NetworkSetting {
}

// Definition of the different units of HBAR, the native token of the Hedera network.
// `symbol` and `baseUnitFactor` are inherited from NativeTokenUnit; each constant provides its own
// normative values below. `baseUnitFactor` is the number of tinybars represented by one unit.
enum HbarUnit extends NativeTokenUnit {
    TINYBAR  // symbol: "tℏ", baseUnitFactor: 1
    MICROBAR // symbol: "μℏ", baseUnitFactor: 100
    MILLIBAR // symbol: "mℏ", baseUnitFactor: 100_000
    HBAR     // symbol: "ℏ", baseUnitFactor: 100_000_000
    KILOBAR  // symbol: "kℏ", baseUnitFactor: 100_000_000_000
    MEGABAR  // symbol: "Mℏ", baseUnitFactor: 100_000_000_000_000
    GIGABAR  // symbol: "Gℏ", baseUnitFactor: 100_000_000_000_000_000
}

// HBAR, the native token of the Hedera network. `amount`, `unit`, and `to(...)` are inherited from
// NativeToken.
Hbar extends NativeToken<Hbar, HbarUnit> {

    // Total amount in tinybars (the base unit of HBAR); equivalent to toBaseUnits().
    int64 toTinybars()
}

```

## Questions & Comments
