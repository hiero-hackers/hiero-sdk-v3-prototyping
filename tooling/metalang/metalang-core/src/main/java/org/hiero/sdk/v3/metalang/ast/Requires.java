package org.hiero.sdk.v3.metalang.ast;

import java.util.List;
import java.util.Objects;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;

/**
 * A {@code requires {A, B} from ns} or {@code requires {*} from ns} statement.
 *
 * @param namespace the source namespace
 * @param types     the imported type names (empty for a wildcard import)
 * @param wildcard  whether this is {@code requires {*}}
 * @param location  the source location
 */
public record Requires(String namespace, List<String> types, boolean wildcard, SourceLocation location)
        implements Node {

    /**
     * Creates a requires statement.
     *
     * @param namespace the source namespace
     * @param types     the imported types
     * @param wildcard  whether it is a wildcard import
     * @param location  the source location
     */
    public Requires {
        Objects.requireNonNull(namespace, "namespace must not be null");
        types = List.copyOf(Objects.requireNonNull(types, "types must not be null"));
        Objects.requireNonNull(location, "location must not be null");
    }
}
