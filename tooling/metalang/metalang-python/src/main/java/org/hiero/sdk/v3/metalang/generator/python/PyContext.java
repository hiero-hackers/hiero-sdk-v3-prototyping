package org.hiero.sdk.v3.metalang.generator.python;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.hiero.sdk.v3.metalang.model.FieldDefinition;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.hiero.sdk.v3.metalang.model.ParameterDefinition;
import org.hiero.sdk.v3.metalang.model.QualifiedName;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.model.TypeDefinition;
import org.hiero.sdk.v3.metalang.model.TypeParameterDefinition;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;

/**
 * What the generators of single declarations need to know about the whole model: the Python form of every type, the
 * declarations that cannot be generated, which concrete types are frozen and the class a concrete type extends.
 */
final class PyContext {

    private final LinkedModel model;
    private final PythonGeneratorConfig config;
    private final Set<String> declaredNames;
    private Map<QualifiedName, String> gaps;
    private Map<QualifiedName, Boolean> frozen;

    /**
     * Creates the context.
     *
     * @param model  the linked model
     * @param config the configuration
     */
    PyContext(final LinkedModel model, final PythonGeneratorConfig config) {
        this.model = Objects.requireNonNull(model, "model must not be null");
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.declaredNames = model.types().stream().map(t -> t.name().name()).collect(Collectors.toSet());
    }

    LinkedModel model() {
        return model;
    }

    PythonGeneratorConfig config() {
        return config;
    }

    /**
     * The dotted path of the private module that declares a type.
     *
     * @param name the type
     * @return e.g. {@code hiero_sdk.ledger._account_id}
     */
    String module(final QualifiedName name) {
        return config.modulePath(name.namespace()) + "." + PyNames.module(name.name());
    }

    // --- types ------------------------------------------------------------------------------------------------

    /**
     * The Python form of a type in an annotation.
     *
     * @param type    the meta-language type
     * @param imports the imports of the module
     * @return the annotation
     * @throws PyGap if the type has no Python form yet
     */
    String type(final Type type, final PyImports imports) {
        return type(type, imports, Mode.ANNOTATION);
    }

    /**
     * How a type is rendered: in an annotation, in an expression the class statement evaluates (the arguments of a
     * base class need runtime imports), or as the bound of a type parameter (which may not mention a type variable).
     */
    enum Mode {
        /** an annotation, never evaluated at runtime */
        ANNOTATION,
        /** evaluated when the class is created */
        RUNTIME,
        /** a bound: type variables are erased to {@code Any} */
        BOUND
    }

    /**
     * The Python form of a type.
     *
     * @param type    the meta-language type
     * @param imports the imports of the module
     * @param mode    how the type is used
     * @return the type expression
     * @throws PyGap if the type has no Python form yet
     */
    String type(final Type type, final PyImports imports, final Mode mode) {
        return switch (type) {
            case Type.BasicType basic -> basic(basic, imports, mode);
            case Type.DeclaredType declared -> declared(declared, imports, mode);
            case Type.TypeVariable variable -> mode == Mode.BOUND ? imports.standard("typing", "Any")
                    : variable(variable.name(), imports);
            case Type.WildcardType wildcard -> wildcard.upperBound() == null ? imports.standard("typing", "Any")
                    : type(wildcard.upperBound(), imports, mode);
            case Type.AnyType ignored -> "object";
            case Type.VoidType ignored -> "None";
            case Type.FunctionType function -> imports.standard("collections.abc", "Callable") + "[["
                    + function.parameters().stream().map(ParameterDefinition::type).map(t -> type(t, imports, mode))
                    .collect(Collectors.joining(", ")) + "], " + type(function.returnType(), imports, mode) + "]";
            case Type.UnresolvedType unresolved ->
                    throw new PyGap("the type '" + unresolved.reference() + "' is not resolved");
        };
    }

    private String basic(final Type.BasicType basic, final PyImports imports, final Mode mode) {
        final BuiltinType builtin = basic.builtin();
        return switch (builtin.category()) {
            case INTEGER -> "int";
            case FLOAT -> "float";
            case DECIMAL -> imports.module("decimal") + ".Decimal";
            case STRING -> "str";
            case BOOL -> "bool";
            case BYTES -> "bytes";
            case COLLECTION -> "set".equals(builtin.name())
                    ? "frozenset[" + type(basic.arguments().getFirst(), imports, mode) + "]"
                    : "tuple[" + type(basic.arguments().getFirst(), imports, mode) + ", ...]";
            case MAP -> imports.standard("collections.abc", "Mapping") + "[" + type(basic.arguments().get(0), imports, mode)
                    + ", " + type(basic.arguments().get(1), imports, mode) + "]";
            case TYPE -> "type[" + (basic.arguments().isEmpty() ? "object"
                    : type(basic.arguments().getFirst(), imports, mode)) + "]";
            case UUID -> imports.module("uuid") + ".UUID";
            case TEMPORAL -> imports.module("datetime") + "." + switch (builtin.name()) {
                case "date" -> "date";
                case "time" -> "time";
                default -> "datetime";
            };
            case DURATION -> imports.module("datetime") + ".timedelta";
            case STREAM_RESULT -> throw new PyGap("'streamResult' maps to StreamItem of the support package, which "
                    + "does not exist yet");
        };
    }

    /**
     * The Python form of a declared type; the self type parameter takes no argument and a missing argument is
     * {@code Any}.
     *
     * @param declared  the type
     * @param imports   the imports of the module
     * @param mode      how the type is used
     * @return the annotation
     */
    String declared(final Type.DeclaredType declared, final PyImports imports, final Mode mode) {
        final TypeDefinition definition = model.type(declared.name())
                .orElseThrow(() -> new PyGap("the type " + declared.name() + " is not declared"));
        final String name = imports.sdk(module(declared.name()), declared.name().name(), mode == Mode.RUNTIME);
        final List<TypeParameterDefinition> parameters = definition.typeParameters();
        final List<String> arguments = new ArrayList<>();
        for (int i = 0; i < parameters.size(); i++) {
            if (isSelfParameter(parameters.get(i))) {
                continue; // the Python declaration does not have this parameter
            }
            arguments.add(i < declared.arguments().size() ? argument(declared.arguments().get(i), imports, mode)
                    : imports.standard("typing", "Any"));
        }
        return arguments.isEmpty() ? name : name + "[" + String.join(", ", arguments) + "]";
    }

    /** A type argument: a wildcard is {@code Any}, which fits invariant and covariant parameters alike. */
    private String argument(final Type argument, final PyImports imports, final Mode mode) {
        return argument instanceof Type.WildcardType || argument instanceof Type.AnyType
                ? imports.standard("typing", "Any") : type(argument, imports, mode);
    }

    /** A type variable: a dropped self type is {@code Any}, every other one keeps its (possibly suffixed) name. */
    private String variable(final String metaName, final PyImports imports) {
        if (selfParameterNames().contains(metaName)) {
            return imports.standard("typing", "Any");
        }
        return parameterName(metaName);
    }

    private Set<String> selfParameterNames() {
        final Set<String> names = new HashSet<>();
        model.types().forEach(t -> t.typeParameters().stream().filter(PyContext::isSelfParameter)
                .forEach(p -> names.add(p.name())));
        return names;
    }

    /**
     * The Python name of a type parameter: {@code $$Receipt} is {@code Receipt}, or {@code ReceiptT} when a declared
     * type has that name — PEP 695 would otherwise shadow it in the class header.
     *
     * @param metaName the name with its {@code $$} prefix
     * @return the Python name
     */
    String parameterName(final String metaName) {
        final String name = metaName.startsWith("$$") ? metaName.substring(2) : metaName;
        return declaredNames.contains(name) ? name + "T" : name;
    }

    /**
     * The type parameters a Python declaration has: all but the self type.
     *
     * @param definition the declaration
     * @return the parameters
     */
    static List<TypeParameterDefinition> pythonParameters(final TypeDefinition definition) {
        return definition.typeParameters().stream().filter(p -> !isSelfParameter(p)).toList();
    }

    /**
     * Whether a type parameter is F-bounded by itself ({@code $$Self extends Transaction<$$Self, ...>}).
     *
     * @param parameter the parameter
     * @return {@code true} for a self type
     */
    static boolean isSelfParameter(final TypeParameterDefinition parameter) {
        return parameter.bound() != null && mentions(parameter.bound(), parameter.variable());
    }

    /**
     * Whether a type mentions any type variable — such a bound is widened, because a Python bound cannot.
     *
     * @param type the type
     * @return {@code true} if a type variable occurs in it
     */
    static boolean mentionsVariable(final Type type) {
        return mentions(type, null);
    }

    private static boolean mentions(final Type type, final Type.TypeVariable variable) {
        if (type instanceof Type.TypeVariable found) {
            return variable == null || found.equals(variable);
        }
        return arguments(type).stream().anyMatch(argument -> mentions(argument, variable));
    }

    private static List<Type> arguments(final Type type) {
        return switch (type) {
            case Type.DeclaredType declared -> declared.arguments();
            case Type.BasicType basic -> basic.arguments();
            case Type.WildcardType wildcard -> wildcard.upperBound() == null ? List.of()
                    : List.of(wildcard.upperBound());
            case Type.FunctionType function -> java.util.stream.Stream.concat(
                    java.util.stream.Stream.of(function.returnType()),
                    function.parameters().stream().map(ParameterDefinition::type)).toList();
            default -> List.of();
        };
    }

    // --- shape ------------------------------------------------------------------------------------------------

    /**
     * Whether a declaration is an abstraction (a protocol).
     *
     * @param definition the declaration
     * @return {@code true} for an abstraction
     */
    static boolean isAbstraction(final TypeDefinition definition) {
        return definition instanceof TypeDefinition.ComplexTypeDefinition complex && complex.abstraction();
    }

    /**
     * The supertypes of a declaration that are abstractions, or that are concrete types, in declaration order.
     *
     * @param definition  the declaration
     * @param abstraction {@code true} for the abstractions, {@code false} for the concrete types
     * @return the supertypes
     */
    List<Type.DeclaredType> supertypes(final TypeDefinition definition, final boolean abstraction) {
        final List<Type.DeclaredType> result = new ArrayList<>();
        for (final Type supertype : definition.supertypes()) {
            if (supertype instanceof Type.DeclaredType declared && model.type(declared.name())
                    .map(d -> isAbstraction(d) == abstraction).orElse(false)) {
                result.add(declared);
            }
        }
        return List.copyOf(result);
    }

    /**
     * The concrete type a concrete type extends, if any.
     *
     * @param definition the concrete type
     * @return the superclass
     * @throws PyGap if it extends more than one concrete type
     */
    Optional<Type.DeclaredType> superclass(final TypeDefinition definition) {
        final List<Type.DeclaredType> classes = supertypes(definition, false);
        if (classes.size() > 1) {
            throw new PyGap("it extends more than one concrete type, which a dataclass cannot");
        }
        return classes.stream().findFirst();
    }

    /**
     * Whether a concrete type is a frozen dataclass: none of the concrete types connected to it by inheritance has a
     * mutable attribute, because a frozen dataclass can neither inherit from a non-frozen one nor the other way round.
     *
     * @param name the concrete type
     * @return {@code true} if frozen
     */
    boolean isFrozen(final QualifiedName name) {
        if (frozen == null) {
            frozen = computeFrozen();
        }
        return frozen.getOrDefault(name, true);
    }

    private Map<QualifiedName, Boolean> computeFrozen() {
        final Map<QualifiedName, Set<QualifiedName>> edges = new HashMap<>();
        for (final TypeDefinition definition : model.types()) {
            if (definition instanceof TypeDefinition.ComplexTypeDefinition complex && !complex.abstraction()) {
                edges.computeIfAbsent(definition.name(), k -> new HashSet<>());
                for (final Type.DeclaredType superclass : supertypes(definition, false)) {
                    edges.computeIfAbsent(definition.name(), k -> new HashSet<>()).add(superclass.name());
                    edges.computeIfAbsent(superclass.name(), k -> new HashSet<>()).add(definition.name());
                }
            }
        }
        final Map<QualifiedName, Boolean> result = new HashMap<>();
        for (final QualifiedName start : edges.keySet()) {
            if (result.containsKey(start)) {
                continue;
            }
            final Set<QualifiedName> component = new HashSet<>();
            final Deque<QualifiedName> open = new ArrayDeque<>(List.of(start));
            while (!open.isEmpty()) {
                final QualifiedName next = open.pop();
                if (component.add(next)) {
                    open.addAll(edges.getOrDefault(next, Set.of()));
                }
            }
            final boolean immutable = component.stream().map(model::type).flatMap(Optional::stream)
                    .flatMap(d -> d.fields().stream()).allMatch(f -> f.hasAnnotation("immutable"));
            component.forEach(member -> result.put(member, immutable));
        }
        return result;
    }

    // --- gaps -------------------------------------------------------------------------------------------------

    /**
     * The declarations that cannot be generated, with the reason. A declaration is in here when its own shape has no
     * Python form, and also when it references such a declaration — the gap propagates to a fixed point, so the
     * generated package never imports a module that does not exist.
     *
     * @return the gaps
     */
    Map<QualifiedName, String> gaps() {
        if (gaps != null) {
            return gaps;
        }
        final Map<QualifiedName, String> found = new LinkedHashMap<>();
        final Set<String> modules = new HashSet<>();
        for (final TypeDefinition definition : model.types()) {
            try {
                check(definition, modules);
            } catch (final PyGap gap) {
                found.put(definition.name(), gap.getMessage());
            }
        }
        boolean grew = true;
        while (grew) {
            grew = false;
            for (final TypeDefinition definition : model.types()) {
                if (found.containsKey(definition.name())) {
                    continue;
                }
                final QualifiedName cause = referenced(definition).stream().filter(found::containsKey)
                        .findFirst().orElse(null);
                if (cause != null) {
                    found.put(definition.name(), "it references " + cause + ", which cannot be generated");
                    grew = true;
                }
            }
        }
        gaps = Collections.unmodifiableMap(found);
        return gaps;
    }

    /** Renders everything a declaration needs once, so that a missing Python form shows up as a gap. */
    private void check(final TypeDefinition definition, final Set<String> modules) {
        if (!modules.add(module(definition.name()))) {
            throw new PyGap("another type of the namespace has the same module name " + module(definition.name()));
        }
        final PyImports scratch = new PyImports(module(definition.name()), Set.of(definition.name().name()));
        pythonParameters(definition).stream().map(TypeParameterDefinition::bound).filter(Objects::nonNull)
                .forEach(t -> type(t, scratch));
        definition.supertypes().forEach(t -> type(t, scratch));
        definition.fields().stream().map(FieldDefinition::type).forEach(t -> type(t, scratch));
        if (isAbstraction(definition) && !supertypes(definition, false).isEmpty()) {
            throw new PyGap("an abstraction extends a concrete type, which a protocol cannot");
        }
        if (definition instanceof TypeDefinition.ComplexTypeDefinition complex && !complex.abstraction()) {
            superclass(definition);
        }
        if (definition instanceof TypeDefinition.EnumDefinition enumeration) {
            for (final ParameterDefinition attribute : enumeration.attributes()) {
                if (Set.of("name", "value").contains(PyNames.identifier(attribute.name()))) {
                    throw new PyGap("the attribute '" + attribute.name() + "' clashes with Enum." + attribute.name());
                }
            }
        }
        // the declaration itself, once: what only its generation detects (a default without Python form, an enum
        // value with the wrong number of arguments) is a gap before anything refers to it
        new PyTypeGenerator(this).generate(definition, scratch);
    }

    /**
     * The permitted subtypes of a sealed abstraction, as {@code @@sealed(A, B)} lists them; a name is looked up in
     * the abstraction's namespace first.
     *
     * @param definition the sealed abstraction
     * @return the subtypes, in the order of the annotation
     * @throws PyGap if the annotation lists none or a name that is not declared
     */
    List<QualifiedName> sealedMembers(final TypeDefinition definition) {
        final List<QualifiedName> members = new ArrayList<>();
        for (final org.hiero.sdk.v3.metalang.ast.Literal argument : definition.annotation("sealed").orElseThrow()
                .arguments()) {
            final String text = argument.text();
            final String simple = text.substring(text.lastIndexOf('.') + 1);
            final QualifiedName member = text.contains(".")
                    ? new QualifiedName(text.substring(0, text.lastIndexOf('.')), simple)
                    : model.type(definition.name().namespace(), simple).map(TypeDefinition::name)
                    .or(() -> model.types().stream().map(TypeDefinition::name)
                            .filter(n -> n.name().equals(simple)).findFirst())
                    .orElseThrow(() -> new PyGap("the permitted subtype " + text + " is not declared"));
            if (model.type(member).isEmpty()) {
                throw new PyGap("the permitted subtype " + text + " is not declared");
            }
            members.add(member);
        }
        if (members.isEmpty()) {
            throw new PyGap("@@sealed lists no permitted subtype to form a union of");
        }
        return members;
    }

    /** Every declared type a declaration names in its supertypes, attributes or type parameter bounds. */
    private Set<QualifiedName> referenced(final TypeDefinition definition) {
        final Set<QualifiedName> names = new HashSet<>();
        definition.supertypes().forEach(t -> collect(t, names));
        definition.fields().forEach(f -> collect(f.type(), names));
        pythonParameters(definition).stream().map(TypeParameterDefinition::bound).filter(Objects::nonNull)
                .forEach(t -> collect(t, names));
        if (definition instanceof TypeDefinition.EnumDefinition enumeration) {
            enumeration.attributes().forEach(a -> collect(a.type(), names));
        }
        if (definition.hasAnnotation("sealed")) {
            try {
                names.addAll(sealedMembers(definition)); // the union alias names every permitted subtype
            } catch (final PyGap ignored) {
                // reported by the declaration itself
            }
        }
        names.remove(definition.name());
        return names;
    }

    private static void collect(final Type type, final Set<QualifiedName> names) {
        if (type instanceof Type.DeclaredType declared) {
            names.add(declared.name());
        }
        arguments(type).forEach(argument -> collect(argument, names));
    }
}
