package org.hiero.sdk.v3.metalang.generator.java;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.hiero.sdk.v3.metalang.MetaLang;
import org.hiero.sdk.v3.metalang.TestSpecs;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SupportFilesTest {


    private static final String STREAMING_SPEC = """
            namespace a
            Message { @@immutable text: string }
            abstraction Topic {
                // Subscribes to the messages of the topic.
                @@streaming @@throws(connection-error) Message subscribe()
                @@streaming streamResult<Message> subscribeSafely()
            }
            """;

    @TempDir
    Path temp;

    private static List<GeneratedFile> generate(final String schema) {
        return new JavaGenerator().generate(LinkedModel.of(new MetaLang().validate(Map.of("f/a.md",
                TestSpecs.markdown(schema))).model()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"annotation/ThreadSafe", "common/HieroStream", "common/StreamItem", "common/HieroPublisher",
            "common/HieroSubscription"})
    void theSupportModuleShouldProvideTheTypesTheGeneratorUses(final String name) throws Exception {
        // the packages the generator imports from are the packages of the support module, and it exports them
        final String source = Files.readString(GeneratedJava.SUPPORT.resolve("org/hiero/sdk/" + name + ".java"),
                StandardCharsets.UTF_8);
        assertThat(source).containsAnyOf("package " + SupportFiles.STREAMING_PACKAGE + ";",
                "package " + ThreadSafeGenerator.PACKAGE + ";");
        assertThat(Files.readString(GeneratedJava.SUPPORT.resolve("module-info.java"), StandardCharsets.UTF_8))
                .contains("module " + SupportFiles.MODULE + " {")
                .contains("    exports " + SupportFiles.STREAMING_PACKAGE + ";")
                .contains("    exports " + ThreadSafeGenerator.PACKAGE + ";");
    }

    @Test
    void shouldMapStreamingMethodsToHieroStreamAndStreamResultToStreamItem() throws Exception {
        // WHEN
        final List<GeneratedFile> files = generate(STREAMING_SPEC);

        // THEN
        final String topic = files.stream().filter(f -> f.path().endsWith("/Topic.java")).findFirst().orElseThrow()
                .content();
        assertThat(topic).contains("import org.hiero.sdk.common.HieroStream;")
                .contains("""
                            /// Subscribes to the messages of the topic.
                            ///
                            /// The stream ends with `ConnectionException` if it fails.
                            HieroStream<Message> subscribe();
                        """)
                .contains("    HieroStream<StreamItem<Message>> subscribeSafely();");
        assertThat(files).noneMatch(f -> f.path().contains("/org/hiero/sdk/"));
        assertThat(files.stream().filter(f -> f.path().endsWith("module-info.java")).findFirst().orElseThrow()
                .content()).contains("    requires transitive org.hiero.sdk.support;\n")
                .doesNotContain("exports org.hiero.sdk");
        assertThat(generate("namespace a\nX { @@immutable x: int32 }\n"))
                .noneMatch(f -> f.content().contains("org.hiero.sdk.support"));
    }

    @Test
    void shouldDeliverOnDemandWithoutPollingAndCapTheDemand() throws Throwable {
        // GIVEN the compiled support files and a pull stream of five items
        final GeneratedJava.Compilation compilation = GeneratedJava.compile(generate(STREAMING_SPEC), temp);
        assertThat(compilation.diagnostics()).isEmpty();
        final ClassLoader loader = compilation.classLoader();
        final Class<?> streamType = loader.loadClass("org.hiero.sdk.common.HieroStream");
        final AtomicBoolean closed = new AtomicBoolean();
        final Object stream = Proxy.newProxyInstance(loader, new Class<?>[] {streamType}, (proxy, method, args) ->
                switch (method.getName()) {
                    case "iterator" -> List.of(1, 2, 3, 4, 5).iterator();
                    case "close" -> {
                        closed.set(true);
                        yield null;
                    }
                    default -> throw new UnsupportedOperationException(method.getName());
                });
        @SuppressWarnings("unchecked")
        final Flow.Publisher<Object> publisher = (Flow.Publisher<Object>) loader
                .loadClass("org.hiero.sdk.common.HieroPublisher").getConstructor(streamType).newInstance(stream);
        final Recorder recorder = new Recorder();

        // WHEN two items are requested
        publisher.subscribe(recorder);
        recorder.subscription.get().request(2);

        // THEN exactly two are delivered, then the publisher waits for demand
        recorder.awaitItems(2);
        Thread.sleep(Duration.ofMillis(100));
        assertThat(recorder.items).containsExactly(1, 2);

        // WHEN the demand overflows: it is capped (unbounded) and the rest is delivered
        recorder.subscription.get().request(Long.MAX_VALUE);
        recorder.subscription.get().request(Long.MAX_VALUE);

        // THEN
        assertThat(recorder.completed.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(recorder.items).containsExactly(1, 2, 3, 4, 5);
        assertThat(recorder.error.get()).isNull();
    }

    @Test
    void shouldCancelAndRejectNonPositiveDemand() throws Throwable {
        // GIVEN an endless pull stream
        final GeneratedJava.Compilation compilation = GeneratedJava.compile(generate(STREAMING_SPEC), temp);
        final ClassLoader loader = compilation.classLoader();
        final Class<?> streamType = loader.loadClass("org.hiero.sdk.common.HieroStream");
        final AtomicBoolean closed = new AtomicBoolean();
        final Object stream = Proxy.newProxyInstance(loader, new Class<?>[] {streamType}, (proxy, method, args) ->
                switch (method.getName()) {
                    case "iterator" -> java.util.stream.Stream.iterate(1, i -> i + 1).iterator();
                    case "close" -> {
                        closed.set(true);
                        yield null;
                    }
                    default -> throw new UnsupportedOperationException(method.getName());
                });
        @SuppressWarnings("unchecked")
        final Flow.Publisher<Object> publisher = (Flow.Publisher<Object>) loader
                .loadClass("org.hiero.sdk.common.HieroPublisher").getConstructor(streamType).newInstance(stream);

        // WHEN the subscriber cancels while the publisher waits for demand
        final Recorder waiting = new Recorder();
        publisher.subscribe(waiting);
        Thread.sleep(Duration.ofMillis(50));
        waiting.subscription.get().cancel();

        // THEN the stream is closed and nothing is signalled
        assertThat(closed.get()).isTrue();
        Thread.sleep(Duration.ofMillis(50));
        assertThat(waiting.items).isEmpty();
        assertThat(waiting.completed.getCount()).isEqualTo(1);
        assertThat(waiting.error.get()).isNull();

        // WHEN a non-positive demand is requested
        final Recorder invalid = new Recorder();
        publisher.subscribe(invalid);
        invalid.subscription.get().request(0);

        // THEN the subscription is cancelled with an IllegalArgumentException
        assertThat(invalid.error.get()).isInstanceOf(IllegalArgumentException.class);
    }

    /** A subscriber that records what it receives. */
    private static final class Recorder implements Flow.Subscriber<Object> {

        private final AtomicReference<Flow.Subscription> subscription = new AtomicReference<>();
        private final List<Object> items = new CopyOnWriteArrayList<>();
        private final AtomicReference<Throwable> error = new AtomicReference<>();
        private final CountDownLatch completed = new CountDownLatch(1);

        @Override
        public void onSubscribe(final Flow.Subscription value) {
            subscription.set(value);
        }

        @Override
        public void onNext(final Object item) {
            items.add(item);
        }

        @Override
        public void onError(final Throwable throwable) {
            error.set(throwable);
        }

        @Override
        public void onComplete() {
            completed.countDown();
        }

        private void awaitItems(final int count) throws InterruptedException {
            final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (items.size() < count && System.nanoTime() < deadline) {
                Thread.sleep(Duration.ofMillis(5));
            }
            assertThat(items).hasSize(count);
        }
    }
}
