package org.hiero.sdk.v3.metalang.generator.java;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilderFactory;
import org.hiero.sdk.v3.metalang.MetaLang;
import org.hiero.sdk.v3.metalang.TestSpecs;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.generator.GenerationException;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

class MavenGeneratorTest {

    @TempDir
    Path temp;

    private static List<GeneratedFile> generate(final JavaGeneratorConfig config) {
        final Map<String, String> documents = new TreeMap<>();
        documents.put("base/b.md", TestSpecs.markdown("namespace b\nB { @@immutable x: int32 }\n"));
        documents.put("node-client/c.md", TestSpecs.markdown("namespace c\nrequires {B} from b\n"
                + "C { @@immutable b: B }\n"));
        return new JavaGenerator(config).generate(LinkedModel.of(new MetaLang().validate(documents).model()));
    }

    private static Document pom(final List<GeneratedFile> files, final String path) throws Exception {
        final String xml = files.stream().filter(f -> f.path().equals(path)).findFirst().orElseThrow().content();
        assertThat(xml).startsWith("<!-- " + JavaGenerator.MARKER + " -->\n");
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    private static List<String> texts(final Document document, final String tag) {
        final NodeList nodes = document.getElementsByTagName(tag);
        final List<String> result = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            result.add(nodes.item(i).getTextContent().strip());
        }
        return result;
    }

    @Test
    void shouldGenerateAParentProjectWithOneSubModulePerJavaModule() throws Exception {
        // WHEN
        final List<GeneratedFile> files = generate(JavaGeneratorConfig.DEFAULT);
        final Document parent = pom(files, "pom.xml");

        // THEN
        assertThat(files).extracting(GeneratedFile::path).filteredOn(p -> p.endsWith("pom.xml"))
                .containsExactly("org.hiero.base/pom.xml", "org.hiero.node.client/pom.xml", "pom.xml");
        assertThat(texts(parent, "packaging")).containsExactly("pom");
        assertThat(texts(parent, "module")).containsExactly("org.hiero.base", "org.hiero.node.client");
        assertThat(texts(parent, "maven.compiler.release")).containsExactly("25");
        assertThat(texts(parent, "arg")).containsExactly("-Xlint:all", "-Werror");
        assertThat(texts(parent, "doclint")).containsExactly("all,-missing");
        assertThat(texts(parent, "failOnWarnings")).containsExactly("true");
    }

    @Test
    void shouldPinTheVersionOfEveryPlugin() throws Exception {
        // GIVEN
        final Document parent = pom(generate(JavaGeneratorConfig.DEFAULT), "pom.xml");
        final Element management = (Element) parent.getElementsByTagName("pluginManagement").item(0);
        final NodeList plugins = management.getElementsByTagName("plugin");

        // THEN every managed plugin has a version, and the Javadoc plugin that is used is managed
        final List<String> managed = new ArrayList<>();
        for (int i = 0; i < plugins.getLength(); i++) {
            final Element plugin = (Element) plugins.item(i);
            assertThat(plugin.getElementsByTagName("version").getLength()).isEqualTo(1);
            managed.add(plugin.getElementsByTagName("artifactId").item(0).getTextContent());
        }
        assertThat(managed).contains("maven-compiler-plugin", "maven-jar-plugin", "maven-javadoc-plugin",
                "maven-resources-plugin", "maven-surefire-plugin", "maven-install-plugin", "maven-clean-plugin");
        assertThat(texts(parent, "goal")).containsExactly("jar");
    }

    @Test
    void shouldDependOnTheRequiredModulesAndJspecify() throws Exception {
        // WHEN
        final Document client = pom(generate(JavaGeneratorConfig.DEFAULT), "org.hiero.node.client/pom.xml");

        // THEN
        assertThat(texts(client, "artifactId")).containsExactly("hiero-sdk", "hiero-node-client", "hiero-base",
                "jspecify", "junit-jupiter");
        assertThat(texts(client, "scope")).containsExactly("test");
        assertThat(texts(client, "description")).containsExactly("Module org.hiero.node.client of the Hiero SDK.");
        assertThat(MavenGenerator.artifactId("consensus-node-client")).isEqualTo("hiero-consensus-node-client");
    }

    @Test
    void shouldUseTheConfiguredCoordinates() throws Exception {
        // GIVEN
        final Path file = temp.resolve("generator.properties");
        Files.writeString(file, "java.groupId = com.example.sdk\njava.version = 3.0.0\n");

        // WHEN
        final JavaGeneratorConfig config = JavaGeneratorConfig.load(file);
        final List<GeneratedFile> files = generate(config);

        // THEN
        assertThat(config.groupId()).isEqualTo("com.example.sdk");
        assertThat(texts(pom(files, "pom.xml"), "version")).first().isEqualTo("3.0.0");
        assertThat(texts(pom(files, "org.hiero.base/pom.xml"), "groupId")).first().isEqualTo("com.example.sdk");
        assertThat(new JavaGeneratorConfig(Set.of())).isEqualTo(JavaGeneratorConfig.DEFAULT);
        Files.writeString(file, "java.groupId = com example\njava.version = 1 0\n");
        assertThatThrownBy(() -> JavaGeneratorConfig.load(file)).isInstanceOfSatisfying(GenerationException.class,
                e -> assertThat(e.problems()).containsExactly(
                        "'com example' in java.groupId is no valid Maven groupId",
                        "'1 0' in java.version is no valid Maven version"));
    }

    @Test
    void shouldReportUnknownKeysAndMalformedInterfaceNames() throws Exception {
        // GIVEN
        final Path file = temp.resolve("generator.properties");
        Files.writeString(file, "java.interfaces = , Plain .Leading trailing. a.b.Valid\nother = 1\n");

        // WHEN / THEN
        assertThatThrownBy(() -> JavaGeneratorConfig.load(file)).isInstanceOfSatisfying(GenerationException.class,
                e -> assertThat(e.problems()).containsExactly(
                        "Unknown key 'other' in " + file + " (known: java.interfaces, java.groupId, java.version, java.protobuf)",
                        "'Plain' in java.interfaces is no qualified type name (namespace.Type)",
                        "'.Leading' in java.interfaces is no qualified type name (namespace.Type)",
                        "'trailing.' in java.interfaces is no qualified type name (namespace.Type)"));
    }

    @Test
    void shouldDependOnTheProtobufModuleOnlyWhereItIsConfigured() throws Exception {
        // GIVEN the node-client folder is configured to need the protobuf messages
        final JavaGeneratorConfig config = new JavaGeneratorConfig(Set.of(),
                JavaGeneratorConfig.DEFAULT_GROUP_ID, JavaGeneratorConfig.DEFAULT_VERSION, Set.of("node-client"));

        // WHEN
        final List<GeneratedFile> files = generate(config);

        // THEN the configured module depends on it, the other one does not
        assertThat(texts(pom(files, "org.hiero.node.client/pom.xml"), "artifactId"))
                .contains("hiero-sdk-protobuf");
        assertThat(texts(pom(files, "org.hiero.base/pom.xml"), "artifactId"))
                .doesNotContain("hiero-sdk-protobuf");

        // AND it requires the module without `transitive`: the wire format is no part of the API
        assertThat(content(files, "org.hiero.node.client/src/main/java/module-info.java"))
                .contains("    requires org.hiero.sdk.protobuf;")
                .doesNotContain("requires transitive org.hiero.sdk.protobuf");
        assertThat(content(files, "org.hiero.base/src/main/java/module-info.java"))
                .doesNotContain("org.hiero.sdk.protobuf");
    }

    @Test
    void shouldGenerateNoProtobufWiringWithoutConfiguration() throws Exception {
        // WHEN
        final List<GeneratedFile> files = generate(JavaGeneratorConfig.DEFAULT);

        // THEN
        assertThat(texts(pom(files, "org.hiero.node.client/pom.xml"), "artifactId"))
                .doesNotContain("hiero-sdk-protobuf");
        assertThat(content(files, "org.hiero.node.client/src/main/java/module-info.java"))
                .doesNotContain("org.hiero.sdk.protobuf");
    }

    @Test
    void shouldReadAndValidateTheProtobufFolders() throws Exception {
        // GIVEN
        final Path valid = temp.resolve("valid.properties");
        Files.writeString(valid, "java.protobuf = consensus-node-client, mirror-node-client\n");
        final Path invalid = temp.resolve("invalid.properties");
        Files.writeString(invalid, "java.protobuf = Node_Client\n");

        // WHEN / THEN
        assertThat(JavaGeneratorConfig.load(valid).protobuf())
                .containsExactlyInAnyOrder("consensus-node-client", "mirror-node-client");
        assertThatThrownBy(() -> JavaGeneratorConfig.load(invalid))
                .isInstanceOfSatisfying(GenerationException.class, e -> assertThat(e.problems()).containsExactly(
                        "'Node_Client' in java.protobuf is no spec folder (lowercase, '-' separated)"));
    }

    private static String content(final List<GeneratedFile> files, final String path) {
        return files.stream().filter(f -> f.path().equals(path)).findFirst().orElseThrow().content();
    }
}
