package org.hiero.sdk.v3.metalang.generator.ts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assumptions.assumeThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.hiero.sdk.v3.metalang.MetaLang;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.generator.GenerationException;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.hiero.sdk.v3.metalang.tck.TckBindings;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TsTckGeneratorTest {

    private static final Path SPECS = Path.of(System.getProperty("spec.root", "../../../spec")).toAbsolutePath()
            .normalize();
    private static final Path REPOSITORY = SPECS.getParent();

    private static LinkedModel model;
    private static List<GeneratedFile> files;

    @BeforeAll
    static void generate() throws IOException {
        model = LinkedModel.of(new MetaLang().validate(SPECS).model());
        files = new TsTckGenerator(TsGeneratorConfig.DEFAULT, "../../generated")
                .generate(model, TckBindings.read(REPOSITORY.resolve("tck/bindings"), model));
    }

    private static String content(final String path) {
        return files.stream().filter(f -> f.path().equals(path)).findFirst().orElseThrow().content();
    }

    @Test
    void shouldGenerateTheContractAndTheServer() {
        assertThat(files).extracting(GeneratedFile::path).containsExactly(
                "contract/package.json",
                "contract/src/Converters.ts",
                "contract/src/TckRuntime.ts",
                "contract/src/index.ts",
                "contract/tsconfig.json",
                "server/package.json",
                "server/src/CommonBindings.ts",
                "server/src/CryptoServiceBindings.ts",
                "server/src/index.ts",
                "server/src/main.ts",
                "server/src/server.ts",
                "server/tsconfig.json");
        assertThat(files).allMatch(f -> f.content().lines().findFirst().orElseThrow().contains(TsGenerator.MARKER));
        assertThat(TsTckGenerator.className("services/token_service.md")).isEqualTo("TokenServiceBindings");
    }

    @Test
    void theConvertersShouldBeDerivedFromTheCatalogue() {
        assertThat(content("contract/src/Converters.ts"))
                .contains("    key(json: unknown): Authority;\n")
                .contains("    keyJson(value: Authority): unknown;\n")
                .contains("    tinybar(json: unknown): NativeToken<any, NativeTokenUnit>;\n")
                .contains("    int64(json: unknown): bigint;\n")
                .contains("    seconds(json: unknown): Duration;\n")
                .contains("    timestamp(json: unknown): Date;\n")
                .contains("    evmAccountId(json: unknown): AccountId;\n").doesNotContain("evmAccountIdJson")
                .contains("    statusJson(value: TransactionStatus): unknown;\n").doesNotContain(" status(json")
                .contains("import type { Duration } from \"@hiero/support\";");
        assertThat(content("contract/package.json")).contains("\"@hiero/support\": \"0.1.0-SNAPSHOT\"")
                .contains("\"name\": \"@hiero/tck-contract\"").doesNotContain("tck-runtime");
        assertThat(content("contract/tsconfig.json")).contains("\"extends\": \"../../../generated/tsconfig.base.json\"")
                .contains("{ \"path\": \"../../../support\" }")
                .contains("{ \"path\": \"../../../generated/packages/base\" }");
    }

    @Test
    void theServerShouldDependOnTheRuntimeOnlyAtRuntime() {
        assertThat(content("server/package.json")).contains("\"@hiero/tck-runtime\": \"0.1.0-SNAPSHOT\"")
                .contains("\"@hiero/tck-contract\": \"0.1.0-SNAPSHOT\"");
        assertThat(content("server/tsconfig.json")).contains("{ \"path\": \"../contract\" }")
                .doesNotContain("runtime");
        assertThat(files).filteredOn(f -> f.path().startsWith("server/src/"))
                .noneMatch(f -> f.content().contains("from \"@hiero/tck-runtime\""));
        assertThat(content("server/src/server.ts"))
                .contains("const RUNTIME: string = process.env[\"TCK_RUNTIME\"] ?? \"@hiero/tck-runtime\";")
                .contains("(await import(RUNTIME)) as TckRuntimeModule");
    }

    @Test
    void theBindingsShouldBeGeneratedFromTheSpecs() {
        assertThat(content("server/src/CryptoServiceBindings.ts"))
                .contains("methods.set(\"createAccount\", (params, session) => this.createAccount(params, session));")
                .contains("authority: this.#runtime.required(this.#runtime.value(params, [{ path: \"key\", "
                        + "converter: (json) => this.#convert.key(json) }]), \"key\")")
                .contains("receiverSignatureRequired: this.#runtime.or(")
                .contains("for (const element1 of this.#runtime.objects(params, \"transfers\")) {")
                .contains("const item1 = this.#runtime.object(element1, \"hbar\");")
                .contains("this.#runtime.value(item1, [{ path: \"accountId\", converter: (json) => "
                        + "this.#convert.accountId(json) }, { path: \"evmAddress\", converter: (json) => "
                        + "this.#convert.evmAccountId(json) }])")
                .contains("const receipt = await this.#runtime.transaction(session, transaction, commonParams);")
                .contains("const response = await this.#runtime.query(session, query);")
                .contains("this.#runtime.put(result, \"stakingInfo.stakedNodeId\", response.stakedNodeId, "
                        + "(value) => this.#convert.int64Json(value));")
                .contains("// not provided: ethereumNonce");
        assertThat(content("server/src/CommonBindings.ts")).contains("transaction.memo = memo;")
                .contains("this.#runtime.unsupported(params, \"transactionId\"");
    }

    @Test
    void bindingsWithErrorsShouldBeRejected() {
        final TckBindings.Bindings broken = TckBindings.resolve(Map.of("broken.md",
                "```bindings\nbinding foo -> UnknownType {\n}\n```\n"), model);
        assertThatThrownBy(() -> new TsTckGenerator(TsGeneratorConfig.DEFAULT, "../../generated").generate(model, broken))
                .isInstanceOf(GenerationException.class).hasMessageContaining("UnknownType");
        assertThat(new TsTckGenerator(new TsGeneratorConfig("@hiero", "0.1.0-SNAPSHOT", "/opt/support"),
                "/work/ts").generate(model, TckBindings.resolve(Map.of(), model)))
                .filteredOn(f -> f.path().equals("contract/tsconfig.json")).singleElement()
                .satisfies(f -> assertThat(f.content()).contains("\"extends\": \"/work/ts/tsconfig.base.json\"")
                        .contains("{ \"path\": \"/opt/support\" }"));
    }

    /** A model with the forms the repository bindings do not use (yet). */
    private static LinkedModel shop() {
        return LinkedModel.of(new MetaLang().validate(Map.of(
                "f/tx.md", org.hiero.sdk.v3.metalang.TestSpecs.markdown("""
                        namespace consensusnode.transactions
                        abstraction Receipt { @@immutable status: string }
                        abstraction Transaction { @@nullable memo: string }
                        """),
                "f/q.md", org.hiero.sdk.v3.metalang.TestSpecs.markdown("""
                        namespace consensusnode.queries
                        abstraction QueryResponse { @@immutable answer: string }
                        abstraction Query { @@immutable id: string }
                        """),
                "f/shop.md", org.hiero.sdk.v3.metalang.TestSpecs.markdown("""
                        namespace shop
                        requires {Receipt, Transaction} from consensusnode.transactions
                        Response<$$R> { @@immutable value: $$R }
                        Detail { @@immutable note: string }
                        OrderReceipt extends Receipt {
                            @@immutable orderId: string
                            @@immutable @@nullable detail: Detail
                        }
                        Item {
                            @@immutable name: string
                            @@immutable @@default(false) gift: bool
                        }
                        Group { @@immutable @@default([]) items: list<Item> }
                        OrderTransaction extends Transaction {
                            @@immutable customer: string
                            @@immutable @@nullable note: string
                            @@immutable @@default([]) items: list<Item>
                            @@immutable @@default([]) groups: list<Group>
                            Response<OrderReceipt> signWithOperatorAndSubmit()
                        }
                        Empty extends Transaction { Response<OrderReceipt> signWithOperatorAndSubmit() }
                        Strict extends Transaction {
                            count: int32
                            Response<OrderReceipt> signWithOperatorAndSubmit()
                        }
                        Broken extends Transaction {
                            @@immutable @@nullable callback: Unknown
                            Response<OrderReceipt> signWithOperatorAndSubmit()
                        }
                        """))).model());
    }

    private static TckBindings.Bindings shopBindings(final LinkedModel shop, final String bindings) {
        return TckBindings.resolve(Map.of("shop--orders.md", "```bindings\nrequires {OrderTransaction, Item, Group, "
                + "Empty, Strict, Broken} from shop\n" + bindings + "```\n"), shop);
    }

    @Test
    void allBindingFormsShouldBeGenerated() {
        // GIVEN
        final LinkedModel shop = shop();

        // WHEN
        final List<GeneratedFile> generated = new TsTckGenerator(TsGeneratorConfig.DEFAULT, "../../generated").generate(shop,
                shopBindings(shop, """
                        binding createOrder -> OrderTransaction {
                            customer = customer : string | name : string
                            note = note : string
                            items = each items -> Item {
                                name = name : string | ^label : string
                            }
                            groups = each groups -> Group {
                                items = each entries.item -> Item {
                                    name = name : string
                                }
                            }
                            result note = receipt.detail.note : string
                        }
                        binding empty -> Empty {
                        }
                        common c -> OrderTransaction {
                            memo = memo : string
                        }
                        """));

        // THEN
        final String code = generated.stream().filter(f -> f.path().endsWith("/ShopOrdersBindings.ts")).findFirst()
                .orElseThrow().content();
        assertThat(code)
                .contains("const item1 = element1;")
                .contains("this.#runtime.or(this.#runtime.value(item1, [{ path: \"name\", converter: (json) => "
                        + "this.#convert.string(json) }]), this.#runtime.value(element1, [{ path: \"label\", "
                        + "converter: (json) => this.#convert.string(json) }]))")
                .contains("const items2: Item[] = [];")
                .contains("const item2 = this.#runtime.object(element2, \"item\");")
                .contains("receipt.detail?.note")
                .contains("const transaction = new Empty({});");
        // converters whose type the API does not have cannot be used, so the contract leaves them out
        assertThat(generated.stream().filter(f -> f.path().endsWith("/Converters.ts")).findFirst().orElseThrow()
                .content()).contains("    string(json: unknown): string;\n").doesNotContain("accountId");
    }

    @Test
    void requestsThatCannotBeBuiltShouldBeRejected() {
        final LinkedModel shop = shop();
        assertThatThrownBy(() -> new TsTckGenerator(TsGeneratorConfig.DEFAULT, "../../generated").generate(shop,
                shopBindings(shop, "binding broken -> Broken {\n}\nbinding strict -> Strict {\n}\n")))
                .isInstanceOfSatisfying(GenerationException.class, e -> assertThat(e.problems()).containsExactly(
                        "shop--orders.md: Type 'shop.Broken' is not a class of the generated API"));
        assertThatThrownBy(() -> new TsTckGenerator(TsGeneratorConfig.DEFAULT, "../../generated").generate(shop,
                shopBindings(shop, "binding strict -> Strict {\n}\n")))
                .isInstanceOfSatisfying(GenerationException.class, e -> assertThat(e.problems()).containsExactly(
                        "shop--orders.md: Required attribute 'count' of 'shop.Strict' is not bound"));
        // a model without the transaction types of the contract
        final LinkedModel bare = LinkedModel.of(new MetaLang().validate(Map.of("f/a.md",
                org.hiero.sdk.v3.metalang.TestSpecs.markdown("namespace a\nX { @@immutable x: int32 }\n"))).model());
        assertThatThrownBy(() -> new TsTckGenerator(TsGeneratorConfig.DEFAULT, "../../generated").generate(bare,
                TckBindings.resolve(Map.of(), bare)))
                .isInstanceOfSatisfying(GenerationException.class, e -> assertThat(e.problems())
                        .anySatisfy(p -> assertThat(p).startsWith("contract: "))
                        .anySatisfy(p -> assertThat(p).startsWith("common transaction parameters: ")));
    }

    /**
     * Builds the repository layout in a temporary directory — generated API, support package, contract, server and
     * the runtime of the repository below one node_modules — compiles the server without the runtime, then the
     * runtime, and sends requests to the server.
     */
    @Test
    void theServerShouldBuildAndAnswerRequests(@TempDir final Path temp) throws Exception {
        assumeThat(GeneratedTs.available()).as("Node.js and TypeScript in sdk-ts/node_modules").isTrue();
        // GIVEN
        write(new TsGenerator().generate(model), temp.resolve("sdk-ts/generated"));
        write(files, temp.resolve("sdk-ts/tck/generated"));
        copy(GeneratedTs.SUPPORT, temp.resolve("sdk-ts/support"));
        copy(REPOSITORY.resolve("sdk-ts/tck/runtime"), temp.resolve("sdk-ts/tck/runtime"));
        final Path modules = temp.resolve("node_modules");
        Files.createDirectories(modules.resolve("@hiero"));
        Files.createSymbolicLink(modules.resolve("typescript"), GeneratedTs.MODULES.resolve("typescript"));
        Files.createSymbolicLink(modules.resolve("@types"), GeneratedTs.MODULES.resolve("@types"));
        try (var packages = Files.list(temp.resolve("sdk-ts/generated/packages"))) {
            for (final Path pkg : packages.toList()) {
                Files.createSymbolicLink(modules.resolve("@hiero").resolve(pkg.getFileName()), pkg);
            }
        }
        Files.createSymbolicLink(modules.resolve("@hiero/support"), temp.resolve("sdk-ts/support"));
        Files.createSymbolicLink(modules.resolve("@hiero/tck-contract"), temp.resolve("sdk-ts/tck/generated/contract"));
        Files.createSymbolicLink(modules.resolve("@hiero/tck-server"), temp.resolve("sdk-ts/tck/generated/server"));

        // WHEN the server is compiled while the runtime is not even installed
        assertThat(run(temp, "node", GeneratedTs.MODULES.resolve("typescript/bin/tsc").toString(), "--build",
                "sdk-ts/tck/generated/server")).isEmpty();
        Files.createSymbolicLink(modules.resolve("@hiero/tck-runtime"), temp.resolve("sdk-ts/tck/runtime"));
        assertThat(run(temp, "node", GeneratedTs.MODULES.resolve("typescript/bin/tsc").toString(), "--build",
                "sdk-ts/tck/runtime")).isEmpty();
        final String output = run(temp, "node", "--input-type=module", "-e", """
                import { createServer } from "@hiero/tck-server";
                const server = await createServer();
                for (const request of [
                        '{"jsonrpc":"2.0","id":1,"method":"reset","params":{"sessionId":"s"}}',
                        '{"jsonrpc":"2.0","id":2,"method":"unknownMethod","params":{}}',
                        '{"jsonrpc":"2.0","id":3,"method":"createAccount","params":{"sessionId":"s","key":"302a"}}',
                        '{"jsonrpc":"2.0","id":4,"method":"generateKey","params":{"type":"keyList"}}']) {
                    console.log(await server.handle(request));
                }
                """);

        // THEN
        assertThat(output).contains("{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":{\"status\":\"SUCCESS\"}}")
                .contains("\"id\":2,\"error\":{\"code\":-32601")
                .contains("\"id\":3,\"error\":{\"code\":-32603").contains("Not implemented yet")
                .contains("not supported by the API");
    }

    private static void write(final List<GeneratedFile> generated, final Path directory) throws IOException {
        for (final GeneratedFile file : generated) {
            final Path target = directory.resolve(file.path());
            Files.createDirectories(target.getParent());
            Files.writeString(target, file.content(), StandardCharsets.UTF_8);
        }
    }

    /** Copies the sources of a hand-written package (without build output and installed modules). */
    private static void copy(final Path source, final Path target) throws IOException {
        try (var paths = Files.walk(source)) {
            for (final Path path : paths.filter(Files::isRegularFile).filter(p -> !source.relativize(p).toString()
                    .matches("(dist|node_modules)/.*|.*\\.tsbuildinfo")).toList()) {
                final Path copy = target.resolve(source.relativize(path).toString());
                Files.createDirectories(copy.getParent());
                Files.copy(path, copy);
            }
        }
    }

    /** Runs a command; returns its output, or an empty string if a build succeeds without output. */
    private static String run(final Path directory, final String... command) throws Exception {
        final Process process = new ProcessBuilder(command).directory(directory.toFile()).redirectErrorStream(true)
                .start();
        final String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        final int exit = process.waitFor();
        assertThat(exit).as(output).isZero();
        return output;
    }
}
