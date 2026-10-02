package org.hiero.sdk.v3.metalang;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.hiero.sdk.v3.metalang.ast.SchemaFile;
import org.hiero.sdk.v3.metalang.diagnostic.DiagnosticCollector;
import org.hiero.sdk.v3.metalang.parser.ParseResult;
import org.hiero.sdk.v3.metalang.parser.SchemaParser;
import org.hiero.sdk.v3.metalang.semantic.SpecModel;
import org.hiero.sdk.v3.metalang.source.ExtractionResult;
import org.hiero.sdk.v3.metalang.source.MarkdownSchemaExtractor;
import org.hiero.sdk.v3.metalang.validation.Validator;

/**
 * Entry point of the meta-language tooling: runs extraction, parsing, model building and validation.
 */
public final class MetaLang {

    private final MarkdownSchemaExtractor extractor = new MarkdownSchemaExtractor();
    private final SchemaParser parser = new SchemaParser();
    private final Validator validator = new Validator();

    /**
     * Validates all Markdown files below a directory (or a single Markdown file). File names in the
     * report are relative to {@code root} (with {@code /} as separator), so reports do not depend on
     * the working directory.
     *
     * @param root a directory or a single {@code .md} file
     * @return the report
     */
    public ValidationReport validate(final Path root) {
        Objects.requireNonNull(root, "root must not be null");
        final SortedMap<String, String> documents = new TreeMap<>();
        try {
            if (Files.isDirectory(root)) {
                final List<Path> files;
                try (Stream<Path> walk = Files.walk(root)) {
                    files = walk.filter(p -> p.toString().endsWith(".md")).filter(Files::isRegularFile).toList();
                }
                for (final Path file : files) {
                    documents.put(root.relativize(file).toString().replace('\\', '/'),
                            Files.readString(file, StandardCharsets.UTF_8));
                }
            } else {
                documents.put(root.getFileName().toString(), Files.readString(root, StandardCharsets.UTF_8));
            }
        } catch (final IOException e) {
            throw new UncheckedIOException("Cannot read specs from " + root, e);
        }
        return validate(documents);
    }

    /**
     * Validates the given Markdown documents.
     *
     * @param markdownByFile Markdown content keyed by file name
     * @return the report
     */
    public ValidationReport validate(final Map<String, String> markdownByFile) {
        Objects.requireNonNull(markdownByFile, "markdownByFile must not be null");
        final DiagnosticCollector diagnostics = new DiagnosticCollector();
        final List<SchemaFile> schemas = new ArrayList<>();
        int specCount = 0;
        for (final Map.Entry<String, String> entry : new TreeMap<>(markdownByFile).entrySet()) {
            final ExtractionResult extraction = extractor.extract(entry.getKey(), entry.getValue());
            diagnostics.addAll(extraction.diagnostics());
            if (extraction.source() == null) {
                continue;
            }
            specCount++;
            final ParseResult parsed = parser.parse(extraction.source());
            diagnostics.addAll(parsed.diagnostics());
            parsed.ast().ifPresent(schemas::add);
        }
        final SpecModel model = SpecModel.of(schemas);
        diagnostics.addAll(validator.validate(model));
        return new ValidationReport(model, diagnostics.sorted(), specCount);
    }
}
