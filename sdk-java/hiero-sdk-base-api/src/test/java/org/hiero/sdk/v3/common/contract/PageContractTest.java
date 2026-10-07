package org.hiero.sdk.v3.common.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import org.hiero.sdk.v3.common.MirrorNodeException;
import org.hiero.sdk.v3.common.Page;
import org.junit.jupiter.api.Test;

class PageContractTest {
    @Test
    void rejectsNullData() {
        assertThatNullPointerException().isThrownBy(() -> page(null, 0, 0, false, true));
    }

    @Test
    void storesAnImmutableSnapshot() {
        final var source = new ArrayList<>(List.of("first", "second"));
        final var page = page(source, 2, 0, false, true);

        source.add("third");

        assertThat(page.data()).containsExactly("first", "second");
        assertThatThrownBy(() -> page.data().add("third"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void exposesDeclaredStateAndPosition() {
        final var page = page(List.of("value"), 7, 3, true, false);

        assertThat(page.size()).isEqualTo(7);
        assertThat(page.pageIndex()).isEqualTo(3);
        assertThat(page.hasNext()).isTrue();
        assertThat(page.isFirst()).isFalse();
    }

    @Test
    void completesNavigationWithTheRequestedPages() {
        final var first = page(List.of("first"), 1, 0, true, true);
        final var next = page(List.of("next"), 1, 2, false, false);
        final var current = new TestPage<>(
                List.of("current"),
                1,
                1,
                true,
                false,
                CompletableFuture.completedFuture(next),
                CompletableFuture.completedFuture(first));

        assertThat(current.next().toCompletableFuture().join()).isSameAs(next);
        assertThat(current.first().toCompletableFuture().join()).isSameAs(first);
    }

    @Test
    void completesNavigationExceptionallyWithMirrorNodeException() {
        final var failure = new MirrorNodeException("request failed");
        final CompletionStage<Page<String>> failedStage = CompletableFuture.failedFuture(failure);
        final var page = new TestPage<>(List.of(), 0, 0, false, true, failedStage, failedStage);

        assertThatThrownBy(() -> page.next().toCompletableFuture().join())
                .isInstanceOf(CompletionException.class)
                .satisfies(exception -> assertThat(exception.getCause()).isSameAs(failure));
        assertThatThrownBy(() -> page.first().toCompletableFuture().join())
                .isInstanceOf(CompletionException.class)
                .satisfies(exception -> assertThat(exception.getCause()).isSameAs(failure));
    }

    @Test
    void preservesMirrorNodeExceptionMessageAndCause() {
        final var cause = new IllegalStateException("transport failure");
        final var exception = new MirrorNodeException("request failed", cause);

        assertThat(exception).hasMessage("request failed");
        assertThat(exception.getCause()).isSameAs(cause);
    }

    private static <T> TestPage<T> page(
            final List<T> data,
            final int size,
            final int pageIndex,
            final boolean hasNext,
            final boolean isFirst) {
        final CompletionStage<Page<T>> unconfigured = new CompletableFuture<>();
        return new TestPage<>(data, size, pageIndex, hasNext, isFirst, unconfigured, unconfigured);
    }

    private static final class TestPage<T> extends Page<T> {
        private final boolean hasNext;
        private final boolean first;
        private final CompletionStage<Page<T>> nextPage;
        private final CompletionStage<Page<T>> firstPage;

        private TestPage(
                final List<T> data,
                final int size,
                final int pageIndex,
                final boolean hasNext,
                final boolean first,
                final CompletionStage<Page<T>> nextPage,
                final CompletionStage<Page<T>> firstPage) {
            super(data, size, pageIndex);
            this.hasNext = hasNext;
            this.first = first;
            this.nextPage = nextPage;
            this.firstPage = firstPage;
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public boolean isFirst() {
            return first;
        }

        @Override
        public CompletionStage<Page<T>> next() {
            return nextPage;
        }

        @Override
        public CompletionStage<Page<T>> first() {
            return firstPage;
        }
    }
}
