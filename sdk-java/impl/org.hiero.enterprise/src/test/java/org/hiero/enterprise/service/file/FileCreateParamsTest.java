package org.hiero.enterprise.service.file;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `FileCreateParams`, generated from the specs.
@DisplayName("FileCreateParams")
class FileCreateParamsTest {

    @Test
    @DisplayName("creates a FileCreateParams and returns the values")
    @Tag("testId:enterprise.service.file.FileCreateParams/create")
    void shouldCreateAndReturnTheValues() {
        final byte[] contents = contentsValue();
        final @Nullable Authority authority = authorityValue();
        final @Nullable ZonedDateTime expirationTime = expirationTimeValue();
        final @Nullable String fileMemo = fileMemoValue();
        final FileCreateParams subject = new FileCreateParams(contents, authority, expirationTime, fileMemo);
        assertValue(contents, subject.contents());
        assertValue(authority, subject.authority());
        assertValue(expirationTime, subject.expirationTime());
        assertValue(fileMemo, subject.fileMemo());
    }

    @Test
    @DisplayName("creates a FileCreateParams with the default values")
    @Tag("testId:enterprise.service.file.FileCreateParams/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final FileCreateParams subject = new FileCreateParams(authorityValue(), expirationTimeValue(), fileMemoValue());
        assertValue(new byte[0], subject.contents());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:enterprise.service.file.FileCreateParams/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final byte[] contents = contentsValue();
        final @Nullable Authority authority = authorityValue();
        final @Nullable ZonedDateTime expirationTime = expirationTimeValue();
        final @Nullable String fileMemo = fileMemoValue();
        final FileCreateParams subject = FileCreateParams.builder()
                .contents(contents)
                .authority(authority)
                .expirationTime(expirationTime)
                .fileMemo(fileMemo)
                .build();
        assertValue(contents, subject.contents());
        assertValue(authority, subject.authority());
        assertValue(expirationTime, subject.expirationTime());
        assertValue(fileMemo, subject.fileMemo());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:enterprise.service.file.FileCreateParams/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final FileCreateParams subject = FileCreateParams.builder()
                .build();
        assertValue(new byte[0], subject.contents());
        assertNull(subject.authority());
        assertNull(subject.expirationTime());
        assertNull(subject.fileMemo());
    }

    @Test
    @DisplayName("rejects null for contents")
    @Tag("testId:enterprise.service.file.FileCreateParams/contents/rejects-null")
    void shouldRejectNullContents() {
        assertThrows(NullPointerException.class, () -> new FileCreateParams((byte[]) null, authorityValue(), expirationTimeValue(), fileMemoValue()));
    }

    @Test
    @DisplayName("the constructor copies contents; the accessor returns a copy")
    @Tag("testId:enterprise.service.file.FileCreateParams/contents/copies-in")
    void shouldCopyContentsIn() {
        final byte[] value = contentsValue();
        final byte[] expected = value.clone();
        final FileCreateParams subject = new FileCreateParams(value, authorityValue(), expirationTimeValue(), fileMemoValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.contents());
        final byte[] returned = subject.contents();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.contents());
    }

    @Test
    @DisplayName("fileMemo: accepts more elements than the maximum")
    @Tag("testId:enterprise.service.file.FileCreateParams/fileMemo/accepts-at-maximum-bytes")
    void shouldAcceptFileMemoAtMaximumBytes() {
        final @Nullable String value = "ääääääääääääääääääääääääääääääääääääääääääääääääää";
        final FileCreateParams subject = new FileCreateParams(contentsValue(), authorityValue(), expirationTimeValue(), value);
        assertValue(value, subject.fileMemo());
    }

    @Test
    @DisplayName("fileMemo: rejects more elements than the maximum")
    @Tag("testId:enterprise.service.file.FileCreateParams/fileMemo/rejects-above-maximum-bytes")
    void shouldRejectFileMemoAboveMaximumBytes() {
        assertThrows(IllegalArgumentException.class, () -> new FileCreateParams(contentsValue(), authorityValue(), expirationTimeValue(), "ääääääääääääääääääääääääääääääääääääääääääääääääääa"));
    }

    private static byte[] contentsValue() {
        return new byte[] {1, 2, 3};
    }

    private static Authority authorityValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static ZonedDateTime expirationTimeValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static String fileMemoValue() {
        return "value";
    }

    private static FileCreateParams create() {
        return new FileCreateParams(contentsValue(), authorityValue(), expirationTimeValue(), fileMemoValue());
    }

    private static FileCreateParams defaultSubject() {
        return new FileCreateParams(new byte[0], null, null, null);
    }
}
