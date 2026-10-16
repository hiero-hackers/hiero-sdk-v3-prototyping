package org.hiero.sdk.v3.metalang.generator.java;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.hiero.sdk.v3.metalang.MetaLang;
import org.hiero.sdk.v3.metalang.TestSpecs;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The builders of records and classes with many or optional constructor parameters: which types get one, how it
 * looks, and that it and its generated tests compile and pass.
 */
class BuilderGeneratorTest {

    private static final String SCHEMA = """
            namespace a

            // A value with optional parts.
            Order {
                @@immutable id: int64
                // the customer
                @@immutable @@nullable customer: string
                @@immutable @@default(1) quantity: int32
                @@immutable @@nullable @@minLength(1) note: string
                @@immutable @@nullable data: bytes
            }

            Pair<$$T> {
                @@immutable @@nullable first: $$T
                @@immutable @@nullable second: $$T
            }

            abstraction Shape {}

            Circle extends Shape {
                @@immutable @@nullable label: string
                @@immutable @@nullable color: string
                mutable: int32
            }

            Point {
                @@immutable x: int32
                @@immutable y: int32
            }

            Wide {
                @@immutable a: int32
                @@immutable b: int32
                @@immutable c: int32
                @@immutable d: int32
                @@immutable e: int32
            }

            Base {
                @@immutable @@nullable a: string
                @@immutable @@nullable b: string
            }

            Derived extends Base {
                @@immutable @@nullable c: string
            }

            Clash {
                @@immutable @@nullable build: string
                @@immutable @@nullable other: string
            }
            """;

    @TempDir
    Path temp;

    private static List<GeneratedFile> generate() {
        return new JavaGenerator(JavaGeneratorConfig.DEFAULT).generate(LinkedModel.of(new MetaLang().validate(
                Map.of("f/a.md", TestSpecs.markdown(SCHEMA))).model()));
    }

    private static String source(final List<GeneratedFile> files, final String type) {
        return files.stream().filter(f -> f.path().endsWith("/" + type + ".java")).findFirst().orElseThrow()
                .content();
    }

    @Test
    void typesWithManyOrOptionalConstructorParametersShouldGetABuilder() {
        final List<GeneratedFile> files = generate();
        assertThat(List.of("Order", "Pair", "Circle", "Wide", "Derived"))
                .allSatisfy(t -> assertThat(source(files, t)).contains("public static final class Builder"));
        // two required values; a superclass (its builder could not be hidden); a clashing name
        assertThat(List.of("Point", "Base", "Clash"))
                .allSatisfy(t -> assertThat(source(files, t)).doesNotContain("Builder"));
    }

    @Test
    void theBuilderShouldSetEveryConstructorValueAndCallTheConstructor() {
        final String order = source(generate(), "Order");
        assertThat(order)
                .contains("    public static Builder builder() {\n        return new Builder();\n    }\n")
                .contains("        private @Nullable Long id;\n")
                .contains("        private int quantity = 1;\n")
                .contains("        /// Sets the `customer`.\n        ///\n        /// the customer\n")
                .contains("        public Builder customer(final @Nullable String customer) {\n"
                        + "            this.customer = customer;\n            return this;\n        }\n")
                .contains("        public Builder id(final long id) {\n")
                .contains("        /// @throws NullPointerException if a required value is not set\n"
                        + "        /// @throws IllegalArgumentException if a value violates its constraints\n")
                .contains("            return new Order(\n"
                        + "                    Objects.requireNonNull(id, \"id must not be null\"),\n"
                        + "                    customer,\n");
        assertThat(source(generate(), "Pair"))
                .contains("    public static <T> Builder<T> builder() {\n        return new Builder<>();\n    }\n")
                .contains("    public static final class Builder<T> {\n")
                .contains("        public Builder<T> first(final @Nullable T first) {\n")
                .contains("        public Pair<T> build() {\n            return new Pair<>(first, second);\n")
                .doesNotContain("@throws");
        // a class: the constructor parameters only (not the mutable attribute with its setter)
        assertThat(source(generate(), "Circle")).contains("return new Circle(label, color, Objects.requireNonNull(")
                .contains("        public Builder mutable(final int mutable) {\n");
    }

    @Test
    void theBuildersAndTheirTestsShouldCompileAndPass() throws Exception {
        final List<GeneratedFile> files = generate();
        final GeneratedJava.Compilation modules = GeneratedJava.compile(files, temp);
        assertThat(modules.diagnostics()).isEmpty();
        final GeneratedJava.Compilation tests = GeneratedJava.compileTests(files, modules, temp);
        assertThat(tests.diagnostics()).isEmpty();
        final GeneratedJava.TestRun run = GeneratedJava.runTests(files, tests);
        assertThat(run.unexpected()).isEmpty();
        assertThat(source(files, "OrderTest"))
                .contains("    void shouldBuildTheValuesOfTheConstructor() {\n")
                .contains("    void shouldBuildWithoutTheOptionalValues() {\n")
                .contains("assertThrows(NullPointerException.class, () -> Order.builder().build());");
        assertThat(source(files, "PairTest")).contains("final Pair<String> subject = Pair.<String>builder()\n")
                .doesNotContain("shouldRejectAMissingRequiredValue");
    }
}
