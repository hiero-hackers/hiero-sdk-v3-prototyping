package org.hiero.sdk.v3.metalang.validation;

import java.util.Objects;
import java.util.Optional;
import org.hiero.sdk.v3.metalang.ast.Declaration;
import org.hiero.sdk.v3.metalang.model.FieldDefinition;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.hiero.sdk.v3.metalang.model.QualifiedName;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.semantic.SpecModel;

/**
 * Input of every {@link Check}: the semantic model and the linked model built from it.
 *
 * @param model  the semantic model
 * @param linked the linked model
 */
record ValidationContext(SpecModel model, LinkedModel linked) {

    ValidationContext {
        Objects.requireNonNull(model, "model must not be null");
        Objects.requireNonNull(linked, "linked must not be null");
    }

    /**
     * Returns the qualified name of a type declaration of the model.
     */
    QualifiedName nameOf(final Declaration.TypeDeclaration type) {
        return new QualifiedName(model.fileOf(type).namespace(), type.name());
    }

    /**
     * Returns the inherited type of a member, as seen from the inheriting type (type arguments of the supertype
     * substituted), if the declared type of the member differs from it. Empty if both types are equal or one of
     * them cannot be determined (unresolved references are reported elsewhere).
     */
    Optional<Type> inheritedTypeIfDifferent(final Declaration.TypeDeclaration type, final String member) {
        final QualifiedName name = nameOf(type);
        final Optional<Type> own = linked.type(name).flatMap(d -> d.field(member)).map(FieldDefinition::type);
        final Optional<Type> inherited = linked.inheritedField(name, member).map(FieldDefinition::type);
        if (own.isEmpty() || inherited.isEmpty() || containsUnresolved(own.get())
                || containsUnresolved(inherited.get()) || own.get().equals(inherited.get())) {
            return Optional.empty();
        }
        return inherited;
    }

    private static boolean containsUnresolved(final Type type) {
        return switch (type) {
            case Type.UnresolvedType ignored -> true;
            case Type.BasicType basic -> basic.arguments().stream().anyMatch(ValidationContext::containsUnresolved);
            case Type.DeclaredType declared ->
                    declared.arguments().stream().anyMatch(ValidationContext::containsUnresolved);
            case Type.WildcardType wildcard -> wildcard.upperBound() != null
                    && containsUnresolved(wildcard.upperBound());
            case Type.FunctionType function -> containsUnresolved(function.returnType())
                    || function.parameters().stream().anyMatch(p -> containsUnresolved(p.type()));
            default -> false;
        };
    }
}
