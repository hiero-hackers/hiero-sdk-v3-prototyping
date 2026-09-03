/**
 * Safely parses a uint64 value to BigInt.
 * Enforces use of BigInt to prevent precision loss in JS.
 */
export function parseUint64(val) {
    return BigInt(val);
}
