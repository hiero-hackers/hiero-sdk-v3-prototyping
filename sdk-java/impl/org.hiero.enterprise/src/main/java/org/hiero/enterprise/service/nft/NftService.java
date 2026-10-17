package org.hiero.enterprise.service.nft;

import java.util.List;
import java.util.Set;
import org.hiero.authority.Authority;
import org.hiero.common.Page;
import org.hiero.enterprise.service.ServiceException;
import org.hiero.enterprise.service.internal.Unbound;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.mirrornode.nft.Nft;
import org.hiero.mirrornode.nft.NftMetadata;
import org.jspecify.annotations.Nullable;

public class NftService {

    /// Creates a new `NftService` that is not bound to a session: every method fails with `ServiceException`.
    /// A usable service is created with `NftFactory.createService(session)`.
    public NftService() {
    }

    /// Create a new NFT type (collection); returns the metadata of the new type, including its token id.
    /// The treasury defaults to the operator account; a supply authority enables minting. The same as createNftType
    /// with NftTypeCreateParams that have only these values.
    ///
    /// @throws ServiceException if a service error occurs
    public NftMetadata createNftType(final String name, final String symbol) {
        throw Unbound.error("NftService", "NftFactory");
    }

    /// @throws ServiceException if a service error occurs
    public NftMetadata createNftType(final String name, final String symbol, final Authority supplyAuthority) {
        throw Unbound.error("NftService", "NftFactory");
    }

    /// @throws ServiceException if a service error occurs
    public NftMetadata createNftType(final String name, final String symbol, final AccountId treasuryAccount, final Authority supplyAuthority) {
        throw Unbound.error("NftService", "NftFactory");
    }

    /// Create a new NFT type with the given parameters: the general form of the methods above, for every option of
    /// a new NFT type.
    ///
    /// @throws ServiceException if a service error occurs
    public NftMetadata createNftType(final NftTypeCreateParams params) {
        throw Unbound.error("NftService", "NftFactory");
    }

    /// Associate an account with one or more NFT types so it can hold them
    ///
    /// @throws ServiceException if a service error occurs
    public void associateNft(final AccountId accountId, final Address... tokenIds) {
        throw Unbound.error("NftService", "NftFactory");
    }

    /// Remove the association between an account and one or more NFT types
    ///
    /// @throws ServiceException if a service error occurs
    public void dissociateNft(final AccountId accountId, final Address... tokenIds) {
        throw Unbound.error("NftService", "NftFactory");
    }

    /// Mint a single NFT with the given metadata; returns the new NFT including its serial number
    ///
    /// @throws ServiceException if a service error occurs
    public Nft mintNft(final Address tokenId, final byte[] metadata) {
        throw Unbound.error("NftService", "NftFactory");
    }

    /// Mint multiple NFTs in one operation; returns the new NFTs in the order of the given metadata
    ///
    /// @throws ServiceException if a service error occurs
    public List<Nft> mintNfts(final Address tokenId, final byte[]... metadata) {
        throw Unbound.error("NftService", "NftFactory");
    }

    /// Burn a single NFT by serial number
    ///
    /// @throws ServiceException if a service error occurs
    public void burnNft(final Address tokenId, final long serialNumber) {
        throw Unbound.error("NftService", "NftFactory");
    }

    /// Burn the given NFT
    ///
    /// @throws ServiceException if a service error occurs
    public void burnNft(final Nft nft) {
        throw Unbound.error("NftService", "NftFactory");
    }

    /// Burn multiple NFTs by serial number
    ///
    /// @throws ServiceException if a service error occurs
    public void burnNfts(final Address tokenId, final Set<Long> serialNumbers) {
        throw Unbound.error("NftService", "NftFactory");
    }

    /// Burn the given NFTs
    ///
    /// @throws ServiceException if a service error occurs
    public void burnNfts(final Set<Nft> nfts) {
        throw Unbound.error("NftService", "NftFactory");
    }

    /// Transfer a single NFT to another account
    ///
    /// @throws ServiceException if a service error occurs
    public void transferNft(final Address tokenId, final long serialNumber, final AccountId fromAccountId, final AccountId toAccountId) {
        throw Unbound.error("NftService", "NftFactory");
    }

    /// @throws ServiceException if a service error occurs
    public void transferNfts(final Nft nft, final AccountId fromAccountId, final AccountId toAccountId) {
        throw Unbound.error("NftService", "NftFactory");
    }

    /// Transfer multiple NFTs of the same type to another account
    ///
    /// @throws ServiceException if a service error occurs
    public void transferNfts(final Address tokenId, final List<Long> serialNumbers, final AccountId fromAccountId, final AccountId toAccountId) {
        throw Unbound.error("NftService", "NftFactory");
    }

    /// @throws ServiceException if a service error occurs
    public void transferNfts(final Set<Nft> nfts, final AccountId fromAccountId, final AccountId toAccountId) {
        throw Unbound.error("NftService", "NftFactory");
    }

    /// Return all known NFT types (collections)
    ///
    /// @throws ServiceException if a service error occurs
    public Page<NftMetadata> findAllTypes() {
        throw Unbound.error("NftService", "NftFactory");
    }

    /// Return all NFT types that the given account holds at least one NFT of
    ///
    /// @throws ServiceException if a service error occurs
    public Page<NftMetadata> findTypesByOwner(final AccountId ownerId) {
        throw Unbound.error("NftService", "NftFactory");
    }

    /// Return the metadata of a single NFT type
    ///
    /// @throws ServiceException if a service error occurs
    public @Nullable NftMetadata findTypeById(final Address tokenId) {
        throw Unbound.error("NftService", "NftFactory");
    }

    /// Return all NFTs owned by the given account
    ///
    /// @throws ServiceException if a service error occurs
    public Page<Nft> findByOwner(final AccountId ownerId) {
        throw Unbound.error("NftService", "NftFactory");
    }

    /// Return all NFTs of the given type
    ///
    /// @throws ServiceException if a service error occurs
    public Page<Nft> findByType(final Address tokenId) {
        throw Unbound.error("NftService", "NftFactory");
    }

    /// Return a single NFT identified by type and serial number
    ///
    /// @throws ServiceException if a service error occurs
    public @Nullable Nft findByTypeAndSerial(final Address tokenId, final long serialNumber) {
        throw Unbound.error("NftService", "NftFactory");
    }

    /// Return all NFTs of the given type that are owned by the given account
    ///
    /// @throws ServiceException if a service error occurs
    public Page<Nft> findByOwnerAndType(final AccountId ownerId, final Address tokenId) {
        throw Unbound.error("NftService", "NftFactory");
    }

    /// Return a single NFT identified by owner, type, and serial number
    ///
    /// @throws ServiceException if a service error occurs
    public @Nullable Nft findByOwnerAndTypeAndSerial(final AccountId ownerId, final Address tokenId, final long serialNumber) {
        throw Unbound.error("NftService", "NftFactory");
    }
}
