package org.hiero.sdk.v3.nativeToken.contract;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.module.ModuleFinder;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import javax.tools.ToolProvider;
import org.hiero.sdk.v3.nativeToken.NativeToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Compiles and runs an independent named module that depends only on the public API module. */
class NativeTokenModuleConsumerTest {
    private static final String MODULE_INFO = """
            module consumer {
                requires org.hiero.sdk.v3.base;
                exports consumer;
            }
            """;

    private static final String CONSUMER = """
            package consumer;

            import java.util.function.Supplier;
            import org.hiero.sdk.v3.nativeToken.NativeToken;
            import org.hiero.sdk.v3.nativeToken.NativeTokenUnit;

            public final class Main implements Supplier<String> {
                enum U implements NativeTokenUnit {
                    BASE("b", 1), TEN("t", 10);
                    private final String s; private final long f;
                    U(String s, long f) { this.s = s; this.f = f; }
                    public String symbol() { return s; }
                    public long baseUnitFactor() { return f; }
                }

                static final class T extends NativeToken<T, U> {
                    T(long a, U u) { super(a, u); }
                    public T to(U target) { return new T(toBaseUnits() / target.baseUnitFactor(), target); }
                    public long toBaseUnits() { return amount() * unit().baseUnitFactor(); }
                }

                public String get() {
                    T converted = new T(3, U.TEN).to(U.BASE);
                    return converted.amount() + converted.unit().symbol();
                }
            }
            """;

    private static final String UNRELATED_UNIT = CONSUMER.replace(
            "T converted = new T(3, U.TEN).to(U.BASE);",
            "T converted = new T(3, U.TEN).to(new org.hiero.sdk.v3.nativeToken.NativeTokenUnit() {"
                    + " public String symbol() { return \"x\"; } public long baseUnitFactor() { return 1; } });");

    @Test
    void consumerCompilesAndRunsAgainstThePublicModuleOnly(@TempDir final Path dir) throws Exception {
        final var out = compile(dir, CONSUMER);

        assertThat(out.success()).as(out.diagnostics()).isTrue();

        final var finder = ModuleFinder.of(apiPath(), dir.resolve("classes"));
        final var parent = ModuleLayer.boot();
        final var configuration = parent.configuration().resolve(finder, ModuleFinder.of(), Set.of("consumer"));
        final var layer = ModuleLayer.defineModulesWithOneLoader(configuration, List.of(parent), null).layer();
        final var main = layer.findLoader("consumer").loadClass("consumer.Main");
        final var supplier = (Supplier<?>) main.getDeclaredConstructor().newInstance();

        assertThat(supplier.get()).isEqualTo("30b");
    }

    @Test
    void compilerRejectsUnrelatedUnitTypes(@TempDir final Path dir) throws Exception {
        final var out = compile(dir, UNRELATED_UNIT);

        assertThat(out.success()).as(out.diagnostics()).isFalse();
    }

    private static Result compile(final Path dir, final String source) throws IOException, URISyntaxException {
        final var src = dir.resolve("src/consumer");
        Files.createDirectories(src);
        Files.writeString(dir.resolve("src/module-info.java"), MODULE_INFO);
        Files.writeString(src.resolve("Main.java"), source);
        final var classes = Files.createDirectories(dir.resolve("classes"));

        final var compiler = ToolProvider.getSystemJavaCompiler();
        final var diagnostics = new StringWriter();
        final int status = compiler.getTask(
                        diagnostics,
                        null,
                        null,
                        List.of("--module-path", apiPath().toString(), "-d", classes.toString()),
                        null,
                        compiler.getStandardFileManager(null, null, null)
                                .getJavaFileObjects(dir.resolve("src/module-info.java"), src.resolve("Main.java")))
                .call()
                ? 0
                : 1;
        return new Result(status == 0, diagnostics.toString());
    }

    private static Path apiPath() throws URISyntaxException {
        return Path.of(NativeToken.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    }

    private record Result(boolean success, String diagnostics) {}
}
