package org.hiero.enterprise.service.internal;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import org.hiero.common.Page;
import org.jspecify.annotations.Nullable;

/// A page whose items are the items of another page, converted one by one; an item the conversion maps to `null` is
/// left out. Loading the next or the first page loads it from the source page and converts it as well.
///
/// @param <S> the type of the items of the source page
/// @param <T> the type of the items of this page
public final class MappedPage<S, T> extends Page<T> {

    private final Page<S> source;

    private final Function<? super S, ? extends @Nullable T> mapper;

    private MappedPage(final Page<S> source, final List<T> data, final Function<? super S, ? extends @Nullable T> mapper) {
        super(data, source.size(), source.pageIndex());
        this.source = source;
        this.mapper = mapper;
    }

    /// Converts the items of a page.
    ///
    /// @param source the page
    /// @param mapper the conversion of one item, `null` to leave the item out
    /// @param <S>    the type of the items of the source page
    /// @param <T>    the type of the items of the new page
    /// @return the converted page
    public static <S, T> Page<T> of(final Page<S> source, final Function<? super S, ? extends @Nullable T> mapper) {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(mapper, "mapper must not be null");
        final List<T> data = source.data().stream().<@Nullable T>map(mapper).filter(Objects::nonNull)
                .toList();
        return new MappedPage<>(source, data, mapper);
    }

    @Override
    public boolean hasNext() {
        return source.hasNext();
    }

    @Override
    public boolean isFirst() {
        return source.isFirst();
    }

    @Override
    public CompletionStage<Page<T>> next() {
        return source.next().thenApply(page -> of(page, mapper));
    }

    @Override
    public CompletionStage<Page<T>> first() {
        return source.first().thenApply(page -> of(page, mapper));
    }
}
