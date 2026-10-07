package org.hiero.sdk.v3.common;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;

/**
 * An immutable page of asynchronously navigable Mirror Node data.
 *
 * @param <T> the type of each value in the page
 */
public abstract class Page<T> {
    private final List<T> data;
    private final int size;
    private final int pageIndex;

    /**
     * Creates a page and snapshots its data.
     *
     * @param data the ordered page values
     * @param size the declared page size
     * @param pageIndex the zero-based page index
     * @throws NullPointerException if {@code data} or one of its elements is {@code null}
     */
    protected Page(final List<T> data, final int size, final int pageIndex) {
        this.data = List.copyOf(Objects.requireNonNull(data, "data"));
        this.size = size;
        this.pageIndex = pageIndex;
    }

    /**
     * Returns the immutable ordered data snapshot.
     *
     * @return the immutable page data
     */
    public final List<T> data() {
        return data;
    }

    /**
     * Returns the declared page size.
     *
     * @return the declared page size
     */
    public final int size() {
        return size;
    }

    /**
     * Returns the zero-based page index.
     *
     * @return the zero-based page index
     */
    public final int pageIndex() {
        return pageIndex;
    }

    /**
     * Reports whether another page is available without retrieving it.
     *
     * @return {@code true} when another page is available
     */
    public abstract boolean hasNext();

    /**
     * Reports whether this is the first page without retrieving it.
     *
     * @return {@code true} when this is the first page
     */
    public abstract boolean isFirst();

    /**
     * Retrieves the next page asynchronously.
     *
     * @return a stage that completes with the next page, or exceptionally with
     *         {@link MirrorNodeException} when the Mirror Node request fails
     */
    public abstract CompletionStage<Page<T>> next();

    /**
     * Retrieves the first page associated with the same paginated operation asynchronously.
     *
     * @return a stage that completes with the first page, or exceptionally with
     *         {@link MirrorNodeException} when the Mirror Node request fails
     */
    public abstract CompletionStage<Page<T>> first();
}
