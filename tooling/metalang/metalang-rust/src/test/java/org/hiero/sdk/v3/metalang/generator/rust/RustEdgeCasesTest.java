package org.hiero.sdk.v3.metalang.generator.rust;

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

/** Edge cases of the Rust generator, its test values and its tests; every result is built and run with Cargo. */
class RustEdgeCasesTest {

    /** Values of every kind, members of every kind. */
    private static final String VALUES = """
            namespace a
            // A kind.
            enum Kind {
                A
                // Use A.
                @@deprecated B
            }
            enum Legacy { @@deprecated OLD }
            enum Shade(rgb: bytes, weight: decimal, tags: list<string>) {
                DARK([1, 2], 1.5, ["x"])
            }
            enum Printable {
                P
                string toString()
            }
            abstraction Unit { @@immutable symbol: string }
            abstraction Remote {
                void call()
                @@streaming string lines()
                @@streaming @@throws(io-error) string read()
            }
            @@sealed(Circle, Square)
            abstraction Shape {
                @@immutable name: string
                double area()
                string toString()
                @@static Shape origin()
            }
            Circle extends Shape {
                @@immutable radius: double
                double area()
            }
            Square extends Shape {
                double area()
            }
            UnitImpl extends Unit { }
            // A value with all kinds of attributes.
            //
            // ```
            // code without end
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
                @@nullable @@minLength(1) code: string
                @@immutable pairs: map<Remote, int32>
                name: string
                string toString()
            }
            Base {
                @@nullable num: int64
            }
            Narrow extends Base {
                @@override num: int64
            }
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
                @@finalMethod $$C keep<$$C extends Circle>(value: $$C)
            }
            @@static Remote connect(@@nullable host: string, ports: int32...)
            @@static @@async Remote connectLater()
            @@static void reset()
            @@static @@nullable Remote find()
            @@static @@throws(invalid-argument-error) @@throws(io-error) string load(id: int64)
            @@static Values values(flag: bool)
            constant ANSWER: int32 = 42
            constant GREETING: string = "a \\"quoted\\" text"
            constant WAIT: seconds = 5
            constant RATE: decimal = 1.5
            constant BYTES: bytes = [1, 2]
            constant NAMES: list<string> = ["x"]
            constant KINDS: set<Kind> = [Kind.A]
            """;

    private static final String VALUES_INSTANCES = """
            instance Remote = connect()
            instance Unit = UnitImpl{symbol: "u"}
            instance Shape = Circle{name: "c", radius: 1}
            """;

    /** Values that cannot be built, names that need care. */
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
            }
            Flags { @@immutable @@minSize(3) flags: set<bool> }
            Switches { @@immutable @@minSize(3) switches: map<bool, string> }
            Values {
                @@immutable opaques: list<Opaque>
                @@immutable lookup: map<string, Opaque>
                @@immutable @@min(100) high: int8
                @@immutable @@minSize(0) @@maxSize(4) data: bytes
                @@immutable @@maxLength(4) code: string
                @@immutable named: Named
                @@immutable match: string
                @@immutable self: string
                @@streaming string lines()
                @@async @@nullable string later()
                @@nullable string maybe()
                @@finalMethod $$V pick<$$V extends Opaque>(value: $$V)
                @@throws(timeout) void wait2(in: Opaque)
            }
            Factory {
                @@immutable opaque: Opaque
                @@static @@throws(timeout) Factory risky(seed: int32)
                @@static Factory make(@@nullable hint: string, opaque: Opaque)
                @@static Factory many(opaque: Opaque, extra: int32...)
            }
            @@static Opaque wrap(first: Opaque)
            """;

    private static final String UNBUILDABLE_INSTANCES = """
            instance Named = Flavor.SWEET
            """;

    /** Subtypes, factories and default instances. */
    private static final String SUBTYPES = """
            namespace c
            abstraction Shape { double area() }
            Plain extends Shape { double area() }
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
            abstraction Account {
                @@immutable id: int64
                @@nullable memo: string
                @@nullable @@min(1) count: int32
                @@min(1) level: int32
                Account copy()
            }
            Wallet extends Account {
                @@override @@min(1) @@max(5) count: int32
                Account copy()
            }
            Uses {
                @@immutable shape: Shape
                @@immutable anyContainer: Container<ANY>
                @@immutable made: Made
                @@immutable anyClass: type<ANY>
                @@immutable roundClass: type<Round>
                @@immutable callbacks: list<function<void run()>>
                @@immutable values: list<int64>
                @@throws(timeout) @@async string fetch()
                @@throws(timeout) @@nullable string peek()
                @@throws(timeout) @@streaming string tail()
            }
            Config {
                @@immutable data: bytes
                @@immutable tags: set<string>
                @@immutable n: int32
                @@immutable label: string
                @@immutable count: int32
                @@immutable kinds: list<Round>
            }
            Holder {
                @@immutable config: Config
                @@immutable account: Account
                @@immutable memo: string
            }
            // An old type.
            //
            // It is deprecated, use Uses.
            @@deprecated
            Old { @@immutable name: string }
            @@static Account open(id: int64)
            @@static @@async Account openLater(id: int64)
            constant ANSWER: int32 = 42
            constant NAME: string = "n"
            constant ROUND: Round = Round.ONE
            constant DEFAULT_CONFIG: Config = Config{data: [1], tags: ["t"], n: 1, label: "l", count: 2, kinds: [Round.ONE]}
            """;

    private static final String SUBTYPES_INSTANCES = """
            instance Config = Config{data: [1, 2], tags: ["a"], n: ANSWER, label: NAME, count: ANSWER, kinds: [ROUND]}
            instance Account = openLater(id: 7)
            instance Holder = Holder{config: DEFAULT, account: DEFAULT, memo: DEFAULT(Account).copy().memo}
            """;

    /** Conversions: of subtypes to their struct supertype, of narrowed and erased attributes, in instances. */
    private static final String CONVERSIONS = """
            namespace d
            abstraction Item { @@immutable label: string }
            Thing extends Item { }
            Base {
                @@immutable text: string
                @@nullable maybe: string
                @@immutable data: bytes
                @@nullable maybeData: bytes
                @@immutable numbers: list<int32>
                @@nullable maybeNumbers: list<int32>
                @@immutable pairs: map<Item, int32>
                @@nullable maybePairs: map<Item, int32>
                @@immutable thing: Thing
                @@nullable maybeThing: Thing
                @@immutable item: Item
                @@nullable maybeItem: Item
                @@nullable @@min(1) limit: int32
            }
            Sub extends Base {
                @@immutable extra: int32
            }
            @@sealed(VariantA, VariantB)
            abstraction Sealed {
                // The text.
                @@immutable text: string
                @@nullable maybe: string
                string describe()
                string toString()
            }
            VariantA extends Sealed { }
            VariantB extends Sealed {
                @@override maybe: string
            }
            abstraction Pocket<$$T extends Item> {
                @@nullable content: $$T
                @@immutable first: $$T
            }
            ThingPocket extends Pocket<Thing> { }
            abstraction Chain<$$Self extends Chain<$$Self>> {
                @@nullable next: $$Self
                @@immutable head: $$Self
            }
            Holder {
                @@immutable sealed: Sealed
                @@immutable base: Base
                @@immutable pocket: Pocket<ANY>
                @@immutable texts: list<string>
                @@immutable label: string
                @@nullable maybe: string
                @@nullable data: bytes
                @@nullable numbers: list<int32>
                @@nullable thing: Thing
                @@immutable item: Item
            }
            """;

    private static final String CONVERSIONS_INSTANCES = """
            instance Item = Thing{label: "t"}
            instance Holder = Holder{sealed: DEFAULT, base: DEFAULT, pocket: DEFAULT, texts: DEFAULT, label: DEFAULT(Item).label, maybe: DEFAULT(Base).maybe, data: DEFAULT(Base).maybeData, numbers: DEFAULT(Base).maybeNumbers, thing: DEFAULT(Base).maybeThing, item: DEFAULT(Base).item}
            """;

    @TempDir
    Path temp;

    private static List<GeneratedFile> generate(final String folder, final String schema, final String instances) {
        final String markdown = TestSpecs.markdown(schema).replace("## Testing", "## Default Instances\n\n```\n"
                + instances + "```\n\n## Testing");
        final ValidationReport report = new MetaLang().validate(Map.of(folder + "/x.md", markdown));
        // nullable collections are an error of the specs, but the real specs still have them (see collection.nullable)
        assertThat(report.diagnostics()).filteredOn(d -> d.severity() == Severity.ERROR
                && !d.ruleId().equals("collection.nullable")).isEmpty();
        return new RustGenerator().generate(LinkedModel.of(report.model()));
    }

    private void buildAndRun(final List<GeneratedFile> files) throws Exception {
        assumeTrue(GeneratedRust.available(), "cargo is needed");
        assertThat(GeneratedRust.build(files, temp)).isEmpty();
        final GeneratedRust.TestRun run = GeneratedRust.test(temp);
        assertThat(run.failures()).isEmpty();
        assertThat(run.ignored()).isEqualTo(RustTestGenerator.untested(files).size());
    }

    @Test
    void shouldBuildValuesAndMembersOfEveryKind() throws Exception {
        // WHEN
        final List<GeneratedFile> files = generate("f", VALUES, VALUES_INSTANCES);

        // THEN
        assertThat(RustGeneratorTest.file(files, "/src/a/shade.rs")).contains("pub fn rgb(&self) -> Vec<u8> {")
                .contains("pub fn weight(&self) -> rust_decimal::Decimal {");
        assertThat(RustGeneratorTest.file(files, "/src/a/shape.rs"))
                .contains("pub enum Shape {\n    Circle(Circle),\n    Square(Square),\n}")
                .contains("Shape::Circle(v) => v.name(),").contains("impl Display for Shape {");
        assertThat(RustGeneratorTest.file(files, "/src/a/narrow.rs"))
                .contains("pub fn set_num(&mut self, num: i64) -> &mut Self {");
        assertThat(RustGeneratorTest.file(files, "/src/a/values.rs")).contains("```text");
        assertThat(RustGeneratorTest.file(files, "/src/a/constants.rs"))
                .contains("pub const GREETING: &str = \"a \\\"quoted\\\" text\";")
                .contains("pub static BYTES: LazyLock<Vec<u8>> = LazyLock::new(|| vec![1, 2]);")
                .contains("HashSet::from([Kind::A])");
        assertThat(RustGeneratorTest.file(files, "/src/a/user.rs")).contains("pub fn keep<C>(&self, value: C) -> C {");
        buildAndRun(files);
    }

    @Test
    void shouldReportTheValuesThatCannotBeBuilt() throws Exception {
        // WHEN
        final List<GeneratedFile> files = generate("g", UNBUILDABLE, UNBUILDABLE_INSTANCES);

        // THEN
        assertThat(RustTestGenerator.untested(files)).contains(
                "b/flags.rs: no valid value for `flags` of Flags: constructor, attribute and method tests",
                "b/switches.rs: no valid value for `switches` of Switches: constructor, attribute and method tests",
                "b/node.rs: no type arguments for Node (a type parameter refers to itself)",
                "b/functions.rs: no valid arguments for `wrap(b.Opaque)`");
        assertThat(RustGeneratorTest.file(files, "/src/b/values.rs")).contains("r#match: String,")
                .contains("self_: String,").contains("pub fn r#match(&self) -> &str {");
        buildAndRun(files);
    }

    @Test
    void shouldFindValuesOfSubtypesFactoriesAndInstances() throws Exception {
        // WHEN
        final List<GeneratedFile> files = generate("h", SUBTYPES, SUBTYPES_INSTANCES);

        // THEN
        assertThat(RustGeneratorTest.file(files, "/src/c/old.rs")).contains("#[deprecated(note = \"It is deprecated, "
                + "use Uses.\")]");
        assertThat(RustGeneratorTest.file(files, "/src/c/wallet.rs"))
                .contains("fn set_level(&mut self, level: i32) -> Result<(), InvalidArgumentError> {\n"
                        + "        self.set_level(level).map(|_| ())")
                .contains("fn set_count(&mut self, count: Option<i32>) -> Result<(), InvalidArgumentError> {\n"
                        + "        todo!(\"Wallet.count\")");
        assertThat(RustGeneratorTest.file(files, "tests/api/c/holder.rs"))
                .contains("crate::helpers::block_on(open_later(7))");
        buildAndRun(files);
    }

    @Test
    void shouldConvertBetweenTheFormsOfAValue() throws Exception {
        // WHEN
        final List<GeneratedFile> files = generate("k", CONVERSIONS, CONVERSIONS_INSTANCES);

        // THEN
        assertThat(RustGeneratorTest.file(files, "/src/d/sub.rs")).contains("impl From<Sub> for Base {")
                .contains("value.maybe().map(str::to_string)").contains("value.data().to_vec()")
                .contains("value.maybe_data().map(<[u8]>::to_vec)").contains("value.maybe_numbers().map(<[_]>::to_vec)")
                .contains("value.thing().clone()").contains("value.maybe_thing().cloned()")
                .contains(".expect(\"the values of a subtype are valid\")");
        assertThat(RustGeneratorTest.file(files, "/src/d/sealed.rs")).contains("Sealed::VariantB(v) => Some(v.maybe()),");
        assertThat(RustGeneratorTest.file(files, "/src/d/thing_pocket.rs"))
                .contains("self.content().map(|v| Arc::new(v.clone()) as Arc<dyn Item>)");
        assertThat(RustGeneratorTest.file(files, "/src/d/chain.rs")).contains("fn head(&self) -> Self where Self: Sized;");
        buildAndRun(files);
    }

    @Test
    void shouldOnlyUseTheTypesOfRequiredCratesAndSkipDeferredOnes() throws Exception {
        // GIVEN implementations, factories and default instances in a crate that the base crate does not require,
        // and types that cannot be generated (they refer to an unknown type)
        final Map<String, String> documents = Map.of(
                "base/a.md", TestSpecs.markdown("""
                        namespace a
                        abstraction Shape { double area() }
                        enum Nothing { }
                        Holder {
                            @@immutable shape: Shape
                            @@immutable broken: list<Broken>
                            double size()
                        }
                        Broken { @@immutable x: Missing }
                        Plain {
                            @@immutable shape: Shape
                            @@finalMethod $$S pick<$$S extends Shape>(value: $$S)
                            @@finalMethod $$T keep<$$T extends Plain>(value: $$T)
                        }
                        @@static Broken broken()
                        constant BROKEN: Broken = Broken{x: 1}
                        """),
                "extra/b.md", TestSpecs.markdown("""
                        namespace b
                        requires {Shape} from a
                        Circle extends Shape { double area() }
                        @@static Shape circle()
                        """).replace("## Testing", "## Default Instances\n\n```\ninstance Shape = circle()\n```\n\n"
                        + "## Testing"));
        final LinkedModel model = LinkedModel.of(new MetaLang().validate(documents).model());

        // WHEN
        final List<GeneratedFile> files = new RustGenerator().generate(model);

        // THEN the base crate uses a test double for Shape and does not generate the broken declarations
        assertThat(RustGeneratorTest.file(files, "crates/base/tests/api/a/plain.rs"))
                .contains("Arc::new(ShapeDouble)").contains("no type arguments for `pick($$S)`")
                .contains("no valid arguments for `keep($$T)`");
        assertThat(files).noneMatch(f -> f.path().endsWith("/broken.rs") || f.path().endsWith("/holder.rs")
                || f.path().endsWith("/constants.rs"));
        assertThat(RustGeneratorTest.file(files, "crates/base/src/a/nothing.rs")).contains("match *self {}");
        buildAndRun(files);
    }

    @Test
    void shouldAliasClashingNamesAndRenderAllLiterals() throws Exception {
        // GIVEN types with the same name in two namespaces, a type named like a prelude type, literals of all kinds
        final Map<String, String> documents = Map.of(
                "base/p.md", TestSpecs.markdown("namespace p\nItem { @@immutable id: int32 }\n"),
                "base/q.md", TestSpecs.markdown("namespace q\nItem { @@immutable name: string }\n"),
                "base/r.md", TestSpecs.markdown("""
                        namespace r
                        requires {Item} from p
                        requires {*} from q
                        Result { @@immutable ok: bool }
                        Pair {
                            @@immutable first: p.Item
                            @@immutable second: q.Item
                            @@immutable result: Result
                            @@immutable parsed: Parsed
                        }
                        Limited {
                            @@immutable @@min(1) count: int32
                            @@immutable @@default(3) level: int32
                        }
                        abstraction Parsed {
                            @@static @@throws(illegal-format) Parsed parse(text: string)
                        }
                        Arc {
                            @@immutable parsed: Parsed
                            @@deprecated label: string
                        }
                        Wrapper<$$T> {
                            @@immutable value: $$T
                            string toString()
                        }
                        abstraction Runner1 { void run(x: int32) }
                        abstraction Runner2 { void run(x: string) }
                        Runner extends Runner1, Runner2 { }
                        constant EMPTY: map<string, int32> = []
                        constant FLAG: bool = true
                        constant PI: double = 3
                        constant LIMITS: Limited = Limited{count: 2}
                        """));
        final ValidationReport report = new MetaLang().validate(documents);
        assertThat(report.diagnostics()).filteredOn(d -> d.severity() == Severity.ERROR).isEmpty();

        // WHEN
        final List<GeneratedFile> files = new RustGenerator().generate(LinkedModel.of(report.model()));

        // THEN
        assertThat(RustGeneratorTest.file(files, "/src/r/pair.rs")).contains("use crate::p::Item;")
                .contains("use crate::q::Item as QItem;").contains("use crate::r::Result as RResult;")
                .contains("second: QItem,").contains("result: RResult,");
        assertThat(RustGeneratorTest.file(files, "/src/r/arc.rs")).contains("parsed: std::sync::Arc<dyn Parsed>,")
                .contains("    #[deprecated(note = \"Retained for compatibility; do not use it in new code.\")]\n"
                        + "    pub fn set_label(");
        assertThat(RustGeneratorTest.file(files, "/src/r/wrapper.rs")).contains("impl<T> Display for Wrapper<T> {");
        assertThat(RustGeneratorTest.file(files, "/src/r/runner.rs")).contains("pub fn run(&self, x: i32) {")
                .contains("pub fn run_runner2(&self, x: String) {").contains("self.run_runner2(x)");
        assertThat(RustGeneratorTest.file(files, "/src/r/constants.rs"))
                .contains("pub static EMPTY: LazyLock<HashMap<String, i32>> = LazyLock::new(|| HashMap::new());")
                .contains("pub const FLAG: bool = true;").contains("pub const PI: f64 = 3.0;")
                .contains("Limited::new(2, 3).expect(\"valid value\")");
        assertThat(RustGeneratorTest.file(files, "tests/api/r/pair.rs"))
                .contains("<dyn Parsed>::parse(\"value\".to_string()).expect(\"valid value\")");
        buildAndRun(files);
    }

    @Test
    void shouldQuoteAndEscape() {
        assertThat(RustLiterals.quote("\"\\\n\r\t\u0001x")).isEqualTo("\"\\\"\\\\\\n\\r\\t\\u{1}x\"");
        assertThat(RustDoc.escape("a <b> [c] [d](e) `<f>`")).isEqualTo("a \\<b> \\[c] [d](e) `<f>`");
        assertThat(RustNames.snake("HTTPClient")).isEqualTo("http_client");
        assertThat(RustNames.snake("ofBytes23")).isEqualTo("of_bytes23");
        assertThat(RustNames.member("type")).isEqualTo("r#type");
        assertThat(RustNames.member("self")).isEqualTo("self_");
        assertThat(RustNames.variant("ECDSA_SECP256K1")).isEqualTo("EcdsaSecp256k1");
        assertThat(RustNames.variant("_")).isEqualTo("_");
        assertThat(RustNames.errorType("timeout")).isEqualTo("TimeoutError");
        assertThat(RustImports.codeOnly("a // b\n\"c\\\"d\" e")).isEqualTo("a \n\"\" e");
        assertThat(RustImports.codeOnly("a / b // end")).isEqualTo("a / b ");
        assertThat(RustImports.codeOnly("x \"open")).isEqualTo("x \"\"");
    }
}
