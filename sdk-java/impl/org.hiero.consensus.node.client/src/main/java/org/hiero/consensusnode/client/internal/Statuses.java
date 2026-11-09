package org.hiero.consensusnode.client.internal;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.hiero.consensusnode.status.BasicTransactionStatus;

/// The status of a HAPI status code: the `BasicTransactionStatus` with that code.
public final class Statuses {

    private static final Map<Integer, BasicTransactionStatus> BY_CODE = Arrays.stream(BasicTransactionStatus.values())
            .collect(Collectors.toUnmodifiableMap(BasicTransactionStatus::code, Function.identity()));

    private Statuses() {
    }

    /// Returns the status of a code.
    ///
    /// @param code the HAPI code
    /// @return the status
    /// @throws IllegalArgumentException if HAPI defines no such code
    public static BasicTransactionStatus ofCode(final int code) {
        final BasicTransactionStatus status = BY_CODE.get(code);
        if (status == null) {
            throw new IllegalArgumentException("Unknown HAPI status code " + code);
        }
        return status;
    }
}
