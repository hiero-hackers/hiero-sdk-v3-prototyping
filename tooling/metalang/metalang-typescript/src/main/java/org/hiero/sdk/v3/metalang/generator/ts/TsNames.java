package org.hiero.sdk.v3.metalang.generator.ts;

import java.util.Set;

/**
 * Names and paths of the TypeScript mapping: one npm package per spec folder ({@code packages/<folder>}, published as
 * {@code <scope>/<folder>}), one directory per namespace ({@code src/consensusnode/transactions}) that the package
 * exports as subpath ({@code @hiero/consensus-node-client/consensusnode/transactions}), one file per type.
 */
final class TsNames {

    /** Words that cannot be names of parameters or variables. */
    private static final Set<String> RESERVED = Set.of("break", "case", "catch", "class", "const", "continue",
            "debugger", "default", "delete", "do", "else", "enum", "export", "extends", "false", "finally", "for",
            "function", "if", "import", "in", "instanceof", "new", "null", "return", "super", "switch", "this", "throw",
            "true", "try", "typeof", "var", "void", "while", "with", "yield", "let", "static", "implements",
            "interface", "package", "private", "protected", "public", "await", "arguments", "eval");

    /** The directory of the support files ({@code Duration}, {@code StreamItem}, ...) in a package. */
    static final String SUPPORT = "support";

    private TsNames() {
    }

    static String packageDirectory(final String folder) {
        return "packages/" + folder;
    }

    static String namespacePath(final String namespace) {
        return namespace.replace('.', '/');
    }

    static String sourceDirectory(final String folder, final String namespace) {
        return packageDirectory(folder) + "/src/" + namespacePath(namespace);
    }

    static String packageName(final TsGeneratorConfig config, final String folder) {
        return config.scope() + "/" + folder;
    }

    /**
     * A name of a parameter or variable: reserved words get a trailing {@code _}.
     *
     * @param name the name in the specs
     * @return the TypeScript name
     */
    static String local(final String name) {
        return RESERVED.contains(name) ? name + "_" : name;
    }

    /**
     * The name of an error class: {@code not-found-error} becomes {@code NotFoundError}, {@code illegal-format}
     * becomes {@code IllegalFormatError}.
     *
     * @param errorId the error identifier
     * @return the class name
     */
    static String errorClass(final String errorId) {
        final String base = errorId.endsWith("-error") ? errorId.substring(0, errorId.length() - 6) : errorId;
        final StringBuilder name = new StringBuilder();
        for (final String part : base.split("-")) {
            if (!part.isEmpty()) {
                name.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
        }
        return name.append("Error").toString();
    }

    /**
     * The relative module specifier from a directory to a file ({@code ../ledger/AccountId.js}).
     *
     * @param fromDirectory the directory of the importing file
     * @param toFile        the imported file without extension
     * @return the specifier
     */
    static String relative(final String fromDirectory, final String toFile) {
        final String[] from = fromDirectory.split("/");
        final String[] to = toFile.split("/");
        int common = 0;
        while (common < from.length && common < to.length - 1 && from[common].equals(to[common])) {
            common++;
        }
        final StringBuilder path = new StringBuilder();
        for (int i = common; i < from.length; i++) {
            path.append("../");
        }
        if (path.isEmpty()) {
            path.append("./");
        }
        for (int i = common; i < to.length; i++) {
            path.append(to[i]).append(i + 1 < to.length ? "/" : "");
        }
        return path + ".js";
    }
}
