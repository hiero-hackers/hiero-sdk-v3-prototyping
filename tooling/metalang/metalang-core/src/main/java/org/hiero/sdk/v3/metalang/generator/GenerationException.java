package org.hiero.sdk.v3.metalang.generator;

import java.util.List;
import java.util.Objects;

/**
 * The model cannot be mapped to the target language (for example because of a structural constraint of the
 * language). Carries every problem found, not only the first one.
 */
public final class GenerationException extends RuntimeException {

    private final List<String> problems;

    /**
     * Creates the exception.
     *
     * @param problems the problems, at least one
     */
    public GenerationException(final List<String> problems) {
        super(String.join("\n", Objects.requireNonNull(problems, "problems must not be null")));
        if (problems.isEmpty()) {
            throw new IllegalArgumentException("problems must not be empty");
        }
        this.problems = List.copyOf(problems);
    }

    /**
     * Returns all problems.
     *
     * @return the problems
     */
    public List<String> problems() {
        return problems;
    }
}
