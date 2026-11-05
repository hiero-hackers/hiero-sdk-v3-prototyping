import { requireUint64 } from "./int64.js";
import type { NativeTokenUnit } from "./native-token-unit.js";
import type { Network } from "./network.js";

/**
 * The abstract base of every entity identifier that lives in the (shard, realm) space of a Hiero ledger.
 *
 * Subtypes differ only in their selector, the third part of the identifier. The numeric selector `num` is nullable
 * here because some subtypes (for example EVM-form identifiers) may not carry one; {@link Address} narrows it to a
 * non-null value.
 */
export abstract class BaseAddress {
  readonly #shard: bigint;
  readonly #realm: bigint;
  readonly #checksum: string;

  /**
   * Creates the shared address state.
   *
   * @param shard - the shard number, a 64-bit unsigned integer
   * @param realm - the realm number, a 64-bit unsigned integer
   * @param checksum - the protocol-side checksum, or an empty string when no checksum applies
   * @throws TypeError if `shard` or `realm` is not a `bigint`, or `checksum` is not a string
   * @throws RangeError if `shard` or `realm` is outside the unsigned 64-bit range
   */
  protected constructor(shard: bigint, realm: bigint, checksum: string) {
    requireUint64(shard, "shard");
    requireUint64(realm, "realm");
    if (typeof checksum !== "string") {
      throw new TypeError(`checksum must be a string, but was ${typeof checksum}`);
    }
    this.#shard = shard;
    this.#realm = realm;
    this.#checksum = checksum;
  }

  /**
   * The shard number.
   */
  get shard(): bigint {
    return this.#shard;
  }

  /**
   * The realm number.
   */
  get realm(): bigint {
    return this.#realm;
  }

  /**
   * The protocol-side checksum over the (shard, realm, selector) form, or an empty string when no checksum applies.
   */
  get checksum(): string {
    return this.#checksum;
  }

  /**
   * The Hiero entity number, or `null` when this address is not addressed by number.
   */
  abstract get num(): bigint | null;

  /**
   * Validates the checksum of this address against the given network's checksum scheme.
   *
   * @param network - the network to validate against; a network of any native token unit
   * @returns `true` when the checksum matches the network
   */
  abstract validateChecksum(network: Network<NativeTokenUnit>): boolean;

  /**
   * Returns the canonical string form of this address, without a checksum.
   *
   * @returns the canonical string form
   */
  abstract toString(): string;

  /**
   * Returns {@link BaseAddress.toString} followed by `-<checksum>` when a non-empty checksum is set.
   *
   * @returns the canonical string form with the checksum suffix when present
   */
  toStringWithChecksum(): string {
    return this.#checksum === "" ? this.toString() : `${this.toString()}-${this.#checksum}`;
  }
}
