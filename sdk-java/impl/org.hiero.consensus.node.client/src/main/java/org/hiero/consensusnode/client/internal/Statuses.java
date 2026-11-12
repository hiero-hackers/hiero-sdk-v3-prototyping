package org.hiero.consensusnode.client.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import org.hiero.consensusnode.client.StatusConflictException;
import org.hiero.consensusnode.client.UnknownStatusException;
import org.hiero.consensusnode.status.TransactionStatus;
import org.hiero.consensusnode.transactions.spi.TransactionStatusSupport;
import org.jspecify.annotations.Nullable;

/// The status of a HAPI status code: the status of the registered `TransactionStatusSupport` that names it. The support
/// of `BasicTransactionStatus` is always registered (first); the others come from the `ServiceLoader`. The supports are
/// loaded once; two statuses with the same code are a conflict, which every later call reports again.
public final class Statuses {

    private static final Object LOCK = new Object();

    private static @Nullable Map<Integer, TransactionStatus> byCode;

    private static @Nullable Set<TransactionStatusSupport> supports;

    private static @Nullable StatusConflictException conflict;

    private Statuses() {
    }

    /// Loads the registered supports, if not done yet.
    ///
    /// @throws StatusConflictException if two statuses name the same code
    public static void load() {
        statuses();
    }

    /// Returns the registered supports.
    ///
    /// @return the supports
    /// @throws StatusConflictException if two statuses name the same code
    public static Set<TransactionStatusSupport> supports() {
        statuses();
        return supports;
    }

    /// Returns the status of a code.
    ///
    /// @param code the HAPI code
    /// @return the status
    /// @throws UnknownStatusException if no registered support names the code
    /// @throws StatusConflictException if two statuses name the same code
    public static TransactionStatus ofCode(final int code) {
        final TransactionStatus status = statuses().get(code);
        if (status == null) {
            throw new UnknownStatusException("No status is known for the code " + code
                    + "; register a TransactionStatusSupport that names it", null);
        }
        return status;
    }

    /// The name of a status: the name of its enum constant, or `STATUS_<code>` for a status that is no enum constant.
    ///
    /// @param status the status
    /// @return the name
    public static String name(final TransactionStatus status) {
        return status instanceof Enum<?> constant ? constant.name() : "STATUS_" + status.code();
    }

    private static Map<Integer, TransactionStatus> statuses() {
        synchronized (LOCK) {
            if (conflict != null) {
                throw conflict;
            }
            if (byCode == null) {
                final Map<Integer, TransactionStatus> statuses = new HashMap<>();
                final Set<TransactionStatusSupport> loaded = new LinkedHashSet<>();
                // the codes of the protocol first, then those of the other modules; one support per class
                final List<TransactionStatusSupport> candidates = new ArrayList<>();
                candidates.add(new BasicStatusSupport());
                ServiceLoader.load(TransactionStatusSupport.class).forEach(candidates::add);
                final Set<Class<?>> classes = new HashSet<>();
                for (final TransactionStatusSupport support : candidates) {
                    if (!classes.add(support.getClass())) {
                        continue;
                    }
                    loaded.add(support);
                    for (final TransactionStatus status : support.getStatuses()) {
                        final TransactionStatus other = statuses.putIfAbsent(status.code(), status);
                        if (other != null) {
                            conflict = new StatusConflictException("The code " + status.code() + " is named twice: "
                                    + name(other) + " (" + other.getClass().getName() + ") and " + name(status)
                                    + " (" + status.getClass().getName() + ")", null);
                            throw conflict;
                        }
                    }
                }
                byCode = Map.copyOf(statuses);
                supports = Set.copyOf(loaded);
            }
            return byCode;
        }
    }
}
