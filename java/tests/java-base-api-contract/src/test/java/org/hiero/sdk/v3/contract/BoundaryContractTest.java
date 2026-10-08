package org.hiero.sdk.v3.contract;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.hiero.sdk.v3.authority.Authority;
import org.hiero.sdk.v3.keys.PrivateKey;
import org.junit.jupiter.api.Test;

class BoundaryContractTest {
    private static final List<String> FORBIDDEN_SOURCE_MARKERS = List.of(
            "UnsupportedOperationException",
            "ServiceLoader",
            "java.net.",
            "java.security.",
            "javax.crypto.",
            "Executors.",
            "HttpClient",
            "Socket",
            "Provider(");

    @Test
    void shouldRejectOperationalImplementationPatterns() throws IOException {
        for (Path source : ContractTestSupport.javaSources()) {
            final String content = Files.readString(source);
            assertThat(FORBIDDEN_SOURCE_MARKERS)
                    .as("forbidden operational marker in %s", source)
                    .noneMatch(content::contains);
        }
    }

    @Test
    void shouldExcludePrivateKeysAndProviders() throws IOException, ClassNotFoundException {
        assertThat(Authority.class.isAssignableFrom(PrivateKey.class)).isFalse();
        assertThat(Authority.class.getPermittedSubclasses())
                .extracting(Class::getSimpleName)
                .containsExactlyInAnyOrder("PublicKeyAuthority", "ContractAuthority", "AuthorityList");
        assertThat(ContractTestSupport.apiTypes())
                .extracting(Class::getName)
                .noneMatch(name -> name.contains("Provider") || name.contains("Implementation") || name.endsWith("Impl"));
    }

    @Test
    void shouldKeepAllApiStateFinal() throws IOException, ClassNotFoundException {
        for (Class<?> type : ContractTestSupport.apiTypes()) {
            for (Field field : type.getDeclaredFields()) {
                if (!field.isSynthetic()) {
                    assertThat(Modifier.isFinal(field.getModifiers()))
                            .as("field must be final: %s.%s", type.getName(), field.getName())
                            .isTrue();
                }
            }
        }
    }

    @Test
    void shouldHaveNoOperationalRuntimeDependency() throws IOException {
        final String pom = Files.readString(ContractTestSupport.JAVA_ROOT.resolve("hiero-sdk-base-api/pom.xml"));
        assertThat(pom).contains("<artifactId>jspecify</artifactId>", "<scope>provided</scope>");
        assertThat(pom).doesNotContain("protobuf", "grpc-", "bouncycastle", "netty", "implementation");
    }

    @Test
    void shouldKeepImplementationTestsAndToolingOutOfThePublishedJar() throws IOException {
        final Path jarPath = ContractTestSupport.JAVA_ROOT.resolve(
                "hiero-sdk-base-api/target/hiero-sdk-base-api-0.1.0-SNAPSHOT.jar");
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            final List<String> entries = jar.stream().map(JarEntry::getName).toList();
            assertThat(entries).contains("module-info.class");
            assertThat(entries)
                    .filteredOn(name -> name.endsWith(".class") && !name.equals("module-info.class"))
                    .allMatch(name -> name.startsWith("org/hiero/sdk/v3/"))
                    .noneMatch(name -> name.contains("/contract/")
                            || name.contains("/tools/")
                            || name.contains("/internal/")
                            || name.contains("Provider")
                            || name.endsWith("Impl.class"));
        }
    }

    @Test
    void shouldKeepReviewedPublicSignatureSnapshot() throws IOException, ClassNotFoundException {
        assertThat(ContractTestSupport.publicSignatureHash())
                .isEqualTo("70c808536113d323d963595265c0a2d3608411c6ce3c9e51fa630570ec5f0372");
    }
}
