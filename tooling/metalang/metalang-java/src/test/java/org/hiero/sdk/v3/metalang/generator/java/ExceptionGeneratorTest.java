package org.hiero.sdk.v3.metalang.generator.java;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.hiero.sdk.v3.metalang.MetaLang;
import org.hiero.sdk.v3.metalang.TestSpecs;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.generator.GenerationException;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ExceptionGeneratorTest {

    @TempDir
    Path temp;

    private static List<GeneratedFile> generate(final Map<String, String> schemas) {
        final Map<String, String> documents = new TreeMap<>();
        schemas.forEach((file, schema) -> documents.put(file, TestSpecs.markdown(schema)));
        return new JavaGenerator().generate(LinkedModel.of(new MetaLang().validate(documents).model()));
    }

    private static List<String> exceptionFiles(final List<GeneratedFile> files) {
        return files.stream().map(GeneratedFile::path).filter(p -> p.endsWith("Exception.java")).toList();
    }

    private static String source(final List<GeneratedFile> files, final String type) {
        return files.stream().filter(f -> f.path().endsWith("/" + type + ".java")).findFirst().orElseThrow()
                .content();
    }

    @Nested
    class Names {

        @Test
        void shouldDeriveClassNamesAndWordsFromTheErrorIdentifier() {
            assertThat(JavaExceptions.className("client-closed-error")).isEqualTo("ClientClosedException");
            assertThat(JavaExceptions.className("rate-limit")).isEqualTo("RateLimitException");
            assertThat(JavaExceptions.className("error")).isEqualTo("ErrorException");
            assertThat(JavaExceptions.words("mirror-node-error")).isEqualTo("mirror node");
            assertThat(JavaExceptions.words("rate-limit")).isEqualTo("rate limit");
            assertThat(JavaExceptions.anError("io-error")).isEqualTo("an io error");
            assertThat(JavaExceptions.anError("service-error")).isEqualTo("a service error");
        }

        @Test
        void shouldUseStandardExceptionsWhereTheJdkHasOne() {
            assertThat(JavaExceptions.standard("timeout-error")).hasValueSatisfying(e -> {
                assertThat(e.simpleName()).isEqualTo("TimeoutException");
                assertThat(e.checked()).isTrue();
            });
            assertThat(JavaExceptions.standard("not-found-error")).hasValueSatisfying(e ->
                    assertThat(e.checked()).isFalse());
            assertThat(JavaExceptions.standard("service-error")).isEmpty();
        }
    }

    @Nested
    class Placement {

        @Test
        void shouldPlaceTheClassInTheShortestNamespaceOfTheModuleThatAllUsersRequire() {
            // GIVEN error "shared-error" used in base (b) and in two namespaces of client (c, c.sub) that require base
            final List<GeneratedFile> files = generate(Map.of(
                    "base/b.md", "namespace b\nB { @@throws(shared-error) void run() }\n",
                    "client/c.md", "namespace c\nrequires {B} from b\n"
                            + "C { @@immutable b: B\n @@throws(shared-error, local-error) void run() }\n",
                    "client/sub.md", "namespace c.sub\nS { @@throws(local-error, not-found-error) void run() }\n"));

            // THEN
            assertThat(exceptionFiles(files)).containsExactly(
                    "org.hiero.base/src/main/java/org/hiero/b/SharedException.java",
                    "org.hiero.client/src/main/java/org/hiero/c/LocalException.java");
            assertThat(source(files, "S")).contains("import org.hiero.c.LocalException;")
                    .contains("/// @throws LocalException if a local error occurs\n"
                            + "    /// @throws NoSuchElementException if a not found error occurs\n");
            assertThat(files.stream().filter(f -> f.path().endsWith("org.hiero.base/src/main/java/module-info.java"))
                    .findFirst().orElseThrow().content()).contains("    exports org.hiero.b;\n");
        }

        @Test
        void shouldRejectErrorsWithoutCommonModuleOrWithClashingNames() {
            assertThatThrownBy(() -> generate(Map.of(
                    "one/a.md", "namespace a\nA { @@throws(shared-error) void run() }\n",
                    "two/b.md", "namespace b\nB { @@throws(shared-error) void run() }\n",
                    "three/c.md", "namespace c\nShared {}\nC { @@throws(shared-error) void run() }\n"
                            + "D { @@throws(c-error) void run() }\nCException {}\n")))
                    .isInstanceOfSatisfying(GenerationException.class, e -> assertThat(e.problems()).containsExactly(
                            "The exception class of error 'c-error' clashes with the type c.CException",
                            "Error 'shared-error' is used in the modules [org.hiero.one, org.hiero.three, "
                                    + "org.hiero.two], but none of them is required by all others; its exception "
                                    + "class has no home"));
        }
    }

    @Nested
    class Methods {

        @Test
        void shouldDeclareCheckedExceptionsAndDescribeAsyncFailures() throws Exception {
            // WHEN
            final List<GeneratedFile> files = generate(Map.of("f/a.md", """
                    namespace a
                    abstraction Client {
                        @@throws(io-error, io-error, service-error) void sync()
                        @@async @@throws(connection-error, timeout-error, client-closed-error) void call()
                        @@async @@throws(timeout-error) void once()
                    }
                    """));

            // THEN
            assertThat(source(files, "Client"))
                    .contains("    /// @throws IOException if an io error occurs\n"
                            + "    /// @throws ServiceException if a service error occurs\n"
                            + "    void sync() throws IOException;\n")
                    .contains("    /// The returned stage completes exceptionally with `ConnectionException`, "
                            + "`TimeoutException` or `ClientClosedException` if the operation fails.\n"
                            + "    CompletionStage<Void> call();\n")
                    .contains("with `TimeoutException` if the operation fails.\n")
                    .doesNotContain("import java.util.concurrent.TimeoutException;");
            final GeneratedJava.Compilation compilation = GeneratedJava.compile(files, temp);
            assertThat(compilation.diagnostics()).isEmpty();
        }
    }

    @Nested
    class Behaviour {

        @Test
        void shouldGenerateFinalUncheckedExceptionsWithMessageAndCause() throws Throwable {
            // GIVEN
            final List<GeneratedFile> files = generate(Map.of("f/a.md",
                    "namespace a\nA { @@throws(rate-limit-error) void run() }\n"));
            final GeneratedJava.Compilation compilation = GeneratedJava.compile(files, temp);
            assertThat(compilation.diagnostics()).isEmpty();
            final Class<?> type = compilation.classLoader().loadClass("org.hiero.a.RateLimitException");

            // WHEN
            final Constructor<?> constructor = type.getConstructor(String.class, Throwable.class);
            final IllegalStateException cause = new IllegalStateException("cause");
            final Throwable exception = (Throwable) constructor.newInstance("limit reached", cause);

            // THEN
            assertThat(type.getConstructors()).hasSize(1);
            assertThat(Modifier.isFinal(type.getModifiers())).isTrue();
            assertThat(exception).isInstanceOf(RuntimeException.class)
                    .hasMessage("limit reached").hasCause(cause);
            assertThat((Throwable) constructor.newInstance("no cause", null)).hasNoCause();
            assertThatThrownBy(() -> {
                try {
                    constructor.newInstance(null, cause);
                } catch (final InvocationTargetException e) {
                    throw e.getCause();
                }
            }).isInstanceOf(NullPointerException.class).hasMessage("message must not be null");
            assertThat(source(files, "RateLimitException")).contains("/// Signals a rate limit error.\n");
        }
    }
}
