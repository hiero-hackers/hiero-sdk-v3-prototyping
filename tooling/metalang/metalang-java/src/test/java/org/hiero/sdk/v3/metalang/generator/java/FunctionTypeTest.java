package org.hiero.sdk.v3.metalang.generator.java;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.hiero.sdk.v3.metalang.MetaLang;
import org.hiero.sdk.v3.metalang.TestSpecs;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.hiero.sdk.v3.metalang.model.QualifiedName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FunctionTypeTest {

    @TempDir
    Path temp;

    private static LinkedModel model(final Map<String, String> schemas) {
        final Map<String, String> documents = new TreeMap<>();
        schemas.forEach((file, schema) -> documents.put(file, TestSpecs.markdown(schema)));
        return LinkedModel.of(new MetaLang().validate(documents).model());
    }

    private static String source(final List<GeneratedFile> files, final String type) {
        return files.stream().filter(f -> f.path().endsWith("/" + type + ".java")).findFirst().orElseThrow()
                .content();
    }

    private List<GeneratedFile> compiled(final Map<String, String> schemas) throws IOException {
        final List<GeneratedFile> files = new JavaGenerator().generate(model(schemas));
        final GeneratedJava.Compilation compilation = GeneratedJava.compile(files, temp);
        assertThat(compilation.diagnostics()).isEmpty();
        assertThat(compilation.success()).isTrue();
        return files;
    }

    @Test
    void shouldMapFunctionTypesByShapeToJavaUtilFunctionWithWrapperTypes() throws IOException {
        // WHEN
        final String bus = source(compiled(Map.of("f/a.md", """
                namespace a
                Event { @@immutable id: int32 }
                Bus {
                    void run(action: function<void run()>)
                    void subscribe(callback: function<void onEvent(event: Event)>)
                    void filter(test: function<bool test(event: Event)>)
                    void map(mapper: function<string apply(@@nullable event: Event)>)
                    void supply(supplier: function<int32 get()>)
                    void both(callback: function<void accept(a: Event, b: string)>)
                    void check(callback: function<bool test(a: Event, b: int64)>)
                    void combine(callback: function<int64 apply(a: Event, b: string)>)
                    function<int32 count(event: Event)> counter()
                }
                """)), "Bus");

        // THEN the name of the function does not matter, primitives are boxed
        assertThat(bus).contains("public void run(final Runnable action) {")
                .contains("public void subscribe(final Consumer<Event> callback) {")
                .contains("public void filter(final Predicate<Event> test) {")
                .contains("public void map(final Function<@Nullable Event, String> mapper) {")
                .contains("public void supply(final Supplier<Integer> supplier) {")
                .contains("public void both(final BiConsumer<Event, String> callback) {")
                .contains("public void check(final BiPredicate<Event, Long> callback) {")
                .contains("public void combine(final BiFunction<Event, String, Long> callback) {")
                .contains("public Function<Event, Integer> counter() {")
                .contains("import java.util.function.BiConsumer;");
    }

    @Test
    void shouldGenerateOneFunctionalInterfacePerFunctionTypeWithoutStandardInterface() throws IOException {
        // WHEN two types use the same function type; one function type has varargs
        final List<GeneratedFile> files = compiled(Map.of("f/a.md", """
                namespace a
                One { void on(handler: function<bool onMessage(topic: string, message: bytes, @@nullable at: dateTime)>) }
                Two { void on(handler: function<bool onMessage(topic: string, message: bytes, @@nullable at: dateTime)>) }
                Three { void on(handler: function<void onAll(names: string...)>) }
                """));

        // THEN
        assertThat(files).filteredOn(f -> f.path().endsWith("Function.java")).hasSize(2);
        assertThat(source(files, "OnMessageFunction")).contains("""
                /// A function `onMessage` with the parameters `topic`, `message`, `at`.
                @FunctionalInterface
                public interface OnMessageFunction {

                    boolean onMessage(String topic, byte[] message, @Nullable LocalDateTime at);
                }
                """);
        assertThat(source(files, "OnAllFunction")).contains("    void onAll(String... names);\n");
        assertThat(source(files, "Two")).contains("public void on(final OnMessageFunction handler) {");
    }

    @Test
    void shouldPlaceTheInterfaceWhereAllUsersSeeItAlsoForInheritedMethods() throws IOException {
        // WHEN an abstraction in base uses a function type and a type in client implements it
        final List<GeneratedFile> files = compiled(Map.of(
                "base/b.md", """
                        namespace b
                        abstraction Handler { void on(callback: function<void onTriple(a: int8, b: int8, c: int8)>) }
                        """,
                "client/c.md", """
                        namespace c.sub
                        requires {Handler} from b
                        Impl extends Handler { @@immutable x: int32 }
                        """));

        // THEN there is one interface, in base, and the implementation overrides with the same type
        assertThat(files).extracting(GeneratedFile::path)
                .contains("org.hiero.base/src/main/java/org/hiero/b/OnTripleFunction.java")
                .noneMatch(p -> p.startsWith("org.hiero.client") && p.endsWith("Function.java"));
        assertThat(source(files, "Impl")).contains("import org.hiero.b.OnTripleFunction;")
                .contains("    @Override\n    public void on(final OnTripleFunction callback) {");
    }

    @Test
    void shouldDeferDeclarationsWhoseFunctionTypeHasNoInterface() {
        // GIVEN a function type with type variables, name clashes with a type and between two function types, and
        // a function type used in two modules that do not know each other
        final LinkedModel model = model(Map.of(
                "f/a.md", """
                        namespace a
                        Generic<$$T> { void on(cb: function<void each(a: $$T, b: $$T, c: $$T)>) }
                        OnClashFunction { }
                        Clash { void on(cb: function<void onClash(a: int8, b: int8, c: int8)>) }
                        Twice { void on(cb: function<void onTwice(a: int8, b: int8, c: int8)>)
                            void off(cb: function<void onTwice(x: int8, y: int8, z: int8)>) }
                        Fine { void on(cb: function<void onFine(a: int8, b: int8, c: int8)>) }
                        """,
                "one/b.md", "namespace b\nB { void on(cb: function<void onHome(a: int8, b: int8, c: int8)>) }\n",
                "two/c.md", "namespace c\nC { void on(cb: function<void onHome(a: int8, b: int8, c: int8)>) }\n"));

        // WHEN
        final Map<QualifiedName, String> deferred = new JavaGenerator().deferredTypes(model);

        // THEN
        assertThat(deferred).containsOnlyKeys(new QualifiedName("a", "Generic"), new QualifiedName("a", "Clash"),
                new QualifiedName("a", "Twice"), new QualifiedName("b", "B"), new QualifiedName("c", "C"));
        assertThat(deferred.get(new QualifiedName("a", "Generic")))
                .isEqualTo("Function type 'function<void each(a: $$T, b: $$T, c: $$T)>' needs its own functional "
                        + "interface, which cannot use type variables (only function types with at most two "
                        + "parameters can)");
        assertThat(deferred.get(new QualifiedName("a", "Clash"))).isEqualTo("The functional interface "
                + "a.OnClashFunction of function type 'function<void onClash(a: int8, b: int8, c: int8)>' clashes "
                + "with a type of the same name");
        assertThat(deferred.get(new QualifiedName("a", "Twice"))).contains("clashes with the interface of another "
                + "function type");
        assertThat(deferred.get(new QualifiedName("b", "B"))).isEqualTo("Function type "
                + "'function<void onHome(a: int8, b: int8, c: int8)>' is used in modules of which none is required "
                + "by all others; its functional interface has no home");
    }
}
