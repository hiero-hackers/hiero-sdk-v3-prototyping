package org.hiero.consensusnode.client.internal;

import com.google.protobuf.ByteString;
import com.hederahashgraph.api.proto.java.AccountID;
import com.hederahashgraph.api.proto.java.ContractCallLocalQuery;
import com.hederahashgraph.api.proto.java.ContractGetBytecodeQuery;
import com.hederahashgraph.api.proto.java.ContractGetInfoQuery;
import com.hederahashgraph.api.proto.java.ContractGetInfoResponse;
import com.hederahashgraph.api.proto.java.Key;
import com.hederahashgraph.api.proto.java.Query;
import com.hederahashgraph.api.proto.java.StakingInfo;
import org.hiero.consensusnode.queries.contracts.ContractBytecode;
import org.hiero.consensusnode.queries.contracts.ContractBytecodeQuery;
import org.hiero.consensusnode.queries.contracts.ContractCallQuery;
import org.hiero.consensusnode.queries.contracts.ContractInfo;
import org.hiero.consensusnode.queries.contracts.ContractInfoQuery;
import org.hiero.consensusnode.transactions.ContractFunctionResult;
import org.hiero.evm.EvmAddress;
import org.hiero.ledger.AccountId;
import org.jspecify.annotations.Nullable;

/// The query types of contracts.
public final class ContractQueries {

    /// `ContractCallQuery`: `SmartContractService.contractCallLocalMethod`, paid.
    public static final QueryKind<ContractCallQuery, ContractFunctionResult> CONTRACT_CALL = new QueryKind<>(
            "proto.SmartContractService", "contractCallLocalMethod",
            (query, header) -> {
                final ContractCallLocalQuery.Builder call = ContractCallLocalQuery.newBuilder().setHeader(header)
                        .setContractID(Protobuf.toProto(query.contractId()))
                        .setGas(query.gas())
                        .setFunctionParameters(ByteString.copyFrom(query.callData()));
                final AccountId senderId = query.senderId();
                if (senderId != null) {
                    call.setSenderId(Protobuf.toProto(senderId));
                }
                return Query.newBuilder().setContractCallLocal(call).build();
            },
            response -> response.getContractCallLocal().getHeader(),
            (query, response) -> ContractKinds.fromProto(response.getContractCallLocal().getFunctionResult()));

    /// `ContractInfoQuery`: `SmartContractService.getContractInfo`, paid.
    public static final QueryKind<ContractInfoQuery, ContractInfo> CONTRACT_INFO = new QueryKind<>(
            "proto.SmartContractService", "getContractInfo",
            (query, header) -> Query.newBuilder().setContractGetInfo(ContractGetInfoQuery.newBuilder()
                    .setHeader(header).setContractID(Protobuf.toProto(query.contractId()))).build(),
            response -> response.getContractGetInfo().getHeader(),
            (query, response) -> info(response.getContractGetInfo().getContractInfo()));

    /// `ContractBytecodeQuery`: `SmartContractService.ContractGetBytecode`, paid. The answer does not name the
    /// contract, so the bytecode belongs to the contract of the query.
    public static final QueryKind<ContractBytecodeQuery, ContractBytecode> CONTRACT_BYTECODE = new QueryKind<>(
            "proto.SmartContractService", "ContractGetBytecode",
            (query, header) -> Query.newBuilder().setContractGetBytecode(ContractGetBytecodeQuery.newBuilder()
                    .setHeader(header).setContractID(Protobuf.toProto(query.contractId()))).build(),
            response -> response.getContractGetBytecodeResponse().getHeader(),
            (query, response) -> new ContractBytecode(query.contractId(),
                    response.getContractGetBytecodeResponse().getBytecode().toByteArray()));

    private ContractQueries() {
    }

    private static ContractInfo info(final ContractGetInfoResponse.ContractInfo info) {
        final StakingInfo staking = info.getStakingInfo();
        return new ContractInfo(
                Protobuf.fromProto(info.getContractID()),
                Protobuf.fromProto(info.getAccountID()),
                EvmAddress.fromString(info.getContractAccountID()),
                info.hasAdminKey() && info.getAdminKey().getKeyCase() != Key.KeyCase.KEY_NOT_SET
                        ? Protobuf.fromProto(info.getAdminKey()) : null,
                Protobuf.fromProto(info.getExpirationTime()),
                info.hasAutoRenewPeriod() ? Protobuf.fromProto(info.getAutoRenewPeriod()) : null,
                info.hasAutoRenewAccountId() ? account(info.getAutoRenewAccountId()) : null,
                info.getStorage(),
                info.getMemo(),
                Protobuf.hbar(info.getBalance()),
                info.getDeleted(),
                info.getMaxAutomaticTokenAssociations(),
                staking.getStakedIdCase() == StakingInfo.StakedIdCase.STAKED_ACCOUNT_ID
                        ? Protobuf.fromProto(staking.getStakedAccountId()) : null,
                staking.getStakedIdCase() == StakingInfo.StakedIdCase.STAKED_NODE_ID
                        ? staking.getStakedNodeId() : null,
                staking.getDeclineReward());
    }

    /// The auto-renew account of a contract; the account `0.0.0` (the "no account" sentinel) is no account.
    private static @Nullable AccountId account(final AccountID account) {
        return account.getAccountCase() == AccountID.AccountCase.ACCOUNTNUM && account.getAccountNum() == 0
                ? null : Protobuf.fromProto(account);
    }
}
