package org.hiero.sdk.v3.metalang.generator.ts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.hiero.sdk.v3.metalang.ast.Expression.Operator;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.generator.SpecFolders;
import org.hiero.sdk.v3.metalang.model.Builtins;
import org.hiero.sdk.v3.metalang.model.InstanceExpression;
import org.hiero.sdk.v3.metalang.model.MethodDefinition;
import org.hiero.sdk.v3.metalang.model.ParameterDefinition;
import org.hiero.sdk.v3.metalang.model.TestDefinition;
import org.hiero.sdk.v3.metalang.model.TestValuesDefinition;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.model.TypeDefinition;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;

/**
 * Generates the tests of the {@code tests} blocks of a spec ({@code guidelines/test-language.md}) as one
 * {@code <spec>.spec.test.ts} with the Node.js test runner, in the directory of the spec's namespace. The built-in
 * functions and assertions are those of the hand-written package {@code <scope>/test-support}; integers of the test
 * language are {@code bigint} and checked against their type where they reach the API, {@code ==} is the value
 * equality of the test language, named test values are module constants, default instances are helper functions
 * ({@code defaultPrivateKey()}). A test without TypeScript form is not generated and listed at the top of the file.
 */
final class TsSpecTestGenerator {

    private static final String INDENT = "    ";

    /** The prefix of a line that lists a test that is not generated. */
    private static final String NOTE = "// - ";

    /** Built-in functions whose overloads differ by a numeric type, which TypeScript cannot tell apart at runtime. */
    private static final Set<String> NUMERIC_OVERLOADS = Set.of("abs", "min", "max", "toDouble", "toDecimal",
            "toString");

    private TsSpecTestGenerator() {
    }

    /**
     * The name of the test file of a spec: {@code native-token} becomes {@code native-token.spec.test.ts}.
     *
     * @param specName the name of the spec file without extension
     * @return the file name
     */
    static String fileName(final String specName) {
        return specName + ".spec.test.ts";
    }

    /**
     * Generates the test file of a spec.
     *
     * @param namespace the namespace
     * @param specName  the name of the spec file without extension
     * @param tests     the tests of the spec
     * @param values    the named test values of the spec
     * @param context   the generation context
     * @return the test file
     */
    static GeneratedFile generate(final String namespace, final String specName, final List<TestDefinition> tests,
                                  final List<TestValuesDefinition> values, final TsContext context) {
        final TestFile file = new TestFile(namespace, context, names(tests, values));
        values.forEach(file::values);
        tests.forEach(file::test);
        return file.render(specName);
    }

    /** Every name the file binds: imports must not be hidden by them. */
    private static String[] names(final List<TestDefinition> tests, final List<TestValuesDefinition> values) {
        final Set<String> names = new HashSet<>();
        values.forEach(v -> names.add(TsNames.local(v.name())));
        tests.forEach(t -> names(t.body(), names));
        return names.toArray(String[]::new);
    }

    private static void names(final List<TestDefinition.Statement> statements, final Set<String> out) {
        for (final TestDefinition.Statement statement : statements) {
            switch (statement) {
                case TestDefinition.Let let -> out.add(TsNames.local(let.name()));
                case TestDefinition.For loop -> {
                    out.add(TsNames.local(loop.variable()));
                    names(loop.body(), out);
                }
                case TestDefinition.If conditional -> {
                    names(conditional.then(), out);
                    names(conditional.otherwise(), out);
                }
                default -> {
                    // binds nothing
                }
            }
        }
    }

    /** A part of a test that has no TypeScript form; the test is then not generated. */
    private static final class Unsupported extends RuntimeException {

        private static final long serialVersionUID = 1L;

        private Unsupported(final String reason) {
            super(reason, null, false, false);
        }
    }

    /** The content of one test file. */
    private static final class TestFile {

        private final TsContext context;
        private final String directory;
        private final TsImports imports;
        private final TsSamples samples;
        /** The functions that create the named test values, by name. */
        private final Map<String, String> constants = new LinkedHashMap<>();
        private final List<String> tests = new ArrayList<>();
        /** The functions that return the default instances, by name. */
        private final Map<String, String> helpers = new LinkedHashMap<>();
        private final List<String> notes = new ArrayList<>();
        /** The named test values without a TypeScript form. */
        private final Set<String> missingValues = new HashSet<>();

        private TestFile(final String namespace, final TsContext context, final String... ownNames) {
            this.context = context;
            final String folder = context.folder(namespace);
            this.directory = TsNames.sourceDirectory(folder, namespace);
            final String[] own = java.util.stream.Stream.concat(java.util.stream.Stream.of(ownNames),
                    java.util.stream.Stream.of("describe", "test", "assert")).toArray(String[]::new);
            this.imports = new TsImports(context, folder, directory, own);
            final Set<String> visible = new HashSet<>(SpecFolders.required(folder, context.folders()));
            visible.add(folder);
            this.samples = new TsSamples(context, imports, visible::contains);
            samples.testExpressions(e -> Optional.of(expression(e)), (value, target) -> Optional.of(converted(value,
                    target)));
        }

        // ------------------------------------------------------------------------------------------------ tests

        /**
         * Named test values: a function of the module that creates them where a test uses them - a value that cannot be
         * created fails only the tests that use it, with its cause. A test that uses values without TypeScript form is
         * not generated.
         */
        private void values(final TestValuesDefinition values) {
            final String value;
            try {
                value = "function " + TsNames.local(values.name()) + "(): " + TsTypes.declaration(values.type(), false,
                        imports) + " {\n" + INDENT + "return " + converted(values.value(), values.type()) + ";\n}\n";
            } catch (final Unsupported e) {
                notes.add("values `" + values.name() + "`: " + e.getMessage());
                missingValues.add(values.name());
                return;
            }
            final StringBuilder ts = new StringBuilder();
            if (!values.documentation().isBlank()) {
                ts.append("/**\n");
                values.documentation().lines().forEach(l -> ts.append((" * " + l).stripTrailing()).append('\n'));
                ts.append(" */\n");
            }
            constants.put(TsNames.local(values.name()), ts.append(value).toString());
        }

        private void test(final TestDefinition test) {
            final String body;
            try {
                body = statements(test.body(), 2);
            } catch (final Unsupported e) {
                notes.add("`" + test.name() + "`: " + e.getMessage());
                return;
            }
            final StringBuilder ts = new StringBuilder();
            ts.append(INDENT).append("// `").append(test.id()).append("`\n");
            if (!test.documentation().isBlank()) {
                ts.append(INDENT).append("//\n");
                test.documentation().lines().forEach(l -> ts.append((INDENT + "// " + l).stripTrailing())
                        .append('\n'));
            }
            test.annotation("deprecated").ifPresent(a -> ts.append(INDENT).append("//\n").append(INDENT)
                    .append("// Deprecated: ").append(((Literal.StringLiteral) a.arguments().getFirst()).value())
                    .append('\n'));
            final String options = test.annotation("disabled").map(a -> "{ skip: " + TsLiterals.quote(
                    ((Literal.StringLiteral) a.arguments().getFirst()).value()) + " }, ").orElse("");
            final boolean async = body.contains("await ");
            ts.append(INDENT).append("test(").append(TsLiterals.quote(test.name())).append(", ").append(options)
                    .append(async ? "async " : "").append("() => {\n").append(body).append(INDENT).append("});\n");
            tests.add(ts.toString());
        }

        // ------------------------------------------------------------------------------------------- statements

        /**
         * The statements of a block. The comments of the spec are kept: a trailing comment stays behind its statement,
         * and a comment that follows a statement starts a new paragraph (given / when / then).
         */
        private String statements(final List<TestDefinition.Statement> statements, final int depth) {
            final StringBuilder ts = new StringBuilder();
            TestDefinition.Statement previous = null;
            for (final TestDefinition.Statement statement : statements) {
                if (statement instanceof TestDefinition.Comment comment && comment.trailing()) {
                    ts.setLength(ts.length() - 1);
                    ts.append(" // ").append(comment.text()).append('\n');
                } else {
                    if (statement instanceof TestDefinition.Comment && previous != null
                            && !(previous instanceof TestDefinition.Comment)) {
                        ts.append('\n');
                    }
                    ts.append(statement(statement, depth));
                }
                previous = statement;
            }
            return ts.toString();
        }

        private String statement(final TestDefinition.Statement statement, final int depth) {
            final String indent = INDENT.repeat(depth);
            return switch (statement) {
                case TestDefinition.Comment comment -> (indent + "// " + comment.text()).stripTrailing() + "\n";
                case TestDefinition.Let let -> indent + "const " + TsNames.local(let.name()) + ": "
                        + declaration(let.type(), let.nullable()) + " = " + converted(let.value(), let.type()) + ";\n";
                case TestDefinition.Evaluate evaluate when evaluate.expectedError() != null -> {
                    final TsContext.ErrorClass error = context.error(evaluate.expectedError());
                    final String errorClass = error.namespace() == null ? error.name() : imports.error(error, true);
                    final String call = expression(evaluate.expression());
                    yield call.contains("await ")
                            ? indent + "await assert.rejects(async () => " + call + ", " + errorClass + ");\n"
                            : indent + "assert.throws(() => " + call + ", " + errorClass + ");\n";
                }
                case TestDefinition.Evaluate evaluate -> indent + expression(evaluate.expression()) + ";\n";
                case TestDefinition.If conditional -> indent + conditional(conditional, depth) + "\n";
                case TestDefinition.For loop -> indent + "for (const " + TsNames.local(loop.variable()) + " of "
                        + expression(loop.iterable()) + ") {\n" + statements(loop.body(), depth + 1) + indent + "}\n";
            };
        }

        /** {@code if (...) { ... } else if (...) { ... } else { ... }}, without the indentation of the first line. */
        private String conditional(final TestDefinition.If conditional, final int depth) {
            final String indent = INDENT.repeat(depth);
            final StringBuilder ts = new StringBuilder("if (").append(expression(conditional.condition()))
                    .append(") {\n").append(statements(conditional.then(), depth + 1)).append(indent).append('}');
            if (conditional.otherwise().size() == 1
                    && conditional.otherwise().getFirst() instanceof TestDefinition.If elseIf) {
                ts.append(" else ").append(conditional(elseIf, depth));
            } else if (!conditional.otherwise().isEmpty()) {
                ts.append(" else {\n").append(statements(conditional.otherwise(), depth + 1)).append(indent)
                        .append('}');
            }
            return ts.toString();
        }

        private String declaration(final Type type, final boolean nullable) {
            return TsTypes.declaration(type, nullable, imports);
        }

        // ------------------------------------------------------------------------------------------ expressions

        /** An expression; the API parts are rendered by {@link TsSamples}, the parts of tests here. */
        private String expression(final InstanceExpression expression) {
            return switch (expression) {
                case InstanceExpression.Variable variable when missingValues.contains(variable.name()) ->
                        throw new Unsupported("the values " + variable.name() + " have no TypeScript form");
                case InstanceExpression.Variable variable when constants.containsKey(TsNames.local(variable.name())) ->
                        TsNames.local(variable.name()) + "()";
                case InstanceExpression.Variable variable -> TsNames.local(variable.name());
                case InstanceExpression.Default value -> defaultValue(value.type());
                case InstanceExpression.Negate negate -> negate(negate);
                case InstanceExpression.Binary binary -> binary(binary);
                case InstanceExpression.IsNull isNull -> operand(isNull.operand()) + (isNull.negated() ? " !== null"
                        : " === null");
                case InstanceExpression.IsType isType -> isType(isType);
                case InstanceExpression.Index index -> testing("get") + "(" + expression(index.target()) + ", "
                        + (category(index.index().type()) == BuiltinType.Category.INTEGER
                        && category(index.target().type()) == BuiltinType.Category.COLLECTION
                        ? integer(index.index()) : expression(index.index())) + ")";
                case InstanceExpression.ListValue list -> list(list);
                case InstanceExpression.MapValue map -> map(map);
                case InstanceExpression.Call call when call.namespace().equals(Builtins.NAMESPACE) -> builtin(call);
                default -> samples.expression(expression).orElseThrow(() -> new Unsupported(
                        "no TypeScript form for a value of " + expression.type().text()));
            };
        }

        /**
         * The default instance of a type: a call of a helper function {@code defaultPrivateKey()}, like the value
         * helpers of the other generated tests, so that a test reads as the spec does.
         */
        private String defaultValue(final Type type) {
            if (!(type instanceof Type.DeclaredType declared)) {
                return samples.value(type, List.of(), 0, false).orElseThrow(() -> new Unsupported("no value of "
                        + type.text()));
            }
            final String name = "default" + declared.name().name() + declared.arguments().stream()
                    .map(a -> a instanceof Type.DeclaredType d ? d.name().name() : "")
                    .collect(Collectors.joining());
            if (!helpers.containsKey(name)) {
                helpers.put(name, ""); // reserved: the value may use other default instances
                final Optional<String> value = samples.value(type, List.of(), 0, false);
                if (value.isEmpty()) {
                    helpers.remove(name);
                    throw new Unsupported("no value of " + type.text());
                }
                helpers.put(name, "function " + name + "(): " + declaration(type, false) + " {\n" + INDENT
                        + "return " + value.get() + ";\n}\n");
            }
            return name + "()";
        }

        /** An operand of an operator: parenthesized if it is rendered with a TypeScript operator itself. */
        private String operand(final InstanceExpression expression) {
            final String ts = expression(expression);
            return infix(expression) ? "(" + ts + ")" : ts;
        }

        /** Whether the TypeScript form of an expression is an operator expression (not a function call). */
        private boolean infix(final InstanceExpression expression) {
            return switch (expression) {
                case InstanceExpression.IsNull ignored -> true;
                case InstanceExpression.IsType ignored -> true;
                // an operand of an operator is a number or a bool: decimals, bytes and lists are compared and joined
                // with function calls, which take no operands
                case InstanceExpression.Negate ignored -> true;
                // `equal(a, b)` and `!equal(a, b)` need no parentheses, `a === b` of enums does
                case InstanceExpression.Binary binary when binary.operator() == Operator.EQUAL
                        || binary.operator() == Operator.NOT_EQUAL ->
                        isEnum(binary.left().type()) && isEnum(binary.right().type());
                case InstanceExpression.Binary ignored -> true;
                default -> false;
            };
        }

        private String negate(final InstanceExpression.Negate negate) {
            return switch (category(negate.operand().type())) {
                case INTEGER -> "-" + integerOperand(negate.operand());
                case DECIMAL -> testing("negateDecimal") + "(" + expression(negate.operand()) + ")";
                default -> "-" + operand(negate.operand());
            };
        }

        private String binary(final InstanceExpression.Binary binary) {
            final Operator operator = binary.operator();
            final BuiltinType.Category category = category(binary.left().type());
            return switch (operator) {
                case AND, OR -> operand(binary.left()) + " " + operator.symbol() + " " + operand(binary.right());
                case EQUAL, NOT_EQUAL -> {
                    if (isEnum(binary.left().type()) && isEnum(binary.right().type())) {
                        yield operand(binary.left()) + (operator == Operator.EQUAL ? " === " : " !== ")
                                + operand(binary.right());
                    }
                    yield (operator == Operator.NOT_EQUAL ? "!" : "") + testing("equal") + "("
                            + comparable(binary.left(), binary.right()) + ")";
                }
                case LESS, LESS_OR_EQUAL, GREATER, GREATER_OR_EQUAL -> switch (category) {
                    case INTEGER -> integerOperand(binary.left()) + " " + operator.symbol() + " "
                            + integerOperand(binary.right());
                    case FLOAT -> operand(binary.left()) + " " + operator.symbol() + " " + operand(binary.right());
                    case DECIMAL -> testing("compareDecimal") + "(" + expression(binary.left()) + ", "
                            + expression(binary.right()) + ") " + operator.symbol() + " 0";
                    default -> testing("compare") + "(" + expression(binary.left()) + ", "
                            + expression(binary.right()) + ") " + operator.symbol() + " 0";
                };
                default -> switch (category) {
                    case INTEGER -> integerOperand(binary.left()) + " " + operator.symbol() + " "
                            + integerOperand(binary.right());
                    case DECIMAL -> testing(decimalFunction(operator)) + "(" + expression(binary.left()) + ", "
                            + expression(binary.right()) + ")";
                    case BYTES, COLLECTION -> testing("concat") + "(" + expression(binary.left()) + ", "
                            + expression(binary.right()) + ")";
                    default -> operand(binary.left()) + " " + operator.symbol() + " " + operand(binary.right());
                };
            };
        }

        private static String decimalFunction(final Operator operator) {
            return switch (operator) {
                case ADD -> "addDecimal";
                case SUBTRACT -> "subtractDecimal";
                case MULTIPLY -> "multiplyDecimal";
                default -> throw new Unsupported("'" + operator.symbol() + "' on decimals");
            };
        }

        private String isType(final InstanceExpression.IsType isType) {
            if (!(isType.checked() instanceof Type.DeclaredType declared) || !context.isClass(declared.name())
                    && !context.isEnum(declared.name())) {
                throw new Unsupported("'is " + isType.checked().text() + "': only a class has a type at runtime");
            }
            final String check = operand(isType.operand()) + " instanceof " + imports.value(declared.name());
            return isType.negated() ? "!(" + check + ")" : check;
        }

        /** The operands of an equality check; decimals are compared without their scale. */
        private String comparable(final InstanceExpression left, final InstanceExpression right) {
            if (category(left.type()) == BuiltinType.Category.DECIMAL
                    && category(right.type()) == BuiltinType.Category.DECIMAL) {
                return testing("decimal") + "(" + expression(left) + "), " + testing("decimal") + "("
                        + expression(right) + ")";
            }
            return expression(left) + ", " + expression(right);
        }

        /** An integer as exact {@code bigint}. */
        private String integer(final InstanceExpression expression) {
            if (expression instanceof InstanceExpression.Value value
                    && value.literal() instanceof Literal.NumberLiteral number) {
                return new java.math.BigInteger(number.text().replace("_", "")) + "n";
            }
            final String ts = expression(expression);
            return isBigInt(expression.type()) ? ts : testing("int") + "(" + ts + ")";
        }

        /** An integer as exact {@code bigint}, parenthesized as operand of an operator. */
        private String integerOperand(final InstanceExpression expression) {
            return isBigInt(expression.type()) ? operand(expression) : integer(expression);
        }

        /** Enum constants are compared by identity: each constant exists once. */
        private boolean isEnum(final Type type) {
            return type instanceof Type.DeclaredType declared && context.model().type(declared.name())
                    .filter(TypeDefinition.EnumDefinition.class::isInstance).isPresent();
        }

        private static boolean isBigInt(final Type type) {
            return type instanceof Type.BasicType basic && TsTypes.isBigInt(basic.builtin());
        }

        /**
         * A value passed to a variable, parameter or attribute of the given type: an exact integer of the test
         * language is checked against the range of the type and converted to its TypeScript type.
         */
        private String converted(final InstanceExpression value, final Type target) {
            // a type literal (the resolver gives it its type as argument: type<Receipt>) is the class of the type
            if (value instanceof InstanceExpression.Value && value.type() instanceof Type.BasicType basic
                    && basic.builtin().category() == BuiltinType.Category.TYPE && !basic.arguments().isEmpty()) {
                return typeLiteral(basic.arguments().getFirst());
            }
            if (!(value.type() instanceof Type.BasicType from)
                    || from.builtin().category() != BuiltinType.Category.INTEGER
                    || !(target instanceof Type.BasicType to) || to.builtin().category() != BuiltinType.Category.INTEGER
                    || to.builtin().name().equals(from.builtin().name())) {
                return switch (value) {
                    case InstanceExpression.ListValue list when target instanceof Type.BasicType basic
                            && basic.builtin().category() == BuiltinType.Category.COLLECTION ->
                            list(new InstanceExpression.ListValue(list.items(), target));
                    case InstanceExpression.MapValue map when target instanceof Type.BasicType basic
                            && basic.builtin().category() == BuiltinType.Category.MAP ->
                            map(new InstanceExpression.MapValue(map.entries(), target));
                    default -> expression(value);
                };
            }
            if (value instanceof InstanceExpression.Value literal) {
                return TsLiterals.expression(literal.literal(), target, imports);
            }
            final String exact = integer(value);
            return TsTypes.isBigInt(to.builtin())
                    ? testing("checked") + "(" + exact + ", " + TsLiterals.quote(to.builtin().name()) + ")"
                    : testing("checkedNumber") + "(" + exact + ", " + TsLiterals.quote(to.builtin().name()) + ")";
        }

        private String list(final InstanceExpression.ListValue list) {
            final Type.BasicType type = (Type.BasicType) list.type();
            if (type.builtin().category() == BuiltinType.Category.BYTES) {
                // the elements of a bytes literal are numbers
                return "new Uint8Array([" + list.items().stream().map(i -> ((InstanceExpression.Value) i).literal()
                        .text()).collect(Collectors.joining(", ")) + "])";
            }
            final Type element = type.arguments().getFirst();
            final boolean concrete = concrete(element);
            final String items = list.items().stream().map(i -> concrete ? converted(i, element) : expression(i))
                    .collect(Collectors.joining(", "));
            if (!type.builtin().name().equals("set")) {
                return "[" + items + "]";
            }
            return "new Set" + (concrete ? "<" + declaration(element, false) + ">" : "") + "([" + items + "])";
        }

        private String map(final InstanceExpression.MapValue map) {
            final Type.BasicType type = (Type.BasicType) map.type();
            final Type key = type.arguments().get(0);
            final Type value = type.arguments().get(1);
            // the literal of a generic parameter (keys(value: {...})) has no type of its own: TypeScript infers it
            final boolean concrete = concrete(key) && concrete(value);
            return "new Map" + (concrete ? "<" + declaration(key, false) + ", " + declaration(value, false) + ">" : "")
                    + "([" + map.entries().stream().map(e -> "[" + (concrete ? converted(e.getKey(), key)
                    : expression(e.getKey())) + ", " + (concrete ? converted(e.getValue(), value)
                    : expression(e.getValue())) + "]").collect(Collectors.joining(", ")) + "])";
        }

        /** Whether a type has no type variables. */
        private static boolean concrete(final Type type) {
            return switch (type) {
                case Type.TypeVariable ignored -> false;
                case Type.BasicType basic -> basic.arguments().stream().allMatch(TestFile::concrete);
                case Type.DeclaredType declared -> declared.arguments().stream().allMatch(TestFile::concrete);
                default -> true;
            };
        }

        /** A built-in function or assertion: a function of the test support package, arguments in order. */
        private String builtin(final InstanceExpression.Call call) {
            final MethodDefinition method = call.method();
            final String name = method.name();
            if (name.equals("not")) {
                return "!" + operand(call.arguments().get("value"));
            }
            final List<String> arguments = new ArrayList<>();
            final boolean compares = name.equals("assertEquals") || name.equals("assertNotEquals");
            if (compares) {
                final InstanceExpression first = call.arguments().get(method.parameters().getFirst().name());
                arguments.add(comparable(first, call.arguments().get("actual")));
            }
            for (final ParameterDefinition parameter : method.parameters()) {
                final InstanceExpression value = call.arguments().get(parameter.name());
                if (value == null || compares && !parameter.name().equals("message")) {
                    continue; // an omitted optional parameter (message)
                }
                arguments.add(argument(value, parameter.type()));
            }
            return testing(functionName(method)) + "(" + String.join(", ", arguments) + ")";
        }

        /**
         * The name of a built-in function in the test support package. Overloads that differ by a numeric type get a
         * suffix ({@code absDouble}, {@code toDecimalDouble}), because {@code number} and {@code string} also stand for
         * other types; {@code toString} is {@code text}.
         */
        private static String functionName(final MethodDefinition method) {
            final String name = method.name().equals("toString") ? "text" : method.name();
            if (!NUMERIC_OVERLOADS.contains(method.name()) || method.parameters().isEmpty()) {
                return name;
            }
            return switch (category(method.parameters().getFirst().type())) {
                case FLOAT -> name + "Double";
                case DECIMAL -> name + "Decimal";
                default -> name;
            };
        }

        /** An argument of a built-in function: a type literal is a class, integers are exact. */
        private String argument(final InstanceExpression value, final Type parameter) {
            if (value.type() instanceof Type.BasicType basic && basic.builtin().category() == BuiltinType.Category.TYPE) {
                return typeLiteral(basic.arguments().getFirst());
            }
            if (parameter instanceof Type.BasicType target && target.builtin().category() == BuiltinType.Category.INTEGER
                    && TsTypes.isBigInt(target.builtin()) && target.builtin().bits() > 64) {
                return integer(value);
            }
            return converted(value, parameter);
        }

        /** The class that stands for a type at runtime (a type literal names a declared type; enums are classes). */
        private String typeLiteral(final Type type) {
            if (type instanceof Type.DeclaredType declared && (context.isClass(declared.name())
                    || context.isEnum(declared.name()))) {
                return imports.value(declared.name());
            }
            throw new Unsupported("the type " + type.text() + " has no class at runtime");
        }

        private String testing(final String name) {
            return imports.testSupport(name);
        }

        private static BuiltinType.Category category(final Type type) {
            return type instanceof Type.BasicType basic ? basic.builtin().category() : BuiltinType.Category.TYPE;
        }

        // ------------------------------------------------------------------------------------------- rendering

        private GeneratedFile render(final String specName) {
            final List<String> all = new ArrayList<>(tests);
            notes.stream().distinct().forEach(n -> all.add(INDENT + "test.todo(" + TsLiterals.quote("not generated: "
                    + n) + ");\n"));
            final StringBuilder code = new StringBuilder();
            // only the named test values that a generated test uses (an unused function is a compiler error)
            for (final Map.Entry<String, String> constant : constants.entrySet()) {
                if (String.join("", all).contains(constant.getKey() + "()")) {
                    code.append(constant.getValue()).append('\n');
                }
            }
            code.append("describe(").append(TsLiterals.quote(specName)).append(", () => {\n");
            code.append(String.join("\n", all));
            code.append("});\n");
            // only the default instances that a generated test uses, directly or through another default instance (an
            // unused function is a compiler error, e.g. when no test of the spec could be generated)
            final StringBuilder used = new StringBuilder(String.join("", all));
            final java.util.Set<String> included = new java.util.LinkedHashSet<>();
            boolean grown = true;
            while (grown) {
                grown = false;
                for (final Map.Entry<String, String> helper : helpers.entrySet()) {
                    if (!included.contains(helper.getKey()) && used.indexOf(helper.getKey() + "()") >= 0) {
                        included.add(helper.getKey());
                        used.append(helper.getValue());
                        grown = true;
                    }
                }
            }
            for (final Map.Entry<String, String> helper : helpers.entrySet()) {
                if (included.contains(helper.getKey())) {
                    code.append('\n').append(helper.getValue());
                }
            }
            final StringBuilder ts = new StringBuilder(TsGenerator.HEADER);
            ts.append("//\n// Tests of the spec `").append(specName).append("`, generated from the `tests` blocks of")
                    .append(" its `## Testing` section.\n");
            if (!notes.isEmpty()) {
                ts.append("//\n// Not generated:\n");
                notes.stream().distinct().forEach(n -> ts.append(NOTE).append(n).append('\n'));
            }
            ts.append('\n');
            // only if a generated test uses it (an unused import is a compiler error)
            if (java.util.regex.Pattern.compile("(?<![\\w$])assert\\.").matcher(code).find()) {
                ts.append("import assert from \"node:assert/strict\";\n");
            }
            ts.append("import { describe, test } from \"node:test\";\n");
            ts.append(imports.render(code.toString())).append('\n');
            ts.append(code);
            return new GeneratedFile(directory + "/" + fileName(specName), ts.toString());
        }
    }
}
