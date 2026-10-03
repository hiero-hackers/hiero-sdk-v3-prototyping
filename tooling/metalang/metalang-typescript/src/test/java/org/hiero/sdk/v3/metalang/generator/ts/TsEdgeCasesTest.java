package org.hiero.sdk.v3.metalang.generator.ts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

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

/** Edge cases of the TypeScript generator, its test values and its tests; the result is built and run. */
class TsEdgeCasesTest {

    private static final String SPEC = """
            namespace a
            // A kind.
            enum Kind {
                A
                // Use A.
                @@deprecated B
            }
            enum Legacy { @@deprecated OLD }
            abstraction Unit { @@immutable symbol: string }
            abstraction Remote {
                void call()
                @@streaming string lines()
                @@streaming @@throws(io-error) string read()
            }
            @@sealed(Circle, Square)
            abstraction Shape { @@immutable name: string }
            Circle extends Shape { }
            Square extends Shape { }
            UnitImpl extends Unit { }
            // A value with all kinds of attributes.
            //
            // @see is not a tag here
            // Line two.
            Values {
                @@immutable anything: ANY
                @@immutable wildcards: list<ANY>
                @@immutable kinds: set<Kind>
                @@immutable legacy: Legacy
                @@immutable flag: bool
                @@immutable id: uuid
                @@immutable @@min(0.5) @@max(9.5) amount: decimal
                @@immutable @@min(1) @@max(60) timeout: seconds
                @@immutable @@minSize(3) items: map<string, Kind>
                @@immutable @@minSize(2) @@maxSize(2) pair: list<Unit>
                @@immutable shape: Shape
                @@immutable remote: Remote
                @@immutable factory: function<Remote make()>
                @@immutable action: function<void run(x: int32)>
                @@nullable results: streamResult<string>
                @@immutable tiny: int8
                name: string
                string toString()
            }
            Base { @@nullable num: int64 }
            Narrow extends Base { @@override num: int64 }
            Box<$$U extends Unit> {
                @@immutable unit: $$U
                @@immutable units: list<$$U>
                @@finalMethod $$V pick<$$V extends Unit>(value: $$V)
                @@static Box<UnitImpl> of(unit: UnitImpl)
            }
            User {
                @@immutable remote: Remote
                void use(remote: Remote, @@nullable shape: Shape)
                @@deprecated void old()
                @@nullable string maybe()
                void many(values: int32...)
            }
            @@static Remote connect(@@nullable host: string, ports: int32...)
            @@static @@async Remote connectLater()
            @@static void reset()
            @@static @@nullable Remote find()
            @@static @@throws(invalid-argument-error) @@throws(io-error) string load(@@min(1) id: int64)
            @@static Values values(flag: bool)
            constant ANSWER: int32 = 42
            constant GREETING: string = "a \\"quoted\\"\\n\\ttext"
            constant WAIT: seconds = 5
            constant RATE: decimal = 1.5
            """;

    private static final String INSTANCES = """
            instance Remote = connect()
            instance Unit = UnitImpl{symbol: "u"}
            instance Shape = Circle{name: "c"}
            """;

    @TempDir
    Path temp;

    private static List<GeneratedFile> generate() {
        final String markdown = TestSpecs.markdown(SPEC).replace("## Testing", "## Default Instances\n\n```\n"
                + INSTANCES + "```\n\n## Testing");
        final ValidationReport report = new MetaLang().validate(Map.of("f/a.md", markdown));
        assertThat(report.diagnostics()).filteredOn(d -> d.severity() == Severity.ERROR).isEmpty();
        return new TsGenerator().generate(LinkedModel.of(report.model()));
    }

    @Test
    void shouldGenerateTheEdgeCases() {
        // WHEN
        final List<GeneratedFile> files = generate();

        // THEN
        assertThat(TsGeneratorTest.file(files, "/a/constants.ts")).contains("GREETING: string = \"a \\\"quoted\\\"")
                .contains("WAIT: Duration = Duration.ofSeconds(5)").contains("RATE: string = \"1.5\"");
        assertThat(TsGeneratorTest.file(files, "/a/Values.test.ts"))
                .contains("return \"00000000-0000-0000-0000-000000000001\";")
                .contains("return Legacy.OLD;")
                .contains("return new Map([[\"value\", Kind.A], [\"value1\", Kind.A], [\"value2\", Kind.A]]);")
                .contains("return [new UnitImpl({ symbol: \"u\" }), new UnitImpl({ symbol: \"u\" })];")
                .contains("return () => connect(null);")
                .contains("return (p0) => {};")
                .contains("test(\"amount: rejects a value below the minimum\"")
                .contains("test(\"timeout: rejects a value above the maximum\"")
                .contains("test(\"tiny: rejects a value that is no integer (-127.5)\"");
        assertThat(TsGeneratorTest.file(files, "/a/Narrow.ts")).contains("override get num(): bigint {");
        assertThat(TsGeneratorTest.file(files, "/a/errors.ts")).contains("export class IoError extends Error {");
        assertThat(TsGeneratorTest.file(files, "/a/functions.test.ts"))
                .contains("test(\"connectLater() can be called and returns a promise\"");
    }

    @Test
    void shouldBuildAndRunTheGeneratedTests() throws Exception {
        assumeTrue(GeneratedTs.available(), "node and generated/ts/node_modules (npm install) are needed");
        // GIVEN
        final List<GeneratedFile> files = generate();

        // WHEN
        final String build = GeneratedTs.build(files, temp, "@hiero");
        final GeneratedTs.TestRun run = GeneratedTs.test(temp);

        // THEN
        assertThat(build).isEmpty();
        assertThat(run.failures()).isEmpty();
        assertThat(run.passed()).isGreaterThan(10);
    }

    private static final String UNBUILDABLE = """
            namespace b
            abstraction Opaque { void touch() }
            abstraction Named { string label() }
            enum Flavor extends Named {
                SWEET
                string label()
                @@static Flavor parse(text: string)
            }
            Node<$$T extends Node<$$T>> { @@immutable parent: $$T }
            Wrapper<$$Map> {
                @@immutable inner: $$Map
                @@immutable mapper: function<$$Map convert(value: $$Map)>
                @@immutable bounded: list<ANY extends Named>
                @@immutable open: Wrapper<ANY>
            }
            Values {
                @@immutable opaques: list<Opaque>
                @@immutable lookup: map<string, Opaque>
                @@immutable @@min(100) high: int8
                @@immutable @@minSize(0) @@maxSize(4) data: bytes
                @@immutable @@maxLength(4) code: string
                @@immutable named: Named
                @@immutable delete: string
                @@streaming string lines()
                @@async @@nullable string later()
                @@nullable string maybe()
                @@finalMethod $$V pick<$$V extends Opaque>(value: $$V)
                @@throws(timeout) void wait2(in: Opaque)
            }
            Flags { @@immutable @@minSize(3) flags: set<bool> }
            Switches { @@immutable @@minSize(3) switches: map<bool, string> }
            Factory {
                @@immutable opaque: Opaque
                @@static @@throws(timeout) Factory risky(seed: int32)
                @@static Factory make(@@nullable hint: string, opaque: Opaque)
                @@static Factory many(opaque: Opaque, extra: int32...)
            }
            @@static Opaque wrap(first: Opaque)
            constant BYTES: bytes = [1, 2]
            constant NAMES: list<string> = ["x"]
            """;

    private static final String UNBUILDABLE_INSTANCES = """
            instance Named = Flavor.SWEET
            """;

    @Test
    void shouldReportTheValuesThatCannotBeBuilt() throws Exception {
        // GIVEN
        final String markdown = TestSpecs.markdown(UNBUILDABLE).replace("## Testing", "## Default Instances\n\n```\n"
                + UNBUILDABLE_INSTANCES + "```\n\n## Testing");
        final ValidationReport report = new MetaLang().validate(Map.of("g/b.md", markdown));
        assertThat(report.diagnostics()).filteredOn(d -> d.severity() == Severity.ERROR).isEmpty();

        // WHEN
        final List<GeneratedFile> files = new TsGenerator().generate(LinkedModel.of(report.model()));

        // THEN
        assertThat(TsTestGenerator.untested(files)).contains(
                "Flags.test.ts: no valid value for `flags` of Flags: constructor, attribute and method tests",
                "Switches.test.ts: no valid value for `switches` of Switches: constructor, attribute and method tests",
                "Node.test.ts: no type arguments for Node (a type parameter refers to itself)",
                "functions.test.ts: no valid arguments for `wrap(b.Opaque)`");
        assertThat(TsGeneratorTest.file(files, "/b/Wrapper.ts")).contains("export class Wrapper<MapT> {");
        assertThat(TsGeneratorTest.file(files, "/b/Values.ts")).contains("const delete_ = init.delete;");
        assumeTrue(GeneratedTs.available(), "node and generated/ts/node_modules (npm install) are needed");
        assertThat(GeneratedTs.build(files, temp, "@hiero")).isEmpty();
        assertThat(GeneratedTs.test(temp).failures()).isEmpty();
    }

    @Test
    void shouldQuoteAllSpecialCharacters() {
        assertThat(TsLiterals.quote("\"\\\n\r\t\u0001\u2028\u2029x"))
                .isEqualTo("\"\\\"\\\\\\n\\r\\t\\u0001\\u2028\\u2029x\"");
    }

    private static final String SUBTYPES = """
            namespace c
            abstraction Opaque { void touch() }
            abstraction Shape { double area() }
            Plain extends Shape { double area() }
            Gen<$$X> extends Shape {
                @@immutable x: $$X
                double area()
            }
            enum Round extends Shape {
                ONE
                double area()
            }
            abstraction Container<$$X> { @@immutable size: int32 }
            Box<$$X> extends Container<$$X> { }
            abstraction Made {
                @@static @@nullable Made a()
                @@static @@async Made b()
                @@static @@throws(timeout) Made d(x: int32)
                @@static Made e(@@nullable h: string, xs: int32...)
            }
            Flags { @@immutable @@minSize(3) flags: set<bool> }
            Uses {
                @@immutable shape: Shape
                @@immutable anyContainer: Container<ANY>
                @@immutable made: Made
                @@immutable anyClass: type<ANY>
                @@immutable stringClass: type<string>
                @@immutable roundClass: type<Round>
                @@immutable callbacks: list<function<void run()>>
                @@immutable values: list<int64>
                @@throws(timeout) @@async string fetch()
                @@throws(timeout) @@nullable string peek()
                @@throws(timeout) @@streaming string tail()
            }
            StringContainer { @@immutable container: Container<string> }
            ListOfFlags { @@immutable items: list<Flags> }
            MapOfFlags { @@immutable items: map<string, Flags> }
            HasFlags { @@immutable flags: Flags }
            OpaqueClass { @@immutable kind: type<Opaque> }
            // An old type.
            //
            // It is deprecated, use Uses.
            @@deprecated
            Old { @@immutable name: string }
            Config {
                @@immutable data: bytes
                @@immutable tags: set<string>
                @@immutable n: int32
                @@immutable label: string
            }
            constant ANSWER: int32 = 42
            constant NAME: string = "n"
            """;

    private static final String SUBTYPES_INSTANCES = """
            instance Config = Config{data: [1, 2], tags: ["a"], n: ANSWER, label: NAME}
            """;

    @Test
    void shouldFindValuesOfSubtypesFactoriesAndInstances() throws Exception {
        // GIVEN
        final String markdown = TestSpecs.markdown(SUBTYPES).replace("## Testing", "## Default Instances\n\n```\n"
                + SUBTYPES_INSTANCES + "```\n\n## Testing");
        final ValidationReport report = new MetaLang().validate(Map.of("h/c.md", markdown));
        assertThat(report.diagnostics()).filteredOn(d -> d.severity() == Severity.ERROR).isEmpty();

        // WHEN
        final List<GeneratedFile> files = new TsGenerator().generate(LinkedModel.of(report.model()));

        // THEN
        assertThat(TsTestGenerator.untested(files)).containsExactlyInAnyOrder(
                "Flags.test.ts: no valid value for `flags` of Flags: constructor, attribute and method tests",
                "HasFlags.test.ts: no valid value for `flags` of HasFlags: constructor, attribute and method tests",
                "ListOfFlags.test.ts: no valid value for `items` of ListOfFlags: constructor, attribute and method tests",
                "MapOfFlags.test.ts: no valid value for `items` of MapOfFlags: constructor, attribute and method tests",
                "OpaqueClass.test.ts: no valid value for `kind` of OpaqueClass: constructor, attribute and method tests",
                "StringContainer.test.ts: no valid value for `container` of StringContainer: constructor, attribute "
                        + "and method tests");
        assertThat(TsGeneratorTest.file(files, "/c/Old.ts")).contains(" * @deprecated It is deprecated, use Uses.");
        assumeTrue(GeneratedTs.available(), "node and generated/ts/node_modules (npm install) are needed");
        assertThat(GeneratedTs.build(files, temp, "@hiero")).isEmpty();
        assertThat(GeneratedTs.test(temp).failures()).isEmpty();
    }

    @Test
    void shouldUseTheBoundsOfTypeParametersForAnyArguments() throws Exception {
        // GIVEN bounds that refer to the type parameter in all kinds of types
        final String schema = """
                namespace d
                abstraction Sink<$$X> { void put() }
                Bound1<$$H extends Sink<list<$$H>>> { @@immutable size: int32 }
                Bound2<$$H extends Sink<function<void f(x: $$H)>>> { @@immutable size: int32 }
                Bound3<$$H extends Sink<ANY extends $$H>> { @@immutable size: int32 }
                Bound4<$$H extends Sink<map<string, int32>>> { @@immutable size: int32 }
                Bound5<$$H extends Sink<function<$$H f()>>> { @@immutable size: int32 }
                UsesBounds {
                    @@immutable b1: Bound1<ANY>
                    @@immutable b2: Bound2<ANY>
                    @@immutable b3: Bound3<ANY>
                    @@immutable b4: Bound4<ANY>
                    @@immutable b5: Bound5<ANY>
                    void many(callbacks: function<void run()>...)
                }
                """;
        final ValidationReport report = new MetaLang().validate(Map.of("k/d.md", TestSpecs.markdown(schema)));
        assertThat(report.diagnostics()).filteredOn(d -> d.severity() == Severity.ERROR).isEmpty();

        // WHEN
        final List<GeneratedFile> files = new TsGenerator().generate(LinkedModel.of(report.model()));

        // THEN
        assertThat(TsGeneratorTest.file(files, "/d/UsesBounds.ts"))
                .contains("readonly b1: Bound1<any>;", "readonly b2: Bound2<any>;", "readonly b3: Bound3<any>;",
                        "readonly b4: Bound4<Sink<ReadonlyMap<string, number>>>;", "readonly b5: Bound5<any>;")
                .contains("many(...callbacks: Array<() => void>): void");
        assumeTrue(GeneratedTs.available(), "node and generated/ts/node_modules (npm install) are needed");
        assertThat(GeneratedTs.build(files, temp, "@hiero")).isEmpty();
        assertThat(GeneratedTs.test(temp).failures()).isEmpty();
    }
}
