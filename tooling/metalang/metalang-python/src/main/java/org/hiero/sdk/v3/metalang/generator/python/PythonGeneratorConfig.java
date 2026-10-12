package org.hiero.sdk.v3.metalang.generator.python;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import org.hiero.sdk.v3.metalang.generator.GenerationException;

/**
 * The project-specific configuration of the Python generator, read from a {@code .properties} file (e.g.
 * {@code sdk-python/generator.properties}).
 *
 * <ul>
 *   <li>{@code python.distribution}: the distribution name on PyPI, e.g. {@code hiero-sdk} — the Hiero namespace
 *       guidelines make the {@code hiero-} prefix mandatory</li>
 *   <li>{@code python.package}: the import package, e.g. {@code hiero_sdk}</li>
 *   <li>{@code python.version}: the version of the distribution (PEP 440)</li>
 * </ul>
 *
 * @param distribution the distribution name
 * @param packageName  the import package
 * @param version      the version
 */
public record PythonGeneratorConfig(String distribution, String packageName, String version) {

    /** The key of the distribution name. */
    public static final String DISTRIBUTION = "python.distribution";

    /** The key of the import package. */
    public static final String PACKAGE = "python.package";

    /** The key of the version. */
    public static final String VERSION = "python.version";

    /** The configuration without a configuration file. */
    public static final PythonGeneratorConfig DEFAULT = new PythonGeneratorConfig("hiero-sdk", "hiero_sdk", "0.1.0");

    /**
     * Creates a configuration.
     *
     * @param distribution the distribution name
     * @param packageName  the import package
     * @param version      the version
     * @throws GenerationException if a value is not valid for Python packaging
     */
    public PythonGeneratorConfig {
        Objects.requireNonNull(distribution, "distribution must not be null");
        Objects.requireNonNull(packageName, "packageName must not be null");
        Objects.requireNonNull(version, "version must not be null");
        final List<String> problems = new ArrayList<>();
        if (!distribution.matches("hiero-[a-z0-9]+([.-][a-z0-9]+)*")) {
            problems.add(DISTRIBUTION + ": '" + distribution + "' is no distribution name with the mandatory "
                    + "'hiero-' prefix (lowercase letters, digits, '.' and '-')");
        }
        if (!packageName.matches("[a-z_][a-z0-9_]*") || PyNames.isKeyword(packageName)) {
            problems.add(PACKAGE + ": '" + packageName + "' is no Python package name");
        }
        if (!version.matches("\\d+(\\.\\d+)*((a|b|rc)\\d+)?(\\.post\\d+)?(\\.dev\\d+)?")) {
            problems.add(VERSION + ": '" + version + "' is no PEP 440 version");
        }
        if (!problems.isEmpty()) {
            throw new GenerationException(problems);
        }
    }

    /**
     * Reads a configuration file; keys of other languages are ignored.
     *
     * @param file the {@code .properties} file
     * @return the configuration
     * @throws IOException         if the file cannot be read
     * @throws GenerationException if a {@code python.*} key is unknown or a value is invalid
     */
    public static PythonGeneratorConfig load(final Path file) throws IOException {
        final Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        final Set<String> known = Set.of(DISTRIBUTION, PACKAGE, VERSION);
        final List<String> problems = new ArrayList<>();
        properties.stringPropertyNames().stream().sorted()
                .filter(key -> key.startsWith("python.") && !known.contains(key))
                .forEach(key -> problems.add("Unknown key '" + key + "' (known: " + DISTRIBUTION + ", " + PACKAGE
                        + ", " + VERSION + ")"));
        if (!problems.isEmpty()) {
            throw new GenerationException(problems);
        }
        return new PythonGeneratorConfig(properties.getProperty(DISTRIBUTION, DEFAULT.distribution()).strip(),
                properties.getProperty(PACKAGE, DEFAULT.packageName()).strip(),
                properties.getProperty(VERSION, DEFAULT.version()).strip());
    }

    /**
     * Returns the import path of the package of a namespace: {@code consensusnode.transactions} becomes
     * {@code hiero_sdk.consensusnode.transactions}.
     *
     * @param namespace the namespace
     * @return the dotted module path
     */
    public String modulePath(final String namespace) {
        return packageName + "." + PyNames.packagePath(namespace).replace('/', '.');
    }
}
