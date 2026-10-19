package org.hiero.consensusnode.client.internal;

import com.hederahashgraph.api.proto.java.Key;
import com.hederahashgraph.api.proto.java.Query;
import com.hederahashgraph.api.proto.java.TokenGetInfoQuery;
import com.hederahashgraph.api.proto.java.TokenGetNftInfoQuery;
import java.util.concurrent.CompletionStage;
import org.hiero.authority.Authority;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.queries.PaidQuery;
import org.hiero.consensusnode.queries.tokens.TokenInfo;
import org.hiero.consensusnode.queries.tokens.TokenInfoQuery;
import org.hiero.consensusnode.queries.tokens.TokenNftInfo;
import org.hiero.consensusnode.queries.tokens.TokenNftInfoQuery;
import org.hiero.nativeToken.NativeToken;
import org.hiero.token.TokenSupplyType;
import org.hiero.token.TokenType;
import org.jspecify.annotations.Nullable;

/// The query types of tokens.
public final class TokenQueries {

    /// `TokenInfoQuery`: `TokenService.getTokenInfo`.
    public static final QueryKind<TokenInfoQuery, TokenInfo> TOKEN_INFO = new QueryKind<>(
            "proto.TokenService", "getTokenInfo",
            (query, header) -> Query.newBuilder().setTokenGetInfo(TokenGetInfoQuery.newBuilder()
                    .setHeader(header)
                    .setToken(Protobuf.toTokenId(query.tokenId()))).build(),
            response -> response.getTokenGetInfo().getHeader(),
            (query, response) -> tokenInfo(response.getTokenGetInfo().getTokenInfo()));

    /// `TokenNftInfoQuery`: `TokenService.getTokenNftInfo`.
    public static final QueryKind<TokenNftInfoQuery, TokenNftInfo> TOKEN_NFT_INFO = new QueryKind<>(
            "proto.TokenService", "getTokenNftInfo",
            (query, header) -> Query.newBuilder().setTokenGetNftInfo(TokenGetNftInfoQuery.newBuilder()
                    .setHeader(header)
                    .setNftID(TokenKinds.nftId(query.tokenId(), query.serial()))).build(),
            response -> response.getTokenGetNftInfo().getHeader(),
            (query, response) -> tokenNftInfo(response.getTokenGetNftInfo().getNft()));

    private TokenQueries() {
    }

    /// Asks a node what a paid query costs and fails if the cost exceeds the `maxQueryPayment` of the query.
    ///
    /// @param query  the query
    /// @param kind   the query type
    /// @param client the client
    /// @param <Q> the query type
    /// @param <T> the type of the value
    /// @return the cost
    public static <Q extends PaidQuery<T>, T> CompletionStage<NativeToken<?, ?>> cost(final Q query,
                                                                                     final QueryKind<Q, T> kind,
                                                                                     final HieroClient<?> client) {
        final NativeToken<?, ?> maxQueryPayment = query.maxQueryPayment();
        return Querying.cost(query, kind, client).thenApply(cost -> {
            if (maxQueryPayment != null && cost.toBaseUnits() > maxQueryPayment.toBaseUnits()) {
                throw new IllegalStateException("The query costs " + cost + ", more than its maxQueryPayment "
                        + maxQueryPayment);
            }
            return cost;
        });
    }

    private static TokenInfo tokenInfo(final com.hederahashgraph.api.proto.java.TokenInfo info) {
        final TokenSupplyType supplyType = switch (info.getSupplyType()) {
            case INFINITE -> TokenSupplyType.INFINITE;
            case FINITE -> TokenSupplyType.FINITE;
            case UNRECOGNIZED -> throw new IllegalArgumentException("Unknown token supply type "
                    + info.getSupplyTypeValue());
        };
        final TokenType type = switch (info.getTokenType()) {
            case FUNGIBLE_COMMON -> TokenType.FUNGIBLE_COMMON;
            case NON_FUNGIBLE_UNIQUE -> TokenType.NON_FUNGIBLE_UNIQUE;
            case UNRECOGNIZED -> throw new IllegalArgumentException("Unknown token type " + info.getTokenTypeValue());
        };
        final Boolean defaultFreezeStatus = switch (info.getDefaultFreezeStatus()) {
            case Frozen -> Boolean.TRUE;
            case Unfrozen -> Boolean.FALSE;
            case FreezeNotApplicable, UNRECOGNIZED -> null;
        };
        final Boolean defaultKycStatus = switch (info.getDefaultKycStatus()) {
            case Granted -> Boolean.TRUE;
            case Revoked -> Boolean.FALSE;
            case KycNotApplicable, UNRECOGNIZED -> null;
        };
        final Boolean paused = switch (info.getPauseStatus()) {
            case Paused -> Boolean.TRUE;
            case Unpaused -> Boolean.FALSE;
            case PauseNotApplicable, UNRECOGNIZED -> null;
        };
        return new TokenInfo(
                Protobuf.fromProto(info.getTokenId()),
                type,
                info.getName(),
                info.getSymbol(),
                info.getDecimals(),
                info.getTotalSupply(),
                Protobuf.fromProto(info.getTreasury()),
                supplyType,
                supplyType == TokenSupplyType.FINITE ? info.getMaxSupply() : null,
                info.getMemo(),
                info.getMetadata().toByteArray(),
                Protobuf.fromProto(info.getExpiry()),
                info.hasAutoRenewPeriod() ? Protobuf.fromProto(info.getAutoRenewPeriod()) : null,
                info.hasAutoRenewAccount() ? Protobuf.fromProto(info.getAutoRenewAccount()) : null,
                info.getDeleted(),
                authority(info.hasAdminKey(), info.getAdminKey()),
                authority(info.hasSupplyKey(), info.getSupplyKey()),
                authority(info.hasKycKey(), info.getKycKey()),
                authority(info.hasFreezeKey(), info.getFreezeKey()),
                authority(info.hasWipeKey(), info.getWipeKey()),
                authority(info.hasPauseKey(), info.getPauseKey()),
                authority(info.hasFeeScheduleKey(), info.getFeeScheduleKey()),
                authority(info.hasMetadataKey(), info.getMetadataKey()),
                defaultFreezeStatus,
                defaultKycStatus,
                paused);
    }

    private static @Nullable Authority authority(final boolean present, final Key key) {
        return present ? Protobuf.fromProto(key) : null;
    }

    private static TokenNftInfo tokenNftInfo(final com.hederahashgraph.api.proto.java.TokenNftInfo nft) {
        return new TokenNftInfo(
                Protobuf.fromProto(nft.getNftID().getTokenID()),
                nft.getNftID().getSerialNumber(),
                Protobuf.fromProto(nft.getAccountID()),
                Protobuf.fromProto(nft.getCreationTime()),
                nft.getMetadata().toByteArray(),
                nft.hasSpenderId() ? Protobuf.fromProto(nft.getSpenderId()) : null);
    }
}
