package org.hiero.sdk.v3.codegen.render;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public record JavaSourceFile(
        Path relativePath,
        String sourcePath,
        String declarationIdentity,
        String packageName,
        List<String> imports,
        String body) {

    public JavaSourceFile {
        Objects.requireNonNull(relativePath, "relativePath");
        Objects.requireNonNull(sourcePath, "sourcePath");
        Objects.requireNonNull(declarationIdentity, "declarationIdentity");
        Objects.requireNonNull(packageName, "packageName");
        imports = List.copyOf(imports);
        Objects.requireNonNull(body, "body");
    }
}
