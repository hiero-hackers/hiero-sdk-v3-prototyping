package org.hiero.sdk.v3.metalang.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.hiero.sdk.v3.metalang.ast.Declaration;
import org.hiero.sdk.v3.metalang.ast.EnumValue;
import org.hiero.sdk.v3.metalang.ast.Field;
import org.hiero.sdk.v3.metalang.ast.Method;
import org.hiero.sdk.v3.metalang.ast.Parameter;
import org.hiero.sdk.v3.metalang.ast.SchemaFile;
import org.hiero.sdk.v3.metalang.ast.TypeParameter;
import org.hiero.sdk.v3.metalang.ast.TypeRef;
import org.hiero.sdk.v3.metalang.semantic.ResolvedType;
import org.hiero.sdk.v3.metalang.semantic.SpecModel;

/**
 * Builds a {@link LinkedModel}: resolves all type references, then computes the effective members of every type.
 * Never throws on erroneous input; unresolvable references become {@link Type.UnresolvedType} and inheritance cycles
 * contribute no inherited members.
 */
final class Linker {

    private final SpecModel model;
    private final Map<QualifiedName, Declaration.TypeDeclaration> declarations = new TreeMap<>();
    private final Map<QualifiedName, SchemaFile> files = new HashMap<>();
    private final Map<QualifiedName, Shell> shells = new TreeMap<>();
    private final Map<QualifiedName, List<FieldDefinition>> effectiveFields = new HashMap<>();
    private final Map<QualifiedName, List<MethodDefinition>> effectiveMethods = new HashMap<>();
    private final Set<QualifiedName> fieldsInProgress = new HashSet<>();
    private final Set<QualifiedName> methodsInProgress = new HashSet<>();

    /** The declared (not yet effective) parts of a type. */
    private record Shell(Declaration.TypeDeclaration declaration, List<TypeParameterDefinition> typeParameters,
                         List<Type> supertypes, List<FieldDefinition> declaredFields,
                         List<MethodDefinition> declaredMethods, List<ParameterDefinition> enumAttributes) {
    }

    Linker(final SpecModel model) {
        this.model = model;
    }

    LinkedModel link() {
        for (final SchemaFile file : model.files()) {
            for (final Declaration.TypeDeclaration type : file.types()) {
                final QualifiedName name = new QualifiedName(file.namespace(), type.name());
                if (declarations.putIfAbsent(name, type) == null) {
                    files.put(name, file);
                }
            }
        }
        declarations.forEach((name, declaration) -> shells.put(name, shell(name, declaration, files.get(name))));

        final Map<QualifiedName, TypeDefinition> types = new LinkedHashMap<>();
        shells.forEach((name, shell) -> types.put(name, definition(name, shell)));

        final List<FunctionDefinition> functions = new ArrayList<>();
        final List<ConstantDefinition> constants = new ArrayList<>();
        for (final SchemaFile file : model.files()) {
            for (final Declaration declaration : file.declarations()) {
                switch (declaration) {
                    case Declaration.Function function -> functions.add(new FunctionDefinition(file.namespace(),
                            method(function.method(), null, file.namespace(), Map.of(), file)));
                    case Declaration.Constant constant -> constants.add(new ConstantDefinition(
                            new QualifiedName(file.namespace(), constant.name()),
                            type(constant.type(), file, Map.of()), constant.value(), constant.annotations(),
                            constant.documentation(), constant.location()));
                    case Declaration.TypeDeclaration ignored -> {
                        // linked above
                    }
                }
            }
        }
        return new LinkedModel(types, functions, constants, namespaces(types, functions, constants));
    }

    // --- namespaces ------------------------------------------------------------------------------

    private List<NamespaceDefinition> namespaces(final Map<QualifiedName, TypeDefinition> types,
                                                 final List<FunctionDefinition> functions,
                                                 final List<ConstantDefinition> constants) {
        final Map<String, Set<String>> required = new TreeMap<>();
        for (final String namespace : model.namespaceNames()) {
            final Set<String> namespaces = required.computeIfAbsent(namespace, k -> new java.util.TreeSet<>());
            for (final SchemaFile file : model.filesOf(namespace)) {
                file.requires().stream().map(r -> r.namespace()).filter(model::hasNamespace)
                        .forEach(namespaces::add);
            }
        }
        final java.util.function.BiConsumer<String, Type> use = (namespace, type) ->
                referencedNamespaces(type, required.get(namespace));
        for (final TypeDefinition type : types.values()) {
            final String namespace = type.name().namespace();
            type.supertypes().forEach(t -> use.accept(namespace, t));
            type.typeParameters().stream().filter(p -> p.bound() != null).forEach(p -> use.accept(namespace, p.bound()));
            // only what the type declares itself: inherited members are reachable through the supertype's module
            switch (type) {
                case TypeDefinition.ComplexTypeDefinition complex ->
                        complex.declaredFields().forEach(f -> use.accept(namespace, f.type()));
                case TypeDefinition.EnumDefinition enumType ->
                        enumType.attributes().forEach(a -> use.accept(namespace, a.type()));
            }
            type.declaredMethods().forEach(m -> methodTypes(m).forEach(t -> use.accept(namespace, t)));
        }
        functions.forEach(f -> methodTypes(f.method()).forEach(t -> use.accept(f.namespace(), t)));
        constants.forEach(c -> use.accept(c.name().namespace(), c.type()));

        final List<NamespaceDefinition> result = new ArrayList<>();
        required.forEach((namespace, namespaces) -> {
            namespaces.remove(namespace);
            result.add(new NamespaceDefinition(namespace, model.filesOf(namespace).stream()
                    .map(f -> new NamespaceDefinition.Source(f.file(), f.description()))
                    .toList(), List.copyOf(namespaces)));
        });
        return result;
    }

    private static List<Type> methodTypes(final MethodDefinition method) {
        final List<Type> result = new ArrayList<>();
        result.add(method.returnType());
        method.parameters().forEach(p -> result.add(p.type()));
        method.typeParameters().stream().filter(p -> p.bound() != null).forEach(p -> result.add(p.bound()));
        return result;
    }

    private static void referencedNamespaces(final Type type, final Set<String> out) {
        switch (type) {
            case Type.DeclaredType declared -> {
                out.add(declared.name().namespace());
                declared.arguments().forEach(a -> referencedNamespaces(a, out));
            }
            case Type.BasicType basic -> basic.arguments().forEach(a -> referencedNamespaces(a, out));
            case Type.WildcardType wildcard -> {
                if (wildcard.upperBound() != null) {
                    referencedNamespaces(wildcard.upperBound(), out);
                }
            }
            case Type.FunctionType function -> {
                referencedNamespaces(function.returnType(), out);
                function.parameters().forEach(p -> referencedNamespaces(p.type(), out));
            }
            default -> {
                // no namespaces
            }
        }
    }

    // --- declared parts --------------------------------------------------------------------------

    private Shell shell(final QualifiedName name, final Declaration.TypeDeclaration declaration, final SchemaFile file) {
        final Map<String, String> scope = new HashMap<>();
        declaration.typeParameters().forEach(p -> scope.put(p.name(), name.toString()));
        final List<TypeParameterDefinition> typeParameters = typeParameters(declaration.typeParameters(),
                name.toString(), file, scope);
        final List<Type> supertypes = declaration.supertypes().stream().map(t -> type(t, file, scope)).toList();
        final List<FieldDefinition> fields = declaration.fields().stream()
                .map(f -> field(f, name, file, scope))
                .toList();
        final List<MethodDefinition> methods = declaration.methods().stream()
                .map(m -> method(m, name, name.toString(), scope, file))
                .toList();
        final List<ParameterDefinition> attributes = declaration instanceof Declaration.EnumType enumType
                ? enumType.attributes().stream().map(a -> parameter(a, file, scope)).toList()
                : List.of();
        return new Shell(declaration, typeParameters, supertypes, fields, methods, attributes);
    }

    private List<TypeParameterDefinition> typeParameters(final List<TypeParameter> parameters, final String owner,
                                                         final SchemaFile file, final Map<String, String> scope) {
        return parameters.stream()
                .map(p -> new TypeParameterDefinition(new Type.TypeVariable(p.name(), owner),
                        p.bound() == null ? null : type(p.bound(), file, scope)))
                .toList();
    }

    private FieldDefinition field(final Field field, final QualifiedName owner, final SchemaFile file,
                                  final Map<String, String> scope) {
        return new FieldDefinition(field.name(), type(field.type(), file, scope), owner, field.annotations(),
                field.documentation(), field.location());
    }

    private MethodDefinition method(final Method method, final QualifiedName declaringType, final String ownerPrefix,
                                    final Map<String, String> outerScope, final SchemaFile file) {
        final String owner = ownerPrefix + "#" + method.signature();
        final Map<String, String> scope = new HashMap<>(outerScope);
        method.typeParameters().forEach(p -> scope.put(p.name(), owner));
        // non-standard use-site bounds (reported by the validator) declare the name for the method
        final Set<String> useSite = new HashSet<>();
        collectBoundedGenerics(method.returnType(), useSite);
        method.parameters().forEach(p -> collectBoundedGenerics(p.type(), useSite));
        useSite.forEach(n -> scope.putIfAbsent(n, owner));
        return new MethodDefinition(method.name(), typeParameters(method.typeParameters(), owner, file, scope),
                type(method.returnType(), file, scope),
                method.parameters().stream().map(p -> parameter(p, file, scope)).toList(),
                declaringType, method.annotations(), method.documentation(), method.location());
    }

    private ParameterDefinition parameter(final Parameter parameter, final SchemaFile file,
                                          final Map<String, String> scope) {
        return new ParameterDefinition(parameter.name(), type(parameter.type(), file, scope), parameter.varargs(),
                parameter.annotations(), parameter.location());
    }

    private static void collectBoundedGenerics(final TypeRef type, final Set<String> names) {
        if (type instanceof TypeRef.Named named) {
            for (final TypeRef.TypeArgument argument : named.arguments()) {
                switch (argument) {
                    case TypeRef.BoundedGeneric bounded -> {
                        names.add(bounded.name());
                        collectBoundedGenerics(bounded.bound(), names);
                    }
                    case TypeRef.Concrete concrete -> collectBoundedGenerics(concrete.type(), names);
                    case TypeRef.Wildcard wildcard -> {
                        if (wildcard.upperBound() != null) {
                            collectBoundedGenerics(wildcard.upperBound(), names);
                        }
                    }
                }
            }
        } else if (type instanceof TypeRef.Function function) {
            collectBoundedGenerics(function.returnType(), names);
            function.parameters().forEach(p -> collectBoundedGenerics(p.type(), names));
        }
    }

    // --- type references -------------------------------------------------------------------------

    private Type type(final TypeRef ref, final SchemaFile file, final Map<String, String> scope) {
        return switch (ref) {
            case TypeRef.Named named -> {
                final List<Type> arguments = named.arguments().stream()
                        .map(a -> argument(a, file, scope))
                        .toList();
                yield switch (model.resolve(file, named)) {
                    case ResolvedType.Builtin builtin -> new Type.BasicType(builtin.type(), arguments);
                    case ResolvedType.Declared declared -> new Type.DeclaredType(
                            new QualifiedName(declared.namespace(), declared.declaration().name()), arguments);
                    case ResolvedType.Unresolved ignored -> new Type.UnresolvedType(named.text());
                };
            }
            case TypeRef.GenericParameter generic -> variable(generic.name(), scope);
            case TypeRef.Any ignored -> new Type.AnyType();
            case TypeRef.Void ignored -> new Type.VoidType();
            case TypeRef.Function function -> new Type.FunctionType(type(function.returnType(), file, scope),
                    function.name(), function.parameters().stream().map(p -> parameter(p, file, scope)).toList());
        };
    }

    private Type argument(final TypeRef.TypeArgument argument, final SchemaFile file,
                          final Map<String, String> scope) {
        return switch (argument) {
            case TypeRef.Concrete concrete -> type(concrete.type(), file, scope);
            case TypeRef.Wildcard wildcard -> new Type.WildcardType(
                    wildcard.upperBound() == null ? null : type(wildcard.upperBound(), file, scope));
            case TypeRef.BoundedGeneric bounded -> variable(bounded.name(), scope);
        };
    }

    private static Type variable(final String name, final Map<String, String> scope) {
        final String owner = scope.get(name);
        return owner == null ? new Type.UnresolvedType(name) : new Type.TypeVariable(name, owner);
    }

    // --- effective members -----------------------------------------------------------------------

    private TypeDefinition definition(final QualifiedName name, final Shell shell) {
        final Declaration.TypeDeclaration declaration = shell.declaration();
        return switch (declaration) {
            case Declaration.ComplexType complex -> new TypeDefinition.ComplexTypeDefinition(name,
                    complex.abstraction(), shell.typeParameters(), shell.supertypes(), shell.declaredFields(),
                    shell.declaredMethods(), effectiveFields(name), effectiveMethods(name), complex.annotations(),
                    complex.documentation(), complex.location());
            case Declaration.EnumType enumType -> new TypeDefinition.EnumDefinition(name, shell.enumAttributes(),
                    enumType.values().stream().map(Linker::enumValue).toList(), shell.supertypes(),
                    shell.declaredMethods(), effectiveMethods(name), enumType.annotations(),
                    enumType.documentation(), enumType.location());
        };
    }

    private static EnumValueDefinition enumValue(final EnumValue value) {
        return new EnumValueDefinition(value.name(), value.arguments(), value.annotations(), value.documentation(),
                value.location());
    }

    /** Maps the type parameters of a supertype to the type arguments given in the {@code extends} clause. */
    private Map<Type.TypeVariable, Type> substitution(final Type.DeclaredType supertype) {
        final Shell parent = shells.get(supertype.name());
        final Map<Type.TypeVariable, Type> map = new HashMap<>();
        if (parent != null && parent.typeParameters().size() == supertype.arguments().size()) {
            for (int i = 0; i < parent.typeParameters().size(); i++) {
                map.put(parent.typeParameters().get(i).variable(), supertype.arguments().get(i));
            }
        }
        return map;
    }

    private List<Type.DeclaredType> declaredSupertypes(final Shell shell) {
        return shell.supertypes().stream()
                .filter(t -> t instanceof Type.DeclaredType d && shells.containsKey(d.name()))
                .map(t -> (Type.DeclaredType) t)
                .toList();
    }

    private List<FieldDefinition> effectiveFields(final QualifiedName name) {
        final List<FieldDefinition> cached = effectiveFields.get(name);
        if (cached != null) {
            return cached;
        }
        final Shell shell = shells.get(name);
        if (shell.declaration() instanceof Declaration.EnumType || !fieldsInProgress.add(name)) {
            return List.of(); // enums cannot be extended; a cycle contributes nothing
        }
        final List<FieldDefinition> result = new ArrayList<>();
        for (final Type.DeclaredType supertype : declaredSupertypes(shell)) {
            final Map<Type.TypeVariable, Type> map = substitution(supertype);
            for (final FieldDefinition inherited : effectiveFields(supertype.name())) {
                if (result.stream().noneMatch(f -> f.name().equals(inherited.name()))) {
                    result.add(inherited.withType(substitute(inherited.type(), map)));
                }
            }
        }
        for (final FieldDefinition own : shell.declaredFields()) {
            final int index = indexOf(result, own.name());
            if (index >= 0) {
                result.set(index, own);
            } else {
                result.add(own);
            }
        }
        fieldsInProgress.remove(name);
        effectiveFields.put(name, List.copyOf(result));
        return effectiveFields.get(name);
    }

    private static int indexOf(final List<FieldDefinition> fields, final String name) {
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i).name().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    private List<MethodDefinition> effectiveMethods(final QualifiedName name) {
        final List<MethodDefinition> cached = effectiveMethods.get(name);
        if (cached != null) {
            return cached;
        }
        if (!methodsInProgress.add(name)) {
            return List.of(); // inheritance cycle
        }
        final Shell shell = shells.get(name);
        final Map<String, MethodDefinition> bySignature = new LinkedHashMap<>();
        for (final Type.DeclaredType supertype : declaredSupertypes(shell)) {
            final Map<Type.TypeVariable, Type> map = substitution(supertype);
            for (final MethodDefinition inherited : effectiveMethods(supertype.name())) {
                if (!inherited.isStatic()) {
                    final MethodDefinition substituted = substitute(inherited, map);
                    bySignature.putIfAbsent(substituted.signature(), substituted);
                }
            }
        }
        for (final MethodDefinition own : shell.declaredMethods()) {
            bySignature.put(own.signature(), own); // replaces an inherited method in place
        }
        methodsInProgress.remove(name);
        effectiveMethods.put(name, List.copyOf(bySignature.values()));
        return effectiveMethods.get(name);
    }

    // --- substitution ----------------------------------------------------------------------------

    private static MethodDefinition substitute(final MethodDefinition method, final Map<Type.TypeVariable, Type> map) {
        if (map.isEmpty()) {
            return method;
        }
        return new MethodDefinition(method.name(),
                method.typeParameters().stream()
                        .map(p -> new TypeParameterDefinition(p.variable(),
                                p.bound() == null ? null : substitute(p.bound(), map)))
                        .toList(),
                substitute(method.returnType(), map),
                method.parameters().stream().map(p -> p.withType(substitute(p.type(), map))).toList(),
                method.declaringType(), method.annotations(), method.documentation(), method.location());
    }

    static Type substitute(final Type type, final Map<Type.TypeVariable, Type> map) {
        if (map.isEmpty()) {
            return type;
        }
        return switch (type) {
            case Type.TypeVariable variable -> map.getOrDefault(variable, variable);
            case Type.BasicType basic -> new Type.BasicType(basic.builtin(),
                    basic.arguments().stream().map(a -> substitute(a, map)).toList());
            case Type.DeclaredType declared -> new Type.DeclaredType(declared.name(),
                    declared.arguments().stream().map(a -> substitute(a, map)).toList());
            case Type.WildcardType wildcard -> new Type.WildcardType(
                    wildcard.upperBound() == null ? null : substitute(wildcard.upperBound(), map));
            case Type.FunctionType function -> new Type.FunctionType(substitute(function.returnType(), map),
                    function.name(), function.parameters().stream()
                    .map(p -> p.withType(substitute(p.type(), map))).toList());
            case Type.AnyType any -> any;
            case Type.VoidType ignored -> type;
            case Type.UnresolvedType ignored -> type;
        };
    }
}
