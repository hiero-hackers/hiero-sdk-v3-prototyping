package org.hiero.sdk.v3.metalang.generator.go;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * The imports of one generated Go file.
 *
 * <p>A Go reference is qualified by the <em>package name</em>, which is the last segment of the import path. Two
 * imported paths can end in the same segment ({@code .../ledger/config} and {@code .../token/config}); the second
 * one then gets an explicit alias, because Go has no other way to tell them apart.
 */
final class GoImports {

    /** The import path of the package the file belongs to; references to it are not qualified. */
    private final String self;

    /** Import path to the name the file refers to it by, in insertion order. */
    private final Map<String, String> imports = new LinkedHashMap<>();

    /** The names already taken, so a second path ending in the same segment is aliased. */
    private final Map<String, String> takenBy = new LinkedHashMap<>();

    /**
     * Creates the imports of a file.
     *
     * @param self the import path of the file's own package
     */
    GoImports(final String self) {
        this.self = Objects.requireNonNull(self, "self must not be null");
    }

    /**
     * Qualifies a name from another package and records the import.
     *
     * @param path the import path, e.g. {@code time} or {@code github.com/x/sdk/ledger}
     * @param name the exported name within that package, e.g. {@code Time}
     * @return the reference as it is written in the file, e.g. {@code time.Time}
     */
    String qualify(final String path, final String name) {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(name, "name must not be null");
        if (path.equals(self)) {
            return name;
        }
        return use(path) + "." + name;
    }

    /**
     * Records an import without qualifying a name, for a package a generated body needs on its own.
     *
     * @param path the import path
     * @return the name the file refers to the package by
     */
    String use(final String path) {
        Objects.requireNonNull(path, "path must not be null");
        final String existing = imports.get(path);
        if (existing != null) {
            return existing;
        }
        final String name = free(packageName(path), path);
        imports.put(path, name);
        takenBy.put(name, path);
        return name;
    }

    /** The package name of an import path: its last segment, without the characters Go forbids. */
    private static String packageName(final String path) {
        final String last = path.substring(path.lastIndexOf('/') + 1);
        return GoNames.packageName(last);
    }

    /** A name not yet taken by another path: the package name, else more and more of the path prefixed to it. */
    private String free(final String preferred, final String path) {
        if (!takenBy.containsKey(preferred)) {
            return preferred;
        }
        final String[] segments = path.split("/");
        final StringBuilder name = new StringBuilder(preferred);
        for (int i = segments.length - 2; i >= 0; i--) {
            name.insert(0, GoNames.packageName(segments[i]));
            if (!takenBy.containsKey(name.toString())) {
                return name.toString();
            }
        }
        // two identical paths cannot both be unresolved: the map lookup above would have found the first one
        for (int i = 2; ; i++) {
            final String numbered = preferred + i;
            if (!takenBy.containsKey(numbered)) {
                return numbered;
            }
        }
    }

    /**
     * Renders the {@code import} block, or the empty string when the file imports nothing. The standard library
     * forms the first group, everything else the second - the grouping {@code gofmt} preserves and {@code
     * goimports} produces.
     *
     * @return the block, ending in a blank line, or {@code ""}
     */
    String render() {
        if (imports.isEmpty()) {
            return "";
        }
        final Map<String, String> standard = new TreeMap<>();
        final Map<String, String> external = new TreeMap<>();
        imports.forEach((path, name) -> (isStandardLibrary(path) ? standard : external).put(path, name));
        final StringBuilder go = new StringBuilder("import (\n");
        go.append(group(standard));
        if (!standard.isEmpty() && !external.isEmpty()) {
            go.append('\n');
        }
        go.append(group(external));
        return go.append(")\n\n").toString();
    }

    private static String group(final Map<String, String> paths) {
        final StringBuilder go = new StringBuilder();
        paths.forEach((path, name) -> go.append('\t')
                .append(name.equals(packageName(path)) ? "" : name + " ")
                .append('"').append(path).append("\"\n"));
        return go.toString();
    }

    /** A standard library path has no dot in its first segment - the rule the Go tooling itself uses. */
    private static boolean isStandardLibrary(final String path) {
        final int slash = path.indexOf('/');
        return !(slash < 0 ? path : path.substring(0, slash)).contains(".");
    }
}
