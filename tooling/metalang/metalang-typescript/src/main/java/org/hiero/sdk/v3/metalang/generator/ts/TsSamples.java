package org.hiero.sdk.v3.metalang.generator.ts;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.hiero.sdk.v3.metalang.ast.Annotation;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.generator.Constraints;
import org.hiero.sdk.v3.metalang.generator.IntegerRange;
import org.hiero.sdk.v3.metalang.model.EnumValueDefinition;
import org.hiero.sdk.v3.metalang.model.FieldDefinition;
import org.hiero.sdk.v3.metalang.model.FunctionDefinition;
import org.hiero.sdk.v3.metalang.model.InstanceDefinition;
import org.hiero.sdk.v3.metalang.model.InstanceExpression;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.hiero.sdk.v3.metalang.model.MethodDefinition;
import org.hiero.sdk.v3.metalang.model.ParameterDefinition;
import org.hiero.sdk.v3.metalang.model.QualifiedName;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.model.TypeDefinition;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;

/**
 * Builds valid values for the generated TypeScript tests, like the Java counterpart: the default instance of a type
 * first, then values within the range and the validation annotations of the type, enum constants, classes created
 * with their constructor, a generated class for an interface, a static method or function that returns the type, and
 * for values that are only stored a test double. The values are deterministic; the {@code variant} gives different
 * values of the same type.
 */
final class TsSamples {

    private final TsContext context;
    private final TsImports imports;
    private final Predicate<String> visibleFolder;
    private boolean doubles;

    /**
     * Creates the sample builder of a test file.
     *
     * @param context       the generation context
     * @param imports       the imports of the test file
     * @param visibleFolder the packages the test file can use (its package and the ones it requires)
     */
    TsSamples(final TsContext context, final TsImports imports, final Predicate<String> visibleFolder) {
        this.context = context;
        this.imports = imports;
        this.visibleFolder = visibleFolder;
    }

    private boolean visible(final String namespace) {
        return visibleFolder.test(context.folder(namespace));
    }

    /**
     * Returns a valid value.
     *
     * @param type         the type, without type variables
     * @param annotations  the annotations of the attribute or parameter
     * @param variant      selects one of several values
     * @param allowDoubles whether an interface without implementation may be represented by a test double (only for
     *                     values that are stored, never for arguments of methods)
     * @return the TypeScript expression, empty if no value can be built
     */
    Optional<String> value(final Type type, final List<Annotation> annotations, final int variant,
                           final boolean allowDoubles) {
        doubles = allowDoubles;
        try {
            return value(type, annotations, variant, Set.of());
        } finally {
            doubles = false;
        }
    }

    private Optional<String> value(final Type type, final List<Annotation> annotations, final int variant,
                                   final Set<QualifiedName> visiting) {
        return switch (type) {
            case Type.BasicType basic -> basic(basic, annotations, variant, visiting);
            case Type.DeclaredType declared -> declared(declared, variant, visiting);
            case Type.TypeVariable ignored -> value(Constraints.STRING, annotations, variant, visiting);
            case Type.WildcardType wildcard -> value(wildcard.upperBound() == null ? Constraints.STRING
                    : wildcard.upperBound(), annotations, variant, visiting);
            case Type.AnyType ignored -> Optional.of(TsLiterals.quote("value" + suffix(variant)));
            case Type.FunctionType function -> lambda(function, visiting);
            case Type.VoidType ignored -> Optional.empty();
            case Type.UnresolvedType ignored -> Optional.empty();
        };
    }

    private Optional<String> basic(final Type.BasicType type, final List<Annotation> annotations, final int variant,
                                   final Set<QualifiedName> visiting) {
        final BuiltinType builtin = type.builtin();
        return switch (builtin.category()) {
            case INTEGER -> integer(builtin, annotations, variant);
            case FLOAT -> Constraints.decimal(annotations, variant).map(BigDecimal::toPlainString);
            case DECIMAL -> Constraints.decimal(annotations, variant).map(v -> TsLiterals.quote(v.toPlainString()));
            case BOOL -> Optional.of(variant % 2 == 0 ? "true" : "false");
            case STRING -> Constraints.string(annotations, variant).map(TsLiterals::quote);
            case BYTES -> {
                final int size = Constraints.elementCount(annotations, 3);
                yield size < 0 ? Optional.empty() : Optional.of(bytes(size, variant));
            }
            case COLLECTION -> collection(type, annotations, variant, visiting);
            case MAP -> map(type, annotations, variant, visiting);
            case TYPE -> classLiteral(type);
            case UUID -> Optional.of(TsLiterals.quote("00000000-0000-0000-0000-"
                    + String.format(java.util.Locale.ROOT, "%012d", variant + 1)));
            case TEMPORAL -> Optional.of("new Date(\"2024-01-" + String.format(java.util.Locale.ROOT, "%02d",
                    variant % 28 + 1) + "T12:00:00.000Z\")");
            case DURATION -> duration(builtin, annotations, variant);
            case STREAM_RESULT -> Optional.empty();
        };
    }

    private Optional<String> integer(final BuiltinType builtin, final List<Annotation> annotations,
                                     final int variant) {
        final IntegerRange range = Constraints.range(builtin, annotations);
        if (range.min().compareTo(range.max()) > 0) {
            return Optional.empty();
        }
        BigInteger value = BigInteger.valueOf(1L + variant);
        if (!range.contains(value)) {
            value = range.min().add(BigInteger.valueOf(variant));
        }
        if (!range.contains(value)) {
            value = range.min();
        }
        return Optional.of(integerLiteral(builtin, value));
    }

    /**
     * Renders an integer of a type: {@code 5} or {@code 5n}.
     *
     * @param builtin the integer type
     * @param value   the value
     * @return the literal
     */
    static String integerLiteral(final BuiltinType builtin, final BigInteger value) {
        return TsTypes.isBigInt(builtin) ? value + "n" : value.toString();
    }

    static String bytes(final int size, final int variant) {
        return "new Uint8Array([" + IntStream.range(0, size).mapToObj(i -> String.valueOf((variant + i) % 100 + 1))
                .collect(Collectors.joining(", ")) + "])";
    }

    private Optional<String> collection(final Type.BasicType type, final List<Annotation> annotations,
                                        final int variant, final Set<QualifiedName> visiting) {
        final boolean set = type.builtin().name().equals("set");
        final int size = Constraints.elementCount(annotations, 1);
        if (size < 0 || type.arguments().isEmpty()) {
            return Optional.empty();
        }
        final List<String> elements = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            final Optional<String> element = value(type.arguments().getFirst(), List.of(), variant * size + i,
                    visiting);
            if (element.isEmpty()) {
                return Optional.empty();
            }
            elements.add(element.get());
        }
        if (set && new HashSet<>(elements).size() < elements.size()) {
            return Optional.empty();
        }
        return Optional.of(set ? "new Set([" + String.join(", ", elements) + "])"
                : "[" + String.join(", ", elements) + "]");
    }

    private Optional<String> map(final Type.BasicType type, final List<Annotation> annotations, final int variant,
                                 final Set<QualifiedName> visiting) {
        final int size = Constraints.elementCount(annotations, 1);
        if (size < 0 || type.arguments().size() != 2) {
            return Optional.empty();
        }
        final List<String> keys = new ArrayList<>();
        final List<String> entries = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            final Optional<String> key = value(type.arguments().get(0), List.of(), variant * size + i, visiting);
            final Optional<String> value = value(type.arguments().get(1), List.of(), variant * size + i, visiting);
            if (key.isEmpty() || value.isEmpty()) {
                return Optional.empty();
            }
            keys.add(key.get());
            entries.add("[" + key.get() + ", " + value.get() + "]");
        }
        if (new HashSet<>(keys).size() < keys.size()) {
            return Optional.empty();
        }
        return Optional.of("new Map([" + String.join(", ", entries) + "])");
    }

    private Optional<String> classLiteral(final Type.BasicType type) {
        // any class: type, type<ANY>, type<string>
        if (type.arguments().isEmpty() || type.arguments().getFirst() instanceof Type.AnyType
                || type.arguments().getFirst() instanceof Type.WildcardType w && w.upperBound() == null) {
            return Optional.of("Object");
        }
        if (type.arguments().getFirst() instanceof Type.BasicType basic
                && basic.builtin().category() == BuiltinType.Category.STRING) {
            return Optional.of("String");
        }
        if (!type.arguments().isEmpty() && type.arguments().getFirst() instanceof Type.DeclaredType declared
                && declared.arguments().isEmpty() && context.isGenerated(declared.name())
                && (context.isClass(declared.name()) || context.isEnum(declared.name()))
                && visible(declared.name().namespace())) {
            return Optional.of(imports.value(declared.name()));
        }
        return Optional.empty();
    }

    private Optional<String> duration(final BuiltinType builtin, final List<Annotation> annotations,
                                      final int variant) {
        final Optional<BigDecimal> bound = Constraints.bound(annotations, "min")
                .or(() -> Constraints.bound(annotations, "max"));
        if (bound.isPresent()) {
            return Optional.of(TsLiterals.number(bound.get().toPlainString(), builtin, imports));
        }
        return Optional.of(imports.support("Duration", true) + ".ofSeconds(" + (variant + 1) + ")");
    }

    // --- declared types --------------------------------------------------------------------------

    private Optional<String> declared(final Type.DeclaredType type, final int variant,
                                      final Set<QualifiedName> visiting) {
        if (!context.isGenerated(type.name()) || !visible(type.name().namespace())) {
            return Optional.empty();
        }
        final LinkedModel model = context.model();
        final TypeDefinition definition = model.definition(type);
        final Optional<InstanceDefinition> instance = model.instance(type.name())
                .filter(i -> !visiting.contains(type.name()) && Constraints.fitsInstance(i.type(), type));
        if (instance.isPresent()) {
            final Set<QualifiedName> nested = new HashSet<>(visiting);
            nested.add(type.name());
            final Optional<String> value = render(instance.get().expression(), nested);
            if (value.isPresent()) {
                return value;
            }
        }
        return switch (definition) {
            case TypeDefinition.EnumDefinition enumType -> constant(enumType, variant);
            case TypeDefinition.ComplexTypeDefinition complex when complex.abstraction() ->
                    subtype(type, variant, visiting);
            case TypeDefinition.ComplexTypeDefinition complex -> construct(complex, type, variant, visiting)
                    .or(() -> factoryFunction(type, visiting));
        };
    }

    private Optional<String> constant(final TypeDefinition.EnumDefinition enumType, final int variant) {
        final List<EnumValueDefinition> current = enumType.values().stream()
                .filter(v -> !v.hasAnnotation("deprecated")).toList();
        final List<EnumValueDefinition> values = current.isEmpty() ? enumType.values() : current;
        if (values.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(imports.value(enumType.name()) + "." + values.get(variant % values.size()).name());
    }

    private Optional<String> construct(final TypeDefinition.ComplexTypeDefinition definition,
                                       final Type.DeclaredType type, final int variant,
                                       final Set<QualifiedName> visiting) {
        if (visiting.contains(definition.name())) {
            return Optional.empty();
        }
        final Optional<Map<Type.TypeVariable, Type>> arguments = Constraints.arguments(definition, type);
        if (arguments.isEmpty()) {
            return Optional.empty();
        }
        final Set<QualifiedName> nested = new HashSet<>(visiting);
        nested.add(definition.name());
        final List<String> values = new ArrayList<>();
        int index = 0;
        for (final FieldDefinition field : definition.fields()) {
            if (field.hasAnnotation("nullable") || field.hasAnnotation("default")) {
                continue; // omitted: null or the default
            }
            final Optional<String> value = value(LinkedModel.substitute(field.type(), arguments.get()),
                    field.annotations(), index++ == 0 ? variant : 0, nested);
            if (value.isEmpty()) {
                return Optional.empty();
            }
            values.add(field.name() + ": " + value.get());
        }
        return Optional.of("new " + imports.value(definition.name()) + "({ " + String.join(", ", values) + " })");
    }

    /** A value of an interface: a generated class, a static function, a namespace function, a test double. */
    private Optional<String> subtype(final Type.DeclaredType type, final int variant,
                                     final Set<QualifiedName> visiting) {
        final boolean anyArguments = type.arguments().stream()
                .allMatch(a -> a instanceof Type.WildcardType || a instanceof Type.AnyType);
        final List<TypeDefinition> candidates = context.model().types().stream()
                .filter(t -> context.isGenerated(t.name()) && !t.name().equals(type.name())
                        && visible(t.name().namespace()))
                .filter(t -> !(t instanceof TypeDefinition.ComplexTypeDefinition c && c.abstraction()))
                .filter(t -> context.model().isSubtypeOf(t.name(), type.name()))
                .sorted(Comparator.comparing((TypeDefinition t) -> t.typeParameters().isEmpty() ? 0 : 1)
                        .thenComparing(t -> t instanceof TypeDefinition.EnumDefinition ? 0 : 1)
                        .thenComparing(t -> t.name().toString()))
                .toList();
        for (final TypeDefinition candidate : candidates) {
            if (!anyArguments && !candidate.typeParameters().isEmpty()) {
                continue;
            }
            final Optional<String> value = switch (candidate) {
                case TypeDefinition.EnumDefinition enumType -> constant(enumType, variant);
                case TypeDefinition.ComplexTypeDefinition complex ->
                        construct(complex, new Type.DeclaredType(complex.name(), List.of()), variant, visiting);
            };
            if (value.isPresent()) {
                return value;
            }
        }
        return staticFunction(type, visiting).or(() -> factoryFunction(type, visiting))
                .or(() -> doubles && type.arguments().isEmpty() ? Optional.of("(Object.freeze({}) as unknown as "
                        + TsTypes.type(type, imports) + ")") : Optional.empty());
    }

    private Optional<String> staticFunction(final Type.DeclaredType type, final Set<QualifiedName> visiting) {
        final TypeDefinition definition = context.model().definition(type);
        if (!definition.typeParameters().isEmpty()) {
            return Optional.empty();
        }
        // imported only if the method is used
        return call(type, definition.methods().stream().filter(MethodDefinition::isStatic).toList(),
                m -> imports.value(definition.name()), visiting);
    }

    private Optional<String> factoryFunction(final Type.DeclaredType type, final Set<QualifiedName> visiting) {
        final Map<MethodDefinition, String> owners = new LinkedHashMap<>();
        for (final FunctionDefinition function : context.functions()) {
            if (visible(function.namespace())) {
                owners.put(function.method(), function.namespace());
            }
        }
        return call(type, List.copyOf(owners.keySet()), m -> null, visiting, owners);
    }

    private Optional<String> call(final Type.DeclaredType type, final List<MethodDefinition> candidates,
                                  final java.util.function.Function<MethodDefinition, String> owner,
                                  final Set<QualifiedName> visiting) {
        return call(type, candidates, owner, visiting, Map.of());
    }

    /**
     * Calls the first static method or function that returns the type and whose arguments can be built (without
     * {@code @@throws} and with fewer parameters first).
     */
    private Optional<String> call(final Type.DeclaredType type, final List<MethodDefinition> candidates,
                                  final java.util.function.Function<MethodDefinition, String> owner,
                                  final Set<QualifiedName> visiting, final Map<MethodDefinition, String> functions) {
        final TypeDefinition definition = context.model().definition(type);
        if (visiting.contains(definition.name())) {
            return Optional.empty();
        }
        final Set<QualifiedName> nested = new HashSet<>(visiting);
        nested.add(definition.name());
        final List<MethodDefinition> methods = candidates.stream()
                .filter(m -> m.typeParameters().isEmpty() && !m.hasAnnotation("nullable")
                        && !m.hasAnnotation("async") && !m.hasAnnotation("streaming"))
                .filter(m -> m.returnType() instanceof Type.DeclaredType returned
                        && returned.name().equals(definition.name()))
                .sorted(Comparator.comparing((MethodDefinition m) -> m.hasAnnotation("throws") ? 1 : 0)
                        .thenComparing(m -> m.parameters().size())
                        .thenComparing(MethodDefinition::signature)
                        .thenComparing(m -> functions.getOrDefault(m, "")))
                .toList();
        for (final MethodDefinition method : methods) {
            final List<String> arguments = new ArrayList<>();
            for (final ParameterDefinition parameter : method.parameters()) {
                if (parameter.varargs()) {
                    continue;
                }
                final Optional<String> value = parameter.hasAnnotation("nullable") ? Optional.of("null")
                        : value(parameter.type(), parameter.annotations(), 0, nested);
                if (value.isEmpty()) {
                    break;
                }
                arguments.add(value.get());
            }
            if (arguments.size() == method.parameters().stream().filter(p -> !p.varargs()).count()) {
                final String namespace = functions.get(method);
                final String target = namespace == null ? owner.apply(method) + "." + method.name()
                        : imports.function(namespace, method.name());
                return Optional.of(target + "(" + String.join(", ", arguments) + ")");
            }
        }
        return Optional.empty();
    }

    private Optional<String> lambda(final Type.FunctionType function, final Set<QualifiedName> visiting) {
        final String names = "(" + IntStream.range(0, function.parameters().size()).mapToObj(i -> "p" + i)
                .collect(Collectors.joining(", ")) + ")";
        if (function.returnType() instanceof Type.VoidType) {
            return Optional.of(names + " => {}");
        }
        return value(function.returnType(), List.of(), 0, visiting).map(v -> names + " => " + v);
    }

    // --- default instances -----------------------------------------------------------------------

    private Optional<String> render(final InstanceExpression expression, final Set<QualifiedName> visiting) {
        return switch (expression) {
            case InstanceExpression.Default value -> value(value.type(), List.of(), 0, visiting);
            case InstanceExpression.Construct construct -> {
                if (!context.isGenerated(construct.type().name())) {
                    yield Optional.empty();
                }
                final List<String> values = new ArrayList<>();
                for (final Map.Entry<String, InstanceExpression> value : construct.values().entrySet()) {
                    final Optional<String> rendered = render(value.getValue(), visiting);
                    if (rendered.isEmpty()) {
                        yield Optional.empty();
                    }
                    values.add(value.getKey() + ": " + rendered.get());
                }
                yield Optional.of("new " + imports.value(construct.type().name()) + "({ " + String.join(", ", values)
                        + " })");
            }
            case InstanceExpression.Call call -> {
                final String target;
                if (call.owner() != null) {
                    if (!context.isGenerated(call.owner()) || !visible(call.owner().namespace())) {
                        yield Optional.empty();
                    }
                    target = imports.value(call.owner()) + "." + call.method().name();
                } else {
                    if (context.functions(call.namespace()).stream().noneMatch(f -> f.method().equals(call.method()))
                            || !visible(call.namespace())) {
                        yield Optional.empty();
                    }
                    target = imports.function(call.namespace(), call.method().name());
                }
                yield arguments(call.method(), call.arguments(), visiting).map(a -> target + "(" + a + ")");
            }
            case InstanceExpression.MethodCall call -> render(call.target(), visiting).flatMap(target ->
                    arguments(call.method(), call.arguments(), visiting).map(a -> target + "." + call.method().name()
                            + "(" + a + ")"));
            case InstanceExpression.Access access -> render(access.target(), visiting)
                    .map(target -> target + "." + access.field().name());
            case InstanceExpression.Value value -> Optional.of(renderValue(value));
            case InstanceExpression.ConstantValue constant -> visible(constant.constant().name().namespace())
                    ? Optional.of(imports.constant(constant.constant().name().namespace(),
                    constant.constant().name().name())) : Optional.empty();
            case InstanceExpression.ListValue list -> {
                final List<String> items = new ArrayList<>();
                for (final InstanceExpression item : list.items()) {
                    final Optional<String> rendered = item instanceof InstanceExpression.Value v
                            && ((Type.BasicType) list.type()).builtin().category() == BuiltinType.Category.BYTES
                            ? Optional.of(v.literal().text()) : render(item, visiting);
                    if (rendered.isEmpty()) {
                        yield Optional.empty();
                    }
                    items.add(rendered.get());
                }
                final BuiltinType builtin = ((Type.BasicType) list.type()).builtin();
                yield Optional.of(builtin.category() == BuiltinType.Category.BYTES
                        ? "new Uint8Array([" + String.join(", ", items) + "])"
                        : builtin.name().equals("set") ? "new Set([" + String.join(", ", items) + "])"
                        : "[" + String.join(", ", items) + "]");
            }
        };
    }

    private String renderValue(final InstanceExpression.Value value) {
        final Literal literal = value.literal();
        if (value.type() instanceof Type.BasicType || value.type() instanceof Type.DeclaredType) {
            try {
                return TsLiterals.expression(literal, value.type(), imports);
            } catch (final IllegalArgumentException e) {
                // rendered like for ANY
            }
        }
        return literal instanceof Literal.StringLiteral string ? TsLiterals.quote(string.value()) : literal.text();
    }

    private Optional<String> arguments(final MethodDefinition method, final Map<String, InstanceExpression> arguments,
                                       final Set<QualifiedName> visiting) {
        final List<String> result = new ArrayList<>();
        for (final ParameterDefinition parameter : method.parameters()) {
            final InstanceExpression value = arguments.get(parameter.name());
            if (value == null) {
                if (!parameter.varargs()) {
                    result.add("null");
                }
                continue;
            }
            final Optional<String> rendered = render(value, visiting);
            if (rendered.isEmpty()) {
                return Optional.empty();
            }
            result.add(rendered.get());
        }
        return Optional.of(String.join(", ", result));
    }

    private static String suffix(final int variant) {
        return variant == 0 ? "" : String.valueOf(variant);
    }
}
