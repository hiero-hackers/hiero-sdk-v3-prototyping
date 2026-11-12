package org.hiero.blocknode;


/// The kinds of block items of the block stream.
public enum BasicBlockItemKind implements BlockItemKind {

    /// starts a block: number, consensus time, versions, hash algorithm
    BLOCK_HEADER(1),
    /// starts an event of the consensus algorithm
    EVENT_HEADER(2),
    /// starts a round of the consensus algorithm
    ROUND_HEADER(3),
    /// a signed transaction as submitted to the network
    SIGNED_TRANSACTION(4),
    /// the result of a transaction
    TRANSACTION_RESULT(5),
    /// an output of a transaction that is specific to its type, e.g. of a contract call
    TRANSACTION_OUTPUT(6),
    /// changes of the network state
    STATE_CHANGES(7),
    /// stands in for an item that was removed from a filtered block
    FILTERED_SINGLE_ITEM(8),
    /// a proof of the block
    BLOCK_PROOF(9),
    /// a record file of the former record stream, wrapped into a block
    RECORD_FILE(10),
    /// informational trace data, e.g. of the EVM
    TRACE_DATA(11),
    /// ends the hashed content of a block
    BLOCK_FOOTER(12),
    /// stands in for an item whose content was redacted
    REDACTED_ITEM(19);

    private final int code;

    BasicBlockItemKind(final int code) {
        this.code = code;
    }

    /// Returns the `code` of this value.
    @Override
    public int code() {
        return code;
    }
}
