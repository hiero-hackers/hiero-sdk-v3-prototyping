package org.hiero.sdk.v3.metalang.model;

import java.util.Comparator;
import java.util.Objects;

/**
 * The unique name of a declaration: its namespace plus its simple name, e.g.
 * {@code consensusnode.transactions.Transaction}.
 *
 * @param namespace the namespace
 * @param name      the simple name
 */
public record QualifiedName(String namespace, String name) implements Comparable<QualifiedName> {

    private static final Comparator<QualifiedName> ORDER = Comparator.comparing(QualifiedName::namespace)
            .thenComparing(QualifiedName::name);

    /**
     * Creates a qualified name.
     *
     * @param namespace the namespace
     * @param name      the simple name
     */
    public QualifiedName {
        Objects.requireNonNull(namespace, "namespace must not be null");
        Objects.requireNonNull(name, "name must not be null");
    }

    @Override
    public int compareTo(final QualifiedName other) {
        return ORDER.compare(this, other);
    }

    @Override
    public String toString() {
        return namespace + "." + name;
    }
}
