package org.hiero.sdk.v3.metalang.generator.java;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.hiero.sdk.v3.metalang.MetaLang;
import org.hiero.sdk.v3.metalang.TestSpecs;
import org.hiero.sdk.v3.metalang.ValidationReport;
import org.hiero.sdk.v3.metalang.diagnostic.Severity;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Edge cases of the test values ({@link JavaSamples}) and of the generated tests. */
class TestValuesTest {

    @TempDir
    Path temp;

    @Test
    void shouldBuildValuesForEdgeCasesAndRunTheTests() throws Exception {
        // GIVEN
        final String markdown = TestSpecs.markdown("""
                namespace a
                enum Kind { A }
                abstraction Unit { @@immutable symbol: string }
                abstraction Remote { void call() }
                abstraction Shape {
                    @@immutable name: string
                    @@finalMethod string label()
                    double area()
                }
                UnitImpl extends Unit { }
                Holder<$$T> {
                    @@immutable anything: ANY
                    @@immutable values: list<$$T>
                    @@immutable wildcards: list<ANY>
                    @@immutable kinds: set<Kind>
                    @@immutable @@minSize(2) manyKinds: set<Kind>
                    @@immutable remotes: map<string, Remote>
                    @@immutable shape: Shape
                    @@immutable factory: function<Remote make()>
                    @@nullable callback: function<void run(x: int32, y: int32, z: int32)>
                }
                Box<$$U extends Unit> { @@immutable unit: $$U }
                User {
                    @@immutable remote: Remote
                    void use(remote: Remote, @@nullable shape: Shape)
                    @@deprecated void old()
                }
                @@static Remote connect(@@nullable host: string, ports: int32...)
                @@static @@async Remote connectLater()
                @@static void reset()
                @@static @@nullable Remote find()
                """).replace("## Testing", """
                ## Default Instances

                ```
                instance Remote = connect()
                ```

                ## Testing""");
        final ValidationReport report = new MetaLang().validate(Map.of("f/a.md", markdown));
        assertThat(report.diagnostics()).filteredOn(d -> d.severity() == Severity.ERROR).isEmpty();

        // WHEN
        final List<GeneratedFile> files = new JavaGenerator().generate(LinkedModel.of(report.model()));
        final String holder = files.stream().filter(f -> f.path().endsWith("/HolderTest.java")).findFirst()
                .orElseThrow().content();

        // THEN
        assertThat(holder).contains("return \"value\";")
                .contains("return List.of(\"value\");")
                .contains("/// - no valid value for `manyKinds` of Holder")
                .doesNotContain("shouldCompareByValue");
        assertThat(files.stream().filter(f -> f.path().endsWith("/BoxTest.java")).findFirst().orElseThrow()
                .content()).contains("new UnitImpl(\"value\")");
        assertThat(files.stream().filter(f -> f.path().endsWith("/UserTest.java")).findFirst().orElseThrow()
                .content()).contains("@SuppressWarnings(\"deprecation\")")
                .contains("create().use(remoteValue(), (Shape) null)")
                .contains("return AFactory.connect((String) null);");
        assertThat(TestGenerator.untested(files)).contains(
                "HolderTest: no valid value for `manyKinds` of Holder: constructor, attribute and method tests");

        // WHEN the generated tests run
        final GeneratedJava.Compilation modules = GeneratedJava.compile(files, temp);
        final GeneratedJava.Compilation tests = GeneratedJava.compileTests(files, modules, temp);
        final GeneratedJava.TestRun run = GeneratedJava.runTests(files, tests);

        // THEN
        assertThat(modules.diagnostics()).isEmpty();
        assertThat(tests.diagnostics()).isEmpty();
        assertThat(run.unexpected()).isEmpty();
        assertThat(run.stubs()).isNotEmpty();
    }
}
