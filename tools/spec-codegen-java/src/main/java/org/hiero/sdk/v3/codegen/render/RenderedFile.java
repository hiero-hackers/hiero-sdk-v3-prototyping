package org.hiero.sdk.v3.codegen.render;

import java.nio.file.Path;
import java.util.Objects;

public record RenderedFile(Path relativePath, byte[] content) {

    public RenderedFile {
        Objects.requireNonNull(relativePath, "relativePath");
        content = Objects.requireNonNull(content, "content").clone();
    }

    @Override
    public byte[] content() {
        return content.clone();
    }
}
