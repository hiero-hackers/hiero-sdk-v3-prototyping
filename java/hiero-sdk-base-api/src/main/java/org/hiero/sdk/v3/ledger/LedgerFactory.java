// Generated from spec/base/ledger.md (ledger#LedgerFactory); DO NOT EDIT.

package org.hiero.sdk.v3.ledger;

/** Body-free ledger value construction and parsing contracts. */
public interface LedgerFactory {
    EvmAddress evmAddressFromString(String value);
    EvmAddress evmAddressFromBytes(byte[] value);
    ContractId contractIdFromString(String value);
    ContractId contractIdFromEvmAddress(long shard, long realm, EvmAddress address);
    AccountId accountIdFromString(String value);
    AccountId accountIdFromEvmAddress(long shard, long realm, EvmAddress address);
    IpAddress ipAddressFromString(String value);
    IpAddress ipAddressFromBytes(byte[] value);
    Address addressFromString(String value);
    TransactionId generateTransactionId(Address accountId);
    TransactionId transactionIdFromString(String value);
}
