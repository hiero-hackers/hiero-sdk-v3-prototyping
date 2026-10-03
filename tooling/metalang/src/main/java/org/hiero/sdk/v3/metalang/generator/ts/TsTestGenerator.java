package org.hiero.sdk.v3.metalang.generator.ts;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.hiero.sdk.v3.metalang.ast.Annotation;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.generator.Constraints;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.generator.IntegerRange;
import org.hiero.sdk.v3.metalang.generator.RegexSamples;
import org.hiero.sdk.v3.metalang.generator.SpecFolders;
import org.hiero.sdk.v3.metalang.model.EnumValueDefinition;
import org.hiero.sdk.v3.metalang.model.FieldDefinition;
import org.hiero.sdk.v3.metalang.model.FunctionDefinition;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.hiero.sdk.v3.metalang.model.MethodDefinition;
import org.hiero.sdk.v3.metalang.model.ParameterDefinition;
import org.hiero.sdk.v3.metalang.model.QualifiedName;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.model.TypeDefinition;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;

/**
 * Generates the tests of the TypeScript API with the Node.js test runner ({@code node:test}, {@code node:assert}):
 * one {@code <Type>.test.ts} per class and enum (and per interface with static functions), one
 * {@code functions.test.ts} per namespace with functions. The tests check the contract of the specs like the Java
 * tests: construction and attribute values, {@code null} ({@code TypeError}), the boundaries of the integer ranges
 * (and that a {@code number} is an integer) and of the validation annotations ({@code RangeError}), copies of
 * {@code bytes}, collections and dates, setters, that every method and function can be called, and the constants of
 * enums. Every test is named by what it checks. Methods are stubs until they are implemented, so their tests fail
 * until then. A test whose values cannot be built is not generated and listed at the top of the test file.
 */
public final class TsTestGenerator {

    private static final String INDENT = "    ";

    /** The prefix of a line that lists a test that is not generated. */
    private static final String NOTE = "// - ";

    private TsTestGenerator() {
    }

    /**
     * Returns the tests that could not be generated, as listed in the generated test files.
     *
     * @param files the generated files
     * @return {@code test file: reason}, sorted
     */
    public static List<String> untested(final List<GeneratedFile> files) {
        final List<String> result = new ArrayList<>();
        for (final GeneratedFile file : files) {
            if (file.path().endsWith(".test.ts")) {
                final String name = file.path().substring(file.path().lastIndexOf('/') + 1);
                file.content().lines().filter(l -> l.startsWith(NOTE))
                        .forEach(l -> result.add(name + ": " + l.substring(NOTE.length())));
            }
        }
        return result.stream().sorted().toList();
    }

    static Optional<GeneratedFile> generate(final TypeDefinition type, final TsContext context) {
        final TestFile file = new TestFile(type.name().namespace(), context);
        final boolean tested = switch (type) {
            case TypeDefinition.EnumDefinition enumType -> file.enumTests(enumType);
            case TypeDefinition.ComplexTypeDefinition complex when complex.abstraction() ->
                    file.staticTests(complex);
            case TypeDefinition.ComplexTypeDefinition complex -> file.classTests(complex);
        };
        return tested ? Optional.of(file.render(type.name().name(), type.name().name() + ".test.ts"))
                : Optional.empty();
    }

    static Optional<GeneratedFile> functions(final String namespace, final TsContext context) {
        final List<FunctionDefinition> functions = context.functions(namespace);
        if (functions.isEmpty()) {
            return Optional.empty();
        }
        final TestFile file = new TestFile(namespace, context);
        file.functionTests(functions);
        return Optional.of(file.render(namespace + " functions", "functions.test.ts"));
    }

    /** A value the tests pass: an attribute or a parameter. */
    private record Input(String name, Type type, List<Annotation> annotations, boolean varargs) {

        boolean nullable() {
            return annotations.stream().anyMatch(a -> a.name().equals("nullable"));
        }
    }

    /** A boundary value of the validation annotations. */
    private record Case(String text, String expression, boolean accepted) {
    }

    /** The content of one test file. */
    private static final class TestFile {

        private final String namespace;
        private final TsContext context;
        private final String directory;
        private final TsImports imports;
        private final TsSamples samples;
        private final List<String> tests = new ArrayList<>();
        private final Map<String, String> helpers = new LinkedHashMap<>();
        private final Map<String, String> helperNames = new HashMap<>();
        private final List<String> notes = new ArrayList<>();
        private boolean usesAssertValue;

        private TestFile(final String namespace, final TsContext context) {
            this.namespace = namespace;
            this.context = context;
            final String folder = context.folder(namespace);
            this.directory = TsNames.sourceDirectory(folder, namespace);
            this.imports = new TsImports(context, folder, directory, "create", "init", "assertValue", "describe",
                    "test", "assert");
            final Set<String> visible = new HashSet<>(SpecFolders.required(folder, context.folders()));
            visible.add(folder);
            this.samples = new TsSamples(context, imports, visible::contains);
        }

        // ------------------------------------------------------------------------------------------- classes

        private boolean classTests(final TypeDefinition.ComplexTypeDefinition type) {
            final Optional<Map<Type.TypeVariable, Type>> variables = Constraints.defaults(type.typeParameters());
            if (variables.isEmpty()) {
                notes.add("no type arguments for " + type.name().name() + " (a type parameter refers to itself)");
                return true;
            }
            final Type.DeclaredType self = new Type.DeclaredType(type.name(), type.typeParameters().stream()
                    .map(p -> variables.get().get(p.variable())).toList());
            final String typeName = TsTypes.type(self, imports);
            final String className = imports.value(type.name());
            final List<FieldDefinition> fields = type.fields();
            final List<Input> inputs = fields.stream().map(f -> input(f, variables.get())).toList();

            // the attribute values of create()
            final List<String> values = new ArrayList<>();
            for (final Input input : inputs) {
                final Optional<String> helper = helper(input, 0, true);
                if (helper.isEmpty() && !input.nullable() && !hasDefault(fields, input)) {
                    notes.add("no valid value for `" + input.name() + "` of " + type.name().name()
                            + ": constructor, attribute and method tests");
                    staticMethods(type);
                    return true;
                }
                helper.ifPresent(h -> values.add(INDENT + INDENT + input.name() + ": " + h + "(),\n"));
            }
            helpers.put("init", "function init() {\n" + INDENT + "return {\n" + String.join("", values) + INDENT
                    + "};\n}\n");
            helpers.put("create", "function create(): " + typeName + " {\n" + INDENT + "return new " + className
                    + "(init());\n}\n");

            // construction
            final StringBuilder create = new StringBuilder(INDENT + INDENT + "const values = init();\n"
                    + INDENT + INDENT + "const subject = new " + className + "(values);\n"
                    + INDENT + INDENT + "assert.ok(subject instanceof " + className + ");\n");
            for (final Input input : inputs) {
                if (helper(input, 0, true).isPresent()) {
                    create.append(assertValue("values." + input.name(), "subject." + input.name()));
                }
            }
            test("creates " + article(type.name().name()) + " and returns the values", false, create.toString());
            defaultsTest(type, inputs, className);
            for (final Input input : inputs) {
                final java.util.function.Function<String, String> construct = value -> "new " + className
                        + "({ ...init(), " + input.name() + ": " + value + " })";
                if (!input.nullable()) {
                    test("rejects null for " + input.name(), false, INDENT + INDENT + "assert.throws(() => "
                            + construct.apply("null as never") + ", TypeError);\n");
                }
                for (final Case c : cases(input)) {
                    if (c.accepted()) {
                        test(input.name() + ": accepts " + c.text(), false, INDENT + INDENT + "const value = "
                                + c.expression() + ";\n" + INDENT + INDENT + "const subject = "
                                + construct.apply("value") + ";\n" + assertValue("value", "subject." + input.name()));
                    } else {
                        test(input.name() + ": rejects " + c.text(), false, INDENT + INDENT + "assert.throws(() => "
                                + construct.apply(c.expression()) + ", RangeError);\n");
                    }
                }
                copyTest(input, "the constructor", construct, null);
            }

            // setters
            for (int i = 0; i < fields.size(); i++) {
                if (!fields.get(i).hasAnnotation("immutable")) {
                    setterTests(inputs.get(i));
                }
            }

            // methods
            for (final MethodDefinition method : type.methods()) {
                if (!method.isStatic() || type.name().equals(method.declaringType())) {
                    methodTest(method, (method.isStatic() ? className : "create()") + "." + method.name(),
                            variables.get());
                }
            }
            return true;
        }

        private static boolean hasDefault(final List<FieldDefinition> fields, final Input input) {
            return fields.stream().anyMatch(f -> f.name().equals(input.name()) && f.hasAnnotation("default"));
        }

        private void defaultsTest(final TypeDefinition.ComplexTypeDefinition type, final List<Input> inputs,
                                  final String className) {
            final List<FieldDefinition> optional = type.fields().stream()
                    .filter(f -> f.hasAnnotation("nullable") || f.hasAnnotation("default")).toList();
            if (optional.isEmpty()) {
                return;
            }
            final List<String> required = new ArrayList<>();
            for (final Input input : inputs) {
                if (optional.stream().noneMatch(f -> f.name().equals(input.name()))) {
                    final Optional<String> helper = helper(input, 0, true);
                    if (helper.isEmpty()) {
                        return;
                    }
                    required.add(input.name() + ": " + helper.get() + "()");
                }
            }
            final StringBuilder body = new StringBuilder(INDENT + INDENT + "const subject = new " + className + "({ "
                    + String.join(", ", required) + " });\n");
            for (final FieldDefinition field : optional) {
                if (field.hasAnnotation("default")) {
                    body.append(assertValue(TsLiterals.expression(field.annotation("default").orElseThrow()
                            .arguments().getFirst(), field.type(), imports), "subject." + field.name()));
                } else {
                    body.append(INDENT).append(INDENT).append("assert.strictEqual(subject.").append(field.name())
                            .append(", null);\n");
                }
            }
            test("creates " + article(type.name().name()) + " with the default values", false, body.toString());
        }

        private void setterTests(final Input input) {
            final String property = "subject." + input.name();
            final Optional<String> other = helper(input, 1, true);
            if (other.isPresent()) {
                test(input.name() + " can be changed", false, INDENT + INDENT + "const subject = create();\n"
                        + INDENT + INDENT + "const value = " + other.get() + "();\n"
                        + INDENT + INDENT + property + " = value;\n" + assertValue("value", property));
            }
            if (input.nullable()) {
                test(input.name() + " can be set to null", false, INDENT + INDENT + "const subject = create();\n"
                        + INDENT + INDENT + property + " = null;\n"
                        + INDENT + INDENT + "assert.strictEqual(" + property + ", null);\n");
            } else {
                test("setting " + input.name() + " rejects null and keeps the value", false, INDENT + INDENT
                        + "const subject = create();\n" + INDENT + INDENT + "const before = " + property + ";\n"
                        + INDENT + INDENT + "assert.throws(() => {\n" + INDENT + INDENT + INDENT + property
                        + " = null as never;\n" + INDENT + INDENT + "}, TypeError);\n" + assertValue("before", property));
            }
            for (final Case c : cases(input)) {
                if (c.accepted()) {
                    test("setting " + input.name() + " accepts " + c.text(), false, INDENT + INDENT
                            + "const subject = create();\n" + INDENT + INDENT + "const value = " + c.expression()
                            + ";\n" + INDENT + INDENT + property + " = value;\n" + assertValue("value", property));
                } else {
                    test("setting " + input.name() + " rejects " + c.text() + " and keeps the value", false,
                            INDENT + INDENT + "const subject = create();\n" + INDENT + INDENT + "const before = "
                                    + property + ";\n" + INDENT + INDENT + "assert.throws(() => {\n" + INDENT + INDENT
                                    + INDENT + property + " = " + c.expression() + ";\n" + INDENT + INDENT
                                    + "}, RangeError);\n" + assertValue("before", property));
                }
            }
            copyTest(input, "setting " + input.name(), null, property);
        }

        /**
         * The copy test of a {@code bytes}, collection or date value: changing the value passed in does not change
         * the object, and neither does changing the returned value (arrays are frozen).
         *
         * @param input     the attribute
         * @param who       who copies, for the test name
         * @param construct creates the object with a value (constructor test), {@code null} for the setter test
         * @param property  the property to set (setter test)
         */
        private void copyTest(final Input input, final String who,
                              final java.util.function.Function<String, String> construct, final String property) {
            if (!(input.type() instanceof Type.BasicType basic)) {
                return;
            }
            final Optional<String> helper = helper(input, 0, true);
            if (helper.isEmpty()) {
                return;
            }
            final String value = helper.get() + "()";
            final String accessor = "subject." + input.name();
            final String create = construct == null
                    ? INDENT + INDENT + "const subject = create();\n" + INDENT + INDENT + property + " = value;\n"
                    : INDENT + INDENT + "const subject = " + construct.apply("value") + ";\n";
            final StringBuilder body = new StringBuilder();
            final String what;
            switch (basic.builtin().category()) {
                case BYTES -> {
                    body.append(INDENT).append(INDENT).append("const value = ").append(value).append(";\n")
                            .append(INDENT).append(INDENT).append("const expected = value.slice();\n").append(create)
                            .append(INDENT).append(INDENT).append("value[0] = value[0]! + 1;\n")
                            .append(assertValue("expected", accessor))
                            .append(INDENT).append(INDENT).append("const returned = ").append(accessor).append("!;\n")
                            .append(INDENT).append(INDENT).append("returned[0] = returned[0]! + 1;\n")
                            .append(assertValue("expected", accessor));
                    what = "; the property returns a copy";
                }
                case COLLECTION -> {
                    final boolean set = basic.builtin().name().equals("set");
                    body.append(INDENT).append(INDENT).append("const value = ").append(set ? "new Set(" + value + ")"
                                    : "[..." + value + "]").append(";\n")
                            .append(INDENT).append(INDENT).append("const expected = ")
                            .append(set ? "new Set(value)" : "[...value]").append(";\n").append(create)
                            .append(INDENT).append(INDENT).append(set ? "value.clear();\n" : "value.splice(0);\n")
                            .append(assertValue("expected", accessor));
                    if (set) {
                        body.append(INDENT).append(INDENT).append("(").append(accessor)
                                .append(" as unknown as Set<unknown>).clear();\n")
                                .append(assertValue("expected", accessor));
                    } else {
                        body.append(INDENT).append(INDENT).append("assert.throws(() => (").append(accessor)
                                .append(" as unknown as unknown[]).push(expected[0]), TypeError);\n");
                    }
                    what = set ? "; the property returns a copy" : "; the property is frozen";
                }
                case MAP -> {
                    body.append(INDENT).append(INDENT).append("const value = new Map(").append(value).append(");\n")
                            .append(INDENT).append(INDENT).append("const expected = new Map(value);\n").append(create)
                            .append(INDENT).append(INDENT).append("value.clear();\n")
                            .append(assertValue("expected", accessor))
                            .append(INDENT).append(INDENT).append("(").append(accessor)
                            .append(" as unknown as Map<unknown, unknown>).clear();\n")
                            .append(assertValue("expected", accessor));
                    what = "; the property returns a copy";
                }
                case TEMPORAL -> {
                    body.append(INDENT).append(INDENT).append("const value = ").append(value).append(";\n")
                            .append(INDENT).append(INDENT).append("const expected = new Date(value.getTime());\n")
                            .append(create)
                            .append(INDENT).append(INDENT).append("value.setTime(0);\n")
                            .append(assertValue("expected", accessor))
                            .append(INDENT).append(INDENT).append(accessor).append("!.setTime(0);\n")
                            .append(assertValue("expected", accessor));
                    what = "; the property returns a copy";
                }
                default -> {
                    return;
                }
            }
            test(who + " copies " + input.name() + what, false, body.toString());
        }

        // --------------------------------------------------------------------------------------------- enums

        private boolean enumTests(final TypeDefinition.EnumDefinition type) {
            final String name = imports.value(type.name());
            if (!type.attributes().isEmpty()) {
                final StringBuilder body = new StringBuilder();
                for (final EnumValueDefinition value : type.values()) {
                    for (int i = 0; i < type.attributes().size() && i < value.arguments().size(); i++) {
                        final ParameterDefinition attribute = type.attributes().get(i);
                        body.append(assertValue(TsLiterals.expression(value.arguments().get(i), attribute.type(),
                                imports), name + "." + value.name() + "." + attribute.name()));
                    }
                }
                test("the constants have the attribute values of the specs", false, body.toString());
            }
            test("valueOf returns the constant of every name", false, INDENT + INDENT + "for (const value of "
                    + name + ".values()) {\n" + INDENT + INDENT + INDENT + "assert.strictEqual(" + name
                    + ".valueOf(value.name), value);\n" + INDENT + INDENT + "}\n" + INDENT + INDENT
                    + "assert.strictEqual(" + name + ".values().length, " + type.values().size() + ");\n");
            final Optional<EnumValueDefinition> first = type.values().stream()
                    .filter(v -> !v.hasAnnotation("deprecated")).findFirst()
                    .or(() -> type.values().stream().findFirst());
            for (final MethodDefinition method : type.methods()) {
                if (method.isStatic()) {
                    methodTest(method, name + "." + method.name(), Map.of());
                } else {
                    first.ifPresent(f -> methodTest(method, name + "." + f.name() + "." + method.name(), Map.of()));
                }
            }
            return true;
        }

        // ------------------------------------------------------------------------------------------- methods

        private boolean staticTests(final TypeDefinition.ComplexTypeDefinition type) {
            staticMethods(type);
            return !tests.isEmpty();
        }

        private void staticMethods(final TypeDefinition.ComplexTypeDefinition type) {
            for (final MethodDefinition method : type.declaredMethods()) {
                if (method.isStatic()) {
                    methodTest(method, imports.value(type.name()) + "." + method.name(), Map.of());
                }
            }
        }

        /**
         * The test that a method or function can be called with valid arguments and returns a value.
         *
         * @param method    the method or function
         * @param callee    the expression that is called, e.g. {@code create().sign}
         * @param variables the type arguments of the type
         */
        private void methodTest(final MethodDefinition method, final String callee,
                                final Map<Type.TypeVariable, Type> variables) {
            final Optional<Map<Type.TypeVariable, Type>> own = Constraints.defaults(method.typeParameters());
            if (own.isEmpty()) {
                notes.add("no type arguments for `" + method.signature() + "`");
                return;
            }
            final Map<Type.TypeVariable, Type> all = new HashMap<>(variables);
            all.putAll(own.get());
            final Optional<List<String>> arguments = arguments(method, all);
            if (arguments.isEmpty()) {
                notes.add("no valid arguments for `" + method.signature() + "`");
                return;
            }
            final String call = callee + "(" + String.join(", ", arguments.get()) + ")";
            final String display = method.name() + "(" + method.parameters().stream().map(ParameterDefinition::name)
                    .collect(Collectors.joining(", ")) + ") can be called";
            final String inner = INDENT + INDENT + INDENT;
            final StringBuilder statements = new StringBuilder();
            if (method.hasAnnotation("async")) {
                statements.append(inner).append("const result = ").append(call).append(";\n")
                        .append(inner).append("assert.ok(result instanceof Promise);\n")
                        .append(inner).append("result.catch(() => undefined);\n");
            } else if (method.hasAnnotation("streaming")) {
                statements.append(inner).append("const result = ").append(call).append(";\n")
                        .append(inner).append("assert.ok(Symbol.asyncIterator in Object(result));\n");
            } else if (method.returnType() instanceof Type.VoidType || method.hasAnnotation("nullable")) {
                statements.append(inner).append(call).append(";\n");
            } else {
                statements.append(inner).append("const result = ").append(call).append(";\n")
                        .append(inner).append("assert.notStrictEqual(result, null);\n")
                        .append(inner).append("assert.notStrictEqual(result, undefined);\n");
            }
            final List<String> errors = method.hasAnnotation("async") || method.hasAnnotation("streaming") ? List.of()
                    : method.annotation("throws").stream().flatMap(t -> t.arguments().stream())
                    .map(Literal::text).distinct().map(context::error)
                    .map(e -> e.namespace() == null ? e.name() : imports.error(e, true)).distinct().toList();
            if (errors.isEmpty()) {
                test(display + (method.hasAnnotation("async") ? " and returns a promise"
                        : method.hasAnnotation("streaming") ? " and returns a stream" : ""), false,
                        statements.toString().replace(inner, INDENT + INDENT));
                return;
            }
            test(display + " (only " + String.join(", ", errors) + " may occur)", false, INDENT + INDENT + "try {\n"
                    + statements + INDENT + INDENT + "} catch (error) {\n"
                    + INDENT + INDENT + INDENT + "if (!(" + errors.stream().map(e -> "error instanceof " + e)
                    .collect(Collectors.joining(" || ")) + ")) {\n"
                    + INDENT + INDENT + INDENT + INDENT + "throw error;\n"
                    + INDENT + INDENT + INDENT + "}\n"
                    + INDENT + INDENT + "}\n");
        }

        private Optional<List<String>> arguments(final MethodDefinition method,
                                                 final Map<Type.TypeVariable, Type> variables) {
            final List<String> arguments = new ArrayList<>();
            for (final ParameterDefinition parameter : method.parameters()) {
                final Input input = input(parameter, variables);
                final Optional<String> helper = helper(input, 0, false);
                if (helper.isEmpty() && (!input.nullable() || input.varargs())) {
                    return Optional.empty();
                }
                arguments.add(helper.map(h -> h + "()").orElse("null"));
            }
            return Optional.of(arguments);
        }

        // ----------------------------------------------------------------------------------------- functions

        private void functionTests(final List<FunctionDefinition> functions) {
            for (final FunctionDefinition function : functions) {
                final MethodDefinition method = function.method();
                final String target = imports.function(function.namespace(), method.name());
                methodTest(method, target, Map.of());
                final Optional<Map<Type.TypeVariable, Type>> variables = Constraints.defaults(method.typeParameters());
                final Optional<List<String>> arguments = variables.flatMap(v -> arguments(method, v));
                if (arguments.isEmpty()) {
                    continue;
                }
                for (int i = 0; i < method.parameters().size(); i++) {
                    final Input parameter = input(method.parameters().get(i), variables.get());
                    final int index = i;
                    final java.util.function.Function<String, String> call = value -> target + "("
                            + replace(arguments.get(), index, value) + ")";
                    if (!parameter.nullable() && !parameter.varargs()) {
                        test(method.name() + " rejects null for " + parameter.name(), false, INDENT + INDENT
                                + "assert.throws(() => " + call.apply("null as never") + ", TypeError);\n");
                    }
                    for (final Case c : parameter.varargs() ? List.<Case>of() : cases(parameter)) {
                        if (c.accepted()) {
                            test(method.name() + ": " + parameter.name() + " accepts " + c.text(), false, INDENT
                                    + INDENT + (returnsValue(method) ? "assert.notStrictEqual(" + call.apply(
                                    c.expression()) + ", null)" : call.apply(c.expression())) + ";\n");
                        } else {
                            test(method.name() + ": " + parameter.name() + " rejects " + c.text(), false, INDENT
                                    + INDENT + "assert.throws(() => " + call.apply(c.expression()) + ", RangeError);\n");
                        }
                    }
                }
            }
        }

        private static boolean returnsValue(final MethodDefinition method) {
            return method.hasAnnotation("async") || method.hasAnnotation("streaming")
                    || !(method.returnType() instanceof Type.VoidType) && !method.hasAnnotation("nullable");
        }

        // -------------------------------------------------------------------------------------------- values

        private Input input(final FieldDefinition field, final Map<Type.TypeVariable, Type> variables) {
            return new Input(field.name(), LinkedModel.substitute(field.type(), variables), field.annotations(),
                    false);
        }

        private Input input(final ParameterDefinition parameter, final Map<Type.TypeVariable, Type> variables) {
            return new Input(parameter.name(), LinkedModel.substitute(parameter.type(), variables),
                    parameter.annotations(), parameter.varargs());
        }

        /** The name of the helper function that returns a new valid value of the input. */
        private Optional<String> helper(final Input input, final int variant, final boolean stored) {
            final String key = input.name() + "|" + input.type().text() + "|" + input.annotations().stream()
                    .map(a -> a.name() + a.arguments().stream().map(Literal::text).toList())
                    .collect(Collectors.joining(",")) + "|" + variant + "|" + stored;
            if (helperNames.containsKey(key)) {
                return Optional.ofNullable(helperNames.get(key));
            }
            final Optional<String> value = samples.value(input.type(), input.annotations(), variant, stored);
            if (value.isEmpty()) {
                helperNames.put(key, null);
                return Optional.empty();
            }
            final String declaration = TsTypes.type(input.type(), imports);
            final String base = TsNames.local(input.name()) + (variant == 0 ? "" : "Other") + "Value";
            final java.util.function.Function<String, String> render = n -> "function " + n + "(): " + declaration
                    + " {\n" + INDENT + "return " + value.get() + ";\n}\n";
            String name = base;
            for (int i = 2; helpers.containsKey(name) && !helpers.get(name).equals(render.apply(name)); i++) {
                name = base + i;
            }
            helperNames.put(key, name);
            helpers.put(name, render.apply(name));
            return Optional.of(name);
        }

        /** The boundary values of the integer range and the validation annotations of an input. */
        private List<Case> cases(final Input input) {
            final List<Case> cases = new ArrayList<>();
            final List<Annotation> annotations = input.annotations();
            if (input.type() instanceof Type.BasicType basic) {
                final BuiltinType builtin = basic.builtin();
                switch (builtin.category()) {
                    case INTEGER -> integerCases(builtin, annotations, cases);
                    case FLOAT, DECIMAL, DURATION -> boundCases(builtin, annotations, cases);
                    case STRING -> stringCases(annotations, cases);
                    case BYTES, COLLECTION, MAP -> sizeCases(input, cases);
                    default -> {
                    }
                }
            }
            final Set<String> texts = new HashSet<>();
            final Set<String> accepted = new HashSet<>();
            return cases.stream().filter(c -> texts.add(c.text()))
                    .filter(c -> !c.accepted() || accepted.add(c.expression())).toList();
        }

        private void integerCases(final BuiltinType builtin, final List<Annotation> annotations,
                                  final List<Case> cases) {
            final IntegerRange range = Constraints.range(builtin, annotations);
            if (range.min().compareTo(range.max()) > 0) {
                return;
            }
            cases.add(new Case("the minimum (" + range.min() + ")", TsSamples.integerLiteral(builtin, range.min()),
                    true));
            cases.add(new Case("the maximum (" + range.max() + ")", TsSamples.integerLiteral(builtin, range.max()),
                    true));
            final BigInteger below = range.min().subtract(BigInteger.ONE);
            final BigInteger above = range.max().add(BigInteger.ONE);
            cases.add(new Case("a value below the minimum (" + below + ")", TsSamples.integerLiteral(builtin, below),
                    false));
            cases.add(new Case("a value above the maximum (" + above + ")", TsSamples.integerLiteral(builtin, above),
                    false));
            if (!TsTypes.isBigInt(builtin) && range.max().compareTo(range.min()) > 0) {
                final String fraction = new BigDecimal(range.min()).add(new BigDecimal("0.5")).toPlainString();
                cases.add(new Case("a value that is no integer (" + fraction + ")", fraction, false));
            }
        }

        private void boundCases(final BuiltinType builtin, final List<Annotation> annotations,
                                final List<Case> cases) {
            for (final Annotation annotation : annotations) {
                final boolean min = annotation.name().equals("min");
                if (!min && !annotation.name().equals("max")) {
                    continue;
                }
                final BigDecimal bound = Constraints.bound(annotations, annotation.name()).orElseThrow();
                final String accepted;
                final String rejected;
                switch (builtin.category()) {
                    case FLOAT -> {
                        accepted = bound.toPlainString();
                        final double next = min ? Math.nextDown(bound.doubleValue()) : Math.nextUp(bound.doubleValue());
                        rejected = Double.toString(next);
                    }
                    case DECIMAL -> {
                        accepted = TsLiterals.quote(bound.toPlainString());
                        final BigDecimal step = new BigDecimal("0.001");
                        rejected = TsLiterals.quote((min ? bound.subtract(step) : bound.add(step)).toPlainString());
                    }
                    default -> {
                        final long millis = builtin.name().equals("seconds")
                                ? bound.multiply(BigDecimal.valueOf(1000)).longValueExact() : bound.longValueExact();
                        final String duration = imports.support("Duration", true);
                        accepted = duration + ".ofMillis(" + millis + ")";
                        rejected = duration + ".ofMillis(" + (min ? millis - 1 : millis + 1) + ")";
                    }
                }
                cases.add(new Case(min ? "the minimum (" + bound.toPlainString() + ")"
                        : "the maximum (" + bound.toPlainString() + ")", accepted, true));
                cases.add(new Case(min ? "a value below the minimum" : "a value above the maximum", rejected, false));
            }
        }

        private void stringCases(final List<Annotation> annotations, final List<Case> cases) {
            final Optional<Integer> minLength = Constraints.size(annotations, "minLength");
            final Optional<Integer> maxLength = Constraints.size(annotations, "maxLength");
            if (minLength.isPresent()) {
                final int length = minLength.get();
                Constraints.string(sized(annotations, "minLength", "maxLength", length), 0)
                        .ifPresent(s -> cases.add(new Case("a value with the minimum length", TsLiterals.quote(s),
                                true)));
                if (length > 0) {
                    cases.add(new Case("a value shorter than the minimum length",
                            TsLiterals.quote("a".repeat(length - 1)), false));
                }
            }
            if (maxLength.isPresent()) {
                final int length = maxLength.get();
                Constraints.string(sized(annotations, "minLength", "maxLength", length), 0)
                        .ifPresent(s -> cases.add(new Case("a value with the maximum length", TsLiterals.quote(s),
                                true)));
                cases.add(new Case("a value longer than the maximum length", TsLiterals.quote("a".repeat(length + 1)),
                        false));
            }
            Constraints.stringArgument(annotations, "pattern").flatMap(RegexSamples::rejected)
                    .filter(s -> !Constraints.isValidString(s, annotations))
                    .ifPresent(s -> cases.add(new Case("a value that does not match the pattern", TsLiterals.quote(s),
                            false)));
            if (annotations.stream().anyMatch(a -> a.name().equals("urlPattern"))) {
                cases.add(new Case("a value that is no URL", TsLiterals.quote("not a url"), false));
            }
        }

        private void sizeCases(final Input input, final List<Case> cases) {
            final List<Annotation> annotations = input.annotations();
            final Optional<Integer> minSize = Constraints.size(annotations, "minSize");
            final Optional<Integer> maxSize = Constraints.size(annotations, "maxSize");
            if (minSize.isPresent()) {
                sized(input, minSize.get()).ifPresent(v -> cases.add(new Case("the minimum number of elements", v,
                        true)));
                if (minSize.get() > 0) {
                    sized(input, minSize.get() - 1).ifPresent(v -> cases.add(new Case("fewer elements than the "
                            + "minimum", v, false)));
                }
            }
            if (maxSize.isPresent()) {
                sized(input, maxSize.get()).ifPresent(v -> cases.add(new Case("the maximum number of elements", v,
                        true)));
                sized(input, maxSize.get() + 1).ifPresent(v -> cases.add(new Case("more elements than the maximum",
                        v, false)));
            }
        }

        private Optional<String> sized(final Input input, final int size) {
            return samples.value(input.type(), sized(input.annotations(), "minSize", "maxSize", size), 0, true);
        }

        private static List<Annotation> sized(final List<Annotation> annotations, final String min, final String max,
                                              final int size) {
            final List<Annotation> result = new ArrayList<>(annotations.stream()
                    .filter(a -> !a.name().equals(min) && !a.name().equals(max)).toList());
            final Annotation template = annotations.stream().filter(a -> a.name().equals(min) || a.name().equals(max))
                    .findFirst().orElseThrow();
            final Literal value = new Literal.NumberLiteral(String.valueOf(size), template.location());
            result.add(new Annotation(min, List.of(value), true, template.location()));
            result.add(new Annotation(max, List.of(value), true, template.location()));
            return result;
        }

        // ----------------------------------------------------------------------------------------- rendering

        private String assertValue(final String expected, final String actual) {
            usesAssertValue = true;
            return INDENT + INDENT + "assertValue(" + expected + ", " + actual + ");\n";
        }

        private void test(final String name, final boolean async, final String body) {
            tests.add(INDENT + "test(" + TsLiterals.quote(name) + ", " + (async ? "async " : "") + "() => {\n" + body
                    + INDENT + "});\n");
        }

        private static String replace(final List<String> arguments, final int index, final String value) {
            final List<String> result = new ArrayList<>(arguments);
            result.set(index, value);
            return String.join(", ", result);
        }

        private static String article(final String name) {
            return ("AEIOU".indexOf(name.charAt(0)) >= 0 ? "an " : "a ") + name;
        }

        private GeneratedFile render(final String suite, final String fileName) {
            // the tests that cannot be generated are listed as todo tests as well
            final List<String> all = new ArrayList<>(tests);
            notes.stream().distinct().forEach(n -> all.add(INDENT + "test.todo(" + TsLiterals.quote("not generated: "
                    + n) + ");\n"));
            final StringBuilder code = new StringBuilder();
            code.append("describe(").append(TsLiterals.quote(suite)).append(", () => {\n");
            code.append(String.join("\n", all));
            code.append("});\n");
            // only the helpers that the tests (or other used helpers) use
            final List<String> used = new ArrayList<>();
            boolean changed = true;
            while (changed) {
                changed = false;
                final String text = code + String.join("", used);
                for (final Map.Entry<String, String> helper : helpers.entrySet()) {
                    if (!used.contains(helper.getValue()) && java.util.regex.Pattern.compile("(?<![\\w$.])"
                            + java.util.regex.Pattern.quote(helper.getKey()) + "\\(").matcher(text).find()) {
                        used.add(helper.getValue());
                        changed = true;
                    }
                }
            }
            helpers.values().stream().filter(used::contains).forEach(h -> code.append('\n').append(h));
            final StringBuilder ts = new StringBuilder(TsGenerator.HEADER);
            if (!notes.isEmpty()) {
                ts.append("//\n// Not generated:\n");
                notes.stream().distinct().forEach(n -> ts.append(NOTE).append(n).append('\n'));
            }
            ts.append('\n');
            if (code.indexOf("assert") >= 0) {
                ts.append("import assert from \"node:assert/strict\";\n");
            }
            ts.append("import { describe, test } from \"node:test\";\n");
            ts.append(imports.render(code.toString())).append('\n');
            ts.append(code);
            if (usesAssertValue) {
                ts.append("""

                        /** Asserts that two values are equal: by `equals` if they have it, by content if they are \
                        arrays, bytes, sets, maps or dates, otherwise identical. */
                        function assertValue(expected: unknown, actual: unknown): void {
                            if (typeof expected === "object" && expected !== null && "equals" in expected
                                    && typeof expected.equals === "function") {
                                assert.ok(expected.equals(actual), `expected ${String(expected)} but was ${String(actual)}`);
                            } else if (expected instanceof Uint8Array || expected instanceof Date || expected instanceof Set
                                    || expected instanceof Map || Array.isArray(expected)) {
                                assert.deepStrictEqual(actual, expected);
                            } else {
                                assert.strictEqual(actual, expected);
                            }
                        }
                        """);
            }
            return new GeneratedFile(directory + "/" + fileName, ts.toString());
        }
    }
}
