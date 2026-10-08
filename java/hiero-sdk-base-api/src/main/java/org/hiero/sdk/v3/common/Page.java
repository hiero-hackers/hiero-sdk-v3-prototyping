// Source: spec/base/common.md (common.Page).

package org.hiero.sdk.v3.common;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;

/** An immutable page of asynchronously navigable mirror-node data. */
public abstract class Page<T> {
    private final List<T> data;
    private final int size;
    private final int pageIndex;

    /** Creates a page base and snapshots its non-null data. */
    protected Page(List<T> data, int size, int pageIndex) {
        this.data = List.copyOf(Objects.requireNonNull(data, "data"));
        this.size = size;
        this.pageIndex = pageIndex;
    }

    /** Returns the immutable ordered data snapshot. */
    public final List<T> data() { return data; }
    /** Returns the declared number of elements in this page. */
    public final int size() { return size; }
    /** Returns the zero-based page index. */
    public final int pageIndex() { return pageIndex; }
    /** Returns whether a next page exists. */
    public abstract boolean hasNext();
    /** Returns whether this is the first page. */
    public abstract boolean isFirst();
    /** Returns the next page without blocking; mirror-node failures complete exceptionally. */
    public abstract CompletionStage<Page<T>> next();
    /** Returns the first page without blocking; mirror-node failures complete exceptionally. */
    public abstract CompletionStage<Page<T>> first();
}
