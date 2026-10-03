package org.hiero.sdk.v3.metalang.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.hiero.sdk.v3.metalang.TestSpecs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests of {@code metalang model}. The golden file {@code model-golden/model.json} is the expected output for
 * {@code model-golden/spec} (test resources of metalang-core); regenerate it from {@code tooling/metalang} with
 * {@code java -jar target/metalang-*-cli.jar model metalang-core/src/test/resources/model-golden/spec} after an
 * intended change.
 */
class ModelCommandTest {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();
    private final MetaLangCli cli = new MetaLangCli(new PrintStream(out, true, StandardCharsets.UTF_8),
            new PrintStream(err, true, StandardCharsets.UTF_8));

    @TempDir
    Path temp;

    private static Path golden(final String name) throws URISyntaxException {
        return org.hiero.sdk.v3.metalang.TestSpecs.shared("model-golden/" + name);
    }

    private String out() {
        return out.toString(StandardCharsets.UTF_8);
    }

    @Test
    void shouldPrintTheGoldenModel() throws Exception {
        // WHEN
        final int exit = cli.run("model", golden("spec").toString());

        // THEN
        assertThat(exit).isEqualTo(MetaLangCli.EXIT_OK);
        assertThat(out()).isEqualTo(Files.readString(golden("model.json"), StandardCharsets.UTF_8));
    }

    @Test
    void shouldFilterByNamespaceIncludingSubNamespaces() throws Exception {
        // WHEN
        cli.run("model", "--namespace=shop.money", golden("spec").toString());

        // THEN
        assertThat(out()).contains("\"name\": \"shop.money.Money\"").doesNotContain("\"name\": \"shop.Product\"")
                .contains("\"functions\": []").contains("\"constants\": []");
        out.reset();

        // WHEN the parent namespace is selected, the sub-namespace is included
        cli.run("model", "--namespace=shop", golden("spec").toString());

        // THEN
        assertThat(out()).contains("\"name\": \"shop.money.Money\"").contains("\"name\": \"shop.Product\"")
                .contains("\"name\": \"shop.MAX_PRODUCTS\"");
    }

    @Test
    void shouldNotTreatNamespacePrefixAsSubNamespace() throws Exception {
        cli.run("model", "--namespace=sho", golden("spec").toString());
        assertThat(out()).contains("\"types\": []");
    }

    @Test
    void shouldPrintASingleTypeWithoutFunctionsAndConstants() throws Exception {
        // WHEN
        final int exit = cli.run("model", "--type=shop.Product", golden("spec").toString());

        // THEN
        assertThat(exit).isEqualTo(MetaLangCli.EXIT_OK);
        assertThat(out()).contains("\"name\": \"shop.Product\"").doesNotContain("\"name\": \"shop.Entity\"")
                .contains("\"functions\": []").contains("\"constants\": []")
                .contains("\"declaredIn\": \"shop.Entity\"");
    }

    @Test
    void shouldRejectUnknownTypeAndInvalidOptions() throws Exception {
        assertThat(cli.run("model", "--type=shop.Nope", golden("spec").toString())).isEqualTo(MetaLangCli.EXIT_USAGE);
        assertThat(err.toString(StandardCharsets.UTF_8)).contains("Unknown type 'shop.Nope'");
        assertThat(cli.run("model", "--verbose", golden("spec").toString())).isEqualTo(MetaLangCli.EXIT_USAGE);
        assertThat(cli.run("model", "--fail-on=sometimes", golden("spec").toString()))
                .isEqualTo(MetaLangCli.EXIT_USAGE);
        assertThat(cli.run("model")).isEqualTo(MetaLangCli.EXIT_USAGE);
        assertThat(cli.run("model", temp.resolve("missing").toString())).isEqualTo(MetaLangCli.EXIT_USAGE);
    }

    @Test
    void shouldPrintTheModelButFailWhenTheSpecsHaveErrors() throws IOException {
        // GIVEN a spec with an unknown type
        Files.writeString(temp.resolve("broken.md"), TestSpecs.markdown("namespace a\nX { @@immutable m: Missing }\n"));

        // WHEN / THEN
        assertThat(cli.run("model", temp.toString())).isEqualTo(MetaLangCli.EXIT_FINDINGS);
        assertThat(out()).contains("\"errors\": 1").contains("\"type\": \"?Missing\"");
        out.reset();
        assertThat(cli.run("model", "--fail-on=never", temp.toString())).isEqualTo(MetaLangCli.EXIT_OK);
        assertThat(out()).contains("\"name\": \"a.X\"");
    }

    @Test
    void shouldBeDeterministicForTheRealSpecs() {
        // GIVEN
        final String specs = Path.of(System.getProperty("spec.root", "../../../spec")).toString();

        // WHEN
        cli.run("model", "--fail-on=never", specs);
        final String first = out();
        out.reset();
        cli.run("model", "--fail-on=never", specs);

        // THEN
        assertThat(out()).isEqualTo(first);
        assertThat(first).startsWith("{\n").endsWith("}\n");
        assertThat(balanced(first)).as("brackets balanced outside of strings").isTrue();
    }

    /** Structural sanity check of the JSON text: brackets are balanced outside of string literals. */
    private static boolean balanced(final String json) {
        int depth = 0;
        boolean inString = false;
        for (int i = 0; i < json.length(); i++) {
            final char c = json.charAt(i);
            if (inString) {
                if (c == '\\') {
                    i++;
                } else if (c == '"') {
                    inString = false;
                }
            } else if (c == '"') {
                inString = true;
            } else if (c == '{' || c == '[') {
                depth++;
            } else if (c == '}' || c == ']') {
                depth--;
                if (depth < 0) {
                    return false;
                }
            }
        }
        return depth == 0 && !inString;
    }

    @Test
    void jsonWriterShouldWriteAllValueKindsStably() {
        // GIVEN
        final Map<String, Object> value = new LinkedHashMap<>();
        value.put("text", "a\"b\nc");
        value.put("number", 42);
        value.put("flag", true);
        value.put("nothing", null);
        value.put("emptyList", List.of());
        value.put("emptyMap", Map.of());
        value.put("list", List.of(1, List.of("x")));

        // WHEN / THEN
        assertThat(Json.write(value)).isEqualTo("""
                {
                  "text": "a\\"b\\nc",
                  "number": 42,
                  "flag": true,
                  "nothing": null,
                  "emptyList": [],
                  "emptyMap": {},
                  "list": [
                    1,
                    [
                      "x"
                    ]
                  ]
                }
                """);
    }

    @Test
    void jsonWriterShouldRejectUnsupportedValues() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> Json.write(new Object()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
