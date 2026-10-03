# Hedera API

## Description

Support for the Hedera network, the public network built on Hiero. It provides the identifiers of
the Hedera mainnet and testnet, which can be used to look up their network settings, and `Hbar`,
the native token of Hedera, together with its units (`HbarUnit`) from tinybar up to gigabar.

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
// `symbol` and `baseUnitFactor` are inherited from NativeTokenUnit; each value assigns its own
// normative values. `baseUnitFactor` is the number of tinybars represented by one unit.
enum HbarUnit(symbol: string, baseUnitFactor: int64) extends NativeTokenUnit {
    TINYBAR("tℏ", 1)
    MICROBAR("μℏ", 100)
    MILLIBAR("mℏ", 100_000)
    HBAR("ℏ", 100_000_000)
    KILOBAR("kℏ", 100_000_000_000)
    MEGABAR("Mℏ", 100_000_000_000_000)
    GIGABAR("Gℏ", 100_000_000_000_000_000)
}

// HBAR, the native token of the Hedera network. `amount`, `unit`, and `to(...)` are inherited from
// NativeToken.
Hbar extends NativeToken<Hbar, HbarUnit> {

    // Total amount in tinybars (the base unit of HBAR); equivalent to toBaseUnits().
    int64 toTinybars()
}

```

## Questions & Comments
