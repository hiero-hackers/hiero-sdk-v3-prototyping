package org.hiero.sdk.v3.metalang.check.java;

import org.hiero.sdk.v3.metalang.check.ApiDifference;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.hiero.sdk.v3.metalang.MetaLang;
import org.hiero.sdk.v3.metalang.TestSpecs;
import org.hiero.sdk.v3.metalang.generator.GeneratedOutput;
import org.hiero.sdk.v3.metalang.generator.java.JavaGenerator;
import org.hiero.sdk.v3.metalang.generator.java.JavaGeneratorConfig;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JavaConformanceTest {

    @TempDir
    Path temp;

    private static LinkedModel model(final String schema) {
        return LinkedModel.of(new MetaLang().validate(Map.of("f/a.md", TestSpecs.markdown(schema))).model());
    }

    @Test
    void shouldAcceptTheGeneratedCodeAndAnImplementationButNotAnOutdatedProject() throws Exception {
        // GIVEN the generated code of a spec
        final JavaGenerator generator = new JavaGenerator();
        final String schema = """
                namespace a
                abstraction Client {
                    @@immutable name: string
                    @@async string fetch(id: int64)
                }
                """;
        GeneratedOutput.write(temp, generator.generate(model(schema)), JavaGenerator.MARKER);
        final Path client = temp.resolve("org.hiero.f/src/main/java/org/hiero/a/Client.java");

        // WHEN checked as generated
        final JavaConformance.Result generated = JavaConformance.check(model(schema), generator, temp);

        // THEN
        assertThat(generated.differences()).isEmpty();
        assertThat(generated.types()).isEqualTo(1);
        assertThat(generated.modules()).isEqualTo(1);

        // WHEN the project is implemented: a method body and a helper are added
        Files.writeString(client, Files.readString(client)
                .replace("public abstract CompletionStage<String> fetch(final long id);",
                        "public CompletionStage<String> fetch(final long id) { return helper(); }\n\n"
                                + "    private CompletionStage<String> helper() { return null; }"));

        // THEN it still provides the API
        assertThat(JavaConformance.check(model(schema), generator, temp).differences()).isEmpty();

        // WHEN the spec changes, but the project is not updated
        final JavaConformance.Result outdated = JavaConformance.check(model(schema
                .replace("fetch(id: int64)", "fetch(id: int64, retries: int32)")
                .replace("}\n", "    void close()\n}\n")), generator, temp);

        // THEN
        assertThat(outdated.differences()).extracting(ApiDifference::message).containsExactly(
                "Method org.hiero.a.Client.close() is missing",
                "Method org.hiero.a.Client.fetch(long, int) is missing");
    }

    @Test
    void generatedJavaShouldProvideTheApiOfTheSpecs() throws Exception {
        // GIVEN the specs, the configuration and the generated code of the repository
        final Path specs = Path.of(System.getProperty("spec.root", "../../spec"));
        final Path repository = specs.toAbsolutePath().normalize().getParent();
        final JavaGenerator generator = new JavaGenerator(JavaGeneratorConfig.load(
                repository.resolve("sdk-java/generator.properties")));

        // WHEN
        final JavaConformance.Result result = JavaConformance.check(LinkedModel.of(new MetaLang().validate(specs)
                .model()), generator, repository.resolve("generated/java"));

        // THEN generated/java is up to date (otherwise: regenerate it, see tooling/metalang/README.md)
        assertThat(result.differences()).isEmpty();
        assertThat(result.types()).isPositive();
    }
}
