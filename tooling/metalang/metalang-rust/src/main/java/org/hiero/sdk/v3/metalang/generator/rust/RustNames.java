package org.hiero.sdk.v3.metalang.generator.rust;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/** The Rust names of the declarations of the specs. */
final class RustNames {

    /** Keywords of Rust (edition 2024, including the reserved ones); they become raw identifiers. */
    private static final Set<String> KEYWORDS = Set.of("as", "break", "const", "continue", "else", "enum",
            "extern", "false", "fn", "for", "if", "impl", "in", "let", "loop", "match", "mod", "move", "mut", "pub",
            "ref", "return", "static", "struct", "trait", "true", "type", "unsafe", "use", "where", "while", "async",
            "await", "dyn", "abstract", "become", "box", "do", "final", "macro", "override", "priv", "typeof",
            "unsized", "virtual", "yield", "try", "gen");

    /** Keywords that cannot be raw identifiers; they get a trailing underscore. */
    private static final Set<String> NO_RAW = Set.of("crate", "self", "super", "Self");

    /** The directory of the support module ({@code src/support}) and its module name. */
    static final String SUPPORT = "support";

    private RustNames() {
    }

    /**
     * Converts a camel-case name to snake case: {@code toStringWithChecksum} → {@code to_string_with_checksum},
     * {@code HTTPClient} → {@code http_client}, {@code ofBytes23} → {@code of_bytes23}.
     *
     * @param name the name
     * @return the snake-case name
     */
    static String snake(final String name) {
        final StringBuilder out = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            final char c = name.charAt(i);
            if (Character.isUpperCase(c)) {
                final boolean previousLower = i > 0 && (Character.isLowerCase(name.charAt(i - 1))
                        || Character.isDigit(name.charAt(i - 1)));
                final boolean acronymEnd = i > 0 && Character.isUpperCase(name.charAt(i - 1))
                        && i + 1 < name.length() && Character.isLowerCase(name.charAt(i + 1));
                if ((previousLower || acronymEnd) && !out.isEmpty() && out.charAt(out.length() - 1) != '_') {
                    out.append('_');
                }
                out.append(Character.toLowerCase(c));
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    /**
     * Converts a snake or kebab-case name to Pascal case: {@code ECDSA_SECP256K1} → {@code EcdsaSecp256k1},
     * {@code not-found} → {@code NotFound}.
     *
     * @param name the name
     * @return the Pascal-case name
     */
    static String pascal(final String name) {
        return Arrays.stream(name.split("[_\\-]+")).filter(p -> !p.isEmpty())
                .map(p -> Character.toUpperCase(p.charAt(0)) + p.substring(1).toLowerCase(Locale.ROOT))
                .collect(Collectors.joining());
    }

    /**
     * Returns an identifier for a snake-case name: keywords become raw identifiers ({@code r#type}).
     *
     * @param name the name
     * @return the identifier
     */
    static String identifier(final String name) {
        if (NO_RAW.contains(name)) {
            return name + "_";
        }
        return KEYWORDS.contains(name) ? "r#" + name : name;
    }

    /**
     * Returns the identifier of an attribute, method, function or parameter.
     *
     * @param name the name in the specs (lower camel case)
     * @return the snake-case identifier
     */
    static String member(final String name) {
        return identifier(snake(name));
    }

    /**
     * Returns the name of an enum variant.
     *
     * @param name the name of the enum value in the specs (upper snake case)
     * @return the Pascal-case variant
     */
    static String variant(final String name) {
        final String pascal = pascal(name);
        return pascal.isEmpty() ? name : pascal;
    }

    /**
     * Returns the module name of a namespace segment.
     *
     * @param segment the segment, e.g. {@code nativeToken}
     * @return the module name, e.g. {@code native_token}
     */
    static String module(final String segment) {
        return identifier(snake(segment));
    }

    /**
     * Returns the module path of a namespace within its crate.
     *
     * @param namespace the namespace, e.g. {@code consensusnode.transactions}
     * @return the path, e.g. {@code consensusnode::transactions}
     */
    static String modulePath(final String namespace) {
        return Arrays.stream(namespace.split("\\.")).map(RustNames::module).collect(Collectors.joining("::"));
    }

    /**
     * Returns the source directory of a namespace within its crate.
     *
     * @param namespace the namespace
     * @return e.g. {@code consensusnode/transactions}
     */
    static String directory(final String namespace) {
        return Arrays.stream(namespace.split("\\.")).map(s -> snake(s)).collect(Collectors.joining("/"));
    }

    /**
     * Returns the name of the error type of an error identifier.
     *
     * @param errorId the identifier, e.g. {@code not-found-error}
     * @return e.g. {@code NotFoundError}
     */
    static String errorType(final String errorId) {
        final String base = errorId.endsWith("-error") ? errorId.substring(0, errorId.length() - 6) : errorId;
        return pascal(base) + "Error";
    }

    /**
     * Returns the directory of a crate in the workspace.
     *
     * @param folder the spec folder
     * @return e.g. {@code crates/base}
     */
    static String crateDirectory(final String folder) {
        return "crates/" + folder;
    }
}
