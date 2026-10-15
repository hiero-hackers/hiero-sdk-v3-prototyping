# Built-in functions of the test language

## Description

The built-in functions and assertions of the tests in the specs (namespace `testing`). They exist only in tests.
The source of truth is the section "Built-in functions" of `guidelines/test-language.md`; this copy is what the
tooling reads, and a test keeps both equal.

## API Schema

```
namespace testing

// --- assertions ------------------------------------------------------------------------------

// Fails the test unless actual is equal to expected (value equality).
@@static void assertEquals<$$T>(@@nullable expected: $$T, @@nullable actual: $$T, @@nullable message: string)

// Fails the test if actual is equal to unexpected (value equality).
@@static void assertNotEquals<$$T>(@@nullable unexpected: $$T, @@nullable actual: $$T, @@nullable message: string)

// Fails the test unless the condition is true.
@@static void assertTrue(condition: bool, @@nullable message: string)

// Fails the test unless the condition is false.
@@static void assertFalse(condition: bool, @@nullable message: string)

// Fails the test unless the value is null.
@@static void assertNull<$$T>(@@nullable actual: $$T, @@nullable message: string)

// Fails the test if the value is null.
@@static void assertNotNull<$$T>(@@nullable actual: $$T, @@nullable message: string)

// Fails the test unless actual is an instance of the expected type.
@@static void assertInstanceOf<$$T, $$V>(expected: type<$$T>, @@nullable actual: $$V, @@nullable message: string)

// Fails the test unless actual differs from expected by at most tolerance.
@@static void assertApprox(expected: double, actual: double, tolerance: double, @@nullable message: string)

// Fails the test unconditionally.
@@static void fail(message: string)

// --- booleans --------------------------------------------------------------------------------

// The negation of the value.
@@static bool not(value: bool)

// --- strings ---------------------------------------------------------------------------------

// Number of code points of the string.
@@static int64 length(value: string)

// The code points from start (inclusive) to end (exclusive).
@@static string substring(value: string, start: int64, end: int64)

// Whether the string contains the part.
@@static bool contains(value: string, part: string)

// Whether the string starts with the prefix.
@@static bool startsWith(value: string, prefix: string)

// Whether the string ends with the suffix.
@@static bool endsWith(value: string, suffix: string)

// The string repeated count times, for boundary tests.
@@static string repeat(value: string, count: int64)

// --- bytes -----------------------------------------------------------------------------------

// Number of bytes.
@@static int64 length(value: bytes)

// The bytes from start (inclusive) to end (exclusive).
@@static bytes slice(value: bytes, start: int64, end: int64)

// Whether the bytes contain the sequence part.
@@static bool contains(value: bytes, part: bytes)

// Whether the bytes start with the prefix.
@@static bool startsWith(value: bytes, prefix: bytes)

// Whether the bytes end with the suffix.
@@static bool endsWith(value: bytes, suffix: bytes)

// The bytes repeated count times.
@@static bytes repeat(value: bytes, count: int64)

// Bytes from hex text: case-insensitive, whitespace between bytes allowed.
@@static bytes hex(text: string)

// Bytes from Base64 text (RFC 4648, with padding).
@@static bytes base64(text: string)

// The UTF-8 encoding of the text.
@@static bytes utf8(text: string)

// Lowercase hex of the bytes, without separators.
@@static string toHex(value: bytes)

// --- collections -----------------------------------------------------------------------------

// Number of elements.
@@static int64 length<$$T>(value: list<$$T>)

// Number of elements.
@@static int64 length<$$T>(value: set<$$T>)

// Number of entries.
@@static int64 length<$$K, $$V>(value: map<$$K, $$V>)

// The elements from start (inclusive) to end (exclusive).
@@static list<$$T> slice<$$T>(value: list<$$T>, start: int64, end: int64)

// Whether the list contains an element equal to the given one.
@@static bool contains<$$T>(value: list<$$T>, element: $$T)

// Whether the set contains an element equal to the given one.
@@static bool contains<$$T>(value: set<$$T>, element: $$T)

// Whether the map contains the key.
@@static bool containsKey<$$K, $$V>(value: map<$$K, $$V>, key: $$K)

// The keys of the map, in an unspecified order.
@@static list<$$K> keys<$$K, $$V>(value: map<$$K, $$V>)

// The values of the map, in an unspecified order.
@@static list<$$V> values<$$K, $$V>(value: map<$$K, $$V>)

// The integers from start (inclusive) to end (exclusive).
@@static list<int64> range(start: int64, end: int64)

// --- numbers ---------------------------------------------------------------------------------

// The absolute value.
@@static int256 abs(value: int256)

// The absolute value.
@@static double abs(value: double)

// The absolute value.
@@static decimal abs(value: decimal)

// The smaller of the two values.
@@static int256 min(a: int256, b: int256)

// The smaller of the two values.
@@static double min(a: double, b: double)

// The smaller of the two values.
@@static decimal min(a: decimal, b: decimal)

// The larger of the two values.
@@static int256 max(a: int256, b: int256)

// The larger of the two values.
@@static double max(a: double, b: double)

// The larger of the two values.
@@static decimal max(a: decimal, b: decimal)

// Whether actual differs from expected by at most tolerance.
@@static bool approx(actual: double, expected: double, tolerance: double)

// Whether the value is NaN.
@@static bool isNaN(value: double)

// The quotient, rounded half-even to scale digits after the point.
@@static decimal divide(dividend: decimal, divisor: decimal, scale: int32)

// --- conversions -----------------------------------------------------------------------------

// Decimal text without grouping, e.g. "-1234".
@@static string toString(value: int256)

// Decimal text without grouping, e.g. "0.5".
@@static string toString(value: decimal)

// Parses decimal text; text that is no integer fails the test.
@@static int256 toInt(text: string)

// The nearest double.
@@static double toDouble(value: int256)

// The nearest double.
@@static double toDouble(value: decimal)

// The exact value.
@@static decimal toDecimal(value: int256)

// The exact value.
@@static decimal toDecimal(value: double)
```

## Testing

None.

## Questions & Comments

None.
