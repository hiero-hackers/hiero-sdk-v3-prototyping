import type { NativeTokenUnit } from './nativeToken.js';
import { NetworkImpl, calculateHip15Checksum } from './internal/ledger-impl.js';

/**
 * Error thrown when an entity string or address does not conform to the expected format.
 */
export class IllegalFormatError extends Error {
    constructor(message: string) {
        super(message);
        this.name = 'IllegalFormatError';
    }
}

/**
 * Represents the identity of a network, including its ledger ID, name, and the native token unit it uses.
 */
export interface Network<Unit extends NativeTokenUnit = NativeTokenUnit> {
    /**
     * The unique identifier bytes of the network (network byte order).
     * Defensively copied to ensure immutability.
     */
    readonly id: Uint8Array;

    /**
     * Optional human readable name of the network, or null if unnamed.
     */
    readonly name: string | null;

    /**
     * The native token unit instance associated with this network.
     */
    readonly nativeTokenUnit: Unit;
}

/**
 * Creates an immutable Network identity instance.
 *
 * @param id - The network identifier bytes (must not be undefined or null).
 * @param name - The optional network name (must be string or null; undefined is rejected).
 * @param nativeTokenUnit - The native token unit instance (must not be undefined or null).
 * @returns An immutable Network instance.
 */
export function createNetwork<Unit extends NativeTokenUnit>(
    id: Uint8Array,
    name: string | null,
    nativeTokenUnit: Unit
): Network<Unit> {
    return new NetworkImpl(id, name, nativeTokenUnit);
}

/**
 * Abstract base for every entity identifier that lives in the (shard, realm)-space of a Hiero ledger.
 */
export abstract class BaseAddress {
    readonly #shard: bigint;
    readonly #realm: bigint;
    readonly #checksum: string;
    readonly #num: bigint | null;

    /**
     * Protected constructor for base address types.
     */
    protected constructor(
        shard: bigint | number,
        realm: bigint | number,
        num: bigint | number | null,
        checksum: string = ''
    ) {
        if (shard === undefined || shard === null) {
            throw new TypeError('Shard must not be null or undefined');
        }
        if (realm === undefined || realm === null) {
            throw new TypeError('Realm must not be null or undefined');
        }
        if (checksum === undefined || checksum === null) {
            throw new TypeError('Checksum must not be null or undefined');
        }

        const s = BigInt(shard);
        const r = BigInt(realm);
        if (s < 0n || r < 0n) {
            throw new IllegalFormatError('Shard and realm must be non-negative');
        }

        let n: bigint | null = null;
        if (num !== null) {
            n = BigInt(num);
            if (n < 0n) {
                throw new IllegalFormatError('Entity number must be non-negative');
            }
        }

        this.#shard = s;
        this.#realm = r;
        this.#num = n;
        this.#checksum = checksum;
    }

    /**
     * Shard number.
     */
    get shard(): bigint {
        return this.#shard;
    }

    /**
     * Realm number.
     */
    get realm(): bigint {
        return this.#realm;
    }

    /**
     * Protocol-side checksum over the (shard, realm, selector) form; empty string when no checksum applies.
     */
    get checksum(): string {
        return this.#checksum;
    }

    /**
     * Hiero entity number. Nullable on BaseAddress, tightened to non-null on Address.
     */
    get num(): bigint | null {
        return this.#num;
    }

    /**
     * Validates the checksum against the given network's checksum scheme.
     *
     * @param network - The network to validate against.
     * @returns True if the checksum matches the expected checksum on this network, or if no checksum is present.
     */
    validateChecksum(network: Network<any>): boolean {
        if (network === undefined || network === null) {
            throw new TypeError('Network must not be null or undefined');
        }
        if (this.#checksum === '' || this.#num === null) {
            return true;
        }
        const expected = calculateHip15Checksum(this.toString(), network.id);
        return this.#checksum === expected;
    }

    /**
     * Canonical string form ("shard.realm.num").
     */
    toString(): string {
        return `${this.#shard}.${this.#realm}.${this.#num ?? '?'}`;
    }

    /**
     * toString() with an appended "-<checksum>" suffix when a non-empty checksum is set.
     */
    toStringWithChecksum(): string {
        const canonical = this.toString();
        return this.#checksum ? `${canonical}-${this.#checksum}` : canonical;
    }
}

/**
 * Concrete address with a single numeric selector. Used for entities whose HAPI id is purely
 * (shard, realm, num): tokens, topics, files, schedules.
 */
export class Address extends BaseAddress {
    /**
     * Creates an immutable Address instance.
     */
    constructor(
        shard: bigint | number,
        realm: bigint | number,
        num: bigint | number,
        checksum: string = ''
    ) {
        if (num === null || num === undefined) {
            throw new TypeError('Address num must not be null or undefined');
        }
        super(shard, realm, num, checksum);
        Object.freeze(this);
    }

    /**
     * Tightened to non-nullable on Address.
     */
    override get num(): bigint {
        return super.num as bigint;
    }

    /**
     * Parses an Address from a string format: "shard.realm.num" or "shard.realm.num-checksum".
     *
     * @param address - The address string to parse.
     * @returns An immutable Address instance.
     * @throws IllegalFormatError if the format is invalid or values are negative.
     */
    static fromString(address: string): Address {
        if (typeof address !== 'string') {
            throw new IllegalFormatError('Address string must be a string');
        }
        const match = address.match(/^(\d+)\.(\d+)\.(\d+)(?:-([a-z]{5}))?$/);
        if (!match) {
            throw new IllegalFormatError(`Invalid address format: '${address}'`);
        }

        try {
            const shard = BigInt(match[1]!);
            const realm = BigInt(match[2]!);
            const num = BigInt(match[3]!);
            const checksum = match[4] ?? '';
            return new Address(shard, realm, num, checksum);
        } catch {
            throw new IllegalFormatError(`Failed to parse numeric parts in address: '${address}'`);
        }
    }
}

/**
 * The zero address (0.0.0). Callers should not use this value as an actual entity id.
 */
export const ZERO_ADDRESS: Address = new Address(0n, 0n, 0n, '');
