package org.hiero.mirrornode.internal;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import org.hiero.common.Page;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.ContractId;
import org.hiero.ledger.EvmAddress;
import org.hiero.mirrornode.common.Order;
import org.hiero.mirrornode.contract.AccessListEntry;
import org.hiero.mirrornode.contract.AuthorizationListEntry;
import org.hiero.mirrornode.contract.Contract;
import org.hiero.mirrornode.contract.ContractAction;
import org.hiero.mirrornode.contract.ContractActionType;
import org.hiero.mirrornode.contract.ContractCallOperationType;
import org.hiero.mirrornode.contract.ContractCallRequest;
import org.hiero.mirrornode.contract.ContractEntityType;
import org.hiero.mirrornode.contract.ContractLog;
import org.hiero.mirrornode.contract.ContractRepository;
import org.hiero.mirrornode.contract.ContractResult;
import org.hiero.mirrornode.contract.ContractResultDataType;
import org.hiero.mirrornode.contract.ContractState;
import org.hiero.mirrornode.contract.ContractStateChange;
import org.hiero.mirrornode.contract.Opcode;
import org.hiero.mirrornode.contract.OpcodeTrace;
import org.hiero.mirrornode.internal.Json.JsonObject;
import org.jspecify.annotations.Nullable;

/// The `ContractRepository` of the SDK, on the endpoints `/contracts/...` of the Mirror Node REST API.
public final class DefaultContractRepository extends ContractRepository {

    private final MirrorNodeRest rest;

    /// Creates the repository on `rest`.
    ///
    /// @param rest the calls against the mirror node
    /// @throws NullPointerException if `rest` is `null`
    public DefaultContractRepository(final MirrorNodeRest rest) {
        this.rest = Objects.requireNonNull(rest, "rest must not be null");
    }

    @Override
    public CompletionStage<Page<Contract>> findAll() {
        return findAll(List.of(), null, null);
    }

    @Override
    public CompletionStage<Page<Contract>> findAll(final List<String> contractId, final @Nullable Integer limit,
            final @Nullable Order order) {
        final String path = Query.of("/contracts").addAll("contract.id", contractId).add("limit", limit)
                .add("order", order).build();
        return rest.page(path, "contracts", DefaultContractRepository::contract);
    }

    @Override
    public CompletionStage<@Nullable Contract> findById(final ContractId contractId) {
        return findById(contractId, List.of());
    }

    @Override
    public CompletionStage<@Nullable Contract> findById(final ContractId contractId, final List<String> timestamp) {
        final String path = Query.of(contractPath(contractId)).addAll("timestamp", timestamp).build();
        return rest.getAndMap(path, DefaultContractRepository::contract);
    }

    @Override
    public CompletionStage<Page<ContractResult>> findResults(final ContractId contractId) {
        return findResults(contractId, List.of(), List.of(), null, null, null, null, List.of(), null);
    }

    @Override
    public CompletionStage<Page<ContractResult>> findResults(final ContractId contractId,
            final List<String> blockHash, final List<String> blockNumber, final @Nullable AccountId from,
            final @Nullable Boolean internal, final @Nullable Integer limit, final @Nullable Order order,
            final List<String> timestamp, final @Nullable Integer transactionIndex) {
        final String path = Query.of(contractPath(contractId) + "/results").addAll("block.hash", blockHash)
                .addAll("block.number", blockNumber).add("from", sender(from)).add("internal", internal)
                .add("limit", limit).add("order", order).addAll("timestamp", timestamp)
                .add("transaction.index", transactionIndex).build();
        return rest.page(path, "results", DefaultContractRepository::contractResult);
    }

    @Override
    public CompletionStage<Page<ContractState>> findState(final ContractId contractId) {
        return findState(contractId, null, null, List.of(), List.of());
    }

    @Override
    public CompletionStage<Page<ContractState>> findState(final ContractId contractId, final @Nullable Integer limit,
            final @Nullable Order order, final List<String> slot, final List<String> timestamp) {
        final String path = Query.of(contractPath(contractId) + "/state").add("limit", limit).add("order", order)
                .addAll("slot", slot).addAll("timestamp", timestamp).build();
        return rest.page(path, "state", DefaultContractRepository::contractState);
    }

    @Override
    public CompletionStage<@Nullable ContractResult> findResult(final ContractId contractId,
            final ZonedDateTime timestamp) {
        return findResult(contractId, timestamp, null);
    }

    @Override
    public CompletionStage<@Nullable ContractResult> findResult(final ContractId contractId,
            final ZonedDateTime timestamp, final @Nullable Boolean hbar) {
        Objects.requireNonNull(timestamp, "timestamp must not be null");
        final String path = Query.of(contractPath(contractId) + "/results/" + timestampText(timestamp))
                .add("hbar", hbar).build();
        return rest.getAndMap(path, DefaultContractRepository::contractResult);
    }

    @Override
    public CompletionStage<Page<ContractResult>> findAllResults() {
        return findAllResults(null, List.of(), List.of(), null, null, null, null, List.of(), null);
    }

    @Override
    public CompletionStage<Page<ContractResult>> findAllResults(final @Nullable AccountId from,
            final List<String> blockHash, final List<String> blockNumber, final @Nullable Boolean hbar,
            final @Nullable Boolean internal, final @Nullable Integer limit, final @Nullable Order order,
            final List<String> timestamp, final @Nullable Integer transactionIndex) {
        final String path = Query.of("/contracts/results").add("from", sender(from)).addAll("block.hash", blockHash)
                .addAll("block.number", blockNumber).add("hbar", hbar).add("internal", internal).add("limit", limit)
                .add("order", order).addAll("timestamp", timestamp).add("transaction.index", transactionIndex)
                .build();
        return rest.page(path, "results", DefaultContractRepository::contractResult);
    }

    @Override
    public CompletionStage<@Nullable ContractResult> findResultByTransaction(final String transactionIdOrHash) {
        return findResultByTransaction(transactionIdOrHash, null, null);
    }

    @Override
    public CompletionStage<@Nullable ContractResult> findResultByTransaction(final String transactionIdOrHash,
            final @Nullable Boolean hbar, final @Nullable Integer nonce) {
        final String path = Query.of(transactionPath(transactionIdOrHash)).add("hbar", hbar).add("nonce", nonce)
                .build();
        return rest.getAndMap(path, DefaultContractRepository::contractResult);
    }

    @Override
    public CompletionStage<Page<ContractAction>> findActions(final String transactionIdOrHash) {
        return findActions(transactionIdOrHash, List.of(), null, null);
    }

    @Override
    public CompletionStage<Page<ContractAction>> findActions(final String transactionIdOrHash,
            final List<String> index, final @Nullable Integer limit, final @Nullable Order order) {
        final String path = Query.of(transactionPath(transactionIdOrHash) + "/actions").addAll("index", index)
                .add("limit", limit).add("order", order).build();
        return rest.page(path, "actions", DefaultContractRepository::contractAction);
    }

    @Override
    public CompletionStage<@Nullable OpcodeTrace> findOpcodes(final String transactionIdOrHash) {
        return findOpcodes(transactionIdOrHash, null, null, null);
    }

    @Override
    public CompletionStage<@Nullable OpcodeTrace> findOpcodes(final String transactionIdOrHash,
            final @Nullable Boolean stack, final @Nullable Boolean memory, final @Nullable Boolean storage) {
        final String path = Query.of(transactionPath(transactionIdOrHash) + "/opcodes").add("stack", stack)
                .add("memory", memory).add("storage", storage).build();
        return rest.getAndMap(path, DefaultContractRepository::opcodeTrace);
    }

    @Override
    public CompletionStage<Page<ContractLog>> findLogs(final ContractId contractId) {
        return findLogs(contractId, List.of(), null, null, List.of(), List.of(), List.of(), List.of(), List.of());
    }

    @Override
    public CompletionStage<Page<ContractLog>> findLogs(final ContractId contractId, final List<String> index,
            final @Nullable Integer limit, final @Nullable Order order, final List<String> timestamp,
            final List<String> topic0, final List<String> topic1, final List<String> topic2,
            final List<String> topic3) {
        final String path = Query.of(contractPath(contractId) + "/results/logs").addAll("index", index)
                .add("limit", limit).add("order", order).addAll("timestamp", timestamp).addAll("topic0", topic0)
                .addAll("topic1", topic1).addAll("topic2", topic2).addAll("topic3", topic3).build();
        return rest.page(path, "logs", DefaultContractRepository::contractLog);
    }

    @Override
    public CompletionStage<Page<ContractLog>> findAllLogs() {
        return findAllLogs(List.of(), null, null, List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
    }

    @Override
    public CompletionStage<Page<ContractLog>> findAllLogs(final List<String> index, final @Nullable Integer limit,
            final @Nullable Order order, final List<String> timestamp, final List<String> topic0,
            final List<String> topic1, final List<String> topic2, final List<String> topic3,
            final List<String> transactionHash) {
        final String path = Query.of("/contracts/results/logs").addAll("index", index).add("limit", limit)
                .add("order", order).addAll("timestamp", timestamp).addAll("topic0", topic0)
                .addAll("topic1", topic1).addAll("topic2", topic2).addAll("topic3", topic3)
                .addAll("transaction.hash", transactionHash).build();
        return rest.page(path, "logs", DefaultContractRepository::contractLog);
    }

    @Override
    public CompletionStage<String> call(final ContractCallRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        final byte[] body = callBody(request).getBytes(StandardCharsets.UTF_8);
        return rest.post("/contracts/call", body, "application/json").thenApply(json -> json.string("result"));
    }

    private static String contractPath(final ContractId contractId) {
        Objects.requireNonNull(contractId, "contractId must not be null");
        return "/contracts/" + contractId;
    }

    private static String transactionPath(final String transactionIdOrHash) {
        Objects.requireNonNull(transactionIdOrHash, "transactionIdOrHash must not be null");
        return "/contracts/results/" + transactionIdOrHash;
    }

    /// The sender filter: `shard.realm.num`, or the EVM address of an account that is given by one.
    private static @Nullable String sender(final @Nullable AccountId from) {
        if (from == null) {
            return null;
        }
        final EvmAddress evmAddress = from.evmAddress();
        return from.num() == null && evmAddress != null ? evmAddress.toString() : from.toString();
    }

    /// The consensus time in the form `seconds.nanoseconds` of the REST API.
    private static String timestampText(final ZonedDateTime time) {
        final Instant instant = time.toInstant();
        return instant.getEpochSecond() + "." + String.format("%09d", instant.getNano());
    }

    /// The JSON body of a call; members that are `null` are left out.
    private static String callBody(final ContractCallRequest request) {
        final Map<String, @Nullable Object> members = new LinkedHashMap<>();
        members.put("block", request.block());
        members.put("data", request.data());
        members.put("estimate", request.estimate());
        final EvmAddress from = request.from();
        members.put("from", from == null ? null : from.toString());
        members.put("gas", request.gas());
        members.put("gasPrice", request.gasPrice());
        members.put("to", request.to().toString());
        members.put("value", request.value());
        final StringBuilder json = new StringBuilder("{");
        members.forEach((name, value) -> {
            if (value == null) {
                return;
            }
            if (json.length() > 1) {
                json.append(',');
            }
            json.append(quote(name)).append(':')
                    .append(value instanceof String text ? quote(text) : value.toString());
        });
        return json.append('}').toString();
    }

    private static String quote(final String text) {
        final StringBuilder quoted = new StringBuilder("\"");
        for (final char c : text.toCharArray()) {
            switch (c) {
                case '"' -> quoted.append("\\\"");
                case '\\' -> quoted.append("\\\\");
                default -> {
                    if (c < 0x20) {
                        quoted.append(String.format("\\u%04x", (int) c));
                    } else {
                        quoted.append(c);
                    }
                }
            }
        }
        return quoted.append('"').toString();
    }

    private static Contract contract(final JsonObject json) {
        final Long autoRenewPeriod = json.optLong("auto_renew_period");
        return new Contract(
                ContractId.fromString(json.string("contract_id")),
                Values.authority(json.optObject("admin_key")),
                Values.optAccount(json.optString("auto_renew_account")),
                autoRenewPeriod == null ? null : Duration.ofSeconds(autoRenewPeriod),
                Values.timestamp(json.optString("created_timestamp")),
                json.bool("deleted", false),
                Values.timestamp(json.optString("expiration_timestamp")),
                Values.optAddress(json.optString("file_id")),
                Values.optEvmAddress(json.optString("evm_address")),
                json.optString("memo"),
                optInt(json, "max_automatic_token_associations"),
                json.optLong("nonce"),
                Values.optAddress(json.optString("obtainer_id")),
                json.optBoolean("permanent_removal"),
                Values.optAccount(json.optString("proxy_account_id")),
                Values.range(json, "timestamp"),
                json.optString("bytecode"),
                json.optString("runtime_bytecode"));
    }

    private static ContractResult contractResult(final JsonObject json) {
        return new ContractResult(
                json.objects("access_list").stream().map(DefaultContractRepository::accessListEntry).toList(),
                Values.optEvmAddress(json.optString("address")),
                json.objects("authorization_list").stream()
                        .map(DefaultContractRepository::authorizationListEntry).toList(),
                json.optInteger("amount"),
                json.optLong("block_gas_used"),
                json.optString("block_hash"),
                json.optLong("block_number"),
                json.optString("bloom"),
                json.optString("call_result"),
                json.optString("chain_id"),
                optContract(json.optString("contract_id")),
                strings(json, "created_contract_ids").stream().map(ContractId::fromString).toList(),
                json.optString("error_message"),
                json.optString("failed_initcode"),
                Values.optEvmAddress(json.optString("from")),
                json.optString("function_parameters"),
                json.optLong("gas_consumed"),
                json.longValue("gas_limit"),
                json.optString("gas_price"),
                json.optLong("gas_used"),
                json.string("hash"),
                json.optString("max_fee_per_gas"),
                json.optString("max_priority_fee_per_gas"),
                json.optLong("nonce"),
                json.optString("r"),
                json.string("result"),
                json.optString("s"),
                json.string("status"),
                Values.requiredTimestamp(json, "timestamp"),
                Values.optEvmAddress(json.optString("to")),
                json.optLong("transaction_index"),
                optInt(json, "type"),
                optInt(json, "v"),
                json.objects("logs").stream().map(DefaultContractRepository::contractLog).toList(),
                json.objects("state_changes").stream().map(DefaultContractRepository::stateChange).toList());
    }

    private static AccessListEntry accessListEntry(final JsonObject json) {
        return new AccessListEntry(evmAddress(json, "address"), strings(json, "storage_keys"));
    }

    private static AuthorizationListEntry authorizationListEntry(final JsonObject json) {
        return new AuthorizationListEntry(evmAddress(json, "address"), json.string("chain_id"),
                json.longValue("nonce"), json.string("r"), json.string("s"), json.string("y_parity"));
    }

    private static ContractLog contractLog(final JsonObject json) {
        return new ContractLog(
                evmAddress(json, "address"),
                json.optString("bloom"),
                optContract(json.optString("contract_id")),
                json.optString("data"),
                Math.toIntExact(json.longValue("index")),
                strings(json, "topics"),
                json.optString("block_hash"),
                json.optLong("block_number"),
                optContract(json.optString("root_contract_id")),
                Values.timestamp(json.optString("timestamp")),
                json.optString("transaction_hash"),
                optInt(json, "transaction_index"));
    }

    private static ContractStateChange stateChange(final JsonObject json) {
        return new ContractStateChange(evmAddress(json, "address"),
                ContractId.fromString(json.string("contract_id")), json.string("slot"), json.string("value_read"),
                json.optString("value_written"));
    }

    private static ContractState contractState(final JsonObject json) {
        return new ContractState(evmAddress(json, "address"), ContractId.fromString(json.string("contract_id")),
                Values.requiredTimestamp(json, "timestamp"), json.string("slot"), json.string("value"));
    }

    private static ContractAction contractAction(final JsonObject json) {
        final String recipientType = json.optString("recipient_type");
        return new ContractAction(
                Math.toIntExact(json.longValue("call_depth")),
                ContractCallOperationType.valueOf(json.string("call_operation_type")),
                ContractActionType.valueOf(json.string("call_type")),
                Values.optAddress(json.optString("caller")),
                ContractEntityType.valueOf(json.string("caller_type")),
                evmAddress(json, "from"),
                json.longValue("gas"),
                json.longValue("gas_used"),
                Math.toIntExact(json.longValue("index")),
                json.optString("input"),
                Values.optAddress(json.optString("recipient")),
                recipientType == null ? null : ContractEntityType.valueOf(recipientType),
                json.optString("result_data"),
                ContractResultDataType.valueOf(json.string("result_data_type")),
                Values.requiredTimestamp(json, "timestamp"),
                Values.optEvmAddress(json.optString("to")),
                json.longValue("value"));
    }

    private static OpcodeTrace opcodeTrace(final JsonObject json) {
        return new OpcodeTrace(
                evmAddress(json, "address"),
                optContract(json.optString("contract_id")),
                json.bool("failed", false),
                json.longValue("gas"),
                json.objects("opcodes").stream().map(DefaultContractRepository::opcode).toList(),
                json.string("return_value"));
    }

    private static Opcode opcode(final JsonObject json) {
        final JsonObject storage = json.optObject("storage");
        final Map<String, String> slots = new LinkedHashMap<>();
        if (storage != null) {
            storage.members().forEach((slot, value) -> {
                if (value != null) {
                    slots.put(slot, storage.string(slot));
                }
            });
        }
        return new Opcode(
                Math.toIntExact(json.longValue("depth")),
                json.longValue("gas"),
                json.longValue("gas_cost"),
                strings(json, "memory"),
                json.string("op"),
                Math.toIntExact(json.longValue("pc")),
                json.optString("reason"),
                strings(json, "stack"),
                slots);
    }

    private static EvmAddress evmAddress(final JsonObject json, final String name) {
        return Objects.requireNonNull(Values.optEvmAddress(json.string(name)));
    }

    private static @Nullable ContractId optContract(final @Nullable String text) {
        return text == null || text.isEmpty() ? null : ContractId.fromString(text);
    }

    private static @Nullable Integer optInt(final JsonObject json, final String name) {
        final Long value = json.optLong(name);
        return value == null ? null : Math.toIntExact(value);
    }

    /// The strings of the array member `name`; an absent or `null` member is an empty list.
    private static List<String> strings(final JsonObject json, final String name) {
        final Object value = json.opt(name);
        if (value == null) {
            return List.of();
        }
        if (!(value instanceof List<?> elements)) {
            throw new IllegalArgumentException("member '" + name + "' is no array");
        }
        final List<String> result = new ArrayList<>(elements.size());
        for (final Object element : elements) {
            if (!(element instanceof String text)) {
                throw new IllegalArgumentException("member '" + name + "' holds an element that is no string");
            }
            result.add(text);
        }
        return List.copyOf(result);
    }
}
