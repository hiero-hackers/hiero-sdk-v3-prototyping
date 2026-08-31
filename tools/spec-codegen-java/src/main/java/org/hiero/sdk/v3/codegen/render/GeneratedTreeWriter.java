package org.hiero.sdk.v3.codegen.render;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;

public final class GeneratedTreeWriter {

    public void replace(Path outputRoot, Path stagingRoot, List<RenderedFile> files) throws IOException {
        var output = outputRoot.toAbsolutePath().normalize();
        var staging = stagingRoot.toAbsolutePath().normalize();
        if (output.equals(staging) || output.startsWith(staging) || staging.startsWith(output)) {
            throw new IllegalArgumentException("output and staging directories must be disjoint");
        }
        deleteTree(staging);
        Files.createDirectories(staging);
        try {
            for (var file : files) {
                var destination = staging.resolve(file.relativePath()).normalize();
                if (!destination.startsWith(staging)) {
                    throw new IllegalArgumentException("rendered path escapes staging directory: " + file.relativePath());
                }
                Files.createDirectories(destination.getParent());
                Files.write(destination, file.content());
            }
            replaceDirectory(output, staging);
        } catch (IOException | RuntimeException exception) {
            deleteTree(staging);
            throw exception;
        }
    }

    private static void replaceDirectory(Path output, Path staging) throws IOException {
        Files.createDirectories(output.getParent());
        var backup = output.resolveSibling(output.getFileName() + ".codegen-backup");
        deleteTree(backup);
        var hadOutput = Files.exists(output);
        try {
            if (hadOutput) {
                atomicMove(output, backup);
            }
            atomicMove(staging, output);
            deleteTree(backup);
        } catch (IOException exception) {
            if (!Files.exists(output) && Files.exists(backup)) {
                atomicMove(backup, output);
            }
            throw exception;
        }
    }

    private static void atomicMove(Path source, Path destination) throws IOException {
        try {
            Files.move(source, destination, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            throw new IOException("filesystem does not support atomic generated-tree replacement", exception);
        }
    }

    private static void deleteTree(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (var paths = Files.walk(root)) {
            for (var path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(path);
            }
        }
    }
}
