package org.hiero.sdk.v3.metalang.model;

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
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;
import org.hiero.sdk.v3.metalang.semantic.ResolvedType;
import org.hiero.sdk.v3.metalang.semantic.SpecModel;
import org.jspecify.annotations.Nullable;

/**
 * Resolves the default instances of the specs ({@code ## Default Instances}) against the linked model: names of types,
 * functions, static methods, methods, attributes, constants and enum constants, the overload of a call (by the names
 * and types of its arguments), and the types of all values. A default instance must be declared in the spec of the
 * namespace that declares its type, at most once per type, and default instances must not depend on each other in a
 * cycle ({@code DEFAULT(A)} in the instance of {@code B} and vice versa).
 */
final class InstanceResolver {

    private final SpecModel spec;
    private final LinkedModel linked;
    private final BiFunction<TypeRef, SchemaFile, Type> types;

    /**
     * The resolved default instances and the problems found.
     *
     * @param instances   the default instances that could be resolved
     * @param diagnostics the problems
     */
    record Result(List<InstanceDefinition> instances, List<Diagnostic> diagnostics) {
    }

    private InstanceResolver(final SpecModel spec, final LinkedModel linked,
                             final BiFunction<TypeRef, SchemaFile, Type> types) {
        this.spec = spec;
        this.linked = linked;
        this.types = types;
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
        return new InstanceResolver(spec, linked, types).resolve();
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
        };
        if (result != null && expected != null && !assignable(result.type(), expected)) {
            out.report(Rule.INSTANCE_INVALID, "The value is a '" + result.type().text() + "', but a '"
                    + expected.text() + "' is expected", expression.location());
            return null;
        }
        return result;
    }

    private @Nullable InstanceExpression defaultValue(final Expression.Default value, final @Nullable Type expected,
                                                      final SchemaFile file, final DiagnosticCollector out) {
        final Type type = value.type() == null ? expected : types.apply(value.type(), file);
        if (type == null || type instanceof Type.UnresolvedType) {
            out.report(Rule.INSTANCE_INVALID, type == null ? "DEFAULT needs a type here: DEFAULT(Type)"
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
            out.report(Rule.INSTANCE_INVALID, "'" + construct.type().text() + "' is no complex type that can be "
                    + "created (abstractions and enums cannot)", construct.location());
            return null;
        }
        final Type.DeclaredType declared = (Type.DeclaredType) type;
        final Map<String, FieldDefinition> fields = new LinkedHashMap<>();
        complex.fields().forEach(f -> fields.put(f.name(), f.withType(substitute(declared, f.type()))));
        final Map<String, InstanceExpression> values = arguments(construct.values(), fields.values().stream()
                .map(f -> new Slot(f.name(), f.type(), optional(f))).toList(), "attribute", "'" + declared.text()
                + "'", construct.location(), file, out);
        return values == null ? null : new InstanceExpression.Construct(declared, values);
    }

    private @Nullable InstanceExpression call(final Expression.Call call, final SchemaFile file,
                                              final DiagnosticCollector out) {
        final String name = call.name();
        final int dot = name.lastIndexOf('.');
        final String simple = name.substring(dot + 1);
        final String prefix = dot < 0 ? null : name.substring(0, dot);
        // Type.method(...): a static method; ns.function(...) or function(...): a namespace-level function
        if (prefix != null && spec.resolve(file, prefix) instanceof ResolvedType.Declared declared) {
            final QualifiedName owner = new QualifiedName(declared.namespace(), declared.declaration().name());
            final List<MethodDefinition> methods = linked.type(owner).orElseThrow().methods(simple).stream()
                    .filter(MethodDefinition::isStatic).toList();
            return invoke(methods, call.arguments(), "static method '" + name + "'", call.location(), file, out,
                    (method, arguments) -> new InstanceExpression.Call(owner, owner.namespace(), method, arguments,
                            method.returnType()));
        }
        final String namespace = prefix == null ? file.namespace() : prefix;
        final List<MethodDefinition> functions = linked.functions().stream()
                .filter(f -> f.namespace().equals(namespace) && f.method().name().equals(simple))
                .map(FunctionDefinition::method).toList();
        return invoke(functions, call.arguments(), "function '" + name + "'" + (prefix == null ? " in namespace '"
                + namespace + "'" : ""), call.location(), file, out, (method, arguments) ->
                new InstanceExpression.Call(null, namespace, method, arguments, method.returnType()));
    }

    private @Nullable InstanceExpression methodCall(final Expression.MethodCall call, final SchemaFile file,
                                                    final DiagnosticCollector out) {
        final InstanceExpression target = expression(call.target(), null, file, out);
        if (target == null) {
            return null;
        }
        final Optional<TypeDefinition> definition = definition(target.type());
        if (definition.isEmpty()) {
            out.report(Rule.INSTANCE_INVALID, "A '" + target.type().text() + "' has no methods", call.location());
            return null;
        }
        final Type.DeclaredType owner = (Type.DeclaredType) target.type();
        final List<MethodDefinition> methods = definition.get().methods(call.method()).stream()
                .filter(m -> !m.isStatic()).toList();
        return invoke(methods, call.arguments(), "method '" + call.method() + "' of '" + owner.text() + "'",
                call.location(), file, out, (method, arguments) -> new InstanceExpression.MethodCall(target, method,
                        arguments, substitute(owner, method.returnType())));
    }

    private @Nullable InstanceExpression access(final Expression.Access access, final SchemaFile file,
                                                final DiagnosticCollector out) {
        final InstanceExpression target = expression(access.target(), null, file, out);
        if (target == null) {
            return null;
        }
        final Optional<FieldDefinition> field = definition(target.type()).flatMap(d -> d.field(access.attribute()));
        if (field.isEmpty()) {
            out.report(Rule.INSTANCE_INVALID, "A '" + target.type().text() + "' has no attribute '"
                    + access.attribute() + "'", access.location());
            return null;
        }
        return new InstanceExpression.Access(target, field.get(),
                substitute((Type.DeclaredType) target.type(), field.get().type()));
    }

    private @Nullable InstanceExpression value(final Expression.Value value, final @Nullable Type expected,
                                               final SchemaFile file, final DiagnosticCollector out) {
        final Literal literal = value.literal();
        final Type type = expected == null ? new Type.AnyType() : expected;
        switch (literal) {
            case Literal.StringLiteral ignored -> {
                if (!accepts(type, BuiltinType.Category.STRING)) {
                    out.report(Rule.INSTANCE_INVALID, "A string is no '" + type.text() + "'", value.location());
                    return null;
                }
                return new InstanceExpression.Value(literal, expected == null ? basic("string") : type);
            }
            case Literal.NumberLiteral number -> {
                if (!accepts(type, BuiltinType.Category.INTEGER) && !accepts(type, BuiltinType.Category.FLOAT)
                        && !accepts(type, BuiltinType.Category.DECIMAL) && !accepts(type, BuiltinType.Category.DURATION)
                        || number.text().contains(".") && accepts(type, BuiltinType.Category.INTEGER)
                        && !accepts(type, BuiltinType.Category.FLOAT)) {
                    out.report(Rule.INSTANCE_INVALID, "The number " + number.text() + " is no '" + type.text() + "'",
                            value.location());
                    return null;
                }
                return new InstanceExpression.Value(literal, expected == null ? basic("int64") : type);
            }
            case Literal.NameLiteral name -> {
                return name(name, type, expected, value.location(), file, out);
            }
            default -> {
                out.report(Rule.INSTANCE_INVALID, "Unsupported value " + literal.text(), value.location());
                return null;
            }
        }
    }

    /** {@code true}, {@code false}, {@code null}, a constant or an enum constant. */
    private @Nullable InstanceExpression name(final Literal.NameLiteral name, final Type type,
                                              final @Nullable Type expected, final SourceLocation location,
                                              final SchemaFile file, final DiagnosticCollector out) {
        final String text = name.text();
        if (text.equals("null")) {
            return new InstanceExpression.Value(name, type);
        }
        if (text.equals("true") || text.equals("false")) {
            if (!accepts(type, BuiltinType.Category.BOOL)) {
                out.report(Rule.INSTANCE_INVALID, text + " is no '" + type.text() + "'", location);
                return null;
            }
            return new InstanceExpression.Value(name, expected == null ? basic("bool") : type);
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
        if (!known) {
            out.report(Rule.INSTANCE_INVALID, "'" + text + "' is no constant and no enum constant"
                    + (type instanceof Type.DeclaredType ? " of '" + type.text() + "'" : ""), location);
            return null;
        }
        return new InstanceExpression.Value(new Literal.NameLiteral(enumType.get().name().name() + "." + simple,
                location), enumType.get());
    }

    private @Nullable InstanceExpression list(final Expression.ListValue list, final @Nullable Type expected,
                                              final SchemaFile file, final DiagnosticCollector out) {
        if (!(expected instanceof Type.BasicType basic) || basic.builtin().category() != BuiltinType.Category.BYTES
                && basic.builtin().category() != BuiltinType.Category.COLLECTION) {
            out.report(Rule.INSTANCE_INVALID, "A list is used for a list, a set or bytes, not for '"
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
    private record Slot(String name, Type type, boolean optional) {
    }

    private interface Invocation {
        InstanceExpression create(MethodDefinition method, Map<String, InstanceExpression> arguments);
    }

    /** Selects the overload whose parameters fit the arguments and resolves the call. */
    private @Nullable InstanceExpression invoke(final List<MethodDefinition> candidates,
                                                final List<Expression.Argument> arguments, final String what,
                                                final SourceLocation location, final SchemaFile file,
                                                final DiagnosticCollector out, final Invocation invocation) {
        if (candidates.isEmpty()) {
            out.report(Rule.INSTANCE_INVALID, "Unknown " + what, location);
            return null;
        }
        final List<MethodDefinition> matching = new ArrayList<>();
        for (final MethodDefinition candidate : candidates) {
            final DiagnosticCollector trial = new DiagnosticCollector();
            if (arguments(arguments, slots(candidate), "parameter", what, location, file, trial) != null
                    && trial.sorted().isEmpty()) {
                matching.add(candidate);
            }
        }
        if (matching.size() == 1) {
            final MethodDefinition method = matching.getFirst();
            final Map<String, InstanceExpression> resolved = arguments(arguments, slots(method), "parameter", what,
                    location, file, out);
            return resolved == null ? null : invocation.create(method, resolved);
        }
        if (candidates.size() == 1) {
            // report the actual problem of the only candidate
            arguments(arguments, slots(candidates.getFirst()), "parameter", what, location, file, out);
            return null;
        }
        out.report(Rule.INSTANCE_INVALID, (matching.isEmpty() ? "No overload of " : "More than one overload of ")
                + what + " fits the arguments (" + arguments.stream().map(Expression.Argument::name)
                .collect(Collectors.joining(", ")) + "): " + candidates.stream().map(MethodDefinition::signature)
                .collect(Collectors.joining(", ")), location);
        return null;
    }

    private static List<Slot> slots(final MethodDefinition method) {
        return method.parameters().stream().map(p -> new Slot(p.name(), p.type(),
                p.hasAnnotation("nullable") || p.varargs())).toList();
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
        for (final Expression.Argument argument : arguments) {
            final Slot slot = byName.get(argument.name());
            if (slot == null) {
                out.report(Rule.INSTANCE_INVALID, what + " has no " + kind + " '" + argument.name() + "'",
                        argument.location());
                valid = false;
                continue;
            }
            if (given.containsKey(argument.name())) {
                out.report(Rule.INSTANCE_INVALID, "'" + argument.name() + "' is given twice", argument.location());
                valid = false;
                continue;
            }
            final InstanceExpression value = expression(argument.value(), slot.type(), file, out);
            if (value == null) {
                valid = false;
            } else {
                given.put(argument.name(), value);
            }
        }
        // an argument whose value is invalid is reported as such, not as missing
        final Set<String> named = arguments.stream().map(Expression.Argument::name).collect(Collectors.toSet());
        final List<String> missing = slots.stream().filter(s -> !s.optional() && !named.contains(s.name()))
                .map(Slot::name).toList();
        if (!missing.isEmpty()) {
            out.report(Rule.INSTANCE_INVALID, what + " needs a value for " + missing.stream().map(m -> "'" + m + "'")
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
