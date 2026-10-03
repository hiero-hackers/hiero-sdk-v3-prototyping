package org.hiero.sdk.v3.metalang;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.hiero.sdk.v3.metalang.parser.SchemaParser;
import org.hiero.sdk.v3.metalang.source.MarkdownSchemaExtractor;
import org.hiero.sdk.v3.metalang.source.MarkdownSchemaExtractor.CodeBlock;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

/**
 * Every meta-language example in {@code guidelines/api-guideline.md} must be accepted by the grammar. Examples are
 * often fragments, so each block is tried as-is, wrapped into a namespace, and wrapped into a type body. Blocks that
 * are not meta-language (pseudo code, prose tables) must be listed in {@link #NOT_META_LANGUAGE} with a reason; the
 * list must not contain stale entries.
 */
class GuidelineExamplesTest {

    private static final Path GUIDELINE = Path.of(System.getProperty("spec.root", "../../spec"))
            .resolve("../guidelines/api-guideline.md");

    /** First line of a code block that is intentionally not meta-language, mapped to the reason. */
    private static final Map<String, String> NOT_META_LANGUAGE = Map.of(
            "// wrong: attached from outside",
            "intentional counter-example: the first half shows the forbidden 'Type.method(...)' syntax",
            "try {",
            "pseudo code of a stream consumer, not meta-language");

    private final SchemaParser parser = new SchemaParser();

    private record Example(int line, String firstLine, String text) {
    }

    private static List<Example> examples() throws IOException {
        final List<Example> result = new ArrayList<>();
        for (final CodeBlock block : new MarkdownSchemaExtractor().codeBlocks(Files.readString(GUIDELINE))) {
            if (!block.info().isEmpty()) {
                continue; // blocks with a language tag (java, rust, ...) are no meta-language
            }
            // the guideline uses a line consisting only of "..." for omitted content
            final String text = block.content().replaceAll("(?m)^\\s*\\.\\.\\.\\s*$", "");
            final String firstLine = text.strip().lines().findFirst().orElse("").strip();
            result.add(new Example(block.firstContentLine(), firstLine, text));
        }
        return result;
    }

    private Optional<String> parsingWrapper(final String text) {
        // "type expressions": every line (without trailing comment) is used as the type of a field
        final String fields = String.join("\n", text.lines()
                .map(l -> l.replaceAll("//.*$", "").strip())
                .filter(l -> !l.isEmpty())
                .map(l -> "f: " + l)
                .toList());
        final Map<String, String> wrappers = Map.of(
                "as-is", text,
                "namespace", "namespace example\n" + text,
                "type body", "namespace example\nWrapper {\n" + text + "\n}\n",
                "type expressions", "namespace example\nWrapper {\n" + fields + "\n}\n");
        final Optional<String> schema = Stream.of("as-is", "namespace", "type body", "type expressions")
                .filter(w -> parser.parse("api-guideline.md", wrappers.get(w)).diagnostics().isEmpty())
                .findFirst();
        // the content of a "## Default Instances" section
        return schema.or(() -> parser.parseInstances(org.hiero.sdk.v3.metalang.source.SchemaSource.ofPlainText(
                "api-guideline.md", text)).diagnostics().isEmpty() ? Optional.of("default instances")
                : Optional.empty());
    }

    @TestFactory
    Stream<DynamicTest> everyMetaLanguageExampleShouldParse() throws IOException {
        return examples().stream()
                .filter(e -> !NOT_META_LANGUAGE.containsKey(e.firstLine()))
                .map(e -> DynamicTest.dynamicTest("api-guideline.md:" + e.line() + " " + e.firstLine(),
                        () -> assertThat(parsingWrapper(e.text())).as(e.text()).isPresent()));
    }

    @Test
    void exceptionListShouldOnlyContainExistingNonParsingBlocks() throws IOException {
        final List<Example> examples = examples();
        for (final Map.Entry<String, String> exception : NOT_META_LANGUAGE.entrySet()) {
            final Optional<Example> example = examples.stream()
                    .filter(e -> e.firstLine().equals(exception.getKey()))
                    .findFirst();
            assertThat(example).as("stale exception: " + exception.getKey()).isPresent();
            assertThat(parsingWrapper(example.get().text())).as("exception now parses: " + exception.getKey())
                    .isEmpty();
        }
    }

    @Test
    void guidelineShouldContainExamples() throws IOException {
        assertThat(examples()).hasSizeGreaterThan(20);
    }
}
