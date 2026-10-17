package org.hiero.enterprise.service.contract;

import org.hiero.common.Page;
import org.hiero.enterprise.service.ServiceException;
import org.hiero.enterprise.service.internal.Unbound;
import org.hiero.ledger.Address;
import org.hiero.ledger.ContractId;
import org.jspecify.annotations.Nullable;

public class SmartContractService {

    /// Creates a new `SmartContractService` that is not bound to a session: every method fails with `ServiceException`.
    /// A usable service is created with `ContractFactory.createService(session)`.
    public SmartContractService() {
    }

    /// Create a contract from the bytecode in a file or from the bytecode itself. The same as createContract with
    /// ContractCreateParams that have only these values.
    ///
    /// @throws ServiceException if a service error occurs
    public Contract createContract(final Address fileId, final Param<?, ?>... constructorParams) {
        throw Unbound.error("SmartContractService", "ContractFactory");
    }

    /// @throws ServiceException if a service error occurs
    public Contract createContract(final byte[] contents, final Param<?, ?>... constructorParams) {
        throw Unbound.error("SmartContractService", "ContractFactory");
    }

    /// Create a contract with the given parameters: the general form of the methods above, for every option of a new
    /// contract.
    ///
    /// @throws ServiceException if a service error occurs
    public Contract createContract(final ContractCreateParams params) {
        throw Unbound.error("SmartContractService", "ContractFactory");
    }

    /// @throws ServiceException if a service error occurs
    public ContractCallResult callContractFunction(final Contract contract, final String functionName, final Param<?, ?>... params) {
        throw Unbound.error("SmartContractService", "ContractFactory");
    }

    /// @throws ServiceException if a service error occurs
    public ContractCallResult callContractFunction(final ContractId contractId, final String functionName, final Param<?, ?>... params) {
        throw Unbound.error("SmartContractService", "ContractFactory");
    }

    /// Return the contract information for the given contract id
    ///
    /// @throws ServiceException if a service error occurs
    public @Nullable Contract findById(final ContractId contractId) {
        throw Unbound.error("SmartContractService", "ContractFactory");
    }

    /// Return all known contracts
    ///
    /// @throws ServiceException if a service error occurs
    public Page<Contract> findAll() {
        throw Unbound.error("SmartContractService", "ContractFactory");
    }
}
