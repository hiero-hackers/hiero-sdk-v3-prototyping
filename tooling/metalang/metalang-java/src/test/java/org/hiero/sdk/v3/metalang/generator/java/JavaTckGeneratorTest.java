package org.hiero.sdk.v3.metalang.generator.java;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.hiero.sdk.v3.metalang.MetaLang;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.generator.GenerationException;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.hiero.sdk.v3.metalang.tck.TckBindings;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;

class JavaTckGeneratorTest {

    private static final Path SPECS = Path.of(System.getProperty("spec.root", "../../../spec"));
    private static final Path BINDINGS = SPECS.resolveSibling("tck/bindings");
    /** The sources of the hand-written runtime, a dependency of the generated server. */
    private static final Path RUNTIME = SPECS.resolveSibling("tck/runtime/java/src/main/java");

    private final JavaTckGenerator generator = new JavaTckGenerator(JavaGeneratorConfig.DEFAULT);

    @Test
    void classNamesShouldFollowTheBindingsFile() {
        assertThat(JavaTckGenerator.className("crypto-service.md")).isEqualTo("CryptoServiceBindings");
        assertThat(JavaTckGenerator.className("services/token_service.md")).isEqualTo("TokenServiceBindings");
    }

    @Test
    void bindingsWithErrorsShouldBeRejected() {
        // GIVEN
        final LinkedModel model = LinkedModel.of(new MetaLang().validate(SPECS).model());
        final TckBindings.Bindings bindings = TckBindings.resolve(Map.of("broken.md", """
                ```bindings
                binding foo -> UnknownType {
                }
                ```
                """), model);

        // THEN
        assertThatThrownBy(() -> generator.generate(model, bindings)).isInstanceOf(GenerationException.class)
                .hasMessageContaining("UnknownType");
    }

    /** A model with the forms the repository bindings do not use (yet). */
    private static final LinkedModel SHOP = LinkedModel.of(new MetaLang().validate(Map.of("f/shop.md",
            org.hiero.sdk.v3.metalang.TestSpecs.markdown("""
                    namespace shop

                    Response<$$R> {
                        @@immutable value: $$R
                    }

                    Detail {
                        @@immutable note: string
                    }

                    OrderReceipt {
                        @@immutable orderId: string
                        @@immutable @@nullable detail: Detail
                    }

                    Item {
                        @@immutable name: string
                        @@immutable @@default(false) gift: bool
                    }

                    Group {
                        @@immutable @@default([]) items: list<Item>
                    }

                    OrderTransaction {
                        @@immutable customer: string
                        @@immutable @@nullable note: string
                        @@immutable @@default(false) express: bool
                        @@immutable @@default([]) items: list<Item>
                        @@immutable @@default([]) groups: list<Group>
                        Response<OrderReceipt> signWithOperatorAndSubmit()
                    }

                    BrokenTransaction {
                        @@immutable @@nullable callback: Unknown
                        Response<OrderReceipt> signWithOperatorAndSubmit()
                    }
                    """))).model());

    private static TckBindings.Bindings shopBindings(final String bindings) {
        return TckBindings.resolve(Map.of("shop.md", "```bindings\nrequires {OrderTransaction, Item, Group, "
                + "BrokenTransaction} from shop\n" + bindings + "```\n"), SHOP);
    }

    @Test
    void allBindingFormsShouldBeGenerated() {
        // WHEN
        final List<GeneratedFile> files = generator.generate(SHOP, shopBindings("""
                binding createOrder -> OrderTransaction {
                    customer = customer : string | name : string
                    items = each items -> Item {
                        name = name : string | ^label : string
                    }
                    groups = each groups -> Group {
                        items = each entries.item -> Item {
                            name = name : string
                        }
                    }
                    result orderId = receipt.orderId : string
                    result note = receipt.detail.note : string
                }
                """));

        // THEN
        final String java = files.stream().filter(f -> f.path().endsWith("/ShopBindings.java")).findFirst()
                .orElseThrow().content();
        assertThat(java)
                .contains("runtime.required(runtime.value(params, List.of(new Source<>(\"customer\", convert::string), "
                        + "new Source<>(\"name\", convert::string))), \"customer | name\")")
                .contains("final Map<String, Object> item1 = element1;")
                .contains("runtime.or(runtime.value(item1, List.of(new Source<>(\"name\", convert::string))), "
                        + "runtime.value(element1, List.of(new Source<>(\"label\", convert::string))))")
                .contains("final List<Item> items2 = new ArrayList<>();")
                .contains("final Map<String, Object> item2 = runtime.object(element2, \"item\");")
                .contains("null,\n")
                .contains("false,\n")
                .contains("Optional.ofNullable(receipt.detail()).map(v -> v.note()).orElse(null)");
        assertThat(files.stream().filter(f -> f.path().endsWith("/CommonBindings.java")).findFirst()
                .orElseThrow().content()).doesNotContain("transaction.set");
        // converters whose type the API does not have cannot be used, so the contract leaves them out
        assertThat(files.stream().filter(f -> f.path().endsWith("/Converters.java")).findFirst().orElseThrow()
                .content()).contains("    String string(Object json);\n").doesNotContain("accountId");
    }

    @Test
    void typesThatAreNotGeneratedShouldBeRejected() {
        assertThatThrownBy(() -> generator.generate(SHOP, shopBindings("""
                binding broken -> BrokenTransaction {
                }
                """))).isInstanceOf(GenerationException.class)
                .hasMessageContaining("Type 'shop.BrokenTransaction' is not part of the generated Java API");
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class RepositoryBindings {

        private List<GeneratedFile> files;
        private ClassLoader loader;
        private List<String> diagnostics;

        @BeforeAll
        void generateAndCompile(@TempDir final Path output) throws Exception {
            final LinkedModel model = LinkedModel.of(new MetaLang().validate(SPECS).model());
            final GeneratedJava.Compilation api = GeneratedJava.compile(new JavaGenerator().generate(model),
                    output.resolve("api"));
            assertThat(api.success()).as(String.join("\n", api.diagnostics())).isTrue();
            files = generator.generate(model, TckBindings.read(BINDINGS, model));
            final Path generated = output.resolve("generated");
            for (final GeneratedFile file : files) {
                final Path target = generated.resolve(file.path());
                Files.createDirectories(target.getParent());
                Files.writeString(target, file.content(), StandardCharsets.UTF_8);
            }
            final List<String> apiPath = new ArrayList<>(List.of(System.getProperty("java.class.path")
                    .split(File.pathSeparator)));
            try (Stream<Path> modules = Files.list(api.classes())) {
                modules.sorted().forEach(m -> apiPath.add(m.toString()));
            }
            // the contract against the API, the runtime against the contract, the server WITHOUT the runtime
            final Path contract = output.resolve("contract-classes");
            final Path runtime = output.resolve("runtime-classes");
            final Path server = output.resolve("server-classes");
            final List<String> all = new ArrayList<>();
            all.addAll(compile(sources(generated.resolve("contract")), apiPath, contract));
            all.addAll(compile(sources(RUNTIME), plus(apiPath, contract), runtime));
            all.addAll(compile(sources(generated.resolve("server")), plus(apiPath, contract), server));
            diagnostics = all;
            final List<URL> urls = new ArrayList<>(List.of(server.toUri().toURL(), contract.toUri().toURL(),
                    runtime.toUri().toURL(), RUNTIME.resolveSibling("resources").toUri().toURL()));
            try (Stream<Path> modules = Files.list(api.classes())) {
                for (final Path module : modules.sorted().toList()) {
                    urls.add(module.toUri().toURL());
                }
            }
            loader = new URLClassLoader(urls.toArray(URL[]::new), getClass().getClassLoader());
        }

        private static List<Path> sources(final Path directory) throws IOException {
            try (Stream<Path> paths = Files.walk(directory)) {
                return paths.filter(p -> p.toString().endsWith(".java")).sorted().toList();
            }
        }

        private static List<String> plus(final List<String> classPath, final Path directory) {
            final List<String> result = new ArrayList<>(classPath);
            result.add(directory.toString());
            return result;
        }

        private static List<String> compile(final List<Path> sources, final List<String> classPath,
                                            final Path classes) throws IOException {
            Files.createDirectories(classes);
            final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            final DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
            try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(collector, Locale.ROOT,
                    StandardCharsets.UTF_8)) {
                compiler.getTask(null, fileManager, collector, List.of("-Xlint:all", "-Werror", "-proc:none",
                                "-classpath", String.join(File.pathSeparator, classPath), "-d", classes.toString()),
                        null, fileManager.getJavaFileObjectsFromPaths(sources)).call();
            }
            return collector.getDiagnostics().stream().map(Diagnostic::toString).toList();
        }

        @Test
        void theServerShouldCompileWithoutWarnings() {
            assertThat(diagnostics).isEmpty();
            assertThat(files).extracting(GeneratedFile::path).containsExactly(
                    "contract/pom.xml",
                    "contract/src/main/java/org/hiero/tck/contract/Converters.java",
                    "contract/src/main/java/org/hiero/tck/contract/Handler.java",
                    "contract/src/main/java/org/hiero/tck/contract/Session.java",
                    "contract/src/main/java/org/hiero/tck/contract/Source.java",
                    "contract/src/main/java/org/hiero/tck/contract/TckRuntime.java",
                    "contract/src/main/java/org/hiero/tck/contract/TckServer.java",
                    "server/pom.xml",
                    "server/src/main/java/org/hiero/tck/server/CommonBindings.java",
                    "server/src/main/java/org/hiero/tck/server/CryptoServiceBindings.java",
                    "server/src/main/java/org/hiero/tck/server/TckMain.java");
            assertThat(files).filteredOn(f -> f.path().endsWith(".java"))
                    .allSatisfy(f -> assertThat(f.content()).startsWith(JavaGenerator.HEADER));
            // the server knows the runtime only as runtime dependency
            assertThat(content("server/pom.xml")).containsPattern("<artifactId>hiero-sdk-tck-runtime</artifactId>"
                    + "\\s*<version>\\$\\{project.version}</version>\\s*<scope>runtime</scope>")
                    .contains("<artifactId>hiero-sdk-tck-contract</artifactId>");
            assertThat(content("contract/pom.xml")).doesNotContain("hiero-sdk-tck-runtime");
            assertThat(files).filteredOn(f -> f.path().startsWith("server/"))
                    .noneMatch(f -> f.content().contains("org.hiero.tck.runtime"));
        }

        @Test
        void theConvertersShouldBeDerivedFromTheCatalogue() {
            assertThat(content("Converters.java"))
                    .contains("    Authority key(Object json);\n")
                    .contains("    Object keyJson(Authority value);\n")
                    .contains("    NativeToken<?, ?> tinybar(Object json);\n")
                    .contains("    Integer int32(Object json);\n")
                    .contains("    byte[] hex(Object json);\n")
                    .contains("    ZonedDateTime timestamp(Object json);\n")
                    // inbound or outbound only
                    .contains("    AccountId evmAccountId(Object json);\n").doesNotContain("evmAccountIdJson")
                    .contains("    Object statusJson(TransactionStatus value);\n").doesNotContain(" status(Object");
        }

        @Test
        void theBindingsShouldBeGeneratedFromTheSpecs() {
            final String crypto = content("CryptoServiceBindings.java");
            assertThat(crypto)
                    .contains("methods.put(\"createAccount\", this::createAccount);")
                    .contains("runtime.required(runtime.value(params, List.of(new Source<>(\"key\", convert::key))), "
                            + "\"key\")")
                    .contains("runtime.value(item1, List.of(new Source<>(\"accountId\", convert::accountId), "
                            + "new Source<>(\"evmAddress\", convert::evmAccountId)))")
                    .contains("runtime.or(runtime.value(element1, List.of(new Source<>(\"approved\", convert::bool))), "
                            + "false)")
                    .contains("final Map<String, Object> commonParams = runtime.object(params, "
                            + "\"commonTransactionParams\");")
                    .contains("final var receipt = runtime.transaction(session, transaction, commonParams);")
                    .contains("runtime.put(result, \"stakingInfo.stakedNodeId\", response.stakedNodeId(), "
                            + "convert::int64Json);")
                    .contains("// not provided: ethereumNonce");
            assertThat(content("CommonBindings.java"))
                    .contains("transaction.setMemo(memo);")
                    .contains("runtime.unsupported(params, \"transactionId\"");
        }

        @Test
        void theServerShouldAnswerJsonRpcRequests() throws Exception {
            // GIVEN
            final Class<?> main = loader.loadClass("org.hiero.tck.server.TckMain");
            final Object server = main.getMethod("server").invoke(null);
            final Method handle = serverType().getMethod("handle", String.class);

            // WHEN / THEN unknown methods are skipped by the TCK
            assertThat((String) handle.invoke(server, request("unknownMethod", "{}")))
                    .contains("\"code\":-32601");
            // the API is still a stub: the SDK error becomes an internal error with the message
            assertThat((String) handle.invoke(server, request("createAccount",
                    "{\"sessionId\":\"s\",\"key\":\"302a300506032b6570032100\"}")))
                    .contains("\"code\":-32603").contains("Not implemented yet");
            assertThat((String) handle.invoke(server, request("generateKey",
                    "{\"type\":\"keyList\"}"))).contains("not supported by the API");
            assertThat((String) handle.invoke(server, "[]")).contains("\"code\":-32600");
        }

        @Test
        void theServerShouldListenOnHttp() throws Exception {
            // GIVEN
            final Object server = loader.loadClass("org.hiero.tck.server.TckMain").getMethod("server").invoke(null);
            final int port = (int) serverType().getMethod("start", int.class).invoke(server, 0);
            try (HttpClient client = HttpClient.newHttpClient()) {
                // WHEN
                final HttpResponse<String> response = client.send(HttpRequest.newBuilder(
                                URI.create("http://localhost:" + port + "/"))
                        .POST(HttpRequest.BodyPublishers.ofString(request("reset", "{\"sessionId\":\"s\"}")))
                        .build(), HttpResponse.BodyHandlers.ofString());

                // THEN
                assertThat(response.body()).isEqualTo("{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":"
                        + "{\"status\":\"SUCCESS\"}}");
            } finally {
                serverType().getMethod("stop").invoke(server);
            }
        }

        /** The server interface of the contract (the implementation of the runtime is not public). */
        private Class<?> serverType() throws ClassNotFoundException {
            return loader.loadClass("org.hiero.tck.contract.TckServer");
        }

        private String content(final String name) {
            return files.stream().filter(f -> f.path().equals(name) || f.path().endsWith("/" + name)).findFirst()
                    .orElseThrow().content();
        }

        private static String request(final String method, final String params) {
            return "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"" + method + "\",\"params\":" + params + "}";
        }
    }
}
