package org.hiero.sdk.v3.metalang.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.hiero.sdk.v3.metalang.ast.Annotation;
import org.hiero.sdk.v3.metalang.ast.Declaration;
import org.hiero.sdk.v3.metalang.ast.Field;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.ast.SchemaFile;
import org.hiero.sdk.v3.metalang.ast.TypeRef;
import org.hiero.sdk.v3.metalang.diagnostic.DiagnosticCollector;
import org.hiero.sdk.v3.metalang.diagnostic.Rule;
import org.hiero.sdk.v3.metalang.semantic.ResolvedType;
import org.hiero.sdk.v3.metalang.semantic.SpecModel;

/**
 * Inheritance rules: valid super types, cycles, {@code @@finalType}, {@code @@sealed} and the
 * {@code @@override} narrowing of inherited nullability.
 */
final class InheritanceCheck implements Check {

    @Override
    public void run(final SpecModel model, final DiagnosticCollector out) {
        for (final SchemaFile file : model.files()) {
            for (final Declaration.TypeDeclaration type : file.types()) {
                checkSupertypes(model, file, type, out);
                checkSealed(model, file, type, out);
                checkOverrides(model, type, out);
                if (type instanceof Declaration.ComplexType complex && complex.abstraction()
                        && complex.hasAnnotation("finalType")) {
                    out.report(Rule.FINAL_TYPE_ON_ABSTRACTION, "Abstraction '" + type.name()
                            + "' cannot be @@finalType", type.location());
                }
            }
        }
        checkCycles(model, out);
    }

    private static void checkSupertypes(final SpecModel model, final SchemaFile file,
                                        final Declaration.TypeDeclaration type, final DiagnosticCollector out) {
        if (type.supertypes().size() > 1) {
            out.report(Rule.EXTENDS_MULTIPLE, "'" + type.name() + "' extends " + type.supertypes().size()
                    + " types", type.location());
        }
        for (final TypeRef supertype : type.supertypes()) {
            if (!(supertype instanceof TypeRef.Named named)) {
                out.report(Rule.EXTENDS_INVALID, "'" + supertype.text() + "' cannot be extended", supertype.location());
                continue;
            }
            switch (model.resolve(file, named)) {
                case ResolvedType.Builtin builtin -> out.report(Rule.EXTENDS_INVALID, "Basic data type '"
                        + builtin.type().name() + "' cannot be extended", supertype.location());
                case ResolvedType.Unresolved ignored -> {
                    // reported by TypeReferenceCheck
                }
                case ResolvedType.Declared declared -> {
                    switch (declared.declaration()) {
                        case Declaration.EnumType enumType -> out.report(Rule.EXTENDS_INVALID, "Enum '"
                                + enumType.name() + "' cannot be extended", supertype.location());
                        case Declaration.ComplexType parent -> {
                            if (parent.hasAnnotation("finalType")) {
                                out.report(Rule.FINAL_TYPE_EXTENDED, "'" + parent.name() + "' is @@finalType",
                                        supertype.location());
                            }
                            if (type instanceof Declaration.EnumType && !parent.abstraction()) {
                                out.report(Rule.EXTENDS_INVALID, "An enum can only extend an abstraction, '"
                                        + parent.name() + "' is a concrete type", supertype.location());
                            }
                        }
                    }
                }
            }
        }
    }

    private static void checkSealed(final SpecModel model, final SchemaFile file,
                                    final Declaration.TypeDeclaration type, final DiagnosticCollector out) {
        final Optional<Annotation> sealed = type.annotation("sealed");
        if (sealed.isPresent()) {
            if (!(type instanceof Declaration.ComplexType complex && complex.abstraction())) {
                out.report(Rule.SEALED_NOT_ABSTRACTION, "'" + type.name() + "' is not an abstraction",
                        sealed.get().location());
            }
            for (final Literal argument : sealed.get().arguments()) {
                final ResolvedType resolved = model.resolve(file, argument.text());
                if (!(resolved instanceof ResolvedType.Declared declared)) {
                    out.report(Rule.SEALED_UNKNOWN_SUBTYPE, "'" + argument.text()
                            + "' is not a type visible in namespace '" + file.namespace() + "'", argument.location());
                } else if (model.directSupertypes(declared.declaration()).stream()
                        .noneMatch(p -> p.declaration() == type)) {
                    out.report(Rule.SEALED_SUBTYPE_NOT_EXTENDING, "'" + argument.text() + "' does not extend '"
                            + type.name() + "'", argument.location());
                }
            }
        }
        for (final ResolvedType.Declared parent : model.directSupertypes(type)) {
            parent.declaration().annotation("sealed").ifPresent(parentSealed -> {
                final boolean listed = parentSealed.arguments().stream()
                        .anyMatch(a -> a.text().equals(type.name())
                                && model.resolve(model.fileOf(parent.declaration()), a.text())
                                instanceof ResolvedType.Declared d && d.declaration() == type);
                if (!listed) {
                    out.report(Rule.SEALED_UNLISTED_SUBTYPE, "'" + type.name() + "' extends sealed '"
                            + parent.declaration().name() + "' but is not listed in its @@sealed", type.location());
                }
            });
        }
    }

    private static void checkOverrides(final SpecModel model, final Declaration.TypeDeclaration type,
                                       final DiagnosticCollector out) {
        final List<Declaration.TypeDeclaration> ancestors = model.ancestors(type);
        for (final Field field : type.fields()) {
            final Optional<LiteralTypes.FieldWithFile> parent = ancestors.stream()
                    .flatMap(a -> a.fields().stream()
                            .filter(f -> f.name().equals(field.name()))
                            .map(f -> new LiteralTypes.FieldWithFile(f, model.fileOf(a), a)))
                    .findFirst();
            final boolean override = field.hasAnnotation("override");
            if (parent.isEmpty()) {
                if (override) {
                    out.report(Rule.OVERRIDE_NO_PARENT_FIELD, "No parent of '" + type.name() + "' declares field '"
                            + field.name() + "'", field.location());
                }
                continue;
            }
            final Field parentField = parent.get().field();
            final String parentName = parent.get().owner().name();
            if (!override) {
                out.report(Rule.OVERRIDE_MISSING, "Field '" + field.name() + "' is inherited from '" + parentName
                        + "'; re-declaring it requires @@override", field.location());
                continue;
            }
            if (!sameType(model, model.fileOf(type), field.type(), parent.get().file(), parentField.type())) {
                out.report(Rule.OVERRIDE_TYPE_MISMATCH, "'" + field.type().text() + "' differs from '"
                        + parentField.type().text() + "' in '" + parentName + "'", field.location());
            }
            if (field.hasAnnotation("immutable") != parentField.hasAnnotation("immutable")) {
                out.report(Rule.OVERRIDE_IMMUTABILITY_MISMATCH, "@@immutable of '" + field.name()
                        + "' differs from '" + parentName + "'", field.location());
            }
            if (!parentField.hasAnnotation("nullable") || field.hasAnnotation("nullable")) {
                out.report(Rule.OVERRIDE_NOT_NARROWING, "@@override of '" + field.name()
                        + "' must turn a @@nullable parent field into a non-nullable one", field.location());
            }
        }
    }

    private static boolean sameType(final SpecModel model, final SchemaFile fileA, final TypeRef a,
                                    final SchemaFile fileB, final TypeRef b) {
        if (a instanceof TypeRef.GenericParameter || b instanceof TypeRef.GenericParameter) {
            return true;
        }
        if (a instanceof TypeRef.Named na && b instanceof TypeRef.Named nb) {
            final ResolvedType ra = model.resolve(fileA, na);
            final ResolvedType rb = model.resolve(fileB, nb);
            final boolean sameHead = switch (ra) {
                case ResolvedType.Declared da -> rb instanceof ResolvedType.Declared db
                        && da.declaration() == db.declaration();
                case ResolvedType.Builtin ba -> rb instanceof ResolvedType.Builtin bb
                        && ba.type().name().equals(bb.type().name());
                case ResolvedType.Unresolved ignored -> na.name().equals(nb.name());
            };
            if (!sameHead || na.arguments().size() != nb.arguments().size()) {
                return false;
            }
            for (int i = 0; i < na.arguments().size(); i++) {
                final TypeRef.TypeArgument argA = na.arguments().get(i);
                final TypeRef.TypeArgument argB = nb.arguments().get(i);
                final boolean same = argA instanceof TypeRef.Concrete ca && argB instanceof TypeRef.Concrete cb
                        ? sameType(model, fileA, ca.type(), fileB, cb.type())
                        : argA.text().equals(argB.text());
                if (!same) {
                    return false;
                }
            }
            return true;
        }
        return a.text().equals(b.text());
    }

    private static void checkCycles(final SpecModel model, final DiagnosticCollector out) {
        final Set<Declaration.TypeDeclaration> reported = Collections.newSetFromMap(new IdentityHashMap<>());
        final Map<Declaration.TypeDeclaration, Boolean> done = new IdentityHashMap<>();
        for (final SchemaFile file : model.files()) {
            for (final Declaration.TypeDeclaration type : file.types()) {
                findCycle(model, type, new ArrayList<>(), done, reported, out);
            }
        }
    }

    private static void findCycle(final SpecModel model, final Declaration.TypeDeclaration type,
                                  final List<Declaration.TypeDeclaration> path,
                                  final Map<Declaration.TypeDeclaration, Boolean> done,
                                  final Set<Declaration.TypeDeclaration> reported, final DiagnosticCollector out) {
        final int index = indexOf(path, type);
        if (index >= 0) {
            final List<Declaration.TypeDeclaration> cycle = path.subList(index, path.size());
            for (final Declaration.TypeDeclaration member : cycle) {
                if (reported.add(member)) {
                    out.report(Rule.EXTENDS_CYCLE, "'" + member.name() + "' is part of an inheritance cycle: "
                            + String.join(" -> ", cycle.stream().map(Declaration::name).toList()) + " -> "
                            + cycle.getFirst().name(), member.location());
                }
            }
            return;
        }
        if (done.containsKey(type)) {
            return;
        }
        path.add(type);
        for (final ResolvedType.Declared parent : model.directSupertypes(type)) {
            findCycle(model, parent.declaration(), path, done, reported, out);
        }
        path.removeLast();
        done.put(type, Boolean.TRUE);
    }

    private static int indexOf(final List<Declaration.TypeDeclaration> path, final Declaration.TypeDeclaration type) {
        for (int i = 0; i < path.size(); i++) {
            if (path.get(i) == type) {
                return i;
            }
        }
        return -1;
    }
}
