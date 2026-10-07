package org.hiero.sdk.v3.metalang.generator.go;

import java.util.List;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.model.NamespaceDefinition;

/**
 * Generates the files of the Go module itself: {@code go.mod}, {@code .gitignore} and the {@code doc.go} that
 * carries a package's documentation.
 */
final class GoProjectGenerator {

    /** How wide a documentation comment is wrapped. */
    private static final int WIDTH = 110;

    private GoProjectGenerator() {
    }

    /**
     * The module files.
     *
     * @param config the configuration
     * @return {@code go.mod} and {@code .gitignore}
     */
    static List<GeneratedFile> module(final GoGeneratorConfig config) {
        final String gomod = "// " + GoGenerator.MARKER + "\n\n"
                + "module " + config.module() + "\n\n"
                + "go " + config.version() + "\n";
        final String gitignore = "# " + GoGenerator.MARKER + "\n"
                + "/bin/\n";
        return List.of(new GeneratedFile("go.mod", gomod), new GeneratedFile(".gitignore", gitignore));
    }

    /**
     * The {@code doc.go} of a namespace: the package clause and the package documentation, which is the
     * {@code ## Description} of the namespace's spec files.
     *
     * @param namespace the namespace
     * @param config    the configuration
     * @return the generated file
     */
    static GeneratedFile packageDoc(final NamespaceDefinition namespace, final GoGeneratorConfig config) {
        final String directory = GoNames.directory(namespace.name());
        final String name = directory.substring(directory.lastIndexOf('/') + 1);
        final StringBuilder go = new StringBuilder(GoGenerator.HEADER).append('\n');
        // a Go doc comment starts with the identifier it documents
        go.append(comment(name + " holds the API of the meta-language namespace " + namespace.name() + "."));
        namespace.sources().stream()
                .map(NamespaceDefinition.Source::description)
                .filter(description -> !description.isBlank())
                .forEach(description -> go.append("//\n").append(comment(description.strip())));
        go.append("package ").append(name).append('\n');
        return new GeneratedFile(directory + "/doc.go", go.toString());
    }

    /** Wraps text into {@code //} comment lines of at most {@link #WIDTH} characters. */
    private static String comment(final String text) {
        final StringBuilder out = new StringBuilder();
        for (final String paragraph : text.split("\n\n+")) {
            if (!out.isEmpty()) {
                out.append("//\n");
            }
            final StringBuilder line = new StringBuilder("//");
            for (final String word : paragraph.replace('\n', ' ').trim().split("\\s+")) {
                if (line.length() + 1 + word.length() > WIDTH) {
                    out.append(line).append('\n');
                    line.setLength(0);
                    line.append("//");
                }
                line.append(' ').append(word);
            }
            out.append(line).append('\n');
        }
        return out.toString();
    }
}
