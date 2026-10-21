import type { NativeTokenUnit } from './nativeToken.js';
import { NetworkImpl } from './internal/ledger-impl.js';

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
