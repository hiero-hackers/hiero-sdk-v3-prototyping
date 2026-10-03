package org.hiero.sdk.v3.metalang.generator.ts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.model.ConstantDefinition;
import org.hiero.sdk.v3.metalang.model.FunctionDefinition;
import org.hiero.sdk.v3.metalang.model.NamespaceDefinition;
import org.hiero.sdk.v3.metalang.model.TypeDefinition;

/**
 * Generates the files of a namespace besides the types: {@code functions.ts} (the namespace-level functions),
 * {@code constants.ts}, {@code errors.ts} (the error classes placed in the namespace) and {@code index.ts}, which
 * re-exports everything and is the module of the namespace subpath of the package.
 */
final class TsNamespaceGenerator {

    private TsNamespaceGenerator() {
    }

    static Optional<GeneratedFile> functions(final String namespace, final TsContext context) {
        final List<FunctionDefinition> functions = context.functions(namespace);
        if (functions.isEmpty()) {
            return Optional.empty();
        }
        final String folder = context.folder(namespace);
        final String directory = TsNames.sourceDirectory(folder, namespace);
        final TsImports imports = new TsImports(context, folder, directory, functions.stream()
                .map(f -> f.method().name()).distinct().toArray(String[]::new));
        final String body = String.join("\n", TsMembers.render(namespace, functions.stream()
                .map(FunctionDefinition::method).toList(), TsMembers.Kind.FUNCTION, "", imports));
        return Optional.of(file(directory + "/functions.ts", imports, body));
    }

    static Optional<GeneratedFile> constants(final String namespace, final TsContext context) {
        final List<ConstantDefinition> constants = context.constants(namespace);
        if (constants.isEmpty()) {
            return Optional.empty();
        }
        final String folder = context.folder(namespace);
        final String directory = TsNames.sourceDirectory(folder, namespace);
        final TsImports imports = new TsImports(context, folder, directory, constants.stream()
                .map(c -> c.name().name()).toArray(String[]::new));
        final List<String> declarations = new ArrayList<>();
        for (final ConstantDefinition constant : constants) {
            declarations.add(TsDoc.render("", List.of(constant.documentation()), List.of(),
                    constant.hasAnnotation("deprecated")) + "export const " + constant.name().name() + ": "
                    + TsTypes.type(constant.type(), imports) + " = "
                    + TsLiterals.expression(constant.value(), constant.type(), imports) + ";\n");
        }
        return Optional.of(file(directory + "/constants.ts", imports, String.join("\n", declarations)));
    }

    static Optional<GeneratedFile> errors(final String namespace, final TsContext context) {
        final List<String> classes = new ArrayList<>();
        for (final Map.Entry<String, TsContext.ErrorClass> error : context.errors().entrySet()) {
            if (!namespace.equals(error.getValue().namespace())) {
                continue;
            }
            final String name = error.getValue().name();
            classes.add(TsDoc.render("", List.of("Thrown if " + words(error.getKey()) + " occurs.")) + "export class "
                    + name + " extends Error {\n\n"
                    + TsDoc.render("    ", List.of("Creates a new `" + name + "`."), List.of(
                    "@param message - the description of the problem",
                    "@param options - the cause of the problem, if any"), false)
                    + "    constructor(message: string, options?: ErrorOptions) {\n"
                    + "        super(message, options);\n"
                    + "        this.name = \"" + name + "\";\n"
                    + "    }\n}\n");
        }
        if (classes.isEmpty()) {
            return Optional.empty();
        }
        final String folder = context.folder(namespace);
        final String directory = TsNames.sourceDirectory(folder, namespace);
        return Optional.of(file(directory + "/errors.ts", new TsImports(context, folder, directory),
                String.join("\n", classes)));
    }

    /**
     * The index module of a namespace: re-exports the types, functions, constants and errors.
     *
     * @param namespace the namespace
     * @param modules   the file names (without extension) of the namespace
     * @param context   the generation context
     * @return the file
     */
    static GeneratedFile index(final NamespaceDefinition namespace, final List<String> modules,
                               final TsContext context) {
        final String directory = TsNames.sourceDirectory(context.folder(namespace.name()), namespace.name());
        final String description = namespace.sources().stream().map(NamespaceDefinition.Source::description)
                .filter(d -> !d.isBlank()).reduce((a, b) -> a + "\n\n" + b).orElse("");
        final StringBuilder ts = new StringBuilder(TsGenerator.HEADER).append('\n');
        ts.append(TsDoc.render("", List.of(description), List.of("@packageDocumentation"), false));
        modules.stream().sorted().forEach(m -> ts.append("export * from \"./").append(m).append(".js\";\n"));
        if (modules.isEmpty()) {
            ts.append("export {};\n");
        }
        return new GeneratedFile(directory + "/index.ts", ts.toString());
    }

    /** The files of a namespace that the index re-exports. */
    static List<String> modules(final String namespace, final List<TypeDefinition> types, final List<GeneratedFile> files,
                                final TsContext context) {
        final List<String> modules = new ArrayList<>();
        types.stream().filter(t -> context.isGenerated(t.name())).forEach(t -> modules.add(t.name().name()));
        final String directory = TsNames.sourceDirectory(context.folder(namespace), namespace) + "/";
        for (final GeneratedFile file : files) {
            for (final String stem : List.of("functions", "constants", "errors")) {
                if (file.path().equals(directory + stem + ".ts")) {
                    modules.add(stem);
                }
            }
        }
        return modules;
    }

    private static GeneratedFile file(final String path, final TsImports imports, final String body) {
        final StringBuilder ts = new StringBuilder(TsGenerator.HEADER).append('\n');
        final String importBlock = imports.render();
        if (!importBlock.isEmpty()) {
            ts.append(importBlock).append('\n');
        }
        return new GeneratedFile(path, ts.append(body).toString());
    }

    private static String words(final String errorId) {
        final String words = (errorId.endsWith("-error") ? errorId.substring(0, errorId.length() - 6) : errorId)
                .replace('-', ' ');
        return ("aeiou".indexOf(words.charAt(0)) >= 0 ? "an " : "a ") + words + " error";
    }
}
