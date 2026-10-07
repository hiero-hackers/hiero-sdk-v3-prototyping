package org.hiero.ledger.internal;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.TransactionId;
import org.jspecify.annotations.Nullable;

/// The transaction id of the consensus node: the payer plus the instant from which the transaction is valid.
public final class DefaultTransactionId extends TransactionId {

    /// The valid start is dated back by this much, so that a clock that runs ahead of the node does not
    /// produce `INVALID_TRANSACTION_START`.
    private static final long BACKDATE_NANOS = 10_000_000_000L;

    private static final long NANOS_PER_MILLI = 1_000_000L;

    /// Keeps the generated instants strictly increasing within this process.
    private static final AtomicLong LAST_NANOS = new AtomicLong();

    /// Creates a transaction id.
    ///
    /// @param accountId  the payer
    /// @param validStart the instant from which the transaction is valid
    /// @param nonce      the nonce of a child transaction, `null` for a user transaction
    public DefaultTransactionId(final AccountId accountId, final ZonedDateTime validStart,
                                final @Nullable Integer nonce) {
        super(accountId, validStart, nonce);
    }

    /// Generates a transaction id for a payer, with a valid start that is unique within this process.
    ///
    /// @param payer the payer
    /// @return the transaction id
    public static DefaultTransactionId generate(final AccountId payer) {
        long nanos;
        long previous;
        do {
            previous = LAST_NANOS.get();
            nanos = System.currentTimeMillis() * NANOS_PER_MILLI - BACKDATE_NANOS;
            if (nanos <= previous) {
                nanos = previous + 1_000L;
            }
        } while (!LAST_NANOS.compareAndSet(previous, nanos));
        final long jittered = nanos + ThreadLocalRandom.current().nextLong(1_000L);
        final ZonedDateTime validStart =
                ZonedDateTime.ofInstant(Instant.ofEpochSecond(0L, jittered), ZoneOffset.UTC);
        return new DefaultTransactionId(payer, validStart, null);
    }

    @Override
    public String toString() {
        final Instant instant = validStart().toInstant();
        final StringBuilder text = new StringBuilder()
                .append(accountId())
                .append('@')
                .append(instant.getEpochSecond())
                .append('.')
                .append(String.format("%09d", instant.getNano()));
        final Integer nonce = nonce();
        if (nonce != null) {
            text.append('/').append(nonce);
        }
        return text.toString();
    }

    @Override
    public String toStringWithChecksum() {
        return toString();
    }
}
