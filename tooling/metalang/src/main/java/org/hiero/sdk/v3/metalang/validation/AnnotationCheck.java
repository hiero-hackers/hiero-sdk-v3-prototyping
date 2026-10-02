package org.hiero.sdk.v3.metalang.validation;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.hiero.sdk.v3.metalang.ast.Annotated;
import org.hiero.sdk.v3.metalang.ast.Annotation;
import org.hiero.sdk.v3.metalang.ast.Declaration;
import org.hiero.sdk.v3.metalang.ast.Field;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.ast.Method;
import org.hiero.sdk.v3.metalang.ast.Parameter;
import org.hiero.sdk.v3.metalang.ast.SchemaFile;
import org.hiero.sdk.v3.metalang.ast.TypeRef;
import org.hiero.sdk.v3.metalang.diagnostic.DiagnosticCollector;
import org.hiero.sdk.v3.metalang.diagnostic.Rule;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;
import org.hiero.sdk.v3.metalang.semantic.KnownAnnotation;
import org.hiero.sdk.v3.metalang.semantic.KnownAnnotation.ElementKind;
import org.hiero.sdk.v3.metalang.semantic.ResolvedType;
import org.hiero.sdk.v3.metalang.semantic.SpecModel;

/**
 * Checks annotations: known names, allowed targets, argument shapes, duplicates, compatibility of
 * validation annotations with the annotated type, and {@code @@default}/constant values.
 */
final class AnnotationCheck implements Check {

    private static final Pattern INTEGER = Pattern.compile("[0-9][0-9_]*");
    private static final Pattern SIMPLE_NAME = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");

    @Override
    public void run(final SpecModel model, final DiagnosticCollector out) {
        for (final SchemaFile file : model.files()) {
            for (final Declaration declaration : file.declarations()) {
                switch (declaration) {
                    case Declaration.ComplexType type -> {
                        checkElement(type, type.abstraction() ? ElementKind.ABSTRACTION : ElementKind.COMPLEX_TYPE,
                                "type '" + type.name() + "'", out);
                        checkMembers(model, file, type.fields(), type.methods(), out);
                        checkRedundantThreadSafe(type, out);
                    }
                    case Declaration.EnumType enumType -> {
                        checkElement(enumType, ElementKind.ENUM, "enum '" + enumType.name() + "'", out);
                        enumType.values().forEach(v -> checkElement(v, ElementKind.ENUM_VALUE,
                                "enum value '" + v.name() + "'", out));
                        checkMembers(model, file, enumType.fields(), enumType.methods(), out);
                        checkRedundantThreadSafe(enumType, out);
                    }
                    case Declaration.Constant constant -> {
                        checkElement(constant, ElementKind.CONSTANT, "constant '" + constant.name() + "'", out);
                        LiteralTypes.mismatch(model, file, constant.value(), constant.type(), false)
                                .ifPresent(m -> out.report(Rule.CONSTANT_VALUE_TYPE, m, constant.value().location()));
                    }
                    case Declaration.Function function -> checkMethod(model, file, function.method(),
                            ElementKind.FUNCTION, out);
                }
            }
        }
    }

    private static void checkRedundantThreadSafe(final Declaration.TypeDeclaration type,
                                                 final DiagnosticCollector out) {
        if (!type.hasAnnotation("threadSafe")) {
            return;
        }
        for (final Method method : type.methods()) {
            method.annotation("threadSafe").ifPresent(a -> out.report(Rule.ANNOTATION_REDUNDANT_THREAD_SAFE,
                    "'" + method.name() + "' is already covered by @@threadSafe on type '" + type.name() + "'",
                    a.location()));
        }
    }

    private void checkMembers(final SpecModel model, final SchemaFile file, final List<Field> fields,
                              final List<Method> methods, final DiagnosticCollector out) {
        for (final Field field : fields) {
            checkElement(field, ElementKind.FIELD, "field '" + field.name() + "'", out);
            checkValueAnnotations(model, file, field, field.type(), false, out);
            field.annotation("default").ifPresent(d -> checkDefault(model, file, field, d, out));
        }
        for (final Method method : methods) {
            checkMethod(model, file, method, ElementKind.METHOD, out);
        }
    }

    private void checkMethod(final SpecModel model, final SchemaFile file, final Method method,
                             final ElementKind kind, final DiagnosticCollector out) {
        checkElement(method, kind, "method '" + method.name() + "'", out);
        for (final Parameter parameter : method.parameters()) {
            checkElement(parameter, ElementKind.PARAMETER, "parameter '" + parameter.name() + "'", out);
            checkValueAnnotations(model, file, parameter, parameter.type(), parameter.varargs(), out);
        }
    }

    private void checkElement(final Annotated element, final ElementKind kind, final String description,
                              final DiagnosticCollector out) {
        final Set<String> seen = new HashSet<>();
        for (final Annotation annotation : element.annotations()) {
            if (!seen.add(annotation.name())) {
                out.report(Rule.ANNOTATION_DUPLICATE, "@@" + annotation.name() + " is repeated on " + description,
                        annotation.location());
            }
            final Optional<KnownAnnotation> known = KnownAnnotation.byName(annotation.name());
            if (known.isEmpty()) {
                out.report(Rule.ANNOTATION_UNKNOWN, "Unknown annotation @@" + annotation.name()
                        + suggestion(annotation.name()), annotation.location());
                continue;
            }
            if (!known.get().targets().contains(kind)) {
                out.report(Rule.ANNOTATION_TARGET, "@@" + annotation.name() + " is not allowed on " + description,
                        annotation.location());
            }
            checkArguments(known.get(), annotation, out);
            if (annotation.parenthesized() && annotation.arguments().isEmpty()
                    && (known.get().arguments() == KnownAnnotation.Arguments.NONE
                    || known.get().arguments() == KnownAnnotation.Arguments.OPTIONAL_NAME)) {
                out.report(Rule.ANNOTATION_EMPTY_PARENTHESES, "Write '@@" + annotation.name() + "' instead of '@@"
                        + annotation.name() + "()'", annotation.location());
            }
        }
    }

    private static String suggestion(final String name) {
        return switch (name) {
            case "positiveValue" -> "; use @@min(1) (or @@min(0) if zero is allowed)";
            case "final" -> "; use @@finalType";
            case "optional" -> "; use @@nullable";
            default -> "";
        };
    }

    private static void checkArguments(final KnownAnnotation known, final Annotation annotation,
                                       final DiagnosticCollector out) {
        final List<Literal> args = annotation.arguments();
        final String problem = switch (known.arguments()) {
            case NONE -> args.isEmpty() ? null : "takes no arguments";
            case OPTIONAL_NAME -> args.isEmpty() || (args.size() == 1 && isSimpleName(args.getFirst()))
                    ? null : "takes at most one group name";
            case ONE_LITERAL -> args.size() == 1 ? null : "takes exactly one value";
            case ONE_NUMBER -> args.size() == 1 && args.getFirst() instanceof Literal.NumberLiteral
                    ? null : "takes exactly one number";
            case ONE_NON_NEGATIVE_INTEGER -> args.size() == 1 && args.getFirst() instanceof Literal.NumberLiteral n
                    && INTEGER.matcher(n.text()).matches() ? null : "takes exactly one non-negative integer";
            case ONE_STRING -> args.size() == 1 && args.getFirst() instanceof Literal.StringLiteral s
                    ? regexProblem(s.value()) : "takes exactly one string";
            case NAMES -> !args.isEmpty() && args.stream().allMatch(a -> a instanceof Literal.NameLiteral)
                    ? null : "takes one or more names";
            case AT_LEAST_TWO_NAMES -> args.size() >= 2 && args.stream().allMatch(AnnotationCheck::isSimpleName)
                    ? null : "takes two or more field names";
        };
        if (problem != null) {
            out.report(Rule.ANNOTATION_ARGUMENTS, "@@" + annotation.name() + " " + problem, annotation.location());
        }
    }

    private static boolean isSimpleName(final Literal literal) {
        return literal instanceof Literal.NameLiteral name && SIMPLE_NAME.matcher(name.text()).matches();
    }

    private static String regexProblem(final String regex) {
        try {
            Pattern.compile(regex);
            return null;
        } catch (final PatternSyntaxException e) {
            return "has an invalid regular expression: " + e.getDescription();
        }
    }

    private void checkValueAnnotations(final SpecModel model, final SchemaFile file, final Annotated element,
                                       final TypeRef type, final boolean varargs, final DiagnosticCollector out) {
        final BuiltinType builtin = varargs ? null : builtinOf(model, file, type);
        for (final Annotation annotation : element.annotations()) {
            switch (annotation.name()) {
                case "min", "max" -> {
                    if (builtin == null || !(builtin.isNumeric() || builtin.category() == BuiltinType.Category.DURATION)) {
                        out.report(Rule.ANNOTATION_VALUE_TYPE, "@@" + annotation.name()
                                + " requires a numeric type, not '" + type.text() + "'", annotation.location());
                    }
                }
                case "minLength", "maxLength" -> {
                    if (varargs || builtin == null || builtin.category() != BuiltinType.Category.STRING) {
                        out.report(Rule.ANNOTATION_VALUE_TYPE, "@@" + annotation.name()
                                + " requires a string type, not '" + describe(type, varargs) + "'"
                                + (isSized(builtin, varargs) ? "; use @@" + annotation.name().replace("Length", "Size")
                                : ""), annotation.location());
                    }
                }
                case "minSize", "maxSize" -> {
                    if (!isSized(builtin, varargs)) {
                        out.report(Rule.ANNOTATION_VALUE_TYPE, "@@" + annotation.name()
                                + " requires a list, set, map, bytes or varargs type, not '" + type.text() + "'"
                                + (builtin != null && builtin.category() == BuiltinType.Category.STRING
                                ? "; use @@" + annotation.name().replace("Size", "Length") : ""),
                                annotation.location());
                    }
                }
                case "pattern", "urlPattern" -> {
                    if (builtin == null || builtin.category() != BuiltinType.Category.STRING) {
                        out.report(Rule.ANNOTATION_VALUE_TYPE, "@@" + annotation.name()
                                + " requires a string type, not '" + type.text() + "'", annotation.location());
                    }
                }
                default -> {
                    // not a validation annotation
                }
            }
        }
    }

    private static boolean isSized(final BuiltinType builtin, final boolean varargs) {
        return varargs || (builtin != null && (builtin.isCollection() || builtin.category() == BuiltinType.Category.BYTES));
    }

    private static String describe(final TypeRef type, final boolean varargs) {
        return type.text() + (varargs ? "..." : "");
    }

    private static BuiltinType builtinOf(final SpecModel model, final SchemaFile file, final TypeRef type) {
        if (type instanceof TypeRef.Named named && model.resolve(file, named) instanceof ResolvedType.Builtin builtin) {
            return builtin.type();
        }
        return null;
    }

    private static void checkDefault(final SpecModel model, final SchemaFile file, final Field field,
                                     final Annotation annotation, final DiagnosticCollector out) {
        if (annotation.arguments().size() != 1) {
            return;
        }
        LiteralTypes.mismatch(model, file, annotation.arguments().getFirst(), field.type(),
                        field.hasAnnotation("nullable"))
                .ifPresent(m -> out.report(Rule.DEFAULT_VALUE_TYPE, "@@default of '" + field.name() + "': " + m,
                        annotation.location()));
    }
}
