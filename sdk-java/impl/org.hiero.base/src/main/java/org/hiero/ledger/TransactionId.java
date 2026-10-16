package org.hiero.ledger;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.hiero.ledger.internal.DefaultTransactionId;
import org.jspecify.annotations.Nullable;

/// Id of a transaction
public abstract class TransactionId {

    private final AccountId accountId;
    private final ZonedDateTime validStart;
    private final @Nullable Integer nonce;

    /// Creates a new `TransactionId`.
    ///
    /// @throws NullPointerException if a required value is `null`
    protected TransactionId(
            final AccountId accountId,
            final ZonedDateTime validStart,
            final @Nullable Integer nonce) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        this.accountId = accountId;
        Objects.requireNonNull(validStart, "validStart must not be null");
        this.validStart = validStart;
        this.nonce = nonce;
    }

    /// the account that is the payer of the transaction
    public AccountId accountId() {
        return accountId;
    }

    /// the start time of the transaction
    public ZonedDateTime validStart() {
        return validStart;
    }

    /// nonce of an internal transaction
    public @Nullable Integer nonce() {
        return nonce;
    }

    /// returns the transaction id as a string
    @Override
    public abstract String toString();

    /// returns the transaction id as a string with a checksum
    public abstract String toStringWithChecksum();

    /// Generates a new TransactionId for the given payer account.
    public static TransactionId generateTransactionId(final Address accountId) {
        return DefaultTransactionId.generate(new AccountId(accountId.shard(), accountId.realm(), accountId.checksum(),
                accountId.num(), null, null));
    }

    /// Parses a TransactionId from its string form. Throws if the format is invalid.
    ///
    /// @throws IllegalArgumentException if an illegal format error occurs
    public static TransactionId fromString(final String transactionId) {
        final Matcher matcher = Pattern.compile("([^@]+)@(\\d+)\\.(\\d{1,9})(?:/(\\d+))?").matcher(transactionId);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("'" + transactionId + "' is no transaction id of the form "
                    + "account@seconds.nanos or account@seconds.nanos/nonce");
        }
        // the digits after the dot are the nanoseconds as a number, as in all Hiero SDKs - not a decimal fraction
        final Instant start = Instant.ofEpochSecond(Long.parseLong(matcher.group(2)), Long.parseLong(matcher.group(3)));
        return new DefaultTransactionId(AccountId.fromString(matcher.group(1)), ZonedDateTime.ofInstant(start, ZoneOffset.UTC),
                matcher.group(4) == null ? null : Integer.valueOf(matcher.group(4)));
    }
}
