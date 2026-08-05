// Generated from spec/base/common.md (common#Page); DO NOT EDIT.

package org.hiero.sdk.v3.common;

import java.util.List;
import java.util.concurrent.CompletionStage;

/** An immutable page of asynchronously navigable mirror-node data. */
public interface Page<T> {
    /** Returns an immutable ordered data snapshot. */
    List<T> data();
    /** Returns the number of elements in this page. */
    int size();
    /** Returns the zero-based page index. */
    int pageIndex();
    /** Returns whether a next page exists. */
    boolean hasNext();
    /** Returns whether this is the first page. */
    boolean isFirst();
    /** Returns the next page without blocking; mirror-node failures complete exceptionally. */
    CompletionStage<Page<T>> next();
    /** Returns the first page without blocking; mirror-node failures complete exceptionally. */
    CompletionStage<Page<T>> first();
}
