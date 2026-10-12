package org.hiero.sdk.v3.metalang.generator.python;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import org.hiero.sdk.v3.metalang.generator.GenerationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PythonGeneratorConfigTest {

    @TempDir
    Path temp;

    @Test
    void theDefaultShouldFollowTheHieroNamespaceGuidelines() {
        assertThat(PythonGeneratorConfig.DEFAULT.distribution()).isEqualTo("hiero-sdk");
        assertThat(PythonGeneratorConfig.DEFAULT.packageName()).isEqualTo("hiero_sdk");
        assertThat(PythonGeneratorConfig.DEFAULT.modulePath("nativeToken")).isEqualTo("hiero_sdk.native_token");
        assertThat(PythonGeneratorConfig.DEFAULT.modulePath("ledger.config")).isEqualTo("hiero_sdk.ledger.config");
    }

    @Test
    void invalidValuesShouldBeRejected() {
        assertThatThrownBy(() -> new PythonGeneratorConfig("sdk", "Hiero", "x"))
                .isInstanceOfSatisfying(GenerationException.class, e -> assertThat(e.problems()).hasSize(3)
                        .anySatisfy(p -> assertThat(p).contains("'hiero-' prefix")));
        assertThatThrownBy(() -> new PythonGeneratorConfig("hiero-sdk", "class", "1.0"))
                .isInstanceOf(GenerationException.class);
        assertThat(new PythonGeneratorConfig("hiero-sdk.core", "acme", "1.0.0rc1.dev3").version())
                .isEqualTo("1.0.0rc1.dev3");
    }

    @Test
    void shouldLoadTheConfigurationFile() throws Exception {
        // GIVEN keys of other languages are ignored
        final Path file = temp.resolve("generator.properties");
        Files.writeString(file, "go.module = x\npython.distribution = hiero-python\npython.package = hiero_py\n"
                + "python.version = 2.0.0\n");

        // THEN
        assertThat(PythonGeneratorConfig.load(file))
                .isEqualTo(new PythonGeneratorConfig("hiero-python", "hiero_py", "2.0.0"));
        Files.writeString(file, "");
        assertThat(PythonGeneratorConfig.load(file)).isEqualTo(PythonGeneratorConfig.DEFAULT);
        Files.writeString(file, "python.unknown = 1\n");
        assertThatThrownBy(() -> PythonGeneratorConfig.load(file)).isInstanceOfSatisfying(GenerationException.class,
                e -> assertThat(e.problems()).containsExactly("Unknown key 'python.unknown' (known: "
                        + "python.distribution, python.package, python.version)"));
    }
}
