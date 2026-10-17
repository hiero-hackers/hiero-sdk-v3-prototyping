import org.jspecify.annotations.NullMarked;

/// Module `org.hiero.consensus.node.admin.client` of the Hiero SDK.
///
/// Packages:
/// - `org.hiero.consensusnode.admin.freeze`
/// - `org.hiero.consensusnode.admin.network`
/// - `org.hiero.consensusnode.admin.nodes`
/// - `org.hiero.consensusnode.admin.system`
@NullMarked
module org.hiero.consensus.node.admin.client {
    requires transitive org.hiero.base;
    requires transitive org.hiero.consensus.node.client;
    requires static transitive org.jspecify;

    exports org.hiero.consensusnode.admin.freeze;
    exports org.hiero.consensusnode.admin.network;
    exports org.hiero.consensusnode.admin.nodes;
    exports org.hiero.consensusnode.admin.system;

    provides org.hiero.consensusnode.transactions.spi.TransactionSupport with
            org.hiero.consensusnode.admin.internal.AdminSupports.Freeze,
            org.hiero.consensusnode.admin.internal.AdminSupports.NodeCreate,
            org.hiero.consensusnode.admin.internal.AdminSupports.NodeUpdate,
            org.hiero.consensusnode.admin.internal.AdminSupports.NodeDelete,
            org.hiero.consensusnode.admin.internal.AdminSupports.SystemDelete,
            org.hiero.consensusnode.admin.internal.AdminSupports.SystemUndelete;
}
