package org.hiero.sdk.v3.codegen.manifest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import org.hiero.sdk.v3.codegen.model.MappingDecision;

public final class ManifestWriter {

    private static final Pattern SECRET = Pattern.compile(
            "(?i)(password|passwd|secret|api[_-]?key|private[_-]?key)\\s*[:=]\\s*\\S+");

    public String toJson(GenerationManifest manifest) {
        rejectSecrets(manifest);
        var result = new StringBuilder("{\n");
        field(result, 1, "sourceRevision", manifest.sourceRevision(), true);
        result.append("  \"generator\": {\n");
        field(result, 2, "groupId", manifest.generator().groupId(), true);
        field(result, 2, "artifactId", manifest.generator().artifactId(), true);
        field(result, 2, "version", manifest.generator().version(), false);
        result.append("  },\n");
        field(result, 1, "configurationSha256", manifest.configurationSha256(), true);
        result.append("  \"javaRelease\": ").append(manifest.javaRelease()).append(",\n");
        field(result, 1, "packageRoot", manifest.packageRoot(), true);
        fileDigests(result, "inputs", manifest.inputs(), true);
        mappings(result, manifest.mappings());
        retainedQuestions(result, manifest.retainedQuestions());
        deferred(result, manifest.deferredEnforcement());
        fileDigests(result, "outputs", manifest.outputs(), false);
        return result.append("}\n").toString();
    }

    private static void retainedQuestions(StringBuilder result, List<RetainedQuestion> values) {
        result.append("  \"retainedQuestions\": [");
        var sorted = values.stream().sorted(Comparator.comparing(RetainedQuestion::sourcePath)
                .thenComparing(RetainedQuestion::text)).toList();
        appendObjects(result, sorted.stream().map(value -> "{\"sourcePath\": " + quote(value.sourcePath())
                + ", \"text\": " + quote(value.text()) + "}").toList());
        result.append("],\n");
    }

    public void write(Path destination, GenerationManifest manifest) throws IOException {
        var absolute = destination.toAbsolutePath().normalize();
        Files.createDirectories(absolute.getParent());
        var staged = absolute.resolveSibling(absolute.getFileName() + ".staged");
        Files.writeString(staged, toJson(manifest), StandardCharsets.UTF_8);
        Files.move(staged, absolute, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }

    private static void fileDigests(StringBuilder result, String name, List<FileDigest> values, boolean comma) {
        result.append("  ").append(quote(name)).append(": [");
        var sorted = values.stream().sorted(Comparator.comparing(FileDigest::path)).toList();
        appendObjects(result, sorted.stream()
                .map(value -> "{\"path\": " + quote(value.path()) + ", \"sha256\": " + quote(value.sha256()) + "}")
                .toList());
        result.append("]").append(comma ? "," : "").append("\n");
    }

    private static void mappings(StringBuilder result, List<MappingDecision> values) {
        result.append("  \"mappings\": [");
        var sorted = values.stream().sorted(Comparator.comparing(MappingDecision::sourceElement)
                .thenComparing(MappingDecision::javaElement)).toList();
        appendObjects(result, sorted.stream().map(value -> "{\"sourceElement\": " + quote(value.sourceElement())
                + ", \"javaElement\": " + quote(value.javaElement())
                + ", \"mappingKind\": " + quote(value.mappingKind().name())
                + ", \"rationale\": " + quote(value.rationale())
                + ", \"requirementIds\": " + stringArray(value.requirementIds()) + "}").toList());
        result.append("],\n");
    }

    private static void deferred(StringBuilder result, List<DeferredEnforcement> values) {
        result.append("  \"deferredEnforcement\": [");
        var sorted = values.stream().sorted(Comparator.comparing(DeferredEnforcement::sourceElement)
                .thenComparing(DeferredEnforcement::decisionId)).toList();
        appendObjects(result, sorted.stream().map(value -> "{\"sourceElement\": " + quote(value.sourceElement())
                + ", \"decisionId\": " + quote(value.decisionId())
                + ", \"reason\": " + quote(value.reason())
                + ", \"enforcement\": " + quote(value.enforcement()) + "}").toList());
        result.append("],\n");
    }

    private static void appendObjects(StringBuilder result, List<String> objects) {
        if (!objects.isEmpty()) {
            result.append('\n');
            for (var index = 0; index < objects.size(); index++) {
                result.append("    ").append(objects.get(index));
                result.append(index + 1 < objects.size() ? ",\n" : "\n");
            }
            result.append("  ");
        }
    }

    private static String stringArray(List<String> values) {
        return values.stream().sorted().map(ManifestWriter::quote)
                .collect(java.util.stream.Collectors.joining(", ", "[", "]"));
    }

    private static void field(StringBuilder result, int indentation, String name, String value, boolean comma) {
        result.append("  ".repeat(indentation)).append(quote(name)).append(": ").append(quote(value));
        result.append(comma ? ",\n" : "\n");
    }

    private static String quote(String value) {
        var escaped = value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\b", "\\b").replace("\f", "\\f").replace("\n", "\\n")
                .replace("\r", "\\r").replace("\t", "\\t");
        return "\"" + escaped + "\"";
    }

    private static void rejectSecrets(GenerationManifest manifest) {
        manifest.deferredEnforcement().forEach(value -> {
            check(value.reason());
            check(value.enforcement());
        });
        manifest.retainedQuestions().forEach(value -> check(value.text()));
    }

    private static void check(String value) {
        if (SECRET.matcher(value).find()) {
            throw new IllegalArgumentException("manifest contains secret-like metadata");
        }
    }
}
