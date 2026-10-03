package org.hiero.sdk.v3.metalang.generator.java;

import java.util.Objects;

/**
 * Naming rules of the Java mapping.
 *
 * <ul>
 *   <li>Every <b>spec folder</b> (first path segment below the spec root, e.g. {@code consensus-node-client}) is one
 *       JPMS module, named {@code org.hiero.} plus the folder name with {@code -} replaced by {@code .}
 *       ({@code org.hiero.consensus.node.client}). Each module is laid out like a Maven module:
 *       {@code <module>/src/main/java/...}.</li>
 *   <li>Every <b>namespace</b> is one package, named {@code org.hiero.<namespace>} (see "Namespace Mapping" in
 *       {@code guidelines/api-best-practices-java.md}).</li>
 * </ul>
 */
public final class JavaNames {

    /** Prefix of all packages and modules. */
    public static final String PREFIX = "org.hiero.";

    private JavaNames() {
    }

    /**
     * Returns the Java package of a namespace.
     *
     * @param namespace the namespace
     * @return the package name, e.g. {@code org.hiero.consensusnode.transactions}
     */
    public static String packageName(final String namespace) {
        return PREFIX + Objects.requireNonNull(namespace, "namespace must not be null");
    }

    /**
     * Returns the JPMS module of a spec folder.
     *
     * @param folder the spec folder, e.g. {@code consensus-node-client}
     * @return the module name, e.g. {@code org.hiero.consensus.node.client}
     */
    public static String moduleName(final String folder) {
        return PREFIX + Objects.requireNonNull(folder, "folder must not be null").replace('-', '.');
    }

    /**
     * Returns the source root of a module, relative to the output directory.
     *
     * @param module the module name
     * @return the source root, e.g. {@code org.hiero.base/src/main/java}
     */
    public static String sourceRoot(final String module) {
        return Objects.requireNonNull(module, "module must not be null") + "/src/main/java";
    }

    /**
     * Returns the directory of a namespace's package inside its module, relative to the output directory.
     *
     * @param module    the module name
     * @param namespace the namespace
     * @return the package directory, e.g. {@code org.hiero.base/src/main/java/org/hiero/ledger/config}
     */
    public static String packageDirectory(final String module, final String namespace) {
        return sourceRoot(module) + "/" + packageName(namespace).replace('.', '/');
    }
}
