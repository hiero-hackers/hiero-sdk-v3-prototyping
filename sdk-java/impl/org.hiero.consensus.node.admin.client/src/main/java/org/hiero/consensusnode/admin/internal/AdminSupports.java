package org.hiero.consensusnode.admin.internal;

import com.google.auto.service.AutoService;
import org.hiero.consensusnode.admin.freeze.FreezeReceipt;
import org.hiero.consensusnode.admin.freeze.FreezeTransaction;
import org.hiero.consensusnode.admin.nodes.NodeCreateReceipt;
import org.hiero.consensusnode.admin.nodes.NodeCreateTransaction;
import org.hiero.consensusnode.admin.nodes.NodeDeleteReceipt;
import org.hiero.consensusnode.admin.nodes.NodeDeleteTransaction;
import org.hiero.consensusnode.admin.nodes.NodeUpdateReceipt;
import org.hiero.consensusnode.admin.nodes.NodeUpdateTransaction;
import org.hiero.consensusnode.admin.system.SystemDeleteReceipt;
import org.hiero.consensusnode.admin.system.SystemDeleteTransaction;
import org.hiero.consensusnode.admin.system.SystemUndeleteReceipt;
import org.hiero.consensusnode.admin.system.SystemUndeleteTransaction;
import org.hiero.consensusnode.client.internal.KindSupport;
import org.hiero.consensusnode.transactions.spi.TransactionSupport;

/// The transaction types of the administration client, registered with the `ServiceLoader` as `TransactionSupport`,
/// so that the client module finds them (`PackedTransaction.fromBytes`, `SpiFactory`): on the module path by the
/// `provides` of `module-info.java`, on the class path by the `META-INF/services` files `@AutoService` generates.
// @AutoService can name the generic TransactionSupport only as a raw type
@SuppressWarnings("rawtypes")
public final class AdminSupports {

    private AdminSupports() {
    }

    /// The support of `FreezeTransaction`.
    @AutoService(TransactionSupport.class)
    public static final class Freeze extends KindSupport<FreezeReceipt, FreezeTransaction> {

        /// Creates the support (used by the service loader).
        public Freeze() {
            super(AdminKinds.FREEZE);
        }
    }

    /// The support of `NodeCreateTransaction`.
    @AutoService(TransactionSupport.class)
    public static final class NodeCreate extends KindSupport<NodeCreateReceipt, NodeCreateTransaction> {

        /// Creates the support (used by the service loader).
        public NodeCreate() {
            super(AdminKinds.NODE_CREATE);
        }
    }

    /// The support of `NodeUpdateTransaction`.
    @AutoService(TransactionSupport.class)
    public static final class NodeUpdate extends KindSupport<NodeUpdateReceipt, NodeUpdateTransaction> {

        /// Creates the support (used by the service loader).
        public NodeUpdate() {
            super(AdminKinds.NODE_UPDATE);
        }
    }

    /// The support of `NodeDeleteTransaction`.
    @AutoService(TransactionSupport.class)
    public static final class NodeDelete extends KindSupport<NodeDeleteReceipt, NodeDeleteTransaction> {

        /// Creates the support (used by the service loader).
        public NodeDelete() {
            super(AdminKinds.NODE_DELETE);
        }
    }

    /// The support of `SystemDeleteTransaction`.
    @AutoService(TransactionSupport.class)
    public static final class SystemDelete extends KindSupport<SystemDeleteReceipt, SystemDeleteTransaction> {

        /// Creates the support (used by the service loader).
        public SystemDelete() {
            super(AdminKinds.SYSTEM_DELETE);
        }
    }

    /// The support of `SystemUndeleteTransaction`.
    @AutoService(TransactionSupport.class)
    public static final class SystemUndelete extends KindSupport<SystemUndeleteReceipt, SystemUndeleteTransaction> {

        /// Creates the support (used by the service loader).
        public SystemUndelete() {
            super(AdminKinds.SYSTEM_UNDELETE);
        }
    }
}
