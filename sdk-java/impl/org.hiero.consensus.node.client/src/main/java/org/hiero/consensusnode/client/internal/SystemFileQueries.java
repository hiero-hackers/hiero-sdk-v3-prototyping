package org.hiero.consensusnode.client.internal;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hederahashgraph.api.proto.java.ExchangeRateSet;
import com.hederahashgraph.api.proto.java.FileGetContentsQuery;
import com.hederahashgraph.api.proto.java.Query;
import java.util.List;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.queries.systemFiles.ExchangeRates;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.ledger.ConsensusNode;
import org.hiero.nativeToken.NativeTokenUnit;

/// The queries of the system files of `consensusnode.queries.systemFiles`: `FileService.getFileContent` for a file
/// with a fixed number in the shard and realm of the network, whose contents are decoded.
public final class SystemFileQueries {

    /// rule exchange-rates-file: the number of the exchange rate file in the shard and realm of the network.
    private static final long EXCHANGE_RATES_FILE = 112L;

    /// `ExchangeRatesQuery`: `FileService.getFileContent` of the exchange rate file. The query value is the id of the
    /// file.
    public static final QueryKind<Address, ExchangeRates> EXCHANGE_RATES = new QueryKind<>(
            "proto.FileService", "getFileContent",
            (fileId, header) -> Query.newBuilder().setFileGetContents(FileGetContentsQuery.newBuilder()
                    .setHeader(header).setFileID(Protobuf.toFileId(fileId))).build(),
            response -> response.getFileGetContents().getHeader(),
            (fileId, response, unit) -> decode(response.getFileGetContents().getFileContents().getContents()
                    .toByteArray(), unit));

    private SystemFileQueries() {
    }

    /// rule exchange-rates-file: the exchange rate file in the shard and realm of the network of the client.
    ///
    /// @param client the client
    /// @return the id of the exchange rate file
    public static Address exchangeRatesFile(final HieroClient<?> client) {
        return systemFile(client, EXCHANGE_RATES_FILE);
    }

    /// A system file in the shard and realm of the network of the client. Every consensus node of a network lives in
    /// the shard and realm of that network, so the account of any node of the client gives them.
    ///
    /// @param client the client
    /// @param number the number of the file
    /// @return the id of the file
    static Address systemFile(final HieroClient<?> client, final long number) {
        final List<ConsensusNode> nodes = ClientRuntime.of(client).nodes();
        if (nodes.isEmpty()) {
            throw new IllegalStateException("The network of the client has no consensus node");
        }
        final AccountId node = nodes.getFirst().account();
        return new Address(node.shard(), node.realm(), "", number);
    }

    /// rule exchange-rates-decoding: the contents of the exchange rate file as `ExchangeRates`, quoted in the native
    /// token unit of the network.
    ///
    /// @param contents the contents of the file
    /// @param unit     the native token unit of the network
    /// @return the rates
    /// @throws MalformedAnswerException if the contents cannot be decoded or lack one of the two rates
    static ExchangeRates decode(final byte[] contents, final NativeTokenUnit unit) {
        final ExchangeRateSet rates;
        try {
            rates = ExchangeRateSet.parseFrom(contents);
        } catch (final InvalidProtocolBufferException invalid) {
            throw new MalformedAnswerException("The exchange rate file cannot be decoded", invalid);
        }
        if (!rates.hasCurrentRate() || !rates.hasNextRate()) {
            throw new MalformedAnswerException("The exchange rate file lacks the current or the next rate", null);
        }
        return new ExchangeRates(Protobuf.currentRate(rates, unit), Protobuf.nextRate(rates, unit));
    }
}
