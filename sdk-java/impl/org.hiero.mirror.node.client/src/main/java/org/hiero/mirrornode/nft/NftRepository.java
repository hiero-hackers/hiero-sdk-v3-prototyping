package org.hiero.mirrornode.nft;

import java.util.List;
import java.util.concurrent.CompletionStage;
import org.hiero.common.Page;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.mirrornode.common.Order;
import org.jspecify.annotations.Nullable;

public interface NftRepository {

    /// The NFTs owned by the account. Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/nfts.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<Nft>> findByOwner(final AccountId ownerId);

    /// The NFTs owned by the account, filtered by serial number, spender and token. A `serialNumber` condition needs
    /// a single `tokenId`. Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/nfts.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<Nft>> findByOwner(final AccountId ownerId, final @Nullable Integer limit, final @Nullable Order order, final List<String> serialNumber, final List<String> spenderId, final List<String> tokenId);

    /// The NFTs of the type. Maps to GET /api/v1/tokens/{tokenId}/nfts.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<Nft>> findByType(final Address tokenId);

    /// The NFTs of the type, filtered by owner and serial number. Maps to GET /api/v1/tokens/{tokenId}/nfts.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<Nft>> findByType(final Address tokenId, final List<String> accountId, final @Nullable Integer limit, final @Nullable Order order, final List<String> serialNumber);

    /// The NFT, or null if the mirror node does not know it.
    /// Maps to GET /api/v1/tokens/{tokenId}/nfts/{serialNumber}.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<@Nullable Nft> findByTypeAndSerial(final Address tokenId, final long serialNumber);

    /// The NFTs of the type owned by the account. Maps to GET /api/v1/accounts/{idOrAliasOrEvmAddress}/nfts.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<Nft>> findByOwnerAndType(final AccountId ownerId, final Address tokenId);

    /// The transfer history of the NFT. Maps to GET /api/v1/tokens/{tokenId}/nfts/{serialNumber}/transactions.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<NftTransactionTransfer>> findTransactionHistory(final Address tokenId, final long serialNumber);

    /// The transfer history of the NFT in the given period.
    /// Maps to GET /api/v1/tokens/{tokenId}/nfts/{serialNumber}/transactions.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<NftTransactionTransfer>> findTransactionHistory(final Address tokenId, final long serialNumber, final @Nullable Integer limit, final @Nullable Order order, final List<String> timestamp);

    /// The NFT types of the network. Maps to GET /api/v1/tokens (type NON_FUNGIBLE_UNIQUE) and
    /// GET /api/v1/tokens/{tokenId} per type.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<NftMetadata>> findAllTypes();

    /// The NFT types associated with the account. Maps to GET /api/v1/tokens (type NON_FUNGIBLE_UNIQUE) and
    /// GET /api/v1/tokens/{tokenId} per type.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<NftMetadata>> findTypesByOwner(final AccountId ownerId);

    /// The metadata of the NFT type, or null if the token is unknown or no NFT type.
    /// Maps to GET /api/v1/tokens/{tokenId}.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<@Nullable NftMetadata> getNftMetadata(final Address tokenId);
}
