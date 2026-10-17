package org.hiero.enterprise.service.internal;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.hiero.common.Page;
import org.hiero.enterprise.service.ServiceException;
import org.hiero.enterprise.service.contract.Contract;
import org.hiero.enterprise.service.contract.ContractCallResult;
import org.hiero.enterprise.service.contract.ContractCreateParams;
import org.hiero.enterprise.service.contract.Param;
import org.hiero.enterprise.service.contract.SmartContractService;
import org.hiero.ledger.Address;
import org.hiero.ledger.ContractId;
import org.jspecify.annotations.Nullable;

/// The `SmartContractService` of a session. The parameters of a call are ABI-encoded; the consensus node client has no
/// transaction to create a contract or to call one yet, so both fail with a `ServiceException` after the parameters
/// have been checked. The lookups read the Mirror Node.
public final class DefaultSmartContractService extends SmartContractService {

    private final DefaultSession session;

    /// Creates the service of a session.
    ///
    /// @param session the session
    public DefaultSmartContractService(final DefaultSession session) {
        this.session = Objects.requireNonNull(session, "session must not be null");
    }

    @Override
    public Contract createContract(final Address fileId, final Param<?, ?>... constructorParams) {
        Objects.requireNonNull(fileId, "fileId must not be null");
        return createContract(ContractCreateParams.builder().fileId(fileId).constructorParams(params(constructorParams))
                .build());
    }

    @Override
    public Contract createContract(final byte[] contents, final Param<?, ?>... constructorParams) {
        Objects.requireNonNull(contents, "contents must not be null");
        return createContract(ContractCreateParams.builder().bytecode(contents)
                .constructorParams(params(constructorParams)).build());
    }

    @Override
    public Contract createContract(final ContractCreateParams params) {
        Objects.requireNonNull(params, "params must not be null");
        Abi.encodeArguments(params.constructorParams());
        throw new ServiceException("The consensus node client offers no transaction to create a smart contract", null);
    }

    @Override
    public ContractCallResult callContractFunction(final Contract contract, final String functionName,
            final Param<?, ?>... params) {
        Objects.requireNonNull(contract, "contract must not be null");
        return callContractFunction(contract.contractId(), functionName, params);
    }

    @Override
    public ContractCallResult callContractFunction(final ContractId contractId, final String functionName,
            final Param<?, ?>... params) {
        Objects.requireNonNull(contractId, "contractId must not be null");
        Abi.encodeCall(functionName, params(params));
        throw new ServiceException("The consensus node client offers no transaction to call a smart contract", null);
    }

    @Override
    public @Nullable Contract findById(final ContractId contractId) {
        Objects.requireNonNull(contractId, "contractId must not be null");
        final org.hiero.mirrornode.contract.Contract contract =
                session.read(mirror -> mirror.contracts().findById(contractId));
        return contract == null ? null : new Contract(contract.contractId());
    }

    @Override
    public Page<Contract> findAll() {
        return MappedPage.of(session.read(mirror -> mirror.contracts().findAll()),
                contract -> new Contract(contract.contractId()));
    }

    private static List<Param<?, ?>> params(final Param<?, ?>... params) {
        Objects.requireNonNull(params, "params must not be null");
        return List.copyOf(Arrays.asList(params));
    }
}
