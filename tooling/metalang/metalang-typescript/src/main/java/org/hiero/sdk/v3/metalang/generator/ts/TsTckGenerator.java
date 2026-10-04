package org.hiero.sdk.v3.metalang.generator.ts;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.generator.GenerationException;
import org.hiero.sdk.v3.metalang.model.FieldDefinition;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.hiero.sdk.v3.metalang.model.QualifiedName;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.model.TypeDefinition;
import org.hiero.sdk.v3.metalang.tck.Binding;
import org.hiero.sdk.v3.metalang.tck.Converter;
import org.hiero.sdk.v3.metalang.tck.TckBindings;
import org.hiero.sdk.v3.metalang.tck.TckSpecifications;
import org.jspecify.annotations.Nullable;

/**
 * Generates the TypeScript TCK server (see {@code tck-binding.md} and ADR-0007) as two npm packages:
 * <ul>
 *   <li>{@code contract} ({@code <scope>/tck-contract}): the contract with the hand-written runtime — interfaces and
 *       types only. {@code Converters} is derived from the converter catalogue;</li>
 *   <li>{@code server} ({@code <scope>/tck-server}): one class per bindings file whose methods build the request
 *       from the JSON parameters, execute it and convert the receipt or response into the JSON result. It is compiled
 *       against the generated API and the contract only; the runtime ({@code tck/runtime/ts}, package
 *       {@code <scope>/tck-runtime}) implements the contract and is loaded with a dynamic {@code import} at
 *       start.</li>
 * </ul>
 * The packages are members of the npm workspace of the repository root, which links them with the generated API,
 * the support package and the runtime.
 */
public final class TsTckGenerator {

    /** The directory of the contract package in the output. */
    static final String CONTRACT = "contract";

    /** The directory of the server package in the output. */
    static final String SERVER = "server";

    private static final String INDENT = "    ";
    private static final Pattern PACKAGE_IMPORT = Pattern.compile("from \"(@[^/\"]+/[^/\"]+)");

    private final TsGeneratorConfig config;
    private final String api;

    /**
     * Creates a generator.
     *
     * @param config the configuration of the generated API (scope, version, support package)
     * @param api    the directory of the generated API workspace, relative to the output directory (e.g.
     *               {@code ../ts})
     */
    public TsTckGenerator(final TsGeneratorConfig config, final String api) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.api = Objects.requireNonNull(api, "api must not be null").replace('\\', '/');
    }

    /**
     * Generates the contract and the TCK server.
     *
     * @param model    the linked model of the specs
     * @param bindings the resolved bindings (must be free of errors)
     * @return the generated files, sorted by path ({@code contract/...} and {@code server/...})
     * @throws GenerationException if a bound type is not part of the generated API
     */
    public List<GeneratedFile> generate(final LinkedModel model, final TckBindings.Bindings bindings) {
        Objects.requireNonNull(model, "model must not be null");
        Objects.requireNonNull(bindings, "bindings must not be null");
        if (bindings.hasErrors()) {
            throw new GenerationException(bindings.diagnostics().stream().map(Object::toString).toList());
        }
        final TsContext context = new TsContext(config, model);
        final List<String> problems = new ArrayList<>();
        final List<GeneratedFile> files = new ArrayList<>();
        try {
            files.addAll(contract(context));
        } catch (final IllegalStateException | NullPointerException e) {
            problems.add("contract: " + e.getMessage());
        }
        final List<String> classes = new ArrayList<>();
        final List<GeneratedFile> server = new ArrayList<>();
        bindings.bindings().stream().collect(Collectors.groupingBy(TckBindings.Resolved::file,
                java.util.TreeMap::new, Collectors.toList())).forEach((file, resolved) -> {
                    final String className = className(file);
                    classes.add(className);
                    try {
                        server.add(bindingsClass(className, file, resolved, context));
                    } catch (final IllegalStateException | NullPointerException e) {
                        problems.add(file + ": " + e.getMessage());
                    }
                });
        try {
            server.add(commonClass(bindings.common(), context));
        } catch (final IllegalStateException | NullPointerException e) {
            problems.add("common transaction parameters: " + e.getMessage());
        }
        if (!problems.isEmpty()) {
            throw new GenerationException(problems);
        }
        server.add(serverModule(classes));
        server.add(new GeneratedFile(SERVER + "/src/main.ts", TsGenerator.HEADER + """

                import { createServer } from "./server.js";

                // Starts the TCK server: `node dist/main.js [port]` (default 8544).
                const server = await createServer();
                const port = await server.start(process.argv[2] === undefined ? 8544 : Number(process.argv[2]));
                console.log(`TCK server listening on port ${port}`);
                """));
        server.add(new GeneratedFile(SERVER + "/src/index.ts", TsGenerator.HEADER + "\n"
                + "export { createServer } from \"./server.js\";\n"));
        files.addAll(server);
        files.add(packageJson(SERVER, "tck-server", "The JSON-RPC server of the Hiero SDK TCK for the TypeScript API.",
                server, List.of(contractPackage(), runtimePackage()), "server.js"));
        files.add(tsconfig(SERVER, server, List.of("../" + CONTRACT)));
        return files.stream().sorted().toList();
    }

    /**
     * Returns the class of a bindings file: {@code crypto-service.md} becomes {@code CryptoServiceBindings}.
     *
     * @param file the bindings file
     * @return the class name
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

    private String contractPackage() {
        return config.scope() + "/tck-contract";
    }

    private String runtimePackage() {
        return config.scope() + "/tck-runtime";
    }

    // --- contract ------------------------------------------------------------------------------------

    private List<GeneratedFile> contract(final TsContext context) {
        final List<GeneratedFile> files = new ArrayList<>();
        files.add(converters(context));
        final TsImports imports = new TsImports(context, "", CONTRACT + "/src");
        final String transaction = imports.type(new QualifiedName("consensusnode.transactions", "Transaction"));
        final String receipt = imports.type(new QualifiedName("consensusnode.transactions", "Receipt"));
        final String query = imports.type(new QualifiedName("consensusnode.queries", "Query"));
        final String queryResponse = imports.type(new QualifiedName("consensusnode.queries", "QueryResponse"));
        final String body = RUNTIME.replace("$Transaction", transaction).replace("$Receipt", receipt)
                .replace("$QueryResponse", queryResponse).replace("$Query", query);
        files.add(new GeneratedFile(CONTRACT + "/src/TckRuntime.ts", TsGenerator.HEADER + "\n"
                + "import type { Converters } from \"./Converters.js\";\n" + imports.render() + "\n" + body));
        files.add(new GeneratedFile(CONTRACT + "/src/index.ts", TsGenerator.HEADER + "\n"
                + "/**\n * The contract between the generated TCK server and the hand-written TCK runtime.\n *\n"
                + " * @packageDocumentation\n */\n"
                + "export * from \"./Converters.js\";\nexport * from \"./TckRuntime.js\";\n"));
        files.add(packageJson(CONTRACT, "tck-contract",
                "The contract between the generated TCK server and the hand-written TCK runtime.", files, List.of(),
                "index.js"));
        files.add(tsconfig(CONTRACT, files, List.of()));
        return files;
    }

    /** The interface of the converter catalogue. */
    private GeneratedFile converters(final TsContext context) {
        final TsImports imports = new TsImports(context, "", CONTRACT + "/src");
        final StringBuilder methods = new StringBuilder();
        for (final Converter converter : Converter.values()) {
            if (converter.type() instanceof Type.DeclaredType declared && !context.isGenerated(declared.name())) {
                continue; // no binding can use it: its type is not part of the API
            }
            final String type = tsType(converter, context, imports);
            if (converter.inbound()) {
                methods.append('\n').append(TsDoc.render(INDENT, List.of("`" + converter.id() + "`: "
                                + converter.description() + " JSON to value."),
                        List.of("@param json - the JSON value (string, number or boolean)", "@returns the value"),
                        false))
                        .append(INDENT).append(converter.id()).append("(json: unknown): ").append(type).append(";\n");
            }
            if (converter.outbound()) {
                methods.append('\n').append(TsDoc.render(INDENT, List.of("`" + converter.id() + "`: "
                                + converter.description() + " Value to JSON."),
                        List.of("@param value - the value", "@returns the JSON value"), false))
                        .append(INDENT).append(converter.id()).append("Json(value: ").append(type)
                        .append("): unknown;\n");
            }
        }
        final String code = "/**\n"
                + " * The converter catalogue of the TCK bindings: conversions between the JSON conventions of the TCK\n"
                + " * and API values. Every language implements it once in its runtime; the generated server only\n"
                + " * calls it.\n"
                + " */\n"
                + "export interface Converters {\n" + methods + "}\n";
        final String rendered = imports.render(code);
        return new GeneratedFile(CONTRACT + "/src/Converters.ts", TsGenerator.HEADER + "\n"
                + (rendered.isEmpty() ? "" : rendered + "\n") + code);
    }

    /** The TypeScript type of the canonical type of a converter; a generic declared type gets wildcards. */
    private static String tsType(final Converter converter, final TsContext context, final TsImports imports) {
        final Type type = converter.type();
        if (type instanceof Type.DeclaredType declared) {
            final int parameters = context.model().definition(declared).typeParameters().size();
            return TsTypes.type(new Type.DeclaredType(declared.name(),
                    Collections.nCopies(parameters, new Type.WildcardType(null))), imports);
        }
        return TsTypes.type(type, imports);
    }

    private static final String RUNTIME = """
            /** A JSON object of a request or result. */
            export type JsonObject = Readonly<Record<string, unknown>>;

            /** A JSON source of a value: a path in a JSON object and the converter of the value. */
            export interface Source<T> {
                /** The path (`memo`, `hbar.amount`). */
                readonly path: string;
                /** The converter of the JSON value. */
                readonly converter: (json: unknown) => T;
            }

            /**
             * The session of a TCK test file (the `sessionId` of its requests). Opaque for the generated server; the
             * runtime keeps the client of the session behind it.
             */
            export interface Session {
                /** The session ID. */
                readonly id: string;
            }

            /** The handler of a JSON-RPC method: returns the result (an object, array or value). */
            export type Handler = (params: JsonObject, session: Session) => Promise<unknown>;

            /** The JSON-RPC server of the TCK. */
            export interface TckServer {
                /** Handles a JSON-RPC request (JSON text) and returns the response (JSON text). */
                handle(request: string): Promise<string>;
                /** Starts the HTTP server on a port (0 for any free port) and returns the port. */
                start(port: number): Promise<number>;
                /** Stops the HTTP server. */
                stop(): Promise<void>;
            }

            /**
             * The runtime of the TCK server: everything the generated server needs besides the API. Every language
             * implements it by hand once; the generated server loads the implementation with a dynamic `import` and is
             * compiled without it.
             */
            export interface TckRuntime {
                /** The converters. */
                readonly converters: Converters;
                /** Returns the converted value of the first source that is present in a JSON object, `null` if none is. */
                value<T>(json: JsonObject, sources: ReadonlyArray<Source<T>>): T | null;
                /** Returns a value the API requires; fails with the TCK parameters it comes from if it is `null`. */
                required<T>(value: T | null, parameters: string): T;
                /** Returns a value, or the default if it is `null`. */
                or<T>(value: T | null, defaultValue: T): T;
                /** Returns a nested JSON object (path `a.b`), `null` if absent. */
                object(json: JsonObject, path: string): JsonObject | null;
                /** Returns the objects of a JSON list, empty if absent. */
                objects(json: JsonObject, path: string): ReadonlyArray<JsonObject>;
                /** Rejects a parameter the API cannot provide, if it is sent. */
                unsupported(json: JsonObject, name: string, reason: string): void;
                /**
                 * Signs a transaction with the operator and the additional signers of the common transaction
                 * parameters, sends it and returns the receipt; a receipt with another status than `SUCCESS` is
                 * answered as error of the network.
                 */
                transaction<R extends $Receipt>(session: Session, transaction: $Transaction<R, any>,
                                                common: JsonObject | null): Promise<R>;
                /** Sends a query and returns the value of its response. */
                query<T>(session: Session, query: $Query<any, $QueryResponse<T>>): Promise<T>;
                /** Adds a field to a JSON result if the value is present; a path (`a.b`) creates nested objects. */
                put<T>(result: Record<string, unknown>, path: string, value: T | null | undefined,
                       converter: (value: T) => unknown): void;
                /**
                 * Creates the JSON-RPC server with the given methods and the runtime methods `setup`, `reset` and
                 * `generateKey`.
                 */
                server(methods: ReadonlyMap<string, Handler>): TckServer;
            }

            /** The module of a runtime: the generated server imports it by its package name. */
            export interface TckRuntimeModule {
                /** Creates the runtime. */
                createRuntime(): TckRuntime;
            }
            """;

    // --- bindings classes ----------------------------------------------------------------------------

    private GeneratedFile bindingsClass(final String className, final String file,
                                        final List<TckBindings.Resolved> bindings, final TsContext context) {
        final TsImports imports = new TsImports(context, "", SERVER + "/src", className);
        final List<String> methods = new ArrayList<>();
        for (final TckBindings.Resolved binding : bindings) {
            methods.add(method(binding, imports, context));
        }
        final StringBuilder code = new StringBuilder()
                .append("/** The TCK methods of the bindings file `").append(file).append("`. */\n")
                .append("export class ").append(className).append(" {\n\n");
        final String body = String.join("\n", methods);
        code.append(fields(body, true))
                .append(INDENT).append("/** Creates the methods. */\n")
                .append(INDENT).append("constructor(runtime: TckRuntime, common: CommonBindings) {\n")
                .append(INDENT).append(INDENT).append("this.#runtime = runtime;\n");
        if (body.contains("this.#convert")) {
            code.append(INDENT).append(INDENT).append("this.#convert = runtime.converters;\n");
        }
        if (body.contains("this.#common")) {
            code.append(INDENT).append(INDENT).append("this.#common = common;\n");
        }
        code.append(INDENT).append("}\n\n")
                .append(INDENT).append("/** Registers the methods. */\n")
                .append(INDENT).append("register(methods: Map<string, Handler>): void {\n");
        for (final TckBindings.Resolved binding : bindings) {
            code.append(INDENT).append(INDENT).append("methods.set(")
                    .append(TsLiterals.quote(binding.binding().name())).append(", (params, session) => this.")
                    .append(binding.binding().name()).append("(params, session));\n");
        }
        code.append(INDENT).append("}\n");
        methods.forEach(m -> code.append('\n').append(m));
        code.append("}\n");
        for (final String name : List.of("TckRuntime", "Converters", "Handler", "JsonObject", "Session")) {
            imports.external(contractPackage(), name, false);
        }
        final String rendered = imports.render(code.toString());
        return new GeneratedFile(SERVER + "/src/" + className + ".ts", TsGenerator.HEADER + "\n" + rendered
                + "import type { CommonBindings } from \"./CommonBindings.js\";\n\n" + code);
    }

    /** The private fields the methods use. */
    private static String fields(final String body, final boolean common) {
        final StringBuilder java = new StringBuilder()
                .append(INDENT).append("readonly #runtime: TckRuntime;\n");
        if (body.contains("this.#convert")) {
            java.append(INDENT).append("readonly #convert: Converters;\n");
        }
        if (common && body.contains("this.#common")) {
            java.append(INDENT).append("readonly #common: CommonBindings;\n");
        }
        return java.append('\n').toString();
    }

    private String method(final TckBindings.Resolved binding, final TsImports imports, final TsContext context) {
        final String indent = INDENT + INDENT;
        final StringBuilder body = new StringBuilder();
        rejectUnsupported(binding.members(), "params", body, indent);
        final String request = construct(binding.type(), binding.members(), "params", null, 1, body, indent,
                imports, context);
        final boolean transaction = binding.kind() == TckBindings.Kind.TRANSACTION;
        final String variable = transaction ? "transaction" : "query";
        body.append(indent).append("const ").append(variable).append(" = ").append(request).append(";\n");
        final String value = transaction ? "receipt" : "response";
        if (transaction) {
            body.append(indent).append("const commonParams = this.#runtime.object(params, ")
                    .append(TsLiterals.quote(TckSpecifications.COMMON)).append(");\n")
                    .append(indent).append("this.#common.apply(transaction, commonParams);\n")
                    .append(indent)
                    .append("const receipt = await this.#runtime.transaction(session, transaction, commonParams);\n");
        } else {
            body.append(indent).append("const response = await this.#runtime.query(session, query);\n");
        }
        body.append(indent).append("const result: Record<string, unknown> = {};\n");
        for (final TckBindings.Member member : binding.members()) {
            if (member instanceof TckBindings.Result result) {
                body.append(indent).append("this.#runtime.put(result, ").append(TsLiterals.quote(result.name()
                        .toString())).append(", ").append(access(value, result.fields()))
                        .append(", (value) => this.#convert.").append(result.converter().id()).append("Json(value));\n");
            } else if (member instanceof TckBindings.Unsupported(final Binding.Unsupported unsupported)
                    && unsupported.result()) {
                body.append(indent).append("// not provided: ").append(unsupported.name()).append(" (")
                        .append(unsupported.reason()).append(")\n");
            }
        }
        body.append(indent).append("return result;\n");
        final StringBuilder code = new StringBuilder();
        if (!binding.binding().documentation().isBlank()) {
            code.append(TsDoc.render(INDENT, List.of(binding.binding().documentation())));
        }
        return code.append(INDENT).append("async ").append(binding.binding().name())
                .append("(params: JsonObject, session: Session): Promise<unknown> {\n")
                .append(body).append(INDENT).append("}\n").toString();
    }

    /** The JSON parameters the API cannot provide are rejected if they are sent. */
    private static void rejectUnsupported(final List<TckBindings.Member> members, final String json,
                                          final StringBuilder body, final String indent) {
        for (final TckBindings.Member member : members) {
            if (member instanceof TckBindings.Unsupported(final Binding.Unsupported unsupported)
                    && !unsupported.result()) {
                body.append(indent).append("this.#runtime.unsupported(").append(json).append(", ")
                        .append(TsLiterals.quote(unsupported.name())).append(", ")
                        .append(TsLiterals.quote(unsupported.reason())).append(");\n");
            }
        }
    }

    /**
     * Returns the constructor call of a type with the bound attributes (the constructor applies the defaults of the
     * others); lists of {@code each} are built by statements appended to {@code body} first.
     */
    private String construct(final TypeDefinition type, final List<TckBindings.Member> members, final String json,
                             final @Nullable String parent, final int depth, final StringBuilder body,
                             final String indent, final TsImports imports, final TsContext context) {
        if (!context.isGenerated(type.name()) || !context.isClass(type.name())) {
            throw new IllegalStateException("Type '" + type.name() + "' is not a class of the generated API");
        }
        final List<String> properties = new ArrayList<>();
        for (final FieldDefinition field : type.fields()) {
            final Optional<TckBindings.Member> member = members.stream().filter(m -> field(m)
                    .filter(f -> f.name().equals(field.name())).isPresent()).findFirst();
            if (member.isEmpty()) {
                if (!field.hasAnnotation("nullable") && !field.hasAnnotation("default")) {
                    throw new IllegalStateException("Required attribute '" + field.name() + "' of '" + type.name()
                            + "' is not bound");
                }
                continue; // the constructor applies null or the default
            }
            final String value = member.get() instanceof TckBindings.Value v
                    ? value(field, v, json, parent, imports)
                    : elements(field, (TckBindings.Elements) member.get(), json, depth, body, indent, imports,
                    context);
            properties.add(field.name() + ": " + value);
        }
        final String name = imports.value(type.name());
        if (properties.isEmpty()) {
            return "new " + name + "({})";
        }
        final String propertyIndent = "\n" + indent + INDENT;
        return "new " + name + "({" + propertyIndent + String.join("," + propertyIndent, properties) + "\n" + indent
                + "})";
    }

    private static Optional<FieldDefinition> field(final TckBindings.Member member) {
        return switch (member) {
            case TckBindings.Value value -> Optional.of(value.field());
            case TckBindings.Elements elements -> Optional.of(elements.field());
            default -> Optional.empty();
        };
    }

    /** A bound value: the first present source, then the default, {@code null} or the check that it is present. */
    private static String value(final FieldDefinition field, final TckBindings.Value value, final String json,
                                final @Nullable String parent, final TsImports imports) {
        final String lookup = lookup(value.sources(), json, parent);
        if (field.hasAnnotation("default")) {
            return "this.#runtime.or(" + lookup + ", " + TsLiterals.expression(field.annotation("default")
                    .orElseThrow().arguments().getFirst(), field.type(), imports) + ")";
        }
        if (field.hasAnnotation("nullable")) {
            return lookup;
        }
        return "this.#runtime.required(" + lookup + ", " + TsLiterals.quote(value.sources().stream()
                .map(s -> s.path().toString()).collect(Collectors.joining(" | "))) + ")";
    }

    /** The lookup of the sources: one {@code value} per JSON object, the first present one wins. */
    private static String lookup(final List<TckBindings.Source> sources, final String json,
                                 final @Nullable String parent) {
        final List<String> groups = new ArrayList<>();
        String currentObject = null;
        List<String> current = new ArrayList<>();
        for (final TckBindings.Source source : sources) {
            final String object = source.path().parent() ? Objects.requireNonNull(parent,
                    "'^' outside of 'each'") : json;
            if (!object.equals(currentObject) && !current.isEmpty()) {
                groups.add("this.#runtime.value(" + currentObject + ", [" + String.join(", ", current) + "])");
                current = new ArrayList<>();
            }
            currentObject = object;
            current.add("{ path: " + TsLiterals.quote(String.join(".", source.path().segments()))
                    + ", converter: (json) => this.#convert." + source.converter().id() + "(json) }");
        }
        groups.add("this.#runtime.value(" + currentObject + ", [" + String.join(", ", current) + "])");
        String lookup = groups.getLast();
        for (int i = groups.size() - 2; i >= 0; i--) {
            lookup = "this.#runtime.or(" + groups.get(i) + ", " + lookup + ")";
        }
        return lookup;
    }

    /**
     * A list built with {@code each}: the first segment of the source is the JSON list, the rest the object inside
     * each element ({@code transfers.hbar}); elements without that object are skipped.
     */
    private String elements(final FieldDefinition field, final TckBindings.Elements elements, final String json,
                            final int depth, final StringBuilder body, final String indent, final TsImports imports,
                            final TsContext context) {
        final String list = TsNames.local(field.name()) + (depth > 1 ? String.valueOf(depth) : "");
        final String element = "element" + depth;
        final String item = "item" + depth;
        final String elementType = imports.type(elements.elementType().name());
        body.append(indent).append("const ").append(list).append(": ").append(elementType).append("[] = [];\n")
                .append(indent).append("for (const ").append(element).append(" of this.#runtime.objects(")
                .append(json).append(", ").append(TsLiterals.quote(elements.source().first())).append(")) {\n");
        final String inner = indent + INDENT;
        if (elements.source().segments().size() == 1) {
            body.append(inner).append("const ").append(item).append(" = ").append(element).append(";\n");
        } else {
            body.append(inner).append("const ").append(item).append(" = this.#runtime.object(").append(element)
                    .append(", ").append(TsLiterals.quote(elements.source().rest().toString())).append(");\n")
                    .append(inner).append("if (").append(item).append(" === null) {\n")
                    .append(inner).append(INDENT).append("continue;\n")
                    .append(inner).append("}\n");
        }
        rejectUnsupported(elements.members(), item, body, inner);
        final String construction = construct(elements.elementType(), elements.members(), item, element, depth + 1,
                body, inner, imports, context);
        body.append(inner).append(list).append(".push(").append(construction).append(");\n")
                .append(indent).append("}\n");
        return list;
    }

    /** The access to a result value: properties, null-safe after a nullable attribute. */
    private static String access(final String root, final List<FieldDefinition> fields) {
        final StringBuilder code = new StringBuilder(root);
        boolean optional = false;
        for (final FieldDefinition field : fields) {
            code.append(optional ? "?." : ".").append(field.name());
            optional |= field.hasAnnotation("nullable");
        }
        return code.toString();
    }

    // --- common transaction parameters ---------------------------------------------------------------

    private GeneratedFile commonClass(final Optional<TckBindings.Resolved> common, final TsContext context) {
        final TsImports imports = new TsImports(context, "", SERVER + "/src", "CommonBindings");
        final String transaction = imports.type(new QualifiedName("consensusnode.transactions", "Transaction"));
        final String indent = INDENT + INDENT;
        final StringBuilder body = new StringBuilder();
        if (common.isPresent()) {
            for (final String line : common.get().binding().documentation().lines().toList()) {
                body.append(indent).append("// ").append(line).append('\n');
            }
            rejectUnsupported(common.get().members(), "params", body, indent);
            for (final TckBindings.Member member : common.get().members()) {
                if (member instanceof TckBindings.Value value) {
                    final String name = TsNames.local(value.field().name());
                    body.append(indent).append("const ").append(name).append(" = ")
                            .append(lookup(value.sources(), "params", null)).append(";\n")
                            .append(indent).append("if (").append(name).append(" !== null) {\n")
                            .append(indent).append(INDENT).append("transaction.").append(value.field().name())
                            .append(" = ").append(name).append(";\n")
                            .append(indent).append("}\n");
                }
            }
        }
        final String bodyText = body.toString();
        final StringBuilder code = new StringBuilder()
                .append("/** The common transaction parameters (`").append(TckSpecifications.COMMON)
                .append("`) of all transactions. */\n")
                .append("export class CommonBindings {\n\n")
                .append(fields(bodyText, false))
                .append(INDENT).append("/** Creates the common transaction parameters. */\n")
                .append(INDENT).append("constructor(runtime: TckRuntime) {\n")
                .append(INDENT).append(INDENT).append("this.#runtime = runtime;\n");
        if (bodyText.contains("this.#convert")) {
            code.append(INDENT).append(INDENT).append("this.#convert = runtime.converters;\n");
        }
        code.append(INDENT).append("}\n\n")
                .append(INDENT).append("/** Applies the common transaction parameters (`null` if none are sent). */\n")
                .append(INDENT).append("apply(transaction: ").append(transaction)
                .append("<any, any>, params: JsonObject | null): void {\n")
                .append(indent).append("if (params === null) {\n")
                .append(indent).append(INDENT).append("return;\n")
                .append(indent).append("}\n")
                .append(bodyText.isEmpty() ? indent + "void this.#runtime;\n" : bodyText)
                .append(INDENT).append("}\n")
                .append("}\n");
        for (final String name : List.of("TckRuntime", "Converters", "JsonObject")) {
            imports.external(contractPackage(), name, false);
        }
        return new GeneratedFile(SERVER + "/src/CommonBindings.ts", TsGenerator.HEADER + "\n"
                + imports.render(code.toString()) + "\n" + code);
    }

    // --- server module and packages ------------------------------------------------------------------

    private GeneratedFile serverModule(final List<String> classes) {
        final StringBuilder code = new StringBuilder(TsGenerator.HEADER).append('\n')
                .append("import type { Handler, TckRuntimeModule, TckServer } from \"").append(contractPackage())
                .append("\";\n")
                .append("import { CommonBindings } from \"./CommonBindings.js\";\n");
        classes.stream().sorted().forEach(c -> code.append("import { ").append(c).append(" } from \"./").append(c)
                .append(".js\";\n"));
        code.append('\n')
                .append("/** The package of the runtime; `TCK_RUNTIME` selects another one. */\n")
                .append("const RUNTIME: string = process.env[\"TCK_RUNTIME\"] ?? \"").append(runtimePackage())
                .append("\";\n\n")
                .append("/**\n")
                .append(" * Creates the server with all methods and the runtime. The runtime is loaded at runtime: the server\n")
                .append(" * is compiled against the contract only.\n")
                .append(" *\n")
                .append(" * @returns the server, not started\n")
                .append(" */\n")
                .append("export async function createServer(): Promise<TckServer> {\n")
                .append(INDENT).append("const module = (await import(RUNTIME)) as TckRuntimeModule;\n")
                .append(INDENT).append("const runtime = module.createRuntime();\n")
                .append(INDENT).append("const common = new CommonBindings(runtime);\n")
                .append(INDENT).append("const methods = new Map<string, Handler>();\n");
        classes.forEach(c -> code.append(INDENT).append("new ").append(c).append("(runtime, common).register(methods);\n"));
        code.append(INDENT).append("return runtime.server(methods);\n")
                .append("}\n");
        return new GeneratedFile(SERVER + "/src/server.ts", code.toString());
    }

    /** The packages a package imports (API packages, support, contract), from its generated sources. */
    private static SortedSet<String> imported(final List<GeneratedFile> files, final String directory) {
        final SortedSet<String> packages = new TreeSet<>();
        for (final GeneratedFile file : files) {
            if (file.path().startsWith(directory + "/src/")) {
                final Matcher matcher = PACKAGE_IMPORT.matcher(file.content());
                while (matcher.find()) {
                    packages.add(matcher.group(1));
                }
            }
        }
        return packages;
    }

    private GeneratedFile packageJson(final String directory, final String name, final String description,
                                      final List<GeneratedFile> files, final List<String> more, final String main) {
        final SortedSet<String> dependencies = imported(files, directory);
        dependencies.addAll(more);
        return new GeneratedFile(directory + "/package.json", "{ \"//\": \"" + TsGenerator.MARKER + "\",\n"
                + "  \"name\": \"" + config.scope() + "/" + name + "\",\n"
                + "  \"version\": \"" + config.version() + "\",\n"
                + "  \"description\": \"" + description + "\",\n"
                + "  \"type\": \"module\",\n"
                + "  \"exports\": {\n"
                + "    \".\": {\n"
                + "      \"types\": \"./dist/" + main.replace(".js", ".d.ts") + "\",\n"
                + "      \"default\": \"./dist/" + main + "\"\n"
                + "    }\n"
                + "  },\n"
                + "  \"files\": [\"dist\"],\n"
                + "  \"dependencies\": {\n"
                + dependencies.stream().map(d -> "    \"" + d + "\": \"" + config.version() + "\"")
                .collect(Collectors.joining(",\n")) + "\n"
                + "  }\n"
                + "}\n");
    }

    /**
     * The project of a package: it extends the base configuration of the generated API and references the projects of
     * the packages it imports (never the runtime).
     */
    private GeneratedFile tsconfig(final String directory, final List<GeneratedFile> files,
                                   final List<String> more) {
        final List<String> references = new ArrayList<>();
        for (final String imported : imported(files, directory)) {
            final String name = imported.substring(imported.indexOf('/') + 1);
            if (imported.equals(TsNames.supportPackage(config))) {
                final String support = TsNames.supportDirectory(config, "");
                references.add(support.startsWith("/") ? support : path(apiFromPackage() + "/" + support));
            } else if (!imported.equals(contractPackage()) && !imported.equals(runtimePackage())) {
                references.add(path(apiFromPackage() + "/" + TsNames.packageDirectory(name)));
            }
        }
        references.addAll(more);
        return new GeneratedFile(directory + "/tsconfig.json", "// " + TsGenerator.MARKER + "\n"
                + "{\n"
                + "  \"extends\": \"" + path(apiFromPackage() + "/tsconfig.base.json") + "\",\n"
                + "  \"compilerOptions\": {\n"
                + "    \"rootDir\": \"src\",\n"
                + "    \"outDir\": \"dist\"\n"
                + "  },\n"
                + "  \"include\": [\"src\"],\n"
                + "  \"references\": [\n"
                + references.stream().map(r -> "    { \"path\": \"" + r + "\" }").collect(Collectors.joining(",\n"))
                + "\n  ]\n"
                + "}\n");
    }

    /** The directory of the generated API workspace from the directory of a package of the output. */
    private String apiFromPackage() {
        return api.startsWith("/") ? api : "../" + api;
    }

    /** A normalized path ({@code ../../ts/../../sdk-ts/support} becomes {@code ../../../sdk-ts/support}). */
    private static String path(final String path) {
        return Path.of(path).normalize().toString().replace('\\', '/');
    }
}
