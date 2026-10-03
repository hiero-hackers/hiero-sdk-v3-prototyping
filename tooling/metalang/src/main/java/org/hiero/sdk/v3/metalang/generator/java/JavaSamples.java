package org.hiero.sdk.v3.metalang.generator.java;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.hiero.sdk.v3.metalang.ast.Annotation;
import org.hiero.sdk.v3.metalang.ast.Literal;
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
import org.hiero.sdk.v3.metalang.model.TypeParameterDefinition;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;
import org.jspecify.annotations.Nullable;

/**
 * Builds valid values for the generated tests as Java expressions: for a type and the validation annotations of the
 * attribute or parameter it belongs to ({@code @@min}, {@code @@maxLength}, {@code @@pattern}, {@code @@minSize},
 * ...), so that the generated code accepts them. Values are deterministic; the {@code variant} gives different
 * values of the same type (elements of a set, a second value for a setter).
 *
 * <ul>
 *   <li>Basic types get literals within their range and constraints ({@code (short) 1}, {@code "value"},
 *       {@code new byte[] {1, 2, 3}}, {@code List.of(...)}, {@code LocalDate.of(2024, 1, 1)}, ...); strings of a
 *       {@code @@pattern} come from {@link RegexSamples}.</li>
 *   <li>Enums use their first constant that is not deprecated; records and classes are created with their constructor
 *       ({@code null} for nullable attributes, which keeps the values small and ends recursive types).</li>
 *   <li>An abstraction is represented by a generated concrete subtype (non-generic ones first, then enums, records,
 *       classes, then by name) whose type arguments fit.</li>
 *   <li>Function types become lambdas, type variables their type argument ({@code String} by default).</li>
 * </ul>
 *
 * <p>Where no value can be built (no concrete subtype, a cycle without nullable attribute, a pattern that is not
 * understood, contradicting constraints), the result is empty and the test that needs it is not generated.
 */
final class JavaSamples {

    /** The {@code string} type, the default for type variables without bound. */
    static final Type STRING = new Type.BasicType(BuiltinType.lookup("string").orElseThrow(), List.of());

    private final JavaContext context;
    private final Imports imports;
    private final java.util.function.Predicate<QualifiedName> visible;
    private final List<FunctionDefinition> functions;
    private boolean deprecated;
    private boolean doubles;

    /**
     * Creates the sample builder of a test file.
     *
     * @param context the generation context
     * @param imports the imports of the test file
     * @param visible   the types the test file can use (in a module that its module requires)
     * @param functions the generated namespace-level functions (static methods of the factory classes)
     */
    JavaSamples(final JavaContext context, final Imports imports,
                final java.util.function.Predicate<QualifiedName> visible, final List<FunctionDefinition> functions) {
        this.context = context;
        this.imports = imports;
        this.visible = visible;
        this.functions = List.copyOf(functions);
    }

    /**
     * Whether a value uses a deprecated type or enum constant (the test then suppresses deprecation warnings).
     *
     * @return {@code true} if a deprecated element was used
     */
    boolean usesDeprecated() {
        return deprecated;
    }

    /**
     * Returns the type arguments the tests use for type parameters: the bound, or {@code string} without bound.
     *
     * @param parameters the type parameters
     * @return the type argument of every type variable; empty if a bound refers to a type variable (e.g. a self
     *         type), which has no simple type argument
     */
    static Optional<Map<Type.TypeVariable, Type>> defaults(final List<TypeParameterDefinition> parameters) {
        final Map<Type.TypeVariable, Type> result = new HashMap<>();
        for (final TypeParameterDefinition parameter : parameters) {
            if (parameter.bound() == null) {
                result.put(parameter.variable(), STRING);
            } else if (containsVariable(parameter.bound())) {
                return Optional.empty();
            } else {
                result.put(parameter.variable(), parameter.bound());
            }
        }
        return Optional.of(result);
    }

    /**
     * Returns a valid value.
     *
     * @param type        the type, without type variables (substituted with {@link #defaults})
     * @param annotations the annotations of the attribute or parameter (validation annotations)
     * @param variant     selects one of several values (0 for the first)
     * @return the Java expression, empty if no value can be built
     */
    Optional<String> value(final Type type, final List<Annotation> annotations, final int variant) {
        return value(type, annotations, variant, false);
    }

    /**
     * Returns a valid value, optionally with test doubles for abstractions without implementation.
     *
     * @param type          the type, without type variables (substituted with {@link #defaults})
     * @param annotations   the annotations of the attribute or parameter (validation annotations)
     * @param variant       selects one of several values (0 for the first)
     * @param allowDoubles  whether an abstraction without generated subtype and factory method may be represented by
     *                      a test double (an anonymous subclass whose methods throw): only for values that the object
     *                      under test stores, never for arguments of methods, which may use them
     * @return the Java expression, empty if no value can be built
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
            case Type.TypeVariable ignored -> value(STRING, annotations, variant, visiting);
            case Type.WildcardType wildcard -> value(wildcard.upperBound() == null ? STRING : wildcard.upperBound(),
                    annotations, variant, visiting);
            case Type.AnyType ignored -> Optional.of(JavaLiterals.quote("value" + suffix(variant)));
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
            case FLOAT -> decimal(annotations, variant).map(JavaSamples::doubleLiteral);
            case DECIMAL -> decimal(annotations, variant)
                    .map(v -> "new " + imports.use("java.math", "BigDecimal") + "(\"" + v.toPlainString() + "\")");
            case BOOL -> Optional.of(variant % 2 == 0 ? "true" : "false");
            case STRING -> string(annotations, variant).map(JavaLiterals::quote);
            case BYTES -> bytes(annotations, variant);
            case COLLECTION -> collection(type, annotations, variant, visiting);
            case MAP -> map(type, annotations, variant, visiting);
            case TYPE -> classLiteral(type);
            case UUID -> Optional.of(imports.use("java.util", "UUID") + ".fromString(\"00000000-0000-0000-0000-"
                    + String.format(java.util.Locale.ROOT, "%012d", variant + 1) + "\")");
            case TEMPORAL -> Optional.of(temporal(builtin.name(), variant));
            case DURATION -> duration(type, annotations, variant);
            case STREAM_RESULT -> Optional.empty();
        };
    }

    /**
     * The values of an integer type with its {@code @@min}/{@code @@max}: the range of the type intersected with the
     * annotations.
     *
     * @param builtin     the integer type
     * @param annotations the annotations
     * @return the range; {@code min > max} if the constraints contradict each other
     */
    static JavaIntegers.Range range(final BuiltinType builtin, final List<Annotation> annotations) {
        JavaIntegers.Range range = JavaIntegers.range(builtin);
        final Optional<BigDecimal> min = bound(annotations, "min");
        final Optional<BigDecimal> max = bound(annotations, "max");
        if (min.isPresent()) {
            range = range.intersect(new JavaIntegers.Range(min.get().setScale(0, java.math.RoundingMode.CEILING)
                    .toBigIntegerExact(), range.max()));
        }
        if (max.isPresent()) {
            range = range.intersect(new JavaIntegers.Range(range.min(), max.get()
                    .setScale(0, java.math.RoundingMode.FLOOR).toBigIntegerExact()));
        }
        return range;
    }

    private Optional<String> integer(final BuiltinType builtin, final List<Annotation> annotations,
                                     final int variant) {
        final JavaIntegers.Range range = range(builtin, annotations);
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
        return Optional.of(JavaIntegers.literal(builtin, value, imports));
    }

    private static Optional<BigDecimal> decimal(final List<Annotation> annotations, final int variant) {
        BigDecimal value = new BigDecimal("1.5").add(BigDecimal.valueOf(variant));
        final Optional<BigDecimal> min = bound(annotations, "min");
        final Optional<BigDecimal> max = bound(annotations, "max");
        if (min.isPresent() && max.isPresent() && min.get().compareTo(max.get()) > 0) {
            return Optional.empty();
        }
        if (min.isPresent() && value.compareTo(min.get()) < 0) {
            value = min.get();
        }
        if (max.isPresent() && value.compareTo(max.get()) > 0) {
            value = max.get();
        }
        return Optional.of(value);
    }

    /**
     * Renders a number as {@code double} literal.
     *
     * @param value the number
     * @return the literal, e.g. {@code 1.5}
     */
    static String doubleLiteral(final BigDecimal value) {
        final String text = value.toPlainString();
        return text.contains(".") ? text : text + ".0";
    }

    /**
     * Returns a string that fulfils the string constraints ({@code @@pattern}, {@code @@urlPattern},
     * {@code @@minLength}, {@code @@maxLength}), verified like the generated checks.
     *
     * @param annotations the annotations
     * @param variant     the variant
     * @return the string, empty if none was found
     */
    static Optional<String> string(final List<Annotation> annotations, final int variant) {
        final Optional<String> pattern = stringArgument(annotations, "pattern");
        final boolean url = annotations.stream().anyMatch(a -> a.name().equals("urlPattern"));
        final List<String> bases = new ArrayList<>();
        if (url) {
            bases.add("https://example.com/v" + variant);
        }
        if (pattern.isPresent()) {
            final Optional<String> accepted = RegexSamples.accepted(pattern.get());
            accepted.ifPresent(a -> bases.add(a + suffix(variant)));
            accepted.ifPresent(bases::add);
        }
        bases.add("value" + suffix(variant));
        bases.add("a" + suffix(variant));
        return bases.stream().map(b -> fitLength(b, annotations))
                .filter(c -> isValidString(c, annotations)).findFirst();
    }

    private static String fitLength(final String value, final List<Annotation> annotations) {
        final int min = size(annotations, "minLength").orElse(0);
        final int max = size(annotations, "maxLength").orElse(Integer.MAX_VALUE);
        String result = value;
        if (result.length() < min) {
            result = result + "a".repeat(min - result.length());
        }
        if (result.length() > max) {
            result = result.substring(0, max);
        }
        return result;
    }

    /**
     * Whether the generated checks accept the string.
     *
     * @param value       the string
     * @param annotations the annotations of the attribute or parameter
     * @return {@code true} if every string constraint is fulfilled
     */
    static boolean isValidString(final String value, final List<Annotation> annotations) {
        if (size(annotations, "minLength").filter(min -> value.length() < min).isPresent()
                || size(annotations, "maxLength").filter(max -> value.length() > max).isPresent()) {
            return false;
        }
        final Optional<String> pattern = stringArgument(annotations, "pattern");
        if (pattern.isPresent() && !Pattern.compile(pattern.get()).matcher(value).find()) {
            return false;
        }
        if (annotations.stream().anyMatch(a -> a.name().equals("urlPattern"))) {
            try {
                final URI uri = new URI(value);
                return uri.isAbsolute() && uri.getHost() != null;
            } catch (final URISyntaxException e) {
                return false;
            }
        }
        return true;
    }

    private static Optional<String> bytesValue(final int size, final int variant) {
        if (size == 0) {
            return Optional.of("new byte[0]");
        }
        final List<String> items = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            items.add(String.valueOf((variant + i) % 100 + 1));
        }
        return Optional.of("new byte[] {" + String.join(", ", items) + "}");
    }

    private Optional<String> bytes(final List<Annotation> annotations, final int variant) {
        final int size = elementCount(annotations, 3);
        return size < 0 ? Optional.empty() : bytesValue(size, variant);
    }

    /**
     * The number of elements of a value with {@code @@minSize}/{@code @@maxSize}: the preferred number within the
     * bounds.
     *
     * @param annotations the annotations
     * @param preferred   the preferred number
     * @return the number, -1 if the bounds contradict each other
     */
    static int elementCount(final List<Annotation> annotations, final int preferred) {
        final int min = size(annotations, "minSize").orElse(0);
        final int max = size(annotations, "maxSize").orElse(Integer.MAX_VALUE);
        if (min > max) {
            return -1;
        }
        return Math.max(min, Math.min(max, preferred));
    }

    private Optional<String> collection(final Type.BasicType type, final List<Annotation> annotations,
                                        final int variant, final Set<QualifiedName> visiting) {
        final boolean set = type.builtin().name().equals("set");
        final int size = elementCount(annotations, 1);
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
            return Optional.empty(); // Set.of rejects duplicates
        }
        return Optional.of(imports.use("java.util", set ? "Set" : "List") + ".of(" + String.join(", ", elements)
                + ")");
    }

    private Optional<String> map(final Type.BasicType type, final List<Annotation> annotations, final int variant,
                                 final Set<QualifiedName> visiting) {
        final int size = elementCount(annotations, 1);
        if (size < 0 || type.arguments().size() != 2) {
            return Optional.empty();
        }
        final String map = imports.use("java.util", "Map");
        final List<String> keys = new ArrayList<>();
        final List<String> entries = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            final Optional<String> key = value(type.arguments().get(0), List.of(), variant * size + i, visiting);
            final Optional<String> value = value(type.arguments().get(1), List.of(), variant * size + i, visiting);
            if (key.isEmpty() || value.isEmpty()) {
                return Optional.empty();
            }
            keys.add(key.get());
            entries.add(map + ".entry(" + key.get() + ", " + value.get() + ")");
        }
        if (new HashSet<>(keys).size() < keys.size()) {
            return Optional.empty();
        }
        return Optional.of(map + ".ofEntries(" + String.join(", ", entries) + ")");
    }

    private Optional<String> classLiteral(final Type.BasicType type) {
        if (type.arguments().isEmpty()) {
            return Optional.of("String.class");
        }
        final Type bound = type.arguments().getFirst();
        if (bound instanceof Type.DeclaredType declared && declared.arguments().isEmpty()
                && context.isGenerated(declared.name())) {
            return Optional.of(imports.use(JavaNames.packageName(declared.name().namespace()), declared.name().name())
                    + ".class");
        }
        if (bound instanceof Type.BasicType basic && basic.builtin().category() == BuiltinType.Category.STRING) {
            return Optional.of("String.class");
        }
        return Optional.empty();
    }

    private String temporal(final String name, final int variant) {
        final int day = variant % 28 + 1;
        return switch (name) {
            case "date" -> imports.use("java.time", "LocalDate") + ".of(2024, 1, " + day + ")";
            case "time" -> imports.use("java.time", "LocalTime") + ".of(12, " + variant % 60 + ")";
            case "dateTime" -> imports.use("java.time", "LocalDateTime") + ".of(2024, 1, " + day + ", 12, 0)";
            default -> imports.use("java.time", "ZonedDateTime") + ".of(2024, 1, " + day + ", 12, 0, 0, 0, "
                    + imports.use("java.time", "ZoneOffset") + ".UTC)";
        };
    }

    private Optional<String> duration(final Type.BasicType type, final List<Annotation> annotations,
                                      final int variant) {
        final Optional<Annotation> bound = annotations.stream()
                .filter(a -> a.name().equals("min") || a.name().equals("max")).findFirst();
        if (bound.isPresent()) {
            return Optional.of(JavaLiterals.expression(bound.get().arguments().getFirst(), type, imports));
        }
        return Optional.of(imports.use("java.time", "Duration") + ".ofSeconds(" + (variant + 1) + "L)");
    }

    private Optional<String> declared(final Type.DeclaredType type, final int variant,
                                      final Set<QualifiedName> visiting) {
        if (!context.isGenerated(type.name())) {
            return Optional.empty();
        }
        final TypeDefinition definition = context.model().definition(type);
        // the default instance of the specs comes first: it defines the values the tests must use
        final Optional<InstanceDefinition> instance = context.model().instance(type.name())
                .filter(i -> !visiting.contains(type.name()) && fitsInstance(i.type(), type));
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
            // a type whose constructor needs a value that cannot be built may have a factory function (createClient)
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
        final EnumValueDefinition value = values.get(variant % values.size());
        deprecated |= enumType.hasAnnotation("deprecated") || value.hasAnnotation("deprecated");
        return Optional.of(imports.use(JavaNames.packageName(enumType.name().namespace()), enumType.name().name())
                + "." + value.name());
    }

    /**
     * Creates a record or class with its constructor.
     *
     * @param definition the type
     * @param type       the type with its type arguments (wildcards and missing ones get the defaults)
     * @param variant    the variant, passed on to the first constructor argument
     * @param visiting   the types being created (cycle detection)
     */
    private Optional<String> construct(final TypeDefinition.ComplexTypeDefinition definition,
                                       final Type.DeclaredType type, final int variant,
                                       final Set<QualifiedName> visiting) {
        if (visiting.contains(definition.name())) {
            return Optional.empty();
        }
        final Optional<Map<Type.TypeVariable, Type>> arguments = arguments(definition, type);
        if (arguments.isEmpty()) {
            return Optional.empty();
        }
        final Set<QualifiedName> nested = new HashSet<>(visiting);
        nested.add(definition.name());
        deprecated |= definition.hasAnnotation("deprecated");
        final List<String> values = new ArrayList<>();
        final List<FieldDefinition> parameters = constructorParameters(definition);
        for (int i = 0; i < parameters.size(); i++) {
            final FieldDefinition parameter = parameters.get(i);
            if (parameter.hasAnnotation("nullable")) {
                values.add("null");
                continue;
            }
            final Optional<String> value = value(LinkedModel.substitute(parameter.type(), arguments.get()),
                    parameter.annotations(), i == 0 ? variant : 0, nested);
            if (value.isEmpty()) {
                return Optional.empty();
            }
            values.add(value.get());
        }
        return Optional.of("new " + imports.use(JavaNames.packageName(definition.name().namespace()),
                definition.name().name()) + (definition.typeParameters().isEmpty() ? "" : "<>") + "("
                + String.join(", ", values) + ")");
    }

    /**
     * The parameters of the constructor that takes all attributes: every attribute of a record, the constructor
     * parameters of a class.
     *
     * @param definition the record or class
     * @return the parameters, in order
     */
    List<FieldDefinition> constructorParameters(final TypeDefinition definition) {
        return context.isClass(definition.name()) ? ClassGenerator.constructorParameters(definition.fields())
                : definition.fields();
    }

    /**
     * The type arguments used for a generic type: the given ones, the defaults for wildcards and missing ones.
     *
     * @param definition the type
     * @param type       the type as used
     * @return the type argument of every type variable, empty if a default does not exist
     */
    static Optional<Map<Type.TypeVariable, Type>> arguments(final TypeDefinition definition,
                                                           final Type.DeclaredType type) {
        final Optional<Map<Type.TypeVariable, Type>> defaults = defaults(definition.typeParameters());
        final Map<Type.TypeVariable, Type> result = new HashMap<>();
        final List<TypeParameterDefinition> parameters = definition.typeParameters();
        for (int i = 0; i < parameters.size(); i++) {
            final Type argument = i < type.arguments().size() ? type.arguments().get(i) : null;
            if (argument == null || argument instanceof Type.WildcardType) {
                if (defaults.isEmpty()) {
                    return Optional.empty();
                }
                result.put(parameters.get(i).variable(), defaults.get().get(parameters.get(i).variable()));
            } else {
                result.put(parameters.get(i).variable(), argument);
            }
        }
        return Optional.of(result);
    }

    /** A value of an abstraction: a value of a generated concrete subtype whose type arguments fit. */
    private Optional<String> subtype(final Type.DeclaredType type, final int variant,
                                     final Set<QualifiedName> visiting) {
        final boolean anyArguments = type.arguments().stream()
                .allMatch(a -> a instanceof Type.WildcardType || a instanceof Type.AnyType);
        final List<TypeDefinition> candidates = context.model().types().stream()
                .filter(t -> context.isGenerated(t.name()) && !t.name().equals(type.name()) && visible.test(t.name()))
                .filter(t -> !(t instanceof TypeDefinition.ComplexTypeDefinition c && c.abstraction()))
                .filter(t -> context.model().isSubtypeOf(t.name(), type.name()))
                .sorted(Comparator.comparing((TypeDefinition t) -> t.typeParameters().isEmpty() ? 0 : 1)
                        .thenComparing(t -> t instanceof TypeDefinition.EnumDefinition ? 0
                                : context.isClass(t.name()) ? 2 : 1)
                        .thenComparing(t -> t.name().toString()))
                .toList();
        for (final TypeDefinition candidate : candidates) {
            if (!anyArguments && (!candidate.typeParameters().isEmpty()
                    || !fits(supertypeAs(candidate.asType(), type.name(), new HashSet<>()), type))) {
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
        return factoryMethod(type, visiting).or(() -> factoryFunction(type, visiting))
                .or(() -> doubles ? testDouble(type, visiting) : Optional.empty());
    }

    /**
     * A test double of a non-generic, non-sealed abstraction: an anonymous subclass (with the constructor values of an
     * abstract class) whose methods throw {@code UnsupportedOperationException}.
     */
    private Optional<String> testDouble(final Type.DeclaredType type, final Set<QualifiedName> visiting) {
        final TypeDefinition definition = context.model().definition(type);
        if (!definition.typeParameters().isEmpty() || visiting.contains(definition.name())
                || !InterfaceGenerator.permittedSubtypes(definition).isEmpty()) {
            return Optional.empty();
        }
        final Set<QualifiedName> nested = new HashSet<>(visiting);
        nested.add(definition.name());
        final boolean abstractClass = context.isClass(definition.name());
        final List<String> arguments = new ArrayList<>();
        if (abstractClass) {
            for (final FieldDefinition parameter : ClassGenerator.constructorParameters(definition.fields())) {
                final Optional<String> value = parameter.hasAnnotation("nullable") ? Optional.of("null")
                        : value(parameter.type(), parameter.annotations(), 0, nested);
                if (value.isEmpty()) {
                    return Optional.empty();
                }
                arguments.add(value.get());
            }
        }
        final String indent = "            ";
        final String name = imports.use(JavaNames.packageName(definition.name().namespace()), definition.name().name());
        final String failure = "throw new UnsupportedOperationException(\"test double\");";
        final StringBuilder body = new StringBuilder();
        if (!abstractClass) {
            for (final FieldDefinition field : definition.fields()) {
                final String declaration = JavaTypes.declaration(field.type(), field.hasAnnotation("nullable"),
                        context.boxed(definition.name(), field.name()), imports);
                final String accessor = JavaKeywords.identifier(field.name());
                body.append(indent).append("@Override\n").append(indent).append("public ").append(declaration)
                        .append(' ').append(accessor).append("() {\n").append(indent).append("    ").append(failure)
                        .append("\n").append(indent).append("}\n");
                if (!field.hasAnnotation("immutable")) {
                    body.append(indent).append("@Override\n").append(indent).append("public ").append(name)
                            .append(' ').append(InterfaceGenerator.setter(field.name())).append("(final ")
                            .append(declaration).append(' ').append(accessor).append(") {\n").append(indent)
                            .append("    ").append(failure).append("\n").append(indent).append("}\n");
                }
                deprecated |= field.hasAnnotation("deprecated");
            }
        }
        for (final MethodDefinition method : definition.methods()) {
            if (method.isStatic() || method.hasAnnotation("finalMethod")) {
                continue;
            }
            deprecated |= method.hasAnnotation("deprecated");
            final String stub = JavaMembers.method(definition.name(), method, context, imports, JavaMembers.Body.STUB);
            stub.lines().filter(l -> !l.stripLeading().startsWith("///")).forEach(line -> body.append(indent)
                    .append(line.startsWith("    ") ? line.substring(4) : line).append('\n'));
        }
        deprecated |= definition.hasAnnotation("deprecated");
        return Optional.of("new " + name + "(" + String.join(", ", arguments) + ") {\n" + body.toString()
                .replaceAll("throw new UnsupportedOperationException\\(\"Not implemented yet: [^\"]*\"\\);",
                        failure) + "        }");
    }

    /**
     * A value of an abstraction without a usable concrete subtype: the result of one of its static methods that
     * return it (e.g. {@code TransactionId.generateTransactionId(accountId)}); methods without {@code @@throws} and
     * with fewer parameters first. Until the method is implemented, the tests that use the value fail.
     */
    private Optional<String> factoryMethod(final Type.DeclaredType type, final Set<QualifiedName> visiting) {
        final TypeDefinition definition = context.model().definition(type);
        if (!definition.typeParameters().isEmpty()) {
            return Optional.empty();
        }
        final String owner = imports.use(JavaNames.packageName(definition.name().namespace()), definition.name().name());
        return call(type, definition.methods().stream().filter(MethodDefinition::isStatic).toList(), m -> owner,
                visiting);
    }

    /**
     * A value from a namespace-level function that returns the type (e.g.
     * {@code ClientFactory.createClient(networkSettings, operatorAccount)}), from a module the test can use.
     */
    private Optional<String> factoryFunction(final Type.DeclaredType type, final Set<QualifiedName> visiting) {
        // in the order of the model: the candidates must not depend on hash codes (enums hash by identity)
        final Map<MethodDefinition, String> owners = new java.util.LinkedHashMap<>();
        for (final FunctionDefinition function : functions) {
            final QualifiedName factory = new QualifiedName(function.namespace(),
                    FactoryGenerator.className(function.namespace()));
            if (visible.test(factory)) {
                owners.put(function.method(), imports.use(JavaNames.packageName(function.namespace()),
                        factory.name()));
            }
        }
        return call(type, List.copyOf(owners.keySet()), owners::get, visiting);
    }

    /** A stable tie-breaker: the declaring type of a method, the source location of a function. */
    private static String origin(final MethodDefinition method) {
        return method.declaringType() == null ? method.location().toString() : method.declaringType().toString();
    }

    /**
     * Calls the first of the static methods or functions that returns the type and whose arguments can be built:
     * methods without {@code @@throws} and with fewer parameters first. Until the method is implemented, the tests
     * that use the value fail.
     */
    private Optional<String> call(final Type.DeclaredType type, final List<MethodDefinition> candidates,
                                  final java.util.function.Function<MethodDefinition, String> owner,
                                  final Set<QualifiedName> visiting) {
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
                        && returned.name().equals(definition.name()) && fits(Optional.of(returned), type))
                .filter(m -> m.annotation("throws").stream().flatMap(t -> t.arguments().stream())
                        .noneMatch(id -> context.exception(id.text()).checked()))
                .sorted(Comparator.comparing((MethodDefinition m) -> m.hasAnnotation("throws") ? 1 : 0)
                        .thenComparing(m -> m.parameters().size())
                        .thenComparing(MethodDefinition::signature)
                        .thenComparing(JavaSamples::origin))
                .toList();
        for (final MethodDefinition method : methods) {
            final List<String> arguments = new ArrayList<>();
            for (final ParameterDefinition parameter : method.parameters()) {
                final Optional<String> value = parameter.hasAnnotation("nullable") ? Optional.of("null")
                        : value(parameter.type(), parameter.annotations(), 0, nested);
                if (value.isEmpty()) {
                    break;
                }
                arguments.add(value.get());
            }
            if (arguments.size() == method.parameters().size()) {
                deprecated |= method.hasAnnotation("deprecated") || definition.hasAnnotation("deprecated");
                return Optional.of(owner.apply(method) + "." + JavaKeywords.identifier(method.name()) + "("
                        + String.join(", ", arguments) + ")");
            }
        }
        return Optional.empty();
    }

    /** Whether the default instance (e.g. of {@code HieroClient<ANY>}) can be used where the type is required. */
    private static boolean fitsInstance(final Type.DeclaredType instance, final Type.DeclaredType required) {
        for (int i = 0; i < required.arguments().size(); i++) {
            final Type argument = required.arguments().get(i);
            if (!(argument instanceof Type.WildcardType) && !(argument instanceof Type.AnyType)
                    && (i >= instance.arguments().size() || !argument.text().equals(instance.arguments().get(i)
                    .text()))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Renders an expression of a default instance as Java expression.
     *
     * @param expression the expression
     * @param visiting   the types being created (cycle detection)
     * @return the Java expression, empty if a part has no Java form (a function that is not generated, a call that
     *         declares a checked exception, ...)
     */
    private Optional<String> render(final InstanceExpression expression, final Set<QualifiedName> visiting) {
        return switch (expression) {
            case InstanceExpression.Default value -> value(value.type(), List.of(), 0, visiting);
            case InstanceExpression.Construct construct -> renderConstruct(construct, visiting);
            case InstanceExpression.Call call -> renderCall(call, visiting);
            case InstanceExpression.MethodCall call -> checked(call.method()) ? Optional.empty()
                    : render(call.target(), visiting).flatMap(target -> renderArguments(call.method(),
                    call.arguments(), visiting).map(arguments -> target + "."
                    + JavaKeywords.identifier(call.method().name()) + "(" + arguments + ")"));
            case InstanceExpression.Access access -> render(access.target(), visiting)
                    .map(target -> target + "." + JavaKeywords.identifier(access.field().name()) + "()");
            case InstanceExpression.Value value -> Optional.of(renderValue(value));
            case InstanceExpression.ConstantValue constant -> Optional.of(imports.use(JavaNames.packageName(
                    constant.constant().name().namespace()), ConstantsGenerator.className(constant.constant().name()
                    .namespace())) + "." + constant.constant().name().name());
            case InstanceExpression.ListValue list -> renderList(list, visiting);
        };
    }

    private String renderValue(final InstanceExpression.Value value) {
        final Literal literal = value.literal();
        if (value.type() instanceof Type.BasicType || value.type() instanceof Type.DeclaredType) {
            try {
                return JavaLiterals.expression(literal, value.type(), imports, context);
            } catch (final JavaTypes.UnsupportedTypeException e) {
                // a literal for a type without Java literal: rendered like for ANY
            }
        }
        return literal instanceof Literal.StringLiteral string ? JavaLiterals.quote(string.value()) : literal.text();
    }

    private Optional<String> renderList(final InstanceExpression.ListValue list, final Set<QualifiedName> visiting) {
        final BuiltinType builtin = ((Type.BasicType) list.type()).builtin();
        if (builtin.category() == BuiltinType.Category.BYTES) {
            return Optional.of(list.items().isEmpty() ? "new byte[0]" : "new byte[] {" + list.items().stream()
                    .map(i -> "(byte) " + ((InstanceExpression.Value) i).literal().text())
                    .collect(java.util.stream.Collectors.joining(", ")) + "}");
        }
        final List<String> items = new ArrayList<>();
        for (final InstanceExpression item : list.items()) {
            final Optional<String> value = render(item, visiting);
            if (value.isEmpty()) {
                return Optional.empty();
            }
            items.add(value.get());
        }
        return Optional.of(imports.use("java.util", builtin.name().equals("set") ? "Set" : "List") + ".of("
                + String.join(", ", items) + ")");
    }

    private Optional<String> renderConstruct(final InstanceExpression.Construct construct,
                                             final Set<QualifiedName> visiting) {
        final TypeDefinition definition = context.model().definition(construct.type());
        if (!context.isGenerated(definition.name())) {
            return Optional.empty();
        }
        final List<FieldDefinition> parameters = constructorParameters(definition);
        final List<String> arguments = new ArrayList<>();
        for (final FieldDefinition parameter : parameters) {
            final InstanceExpression value = construct.values().get(parameter.name());
            if (value != null) {
                final Optional<String> rendered = render(value, visiting);
                if (rendered.isEmpty()) {
                    return Optional.empty();
                }
                arguments.add(rendered.get());
            } else if (parameter.hasAnnotation("default")) {
                arguments.add(JavaLiterals.expression(parameter.annotation("default").orElseThrow().arguments()
                        .getFirst(), context.model().substitute(construct.type(), parameter.type()), imports, context));
            } else {
                arguments.add("null");
            }
        }
        final StringBuilder java = new StringBuilder("new ").append(imports.use(JavaNames.packageName(
                definition.name().namespace()), definition.name().name()))
                .append(definition.typeParameters().isEmpty() ? "" : "<>").append('(')
                .append(String.join(", ", arguments)).append(')');
        // attributes that the constructor does not take are set with their setters
        for (final Map.Entry<String, InstanceExpression> value : construct.values().entrySet()) {
            if (parameters.stream().noneMatch(p -> p.name().equals(value.getKey()))) {
                final Optional<String> rendered = render(value.getValue(), visiting);
                if (rendered.isEmpty()) {
                    return Optional.empty();
                }
                java.append('.').append(InterfaceGenerator.setter(value.getKey())).append('(')
                        .append(rendered.get()).append(')');
            }
        }
        deprecated |= definition.hasAnnotation("deprecated");
        return Optional.of(java.toString());
    }

    private Optional<String> renderCall(final InstanceExpression.Call call, final Set<QualifiedName> visiting) {
        if (checked(call.method())) {
            return Optional.empty();
        }
        final String owner;
        if (call.owner() != null) {
            if (!context.isGenerated(call.owner()) || !visible.test(call.owner())) {
                return Optional.empty();
            }
            owner = imports.use(JavaNames.packageName(call.owner().namespace()), call.owner().name());
        } else {
            final QualifiedName factory = new QualifiedName(call.namespace(),
                    FactoryGenerator.className(call.namespace()));
            if (functions.stream().noneMatch(f -> f.method().equals(call.method())) || !visible.test(factory)) {
                return Optional.empty();
            }
            owner = imports.use(JavaNames.packageName(call.namespace()), factory.name());
        }
        deprecated |= call.method().hasAnnotation("deprecated");
        return renderArguments(call.method(), call.arguments(), visiting)
                .map(arguments -> owner + "." + JavaKeywords.identifier(call.method().name()) + "(" + arguments + ")");
    }

    /** The arguments in parameter order; {@code null} (with the parameter type) for parameters without value. */
    private Optional<String> renderArguments(final MethodDefinition method,
                                             final Map<String, InstanceExpression> arguments,
                                             final Set<QualifiedName> visiting) {
        final List<String> result = new ArrayList<>();
        for (final ParameterDefinition parameter : method.parameters()) {
            final InstanceExpression value = arguments.get(parameter.name());
            if (value == null) {
                if (!parameter.varargs()) {
                    result.add("(" + JavaTypes.type(parameter.type(), true, imports) + ") null");
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

    /** Whether the method declares a checked exception (a test value cannot be built in an expression then). */
    private boolean checked(final MethodDefinition method) {
        return method.annotation("throws").stream().flatMap(t -> t.arguments().stream())
                .anyMatch(id -> context.exception(id.text()).checked());
    }

    private static boolean fits(final Optional<Type.DeclaredType> actual, final Type.DeclaredType required) {
        if (actual.isEmpty() || actual.get().arguments().size() != required.arguments().size()) {
            return false;
        }
        for (int i = 0; i < required.arguments().size(); i++) {
            final Type argument = required.arguments().get(i);
            if (!(argument instanceof Type.WildcardType) && !(argument instanceof Type.AnyType)
                    && !argument.text().equals(actual.get().arguments().get(i).text())) {
                return false;
            }
        }
        return true;
    }

    /** The supertype {@code target} as seen from {@code type}, with the type arguments substituted. */
    private Optional<Type.DeclaredType> supertypeAs(final Type.DeclaredType type, final QualifiedName target,
                                                    final Set<QualifiedName> visited) {
        if (type.name().equals(target)) {
            return Optional.of(type);
        }
        if (!visited.add(type.name()) || context.model().type(type.name()).isEmpty()) {
            return Optional.empty();
        }
        for (final Type supertype : context.model().definition(type).supertypes()) {
            if (supertype instanceof Type.DeclaredType declared) {
                final Optional<Type.DeclaredType> result = supertypeAs(
                        (Type.DeclaredType) context.model().substitute(type, declared), target, visited);
                if (result.isPresent()) {
                    return result;
                }
            }
        }
        return Optional.empty();
    }

    private Optional<String> lambda(final Type.FunctionType function, final Set<QualifiedName> visiting) {
        final List<ParameterDefinition> parameters = function.parameters();
        final String names = parameters.size() == 1 ? "p0" : "(" + java.util.stream.IntStream
                .range(0, parameters.size()).mapToObj(i -> "p" + i).collect(Collectors.joining(", ")) + ")";
        if (function.returnType() instanceof Type.VoidType) {
            return Optional.of(names + " -> { }");
        }
        return value(function.returnType(), List.of(), 0, visiting).map(v -> names + " -> " + v);
    }

    /**
     * The numeric argument of a {@code @@min}/{@code @@max}.
     *
     * @param annotations the annotations
     * @param name        {@code min} or {@code max}
     * @return the bound
     */
    static Optional<BigDecimal> bound(final List<Annotation> annotations, final String name) {
        return annotations.stream().filter(a -> a.name().equals(name)).findFirst()
                .map(a -> a.arguments().getFirst())
                .filter(Literal.NumberLiteral.class::isInstance)
                .map(l -> ((Literal.NumberLiteral) l).value());
    }

    /**
     * The integer argument of a size or length annotation.
     *
     * @param annotations the annotations
     * @param name        e.g. {@code minSize}
     * @return the value
     */
    static Optional<Integer> size(final List<Annotation> annotations, final String name) {
        return bound(annotations, name).map(BigDecimal::intValueExact);
    }

    /**
     * The string argument of an annotation.
     *
     * @param annotations the annotations
     * @param name        e.g. {@code pattern}
     * @return the value
     */
    static Optional<String> stringArgument(final List<Annotation> annotations, final String name) {
        return annotations.stream().filter(a -> a.name().equals(name)).findFirst()
                .map(a -> a.arguments().getFirst())
                .map(l -> l instanceof Literal.StringLiteral string ? string.value() : l.text());
    }

    private static String suffix(final int variant) {
        return variant == 0 ? "" : String.valueOf(variant);
    }

    private static boolean containsVariable(final @Nullable Type type) {
        return switch (type) {
            case null -> false;
            case Type.TypeVariable ignored -> true;
            case Type.BasicType basic -> basic.arguments().stream().anyMatch(JavaSamples::containsVariable);
            case Type.DeclaredType declared -> declared.arguments().stream().anyMatch(JavaSamples::containsVariable);
            case Type.WildcardType wildcard -> containsVariable(wildcard.upperBound());
            case Type.FunctionType function -> containsVariable(function.returnType())
                    || function.parameters().stream().anyMatch(p -> containsVariable(p.type()));
            default -> false;
        };
    }
}
