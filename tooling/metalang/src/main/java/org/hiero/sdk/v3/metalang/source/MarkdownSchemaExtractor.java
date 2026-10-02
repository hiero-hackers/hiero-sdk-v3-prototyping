package org.hiero.sdk.v3.metalang.source;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hiero.sdk.v3.metalang.diagnostic.DiagnosticCollector;
import org.hiero.sdk.v3.metalang.diagnostic.Rule;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;
import org.jspecify.annotations.Nullable;

/**
 * Extracts the meta-language schema from a Markdown spec file and checks the document skeleton.
 *
 * <p>The expected skeleton (see {@code CLAUDE.md} and {@code guidelines/testing-guideline.md}) is
 * {@code # Title → ## Description → ## API Schema → ## Examples → ## Testing → ## Questions & Comments}.
 * The schema is the first fenced code block in the {@code ## API Schema} section. Other level-2
 * headings are allowed anywhere.
 *
 * <p>The extractor is a small line-based scanner on purpose: it only needs to understand ATX headings
 * and fenced code blocks, and keeping it dependency-free makes its behaviour fully predictable.
 */
public final class MarkdownSchemaExtractor {

    private static final List<String> CANONICAL_ORDER = List.of(
            "Description", "API Schema", "Examples", "Testing", "Questions & Comments");

    private static final List<String> REQUIRED_SECTIONS = List.of(
            "Description", "API Schema", "Testing", "Questions & Comments");

    private record Heading(String text, int line) {
    }

    private record CodeBlock(int firstContentLine, String content, @Nullable Heading section) {
    }

    /**
     * Extracts the schema of the given Markdown document.
     *
     * @param file     file name used in diagnostics
     * @param markdown the Markdown content
     * @return the extraction result
     */
    public ExtractionResult extract(final String file, final String markdown) {
        Objects.requireNonNull(file, "file must not be null");
        Objects.requireNonNull(markdown, "markdown must not be null");
        final DiagnosticCollector diagnostics = new DiagnosticCollector();
        // a trailing line break terminates the last line; it does not start an extra empty line
        final String[] lines = markdown.split("\\R");

        final List<Heading> headings = new ArrayList<>();
        final List<CodeBlock> blocks = new ArrayList<>();
        Heading currentSection = null;
        String fence = null;
        int fenceStart = 0;
        StringBuilder blockContent = null;

        for (int i = 0; i < lines.length; i++) {
            final String line = lines[i];
            final int lineNumber = i + 1;
            final String trimmed = line.strip();
            if (fence == null) {
                if (trimmed.startsWith("```") || trimmed.startsWith("~~~")) {
                    fence = trimmed.substring(0, 3);
                    fenceStart = lineNumber;
                    blockContent = new StringBuilder();
                } else if (line.startsWith("## ")) {
                    currentSection = new Heading(line.substring(3).strip(), lineNumber);
                    headings.add(currentSection);
                }
            } else if (trimmed.startsWith(fence) && trimmed.substring(3).isBlank()) {
                blocks.add(new CodeBlock(fenceStart + 1, blockContent.toString(), currentSection));
                fence = null;
                blockContent = null;
            } else {
                blockContent.append(line).append('\n');
            }
        }
        if (fence != null) {
            diagnostics.report(Rule.DOC_UNCLOSED_FENCE, "Fenced code block is never closed",
                    new SourceLocation(file, fenceStart, 1));
            blocks.add(new CodeBlock(fenceStart + 1, blockContent.toString(), currentSection));
        }

        final SchemaSource source = selectSchema(file, headings, blocks, diagnostics);
        if (source != null) {
            checkSkeleton(file, headings, diagnostics);
        }
        return new ExtractionResult(source, diagnostics.sorted());
    }

    private @Nullable SchemaSource selectSchema(final String file, final List<Heading> headings,
                                                final List<CodeBlock> blocks,
                                                final DiagnosticCollector diagnostics) {
        final Heading schemaSection = headings.stream()
                .filter(h -> isSchemaHeading(h.text()))
                .findFirst()
                .orElse(null);
        if (schemaSection != null) {
            final List<CodeBlock> inSection = blocks.stream()
                    .filter(b -> b.section() == schemaSection)
                    .toList();
            if (inSection.isEmpty()) {
                diagnostics.report(Rule.DOC_EMPTY_SCHEMA_SECTION,
                        "Section '## " + schemaSection.text() + "' contains no code block",
                        new SourceLocation(file, schemaSection.line(), 1));
                return null;
            }
            if (inSection.size() > 1) {
                diagnostics.report(Rule.DOC_MULTIPLE_SCHEMA_BLOCKS,
                        "Section '## " + schemaSection.text() + "' contains " + inSection.size()
                                + " code blocks; only the first one is used as schema",
                        new SourceLocation(file, inSection.get(1).firstContentLine() - 1, 1));
            }
            final CodeBlock block = inSection.getFirst();
            return new SchemaSource(file, block.content(), block.firstContentLine() - 1);
        }
        final CodeBlock candidate = blocks.stream()
                .filter(b -> startsWithNamespace(b.content()))
                .findFirst()
                .orElse(null);
        if (candidate == null) {
            diagnostics.report(Rule.DOC_NO_SCHEMA, "File contains no API schema; it is not treated as a spec",
                    new SourceLocation(file, 1, 1));
            return null;
        }
        diagnostics.report(Rule.DOC_SCHEMA_OUTSIDE_SECTION,
                "Schema code block is not placed in a '## API Schema' section",
                new SourceLocation(file, candidate.firstContentLine() - 1, 1));
        return new SchemaSource(file, candidate.content(), candidate.firstContentLine() - 1);
    }

    private void checkSkeleton(final String file, final List<Heading> headings,
                               final DiagnosticCollector diagnostics) {
        final List<String> seen = new ArrayList<>();
        int lastIndex = -1;
        for (final Heading heading : headings) {
            final String canonical = canonicalSection(heading.text());
            if (canonical == null) {
                continue;
            }
            if (!canonical.equals(heading.text()) && !isSchemaHeading(heading.text())) {
                diagnostics.report(Rule.DOC_SECTION_NAME,
                        "Section '## " + heading.text() + "' should be named '## " + canonical + "'",
                        new SourceLocation(file, heading.line(), 1));
            }
            final int index = CANONICAL_ORDER.indexOf(canonical);
            if (index < lastIndex) {
                diagnostics.report(Rule.DOC_SECTION_ORDER,
                        "Section '## " + canonical + "' must come before '## "
                                + CANONICAL_ORDER.get(lastIndex) + "'",
                        new SourceLocation(file, heading.line(), 1));
            }
            lastIndex = Math.max(lastIndex, index);
            seen.add(canonical);
        }
        for (final String required : REQUIRED_SECTIONS) {
            if (!seen.contains(required)) {
                diagnostics.report(Rule.DOC_MISSING_SECTION, "Section '## " + required + "' is missing",
                        new SourceLocation(file, 1, 1));
            }
        }
    }

    private static boolean isSchemaHeading(final String heading) {
        return heading.equals("API Schema") || heading.startsWith("API Schema ");
    }

    private static @Nullable String canonicalSection(final String heading) {
        if (isSchemaHeading(heading)) {
            return "API Schema";
        }
        return switch (heading) {
            case "Description", "Examples", "Testing", "Questions & Comments" -> heading;
            case "Example" -> "Examples";
            default -> null;
        };
    }

    private static boolean startsWithNamespace(final String content) {
        return content.lines()
                .map(String::strip)
                .filter(l -> !l.isEmpty() && !l.startsWith("//"))
                .findFirst()
                .map(l -> l.startsWith("namespace "))
                .orElse(false);
    }
}
