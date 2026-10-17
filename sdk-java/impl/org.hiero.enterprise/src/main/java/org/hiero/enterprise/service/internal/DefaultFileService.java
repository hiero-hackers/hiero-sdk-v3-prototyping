package org.hiero.enterprise.service.internal;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Objects;
import org.hiero.authority.Authority;
import org.hiero.consensusnode.queries.files.FileContentsQuery;
import org.hiero.consensusnode.queries.files.FileInfo;
import org.hiero.consensusnode.queries.files.FileInfoQuery;
import org.hiero.consensusnode.transactions.files.FileAppendTransaction;
import org.hiero.consensusnode.transactions.files.FileCreateTransaction;
import org.hiero.consensusnode.transactions.files.FileDeleteTransaction;
import org.hiero.consensusnode.transactions.files.FileUpdateTransaction;
import org.hiero.enterprise.service.ServiceException;
import org.hiero.enterprise.service.file.FileCreateParams;
import org.hiero.enterprise.service.file.FileService;
import org.hiero.ledger.Address;

/// The `FileService` of a session. Contents larger than one transaction are written in chunks: the first chunk with
/// the create or update transaction, the others with append transactions. A file without an explicit authority is
/// controlled by the operator account, so that the service can change and delete it later.
public final class DefaultFileService extends FileService {

    /// The largest chunk written by one transaction.
    static final int CHUNK_SIZE = 2048;

    /// The largest file the network accepts (1024 KiB).
    static final int MAX_FILE_SIZE = 1024 * 1024;

    private final DefaultSession session;

    /// Creates the service of a session.
    ///
    /// @param session the session
    public DefaultFileService(final DefaultSession session) {
        this.session = Objects.requireNonNull(session, "session must not be null");
    }

    @Override
    public Address createFile(final byte[] contents) {
        Objects.requireNonNull(contents, "contents must not be null");
        return createFile(FileCreateParams.builder().contents(contents).build());
    }

    @Override
    public Address createFile(final byte[] contents, final ZonedDateTime expirationTime) {
        Objects.requireNonNull(contents, "contents must not be null");
        Objects.requireNonNull(expirationTime, "expirationTime must not be null");
        return createFile(FileCreateParams.builder().contents(contents).expirationTime(expirationTime).build());
    }

    @Override
    public Address createFile(final FileCreateParams params) {
        Objects.requireNonNull(params, "params must not be null");
        final byte[] contents = params.contents();
        checkSize(contents);
        final Authority authority = params.authority() == null ? session.operatorAuthority() : params.authority();
        final Address fileId = session.execute(new FileCreateTransaction(firstChunk(contents), authority,
                params.expirationTime(), params.fileMemo())).fileId();
        append(fileId, contents);
        return fileId;
    }

    @Override
    public byte[] readFile(final Address fileId) {
        Objects.requireNonNull(fileId, "fileId must not be null");
        return session.query(new FileContentsQuery(fileId)).contents();
    }

    @Override
    public void updateFile(final Address fileId, final byte[] contents) {
        Objects.requireNonNull(fileId, "fileId must not be null");
        Objects.requireNonNull(contents, "contents must not be null");
        checkSize(contents);
        session.execute(new FileUpdateTransaction(fileId, firstChunk(contents), null, null, null));
        append(fileId, contents);
    }

    @Override
    public void updateExpirationTime(final Address fileId, final ZonedDateTime expirationTime) {
        Objects.requireNonNull(fileId, "fileId must not be null");
        Objects.requireNonNull(expirationTime, "expirationTime must not be null");
        session.execute(new FileUpdateTransaction(fileId, null, null, expirationTime, null));
    }

    @Override
    public void deleteFile(final Address fileId) {
        Objects.requireNonNull(fileId, "fileId must not be null");
        session.execute(new FileDeleteTransaction(fileId));
    }

    @Override
    public boolean isDeleted(final Address fileId) {
        return info(fileId).deleted();
    }

    @Override
    public int getSize(final Address fileId) {
        final long size = info(fileId).size();
        if (size > Integer.MAX_VALUE) {
            throw new ServiceException("File " + fileId + " has more than " + Integer.MAX_VALUE + " bytes", null);
        }
        return (int) size;
    }

    @Override
    public ZonedDateTime getExpirationTime(final Address fileId) {
        return info(fileId).expirationTime();
    }

    private FileInfo info(final Address fileId) {
        Objects.requireNonNull(fileId, "fileId must not be null");
        return session.query(new FileInfoQuery(fileId));
    }

    private void append(final Address fileId, final byte[] contents) {
        for (int offset = CHUNK_SIZE; offset < contents.length; offset += CHUNK_SIZE) {
            final byte[] chunk = Arrays.copyOfRange(contents, offset, Math.min(contents.length, offset + CHUNK_SIZE));
            try {
                session.execute(new FileAppendTransaction(fileId, chunk));
            } catch (final ServiceException e) {
                throw new ServiceException("File " + fileId + " holds only the first " + offset + " of "
                        + contents.length + " bytes: " + e.getMessage(), e);
            }
        }
    }

    private static byte[] firstChunk(final byte[] contents) {
        return Arrays.copyOf(contents, Math.min(contents.length, CHUNK_SIZE));
    }

    private static void checkSize(final byte[] contents) {
        if (contents.length > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("contents must be at most " + MAX_FILE_SIZE + " bytes long");
        }
    }
}
