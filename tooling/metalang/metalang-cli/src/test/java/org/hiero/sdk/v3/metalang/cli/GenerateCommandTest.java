package org.hiero.sdk.v3.metalang.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.hiero.sdk.v3.metalang.TestSpecs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GenerateCommandTest {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();
    private final MetaLangCli cli = new MetaLangCli(new PrintStream(out, true, StandardCharsets.UTF_8),
            new PrintStream(err, true, StandardCharsets.UTF_8));

    /** The tests of the example specs that cannot be generated. */
    private static final String UNTESTED = "2 test(s) not generated because their values cannot be built "
            + "(--show-untested lists them; see the instance.missing warnings of 'metalang validate')\n";

    @TempDir
    Path temp;

    private static Path goldenSpec() throws URISyntaxException {
        return TestSpecs.shared("model-golden/spec");
    }

    private String err() {
        return err.toString(StandardCharsets.UTF_8);
    }

    /** A spec folder with a type that has no Java mapping yet and a type that refers to it. */
    private Path deferringSpec() throws Exception {
        final Path spec = temp.resolve("spec/f/a.md");
        Files.createDirectories(spec.getParent());
        Files.writeString(spec, TestSpecs.markdown("""
                namespace a
                Callback { @@immutable run: Unknown }
                Uses { @@immutable callback: Callback }
                abstraction Named { @@immutable name: string }
                """));
        return temp.resolve("spec");
    }

    @Test
    void shouldGenerateTypeScript() throws Exception {
        // GIVEN
        final Path config = temp.resolve("generator.properties");
        Files.writeString(config, "ts.scope = @shop\n");

        // WHEN
        final int exit = cli.run("generate", "--language=ts", "--config=" + config, "--output=" + temp.resolve("ts"),
                goldenSpec().toString());

        // THEN
        assertThat(exit).isEqualTo(MetaLangCli.EXIT_OK);
        assertThat(out.toString(StandardCharsets.UTF_8)).startsWith("").contains(" file(s) generated in ");
        assertThat(Files.readString(temp.resolve("ts/packages/shop/package.json"))).contains("\"name\": \"@shop/shop\"");
        assertThat(Files.readString(temp.resolve("ts/tsconfig.base.json"))).contains("\"strict\": true");

        // WHEN the configuration is invalid
        Files.writeString(config, "ts.scope = shop\n");

        // THEN
        assertThat(cli.run("generate", "--language=ts", "--config=" + config, "--output=" + temp.resolve("ts"),
                goldenSpec().toString())).isEqualTo(MetaLangCli.EXIT_FINDINGS);
        assertThat(err()).contains("Cannot generate: ts.scope: 'shop' is no npm scope");
    }

    @Test
    void shouldGenerateRust() throws Exception {
        // GIVEN
        final Path config = temp.resolve("generator.properties");
        Files.writeString(config, "rust.cratePrefix = shop\n");

        // WHEN
        final int exit = cli.run("generate", "--language=rust", "--config=" + config, "--output="
                + temp.resolve("rust"), goldenSpec().toString());

        // THEN
        assertThat(exit).isEqualTo(MetaLangCli.EXIT_OK);
        assertThat(out.toString(StandardCharsets.UTF_8)).contains(" file(s) generated in ");
        assertThat(Files.readString(temp.resolve("rust/crates/shop/Cargo.toml"))).contains("name = \"shop-shop\"");

        // WHEN the configuration is invalid
        Files.writeString(config, "rust.version = one\n");

        // THEN
        assertThat(cli.run("generate", "--language=rust", "--config=" + config, "--output=" + temp.resolve("rust"),
                goldenSpec().toString())).isEqualTo(MetaLangCli.EXIT_FINDINGS);
        assertThat(err()).contains("Cannot generate: rust.version: 'one' is no semantic version");
    }

    @Test
    void shouldListTheUntestedTestsOnRequest() throws Exception {
        // WHEN
        final int exit = cli.run("generate", "--language=java", "--show-untested",
                "--output=" + temp.resolve("out"), goldenSpec().toString());

        // THEN
        assertThat(exit).isEqualTo(MetaLangCli.EXIT_OK);
        assertThat(out.toString(StandardCharsets.UTF_8)).endsWith(
                "2 test(s) not generated because their values cannot be built:\n"
                        + "  ListingTest: no valid value for `entity` of Listing: constructor, attribute and method "
                        + "tests\n"
                        + "  ProductTest: no valid arguments for `sameAs(shop.Entity<$$O>)`\n");
    }

    @Test
    void shouldListTheDeferredTypesOnRequest() throws Exception {
        // WHEN
        final int exit = cli.run("generate", "--language=java", "--fail-on=never", "--show-deferred",
                "--output=" + temp.resolve("out"), deferringSpec().toString());

        // THEN
        assertThat(exit).isEqualTo(MetaLangCli.EXIT_OK);
        assertThat(out.toString(StandardCharsets.UTF_8)).endsWith(
                "2 declaration(s) deferred until the types they refer to are generated:\n"
                        + "  a.Callback: Type '?Unknown' has no Java mapping yet\n"
                        + "  a.Uses: refers to a.Callback (record, not generated yet)\n");
    }

    @Test
    void shouldApplyTheConfiguration() throws Exception {
        // GIVEN a configuration that makes the abstraction an interface
        final Path config = temp.resolve("generator.properties");
        Files.writeString(config, "# comment\njava.interfaces = a.Named\n");

        // WHEN
        final int exit = cli.run("generate", "--language=java", "--fail-on=never", "--config=" + config,
                "--output=" + temp.resolve("out"), deferringSpec().toString());

        // THEN
        assertThat(exit).isEqualTo(MetaLangCli.EXIT_OK);
        assertThat(Files.readString(temp.resolve("out/org.hiero.f/src/main/java/org/hiero/a/Named.java")))
                .contains("public interface Named {");
    }

    @Test
    void shouldRejectInvalidConfigurations() throws Exception {
        // GIVEN an unknown key, a malformed name, an unknown type and a missing file
        final Path config = temp.resolve("generator.properties");
        Files.writeString(config, "java.interface = a.Named\njava.interfaces = Named a.Missing a.Uses\n");

        // WHEN / THEN
        assertThat(cli.run("generate", "--language=java", "--fail-on=never", "--config=" + config, "--output=" + temp.resolve("out"),
                deferringSpec().toString())).isEqualTo(MetaLangCli.EXIT_FINDINGS);
        assertThat(err()).contains("Cannot generate: Unknown key 'java.interface'")
                .contains("Cannot generate: 'Named' in java.interfaces is no qualified type name (namespace.Type)");
        Files.writeString(config, "java.interfaces = a.Missing, a.Uses\n");
        assertThat(cli.run("generate", "--language=java", "--fail-on=never", "--config=" + config, "--output=" + temp.resolve("out"),
                deferringSpec().toString())).isEqualTo(MetaLangCli.EXIT_FINDINGS);
        assertThat(err()).contains("Cannot generate: java.interfaces: unknown type a.Missing")
                .contains("Cannot generate: java.interfaces: a.Uses is no abstraction");
        assertThat(cli.run("generate", "--language=java", "--fail-on=never", "--config=" + temp.resolve("missing.properties"),
                "--output=" + temp.resolve("out"), deferringSpec().toString())).isEqualTo(MetaLangCli.EXIT_FINDINGS);
        assertThat(err()).contains("Cannot read the configuration");
    }

    @Test
    void shouldReportUnchangedAndRemovedFilesOnTheNextRun() throws Exception {
        // GIVEN a first run and a stale generated file of a type that no longer exists
        final Path output = temp.resolve("out/java");
        cli.run("generate", "--language=java", "--output=" + output, goldenSpec().toString());
        final Path stale = output.resolve("org.hiero.shop/src/main/java/org/hiero/shop/Gone.java");
        Files.writeString(stale, "// Generated by metalang from the Hiero SDK V3 specs. Do not edit.\nclass Gone {}\n");
        out.reset();

        // WHEN
        final int exit = cli.run("generate", "--language=java", "--output=" + output, goldenSpec().toString());

        // THEN
        assertThat(exit).isEqualTo(MetaLangCli.EXIT_OK);
        assertThat(out.toString(StandardCharsets.UTF_8)).isEqualTo("48 file(s) generated in " + output
                + " (0 changed, 1 stale removed)\n  removed " + Path.of("org.hiero.shop/src/main/java/org/hiero/shop/Gone.java")
                + "\n" + UNTESTED);
        assertThat(stale).doesNotExist();
    }

    @Test
    void shouldWriteTheGeneratedFiles() throws Exception {
        // GIVEN
        final Path output = temp.resolve("out/java");

        // WHEN
        final int exit = cli.run("generate", "--language=java", "--output=" + output, goldenSpec().toString());

        // THEN
        assertThat(exit).isEqualTo(MetaLangCli.EXIT_OK);
        assertThat(out.toString(StandardCharsets.UTF_8))
                .isEqualTo("48 file(s) generated in " + output + " (48 changed, 0 stale removed)\n" + UNTESTED);
        assertThat(output.resolve("org.hiero.shop/src/main/java/module-info.java")).exists();
        assertThat(Files.readString(output.resolve("org.hiero.shop/src/main/java/org/hiero/shop/package-info.java")))
                .contains("package org.hiero.shop;");
    }

    @Test
    void shouldNotGenerateWhenTheSpecsHaveErrorsUnlessAllowed() throws IOException {
        // GIVEN
        Files.createDirectories(temp.resolve("specs/folder"));
        Files.writeString(temp.resolve("specs/folder/broken.md"),
                TestSpecs.markdown("namespace a\nX { @@immutable m: Missing }\n"));
        final Path output = temp.resolve("out");

        // WHEN / THEN
        assertThat(cli.run("generate", "--language=java", "--output=" + output, temp.resolve("specs").toString()))
                .isEqualTo(MetaLangCli.EXIT_FINDINGS);
        assertThat(err()).contains("Not generating: the specs have 1 finding(s) at or above error");
        assertThat(output).doesNotExist();

        assertThat(cli.run("generate", "--language=java", "--fail-on=never", "--output=" + output,
                temp.resolve("specs").toString())).isEqualTo(MetaLangCli.EXIT_OK);
        assertThat(output.resolve("org.hiero.folder/src/main/java/module-info.java")).exists();
    }

    @Test
    void shouldReportStructuresThatCannotBeGenerated() throws IOException {
        // GIVEN a spec directly in the spec root (no folder, so no Java module)
        Files.writeString(temp.resolve("root.md"), TestSpecs.markdown("namespace a\nX {}\n"));

        // WHEN / THEN
        assertThat(cli.run("generate", "--language=java", "--output=" + temp.resolve("out"), temp.toString()))
                .isEqualTo(MetaLangCli.EXIT_FINDINGS);
        assertThat(err()).contains("Cannot generate: Spec file 'root.md' of namespace 'a' is not inside a folder");
    }

    @Test
    void shouldReportWhenTheOutputCannotBeWritten() throws Exception {
        // GIVEN the output "directory" is a file
        final Path output = Files.writeString(temp.resolve("not-a-directory"), "x");

        // WHEN / THEN
        assertThat(cli.run("generate", "--language=java", "--output=" + output, goldenSpec().toString()))
                .isEqualTo(MetaLangCli.EXIT_FINDINGS);
        assertThat(err()).contains("Cannot write to " + output);
    }

    @Test
    void shouldRejectInvalidUsage() throws Exception {
        final String spec = goldenSpec().toString();
        assertThat(cli.run("generate", "--output=x", spec)).isEqualTo(MetaLangCli.EXIT_USAGE);
        assertThat(err()).contains("Missing --language");
        // go is supported by `generate`; only `check` still rejects it (no Go conformance check yet)
        assertThat(cli.run("generate", "--language=cobol", "--output=x", spec)).isEqualTo(MetaLangCli.EXIT_USAGE);
        assertThat(err()).contains("Unsupported language 'cobol'");
        assertThat(cli.run("generate", "--language=java", spec)).isEqualTo(MetaLangCli.EXIT_USAGE);
        assertThat(err()).contains("Missing --output");
        assertThat(cli.run("generate", "--language=java", "--output=x")).isEqualTo(MetaLangCli.EXIT_USAGE);
        assertThat(cli.run("generate", "--language=java", "--output=x", temp.resolve("missing").toString()))
                .isEqualTo(MetaLangCli.EXIT_USAGE);
        assertThat(cli.run("generate", "--language=java", "--output=x", "--fail-on=often", spec))
                .isEqualTo(MetaLangCli.EXIT_USAGE);
        assertThat(cli.run("generate", "--language=java", "--output=x", "--verbose", spec))
                .isEqualTo(MetaLangCli.EXIT_USAGE);
    }
}
