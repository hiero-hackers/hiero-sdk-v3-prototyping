package org.hiero.consensusnode.clpr.internal;

import com.google.auto.service.AutoService;
import java.util.Set;
import org.hiero.consensusnode.client.internal.KindSupport;
import org.hiero.consensusnode.clpr.ClprRequestType;
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
import org.hiero.consensusnode.requestTypes.RequestType;
import org.hiero.consensusnode.status.TransactionStatus;
import org.hiero.consensusnode.transactions.spi.RequestTypeSupport;
import org.hiero.consensusnode.transactions.spi.TransactionStatusSupport;
import org.hiero.consensusnode.transactions.spi.TransactionSupport;

/// The transaction types of the CLPR module, registered with the `ServiceLoader` as `TransactionSupport`, so that the
/// client module finds them (`PackedTransaction.fromBytes`, `SpiFactory`), the CLPR status codes, registered as
/// `TransactionStatusSupport`, and the CLPR request types, registered as `RequestTypeSupport`: on the module path by the
/// `provides` of `module-info.java`, on the class path by the `META-INF/services` files `@AutoService` generates.
// @AutoService can name the generic TransactionSupport only as a raw type
@SuppressWarnings("rawtypes")
public final class ClprSupports {

    private ClprSupports() {
    }

    /// The status codes of the CLPR service.
    @AutoService(TransactionStatusSupport.class)
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

    /// The request types of the CLPR service.
    @AutoService(RequestTypeSupport.class)
    public static final class RequestTypes implements RequestTypeSupport {

        /// Creates the support (used by the service loader).
        public RequestTypes() {
            // nothing to set up
        }

        @Override
        public Set<RequestType> getRequestTypes() {
            return Set.of(ClprRequestType.values());
        }
    }

    /// The support of `ClprRegisterChannelTransaction`.
    @AutoService(TransactionSupport.class)
    public static final class RegisterChannel
            extends KindSupport<ClprRegisterChannelReceipt, ClprRegisterChannelTransaction> {

        /// Creates the support (used by the service loader).
        public RegisterChannel() {
            super(ClprKinds.REGISTER_CHANNEL);
        }
    }

    /// The support of `ClprCompleteChannelTransaction`.
    @AutoService(TransactionSupport.class)
    public static final class CompleteChannel
            extends KindSupport<ClprCompleteChannelReceipt, ClprCompleteChannelTransaction> {

        /// Creates the support (used by the service loader).
        public CompleteChannel() {
            super(ClprKinds.COMPLETE_CHANNEL);
        }
    }

    /// The support of `ClprCloseChannelTransaction`.
    @AutoService(TransactionSupport.class)
    public static final class CloseChannel extends KindSupport<ClprCloseChannelReceipt, ClprCloseChannelTransaction> {

        /// Creates the support (used by the service loader).
        public CloseChannel() {
            super(ClprKinds.CLOSE_CHANNEL);
        }
    }

    /// The support of `ClprSubmitBundleTransaction`.
    @AutoService(TransactionSupport.class)
    public static final class SubmitBundle extends KindSupport<ClprSubmitBundleReceipt, ClprSubmitBundleTransaction> {

        /// Creates the support (used by the service loader).
        public SubmitBundle() {
            super(ClprKinds.SUBMIT_BUNDLE);
        }
    }

    /// The support of `ClprRegisterConnectorTransaction`.
    @AutoService(TransactionSupport.class)
    public static final class RegisterConnector
            extends KindSupport<ClprRegisterConnectorReceipt, ClprRegisterConnectorTransaction> {

        /// Creates the support (used by the service loader).
        public RegisterConnector() {
            super(ClprKinds.REGISTER_CONNECTOR);
        }
    }

    /// The support of `ClprCompleteConnectorTransaction`.
    @AutoService(TransactionSupport.class)
    public static final class CompleteConnector
            extends KindSupport<ClprCompleteConnectorReceipt, ClprCompleteConnectorTransaction> {

        /// Creates the support (used by the service loader).
        public CompleteConnector() {
            super(ClprKinds.COMPLETE_CONNECTOR);
        }
    }

    /// The support of `ClprDeregisterConnectorTransaction`.
    @AutoService(TransactionSupport.class)
    public static final class DeregisterConnector
            extends KindSupport<ClprDeregisterConnectorReceipt, ClprDeregisterConnectorTransaction> {

        /// Creates the support (used by the service loader).
        public DeregisterConnector() {
            super(ClprKinds.DEREGISTER_CONNECTOR);
        }
    }

    /// The support of `ClprUpdateLedgerConfigurationTransaction`.
    @AutoService(TransactionSupport.class)
    public static final class UpdateLedgerConfiguration
            extends KindSupport<ClprUpdateLedgerConfigurationReceipt, ClprUpdateLedgerConfigurationTransaction> {

        /// Creates the support (used by the service loader).
        public UpdateLedgerConfiguration() {
            super(ClprKinds.UPDATE_LEDGER_CONFIGURATION);
        }
    }
}
