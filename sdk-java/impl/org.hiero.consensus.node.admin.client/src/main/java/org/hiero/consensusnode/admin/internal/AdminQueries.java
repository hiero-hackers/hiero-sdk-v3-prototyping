package org.hiero.consensusnode.admin.internal;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hederahashgraph.api.proto.java.FileGetContentsQuery;
import com.hederahashgraph.api.proto.java.FileID;
import com.hederahashgraph.api.proto.java.NetworkGetVersionInfoQuery;
import com.hederahashgraph.api.proto.java.Query;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import org.hiero.consensusnode.admin.network.NetworkVersionInfo;
import org.hiero.consensusnode.admin.network.NetworkVersionInfoQuery;
import org.hiero.consensusnode.admin.network.NodeAddress;
import org.hiero.consensusnode.admin.network.NodeAddressBook;
import org.hiero.consensusnode.admin.network.SemanticVersion;
import org.hiero.consensusnode.client.internal.Protobuf;
import org.hiero.consensusnode.client.internal.QueryKind;
import org.hiero.ledger.AccountId;
import org.jspecify.annotations.Nullable;

/// The query types of the administration of the network.
public final class AdminQueries {

    /// The number of the address-book file (`0.0.102` on the default shard and realm).
    private static final long ADDRESS_BOOK_FILE = 102;

    /// `NetworkVersionInfoQuery`: `NetworkService.getVersionInfo`.
    public static final QueryKind<NetworkVersionInfoQuery, NetworkVersionInfo> NETWORK_VERSION_INFO = new QueryKind<>(
            "proto.NetworkService", "getVersionInfo",
            (query, header) -> Query.newBuilder().setNetworkGetVersionInfo(NetworkGetVersionInfoQuery.newBuilder()
                    .setHeader(header)).build(),
            response -> response.getNetworkGetVersionInfo().getHeader(),
            (query, response) -> new NetworkVersionInfo(
                    version(response.getNetworkGetVersionInfo().getHapiProtoVersion()),
                    version(response.getNetworkGetVersionInfo().getHederaServicesVersion())));

    /// `NodeAddressBookQuery`: `FileService.getFileContent` of the address-book file, whose id is the query (the file
    /// lives in the shard and realm of the network, which the query itself does not know).
    public static final QueryKind<FileID, NodeAddressBook> NODE_ADDRESS_BOOK = new QueryKind<>(
            "proto.FileService", "getFileContent",
            (file, header) -> Query.newBuilder().setFileGetContents(FileGetContentsQuery.newBuilder()
                    .setHeader(header).setFileID(file)).build(),
            response -> response.getFileGetContents().getHeader(),
            (file, response) -> addressBook(response.getFileGetContents().getFileContents().getContents()));

    private AdminQueries() {
    }

    /// The id of the address-book file in the shard and realm of an account of the network (the operator).
    ///
    /// @param account an account of the network
    /// @return the file id
    public static FileID addressBookFile(final AccountId account) {
        return FileID.newBuilder().setShardNum(account.shard()).setRealmNum(account.realm())
                .setFileNum(ADDRESS_BOOK_FILE).build();
    }

    /// Maps a semantic version message; an empty label is an absent one.
    private static SemanticVersion version(final com.hederahashgraph.api.proto.java.SemanticVersion version) {
        return new SemanticVersion(version.getMajor(), version.getMinor(), version.getPatch(),
                emptyToNull(version.getPre()), emptyToNull(version.getBuild()));
    }

    private static @Nullable String emptyToNull(final String value) {
        return value.isEmpty() ? null : value;
    }

    /// Parses the content of the address-book file, a `NodeAddressBook` message.
    private static NodeAddressBook addressBook(final ByteString contents) {
        final com.hederahashgraph.api.proto.java.NodeAddressBook book;
        try {
            book = com.hederahashgraph.api.proto.java.NodeAddressBook.parseFrom(contents);
        } catch (final InvalidProtocolBufferException e) {
            throw new IllegalStateException("The address-book file holds no NodeAddressBook", e);
        }
        final List<NodeAddress> nodes = book.getNodeAddressList().stream()
                .map(AdminQueries::nodeAddress)
                .sorted(Comparator.comparingLong(NodeAddress::nodeId))
                .toList();
        return new NodeAddressBook(nodes);
    }

    /// Maps one entry of the address-book file. The file carries no gossip CA certificate, so it is empty; the
    /// certificate hash and the RSA key are hex text in the file and are decoded to their bytes.
    private static NodeAddress nodeAddress(final com.hederahashgraph.api.proto.java.NodeAddress node) {
        return new NodeAddress(node.getNodeId(), Protobuf.fromProto(node.getNodeAccountId()),
                node.getServiceEndpointList().stream().map(Endpoints::fromProto).filter(Objects::nonNull).toList(),
                emptyToNull(node.getDescription()), new byte[0],
                hexOrRaw(node.getNodeCertHash().toString(StandardCharsets.UTF_8), node.getNodeCertHash().toByteArray()),
                hexOrRaw(node.getRSAPubKey(), node.getRSAPubKey().getBytes(StandardCharsets.UTF_8)));
    }

    /// Decodes hex text (optionally with a `0x` prefix); bytes that are no hex text are kept as they are.
    private static byte[] hexOrRaw(final String text, final byte[] raw) {
        final String hex = text.startsWith("0x") ? text.substring(2) : text;
        if (hex.isEmpty() || hex.length() % 2 != 0 || !hex.chars().allMatch(c -> Character.digit(c, 16) >= 0)) {
            return raw;
        }
        return HexFormat.of().parseHex(hex);
    }
}
