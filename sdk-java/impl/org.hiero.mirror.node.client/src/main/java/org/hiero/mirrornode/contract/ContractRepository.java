package org.hiero.mirrornode.contract;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.CompletionStage;
import org.hiero.common.Page;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.ContractId;
import org.hiero.mirrornode.common.Order;
import org.hiero.mirrornode.internal.MirrorNodeRest;
import org.jspecify.annotations.Nullable;

public class ContractRepository {

    /// Creates a new `ContractRepository`.
    ///
    /// A repository created with this constructor is bound to no mirror node: its queries fail with
    /// `MirrorNodeException`. `ContractFactory.createRepository` creates a repository for a mirror node.
    public ContractRepository() {
    }

    /// The contracts of the network. Maps to GET /api/v1/contracts.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<Page<Contract>> findAll() {
        return MirrorNodeRest.unbound("ContractRepository");
    }

    /// The contracts of the network, filtered by their id. Maps to GET /api/v1/contracts.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<Page<Contract>> findAll(final List<String> contractId, final @Nullable Integer limit, final @Nullable Order order) {
        return MirrorNodeRest.unbound("ContractRepository");
    }

    /// The contract with the given id, with its bytecode. Maps to GET /api/v1/contracts/{contractIdOrAddress}.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<@Nullable Contract> findById(final ContractId contractId) {
        return MirrorNodeRest.unbound("ContractRepository");
    }

    /// The contract with the given id at the given consensus time, with its bytecode.
    /// Maps to GET /api/v1/contracts/{contractIdOrAddress}.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<@Nullable Contract> findById(final ContractId contractId, final List<String> timestamp) {
        return MirrorNodeRest.unbound("ContractRepository");
    }

    /// The results of the executions of a contract. Maps to GET /api/v1/contracts/{contractIdOrAddress}/results.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<Page<ContractResult>> findResults(final ContractId contractId) {
        return MirrorNodeRest.unbound("ContractRepository");
    }

    /// The results of the executions of a contract, filtered by block, sender, consensus time and position in the
    /// block; `internal` includes the child transactions. Maps to GET /api/v1/contracts/{contractIdOrAddress}/results.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<Page<ContractResult>> findResults(final ContractId contractId, final List<String> blockHash, final List<String> blockNumber, final @Nullable AccountId from, final @Nullable Boolean internal, final @Nullable Integer limit, final @Nullable Order order, final List<String> timestamp, final @Nullable Integer transactionIndex) {
        return MirrorNodeRest.unbound("ContractRepository");
    }

    /// The storage slots of a contract and their current values.
    /// Maps to GET /api/v1/contracts/{contractIdOrAddress}/state.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<Page<ContractState>> findState(final ContractId contractId) {
        return MirrorNodeRest.unbound("ContractRepository");
    }

    /// The storage slots of a contract, filtered by slot, and their values at the given consensus time.
    /// Maps to GET /api/v1/contracts/{contractIdOrAddress}/state.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<Page<ContractState>> findState(final ContractId contractId, final @Nullable Integer limit, final @Nullable Order order, final List<String> slot, final List<String> timestamp) {
        return MirrorNodeRest.unbound("ContractRepository");
    }

    /// The result of the execution of a contract at the given consensus time, with its logs and state changes.
    /// Maps to GET /api/v1/contracts/{contractIdOrAddress}/results/{timestamp}.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<@Nullable ContractResult> findResult(final ContractId contractId, final ZonedDateTime timestamp) {
        return MirrorNodeRest.unbound("ContractRepository");
    }

    /// The result of the execution of a contract at the given consensus time; `hbar = false` gives the monetary values
    /// in weibars instead of tinybars. Maps to GET /api/v1/contracts/{contractIdOrAddress}/results/{timestamp}.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<@Nullable ContractResult> findResult(final ContractId contractId, final ZonedDateTime timestamp, final @Nullable Boolean hbar) {
        return MirrorNodeRest.unbound("ContractRepository");
    }

    /// The results of the executions of all contracts. Maps to GET /api/v1/contracts/results.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<Page<ContractResult>> findAllResults() {
        return MirrorNodeRest.unbound("ContractRepository");
    }

    /// The results of the executions of all contracts, filtered by sender, block, consensus time and position in the
    /// block; `hbar = false` gives the monetary values in weibars, `internal` includes the child transactions.
    /// Maps to GET /api/v1/contracts/results.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<Page<ContractResult>> findAllResults(final @Nullable AccountId from, final List<String> blockHash, final List<String> blockNumber, final @Nullable Boolean hbar, final @Nullable Boolean internal, final @Nullable Integer limit, final @Nullable Order order, final List<String> timestamp, final @Nullable Integer transactionIndex) {
        return MirrorNodeRest.unbound("ContractRepository");
    }

    /// The result of the contract execution of a transaction, given by its transaction id or Ethereum transaction hash,
    /// with its logs and state changes. Maps to GET /api/v1/contracts/results/{transactionIdOrHash}.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<@Nullable ContractResult> findResultByTransaction(final String transactionIdOrHash) {
        return MirrorNodeRest.unbound("ContractRepository");
    }

    /// The result of the contract execution of a transaction; `hbar = false` gives the monetary values in weibars,
    /// `nonce` selects a child transaction. Maps to GET /api/v1/contracts/results/{transactionIdOrHash}.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<@Nullable ContractResult> findResultByTransaction(final String transactionIdOrHash, final @Nullable Boolean hbar, final @Nullable Integer nonce) {
        return MirrorNodeRest.unbound("ContractRepository");
    }

    /// The actions (calls) of the execution of a transaction.
    /// Maps to GET /api/v1/contracts/results/{transactionIdOrHash}/actions.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<Page<ContractAction>> findActions(final String transactionIdOrHash) {
        return MirrorNodeRest.unbound("ContractRepository");
    }

    /// The actions (calls) of the execution of a transaction, filtered by their position.
    /// Maps to GET /api/v1/contracts/results/{transactionIdOrHash}/actions.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<Page<ContractAction>> findActions(final String transactionIdOrHash, final List<String> index, final @Nullable Integer limit, final @Nullable Order order) {
        return MirrorNodeRest.unbound("ContractRepository");
    }

    /// The opcode trace of the execution of a transaction, with the stack of each step.
    /// Maps to GET /api/v1/contracts/results/{transactionIdOrHash}/opcodes.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<@Nullable OpcodeTrace> findOpcodes(final String transactionIdOrHash) {
        return MirrorNodeRest.unbound("ContractRepository");
    }

    /// The opcode trace of the execution of a transaction, with the stack, the memory and the storage of each step as
    /// requested. Maps to GET /api/v1/contracts/results/{transactionIdOrHash}/opcodes.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<@Nullable OpcodeTrace> findOpcodes(final String transactionIdOrHash, final @Nullable Boolean stack, final @Nullable Boolean memory, final @Nullable Boolean storage) {
        return MirrorNodeRest.unbound("ContractRepository");
    }

    /// The log events emitted by a contract. Maps to GET /api/v1/contracts/{contractIdOrAddress}/results/logs.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<Page<ContractLog>> findLogs(final ContractId contractId) {
        return MirrorNodeRest.unbound("ContractRepository");
    }

    /// The log events emitted by a contract, filtered by position, consensus time and topics (each in hex).
    /// Maps to GET /api/v1/contracts/{contractIdOrAddress}/results/logs.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<Page<ContractLog>> findLogs(final ContractId contractId, final List<String> index, final @Nullable Integer limit, final @Nullable Order order, final List<String> timestamp, final List<String> topic0, final List<String> topic1, final List<String> topic2, final List<String> topic3) {
        return MirrorNodeRest.unbound("ContractRepository");
    }

    /// The log events emitted by all contracts. Maps to GET /api/v1/contracts/results/logs.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<Page<ContractLog>> findAllLogs() {
        return MirrorNodeRest.unbound("ContractRepository");
    }

    /// The log events emitted by all contracts, filtered by position, consensus time, topics (each in hex) and
    /// transaction hash. Maps to GET /api/v1/contracts/results/logs.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<Page<ContractLog>> findAllLogs(final List<String> index, final @Nullable Integer limit, final @Nullable Order order, final List<String> timestamp, final List<String> topic0, final List<String> topic1, final List<String> topic2, final List<String> topic3, final List<String> transactionHash) {
        return MirrorNodeRest.unbound("ContractRepository");
    }

    /// Simulates a call of a contract without submitting a transaction, or estimates its gas, and returns the result
    /// in hex. Maps to POST /api/v1/contracts/call.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<String> call(final ContractCallRequest request) {
        return MirrorNodeRest.unbound("ContractRepository");
    }
}
