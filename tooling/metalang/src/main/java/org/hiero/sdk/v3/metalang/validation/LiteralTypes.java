package org.hiero.sdk.v3.metalang.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.hiero.sdk.v3.metalang.ast.Declaration;
import org.hiero.sdk.v3.metalang.ast.EnumValue;
import org.hiero.sdk.v3.metalang.ast.Field;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.ast.SchemaFile;
import org.hiero.sdk.v3.metalang.ast.TypeRef;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;
import org.hiero.sdk.v3.metalang.semantic.ResolvedType;
import org.hiero.sdk.v3.metalang.semantic.SpecModel;

/**
 * Checks whether a literal is a valid value for a type. Used for {@code @@default} values and
 * constants. Types the tool cannot reason about (generic parameters, unresolved types) accept every
 * literal so that only definite mismatches are reported.
 */
final class LiteralTypes {

    private LiteralTypes() {
    }

    /**
     * Returns a problem description if the literal does not fit the type.
     *
     * @param model    the model
     * @param file     the file in whose scope {@code type} is resolved
     * @param literal  the literal
     * @param type     the expected type
     * @param nullable whether {@code null} is acceptable
     * @return the problem, or empty if the literal fits
     */
    static Optional<String> mismatch(final SpecModel model, final SchemaFile file, final Literal literal,
                                     final TypeRef type, final boolean nullable) {
        if (literal instanceof Literal.NameLiteral name && name.text().equals("null")) {
            return nullable ? Optional.empty() : Optional.of("'null' is not allowed for non-nullable type '"
                    + type.text() + "'");
        }
        if (!(type instanceof TypeRef.Named named)) {
            return Optional.empty();
        }
        return switch (model.resolve(file, named)) {
            case ResolvedType.Builtin builtin -> builtinMismatch(literal, builtin.type(), type);
            case ResolvedType.Declared declared -> declaredMismatch(model, literal, declared, type);
            case ResolvedType.Unresolved ignored -> Optional.empty();
        };
    }

    private static Optional<String> builtinMismatch(final Literal literal, final BuiltinType builtin,
                                                    final TypeRef type) {
        final boolean fits = switch (builtin.category()) {
            case INTEGER -> literal instanceof Literal.NumberLiteral n && !n.text().contains(".")
                    && (!n.text().startsWith("-") || !builtin.name().startsWith("u"));
            case FLOAT, DECIMAL, DURATION -> literal instanceof Literal.NumberLiteral;
            case STRING, UUID, TEMPORAL -> literal instanceof Literal.StringLiteral;
            case BOOL -> literal instanceof Literal.NameLiteral n
                    && (n.text().equals("true") || n.text().equals("false"));
            case BYTES, COLLECTION, MAP -> literal instanceof Literal.ListLiteral;
            case TYPE, STREAM_RESULT -> true;
        };
        return fits ? Optional.empty() : Optional.of("Value " + literal.text() + " is not a valid '" + type.text()
                + "'");
    }

    private static Optional<String> declaredMismatch(final SpecModel model, final Literal literal,
                                                     final ResolvedType.Declared declared, final TypeRef type) {
        switch (declared.declaration()) {
            case Declaration.EnumType enumType -> {
                if (literal instanceof Literal.NameLiteral name) {
                    final String valueName = name.text().substring(name.text().lastIndexOf('.') + 1);
                    if (enumType.values().stream().map(EnumValue::name).anyMatch(valueName::equals)
                            || !enumType.placeholders().isEmpty()) {
                        return Optional.empty();
                    }
                    return Optional.of("'" + valueName + "' is not a value of enum '" + enumType.name() + "'");
                }
                return Optional.of("Value " + literal.text() + " is not a value of enum '" + enumType.name() + "'");
            }
            case Declaration.ComplexType complexType -> {
                if (!(literal instanceof Literal.StructLiteral struct)) {
                    return Optional.of("Value " + literal.text() + " is not a valid '" + type.text()
                            + "'; use a struct literal '" + complexType.name() + "{...}'");
                }
                final String structSimpleName = struct.typeName().substring(struct.typeName().lastIndexOf('.') + 1);
                if (!structSimpleName.equals(complexType.name())) {
                    return Optional.of("Struct literal of type '" + struct.typeName() + "' is not a '"
                            + complexType.name() + "'");
                }
                return structFieldMismatch(model, struct, complexType);
            }
        }
    }

    private static Optional<String> structFieldMismatch(final SpecModel model, final Literal.StructLiteral struct,
                                                        final Declaration.ComplexType type) {
        final List<FieldWithFile> fields = allFields(model, type);
        for (final Literal.StructEntry entry : struct.entries()) {
            final Optional<FieldWithFile> field = fields.stream()
                    .filter(f -> f.field().name().equals(entry.name()))
                    .findFirst();
            if (field.isEmpty()) {
                return Optional.of("'" + type.name() + "' has no field '" + entry.name() + "'");
            }
            final Optional<String> nested = mismatch(model, field.get().file(), entry.value(),
                    field.get().field().type(), field.get().field().hasAnnotation("nullable"));
            if (nested.isPresent()) {
                return Optional.of("Field '" + entry.name() + "': " + nested.get());
            }
        }
        return Optional.empty();
    }

    /**
     * A field together with the file that declares it (needed to resolve the field's type).
     */
    record FieldWithFile(Field field, SchemaFile file, Declaration.TypeDeclaration owner) {
    }

    /**
     * Returns the own and inherited fields of a type, own fields first.
     */
    static List<FieldWithFile> allFields(final SpecModel model, final Declaration.TypeDeclaration type) {
        final List<FieldWithFile> result = new ArrayList<>();
        type.fields().forEach(f -> result.add(new FieldWithFile(f, model.fileOf(type), type)));
        for (final Declaration.TypeDeclaration ancestor : model.ancestors(type)) {
            ancestor.fields().forEach(f -> result.add(new FieldWithFile(f, model.fileOf(ancestor), ancestor)));
        }
        return result;
    }
}
