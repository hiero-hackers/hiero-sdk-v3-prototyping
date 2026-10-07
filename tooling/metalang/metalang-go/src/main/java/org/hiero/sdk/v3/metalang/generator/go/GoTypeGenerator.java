package org.hiero.sdk.v3.metalang.generator.go;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.model.EnumValueDefinition;
import org.hiero.sdk.v3.metalang.model.FieldDefinition;
import org.hiero.sdk.v3.metalang.model.ParameterDefinition;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.model.TypeDefinition;
import org.hiero.sdk.v3.metalang.model.TypeParameterDefinition;

/**
 * Generates the Go declaration of one spec type: an interface for an abstraction, a struct with a constructor and
 * getters for a concrete type, and one of the two enum shapes for an enumeration.
 *
 * <p><strong>Inheritance.</strong> The guideline's first sketch gave an abstraction with attributes an unexported
 * base struct that every subtype embeds. That does not work across packages - an unexported type cannot be
 * embedded from another package, and the specs inherit across namespaces throughout. A concrete struct therefore
 * carries <em>all</em> its effective attributes as its own unexported fields; the abstraction contributes the
 * interface, and Go's structural typing makes the struct satisfy it without a declaration.
 */
final class GoTypeGenerator {

    private final GoContext context;



    /**
     * Creates the generator.
     *
     * @param context the generator context
     */
    GoTypeGenerator(final GoContext context) {
        this.context = Objects.requireNonNull(context, "context must not be null");
    }

    /**
     * Generates the body of the file declaring a type; the caller adds the package clause and the imports.
     *
     * @param definition the declaration
     * @param imports    the imports of the file, extended by what the declaration needs
     * @return the Go code
     * @throws GoGap if the declaration has no Go form yet
     */
    String generate(final TypeDefinition definition, final GoImports imports) {
        return switch (definition) {
            case TypeDefinition.EnumDefinition enumeration -> enumeration(enumeration, imports);
            case TypeDefinition.ComplexTypeDefinition complex -> complex.abstraction()
                    ? abstraction(complex, imports) : struct(complex, imports);
        };
    }

    // --- abstractions -------------------------------------------------------------------------------------

    /** An abstraction is an interface: its attributes become getter methods, its supertypes are embedded. */
    private String abstraction(final TypeDefinition.ComplexTypeDefinition definition, final GoImports imports) {
        final String name = GoNames.exported(definition.name().name());
        final StringBuilder go = new StringBuilder(GoDoc.of(name, definition.documentation(), definition));
        go.append(widenedNote(definition));
        go.append("type ").append(name).append(typeParameters(GoContext.goParameters(definition), imports))
                .append(" interface {\n");
        final List<String> members = new ArrayList<>();
        for (final Type.DeclaredType supertype : context.abstractionSupertypes(definition)) {
            members.add("\t" + context.type(supertype).render(imports) + "\n");
        }
        for (final FieldDefinition field : definition.declaredFields()) {
            final String getter = GoNames.exported(field.name());
            members.add(GoDoc.comment(getterDoc(field, getter)).indent(0).replace("//", "\t//")
                    + "\t" + getter + "() " + fieldType(field, imports).render(imports) + "\n");
        }
        if (definition.hasAnnotation("sealed")) {
            // only the declaring package can implement an unexported method, which is Go's sealed hierarchy
            members.add("\t// " + marker(name) + " keeps the implementations of " + name
                    + " inside this package.\n\t" + marker(name) + "()\n");
        }
        go.append(String.join("\n", members)).append("}\n");
        return go.toString();
    }

    /** The name of the marker method of a sealed abstraction. */
    private static String marker(final String name) {
        return "is" + name;
    }

    // --- structs ------------------------------------------------------------------------------------------

    /** A concrete type is a struct with unexported fields, a constructor and value-receiver getters. */
    private String struct(final TypeDefinition.ComplexTypeDefinition definition, final GoImports imports) {
        final String name = GoNames.exported(definition.name().name());
        final List<FieldDefinition> fields = definition.fields();
        final List<TypeParameterDefinition> goParameters = GoContext.goParameters(definition);
        final String parameters = typeParameters(goParameters, imports);
        final String self = name + typeArguments(goParameters);
        final String receiver = receiver(name);

        final StringBuilder go = new StringBuilder(GoDoc.of(name, definition.documentation(), definition));
        go.append(widenedNote(definition));
        go.append("type ").append(name).append(parameters).append(" struct {\n");
        go.append(GoFormat.block(fields.stream()
                .map(field -> "\t" + GoNames.unexported(field.name()) + " "
                        + fieldType(field, imports).render(imports))
                .toList()));
        go.append("}\n");

        go.append('\n').append(constructor(definition, name, self, parameters, fields, imports));
        for (final FieldDefinition field : fields) {
            go.append('\n').append(getter(field, receiver, self, parameters, imports));
        }
        for (final Type.DeclaredType supertype : context.abstractionSupertypes(definition)) {
            final TypeDefinition declaration = context.definition(supertype.name()).orElseThrow();
            if (declaration.hasAnnotation("sealed")) {
                final String sealed = GoNames.exported(supertype.name().name());
                go.append("\nfunc (").append(receiver).append(' ').append(self).append(") ")
                        .append(marker(sealed)).append("() {}\n");
            }
        }
        return go.toString();
    }

    /** {@code func NewAccountID(shard uint64, ...) AccountID}. */
    private String constructor(final TypeDefinition definition, final String name, final String self,
                               final String parameters, final List<FieldDefinition> fields,
                               final GoImports imports) {
        final String constructorName = "New" + name;
        final List<String> arguments = new ArrayList<>();
        final Set<String> names = new LinkedHashSet<>();
        for (final FieldDefinition field : fields) {
            final String parameter = unique(GoNames.unexported(field.name()), names);
            arguments.add(parameter + " " + fieldType(field, imports).render(imports));
        }
        final StringBuilder go = new StringBuilder(GoDoc.comment(constructorName + " creates a " + name + "."));
        go.append("func ").append(constructorName).append(parameters)
                .append('(').append(String.join(", ", arguments)).append(") ").append(self).append(" {\n");
        if (fields.isEmpty()) {
            return go.append("\treturn ").append(self).append("{}\n}\n").toString();
        }
        go.append("\treturn ").append(self).append("{\n");
        final List<String> assignments = new ArrayList<>(names);
        final List<String> entries = new ArrayList<>();
        int index = 0;
        for (final FieldDefinition field : fields) {
            entries.add("\t\t" + GoNames.unexported(field.name()) + ": "
                    + copy(field, assignments.get(index++), imports) + ",");
        }
        go.append(GoFormat.block(entries));
        go.append("\t}\n}\n");
        return go.toString();
    }

    /** {@code func (a AccountID) Shard() uint64}. */
    private String getter(final FieldDefinition field, final String receiver, final String self,
                          final String parameters, final GoImports imports) {
        final String name = GoNames.exported(field.name());
        final GoType type = fieldType(field, imports);
        final StringBuilder go = new StringBuilder(GoDoc.comment(getterDoc(field, name)));
        go.append("func (").append(receiver).append(' ').append(self).append(") ").append(name)
                .append("() ").append(type.render(imports)).append(" {\n");
        go.append("\treturn ").append(copy(field, receiver + "." + GoNames.unexported(field.name()), imports))
                .append("\n}\n");
        return go.toString();
    }

    private static String getterDoc(final FieldDefinition field, final String name) {
        final String text = field.documentation() == null ? "" : field.documentation().strip();
        if (text.isEmpty()) {
            return name + " returns the " + spaced(field.name()) + ".";
        }
        return text.startsWith(name) ? text : name + " returns " + decapitalize(text);
    }

    private static String decapitalize(final String text) {
        return Character.toLowerCase(text.charAt(0)) + text.substring(1);
    }

    /** {@code evmAddress} becomes {@code evm address}, so a generated sentence reads as prose. */
    private static String spaced(final String name) {
        return String.join(" ", GoNames.words(name)).toLowerCase(Locale.ROOT);
    }

    // --- enumerations -------------------------------------------------------------------------------------

    private String enumeration(final TypeDefinition.EnumDefinition definition, final GoImports imports) {
        return definition.attributes().isEmpty() ? plainEnum(definition, imports)
                : valueEnum(definition, imports);
    }

    /** Without attributes: a defined integer type with typed constants. */
    private String plainEnum(final TypeDefinition.EnumDefinition definition, final GoImports imports) {
        final String name = GoNames.exported(definition.name().name());
        final StringBuilder go = new StringBuilder(GoDoc.of(name, definition.documentation(), definition));
        go.append("type ").append(name).append(" int\n\n");
        go.append("const (\n");
        final List<String> constants = new ArrayList<>();
        boolean first = true;
        for (final EnumValueDefinition value : definition.values()) {
            final String constant = GoNames.enumConstant(definition.name().name(), value.name());
            final String doc = GoDoc.of(constant, value.documentation(), value);
            if (!doc.isEmpty()) {
                doc.lines().forEach(line -> constants.add("\t" + line));
            }
            constants.add("\t" + constant + (first ? " " + name + " = iota" : ""));
            first = false;
        }
        go.append(GoFormat.block(constants));
        go.append(")\n\n");
        go.append(names(definition, name, imports));
        return go.toString();
    }

    /** With attributes: a struct and package-level values, because Go has no constant struct. */
    private String valueEnum(final TypeDefinition.EnumDefinition definition, final GoImports imports) {
        final String name = GoNames.exported(definition.name().name());
        final StringBuilder go = new StringBuilder(GoDoc.of(name, definition.documentation(), definition));
        go.append("type ").append(name).append(" struct {\n");
        final List<String> attributes = new ArrayList<>(List.of("\tname string"));
        definition.attributes().forEach(attribute -> attributes.add("\t"
                + GoNames.unexported(attribute.name()) + " " + context.type(attribute.type()).render(imports)));
        go.append(GoFormat.block(attributes));
        go.append("}\n\n");

        go.append("var (\n");
        final List<String> values = new ArrayList<>();
        for (final EnumValueDefinition value : definition.values()) {
            final String constant = GoNames.enumConstant(definition.name().name(), value.name());
            final String doc = GoDoc.of(constant, value.documentation(), value);
            if (!doc.isEmpty()) {
                doc.lines().forEach(line -> values.add("\t" + line));
            }
            if (value.arguments().size() != definition.attributes().size()) {
                throw new GoGap("the value " + value.name() + " gives " + value.arguments().size()
                        + " argument(s) for " + definition.attributes().size() + " attribute(s)");
            }
            final List<String> arguments = new ArrayList<>();
            arguments.add('"' + value.name() + '"');
            for (int i = 0; i < definition.attributes().size(); i++) {
                arguments.add(literal(value.arguments().get(i), definition.attributes().get(i).type(), imports));
            }
            values.add("\t" + constant + " = " + name + "{" + String.join(", ", arguments) + "}");
        }
        go.append(GoFormat.block(values));
        go.append(")\n\n");

        for (final ParameterDefinition attribute : definition.attributes()) {
            final String getter = GoNames.exported(attribute.name());
            go.append(GoDoc.comment(getter + " returns the " + spaced(attribute.name()) + " of the value."));
            go.append("func (").append(receiver(name)).append(' ').append(name).append(") ").append(getter)
                    .append("() ").append(context.type(attribute.type()).render(imports)).append(" {\n")
                    .append("\treturn ").append(receiver(name)).append('.')
                    .append(GoNames.unexported(attribute.name())).append("\n}\n\n");
        }
        go.append(names(definition, name, imports));
        return go.toString();
    }

    /** {@code String()}, {@code <Name>Values()} and {@code <Name>ValueOf(name)}, shared by both enum shapes. */
    private String names(final TypeDefinition.EnumDefinition definition, final String name,
                         final GoImports imports) {
        final String receiver = receiver(name);
        final List<String> constants = definition.values().stream()
                .map(v -> GoNames.enumConstant(definition.name().name(), v.name())).toList();
        final StringBuilder go = new StringBuilder();

        go.append(GoDoc.comment("String returns the name of the value as the spec writes it."));
        go.append("func (").append(receiver).append(' ').append(name).append(") String() string {\n");
        if (definition.attributes().isEmpty()) {
            go.append("\tswitch ").append(receiver).append(" {\n");
            for (int i = 0; i < constants.size(); i++) {
                go.append("\tcase ").append(constants.get(i)).append(":\n\t\treturn \"")
                        .append(definition.values().get(i).name()).append("\"\n");
            }
            go.append("\t}\n\treturn \"\"\n}\n\n");
        } else {
            go.append("\treturn ").append(receiver).append(".name\n}\n\n");
        }

        go.append(GoDoc.comment(name + "Values returns every value of " + name + ", in declaration order."));
        go.append("func ").append(name).append("Values() []").append(name).append(" {\n")
                .append("\treturn []").append(name).append("{").append(String.join(", ", constants))
                .append("}\n}\n\n");

        go.append(GoDoc.comment(name + "ValueOf returns the value with the given name, or an error when no value "
                + "has it."));
        go.append("func ").append(name).append("ValueOf(name string) (").append(name).append(", error) {\n")
                .append("\tfor _, value := range ").append(name).append("Values() {\n")
                .append("\t\tif value.String() == name {\n\t\t\treturn value, nil\n\t\t}\n\t}\n")
                .append("\tvar zero ").append(name).append('\n')
                .append("\treturn zero, ").append(imports.qualify("fmt", "Errorf"))
                .append("(\"no ").append(name).append(" is named %q\", name)\n}\n");
        return go.toString();
    }

    // --- shared -------------------------------------------------------------------------------------------

    /**
     * A note in the doc comment for every type parameter whose spec bound Go cannot express, so that the lost
     * constraint is visible in the generated API rather than only in the specs.
     */
    private String widenedNote(final TypeDefinition definition) {
        final List<String> widened = new ArrayList<>();
        for (final TypeParameterDefinition parameter : GoContext.goParameters(definition)) {
            if (parameter.bound() == null) {
                continue;
            }
            final GoType bound = context.type(parameter.bound());
            if (!(bound instanceof GoType.Declared declared) || !declared.iface()) {
                widened.add(parameterName(parameter) + " (" + parameter.bound().text() + ")");
            }
        }
        if (widened.isEmpty()) {
            return "";
        }
        return GoDoc.comment("The type parameter" + (widened.size() == 1 ? " " : "s ")
                + String.join(", ", widened) + (widened.size() == 1 ? " is" : " are")
                + " unconstrained here: the bound the spec gives is a concrete type, and Go has no subtyping for"
                + " structs that a constraint could express.").replace("//", "//").indent(0);
    }

    /** The Go type of an attribute: a pointer when it is nullable and its zero value is not already nil. */
    private GoType fieldType(final FieldDefinition field, final GoImports imports) {
        final GoType type = context.type(field.type());
        return field.hasAnnotation("nullable") && !type.nilable() ? new GoType.Pointer(type) : type;
    }

    /**
     * Copies a slice or a map, so that neither the caller who passes a value in nor the caller who reads one out
     * keeps a reference into the struct's state. Without this an {@code @@immutable} attribute of such a type
     * would be immutable in name only.
     *
     * @param field   the attribute
     * @param value   the expression to copy
     * @param imports the imports of the file
     * @return the expression, wrapped in a clone where one is needed
     */
    private String copy(final FieldDefinition field, final String value, final GoImports imports) {
        final GoType type = context.type(field.type());
        if (type instanceof GoType.Bytes || type instanceof GoType.Slice) {
            return imports.use("slices") + ".Clone(" + value + ")";
        }
        if (type instanceof GoType.MapOf) {
            return imports.use("maps") + ".Clone(" + value + ")";
        }
        return value;
    }

    /** {@code [T any, U Unit]}, or {@code ""} when the type is not generic. */
    private String typeParameters(final List<TypeParameterDefinition> parameters, final GoImports imports) {
        if (parameters.isEmpty()) {
            return "";
        }
        return "[" + parameters.stream().map(parameter -> {
            final String name = parameterName(parameter);
            if (parameter.bound() == null) {
                return name + " any";
            }
            final GoType bound = context.type(parameter.bound());
            // Go constrains a parameter with an interface. A concrete bound has no Go form at all: a type set of
            // one struct admits that struct only, because Go has no subtyping for structs. The parameter is
            // widened to `any` and the declaration says so.
            return name + " " + (bound instanceof GoType.Declared declared && declared.iface()
                    ? bound.render(imports) : "any");
        }).collect(Collectors.joining(", ")) + "]";
    }

    /** {@code [T, U]}: the parameters again, as arguments, for a receiver or a return type. */
    private String typeArguments(final List<TypeParameterDefinition> parameters) {
        if (parameters.isEmpty()) {
            return "";
        }
        return "[" + parameters.stream().map(this::parameterName).collect(Collectors.joining(", ")) + "]";
    }

    /**
     * The Go name of a type parameter. {@code $$Receipt} would render as {@code Receipt} and shadow the declared
     * type of the same name, which Go rejects with "cannot use a type parameter as constraint"; such a name gets
     * a {@code T} suffix.
     */
    private String parameterName(final TypeParameterDefinition parameter) {
        return context.parameterName(parameter.name());
    }

    /** The receiver name: the first letter of the type, which is what Go code does. */
    private static String receiver(final String name) {
        return GoNames.identifier(name.substring(0, 1).toLowerCase(Locale.ROOT));
    }

    /** Makes a parameter name unique within a signature. */
    private static String unique(final String name, final Set<String> taken) {
        String candidate = name;
        for (int i = 2; !taken.add(candidate); i++) {
            candidate = name + i;
        }
        return candidate;
    }

    /** Renders a literal of an enum attribute. */
    private String literal(final Literal literal, final Type type, final GoImports imports) {
        return switch (literal) {
            case Literal.StringLiteral string -> '"' + string.value().replace("\\", "\\\\")
                    .replace("\"", "\\\"") + '"';
            case Literal.NumberLiteral number -> number.text();
            case Literal.NameLiteral name -> name(name, type, imports);
            default -> throw new GoGap("the literal " + literal.text() + " has no Go form yet");
        };
    }

    /** A name literal is {@code true}/{@code false} or a reference to an enum value. */
    private String name(final Literal.NameLiteral literal, final Type type, final GoImports imports) {
        final String text = literal.text();
        if ("true".equals(text) || "false".equals(text)) {
            return text;
        }
        if (!(type instanceof Type.DeclaredType declared)) {
            throw new GoGap("the literal " + text + " has no Go form yet");
        }
        // the value may be written qualified (Color.RED) or bare (RED) when the attribute type says which enum
        final String value = text.substring(text.lastIndexOf('.') + 1);
        final String constant = GoNames.enumConstant(declared.name().name(), value);
        return imports.qualify(context.importPath(declared.name()), constant);
    }
}
