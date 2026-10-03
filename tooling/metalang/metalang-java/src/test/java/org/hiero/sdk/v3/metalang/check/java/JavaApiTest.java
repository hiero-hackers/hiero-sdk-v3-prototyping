package org.hiero.sdk.v3.metalang.check.java;

import org.hiero.sdk.v3.metalang.check.ApiDifference;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import org.hiero.sdk.v3.metalang.check.java.JavaApi.ApiMember;
import org.hiero.sdk.v3.metalang.check.java.JavaApi.ApiModule;
import org.hiero.sdk.v3.metalang.check.java.JavaApi.ApiType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JavaApiTest {

    @TempDir
    Path temp;

    private static JavaApi api(final String... sources) {
        final Map<String, String> files = new java.util.TreeMap<>();
        for (int i = 0; i < sources.length; i++) {
            files.put("F" + i + ".java", sources[i]);
        }
        return JavaApi.of(files);
    }

    private static String signature(final ApiType type, final String key) {
        final ApiMember member = type.members().get(key);
        assertThat(member).as(key + " in " + type.members().keySet()).isNotNull();
        return member.signature();
    }

    @Test
    void shouldResolveTypeNamesThroughImportsPackageJavaLangNestedTypesAndTypeVariables() {
        // WHEN
        final JavaApi api = api("""
                package p;

                import java.util.*;
                import java.util.function.Function;
                import org.jspecify.annotations.Nullable;
                import q.Other;

                public interface Api<T extends Comparable<T>> {
                    record Entry(String key) { }
                    <R> Map<String, R> map(Function<? super T, ? extends R> mapper, Other other, Entry entry);
                    @Nullable String find(List<@Nullable Local> values, String @Nullable [] names, int... counts);
                    Api.Entry first();
                }
                """, "package p;\nclass Local { }\n");

        // THEN
        final ApiType type = api.types().get("p.Api");
        assertThat(type.kind()).isEqualTo("interface");
        assertThat(type.typeParameters()).isEqualTo("<T extends java.lang.Comparable<T>> ");
        assertThat(signature(type, "map(java.util.function.Function, q.Other, p.Api.Entry)")).isEqualTo(
                "public <R> java.util.Map<java.lang.String, R> map(java.util.function.Function<? super T, ? extends R>"
                        + ", q.Other, p.Api.Entry)");
        assertThat(signature(type, "find(java.util.List, java.lang.String[], int[])")).isEqualTo(
                "public @org.jspecify.annotations.Nullable java.lang.String find(java.util.List<"
                        + "@org.jspecify.annotations.Nullable p.Local>, java.lang.String @org.jspecify.annotations"
                        + ".Nullable [], int[])");
        assertThat(signature(type, "first()")).isEqualTo("public p.Api.Entry first()");
        // package-private types are part of the API of the folder as well (they may be referenced)
        assertThat(api.types()).containsOnlyKeys("p.Api", "p.Api.Entry", "p.Local");
        assertThat(api.problems()).isEmpty();
    }

    @Test
    void shouldIgnoreImplementationDetails() {
        // WHEN abstract, default and implemented methods, private members and package-private fields
        final JavaApi api = api("""
                package p;

                public abstract class Base implements Runnable {
                    public static final int LIMIT = 10;
                    protected int counter;
                    int hidden;
                    private String secret;
                    public abstract void run();
                    protected synchronized void hook() { counter++; }
                    private void helper() { }
                    @Override
                    @SuppressWarnings("unused")
                    public String toString() { return "x"; }
                    private static final class Hidden { }
                }
                """, """
                package p;

                public interface Service {
                    int SIZE = 3;
                    default void start() { }
                    static Service create() { return null; }
                    private void helper() { }
                    class Holder { }
                    enum Mode { ON }
                }
                """);

        // THEN
        final ApiType base = api.types().get("p.Base");
        assertThat(base.modifiers()).containsExactly("public", "abstract");
        assertThat(base.interfaces()).containsExactly("java.lang.Runnable");
        assertThat(base.members()).containsOnlyKeys("LIMIT", "counter", "run()", "hook()", "toString()");
        assertThat(signature(base, "LIMIT")).isEqualTo("public static final int LIMIT = 10");
        assertThat(signature(base, "run()")).isEqualTo("public void run()");
        assertThat(signature(base, "hook()")).isEqualTo("protected void hook()");
        assertThat(base.members().get("toString()").annotations()).isEmpty();
        final ApiType service = api.types().get("p.Service");
        assertThat(service.modifiers()).containsExactly("public");
        assertThat(service.members()).containsOnlyKeys("SIZE", "start()", "create()");
        assertThat(signature(service, "SIZE")).isEqualTo("public static final int SIZE = 3");
        assertThat(signature(service, "start()")).isEqualTo("public void start()");
        assertThat(signature(service, "create()")).isEqualTo("public static p.Service create()");
        assertThat(api.types().get("p.Service.Holder").modifiers()).containsExactly("public", "static");
        assertThat(api.types().get("p.Service.Mode").modifiers()).containsExactly("public");
        assertThat(api.types()).doesNotContainKey("p.Base.Hidden");
    }

    @Test
    void shouldReadRecordsEnumsAndSealedTypes() {
        // WHEN
        final JavaApi api = api("""
                package p;

                import org.jspecify.annotations.Nullable;

                public sealed interface Shape permits Circle, Square { }
                """, """
                package p;

                import org.jspecify.annotations.Nullable;

                public record Circle(double radius, @Nullable String name) implements Shape {
                    public Circle {
                        if (radius < 0) throw new IllegalArgumentException();
                    }
                    public Circle(double radius) { this(radius, null); }
                    public static final Circle UNIT = new Circle(1);
                }
                """, """
                package p;

                public final class Square implements Shape {
                    public Square(final double side) { }
                }
                """, """
                package p;

                @Deprecated
                public enum Color {
                    RED("r"), GREEN("g") { @Override public String code() { return "G"; } };
                    private final String code;
                    Color(final String code) { this.code = code; }
                    public String code() { return code; }
                }
                """);

        // THEN
        assertThat(api.types().get("p.Shape").modifiers()).containsExactly("public", "sealed");
        assertThat(api.types().get("p.Shape").permits()).containsExactly("p.Circle", "p.Square");
        final ApiType circle = api.types().get("p.Circle");
        assertThat(circle.kind()).isEqualTo("record");
        assertThat(circle.modifiers()).containsExactly("public");
        assertThat(circle.recordComponents()).containsExactly("double radius",
                "@org.jspecify.annotations.Nullable java.lang.String name");
        // the compact constructor is implied by the components, the additional one is API
        assertThat(circle.members()).containsOnlyKeys("Circle(double)", "UNIT");
        assertThat(signature(circle, "Circle(double)")).isEqualTo("public Circle(double)");
        assertThat(signature(api.types().get("p.Square"), "Square(double)")).isEqualTo("public Square(double)");
        final ApiType color = api.types().get("p.Color");
        assertThat(color.enumConstants()).containsExactly("GREEN", "RED");
        assertThat(color.annotations()).containsExactly("@java.lang.Deprecated");
        assertThat(color.members()).containsOnlyKeys("code()");
    }

    @Test
    void shouldReadModuleDeclarations() {
        // WHEN
        final JavaApi api = api("""
                import org.jspecify.annotations.NullMarked;

                /// The module.
                @NullMarked
                module org.hiero.base {
                    requires transitive org.jspecify;
                    requires static java.compiler;
                    requires java.net.http;
                    exports org.hiero.ledger;
                    exports org.hiero.keys;
                    uses java.lang.Runnable;
                }
                """);

        // THEN
        final ApiModule module = api.modules().get("org.hiero.base");
        assertThat(module.requires()).containsExactly(Map.entry("java.compiler", "static"),
                Map.entry("java.net.http", ""), Map.entry("org.jspecify", "transitive"));
        assertThat(module.exports()).containsExactly("org.hiero.keys", "org.hiero.ledger");
        assertThat(module.annotations()).containsExactly("@org.jspecify.annotations.NullMarked");
        assertThat(module.line()).isEqualTo(4);
    }

    @Test
    void shouldReportSourcesThatCannotBeParsedAndDuplicateTypes() {
        // WHEN
        final JavaApi api = api("package p;\npublic class A { void broken( }\n", "package p;\npublic class B { }\n",
                "package p;\nclass B { }\n");

        // THEN
        assertThat(api.problems()).extracting(ApiDifference::toString).anySatisfy(p -> assertThat(p)
                .startsWith("F0.java:2: Cannot parse: "))
                .contains("F2.java:2: Type p.B is also declared in F1.java");
    }

    @Test
    void shouldReadAllSourcesOfADirectoryExceptBuildOutput() throws Exception {
        // GIVEN
        Files.createDirectories(temp.resolve("m/src/main/java/p"));
        Files.createDirectories(temp.resolve("m/target/generated-sources/p"));
        Files.writeString(temp.resolve("m/src/main/java/p/A.java"), "package p;\npublic class A { B b() { return null; } }\n");
        Files.writeString(temp.resolve("m/target/generated-sources/p/Broken.java"), "broken {");
        Files.writeString(temp.resolve("m/README.md"), "not java");

        // WHEN the referenced type B is not declared, but known
        final JavaApi api = JavaApi.of(temp, Set.of("p.B"));

        // THEN
        assertThat(api.problems()).isEmpty();
        assertThat(api.types()).containsOnlyKeys("p.A");
        assertThat(api.types().get("p.A").file()).isEqualTo("m/src/main/java/p/A.java");
        assertThat(api.types().get("p.A").line()).isEqualTo(2);
    }
}
