# Hedera API

## Description

The Hedera API defines the built-in settings for the Hedera public networks.
Mainnet and testnet settings provide immutable bootstrap snapshots of the
consensus-node endpoints published in each network address book and the canonical
public mirror-node REST endpoint.

Consensus-node topology is dynamic. The built-in settings are suitable for
initial connectivity, and a client's underlying refresh process periodically
replaces its internal consensus-node snapshot after retrieving an updated
address book. A `HederaNetworkSetting` and its previously returned collections
must never be mutated.

## API Schema — Abstraction

```
namespace hedera
requires {NetworkSetting} from ledger.config
requires {NativeToken, NativeTokenUnit} from nativeToken

constant HEDERA_MAINNET_IDENTIFIER:string = "hedera-mainnet" // identifier for the Hedera mainnet
constant HEDERA_TESTNET_IDENTIFIER:string = "hedera-testnet" // identifier for the Hedera testnet
constant HEDERA_MAINNET_MIRROR_REST_BASE_URL:string = "https://mainnet.mirrornode.hedera.com/api/v1"
constant HEDERA_TESTNET_MIRROR_REST_BASE_URL:string = "https://testnet.mirrornode.hedera.com/api/v1"

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

## Built-in Network Settings

An SDK that provides the `hedera` namespace must make both built-in settings
available through `ledger.config.getNetworkSetting`:

| Identifier | Network ID | Network name | Consensus address-book source | Mirror-node REST base URL |
| --- | --- | --- | --- | --- |
| `HEDERA_MAINNET_IDENTIFIER` | single byte `0x00` | `mainnet` | `https://mainnet.mirrornode.hedera.com/api/v1/network/nodes` | `https://mainnet.mirrornode.hedera.com/api/v1` |
| `HEDERA_TESTNET_IDENTIFIER` | single byte `0x01` | `testnet` | `https://testnet.mirrornode.hedera.com/api/v1/network/nodes` | `https://testnet.mirrornode.hedera.com/api/v1` |

Each identifier must resolve to a `HederaNetworkSetting` with the following
properties:

- `network.id` and `network.name` have the values in the table above.
- `network.nativeTokenUnit` is `HbarUnit.TINYBAR`.
- `getConsensusNodes` is a non-empty immutable snapshot of every usable service
  endpoint in the public address book bundled by that SDK release.
- Each published `(IP address, port, node account)` tuple is represented by one
  `ConsensusNode`. Multiple entries may therefore have the same node account when
  that node publishes multiple IP addresses or both plaintext and TLS ports.
- `getMirrorNodes` is an immutable set containing a `MirrorNode` whose
  `restBaseUrl` is the value in the table above.
- The mirror-node URLs are fixed bootstrap configuration. They are not
  dynamically discovered or replaced by consensus-node refresh.
- No setting retrieval performs network access. The consensus address-book URLs
  identify the authoritative refresh source used by the client's periodic
  refresh process.

### Consensus-node Reference Snapshot

The following public address-book snapshot was verified on 2026-08-26. It makes
the bootstrap data used by this specification reviewable; it is not a promise
that network topology will remain unchanged. An SDK release must bundle this
snapshot or a newer snapshot retrieved from the corresponding address-book
source in the table above.

For every row, the setting contains one `ConsensusNode` for each combination of
the listed IP addresses and ports, using the row's node account.

#### Mainnet

| Node account | IP addresses | Ports |
| --- | --- | --- |
| `0.0.3` | `34.64.136.190`, `43.202.249.148` | `50211`, `50212` |
| `0.0.4` | `3.130.52.236`, `35.186.191.247` | `50211`, `50212` |
| `0.0.7` | `3.114.54.4`, `35.203.82.240` | `50211`, `50212` |
| `0.0.8` | `16.170.54.28`, `35.228.232.18` | `50211`, `50212` |
| `0.0.9` | `35.181.158.250`, `35.197.192.225` | `50211`, `50212` |
| `0.0.10` | `3.248.27.48`, `35.242.233.154` | `50211`, `50212` |
| `0.0.12` | `35.177.162.180`, `35.204.86.32` | `50211`, `50212` |
| `0.0.13` | `34.215.192.104`, `35.234.132.107` | `50211`, `50212` |
| `0.0.14` | `35.236.2.27`, `52.8.21.141` | `50211`, `50212` |
| `0.0.15` | `3.121.238.26`, `35.228.11.53` | `50211`, `50212` |
| `0.0.18` | `141.94.175.187` | `50211`, `50212` |
| `0.0.20` | `34.82.78.255`, `52.39.162.216` | `50211`, `50212` |
| `0.0.21` | `13.36.123.209`, `34.76.140.109` | `50211`, `50212` |
| `0.0.22` | `34.64.141.166`, `52.78.202.34` | `50211`, `50212` |
| `0.0.23` | `69.167.169.208` | `50211`, `50212` |
| `0.0.24` | `18.135.7.211`, `34.89.103.38` | `50211`, `50212` |
| `0.0.25` | `89.38.98.73` | `50211`, `50212` |
| `0.0.28` | `213.163.75.152` | `50211`, `50212` |
| `0.0.29` | `80.85.70.197` | `50211`, `50212` |
| `0.0.33` | `13.200.238.211` | `50211`, `50212` |
| `0.0.34` | `13.62.169.41`, `34.51.228.29` | `50211`, `50212` |
| `0.0.35` | `155.204.19.218`, `155.204.19.219` | `50211`, `50212` |
| `0.0.36` | `13.134.89.184`, `34.147.147.145` | `50211`, `50212` |
| `0.0.37` | `46.62.148.213`, `95.216.139.215` | `50211`, `50212` |
| `0.0.38` | `82.223.239.32`, `82.223.240.24` | `50211`, `50212` |

#### Testnet

| Node account | IP addresses | Ports |
| --- | --- | --- |
| `0.0.3` | `34.94.106.61`, `50.18.132.211` | `50211`, `50212` |
| `0.0.4` | `3.212.6.13`, `35.237.119.55` | `50211`, `50212` |
| `0.0.5` | `35.245.27.193`, `52.20.18.86` | `50211`, `50212` |
| `0.0.6` | `34.83.112.116`, `54.70.192.33` | `50211`, `50212` |
| `0.0.7` | `34.94.160.4`, `54.176.199.109` | `50211`, `50212` |
| `0.0.8` | `34.106.102.218`, `35.155.49.147` | `50211`, `50212` |
| `0.0.9` | `34.133.197.230`, `52.14.252.207` | `50211`, `50212` |

The concrete setting classes, address-book resource format, parsing, caching,
refresh scheduling, endpoint selection, health checking, and connection
management are implementation details and are not part of this API.

## Examples

The following example obtains the Hedera testnet bootstrap endpoints without
depending on a concrete setting implementation:

```
NetworkSetting setting = getNetworkSetting(HEDERA_TESTNET_IDENTIFIER)
set<ConsensusNode> consensusNodes = setting.getConsensusNodes
set<MirrorNode> mirrorNodes = setting.getMirrorNodes
```

## Testing

### `hedera.networks/mainnet-setting-exposes-public-endpoints`

 - **Given** an SDK that provides the built-in Hedera network settings.
 - **When** the network setting identified by `HEDERA_MAINNET_IDENTIFIER` is retrieved.
 - **Then** it is a Hedera network setting with network ID `0x00`, name `mainnet`, a non-empty immutable consensus-node set, and the canonical mainnet mirror-node REST base URL.

### `hedera.networks/testnet-setting-exposes-public-endpoints`

 - **Given** an SDK that provides the built-in Hedera network settings.
 - **When** the network setting identified by `HEDERA_TESTNET_IDENTIFIER` is retrieved.
 - **Then** it is a Hedera network setting with network ID `0x01`, name `testnet`, a non-empty immutable consensus-node set, and the canonical testnet mirror-node REST base URL.

### `hedera.networks/consensus-node-snapshot-is-valid`

 - **Given** either built-in Hedera network setting.
 - **When** its consensus-node snapshot is inspected.
 - **Then** every entry has a four-byte IP address, a valid unsigned 16-bit port, and a node account, and every published endpoint bundled by the SDK release is represented.

### `hedera.networks/periodic-refresh-does-not-mutate-setting`

 - **Given** a client created from a previously retrieved built-in Hedera network setting.
 - **When** its underlying process refreshes the consensus-node addresses from a newer address book.
 - **Then** the client uses a new immutable consensus-node snapshot and the network setting remains unchanged.

## Questions & Comments

- `ConsensusNode` currently represents one IPv4 endpoint. If a Hedera public
  address book publishes a domain-only or IPv6 endpoint, `ledger.ConsensusNode`
  must evolve before that endpoint can be represented without information loss.
