package org.hiero.sdk.v3.metalang.generator.go;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.hiero.sdk.v3.metalang.MetaLang;
import org.hiero.sdk.v3.metalang.TestSpecs;
import org.hiero.sdk.v3.metalang.ValidationReport;
import org.hiero.sdk.v3.metalang.diagnostic.Severity;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.generator.GenerationException;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GoGeneratorTest {

    private static final Path SPECS = Path.of(System.getProperty("spec.root", "../../../spec"));

    @TempDir
    Path temp;

    private static LinkedModel model(final Map<String, String> schemas) {
        final Map<String, String> documents = new TreeMap<>();
        schemas.forEach((file, schema) -> documents.put(file, schema.startsWith("# ") ? schema
                : TestSpecs.markdown(schema)));
        final ValidationReport report = new MetaLang().validate(documents);
        assertThat(report.diagnostics()).filteredOn(d -> d.severity() == Severity.ERROR).isEmpty();
        return LinkedModel.of(report.model());
    }

    static String file(final List<GeneratedFile> files, final String path) {
        return files.stream().filter(f -> f.path().equals(path)).findFirst()
                .orElseThrow(() -> new AssertionError("no " + path + " in " + files.stream()
                        .map(GeneratedFile::path).toList())).content();
    }

    @Nested
    class Layout {

        @Test
        void shouldCreateOneModuleWithOnePackagePerNamespace() {
            // GIVEN three namespaces in two spec folders - unlike Rust, a folder is no artifact boundary in Go
            final LinkedModel model = model(Map.of(
                    "base/b.md", "namespace ledger\nPoint { @@immutable x: int32 }\n",
                    "base/c.md", "namespace ledger.config\nSetting { @@immutable name: string }\n",
                    "client/d.md", "namespace nativeToken\nUnit { @@immutable symbol: string }\n"));

            // WHEN
            final List<GeneratedFile> files = new GoGenerator().generate(model);

            // THEN one module, and a package in a directory per namespace segment, with a file per type
            assertThat(files).extracting(GeneratedFile::path).containsExactly(
                    ".gitignore", "go.mod", "ledger/config/doc.go", "ledger/config/setting.go",
                    "ledger/doc.go", "ledger/point.go", "nativetoken/doc.go", "nativetoken/unit.go");
            assertThat(files).allMatch(f -> f.content().lines().findFirst().orElseThrow()
                    .contains(GoGenerator.MARKER));
        }

        @Test
        void shouldUseTheConfiguredModulePathAndVersion() {
            // GIVEN
            final GoGeneratorConfig config = new GoGeneratorConfig("example.com/acme/sdk", "1.28");

            // WHEN
            final List<GeneratedFile> files = new GoGenerator(config)
                    .generate(model(Map.of("f/a.md", "namespace a\nX { @@immutable n: int32 }\n")));

            // THEN
            assertThat(file(files, "go.mod"))
                    .isEqualTo("// " + GoGenerator.MARKER + "\n\nmodule example.com/acme/sdk\n\ngo 1.28\n");
            assertThat(config.importPath("ledger.config")).isEqualTo("example.com/acme/sdk/ledger/config");
            assertThat(new GoGenerator(config).config()).isSameAs(config);
        }

        @Test
        void shouldOpenThePackageCommentWithThePackageName() {
            // GIVEN / WHEN - `go doc` expects a doc comment to start with the identifier it documents
            final List<GeneratedFile> files = new GoGenerator()
                    .generate(model(Map.of("f/a.md", "namespace nativeToken\nX { @@immutable n: int32 }\n")));

            // THEN
            assertThat(file(files, "nativetoken/doc.go"))
                    .startsWith("// " + GoGenerator.MARKER + "\n\n"
                            + "// nativetoken holds the API of the meta-language namespace nativeToken.\n")
                    .endsWith("package nativetoken\n");
        }

        @Test
        void shouldWrapEveryDescriptionOfANamespaceIntoCommentLines() {
            // GIVEN two spec files of one namespace, the first with a description longer than one line
            final String long_ = "The ledger is the network itself. " + "It carries accounts and tokens. ".repeat(6);
            final LinkedModel model = model(Map.of(
                    "f/a.md", TestSpecs.markdown("namespace a\nX { @@immutable n: int32 }\n")
                            .replace("Text.", long_),
                    "f/b.md", TestSpecs.markdown("namespace a\nY { @@immutable n: int32 }\n")
                            .replace("Text.", "Second file.")));

            // WHEN
            final String doc = file(new GoGenerator().generate(model), "a/doc.go");

            // THEN every description becomes its own paragraph, wrapped, and nothing exceeds the line width
            assertThat(doc).contains("//\n// The ledger is the network itself.")
                    .contains("//\n// Second file.\n");
            assertThat(doc.lines().filter(line -> line.startsWith("//")))
                    .isNotEmpty()
                    .allMatch(line -> line.length() <= 112, "wrapped to the comment width");
            assertThat(doc).doesNotContain("\n\n\n");
        }

        @Test
        void shouldSkipANamespaceWithoutADescription() {
            // GIVEN a spec whose ## Description section is empty
            final LinkedModel model = model(Map.of(
                    "f/a.md", TestSpecs.markdown("namespace a\nX { @@immutable n: int32 }\n")
                            .replace("Text.", "")));

            // WHEN
            final String doc = file(new GoGenerator().generate(model), "a/doc.go");

            // THEN only the generated sentence remains - no empty comment paragraph
            assertThat(doc).isEqualTo("// " + GoGenerator.MARKER + "\n\n"
                    + "// a holds the API of the meta-language namespace a.\n"
                    + "package a\n");
        }

        @Test
        void shouldIgnoreTheBuildOutputInGit() {
            final List<GeneratedFile> files = new GoGenerator()
                    .generate(model(Map.of("f/a.md", "namespace a\nX { @@immutable n: int32 }\n")));

            assertThat(file(files, ".gitignore")).contains("/bin/");
        }
    }

    @Nested
    class Configuration {

        @Test
        void shouldReadTheGoKeysAndIgnoreOtherLanguages() throws Exception {
            // GIVEN
            final Path file = Files.writeString(temp.resolve("generator.properties"),
                    "java.groupId = org.hiero\ngo.module = example.com/m\ngo.version = 1.27\n");

            // WHEN / THEN
            assertThat(GoGeneratorConfig.load(file)).isEqualTo(new GoGeneratorConfig("example.com/m", "1.27"));
        }

        @Test
        void shouldFallBackToTheDefaults() throws Exception {
            // GIVEN
            final Path file = Files.writeString(temp.resolve("empty.properties"), "");

            // WHEN / THEN
            assertThat(GoGeneratorConfig.load(file)).isEqualTo(GoGeneratorConfig.DEFAULT);
            assertThat(GoGeneratorConfig.DEFAULT.version()).isEqualTo(GoGeneratorConfig.DEFAULT_VERSION);
            assertThat(GoGeneratorConfig.DEFAULT.module()).isEqualTo(GoGeneratorConfig.DEFAULT_MODULE);
        }

        @Test
        void shouldRejectAnUnknownGoKey() throws Exception {
            // GIVEN
            final Path file = Files.writeString(temp.resolve("bad.properties"), "go.unknown = 1\n");

            // WHEN / THEN
            assertThatThrownBy(() -> GoGeneratorConfig.load(file))
                    .isInstanceOfSatisfying(GenerationException.class, e -> assertThat(e.problems())
                            .containsExactly("Unknown key 'go.unknown' (known: go.module, go.version)"));
        }

        @Test
        void shouldRejectValuesThatAreNotValidForGo() {
            // a module path is a slash-separated path, a version is `major.minor[.patch]`
            assertThatThrownBy(() -> new GoGeneratorConfig("not a module path", "1.27"))
                    .isInstanceOfSatisfying(GenerationException.class, e -> assertThat(e.problems())
                            .containsExactly("go.module: 'not a module path' is no Go module path"));
            assertThatThrownBy(() -> new GoGeneratorConfig("example.com/m", "latest"))
                    .isInstanceOfSatisfying(GenerationException.class, e -> assertThat(e.problems())
                            .containsExactly("go.version: 'latest' is no Go version"));
            assertThatThrownBy(() -> new GoGeneratorConfig("", ""))
                    .isInstanceOfSatisfying(GenerationException.class,
                            e -> assertThat(e.problems()).hasSize(2));
        }
    }

    @Nested
    class RepositorySpecs {

        @Test
        void shouldGenerateTheSpecsOfThisRepository() {
            // GIVEN the real specs
            final LinkedModel model = LinkedModel.of(new MetaLang().validate(SPECS).model());

            // WHEN
            final List<GeneratedFile> files = new GoGenerator().generate(model);

            // THEN every namespace becomes a package, and no two namespaces claim the same directory
            assertThat(files).extracting(GeneratedFile::path).doesNotHaveDuplicates();
            assertThat(files).filteredOn(f -> f.path().endsWith("/doc.go"))
                    .hasSize(model.namespaces().size());
            assertThat(file(files, "go.mod")).contains("module " + GoGeneratorConfig.DEFAULT_MODULE);
            assertThat(file(files, "consensusnode/transactions/accounts/doc.go"))
                    .contains("package accounts");
        }
    }
}
