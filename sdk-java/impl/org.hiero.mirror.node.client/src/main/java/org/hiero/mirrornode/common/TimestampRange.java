package org.hiero.mirrornode.common;

import java.time.ZonedDateTime;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/// The period an entity or a value is valid for: from the inclusive start to the exclusive end. A period that has not
/// ended has no end.
///
/// @param from inclusive start
/// @param to exclusive end; absent while the period lasts
public record TimestampRange(ZonedDateTime from, @Nullable ZonedDateTime to) {

    /// Creates a new `TimestampRange`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public TimestampRange {
        Objects.requireNonNull(from, "from must not be null");
    }
}
