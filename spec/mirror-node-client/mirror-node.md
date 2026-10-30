# Mirror Node Query API

## Description

Query historical and current ledger data from a Hiero Mirror Node via its REST API.

Create a `MirrorNodeClient` for a `MirrorNode` with `createMirrorNodeClient`. The client groups its queries into
repositories, one per domain: `accounts`, `blocks`, `contracts`, `network`, `nft`, `schedules`, `token`, `topic` and
`transaction`. Lookups by id return the entity or nothing if it does not exist; list queries return a `Page` that can
be used to navigate through the full result. All queries are asynchronous and fail with a mirror node error if the Mirror Node cannot be
reached or returns an unexpected response.

Every list query exists twice: with the identifying arguments only (e.g. the account whose rewards are listed), and
with all filters and options of the Mirror Node as well. In the second form:

- A filter that compares is a list of conditions in the syntax of the Mirror Node REST API: a value with an optional
  operator prefix `eq:`, `ne:`, `gt:`, `gte:`, `lt:` or `lte:` (e.g. `["gte:1700000000", "lt:1700086400"]` for a
  timestamp, `["gt:0.0.1000"]` for an account). An empty list leaves the filter out. The conditions are not checked
  by the SDK; the Mirror Node rejects an invalid one, and the query fails with a mirror node error.
- Every other filter and option has its own type and is left out when it is `null`: `limit` is the number of items
  of one page (the Mirror Node's default when it is left out), `order` the order of the items.

For endpoints that have no typed repository method, `mirrorNodeHttpClient` gives direct access to the Mirror Node REST
API.

## Design Notes

- **The repositories cover the REST API completely.** Every endpoint, query parameter and response field of the
  pinned OpenAPI definition (`tooling/openapi/mirror-node/openapi.yml`, see `tooling/openapi/README.md`) has a
  counterpart in the specs of the mirror node client. Names follow the naming conventions (`node_account_id` →
  `accountId`, `account.publickey` → `accountPublicKey`); every other deviation - an element left out, merged or typed
  differently - is listed in the `## Design Notes` of the spec that declares the element.
- **Query parameters, first step.** A list endpoint has two overloads: one with its path parameters only and one with
  every query parameter in the order of the OpenAPI definition. A parameter that accepts the operators
  `eq`/`ne`/`gt`/`gte`/`lt`/`lte` (and may be repeated) is a `list<string>` in REST syntax - a collection must not be
  `@@nullable`, so an empty list means "not sent". Every other parameter is `@@nullable` with its own type and the
  constraints of the OpenAPI definition (`@@min(1) @@max(100) limit`). The string conditions trade type safety for
  coverage; a typed condition model is the next step once everything is covered.

## API Schema

```
namespace mirrornode

requires {MirrorNode} from ledger
requires {MirrorNodeHttpClient} from mirrornode.http
requires {AccountRepository} from mirrornode.account
requires {BlockRepository} from mirrornode.block
requires {ContractRepository} from mirrornode.contract
requires {NetworkRepository} from mirrornode.network
requires {NftRepository} from mirrornode.nft
requires {ScheduleRepository} from mirrornode.schedule
requires {TokenRepository} from mirrornode.token
requires {TopicRepository} from mirrornode.topic
requires {TransactionRepository} from mirrornode.transaction

MirrorNodeClient {
    @@immutable accounts: AccountRepository
    @@immutable blocks: BlockRepository
    @@immutable contracts: ContractRepository
    @@immutable network: NetworkRepository
    @@immutable nft: NftRepository
    @@immutable schedules: ScheduleRepository
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

- **Attributes that are not nullable here are `null` in REST responses.** With the complete coverage of the REST API
  most such attributes became `@@nullable` (`Contract`, `Topic`, token info, `NetworkNode`). `AccountInfo` still
  declares `balance`, `ethereumNonce`, `pendingReward` and `deleted` as required, and the Java implementation
  substitutes `0` / `false` when the Mirror Node returns `null` (see the Design Notes of
  [`mirror-node-account.md`](mirror-node-account.md)), so a caller cannot tell a real value from a missing one.
  Should these become `@@nullable` as well?

- **The HTTP client behind a repository is never closed.** `createRepository(mirrorNode)` of each namespace and
  `createMirrorNodeClient(mirrorNode)` take only a `MirrorNode` and create the HTTP client themselves, but neither
  the repositories nor `MirrorNodeClient` have a `close()`. The Java implementation therefore never closes these
  clients. Should the factories take an existing client (cf. the open item in
  [`mirror-node-http.md`](mirror-node-http.md)), or should the repositories and the client become closeable?
