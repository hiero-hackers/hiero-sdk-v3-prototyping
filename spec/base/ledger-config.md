# Configuration API

This section defines the API for configuration.

## Description

The config API provides functions to define and retrieve the configuration of a specific network.

## API Schema

```
namespace ledger.config
requires {Network, ConsensusNode, MirrorNode} from ledger

// The full configuration to connect to a specific network
NetworkSetting {
 
    @@immutable network: Network<ANY>
   
    @@immutable  getConsensusNodes: set<ConsensusNode>

    @@immutable  getMirrorNodes: set<MirrorNode>

}

// factory methods of `NetworkSetting` that should be added to the namespace in the best language dependent way

// Registers a network setting under the given identifier.
@@static void registerNetworkSetting(identifier: string, setting: NetworkSetting)

// Returns the network setting registered for the given identifier; throws if none exists.
// Network settings can be added as plug and play by external modules
@@throws(not-found-error) @@static NetworkSetting getNetworkSetting(identifier: string) 
```

## Default Instances for Testing

```
// The configuration of the Hedera testnet.
instance NetworkSetting = NetworkSetting{network: DEFAULT, getConsensusNodes: [DEFAULT], getMirrorNodes: [DEFAULT]}
```

## Examples

The following example shows how to load the network configuration for the Hedera testnet:

```
NetworkSetting setting = NetworkSetting.getNetworkSetting(HEDERA_TESTNET_IDENTIFIER)
```

## Testing

Tests of the registry of network settings, written as given / when / then. The comment above each test names its
source. The registry is shared by the whole process, so a test registers its settings under an identifier of its own.

```tests
// Source: this spec (getNetworkSetting returns the setting registered for the identifier); the Java and Go SDKs (a
// client is created for the network of a name: Client.forName, ClientForName).
test "a registered network setting is returned for its identifier" {
    // given
    let setting = DEFAULT(NetworkSetting)
    registerNetworkSetting(identifier: "ledger-config-test-registered", setting: setting)

    // when
    let found = getNetworkSetting(identifier: "ledger-config-test-registered")

    // then
    assertEquals(expected: setting, actual: found)
}

// Source: this spec (getNetworkSetting throws if no setting exists for the identifier); the Java SDK (Client.forName
// rejects an unknown name).
test "an identifier without a registered network setting is rejected" {
    // when / then
    @@expectThrows(not-found-error)
    getNetworkSetting(identifier: "ledger-config-test-unknown")
}
```

## Questions & Comments

- **Registering an identifier twice.** The spec does not say what `registerNetworkSetting` does for an identifier that
  already has a setting: replace it, keep the first one, or fail. Network settings are added "plug and play" by
  external modules, so two modules could claim the same identifier. Should the second registration fail (with a
  declared error), and may the built-in identifiers (`HEDERA_MAINNET_IDENTIFIER`, `SOLO_IDENTIFIER`, ...) be replaced?
