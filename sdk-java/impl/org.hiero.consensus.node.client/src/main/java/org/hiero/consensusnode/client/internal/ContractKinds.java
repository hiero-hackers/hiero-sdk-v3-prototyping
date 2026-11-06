package org.hiero.consensusnode.client.internal;

import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;
import com.hederahashgraph.api.proto.java.AccountID;
import com.hederahashgraph.api.proto.java.ContractCallTransactionBody;
import com.hederahashgraph.api.proto.java.ContractCreateTransactionBody;
import com.hederahashgraph.api.proto.java.ContractDeleteTransactionBody;
import com.hederahashgraph.api.proto.java.ContractFunctionResult;
import com.hederahashgraph.api.proto.java.ContractLoginfo;
import com.hederahashgraph.api.proto.java.ContractUpdateTransactionBody;
import com.hederahashgraph.api.proto.java.EthereumTransactionBody;
import com.hederahashgraph.api.proto.java.TransactionBody;
import com.hederahashgraph.api.proto.java.TransactionRecord;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import org.hiero.authority.Authority;
import org.hiero.consensusnode.transactions.ContractLog;
import org.hiero.consensusnode.transactions.ContractNonce;
import org.hiero.consensusnode.transactions.contracts.ContractCreateReceipt;
import org.hiero.consensusnode.transactions.contracts.ContractCreateTransaction;
import org.hiero.consensusnode.transactions.contracts.ContractDeleteReceipt;
import org.hiero.consensusnode.transactions.contracts.ContractDeleteTransaction;
import org.hiero.consensusnode.transactions.contracts.ContractExecuteReceipt;
import org.hiero.consensusnode.transactions.contracts.ContractExecuteTransaction;
import org.hiero.consensusnode.transactions.contracts.ContractUpdateReceipt;
import org.hiero.consensusnode.transactions.contracts.ContractUpdateTransaction;
import org.hiero.consensusnode.transactions.contracts.EthereumTransaction;
import org.hiero.consensusnode.transactions.contracts.EthereumTransactionReceipt;
import org.hiero.evm.EvmAddress;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.ledger.ContractId;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// The transaction types of smart contracts, and the result of a contract execution in a record.
public final class ContractKinds {

    /// HAPI requires an auto-renew period for a new contract; the default when the transaction sets none - the one of
    /// the Hiero SDKs (90 days), as for accounts.
    private static final Duration DEFAULT_AUTO_RENEW_PERIOD = Duration.ofDays(90);

    /// The contract a receipt names none of (a failed create).
    private static final ContractId NO_CONTRACT = new ContractId(0L, 0L, "", 0L, null);

    /// `ContractCreateTransaction`: `SmartContractService.createContract`.
    public static final TransactionKind<ContractCreateReceipt, ContractCreateTransaction> CONTRACT_CREATE =
            new TransactionKind<>(ContractCreateTransaction.class, "proto.SmartContractService", "createContract",
                    TransactionBody.DataCase.CONTRACTCREATEINSTANCE, ContractKinds::contractCreate,
                    (id, status, rate, nextRate, receipt) -> new ContractCreateReceipt(id, status, rate, nextRate,
                            receipt.hasContractID() ? Protobuf.fromProto(receipt.getContractID()) : NO_CONTRACT));

    /// `ContractExecuteTransaction`: `SmartContractService.contractCallMethod`.
    public static final TransactionKind<ContractExecuteReceipt, ContractExecuteTransaction> CONTRACT_EXECUTE =
            new TransactionKind<>(ContractExecuteTransaction.class, "proto.SmartContractService", "contractCallMethod",
                    TransactionBody.DataCase.CONTRACTCALL, ContractKinds::contractExecute,
                    (id, status, rate, nextRate, receipt) -> new ContractExecuteReceipt(id, status, rate, nextRate));

    /// `ContractUpdateTransaction`: `SmartContractService.updateContract`.
    public static final TransactionKind<ContractUpdateReceipt, ContractUpdateTransaction> CONTRACT_UPDATE =
            new TransactionKind<>(ContractUpdateTransaction.class, "proto.SmartContractService", "updateContract",
                    TransactionBody.DataCase.CONTRACTUPDATEINSTANCE, ContractKinds::contractUpdate,
                    (id, status, rate, nextRate, receipt) -> new ContractUpdateReceipt(id, status, rate, nextRate));

    /// `ContractDeleteTransaction`: `SmartContractService.deleteContract`.
    public static final TransactionKind<ContractDeleteReceipt, ContractDeleteTransaction> CONTRACT_DELETE =
            new TransactionKind<>(ContractDeleteTransaction.class, "proto.SmartContractService", "deleteContract",
                    TransactionBody.DataCase.CONTRACTDELETEINSTANCE, ContractKinds::contractDelete,
                    (id, status, rate, nextRate, receipt) -> new ContractDeleteReceipt(id, status, rate, nextRate));

    /// `EthereumTransaction`: `SmartContractService.callEthereum`.
    public static final TransactionKind<EthereumTransactionReceipt, EthereumTransaction> ETHEREUM =
            new TransactionKind<>(EthereumTransaction.class, "proto.SmartContractService", "callEthereum",
                    TransactionBody.DataCase.ETHEREUMTRANSACTION, ContractKinds::ethereum,
                    (id, status, rate, nextRate, receipt) -> new EthereumTransactionReceipt(id, status, rate, nextRate,
                            receipt.hasContractID() ? Protobuf.fromProto(receipt.getContractID()) : null));

    /// All transaction types of this class.
    public static final List<TransactionKind<?, ?>> ALL =
            List.of(CONTRACT_CREATE, CONTRACT_EXECUTE, CONTRACT_UPDATE, CONTRACT_DELETE, ETHEREUM);

    private ContractKinds() {
    }

    /// The result of the contract execution in a record: the result of a call (`contractCallResult`) or of a creation
    /// (`contractCreateResult`).
    ///
    /// @param record the record message
    /// @return the result, `null` if the record has none
    public static org.hiero.consensusnode.transactions.@Nullable ContractFunctionResult contractResult(
            final TransactionRecord record) {
        if (record.hasContractCallResult()) {
            return fromProto(record.getContractCallResult());
        }
        if (record.hasContractCreateResult()) {
            return fromProto(record.getContractCreateResult());
        }
        return null;
    }

    private static org.hiero.consensusnode.transactions.ContractFunctionResult fromProto(
            final ContractFunctionResult result) {
        final String errorMessage = result.getErrorMessage();
        final byte[] evmAddress = result.hasEvmAddress() ? result.getEvmAddress().getValue().toByteArray() : null;
        return new org.hiero.consensusnode.transactions.ContractFunctionResult(
                Protobuf.fromProto(result.getContractID()),
                evmAddress != null && evmAddress.length == 20 ? EvmAddress.fromBytes(evmAddress) : null,
                result.getContractCallResult().toByteArray(),
                errorMessage.isEmpty() ? null : errorMessage,
                result.getBloom().toByteArray(),
                result.getGasUsed(),
                result.getGas(),
                Protobuf.hbar(result.getAmount()),
                result.getFunctionParameters().toByteArray(),
                result.hasSenderId() ? Protobuf.fromProto(result.getSenderId()) : null,
                result.getLogInfoList().stream().map(ContractKinds::fromProto).toList(),
                result.getContractNoncesList().stream()
                        .map(nonce -> new ContractNonce(Protobuf.fromProto(nonce.getContractId()), nonce.getNonce()))
                        .toList(),
                result.hasSignerNonce() ? result.getSignerNonce().getValue() : null);
    }

    private static ContractLog fromProto(final ContractLoginfo log) {
        return new ContractLog(Protobuf.fromProto(log.getContractID()), log.getBloom().toByteArray(),
                log.getTopicList().stream().map(ByteString::toByteArray).toList(), log.getData().toByteArray());
    }

    private static void contractCreate(final ContractCreateTransaction transaction, final TransactionBody.Builder body) {
        final Duration autoRenewPeriod = transaction.autoRenewPeriod();
        final ContractCreateTransactionBody.Builder create = ContractCreateTransactionBody.newBuilder()
                .setGas(transaction.gas())
                .setConstructorParameters(ByteString.copyFrom(transaction.constructorParameters()))
                .setDeclineReward(transaction.declineStakingReward())
                .setAutoRenewPeriod(Protobuf.toProto(autoRenewPeriod == null ? DEFAULT_AUTO_RENEW_PERIOD
                        : autoRenewPeriod));
        final byte[] bytecode = transaction.bytecode();
        if (bytecode != null) {
            create.setInitcode(ByteString.copyFrom(bytecode));
        }
        final Address bytecodeFileId = transaction.bytecodeFileId();
        if (bytecodeFileId != null) {
            create.setFileID(Protobuf.toFileId(bytecodeFileId));
        }
        final NativeToken<?, ?> initialBalance = transaction.initialBalance();
        if (initialBalance != null) {
            create.setInitialBalance(initialBalance.toBaseUnits());
        }
        final Authority adminAuthority = transaction.adminAuthority();
        if (adminAuthority != null) {
            create.setAdminKey(Protobuf.toProto(adminAuthority));
        }
        final String memo = transaction.contractMemo();
        if (memo != null) {
            create.setMemo(memo);
        }
        final AccountId autoRenewAccount = transaction.autoRenewAccount();
        if (autoRenewAccount != null) {
            create.setAutoRenewAccountId(Protobuf.toProto(autoRenewAccount));
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
        body.setContractCreateInstance(create);
    }

    private static void contractExecute(final ContractExecuteTransaction transaction,
                                        final TransactionBody.Builder body) {
        final ContractCallTransactionBody.Builder call = ContractCallTransactionBody.newBuilder()
                .setContractID(Protobuf.toProto(transaction.contractId()))
                .setGas(transaction.gas())
                .setFunctionParameters(ByteString.copyFrom(transaction.callData()));
        final NativeToken<?, ?> payableAmount = transaction.payableAmount();
        if (payableAmount != null) {
            call.setAmount(payableAmount.toBaseUnits());
        }
        body.setContractCall(call);
    }

    /// Fills `ContractUpdateTransactionBody`. Every attribute that is `null` stays unchanged on the network, so only
    /// the set attributes are written. The sentinels of HAPI (`0.0.0` for the auto-renew account and the staked
    /// account, `-1` for the staked node, which remove them) are passed through unchanged.
    private static void contractUpdate(final ContractUpdateTransaction transaction,
                                       final TransactionBody.Builder body) {
        final ContractUpdateTransactionBody.Builder update = ContractUpdateTransactionBody.newBuilder()
                .setContractID(Protobuf.toProto(transaction.contractId()));
        final Authority adminAuthority = transaction.adminAuthority();
        if (adminAuthority != null) {
            update.setAdminKey(Protobuf.toProto(adminAuthority));
        }
        final String memo = transaction.contractMemo();
        if (memo != null) {
            update.setMemoWrapper(StringValue.of(memo));
        }
        final ZonedDateTime expirationTime = transaction.expirationTime();
        if (expirationTime != null) {
            update.setExpirationTime(Protobuf.toProto(expirationTime));
        }
        final Duration autoRenewPeriod = transaction.autoRenewPeriod();
        if (autoRenewPeriod != null) {
            update.setAutoRenewPeriod(Protobuf.toProto(autoRenewPeriod));
        }
        final AccountId autoRenewAccount = transaction.autoRenewAccount();
        if (autoRenewAccount != null) {
            // the network removes the auto-renew account for an AccountID without a number (the default message); an
            // AccountID with the number 0 is rejected with INVALID_AUTORENEW_ACCOUNT
            update.setAutoRenewAccountId(isZero(autoRenewAccount) ? AccountID.getDefaultInstance()
                    : Protobuf.toProto(autoRenewAccount));
        }
        final Integer maxAutomaticTokenAssociations = transaction.maxAutomaticTokenAssociations();
        if (maxAutomaticTokenAssociations != null) {
            update.setMaxAutomaticTokenAssociations(Int32Value.of(maxAutomaticTokenAssociations));
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
        body.setContractUpdateInstance(update);
    }

    /// Whether an account id is `ZERO_ACCOUNT_ID` (`0.0.0`), the sentinel that removes an account.
    private static boolean isZero(final AccountId accountId) {
        final Long num = accountId.num();
        return accountId.shard() == 0L && accountId.realm() == 0L && num != null && num == 0L;
    }

    private static void contractDelete(final ContractDeleteTransaction transaction,
                                       final TransactionBody.Builder body) {
        final ContractDeleteTransactionBody.Builder delete = ContractDeleteTransactionBody.newBuilder()
                .setContractID(Protobuf.toProto(transaction.contractId()));
        final AccountId transferAccountId = transaction.transferAccountId();
        if (transferAccountId != null) {
            delete.setTransferAccountID(Protobuf.toProto(transferAccountId));
        }
        final ContractId transferContractId = transaction.transferContractId();
        if (transferContractId != null) {
            delete.setTransferContractID(Protobuf.toProto(transferContractId));
        }
        body.setContractDeleteInstance(delete);
    }

    private static void ethereum(final EthereumTransaction transaction, final TransactionBody.Builder body) {
        final EthereumTransactionBody.Builder ethereum = EthereumTransactionBody.newBuilder()
                .setEthereumData(ByteString.copyFrom(transaction.ethereumData()));
        final Address callDataFileId = transaction.callDataFileId();
        if (callDataFileId != null) {
            ethereum.setCallData(Protobuf.toFileId(callDataFileId));
        }
        final NativeToken<?, ?> maxGasAllowance = transaction.maxGasAllowance();
        if (maxGasAllowance != null) {
            ethereum.setMaxGasAllowance(maxGasAllowance.toBaseUnits());
        }
        body.setEthereumTransaction(ethereum);
    }
}
