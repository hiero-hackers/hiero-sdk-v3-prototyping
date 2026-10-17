package org.hiero.consensusnode.client.internal;

import com.hederahashgraph.api.proto.java.TransactionBody;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Stream;
import org.hiero.consensusnode.transactions.spi.TransactionSupport;
import org.jspecify.annotations.Nullable;

/// All transaction types the implementation knows: the built-in ones of this module and those other modules of the
/// SDK register as `TransactionSupport` with the `ServiceLoader` (the administration client).
public final class TransactionKinds {

    private static final List<TransactionKind<?, ?>> BUILT_IN = Stream.of(AccountKinds.ALL, FileKinds.ALL,
            TopicKinds.ALL, ScheduleKinds.ALL, TokenKinds.ALL, UtilityKinds.ALL).flatMap(List::stream).toList();

    private static volatile @Nullable List<TransactionKind<?, ?>> all;

    private TransactionKinds() {
    }

    /// All transaction types.
    ///
    /// @return the built-in types and those of the registered supports
    public static List<TransactionKind<?, ?>> all() {
        List<TransactionKind<?, ?>> kinds = all;
        if (kinds == null) {
            final Stream<TransactionKind<?, ?>> loaded = ServiceLoader.load(TransactionSupport.class).stream()
                    .map(ServiceLoader.Provider::get).filter(KindSupport.class::isInstance)
                    .map(support -> ((KindSupport<?, ?>) support).kind());
            kinds = Stream.concat(BUILT_IN.stream(), loaded).distinct().toList();
            all = kinds;
        }
        return kinds;
    }

    /// The kind of a transaction body.
    ///
    /// @param body the body
    /// @return the kind, `null` if no type fills the field of the body
    public static @Nullable TransactionKind<?, ?> of(final TransactionBody body) {
        return all().stream().filter(kind -> kind.dataCase() == body.getDataCase() && kind.selects().test(body))
                .findFirst().orElse(null);
    }

    /// The kind of a transaction type.
    ///
    /// @param type the transaction type
    /// @return the kind, `null` if the type is not known
    public static @Nullable TransactionKind<?, ?> of(final Class<?> type) {
        return all().stream().filter(kind -> kind.type().equals(type)).findFirst().orElse(null);
    }
}
