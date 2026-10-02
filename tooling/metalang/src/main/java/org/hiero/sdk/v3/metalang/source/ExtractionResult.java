package org.hiero.sdk.v3.metalang.source;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.hiero.sdk.v3.metalang.diagnostic.Diagnostic;
import org.jspecify.annotations.Nullable;

/**
 * Result of extracting the schema from a Markdown spec file.
 *
 * @param source      the extracted schema, or {@code null} if the file contains none
 * @param diagnostics findings about the document structure
 */
public record ExtractionResult(@Nullable SchemaSource source, List<Diagnostic> diagnostics) {

    /**
     * Creates a new extraction result.
     *
     * @param source      the extracted schema or {@code null}
     * @param diagnostics findings about the document structure
     */
    public ExtractionResult {
        diagnostics = List.copyOf(Objects.requireNonNull(diagnostics, "diagnostics must not be null"));
    }

    /**
     * Returns the extracted schema, if any.
     *
     * @return the schema source
     */
    public Optional<SchemaSource> schema() {
        return Optional.ofNullable(source);
    }
}
