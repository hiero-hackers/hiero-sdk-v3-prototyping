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
 * </pre>
 *
 * @param interfaces abstractions that are generated as interfaces instead of abstract classes
 */
public record JavaGeneratorConfig(Set<QualifiedName> interfaces) {

    /** The key that lists the abstractions that become interfaces. */
    public static final String INTERFACES = "java.interfaces";

    /** A configuration without project-specific decisions. */
    public static final JavaGeneratorConfig DEFAULT = new JavaGeneratorConfig(Set.of());

    /**
     * Creates a configuration.
     *
     * @param interfaces abstractions that are generated as interfaces
     */
    public JavaGeneratorConfig {
        interfaces = Set.copyOf(Objects.requireNonNull(interfaces, "interfaces must not be null"));
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
        keys.stream().filter(k -> !k.equals(INTERFACES))
                .forEach(k -> problems.add("Unknown key '" + k + "' in " + file + " (known: " + INTERFACES + ")"));
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
        return new JavaGeneratorConfig(interfaces);
    }
}
