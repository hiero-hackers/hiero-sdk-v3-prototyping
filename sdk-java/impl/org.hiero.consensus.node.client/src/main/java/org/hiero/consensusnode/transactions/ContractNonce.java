package org.hiero.consensusnode.transactions;

import java.util.Objects;
import org.hiero.ledger.ContractId;

/// The nonce of a contract after an execution. The nonce counts the contracts the contract created.
public record ContractNonce(ContractId contractId, long nonce) {

    /// Creates a new `ContractNonce`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public ContractNonce {
        Objects.requireNonNull(contractId, "contractId must not be null");
    }
}
