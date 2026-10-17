package org.hiero.mirrornode.internal;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.hiero.common.Page;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.mirrornode.internal.Json.JsonObject;
import org.hiero.mirrornode.nft.Nft;
import org.hiero.mirrornode.nft.NftMetadata;
import org.hiero.mirrornode.nft.NftRepository;
import org.hiero.token.TokenType;
import org.jspecify.annotations.Nullable;

/// The `NftRepository` of the SDK, on the endpoints `/accounts/{id}/nfts`, `/tokens/{id}/nfts` and `/tokens` of the
/// Mirror Node REST API.
///
/// An NFT without owner (a burned or deleted serial) is left out. The token list of the REST API has no treasury
/// account, so the metadata of every NFT type of a page is loaded with one more request per type.
public final class DefaultNftRepository implements NftRepository {

    private static final String NFT_TYPES = "/tokens?type=" + TokenType.NON_FUNGIBLE_UNIQUE.name();

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
        Objects.requireNonNull(ownerId, "ownerId must not be null");
        return rest.page("/accounts/" + ownerId + "/nfts", "nfts", DefaultNftRepository::nft);
    }

    @Override
    public CompletionStage<Page<Nft>> findByType(final Address tokenId) {
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        return rest.page("/tokens/" + tokenId + "/nfts", "nfts", DefaultNftRepository::nft);
    }

    @Override
    public CompletionStage<@Nullable Nft> findByTypeAndSerial(final Address tokenId, final long serialNumber) {
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        return rest.getAndMap("/tokens/" + tokenId + "/nfts/" + serialNumber, DefaultNftRepository::nft);
    }

    @Override
    public CompletionStage<Page<Nft>> findByOwnerAndType(final AccountId ownerId, final Address tokenId) {
        Objects.requireNonNull(ownerId, "ownerId must not be null");
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        return rest.page("/accounts/" + ownerId + "/nfts?token.id=" + tokenId, "nfts", DefaultNftRepository::nft);
    }

    @Override
    public CompletionStage<Page<NftMetadata>> findAllTypes() {
        return rest.pageAsync(NFT_TYPES, "tokens", this::metadataOfListedToken);
    }

    @Override
    public CompletionStage<Page<NftMetadata>> findTypesByOwner(final AccountId ownerId) {
        Objects.requireNonNull(ownerId, "ownerId must not be null");
        return rest.pageAsync(NFT_TYPES + "&account.id=" + ownerId, "tokens", this::metadataOfListedToken);
    }

    @Override
    public CompletionStage<@Nullable NftMetadata> getNftMetadata(final Address tokenId) {
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        return rest.getAndMap("/tokens/" + tokenId, DefaultNftRepository::nftMetadata);
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

    private static @Nullable Nft nft(final JsonObject json) {
        final AccountId owner = Values.optAccount(json.optString("account_id"));
        if (owner == null) {
            return null;
        }
        return new Nft(Values.address(json, "token_id"), json.longValue("serial_number"), owner,
                Values.base64(json.optString("metadata")));
    }
}
