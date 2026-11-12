package org.hiero.blocknode;


/// The kind of a block item. Open for kinds unknown to the SDK, so that the blocks of newer network releases stay
/// readable.
public interface BlockItemKind {

    /// the field number of the item in the block stream message `BlockItem`
    int code();
}
