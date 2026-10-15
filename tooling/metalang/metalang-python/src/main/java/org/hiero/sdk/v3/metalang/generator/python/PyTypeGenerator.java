package org.hiero.sdk.v3.metalang.generator.python;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.model.EnumValueDefinition;
import org.hiero.sdk.v3.metalang.model.FieldDefinition;
import org.hiero.sdk.v3.metalang.model.ParameterDefinition;
import org.hiero.sdk.v3.metalang.model.QualifiedName;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.model.TypeDefinition;
import org.hiero.sdk.v3.metalang.model.TypeParameterDefinition;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;

/**
 * Generates the Python declaration of one spec type: a dataclass for a concrete type, a protocol for an abstraction, a
 * union alias for a sealed abstraction, and one of the two enum shapes for an enumeration.
 *
 * <p><strong>Inheritance.</strong> A concrete type extends at most one concrete type, nominally (dataclass
 * inheritance). It does not name the abstractions it implements: a dataclass that inherits a property of the same
 * name as one of its attributes takes the property as the attribute's default value, so the abstractions are
 * protocols that a concrete type satisfies structurally, and the concrete type carries all its effective attributes.
 */
final class PyTypeGenerator {

    private static final String INDENT = "    ";

    private final PyContext context;

    private final PyMembers members;

    /**
     * Creates the generator.
     *
     * @param context the generator context
     */
    PyTypeGenerator(final PyContext context) {
        this.context = Objects.requireNonNull(context, "context must not be null");
        this.members = new PyMembers(context);
    }

    /**
     * Generates the declaration; the caller adds the header and the imports.
     *
     * @param definition the declaration
     * @param imports    the imports of the module, extended by what the declaration needs
     * @return the Python code
     * @throws PyGap if the declaration has no Python form yet
     */
    String generate(final TypeDefinition definition, final PyImports imports) {
        return switch (definition) {
            case TypeDefinition.EnumDefinition enumeration -> enumeration(enumeration, imports);
            case TypeDefinition.ComplexTypeDefinition complex when complex.abstraction()
                    && complex.hasAnnotation("sealed") -> sealed(complex, imports);
            case TypeDefinition.ComplexTypeDefinition complex when complex.abstraction() ->
                    protocol(complex, imports);
            case TypeDefinition.ComplexTypeDefinition complex -> dataclass(complex, imports);
        };
    }

    // --- concrete types ---------------------------------------------------------------------------------------

    /** A concrete type: a dataclass with its own and the inherited abstract attributes. */
    private String dataclass(final TypeDefinition.ComplexTypeDefinition definition, final PyImports imports) {
        final Optional<Type.DeclaredType> superclass = context.superclass(definition);
        final Set<String> inherited = superclass.map(s -> context.model().definition(s).fields().stream()
                .map(FieldDefinition::name).collect(Collectors.toSet())).orElse(Set.of());
        final Set<String> declared = definition.declaredFields().stream().map(FieldDefinition::name)
                .collect(Collectors.toSet());
        final List<FieldDefinition> fields = definition.fields().stream()
                .filter(f -> !inherited.contains(f.name()) || declared.contains(f.name())).toList();
        final boolean frozen = context.isFrozen(definition.name());

        final StringBuilder py = new StringBuilder("@").append(imports.standard("dataclasses", "dataclass"))
                .append(frozen ? "(frozen=True, slots=True, kw_only=True)\n" : "(slots=True, kw_only=True)\n");
        py.append("class ").append(definition.name().name()).append(typeParameters(definition, imports));
        superclass.ifPresent(s -> py.append('(').append(context.declared(s, imports, PyContext.Mode.RUNTIME))
                .append(')'));
        py.append(":\n");
        final List<String> declarations = new ArrayList<>();
        final String doc = PyDoc.of(INDENT, withNotes(documentation(definition), definition), definition);
        if (!doc.isEmpty()) {
            declarations.add(doc);
        }
        for (final FieldDefinition field : fields) {
            declarations.add(INDENT + PyNames.identifier(field.name()) + ": " + fieldType(field, imports)
                    + defaultValue(field, imports) + "\n" + PyDoc.of(INDENT, field.documentation(), field));
        }
        // every effective method: Python has no inherited implementation here, and a dataclass satisfies a
        // protocol only when it declares what the protocol declares
        members.owner(definition);
        PyMembers.byName(definition.methods()).values()
                .forEach(overloads -> declarations.add(members.render(overloads, PyMembers.Kind.METHOD,
                        definition.name().name(), INDENT, imports)));
        return py.append(body(declarations)).toString();
    }

    /** The documentation of a concrete type, with the abstractions it implements. */
    private String documentation(final TypeDefinition.ComplexTypeDefinition definition) {
        final List<String> implemented = context.supertypes(definition, true).stream()
                .map(t -> "``" + t.name().name() + "``").toList();
        final String text = definition.documentation() == null ? "" : definition.documentation().strip();
        if (implemented.isEmpty()) {
            return text;
        }
        return (text.isEmpty() ? "" : text + "\n\n") + "Implements " + String.join(", ", implemented) + ".";
    }

    /** The default value of an attribute: its {@code @@default}, otherwise {@code None} when it is nullable. */
    private String defaultValue(final FieldDefinition field, final PyImports imports) {
        final Optional<Literal> literal = field.annotation("default").map(a -> a.arguments().getFirst());
        if (literal.isPresent()) {
            final String value = literal(literal.get(), field.type(), imports);
            return value.startsWith("field(") ? " = " + imports.standard("dataclasses", "field") + value.substring(5)
                    : " = " + value;
        }
        return field.hasAnnotation("nullable") ? " = None" : "";
    }

    // --- abstractions -----------------------------------------------------------------------------------------

    /** An abstraction: a protocol with read-only properties for immutable and attributes for mutable attributes. */
    private String protocol(final TypeDefinition.ComplexTypeDefinition definition, final PyImports imports) {
        final List<String> bases = new ArrayList<>();
        for (final Type.DeclaredType supertype : context.supertypes(definition, true)) {
            bases.add(context.declared(supertype, imports, PyContext.Mode.RUNTIME));
        }
        bases.add(imports.standard("typing", "Protocol"));
        final StringBuilder py = new StringBuilder("class ").append(definition.name().name())
                .append(typeParameters(definition, imports)).append('(').append(String.join(", ", bases))
                .append("):\n");
        final List<String> declarations = new ArrayList<>();
        final String doc = PyDoc.of(INDENT, withNotes(definition.documentation(), definition), definition);
        if (!doc.isEmpty()) {
            declarations.add(doc);
        }
        for (final FieldDefinition field : definition.declaredFields()) {
            final String name = PyNames.identifier(field.name());
            final String type = fieldType(field, imports);
            final String fieldDoc = PyDoc.of(INDENT + INDENT, field.documentation(), field);
            if (field.hasAnnotation("immutable")) {
                declarations.add(INDENT + "@property\n" + INDENT + "def " + name + "(self) -> " + type + ":\n"
                        + fieldDoc + INDENT + INDENT + "...\n");
            } else {
                declarations.add(INDENT + name + ": " + type + "\n"
                        + PyDoc.of(INDENT, field.documentation(), field));
            }
        }
        members.owner(definition);
        PyMembers.byName(definition.declaredMethods()).values()
                .forEach(overloads -> declarations.add(members.render(overloads, PyMembers.Kind.PROTOCOL,
                        definition.name().name(), INDENT, imports)));
        return py.append(body(declarations)).toString();
    }

    /**
     * A sealed abstraction: the union of the subtypes {@code @@sealed} permits.
     *
     * <p>A generic abstraction keeps its parameters on the alias although the union does not use them: a permitted
     * subtype binds them (a transaction binds its receipt and its self type), so the union of the subtypes is already
     * the closed set, while a usage that still passes arguments stays valid. Shared members need no separate
     * protocol - mypy resolves an attribute on a union when every member has it.
     */
    private String sealed(final TypeDefinition.ComplexTypeDefinition definition, final PyImports imports) {
        for (final QualifiedName variant : context.sealedMembers(definition)) {
            if (!PyContext.pythonParameters(context.model().type(variant)
                    .orElseThrow(() -> new PyGap("the permitted subtype " + variant.name() + " is not declared")))
                    .isEmpty()) {
                throw new PyGap("the permitted subtype " + variant.name()
                        + " is generic; a union alias of generic subtypes is not supported yet");
            }
        }
        final List<String> members = context.sealedMembers(definition).stream()
                .map(t -> imports.sdk(context.module(t), t.name(), true)).toList();
        final String doc = PyDoc.of("", definition.documentation(), definition);
        return "type " + definition.name().name() + typeParameters(definition, imports) + " = "
                + String.join(" | ", members) + "\n" + (doc.isEmpty() ? "" : doc);
    }

    // --- enumerations -----------------------------------------------------------------------------------------

    private String enumeration(final TypeDefinition.EnumDefinition definition, final PyImports imports) {
        final String name = definition.name().name();
        final StringBuilder py = new StringBuilder("class ").append(name).append('(')
                .append(imports.standard("enum", "Enum")).append("):\n");
        final List<String> members = new ArrayList<>();
        final String doc = PyDoc.of(INDENT, definition.documentation(), definition);
        if (!doc.isEmpty()) {
            members.add(doc);
        }
        final StringBuilder values = new StringBuilder();
        for (final EnumValueDefinition value : definition.values()) {
            final String text;
            if (definition.attributes().isEmpty()) {
                text = quote(value.name());
            } else {
                if (value.arguments().size() != definition.attributes().size()) {
                    throw new PyGap("the value " + value.name() + " gives " + value.arguments().size()
                            + " argument(s) for " + definition.attributes().size() + " attribute(s)");
                }
                final List<String> arguments = new ArrayList<>(List.of(quote(value.name())));
                for (int i = 0; i < definition.attributes().size(); i++) {
                    arguments.add(literal(value.arguments().get(i), definition.attributes().get(i).type(), imports));
                }
                text = "(" + String.join(", ", arguments) + ")";
            }
            values.append(INDENT).append(value.name()).append(" = ").append(text).append('\n')
                    .append(PyDoc.of(INDENT, value.documentation(), value));
        }
        members.add(values.toString());
        if (!definition.attributes().isEmpty()) {
            final StringBuilder init = new StringBuilder(INDENT).append("def __init__(self, value_name: str");
            for (final ParameterDefinition attribute : definition.attributes()) {
                init.append(", ").append(PyNames.identifier(attribute.name())).append(": ")
                        .append(context.type(attribute.type(), imports));
            }
            init.append(") -> None:\n");
            for (final ParameterDefinition attribute : definition.attributes()) {
                final String attributeName = PyNames.identifier(attribute.name());
                init.append(INDENT).append(INDENT).append("self._").append(attributeName).append(" = ")
                        .append(attributeName).append('\n');
            }
            members.add(init.toString());
            for (final ParameterDefinition attribute : definition.attributes()) {
                final String attributeName = PyNames.identifier(attribute.name());
                members.add(INDENT + "@property\n" + INDENT + "def " + attributeName + "(self) -> "
                        + context.type(attribute.type(), imports) + ":\n"
                        + PyDoc.docstring(INDENT + INDENT, "The " + String.join(" ", PyNames.words(attribute.name()))
                        .toLowerCase(java.util.Locale.ROOT) + " of the value.")
                        + INDENT + INDENT + "return self._" + attributeName + "\n");
            }
        }
        return py.append(body(members)).toString();
    }

    // --- shared -----------------------------------------------------------------------------------------------

    /**
     * The documentation with a note for every type parameter whose bound mentions another type parameter: Python
     * cannot express that, so the variables in the bound are widened to {@code Any} and the declaration says so.
     */
    private String withNotes(final String documentation, final TypeDefinition definition) {
        final List<String> widened = PyContext.pythonParameters(definition).stream()
                .filter(p -> p.bound() != null && PyContext.mentionsVariable(p.bound()))
                .map(p -> "``" + context.parameterName(p.name()) + "`` (" + p.bound().text() + ")").toList();
        final String text = documentation == null ? "" : documentation.strip();
        if (widened.isEmpty()) {
            return text;
        }
        return (text.isEmpty() ? "" : text + "\n\n") + "The bound" + (widened.size() == 1 ? " of the type parameter "
                : "s of the type parameters ") + String.join(", ", widened) + (widened.size() == 1 ? " is" : " are")
                + " widened: a Python bound cannot mention another type parameter.";
    }

    /** The members of a class body, separated by blank lines; an empty body is {@code pass}. */
    private static String body(final List<String> members) {
        return members.isEmpty() ? INDENT + "pass\n" : String.join("\n", members);
    }

    /** {@code [T, R: Receipt]}, or {@code ""} for a type that is not generic. */
    private String typeParameters(final TypeDefinition definition, final PyImports imports) {
        final List<TypeParameterDefinition> parameters = PyContext.pythonParameters(definition);
        if (parameters.isEmpty()) {
            return "";
        }
        return "[" + parameters.stream().map(p -> context.parameterName(p.name())
                + (p.bound() == null ? "" : ": " + context.type(p.bound(), imports, PyContext.Mode.BOUND)))
                .collect(Collectors.joining(", ")) + "]";
    }

    /** The annotation of an attribute: {@code T | None} when it is nullable. */
    private String fieldType(final FieldDefinition field, final PyImports imports) {
        final String type = context.type(field.type(), imports);
        return field.hasAnnotation("nullable") && !"object".equals(type) ? type + " | None" : type;
    }

    /**
     * Renders a literal of a default value or an enum attribute.
     *
     * @param literal the literal
     * @param type    the type of the value
     * @param imports the imports of the module
     * @return the Python expression; {@code field(default_factory=...)} for a mutable default
     */
    private String literal(final Literal literal, final Type type, final PyImports imports) {
        return switch (literal) {
            case Literal.StringLiteral string -> quote(string.value());
            case Literal.NumberLiteral number -> number(number.text(), type, imports);
            case Literal.NameLiteral name -> name(name.text(), type, imports);
            case Literal.ListLiteral list -> list(list, type, imports);
            default -> throw new PyGap("the literal " + literal.text() + " has no Python form yet");
        };
    }

    private String number(final String text, final Type type, final PyImports imports) {
        if (type instanceof Type.BasicType basic) {
            final BuiltinType.Category category = basic.builtin().category();
            if (category == BuiltinType.Category.DECIMAL) {
                return imports.module("decimal") + ".Decimal(" + quote(text) + ")";
            }
            if (category == BuiltinType.Category.INTEGER || category == BuiltinType.Category.FLOAT) {
                return text;
            }
        }
        throw new PyGap("the number " + text + " has no Python form as a " + type.text());
    }

    /** {@code true}, {@code false}, {@code null}, or a value of an enumeration. */
    private String name(final String text, final Type type, final PyImports imports) {
        switch (text) {
            case "true" -> {
                return "True";
            }
            case "false" -> {
                return "False";
            }
            case "null" -> {
                return "None";
            }
            default -> {
                // the value may be written qualified (Color.RED) or bare (RED) when the type says which enum
                if (type instanceof Type.DeclaredType declared && context.model().type(declared.name())
                        .filter(TypeDefinition.EnumDefinition.class::isInstance).isPresent()) {
                    return imports.sdk(context.module(declared.name()), declared.name().name(), true) + "."
                            + text.substring(text.lastIndexOf('.') + 1);
                }
                throw new PyGap("the literal " + text + " has no Python form yet");
            }
        }
    }

    /** A list literal: a tuple, a frozenset, or an empty mapping created per instance. */
    private String list(final Literal.ListLiteral list, final Type type, final PyImports imports) {
        if (!(type instanceof Type.BasicType basic)) {
            throw new PyGap("the list " + list.text() + " is no value of " + type.text());
        }
        final String category = basic.builtin().name();
        if (basic.builtin().category() == BuiltinType.Category.MAP && list.items().isEmpty()) {
            return "field(default_factory=dict)";
        }
        if (basic.builtin().category() == BuiltinType.Category.BYTES) {
            // a list of byte values: bytes are immutable, so the value can be shared
            return list.items().isEmpty() ? "b\"\"" : "bytes((" + list.items().stream().map(item -> literal(item,
                    new Type.BasicType(BuiltinType.lookup("uint8").orElseThrow(), List.of()), imports))
                    .collect(Collectors.joining(", ")) + (list.items().size() == 1 ? ",))" : "))");
        }
        if (basic.builtin().category() != BuiltinType.Category.COLLECTION) {
            throw new PyGap("the list " + list.text() + " is no value of " + type.text());
        }
        final Type element = basic.arguments().getFirst();
        final List<String> items = list.items().stream().map(item -> literal(item, element, imports)).toList();
        if ("set".equals(category)) {
            return items.isEmpty() ? "frozenset()" : "frozenset({" + String.join(", ", items) + "})";
        }
        return items.isEmpty() ? "()" : "(" + String.join(", ", items) + (items.size() == 1 ? ",)" : ")");
    }

    /** A Python string literal. */
    static String quote(final String value) {
        return '"' + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + '"';
    }
}
