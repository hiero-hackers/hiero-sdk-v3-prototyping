package org.hiero.sdk.v3.metalang.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.hiero.sdk.v3.metalang.TestSpecs;
import org.hiero.sdk.v3.metalang.diagnostic.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MetaLangCliTest {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();
    private final MetaLangCli cli = new MetaLangCli(new PrintStream(out, true, StandardCharsets.UTF_8),
            new PrintStream(err, true, StandardCharsets.UTF_8));

    @TempDir
    Path specs;

    private String out() {
        return out.toString(StandardCharsets.UTF_8);
    }

    private String err() {
        return err.toString(StandardCharsets.UTF_8);
    }

    @BeforeEach
    void writeSpecs() throws IOException {
        Files.createDirectories(specs.resolve("base"));
        // one error (unknown type), one warning (type keyword), one info (mutable field)
        Files.writeString(specs.resolve("base/a.md"), TestSpecs.markdown("""
                namespace a
                type Foo {
                    @@immutable x: Missing
                    y: int32
                }
                """));
        Files.writeString(specs.resolve("notes.md"), "# Notes\n");
    }

    @Nested
    class Validate {

        @Test
        void shouldPrintFindingsWithRelativePathsAndFailOnErrors() {
            // WHEN
            final int exit = cli.run("validate", specs.toString());

            // THEN
            assertThat(exit).isEqualTo(MetaLangCli.EXIT_FINDINGS);
            assertThat(out()).contains("base/a.md:11:6: WARNING [syntax.type-keyword]")
                    .contains("base/a.md:12:20: ERROR [type.unknown] Unknown type 'Missing'")
                    .contains("notes.md:1:1: INFO [doc.no-schema]")
                    .endsWith("1 spec(s): 1 error(s), 1 warning(s), 2 info(s)\n");
        }

        @Test
        void shouldFilterByMinimumSeverity() {
            // WHEN
            cli.run("validate", "--min-severity=error", specs.toString());

            // THEN
            assertThat(out()).contains("ERROR").doesNotContain("WARNING [").doesNotContain("INFO [");
        }

        @Test
        void shouldHonourFailOnThreshold() {
            assertThat(cli.run("validate", "--fail-on=never", specs.toString())).isEqualTo(MetaLangCli.EXIT_OK);
            assertThat(cli.run("validate", "--fail-on=info", specs.resolve("notes.md").toString()))
                    .isEqualTo(MetaLangCli.EXIT_FINDINGS);
            assertThat(cli.run("validate", "--fail-on=warning", specs.resolve("notes.md").toString()))
                    .isEqualTo(MetaLangCli.EXIT_OK);
        }

        @Test
        void shouldPrintSummary() {
            // WHEN
            cli.run("validate", "--summary", "--min-severity=warning", specs.toString());

            // THEN
            assertThat(out()).isEqualTo("""
                         1  WARNING  syntax.type-keyword
                         1  ERROR    type.unknown
                    1 spec(s): 1 error(s), 1 warning(s), 2 info(s)
                    """);
        }

        @Test
        void shouldPrintJson() {
            // WHEN
            cli.run("validate", "--format=json", "--min-severity=error", specs.toString());

            // THEN
            assertThat(out()).isEqualTo("""
                    {
                      "specs": 1,
                      "errors": 1,
                      "warnings": 1,
                      "infos": 2,
                      "diagnostics": [
                        {"file": "base/a.md", "line": 12, "column": 20, "severity": "error", "rule": "type.unknown", \
                    "message": "Unknown type 'Missing'"}
                      ]
                    }
                    """);
        }

        @Test
        void shouldPrintEmptyJsonArray() {
            cli.run("validate", "--format=json", "--format=text", "--format=json", "--min-severity=error",
                    specs.resolve("notes.md").toString());
            assertThat(out()).contains("\"diagnostics\": []");
        }

        @Test
        void shouldBeDeterministic() {
            // WHEN
            cli.run("validate", specs.toString());
            final String first = out();
            out.reset();
            cli.run("validate", specs.toString());

            // THEN
            assertThat(out()).isEqualTo(first);
        }
    }

    @Nested
    class Usage {

        @ParameterizedTest
        @ValueSource(strings = {"--min-severity=fatal", "--fail-on=sometimes", "--verbose"})
        void shouldRejectInvalidOptions(final String option) {
            assertThat(cli.run("validate", option, specs.toString())).isEqualTo(MetaLangCli.EXIT_USAGE);
            assertThat(err()).contains("Usage:");
        }

        @Test
        void shouldRejectMissingOrMultiplePaths() {
            assertThat(cli.run("validate")).isEqualTo(MetaLangCli.EXIT_USAGE);
            assertThat(cli.run("validate", "a", "b")).isEqualTo(MetaLangCli.EXIT_USAGE);
        }

        @Test
        void shouldRejectNonExistingPath() {
            assertThat(cli.run("validate", specs.resolve("missing").toString())).isEqualTo(MetaLangCli.EXIT_USAGE);
            assertThat(err()).contains("Path does not exist");
        }

        @Test
        void shouldRejectUnknownCommandAndEmptyArguments() {
            assertThat(cli.run("frobnicate")).isEqualTo(MetaLangCli.EXIT_USAGE);
            assertThat(cli.run()).isEqualTo(MetaLangCli.EXIT_USAGE);
        }

        @ParameterizedTest
        @ValueSource(strings = {"help", "--help", "-h"})
        void shouldPrintHelp(final String arg) {
            assertThat(cli.run(arg)).isEqualTo(MetaLangCli.EXIT_OK);
            assertThat(out()).startsWith("Usage:");
        }

        @Test
        void shouldListAllRules() {
            assertThat(cli.run("rules")).isEqualTo(MetaLangCli.EXIT_OK);
            assertThat(out().lines()).hasSize(Rule.values().length);
            assertThat(out()).contains("naming.type").contains("api-guideline.md#naming-conventions");
        }
    }

    @Test
    void shouldEscapeJsonStrings() {
        assertThat(JsonReport.quote("a\"b\\c\nd\re\tf\u0001")).isEqualTo("\"a\\\"b\\\\c\\nd\\re\\tf\\u0001\"");
    }
}
