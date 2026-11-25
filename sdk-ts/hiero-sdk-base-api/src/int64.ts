const INT64_MIN = -(2n ** 63n);
const INT64_MAX = 2n ** 63n - 1n;

/**
 * Validates that a value is a `bigint` within the signed 64-bit range.
 *
 * @internal
 * @param value - the value to validate
 * @param name - the name used in error messages
 * @returns the validated value
 * @throws TypeError if `value` is not a `bigint`
 * @throws RangeError if `value` is outside the signed 64-bit range
 */
export function requireInt64(value: bigint, name: string): bigint {
  if (typeof value !== "bigint") {
    throw new TypeError(`${name} must be a bigint, but was ${typeof value}`);
  }
  if (value < INT64_MIN || value > INT64_MAX) {
    throw new RangeError(`${name} must be a 64-bit signed integer, but was ${value.toString()}`);
  }
  return value;
}

const UINT64_MAX = 2n ** 64n - 1n;

/**
 * Validates that a value is a `bigint` within the unsigned 64-bit range.
 *
 * @internal
 * @param value - the value to validate
 * @param name - the name used in error messages
 * @returns the validated value
 * @throws TypeError if `value` is not a `bigint`
 * @throws RangeError if `value` is outside the unsigned 64-bit range
 */
export function requireUint64(value: bigint, name: string): bigint {
  if (typeof value !== "bigint") {
    throw new TypeError(`${name} must be a bigint, but was ${typeof value}`);
  }
  if (value < 0n || value > UINT64_MAX) {
    throw new RangeError(`${name} must be a 64-bit unsigned integer, but was ${value.toString()}`);
  }
  return value;
}

/**
 * Reports whether a `bigint` is within the unsigned 64-bit range.
 *
 * @internal
 * @param value - the value to check
 * @returns `true` when `0 <= value <= 2^64 - 1`
 */
export function isUint64(value: bigint): boolean {
  return value >= 0n && value <= UINT64_MAX;
}
