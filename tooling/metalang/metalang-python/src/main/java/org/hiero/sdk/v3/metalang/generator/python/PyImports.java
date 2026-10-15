package org.hiero.sdk.v3.metalang.generator.python;

import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * The imports of one generated module.
 *
 * <p>Every module starts with {@code from __future__ import annotations}, so annotations are never evaluated at
 * runtime. A type of the SDK that only appears in annotations is therefore imported under {@code TYPE_CHECKING}: the
 * type checker sees it, the interpreter never imports it, and two modules whose types refer to each other cannot form
 * an import cycle. A type that the class statement itself needs — a base class, an enum value, a default value — is
 * imported at runtime, from its private module rather than the package, which is safe even while the package is being
 * initialised.
 *
 * <p>Standard library modules whose names are lowercase ({@code datetime}, {@code uuid}, {@code decimal}) are
 * imported under an alias with a leading underscore, because an attribute of the same name (a field {@code uuid})
 * would otherwise shadow the module in the class body.
 */
final class PyImports {

    /** The lowercase standard library modules, imported as a whole under an alias. */
    private static final Map<String, String> MODULE_ALIASES = Map.of(
            "datetime", "_dt", "decimal", "_decimal", "uuid", "_uuid", "builtins", "_builtins");

    /** The line length of the generated code, the one {@code pyproject.toml} configures for ruff. */
    static final int LINE_LENGTH = 120;

    private final String self;
    private final SortedMap<String, SortedSet<String>> standard = new TreeMap<>();
    private final SortedSet<String> modules = new TreeSet<>();
    private final SortedMap<String, SortedMap<String, String>> runtime = new TreeMap<>();
    private final SortedMap<String, SortedMap<String, String>> typing = new TreeMap<>();
    private final Map<String, String> owner = new TreeMap<>();

    /**
     * Creates the imports of a module.
     *
     * @param self the dotted path of the module itself
     * @param own  the names the module declares
     */
    PyImports(final String self, final Set<String> own) {
        this.self = self;
        own.forEach(name -> owner.put(name, self));
    }

    /**
     * A name of the standard library ({@code typing.Protocol}, {@code collections.abc.Mapping}).
     *
     * @param module the module
     * @param name   the name
     * @return the name to use
     */
    String standard(final String module, final String name) {
        standard.computeIfAbsent(module, k -> new TreeSet<>()).add(name);
        return name;
    }

    /**
     * A lowercase standard library module, imported under its alias ({@code datetime} as {@code _dt}).
     *
     * @param module the module
     * @return the alias
     */
    String module(final String module) {
        final String alias = MODULE_ALIASES.get(module);
        modules.add(module);
        return alias;
    }

    /**
     * A type of the SDK.
     *
     * @param module  the dotted path of its private module
     * @param name    the name
     * @param atRuntime whether the class statement needs it (base class, enum value, default value)
     * @return the name to use in the module
     */
    String sdk(final String module, final String name, final boolean atRuntime) {
        if (module.equals(self)) {
            return name;
        }
        final String taken = owner.putIfAbsent(name, module);
        final String local = taken == null || taken.equals(module) ? name
                : module.replace('.', '_').replaceFirst("^_+", "") + "_" + name;
        if (!local.equals(name)) {
            owner.putIfAbsent(local, module);
        }
        (atRuntime ? runtime : typing).computeIfAbsent(module, k -> new TreeMap<>()).put(name, local);
        if (atRuntime) {
            // a runtime import also serves the annotations
            final SortedMap<String, String> names = typing.get(module);
            if (names != null) {
                names.remove(name);
                if (names.isEmpty()) {
                    typing.remove(module);
                }
            }
        }
        return local;
    }

    /**
     * Renders the import statements.
     *
     * @return the imports, ending with a blank line
     */
    String render() {
        final StringBuilder out = new StringBuilder("from __future__ import annotations\n\n");
        final boolean typeChecking = typing.entrySet().stream()
                .anyMatch(e -> !runtime.containsKey(e.getKey()) || !runtime.get(e.getKey()).keySet()
                        .containsAll(e.getValue().keySet()));
        if (typeChecking) {
            standard("typing", "TYPE_CHECKING");
        }
        // the order of isort and ruff: plain imports before from-imports, constants before classes
        modules.forEach(module -> out.append("import ").append(module).append(" as ")
                .append(MODULE_ALIASES.get(module)).append('\n'));
        standard.forEach((module, names) -> out.append(statement("", module, names.stream()
                .sorted(PyImports::byKind).toList())));
        if (!runtime.isEmpty()) {
            if (!modules.isEmpty() || !standard.isEmpty()) {
                out.append('\n');
            }
            runtime.forEach((module, names) -> out.append(from("", module, names)));
        }
        if (typeChecking) {
            out.append("\nif TYPE_CHECKING:\n");
            typing.forEach((module, names) -> {
                final SortedMap<String, String> remaining = new TreeMap<>(names);
                if (runtime.containsKey(module)) {
                    remaining.keySet().removeAll(runtime.get(module).keySet());
                }
                if (!remaining.isEmpty()) {
                    out.append(from("    ", module, remaining));
                }
            });
        }
        return out.append("\n\n").toString();
    }

    /** Constants ({@code TYPE_CHECKING}) before classes, each group alphabetically. */
    private static int byKind(final String left, final String right) {
        final boolean leftConstant = left.equals(left.toUpperCase(java.util.Locale.ROOT));
        final boolean rightConstant = right.equals(right.toUpperCase(java.util.Locale.ROOT));
        return leftConstant != rightConstant ? (leftConstant ? -1 : 1) : left.compareTo(right);
    }

    private static String from(final String indent, final String module, final SortedMap<String, String> names) {
        return statement(indent, module, names.entrySet().stream()
                .map(e -> e.getKey().equals(e.getValue()) ? e.getKey() : e.getKey() + " as " + e.getValue())
                .toList());
    }

    /**
     * {@code from module import a, b}, wrapped in parentheses with one name per line when it is longer than the
     * line length of the generated code, as ruff and isort do.
     *
     * @param indent the indentation
     * @param module the module
     * @param names  the names, in order
     * @return the statement with a trailing newline
     */
    static String statement(final String indent, final String module, final java.util.List<String> names) {
        final String line = indent + "from " + module + " import " + String.join(", ", names);
        if (line.length() <= LINE_LENGTH) {
            return line + "\n";
        }
        final StringBuilder out = new StringBuilder(indent).append("from ").append(module).append(" import (\n");
        names.forEach(name -> out.append(indent).append("    ").append(name).append(",\n"));
        return out.append(indent).append(")\n").toString();
    }
}
