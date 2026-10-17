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

## Testing

```tests
// Source: the Go SDK (TestUnitClientMirrorRestClientResolvesBaseURL) and the Java SDK (ClientMirrorBaseUrlTest): the
// REST client of a client talks to the configured mirror node. Creating the client sends no request.
@@testId("a-mirror-node-client-talks-to-the-mirror-node-it-was-created-for")
test "a mirror node client talks to the mirror node it was created for" {
    // given
    let mirrorNode = MirrorNode{restBaseUrl: "https://mainnet.mirrornode.hedera.com/api/v1"}

    // when
    let client = createMirrorNodeClient(mirrorNode: mirrorNode)

    // then
    assertEquals(expected: mirrorNode, actual: client.mirrorNodeHttpClient.mirrorNode)
}
```

## Questions & Comments

- **Attributes that are not nullable here are `null` in REST responses.** The Mirror Node returns `null` for several
  attributes that the specs declare as required, e.g. `permanent_removal`, `deleted`, `auto_renew_period` and
  `created_timestamp` (`Contract`, `Topic`, token info), `decline_reward` and `deleted` (`AccountInfo`, which declares
  `@@default(false)`), `stake` (`NetworkNode`) and `decimals` (tokens). The Java implementation substitutes fallback values (e.g. `false`, `0` or a sentinel time), so a caller
  cannot tell a real value from a missing one. Should each of these attributes be `@@nullable`, or should the specs
  state the fallback value?

- **The HTTP client behind a repository is never closed.** `createRepository(mirrorNode)` of each namespace and
  `createMirrorNodeClient(mirrorNode)` take only a `MirrorNode` and create the HTTP client themselves, but neither
  the repositories nor `MirrorNodeClient` have a `close()`. The Java implementation therefore never closes these
  clients. Should the factories take an existing client (cf. the open item in
  [`mirror-node-http.md`](mirror-node-http.md)), or should the repositories and the client become closeable?
