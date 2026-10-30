package org.hiero.enterprise.service.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.hiero.authority.Authority;
import org.hiero.common.Page;
import org.hiero.consensusnode.queries.tokens.TokenInfoQuery;
import org.hiero.consensusnode.transactions.accounts.NftTransfer;
import org.hiero.consensusnode.transactions.accounts.TransferTransaction;
import org.hiero.consensusnode.transactions.tokens.TokenAssociateTransaction;
import org.hiero.consensusnode.transactions.tokens.TokenBurnNftTransaction;
import org.hiero.consensusnode.transactions.tokens.TokenCreateTransaction;
import org.hiero.consensusnode.transactions.tokens.TokenDissociateTransaction;
import org.hiero.consensusnode.transactions.tokens.TokenMintNftTransaction;
import org.hiero.enterprise.service.ServiceException;
import org.hiero.enterprise.service.nft.NftService;
import org.hiero.enterprise.service.nft.NftTypeCreateParams;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.mirrornode.nft.Nft;
import org.hiero.mirrornode.nft.NftMetadata;
import org.hiero.token.TokenType;
import org.jspecify.annotations.Nullable;

/// The `NftService` of a session. The short forms of `createNftType` make the operator account the supply authority,
/// so that the service can mint and burn NFTs of the new type. A new NFT is owned by the treasury of its type.
public final class DefaultNftService extends NftService {

    /// The largest metadata of a single NFT.
    private static final int MAX_METADATA_SIZE = 100;

    private final DefaultSession session;

    /// Creates the service of a session.
    ///
    /// @param session the session
    public DefaultNftService(final DefaultSession session) {
        this.session = Objects.requireNonNull(session, "session must not be null");
    }

    @Override
    public NftMetadata createNftType(final String name, final String symbol) {
        return createNftType(name, symbol, session.operatorAuthority());
    }

    @Override
    public NftMetadata createNftType(final String name, final String symbol, final Authority supplyAuthority) {
        return createNftType(name, symbol, session.operatorAccountId(), supplyAuthority);
    }

    @Override
    public NftMetadata createNftType(final String name, final String symbol, final AccountId treasuryAccount,
            final Authority supplyAuthority) {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(symbol, "symbol must not be null");
        Objects.requireNonNull(treasuryAccount, "treasuryAccount must not be null");
        Objects.requireNonNull(supplyAuthority, "supplyAuthority must not be null");
        return createNftType(NftTypeCreateParams.builder().name(name).symbol(symbol)
                .treasuryAccountId(treasuryAccount).supplyAuthority(supplyAuthority).build());
    }

    @Override
    public NftMetadata createNftType(final NftTypeCreateParams params) {
        Objects.requireNonNull(params, "params must not be null");
        final AccountId treasury = params.treasuryAccountId() == null
                ? session.operatorAccountId() : params.treasuryAccountId();
        final Address tokenId = session.execute(new TokenCreateTransaction(params.name(), params.symbol(),
                TokenType.NON_FUNGIBLE_UNIQUE, 0, 0L, treasury, params.supplyType(), params.maxSupply(),
                params.freezeDefault(), params.tokenMemo(), params.metadata(), params.expirationTime(),
                params.autoRenewPeriod(), params.autoRenewAccount(), params.adminAuthority(),
                params.supplyAuthority(), params.kycAuthority(), params.freezeAuthority(), params.wipeAuthority(),
                params.pauseAuthority(), params.feeScheduleAuthority(), params.metadataAuthority())).tokenId();
        return new NftMetadata(tokenId, params.name(), params.symbol(), treasury);
    }

    @Override
    public void associateNft(final AccountId accountId, final Address... tokenIds) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        session.execute(new TokenAssociateTransaction(accountId, DefaultFungibleTokenService.tokens(tokenIds)));
    }

    @Override
    public void dissociateNft(final AccountId accountId, final Address... tokenIds) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        session.execute(new TokenDissociateTransaction(accountId, DefaultFungibleTokenService.tokens(tokenIds)));
    }

    @Override
    public Nft mintNft(final Address tokenId, final byte[] metadata) {
        Objects.requireNonNull(metadata, "metadata must not be null");
        if (metadata.length > MAX_METADATA_SIZE) {
            throw new IllegalArgumentException("metadata must contain at most " + MAX_METADATA_SIZE + " element(s)");
        }
        return mintNfts(tokenId, metadata).getFirst();
    }

    @Override
    public List<Nft> mintNfts(final Address tokenId, final byte[]... metadata) {
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        Objects.requireNonNull(metadata, "metadata must not be null");
        final List<byte[]> values = Arrays.asList(metadata);
        final List<Long> serials = session.execute(new TokenMintNftTransaction(tokenId, values)).serials();
        if (serials.size() != values.size()) {
            throw new ServiceException("Minting " + values.size() + " NFTs of " + tokenId + " returned "
                    + serials.size() + " serial numbers", null);
        }
        final AccountId treasury = session.query(new TokenInfoQuery(tokenId)).treasuryAccountId();
        final List<Nft> nfts = new ArrayList<>(serials.size());
        for (int i = 0; i < serials.size(); i++) {
            nfts.add(new Nft(tokenId, serials.get(i), treasury, values.get(i), null, null, null, null, false));
        }
        return List.copyOf(nfts);
    }

    @Override
    public void burnNft(final Address tokenId, final long serialNumber) {
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        session.execute(new TokenBurnNftTransaction(tokenId, List.of(serialNumber)));
    }

    @Override
    public void burnNft(final Nft nft) {
        Objects.requireNonNull(nft, "nft must not be null");
        burnNft(nft.tokenId(), nft.serialNumber());
    }

    @Override
    public void burnNfts(final Address tokenId, final Set<Long> serialNumbers) {
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        Objects.requireNonNull(serialNumbers, "serialNumbers must not be null");
        session.execute(new TokenBurnNftTransaction(tokenId, serialNumbers.stream().sorted().toList()));
    }

    @Override
    public void burnNfts(final Set<Nft> nfts) {
        Objects.requireNonNull(nfts, "nfts must not be null");
        final Map<Address, List<Long>> byType = new LinkedHashMap<>();
        nfts.forEach(nft -> byType.computeIfAbsent(nft.tokenId(), type -> new ArrayList<>()).add(nft.serialNumber()));
        byType.forEach((tokenId, serials) -> session.execute(
                new TokenBurnNftTransaction(tokenId, serials.stream().sorted().toList())));
    }

    @Override
    public void transferNft(final Address tokenId, final long serialNumber, final AccountId fromAccountId,
            final AccountId toAccountId) {
        transferNfts(tokenId, List.of(serialNumber), fromAccountId, toAccountId);
    }

    @Override
    public void transferNfts(final Nft nft, final AccountId fromAccountId, final AccountId toAccountId) {
        Objects.requireNonNull(nft, "nft must not be null");
        transferNfts(nft.tokenId(), List.of(nft.serialNumber()), fromAccountId, toAccountId);
    }

    @Override
    public void transferNfts(final Address tokenId, final List<Long> serialNumbers, final AccountId fromAccountId,
            final AccountId toAccountId) {
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        Objects.requireNonNull(serialNumbers, "serialNumbers must not be null");
        Objects.requireNonNull(fromAccountId, "fromAccountId must not be null");
        Objects.requireNonNull(toAccountId, "toAccountId must not be null");
        transfer(serialNumbers.stream().map(serial -> new NftTransfer(tokenId, serial, fromAccountId, toAccountId))
                .toList());
    }

    @Override
    public void transferNfts(final Set<Nft> nfts, final AccountId fromAccountId, final AccountId toAccountId) {
        Objects.requireNonNull(nfts, "nfts must not be null");
        Objects.requireNonNull(fromAccountId, "fromAccountId must not be null");
        Objects.requireNonNull(toAccountId, "toAccountId must not be null");
        transfer(nfts.stream().map(nft -> new NftTransfer(nft.tokenId(), nft.serialNumber(), fromAccountId, toAccountId))
                .toList());
    }

    private void transfer(final List<NftTransfer> transfers) {
        session.execute(new TransferTransaction(List.of(), List.of(), transfers));
    }

    @Override
    public Page<NftMetadata> findAllTypes() {
        return session.read(mirror -> mirror.nft().findAllTypes());
    }

    @Override
    public Page<NftMetadata> findTypesByOwner(final AccountId ownerId) {
        Objects.requireNonNull(ownerId, "ownerId must not be null");
        return session.read(mirror -> mirror.nft().findTypesByOwner(ownerId));
    }

    @Override
    public @Nullable NftMetadata findTypeById(final Address tokenId) {
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        return session.read(mirror -> mirror.nft().getNftMetadata(tokenId));
    }

    @Override
    public Page<Nft> findByOwner(final AccountId ownerId) {
        Objects.requireNonNull(ownerId, "ownerId must not be null");
        return session.read(mirror -> mirror.nft().findByOwner(ownerId));
    }

    @Override
    public Page<Nft> findByType(final Address tokenId) {
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        return session.read(mirror -> mirror.nft().findByType(tokenId));
    }

    @Override
    public @Nullable Nft findByTypeAndSerial(final Address tokenId, final long serialNumber) {
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        return session.read(mirror -> mirror.nft().findByTypeAndSerial(tokenId, serialNumber));
    }

    @Override
    public Page<Nft> findByOwnerAndType(final AccountId ownerId, final Address tokenId) {
        Objects.requireNonNull(ownerId, "ownerId must not be null");
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        return session.read(mirror -> mirror.nft().findByOwnerAndType(ownerId, tokenId));
    }

    @Override
    public @Nullable Nft findByOwnerAndTypeAndSerial(final AccountId ownerId, final Address tokenId,
            final long serialNumber) {
        Objects.requireNonNull(ownerId, "ownerId must not be null");
        final Nft nft = findByTypeAndSerial(tokenId, serialNumber);
        return nft != null && ownerId.equals(nft.accountId()) ? nft : null;
    }
}
