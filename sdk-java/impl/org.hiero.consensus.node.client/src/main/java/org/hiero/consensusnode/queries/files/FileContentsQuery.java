package org.hiero.consensusnode.queries.files;

import java.time.Duration;
import org.hiero.consensusnode.client.RetryPolicy;
import org.hiero.consensusnode.client.internal.FileQueries;
import org.hiero.consensusnode.client.internal.Querying;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.queries.PaidQuery;
import org.hiero.consensusnode.queries.PaidQueryResponse;
import org.hiero.ledger.Address;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// Paid query for the raw byte content of a file. The fee scales with file size; bound
/// the spend with `maxQueryPayment` for large files.
public final class FileContentsQuery extends PaidQuery<FileContents> {

    private final Address fileId;

    /// Creates a new `FileContentsQuery`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public FileContentsQuery(final Address fileId) {
        super();
        Objects.requireNonNull(fileId, "fileId must not be null");
        this.fileId = fileId;
    }

    /// Returns the `fileId`.
    public Address fileId() {
        return fileId;
    }

    /// Sets the `retryPolicy`.
    ///
    /// @param retryPolicy the new value
    /// @return this object
    @Override
    public FileContentsQuery setRetryPolicy(final @Nullable RetryPolicy retryPolicy) {
        super.setRetryPolicy(retryPolicy);
        return this;
    }

    /// Sets the `attemptTimeout`.
    ///
    /// @param attemptTimeout the new value
    /// @return this object
    @Override
    public FileContentsQuery setAttemptTimeout(final @Nullable Duration attemptTimeout) {
        super.setAttemptTimeout(attemptTimeout);
        return this;
    }

    /// Sets the `maxQueryPayment`.
    ///
    /// @param maxQueryPayment the new value
    /// @return this object
    @Override
    public FileContentsQuery setMaxQueryPayment(final @Nullable NativeToken<?, ?> maxQueryPayment) {
        super.setMaxQueryPayment(maxQueryPayment);
        return this;
    }

    /// Sends this request to the network and returns the typed result asynchronously. Node selection, retries
    /// and operation-specific protocol details (for example cost discovery and payment for a `PaidQuery`) are
    /// handled transparently. Fails with `attempts-exhausted-error` when the retry policy gives up.
    ///
    /// The returned stage completes exceptionally with `AttemptsExhaustedException` if the operation fails.
    @Override
    public CompletionStage<PaidQueryResponse<FileContents>> submit(final HieroClient<?> client) {
        return Querying.paid(this, this, maxQueryPayment(), FileQueries.FILE_CONTENTS, client);
    }

    /// Asks the network for the current price of this query without executing it. The returned value is a
    /// snapshot — a subsequent call may return a different price as conditions change.
    /// Throws if `maxQueryPayment` is set and the quoted price exceeds it.
    @Override
    public CompletionStage<NativeToken<?, ?>> getCost(final HieroClient<?> client) {
        return Querying.cost(this, this, FileQueries.FILE_CONTENTS, client);
    }

    @Override
    public String toString() {
        return "FileContentsQuery[retryPolicy=" + retryPolicy()
                + ", attemptTimeout=" + attemptTimeout()
                + ", maxQueryPayment=" + maxQueryPayment()
                + ", fileId=" + fileId()
                + "]";
    }
}
