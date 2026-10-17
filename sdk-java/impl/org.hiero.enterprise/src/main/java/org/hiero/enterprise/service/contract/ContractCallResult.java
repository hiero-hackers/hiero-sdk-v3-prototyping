package org.hiero.enterprise.service.contract;


/// The values a contract function returned; created by the SDK from the result of a call.
public abstract class ContractCallResult {

    private final short size;

    /// Creates a new `ContractCallResult`.
    ///
    /// @throws IllegalArgumentException if a value violates its constraints
    protected ContractCallResult(final short size) {
        if (size < (short) 0 || size > (short) 255) {
            throw new IllegalArgumentException("size must be between 0 and 255");
        }
        this.size = size;
    }

    /// Returns the `size`.
    public short size() {
        return size;
    }

    /// Runtime type of the result value at `index`; use it to check the value returned by get(index).
    public abstract Class<?> getType(final short index);

    /// Returns the result value at `index`. Its type depends on the called Solidity function and is only known at
    /// runtime; check it with getType(index) before using the value.
    public abstract Object get(final short index);
}
