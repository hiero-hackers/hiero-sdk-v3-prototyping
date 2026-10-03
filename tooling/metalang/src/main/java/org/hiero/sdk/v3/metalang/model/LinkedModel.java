package org.hiero.sdk.v3.metalang.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import org.hiero.sdk.v3.metalang.semantic.SpecModel;

/**
 * The linked (fully resolved) model of a set of specs: every type reference is resolved, the effective members of
 * every type are computed and type arguments of supertypes are substituted into inherited members.
 *
 * <p>This is the input for generators and conformance checks. It can be built for any {@link SpecModel}, also one
 * with validation errors — unresolvable references become {@link Type.UnresolvedType} — but tools that produce output
 * must only run on models without errors.
 */
public final class LinkedModel {

    private final SortedMap<QualifiedName, TypeDefinition> types;
    private final List<FunctionDefinition> functions;
    private final List<ConstantDefinition> constants;
    private final List<NamespaceDefinition> namespaces;

    LinkedModel(final Map<QualifiedName, TypeDefinition> types, final List<FunctionDefinition> functions,
                final List<ConstantDefinition> constants, final List<NamespaceDefinition> namespaces) {
        this.types = Collections.unmodifiableSortedMap(new TreeMap<>(types));
        this.functions = List.copyOf(functions);
        this.constants = List.copyOf(constants);
        this.namespaces = List.copyOf(namespaces);
    }

    /**
     * Returns all namespaces, ordered by name.
     *
     * @return the namespaces
     */
    public List<NamespaceDefinition> namespaces() {
        return namespaces;
    }

    /**
     * Returns the types declared in the given namespace, ordered by name.
     *
     * @param namespace the namespace
     * @return the types
     */
    public List<TypeDefinition> types(final String namespace) {
        return types.values().stream().filter(t -> t.name().namespace().equals(namespace)).toList();
    }

    /**
     * Links the given semantic model.
     *
     * @param model the semantic model
     * @return the linked model
     */
    public static LinkedModel of(final SpecModel model) {
        Objects.requireNonNull(model, "model must not be null");
        return new Linker(model).link();
    }

    /**
     * Returns all type definitions, ordered by qualified name.
     *
     * @return the types
     */
    public List<TypeDefinition> types() {
        return List.copyOf(types.values());
    }

    /**
     * Returns the type definition with the given name.
     *
     * @param name the qualified name
     * @return the definition, if present
     */
    public Optional<TypeDefinition> type(final QualifiedName name) {
        return Optional.ofNullable(types.get(Objects.requireNonNull(name, "name must not be null")));
    }

    /**
     * Returns the type definition with the given namespace and simple name.
     *
     * @param namespace the namespace
     * @param name      the simple name
     * @return the definition, if present
     */
    public Optional<TypeDefinition> type(final String namespace, final String name) {
        return type(new QualifiedName(namespace, name));
    }

    /**
     * Returns the definition a declared type refers to.
     *
     * @param type the declared type
     * @return the definition
     * @throws IllegalArgumentException if the type is not part of this model
     */
    public TypeDefinition definition(final Type.DeclaredType type) {
        return type(type.name()).orElseThrow(() -> new IllegalArgumentException("Unknown type " + type.name()));
    }

    /**
     * Returns all namespace-level functions, ordered by namespace and source order.
     *
     * @return the functions
     */
    public List<FunctionDefinition> functions() {
        return functions;
    }

    /**
     * Returns all constants, ordered by namespace and source order.
     *
     * @return the constants
     */
    public List<ConstantDefinition> constants() {
        return constants;
    }

    /**
     * Returns the attribute a type inherits under the given name, as seen from that type: the attribute of the first
     * direct supertype (in {@code extends} order) that has it, with the supertype's type arguments substituted.
     * Unlike {@link TypeDefinition#field(String)} this ignores the type's own (overriding) declaration.
     *
     * @param type      the inheriting type
     * @param fieldName the attribute name
     * @return the inherited attribute, if any
     */
    public Optional<FieldDefinition> inheritedField(final QualifiedName type, final String fieldName) {
        return type(type).stream()
                .flatMap(d -> d.supertypes().stream())
                .filter(t -> t instanceof Type.DeclaredType d && types.containsKey(d.name()))
                .map(t -> (Type.DeclaredType) t)
                .flatMap(supertype -> definition(supertype).field(fieldName).stream()
                        .map(f -> f.withType(Linker.substitute(f.type(), substitution(supertype)))))
                .findFirst();
    }

    /**
     * Returns a type of a supertype's member as seen from the subtype: the supertype's type variables replaced by
     * the type arguments in {@code supertype}.
     *
     * @param supertype the supertype with its type arguments (as written in {@code extends})
     * @param type      a type used in a member of the supertype
     * @return the substituted type
     */
    public Type substitute(final Type.DeclaredType supertype, final Type type) {
        return Linker.substitute(type, substitution(supertype));
    }

    private java.util.Map<Type.TypeVariable, Type> substitution(final Type.DeclaredType supertype) {
        final List<TypeParameterDefinition> parameters = definition(supertype).typeParameters();
        final java.util.Map<Type.TypeVariable, Type> map = new java.util.HashMap<>();
        if (parameters.size() == supertype.arguments().size()) {
            for (int i = 0; i < parameters.size(); i++) {
                map.put(parameters.get(i).variable(), supertype.arguments().get(i));
            }
        }
        return map;
    }

    /**
     * Returns whether {@code type} is {@code supertype} or (transitively) extends it. Type arguments are ignored.
     *
     * @param type      the potential subtype
     * @param supertype the potential supertype
     * @return {@code true} if {@code type} is a subtype of {@code supertype}
     */
    public boolean isSubtypeOf(final QualifiedName type, final QualifiedName supertype) {
        return isSubtypeOf(type, supertype, new java.util.HashSet<>());
    }

    private boolean isSubtypeOf(final QualifiedName type, final QualifiedName supertype,
                                final java.util.Set<QualifiedName> visited) {
        if (type.equals(supertype)) {
            return true;
        }
        if (!visited.add(type)) {
            return false;
        }
        return type(type).stream()
                .flatMap(d -> d.supertypes().stream())
                .filter(t -> t instanceof Type.DeclaredType)
                .anyMatch(t -> isSubtypeOf(((Type.DeclaredType) t).name(), supertype, visited));
    }
}
