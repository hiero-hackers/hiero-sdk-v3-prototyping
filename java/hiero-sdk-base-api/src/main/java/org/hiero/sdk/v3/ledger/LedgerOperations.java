// Generated from spec/base/ledger.md (ledger#LedgerOperations); DO NOT EDIT.

package org.hiero.sdk.v3.ledger;

/** Body-free checksum and formatting contracts. */
public interface LedgerOperations {
    boolean validateChecksum(BaseAddress address, Network<?> network);
    String toCanonicalString(BaseAddress address);
    String toStringWithChecksum(BaseAddress address);
    String toCanonicalString(EvmAddress address);
    String toCanonicalString(TransactionId transactionId);
    String toStringWithChecksum(TransactionId transactionId);
    String toCanonicalString(IpAddress address);
}
