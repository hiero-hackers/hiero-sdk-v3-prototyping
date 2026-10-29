package org.hiero.consensusnode.status;


/// The status of a transaction, identified by a numeric code. The set of statuses is open: custom
/// services on the consensus node can define their own transaction types and status codes.
public interface TransactionStatus {

    /// the numeric status code reported by the consensus node
    int code();
}
