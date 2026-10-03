package org.hiero.sdk.v3.metalang.generator.java;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * The imports of one generated Java file. {@link #use(String, String)} returns the name to write in the code: the
 * simple name if it can be imported (or needs no import), otherwise the fully qualified name.
 */
final class Imports {

    private final String ownPackage;
    private final Map<String, String> bySimpleName = new HashMap<>();
    private final SortedSet<String> imports = new TreeSet<>();

    /**
     * Creates the imports of a file.
     *
     * @param ownPackage      the package of the file
     * @param reservedNames   simple names that are already taken in the file (e.g. the declared type itself)
     */
    Imports(final String ownPackage, final String... reservedNames) {
        this.ownPackage = Objects.requireNonNull(ownPackage, "ownPackage must not be null");
        for (final String reserved : reservedNames) {
            bySimpleName.put(reserved, ownPackage + "." + reserved);
        }
    }

    /**
     * Returns the name to use for a type and registers an import if needed.
     *
     * @param packageName the package of the type
     * @param simpleName  the simple name (for nested types: {@code Outer.Inner})
     * @return the name to write in the code
     */
    String use(final String packageName, final String simpleName) {
        final String topLevel = simpleName.contains(".") ? simpleName.substring(0, simpleName.indexOf('.'))
                : simpleName;
        final String qualified = packageName + "." + topLevel;
        final String existing = bySimpleName.get(topLevel);
        if (existing != null && !existing.equals(qualified)) {
            return packageName + "." + simpleName; // name clash: use the qualified name
        }
        bySimpleName.put(topLevel, qualified);
        if (!packageName.equals(ownPackage) && !packageName.equals("java.lang")) {
            imports.add(qualified);
        }
        return simpleName;
    }

    /**
     * Returns the import statements, sorted, each terminated by a line break.
     *
     * @return the import block (empty if there are no imports)
     */
    String render() {
        final StringBuilder out = new StringBuilder();
        imports.forEach(i -> out.append("import ").append(i).append(";\n"));
        return out.toString();
    }
}
