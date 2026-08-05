import type { NativeTokenUnit } from './nativeToken.js';
import { IllegalFormatError } from './keys.js';
import {
    AddressImpl,
    EvmAddressImpl,
    ContractIdImpl,
    AccountIdImpl,
    IpAddressImpl,
    TransactionIdImpl
} from './internal/ledger-impl.js';

export type ZonedDateTime = Date;

/**
 * Represents a specific network instance.
 * @see {@link file://../../spec/base/ledger.md}
 */
export interface Network<Unit extends NativeTokenUnit> {
    readonly id: Uint8Array;
    readonly name: string | null;
    readonly nativeTokenUnit: Unit;
}

/**
 * Abstract base for every entity identifier that lives in the (shard, realm)-space of a Hiero ledger.
 * @see {@link file://../../spec/base/ledger.md}
 */
export interface BaseAddress {
    readonly shard: bigint;
    readonly realm: bigint;
    readonly checksum: string;
    readonly num: bigint | null;

    validateChecksum(network: Network<any>): boolean;
    toString(): string;
    toStringWithChecksum(): string;
}

/**
 * Concrete address with a single numeric selector.
 * @see {@link file://../../spec/base/ledger.md}
 */
export interface Address extends BaseAddress {
    readonly num: bigint;
}

export const Address = {
    /**
     * Parses Address from string format: "shard.realm.num" or "shard.realm.num-checksum"
     * @param address - The string to parse.
     * @returns A parsed Address instance.
     * @throws {IllegalFormatError} if format is invalid, values are negative, or parsing fails.
     */
    fromString(address: string): Address {
        return AddressImpl.fromString(address);
    }
};

/**
 * 20-byte EVM address.
 * @see {@link file://../../spec/base/ledger.md}
 */
export interface EvmAddress {
    readonly bytes: Uint8Array;
    toString(): string;
}

export const EvmAddress = {
    /**
     * Parses an EvmAddress from its hex-string form.
     * @param value - The hex string ("0xabc..." or "abc...").
     * @returns A parsed EvmAddress.
     * @throws {IllegalFormatError} on invalid length or non-hex characters.
     */
    fromString(value: string): EvmAddress {
        return EvmAddressImpl.fromString(value);
    },
    
    /**
     * Wraps raw bytes into an EvmAddress.
     * @param value - 20 raw bytes.
     * @returns An EvmAddress instance.
     * @throws {IllegalFormatError} if value length is not exactly 20.
     */
    fromBytes(value: Uint8Array): EvmAddress {
        return EvmAddressImpl.fromBytes(value);
    }
};

/**
 * Abstract subtype of BaseAddress for entities that can be addressed either by their numeric Hiero id or by a 20-byte EVM address.
 * @see {@link file://../../spec/base/ledger.md}
 */
export interface EvmCapableAddress extends BaseAddress {
    readonly evmAddress: EvmAddress | null;
}

/**
 * Identifier of a smart contract.
 * @see {@link file://../../spec/base/ledger.md}
 */
export interface ContractId extends EvmCapableAddress {}

export const ContractId = {
    /**
     * Parses "shard.realm.num" or "shard.realm.0x<40-hex>".
     * @param value - The string to parse.
     * @returns A ContractId instance.
     * @throws {IllegalFormatError} if format is invalid.
     */
    fromString(value: string): ContractId {
        return ContractIdImpl.fromString(value);
    },

    /**
     * Builds a ContractId from a 20-byte EVM address.
     * @param shard - The shard number.
     * @param realm - The realm number.
     * @param address - The EVM address.
     * @returns A ContractId instance.
     */
    fromEvmAddress(shard: bigint, realm: bigint, address: EvmAddress): ContractId {
        return ContractIdImpl.fromEvmAddress(shard, realm, address);
    }
};

/**
 * Identifier of an account.
 * @see {@link file://../../spec/base/ledger.md}
 */
export interface AccountId extends EvmCapableAddress {
    readonly alias: Uint8Array | null;
}

export const AccountId = {
    /**
     * Parses "shard.realm.num", "shard.realm.0x<40-hex>", or "shard.realm.<base32 key alias>".
     * @param value - The string to parse.
     * @returns An AccountId instance.
     * @throws {IllegalFormatError} if format is invalid.
     */
    fromString(value: string): AccountId {
        return AccountIdImpl.fromString(value);
    },

    /**
     * Builds an AccountId from a 20-byte EVM address (HIP-583 auto-create form).
     * @param shard - The shard number.
     * @param realm - The realm number.
     * @param address - The EVM address.
     * @returns An AccountId instance.
     */
    fromEvmAddress(shard: bigint, realm: bigint, address: EvmAddress): AccountId {
        return AccountIdImpl.fromEvmAddress(shard, realm, address);
    }
};

/**
 * Id of a transaction.
 * @see {@link file://../../spec/base/ledger.md}
 */
export interface TransactionId {
    readonly accountId: AccountId;
    readonly validStart: ZonedDateTime;
    readonly nonce: number | null;

    toString(): string;
    toStringWithChecksum(): string;
}

export const TransactionId = {
    /**
     * Generates a new transaction ID.
     * @param accountId - The account ID of the payer.
     * @returns A newly generated TransactionId.
     */
    generateTransactionId(accountId: Address): TransactionId {
        // NOTE: Spec specifies accountId: Address but TransactionId interface requires accountId: AccountId.
        // In the spec, `generateTransactionId(accountId:Address)` is written. Wait, TransactionId interface has `accountId:AccountId`.
        // I will use AccountId here or just accept Address and convert, but typically it should be AccountId.
        // Will follow the spec signature: `accountId: Address`.
        return TransactionIdImpl.generateTransactionId(accountId);
    },

    /**
     * Parses a transaction ID from string.
     * @param transactionId - The transaction ID string.
     * @returns A parsed TransactionId.
     * @throws {IllegalFormatError} if format is invalid.
     */
    fromString(transactionId: string): TransactionId {
        return TransactionIdImpl.fromString(transactionId);
    }
};

/**
 * Single IP address representation (IPv4).
 * @see {@link file://../../spec/base/ledger.md}
 */
export interface IpAddress {
    readonly bytes: Uint8Array;
    toString(): string;
}

export const IpAddress = {
    /**
     * Parses an IpAddress from textual form (e.g., "10.0.0.7").
     * @param value - The IP string.
     * @returns A parsed IpAddress.
     * @throws {IllegalFormatError} if format is invalid.
     */
    fromString(value: string): IpAddress {
        return IpAddressImpl.fromString(value);
    },

    /**
     * Wraps raw network-order bytes.
     * @param value - The 4-byte IP.
     * @returns A parsed IpAddress.
     * @throws {IllegalFormatError} if value.length is not 4.
     */
    fromBytes(value: Uint8Array): IpAddress {
        return IpAddressImpl.fromBytes(value);
    }
};

/**
 * Represents a consensus node on a network.
 * @see {@link file://../../spec/base/ledger.md}
 */
export interface ConsensusNode {
    readonly ip: IpAddress;
    readonly port: number; // uint16
    readonly account: AccountId;
}

/**
 * Represents a mirror node on a network.
 * @see {@link file://../../spec/base/ledger.md}
 */
export interface MirrorNode {
    readonly restBaseUrl: string;
}

// ============================================================================
// Sentinels
// ============================================================================

export const ZERO_ADDRESS: Address = AddressImpl.createZero();
export const ZERO_ACCOUNT_ID: AccountId = AccountIdImpl.createZero();
export const ZERO_CONTRACT_ID: ContractId = ContractIdImpl.createZero();
