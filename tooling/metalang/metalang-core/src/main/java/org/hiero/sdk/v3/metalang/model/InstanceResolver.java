package org.hiero.sdk.v3.metalang.model;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.hiero.sdk.v3.metalang.ast.Expression;
import org.hiero.sdk.v3.metalang.ast.Instance;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.ast.SchemaFile;
import org.hiero.sdk.v3.metalang.ast.TypeRef;
import org.hiero.sdk.v3.metalang.diagnostic.Diagnostic;
import org.hiero.sdk.v3.metalang.diagnostic.DiagnosticCollector;
import org.hiero.sdk.v3.metalang.diagnostic.Rule;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;
import org.hiero.sdk.v3.metalang.generator.IntegerRange;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;
import org.hiero.sdk.v3.metalang.semantic.ResolvedType;
import org.hiero.sdk.v3.metalang.semantic.SpecModel;
import org.jspecify.annotations.Nullable;

/**
 * Resolves the default instances of the specs ({@code ## Default Instances for Testing}) against the linked model: names of types,
 * functions, static methods, methods, attributes, constants and enum constants, the overload of a call (by the names
 * and types of its arguments), and the types of all values. A default instance must be declared in the spec of the
 * namespace that declares its type, at most once per type, and default instances must not depend on each other in a
 * cycle ({@code DEFAULT(A)} in the instance of {@code B} and vice versa).
 */
final class InstanceResolver {

    private final SpecModel spec;
    private final LinkedModel linked;
    private final BiFunction<TypeRef, SchemaFile, Type> types;
    /** The rule of an expression that does not resolve: {@code instance.invalid} or {@code test.invalid}. */
    private final Rule invalid;
    /** The built-in functions of the namespace {@code testing}; empty unless tests are resolved. */
    private final List<FunctionDefinition> builtins;
    /** The variables visible at the expression being resolved (tests only). */
    private Map<String, InstanceExpression.Variable> scope = Map.of();

    /**
     * The resolved default instances and the problems found.
     *
     * @param instances   the default instances that could be resolved
     * @param diagnostics the problems
     */
    record Result(List<InstanceDefinition> instances, List<Diagnostic> diagnostics) {
    }

    private InstanceResolver(final SpecModel spec, final LinkedModel linked,
                             final BiFunction<TypeRef, SchemaFile, Type> types, final Rule invalid,
                             final List<FunctionDefinition> builtins) {
        this.spec = spec;
        this.linked = linked;
        this.types = types;
        this.invalid = invalid;
        this.builtins = List.copyOf(builtins);
    }

    /**
     * Creates a resolver for the expressions of tests: they may use variables, operators and the built-in functions.
     *
     * @param spec     the specs
     * @param linked   the linked model
     * @param types    converts a type reference of a file into a type
     * @param builtins the functions of the namespace {@code testing}
     * @return the resolver
     */
    static InstanceResolver forTests(final SpecModel spec, final LinkedModel linked,
                                     final BiFunction<TypeRef, SchemaFile, Type> types,
                                     final List<FunctionDefinition> builtins) {
        return new InstanceResolver(spec, linked, types, Rule.TEST_INVALID, builtins);
    }

    /**
     * Resolves an expression of a test.
     *
     * @param expression the expression
     * @param expected   the expected type, {@code null} if any type is accepted
     * @param variables  the variables visible at the expression
     * @param file       the spec of the test
     * @param out        receives the problems
     * @return the resolved expression, {@code null} if it does not resolve
     */
    @Nullable InstanceExpression resolve(final Expression expression, final @Nullable Type expected,
                                         final Map<String, InstanceExpression.Variable> variables,
                                         final SchemaFile file, final DiagnosticCollector out) {
        scope = Map.copyOf(variables);
        return expression(expression, expected, file, out);
    }

    /**
     * Converts a type reference of a spec into a type.
     *
     * @param ref  the type reference
     * @param file the spec
     * @return the type
     */
    Type type(final TypeRef ref, final SchemaFile file) {
        return types.apply(ref, file);
    }

    private boolean tests() {
        return invalid == Rule.TEST_INVALID;
    }

    /**
     * Resolves all default instances.
     *
     * @param spec   the specs
     * @param linked the linked model (without default instances)
     * @param types  converts a type reference of a file into a type
     * @return the result
     */
    static Result resolve(final SpecModel spec, final LinkedModel linked,
                          final BiFunction<TypeRef, SchemaFile, Type> types) {
        return new InstanceResolver(spec, linked, types, Rule.INSTANCE_INVALID, List.of()).resolve();
    }

    private Result resolve() {
        final DiagnosticCollector diagnostics = new DiagnosticCollector();
        final Map<QualifiedName, InstanceDefinition> instances = new LinkedHashMap<>();
        for (final SchemaFile file : spec.files()) {
            for (final Instance instance : file.instances()) {
                final Type type = types.apply(instance.type(), file);
                if (!(type instanceof Type.DeclaredType declared) || linked.type(declared.name()).isEmpty()) {
                    diagnostics.report(Rule.INSTANCE_INVALID, "A default instance needs a type declared in the specs, "
                            + "not '" + instance.type().text() + "'", instance.location());
                    continue;
                }
                if (!declared.name().namespace().equals(file.namespace())) {
                    diagnostics.report(Rule.INSTANCE_INVALID, "The default instance of '" + declared.name()
                            + "' belongs to the spec of namespace '" + declared.name().namespace() + "'",
                            instance.location());
                    continue;
                }
                if (instances.containsKey(declared.name())) {
                    diagnostics.report(Rule.INSTANCE_INVALID, "'" + declared.name() + "' already has a default "
                            + "instance (" + instances.get(declared.name()).location() + ")", instance.location());
                    continue;
                }
                final InstanceExpression expression = expression(instance.expression(), declared, file, diagnostics);
                if (expression != null) {
                    instances.put(declared.name(), new InstanceDefinition(declared, expression,
                            instance.documentation(), instance.location()));
                }
            }
        }
        cycles(instances, diagnostics);
        return new Result(List.copyOf(instances.values()), diagnostics.sorted());
    }

    // --- expressions -----------------------------------------------------------------------------

    private @Nullable InstanceExpression expression(final Expression expression, final @Nullable Type expected,
                                                    final SchemaFile file, final DiagnosticCollector out) {
        final InstanceExpression result = switch (expression) {
            case Expression.Default value -> defaultValue(value, expected, file, out);
            case Expression.Construct construct -> construct(construct, file, out);
            case Expression.Call call -> call(call, file, out);
            case Expression.MethodCall call -> methodCall(call, file, out);
            case Expression.Access access -> access(access, file, out);
            case Expression.Value value -> value(value, expected, file, out);
            case Expression.ListValue list -> list(list, expected, file, out);
            case Expression.Negate negate -> tests() ? negate(negate, file, out) : testOnly(negate, out);
            case Expression.Binary binary -> tests() ? binary(binary, file, out) : testOnly(binary, out);
            case Expression.IsNull isNull -> tests() ? isNull(isNull, file, out) : testOnly(isNull, out);
            case Expression.IsType isType -> tests() ? isType(isType, file, out) : testOnly(isType, out);
            case Expression.Index index -> tests() ? index(index, file, out) : testOnly(index, out);
            case Expression.MapValue map -> tests() ? map(map, expected, file, out) : testOnly(map, out);
        };
        if (result != null && expected != null && !assignable(result.type(), expected)) {
            out.report(invalid, "The value is a '" + result.type().text() + "', but a '"
                    + expected.text() + "' is expected", expression.location());
            return null;
        }
        return result;
    }

    private static @Nullable InstanceExpression testOnly(final Expression expression, final DiagnosticCollector out) {
        out.report(Rule.INSTANCE_INVALID, "Operators, indexes and maps are only allowed in tests, not in a default "
                + "instance", expression.location());
        return null;
    }

    private @Nullable InstanceExpression defaultValue(final Expression.Default value, final @Nullable Type expected,
                                                      final SchemaFile file, final DiagnosticCollector out) {
        final Type type = value.type() == null ? expected : types.apply(value.type(), file);
        if (type == null || type instanceof Type.UnresolvedType) {
            out.report(invalid, type == null ? "DEFAULT needs a type here: DEFAULT(Type)"
                    : "Unknown type '" + type.text() + "'", value.location());
            return null;
        }
        return new InstanceExpression.Default(type);
    }

    private @Nullable InstanceExpression construct(final Expression.Construct construct, final SchemaFile file,
                                                   final DiagnosticCollector out) {
        final Type type = types.apply(construct.type(), file);
        final Optional<TypeDefinition> definition = type instanceof Type.DeclaredType d ? linked.type(d.name())
                : Optional.empty();
        if (definition.isEmpty() || !(definition.get() instanceof TypeDefinition.ComplexTypeDefinition complex)
                || complex.abstraction()) {
            out.report(invalid, "'" + construct.type().text() + "' is no complex type that can be "
                    + "created (abstractions and enums cannot)", construct.location());
            return null;
        }
        final Type.DeclaredType declared = (Type.DeclaredType) type;
        final Map<String, FieldDefinition> fields = new LinkedHashMap<>();
        complex.fields().forEach(f -> fields.put(f.name(), f.withType(substitute(declared, f.type()))));
        final Map<String, InstanceExpression> values = arguments(construct.values(), fields.values().stream()
                .map(f -> new Slot(f.name(), f.type(), optional(f), f.hasAnnotation("nullable"))).toList(),
                "attribute", "'" + declared.text()
                + "'", construct.location(), file, out);
        return values == null ? null : new InstanceExpression.Construct(declared, values);
    }

    private @Nullable InstanceExpression call(final Expression.Call call, final SchemaFile file,
                                              final DiagnosticCollector out) {
        final String name = call.name();
        final int dot = name.lastIndexOf('.');
        final String simple = name.substring(dot + 1);
        final String prefix = dot < 0 ? null : name.substring(0, dot);
        // variable.method(...) in a test: a method of the value of a variable (or of one of its attributes)
        if (prefix != null && scope.containsKey(prefix.split("\\.")[0])) {
            return methodCall(new Expression.MethodCall(new Expression.Value(new Literal.NameLiteral(prefix,
                    call.location()), call.location()), simple, call.arguments(), call.location()), file, out);
        }
        // Type.method(...): a static method; ns.function(...) or function(...): a namespace-level function
        if (prefix != null && spec.resolve(file, prefix) instanceof ResolvedType.Declared declared) {
            final QualifiedName owner = new QualifiedName(declared.namespace(), declared.declaration().name());
            final List<MethodDefinition> methods = linked.type(owner).orElseThrow().methods(simple).stream()
                    .filter(MethodDefinition::isStatic).toList();
            return invoke(methods, call.arguments(), "static method '" + name + "'", call.location(), file, out,
                    (method, arguments, result) -> new InstanceExpression.Call(owner, owner.namespace(), method,
                            arguments, result));
        }
        final List<MethodDefinition> builtin = builtins.stream()
                .filter(f -> f.method().name().equals(simple) && (prefix == null || prefix.equals(TESTING)))
                .map(FunctionDefinition::method).toList();
        final String namespace = prefix == null ? file.namespace() : prefix;
        List<MethodDefinition> functions = linked.functions().stream()
                .filter(f -> f.namespace().equals(namespace) && f.method().name().equals(simple))
                .map(FunctionDefinition::method).toList();
        if (!builtin.isEmpty() && !functions.isEmpty()) {
            out.report(Rule.TEST_AMBIGUOUS_CALL, "'" + name + "' is a built-in function and a function of namespace '"
                    + namespace + "'; qualify the call: '" + TESTING + "." + simple + "' or '" + namespace + "."
                    + simple + "'", call.location());
            return null;
        }
        if (!builtin.isEmpty()) {
            return invoke(builtin, call.arguments(), "built-in function '" + simple + "'", call.location(), file, out,
                    (method, arguments, result) -> new InstanceExpression.Call(null, TESTING, method, arguments,
                            result));
        }
        return invoke(functions, call.arguments(), "function '" + name + "'" + (prefix == null ? " in namespace '"
                + namespace + "'" : ""), call.location(), file, out, (method, arguments, result) ->
                new InstanceExpression.Call(null, namespace, method, arguments, result));
    }

    private @Nullable InstanceExpression methodCall(final Expression.MethodCall call, final SchemaFile file,
                                                    final DiagnosticCollector out) {
        final InstanceExpression target = expression(call.target(), null, file, out);
        if (target == null) {
            return null;
        }
        if (tests() && nullable(target)) {
            out.report(Rule.TEST_NULLABLE, "The value may be null; check it with assertNotNull or 'is not null' "
                    + "before calling '" + call.method() + "'", call.location());
            return null;
        }
        final Optional<TypeDefinition> definition = definition(target.type());
        if (definition.isEmpty()) {
            out.report(invalid, "A '" + target.type().text() + "' has no methods", call.location());
            return null;
        }
        final Type.DeclaredType owner = (Type.DeclaredType) target.type();
        final List<MethodDefinition> methods = definition.get().methods(call.method()).stream()
                .filter(m -> !m.isStatic()).toList();
        return invoke(methods, call.arguments(), "method '" + call.method() + "' of '" + owner.text() + "'",
                call.location(), file, out, (method, arguments, result) -> new InstanceExpression.MethodCall(target,
                        method, arguments, substitute(owner, result)));
    }

    private @Nullable InstanceExpression access(final Expression.Access access, final SchemaFile file,
                                                final DiagnosticCollector out) {
        final InstanceExpression target = expression(access.target(), null, file, out);
        return target == null ? null : attribute(target, access.attribute(), access.location(), out);
    }

    private @Nullable InstanceExpression attribute(final InstanceExpression target, final String attribute,
                                                   final SourceLocation location, final DiagnosticCollector out) {
        final Optional<FieldDefinition> field = definition(target.type()).flatMap(d -> d.field(attribute));
        if (field.isEmpty()) {
            out.report(invalid, "A '" + target.type().text() + "' has no attribute '" + attribute + "'", location);
            return null;
        }
        if (tests() && nullable(target)) {
            out.report(Rule.TEST_NULLABLE, "The value may be null; check it with assertNotNull or 'is not null' "
                    + "before reading '" + attribute + "'", location);
            return null;
        }
        return new InstanceExpression.Access(target, field.get(),
                substitute((Type.DeclaredType) target.type(), field.get().type()));
    }

    private @Nullable InstanceExpression value(final Expression.Value value, final @Nullable Type expected,
                                               final SchemaFile file, final DiagnosticCollector out) {
        final Literal literal = value.literal();
        final Type type = expected == null ? new Type.AnyType() : expected;
        // in a test, a literal without a concrete expected type (a type parameter) gets the type of its own kind
        final boolean concrete = expected != null && !(expected instanceof Type.TypeVariable)
                && !(expected instanceof Type.AnyType) && !(expected instanceof Type.WildcardType);
        switch (literal) {
            case Literal.StringLiteral ignored -> {
                if (!accepts(type, BuiltinType.Category.STRING)) {
                    out.report(invalid, "A string is no '" + type.text() + "'", value.location());
                    return null;
                }
                return new InstanceExpression.Value(literal, expected == null || tests() && !concrete
                        ? basic("string") : type);
            }
            case Literal.NumberLiteral number -> {
                if (!accepts(type, BuiltinType.Category.INTEGER) && !accepts(type, BuiltinType.Category.FLOAT)
                        && !accepts(type, BuiltinType.Category.DECIMAL) && !accepts(type, BuiltinType.Category.DURATION)
                        || number.text().contains(".") && accepts(type, BuiltinType.Category.INTEGER)
                        && !accepts(type, BuiltinType.Category.FLOAT)) {
                    out.report(invalid, "The number " + number.text() + " is no '" + type.text() + "'",
                            value.location());
                    return null;
                }
                if (tests() && concrete && type instanceof Type.BasicType basic
                        && basic.builtin().category() == BuiltinType.Category.INTEGER
                        && !IntegerRange.of(basic.builtin()).contains(new BigInteger(number.text().replace("_", "")))) {
                    out.report(Rule.TEST_OUT_OF_RANGE, "The number " + number.text() + " is out of the range of '"
                            + type.text() + "'", value.location());
                    return null;
                }
                if (expected == null || tests() && !concrete) {
                    return new InstanceExpression.Value(literal, basic(number.text().contains(".") ? "double"
                            : "int64"));
                }
                return new InstanceExpression.Value(literal, type);
            }
            case Literal.NameLiteral name -> {
                return name(name, type, expected, value.location(), file, out);
            }
            default -> {
                out.report(invalid, "Unsupported value " + literal.text(), value.location());
                return null;
            }
        }
    }

    /** {@code true}, {@code false}, {@code null}, a constant or an enum constant. */
    private @Nullable InstanceExpression name(final Literal.NameLiteral name, final Type type,
                                              final @Nullable Type expected, final SourceLocation location,
                                              final SchemaFile file, final DiagnosticCollector out) {
        final String text = name.text();
        final String[] segments = text.split("\\.");
        if (scope.containsKey(segments[0])) {
            // a variable of a test, and attributes of its value
            InstanceExpression result = scope.get(segments[0]);
            for (int i = 1; i < segments.length && result != null; i++) {
                result = attribute(result, segments[i], location, out);
            }
            return result;
        }
        if (text.equals("null")) {
            return new InstanceExpression.Value(name, type);
        }
        // a type where a `type<T>` is expected (in a test): assertInstanceOf(expected: PublicKey, actual: key)
        if (tests() && type instanceof Type.BasicType basic && basic.builtin().category() == BuiltinType.Category.TYPE
                && spec.resolve(file, text) instanceof ResolvedType.Declared declared) {
            final Type.DeclaredType named = new Type.DeclaredType(new QualifiedName(declared.namespace(),
                    declared.declaration().name()), List.of());
            return new InstanceExpression.Value(new Literal.NameLiteral(named.text(), location),
                    new Type.BasicType(basic.builtin(), List.of(named)));
        }
        if (text.equals("true") || text.equals("false")) {
            if (!accepts(type, BuiltinType.Category.BOOL)) {
                out.report(invalid, text + " is no '" + type.text() + "'", location);
                return null;
            }
            return new InstanceExpression.Value(name, expected == null || tests() ? basic("bool") : type);
        }
        final int dot = text.lastIndexOf('.');
        final String prefix = dot < 0 ? null : text.substring(0, dot);
        final String simple = text.substring(dot + 1);
        // a constant: NAME (this namespace) or namespace.NAME
        final String namespace = prefix == null ? file.namespace() : prefix;
        final Optional<ConstantDefinition> constant = linked.constants().stream()
                .filter(c -> c.name().equals(new QualifiedName(namespace, simple))).findFirst();
        if (constant.isPresent()) {
            return new InstanceExpression.ConstantValue(constant.get());
        }
        // an enum constant: Enum.VALUE, or VALUE where an enum is expected
        final Optional<Type.DeclaredType> enumType = prefix == null
                ? Optional.of(type).filter(Type.DeclaredType.class::isInstance).map(Type.DeclaredType.class::cast)
                : Optional.of(spec.resolve(file, prefix)).filter(ResolvedType.Declared.class::isInstance)
                .map(ResolvedType.Declared.class::cast)
                .map(d -> new Type.DeclaredType(new QualifiedName(d.namespace(), d.declaration().name()), List.of()));
        final boolean known = enumType.flatMap(t -> linked.type(t.name()))
                .filter(TypeDefinition.EnumDefinition.class::isInstance).map(TypeDefinition.EnumDefinition.class::cast)
                .filter(e -> e.values().stream().anyMatch(v -> v.name().equals(simple))).isPresent();
        if (!known && tests()) {
            // an attribute of a constant or enum constant: BasicTransactionStatus.OK.code
            for (int end = segments.length - 1; end >= 2; end--) {
                final DiagnosticCollector trial = new DiagnosticCollector();
                InstanceExpression result = name(new Literal.NameLiteral(String.join(".",
                        java.util.Arrays.copyOfRange(segments, 0, end)), location), new Type.AnyType(), null,
                        location, file, trial);
                if (result == null || !trial.sorted().isEmpty()) {
                    continue;
                }
                for (int i = end; i < segments.length && result != null; i++) {
                    result = attribute(result, segments[i], location, out);
                }
                return result;
            }
        }
        if (!known) {
            out.report(invalid, "'" + text + "' is no constant and no enum constant"
                    + (type instanceof Type.DeclaredType ? " of '" + type.text() + "'" : ""), location);
            return null;
        }
        return new InstanceExpression.Value(new Literal.NameLiteral(enumType.get().name().name() + "." + simple,
                location), enumType.get());
    }

    private @Nullable InstanceExpression list(final Expression.ListValue list, final @Nullable Type expected,
                                              final SchemaFile file, final DiagnosticCollector out) {
        if (tests() && !(expected instanceof Type.BasicType)) {
            return inferredList(list, file, out);
        }
        if (!(expected instanceof Type.BasicType basic) || basic.builtin().category() != BuiltinType.Category.BYTES
                && basic.builtin().category() != BuiltinType.Category.COLLECTION) {
            out.report(invalid, "A list is used for a list, a set or bytes, not for '"
                    + (expected == null ? "an unknown type" : expected.text()) + "'", list.location());
            return null;
        }
        final Type element = basic.builtin().category() == BuiltinType.Category.BYTES ? basic("uint8")
                : basic.arguments().getFirst();
        final List<InstanceExpression> items = new ArrayList<>();
        for (final Expression item : list.items()) {
            final InstanceExpression resolved = expression(item, element, file, out);
            if (resolved == null) {
                return null;
            }
            items.add(resolved);
        }
        return new InstanceExpression.ListValue(items, expected);
    }

    // --- arguments and overloads -----------------------------------------------------------------

    /** A parameter or attribute an argument can be passed to. */
    private record Slot(String name, Type type, boolean optional, boolean nullable) {
    }

    private interface Invocation {
        InstanceExpression create(MethodDefinition method, Map<String, InstanceExpression> arguments, Type result);
    }

    /** Selects the overload whose parameters fit the arguments and resolves the call. */
    private @Nullable InstanceExpression invoke(final List<MethodDefinition> candidates,
                                                final List<Expression.Argument> arguments, final String what,
                                                final SourceLocation location, final SchemaFile file,
                                                final DiagnosticCollector out, final Invocation invocation) {
        if (candidates.isEmpty()) {
            out.report(invalid, "Unknown " + what, location);
            return null;
        }
        final List<MethodDefinition> matching = new ArrayList<>();
        for (final MethodDefinition candidate : candidates) {
            final DiagnosticCollector trial = new DiagnosticCollector();
            final boolean fits = arguments(arguments, slots(candidate), "parameter", what, location, file, trial) != null
                    && trial.sorted().isEmpty();
            // a nullable argument does not decide the overload; it is reported for the overload that fits otherwise
            final boolean onlyNullable = !trial.sorted().isEmpty() && trial.sorted().stream()
                    .allMatch(d -> d.ruleId().equals(Rule.TEST_NULLABLE.id()));
            if (fits || onlyNullable) {
                matching.add(candidate);
            }
        }
        if (matching.size() > 1) {
            // a number literal prefers the overload of its own kind: toDecimal(value: 5) takes the integer
            final List<MethodDefinition> exact = matching.stream().filter(m -> exactNumbers(m, arguments)).toList();
            if (exact.size() == 1) {
                matching.retainAll(exact);
            }
        }
        if (matching.size() == 1) {
            final MethodDefinition method = matching.getFirst();
            final Map<String, InstanceExpression> resolved = arguments(arguments, slots(method), "parameter", what,
                    location, file, out);
            if (resolved == null) {
                return null;
            }
            // the type arguments of a generic method follow from its arguments
            final Map<Type.TypeVariable, Type> bindings = new HashMap<>();
            method.parameters().stream().filter(p -> resolved.containsKey(p.name()))
                    .forEach(p -> bind(p.type(), resolved.get(p.name()).type(), bindings));
            return invocation.create(method, resolved, LinkedModel.substitute(method.returnType(), bindings));
        }
        if (candidates.size() == 1) {
            // report the actual problem of the only candidate
            arguments(arguments, slots(candidates.getFirst()), "parameter", what, location, file, out);
            return null;
        }
        out.report(invalid, (matching.isEmpty() ? "No overload of " : "More than one overload of ")
                + what + " fits the arguments (" + arguments.stream().map(Expression.Argument::name)
                .collect(Collectors.joining(", ")) + "): " + candidates.stream().map(MethodDefinition::signature)
                .collect(Collectors.joining(", ")), location);
        return null;
    }

    /** Whether every number literal among the arguments goes to a parameter of its kind (integer or not). */
    private static boolean exactNumbers(final MethodDefinition method, final List<Expression.Argument> arguments) {
        for (final Expression.Argument argument : arguments) {
            if (argument.value() instanceof Expression.Value value && value.literal() instanceof Literal.NumberLiteral n) {
                final boolean integer = !n.text().contains(".");
                final boolean integerParameter = method.parameters().stream()
                        .filter(p -> p.name().equals(argument.name())).findFirst()
                        .map(p -> category(p.type()) == BuiltinType.Category.INTEGER).orElse(false);
                if (integer != integerParameter) {
                    return false;
                }
            }
        }
        return true;
    }

    private static List<Slot> slots(final MethodDefinition method) {
        return method.parameters().stream().map(p -> new Slot(p.name(), p.type(),
                p.hasAnnotation("nullable") || p.varargs(), p.hasAnnotation("nullable"))).toList();
    }

    /** Resolves named arguments against the parameters or attributes; {@code null} if they do not fit. */
    private @Nullable Map<String, InstanceExpression> arguments(final List<Expression.Argument> arguments,
                                                                final List<Slot> slots, final String kind,
                                                                final String what, final SourceLocation location,
                                                                final SchemaFile file,
                                                                final DiagnosticCollector out) {
        final Map<String, Slot> byName = new LinkedHashMap<>();
        slots.forEach(s -> byName.put(s.name(), s));
        final Map<String, InstanceExpression> given = new HashMap<>();
        boolean valid = true;
        // in a test, arguments for a type parameter are resolved first without an expected type, and the literals
        // after them with the type the others bind the parameter to: assertEquals(expected: 1, actual: id.num)
        final Map<Type.TypeVariable, Type> bindings = new HashMap<>();
        final List<Expression.Argument> resolutionOrder = new ArrayList<>(arguments);
        if (tests()) {
            resolutionOrder.sort(java.util.Comparator.comparing(a -> literalLike(a.value()) ? 1 : 0));
        }
        for (final Expression.Argument argument : resolutionOrder) {
            final Slot slot = byName.get(argument.name());
            if (slot == null) {
                out.report(invalid, what + " has no " + kind + " '" + argument.name() + "'",
                        argument.location());
                valid = false;
                continue;
            }
            if (given.containsKey(argument.name())) {
                out.report(invalid, "'" + argument.name() + "' is given twice", argument.location());
                valid = false;
                continue;
            }
            final boolean generic = tests() && mentionsVariable(slot.type());
            final Type bound = generic ? LinkedModel.substitute(slot.type(), bindings) : slot.type();
            // a bare type parameter accepts any value; `list<$$T>` or `type<$$T>` still tell the kind of value
            final InstanceExpression value = expression(argument.value(),
                    generic && bound instanceof Type.TypeVariable ? null : bound, file, out);
            if (value == null) {
                valid = false;
            } else if (generic && !compatible(value.type(), bound)) {
                out.report(invalid, "The value is a '" + value.type().text() + "', but a '" + bound.text()
                        + "' is expected", argument.location());
                valid = false;
            } else if (tests() && !slot.nullable() && nullable(value)) {
                out.report(Rule.TEST_NULLABLE, "The value of '" + argument.name() + "' may be null, but the "
                        + kind + " is not nullable; check it with assertNotNull or 'is not null' first",
                        argument.location());
                valid = false;
            } else {
                if (generic) {
                    bind(slot.type(), value.type(), bindings);
                }
                given.put(argument.name(), value);
            }
        }
        // an argument whose value is invalid is reported as such, not as missing
        final Set<String> named = arguments.stream().map(Expression.Argument::name).collect(Collectors.toSet());
        final List<String> missing = slots.stream().filter(s -> !s.optional() && !named.contains(s.name()))
                .map(Slot::name).toList();
        if (!missing.isEmpty()) {
            out.report(invalid, what + " needs a value for " + missing.stream().map(m -> "'" + m + "'")
                    .collect(Collectors.joining(", ")), location);
            valid = false;
        }
        if (!valid) {
            return null;
        }
        final Map<String, InstanceExpression> ordered = new LinkedHashMap<>();
        slots.stream().filter(s -> given.containsKey(s.name())).forEach(s -> ordered.put(s.name(), given.get(s.name())));
        return ordered;
    }

    // --- operators (tests only) ----------------------------------------------------------------------

    private static final String TESTING = "testing";

    private @Nullable InstanceExpression negate(final Expression.Negate negate, final SchemaFile file,
                                                final DiagnosticCollector out) {
        final InstanceExpression operand = expression(negate.operand(), null, file, out);
        if (operand == null) {
            return null;
        }
        final BuiltinType.@Nullable Category category = category(operand.type());
        if (category != BuiltinType.Category.INTEGER && category != BuiltinType.Category.FLOAT
                && category != BuiltinType.Category.DECIMAL) {
            out.report(invalid, "'-' needs a number, not a '" + operand.type().text() + "'", negate.location());
            return null;
        }
        if (nullable(operand)) {
            return nullOperand(negate.location(), out);
        }
        return new InstanceExpression.Negate(operand, category == BuiltinType.Category.INTEGER ? basic(INTEGER)
                : operand.type());
    }

    /** The type of the result of integer arithmetic: the test language computes without overflow. */
    private static final String INTEGER = "int256";

    private @Nullable InstanceExpression binary(final Expression.Binary binary, final SchemaFile file,
                                                final DiagnosticCollector out) {
        final Expression.Operator operator = binary.operator();
        if (operator == Expression.Operator.AND || operator == Expression.Operator.OR) {
            final InstanceExpression left = expression(binary.left(), basic("bool"), file, out);
            final InstanceExpression right = expression(binary.right(), basic("bool"), file, out);
            if (left == null || right == null) {
                return null;
            }
            if (nullable(left) || nullable(right)) {
                return nullOperand(binary.location(), out);
            }
            return new InstanceExpression.Binary(operator, left, right, basic("bool"));
        }
        // the literal side takes the type of the other side
        final InstanceExpression left;
        final InstanceExpression right;
        if (literalLike(binary.left()) && !literalLike(binary.right())) {
            right = expression(binary.right(), null, file, out);
            left = right == null ? null : operand(binary.left(), right.type(), file, out);
        } else {
            left = expression(binary.left(), null, file, out);
            right = left == null ? null : literalLike(binary.right()) ? operand(binary.right(), left.type(), file, out)
                    : expression(binary.right(), null, file, out);
        }
        if (left == null || right == null) {
            return null;
        }
        if (operator == Expression.Operator.EQUAL || operator == Expression.Operator.NOT_EQUAL) {
            if (!compatible(left.type(), right.type())) {
                out.report(Rule.TEST_INCOMPARABLE, "A '" + left.type().text() + "' cannot be compared with a '"
                        + right.type().text() + "'", binary.location());
                return null;
            }
            return new InstanceExpression.Binary(operator, left, right, basic("bool"));
        }
        if (nullable(left) || nullable(right)) {
            return nullOperand(binary.location(), out);
        }
        final BuiltinType.@Nullable Category category = category(left.type());
        final boolean sameKind = category != null && category == category(right.type());
        final boolean numbers = sameKind && (category == BuiltinType.Category.INTEGER
                || category == BuiltinType.Category.FLOAT || category == BuiltinType.Category.DECIMAL);
        final Type result = switch (operator) {
            case LESS, LESS_OR_EQUAL, GREATER, GREATER_OR_EQUAL -> numbers || sameKind
                    && (category == BuiltinType.Category.TEMPORAL || category == BuiltinType.Category.DURATION)
                    && left.type().text().equals(right.type().text()) ? basic("bool") : null;
            case MULTIPLY, REMAINDER, SUBTRACT -> numbers ? arithmetic(left.type()) : null;
            case DIVIDE -> numbers && category != BuiltinType.Category.DECIMAL ? arithmetic(left.type()) : null;
            case ADD -> numbers ? arithmetic(left.type()) : sameKind && (category == BuiltinType.Category.STRING
                    || category == BuiltinType.Category.BYTES || category == BuiltinType.Category.COLLECTION
                    && left.type().text().equals(right.type().text())) ? left.type() : null;
            default -> null;
        };
        if (result == null) {
            out.report(invalid, "'" + operator.symbol() + "' cannot be applied to a '" + left.type().text()
                    + "' and a '" + right.type().text() + "'" + (operator == Expression.Operator.DIVIDE
                    && category == BuiltinType.Category.DECIMAL ? "; divide decimals with divide(...)" : ""),
                    binary.location());
            return null;
        }
        return new InstanceExpression.Binary(operator, left, right, result);
    }

    /**
     * A literal operand: with the type of the other operand where it is one of its values, otherwise with its own
     * type (then the operator reports the mismatch, e.g. {@code test.incomparable}).
     */
    private @Nullable InstanceExpression operand(final Expression literal, final Type other, final SchemaFile file,
                                                 final DiagnosticCollector out) {
        final DiagnosticCollector trial = new DiagnosticCollector();
        final InstanceExpression typed = expression(literal, literalType(other), file, trial);
        if (typed != null && trial.sorted().isEmpty()) {
            return typed;
        }
        final DiagnosticCollector own = new DiagnosticCollector();
        final InstanceExpression untyped = expression(literal, null, file, own);
        if (untyped != null && own.sorted().isEmpty()) {
            return untyped;
        }
        out.addAll(trial.sorted());
        return null;
    }

    private static Type arithmetic(final Type operand) {
        return category(operand) == BuiltinType.Category.INTEGER ? basic(INTEGER) : operand;
    }

    private @Nullable InstanceExpression nullOperand(final SourceLocation location, final DiagnosticCollector out) {
        out.report(Rule.TEST_NULLABLE, "An operand may be null; check it with assertNotNull or 'is not null' first",
                location);
        return null;
    }

    private @Nullable InstanceExpression isNull(final Expression.IsNull isNull, final SchemaFile file,
                                                final DiagnosticCollector out) {
        final InstanceExpression operand = expression(isNull.operand(), null, file, out);
        return operand == null ? null : new InstanceExpression.IsNull(operand, isNull.negated(), basic("bool"));
    }

    private @Nullable InstanceExpression isType(final Expression.IsType isType, final SchemaFile file,
                                                final DiagnosticCollector out) {
        final InstanceExpression operand = expression(isType.operand(), null, file, out);
        if (operand == null) {
            return null;
        }
        final Type checked = types.apply(isType.type(), file);
        if (!(checked instanceof Type.DeclaredType declared) || !(operand.type() instanceof Type.DeclaredType actual)
                || !linked.isSubtypeOf(declared.name(), actual.name())) {
            out.report(invalid, "'is " + isType.type().text() + "' needs a value of a supertype of '"
                    + isType.type().text() + "', not a '" + operand.type().text() + "'", isType.location());
            return null;
        }
        return new InstanceExpression.IsType(operand, checked, isType.negated(), basic("bool"));
    }

    private @Nullable InstanceExpression index(final Expression.Index index, final SchemaFile file,
                                               final DiagnosticCollector out) {
        final InstanceExpression target = expression(index.target(), null, file, out);
        if (target == null) {
            return null;
        }
        if (nullable(target)) {
            return nullOperand(index.location(), out);
        }
        if (target.type() instanceof Type.BasicType basic && basic.builtin().name().equals("list")) {
            final InstanceExpression position = expression(index.index(), basic("int64"), file, out);
            return position == null ? null
                    : new InstanceExpression.Index(target, position, basic.arguments().getFirst());
        }
        if (target.type() instanceof Type.BasicType basic && basic.builtin().category() == BuiltinType.Category.MAP) {
            final InstanceExpression key = expression(index.index(), basic.arguments().getFirst(), file, out);
            return key == null ? null : new InstanceExpression.Index(target, key, basic.arguments().get(1));
        }
        out.report(invalid, "'[...]' needs a list or a map, not a '" + target.type().text() + "'", index.location());
        return null;
    }

    private @Nullable InstanceExpression map(final Expression.MapValue map, final @Nullable Type expected,
                                             final SchemaFile file, final DiagnosticCollector out) {
        Type key = null;
        Type value = null;
        if (expected instanceof Type.BasicType basic && basic.builtin().category() == BuiltinType.Category.MAP) {
            key = basic.arguments().getFirst();
            value = basic.arguments().get(1);
        } else if (expected != null && !(expected instanceof Type.TypeVariable) && !(expected instanceof Type.AnyType)) {
            out.report(invalid, "A map is no '" + expected.text() + "'", map.location());
            return null;
        }
        final List<Map.Entry<InstanceExpression, InstanceExpression>> entries = new ArrayList<>();
        for (final Expression.Entry entry : map.entries()) {
            final InstanceExpression k = expression(entry.key(), key, file, out);
            final InstanceExpression v = expression(entry.value(), value, file, out);
            if (k == null || v == null) {
                return null;
            }
            key = key == null ? k.type() : key;
            value = value == null ? v.type() : value;
            entries.add(Map.entry(k, v));
        }
        if (key == null) {
            out.report(invalid, "The type of an empty map is unknown here", map.location());
            return null;
        }
        return new InstanceExpression.MapValue(entries, new Type.BasicType(BuiltinType.lookup("map").orElseThrow(),
                List.of(key, value)));
    }

    /** A list without an expected type: its elements decide (the first one that is not a literal). */
    private @Nullable InstanceExpression inferredList(final Expression.ListValue list, final SchemaFile file,
                                                      final DiagnosticCollector out) {
        Type element = null;
        final List<Expression> ordered = new ArrayList<>(list.items());
        ordered.sort(java.util.Comparator.comparing(i -> literalLike(i) ? 1 : 0));
        final Map<Expression, InstanceExpression> resolved = new HashMap<>();
        for (final Expression item : ordered) {
            final InstanceExpression value = expression(item, element, file, out);
            if (value == null) {
                return null;
            }
            element = element == null ? value.type() : element;
            resolved.put(item, value);
        }
        if (element == null) {
            out.report(invalid, "The type of an empty list is unknown here", list.location());
            return null;
        }
        return new InstanceExpression.ListValue(list.items().stream().map(resolved::get).toList(),
                new Type.BasicType(BuiltinType.lookup("list").orElseThrow(), List.of(element)));
    }

    /**
     * Whether the expression needs an expected type to get its own: a literal, a list, a map, {@code DEFAULT} or an
     * unqualified name (an enum constant), but not a variable.
     */
    private boolean literalLike(final Expression expression) {
        return switch (expression) {
            case Expression.Value value -> !(value.literal() instanceof Literal.NameLiteral name)
                    || !name.text().contains(".") && !scope.containsKey(name.text());
            case Expression.ListValue ignored -> true;
            case Expression.MapValue ignored -> true;
            case Expression.Default value -> value.type() == null;
            case Expression.Negate negate -> literalLike(negate.operand());
            default -> false;
        };
    }

    /** The type a literal compared with or combined with a value of the given type gets. */
    private static Type literalType(final Type other) {
        return other instanceof Type.BasicType basic && basic.builtin().name().equals(INTEGER) ? basic("int64") : other;
    }

    /** Whether the value may be {@code null}: a nullable attribute or result, {@code null}, or such a variable. */
    static boolean nullable(final InstanceExpression expression) {
        return switch (expression) {
            case InstanceExpression.Variable variable -> variable.nullable();
            case InstanceExpression.Access access -> access.field().hasAnnotation("nullable");
            case InstanceExpression.Call call -> call.method().hasAnnotation("nullable");
            case InstanceExpression.MethodCall call -> call.method().hasAnnotation("nullable");
            case InstanceExpression.Value value -> value.literal() instanceof Literal.NameLiteral name
                    && name.text().equals("null");
            default -> false;
        };
    }

    private static BuiltinType.@Nullable Category category(final Type type) {
        return type instanceof Type.BasicType basic ? basic.builtin().category() : null;
    }

    /** Whether one type can be used for the other (in either direction). */
    private boolean compatible(final Type a, final Type b) {
        return assignable(a, b) || assignable(b, a);
    }

    private static boolean mentionsVariable(final Type type) {
        return switch (type) {
            case Type.TypeVariable ignored -> true;
            case Type.BasicType basic -> basic.arguments().stream().anyMatch(InstanceResolver::mentionsVariable);
            case Type.DeclaredType declared -> declared.arguments().stream()
                    .anyMatch(InstanceResolver::mentionsVariable);
            default -> false;
        };
    }

    /** Binds the type variables of a parameter type to the parts of the argument type at the same position. */
    private void bind(final Type parameter, final Type argument, final Map<Type.TypeVariable, Type> bindings) {
        switch (parameter) {
            case Type.TypeVariable variable -> bindings.putIfAbsent(variable, argument);
            case Type.BasicType basic when argument instanceof Type.BasicType actual
                    && actual.builtin().name().equals(basic.builtin().name())
                    && actual.arguments().size() == basic.arguments().size() -> {
                for (int i = 0; i < basic.arguments().size(); i++) {
                    bind(basic.arguments().get(i), actual.arguments().get(i), bindings);
                }
            }
            case Type.DeclaredType declared when argument instanceof Type.DeclaredType given -> {
                // a subtype binds through the supertype it extends: AccountCreateTransaction as
                // Transaction<AccountCreateReceipt, AccountCreateTransaction> binds Transaction<$$R, ANY>
                final Type.DeclaredType actual = linked.asSupertype(given, declared.name()).orElse(given);
                if (actual.name().equals(declared.name())
                        && actual.arguments().size() == declared.arguments().size()) {
                    for (int i = 0; i < declared.arguments().size(); i++) {
                        bind(declared.arguments().get(i), actual.arguments().get(i), bindings);
                    }
                }
            }
            default -> {
                // nothing to bind
            }
        }
    }

    // --- cycles ----------------------------------------------------------------------------------

    private void cycles(final Map<QualifiedName, InstanceDefinition> instances, final DiagnosticCollector out) {
        final Map<QualifiedName, Set<QualifiedName>> edges = new HashMap<>();
        instances.forEach((name, instance) -> {
            final Set<QualifiedName> used = new HashSet<>();
            defaults(instance.expression(), used);
            used.retainAll(instances.keySet());
            edges.put(name, used);
        });
        for (final QualifiedName name : instances.keySet()) {
            if (reaches(name, name, edges, new HashSet<>())) {
                out.report(Rule.INSTANCE_CYCLE, "The default instance of '" + name + "' depends on itself through "
                        + "DEFAULT", instances.get(name).location());
            }
        }
    }

    private static boolean reaches(final QualifiedName from, final QualifiedName target,
                                   final Map<QualifiedName, Set<QualifiedName>> edges, final Set<QualifiedName> seen) {
        for (final QualifiedName next : edges.getOrDefault(from, Set.of())) {
            if (next.equals(target) || seen.add(next) && reaches(next, target, edges, seen)) {
                return true;
            }
        }
        return false;
    }

    /** The types of the {@code DEFAULT} values in an expression. */
    static void defaults(final InstanceExpression expression, final Set<QualifiedName> out) {
        switch (expression) {
            case InstanceExpression.Default value -> {
                if (value.type() instanceof Type.DeclaredType declared) {
                    out.add(declared.name());
                }
            }
            case InstanceExpression.Construct construct -> construct.values().values().forEach(v -> defaults(v, out));
            case InstanceExpression.Call call -> call.arguments().values().forEach(v -> defaults(v, out));
            case InstanceExpression.MethodCall call -> {
                defaults(call.target(), out);
                call.arguments().values().forEach(v -> defaults(v, out));
            }
            case InstanceExpression.Access access -> defaults(access.target(), out);
            case InstanceExpression.ListValue list -> list.items().forEach(v -> defaults(v, out));
            case InstanceExpression.Value ignored -> {
            }
            case InstanceExpression.ConstantValue ignored -> {
            }
            default -> {
                // the test-only expressions do not occur in default instances
            }
        }
    }

    // --- types -----------------------------------------------------------------------------------

    private Optional<TypeDefinition> definition(final Type type) {
        return type instanceof Type.DeclaredType declared ? linked.type(declared.name()) : Optional.empty();
    }

    private Type substitute(final Type.DeclaredType owner, final Type type) {
        return linked.type(owner.name()).isPresent() ? linked.substitute(owner, type) : type;
    }

    private static boolean optional(final FieldDefinition field) {
        return field.hasAnnotation("nullable") || field.hasAnnotation("default");
    }

    private static Type basic(final String name) {
        return new Type.BasicType(BuiltinType.lookup(name).orElseThrow(), List.of());
    }

    /** Whether a value of the category can be used for the type (type variables and {@code ANY} accept all). */
    private static boolean accepts(final Type type, final BuiltinType.Category category) {
        return switch (type) {
            case Type.BasicType basic -> basic.builtin().category() == category;
            case Type.AnyType ignored -> true;
            case Type.TypeVariable ignored -> true;
            case Type.WildcardType wildcard -> wildcard.upperBound() == null || accepts(wildcard.upperBound(), category);
            default -> false;
        };
    }

    /** Whether a value of {@code actual} can be used where {@code expected} is required (type arguments ignored). */
    private boolean assignable(final Type actual, final Type expected) {
        return switch (expected) {
            case Type.AnyType ignored -> true;
            case Type.TypeVariable ignored -> true;
            case Type.UnresolvedType ignored -> true;
            case Type.WildcardType wildcard -> wildcard.upperBound() == null || assignable(actual, wildcard.upperBound());
            case Type.DeclaredType declared -> actual instanceof Type.DeclaredType a
                    && linked.isSubtypeOf(a.name(), declared.name());
            case Type.BasicType basic -> actual instanceof Type.BasicType a
                    && (a.builtin().name().equals(basic.builtin().name())
                    || a.builtin().category() == basic.builtin().category()
                    && a.builtin().category() == BuiltinType.Category.INTEGER);
            case Type.FunctionType ignored -> false;
            case Type.VoidType ignored -> false;
        };
    }
}
