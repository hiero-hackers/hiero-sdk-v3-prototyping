package org.hiero.blocknode;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.blocknode.status.SemanticVersion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `BlockHeader`, generated from the specs.
@DisplayName("BlockHeader")
class BlockHeaderTest {

    @Test
    @DisplayName("creates a BlockHeader and returns the values")
    @Tag("testId:blocknode.BlockHeader/create")
    void shouldCreateAndReturnTheValues() {
        final long number = numberValue();
        final ZonedDateTime timestamp = timestampValue();
        final SemanticVersion hapiProtoVersion = hapiProtoVersionValue();
        final SemanticVersion softwareVersion = softwareVersionValue();
        final BlockHashAlgorithm hashAlgorithm = hashAlgorithmValue();
        final BlockHeader subject = new BlockHeader(number, timestamp, hapiProtoVersion, softwareVersion, hashAlgorithm);
        assertValue(number, subject.number());
        assertValue(timestamp, subject.timestamp());
        assertValue(hapiProtoVersion, subject.hapiProtoVersion());
        assertValue(softwareVersion, subject.softwareVersion());
        assertValue(hashAlgorithm, subject.hashAlgorithm());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:blocknode.BlockHeader/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final long number = numberValue();
        final ZonedDateTime timestamp = timestampValue();
        final SemanticVersion hapiProtoVersion = hapiProtoVersionValue();
        final SemanticVersion softwareVersion = softwareVersionValue();
        final BlockHashAlgorithm hashAlgorithm = hashAlgorithmValue();
        final BlockHeader subject = BlockHeader.builder()
                .number(number)
                .timestamp(timestamp)
                .hapiProtoVersion(hapiProtoVersion)
                .softwareVersion(softwareVersion)
                .hashAlgorithm(hashAlgorithm)
                .build();
        assertValue(number, subject.number());
        assertValue(timestamp, subject.timestamp());
        assertValue(hapiProtoVersion, subject.hapiProtoVersion());
        assertValue(softwareVersion, subject.softwareVersion());
        assertValue(hashAlgorithm, subject.hashAlgorithm());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:blocknode.BlockHeader/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final BlockHeader subject = BlockHeader.builder()
                .number(numberValue())
                .timestamp(timestampValue())
                .hapiProtoVersion(hapiProtoVersionValue())
                .softwareVersion(softwareVersionValue())
                .hashAlgorithm(hashAlgorithmValue())
                .build();
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:blocknode.BlockHeader/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> BlockHeader.builder().build());
    }

    @Test
    @DisplayName("number: accepts the minimum (0)")
    @Tag("testId:blocknode.BlockHeader/number/accepts-at-minimum")
    void shouldAcceptNumberAtMinimum() {
        final long value = 0L;
        final BlockHeader subject = new BlockHeader(value, timestampValue(), hapiProtoVersionValue(), softwareVersionValue(), hashAlgorithmValue());
        assertValue(value, subject.number());
    }

    @Test
    @DisplayName("number: accepts the maximum (18446744073709551615)")
    @Tag("testId:blocknode.BlockHeader/number/accepts-at-maximum")
    void shouldAcceptNumberAtMaximum() {
        final long value = Long.parseUnsignedLong("18446744073709551615");
        final BlockHeader subject = new BlockHeader(value, timestampValue(), hapiProtoVersionValue(), softwareVersionValue(), hashAlgorithmValue());
        assertValue(value, subject.number());
    }

    @Test
    @DisplayName("rejects null for timestamp")
    @Tag("testId:blocknode.BlockHeader/timestamp/rejects-null")
    void shouldRejectNullTimestamp() {
        assertThrows(NullPointerException.class, () -> new BlockHeader(numberValue(), (ZonedDateTime) null, hapiProtoVersionValue(), softwareVersionValue(), hashAlgorithmValue()));
    }

    @Test
    @DisplayName("rejects null for hapiProtoVersion")
    @Tag("testId:blocknode.BlockHeader/hapiProtoVersion/rejects-null")
    void shouldRejectNullHapiProtoVersion() {
        assertThrows(NullPointerException.class, () -> new BlockHeader(numberValue(), timestampValue(), (SemanticVersion) null, softwareVersionValue(), hashAlgorithmValue()));
    }

    @Test
    @DisplayName("rejects null for softwareVersion")
    @Tag("testId:blocknode.BlockHeader/softwareVersion/rejects-null")
    void shouldRejectNullSoftwareVersion() {
        assertThrows(NullPointerException.class, () -> new BlockHeader(numberValue(), timestampValue(), hapiProtoVersionValue(), (SemanticVersion) null, hashAlgorithmValue()));
    }

    @Test
    @DisplayName("rejects null for hashAlgorithm")
    @Tag("testId:blocknode.BlockHeader/hashAlgorithm/rejects-null")
    void shouldRejectNullHashAlgorithm() {
        assertThrows(NullPointerException.class, () -> new BlockHeader(numberValue(), timestampValue(), hapiProtoVersionValue(), softwareVersionValue(), (BlockHashAlgorithm) null));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:blocknode.BlockHeader/value-semantics")
    void shouldCompareByValue() {
        final BlockHeader subject = create();
        final BlockHeader other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static long numberValue() {
        return 1L;
    }

    private static ZonedDateTime timestampValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static SemanticVersion hapiProtoVersionValue() {
        return new SemanticVersion(0, 44, 2, "", "");
    }

    private static SemanticVersion softwareVersionValue() {
        return new SemanticVersion(0, 44, 2, "", "");
    }

    private static BlockHashAlgorithm hashAlgorithmValue() {
        return BlockHashAlgorithm.SHA2_384;
    }

    private static BlockHeader create() {
        return new BlockHeader(numberValue(), timestampValue(), hapiProtoVersionValue(), softwareVersionValue(), hashAlgorithmValue());
    }

    private static BlockHeader defaultSubject() {
        return new BlockHeader(1L, ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), new SemanticVersion(0, 70, 0, "", ""), new SemanticVersion(0, 70, 0, "", ""), BlockHashAlgorithm.SHA2_384);
    }
}
