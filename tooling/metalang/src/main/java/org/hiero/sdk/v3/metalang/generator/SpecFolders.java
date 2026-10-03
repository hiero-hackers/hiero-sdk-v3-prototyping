package org.hiero.sdk.v3.metalang.generator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.hiero.sdk.v3.metalang.model.NamespaceDefinition;

/**
 * The spec folders as build units of a generated SDK (a Java module, an npm package, ...): every folder contains the
 * namespaces of its spec files and requires the folders of the namespaces it uses. A namespace must belong to exactly
 * one folder, every spec file must be inside a folder, and the folders must not depend on each other in a cycle.
 */
public final class SpecFolders {

    /**
     * A spec folder.
     *
     * @param name       the folder name, e.g. {@code consensus-node-client}
     * @param namespaces the namespaces of the folder
     * @param requires   the names of the folders it requires directly
     */
    public record Folder(String name, List<NamespaceDefinition> namespaces, SortedSet<String> requires) {

        /**
         * Creates a folder.
         *
         * @param name       the name
         * @param namespaces the namespaces
         * @param requires   the required folders
         */
        public Folder {
            namespaces = List.copyOf(namespaces);
            requires = java.util.Collections.unmodifiableSortedSet(new TreeSet<>(requires));
        }
    }

    private SpecFolders() {
    }

    /**
     * Returns the folders of a model.
     *
     * @param model the linked model
     * @param unit  what a folder becomes, for the messages (e.g. {@code npm package})
     * @return the folders, sorted by name
     * @throws GenerationException if a namespace is spread over folders, a spec file is outside of a folder, or the
     *                             folders depend on each other in a cycle
     */
    public static List<Folder> of(final LinkedModel model, final String unit) {
        final List<String> problems = new ArrayList<>();
        final Map<String, String> folderOf = new TreeMap<>();
        final SortedMap<String, List<NamespaceDefinition>> namespacesByFolder = new TreeMap<>();
        for (final NamespaceDefinition namespace : model.namespaces()) {
            final SortedSet<String> folders = new TreeSet<>();
            for (final NamespaceDefinition.Source source : namespace.sources()) {
                final int slash = source.file().indexOf('/');
                if (slash < 0) {
                    problems.add("Spec file '" + source.file() + "' of namespace '" + namespace.name()
                            + "' is not inside a folder; the folder defines the " + unit);
                } else {
                    folders.add(source.file().substring(0, slash));
                }
            }
            if (folders.size() > 1) {
                problems.add("Namespace '" + namespace.name() + "' is spread over the spec folders " + folders
                        + "; it must belong to exactly one " + unit);
            }
            if (!folders.isEmpty()) {
                folderOf.put(namespace.name(), folders.first());
                namespacesByFolder.computeIfAbsent(folders.first(), k -> new ArrayList<>()).add(namespace);
            }
        }
        final List<Folder> result = new ArrayList<>();
        namespacesByFolder.forEach((folder, namespaces) -> {
            final SortedSet<String> requires = new TreeSet<>();
            for (final NamespaceDefinition namespace : namespaces) {
                for (final String required : namespace.requiredNamespaces()) {
                    final String requiredFolder = folderOf.get(required);
                    if (requiredFolder != null && !requiredFolder.equals(folder)) {
                        requires.add(requiredFolder);
                    }
                }
            }
            result.add(new Folder(folder, namespaces, requires));
        });
        cycle(result).ifPresent(c -> problems.add("Dependency cycle between the spec folders (" + unit + "s): " + c));
        if (!problems.isEmpty()) {
            throw new GenerationException(problems);
        }
        return result;
    }

    /**
     * Returns the folders a folder requires directly or indirectly.
     *
     * @param folder  the folder name
     * @param folders all folders
     * @return the required folder names
     */
    public static SortedSet<String> required(final String folder, final List<Folder> folders) {
        final Map<String, SortedSet<String>> edges = new TreeMap<>();
        folders.forEach(f -> edges.put(f.name(), f.requires()));
        final SortedSet<String> result = new TreeSet<>();
        final List<String> pending = new ArrayList<>(edges.getOrDefault(folder, new TreeSet<>()));
        while (!pending.isEmpty()) {
            final String next = pending.removeLast();
            if (result.add(next)) {
                pending.addAll(edges.getOrDefault(next, new TreeSet<>()));
            }
        }
        return result;
    }

    /**
     * Returns the folder that all given folders can use: one of them that all others require (the one required by
     * the most folders first, then by name).
     *
     * @param users   the folders that use something
     * @param folders all folders
     * @return the folder, empty if there is none
     */
    public static Optional<String> common(final Set<String> users, final List<Folder> folders) {
        return users.stream().filter(candidate -> users.stream()
                        .allMatch(u -> u.equals(candidate) || required(u, folders).contains(candidate)))
                .sorted().findFirst();
    }

    private static Optional<String> cycle(final List<Folder> folders) {
        final Map<String, SortedSet<String>> edges = new TreeMap<>();
        folders.forEach(f -> edges.put(f.name(), f.requires()));
        final Set<String> done = new HashSet<>();
        for (final String start : edges.keySet()) {
            final Optional<String> cycle = visit(start, edges, new ArrayList<>(), done);
            if (cycle.isPresent()) {
                return cycle;
            }
        }
        return Optional.empty();
    }

    private static Optional<String> visit(final String folder, final Map<String, SortedSet<String>> edges,
                                          final List<String> path, final Set<String> done) {
        final int index = path.indexOf(folder);
        if (index >= 0) {
            final List<String> cycle = new ArrayList<>(path.subList(index, path.size()));
            cycle.add(folder);
            return Optional.of(String.join(" -> ", cycle));
        }
        if (!done.add(folder)) {
            return Optional.empty();
        }
        path.add(folder);
        for (final String next : edges.getOrDefault(folder, new TreeSet<>())) {
            final Optional<String> cycle = visit(next, edges, path, done);
            if (cycle.isPresent()) {
                return cycle;
            }
        }
        path.removeLast();
        return Optional.empty();
    }
}
