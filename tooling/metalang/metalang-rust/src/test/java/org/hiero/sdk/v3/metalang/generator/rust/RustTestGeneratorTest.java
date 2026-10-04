package org.hiero.sdk.v3.metalang.generator.rust;

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

class RustTestGeneratorTest {

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
                @@immutable small: int24
                @@immutable @@min(1) count: int64
                @@immutable @@min(0.5) @@max(2.5) ratio: double
                @@immutable @@max(30) delay: duration
                @@immutable @@minLength(2) @@maxLength(5) code: string
                @@immutable @@pattern("^/[^\\s]*$") path: string
                @@immutable @@urlPattern url: string
                @@immutable @@minSize(1) @@maxSize(3) data: bytes
                @@immutable @@minSize(1) tags: list<string>
                @@immutable labels: set<string>
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
        return new RustGenerator().generate(LinkedModel.of(new MetaLang().validate(Map.of("f/a.md",
                TestSpecs.markdown(SPEC))).model()));
    }

    private static List<GeneratedFile> mutate(final List<GeneratedFile> files, final String file, final String from,
                                              final String to) {
        final List<GeneratedFile> result = new ArrayList<>();
        for (final GeneratedFile generated : files) {
            if (generated.path().endsWith("/src/a/" + file)) {
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
        final String test = RustGeneratorTest.file(files, "crates/f/tests/api/a/limits.rs");

        // THEN
        assertThat(test).startsWith(RustGenerator.HEADER)
                .contains("use hiero_f::a::Limits;")
                .contains("/// creates a Limits and returns the values\n#[test]\n"
                        + "fn creates_a_limits_and_returns_the_values() {\n    let subject = create();\n")
                .contains("    assert_eq!(subject.code(), code_value());\n")
                .contains("    assert_eq!(subject.data(), (data_value()).as_slice());\n")
                .contains("    assert_eq!(subject.note(), (note_value()).as_deref());\n")
                .contains("fn small_rejects_a_value_above_the_maximum_8388608() {")
                .contains("fn count_rejects_a_value_below_the_minimum_0() {")
                .contains("fn count_accepts_the_maximum_9223372036854775807() {")
                .contains("fn ratio_rejects_a_value_below_the_minimum() {", "0.49999999999999994")
                .contains("std::time::Duration::from_millis(31)")
                .contains("fn code_accepts_a_value_with_the_minimum_length() {")
                .contains("fn path_rejects_a_value_that_does_not_match_the_pattern() {")
                .contains("fn url_rejects_a_value_that_is_no_url() {")
                .contains("fn data_rejects_more_elements_than_the_maximum() {")
                .contains("fn note_can_be_set_to_none() {")
                .contains("fn setting_quantity_rejects_a_value_above_the_maximum_10_and_keeps_the_value() {")
                .contains("    let before = format!(\"{:?}\", subject.quantity());\n")
                .contains("fn describe_can_be_called() {\n    let _ = create().describe();\n}")
                .contains("fn fetch_key_can_be_called_and_returns_a_future() {\n"
                        + "    let _ = crate::helpers::block_on(create().fetch(key_value()));\n}")
                .contains("fn find_key_can_be_called() {")
                .contains("#[ignore = \"not generated: no valid arguments for `use(a.Signer)`\"]")
                .contains("// - no valid arguments for `use(a.Signer)`")
                .doesNotContain("fn compares_by_value() {") // a trait object has no equality
                .contains("crate::helpers::assert_send_sync::<Limits>();")
                .contains("#[derive(Debug)]\nstruct SignerDouble;\n")
                .contains("impl Signer for SignerDouble {\n    fn sign(&self, data: Vec<u8>) -> Vec<u8> {\n"
                        + "        unimplemented!(\"test double\")");
        assertThat(RustGeneratorTest.file(files, "tests/api/a/holder.rs")).contains("<dyn Id>::generate(1)");
        assertThat(RustGeneratorTest.file(files, "tests/api/a/mode.rs"))
                .contains("assert_eq!(Mode::On.code(), 1);")
                .contains("fn parses_the_name_of_every_constant() {")
                .contains("let _ = Mode::On.label();");
        assertThat(RustGeneratorTest.file(files, "tests/api/a/id.rs")).contains("fn generate_seed_can_be_called() {")
                .contains("crate::helpers::assert_send_sync::<dyn Id>();");
        assertThat(RustGeneratorTest.file(files, "tests/api/a/functions.rs"))
                .contains("let _ = parse(text_value(), hint_value());");
        assertThat(RustGeneratorTest.file(files, "tests/api/a/mod.rs")).contains("mod functions;\nmod holder;\n");
        assertThat(RustGeneratorTest.file(files, "tests/api/helpers.rs")).contains("pub fn block_on<F: Future>");
        assertThat(RustTestGenerator.untested(files)).containsExactly(
                "a/limits.rs: no valid arguments for `use(a.Signer)`");
    }

    @Test
    void shouldPassForTheGeneratedCodeAndDetectMutations() throws Exception {
        assumeTrue(GeneratedRust.available(), "cargo is needed");
        // GIVEN the generated code without range check, length check and setter check
        List<GeneratedFile> files = generate();
        files = mutate(files, "limits.rs", "if !(-8388608..=8388607).contains(&small) {", "if false {");
        files = mutate(files, "limits.rs", "if code.chars().count() > 5 {", "if false {");
        files = mutate(files, "limits.rs", "if quantity > 9 {", "if false {");

        // WHEN
        final String build = GeneratedRust.build(files, temp);
        final GeneratedRust.TestRun run = GeneratedRust.test(temp);

        // THEN the mutations are found, all other tests pass or fail because of the stubs
        assertThat(build).isEmpty();
        assertThat(run.failures()).hasSize(5)
                .anyMatch(f -> f.startsWith("a::limits::small_rejects_a_value_above_the_maximum_8388608 "))
                .anyMatch(f -> f.startsWith("a::limits::small_rejects_a_value_below_the_minimum_8388609 "))
                .anyMatch(f -> f.startsWith("a::limits::code_rejects_a_value_longer_than_the_maximum_length "))
                .anyMatch(f -> f.startsWith("a::limits::quantity_rejects_a_value_above_the_maximum_10 "))
                .anyMatch(f -> f.startsWith("a::limits::setting_quantity_rejects_a_value_above_the_maximum_10_"));
        assertThat(run.stubs()).contains("a::limits::describe_can_be_called", "a::mode::label_can_be_called");
        assertThat(run.ignored()).isEqualTo(1);
        assertThat(run.passed()).isGreaterThan(30);
    }
}
