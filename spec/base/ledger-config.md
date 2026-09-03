# Configuration API

This section defines the API for configuration.

## Description

The config API provides functions to define and retrieve the configuration of a specific network. A
`NetworkSetting` is an immutable snapshot of the network and the consensus-node and mirror-node addresses needed to
connect to it. Retrieving a setting must not perform network access.

## API Schema

```
namespace ledger.config
requires {Network, ConsensusNode, MirrorNode} from ledger

// The full configuration to connect to a specific network
NetworkSetting {
 
    @@immutable network: Network<ANY>
   
    // Consensus-node address snapshot used to submit requests
    @@immutable getConsensusNodes: set<ConsensusNode>

    // Fixed mirror-node address snapshot used for mirror queries and consensus-node discovery
    @@immutable getMirrorNodes: set<MirrorNode>

}

// factory methods of `NetworkSetting` that should be added to the namespace in the best language dependent way

// Method to register a network configuration
@@static void registerNetworkSetting(identifier: string, setting: NetworkSetting)

// throws not-found-error if no network with that identifier exists
// Network settings can be added as plug and play by external modules
@@throws(not-found-error) @@static NetworkSetting getNetworkSetting(identifier: string) 
```

## Examples

The following example shows how to load the network configuration for the Hedera testnet:

```
NetworkSetting setting = NetworkSetting.getNetworkSetting(HEDERA_TESTNET_IDENTIFIER)
```

## Node Address Snapshots

Built-in public-network settings must contain enough bootstrap addresses to establish initial connectivity. Consensus
nodes come from an address-book snapshot packaged with the SDK release. Mirror nodes come from fixed public endpoints
maintained by the network. Custom settings use the addresses supplied when they are registered.

The returned collections are immutable snapshots. A caller that already holds a `NetworkSetting` must not observe later
registry or client updates through that instance. Registering a newer setting under an existing identifier affects
subsequent calls to `getNetworkSetting`, but it must not mutate settings that were previously returned.

Runtime consensus-address refresh is performed periodically by the underlying process defined in the
[client API](../consensus-node-client/client.md#node-address-management).

## Testing

### `ledger.config/node-address-snapshots-are-immutable`

 - **Given** a registered network setting.
 - **When** its consensus and mirror nodes are retrieved.
 - **Then** both results are immutable snapshots and attempts to modify caller-owned copies do not affect the setting.

### `ledger.config/re-registration-does-not-mutate-previous-setting`

 - **Given** a setting previously retrieved from an identifier.
 - **When** a newer setting is registered under the same identifier.
 - **Then** subsequent retrieval returns the newer snapshot and the previous setting remains unchanged.

## Questions & Comments
