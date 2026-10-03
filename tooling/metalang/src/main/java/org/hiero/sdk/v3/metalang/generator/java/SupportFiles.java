package org.hiero.sdk.v3.metalang.generator.java;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;

/**
 * The Java support files of the SDK: classes that the generated API uses but that the specs do not declare
 * ({@code @ThreadSafe}, {@code HieroStream}, {@code StreamItem}, ...). Their single source are the files of the Java
 * guideline ({@code guidelines/java-files}); the build copies them into the tool, and the generator writes them 1:1
 * into the generated code, only preceded by the generator's header line.
 */
final class SupportFiles {

    /** The package of the streaming support ({@code @@streaming}, {@code streamResult<T>}). */
    static final String STREAMING_PACKAGE = "org.hiero.sdk.common";

    /** The streaming support files, by simple class name. */
    static final List<String> STREAMING = List.of("HieroPublisher", "HieroStream", "HieroSubscription", "StreamItem");

    private static final Pattern PACKAGE = Pattern.compile("(?m)^package ([\\w.]+);");

    private SupportFiles() {
    }

    /**
     * Returns the content of a support file as it is in the guideline.
     *
     * @param name the simple class name, e.g. {@code HieroStream}
     * @return the source code
     * @throws IllegalStateException if the file is not part of the tool (broken build)
     */
    static String source(final String name) {
        try (InputStream in = SupportFiles.class.getResourceAsStream("support/" + name + ".java")) {
            if (in == null) {
                throw new IllegalStateException("Support file " + name + ".java is missing; it is copied from "
                        + "guidelines/java-files by the build");
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Generates a support file into a module: the guideline file preceded by the generator's header line.
     *
     * @param module the Java module
     * @param name   the simple class name
     * @return the generated file
     */
    static GeneratedFile generate(final String module, final String name) {
        final String source = source(name);
        return new GeneratedFile(JavaNames.sourceRoot(module) + "/" + packageOf(source).replace('.', '/') + "/" + name
                + ".java", JavaGenerator.HEADER + "\n" + source);
    }

    /**
     * Returns the package of a support file.
     *
     * @param source the source code
     * @return the package name
     */
    static String packageOf(final String source) {
        final Matcher matcher = PACKAGE.matcher(Objects.requireNonNull(source, "source must not be null"));
        if (!matcher.find()) {
            throw new IllegalStateException("Support file without package declaration");
        }
        return matcher.group(1);
    }
}
