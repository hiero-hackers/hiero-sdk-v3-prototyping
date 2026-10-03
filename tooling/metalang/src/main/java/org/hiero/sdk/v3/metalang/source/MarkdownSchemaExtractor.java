package org.hiero.sdk.v3.metalang.source;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
            "Description", "Design Notes", "API Schema", "Examples", "Testing", "Questions & Comments");

    private static final List<String> REQUIRED_SECTIONS = List.of(
            "Description", "API Schema", "Testing", "Questions & Comments");

    private static final Pattern FENCE_OPEN = Pattern.compile("^ {0,3}(`{3,}|~{3,})(.*)$");
    private static final Pattern HEADING_2 = Pattern.compile("^ {0,3}## +(.*?)(?: +#+)? *$");

    private record Heading(String text, int line) {
    }

    /**
     * A fenced code block of a Markdown document.
     *
     * @param openingLine      1-based line of the opening fence
     * @param firstContentLine 1-based line of the first content line
     * @param info             the info string after the opening fence (e.g. {@code java}), may be empty
     * @param content          the content without the fences, each line terminated by {@code \n}
     * @param section          text of the enclosing level-2 heading, or {@code null} before the first one
     * @param sectionLine      1-based line of the enclosing level-2 heading, or 0
     * @param closed           whether the block has a closing fence
     */
    public record CodeBlock(int openingLine, int firstContentLine, String info, String content,
                            @Nullable String section, int sectionLine, boolean closed) {

        /**
         * Creates a code block.
         *
         * @param openingLine      line of the opening fence
         * @param firstContentLine line of the first content line
         * @param info             the info string
         * @param content          the content
         * @param section          the enclosing section or {@code null}
         * @param sectionLine      line of the enclosing section or 0
         * @param closed           whether the block is closed
         */
        public CodeBlock {
            Objects.requireNonNull(info, "info must not be null");
            Objects.requireNonNull(content, "content must not be null");
        }
    }

    private record Scan(List<Heading> headings, List<CodeBlock> blocks, List<String> lines) {
    }

    /**
     * Returns all fenced code blocks of a Markdown document in document order.
     *
     * <p>Fences follow CommonMark: an opening fence is at least three backticks or tildes, indented by at most
     * three spaces; the block is closed by a fence of the same character that is at least as long and has nothing
     * but whitespace after it.
     *
     * @param markdown the Markdown content
     * @return the code blocks
     */
    public List<CodeBlock> codeBlocks(final String markdown) {
        Objects.requireNonNull(markdown, "markdown must not be null");
        return scan(markdown).blocks();
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
        final Scan scan = scan(markdown);
        scan.blocks().stream().filter(b -> !b.closed()).forEach(b -> diagnostics.report(Rule.DOC_UNCLOSED_FENCE,
                "Fenced code block is never closed", new SourceLocation(file, b.openingLine(), 1)));
        final SchemaSource selected = selectSchema(file, scan.headings(), scan.blocks(), diagnostics);
        final Description description = description(scan);
        final SchemaSource source = selected == null ? null : new SchemaSource(selected.file(), selected.text(),
                selected.lineOffset(), description.text(), description.line());
        if (source != null) {
            checkSkeleton(file, scan.headings(), diagnostics);
        }
        return new ExtractionResult(source, diagnostics.sorted());
    }

    private static Scan scan(final String markdown) {
        // a trailing line break terminates the last line; it does not start an extra empty line
        final String[] lines = markdown.split("\\R");
        final List<Heading> headings = new ArrayList<>();
        final List<CodeBlock> blocks = new ArrayList<>();
        Heading currentSection = null;
        String fence = null;
        int fenceStart = 0;
        String info = "";
        StringBuilder blockContent = null;

        for (int i = 0; i < lines.length; i++) {
            final String line = lines[i];
            final int lineNumber = i + 1;
            if (fence == null) {
                final Matcher opening = FENCE_OPEN.matcher(line);
                final Matcher heading = HEADING_2.matcher(line);
                if (opening.matches() && !(opening.group(1).charAt(0) == '`' && opening.group(2).contains("`"))) {
                    fence = opening.group(1);
                    info = opening.group(2).strip();
                    fenceStart = lineNumber;
                    blockContent = new StringBuilder();
                } else if (heading.matches()) {
                    currentSection = new Heading(heading.group(1).strip(), lineNumber);
                    headings.add(currentSection);
                }
            } else if (isClosingFence(line, fence)) {
                blocks.add(block(fenceStart, info, blockContent, currentSection, true));
                fence = null;
                blockContent = null;
            } else {
                blockContent.append(line).append('\n');
            }
        }
        if (fence != null) {
            blocks.add(block(fenceStart, info, blockContent, currentSection, false));
        }
        return new Scan(List.copyOf(headings), List.copyOf(blocks), List.of(lines));
    }

    private record Description(String text, int line) {
    }

    /**
     * Returns the Markdown of the {@code ## Description} section — all lines between its heading and the next
     * level-2 heading, without leading and trailing blank lines — and the line of its first line.
     */
    private static Description description(final Scan scan) {
        for (int i = 0; i < scan.headings().size(); i++) {
            if (scan.headings().get(i).text().equals("Description")) {
                int start = scan.headings().get(i).line(); // 1-based heading line = 0-based first content line
                final int end = i + 1 < scan.headings().size()
                        ? scan.headings().get(i + 1).line() - 1 : scan.lines().size();
                while (start < end && scan.lines().get(start).isBlank()) {
                    start++;
                }
                final String text = String.join("\n", scan.lines().subList(Math.min(start, end), end)).strip();
                return new Description(text, text.isEmpty() ? 0 : start + 1);
            }
        }
        return new Description("", 0);
    }

    private static CodeBlock block(final int openingLine, final String info, final StringBuilder content,
                                   final @Nullable Heading section, final boolean closed) {
        return new CodeBlock(openingLine, openingLine + 1, info, content.toString(),
                section == null ? null : section.text(), section == null ? 0 : section.line(), closed);
    }

    private static boolean isClosingFence(final String line, final String fence) {
        final String withoutIndent = line.replaceFirst("^ {0,3}", "");
        final char fenceChar = fence.charAt(0);
        int length = 0;
        while (length < withoutIndent.length() && withoutIndent.charAt(length) == fenceChar) {
            length++;
        }
        return length >= fence.length() && withoutIndent.substring(length).isBlank();
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
                    .filter(b -> b.sectionLine() == schemaSection.line())
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
            case "Description", "Design Notes", "Examples", "Testing", "Questions & Comments" -> heading;
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
