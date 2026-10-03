package org.hiero.sdk.v3.metalang.generator.ts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.hiero.sdk.v3.metalang.MetaLang;
import org.hiero.sdk.v3.metalang.TestSpecs;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TsTestGeneratorTest {

    private static final String SPEC = """
            namespace a
            enum Mode(code: int32) {
                ON(1)
                OFF(0)
                string label()
            }
            abstraction Signer { bytes sign(data: bytes) }
            abstraction Id {
                @@immutable value: string
                @@static Id generate(seed: int32)
            }
            Limits {
                @@immutable small: uint8
                @@immutable @@min(1) count: int64
                @@immutable big: uint64
                @@immutable @@min(0.5) @@max(2.5) ratio: double
                @@immutable @@max(30) delay: duration
                @@immutable @@minLength(2) @@maxLength(5) code: string
                @@immutable @@pattern("^/[^\\s]*$") path: string
                @@immutable @@urlPattern url: string
                @@immutable @@minSize(1) @@maxSize(3) data: bytes
                @@immutable @@minSize(1) tags: list<string>
                @@immutable labels: set<string>
                @@immutable sizes: map<string, int32>
                @@immutable created: zonedDateTime
                @@immutable mode: Mode
                @@immutable signer: Signer
                @@nullable note: string
                @@min(1) @@max(9) quantity: int32
                @@immutable @@default(7) level: int32
                string describe()
                @@async string fetch(key: string)
                @@throws(not-found-error) string find(key: string)
                void use(signer: Signer)
            }
            Holder { @@immutable id: Id }
            @@static Limits parse(@@minLength(1) text: string, @@nullable hint: string)
            """;

    @TempDir
    Path temp;

    private static List<GeneratedFile> generate() {
        return new TsGenerator().generate(LinkedModel.of(new MetaLang().validate(Map.of("f/a.md",
                TestSpecs.markdown(SPEC))).model()));
    }

    private static List<GeneratedFile> mutate(final List<GeneratedFile> files, final String file, final String from,
                                              final String to) {
        final List<GeneratedFile> result = new ArrayList<>();
        for (final GeneratedFile generated : files) {
            if (generated.path().endsWith("/a/" + file)) {
                assertThat(generated.content()).contains(from);
                result.add(new GeneratedFile(generated.path(), generated.content().replace(from, to)));
            } else {
                result.add(generated);
            }
        }
        return result;
    }

    @Test
    void shouldGenerateTheTestsOfTheContract() {
        // WHEN
        final List<GeneratedFile> files = generate();
        final String test = TsGeneratorTest.file(files, "/a/Limits.test.ts");

        // THEN
        assertThat(test).startsWith(TsGenerator.HEADER)
                .contains("\nimport assert from \"node:assert/strict\";\nimport { describe, test } from \"node:test\";\n")
                .contains("describe(\"Limits\", () => {")
                .contains("test(\"creates a Limits and returns the values\", () => {")
                .contains("test(\"creates a Limits with the default values\", () => {")
                .contains("test(\"rejects null for small\", () => {\n        assert.throws(() => new Limits({ "
                        + "...init(), small: null as never }), TypeError);")
                .contains("test(\"small: rejects a value above the maximum (256)\"")
                .contains("test(\"small: rejects a value that is no integer (0.5)\"")
                .contains("test(\"count: rejects a value below the minimum (0)\"", "0n")
                .contains("test(\"big: accepts the maximum (18446744073709551615)\"", "18446744073709551615n")
                .contains("test(\"big: rejects a value above the maximum (18446744073709551616)\"")
                .contains("test(\"ratio: rejects a value below the minimum\"", "0.49999999999999994")
                .contains("Duration.ofMillis(31)")
                .contains("test(\"code: accepts a value with the minimum length\"")
                .contains("test(\"path: rejects a value that does not match the pattern\"")
                .contains("test(\"url: rejects a value that is no URL\"")
                .contains("test(\"data: rejects more elements than the maximum\"")
                .contains("test(\"the constructor copies data; the property returns a copy\"")
                .contains("test(\"the constructor copies tags; the property is frozen\"")
                .contains("test(\"the constructor copies labels; the property returns a copy\"")
                .contains("test(\"the constructor copies sizes; the property returns a copy\"")
                .contains("test(\"the constructor copies created; the property returns a copy\"")
                .contains("test(\"note can be set to null\"")
                .contains("test(\"setting quantity rejects a value above the maximum (10) and keeps the value\"")
                .contains("test(\"describe() can be called\"")
                .contains("test(\"fetch(key) can be called and returns a promise\"")
                .contains("test(\"find(key) can be called (only NotFoundError may occur)\"")
                .contains("test.todo(\"not generated: no valid arguments for `use(a.Signer)`\");")
                .contains("// - no valid arguments for `use(a.Signer)`")
                .contains("return (Object.freeze({}) as unknown as Signer);");
        assertThat(TsGeneratorTest.file(files, "/a/Holder.test.ts")).contains("return Id.generate(1);");
        assertThat(TsGeneratorTest.file(files, "/a/Mode.test.ts"))
                .contains("assertValue(1, Mode.ON.code);")
                .contains("test(\"valueOf returns the constant of every name\"")
                .contains("Mode.ON.label()");
        assertThat(TsGeneratorTest.file(files, "/a/Id.test.ts")).contains("test(\"generate(seed) can be called\"");
        assertThat(TsGeneratorTest.file(files, "/a/functions.test.ts"))
                .contains("test(\"parse rejects null for text\"")
                .contains("test(\"parse: text rejects a value shorter than the minimum length\"")
                .doesNotContain("rejects null for hint");
        assertThat(TsTestGenerator.untested(files)).containsExactly(
                "Limits.test.ts: no valid arguments for `use(a.Signer)`");
    }

    @Test
    void shouldPassForTheGeneratedCodeAndDetectMutations() throws Exception {
        assumeTrue(GeneratedTs.available(), "node and generated/ts/node_modules (npm install) are needed");
        // GIVEN the generated code without range check, copy and setter check
        List<GeneratedFile> files = generate();
        files = mutate(files, "Limits.ts", "small < 0 || small > 255", "small < 0");
        files = mutate(files, "Limits.ts", "this.#tags = Object.freeze([...tags]);", "this.#tags = tags;");
        files = mutate(files, "Limits.ts", "this.#data = data.slice();", "this.#data = data;");

        // WHEN
        final String build = GeneratedTs.build(files, temp, "@hiero");
        final GeneratedTs.TestRun run = GeneratedTs.test(temp);

        // THEN the mutations are found, all other tests pass or fail because of the stubs
        assertThat(build).isEmpty();
        assertThat(run.failures()).hasSize(3)
                .anyMatch(f -> f.startsWith("small: rejects a value above the maximum (256) "))
                .anyMatch(f -> f.startsWith("the constructor copies tags; the property is frozen "))
                .anyMatch(f -> f.startsWith("the constructor copies data; the property returns a copy "));
        assertThat(run.stubs()).contains("describe() can be called", "parse rejects null for text",
                "label() can be called");
        assertThat(run.todo()).isEqualTo(1);
        assertThat(run.passed()).isGreaterThan(40);
    }
}
