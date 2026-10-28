package org.hiero.sdk.v3.ledger.contract;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringWriter;
import java.lang.module.ModuleFinder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import javax.tools.ToolProvider;
import org.hiero.sdk.v3.ledger.EvmAddress;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Compiles and runs an independent named module that depends only on the public API module. */
class EvmAddressModuleConsumerTest {
    private static final String CONSUMER = """
            package consumer;

            import java.util.function.Supplier;
            import org.hiero.sdk.v3.ledger.EvmAddress;

            public final class Main implements Supplier<String> {
                public String get() {
                    byte[] raw = new byte[20];
                    raw[19] = 1;
                    EvmAddress fromBytes = EvmAddress.fromBytes(raw);
                    raw[19] = 2;
                    fromBytes.bytes()[19] = 3;
                    EvmAddress parsed = EvmAddress.fromString("0xfb6916095ca1df60bb79ce92ce3ea74c37c5d359");
                    return fromBytes.bytes()[19] + ":" + fromBytes + ":" + parsed;
                }
            }
            """;

    @Test
    void consumerCompilesAndRunsAgainstThePublicModuleOnly(@TempDir final Path dir) throws Exception {
        final var api = Path.of(EvmAddress.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        final var src = Files.createDirectories(dir.resolve("src/consumer"));
        Files.writeString(dir.resolve("src/module-info.java"), """
                module consumer {
                    requires org.hiero.sdk.v3.base;
                    exports consumer;
                }
                """);
        Files.writeString(src.resolve("Main.java"), CONSUMER);
        final var classes = Files.createDirectories(dir.resolve("classes"));

        final var compiler = ToolProvider.getSystemJavaCompiler();
        final var diagnostics = new StringWriter();
        final boolean ok = compiler.getTask(
                        diagnostics,
                        null,
                        null,
                        List.of("--module-path", api.toString(), "-d", classes.toString()),
                        null,
                        compiler.getStandardFileManager(null, null, null)
                                .getJavaFileObjects(dir.resolve("src/module-info.java"), src.resolve("Main.java")))
                .call();
        assertThat(ok).as(diagnostics.toString()).isTrue();

        final var parent = ModuleLayer.boot();
        final var configuration =
                parent.configuration().resolve(ModuleFinder.of(api, classes), ModuleFinder.of(), Set.of("consumer"));
        final var layer = ModuleLayer.defineModulesWithOneLoader(configuration, List.of(parent), null).layer();
        final var main = layer.findLoader("consumer").loadClass("consumer.Main");
        final var supplier = (Supplier<?>) main.getDeclaredConstructor().newInstance();

        assertThat(supplier.get())
                .isEqualTo("1:0x0000000000000000000000000000000000000001:0xfB6916095ca1df60bB79Ce92cE3Ea74c37c5d359");
    }
}
