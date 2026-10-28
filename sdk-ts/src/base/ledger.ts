import type { NativeTokenUnit } from "./nativeToken.js";
import { NetworkImpl } from "./internal/ledger-impl.js";

/**
 * Represents the identity of a network, including its ledger ID, optional human-readable name,
 * and the native token unit it uses.
 *
 * @typeParam Unit - The native token unit type used by this network.
 */
export interface Network<Unit extends NativeTokenUnit> {
  /**
   * The ledger ID bytes that uniquely identify this network.
   * A fresh defensive copy is returned on every read.
   */
  readonly id: Uint8Array;

  /**
   * Human-readable name of the network (e.g. "mainnet"), or `null` if not set.
   * Never `undefined`.
   */
  readonly name: string | null;

  /**
   * The native token unit associated with this network.
   */
  readonly nativeTokenUnit: Unit;
}

/**
 * Creates a new immutable `Network` instance.
 *
 * @param id - Ledger ID bytes. A defensive copy is made; the original array is not retained.
 * @param name - Human-readable network name, or `null` for no name. `undefined` is not allowed.
 * @param nativeTokenUnit - The native token unit used by this network.
 * @throws Error if `id` is null/undefined, `name` is `undefined`, or `nativeTokenUnit` is null/undefined.
 */
export function createNetwork<Unit extends NativeTokenUnit>(
  id: Uint8Array,
  name: string | null,
  nativeTokenUnit: Unit,
): Network<Unit> {
  return new NetworkImpl(id, name, nativeTokenUnit);
}
