package org.hiero.consensusnode.client.internal;

import com.google.protobuf.ByteString;
import com.google.protobuf.BytesValue;
import com.google.protobuf.StringValue;
import com.google.protobuf.UInt32Value;
import com.hederahashgraph.api.proto.java.AccountAmount;
import com.hederahashgraph.api.proto.java.NftID;
import com.hederahashgraph.api.proto.java.PendingAirdropId;
import com.hederahashgraph.api.proto.java.TokenAirdropTransactionBody;
import com.hederahashgraph.api.proto.java.TokenAssociateTransactionBody;
import com.hederahashgraph.api.proto.java.TokenBurnTransactionBody;
import com.hederahashgraph.api.proto.java.TokenCancelAirdropTransactionBody;
import com.hederahashgraph.api.proto.java.TokenClaimAirdropTransactionBody;
import com.hederahashgraph.api.proto.java.TokenCreateTransactionBody;
import com.hederahashgraph.api.proto.java.TokenDeleteTransactionBody;
import com.hederahashgraph.api.proto.java.TokenDissociateTransactionBody;
import com.hederahashgraph.api.proto.java.TokenFreezeAccountTransactionBody;
import com.hederahashgraph.api.proto.java.TokenGrantKycTransactionBody;
import com.hederahashgraph.api.proto.java.TokenID;
import com.hederahashgraph.api.proto.java.TokenMintTransactionBody;
import com.hederahashgraph.api.proto.java.TokenPauseTransactionBody;
import com.hederahashgraph.api.proto.java.TokenRejectTransactionBody;
import com.hederahashgraph.api.proto.java.TokenRevokeKycTransactionBody;
import com.hederahashgraph.api.proto.java.TokenTransferList;
import com.hederahashgraph.api.proto.java.TokenUnfreezeAccountTransactionBody;
import com.hederahashgraph.api.proto.java.TokenUnpauseTransactionBody;
import com.hederahashgraph.api.proto.java.TokenUpdateNftsTransactionBody;
import com.hederahashgraph.api.proto.java.TokenUpdateTransactionBody;
import com.hederahashgraph.api.proto.java.TokenWipeAccountTransactionBody;
import com.hederahashgraph.api.proto.java.TransactionBody;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.hiero.authority.Authority;
import org.hiero.consensusnode.transactions.accounts.NftTransfer;
import org.hiero.consensusnode.transactions.accounts.TokenTransfer;
import org.hiero.consensusnode.transactions.tokens.PendingAirdrop;
import org.hiero.consensusnode.transactions.tokens.TokenAirdropReceipt;
import org.hiero.consensusnode.transactions.tokens.TokenAirdropTransaction;
import org.hiero.consensusnode.transactions.tokens.TokenAssociateReceipt;
import org.hiero.consensusnode.transactions.tokens.TokenAssociateTransaction;
import org.hiero.consensusnode.transactions.tokens.TokenBurnFungibleTransaction;
import org.hiero.consensusnode.transactions.tokens.TokenBurnNftTransaction;
import org.hiero.consensusnode.transactions.tokens.TokenBurnReceipt;
import org.hiero.consensusnode.transactions.tokens.TokenCancelAirdropReceipt;
import org.hiero.consensusnode.transactions.tokens.TokenCancelAirdropTransaction;
import org.hiero.consensusnode.transactions.tokens.TokenClaimAirdropReceipt;
import org.hiero.consensusnode.transactions.tokens.TokenClaimAirdropTransaction;
import org.hiero.consensusnode.transactions.tokens.TokenCreateReceipt;
import org.hiero.consensusnode.transactions.tokens.TokenCreateTransaction;
import org.hiero.consensusnode.transactions.tokens.TokenDeleteReceipt;
import org.hiero.consensusnode.transactions.tokens.TokenDeleteTransaction;
import org.hiero.consensusnode.transactions.tokens.TokenDissociateReceipt;
import org.hiero.consensusnode.transactions.tokens.TokenDissociateTransaction;
import org.hiero.consensusnode.transactions.tokens.TokenFreezeReceipt;
import org.hiero.consensusnode.transactions.tokens.TokenFreezeTransaction;
import org.hiero.consensusnode.transactions.tokens.TokenGrantKycReceipt;
import org.hiero.consensusnode.transactions.tokens.TokenGrantKycTransaction;
import org.hiero.consensusnode.transactions.tokens.TokenMintFungibleTransaction;
import org.hiero.consensusnode.transactions.tokens.TokenMintNftReceipt;
import org.hiero.consensusnode.transactions.tokens.TokenMintNftTransaction;
import org.hiero.consensusnode.transactions.tokens.TokenMintReceipt;
import org.hiero.consensusnode.transactions.tokens.TokenPauseReceipt;
import org.hiero.consensusnode.transactions.tokens.TokenPauseTransaction;
import org.hiero.consensusnode.transactions.tokens.TokenReference;
import org.hiero.consensusnode.transactions.tokens.TokenRejectReceipt;
import org.hiero.consensusnode.transactions.tokens.TokenRejectTransaction;
import org.hiero.consensusnode.transactions.tokens.TokenRevokeKycReceipt;
import org.hiero.consensusnode.transactions.tokens.TokenRevokeKycTransaction;
import org.hiero.consensusnode.transactions.tokens.TokenUnfreezeReceipt;
import org.hiero.consensusnode.transactions.tokens.TokenUnfreezeTransaction;
import org.hiero.consensusnode.transactions.tokens.TokenUnpauseReceipt;
import org.hiero.consensusnode.transactions.tokens.TokenUnpauseTransaction;
import org.hiero.consensusnode.transactions.tokens.TokenUpdateNftsReceipt;
import org.hiero.consensusnode.transactions.tokens.TokenUpdateNftsTransaction;
import org.hiero.consensusnode.transactions.tokens.TokenUpdateReceipt;
import org.hiero.consensusnode.transactions.tokens.TokenUpdateTransaction;
import org.hiero.consensusnode.transactions.tokens.TokenWipeFungibleTransaction;
import org.hiero.consensusnode.transactions.tokens.TokenWipeNftTransaction;
import org.hiero.consensusnode.transactions.tokens.TokenWipeReceipt;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.token.TokenSupplyType;
import org.hiero.token.TokenType;

/// The transaction types of tokens.
public final class TokenKinds {

    /// HAPI requires an auto-renew period or an expiration time for a new token; the auto-renew period when the
    /// transaction sets neither (as the Hiero SDKs do).
    private static final Duration DEFAULT_AUTO_RENEW_PERIOD = Duration.ofDays(90);

    /// `TokenCreateTransaction`: `TokenService.createToken`.
    public static final TransactionKind<TokenCreateReceipt, TokenCreateTransaction> TOKEN_CREATE = new TransactionKind<>(
            TokenCreateTransaction.class, "proto.TokenService", "createToken", TransactionBody.DataCase.TOKENCREATION,
            TokenKinds::tokenCreate,
            (id, status, rate, nextRate, receipt) -> new TokenCreateReceipt(id, status, rate, nextRate,
                    receipt.hasTokenID() ? Protobuf.fromProto(receipt.getTokenID()) : new Address(0L, 0L, "", 0L)));

    /// `TokenUpdateTransaction`: `TokenService.updateToken`.
    public static final TransactionKind<TokenUpdateReceipt, TokenUpdateTransaction> TOKEN_UPDATE = new TransactionKind<>(
            TokenUpdateTransaction.class, "proto.TokenService", "updateToken", TransactionBody.DataCase.TOKENUPDATE,
            TokenKinds::tokenUpdate,
            (id, status, rate, nextRate, receipt) -> new TokenUpdateReceipt(id, status, rate, nextRate));

    /// `TokenDeleteTransaction`: `TokenService.deleteToken`.
    public static final TransactionKind<TokenDeleteReceipt, TokenDeleteTransaction> TOKEN_DELETE = new TransactionKind<>(
            TokenDeleteTransaction.class, "proto.TokenService", "deleteToken", TransactionBody.DataCase.TOKENDELETION,
            (transaction, body) -> body.setTokenDeletion(TokenDeleteTransactionBody.newBuilder()
                    .setToken(Protobuf.toTokenId(transaction.tokenId()))),
            (id, status, rate, nextRate, receipt) -> new TokenDeleteReceipt(id, status, rate, nextRate));

    /// `TokenMintFungibleTransaction`: `TokenService.mintToken`. A mint without metadata.
    public static final TransactionKind<TokenMintReceipt, TokenMintFungibleTransaction> TOKEN_MINT_FUNGIBLE = new TransactionKind<>(
            TokenMintFungibleTransaction.class, "proto.TokenService", "mintToken", TransactionBody.DataCase.TOKENMINT,
            (transaction, body) -> body.setTokenMint(TokenMintTransactionBody.newBuilder()
                    .setToken(Protobuf.toTokenId(transaction.tokenId()))
                    .setAmount(transaction.amount())),
            (id, status, rate, nextRate, receipt) -> new TokenMintReceipt(id, status, rate, nextRate,
                    receipt.getNewTotalSupply()),
            body -> body.getTokenMint().getMetadataCount() == 0);

    /// `TokenMintNftTransaction`: `TokenService.mintToken`. A mint with metadata.
    public static final TransactionKind<TokenMintNftReceipt, TokenMintNftTransaction> TOKEN_MINT_NFT = new TransactionKind<>(
            TokenMintNftTransaction.class, "proto.TokenService", "mintToken", TransactionBody.DataCase.TOKENMINT,
            (transaction, body) -> {
                final TokenMintTransactionBody.Builder mint = TokenMintTransactionBody.newBuilder()
                        .setToken(Protobuf.toTokenId(transaction.tokenId()));
                transaction.metadata().forEach(metadata -> mint.addMetadata(ByteString.copyFrom(metadata)));
                body.setTokenMint(mint);
            },
            (id, status, rate, nextRate, receipt) -> new TokenMintNftReceipt(id, status, rate, nextRate,
                    receipt.getNewTotalSupply(), receipt.getSerialNumbersList()),
            body -> body.getTokenMint().getMetadataCount() > 0);

    /// `TokenBurnFungibleTransaction`: `TokenService.burnToken`. A burn without serial numbers.
    public static final TransactionKind<TokenBurnReceipt, TokenBurnFungibleTransaction> TOKEN_BURN_FUNGIBLE = new TransactionKind<>(
            TokenBurnFungibleTransaction.class, "proto.TokenService", "burnToken", TransactionBody.DataCase.TOKENBURN,
            (transaction, body) -> body.setTokenBurn(TokenBurnTransactionBody.newBuilder()
                    .setToken(Protobuf.toTokenId(transaction.tokenId()))
                    .setAmount(transaction.amount())),
            (id, status, rate, nextRate, receipt) -> new TokenBurnReceipt(id, status, rate, nextRate,
                    receipt.getNewTotalSupply()),
            body -> body.getTokenBurn().getSerialNumbersCount() == 0);

    /// `TokenBurnNftTransaction`: `TokenService.burnToken`. A burn with serial numbers.
    public static final TransactionKind<TokenBurnReceipt, TokenBurnNftTransaction> TOKEN_BURN_NFT = new TransactionKind<>(
            TokenBurnNftTransaction.class, "proto.TokenService", "burnToken", TransactionBody.DataCase.TOKENBURN,
            (transaction, body) -> body.setTokenBurn(TokenBurnTransactionBody.newBuilder()
                    .setToken(Protobuf.toTokenId(transaction.tokenId()))
                    .addAllSerialNumbers(transaction.serials())),
            (id, status, rate, nextRate, receipt) -> new TokenBurnReceipt(id, status, rate, nextRate,
                    receipt.getNewTotalSupply()),
            body -> body.getTokenBurn().getSerialNumbersCount() > 0);

    /// `TokenWipeFungibleTransaction`: `TokenService.wipeTokenAccount`. A wipe without serial numbers.
    public static final TransactionKind<TokenWipeReceipt, TokenWipeFungibleTransaction> TOKEN_WIPE_FUNGIBLE = new TransactionKind<>(
            TokenWipeFungibleTransaction.class, "proto.TokenService", "wipeTokenAccount", TransactionBody.DataCase.TOKENWIPE,
            (transaction, body) -> body.setTokenWipe(TokenWipeAccountTransactionBody.newBuilder()
                    .setToken(Protobuf.toTokenId(transaction.tokenId()))
                    .setAccount(Protobuf.toProto(transaction.accountId()))
                    .setAmount(transaction.amount())),
            (id, status, rate, nextRate, receipt) -> new TokenWipeReceipt(id, status, rate, nextRate,
                    receipt.getNewTotalSupply()),
            body -> body.getTokenWipe().getSerialNumbersCount() == 0);

    /// `TokenWipeNftTransaction`: `TokenService.wipeTokenAccount`. A wipe with serial numbers.
    public static final TransactionKind<TokenWipeReceipt, TokenWipeNftTransaction> TOKEN_WIPE_NFT = new TransactionKind<>(
            TokenWipeNftTransaction.class, "proto.TokenService", "wipeTokenAccount", TransactionBody.DataCase.TOKENWIPE,
            (transaction, body) -> body.setTokenWipe(TokenWipeAccountTransactionBody.newBuilder()
                    .setToken(Protobuf.toTokenId(transaction.tokenId()))
                    .setAccount(Protobuf.toProto(transaction.accountId()))
                    .addAllSerialNumbers(transaction.serials())),
            (id, status, rate, nextRate, receipt) -> new TokenWipeReceipt(id, status, rate, nextRate,
                    receipt.getNewTotalSupply()),
            body -> body.getTokenWipe().getSerialNumbersCount() > 0);

    /// `TokenFreezeTransaction`: `TokenService.freezeTokenAccount`.
    public static final TransactionKind<TokenFreezeReceipt, TokenFreezeTransaction> TOKEN_FREEZE = new TransactionKind<>(
            TokenFreezeTransaction.class, "proto.TokenService", "freezeTokenAccount", TransactionBody.DataCase.TOKENFREEZE,
            (transaction, body) -> body.setTokenFreeze(TokenFreezeAccountTransactionBody.newBuilder()
                    .setToken(Protobuf.toTokenId(transaction.tokenId()))
                    .setAccount(Protobuf.toProto(transaction.accountId()))),
            (id, status, rate, nextRate, receipt) -> new TokenFreezeReceipt(id, status, rate, nextRate));

    /// `TokenUnfreezeTransaction`: `TokenService.unfreezeTokenAccount`.
    public static final TransactionKind<TokenUnfreezeReceipt, TokenUnfreezeTransaction> TOKEN_UNFREEZE = new TransactionKind<>(
            TokenUnfreezeTransaction.class, "proto.TokenService", "unfreezeTokenAccount", TransactionBody.DataCase.TOKENUNFREEZE,
            (transaction, body) -> body.setTokenUnfreeze(TokenUnfreezeAccountTransactionBody.newBuilder()
                    .setToken(Protobuf.toTokenId(transaction.tokenId()))
                    .setAccount(Protobuf.toProto(transaction.accountId()))),
            (id, status, rate, nextRate, receipt) -> new TokenUnfreezeReceipt(id, status, rate, nextRate));

    /// `TokenGrantKycTransaction`: `TokenService.grantKycToTokenAccount`.
    public static final TransactionKind<TokenGrantKycReceipt, TokenGrantKycTransaction> TOKEN_GRANT_KYC = new TransactionKind<>(
            TokenGrantKycTransaction.class, "proto.TokenService", "grantKycToTokenAccount", TransactionBody.DataCase.TOKENGRANTKYC,
            (transaction, body) -> body.setTokenGrantKyc(TokenGrantKycTransactionBody.newBuilder()
                    .setToken(Protobuf.toTokenId(transaction.tokenId()))
                    .setAccount(Protobuf.toProto(transaction.accountId()))),
            (id, status, rate, nextRate, receipt) -> new TokenGrantKycReceipt(id, status, rate, nextRate));

    /// `TokenRevokeKycTransaction`: `TokenService.revokeKycFromTokenAccount`.
    public static final TransactionKind<TokenRevokeKycReceipt, TokenRevokeKycTransaction> TOKEN_REVOKE_KYC = new TransactionKind<>(
            TokenRevokeKycTransaction.class, "proto.TokenService", "revokeKycFromTokenAccount", TransactionBody.DataCase.TOKENREVOKEKYC,
            (transaction, body) -> body.setTokenRevokeKyc(TokenRevokeKycTransactionBody.newBuilder()
                    .setToken(Protobuf.toTokenId(transaction.tokenId()))
                    .setAccount(Protobuf.toProto(transaction.accountId()))),
            (id, status, rate, nextRate, receipt) -> new TokenRevokeKycReceipt(id, status, rate, nextRate));

    /// `TokenAssociateTransaction`: `TokenService.associateTokens`.
    public static final TransactionKind<TokenAssociateReceipt, TokenAssociateTransaction> TOKEN_ASSOCIATE = new TransactionKind<>(
            TokenAssociateTransaction.class, "proto.TokenService", "associateTokens", TransactionBody.DataCase.TOKENASSOCIATE,
            (transaction, body) -> body.setTokenAssociate(TokenAssociateTransactionBody.newBuilder()
                    .setAccount(Protobuf.toProto(transaction.accountId()))
                    .addAllTokens(transaction.tokens().stream().map(Protobuf::toTokenId).toList())),
            (id, status, rate, nextRate, receipt) -> new TokenAssociateReceipt(id, status, rate, nextRate));

    /// `TokenDissociateTransaction`: `TokenService.dissociateTokens`.
    public static final TransactionKind<TokenDissociateReceipt, TokenDissociateTransaction> TOKEN_DISSOCIATE = new TransactionKind<>(
            TokenDissociateTransaction.class, "proto.TokenService", "dissociateTokens", TransactionBody.DataCase.TOKENDISSOCIATE,
            (transaction, body) -> body.setTokenDissociate(TokenDissociateTransactionBody.newBuilder()
                    .setAccount(Protobuf.toProto(transaction.accountId()))
                    .addAllTokens(transaction.tokens().stream().map(Protobuf::toTokenId).toList())),
            (id, status, rate, nextRate, receipt) -> new TokenDissociateReceipt(id, status, rate, nextRate));

    /// `TokenPauseTransaction`: `TokenService.pauseToken`.
    public static final TransactionKind<TokenPauseReceipt, TokenPauseTransaction> TOKEN_PAUSE = new TransactionKind<>(
            TokenPauseTransaction.class, "proto.TokenService", "pauseToken", TransactionBody.DataCase.TOKEN_PAUSE,
            (transaction, body) -> body.setTokenPause(TokenPauseTransactionBody.newBuilder()
                    .setToken(Protobuf.toTokenId(transaction.tokenId()))),
            (id, status, rate, nextRate, receipt) -> new TokenPauseReceipt(id, status, rate, nextRate));

    /// `TokenUnpauseTransaction`: `TokenService.unpauseToken`.
    public static final TransactionKind<TokenUnpauseReceipt, TokenUnpauseTransaction> TOKEN_UNPAUSE = new TransactionKind<>(
            TokenUnpauseTransaction.class, "proto.TokenService", "unpauseToken", TransactionBody.DataCase.TOKEN_UNPAUSE,
            (transaction, body) -> body.setTokenUnpause(TokenUnpauseTransactionBody.newBuilder()
                    .setToken(Protobuf.toTokenId(transaction.tokenId()))),
            (id, status, rate, nextRate, receipt) -> new TokenUnpauseReceipt(id, status, rate, nextRate));

    /// `TokenUpdateNftsTransaction`: `TokenService.updateNfts`.
    public static final TransactionKind<TokenUpdateNftsReceipt, TokenUpdateNftsTransaction> TOKEN_UPDATE_NFTS = new TransactionKind<>(
            TokenUpdateNftsTransaction.class, "proto.TokenService", "updateNfts", TransactionBody.DataCase.TOKEN_UPDATE_NFTS,
            (transaction, body) -> {
                final TokenUpdateNftsTransactionBody.Builder update = TokenUpdateNftsTransactionBody.newBuilder()
                        .setToken(Protobuf.toTokenId(transaction.tokenId()))
                        .addAllSerialNumbers(transaction.serials());
                final byte[] metadata = transaction.metadata();
                if (metadata != null) {
                    update.setMetadata(BytesValue.of(ByteString.copyFrom(metadata)));
                }
                body.setTokenUpdateNfts(update);
            },
            (id, status, rate, nextRate, receipt) -> new TokenUpdateNftsReceipt(id, status, rate, nextRate));

    /// `TokenRejectTransaction`: `TokenService.rejectToken`.
    public static final TransactionKind<TokenRejectReceipt, TokenRejectTransaction> TOKEN_REJECT = new TransactionKind<>(
            TokenRejectTransaction.class, "proto.TokenService", "rejectToken", TransactionBody.DataCase.TOKENREJECT,
            (transaction, body) -> {
                final TokenRejectTransactionBody.Builder reject = TokenRejectTransactionBody.newBuilder();
                final AccountId owner = transaction.owner();
                if (owner != null) {
                    reject.setOwner(Protobuf.toProto(owner));
                }
                for (final TokenReference rejection : transaction.rejections()) {
                    final Long serial = rejection.serial();
                    reject.addRejections(serial == null
                            ? com.hederahashgraph.api.proto.java.TokenReference.newBuilder()
                            .setFungibleToken(Protobuf.toTokenId(rejection.tokenId()))
                            : com.hederahashgraph.api.proto.java.TokenReference.newBuilder()
                            .setNft(nftId(rejection.tokenId(), serial)));
                }
                body.setTokenReject(reject);
            },
            (id, status, rate, nextRate, receipt) -> new TokenRejectReceipt(id, status, rate, nextRate));

    /// `TokenAirdropTransaction`: `TokenService.airdropTokens`.
    public static final TransactionKind<TokenAirdropReceipt, TokenAirdropTransaction> TOKEN_AIRDROP = new TransactionKind<>(
            TokenAirdropTransaction.class, "proto.TokenService", "airdropTokens", TransactionBody.DataCase.TOKENAIRDROP,
            (transaction, body) -> body.setTokenAirdrop(TokenAirdropTransactionBody.newBuilder()
                    .addAllTokenTransfers(tokenTransferLists(transaction.tokenTransfers(),
                            transaction.nftTransfers()))),
            (id, status, rate, nextRate, receipt) -> new TokenAirdropReceipt(id, status, rate, nextRate));

    /// `TokenCancelAirdropTransaction`: `TokenService.cancelAirdrop`.
    public static final TransactionKind<TokenCancelAirdropReceipt, TokenCancelAirdropTransaction> TOKEN_CANCEL_AIRDROP = new TransactionKind<>(
            TokenCancelAirdropTransaction.class, "proto.TokenService", "cancelAirdrop", TransactionBody.DataCase.TOKENCANCELAIRDROP,
            (transaction, body) -> body.setTokenCancelAirdrop(TokenCancelAirdropTransactionBody.newBuilder()
                    .addAllPendingAirdrops(transaction.pendingAirdrops().stream().map(TokenKinds::pendingAirdropId)
                            .toList())),
            (id, status, rate, nextRate, receipt) -> new TokenCancelAirdropReceipt(id, status, rate, nextRate));

    /// `TokenClaimAirdropTransaction`: `TokenService.claimAirdrop`.
    public static final TransactionKind<TokenClaimAirdropReceipt, TokenClaimAirdropTransaction> TOKEN_CLAIM_AIRDROP = new TransactionKind<>(
            TokenClaimAirdropTransaction.class, "proto.TokenService", "claimAirdrop", TransactionBody.DataCase.TOKENCLAIMAIRDROP,
            (transaction, body) -> body.setTokenClaimAirdrop(TokenClaimAirdropTransactionBody.newBuilder()
                    .addAllPendingAirdrops(transaction.pendingAirdrops().stream().map(TokenKinds::pendingAirdropId)
                            .toList())),
            (id, status, rate, nextRate, receipt) -> new TokenClaimAirdropReceipt(id, status, rate, nextRate));

    /// All transaction types of this class.
    public static final List<TransactionKind<?, ?>> ALL = List.of(TOKEN_CREATE, TOKEN_UPDATE, TOKEN_DELETE, TOKEN_MINT_FUNGIBLE, TOKEN_MINT_NFT, TOKEN_BURN_FUNGIBLE, TOKEN_BURN_NFT, TOKEN_WIPE_FUNGIBLE, TOKEN_WIPE_NFT, TOKEN_FREEZE, TOKEN_UNFREEZE, TOKEN_GRANT_KYC, TOKEN_REVOKE_KYC, TOKEN_ASSOCIATE, TOKEN_DISSOCIATE, TOKEN_PAUSE, TOKEN_UNPAUSE, TOKEN_UPDATE_NFTS, TOKEN_REJECT, TOKEN_AIRDROP, TOKEN_CANCEL_AIRDROP, TOKEN_CLAIM_AIRDROP);

    private TokenKinds() {
    }

    private static void tokenCreate(final TokenCreateTransaction transaction, final TransactionBody.Builder body) {
        final TokenCreateTransactionBody.Builder create = TokenCreateTransactionBody.newBuilder()
                .setName(transaction.name())
                .setSymbol(transaction.symbol())
                .setDecimals(transaction.decimals())
                .setInitialSupply(transaction.initialSupply())
                .setTreasury(Protobuf.toProto(transaction.treasuryAccountId()))
                .setFreezeDefault(transaction.freezeDefault())
                .setTokenType(tokenType(transaction.tokenType()))
                .setSupplyType(supplyType(transaction.supplyType()))
                .setMetadata(ByteString.copyFrom(transaction.metadata()));
        final Long maxSupply = transaction.maxSupply();
        if (maxSupply != null) {
            create.setMaxSupply(maxSupply);
        }
        final String memo = transaction.tokenMemo();
        if (memo != null) {
            create.setMemo(memo);
        }
        final ZonedDateTime expirationTime = transaction.expirationTime();
        if (expirationTime != null) {
            create.setExpiry(Protobuf.toProto(expirationTime));
        }
        // As the Hiero SDKs: without an expiration time the token renews itself every 90 days, and without an
        // auto-renew account the payer pays for the renewal.
        final Duration requestedAutoRenewPeriod = transaction.autoRenewPeriod();
        final Duration autoRenewPeriod = requestedAutoRenewPeriod != null ? requestedAutoRenewPeriod
                : expirationTime == null ? DEFAULT_AUTO_RENEW_PERIOD : null;
        if (autoRenewPeriod != null) {
            create.setAutoRenewPeriod(Protobuf.toProto(autoRenewPeriod));
        }
        final AccountId autoRenewAccount = transaction.autoRenewAccount();
        if (autoRenewAccount != null) {
            create.setAutoRenewAccount(Protobuf.toProto(autoRenewAccount));
        } else if (autoRenewPeriod != null && !autoRenewPeriod.isZero() && body.hasTransactionID()) {
            create.setAutoRenewAccount(body.getTransactionID().getAccountID());
        }
        final Authority admin = transaction.adminAuthority();
        if (admin != null) {
            create.setAdminKey(Protobuf.toProto(admin));
        }
        final Authority supply = transaction.supplyAuthority();
        if (supply != null) {
            create.setSupplyKey(Protobuf.toProto(supply));
        }
        final Authority kyc = transaction.kycAuthority();
        if (kyc != null) {
            create.setKycKey(Protobuf.toProto(kyc));
        }
        final Authority freeze = transaction.freezeAuthority();
        if (freeze != null) {
            create.setFreezeKey(Protobuf.toProto(freeze));
        }
        final Authority wipe = transaction.wipeAuthority();
        if (wipe != null) {
            create.setWipeKey(Protobuf.toProto(wipe));
        }
        final Authority pause = transaction.pauseAuthority();
        if (pause != null) {
            create.setPauseKey(Protobuf.toProto(pause));
        }
        final Authority feeSchedule = transaction.feeScheduleAuthority();
        if (feeSchedule != null) {
            create.setFeeScheduleKey(Protobuf.toProto(feeSchedule));
        }
        final Authority metadata = transaction.metadataAuthority();
        if (metadata != null) {
            create.setMetadataKey(Protobuf.toProto(metadata));
        }
        body.setTokenCreation(create);
    }

    private static void tokenUpdate(final TokenUpdateTransaction transaction, final TransactionBody.Builder body) {
        final TokenUpdateTransactionBody.Builder update = TokenUpdateTransactionBody.newBuilder()
                .setToken(Protobuf.toTokenId(transaction.tokenId()));
        final String name = transaction.name();
        if (name != null) {
            update.setName(name);
        }
        final String symbol = transaction.symbol();
        if (symbol != null) {
            update.setSymbol(symbol);
        }
        final AccountId treasury = transaction.treasuryAccountId();
        if (treasury != null) {
            update.setTreasury(Protobuf.toProto(treasury));
        }
        final String memo = transaction.tokenMemo();
        if (memo != null) {
            update.setMemo(StringValue.of(memo));
        }
        final byte[] metadata = transaction.metadata();
        if (metadata != null) {
            update.setMetadata(BytesValue.of(ByteString.copyFrom(metadata)));
        }
        final ZonedDateTime expirationTime = transaction.expirationTime();
        if (expirationTime != null) {
            update.setExpiry(Protobuf.toProto(expirationTime));
        }
        final Duration autoRenewPeriod = transaction.autoRenewPeriod();
        if (autoRenewPeriod != null) {
            update.setAutoRenewPeriod(Protobuf.toProto(autoRenewPeriod));
        }
        final AccountId autoRenewAccount = transaction.autoRenewAccount();
        if (autoRenewAccount != null) {
            update.setAutoRenewAccount(Protobuf.toProto(autoRenewAccount));
        }
        final Authority admin = transaction.adminAuthority();
        if (admin != null) {
            update.setAdminKey(Protobuf.toProto(admin));
        }
        final Authority supply = transaction.supplyAuthority();
        if (supply != null) {
            update.setSupplyKey(Protobuf.toProto(supply));
        }
        final Authority kyc = transaction.kycAuthority();
        if (kyc != null) {
            update.setKycKey(Protobuf.toProto(kyc));
        }
        final Authority freeze = transaction.freezeAuthority();
        if (freeze != null) {
            update.setFreezeKey(Protobuf.toProto(freeze));
        }
        final Authority wipe = transaction.wipeAuthority();
        if (wipe != null) {
            update.setWipeKey(Protobuf.toProto(wipe));
        }
        final Authority pause = transaction.pauseAuthority();
        if (pause != null) {
            update.setPauseKey(Protobuf.toProto(pause));
        }
        final Authority feeSchedule = transaction.feeScheduleAuthority();
        if (feeSchedule != null) {
            update.setFeeScheduleKey(Protobuf.toProto(feeSchedule));
        }
        final Authority metadataAuthority = transaction.metadataAuthority();
        if (metadataAuthority != null) {
            update.setMetadataKey(Protobuf.toProto(metadataAuthority));
        }
        body.setTokenUpdate(update);
    }

    private static com.hederahashgraph.api.proto.java.TokenType tokenType(final TokenType type) {
        return switch (type) {
            case FUNGIBLE_COMMON -> com.hederahashgraph.api.proto.java.TokenType.FUNGIBLE_COMMON;
            case NON_FUNGIBLE_UNIQUE -> com.hederahashgraph.api.proto.java.TokenType.NON_FUNGIBLE_UNIQUE;
        };
    }

    private static com.hederahashgraph.api.proto.java.TokenSupplyType supplyType(final TokenSupplyType type) {
        return switch (type) {
            case INFINITE -> com.hederahashgraph.api.proto.java.TokenSupplyType.INFINITE;
            case FINITE -> com.hederahashgraph.api.proto.java.TokenSupplyType.FINITE;
        };
    }

    /// Maps an NFT, i.e. a serial of a token.
    ///
    /// @param tokenId the token
    /// @param serial  the serial number
    /// @return the message
    static NftID nftId(final Address tokenId, final long serial) {
        return NftID.newBuilder().setTokenID(Protobuf.toTokenId(tokenId)).setSerialNumber(serial).build();
    }

    private static PendingAirdropId pendingAirdropId(final PendingAirdrop airdrop) {
        final PendingAirdropId.Builder id = PendingAirdropId.newBuilder()
                .setSenderId(Protobuf.toProto(airdrop.senderId()))
                .setReceiverId(Protobuf.toProto(airdrop.receiverId()));
        final Long serial = airdrop.serial();
        return serial == null ? id.setFungibleTokenType(Protobuf.toTokenId(airdrop.tokenId())).build()
                : id.setNonFungibleToken(nftId(airdrop.tokenId(), serial)).build();
    }

    /// Groups fungible and NFT transfers into one transfer list per token, in the order the tokens first appear (the
    /// shape of the token transfers of a `CryptoTransfer`).
    ///
    /// @param tokenTransfers the fungible transfers
    /// @param nftTransfers   the NFT transfers
    /// @return the transfer lists
    /// @throws IllegalArgumentException if the transfers of one token expect different decimals
    static List<TokenTransferList> tokenTransferLists(final List<TokenTransfer> tokenTransfers,
                                                      final List<NftTransfer> nftTransfers) {
        final Map<TokenID, TokenTransferList.Builder> lists = new LinkedHashMap<>();
        for (final TokenTransfer transfer : tokenTransfers) {
            final TokenID token = Protobuf.toTokenId(transfer.tokenId());
            final TokenTransferList.Builder list = lists.computeIfAbsent(token,
                    key -> TokenTransferList.newBuilder().setToken(key));
            list.addTransfers(AccountAmount.newBuilder()
                    .setAccountID(Protobuf.toProto(transfer.accountId()))
                    .setAmount(transfer.amount())
                    .setIsApproval(transfer.approved()));
            final Integer expectedDecimals = transfer.expectedDecimals();
            if (expectedDecimals != null) {
                if (list.hasExpectedDecimals() && list.getExpectedDecimals().getValue() != expectedDecimals) {
                    throw new IllegalArgumentException("The transfers of token " + transfer.tokenId()
                            + " expect different decimals");
                }
                list.setExpectedDecimals(UInt32Value.of(expectedDecimals));
            }
        }
        for (final NftTransfer transfer : nftTransfers) {
            final TokenID token = Protobuf.toTokenId(transfer.tokenId());
            lists.computeIfAbsent(token, key -> TokenTransferList.newBuilder().setToken(key))
                    .addNftTransfers(com.hederahashgraph.api.proto.java.NftTransfer.newBuilder()
                            .setSenderAccountID(Protobuf.toProto(transfer.sender()))
                            .setReceiverAccountID(Protobuf.toProto(transfer.receiver()))
                            .setSerialNumber(transfer.serial())
                            .setIsApproval(transfer.approved()));
        }
        return lists.values().stream().map(TokenTransferList.Builder::build).toList();
    }
}
