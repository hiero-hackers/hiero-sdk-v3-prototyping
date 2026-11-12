import org.jspecify.annotations.NullMarked;

/// Module `org.hiero.consensus.node.clpr` of the Hiero SDK.
///
/// Packages:
/// - `org.hiero.consensusnode.clpr`
/// - `org.hiero.consensusnode.clpr.queries`
/// - `org.hiero.consensusnode.clpr.transactions`
@NullMarked
module org.hiero.consensus.node.clpr {
    requires transitive org.hiero.base;
    requires transitive org.hiero.consensus.node.client;
    requires static transitive org.jspecify;
    requires static transitive java.compiler;
    // Keccak-256 and the secp256k1 point encoding of the ownership proofs (org.hiero.consensusnode.clpr.internal)
    requires org.bouncycastle.provider;

    exports org.hiero.consensusnode.clpr;
    exports org.hiero.consensusnode.clpr.queries;
    exports org.hiero.consensusnode.clpr.transactions;

    provides org.hiero.consensusnode.transactions.spi.TransactionSupport with
            org.hiero.consensusnode.clpr.internal.ClprSupports.RegisterChannel,
            org.hiero.consensusnode.clpr.internal.ClprSupports.CompleteChannel,
            org.hiero.consensusnode.clpr.internal.ClprSupports.CloseChannel,
            org.hiero.consensusnode.clpr.internal.ClprSupports.SubmitBundle,
            org.hiero.consensusnode.clpr.internal.ClprSupports.RegisterConnector,
            org.hiero.consensusnode.clpr.internal.ClprSupports.CompleteConnector,
            org.hiero.consensusnode.clpr.internal.ClprSupports.DeregisterConnector,
            org.hiero.consensusnode.clpr.internal.ClprSupports.UpdateLedgerConfiguration;
    provides org.hiero.consensusnode.transactions.spi.TransactionStatusSupport with
            org.hiero.consensusnode.clpr.internal.ClprSupports.Statuses;
}
