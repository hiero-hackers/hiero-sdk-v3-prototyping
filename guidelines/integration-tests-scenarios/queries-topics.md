# Queries / Topics

## Scenarios

### `queries.topics/can-query-topic-info`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to query topic info.
 - **Then** the operation completes without error and the returned information topic memo equals the expected text.

### `queries.topics/get-cost-query-topic-info`

> **Conformance:** Deferred — Unavailable in the Solo environment

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to get cost for topic info query.
 - **Then** the operation completes without error, the cost is present, and the returned information topic memo equals the expected text.

### `queries.topics/get-cost-big-max-query-topic-info`

> **Conformance:** Deferred — Unavailable in the Solo environment

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to get cost for topic info query.
 - **Then** the operation completes without error, the cost is present, and the returned information topic memo equals the expected text.

### `queries.topics/get-cost-small-max-query-topic-info`

> **Conformance:** Deferred — Unavailable in the Solo environment

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to get cost for topic info query.
 - **Then** the request is rejected with the expected error.

### `queries.topics/get-cost-insufficient-tx-fee-query-topic-info`

> **Conformance:** Deferred — Unavailable in the Solo environment

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to get cost for topic info query.
 - **Then** the request is rejected with the expected error.

### `queries.topics/can-receive-a-topic-message`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to receive a topic message.
 - **Then** the operation completes without error, the returned information topic identifier equals the expected topic identifier, the returned information topic memo equals the expected text, and the returned information sequence number equals 0.

### `queries.topics/can-receive-a-large-topic-message`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to receive a large topic message.
 - **Then** the operation completes without error, the returned information topic identifier equals the expected topic identifier, the returned information topic memo equals the expected text, and the returned information sequence number equals 0.

### `queries.topics/unsubscribing-does-not-log-retry-warnings`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “unsubscribing does not log retry warnings” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.
