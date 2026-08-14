// Source: spec/base/ledger.md (ledger#validateChecksum).

package org.hiero.sdk.v3.ledger;

/** Operational checksum contract implemented by a later provider module. */
public interface LedgerOperations {
    /** Validates an address checksum using the supplied network's checksum scheme. */
    boolean validateChecksum(BaseAddress address, Network<?> network);
}
