package org.hiero.sdk.v3.metalang.generator.java;

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
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
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
 * Generates the JUnit tests of the generated API: one test class per record, class and enum (and per abstraction with
 * static methods), one per factory class, in {@code src/test/java} of the module and in the package of the tested
 * type. The tests check the contract of the specs, so they cover what the generator implements and what humans
 * implement later:
 *
 * <ul>
 *   <li><b>Constructors:</b> valid values create an object whose accessors return them; {@code null} for a
 *       non-nullable value is rejected with {@code NullPointerException}; every validation annotation
 *       ({@code @@min}/{@code @@max}, the range of the integer type, {@code @@minLength}/{@code @@maxLength},
 *       {@code @@minSize}/{@code @@maxSize}, {@code @@pattern}, {@code @@urlPattern}) accepts its boundary values and
 *       rejects the values just outside with {@code IllegalArgumentException}; the constructor without the
 *       {@code @@default} attributes sets the defaults.</li>
 *   <li><b>Copies:</b> collections and {@code bytes} passed in are copied (changing them afterwards does not change
 *       the object), returned collections are unmodifiable, returned arrays are copies.</li>
 *   <li><b>Setters</b> of mutable attributes: return the object, change the value, accept or reject {@code null},
 *       check the validation annotations and keep the old value when they reject one; the initial value of
 *       attributes that the constructor does not take.</li>
 *   <li><b>Value semantics</b> of records and immutable classes: equal values give equal objects and hash
 *       codes.</li>
 *   <li><b>Methods:</b> every method can be called with valid arguments and returns a value (not {@code null} unless
 *       {@code @@nullable}); only the errors of {@code @@throws} may occur.</li>
 *   <li><b>Factory methods:</b> return objects for valid arguments, reject {@code null} for non-nullable parameters
 *       and check the validation annotations of the parameters like constructors.</li>
 * </ul>
 *
 * <p>Methods are stubs until they are implemented, so their tests fail until then. Test values come from
 * {@link JavaSamples}; a test whose values cannot be built is not generated and listed in the comment of the test
 * class.
 */
public final class TestGenerator {

    /** The package of the JUnit annotations. */
    static final String JUNIT = "org.junit.jupiter.api";

    private static final String INDENT = "    ";

    private TestGenerator() {
    }

    /**
     * Returns the tests that could not be generated, as listed in the comments of the generated test classes.
     *
     * @param files the generated files
     * @return {@code TestClass: reason}, sorted
     */
    public static List<String> untested(final List<GeneratedFile> files) {
        final List<String> result = new ArrayList<>();
        for (final GeneratedFile file : files) {
            if (!file.path().contains("/src/test/java/")) {
                continue;
            }
            final String name = file.path().substring(file.path().lastIndexOf('/') + 1, file.path().length() - 5);
            file.content().lines().filter(l -> l.startsWith(NOTE)).forEach(l -> result.add(name + ": "
                    + l.substring(NOTE.length())));
        }
        return result.stream().sorted().toList();
    }

    /** The prefix of a line that lists a test that is not generated. */
    private static final String NOTE = "/// - ";

    /**
     * The path of a test file.
     *
     * @param module    the Java module
     * @param namespace the namespace of the tested type
     * @param className the simple name of the test class
     * @return the path below {@code src/test/java}
     */
    static String path(final String module, final String namespace, final String className) {
        return JavaNames.packageDirectory(module, namespace).replaceFirst("/src/main/java/", "/src/test/java/") + "/"
                + className + ".java";
    }

    /**
     * Generates the test class of a type.
     *
     * @param module  the Java module
     * @param type    the generated type
     * @param context the generation context
     * @param visible   the Java modules the tests can use: the module and the modules it requires
     * @param functions the generated namespace-level functions (factory methods for test values)
     * @return the test file, empty if there is nothing to test (an abstraction without static methods)
     */
    static Optional<GeneratedFile> generate(final String module, final TypeDefinition type,
                                            final JavaContext context, final Set<String> visible,
                                            final List<FunctionDefinition> functions) {
        final String className = type.name().name() + "Test";
        if (context.model().type(new QualifiedName(type.name().namespace(), className)).isPresent()) {
            return Optional.empty(); // a spec type with the name of the test class
        }
        final TestFile file = new TestFile(type.name().namespace(), className, context, visible, functions);
        final boolean tested = switch (type) {
            case TypeDefinition.EnumDefinition enumType -> file.enumTests(enumType);
            case TypeDefinition.ComplexTypeDefinition complex when complex.abstraction() ->
                    file.staticMethodTests(complex);
            case TypeDefinition.ComplexTypeDefinition complex -> file.objectTests(complex);
        };
        return tested ? Optional.of(new GeneratedFile(path(module, type.name().namespace(), className),
                file.render("`" + type.name().name() + "`", type.hasAnnotation("deprecated")))) : Optional.empty();
    }

    /**
     * Generates the test class of a factory class.
     *
     * @param module    the Java module
     * @param namespace the namespace
     * @param functions the functions of the factory class
     * @param context   the generation context
     * @param visible   the Java modules the tests can use: the module and the modules it requires
     * @param all       all generated namespace-level functions (factory methods for test values)
     * @return the test file
     */
    static GeneratedFile factory(final String module, final String namespace, final List<FunctionDefinition> functions,
                                 final JavaContext context, final Set<String> visible,
                                 final List<FunctionDefinition> all) {
        final String factory = FactoryGenerator.className(namespace);
        final TestFile file = new TestFile(namespace, factory + "Test", context, visible, all);
        file.factoryTests(factory, functions);
        return new GeneratedFile(path(module, namespace, factory + "Test"), file.render("`" + factory + "`", false));
    }

    /**
     * A value the tests pass: an attribute or a parameter.
     *
     * @param name        the name in the specs
     * @param type        the type, type variables substituted
     * @param annotations the annotations (nullability and validation)
     * @param declaration the Java type of the attribute or parameter
     * @param varargs     whether it is a varargs parameter (the type is the element type)
     */
    private record Input(String name, Type type, List<Annotation> annotations, String declaration, boolean varargs) {

        boolean nullable() {
            return annotations.stream().anyMatch(a -> a.name().equals("nullable"));
        }

        String identifier() {
            return JavaKeywords.identifier(name);
        }

        String capitalized() {
            return Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }
    }

    /**
     * A test value for the validation annotations: accepted or rejected.
     *
     * @param label      the part of the test name, e.g. {@code AtMaximum}
     * @param expression the value
     * @param accepted   whether the value is valid
     */
    private record Case(String label, String expression, boolean accepted, String detail) {

        private Case(final String label, final String expression, final boolean accepted) {
            this(label, expression, accepted, "");
        }

        /** The readable description, e.g. {@code a value above the maximum (65536)}. */
        String text() {
            final String text = switch (label) {
                case "AtMinimum" -> "the minimum";
                case "AtMaximum" -> "the maximum";
                case "BelowMinimum" -> "a value below the minimum";
                case "AboveMaximum" -> "a value above the maximum";
                case "AtMinimumLength" -> "a value with the minimum length";
                case "AtMaximumLength" -> "a value with the maximum length";
                case "BelowMinimumLength" -> "a value shorter than the minimum length";
                case "AboveMaximumLength" -> "a value longer than the maximum length";
                case "NotMatchingThePattern" -> "a value that does not match the pattern";
                case "ThatIsNoUrl" -> "a value that is no URL";
                case "AtMinimumSize" -> "the minimum number of elements";
                case "AtMaximumSize" -> "the maximum number of elements";
                case "BelowMinimumSize" -> "fewer elements than the minimum";
                default -> "more elements than the maximum";
            };
            return detail.isEmpty() ? text : text + " (" + detail + ")";
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
        private final Map<String, String> helpers = new LinkedHashMap<>();
        private final Map<String, String> helperNames = new HashMap<>();
        private final Set<String> testNames = new HashSet<>();
        private final List<String> notes = new ArrayList<>();
        private boolean deprecated;
        private boolean usesAssertValue;

        private TestFile(final String namespace, final String className, final JavaContext context,
                         final Set<String> visible, final List<FunctionDefinition> functions) {
            this.namespace = namespace;
            this.className = className;
            this.context = context;
            this.imports = context.imports(JavaNames.packageName(namespace), className);
            this.samples = new JavaSamples(context, imports,
                    name -> context.module(name.namespace()).filter(visible::contains).isPresent(), functions);
        }

        // ------------------------------------------------------------------------------------------------ objects

        /** Tests of a record or class. */
        private boolean objectTests(final TypeDefinition.ComplexTypeDefinition type) {
            final Optional<Map<Type.TypeVariable, Type>> variables = JavaSamples.defaults(type.typeParameters());
            if (variables.isEmpty()) {
                notes.add("no type arguments for " + type.name().name() + " (a type parameter refers to itself)");
                return true;
            }
            deprecated |= type.hasAnnotation("deprecated");
            final Type.DeclaredType self = new Type.DeclaredType(type.name(), type.typeParameters().stream()
                    .map(p -> variables.get().get(p.variable())).toList());
            final String typeName = JavaTypes.type(self, true, imports);
            final String rawName = imports.use(JavaNames.packageName(namespace), type.name().name());
            final String constructor = "new " + rawName + (type.typeParameters().isEmpty() ? "" : "<>");
            final List<Input> parameters = samples.constructorParameters(type).stream()
                    .map(f -> input(type, f, variables.get())).toList();
            final List<Input> fields = type.fields().stream().map(f -> input(type, f, variables.get())).toList();
            final Map<String, FieldDefinition> definitions = new HashMap<>();
            type.fields().forEach(f -> definitions.put(f.name(), f));

            // the values of the constructor
            final List<String> arguments = new ArrayList<>();
            for (final Input parameter : parameters) {
                final Optional<String> helper = helper(parameter, 0, true);
                if (helper.isEmpty() && !parameter.nullable()) {
                    notes.add("no valid value for `" + parameter.name() + "` of " + type.name().name()
                            + ": constructor, attribute and method tests");
                    staticMethodTests(type);
                    return true;
                }
                arguments.add(helper.map(h -> h + "()").orElseGet(() -> nullOf(parameter)));
            }
            helpers.put("create", INDENT + "private static " + typeName + " create() {\n" + INDENT + INDENT
                    + "return " + constructor + "(" + String.join(", ", arguments) + ");\n" + INDENT + "}\n");

            // constructor: values, defaults, null, constraints, copies
            final StringBuilder create = new StringBuilder();
            final List<String> locals = new ArrayList<>();
            for (int i = 0; i < parameters.size(); i++) {
                final Input parameter = parameters.get(i);
                final String local = local(parameter.identifier());
                create.append(INDENT).append(INDENT).append("final ").append(parameter.declaration()).append(' ')
                        .append(local).append(" = ").append(arguments.get(i)).append(";\n");
                locals.add(local);
            }
            create.append(INDENT).append(INDENT).append("final ").append(typeName).append(" subject = ")
                    .append(constructor).append("(").append(String.join(", ", locals)).append(");\n");
            for (int i = 0; i < parameters.size(); i++) {
                create.append(assertValue(locals.get(i), "subject." + parameters.get(i).identifier() + "()"));
            }
            for (final Input field : fields) {
                if (parameters.stream().noneMatch(p -> p.name().equals(field.name()))) {
                    create.append(initialValue(definitions.get(field.name()), field));
                }
            }
            test("shouldCreateAndReturnTheValues", "creates " + article(type.name().name())
                    + " and returns the values", "", create.toString());
            defaultConstructorTest(type, parameters, definitions, constructor, typeName);
            for (int i = 0; i < parameters.size(); i++) {
                final Input parameter = parameters.get(i);
                final int index = i;
                final java.util.function.Function<String, String> call = value -> constructor + "("
                        + replace(arguments, index, value) + ")";
                if (!parameter.nullable() && !JavaTypes.isPrimitive(parameter.declaration())) {
                    test("shouldRejectNull" + parameter.capitalized(), "rejects null for " + parameter.name(), "",
                            INDENT + INDENT
                            + "assertThrows(NullPointerException.class, () -> " + call.apply(nullOf(parameter))
                            + ");\n");
                }
                for (final Case c : cases(parameter)) {
                    if (c.accepted()) {
                        test("shouldAccept" + parameter.capitalized() + c.label(), parameter.name() + ": accepts "
                                        + c.text(), "",
                                INDENT + INDENT + "final " + parameter.declaration() + " value = " + c.expression()
                                        + ";\n" + INDENT + INDENT + "final " + typeName + " subject = "
                                        + call.apply("value") + ";\n"
                                        + assertValue("value", "subject." + parameter.identifier() + "()"));
                    } else {
                        test("shouldReject" + parameter.capitalized() + c.label(), parameter.name() + ": rejects "
                                + c.text(), "", INDENT + INDENT
                                + "assertThrows(IllegalArgumentException.class, () -> "
                                + call.apply(c.expression()) + ");\n");
                    }
                }
                copyTest(parameter, arguments.get(i), "In", local -> call.apply(local), "subject."
                        + parameter.identifier() + "()", typeName);
            }

            // setters of the mutable attributes
            for (final Input field : fields) {
                final FieldDefinition definition = definitions.get(field.name());
                if (!definition.hasAnnotation("immutable")) {
                    setterTests(field, current(field, definition, parameters), typeName);
                }
            }

            // value semantics
            if (type.fields().stream().allMatch(f -> f.hasAnnotation("immutable")) && !type.fields().isEmpty()
                    && type.methods("equals").isEmpty() && type.methods("hashCode").isEmpty()
                    && fields.stream().allMatch(f -> hasValueSemantics(f.type(), new HashSet<>()))) {
                test("shouldCompareByValue", "equal values give equal objects and hash codes", "", INDENT + INDENT + "final " + typeName + " subject = create();\n"
                        + INDENT + INDENT + "final " + typeName + " other = create();\n"
                        + INDENT + INDENT + "assertEquals(subject, other);\n"
                        + INDENT + INDENT + "assertEquals(subject.hashCode(), other.hashCode());\n");
            }

            // methods
            for (final MethodDefinition method : type.methods()) {
                methodTest(method, method.isStatic() ? rawName : "create()", variables.get());
            }
            return true;
        }

        private void defaultConstructorTest(final TypeDefinition.ComplexTypeDefinition type,
                                            final List<Input> parameters,
                                            final Map<String, FieldDefinition> definitions, final String constructor,
                                            final String typeName) {
            final List<Input> required = parameters.stream()
                    .filter(p -> !definitions.get(p.name()).hasAnnotation("default")).toList();
            if (required.size() == parameters.size()) {
                return;
            }
            final List<String> arguments = new ArrayList<>();
            for (final Input parameter : required) {
                final Optional<String> helper = helper(parameter, 0, true);
                if (helper.isEmpty() && !parameter.nullable()) {
                    return;
                }
                arguments.add(helper.map(h -> h + "()").orElseGet(() -> nullOf(parameter)));
            }
            final StringBuilder body = new StringBuilder(INDENT + INDENT + "final " + typeName + " subject = "
                    + constructor + "(" + String.join(", ", arguments) + ");\n");
            for (final Input parameter : parameters) {
                final FieldDefinition definition = definitions.get(parameter.name());
                if (definition.hasAnnotation("default")) {
                    body.append(assertValue(defaultValue(definition, parameter), "subject." + parameter.identifier()
                            + "()"));
                }
            }
            test("shouldCreateWithTheDefaultValues", "creates " + article(type.name().name())
                    + " with the default values", "", body.toString());
        }

        /** The assertion of the initial value of an attribute that the constructor does not take. */
        private String initialValue(final FieldDefinition definition, final Input field) {
            if (definition.hasAnnotation("default")) {
                return assertValue(defaultValue(definition, field), "subject." + field.identifier() + "()");
            }
            return INDENT + INDENT + "assertNull(subject." + field.identifier() + "());\n";
        }

        private String defaultValue(final FieldDefinition definition, final Input input) {
            final Literal value = definition.annotation("default").orElseThrow().arguments().getFirst();
            if (value instanceof Literal.NameLiteral name && name.text().equals("null")) {
                return "null";
            }
            return JavaLiterals.expression(value, input.type(), imports, context);
        }

        /** The expression of the value an attribute has after {@code create()}, for "unchanged" checks. */
        private Optional<String> current(final Input field, final FieldDefinition definition,
                                         final List<Input> parameters) {
            if (parameters.stream().anyMatch(p -> p.name().equals(field.name()))) {
                return helper(field, 0, true).map(h -> h + "()").or(() -> Optional.of("null"));
            }
            if (definition.hasAnnotation("default")) {
                return Optional.of(defaultValue(definition, field));
            }
            return Optional.of("null");
        }

        private void setterTests(final Input field, final Optional<String> current, final String typeName) {
            final String setter = InterfaceGenerator.setter(field.name());
            final String accessor = "subject." + field.identifier() + "()";
            final Optional<String> other = helper(field, 1, true);
            if (other.isPresent()) {
                test("shouldSet" + field.capitalized(), setterName(field) + " changes " + field.name()
                        + " and returns the object", "", INDENT + INDENT + "final " + typeName
                        + " subject = create();\n"
                        + INDENT + INDENT + "final " + field.declaration() + " value = " + other.get() + "();\n"
                        + INDENT + INDENT + "assertSame(subject, subject." + setter + "(value));\n"
                        + assertValue("value", accessor));
            }
            if (field.nullable()) {
                test("shouldSet" + field.capitalized() + "ToNull", setterName(field) + " accepts null", "", INDENT + INDENT + "final " + typeName
                        + " subject = create();\n"
                        + INDENT + INDENT + "subject." + setter + "(" + nullOf(field) + ");\n"
                        + INDENT + INDENT + "assertNull(" + accessor + ");\n");
            } else if (!JavaTypes.isPrimitive(field.declaration())) {
                test("shouldRejectNull" + field.capitalized() + "InSetter", setterName(field)
                        + " rejects null and keeps the value", "", INDENT + INDENT + "final "
                        + typeName + " subject = create();\n"
                        + INDENT + INDENT + "assertThrows(NullPointerException.class, () -> subject." + setter
                        + "(" + nullOf(field) + "));\n"
                        + current.map(c -> assertValue(c, accessor)).orElse(""));
            }
            for (final Case c : cases(field)) {
                if (c.accepted()) {
                    test("shouldSet" + field.capitalized() + c.label(), setterName(field) + " accepts " + c.text(),
                            "", INDENT + INDENT + "final " + typeName
                            + " subject = create();\n"
                            + INDENT + INDENT + "final " + field.declaration() + " value = " + c.expression() + ";\n"
                            + INDENT + INDENT + "subject." + setter + "(value);\n"
                            + assertValue("value", accessor));
                } else {
                    test("shouldNotSet" + field.capitalized() + c.label(), setterName(field) + " rejects " + c.text()
                            + " and keeps the value", "", INDENT + INDENT + "final "
                            + typeName + " subject = create();\n"
                            + INDENT + INDENT + "assertThrows(IllegalArgumentException.class, () -> subject."
                            + setter + "(" + c.expression() + "));\n"
                            + current.map(v -> assertValue(v, accessor)).orElse(""));
                }
            }
            other.ifPresent(o -> copyTest(field, o + "()", "InSetter", local -> "create()",
                    accessor, typeName));
        }

        /**
         * The copy test of a collection or {@code bytes} value: changing the value passed in does not change the
         * object; the returned collection is unmodifiable, the returned array a copy.
         *
         * @param input    the attribute
         * @param value    the expression of a valid value
         * @param suffix   the suffix of the test name
         * @param create   creates the object with the given local as value (for setters: without it)
         * @param accessor the accessor call on {@code subject}
         * @param typeName the Java type of the object
         */
        private void copyTest(final Input input, final String value, final String suffix,
                              final java.util.function.Function<String, String> create, final String accessor,
                              final String typeName) {
            final Optional<String> copy = mutableCopy(input.type(), value);
            if (copy.isEmpty()) {
                return;
            }
            final boolean bytes = JavaConstraints.isBytes(input.type());
            final String setter = suffix.equals("InSetter") ? INDENT + INDENT + "subject."
                    + InterfaceGenerator.setter(input.name()) + "(value);\n" : "";
            final String declaration = JavaTypes.declaration(input.type(), false, imports);
            final StringBuilder body = new StringBuilder();
            body.append(INDENT).append(INDENT).append("final ").append(declaration).append(" value = ")
                    .append(copy.get()).append(";\n");
            body.append(INDENT).append(INDENT).append("final ").append(declaration).append(" expected = ")
                    .append(bytes ? "value.clone()" : copyOf(input.type(), "value")).append(";\n");
            body.append(INDENT).append(INDENT).append("final ").append(typeName).append(" subject = ")
                    .append(create.apply("value")).append(";\n").append(setter);
            if (bytes) {
                body.append(INDENT).append(INDENT).append("value[0] = (byte) (value[0] + 1);\n");
                body.append(assertValue("expected", accessor));
                body.append(INDENT).append(INDENT).append("final byte[] returned = ").append(accessor)
                        .append(";\n");
                body.append(INDENT).append(INDENT).append("assertNotNull(returned);\n");
                body.append(INDENT).append(INDENT).append("returned[0] = (byte) (returned[0] + 1);\n");
                body.append(assertValue("expected", accessor));
            } else {
                body.append(INDENT).append(INDENT).append("value.clear();\n");
                body.append(assertValue("expected", accessor));
                body.append(INDENT).append(INDENT).append("assertThrows(UnsupportedOperationException.class, () -> ")
                        .append(imports.use("java.util", "Objects")).append(".requireNonNull(").append(accessor)
                        .append(").clear());\n");
            }
            test("shouldCopy" + input.capitalized() + suffix, (suffix.equals("InSetter") ? setterName(input)
                    : "the constructor") + " copies " + input.name() + (bytes ? "; the accessor returns a copy"
                    : "; the accessor returns an unmodifiable " + (input.type() instanceof Type.BasicType basic
                    && basic.builtin().category() == BuiltinType.Category.MAP ? "map" : "collection")), "",
                    body.toString());
        }

        /**
         * A modifiable copy of a non-empty collection or array value.
         *
         * @param type  the type
         * @param value the expression of the value
         * @return the expression, empty if the value is no collection or array or empty
         */
        private Optional<String> mutableCopy(final Type type, final String value) {
            if (!(type instanceof Type.BasicType basic)) {
                return Optional.empty();
            }
            return switch (basic.builtin().category()) {
                case BYTES -> Optional.of(value).filter(v -> !v.equals("new byte[0]"));
                case COLLECTION -> Optional.of("new " + imports.use("java.util", basic.builtin().name().equals("set")
                        ? "LinkedHashSet" : "ArrayList") + "<>(" + value + ")");
                case MAP -> Optional.of("new " + imports.use("java.util", "LinkedHashMap") + "<>(" + value + ")");
                default -> Optional.empty();
            };
        }

        private String copyOf(final Type type, final String value) {
            final BuiltinType builtin = ((Type.BasicType) type).builtin();
            return imports.use("java.util", builtin.category() == BuiltinType.Category.MAP ? "Map"
                    : builtin.name().equals("set") ? "Set" : "List") + ".copyOf(" + value + ")";
        }

        // ------------------------------------------------------------------------------------------------- enums

        private boolean enumTests(final TypeDefinition.EnumDefinition type) {
            deprecated |= type.hasAnnotation("deprecated") || type.values().stream()
                    .anyMatch(v -> v.hasAnnotation("deprecated"));
            final String enumName = imports.use(JavaNames.packageName(namespace), type.name().name());
            if (!type.attributes().isEmpty()) {
                final StringBuilder body = new StringBuilder();
                for (final EnumValueDefinition value : type.values()) {
                    for (int i = 0; i < type.attributes().size() && i < value.arguments().size(); i++) {
                        final ParameterDefinition attribute = type.attributes().get(i);
                        body.append(assertValue(JavaLiterals.expression(value.arguments().get(i), attribute.type(),
                                imports, context), enumName + "." + value.name() + "."
                                + JavaKeywords.identifier(attribute.name()) + "()"));
                    }
                }
                test("shouldProvideTheAttributesOfTheConstants", "the constants have the attribute values of the "
                        + "specs", "", body.toString());
            }
            final Optional<EnumValueDefinition> first = type.values().stream()
                    .filter(v -> !v.hasAnnotation("deprecated")).findFirst()
                    .or(() -> type.values().stream().findFirst());
            for (final MethodDefinition method : type.methods()) {
                if (method.isStatic()) {
                    methodTest(method, enumName, Map.of());
                } else {
                    first.ifPresent(f -> methodTest(method, enumName + "." + f.name(), Map.of()));
                }
            }
            return !tests.isEmpty() || !notes.isEmpty();
        }

        // --------------------------------------------------------------------------------------------- methods

        /** Tests of the static methods of an abstraction (or of a type that cannot be created). */
        private boolean staticMethodTests(final TypeDefinition.ComplexTypeDefinition type) {
            final String typeName = imports.use(JavaNames.packageName(namespace), type.name().name());
            for (final MethodDefinition method : type.methods()) {
                if (method.isStatic()) {
                    methodTest(method, typeName, Map.of());
                }
            }
            return !tests.isEmpty() || !notes.isEmpty() && !type.abstraction();
        }

        /**
         * The test that a method can be called with valid arguments: it returns a value (not {@code null} unless
         * {@code @@nullable}) and throws at most the errors of {@code @@throws}.
         *
         * @param method    the method
         * @param target    the object or class the method is called on
         * @param variables the type arguments of the type
         */
        private void methodTest(final MethodDefinition method, final String target,
                                final Map<Type.TypeVariable, Type> variables) {
            final Optional<Map<Type.TypeVariable, Type>> own = JavaSamples.defaults(method.typeParameters());
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
            deprecated |= method.hasAnnotation("deprecated");
            final String call = target + "." + JavaKeywords.identifier(method.name()) + "("
                    + String.join(", ", arguments.get()) + ")";
            final boolean async = method.hasAnnotation("async") || method.hasAnnotation("streaming");
            final Type returnType = LinkedModel.substitute(method.returnType(), all);
            final boolean returnsValue = async || !(returnType instanceof Type.VoidType);
            final boolean checkResult = async || returnsValue && !method.hasAnnotation("nullable")
                    && !(returnType instanceof Type.BasicType basic && JavaTypes.isPrimitive(JavaTypes.type(basic,
                    false, imports)));
            final String inner = async ? INDENT + INDENT : INDENT + INDENT + INDENT;
            final String display = method.name() + "(" + method.parameters().stream().map(ParameterDefinition::name)
                    .collect(Collectors.joining(", ")) + ") can be called" + (checkResult ? " and returns a value" : "");
            final StringBuilder statements = new StringBuilder();
            if (checkResult) {
                statements.append(inner).append("assertNotNull(").append(call).append(");\n");
            } else {
                statements.append(inner).append(call).append(";\n");
            }
            final List<String> errors = async ? List.of() : method.annotation("throws").stream()
                    .flatMap(t -> t.arguments().stream()).map(Literal::text).distinct()
                    .map(context::exception).map(e -> imports.use(e.packageName(), e.simpleName())).distinct()
                    .toList();
            if (errors.isEmpty() && !async) {
                test("shouldCall" + capitalize(method.name()), display, "",
                        statements.toString().substring(INDENT.length()));
                return;
            }
            if (errors.isEmpty()) {
                test("shouldCall" + capitalize(method.name()), display, "", statements.toString());
                return;
            }
            // the declared errors are an allowed result
            test("shouldCall" + capitalize(method.name()), display + " (only " + String.join(", ", errors)
                    + " may occur)", " throws Exception", INDENT + INDENT + "try {\n"
                    + statements
                    + INDENT + INDENT + "} catch (final Exception e) {\n"
                    + INDENT + INDENT + INDENT + "if (!(" + errors.stream().map(e -> "e instanceof " + e)
                    .collect(Collectors.joining(" || ")) + ")) {\n"
                    + INDENT + INDENT + INDENT + INDENT + "throw e;\n"
                    + INDENT + INDENT + INDENT + "}\n"
                    + INDENT + INDENT + "}\n");
        }

        /** The arguments of a call: a helper value for every parameter, {@code null} where none exists. */
        private Optional<List<String>> arguments(final MethodDefinition method,
                                                 final Map<Type.TypeVariable, Type> variables) {
            final List<String> arguments = new ArrayList<>();
            for (final ParameterDefinition parameter : method.parameters()) {
                final Input input = input(parameter, variables);
                final Optional<String> helper = helper(input, 0);
                if (helper.isEmpty() && (!input.nullable() || input.varargs())) {
                    return Optional.empty();
                }
                arguments.add(helper.map(h -> h + "()").orElseGet(() -> nullOf(input)));
            }
            return Optional.of(arguments);
        }

        // ------------------------------------------------------------------------------------------- factories

        private void factoryTests(final String factory, final List<FunctionDefinition> functions) {
            final String factoryName = imports.use(JavaNames.packageName(namespace), factory);
            for (final FunctionDefinition function : functions) {
                final MethodDefinition method = function.method();
                methodTest(method, factoryName, Map.of());
                final Optional<Map<Type.TypeVariable, Type>> variables = JavaSamples.defaults(method.typeParameters());
                final Optional<List<String>> arguments = variables.flatMap(v -> arguments(method, v));
                if (arguments.isEmpty()) {
                    continue;
                }
                final String name = capitalize(method.name());
                for (int i = 0; i < method.parameters().size(); i++) {
                    final Input parameter = input(method.parameters().get(i), variables.get());
                    final int index = i;
                    final java.util.function.Function<String, String> call = value -> factoryName + "."
                            + JavaKeywords.identifier(method.name()) + "(" + replace(arguments.get(), index, value)
                            + ")";
                    if (!parameter.nullable() && !parameter.varargs()
                            && !JavaTypes.isPrimitive(parameter.declaration())) {
                        test("shouldRejectNull" + parameter.capitalized() + "In" + name, method.name()
                                + " rejects null for " + parameter.name(), "", INDENT + INDENT
                                + "assertThrows(NullPointerException.class, () -> " + call.apply(nullOf(parameter))
                                + ");\n");
                    }
                    for (final Case c : parameter.varargs() ? List.<Case>of() : cases(parameter)) {
                        if (c.accepted()) {
                            test("shouldAccept" + parameter.capitalized() + c.label() + "In" + name, method.name()
                                            + ": " + parameter.name() + " accepts " + c.text(), "",
                                    INDENT + INDENT + (returnsReference(method) ? "assertNotNull("
                                            + call.apply(c.expression()) + ")" : call.apply(c.expression())) + ";\n");
                        } else {
                            test("shouldReject" + parameter.capitalized() + c.label() + "In" + name, method.name()
                                            + ": " + parameter.name() + " rejects " + c.text(), "",
                                    INDENT + INDENT + "assertThrows(IllegalArgumentException.class, () -> "
                                            + call.apply(c.expression()) + ");\n");
                        }
                    }
                }
            }
        }

        private boolean returnsReference(final MethodDefinition method) {
            if (method.hasAnnotation("async") || method.hasAnnotation("streaming")) {
                return true;
            }
            return !(method.returnType() instanceof Type.VoidType) && !method.hasAnnotation("nullable")
                    && !(method.returnType() instanceof Type.BasicType basic
                    && JavaTypes.isPrimitive(JavaTypes.type(basic, false, imports)));
        }

        // -------------------------------------------------------------------------------------------- values

        private Input input(final TypeDefinition owner, final FieldDefinition field,
                            final Map<Type.TypeVariable, Type> variables) {
            final Type type = LinkedModel.substitute(field.type(), variables);
            deprecated |= field.hasAnnotation("deprecated");
            return new Input(field.name(), type, field.annotations(), JavaTypes.declaration(type,
                    field.hasAnnotation("nullable"), context.boxed(owner.name(), field.name()), imports), false);
        }

        private Input input(final ParameterDefinition parameter, final Map<Type.TypeVariable, Type> variables) {
            final Type type = LinkedModel.substitute(parameter.type(), variables);
            return new Input(parameter.name(), type, parameter.annotations(), JavaTypes.declaration(type,
                    parameter.hasAnnotation("nullable"), imports), parameter.varargs());
        }

        /**
         * The name of the helper method that returns a valid value of the input (a new value per call).
         *
         * @param input   the attribute or parameter
         * @param variant the variant of the value
         * @return the method name, empty if no value can be built
         */
        private Optional<String> helper(final Input input, final int variant) {
            return helper(input, variant, false);
        }

        /**
         * The name of the helper method that returns a valid value of the input (a new value per call).
         *
         * @param input   the attribute or parameter
         * @param variant the variant of the value
         * @param stored  whether the value is stored by the object under test (attributes): then abstractions
         *                without implementation may be represented by test doubles
         * @return the method name, empty if no value can be built
         */
        private Optional<String> helper(final Input input, final int variant, final boolean stored) {
            final String declaration = JavaTypes.declaration(input.type(), false, imports);
            final String key = input.name() + "|" + declaration + "|" + input.annotations().stream()
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
            final String base = input.identifier() + (variant == 0 ? "" : "Other") + "Value";
            final java.util.function.Function<String, String> render = n -> INDENT + "private static " + declaration
                    + " " + n + "() {\n" + INDENT + INDENT + "return " + value.get() + ";\n" + INDENT + "}\n";
            String name = base;
            for (int i = 2; helpers.containsKey(name) && !helpers.get(name).equals(render.apply(name)); i++) {
                name = base + i;
            }
            helperNames.put(key, name);
            helpers.put(name, render.apply(name));
            return Optional.of(name);
        }

        /**
         * The boundary values of the validation annotations of an input.
         *
         * @param input the attribute or parameter
         * @return the accepted and rejected values
         */
        private List<Case> cases(final Input input) {
            final List<Case> cases = new ArrayList<>();
            final List<Annotation> annotations = input.annotations();
            if (input.type() instanceof Type.BasicType basic) {
                final BuiltinType builtin = basic.builtin();
                switch (builtin.category()) {
                    case INTEGER -> integerCases(builtin, annotations, cases);
                    case FLOAT, DECIMAL, DURATION -> boundCases(basic, annotations, cases);
                    case STRING -> stringCases(annotations, cases);
                    case BYTES, COLLECTION, MAP -> sizeCases(input, cases);
                    default -> {
                    }
                }
            }
            // one test per label, and one per accepted value (@@minSize(4) @@maxSize(4) accept the same value)
            final Set<String> labels = new HashSet<>();
            final Set<String> accepted = new HashSet<>();
            return cases.stream().filter(c -> labels.add(c.label()))
                    .filter(c -> !c.accepted() || accepted.add(c.expression())).toList();
        }

        private void integerCases(final BuiltinType builtin, final List<Annotation> annotations,
                                  final List<Case> cases) {
            final JavaIntegers.Range range = JavaSamples.range(builtin, annotations);
            if (range.min().compareTo(range.max()) > 0) {
                return;
            }
            cases.add(new Case("AtMinimum", JavaIntegers.literal(builtin, range.min(), imports), true,
                    range.min().toString()));
            cases.add(new Case("AtMaximum", JavaIntegers.literal(builtin, range.max(), imports), true,
                    range.max().toString()));
            final BigInteger below = range.min().subtract(BigInteger.ONE);
            if (JavaIntegers.representable(builtin, below)) {
                cases.add(new Case("BelowMinimum", JavaIntegers.literal(builtin, below, imports), false,
                        below.toString()));
            }
            final BigInteger above = range.max().add(BigInteger.ONE);
            if (JavaIntegers.representable(builtin, above)) {
                cases.add(new Case("AboveMaximum", JavaIntegers.literal(builtin, above, imports), false,
                        above.toString()));
            }
        }

        private void boundCases(final Type.BasicType type, final List<Annotation> annotations,
                                final List<Case> cases) {
            for (final Annotation annotation : annotations) {
                final boolean min = annotation.name().equals("min");
                if (!min && !annotation.name().equals("max")) {
                    continue;
                }
                final String accepted;
                final String rejected;
                switch (type.builtin().category()) {
                    case FLOAT -> {
                        final BigDecimal bound = JavaSamples.bound(annotations, annotation.name()).orElseThrow();
                        accepted = JavaSamples.doubleLiteral(bound);
                        rejected = "Math." + (min ? "nextDown(" : "nextUp(") + accepted + ")";
                    }
                    case DECIMAL -> {
                        final BigDecimal bound = JavaSamples.bound(annotations, annotation.name()).orElseThrow();
                        final String decimal = imports.use("java.math", "BigDecimal");
                        accepted = "new " + decimal + "(\"" + bound.toPlainString() + "\")";
                        final BigDecimal step = new BigDecimal("0.001");
                        rejected = "new " + decimal + "(\"" + (min ? bound.subtract(step) : bound.add(step))
                                .toPlainString() + "\")";
                    }
                    default -> {
                        accepted = JavaLiterals.expression(annotation.arguments().getFirst(), type, imports);
                        rejected = accepted + (min ? ".minusNanos(1)" : ".plusNanos(1)");
                    }
                }
                final String bound = annotation.arguments().getFirst().text();
                cases.add(new Case(min ? "AtMinimum" : "AtMaximum", accepted, true, bound));
                cases.add(new Case(min ? "BelowMinimum" : "AboveMaximum", rejected, false));
            }
        }

        private void stringCases(final List<Annotation> annotations, final List<Case> cases) {
            final Optional<Integer> minLength = JavaSamples.size(annotations, "minLength");
            final Optional<Integer> maxLength = JavaSamples.size(annotations, "maxLength");
            if (minLength.isPresent()) {
                final int length = minLength.get();
                JavaSamples.string(sized(annotations, "minLength", "maxLength", length), 0)
                        .ifPresent(s -> cases.add(new Case("AtMinimumLength", JavaLiterals.quote(s), true)));
                if (length > 0) {
                    cases.add(new Case("BelowMinimumLength", JavaLiterals.quote("a".repeat(length - 1)), false));
                }
            }
            if (maxLength.isPresent()) {
                final int length = maxLength.get();
                JavaSamples.string(sized(annotations, "minLength", "maxLength", length), 0)
                        .ifPresent(s -> cases.add(new Case("AtMaximumLength", JavaLiterals.quote(s), true)));
                cases.add(new Case("AboveMaximumLength", JavaLiterals.quote("a".repeat(length + 1)), false));
            }
            JavaSamples.stringArgument(annotations, "pattern").flatMap(RegexSamples::rejected)
                    .filter(s -> !JavaSamples.isValidString(s, annotations))
                    .ifPresent(s -> cases.add(new Case("NotMatchingThePattern", JavaLiterals.quote(s), false)));
            if (annotations.stream().anyMatch(a -> a.name().equals("urlPattern"))) {
                cases.add(new Case("ThatIsNoUrl", JavaLiterals.quote("not a url"), false));
            }
        }

        private void sizeCases(final Input input, final List<Case> cases) {
            final List<Annotation> annotations = input.annotations();
            final Optional<Integer> minSize = JavaSamples.size(annotations, "minSize");
            final Optional<Integer> maxSize = JavaSamples.size(annotations, "maxSize");
            if (minSize.isPresent()) {
                sizedValue(input, minSize.get()).ifPresent(v -> cases.add(new Case("AtMinimumSize", v, true)));
                if (minSize.get() > 0) {
                    sizedValue(input, minSize.get() - 1).ifPresent(v -> cases.add(new Case("BelowMinimumSize", v,
                            false)));
                }
            }
            if (maxSize.isPresent()) {
                sizedValue(input, maxSize.get()).ifPresent(v -> cases.add(new Case("AtMaximumSize", v, true)));
                sizedValue(input, maxSize.get() + 1).ifPresent(v -> cases.add(new Case("AboveMaximumSize", v,
                        false)));
            }
        }

        private Optional<String> sizedValue(final Input input, final int size) {
            return samples.value(input.type(), sized(input.annotations(), "minSize", "maxSize", size), 0);
        }

        /** The annotations with the given lower and upper bound replaced by {@code size}. */
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

        /** Whether equal values of the type are equal objects ({@code equals} compares the content). */
        private boolean hasValueSemantics(final Type type, final Set<QualifiedName> visited) {
            return switch (type) {
                case Type.BasicType basic -> basic.builtin().category() != BuiltinType.Category.STREAM_RESULT
                        && basic.arguments().stream().allMatch(a -> hasValueSemantics(a, visited));
                case Type.DeclaredType declared -> {
                    if (!visited.add(declared.name())) {
                        yield true;
                    }
                    final TypeDefinition definition = context.model().definition(declared);
                    yield switch (definition) {
                        case TypeDefinition.EnumDefinition ignored -> true;
                        case TypeDefinition.ComplexTypeDefinition complex when complex.abstraction() -> false;
                        // a class without attributes has no equals (identity)
                        case TypeDefinition.ComplexTypeDefinition complex -> !complex.fields().isEmpty()
                                && complex.fields().stream().allMatch(f -> f.hasAnnotation("immutable"))
                                        && complex.methods("equals").isEmpty() && complex.methods("hashCode").isEmpty()
                                        && complex.fields().stream().allMatch(f -> f.hasAnnotation("nullable")
                                        || hasValueSemantics(LinkedModel.substitute(f.type(), JavaSamples
                                        .arguments(complex, declared).orElse(Map.of())), visited));
                    };
                }
                case Type.WildcardType wildcard -> wildcard.upperBound() == null
                        || hasValueSemantics(wildcard.upperBound(), visited);
                case Type.FunctionType ignored -> false;
                default -> true;
            };
        }

        // ------------------------------------------------------------------------------------------- rendering

        /** {@code null} with the type of the input: selects the overload of the call. */
        private String nullOf(final Input input) {
            return "(" + JavaTypes.type(input.type(), true, imports) + ") null";
        }

        private String assertValue(final String expected, final String actual) {
            usesAssertValue = true;
            return INDENT + INDENT + "assertValue(" + expected + ", " + actual + ");\n";
        }

        /**
         * Adds a test method.
         *
         * @param name         the method name (made unique)
         * @param display      the readable description ({@code @DisplayName})
         * @param throwsClause the throws clause, empty if none
         * @param body         the statements
         */
        private void test(final String name, final String display, final String throwsClause, final String body) {
            String unique = name;
            for (int i = 2; !testNames.add(unique); i++) {
                unique = name + i;
            }
            tests.add(INDENT + "@" + imports.use(JUNIT, "Test") + "\n" + INDENT + "@"
                    + imports.use(JUNIT, "DisplayName") + "(" + JavaLiterals.quote(display) + ")\n"
                    + INDENT + "void " + unique + "()" + throwsClause + " {\n" + body + INDENT + "}\n");
        }

        private static String setterName(final Input input) {
            return InterfaceGenerator.setter(input.name());
        }

        private static String article(final String name) {
            return ("AEIOU".indexOf(name.charAt(0)) >= 0 ? "an " : "a ") + name;
        }

        private static String local(final String identifier) {
            return switch (identifier) {
                case "subject", "value", "expected", "other", "returned" -> identifier + "Argument";
                default -> identifier;
            };
        }

        private static String replace(final List<String> arguments, final int index, final String value) {
            final List<String> result = new ArrayList<>(arguments);
            result.set(index, value);
            return String.join(", ", result);
        }

        private static String capitalize(final String name) {
            return Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }

        private String render(final String tested, final boolean testedDeprecated) {
            deprecated |= testedDeprecated || samples.usesDeprecated();
            final String nullable = usesAssertValue ? imports.use(JavaTypes.JSPECIFY, "Nullable") : "";
            final String arrays = usesAssertValue ? imports.use("java.util", "Arrays") : "";
            final StringBuilder java = new StringBuilder(JavaGenerator.HEADER).append('\n');
            java.append("package ").append(JavaNames.packageName(namespace)).append(";\n\n");
            java.append("import static ").append(JUNIT).append(".Assertions.*;\n\n");
            // render the members first: they register the imports
            final List<String> members = new ArrayList<>(tests);
            members.addAll(helpers.values());
            if (usesAssertValue) {
                members.add(INDENT + "/// Asserts that two values are equal; arrays by their content.\n"
                        + INDENT + "private static void assertValue(final @" + nullable + " Object expected, "
                        + "final @" + nullable + " Object actual) {\n"
                        + INDENT + INDENT + "if (expected instanceof byte[] bytes && actual instanceof byte[] other) {\n"
                        + INDENT + INDENT + INDENT + "assertArrayEquals(bytes, other, () -> "
                        + "\"expected \" + " + arrays + ".toString(bytes) + \" but was \" + " + arrays
                        + ".toString(other));\n"
                        + INDENT + INDENT + "} else {\n"
                        + INDENT + INDENT + INDENT + "assertEquals(expected, actual);\n"
                        + INDENT + INDENT + "}\n"
                        + INDENT + "}\n");
            }
            final String displayName = imports.use(JUNIT, "DisplayName");
            final String importBlock = imports.render();
            if (!importBlock.isEmpty()) {
                java.append(importBlock).append('\n');
            }
            java.append("/// Tests of ").append(tested).append(", generated from the specs.\n");
            if (!notes.isEmpty()) {
                java.append("///\n/// Not generated:\n");
                notes.stream().distinct().forEach(n -> java.append(NOTE).append(n).append('\n'));
            }
            if (deprecated) {
                java.append("@SuppressWarnings(\"deprecation\")\n");
            }
            java.append("@").append(displayName).append("(").append(JavaLiterals.quote(
                    tested.replace("`", ""))).append(")\n");
            java.append("class ").append(className).append(" {\n");
            for (final String member : members) {
                java.append('\n').append(member);
            }
            java.append("}\n");
            return java.toString();
        }
    }
}
