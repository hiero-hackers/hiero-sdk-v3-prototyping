package org.hiero.sdk.v3.metalang.ast;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;

/**
 * The AST of one spec's API schema.
 *
 * @param namespace    the declared namespace
 * @param requires     the requires statements
 * @param declarations the top-level declarations in source order
 * @param comments     all comments of the schema in source order
 * @param location     location of the {@code namespace} statement
 */
public record SchemaFile(String namespace, List<Requires> requires, List<Declaration> declarations,
                         List<Comment> comments, SourceLocation location) implements Node {

    /**
     * Creates a schema file.
     *
     * @param namespace    the namespace
     * @param requires     the requires statements
     * @param declarations the declarations
     * @param comments     the comments
     * @param location     the location of the namespace statement
     */
    public SchemaFile {
        Objects.requireNonNull(namespace, "namespace must not be null");
        requires = List.copyOf(Objects.requireNonNull(requires, "requires must not be null"));
        declarations = List.copyOf(Objects.requireNonNull(declarations, "declarations must not be null"));
        comments = List.copyOf(Objects.requireNonNull(comments, "comments must not be null"));
        Objects.requireNonNull(location, "location must not be null");
    }

    /**
     * Returns all type declarations (complex types and enums).
     *
     * @return the type declarations in source order
     */
    public List<Declaration.TypeDeclaration> types() {
        return declarations.stream()
                .flatMap(d -> d instanceof Declaration.TypeDeclaration t ? Stream.of(t) : Stream.empty())
                .toList();
    }

    /**
     * Returns the file name the schema was read from.
     *
     * @return the file name
     */
    public String file() {
        return location.file();
    }
}
