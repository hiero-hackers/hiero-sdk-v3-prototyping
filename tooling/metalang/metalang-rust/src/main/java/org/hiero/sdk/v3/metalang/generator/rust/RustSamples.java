package org.hiero.sdk.v3.metalang.generator.rust;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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
 * Builds valid values for the generated Rust tests, like the Java and TypeScript counterparts: the default instance of
 * a type first, then values within the validation annotations of the type, enum constants, structs created with
 * {@code new}, a generated type for a trait, a static method or function that returns the type, and for values that
 * are only stored a test double (a struct of the test file that implements the trait). The values are deterministic;
 * the {@code variant} gives different values of the same type.
 */
final class RustSamples {

    private final RustContext context;
    private final RustImports imports;
    private final Predicate<String> visibleFolder;
    /** the test doubles of the file: name → declaration */
    private final Map<String, String> doubles = new LinkedHashMap<>();
    private boolean allowDoubles;

    RustSamples(final RustContext context, final RustImports imports, final Predicate<String> visibleFolder) {
        this.context = context;
        this.imports = imports;
        this.visibleFolder = visibleFolder;
    }

    Map<String, String> doubles() {
        return doubles;
    }

    private boolean visible(final String namespace) {
        return visibleFolder.test(context.folder(namespace));
    }

    /**
     * Returns a valid value.
     *
     * @param type        the type, without type variables
     * @param annotations the annotations of the attribute or parameter
     * @param variant     selects one of several values
     * @param stored      whether a trait without implementation may be represented by a test double (only for
     *                    values that are stored, never for arguments of methods)
     * @return the Rust expression of the value in the Rust form of the type, empty if no value can be built
     */
    Optional<String> value(final Type type, final List<Annotation> annotations, final int variant,
                           final boolean stored) {
        allowDoubles = stored;
        try {
            return value(type, annotations, variant, Set.of());
        } finally {
            allowDoubles = false;
        }
    }

    /** The Rust form of a type of the tests (no type variables). */
    RustType rustType(final Type type) {
        return context.rustType(type, Map.of());
    }

    private Optional<String> value(final Type type, final List<Annotation> annotations, final int variant,
                                   final Set<QualifiedName> visiting) {
        // the types of the tests have no type variables: they are replaced by type arguments
        return switch (type) {
            case Type.BasicType basic -> basic(basic, annotations, variant, visiting);
            case Type.DeclaredType declared -> declared(declared, variant, visiting);
            case Type.FunctionType function -> lambda(function, visiting);
            case Type.WildcardType ignored -> any(variant);
            case Type.AnyType ignored -> any(variant);
            default -> Optional.empty();
        };
    }

    private Optional<String> any(final int variant) {
        return Optional.of(arc() + "::new(" + RustLiterals.quote("value" + suffix(variant)) + ".to_string()) as "
                + new RustType.AnyValue().render(imports));
    }

    private String arc() {
        return imports.external("std::sync::Arc");
    }

    private Optional<String> basic(final Type.BasicType type, final List<Annotation> annotations, final int variant,
                                   final Set<QualifiedName> visiting) {
        final BuiltinType builtin = type.builtin();
        return switch (builtin.category()) {
            case INTEGER -> integer(builtin, annotations, variant);
            case FLOAT, DECIMAL -> Constraints.decimal(annotations, variant)
                    .map(v -> RustLiterals.number(v, builtin));
            case BOOL -> Optional.of(variant % 2 == 0 ? "true" : "false");
            case STRING -> Constraints.string(annotations, variant).map(s -> RustLiterals.quote(s) + ".to_string()");
            case BYTES -> {
                final int size = Constraints.elementCount(annotations, 3);
                yield size < 0 ? Optional.empty() : Optional.of(bytes(size, variant));
            }
            case COLLECTION -> collection(type, annotations, variant, visiting);
            case MAP -> map(type, annotations, variant, visiting);
            case TYPE -> Optional.of("std::any::TypeId::of::<" + typeArgument(type) + ">()");
            case UUID -> Optional.of("uuid::Uuid::from_u128(" + (variant + 1) + ")");
            case TEMPORAL -> Optional.of(temporal(builtin, variant));
            case DURATION -> duration(builtin, annotations, variant);
            case STREAM_RESULT -> Optional.empty();
        };
    }

    private Optional<String> integer(final BuiltinType builtin, final List<Annotation> annotations,
                                     final int variant) {
        // the validator rejects contradictory bounds: the range is never empty
        final IntegerRange range = Constraints.range(builtin, annotations);
        final BigInteger value = BigInteger.valueOf(1L + variant).max(range.min()).min(range.max());
        return Optional.of(RustLiterals.number(new BigDecimal(value), builtin));
    }

    static String bytes(final int size, final int variant) {
        return "vec![" + IntStream.range(0, size).mapToObj(i -> String.valueOf((variant + i) % 100 + 1))
                .collect(Collectors.joining(", ")) + "]";
    }

    private static String temporal(final BuiltinType builtin, final int variant) {
        final String day = String.valueOf(variant % 28 + 1);
        return switch (builtin.name()) {
            case "date" -> "chrono::NaiveDate::from_ymd_opt(2024, 1, " + day + ").expect(\"valid date\")";
            case "time" -> "chrono::NaiveTime::from_hms_opt(12, 0, " + (variant % 60) + ").expect(\"valid time\")";
            case "dateTime" -> "chrono::NaiveDate::from_ymd_opt(2024, 1, " + day
                    + ").and_then(|d| d.and_hms_opt(12, 0, 0)).expect(\"valid date and time\")";
            default -> "chrono::DateTime::parse_from_rfc3339(\"2024-01-" + String.format(java.util.Locale.ROOT,
                    "%02d", variant % 28 + 1) + "T12:00:00Z\").expect(\"valid date and time\")";
        };
    }

    private Optional<String> duration(final BuiltinType builtin, final List<Annotation> annotations,
                                      final int variant) {
        final Optional<BigDecimal> bound = Constraints.bound(annotations, "min")
                .or(() -> Constraints.bound(annotations, "max"));
        if (bound.isPresent()) {
            return Optional.of(RustLiterals.number(bound.get(), builtin));
        }
        return Optional.of("std::time::Duration::from_secs(" + (variant + 1) + ")");
    }

    /** The type argument of {@code std::any::TypeId::of}: a generated type of {@code type<T>}, else {@code ()}. */
    private String typeArgument(final Type.BasicType type) {
        if (!type.arguments().isEmpty() && type.arguments().getFirst() instanceof Type.DeclaredType declared
                && context.isGenerated(declared.name()) && visible(declared.name().namespace())
                && declared.arguments().isEmpty() && !context.isTrait(declared.name())) {
            return rustType(declared).render(imports);
        }
        return "()";
    }

    private Optional<String> collection(final Type.BasicType type, final List<Annotation> annotations,
                                        final int variant, final Set<QualifiedName> visiting) {
        final int size = Constraints.elementCount(annotations, 1);
        if (size < 0) {
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
        final boolean set = type.builtin().name().equals("set");
        if (set && new HashSet<>(elements).size() < elements.size()) {
            return Optional.empty();
        }
        return Optional.of(rustType(type) instanceof RustType.SetOf ? imports.external("std::collections::HashSet")
                + "::from([" + String.join(", ", elements) + "])" : "vec![" + String.join(", ", elements) + "]");
    }

    private Optional<String> map(final Type.BasicType type, final List<Annotation> annotations, final int variant,
                                 final Set<QualifiedName> visiting) {
        final int size = Constraints.elementCount(annotations, 1);
        if (size < 0) {
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
            entries.add("(" + key.get() + ", " + value.get() + ")");
        }
        if (new HashSet<>(keys).size() < keys.size()) {
            return Optional.empty();
        }
        return Optional.of(rustType(type) instanceof RustType.MapOf ? imports.external("std::collections::HashMap")
                + "::from([" + String.join(", ", entries) + "])" : "vec![" + String.join(", ", entries) + "]");
    }

    private Optional<String> lambda(final Type.FunctionType function, final Set<QualifiedName> visiting) {
        // the cast (`as`) turns the closure into the function type also where Rust does not coerce (in tuples)
        final RustType type = rustType(function);
        final String parameters = ((RustType.Function) type).parameters().stream()
                .map(p -> "_: " + p.render(imports)).collect(Collectors.joining(", "));
        if (function.returnType() instanceof Type.VoidType) {
            return Optional.of(arc() + "::new(|" + parameters + "| {}) as " + type.render(imports));
        }
        return value(function.returnType(), List.of(), 0, visiting)
                .map(v -> arc() + "::new(move |" + parameters + "| " + v + ") as " + type.render(imports));
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
            final Optional<String> value = render(instance.get().expression(), rustType(type), nested);
            if (value.isPresent()) {
                return value;
            }
        }
        return switch (definition) {
            case TypeDefinition.EnumDefinition enumType -> constant(enumType, variant);
            case TypeDefinition.ComplexTypeDefinition complex when context.isSealed(complex.name()) ->
                    sealed(complex, variant, visiting);
            case TypeDefinition.ComplexTypeDefinition complex when complex.abstraction() ->
                    implementation(type, variant, visiting);
            case TypeDefinition.ComplexTypeDefinition complex -> construct(complex, type, variant, visiting)
                    .or(() -> factory(type, visiting));
        };
    }

    private Optional<String> constant(final TypeDefinition.EnumDefinition enumType, final int variant) {
        final List<EnumValueDefinition> current = enumType.values().stream()
                .filter(v -> !v.hasAnnotation("deprecated")).toList();
        final List<EnumValueDefinition> values = current.isEmpty() ? enumType.values() : current;
        if (values.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(imports.type(enumType.name()) + "::"
                + RustNames.variant(values.get(variant % values.size()).name()));
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
        final Map<Type.TypeVariable, RustType> scope = context.implScope(definition, new Type.DeclaredType(
                definition.name(), definition.typeParameters().stream().map(p -> arguments.get().get(p.variable()))
                .toList()), Map.of());
        int index = 0;
        for (final FieldDefinition field : definition.fields()) {
            final Type fieldType = LinkedModel.substitute(field.type(), arguments.get());
            final RustType expected = RustMembers.storage(field, scope, context);
            if (field.hasAnnotation("default")) {
                values.add(RustLiterals.expression(field.annotation("default").orElseThrow().arguments().getFirst(),
                        fieldType, field.hasAnnotation("nullable"), imports));
                continue;
            }
            if (field.hasAnnotation("nullable")) {
                values.add("None");
                continue;
            }
            final Optional<String> value = value(fieldType, field.annotations(), index++ == 0 ? variant : 0, nested)
                    .flatMap(v -> RustTypeGenerator.convert(v, rustType(fieldType), expected, false, imports));
            if (value.isEmpty()) {
                return Optional.empty();
            }
            values.add(value.get());
        }
        return Optional.of(imports.type(definition.name()) + "::new(" + String.join(", ", values) + ")"
                + (context.isFallible(definition.name()) ? ".expect(\"valid value\")" : ""));
    }

    private Optional<String> sealed(final TypeDefinition.ComplexTypeDefinition type, final int variant,
                                    final Set<QualifiedName> visiting) {
        for (final QualifiedName name : context.sealedVariants(type)) {
            if (!context.isGenerated(name) || !visible(name.namespace())) {
                continue;
            }
            final Optional<String> value = value(new Type.DeclaredType(name, List.of()), List.of(), variant,
                    visiting);
            if (value.isPresent()) {
                return Optional.of(imports.type(type.name()) + "::from(" + value.get() + ")");
            }
        }
        return staticFunction(new Type.DeclaredType(type.name(), List.of()), visiting)
                .or(() -> factory(new Type.DeclaredType(type.name(), List.of()), visiting));
    }

    /** A value of a trait: a generated type, a static function, a namespace function, a test double. */
    private Optional<String> implementation(final Type.DeclaredType type, final int variant,
                                            final Set<QualifiedName> visiting) {
        final boolean generic = !context.keptArguments(type, Map.of()).isEmpty();
        final List<TypeDefinition> candidates = context.model().types().stream()
                .filter(t -> context.isGenerated(t.name()) && !t.name().equals(type.name())
                        && visible(t.name().namespace()))
                .filter(t -> !(t instanceof TypeDefinition.ComplexTypeDefinition c && c.abstraction()))
                .filter(t -> t.typeParameters().isEmpty())
                .filter(t -> context.model().isSubtypeOf(t.name(), type.name()))
                .sorted(Comparator.comparing((TypeDefinition t) -> t instanceof TypeDefinition.EnumDefinition ? 0 : 1)
                        .thenComparing(t -> t.name().toString()))
                .toList();
        if (!generic) {
            for (final TypeDefinition candidate : candidates) {
                final Optional<String> value = switch (candidate) {
                    case TypeDefinition.EnumDefinition enumType -> constant(enumType, variant);
                    case TypeDefinition.ComplexTypeDefinition complex ->
                            construct(complex, new Type.DeclaredType(complex.name(), List.of()), variant, visiting);
                };
                if (value.isPresent()) {
                    return Optional.of(arc() + "::new(" + value.get() + ") as " + rustType(type).render(imports));
                }
            }
        }
        return staticFunction(type, visiting).or(() -> factory(type, visiting))
                .or(() -> allowDoubles ? Optional.of(arc() + "::new(" + testDouble(type) + ") as "
                        + rustType(type).render(imports)) : Optional.empty());
    }

    private Optional<String> staticFunction(final Type.DeclaredType type, final Set<QualifiedName> visiting) {
        final TypeDefinition definition = context.model().definition(type);
        if (!definition.typeParameters().isEmpty()) {
            return Optional.empty();
        }
        final String owner = context.isTrait(type.name()) ? "<dyn " + imports.type(type.name()) + ">"
                : imports.type(type.name());
        return call(type, definition.declaredMethods().stream().filter(MethodDefinition::isStatic).toList(),
                m -> owner + "::" + context.methodName(m), visiting);
    }

    private Optional<String> factory(final Type.DeclaredType type, final Set<QualifiedName> visiting) {
        final Map<MethodDefinition, String> owners = new LinkedHashMap<>();
        for (final FunctionDefinition function : context.functions()) {
            if (visible(function.namespace())) {
                owners.put(function.method(), function.namespace());
            }
        }
        return call(type, List.copyOf(owners.keySet()),
                m -> imports.function(owners.get(m), context.methodName(m)), visiting);
    }

    /**
     * Calls the first static method or function that returns the type and whose arguments can be built (without
     * {@code @@throws} and with fewer parameters first).
     */
    private Optional<String> call(final Type.DeclaredType type, final List<MethodDefinition> candidates,
                                  final java.util.function.Function<MethodDefinition, String> target,
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
                        && returned.name().equals(definition.name()))
                .sorted(Comparator.comparing((MethodDefinition m) -> m.hasAnnotation("throws") ? 1 : 0)
                        .thenComparing(m -> m.parameters().size())
                        .thenComparing(MethodDefinition::signature))
                .toList();
        for (final MethodDefinition method : methods) {
            final List<String> arguments = new ArrayList<>();
            for (final ParameterDefinition parameter : method.parameters()) {
                final Optional<String> value = parameter.varargs() ? Optional.of("vec![]")
                        : parameter.hasAnnotation("nullable") ? Optional.of("None")
                        : value(parameter.type(), parameter.annotations(), 0, nested);
                if (value.isEmpty()) {
                    break;
                }
                arguments.add(value.get());
            }
            if (arguments.size() == method.parameters().size()) {
                return Optional.of(target.apply(method) + "(" + String.join(", ", arguments) + ")"
                        + (method.hasAnnotation("throws") ? ".expect(\"valid value\")" : ""));
            }
        }
        return Optional.empty();
    }

    // --- test doubles ----------------------------------------------------------------------------

    /**
     * Returns the test double of a trait: a unit struct of the test file that implements the trait and its
     * supertraits; every method panics.
     *
     * @param type the trait with its type arguments
     * @return the expression that creates the double
     */
    String testDouble(final Type.DeclaredType type) {
        final String base = type.name().name() + "Double";
        final String key = type.text();
        for (final Map.Entry<String, String> existing : doubles.entrySet()) {
            if (existing.getValue().startsWith("// " + key + "\n")) {
                return existing.getKey();
            }
        }
        String name = base;
        for (int i = 2; doubles.containsKey(name); i++) {
            name = base + i;
        }
        imports.reserve(name);
        doubles.put(name, ""); // reserved while the declaration is built
        final StringBuilder out = new StringBuilder("// ").append(key).append('\n');
        out.append("#[derive(Debug)]\nstruct ").append(name).append(";\n");
        final TypeDefinition definition = context.model().definition(type);
        final Map<Type.TypeVariable, RustType> scope = context.implScope(definition, type, Map.of());
        out.append('\n').append(doubleImpl(name, definition, type, Map.of()));
        final List<Type.DeclaredType> traits = new ArrayList<>(List.of(type));
        for (final Type.DeclaredType trait : context.traits(definition)) {
            if (context.isGenerated(trait.name())) {
                traits.add(trait);
                out.append('\n').append(doubleImpl(name, context.model().definition(trait), trait, scope));
            }
        }
        if (traits.stream().anyMatch(t -> context.model().definition(t).declaredMethods().stream()
                .anyMatch(RustContext::isDisplay))) {
            out.append("\nimpl ").append(imports.external("std::fmt::Display")).append(" for ").append(name)
                    .append(" {\n").append(RustTypeGenerator.INDENT)
                    .append("fn fmt(&self, _: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {\n")
                    .append(RustTypeGenerator.INDENT).append(RustTypeGenerator.INDENT)
                    .append("unimplemented!(\"test double\")\n").append(RustTypeGenerator.INDENT).append("}\n}\n");
        }
        doubles.put(name, out.toString());
        return name;
    }

    private String doubleImpl(final String name, final TypeDefinition definition, final Type.DeclaredType trait,
                              final Map<Type.TypeVariable, RustType> outer) {
        final Map<Type.TypeVariable, RustType> scope = context.implScope(definition, trait, outer);
        final String traitType = new RustType.Dyn(trait.name(), context.keptArguments(trait, outer))
                .render(imports).replaceFirst("^.*?<dyn ", "").replaceFirst(">$", "");
        final String indent = RustTypeGenerator.INDENT;
        final List<String> members = new ArrayList<>();
        for (final FieldDefinition field : context.traitFields(definition)) {
            members.add(indent + RustMembers.traitGetter(field, scope, imports) + " {\n" + indent + indent
                    + "unimplemented!(\"test double\")\n" + indent + "}\n");
            if (!field.hasAnnotation("immutable")) {
                members.add(indent + RustMembers.traitSetter(field, scope, imports) + " {\n" + indent + indent
                        + "unimplemented!(\"test double\")\n" + indent + "}\n");
            }
        }
        for (final MethodDefinition method : context.traitMethods(definition)) {
            members.add(RustMembers.render(method, RustMembers.signature(method, context.methodName(method), scope,
                    RustMembers.Kind.TRAIT_IMPL, imports), RustMembers.Kind.TRAIT_IMPL,
                    "unimplemented!(\"test double\")", indent));
        }
        return "#[allow(unused_variables)]\nimpl " + traitType + " for " + name + " {\n" + String.join("\n", members)
                + "}\n";
    }

    // --- default instances -----------------------------------------------------------------------

    /**
     * Renders a default instance expression as a value of a Rust type.
     *
     * @param expression the expression
     * @param expected   the Rust type the value must have
     * @param visiting   the types whose values are being built (against cycles)
     * @return the Rust expression, empty if it cannot be rendered
     */
    private Optional<String> render(final InstanceExpression expression, final RustType expected,
                                    final Set<QualifiedName> visiting) {
        final Optional<String> value = render(expression, visiting);
        if (value.isEmpty()) {
            return value;
        }
        final RustType produced = produced(expression);
        return RustTypeGenerator.convert(value.get(), produced, expected, false, imports);
    }

    /** The Rust type of the value of an expression. */
    private RustType produced(final InstanceExpression expression) {
        return switch (expression) {
            case InstanceExpression.Access access -> {
                final RustType type = rustType(access.type());
                yield access.field().hasAnnotation("nullable") ? new RustType.Optional(type) : type;
            }
            case InstanceExpression.Call call when call.method().hasAnnotation("nullable") ->
                    new RustType.Optional(rustType(call.type()));
            case InstanceExpression.MethodCall call when call.method().hasAnnotation("nullable") ->
                    new RustType.Optional(rustType(call.type()));
            default -> rustType(expression.type());
        };
    }

    private Optional<String> render(final InstanceExpression expression, final Set<QualifiedName> visiting) {
        return switch (expression) {
            case InstanceExpression.Default value -> value(value.type(), List.of(), 0, visiting);
            case InstanceExpression.Construct construct -> construct(construct, visiting);
            case InstanceExpression.Call call -> {
                final String target;
                if (call.owner() != null) {
                    if (!context.isGenerated(call.owner()) || !visible(call.owner().namespace())) {
                        yield Optional.empty();
                    }
                    target = (context.isTrait(call.owner()) ? "<dyn " + imports.type(call.owner()) + ">"
                            : imports.type(call.owner())) + "::" + context.methodName(call.method());
                } else {
                    if (context.functions(call.namespace()).stream().noneMatch(f -> f.method().equals(call.method()))
                            || !visible(call.namespace())) {
                        yield Optional.empty();
                    }
                    target = imports.function(call.namespace(), context.methodName(call.method()));
                }
                yield arguments(call.method(), call.arguments(), visiting)
                        .map(a -> result(call.method(), target + "(" + a + ")"));
            }
            case InstanceExpression.MethodCall call -> {
                final RustType targetType = produced(call.target());
                if (targetType instanceof RustType.Dyn) {
                    imports.traitInScope(call.method().declaringType());
                }
                yield render(call.target(), visiting).flatMap(target -> arguments(call.method(), call.arguments(),
                        visiting).map(a -> result(call.method(), target + "." + context.methodName(call.method())
                        + "(" + a + ")")));
            }
            case InstanceExpression.Access access -> {
                final RustType targetType = produced(access.target());
                if (targetType instanceof RustType.Dyn) {
                    imports.traitInScope(access.field().declaringType());
                }
                final RustType storage = produced(access);
                yield render(access.target(), visiting).map(target -> RustTypeGenerator.owned(target + "."
                        + RustNames.member(access.field().name()) + "()", storage, context));
            }
            case InstanceExpression.Value value -> Optional.of(RustLiterals.expression(value.literal(), value.type(),
                    false, imports));
            case InstanceExpression.ConstantValue constant -> {
                final QualifiedName name = constant.constant().name();
                if (!visible(name.namespace())) {
                    yield Optional.empty();
                }
                final String reference = imports.constant(name.namespace(), name.name());
                final RustType type = rustType(constant.type());
                yield Optional.of(type instanceof RustType.Text ? reference + ".to_string()"
                        : type instanceof RustType.Primitive || type instanceof RustType.Enum ? reference
                        : "(*" + reference + ").clone()");
            }
            case InstanceExpression.ListValue list -> {
                final Type.BasicType type = (Type.BasicType) list.type();
                final RustType element = type.builtin().category() == BuiltinType.Category.BYTES
                        ? new RustType.Primitive("u8", true, true, true) : rustType(type.arguments().getFirst());
                final List<String> items = new ArrayList<>();
                for (final InstanceExpression item : list.items()) {
                    final Optional<String> rendered = item instanceof InstanceExpression.Value v
                            && type.builtin().category() == BuiltinType.Category.BYTES
                            ? Optional.of(v.literal().text()) : render(item, element, visiting);
                    if (rendered.isEmpty()) {
                        yield Optional.empty();
                    }
                    items.add(rendered.get());
                }
                yield Optional.of(rustType(type) instanceof RustType.SetOf
                        ? imports.external("std::collections::HashSet") + "::from([" + String.join(", ", items) + "])"
                        : "vec![" + String.join(", ", items) + "]");
            }
        };
    }

    private Optional<String> construct(final InstanceExpression.Construct construct,
                                       final Set<QualifiedName> visiting) {
        if (!context.isGenerated(construct.type().name()) || !visible(construct.type().name().namespace())) {
            return Optional.empty();
        }
        final TypeDefinition definition = context.model().definition(construct.type());
        final Map<Type.TypeVariable, RustType> scope = context.implScope(definition, construct.type(), Map.of());
        final List<String> values = new ArrayList<>();
        for (final FieldDefinition field : definition.fields()) {
            final Type fieldType = context.model().substitute(construct.type(), field.type());
            final RustType expected = RustMembers.storage(field, scope, context);
            final InstanceExpression value = construct.values().get(field.name());
            final Optional<String> rendered;
            if (value != null) {
                rendered = render(value, expected, visiting);
            } else if (field.hasAnnotation("default")) {
                rendered = Optional.of(RustLiterals.expression(field.annotation("default").orElseThrow().arguments()
                        .getFirst(), fieldType, field.hasAnnotation("nullable"), imports));
            } else if (field.hasAnnotation("nullable")) {
                rendered = Optional.of("None");
            } else {
                rendered = value(fieldType, field.annotations(), 0, visiting).flatMap(v -> RustTypeGenerator.convert(v,
                        rustType(fieldType), expected, false, imports));
            }
            if (rendered.isEmpty()) {
                return Optional.empty();
            }
            values.add(rendered.get());
        }
        return Optional.of(imports.type(definition.name()) + "::new(" + String.join(", ", values) + ")"
                + (context.isFallible(definition.name()) ? ".expect(\"valid value\")" : ""));
    }

    private String result(final MethodDefinition method, final String call) {
        String result = call;
        if (method.hasAnnotation("async")) {
            result = "crate::helpers::block_on(" + result + ")";
        }
        if (method.hasAnnotation("throws")) {
            result = result + ".expect(\"valid value\")";
        }
        return result;
    }

    private Optional<String> arguments(final MethodDefinition method, final Map<String, InstanceExpression> arguments,
                                       final Set<QualifiedName> visiting) {
        final List<String> result = new ArrayList<>();
        for (final ParameterDefinition parameter : method.parameters()) {
            final InstanceExpression value = arguments.get(parameter.name());
            RustType expected = rustType(parameter.type());
            if (parameter.varargs()) {
                expected = new RustType.VecOf(expected);
            }
            if (parameter.hasAnnotation("nullable")) {
                expected = new RustType.Optional(expected);
            }
            final Optional<String> rendered = value == null
                    ? parameter.varargs() ? Optional.of("vec![]")
                    : parameter.hasAnnotation("nullable") ? Optional.of("None")
                    : value(parameter.type(), parameter.annotations(), 0, visiting)
                    : parameter.varargs() ? render(value, rustType(parameter.type()), visiting).map(v -> "vec![" + v + "]")
                    : render(value, expected, visiting);
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
