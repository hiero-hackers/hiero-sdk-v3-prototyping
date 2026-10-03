package org.hiero.sdk.v3.metalang.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.hiero.sdk.v3.metalang.TestSpecs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CheckCommandTest {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();
    private final MetaLangCli cli = new MetaLangCli(new PrintStream(out, true, StandardCharsets.UTF_8),
            new PrintStream(err, true, StandardCharsets.UTF_8));

    @TempDir
    Path temp;

    private String out() {
        return out.toString(StandardCharsets.UTF_8);
    }

    private String err() {
        return err.toString(StandardCharsets.UTF_8);
    }

    private Path spec(final String schema) throws Exception {
        final Path spec = temp.resolve("spec/f/a.md");
        Files.createDirectories(spec.getParent());
        Files.writeString(spec, TestSpecs.markdown(schema));
        return temp.resolve("spec");
    }

    @Test
    void shouldAcceptTheGeneratedProjectAndReportDifferencesAfterASpecChange() throws Exception {
        // GIVEN a generated project
        final Path spec = spec("namespace a\nabstraction Client { void close() }\n");
        final Path project = temp.resolve("project");
        assertThat(cli.run("generate", "--language=java", "--output=" + project, spec.toString()))
                .isEqualTo(MetaLangCli.EXIT_OK);
        out.reset();

        // WHEN
        final int exit = cli.run("check", "--language=java", "--project=" + project, spec.toString());

        // THEN
        assertThat(exit).isEqualTo(MetaLangCli.EXIT_OK);
        assertThat(out()).isEqualTo(project + " provides the API of the specs (1 type(s), 1 module(s))\n");

        // WHEN the spec gets a new method
        out.reset();
        spec("namespace a\nabstraction Client {\n    void close()\n    void open()\n}\n");
        final int changed = cli.run("check", "--language=java", "--project=" + project, spec.toString());

        // THEN
        assertThat(changed).isEqualTo(MetaLangCli.EXIT_FINDINGS);
        assertThat(out()).isEqualTo("org.hiero.f/src/main/java/org/hiero/a/Client.java:5: Method "
                + "org.hiero.a.Client.open() is missing\n"
                + "1 difference(s) between " + project + " and the API of the specs (1 type(s), 1 module(s) "
                + "expected)\n");
    }

    @Test
    void shouldCheckTypeScriptProjects() throws Exception {
        // GIVEN a generated TypeScript project without installed TypeScript
        final Path spec = spec("namespace a\nabstraction Client { void close() }\n");
        final Path project = temp.resolve("ts");
        cli.run("generate", "--language=ts", "--output=" + project, spec.toString());
        out.reset();

        // WHEN / THEN
        assertThat(cli.run("check", "--language=ts", "--project=" + project, spec.toString()))
                .isEqualTo(MetaLangCli.EXIT_FINDINGS);
        assertThat(err()).contains("TypeScript not found in " + project.resolve("node_modules/typescript"));

        // WHEN TypeScript is installed (npm install in generated/ts)
        final Path typescript = Path.of(System.getProperty("spec.root", "../../../spec")).toAbsolutePath().normalize()
                .resolveSibling("generated/ts/node_modules/typescript");
        org.junit.jupiter.api.Assumptions.assumeTrue(Files.isRegularFile(typescript.resolve("lib/typescript.js")));
        final int exit = cli.run("check", "--language=ts", "--project=" + project, "--typescript=" + typescript,
                spec.toString());

        // THEN
        assertThat(exit).isEqualTo(MetaLangCli.EXIT_OK);
        assertThat(out()).isEqualTo(project + " provides the API of the specs (1 declaration(s), 1 package(s))\n");
    }

    @Test
    void shouldApplyTheConfiguration() throws Exception {
        // GIVEN a project generated without configuration: the abstraction with an attribute is a class
        final Path spec = spec("namespace a\nabstraction Named { @@immutable name: string }\n");
        final Path project = temp.resolve("project");
        cli.run("generate", "--language=java", "--output=" + project, spec.toString());
        final Path config = temp.resolve("generator.properties");
        Files.writeString(config, "java.interfaces = a.Named\n");
        out.reset();

        // WHEN checked with a configuration that makes it an interface
        final int exit = cli.run("check", "--language=java", "--config=" + config, "--project=" + project,
                spec.toString());

        // THEN
        assertThat(exit).isEqualTo(MetaLangCli.EXIT_FINDINGS);
        assertThat(out()).contains("Type org.hiero.a.Named must be an interface, found a class");
    }

    @Test
    void shouldNotCheckInvalidSpecsOrConfigurations() throws Exception {
        // GIVEN
        final Path spec = spec("namespace a\nX { @@immutable x: Unknown }\n");
        final Path project = Files.createDirectories(temp.resolve("project"));

        // WHEN / THEN
        assertThat(cli.run("check", "--language=java", "--project=" + project, spec.toString()))
                .isEqualTo(MetaLangCli.EXIT_FINDINGS);
        assertThat(err()).startsWith("Not checking: the specs have ");
        err.reset();
        assertThat(cli.run("check", "--language=java", "--fail-on=never", "--config=" + temp.resolve("missing"),
                "--project=" + project, spec.toString())).isEqualTo(MetaLangCli.EXIT_FINDINGS);
        assertThat(err()).startsWith("Cannot read " + temp.resolve("missing") + " or " + project + ": ");
        err.reset();
        Files.writeString(temp.resolve("bad.properties"), "java.interfaces = a.Missing\n");
        assertThat(cli.run("check", "--language=java", "--fail-on=never", "--config=" + temp.resolve("bad.properties"),
                "--project=" + project, spec.toString())).isEqualTo(MetaLangCli.EXIT_FINDINGS);
        assertThat(err()).startsWith("Cannot generate: ");
    }

    @Test
    void shouldRejectInvalidUsage() throws Exception {
        final Path spec = spec("namespace a\nX { @@immutable x: int32 }\n");
        final String project = "--project=" + Files.createDirectories(temp.resolve("project"));
        assertThat(cli.run("check", project, spec.toString())).isEqualTo(MetaLangCli.EXIT_USAGE);
        assertThat(cli.run("check", "--language=rust", project, spec.toString())).isEqualTo(MetaLangCli.EXIT_USAGE);
        assertThat(cli.run("check", "--language=ts", "--config=" + temp.resolve("none"), project, spec.toString()))
                .isEqualTo(MetaLangCli.EXIT_FINDINGS);
        assertThat(cli.run("check", "--language=java", spec.toString())).isEqualTo(MetaLangCli.EXIT_USAGE);
        assertThat(cli.run("check", "--language=java", "--project=" + temp.resolve("none"), spec.toString()))
                .isEqualTo(MetaLangCli.EXIT_USAGE);
        assertThat(cli.run("check", "--language=java", project)).isEqualTo(MetaLangCli.EXIT_USAGE);
        assertThat(cli.run("check", "--language=java", project, temp.resolve("none").toString()))
                .isEqualTo(MetaLangCli.EXIT_USAGE);
        assertThat(cli.run("check", "--language=java", "--fail-on=sometimes", project, spec.toString()))
                .isEqualTo(MetaLangCli.EXIT_USAGE);
        assertThat(cli.run("check", "--language=java", "--unknown", project, spec.toString()))
                .isEqualTo(MetaLangCli.EXIT_USAGE);
        assertThat(err()).contains("Missing --language", "Unsupported language 'rust'", "Missing --project",
                "Project directory does not exist: ", "Expected exactly one spec directory or file",
                "Path does not exist: ", "Invalid severity in --fail-on=sometimes", "Unknown option --unknown",
                "metalang check --language=java|ts --project=<dir> [options] <spec-dir-or-file>");
    }
}
