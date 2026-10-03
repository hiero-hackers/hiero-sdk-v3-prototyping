package org.hiero.sdk.v3.metalang.generator.java;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.hiero.sdk.v3.metalang.model.MethodDefinition;
import org.hiero.sdk.v3.metalang.model.ParameterDefinition;
import org.hiero.sdk.v3.metalang.model.QualifiedName;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.model.TypeDefinition;
import org.hiero.sdk.v3.metalang.model.TypeParameterDefinition;
import org.hiero.sdk.v3.metalang.semantic.ReservedNames;

/**
 * Renders the members that all generated Java types share: methods, accessors, parameters and type parameters.
 */
final class JavaMembers {

    /** Where a method is rendered and therefore how its body looks. */
    enum Body {
        /** In a class, record or enum: a body that throws {@link UnsupportedOperationException}. */
        STUB,
        /** In an interface: abstract, {@code @@static} methods are stubs. */
        INTERFACE,
        /** In an abstract class: abstract, {@code @@finalMethod} and {@code @@static} methods are stubs. */
        ABSTRACT_CLASS
    }

    private JavaMembers() {
    }

    /**
     * Renders a method. In classes, records and enums the behaviour is not generated, so the body throws
     * {@link UnsupportedOperationException}; in interfaces non-static methods are abstract.
     *
     * @param owner    the type or factory class that contains the method (a type may inherit the method)
     * @param method   the method
     * @param context  the generation context
     * @param imports  the imports of the file
     * @param body     how the method is rendered
     * @return the Java code
     */
    static String method(final QualifiedName owner, final MethodDefinition method, final JavaContext context,
                         final Imports imports, final Body body) {
        final StringBuilder java = new StringBuilder();
        final List<String> errorIds = method.annotation("throws").stream()
                .flatMap(t -> t.arguments().stream())
                .map(a -> a.text())
                .distinct()
                .toList();
        final List<String> paragraphs = new ArrayList<>(List.of(method.documentation()));
        final List<String> tags = new ArrayList<>();
        final List<String> declared = new ArrayList<>();
        if (method.hasAnnotation("async")) {
            // nothing is thrown: the errors complete the returned stage exceptionally (text only, no import)
            final List<String> names = errorIds.stream().map(id -> context.exception(id).simpleName()).distinct()
                    .map(n -> "`" + n + "`").toList();
            if (!names.isEmpty()) {
                paragraphs.add("The returned stage completes exceptionally with " + enumeration(names)
                        + " if the operation fails.");
            }
        } else {
            for (final String errorId : errorIds) {
                final JavaExceptions.JavaException error = context.exception(errorId);
                final String name = imports.use(error.packageName(), error.simpleName());
                tags.add("@throws " + name + " if " + JavaExceptions.anError(errorId) + " occurs");
                if (error.checked() && !declared.contains(name)) {
                    declared.add(name);
                }
            }
        }
        java.append(MarkdownComment.render("    ", paragraphs, tags));
        if (method.hasAnnotation("deprecated")) {
            java.append("    @Deprecated\n");
        }
        if (overridesObjectMethod(method) || (!method.isStatic() && method.declaringType() != null
                && !method.declaringType().equals(owner)
                && context.inheritsFrom(owner, method.declaringType()))) {
            java.append("    @Override\n");
        }
        final boolean isFinal = method.hasAnnotation("finalMethod") && body == Body.ABSTRACT_CLASS;
        final boolean bodiless = body != Body.STUB && !method.isStatic() && !isFinal;
        java.append("    ").append(body == Body.INTERFACE ? "" : "public ");
        if (method.isStatic()) {
            java.append("static ");
        } else if (isFinal) {
            java.append("final ");
        } else if (bodiless && body == Body.ABSTRACT_CLASS) {
            java.append("abstract ");
        }
        if (!method.typeParameters().isEmpty()) {
            java.append('<').append(method.typeParameters().stream().map(p -> typeParameter(p, imports))
                    .collect(Collectors.joining(", "))).append("> ");
        }
        // an implementation keeps the Java types of the declaration it implements: Long for an inherited $$T
        final MethodDefinition origin = context.origin(method);
        final List<String> parameters = new ArrayList<>();
        for (int i = 0; i < method.parameters().size(); i++) {
            final ParameterDefinition parameter = method.parameters().get(i);
            if (overridesObjectMethod(method)) {
                // Object.equals accepts null; the parameter must not be narrowed to non-null
                parameters.add("final @" + imports.use(JavaTypes.JSPECIFY, "Nullable") + " Object "
                        + JavaKeywords.identifier(parameter.name()));
            } else {
                parameters.add(parameter(parameter, isTypeVariable(origin.parameters(), i), imports));
            }
        }
        java.append(returnType(method, origin.returnType() instanceof Type.TypeVariable, imports)).append(' ')
                .append(JavaKeywords.identifier(method.name())).append('(').append(String.join(", ", parameters))
                .append(')');
        if (!declared.isEmpty()) {
            java.append(" throws ").append(String.join(", ", declared));
        }
        if (bodiless) {
            java.append(";\n");
        } else {
            java.append(" {\n")
                    .append("        throw new UnsupportedOperationException(\"Not implemented yet: ")
                    .append(owner.name()).append('.').append(method.name()).append("\");\n")
                    .append("    }\n");
        }
        return java.toString();
    }

    private static boolean isTypeVariable(final List<ParameterDefinition> parameters, final int index) {
        return index < parameters.size() && parameters.get(index).type() instanceof Type.TypeVariable;
    }

    private static String returnType(final MethodDefinition method, final boolean boxed, final Imports imports) {
        if (method.hasAnnotation("streaming")) {
            throw new JavaTypes.UnsupportedTypeException("@@streaming " + method.returnType().text());
        }
        if (method.hasAnnotation("async")) {
            final String result = JavaTypes.type(method.returnType(), true, imports);
            // a @@nullable result is a nullable type argument: CompletionStage<@Nullable T>
            return imports.use("java.util.concurrent", "CompletionStage") + "<" + (method.hasAnnotation("nullable")
                    ? JavaTypes.annotate(result, imports.use(JavaTypes.JSPECIFY, "Nullable")) : result) + ">";
        }
        return JavaTypes.declaration(method.returnType(), method.hasAnnotation("nullable"), boxed, imports);
    }

    private static String parameter(final ParameterDefinition parameter, final boolean boxed, final Imports imports) {
        final String name = JavaKeywords.identifier(parameter.name());
        if (parameter.varargs()) {
            return "final " + JavaTypes.declaration(parameter.type(), false, imports) + "... " + name;
        }
        return "final " + JavaTypes.declaration(parameter.type(), parameter.hasAnnotation("nullable"), boxed, imports)
                + " " + name;
    }

    /**
     * Returns the type that setters return: the type parameter that stands for the implementing type
     * ({@code $$Self extends Transaction<..., $$Self>}) or the type itself.
     *
     * @param type    the type
     * @param imports the imports of the file
     * @return the Java type
     */
    static String selfType(final TypeDefinition type, final Imports imports) {
        return selfParameter(type).map(p -> imports.typeVariable(p.name()))
                .orElseGet(() -> JavaTypes.type(type.asType(), true, imports));
    }

    /**
     * Returns the type parameter that stands for the implementing type, if the type declares one.
     *
     * @param type the type
     * @return the self type parameter
     */
    static Optional<TypeParameterDefinition> selfParameter(final TypeDefinition type) {
        return type.typeParameters().stream()
                .filter(p -> p.bound() instanceof Type.DeclaredType bound && bound.name().equals(type.name())
                        && bound.arguments().contains(p.variable()))
                .findFirst();
    }

    /** {@code a}, {@code a or b}, {@code a, b or c}. */
    private static String enumeration(final List<String> items) {
        if (items.size() == 1) {
            return items.getFirst();
        }
        return String.join(", ", items.subList(0, items.size() - 1)) + " or " + items.getLast();
    }

    static String typeParameter(final TypeParameterDefinition parameter, final Imports imports) {
        final String name = imports.typeVariable(parameter.name());
        return parameter.bound() == null ? name : name + " extends " + JavaTypes.type(parameter.bound(), true, imports);
    }

    /**
     * Renders the type parameters of a type ({@code <K, V extends B>}).
     *
     * @param type    the type
     * @param imports the imports of the file
     * @return the type parameters, or an empty string
     */
    static String typeParameters(final TypeDefinition type, final Imports imports) {
        if (type.typeParameters().isEmpty()) {
            return "";
        }
        return "<" + type.typeParameters().stream().map(p -> typeParameter(p, imports))
                .collect(Collectors.joining(", ")) + ">";
    }

    /**
     * Returns the Java name of an accessor and rejects names that clash with methods of {@code Object} (and of
     * {@code Enum} for enums).
     *
     * @param attribute the attribute name
     * @param inEnum    whether the accessor belongs to an enum
     * @return the accessor name
     * @throws JavaTypes.UnsupportedTypeException if the name clashes
     */
    static String accessor(final String attribute, final boolean inEnum) {
        // the validator reports these names (naming.reserved); the check keeps the generated code compilable
        ReservedNames.attribute(attribute, inEnum).ifPresent(member -> {
            throw JavaTypes.UnsupportedTypeException.withMessage("Attribute '" + attribute + "' clashes with "
                    + member);
        });
        return JavaKeywords.identifier(attribute);
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
