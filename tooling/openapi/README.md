# OpenAPI definitions

The OpenAPI definitions of the node types that are spoken over REST, vendored at pinned versions from their upstream
repositories - the counterpart of [`tooling/protobuf`](../protobuf/README.md) for the REST APIs. Today this is the
REST API of the mirror node (`mirror-node/openapi.yml`), which the specs in
[`spec/mirror-node-client`](../../spec/mirror-node-client) model.

The specs cover the pinned version completely: every endpoint, every query parameter and every field of a response.
Where a spec deliberately deviates - an element left out (e.g. a deprecated field), merged or typed differently - its
`## Design Notes` say so. A renaming by the naming conventions (`node_account_id` → `accountId`, `account.id` →
`accountId`) is no deviation.

| File | Content |
|---|---|
| `sources.json` | repository, tag and path per source; `commit` and `updated` are written by `update.sh` |
| `update.sh` | re-fetches the pinned files (`update.sh [mirror-node ...]`) |
| `mirror-node/openapi.yml` | the REST API of the mirror node (`/api/v1`) |

Never edit a vendored file by hand: change the tag in `sources.json` and run `update.sh`. The mirror node's REST API
is pinned to the same release as its gRPC API in `tooling/protobuf/sources.json`.

## Open

- A check that compares the pinned OpenAPI definition with the specs (endpoints, query parameters, response fields)
  and reports every element that neither has a counterpart nor is listed as a deviation. Until then completeness is
  reviewed by hand.
