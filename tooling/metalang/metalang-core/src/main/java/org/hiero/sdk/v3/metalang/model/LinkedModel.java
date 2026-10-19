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
    private final List<InstanceDefinition> instances;
    private final List<org.hiero.sdk.v3.metalang.diagnostic.Diagnostic> instanceDiagnostics;
    private final List<TestDefinition> tests;
    private final List<TestValuesDefinition> testValues;
    private final List<org.hiero.sdk.v3.metalang.diagnostic.Diagnostic> testDiagnostics;

    LinkedModel(final Map<QualifiedName, TypeDefinition> types, final List<FunctionDefinition> functions,
                final List<ConstantDefinition> constants, final List<NamespaceDefinition> namespaces,
                final List<InstanceDefinition> instances,
                final List<org.hiero.sdk.v3.metalang.diagnostic.Diagnostic> instanceDiagnostics,
                final List<TestDefinition> tests, final List<TestValuesDefinition> testValues,
                final List<org.hiero.sdk.v3.metalang.diagnostic.Diagnostic> testDiagnostics) {
        this.tests = List.copyOf(tests);
        this.testValues = List.copyOf(testValues);
        this.testDiagnostics = List.copyOf(testDiagnostics);
        this.types = Collections.unmodifiableSortedMap(new TreeMap<>(types));
        this.functions = List.copyOf(functions);
        this.constants = List.copyOf(constants);
        this.namespaces = List.copyOf(namespaces);
        this.instances = List.copyOf(instances);
        this.instanceDiagnostics = List.copyOf(instanceDiagnostics);
    }

    /**
     * Returns the default instances of the specs ({@code ## Default Instances for Testing}) that could be resolved.
     *
     * @return the default instances, ordered by spec file and source order
     */
    public List<InstanceDefinition> instances() {
        return instances;
    }

    /**
     * Returns the default instance of a type.
     *
     * @param type the type
     * @return the default instance, if the specs define one
     */
    public Optional<InstanceDefinition> instance(final QualifiedName type) {
        return instances.stream().filter(i -> i.type().name().equals(type)).findFirst();
    }

    /**
     * Returns the problems found while resolving the default instances (reported by the validator).
     *
     * @return the diagnostics
     */
    public List<org.hiero.sdk.v3.metalang.diagnostic.Diagnostic> instanceDiagnostics() {
        return instanceDiagnostics;
    }

    /**
     * Returns the tests of the specs ({@code tests} blocks of {@code ## Testing}) that could be resolved.
     *
     * @return the tests, ordered by spec file and source order
     */
    public List<TestDefinition> tests() {
        return tests;
    }

    /**
     * Returns the named test values of the specs ({@code values} in {@code ## Default Instances for Testing}) that could
     * be resolved.
     *
     * @return the named test values, ordered by spec file and source order
     */
    public List<TestValuesDefinition> testValues() {
        return testValues;
    }

    /**
     * Returns the problems found while resolving the tests (reported by the validator).
     *
     * @return the diagnostics
     */
    public List<org.hiero.sdk.v3.metalang.diagnostic.Diagnostic> testDiagnostics() {
        return testDiagnostics;
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

    /**
     * Replaces type variables in a type.
     *
     * @param type      the type
     * @param variables the replacements; type variables without replacement are kept
     * @return the substituted type
     */
    public static Type substitute(final Type type, final java.util.Map<Type.TypeVariable, Type> variables) {
        return Linker.substitute(type, variables);
    }

    /**
     * Returns a type as one of its supertypes, with the type arguments the supertype gets from it:
     * {@code AccountCreateTransaction} as {@code Transaction} is
     * {@code Transaction<AccountCreateReceipt, AccountCreateTransaction>}.
     *
     * @param type      the type
     * @param supertype the name of the supertype
     * @return the supertype with its type arguments, empty if {@code type} does not extend it
     */
    public Optional<Type.DeclaredType> asSupertype(final Type.DeclaredType type, final QualifiedName supertype) {
        return asSupertype(type, supertype, new java.util.HashSet<>());
    }

    private Optional<Type.DeclaredType> asSupertype(final Type.DeclaredType type, final QualifiedName supertype,
                                                    final java.util.Set<QualifiedName> visited) {
        if (type.name().equals(supertype)) {
            return Optional.of(type);
        }
        if (!visited.add(type.name()) || type(type.name()).isEmpty()) {
            return Optional.empty();
        }
        final java.util.Map<Type.TypeVariable, Type> arguments = substitution(type);
        for (final Type extended : definition(type).supertypes()) {
            if (Linker.substitute(extended, arguments) instanceof Type.DeclaredType declared) {
                final Optional<Type.DeclaredType> found = asSupertype(declared, supertype, visited);
                if (found.isPresent()) {
                    return found;
                }
            }
        }
        return Optional.empty();
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
