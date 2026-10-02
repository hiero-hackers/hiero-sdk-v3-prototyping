package org.hiero.sdk.v3.metalang.validation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hiero.sdk.v3.metalang.ast.Annotated;
import org.hiero.sdk.v3.metalang.ast.Declaration;
import org.hiero.sdk.v3.metalang.ast.Field;
import org.hiero.sdk.v3.metalang.ast.Method;
import org.hiero.sdk.v3.metalang.ast.Parameter;
import org.hiero.sdk.v3.metalang.ast.SchemaFile;
import org.hiero.sdk.v3.metalang.ast.TypeRef;
import org.hiero.sdk.v3.metalang.diagnostic.DiagnosticCollector;
import org.hiero.sdk.v3.metalang.diagnostic.Rule;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;
import org.hiero.sdk.v3.metalang.semantic.ResolvedType;
import org.hiero.sdk.v3.metalang.semantic.SpecModel;

/**
 * Rules for fields, methods and parameters: duplicates, varargs, {@code @@async}/{@code @@streaming}
 * combinations, nullable collections and mutability.
 */
final class MemberCheck implements Check {

    @Override
    public void run(final SpecModel model, final DiagnosticCollector out) {
        for (final SchemaFile file : model.files()) {
            final Map<String, Method> functions = new HashMap<>();
            for (final Declaration declaration : file.declarations()) {
                switch (declaration) {
                    case Declaration.TypeDeclaration type -> checkType(model, file, type, out);
                    case Declaration.Function function -> {
                        final String key = (function.owner() == null ? "" : function.owner() + ".")
                                + function.method().signature();
                        final Method previous = functions.putIfAbsent(key, function.method());
                        if (previous != null) {
                            out.report(Rule.MEMBER_DUPLICATE_METHOD, "Function '" + key + "' is already declared at "
                                    + previous.location(), function.location());
                        }
                        checkMethod(model, file, function.method(), out);
                    }
                    case Declaration.Constant ignored -> {
                        // constants have no members
                    }
                }
            }
        }
    }

    private void checkType(final SpecModel model, final SchemaFile file, final Declaration.TypeDeclaration type,
                           final DiagnosticCollector out) {
        final Set<String> fieldNames = new HashSet<>();
        for (final Field field : type.fields()) {
            if (!fieldNames.add(field.name())) {
                out.report(Rule.MEMBER_DUPLICATE_FIELD, "Field '" + field.name() + "' is declared twice in '"
                        + type.name() + "'", field.location());
            }
            checkNullableCollection(model, file, field, field.type(), "Field '" + field.name() + "'",
                    field.location(), out);
            if (type instanceof Declaration.ComplexType && !field.hasAnnotation("immutable")) {
                out.report(Rule.FIELD_MUTABLE, "Field '" + type.name() + "." + field.name() + "' is mutable",
                        field.location());
            }
        }
        final Map<String, Method> signatures = new HashMap<>();
        for (final Method method : type.methods()) {
            final Method previous = signatures.putIfAbsent(method.signature(), method);
            if (previous != null) {
                out.report(Rule.MEMBER_DUPLICATE_METHOD, "Method '" + method.signature() + "' is already declared at "
                        + previous.location(), method.location());
            }
            checkMethod(model, file, method, out);
        }
    }

    private void checkMethod(final SpecModel model, final SchemaFile file, final Method method,
                             final DiagnosticCollector out) {
        if (method.hasAnnotation("async") && method.hasAnnotation("streaming")) {
            out.report(Rule.METHOD_ASYNC_AND_STREAMING, "'" + method.name()
                    + "' is both @@async and @@streaming", method.location());
        }
        if (method.hasAnnotation("streaming") && method.hasAnnotation("static")) {
            out.report(Rule.METHOD_STREAMING_STATIC, "'" + method.name()
                    + "' is both @@streaming and @@static", method.location());
        }
        checkNullableCollection(model, file, method, method.returnType(), "Return type of '" + method.name() + "'",
                method.location(), out);
        checkParameters(model, file, method.name(), method.parameters(), out);
    }

    private void checkParameters(final SpecModel model, final SchemaFile file, final String owner,
                                 final List<Parameter> parameters, final DiagnosticCollector out) {
        final Set<String> names = new HashSet<>();
        int varargsCount = 0;
        for (int i = 0; i < parameters.size(); i++) {
            final Parameter parameter = parameters.get(i);
            if (!names.add(parameter.name())) {
                out.report(Rule.MEMBER_DUPLICATE_PARAMETER, "Parameter '" + parameter.name() + "' of '" + owner
                        + "' is declared twice", parameter.location());
            }
            if (parameter.varargs()) {
                varargsCount++;
                if (varargsCount > 1) {
                    out.report(Rule.VARARGS_MULTIPLE, "'" + owner + "' declares more than one varargs parameter",
                            parameter.location());
                } else if (i != parameters.size() - 1) {
                    out.report(Rule.VARARGS_NOT_LAST, "Varargs parameter '" + parameter.name() + "' of '" + owner
                            + "' is not the last parameter", parameter.location());
                }
                if (parameter.hasAnnotation("nullable")) {
                    out.report(Rule.VARARGS_NULLABLE, "Varargs parameter '" + parameter.name()
                            + "' must not be @@nullable", parameter.location());
                }
            } else {
                checkNullableCollection(model, file, parameter, parameter.type(), "Parameter '" + parameter.name()
                        + "' of '" + owner + "'", parameter.location(), out);
            }
            if (parameter.type() instanceof TypeRef.Function function) {
                checkParameters(model, file, function.name(), function.parameters(), out);
            }
        }
    }

    private static void checkNullableCollection(final SpecModel model, final SchemaFile file, final Annotated element,
                                                final TypeRef type, final String description,
                                                final SourceLocation location, final DiagnosticCollector out) {
        if (element.hasAnnotation("nullable") && type instanceof TypeRef.Named named
                && model.resolve(file, named) instanceof ResolvedType.Builtin builtin
                && builtin.type().isCollection()) {
            out.report(Rule.COLLECTION_NULLABLE, description + " is a @@nullable '" + type.text()
                    + "'; use an empty collection instead", location);
        }
    }
}
