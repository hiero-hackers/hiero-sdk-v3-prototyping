// Source: spec/base/ledger.md (ledger#LedgerConstants).

package org.hiero.sdk.v3.ledger;

/** Structural ledger sentinel values. */
public final class LedgerConstants {
    /** Numeric zero-address clear sentinel. */ public static final Address ZERO_ADDRESS = new Address(0, 0, 0, "");
    /** Numeric zero-account clear sentinel. */ public static final AccountId ZERO_ACCOUNT_ID = new AccountId(0, 0, 0L, "", null, null);
    /** Numeric zero-contract clear sentinel. */ public static final ContractId ZERO_CONTRACT_ID = new ContractId(0, 0, 0L, "", null);
    private LedgerConstants() {}
}
