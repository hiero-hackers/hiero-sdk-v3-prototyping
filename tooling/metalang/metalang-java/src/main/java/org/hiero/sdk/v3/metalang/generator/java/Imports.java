package org.hiero.sdk.v3.metalang.generator.java;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * The imports of one generated Java file. {@link #use(String, String)} returns the name to write in the code: the
 * simple name if it can be imported (or needs no import), otherwise the fully qualified name.
 */
final class Imports {

    /** Types of {@code java.lang} the generated code may use without import; type variables must not shadow them. */
    private static final Set<String> JAVA_LANG = Set.of("Boolean", "Byte", "Class", "Deprecated", "Double", "Integer",
            "Long", "Object", "Override", "Short", "String", "UnsupportedOperationException", "Void");

    private final String ownPackage;
    private final Set<String> typeNames;
    private final FunctionInterfaces functionInterfaces;
    private final Map<String, String> externalPackages;
    private final Map<String, String> bySimpleName = new HashMap<>();
    private final SortedSet<String> imports = new TreeSet<>();

    /**
     * Creates the imports of a file whose type variables cannot clash with spec types.
     *
     * @param ownPackage      the package of the file
     * @param reservedNames   simple names that are already taken in the file (e.g. the declared type itself)
     */
    Imports(final String ownPackage, final String... reservedNames) {
        this(ownPackage, Set.of(), FunctionInterfaces.NONE, reservedNames);
    }

    /**
     * Creates the imports of a file without external namespaces.
     *
     * @param ownPackage         the package of the file
     * @param typeNames          the simple names of all spec types (type variables must not shadow them)
     * @param functionInterfaces the custom functional interfaces of function types
     * @param reservedNames      simple names that are already taken in the file (e.g. the declared type itself)
     */
    Imports(final String ownPackage, final Set<String> typeNames, final FunctionInterfaces functionInterfaces,
            final String... reservedNames) {
        this(ownPackage, typeNames, functionInterfaces, Map.of(), reservedNames);
    }

    /**
     * Creates the imports of a file.
     *
     * @param ownPackage      the package of the file
     * @param typeNames          the simple names of all spec types (type variables must not shadow them)
     * @param functionInterfaces the custom functional interfaces of function types
     * @param externalPackages   the real Java package of every package that is not generated, by the package name
     *                           the namespace would have ({@link JavaGeneratorConfig#EXTERNAL_NAMESPACE_PREFIX})
     * @param reservedNames      simple names that are already taken in the file (e.g. the declared type itself)
     */
    Imports(final String ownPackage, final Set<String> typeNames, final FunctionInterfaces functionInterfaces,
            final Map<String, String> externalPackages, final String... reservedNames) {
        this.ownPackage = Objects.requireNonNull(ownPackage, "ownPackage must not be null");
        this.typeNames = Set.copyOf(Objects.requireNonNull(typeNames, "typeNames must not be null"));
        this.functionInterfaces = Objects.requireNonNull(functionInterfaces, "functionInterfaces must not be null");
        this.externalPackages = Map.copyOf(Objects.requireNonNull(externalPackages,
                "externalPackages must not be null"));
        for (final String reserved : reservedNames) {
            bySimpleName.put(reserved, ownPackage + "." + reserved);
        }
    }

    /**
     * Returns the name to use for a type and registers an import if needed. A package of an external namespace is
     * replaced by the Java package that really provides the type, so the rest of the generator can keep computing
     * package names from namespaces.
     *
     * @param generatedPackage the package the namespace of the type maps to
     * @param simpleName       the simple name (for nested types: {@code Outer.Inner})
     * @return the name to write in the code
     */
    String use(final String generatedPackage, final String simpleName) {
        final String packageName = externalPackages.getOrDefault(generatedPackage, generatedPackage);
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
    /**
     * Returns the Java name of a type variable: the meta-language name without {@code $$}. If that name is also the
     * name of a spec type or of a {@code java.lang} type, a {@code T} is appended until it is unique
     * ({@code $$Receipt extends Receipt} becomes {@code ReceiptT extends Receipt}); otherwise the type variable would
     * shadow the type.
     *
     * @param name the meta-language name, e.g. {@code $$Receipt}
     * @return the Java name
     */
    String typeVariable(final String name) {
        String java = name.startsWith("$$") ? name.substring(2) : name;
        while (typeNames.contains(java) || JAVA_LANG.contains(java)) {
            java = java + "T";
        }
        return java;
    }

    /**
     * Returns the custom functional interfaces of the generation.
     *
     * @return the functional interfaces
     */
    FunctionInterfaces functionInterfaces() {
        return functionInterfaces;
    }

    String render() {
        final StringBuilder out = new StringBuilder();
        imports.forEach(i -> out.append("import ").append(i).append(";\n"));
        return out.toString();
    }
}
