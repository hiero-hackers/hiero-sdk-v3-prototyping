package org.hiero.enterprise.service.contract;

import java.util.Objects;
import org.hiero.ledger.ContractId;

public record Contract(ContractId contractId) {

    /// Creates a new `Contract`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public Contract {
        Objects.requireNonNull(contractId, "contractId must not be null");
    }
}
