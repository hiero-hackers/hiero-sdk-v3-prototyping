# Specification-driven comparison of the Java base API

## 1. Scope and assessment rules

Compared module: `sdk-java/hiero-sdk-base-api`. Every Java source, test, package descriptor, and module descriptor is included. Maven configuration is discussed separately where it affects verification or consumption.

| Branch | Exact commit |
|---|---|
| `feat/java-base-public-api-openai` | `d3fd977fea6786fefc7234b6bdcf41c59e705c3a` |
| `feat/java-base-public-api-claude` | `1da79859020daadc922d7ff92f92855f33ca7b69` |

These are the same frozen commits used in the earlier comparison. All 62 extracted Java files were checked against their Git blob hashes. The review used independent extracted snapshots. This report is committed separately on a branch based on main.

**Result:** both implementations pass the tested shared core contracts. They differ in public API compatibility, address input/construction policy, exception nullability, representation, documentation, and verification depth. There is no unconditional overall winner established by this review.

### Contract sources and precedence

1. Shared numbered OpenSpec deltas define this feature scope. Existing broader ledger specifications include future types outside these six changes; their absence from both branches is not counted as a defect here.
2. Shared Java mapping guidelines and the accepted address-hierarchy ADR inform language mapping and extension semantics.
3. Each branch's README/Javadoc records its own additional policies. A policy declared only in OpenAI's README is not automatically a requirement on Claude, and vice versa. Historical approval claims in a README are treated as documentary claims, not independently verified authorization.
4. HIP-15, EIP-55, and JSpecify establish external rules where relevant. External-standard compliance is distinguished from local shared-delta compliance.

Shared reference files: [pagination](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/openspec/changes/1-add-common-pagination-public-api/specs/common-pagination/spec.md), [native token](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/openspec/changes/2-add-native-token-public-api/specs/native-token/spec.md), [exchange rate](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/openspec/changes/3-add-exchange-rate-public-api/specs/exchange-rate/spec.md), [network](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/openspec/changes/4-add-network-identity-public-api/specs/network-identity/spec.md), [numeric address](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/openspec/changes/5-add-numeric-address-public-api/specs/numeric-address/spec.md), [EVM address](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/openspec/changes/6-add-evm-address-public-api/specs/evm-address/spec.md), [Java mapping guideline](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/guidelines/api-best-practices-java.md), [address ADR](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/docs/adr/0003-three-level-address-hierarchy-with-nullability-narrowing.md).

Each finding uses one of these classifications:

| Classification | Meaning |
|---|---|
| Confirmed defect | Reproduced violation of an established, identified requirement |
| Compatibility difference | A caller, subclass, analyzer, or observable output changes when switching branches |
| Unspecified behavior | The shared contract leaves a policy open; an observed inconsistency may still warrant resolution |
| Implementation trade-off | Internal structure differs without an established observable-contract violation |
| Documentation/style difference | Clarity, conventions, or redundant declarations differ |

Severity is expressed through concrete migration impact rather than a numerical branch score. No weighted score or test count is used to declare a winner.

## 2. Complete file inventory

| Inventory | Count |
|---|---:|
| Java files per branch | 31 |
| Unique paths across both branches | 32 |
| Shared paths | 30 |
| Shared identical / shared different | 2 / 28 |
| OpenAI only / Claude only | 1 / 1 |

### Production files

Paths relative to `sdk-java/hiero-sdk-base-api/`.

| File | OpenAI | Claude | Content status |
|---|---|---|---|
| `src/main/java/module-info.java` | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/main/java/module-info.java) | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/main/java/module-info.java) | Identical |
| `src/main/java/org/hiero/sdk/v3/common/MirrorNodeException.java` | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/common/MirrorNodeException.java) | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/common/MirrorNodeException.java) | Different |
| `src/main/java/org/hiero/sdk/v3/common/Page.java` | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/common/Page.java) | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/common/Page.java) | Different |
| `src/main/java/org/hiero/sdk/v3/common/package-info.java` | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/common/package-info.java) | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/common/package-info.java) | Identical |
| `src/main/java/org/hiero/sdk/v3/ledger/Address.java` | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/ledger/Address.java) | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/ledger/Address.java) | Different |
| `src/main/java/org/hiero/sdk/v3/ledger/BaseAddress.java` | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/ledger/BaseAddress.java) | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/ledger/BaseAddress.java) | Different |
| `src/main/java/org/hiero/sdk/v3/ledger/EvmAddress.java` | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/ledger/EvmAddress.java) | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/ledger/EvmAddress.java) | Different |
| `src/main/java/org/hiero/sdk/v3/ledger/Keccak256.java` | Missing | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/ledger/Keccak256.java) | Claude only |
| `src/main/java/org/hiero/sdk/v3/ledger/Network.java` | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/ledger/Network.java) | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/ledger/Network.java) | Different |
| `src/main/java/org/hiero/sdk/v3/ledger/package-info.java` | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/ledger/package-info.java) | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/ledger/package-info.java) | Different |
| `src/main/java/org/hiero/sdk/v3/nativeToken/ExchangeRate.java` | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/nativeToken/ExchangeRate.java) | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/nativeToken/ExchangeRate.java) | Different |
| `src/main/java/org/hiero/sdk/v3/nativeToken/NativeToken.java` | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/nativeToken/NativeToken.java) | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/nativeToken/NativeToken.java) | Different |
| `src/main/java/org/hiero/sdk/v3/nativeToken/NativeTokenUnit.java` | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/nativeToken/NativeTokenUnit.java) | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/nativeToken/NativeTokenUnit.java) | Different |
| `src/main/java/org/hiero/sdk/v3/nativeToken/package-info.java` | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/nativeToken/package-info.java) | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/nativeToken/package-info.java) | Different |

### Test files

Paths relative to `sdk-java/hiero-sdk-base-api/`.

| File | OpenAI | Claude | Content status |
|---|---|---|---|
| `src/test/java/org/hiero/sdk/v3/common/contract/ModuleConsumerTest.java` | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/test/java/org/hiero/sdk/v3/common/contract/ModuleConsumerTest.java) | Missing | OpenAI only |
| `src/test/java/org/hiero/sdk/v3/common/contract/PageContractTest.java` | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/test/java/org/hiero/sdk/v3/common/contract/PageContractTest.java) | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/test/java/org/hiero/sdk/v3/common/contract/PageContractTest.java) | Different |
| `src/test/java/org/hiero/sdk/v3/common/contract/PublicApiShapeTest.java` | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/test/java/org/hiero/sdk/v3/common/contract/PublicApiShapeTest.java) | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/test/java/org/hiero/sdk/v3/common/contract/PublicApiShapeTest.java) | Different |
| `src/test/java/org/hiero/sdk/v3/ledger/contract/AddressApiShapeTest.java` | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/test/java/org/hiero/sdk/v3/ledger/contract/AddressApiShapeTest.java) | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/test/java/org/hiero/sdk/v3/ledger/contract/AddressApiShapeTest.java) | Different |
| `src/test/java/org/hiero/sdk/v3/ledger/contract/AddressContractTest.java` | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/test/java/org/hiero/sdk/v3/ledger/contract/AddressContractTest.java) | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/test/java/org/hiero/sdk/v3/ledger/contract/AddressContractTest.java) | Different |
| `src/test/java/org/hiero/sdk/v3/ledger/contract/AddressModuleConsumerTest.java` | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/test/java/org/hiero/sdk/v3/ledger/contract/AddressModuleConsumerTest.java) | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/test/java/org/hiero/sdk/v3/ledger/contract/AddressModuleConsumerTest.java) | Different |
| `src/test/java/org/hiero/sdk/v3/ledger/contract/EvmAddressApiShapeTest.java` | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/test/java/org/hiero/sdk/v3/ledger/contract/EvmAddressApiShapeTest.java) | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/test/java/org/hiero/sdk/v3/ledger/contract/EvmAddressApiShapeTest.java) | Different |
| `src/test/java/org/hiero/sdk/v3/ledger/contract/EvmAddressContractTest.java` | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/test/java/org/hiero/sdk/v3/ledger/contract/EvmAddressContractTest.java) | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/test/java/org/hiero/sdk/v3/ledger/contract/EvmAddressContractTest.java) | Different |
| `src/test/java/org/hiero/sdk/v3/ledger/contract/EvmAddressModuleConsumerTest.java` | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/test/java/org/hiero/sdk/v3/ledger/contract/EvmAddressModuleConsumerTest.java) | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/test/java/org/hiero/sdk/v3/ledger/contract/EvmAddressModuleConsumerTest.java) | Different |
| `src/test/java/org/hiero/sdk/v3/ledger/contract/NetworkApiShapeTest.java` | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/test/java/org/hiero/sdk/v3/ledger/contract/NetworkApiShapeTest.java) | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/test/java/org/hiero/sdk/v3/ledger/contract/NetworkApiShapeTest.java) | Different |
| `src/test/java/org/hiero/sdk/v3/ledger/contract/NetworkContractTest.java` | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/test/java/org/hiero/sdk/v3/ledger/contract/NetworkContractTest.java) | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/test/java/org/hiero/sdk/v3/ledger/contract/NetworkContractTest.java) | Different |
| `src/test/java/org/hiero/sdk/v3/ledger/contract/NetworkModuleConsumerTest.java` | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/test/java/org/hiero/sdk/v3/ledger/contract/NetworkModuleConsumerTest.java) | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/test/java/org/hiero/sdk/v3/ledger/contract/NetworkModuleConsumerTest.java) | Different |
| `src/test/java/org/hiero/sdk/v3/nativeToken/contract/ExchangeRateApiShapeTest.java` | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/test/java/org/hiero/sdk/v3/nativeToken/contract/ExchangeRateApiShapeTest.java) | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/test/java/org/hiero/sdk/v3/nativeToken/contract/ExchangeRateApiShapeTest.java) | Different |
| `src/test/java/org/hiero/sdk/v3/nativeToken/contract/ExchangeRateContractTest.java` | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/test/java/org/hiero/sdk/v3/nativeToken/contract/ExchangeRateContractTest.java) | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/test/java/org/hiero/sdk/v3/nativeToken/contract/ExchangeRateContractTest.java) | Different |
| `src/test/java/org/hiero/sdk/v3/nativeToken/contract/ExchangeRateModuleConsumerTest.java` | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/test/java/org/hiero/sdk/v3/nativeToken/contract/ExchangeRateModuleConsumerTest.java) | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/test/java/org/hiero/sdk/v3/nativeToken/contract/ExchangeRateModuleConsumerTest.java) | Different |
| `src/test/java/org/hiero/sdk/v3/nativeToken/contract/NativeTokenApiShapeTest.java` | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/test/java/org/hiero/sdk/v3/nativeToken/contract/NativeTokenApiShapeTest.java) | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/test/java/org/hiero/sdk/v3/nativeToken/contract/NativeTokenApiShapeTest.java) | Different |
| `src/test/java/org/hiero/sdk/v3/nativeToken/contract/NativeTokenContractTest.java` | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/test/java/org/hiero/sdk/v3/nativeToken/contract/NativeTokenContractTest.java) | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/test/java/org/hiero/sdk/v3/nativeToken/contract/NativeTokenContractTest.java) | Different |
| `src/test/java/org/hiero/sdk/v3/nativeToken/contract/NativeTokenModuleConsumerTest.java` | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/test/java/org/hiero/sdk/v3/nativeToken/contract/NativeTokenModuleConsumerTest.java) | [Present](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/test/java/org/hiero/sdk/v3/nativeToken/contract/NativeTokenModuleConsumerTest.java) | Different |


## 3. Production files assessed using the same criteria

Each file below is evaluated for public API, behavior, validation, state ownership, documentation, compatibility, and maintainability. Source links point to the exact reviewed GitHub commits.

### Address.java

Sources: [Openai](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/ledger/Address.java) / [Claude](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/ledger/Address.java).

| Criterion | OpenAI | Claude |
|---|---|---|
| Public API | One four-argument constructor; final Address subtype | Adds three-argument constructor; same final subtype |
| Behavior | Canonical unsigned parsing and formatting; HIP-15 checksum | Same canonical output/checksum, broader input grammar |
| Validation | Checks constructor suffix syntax; rejects leading zeros/nulls/overflow | Checks null checksum; accepts malformed constructor suffixes and leading zeros; rejects overflow |
| State ownership | Uses BaseAddress selector storage | Adds private final primitive selector alongside base storage |
| Documentation | Documents strict grammar, suffix preservation, network validation boundary | Documents digits-based grammar and value semantics; constructor pattern restriction absent |
| Compatibility | Cannot consume Claude three-argument calls; inherited formatter is final | Adds constructor and accepts inputs rejected by OpenAI |
| Maintainability | Explicit types and descriptive algorithm names; one selector state | Shorter code and convenience API; redundant selector state |

### BaseAddress.java

Sources: [Openai](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/ledger/BaseAddress.java) / [Claude](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/ledger/BaseAddress.java).

| Criterion | OpenAI | Claude |
|---|---|---|
| Public API | Unsealed abstract base; protected constructor; final suffix formatter | Same shape, but suffix formatter can be overridden |
| Behavior | Canonical subtype text plus stored suffix | Same inherited behavior; subclasses may replace it |
| Validation | Rejects null checksum; nullable selector allowed | Same boundary behavior; shorter null message |
| State ownership | Private final shard, realm, nullable num, checksum | Same owned state |
| Documentation | Explicit absent-checksum and no-I/O provider obligations | No-I/O/null-network obligations; less detail about absent base checksums |
| Compatibility | Rejects a subclass overriding the formatter | Accepts that subclass; moving it to OpenAI breaks |
| Maintainability | Protects common formatting invariant | More flexible extension point, broader subclass contract |

### EvmAddress.java

Sources: [Openai](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/ledger/EvmAddress.java) / [Claude](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/ledger/EvmAddress.java).

| Criterion | OpenAI | Claude |
|---|---|---|
| Public API | Standalone record with same public factories/accessors | Same public descriptors and record shape |
| Behavior | Observed canonical EIP-55 output and byte-value semantics | Same observed output/value semantics |
| Validation | Length/prefix checks plus HexFormat; 20-byte invariant | Regex plus HexFormat; same 20-byte invariant |
| State ownership | Copies input and every returned array | Same ownership protections |
| Documentation | Detailed grammar, byte order, casing and scheme references | Documents equivalent accepted grammar and casing behavior |
| Compatibility | No observed public-descriptor/behavior incompatibility for shared inputs | Same; error-message strings differ |
| Maintainability | Private specialized Keccak implementation embedded in record | Address delegates to separate package-private helper |

### Keccak256.java

Sources: [Claude](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/ledger/Keccak256.java).

| Criterion | OpenAI | Claude |
|---|---|---|
| Public API | No separate type; private EvmAddress helper instead | Package-private final helper; no new public type |
| Behavior | Only fixed 40-byte input is needed/implemented privately | Generic byte-array hash with multi-block absorption |
| Validation | Caller is private and always supplies the fixed-length representation | No separate public boundary; current caller always supplies 40 bytes |
| State ownership | Per-call local state arrays | Per-call local state arrays; private constants |
| Documentation | Private comments describe fixed-input specialization and padding | Helper identifies original Keccak variant and internal scope |
| Compatibility | No public API obligation for helper | No public API obligation; does not export an accessible new type |
| Maintainability | Narrow algorithm surface; mixed with representation code | Better separation; general block handling needs direct tests before reuse |

### Network.java

Sources: [Openai](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/ledger/Network.java) / [Claude](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/ledger/Network.java).

| Criterion | OpenAI | Claude |
|---|---|---|
| Public API | Generic record; explicitly declared documented accessors | Same public signatures; compact constructor/generated accessors |
| Behavior | Content equality; decimal byte-array text; manual hash combination | Same equality; hexadecimal ID text; Objects.hash combination |
| Validation | Rejects null ID/unit; accepts arbitrary ID/name contents | Same validation; shorter null messages |
| State ownership | Clones ID on input/read; retains immutable unit reference | Same ownership |
| Documentation | Detailed identity semantics and accessors | Concise identity semantics; explicitly describes hex output |
| Compatibility | Different toString/hash numbers from Claude | Different toString/hash numbers from OpenAI; equality rules unchanged |
| Maintainability | More explicit record boilerplate | Compact idiomatic constructor/accessors and hex formatter |

### Page.java

Sources: [Openai](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/common/Page.java) / [Claude](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/common/Page.java).

| Criterion | OpenAI | Claude |
|---|---|---|
| Public API | Abstract Page<T>; protected constructor; final state accessors | Same API shape and generic navigation signatures |
| Behavior | Immutable shallow snapshot; provider-defined navigation/position | Same executable behavior |
| Validation | List.copyOf rejects null list/elements; no integer bounds | Same validation; null message changes to data |
| State ownership | Private final copied list, size and index | Same ownership |
| Documentation | Explicitly explains shallow snapshot and absence of numeric constraints | Shorter documentation omits those clarifications |
| Compatibility | Only redundant non-null annotations and message wording differ | Same descriptors; package null defaults remain |
| Maintainability | Explicit types/annotations; fuller explanation | Less annotation/documentation repetition |

### MirrorNodeException.java

Sources: [Openai](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/common/MirrorNodeException.java) / [Claude](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/common/MirrorNodeException.java).

| Criterion | OpenAI | Claude |
|---|---|---|
| Public API | RuntimeException subtype; message/cause declared nullable | Same constructors, declared non-null through package default |
| Behavior | Preserves message/cause, including null | Same runtime behavior including null |
| Validation | No extra validation; nullable inputs are intentional | No validation enforces its stronger declared contract |
| State ownership | Inherited Throwable state; same serialVersionUID | Same state ownership/serialization ID |
| Documentation | Explicit null allowance in constructor documentation | No null allowance documented |
| Compatibility | Nullable calls accepted by declared contract | Can introduce static-analysis failures for those calls |
| Maintainability | Declarations match observed permissive behavior | Shorter declarations but static/runtime contract divergence |

### NativeToken.java

Sources: [Openai](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/nativeToken/NativeToken.java) / [Claude](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/nativeToken/NativeToken.java).

| Criterion | OpenAI | Claude |
|---|---|---|
| Public API | Same recursive SELF/UNIT bounds and abstract conversion methods | Same public descriptors/generic bounds |
| Behavior | Stores any long amount; arithmetic delegated to providers | Same production behavior |
| Validation | Null unit rejected; to() null policy documented for providers | Null unit rejected; provider null-target obligation retained, shorter docs |
| State ownership | Private final amount and unit; final accessors | Same state ownership |
| Documentation | Fuller conversion/new-instance/provider-policy explanation | More concise conversion/provider documentation |
| Compatibility | No demonstrated descriptor/behavior break in common state API | Same; non-null defaults preserved, exception text differs |
| Maintainability | Explicit types/annotations | Less annotation repetition; no production arithmetic simplification to compare |

### NativeTokenUnit.java

Sources: [Openai](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/nativeToken/NativeTokenUnit.java) / [Claude](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/nativeToken/NativeTokenUnit.java).

| Criterion | OpenAI | Claude |
|---|---|---|
| Public API | Open interface: symbol() and baseUnitFactor() | Same interface methods |
| Behavior | Immutable provider symbol/factor contract | Same provider role |
| Validation | No numeric-factor validation is implemented | Same; no positivity enforcement |
| State ownership | No state; providers must own immutable values | Same |
| Documentation | Explicit factor-policy non-selection and symbol nullability | Concise unit meaning and enum-provider guidance |
| Compatibility | Explicit NonNull symbol | Equivalent concrete String contract under NullMarked |
| Maintainability | Small interface; no implementation code | Same; mainly wording/convention difference |

### ExchangeRate.java

Sources: [Openai](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/nativeToken/ExchangeRate.java) / [Claude](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/nativeToken/ExchangeRate.java).

| Criterion | OpenAI | Claude |
|---|---|---|
| Public API | Abstract type; protected constructor; final state accessors | Same API shape |
| Behavior | Stores ZonedDateTime/double; expiration delegated to providers | Same state behavior and expiration contract |
| Validation | Rejects null expiration; accepts all doubles and past times | Same; shorter null message |
| State ownership | Private final immutable expiration and primitive rate | Same ownership |
| Documentation | Strictly-later instant comparison; equality false, zone retained | Same essential guarantees in shorter documentation |
| Compatibility | No demonstrated descriptor/behavior break | Same; annotations/message wording differ |
| Maintainability | Explicit annotations and fuller provider-boundary text | Concise contract; neither supplies production expiration algorithm |


### Descriptors

`module-info.java` and `common/package-info.java` are byte-identical. Both expose the same three packages and use `requires static org.jspecify`; the module name is `org.hiero.sdk.v3.base`. The ledger/nativeToken package descriptors change only their summary sentences and keep `@NullMarked`. They add no executable behavior, state, validation, or public members. No compatibility difference was found in these descriptor files.

## 4. Classified findings and executable examples

| ID | Classification | Difference | Expected contract / basis | Impact | Source |
|---|---|---|---|---|---|
| F01 | Unspecified behavior | Constructor permits values its parser rejects in Claude | Shared delta lacks a constructor checksum grammar; confirmed round-trip inconsistency | Resolve before choosing constructor policy | [O](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/ledger/Address.java) / [C](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/ledger/Address.java) |
| F02 | Unspecified behavior; external-standard discrepancy | Claude accepts leading zeros; OpenAI rejects | HIP-15 rejects them, shared delta leaves precise grammar open | Decide strict HIP-15 input or documented normalization | [O](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/ledger/Address.java) / [C](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/ledger/Address.java) |
| F03 | Compatibility difference | Claude-only three-argument constructor | Shared schema does not prescribe this overload | Confirmed source and binary failure when used with OpenAI | [O](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/ledger/Address.java) / [C](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/ledger/Address.java) |
| F04 | Compatibility difference | Claude permits suffix-formatter overriding; OpenAI finalizes it | Shared schema describes suffix behavior, not Java finality | Confirmed source and binary failure for overriding subclasses | [O](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/ledger/BaseAddress.java) / [C](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/ledger/BaseAddress.java) |
| F05 | Compatibility difference | Exception nullable declarations disappear in Claude | Package NullMarked changes declared input meaning; runtime still accepts null | Potential static-analysis migration failure; analyzer not run | [O](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/common/MirrorNodeException.java) / [C](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/common/MirrorNodeException.java) |
| F06 | Unspecified behavior / compatibility difference | Network text changes decimal arrays to hex | No shared toString encoding policy identified | Observable change for diagnostics and text-dependent consumers | [O](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/ledger/Network.java) / [C](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/ledger/Network.java) |
| F07 | Implementation trade-off | Network hash formula changes | Both preserve equal-values/equal-hashes within each version | No observed defect; avoid assuming stable numeric hashes | [O](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/ledger/Network.java) / [C](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/ledger/Network.java) |
| F08 | Implementation trade-off | Claude duplicates numeric selector state | Both remain immutable and keep values consistent | Maintenance cost, no reproduced value inconsistency | [O](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/ledger/Address.java) / [C](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/ledger/Address.java) |
| F09 | Documentation/style difference | Explicit NonNull versus package defaults | Most concrete-reference nullness meaning remains equivalent | Direct annotation reflection differs; not a general nullness regression | [O](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/common/Page.java) / [C](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/common/Page.java) |
| F10 | Implementation trade-off | Claude extracts a general Keccak helper | No public API addition; canonical formatting agrees in checked cases | Prefer separation conditionally; add direct general-input tests | [O](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/ledger/EvmAddress.java) / [C](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/hiero-sdk-base-api/src/main/java/org/hiero/sdk/v3/ledger/EvmAddress.java) |

No unconditional violation of the common core contract was reproduced in the checks performed. This is a bounded result, not a completeness proof. F01 is a confirmed behavioral inconsistency with missing shared policy; F02 would violate a strict HIP-15 input requirement if that requirement is adopted for this API.


### Address construction and parser agreement

```java
Address value = new Address(1, 2, 3, "ABC");
String text = value.toStringWithChecksum();
Address.fromString(text);
```

OpenAI rejects construction. Claude constructs the value, produces `1.2.3-ABC`, and then rejects that text. The discrepancy is confirmed. The local shared delta does not explicitly impose a checksum pattern on the Java constructor, so this report classifies the policy as unspecified with a round-trip inconsistency, rather than asserting an unconditional shared-spec defect. A consistent construction/parser policy should be selected before release.

Neither constructor verifies network-specific checksum correctness. Both parsers preserve syntactically valid suffixes, with validation requested separately through `validateChecksum(network)`. Calling either parser alone does not establish that a checksum is correct or an entity exists.

### Numeric input grammar

| Input | OpenAI | Claude |
|---|---|---|
| `001.02.0003` | IllegalArgumentException | Formats as `1.2.3` |
| `00000000000000000000000001.0.0` | IllegalArgumentException | Formats as `1.0.0` |
| `0.0.123-vfmkw` | Accepted | Accepted |
| `18446744073709551616.0.0` | IllegalArgumentException | IllegalArgumentException |

HIP-15 rejects leading-zero address components. OpenAI follows that grammar; Claude accepts a broader grammar. The shared local address delta does not explicitly adopt this restriction, so unconditional local noncompliance is not established. OpenAI's branch README explicitly selects canonical parsing; Claude's README records implementation choices as open for shared-contract revision. [HIP-15 specification](https://raw.githubusercontent.com/hiero-ledger/hiero-improvement-proposals/main/HIP/hip-15.md).

### Reproduced source and binary compatibility failures

| Claude consumer feature | Compile against Claude | Compile against OpenAI | Claude-compiled consumer run against OpenAI |
|---|---|---|---|
| `new Address(1, 2, 3)` | Pass | Missing constructor | NoSuchMethodError |
| Override `BaseAddress.toStringWithChecksum()` | Pass | Cannot override final method | IncompatibleClassChangeError |

The same compiled consumers run successfully with Claude. These are conditional migration failures for consumers using the differing features, not a claim that all consumers fail. The compiler/runtime results were captured by local review harnesses; representative reproductions appear below.

### Nullability and representation

Claude normally relies on the retained package-level `@NullMarked` defaults; removing redundant `@NonNull` does not erase the corresponding concrete-reference guarantee. Its overridden Address.num() still narrows the nullable base return. MirrorNodeException differs: removing `@Nullable` changes its declared inputs while both constructors still accept null at runtime. This static-contract conclusion follows the declarations and JSpecify rules; no static nullness checker was executed. [JSpecify specification](https://jspecify.dev/docs/spec/).

Network IDs `{0,15,-1}` render as `[0, 15, -1]` in OpenAI and `000fff` in Claude. Hash formulas differ too, while equality semantics and within-implementation hash consistency remain correct. Neither textual formatting nor numeric hash stability across branches is established by the shared network delta.

## 5. Validation and test quality

### Common checks applied to both implementations

The **same source** was compiled against each unmodified API with `javac --release 21 -Xlint:all -Werror`, then executed as an independent named module in separate JVMs without the annotation library on the runtime module path. Both pass **99 shared assertions**.

| Check group | Evidence and limit |
|---|---|
| Module consumption | Both run as named modules, export the expected packages, and run without JSpecify resolved |
| Address | Shared valid/invalid grammar, unsigned boundaries, sentinel, nulls, equality, known checksum/mismatch/absence |
| Network | Defensive copies, typed units, nullable names, null inputs, stable content equality/hash |
| EVM | Published vector, malformed inputs, byte lengths, nulls, copies, equality/hash, round trips |
| Page | Snapshot, unmodifiable data, null list/elements, declared state, provider-stage completion/failure |
| Native token | Signed amount boundaries, null unit, state retention, exactly representable provider conversion |
| Exchange rate | Stored zone/rate bits, null expiration, provider examples for past/equal/future instants |

Abstract-method checks exercise consumer-provided Page/Token/Rate examples. They validate API usability and stored state; they do not prove production navigation, rounding, overflow handling, or clock behavior absent from both API modules.

The common consumer and its per-branch logs were generated locally during the review. The results above are recorded here; those temporary harness files are not included in this documentation-only commit.

### Additional evidence retained from the earlier run

| Validation | OpenAI | Claude | Interpretation |
|---|---|---|---|
| Original `mvn -o -B test` | 273 pass | 149 pass | Own suites, different case distributions |
| Strict `mvn -o -B javadoc:javadoc` | Pass | Pass | Configured doclint/all and failOnWarnings checks |
| 10,000 generated EVM values | Pass | Pass | Identical text/hash; successful byte round trips |
| 2,000 generated numeric/checksum values | Pass | Pass | Unsigned components; ledger lengths 0..72; reference-formula and round-trip checks |
| Compiled public API inspection | Inspected | Inspected | Added constructor and changed final modifier are substantive differences |

The additional checksum formula in the probe is independently written but follows the same published arithmetic; it is not an independent cryptographic oracle. Randomized agreement alone is not proof of correctness. Both existing EVM suites also include the published [EIP-55 reference vectors](https://eips.ethereum.org/EIPS/eip-55).

### File-by-file test assessment

| Test file | OpenAI cases | Claude cases | Substantive comparison |
|---|---:|---:|---|
| `AddressApiShapeTest.java` | 5 | 5 | Same count, different assertions: OpenAI prohibits child state and pins finality/explicit annotations; Claude allows its design. |
| `AddressContractTest.java` | 82 | 40 | OpenAI checks constructor suffix grammar, leading zeros, wider invalid inputs and published multi-byte-ledger vectors. |
| `AddressModuleConsumerTest.java` | 4 | 3 | OpenAI fresh JVM/diagnostic checks; Claude in-process compiler/module layer; both check illegal subclassing/return types. |
| `EvmAddressApiShapeTest.java` | 3 | 4 | OpenAI has stricter annotation/signature assertions; Claude explicitly excludes BaseAddress inheritance and interfaces. |
| `EvmAddressContractTest.java` | 64 | 47 | Both test vectors, ownership, parsing and round trips; OpenAI has broader parameterized invalid inputs. |
| `EvmAddressModuleConsumerTest.java` | 1 | 1 | Same scenario count; fresh process in OpenAI versus child module layer in Claude. |
| `ExchangeRateApiShapeTest.java` | 4 | 4 | Same count, different breadth: OpenAI includes annotation/export/signature checks; Claude emphasizes state/no setters. |
| `ExchangeRateContractTest.java` | 21 | 7 | OpenAI covers raw double bits including signed zero and a broader zone/value matrix; both use fixed-clock providers. |
| `ExchangeRateModuleConsumerTest.java` | 1 | 1 | Both demonstrate stored state/expiration provider use; fresh JVM versus module layer. |
| `ModuleConsumerTest.java` | 2 | Absent | OpenAI-only: fresh-JVM Page consumer and final-accessor rejection; no Claude counterpart. |
| `NativeTokenApiShapeTest.java` | 5 | 4 | Both recursive bounds; OpenAI also checks annotations, exact member sets and signature boundaries. |
| `NativeTokenContractTest.java` | 23 | 7 | OpenAI has broader amount/factor/conversion matrix and explicit provider null-target checks; both use example arithmetic. |
| `NativeTokenModuleConsumerTest.java` | 3 | 2 | OpenAI exercises more negative generic cases and fresh runtime; Claude uses an in-process module layer. |
| `NetworkApiShapeTest.java` | 5 | 4 | OpenAI additionally verifies explicit nullability, export, signature boundaries and precise construction shape. |
| `NetworkContractTest.java` | 17 | 8 | OpenAI adds value-equal non-enum units, metadata-read instrumentation, wider mutation/name cases; Claude pins hex text. |
| `NetworkModuleConsumerTest.java` | 3 | 3 | Both have generic-negative consumers; isolation mechanisms and diagnostics differ. |
| `PageContractTest.java` | 24 | 6 | OpenAI adds pending-stage scenarios, position combinations, integer boundaries, mutation variants, nullable exception cases. |
| `PublicApiShapeTest.java` | 6 | 3 | OpenAI additionally checks exports, requires-static dependency, nullability and exact public shape. |

Test counts include parameterized invocations. They are neither code-coverage percentages nor measurements of test effectiveness. OpenAI covers more distinct edge cases, but some reflection tests pin private field names, exact member sets, and redundant annotations, restricting harmless refactoring. Claude's original in-process ModuleLayer tests are less isolated; the new common consumer separately demonstrates no-JSpecify runtime operation for both branches.

### Build configuration context

The module POM is outside the Java inventory but explains two differences. OpenAI declares JSpecify as an optional compile dependency; Claude uses provided scope. Both retain the same requires-static production module declaration. OpenAI adds module-level CycloneDX generation; Claude configures test compiler/runtime reads of java.compiler for its in-process consumer compiler. Both parent POMs retain aggregate SBOM configuration. These are packaging/verification choices, not additional public Java types.

Branch-specific policy documentation: [OpenAI README](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/d3fd977fea6786fefc7234b6bdcf41c59e705c3a/sdk-java/README.md) and [Claude README](https://github.com/hiero-hackers/hiero-sdk-v3-prototyping/blob/1da79859020daadc922d7ff92f92855f33ca7b69/sdk-java/README.md). These documents establish what each implementation claims; only genuinely shared requirements are applied equally to both.

### Environment and limitations

OpenJDK 25.0.1 executed the checks. Production code and the new common/compatibility consumers target Java 21, but no Java 21 runtime was used. No performance benchmarks, static nullness analysis, complete binary-compatibility scanner, or exhaustive input proof was run. Original Maven/Javadoc results are retained for these exact snapshots; those commands were not repeated merely to restructure the report.

## 6. Decision table and recommended next steps

| Area | Proposed choice | Reason | Required resolution or follow-up |
|---|---|---|---|
| Shared public contracts | Keep the common structure | Both pass the tested shared scenarios | Preserve shared tests as a neutral baseline |
| Constructor checksum grammar | Prefer OpenAI validation | Avoids values that the same parser cannot read | Put the constructor invariant in the shared contract |
| Leading-zero numeric input | Prefer OpenAI if HIP-15 grammar is intended | Aligns parser with the external reference | Explicitly select strict parsing or documented normalization |
| Three-argument constructor | Requires an API decision | Useful convenience, additional long-term public commitment | Keep or omit deliberately; document migration |
| Base checksum formatting | Prefer final if the suffix rule is universal | Protects the documented inherited formatting invariant | Decide whether custom subclass formatting is a supported extension point |
| Numeric state ownership | Prefer OpenAI | One inherited selector value avoids redundant state | Preserve nullable base/non-null Address semantics |
| Keccak organization | Prefer Claude's helper separation | Keeps address representation focused | Add direct digest and block-boundary checks if retaining general-purpose behavior |
| Network text | Requires a contract decision | Hex is compact; both forms are viable | Document chosen representation if consumers may depend on it |
| Network hashes | Accept either correct formula | No observed equality/hash-contract defect | Do not treat cross-branch numeric differences as a correctness failure |
| Exception null inputs | Prefer explicit nullable declaration if preserving current permissive behavior | Matches actual constructors and RuntimeException-style usage | Document and test intended boundary behavior |
| General non-null style | Either default-aware approach can be semantically sound | Package defaults are meaningful | Align with shared Java conventions; avoid reflection-only assumptions |
| Test suite | Combine behavioral strengths | Broader scenarios plus neutral consumers provide better evidence | Revisit assertions on private implementation details |

**Conditional recommendation:** OpenAI is the stronger starting point when strict numeric-address validation and protection of existing invariants are the priority. Claude contributes clearer internal hashing separation and a compact network representation. Before selecting or combining them, settle the constructor, parsing, extension, and representation policies in the shared contract. No implementation changes or architectural decisions were made by this review.

## Verification provenance and reproduction

The report records checks executed on the exact commits listed above. Generated logs and custom harness files remain local review artifacts and are not part of this documentation-only commit. The original branch test sources and Maven configuration are accessible through the commit-pinned source links. The full set of custom assertions is not reproducible from this document alone; use the summarized behavior matrix when constructing an equivalent harness.

To reproduce the original Maven suites and Javadoc checks, create separate checkouts at each recorded commit and run from each checkout's `sdk-java/` directory:

```sh
mvn -o -B test
mvn -o -B javadoc:javadoc
```

Offline mode requires the dependencies to be cached. The original review ran on OpenJDK 25.0.1 with production compilation targeting Java 21.

The complete module diff can be regenerated from a repository containing both commits:

```sh
git diff d3fd977fea6786fefc7234b6bdcf41c59e705c3a \
  1da79859020daadc922d7ff92f92855f33ca7b69 \
  -- sdk-java/hiero-sdk-base-api
```

### Representative compatibility consumers

Compile the following consumer separately against each built API module. It compiles with Claude, fails compilation with OpenAI, and throws `NoSuchMethodError` if the Claude-compiled class is run with OpenAI:

```java
package consumer;

import org.hiero.sdk.v3.ledger.Address;

public final class Main {
    private Main() {}

    public static void main(final String[] args) {
        System.out.println(new Address(1, 2, 3));
    }
}
```

This second consumer also compiles with Claude and fails compilation with OpenAI. Loading the Claude-compiled class with OpenAI throws `IncompatibleClassChangeError` because the overridden method is final:

```java
package consumer;

import org.hiero.sdk.v3.ledger.BaseAddress;
import org.hiero.sdk.v3.ledger.Network;

public final class Main extends BaseAddress {
    private Main() {
        super(1, 2, 3L, "");
    }

    @Override
    public boolean validateChecksum(final Network<?> network) {
        return false;
    }

    @Override
    public String toString() {
        return "1.2.3";
    }

    @Override
    public String toStringWithChecksum() {
        return "custom";
    }

    public static void main(final String[] args) {
        System.out.println(new Main().toStringWithChecksum());
    }
}
```

Both can use this module descriptor:

```java
module compat.consumer {
    requires org.hiero.sdk.v3.base;
}
```

The compatibility checks used `javac --release 21 -Xlint:all -Werror`, the built API and JSpecify on the compilation module path, and only the built API and consumer on the runtime module path.
