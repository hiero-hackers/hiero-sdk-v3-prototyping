import { NativeTokenUnit } from "./native-token-unit.js";

/**
 * The identity of a network: its ledger id, an optional human-readable name, and the unit of its native token.
 *
 * Instances are immutable. Because a `Uint8Array` cannot be frozen, the id bytes are copied at construction and every
 * read of {@link Network.id} returns a new copy; compare ids by content, not by reference.
 *
 * A `Network` of any concrete unit type is assignable to `Network<NativeTokenUnit>`, which accepts any network.
 *
 * @typeParam Unit - the unit type of the network's native token
 */
export class Network<Unit extends NativeTokenUnit> {
  readonly #id: Uint8Array;
  readonly #name: string | null;
  readonly #nativeTokenUnit: Unit;

  /**
   * Creates a network identity.
   *
   * @param id - the ledger id bytes of the network; copied, so later changes to the array are not reflected
   * @param name - the human-readable name of the network, or `null` when it has none
   * @param nativeTokenUnit - the unit of the network's native token
   * @throws TypeError if `id` is not a `Uint8Array`, `name` is neither a string nor `null`, or `nativeTokenUnit` is
   *   not a {@link NativeTokenUnit}
   */
  constructor(id: Uint8Array, name: string | null, nativeTokenUnit: Unit) {
    if (!(id instanceof Uint8Array)) {
      throw new TypeError("id must be a Uint8Array");
    }
    if (name !== null && typeof name !== "string") {
      throw new TypeError(`name must be a string or null, but was ${typeof name}`);
    }
    if (!(nativeTokenUnit instanceof NativeTokenUnit)) {
      throw new TypeError("nativeTokenUnit must be a NativeTokenUnit");
    }
    this.#id = new Uint8Array(id);
    this.#name = name;
    this.#nativeTokenUnit = nativeTokenUnit;
  }

  /**
   * A copy of the ledger id bytes of this network. Modifying the returned array does not change this network.
   */
  get id(): Uint8Array {
    return this.#id.slice();
  }

  /**
   * The human-readable name of this network, or `null` when it has none.
   */
  get name(): string | null {
    return this.#name;
  }

  /**
   * The unit of this network's native token.
   */
  get nativeTokenUnit(): Unit {
    return this.#nativeTokenUnit;
  }
}
