package org.hiero.sdk.v3.metalang.generator.java;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.hiero.sdk.v3.metalang.MetaLang;
import org.hiero.sdk.v3.metalang.TestSpecs;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JavaGeneratorTest {

    private final JavaGenerator generator = new JavaGenerator();

    private static Path resource(final String name) throws URISyntaxException {
        return Path.of(JavaGeneratorTest.class.getResource(name).toURI());
    }

    private static List<GeneratedFile> generate(final Map<String, String> documents) {
        return new JavaGenerator().generate(LinkedModel.of(new MetaLang().validate(documents).model()));
    }

    @Nested
    class Golden {

        @Test
        void shouldGenerateTheGoldenFilesForTheExampleSpec() throws Exception {
            // GIVEN
            final Path expectedRoot = resource("/generator-golden/java");
            final List<GeneratedFile> expected = new ArrayList<>();
            try (Stream<Path> files = Files.walk(expectedRoot)) {
                for (final Path file : files.filter(Files::isRegularFile).sorted().toList()) {
                    expected.add(new GeneratedFile(expectedRoot.relativize(file).toString().replace('\\', '/'),
                            Files.readString(file, StandardCharsets.UTF_8)));
                }
            }

            // WHEN
            final List<GeneratedFile> actual = generator.generate(
                    LinkedModel.of(new MetaLang().validate(resource("/model-golden/spec")).model()));

            // THEN regenerate the golden files after an intended change with:
            // java -jar target/metalang-*-cli.jar generate --language=java
            //      --output=src/test/resources/generator-golden/java src/test/resources/model-golden/spec
            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    class Modules {

        private static String moduleInfo(final List<GeneratedFile> files, final String module) {
            return files.stream().filter(f -> f.path().equals(module + "/src/main/java/module-info.java"))
                    .findFirst().orElseThrow().content();
        }

        @Test
        void shouldCreateOneModulePerSpecFolderWithOnePackagePerNamespace() {
            // GIVEN two namespaces in folder "my-base" and one in "client"
            final List<GeneratedFile> files = generate(Map.of(
                    "my-base/a.md", TestSpecs.markdown("namespace a\nA {}\n"),
                    "my-base/b.md", TestSpecs.markdown("namespace b.sub\n"),
                    "client/c.md", TestSpecs.markdown("namespace c\nrequires {A} from a\nC { @@immutable a: A }\n")));

            // THEN
            assertThat(files).extracting(GeneratedFile::path).filteredOn(p -> p.endsWith(".java")).containsExactly(
                    "org.hiero.client/src/main/java/module-info.java",
                    "org.hiero.client/src/main/java/org/hiero/c/C.java",
                    "org.hiero.client/src/main/java/org/hiero/c/package-info.java",
                    "org.hiero.my.base/src/main/java/module-info.java",
                    "org.hiero.my.base/src/main/java/org/hiero/a/A.java",
                    "org.hiero.my.base/src/main/java/org/hiero/a/package-info.java",
                    "org.hiero.my.base/src/main/java/org/hiero/b/sub/package-info.java");
            assertThat(moduleInfo(files, "org.hiero.my.base")).contains("""
                    module org.hiero.my.base {
                        requires static transitive org.jspecify;

                        exports org.hiero.a;
                        // exports org.hiero.b.sub; (enabled as soon as the package contains generated types)
                    }
                    """).contains("/// Module `org.hiero.my.base` of the Hiero SDK.\n///\n/// Packages:\n"
                    + "/// - `org.hiero.a`\n/// - `org.hiero.b.sub`\n").doesNotContain("my-base");
            assertThat(moduleInfo(files, "org.hiero.client")).contains("requires transitive org.hiero.my.base;");
        }

        @Test
        void shouldRequireModulesOfQualifiedReferencesButNotOfInheritedMembers() {
            // GIVEN a (folder fa) extends b.Base (folder fb) and references d.D (folder fd) qualified without import;
            // b.Base has a field of type c.C (folder fc), which a only reaches through inheritance
            final List<GeneratedFile> files = generate(Map.of(
                    "fa/a.md", TestSpecs.markdown("namespace a\nrequires {Base} from b\n"
                            + "X extends Base { @@immutable d: d.D }\n"),
                    "fb/b.md", TestSpecs.markdown("namespace b\nrequires {C} from c\nabstraction Base { @@immutable c: C }\n"),
                    "fc/c.md", TestSpecs.markdown("namespace c\nC {}\n"),
                    "fd/d.md", TestSpecs.markdown("namespace d\nD {}\n")));

            // THEN
            final String moduleA = moduleInfo(files, "org.hiero.fa");
            assertThat(moduleA).contains("requires transitive org.hiero.fb;\n    requires transitive org.hiero.fd;")
                    .doesNotContain("org.hiero.fc");
        }

        @Test
        void shouldRejectStructuresThatCannotBeJavaModules() {
            // GIVEN a namespace spread over two folders, a spec outside of any folder and a cycle between folders
            final Map<String, String> documents = Map.of(
                    "x/split1.md", TestSpecs.markdown("namespace split\nS1 {}\n"),
                    "y/split2.md", TestSpecs.markdown("namespace split\nS2 {}\n"),
                    "root.md", TestSpecs.markdown("namespace root\nR {}\n"),
                    "p/p1.md", TestSpecs.markdown("namespace p1\nrequires {Q2} from q2\nP1 { @@immutable q: Q2 }\n"),
                    "p/p2.md", TestSpecs.markdown("namespace p2\nP2 {}\n"),
                    "q/q2.md", TestSpecs.markdown("namespace q2\nrequires {P2} from p2\nQ2 { @@immutable p: P2 }\n"));

            // WHEN / THEN
            assertThatThrownBy(() -> generate(documents)).isInstanceOfSatisfying(
                    org.hiero.sdk.v3.metalang.generator.GenerationException.class,
                    e -> assertThat(e.problems()).containsExactly(
                            "Spec file 'root.md' of namespace 'root' is not inside a folder; the folder defines the "
                                    + "Java module",
                            "Namespace 'split' is spread over the spec folders [x, y]; a Java package must belong "
                                    + "to exactly one module",
                            "Dependency cycle between Java modules: org.hiero.p -> org.hiero.q -> org.hiero.p"));
            assertThatThrownBy(() -> new org.hiero.sdk.v3.metalang.generator.GenerationException(List.of()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class PackageInfo {

        @Test
        void shouldCombineTheDescriptionsOfASplitNamespaceWithoutMentioningTheSpecFiles() {
            // GIVEN
            final String first = "# A\n\n## Description\n\nFirst part.\n\n## API Schema\n\n```\nnamespace a\nX {}\n```\n";
            final String second = "# B\n\n## Description\n\n## API Schema\n\n```\nnamespace a\nY {}\n```\n";
            final String third = "# C\n\n## Description\n\nThird part.\n\n## API Schema\n\n```\nnamespace a\nZ {}\n```\n";

            // WHEN
            final List<GeneratedFile> files = generate(Map.of("f/a1.md", first, "f/a2.md", second, "f/a3.md", third));

            // THEN the empty description of a2.md is skipped
            assertThat(files).filteredOn(f -> f.path().endsWith("package-info.java")).singleElement()
                    .satisfies(f -> assertThat(f.content()).isEqualTo("""
                            // Generated by metalang from the Hiero SDK V3 specs. Do not edit.

                            /// First part.
                            ///
                            /// Third part.
                            package org.hiero.a;
                            """));
        }

        @Test
        void shouldNotGenerateAPackageInfoWithoutDescription() {
            final List<GeneratedFile> files = generate(Map.of("f/a.md", "## API Schema\n```\nnamespace a\nX {}\n```\n"));
            assertThat(files).extracting(GeneratedFile::path).filteredOn(p -> p.endsWith(".java")).containsExactly("org.hiero.f/src/main/java/module-info.java",
                    "org.hiero.f/src/main/java/org/hiero/a/X.java");
        }
    }

    @Nested
    class Comments {

        @Test
        void shouldMoveTheDeprecationParagraphIntoTheDeprecatedTag() {
            // GIVEN a documentation whose second paragraph explains the deprecation over two lines
            final String documentation = "The old endpoint.\n\nDeprecated: the network removed it;\n@see `uri`.";

            // WHEN / THEN
            assertThat(MarkdownComment.render("", List.of(documentation), List.of("@param x the x"), true))
                    .isEqualTo("/// The old endpoint.\n///\n/// @param x the x\n/// @deprecated Deprecated: the network "
                            + "removed it;\n///   &#64;see `uri`.\n");
            assertThat(MarkdownComment.render("", List.of("Plain."), List.of(), true))
                    .isEqualTo("/// Plain.\n///\n/// @deprecated " + MarkdownComment.DEFAULT_DEPRECATION + "\n");
            assertThat(MarkdownComment.render("", List.of("Plain."), List.of(), false)).isEqualTo("/// Plain.\n");
            assertThat(MarkdownComment.deprecationReason(documentation))
                    .isEqualTo("Deprecated: the network removed it;\n@see `uri`.");
            assertThat(MarkdownComment.deprecationReason("No reason.")).isEmpty();
        }

        @Test
        void shouldRepeatTheReasonAtSettersAndDocumentDeprecatedMethods() {
            // WHEN
            final List<GeneratedFile> files = generate(Map.of("f/a.md", TestSpecs.markdown("""
                    namespace a
                    Client {
                        // The endpoint.
                        //
                        // Deprecated, use uri instead.
                        @@deprecated endpoint: string
                        // Connects the old way. Deprecated since the network changed.
                        @@deprecated void connectLegacy()
                    }
                    """)));
            final String client = files.stream().filter(f -> f.path().endsWith("/Client.java")).findFirst()
                    .orElseThrow().content();

            // THEN
            assertThat(client).contains("""
                        /// The endpoint.
                        ///
                        /// @deprecated Deprecated, use uri instead.
                        @Deprecated
                        public String endpoint() {
                    """).contains("""
                        /// Sets the `endpoint`.
                        ///
                        /// @param endpoint the new value
                        /// @return this object
                        /// @throws NullPointerException if the value is `null`
                        /// @deprecated Deprecated, use uri instead.
                        @Deprecated
                    """).contains("""
                        /// @deprecated Connects the old way. Deprecated since the network changed.
                        @Deprecated
                        public void connectLegacy() {
                    """);
        }

        @Test
        void shouldEscapeLeadingAtSignsOutsideOfCodeBlocks() {
            // GIVEN (fences follow CommonMark: a block is closed by a fence of the same character that is at least
            // as long as the opening fence)
            final String markdown = """
                    Text with @inline and
                    @@nullable at the start
                      @indented
                    ````
                    @@kept in code
                    ```
                    @@still code: a shorter fence does not close
                    ````
                    @after the block
                    ~~~
                    @@tilde code
                    ~~~~
                    """;

            // WHEN
            final String comment = MarkdownComment.render("  ", List.of(markdown));

            // THEN
            assertThat(comment).isEqualTo("""
                      /// Text with @inline and
                      /// &#64;@nullable at the start
                      ///   &#64;indented
                      /// ````
                      /// @@kept in code
                      /// ```
                      /// @@still code: a shorter fence does not close
                      /// ````
                      /// &#64;after the block
                      /// ~~~
                      /// @@tilde code
                      /// ~~~~
                    """);
        }

        @Test
        void shouldSkipBlankParagraphsAndRenderEmptyLines() {
            assertThat(MarkdownComment.render("", List.of("", "a  \n\nb", "  "))).isEqualTo("/// a\n///\n/// b\n");
            assertThat(MarkdownComment.render("", List.of(" "))).isEmpty();
        }
    }

    @Nested
    class Names {

        @Test
        void shouldMapFoldersToModulesAndNamespacesToPackages() {
            assertThat(JavaNames.packageName("consensusnode.transactions")).isEqualTo("org.hiero.consensusnode.transactions");
            assertThat(JavaNames.moduleName("consensus-node-client")).isEqualTo("org.hiero.consensus.node.client");
            assertThat(JavaNames.moduleName("base")).isEqualTo("org.hiero.base");
            assertThat(JavaNames.sourceRoot("org.hiero.base")).isEqualTo("org.hiero.base/src/main/java");
            assertThat(JavaNames.packageDirectory("org.hiero.base", "ledger.config"))
                    .isEqualTo("org.hiero.base/src/main/java/org/hiero/ledger/config");
        }

        @Test
        void shouldOnlyAcceptRelativeSlashSeparatedPaths() {
            assertThatThrownBy(() -> new GeneratedFile("/abs", "")).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new GeneratedFile("a/../b", "")).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new GeneratedFile("a\\b", "")).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class RealSpecs {

        @TempDir
        Path output;

        @Test
        void generatedModulesForAllSpecsShouldCompileWithoutWarnings() throws IOException {
            // GIVEN
            final Path specs = Path.of(System.getProperty("spec.root", "../../spec"));
            final List<GeneratedFile> files = generator.generate(LinkedModel.of(new MetaLang().validate(specs).model()));

            // WHEN compiling all modules with the module system (catches unknown modules and cycles)
            final GeneratedJava.Compilation compilation = GeneratedJava.compile(files, output);

            // THEN
            assertThat(compilation.diagnostics()).isEmpty();
            assertThat(compilation.success()).isTrue();
            final LinkedModel model = LinkedModel.of(new MetaLang().validate(specs).model());
            final int namespaces = model.namespaces().size();
            final long enums = model.types().stream()
                    .filter(t -> t instanceof org.hiero.sdk.v3.metalang.model.TypeDefinition.EnumDefinition).count();
            assertThat(files).filteredOn(f -> f.path().endsWith("/module-info.java")).extracting(GeneratedFile::path)
                    .containsExactly(
                            "org.hiero.base/src/main/java/module-info.java",
                            "org.hiero.consensus.node.admin.client/src/main/java/module-info.java",
                            "org.hiero.consensus.node.client/src/main/java/module-info.java",
                            "org.hiero.enterprise/src/main/java/module-info.java",
                            "org.hiero.mirror.node.client/src/main/java/module-info.java");
            final long withDescription = model.namespaces().stream()
                    .filter(n -> n.sources().stream().anyMatch(src -> !src.description().isBlank())).count();
            final long records = files.stream().filter(f -> f.content().contains("\npublic record ")).count();
            final long interfaces = files.stream()
                    .filter(f -> f.content().matches("(?s).*\npublic (sealed |non-sealed )?interface .*")).count();
            final long enumFiles = files.stream().filter(f -> f.content().contains("\npublic enum ")).count();
            final long classes = files.stream()
                    .filter(f -> f.content().matches("(?s).*\npublic (abstract )?(sealed |non-sealed |final )?class .*"))
                    .count();
            // 5 module-info.java, 6 pom.xml (parent and one per module)
            assertThat(files).hasSize(5 + 6 + (int) withDescription + (int) enumFiles + (int) records + (int) interfaces
                    + (int) classes);
            assertThat(enumFiles).isEqualTo(enums);
            assertThat(records).isGreaterThanOrEqualTo(80);
            assertThat(classes).isGreaterThanOrEqualTo(100);
            assertThat(interfaces).isGreaterThanOrEqualTo(10);
            // what is left are spec issues: covariant @@async overrides, missing Java mappings, and their dependants
            assertThat(generator.deferredTypes(model)).hasSizeLessThanOrEqualTo(13);
            assertThat(namespaces).isGreaterThanOrEqualTo((int) withDescription);
            assertThat(files).noneMatch(f -> f.content().contains("Specified in") || f.content().contains(".md`"));
            // non-null is the default of every module; only @@nullable declarations are annotated
            assertThat(files).filteredOn(f -> f.path().endsWith("/module-info.java"))
                    .allMatch(f -> f.content().contains("@NullMarked\nmodule "));
            assertThat(files).noneMatch(f -> f.content().contains("@NonNull"));
            assertThat(enums).isEqualTo(16);
        }

        @Test
        void generationShouldBeDeterministic() {
            final Path specs = Path.of(System.getProperty("spec.root", "../../spec"));
            assertThat(generator.generate(LinkedModel.of(new MetaLang().validate(specs).model())))
                    .isEqualTo(generator.generate(LinkedModel.of(new MetaLang().validate(specs).model())));
        }
    }
}
