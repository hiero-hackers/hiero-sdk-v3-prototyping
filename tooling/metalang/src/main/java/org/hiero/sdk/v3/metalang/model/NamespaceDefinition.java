package org.hiero.sdk.v3.metalang.model;

import java.util.List;
import java.util.Objects;

/**
 * A namespace with the spec files that declare it and the namespaces it depends on.
 *
 * @param name               the namespace name
 * @param sources            the spec files that declare the namespace (sorted by file name)
 * @param requiredNamespaces the other namespaces it depends on (sorted): every namespace named in a
 *                           {@code requires} statement plus every namespace whose types it references
 */
public record NamespaceDefinition(String name, List<Source> sources, List<String> requiredNamespaces) {

    /**
     * Creates a namespace definition.
     *
     * @param name               the name
     * @param sources            the declaring spec files
     * @param requiredNamespaces the required namespaces
     */
    public NamespaceDefinition {
        Objects.requireNonNull(name, "name must not be null");
        sources = List.copyOf(Objects.requireNonNull(sources, "sources must not be null"));
        requiredNamespaces = List.copyOf(Objects.requireNonNull(requiredNamespaces,
                "requiredNamespaces must not be null"));
    }

    /**
     * A spec file that declares (a part of) the namespace.
     *
     * @param file        the spec file name relative to the spec root
     * @param description the Markdown text of its {@code ## Description} section (may be empty)
     */
    public record Source(String file, String description) {

        /**
         * Creates a source.
         *
         * @param file        the file name
         * @param description the description
         */
        public Source {
            Objects.requireNonNull(file, "file must not be null");
            Objects.requireNonNull(description, "description must not be null");
        }
    }
}
