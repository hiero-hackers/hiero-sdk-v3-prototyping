import { BaseAddress } from "./base-address.js";
import { hip15Checksum } from "./checksum.js";
import { IllegalFormatError } from "./illegal-format-error.js";
import { isUint64, requireUint64 } from "./int64.js";
import type { NativeTokenUnit } from "./native-token-unit.js";
import { Network } from "./network.js";

const ADDRESS_PATTERN = /^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(?:-([a-z]{5}))?$/;
const CHECKSUM_PATTERN = /^[a-z]{5}$/;

/**
 * A concrete address with a single numeric selector, used for entities identified by (shard, realm, num) such as
 * tokens, topics, files, and schedules.
 *
 * `Address` is final: its constructor is private, so it cannot be extended. Create instances with {@link Address.of},
 * {@link Address.fromString}, or use {@link ZERO_ADDRESS}. Checksums follow HIP-15.
 */
export class Address extends BaseAddress {
  readonly #num: bigint;

  private constructor(shard: bigint, realm: bigint, num: bigint, checksum: string) {
    super(shard, realm, checksum);
    requireUint64(num, "num");
    if (checksum !== "" && !CHECKSUM_PATTERN.test(checksum)) {
      throw new IllegalFormatError(`checksum must be empty or five lowercase letters, but was "${checksum}"`);
    }
    this.#num = num;
  }

  /**
   * Creates an address.
   *
   * @param shard - the shard number, a 64-bit unsigned integer
   * @param realm - the realm number, a 64-bit unsigned integer
   * @param num - the entity number, a 64-bit unsigned integer
   * @param checksum - the HIP-15 checksum (five lowercase letters), or an empty string for none
   * @returns the address
   * @throws TypeError if a number is not a `bigint` or `checksum` is not a string
   * @throws RangeError if a number is outside the unsigned 64-bit range
   * @throws IllegalFormatError if `checksum` is neither empty nor five lowercase letters
   */
  static of(shard: bigint, realm: bigint, num: bigint, checksum: string = ""): Address {
    return new Address(shard, realm, num, checksum);
  }

  /**
   * Parses an address from its canonical `"shard.realm.num"` or `"shard.realm.num-checksum"` form.
   *
   * Numbers must be decimal without signs, whitespace, or leading zeros, and at most 2^64 - 1. The checksum, when
   * present, must be five lowercase letters; it is stored but not validated, because no network is given.
   *
   * @param address - the string to parse
   * @returns the parsed address
   * @throws TypeError if `address` is not a string
   * @throws IllegalFormatError if `address` is not in the canonical form or a number is out of range
   */
  static fromString(address: string): Address {
    if (typeof address !== "string") {
      throw new TypeError(`address must be a string, but was ${typeof address}`);
    }
    const match = ADDRESS_PATTERN.exec(address);
    if (match === null) {
      throw new IllegalFormatError(`"${address}" is not a valid address; expected "shard.realm.num[-checksum]"`);
    }
    const [, shardText = "", realmText = "", numText = "", checksum = ""] = match;
    const shard = BigInt(shardText);
    const realm = BigInt(realmText);
    const num = BigInt(numText);
    if (![shard, realm, num].every(isUint64)) {
      throw new IllegalFormatError(`"${address}" contains a number outside the unsigned 64-bit range`);
    }
    return new Address(shard, realm, num, checksum);
  }

  /**
   * The Hiero entity number. Always set on an `Address`.
   */
  override get num(): bigint {
    return this.#num;
  }

  /**
   * Validates the HIP-15 checksum of this address against the network's ledger id.
   *
   * @param network - the network to validate against; a network of any native token unit
   * @returns `true` when this address has a checksum and it equals the HIP-15 checksum for `network`; `false`
   *   otherwise, including when this address has no checksum
   * @throws TypeError if `network` is not a {@link Network}
   */
  override validateChecksum(network: Network<NativeTokenUnit>): boolean {
    if (!(network instanceof Network)) {
      throw new TypeError("network must be a Network");
    }
    return this.checksum !== "" && this.checksum === hip15Checksum(network.id, this.toString());
  }

  /**
   * Returns the canonical `"shard.realm.num"` form, without a checksum.
   *
   * @returns the canonical string form
   */
  override toString(): string {
    return `${this.shard.toString()}.${this.realm.toString()}.${this.#num.toString()}`;
  }
}

/**
 * The zero address (`0.0.0`) with an empty checksum.
 *
 * Used as a clear-sentinel for address-typed fields of update transactions. Do not use it as an actual entity id;
 * there is no entity `0.0.0` on the network.
 */
export const ZERO_ADDRESS: Address = Address.of(0n, 0n, 0n);
