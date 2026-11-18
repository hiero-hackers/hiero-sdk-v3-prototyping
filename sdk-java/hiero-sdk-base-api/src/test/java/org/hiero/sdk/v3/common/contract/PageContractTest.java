package org.hiero.sdk.v3.common.contract;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import org.hiero.sdk.v3.common.MirrorNodeException;
import org.hiero.sdk.v3.common.Page;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Timeout(10)
class PageContractTest {

    @Nested
    class State {

        @Test
        void rejectsNullDataImmediatelyWithAUsefulMessage() {
            // GIVEN / WHEN / THEN
            assertThatNullPointerException().isThrownBy(() -> page(null, 0, 0, false, true))
                    .withMessage("data must not be null");
        }

        @Test
        void snapshotsTheOrderedSourceList() {
            // GIVEN
            final List<String> source = new ArrayList<>(List.of("first", "second"));
            final Page<String> page = page(source, 7, 3, true, false);
            // WHEN
            source.set(0, "changed");
            source.clear();
            // THEN
            assertThat(page.data()).containsExactly("first", "second");
        }

        @ParameterizedTest
        @ValueSource(strings = {"add", "set", "clear"})
        void rejectsChangesToTheReturnedCollection(final String operation) {
            // GIVEN
            final Page<String> page = page(List.of("first", "second"), 2, 0, false, true);
            final Runnable mutation = switch (operation) {
                case "add" -> () -> page.data().add("third");
                case "set" -> () -> page.data().set(0, "changed");
                case "clear" -> () -> page.data().clear();
                default -> throw new AssertionError(operation);
            };
            // WHEN / THEN
            assertThatThrownBy(mutation::run).isInstanceOf(UnsupportedOperationException.class);
            assertThat(page.data()).containsExactly("first", "second");
        }

        @ParameterizedTest
        @CsvSource({"0,0", "7,3", "2147483647,2147483647", "-2147483648,-2147483648"})
        void preservesDeclaredStateWithoutAddingNumericConstraints(final int size, final int index) {
            // GIVEN
            final Page<String> page = page(List.of("value"), size, index, false, false);
            // WHEN / THEN
            assertThat(page.size()).isEqualTo(size);
            assertThat(page.pageIndex()).isEqualTo(index);
        }

        @Test
        void acceptsAnEmptyPage() {
            // GIVEN
            final Page<String> page = page(List.of(), 0, 0, false, true);
            // WHEN / THEN
            assertThat(page.data()).isEmpty();
            assertThat(page.size()).isZero();
            assertThat(page.pageIndex()).isZero();
        }
    }

    @Nested
    class Position {

        @ParameterizedTest
        @CsvSource({"true,true", "true,false", "false,true", "false,false"})
        void inspectsPositionWithoutStartingRetrieval(final boolean hasNext, final boolean first) {
            // GIVEN
            final TestPage<String> page = page(List.of("value"), 1, first ? 0 : 1, hasNext, first);
            // WHEN / THEN
            assertThat(page.hasNext()).isEqualTo(hasNext);
            assertThat(page.isFirst()).isEqualTo(first);
            assertThat(page.nextCalls).isZero();
            assertThat(page.firstCalls).isZero();
        }
    }

    @Nested
    class Navigation {

        @Test
        void completesNextWithTheNextPageAsynchronously() {
            // GIVEN
            final Page<String> next = page(List.of("next"), 1, 2, false, false);
            final CompletableFuture<Page<String>> pending = new CompletableFuture<>();
            final TestPage<String> current = new TestPage<>(
                    List.of("current"), 1, 1, true, false, pending, new CompletableFuture<>());
            // WHEN
            final CompletionStage<Page<String>> result = current.next();
            // THEN
            assertThat(result.toCompletableFuture().isDone()).isFalse();
            // WHEN
            pending.complete(next);
            // THEN
            assertThat(result.toCompletableFuture().join()).isSameAs(next);
            assertThat(current.nextCalls).isEqualTo(1);
            assertThat(current.data()).containsExactly("current");
            assertThat(current.pageIndex()).isEqualTo(1);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 5})
        void completesFirstWithTheSameOperationsFirstPage(final int currentIndex) {
            // GIVEN
            final Page<String> first = page(List.of("first"), 1, 0, true, true);
            final CompletableFuture<Page<String>> pending = new CompletableFuture<>();
            final TestPage<String> current = new TestPage<>(
                    List.of("current"), 1, currentIndex, true, currentIndex == 0,
                    new CompletableFuture<>(), pending);
            // WHEN
            final CompletionStage<Page<String>> result = current.first();
            // THEN
            assertThat(result.toCompletableFuture().isDone()).isFalse();
            // WHEN
            pending.complete(first);
            // THEN
            assertThat(result.toCompletableFuture().join()).isSameAs(first);
            assertThat(current.firstCalls).isEqualTo(1);
            assertThat(current.pageIndex()).isEqualTo(currentIndex);
        }

        @ParameterizedTest
        @ValueSource(strings = {"next", "first"})
        void propagatesMirrorNodeFailureThroughTheReturnedStage(final String operation) {
            // GIVEN
            final Throwable cause = new IllegalStateException("provider failure");
            final MirrorNodeException failure = new MirrorNodeException("request failed", cause);
            final CompletableFuture<Page<String>> pending = new CompletableFuture<>();
            final TestPage<String> page = new TestPage<>(List.of(), 0, 0, true, true, pending, pending);
            // WHEN: invoking the operation returns before the failure is supplied
            final CompletionStage<Page<String>> result = operation.equals("next") ? page.next() : page.first();
            pending.completeExceptionally(failure);
            // THEN
            assertThatThrownBy(() -> result.toCompletableFuture().join())
                    .isInstanceOf(CompletionException.class)
                    .hasCause(failure);
            assertThat(failure).hasMessage("request failed").hasCause(cause);
        }

        @ParameterizedTest
        @ValueSource(strings = {"next", "first"})
        void preservesAnAlreadyFailedStage(final String operation) {
            // GIVEN
            final MirrorNodeException failure = new MirrorNodeException("request failed");
            final CompletionStage<Page<String>> failed = CompletableFuture.failedFuture(failure);
            final TestPage<String> page = new TestPage<>(List.of(), 0, 0, true, true, failed, failed);
            // WHEN
            final CompletionStage<Page<String>> result = operation.equals("next") ? page.next() : page.first();
            // THEN
            assertThatThrownBy(() -> result.toCompletableFuture().join())
                    .isInstanceOf(CompletionException.class)
                    .hasCause(failure);
        }
    }

    @Nested
    class ErrorMapping {

        @Test
        void preservesTheFailureMessageAndCause() {
            // GIVEN
            final Throwable cause = new IllegalStateException("provider failure");
            // WHEN
            final MirrorNodeException exception = new MirrorNodeException("request failed", cause);
            // THEN
            assertThat(exception).hasMessage("request failed").hasCause(cause);
        }

        @Test
        void preservesRuntimeExceptionNullableMessageAndCauseSemantics() {
            // GIVEN / WHEN
            final MirrorNodeException messageOnly = new MirrorNodeException(null);
            final MirrorNodeException withCause = new MirrorNodeException(null, null);
            // THEN
            assertThat(messageOnly.getMessage()).isNull();
            assertThat(withCause.getMessage()).isNull();
            assertThat(withCause.getCause()).isNull();
        }
    }

    private static <T> TestPage<T> page(
            final List<T> data, final int size, final int pageIndex,
            final boolean hasNext, final boolean first) {
        return new TestPage<>(data, size, pageIndex, hasNext, first,
                new CompletableFuture<>(), new CompletableFuture<>());
    }

    private static final class TestPage<T> extends Page<T> {

        private final boolean hasNext;
        private final boolean first;
        private final CompletionStage<Page<T>> nextPage;
        private final CompletionStage<Page<T>> firstPage;
        private int nextCalls;
        private int firstCalls;

        private TestPage(
                final List<T> data, final int size, final int pageIndex,
                final boolean hasNext, final boolean first,
                final CompletionStage<Page<T>> nextPage, final CompletionStage<Page<T>> firstPage) {
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
            nextCalls++;
            return nextPage;
        }

        @Override
        public CompletionStage<Page<T>> first() {
            firstCalls++;
            return firstPage;
        }
    }
}
