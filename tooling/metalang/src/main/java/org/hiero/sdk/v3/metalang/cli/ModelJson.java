package org.hiero.sdk.v3.metalang.cli;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.hiero.sdk.v3.metalang.ValidationReport;
import org.hiero.sdk.v3.metalang.ast.Annotated;
import org.hiero.sdk.v3.metalang.ast.Annotation;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.diagnostic.Severity;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;
import org.hiero.sdk.v3.metalang.model.ConstantDefinition;
import org.hiero.sdk.v3.metalang.model.EnumValueDefinition;
import org.hiero.sdk.v3.metalang.model.FieldDefinition;
import org.hiero.sdk.v3.metalang.model.FunctionDefinition;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.hiero.sdk.v3.metalang.model.MethodDefinition;
import org.hiero.sdk.v3.metalang.model.ParameterDefinition;
import org.hiero.sdk.v3.metalang.model.TypeDefinition;
import org.hiero.sdk.v3.metalang.model.TypeParameterDefinition;

/**
 * Renders a {@link LinkedModel} as JSON. Types are written with qualified names; the output is deterministic so it
 * can be diffed and used as a golden file.
 */
final class ModelJson {

    private ModelJson() {
    }

    /**
     * Renders the model.
     *
     * @param report        the validation report (for counts)
     * @param model         the linked model
     * @param namespaceFilter only namespaces for which this predicate is true are written
     * @param typeFilter    only types for which this predicate is true are written
     * @param typesOnly     whether functions and constants are omitted (used when filtering for a single type)
     * @return the JSON text
     */
    static String render(final ValidationReport report, final LinkedModel model,
                         final Predicate<String> namespaceFilter, final Predicate<TypeDefinition> typeFilter,
                         final boolean typesOnly) {
        final Map<String, Object> root = new LinkedHashMap<>();
        root.put("specs", report.specCount());
        root.put("errors", report.count(Severity.ERROR));
        root.put("warnings", report.count(Severity.WARNING));
        root.put("types", model.types().stream()
                .filter(t -> namespaceFilter.test(t.name().namespace()) && typeFilter.test(t))
                .map(ModelJson::type)
                .toList());
        root.put("functions", typesOnly ? List.of() : model.functions().stream()
                .filter(f -> namespaceFilter.test(f.namespace()))
                .map(ModelJson::function)
                .toList());
        root.put("constants", typesOnly ? List.of() : model.constants().stream()
                .filter(c -> namespaceFilter.test(c.name().namespace()))
                .map(ModelJson::constant)
                .toList());
        return Json.write(root);
    }

    private static Map<String, Object> type(final TypeDefinition type) {
        final Map<String, Object> json = new LinkedHashMap<>();
        json.put("name", type.name().toString());
        json.put("kind", switch (type) {
            case TypeDefinition.ComplexTypeDefinition complex -> complex.abstraction() ? "abstraction" : "type";
            case TypeDefinition.EnumDefinition ignored -> "enum";
        });
        json.put("location", location(type.location()));
        json.put("documentation", type.documentation());
        json.put("annotations", annotations(type));
        json.put("typeParameters", type.typeParameters().stream().map(ModelJson::typeParameter).toList());
        json.put("supertypes", type.supertypes().stream().map(t -> (Object) t.text()).toList());
        if (type instanceof TypeDefinition.EnumDefinition enumType) {
            json.put("attributes", enumType.attributes().stream().map(ModelJson::parameter).toList());
            json.put("values", enumType.values().stream().map(ModelJson::enumValue).toList());
        } else {
            json.put("fields", type.fields().stream().map(ModelJson::field).toList());
        }
        json.put("methods", type.methods().stream().map(ModelJson::method).toList());
        return json;
    }

    private static Map<String, Object> field(final FieldDefinition field) {
        final Map<String, Object> json = new LinkedHashMap<>();
        json.put("name", field.name());
        json.put("type", field.type().text());
        json.put("declaredIn", field.declaringType().toString());
        json.put("annotations", annotations(field));
        json.put("documentation", field.documentation());
        json.put("location", location(field.location()));
        return json;
    }

    private static Map<String, Object> method(final MethodDefinition method) {
        final Map<String, Object> json = new LinkedHashMap<>();
        json.put("name", method.name());
        json.put("signature", method.signature());
        json.put("returnType", method.returnType().text());
        json.put("typeParameters", method.typeParameters().stream().map(ModelJson::typeParameter).toList());
        json.put("parameters", method.parameters().stream().map(ModelJson::parameter).toList());
        if (method.declaringType() != null) {
            json.put("declaredIn", method.declaringType().toString());
        }
        json.put("annotations", annotations(method));
        json.put("documentation", method.documentation());
        json.put("location", location(method.location()));
        return json;
    }

    private static Map<String, Object> parameter(final ParameterDefinition parameter) {
        final Map<String, Object> json = new LinkedHashMap<>();
        json.put("name", parameter.name());
        json.put("type", parameter.type().text());
        json.put("varargs", parameter.varargs());
        json.put("annotations", annotations(parameter));
        return json;
    }

    private static Map<String, Object> typeParameter(final TypeParameterDefinition parameter) {
        final Map<String, Object> json = new LinkedHashMap<>();
        json.put("name", parameter.name());
        json.put("bound", parameter.bound() == null ? null : parameter.bound().text());
        return json;
    }

    private static Map<String, Object> enumValue(final EnumValueDefinition value) {
        final Map<String, Object> json = new LinkedHashMap<>();
        json.put("name", value.name());
        json.put("arguments", value.arguments().stream().map(l -> (Object) l.text()).toList());
        json.put("annotations", annotations(value));
        json.put("documentation", value.documentation());
        json.put("location", location(value.location()));
        return json;
    }

    private static Map<String, Object> function(final FunctionDefinition function) {
        final Map<String, Object> json = new LinkedHashMap<>();
        json.put("namespace", function.namespace());
        json.putAll(method(function.method()));
        return json;
    }

    private static Map<String, Object> constant(final ConstantDefinition constant) {
        final Map<String, Object> json = new LinkedHashMap<>();
        json.put("name", constant.name().toString());
        json.put("type", constant.type().text());
        json.put("value", constant.value().text());
        json.put("annotations", annotations(constant));
        json.put("documentation", constant.documentation());
        json.put("location", location(constant.location()));
        return json;
    }

    private static List<Object> annotations(final Annotated element) {
        final List<Object> result = new ArrayList<>();
        for (final Annotation annotation : element.annotations()) {
            result.add("@@" + annotation.name() + (annotation.arguments().isEmpty() ? ""
                    : "(" + String.join(", ", annotation.arguments().stream().map(Literal::text).toList()) + ")"));
        }
        return result;
    }

    private static String location(final SourceLocation location) {
        return location.toString();
    }
}
