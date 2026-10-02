package org.hiero.sdk.v3.metalang.validation;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import org.hiero.sdk.v3.metalang.ast.Annotation;
import org.hiero.sdk.v3.metalang.ast.Declaration;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.ast.Parameter;
import org.hiero.sdk.v3.metalang.ast.Requires;
import org.hiero.sdk.v3.metalang.ast.SchemaFile;
import org.hiero.sdk.v3.metalang.ast.TypeRef;
import org.hiero.sdk.v3.metalang.diagnostic.DiagnosticCollector;
import org.hiero.sdk.v3.metalang.diagnostic.Rule;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;
import org.hiero.sdk.v3.metalang.semantic.ResolvedType;
import org.hiero.sdk.v3.metalang.semantic.SpecModel;

/**
 * Resolves every type reference and checks arity, integer widths, generic scoping, {@code ANY},
 * {@code void}, {@code streamResult} placement and unused imports.
 */
final class TypeReferenceCheck implements Check {

    @Override
    public void run(final SpecModel model, final DiagnosticCollector out) {
        final Map<SchemaFile, Set<String>> usedNames = new IdentityHashMap<>();
        Elements.forEachTypeSite(model, site -> {
            final Set<String> used = usedNames.computeIfAbsent(site.file(), k -> new HashSet<>());
            check(model, site.file(), site.type(), site.position(), site.genericScope(), true, used, out);
        });
        for (final SchemaFile file : model.files()) {
            final Set<String> used = usedNames.computeIfAbsent(file, k -> new HashSet<>());
            collectNonTypeRefUsages(file, used);
            for (final Requires requires : file.requires()) {
                for (final String type : requires.types()) {
                    if (!used.contains(type)) {
                        out.report(Rule.REQUIRES_UNUSED, "Imported type '" + type + "' from '" + requires.namespace()
                                + "' is never used", requires.location());
                    }
                }
            }
        }
    }

    /** Type names used outside of type references: @@sealed arguments, struct literals, function owners. */
    private static void collectNonTypeRefUsages(final SchemaFile file, final Set<String> used) {
        for (final Declaration declaration : file.declarations()) {
            declaration.annotation("sealed").map(Annotation::arguments).ifPresent(args ->
                    args.forEach(a -> used.add(a.text())));
            if (declaration instanceof Declaration.Constant constant) {
                collectStructTypes(constant.value(), used);
            }
            if (declaration instanceof Declaration.Function function && function.owner() != null) {
                used.add(function.owner());
            }
        }
    }

    private static void collectStructTypes(final Literal literal, final Set<String> used) {
        switch (literal) {
            case Literal.StructLiteral struct -> {
                used.add(struct.typeName());
                struct.entries().forEach(e -> collectStructTypes(e.value(), used));
            }
            case Literal.ListLiteral list -> list.items().forEach(i -> collectStructTypes(i, used));
            default -> {
                // no type names
            }
        }
    }

    private void check(final SpecModel model, final SchemaFile file, final TypeRef type,
                       final Elements.Position position, final Set<String> scope, final boolean topLevel,
                       final Set<String> used, final DiagnosticCollector out) {
        switch (type) {
            case TypeRef.Named named -> checkNamed(model, file, named, position, scope, topLevel, used, out);
            case TypeRef.GenericParameter generic -> {
                if (!scope.contains(generic.name())) {
                    out.report(Rule.GENERIC_UNDECLARED, "Generic parameter '" + generic.name()
                            + "' is not declared by the enclosing type", generic.location());
                }
            }
            case TypeRef.Any any -> out.report(Rule.TYPE_ANY_STANDALONE,
                    "Prefer a generic parameter, a concrete base type, a @@oneOf union or bytes over ANY",
                    any.location());
            case TypeRef.Void v -> {
                if (!topLevel || (position != Elements.Position.RETURN
                        && position != Elements.Position.STREAMING_RETURN)) {
                    out.report(Rule.TYPE_UNKNOWN, "'void' is only allowed as a return type", v.location());
                }
            }
            case TypeRef.Function function -> {
                check(model, file, function.returnType(), Elements.Position.RETURN, scope, true, used, out);
                for (final Parameter parameter : function.parameters()) {
                    check(model, file, parameter.type(), Elements.Position.PARAMETER, scope, true, used, out);
                }
            }
        }
    }

    private void checkNamed(final SpecModel model, final SchemaFile file, final TypeRef.Named named,
                            final Elements.Position position, final Set<String> scope, final boolean topLevel,
                            final Set<String> used, final DiagnosticCollector out) {
        used.add(named.name());
        final ResolvedType resolved = model.resolve(file, named);
        switch (resolved) {
            case ResolvedType.Unresolved unresolved -> out.report(unresolved.rule(), unresolved.message(),
                    named.location());
            case ResolvedType.Builtin builtin -> checkBuiltin(builtin.type(), named, position, topLevel, out);
            case ResolvedType.Declared declared -> {
                final int expected = declared.declaration().typeParameters().size();
                if (expected != named.arguments().size()) {
                    out.report(Rule.TYPE_ARITY, "'" + declared.declaration().name() + "' expects " + expected
                            + " type argument(s) but got " + named.arguments().size(), named.location());
                }
                if (named.isQualified() && model.resolve(file, named.simpleName()) instanceof ResolvedType.Declared d
                        && d.declaration() == declared.declaration()) {
                    out.report(Rule.TYPE_UNNECESSARY_QUALIFICATION, "Use the simple name '" + named.simpleName()
                            + "'", named.location());
                }
            }
        }
        for (final TypeRef.TypeArgument argument : named.arguments()) {
            switch (argument) {
                case TypeRef.Concrete concrete ->
                        check(model, file, concrete.type(), position, scope, false, used, out);
                case TypeRef.Wildcard wildcard -> {
                    if (wildcard.upperBound() != null) {
                        check(model, file, wildcard.upperBound(), position, scope, false, used, out);
                    }
                }
                case TypeRef.BoundedGeneric bounded -> {
                    out.report(Rule.SYNTAX_USE_SITE_BOUND, "Write '" + bounded.name() + "' here and declare the bound '"
                            + bounded.text() + "' on the type parameter", bounded.location());
                    check(model, file, bounded.bound(), position, scope, false, used, out);
                }
            }
        }
    }

    private static void checkBuiltin(final BuiltinType builtin, final TypeRef.Named named,
                                     final Elements.Position position, final boolean topLevel,
                                     final DiagnosticCollector out) {
        if (!builtin.hasValidWidth()) {
            out.report(Rule.TYPE_INT_WIDTH, "Integer width " + builtin.bits() + " of '" + builtin.name()
                    + "' is outside " + BuiltinType.MIN_INT_BITS + ".." + BuiltinType.MAX_INT_BITS, named.location());
        }
        if (builtin.arity() != named.arguments().size()) {
            out.report(Rule.TYPE_ARITY, "'" + builtin.name() + "' expects " + builtin.arity()
                    + " type argument(s) but got " + named.arguments().size(), named.location());
        }
        if (builtin.category() == BuiltinType.Category.STREAM_RESULT
                && !(topLevel && position == Elements.Position.STREAMING_RETURN)) {
            out.report(Rule.TYPE_STREAM_RESULT_OUTSIDE_STREAMING,
                    "streamResult<T> is only meaningful as the return type of a @@streaming method", named.location());
        }
    }
}
