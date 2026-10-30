package org.hiero.mirrornode.internal;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.hiero.common.Page;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.mirrornode.common.Order;
import org.hiero.mirrornode.internal.Json.JsonObject;
import org.hiero.mirrornode.nft.Nft;
import org.hiero.mirrornode.nft.NftMetadata;
import org.hiero.mirrornode.nft.NftRepository;
import org.hiero.mirrornode.nft.NftTransactionTransfer;
import org.hiero.mirrornode.transaction.BasicTransactionType;
import org.hiero.mirrornode.transaction.TransactionType;
import org.hiero.token.TokenType;
import org.jspecify.annotations.Nullable;

/// The `NftRepository` of the SDK, on the endpoints `/accounts/{id}/nfts`, `/tokens/{id}/nfts`,
/// `/tokens/{id}/nfts/{serial}/transactions` and `/tokens` of the Mirror Node REST API.
///
/// The token list of the REST API has no treasury account, so the metadata of every NFT type of a page is loaded with
/// one more request per type.
public final class DefaultNftRepository implements NftRepository {

    private final MirrorNodeRest rest;

    /// Creates the repository on `rest`.
    ///
    /// @param rest the calls against the mirror node
    /// @throws NullPointerException if `rest` is `null`
    public DefaultNftRepository(final MirrorNodeRest rest) {
        this.rest = Objects.requireNonNull(rest, "rest must not be null");
    }

    @Override
    public CompletionStage<Page<Nft>> findByOwner(final AccountId ownerId) {
        return findByOwner(ownerId, null, null, List.of(), List.of(), List.of());
    }

    @Override
    public CompletionStage<Page<Nft>> findByOwner(final AccountId ownerId, final @Nullable Integer limit,
            final @Nullable Order order, final List<String> serialNumber, final List<String> spenderId,
            final List<String> tokenId) {
        Objects.requireNonNull(ownerId, "ownerId must not be null");
        final String path = Query.of("/accounts/" + ownerId + "/nfts").add("limit", limit).add("order", order)
                .addAll("serialnumber", serialNumber).addAll("spender.id", spenderId).addAll("token.id", tokenId)
                .build();
        return rest.page(path, "nfts", DefaultNftRepository::nft);
    }

    @Override
    public CompletionStage<Page<Nft>> findByType(final Address tokenId) {
        return findByType(tokenId, List.of(), null, null, List.of());
    }

    @Override
    public CompletionStage<Page<Nft>> findByType(final Address tokenId, final List<String> accountId,
            final @Nullable Integer limit, final @Nullable Order order, final List<String> serialNumber) {
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        final String path = Query.of("/tokens/" + tokenId + "/nfts").addAll("account.id", accountId)
                .add("limit", limit).add("order", order).addAll("serialnumber", serialNumber).build();
        return rest.page(path, "nfts", DefaultNftRepository::nft);
    }

    @Override
    public CompletionStage<@Nullable Nft> findByTypeAndSerial(final Address tokenId, final long serialNumber) {
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        return rest.getAndMap("/tokens/" + tokenId + "/nfts/" + serialNumber, DefaultNftRepository::nft);
    }

    @Override
    public CompletionStage<Page<Nft>> findByOwnerAndType(final AccountId ownerId, final Address tokenId) {
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        return findByOwner(ownerId, null, null, List.of(), List.of(), List.of(tokenId.toString()));
    }

    @Override
    public CompletionStage<Page<NftTransactionTransfer>> findTransactionHistory(final Address tokenId,
            final long serialNumber) {
        return findTransactionHistory(tokenId, serialNumber, null, null, List.of());
    }

    @Override
    public CompletionStage<Page<NftTransactionTransfer>> findTransactionHistory(final Address tokenId,
            final long serialNumber, final @Nullable Integer limit, final @Nullable Order order,
            final List<String> timestamp) {
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        final String path = Query.of("/tokens/" + tokenId + "/nfts/" + serialNumber + "/transactions")
                .add("limit", limit).add("order", order).addAll("timestamp", timestamp).build();
        return rest.page(path, "transactions", DefaultNftRepository::transactionTransfer);
    }

    @Override
    public CompletionStage<Page<NftMetadata>> findAllTypes() {
        return rest.pageAsync(nftTypes(List.of()), "tokens", this::metadataOfListedToken);
    }

    @Override
    public CompletionStage<Page<NftMetadata>> findTypesByOwner(final AccountId ownerId) {
        Objects.requireNonNull(ownerId, "ownerId must not be null");
        return rest.pageAsync(nftTypes(List.of(ownerId.toString())), "tokens", this::metadataOfListedToken);
    }

    @Override
    public CompletionStage<@Nullable NftMetadata> getNftMetadata(final Address tokenId) {
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        return rest.getAndMap("/tokens/" + tokenId, DefaultNftRepository::nftMetadata);
    }

    private static String nftTypes(final List<String> accountId) {
        return Query.of("/tokens").addAll("account.id", accountId)
                .add("type", TokenType.NON_FUNGIBLE_UNIQUE).build();
    }

    private CompletionStage<@Nullable NftMetadata> metadataOfListedToken(final JsonObject token) {
        final String tokenId = token.optString("token_id");
        if (tokenId == null) {
            return CompletableFuture.completedFuture(null);
        }
        return rest.getAndMap("/tokens/" + tokenId, DefaultNftRepository::nftMetadata);
    }

    /// Maps a token of `/tokens/{id}`; `null` for a fungible token, which is no NFT type.
    private static @Nullable NftMetadata nftMetadata(final JsonObject json) {
        if (!TokenType.NON_FUNGIBLE_UNIQUE.name().equals(json.optString("type"))) {
            return null;
        }
        return new NftMetadata(Values.address(json, "token_id"), json.string("name"), json.string("symbol"),
                Values.account(json, "treasury_account_id"));
    }

    private static Nft nft(final JsonObject json) {
        return new Nft(
                Values.address(json, "token_id"),
                json.longValue("serial_number"),
                Values.optAccount(json.optString("account_id")),
                Values.optBase64(json.optString("metadata")),
                Values.timestamp(json.optString("created_timestamp")),
                Values.timestamp(json.optString("modified_timestamp")),
                Values.optAccount(json.optString("delegating_spender")),
                Values.optAccount(json.optString("spender")),
                json.bool("deleted", false));
    }

    private static NftTransactionTransfer transactionTransfer(final JsonObject json) {
        final Long nonce = json.optLong("nonce");
        final int nonceValue = nonce == null ? 0 : Math.toIntExact(nonce);
        return new NftTransactionTransfer(
                Values.requiredTimestamp(json, "consensus_timestamp"),
                json.bool("is_approval", false),
                nonceValue,
                Values.optAccount(json.optString("receiver_account_id")),
                Values.optAccount(json.optString("sender_account_id")),
                Values.transactionId(json.string("transaction_id"), nonceValue),
                transactionType(json.string("type")));
    }

    /// Returns the known transaction type with the REST name `name`, or an unknown type with that name.
    private static TransactionType transactionType(final String name) {
        for (final BasicTransactionType type : BasicTransactionType.values()) {
            if (type.protocolName().equals(name)) {
                return type;
            }
        }
        return new DefaultTransactionRepository.UnknownTransactionType(name);
    }
}
