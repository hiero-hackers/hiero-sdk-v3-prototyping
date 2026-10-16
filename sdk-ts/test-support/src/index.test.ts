// The semantics of the test language that the TypeScript tests generated from the specs rely on; the counterpart of
// TestingTest of the Java test support module.

import assert from "node:assert/strict";
import { AssertionError } from "node:assert";
import { describe, test } from "node:test";
import {
    abs, absDecimal, addDecimal, assertEquals, assertValue, assertFalse, assertInstanceOf, assertNotEquals, assertNotNull,
    assertNull, assertTrue, base64, checked, checkedNumber, compare, compareDecimal, concat, contains, containsKey,
    decimal, divide, endsWith, equal, get, hex, int, keys, length, max, maxDecimal, min, multiplyDecimal,
    negateDecimal, range, repeat, slice, startsWith, subtractDecimal, substring, textDecimal, toDecimalDouble, toHex,
    toInt, utf8, values, toZonedDateTime, toDateTime, toDate, toTime, toDuration, toSeconds,
} from "./index.js";
import { Duration } from "@hiero/support";

class Point {
    readonly #x: number;
    readonly #label: string;

    constructor(x: number, label: string) {
        this.#x = x;
        this.#label = label;
    }

    get x(): number {
        return this.#x;
    }

    get label(): string {
        return this.#label;
    }
}

class Amount {
    constructor(readonly tinybars: bigint) {
    }

    equals(other: unknown): boolean {
        return other instanceof Amount && other.tinybars === this.tinybars;
    }
}

describe("test support", () => {
    test("strings count code points", () => {
        const emoji = "a😀b";
        assert.equal(length(emoji), 3n);
        assert.equal(substring(emoji, 1n, 2n), "😀");
        assert.equal(length("ℏ"), 1n);
        assert.throws(() => substring(emoji, 2n, 4n), AssertionError);
        assert.ok(compare("z", "😀") < 0 && compare("ab", "a") > 0 && compare("a", "a") === 0);
    });

    test("equality compares values", () => {
        assert.ok(equal(new Uint8Array([1, 2]), new Uint8Array([1, 2])));
        assert.ok(equal([new Uint8Array([1])], [new Uint8Array([1])]));
        assert.ok(equal(new Set([1, 2]), new Set([2, 1])));
        assert.ok(equal(3, 3n) && equal(10n, 10));
        assert.ok(!equal(1.5, 1n));
        assert.ok(!equal(Number.NaN, Number.NaN));
        assert.ok(equal(0.0, -0.0));
        assert.ok(equal(null, null) && equal(null, undefined) && !equal(null, 1));
        assert.ok(!equal([1, 2], [2, 1]));
        assert.ok(equal(new Map([["k", new Uint8Array([1])]]), new Map([["k", new Uint8Array([1])]])));
        assert.ok(!equal(new Set([1]), [1]));
        assert.ok(equal(new Date(5), new Date(5)) && !equal(new Date(5), new Date(6)));
        // a class by its getters, a value with equals by that method
        assert.ok(equal(new Point(1, "a"), new Point(1, "a")) && !equal(new Point(1, "a"), new Point(2, "a")));
        assert.ok(equal(new Amount(1n), new Amount(1n)) && !equal(new Amount(1n), new Amount(2n)));
        assert.ok(!equal({}, {}) && !equal(new Point(1, "a"), new Amount(1n)));
    });

    test("integers are exact", () => {
        assert.equal(int(7), 7n);
        assert.throws(() => int(1.5), AssertionError);
        assert.equal(-7n / 2n, -3n);
        assert.equal(-7n % 2n, -1n);
        assert.equal(checked(255n, "uint8"), 255n);
        assert.equal(checkedNumber(-128n, "int8"), -128);
        assert.throws(() => checked(256n, "uint8"), AssertionError);
        assert.throws(() => checked(-129n, "int8"), AssertionError);
        assert.equal(abs(-3n), 3n);
        assert.equal(min(1n, 2n), 1n);
        assert.equal(max(1n, 2n), 2n);
        assert.equal(toInt("+42"), 42n);
        assert.throws(() => toInt("4.2"), AssertionError);
    });

    test("decimals are exact", () => {
        assert.equal(addDecimal("1.5", "2.25"), "3.75");
        assert.equal(subtractDecimal("1", "0.25"), "0.75");
        assert.equal(multiplyDecimal("-1.5", "0.2"), "-0.30");
        assert.equal(negateDecimal("0.5"), "-0.5");
        assert.equal(absDecimal("-0.5"), "0.5");
        assert.equal(maxDecimal("1.5", "1.25"), "1.5");
        assert.equal(compareDecimal("1.0", "1.00"), 0);
        assert.equal(decimal("1.500"), "1.5");
        assert.equal(decimal("10"), "10");
        assert.equal(textDecimal("+1.50"), "1.50");
        assert.equal(divide("1", "3", 4), "0.3333");
        assert.equal(divide("2", "3", 0), "1");
        assert.equal(divide("5", "2", 0), "2", "half-even rounds 2.5 to 2");
        assert.equal(divide("-7", "2", 0), "-4", "half-even rounds -3.5 to -4");
        assert.throws(() => divide("1", "0", 2), AssertionError);
        assert.equal(toDecimalDouble(0.5), "0.5");
        assert.equal(toDecimalDouble(0.1), "0.1000000000000000055511151231257827021181583404541015625");
        assert.equal(toDecimalDouble(-2), "-2");
        assert.throws(() => toDecimalDouble(Number.NaN), AssertionError);
        assert.throws(() => addDecimal("1e5", "1"), AssertionError);
    });

    test("bytes are read and searched", () => {
        assert.deepEqual(hex("0A 1b"), new Uint8Array([0x0a, 0x1b]));
        assert.deepEqual(base64("Chs="), new Uint8Array([0x0a, 0x1b]));
        assert.deepEqual(utf8("hi"), new Uint8Array([0x68, 0x69]));
        assert.equal(toHex(new Uint8Array([0x0a, 0x1b])), "0a1b");
        assert.ok(contains(new Uint8Array([1, 2, 3]), new Uint8Array([2, 3])));
        assert.ok(startsWith(new Uint8Array([1, 2, 3]), new Uint8Array([1])));
        assert.ok(endsWith(new Uint8Array([1, 2, 3]), new Uint8Array([3])));
        assert.deepEqual(repeat(new Uint8Array([1, 2]), 2n), new Uint8Array([1, 2, 1, 2]));
        assert.deepEqual(concat(new Uint8Array([1]), new Uint8Array([2])), new Uint8Array([1, 2]));
        assert.deepEqual(slice(new Uint8Array([1, 2, 3]), 1n, 3n), new Uint8Array([2, 3]));
        assert.throws(() => hex("zz"), AssertionError);
        assert.throws(() => base64("Chs"), AssertionError);
        assert.throws(() => repeat("x", -1n), AssertionError);
    });

    test("collections are indexed", () => {
        assert.equal(get(["a", "b"], 1n), "b");
        assert.throws(() => get(["a"], 1n), AssertionError);
        assert.equal(get(new Map([["a", 1]]), "a"), 1);
        assert.equal(get(new Map<unknown, string>([[3n, "x"]]), 3), "x", "integer keys compare by value");
        assert.throws(() => get(new Map([["a", 1]]), "b"), AssertionError);
        assert.deepEqual(range(0n, 3n), [0n, 1n, 2n]);
        assert.deepEqual(concat([1], [2]), [1, 2]);
        assert.ok(contains([new Uint8Array([1])], new Uint8Array([1])));
        assert.ok(contains(new Set(["a"]), "a"));
        assert.ok(containsKey(new Map([[1n, 1]]), 1) && !containsKey(new Map([[1n, 1]]), 2));
        assert.deepEqual(keys(new Map([["a", 1]])), ["a"]);
        assert.deepEqual(values(new Map([["a", 1]])), [1]);
        assert.equal(length(new Map([["a", 1]])), 1n);
        assert.equal(length(new Set([1, 2])), 2n);
        assert.equal(length([1, 2, 3]), 3n);
    });

    test("assertions fail with assertion errors", () => {
        assert.doesNotThrow(() => assertEquals(new Uint8Array([1]), new Uint8Array([1])));
        assert.throws(() => assertEquals(1, 2, "numbers"), /numbers: expected 1 but was 2/);
        assert.throws(() => assertNotEquals(1, 1n), AssertionError);
        assert.throws(() => assertTrue(false), AssertionError);
        assert.throws(() => assertFalse(true), AssertionError);
        assert.throws(() => assertNull("x"), AssertionError);
        assert.throws(() => assertNotNull(null), AssertionError);
        assert.throws(() => assertInstanceOf(Point, new Amount(1n)), AssertionError);
        assert.doesNotThrow(() => assertInstanceOf(Point, new Point(1, "a")));
        assert.throws(() => compare({}, {}), AssertionError);
        // the comparison of the contract tests: by equals, by content, otherwise by identity
        assert.doesNotThrow(() => assertValue(new Amount(1n), new Amount(1n)));
        assert.throws(() => assertValue(new Amount(1n), new Amount(2n)), AssertionError);
        assert.doesNotThrow(() => assertValue(new Uint8Array([1]), new Uint8Array([1])));
        assert.throws(() => assertValue([1], [2]), AssertionError);
        assert.throws(() => assertValue(new Point(1, "a"), new Point(1, "a")), AssertionError);
        assert.throws(() => assertValue(1, 1n), AssertionError);
    });
    test("date, time and durations are read from ISO 8601", () => {
        assert.equal(toZonedDateTime("2022-01-02T02:00:01.5Z").toISOString(), "2022-01-02T02:00:01.500Z");
        assert.equal(toZonedDateTime("2022-01-02T03:00:01+01:00").toISOString(), "2022-01-02T02:00:01.000Z");
        assert.equal(toZonedDateTime("2022-01-02T02:00:01.500000000Z").getTime(), Date.UTC(2022, 0, 2, 2, 0, 1, 500));
        assert.throws(() => toZonedDateTime("2022-01-02T02:00:01.000000002Z"), /more precise/);
        assert.throws(() => toZonedDateTime("2022-01-02T02:00:01"), AssertionError);
        assert.equal(toDateTime("2022-01-02T02:00:01").toISOString(), "2022-01-02T02:00:01.000Z");
        assert.equal(toDate("2022-01-02").toISOString(), "2022-01-02T00:00:00.000Z");
        assert.throws(() => toDate("2022-02-30"), AssertionError);
        assert.equal(toTime("02:00:01.5").toISOString(), "1970-01-01T02:00:01.500Z");
        assert.ok(equal(toDuration("PT1.5S"), Duration.ofMillis(1500)));
        assert.ok(equal(toDuration("P1DT2H3M4S"), Duration.ofMillis(((24 + 2) * 60 + 3) * 60000 + 4000)));
        assert.throws(() => toDuration("1.5 seconds"), AssertionError);
        assert.throws(() => toDuration("PT"), AssertionError);
        assert.ok(equal(toSeconds(7776000n), Duration.ofSeconds(7776000)));
        assert.ok(compare(toSeconds(89n), toSeconds(90n)) < 0 && compare(toDuration("PT1M"), toDuration("PT60S")) === 0);
    });
});
