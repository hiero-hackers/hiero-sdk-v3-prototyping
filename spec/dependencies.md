# Module / Namespace Dependencies

This document gives an overview of how the V3 API modules (namespaces) depend on each other. The graphs are derived
from the `requires {Type} from <namespace>` declarations in the spec files — an edge `A --> B` means "namespace `A`
imports one or more types from namespace `B`".

The namespaces group into the seven `spec/` folders / layers:

- **base** — `common`, `grpc`, `http`, `proto`, `evm`, `nativeToken`, `keys`, `ledger`, `ledger.config`, `authority`,
  `token`, `hedera`, `solo`
- **consensus-node-client** — `consensusnode.client`, `consensusnode.status`,
  `consensusnode.transactions[.accounts|.batch|.contracts|.files|.schedule|.spi|.tokens|.topics|.utility]`,
  `consensusnode.queries[.accounts|.files|.receipts|.schedule|.tokens|.topics]`, `consensusnode.proto[.account]`
- **consensus-node-admin-client** — `consensusnode.admin.{freeze|system|nodes|network}`
- **consensus-node-clpr** — `consensusnode.clpr[.transactions|.queries]` (Cross Ledger Protocol, HIP-1535)
- **block-node-client** — `blocknode`, `blocknode.status`
- **mirror-node-client** — `mirrornode` and its sub-namespaces (`account`, `block`, `common`, `contract`, `http`,
  `network`, `nft`, `schedule`, `token`, `topic`, `transaction`)
- **enterprise** — `enterprise.service[.account|.contract|.file|.token|.nft|.topic]`

## Layer overview

```mermaid
flowchart LR
    enterprise["enterprise<br/>(high-level service layer)"]
    consensus["consensus-node-client"]
    admin["consensus-node-admin-client<br/>(privileged council ops)"]
    clpr["consensus-node-clpr<br/>(Cross Ledger Protocol)"]
    mirror["mirror-node-client"]
    block["block-node-client"]
    base["base<br/>(common, ledger, keys, authority, nativeToken, evm, …)"]

    enterprise --> consensus
    enterprise --> mirror
    enterprise --> base
    consensus --> base
    admin --> consensus
    admin --> base
    clpr --> consensus
    clpr --> base
    mirror --> base
    block --> base
```

`mirror-node-client`, `block-node-client` and `consensus-node-client` are independent of each other;
`consensus-node-admin-client` is opt-in on top of `consensus-node-client` (privileged
freeze / system-delete / DAB-node transactions that normal applications do not consume);
`consensus-node-clpr` is opt-in on top of `consensus-node-client` in the same way (the CLPR service is disabled on a
ledger by default);
`enterprise` builds on top of `consensus-node-client`, `mirror-node-client`, and `base`.
Everything ultimately rests on `base`.

## Namespace dependency graphs

One graph per layer; `consensus-node-client` is split into client & transactions and queries. Each graph shows its
namespaces and every edge that starts in them; other namespaces appear only as far as they are imported, in their own
box and the color of their layer. Together the eight graphs contain every edge exactly once. `∅` marks namespaces that are still empty stubs (no types defined yet).

### base

```mermaid
flowchart LR
    subgraph base["base"]
        common["common"]
        grpc["grpc"]
        http["http"]
        proto["proto ∅"]
        evm["evm"]
        nativeToken["nativeToken"]
        keys["keys"]
        ledger["ledger"]
        ledger_config["ledger.config"]
        authority["authority"]
        token["token"]
        hedera["hedera"]
        solo["solo"]
    end

    authority --> keys
    authority --> ledger
    hedera --> ledger_config
    hedera --> nativeToken
    ledger --> evm
    ledger --> nativeToken
    ledger_config --> ledger
    solo --> ledger_config

    classDef base fill:#e8f0fe,stroke:#4285f4,color:#000;

    class common,grpc,http,proto,evm,nativeToken,keys,ledger,ledger_config,authority,token,hedera,solo base;
```

### consensus-node-client: client & transactions

```mermaid
flowchart LR
    subgraph consensus["consensus-node-client: client & transactions"]
        cn_client["consensusnode.client"]
        cn_status["consensusnode.status"]
        cn_tx["consensusnode.transactions"]
        cn_tx_accounts["…transactions.accounts"]
        cn_tx_batch["…transactions.batch"]
        cn_tx_contracts["…transactions.contracts"]
        cn_tx_files["…transactions.files"]
        cn_tx_schedule["…transactions.schedule"]
        cn_tx_spi["…transactions.spi"]
        cn_tx_tokens["…transactions.tokens"]
        cn_tx_topics["…transactions.topics"]
        cn_tx_utility["…transactions.utility"]
        cn_proto["consensusnode.proto"]
        cn_proto_account["…proto.account ∅"]
    end
    subgraph ext_base["base"]
        grpc["grpc"]
        evm["evm"]
        nativeToken["nativeToken"]
        keys["keys"]
        ledger["ledger"]
        ledger_config["ledger.config"]
        authority["authority"]
        token["token"]
    end

    cn_client --> cn_status
    cn_client --> keys
    cn_client --> ledger
    cn_client --> ledger_config
    cn_client --> nativeToken
    cn_tx --> authority
    cn_tx --> cn_client
    cn_tx --> cn_status
    cn_tx --> evm
    cn_tx --> ledger
    cn_tx --> nativeToken
    cn_tx_accounts --> authority
    cn_tx_accounts --> cn_tx
    cn_tx_accounts --> ledger
    cn_tx_accounts --> nativeToken
    cn_tx_batch --> cn_tx
    cn_tx_contracts --> authority
    cn_tx_contracts --> cn_tx
    cn_tx_contracts --> ledger
    cn_tx_contracts --> nativeToken
    cn_tx_files --> authority
    cn_tx_files --> cn_tx
    cn_tx_files --> ledger
    cn_tx_schedule --> authority
    cn_tx_schedule --> cn_tx
    cn_tx_schedule --> ledger
    cn_tx_spi --> cn_proto
    cn_tx_spi --> cn_status
    cn_tx_spi --> cn_tx
    cn_tx_spi --> grpc
    cn_tx_spi --> ledger
    cn_tx_spi --> nativeToken
    cn_tx_tokens --> authority
    cn_tx_tokens --> cn_tx
    cn_tx_tokens --> cn_tx_accounts
    cn_tx_tokens --> ledger
    cn_tx_tokens --> token
    cn_tx_topics --> authority
    cn_tx_topics --> cn_tx
    cn_tx_topics --> ledger
    cn_tx_utility --> cn_tx

    classDef consensus fill:#e6f4ea,stroke:#34a853,color:#000;
    classDef base fill:#e8f0fe,stroke:#4285f4,color:#000;

    class cn_client,cn_status,cn_tx,cn_tx_accounts,cn_tx_batch,cn_tx_contracts,cn_tx_files,cn_tx_schedule,cn_tx_spi,cn_tx_tokens,cn_tx_topics,cn_tx_utility,cn_proto,cn_proto_account consensus;
    class keys,ledger,ledger_config,nativeToken,authority,evm,grpc,token base;
```

### consensus-node-client: queries

```mermaid
flowchart LR
    subgraph consensus["consensus-node-client: queries"]
        cn_queries["consensusnode.queries"]
        cn_queries_accounts["…queries.accounts"]
        cn_queries_files["…queries.files"]
        cn_queries_receipts["…queries.receipts"]
        cn_queries_schedule["…queries.schedule"]
        cn_queries_tokens["…queries.tokens"]
        cn_queries_topics["…queries.topics"]
    end
    subgraph ext_base["base"]
        evm["evm"]
        nativeToken["nativeToken"]
        keys["keys"]
        ledger["ledger"]
        authority["authority"]
        token["token"]
    end
    subgraph ext_consensus["consensus-node-client: client & transactions"]
        cn_client["consensusnode.client"]
        cn_tx["consensusnode.transactions"]
    end

    cn_queries --> cn_client
    cn_queries --> ledger
    cn_queries --> nativeToken
    cn_queries_accounts --> authority
    cn_queries_accounts --> cn_queries
    cn_queries_accounts --> cn_tx
    cn_queries_accounts --> evm
    cn_queries_accounts --> ledger
    cn_queries_accounts --> nativeToken
    cn_queries_files --> authority
    cn_queries_files --> cn_queries
    cn_queries_files --> ledger
    cn_queries_receipts --> cn_queries
    cn_queries_receipts --> cn_tx
    cn_queries_receipts --> ledger
    cn_queries_schedule --> authority
    cn_queries_schedule --> cn_queries
    cn_queries_schedule --> cn_tx
    cn_queries_schedule --> keys
    cn_queries_schedule --> ledger
    cn_queries_tokens --> authority
    cn_queries_tokens --> cn_queries
    cn_queries_tokens --> ledger
    cn_queries_tokens --> token
    cn_queries_topics --> authority
    cn_queries_topics --> cn_queries
    cn_queries_topics --> ledger

    classDef consensus fill:#e6f4ea,stroke:#34a853,color:#000;
    classDef base fill:#e8f0fe,stroke:#4285f4,color:#000;

    class cn_queries,cn_queries_accounts,cn_queries_files,cn_queries_receipts,cn_queries_schedule,cn_queries_tokens,cn_queries_topics,cn_client,cn_tx consensus;
    class ledger,nativeToken,authority,evm,keys,token base;
```

### consensus-node-admin-client

```mermaid
flowchart LR
    subgraph admin["consensus-node-admin-client"]
        cn_admin_freeze["consensusnode.admin.freeze"]
        cn_admin_system["consensusnode.admin.system"]
        cn_admin_nodes["consensusnode.admin.nodes"]
        cn_admin_network["consensusnode.admin.network"]
    end
    subgraph ext_base["base"]
        ledger["ledger"]
        authority["authority"]
    end
    subgraph ext_consensus["consensus-node-client"]
        cn_tx["consensusnode.transactions"]
        cn_queries["consensusnode.queries"]
    end

    cn_admin_freeze --> cn_tx
    cn_admin_freeze --> ledger
    cn_admin_network --> cn_admin_nodes
    cn_admin_network --> cn_queries
    cn_admin_network --> ledger
    cn_admin_nodes --> authority
    cn_admin_nodes --> cn_tx
    cn_admin_nodes --> ledger
    cn_admin_system --> cn_tx
    cn_admin_system --> ledger

    classDef admin fill:#d2e3fc,stroke:#1967d2,color:#000;
    classDef consensus fill:#e6f4ea,stroke:#34a853,color:#000;
    classDef base fill:#e8f0fe,stroke:#4285f4,color:#000;

    class cn_admin_freeze,cn_admin_system,cn_admin_nodes,cn_admin_network admin;
    class cn_tx,cn_queries consensus;
    class ledger,authority base;
```

### consensus-node-clpr

```mermaid
flowchart LR
    subgraph clpr["consensus-node-clpr"]
        clpr_base["consensusnode.clpr"]
        clpr_tx["consensusnode.clpr.transactions"]
        clpr_queries["consensusnode.clpr.queries"]
    end
    subgraph ext_base["base"]
        ledger["ledger"]
        keys["keys"]
        authority["authority"]
        nativeToken["nativeToken"]
    end
    subgraph ext_consensus["consensus-node-client"]
        cn_status["consensusnode.status"]
        cn_tx["consensusnode.transactions"]
        cn_queries["consensusnode.queries"]
    end

    clpr_base --> cn_status
    clpr_tx --> authority
    clpr_tx --> clpr_base
    clpr_tx --> cn_tx
    clpr_tx --> keys
    clpr_tx --> ledger
    clpr_tx --> nativeToken
    clpr_queries --> clpr_base
    clpr_queries --> cn_queries

    classDef clpr fill:#f3e8fd,stroke:#9334e6,color:#000;
    classDef consensus fill:#e6f4ea,stroke:#34a853,color:#000;
    classDef base fill:#e8f0fe,stroke:#4285f4,color:#000;

    class clpr_base,clpr_tx,clpr_queries clpr;
    class cn_status,cn_tx,cn_queries consensus;
    class ledger,keys,authority,nativeToken base;
```

### block-node-client

```mermaid
flowchart LR
    subgraph block["block-node-client"]
        bn["blocknode"]
        bn_status["blocknode.status"]
    end
    subgraph ext_base["base"]
        ledger["ledger"]
    end

    bn --> bn_status
    bn --> ledger

    classDef block fill:#e0f7fa,stroke:#00838f,color:#000;
    classDef base fill:#e8f0fe,stroke:#4285f4,color:#000;

    class bn,bn_status block;
    class ledger base;
```

### mirror-node-client

```mermaid
flowchart LR
    subgraph mirror["mirror-node-client"]
        mn["mirrornode"]
        mn_account["mirrornode.account"]
        mn_block["mirrornode.block"]
        mn_common["mirrornode.common"]
        mn_contract["mirrornode.contract"]
        mn_http["mirrornode.http"]
        mn_network["mirrornode.network"]
        mn_nft["mirrornode.nft"]
        mn_schedule["mirrornode.schedule"]
        mn_token["mirrornode.token"]
        mn_topic["mirrornode.topic"]
        mn_transaction["mirrornode.transaction"]
    end
    subgraph ext_base["base"]
        common["common"]
        http["http"]
        evm["evm"]
        ledger["ledger"]
        authority["authority"]
        token["token"]
    end

    mn --> ledger
    mn --> mn_account
    mn --> mn_block
    mn --> mn_contract
    mn --> mn_http
    mn --> mn_network
    mn --> mn_nft
    mn --> mn_schedule
    mn --> mn_token
    mn --> mn_topic
    mn --> mn_transaction
    mn_account --> authority
    mn_account --> common
    mn_account --> evm
    mn_account --> ledger
    mn_account --> mn_common
    mn_account --> mn_transaction
    mn_block --> common
    mn_block --> ledger
    mn_block --> mn_common
    mn_common --> ledger
    mn_contract --> authority
    mn_contract --> common
    mn_contract --> evm
    mn_contract --> ledger
    mn_contract --> mn_common
    mn_http --> http
    mn_http --> ledger
    mn_network --> authority
    mn_network --> common
    mn_network --> ledger
    mn_network --> mn_common
    mn_nft --> common
    mn_nft --> ledger
    mn_nft --> mn_common
    mn_nft --> mn_transaction
    mn_schedule --> authority
    mn_schedule --> common
    mn_schedule --> ledger
    mn_schedule --> mn_common
    mn_token --> authority
    mn_token --> common
    mn_token --> ledger
    mn_token --> mn_common
    mn_token --> token
    mn_topic --> authority
    mn_topic --> common
    mn_topic --> ledger
    mn_topic --> mn_common
    mn_transaction --> authority
    mn_transaction --> common
    mn_transaction --> ledger
    mn_transaction --> mn_common

    classDef mirror fill:#fef7e0,stroke:#f9ab00,color:#000;
    classDef base fill:#e8f0fe,stroke:#4285f4,color:#000;

    class mn,mn_account,mn_block,mn_common,mn_contract,mn_http,mn_network,mn_nft,mn_schedule,mn_token,mn_topic,mn_transaction mirror;
    class ledger,authority,common,evm,http,token base;
```

### enterprise

```mermaid
flowchart LR
    subgraph enterprise["enterprise"]
        ent_service["enterprise.service"]
        ent_account["…service.account"]
        ent_contract["…service.contract"]
        ent_file["…service.file"]
        ent_token["…service.token"]
        ent_nft["…service.nft"]
        ent_topic["…service.topic"]
    end
    subgraph ext_base["base"]
        common["common"]
        evm["evm"]
        nativeToken["nativeToken"]
        ledger["ledger"]
        ledger_config["ledger.config"]
        authority["authority"]
        token["token"]
    end
    subgraph ext_consensus["consensus-node-client"]
        cn_client["consensusnode.client"]
    end
    subgraph ext_mirror["mirror-node-client"]
        mn_nft["mirrornode.nft"]
        mn_token["mirrornode.token"]
    end

    ent_service --> cn_client
    ent_service --> ledger_config
    ent_account --> authority
    ent_account --> common
    ent_account --> ent_service
    ent_account --> ledger
    ent_account --> nativeToken
    ent_contract --> authority
    ent_contract --> common
    ent_contract --> ent_service
    ent_contract --> evm
    ent_contract --> ledger
    ent_contract --> nativeToken
    ent_file --> authority
    ent_file --> ent_service
    ent_file --> ledger
    ent_nft --> authority
    ent_nft --> common
    ent_nft --> ent_service
    ent_nft --> ledger
    ent_nft --> mn_nft
    ent_nft --> token
    ent_token --> authority
    ent_token --> common
    ent_token --> ent_service
    ent_token --> ledger
    ent_token --> mn_token
    ent_token --> token
    ent_topic --> authority
    ent_topic --> common
    ent_topic --> ent_service
    ent_topic --> ledger

    classDef enterprise fill:#fce8e6,stroke:#ea4335,color:#000;
    classDef consensus fill:#e6f4ea,stroke:#34a853,color:#000;
    classDef base fill:#e8f0fe,stroke:#4285f4,color:#000;
    classDef mirror fill:#fef7e0,stroke:#f9ab00,color:#000;

    class ent_service,ent_account,ent_contract,ent_file,ent_token,ent_nft,ent_topic enterprise;
    class cn_client consensus;
    class ledger_config,authority,common,ledger,nativeToken,evm,token base;
    class mn_nft,mn_token mirror;
```

## Observations

- **Foundation:** `ledger` (42 incoming edges) and `authority` (27) are by far the most depended-upon namespaces —
  every layer imports `ledger`, and every namespace that carries an admin/submit/supply/... key imports `Authority`.
  `keys` itself is imported only by `authority`, `consensusnode.client` (signing),
  `consensusnode.queries.schedule` and `consensusnode.clpr.transactions` (ownership proofs); everything else reaches
  keys through `Authority`. `common` (the `Page<$$T>`
  type) is required by most mirror-node namespaces and by every enterprise service that exposes paginated queries
  (`account`, `topic`, `nft`, `token`, `contract`).
- **Base-internal leaves:** `evm`, `nativeToken`, `keys`, `common`, `token`, `grpc` and `http` import nothing.
  `evm` is deliberately dependency-free (ADR-0015); `ledger` imports it for `EvmAddress`.
- **No cycles:** the dependency graph is acyclic. (The earlier `keys ↔ keys.io` cycle was removed by merging the
  `keys.io` sub-namespace into `keys`.)
- **Clean layer separation:** `mirror-node-client` has no dependency on `consensus-node-client` (or vice versa),
  `block-node-client` depends on neither, and `base` depends on nothing outside `base`. `enterprise` is the only layer that combines `consensus-node-client`,
  `mirror-node-client`, and `base` — the mirror-node edges (`…service.nft --> mirrornode.nft`,
  `…service.token --> mirrornode.token`) let the service layer reuse the domain types (`Nft`, `NftMetadata`, `Token`,
  `TokenInfo`, `Balance`) for its query methods instead of redeclaring them. (`enterprise.service.contract` keeps a
  thin local `Contract` type and therefore does not depend on `mirrornode.contract`; it imports the ABI types from
  `evm` directly.)
- **`consensusnode.transactions` is the hub of the client:** every `…transactions.*` sub-namespace, the admin
  and CLPR transactions and the queries that return receipts, records or scheduled transactions (`…queries.accounts`,
  `…queries.receipts`, `…queries.schedule`) depend on it. The only edge between two sibling sub-namespaces is
  `…transactions.tokens --> …transactions.accounts` (token airdrops reuse the transfer types).
- **Status codes:** `consensusnode.status` is imported by `consensusnode.client`, `consensusnode.transactions`,
  `…transactions.spi` and `consensusnode.clpr`, which adds the status codes of the CLPR service to the open
  `TransactionStatus`.
- **Admin layer is opt-in and one-way:** `consensus-node-admin-client` depends on `consensus-node-client` (it
  reuses `Transaction<$$Receipt, $$Self>`, `Receipt`, and the signing / packing lifecycle) but is not depended on by it —
  the edge is strictly `admin --> consensus-node-client`. Applications that never call freeze / system-delete / DAB
  node operations do not pull the admin module in. `enterprise` does not depend on the admin module either; surfacing
  these privileged operations through the high-level service layer is intentionally out of scope.
- **CLPR is opt-in and one-way as well:** `consensus-node-clpr` depends on `consensus-node-client` (transactions,
  queries, status) and on `base`, and nothing depends on it. `consensusnode.clpr` holds what its transactions and
  queries share (`ClprThrottles`, `ClprTransactionStatus`). The mirror node queries of HIP-1535 are not specified yet;
  they would go to `mirror-node-client`, not to this module.
- **The block node client is the smallest layer:** `blocknode` imports only `BlockNode` from `ledger` (declared there
  beside `ConsensusNode` and `MirrorNode`) and the status types of `blocknode.status`, which imports nothing. Blocks are
  returned as their protobuf encoding, so the layer has no dependency on the consensus node's block stream types.
- **Session as the shared root of `enterprise`:** `enterprise.service` defines the `Session` (network settings,
  operator account, transaction signer, read-your-writes high-water-mark) that every concrete service
  (`…service.account`, `…service.contract`, `…service.file`, `…service.token`, `…service.nft`, `…service.topic`)
  depends on. As a result the concrete services no longer import `ledger.config` or `consensusnode.client`
  directly — both reach them through `enterprise.service`.
- **Transport namespaces have one consumer each:** `grpc` is used only by `consensusnode.transactions.spi`, `http`
  only by `mirrornode.http`.
- **Empty stubs (no edges):** `proto` and `consensusnode.proto.account` define no types yet. `consensusnode.proto`
  (the four protobuf messages of the SPI, ADR-0009) is used only by `consensusnode.transactions.spi`.
- **Network settings are base-resident:** `hedera` (HBAR + Hedera network settings) and `solo` (the local Solo
  network) live in the base layer and depend on `ledger.config` (`hedera` also on `nativeToken`) — see the open
  question in [`native-token.md`](base/native-token.md) about whether Hedera-specific namespaces should move out of
  `base`.

> The graphs are generated from the `requires` declarations; regenerate after changing imports.
