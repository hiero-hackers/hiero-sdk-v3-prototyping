package org.hiero.sdk.v3.metalang.generator.ts;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.model.EnumValueDefinition;
import org.hiero.sdk.v3.metalang.model.FieldDefinition;
import org.hiero.sdk.v3.metalang.model.MethodDefinition;
import org.hiero.sdk.v3.metalang.model.ParameterDefinition;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.model.TypeDefinition;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;

/**
 * Generates the file of a type (see {@code guidelines/api-best-practices-ts.md}).
 *
 * <ul>
 *   <li>An <b>abstraction</b> becomes an {@code interface}: attributes are {@code readonly} properties (mutable ones
 *       without {@code readonly}), methods are signatures. {@code @@static} methods are functions of a namespace with
 *       the name of the interface ({@code TransactionId.generateTransactionId(...)}).</li>
 *   <li>A <b>complex type</b> becomes a {@code class} that implements its abstractions and extends its concrete
 *       supertype. Attributes are {@code #private} fields with getters (and setters for mutable attributes); the
 *       constructor takes one object with all attributes ({@code new AccountId({ shard: 0n, ... })}), nullable
 *       attributes and attributes with {@code @@default} may be omitted. Constructor and setters check
 *       {@code null}, the integer ranges and the validation annotations and copy {@code bytes}, collections and dates;
 *       getters return copies of {@code bytes}, sets, maps and dates (arrays are frozen). Instances of a class
 *       without subtypes are frozen. Methods are stubs.</li>
 *   <li>An <b>enum</b> becomes a class with one {@code static readonly} instance per value, a private constructor
 *       with the attributes, {@code name}, {@code values()} and {@code valueOf(name)}.</li>
 * </ul>
 */
final class TsTypeGenerator {

    private static final String INDENT = "    ";

    private TsTypeGenerator() {
    }

    static GeneratedFile generate(final TypeDefinition type, final TsContext context) {
        final String folder = context.folder(type.name().namespace());
        final String directory = TsNames.sourceDirectory(folder, type.name().namespace());
        final TsImports imports = new TsImports(context, folder, directory, type.name().name());
        final String body = switch (type) {
            case TypeDefinition.EnumDefinition enumType -> enumClass(enumType, imports);
            case TypeDefinition.ComplexTypeDefinition complex when complex.abstraction() -> interfaceType(complex,
                    imports);
            case TypeDefinition.ComplexTypeDefinition complex -> classType(complex, imports);
        };
        final StringBuilder ts = new StringBuilder(TsGenerator.HEADER).append('\n');
        final String importBlock = imports.render();
        if (!importBlock.isEmpty()) {
            ts.append(importBlock).append('\n');
        }
        ts.append(body);
        return new GeneratedFile(directory + "/" + type.name().name() + ".ts", ts.toString());
    }

    // --- interfaces ------------------------------------------------------------------------------

    private static String interfaceType(final TypeDefinition.ComplexTypeDefinition type, final TsImports imports) {
        final String name = type.name().name();
        final List<String> permitted = permitted(type, imports.context());
        if (!permitted.isEmpty()) {
            // a sealed abstraction is the union of its permitted types, distinguished with instanceof
            final StringBuilder ts = new StringBuilder(TsDoc.render("", List.of(type.documentation()), List.of(),
                    type.hasAnnotation("deprecated")));
            ts.append("export type ").append(name).append(TsTypes.typeParameters(type.typeParameters(), imports))
                    .append(" = ").append(String.join(" | ", permitted.stream()
                            .map(p -> imports.type(new org.hiero.sdk.v3.metalang.model.QualifiedName(
                                    p.substring(0, p.lastIndexOf('.')), p.substring(p.lastIndexOf('.') + 1))))
                            .toList())).append(";\n");
            return ts.toString();
        }
        final List<String> members = new ArrayList<>();
        for (final FieldDefinition field : type.declaredFields()) {
            members.add(TsDoc.render(INDENT, List.of(field.documentation()), List.of(),
                    field.hasAnnotation("deprecated")) + INDENT + (field.hasAnnotation("immutable") ? "readonly " : "")
                    + field.name() + ": " + TsTypes.declaration(field.type(), field.hasAnnotation("nullable"), imports)
                    + ";\n");
        }
        members.addAll(TsMembers.render(name, type.declaredMethods().stream().filter(m -> !m.isStatic()).toList(),
                TsMembers.Kind.INTERFACE, INDENT, imports));
        final List<String> supertypes = type.supertypes().stream().filter(Type.DeclaredType.class::isInstance)
                .map(Type.DeclaredType.class::cast).filter(s -> !isSealed(s, imports.context()))
                .map(s -> TsTypes.type(s, imports)).toList();
        final StringBuilder ts = new StringBuilder(TsDoc.render("", List.of(type.documentation()), List.of(),
                type.hasAnnotation("deprecated")));
        ts.append("export interface ").append(name).append(TsTypes.typeParameters(type.typeParameters(), imports))
                .append(supertypes.isEmpty() ? "" : " extends " + String.join(", ", supertypes)).append(" {\n");
        ts.append(String.join("\n", members)).append("}\n");
        final List<MethodDefinition> statics = type.declaredMethods().stream().filter(MethodDefinition::isStatic)
                .toList();
        if (!statics.isEmpty()) {
            ts.append('\n').append(TsDoc.render("", List.of("Functions of `" + name + "`.")))
                    .append("export namespace ").append(name).append(" {\n")
                    .append(String.join("\n", TsMembers.render(name, statics, TsMembers.Kind.FUNCTION, INDENT,
                            imports)))
                    .append("}\n");
        }
        return ts.toString();
    }

    // --- classes ---------------------------------------------------------------------------------

    private static String classType(final TypeDefinition.ComplexTypeDefinition type, final TsImports imports) {
        final TsContext context = imports.context();
        final String name = type.name().name();
        final Optional<Type.DeclaredType> superclass = context.superclass(type);
        final Optional<TypeDefinition> superDefinition = superclass.map(s -> context.model().definition(s));
        final Set<String> inherited = superDefinition.map(d -> d.fields().stream().map(FieldDefinition::name)
                .collect(Collectors.toSet())).orElse(Set.of());
        final List<FieldDefinition> stored = type.fields().stream().filter(f -> !inherited.contains(f.name()))
                .toList();
        final List<String> members = new ArrayList<>();
        boolean urlHelper = false;

        // fields
        if (!stored.isEmpty()) {
            members.add(stored.stream().map(f -> INDENT + (f.hasAnnotation("immutable") ? "readonly " : "") + "#"
                    + f.name() + ": " + TsTypes.declaration(f.type(), f.hasAnnotation("nullable"), imports) + ";\n")
                    .collect(Collectors.joining()));
        }

        // constructor
        final StringBuilder constructor = new StringBuilder();
        if (superclass.isPresent()) {
            constructor.append(INDENT).append(INDENT).append("super(init);\n");
        }
        boolean nullChecks = false;
        boolean rangeChecks = false;
        for (final FieldDefinition field : type.fields()) {
            final boolean own = !inherited.contains(field.name());
            final boolean narrowed = !own && type.name().equals(field.declaringType());
            if (!own && !narrowed) {
                continue;
            }
            final String local = TsNames.local(field.name());
            constructor.append(INDENT).append(INDENT).append("const ").append(local).append(" = ")
                    .append(initValue(field, imports)).append(";\n");
            final String checks = TsConstraints.checks(field, local, INDENT + INDENT, imports);
            nullChecks |= checks.contains("TypeError");
            rangeChecks |= checks.contains("RangeError");
            urlHelper |= TsConstraints.needsUrlCheck(field);
            constructor.append(checks);
            if (own) {
                constructor.append(INDENT).append(INDENT).append("this.#").append(field.name()).append(" = ")
                        .append(copyIn(field, local)).append(";\n");
            }
        }
        if (!context.hasSubtypes(type.name())) {
            constructor.append(INDENT).append(INDENT).append("Object.freeze(this);\n");
        }
        final List<String> tags = new ArrayList<>(List.of("@param init - the attributes"));
        if (nullChecks) {
            tags.add("@throws TypeError if a required attribute is `null`");
        }
        if (rangeChecks) {
            tags.add("@throws RangeError if an attribute violates its constraints");
        }
        members.add(TsDoc.render(INDENT, List.of("Creates a new `" + name + "`."), tags, false) + INDENT
                + "constructor(init: " + initType(type.fields(), imports) + ") {\n" + constructor + INDENT + "}\n");

        // accessors
        for (final FieldDefinition field : type.fields()) {
            final boolean own = !inherited.contains(field.name());
            final boolean narrowed = !own && type.name().equals(field.declaringType());
            if (own) {
                members.add(getter(field, "this.#" + field.name(), imports));
                if (!field.hasAnnotation("immutable")) {
                    members.add(setter(field, imports, false));
                    urlHelper |= TsConstraints.needsUrlCheck(field);
                }
            } else if (narrowed) {
                final String declaration = TsTypes.declaration(field.type(), field.hasAnnotation("nullable"), imports);
                members.add(TsDoc.render(INDENT, List.of(field.documentation()), List.of(),
                        field.hasAnnotation("deprecated")) + INDENT + "override get " + field.name() + "(): "
                        + declaration + " {\n" + INDENT + INDENT + "return super." + field.name() + " as "
                        + declaration + ";\n" + INDENT + "}\n");
                if (!field.hasAnnotation("immutable")) {
                    members.add(setter(field, imports, true));
                }
            }
        }

        // methods: own ones, inherited ones that no concrete superclass implements, own static ones
        final List<MethodDefinition> methods = type.methods().stream()
                .filter(m -> m.isStatic() ? type.name().equals(m.declaringType())
                        : type.name().equals(m.declaringType()) || superDefinition.isEmpty()
                        || superDefinition.get().methods().stream().noneMatch(s -> s.signature().equals(m.signature())))
                .toList();
        members.addAll(TsMembers.render(name, methods, TsMembers.Kind.CLASS, INDENT, imports));

        final List<String> interfaces = type.supertypes().stream().filter(Type.DeclaredType.class::isInstance)
                .map(Type.DeclaredType.class::cast)
                .filter(s -> superclass.map(c -> !c.name().equals(s.name())).orElse(true))
                .filter(s -> !context.isClass(s.name()) && !isSealed(s, context))
                .map(s -> TsTypes.type(s, imports)).toList();
        final StringBuilder ts = new StringBuilder(TsDoc.render("", List.of(type.documentation()), List.of(),
                type.hasAnnotation("deprecated")));
        ts.append("export class ").append(name).append(TsTypes.typeParameters(type.typeParameters(), imports));
        superclass.ifPresent(s -> ts.append(" extends ").append(imports.value(s.name()))
                .append(typeArguments(s, imports)));
        if (!interfaces.isEmpty()) {
            ts.append(" implements ").append(String.join(", ", interfaces));
        }
        ts.append(" {\n\n").append(String.join("\n", members)).append("}\n");
        if (urlHelper) {
            ts.append('\n').append(TsConstraints.urlHelper());
        }
        return ts.toString();
    }

    /** The permitted subtypes of a sealed abstraction ({@code @@sealed(A, B)}) as qualified names. */
    private static List<String> permitted(final TypeDefinition type, final TsContext context) {
        return type.annotation("sealed").stream().flatMap(a -> a.arguments().stream()).map(a -> a.text())
                .map(n -> n.contains(".") ? n : type.name().namespace() + "." + n)
                .filter(n -> context.isGenerated(new org.hiero.sdk.v3.metalang.model.QualifiedName(
                        n.substring(0, n.lastIndexOf('.')), n.substring(n.lastIndexOf('.') + 1))))
                .toList();
    }

    /** Whether a supertype is a sealed abstraction: a union type that classes do not implement explicitly. */
    private static boolean isSealed(final Type.DeclaredType type, final TsContext context) {
        return context.model().type(type.name()).map(t -> !permitted(t, context).isEmpty()).orElse(false);
    }

    private static String typeArguments(final Type.DeclaredType type, final TsImports imports) {
        final String ts = TsTypes.type(type, imports);
        final int start = ts.indexOf('<');
        return start < 0 ? "" : ts.substring(start);
    }

    /** The type of the constructor argument: all attributes, the nullable ones and those with a default optional. */
    private static String initType(final List<FieldDefinition> fields, final TsImports imports) {
        if (fields.isEmpty()) {
            return "{}";
        }
        return "{\n" + fields.stream().map(f -> INDENT + INDENT + "readonly " + f.name()
                        + (f.hasAnnotation("nullable") || f.hasAnnotation("default") ? "?: " : ": ")
                        + TsTypes.declaration(f.type(), f.hasAnnotation("nullable"), imports) + ";\n")
                .collect(Collectors.joining()) + INDENT + "}";
    }

    /** The value of an attribute from the constructor argument, with the default where it is omitted. */
    private static String initValue(final FieldDefinition field, final TsImports imports) {
        final String value = "init." + field.name();
        if (field.hasAnnotation("default")) {
            final Literal literal = field.annotation("default").orElseThrow().arguments().getFirst();
            return value + " === undefined ? " + TsLiterals.expression(literal, field.type(), imports) + " : " + value;
        }
        if (field.hasAnnotation("nullable")) {
            return value + " === undefined ? null : " + value;
        }
        return value;
    }

    private static String getter(final FieldDefinition field, final String value, final TsImports imports) {
        final String declaration = TsTypes.declaration(field.type(), field.hasAnnotation("nullable"), imports);
        return TsDoc.render(INDENT, List.of(field.documentation().isBlank() ? "Returns the `" + field.name() + "`."
                : field.documentation()), List.of(), field.hasAnnotation("deprecated"))
                + INDENT + "get " + field.name() + "(): " + declaration + " {\n"
                + INDENT + INDENT + "return " + copyOut(field, value) + ";\n" + INDENT + "}\n";
    }

    private static String setter(final FieldDefinition field, final TsImports imports, final boolean narrowed) {
        final String declaration = TsTypes.declaration(field.type(), field.hasAnnotation("nullable"), imports);
        final String checks = TsConstraints.checks(field, "value", INDENT + INDENT, imports);
        final List<String> tags = new ArrayList<>();
        if (checks.contains("TypeError")) {
            tags.add("@throws TypeError if the value is `null`");
        }
        if (checks.contains("RangeError")) {
            tags.add("@throws RangeError if the value violates its constraints");
        }
        return TsDoc.render(INDENT, List.of("Sets the `" + field.name() + "`."), tags,
                field.hasAnnotation("deprecated"))
                + INDENT + (narrowed ? "override " : "") + "set " + field.name() + "(value: " + declaration + ") {\n"
                + checks + INDENT + INDENT + (narrowed ? "super." + field.name() + " = value"
                : "this.#" + field.name() + " = " + copyIn(field, "value")) + ";\n" + INDENT + "}\n";
    }

    /** The stored value: a copy of {@code bytes}, collections and dates, a frozen copy of arrays. */
    static String copyIn(final FieldDefinition field, final String value) {
        final String copy = copy(field.type(), value, true);
        if (copy.equals(value) || !field.hasAnnotation("nullable")) {
            return copy;
        }
        return value + " === null ? null : " + copy;
    }

    /** The returned value: a copy of {@code bytes}, sets, maps and dates (arrays are frozen). */
    static String copyOut(final FieldDefinition field, final String value) {
        final String copy = copy(field.type(), value, false);
        if (copy.equals(value) || !field.hasAnnotation("nullable")) {
            return copy;
        }
        return value + " === null ? null : " + copy;
    }

    private static String copy(final Type type, final String value, final boolean in) {
        if (!(type instanceof Type.BasicType basic)) {
            return value;
        }
        final BuiltinType builtin = basic.builtin();
        return switch (builtin.category()) {
            case BYTES -> value + ".slice()";
            case COLLECTION -> builtin.name().equals("set") ? "new Set(" + value + ")"
                    : in ? "Object.freeze([..." + value + "])" : value;
            case MAP -> "new Map(" + value + ")";
            case TEMPORAL -> "new Date(" + value + ".getTime())";
            default -> value;
        };
    }

    // --- enums -----------------------------------------------------------------------------------

    private static String enumClass(final TypeDefinition.EnumDefinition type, final TsImports imports) {
        final String name = type.name().name();
        final List<String> members = new ArrayList<>();
        final StringBuilder constants = new StringBuilder();
        for (final EnumValueDefinition value : type.values()) {
            final List<String> arguments = new ArrayList<>(List.of(TsLiterals.quote(value.name())));
            for (int i = 0; i < value.arguments().size() && i < type.attributes().size(); i++) {
                arguments.add(TsLiterals.expression(value.arguments().get(i), type.attributes().get(i).type(),
                        imports));
            }
            constants.append(TsDoc.render(INDENT, List.of(value.documentation()), List.of(),
                    value.hasAnnotation("deprecated"))).append(INDENT).append("static readonly ").append(value.name())
                    .append(": ").append(name).append(" = new ").append(name).append('(')
                    .append(String.join(", ", arguments)).append(");\n");
        }
        members.add(constants.toString());
        final StringBuilder fields = new StringBuilder(INDENT + "readonly #name: string;\n");
        final StringBuilder assignments = new StringBuilder(INDENT + INDENT + "this.#name = name;\n");
        final List<String> parameters = new ArrayList<>(List.of("name: string"));
        for (final ParameterDefinition attribute : type.attributes()) {
            final String declaration = TsTypes.declaration(attribute.type(), attribute.hasAnnotation("nullable"),
                    imports);
            fields.append(INDENT).append("readonly #").append(attribute.name()).append(": ").append(declaration)
                    .append(";\n");
            parameters.add(TsNames.local(attribute.name()) + ": " + declaration);
            assignments.append(INDENT).append(INDENT).append("this.#").append(attribute.name()).append(" = ")
                    .append(TsNames.local(attribute.name())).append(";\n");
        }
        members.add(fields.toString());
        members.add(INDENT + "private constructor(" + String.join(", ", parameters) + ") {\n" + assignments + INDENT
                + INDENT + "Object.freeze(this);\n" + INDENT + "}\n");
        members.add(TsDoc.render(INDENT, List.of("Returns the name of the constant.")) + INDENT
                + "get name(): string {\n" + INDENT + INDENT + "return this.#name;\n" + INDENT + "}\n");
        for (final ParameterDefinition attribute : type.attributes()) {
            final FieldDefinition field = new FieldDefinition(attribute.name(), attribute.type(), type.name(),
                    attribute.annotations(), "", attribute.location());
            members.add(getter(field, "this.#" + attribute.name(), imports));
        }
        members.add(TsDoc.render(INDENT, List.of("Returns all constants in declaration order."), List.of(
                "@returns the constants"), false) + INDENT + "static values(): ReadonlyArray<" + name + "> {\n"
                + INDENT + INDENT + "return Object.freeze([" + type.values().stream().map(v -> name + "." + v.name())
                .collect(Collectors.joining(", ")) + "]);\n" + INDENT + "}\n");
        members.add(TsDoc.render(INDENT, List.of("Returns the constant with the given name."), List.of(
                "@param name - the name of the constant", "@returns the constant",
                "@throws RangeError if there is no constant with the name"), false)
                + INDENT + "static valueOf(name: string): " + name + " {\n"
                + INDENT + INDENT + "const value = " + name + ".values().find(v => v.name === name);\n"
                + INDENT + INDENT + "if (value === undefined) {\n"
                + INDENT + INDENT + INDENT + "throw new RangeError(`No constant " + name + ".${name}`);\n"
                + INDENT + INDENT + "}\n"
                + INDENT + INDENT + "return value;\n" + INDENT + "}\n");
        if (type.methods("toString").isEmpty()) {
            members.add(INDENT + "toString(): string {\n" + INDENT + INDENT + "return this.#name;\n" + INDENT + "}\n");
        }
        members.addAll(TsMembers.render(name, type.methods().stream()
                .filter(m -> !m.isStatic() || type.name().equals(m.declaringType())).toList(), TsMembers.Kind.CLASS,
                INDENT, imports));
        final List<String> interfaces = type.supertypes().stream().filter(Type.DeclaredType.class::isInstance)
                .map(Type.DeclaredType.class::cast).filter(s -> !isSealed(s, imports.context()))
                .map(s -> TsTypes.type(s, imports)).toList();
        return TsDoc.render("", List.of(type.documentation()), List.of(), type.hasAnnotation("deprecated"))
                + "export class " + name + (interfaces.isEmpty() ? "" : " implements " + String.join(", ", interfaces))
                + " {\n\n" + String.join("\n", members) + "}\n";
    }
}
