package org.hiero.sdk.v3.metalang.model;

import java.util.List;
import java.util.Objects;
import org.hiero.sdk.v3.metalang.ast.Annotated;
import org.hiero.sdk.v3.metalang.ast.Annotation;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;
import org.jspecify.annotations.Nullable;

/**
 * A resolved method of a type, or a namespace-level function ({@link #declaringType()} is {@code null}).
 *
 * @param name           the name
 * @param typeParameters the method's own type parameters
 * @param returnType     the resolved return type
 * @param parameters     the parameters
 * @param declaringType  the declaring type, {@code null} for namespace-level functions
 * @param annotations    the annotations
 * @param documentation  the documentation
 * @param location       the source location of the declaration
 */
public record MethodDefinition(String name, List<TypeParameterDefinition> typeParameters, Type returnType,
                               List<ParameterDefinition> parameters, @Nullable QualifiedName declaringType,
                               List<Annotation> annotations, String documentation, SourceLocation location)
        implements Annotated {

    /**
     * Creates a method definition.
     *
     * @param name           the name
     * @param typeParameters the type parameters
     * @param returnType     the return type
     * @param parameters     the parameters
     * @param declaringType  the declaring type or {@code null}
     * @param annotations    the annotations
     * @param documentation  the documentation
     * @param location       the source location
     */
    public MethodDefinition {
        Objects.requireNonNull(name, "name must not be null");
        typeParameters = List.copyOf(Objects.requireNonNull(typeParameters, "typeParameters must not be null"));
        Objects.requireNonNull(returnType, "returnType must not be null");
        parameters = List.copyOf(Objects.requireNonNull(parameters, "parameters must not be null"));
        annotations = List.copyOf(Objects.requireNonNull(annotations, "annotations must not be null"));
        Objects.requireNonNull(documentation, "documentation must not be null");
        Objects.requireNonNull(location, "location must not be null");
    }

    /**
     * Returns whether the method is {@code @@static}.
     *
     * @return {@code true} if static
     */
    public boolean isStatic() {
        return hasAnnotation("static");
    }

    /**
     * Returns the signature used to detect overrides: name and parameter types (with type arguments).
     *
     * @return the signature, e.g. {@code sign(ledger.AccountId,consensusnode.client.TransactionSigner)}
     */
    public String signature() {
        return name + "(" + String.join(",", parameters.stream()
                .map(p -> p.type().text() + (p.varargs() ? "..." : "")).toList()) + ")";
    }
}
