package org.hiero.mirrornode.internal;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import org.hiero.common.Page;
import org.hiero.ledger.ContractId;
import org.hiero.mirrornode.contract.Contract;
import org.hiero.mirrornode.contract.ContractRepository;
import org.hiero.mirrornode.internal.Json.JsonObject;
import org.jspecify.annotations.Nullable;

/// The `ContractRepository` of the SDK, on the endpoint `/contracts` of the Mirror Node REST API.
///
/// The REST API returns the validity of the contract state as an open range for the current state; its end is then
/// `Values.OPEN_END`, a missing auto-renew period is zero and a missing `permanent_removal` is `false` (see the
/// `## Questions & Comments` of the spec).
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
        return rest.page("/contracts", "contracts", DefaultContractRepository::contract);
    }

    @Override
    public CompletionStage<@Nullable Contract> findById(final ContractId contractId) {
        Objects.requireNonNull(contractId, "contractId must not be null");
        return rest.getAndMap("/contracts/" + contractId, DefaultContractRepository::contract);
    }

    private static Contract contract(final JsonObject json) {
        final Long autoRenewPeriod = json.optLong("auto_renew_period");
        final Long maxAssociations = json.optLong("max_automatic_token_associations");
        final ZonedDateTime from = Values.rangeFrom(json, "timestamp");
        final ZonedDateTime created = Values.timestamp(json.optString("created_timestamp"));
        return new Contract(
                ContractId.fromString(json.string("contract_id")),
                Values.authority(json.optObject("admin_key")),
                Values.optAccount(json.optString("auto_renew_account")),
                Duration.ofSeconds(autoRenewPeriod == null ? 0L : autoRenewPeriod),
                created == null ? from : created,
                json.bool("deleted", false),
                Values.timestamp(json.optString("expiration_timestamp")),
                json.optString("file_id"),
                Values.optEvmAddress(json.optString("evm_address")),
                json.optString("memo"),
                maxAssociations == null ? null : Math.toIntExact(maxAssociations),
                json.optLong("nonce"),
                json.optString("obtainer_id"),
                json.bool("permanent_removal", false),
                json.optString("proxy_account_id"),
                from,
                Values.rangeTo(json, "timestamp"),
                json.optString("bytecode"),
                json.optString("runtime_bytecode"));
    }
}
