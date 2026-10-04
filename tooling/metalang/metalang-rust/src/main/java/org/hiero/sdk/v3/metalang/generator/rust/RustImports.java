package org.hiero.sdk.v3.metalang.generator.rust;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.hiero.sdk.v3.metalang.model.QualifiedName;

/**
 * The {@code use} declarations of one generated Rust file. Every referenced item is imported once under its simple
 * name, or under an alias if the name is taken; {@link #render(String)} only keeps the imports the code uses (and the
 * traits whose methods it calls).
 */
final class RustImports {

    private final RustContext context;
    private final String folder;
    private final boolean testCrate;
    private final Set<String> reserved;
    /** full path → local name */
    private final Map<String, String> imports = new TreeMap<>();
    private final Map<String, String> byLocal = new HashMap<>();
    /** full paths of traits imported anonymously ({@code use path as _;}) for their methods */
    private final Set<String> traits = new TreeSet<>();
    private final Set<QualifiedName> declared = new HashSet<>();

    /**
     * Creates the imports of a file.
     *
     * @param context   the generation context
     * @param folder    the spec folder of the crate the file belongs to
     * @param testCrate whether the file is part of an integration test (items of the own crate are then referenced
     *                  by the library name instead of {@code crate})
     * @param reserved  names the file declares itself; imports with these names get an alias
     */
    RustImports(final RustContext context, final String folder, final boolean testCrate, final String... reserved) {
        this.context = context;
        this.folder = folder;
        this.testCrate = testCrate;
        this.reserved = new HashSet<>(List.of(reserved));
    }

    RustContext context() {
        return context;
    }

    String folder() {
        return folder;
    }

    /** Declares a name of the file; an import with this name gets an alias. */
    void reserve(final String name) {
        reserved.add(name);
    }

    /** Declares a type that the file defines; it is referenced by its name without import. */
    void declare(final QualifiedName type) {
        declared.add(type);
        reserved.add(type.name());
    }

    /**
     * Returns the local name of a generated type, trait or enum.
     *
     * @param type the type
     * @return the name to use in the code
     */
    String type(final QualifiedName type) {
        if (declared.contains(type)) {
            return type.name();
        }
        return use(path(type.namespace()) + "::" + type.name(), type.name(),
                RustNames.pascal(lastSegment(type.namespace())) + type.name());
    }

    /**
     * Returns the local name of a function of a namespace.
     *
     * @param namespace the namespace
     * @param name      the Rust name of the function
     * @return the name to use in the code
     */
    String function(final String namespace, final String name) {
        return use(path(namespace) + "::" + name, name, RustNames.snake(lastSegment(namespace)) + "_" + name);
    }

    /**
     * Returns the local name of a constant of a namespace.
     *
     * @param namespace the namespace
     * @param name      the name of the constant
     * @return the name to use in the code
     */
    String constant(final String namespace, final String name) {
        return use(path(namespace) + "::" + name, name,
                RustNames.snake(lastSegment(namespace)).toUpperCase(java.util.Locale.ROOT) + "_" + name);
    }

    /**
     * Returns the local name of an error type.
     *
     * @param error the error type
     * @return the name to use in the code
     */
    String error(final RustContext.ErrorType error) {
        if (error.namespace() == null) {
            return support(error.name());
        }
        if (declared.contains(new QualifiedName(error.namespace(), error.name()))) {
            return error.name();
        }
        return use(path(error.namespace()) + "::" + error.name(), error.name(),
                RustNames.pascal(lastSegment(error.namespace())) + error.name());
    }

    /**
     * Returns the local name of a support type ({@code InvalidArgumentError}, {@code BoxFuture}, ...).
     *
     * @param name the name of the support item
     * @return the name to use in the code
     */
    String support(final String name) {
        final String supportFolder = context.supportFolder();
        final String root = !testCrate && supportFolder.equals(folder) ? "crate"
                : context.config().libraryName(supportFolder);
        return use(root + "::" + RustNames.SUPPORT + "::" + name, name, "Support" + name);
    }

    /**
     * Returns the local name of an item of the standard library or of a dependency, e.g. {@code std::sync::Arc}.
     *
     * @param path the full path
     * @return the name to use in the code
     */
    String external(final String path) {
        final String name = path.substring(path.lastIndexOf(':') + 1);
        final String local = use(path, name, null);
        return local == null ? path : local;
    }

    /**
     * Imports a trait anonymously so that its methods can be called.
     *
     * @param trait the trait
     */
    void traitInScope(final QualifiedName trait) {
        traits.add(path(trait.namespace()) + "::" + trait.name());
    }

    /** Imports a trait of the standard library or of a dependency anonymously, e.g. {@code std::str::FromStr}. */
    void externalTraitInScope(final String path) {
        traits.add(path);
    }

    /**
     * Returns the path of a namespace module: {@code crate::ledger} in its own crate, {@code hiero_base::ledger}
     * otherwise.
     *
     * @param namespace the namespace
     * @return the module path
     */
    String path(final String namespace) {
        final String namespaceFolder = context.folder(namespace);
        final String root = !testCrate && namespaceFolder.equals(folder) ? "crate"
                : context.config().libraryName(namespaceFolder);
        return root + "::" + RustNames.modulePath(namespace);
    }

    private String use(final String path, final String name, final String alias) {
        final String existing = imports.get(path);
        if (existing != null) {
            return existing;
        }
        String local = name;
        if (reserved.contains(local) || byLocal.containsKey(local)
                || alias != null && RustContext.PRELUDE.contains(local)) {
            if (alias == null) {
                return null;
            }
            local = alias;
            for (int i = 2; reserved.contains(local) || byLocal.containsKey(local); i++) {
                local = alias + i;
            }
        }
        imports.put(path, local);
        byLocal.put(local, path);
        return local;
    }

    /**
     * Renders the {@code use} declarations that the code needs.
     *
     * @param code the code of the file without the imports
     * @return the declarations, each on its own line; empty if there are none
     */
    String render(final String code) {
        final String names = RustImports.codeOnly(code);
        final Set<String> lines = new TreeSet<>();
        imports.forEach((path, local) -> {
            if (Pattern.compile("(?<![\\w:])" + Pattern.quote(local) + "(?![\\w])").matcher(names).find()) {
                final String name = path.substring(path.lastIndexOf(':') + 1);
                lines.add("use " + path + (name.equals(local) ? "" : " as " + local) + ";");
            }
        });
        for (final String trait : traits) {
            if (lines.stream().noneMatch(l -> l.startsWith("use " + trait + ";") || l.startsWith("use " + trait
                    + " as "))) {
                lines.add("use " + trait + " as _;");
            }
        }
        return lines.isEmpty() ? "" : String.join("\n", lines) + "\n";
    }

    /** The code without string literals and comments, to find the names it uses. */
    static String codeOnly(final String code) {
        final StringBuilder out = new StringBuilder();
        int i = 0;
        while (i < code.length()) {
            final char c = code.charAt(i);
            if (c == '/' && i + 1 < code.length() && code.charAt(i + 1) == '/') {
                while (i < code.length() && code.charAt(i) != '\n') {
                    i++;
                }
            } else if (c == '"') {
                i++;
                while (i < code.length() && code.charAt(i) != '"') {
                    i += code.charAt(i) == '\\' ? 2 : 1;
                }
                i++;
                out.append("\"\"");
            } else {
                out.append(c);
                i++;
            }
        }
        return out.toString();
    }

    private static String lastSegment(final String namespace) {
        return namespace.substring(namespace.lastIndexOf('.') + 1);
    }
}
