package org.hiero.sdk.v3.metalang.generator.rust;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
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
import org.hiero.sdk.v3.metalang.model.TypeParameterDefinition;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;

/**
 * Generates the tests of the Rust API as one integration test per crate ({@code tests/api}): one module per type and
 * one per namespace with functions, in the module tree of the namespaces. The tests check the contract of the specs
 * like the Java and TypeScript tests, as far as the Rust type system does not already guarantee it (no tests for
 * {@code null}, copies or integer ranges of exact Rust types): construction and attribute values, the boundaries of
 * the validation annotations and of integer types without exact Rust type ({@code InvalidArgumentError}), setters,
 * that every method and function can be called, the constants of enums ({@code Display}, {@code FromStr}), value
 * equality, and that every type can be shared between threads. Methods are stubs ({@code todo!}) until they are
 * implemented, so their tests fail until then. A test whose values cannot be built is an ignored test and listed at
 * the top of the test file.
 */
public final class RustTestGenerator {

    private static final String INDENT = RustTypeGenerator.INDENT;

    /** The prefix of a line that lists a test that is not generated. */
    private static final String NOTE = "// - ";

    /** The directory of the integration test of a crate. */
    static final String TESTS = "tests/api";

    private RustTestGenerator() {
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
            if (file.path().contains("/" + TESTS + "/") && file.path().endsWith(".rs")) {
                final String name = file.path().substring(file.path().indexOf("/" + TESTS + "/") + TESTS.length() + 2);
                file.content().lines().filter(l -> l.startsWith(NOTE))
                        .forEach(l -> result.add(name + ": " + l.substring(NOTE.length())));
            }
        }
        return result.stream().sorted().toList();
    }

    static Optional<GeneratedFile> generate(final TypeDefinition type, final RustContext context) {
        final TestFile file = new TestFile(type.name().namespace(), context);
        final boolean tested = switch (type) {
            case TypeDefinition.EnumDefinition enumType -> file.enumTests(enumType);
            case TypeDefinition.ComplexTypeDefinition complex when context.isSealed(complex.name()) ->
                    file.sealedTests(complex);
            case TypeDefinition.ComplexTypeDefinition complex when complex.abstraction() -> file.traitTests(complex);
            case TypeDefinition.ComplexTypeDefinition complex -> file.structTests(complex);
        };
        return tested ? Optional.of(file.render(context.fileModule(type) + ".rs")) : Optional.empty();
    }

    static Optional<GeneratedFile> functions(final String namespace, final RustContext context) {
        final List<FunctionDefinition> functions = context.functions(namespace);
        if (functions.isEmpty()) {
            return Optional.empty();
        }
        final TestFile file = new TestFile(namespace, context);
        file.functionTests(functions);
        return Optional.of(file.render("functions.rs"));
    }

    /**
     * Generates the module files of the integration test of a crate: {@code main.rs}, {@code helpers.rs} and the
     * {@code mod.rs} of every module.
     *
     * @param folder  the spec folder of the crate
     * @param tests   the generated test files
     * @param context the generation context
     * @return the files, empty if the crate has no tests
     */
    static List<GeneratedFile> testModules(final String folder, final List<GeneratedFile> tests,
                                           final RustContext context) {
        if (tests.isEmpty()) {
            return List.of();
        }
        final String root = RustNames.crateDirectory(folder) + "/" + TESTS + "/";
        // directory → module names
        final Map<String, TreeSet<String>> modules = new LinkedHashMap<>();
        for (final GeneratedFile test : tests) {
            final String relative = test.path().substring(root.length());
            final String[] parts = relative.split("/");
            String directory = "";
            for (int i = 0; i < parts.length; i++) {
                final String name = i == parts.length - 1 ? parts[i].replace(".rs", "") : parts[i];
                modules.computeIfAbsent(directory, k -> new TreeSet<>()).add(name);
                directory = directory.isEmpty() ? parts[i] : directory + "/" + parts[i];
            }
        }
        final List<GeneratedFile> files = new ArrayList<>();
        for (final Map.Entry<String, TreeSet<String>> entry : modules.entrySet()) {
            final StringBuilder rs = new StringBuilder(RustGenerator.HEADER);
            if (entry.getKey().isEmpty()) {
                rs.append("//! The tests of the `").append(context.config().crateName(folder))
                        .append("` crate, generated from the specs: the contract of the API.\n")
                        .append("#![allow(deprecated)]\n\nmod helpers;\n");
            } else {
                rs.append('\n');
            }
            entry.getValue().forEach(m -> rs.append("mod ").append(m).append(";\n"));
            files.add(new GeneratedFile(root + (entry.getKey().isEmpty() ? "main.rs" : entry.getKey() + "/mod.rs"),
                    rs.toString()));
        }
        files.add(new GeneratedFile(root + "helpers.rs", RustGenerator.HEADER + """
                //! Helpers of the generated tests.
                #![allow(dead_code)]

                use std::future::Future;
                use std::pin::pin;
                use std::sync::Arc;
                use std::task::{Context, Poll, Wake, Waker};

                struct ThreadWaker(std::thread::Thread);

                impl Wake for ThreadWaker {
                    fn wake(self: Arc<Self>) {
                        self.0.unpark();
                    }
                }

                /// Runs a future to completion on the current thread.
                pub fn block_on<F: Future>(future: F) -> F::Output {
                    let waker = Waker::from(Arc::new(ThreadWaker(std::thread::current())));
                    let mut context = Context::from_waker(&waker);
                    let mut future = pin!(future);
                    loop {
                        if let Poll::Ready(output) = future.as_mut().poll(&mut context) {
                            return output;
                        }
                        std::thread::park();
                    }
                }

                /// Compiles only if the type can be sent to and shared between threads.
                pub fn assert_send_sync<T: Send + Sync + ?Sized>() {}
                """));
        return files;
    }

    /**
     * A value the tests pass: an attribute or a parameter.
     *
     * @param name        the name
     * @param type        the meta-language type (without type variables): the samples are built for it
     * @param annotations the annotations
     * @param varargs     whether it is a varargs parameter
     * @param nullable    whether it is optional
     * @param rust        the Rust type the value must have (the storage of the attribute, the parameter type)
     * @param declared    the Rust type as declared (a type parameter stays a parameter): decides how the getter
     *                    returns the value
     */
    private record Input(String name, Type type, List<Annotation> annotations, boolean varargs, boolean nullable,
                         RustType rust, RustType declared) {

        /** The Rust form of the value (with {@code Option} and {@code Vec} for varargs). */
        RustType rustType(final RustContext context) {
            return rust;
        }

        /** The Rust form of one value as the samples build it (without {@code Option} and {@code Vec}). */
        RustType sampleType(final RustContext context) {
            return context.rustType(type, Map.of());
        }

        /** The Rust form of one value as stored or passed (without {@code Option} and {@code Vec}). */
        RustType elementType() {
            RustType element = nullable && rust instanceof RustType.Optional optional ? optional.inner() : rust;
            if (varargs && element instanceof RustType.VecOf vec) {
                element = vec.element();
            }
            return element;
        }
    }

    /** A boundary value of the validation annotations. */
    private record Case(String text, String expression, boolean accepted) {
    }

    /** A test function. */
    private record Test(String name, String description, String body, String ignored) {
    }

    /** The content of one test file. */
    private static final class TestFile {

        private final String namespace;
        private final RustContext context;
        private final String directory;
        private final RustImports imports;
        private final RustSamples samples;
        private final List<Test> tests = new ArrayList<>();
        private final Set<String> testNames = new HashSet<>();
        private final Map<String, String> helpers = new LinkedHashMap<>();
        private final Map<String, String> helperNames = new HashMap<>();
        private final List<String> notes = new ArrayList<>();

        private TestFile(final String namespace, final RustContext context) {
            this.namespace = namespace;
            this.context = context;
            final String folder = context.folder(namespace);
            this.directory = RustNames.crateDirectory(folder) + "/" + TESTS + "/" + RustNames.directory(namespace);
            this.imports = new RustImports(context, folder, true, "create", "block_on", "assert_send_sync");
            final Set<String> visible = new HashSet<>(SpecFolders.required(folder, context.folders()));
            visible.add(folder);
            this.samples = new RustSamples(context, imports, visible::contains);
        }

        // ------------------------------------------------------------------------------------------- structs

        private boolean structTests(final TypeDefinition.ComplexTypeDefinition type) {
            final Optional<Map<Type.TypeVariable, Type>> variables = Constraints.defaults(type.typeParameters());
            if (variables.isEmpty()) {
                notes.add("no type arguments for " + type.name().name() + " (a type parameter refers to itself)");
                return true;
            }
            final Type.DeclaredType self = new Type.DeclaredType(type.name(), type.typeParameters().stream()
                    .map(p -> variables.get().get(p.variable())).toList());
            final String typeName = context.rustType(self, Map.of()).render(imports);
            // the type in expressions: `Box::<String>::of(..)`
            final String structName = typeName.replaceFirst("<", "::<");
            final boolean fallible = context.isFallible(type.name());
            final List<FieldDefinition> fields = type.fields();
            final Map<Type.TypeVariable, RustType> scope = context.implScope(type, self, Map.of());
            final Map<Type.TypeVariable, RustType> declared = context.scope(type);
            final List<Input> inputs = fields.stream().map(f -> input(f, variables.get(), scope, declared)).toList();

            // the values of create()
            final List<String> values = new ArrayList<>();
            for (final Input input : inputs) {
                final Optional<String> helper = helper(input, 0, true);
                if (helper.isEmpty() && !input.nullable()) {
                    notes.add("no valid value for `" + input.name() + "` of " + type.name().name()
                            + ": constructor, attribute and method tests");
                    staticMethods(type, structName, variables.get(), scope);
                    sendSync(typeName);
                    return true;
                }
                values.add(helper.map(h -> h + "()").orElse("None"));
            }
            final java.util.function.Function<List<String>, String> construct = arguments -> structName + "::new("
                    + String.join(", ", arguments) + ")";
            final String valid = fallible ? ".expect(\"valid values\")" : "";
            helpers.put("create", "fn create() -> " + typeName + " {\n" + INDENT + construct.apply(values) + valid
                    + "\n}\n");

            // construction
            final StringBuilder create = new StringBuilder();
            for (int i = 0; i < inputs.size(); i++) {
                assertion(inputs.get(i), "subject", values.get(i)).ifPresent(create::append);
            }
            test("creates " + article(type.name().name()) + " and returns the values", create.isEmpty()
                    ? INDENT + "let _ = create();\n" : INDENT + "let subject = create();\n" + create);
            for (int i = 0; i < inputs.size(); i++) {
                final Input input = inputs.get(i);
                for (final Case c : cases(input)) {
                    final List<String> arguments = new ArrayList<>(values);
                    arguments.set(i, input.nullable() ? "Some(" + c.expression() + ")" : c.expression());
                    if (c.accepted()) {
                        final Optional<String> assertion = assertion(input, "subject", arguments.get(i));
                        test(input.name() + ": accepts " + c.text(), INDENT + "let " + (assertion.isPresent()
                                ? "subject" : "_") + " = " + construct.apply(arguments) + valid + ";\n"
                                + assertion.orElse(""));
                    } else {
                        test(input.name() + ": rejects " + c.text(), INDENT + "assert!("
                                + construct.apply(arguments) + ".is_err());\n");
                    }
                }
            }

            // setters
            for (int i = 0; i < fields.size(); i++) {
                if (!fields.get(i).hasAnnotation("immutable")) {
                    setterTests(inputs.get(i), RustConstraints.isChecked(fields.get(i)));
                }
            }

            // methods
            final Map<MethodDefinition, String> names = RustTypeGenerator.inherentNames(type, context);
            final Map<Type.TypeVariable, Type> all = variables.get();
            names.forEach((method, name) -> methodTest(method, name, method.isStatic() ? structName + "::" + name
                    : "create()." + name, all, scope));
            if (type.methods().stream().anyMatch(RustContext::isDisplay)) {
                test("can be formatted (toString)", INDENT + "let _ = create().to_string();\n");
            }
            if (context.capabilities(context.rustType(self, Map.of())).eq()) {
                test("compares by value", INDENT + "assert_eq!(create(), create());\n" + INDENT
                        + "assert_eq!(create().clone(), create());\n");
            }
            sendSync(typeName);
            return true;
        }

        private void setterTests(final Input input, final boolean checked) {
            final String setter = "set_" + RustNames.snake(input.name());
            final String getter = RustNames.member(input.name());
            final String ok = checked ? ".expect(\"valid value\")" : "";
            final Optional<String> other = helper(input, 1, true);
            if (other.isPresent()) {
                final StringBuilder body = new StringBuilder(INDENT + "let mut subject = create();\n" + INDENT
                        + "subject." + setter + "(" + other.get() + "())" + ok + ";\n");
                assertion(input, "subject", other.get() + "()").ifPresent(body::append);
                test(input.name() + " can be changed", body.toString());
            }
            if (input.nullable()) {
                test(input.name() + " can be set to none", INDENT + "let mut subject = create();\n" + INDENT
                        + "subject." + setter + "(None)" + ok + ";\n" + INDENT + "assert!(subject." + getter
                        + "().is_none());\n");
            }
            for (final Case c : cases(input)) {
                final String value = input.nullable() ? "Some(" + c.expression() + ")" : c.expression();
                if (c.accepted()) {
                    final StringBuilder body = new StringBuilder(INDENT + "let mut subject = create();\n" + INDENT
                            + "subject." + setter + "(" + value + ")" + ok + ";\n");
                    assertion(input, "subject", value).ifPresent(body::append);
                    test("setting " + input.name() + " accepts " + c.text(), body.toString());
                } else {
                    test("setting " + input.name() + " rejects " + c.text() + " and keeps the value", INDENT
                            + "let mut subject = create();\n" + INDENT + "let before = format!(\"{:?}\", subject."
                            + getter + "());\n" + INDENT + "assert!(subject." + setter + "(" + value + ").is_err());\n"
                            + INDENT + "assert_eq!(format!(\"{:?}\", subject." + getter + "()), before);\n");
                }
            }
        }

        /**
         * The assertion that the getter returns a value, if the value can be compared.
         *
         * @param input    the attribute
         * @param subject  the variable of the object
         * @param expected the expression of the expected value (owned, as passed to the constructor)
         * @return the statement
         */
        private Optional<String> assertion(final Input input, final String subject, final String expected) {
            if (!context.capabilities(input.rustType(context)).eq()) {
                return Optional.empty();
            }
            final RustType type = input.declared();
            final String actual = subject + "." + RustNames.member(input.name()) + "()";
            final String compared = switch (type) {
                case RustType.Optional optional -> switch (optional.inner()) {
                    case RustType.Text ignored -> "(" + expected + ").as_deref()";
                    case RustType.Bytes ignored -> "(" + expected + ").as_deref()";
                    case RustType.VecOf ignored -> "(" + expected + ").as_deref()";
                    case RustType.Pairs ignored -> "(" + expected + ").as_deref()";
                    case RustType inner when RustMembers.isCopy(inner, context) -> expected;
                    default -> "(" + expected + ").as_ref()";
                };
                case RustType.Text ignored -> expected;
                case RustType.Bytes ignored -> "(" + expected + ").as_slice()";
                case RustType.VecOf ignored -> "(" + expected + ").as_slice()";
                case RustType.Pairs ignored -> "(" + expected + ").as_slice()";
                case RustType inner when RustMembers.isCopy(inner, context) -> expected;
                default -> "&" + expected;
            };
            return Optional.of(INDENT + "assert_eq!(" + actual + ", " + compared + ");\n");
        }

        private void sendSync(final String type) {
            test("can be sent to and shared between threads", INDENT + "crate::helpers::assert_send_sync::<" + type
                    + ">();\n");
        }

        // --------------------------------------------------------------------------------------------- enums

        private boolean enumTests(final TypeDefinition.EnumDefinition type) {
            final String name = imports.type(type.name());
            final String nameMethod = "name";
            final String valuesMethod = "values";
            if (!type.attributes().isEmpty() && !type.values().isEmpty()) {
                final StringBuilder body = new StringBuilder();
                for (final EnumValueDefinition value : type.values()) {
                    for (int i = 0; i < type.attributes().size() && i < value.arguments().size(); i++) {
                        final ParameterDefinition attribute = type.attributes().get(i);
                        body.append(INDENT).append("assert_eq!(").append(name).append("::")
                                .append(RustNames.variant(value.name())).append('.')
                                .append(RustNames.member(attribute.name())).append("(), ")
                                .append(RustLiterals.constant(value.arguments().get(i), attribute.type(), imports))
                                .append(");\n");
                    }
                }
                test("the constants have the attribute values of the specs", body.toString());
            }
            imports.externalTraitInScope("std::str::FromStr");
            final String loop = type.values().isEmpty() ? "" : INDENT + "for value in " + name + "::" + valuesMethod
                    + "() {\n" + INDENT + INDENT + "assert_eq!(" + name + "::from_str(value." + nameMethod
                    + "()).expect(\"known name\"), *value);\n" + INDENT + INDENT + "assert_eq!(value.to_string(), value."
                    + nameMethod + "());\n" + INDENT + "}\n";
            test("parses the name of every constant", loop + INDENT + "assert_eq!(" + name + "::" + valuesMethod
                    + "().len(), " + type.values().size() + ");\n" + INDENT + "assert!(" + name
                    + "::from_str(\"no constant\").is_err());\n");
            final Optional<EnumValueDefinition> first = type.values().stream()
                    .filter(v -> !v.hasAnnotation("deprecated")).findFirst()
                    .or(() -> type.values().stream().findFirst());
            RustTypeGenerator.inherentNames(type, context).forEach((method, rustName) -> {
                if (method.isStatic()) {
                    methodTest(method, rustName, name + "::" + rustName, Map.of(), Map.of());
                } else {
                    first.ifPresent(f -> methodTest(method, rustName, name + "::" + RustNames.variant(f.name()) + "."
                            + rustName, Map.of(), Map.of()));
                }
            });
            sendSync(name);
            return true;
        }

        // ---------------------------------------------------------------------------- traits and sealed enums

        private boolean traitTests(final TypeDefinition.ComplexTypeDefinition type) {
            final Optional<Map<Type.TypeVariable, Type>> variables = Constraints.defaults(type.typeParameters());
            if (variables.isEmpty()) {
                return false;
            }
            final Type.DeclaredType self = new Type.DeclaredType(type.name(), type.typeParameters().stream()
                    .map(p -> variables.get().get(p.variable())).toList());
            final String dyn = context.rustType(self, Map.of()).render(imports).replaceFirst("^.*?<dyn ", "dyn ")
                    .replaceFirst(">$", "");
            staticMethods(type, "<" + dyn + ">", variables.get(), context.implScope(type, self, Map.of()));
            sendSync(dyn);
            return true;
        }

        private boolean sealedTests(final TypeDefinition.ComplexTypeDefinition type) {
            final String name = imports.type(type.name());
            for (final QualifiedName variant : context.sealedVariants(type)) {
                if (!context.isGenerated(variant)) {
                    continue;
                }
                final Optional<String> value = samples.value(new Type.DeclaredType(variant, List.of()), List.of(), 0,
                        true);
                if (value.isEmpty()) {
                    notes.add("no value of " + variant.name() + " for the conversion to " + type.name().name());
                    continue;
                }
                final String variantType = context.rustType(new Type.DeclaredType(variant, List.of()), Map.of())
                        .render(imports);
                test("converts " + article(variant.name()), INDENT + "let value: " + variantType + " = " + value.get()
                        + ";\n" + INDENT
                        + "assert!(matches!(" + name + "::from(value), " + name + "::" + variant.name() + "(_)));\n");
            }
            staticMethods(type, name, Map.of(), Map.of());
            sendSync(name);
            return true;
        }

        private void staticMethods(final TypeDefinition.ComplexTypeDefinition type, final String owner,
                                   final Map<Type.TypeVariable, Type> variables,
                                   final Map<Type.TypeVariable, RustType> scope) {
            for (final MethodDefinition method : type.declaredMethods()) {
                if (method.isStatic()) {
                    methodTest(method, context.methodName(method), owner + "::" + context.methodName(method),
                            variables, scope);
                }
            }
        }

        // ------------------------------------------------------------------------------------------- methods

        /**
         * The test that a method or function can be called with valid arguments.
         *
         * @param method    the method or function
         * @param name      its Rust name
         * @param callee    the expression that is called, e.g. {@code create().sign}
         * @param variables the type arguments of the type
         */
        private void methodTest(final MethodDefinition method, final String name, final String callee,
                                final Map<Type.TypeVariable, Type> variables,
                                final Map<Type.TypeVariable, RustType> scope) {
            final Map<Type.TypeVariable, Type> all = new HashMap<>(variables);
            final Map<Type.TypeVariable, RustType> methodScope = new HashMap<>(scope);
            final List<String> typeArguments = new ArrayList<>();
            for (final TypeParameterDefinition parameter : method.typeParameters()) {
                final Optional<Type> argument = typeArgument(parameter);
                if (argument.isEmpty()) {
                    notes.add("no type arguments for `" + method.signature() + "`");
                    return;
                }
                all.put(parameter.variable(), argument.get());
                methodScope.put(parameter.variable(), context.rustType(argument.get(), Map.of()));
                typeArguments.add(context.rustType(argument.get(), Map.of()).render(imports));
            }
            final List<String> arguments = new ArrayList<>();
            for (final ParameterDefinition parameter : method.parameters()) {
                final Input input = input(parameter, all, methodScope);
                final Optional<String> helper = helper(input, 0, false);
                if (helper.isPresent()) {
                    arguments.add(helper.get() + "()");
                } else if (input.nullable()) {
                    arguments.add("None");
                } else if (input.varargs()) {
                    arguments.add("vec![]");
                } else {
                    notes.add("no valid arguments for `" + method.signature() + "`");
                    return;
                }
            }
            final String call = callee + (typeArguments.isEmpty() ? "" : "::<" + String.join(", ", typeArguments)
                    + ">") + "(" + String.join(", ", arguments) + ")";
            final String display = method.name() + "(" + method.parameters().stream().map(ParameterDefinition::name)
                    .collect(Collectors.joining(", ")) + ") can be called";
            if (method.hasAnnotation("async")) {
                test(display + " and returns a future", INDENT + "let _ = crate::helpers::block_on(" + call + ");\n");
            } else if (method.hasAnnotation("streaming")) {
                test(display + " and returns a stream", INDENT + "let _ = " + call + ";\n");
            } else if (method.returnType() instanceof Type.VoidType && !method.hasAnnotation("throws")) {
                test(display, INDENT + call + ";\n");
            } else {
                test(display, INDENT + "let _ = " + call + ";\n");
            }
        }

        /** The type argument of a type parameter of a method: a type that implements its bound. */
        private Optional<Type> typeArgument(final TypeParameterDefinition parameter) {
            if (parameter.bound() == null) {
                return Optional.of(Constraints.STRING);
            }
            if (!(parameter.bound() instanceof Type.DeclaredType bound) || !context.isTrait(bound.name())) {
                return Optional.of(parameter.bound());
            }
            return context.model().types().stream()
                    .filter(t -> context.isGenerated(t.name()) && t.typeParameters().isEmpty()
                            && !(t instanceof TypeDefinition.ComplexTypeDefinition c && c.abstraction())
                            && context.model().isSubtypeOf(t.name(), bound.name()) && visible(t.name().namespace()))
                    .map(t -> (Type) new Type.DeclaredType(t.name(), List.of())).findFirst();
        }

        private boolean visible(final String namespace) {
            final String folder = context.folder(this.namespace);
            return context.folder(namespace).equals(folder)
                    || SpecFolders.required(folder, context.folders()).contains(context.folder(namespace));
        }

        // ----------------------------------------------------------------------------------------- functions

        private void functionTests(final List<FunctionDefinition> functions) {
            for (final FunctionDefinition function : functions) {
                final MethodDefinition method = function.method();
                final String name = context.methodName(method);
                methodTest(method, name, imports.function(function.namespace(), name), Map.of(), Map.of());
            }
        }

        // -------------------------------------------------------------------------------------------- values

        private Input input(final FieldDefinition field, final Map<Type.TypeVariable, Type> variables,
                            final Map<Type.TypeVariable, RustType> scope,
                            final Map<Type.TypeVariable, RustType> declared) {
            return new Input(field.name(), LinkedModel.substitute(field.type(), variables), field.annotations(),
                    false, field.hasAnnotation("nullable"), RustMembers.storage(field, scope, context),
                    RustMembers.storage(field, declared, context));
        }

        private Input input(final ParameterDefinition parameter, final Map<Type.TypeVariable, Type> variables,
                            final Map<Type.TypeVariable, RustType> scope) {
            RustType rust = context.rustType(parameter.type(), scope);
            if (parameter.varargs()) {
                rust = new RustType.VecOf(rust);
            }
            if (parameter.hasAnnotation("nullable")) {
                rust = new RustType.Optional(rust);
            }
            return new Input(parameter.name(), LinkedModel.substitute(parameter.type(), variables),
                    parameter.annotations(), parameter.varargs(), parameter.hasAnnotation("nullable"), rust, rust);
        }

        /** The name of the helper function that returns a new valid value of the input (with Some and Vec). */
        private Optional<String> helper(final Input input, final int variant, final boolean stored) {
            final String key = input.name() + "|" + input.type().text() + "|" + input.annotations().stream()
                    .map(a -> a.name() + a.arguments().stream().map(Literal::text).toList())
                    .collect(Collectors.joining(",")) + "|" + variant + "|" + stored + "|" + input.varargs()
                    + "|" + input.nullable();
            if (helperNames.containsKey(key)) {
                return Optional.ofNullable(helperNames.get(key));
            }
            final Optional<String> sample = samples.value(input.type(), input.annotations(), variant, stored)
                    .flatMap(v -> RustTypeGenerator.convert(v, input.sampleType(context), input.elementType(), false,
                            imports));
            if (sample.isEmpty()) {
                helperNames.put(key, null);
                return Optional.empty();
            }
            String value = sample.get();
            if (input.varargs()) {
                value = "vec![" + value + "]";
            }
            if (input.nullable()) {
                value = "Some(" + value + ")";
            }
            final String declaration = input.rustType(context).render(imports);
            final String base = RustNames.snake(input.name()) + (variant == 0 ? "" : "_other") + "_value";
            final String body = value;
            final java.util.function.Function<String, String> render = n -> "fn " + n + "() -> " + declaration
                    + " {\n" + INDENT + body + "\n}\n";
            String name = base;
            for (int i = 2; helpers.containsKey(name) && !helpers.get(name).equals(render.apply(name)); i++) {
                name = base + i;
            }
            helperNames.put(key, name);
            helpers.put(name, render.apply(name));
            return Optional.of(name);
        }

        /** The boundary values of the validation annotations (and of integers without exact Rust type). */
        private List<Case> cases(final Input input) {
            final List<Case> cases = new ArrayList<>();
            final List<Annotation> annotations = input.annotations();
            if (input.type() instanceof Type.BasicType basic && !input.varargs()) {
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
            final boolean bounded = annotations.stream().anyMatch(a -> a.name().equals("min") || a.name().equals("max"));
            if (!bounded && !RustContext.needsRangeCheck(builtin)) {
                return; // the Rust type has exactly the values of the meta-language type
            }
            final IntegerRange range = Constraints.range(builtin, annotations);
            if (range.min().compareTo(range.max()) > 0) {
                return;
            }
            final IntegerRange representable = rustRange(builtin);
            cases.add(new Case("the minimum (" + range.min() + ")", literal(builtin, range.min()), true));
            cases.add(new Case("the maximum (" + range.max() + ")", literal(builtin, range.max()), true));
            final BigInteger below = range.min().subtract(BigInteger.ONE);
            final BigInteger above = range.max().add(BigInteger.ONE);
            if (representable.contains(below)) {
                cases.add(new Case("a value below the minimum (" + below + ")", literal(builtin, below), false));
            }
            if (representable.contains(above)) {
                cases.add(new Case("a value above the maximum (" + above + ")", literal(builtin, above), false));
            }
        }

        /** The values of the Rust integer type of a meta-language integer type. */
        private static IntegerRange rustRange(final BuiltinType builtin) {
            final String type = RustContext.integer(builtin);
            final int bits = type.startsWith("ethnum") ? 256 : Integer.parseInt(type.substring(1));
            final boolean unsigned = type.startsWith("u") || type.equals("ethnum::U256");
            return unsigned ? new IntegerRange(BigInteger.ZERO, BigInteger.TWO.pow(bits).subtract(BigInteger.ONE))
                    : new IntegerRange(BigInteger.TWO.pow(bits - 1).negate(),
                    BigInteger.TWO.pow(bits - 1).subtract(BigInteger.ONE));
        }

        private static String literal(final BuiltinType builtin, final BigInteger value) {
            return RustLiterals.number(new BigDecimal(value), builtin);
        }

        private void boundCases(final BuiltinType builtin, final List<Annotation> annotations,
                                final List<Case> cases) {
            for (final Annotation annotation : annotations) {
                final boolean min = annotation.name().equals("min");
                if (!min && !annotation.name().equals("max")) {
                    continue;
                }
                final BigDecimal bound = Constraints.bound(annotations, annotation.name()).orElseThrow();
                final String accepted = RustLiterals.number(bound, builtin);
                final String rejected;
                switch (builtin.category()) {
                    case FLOAT -> rejected = Double.toString(min ? Math.nextDown(bound.doubleValue())
                            : Math.nextUp(bound.doubleValue()));
                    case DECIMAL -> {
                        final BigDecimal step = new BigDecimal("0.001");
                        rejected = RustLiterals.number(min ? bound.subtract(step) : bound.add(step), builtin);
                    }
                    default -> {
                        final long millis = builtin.name().equals("seconds")
                                ? bound.multiply(BigDecimal.valueOf(1000)).longValueExact() : bound.longValueExact();
                        final long other = min ? millis - 1 : millis + 1;
                        rejected = other < 0 ? null : "std::time::Duration::from_millis(" + other + ")";
                    }
                }
                cases.add(new Case(min ? "the minimum (" + bound.toPlainString() + ")"
                        : "the maximum (" + bound.toPlainString() + ")", accepted, true));
                if (rejected != null) {
                    cases.add(new Case(min ? "a value below the minimum" : "a value above the maximum", rejected,
                            false));
                }
            }
        }

        private void stringCases(final List<Annotation> annotations, final List<Case> cases) {
            final Optional<Integer> minLength = Constraints.size(annotations, "minLength");
            final Optional<Integer> maxLength = Constraints.size(annotations, "maxLength");
            if (minLength.isPresent()) {
                final int length = minLength.get();
                Constraints.string(sized(annotations, "minLength", "maxLength", length), 0)
                        .ifPresent(s -> cases.add(new Case("a value with the minimum length", string(s), true)));
                if (length > 0) {
                    cases.add(new Case("a value shorter than the minimum length", string("a".repeat(length - 1)),
                            false));
                }
            }
            if (maxLength.isPresent()) {
                final int length = maxLength.get();
                Constraints.string(sized(annotations, "minLength", "maxLength", length), 0)
                        .ifPresent(s -> cases.add(new Case("a value with the maximum length", string(s), true)));
                cases.add(new Case("a value longer than the maximum length", string("a".repeat(length + 1)), false));
            }
            Constraints.stringArgument(annotations, "pattern").flatMap(RegexSamples::rejected)
                    .filter(s -> !Constraints.isValidString(s, annotations))
                    .ifPresent(s -> cases.add(new Case("a value that does not match the pattern", string(s), false)));
            if (annotations.stream().anyMatch(a -> a.name().equals("urlPattern"))) {
                cases.add(new Case("a value that is no URL", string("not a url"), false));
            }
        }

        private static String string(final String value) {
            return RustLiterals.quote(value) + ".to_string()";
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

        private void test(final String description, final String body) {
            final String base = functionName(description);
            String name = base;
            for (int i = 2; !testNames.add(name); i++) {
                name = base + "_" + i;
            }
            tests.add(new Test(name, description, body, null));
        }

        /** The name of a test function: the description in snake case. */
        static String functionName(final String description) {
            String name = RustNames.snake(description.replaceAll("[^A-Za-z0-9]+", " ").strip()
                    .replaceAll("\\s+", "_")).toLowerCase(Locale.ROOT).replaceAll("_+", "_");
            if (name.isEmpty() || Character.isDigit(name.charAt(0))) {
                name = "test_" + name;
            }
            return RustNames.identifier(name);
        }

        private static String article(final String name) {
            return ("AEIOU".indexOf(name.charAt(0)) >= 0 ? "an " : "a ") + name;
        }

        private GeneratedFile render(final String fileName) {
            final List<Test> all = new ArrayList<>(tests);
            int index = 1;
            for (final String note : notes.stream().distinct().toList()) {
                String name = "not_generated_" + index++;
                while (!testNames.add(name)) {
                    name = "not_generated_" + index++;
                }
                all.add(new Test(name, "not generated: " + note, "", note));
            }
            final StringBuilder code = new StringBuilder();
            for (final Test test : all) {
                code.append('\n').append("/// ").append(RustDoc.escape(test.description())).append('\n')
                        .append("#[test]\n");
                if (test.ignored() != null) {
                    code.append("#[ignore = ").append(RustLiterals.quote("not generated: " + test.ignored()))
                            .append("]\n");
                }
                code.append("fn ").append(test.name()).append("() {\n").append(test.body()).append("}\n");
            }
            // only the helpers that the tests (or other used helpers) use
            final List<String> used = new ArrayList<>();
            boolean changed = true;
            while (changed) {
                changed = false;
                final String text = code + String.join("", used);
                for (final Map.Entry<String, String> helper : helpers.entrySet()) {
                    if (!used.contains(helper.getValue()) && Pattern.compile("(?<![\\w.:])"
                            + Pattern.quote(helper.getKey()) + "\\(").matcher(text).find()) {
                        used.add(helper.getValue());
                        changed = true;
                    }
                }
                for (final Map.Entry<String, String> testDouble : samples.doubles().entrySet()) {
                    if (!used.contains(testDouble.getValue()) && Pattern.compile("\\b"
                            + Pattern.quote(testDouble.getKey()) + "\\b").matcher(text).find()) {
                        used.add(testDouble.getValue());
                        changed = true;
                    }
                }
            }
            helpers.values().stream().filter(used::contains).forEach(h -> code.append('\n').append(h));
            samples.doubles().values().stream().filter(used::contains)
                    .forEach(d -> code.append('\n').append(d.substring(d.indexOf('\n') + 1)));
            final StringBuilder rs = new StringBuilder(RustGenerator.HEADER);
            if (!notes.isEmpty()) {
                rs.append("//\n// Not generated:\n");
                notes.stream().distinct().forEach(n -> rs.append(NOTE).append(n).append('\n'));
            }
            rs.append('\n');
            final String block = imports.render(code.toString());
            if (!block.isEmpty()) {
                rs.append(block);
            }
            rs.append(code);
            return new GeneratedFile(directory + "/" + fileName, rs.toString());
        }
    }
}
