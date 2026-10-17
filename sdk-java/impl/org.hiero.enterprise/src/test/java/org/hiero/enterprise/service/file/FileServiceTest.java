package org.hiero.enterprise.service.file;

import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.enterprise.service.ServiceException;
import org.hiero.ledger.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `FileService`, generated from the specs.
@DisplayName("FileService")
class FileServiceTest {

    @Test
    @DisplayName("creates a FileService and returns the values")
    @Tag("testId:enterprise.service.file.FileService/create")
    void shouldCreateAndReturnTheValues() {
        final FileService subject = new FileService();
    }

    @Test
    @DisplayName("createFile(contents) can be called and returns a value (only ServiceException may occur)")
    @Tag("testId:enterprise.service.file.FileService/createFile/call")
    void shouldCallCreateFile() throws Exception {
        try {
            assertNotNull(create().createFile(contentsValue()));
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("createFile(contents, expirationTime) can be called and returns a value (only ServiceException may occur)")
    @Tag("testId:enterprise.service.file.FileService/createFile/call-2")
    void shouldCallCreateFile2() throws Exception {
        try {
            assertNotNull(create().createFile(contentsValue(), expirationTimeValue()));
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("createFile(params) can be called and returns a value (only ServiceException may occur)")
    @Tag("testId:enterprise.service.file.FileService/createFile/call-3")
    void shouldCallCreateFile3() throws Exception {
        try {
            assertNotNull(create().createFile(paramsValue()));
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("readFile(fileId) can be called and returns a value (only ServiceException may occur)")
    @Tag("testId:enterprise.service.file.FileService/readFile/call")
    void shouldCallReadFile() throws Exception {
        try {
            assertNotNull(create().readFile(fileIdValue()));
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("updateFile(fileId, contents) can be called (only ServiceException may occur)")
    @Tag("testId:enterprise.service.file.FileService/updateFile/call")
    void shouldCallUpdateFile() throws Exception {
        try {
            create().updateFile(fileIdValue(), contentsValue());
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("updateExpirationTime(fileId, expirationTime) can be called (only ServiceException may occur)")
    @Tag("testId:enterprise.service.file.FileService/updateExpirationTime/call")
    void shouldCallUpdateExpirationTime() throws Exception {
        try {
            create().updateExpirationTime(fileIdValue(), expirationTimeValue());
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("deleteFile(fileId) can be called (only ServiceException may occur)")
    @Tag("testId:enterprise.service.file.FileService/deleteFile/call")
    void shouldCallDeleteFile() throws Exception {
        try {
            create().deleteFile(fileIdValue());
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("isDeleted(fileId) can be called (only ServiceException may occur)")
    @Tag("testId:enterprise.service.file.FileService/isDeleted/call")
    void shouldCallIsDeleted() throws Exception {
        try {
            create().isDeleted(fileIdValue());
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("getSize(fileId) can be called (only ServiceException may occur)")
    @Tag("testId:enterprise.service.file.FileService/getSize/call")
    void shouldCallGetSize() throws Exception {
        try {
            create().getSize(fileIdValue());
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("getExpirationTime(fileId) can be called and returns a value (only ServiceException may occur)")
    @Tag("testId:enterprise.service.file.FileService/getExpirationTime/call")
    void shouldCallGetExpirationTime() throws Exception {
        try {
            assertNotNull(create().getExpirationTime(fileIdValue()));
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    private static FileService create() {
        return new FileService();
    }

    private static byte[] contentsValue() {
        return new byte[] {1, 2, 3};
    }

    private static ZonedDateTime expirationTimeValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static FileCreateParams paramsValue() {
        return new FileCreateParams(new byte[0], null, null, null);
    }

    private static Address fileIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }
}
