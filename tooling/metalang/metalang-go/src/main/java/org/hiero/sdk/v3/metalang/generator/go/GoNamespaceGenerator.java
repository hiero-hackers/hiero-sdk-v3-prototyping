package org.hiero.sdk.v3.metalang.generator.go;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Locale;
import java.util.Objects;
import java.util.TreeMap;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.model.FunctionDefinition;
import org.hiero.sdk.v3.metalang.model.MethodDefinition;
import org.hiero.sdk.v3.metalang.model.QualifiedName;
import org.hiero.sdk.v3.metalang.model.TypeDefinition;

/**
 * Generates the files of one namespace: one {@code .go} file per declared type, next to the {@code doc.go} that
 * carries the package documentation.
 */
final class GoNamespaceGenerator {

    private final GoContext context;

    /** The declarations that could not be generated, with the reason, keyed by qualified name. */
    private final TreeMap<QualifiedName, String> deferred = new TreeMap<>();

    /**
     * Creates the generator.
     *
     * @param context the generator context
     */
    GoNamespaceGenerator(final GoContext context) {
        this.context = Objects.requireNonNull(context, "context must not be null");
    }

    /**
     * Generates the type files of a namespace.
     *
     * @param namespace the namespace
     * @return the generated files
     */
    List<GeneratedFile> generate(final String namespace) {
        final String directory = GoNames.directory(namespace);
        final String self = context.config().importPath(namespace);
        final String packageName = directory.substring(directory.lastIndexOf('/') + 1);
        final GoTypeGenerator types = new GoTypeGenerator(context);
        final List<GeneratedFile> files = new ArrayList<>();
        for (final TypeDefinition definition : context.model().types(namespace)) {
            final String gap = context.gaps().get(definition.name());
            if (gap != null) {
                deferred.put(definition.name(), gap);
                continue;
            }
            final GoImports imports = new GoImports(self);
            final String body;
            try {
                body = types.generate(definition, imports);
            } catch (final GoGap caught) {
                // a gap the pre-pass did not see; it is reported the same way
                deferred.put(definition.name(), caught.getMessage());
                continue;
            }
            files.add(new GeneratedFile(directory + "/" + fileName(definition),
                    GoGenerator.HEADER + "\npackage " + packageName + "\n\n" + imports.render() + body));
        }
        functions(namespace, directory, packageName, self).ifPresent(files::add);
        return List.copyOf(files);
    }

    /**
     * The namespace functions of a package, in {@code functions.go}. A namespace is no type in Go, so its functions
     * are package-level functions - the form the guide prescribes, and the same one a factory type's methods take.
     */
    private Optional<GeneratedFile> functions(final String namespace, final String directory,
                                              final String packageName, final String self) {
        final List<MethodDefinition> declarations = context.model().functions().stream()
                .filter(function -> function.namespace().equals(namespace))
                .map(FunctionDefinition::method).toList();
        if (declarations.isEmpty()) {
            return Optional.empty();
        }
        final GoMembers members = new GoMembers(context);
        final GoImports imports = new GoImports(self);
        final StringBuilder go = new StringBuilder();
        final Map<MethodDefinition, String> names = members.names(declarations);
        for (final Map.Entry<MethodDefinition, String> function : names.entrySet()) {
            try {
                go.append(go.isEmpty() ? "" : "\n")
                        .append(members.render(function.getKey(), function.getValue(), null, packageName, imports));
            } catch (final GoGap gap) {
                deferred.put(new QualifiedName(namespace, function.getKey().name()), gap.getMessage());
            }
        }
        if (go.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new GeneratedFile(directory + "/functions.go",
                GoGenerator.HEADER + "\npackage " + packageName + "\n\n" + imports.render() + go));
    }

    /**
     * The declarations left out, with the reason each was left out.
     *
     * @return the deferred declarations, keyed by qualified name
     */
    TreeMap<QualifiedName, String> deferred() {
        return deferred;
    }

    /** {@code AccountId} becomes {@code account_id.go}, the file naming Go code uses. */
    private static String fileName(final TypeDefinition definition) {
        return String.join("_", GoNames.words(definition.name().name())).toLowerCase(Locale.ROOT) + ".go";
    }
}
