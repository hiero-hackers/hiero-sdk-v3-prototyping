package org.hiero.sdk.v3.fixture;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.hiero.sdk.v3.common.Page;

public record PageConsumer<T>(List<T> data, int size, int pageIndex) implements Page<T> {
    public PageConsumer { data = List.copyOf(data); }
    @Override public boolean hasNext() { return false; }
    @Override public boolean isFirst() { return pageIndex == 0; }
    @Override public CompletionStage<Page<T>> next() { return CompletableFuture.completedFuture(this); }
    @Override public CompletionStage<Page<T>> first() { return CompletableFuture.completedFuture(this); }
}
