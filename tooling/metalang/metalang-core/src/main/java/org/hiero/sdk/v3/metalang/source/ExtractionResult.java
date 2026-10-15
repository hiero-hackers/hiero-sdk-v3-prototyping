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
 * @param instances   the code block of the "## Default Instances for Testing" section, or {@code null} if there is none
 * @param tests       the {@code tests} code blocks of the "## Testing" section, in document order
 */
public record ExtractionResult(@Nullable SchemaSource source, List<Diagnostic> diagnostics,
                               @Nullable SchemaSource instances, List<SchemaSource> tests) {

    /**
     * Creates a new extraction result.
     *
     * @param source      the extracted schema or {@code null}
     * @param diagnostics findings about the document structure
     * @param instances   the default instances block or {@code null}
     * @param tests       the tests blocks
     */
    public ExtractionResult {
        diagnostics = List.copyOf(Objects.requireNonNull(diagnostics, "diagnostics must not be null"));
        tests = List.copyOf(Objects.requireNonNull(tests, "tests must not be null"));
    }

    /**
     * Returns the extracted schema, if any.
     *
     * @return the schema source
     */
    public Optional<SchemaSource> schema() {
        return Optional.ofNullable(source);
    }

    /**
     * Creates a result without default instances.
     *
     * @param source      the extracted schema or {@code null}
     * @param diagnostics findings about the document structure
     */
    public ExtractionResult(final @Nullable SchemaSource source, final List<Diagnostic> diagnostics) {
        this(source, diagnostics, null, List.of());
    }

    /**
     * Returns the code block of the "## Default Instances for Testing" section, if any.
     *
     * @return the block
     */
    public Optional<SchemaSource> defaultInstances() {
        return Optional.ofNullable(instances);
    }
}
