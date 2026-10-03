package org.hiero.sdk.v3.metalang.generator.java;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;

/**
 * Compiles generated Java modules in-process ({@code -Xlint:all -Werror}, module system) and loads the classes.
 */
final class GeneratedJava {

    /**
     * The result of a compilation.
     *
     * @param success     whether javac succeeded
     * @param diagnostics all javac diagnostics (warnings included)
     * @param classes     the directory with one sub-directory of class files per module
     */
    record Compilation(boolean success, List<String> diagnostics, Path classes) {

        /**
         * Loads the classes of all modules with a new class loader.
         *
         * @return the class loader
         * @throws IOException if the class directories cannot be read
         */
        ClassLoader classLoader() throws IOException {
            final List<URL> urls = new ArrayList<>();
            try (Stream<Path> modules = Files.list(classes)) {
                for (final Path module : modules.sorted().toList()) {
                    urls.add(module.toUri().toURL());
                }
            }
            return new URLClassLoader(urls.toArray(URL[]::new), GeneratedJava.class.getClassLoader());
        }
    }

    private GeneratedJava() {
    }

    /**
     * Writes the files below {@code directory/src} and compiles them to {@code directory/classes}.
     *
     * @param files     the generated files
     * @param directory a temporary directory
     * @return the compilation result
     * @throws IOException if the files cannot be written
     */
    static Compilation compile(final List<GeneratedFile> files, final Path directory) throws IOException {
        final List<Path> sources = new ArrayList<>();
        for (final GeneratedFile file : files) {
            final Path target = directory.resolve("src").resolve(file.path());
            Files.createDirectories(target.getParent());
            Files.writeString(target, file.content(), StandardCharsets.UTF_8);
            sources.add(target);
        }
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        final boolean success;
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, Locale.ROOT,
                StandardCharsets.UTF_8)) {
            final String jspecify = Stream.of(System.getProperty("java.class.path").split(File.pathSeparator))
                    .filter(p -> p.contains("jspecify")).findFirst().orElseThrow();
            success = compiler.getTask(null, fileManager, diagnostics, List.of(
                            "-Xlint:all", "-Werror",
                            "--module-source-path", directory.resolve("src") + "/*/src/main/java",
                            "--module-path", jspecify,
                            "-d", directory.resolve("classes").toString()),
                    null, fileManager.getJavaFileObjectsFromPaths(sources)).call();
        }
        return new Compilation(success, diagnostics.getDiagnostics().stream().map(Diagnostic::toString).toList(),
                directory.resolve("classes"));
    }
}
