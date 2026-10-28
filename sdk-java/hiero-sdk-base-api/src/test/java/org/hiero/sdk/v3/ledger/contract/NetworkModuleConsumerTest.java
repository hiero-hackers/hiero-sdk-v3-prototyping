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
import org.hiero.sdk.v3.ledger.Network;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Compiles and runs independent named modules that depend only on the public API module. */
class NetworkModuleConsumerTest {
    private static final String MODULE_INFO = """
            module consumer {
                requires org.hiero.sdk.v3.base;
                exports consumer;
            }
            """;

    private static final String CONSUMER = """
            package consumer;

            import java.util.function.Supplier;
            import org.hiero.sdk.v3.ledger.Network;
            import org.hiero.sdk.v3.nativeToken.NativeTokenUnit;

            public final class Main implements Supplier<String> {
                enum U implements NativeTokenUnit {
                    BASE;
                    public String symbol() { return "b"; }
                    public long baseUnitFactor() { return 1; }
                }

                public String get() {
                    byte[] source = {0, 7};
                    Network<U> network = new Network<>(source, null, U.BASE);
                    source[1] = 9;
                    network.id()[0] = 5;
                    U unit = network.nativeTokenUnit();
                    return network.id().length + ":" + network.id()[1] + ":" + network.name() + ":" + unit.symbol();
                }
            }
            """;

    private static final String NON_UNIT_ARGUMENT = CONSUMER.replace(
            "Network<U> network = new Network<>(source, null, U.BASE);",
            "Network<String> network = null;");

    private static final String UNRELATED_UNIT_ASSIGNMENT = CONSUMER.replace(
            "U unit = network.nativeTokenUnit();",
            "Integer unit = network.nativeTokenUnit();");

    @Test
    void consumerCompilesAndRunsAgainstThePublicModuleOnly(@TempDir final Path dir) throws Exception {
        final var result = compile(dir, CONSUMER);
        assertThat(result.success()).as(result.diagnostics()).isTrue();

        final var api = apiPath();
        final var parent = ModuleLayer.boot();
        final var configuration = parent.configuration()
                .resolve(ModuleFinder.of(api, dir.resolve("classes")), ModuleFinder.of(), Set.of("consumer"));
        final var layer = ModuleLayer.defineModulesWithOneLoader(configuration, List.of(parent), null).layer();
        final var main = layer.findLoader("consumer").loadClass("consumer.Main");
        final var supplier = (Supplier<?>) main.getDeclaredConstructor().newInstance();

        assertThat(supplier.get()).isEqualTo("2:7:null:b");
    }

    @Test
    void compilerRejectsNonUnitTypeArguments(@TempDir final Path dir) throws Exception {
        assertThat(compile(dir, NON_UNIT_ARGUMENT).success()).isFalse();
    }

    @Test
    void compilerRejectsAssignmentToAnUnrelatedUnitType(@TempDir final Path dir) throws Exception {
        assertThat(compile(dir, UNRELATED_UNIT_ASSIGNMENT).success()).isFalse();
    }

    private static Result compile(final Path dir, final String source) throws Exception {
        final var src = Files.createDirectories(dir.resolve("src/consumer"));
        Files.writeString(dir.resolve("src/module-info.java"), MODULE_INFO);
        Files.writeString(src.resolve("Main.java"), source);
        final var classes = Files.createDirectories(dir.resolve("classes"));

        final var compiler = ToolProvider.getSystemJavaCompiler();
        final var diagnostics = new StringWriter();
        final boolean ok = compiler.getTask(
                        diagnostics,
                        null,
                        null,
                        List.of("--module-path", apiPath().toString(), "-d", classes.toString()),
                        null,
                        compiler.getStandardFileManager(null, null, null)
                                .getJavaFileObjects(dir.resolve("src/module-info.java"), src.resolve("Main.java")))
                .call();
        return new Result(ok, diagnostics.toString());
    }

    private static Path apiPath() throws Exception {
        return Path.of(Network.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    }

    private record Result(boolean success, String diagnostics) {}
}
