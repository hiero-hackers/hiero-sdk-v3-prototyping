package org.hiero.enterprise.service.file;

import java.time.ZonedDateTime;
import org.hiero.enterprise.service.ServiceException;
import org.hiero.enterprise.service.internal.Unbound;
import org.hiero.ledger.Address;

public class FileService {

    /// Creates a new `FileService` that is not bound to a session: every method fails with `ServiceException`.
    /// A usable service is created with `FileFactory.createService(session)`.
    public FileService() {
    }

    /// Create a new file with the given contents; returns the id of the new file. The same as createFile with
    /// FileCreateParams that have only these values.
    ///
    /// @throws ServiceException if a service error occurs
    public Address createFile(final byte[] contents) {
        throw Unbound.error("FileService", "FileFactory");
    }

    /// @throws ServiceException if a service error occurs
    public Address createFile(final byte[] contents, final ZonedDateTime expirationTime) {
        throw Unbound.error("FileService", "FileFactory");
    }

    /// Create a new file with the given parameters; returns the id of the new file: the general form of the methods
    /// above, for every option of a new file.
    ///
    /// @throws ServiceException if a service error occurs
    public Address createFile(final FileCreateParams params) {
        throw Unbound.error("FileService", "FileFactory");
    }

    /// Read the full contents of a file
    ///
    /// @throws ServiceException if a service error occurs
    public byte[] readFile(final Address fileId) {
        throw Unbound.error("FileService", "FileFactory");
    }

    /// Replace the contents of a file
    ///
    /// @throws ServiceException if a service error occurs
    public void updateFile(final Address fileId, final byte[] contents) {
        throw Unbound.error("FileService", "FileFactory");
    }

    /// Update only the expiration time of a file
    ///
    /// @throws ServiceException if a service error occurs
    public void updateExpirationTime(final Address fileId, final ZonedDateTime expirationTime) {
        throw Unbound.error("FileService", "FileFactory");
    }

    /// Delete a file
    ///
    /// @throws ServiceException if a service error occurs
    public void deleteFile(final Address fileId) {
        throw Unbound.error("FileService", "FileFactory");
    }

    /// Returns true if the file has been deleted
    ///
    /// @throws ServiceException if a service error occurs
    public boolean isDeleted(final Address fileId) {
        throw Unbound.error("FileService", "FileFactory");
    }

    /// Size of the file contents in bytes
    ///
    /// @throws ServiceException if a service error occurs
    public int getSize(final Address fileId) {
        throw Unbound.error("FileService", "FileFactory");
    }

    /// Expiration time of the file
    ///
    /// @throws ServiceException if a service error occurs
    public ZonedDateTime getExpirationTime(final Address fileId) {
        throw Unbound.error("FileService", "FileFactory");
    }
}
