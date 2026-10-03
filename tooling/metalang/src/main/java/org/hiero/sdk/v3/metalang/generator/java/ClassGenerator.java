package org.hiero.sdk.v3.metalang.generator.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.model.FieldDefinition;
import org.hiero.sdk.v3.metalang.model.MethodDefinition;
import org.hiero.sdk.v3.metalang.model.QualifiedName;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.model.TypeDefinition;

/**
 * Generates a Java class: an {@code abstract class} for an abstraction with attributes, a class for a complex type
 * that cannot be a record (see "Immutable Objects", "Accessors and Setters" and "Complex Types" in
 * {@code guidelines/api-best-practices-java.md}).
 *
 * <ul>
 *   <li>The class stores the attributes its superclass does not store: {@code private final} for {@code @@immutable}
 *       attributes, otherwise {@code private} with a setter {@code setName(value)} that returns the object (the
 *       {@code $$Self} type parameter or the class itself). Accessors are named after the attribute.</li>
 *   <li>The constructor ({@code protected} for abstract classes) takes every attribute that has no other initial
 *       value: the immutable ones and the mutable non-nullable ones without {@code @@default}. It passes the
 *       attributes of the superclass to {@code super(...)}, checks the others ({@code Objects.requireNonNull}, the
 *       validation annotations) and copies collections and arrays. A second constructor without the immutable
 *       {@code @@default} attributes is generated if there are such attributes. Setters check like the
 *       constructor.</li>
 *   <li>Setters inherited from the superclass are overridden covariantly if their return type is not already the
 *       class itself, so that chained calls keep the concrete type.</li>
 *   <li>Abstract classes: declared methods are abstract, {@code @@finalMethod} methods are {@code final}. Concrete
 *       classes implement every inherited abstract method; the behaviour is not generated, so the body throws
 *       {@link UnsupportedOperationException}.</li>
 *   <li>Concrete classes get {@code toString} ({@code Name[a=..., b=...]}, arrays only with their length) and, if all
 *       attributes are immutable (a value type), {@code equals} and {@code hashCode} over all attributes.</li>
 *   <li>A class is {@code final} if it is {@code @@finalType} or if it extends a sealed type and has no subtypes;
 *       otherwise it is {@code non-sealed} if it extends a sealed type. {@code @@sealed(A, B)} becomes
 *       {@code sealed ... permits A, B}.</li>
 * </ul>
 */
final class ClassGenerator {

    private static final String INDENT = "    ";

    private ClassGenerator() {
    }

    static GeneratedFile generate(final String module, final TypeDefinition.ComplexTypeDefinition type,
                                  final JavaContext context) {
        final boolean isAbstract = type.abstraction();
        final String packageName = JavaNames.packageName(type.name().namespace());
        final String name = type.name().name();
        final Imports imports = context.imports(type);
        final List<FieldDefinition> fields = type.fields();
        final Optional<Type.DeclaredType> superclass = context.superclass(type);
        final Optional<TypeDefinition> superDefinition = superclass.map(s -> context.model().definition(s));
        final Set<String> inherited = superDefinition.map(d -> d.fields().stream().map(FieldDefinition::name)
                .collect(Collectors.toSet())).orElse(Set.of());
        final List<FieldDefinition> stored = fields.stream().filter(f -> !inherited.contains(f.name())).toList();
        final Map<String, String> declarations = new HashMap<>();
        for (final FieldDefinition field : fields) {
            JavaMembers.accessor(field.name(), false);
            declarations.put(field.name(), JavaTypes.declaration(field.type(), field.hasAnnotation("nullable"),
                    context.boxed(type.name(), field.name()), imports));
        }
        final String self = JavaMembers.selfType(type, imports);
        final boolean selfIsVariable = JavaMembers.selfParameter(type).isPresent();
        final Set<String> constants = new LinkedHashSet<>();
        final List<String> members = new ArrayList<>();

        // fields
        if (!stored.isEmpty()) {
            final StringBuilder java = new StringBuilder();
            for (final FieldDefinition field : stored) {
                java.append(INDENT).append("private ").append(field.hasAnnotation("immutable") ? "final " : "")
                        .append(declarations.get(field.name())).append(' ')
                        .append(JavaKeywords.identifier(field.name())).append(initializer(field, imports, context))
                        .append(";\n");
            }
            members.add(java.toString());
        }

        // constructors
        final List<FieldDefinition> parameters = constructorParameters(fields);
        final List<FieldDefinition> superParameters = superDefinition.map(d -> constructorParameters(d.fields()))
                .orElse(List.of());
        final Set<String> superParameterNames = superParameters.stream().map(FieldDefinition::name)
                .collect(Collectors.toSet());
        final StringBuilder constructorBody = new StringBuilder();
        if (superclass.isPresent()) {
            constructorBody.append(INDENT).append(INDENT).append("super(").append(superParameters.stream()
                    .map(f -> JavaKeywords.identifier(f.name())).collect(Collectors.joining(", "))).append(");\n");
        }
        for (final FieldDefinition field : parameters) {
            final String indent = INDENT + INDENT;
            if (!inherited.contains(field.name())) {
                constructorBody.append(checks(field, declarations.get(field.name()), imports, constants, indent))
                        .append(assignment(field, imports, indent));
            } else if (!superParameterNames.contains(field.name())) {
                // narrowed in this class (e.g. no longer nullable): set through the setter of the superclass
                constructorBody.append(checks(field, declarations.get(field.name()), imports, constants, indent))
                        .append(indent).append(InterfaceGenerator.setter(field.name())).append('(')
                        .append(JavaKeywords.identifier(field.name())).append(");\n");
            } else if (type.name().equals(field.declaringType()) && !field.hasAnnotation("nullable")) {
                // nullability narrowed with @@override: the superclass accepts null, this class does not
                constructorBody.append(checks(field, declarations.get(field.name()), imports, constants, indent));
            }
        }
        // always explicit: an implicit default constructor of an exported class is a javac lint warning
        final List<String> tags = new ArrayList<>();
        if (constructorBody.indexOf("requireNonNull") >= 0) {
            tags.add("@throws NullPointerException if a required value is `null`");
        }
        if (constructorBody.indexOf("IllegalArgumentException") >= 0) {
            tags.add("@throws IllegalArgumentException if a value violates its constraints");
        }
        members.add(MarkdownComment.render(INDENT, List.of("Creates a new `" + name + "`."), tags)
                + INDENT + (isAbstract ? "protected " : "public ") + name
                + parameterList(parameters.stream().map(f -> "final " + declarations.get(f.name()) + " "
                + JavaKeywords.identifier(f.name())).toList()) + " {\n" + constructorBody + INDENT + "}\n");
        final List<FieldDefinition> required = parameters.stream()
                .filter(f -> !f.hasAnnotation("default")).toList();
        if (required.size() < parameters.size()) {
            members.add(MarkdownComment.render(INDENT, List.of("Creates a new `" + name + "` with the default value"
                    + " of " + parameters.stream().filter(f -> f.hasAnnotation("default"))
                    .map(f -> "`" + f.name() + "`").collect(Collectors.joining(", ")) + "."))
                    + INDENT + (isAbstract ? "protected " : "public ") + name
                    + parameterList(required.stream().map(f -> "final " + declarations.get(f.name()) + " "
                    + JavaKeywords.identifier(f.name())).toList()) + " {\n"
                    + INDENT + INDENT + "this(" + parameters.stream().map(f -> f.hasAnnotation("default")
                    ? JavaLiterals.expression(f.annotation("default").orElseThrow().arguments().getFirst(), f.type(),
                    imports, context) : JavaKeywords.identifier(f.name())).collect(Collectors.joining(", ")) + ");\n"
                    + INDENT + "}\n");
        }

        // accessors and setters of the stored attributes
        for (final FieldDefinition field : stored) {
            members.add(accessor(type, field, declarations.get(field.name()), context));
            if (!field.hasAnnotation("immutable")) {
                members.add(setter(type, field, declarations.get(field.name()), self, selfIsVariable, context,
                        imports, constants));
            }
        }
        // accessors narrowed with @@override, setters inherited from the superclass
        for (final FieldDefinition field : fields) {
            if (!inherited.contains(field.name())) {
                continue;
            }
            if (type.name().equals(field.declaringType())) {
                members.add(INDENT + "@Override\n" + INDENT + "public " + declarations.get(field.name()) + ' '
                        + JavaKeywords.identifier(field.name()) + "() {\n" + INDENT + INDENT + "return super."
                        + JavaKeywords.identifier(field.name()) + "();\n" + INDENT + "}\n");
            }
            if (!field.hasAnnotation("immutable")
                    && !inheritedSetterType(superclass.orElseThrow(), superDefinition.orElseThrow(), imports)
                    .equals(self)) {
                members.add(MarkdownComment.render(INDENT, List.of("Sets the `" + field.name() + "`."),
                        List.of("@param " + JavaKeywords.identifier(field.name()) + " the new value",
                                "@return this object"))
                        + (selfIsVariable ? INDENT + "@SuppressWarnings(\"unchecked\")\n" : "")
                        + INDENT + "@Override\n"
                        + (field.hasAnnotation("deprecated") ? INDENT + "@Deprecated\n" : "")
                        + INDENT + "public " + self + ' ' + InterfaceGenerator.setter(field.name()) + "(final "
                        + declarations.get(field.name()) + ' ' + JavaKeywords.identifier(field.name()) + ") {\n"
                        + INDENT + INDENT + "super." + InterfaceGenerator.setter(field.name()) + '('
                        + JavaKeywords.identifier(field.name()) + ");\n"
                        + INDENT + INDENT + "return " + (selfIsVariable ? "(" + self + ") " : "") + "this;\n"
                        + INDENT + "}\n");
            }
        }

        // methods
        for (final MethodDefinition method : type.methods()) {
            final boolean own = type.name().equals(method.declaringType());
            if (isAbstract) {
                if (own) {
                    members.add(JavaMembers.method(type.name(), method, context, imports, JavaMembers.Body.ABSTRACT_CLASS));
                }
            } else if (own || !method.hasAnnotation("finalMethod")
                    && !implementedBySuperclass(method, superDefinition)) {
                members.add(JavaMembers.method(type.name(), method, context, imports, JavaMembers.Body.STUB));
            }
        }

        // value semantics and string representation of concrete classes
        if (!isAbstract && !fields.isEmpty()) {
            final boolean valueType = fields.stream().allMatch(f -> f.hasAnnotation("immutable"));
            if (valueType && type.methods("equals").isEmpty() && type.methods("hashCode").isEmpty()) {
                members.add(equalsMethod(type, declarations, imports));
                members.add(hashCodeMethod(fields, imports));
            }
            if (type.methods("toString").isEmpty()) {
                members.add(toStringMethod(name, fields));
            }
        }

        // the header registers imports, so it is rendered before the import block
        final String header = header(type, superclass, context, imports);
        final StringBuilder java = new StringBuilder(JavaGenerator.HEADER).append('\n');
        java.append("package ").append(packageName).append(";\n\n");
        final String importBlock = imports.render();
        if (!importBlock.isEmpty()) {
            java.append(importBlock).append('\n');
        }
        java.append(MarkdownComment.render("", List.of(type.documentation())));
        if (type.hasAnnotation("deprecated")) {
            java.append("@Deprecated\n");
        }
        java.append(header).append(" {\n");
        if (!constants.isEmpty()) {
            java.append('\n');
            constants.forEach(c -> java.append(INDENT).append(c).append('\n'));
        }
        for (final String member : members) {
            java.append('\n').append(member);
        }
        java.append("}\n");
        return new GeneratedFile(JavaNames.packageDirectory(module, type.name().namespace()) + "/" + name + ".java",
                java.toString());
    }

    /**
     * The attributes the constructor takes: all that have no other initial value (immutable ones, and mutable
     * non-nullable ones without {@code @@default}), in effective order.
     */
    static List<FieldDefinition> constructorParameters(final List<FieldDefinition> fields) {
        return fields.stream().filter(f -> f.hasAnnotation("immutable")
                || !f.hasAnnotation("nullable") && !f.hasAnnotation("default")).toList();
    }

    private static String initializer(final FieldDefinition field, final Imports imports,
                                      final JavaContext context) {
        if (field.hasAnnotation("immutable") || !field.hasAnnotation("default")) {
            return "";
        }
        return " = " + JavaLiterals.expression(field.annotation("default").orElseThrow().arguments().getFirst(),
                field.type(), imports, context);
    }

    private static String header(final TypeDefinition.ComplexTypeDefinition type,
                                 final Optional<Type.DeclaredType> superclass, final JavaContext context,
                                 final Imports imports) {
        final List<QualifiedName> permitted = InterfaceGenerator.permittedSubtypes(type);
        final boolean parentSealed = type.supertypes().stream().anyMatch(s -> s instanceof Type.DeclaredType d
                && context.isGenerated(d.name()) && context.model().type(d.name())
                .map(t -> !InterfaceGenerator.permittedSubtypes(t).isEmpty()).orElse(false));
        final boolean hasSubtypes = context.model().types().stream().anyMatch(t -> t.supertypes().stream()
                .anyMatch(s -> s instanceof Type.DeclaredType d && d.name().equals(type.name())));
        final StringBuilder java = new StringBuilder("public ");
        if (type.abstraction()) {
            java.append("abstract ");
        }
        if (!permitted.isEmpty()) {
            java.append("sealed ");
        } else if (!type.abstraction() && (type.hasAnnotation("finalType") || parentSealed && !hasSubtypes)) {
            java.append("final ");
        } else if (parentSealed) {
            java.append("non-sealed ");
        }
        java.append("class ").append(type.name().name()).append(JavaMembers.typeParameters(type, imports));
        superclass.ifPresent(s -> java.append(" extends ").append(JavaTypes.type(s, true, imports)));
        java.append(context.supertypes(type, "implements", imports));
        if (!permitted.isEmpty()) {
            java.append(" permits ").append(permitted.stream()
                    .map(p -> imports.use(JavaNames.packageName(p.namespace()), p.name()))
                    .collect(Collectors.joining(", ")));
        }
        return java.toString();
    }

    /** Null check and validation of a value before it is stored. */
    private static String checks(final FieldDefinition field, final String declaration, final Imports imports,
                                 final Set<String> constants, final String indent) {
        final StringBuilder java = new StringBuilder();
        final String name = JavaKeywords.identifier(field.name());
        if (!field.hasAnnotation("nullable") && !JavaTypes.isPrimitive(declaration)) {
            java.append(indent).append(imports.use("java.util", "Objects")).append(".requireNonNull(").append(name)
                    .append(", \"").append(field.name()).append(" must not be null\");\n");
        }
        final JavaConstraints.Checks constraints = JavaConstraints.of(field, indent, imports);
        constants.addAll(constraints.constants());
        return java.append(constraints.statements()).toString();
    }

    /** Stores a value, as unmodifiable copy for collections and as copy for arrays. */
    private static String assignment(final FieldDefinition field, final Imports imports, final String indent) {
        final String name = JavaKeywords.identifier(field.name());
        final String copy = RecordGenerator.copy(field.type(), name, imports);
        final String value = copy == null ? name
                : field.hasAnnotation("nullable") ? name + " == null ? null : " + copy : copy;
        return indent + "this." + name + " = " + value + ";\n";
    }

    private static String accessor(final TypeDefinition type, final FieldDefinition field, final String declaration,
                                   final JavaContext context) {
        final String name = JavaKeywords.identifier(field.name());
        final boolean bytes = JavaConstraints.isBytes(field.type());
        final StringBuilder java = new StringBuilder(MarkdownComment.render(INDENT, List.of(
                field.documentation().isBlank() ? "Returns the `" + field.name() + "`." : field.documentation())));
        if (context.overridesAccessor(type.name(), field.name())) {
            java.append(INDENT).append("@Override\n");
        }
        if (field.hasAnnotation("deprecated")) {
            java.append(INDENT).append("@Deprecated\n");
        }
        return java.append(INDENT).append("public ").append(declaration).append(' ').append(name).append("() {\n")
                .append(INDENT).append(INDENT).append("return ")
                .append(bytes && field.hasAnnotation("nullable") ? name + " == null ? null : " : "")
                .append(name).append(bytes ? ".clone()" : "").append(";\n")
                .append(INDENT).append("}\n").toString();
    }

    private static String setter(final TypeDefinition type, final FieldDefinition field, final String declaration,
                                 final String self, final boolean selfIsVariable, final JavaContext context,
                                 final Imports imports, final Set<String> constants) {
        final String name = JavaKeywords.identifier(field.name());
        final String checks = checks(field, declaration, imports, constants, INDENT + INDENT);
        final List<String> tags = new ArrayList<>(List.of("@param " + name + " the new value", "@return this object"));
        if (checks.contains("requireNonNull")) {
            tags.add("@throws NullPointerException if the value is `null`");
        }
        if (checks.contains("IllegalArgumentException")) {
            tags.add("@throws IllegalArgumentException if the value violates its constraints");
        }
        final StringBuilder java = new StringBuilder(MarkdownComment.render(INDENT,
                List.of("Sets the `" + field.name() + "`."), tags));
        if (selfIsVariable) {
            java.append(INDENT).append("@SuppressWarnings(\"unchecked\")\n");
        }
        if (overridesSetter(type, field.name(), context)) {
            java.append(INDENT).append("@Override\n");
        }
        if (field.hasAnnotation("deprecated")) {
            java.append(INDENT).append("@Deprecated\n");
        }
        return java.append(INDENT).append("public ").append(self).append(' ')
                .append(InterfaceGenerator.setter(field.name())).append("(final ").append(declaration).append(' ')
                .append(name).append(") {\n").append(checks).append(assignment(field, imports, INDENT + INDENT))
                .append(INDENT).append(INDENT).append("return ").append(selfIsVariable ? "(" + self + ") " : "")
                .append("this;\n").append(INDENT).append("}\n").toString();
    }

    /** Whether a generated supertype (an interface) declares a setter for the attribute. */
    private static boolean overridesSetter(final TypeDefinition type, final String name, final JavaContext context) {
        return type.supertypes().stream()
                .filter(Type.DeclaredType.class::isInstance).map(Type.DeclaredType.class::cast)
                .filter(s -> context.isGenerated(s.name()))
                .map(s -> context.model().definition(s))
                .anyMatch(s -> s.field(name).filter(f -> !f.hasAnnotation("immutable")).isPresent());
    }

    /** The Java type that the setters of the superclass return, seen from the subclass. */
    private static String inheritedSetterType(final Type.DeclaredType superclass, final TypeDefinition superDefinition,
                                              final Imports imports) {
        return JavaMembers.selfParameter(superDefinition)
                .map(p -> superclass.arguments().get(superDefinition.typeParameters().indexOf(p)))
                .map(t -> JavaTypes.type(t, true, imports))
                .orElseGet(() -> JavaTypes.type(superclass, true, imports));
    }

    /** Whether a concrete superclass already implements the method. */
    private static boolean implementedBySuperclass(final MethodDefinition method,
                                                   final Optional<TypeDefinition> superDefinition) {
        return superDefinition.filter(d -> !(d instanceof TypeDefinition.ComplexTypeDefinition c && c.abstraction()))
                .map(d -> d.methods().stream().anyMatch(m -> m.location().equals(method.location())))
                .orElse(false);
    }

    private static String parameterList(final List<String> parameters) {
        final String line = "(" + String.join(", ", parameters) + ")";
        if (line.length() <= 80) {
            return line;
        }
        return "(\n" + parameters.stream().map(p -> INDENT + INDENT + INDENT + p)
                .collect(Collectors.joining(",\n")) + ")";
    }

    private static String equalsMethod(final TypeDefinition type, final Map<String, String> declarations,
                                       final Imports imports) {
        final String self = type.name().name() + (type.typeParameters().isEmpty() ? "" : "<?>");
        final String conditions = type.fields().stream().map(f -> {
            final String accessor = JavaKeywords.identifier(f.name()) + "()";
            if (JavaConstraints.isBytes(f.type())) {
                return imports.use("java.util", "Arrays") + ".equals(" + accessor + ", other." + accessor + ")";
            }
            return switch (declarations.get(f.name())) {
                case "double" -> "Double.compare(" + accessor + ", other." + accessor + ") == 0";
                case "byte", "short", "int", "long", "boolean" -> accessor + " == other." + accessor;
                default -> imports.use("java.util", "Objects") + ".equals(" + accessor + ", other." + accessor + ")";
            };
        }).map(c -> "\n" + INDENT + INDENT + INDENT + "&& " + c).collect(Collectors.joining());
        return INDENT + "@Override\n"
                + INDENT + "public boolean equals(final @" + imports.use(JavaTypes.JSPECIFY, "Nullable")
                + " Object obj) {\n"
                + INDENT + INDENT + "if (this == obj) {\n"
                + INDENT + INDENT + INDENT + "return true;\n"
                + INDENT + INDENT + "}\n"
                + INDENT + INDENT + "return obj instanceof " + self + " other" + conditions + ";\n"
                + INDENT + "}\n";
    }

    private static String hashCodeMethod(final List<FieldDefinition> fields, final Imports imports) {
        return INDENT + "@Override\n"
                + INDENT + "public int hashCode() {\n"
                + INDENT + INDENT + "return " + imports.use("java.util", "Objects") + ".hash(" + fields.stream()
                .map(f -> JavaConstraints.isBytes(f.type())
                        ? imports.use("java.util", "Arrays") + ".hashCode(" + JavaKeywords.identifier(f.name()) + "())"
                        : JavaKeywords.identifier(f.name()) + "()")
                .collect(Collectors.joining(", ")) + ");\n"
                + INDENT + "}\n";
    }

    private static String toStringMethod(final String name, final List<FieldDefinition> fields) {
        // one attribute per line like in the guideline; arrays only with their length (sensitive or large content)
        final List<String> parts = new ArrayList<>();
        for (int i = 0; i < fields.size(); i++) {
            final FieldDefinition field = fields.get(i);
            final String accessor = JavaKeywords.identifier(field.name()) + "()";
            String value = accessor;
            if (JavaConstraints.isBytes(field.type())) {
                final String length = "\"byte[\" + " + accessor + ".length + \"]\"";
                value = field.hasAnnotation("nullable") ? "(" + accessor + " == null ? \"null\" : " + length + ")"
                        : length;
            }
            parts.add("\"" + (i == 0 ? name + "[" : ", ") + field.name() + "=\" + " + value);
        }
        return INDENT + "@Override\n"
                + INDENT + "public String toString() {\n"
                + INDENT + INDENT + "return " + String.join("\n" + INDENT + INDENT + INDENT + INDENT + "+ ", parts)
                + "\n" + INDENT + INDENT + INDENT + INDENT + "+ \"]\";\n"
                + INDENT + "}\n";
    }
}
