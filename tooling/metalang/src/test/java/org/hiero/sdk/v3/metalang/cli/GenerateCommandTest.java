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

    @TempDir
    Path temp;

    private static Path goldenSpec() throws URISyntaxException {
        return Path.of(GenerateCommandTest.class.getResource("/model-golden/spec").toURI());
    }

    private String err() {
        return err.toString(StandardCharsets.UTF_8);
    }

    @Test
    void shouldWriteTheGeneratedFiles() throws Exception {
        // GIVEN
        final Path output = temp.resolve("out/java");

        // WHEN
        final int exit = cli.run("generate", "--language=java", "--output=" + output, goldenSpec().toString());

        // THEN
        assertThat(exit).isEqualTo(MetaLangCli.EXIT_OK);
        assertThat(out.toString(StandardCharsets.UTF_8)).isEqualTo("6 file(s) written to " + output + "\n");
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
        assertThat(cli.run("generate", "--language=rust", "--output=x", spec)).isEqualTo(MetaLangCli.EXIT_USAGE);
        assertThat(err()).contains("Unsupported language 'rust'");
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
