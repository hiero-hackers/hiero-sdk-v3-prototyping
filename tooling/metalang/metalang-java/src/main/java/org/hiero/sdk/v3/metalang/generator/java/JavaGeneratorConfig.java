package org.hiero.sdk.v3.metalang.generator.java;

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
import java.util.SortedSet;
import java.util.TreeSet;
import org.hiero.sdk.v3.metalang.generator.GenerationException;
import org.hiero.sdk.v3.metalang.model.QualifiedName;

/**
 * Project-specific decisions for the Java generator that the specs cannot express.
 *
 * <p>The configuration is a {@code .properties} file; all keys of the Java generator start with {@code java.}:
 * <pre>
 * # abstractions that become interfaces although they have attributes (comma or whitespace separated)
 * java.interfaces = consensusnode.client.Submittable, ledger.BaseAddress
 * # Maven coordinates of the generated project
 * java.groupId = org.hiero.sdk
 * java.version = 0.1.0-SNAPSHOT
 * </pre>
 *
 * @param interfaces abstractions that are generated as interfaces instead of abstract classes
 * @param groupId    the Maven groupId of the generated project and its modules
 * @param version    the Maven version of the generated project and its modules
 */
public record JavaGeneratorConfig(Set<QualifiedName> interfaces, String groupId, String version) {

    /** The key that lists the abstractions that become interfaces. */
    public static final String INTERFACES = "java.interfaces";

    /** The key of the Maven groupId. */
    public static final String GROUP_ID = "java.groupId";

    /** The key of the Maven version. */
    public static final String VERSION = "java.version";

    /** The default Maven groupId. */
    public static final String DEFAULT_GROUP_ID = "org.hiero.sdk";

    /** The default Maven version. */
    public static final String DEFAULT_VERSION = "0.1.0-SNAPSHOT";

    /** A configuration without project-specific decisions. */
    public static final JavaGeneratorConfig DEFAULT = new JavaGeneratorConfig(Set.of());

    /**
     * Creates a configuration.
     *
     * @param interfaces abstractions that are generated as interfaces
     * @param groupId    the Maven groupId
     * @param version    the Maven version
     */
    public JavaGeneratorConfig {
        interfaces = Set.copyOf(Objects.requireNonNull(interfaces, "interfaces must not be null"));
        Objects.requireNonNull(groupId, "groupId must not be null");
        Objects.requireNonNull(version, "version must not be null");
    }

    /**
     * Creates a configuration with the default Maven coordinates.
     *
     * @param interfaces abstractions that are generated as interfaces
     */
    public JavaGeneratorConfig(final Set<QualifiedName> interfaces) {
        this(interfaces, DEFAULT_GROUP_ID, DEFAULT_VERSION);
    }

    /**
     * Reads a configuration file.
     *
     * @param file the {@code .properties} file
     * @return the configuration
     * @throws IOException         if the file cannot be read
     * @throws GenerationException if the file contains unknown keys or malformed type names
     */
    public static JavaGeneratorConfig load(final Path file) throws IOException {
        final Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        final List<String> problems = new ArrayList<>();
        final SortedSet<String> keys = new TreeSet<>(properties.stringPropertyNames());
        final Set<String> known = Set.of(INTERFACES, GROUP_ID, VERSION);
        keys.stream().filter(k -> !known.contains(k))
                .forEach(k -> problems.add("Unknown key '" + k + "' in " + file + " (known: " + INTERFACES + ", "
                        + GROUP_ID + ", " + VERSION + ")"));
        final String groupId = properties.getProperty(GROUP_ID, DEFAULT_GROUP_ID).strip();
        final String version = properties.getProperty(VERSION, DEFAULT_VERSION).strip();
        if (!groupId.matches("[A-Za-z0-9_.-]+")) {
            problems.add("'" + groupId + "' in " + GROUP_ID + " is no valid Maven groupId");
        }
        if (!version.matches("[A-Za-z0-9_.-]+")) {
            problems.add("'" + version + "' in " + VERSION + " is no valid Maven version");
        }
        final Set<QualifiedName> interfaces = new TreeSet<>();
        for (final String name : properties.getProperty(INTERFACES, "").split("[,\\s]+")) {
            if (name.isEmpty()) {
                continue;
            }
            final int dot = name.lastIndexOf('.');
            if (dot <= 0 || dot == name.length() - 1) {
                problems.add("'" + name + "' in " + INTERFACES + " is no qualified type name (namespace.Type)");
            } else {
                interfaces.add(new QualifiedName(name.substring(0, dot), name.substring(dot + 1)));
            }
        }
        if (!problems.isEmpty()) {
            throw new GenerationException(problems);
        }
        return new JavaGeneratorConfig(interfaces, groupId, version);
    }
}
