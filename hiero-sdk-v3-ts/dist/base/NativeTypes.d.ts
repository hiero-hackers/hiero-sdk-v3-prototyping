/**
 * Safely parses a uint64 value to BigInt.
 * Enforces use of BigInt to prevent precision loss in JS.
 */
export declare function parseUint64(val: string | number | bigint): bigint;
