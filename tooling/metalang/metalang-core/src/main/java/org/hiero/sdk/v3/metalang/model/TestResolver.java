package org.hiero.sdk.v3.metalang.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import org.hiero.sdk.v3.metalang.ast.Annotation;
import org.hiero.sdk.v3.metalang.ast.Expression;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.ast.SchemaFile;
import org.hiero.sdk.v3.metalang.ast.Statement;
import org.hiero.sdk.v3.metalang.ast.TestCase;
import org.hiero.sdk.v3.metalang.ast.TestValues;
import org.hiero.sdk.v3.metalang.ast.TypeRef;
import org.hiero.sdk.v3.metalang.diagnostic.Diagnostic;
import org.hiero.sdk.v3.metalang.diagnostic.DiagnosticCollector;
import org.hiero.sdk.v3.metalang.diagnostic.Rule;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;
import org.hiero.sdk.v3.metalang.semantic.SpecModel;
import org.jspecify.annotations.Nullable;

/**
 * Resolves the tests of the specs (see {@code guidelines/test-language.md}) against the linked model: the statements,
 * the variables and their scopes, the narrowing of nullable variables, the annotations, and every expression with the
 * {@link InstanceResolver} in its test mode. A test with a problem is reported and left out.
 */
final class TestResolver {

    private static final Set<String> TEST_ANNOTATIONS = Set.of("disabled", "deprecated");
    private static final String EXPECT_THROWS = "expectThrows";

    /** The error of a value that violates a validation annotation or a @@oneOf/@@oneOrNoneOf group. */
    static final String INVALID_ARGUMENT = "invalid-argument-error";

    /** The annotations whose violation is an {@value #INVALID_ARGUMENT}. */
    private static final Set<String> VALIDATION = Set.of("min", "max", "minLength", "maxLength", "maxBytes", "minSize", "maxSize",
            "pattern", "urlPattern");

    private final SpecModel spec;
    private final InstanceResolver expressions;
    private final LinkedModel linked;

    /**
     * The resolved tests and the problems found.
     *
     * @param tests       the tests that could be resolved
     * @param values      the named test values that could be resolved
     * @param diagnostics the problems
     */
    record Result(List<TestDefinition> tests, List<TestValuesDefinition> values, List<Diagnostic> diagnostics) {
    }

    private TestResolver(final SpecModel spec, final InstanceResolver expressions, final LinkedModel linked) {
        this.spec = spec;
        this.expressions = expressions;
        this.linked = linked;
    }

    /**
     * Resolves all tests.
     *
     * @param spec   the specs
     * @param linked the linked model with the default instances
     * @param types  converts a type reference of a file into a type
     * @return the result
     */
    static Result resolve(final SpecModel spec, final LinkedModel linked,
                          final BiFunction<TypeRef, SchemaFile, Type> types) {
        if (spec.files().stream().allMatch(f -> f.tests().isEmpty() && f.values().isEmpty())) {
            return new Result(List.of(), List.of(), List.of());
        }
        return new TestResolver(spec, InstanceResolver.forTests(spec, linked, types, Builtins.functions()), linked)
                .resolve();
    }

    private Result resolve() {
        final DiagnosticCollector diagnostics = new DiagnosticCollector();
        final List<TestDefinition> tests = new ArrayList<>();
        final Map<String, SourceLocation> ids = new HashMap<>();
        final List<TestValuesDefinition> allValues = new ArrayList<>();
        for (final SchemaFile file : spec.files()) {
            final Map<String, TestValuesDefinition> values = values(file, diagnostics);
            allValues.addAll(values.values());
            final Set<String> used = new HashSet<>();
            for (final TestCase test : file.tests()) {
                final String id = file.namespace() + "/" + kebab(test.name());
                final SourceLocation previous = ids.putIfAbsent(id, test.location());
                if (previous != null) {
                    diagnostics.report(Rule.TEST_DUPLICATE_ID, "Another test of namespace '" + file.namespace()
                            + "' has the id '" + id + "' (" + previous + ")", test.location());
                    continue;
                }
                final DiagnosticCollector problems = new DiagnosticCollector();
                annotations(test, problems);
                final Body body = new Body(file, problems, values.keySet());
                final Map<String, InstanceExpression.Variable> scope = new LinkedHashMap<>();
                values.values().forEach(v -> scope.put(v.name(), new InstanceExpression.Variable(v.name(), v.type(),
                        false)));
                final List<TestDefinition.Statement> statements = body.statements(test.body(), scope);
                statements.forEach(st -> variables(st, used));
                final List<Diagnostic> found = problems.sorted();
                diagnostics.addAll(found);
                if (found.stream().noneMatch(d -> d.severity() == org.hiero.sdk.v3.metalang.diagnostic.Severity.ERROR)) {
                    if (!body.checks) {
                        diagnostics.report(Rule.TEST_EMPTY, "The test '" + test.name() + "' checks nothing: it "
                                + "calls no assertion and has no @@expectThrows", test.location());
                    }
                    tests.add(new TestDefinition(id, test.name(), file.namespace(), specName(file), test.annotations(),
                            statements, test.documentation(), test.location()));
                }
            }
            values.values().stream().filter(v -> !used.contains(v.name())).forEach(v -> diagnostics.report(
                    Rule.TEST_UNUSED_VALUES, "No test of the spec uses the values '" + v.name() + "'", v.location()));
        }
        return new Result(List.copyOf(tests), List.copyOf(allValues), diagnostics.sorted());
    }

    /** Resolves the named test values of a file; they see no variables, only the API and the built-in functions. */
    private Map<String, TestValuesDefinition> values(final SchemaFile file, final DiagnosticCollector diagnostics) {
        final Map<String, TestValuesDefinition> result = new LinkedHashMap<>();
        for (final TestValues values : file.values()) {
            final TestValuesDefinition previous = result.get(values.name());
            if (previous != null) {
                diagnostics.report(Rule.TEST_DUPLICATE_VALUES, "The spec already declares values named '"
                        + values.name() + "' (" + previous.location() + ")", values.location());
                continue;
            }
            final Type type = expressions.type(values.type(), file);
            final InstanceExpression value = expressions.resolve(values.expression(), type, new LinkedHashMap<>(),
                    file, diagnostics);
            if (value == null) {
                continue;
            }
            if (InstanceResolver.nullable(value)) {
                diagnostics.report(Rule.TEST_NULLABLE, "Named test values cannot be null", values.location());
                continue;
            }
            result.put(values.name(), new TestValuesDefinition(values.name(), file.namespace(), specName(file), type,
                    value, values.documentation(), values.location()));
        }
        return result;
    }

    /** Collects the names of the variables a statement reads. */
    private static void variables(final TestDefinition.Statement statement, final Set<String> out) {
        switch (statement) {
            case TestDefinition.Comment ignored -> {
                // no expression
            }
            case TestDefinition.Let let -> variables(let.value(), out);
            case TestDefinition.Evaluate evaluate -> variables(evaluate.expression(), out);
            case TestDefinition.If conditional -> {
                variables(conditional.condition(), out);
                conditional.then().forEach(st -> variables(st, out));
                conditional.otherwise().forEach(st -> variables(st, out));
            }
            case TestDefinition.For loop -> {
                variables(loop.iterable(), out);
                loop.body().forEach(st -> variables(st, out));
            }
        }
    }

    private static void variables(final InstanceExpression expression, final Set<String> out) {
        switch (expression) {
            case InstanceExpression.Variable variable -> out.add(variable.name());
            case InstanceExpression.Call call -> call.arguments().values().forEach(a -> variables(a, out));
            case InstanceExpression.MethodCall call -> {
                variables(call.target(), out);
                call.arguments().values().forEach(a -> variables(a, out));
            }
            case InstanceExpression.Construct construct -> construct.values().values().forEach(v -> variables(v, out));
            case InstanceExpression.Access access -> variables(access.target(), out);
            case InstanceExpression.ListValue list -> list.items().forEach(i -> variables(i, out));
            case InstanceExpression.MapValue map -> map.entries().forEach(e -> {
                variables(e.getKey(), out);
                variables(e.getValue(), out);
            });
            case InstanceExpression.Binary binary -> {
                variables(binary.left(), out);
                variables(binary.right(), out);
            }
            case InstanceExpression.Negate negate -> variables(negate.operand(), out);
            case InstanceExpression.IsNull isNull -> variables(isNull.operand(), out);
            case InstanceExpression.IsType isType -> variables(isType.operand(), out);
            case InstanceExpression.Index index -> {
                variables(index.target(), out);
                variables(index.index(), out);
            }
            default -> {
                // no variables
            }
        }
    }

    private static void annotations(final TestCase test, final DiagnosticCollector out) {
        for (final Annotation annotation : test.annotations()) {
            if (!TEST_ANNOTATIONS.contains(annotation.name())) {
                out.report(Rule.TEST_INVALID, "A test cannot be annotated with @@" + annotation.name()
                        + "; allowed are @@disabled(\"reason\") and @@deprecated(\"reason\")", annotation.location());
            } else if (annotation.arguments().size() != 1
                    || !(annotation.arguments().getFirst() instanceof Literal.StringLiteral)) {
                out.report(Rule.TEST_INVALID, "@@" + annotation.name() + " needs the reason as a string: @@"
                        + annotation.name() + "(\"reason\")", annotation.location());
            }
        }
    }

    /** The statements of one test: the variables of all its blocks share one set of names. */
    private final class Body {

        private final SchemaFile file;
        private final DiagnosticCollector out;
        private final Set<String> names = new HashSet<>();
        /** The names of the spec's named test values, which a test cannot bind again. */
        private final Set<String> values;
        /** Whether the test calls an assertion or expects an error. */
        private boolean checks;

        private Body(final SchemaFile file, final DiagnosticCollector out, final Set<String> values) {
            this.file = file;
            this.out = out;
            this.values = values;
        }

        /**
         * Resolves a block.
         *
         * @param statements the statements
         * @param scope      the variables visible in the block; the block adds its own (a copy is passed in)
         * @return the resolved statements
         */
        private List<TestDefinition.Statement> statements(final List<Statement> statements,
                                                          final Map<String, InstanceExpression.Variable> scope) {
            final List<TestDefinition.Statement> result = new ArrayList<>();
            for (final Statement statement : statements) {
                final TestDefinition.Statement resolved = statement(statement, scope);
                if (resolved != null) {
                    result.add(resolved);
                }
            }
            return result;
        }

        private TestDefinition.@Nullable Statement statement(final Statement statement,
                                                             final Map<String, InstanceExpression.Variable> scope) {
            return switch (statement) {
                case Statement.Comment comment -> new TestDefinition.Comment(comment.text(), comment.trailing());
                case Statement.Let let -> let(let, scope);
                case Statement.Evaluate evaluate -> evaluate(evaluate, scope);
                case Statement.If conditional -> conditional(conditional, scope);
                case Statement.For loop -> loop(loop, scope);
            };
        }

        private TestDefinition.@Nullable Statement let(final Statement.Let let,
                                                       final Map<String, InstanceExpression.Variable> scope) {
            if (!declare(let.name(), let.location())) {
                return null;
            }
            final Type declared = let.type() == null ? null : expressions.type(let.type(), file);
            final InstanceExpression value = expressions.resolve(let.value(), declared, scope, file, out);
            if (value == null) {
                return null;
            }
            final Type type = declared == null ? value.type() : declared;
            final boolean nullable = InstanceResolver.nullable(value);
            scope.put(let.name(), new InstanceExpression.Variable(let.name(), type, nullable));
            return new TestDefinition.Let(let.name(), type, nullable, value);
        }

        private TestDefinition.@Nullable Statement evaluate(final Statement.Evaluate evaluate,
                                                            final Map<String, InstanceExpression.Variable> scope) {
            String expected = null;
            for (final Annotation annotation : evaluate.annotations()) {
                if (!annotation.name().equals(EXPECT_THROWS) || expected != null || annotation.arguments().size() != 1
                        || !(annotation.arguments().getFirst() instanceof Literal.NameLiteral id)
                        || !id.text().contains("-")) {
                    out.report(Rule.TEST_INVALID, "A call can only be annotated with one @@expectThrows(error-id)",
                            annotation.location());
                    return null;
                }
                expected = id.text();
            }
            final InstanceExpression expression = expressions.resolve(evaluate.expression(), null, scope, file, out);
            if (expression == null) {
                return null;
            }
            if (expected != null) {
                final Set<String> declared = new HashSet<>();
                errors(expression, declared);
                if (!declared.contains(expected)) {
                    out.report(Rule.TEST_UNDECLARED_ERROR, "No call of the statement declares the error '" + expected
                            + "'" + (expected.equals(INVALID_ARGUMENT) ? " and no validation annotation checks its "
                            + "values" : "") + (declared.isEmpty() ? "" : "; declared are: " + String.join(", ",
                            declared.stream().sorted().toList())), evaluate.location());
                    return null;
                }
                checks = true;
            }
            if (expression instanceof InstanceExpression.Call call && call.namespace().equals(Builtins.NAMESPACE)
                    && (call.method().name().startsWith("assert") || call.method().name().equals("fail"))) {
                checks = true;
                // assertNotNull(actual: x): x is not null in the rest of the block
                if (call.method().name().equals("assertNotNull")
                        && call.arguments().get("actual") instanceof InstanceExpression.Variable variable) {
                    scope.put(variable.name(), new InstanceExpression.Variable(variable.name(), variable.type(),
                            false));
                }
            }
            return new TestDefinition.Evaluate(expression, expected);
        }

        private TestDefinition.@Nullable Statement conditional(final Statement.If conditional,
                                                               final Map<String, InstanceExpression.Variable> scope) {
            final InstanceExpression condition = condition(conditional.condition(), scope, conditional.location());
            final Map<String, InstanceExpression.Variable> then = new LinkedHashMap<>(scope);
            final Map<String, InstanceExpression.Variable> otherwise = new LinkedHashMap<>(scope);
            // `if x is not null`: x is not null in the then block; `if x is null`: in the else block
            if (condition instanceof InstanceExpression.IsNull check
                    && check.operand() instanceof InstanceExpression.Variable variable) {
                (check.negated() ? then : otherwise).put(variable.name(), new InstanceExpression.Variable(
                        variable.name(), variable.type(), false));
            }
            final List<TestDefinition.Statement> thenStatements = statements(conditional.then(), then);
            final List<TestDefinition.Statement> otherwiseStatements = statements(conditional.otherwise(), otherwise);
            return condition == null ? null
                    : new TestDefinition.If(condition, thenStatements, otherwiseStatements);
        }

        private TestDefinition.@Nullable Statement loop(final Statement.For loop,
                                                        final Map<String, InstanceExpression.Variable> scope) {
            final InstanceExpression iterable = expressions.resolve(loop.iterable(), null, scope, file, out);
            final boolean declared = declare(loop.variable(), loop.location());
            if (iterable == null || !declared) {
                return null;
            }
            if (!(iterable.type() instanceof Type.BasicType basic)
                    || basic.builtin().category() != BuiltinType.Category.COLLECTION) {
                out.report(Rule.TEST_INVALID, "'for' needs a list or a set, not a '" + iterable.type().text() + "'",
                        loop.location());
                return null;
            }
            if (InstanceResolver.nullable(iterable)) {
                out.report(Rule.TEST_NULLABLE, "The list or set may be null; check it with assertNotNull or "
                        + "'is not null' first", loop.location());
                return null;
            }
            final Type element = basic.arguments().getFirst();
            final Map<String, InstanceExpression.Variable> inner = new LinkedHashMap<>(scope);
            inner.put(loop.variable(), new InstanceExpression.Variable(loop.variable(), element, false));
            return new TestDefinition.For(loop.variable(), element, iterable, statements(loop.body(), inner));
        }

        private @Nullable InstanceExpression condition(final Expression condition,
                                                       final Map<String, InstanceExpression.Variable> scope,
                                                       final SourceLocation location) {
            final InstanceExpression resolved = expressions.resolve(condition, basic("bool"), scope, file, out);
            if (resolved != null && InstanceResolver.nullable(resolved)) {
                out.report(Rule.TEST_NULLABLE, "The condition may be null", location);
                return null;
            }
            return resolved;
        }

        /** Registers a variable name; a name is bound at most once per test. */
        private boolean declare(final String name, final SourceLocation location) {
            if (values.contains(name)) {
                out.report(Rule.TEST_DUPLICATE_NAME, "'" + name + "' is the name of values of this spec", location);
                return false;
            }
            if (!names.add(name)) {
                out.report(Rule.TEST_DUPLICATE_NAME, "'" + name + "' is already bound in this test", location);
                return false;
            }
            return true;
        }
    }

    /** The error ids that the calls of an expression declare with {@code @@throws}. */
    /**
     * The error ids that an expression can raise: the ones its calls declare with {@code @@throws}, and
     * {@value #INVALID_ARGUMENT} for a value created or passed where a validation annotation or a {@code @@oneOf} /
     * {@code @@oneOrNoneOf} group checks it.
     */
    private void errors(final InstanceExpression expression, final Set<String> out) {
        switch (expression) {
            case InstanceExpression.Call call -> {
                throwsOf(call.method(), out);
                validated(call.method(), out);
                call.arguments().values().forEach(a -> errors(a, out));
            }
            case InstanceExpression.MethodCall call -> {
                throwsOf(call.method(), out);
                validated(call.method(), out);
                errors(call.target(), out);
                call.arguments().values().forEach(a -> errors(a, out));
            }
            case InstanceExpression.Construct construct -> {
                if (linked.definition(construct.type()) instanceof TypeDefinition.ComplexTypeDefinition type
                        && (type.fields().stream().anyMatch(f -> f.annotations().stream()
                        .anyMatch(a -> VALIDATION.contains(a.name())))
                        || type.annotations().stream().anyMatch(a -> checkedGroup(type, a)))) {
                    out.add(INVALID_ARGUMENT);
                }
                construct.values().values().forEach(v -> errors(v, out));
            }
            case InstanceExpression.Access access -> errors(access.target(), out);
            case InstanceExpression.ListValue list -> list.items().forEach(i -> errors(i, out));
            case InstanceExpression.Binary binary -> {
                errors(binary.left(), out);
                errors(binary.right(), out);
            }
            case InstanceExpression.Negate negate -> errors(negate.operand(), out);
            case InstanceExpression.Index index -> {
                errors(index.target(), out);
                errors(index.index(), out);
            }
            default -> {
                // no calls
            }
        }
    }

    /**
     * Whether an annotation is a {@code @@oneOf} or {@code @@oneOrNoneOf} group that the constructor checks: one of
     * immutable attributes (the attributes of a mutable group are set one by one, so it holds only when used).
     */
    private static boolean checkedGroup(final TypeDefinition.ComplexTypeDefinition type, final Annotation annotation) {
        return (annotation.name().equals("oneOf") || annotation.name().equals("oneOrNoneOf"))
                && annotation.arguments().stream().allMatch(name -> type.fields().stream()
                .anyMatch(f -> f.name().equals(name.text()) && f.hasAnnotation("immutable")));
    }

    private static void validated(final MethodDefinition method, final Set<String> out) {
        if (method.parameters().stream().anyMatch(p -> p.annotations().stream()
                .anyMatch(a -> VALIDATION.contains(a.name())))) {
            out.add(INVALID_ARGUMENT);
        }
    }

    private static void throwsOf(final MethodDefinition method, final Set<String> out) {
        method.annotations().stream().filter(a -> a.name().equals("throws"))
                .flatMap(a -> a.arguments().stream()).map(Literal::text).forEach(out::add);
    }

    /** The name of a test in kebab-case: {@code fromString rejects "x"} becomes {@code fromstring-rejects-x}. */
    static String kebab(final String name) {
        return name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }

    private static String specName(final SchemaFile file) {
        final String path = file.file();
        final String name = path.substring(path.lastIndexOf('/') + 1);
        return name.endsWith(".md") ? name.substring(0, name.length() - 3) : name;
    }

    private static Type basic(final String name) {
        return new Type.BasicType(BuiltinType.lookup(name).orElseThrow(), List.of());
    }
}
