package org.hiero.sdk.v3.metalang.generator.ts;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.hiero.sdk.v3.metalang.model.QualifiedName;

/**
 * The imports of one generated file. Declarations of the same package are imported from their file (relative
 * specifier), declarations of other packages from the namespace subpath of the package
 * ({@code @hiero/base/ledger}). Names that are only used in type positions are imported with {@code type}, so that
 * modules do not depend on each other at runtime without need. A simple name used for two declarations gets an alias
 * ({@code ledger_Address}).
 */
final class TsImports {

    private record Entry(String specifier, String name) {
    }

    private final TsContext context;
    private final String folder;
    private final String directory;
    private final Map<String, Entry> byLocal = new HashMap<>();
    private final SortedMap<String, SortedMap<String, Boolean>> imports = new TreeMap<>();

    /**
     * Creates the imports of a file.
     *
     * @param context    the generation context
     * @param folder     the spec folder (package) of the file
     * @param directory  the directory of the file
     * @param ownNames   the names the file declares itself
     */
    TsImports(final TsContext context, final String folder, final String directory, final String... ownNames) {
        this.context = context;
        this.folder = folder;
        this.directory = directory;
        for (final String own : ownNames) {
            byLocal.put(own, new Entry("", own));
        }
    }

    TsContext context() {
        return context;
    }

    /** A type, class or enum, used as type. */
    String type(final QualifiedName type) {
        return use(context.folder(type.namespace()), type.namespace(), type.name(), type.name(), false);
    }

    /** A class or enum, used as value ({@code new}, {@code extends}, constants, static methods). */
    String value(final QualifiedName type) {
        return use(context.folder(type.namespace()), type.namespace(), type.name(), type.name(), true);
    }

    /** A namespace-level function. */
    String function(final String namespace, final String name) {
        return use(context.folder(namespace), namespace, name, "functions", true);
    }

    /** A constant. */
    String constant(final String namespace, final String name) {
        return use(context.folder(namespace), namespace, name, "constants", true);
    }

    /** An error class. */
    String error(final TsContext.ErrorClass error, final boolean value) {
        return use(context.folder(error.namespace()), error.namespace(), error.name(), "errors", value);
    }

    /** A declaration of a package that is not generated from the specs (e.g. the TCK contract). */
    String external(final String specifier, final String name, final boolean value) {
        return add(specifier, "external", name, value);
    }

    /** A declaration of the hand-written support package ({@code Duration}, {@code StreamItem}, ...). */
    String support(final String name, final boolean value) {
        return add(TsNames.supportPackage(context.config()), "support", name, value);
    }

    private String use(final String targetFolder, final String namespace, final String name, final String stem,
                       final boolean value) {
        final String specifier;
        if (targetFolder.equals(folder)) {
            final String target = TsNames.sourceDirectory(targetFolder, namespace) + "/" + stem;
            if (target.equals(directory + "/" + stem) && byLocal.containsKey(name)
                    && byLocal.get(name).specifier().isEmpty()) {
                return name; // declared in this file
            }
            specifier = TsNames.relative(directory, target);
        } else {
            specifier = TsNames.packageName(context.config(), targetFolder) + "/" + TsNames.namespacePath(namespace);
        }
        return add(specifier, namespace.replace('.', '_'), name, value);
    }

    /** Adds an import; a name that is already imported from elsewhere gets a prefixed local name. */
    private String add(final String specifier, final String prefix, final String name, final boolean value) {
        final Entry entry = new Entry(specifier, name);
        String local = name;
        if (byLocal.containsKey(local) && !byLocal.get(local).equals(entry)) {
            local = prefix + "_" + name;
        }
        byLocal.putIfAbsent(local, entry);
        final String imported = local.equals(name) ? name : name + " as " + local;
        imports.computeIfAbsent(specifier, k -> new TreeMap<>()).merge(imported, value, Boolean::logicalOr);
        return local;
    }

    /**
     * Renders the import statements.
     *
     * @return the imports, one per line, sorted by specifier (packages before relative paths)
     */
    String render() {
        return render(null);
    }

    /**
     * Renders the import statements of the names that the code uses.
     *
     * @param code the code of the file without imports, {@code null} to render all imports
     * @return the imports, one per line, sorted by specifier
     */
    String render(final String code) {
        // names in string literals (test names) do not use the import
        final String text = code == null ? null : code.replaceAll("\"(\\\\.|[^\"\\\\])*\"", "\"\"");
        final StringBuilder out = new StringBuilder();
        imports.forEach((specifier, all) -> {
            final SortedMap<String, Boolean> names = new TreeMap<>();
            all.forEach((imported, value) -> {
                final String local = imported.contains(" as ") ? imported.substring(imported.indexOf(" as ") + 4)
                        : imported;
                if (text == null || java.util.regex.Pattern.compile("(?<![\\w$.])" + java.util.regex.Pattern.quote(local)
                        + "(?![\\w$])").matcher(text).find()) {
                    names.put(imported, value);
                }
            });
            if (names.isEmpty()) {
                return;
            }
            if (names.values().stream().noneMatch(v -> v)) {
                out.append("import type { ").append(String.join(", ", names.keySet()));
            } else {
                out.append("import { ").append(names.entrySet().stream()
                        .map(e -> (e.getValue() ? "" : "type ") + e.getKey()).collect(Collectors.joining(", ")));
            }
            out.append(" } from \"").append(specifier).append("\";\n");
        });
        return out.toString();
    }
}
