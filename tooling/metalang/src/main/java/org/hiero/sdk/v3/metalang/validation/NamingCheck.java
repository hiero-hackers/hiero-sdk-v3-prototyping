package org.hiero.sdk.v3.metalang.validation;

import java.util.regex.Pattern;
import org.hiero.sdk.v3.metalang.ast.Annotation;
import org.hiero.sdk.v3.metalang.ast.Declaration;
import org.hiero.sdk.v3.metalang.ast.EnumValue;
import org.hiero.sdk.v3.metalang.ast.Field;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.ast.Method;
import org.hiero.sdk.v3.metalang.ast.Parameter;
import org.hiero.sdk.v3.metalang.ast.SchemaFile;
import org.hiero.sdk.v3.metalang.ast.TypeParameter;
import org.hiero.sdk.v3.metalang.ast.TypeRef;
import org.hiero.sdk.v3.metalang.diagnostic.DiagnosticCollector;
import org.hiero.sdk.v3.metalang.diagnostic.Rule;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;
import org.hiero.sdk.v3.metalang.semantic.SpecModel;

/**
 * Naming conventions (guideline section "Naming conventions").
 */
final class NamingCheck implements Check {

    static final Pattern LOWER_CAMEL = Pattern.compile("[a-z][a-zA-Z0-9]*");
    static final Pattern PASCAL = Pattern.compile("[A-Z][a-zA-Z0-9]*");
    static final Pattern UPPER_SNAKE = Pattern.compile("[A-Z][A-Z0-9]*(_[A-Z0-9]+)*");
    static final Pattern KEBAB = Pattern.compile("[a-z][a-z0-9]*(-[a-z0-9]+)*");
    static final Pattern GENERIC = Pattern.compile("\\$\\$[A-Z][a-zA-Z0-9]*");

    @Override
    public void run(final ValidationContext context, final DiagnosticCollector out) {
        final SpecModel model = context.model();
        for (final SchemaFile file : model.files()) {
            for (final String segment : file.namespace().split("\\.")) {
                if (!LOWER_CAMEL.matcher(segment).matches()) {
                    out.report(Rule.NAMING_NAMESPACE, "Namespace segment '" + segment + "' of '" + file.namespace()
                            + "' is not lowerCamelCase", file.location());
                }
            }
            for (final Declaration declaration : file.declarations()) {
                checkDeclaration(declaration, out);
            }
        }
    }

    private void checkDeclaration(final Declaration declaration, final DiagnosticCollector out) {
        switch (declaration) {
            case Declaration.TypeDeclaration type -> {
                check(PASCAL, Rule.NAMING_TYPE, "Type", type.name(), type.location(), out);
                type.typeParameters().forEach(p -> checkGeneric(p, out));
                type.fields().forEach(f -> checkField(f, out));
                type.methods().forEach(m -> checkMethod(m, out));
                if (type instanceof Declaration.EnumType enumType) {
                    for (final Parameter attribute : enumType.attributes()) {
                        check(LOWER_CAMEL, Rule.NAMING_MEMBER, "Enum attribute", attribute.name(),
                                attribute.location(), out);
                    }
                    for (final EnumValue value : enumType.values()) {
                        check(UPPER_SNAKE, Rule.NAMING_ENUM_VALUE, "Enum value", value.name(), value.location(), out);
                    }
                }
            }
            case Declaration.Constant constant ->
                    check(UPPER_SNAKE, Rule.NAMING_CONSTANT, "Constant", constant.name(), constant.location(), out);
            case Declaration.Function function -> checkMethod(function.method(), out);
        }
    }

    private void checkField(final Field field, final DiagnosticCollector out) {
        check(LOWER_CAMEL, Rule.NAMING_MEMBER, "Field", field.name(), field.location(), out);
        checkType(field.type(), out);
    }

    private void checkMethod(final Method method, final DiagnosticCollector out) {
        check(LOWER_CAMEL, Rule.NAMING_MEMBER, "Method", method.name(), method.location(), out);
        method.typeParameters().forEach(p -> checkGeneric(p, out));
        method.parameters().forEach(p -> checkParameter(p, out));
        checkType(method.returnType(), out);
        method.annotation("throws").ifPresent(a -> checkThrows(a, out));
    }

    private void checkParameter(final Parameter parameter, final DiagnosticCollector out) {
        check(LOWER_CAMEL, Rule.NAMING_MEMBER, "Parameter", parameter.name(), parameter.location(), out);
        checkType(parameter.type(), out);
    }

    private void checkType(final TypeRef type, final DiagnosticCollector out) {
        if (type instanceof TypeRef.Function function) {
            check(LOWER_CAMEL, Rule.NAMING_MEMBER, "Function type name", function.name(), function.location(), out);
            function.parameters().forEach(p -> checkParameter(p, out));
            checkType(function.returnType(), out);
        }
    }

    private void checkThrows(final Annotation annotation, final DiagnosticCollector out) {
        for (final Literal argument : annotation.arguments()) {
            if (argument instanceof Literal.NameLiteral name && !KEBAB.matcher(name.text()).matches()) {
                out.report(Rule.NAMING_ERROR_ID, "Error identifier '" + name.text()
                        + "' is not lowercase-kebab-case", argument.location());
            }
        }
    }

    private void checkGeneric(final TypeParameter parameter, final DiagnosticCollector out) {
        if (!GENERIC.matcher(parameter.name()).matches()) {
            out.report(Rule.NAMING_GENERIC, "Generic parameter '" + parameter.name()
                    + "' should be '$$' followed by a PascalCase name", parameter.location());
        }
    }

    private static void check(final Pattern pattern, final Rule rule, final String kind, final String name,
                              final SourceLocation location, final DiagnosticCollector out) {
        if (!pattern.matcher(name).matches()) {
            out.report(rule, kind + " name '" + name + "' violates the naming convention", location);
        }
    }
}
