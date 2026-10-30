package org.hiero.consensusnode.admin.internal;

import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;
import com.google.protobuf.BytesValue;
import com.google.protobuf.StringValue;
import com.hederahashgraph.api.proto.java.FreezeTransactionBody;
import com.hederahashgraph.api.proto.java.NodeCreateTransactionBody;
import com.hederahashgraph.api.proto.java.NodeDeleteTransactionBody;
import com.hederahashgraph.api.proto.java.NodeUpdateTransactionBody;
import com.hederahashgraph.api.proto.java.SystemDeleteTransactionBody;
import com.hederahashgraph.api.proto.java.SystemUndeleteTransactionBody;
import com.hederahashgraph.api.proto.java.TimestampSeconds;
import com.hederahashgraph.api.proto.java.TransactionBody;
import java.time.ZonedDateTime;
import java.util.List;
import org.hiero.authority.Authority;
import org.hiero.consensusnode.admin.freeze.FreezeReceipt;
import org.hiero.consensusnode.admin.freeze.FreezeTransaction;
import org.hiero.consensusnode.admin.freeze.FreezeType;
import org.hiero.consensusnode.admin.nodes.NodeCreateReceipt;
import org.hiero.consensusnode.admin.nodes.NodeCreateTransaction;
import org.hiero.consensusnode.admin.nodes.NodeDeleteReceipt;
import org.hiero.consensusnode.admin.nodes.NodeDeleteTransaction;
import org.hiero.consensusnode.admin.nodes.NodeUpdateReceipt;
import org.hiero.consensusnode.admin.nodes.NodeUpdateTransaction;
import org.hiero.consensusnode.admin.nodes.ServiceEndpoint;
import org.hiero.consensusnode.admin.system.SystemDeleteReceipt;
import org.hiero.consensusnode.admin.system.SystemDeleteTransaction;
import org.hiero.consensusnode.admin.system.SystemUndeleteReceipt;
import org.hiero.consensusnode.admin.system.SystemUndeleteTransaction;
import org.hiero.consensusnode.client.internal.Protobuf;
import org.hiero.consensusnode.client.internal.TransactionKind;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.ledger.ContractId;

/// The transaction types of the administration of the network (freeze, nodes, system delete).
public final class AdminKinds {

    /// `FreezeTransaction`: `FreezeService.freeze`.
    public static final TransactionKind<FreezeReceipt, FreezeTransaction> FREEZE = new TransactionKind<>(
            FreezeTransaction.class, "proto.FreezeService", "freeze", TransactionBody.DataCase.FREEZE,
            AdminKinds::freeze,
            (id, status, rate, nextRate, receipt) -> new FreezeReceipt(id, status, rate, nextRate));

    /// `NodeCreateTransaction`: `AddressBookService.createNode`.
    public static final TransactionKind<NodeCreateReceipt, NodeCreateTransaction> NODE_CREATE = new TransactionKind<>(
            NodeCreateTransaction.class, "proto.AddressBookService", "createNode", TransactionBody.DataCase.NODECREATE,
            AdminKinds::nodeCreate,
            (id, status, rate, nextRate, receipt) -> new NodeCreateReceipt(id, status, rate, nextRate,
                    receipt.getNodeId()));

    /// `NodeUpdateTransaction`: `AddressBookService.updateNode`.
    public static final TransactionKind<NodeUpdateReceipt, NodeUpdateTransaction> NODE_UPDATE = new TransactionKind<>(
            NodeUpdateTransaction.class, "proto.AddressBookService", "updateNode", TransactionBody.DataCase.NODEUPDATE,
            AdminKinds::nodeUpdate,
            (id, status, rate, nextRate, receipt) -> new NodeUpdateReceipt(id, status, rate, nextRate));

    /// `NodeDeleteTransaction`: `AddressBookService.deleteNode`.
    public static final TransactionKind<NodeDeleteReceipt, NodeDeleteTransaction> NODE_DELETE = new TransactionKind<>(
            NodeDeleteTransaction.class, "proto.AddressBookService", "deleteNode", TransactionBody.DataCase.NODEDELETE,
            (transaction, body) -> body.setNodeDelete(NodeDeleteTransactionBody.newBuilder()
                    .setNodeId(transaction.nodeId())),
            (id, status, rate, nextRate, receipt) -> new NodeDeleteReceipt(id, status, rate, nextRate));

    /// `SystemDeleteTransaction`: `FileService.systemDelete`.
    public static final TransactionKind<SystemDeleteReceipt, SystemDeleteTransaction> SYSTEM_DELETE = new TransactionKind<>(
            SystemDeleteTransaction.class, "proto.FileService", "systemDelete", TransactionBody.DataCase.SYSTEMDELETE,
            AdminKinds::systemDelete,
            (id, status, rate, nextRate, receipt) -> new SystemDeleteReceipt(id, status, rate, nextRate));

    /// `SystemUndeleteTransaction`: `FileService.systemUndelete`.
    public static final TransactionKind<SystemUndeleteReceipt, SystemUndeleteTransaction> SYSTEM_UNDELETE = new TransactionKind<>(
            SystemUndeleteTransaction.class, "proto.FileService", "systemUndelete", TransactionBody.DataCase.SYSTEMUNDELETE,
            AdminKinds::systemUndelete,
            (id, status, rate, nextRate, receipt) -> new SystemUndeleteReceipt(id, status, rate, nextRate));

    /// All transaction types of this class.
    public static final List<TransactionKind<?, ?>> ALL = List.of(FREEZE, NODE_CREATE, NODE_UPDATE, NODE_DELETE, SYSTEM_DELETE, SYSTEM_UNDELETE);

    private AdminKinds() {
    }

    /// Fills the freeze body. The fields that do not fit the freeze type are sent as they are set; the consensus node
    /// decides whether they fit.
    private static void freeze(final FreezeTransaction transaction, final TransactionBody.Builder body) {
        final FreezeTransactionBody.Builder freeze = FreezeTransactionBody.newBuilder()
                .setFreezeType(toProto(transaction.freezeType()));
        final ZonedDateTime startTime = transaction.startTime();
        if (startTime != null) {
            freeze.setStartTime(Protobuf.toProto(startTime));
        }
        final Address updateFile = transaction.updateFile();
        if (updateFile != null) {
            freeze.setUpdateFile(Protobuf.toFileId(updateFile));
        }
        final byte[] fileHash = transaction.fileHash();
        if (fileHash != null) {
            freeze.setFileHash(ByteString.copyFrom(fileHash));
        }
        body.setFreeze(freeze);
    }

    private static com.hederahashgraph.api.proto.java.FreezeType toProto(final FreezeType freezeType) {
        return switch (freezeType) {
            case FREEZE_ONLY -> com.hederahashgraph.api.proto.java.FreezeType.FREEZE_ONLY;
            case PREPARE_UPGRADE -> com.hederahashgraph.api.proto.java.FreezeType.PREPARE_UPGRADE;
            case FREEZE_UPGRADE -> com.hederahashgraph.api.proto.java.FreezeType.FREEZE_UPGRADE;
            case FREEZE_ABORT -> com.hederahashgraph.api.proto.java.FreezeType.FREEZE_ABORT;
            case TELEMETRY_UPGRADE -> com.hederahashgraph.api.proto.java.FreezeType.TELEMETRY_UPGRADE;
        };
    }

    /// Fills the node create body. The description, the gRPC certificate hash and the gRPC-web proxy endpoint (HIP-1046)
    /// are sent only when they are set.
    private static void nodeCreate(final NodeCreateTransaction transaction, final TransactionBody.Builder body) {
        final NodeCreateTransactionBody.Builder create = NodeCreateTransactionBody.newBuilder()
                .setAccountId(Protobuf.toProto(transaction.accountId()))
                .addAllGossipEndpoint(Endpoints.toProto(transaction.gossipEndpoints()))
                .addAllServiceEndpoint(Endpoints.toProto(transaction.serviceEndpoints()))
                .setGossipCaCertificate(ByteString.copyFrom(transaction.gossipCaCertificate()))
                .setAdminKey(Protobuf.toProto(transaction.adminAuthority()))
                .setDeclineReward(transaction.declineReward());
        final String description = transaction.description();
        if (description != null) {
            create.setDescription(description);
        }
        final byte[] grpcCertificateHash = transaction.grpcCertificateHash();
        if (grpcCertificateHash != null) {
            create.setGrpcCertificateHash(ByteString.copyFrom(grpcCertificateHash));
        }
        final ServiceEndpoint grpcWebProxyEndpoint = transaction.grpcWebProxyEndpoint();
        if (grpcWebProxyEndpoint != null) {
            create.setGrpcProxyEndpoint(Endpoints.toProto(grpcWebProxyEndpoint));
        }
        body.setNodeCreate(create);
    }

    /// Fills the node update body. Only what is set is sent; an empty endpoint list is not sent, which leaves the list
    /// of the node unchanged (protobuf cannot tell an empty repeated field from an absent one).
    private static void nodeUpdate(final NodeUpdateTransaction transaction, final TransactionBody.Builder body) {
        final NodeUpdateTransactionBody.Builder update = NodeUpdateTransactionBody.newBuilder()
                .setNodeId(transaction.nodeId())
                .addAllGossipEndpoint(Endpoints.toProto(transaction.gossipEndpoints()))
                .addAllServiceEndpoint(Endpoints.toProto(transaction.serviceEndpoints()));
        final AccountId accountId = transaction.accountId();
        if (accountId != null) {
            update.setAccountId(Protobuf.toProto(accountId));
        }
        final String description = transaction.description();
        if (description != null) {
            update.setDescription(StringValue.of(description));
        }
        final byte[] gossipCaCertificate = transaction.gossipCaCertificate();
        if (gossipCaCertificate != null) {
            update.setGossipCaCertificate(BytesValue.of(ByteString.copyFrom(gossipCaCertificate)));
        }
        final byte[] grpcCertificateHash = transaction.grpcCertificateHash();
        if (grpcCertificateHash != null) {
            update.setGrpcCertificateHash(BytesValue.of(ByteString.copyFrom(grpcCertificateHash)));
        }
        final Authority adminAuthority = transaction.adminAuthority();
        if (adminAuthority != null) {
            update.setAdminKey(Protobuf.toProto(adminAuthority));
        }
        final Boolean declineReward = transaction.declineReward();
        if (declineReward != null) {
            update.setDeclineReward(BoolValue.of(declineReward));
        }
        final ServiceEndpoint grpcWebProxyEndpoint = transaction.grpcWebProxyEndpoint();
        if (grpcWebProxyEndpoint != null) {
            update.setGrpcProxyEndpoint(Endpoints.toProto(grpcWebProxyEndpoint));
        }
        body.setNodeUpdate(update);
    }

    /// Fills the system delete body. HAPI carries the expiration time in whole seconds, so its sub-second part is
    /// dropped; it is sent whenever it is set, also for a contract (the consensus node ignores it there).
    private static void systemDelete(final SystemDeleteTransaction transaction, final TransactionBody.Builder body) {
        final SystemDeleteTransactionBody.Builder delete = SystemDeleteTransactionBody.newBuilder();
        final Address fileId = transaction.fileId();
        if (fileId != null) {
            delete.setFileID(Protobuf.toFileId(fileId));
        }
        final ContractId contractId = transaction.contractId();
        if (contractId != null) {
            delete.setContractID(Protobuf.toProto(contractId));
        }
        final ZonedDateTime expirationTime = transaction.expirationTime();
        if (expirationTime != null) {
            delete.setExpirationTime(TimestampSeconds.newBuilder()
                    .setSeconds(expirationTime.toInstant().getEpochSecond()));
        }
        body.setSystemDelete(delete);
    }

    private static void systemUndelete(final SystemUndeleteTransaction transaction, final TransactionBody.Builder body) {
        final SystemUndeleteTransactionBody.Builder undelete = SystemUndeleteTransactionBody.newBuilder();
        final Address fileId = transaction.fileId();
        if (fileId != null) {
            undelete.setFileID(Protobuf.toFileId(fileId));
        }
        final ContractId contractId = transaction.contractId();
        if (contractId != null) {
            undelete.setContractID(Protobuf.toProto(contractId));
        }
        body.setSystemUndelete(undelete);
    }
}
