package org.hiero.sdk.v3.metalang.generator.rust;

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
import java.util.TreeSet;
import org.hiero.sdk.v3.metalang.generator.GenerationException;

/**
 * The project-specific configuration of the Rust generator, read from the {@code rust.*} keys of a
 * {@code generator.properties} file (e.g. {@code sdk-rust/generator.properties}).
 *
 * @param cratePrefix  the prefix of the crate names: the crate of the spec folder {@code base} is
 *                     {@code <prefix>-base} (library {@code <prefix>_base})
 * @param version      the version of all crates (a semantic version)
 * @param protobuf     the spec folders whose crate compiles the protobuf messages in its {@code build.rs}
 * @param protobufRoot the directory of the vendored protobuf definitions, relative to a crate directory
 */
public record RustGeneratorConfig(String cratePrefix, String version, Set<String> protobuf, String protobufRoot) {

    /** The key that lists the spec folders whose crate needs the protobuf messages. */
    public static final String PROTOBUF = "rust.protobuf";

    /** The key of the directory of the vendored protobuf definitions. */
    public static final String PROTOBUF_ROOT = "rust.protobufRoot";

    /** The protobuf definitions of a workspace in {@code generated/rust}, seen from {@code crates/<crate>}. */
    public static final String DEFAULT_PROTOBUF_ROOT = "../../../../protobuf/consensus-node";

    /** The configuration without a configuration file. */
    public static final RustGeneratorConfig DEFAULT = new RustGeneratorConfig("hiero", "0.1.0");

    /**
     * Creates a configuration without protobuf crates.
     *
     * @param cratePrefix the prefix of the crate names
     * @param version     the version
     * @throws GenerationException if a value is not valid for Cargo
     */
    public RustGeneratorConfig(final String cratePrefix, final String version) {
        this(cratePrefix, version, Set.of(), DEFAULT_PROTOBUF_ROOT);
    }

    /**
     * Creates a configuration.
     *
     * @param cratePrefix the prefix of the crate names
     * @param version     the version
     * @throws GenerationException if a value is not valid for Cargo
     */
    public RustGeneratorConfig {
        Objects.requireNonNull(cratePrefix, "cratePrefix must not be null");
        Objects.requireNonNull(version, "version must not be null");
        protobuf = Set.copyOf(Objects.requireNonNull(protobuf, "protobuf must not be null"));
        Objects.requireNonNull(protobufRoot, "protobufRoot must not be null");
        final List<String> problems = new ArrayList<>();
        if (protobufRoot.isBlank() || protobufRoot.contains("\\") || protobufRoot.contains("\"")) {
            problems.add(PROTOBUF_ROOT + ": '" + protobufRoot + "' is no directory (use '/' as separator)");
        }
        if (!cratePrefix.matches("[a-z][a-z0-9]*(-[a-z0-9]+)*")) {
            problems.add("rust.cratePrefix: '" + cratePrefix + "' is no crate name prefix (lowercase letters and "
                    + "digits, separated by '-')");
        }
        if (!version.matches("\\d+\\.\\d+\\.\\d+(-[0-9A-Za-z.-]+)?")) {
            problems.add("rust.version: '" + version + "' is no semantic version");
        }
        if (!problems.isEmpty()) {
            throw new GenerationException(problems);
        }
    }

    /**
     * Reads a configuration file; keys of other languages ({@code java.*}, {@code ts.*}) are ignored.
     *
     * @param file the {@code .properties} file
     * @return the configuration
     * @throws IOException         if the file cannot be read
     * @throws GenerationException if a {@code rust.*} key is unknown or a value is invalid
     */
    public static RustGeneratorConfig load(final Path file) throws IOException {
        final Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        final List<String> problems = new ArrayList<>();
        for (final String key : properties.stringPropertyNames().stream().sorted().toList()) {
            if (key.startsWith("rust.") && !key.equals("rust.cratePrefix") && !key.equals("rust.version")
                    && !key.equals(PROTOBUF) && !key.equals(PROTOBUF_ROOT)) {
                problems.add("Unknown key '" + key + "' (known: rust.cratePrefix, rust.version, " + PROTOBUF + ", "
                        + PROTOBUF_ROOT + ")");
            }
        }
        if (!problems.isEmpty()) {
            throw new GenerationException(problems);
        }
        final Set<String> protobuf = new TreeSet<>();
        for (final String folder : properties.getProperty(PROTOBUF, "").split("[,\\s]+")) {
            if (folder.isEmpty()) {
                continue;
            }
            if (!folder.matches("[a-z0-9]+(-[a-z0-9]+)*")) {
                problems.add("'" + folder + "' in " + PROTOBUF + " is no spec folder (lowercase, '-' separated)");
            } else {
                protobuf.add(folder);
            }
        }
        if (!problems.isEmpty()) {
            throw new GenerationException(problems);
        }
        return new RustGeneratorConfig(properties.getProperty("rust.cratePrefix", DEFAULT.cratePrefix()).strip(),
                properties.getProperty("rust.version", DEFAULT.version()).strip(), protobuf,
                properties.getProperty(PROTOBUF_ROOT, DEFAULT_PROTOBUF_ROOT).strip());
    }

    /**
     * Returns the package name of the crate of a spec folder.
     *
     * @param folder the spec folder
     * @return e.g. {@code hiero-consensus-node-client}
     */
    public String crateName(final String folder) {
        return cratePrefix + "-" + folder.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
    }

    /**
     * Returns the library name of the crate of a spec folder, as used in paths.
     *
     * @param folder the spec folder
     * @return e.g. {@code hiero_consensus_node_client}
     */
    public String libraryName(final String folder) {
        return crateName(folder).replace('-', '_');
    }
}
