package org.hiero.mirrornode.contract;

import java.util.List;
import java.util.Objects;
import org.hiero.ledger.EvmAddress;

/// A contract and the storage keys of it that a wrapped Ethereum transaction accesses (EIP-2930).
///
/// @param address the accessed contract
/// @param storageKeys the accessed storage keys in hex
public record AccessListEntry(EvmAddress address, List<String> storageKeys) {

    /// Creates a new `AccessListEntry`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public AccessListEntry {
        Objects.requireNonNull(address, "address must not be null");
        Objects.requireNonNull(storageKeys, "storageKeys must not be null");
        storageKeys = List.copyOf(storageKeys);
    }

    /// Creates a new `AccessListEntry` with the default value of `storageKeys`.
    public AccessListEntry(final EvmAddress address) {
        this(address, List.of());
    }
}
