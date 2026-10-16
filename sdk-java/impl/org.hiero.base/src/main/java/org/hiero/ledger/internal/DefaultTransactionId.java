package org.hiero.ledger.internal;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.TransactionId;
import org.jspecify.annotations.Nullable;

/// The only concrete `TransactionId`: the payer, the valid start and the nonce of a child transaction.
public final class DefaultTransactionId extends TransactionId {

    /// The valid start lies this far in the past, so that a node whose clock is behind still accepts it.
    private static final long BACKDATE_NANOS = 10_000_000_000L;

    private static final AtomicLong LAST_NANOS = new AtomicLong();

    /// Creates a transaction id.
    ///
    /// @param accountId  the payer
    /// @param validStart the valid start
    /// @param nonce      the nonce of a child transaction, `null` for a top-level transaction
    public DefaultTransactionId(final AccountId accountId, final ZonedDateTime validStart,
                                final @Nullable Integer nonce) {
        super(accountId, validStart, nonce);
    }

    /// Generates the id of a new transaction: a valid start ~10 s in the past that is strictly increasing within the
    /// process, so that two transactions of the same payer never share an id.
    ///
    /// @param payer the payer
    /// @return the id
    public static DefaultTransactionId generate(final AccountId payer) {
        long nanos;
        long previous;
        do {
            previous = LAST_NANOS.get();
            nanos = System.currentTimeMillis() * 1_000_000L - BACKDATE_NANOS
                    + ThreadLocalRandom.current().nextLong(1_000L);
            if (nanos <= previous) {
                nanos = previous + 1;
            }
        } while (!LAST_NANOS.compareAndSet(previous, nanos));
        return new DefaultTransactionId(payer, ZonedDateTime.ofInstant(Instant.ofEpochSecond(0L, nanos),
                ZoneOffset.UTC), null);
    }

    @Override
    public String toString() {
        return text(accountId().toString());
    }

    @Override
    public String toStringWithChecksum() {
        return text(accountId().toStringWithChecksum());
    }

    private String text(final String account) {
        final Instant start = validStart().toInstant();
        final Integer nonce = nonce();
        return account + "@" + start.getEpochSecond() + "." + String.format("%09d", start.getNano())
                + (nonce == null ? "" : "/" + nonce);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof TransactionId other && accountId().equals(other.accountId())
                && validStart().toInstant().equals(other.validStart().toInstant())
                && Objects.equals(nonce(), other.nonce());
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId(), validStart().toInstant(), nonce());
    }
}
