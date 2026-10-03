package org.hiero.sdk.v3.metalang.generator.java;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.hiero.sdk.v3.metalang.MetaLang;
import org.hiero.sdk.v3.metalang.TestSpecs;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TestGeneratorTest {

    @TempDir
    Path temp;

    private static List<GeneratedFile> generate(final String schema) {
        return generate(Map.of("f/a.md", schema));
    }

    private static List<GeneratedFile> generate(final Map<String, String> schemas) {
        return generate(schemas, JavaGeneratorConfig.DEFAULT);
    }

    private static List<GeneratedFile> generate(final Map<String, String> schemas, final JavaGeneratorConfig config) {
        final Map<String, String> documents = new TreeMap<>();
        schemas.forEach((file, schema) -> documents.put(file, TestSpecs.markdown(schema)));
        final org.hiero.sdk.v3.metalang.ValidationReport report = new MetaLang().validate(documents);
        // the specs of the tests must be valid
        assertThat(report.diagnostics()).filteredOn(d -> d.severity()
                == org.hiero.sdk.v3.metalang.diagnostic.Severity.ERROR).isEmpty();
        return new JavaGenerator(config).generate(LinkedModel.of(report.model()));
    }

    private static String test(final List<GeneratedFile> files, final String className) {
        return files.stream().filter(f -> f.path().contains("/src/test/java/")
                        && f.path().endsWith("/" + className + ".java"))
                .findFirst().orElseThrow(() -> new AssertionError("no " + className + " in "
                        + files.stream().map(GeneratedFile::path).toList())).content();
    }

    private GeneratedJava.TestRun run(final List<GeneratedFile> files) throws Exception {
        final GeneratedJava.Compilation modules = GeneratedJava.compile(files, temp);
        assertThat(modules.diagnostics()).isEmpty();
        final GeneratedJava.Compilation tests = GeneratedJava.compileTests(files, modules, temp);
        assertThat(tests.diagnostics()).isEmpty();
        return GeneratedJava.runTests(files, tests);
    }

    /** Replaces a text in a generated file, to check that the tests detect the change. */
    private static List<GeneratedFile> mutate(final List<GeneratedFile> files, final String type, final String from,
                                              final String to) {
        final List<GeneratedFile> result = new ArrayList<>();
        for (final GeneratedFile file : files) {
            if (file.path().endsWith("/src/main/java/org/hiero/a/" + type + ".java")) {
                assertThat(file.content()).contains(from);
                result.add(new GeneratedFile(file.path(), file.content().replace(from, to)));
            } else {
                result.add(file);
            }
        }
        return result;
    }

    @Nested
    class Records {

        private static final String SPEC = """
                namespace a
                enum Color { RED GREEN }
                Limits {
                    @@immutable small: uint8
                    @@immutable @@min(1) count: int64
                    @@immutable big: uint64
                    @@immutable huge: int256
                    @@immutable @@min(0.5) @@max(2.5) ratio: double
                    @@immutable @@min(0) amount: decimal
                    @@immutable @@max(30) delay: duration
                    @@immutable @@minLength(2) @@maxLength(5) code: string
                    @@immutable @@pattern("^/[^\\s]*$") path: string
                    @@immutable @@urlPattern url: string
                    @@immutable @@minSize(1) @@maxSize(3) data: bytes
                    @@immutable @@minSize(1) tags: list<string>
                    @@immutable colors: set<Color>
                    @@immutable labels: map<string, int32>
                    @@immutable @@nullable note: string
                    @@immutable @@default(7) level: int32
                }
                """;

        @Test
        void shouldTestConstructorChecksCopiesAndValueSemantics() throws Exception {
            // WHEN
            final List<GeneratedFile> files = generate(SPEC);
            final String test = test(files, "LimitsTest");

            // THEN
            assertThat(files).extracting(GeneratedFile::path)
                    .contains("org.hiero.f/src/test/java/org/hiero/a/LimitsTest.java")
                    .doesNotContain("org.hiero.f/src/test/java/org/hiero/a/ColorTest.java");
            assertThat(test).startsWith(JavaGenerator.HEADER)
                    .contains("import static org.junit.jupiter.api.Assertions.*;")
                    .contains("void shouldCreateAndReturnTheValues()")
                    .contains("void shouldCreateWithTheDefaultValues()")
                    .contains("void shouldRejectNullTags()", "(List<String>) null")
                    .doesNotContain("void shouldRejectNullSmall()", "void shouldRejectNullNote()")
                    // the range of the type and of the annotations
                    .contains("void shouldRejectSmallBelowMinimum()", "(short) (-1)", "(short) 256")
                    .contains("void shouldAcceptSmallAtMaximum()", "final short value = (short) 255;")
                    .contains("void shouldRejectCountBelowMinimum()", "0L")
                    .contains("void shouldAcceptCountAtMaximum()", "9223372036854775807L")
                    .doesNotContain("void shouldRejectCountAboveMaximum()")
                    .contains("void shouldAcceptBigAtMaximum()", "Long.parseUnsignedLong(\"18446744073709551615\")")
                    .doesNotContain("void shouldRejectBigBelowMinimum()")
                    .contains("void shouldRejectHugeAboveMaximum()")
                    .contains("void shouldRejectRatioBelowMinimum()", "Math.nextDown(0.5)", "Math.nextUp(2.5)")
                    .contains("new BigDecimal(\"-0.001\")")
                    .contains("Duration.ofMillis(30L).plusNanos(1)")
                    .contains("void shouldAcceptCodeAtMinimumLength()", "\"va\"", "\"a\"", "\"aaaaaa\"")
                    .contains("void shouldRejectPathNotMatchingThePattern()")
                    .contains("void shouldRejectUrlThatIsNoUrl()", "\"not a url\"")
                    .contains("void shouldRejectDataBelowMinimumSize()", "new byte[0]")
                    .contains("void shouldRejectDataAboveMaximumSize()", "new byte[] {1, 2, 3, 4}")
                    .contains("void shouldRejectTagsBelowMinimumSize()", "List.of()")
                    // copies
                    .contains("void shouldCopyDataIn()", "void shouldCopyTagsIn()", "void shouldCopyColorsIn()",
                            "void shouldCopyLabelsIn()", "new ArrayList<>(tagsValue())",
                            "new LinkedHashSet<>(colorsValue())", "new LinkedHashMap<>(labelsValue())")
                    .contains("void shouldCompareByValue()")
                    .doesNotContain("@SuppressWarnings");

            // WHEN the tests run against the generated code
            final GeneratedJava.TestRun run = run(files);

            // THEN all pass
            assertThat(run.failures()).isEmpty();
            assertThat(run.tests()).isGreaterThan(40);
        }

        @Test
        void shouldDetectMissingChecksAndCopies() throws Exception {
            // GIVEN the generated code without range check, null check and copies
            List<GeneratedFile> files = generate(SPEC);
            files = mutate(files, "Limits", "small < (short) 0 || small > (short) 255", "false");
            files = mutate(files, "Limits", "tags = List.copyOf(tags);", "");
            files = mutate(files, "Limits", "data = data.clone();", "");

            // WHEN
            final GeneratedJava.TestRun run = run(files);

            // THEN
            assertThat(run.failures()).containsOnlyKeys("LimitsTest.shouldRejectSmallBelowMinimum",
                    "LimitsTest.shouldRejectSmallAboveMaximum", "LimitsTest.shouldCopyTagsIn",
                    "LimitsTest.shouldCopyDataIn");
        }
    }

    @Nested
    class Classes {

        private static final String SPEC = """
                namespace a
                abstraction Base<$$Self extends Base<$$Self>> {
                    @@nullable memo: string
                }
                @@finalType
                Order extends Base<Order> {
                    @@immutable id: string
                    @@min(1) @@max(9) quantity: int32
                    @@default(true) active: bool
                    items: list<string>
                    @@nullable @@deprecated data: bytes
                }
                """;

        @Test
        void shouldTestSettersAndInitialValues() throws Exception {
            // WHEN
            final List<GeneratedFile> files = generate(SPEC);
            final String test = test(files, "OrderTest");

            // THEN
            assertThat(test).contains("@SuppressWarnings(\"deprecation\")\n@DisplayName(\"Order\")\nclass OrderTest {")
                    .contains("assertValue(true, subject.active());")
                    .contains("assertNull(subject.memo());")
                    .contains("void shouldSetMemo()", "assertSame(subject, subject.setMemo(value));")
                    .contains("void shouldSetMemoToNull()", "subject.setMemo((String) null);")
                    .contains("void shouldRejectNullItemsInSetter()")
                    .contains("void shouldSetQuantityAtMaximum()", "void shouldNotSetQuantityAboveMaximum()")
                    .contains("void shouldCopyItemsInSetter()", "void shouldCopyDataInSetter()")
                    .doesNotContain("void shouldCompareByValue()");

            // WHEN
            final GeneratedJava.TestRun run = run(files);

            // THEN
            assertThat(run.failures()).isEmpty();

            // WHEN the setter does not check the value
            final GeneratedJava.TestRun mutated = run(mutate(files, "Order",
                    "        if (quantity < 1) {\n            throw new IllegalArgumentException(\"quantity must be at "
                            + "least 1\");\n        }\n        if (quantity > 9) {\n            throw new "
                            + "IllegalArgumentException(\"quantity must be at most 9\");\n        }\n        this"
                            + ".quantity = quantity;\n        return this;",
                    "        this.quantity = quantity;\n        return this;"));

            // THEN
            assertThat(mutated.failures()).containsOnlyKeys("OrderTest.shouldNotSetQuantityAboveMaximum",
                    "OrderTest.shouldNotSetQuantityBelowMinimum");
        }
    }

    @Nested
    class Methods {

        @Test
        void shouldCallMethodsAndFailForStubs() throws Exception {
            // WHEN
            final List<GeneratedFile> files = generate("""
                    namespace a
                    enum Mode(code: int32) {
                        ON(1)
                        @@deprecated OFF(0)
                        string label()
                    }
                    Account {
                        @@immutable name: string
                        string describe()
                        @@nullable string nickname()
                        int64 balance()
                        void close()
                        @@async string fetch(id: int64)
                        @@streaming string watch()
                        @@throws(not-found-error, timeout-error) string load(key: string, @@nullable hint: string)
                        @@static Account of(name: string)
                        @@finalMethod $$T convert<$$T>(type: type<$$T>)
                    }
                    abstraction Service {
                        @@static Service create()
                        void run()
                    }
                    """);

            // THEN
            assertThat(test(files, "AccountTest"))
                    .contains("assertNotNull(create().describe());")
                    .contains("        create().nickname();\n")
                    .contains("        create().balance();\n")
                    .contains("        create().close();\n")
                    .contains("assertNotNull(create().fetch(idValue()));")
                    .contains("assertNotNull(create().watch());")
                    .contains("void shouldCallLoad() throws Exception {", "create().load(keyValue(), hintValue())",
                            "if (!(e instanceof NoSuchElementException || e instanceof TimeoutException)) {")
                    .contains("assertNotNull(Account.of(nameValue()));")
                    .contains("assertNotNull(create().convert(typeValue()));");
            assertThat(test(files, "ModeTest")).contains("@SuppressWarnings(\"deprecation\")")
                    .contains("assertValue(1, Mode.ON.code());", "assertValue(0, Mode.OFF.code());")
                    .contains("assertNotNull(Mode.ON.label());");
            assertThat(test(files, "ServiceTest")).contains("assertNotNull(Service.create());")
                    .doesNotContain("run()");

            // WHEN
            final GeneratedJava.TestRun run = run(files);

            // THEN every method test fails because of the stub, nothing else
            assertThat(run.unexpected()).isEmpty();
            assertThat(run.stubs()).containsExactly("AccountTest.shouldCallBalance", "AccountTest.shouldCallClose",
                    "AccountTest.shouldCallConvert", "AccountTest.shouldCallDescribe", "AccountTest.shouldCallFetch",
                    "AccountTest.shouldCallLoad", "AccountTest.shouldCallNickname", "AccountTest.shouldCallOf",
                    "AccountTest.shouldCallWatch", "ModeTest.shouldCallLabel", "ServiceTest.shouldCallCreate");
        }

        @Test
        void shouldTestFactoryMethods() throws Exception {
            // WHEN
            final List<GeneratedFile> files = generate("""
                    namespace a
                    Point { @@immutable x: int32 }
                    @@static Point createPoint(x: int32, @@nullable label: string)
                    @@static @@nullable Point findPoint(name: string)
                    @@static void reset(points: Point...)
                    """);
            final String test = test(files, "AFactoryTest");

            // THEN
            assertThat(test).contains("assertNotNull(AFactory.createPoint(xValue(), labelValue()));")
                    .contains("void shouldRejectNullNameInFindPoint()")
                    .contains("void shouldAcceptXAtMaximumInCreatePoint()",
                            "assertNotNull(AFactory.createPoint(2147483647, labelValue()));")
                    .contains("        AFactory.findPoint(nameValue());\n")
                    .contains("AFactory.reset(pointsValue());")
                    .doesNotContain("shouldRejectNullPoints")
                    .doesNotContain("shouldRejectNullLabel");

            // WHEN
            final GeneratedJava.TestRun run = run(files);

            // THEN
            assertThat(run.unexpected()).isEmpty();
            assertThat(run.stubs()).contains("AFactoryTest.shouldCallCreatePoint",
                    "AFactoryTest.shouldRejectNullNameInFindPoint");
        }
    }

    @Nested
    class Values {

        @Test
        void shouldUseSubtypesFactoryMethodsAndTestDoubles() throws Exception {
            // WHEN Named is an interface (configuration), Rate an abstract class
            final List<GeneratedFile> files = generate(Map.of("f/a.md", """
                    namespace a
                    abstraction Shape { double area() }
                    abstraction Rate {
                        @@immutable value: double
                        bool isExpired()
                        string toString()
                    }
                    abstraction Signer { bytes sign(data: bytes) }
                    abstraction Id {
                        @@immutable value: string
                        @@static Id generate(seed: int32)
                        @@throws(illegal-format) @@static Id parse(text: string)
                    }
                    abstraction Named {
                        @@immutable name: string
                        @@nullable label: string
                    }
                    Square extends Shape { @@immutable side: double }
                    Holder {
                        @@immutable shape: Shape
                        @@immutable rate: Rate
                        @@immutable id: Id
                        @@immutable named: Named
                        void use(signer: Signer)
                    }
                    """), new JavaGeneratorConfig(java.util.Set.of(new org.hiero.sdk.v3.metalang.model
                    .QualifiedName("a", "Named"))));
            final String test = test(files, "HolderTest");

            // THEN a concrete subtype, the static factory method without errors, test doubles
            assertThat(test).contains("return new Square(1.5);")
                    .contains("return Id.generate(1);")
                    .contains("return new Rate(1.5) {\n"
                            + "            public boolean isExpired() {\n"
                            + "                throw new UnsupportedOperationException(\"test double\");\n"
                            + "            }\n"
                            + "            @Override\n"
                            + "            public String toString() {\n"
                            + "                throw new UnsupportedOperationException(\"test double\");\n"
                            + "            }\n"
                            + "        };")
                    .contains("return new Named() {\n"
                            + "            @Override\n"
                            + "            public String name() {\n")
                    .contains("            public Named setLabel(final @Nullable String label) {\n")
                    // never a test double as argument of a method
                    .contains("/// - no valid arguments for `use(a.Signer)`");

            // WHEN
            final GeneratedJava.TestRun run = run(files);

            // THEN the tests that create a holder fail until Id.generate is implemented
            assertThat(run.unexpected()).isEmpty();
            assertThat(run.stubs()).contains("HolderTest.shouldCreateAndReturnTheValues");
        }

        @Test
        void shouldUseTheDefaultInstancesOfTheSpecs() throws Exception {
            // GIVEN default instances with every kind of expression
            final String markdown = TestSpecs.markdown("""
                    namespace a
                    enum Kind { SMALL BIG }
                    abstraction Signer { bytes sign(data: bytes) }
                    abstraction Key { @@immutable value: bytes
                        Key derive(seed: int32) }
                    Session {
                        @@immutable key: Key
                        @@immutable signer: Signer
                        @@immutable @@default(3) retries: int32
                        @@nullable note: string
                    }
                    Client { @@immutable session: Session
                        @@immutable tags: list<string>
                        @@immutable kind: Kind
                        void send(signer: Signer, @@nullable comment: string) }
                    constant LIMIT: int32 = 5
                    @@static Key createKey(value: bytes)
                    @@static Session openSession(key: Key)
                    """).replace("## Testing", """
                    ## Default Instances

                    ```
                    // a fixed key
                    instance Key = createKey(value: [1, 2, 3])
                    instance Session = Session{key: DEFAULT(Key).derive(seed: LIMIT), signer: DEFAULT, note: "n"}
                    instance Signer = DEFAULT(Client).session.signer
                    instance Client = Client{session: openSession(key: DEFAULT), tags: ["x"], kind: Kind.BIG}
                    ```

                    ## Testing""");
            final org.hiero.sdk.v3.metalang.ValidationReport report = new MetaLang().validate(Map.of("f/a.md",
                    markdown));
            assertThat(report.diagnostics()).filteredOn(d -> d.ruleId().startsWith("instance.")).isEmpty();

            // WHEN
            final List<GeneratedFile> files = new JavaGenerator().generate(LinkedModel.of(report.model()));
            final String test = test(files, "ClientTest");

            // THEN the default instances are used, also for method arguments
            assertThat(test).contains("return new Session(AFactory.createKey(new byte[] {(byte) 1, (byte) 2, "
                            + "(byte) 3}).derive(AConstants.LIMIT), ")
                    .contains(", 3).setNote(\"n\");")
                    .contains("return new Client(AFactory.openSession(AFactory.createKey(new byte[] {(byte) 1, "
                            + "(byte) 2, (byte) 3})), List.of(\"x\"), Kind.BIG).session().signer();")
                    .contains("create().send(signerValue(), commentValue());")
                    .contains("@DisplayName(\"Client\")\nclass ClientTest {")
                    .contains("@DisplayName(\"creates a Client and returns the values\")")
                    .contains("@DisplayName(\"send(signer, comment) can be called\")");
            // the attributes of a tested type use the default instances of their own types
            assertThat(test(files, "SessionTest")).contains("return AFactory.createKey(new byte[] {(byte) 1, "
                    + "(byte) 2, (byte) 3});");

            // WHEN
            final GeneratedJava.TestRun run = run(files);

            // THEN the values come from the factory functions, so the tests fail until they are implemented
            assertThat(run.unexpected()).isEmpty();
            assertThat(run.stubs()).contains("ClientTest.shouldCreateAndReturnTheValues");
        }

        @Test
        void shouldCreateValuesWithFactoryFunctions() throws Exception {
            // WHEN the constructor of Client needs a Signer (no implementation), but a factory function exists
            final List<GeneratedFile> files = generate(Map.of(
                    "base/b.md", """
                            namespace b
                            abstraction Signer { bytes sign(data: bytes) }
                            Client {
                                @@immutable name: string
                                @@immutable signer: Signer
                            }
                            @@static Client createClient(name: string)
                            @@static Client createClient(name: string, signer: Signer)
                            """,
                    "app/c.md", """
                            namespace c
                            requires {Client} from b
                            Job {
                                @@immutable id: int32
                                void run(client: Client)
                            }
                            """));

            // THEN method arguments use the factory function with the arguments that can be built
            assertThat(test(files, "JobTest")).contains("return BFactory.createClient(\"value\");")
                    .contains("create().run(clientValue());");
            // stored values still use the constructor (with a test double)
            assertThat(test(files, "ClientTest")).contains("return new Signer() {");

            // WHEN
            final GeneratedJava.TestRun run = run(files);

            // THEN the tests that need a client fail until the factory function is implemented
            assertThat(run.unexpected()).isEmpty();
            assertThat(run.stubs()).contains("JobTest.shouldCallRun");
        }

        @Test
        void shouldBuildValuesOfAllTypes() throws Exception {
            // WHEN
            final List<GeneratedFile> files = generate("""
                    namespace a
                    enum Color { RED }
                    abstraction Marker { void mark() }
                    Point { @@immutable x: int32 }
                    Everything {
                        @@immutable day: date
                        @@immutable clock: time
                        @@immutable moment: dateTime
                        @@immutable instant: zonedDateTime
                        @@immutable id: uuid
                        @@immutable pause: seconds
                        @@immutable @@min(5) delay: duration
                        @@immutable flag: bool
                        @@immutable kind: type<Point>
                        @@immutable anyKind: type<Marker>
                        @@immutable anything: ANY
                        @@immutable task: function<void run()>
                        @@immutable supplier: function<string get()>
                        @@immutable mapper: function<Point map(value: int32)>
                        @@immutable test: function<bool check(value: string)>
                        @@immutable merger: function<string merge(a: string, b: string)>
                        @@immutable sink: function<void accept(a: string, b: string)>
                        @@immutable triple: function<string join(a: string, b: string, c: string)>
                        @@immutable points: map<string, Point>
                        @@immutable @@minSize(2) words: set<string>
                        @@immutable @@minSize(2) ratios: list<double>
                    }
                    Crowded { @@immutable @@minSize(2) colors: set<Color> }
                    """);

            // THEN
            assertThat(test(files, "EverythingTest")).contains("LocalDate.of(2024, 1, 1)", "LocalTime.of(12, 0)",
                    "LocalDateTime.of(2024, 1, 1, 12, 0)", "ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC)",
                    "UUID.fromString(\"00000000-0000-0000-0000-000000000001\")", "Duration.ofSeconds(1L)",
                    "Duration.ofMillis(5L)", "Point.class", "return \"value\";", "return () -> { };",
                    "return () -> \"value\";", "return p0 -> new Point(1);", "return p0 -> true;", "Marker.class",
                    "return (p0, p1) -> \"value\";", "return (p0, p1) -> { };", "return (p0, p1, p2) -> \"value\";",
                    "Map.ofEntries(Map.entry(\"value\", new Point(1)))", "Set.of(\"value\", \"value1\")",
                    "List.of(1.5, 2.5)")
                    .doesNotContain("/// - ");
            assertThat(test(files, "CrowdedTest")).contains("/// - no valid value for `colors` of Crowded");
            assertThat(run(files).failures()).isEmpty();
        }

        @Test
        void shouldListWhatCannotBeTested() {
            // WHEN
            final List<GeneratedFile> files = generate("""
                    namespace a
                    abstraction Unknown<$$T> { void run() }
                    @@sealed(Leaf)
                    abstraction Tree { void grow() }
                    Leaf extends Tree { @@immutable size: int32
                        void grow() }
                    Owner { @@immutable unknown: Unknown<int32> }
                    Self<$$T extends Self<$$T>> { @@immutable value: int32 }
                    Generic<$$T> { @@immutable value: $$T
                        @@immutable unknown: Unknown<$$T> }
                    Cycle { @@immutable next: Cycle }
                    Selfish { @@immutable value: int32
                        @@finalMethod $$T copy<$$T extends Selfish>(other: $$T) }
                    Clash { @@immutable value: int32 }
                    ClashTest { @@immutable value: int32 }
                    """);

            // THEN
            assertThat(test(files, "OwnerTest")).contains("/// - no valid value for `unknown` of Owner: constructor, "
                    + "attribute and method tests");
            assertThat(test(files, "SelfTest")).contains("/// - no type arguments for Self (a type parameter refers "
                    + "to itself)");
            assertThat(test(files, "GenericTest")).contains("/// - no valid value for `unknown` of Generic");
            assertThat(test(files, "CycleTest")).contains("/// - no valid value for `next` of Cycle");
            // a type parameter of a method gets its bound
            assertThat(test(files, "SelfishTest")).contains("assertNotNull(create().copy(otherValue()));");
            assertThat(files).extracting(GeneratedFile::path)
                    .doesNotContain("org.hiero.f/src/test/java/org/hiero/a/ClashTest.java")
                    .doesNotContain("org.hiero.f/src/test/java/org/hiero/a/UnknownTest.java");
        }

        @Test
        void shouldOnlyUseTypesOfRequiredModules() {
            // WHEN the only implementation of Shape is in a module that the module of Box does not require
            final List<GeneratedFile> files = generate(Map.of(
                    "base/b.md", "namespace b\nabstraction Shape { void draw() }\nBox { @@immutable shape: Shape }\n",
                    "extra/c.md", "namespace c\nrequires {Shape} from b\nCircle extends Shape { @@immutable r: int32\n"
                            + "    void draw() }\n"));

            // THEN Box uses a test double instead of Circle
            assertThat(test(files, "BoxTest")).doesNotContain("Circle").contains("return new Shape() {");
        }
    }
}
