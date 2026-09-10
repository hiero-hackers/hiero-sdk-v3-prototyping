// Source: spec/base/ledger.md (ledger#LedgerFactory).

package org.hiero.sdk.v3.ledger;

/** Body-free ledger parsing, provider construction, and transaction-id contracts. */
public interface LedgerFactory {
    /** Parses a hexadecimal EVM address; throws {@link IllegalArgumentException} for illegal format. */
    EvmAddress evmAddressFromString(String value);
    /** Creates a provider EVM-address instance from exactly twenty raw bytes. */
    EvmAddress evmAddressFromBytes(byte[] value);
    /** Parses a contract identifier; throws {@link IllegalArgumentException} for illegal format. */
    ContractId contractIdFromString(String value);
    /** Parses an account identifier; throws {@link IllegalArgumentException} for illegal format. */
    AccountId accountIdFromString(String value);
    /** Parses an IP address; throws {@link IllegalArgumentException} for illegal format. */
    IpAddress ipAddressFromString(String value);
    /** Parses a numeric address; throws {@link IllegalArgumentException} for illegal format. */
    Address addressFromString(String value);
    /** Generates a transaction identifier using provider clock and randomness policy. */
    TransactionId generateTransactionId(Address accountId);
    /** Parses a transaction identifier; throws {@link IllegalArgumentException} for illegal format. */
    TransactionId transactionIdFromString(String value);
}
