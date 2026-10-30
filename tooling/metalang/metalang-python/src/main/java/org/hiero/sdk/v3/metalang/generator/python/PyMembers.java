package org.hiero.sdk.v3.metalang.generator.python;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.hiero.sdk.v3.metalang.model.MethodDefinition;
import org.hiero.sdk.v3.metalang.model.ParameterDefinition;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.model.TypeDefinition;
import org.hiero.sdk.v3.metalang.model.TypeParameterDefinition;

/**
 * Methods and functions in Python, following
 * {@code guidelines/api-best-practices-python.md} ("Methods and overloading", "Asynchronous methods", "Streaming"):
 *
 * <ul>
 *   <li>a method is an instance method with a {@code snake_case} name; a namespace function is a module-level
 *       function and a {@code @@static} method a {@code @staticmethod};</li>
 *   <li>{@code @@async} is {@code async def}, returning the awaited value - there is no synchronous twin;</li>
 *   <li>{@code @@streaming} returns {@code collections.abc.AsyncIterator[T]};</li>
 *   <li>an overload set stays one name, as Python does it: {@code typing.overload} declarations followed by one
 *       implementation.</li>
 * </ul>
 *
 * <p>A protocol declares its members with {@code ...}; everywhere else the body raises {@code NotImplementedError},
 * the Python counterpart of the stubs the other generators emit.
 */
final class PyMembers {

    private static final String INDENT = "    ";

    /** Where a member is rendered, which decides the receiver and the body. */
    enum Kind {
        /** Inside a {@code Protocol}: {@code self}, and {@code ...} as the body. */
        PROTOCOL,
        /** Inside a class: {@code self}, and a raising body. */
        METHOD,
        /** Module level: no {@code self}, and a raising body. */
        FUNCTION
    }

    /** The builtin type names the Python mapping uses; a member of the same name shadows one inside the class. */
    private static final Set<String> BUILTINS = Set.of("bool", "bytes", "complex", "dict", "float", "frozenset",
            "int", "list", "object", "set", "str", "tuple", "type");

    private final PyContext context;

    /** The builtins the declaration being rendered shadows with a member of the same name. */
    private Set<String> shadowed = Set.of();

    PyMembers(final PyContext context) {
        this.context = Objects.requireNonNull(context, "context must not be null");
    }

    /**
     * Binds the declaration whose members are rendered next. A member named like a builtin - an attribute
     * {@code bytes} - shadows it inside the class body, so an annotation of that type has to name the module.
     *
     * @param definition the declaration, or {@code null} for module-level functions
     */
    void owner(final TypeDefinition.ComplexTypeDefinition definition) {
        if (definition == null) {
            shadowed = Set.of();
            return;
        }
        final Set<String> names = new HashSet<>();
        definition.fields().forEach(field -> names.add(PyNames.identifier(field.name())));
        definition.methods().forEach(method -> names.add(PyNames.identifier(method.name())));
        names.retainAll(BUILTINS);
        shadowed = Set.copyOf(names);
    }

    /**
     * An annotation with every shadowed builtin qualified, so that it names the type and not the member.
     *
     * @param rendered the rendered annotation
     * @param imports the imports of the module
     * @return the annotation
     */
    String annotation(final String rendered, final PyImports imports) {
        String text = rendered;
        for (final String builtin : shadowed) {
            if (text.matches(".*\\b" + builtin + "\\b.*")) {
                text = text.replaceAll("\\b" + builtin + "\\b", imports.module("builtins") + "." + builtin);
            }
        }
        return text;
    }

    /**
     * Groups methods by name, in declaration order; a name with more than one entry is an overload set.
     *
     * @param methods the methods
     * @return the methods of each name
     */
    static Map<String, List<MethodDefinition>> byName(final List<MethodDefinition> methods) {
        final Map<String, List<MethodDefinition>> grouped = new LinkedHashMap<>();
        methods.forEach(method -> grouped.computeIfAbsent(method.name(), key -> new ArrayList<>()).add(method));
        return grouped;
    }

    /**
     * Renders one method or an overload set of the same name.
     *
     * @param overloads the declarations of one name, in declaration order
     * @param kind      where the member is rendered
     * @param owner     the type or namespace, named in the {@code NotImplementedError}
     * @param indent    the indentation of the member
     * @param imports   the imports of the module
     * @return the Python code
     * @throws PyGap if the member has no Python form
     */
    String render(final List<MethodDefinition> overloads, final Kind kind, final String owner, final String indent,
                  final PyImports imports) {
        if (overloads.size() == 1) {
            return definition(overloads.getFirst(), kind, owner, indent, imports, null, null);
        }
        final StringBuilder py = new StringBuilder();
        for (final MethodDefinition overload : overloads) {
            py.append(indent).append('@').append(imports.standard("typing", "overload")).append('\n')
                    .append(signature(overload, kind, indent, imports)).append(" ...\n");
        }
        // one implementation carries the documentation and the body; its signature has to accept every overload,
        // and its result has to be one every overload can produce
        final String any = imports.standard("typing", "Any");
        final List<String> results = overloads.stream().map(overload -> result(overload, imports)).distinct()
                .toList();
        py.append(definition(overloads.getFirst(), kind, owner, indent, imports,
                (kind == Kind.FUNCTION || overloads.getFirst().isStatic() ? "" : "self, ") + "*args: " + any
                        + ", **kwargs: " + any,
                results.size() == 1 ? null : String.join(" | ", results)));
        return py.toString();
    }

    /** A complete definition: signature, docstring and body. */
    private String definition(final MethodDefinition method, final Kind kind, final String owner,
                              final String indent, final PyImports imports, final String parameterOverride,
                              final String resultOverride) {
        final StringBuilder py = new StringBuilder(signature(method, kind, indent, imports, parameterOverride,
                resultOverride));
        py.append('\n');
        final String doc = PyDoc.of(indent + INDENT, method.documentation(), method);
        py.append(doc);
        py.append(indent).append(INDENT).append(kind == Kind.PROTOCOL ? "..."
                : "raise NotImplementedError(\"Not implemented yet: " + owner + "." + name(method) + "\")");
        return py.append('\n').toString();
    }

    private String signature(final MethodDefinition method, final Kind kind, final String indent,
                             final PyImports imports) {
        return signature(method, kind, indent, imports, null, null);
    }

    /** {@code    async def load(self, key: str) -> str:} */
    private String signature(final MethodDefinition method, final Kind kind, final String indent,
                             final PyImports imports, final String parameterOverride,
                             final String resultOverride) {
        final StringBuilder py = new StringBuilder();
        if (method.isStatic() && kind != Kind.FUNCTION) {
            py.append(indent).append("@staticmethod\n");
        }
        py.append(indent).append(method.hasAnnotation("async") ? "async def " : "def ").append(name(method));
        py.append(typeParameters(method, imports));
        py.append('(').append(parameterOverride != null ? parameterOverride
                : String.join(", ", parameters(method, kind, imports))).append(')');
        return py.append(" -> ").append(resultOverride != null ? resultOverride : result(method, imports))
                .append(':').toString();
    }

    private List<String> parameters(final MethodDefinition method, final Kind kind, final PyImports imports) {
        final List<String> rendered = new ArrayList<>();
        if (kind != Kind.FUNCTION && !method.isStatic()) {
            rendered.add("self");
        }
        for (final ParameterDefinition parameter : method.parameters()) {
            rendered.add((parameter.varargs() ? "*" : "") + PyNames.identifier(parameter.name()) + ": "
                    + annotation(context.type(parameter.type(), imports, PyContext.Mode.ANNOTATION), imports));
        }
        return rendered;
    }

    /** The annotation of the result: {@code None} for a void method, an async iterator for a stream. */
    private String result(final MethodDefinition method, final PyImports imports) {
        if (method.returnType() instanceof Type.VoidType) {
            return "None";
        }
        final String type = annotation(context.type(method.returnType(), imports, PyContext.Mode.ANNOTATION),
                imports);
        if (method.hasAnnotation("streaming")) {
            return imports.standard("collections.abc", "AsyncIterator") + "[" + type + "]";
        }
        return type;
    }

    /** PEP 695 type parameters of a generic method; a self type has no Python form and is dropped. */
    private String typeParameters(final MethodDefinition method, final PyImports imports) {
        final List<TypeParameterDefinition> parameters = method.typeParameters().stream()
                .filter(parameter -> !PyContext.isSelfParameter(parameter)).toList();
        if (parameters.isEmpty()) {
            return "";
        }
        final List<String> rendered = new ArrayList<>();
        for (final TypeParameterDefinition parameter : parameters) {
            final String name = context.parameterName(parameter.name());
            rendered.add(parameter.bound() == null ? name
                    : name + ": " + context.type(parameter.bound(), imports, PyContext.Mode.BOUND));
        }
        return "[" + String.join(", ", rendered) + "]";
    }

    private static String name(final MethodDefinition method) {
        return PyNames.identifier(method.name());
    }
}
