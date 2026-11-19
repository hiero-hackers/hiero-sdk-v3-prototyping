/// Every transaction and query the consensus node handles has a request type, identified by its numeric code. The
/// network names request types where it reports something per type of request, for example the prices of its fee
/// schedule (`FeeScheduleQuery`). `BasicRequestType` names the request types of the built-in services of the consensus
/// node.
///
/// The set of request types is open. Every request type the SDK knows is registered as a `RequestTypeSupport`
/// (`consensusnode.transactions.spi`): the SDK registers the one of `BasicRequestType`, a module or a custom service its
/// own request types (the CLPR module, for example, `ClprRequestType`). `getRequestType(code)` returns the request type
/// of a code.
package org.hiero.consensusnode.requestTypes;
