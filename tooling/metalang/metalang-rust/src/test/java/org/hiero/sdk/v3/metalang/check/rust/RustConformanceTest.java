package org.hiero.sdk.v3.metalang.check.rust;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.hiero.sdk.v3.metalang.MetaLang;
import org.hiero.sdk.v3.metalang.TestSpecs;
import org.hiero.sdk.v3.metalang.check.ApiDifference;
import org.hiero.sdk.v3.metalang.generator.GeneratedOutput;
import org.hiero.sdk.v3.metalang.generator.rust.RustGenerator;
import org.hiero.sdk.v3.metalang.generator.rust.RustGeneratorConfig;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RustConformanceTest {

    private static final Path REPOSITORY = Path.of(System.getProperty("spec.root", "../../../spec"))
            .toAbsolutePath().normalize().getParent();

    @TempDir
    Path temp;

    private static boolean available() {
        try {
            return new ProcessBuilder("cargo", "--version").start().waitFor() == 0;
        } catch (final IOException | InterruptedException e) {
            return false;
        }
    }

    @Test
    void shouldCompareDeclarationsAndAllowAdditions() {
        // GIVEN the expected API
        final String expected = """
                D\tx::a::Id\tstruct\t<T>\tcrates/x/src/a/id.rs\t3
                H\tx::a::Id\timplements\tClone
                H\tx::a::Id\timplements\tx::a::Named
                M\tx::a::Id\tfn value\tfn value(&self) -> u64\tcrates/x/src/a/id.rs\t5
                D\tx::a::Named\ttrait\t\tcrates/x/src/a/named.rs\t1
                H\tx::a::Named\textends\tx::a::Base
                M\tx::a::Named\tfn name\tfn name(&self) -> &str\tcrates/x/src/a/named.rs\t2
                D\tx::a::Gone\tenum\t\tcrates/x/src/a/gone.rs\t1
                D\tx::a::make\tfn\tfn make() -> x::a::Id<String>\tcrates/x/src/a/functions.rs\t4
                D\tx::a::LIMIT\tconst\ti32\tcrates/x/src/a/constants.rs\t2
                """;

        // WHEN the project implements the API, with additions
        final String additions = expected + """
                H\tx::a::Id\timplements\tDefault
                M\tx::a::Id\tfn extra\tfn extra(&self)\tcrates/x/src/a/id.rs\t9
                D\tx::a::Extra\tstruct\t\tcrates/x/src/a/extra.rs\t1
                """;

        // THEN
        assertThat(RustConformance.compare(expected, additions)).isEmpty();

        // WHEN the project changes the API
        final String changed = """
                D\tx::a::Id\tstruct\t<U>\tsrc/id.rs\t3
                M\tx::a::Id\tfn value\tfn value(&self) -> i64\tsrc/id.rs\t5
                D\tx::a::Named\tstruct\t\tsrc/named.rs\t1
                D\tx::a::make\tfn\tfn make() -> String\tsrc/lib.rs\t4
                D\tx::a::LIMIT\tstatic\ti32\tsrc/lib.rs\t2
                E\tsrc/broken.rs\t2\texpected `;`
                X\tsomething new
                """;

        // THEN
        assertThat(RustConformance.compare(expected, changed)).extracting(ApiDifference::toString).containsExactly(
                "An enum x::a::Gone is missing (expected in crates/x/src/a/gone.rs)",
                "src/broken.rs:2: Cannot parse: expected `;`",
                "src/id.rs:3: Struct x::a::Id has different type parameters: expected '<T>', found '<U>'",
                "src/id.rs:3: x::a::Id must implement Clone",
                "src/id.rs:3: x::a::Id must implement x::a::Named",
                "src/id.rs:5: Fn value of x::a::Id has a different declaration: expected 'fn value(&self) -> u64', "
                        + "found 'fn value(&self) -> i64'",
                "src/lib.rs:2: x::a::LIMIT must be a constant, found a static",
                "src/lib.rs:4: Function x::a::make has a different declaration: expected 'fn make() -> "
                        + "x::a::Id<String>', found 'fn make() -> String'",
                "src/named.rs:1: Fn name of x::a::Named is missing",
                "src/named.rs:1: x::a::Named must be a trait, found a struct",
                "src/named.rs:1: x::a::Named must extend x::a::Base");
    }

    @Test
    void shouldAcceptTheGeneratedCodeAndAnImplementationButNotAnOutdatedProject() throws Exception {
        assumeTrue(available(), "cargo is needed");
        // GIVEN the generated code of a spec
        final RustGenerator generator = new RustGenerator();
        final String schema = """
                namespace a
                abstraction Named { @@immutable name: string }
                Client extends Named {
                    @@immutable timeout: int32
                    @@async string fetch(id: int64)
                }
                enum Mode { ON }
                @@static Client connect(name: string)
                """;
        final LinkedModel model = LinkedModel.of(new MetaLang().validate(Map.of("f/a.md",
                TestSpecs.markdown(schema))).model());
        GeneratedOutput.write(temp, generator.generate(model), RustGenerator.MARKER);
        final Path client = temp.resolve("crates/f/src/a/client.rs");

        // THEN as generated
        final RustConformance.Result result = RustConformance.check(model, generator, temp, null);
        assertThat(result.differences()).isEmpty();
        assertThat(result.crates()).isEqualTo(1);

        // WHEN implemented, with other imports
        Files.writeString(client, Files.readString(client, StandardCharsets.UTF_8)
                .replace("todo!(\"Client.fetch\")", "id.to_string()")
                .replace("use crate::a::Named;", "use super::*;"), StandardCharsets.UTF_8);

        // THEN
        assertThat(RustConformance.check(model, generator, temp, null).differences()).isEmpty();

        // WHEN the spec changes
        final LinkedModel changed = LinkedModel.of(new MetaLang().validate(Map.of("f/a.md", TestSpecs.markdown(
                schema.replace("fetch(id: int64)", "fetch(id: int64, retries: int32)")))).model());

        // THEN
        assertThat(RustConformance.check(changed, generator, temp, "cargo").differences())
                .extracting(ApiDifference::message).containsExactly(
                        "Fn fetch of hiero_f::a::Client has a different declaration: expected 'async fn fetch(&self, "
                                + "id: i64, retries: i32) -> String', found 'async fn fetch(&self, id: i64) -> String'");

        // WHEN a file cannot be parsed
        Files.writeString(client, "pub struct Client {", StandardCharsets.UTF_8);

        // THEN
        assertThat(RustConformance.check(model, generator, temp, null).differences())
                .extracting(ApiDifference::message).anyMatch(m -> m.startsWith("Cannot parse: "))
                .anyMatch(m -> m.equals("A struct hiero_f::a::Client is missing (expected in "
                        + "crates/f/src/a/client.rs)"));
    }

    @Test
    void shouldReportAMissingProjectAndAMissingCargo() {
        final LinkedModel model = LinkedModel.of(new MetaLang().validate(Map.of("f/a.md",
                TestSpecs.markdown("namespace a\nX { @@immutable x: int32 }\n"))).model());
        assertThatThrownBy(() -> RustConformance.check(model, new RustGenerator(), temp.resolve("missing"), null))
                .isInstanceOf(IOException.class).hasMessageContaining("Project directory not found");
    }

    @Test
    void shouldReportACargoThatIsMissingOrFails() throws Exception {
        // GIVEN no rs-api program built yet (another temporary directory) and a spec
        final LinkedModel model = LinkedModel.of(new MetaLang().validate(Map.of("f/a.md",
                TestSpecs.markdown("namespace a\nX { @@immutable x: int32 }\n"))).model());
        final String tmp = System.getProperty("java.io.tmpdir");
        final Path failing = temp.resolve("cargo-that-fails");
        Files.writeString(failing, "#!/bin/sh\necho broken toolchain\nexit 3\n");
        assumeTrue(failing.toFile().setExecutable(true), "an executable script is needed");
        try {
            System.setProperty("java.io.tmpdir", temp.toString());

            // WHEN / THEN
            assertThatThrownBy(() -> RustConformance.check(model, new RustGenerator(), temp, temp.resolve("no-cargo")
                    .toString())).isInstanceOf(IOException.class).hasMessageContaining("Cargo not found");
            assertThatThrownBy(() -> RustConformance.check(model, new RustGenerator(), temp, failing.toString()))
                    .isInstanceOf(IOException.class).hasMessageContaining("Building the rs-api program failed: "
                            + "broken toolchain");
        } finally {
            System.setProperty("java.io.tmpdir", tmp);
        }
    }

    @Test
    void generatedRustShouldProvideTheApiOfTheSpecs() throws Exception {
        assumeTrue(available(), "cargo is needed");
        final RustConformance.Result result = RustConformance.check(LinkedModel.of(new MetaLang().validate(
                        REPOSITORY.resolve("spec")).model()), new RustGenerator(RustGeneratorConfig.load(
                        REPOSITORY.resolve("sdk-rust/generator.properties"))), REPOSITORY.resolve("generated/rust"),
                null);
        // generated/rust is up to date (otherwise: regenerate it, see tooling/metalang/README.md)
        assertThat(result.differences()).isEmpty();
        assertThat(result.declarations()).isGreaterThan(300);
    }
}
