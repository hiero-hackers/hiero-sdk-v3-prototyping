# Hedera API

## Description

Support for the Hedera network, the public network built on Hiero. It provides the identifiers of
the Hedera mainnet and testnet, which can be used to look up their network settings, and `Hbar`,
the native token of Hedera, together with its units (`HbarUnit`) from tinybar up to gigabar.

The SDK knows the network settings of the Hedera mainnet and testnet; every SDK knows the same:

`rule:hedera-mainnet-setting` The network setting of `HEDERA_MAINNET_IDENTIFIER` is a `HederaNetworkSetting` of the
network with the id `0x00` and the name `mainnet`, with the mirror node
`https://mainnet-public.mirrornode.hedera.com/api/v1` and these 52 consensus nodes, each with the gRPC port 50211
(plaintext):

| Node account | IP addresses |
|---|---|
| `0.0.3` | `34.239.82.6`, `35.237.200.180` |
| `0.0.4` | `3.130.52.236`, `35.186.191.247` |
| `0.0.5` | `3.18.18.254`, `35.192.2.25`, `74.50.117.35`, `23.111.186.250`, `107.155.64.98` |
| `0.0.7` | `3.114.54.4`, `35.203.82.240` |
| `0.0.8` | `35.236.5.219`, `35.183.66.150` |
| `0.0.9` | `35.181.158.250`, `35.197.192.225` |
| `0.0.10` | `177.154.62.234`, `3.248.27.48`, `35.242.233.154` |
| `0.0.12` | `35.204.86.32`, `35.177.162.180` |
| `0.0.13` | `34.215.192.104`, `35.234.132.107` |
| `0.0.14` | `52.8.21.141`, `35.236.2.27` |
| `0.0.15` | `35.228.11.53`, `3.121.238.26` |
| `0.0.18` | `141.94.175.187` |
| `0.0.20` | `34.82.78.255`, `52.39.162.216` |
| `0.0.21` | `34.76.140.109`, `13.36.123.209` |
| `0.0.22` | `52.78.202.34`, `34.64.141.166` |
| `0.0.23` | `3.18.91.176`, `35.232.244.145`, `69.167.169.208` |
| `0.0.24` | `34.89.103.38`, `18.135.7.211` |
| `0.0.25` | `34.93.112.7`, `13.232.240.207` |
| `0.0.28` | `35.198.220.75`, `18.139.47.5` |
| `0.0.29` | `54.74.60.120`, `34.142.71.129`, `80.85.70.197` |
| `0.0.31` | `217.76.57.165`, `3.77.94.254`, `34.107.78.179` |
| `0.0.33` | `18.136.65.22`, `34.142.172.228` |
| `0.0.34` | `34.16.139.248`, `35.155.212.90` |

`rule:hedera-testnet-setting` The network setting of `HEDERA_TESTNET_IDENTIFIER` is a `HederaNetworkSetting` of the
network with the id `0x01` and the name `testnet`, with the mirror node `https://testnet.mirrornode.hedera.com/api/v1`
and these 14 consensus nodes, each with the gRPC port 50211 (plaintext):

| Node account | IP addresses |
|---|---|
| `0.0.3` | `34.94.106.61`, `50.18.132.211` |
| `0.0.4` | `3.212.6.13`, `35.237.119.55` |
| `0.0.5` | `35.245.27.193`, `52.20.18.86` |
| `0.0.6` | `34.83.112.116`, `54.70.192.33` |
| `0.0.7` | `34.94.160.4`, `54.176.199.109` |
| `0.0.8` | `35.155.49.147`, `34.106.102.218` |
| `0.0.9` | `34.133.197.230`, `52.14.252.207` |

## Design Notes

- **Source of the node addresses.** The consensus nodes of the mainnet and testnet settings are those of the Hiero
  JavaScript SDK (`ClientConstants.ALL_NETWORK_IPS`); a node account with several IP addresses is one node reachable
  under each of them, so it appears once per address in `getConsensusNodes`.

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

    // Total amount in tinybars (the base unit of HBAR); equivalent to toBaseUnits(). Fails if the amount does not fit
    // into an int64 of tinybars.
    @@throws(invalid-argument-error) int64 toTinybars()

    // Two amounts of HBAR are equal if they are the same number of tinybars, whatever their units: 1 ℏ equals
    // 100,000,000 tℏ. The amounts are compared exactly, also those that do not fit into an int64 of tinybars.
    bool equals(other: ANY)

    // Consistent with equals: equal amounts in different units have the same hash code.
    int32 hashCode()
}

```

## Default Instances for Testing

```
// One HBAR.
instance Hbar = Hbar{amount: 1, unit: HbarUnit.HBAR}

// The number of tinybars in one of each unit, and its symbol, as the units of HBAR are defined above.
values TINYBARS_PER_UNIT: map<HbarUnit, int64> = {
    HbarUnit.TINYBAR: 1,
    HbarUnit.MICROBAR: 100,
    HbarUnit.MILLIBAR: 100_000,
    HbarUnit.HBAR: 100_000_000,
    HbarUnit.KILOBAR: 100_000_000_000,
    HbarUnit.MEGABAR: 100_000_000_000_000,
    HbarUnit.GIGABAR: 100_000_000_000_000_000
}

values SYMBOLS: map<HbarUnit, string> = {
    HbarUnit.TINYBAR: "tℏ",
    HbarUnit.MICROBAR: "μℏ",
    HbarUnit.MILLIBAR: "mℏ",
    HbarUnit.HBAR: "ℏ",
    HbarUnit.KILOBAR: "kℏ",
    HbarUnit.MEGABAR: "Mℏ",
    HbarUnit.GIGABAR: "Gℏ"
}

// The consensus nodes of the Hedera mainnet, IP address to node account, as listed in `rule:hedera-mainnet-setting`.
values MAINNET_NODES: map<string, string> = {
    "34.239.82.6": "0.0.3",
    "35.237.200.180": "0.0.3",
    "3.130.52.236": "0.0.4",
    "35.186.191.247": "0.0.4",
    "3.18.18.254": "0.0.5",
    "35.192.2.25": "0.0.5",
    "74.50.117.35": "0.0.5",
    "23.111.186.250": "0.0.5",
    "107.155.64.98": "0.0.5",
    "3.114.54.4": "0.0.7",
    "35.203.82.240": "0.0.7",
    "35.236.5.219": "0.0.8",
    "35.183.66.150": "0.0.8",
    "35.181.158.250": "0.0.9",
    "35.197.192.225": "0.0.9",
    "177.154.62.234": "0.0.10",
    "3.248.27.48": "0.0.10",
    "35.242.233.154": "0.0.10",
    "35.204.86.32": "0.0.12",
    "35.177.162.180": "0.0.12",
    "34.215.192.104": "0.0.13",
    "35.234.132.107": "0.0.13",
    "52.8.21.141": "0.0.14",
    "35.236.2.27": "0.0.14",
    "35.228.11.53": "0.0.15",
    "3.121.238.26": "0.0.15",
    "141.94.175.187": "0.0.18",
    "34.82.78.255": "0.0.20",
    "52.39.162.216": "0.0.20",
    "34.76.140.109": "0.0.21",
    "13.36.123.209": "0.0.21",
    "52.78.202.34": "0.0.22",
    "34.64.141.166": "0.0.22",
    "3.18.91.176": "0.0.23",
    "35.232.244.145": "0.0.23",
    "69.167.169.208": "0.0.23",
    "34.89.103.38": "0.0.24",
    "18.135.7.211": "0.0.24",
    "34.93.112.7": "0.0.25",
    "13.232.240.207": "0.0.25",
    "35.198.220.75": "0.0.28",
    "18.139.47.5": "0.0.28",
    "54.74.60.120": "0.0.29",
    "34.142.71.129": "0.0.29",
    "80.85.70.197": "0.0.29",
    "217.76.57.165": "0.0.31",
    "3.77.94.254": "0.0.31",
    "34.107.78.179": "0.0.31",
    "18.136.65.22": "0.0.33",
    "34.142.172.228": "0.0.33",
    "34.16.139.248": "0.0.34",
    "35.155.212.90": "0.0.34"
}

// The consensus nodes of the Hedera testnet, IP address to node account, as listed in `rule:hedera-testnet-setting`.
values TESTNET_NODES: map<string, string> = {
    "34.94.106.61": "0.0.3",
    "50.18.132.211": "0.0.3",
    "3.212.6.13": "0.0.4",
    "35.237.119.55": "0.0.4",
    "35.245.27.193": "0.0.5",
    "52.20.18.86": "0.0.5",
    "34.83.112.116": "0.0.6",
    "54.70.192.33": "0.0.6",
    "34.94.160.4": "0.0.7",
    "54.176.199.109": "0.0.7",
    "35.155.49.147": "0.0.8",
    "34.106.102.218": "0.0.8",
    "34.133.197.230": "0.0.9",
    "52.14.252.207": "0.0.9"
}
```

## Testing

Tests of HBAR and its units, written as given / when / then. The comment above each test names its source; the units
and their factors are those of this spec, which match the HBAR denominations of the Hedera documentation.

```tests
// Source: this spec (HbarUnit); the unit tests of the Java, JavaScript, Go, Rust and Swift SDKs.
@@testId("every-unit-has-its-symbol-and-its-number-of-tinybars")
test "every unit has its symbol and its number of tinybars" {
    for unit in keys(value: TINYBARS_PER_UNIT) {
        // when / then
        assertEquals(expected: TINYBARS_PER_UNIT[unit], actual: unit.baseUnitFactor, message: unit.symbol)
        assertEquals(expected: SYMBOLS[unit], actual: unit.symbol)
    }
}

// Source: this spec (toTinybars is toBaseUnits); the Java, JavaScript, Go and Rust SDKs (one of each unit in tinybars).
@@testId("one-of-each-unit-is-its-number-of-tinybars")
test "one of each unit is its number of tinybars" {
    for unit in keys(value: TINYBARS_PER_UNIT) {
        // given
        let one = Hbar{amount: 1, unit: unit}

        // when / then
        assertEquals(expected: TINYBARS_PER_UNIT[unit], actual: one.toTinybars(), message: unit.symbol)
        assertEquals(expected: TINYBARS_PER_UNIT[unit], actual: one.toBaseUnits(), message: unit.symbol)
    }
}

// Source: the Java, JavaScript and Go SDKs (an amount is converted to every unit and back without loss).
@@testId("an-amount-of-whole-units-converts-to-tinybars-and-back")
test "an amount of whole units converts to tinybars and back" {
    for unit in keys(value: TINYBARS_PER_UNIT) {
        // given
        let one = Hbar{amount: 1, unit: unit}

        // when
        let tinybars = one.to(targetUnit: HbarUnit.TINYBAR)
        let back = tinybars.to(targetUnit: unit)

        // then
        assertEquals(expected: Hbar{amount: TINYBARS_PER_UNIT[unit], unit: HbarUnit.TINYBAR}, actual: tinybars)
        assertEquals(expected: one, actual: back)
    }
}

// Source: the Java and JavaScript SDKs (conversions between units other than tinybar).
@@testId("an-amount-converts-between-units-by-their-factors")
test "an amount converts between units by their factors" {
    // given
    let hbar = DEFAULT(Hbar)

    // when / then
    assertEquals(expected: Hbar{amount: 1_000, unit: HbarUnit.MILLIBAR}, actual: hbar.to(targetUnit: HbarUnit.MILLIBAR))
    assertEquals(expected: Hbar{amount: 1_000_000, unit: HbarUnit.MICROBAR}, actual: hbar.to(targetUnit: HbarUnit.MICROBAR))
    assertEquals(expected: Hbar{amount: 1, unit: HbarUnit.KILOBAR},
            actual: Hbar{amount: 1_000, unit: HbarUnit.HBAR}.to(targetUnit: HbarUnit.KILOBAR))
    assertEquals(expected: Hbar{amount: 1_000_000, unit: HbarUnit.KILOBAR},
            actual: Hbar{amount: 1, unit: HbarUnit.GIGABAR}.to(targetUnit: HbarUnit.KILOBAR))
}

// Source: the Java, JavaScript, Go and Python SDKs (a negative amount, as used by the sender of a transfer).
@@testId("a-negative-amount-converts-like-a-positive-one")
test "a negative amount converts like a positive one" {
    // given
    let debit = Hbar{amount: -5, unit: HbarUnit.HBAR}

    // when / then
    assertEquals(expected: -500_000_000, actual: debit.toTinybars())
    assertEquals(expected: Hbar{amount: -5_000, unit: HbarUnit.MILLIBAR}, actual: debit.to(targetUnit: HbarUnit.MILLIBAR))
}

// Source: the Java and Go SDKs (zero HBAR).
@@testId("zero-is-zero-in-every-unit")
test "zero is zero in every unit" {
    for unit in keys(value: TINYBARS_PER_UNIT) {
        // when / then
        assertEquals(expected: 0, actual: Hbar{amount: 0, unit: unit}.toTinybars())
        assertEquals(expected: Hbar{amount: 0, unit: HbarUnit.TINYBAR},
                actual: Hbar{amount: 0, unit: unit}.to(targetUnit: HbarUnit.TINYBAR))
    }
}

// Source: this spec (amounts are int64 tinybars); the largest whole number of gigabars that fits.
@@testId("the-largest-amounts-that-fit-into-int64-tinybars-convert-exactly")
test "the largest amounts that fit into int64 tinybars convert exactly" {
    // when / then
    assertEquals(expected: 9_200_000_000_000_000_000, actual: Hbar{amount: 92, unit: HbarUnit.GIGABAR}.toTinybars())
    assertEquals(expected: 9_223_372_036_854_775_807,
            actual: Hbar{amount: 9_223_372_036_854_775_807, unit: HbarUnit.TINYBAR}.toTinybars())
}

// Source: this spec (an Hbar is its amount and its unit).
@@testId("an-amount-keeps-its-amount-and-its-unit")
test "an amount keeps its amount and its unit" {
    // when
    let hbar = Hbar{amount: 42, unit: HbarUnit.KILOBAR}

    // then
    assertEquals(expected: 42, actual: hbar.amount)
    assertEquals(expected: HbarUnit.KILOBAR, actual: hbar.unit)
}

// Source: this spec (equals); the Java and JavaScript SDKs (amounts are compared in tinybars).
@@testId("amounts-of-the-same-number-of-tinybars-are-equal-whatever-their-units")
test "amounts of the same number of tinybars are equal whatever their units" {
    for unit in keys(value: TINYBARS_PER_UNIT) {
        // given
        let one = Hbar{amount: 1, unit: unit}
        let inTinybars = Hbar{amount: TINYBARS_PER_UNIT[unit], unit: HbarUnit.TINYBAR}

        // when / then
        assertEquals(expected: inTinybars, actual: one, message: unit.symbol)
        assertEquals(expected: inTinybars.hashCode(), actual: one.hashCode(), message: unit.symbol)
        assertNotEquals(unexpected: Hbar{amount: TINYBARS_PER_UNIT[unit] + 1, unit: HbarUnit.TINYBAR}, actual: one)
    }

    // when / then: units other than tinybars
    assertEquals(expected: Hbar{amount: 1_000, unit: HbarUnit.HBAR}, actual: Hbar{amount: 1, unit: HbarUnit.KILOBAR})
    assertNotEquals(unexpected: Hbar{amount: -1, unit: HbarUnit.HBAR}, actual: DEFAULT(Hbar))
}

// Source: this spec (equals compares exactly): 100 gigabars and 100,000 megabars are both 10^19 tinybars, more than
// an int64 holds.
@@testId("amounts-beyond-int64-tinybars-are-compared-exactly")
test "amounts beyond int64 tinybars are compared exactly" {
    // given
    let gigabars = Hbar{amount: 100, unit: HbarUnit.GIGABAR}

    // when / then
    assertEquals(expected: Hbar{amount: 100_000, unit: HbarUnit.MEGABAR}, actual: gigabars)
    assertEquals(expected: Hbar{amount: 100_000, unit: HbarUnit.MEGABAR}.hashCode(), actual: gigabars.hashCode())
    assertNotEquals(unexpected: Hbar{amount: 100_001, unit: HbarUnit.MEGABAR}, actual: gigabars)
}

// Source: this spec (a conversion never loses a part of the amount).
@@testId("a-conversion-to-a-unit-of-which-the-amount-is-no-whole-number-fails")
test "a conversion to a unit of which the amount is no whole number fails" {
    // when / then
    @@expectThrows(invalid-argument-error)
    Hbar{amount: 1, unit: HbarUnit.TINYBAR}.to(targetUnit: HbarUnit.HBAR)
    @@expectThrows(invalid-argument-error)
    Hbar{amount: 150, unit: HbarUnit.HBAR}.to(targetUnit: HbarUnit.KILOBAR)
    @@expectThrows(invalid-argument-error)
    Hbar{amount: -1, unit: HbarUnit.MICROBAR}.to(targetUnit: HbarUnit.MILLIBAR)
}

// Source: this spec (amounts are int64 in every unit): 93 gigabars are more tinybars than an int64 holds.
@@testId("an-amount-that-does-not-fit-into-int64-tinybars-fails-to-convert")
test "an amount that does not fit into int64 tinybars fails to convert" {
    // given
    let tooMuch = Hbar{amount: 93, unit: HbarUnit.GIGABAR}

    // when / then
    @@expectThrows(invalid-argument-error)
    tooMuch.toTinybars()
    @@expectThrows(invalid-argument-error)
    tooMuch.toBaseUnits()
    @@expectThrows(invalid-argument-error)
    tooMuch.to(targetUnit: HbarUnit.TINYBAR)
    @@expectThrows(invalid-argument-error)
    Hbar{amount: -93, unit: HbarUnit.GIGABAR}.toTinybars()
}

// Source: this spec (`rule:hedera-mainnet-setting`).
@@testId("the-mainnet-setting-has-the-hedera-mainnet-its-mirror-node-and-its-consensus-nodes")
@@rule("hedera-mainnet-setting")
test "the mainnet setting has the Hedera mainnet, its mirror node and its consensus nodes" {
    // when
    let setting = ledger.config.getNetworkSetting(identifier: HEDERA_MAINNET_IDENTIFIER)

    // then
    assertInstanceOf(expected: HederaNetworkSetting, actual: setting)
    assertEquals(expected: hex(text: "00"), actual: setting.network.id)
    assertEquals(expected: "mainnet", actual: setting.network.name)
    assertEquals(expected: 1, actual: length(value: setting.getMirrorNodes))
    assertTrue(condition: contains(value: setting.getMirrorNodes,
            element: ledger.MirrorNode{restBaseUrl: "https://mainnet-public.mirrornode.hedera.com/api/v1"}))
    assertEquals(expected: length(value: MAINNET_NODES), actual: length(value: setting.getConsensusNodes))
    for ip in keys(value: MAINNET_NODES) {
        assertTrue(condition: contains(value: setting.getConsensusNodes, element: ledger.ConsensusNode{
                ip: ledger.IpAddress.fromString(value: ip), port: 50211,
                account: ledger.AccountId.fromString(value: MAINNET_NODES[ip])}), message: ip)
    }
}

// Source: this spec (`rule:hedera-testnet-setting`).
@@testId("the-testnet-setting-has-the-hedera-testnet-its-mirror-node-and-its-consensus-nodes")
@@rule("hedera-testnet-setting")
test "the testnet setting has the Hedera testnet, its mirror node and its consensus nodes" {
    // when
    let setting = ledger.config.getNetworkSetting(identifier: HEDERA_TESTNET_IDENTIFIER)

    // then
    assertInstanceOf(expected: HederaNetworkSetting, actual: setting)
    assertEquals(expected: hex(text: "01"), actual: setting.network.id)
    assertEquals(expected: "testnet", actual: setting.network.name)
    assertEquals(expected: 1, actual: length(value: setting.getMirrorNodes))
    assertTrue(condition: contains(value: setting.getMirrorNodes,
            element: ledger.MirrorNode{restBaseUrl: "https://testnet.mirrornode.hedera.com/api/v1"}))
    assertEquals(expected: length(value: TESTNET_NODES), actual: length(value: setting.getConsensusNodes))
    for ip in keys(value: TESTNET_NODES) {
        assertTrue(condition: contains(value: setting.getConsensusNodes, element: ledger.ConsensusNode{
                ip: ledger.IpAddress.fromString(value: ip), port: 50211,
                account: ledger.AccountId.fromString(value: TESTNET_NODES[ip])}), message: ip)
    }
}
```

## Questions & Comments
- **Keeping the node lists current.** The consensus nodes of `rule:hedera-mainnet-setting` and
  `rule:hedera-testnet-setting` are a snapshot; the address book of a network changes. Should a client update them at
  runtime (from the address book file or the `/network/nodes` endpoint of the mirror node), and who updates the
  snapshot in the specs? — open
