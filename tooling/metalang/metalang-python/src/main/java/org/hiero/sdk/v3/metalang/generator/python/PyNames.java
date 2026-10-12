package org.hiero.sdk.v3.metalang.generator.python;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Python names of the meta-language identifiers, following PEP 8: types keep their spelling, attributes and modules
 * are {@code snake_case}, keywords get a trailing underscore.
 */
final class PyNames {

    /** The hard keywords; {@code type}, {@code match} and {@code case} are soft and stay usable. */
    private static final Set<String> KEYWORDS = Set.of("False", "None", "True", "and", "as", "assert", "async",
            "await", "break", "class", "continue", "def", "del", "elif", "else", "except", "finally", "for", "from",
            "global", "if", "import", "in", "is", "lambda", "nonlocal", "not", "or", "pass", "raise", "return", "try",
            "while", "with", "yield");

    private PyNames() {
    }

    /**
     * Whether a name is a Python keyword.
     *
     * @param name the name
     * @return {@code true} for a hard keyword
     */
    static boolean isKeyword(final String name) {
        return KEYWORDS.contains(name);
    }

    /**
     * Splits an identifier into words: at a change from lower case or digit to upper case, and before the last
     * capital of an acronym ({@code HTTPClient} is {@code HTTP}, {@code Client}).
     *
     * @param name the identifier
     * @return the words
     */
    static List<String> words(final String name) {
        return Arrays.stream(name.split("_|(?<=[a-z0-9])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])"))
                .filter(word -> !word.isEmpty()).toList();
    }

    /**
     * {@code evmAddress} becomes {@code evm_address}, {@code HTTPClient} becomes {@code http_client}.
     *
     * @param name the identifier
     * @return the snake case form
     */
    static String snake(final String name) {
        return words(name).stream().map(word -> word.toLowerCase(Locale.ROOT)).collect(Collectors.joining("_"));
    }

    /**
     * An attribute, parameter or package name: snake case, with a trailing underscore for a keyword.
     *
     * @param name the meta-language name
     * @return the Python identifier
     */
    static String identifier(final String name) {
        final String snake = snake(name);
        return isKeyword(snake) ? snake + "_" : snake;
    }

    /**
     * The directory of a namespace below the import package: {@code nativeToken} is {@code native_token},
     * {@code consensusnode.transactions} is {@code consensusnode/transactions}.
     *
     * @param namespace the namespace
     * @return the relative directory
     */
    static String packagePath(final String namespace) {
        return Arrays.stream(namespace.split("\\.")).map(PyNames::identifier).collect(Collectors.joining("/"));
    }

    /**
     * The private module of a type: {@code AccountId} is {@code _account_id}.
     *
     * @param typeName the simple name of the type
     * @return the module name
     */
    static String module(final String typeName) {
        return "_" + snake(typeName);
    }
}
