package org.hiero.sdk.v3.metalang.parser;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.hiero.sdk.v3.metalang.ast.SchemaFile;
import org.hiero.sdk.v3.metalang.diagnostic.Diagnostic;
import org.jspecify.annotations.Nullable;

/**
 * Result of parsing a single schema.
 *
 * @param schema      the AST, or {@code null} if the schema has syntax errors
 * @param diagnostics syntax diagnostics
 */
public record ParseResult(@Nullable SchemaFile schema, List<Diagnostic> diagnostics) {

    /**
     * Creates a parse result.
     *
     * @param schema      the AST or {@code null}
     * @param diagnostics the diagnostics
     */
    public ParseResult {
        diagnostics = List.copyOf(Objects.requireNonNull(diagnostics, "diagnostics must not be null"));
    }

    /**
     * Returns the AST if the schema could be parsed without syntax errors.
     *
     * @return the AST
     */
    public Optional<SchemaFile> ast() {
        return Optional.ofNullable(schema);
    }
}
