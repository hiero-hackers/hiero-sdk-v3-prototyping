package org.hiero.consensusnode.queries.files;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.Address;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `FileInfo`, generated from the specs.
@DisplayName("FileInfo")
class FileInfoTest {

    @Test
    @DisplayName("creates a FileInfo and returns the values")
    @Tag("testId:consensusnode.queries.files.FileInfo/create")
    void shouldCreateAndReturnTheValues() {
        final Address fileId = fileIdValue();
        final long size = sizeValue();
        final ZonedDateTime expirationTime = expirationTimeValue();
        final boolean deleted = deletedValue();
        final @Nullable Authority authority = authorityValue();
        final @Nullable String fileMemo = fileMemoValue();
        final FileInfo subject = new FileInfo(fileId, size, expirationTime, deleted, authority, fileMemo);
        assertValue(fileId, subject.fileId());
        assertValue(size, subject.size());
        assertValue(expirationTime, subject.expirationTime());
        assertValue(deleted, subject.deleted());
        assertValue(authority, subject.authority());
        assertValue(fileMemo, subject.fileMemo());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:consensusnode.queries.files.FileInfo/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final Address fileId = fileIdValue();
        final long size = sizeValue();
        final ZonedDateTime expirationTime = expirationTimeValue();
        final boolean deleted = deletedValue();
        final @Nullable Authority authority = authorityValue();
        final @Nullable String fileMemo = fileMemoValue();
        final FileInfo subject = FileInfo.builder()
                .fileId(fileId)
                .size(size)
                .expirationTime(expirationTime)
                .deleted(deleted)
                .authority(authority)
                .fileMemo(fileMemo)
                .build();
        assertValue(fileId, subject.fileId());
        assertValue(size, subject.size());
        assertValue(expirationTime, subject.expirationTime());
        assertValue(deleted, subject.deleted());
        assertValue(authority, subject.authority());
        assertValue(fileMemo, subject.fileMemo());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:consensusnode.queries.files.FileInfo/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final FileInfo subject = FileInfo.builder()
                .fileId(fileIdValue())
                .size(sizeValue())
                .expirationTime(expirationTimeValue())
                .deleted(deletedValue())
                .build();
        assertNull(subject.authority());
        assertNull(subject.fileMemo());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:consensusnode.queries.files.FileInfo/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> FileInfo.builder().build());
    }

    @Test
    @DisplayName("rejects null for fileId")
    @Tag("testId:consensusnode.queries.files.FileInfo/fileId/rejects-null")
    void shouldRejectNullFileId() {
        assertThrows(NullPointerException.class, () -> new FileInfo((Address) null, sizeValue(), expirationTimeValue(), deletedValue(), authorityValue(), fileMemoValue()));
    }

    @Test
    @DisplayName("size: accepts the minimum (-9223372036854775808)")
    @Tag("testId:consensusnode.queries.files.FileInfo/size/accepts-at-minimum")
    void shouldAcceptSizeAtMinimum() {
        final long value = -9223372036854775808L;
        final FileInfo subject = new FileInfo(fileIdValue(), value, expirationTimeValue(), deletedValue(), authorityValue(), fileMemoValue());
        assertValue(value, subject.size());
    }

    @Test
    @DisplayName("size: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.queries.files.FileInfo/size/accepts-at-maximum")
    void shouldAcceptSizeAtMaximum() {
        final long value = 9223372036854775807L;
        final FileInfo subject = new FileInfo(fileIdValue(), value, expirationTimeValue(), deletedValue(), authorityValue(), fileMemoValue());
        assertValue(value, subject.size());
    }

    @Test
    @DisplayName("rejects null for expirationTime")
    @Tag("testId:consensusnode.queries.files.FileInfo/expirationTime/rejects-null")
    void shouldRejectNullExpirationTime() {
        assertThrows(NullPointerException.class, () -> new FileInfo(fileIdValue(), sizeValue(), (ZonedDateTime) null, deletedValue(), authorityValue(), fileMemoValue()));
    }

    private static Address fileIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static long sizeValue() {
        return 1L;
    }

    private static ZonedDateTime expirationTimeValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static boolean deletedValue() {
        return true;
    }

    private static Authority authorityValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static String fileMemoValue() {
        return "value";
    }

    private static FileInfo create() {
        return new FileInfo(fileIdValue(), sizeValue(), expirationTimeValue(), deletedValue(), authorityValue(), fileMemoValue());
    }
}
