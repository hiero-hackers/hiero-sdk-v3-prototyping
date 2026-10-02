package org.hiero.sdk.v3.metalang.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.hiero.sdk.v3.metalang.ast.Annotation;
import org.hiero.sdk.v3.metalang.ast.Declaration;
import org.hiero.sdk.v3.metalang.ast.Field;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.ast.SchemaFile;
import org.hiero.sdk.v3.metalang.diagnostic.DiagnosticCollector;
import org.hiero.sdk.v3.metalang.diagnostic.Rule;
import org.hiero.sdk.v3.metalang.semantic.SpecModel;

/**
 * Rules for {@code @@oneOf} and {@code @@oneOrNoneOf} (guideline section "Complex Type annotations").
 */
final class OneOfCheck implements Check {

    @Override
    public void run(final SpecModel model, final DiagnosticCollector out) {
        for (final SchemaFile file : model.files()) {
            for (final Declaration.TypeDeclaration type : file.types()) {
                for (final Annotation annotation : type.annotations()) {
                    if (annotation.name().equals("oneOf") || annotation.name().equals("oneOrNoneOf")) {
                        check(model, type, annotation, out);
                    }
                }
            }
        }
    }

    private static void check(final SpecModel model, final Declaration.TypeDeclaration type,
                              final Annotation annotation, final DiagnosticCollector out) {
        final List<LiteralTypes.FieldWithFile> allFields = LiteralTypes.allFields(model, type);
        final List<Field> listed = new ArrayList<>();
        for (final Literal argument : annotation.arguments()) {
            final Optional<Field> field = allFields.stream()
                    .map(LiteralTypes.FieldWithFile::field)
                    .filter(f -> f.name().equals(argument.text()))
                    .findFirst();
            if (field.isEmpty()) {
                out.report(Rule.ONEOF_UNKNOWN_FIELD, "'" + type.name() + "' has no field '" + argument.text() + "'",
                        argument.location());
                continue;
            }
            listed.add(field.get());
            if (!field.get().hasAnnotation("nullable")) {
                out.report(Rule.ONEOF_NOT_NULLABLE, "Field '" + field.get().name() + "' listed in @@"
                        + annotation.name() + " must be @@nullable", field.get().location());
            }
        }
        final long immutable = listed.stream().filter(f -> f.hasAnnotation("immutable")).count();
        if (immutable != 0 && immutable != listed.size()) {
            out.report(Rule.ONEOF_MIXED_IMMUTABILITY, "Fields of @@" + annotation.name()
                    + " must be all @@immutable or all mutable", annotation.location());
        }
        final long defaults = listed.stream().filter(f -> f.hasAnnotation("default")).count();
        if (defaults > 1) {
            out.report(Rule.ONEOF_MULTIPLE_DEFAULTS, defaults + " fields of @@" + annotation.name()
                    + " declare a @@default; at most one may", annotation.location());
        }
    }
}
