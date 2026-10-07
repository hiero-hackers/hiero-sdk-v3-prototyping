package org.hiero.sdk.v3.metalang.generator.go;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.hiero.sdk.v3.metalang.model.FieldDefinition;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.hiero.sdk.v3.metalang.model.ParameterDefinition;
import org.hiero.sdk.v3.metalang.model.QualifiedName;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.model.TypeDefinition;
import org.hiero.sdk.v3.metalang.model.TypeParameterDefinition;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;

/**
 * What the Go generator needs to know beyond a single declaration: the configuration, the linked model, and the
 * mapping from a meta-language {@link Type} to a {@link GoType}.
 */
final class GoContext {

    /** The widths Go has a fixed integer type for. */
    private static final int[] INT_WIDTHS = {8, 16, 32, 64};

    private final LinkedModel model;

    private final GoGeneratorConfig config;

    /** Memoizes whether a declared type is usable as a map key; the computation walks its attributes. */
    private final Map<QualifiedName, Boolean> comparable = new HashMap<>();

    /** The declarations that cannot be generated, computed once on first use. */
    private Map<QualifiedName, String> gaps;

    /** The exported names of every declared type; a type parameter must not shadow one of them. */
    private Set<String> declaredNames;

    /**
     * Creates a context.
     *
     * @param model  the linked model
     * @param config the configuration
     */
    GoContext(final LinkedModel model, final GoGeneratorConfig config) {
        this.model = Objects.requireNonNull(model, "model must not be null");
        this.config = Objects.requireNonNull(config, "config must not be null");
    }

    LinkedModel model() {
        return model;
    }

    GoGeneratorConfig config() {
        return config;
    }

    /**
     * The import path of the package a declared type lives in.
     *
     * @param name the qualified name
     * @return the import path
     */
    String importPath(final QualifiedName name) {
        return config.importPath(name.namespace());
    }

    /**
     * Maps a meta-language type to its Go form.
     *
     * @param type the resolved type
     * @return the Go type
     * @throws GoGap if the type has no Go form yet
     */
    GoType type(final Type type) {
        return switch (type) {
            case Type.BasicType basic -> basic(basic);
            case Type.DeclaredType declared -> declared(declared);
            case Type.TypeVariable variable -> new GoType.Variable(parameterName(variable.name()));
            case Type.WildcardType wildcard -> wildcard.upperBound() == null ? new GoType.Any()
                    : type(wildcard.upperBound());
            // a wildcard outside a type argument list is the top type
            case Type.AnyType ignored -> new GoType.Any();
            case Type.VoidType ignored -> new GoType.Void();
            case Type.FunctionType function -> new GoType.Func(
                    function.parameters().stream().map(ParameterDefinition::type).map(this::type).toList(),
                    type(function.returnType()));
            case Type.UnresolvedType unresolved ->
                    throw new GoGap("the type '" + unresolved.reference() + "' is not resolved");
        };
    }

    private GoType basic(final Type.BasicType basic) {
        final BuiltinType builtin = basic.builtin();
        return switch (builtin.category()) {
            case INTEGER -> integer(builtin);
            case FLOAT -> new GoType.Predeclared("float64");
            // the guideline maps these two to third-party modules, and go.mod carries no requirements yet, so a
            // declaration using them would not build; it is deferred until the generator manages dependencies
            case DECIMAL -> throw new GoGap("'decimal' maps to github.com/shopspring/decimal, and the generator "
                    + "does not add module requirements to go.mod yet");
            case STRING -> new GoType.Predeclared("string");
            case BOOL -> new GoType.Predeclared("bool");
            case BYTES -> new GoType.Bytes();
            case COLLECTION -> collection(basic);
            case MAP -> map(basic);
            // the argument of type<T> only documents the bound; reflect.Type carries no parameter
            case TYPE -> new GoType.Library("reflect", "Type", true);
            case UUID -> throw new GoGap("'uuid' maps to github.com/google/uuid, and the generator does not add "
                    + "module requirements to go.mod yet");
            case TEMPORAL -> new GoType.Library("time", "Time", true);
            case DURATION -> new GoType.Library("time", "Duration", true);
            case STREAM_RESULT -> new GoType.StreamResult(type(basic.arguments().getFirst()));
        };
    }

    /** {@code intX}: the next width Go has, or {@code *big.Int} beyond 64 bits. */
    private static GoType integer(final BuiltinType builtin) {
        final boolean signed = !builtin.name().startsWith("u");
        for (final int width : INT_WIDTHS) {
            if (builtin.bits() <= width) {
                return new GoType.Predeclared((signed ? "int" : "uint") + width);
            }
        }
        return new GoType.Pointer(new GoType.Library("math/big", "Int", true));
    }

    private GoType collection(final Type.BasicType basic) {
        final GoType element = type(basic.arguments().getFirst());
        if (!"set".equals(basic.builtin().name())) {
            return new GoType.Slice(element);
        }
        // a set is a map to the empty struct, which Go only allows for a comparable element type
        return element.comparable() ? new GoType.MapOf(element, GoType.MapOf.UNIT) : new GoType.Slice(element);
    }

    private GoType map(final Type.BasicType basic) {
        final GoType key = type(basic.arguments().getFirst());
        final GoType value = type(basic.arguments().get(1));
        if (!key.comparable()) {
            throw new GoGap("a map key of type " + basic.arguments().getFirst().text()
                    + " is not comparable, and Go has no map with such a key");
        }
        return new GoType.MapOf(key, value);
    }

    private GoType declared(final Type.DeclaredType declared) {
        final TypeDefinition definition = model.type(declared.name())
                .orElseThrow(() -> new GoGap("the type " + declared.name() + " is not declared"));
        final List<TypeParameterDefinition> parameters = definition.typeParameters();
        final List<GoType> arguments = new ArrayList<>();
        for (int i = 0; i < declared.arguments().size(); i++) {
            final TypeParameterDefinition parameter = i < parameters.size() ? parameters.get(i) : null;
            if (parameter != null && isSelfParameter(parameter)) {
                continue; // the Go declaration does not have this parameter, so it takes no argument for it
            }
            final Type argument = declared.arguments().get(i);
            arguments.add(argument instanceof Type.WildcardType wildcard ? wildcard(wildcard, parameter)
                    : type(argument));
        }
        return new GoType.Declared(importPath(declared.name()), GoNames.exported(declared.name().name()),
                isAbstraction(definition), isComparable(definition), List.copyOf(arguments));
    }

    /**
     * A wildcard type argument. Go has no wildcard: {@code Page<ANY>} has to name a type. The nearest Go form is
     * the bound of the parameter the wildcard fills - but only when that bound does not mention type variables
     * itself. A self-referential bound such as {@code $$Self extends NativeToken<$$Self, $$Unit>} would expand
     * forever, and that declaration is deferred instead of generated wrongly.
     */
    private GoType wildcard(final Type.WildcardType wildcard, final TypeParameterDefinition parameter) {
        final Type bound = wildcard.upperBound() != null ? wildcard.upperBound()
                : parameter == null ? null : parameter.bound();
        if (bound == null) {
            return new GoType.Any();
        }
        if (mentions(bound, null)) {
            throw new GoGap("the wildcard argument 'ANY' fills a parameter bound to " + bound.text()
                    + ", which Go cannot name: Go has no wildcard type argument");
        }
        return type(bound);
    }

    /**
     * Whether a type mentions a type variable: a particular one, or any at all when {@code variable} is
     * {@code null}. A bound that mentions its own parameter is a self type; a bound that mentions any variable
     * cannot stand in for a wildcard.
     */
    private static boolean mentions(final Type type, final Type.TypeVariable variable) {
        if (type instanceof Type.TypeVariable found) {
            return variable == null || found.equals(variable);
        }
        return arguments(type).stream().anyMatch(argument -> mentions(argument, variable));
    }

    /** The types nested inside a type: its type arguments, or the bound of a wildcard. */
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

    /**
     * The Go name of a type parameter. {@code $$Receipt} would render as {@code Receipt} and shadow the declared
     * type of that name - Go then rejects the declaration with "cannot use a type parameter as constraint" - so
     * such a name gets a {@code T} suffix. Declaring and using a parameter has to agree on this, which is why it
     * is decided here and nowhere else.
     *
     * @param metaName the name in the meta-language, including the {@code $$} prefix
     * @return the Go name
     */
    String parameterName(final String metaName) {
        final String name = GoNames.exported(metaName.substring("$$".length()));
        if (declaredNames == null) {
            declaredNames = model.types().stream().map(t -> GoNames.exported(t.name().name()))
                    .collect(java.util.stream.Collectors.toSet());
        }
        return declaredNames.contains(name) ? name + "T" : name;
    }

    /**
     * Whether a type parameter is a self type, i.e. its bound mentions the parameter itself
     * ({@code $$Self extends Transaction<$$Self, ...>}).
     *
     * <p>Go has neither a self type nor covariant returns, and the guideline already decided that a setter
     * returns the concrete type rather than the self type. The parameter therefore carries no information a Go
     * signature could use, and it is dropped from the generated declaration - which is also what makes
     * {@code Transaction<ANY, ANY>} nameable in Go at all.
     *
     * @param parameter the type parameter
     * @return {@code true} if it is a self type
     */
    static boolean isSelfParameter(final TypeParameterDefinition parameter) {
        return parameter.bound() != null && mentions(parameter.bound(), parameter.variable());
    }

    /**
     * The type parameters a Go declaration keeps: all but the self types.
     *
     * @param definition the declaration
     * @return the remaining type parameters, in order
     */
    static List<TypeParameterDefinition> goParameters(final TypeDefinition definition) {
        return definition.typeParameters().stream().filter(p -> !isSelfParameter(p)).toList();
    }

    /**
     * Whether a declaration becomes a Go interface rather than a struct.
     *
     * @param definition the declaration
     * @return {@code true} for an abstraction
     */
    static boolean isAbstraction(final TypeDefinition definition) {
        return definition instanceof TypeDefinition.ComplexTypeDefinition complex && complex.abstraction();
    }

    /**
     * Whether Go allows values of a declared type as a map key. An interface is comparable at compile time; a
     * struct is comparable exactly when every attribute is.
     *
     * @param definition the declaration
     * @return {@code true} if comparable
     */
    boolean isComparable(final TypeDefinition definition) {
        if (isAbstraction(definition)) {
            return true;
        }
        final Boolean known = comparable.get(definition.name());
        if (known != null) {
            return known;
        }
        // a cycle has to pass through an interface or a pointer, which are comparable, so assuming it is safe
        comparable.put(definition.name(), true);
        boolean result = true;
        for (final FieldDefinition field : definition.fields()) {
            try {
                if (!type(field.type()).comparable()) {
                    result = false;
                    break;
                }
            } catch (final GoGap gap) {
                result = false;
                break;
            }
        }
        comparable.put(definition.name(), result);
        return result;
    }

    /**
     * The declarations that cannot be generated, with the reason. A declaration is in here when its own shape has
     * no Go form, and also when it references such a declaration: emitting a type whose attribute type does not
     * exist would produce a module that does not compile, so the gap propagates to a fixed point.
     *
     * @return the gaps, keyed by qualified name
     */
    Map<QualifiedName, String> gaps() {
        if (gaps != null) {
            return gaps;
        }
        final Map<QualifiedName, String> found = new LinkedHashMap<>();
        for (final TypeDefinition definition : model.types()) {
            try {
                goParameters(definition).stream().map(TypeParameterDefinition::bound)
                        .filter(Objects::nonNull).forEach(this::type);
                definition.supertypes().forEach(this::type);
                definition.fields().stream().map(FieldDefinition::type).forEach(this::type);
                if (definition instanceof TypeDefinition.EnumDefinition enumeration) {
                    enumeration.attributes().stream().map(ParameterDefinition::type).forEach(this::type);
                }
            } catch (final GoGap gap) {
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
        gaps = Map.copyOf(found);
        return gaps;
    }

    /** Every declared type a declaration names in its supertypes, attributes or type parameter bounds. */
    private Set<QualifiedName> referenced(final TypeDefinition definition) {
        final Set<QualifiedName> names = new HashSet<>();
        definition.supertypes().forEach(t -> collect(t, names));
        definition.fields().forEach(f -> collect(f.type(), names));
        goParameters(definition).stream().map(TypeParameterDefinition::bound).filter(Objects::nonNull)
                .forEach(t -> collect(t, names));
        if (definition instanceof TypeDefinition.EnumDefinition enumeration) {
            enumeration.attributes().forEach(a -> collect(a.type(), names));
        }
        names.remove(definition.name());
        return names;
    }

    private static void collect(final Type type, final Set<QualifiedName> names) {
        switch (type) {
            case Type.DeclaredType declared -> {
                names.add(declared.name());
                declared.arguments().forEach(a -> collect(a, names));
            }
            case Type.BasicType basic -> basic.arguments().forEach(a -> collect(a, names));
            case Type.WildcardType wildcard -> {
                if (wildcard.upperBound() != null) {
                    collect(wildcard.upperBound(), names);
                }
            }
            case Type.FunctionType function -> {
                collect(function.returnType(), names);
                function.parameters().forEach(a -> collect(a.type(), names));
            }
            default -> {
                // a variable, the top type and void name no declaration
            }
        }
    }

    /**
     * The supertypes of a declaration that are abstractions, in declaration order.
     *
     * @param definition the declaration
     * @return the abstraction supertypes
     */
    List<Type.DeclaredType> abstractionSupertypes(final TypeDefinition definition) {
        final List<Type.DeclaredType> supertypes = new ArrayList<>();
        for (final Type supertype : definition.supertypes()) {
            if (supertype instanceof Type.DeclaredType declared
                    && model.type(declared.name()).map(GoContext::isAbstraction).orElse(false)) {
                supertypes.add(declared);
            }
        }
        return List.copyOf(supertypes);
    }

    /**
     * Looks a declaration up.
     *
     * @param name the qualified name
     * @return the declaration, if present
     */
    Optional<TypeDefinition> definition(final QualifiedName name) {
        return model.type(name);
    }

}
