package org.hiero.sdk.v3.contract;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConsumerCompilationContractTest {
    @TempDir
    Path outputDirectory;

    @Test
    void shouldCompileRepresentativeConsumerFromAllElevenNamespaces() {
        final String source = """
                import java.util.List;
                import java.util.concurrent.CompletionStage;
                import org.hiero.sdk.v3.authority.Authority;
                import org.hiero.sdk.v3.common.Page;
                import org.hiero.sdk.v3.grpc.MethodDescriptor;
                import org.hiero.sdk.v3.hedera.HbarUnit;
                import org.hiero.sdk.v3.keys.KeyAlgorithm;
                import org.hiero.sdk.v3.ledger.Address;
                import org.hiero.sdk.v3.ledger.Network;
                import org.hiero.sdk.v3.ledger.config.NetworkSetting;
                import org.hiero.sdk.v3.nativetoken.NativeTokenUnit;
                import org.hiero.sdk.v3.proto.ProtoNamespace;
                import org.hiero.sdk.v3.solo.SoloConstants;
                import org.hiero.sdk.v3.token.TokenType;

                final class Consumer {
                    void use(Page<String> page) {
                        List<String> values = page.data();
                        CompletionStage<Page<String>> next = page.next();
                        MethodDescriptor method = new MethodDescriptor("service", "method");
                        Network<HbarUnit> network = new Network<>(new byte[] {1}, null, HbarUnit.HBAR);
                        Class<?>[] anchors = {Authority.class, KeyAlgorithm.class, Address.class,
                                NetworkSetting.class, NativeTokenUnit.class, ProtoNamespace.class, TokenType.class};
                        String solo = SoloConstants.SOLO_IDENTIFIER;
                    }
                }
                """;
        assertThat(compiles("Consumer", source)).isTrue();
    }

    @Test
    void shouldRejectPrivateKeyAsAuthorityLeaf() {
        final String source = """
                import org.hiero.sdk.v3.authority.Authority;
                import org.hiero.sdk.v3.keys.PrivateKey;
                final class InvalidAuthority {
                    Authority invalid(PrivateKey key) { return Authority.of(key); }
                }
                """;
        assertThat(compiles("InvalidAuthority", source)).isFalse();
    }

    @Test
    void shouldRejectExternalAuthorityVariantAndInvalidGenericBound() {
        final String sealedSource = """
                import org.hiero.sdk.v3.authority.Authority;
                final class ExternalAuthority implements Authority {}
                """;
        final String genericSource = """
                import org.hiero.sdk.v3.ledger.Network;
                final class InvalidNetwork { Network<String> value; }
                """;
        assertThat(compiles("ExternalAuthority", sealedSource)).isFalse();
        assertThat(compiles("InvalidNetwork", genericSource)).isFalse();
    }

    private boolean compiles(final String className, final String source) {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        final JavaFileObject sourceFile = new SourceFile(className, source);
        final List<String> options = List.of(
                "--release", "21",
                "-classpath", System.getProperty("java.class.path"),
                "-d", outputDirectory.toString());
        try (StandardJavaFileManager files = compiler.getStandardFileManager(diagnostics, null, null)) {
            return Boolean.TRUE.equals(compiler.getTask(null, files, diagnostics, options, null, List.of(sourceFile)).call());
        } catch (Exception exception) {
            throw new IllegalStateException("consumer compilation failed unexpectedly", exception);
        }
    }

    private static final class SourceFile extends SimpleJavaFileObject {
        private final String source;

        private SourceFile(final String className, final String source) {
            super(URI.create("string:///" + className + Kind.SOURCE.extension), Kind.SOURCE);
            this.source = source;
        }

        @Override
        public CharSequence getCharContent(final boolean ignoreEncodingErrors) {
            return source;
        }
    }
}
