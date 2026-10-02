package org.hiero.sdk.v3.metalang.ast;

import java.util.List;
import java.util.Objects;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;

/**
 * A method of a complex type or enum, or a namespace-level function.
 *
 * @param name          the method name
 * @param annotations   the annotations (incl. return-type annotations such as {@code @@nullable})
 * @param returnType    the return type ({@link TypeRef.Void} if none)
 * @param parameters    the parameters
 * @param syntax        the syntactic form
 * @param documentation the attached comment text (may be empty)
 * @param location      the source location
 */
public record Method(String name, List<Annotation> annotations, TypeRef returnType, List<Parameter> parameters,
                     MethodSyntax syntax, String documentation, SourceLocation location) implements Member {

    /**
     * Creates a method.
     *
     * @param name          the name
     * @param annotations   the annotations
     * @param returnType    the return type
     * @param parameters    the parameters
     * @param syntax        the syntactic form
     * @param documentation the documentation
     * @param location      the source location
     */
    public Method {
        Objects.requireNonNull(name, "name must not be null");
        annotations = List.copyOf(Objects.requireNonNull(annotations, "annotations must not be null"));
        Objects.requireNonNull(returnType, "returnType must not be null");
        parameters = List.copyOf(Objects.requireNonNull(parameters, "parameters must not be null"));
        Objects.requireNonNull(syntax, "syntax must not be null");
        Objects.requireNonNull(documentation, "documentation must not be null");
        Objects.requireNonNull(location, "location must not be null");
    }

    /**
     * Returns a signature key that identifies the method by name and erased parameter types,
     * e.g. {@code sign(AccountId,TransactionSigner,list)}.
     *
     * @return the signature key
     */
    public String signature() {
        return name + "(" + String.join(",", parameters.stream().map(p -> erasure(p.type())).toList()) + ")";
    }

    private static String erasure(final TypeRef type) {
        return switch (type) {
            case TypeRef.Named named -> named.name();
            case TypeRef.Function ignored -> "function";
            default -> type.text();
        };
    }
}
