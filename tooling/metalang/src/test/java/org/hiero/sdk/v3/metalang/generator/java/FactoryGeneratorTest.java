package org.hiero.sdk.v3.metalang.generator.java;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.hiero.sdk.v3.metalang.MetaLang;
import org.hiero.sdk.v3.metalang.TestSpecs;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.hiero.sdk.v3.metalang.model.QualifiedName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FactoryGeneratorTest {

    @TempDir
    Path temp;

    private static LinkedModel model(final String schema) {
        return LinkedModel.of(new MetaLang().validate(Map.of("f/a.md", TestSpecs.markdown(schema))).model());
    }

    private static String source(final List<GeneratedFile> files, final String type) {
        return files.stream().filter(f -> f.path().endsWith("/" + type + ".java")).findFirst().orElseThrow()
                .content();
    }

    @Test
    void shouldNameTheClassAfterTheLastNamespaceSegment() {
        assertThat(FactoryGenerator.className("keys")).isEqualTo("KeysFactory");
        assertThat(FactoryGenerator.className("mirrornode.account")).isEqualTo("AccountFactory");
    }

    @Test
    void shouldGenerateStaticMethodsInAFinalClass() throws Exception {
        // GIVEN overloads, generics with a shadowing type variable, varargs, errors and a constant in the namespace
        final List<GeneratedFile> files = new JavaGenerator().generate(model("""
                namespace sub.keys
                abstraction Receipt { }
                Key { @@immutable value: string }

                // Creates a key.
                @@static @@throws(illegal-format, key-error) Key create(value: string)
                @@static Key create(values: string...)
                @@static @@async @@nullable Key load(@@nullable name: string)
                @@static $$Receipt receipt<$$Receipt extends Receipt>(type: type<$$Receipt>)
                constant DEFAULT: string = "x"
                """));

        // THEN
        assertThat(source(files, "KeysFactory")).contains("""
                /// Factory methods of the package `org.hiero.sub.keys`.
                public final class KeysFactory {

                    /// Creates a key.
                    ///
                    /// @throws IllegalArgumentException if an illegal format error occurs
                    /// @throws KeyException if a key error occurs
                    public static Key create(final String value) {
                        throw new UnsupportedOperationException("Not implemented yet: KeysFactory.create");
                    }
                """)
                .contains("    public static Key create(final String... values) {")
                .contains("    public static CompletionStage<@Nullable Key> load(final @Nullable String name) {")
                .contains("    public static <ReceiptT extends Receipt> ReceiptT receipt("
                        + "final Class<? extends ReceiptT> type) {")
                .contains("    private KeysFactory() {\n        throw new UnsupportedOperationException(");
        assertThat(source(files, "KeysConstants")).contains("public static final String DEFAULT = \"x\";");
        final GeneratedJava.Compilation compilation = GeneratedJava.compile(files, temp);
        assertThat(compilation.diagnostics()).isEmpty();
        final Class<?> factory = compilation.classLoader().loadClass("org.hiero.sub.keys.KeysFactory");
        assertThat(Modifier.isFinal(factory.getModifiers())).isTrue();
        final Method create = factory.getMethod("create", String.class);
        assertThat(Modifier.isStatic(create.getModifiers())).isTrue();
        assertThat(org.assertj.core.api.Assertions.catchThrowable(() -> create.invoke(null, "a")))
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldDeferFunctionsThatCannotBeGenerated() {
        // GIVEN a function returning a type that is not generated, one with a function type, and a namespace whose
        // factory class name is taken
        final Map<QualifiedName, String> deferred = new JavaGenerator().deferredTypes(model("""
                namespace a
                Callback { @@immutable run: Unknown }
                @@static Callback callback()
                @@static void each(cb: Unknown)
                @@static int32 one()
                """));
        assertThat(deferred)
                .containsEntry(new QualifiedName("a", "callback()"), "refers to a.Callback (record, not generated yet)")
                .containsEntry(new QualifiedName("a", "each(?Unknown)"),
                        "Type '?Unknown' has no Java mapping yet")
                .doesNotContainKey(new QualifiedName("a", "one()"));
        assertThat(new JavaGenerator().deferredTypes(model("""
                namespace a
                AFactory { }
                @@static int32 one()
                """))).containsExactly(Map.entry(new QualifiedName("a", "one()"),
                "the factory class a.AFactory clashes with a type of the same name"));
    }
}
