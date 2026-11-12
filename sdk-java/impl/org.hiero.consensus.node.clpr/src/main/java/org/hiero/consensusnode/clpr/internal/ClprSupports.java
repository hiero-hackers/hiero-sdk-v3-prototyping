package org.hiero.consensusnode.clpr.internal;

import java.util.Set;
import org.hiero.consensusnode.client.internal.KindSupport;
import org.hiero.consensusnode.clpr.ClprTransactionStatus;
import org.hiero.consensusnode.clpr.transactions.ClprCloseChannelReceipt;
import org.hiero.consensusnode.clpr.transactions.ClprCloseChannelTransaction;
import org.hiero.consensusnode.clpr.transactions.ClprCompleteChannelReceipt;
import org.hiero.consensusnode.clpr.transactions.ClprCompleteChannelTransaction;
import org.hiero.consensusnode.clpr.transactions.ClprCompleteConnectorReceipt;
import org.hiero.consensusnode.clpr.transactions.ClprCompleteConnectorTransaction;
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
import org.hiero.consensusnode.status.TransactionStatus;
import org.hiero.consensusnode.transactions.spi.TransactionStatusSupport;

/// The transaction types of the CLPR module, registered with the `ServiceLoader` as `TransactionSupport`, so that the
/// client module finds them (`PackedTransaction.fromBytes`, `SpiFactory`), and the CLPR status codes, registered as
/// `TransactionStatusSupport`.
public final class ClprSupports {

    private ClprSupports() {
    }

    /// The status codes of the CLPR service.
    public static final class Statuses implements TransactionStatusSupport {

        /// Creates the support (used by the service loader).
        public Statuses() {
            // nothing to set up
        }

        @Override
        public Set<TransactionStatus> getStatuses() {
            return Set.of(ClprTransactionStatus.values());
        }
    }

    /// The support of `ClprRegisterChannelTransaction`.
    public static final class RegisterChannel
            extends KindSupport<ClprRegisterChannelReceipt, ClprRegisterChannelTransaction> {

        /// Creates the support (used by the service loader).
        public RegisterChannel() {
            super(ClprKinds.REGISTER_CHANNEL);
        }
    }

    /// The support of `ClprCompleteChannelTransaction`.
    public static final class CompleteChannel
            extends KindSupport<ClprCompleteChannelReceipt, ClprCompleteChannelTransaction> {

        /// Creates the support (used by the service loader).
        public CompleteChannel() {
            super(ClprKinds.COMPLETE_CHANNEL);
        }
    }

    /// The support of `ClprCloseChannelTransaction`.
    public static final class CloseChannel extends KindSupport<ClprCloseChannelReceipt, ClprCloseChannelTransaction> {

        /// Creates the support (used by the service loader).
        public CloseChannel() {
            super(ClprKinds.CLOSE_CHANNEL);
        }
    }

    /// The support of `ClprSubmitBundleTransaction`.
    public static final class SubmitBundle extends KindSupport<ClprSubmitBundleReceipt, ClprSubmitBundleTransaction> {

        /// Creates the support (used by the service loader).
        public SubmitBundle() {
            super(ClprKinds.SUBMIT_BUNDLE);
        }
    }

    /// The support of `ClprRegisterConnectorTransaction`.
    public static final class RegisterConnector
            extends KindSupport<ClprRegisterConnectorReceipt, ClprRegisterConnectorTransaction> {

        /// Creates the support (used by the service loader).
        public RegisterConnector() {
            super(ClprKinds.REGISTER_CONNECTOR);
        }
    }

    /// The support of `ClprCompleteConnectorTransaction`.
    public static final class CompleteConnector
            extends KindSupport<ClprCompleteConnectorReceipt, ClprCompleteConnectorTransaction> {

        /// Creates the support (used by the service loader).
        public CompleteConnector() {
            super(ClprKinds.COMPLETE_CONNECTOR);
        }
    }

    /// The support of `ClprDeregisterConnectorTransaction`.
    public static final class DeregisterConnector
            extends KindSupport<ClprDeregisterConnectorReceipt, ClprDeregisterConnectorTransaction> {

        /// Creates the support (used by the service loader).
        public DeregisterConnector() {
            super(ClprKinds.DEREGISTER_CONNECTOR);
        }
    }

    /// The support of `ClprUpdateLedgerConfigurationTransaction`.
    public static final class UpdateLedgerConfiguration
            extends KindSupport<ClprUpdateLedgerConfigurationReceipt, ClprUpdateLedgerConfigurationTransaction> {

        /// Creates the support (used by the service loader).
        public UpdateLedgerConfiguration() {
            super(ClprKinds.UPDATE_LEDGER_CONFIGURATION);
        }
    }
}
