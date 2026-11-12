package org.hiero.consensusnode.transactions.spi;

import org.hiero.consensusnode.client.internal.Statuses;
import org.hiero.consensusnode.transactions.Transaction;
import org.hiero.consensusnode.transactions.Receipt;
import org.hiero.consensusnode.client.internal.TransactionKinds;
import org.hiero.consensusnode.client.internal.TransactionKind;
import org.hiero.consensusnode.client.internal.KindSupport;
import java.util.ServiceLoader;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;
import java.util.Set;
import org.hiero.consensusnode.client.StatusConflictException;
import org.hiero.consensusnode.client.UnknownStatusException;
import org.hiero.consensusnode.status.TransactionStatus;

/// Factory methods of the package `org.hiero.consensusnode.transactions.spi`.
public final class SpiFactory {

    /// returns the TransactionSupport for the given transaction type; throws if none is registered
    ///
    /// @throws NoSuchElementException if a not found error occurs
    public static TransactionSupport<?, ?> getTransactionSupport(final Class<?> transactionType) {
        return getAllTransactionSupports().stream().filter(s -> s.getTransactionType().equals(transactionType))
                .findFirst().orElseThrow(() -> new NoSuchElementException("No transaction support is registered for "
                        + transactionType.getName()));
    }

    /// returns all TransactionSupport instances
    public static Set<TransactionSupport<?, ?>> getAllTransactionSupports() {
        // the types of the SDK first, then those of other providers (a custom service); one support per type
        final Map<Class<?>, TransactionSupport<?, ?>> supports = new LinkedHashMap<>();
        TransactionKinds.all().forEach(kind -> supports.put(kind.type(), support(kind)));
        ServiceLoader.load(TransactionSupport.class).stream().map(ServiceLoader.Provider::get)
                .forEach(support -> supports.putIfAbsent(support.getTransactionType(), support));
        return Set.copyOf(supports.values());
    }

    private static <R extends Receipt, T extends Transaction<R, T>> TransactionSupport<R, T> support(
            final TransactionKind<R, T> kind) {
        return new KindSupport<>(kind);
    }

    /// Returns every registered TransactionStatusSupport, among them the one of BasicTransactionStatus. They are loaded once,
    /// when the first client is created or this function or getTransactionStatus is called first. Throws if two supports
    /// name the same code.
    ///
    /// @throws StatusConflictException if a status conflict error occurs
    public static Set<TransactionStatusSupport> getAllTransactionStatusSupports() {
        return Statuses.supports();
    }

    /// Returns the status of a code: the status of the registered TransactionStatusSupport that names it. Throws if no
    /// support names the code, or if the registered supports conflict.
    ///
    /// @throws UnknownStatusException if an unknown status error occurs
    /// @throws StatusConflictException if a status conflict error occurs
    public static TransactionStatus getTransactionStatus(final int code) {
        return Statuses.ofCode(code);
    }

    private SpiFactory() {
        throw new UnsupportedOperationException("Factory class cannot be instantiated");
    }
}
