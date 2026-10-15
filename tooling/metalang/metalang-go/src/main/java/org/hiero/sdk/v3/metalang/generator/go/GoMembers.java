package org.hiero.sdk.v3.metalang.generator.go;

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

/**
 * Methods and functions in Go, following
 * {@code guidelines/api-best-practices-go.md} ("Methods, and the absence of overloading", "Errors",
 * "Asynchronous methods", "Streaming"):
 *
 * <ul>
 *   <li>a method that declares {@code @@throws} returns {@code (T, error)}, one that declares none returns only its
 *       value, and a {@code void} one returns {@code error} or nothing;</li>
 *   <li>{@code @@async} has no future type: the method blocks and takes {@code ctx context.Context} first;</li>
 *   <li>{@code @@streaming} returns {@code iter.Seq2[T, error]};</li>
 *   <li>Go has no overloading, so the first declaration of a name keeps it and every other one is suffixed with
 *       what distinguishes it, as {@code From<Parameters>}.</li>
 * </ul>
 *
 * <p>Bodies are not generated: a method panics with "Not implemented yet", the Go counterpart of the stubs the
 * other generators emit.
 */
final class GoMembers {

    private final GoContext context;

    /** The self type of the declaration being rendered: its variable name and the Go type that replaces it. */
    private String selfVariable;

    private String selfType;

    GoMembers(final GoContext context) {
        this.context = Objects.requireNonNull(context, "context must not be null");
    }

    /**
     * Binds the self type of the declaration whose members are rendered next. Go drops the self parameter, so a
     * member that names it bare - a setter returning {@code $$Self} - uses the declaring type instead.
     *
     * @param definition the declaration
     * @param rendered   the Go form of the declaring type ({@code "AccountID"}, {@code "Page[T]"})
     */
    void owner(final TypeDefinition definition, final String rendered) {
        selfVariable = definition.typeParameters().stream().filter(GoContext::isSelfParameter)
                .map(p -> p.variable().name()).findFirst().orElse(null);
        selfType = rendered;
    }

    /** The Go type of a member type, with the self type of the owner resolved. */
    private String render(final Type type, final GoImports imports) {
        if (type instanceof Type.TypeVariable variable && variable.name().equals(selfVariable)) {
            return selfType;
        }
        return context.type(type).render(imports);
    }

    /**
     * The Go names of the given methods, disambiguating an overload set: the first declaration keeps the bare name,
     * every other one is suffixed with the parameters that distinguish it.
     *
     * @param methods the methods of one type or namespace, in declaration order
     * @return the Go name of each method
     */
    Map<MethodDefinition, String> names(final List<MethodDefinition> methods) {
        final Map<MethodDefinition, String> names = new LinkedHashMap<>();
        final Set<String> taken = new HashSet<>();
        final Map<String, Integer> seen = new LinkedHashMap<>();
        for (final MethodDefinition method : methods) {
            final String base = GoNames.exported(method.name());
            final int count = seen.merge(method.name(), 1, Integer::sum);
            names.put(method, unique(count == 1 ? base : base + "From" + distinguishing(method), taken));
        }
        return names;
    }

    /** The parameters of an overload as a name part: {@code (container, value)} becomes {@code ContainerValue}. */
    private static String distinguishing(final MethodDefinition method) {
        final StringBuilder text = new StringBuilder();
        for (final ParameterDefinition parameter : method.parameters()) {
            text.append(GoNames.exported(parameter.name()));
        }
        return text.isEmpty() ? "NoArguments" : text.toString();
    }

    /**
     * Renders a method of a struct or a package-level function.
     *
     * @param method   the method
     * @param name     its Go name, from {@link #names(List)}
     * @param receiver the receiver ({@code "a AccountID"}), or {@code null} for a package-level function
     * @param owner    the type or namespace the method belongs to, named in the panic message
     * @param imports  the imports of the file
     * @return the Go code
     * @throws GoGap if the method has no Go form
     */
    String render(final MethodDefinition method, final String name, final String receiver, final String owner,
                  final GoImports imports) {
        return render(method, name, receiver, owner, null, imports);
    }

    /**
     * Renders a package-level function that takes the instance first, the form a generic method of an abstraction
     * takes because Go forbids type parameters on an interface method.
     *
     * @param method   the method
     * @param name     its Go name
     * @param owner    the abstraction, named in the panic message
     * @param instance the leading parameter ({@code "o Obj"})
     * @param imports  the imports of the file
     * @return the Go code
     * @throws GoGap if the method has no Go form
     */
    String function(final MethodDefinition method, final String name, final String owner, final String instance,
                    final GoImports imports) {
        return render(method, name, null, owner, instance, imports);
    }

    private String render(final MethodDefinition method, final String name, final String receiver,
                          final String owner, final String instance, final GoImports imports) {
        final StringBuilder go = new StringBuilder(GoDoc.of(name, method.documentation(), method));
        go.append("func ");
        if (receiver != null) {
            go.append('(').append(receiver).append(") ");
        }
        go.append(name).append(context.typeParameters(method.typeParameters(), imports));
        final List<String> parameters = new ArrayList<>();
        if (instance != null) {
            parameters.add(instance);
        }
        parameters.addAll(signature(method, imports));
        go.append('(').append(String.join(", ", parameters)).append(')');
        final String results = results(method, imports);
        go.append(results.isEmpty() ? "" : " " + results);
        go.append(" {\n\tpanic(\"Not implemented yet: ").append(owner).append('.').append(name).append("\")\n}\n");
        return go.toString();
    }

    /**
     * Renders the declaration of a method inside an interface: no receiver, no body, and no type parameters, which
     * Go forbids there.
     *
     * @param method  the method
     * @param name    its Go name
     * @param imports the imports of the file
     * @return the indented declaration
     * @throws GoGap if the method is generic
     */
    String declaration(final MethodDefinition method, final String name, final GoImports imports) {
        if (!method.typeParameters().isEmpty()) {
            throw new GoGap("the generic method '" + method.name() + "' of an abstraction has no Go form yet; Go "
                    + "forbids type parameters on an interface method, so it has to become a package-level function");
        }
        final String results = results(method, imports);
        return GoDoc.of(name, method.documentation(), method).indent(0).replace("//", "\t//")
                + "\t" + name + "(" + String.join(", ", signature(method, imports)) + ")"
                + (results.isEmpty() ? "" : " " + results) + "\n";
    }

    /** The parameter list, with the context of an {@code @@async} method first. */
    private List<String> signature(final MethodDefinition method, final GoImports imports) {
        final List<String> rendered = new ArrayList<>();
        final Set<String> taken = new HashSet<>();
        if (method.hasAnnotation("async")) {
            // @@async has no future type in Go: the method blocks, cancellation travels in the context
            rendered.add("ctx " + imports.qualify("context", "Context"));
            taken.add("ctx");
        }
        for (final ParameterDefinition parameter : method.parameters()) {
            rendered.add(unique(GoNames.unexported(parameter.name()), taken) + " "
                    + (parameter.varargs() ? "..." : "") + render(parameter.type(), imports));
        }
        return rendered;
    }

    /** The results: the value, plus {@code error} when the method declares {@code @@throws}. */
    private String results(final MethodDefinition method, final GoImports imports) {
        if (method.hasAnnotation("streaming")) {
            // a stream carries an error per item, so it never adds a second result
            final GoType item = context.type(method.returnType());
            return item instanceof GoType.StreamResult ? item.render(imports)
                    : imports.qualify("iter", "Seq2") + "[" + item.render(imports) + ", error]";
        }
        final boolean fallible = method.hasAnnotation("throws");
        if (context.type(method.returnType()) instanceof GoType.Void) {
            return fallible ? "error" : "";
        }
        final String result = render(method.returnType(), imports);
        return fallible ? "(" + result + ", error)" : result;
    }

    private static String unique(final String name, final Set<String> taken) {
        String candidate = name;
        for (int i = 2; !taken.add(candidate); i++) {
            candidate = name + i;
        }
        return candidate;
    }
}
