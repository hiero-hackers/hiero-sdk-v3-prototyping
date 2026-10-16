/**
 * The built-in functions and assertions of the test language (`guidelines/test-language.md`), used by the TypeScript
 * tests that are generated from the `tests` blocks of the specs. The counterpart of the Java class
 * `org.hiero.sdk.testing.Testing`.
 *
 * The test language defines its own semantics, which this module implements where JavaScript differs:
 *
 * - **Equality is value equality** (`equal`): bytes by content, lists in order, sets without order, maps by their
 *   entries, integers by value whatever their representation (`number` or `bigint`), decimals without their scale, a
 *   value with an `equals` method by that method, and the instances of a class by all their attributes.
 * - **Integers are exact**: every integer of the test language is a `bigint`; `int` converts an API value to one and
 *   `checked` checks a value against its meta-language type before it is passed to the API.
 * - **Strings are sequences of code points**, not of UTF-16 units.
 * - **Decimals are exact**: a decimal is its text (the TypeScript form of `decimal`), computed with `bigint`.
 *
 * @packageDocumentation
 */

import { AssertionError, deepStrictEqual, strictEqual } from "node:assert/strict";
import { inspect } from "node:util";

// --- assertions --------------------------------------------------------------------------------------------------

/**
 * Fails the test unless actual is equal to expected (value equality).
 *
 * @param expected the expected value
 * @param actual   the actual value
 * @param message  the message of the failure
 */
export function assertEquals(expected: unknown, actual: unknown, message?: string | null): void {
    if (!equal(expected, actual)) {
        failWith(message, `expected ${display(expected)} but was ${display(actual)}`, expected, actual);
    }
}

/**
 * The comparison of the contract tests generated per type: by `equals` if expected has it, by content if it is an
 * array, bytes, a set, a map or a date, otherwise by identity (`===`). Unlike `assertEquals` it does not use the
 * value equality of the test language: a contract test checks that a value is passed through unchanged.
 *
 * @param expected the expected value
 * @param actual   the actual value
 */
export function assertValue(expected: unknown, actual: unknown): void {
    if (typeof expected === "object" && expected !== null && "equals" in expected
        && typeof expected.equals === "function") {
        if (!(expected.equals as (other: unknown) => boolean)(actual)) {
            failWith(null, `expected ${String(expected)} but was ${String(actual)}`, expected, actual);
        }
    } else if (expected instanceof Uint8Array || expected instanceof Date || expected instanceof Set
        || expected instanceof Map || Array.isArray(expected)) {
        deepStrictEqual(actual, expected);
    } else {
        strictEqual(actual, expected);
    }
}

/**
 * Fails the test if actual is equal to unexpected (value equality).
 *
 * @param unexpected the value actual must not be equal to
 * @param actual     the actual value
 * @param message    the message of the failure
 */
export function assertNotEquals(unexpected: unknown, actual: unknown, message?: string | null): void {
    if (equal(unexpected, actual)) {
        failWith(message, `expected a value other than ${display(unexpected)}`, unexpected, actual);
    }
}

/**
 * Fails the test unless the condition is true.
 *
 * @param condition the condition
 * @param message   the message of the failure
 */
export function assertTrue(condition: boolean, message?: string | null): void {
    if (condition !== true) {
        failWith(message, "expected true but was false", true, condition);
    }
}

/**
 * Fails the test unless the condition is false.
 *
 * @param condition the condition
 * @param message   the message of the failure
 */
export function assertFalse(condition: boolean, message?: string | null): void {
    if (condition !== false) {
        failWith(message, "expected false but was true", false, condition);
    }
}

/**
 * Fails the test unless the value is null.
 *
 * @param actual  the value
 * @param message the message of the failure
 */
export function assertNull(actual: unknown, message?: string | null): void {
    if (actual !== null && actual !== undefined) {
        failWith(message, `expected null but was ${display(actual)}`, null, actual);
    }
}

/**
 * Fails the test if the value is null; the value is not null in the rest of the block.
 *
 * @param actual  the value
 * @param message the message of the failure
 */
export function assertNotNull<T>(actual: T, message?: string | null): asserts actual is NonNullable<T> {
    if (actual === null || actual === undefined) {
        failWith(message, "expected a value but was null", "a value", actual);
    }
}

/**
 * Fails the test unless actual is an instance of the expected type.
 *
 * @param expected the class of the type: any function that `instanceof` accepts, also a class with a private
 *                 constructor such as an enum class
 * @param actual   the value
 * @param message  the message of the failure
 */
// eslint-disable-next-line @typescript-eslint/no-unsafe-function-type -- every class, also one with a private constructor
export function assertInstanceOf(expected: Function, actual: unknown, message?: string | null): void {
    if (!(actual instanceof expected)) {
        failWith(message, `expected an instance of ${expected.name} but was ${display(actual)}`, expected.name,
            actual);
    }
}

/**
 * Fails the test unless actual differs from expected by at most tolerance.
 *
 * @param expected  the expected value
 * @param actual    the actual value
 * @param tolerance the largest allowed difference
 * @param message   the message of the failure
 */
export function assertApprox(expected: number, actual: number, tolerance: number, message?: string | null): void {
    if (!approx(actual, expected, tolerance)) {
        failWith(message, `expected ${expected} ± ${tolerance} but was ${actual}`, expected, actual);
    }
}

/**
 * Fails the test unconditionally.
 *
 * @param message the message of the failure
 */
export function fail(message: string): never {
    throw new AssertionError({ message });
}

function failWith(message: string | null | undefined, detail: string, expected: unknown, actual: unknown): never {
    throw new AssertionError({
        message: message === null || message === undefined ? detail : `${message}: ${detail}`,
        expected,
        actual,
    });
}

function display(value: unknown): string {
    if (value instanceof Uint8Array) {
        return "0x" + toHex(value);
    }
    return typeof value === "string" ? JSON.stringify(value) : inspect(value, { depth: 4 });
}

// --- equality ----------------------------------------------------------------------------------------------------

/**
 * Value equality of the test language (`==`): bytes by content, lists in order, sets without order, maps by their
 * entries, integers by value (`number` or `bigint`), `number` as in IEEE 754 (`NaN` is not equal to itself), dates by
 * their time, a value with an `equals` method by that method, the instances of a class by all their attributes
 * (getters), everything else by identity. `null` and `undefined` are both the `null` of the test language.
 *
 * @param a a value
 * @param b a value
 * @returns whether the values are equal
 */
export function equal(a: unknown, b: unknown): boolean {
    if (a === null || a === undefined || b === null || b === undefined) {
        return (a === null || a === undefined) && (b === null || b === undefined);
    }
    if (typeof a === "bigint" || typeof b === "bigint") {
        return isInteger(a) && isInteger(b) && BigInt(a as number | bigint) === BigInt(b as number | bigint);
    }
    if (typeof a !== "object" || typeof b !== "object") {
        return a === b;
    }
    if (a instanceof Uint8Array || b instanceof Uint8Array) {
        return a instanceof Uint8Array && b instanceof Uint8Array && a.length === b.length
            && a.every((x, i) => x === b[i]);
    }
    if (a instanceof Date || b instanceof Date) {
        return a instanceof Date && b instanceof Date && a.getTime() === b.getTime();
    }
    if (Array.isArray(a) || Array.isArray(b)) {
        return Array.isArray(a) && Array.isArray(b) && a.length === b.length && a.every((x, i) => equal(x, b[i]));
    }
    if (a instanceof Set || b instanceof Set) {
        return a instanceof Set && b instanceof Set && a.size === b.size && [...a].every(x => contains(b, x));
    }
    if (a instanceof Map || b instanceof Map) {
        return a instanceof Map && b instanceof Map && a.size === b.size
            && [...a].every(([key, value]) => [...b].some(([k, v]) => equal(key, k) && equal(value, v)));
    }
    if (a === b) {
        return true;
    }
    if ("equals" in a && typeof a.equals === "function") {
        return (a.equals as (other: unknown) => boolean)(b) === true;
    }
    if (Object.getPrototypeOf(a) !== Object.getPrototypeOf(b)) {
        return false;
    }
    const attributes = getters(a);
    return attributes.length > 0 && attributes.every(name => equal(Reflect.get(a, name), Reflect.get(b, name)));
}

function isInteger(value: unknown): value is number | bigint {
    return typeof value === "bigint" || (typeof value === "number" && Number.isInteger(value));
}

/** The names of the getters of an object and its superclasses: the attributes of a generated class. */
function getters(value: object): string[] {
    const names = new Set<string>();
    for (let prototype = Object.getPrototypeOf(value); prototype !== null && prototype !== Object.prototype;
        prototype = Object.getPrototypeOf(prototype)) {
        for (const [name, descriptor] of Object.entries(Object.getOwnPropertyDescriptors(prototype))) {
            if (descriptor.get !== undefined) {
                names.add(name);
            }
        }
    }
    return [...names].sort();
}

/**
 * Compares two values of the same type for `<`, `<=`, `>` and `>=` of the test language: strings code point by code
 * point, dates by their time, integers by value, everything else with a `compareTo` method.
 *
 * @param a a value
 * @param b a value
 * @returns a negative number, zero or a positive number as a is less than, equal to or greater than b
 */
export function compare(a: unknown, b: unknown): number {
    if (typeof a === "string" && typeof b === "string") {
        const x = [...a];
        const y = [...b];
        for (let i = 0; i < Math.min(x.length, y.length); i++) {
            const difference = (x[i]?.codePointAt(0) ?? 0) - (y[i]?.codePointAt(0) ?? 0);
            if (difference !== 0) {
                return difference;
            }
        }
        return x.length - y.length;
    }
    if (a instanceof Date && b instanceof Date) {
        return a.getTime() - b.getTime();
    }
    if (isInteger(a) && isInteger(b)) {
        const difference = BigInt(a) - BigInt(b);
        return difference < 0n ? -1 : difference > 0n ? 1 : 0;
    }
    if (typeof a === "object" && a !== null && "compareTo" in a && typeof a.compareTo === "function") {
        return (a.compareTo as (other: unknown) => number)(b);
    }
    return fail(`${display(a)} and ${display(b)} cannot be compared`);
}

// --- integers ----------------------------------------------------------------------------------------------------

/**
 * An integer of the API (`number` for up to 32 bits, `bigint` above) as exact integer.
 *
 * @param value the value
 * @returns the value as `bigint`
 */
export function int(value: number | bigint): bigint {
    if (typeof value === "number" && !Number.isInteger(value)) {
        fail(`${value} is no integer`);
    }
    return BigInt(value);
}

/**
 * Checks that an integer fits a type of the meta-language before it is passed to the API.
 *
 * @param value the value
 * @param type  the type, `int8` to `int256` or `uint8` to `uint256`
 * @returns the value
 */
export function checked(value: bigint, type: string): bigint {
    const unsigned = type.startsWith("u");
    const bits = BigInt(type.substring(unsigned ? 4 : 3));
    const min = unsigned ? 0n : -(1n << (bits - 1n));
    const max = unsigned ? (1n << bits) - 1n : (1n << (bits - 1n)) - 1n;
    if (value < min || value > max) {
        fail(`${value} is out of the range of ${type} (${min} to ${max})`);
    }
    return value;
}

/**
 * Checks that an integer fits a type of the meta-language whose TypeScript type is `number` (up to 32 bits).
 *
 * @param value the value
 * @param type  the type, `int8` to `int32` or `uint8` to `uint32`
 * @returns the value as `number`
 */
export function checkedNumber(value: bigint, type: string): number {
    return Number(checked(value, type));
}

// --- strings and bytes -------------------------------------------------------------------------------------------

/**
 * Number of code points of a string, of bytes, of elements of a list or set, or of entries of a map.
 *
 * @param value the value
 * @returns the length
 */
export function length(value: string | Uint8Array | ReadonlyArray<unknown> | ReadonlySet<unknown>
    | ReadonlyMap<unknown, unknown>): bigint {
    if (typeof value === "string") {
        return BigInt([...value].length);
    }
    if (value instanceof Uint8Array || Array.isArray(value)) {
        return BigInt(value.length);
    }
    return BigInt((value as ReadonlySet<unknown> | ReadonlyMap<unknown, unknown>).size);
}

/**
 * The code points of a string from start (inclusive) to end (exclusive).
 *
 * @param value the string
 * @param start the first position
 * @param end   the position after the last
 * @returns the part
 */
export function substring(value: string, start: bigint, end: bigint): string {
    const codePoints = [...value];
    checkRange(start, end, codePoints.length);
    return codePoints.slice(Number(start), Number(end)).join("");
}

/**
 * Whether a string contains a part, bytes contain a sequence, or a list or set contains an element equal to the
 * given one.
 *
 * @param value the string, bytes, list or set
 * @param part  the part, sequence or element
 * @returns whether it is contained
 */
export function contains(value: string | Uint8Array | ReadonlyArray<unknown> | ReadonlySet<unknown>,
                         part: unknown): boolean {
    if (typeof value === "string") {
        return value.includes(part as string);
    }
    if (value instanceof Uint8Array) {
        return indexOf(value, part as Uint8Array) >= 0;
    }
    return [...value].some(element => equal(element, part));
}

/**
 * Whether a string or bytes start with the prefix.
 *
 * @param value  the string or bytes
 * @param prefix the prefix
 * @returns whether it starts with the prefix
 */
export function startsWith<T extends string | Uint8Array>(value: T, prefix: T): boolean {
    if (typeof value === "string") {
        return value.startsWith(prefix as string);
    }
    const bytes = prefix as Uint8Array;
    return bytes.length <= value.length && equal(value.subarray(0, bytes.length), bytes);
}

/**
 * Whether a string or bytes end with the suffix.
 *
 * @param value  the string or bytes
 * @param suffix the suffix
 * @returns whether it ends with the suffix
 */
export function endsWith<T extends string | Uint8Array>(value: T, suffix: T): boolean {
    if (typeof value === "string") {
        return value.endsWith(suffix as string);
    }
    const bytes = suffix as Uint8Array;
    return bytes.length <= value.length && equal(value.subarray(value.length - bytes.length), bytes);
}

/**
 * A string or bytes repeated count times, for boundary tests.
 *
 * @param value the string or bytes
 * @param count how often
 * @returns the repetition
 */
export function repeat<T extends string | Uint8Array>(value: T, count: bigint): T {
    if (count < 0n) {
        fail(`A negative count: ${count}`);
    }
    if (typeof value === "string") {
        return value.repeat(Number(count)) as T;
    }
    const bytes = value as Uint8Array;
    const result = new Uint8Array(bytes.length * Number(count));
    for (let i = 0; i < Number(count); i++) {
        result.set(bytes, i * bytes.length);
    }
    return result as T;
}

/**
 * The bytes or the elements of a list from start (inclusive) to end (exclusive).
 *
 * @param value the bytes or list
 * @param start the first position
 * @param end   the position after the last
 * @returns the part, a copy
 */
export function slice<T extends Uint8Array | ReadonlyArray<unknown>>(value: T, start: bigint, end: bigint): T {
    checkRange(start, end, value.length);
    return value.slice(Number(start), Number(end)) as T;
}

/**
 * The concatenation of bytes or of lists (`a + b` of the test language).
 *
 * @param a the first part
 * @param b the second part
 * @returns the concatenation
 */
export function concat<T extends Uint8Array | ReadonlyArray<unknown>>(a: T, b: T): T {
    if (a instanceof Uint8Array) {
        const result = new Uint8Array(a.length + (b as Uint8Array).length);
        result.set(a);
        result.set(b as Uint8Array, a.length);
        return result as T;
    }
    return [...(a as ReadonlyArray<unknown>), ...(b as ReadonlyArray<unknown>)] as unknown as T;
}

/**
 * Bytes from hex text: case-insensitive, whitespace between bytes allowed.
 *
 * @param text the hex text
 * @returns the bytes
 */
export function hex(text: string): Uint8Array {
    const digits = text.replace(/\s+/g, "");
    if (!/^(?:[0-9a-fA-F]{2})*$/.test(digits)) {
        fail(`'${text}' is no hex text`);
    }
    const result = new Uint8Array(digits.length / 2);
    for (let i = 0; i < result.length; i++) {
        result[i] = Number.parseInt(digits.substring(2 * i, 2 * i + 2), 16);
    }
    return result;
}

/**
 * Bytes from Base64 text (RFC 4648, with padding).
 *
 * @param text the Base64 text
 * @returns the bytes
 */
export function base64(text: string): Uint8Array {
    if (!/^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$/.test(text)) {
        fail(`'${text}' is no Base64 text`);
    }
    return new Uint8Array(Buffer.from(text, "base64"));
}

/**
 * The UTF-8 encoding of a string.
 *
 * @param text the string
 * @returns the bytes
 */
export function utf8(text: string): Uint8Array {
    return new TextEncoder().encode(text);
}

/**
 * Lowercase hex of bytes, without separators.
 *
 * @param value the bytes
 * @returns the hex text
 */
export function toHex(value: Uint8Array): string {
    return [...value].map(b => b.toString(16).padStart(2, "0")).join("");
}

function indexOf(value: Uint8Array, part: Uint8Array): number {
    for (let i = 0; i + part.length <= value.length; i++) {
        if (part.every((b, j) => value[i + j] === b)) {
            return i;
        }
    }
    return -1;
}

function checkRange(start: bigint, end: bigint, length: number): void {
    if (start < 0n || end > BigInt(length) || start > end) {
        fail(`The positions ${start} to ${end} are out of the range 0 to ${length}`);
    }
}

// --- collections -------------------------------------------------------------------------------------------------

/**
 * Whether the map contains the key (value equality).
 *
 * @param value the map
 * @param key   the key
 * @returns whether the key is contained
 */
export function containsKey(value: ReadonlyMap<unknown, unknown>, key: unknown): boolean {
    return [...value.keys()].some(k => equal(k, key));
}

/**
 * The element of a list at an index, or the value of a map for a key (value equality); a missing one fails the test.
 *
 * @param value the list or map
 * @param key   the index or key
 * @returns the element or value
 */
export function get<T>(value: ReadonlyArray<T>, key: bigint): T;
export function get<K, V>(value: ReadonlyMap<K, V>, key: K): V;
export function get(value: ReadonlyArray<unknown> | ReadonlyMap<unknown, unknown>, key: unknown): unknown {
    if (Array.isArray(value)) {
        const index = key as bigint;
        checkRange(index, index + 1n, value.length);
        return value[Number(index)];
    }
    for (const [k, v] of value as ReadonlyMap<unknown, unknown>) {
        if (equal(k, key)) {
            return v;
        }
    }
    return fail(`The map has no key ${display(key)}`);
}

/**
 * The keys of a map, in an unspecified order.
 *
 * @param value the map
 * @returns the keys
 */
export function keys<K>(value: ReadonlyMap<K, unknown>): K[] {
    return [...value.keys()];
}

/**
 * The values of a map, in an unspecified order.
 *
 * @param value the map
 * @returns the values
 */
export function values<V>(value: ReadonlyMap<unknown, V>): V[] {
    return [...value.values()];
}

/**
 * The integers from start (inclusive) to end (exclusive).
 *
 * @param start the first integer
 * @param end   the integer after the last
 * @returns the integers
 */
export function range(start: bigint, end: bigint): bigint[] {
    const result: bigint[] = [];
    for (let i = start; i < end; i++) {
        result.push(i);
    }
    return result;
}

// --- numbers -----------------------------------------------------------------------------------------------------

/** The absolute value of an integer. */
export function abs(value: bigint): bigint {
    return value < 0n ? -value : value;
}

/** The absolute value of a `double`. */
export function absDouble(value: number): number {
    return Math.abs(value);
}

/** The absolute value of a decimal. */
export function absDecimal(value: string): string {
    const d = parseDecimal(value);
    return formatDecimal(d.unscaled < 0n ? -d.unscaled : d.unscaled, d.scale);
}

/** The smaller of two integers. */
export function min(a: bigint, b: bigint): bigint {
    return a <= b ? a : b;
}

/** The smaller of two `double` values. */
export function minDouble(a: number, b: number): number {
    return Math.min(a, b);
}

/** The smaller of two decimals. */
export function minDecimal(a: string, b: string): string {
    return compareDecimal(a, b) <= 0 ? a : b;
}

/** The larger of two integers. */
export function max(a: bigint, b: bigint): bigint {
    return a >= b ? a : b;
}

/** The larger of two `double` values. */
export function maxDouble(a: number, b: number): number {
    return Math.max(a, b);
}

/** The larger of two decimals. */
export function maxDecimal(a: string, b: string): string {
    return compareDecimal(a, b) >= 0 ? a : b;
}

/**
 * Whether actual differs from expected by at most tolerance.
 *
 * @param actual    the actual value
 * @param expected  the expected value
 * @param tolerance the largest allowed difference
 * @returns whether the values are close enough
 */
export function approx(actual: number, expected: number, tolerance: number): boolean {
    return Math.abs(actual - expected) <= tolerance;
}

/** Whether a `double` is NaN. */
export function isNaN(value: number): boolean {
    return Number.isNaN(value);
}

// --- decimals ----------------------------------------------------------------------------------------------------

interface Decimal {
    readonly unscaled: bigint;
    readonly scale: number;
}

function parseDecimal(text: string): Decimal {
    const match = /^([+-]?)(\d+)(?:\.(\d+))?$/.exec(text);
    if (match === null) {
        fail(`'${text}' is no decimal`);
    }
    const fraction = match[3] ?? "";
    const unscaled = BigInt((match[2] ?? "") + fraction);
    return { unscaled: match[1] === "-" ? -unscaled : unscaled, scale: fraction.length };
}

function formatDecimal(unscaled: bigint, scale: number): string {
    const negative = unscaled < 0n;
    const digits = (negative ? -unscaled : unscaled).toString().padStart(scale + 1, "0");
    const text = scale === 0 ? digits : digits.substring(0, digits.length - scale) + "." + digits.substring(
        digits.length - scale);
    return negative ? "-" + text : text;
}

function rescale(value: Decimal, scale: number): bigint {
    return value.unscaled * 10n ** BigInt(scale - value.scale);
}

/**
 * A decimal in the form that compares by value: without trailing zeros after the point, so that `1.50` and `1.5` are
 * the same text.
 *
 * @param value the decimal
 * @returns the decimal without trailing zeros
 */
export function decimal(value: string): string {
    let { unscaled, scale } = parseDecimal(value);
    while (scale > 0 && unscaled % 10n === 0n) {
        unscaled /= 10n;
        scale--;
    }
    return formatDecimal(unscaled, scale);
}

/** The sum of two decimals (the scale of the more precise one). */
export function addDecimal(a: string, b: string): string {
    const x = parseDecimal(a);
    const y = parseDecimal(b);
    const scale = Math.max(x.scale, y.scale);
    return formatDecimal(rescale(x, scale) + rescale(y, scale), scale);
}

/** The difference of two decimals (the scale of the more precise one). */
export function subtractDecimal(a: string, b: string): string {
    const x = parseDecimal(a);
    const y = parseDecimal(b);
    const scale = Math.max(x.scale, y.scale);
    return formatDecimal(rescale(x, scale) - rescale(y, scale), scale);
}

/** The product of two decimals (the sum of their scales). */
export function multiplyDecimal(a: string, b: string): string {
    const x = parseDecimal(a);
    const y = parseDecimal(b);
    return formatDecimal(x.unscaled * y.unscaled, x.scale + y.scale);
}

/** The negation of a decimal. */
export function negateDecimal(value: string): string {
    const d = parseDecimal(value);
    return formatDecimal(-d.unscaled, d.scale);
}

/**
 * Compares two decimals by value.
 *
 * @param a a decimal
 * @param b a decimal
 * @returns a negative number, zero or a positive number as a is less than, equal to or greater than b
 */
export function compareDecimal(a: string, b: string): number {
    const x = parseDecimal(a);
    const y = parseDecimal(b);
    const scale = Math.max(x.scale, y.scale);
    const difference = rescale(x, scale) - rescale(y, scale);
    return difference < 0n ? -1 : difference > 0n ? 1 : 0;
}

/**
 * The quotient of two decimals, rounded half-even to scale digits after the point.
 *
 * @param dividend the dividend
 * @param divisor  the divisor
 * @param scale    the number of digits after the point
 * @returns the quotient
 */
export function divide(dividend: string, divisor: string, scale: number): string {
    const x = parseDecimal(dividend);
    const y = parseDecimal(divisor);
    if (y.unscaled === 0n) {
        fail("Division by zero");
    }
    // x / y * 10^scale = x.unscaled * 10^(scale + y.scale) / (y.unscaled * 10^x.scale)
    if (!Number.isInteger(scale) || scale < 0) {
        fail(`${scale} is no scale`);
    }
    const s = scale;
    let numerator = x.unscaled * 10n ** BigInt(Math.max(0, s + y.scale - x.scale));
    let denominator = y.unscaled * 10n ** BigInt(Math.max(0, x.scale - s - y.scale));
    if (denominator < 0n) {
        numerator = -numerator;
        denominator = -denominator;
    }
    let quotient = numerator / denominator;
    const remainder = numerator % denominator;
    const twice = 2n * (remainder < 0n ? -remainder : remainder);
    if (twice > denominator || (twice === denominator && quotient % 2n !== 0n)) {
        quotient += numerator < 0n ? -1n : 1n;
    }
    return formatDecimal(quotient, s);
}

// --- conversions -------------------------------------------------------------------------------------------------

/** Decimal text of an integer without grouping (`toString` of the test language), e.g. `-1234`. */
export function text(value: bigint): string {
    return value.toString();
}

/** Decimal text of a decimal without grouping and exponent (`toString` of the test language), e.g. `0.5`. */
export function textDecimal(value: string): string {
    const d = parseDecimal(value);
    return formatDecimal(d.unscaled, d.scale);
}

/**
 * Parses decimal text; text that is no integer fails the test.
 *
 * @param value the text
 * @returns the integer
 */
export function toInt(value: string): bigint {
    if (!/^[+-]?\d+$/.test(value)) {
        fail(`'${value}' is no integer`);
    }
    return BigInt(value);
}

/** The nearest `double` of an integer. */
export function toDouble(value: bigint): number {
    return Number(value);
}

/** The nearest `double` of a decimal. */
export function toDoubleDecimal(value: string): number {
    return Number(value);
}

/** The exact value of an integer as decimal. */
export function toDecimal(value: bigint): string {
    return value.toString();
}

/** The exact value of a `double` as decimal, e.g. `0.1000000000000000055511151231257827021181583404541015625`. */
export function toDecimalDouble(value: number): string {
    if (!Number.isFinite(value)) {
        fail(`${value} has no decimal value`);
    }
    if (Number.isInteger(value)) {
        return BigInt(value).toString();
    }
    // value = mantissa * 2^exponent with an odd mantissa = mantissa * 5^-exponent / 10^-exponent
    const view = new DataView(new ArrayBuffer(8));
    view.setFloat64(0, value);
    const bits = view.getBigUint64(0);
    const biased = Number((bits >> 52n) & 0x7ffn);
    let mantissa = bits & 0xfffffffffffffn;
    let exponent = biased === 0 ? -1074 : biased - 1075;
    if (biased !== 0) {
        mantissa |= 1n << 52n;
    }
    while (mantissa % 2n === 0n) {
        mantissa /= 2n;
        exponent++;
    }
    const sign = bits >> 63n === 1n ? -1n : 1n;
    return formatDecimal(sign * mantissa * 5n ** BigInt(-exponent), -exponent);
}
