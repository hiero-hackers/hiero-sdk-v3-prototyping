package org.hiero.sdk.v3.metalang.generator.java;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.hiero.sdk.v3.metalang.ast.Expression.Operator;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.model.Builtins;
import org.hiero.sdk.v3.metalang.model.FunctionDefinition;
import org.hiero.sdk.v3.metalang.model.InstanceExpression;
import org.hiero.sdk.v3.metalang.model.MethodDefinition;
import org.hiero.sdk.v3.metalang.model.ParameterDefinition;
import org.hiero.sdk.v3.metalang.model.QualifiedName;
import org.hiero.sdk.v3.metalang.model.TestDefinition;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;

/**
 * Generates the JUnit tests of the {@code tests} blocks of the specs (see {@code guidelines/test-language.md}): one
 * test class per spec, {@code <Spec>SpecTest} in {@code src/test/java} of the module and in the package of the
 * spec's namespace, next to the tests of {@link TestGenerator} and in their style.
 *
 * <p>The tests use the API like a user: values come from the default instances and calls ({@link JavaSamples}), the
 * built-in functions and assertions from the hand-written test support module ({@value #TESTING}, artifact
 * {@value #ARTIFACT}), which implements the semantics of the test language where Java behaves differently: exact
 * integers on {@link java.math.BigInteger}, code-point strings, value equality. Expected errors
 * ({@code @@expectThrows}) become {@code assertThrows} with the exception of the error id.
 *
 * <p>A test that cannot be expressed in Java (a value that has no Java form yet) is not generated and listed in the
 * comment of the test class, like the tests {@link TestGenerator} cannot build.
 */
final class SpecTestGenerator {

    /** The class with the built-in functions and assertions. */
    static final String TESTING = "org.hiero.sdk.testing.Testing";

    /** The artifact of the test support module (sdk-java/test-support). */
    static final String ARTIFACT = "hiero-sdk-test-support";

    private static final String INDENT = "    ";

    /** The built-in functions whose Java name differs: {@code toString} would be hidden by {@link Object#toString()}. */
    private static final Map<String, String> RENAMED = Map.of("toString", "text");

    private SpecTestGenerator() {
    }

    /**
     * The name of the test class of a spec: {@code native-token} becomes {@code NativeTokenSpecTest}.
     *
     * @param specName the name of the spec file without extension
     * @return the class name
     */
    static String className(final String specName) {
        return java.util.Arrays.stream(specName.split("[^A-Za-z0-9]+")).filter(p -> !p.isEmpty())
                .map(p -> Character.toUpperCase(p.charAt(0)) + p.substring(1)).collect(Collectors.joining())
                + "SpecTest";
    }

    /**
     * Generates the test class of a spec.
     *
     * @param module    the Java module of the spec's namespace
     * @param namespace the namespace
     * @param specName  the name of the spec file without extension
     * @param tests     the tests of the spec
     * @param context   the generation context
     * @param visible   the Java modules the tests can use: the module and the modules it requires
     * @param functions the generated namespace-level functions
     * @return the test file, empty if the class name is taken by a type of the specs
     */
    static Optional<GeneratedFile> generate(final String module, final String namespace, final String specName,
                                            final List<TestDefinition> tests, final JavaContext context,
                                            final Set<String> visible, final List<FunctionDefinition> functions) {
        final String className = className(specName);
        if (context.model().type(new QualifiedName(namespace, className)).isPresent()) {
            return Optional.empty();
        }
        final TestFile file = new TestFile(namespace, className, context, visible, functions);
        tests.forEach(file::test);
        return Optional.of(new GeneratedFile(TestGenerator.path(module, namespace, className),
                file.render(specName)));
    }

    /** A part of a test that has no Java form; the test is then not generated. */
    private static final class Unsupported extends RuntimeException {

        private static final long serialVersionUID = 1L;

        private Unsupported(final String reason) {
            super(reason, null, false, false);
        }
    }

    /** The content of one test class. */
    private static final class TestFile {

        private final String namespace;
        private final String className;
        private final JavaContext context;
        private final Imports imports;
        private final JavaSamples samples;
        private final List<String> tests = new ArrayList<>();
        /** The methods that return the default instances, by name. */
        private final Map<String, String> helpers = new java.util.LinkedHashMap<>();
        private final List<String> notes = new ArrayList<>();
        private final Set<String> methodNames = new HashSet<>();
        private boolean usesAssertThrows;
        private boolean deprecated;
        /** Whether the current test calls a method that declares a checked exception. */
        private boolean throwsChecked;

        private TestFile(final String namespace, final String className, final JavaContext context,
                         final Set<String> visible, final List<FunctionDefinition> functions) {
            this.namespace = namespace;
            this.className = className;
            this.context = context;
            this.imports = context.imports(JavaNames.packageName(namespace), className);
            this.samples = new JavaSamples(context, imports,
                    name -> context.module(name.namespace()).filter(visible::contains).isPresent(), functions);
            samples.testExpressions(e -> Optional.of(expression(e)),
                    (value, target) -> Optional.of(converted(value, target)));
        }

        // ------------------------------------------------------------------------------------------------ tests

        private void test(final TestDefinition test) {
            throwsChecked = checked(test.body());
            final String body;
            try {
                body = statements(test.body(), 2);
            } catch (final Unsupported e) {
                notes.add("`" + test.name() + "`: " + e.getMessage());
                return;
            }
            deprecated |= test.annotation("deprecated").isPresent();
            final StringBuilder java = new StringBuilder();
            java.append(INDENT).append("/// `").append(test.id()).append("`\n");
            if (!test.documentation().isBlank()) {
                java.append(INDENT).append("///\n");
                test.documentation().lines().forEach(l -> java.append(INDENT).append("/// ").append(l).append('\n'));
            }
            test.annotation("deprecated").ifPresent(a -> java.append(INDENT).append("///\n").append(INDENT)
                    .append("/// Deprecated: ").append(((Literal.StringLiteral) a.arguments().getFirst()).value())
                    .append('\n'));
            java.append(INDENT).append('@').append(imports.use(TestGenerator.JUNIT, "Test")).append('\n');
            java.append(INDENT).append('@').append(imports.use(TestGenerator.JUNIT, "DisplayName")).append('(')
                    .append(JavaLiterals.quote(test.name())).append(")\n");
            test.annotation("disabled").ifPresent(a -> java.append(INDENT).append('@')
                    .append(imports.use(TestGenerator.JUNIT, "Disabled")).append('(')
                    .append(JavaLiterals.quote(((Literal.StringLiteral) a.arguments().getFirst()).value()))
                    .append(")\n"));
            java.append(INDENT).append("void ").append(methodName(test.name())).append("()")
                    .append(throwsChecked ? " throws Exception" : "").append(" {\n").append(body).append(INDENT)
                    .append("}\n");
            tests.add(java.toString());
        }

        /** Whether a statement calls a method that declares a checked exception (the test then declares it). */
        private boolean checked(final List<TestDefinition.Statement> statements) {
            return statements.stream().anyMatch(statement -> switch (statement) {
                case TestDefinition.Comment ignored -> false;
                case TestDefinition.Let let -> checked(let.value());
                case TestDefinition.Evaluate evaluate -> checked(evaluate.expression());
                case TestDefinition.If conditional -> checked(conditional.condition()) || checked(conditional.then())
                        || checked(conditional.otherwise());
                case TestDefinition.For loop -> checked(loop.iterable()) || checked(loop.body());
            });
        }

        private boolean checked(final InstanceExpression expression) {
            return switch (expression) {
                case InstanceExpression.Call call -> declaresChecked(call.method())
                        || call.arguments().values().stream().anyMatch(this::checked);
                case InstanceExpression.MethodCall call -> declaresChecked(call.method()) || checked(call.target())
                        || call.arguments().values().stream().anyMatch(this::checked);
                case InstanceExpression.Construct construct -> construct.values().values().stream()
                        .anyMatch(this::checked);
                case InstanceExpression.Access access -> checked(access.target());
                case InstanceExpression.ListValue list -> list.items().stream().anyMatch(this::checked);
                case InstanceExpression.Binary binary -> checked(binary.left()) || checked(binary.right());
                case InstanceExpression.Negate negate -> checked(negate.operand());
                case InstanceExpression.Index index -> checked(index.target()) || checked(index.index());
                case InstanceExpression.IsNull isNull -> checked(isNull.operand());
                case InstanceExpression.IsType isType -> checked(isType.operand());
                case InstanceExpression.MapValue map -> map.entries().stream()
                        .anyMatch(e -> checked(e.getKey()) || checked(e.getValue()));
                default -> false;
            };
        }

        private boolean declaresChecked(final MethodDefinition method) {
            return method.annotation("throws").stream().flatMap(t -> t.arguments().stream())
                    .anyMatch(id -> context.exception(id.text()).checked());
        }

        /** The test name in lowerCamelCase: {@code fromString reads "1"} becomes {@code fromStringReads1}. */
        private String methodName(final String name) {
            final String[] words = name.split("[^A-Za-z0-9]+");
            final StringBuilder result = new StringBuilder();
            for (final String word : words) {
                if (word.isEmpty()) {
                    continue;
                }
                result.append(result.isEmpty() ? Character.toLowerCase(word.charAt(0))
                        : Character.toUpperCase(word.charAt(0))).append(word.substring(1));
            }
            String base = result.isEmpty() || !Character.isJavaIdentifierStart(result.charAt(0)) ? "test" + result
                    : JavaKeywords.identifier(result.toString());
            String unique = base;
            for (int i = 2; !methodNames.add(unique); i++) {
                unique = base + i;
            }
            return unique;
        }

        // ------------------------------------------------------------------------------------------- statements

        /**
         * The statements of a block. The comments of the spec are kept: a trailing comment stays behind its statement,
         * and a comment that follows a statement starts a new paragraph (given / when / then).
         */
        private String statements(final List<TestDefinition.Statement> statements, final int depth) {
            final StringBuilder java = new StringBuilder();
            TestDefinition.Statement previous = null;
            for (final TestDefinition.Statement statement : statements) {
                if (statement instanceof TestDefinition.Comment comment && comment.trailing()) {
                    // behind the previous statement, on its line (a trailing comment always follows one)
                    java.setLength(java.length() - 1);
                    java.append(" // ").append(comment.text()).append('\n');
                } else {
                    if (statement instanceof TestDefinition.Comment && previous != null
                            && !(previous instanceof TestDefinition.Comment)) {
                        java.append('\n');
                    }
                    java.append(statement(statement, depth));
                }
                previous = statement;
            }
            return java.toString();
        }

        private String statement(final TestDefinition.Statement statement, final int depth) {
            final String indent = INDENT.repeat(depth);
            return switch (statement) {
                case TestDefinition.Comment comment -> (indent + "// " + comment.text()).stripTrailing() + "\n";
                case TestDefinition.Let let -> indent + "final " + declaration(let.type(), let.nullable()) + " "
                        + JavaKeywords.identifier(let.name()) + " = " + converted(let.value(), let.type()) + ";\n";
                case TestDefinition.Evaluate evaluate when evaluate.expectedError() != null -> {
                    usesAssertThrows = true;
                    final JavaExceptions.JavaException exception = context.exception(evaluate.expectedError());
                    yield indent + "assertThrows(" + imports.use(exception.packageName(), exception.simpleName())
                            + ".class, () -> " + expression(evaluate.expression()) + ");\n";
                }
                case TestDefinition.Evaluate evaluate -> indent + expression(evaluate.expression()) + ";\n";
                case TestDefinition.If conditional -> indent + conditional(conditional, depth) + "\n";
                case TestDefinition.For loop -> indent + "for (final " + declaration(loop.element(), false) + " "
                        + JavaKeywords.identifier(loop.variable()) + " : " + expression(loop.iterable()) + ") {\n"
                        + statements(loop.body(), depth + 1) + indent + "}\n";
            };
        }

        /** {@code if (...) { ... } else if (...) { ... } else { ... }}, without the indentation of the first line. */
        private String conditional(final TestDefinition.If conditional, final int depth) {
            final String indent = INDENT.repeat(depth);
            final StringBuilder java = new StringBuilder("if (").append(expression(conditional.condition()))
                    .append(") {\n").append(statements(conditional.then(), depth + 1)).append(indent).append('}');
            if (conditional.otherwise().size() == 1
                    && conditional.otherwise().getFirst() instanceof TestDefinition.If elseIf) {
                java.append(" else ").append(conditional(elseIf, depth));
            } else if (!conditional.otherwise().isEmpty()) {
                java.append(" else {\n").append(statements(conditional.otherwise(), depth + 1)).append(indent)
                        .append('}');
            }
            return java.toString();
        }

        private String declaration(final Type type, final boolean nullable) {
            try {
                return JavaTypes.declaration(type, nullable, imports);
            } catch (final JavaTypes.UnsupportedTypeException e) {
                throw new Unsupported("the type " + type.text() + " has no Java form yet");
            }
        }

        // ------------------------------------------------------------------------------------------ expressions

        /** An expression; the API parts are rendered by {@link JavaSamples}, the parts of tests here. */
        private String expression(final InstanceExpression expression) {
            return switch (expression) {
                case InstanceExpression.Variable variable -> JavaKeywords.identifier(variable.name());
                case InstanceExpression.Default value -> defaultValue(value.type());
                case InstanceExpression.Negate negate -> negate(negate);
                case InstanceExpression.Binary binary -> binary(binary);
                case InstanceExpression.IsNull isNull -> operand(isNull.operand()) + (isNull.negated() ? " != null"
                        : " == null");
                case InstanceExpression.IsType isType -> (isType.negated() ? "!(" : "") + operand(isType.operand())
                        + " instanceof " + JavaTypes.type(isType.checked(), true, imports)
                        + (isType.negated() ? ")" : "");
                case InstanceExpression.Index index -> "get(" + expression(index.target()) + ", "
                        + expression(index.index()) + ")";
                case InstanceExpression.MapValue map -> map(map);
                case InstanceExpression.Call call when call.namespace().equals(Builtins.NAMESPACE) -> builtin(call);
                default -> samples.expression(expression).orElseThrow(() -> new Unsupported(
                        "no Java form for " + describe(expression)));
            };
        }

        private static String describe(final InstanceExpression expression) {
            return switch (expression) {
                case InstanceExpression.Call call -> "the call of " + call.method().name()
                        + " (not generated, or not visible in this module)";
                case InstanceExpression.MethodCall call -> "the call of " + call.method().name();
                case InstanceExpression.Default value -> "a value of " + value.type().text();
                case InstanceExpression.Construct construct -> "a new " + construct.type().text();
                default -> "a value of " + expression.type().text();
            };
        }

        /**
         * The default instance of a type: a call of a helper method {@code defaultPrivateKey()}, like the value
         * helpers of the other generated tests, so that a test reads as the spec does.
         */
        private String defaultValue(final Type type) {
            if (!(type instanceof Type.DeclaredType declared)) {
                return samples.value(type, List.of(), 0).orElseThrow(() -> new Unsupported("no value of "
                        + type.text()));
            }
            final String name = "default" + declared.name().name() + declared.arguments().stream()
                    .map(a -> a instanceof Type.DeclaredType d ? d.name().name() : "")
                    .collect(Collectors.joining());
            if (!helpers.containsKey(name)) {
                helpers.put(name, ""); // reserved: the value may use other default instances
                final Optional<String> value = samples.value(type, List.of(), 0);
                if (value.isEmpty()) {
                    helpers.remove(name);
                    throw new Unsupported("no value of " + type.text());
                }
                helpers.put(name, INDENT + "private static " + declaration(type, false) + " " + name + "() {\n"
                        + INDENT + INDENT + "return " + value.get() + ";\n" + INDENT + "}\n");
            }
            return name + "()";
        }

        /** An operand of an operator: parenthesized if it is rendered with a Java operator itself. */
        private String operand(final InstanceExpression expression) {
            final String java = expression(expression);
            return infix(expression) ? "(" + java + ")" : java;
        }

        /** Whether the Java form of an expression is an operator expression (not a method call). */
        private boolean infix(final InstanceExpression expression) {
            return switch (expression) {
                case InstanceExpression.IsNull ignored -> true;
                case InstanceExpression.IsType ignored -> true;
                case InstanceExpression.Binary binary -> switch (binary.operator()) {
                    case AND, OR -> true;
                    // `a == b` for enums; `equal(a, b)` and `!equal(a, b)` need no parentheses
                    case EQUAL, NOT_EQUAL -> isEnum(binary.left().type()) && isEnum(binary.right().type());
                    // `a.compareTo(b) < 0` and `a < b`
                    case LESS, LESS_OR_EQUAL, GREATER, GREATER_OR_EQUAL -> true;
                    // `a + b` of doubles and strings; integers, decimals, bytes and lists use method calls
                    default -> category(binary.left().type()) == BuiltinType.Category.FLOAT
                            || category(binary.left().type()) == BuiltinType.Category.STRING;
                };
                default -> false;
            };
        }

        private String negate(final InstanceExpression.Negate negate) {
            return switch (category(negate.operand().type())) {
                case INTEGER -> integer(negate.operand()) + ".negate()";
                case DECIMAL -> operand(negate.operand()) + ".negate()";
                default -> "-" + operand(negate.operand());
            };
        }

        private String binary(final InstanceExpression.Binary binary) {
            final Operator operator = binary.operator();
            final BuiltinType.Category category = category(binary.left().type());
            return switch (operator) {
                case AND, OR -> operand(binary.left()) + " " + operator.symbol() + " " + operand(binary.right());
                case EQUAL, NOT_EQUAL -> isEnum(binary.left().type()) && isEnum(binary.right().type())
                        ? operand(binary.left()) + " " + operator.symbol() + " " + operand(binary.right())
                        : (operator == Operator.NOT_EQUAL ? "!" : "") + "equal("
                        + comparable(binary.left(), binary.right()) + ")";
                case LESS, LESS_OR_EQUAL, GREATER, GREATER_OR_EQUAL -> switch (category) {
                    case INTEGER -> integer(binary.left()) + ".compareTo(" + integer(binary.right()) + ") "
                            + operator.symbol() + " 0";
                    case FLOAT -> operand(binary.left()) + " " + operator.symbol() + " " + operand(binary.right());
                    default -> operand(binary.left()) + ".compareTo(" + expression(binary.right()) + ") "
                            + operator.symbol() + " 0";
                };
                default -> switch (category) {
                    case INTEGER -> integer(binary.left()) + "." + bigMethod(operator) + "(" + integer(binary.right())
                            + ")";
                    case DECIMAL -> operand(binary.left()) + "." + bigMethod(operator) + "("
                            + expression(binary.right()) + ")";
                    case BYTES, COLLECTION -> "concat(" + expression(binary.left()) + ", "
                            + expression(binary.right()) + ")";
                    default -> operand(binary.left()) + " " + operator.symbol() + " " + operand(binary.right());
                };
            };
        }

        private static String bigMethod(final Operator operator) {
            return switch (operator) {
                case MULTIPLY -> "multiply";
                case DIVIDE -> "divide";
                case REMAINDER -> "remainder";
                case ADD -> "add";
                default -> "subtract";
            };
        }

        /**
         * The two operands of an equality check: integers of different Java representations are both converted to
         * exact integers, so that {@code 3} and a {@code uint64} compare by value.
         */
        private String comparable(final InstanceExpression left, final InstanceExpression right) {
            if (category(left.type()) == BuiltinType.Category.INTEGER
                    && category(right.type()) == BuiltinType.Category.INTEGER
                    && !left.type().text().equals(right.type().text())) {
                return integer(left) + ", " + integer(right);
            }
            return expression(left) + ", " + expression(right);
        }

        /** An integer as exact {@code BigInteger}. */
        private String integer(final InstanceExpression expression) {
            final String java = expression(expression);
            if (expression.type() instanceof Type.BasicType basic
                    && JavaTypes.type(basic, false, imports).endsWith("BigInteger")) {
                return java; // already exact: a BigInteger, or a method chain on one
            }
            return (isUnsigned(expression.type()) ? "unsigned" : "integer") + "(" + java + ")";
        }

        /** Enum constants are compared with {@code ==} in Java. */
        private boolean isEnum(final Type type) {
            return type instanceof Type.DeclaredType declared && context.model().type(declared.name())
                    .filter(org.hiero.sdk.v3.metalang.model.TypeDefinition.EnumDefinition.class::isInstance)
                    .isPresent();
        }

        private static boolean isUnsigned(final Type type) {
            return type instanceof Type.BasicType basic && JavaIntegers.isUnsignedLong(basic.builtin());
        }

        /**
         * A value passed to a variable, parameter or attribute of the given type: an exact integer of the test
         * language is checked against the range of the type and converted to its Java type.
         */
        private String converted(final InstanceExpression value, final Type target) {
            final String java = expression(value);
            if (!(value.type() instanceof Type.BasicType from) || from.builtin().category()
                    != BuiltinType.Category.INTEGER || !(target instanceof Type.BasicType to)
                    || to.builtin().category() != BuiltinType.Category.INTEGER
                    || !JavaTypes.type(from, false, imports).endsWith("BigInteger")
                    || to.builtin().name().equals(from.builtin().name())) {
                return java;
            }
            final String checked = "checked(" + java + ", " + JavaLiterals.quote(to.builtin().name())
                    + ")";
            return switch (JavaTypes.type(to, false, imports)) {
                case "byte" -> checked + ".byteValueExact()";
                case "short" -> checked + ".shortValueExact()";
                case "int" -> checked + ".intValueExact()";
                case "long" -> checked + (JavaIntegers.isUnsignedLong(to.builtin()) ? ".longValue()"
                        : ".longValueExact()");
                default -> checked;
            };
        }

        private String map(final InstanceExpression.MapValue map) {
            final String type = imports.use("java.util", "Map");
            if (map.entries().size() <= 10) {
                return type + ".of(" + map.entries().stream().map(e -> expression(e.getKey()) + ", "
                        + expression(e.getValue())).collect(Collectors.joining(", ")) + ")";
            }
            return type + ".ofEntries(" + map.entries().stream().map(e -> type + ".entry(" + expression(e.getKey())
                    + ", " + expression(e.getValue()) + ")").collect(Collectors.joining(", ")) + ")";
        }

        /** A built-in function or assertion: a static method of the test support module, arguments in order. */
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
                final InstanceExpression actual = call.arguments().get("actual");
                arguments.add(comparable(first, actual));
            }
            for (final ParameterDefinition parameter : method.parameters()) {
                final InstanceExpression value = call.arguments().get(parameter.name());
                if (value == null || compares && !parameter.name().equals("message")) {
                    continue; // an omitted optional parameter (message) is an overload without it
                }
                arguments.add(argument(value, parameter.type()));
            }
            return RENAMED.getOrDefault(name, name) + "(" + String.join(", ", arguments) + ")";
        }

        /** An argument of a built-in function: a type literal is a class literal, integers are exact. */
        private String argument(final InstanceExpression value, final Type parameter) {
            if (value.type() instanceof Type.BasicType basic && basic.builtin().category() == BuiltinType.Category.TYPE) {
                // the resolver gives a type literal its type as argument: type<Circle>
                return JavaTypes.type(basic.arguments().getFirst(), true, imports) + ".class";
            }
            if (parameter instanceof Type.BasicType target && target.builtin().category() == BuiltinType.Category.INTEGER
                    && JavaTypes.type(target, false, imports).endsWith("BigInteger")) {
                return integer(value);
            }
            return converted(value, parameter);
        }

        private static BuiltinType.Category category(final Type type) {
            return type instanceof Type.BasicType basic ? basic.builtin().category() : BuiltinType.Category.TYPE;
        }

        // ------------------------------------------------------------------------------------------- rendering

        private String render(final String specName) {
            deprecated |= samples.usesDeprecated();
            final StringBuilder java = new StringBuilder(JavaGenerator.HEADER).append('\n');
            java.append("package ").append(JavaNames.packageName(namespace)).append(";\n\n");
            java.append("import static ").append(TESTING).append(".*;\n");
            if (usesAssertThrows) {
                java.append("import static ").append(TestGenerator.JUNIT).append(".Assertions.assertThrows;\n");
            }
            java.append('\n');
            final String displayName = imports.use(TestGenerator.JUNIT, "DisplayName");
            final String generated = JavaGenerator.generated(imports);
            // never empty: the JUnit annotations are imported
            java.append(imports.render()).append('\n');
            java.append("/// Tests of the spec `").append(specName).append("`, generated from the `tests` blocks of its")
                    .append(" `## Testing` section.\n");
            if (!notes.isEmpty()) {
                java.append("///\n/// Not generated:\n");
                notes.forEach(n -> java.append("/// - ").append(n).append('\n'));
            }
            java.append(generated);
            if (deprecated) {
                java.append("@SuppressWarnings(\"deprecation\")\n");
            }
            java.append('@').append(displayName).append('(').append(JavaLiterals.quote(specName)).append(")\n");
            java.append("class ").append(className).append(" {\n");
            for (final String test : tests) {
                java.append('\n').append(test);
            }
            for (final String helper : helpers.values()) {
                java.append('\n').append(helper);
            }
            java.append("}\n");
            return java.toString().replace("\n\n\n", "\n\n");
        }
    }
}
