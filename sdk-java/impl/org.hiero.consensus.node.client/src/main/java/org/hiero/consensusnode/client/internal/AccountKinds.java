package org.hiero.consensusnode.client.internal;

import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;
import com.google.protobuf.UInt32Value;
import com.hederahashgraph.api.proto.java.AccountAmount;
import com.hederahashgraph.api.proto.java.CryptoAllowance;
import com.hederahashgraph.api.proto.java.CryptoApproveAllowanceTransactionBody;
import com.hederahashgraph.api.proto.java.CryptoCreateTransactionBody;
import com.hederahashgraph.api.proto.java.CryptoDeleteAllowanceTransactionBody;
import com.hederahashgraph.api.proto.java.CryptoDeleteTransactionBody;
import com.hederahashgraph.api.proto.java.CryptoTransferTransactionBody;
import com.hederahashgraph.api.proto.java.CryptoUpdateTransactionBody;
import com.hederahashgraph.api.proto.java.NftRemoveAllowance;
import com.hederahashgraph.api.proto.java.TokenTransferList;
import com.hederahashgraph.api.proto.java.TransactionBody;
import com.hederahashgraph.api.proto.java.TransferList;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.hiero.authority.Authority;
import org.hiero.consensusnode.transactions.accounts.AccountAllowanceApproveReceipt;
import org.hiero.consensusnode.transactions.accounts.AccountAllowanceApproveTransaction;
import org.hiero.consensusnode.transactions.accounts.AccountAllowanceDeleteReceipt;
import org.hiero.consensusnode.transactions.accounts.AccountAllowanceDeleteTransaction;
import org.hiero.consensusnode.transactions.accounts.AccountCreateReceipt;
import org.hiero.consensusnode.transactions.accounts.AccountCreateTransaction;
import org.hiero.consensusnode.transactions.accounts.AccountDeleteReceipt;
import org.hiero.consensusnode.transactions.accounts.AccountDeleteTransaction;
import org.hiero.consensusnode.transactions.accounts.AccountUpdateReceipt;
import org.hiero.consensusnode.transactions.accounts.AccountUpdateTransaction;
import org.hiero.consensusnode.transactions.accounts.HbarAllowance;
import org.hiero.consensusnode.transactions.accounts.HbarTransfer;
import org.hiero.consensusnode.transactions.accounts.NftAllowance;
import org.hiero.consensusnode.transactions.accounts.NftAllowanceDeletion;
import org.hiero.consensusnode.transactions.accounts.NftCollectionGrant;
import org.hiero.consensusnode.transactions.accounts.NftSerialGrant;
import org.hiero.consensusnode.transactions.accounts.NftTransfer;
import org.hiero.consensusnode.transactions.accounts.TokenAllowance;
import org.hiero.consensusnode.transactions.accounts.TokenTransfer;
import org.hiero.consensusnode.transactions.accounts.TransferReceipt;
import org.hiero.consensusnode.transactions.accounts.TransferTransaction;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// The transaction types of accounts, allowances and transfers.
public final class AccountKinds {

    /// HAPI requires an auto-renew period for a new account; the default when the transaction sets none.
    private static final Duration DEFAULT_AUTO_RENEW_PERIOD = Duration.ofDays(90);

    /// `AccountCreateTransaction`: `CryptoService.createAccount`.
    public static final TransactionKind<AccountCreateReceipt, AccountCreateTransaction> ACCOUNT_CREATE =
            new TransactionKind<>(AccountCreateTransaction.class, "proto.CryptoService", "createAccount",
                    TransactionBody.DataCase.CRYPTOCREATEACCOUNT, AccountKinds::accountCreate,
                    (id, status, rate, nextRate, receipt) -> new AccountCreateReceipt(id, status, rate, nextRate,
                            status == HapiTransactionStatus.SUCCESS ? Protobuf.fromProto(receipt.getAccountID())
                                    : new AccountId(0L, 0L, "", 0L, null, null)));

    /// `AccountUpdateTransaction`: `CryptoService.updateAccount`.
    public static final TransactionKind<AccountUpdateReceipt, AccountUpdateTransaction> ACCOUNT_UPDATE = new TransactionKind<>(
            AccountUpdateTransaction.class, "proto.CryptoService", "updateAccount", TransactionBody.DataCase.CRYPTOUPDATEACCOUNT,
            AccountKinds::accountUpdate,
            (id, status, rate, nextRate, receipt) -> new AccountUpdateReceipt(id, status, rate, nextRate));

    /// `AccountDeleteTransaction`: `CryptoService.cryptoDelete`.
    public static final TransactionKind<AccountDeleteReceipt, AccountDeleteTransaction> ACCOUNT_DELETE = new TransactionKind<>(
            AccountDeleteTransaction.class, "proto.CryptoService", "cryptoDelete", TransactionBody.DataCase.CRYPTODELETE,
            AccountKinds::accountDelete,
            (id, status, rate, nextRate, receipt) -> new AccountDeleteReceipt(id, status, rate, nextRate));

    /// `AccountAllowanceApproveTransaction`: `CryptoService.approveAllowances`.
    public static final TransactionKind<AccountAllowanceApproveReceipt, AccountAllowanceApproveTransaction> ACCOUNT_ALLOWANCE_APPROVE = new TransactionKind<>(
            AccountAllowanceApproveTransaction.class, "proto.CryptoService", "approveAllowances", TransactionBody.DataCase.CRYPTOAPPROVEALLOWANCE,
            AccountKinds::allowanceApprove,
            (id, status, rate, nextRate, receipt) -> new AccountAllowanceApproveReceipt(id, status, rate, nextRate));

    /// `AccountAllowanceDeleteTransaction`: `CryptoService.deleteAllowances`.
    public static final TransactionKind<AccountAllowanceDeleteReceipt, AccountAllowanceDeleteTransaction> ACCOUNT_ALLOWANCE_DELETE = new TransactionKind<>(
            AccountAllowanceDeleteTransaction.class, "proto.CryptoService", "deleteAllowances", TransactionBody.DataCase.CRYPTODELETEALLOWANCE,
            AccountKinds::allowanceDelete,
            (id, status, rate, nextRate, receipt) -> new AccountAllowanceDeleteReceipt(id, status, rate, nextRate));

    /// `TransferTransaction`: `CryptoService.cryptoTransfer`.
    public static final TransactionKind<TransferReceipt, TransferTransaction> TRANSFER = new TransactionKind<>(
            TransferTransaction.class, "proto.CryptoService", "cryptoTransfer", TransactionBody.DataCase.CRYPTOTRANSFER,
            AccountKinds::transfer,
            (id, status, rate, nextRate, receipt) -> new TransferReceipt(id, status, rate, nextRate));

    /// All transaction types of this class.
    public static final List<TransactionKind<?, ?>> ALL = List.of(ACCOUNT_CREATE, ACCOUNT_UPDATE, ACCOUNT_DELETE, ACCOUNT_ALLOWANCE_APPROVE, ACCOUNT_ALLOWANCE_DELETE, TRANSFER);

    private AccountKinds() {
    }

    private static void accountCreate(final AccountCreateTransaction transaction, final TransactionBody.Builder body) {
        final Duration autoRenewPeriod = transaction.autoRenewPeriod();
        final CryptoCreateTransactionBody.Builder create = CryptoCreateTransactionBody.newBuilder()
                .setKey(Protobuf.toProto(transaction.authority()))
                .setReceiverSigRequired(transaction.receiverSignatureRequired())
                .setDeclineReward(transaction.declineStakingReward())
                .setAutoRenewPeriod(Protobuf.toProto(autoRenewPeriod == null ? DEFAULT_AUTO_RENEW_PERIOD
                        : autoRenewPeriod));
        final NativeToken<?, ?> initialBalance = transaction.initialBalance();
        if (initialBalance != null) {
            create.setInitialBalance(initialBalance.toBaseUnits());
        }
        final String memo = transaction.accountMemo();
        if (memo != null) {
            create.setMemo(memo);
        }
        final Integer maxAutomaticTokenAssociations = transaction.maxAutomaticTokenAssociations();
        if (maxAutomaticTokenAssociations != null) {
            create.setMaxAutomaticTokenAssociations(maxAutomaticTokenAssociations);
        }
        final AccountId stakedAccountId = transaction.stakedAccountId();
        if (stakedAccountId != null) {
            create.setStakedAccountId(Protobuf.toProto(stakedAccountId));
        }
        final Long stakedNodeId = transaction.stakedNodeId();
        if (stakedNodeId != null) {
            create.setStakedNodeId(stakedNodeId);
        }
        final byte[] alias = transaction.alias();
        if (alias != null) {
            create.setAlias(ByteString.copyFrom(alias));
        }
        body.setCryptoCreateAccount(create);
    }

    /// Fills `CryptoUpdateTransactionBody`. Every attribute that is `null` stays unchanged on the network, so only
    /// the set attributes are written. The staking sentinels of HAPI (`0.0.0` for `stakedAccountId`, `-1` for
    /// `stakedNodeId`, which remove the staking) are passed through unchanged.
    private static void accountUpdate(final AccountUpdateTransaction transaction, final TransactionBody.Builder body) {
        final CryptoUpdateTransactionBody.Builder update = CryptoUpdateTransactionBody.newBuilder()
                .setAccountIDToUpdate(Protobuf.toProto(transaction.accountId()));
        final Authority authority = transaction.authority();
        if (authority != null) {
            update.setKey(Protobuf.toProto(authority));
        }
        final String memo = transaction.accountMemo();
        if (memo != null) {
            update.setMemo(StringValue.of(memo));
        }
        final Boolean receiverSignatureRequired = transaction.receiverSignatureRequired();
        if (receiverSignatureRequired != null) {
            update.setReceiverSigRequiredWrapper(BoolValue.of(receiverSignatureRequired));
        }
        final Integer maxAutomaticTokenAssociations = transaction.maxAutomaticTokenAssociations();
        if (maxAutomaticTokenAssociations != null) {
            update.setMaxAutomaticTokenAssociations(Int32Value.of(maxAutomaticTokenAssociations));
        }
        final Duration autoRenewPeriod = transaction.autoRenewPeriod();
        if (autoRenewPeriod != null) {
            update.setAutoRenewPeriod(Protobuf.toProto(autoRenewPeriod));
        }
        final ZonedDateTime expirationTime = transaction.expirationTime();
        if (expirationTime != null) {
            update.setExpirationTime(Protobuf.toProto(expirationTime));
        }
        final AccountId stakedAccountId = transaction.stakedAccountId();
        if (stakedAccountId != null) {
            update.setStakedAccountId(Protobuf.toProto(stakedAccountId));
        }
        final Long stakedNodeId = transaction.stakedNodeId();
        if (stakedNodeId != null) {
            update.setStakedNodeId(stakedNodeId);
        }
        final Boolean declineStakingReward = transaction.declineStakingReward();
        if (declineStakingReward != null) {
            update.setDeclineReward(BoolValue.of(declineStakingReward));
        }
        body.setCryptoUpdateAccount(update);
    }

    /// Fills `CryptoDeleteTransactionBody`.
    private static void accountDelete(final AccountDeleteTransaction transaction, final TransactionBody.Builder body) {
        body.setCryptoDelete(CryptoDeleteTransactionBody.newBuilder()
                .setDeleteAccountID(Protobuf.toProto(transaction.accountId()))
                .setTransferAccountID(Protobuf.toProto(transaction.transferAccountId())));
    }

    /// Fills `CryptoApproveAllowanceTransactionBody`: one entry per allowance, in the order of the transaction.
    private static void allowanceApprove(final AccountAllowanceApproveTransaction transaction,
                                         final TransactionBody.Builder body) {
        final CryptoApproveAllowanceTransactionBody.Builder approve =
                CryptoApproveAllowanceTransactionBody.newBuilder();
        for (final HbarAllowance allowance : transaction.hbarAllowances()) {
            approve.addCryptoAllowances(CryptoAllowance.newBuilder()
                    .setOwner(Protobuf.toProto(allowance.ownerAccountId()))
                    .setSpender(Protobuf.toProto(allowance.spenderAccountId()))
                    .setAmount(allowance.amount().toBaseUnits()));
        }
        for (final TokenAllowance allowance : transaction.tokenAllowances()) {
            approve.addTokenAllowances(com.hederahashgraph.api.proto.java.TokenAllowance.newBuilder()
                    .setTokenId(Protobuf.toTokenId(allowance.tokenId()))
                    .setOwner(Protobuf.toProto(allowance.ownerAccountId()))
                    .setSpender(Protobuf.toProto(allowance.spenderAccountId()))
                    .setAmount(allowance.amount()));
        }
        for (final NftAllowance allowance : transaction.nftAllowances()) {
            final com.hederahashgraph.api.proto.java.NftAllowance.Builder nft =
                    com.hederahashgraph.api.proto.java.NftAllowance.newBuilder()
                            .setTokenId(Protobuf.toTokenId(allowance.tokenId()))
                            .setOwner(Protobuf.toProto(allowance.ownerAccountId()))
                            .setSpender(Protobuf.toProto(allowance.spenderAccountId()));
            switch (allowance.grant()) {
                case NftSerialGrant serials -> nft.addAllSerialNumbers(serials.serials());
                case NftCollectionGrant collection -> nft.setApprovedForAll(BoolValue.of(collection.approved()));
            }
            final AccountId delegatingSpender = allowance.delegatingSpender();
            if (delegatingSpender != null) {
                nft.setDelegatingSpender(Protobuf.toProto(delegatingSpender));
            }
            approve.addNftAllowances(nft);
        }
        body.setCryptoApproveAllowance(approve);
    }

    /// Fills `CryptoDeleteAllowanceTransactionBody`: one `NftRemoveAllowance` per deletion.
    private static void allowanceDelete(final AccountAllowanceDeleteTransaction transaction,
                                        final TransactionBody.Builder body) {
        final CryptoDeleteAllowanceTransactionBody.Builder delete = CryptoDeleteAllowanceTransactionBody.newBuilder();
        for (final NftAllowanceDeletion deletion : transaction.nftAllowanceDeletions()) {
            delete.addNftAllowances(NftRemoveAllowance.newBuilder()
                    .setTokenId(Protobuf.toTokenId(deletion.tokenId()))
                    .setOwner(Protobuf.toProto(deletion.ownerAccountId()))
                    .addAllSerialNumbers(deletion.serials()));
        }
        body.setCryptoDeleteAllowance(delete);
    }

    /// Fills `CryptoTransferTransactionBody`. HAPI wants every account at most once per list and one
    /// `TokenTransferList` per token that holds both its fungible and its NFT transfers, so legs of the same account
    /// (and token) with the same `approved` flag are summed up, and the token transfers are grouped per token in the
    /// order in which the tokens first appear. A token whose legs name different `expectedDecimals` is rejected.
    private static void transfer(final TransferTransaction transaction, final TransactionBody.Builder body) {
        final Map<Leg, Long> hbar = new LinkedHashMap<>();
        for (final HbarTransfer transfer : transaction.hbarTransfers()) {
            hbar.merge(new Leg(transfer.accountId(), transfer.approved()), transfer.amount().toBaseUnits(),
                    Math::addExact);
        }
        final TransferList.Builder hbarList = TransferList.newBuilder();
        hbar.forEach((leg, amount) -> hbarList.addAccountAmounts(accountAmount(leg.accountId(), amount,
                leg.approved())));

        final Map<Address, TokenGroup> tokens = new LinkedHashMap<>();
        for (final TokenTransfer transfer : transaction.tokenTransfers()) {
            final TokenGroup group = tokens.computeIfAbsent(transfer.tokenId(), any -> new TokenGroup());
            final Integer expectedDecimals = transfer.expectedDecimals();
            if (expectedDecimals != null) {
                if (group.expectedDecimals != null && !group.expectedDecimals.equals(expectedDecimals)) {
                    throw new IllegalArgumentException("The token transfers of " + transfer.tokenId()
                            + " name different expectedDecimals: " + group.expectedDecimals + " and "
                            + expectedDecimals);
                }
                group.expectedDecimals = expectedDecimals;
            }
            group.amounts.merge(new Leg(transfer.accountId(), transfer.approved()), transfer.amount(),
                    Math::addExact);
        }
        for (final NftTransfer transfer : transaction.nftTransfers()) {
            tokens.computeIfAbsent(transfer.tokenId(), any -> new TokenGroup()).nfts.add(
                    com.hederahashgraph.api.proto.java.NftTransfer.newBuilder()
                            .setSenderAccountID(Protobuf.toProto(transfer.sender()))
                            .setReceiverAccountID(Protobuf.toProto(transfer.receiver()))
                            .setSerialNumber(transfer.serial())
                            .setIsApproval(transfer.approved())
                            .build());
        }
        final CryptoTransferTransactionBody.Builder cryptoTransfer = CryptoTransferTransactionBody.newBuilder()
                .setTransfers(hbarList);
        tokens.forEach((tokenId, group) -> {
            final TokenTransferList.Builder list = TokenTransferList.newBuilder().setToken(Protobuf.toTokenId(tokenId))
                    .addAllNftTransfers(group.nfts);
            group.amounts.forEach((leg, amount) -> list.addTransfers(accountAmount(leg.accountId(), amount,
                    leg.approved())));
            if (group.expectedDecimals != null) {
                list.setExpectedDecimals(UInt32Value.of(group.expectedDecimals));
            }
            cryptoTransfer.addTokenTransfers(list);
        });
        body.setCryptoTransfer(cryptoTransfer);
    }

    private static AccountAmount accountAmount(final AccountId accountId, final long amount, final boolean approved) {
        return AccountAmount.newBuilder().setAccountID(Protobuf.toProto(accountId)).setAmount(amount)
                .setIsApproval(approved).build();
    }

    /// The key under which legs of a transfer list are summed up.
    ///
    /// @param accountId the account debited or credited
    /// @param approved  whether the debit is drawn from an allowance
    private record Leg(AccountId accountId, boolean approved) {

        Leg {
            Objects.requireNonNull(accountId, "accountId must not be null");
        }
    }

    /// The transfers of one token, collected for its `TokenTransferList`.
    private static final class TokenGroup {

        private final Map<Leg, Long> amounts = new LinkedHashMap<>();

        private final List<com.hederahashgraph.api.proto.java.NftTransfer> nfts = new ArrayList<>();

        private @Nullable Integer expectedDecimals;
    }
}
