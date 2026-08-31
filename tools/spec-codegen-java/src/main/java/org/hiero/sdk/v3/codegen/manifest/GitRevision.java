package org.hiero.sdk.v3.codegen.manifest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class GitRevision {

    private GitRevision() {}

    public static String read(Path repositoryRoot) throws IOException {
        var git = repositoryRoot.resolve(".git");
        if (!Files.isDirectory(git)) {
            throw new IOException("repository Git directory is unavailable");
        }
        var head = Files.readString(git.resolve("HEAD")).strip();
        if (!head.startsWith("ref: ")) {
            return requireRevision(head);
        }
        var reference = head.substring(5);
        var loose = git.resolve(reference).normalize();
        if (loose.startsWith(git) && Files.isRegularFile(loose)) {
            return requireRevision(Files.readString(loose).strip());
        }
        var packedRefs = git.resolve("packed-refs");
        if (Files.isRegularFile(packedRefs)) {
            for (var line : Files.readAllLines(packedRefs)) {
                if (!line.startsWith("#") && line.endsWith(" " + reference)) {
                    return requireRevision(line.substring(0, line.indexOf(' ')));
                }
            }
        }
        throw new IOException("Git HEAD reference cannot be resolved: " + reference);
    }

    private static String requireRevision(String value) throws IOException {
        if (!value.matches("[0-9a-fA-F]{40,64}")) {
            throw new IOException("invalid Git revision");
        }
        return value.toLowerCase(java.util.Locale.ROOT);
    }
}
