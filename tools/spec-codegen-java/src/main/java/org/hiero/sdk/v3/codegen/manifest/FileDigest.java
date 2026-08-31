package org.hiero.sdk.v3.codegen.manifest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import org.hiero.sdk.v3.codegen.model.SourceLocation;

public record FileDigest(String path, String sha256) {

    public FileDigest {
        path = SourceLocation.requireRepositoryRelative(path);
        if (!Objects.requireNonNull(sha256, "sha256").matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("sha256 must be a lowercase SHA-256 digest");
        }
    }

    public static FileDigest from(Path repositoryRoot, Path file) throws IOException {
        var root = repositoryRoot.toAbsolutePath().normalize();
        var absoluteFile = file.toAbsolutePath().normalize();
        if (!absoluteFile.startsWith(root)) {
            throw new IllegalArgumentException("file must be inside repository root");
        }
        var relative = root.relativize(absoluteFile).toString().replace('\\', '/');
        return new FileDigest(relative, Sha256.digest(Files.readAllBytes(absoluteFile)));
    }

    public static FileDigest fromBytes(String repositoryRelativePath, byte[] content) {
        return new FileDigest(repositoryRelativePath, Sha256.digest(content));
    }
}
