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
import org.hiero.sdk.v3.ledger.Address;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Compiles and runs independent named modules that depend only on the public API module. */
class AddressModuleConsumerTest {
    private static final String MODULE_INFO = """
            module consumer {
                requires org.hiero.sdk.v3.base;
                exports consumer;
            }
            """;

    private static final String CONSUMER = """
            package consumer;

            import java.util.function.Supplier;
            import org.hiero.sdk.v3.ledger.Address;
            import org.hiero.sdk.v3.ledger.BaseAddress;
            import org.hiero.sdk.v3.ledger.Network;
            import org.hiero.sdk.v3.nativeToken.NativeTokenUnit;

            public final class Main implements Supplier<String> {
                enum U implements NativeTokenUnit {
                    BASE;
                    public String symbol() { return "b"; }
                    public long baseUnitFactor() { return 1; }
                }

                public String get() {
                    BaseAddress address = Address.fromString("0.0.123-vfmkw");
                    Network<U> mainnet = new Network<>(new byte[] {0}, "mainnet", U.BASE);
                    Network<U> testnet = new Network<>(new byte[] {1}, "testnet", U.BASE);
                    return address.validateChecksum(mainnet) + ":" + address.validateChecksum(testnet) + ":"
                            + Address.ZERO_ADDRESS + ":" + address.toStringWithChecksum();
                }
            }
            """;

    private static final String EXTENDS_ADDRESS = """
            package consumer;

            import org.hiero.sdk.v3.ledger.Address;

            public final class Main extends Address {
                Main() { super(0, 0, 0); }
            }
            """;

    private static final String PRIMITIVE_NUM_OVERRIDE = """
            package consumer;

            import org.hiero.sdk.v3.ledger.BaseAddress;
            import org.hiero.sdk.v3.ledger.Network;

            public final class Main extends BaseAddress {
                Main() { super(0, 0, null, ""); }
                public long num() { return 0; }
                public boolean validateChecksum(Network<?> n) { return false; }
                public String toString() { return ""; }
            }
            """;

    @Test
    void consumerCompilesAndRunsAgainstThePublicModuleOnly(@TempDir final Path dir) throws Exception {
        final var result = compile(dir, CONSUMER);
        assertThat(result.success()).as(result.diagnostics()).isTrue();

        final var parent = ModuleLayer.boot();
        final var configuration = parent.configuration()
                .resolve(ModuleFinder.of(apiPath(), dir.resolve("classes")), ModuleFinder.of(), Set.of("consumer"));
        final var layer = ModuleLayer.defineModulesWithOneLoader(configuration, List.of(parent), null).layer();
        final var main = layer.findLoader("consumer").loadClass("consumer.Main");
        final var supplier = (Supplier<?>) main.getDeclaredConstructor().newInstance();

        assertThat(supplier.get()).isEqualTo("true:false:0.0.0:0.0.123-vfmkw");
    }

    @Test
    void addressCannotBeExtended(@TempDir final Path dir) throws Exception {
        assertThat(compile(dir, EXTENDS_ADDRESS).success()).isFalse();
    }

    @Test
    void inheritedBoxedNumCannotBeReplacedWithAPrimitive(@TempDir final Path dir) throws Exception {
        assertThat(compile(dir, PRIMITIVE_NUM_OVERRIDE).success()).isFalse();
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
        return Path.of(Address.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    }

    private record Result(boolean success, String diagnostics) {}
}
