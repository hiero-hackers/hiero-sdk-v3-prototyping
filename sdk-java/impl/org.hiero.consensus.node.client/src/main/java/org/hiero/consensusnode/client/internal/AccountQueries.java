package org.hiero.consensusnode.client.internal;

import com.hederahashgraph.api.proto.java.CryptoGetAccountBalanceQuery;
import com.hederahashgraph.api.proto.java.CryptoGetAccountBalanceResponse;
import com.hederahashgraph.api.proto.java.CryptoGetAccountRecordsQuery;
import com.hederahashgraph.api.proto.java.CryptoGetInfoQuery;
import com.hederahashgraph.api.proto.java.CryptoGetInfoResponse;
import com.hederahashgraph.api.proto.java.Key;
import com.hederahashgraph.api.proto.java.Query;
import com.hederahashgraph.api.proto.java.StakingInfo;
import com.hederahashgraph.api.proto.java.TransactionReceipt;
import com.hederahashgraph.api.proto.java.TransactionRecord;
import java.util.HexFormat;
import java.util.Map;
import org.hiero.consensusnode.queries.accounts.AccountBalance;
import org.hiero.consensusnode.queries.accounts.AccountBalanceQuery;
import org.hiero.consensusnode.queries.accounts.AccountInfo;
import org.hiero.consensusnode.queries.accounts.AccountInfoQuery;
import org.hiero.consensusnode.queries.accounts.AccountRecords;
import org.hiero.consensusnode.queries.accounts.AccountRecordsQuery;
import org.hiero.consensusnode.transactions.Receipt;
import org.hiero.consensusnode.transactions.Record;
import org.hiero.consensusnode.transactions.TransactionStatus;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.ContractId;
import org.hiero.ledger.EvmAddress;
import org.hiero.ledger.TransactionId;
import org.hiero.nativeToken.ExchangeRate;
import org.jspecify.annotations.Nullable;

/// The query types of accounts.
public final class AccountQueries {

    private static final int EVM_ADDRESS_LENGTH = 20;

    /// The leading bytes of a long-zero EVM address (`shard`, `realm` and `num` of the entity), which is no alias.
    private static final int LONG_ZERO_PREFIX_LENGTH = 12;

    /// `AccountBalanceQuery`: `CryptoService.cryptoGetBalance`, free.
    ///
    /// The token balances are not read: HAPI deprecated `tokenBalances` of the answer and current networks leave it
    /// empty, so `AccountBalance.tokenBalances` is always empty (the mirror node has them).
    public static final QueryKind<AccountBalanceQuery, AccountBalance> ACCOUNT_BALANCE = new QueryKind<>(
            "proto.CryptoService", "cryptoGetBalance",
            (query, header) -> {
                final CryptoGetAccountBalanceQuery.Builder balance = CryptoGetAccountBalanceQuery.newBuilder()
                        .setHeader(header);
                final AccountId accountId = query.accountId();
                final ContractId contractId = query.contractId();
                if (accountId != null) {
                    balance.setAccountID(Protobuf.toProto(accountId));
                } else if (contractId != null) {
                    balance.setContractID(Protobuf.toProto(contractId));
                }
                return Query.newBuilder().setCryptogetAccountBalance(balance).build();
            },
            response -> response.getCryptogetAccountBalance().getHeader(),
            (query, response) -> balance(query, response.getCryptogetAccountBalance()));

    /// `AccountInfoQuery`: `CryptoService.getAccountInfo`, paid.
    public static final QueryKind<AccountInfoQuery, AccountInfo> ACCOUNT_INFO = new QueryKind<>(
            "proto.CryptoService", "getAccountInfo",
            (query, header) -> Query.newBuilder().setCryptoGetInfo(CryptoGetInfoQuery.newBuilder().setHeader(header)
                    .setAccountID(Protobuf.toProto(query.accountId()))).build(),
            response -> response.getCryptoGetInfo().getHeader(),
            (query, response) -> info(response.getCryptoGetInfo().getAccountInfo()));

    /// `AccountRecordsQuery`: `CryptoService.getAccountRecords`, paid.
    public static final QueryKind<AccountRecordsQuery, AccountRecords> ACCOUNT_RECORDS = new QueryKind<>(
            "proto.CryptoService", "getAccountRecords",
            (query, header) -> Query.newBuilder().setCryptoGetAccountRecords(CryptoGetAccountRecordsQuery.newBuilder()
                    .setHeader(header).setAccountID(Protobuf.toProto(query.accountId()))).build(),
            response -> response.getCryptoGetAccountRecords().getHeader(),
            (query, response) -> new AccountRecords(response.getCryptoGetAccountRecords().hasAccountID()
                    ? Protobuf.fromProto(response.getCryptoGetAccountRecords().getAccountID()) : query.accountId(),
                    response.getCryptoGetAccountRecords().getRecordsList().stream()
                            .map(AccountQueries::record).toList()));

    private AccountQueries() {
    }

    private static AccountBalance balance(final AccountBalanceQuery query,
                                          final CryptoGetAccountBalanceResponse response) {
        final AccountId accountId;
        if (response.hasAccountID()) {
            accountId = Protobuf.fromProto(response.getAccountID());
        } else if (query.accountId() != null) {
            accountId = query.accountId();
        } else {
            final ContractId contractId = query.contractId();
            if (contractId == null || contractId.num() == null) {
                throw new IllegalStateException("The node named no account for the balance of " + contractId);
            }
            accountId = new AccountId(contractId.shard(), contractId.realm(), "", contractId.num(), null, null);
        }
        return new AccountBalance(accountId, Protobuf.hbar(response.getBalance()), Map.of());
    }

    private static AccountInfo info(final CryptoGetInfoResponse.AccountInfo info) {
        final StakingInfo staking = info.getStakingInfo();
        return new AccountInfo(
                Protobuf.fromProto(info.getAccountID()),
                evmAddress(info),
                Protobuf.hbar(info.getBalance()),
                info.hasKey() && info.getKey().getKeyCase() != Key.KeyCase.KEY_NOT_SET
                        ? Protobuf.fromProto(info.getKey()) : null,
                info.getMemo(),
                Protobuf.fromProto(info.getExpirationTime()),
                info.hasAutoRenewPeriod() ? Protobuf.fromProto(info.getAutoRenewPeriod()) : null,
                info.getMaxAutomaticTokenAssociations(),
                info.getReceiverSigRequired(),
                info.getOwnedNfts(),
                info.getDeleted(),
                staking.getStakedIdCase() == StakingInfo.StakedIdCase.STAKED_ACCOUNT_ID
                        ? Protobuf.fromProto(staking.getStakedAccountId()) : null,
                staking.getStakedIdCase() == StakingInfo.StakedIdCase.STAKED_NODE_ID
                        ? staking.getStakedNodeId() : null,
                staking.getDeclineReward());
    }

    /// The EVM-address alias of an account: its alias if that is an EVM address, otherwise the `contractAccountID`
    /// unless that is only the long-zero form of the account number.
    private static @Nullable EvmAddress evmAddress(final CryptoGetInfoResponse.AccountInfo info) {
        final byte[] alias = info.getAlias().toByteArray();
        if (alias.length == EVM_ADDRESS_LENGTH) {
            return new EvmAddress(alias);
        }
        final String text = info.getContractAccountID();
        final String hex = text.startsWith("0x") ? text.substring(2) : text;
        if (hex.length() != 2 * EVM_ADDRESS_LENGTH || !hex.chars().allMatch(c -> Character.digit(c, 16) >= 0)) {
            return null;
        }
        final byte[] bytes = HexFormat.of().parseHex(hex);
        for (int i = 0; i < LONG_ZERO_PREFIX_LENGTH; i++) {
            if (bytes[i] != 0) {
                return new EvmAddress(bytes);
            }
        }
        return null;
    }

    /// A record of a transaction whose type is unknown: a `TransactionRecord` carries no body, so its receipt is an
    /// `UntypedReceipt` with the attributes all receipts share.
    private static Record<Receipt> record(final TransactionRecord record) {
        final TransactionReceipt receipt = record.getReceipt();
        final TransactionId id = Protobuf.fromProto(record.getTransactionID());
        return new Record<>(id, Protobuf.fromProto(record.getConsensusTimestamp()),
                new UntypedReceipt(id, HapiTransactionStatus.ofCode(receipt.getStatusValue()),
                        Protobuf.currentRate(receipt.getExchangeRate()), Protobuf.nextRate(receipt.getExchangeRate())),
                record.hasParentConsensusTimestamp() ? Protobuf.fromProto(record.getParentConsensusTimestamp())
                        : null);
    }

    /// The receipt of a record whose transaction type is unknown: only the attributes every receipt has.
    private static final class UntypedReceipt extends Receipt {

        /// Creates the receipt.
        ///
        /// @param transactionId    the id of the transaction
        /// @param status           its status
        /// @param exchangeRate     the current exchange rate
        /// @param nextExchangeRate the next exchange rate
        UntypedReceipt(final TransactionId transactionId, final TransactionStatus status,
                       final ExchangeRate exchangeRate, final ExchangeRate nextExchangeRate) {
            super(transactionId, status, exchangeRate, nextExchangeRate);
        }

        @Override
        public String toString() {
            return "UntypedReceipt[transactionId=" + transactionId() + ", status=" + status() + ", exchangeRate="
                    + exchangeRate() + ", nextExchangeRate=" + nextExchangeRate() + "]";
        }
    }
}
