package org.hiero.sdk.v3.metalang.generator.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.model.TypeDefinition;
import org.hiero.sdk.v3.metalang.tck.Converter;

/**
 * Generates the contract between the generated TCK server and the hand-written runtime of a language (see
 * {@code tck-binding.md}): interfaces and records only, no implementation. {@code Converters} is derived from the
 * converter catalogue ({@link Converter}): one method per direction of every converter, typed with the Java type of
 * its canonical meta-language type. The other declarations describe what the binding flow needs (JSON access,
 * execution of transactions and queries, the JSON-RPC server); they change only with the binding language.
 */
final class JavaTckContractGenerator {

    /** The package of the contract. */
    static final String PACKAGE = "org.hiero.tck.contract";

    /** The Maven artifact of the contract. */
    static final String ARTIFACT = "hiero-sdk-tck-contract";

    /** The directory of the contract project in the output. */
    static final String DIRECTORY = "contract";

    private static final String INDENT = "    ";
    private static final String SOURCE_ROOT = DIRECTORY + "/src/main/java/" + PACKAGE.replace('.', '/') + "/";

    private JavaTckContractGenerator() {
    }

    /**
     * Generates the contract project.
     *
     * @param context     the context of the generated API
     * @param artifactIds the artifacts of the generated API
     * @param config      the configuration of the generated API (group ID, version)
     * @return the generated files
     */
    static List<GeneratedFile> generate(final JavaContext context, final List<String> artifactIds,
                                        final JavaGeneratorConfig config) {
        final List<GeneratedFile> files = new ArrayList<>();
        files.add(converters(context));
        files.add(file("TckRuntime", RUNTIME));
        files.add(file("Source", SOURCE));
        files.add(file("Session", SESSION));
        files.add(file("Handler", HANDLER));
        files.add(file("TckServer", SERVER));
        files.add(JavaTckGenerator.pom(DIRECTORY, ARTIFACT, "Hiero SDK TCK contract",
                "The contract between the generated TCK server and the hand-written TCK runtime.", artifactIds,
                List.of(), config, false));
        return files;
    }

    /** The interface of the converter catalogue. */
    private static GeneratedFile converters(final JavaContext context) {
        final Imports imports = context.imports(PACKAGE, "Converters");
        final StringBuilder methods = new StringBuilder();
        for (final Converter converter : Converter.values()) {
            if (!available(converter, context)) {
                continue; // no binding can use it: its type is not part of the API
            }
            final String type = javaType(converter, imports, context);
            if (converter.inbound()) {
                methods.append('\n')
                        .append(INDENT).append("/// `").append(converter.id()).append("`: ")
                        .append(converter.description()).append(" JSON to value.\n")
                        .append(INDENT).append("///\n")
                        .append(INDENT).append("/// @param json the JSON value (string, number or boolean)\n")
                        .append(INDENT).append("/// @return the value\n")
                        .append(INDENT).append(type).append(' ').append(converter.id()).append("(Object json);\n");
            }
            if (converter.outbound()) {
                methods.append('\n')
                        .append(INDENT).append("/// `").append(converter.id()).append("`: ")
                        .append(converter.description()).append(" Value to JSON.\n")
                        .append(INDENT).append("///\n")
                        .append(INDENT).append("/// @param value the value\n")
                        .append(INDENT).append("/// @return the JSON value\n")
                        .append(INDENT).append("Object ").append(converter.id()).append("Json(").append(type)
                        .append(" value);\n");
            }
        }
        final String rendered = imports.render();
        final String java = JavaGenerator.HEADER + '\n'
                + "package " + PACKAGE + ";\n\n"
                + (rendered.isEmpty() ? "" : rendered + '\n')
                + "/// The converter catalogue of the TCK bindings: conversions between the JSON conventions of the TCK and\n"
                + "/// API values. Every language implements it once in its runtime; the generated server only calls it.\n"
                + "public interface Converters {\n"
                + methods
                + "}\n";
        return new GeneratedFile(SOURCE_ROOT + "Converters.java", java);
    }

    /** Whether the canonical type of a converter is part of the generated API (always true for basic types). */
    private static boolean available(final Converter converter, final JavaContext context) {
        return !(converter.type() instanceof Type.DeclaredType declared) || context.isGenerated(declared.name());
    }

    /** The Java type of the canonical type of a converter; a generic declared type gets wildcards. */
    private static String javaType(final Converter converter, final Imports imports, final JavaContext context) {
        final Type type = converter.type();
        if (type instanceof Type.DeclaredType declared) {
            final TypeDefinition definition = context.model().definition(declared);
            final String name = imports.use(JavaNames.packageName(declared.name().namespace()), declared.name().name());
            final int parameters = definition.typeParameters().size();
            return parameters == 0 ? name : name + "<" + String.join(", ", Collections.nCopies(parameters, "?")) + ">";
        }
        return JavaTypes.type(type, true, imports);
    }

    private static GeneratedFile file(final String name, final String body) {
        return new GeneratedFile(SOURCE_ROOT + name + ".java", JavaGenerator.HEADER + '\n'
                + "package " + PACKAGE + ";\n\n" + body);
    }

    private static final String RUNTIME = """
            import java.util.List;
            import java.util.Map;
            import java.util.function.Function;
            import org.hiero.consensusnode.queries.Query;
            import org.hiero.consensusnode.queries.QueryResponse;
            import org.hiero.consensusnode.transactions.Receipt;
            import org.hiero.consensusnode.transactions.Transaction;
            import org.jspecify.annotations.Nullable;

            /// The runtime of the TCK server: everything the generated server needs besides the API. Every language
            /// implements it by hand once; the generated server finds the implementation with `java.util.ServiceLoader`
            /// and depends on it only at runtime.
            public interface TckRuntime {

                /// Returns the converters.
                ///
                /// @return the converters
                Converters converters();

                /// Returns the converted value of the first source that is present in a JSON object.
                ///
                /// @param json    the JSON object
                /// @param sources the alternative sources
                /// @param <T>     the API type
                /// @return the value, `null` if no source is present
                <T> @Nullable T value(Map<String, Object> json, List<? extends Source<? extends T>> sources);

                /// Returns a value the API requires.
                ///
                /// @param value      the value
                /// @param parameters the TCK parameters it comes from, for the error
                /// @param <T>        the type
                /// @return the value
                <T> T required(@Nullable T value, String parameters);

                /// Returns a value or a default.
                ///
                /// @param value        the value
                /// @param defaultValue the default
                /// @param <T>          the type
                /// @return the value, or the default if it is `null`
                <T> T or(@Nullable T value, T defaultValue);

                /// Returns a nested JSON object.
                ///
                /// @param json the JSON object
                /// @param path the path (`a.b`)
                /// @return the object, `null` if absent
                @Nullable Map<String, Object> object(Map<String, Object> json, String path);

                /// Returns the objects of a JSON list.
                ///
                /// @param json the JSON object
                /// @param path the path of the list
                /// @return the objects, empty if absent
                List<Map<String, Object>> objects(Map<String, Object> json, String path);

                /// Rejects a parameter the API cannot provide, if it is sent.
                ///
                /// @param json   the JSON object
                /// @param name   the parameter
                /// @param reason why the API cannot provide it
                void unsupported(Map<String, Object> json, String name, String reason);

                /// Signs a transaction with the operator and the additional signers of the common transaction
                /// parameters, sends it and waits for the receipt; a receipt with another status than `SUCCESS` is
                /// answered as error of the network.
                ///
                /// @param session     the session
                /// @param transaction the transaction
                /// @param common      the common transaction parameters, `null` if none are sent
                /// @param <R>         the receipt type
                /// @return the receipt
                <R extends Receipt> R transaction(Session session, Transaction<R, ?> transaction,
                                                  @Nullable Map<String, Object> common);

                /// Sends a query and returns the value of its response.
                ///
                /// @param session the session
                /// @param query   the query
                /// @param <T>     the value type
                /// @param <Q>     the response type
                /// @return the value
                <T, Q extends QueryResponse<T>> T query(Session session, Query<T, Q> query);

                /// Adds a field to a JSON result if the value is present; a path (`a.b`) creates nested objects.
                ///
                /// @param result    the JSON result
                /// @param path      the field
                /// @param value     the API value
                /// @param converter the converter to JSON
                /// @param <T>       the API type
                <T> void put(Map<String, Object> result, String path, @Nullable T value,
                             Function<? super T, Object> converter);

                /// Creates the JSON-RPC server with the given methods and the runtime methods `setup`, `reset` and
                /// `generateKey`.
                ///
                /// @param methods the methods by name
                /// @return the server, not started
                TckServer server(Map<String, Handler> methods);
            }
            """;

    private static final String SOURCE = """
            import java.util.function.Function;

            /// A JSON source of a value: a path in a JSON object and the converter of the value.
            ///
            /// @param path      the path (`memo`, `hbar.amount`)
            /// @param converter the converter
            /// @param <T>       the API type
            public record Source<T>(String path, Function<Object, T> converter) {
            }
            """;

    private static final String SESSION = """
            /// The session of a TCK test file (the `sessionId` of its requests). Opaque for the generated server; the
            /// runtime keeps the client of the session behind it.
            public interface Session {

                /// Returns the session ID.
                ///
                /// @return the ID
                String id();
            }
            """;

    private static final String HANDLER = """
            import java.util.Map;

            /// The handler of a JSON-RPC method.
            @FunctionalInterface
            public interface Handler {

                /// Handles a request.
                ///
                /// @param params  the parameters of the request
                /// @param session the session of the request
                /// @return the result (a map, list or value)
                /// @throws Exception any error; the runtime turns it into a JSON-RPC error
                Object handle(Map<String, Object> params, Session session) throws Exception;
            }
            """;

    private static final String SERVER = """
            import java.io.IOException;

            /// The JSON-RPC server of the TCK.
            public interface TckServer {

                /// Handles a JSON-RPC request.
                ///
                /// @param request the request as JSON text
                /// @return the response as JSON text
                String handle(String request);

                /// Starts the HTTP server.
                ///
                /// @param port the port, 0 for any free port
                /// @return the port the server listens on
                /// @throws IOException if the server cannot be started
                int start(int port) throws IOException;

                /// Stops the HTTP server.
                void stop();
            }
            """;
}
