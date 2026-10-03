package org.hiero.sdk.v3.metalang.generator.java;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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

class ConstantsGeneratorTest {

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
        assertThat(ConstantsGenerator.className("ledger")).isEqualTo("LedgerConstants");
        assertThat(ConstantsGenerator.className("consensusnode.transactions")).isEqualTo("TransactionsConstants");
    }

    @Test
    void shouldGenerateStaticConstantsIncludingStructLiterals() throws Throwable {
        // GIVEN basic constants, a record and a class built from struct literals (missing nullable and @@default
        // attributes are filled in)
        final List<GeneratedFile> files = new JavaGenerator().generate(model("""
                namespace sub.a
                abstraction Base { @@immutable shard: int64 }
                @@finalType
                Id extends Base { @@immutable num: int64
                    @@immutable @@nullable alias: string
                    @@immutable @@default("x") checksum: string }
                Point { @@immutable x: int32
                    @@immutable @@nullable label: string }

                // the maximum
                constant MAX: int32 = 1_000
                @@deprecated
                constant NAME: string = "a \\"b\\""
                constant TIMEOUT: duration = 1_500
                constant TAGS: list<string> = ["a", "b"]
                constant ZERO: Id = Id{num: 0, shard: 1}
                constant ORIGIN: Point = Point{x: 0}
                """));

        // THEN
        assertThat(source(files, "AConstants")).contains("""
                /// Constants of the package `org.hiero.sub.a`.
                public final class AConstants {

                    /// the maximum
                    public static final int MAX = 1_000;

                    /// @deprecated Retained for compatibility; do not use it in new code.
                    @Deprecated
                    public static final String NAME = "a \\"b\\"";
                """)
                .contains("    public static final Duration TIMEOUT = Duration.ofMillis(1_500L);\n")
                .contains("    public static final List<String> TAGS = List.of(\"a\", \"b\");\n")
                .contains("    public static final Id ZERO = new Id(1L, 0L, null, \"x\");\n")
                .contains("    public static final Point ORIGIN = new Point(0, null);\n")
                .contains("    private AConstants() {\n        throw new UnsupportedOperationException(");
        final GeneratedJava.Compilation compilation = GeneratedJava.compile(files, temp);
        assertThat(compilation.diagnostics()).isEmpty();
        final Class<?> constants = compilation.classLoader().loadClass("org.hiero.sub.a.AConstants");
        assertThat(constants.getField("MAX").getInt(null)).isEqualTo(1000);
        assertThat(constants.getField("ZERO").get(null)).hasToString("Id[shard=1, num=0, alias=null, checksum=x]");
        assertThat(Modifier.isFinal(constants.getModifiers())).isTrue();
        final Constructor<?> constructor = constants.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThat(org.assertj.core.api.Assertions.catchThrowable(constructor::newInstance))
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldDeferConstantsThatCannotBeGenerated() {
        // GIVEN constants of a type that is not generated, of an abstraction, without required value, and a
        // namespace whose constants class would clash with a type
        final LinkedModel model = model("""
                namespace a
                Callback { @@immutable run: Unknown }
                abstraction Shape { @@immutable size: int32 }
                Point { @@immutable x: int32
                    @@immutable y: int32 }
                AConstants { }
                constant CB: Callback = Callback{}
                constant SHAPE: Shape = Shape{size: 1}
                constant HALF: Point = Point{x: 1}
                """);

        // WHEN
        final Map<QualifiedName, String> deferred = new JavaGenerator().deferredTypes(model);
        final List<GeneratedFile> files = new JavaGenerator().generate(model);

        // THEN all three are deferred because the class name is taken
        assertThat(deferred).containsEntry(new QualifiedName("a", "CB"),
                        "the constants class a.AConstants clashes with a type of the same name")
                .containsEntry(new QualifiedName("a", "HALF"),
                        "the constants class a.AConstants clashes with a type of the same name");
        assertThat(files).noneMatch(f -> f.content().contains("public static final"));

        // WHEN the clash is gone
        final LinkedModel other = model("""
                namespace a
                Callback { @@immutable run: Unknown }
                abstraction Shape { @@immutable size: int32 }
                Point { @@immutable x: int32
                    @@immutable y: int32 }
                constant CB: Callback = Callback{}
                constant SHAPE: Shape = Shape{size: 1}
                constant HALF: Point = Point{x: 1}
                constant ONE: int32 = 1
                """);

        // THEN each constant is reported with its own reason; the others are generated
        assertThat(new JavaGenerator().deferredTypes(other))
                .containsEntry(new QualifiedName("a", "CB"), "refers to a.Callback (record, not generated yet)")
                .containsEntry(new QualifiedName("a", "SHAPE"), "Struct literal of the abstraction a.Shape")
                .containsEntry(new QualifiedName("a", "HALF"), "Struct literal Point{x: 1} has no value for 'y'");
        assertThat(source(new JavaGenerator().generate(other), "AConstants"))
                .contains("public static final int ONE = 1;").doesNotContain("HALF");
    }
}
