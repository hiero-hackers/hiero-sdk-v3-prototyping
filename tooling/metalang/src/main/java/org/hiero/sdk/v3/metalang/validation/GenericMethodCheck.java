package org.hiero.sdk.v3.metalang.validation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.hiero.sdk.v3.metalang.ast.Declaration;
import org.hiero.sdk.v3.metalang.ast.Method;
import org.hiero.sdk.v3.metalang.ast.SchemaFile;
import org.hiero.sdk.v3.metalang.ast.TypeParameter;
import org.hiero.sdk.v3.metalang.diagnostic.DiagnosticCollector;
import org.hiero.sdk.v3.metalang.diagnostic.Rule;
import org.hiero.sdk.v3.metalang.semantic.ResolvedType;
import org.hiero.sdk.v3.metalang.semantic.SpecModel;

/**
 * Rules for generic type parameters and {@code @@finalMethod} (guideline sections "Generic methods" and
 * "Method annotations").
 */
final class GenericMethodCheck implements Check {

    @Override
    public void run(final SpecModel model, final DiagnosticCollector out) {
        for (final SchemaFile file : model.files()) {
            for (final Declaration declaration : file.declarations()) {
                switch (declaration) {
                    case Declaration.TypeDeclaration type -> checkType(model, type, out);
                    case Declaration.Function function ->
                            checkDuplicates(function.method().typeParameters(), Set.of(), out);
                    case Declaration.Constant ignored -> {
                        // constants have no type parameters
                    }
                }
            }
        }
    }

    private static void checkType(final SpecModel model, final Declaration.TypeDeclaration type,
                                  final DiagnosticCollector out) {
        checkDuplicates(type.typeParameters(), Set.of(), out);
        final Set<String> typeScope = new HashSet<>();
        type.typeParameters().forEach(p -> typeScope.add(p.name()));
        final boolean finalType = type instanceof Declaration.EnumType || type.hasAnnotation("finalType");
        for (final Method method : type.methods()) {
            checkDuplicates(method.typeParameters(), typeScope, out);
            final boolean isStatic = method.hasAnnotation("static");
            final boolean isFinal = method.hasAnnotation("finalMethod");
            if (method.isGeneric() && !isStatic && !isFinal) {
                out.report(Rule.GENERIC_METHOD_NOT_FINAL, "Generic instance method '" + method.name()
                        + "' must be annotated with @@finalMethod (or be @@static)", method.location());
            }
            if (isFinal && isStatic) {
                out.report(Rule.FINAL_METHOD_REDUNDANT, "'" + method.name()
                        + "' is @@static and therefore cannot be overridden anyway", method.location());
            } else if (isFinal && finalType) {
                out.report(Rule.FINAL_METHOD_REDUNDANT, "'" + type.name() + "' cannot be extended, so @@finalMethod on '"
                        + method.name() + "' has no effect", method.location());
            }
        }
        checkFinalOverrides(model, type, out);
        checkFinalMultipleInheritance(model, type, out);
    }

    /**
     * Types declaring {@code @@finalMethod} map to classes in Java/Kotlin/C#. All such ancestors of a type must
     * therefore lie on one inheritance chain. Only reported where the conflict is introduced (each direct
     * supertype on its own is fine).
     */
    private static void checkFinalMultipleInheritance(final SpecModel model, final Declaration.TypeDeclaration type,
                                                      final DiagnosticCollector out) {
        final List<Declaration.TypeDeclaration> parents = model.directSupertypes(type).stream()
                .map(ResolvedType.Declared::declaration)
                .toList();
        if (parents.size() < 2) {
            return;
        }
        final List<Declaration.TypeDeclaration> classLike = new ArrayList<>();
        for (final Declaration.TypeDeclaration parent : parents) {
            final List<Declaration.TypeDeclaration> lineage = new ArrayList<>(List.of(parent));
            lineage.addAll(model.ancestors(parent));
            for (final Declaration.TypeDeclaration candidate : lineage) {
                if (declaresFinalMethod(candidate) && classLike.stream().noneMatch(c -> c == candidate)) {
                    classLike.add(candidate);
                }
            }
        }
        for (int i = 0; i < classLike.size(); i++) {
            for (int j = i + 1; j < classLike.size(); j++) {
                final Declaration.TypeDeclaration a = classLike.get(i);
                final Declaration.TypeDeclaration b = classLike.get(j);
                if (!isAncestor(model, a, b) && !isAncestor(model, b, a)) {
                    out.report(Rule.FINAL_METHOD_MULTIPLE_INHERITANCE, "'" + type.name()
                            + "' inherits @@finalMethod methods from both '" + a.name() + "' and '" + b.name()
                            + "', which would require multiple class inheritance", type.location());
                    return;
                }
            }
        }
    }

    private static boolean declaresFinalMethod(final Declaration.TypeDeclaration type) {
        return type.methods().stream().anyMatch(m -> m.hasAnnotation("finalMethod"));
    }

    private static boolean isAncestor(final SpecModel model, final Declaration.TypeDeclaration ancestor,
                                      final Declaration.TypeDeclaration type) {
        return model.ancestors(type).stream().anyMatch(a -> a == ancestor);
    }

    private static void checkDuplicates(final List<TypeParameter> parameters, final Set<String> outer,
                                        final DiagnosticCollector out) {
        final Set<String> seen = new HashSet<>();
        for (final TypeParameter parameter : parameters) {
            if (!seen.add(parameter.name())) {
                out.report(Rule.GENERIC_DUPLICATE, "Generic parameter '" + parameter.name() + "' is declared twice",
                        parameter.location());
            } else if (outer.contains(parameter.name())) {
                out.report(Rule.GENERIC_DUPLICATE, "Generic parameter '" + parameter.name()
                        + "' shadows the parameter of the enclosing type", parameter.location());
            }
        }
    }

    private static void checkFinalOverrides(final SpecModel model, final Declaration.TypeDeclaration type,
                                            final DiagnosticCollector out) {
        for (final Declaration.TypeDeclaration ancestor : model.ancestors(type)) {
            for (final Method inherited : ancestor.methods()) {
                if (!inherited.hasAnnotation("finalMethod")) {
                    continue;
                }
                for (final Method method : type.methods()) {
                    if (method.signature().equals(inherited.signature())) {
                        out.report(Rule.FINAL_METHOD_OVERRIDDEN, "'" + method.signature() + "' is @@finalMethod in '"
                                + ancestor.name() + "' and must not be re-declared", method.location());
                    }
                }
            }
        }
    }
}
