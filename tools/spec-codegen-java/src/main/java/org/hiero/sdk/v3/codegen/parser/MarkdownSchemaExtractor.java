package org.hiero.sdk.v3.codegen.parser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.regex.Pattern;
import org.hiero.sdk.v3.codegen.model.SchemaException;
import org.hiero.sdk.v3.codegen.model.SourceLocation;

public final class MarkdownSchemaExtractor {

    private static final Pattern NAMESPACE = Pattern.compile("(?m)^\\s*namespace\\s+[A-Za-z][A-Za-z0-9_.]*\\s*$");

    public ExtractedSchema extract(Path repositoryRoot, Path sourceFile) throws IOException {
        var root = repositoryRoot.toRealPath();
        var source = sourceFile.toRealPath();
        if (!source.startsWith(root) || !Files.isRegularFile(source)) {
            throw new IllegalArgumentException("source file must be inside the repository root");
        }
        var relative = root.relativize(source).toString().replace('\\', '/');
        return extract(relative, Files.readString(source, StandardCharsets.UTF_8));
    }

    public ExtractedSchema extract(String sourcePath, String markdown) {
        var path = SourceLocation.requireRepositoryRelative(sourcePath);
        var normalized = markdown.replace("\r\n", "\n").replace('\r', '\n');
        var lines = normalized.split("\n", -1);
        var candidates = new ArrayList<ExtractedSchema>();
        var insideFence = false;
        var openingLine = 0;
        var body = new StringBuilder();

        for (var index = 0; index < lines.length; index++) {
            var line = lines[index];
            if (line.stripLeading().startsWith("```")) {
                if (!insideFence) {
                    insideFence = true;
                    openingLine = index + 1;
                    body.setLength(0);
                } else {
                    addCandidate(path, openingLine, body.toString(), candidates);
                    insideFence = false;
                }
                continue;
            }
            if (insideFence) {
                body.append(line).append('\n');
            }
        }

        if (insideFence && NAMESPACE.matcher(body).find()) {
            var namespaceLine = openingLine + firstNamespaceOffset(body.toString());
            throw new SchemaException(
                    "SCHEMA_FENCE_UNCLOSED",
                    new SourceLocation(path, namespaceLine, 1),
                    "schema fence is not closed");
        }
        if (candidates.isEmpty()) {
            throw new SchemaException(
                    "SCHEMA_FENCE_MISSING",
                    new SourceLocation(path, 1, 1),
                    "expected exactly one fenced block containing a namespace declaration");
        }
        return candidates.getFirst();
    }

    private static void addCandidate(
            String path, int openingLine, String content, ArrayList<ExtractedSchema> candidates) {
        if (!NAMESPACE.matcher(content).find()) {
            return;
        }
        var startLine = openingLine + 1;
        if (!candidates.isEmpty()) {
            throw new SchemaException(
                    "SCHEMA_FENCE_DUPLICATE",
                    new SourceLocation(path, startLine + firstNamespaceOffset(content) - 1, 1),
                    "multiple fenced blocks contain namespace declarations");
        }
        candidates.add(new ExtractedSchema(path, content, startLine));
    }

    private static int firstNamespaceOffset(String content) {
        var matcher = NAMESPACE.matcher(content);
        if (!matcher.find()) {
            return 1;
        }
        var offset = 1;
        for (var index = 0; index < matcher.start(); index++) {
            if (content.charAt(index) == '\n') {
                offset++;
            }
        }
        return offset;
    }
}
