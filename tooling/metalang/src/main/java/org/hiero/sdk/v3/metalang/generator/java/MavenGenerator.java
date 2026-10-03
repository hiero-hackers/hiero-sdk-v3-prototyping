package org.hiero.sdk.v3.metalang.generator.java;

import java.util.List;
import java.util.stream.Collectors;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.jspecify.annotations.Nullable;

/**
 * Generates the Maven build of the generated Java code: a parent project ({@code pom.xml}, packaging {@code pom})
 * with one sub-module per JPMS module, so that every module is built into its own JAR. The parent defines the Java
 * release, the encoding, the jspecify version and the versions of all plugins, compiles with
 * {@code -Xlint:all -Werror} and builds a Javadoc JAR per module with doclint ({@code all,-missing}, warnings fail the
 * build), so that the generated code and its documentation stay free of warnings. A module depends on the modules its {@code module-info.java}
 * requires and on jspecify.
 */
final class MavenGenerator {

    /** The artifactId of the parent project. */
    static final String PARENT_ARTIFACT_ID = "hiero-sdk";

    /** The version of the jspecify annotations. */
    static final String JSPECIFY_VERSION = "1.0.0";

    /** The JUnit version of the generated tests. */
    static final String JUNIT_VERSION = "6.0.3";

    private static final String NAMESPACES = """
            <project xmlns="http://maven.apache.org/POM/4.0.0" \
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 \
            https://maven.apache.org/xsd/maven-4.0.0.xsd">
            """;

    /**
     * A generated Maven module.
     *
     * @param directory  the directory of the module (the JPMS module name)
     * @param artifactId the artifactId
     * @param name       the display name of the spec folder
     * @param requires   the artifactIds of the modules it depends on
     */
    record MavenModule(String directory, String artifactId, String name, List<String> requires) {
    }

    private MavenGenerator() {
    }

    /**
     * Returns the artifactId of the module of a spec folder ({@code consensus-node-client} becomes
     * {@code hiero-consensus-node-client}).
     *
     * @param folder the spec folder
     * @return the artifactId
     */
    static String artifactId(final String folder) {
        return "hiero-" + folder;
    }

    static GeneratedFile parent(final List<MavenModule> modules, final JavaGeneratorConfig config) {
        final String xml = header()
                + "    <modelVersion>4.0.0</modelVersion>\n\n"
                + "    <groupId>" + config.groupId() + "</groupId>\n"
                + "    <artifactId>" + PARENT_ARTIFACT_ID + "</artifactId>\n"
                + "    <version>" + config.version() + "</version>\n"
                + "    <packaging>pom</packaging>\n\n"
                + "    <name>Hiero SDK</name>\n"
                + "    <description>The Java API of the Hiero SDK.</description>\n\n"
                + "    <modules>\n"
                + modules.stream().map(m -> "        <module>" + m.directory() + "</module>\n")
                .collect(Collectors.joining())
                + "    </modules>\n\n"
                + "    <properties>\n"
                + "        <maven.compiler.release>25</maven.compiler.release>\n"
                + "        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n"
                + "        <jspecify.version>" + JSPECIFY_VERSION + "</jspecify.version>\n"
                + "        <junit.version>" + JUNIT_VERSION + "</junit.version>\n"
                + "    </properties>\n\n"
                + "    <dependencyManagement>\n"
                + "        <dependencies>\n"
                + dependency("org.jspecify", "jspecify", "${jspecify.version}", "            ")
                + "            <dependency>\n"
                + "                <groupId>org.junit</groupId>\n"
                + "                <artifactId>junit-bom</artifactId>\n"
                + "                <version>${junit.version}</version>\n"
                + "                <type>pom</type>\n"
                + "                <scope>import</scope>\n"
                + "            </dependency>\n"
                + "        </dependencies>\n"
                + "    </dependencyManagement>\n\n"
                + "    <build>\n"
                + "        <pluginManagement>\n"
                + "            <plugins>\n"
                + plugin("maven-clean-plugin", "3.4.1", "")
                + plugin("maven-resources-plugin", "3.3.1", "")
                + plugin("maven-compiler-plugin", "3.14.0", """
                                            <configuration>
                                                <compilerArgs>
                                                    <arg>-Xlint:all</arg>
                                                    <arg>-Werror</arg>
                                                </compilerArgs>
                                            </configuration>
                        """)
                + plugin("maven-surefire-plugin", "3.5.3", "")
                + plugin("maven-jar-plugin", "3.4.2", "")
                + plugin("maven-javadoc-plugin", "3.12.0", """
                                            <configuration>
                                                <doclint>all,-missing</doclint>
                                                <failOnWarnings>true</failOnWarnings>
                                                <quiet>true</quiet>
                                            </configuration>
                        """)
                + plugin("maven-install-plugin", "3.1.4", "")
                + "            </plugins>\n"
                + "        </pluginManagement>\n"
                + "        <plugins>\n"
                // every module also gets a Javadoc JAR; doclint warnings fail the build
                + "            <plugin>\n"
                + "                <groupId>org.apache.maven.plugins</groupId>\n"
                + "                <artifactId>maven-javadoc-plugin</artifactId>\n"
                + "                <executions>\n"
                + "                    <execution>\n"
                + "                        <id>attach-javadocs</id>\n"
                + "                        <goals>\n"
                + "                            <goal>jar</goal>\n"
                + "                        </goals>\n"
                + "                    </execution>\n"
                + "                </executions>\n"
                + "            </plugin>\n"
                + "        </plugins>\n"
                + "    </build>\n"
                + "</project>\n";
        return new GeneratedFile("pom.xml", xml);
    }

    static GeneratedFile module(final MavenModule module, final JavaGeneratorConfig config) {
        final String xml = header()
                + "    <modelVersion>4.0.0</modelVersion>\n\n"
                + "    <parent>\n"
                + "        <groupId>" + config.groupId() + "</groupId>\n"
                + "        <artifactId>" + PARENT_ARTIFACT_ID + "</artifactId>\n"
                + "        <version>" + config.version() + "</version>\n"
                + "    </parent>\n\n"
                + "    <artifactId>" + module.artifactId() + "</artifactId>\n\n"
                + "    <name>Hiero SDK: " + module.name() + "</name>\n"
                + "    <description>Module " + module.directory() + " of the Hiero SDK.</description>\n\n"
                + "    <dependencies>\n"
                + module.requires().stream()
                .map(r -> dependency("${project.groupId}", r, "${project.version}", "        "))
                .collect(Collectors.joining())
                // the nullness annotations are part of the API (requires static transitive org.jspecify)
                + dependency("org.jspecify", "jspecify", null, "        ")
                // the generated tests (src/test/java)
                + "        <dependency>\n"
                + "            <groupId>org.junit.jupiter</groupId>\n"
                + "            <artifactId>junit-jupiter</artifactId>\n"
                + "            <scope>test</scope>\n"
                + "        </dependency>\n"
                + "    </dependencies>\n"
                + "</project>\n";
        return new GeneratedFile(module.directory() + "/pom.xml", xml);
    }

    private static String header() {
        return "<!-- " + JavaGenerator.MARKER + " -->\n" + NAMESPACES;
    }

    private static String dependency(final String groupId, final String artifactId, final @Nullable String version,
                                     final String indent) {
        return indent + "<dependency>\n"
                + indent + "    <groupId>" + groupId + "</groupId>\n"
                + indent + "    <artifactId>" + artifactId + "</artifactId>\n"
                + (version == null ? "" : indent + "    <version>" + version + "</version>\n")
                + indent + "</dependency>\n";
    }

    private static String plugin(final String artifactId, final String version, final String configuration) {
        return "                <plugin>\n"
                + "                    <groupId>org.apache.maven.plugins</groupId>\n"
                + "                    <artifactId>" + artifactId + "</artifactId>\n"
                + "                    <version>" + version + "</version>\n"
                + configuration
                + "                </plugin>\n";
    }
}
