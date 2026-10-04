package org.hiero.sdk.v3.metalang.generator.java;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.generator.GenerationException;
import org.hiero.sdk.v3.metalang.model.FieldDefinition;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.hiero.sdk.v3.metalang.model.TypeDefinition;
import org.hiero.sdk.v3.metalang.tck.Binding;
import org.hiero.sdk.v3.metalang.tck.TckBindings;
import org.hiero.sdk.v3.metalang.tck.TckSpecifications;
import org.jspecify.annotations.Nullable;

/**
 * Generates the Java TCK server (see {@code tck-binding.md}) as two Maven projects:
 * <ul>
 *   <li>{@code contract}: the contract with the hand-written runtime ({@link JavaTckContractGenerator}), derived from
 *       the converter catalogue;</li>
 *   <li>{@code server}: one class per bindings file whose methods build the request from the JSON parameters, execute
 *       it and convert the receipt or response into the JSON result, plus the main class. It is compiled against the
 *       generated API and the contract only; the runtime ({@code tck/runtime/java}, artifact
 *       {@value #RUNTIME_ARTIFACT}) implements the contract, is found with the {@code ServiceLoader} and is a runtime
 *       dependency.</li>
 * </ul>
 * Every method of a binding is derived from the binding and the spec model only.
 */
public final class JavaTckGenerator {

    /** The package of the generated TCK server. */
    static final String PACKAGE = "org.hiero.tck.server";

    /** The Maven artifact of the runtime. */
    static final String RUNTIME_ARTIFACT = "hiero-sdk-tck-runtime";

    /** The directory of the server project in the output. */
    static final String DIRECTORY = "server";

    /** The classes of the contract the generated server uses. */
    private static final List<String> CONTRACT_CLASSES = List.of("Converters", "Handler", "Session", "Source",
            "TckRuntime", "TckServer");

    private static final String INDENT = "    ";
    private static final String SOURCE_ROOT = DIRECTORY + "/src/main/java/" + PACKAGE.replace('.', '/') + "/";

    private final JavaGenerator generator;

    /**
     * Creates a generator.
     *
     * @param config the configuration of the generated API (group ID, version)
     */
    public JavaTckGenerator(final JavaGeneratorConfig config) {
        this.generator = new JavaGenerator(Objects.requireNonNull(config, "config must not be null"));
    }

    /**
     * Generates the contract and the TCK server.
     *
     * @param model    the linked model of the specs
     * @param bindings the resolved bindings (must be free of errors)
     * @return the generated files, sorted by path ({@code contract/...} and {@code server/...})
     * @throws GenerationException if a bound type is not part of the generated Java API
     */
    public List<GeneratedFile> generate(final LinkedModel model, final TckBindings.Bindings bindings) {
        Objects.requireNonNull(model, "model must not be null");
        Objects.requireNonNull(bindings, "bindings must not be null");
        if (bindings.hasErrors()) {
            throw new GenerationException(bindings.diagnostics().stream().map(Object::toString).toList());
        }
        final JavaContext context = generator.context(model);
        final List<String> artifactIds = generator.artifactIds(model);
        final List<String> problems = new ArrayList<>();
        final List<GeneratedFile> files = new ArrayList<>();
        try {
            files.addAll(JavaTckContractGenerator.generate(context, artifactIds, generator.config()));
        } catch (final JavaTypes.UnsupportedTypeException | IllegalStateException e) {
            problems.add("contract: " + e.getMessage());
        }
        final List<String> classes = new ArrayList<>();
        bindings.bindings().stream().collect(Collectors.groupingBy(TckBindings.Resolved::file,
                java.util.TreeMap::new, Collectors.toList())).forEach((file, resolved) -> {
                    final String className = className(file);
                    classes.add(className);
                    try {
                        files.add(bindingsClass(className, file, resolved, context));
                    } catch (final JavaTypes.UnsupportedTypeException | IllegalStateException e) {
                        problems.add(file + ": " + e.getMessage());
                    }
                });
        try {
            files.add(commonClass(bindings.common(), context));
        } catch (final JavaTypes.UnsupportedTypeException | IllegalStateException e) {
            problems.add("common transaction parameters: " + e.getMessage());
        }
        if (!problems.isEmpty()) {
            throw new GenerationException(problems);
        }
        files.add(mainClass(classes));
        files.add(pom(DIRECTORY, "hiero-sdk-tck", "Hiero SDK TCK server",
                "The JSON-RPC server of the Hiero SDK TCK for the Java API.", artifactIds,
                List.of(JavaTckContractGenerator.ARTIFACT), generator.config(), true));
        return files.stream().sorted().toList();
    }

    /**
     * Returns the class of a bindings file: {@code crypto-service.md} becomes {@code CryptoServiceBindings}.
     *
     * @param file the bindings file
     * @return the simple class name
     */
    static String className(final String file) {
        final String base = file.substring(file.lastIndexOf('/') + 1).replaceFirst("\\.md$", "");
        final StringBuilder name = new StringBuilder();
        for (final String part : base.split("[^A-Za-z0-9]+")) {
            if (!part.isEmpty()) {
                name.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
        }
        return name + "Bindings";
    }

    // --- bindings classes ----------------------------------------------------------------------------

    private static GeneratedFile bindingsClass(final String className, final String file,
                                               final List<TckBindings.Resolved> bindings, final JavaContext context) {
        final Imports imports = context.imports(PACKAGE, className);
        imports.use("java.util", "Map");
        final List<String> methods = new ArrayList<>();
        for (final TckBindings.Resolved binding : bindings) {
            methods.add(method(binding, imports, context));
        }
        final StringBuilder register = new StringBuilder()
                .append(INDENT).append("/// Registers the methods.\n")
                .append(INDENT).append("///\n")
                .append(INDENT).append("/// @param methods the methods by name\n")
                .append(INDENT).append("void register(final Map<String, Handler> methods) {\n");
        for (final TckBindings.Resolved binding : bindings) {
            register.append(INDENT).append(INDENT).append("methods.put(")
                    .append(JavaLiterals.quote(binding.binding().name())).append(", this::")
                    .append(binding.binding().name()).append(");\n");
        }
        register.append(INDENT).append("}\n");
        final String members = fields(className, true) + register + methods.stream().map(m -> "\n" + m)
                .collect(Collectors.joining());
        contractImports(members, imports);
        final StringBuilder java = new StringBuilder(JavaGenerator.HEADER).append('\n')
                .append("package ").append(PACKAGE).append(";\n\n")
                .append(imports.render()).append('\n')
                .append("/// The TCK methods of the bindings file `").append(file).append("`.\n")
                .append("final class ").append(className).append(" {\n\n")
                .append(members)
                .append("}\n");
        return new GeneratedFile(SOURCE_ROOT + className + ".java", java.toString());
    }

    /** The fields and the constructor: the runtime, its converters and (for the bindings) the common parameters. */
    private static String fields(final String className, final boolean common) {
        final StringBuilder java = new StringBuilder()
                .append(INDENT).append("private final TckRuntime runtime;\n")
                .append(INDENT).append("private final Converters convert;\n");
        if (common) {
            java.append(INDENT).append("private final CommonBindings common;\n");
        }
        java.append('\n')
                .append(INDENT).append("/// Creates the methods.\n")
                .append(INDENT).append("///\n")
                .append(INDENT).append("/// @param runtime the runtime\n");
        if (common) {
            java.append(INDENT).append("/// @param common  the common transaction parameters\n");
        }
        java.append(INDENT).append(className).append("(final TckRuntime runtime")
                .append(common ? ", final CommonBindings common" : "").append(") {\n")
                .append(INDENT).append(INDENT).append("this.runtime = runtime;\n")
                .append(INDENT).append(INDENT).append("this.convert = runtime.converters();\n");
        if (common) {
            java.append(INDENT).append(INDENT).append("this.common = common;\n");
        }
        return java.append(INDENT).append("}\n\n").toString();
    }

    private static String method(final TckBindings.Resolved binding, final Imports imports,
                                 final JavaContext context) {
        final String indent = INDENT + INDENT;
        final StringBuilder java = new StringBuilder();
        documentation(binding.binding().documentation(), java);
        java.append(INDENT).append("Object ").append(binding.binding().name())
                .append("(final Map<String, Object> params, final Session session) {\n");
        final StringBuilder body = new StringBuilder();
        rejectUnsupported(binding.members(), "params", body, indent);
        final String request = construct(binding.type(), binding.members(), "params", null, 1, body, indent,
                imports, context);
        final boolean transaction = binding.kind() == TckBindings.Kind.TRANSACTION;
        final String variable = transaction ? "transaction" : "query";
        body.append(indent).append("final var ").append(variable).append(" = ").append(request).append(";\n");
        final String value = transaction ? "receipt" : "response";
        if (transaction) {
            body.append(indent).append("final Map<String, Object> commonParams = runtime.object(params, ")
                    .append(JavaLiterals.quote(TckSpecifications.COMMON)).append(");\n")
                    .append(indent).append("common.apply(transaction, commonParams);\n")
                    .append(indent)
                    .append("final var receipt = runtime.transaction(session, transaction, commonParams);\n");
        } else {
            body.append(indent).append("final var response = runtime.query(session, query);\n");
        }
        imports.use("java.util", "LinkedHashMap");
        body.append(indent).append("final Map<String, Object> result = new LinkedHashMap<>();\n");
        for (final TckBindings.Member member : binding.members()) {
            if (member instanceof TckBindings.Result result) {
                body.append(indent).append("runtime.put(result, ").append(JavaLiterals.quote(result.name()
                        .toString())).append(", ").append(access(value, result.fields(), imports))
                        .append(", convert::").append(result.converter().id()).append("Json);\n");
            } else if (member instanceof TckBindings.Unsupported(final Binding.Unsupported unsupported)
                    && unsupported.result()) {
                body.append(indent).append("// not provided: ").append(unsupported.name()).append(" (")
                        .append(unsupported.reason()).append(")\n");
            }
        }
        body.append(indent).append("return result;\n");
        java.append(body).append(INDENT).append("}\n");
        return java.toString();
    }

    /** The JSON parameters the API cannot provide are rejected if they are sent. */
    private static void rejectUnsupported(final List<TckBindings.Member> members, final String json,
                                          final StringBuilder body, final String indent) {
        for (final TckBindings.Member member : members) {
            if (member instanceof TckBindings.Unsupported(final Binding.Unsupported unsupported)
                    && !unsupported.result()) {
                body.append(indent).append("runtime.unsupported(").append(json).append(", ")
                        .append(JavaLiterals.quote(unsupported.name())).append(", ")
                        .append(JavaLiterals.quote(unsupported.reason())).append(");\n");
            }
        }
    }

    /**
     * Returns the constructor call of a type from the bound values; lists of {@code each} are built by statements
     * appended to {@code body} first.
     */
    private static String construct(final TypeDefinition type, final List<TckBindings.Member> members,
                                    final String json, final @Nullable String parent, final int depth,
                                    final StringBuilder body, final String indent, final Imports imports,
                                    final JavaContext context) {
        if (!context.isGenerated(type.name())) {
            throw new IllegalStateException("Type '" + type.name() + "' is not part of the generated Java API");
        }
        final List<FieldDefinition> parameters = context.isClass(type.name())
                ? ClassGenerator.constructorParameters(type.fields()) : type.fields();
        final List<String> arguments = new ArrayList<>();
        for (final FieldDefinition field : parameters) {
            final Optional<TckBindings.Member> member = members.stream().filter(m -> field(m)
                    .filter(f -> f.name().equals(field.name())).isPresent()).findFirst();
            if (member.isEmpty()) {
                arguments.add(unbound(type, field, imports, context));
            } else if (member.get() instanceof TckBindings.Value value) {
                arguments.add(value(field, value, json, parent, imports, context));
            } else {
                arguments.add(elements(field, (TckBindings.Elements) member.get(), json, depth, body, indent,
                        imports, context));
            }
        }
        final String name = imports.use(JavaNames.packageName(type.name().namespace()), type.name().name());
        final String diamond = type.typeParameters().isEmpty() ? "" : "<>";
        if (arguments.size() <= 1) {
            return "new " + name + diamond + "(" + String.join(", ", arguments) + ")";
        }
        final String argumentIndent = "\n" + indent + INDENT + INDENT;
        return "new " + name + diamond + "(" + argumentIndent + String.join("," + argumentIndent, arguments) + ")";
    }

    private static Optional<FieldDefinition> field(final TckBindings.Member member) {
        return switch (member) {
            case TckBindings.Value value -> Optional.of(value.field());
            case TckBindings.Elements elements -> Optional.of(elements.field());
            default -> Optional.empty();
        };
    }

    private static String unbound(final TypeDefinition type, final FieldDefinition field, final Imports imports,
                                  final JavaContext context) {
        if (field.hasAnnotation("default")) {
            return defaultValue(field, imports, context);
        }
        if (field.hasAnnotation("nullable")) {
            return "null";
        }
        throw new IllegalStateException("Required attribute '" + field.name() + "' of '" + type.name()
                + "' is not bound");
    }

    private static String defaultValue(final FieldDefinition field, final Imports imports,
                                       final JavaContext context) {
        return JavaLiterals.expression(field.annotation("default").orElseThrow().arguments().getFirst(),
                field.type(), imports, context);
    }

    /** A bound value: the first present source, then the default, {@code null} or the check that it is present. */
    private static String value(final FieldDefinition field, final TckBindings.Value value, final String json,
                                final @Nullable String parent, final Imports imports, final JavaContext context) {
        final String lookup = lookup(value.sources(), json, parent, imports);
        if (field.hasAnnotation("default")) {
            return "runtime.or(" + lookup + ", " + defaultValue(field, imports, context) + ")";
        }
        if (field.hasAnnotation("nullable")) {
            return lookup;
        }
        return "runtime.required(" + lookup + ", " + JavaLiterals.quote(value.sources().stream()
                .map(s -> s.path().toString()).collect(Collectors.joining(" | "))) + ")";
    }

    /** The lookup of the sources: one {@code runtime.value} per JSON object, the first present one wins. */
    private static String lookup(final List<TckBindings.Source> sources, final String json,
                                 final @Nullable String parent,
                                 final Imports imports) {
        imports.use("java.util", "List");
        final List<String> groups = new ArrayList<>();
        String currentObject = null;
        List<String> current = new ArrayList<>();
        for (final TckBindings.Source source : sources) {
            final String object = source.path().parent() ? Objects.requireNonNull(parent,
                    "'^' outside of 'each'") : json;
            if (!object.equals(currentObject) && !current.isEmpty()) {
                groups.add("runtime.value(" + currentObject + ", List.of(" + String.join(", ", current) + "))");
                current = new ArrayList<>();
            }
            currentObject = object;
            current.add("new Source<>(" + JavaLiterals.quote(String.join(".", source.path().segments()))
                    + ", convert::" + source.converter().id() + ")");
        }
        groups.add("runtime.value(" + currentObject + ", List.of(" + String.join(", ", current) + "))");
        String lookup = groups.getLast();
        for (int i = groups.size() - 2; i >= 0; i--) {
            lookup = "runtime.or(" + groups.get(i) + ", " + lookup + ")";
        }
        return lookup;
    }

    /**
     * A list built with {@code each}: the first segment of the source is the JSON list, the rest the object inside
     * each element ({@code transfers.hbar}); elements without that object are skipped.
     */
    private static String elements(final FieldDefinition field, final TckBindings.Elements elements,
                                   final String json, final int depth, final StringBuilder body, final String indent,
                                   final Imports imports, final JavaContext context) {
        final String list = JavaKeywords.identifier(field.name()) + (depth > 1 ? String.valueOf(depth) : "");
        final String element = "element" + depth;
        final String item = "item" + depth;
        final String elementType = imports.use(JavaNames.packageName(elements.elementType().name().namespace()),
                elements.elementType().name().name());
        imports.use("java.util", "List");
        imports.use("java.util", "ArrayList");
        body.append(indent).append("final List<").append(elementType).append("> ").append(list)
                .append(" = new ArrayList<>();\n")
                .append(indent).append("for (final Map<String, Object> ").append(element)
                .append(" : runtime.objects(").append(json).append(", ")
                .append(JavaLiterals.quote(elements.source().first())).append(")) {\n");
        final String inner = indent + INDENT;
        if (elements.source().segments().size() == 1) {
            body.append(inner).append("final Map<String, Object> ").append(item).append(" = ").append(element)
                    .append(";\n");
        } else {
            body.append(inner).append("final Map<String, Object> ").append(item).append(" = runtime.object(")
                    .append(element).append(", ").append(JavaLiterals.quote(elements.source().rest().toString()))
                    .append(");\n")
                    .append(inner).append("if (").append(item).append(" == null) {\n")
                    .append(inner).append(INDENT).append("continue;\n")
                    .append(inner).append("}\n");
        }
        rejectUnsupported(elements.members(), item, body, inner);
        final String construction = construct(elements.elementType(), elements.members(), item, element, depth + 1,
                body, inner, imports, context);
        body.append(inner).append(list).append(".add(").append(construction).append(");\n")
                .append(indent).append("}\n");
        return list;
    }

    /** The access to a result value: accessors, null-safe after a nullable attribute. */
    private static String access(final String root, final List<FieldDefinition> fields, final Imports imports) {
        final StringBuilder java = new StringBuilder(root);
        boolean optional = false;
        for (int i = 0; i < fields.size(); i++) {
            final String accessor = JavaMembers.accessor(fields.get(i).name(), false) + "()";
            if (optional) {
                java.append(".map(v -> v.").append(accessor).append(')');
            } else {
                java.append('.').append(accessor);
            }
            if (!optional && i < fields.size() - 1 && fields.get(i).hasAnnotation("nullable")) {
                java.insert(0, imports.use("java.util", "Optional") + ".ofNullable(").append(')');
                optional = true;
            }
        }
        return optional ? java + ".orElse(null)" : java.toString();
    }

    // --- common transaction parameters ---------------------------------------------------------------

    private static GeneratedFile commonClass(final Optional<TckBindings.Resolved> common, final JavaContext context) {
        final Imports imports = context.imports(PACKAGE, "CommonBindings");
        imports.use("java.util", "Map");
        imports.use("org.jspecify.annotations", "Nullable");
        final String transaction = imports.use(JavaNames.packageName("consensusnode.transactions"),
                "Transaction");
        final String indent = INDENT + INDENT;
        final StringBuilder body = new StringBuilder();
        if (common.isPresent()) {
            documentationOf(common.get(), body);
            rejectUnsupported(common.get().members(), "params", body, indent);
            for (final TckBindings.Member member : common.get().members()) {
                if (member instanceof TckBindings.Value value) {
                    final String name = JavaKeywords.identifier(value.field().name());
                    body.append(indent).append("final var ").append(name).append(" = ")
                            .append(lookup(value.sources(), "params", null, imports)).append(";\n")
                            .append(indent).append("if (").append(name).append(" != null) {\n")
                            .append(indent).append(INDENT).append("transaction.")
                            .append(InterfaceGenerator.setter(value.field().name())).append('(').append(name)
                            .append(");\n")
                            .append(indent).append("}\n");
                }
            }
        }
        final String members = fields("CommonBindings", false)
                + INDENT + "/// Applies the common transaction parameters to a transaction.\n"
                + INDENT + "///\n"
                + INDENT + "/// @param transaction the transaction\n"
                + INDENT + "/// @param params      the parameters, `null` if none are sent\n"
                + INDENT + "void apply(final " + transaction
                + "<?, ?> transaction, final @Nullable Map<String, Object> params) {\n"
                + indent + "if (params == null) {\n"
                + indent + INDENT + "return;\n"
                + indent + "}\n"
                + body
                + INDENT + "}\n";
        contractImports(members, imports);
        final String java = JavaGenerator.HEADER + '\n'
                + "package " + PACKAGE + ";\n\n"
                + imports.render() + '\n'
                + "/// The common transaction parameters (`" + TckSpecifications.COMMON + "`) of all transactions.\n"
                + "final class CommonBindings {\n\n"
                + members
                + "}\n";
        return new GeneratedFile(SOURCE_ROOT + "CommonBindings.java", java);
    }

    private static void documentationOf(final TckBindings.Resolved common, final StringBuilder body) {
        for (final String line : common.binding().documentation().lines().toList()) {
            body.append(INDENT).append(INDENT).append("// ").append(line).append('\n');
        }
    }

    private static void documentation(final String documentation, final StringBuilder java) {
        for (final String line : documentation.lines().toList()) {
            java.append(INDENT).append("/// ").append(line).append('\n');
        }
    }

    /** Imports the contract classes that the generated code uses; a name clash cannot be generated. */
    private static void contractImports(final String code, final Imports imports) {
        for (final String name : CONTRACT_CLASSES) {
            if (code.matches("(?s).*\\b" + name + "\\b.*")
                    && !imports.use(JavaTckContractGenerator.PACKAGE, name).equals(name)) {
                throw new IllegalStateException("The contract class " + name + " clashes with a type of the API");
            }
        }
    }

    // --- main class and build ------------------------------------------------------------------------

    private static GeneratedFile mainClass(final List<String> classes) {
        final StringBuilder java = new StringBuilder(JavaGenerator.HEADER).append('\n')
                .append("package ").append(PACKAGE).append(";\n\n")
                .append("import java.io.IOException;\n")
                .append("import java.util.LinkedHashMap;\n")
                .append("import java.util.Map;\n")
                .append("import java.util.ServiceLoader;\n")
                .append("import ").append(JavaTckContractGenerator.PACKAGE).append(".Handler;\n")
                .append("import ").append(JavaTckContractGenerator.PACKAGE).append(".TckRuntime;\n")
                .append("import ").append(JavaTckContractGenerator.PACKAGE).append(".TckServer;\n\n")
                .append("/// Starts the TCK server: `java -jar hiero-sdk-tck.jar [port]` (default 8544).\n")
                .append("public final class TckMain {\n\n")
                .append(INDENT).append("private TckMain() {\n")
                .append(INDENT).append("}\n\n")
                .append(INDENT).append("/// Creates the server with all methods and the runtime on the class path.\n")
                .append(INDENT).append("///\n")
                .append(INDENT).append("/// @return the server, not started\n")
                .append(INDENT).append("/// @throws IllegalStateException if there is no runtime on the class path\n")
                .append(INDENT).append("public static TckServer server() {\n")
                .append(INDENT).append(INDENT).append("final TckRuntime runtime = ServiceLoader.load(TckRuntime.class, ")
                .append("TckMain.class.getClassLoader())\n")
                .append(INDENT).append(INDENT).append(INDENT).append(INDENT)
                .append(".findFirst().orElseThrow(() -> new IllegalStateException(\"No \" + ")
                .append("TckRuntime.class.getName()\n")
                .append(INDENT).append(INDENT).append(INDENT).append(INDENT).append(INDENT).append(INDENT)
                .append("+ \" on the class path\"));\n")
                .append(INDENT).append(INDENT).append("final CommonBindings common = new CommonBindings(runtime);\n")
                .append(INDENT).append(INDENT)
                .append("final Map<String, Handler> methods = new LinkedHashMap<>();\n");
        classes.forEach(c -> java.append(INDENT).append(INDENT).append("new ").append(c)
                .append("(runtime, common).register(methods);\n"));
        java.append(INDENT).append(INDENT).append("return runtime.server(methods);\n")
                .append(INDENT).append("}\n\n")
                .append(INDENT).append("/// Starts the server.\n")
                .append(INDENT).append("///\n")
                .append(INDENT).append("/// @param args the port (optional)\n")
                .append(INDENT).append("/// @throws IOException if the server cannot be started\n")
                .append(INDENT).append("public static void main(final String[] args) throws IOException {\n")
                .append(INDENT).append(INDENT).append("final int port = server().start(args.length > 0 ")
                .append("? Integer.parseInt(args[0]) : 8544);\n")
                .append(INDENT).append(INDENT)
                .append("System.out.println(\"TCK server listening on port \" + port);\n")
                .append(INDENT).append("}\n")
                .append("}\n");
        return new GeneratedFile(SOURCE_ROOT + "TckMain.java", java.toString());
    }

    /**
     * Creates the pom of a project that depends on the generated API.
     *
     * @param directory   the directory of the project
     * @param artifactId  the artifact
     * @param name        the display name
     * @param description the description
     * @param artifactIds the artifacts of the generated API
     * @param more        further artifacts of the same group
     * @param config      the configuration of the generated API
     * @param server      whether the project is the executable server (runtime dependency, manifest, libraries)
     * @return the pom
     */
    static GeneratedFile pom(final String directory, final String artifactId, final String name,
                             final String description, final List<String> artifactIds, final List<String> more,
                             final JavaGeneratorConfig config, final boolean server) {
        final StringBuilder xml = new StringBuilder("<!-- ").append(JavaGenerator.MARKER).append(" -->\n")
                .append("<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n")
                .append("         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n")
                .append("         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 ")
                .append("https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n")
                .append("    <modelVersion>4.0.0</modelVersion>\n\n")
                .append("    <groupId>").append(config.groupId()).append("</groupId>\n")
                .append("    <artifactId>").append(artifactId).append("</artifactId>\n")
                .append("    <version>").append(config.version()).append("</version>\n\n")
                .append("    <name>").append(name).append("</name>\n")
                .append("    <description>").append(description).append("</description>\n\n")
                .append("    <properties>\n")
                .append("        <maven.compiler.release>25</maven.compiler.release>\n")
                .append("        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n")
                .append("    </properties>\n\n")
                .append("    <dependencies>\n");
        for (final String dependency : java.util.stream.Stream.concat(more.stream(), artifactIds.stream()).toList()) {
            xml.append(dependency(config.groupId(), dependency, "${project.version}", null));
        }
        xml.append(dependency("org.jspecify", "jspecify", MavenGenerator.JSPECIFY_VERSION, null));
        if (server) {
            xml.append("        <!-- the implementation of the contract, found with the ServiceLoader: the server is ")
                    .append("compiled\n             without it -->\n")
                    .append(dependency(config.groupId(), RUNTIME_ARTIFACT, "${project.version}", "runtime"));
        }
        xml.append("    </dependencies>\n\n")
                .append("    <build>\n")
                .append("        <plugins>\n")
                .append("            <plugin>\n")
                .append("                <groupId>org.apache.maven.plugins</groupId>\n")
                .append("                <artifactId>maven-compiler-plugin</artifactId>\n")
                .append("                <version>3.14.0</version>\n")
                .append("                <configuration>\n")
                .append("                    <compilerArgs>\n")
                .append("                        <arg>-Xlint:all</arg>\n")
                .append("                        <arg>-Werror</arg>\n")
                .append("                    </compilerArgs>\n")
                .append("                </configuration>\n")
                .append("            </plugin>\n");
        if (server) {
            xml.append("            <plugin>\n")
                    .append("                <groupId>org.apache.maven.plugins</groupId>\n")
                    .append("                <artifactId>maven-jar-plugin</artifactId>\n")
                    .append("                <version>3.4.2</version>\n")
                    .append("                <configuration>\n")
                    .append("                    <archive>\n")
                    .append("                        <manifest>\n")
                    .append("                            <mainClass>").append(PACKAGE).append(".TckMain</mainClass>\n")
                    .append("                            <addClasspath>true</addClasspath>\n")
                    .append("                            <classpathPrefix>lib/</classpathPrefix>\n")
                    .append("                        </manifest>\n")
                    .append("                    </archive>\n")
                    .append("                </configuration>\n")
                    .append("            </plugin>\n")
                    .append("            <!-- the dependencies next to the jar, so that java -jar finds them -->\n")
                    .append("            <plugin>\n")
                    .append("                <groupId>org.apache.maven.plugins</groupId>\n")
                    .append("                <artifactId>maven-dependency-plugin</artifactId>\n")
                    .append("                <version>3.8.1</version>\n")
                    .append("                <executions>\n")
                    .append("                    <execution>\n")
                    .append("                        <phase>package</phase>\n")
                    .append("                        <goals>\n")
                    .append("                            <goal>copy-dependencies</goal>\n")
                    .append("                        </goals>\n")
                    .append("                        <configuration>\n")
                    .append("                            <includeScope>runtime</includeScope>\n")
                    .append("                            <outputDirectory>${project.build.directory}/lib")
                    .append("</outputDirectory>\n")
                    .append("                        </configuration>\n")
                    .append("                    </execution>\n")
                    .append("                </executions>\n")
                    .append("            </plugin>\n");
        }
        xml.append("        </plugins>\n")
                .append("    </build>\n")
                .append("</project>\n");
        return new GeneratedFile(directory + "/pom.xml", xml.toString());
    }

    private static String dependency(final String groupId, final String artifactId, final String version,
                                     final @Nullable String scope) {
        return "        <dependency>\n"
                + "            <groupId>" + groupId + "</groupId>\n"
                + "            <artifactId>" + artifactId + "</artifactId>\n"
                + "            <version>" + version + "</version>\n"
                + (scope == null ? "" : "            <scope>" + scope + "</scope>\n")
                + "        </dependency>\n";
    }
}
