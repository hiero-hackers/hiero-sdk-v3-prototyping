package org.hiero.sdk.v3.metalang.generator.ts;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.hiero.sdk.v3.metalang.model.MethodDefinition;
import org.hiero.sdk.v3.metalang.model.Type;

/**
 * Renders methods and namespace-level functions. Overloads (same name, other parameters) become TypeScript overload
 * signatures with one implementation; the implementation of a method is a stub that throws until a human implements
 * it.
 */
final class TsMembers {

    /** Where the methods are declared. */
    enum Kind {
        /** Members of an interface: signatures only. */
        INTERFACE,
        /** Methods of a class (static ones get {@code static}). */
        CLASS,
        /** Exported functions of a module or of the companion namespace of an interface. */
        FUNCTION
    }

    private TsMembers() {
    }

    /**
     * Renders the methods, grouped by name.
     *
     * @param owner   the name used in the stub message ({@code Type.method}), e.g. the type or {@code ledger}
     * @param methods the methods
     * @param kind    where they are declared
     * @param indent  the indentation
     * @param imports the imports of the file
     * @return the members, separated by blank lines
     */
    static List<String> render(final String owner, final List<MethodDefinition> methods, final Kind kind,
                               final String indent, final TsImports imports) {
        final Map<String, List<MethodDefinition>> groups = new LinkedHashMap<>();
        for (final MethodDefinition method : methods) {
            groups.computeIfAbsent((method.isStatic() && kind == Kind.CLASS ? "static " : "") + method.name(),
                    k -> new ArrayList<>()).add(method);
        }
        final List<String> result = new ArrayList<>();
        groups.forEach((key, group) -> {
            final StringBuilder out = new StringBuilder();
            final String prefix = switch (kind) {
                case INTERFACE -> "";
                case CLASS -> group.getFirst().isStatic() ? "static " : "";
                case FUNCTION -> "export function ";
            };
            final String stub = indent + "    throw new Error(\"Not implemented yet: " + owner + "."
                    + group.getFirst().name() + "\");\n";
            if (kind == Kind.INTERFACE) {
                group.forEach(m -> out.append(doc(m, indent, imports)).append(indent)
                        .append(signature(m, imports)).append(";\n"));
            } else if (group.size() == 1) {
                out.append(doc(group.getFirst(), indent, imports)).append(indent).append(prefix)
                        .append(signature(group.getFirst(), imports)).append(" {\n").append(stub)
                        .append(indent).append("}\n");
            } else {
                group.forEach(m -> out.append(doc(m, indent, imports)).append(indent).append(prefix)
                        .append(signature(m, imports)).append(";\n"));
                // one implementation for all overloads; humans dispatch on the arguments
                out.append(indent).append(prefix).append(group.getFirst().name())
                        .append("(...args: any[]): any {\n").append(stub).append(indent).append("}\n");
            }
            result.add(out.toString());
        });
        return result;
    }

    /**
     * The signature of a method: name, type parameters, parameters and result type.
     *
     * @param method  the method
     * @param imports the imports of the file
     * @return the signature, e.g. {@code sign(payer: Account, nodes: ReadonlyArray<AccountId>): PackedTransaction<R>}
     */
    static String signature(final MethodDefinition method, final TsImports imports) {
        return method.name() + TsTypes.typeParameters(method.typeParameters(), imports) + "("
                + TsTypes.parameters(method.parameters(), imports) + "): " + returnType(method, imports);
    }

    /**
     * The result type: {@code Promise<T>} for {@code @@async}, {@code AsyncIterable<T>} for {@code @@streaming},
     * {@code T | null} for {@code @@nullable}.
     *
     * @param method  the method
     * @param imports the imports of the file
     * @return the type
     */
    static String returnType(final MethodDefinition method, final TsImports imports) {
        final boolean nullable = method.hasAnnotation("nullable");
        if (method.hasAnnotation("streaming")) {
            return "AsyncIterable<" + TsTypes.type(method.returnType(), imports) + ">";
        }
        if (method.hasAnnotation("async")) {
            return "Promise<" + (method.returnType() instanceof Type.VoidType ? "void"
                    : TsTypes.declaration(method.returnType(), nullable, imports)) + ">";
        }
        return TsTypes.declaration(method.returnType(), nullable, imports);
    }

    private static String doc(final MethodDefinition method, final String indent, final TsImports imports) {
        final List<String> paragraphs = new ArrayList<>(List.of(method.documentation()));
        final List<String> tags = new ArrayList<>();
        final List<String> errors = method.annotation("throws").stream().flatMap(t -> t.arguments().stream())
                .map(a -> a.text()).distinct().toList();
        final List<String> names = errors.stream().map(id -> imports.context().error(id).name()).distinct()
                .map(n -> "`" + n + "`").toList();
        if (method.hasAnnotation("streaming") && !names.isEmpty()) {
            paragraphs.add("The iteration ends with " + String.join(" or ", names) + " if it fails.");
        } else if (method.hasAnnotation("async") && !names.isEmpty()) {
            paragraphs.add("The returned promise rejects with " + String.join(" or ", names)
                    + " if the operation fails.");
        } else {
            errors.forEach(id -> tags.add("@throws " + imports.context().error(id).name() + " if " + anError(id)
                    + " occurs"));
        }
        return TsDoc.render(indent, paragraphs, tags, method.hasAnnotation("deprecated"));
    }

    private static String anError(final String errorId) {
        final String words = (errorId.endsWith("-error") ? errorId.substring(0, errorId.length() - 6) : errorId)
                .replace('-', ' ');
        return ("aeiou".indexOf(words.charAt(0)) >= 0 ? "an " : "a ") + words + " error";
    }
}
