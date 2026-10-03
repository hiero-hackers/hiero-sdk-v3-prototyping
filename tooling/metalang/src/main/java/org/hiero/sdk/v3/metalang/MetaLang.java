package org.hiero.sdk.v3.metalang;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
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
import org.hiero.sdk.v3.metalang.diagnostic.Diagnostic;
import org.hiero.sdk.v3.metalang.diagnostic.DiagnosticCollector;
import org.hiero.sdk.v3.metalang.diagnostic.Rule;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;
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
        final DiagnosticCollector readErrors = new DiagnosticCollector();
        try {
            final List<Path> files;
            if (Files.isDirectory(root)) {
                try (Stream<Path> walk = Files.walk(root)) {
                    files = walk.filter(p -> p.toString().endsWith(".md")).filter(Files::isRegularFile).toList();
                }
            } else {
                files = List.of(root);
            }
            for (final Path file : files) {
                final String name = Files.isDirectory(root)
                        ? root.relativize(file).toString().replace('\\', '/')
                        : root.getFileName().toString();
                try {
                    documents.put(name, decodeUtf8(Files.readAllBytes(file)));
                } catch (final CharacterCodingException e) {
                    readErrors.report(Rule.DOC_INVALID_ENCODING, "File is not valid UTF-8; it is skipped",
                            new SourceLocation(name, 1, 1));
                }
            }
        } catch (final IOException e) {
            throw new UncheckedIOException("Cannot read specs from " + root, e);
        }
        return validate(documents, readErrors.sorted());
    }

    /**
     * Strict UTF-8 decoding (malformed input is an error, not silently replaced). A leading byte order mark is
     * removed.
     */
    private static String decodeUtf8(final byte[] bytes) throws CharacterCodingException {
        final String text = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes))
                .toString();
        return text.startsWith("\uFEFF") ? text.substring(1) : text;
    }

    /**
     * Validates the given Markdown documents.
     *
     * @param markdownByFile Markdown content keyed by file name
     * @return the report
     */
    public ValidationReport validate(final Map<String, String> markdownByFile) {
        return validate(markdownByFile, List.of());
    }

    private ValidationReport validate(final Map<String, String> markdownByFile, final List<Diagnostic> readErrors) {
        Objects.requireNonNull(markdownByFile, "markdownByFile must not be null");
        final DiagnosticCollector diagnostics = new DiagnosticCollector();
        diagnostics.addAll(readErrors);
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
