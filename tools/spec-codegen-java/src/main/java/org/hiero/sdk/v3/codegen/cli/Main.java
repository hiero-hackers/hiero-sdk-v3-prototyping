package org.hiero.sdk.v3.codegen.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.hiero.sdk.v3.codegen.config.CodegenConfiguration;
import org.hiero.sdk.v3.codegen.manifest.FileDigest;
import org.hiero.sdk.v3.codegen.manifest.GenerationManifest;
import org.hiero.sdk.v3.codegen.manifest.GitRevision;
import org.hiero.sdk.v3.codegen.manifest.ManifestWriter;
import org.hiero.sdk.v3.codegen.manifest.RetainedQuestion;
import org.hiero.sdk.v3.codegen.mapping.BaseApiMapper;
import org.hiero.sdk.v3.codegen.model.ResolvedSchema;
import org.hiero.sdk.v3.codegen.model.SchemaElements;
import org.hiero.sdk.v3.codegen.model.SchemaException;
import org.hiero.sdk.v3.codegen.parser.MarkdownSchemaExtractor;
import org.hiero.sdk.v3.codegen.parser.SchemaParser;
import org.hiero.sdk.v3.codegen.render.GeneratedTreeWriter;
import org.hiero.sdk.v3.codegen.render.JavaRenderer;
import org.hiero.sdk.v3.codegen.validation.SchemaValidator;

public final class Main {

    private Main() {}

    public static void main(String[] args) {
        var status = run(args, System.out, System.err);
        if (status != 0) {
            System.exit(status);
        }
    }

    static int run(String[] args, PrintStream out, PrintStream err) {
        try {
            var options = Options.parse(args);
            var configuration = CodegenConfiguration.load(options.repositoryRoot(), options.configurationFile());
            var schema = loadSchema(options.repositoryRoot(), configuration);
            switch (options.command()) {
                case "validate" -> out.println("Validated " + schema.documents().size() + " specification namespaces.");
                case "inventory" -> SchemaElements.identities(schema).stream().sorted().forEach(out::println);
                case "generate" -> generate(options, configuration, schema, out);
                default -> throw new IllegalArgumentException("unknown command: " + options.command());
            }
            return 0;
        } catch (SchemaException exception) {
            err.println(exception.getMessage());
            return 2;
        } catch (IllegalArgumentException | IllegalStateException | IOException exception) {
            err.println(exception.getMessage());
            return 2;
        }
    }

    private static void generate(
            Options options, CodegenConfiguration configuration, ResolvedSchema schema, PrintStream out)
            throws IOException {
        var repositoryRoot = options.repositoryRoot();
        var generation = new BaseApiMapper().map(schema, configuration);
        new SchemaValidator().validateMappings(schema, generation.mappings());
        var rendered = new JavaRenderer(configuration.generatedHeader()).render(generation.sourceFiles());

        var inputs = new ArrayList<FileDigest>();
        for (var input : configuration.allInputs().stream().sorted().toList()) {
            inputs.add(FileDigest.from(repositoryRoot, repositoryRoot.resolve(input)));
        }
        var outputs = rendered.stream()
                .map(file -> FileDigest.fromBytes(
                        configuration.sourceDirectory() + "/" + file.relativePath().toString().replace('\\', '/'),
                        file.content()))
                .toList();
        var manifest = new GenerationManifest(
                GitRevision.read(repositoryRoot),
                configuration.generator(),
                configuration.configurationSha256(),
                configuration.javaRelease(),
                configuration.packageRoot(),
                inputs,
                generation.mappings(),
                retainedQuestions(repositoryRoot, configuration),
                generation.deferredEnforcement(),
                outputs);
        var manifestWriter = new ManifestWriter();
        manifestWriter.toJson(manifest);

        new GeneratedTreeWriter().replace(
                options.physicalSourceRoot() == null
                        ? repositoryRoot.resolve(configuration.sourceDirectory())
                        : options.physicalSourceRoot(),
                options.physicalStagingRoot() == null
                        ? repositoryRoot.resolve(configuration.stagingDirectory())
                        : options.physicalStagingRoot(),
                rendered);
        manifestWriter.write(
                options.physicalManifest() == null
                        ? repositoryRoot.resolve(configuration.manifestPath())
                        : options.physicalManifest(),
                manifest);
        out.println("Generated " + rendered.size() + " Java source files.");
    }

    private static List<RetainedQuestion> retainedQuestions(
            Path repositoryRoot, CodegenConfiguration configuration) throws IOException {
        var questions = new ArrayList<RetainedQuestion>();
        for (var sourcePath : configuration.specifications()) {
            var inQuestions = false;
            for (var line : java.nio.file.Files.readAllLines(repositoryRoot.resolve(sourcePath))) {
                if (line.startsWith("## Questions & Comments")) {
                    inQuestions = true;
                    continue;
                }
                if (inQuestions && line.startsWith("## ")) {
                    inQuestions = false;
                }
                if (inQuestions && line.startsWith("- [")) {
                    questions.add(new RetainedQuestion(sourcePath, line.substring(2).strip()));
                }
            }
        }
        return questions.stream().sorted(java.util.Comparator.comparing(RetainedQuestion::sourcePath)
                .thenComparing(RetainedQuestion::text)).toList();
    }

    private static ResolvedSchema loadSchema(Path repositoryRoot, CodegenConfiguration configuration)
            throws IOException {
        var extractor = new MarkdownSchemaExtractor();
        var parser = new SchemaParser();
        var documents = new ArrayList<org.hiero.sdk.v3.codegen.model.SchemaDocument>();
        for (var sourcePath : configuration.specifications().stream().sorted().toList()) {
            var extracted = extractor.extract(repositoryRoot, repositoryRoot.resolve(sourcePath));
            documents.add(parser.parse(extracted));
        }
        return new SchemaValidator().validate(documents);
    }

    private record Options(
            String command,
            Path repositoryRoot,
            Path configurationFile,
            Path physicalSourceRoot,
            Path physicalManifest,
            Path physicalStagingRoot) {
        private static Options parse(String[] args) {
            if (args.length == 0) {
                throw new IllegalArgumentException(
                        "usage: <validate|inventory|generate> [--repository-root PATH] [--config PATH]");
            }
            var command = args[0];
            var repositoryRoot = Path.of(".").toAbsolutePath().normalize();
            var configuration = Path.of("codegen/java-base.yml");
            Path physicalSourceRoot = null;
            Path physicalManifest = null;
            Path physicalStagingRoot = null;
            var remaining = List.of(args).subList(1, args.length);
            for (var index = 0; index < remaining.size(); index += 2) {
                if (index + 1 >= remaining.size()) {
                    throw new IllegalArgumentException("missing value for option " + remaining.get(index));
                }
                switch (remaining.get(index)) {
                    case "--repository-root" -> repositoryRoot = Path.of(remaining.get(index + 1)).toAbsolutePath().normalize();
                    case "--config" -> configuration = Path.of(remaining.get(index + 1));
                    case "--physical-source-root" -> physicalSourceRoot = Path.of(remaining.get(index + 1)).toAbsolutePath().normalize();
                    case "--physical-manifest" -> physicalManifest = Path.of(remaining.get(index + 1)).toAbsolutePath().normalize();
                    case "--physical-staging-root" -> physicalStagingRoot = Path.of(remaining.get(index + 1)).toAbsolutePath().normalize();
                    default -> throw new IllegalArgumentException("unknown option: " + remaining.get(index));
                }
            }
            requireContained(repositoryRoot, physicalSourceRoot, "--physical-source-root");
            requireContained(repositoryRoot, physicalManifest, "--physical-manifest");
            requireContained(repositoryRoot, physicalStagingRoot, "--physical-staging-root");
            return new Options(command, repositoryRoot, configuration, physicalSourceRoot, physicalManifest, physicalStagingRoot);
        }

        private static void requireContained(Path repositoryRoot, Path path, String option) {
            if (path != null && !path.startsWith(repositoryRoot)) {
                throw new IllegalArgumentException(option + " must remain within the repository root");
            }
        }
    }
}
