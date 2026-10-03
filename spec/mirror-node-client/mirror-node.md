# Mirror Node Query API

## Description

Query historical and current ledger data from a Hiero Mirror Node via its REST API.

Create a `MirrorNodeClient` for a `MirrorNode` with `createMirrorNodeClient`. The client groups its queries into
repositories, one per domain: `accounts`, `contracts`, `network`, `nft`, `token`, `topic` and `transaction`. Lookups by
id return the entity or nothing if it does not exist; list queries return a `Page` that can be used to navigate
through the full result. All queries are asynchronous and fail with a mirror node error if the Mirror Node cannot be
reached or returns an unexpected response.

For endpoints that have no typed repository method, `mirrorNodeHttpClient` gives direct access to the Mirror Node REST
API.

## API Schema

```
namespace mirrornode

requires {MirrorNode} from ledger
requires {MirrorNodeHttpClient} from mirrornode.http
requires {AccountRepository} from mirrornode.account
requires {ContractRepository} from mirrornode.contract
requires {NetworkRepository} from mirrornode.network
requires {NftRepository} from mirrornode.nft
requires {TokenRepository} from mirrornode.token
requires {TopicRepository} from mirrornode.topic
requires {TransactionRepository} from mirrornode.transaction

MirrorNodeClient {
    @@immutable accounts: AccountRepository
    @@immutable contracts: ContractRepository
    @@immutable network: NetworkRepository
    @@immutable nft: NftRepository
    @@immutable token: TokenRepository
    @@immutable topic: TopicRepository
    @@immutable transaction: TransactionRepository
    @@immutable mirrorNodeHttpClient: MirrorNodeHttpClient
}

@@static
@@throws(mirror-node-error)
MirrorNodeClient createMirrorNodeClient(mirrorNode: MirrorNode)
```

## Examples

```
mirrorNode = MirrorNode(restBaseUrl: "https://mainnet.mirrornode.hedera.com/api/v1")
client = createMirrorNodeClient(mirrorNode)

// Look up an contract
contract = await client.contracts.findById(ContractId.fromString("0.0.1234"))
```

## Questions & Comments
