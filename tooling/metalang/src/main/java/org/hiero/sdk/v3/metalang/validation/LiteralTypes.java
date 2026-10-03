package org.hiero.sdk.v3.metalang.validation;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.hiero.sdk.v3.metalang.ast.Annotated;
import org.hiero.sdk.v3.metalang.ast.Annotation;
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
        if (builtin.category() == BuiltinType.Category.INTEGER && builtin.hasValidWidth()
                && literal instanceof Literal.NumberLiteral number && !number.text().contains(".")) {
            final BigInteger value = number.value().toBigIntegerExact();
            final boolean unsigned = builtin.name().startsWith("u");
            final BigInteger min = unsigned ? BigInteger.ZERO : BigInteger.TWO.pow(builtin.bits() - 1).negate();
            final BigInteger max = unsigned ? BigInteger.TWO.pow(builtin.bits()).subtract(BigInteger.ONE)
                    : BigInteger.TWO.pow(builtin.bits() - 1).subtract(BigInteger.ONE);
            if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
                return Optional.of("Value " + number.text() + " is outside the range of '" + type.text() + "' ("
                        + min + ".." + max + ")");
            }
        }
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
                    if (enumType.values().stream().map(EnumValue::name).anyMatch(valueName::equals)) {
                        return Optional.empty();
                    }
                    return Optional.of("'" + valueName + "' is not a value of enum '" + enumType.name() + "'");
                }
                return Optional.of("Value " + literal.text() + " is not a value of enum '" + enumType.name() + "'");
            }
            case Declaration.ComplexType complexType -> {
                if (complexType.abstraction()) {
                    return Optional.of("'" + complexType.name() + "' is an abstraction and has no literal form");
                }
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
        // every field that is neither @@nullable nor has a @@default must be given (an override in a subtype wins)
        final Set<String> seen = new HashSet<>();
        for (final FieldWithFile field : fields) {
            if (!seen.add(field.field().name())) {
                continue;
            }
            final boolean given = struct.entries().stream().anyMatch(e -> e.name().equals(field.field().name()));
            if (!given && !field.field().hasAnnotation("nullable") && !field.field().hasAnnotation("default")) {
                return Optional.of("Missing value for field '" + field.field().name() + "' of '" + type.name() + "'");
            }
        }
        return Optional.empty();
    }

    /**
     * Checks a value against the validation annotations of the element it is assigned to ({@code @@min},
     * {@code @@max}, {@code @@minLength}, {@code @@maxLength}, {@code @@minSize}, {@code @@maxSize},
     * {@code @@pattern}, {@code @@urlPattern}). Values the check cannot interpret are accepted.
     *
     * @param literal the value
     * @param element the annotated field, parameter or enum attribute
     * @return the violated constraint, if any
     */
    static Optional<String> constraintViolation(final Literal literal, final Annotated element) {
        for (final Annotation annotation : element.annotations()) {
            final Optional<String> violation = constraintViolation(literal, annotation);
            if (violation.isPresent()) {
                return violation;
            }
        }
        return Optional.empty();
    }

    private static Optional<String> constraintViolation(final Literal literal, final Annotation annotation) {
        final Optional<BigDecimal> bound = annotation.arguments().size() == 1
                && annotation.arguments().getFirst() instanceof Literal.NumberLiteral n
                ? Optional.of(n.value()) : Optional.empty();
        final Optional<BigDecimal> actual = switch (annotation.name()) {
            case "min", "max" -> literal instanceof Literal.NumberLiteral n ? Optional.of(n.value()) : Optional.empty();
            case "minLength", "maxLength" -> literal instanceof Literal.StringLiteral s
                    ? Optional.of(BigDecimal.valueOf(s.value().codePointCount(0, s.value().length())))
                    : Optional.empty();
            case "minSize", "maxSize" -> literal instanceof Literal.ListLiteral l
                    ? Optional.of(BigDecimal.valueOf(l.items().size())) : Optional.empty();
            default -> Optional.empty();
        };
        if (bound.isPresent() && actual.isPresent()) {
            final boolean lower = annotation.name().startsWith("min");
            final int comparison = actual.get().compareTo(bound.get());
            if (lower ? comparison < 0 : comparison > 0) {
                return Optional.of("Value " + literal.text() + " violates @@" + annotation.name() + "("
                        + bound.get().toPlainString() + ")");
            }
        }
        if (literal instanceof Literal.StringLiteral string) {
            if (annotation.name().equals("pattern") && annotation.arguments().size() == 1
                    && annotation.arguments().getFirst() instanceof Literal.StringLiteral regex) {
                try {
                    if (!Pattern.compile(regex.value()).matcher(string.value()).find()) {
                        return Optional.of("Value " + literal.text() + " does not match @@pattern(" + regex.text() + ")");
                    }
                } catch (final PatternSyntaxException e) {
                    return Optional.empty(); // reported by AnnotationCheck
                }
            }
            if (annotation.name().equals("urlPattern") && !isAbsoluteUrl(string.value())) {
                return Optional.of("Value " + literal.text() + " is not an absolute URL (@@urlPattern)");
            }
        }
        return Optional.empty();
    }

    private static boolean isAbsoluteUrl(final String value) {
        try {
            final URI uri = new URI(value);
            return uri.isAbsolute() && uri.getHost() != null;
        } catch (final URISyntaxException e) {
            return false;
        }
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
