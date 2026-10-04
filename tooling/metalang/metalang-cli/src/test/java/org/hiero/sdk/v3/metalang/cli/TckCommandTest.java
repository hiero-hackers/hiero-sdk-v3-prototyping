package org.hiero.sdk.v3.metalang.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TckCommandTest {

    private static final Path SPECS = Path.of(System.getProperty("spec.root", "../../../spec"));
    private static final Path BINDINGS = SPECS.resolveSibling("tck/bindings");
    /** The specs of the repository have open findings; the TCK commands work on the model nevertheless. */
    private static final String NEVER = "--fail-on=never";

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

    /** A TCK with one bound method (with an input the binding does not know) and one unbound method. */
    private Path tck() throws Exception {
        final Path docs = temp.resolve("tck");
        Files.createDirectories(docs.resolve("crypto-service"));
        Files.writeString(docs.resolve("crypto-service/AccountDeleteTransaction.md"), """
                ## JSON-RPC API Endpoint Documentation

                ### Method Name

                `deleteAccount`

                ### Input Parameters

                | Parameter Name    | Type   |
                |-------------------|--------|
                | deleteAccountId   | string |
                | transferAccountId | string |
                | unknownParameter  | string |

                ### Output Parameters

                | Parameter Name | Type   |
                |----------------|--------|
                | status         | string |
                """);
        Files.writeString(docs.resolve("crypto-service/Ping.md"), """
                ### `ping`
                """);
        return docs;
    }

    @Test
    void shouldGenerateTheJavaServer() {
        // WHEN
        final int exit = cli.run("tck", "generate", "--language=java", "--bindings=" + BINDINGS,
                "--output=" + temp.resolve("server"), NEVER, SPECS.toString());

        // THEN
        assertThat(err()).isEmpty();
        assertThat(exit).isZero();
        assertThat(out()).contains("file(s) of the TCK contract and server generated in").endsWith("for 5 method(s)\n");
        assertThat(temp.resolve("server/server/src/main/java/org/hiero/tck/server/CryptoServiceBindings.java")).exists();
        assertThat(temp.resolve("server/contract/src/main/java/org/hiero/tck/contract/Converters.java")).exists();
        assertThat(temp.resolve("server/server/pom.xml")).exists();
    }

    @Test
    void shouldGenerateTheTypeScriptServer() {
        // WHEN the API workspace is given explicitly
        final int exit = cli.run("tck", "generate", "--language=ts", "--bindings=" + BINDINGS,
                "--output=" + temp.resolve("generated/ts-tck"), "--api=" + temp.resolve("api"), NEVER,
                SPECS.toString());

        // THEN the packages reference the projects of the API relative to the output
        assertThat(err()).isEmpty();
        assertThat(exit).isZero();
        assertThat(temp.resolve("generated/ts-tck/server/src/CryptoServiceBindings.ts")).exists();
        assertThat(temp.resolve("generated/ts-tck/contract/tsconfig.json")).content()
                .contains("\"extends\": \"../../../api/tsconfig.base.json\"");

        // WHEN the default: the directory ts next to the output
        assertThat(cli.run("tck", "generate", "--language=ts", "--bindings=" + BINDINGS,
                "--output=" + temp.resolve("other/ts-tck"), NEVER, SPECS.toString())).isZero();
        assertThat(temp.resolve("other/ts-tck/contract/tsconfig.json")).content()
                .contains("\"extends\": \"../../ts/tsconfig.base.json\"");
    }

    @Test
    void shouldReportTheCoverage() throws Exception {
        // WHEN
        final int exit = cli.run("tck", "check", "--bindings=" + BINDINGS, "--tck=" + tck(), NEVER,
                SPECS.toString());

        // THEN
        assertThat(exit).isEqualTo(MetaLangCli.EXIT_FINDINGS);
        assertThat(out()).contains("unknownParameter")
                .contains("1 TCK method(s) bound, 1 unbound, ")
                .contains("Unbound: ping");
    }

    @Test
    void shouldRejectInvalidUsage() throws Exception {
        assertThat(cli.run("tck")).isEqualTo(MetaLangCli.EXIT_USAGE);
        assertThat(cli.run("tck", "run")).isEqualTo(MetaLangCli.EXIT_USAGE);
        assertThat(cli.run("tck", "generate", "--language=go", "--bindings=" + BINDINGS, "--output=x",
                SPECS.toString())).isEqualTo(MetaLangCli.EXIT_USAGE);
        assertThat(cli.run("tck", "generate", "--language=java", "--output=x", SPECS.toString()))
                .isEqualTo(MetaLangCli.EXIT_USAGE);
        assertThat(cli.run("tck", "generate", "--language=java", "--bindings=" + BINDINGS, SPECS.toString()))
                .isEqualTo(MetaLangCli.EXIT_USAGE);
        assertThat(cli.run("tck", "check", "--bindings=" + BINDINGS, SPECS.toString()))
                .isEqualTo(MetaLangCli.EXIT_USAGE);
        assertThat(cli.run("tck", "check", "--bindings=" + BINDINGS, "--tck=" + tck(), "--tck-extra"))
                .isEqualTo(MetaLangCli.EXIT_USAGE);
        assertThat(cli.run("tck", "check", "--bindings=" + BINDINGS, "--tck=" + tck()))
                .isEqualTo(MetaLangCli.EXIT_USAGE);
        assertThat(cli.run("tck", "check", "--bindings=" + BINDINGS, "--tck=" + tck(), "missing"))
                .isEqualTo(MetaLangCli.EXIT_USAGE);
        assertThat(cli.run("tck", "check", "--fail-on=fatal")).isEqualTo(MetaLangCli.EXIT_USAGE);
        assertThat(err()).contains("Invalid severity in --fail-on=fatal").contains("Missing 'tck' command").contains("Unknown 'tck' command 'run'")
                .contains("Unsupported language 'go'").contains("Missing --bindings").contains("Missing --output")
                .contains("Missing --tck").contains("Unknown option --tck-extra")
                .contains("Expected exactly one spec directory or file").contains("Path does not exist: missing");
    }

    @Test
    void shouldStopAtSpecFindings() throws Exception {
        // WHEN
        final int exit = cli.run("tck", "check", "--bindings=" + BINDINGS, "--tck=" + tck(), "--fail-on=info",
                SPECS.toString());

        // THEN
        assertThat(exit).isEqualTo(MetaLangCli.EXIT_FINDINGS);
        assertThat(err()).contains("Stopping: the specs have");
    }

    @Test
    void shouldStopAtBindingErrors() throws Exception {
        // GIVEN
        final Path bindings = temp.resolve("bindings");
        Files.createDirectories(bindings);
        Files.writeString(bindings.resolve("broken.md"), """
                ```bindings
                binding foo -> UnknownType {
                }
                ```
                """);

        // WHEN
        final int exit = cli.run("tck", "generate", "--language=java", "--bindings=" + bindings,
                "--output=" + temp.resolve("server"), NEVER, SPECS.toString());

        // THEN
        assertThat(exit).isEqualTo(MetaLangCli.EXIT_FINDINGS);
        assertThat(err()).contains("tck.unknown-type");
    }
}
