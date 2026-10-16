package org.hiero.consensusnode.client.internal;

import com.google.protobuf.ByteString;
import com.hederahashgraph.api.proto.java.CryptoCreateTransactionBody;
import com.hederahashgraph.api.proto.java.TransactionBody;
import com.hederahashgraph.api.proto.java.UtilPrngTransactionBody;
import java.time.Duration;
import java.util.List;
import org.hiero.consensusnode.transactions.accounts.AccountCreateReceipt;
import org.hiero.consensusnode.transactions.accounts.AccountCreateTransaction;
import org.hiero.consensusnode.transactions.utility.PrngReceipt;
import org.hiero.consensusnode.transactions.utility.PrngTransaction;
import org.hiero.ledger.AccountId;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// The transaction types that are implemented so far. The methods of every other type still throw
/// `UnsupportedOperationException`.
public final class TransactionKinds {

    /// HAPI requires an auto-renew period for a new account; the default when the transaction sets none.
    private static final Duration DEFAULT_AUTO_RENEW_PERIOD = Duration.ofDays(90);

    /// `PrngTransaction`: `UtilService.prng`. The random number or bytes are in the record, not the receipt.
    public static final TransactionKind<PrngReceipt, PrngTransaction> PRNG = new TransactionKind<>(
            PrngTransaction.class, "proto.UtilService", "prng", TransactionBody.DataCase.UTIL_PRNG,
            (transaction, body) -> {
                final UtilPrngTransactionBody.Builder prng = UtilPrngTransactionBody.newBuilder();
                final Integer range = transaction.range();
                if (range != null) {
                    prng.setRange(range);
                }
                body.setUtilPrng(prng);
            },
            (id, status, rate, nextRate, receipt) -> new PrngReceipt(id, status, rate, nextRate, null, null));

    /// `AccountCreateTransaction`: `CryptoService.createAccount`.
    public static final TransactionKind<AccountCreateReceipt, AccountCreateTransaction> ACCOUNT_CREATE =
            new TransactionKind<>(AccountCreateTransaction.class, "proto.CryptoService", "createAccount",
                    TransactionBody.DataCase.CRYPTOCREATEACCOUNT, TransactionKinds::accountCreate,
                    (id, status, rate, nextRate, receipt) -> new AccountCreateReceipt(id, status, rate, nextRate,
                            status == HapiTransactionStatus.SUCCESS ? Protobuf.fromProto(receipt.getAccountID())
                                    : new AccountId(0L, 0L, "", 0L, null, null)));

    private static final List<TransactionKind<?, ?>> ALL = List.of(PRNG, ACCOUNT_CREATE);

    private TransactionKinds() {
    }

    /// The kind of a body field.
    ///
    /// @param dataCase the field of the transaction body
    /// @return the kind, `null` if the type is not implemented
    public static @Nullable TransactionKind<?, ?> of(final TransactionBody.DataCase dataCase) {
        return ALL.stream().filter(kind -> kind.dataCase() == dataCase).findFirst().orElse(null);
    }

    /// The kind of a transaction type.
    ///
    /// @param type the transaction type
    /// @return the kind, `null` if the type is not implemented
    public static @Nullable TransactionKind<?, ?> of(final Class<?> type) {
        return ALL.stream().filter(kind -> kind.type().equals(type)).findFirst().orElse(null);
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
}
