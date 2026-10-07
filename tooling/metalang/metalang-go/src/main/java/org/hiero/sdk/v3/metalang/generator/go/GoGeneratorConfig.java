package org.hiero.sdk.v3.metalang.generator.go;

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
 * The project-specific configuration of the Go generator, read from the {@code go.*} keys of a
 * {@code .properties} file (e.g. {@code sdk-go/generator.properties}).
 *
 * <ul>
 *   <li>{@code go.module}: the module path of the generated module. A Go module path is a repository URL, which
 *       the specs cannot know, so it has to be configured.</li>
 *   <li>{@code go.version}: the minimum Go version in {@code go.mod}. The guideline requires 1.27 or newer:
 *       {@code iter.Seq2} for streaming and generic methods on concrete types.</li>
 * </ul>
 *
 * @param module  the module path, e.g. {@code github.com/hiero-ledger/hiero-sdk-go}
 * @param version the minimum Go version, e.g. {@code 1.27}
 */
public record GoGeneratorConfig(String module, String version) {

    /** The key of the module path. */
    public static final String MODULE = "go.module";

    /** The key of the minimum Go version. */
    public static final String VERSION = "go.version";

    /** The default module path. */
    public static final String DEFAULT_MODULE = "github.com/hiero-ledger/hiero-sdk-go";

    /** The oldest Go version the guideline allows. */
    public static final String DEFAULT_VERSION = "1.27";

    /** The configuration without a configuration file. */
    public static final GoGeneratorConfig DEFAULT = new GoGeneratorConfig(DEFAULT_MODULE, DEFAULT_VERSION);

    /**
     * Creates a configuration.
     *
     * @param module  the module path
     * @param version the minimum Go version
     * @throws GenerationException if a value is not valid for Go
     */
    public GoGeneratorConfig {
        Objects.requireNonNull(module, "module must not be null");
        Objects.requireNonNull(version, "version must not be null");
        final List<String> problems = new ArrayList<>();
        // a module path is a slash-separated path, the first element usually a host name
        if (!module.matches("[A-Za-z0-9._~-]+(/[A-Za-z0-9._~-]+)*")) {
            problems.add(MODULE + ": '" + module + "' is no Go module path");
        }
        if (!version.matches("\\d+\\.\\d+(\\.\\d+)?")) {
            problems.add(VERSION + ": '" + version + "' is no Go version");
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
     * @throws GenerationException if a {@code go.*} key is unknown or a value is invalid
     */
    public static GoGeneratorConfig load(final Path file) throws IOException {
        final Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        final List<String> problems = new ArrayList<>();
        final Set<String> known = Set.of(MODULE, VERSION);
        properties.stringPropertyNames().stream().sorted()
                .filter(key -> key.startsWith("go.") && !known.contains(key))
                .forEach(key -> problems.add("Unknown key '" + key + "' (known: " + MODULE + ", " + VERSION + ")"));
        if (!problems.isEmpty()) {
            throw new GenerationException(problems);
        }
        return new GoGeneratorConfig(properties.getProperty(MODULE, DEFAULT_MODULE).strip(),
                properties.getProperty(VERSION, DEFAULT_VERSION).strip());
    }

    /**
     * The import path of a namespace.
     *
     * @param namespace the namespace, e.g. {@code ledger.config}
     * @return the import path, e.g. {@code github.com/hiero-ledger/hiero-sdk-go/ledger/config}
     */
    public String importPath(final String namespace) {
        return module + "/" + GoNames.directory(namespace);
    }
}
