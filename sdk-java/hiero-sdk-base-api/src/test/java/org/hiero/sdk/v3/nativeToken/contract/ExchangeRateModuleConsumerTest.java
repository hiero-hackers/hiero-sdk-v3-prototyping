package org.hiero.sdk.v3.nativeToken.contract;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringWriter;
import java.lang.module.ModuleFinder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import javax.tools.ToolProvider;
import org.hiero.sdk.v3.nativeToken.ExchangeRate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Compiles and runs an independent named module that depends only on the public API module. */
class ExchangeRateModuleConsumerTest {
    private static final String MODULE_INFO = """
            module consumer {
                requires org.hiero.sdk.v3.base;
                exports consumer;
            }
            """;

    private static final String CONSUMER = """
            package consumer;

            import java.time.Instant;
            import java.time.ZoneId;
            import java.time.ZonedDateTime;
            import java.util.function.Supplier;
            import org.hiero.sdk.v3.nativeToken.ExchangeRate;

            public final class Main implements Supplier<String> {
                static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

                static final class Rate extends ExchangeRate {
                    Rate(ZonedDateTime t, double c) { super(t, c); }
                    public boolean isExpired() { return NOW.isAfter(expirationTime().toInstant()); }
                }

                public String get() {
                    Rate past = new Rate(NOW.minusNanos(1).atZone(ZoneId.of("UTC")), 2.5);
                    Rate equal = new Rate(NOW.atZone(ZoneId.of("Asia/Tokyo")), 1);
                    return past.exchangeRateInUsdCents() + ":" + past.isExpired() + ":" + equal.isExpired();
                }
            }
            """;

    @Test
    void consumerCompilesAndRunsAgainstThePublicModuleOnly(@TempDir final Path dir) throws Exception {
        final var api = Path.of(ExchangeRate.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        final var src = Files.createDirectories(dir.resolve("src/consumer"));
        Files.writeString(dir.resolve("src/module-info.java"), MODULE_INFO);
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

        assertThat(supplier.get()).isEqualTo("2.5:true:false");
    }
}
