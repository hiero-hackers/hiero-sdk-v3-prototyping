package org.hiero.sdk.v3.metalang.validation;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.hiero.sdk.v3.metalang.ast.Declaration;
import org.hiero.sdk.v3.metalang.ast.EnumValue;
import org.hiero.sdk.v3.metalang.ast.Field;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.ast.Method;
import org.hiero.sdk.v3.metalang.ast.Parameter;
import org.hiero.sdk.v3.metalang.ast.SchemaFile;
import org.hiero.sdk.v3.metalang.diagnostic.DiagnosticCollector;
import org.hiero.sdk.v3.metalang.diagnostic.Rule;
import org.hiero.sdk.v3.metalang.semantic.SpecModel;

/**
 * Rules for enumerations (guideline section "Enumerations"), including the attribute list
 * {@code enum Name(attr: Type)} and the positional attribute values of each enum value.
 */
final class EnumCheck implements Check {

    @Override
    public void run(final SpecModel model, final DiagnosticCollector out) {
        for (final SchemaFile file : model.files()) {
            for (final Declaration declaration : file.declarations()) {
                if (declaration instanceof Declaration.EnumType enumType) {
                    check(model, file, enumType, out);
                }
            }
        }
    }

    private static void check(final SpecModel model, final SchemaFile file, final Declaration.EnumType enumType,
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
            out.report(Rule.ENUM_BODY_ATTRIBUTE, "Move attribute '" + field.name() + "' of enum '" + enumType.name()
                    + "' into the attribute list: 'enum " + enumType.name() + "(" + field.name() + ": "
                    + field.type().text() + ")'", field.location());
        }
        for (final Method method : enumType.methods()) {
            if (method.name().equals("values") && method.parameters().isEmpty()) {
                out.report(Rule.ENUM_EXPLICIT_VALUES_METHOD, "Remove 'values()' from enum '" + enumType.name()
                        + "'; it is provided implicitly", method.location());
            }
        }
        checkAttributes(model, file, enumType, out);
        checkArguments(model, file, enumType, out);
    }

    private static void checkAttributes(final SpecModel model, final SchemaFile file,
                                        final Declaration.EnumType enumType, final DiagnosticCollector out) {
        final Set<String> names = new HashSet<>();
        for (final Parameter attribute : enumType.attributes()) {
            if (!names.add(attribute.name())) {
                out.report(Rule.ENUM_ATTRIBUTE_INVALID, "Attribute '" + attribute.name() + "' is declared twice",
                        attribute.location());
            }
            if (attribute.varargs()) {
                out.report(Rule.ENUM_ATTRIBUTE_INVALID, "Attribute '" + attribute.name() + "' must not be varargs",
                        attribute.location());
            }
        }
        for (final Declaration.TypeDeclaration ancestor : model.ancestors(enumType)) {
            for (final Field inherited : ancestor.fields()) {
                final Optional<Parameter> attribute = enumType.attributes().stream()
                        .filter(a -> a.name().equals(inherited.name()))
                        .findFirst();
                if (attribute.isEmpty()) {
                    out.report(Rule.ENUM_INHERITED_ATTRIBUTE_MISSING, "Add '" + inherited.name() + ": "
                            + inherited.type().text() + "' (inherited from '" + ancestor.name()
                            + "') to the attribute list of '" + enumType.name() + "'", enumType.location());
                } else if (!InheritanceCheck.sameType(model, file, attribute.get().type(), model.fileOf(ancestor),
                        inherited.type())) {
                    out.report(Rule.ENUM_ATTRIBUTE_TYPE_MISMATCH, "'" + attribute.get().type().text()
                            + "' differs from '" + inherited.type().text() + "' in '" + ancestor.name() + "'",
                            attribute.get().location());
                }
            }
        }
    }

    private static void checkArguments(final SpecModel model, final SchemaFile file,
                                       final Declaration.EnumType enumType, final DiagnosticCollector out) {
        final List<Parameter> attributes = enumType.attributes();
        for (final EnumValue value : enumType.values()) {
            if (value.arguments().size() != attributes.size()) {
                out.report(Rule.ENUM_ARGUMENT_COUNT, "'" + value.name() + "' passes " + value.arguments().size()
                        + " argument(s) but '" + enumType.name() + "' declares " + attributes.size()
                        + " attribute(s)", value.location());
                continue;
            }
            for (int i = 0; i < attributes.size(); i++) {
                final Parameter attribute = attributes.get(i);
                final Literal argument = value.arguments().get(i);
                LiteralTypes.mismatch(model, file, argument, attribute.type(), attribute.hasAnnotation("nullable"))
                        .ifPresent(m -> out.report(Rule.ENUM_ARGUMENT_TYPE, "'" + value.name() + "', attribute '"
                                + attribute.name() + "': " + m, argument.location()));
            }
        }
    }
}
