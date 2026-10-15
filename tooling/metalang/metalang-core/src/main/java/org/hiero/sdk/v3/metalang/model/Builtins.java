package org.hiero.sdk.v3.metalang.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.hiero.sdk.v3.metalang.ast.SchemaFile;
import org.hiero.sdk.v3.metalang.parser.ParseResult;
import org.hiero.sdk.v3.metalang.parser.SchemaParser;
import org.hiero.sdk.v3.metalang.semantic.SpecModel;
import org.hiero.sdk.v3.metalang.source.ExtractionResult;
import org.hiero.sdk.v3.metalang.source.MarkdownSchemaExtractor;

/**
 * The built-in functions and assertions of the test language: the namespace {@code testing}, declared in the
 * meta-language in the resource {@code testing.md} (a copy of the schema in {@code guidelines/test-language.md}). The
 * namespace is linked on its own, so it never becomes part of the API that generators produce.
 */
public final class Builtins {

    /** The namespace of the built-in functions. */
    public static final String NAMESPACE = "testing";

    /** The resource with the declarations. */
    public static final String RESOURCE = "/org/hiero/sdk/v3/metalang/testing.md";

    private static final List<FunctionDefinition> FUNCTIONS = load();

    private Builtins() {
    }

    /**
     * Returns the built-in functions.
     *
     * @return the functions of the namespace {@code testing}, in declaration order
     */
    public static List<FunctionDefinition> functions() {
        return FUNCTIONS;
    }

    /**
     * Returns the Markdown of the declarations.
     *
     * @return the content of the resource
     */
    public static String markdown() {
        try (InputStream in = Builtins.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Missing resource " + RESOURCE);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new UncheckedIOException("Cannot read " + RESOURCE, e);
        }
    }

    private static List<FunctionDefinition> load() {
        final ExtractionResult extraction = new MarkdownSchemaExtractor().extract("testing.md", markdown());
        final ParseResult parsed = new SchemaParser().parse(extraction.schema().orElseThrow());
        final SchemaFile file = parsed.ast().orElseThrow(() -> new IllegalStateException(
                "The built-in functions do not parse: " + parsed.diagnostics()));
        return LinkedModel.of(SpecModel.of(List.of(file))).functions();
    }
}
