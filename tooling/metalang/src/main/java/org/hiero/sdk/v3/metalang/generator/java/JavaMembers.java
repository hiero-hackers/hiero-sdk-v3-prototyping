package org.hiero.sdk.v3.metalang.generator.java;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.hiero.sdk.v3.metalang.model.MethodDefinition;
import org.hiero.sdk.v3.metalang.model.ParameterDefinition;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.model.TypeParameterDefinition;

/**
 * Renders the members that all generated Java types share: method stubs, parameters and type parameters.
 */
final class JavaMembers {

    private JavaMembers() {
    }

    /**
     * Renders a method whose behaviour is not generated: signature, Javadoc and a body that throws
     * {@link UnsupportedOperationException}.
     */
    static String method(final String owner, final MethodDefinition method, final Imports imports) {
        final StringBuilder java = new StringBuilder();
        final List<String> tags = method.annotation("throws").stream()
                .flatMap(t -> t.arguments().stream())
                .map(a -> JavaExceptions.uncheckedStandardException(a.text()))
                .flatMap(Optional::stream)
                .distinct()
                .map(e -> "@throws " + e)
                .toList();
        java.append(MarkdownComment.render("    ", List.of(method.documentation()), tags));
        if (method.hasAnnotation("deprecated")) {
            java.append("    @Deprecated\n");
        }
        if (overridesObjectMethod(method)) {
            java.append("    @Override\n");
        }
        java.append("    public ");
        if (method.isStatic()) {
            java.append("static ");
        }
        if (!method.typeParameters().isEmpty()) {
            java.append('<').append(method.typeParameters().stream().map(p -> typeParameter(p, imports))
                    .collect(Collectors.joining(", "))).append("> ");
        }
        java.append(returnType(method, imports)).append(' ').append(JavaKeywords.identifier(method.name())).append('(')
                .append(method.parameters().stream()
                        // Object.equals accepts null; the parameter must not be narrowed to @NonNull
                        .map(p -> overridesObjectMethod(method) ? "final @" + imports.use(JavaTypes.JSPECIFY,
                                "Nullable") + " Object " + JavaKeywords.identifier(p.name()) : parameter(p, imports))
                        .collect(Collectors.joining(", ")))
                .append(") {\n")
                .append("        throw new UnsupportedOperationException(\"Not implemented yet: ").append(owner)
                .append('.').append(method.name()).append("\");\n")
                .append("    }\n");
        return java.toString();
    }

    static String returnType(final MethodDefinition method, final Imports imports) {
        if (method.hasAnnotation("streaming")) {
            throw new JavaTypes.UnsupportedTypeException("@@streaming " + method.returnType().text());
        }
        if (method.hasAnnotation("async")) {
            final String result = JavaTypes.type(method.returnType(), true, imports);
            return JavaTypes.annotate(imports.use("java.util.concurrent", "CompletionStage") + "<" + result + ">",
                    imports.use(JavaTypes.JSPECIFY, "NonNull"));
        }
        return JavaTypes.declaration(method.returnType(), method.hasAnnotation("nullable"), imports);
    }

    static String parameter(final ParameterDefinition parameter, final Imports imports) {
        final String name = JavaKeywords.identifier(parameter.name());
        if (parameter.varargs()) {
            return "final " + JavaTypes.declaration(parameter.type(), false, imports) + "... " + name;
        }
        return "final " + declaration(parameter.type(), parameter, imports) + " " + name;
    }

    static String typeParameter(final TypeParameterDefinition parameter, final Imports imports) {
        final String name = parameter.name().substring(2);
        return parameter.bound() == null ? name : name + " extends " + JavaTypes.type(parameter.bound(), true, imports);
    }

    static String declaration(final Type type, final ParameterDefinition element, final Imports imports) {
        return JavaTypes.declaration(type, element.hasAnnotation("nullable"), imports);
    }

    /**
     * Renders the supertypes as a comment ({@code implements} is enabled as soon as abstractions are generated).
     *
     * @param supertypes the supertypes
     * @return the comment, starting with a space, or an empty string if there are no supertypes
     */
    static String implementsComment(final List<Type> supertypes) {
        if (supertypes.isEmpty()) {
            return "";
        }
        return " /* implements " + supertypes.stream()
                .map(t -> t instanceof Type.DeclaredType d
                        ? JavaNames.packageName(d.name().namespace()) + "." + d.name().name() : t.text())
                .collect(Collectors.joining(", ")) + " (enabled as soon as abstractions are generated) */";
    }

    /** Whether the method overrides {@code toString()}, {@code hashCode()} or {@code equals(Object)}. */
    private static boolean overridesObjectMethod(final MethodDefinition method) {
        if (method.isStatic()) {
            return false;
        }
        return switch (method.name()) {
            case "toString", "hashCode" -> method.parameters().isEmpty();
            case "equals" -> method.parameters().size() == 1
                    && method.parameters().getFirst().type() instanceof Type.AnyType;
            default -> false;
        };
    }
}
