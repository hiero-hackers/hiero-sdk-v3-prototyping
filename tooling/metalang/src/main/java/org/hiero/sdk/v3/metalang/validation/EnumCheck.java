package org.hiero.sdk.v3.metalang.validation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.hiero.sdk.v3.metalang.ast.Declaration;
import org.hiero.sdk.v3.metalang.ast.EnumValue;
import org.hiero.sdk.v3.metalang.ast.Field;
import org.hiero.sdk.v3.metalang.ast.Method;
import org.hiero.sdk.v3.metalang.ast.SchemaFile;
import org.hiero.sdk.v3.metalang.diagnostic.DiagnosticCollector;
import org.hiero.sdk.v3.metalang.diagnostic.Rule;
import org.hiero.sdk.v3.metalang.semantic.SpecModel;

/**
 * Rules for enumerations (guideline section "Enumerations").
 */
final class EnumCheck implements Check {

    @Override
    public void run(final SpecModel model, final DiagnosticCollector out) {
        for (final SchemaFile file : model.files()) {
            for (final Declaration declaration : file.declarations()) {
                if (declaration instanceof Declaration.EnumType enumType) {
                    check(model, enumType, out);
                }
            }
        }
    }

    private static void check(final SpecModel model, final Declaration.EnumType enumType,
                              final DiagnosticCollector out) {
        if (enumType.values().isEmpty()) {
            out.report(Rule.ENUM_EMPTY, "Enum '" + enumType.name() + "' declares no values", enumType.location());
        }
        final Set<String> names = new HashSet<>();
        for (final EnumValue value : enumType.values()) {
            if (!names.add(value.name())) {
                out.report(Rule.ENUM_DUPLICATE_VALUE, "Value '" + value.name() + "' is declared twice in '"
                        + enumType.name() + "'", value.location());
            }
        }
        for (final Field field : enumType.fields()) {
            if (!field.hasAnnotation("immutable")) {
                out.report(Rule.ENUM_FIELD_NOT_IMMUTABLE, "Attribute '" + field.name() + "' of enum '"
                        + enumType.name() + "' must be @@immutable", field.location());
            }
        }
        for (final Method method : enumType.methods()) {
            if (method.name().equals("values") && method.parameters().isEmpty()) {
                out.report(Rule.ENUM_EXPLICIT_VALUES_METHOD, "Remove 'values()' from enum '" + enumType.name()
                        + "'; it is provided implicitly", method.location());
            }
        }
        final List<String> attributes = LiteralTypes.allFields(model, enumType).stream()
                .map(f -> f.field().name())
                .distinct()
                .toList();
        if (!attributes.isEmpty()) {
            out.report(Rule.ENUM_UNASSIGNABLE_FIELDS, "Enum '" + enumType.name() + "' has attribute(s) "
                    + String.join(", ", attributes) + " whose per-value values can only be given in comments",
                    enumType.location());
        }
    }
}
