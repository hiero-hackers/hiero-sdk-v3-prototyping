package org.hiero.sdk.v3.metalang.check.ts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.hiero.sdk.v3.metalang.MetaLang;
import org.hiero.sdk.v3.metalang.TestSpecs;
import org.hiero.sdk.v3.metalang.check.ApiDifference;
import org.hiero.sdk.v3.metalang.generator.GeneratedOutput;
import org.hiero.sdk.v3.metalang.generator.ts.TsGenerator;
import org.hiero.sdk.v3.metalang.generator.ts.TsGeneratorConfig;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TsConformanceTest {

    /** The installed TypeScript of the generated workspace (npm install in generated/ts). */
    private static final Path REPOSITORY = Path.of(System.getProperty("spec.root", "../../spec")).toAbsolutePath()
            .normalize().getParent();
    private static final Path TYPESCRIPT = REPOSITORY.resolve("generated/ts/node_modules/typescript");

    @TempDir
    Path temp;

    private static boolean available() {
        if (!Files.isRegularFile(TYPESCRIPT.resolve("lib/typescript.js"))) {
            return false;
        }
        try {
            return new ProcessBuilder("node", "--version").start().waitFor() == 0;
        } catch (final IOException | InterruptedException e) {
            return false;
        }
    }

    @Test
    void shouldCompareDeclarationsAndAllowAdditions() {
        // GIVEN the expected API
        final String expected = """
                D\tbase:ledger#AccountId\tclass\t<T>\tpackages/base/src/ledger/AccountId.ts\t3
                H\tbase:ledger#AccountId\timplements\tbase:ledger#Address
                M\tbase:ledger#AccountId\tproperty num\treadonly bigint\tpackages/base/src/ledger/AccountId.ts\t5
                M\tbase:ledger#AccountId\tmethod sign\t(Uint8Array): Uint8Array\tpackages/base/src/ledger/AccountId.ts\t7
                M\tbase:ledger#AccountId\tmethod sign\t(string): Uint8Array\tpackages/base/src/ledger/AccountId.ts\t8
                D\tbase:ledger#Gone\tinterface\t\tpackages/base/src/ledger/Gone.ts\t1
                D\tbase:ledger#Id\tinterface\t\tpackages/base/src/ledger/Id.ts\t1
                D\tbase:ledger#Id\tnamespace\t\tpackages/base/src/ledger/Id.ts\t9
                M\tbase:ledger#Id\tfunction parse\t(string): base:ledger#Id\tpackages/base/src/ledger/Id.ts\t10
                """;

        // WHEN the project implements the API, with additions
        final String additions = expected + """
                H\tbase:ledger#AccountId\timplements\tbase:ledger#Extra
                M\tbase:ledger#AccountId\tmethod sign\t(number): Uint8Array\tpackages/base/src/ledger/AccountId.ts\t9
                M\tbase:ledger#AccountId\tmethod extra\t(): void\tpackages/base/src/ledger/AccountId.ts\t11
                D\tbase:ledger#Extra\tinterface\t\tpackages/base/src/ledger/Extra.ts\t1
                """;

        // THEN
        assertThat(TsConformance.compare(expected, additions)).isEmpty();

        // WHEN the project changes the API
        final String changed = """
                D\tbase:ledger#AccountId\tclass\t<U>\tpackages/base/src/ledger/Account.ts\t3
                M\tbase:ledger#AccountId\tproperty num\treadonly bigint | null\tpackages/base/src/ledger/Account.ts\t5
                M\tbase:ledger#AccountId\tmethod sign\t(Uint8Array): Uint8Array\tpackages/base/src/ledger/Account.ts\t7
                D\tbase:ledger#Id\tclass\t\tpackages/base/src/ledger/Id.ts\t1
                E\tpackages/base/src/ledger/Broken.ts\t2\t'}' expected.
                """;

        // THEN
        assertThat(TsConformance.compare(expected, changed)).extracting(ApiDifference::toString).containsExactly(
                "An interface Gone (base/ledger) is missing (expected in packages/base/src/ledger/Gone.ts)",
                "packages/base/src/ledger/Account.ts:3: AccountId (base/ledger) must implement Address",
                "packages/base/src/ledger/Account.ts:3: Class AccountId (base/ledger) has different type "
                        + "parameters: expected '<T>', found '<U>'",
                "packages/base/src/ledger/Account.ts:5: Property num of AccountId (base/ledger) has a different "
                        + "declaration: expected 'readonly bigint', found 'readonly bigint | null'",
                "packages/base/src/ledger/Account.ts:7: Method sign of AccountId (base/ledger) has a different "
                        + "declaration: expected '(string): Uint8Array', found '(Uint8Array): Uint8Array'",
                "packages/base/src/ledger/Broken.ts:2: Cannot parse: '}' expected.",
                "packages/base/src/ledger/Id.ts:1: Function parse of Id (base/ledger) is missing",
                "packages/base/src/ledger/Id.ts:1: Id (base/ledger) must be a namespace, found a class",
                "packages/base/src/ledger/Id.ts:1: Id (base/ledger) must be an interface, found a class");
    }

    @Test
    void shouldReportAMissingTypeScriptInstallation() {
        assertThatThrownBy(() -> TsConformance.check(LinkedModel.of(new MetaLang().validate(Map.of("f/a.md",
                TestSpecs.markdown("namespace a\nX { @@immutable x: int32 }\n"))).model()), new TsGenerator(), temp,
                null)).isInstanceOf(IOException.class).hasMessageContaining("TypeScript not found in");
    }

    @Test
    void shouldAcceptTheGeneratedCodeAndAnImplementationButNotAnOutdatedProject() throws Exception {
        assumeTrue(available(), "node and generated/ts/node_modules (npm install) are needed");
        // GIVEN the generated code of a spec
        final TsGenerator generator = new TsGenerator();
        final String schema = """
                namespace a
                Client {
                    @@immutable name: string
                    @@async string fetch(id: int64)
                }
                """;
        final LinkedModel model = LinkedModel.of(new MetaLang().validate(Map.of("f/a.md",
                TestSpecs.markdown(schema))).model());
        GeneratedOutput.write(temp, generator.generate(model), TsGenerator.MARKER);
        final Path client = temp.resolve("packages/f/src/a/Client.ts");

        // THEN as generated
        assertThat(TsConformance.check(model, generator, temp, TYPESCRIPT).differences()).isEmpty();

        // WHEN implemented
        Files.writeString(client, Files.readString(client, StandardCharsets.UTF_8).replace(
                "throw new Error(\"Not implemented yet: Client.fetch\");", "return Promise.resolve(String(id));"),
                StandardCharsets.UTF_8);

        // THEN
        assertThat(TsConformance.check(model, generator, temp, TYPESCRIPT).differences()).isEmpty();

        // WHEN the spec changes
        final LinkedModel changed = LinkedModel.of(new MetaLang().validate(Map.of("f/a.md", TestSpecs.markdown(
                schema.replace("fetch(id: int64)", "fetch(id: int64, retries: int32)")))).model());
        final TsConformance.Result result = TsConformance.check(changed, generator, temp, TYPESCRIPT);

        // THEN
        assertThat(result.differences()).extracting(ApiDifference::message).containsExactly(
                "Method fetch of Client (f/a) has a different declaration: expected '(bigint, number): "
                        + "Promise<string>', found '(bigint): Promise<string>'");
        assertThat(result.packages()).isEqualTo(1);
    }

    @Test
    void generatedTsShouldProvideTheApiOfTheSpecs() throws Exception {
        assumeTrue(available(), "node and generated/ts/node_modules (npm install) are needed");
        final Path specs = REPOSITORY.resolve("spec");
        final TsConformance.Result result = TsConformance.check(LinkedModel.of(new MetaLang().validate(specs)
                        .model()), new TsGenerator(TsGeneratorConfig.load(REPOSITORY.resolve("sdk-ts/generator.properties"))),
                REPOSITORY.resolve("generated/ts"), null);
        // generated/ts is up to date (otherwise: regenerate it, see tooling/metalang/README.md)
        assertThat(result.differences()).isEmpty();
        assertThat(result.declarations()).isPositive();
    }
}
