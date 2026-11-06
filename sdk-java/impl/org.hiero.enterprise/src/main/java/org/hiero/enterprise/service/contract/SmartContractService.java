package org.hiero.enterprise.service.contract;

import org.hiero.common.Page;
import org.hiero.enterprise.service.ServiceException;
import org.hiero.enterprise.service.internal.Unbound;
import org.hiero.evm.AbiFunction;
import org.hiero.evm.AbiValue;
import org.hiero.ledger.Address;
import org.hiero.ledger.ContractId;
import org.jspecify.annotations.Nullable;

public class SmartContractService {

    /// Creates a new `SmartContractService` that is not bound to a session: every method fails with `ServiceException`.
    /// A usable service is created with `ContractFactory.createService(session)`.
    public SmartContractService() {
    }

    /// Create a contract from the bytecode in a file or from the bytecode itself. The same as createContract with
    /// ContractCreateParams that have only these arguments.
    ///
    /// @throws ServiceException if a service error occurs
    public Contract createContract(final Address fileId, final AbiValue... constructorArguments) {
        throw Unbound.error("SmartContractService", "ContractFactory");
    }

    /// @throws ServiceException if a service error occurs
    public Contract createContract(final byte[] contents, final AbiValue... constructorArguments) {
        throw Unbound.error("SmartContractService", "ContractFactory");
    }

    /// Create a contract from the bytecode in a file or from the bytecode itself, with every option of a new contract:
    /// the general form of the methods above.
    ///
    /// @throws ServiceException if a service error occurs
    public Contract createContract(final Address fileId, final ContractCreateParams params) {
        throw Unbound.error("SmartContractService", "ContractFactory");
    }

    /// @throws ServiceException if a service error occurs
    public Contract createContract(final byte[] contents, final ContractCreateParams params) {
        throw Unbound.error("SmartContractService", "ContractFactory");
    }

    /// Calls a function of a contract in a transaction, paid by the operator of the session. Throws if the arguments
    /// do not have the argument types of the function.
    ///
    /// @throws ServiceException if a service error occurs
    public ContractExecuteResult executeFunction(final ContractId contractId, final AbiFunction function, final AbiValue... arguments) {
        throw Unbound.error("SmartContractService", "ContractFactory");
    }

    /// Calls a function of a contract without a transaction, on the Mirror Node; the state of the contract is not
    /// changed. Throws if the arguments do not have the argument types of the function.
    ///
    /// @throws ServiceException if a service error occurs
    public ContractCallResult callFunction(final ContractId contractId, final AbiFunction function, final AbiValue... arguments) {
        throw Unbound.error("SmartContractService", "ContractFactory");
    }

    /// The gas a call of the function with the arguments needs, estimated by the Mirror Node.
    ///
    /// @throws ServiceException if a service error occurs
    public long estimateGas(final ContractId contractId, final AbiFunction function, final AbiValue... arguments) {
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
