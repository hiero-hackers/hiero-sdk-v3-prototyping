package org.hiero.sdk.v3.metalang.generator.rust;

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

class RustGeneratorTest {

    private static final Path SPECS = Path.of(System.getProperty("spec.root", "../../../spec"));

    /** A spec with every kind of declaration the generator maps. */
    static final String FEATURES = """
            namespace a
            // A color.
            enum Color(code: int32, label: string) {
                // Red.
                RED(1, "r")
                // Use RED, this is deprecated.
                @@deprecated GREEN(2, "g")
                string describe()
                @@static Color parse(text: string)
            }
            enum Level(color: Color) {
                LOW(Color.RED)
            }
            abstraction Unit {
                @@immutable symbol: string
            }
            enum Currency(symbol: string) extends Unit {
                EURO("e")
            }
            // A token with a self type.
            abstraction Token<$$Self extends Token<$$Self, $$Unit>, $$Unit extends Unit> {
                @@immutable amount: int64
                @@immutable unit: $$Unit
                $$Self to(target: $$Unit)
            }
            Coin extends Token<Coin, Currency> {
                int64 cents()
            }
            abstraction Named<$$T> {
                @@immutable value: $$T
                @@nullable label: string
                $$T get()
            }
            NamedText extends Named<string> {
            }
            abstraction Shape {
                @@immutable name: string
                @@nullable note: string
                double area()
                @@async string render(scale: int32)
                @@streaming string lines()
                @@throws(not-found-error) string find(key: string)
                @@throws(not-found-error, io-error) void load()
                @@nullable string maybe()
                @@static Shape unit()
                @@finalMethod string title()
                @@finalMethod $$S pick<$$S extends Unit>(value: $$S)
                string toString()
            }
            // A circle.
            Circle extends Shape {
                @@immutable @@min(0.5) @@max(100) radius: double
            }
            Wallet {
                @@immutable token: Token<ANY, ANY>
                @@immutable named: Named<ANY>
                @@immutable shapes: list<Shape>
                @@nullable owner: Circle
            }
            Values {
                @@immutable @@minLength(2) @@maxLength(5) code: string
                @@immutable @@pattern("^[a-z]+$") word: string
                @@immutable @@urlPattern url: string
                @@immutable @@minSize(1) @@maxSize(3) data: bytes
                @@immutable @@minSize(1) tags: list<string>
                @@immutable labels: set<string>
                @@immutable sizes: map<string, int32>
                @@immutable weights: map<Shape, int32>
                @@immutable id: uuid
                @@immutable day: date
                @@immutable hour: time
                @@immutable moment: dateTime
                @@immutable stamp: zonedDateTime
                @@immutable @@min(1) @@max(60) delay: seconds
                @@immutable @@min(1) @@max(9) amount: decimal
                @@immutable small: int24
                @@immutable huge: uint256
                @@immutable type: string
                @@immutable action: function<void run(x: int32)>
                @@immutable kind: type<Circle>
                @@nullable results: streamResult<string>
                @@min(1) @@max(9) count: int32
                @@immutable @@default(7) level: int32
                @@deprecated @@immutable old: bool
            }
            @@sealed(Leaf, Node)
            abstraction Tree {
            }
            Leaf extends Tree {
                @@immutable value: int32
            }
            Node extends Tree {
                @@immutable children: list<Tree>
            }
            Base {
                @@immutable @@nullable num: int64
                @@immutable text: string
            }
            Derived extends Base {
                @@immutable @@override num: int64
            }
            Widths {
                @@immutable a: int12
                @@immutable b: uint48
                @@immutable c: int100
                @@immutable d: int16
                @@immutable e: uint128
                @@immutable shapes: set<Shape>
                @@immutable handlers: map<string, function<void handle()>>
                @@immutable boxes: list<Holder<ANY extends Unit>>
            }
            Holder<$$U extends Unit> { @@immutable unit: $$U }
            abstraction Top { @@immutable id: int32 }
            abstraction Left extends Top { }
            abstraction Right extends Top { }
            Diamond extends Left, Right {
                string toString(format: int32)
            }
            abstraction Pinger { void ping(x: int32) }
            Echo extends Pinger { void ping() }
            @@sealed(Leaf2, Pinger2)
            abstraction Figure { }
            Leaf2 extends Figure { }
            abstraction Pinger2 extends Figure { }
            Service {
                @@immutable name: string
                void send(target: string)
                void send(target: string, retries: int32)
                void send(target: Circle, retries: int32)
                void sendAll(targets: string...)
            }
            @@static Circle circle(radius: double)
            @@static Circle circle(radius: double, name: string)
            @@static @@async Shape later()
            @@static @@throws(illegal-format) Values parse(text: string)
            constant ANSWER: int32 = 42
            constant NAME: string = "n"
            constant RED: Color = Color.RED
            constant ORIGIN: Base = Base{text: "o"}
            constant WAIT: seconds = 1.5
            constant RATE: decimal = 1.25
            """;

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
        return new RustGenerator().generate(model(Map.of("f/a.md", schema)));
    }

    static String file(final List<GeneratedFile> files, final String suffix) {
        return files.stream().filter(f -> f.path().endsWith(suffix)).findFirst()
                .orElseThrow(() -> new AssertionError("no " + suffix + " in " + files.stream()
                        .map(GeneratedFile::path).toList())).content();
    }

    @Nested
    class Layout {

        @Test
        void shouldCreateOneCratePerFolderWithOneModulePerNamespace() {
            // GIVEN
            final LinkedModel model = model(Map.of(
                    "base/a.md", "namespace a\nX { @@immutable x: int32 }\n@@static X make()\n",
                    "base/b.md", "namespace a.sub\nY { @@immutable y: string }\n",
                    "client/c.md", "namespace consensus.client\nrequires {X} from a\nZ { @@immutable x: X }\n"));

            // WHEN
            final List<GeneratedFile> files = new RustGenerator(new RustGeneratorConfig("acme", "1.2.3"))
                    .generate(model);

            // THEN
            assertThat(files).allMatch(f -> f.content().lines().findFirst().orElseThrow()
                    .contains(RustGenerator.MARKER));
            assertThat(file(files, "Cargo.toml")).contains("members = [\n    \"crates/base\",\n    \"crates/client\",\n]")
                    .contains("version = \"1.2.3\"")
                    .contains("acme-base = { path = \"crates/base\", version = \"1.2.3\" }");
            assertThat(file(files, "crates/client/Cargo.toml")).contains("name = \"acme-client\"")
                    .contains("[dependencies]\nacme-base.workspace = true\n");
            assertThat(file(files, "crates/base/Cargo.toml")).contains("futures-core.workspace = true");
            assertThat(file(files, "crates/base/src/lib.rs")).contains("pub mod a;\npub mod support;\n")
                    .contains("#![allow(deprecated)]");
            assertThat(file(files, "crates/base/src/a/mod.rs")).contains("mod functions;\nmod x;\npub mod sub;\n")
                    .contains("pub use functions::make;\npub use x::X;\n");
            assertThat(file(files, "crates/client/src/consensus/mod.rs")).contains("pub mod client;");
            assertThat(file(files, "crates/client/src/consensus/client/z.rs")).contains("use acme_base::a::X;");
            assertThat(file(files, "crates/base/src/support/mod.rs"))
                    .contains("pub use future::{BoxFuture, BoxStream};")
                    .contains("pub use invalid_argument_error::InvalidArgumentError;");
            assertThat(file(files, "crates/base/src/support/url.rs")).contains("pub fn is_absolute_url");
            assertThat(file(files, "crates/client/tests/api/main.rs")).contains("mod helpers;\nmod consensus;\n");
            assertThat(file(files, ".gitignore")).contains("/target/\n/Cargo.lock\n");
        }

        @Test
        void shouldRejectSpecsWithoutCommonCrateOrErrorHome() {
            assertThatThrownBy(() -> new RustGenerator().generate(model(Map.of(
                    "one/a.md", "namespace a\nX { @@immutable x: int32 }\n",
                    "two/b.md", "namespace b\nY { @@immutable y: int32 }\n"))))
                    .isInstanceOfSatisfying(GenerationException.class, e -> assertThat(e.problems())
                            .containsExactly("The support module needs a crate that all crates require, but there "
                                    + "is none among [one, two]"));
            assertThatThrownBy(() -> new RustGenerator().generate(model(Map.of(
                    "base/c.md", "namespace c\nC { @@immutable c: int32 }\n",
                    "one/a.md", "namespace a\nrequires {C} from c\nabstraction A { @@throws(remote-error) C run() }\n",
                    "two/b.md", "namespace b\nrequires {C} from c\nabstraction B { @@throws(remote-error) C run() }\n"))))
                    .isInstanceOfSatisfying(GenerationException.class, e -> assertThat(e.problems())
                            .containsExactly("Error 'remote-error' is used in the crates [one, two], but none of "
                                    + "them is required by all others; its error type has no home"));
        }

        @Test
        void shouldReadAndValidateTheConfiguration() throws Exception {
            // GIVEN
            final Path file = temp.resolve("generator.properties");
            Files.writeString(file, "java.interfaces = a.B\nrust.cratePrefix = acme\nrust.version = 2.0.0\n");

            // WHEN / THEN
            assertThat(RustGeneratorConfig.load(file)).isEqualTo(new RustGeneratorConfig("acme", "2.0.0"));
            assertThat(new RustGeneratorConfig("acme", "2.0.0").libraryName("consensus-node-client"))
                    .isEqualTo("acme_consensus_node_client");
            Files.writeString(file, "rust.other = 1\n");
            assertThatThrownBy(() -> RustGeneratorConfig.load(file)).isInstanceOfSatisfying(
                    GenerationException.class, e -> assertThat(e.problems()).containsExactly(
                            "Unknown key 'rust.other' (known: rust.cratePrefix, rust.version)"));
            assertThatThrownBy(() -> new RustGeneratorConfig("Acme!", "x")).isInstanceOfSatisfying(
                    GenerationException.class, e -> assertThat(e.problems()).hasSize(2));
        }
    }

    @Nested
    class Types {

        @Test
        void shouldMapEveryKindOfDeclaration() {
            // WHEN
            final List<GeneratedFile> files = generate(FEATURES);

            // THEN
            assertThat(file(files, "/src/a/color.rs"))
                    .contains("/// A color.\n#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash)]\npub enum Color {")
                    .contains("    #[deprecated(note = \"Use RED, this is deprecated.\")]\n    Green,")
                    .contains("pub const fn code(&self) -> i32 {")
                    .contains("pub const fn label(&self) -> &'static str {")
                    .contains("pub fn parse(text: String) -> Color {")
                    .contains("impl FromStr for Color {");
            assertThat(file(files, "/src/a/level.rs")).contains("Level::Low => Color::Red,");
            assertThat(file(files, "/src/a/currency.rs")).contains("impl Unit for Currency {")
                    .contains("fn symbol(&self) -> &str {\n        self.symbol()\n    }");
            assertThat(file(files, "/src/a/token.rs"))
                    .contains("pub trait Token: Debug + Send + Sync {")
                    .contains("fn unit(&self) -> Arc<dyn Unit>;")
                    .contains("fn to(&self, target: Arc<dyn Unit>) -> Self where Self: Sized;");
            assertThat(file(files, "/src/a/coin.rs")).contains("pub fn unit(&self) -> Currency {")
                    .contains("pub fn to(&self, target: Currency) -> Coin {")
                    .contains("fn unit(&self) -> Arc<dyn Unit> {\n        Arc::new(self.unit())\n    }");
            // Named<ANY> erases the type parameter: the trait returns the value as `Any`
            assertThat(file(files, "/src/a/named.rs")).contains("pub trait Named: Debug + Send + Sync {")
                    .contains("fn value(&self) -> Arc<dyn Any + Send + Sync>;")
                    .contains("fn set_label(&mut self, label: Option<String>);");
            assertThat(file(files, "/src/a/named_text.rs")).contains("impl Named for NamedText {")
                    .contains("pub fn value(&self) -> &str {")
                    .contains("Arc::new(self.value().to_string())")
                    .contains("pub fn set_label(&mut self, label: Option<String>) -> &mut Self {");
            assertThat(file(files, "/src/a/shape.rs"))
                    .contains("pub trait Shape: Debug + Send + Sync + Display {")
                    .contains("fn render(&self, scale: i32) -> BoxFuture<'_, String>;")
                    .contains("fn lines(&self) -> BoxStream<'_, String>;")
                    .contains("fn find(&self, key: String) -> Result<String, NotFoundError>;")
                    .contains("fn load(&self) -> Result<(), ShapeLoadError>;")
                    .contains("fn maybe(&self) -> Option<String>;")
                    .contains("fn pick<S: Unit>(&self, value: S) -> S where Self: Sized {")
                    .doesNotContain("fn title(&self) -> String;")
                    .contains("impl dyn Shape {\n    pub fn unit() -> Arc<dyn Shape> {")
                    .contains("    fn title(&self) -> String {\n        todo!(\"Shape.title\")")
                    .contains("pub trait ShapeExt: Shape {")
                    .contains("impl<ExtSelf: Shape + ?Sized> ShapeExt for ExtSelf {}");
            assertThat(file(files, "/src/a/circle.rs")).contains("#[derive(Debug, Clone, PartialEq)]")
                    .contains("pub fn new(name: String, note: Option<String>, radius: f64) -> Result<Self, "
                            + "InvalidArgumentError> {")
                    .contains("if radius < 0.5 {").contains("pub async fn render(&self, scale: i32) -> String {")
                    .contains("Box::pin(self.render(scale))")
                    .contains("impl Display for Circle {");
            assertThat(file(files, "/src/a/wallet.rs")).contains("token: Arc<dyn Token>,")
                    .contains("named: Arc<dyn Named>,").contains("owner: Option<Circle>,")
                    .contains("#[derive(Debug, Clone)]");
            assertThat(file(files, "/src/a/values.rs"))
                    .contains("r#type: String,").contains("pub fn r#type(&self) -> &str {")
                    .contains("weights: Vec<(Arc<dyn Shape>, i32)>,")
                    .contains("small: i32,").contains("huge: ethnum::U256,")
                    .contains("if !(-8388608..=8388607).contains(&small) {")
                    .contains("if code.chars().count() < 2 {").contains("if data.is_empty() {")
                    .contains("static PATTERN: std::sync::LazyLock<regex::Regex>")
                    .contains("if !is_absolute_url(&url) {")
                    .contains("if delay < std::time::Duration::from_millis(1000) {")
                    .contains("if count < 1 {")
                    .contains("pub fn set_count(&mut self, count: i32) -> Result<&mut Self, "
                            + "InvalidArgumentError> {")
                    .contains("results: Option<StreamItem<String>>,").doesNotContain("#[derive(")
                    .contains("action: Arc<dyn Fn(i32) + Send + Sync>,")
                    .contains("impl Debug for Values {").contains(".finish_non_exhaustive()")
                    .contains("#[allow(clippy::too_many_arguments)]");
            assertThat(file(files, "/src/a/tree.rs")).contains("pub enum Tree {\n    Leaf(Leaf),\n    Node(Node),\n}")
                    .contains("impl From<Leaf> for Tree {");
            assertThat(file(files, "/src/a/derived.rs")).contains("pub fn num(&self) -> i64 {")
                    .contains("impl From<Derived> for Base {")
                    .contains("Base::new(Some(value.num()), value.text().to_string())");
            assertThat(file(files, "/src/a/service.rs")).contains("pub fn send(&self, target: String) {")
                    .contains("pub fn send_with_target_and_retries(&self, target: String, retries: i32) {")
                    .contains("pub fn send_with_target_and_retries_2(&self, target: Circle, retries: i32) {")
                    .contains("pub fn send_all(&self, targets: Vec<String>) {");
            assertThat(file(files, "/src/a/functions.rs")).contains("pub fn circle(radius: f64) -> Circle {")
                    .contains("pub fn circle_with_radius_and_name(radius: f64, name: String) -> Circle {")
                    .contains("pub async fn later() -> Arc<dyn Shape> {")
                    .contains("pub fn parse(text: String) -> Result<Values, InvalidArgumentError> {");
            assertThat(file(files, "/src/a/constants.rs")).contains("pub const ANSWER: i32 = 42;")
                    .contains("pub const NAME: &str = \"n\";").contains("pub const RED: Color = Color::Red;")
                    .contains("pub static ORIGIN: LazyLock<Base> = LazyLock::new(|| Base::new(None, "
                            + "\"o\".to_string()));")
                    .contains("pub const WAIT: std::time::Duration = std::time::Duration::from_millis(1500);")
                    .contains("pub static RATE: LazyLock<rust_decimal::Decimal> = "
                            + "LazyLock::new(|| rust_decimal::Decimal::from_i128_with_scale(125, 2));");
            assertThat(file(files, "/src/a/errors.rs")).contains("pub struct NotFoundError {")
                    .contains("pub enum ShapeLoadError {\n    /// The error `not-found-error`.\n    NotFound(NotFoundError),\n"
                            + "    /// The error `io-error`.\n    Io(IoError),\n}")
                    .contains("impl From<NotFoundError> for ShapeLoadError {");
            assertThat(file(files, "crates/f/Cargo.toml")).contains("chrono.workspace = true")
                    .contains("ethnum.workspace = true").contains("regex.workspace = true")
                    .contains("rust_decimal.workspace = true").contains("uuid.workspace = true");
        }

        @Test
        void shouldCompileWithoutWarningsAndTheTestsOnlyFailForStubs() throws Exception {
            assumeTrue(GeneratedRust.available(), "cargo is needed");
            // GIVEN
            final List<GeneratedFile> files = generate(FEATURES);

            // WHEN
            final String build = GeneratedRust.build(files, temp);
            final GeneratedRust.TestRun run = GeneratedRust.test(temp);

            // THEN
            assertThat(build).isEmpty();
            assertThat(run.failures()).isEmpty();
            assertThat(run.passed()).isGreaterThan(40);
            assertThat(run.stubs()).isNotEmpty();
        }

        @Test
        void shouldDeferTypesWithoutMapping() {
            // WHEN a type refers to an unknown type (the specs have an error)
            final LinkedModel model = LinkedModel.of(new MetaLang().validate(Map.of("f/a.md", TestSpecs.markdown(
                    "namespace a\nX { @@immutable x: Missing }\nY { @@immutable x: X }\n@@static X make()\n"
                            + "constant C: X = X{x: 1}\n"))).model());

            // THEN
            assertThat(new RustGenerator().deferredTypes(model)).containsOnlyKeys(
                    new org.hiero.sdk.v3.metalang.model.QualifiedName("a", "X"),
                    new org.hiero.sdk.v3.metalang.model.QualifiedName("a", "Y"),
                    new org.hiero.sdk.v3.metalang.model.QualifiedName("a", "make()"),
                    new org.hiero.sdk.v3.metalang.model.QualifiedName("a", "C"));
        }
    }

    @Nested
    class RealSpecs {

        @Test
        void shouldGenerateDeterministicallyAlsoInAnotherJvm() throws Exception {
            // GIVEN
            final List<GeneratedFile> expected = new RustGenerator().generate(LinkedModel.of(new MetaLang()
                    .validate(SPECS).model()));

            // WHEN another JVM with other identity hash codes generates them
            final Path out = temp.resolve("other-jvm");
            final Process process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java")
                    .toString(), "-XX:+UnlockExperimentalVMOptions", "-XX:hashCode=2", "-cp",
                    System.getProperty("java.class.path"), "org.hiero.sdk.v3.metalang.generator.rust.GenerateMain",
                    SPECS.toString(), out.toString()).redirectErrorStream(true).start();
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
            assertThat(new RustGenerator().deferredTypes(LinkedModel.of(new MetaLang().validate(SPECS).model())))
                    .isEmpty();
        }

        @Test
        void generatedCratesShouldCompileAndTheTestsOnlyFailForStubs() throws Exception {
            assumeTrue(GeneratedRust.available(), "cargo is needed");
            // GIVEN
            final List<GeneratedFile> files = new RustGenerator().generate(LinkedModel.of(new MetaLang()
                    .validate(SPECS).model()));

            // WHEN
            final String build = GeneratedRust.build(files, temp);
            final GeneratedRust.TestRun run = GeneratedRust.test(temp);

            // THEN the compiler accepts all crates and tests without warnings; everything the generator implements
            // passes
            assertThat(build).isEmpty();
            assertThat(run.failures()).isEmpty();
            assertThat(run.passed()).isGreaterThan(800);
            assertThat(run.stubs()).isNotEmpty();
            assertThat(RustTestGenerator.untested(files)).hasSize(run.ignored());
        }
    }
}
