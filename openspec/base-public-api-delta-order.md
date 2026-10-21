# Base Public API Delta Order

Each item below can be created as an independent common delta change:

```text
openspec-common-delta-changes/changes/<change-name>/
├── proposal.md
└── spec.md
```

The order follows API dependencies. A change should start only after its dependencies are stable.

| Order | Delta change | API covered | Depends on |
| ---: | --- | --- | --- |
| 1 | `add-common-pagination-public-api` | `Page<T>` and navigation errors | None |
| 2 | `add-native-token-public-api` | `NativeTokenUnit`, `NativeToken`, and conversions | None |
| 3 | `add-exchange-rate-public-api` | `ExchangeRate` and expiration | None |
| 4 | `add-network-identity-public-api` | `Network<T>` identity, name, and native-token unit | Native token |
| 5 | `add-numeric-address-public-api` | `BaseAddress`, `Address`, checksums, parsing, and `ZERO_ADDRESS` | Network identity |
| 6 | `add-evm-address-public-api` | Twenty-byte `EvmAddress`, parsing, and formatting | None |
| 7 | `add-contract-id-public-api` | `EvmCapableAddress`, `ContractId`, selectors, and `ZERO_CONTRACT_ID` | Numeric and EVM addresses |
| 8 | `add-account-id-public-api` | `AccountId`, numeric/EVM/key-alias selectors, and `ZERO_ACCOUNT_ID` | Numeric and EVM addresses |
| 9 | `add-transaction-id-public-api` | `TransactionId`, generation, parsing, and formatting | Account ID |
| 10 | `add-network-endpoints-public-api` | `IpAddress`, `ConsensusNode`, and `MirrorNode` | Account ID |
| 11 | `add-network-setting-public-api` | `NetworkSetting` with consensus and mirror nodes | Network identity and endpoints |
| 12 | `add-network-setting-registry-public-api` | Registering and retrieving network settings | Network setting |
| 13 | `add-hbar-public-api` | `HbarUnit`, `Hbar`, and tinybar conversion | Native token |
| 14 | `add-hedera-network-settings-public-api` | Mainnet/testnet identifiers and `HederaNetworkSetting` | HBAR and network setting |
| 15 | `add-solo-network-setting-public-api` | `SOLO_IDENTIFIER` and `SoloNetworkSetting` | Network setting |
| 16 | `add-key-core-public-api` | Key types, algorithms, key pairs, signing, and verification | None |
| 17 | `add-key-format-public-api` | Raw, DER, PEM, PKCS#8, SPKI, hex, and Base64 formats | Key core |
| 18 | `add-key-factory-public-api` | Key generation, derivation, import, and export | Key core and formats |
| 19 | `add-authority-public-api` | Key, contract, delegatable, and threshold authorities | Public key and Contract ID |
| 20 | `add-token-classification-public-api` | `TokenType` and `TokenSupplyType` | None |
| 21 | `add-grpc-method-descriptor-public-api` | `MethodDescriptor` | None |
| 22 | `add-http-message-public-api` | HTTP methods, configuration, request, and response types | None |
| 23 | `add-http-client-public-api` | Async execution, failures, lifecycle, and factory | HTTP messages |

## Not Ready for a Delta

`spec/base/proto.md` currently declares only an empty namespace. It needs concrete public behavior before it can become
a useful delta change.
