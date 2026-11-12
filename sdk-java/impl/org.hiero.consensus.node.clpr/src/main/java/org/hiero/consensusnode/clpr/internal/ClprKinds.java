package org.hiero.consensusnode.clpr.internal;

import com.google.protobuf.ByteString;
import com.hederahashgraph.api.proto.java.ClprCloseChannelTransactionBody;
import com.hederahashgraph.api.proto.java.ClprCompleteChannelTransactionBody;
import com.hederahashgraph.api.proto.java.ClprCompleteConnectorTransactionBody;
import com.hederahashgraph.api.proto.java.ClprDeregisterConnectorTransactionBody;
import com.hederahashgraph.api.proto.java.ClprLedgerConfiguration;
import com.hederahashgraph.api.proto.java.ClprRegisterChannelTransactionBody;
import com.hederahashgraph.api.proto.java.ClprRegisterConnectorTransactionBody;
import com.hederahashgraph.api.proto.java.ClprSubmitBundleTransactionBody;
import com.hederahashgraph.api.proto.java.ClprUpdateLedgerConfigurationTransactionBody;
import com.hederahashgraph.api.proto.java.TransactionBody;
import java.util.List;
import org.hiero.consensusnode.client.internal.Protobuf;
import org.hiero.consensusnode.client.internal.TransactionKind;
import org.hiero.consensusnode.clpr.ClprThrottles;
import org.hiero.consensusnode.clpr.transactions.ClprChannelOwnershipProof;
import org.hiero.consensusnode.clpr.transactions.ClprCloseChannelReceipt;
import org.hiero.consensusnode.clpr.transactions.ClprCloseChannelTransaction;
import org.hiero.consensusnode.clpr.transactions.ClprCompleteChannelReceipt;
import org.hiero.consensusnode.clpr.transactions.ClprCompleteChannelTransaction;
import org.hiero.consensusnode.clpr.transactions.ClprCompleteConnectorReceipt;
import org.hiero.consensusnode.clpr.transactions.ClprCompleteConnectorTransaction;
import org.hiero.consensusnode.clpr.transactions.ClprConnectorOwnershipProof;
import org.hiero.consensusnode.clpr.transactions.ClprDeregisterConnectorReceipt;
import org.hiero.consensusnode.clpr.transactions.ClprDeregisterConnectorTransaction;
import org.hiero.consensusnode.clpr.transactions.ClprRegisterChannelReceipt;
import org.hiero.consensusnode.clpr.transactions.ClprRegisterChannelTransaction;
import org.hiero.consensusnode.clpr.transactions.ClprRegisterConnectorReceipt;
import org.hiero.consensusnode.clpr.transactions.ClprRegisterConnectorTransaction;
import org.hiero.consensusnode.clpr.transactions.ClprSubmitBundleReceipt;
import org.hiero.consensusnode.clpr.transactions.ClprSubmitBundleTransaction;
import org.hiero.consensusnode.clpr.transactions.ClprUpdateLedgerConfigurationReceipt;
import org.hiero.consensusnode.clpr.transactions.ClprUpdateLedgerConfigurationTransaction;

/// The transaction types of the CLPR service (`proto.ClprService`).
public final class ClprKinds {

    private static final String SERVICE = "proto.ClprService";

    /// `ClprRegisterChannelTransaction`: `ClprService.registerChannel`.
    public static final TransactionKind<ClprRegisterChannelReceipt, ClprRegisterChannelTransaction> REGISTER_CHANNEL =
            new TransactionKind<>(ClprRegisterChannelTransaction.class, SERVICE, "registerChannel",
                    TransactionBody.DataCase.CLPRREGISTERCHANNEL,
                    (transaction, body) -> body.setClprRegisterChannel(ClprRegisterChannelTransactionBody.newBuilder()
                            .setOwnershipCommitment(bytes(transaction.ownershipCommitment()))),
                    (id, status, rate, nextRate, receipt) -> new ClprRegisterChannelReceipt(id, status, rate,
                            nextRate));

    /// `ClprCompleteChannelTransaction`: `ClprService.completeChannel`.
    public static final TransactionKind<ClprCompleteChannelReceipt, ClprCompleteChannelTransaction> COMPLETE_CHANNEL =
            new TransactionKind<>(ClprCompleteChannelTransaction.class, SERVICE, "completeChannel",
                    TransactionBody.DataCase.CLPRCOMPLETECHANNEL, ClprKinds::completeChannel,
                    (id, status, rate, nextRate, receipt) -> new ClprCompleteChannelReceipt(id, status, rate,
                            nextRate));

    /// `ClprCloseChannelTransaction`: `ClprService.closeChannel`.
    public static final TransactionKind<ClprCloseChannelReceipt, ClprCloseChannelTransaction> CLOSE_CHANNEL =
            new TransactionKind<>(ClprCloseChannelTransaction.class, SERVICE, "closeChannel",
                    TransactionBody.DataCase.CLPRCLOSECHANNEL, ClprKinds::closeChannel,
                    (id, status, rate, nextRate, receipt) -> new ClprCloseChannelReceipt(id, status, rate, nextRate));

    /// `ClprSubmitBundleTransaction`: `ClprService.submitBundle`.
    public static final TransactionKind<ClprSubmitBundleReceipt, ClprSubmitBundleTransaction> SUBMIT_BUNDLE =
            new TransactionKind<>(ClprSubmitBundleTransaction.class, SERVICE, "submitBundle",
                    TransactionBody.DataCase.CLPRSUBMITBUNDLE,
                    (transaction, body) -> body.setClprSubmitBundle(ClprSubmitBundleTransactionBody.newBuilder()
                            .setChannelId(bytes(transaction.channelId()))
                            .setBundlePayload(bytes(transaction.bundlePayload()))),
                    (id, status, rate, nextRate, receipt) -> new ClprSubmitBundleReceipt(id, status, rate, nextRate));

    /// `ClprRegisterConnectorTransaction`: `ClprService.registerConnector`.
    public static final TransactionKind<ClprRegisterConnectorReceipt, ClprRegisterConnectorTransaction>
            REGISTER_CONNECTOR = new TransactionKind<>(ClprRegisterConnectorTransaction.class, SERVICE,
                    "registerConnector", TransactionBody.DataCase.CLPRREGISTERCONNECTOR,
                    (transaction, body) -> body.setClprRegisterConnector(ClprRegisterConnectorTransactionBody
                            .newBuilder().setCommitment(bytes(transaction.commitment()))),
                    (id, status, rate, nextRate, receipt) -> new ClprRegisterConnectorReceipt(id, status, rate,
                            nextRate));

    /// `ClprCompleteConnectorTransaction`: `ClprService.completeConnector`.
    public static final TransactionKind<ClprCompleteConnectorReceipt, ClprCompleteConnectorTransaction>
            COMPLETE_CONNECTOR = new TransactionKind<>(ClprCompleteConnectorTransaction.class, SERVICE,
                    "completeConnector", TransactionBody.DataCase.CLPRCOMPLETECONNECTOR, ClprKinds::completeConnector,
                    (id, status, rate, nextRate, receipt) -> new ClprCompleteConnectorReceipt(id, status, rate,
                            nextRate));

    /// `ClprDeregisterConnectorTransaction`: `ClprService.deregisterConnector`.
    public static final TransactionKind<ClprDeregisterConnectorReceipt, ClprDeregisterConnectorTransaction>
            DEREGISTER_CONNECTOR = new TransactionKind<>(ClprDeregisterConnectorTransaction.class, SERVICE,
                    "deregisterConnector", TransactionBody.DataCase.CLPRDEREGISTERCONNECTOR,
                    (transaction, body) -> body.setClprDeregisterConnector(ClprDeregisterConnectorTransactionBody
                            .newBuilder()
                            .setChannelId(bytes(transaction.channelId()))
                            .setConnectorId(bytes(transaction.connectorId()))
                            .setStakeRecipient(Protobuf.toProto(transaction.stakeRecipient()))),
                    (id, status, rate, nextRate, receipt) -> new ClprDeregisterConnectorReceipt(id, status, rate,
                            nextRate));

    /// `ClprUpdateLedgerConfigurationTransaction`: `ClprService.updateLedgerConfiguration`. Only the throttles are
    /// sent; the network ignores the other fields of the configuration.
    public static final TransactionKind<ClprUpdateLedgerConfigurationReceipt, ClprUpdateLedgerConfigurationTransaction>
            UPDATE_LEDGER_CONFIGURATION = new TransactionKind<>(ClprUpdateLedgerConfigurationTransaction.class,
                    SERVICE, "updateLedgerConfiguration", TransactionBody.DataCase.CLPRUPDATELEDGERCONFIGURATION,
                    (transaction, body) -> body.setClprUpdateLedgerConfiguration(
                            ClprUpdateLedgerConfigurationTransactionBody.newBuilder().setConfiguration(
                                    ClprLedgerConfiguration.newBuilder()
                                            .setThrottles(throttles(transaction.throttles())))),
                    (id, status, rate, nextRate, receipt) -> new ClprUpdateLedgerConfigurationReceipt(id, status,
                            rate, nextRate));

    /// All transaction types of this class.
    public static final List<TransactionKind<?, ?>> ALL = List.of(REGISTER_CHANNEL, COMPLETE_CHANNEL, CLOSE_CHANNEL,
            SUBMIT_BUNDLE, REGISTER_CONNECTOR, COMPLETE_CONNECTOR, DEREGISTER_CONNECTOR, UPDATE_LEDGER_CONFIGURATION);

    private ClprKinds() {
    }

    /// The throttles as message.
    ///
    /// @param throttles the throttles
    /// @return the message
    public static com.hederahashgraph.api.proto.java.ClprThrottles throttles(final ClprThrottles throttles) {
        return com.hederahashgraph.api.proto.java.ClprThrottles.newBuilder()
                .setMaxMessagesPerBundle((int) throttles.maxMessagesPerBundle())
                .setMaxMessagePayloadBytes((int) throttles.maxMessagePayloadBytes())
                .setMaxGasPerMessage(throttles.maxGasPerMessage())
                .setMaxQueueDepth((int) throttles.maxQueueDepth())
                .setMaxSyncBytes(throttles.maxSyncBytes())
                .setMaxLocalEndpoints((int) throttles.maxLocalEndpoints())
                .setMaxPeerEndpoints((int) throttles.maxPeerEndpoints())
                .build();
    }

    private static void completeChannel(final ClprCompleteChannelTransaction transaction,
                                        final TransactionBody.Builder body) {
        final ClprChannelOwnershipProof proof = transaction.ownershipProof();
        body.setClprCompleteChannel(ClprCompleteChannelTransactionBody.newBuilder()
                .setChannelId(bytes(proof.channelId()))
                .setPublicKey(bytes(Ownership.encode(proof.publicKey())))
                .setSignature(bytes(proof.signature()))
                .setSignatureScheme(Ownership.scheme(proof.publicKey()))
                .setVerifierContract(Protobuf.toProto(transaction.verifierContract()))
                .setConfigProofBytes(bytes(transaction.configProof()))
                .setEndpointManifestProofBytes(bytes(transaction.endpointManifestProof())));
    }

    private static void closeChannel(final ClprCloseChannelTransaction transaction,
                                     final TransactionBody.Builder body) {
        final ClprCloseChannelTransactionBody.Builder close = ClprCloseChannelTransactionBody.newBuilder()
                .setChannelId(bytes(transaction.channelId()));
        final byte[] commitment = transaction.ownershipCommitment();
        if (commitment != null) {
            close.setOwnershipCommitment(bytes(commitment));
        }
        body.setClprCloseChannel(close);
    }

    private static void completeConnector(final ClprCompleteConnectorTransaction transaction,
                                          final TransactionBody.Builder body) {
        final ClprConnectorOwnershipProof proof = transaction.ownershipProof();
        body.setClprCompleteConnector(ClprCompleteConnectorTransactionBody.newBuilder()
                .setConnectorId(bytes(proof.connectorId()))
                .setPublicKey(bytes(Ownership.encode(proof.publicKey())))
                .setSignature(bytes(proof.signature()))
                .setSignatureScheme(Ownership.scheme(proof.publicKey()))
                .setSalt(bytes(proof.salt()))
                .setChannelId(bytes(proof.channelId()))
                .setConnectorContract(Protobuf.toProto(transaction.connectorContract()))
                .setAdminKey(Protobuf.toProto(transaction.adminAuthority()))
                .setLockedStake(transaction.lockedStake().toBaseUnits()));
    }

    private static ByteString bytes(final byte[] value) {
        return ByteString.copyFrom(value);
    }
}
