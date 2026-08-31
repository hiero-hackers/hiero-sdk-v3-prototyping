package org.hiero.sdk.v3.codegen.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.hiero.sdk.v3.codegen.manifest.GeneratorIdentity;
import org.hiero.sdk.v3.codegen.manifest.Sha256;
import org.hiero.sdk.v3.codegen.model.SourceLocation;

public record CodegenConfiguration(
        int schemaVersion,
        GeneratorIdentity generator,
        int javaRelease,
        String moduleName,
        String packageRoot,
        List<String> specifications,
        List<String> guidance,
        List<String> decisions,
        String sourceDirectory,
        String manifestPath,
        String stagingDirectory,
        String generatedHeader,
        String configurationSha256) {

    private static final Set<String> ROOT_KEYS = Set.of("schemaVersion", "generator", "java", "inputs", "output");
    private static final Set<String> SCALAR_KEYS = Set.of(
            "schemaVersion", "generator.groupId", "generator.artifactId", "generator.version",
            "java.release", "java.moduleName", "java.packageRoot", "output.sourceDirectory",
            "output.manifest", "output.stagingDirectory", "output.encoding", "output.lineEnding",
            "output.terminalNewline", "output.ordering", "output.generatedHeader");
    private static final Set<String> LIST_KEYS = Set.of(
            "inputs.specifications", "inputs.guidance", "inputs.decisions");

    public CodegenConfiguration {
        Objects.requireNonNull(generator, "generator");
        specifications = List.copyOf(specifications);
        guidance = List.copyOf(guidance);
        decisions = List.copyOf(decisions);
    }

    public static CodegenConfiguration load(Path repositoryRoot, Path configurationFile) throws IOException {
        var root = repositoryRoot.toAbsolutePath().normalize();
        var file = configurationFile.isAbsolute()
                ? configurationFile.toAbsolutePath().normalize()
                : root.resolve(configurationFile).normalize();
        requireInside(root, file, "configuration file");
        var bytes = Files.readAllBytes(file);
        var parsed = parse(new String(bytes, StandardCharsets.UTF_8));
        var configuration = new CodegenConfiguration(
                positiveInt(parsed.scalar("schemaVersion"), "schemaVersion"),
                new GeneratorIdentity(
                        parsed.scalar("generator.groupId"),
                        parsed.scalar("generator.artifactId"),
                        parsed.scalar("generator.version")),
                positiveInt(parsed.scalar("java.release"), "java.release"),
                parsed.scalar("java.moduleName"),
                parsed.scalar("java.packageRoot"),
                paths(parsed.list("inputs.specifications")),
                paths(parsed.list("inputs.guidance")),
                paths(parsed.list("inputs.decisions")),
                path(parsed.scalar("output.sourceDirectory")),
                path(parsed.scalar("output.manifest")),
                path(parsed.scalar("output.stagingDirectory")),
                parsed.scalar("output.generatedHeader"),
                Sha256.digest(bytes));
        configuration.validateFixedOutputSettings(parsed);
        configuration.validateInputs(root);
        return configuration;
    }

    public List<String> allInputs() {
        var inputs = new ArrayList<String>();
        inputs.addAll(specifications);
        inputs.addAll(guidance);
        inputs.addAll(decisions);
        return List.copyOf(inputs);
    }

    private void validateFixedOutputSettings(ParsedYaml parsed) {
        requireValue(schemaVersion == 1, "unsupported schemaVersion: " + schemaVersion);
        requireValue("UTF-8".equals(parsed.scalar("output.encoding")), "output.encoding must be UTF-8");
        requireValue("LF".equals(parsed.scalar("output.lineEnding")), "output.lineEnding must be LF");
        requireValue("true".equals(parsed.scalar("output.terminalNewline")), "output.terminalNewline must be true");
        requireValue("canonical".equals(parsed.scalar("output.ordering")), "output.ordering must be canonical");
        requireValue(generatedHeader.contains("{sourcePath}") && generatedHeader.contains("{declaration}"),
                "output.generatedHeader must contain sourcePath and declaration placeholders");
    }

    private void validateInputs(Path root) {
        requireValue(!specifications.isEmpty(), "at least one specification input is required");
        for (var input : allInputs()) {
            var resolved = root.resolve(input).normalize();
            requireInside(root, resolved, "input");
            requireValue(Files.isRegularFile(resolved), "configured input does not exist: " + input);
        }
    }

    private static ParsedYaml parse(String source) {
        var scalars = new HashMap<String, String>();
        var lists = new HashMap<String, List<String>>();
        var rootKeys = new HashSet<String>();
        String section = null;
        String list = null;
        var lines = source.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
        for (var index = 0; index < lines.length; index++) {
            var raw = lines[index];
            if (raw.isBlank() || raw.stripLeading().startsWith("#")) {
                continue;
            }
            if (raw.indexOf('\t') >= 0) {
                throw yamlError(index, "tabs are not supported");
            }
            var indentation = raw.length() - raw.stripLeading().length();
            var text = raw.strip();
            if (indentation == 0) {
                list = null;
                var entry = splitEntry(text, index);
                if (!ROOT_KEYS.contains(entry.key()) || !rootKeys.add(entry.key())) {
                    throw yamlError(index, "unknown or duplicate root key '" + entry.key() + "'");
                }
                if (entry.value() == null) {
                    section = entry.key();
                } else {
                    section = null;
                    putScalar(scalars, entry.key(), entry.value(), index);
                }
            } else if (indentation == 2 && section != null) {
                var entry = splitEntry(text, index);
                var key = section + "." + entry.key();
                if (entry.value() == null) {
                    if (!LIST_KEYS.contains(key) || lists.putIfAbsent(key, new ArrayList<>()) != null) {
                        throw yamlError(index, "unknown or duplicate list key '" + key + "'");
                    }
                    list = key;
                } else {
                    list = null;
                    putScalar(scalars, key, entry.value(), index);
                }
            } else if (indentation == 4 && list != null && text.startsWith("- ")) {
                var value = text.substring(2).strip();
                if (value.isEmpty()) {
                    throw yamlError(index, "list value must not be empty");
                }
                lists.get(list).add(value);
            } else {
                throw yamlError(index, "unsupported indentation or YAML construct");
            }
        }
        if (!scalars.keySet().equals(SCALAR_KEYS) || !lists.keySet().equals(LIST_KEYS)) {
            var missingScalars = new HashSet<>(SCALAR_KEYS);
            missingScalars.removeAll(scalars.keySet());
            var missingLists = new HashSet<>(LIST_KEYS);
            missingLists.removeAll(lists.keySet());
            throw new IllegalArgumentException(
                    "configuration is missing required keys: " + missingScalars + missingLists);
        }
        return new ParsedYaml(Map.copyOf(scalars), lists.entrySet().stream()
                .collect(java.util.stream.Collectors.toUnmodifiableMap(Map.Entry::getKey, entry -> List.copyOf(entry.getValue()))));
    }

    private static Entry splitEntry(String text, int lineIndex) {
        var separator = text.indexOf(':');
        if (separator < 1) {
            throw yamlError(lineIndex, "expected key/value entry");
        }
        var key = text.substring(0, separator).strip();
        var value = text.substring(separator + 1).strip();
        return new Entry(key, value.isEmpty() ? null : unquote(value));
    }

    private static String unquote(String value) {
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1).replace("\\\"", "\"").replace("\\\\", "\\");
        }
        return value;
    }

    private static void putScalar(Map<String, String> scalars, String key, String value, int lineIndex) {
        if (!SCALAR_KEYS.contains(key) || scalars.putIfAbsent(key, value) != null) {
            throw yamlError(lineIndex, "unknown or duplicate scalar key '" + key + "'");
        }
    }

    private static List<String> paths(List<String> values) {
        return values.stream().map(CodegenConfiguration::path).toList();
    }

    private static String path(String value) {
        return SourceLocation.requireRepositoryRelative(value);
    }

    private static int positiveInt(String value, String name) {
        try {
            var parsed = Integer.parseInt(value);
            requireValue(parsed > 0, name + " must be positive");
            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(name + " must be an integer", exception);
        }
    }

    private static void requireInside(Path root, Path path, String kind) {
        requireValue(path.startsWith(root), kind + " must be inside repository root");
    }

    private static void requireValue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    private static IllegalArgumentException yamlError(int lineIndex, String message) {
        return new IllegalArgumentException("configuration line " + (lineIndex + 1) + ": " + message);
    }

    private record Entry(String key, String value) {}

    private record ParsedYaml(Map<String, String> scalars, Map<String, List<String>> lists) {
        private String scalar(String key) {
            return scalars.get(key);
        }

        private List<String> list(String key) {
            return lists.get(key);
        }
    }
}
