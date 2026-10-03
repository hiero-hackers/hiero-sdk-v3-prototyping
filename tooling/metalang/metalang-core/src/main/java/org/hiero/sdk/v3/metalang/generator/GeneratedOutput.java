package org.hiero.sdk.v3.metalang.generator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Writes generated files into an output directory that may be under version control: files are only rewritten if
 * their content changes, and generated files that the generator no longer produces are deleted, so that the
 * directory always reflects the current specs and diffs show exactly what changed. Directories that become empty
 * by these deletions are deleted as well. A file counts as generated if it
 * contains the generator's marker text in its first line (in any comment syntax, e.g. {@code // ...} or
 * {@code <!-- ... -->}); all other files (e.g. hand-written ones) are never touched.
 */
public final class GeneratedOutput {

    /**
     * What a write changed.
     *
     * @param written the number of files that were created or whose content changed
     * @param removed the generated files that were deleted because they are no longer generated
     */
    public record Result(int written, List<Path> removed) {

        /**
         * Creates a result.
         *
         * @param written the number of written files
         * @param removed the deleted files
         */
        public Result {
            removed = List.copyOf(removed);
        }
    }

    /** Directories with build output (Maven, TypeScript) or installed dependencies (npm). */
    private static final Set<String> BUILD_DIRECTORIES = Set.of("target", "dist", "node_modules");

    private GeneratedOutput() {
    }

    /**
     * Writes the files and deletes stale generated files and the directories that became empty.
     *
     * @param directory the output directory (created if missing)
     * @param files     the generated files with paths relative to {@code directory}
     * @param marker    the text that the first line of every generated file contains
     * @return what changed
     * @throws IOException if the directory cannot be read or written
     */
    public static Result write(final Path directory, final List<GeneratedFile> files, final String marker)
            throws IOException {
        Objects.requireNonNull(directory, "directory must not be null");
        Objects.requireNonNull(marker, "marker must not be null");
        final Path root = directory.toAbsolutePath().normalize();
        final Set<Path> targets = files.stream().map(f -> root.resolve(f.path()).normalize())
                .collect(Collectors.toSet());
        int written = 0;
        for (final GeneratedFile file : files) {
            final Path target = root.resolve(file.path()).normalize();
            if (!Files.exists(target) || !Files.readString(target, StandardCharsets.UTF_8).equals(file.content())) {
                Files.createDirectories(target.getParent());
                Files.writeString(target, file.content(), StandardCharsets.UTF_8);
                written++;
            }
        }
        final List<Path> removed = new ArrayList<>();
        try (Stream<Path> existing = Files.walk(root)) {
            for (final Path file : existing.filter(Files::isRegularFile).sorted().toList()) {
                if (!targets.contains(file) && !isBuildOutput(root.relativize(file)) && isGenerated(file, marker)) {
                    Files.delete(file);
                    removed.add(root.relativize(file));
                }
            }
        }
        // only the directories that became empty by the deletions, deepest first
        final Set<Path> parents = new java.util.TreeSet<>(Comparator.reverseOrder());
        for (final Path file : removed) {
            for (Path parent = root.resolve(file).getParent(); parent != null && !parent.equals(root);
                 parent = parent.getParent()) {
                parents.add(parent);
            }
        }
        for (final Path parent : parents) {
            try (Stream<Path> children = Files.list(parent)) {
                if (children.findAny().isEmpty()) {
                    Files.delete(parent);
                }
            }
        }
        return new Result(written, removed);
    }

    /** Build output and installed dependencies may contain copies of generated files; they are never removed. */
    private static boolean isBuildOutput(final Path relative) {
        for (final Path part : relative) {
            if (BUILD_DIRECTORIES.contains(part.toString())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isGenerated(final Path file, final String marker) {
        try (Stream<String> lines = Files.lines(file, StandardCharsets.UTF_8)) {
            return lines.findFirst().map(l -> l.contains(marker.strip())).orElse(false);
        } catch (final IOException | java.io.UncheckedIOException e) {
            return false; // not readable as UTF-8 text: certainly no generated source file
        }
    }
}
