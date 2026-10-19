package org.hiero.consensusnode.queries.files;

import static org.hiero.sdk.testing.Testing.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.ledger.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `queries-files`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("queries-files")
class QueriesFilesSpecTest {

    /// `consensusnode.queries.files/the-file-queries-keep-the-id-of-their-file`
    ///
    /// Source: the Python SDK (file_contents_query_test.py and file_info_query_test.py, test_constructor and
    /// test_get_method) and the Swift SDK (FileContentsQueryUnitTests and FileInfoQueryUnitTests, the get / set tests).
    @Test
    @DisplayName("the file queries keep the id of their file")
    @Tag("testId:consensusnode.queries.files/the-file-queries-keep-the-id-of-their-file")
    void theFileQueriesKeepTheIdOfTheirFile() {
        // given
        final Address file = defaultAddress();

        // when
        final FileContentsQuery contents = new FileContentsQuery(file);
        final FileInfoQuery info = new FileInfoQuery(file);

        // then
        assertEquals(file, contents.fileId());
        assertEquals(file, info.fileId());
    }

    /// `consensusnode.queries.files/a-file-info-of-an-immutable-file-without-memo-has-no-authority-and-no-memo`
    ///
    /// Source: this spec (the authority is absent if the file is immutable); the Python SDK (file_info_test.py,
    /// test_file_info_default_initialization and test_to_proto_with_none_values).
    @Test
    @DisplayName("a file info of an immutable file without memo has no authority and no memo")
    @Tag("testId:consensusnode.queries.files/a-file-info-of-an-immutable-file-without-memo-has-no-authority-and-no-memo")
    void aFileInfoOfAnImmutableFileWithoutMemoHasNoAuthorityAndNoMemo() {
        // when
        final FileInfo info = new FileInfo(defaultAddress(), 0L, ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), false, null, "");

        // then
        assertEquals(defaultAddress(), info.fileId());
        assertEquals(0L, info.size());
        assertFalse(info.deleted());
        assertNull(info.authority());
        assertEquals("", info.fileMemo());
    }

    /// `consensusnode.queries.files/a-file-info-keeps-its-expiration-time`
    ///
    /// Source: the Java SDK (FileInfoTest, the file info of its fixture): expiration time Instant.ofEpochMilli(3), i.e.
    /// 1970-01-01T00:00:00.003Z. Millisecond precision.
    @Test
    @DisplayName("a file info keeps its expiration time")
    @Tag("testId:consensusnode.queries.files/a-file-info-keeps-its-expiration-time")
    void aFileInfoKeepsItsExpirationTime() {
        // given
        final ZonedDateTime expirationTime = toZonedDateTime("1970-01-01T00:00:00.003Z");

        // when
        final FileInfo info = new FileInfo(defaultAddress(), 0L, expirationTime, false, null, "");

        // then
        assertEquals(toZonedDateTime("1970-01-01T00:00:00.003Z"), info.expirationTime());
    }

    private static Address defaultAddress() {
        return new Address(0L, 0L, "", 1001L);
    }
}
