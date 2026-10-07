package org.hiero.sdk.v3.metalang.generator.ts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;
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

class TsGeneratorTest {

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

    static List<GeneratedFile> generate(final String schema) {
        return new TsGenerator().generate(model(Map.of("f/a.md", schema)));
    }

    static String file(final List<GeneratedFile> files, final String suffix) {
        return files.stream().filter(f -> f.path().endsWith(suffix)).findFirst()
                .orElseThrow(() -> new AssertionError("no " + suffix + " in " + files.stream()
                        .map(GeneratedFile::path).toList())).content();
    }

    @Nested
    class Layout {

        @Test
        void shouldCreateOnePackagePerFolderWithOneDirectoryPerNamespace() {
            // WHEN
            final List<GeneratedFile> files = new TsGenerator(new TsGeneratorConfig("@acme", "1.2.3")).generate(model(
                    Map.of("base/b.md", "namespace b\nPoint { @@immutable x: int32 }\n",
                            "client/c.md", "namespace c.sub\nrequires {Point} from b\nLine { @@immutable from: Point\n"
                                    + "    @@immutable length: duration }\n")));

            // THEN
            assertThat(files).extracting(GeneratedFile::path).containsExactly(".gitignore", "package.json",
                    "packages/base/package.json", "packages/base/src/b/Point.test.ts", "packages/base/src/b/Point.ts",
                    "packages/base/src/b/index.ts", "packages/base/tsconfig.json",
                    "packages/client/package.json", "packages/client/src/c/sub/Line.test.ts",
                    "packages/client/src/c/sub/Line.ts", "packages/client/src/c/sub/index.ts",
                    "packages/client/tsconfig.json", "tsconfig.base.json", "tsconfig.json");
            assertThat(files).allMatch(f -> f.content().lines().findFirst().orElseThrow()
                    .contains(TsGenerator.MARKER));
            assertThat(file(files, "packages/client/package.json")).contains("\"name\": \"@acme/client\"")
                    .contains("\"version\": \"1.2.3\"")
                    .contains("\"./c/sub\": {\n      \"types\": \"./dist/c/sub/index.d.ts\"")
                    .contains("\"dependencies\": {\n    \"@acme/support\": \"1.2.3\",\n    \"@acme/base\": \"1.2.3\"\n  }");
            // the hand-written support package is a dependency of the packages that use it: here only client
            assertThat(file(files, "packages/base/package.json")).doesNotContain("dependencies")
                    .doesNotContain("support");
            assertThat(file(files, "packages/client/tsconfig.json"))
                    .contains("{ \"path\": \"../../../../sdk-ts/support\" },\n    { \"path\": \"../base\" }");
            assertThat(file(files, "packages/base/tsconfig.json")).doesNotContain("support");
            assertThat(file(files, "/c/sub/Line.ts"))
                    .contains("import type { Point } from \"@acme/base/b\";")
                    .contains("import type { Duration } from \"@acme/support\";")
                    .contains("readonly #length: Duration;");
            assertThat(file(files, "/c/sub/index.ts")).contains("export * from \"./Line.js\";");
            // the workspace links and builds the support package first
            assertThat(file(files, "package.json")).contains("\"workspaces\": [\n    \"../../sdk-ts/support\",\n");
            assertThat(files.stream().filter(f -> f.path().equals("tsconfig.json")).findFirst().orElseThrow().content())
                    .contains("{ \"path\": \"../../sdk-ts/support\" },\n");
            assertThat(new TsGenerator(new TsGeneratorConfig("@acme", "1.2.3", "/opt/support")).generate(model(
                    Map.of("f/a.md", "namespace a\nX { @@immutable d: duration }\n"))))
                    .filteredOn(f -> f.path().endsWith("tsconfig.json"))
                    .allSatisfy(f -> assertThat(f.content()).contains("{ \"path\": \"/opt/support\" }"));
            assertThat(files.stream().filter(f -> f.path().equals("package.json")).findFirst().orElseThrow()
                    .content()).contains("\"typescript\": \"" + TsProjectGenerator
                    .TYPESCRIPT_VERSION + "\"");
        }

        @Test
        void shouldWireTheProtobufRuntimeOnlyIntoTheConfiguredPackages() {
            // GIVEN the client folder is configured to need the protobuf messages
            final TsGeneratorConfig config = new TsGeneratorConfig("@acme", "1.2.3",
                    TsGeneratorConfig.DEFAULT_SUPPORT, java.util.Set.of("client"));

            // WHEN
            final List<GeneratedFile> files = new TsGenerator(config).generate(model(
                    Map.of("base/b.md", "namespace b\nPoint { @@immutable x: int32 }\n",
                            "client/c.md", "namespace c\nLine { @@immutable n: int32 }\n")));

            // THEN the configured package depends on the runtime, the other one does not
            assertThat(file(files, "packages/client/package.json"))
                    .contains("\"" + TsProjectGenerator.PROTOBUF_RUNTIME + "\": \""
                            + TsProjectGenerator.PROTOBUF_RUNTIME_VERSION + "\"");
            assertThat(file(files, "packages/base/package.json"))
                    .doesNotContain(TsProjectGenerator.PROTOBUF_RUNTIME);

            // AND the messages are no subpath of "exports": that is what keeps them inside the package
            assertThat(file(files, "packages/client/package.json"))
                    .doesNotContain(TsProjectGenerator.PROTOBUF_DIRECTORY);

            // AND they are build output, like dist
            assertThat(file(files, ".gitignore"))
                    .contains("packages/*/" + TsProjectGenerator.PROTOBUF_DIRECTORY + "/");
        }

        @Test
        void shouldGenerateNoProtobufWiringWithoutConfiguration() {
            // WHEN
            final List<GeneratedFile> files = generate("namespace a\nX { @@immutable n: int32 }\n");

            // THEN
            assertThat(file(files, "packages/f/package.json")).doesNotContain(TsProjectGenerator.PROTOBUF_RUNTIME);
            assertThat(file(files, ".gitignore")).doesNotContain(TsProjectGenerator.PROTOBUF_DIRECTORY);
        }

        @Test
        void shouldReadAndValidateTheProtobufFolders() throws Exception {
            // GIVEN
            final Path config = temp.resolve("protobuf.properties");

            // WHEN / THEN
            Files.writeString(config, "ts.protobuf = consensus-node-client, mirror-node-client\n");
            assertThat(TsGeneratorConfig.load(config).protobuf())
                    .containsExactlyInAnyOrder("consensus-node-client", "mirror-node-client");
            Files.writeString(config, "ts.protobuf = Node_Client\n");
            assertThatThrownBy(() -> TsGeneratorConfig.load(config))
                    .isInstanceOfSatisfying(GenerationException.class, e -> assertThat(e.problems()).containsExactly(
                            "'Node_Client' in ts.protobuf is no spec folder (lowercase, '-' separated)"));
        }

        @Test
        void shouldRejectInvalidConfigurationsAndFolders() throws Exception {
            assertThatThrownBy(() -> new TsGeneratorConfig("hiero", "x"))
                    .isInstanceOfSatisfying(GenerationException.class, e -> assertThat(e.problems()).hasSize(2));
            final Path config = temp.resolve("generator.properties");
            Files.writeString(config, "java.groupId = x\nts.scope = @x\nts.version = 2.0.0\n");
            assertThat(TsGeneratorConfig.load(config)).isEqualTo(new TsGeneratorConfig("@x", "2.0.0"));
            Files.writeString(config, "ts.support = ../support\n");
            assertThat(TsGeneratorConfig.load(config).support()).isEqualTo("../support");
            assertThatThrownBy(() -> new TsGeneratorConfig("@x", "1.0.0", " "))
                    .isInstanceOf(GenerationException.class);
            Files.writeString(config, "ts.unknown = 1\n");
            assertThatThrownBy(() -> TsGeneratorConfig.load(config)).isInstanceOf(GenerationException.class);
            assertThatThrownBy(() -> new TsGenerator().generate(model(Map.of(
                    "one/a.md", "namespace a\nabstraction A { @@throws(remote-error) void run() }\n",
                    "two/b.md", "namespace b\nabstraction B { @@throws(remote-error) void run() }\n"))))
                    .isInstanceOfSatisfying(GenerationException.class, e -> assertThat(e.problems())
                            .containsExactly("Error 'remote-error' is used in the packages [one, two], but none of "
                                    + "them is required by all others; its error class has no home"));
        }
    }

    @Nested
    class Types {

        @Test
        void shouldMapTypesToInterfacesClassesAndEnumClasses() {
            // WHEN
            final List<GeneratedFile> files = generate("""
                    namespace a
                    // A color.
                    enum Color(code: int64) {
                        RED(1)
                        // Do not use it any more, it is deprecated.
                        @@deprecated GREEN(2)
                        string label()
                    }
                    abstraction Shape {
                        @@immutable @@nullable name: string
                        tag: string
                        double area()
                        @@static Shape unit()
                    }
                    Circle extends Shape {
                        @@immutable @@min(0.5) radius: double
                        @@immutable @@minSize(1) @@maxSize(3) data: bytes
                        @@immutable tags: list<string>
                        @@immutable labels: set<string>
                        @@immutable sizes: map<string, uint8>
                        @@immutable @@default(7) level: int32
                        @@immutable @@nullable created: zonedDateTime
                        @@immutable @@pattern("^/[^\\s]*$") path: string
                        @@immutable @@urlPattern url: string
                        @@immutable @@nullable callback: function<string convert(value: int32)>
                        @@immutable @@nullable kind: type<Circle>
                        @@nullable note: string
                        double area()
                        @@async string fetch(id: uint64)
                        @@streaming string watch()
                        @@throws(not-found-error, illegal-format) string find(key: string)
                        string find(key: string, @@nullable fallback: string)
                    }
                    constant ORIGIN: Circle = Circle{radius: 1.5, data: [1], tags: [], labels: [], sizes: [], \
                    created: null, path: "/", url: "https://x.org", callback: null, kind: null, tag: "t"}
                    constant LIMIT: uint64 = 10
                    @@static Circle create(radius: double)
                    """);

            // THEN
            assertThat(file(files, "/a/Color.ts"))
                    .contains("/**\n * A color.\n */\nexport class Color {")
                    .contains("    static readonly RED: Color = new Color(\"RED\", 1n);")
                    .contains("    /**\n     * @deprecated Do not use it any more, it is deprecated.\n     */\n"
                            + "    static readonly GREEN: Color = new Color(\"GREEN\", 2n);")
                    .contains("    private constructor(name: string, code: bigint) {")
                    .contains("    static values(): ReadonlyArray<Color> {")
                    .contains("    static valueOf(name: string): Color {")
                    .contains("    label(): string {\n        throw new Error(\"Not implemented yet: Color.label\");");
            assertThat(file(files, "/a/Shape.ts"))
                    .contains("export interface Shape {\n    readonly name: string | null;\n\n    tag: string;\n\n"
                            + "    area(): number;\n}")
                    .contains("export namespace Shape {\n    export function unit(): Shape {");
            assertThat(file(files, "/a/Circle.ts"))
                    .contains("export class Circle implements Shape {")
                    .contains("    readonly #radius: number;")
                    .contains("    #note: string | null;")
                    .contains("        readonly level?: number;")
                    .contains("        readonly note?: string | null;")
                    .contains("        readonly callback?: ((value: number) => string) | null;")
                    .contains("        readonly kind?: AbstractConstructor<Circle> | null;")
                    .contains("        if (radius < 0.5) {\n            throw new RangeError(\"radius must be at least "
                            + "0.5\");")
                    .contains("        if (data.length < 1) {")
                    .contains("        if (!new RegExp(\"^/[^\\\\s]*$\").test(path)) {")
                    .contains("        if (!isAbsoluteUrl(url)) {")
                    .contains("        const level = init.level === undefined ? 7 : init.level;")
                    .contains("        if (!Number.isInteger(level) || level < -2147483648 || level > 2147483647) {")
                    .contains("        this.#data = data.slice();")
                    .contains("        this.#tags = Object.freeze([...tags]);")
                    .contains("        this.#labels = new Set(labels);")
                    .contains("        this.#created = created === null ? null : new Date(created.getTime());")
                    .contains("    get labels(): ReadonlySet<string> {\n        return new Set(this.#labels);")
                    .contains("    set note(value: string | null) {")
                    .contains("        Object.freeze(this);")
                    .contains("    fetch(id: bigint): Promise<string> {")
                    .contains("    watch(): AsyncIterable<string> {")
                    .contains("     * @throws NotFoundError if a not found error occurs\n"
                            + "     * @throws RangeError if an illegal format error occurs\n")
                    .contains("    find(key: string): string;\n")
                    .contains("    find(key: string, fallback: string | null): string;\n"
                            + "    find(...args: any[]): any {")
                    .contains("function isAbsoluteUrl(value: string): boolean {");
            assertThat(file(files, "/a/constants.ts"))
                    .contains("export const ORIGIN: Circle = new Circle({ tag: \"t\", radius: 1.5, data: new "
                            + "Uint8Array([1]), tags: Object.freeze([]), labels: new Set([]), sizes: new Map(), "
                            + "created: null, path: \"/\", url: \"https://x.org\", callback: null, kind: null });")
                    .contains("export const LIMIT: bigint = 10n;");
            assertThat(file(files, "/a/functions.ts")).contains("export function create(radius: number): Circle {");
            assertThat(file(files, "/a/errors.ts")).contains("export class NotFoundError extends Error {")
                    .doesNotContain("IllegalFormat");
        }

        @Test
        void shouldExtendClassesAndNarrowInheritedAttributes() {
            final List<GeneratedFile> files = generate("""
                    namespace a
                    Base { @@immutable @@nullable num: uint64
                        @@nullable label: string }
                    Derived extends Base { @@immutable @@override num: uint64
                        @@immutable extra: int8 }
                    """);
            assertThat(file(files, "/a/Base.ts")).doesNotContain("Object.freeze(this)");
            assertThat(file(files, "/a/Derived.ts"))
                    .contains("export class Derived extends Base {")
                    .contains("        super(init);\n        const num = init.num;\n        if (num === null || num === "
                            + "undefined) {")
                    .contains("    override get num(): bigint {\n        return super.num as bigint;")
                    .contains("        this.#extra = extra;\n        Object.freeze(this);");
        }

        @Test
        void shouldMapSealedAbstractionsToUnionTypes() {
            final List<GeneratedFile> files = generate("""
                    namespace a
                    // A shape.
                    @@sealed(Circle, Square)
                    abstraction Shape { double area() }
                    Circle extends Shape { @@immutable r: double
                        double area() }
                    Square extends Shape { @@immutable side: double
                        double area() }
                    """);
            assertThat(file(files, "/a/Shape.ts")).contains("/**\n * A shape.\n */\n"
                    + "export type Shape = Circle | Square;\n");
            assertThat(file(files, "/a/Circle.ts")).contains("export class Circle {");
        }

        @Test
        void shouldDeferTypesWithoutMapping() {
            // a type that is not declared: the validator reports it, the generator defers the type
            final LinkedModel model = LinkedModel.of(new MetaLang().validate(Map.of("f/a.md", TestSpecs.markdown(
                    "namespace a\nBroken { @@immutable x: Unknown }\nUser { @@immutable broken: Broken }\n")))
                    .model());
            assertThat(new TsGenerator().deferredTypes(model)).containsOnlyKeys(
                    new org.hiero.sdk.v3.metalang.model.QualifiedName("a", "Broken"),
                    new org.hiero.sdk.v3.metalang.model.QualifiedName("a", "User"));
            assertThat(new TsGenerator().generate(model)).noneMatch(f -> f.path().contains("Broken"));
        }
    }

    @Nested
    class RealSpecs {

        @Test
        void shouldGenerateDeterministicallyAlsoInAnotherJvm() throws Exception {
            // GIVEN
            final List<GeneratedFile> expected = new TsGenerator().generate(LinkedModel.of(new MetaLang()
                    .validate(SPECS).model()));

            // WHEN another JVM with other identity hash codes generates them
            final Path out = temp.resolve("other-jvm");
            final Process process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java")
                    .toString(), "-XX:+UnlockExperimentalVMOptions", "-XX:hashCode=2", "-cp",
                    System.getProperty("java.class.path"), "org.hiero.sdk.v3.metalang.generator.ts.GenerateMain", SPECS.toString(), out.toString())
                    .redirectErrorStream(true).start();
            final String log = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            // THEN
            assertThat(process.waitFor()).as(log).isZero();
            for (final GeneratedFile file : expected) {
                assertThat(Files.readString(out.resolve(file.path()), StandardCharsets.UTF_8)).as(file.path())
                        .isEqualTo(file.content());
            }
            try (Stream<Path> files = Files.walk(out)) {
                assertThat(files.filter(Files::isRegularFile).count()).isEqualTo(expected.size());
            }
            assertThat(expected).hasSizeGreaterThan(500);
            assertThat(new TsGenerator().deferredTypes(LinkedModel.of(new MetaLang().validate(SPECS).model())))
                    .isEmpty();
        }

        @Test
        void generatedPackagesShouldCompileAndTheTestsOnlyFailForStubs() throws Exception {
            assumeTrue(GeneratedTs.available(), "node and generated/ts/node_modules (npm install) are needed");
            // GIVEN
            final List<GeneratedFile> files = new TsGenerator().generate(LinkedModel.of(new MetaLang()
                    .validate(SPECS).model()));

            // WHEN
            final String build = GeneratedTs.build(files, temp, "@hiero");
            final GeneratedTs.TestRun run = GeneratedTs.test(temp);

            // THEN the strict compiler accepts all packages and tests; everything the generator implements passes
            assertThat(build).isEmpty();
            assertThat(run.failures()).isEmpty();
            assertThat(run.passed()).isGreaterThan(1900);
            assertThat(run.stubs()).isNotEmpty();
        }
    }
}
