package org.hiero.sdk.v3.metalang.validation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.hiero.sdk.v3.metalang.ast.Declaration;
import org.hiero.sdk.v3.metalang.ast.Method;
import org.hiero.sdk.v3.metalang.ast.Parameter;
import org.hiero.sdk.v3.metalang.ast.SchemaFile;
import org.hiero.sdk.v3.metalang.ast.TypeParameter;
import org.hiero.sdk.v3.metalang.ast.TypeRef;
import org.hiero.sdk.v3.metalang.semantic.SpecModel;

/**
 * Helpers to enumerate the elements of a model in a deterministic order.
 */
final class Elements {

    /**
     * Where a type reference occurs.
     */
    enum Position {
        SUPERTYPE, TYPE_PARAMETER_BOUND, FIELD, PARAMETER, RETURN, STREAMING_RETURN, CONSTANT
    }

    /**
     * A top-level type reference together with its context.
     *
     * @param file          the containing file
     * @param type          the type reference
     * @param position      where it occurs
     * @param genericScope  generic parameter names in scope
     */
    record TypeSite(SchemaFile file, TypeRef type, Position position, Set<String> genericScope) {
    }

    private Elements() {
    }

    /**
     * Visits every top-level type reference of the model.
     */
    static void forEachTypeSite(final SpecModel model, final Consumer<TypeSite> visitor) {
        for (final SchemaFile file : model.files()) {
            for (final Declaration declaration : file.declarations()) {
                switch (declaration) {
                    case Declaration.TypeDeclaration type -> {
                        final Set<String> typeScope = new LinkedHashSet<>();
                        type.typeParameters().forEach(p -> typeScope.add(p.name()));
                        for (final TypeParameter parameter : type.typeParameters()) {
                            if (parameter.bound() != null) {
                                visitor.accept(new TypeSite(file, parameter.bound(), Position.TYPE_PARAMETER_BOUND,
                                        typeScope));
                            }
                        }
                        type.supertypes().forEach(s -> visitor.accept(
                                new TypeSite(file, s, Position.SUPERTYPE, typeScope)));
                        type.fields().forEach(f -> visitor.accept(
                                new TypeSite(file, f.type(), Position.FIELD, typeScope)));
                        if (type instanceof Declaration.EnumType enumType) {
                            enumType.attributes().forEach(a -> visitor.accept(
                                    new TypeSite(file, a.type(), Position.FIELD, typeScope)));
                        }
                        type.methods().forEach(m -> visitMethod(file, m, typeScope, visitor));
                    }
                    case Declaration.Function function -> visitMethod(file, function.method(), Set.of(), visitor);
                    case Declaration.Constant constant -> visitor.accept(
                            new TypeSite(file, constant.type(), Position.CONSTANT, Set.of()));
                }
            }
        }
    }

    private static void visitMethod(final SchemaFile file, final Method method, final Set<String> outerScope,
                                    final Consumer<TypeSite> visitor) {
        final Set<String> scope = new LinkedHashSet<>(outerScope);
        method.typeParameters().forEach(p -> scope.add(p.name()));
        scope.addAll(useSiteGenerics(method));
        for (final TypeParameter parameter : method.typeParameters()) {
            if (parameter.bound() != null) {
                visitor.accept(new TypeSite(file, parameter.bound(), Position.TYPE_PARAMETER_BOUND, scope));
            }
        }
        final Position returnPosition = method.hasAnnotation("streaming") ? Position.STREAMING_RETURN : Position.RETURN;
        visitor.accept(new TypeSite(file, method.returnType(), returnPosition, scope));
        for (final Parameter parameter : method.parameters()) {
            visitor.accept(new TypeSite(file, parameter.type(), Position.PARAMETER, scope));
        }
    }

    /**
     * Generic names that a method introduces through use-site bounds ({@code $$R extends X}) in its
     * signature. They are treated as declared for the method so that only the bound itself is
     * reported, not every later use of the name.
     */
    static Set<String> useSiteGenerics(final Method method) {
        final Set<String> names = new LinkedHashSet<>();
        final List<TypeRef> roots = new ArrayList<>();
        roots.add(method.returnType());
        method.parameters().forEach(p -> roots.add(p.type()));
        for (final TypeRef root : roots) {
            collectBoundedGenerics(root, names);
        }
        return names;
    }

    private static void collectBoundedGenerics(final TypeRef type, final Set<String> names) {
        switch (type) {
            case TypeRef.Named named -> {
                for (final TypeRef.TypeArgument argument : named.arguments()) {
                    switch (argument) {
                        case TypeRef.BoundedGeneric bounded -> {
                            names.add(bounded.name());
                            collectBoundedGenerics(bounded.bound(), names);
                        }
                        case TypeRef.Concrete concrete -> collectBoundedGenerics(concrete.type(), names);
                        case TypeRef.Wildcard wildcard -> {
                            if (wildcard.upperBound() != null) {
                                collectBoundedGenerics(wildcard.upperBound(), names);
                            }
                        }
                    }
                }
            }
            case TypeRef.Function function -> {
                collectBoundedGenerics(function.returnType(), names);
                function.parameters().forEach(p -> collectBoundedGenerics(p.type(), names));
            }
            default -> {
                // no nested type arguments
            }
        }
    }
}
