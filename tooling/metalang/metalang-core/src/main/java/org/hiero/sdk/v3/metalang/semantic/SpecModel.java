package org.hiero.sdk.v3.metalang.semantic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.hiero.sdk.v3.metalang.ast.Declaration;
import org.hiero.sdk.v3.metalang.ast.Requires;
import org.hiero.sdk.v3.metalang.ast.SchemaFile;
import org.hiero.sdk.v3.metalang.ast.TypeRef;
import org.hiero.sdk.v3.metalang.diagnostic.Rule;

/**
 * The semantic model of a set of spec files: namespaces, their types, and name resolution.
 *
 * <p>The model is immutable after construction. All collections are ordered deterministically
 * (files by name, namespaces alphabetically, declarations in source order).
 */
public final class SpecModel {

    private final List<SchemaFile> files;
    private final SortedMap<String, List<SchemaFile>> namespaces;
    private final Map<String, Map<String, Declaration.TypeDeclaration>> types;
    private final Map<Declaration, SchemaFile> declaringFile = new IdentityHashMap<>();
    private final Map<SchemaFile, ImportScope> scopes = new IdentityHashMap<>();

    private record ImportScope(SortedMap<String, SortedSet<String>> explicit, SortedSet<String> wildcard) {
    }

    private SpecModel(final List<SchemaFile> files) {
        this.files = files;
        final SortedMap<String, List<SchemaFile>> byNamespace = new TreeMap<>();
        final Map<String, Map<String, Declaration.TypeDeclaration>> typeIndex = new TreeMap<>();
        for (final SchemaFile file : files) {
            byNamespace.computeIfAbsent(file.namespace(), k -> new ArrayList<>()).add(file);
            final Map<String, Declaration.TypeDeclaration> nsTypes =
                    typeIndex.computeIfAbsent(file.namespace(), k -> new LinkedHashMap<>());
            for (final Declaration declaration : file.declarations()) {
                declaringFile.put(declaration, file);
                if (declaration instanceof Declaration.TypeDeclaration type) {
                    nsTypes.putIfAbsent(type.name(), type);
                }
            }
            scopes.put(file, importScope(file));
        }
        byNamespace.replaceAll((k, v) -> List.copyOf(v));
        this.namespaces = Collections.unmodifiableSortedMap(byNamespace);
        typeIndex.replaceAll((k, v) -> Collections.unmodifiableMap(v));
        this.types = Collections.unmodifiableMap(typeIndex);
    }

    /**
     * Creates a model for the given schema files.
     *
     * @param files the parsed schema files
     * @return the model
     */
    public static SpecModel of(final Collection<SchemaFile> files) {
        Objects.requireNonNull(files, "files must not be null");
        final List<SchemaFile> sorted = files.stream()
                .sorted(Comparator.comparing(SchemaFile::file))
                .toList();
        return new SpecModel(sorted);
    }

    private static ImportScope importScope(final SchemaFile file) {
        final SortedMap<String, SortedSet<String>> explicit = new TreeMap<>();
        final SortedSet<String> wildcard = new TreeSet<>();
        for (final Requires requires : file.requires()) {
            if (requires.wildcard()) {
                wildcard.add(requires.namespace());
            }
            for (final String type : requires.types()) {
                explicit.computeIfAbsent(type, k -> new TreeSet<>()).add(requires.namespace());
            }
        }
        return new ImportScope(explicit, wildcard);
    }

    /**
     * Returns all files, sorted by file name.
     *
     * @return the files
     */
    public List<SchemaFile> files() {
        return files;
    }

    /**
     * Returns the names of all declared namespaces in alphabetical order.
     *
     * @return the namespace names
     */
    public Set<String> namespaceNames() {
        return namespaces.keySet();
    }

    /**
     * Returns the files that declare the given namespace.
     *
     * @param namespace the namespace
     * @return the files (empty if the namespace does not exist)
     */
    public List<SchemaFile> filesOf(final String namespace) {
        return namespaces.getOrDefault(namespace, List.of());
    }

    /**
     * Checks whether a namespace exists.
     *
     * @param namespace the namespace
     * @return {@code true} if at least one file declares it
     */
    public boolean hasNamespace(final String namespace) {
        return namespaces.containsKey(namespace);
    }

    /**
     * Returns the type with the given simple name declared in the given namespace.
     *
     * @param namespace the namespace
     * @param name      the simple type name
     * @return the type declaration, if present
     */
    public Optional<Declaration.TypeDeclaration> type(final String namespace, final String name) {
        return Optional.ofNullable(types.getOrDefault(namespace, Map.of()).get(name));
    }

    /**
     * Returns the file that contains the given declaration.
     *
     * @param declaration a declaration of this model
     * @return the declaring file
     * @throws IllegalArgumentException if the declaration is not part of the model
     */
    public SchemaFile fileOf(final Declaration declaration) {
        final SchemaFile file = declaringFile.get(declaration);
        if (file == null) {
            throw new IllegalArgumentException("Declaration is not part of the model: " + declaration.name());
        }
        return file;
    }

    /**
     * Returns the namespaces (alphabetically) that declare a type with the given simple name.
     *
     * @param name the simple type name
     * @return the namespaces
     */
    public List<String> namespacesDeclaring(final String name) {
        return types.entrySet().stream()
                .filter(e -> e.getValue().containsKey(name))
                .map(Map.Entry::getKey)
                .sorted()
                .toList();
    }

    /**
     * Resolves a named type reference in the scope of the given file.
     *
     * @param file the file that contains the reference
     * @param ref  the reference
     * @return the resolution
     */
    public ResolvedType resolve(final SchemaFile file, final TypeRef.Named ref) {
        return resolve(file, ref.name());
    }

    /**
     * Resolves a (simple or qualified) type name in the scope of the given file.
     *
     * @param file the file in whose scope the name is resolved
     * @param name the type name
     * @return the resolution
     */
    public ResolvedType resolve(final SchemaFile file, final String name) {
        Objects.requireNonNull(file, "file must not be null");
        Objects.requireNonNull(name, "name must not be null");
        final int dot = name.lastIndexOf('.');
        if (dot >= 0) {
            final String namespace = name.substring(0, dot);
            final String simple = name.substring(dot + 1);
            if (!hasNamespace(namespace)) {
                return new ResolvedType.Unresolved(Rule.TYPE_UNKNOWN,
                        "Unknown namespace '" + namespace + "' in type reference '" + name + "'");
            }
            return type(namespace, simple)
                    .<ResolvedType>map(t -> new ResolvedType.Declared(namespace, t))
                    .orElseGet(() -> new ResolvedType.Unresolved(Rule.TYPE_UNKNOWN,
                            "Namespace '" + namespace + "' declares no type '" + simple + "'"));
        }
        final Optional<BuiltinType> builtin = BuiltinType.lookup(name);
        if (builtin.isPresent()) {
            return new ResolvedType.Builtin(builtin.get());
        }
        final Optional<Declaration.TypeDeclaration> local = type(file.namespace(), name);
        if (local.isPresent()) {
            return new ResolvedType.Declared(file.namespace(), local.get());
        }
        final ImportScope scope = scopes.get(file);
        final SortedSet<String> explicitNamespaces = scope == null
                ? new TreeSet<>()
                : scope.explicit().getOrDefault(name, new TreeSet<>());
        final ResolvedType explicit = resolveAmong(name, explicitNamespaces);
        if (explicit != null) {
            return explicit;
        }
        final ResolvedType wildcard = scope == null ? null : resolveAmong(name, scope.wildcard());
        if (wildcard != null) {
            return wildcard;
        }
        if (!explicitNamespaces.isEmpty()) {
            return new ResolvedType.Unresolved(Rule.TYPE_UNKNOWN, "Type '" + name + "' is imported from '"
                    + String.join("', '", explicitNamespaces) + "' but not declared there");
        }
        final List<String> candidates = namespacesDeclaring(name);
        if (!candidates.isEmpty()) {
            return new ResolvedType.Unresolved(Rule.TYPE_NOT_IMPORTED, "Type '" + name
                    + "' is not imported; add 'requires {" + name + "} from " + candidates.getFirst() + "'"
                    + (candidates.size() > 1 ? " (also declared in " + String.join(", ",
                    candidates.subList(1, candidates.size())) + ")" : ""));
        }
        if (name.equals("int") || name.equals("uint")) {
            return new ResolvedType.Unresolved(Rule.TYPE_UNKNOWN,
                    "Unknown type '" + name + "'; integer types need an explicit width, e.g. '" + name + "32'");
        }
        return new ResolvedType.Unresolved(Rule.TYPE_UNKNOWN, "Unknown type '" + name + "'");
    }

    private ResolvedType resolveAmong(final String name, final Collection<String> candidateNamespaces) {
        final List<String> matches = candidateNamespaces.stream()
                .filter(ns -> type(ns, name).isPresent())
                .sorted()
                .toList();
        if (matches.size() == 1) {
            return new ResolvedType.Declared(matches.getFirst(), type(matches.getFirst(), name).orElseThrow());
        }
        if (matches.size() > 1) {
            return new ResolvedType.Unresolved(Rule.TYPE_AMBIGUOUS, "Type '" + name + "' is imported from "
                    + String.join(" and ", matches) + "; use a qualified name");
        }
        return null;
    }

    /**
     * Returns the direct super types of a type declaration that could be resolved to declared types.
     *
     * @param type a type declaration of this model
     * @return the resolved direct super types in declaration order
     */
    public List<ResolvedType.Declared> directSupertypes(final Declaration.TypeDeclaration type) {
        final SchemaFile file = fileOf(type);
        final List<ResolvedType.Declared> result = new ArrayList<>();
        for (final TypeRef supertype : type.supertypes()) {
            if (supertype instanceof TypeRef.Named named
                    && resolve(file, named) instanceof ResolvedType.Declared declared) {
                result.add(declared);
            }
        }
        return result;
    }

    /**
     * Returns all (transitive) super types, nearest first, without duplicates. Cycles are tolerated.
     *
     * @param type a type declaration of this model
     * @return the ancestors in breadth-first order
     */
    public List<Declaration.TypeDeclaration> ancestors(final Declaration.TypeDeclaration type) {
        final List<Declaration.TypeDeclaration> result = new ArrayList<>();
        final Set<Declaration.TypeDeclaration> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        seen.add(type);
        final List<Declaration.TypeDeclaration> queue = new ArrayList<>(List.of(type));
        while (!queue.isEmpty()) {
            final Declaration.TypeDeclaration current = queue.removeFirst();
            for (final ResolvedType.Declared parent : directSupertypes(current)) {
                if (seen.add(parent.declaration())) {
                    result.add(parent.declaration());
                    queue.add(parent.declaration());
                }
            }
        }
        return result;
    }
}
