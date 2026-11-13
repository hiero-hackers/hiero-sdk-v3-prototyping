/// Support for the Hedera network, the public network built on Hiero. It provides the identifiers of
/// the Hedera mainnet and testnet, which can be used to look up their network settings, and `Hbar`,
/// the native token of Hedera, together with its units (`HbarUnit`) from tinybar up to gigabar.
///
/// The SDK knows the network settings of the Hedera mainnet and testnet; every SDK knows the same:
///
/// `rule:hedera-mainnet-setting` The network setting of `HEDERA_MAINNET_IDENTIFIER` is a `HederaNetworkSetting` of the
/// network with the id `0x00` and the name `mainnet`, with the mirror node
/// `https://mainnet-public.mirrornode.hedera.com/api/v1` and these 52 consensus nodes, each with the gRPC port 50211
/// (plaintext):
///
/// | Node account | IP addresses |
/// |---|---|
/// | `0.0.3` | `34.239.82.6`, `35.237.200.180` |
/// | `0.0.4` | `3.130.52.236`, `35.186.191.247` |
/// | `0.0.5` | `3.18.18.254`, `35.192.2.25`, `74.50.117.35`, `23.111.186.250`, `107.155.64.98` |
/// | `0.0.7` | `3.114.54.4`, `35.203.82.240` |
/// | `0.0.8` | `35.236.5.219`, `35.183.66.150` |
/// | `0.0.9` | `35.181.158.250`, `35.197.192.225` |
/// | `0.0.10` | `177.154.62.234`, `3.248.27.48`, `35.242.233.154` |
/// | `0.0.12` | `35.204.86.32`, `35.177.162.180` |
/// | `0.0.13` | `34.215.192.104`, `35.234.132.107` |
/// | `0.0.14` | `52.8.21.141`, `35.236.2.27` |
/// | `0.0.15` | `35.228.11.53`, `3.121.238.26` |
/// | `0.0.18` | `141.94.175.187` |
/// | `0.0.20` | `34.82.78.255`, `52.39.162.216` |
/// | `0.0.21` | `34.76.140.109`, `13.36.123.209` |
/// | `0.0.22` | `52.78.202.34`, `34.64.141.166` |
/// | `0.0.23` | `3.18.91.176`, `35.232.244.145`, `69.167.169.208` |
/// | `0.0.24` | `34.89.103.38`, `18.135.7.211` |
/// | `0.0.25` | `34.93.112.7`, `13.232.240.207` |
/// | `0.0.28` | `35.198.220.75`, `18.139.47.5` |
/// | `0.0.29` | `54.74.60.120`, `34.142.71.129`, `80.85.70.197` |
/// | `0.0.31` | `217.76.57.165`, `3.77.94.254`, `34.107.78.179` |
/// | `0.0.33` | `18.136.65.22`, `34.142.172.228` |
/// | `0.0.34` | `34.16.139.248`, `35.155.212.90` |
///
/// `rule:hedera-testnet-setting` The network setting of `HEDERA_TESTNET_IDENTIFIER` is a `HederaNetworkSetting` of the
/// network with the id `0x01` and the name `testnet`, with the mirror node `https://testnet.mirrornode.hedera.com/api/v1`
/// and these 14 consensus nodes, each with the gRPC port 50211 (plaintext):
///
/// | Node account | IP addresses |
/// |---|---|
/// | `0.0.3` | `34.94.106.61`, `50.18.132.211` |
/// | `0.0.4` | `3.212.6.13`, `35.237.119.55` |
/// | `0.0.5` | `35.245.27.193`, `52.20.18.86` |
/// | `0.0.6` | `34.83.112.116`, `54.70.192.33` |
/// | `0.0.7` | `34.94.160.4`, `54.176.199.109` |
/// | `0.0.8` | `35.155.49.147`, `34.106.102.218` |
/// | `0.0.9` | `34.133.197.230`, `52.14.252.207` |
package org.hiero.hedera;
