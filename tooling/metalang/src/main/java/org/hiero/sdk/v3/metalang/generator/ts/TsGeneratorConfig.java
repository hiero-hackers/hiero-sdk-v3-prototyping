package org.hiero.sdk.v3.metalang.generator.ts;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import org.hiero.sdk.v3.metalang.generator.GenerationException;

/**
 * The project-specific configuration of the TypeScript generator, read from a {@code .properties} file (e.g.
 * {@code sdk-ts/generator.properties}).
 *
 * <ul>
 *   <li>{@code ts.scope}: the npm scope of the packages, e.g. {@code @hiero} (packages {@code @hiero/base}, ...)</li>
 *   <li>{@code ts.version}: the version of all packages</li>
 * </ul>
 *
 * @param scope   the npm scope, starting with {@code @}
 * @param version the package version
 */
public record TsGeneratorConfig(String scope, String version) {

    /** The configuration without a configuration file. */
    public static final TsGeneratorConfig DEFAULT = new TsGeneratorConfig("@hiero", "0.1.0-SNAPSHOT");

    /**
     * Creates a configuration.
     *
     * @param scope   the npm scope
     * @param version the version
     * @throws GenerationException if a value is not valid for npm
     */
    public TsGeneratorConfig {
        Objects.requireNonNull(scope, "scope must not be null");
        Objects.requireNonNull(version, "version must not be null");
        final List<String> problems = new ArrayList<>();
        if (!scope.matches("@[a-z0-9][a-z0-9._-]*")) {
            problems.add("ts.scope: '" + scope + "' is no npm scope (@ followed by lowercase letters, digits, '.', "
                    + "'_' or '-')");
        }
        if (!version.matches("\\d+\\.\\d+\\.\\d+(-[0-9A-Za-z.-]+)?")) {
            problems.add("ts.version: '" + version + "' is no semantic version");
        }
        if (!problems.isEmpty()) {
            throw new GenerationException(problems);
        }
    }

    /**
     * Reads a configuration file; keys of other languages ({@code java.*}) are ignored.
     *
     * @param file the {@code .properties} file
     * @return the configuration
     * @throws IOException         if the file cannot be read
     * @throws GenerationException if a {@code ts.*} key is unknown or a value is invalid
     */
    public static TsGeneratorConfig load(final Path file) throws IOException {
        final Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        final List<String> problems = new ArrayList<>();
        for (final String key : properties.stringPropertyNames()) {
            if (key.startsWith("ts.") && !key.equals("ts.scope") && !key.equals("ts.version")) {
                problems.add("Unknown key '" + key + "' (known: ts.scope, ts.version)");
            }
        }
        if (!problems.isEmpty()) {
            throw new GenerationException(problems);
        }
        return new TsGeneratorConfig(properties.getProperty("ts.scope", DEFAULT.scope()).strip(),
                properties.getProperty("ts.version", DEFAULT.version()).strip());
    }
}
