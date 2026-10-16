package org.hiero.consensusnode.queries.files;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.hiero.ledger.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of `FileContents`, generated from the specs.
@DisplayName("FileContents")
class FileContentsTest {

    @Test
    @DisplayName("creates a FileContents and returns the values")
    void shouldCreateAndReturnTheValues() {
        final Address fileId = fileIdValue();
        final byte[] contents = contentsValue();
        final FileContents subject = new FileContents(fileId, contents);
        assertValue(fileId, subject.fileId());
        assertValue(contents, subject.contents());
    }

    @Test
    @DisplayName("rejects null for fileId")
    void shouldRejectNullFileId() {
        assertThrows(NullPointerException.class, () -> new FileContents((Address) null, contentsValue()));
    }

    @Test
    @DisplayName("rejects null for contents")
    void shouldRejectNullContents() {
        assertThrows(NullPointerException.class, () -> new FileContents(fileIdValue(), (byte[]) null));
    }

    @Test
    @DisplayName("the constructor copies contents; the accessor returns a copy")
    void shouldCopyContentsIn() {
        final byte[] value = contentsValue();
        final byte[] expected = value.clone();
        final FileContents subject = new FileContents(fileIdValue(), value);
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.contents());
        final byte[] returned = subject.contents();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.contents());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    void shouldCompareByValue() {
        final FileContents subject = create();
        final FileContents other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static Address fileIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static byte[] contentsValue() {
        return new byte[] {1, 2, 3};
    }

    private static FileContents create() {
        return new FileContents(fileIdValue(), contentsValue());
    }
}
