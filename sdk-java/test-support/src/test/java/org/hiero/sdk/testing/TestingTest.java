package org.hiero.sdk.testing;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

/// The semantics of the test language that `Testing` implements where Java behaves differently.
class TestingTest {

    @Test
    @DisplayName("strings count code points, not UTF-16 units")
    void stringsShouldCountCodePoints() {
        final String emoji = "a😀b";
        assertEquals(3, Testing.length(emoji));
        assertEquals("😀", Testing.substring(emoji, 1, 2));
        assertEquals(1, Testing.length("ℏ"));
        assertThrows(AssertionFailedError.class, () -> Testing.substring(emoji, 2, 4));
    }

    @Test
    @DisplayName("equality compares values: bytes by content, sets without order, integers by value")
    void equalityShouldCompareValues() {
        assertTrue(Testing.equal(new byte[] {1, 2}, new byte[] {1, 2}));
        assertTrue(Testing.equal(List.of(new byte[] {1}), List.of(new byte[] {1})));
        assertTrue(Testing.equal(new LinkedHashSet<>(List.of(1, 2)), new LinkedHashSet<>(List.of(2, 1))));
        assertTrue(Testing.equal(3, 3L));
        assertTrue(Testing.equal(BigInteger.TEN, 10L));
        assertTrue(Testing.equal(new BigDecimal("1.0"), new BigDecimal("1.00")));
        assertFalse(Testing.equal(Double.NaN, Double.NaN));
        assertTrue(Testing.equal(0.0, -0.0));
        assertTrue(Testing.equal(null, null));
        assertFalse(Testing.equal(null, 1));
        assertFalse(Testing.equal(List.of(1, 2), List.of(2, 1)));
        final Map<String, byte[]> a = new LinkedHashMap<>();
        a.put("k", new byte[] {1});
        assertTrue(Testing.equal(a, Map.of("k", new byte[] {1})));
        assertFalse(Testing.equal(Set.of(1), List.of(1)));
    }

    @Test
    @DisplayName("integers are exact and checked when they leave the test language")
    void integersShouldBeExact() {
        assertEquals(new BigInteger("18446744073709551615"), Testing.unsigned(-1L));
        assertEquals(BigInteger.valueOf(-3), Testing.integer(-7).divide(Testing.integer(2)));
        assertEquals(BigInteger.valueOf(-1), Testing.integer(-7).remainder(Testing.integer(2)));
        assertDoesNotThrow(() -> Testing.checked(BigInteger.valueOf(255), "uint8"));
        assertThrows(AssertionFailedError.class, () -> Testing.checked(BigInteger.valueOf(256), "uint8"));
        assertThrows(AssertionFailedError.class, () -> Testing.checked(BigInteger.valueOf(-129), "int8"));
    }

    @Test
    @DisplayName("bytes are read from hex, Base64 and UTF-8 and compared as sequences")
    void bytesShouldBeReadAndSearched() {
        assertArrayEquals(new byte[] {0x0a, 0x1b}, Testing.hex("0A 1b"));
        assertArrayEquals(new byte[] {0x0a, 0x1b}, Testing.base64("Chs="));
        assertArrayEquals(new byte[] {0x68, 0x69}, Testing.utf8("hi"));
        assertEquals("0a1b", Testing.toHex(new byte[] {0x0a, 0x1b}));
        assertTrue(Testing.contains(new byte[] {1, 2, 3}, new byte[] {2, 3}));
        assertTrue(Testing.startsWith(new byte[] {1, 2, 3}, new byte[] {1}));
        assertTrue(Testing.endsWith(new byte[] {1, 2, 3}, new byte[] {3}));
        assertArrayEquals(new byte[] {1, 2, 1, 2}, Testing.repeat(new byte[] {1, 2}, 2));
        assertArrayEquals(new byte[] {1, 2}, Testing.concat(new byte[] {1}, new byte[] {2}));
        assertThrows(AssertionFailedError.class, () -> Testing.hex("zz"));
    }

    @Test
    @DisplayName("collections are indexed with range checks and missing keys fail")
    void collectionsShouldBeIndexed() {
        assertEquals("b", Testing.get(List.of("a", "b"), 1));
        assertThrows(AssertionFailedError.class, () -> Testing.get(List.of("a"), 1));
        assertEquals(1, Testing.get(Map.of("a", 1), "a"));
        assertThrows(AssertionFailedError.class, () -> Testing.get(Map.of("a", 1), "b"));
        assertEquals(List.of(0L, 1L, 2L), Testing.range(0, 3));
        assertEquals(List.of(1, 2), Testing.concat(List.of(1), List.of(2)));
        assertTrue(Testing.contains(List.of(new byte[] {1}), new byte[] {1}));
    }

    @Test
    @DisplayName("assertions fail with the assertion errors of JUnit")
    void assertionsShouldFail() {
        assertDoesNotThrow(() -> Testing.assertEquals(new byte[] {1}, new byte[] {1}));
        assertThrows(AssertionFailedError.class, () -> Testing.assertEquals(1, 2, "numbers"));
        assertThrows(AssertionFailedError.class, () -> Testing.assertNotEquals(1, 1L));
        assertThrows(AssertionFailedError.class, () -> Testing.assertTrue(false));
        assertThrows(AssertionFailedError.class, () -> Testing.assertNull("x"));
        assertThrows(AssertionFailedError.class, () -> Testing.assertNotNull(null));
        assertThrows(AssertionFailedError.class, () -> Testing.assertInstanceOf(String.class, 1));
        assertThrows(AssertionFailedError.class, () -> Testing.assertApprox(1.0, 1.2, 0.1));
        assertThrows(AssertionFailedError.class, () -> Testing.fail("always"));
    }

    @Test
    @DisplayName("decimals divide half-even and convert exactly")
    void decimalsShouldDivideHalfEven() {
        assertEquals(new BigDecimal("0.12"), Testing.divide(new BigDecimal("0.125"), BigDecimal.ONE, 2));
        assertEquals("0.5", Testing.text(new BigDecimal("0.5")));
        assertEquals(new BigInteger("-12"), Testing.toInt("-12"));
        assertThrows(AssertionFailedError.class, () -> Testing.toInt("x"));
    }

    @Test
    @DisplayName("date, time and durations are read from ISO 8601")
    void dateAndTimeShouldBeReadFromIso8601() {
        assertEquals(java.time.ZonedDateTime.of(2022, 1, 2, 2, 0, 1, 2, java.time.ZoneOffset.UTC),
                Testing.toZonedDateTime("2022-01-02T02:00:01.000000002Z"));
        assertEquals(java.time.LocalDateTime.of(2022, 1, 2, 2, 0, 1), Testing.toDateTime("2022-01-02T02:00:01"));
        assertEquals(java.time.LocalDate.of(2022, 1, 2), Testing.toDate("2022-01-02"));
        assertEquals(java.time.LocalTime.of(2, 0, 1, 500_000_000), Testing.toTime("02:00:01.5"));
        assertEquals(java.time.Duration.ofMillis(1500), Testing.toDuration("PT1.5S"));
        assertEquals(java.time.Duration.ofSeconds(7_776_000), Testing.toSeconds(7_776_000));
        assertThrows(AssertionFailedError.class, () -> Testing.toZonedDateTime("2022-01-02T02:00:01"));
        assertThrows(AssertionFailedError.class, () -> Testing.toDate("2022-13-01"));
        assertThrows(AssertionFailedError.class, () -> Testing.toDuration("1.5 seconds"));
    }
}
