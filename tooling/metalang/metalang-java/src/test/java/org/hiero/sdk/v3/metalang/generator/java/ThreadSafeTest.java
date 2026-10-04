package org.hiero.sdk.v3.metalang.generator.java;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ThreadSafeTest {

    @TempDir
    Path temp;

    private static List<GeneratedFile> generate(final Map<String, String> schemas, final JavaGeneratorConfig config) {
        final Map<String, String> documents = new TreeMap<>();
        schemas.forEach((file, schema) -> documents.put(file, TestSpecs.markdown(schema)));
        return new JavaGenerator(config).generate(LinkedModel.of(new MetaLang().validate(documents).model()));
    }

    private static String source(final List<GeneratedFile> files, final String type) {
        return files.stream().filter(f -> f.path().endsWith("/" + type + ".java")).findFirst().orElseThrow()
                .content();
    }

    @Test
    void theModuleThatAllUsersRequireShouldRequireTheSupportModule() throws Exception {
        // GIVEN @@threadSafe in base and in client, which requires base
        final List<GeneratedFile> files = generate(Map.of(
                "base/b.md", "namespace b\nabstraction Cache { @@threadSafe int32 size() }\n",
                "client/c.md", "namespace c\nrequires {Cache} from b\n"
                        + "@@threadSafe\nabstraction Session { void close() }\n"
                        + "Local extends Cache { @@immutable x: int32 }\n"), JavaGeneratorConfig.DEFAULT);

        // THEN
        assertThat(files).noneMatch(f -> f.path().endsWith("ThreadSafe.java"));
        assertThat(files).filteredOn(f -> f.content().contains("requires transitive org.hiero.sdk.support;"))
                .extracting(GeneratedFile::path).containsExactly("org.hiero.base/src/main/java/module-info.java");
        assertThat(files).filteredOn(f -> f.content().contains("<artifactId>hiero-sdk-support</artifactId>"))
                .extracting(GeneratedFile::path).containsExactly("org.hiero.base/pom.xml");
        final GeneratedJava.Compilation compilation = GeneratedJava.compile(files, temp);
        assertThat(compilation.diagnostics()).isEmpty();
        final Class<?> annotation = compilation.classLoader().loadClass("org.hiero.sdk.annotation.ThreadSafe");
        assertThat(annotation.getAnnotation(Retention.class).value()).isEqualTo(RetentionPolicy.CLASS);
        // not evaluated at runtime: invisible to reflection
        assertThat(compilation.classLoader().loadClass("org.hiero.c.Session").getAnnotations()).isEmpty();
    }

    @Test
    void shouldNotGenerateTheAnnotationIfNobodyUsesIt() {
        assertThat(generate(Map.of("f/a.md", "namespace a\nX { @@immutable x: int32 }\n"), JavaGeneratorConfig.DEFAULT))
                .noneMatch(f -> f.content().contains("org.hiero.sdk.annotation"))
                .noneMatch(f -> f.content().contains("org.hiero.sdk.support"))
                .noneMatch(f -> f.content().contains("hiero-sdk-support"));
    }

    @Test
    void shouldAnnotateMembersAndTypesAndMakeMutableStateVolatile() throws Exception {
        // WHEN
        final List<GeneratedFile> files = generate(Map.of("f/a.md", """
                namespace a
                abstraction Client {
                    @@immutable name: string
                    @@threadSafe(state) @@nullable status: string
                    @@nullable note: string
                    @@threadSafe(state) void connect()
                    void plain()
                }
                @@threadSafe(all)
                abstraction Session {
                    @@nullable token: string
                    void close()
                }
                @@finalType
                LocalClient extends Client { }
                @@finalType
                LocalSession extends Session { @@immutable id: int32 }
                @@threadSafe
                enum Mode { ON
                    void toggle() }
                """), new JavaGeneratorConfig(java.util.Set.of(new org.hiero.sdk.v3.metalang.model.QualifiedName("a",
                "Session"))));

        // THEN members are annotated with their group; only the thread-safe attribute is volatile
        assertThat(source(files, "Client"))
                .contains("    private final String name;\n    private volatile @Nullable String status;\n"
                        + "    private @Nullable String note;\n")
                .contains("    @ThreadSafe(group = \"state\")\n    public @Nullable String status() {")
                .contains("    @ThreadSafe(group = \"state\")\n    public Client setStatus(")
                .contains("    @ThreadSafe(group = \"state\")\n    public abstract void connect();")
                .contains("    public abstract void plain();")
                .doesNotContain("@ThreadSafe\npublic");
        // a type-level annotation covers the members, which are not annotated again
        assertThat(source(files, "Session")).contains("@ThreadSafe(group = \"all\")\npublic interface Session {")
                .doesNotContain("    @ThreadSafe");
        // an implementation of a thread-safe type is thread-safe as a whole: annotated class, volatile state
        assertThat(source(files, "LocalSession"))
                .contains("@ThreadSafe(group = \"all\")\npublic final class LocalSession implements Session {")
                .contains("    private volatile @Nullable String token;\n")
                .doesNotContain("    @ThreadSafe");
        // implementations of thread-safe members carry the annotation as well, the class itself does not
        assertThat(source(files, "LocalClient"))
                .contains("    @ThreadSafe(group = \"state\")\n    public LocalClient setStatus(")
                .contains("    @ThreadSafe(group = \"state\")\n    @Override\n    public void connect() {")
                .contains("    @Override\n    public void plain() {")
                .doesNotContain("@ThreadSafe\npublic");
        assertThat(source(files, "Mode")).contains("@ThreadSafe\npublic enum Mode {");
        final GeneratedJava.Compilation compilation = GeneratedJava.compile(files, temp);
        assertThat(compilation.diagnostics()).isEmpty();
        final ClassLoader loader = compilation.classLoader();
        assertThat(Modifier.isVolatile(loader.loadClass("org.hiero.a.Client").getDeclaredField("status")
                .getModifiers())).isTrue();
        assertThat(Modifier.isVolatile(loader.loadClass("org.hiero.a.Client").getDeclaredField("note")
                .getModifiers())).isFalse();
    }

    @Test
    void shouldRejectUsesWithoutCommonModule() {
        assertThatThrownBy(() -> generate(Map.of(
                "one/a.md", "namespace a\nabstraction A { @@threadSafe void run() }\n",
                "two/b.md", "namespace b\nabstraction B { @@threadSafe void run() }\n"), JavaGeneratorConfig.DEFAULT))
                .isInstanceOfSatisfying(GenerationException.class, e -> assertThat(e.problems()).containsExactly(
                        "@@threadSafe is used in the modules [org.hiero.one, org.hiero.two], but none of them is "
                                + "required by all others; the @ThreadSafe annotation has no home"));
    }
}
