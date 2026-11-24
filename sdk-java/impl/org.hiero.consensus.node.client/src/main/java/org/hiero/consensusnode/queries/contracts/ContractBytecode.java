package org.hiero.consensusnode.queries.contracts;

import java.util.Arrays;
import java.util.Objects;
import org.hiero.ledger.ContractId;
import org.jspecify.annotations.Nullable;

/// The runtime bytecode of a contract. Returned by `ContractBytecodeQuery`.
///
/// @param contractId the contract this bytecode belongs to
/// @param bytecode the code the EVM executes, not the initcode
public record ContractBytecode(ContractId contractId, byte[] bytecode) {

    /// Creates a new `ContractBytecode`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public ContractBytecode {
        Objects.requireNonNull(contractId, "contractId must not be null");
        Objects.requireNonNull(bytecode, "bytecode must not be null");
        bytecode = bytecode.clone();
    }

    /// the code the EVM executes, not the initcode
    ///
    /// Returns a copy of the `bytecode` array.
    @Override
    public byte[] bytecode() {
        return bytecode.clone();
    }

    /// Compares `bytecode` by content: the implicit `equals` of a record compares
    /// an array by reference, so two instances with the same values would never be equal (each holds its
    /// own copy of the array).
    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof ContractBytecode other
            && Objects.equals(contractId, other.contractId)
            && Arrays.equals(bytecode, other.bytecode);
    }

    /// Hashes `bytecode` by content, consistent with [#equals(Object)].
    @Override
    public int hashCode() {
        return Objects.hash(contractId, Arrays.hashCode(bytecode));
    }

    /// Shows only the length of `bytecode` instead of the identity of the array object.
    @Override
    public String toString() {
        return "ContractBytecode[contractId=" + contractId + ", bytecode=" + "byte[" + bytecode.length + "]" + "]";
    }
}
