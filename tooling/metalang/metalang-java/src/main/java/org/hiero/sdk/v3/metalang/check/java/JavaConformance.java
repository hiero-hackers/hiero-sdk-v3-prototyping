package org.hiero.sdk.v3.metalang.check.java;

import org.hiero.sdk.v3.metalang.check.ApiDifference;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.generator.java.JavaGenerator;
import org.hiero.sdk.v3.metalang.model.LinkedModel;

/**
 * Checks whether a Java project provides the API that the generator derives from the specs: the generated
 * declarations are compared structurally with the sources of the project ({@link JavaApiComparison}). The project may
 * be the generated code itself or an implementation that started from it — additional files, types and members and
 * implemented methods are allowed.
 */
public final class JavaConformance {

    private JavaConformance() {
    }

    /**
     * The result of a check.
     *
     * @param types       the number of expected types
     * @param modules     the number of expected modules
     * @param differences the differences; empty if the project provides the expected API
     */
    public record Result(int types, int modules, List<ApiDifference> differences) {

        /**
         * Creates a result.
         *
         * @param types       the number of expected types
         * @param modules     the number of expected modules
         * @param differences the differences
         */
        public Result {
            differences = List.copyOf(differences);
        }
    }

    /**
     * Checks a project against the API generated from a model.
     *
     * @param model     the linked model of the specs
     * @param generator the generator (with its configuration)
     * @param project   the directory of the project; all {@code .java} files below it except build output are read
     * @return the result
     * @throws IOException if the project cannot be read
     */
    public static Result check(final LinkedModel model, final JavaGenerator generator, final Path project)
            throws IOException {
        Objects.requireNonNull(model, "model must not be null");
        Objects.requireNonNull(generator, "generator must not be null");
        Objects.requireNonNull(project, "project must not be null");
        final Map<String, String> sources = new TreeMap<>();
        for (final GeneratedFile file : generator.generate(model)) {
            // the API: the main sources; the generated tests are no part of it
            if (file.path().endsWith(".java") && file.path().contains("/src/main/java/")) {
                sources.put(file.path(), file.content());
            }
        }
        final JavaApi expected = JavaApi.of(sources);
        if (!expected.problems().isEmpty()) {
            throw new IllegalStateException("The generated code cannot be parsed: " + expected.problems());
        }
        return new Result(expected.types().size(), expected.modules().size(),
                JavaApiComparison.compare(expected, JavaApi.of(project, expected.types().keySet())));
    }
}
