package org.hiero.sdk.v3.metalang.generator.go;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * The name mapping of the Go generator (see {@code guidelines/api-best-practices-go.md}, "Names").
 *
 * <p>Go's convention is not a case conversion: initialisms stay fully capitalised, so {@code accountId} becomes
 * {@code AccountID} and {@code restBaseUrl} becomes {@code RESTBaseURL}. A name is therefore split into words and
 * each word is either upper-cased as a whole, when it is a known initialism, or capitalised.
 */
final class GoNames {

    /**
     * The initialisms Go capitalises as a whole. The list follows the one the Go linters use, plus the terms this
     * API adds ({@code DER}, {@code PEM}, {@code EVM}, {@code HBAR}, {@code PKCS8}, {@code SPKI}) and two the
     * linters omit but the ecosystem capitalises ({@code REST} as in Kubernetes' {@code RESTClient},
     * {@code GRPC}).
     */
    static final Set<String> INITIALISMS = Set.of(
            "ACL", "API", "ASCII", "CPU", "CSS", "DER", "DNS", "ECDSA", "EOF", "EVM", "GUID", "HBAR", "HTML",
            "GRPC", "HTTP", "HTTPS", "ID", "IP", "JSON", "LHS", "PEM", "PKCS8", "QPS", "RAM", "REST", "RHS",
            "RPC", "RPS", "RTP", "SLA", "SMTP", "SPKI", "SQL", "SSH", "TCP", "TLS", "TTL", "UDP", "UI", "UID",
            "URI", "URL", "UTF8", "UUID", "VM", "XML", "XMPP", "XSRF", "XSS");

    // ED25519 is deliberately absent: Go spells it Ed25519, as the standard library's crypto/ed25519 does.

    /** Go keywords and predeclared identifiers that a generated name must not collide with. */
    private static final Set<String> RESERVED = Set.of(
            "break", "case", "chan", "const", "continue", "default", "defer", "else", "fallthrough", "for",
            "func", "go", "goto", "if", "import", "interface", "map", "package", "range", "return", "select",
            "struct", "switch", "type", "var",
            "any", "append", "bool", "byte", "cap", "clear", "close", "complex", "copy", "delete", "error",
            "false", "float32", "float64", "int", "len", "make", "new", "nil", "panic", "print", "println",
            "recover", "rune", "string", "true", "uint", "uintptr");

    private GoNames() {
    }

    /**
     * Splits an identifier into its words. The meta-language writes {@code lowerCamelCase} and
     * {@code UPPER_SNAKE_CASE}; both reduce to the same word list.
     *
     * @param name the identifier
     * @return the words, in order, in upper case
     */
    static List<String> words(final String name) {
        final List<String> words = new java.util.ArrayList<>();
        final StringBuilder word = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            final char c = name.charAt(i);
            if (c == '_' || c == '-' || c == '.') {
                flush(words, word);
            } else if (Character.isUpperCase(c) && !word.isEmpty()
                    && !Character.isUpperCase(name.charAt(i - 1))) {
                flush(words, word);
                word.append(c);
            } else {
                // a digit never starts a word: ED25519 and PKCS8 stay one word each
                word.append(c);
            }
        }
        flush(words, word);
        return List.copyOf(words);
    }

    private static void flush(final List<String> words, final StringBuilder word) {
        if (!word.isEmpty()) {
            words.add(word.toString().toUpperCase(Locale.ROOT));
            word.setLength(0);
        }
    }

    /**
     * The exported Go name of an identifier: every word capitalised, initialisms upper case.
     *
     * @param name the identifier
     * @return the exported name
     */
    static String exported(final String name) {
        final StringBuilder go = new StringBuilder();
        for (final String word : words(name)) {
            go.append(INITIALISMS.contains(word) ? word
                    : word.charAt(0) + word.substring(1).toLowerCase(Locale.ROOT));
        }
        return go.toString();
    }

    /**
     * The unexported Go name of an identifier, used for struct fields and parameters. The first word is lower
     * case even when it is an initialism, because a leading capital would export it.
     *
     * @param name the identifier
     * @return the unexported name, escaped when it is a Go keyword
     */
    static String unexported(final String name) {
        final List<String> words = words(name);
        if (words.isEmpty()) {
            return name;
        }
        final StringBuilder go = new StringBuilder(words.getFirst().toLowerCase(Locale.ROOT));
        for (final String word : words.subList(1, words.size())) {
            go.append(INITIALISMS.contains(word) ? word
                    : word.charAt(0) + word.substring(1).toLowerCase(Locale.ROOT));
        }
        return identifier(go.toString());
    }

    /**
     * Escapes a name that collides with a Go keyword or predeclared identifier.
     *
     * @param name the name
     * @return the name, with a trailing {@code _} when it is reserved
     */
    static String identifier(final String name) {
        return RESERVED.contains(name) ? name + "_" : name;
    }

    /**
     * The package name of a namespace segment: lower case, no underscores.
     *
     * @param segment the last segment of the namespace
     * @return the package name
     */
    static String packageName(final String segment) {
        return identifier(String.join("", words(segment)).toLowerCase(Locale.ROOT));
    }

    /**
     * The directory of a namespace, one level per segment.
     *
     * @param namespace the namespace, e.g. {@code consensusnode.transactions.accounts}
     * @return the directory, e.g. {@code consensusnode/transactions/accounts}
     */
    static String directory(final String namespace) {
        final StringBuilder path = new StringBuilder();
        for (final String segment : namespace.split("\\.")) {
            if (!path.isEmpty()) {
                path.append('/');
            }
            path.append(packageName(segment));
        }
        return path.toString();
    }

    /**
     * The constant of an enum value: the type name followed by the value, as Go has no scoped constants.
     *
     * @param type  the enum type
     * @param value the value
     * @return the constant name, e.g. {@code KeyAlgorithmED25519}
     */
    static String enumConstant(final String type, final String value) {
        return exported(type) + exported(value);
    }

    /**
     * The error type of an error identifier.
     *
     * @param errorId the identifier, e.g. {@code not-found-error}
     * @return the type name, e.g. {@code NotFoundError}
     */
    static String errorType(final String errorId) {
        final String name = exported(errorId);
        return name.endsWith("Error") ? name : name + "Error";
    }
}
