import org.jspecify.annotations.NullMarked;

/// Module `org.hiero.consensus.node.clpr` of the Hiero SDK.
///
/// Packages:
/// - `org.hiero.consensusnode.clpr`
/// - `org.hiero.consensusnode.clpr.queries`
/// - `org.hiero.consensusnode.clpr.transactions`
@NullMarked
// auto-service-annotations ships no module descriptor; com.google.auto.service is an automatic module
@SuppressWarnings("requires-automatic")
module org.hiero.consensus.node.clpr {
    requires transitive org.hiero.base;
    requires transitive org.hiero.consensus.node.client;
    requires static transitive org.jspecify;
    // @AutoService writes META-INF/services for the class path; the provides below serve the module path
    requires static com.google.auto.service;
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
    provides org.hiero.consensusnode.transactions.spi.RequestTypeSupport with
            org.hiero.consensusnode.clpr.internal.ClprSupports.RequestTypes;
}
