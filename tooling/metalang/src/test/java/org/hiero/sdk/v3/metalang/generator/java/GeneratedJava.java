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
     * @param testClasses the directory with the class files of the tests, {@code null} without tests
     */
    record Compilation(boolean success, List<String> diagnostics, Path classes, Path testClasses) {

        Compilation(final boolean success, final List<String> diagnostics, final Path classes) {
            this(success, diagnostics, classes, null);
        }

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
            if (testClasses != null) {
                urls.add(testClasses.toUri().toURL());
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
            if (file.path().endsWith(".java") && file.path().contains("/src/main/java/")) {
                sources.add(target); // the Maven files (pom.xml) and the tests are written, but not compiled
            }
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

    /**
     * The result of running the generated tests.
     *
     * @param tests    the number of executed tests
     * @param failures the failed tests: test class and method name with the exception
     */
    record TestRun(long tests, java.util.Map<String, Throwable> failures) {

        /**
         * The failures that are not caused by a method stub ("Not implemented yet").
         *
         * @return the failures by test
         */
        java.util.Map<String, Throwable> unexpected() {
            final java.util.Map<String, Throwable> result = new java.util.TreeMap<>();
            failures.forEach((test, error) -> {
                if (!isStub(error)) {
                    result.put(test, error);
                }
            });
            return result;
        }

        /**
         * The failures caused by a method stub.
         *
         * @return the names of the tests
         */
        java.util.SortedSet<String> stubs() {
            final java.util.SortedSet<String> result = new java.util.TreeSet<>(failures.keySet());
            result.removeAll(unexpected().keySet());
            return result;
        }

        private static boolean isStub(final Throwable error) {
            for (Throwable t = error; t != null; t = t.getCause()) {
                if (t instanceof UnsupportedOperationException && t.getMessage() != null
                        && t.getMessage().startsWith("Not implemented yet: ")) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Compiles the generated tests ({@code src/test/java}) against the compiled modules, on the class path like the
     * tests of a Maven build, with {@code -Xlint:all -Werror}.
     *
     * @param files       the generated files
     * @param compilation the compiled modules
     * @param directory   the working directory
     * @return the compilation of the tests (the class loader contains modules and tests)
     * @throws IOException if a file cannot be written
     */
    static Compilation compileTests(final List<GeneratedFile> files, final Compilation compilation,
                                    final Path directory) throws IOException {
        final List<Path> sources = new ArrayList<>();
        for (final GeneratedFile file : files) {
            if (file.path().endsWith(".java") && file.path().contains("/src/test/java/")) {
                sources.add(directory.resolve("src").resolve(file.path()));
            }
        }
        final List<String> classPath = new ArrayList<>(List.of(System.getProperty("java.class.path")
                .split(File.pathSeparator)));
        try (Stream<Path> modules = Files.list(compilation.classes())) {
            modules.sorted().forEach(m -> classPath.add(m.toString()));
        }
        final Path output = directory.resolve("test-classes");
        Files.createDirectories(output);
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        final boolean success;
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, Locale.ROOT,
                StandardCharsets.UTF_8)) {
            success = sources.isEmpty() || compiler.getTask(null, fileManager, diagnostics, List.of(
                            "-Xlint:all", "-Werror", "-proc:none",
                            "-classpath", String.join(File.pathSeparator, classPath),
                            "-d", output.toString()),
                    null, fileManager.getJavaFileObjectsFromPaths(sources)).call();
        }
        return new Compilation(success, diagnostics.getDiagnostics().stream().map(Diagnostic::toString).toList(),
                compilation.classes(), output);
    }

    /**
     * Runs the compiled tests with the JUnit platform.
     *
     * @param files the generated files (selects the test classes)
     * @param tests the compiled tests
     * @return the result
     * @throws Exception if a test class cannot be loaded
     */
    static TestRun runTests(final List<GeneratedFile> files, final Compilation tests) throws Exception {
        final ClassLoader loader = tests.classLoader();
        final List<org.junit.platform.engine.DiscoverySelector> selectors = new ArrayList<>();
        for (final GeneratedFile file : files) {
            final int start = file.path().indexOf("/src/test/java/");
            if (start >= 0 && file.path().endsWith(".java")) {
                final String name = file.path().substring(start + "/src/test/java/".length(),
                        file.path().length() - ".java".length()).replace('/', '.');
                selectors.add(org.junit.platform.engine.discovery.DiscoverySelectors
                        .selectClass(loader.loadClass(name)));
            }
        }
        final java.util.Map<String, Throwable> failures = new java.util.TreeMap<>();
        final java.util.concurrent.atomic.AtomicLong count = new java.util.concurrent.atomic.AtomicLong();
        final Thread thread = Thread.currentThread();
        final ClassLoader previous = thread.getContextClassLoader();
        thread.setContextClassLoader(loader);
        try {
            org.junit.platform.launcher.core.LauncherFactory.create().execute(
                    org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request().selectors(selectors)
                            .build(),
                    new org.junit.platform.launcher.TestExecutionListener() {
                        @Override
                        public void executionFinished(final org.junit.platform.launcher.TestIdentifier test,
                                                      final org.junit.platform.engine.TestExecutionResult result) {
                            if (!test.isTest()) {
                                return;
                            }
                            count.incrementAndGet();
                            result.getThrowable().ifPresent(error -> failures.put(test.getSource()
                                    .filter(org.junit.platform.engine.support.descriptor.MethodSource.class::isInstance)
                                    .map(org.junit.platform.engine.support.descriptor.MethodSource.class::cast)
                                    .map(m -> m.getClassName().substring(m.getClassName().lastIndexOf('.') + 1)
                                            + "." + m.getMethodName())
                                    .orElse(test.getDisplayName()), error));
                        }
                    });
        } finally {
            thread.setContextClassLoader(previous);
        }
        return new TestRun(count.get(), failures);
    }
}
