package org.hiero.sdk.v3.metalang.check.java;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class JavaApiComparisonTest {

    private static final String MODULE = """
            import org.jspecify.annotations.NullMarked;

            @NullMarked
            module m {
                requires transitive org.jspecify;
                exports p;
            }
            """;

    private static final String CLIENT = """
            package p;

            import java.util.List;
            import org.jspecify.annotations.Nullable;

            public abstract class Client<T> implements AutoCloseable {
                public static final String NAME = "client";
                protected Client(final String id) {
                    throw new UnsupportedOperationException("Not implemented yet: Client");
                }
                public @Nullable String id() {
                    throw new UnsupportedOperationException("Not implemented yet: Client.id");
                }
                @Deprecated
                public abstract List<T> fetch(int limit) throws java.io.IOException;
            }
            """;

    private static final String MODE = """
            package p;

            public enum Mode { ON, OFF }
            """;

    private static final String ENTRY = """
            package p;

            public record Entry(String key, int value) { }
            """;

    private static final String SHAPE = """
            package p;

            public sealed interface Shape permits Circle { }
            """;

    private static final String CIRCLE = """
            package p;

            public final class Circle implements Shape { }
            """;

    private static JavaApi expected() {
        return JavaApi.of(Map.of("m/module-info.java", MODULE, "m/p/Client.java", CLIENT, "m/p/Mode.java", MODE,
                "m/p/Entry.java", ENTRY, "m/p/Shape.java", SHAPE, "m/p/Circle.java", CIRCLE));
    }

    private static List<String> compare(final Map<String, String> changes) {
        final Map<String, String> sources = new java.util.TreeMap<>(Map.of("m/module-info.java", MODULE,
                "m/p/Client.java", CLIENT, "m/p/Mode.java", MODE, "m/p/Entry.java", ENTRY, "m/p/Shape.java", SHAPE,
                "m/p/Circle.java", CIRCLE));
        changes.forEach((path, content) -> {
            if (content == null) {
                sources.remove(path);
            } else {
                sources.put(path, content);
            }
        });
        return JavaApiComparison.compare(expected(), JavaApi.of(sources)).stream().map(ApiDifference::toString)
                .toList();
    }

    @Test
    void shouldAcceptTheExpectedApi() {
        assertThat(compare(Map.of())).isEmpty();
    }

    @Test
    void shouldAcceptImplementationsAndAdditions() {
        // WHEN methods are implemented, members, types, interfaces, constants, exports and files are added and
        // the imports, the formatting and the documentation change
        final java.util.HashMap<String, String> changes = new java.util.HashMap<>();
        changes.put("m/module-info.java", """
                /// The module.
                @org.jspecify.annotations.NullMarked
                module m {
                    requires transitive org.jspecify;
                    requires java.net.http;
                    exports p;
                    exports p.internal to other;
                }
                """);
        changes.put("m/p/Client.java", """
                package p;

                import java.io.*;
                import java.util.*;
                import org.jspecify.annotations.*;

                /// A client.
                public abstract class Client<T> implements AutoCloseable, Comparable<Client<T>> {
                    public static final String NAME = "client";
                    private final String id;
                    protected Client(final String id) { this.id = id; }
                    Client() { this("default"); }
                    public @Nullable String id() { return id; }
                    @Deprecated public List<T> fetch(int limit) throws IOException { return List.of(); }
                    public int compareTo(Client<T> other) { return 0; }
                    protected void hook() { }
                    private void helper() { }
                }
                """);
        changes.put("m/p/Mode.java", "package p;\n\npublic enum Mode { ON, OFF, AUTO; public boolean on() { return this == ON; } }\n");
        changes.put("m/p/Entry.java", "package p;\n\npublic record Entry(String key, int value) {\n"
                + "    public Entry { java.util.Objects.requireNonNull(key); }\n    public Entry(String key) { this(key, 0); }\n}\n");
        changes.put("m/p/Shape.java", "package p;\n\npublic sealed interface Shape permits Circle, Square { }\n");
        changes.put("m/p/Square.java", "package p;\n\npublic final class Square implements Shape { }\n");
        changes.put("m/p/internal/Helper.java", "package p.internal;\n\npublic class Helper { }\n");

        // THEN
        assertThat(compare(changes)).isEmpty();
    }

    @Test
    void shouldReportMissingAndChangedDeclarations() {
        // WHEN
        final java.util.HashMap<String, String> changes = new java.util.HashMap<>();
        changes.put("m/module-info.java", "module m {\n    requires org.jspecify;\n}\n");
        changes.put("m/p/Client.java", """
                package p;

                import java.util.Collection;

                public class Client<T extends Number> extends Object {
                    public static final String NAME = "other";
                    public Client(final String id) { }
                    public String id() { return ""; }
                    public Collection<T> fetch(long limit) { return null; }
                }
                """);
        changes.put("m/p/Mode.java", "package p;\n\npublic enum Mode { ON }\n");
        changes.put("m/p/Entry.java", "package p;\n\npublic record Entry(String key, long value) { }\n");
        changes.put("m/p/Shape.java", "package p;\n\npublic sealed interface Shape permits Other { }\n");
        changes.put("m/p/Circle.java", null);
        changes.put("m/p/Other.java", "package p;\n\npublic final class Other implements Shape { }\n");

        // THEN
        assertThat(compare(changes)).containsExactly(
                "Class p.Circle is missing (expected in m/p/Circle.java)",
                "m/module-info.java:1: Module m must be annotated with @org.jspecify.annotations.NullMarked",
                "m/module-info.java:1: Module m must export p",
                "m/module-info.java:1: Module m must require org.jspecify as 'requires transitive', found 'requires'",
                "m/p/Client.java:5: Class p.Client has different modifiers: expected 'public abstract', found 'public'",
                "m/p/Client.java:5: Class p.Client has different superclass: expected '', found 'java.lang.Object'",
                "m/p/Client.java:5: Class p.Client has different type parameters: expected '<T>', found "
                        + "'<T extends java.lang.Number>'",
                "m/p/Client.java:5: Class p.Client must implement java.lang.AutoCloseable",
                "m/p/Client.java:5: Method p.Client.fetch(int) is missing",
                "m/p/Client.java:6: Field p.Client.NAME has different declaration: expected 'public static final "
                        + "java.lang.String NAME = \"client\"', found 'public static final java.lang.String NAME = "
                        + "\"other\"'",
                "m/p/Client.java:7: Constructor p.Client.Client(java.lang.String) has different declaration: "
                        + "expected 'protected Client(java.lang.String)', found 'public Client(java.lang.String)'",
                "m/p/Client.java:8: Method p.Client.id() has different declaration: expected 'public "
                        + "@org.jspecify.annotations.Nullable java.lang.String id()', found 'public java.lang.String "
                        + "id()'",
                "m/p/Entry.java:3: Record p.Entry has different record components: expected 'java.lang.String key, "
                        + "int value', found 'java.lang.String key, long value'",
                "m/p/Mode.java:3: Enum p.Mode must declare the constant OFF",
                "m/p/Shape.java:3: Interface p.Shape must permit p.Circle");
    }

    @Test
    void shouldReportMissingModulesKindChangesAndMissingAnnotations() {
        // WHEN
        final java.util.HashMap<String, String> changes = new java.util.HashMap<>();
        changes.put("m/module-info.java", null);
        changes.put("m/p/Mode.java", "package p;\n\npublic final class Mode { }\n");
        changes.put("m/p/Client.java", CLIENT.replace("    @Deprecated\n", ""));
        changes.put("m/p/Shape.java", "package p;\n\npublic sealed interface Shape permits Circle {\n");

        // THEN
        assertThat(compare(changes)).hasSize(4).anySatisfy(d -> assertThat(d).startsWith("m/p/Shape.java:3: Cannot parse"))
                .contains("Module m is missing (expected in m/module-info.java)",
                        "m/p/Client.java:14: Method p.Client.fetch(int) must be annotated with @java.lang.Deprecated",
                        "m/p/Mode.java:3: Type p.Mode must be an enum, found a class");
    }
}
