package org.hiero.sdk.v3.metalang.generator.java;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.hiero.sdk.v3.metalang.MetaLang;
import org.hiero.sdk.v3.metalang.ValidationReport;
import org.hiero.sdk.v3.metalang.diagnostic.Severity;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The JUnit tests generated from the {@code tests} blocks of the specs: they compile with {@code -Werror} against
 * the generated modules and the test support module, and they behave as the test language defines when they run.
 */
class SpecTestGeneratorTest {

    private static final String SCHEMA = """
            namespace a

            enum Color { RED, GREEN }

            Point {
                @@immutable x: int32
                @@immutable big: uint64
                @@immutable label: string
                @@immutable @@nullable note: string
                @@immutable data: bytes
                @@immutable tags: list<string>

                @@throws(illegal-format) @@static Point parse(text: string)
                int32 sum(other: int32)
            }

            abstraction Shape {}

            Holder {
                @@immutable shape: Shape
            }
            """;

    private static final String INSTANCES = """
            instance Point = Point{x: 2, big: 18446744073709551615, label: "aℏ", data: [1, 2], tags: ["a", "b"]}

            // labels of one letter
            values LABELS: list<string> = ["a", "b"]
            values HOLDERS: list<Holder> = [DEFAULT(Holder)]
            """;

    private static final String TESTS = """
            // the attributes of a record
            test "a record returns its values" {
                // given
                let p = DEFAULT(Point)

                // then
                assertEquals(expected: 2, actual: p.x) // an int32
                assertEquals(expected: 18446744073709551615, actual: p.big)
                assertEquals(expected: 2, actual: length(value: p.label), message: "code points")
                assertEquals(expected: hex(text: "0102"), actual: p.data)
                assertNull(actual: p.note)
                assertEquals(expected: ["a", "b"], actual: p.tags)
            }

            test "integers are exact" {
                let p = DEFAULT(Point)
                assertEquals(expected: 36893488147419103230, actual: p.big + p.big)
                assertTrue(condition: -7 / 2 == -3 && -7 % 2 == -1)
                assertTrue(condition: p.big > 0)
                assertTrue(condition: p.x + 1 > p.x)
            }

            test "control flow" {
                for t in DEFAULT(Point).tags {
                    if t == "a" {
                        assertEquals(expected: 1, actual: length(value: t))
                    } else if t is not null {
                        assertTrue(condition: startsWith(value: t, prefix: "b"))
                    } else {
                        fail(message: "unreachable")
                    }
                }
                let note = DEFAULT(Point).note
                if note is null {
                    assertTrue(condition: Color.RED != Color.GREEN)
                }
                assertTrue(condition: Color.RED == Color.RED && not(value: Color.GREEN == Color.RED))
            }

            test "maps and indexes" {
                let m = {"one": 1, "two": 2}
                assertEquals(expected: 2, actual: m["two"])
                assertEquals(expected: "b", actual: DEFAULT(Point).tags[1])
                assertTrue(condition: containsKey(value: m, key: "one") && not(value: containsKey(value: m, key: "x")))
                assertEquals(expected: "ab", actual: "a" + "b")
            }

            test "an expected error" {
                @@expectThrows(illegal-format)
                Point.parse(text: "")
            }

            test "a method stub" {
                assertEquals(expected: 3, actual: DEFAULT(Point).sum(other: 1))
            }

            @@disabled("not yet")
            test "a disabled test" {
                fail(message: "never runs")
            }

            test "named test values" {
                for label in LABELS {
                    assertEquals(expected: 1, actual: length(value: label))
                }
            }

            test "named test values without Java form" {
                for holder in HOLDERS {
                    assertNotNull(actual: holder)
                }
            }

            test "a holder" {
                let h = DEFAULT(Holder)
                assertNotNull(actual: h)
            }
            """;

    @TempDir
    Path temp;

    private static String spec(final String tests) {
        return """
                # A

                ## Description

                Text.

                ## API Schema

                ```
                %s```

                ## Default Instances for Testing

                ```
                %s```

                ## Testing

                ```tests
                %s```

                ## Questions & Comments
                """.formatted(SCHEMA, INSTANCES, tests);
    }

    private static List<GeneratedFile> generate(final String tests) {
        final ValidationReport report = new MetaLang().validate(Map.of("f/a.md", spec(tests)));
        assertThat(report.diagnostics()).filteredOn(d -> d.severity() == Severity.ERROR).isEmpty();
        return new JavaGenerator(JavaGeneratorConfig.DEFAULT).generate(LinkedModel.of(report.model()));
    }

    private static String file(final List<GeneratedFile> files, final String suffix) {
        return files.stream().filter(f -> f.path().endsWith(suffix)).findFirst()
                .orElseThrow(() -> new AssertionError("no " + suffix)).content();
    }

    @Test
    void theTestsShouldCompileAndBehaveAsTheTestLanguageDefines() throws Exception {
        final List<GeneratedFile> files = generate(TESTS);
        final GeneratedJava.Compilation modules = GeneratedJava.compile(files, temp);
        assertThat(modules.diagnostics()).isEmpty();
        final GeneratedJava.Compilation tests = GeneratedJava.compileTests(files, modules, temp);
        assertThat(tests.diagnostics()).isEmpty();
        final GeneratedJava.TestRun run = GeneratedJava.runTests(files.stream()
                .filter(f -> f.path().endsWith("SpecTest.java")).toList(), tests);
        assertThat(run.unexpected()).isEmpty();
        // the expected error and the method call reach a method stub; the disabled test does not run
        assertThat(run.stubs()).containsExactly("ASpecTest.aMethodStub", "ASpecTest.anExpectedError");
        assertThat(run.tests()).isEqualTo(7);
    }

    @Test
    void theTestClassShouldLookLikeTheOtherGeneratedTests() {
        final String java = file(generate(TESTS), "/org/hiero/a/ASpecTest.java");
        assertThat(java).startsWith(JavaGenerator.HEADER)
                .contains("import static org.hiero.sdk.testing.Testing.*;\n")
                .contains("import static org.junit.jupiter.api.Assertions.assertThrows;\n")
                .contains("@DisplayName(\"a\")\nclass ASpecTest {\n")
                .contains("    /// `a/a-record-returns-its-values`\n    ///\n    /// the attributes of a record\n"
                        + "    @Test\n    @DisplayName(\"a record returns its values\")\n"
                        // the language-neutral id of the test, for the results (Open Test Reporting)
                        + "    @Tag(\"testId:a/a-record-returns-its-values\")\n"
                        + "    void aRecordReturnsItsValues() {\n")
                .contains("        // given\n        final Point p = defaultPoint();\n\n        // then\n"
                        + "        assertEquals(2, p.x()); // an int32\n")
                .contains("        assertEquals(2L, length(p.label()), \"code points\");\n")
                .contains("        assertEquals(new BigInteger(\"36893488147419103230\"), "
                        + "unsigned(p.big()).add(unsigned(p.big())));\n")
                .contains("        assertEquals(Long.parseUnsignedLong(\"18446744073709551615\"), p.big());\n")
                .contains("        assertTrue(unsigned(p.big()).compareTo(unsigned(0L)) > 0);\n")
                .contains("        assertTrue(equal(integer(-7L).divide(integer(2L)), integer(-3L)) && "
                        + "equal(integer(-7L).remainder(integer(2L)), integer(-1L)));\n")
                .contains("            } else if (t != null) {\n")
                .contains("            assertTrue(Color.RED != Color.GREEN);\n")
                .contains("        assertEquals(2L, get(m, \"two\"));\n")
                .contains("assertTrue(containsKey(m, \"one\") && !containsKey(m, \"x\"));")
                .contains("        assertThrows(IllegalArgumentException.class, () -> Point.parse(\"\"));\n")
                .contains("    @Disabled(\"not yet\")\n")
                .contains("    private static Point defaultPoint() {\n");
    }

    @Test
    void aTestWithoutJavaFormShouldBeListedAsNotGenerated() {
        final List<GeneratedFile> files = generate(TESTS);
        assertThat(file(files, "/org/hiero/a/ASpecTest.java"))
                .contains("/// Not generated:\n/// - values `HOLDERS`: no value of a.Holder\n"
                        + "/// - `named test values without Java form`: the values HOLDERS have no Java form\n"
                        + "/// - `a holder`: no value of a.Holder\n")
                .contains("class ASpecTest {\n\n    /// labels of one letter\n"
                        + "    private static List<String> LABELS() {\n        return List.of(\"a\", \"b\");\n    }\n")
                .contains("for (final String label : LABELS()) {")
                .doesNotContain("HOLDERS =")
                .doesNotContain("void aHolder()");
        assertThat(TestGenerator.untested(files)).contains("ASpecTest: `a holder`: no value of a.Holder");
    }

    @Test
    void onlyAModuleWithTestsShouldDependOnTheTestSupport() {
        assertThat(generate(TESTS).stream().filter(f -> f.path().equals("pom.xml")).findFirst().orElseThrow()
                .content()).doesNotContain("hiero-sdk-test-support");
        final String module = generate(TESTS).stream().filter(f -> f.path().endsWith("/pom.xml")
                && !f.path().equals("pom.xml")).findFirst().orElseThrow().content();
        assertThat(module).contains("            <artifactId>hiero-sdk-test-support</artifactId>\n"
                + "            <version>${project.version}</version>\n            <scope>test</scope>\n");
        // the contract tests use it as well (assertValue); a module without any test does not
        final ValidationReport withoutSpecTests = new MetaLang().validate(Map.of("f/a.md",
                spec("").replace("```tests\n```\n", "")));
        final List<GeneratedFile> contractTestsOnly = new JavaGenerator(JavaGeneratorConfig.DEFAULT)
                .generate(LinkedModel.of(withoutSpecTests.model()));
        assertThat(contractTestsOnly).noneMatch(f -> f.path().endsWith("SpecTest.java"));
        assertThat(contractTestsOnly).anyMatch(f -> f.path().endsWith("/pom.xml")
                && f.content().contains("hiero-sdk-test-support"));
        final ValidationReport withoutTests = new MetaLang().validate(Map.of("f/a.md", "# A\n\n## Description\n\n"
                + "Text.\n\n## API Schema\n\n```\nnamespace a\nabstraction Shape {}\n```\n\n## Questions & Comments\n"));
        assertThat(new JavaGenerator(JavaGeneratorConfig.DEFAULT).generate(LinkedModel.of(withoutTests.model())))
                .noneMatch(f -> f.content().contains("hiero-sdk-test-support"));
    }

    @Test
    void theClassNameShouldFollowTheSpecFile() {
        assertThat(SpecTestGenerator.className("native-token")).isEqualTo("NativeTokenSpecTest");
        assertThat(SpecTestGenerator.className("keys")).isEqualTo("KeysSpecTest");
        assertThat(SpecTestGenerator.className("-a--b")).isEqualTo("ABSpecTest");
        org.assertj.core.api.Assertions.assertThatIllegalArgumentException()
                .isThrownBy(() -> JavaGenerator.annotated("// no type\n"));
    }

    private static final String WIDE_SCHEMA = """
            namespace b

            abstraction Shape {}

            Circle extends Shape {
                @@immutable r: int32
            }

            Box<$$T> {
                @@immutable content: $$T
            }

            Limited {
                @@immutable @@min(1) count: int32
            }

            Numbers {
                @@immutable small: int8
                @@immutable i: int32
                @@immutable u: uint64
                @@immutable d: double
                @@immutable m: decimal
                @@immutable b: bytes
                @@immutable l: list<string>
                @@immutable shape: Shape
                @@immutable box: Box<string>
                @@immutable @@nullable day: date

                int32 echo(value: int32)
                uint64 echoUnsigned(value: uint64)
                int8 echoSmall(value: int8)
                uint8 echoShort(value: uint8)
                int64 echoLong(value: int64)
                int256 echoHuge(value: int256)
                uint128 echoWide(value: uint128)
                @@async int32 later()
                @@async @@throws(io-error) int32 readLater()
                @@throws(io-error) int32 read()
                @@static int32 count(kind: type)
            }
            """;

    private static final String WIDE_INSTANCES = """
            instance Numbers = Numbers{small: 1, i: 2, u: 3, d: 1.5, m: 2.5, b: [1], l: ["x"], \
            shape: Circle{r: 1}, box: Box<string>{content: "c"}}
            """;

    private static final String WIDE_TESTS = """
            /* several
               lines */
            test "doubles and decimals" {
                let n = DEFAULT(Numbers)
                assertApprox(expected: 3.0, actual: n.d * 2.0, tolerance: 0.001)
                assertTrue(condition: -n.d < 0.0 && n.d / 3.0 > 0.0)
                assertEquals(expected: toDecimal(value: 5), actual: n.m + n.m)
                assertTrue(condition: n.m * n.m >= -n.m)
                assertTrue(condition: n.m - n.m <= n.m)
            }

            test "values change their kind" {
                let n = DEFAULT(Numbers)
                let bigger: int32 = n.i + 1
                assertEquals(expected: 3, actual: n.echo(value: n.i + 1))
                assertEquals(expected: 4, actual: n.echoUnsigned(value: n.u + 1))
                assertEquals(expected: 2, actual: n.echoSmall(value: -n.small + 3))
                assertEquals(expected: 3, actual: bigger)
                assertNotEquals(unexpected: n.i, actual: n.u)
                assertTrue(condition: n.i != n.u)
            }

            test "values are joined" {
                let n = DEFAULT(Numbers)
                assertEquals(expected: hex(text: "0102"), actual: n.b + hex(text: "02"))
                assertEquals(expected: ["x", "y"], actual: n.l + ["y"])
                assertTrue(condition: n.shape is Circle && not(value: n.shape is not Circle))
                assertInstanceOf(expected: Circle, actual: n.shape)
                assertEquals(expected: "c", actual: n.box.content)
                assertEquals(expected: 11, actual: length(value: {1: 1, 2: 2, 3: 3, 4: 4, 5: 5, 6: 6, 7: 7, 8: 8,
                        9: 9, 10: 10, 11: 11}))
            }

            @@deprecated("replaced")
            test "1 call aLater" {
                assertEquals(expected: 1, actual: DEFAULT(Numbers).later())
            }

            test "a later read fails" {
                // when / then
                @@expectThrows(io-error)
                DEFAULT(Numbers).readLater()
            }

            test "1 call a later" {
                // when / then
                //
                assertEquals(expected: 1, actual: DEFAULT(Numbers).read()) //
            }

            test "checked calls everywhere" {
                let n = DEFAULT(Numbers)
                if n.read() + 1 > 0 {
                    for x in [n.read()] {
                        assertTrue(condition: [1, 2][x] > 0 || {1: 2}[n.read()] > 0)
                    }
                }
                assertEquals(expected: 1, actual: n.echoShort(value: n.i - 1))
                assertEquals(expected: 1, actual: n.echoWide(value: n.i - 1))
                assertNotNull(actual: DEFAULT(string))
                assertEquals(expected: 1, actual: n.echoLong(value: n.i - 1))
                assertNotNull(actual: DEFAULT(Box<string>).content)
                assertTrue(condition: DEFAULT(Box<Circle>).content.r > 0)
            }

            test "-- an odd name" {
                assertTrue(condition: true)
            }

            test "!!!" {
                // given
                let n = DEFAULT(Numbers)
                // when
                // and then
                assertEquals(expected: 1, actual: n.echoHuge(value: n.i - 1))
                assertFalse(condition: n.shape != n.shape)
            }

            test "date, time and durations are read from ISO 8601" {
                let first = toZonedDateTime(text: "2022-01-02T02:00:01.000000002Z")
                let second = toZonedDateTime(text: "2022-01-02T02:00:02Z")
                assertTrue(condition: first < second)
                assertTrue(condition: toDate(text: "2022-01-02") < toDate(text: "2022-01-03"))
                assertTrue(condition: toTime(text: "02:00:01") < toTime(text: "02:00:01.5"))
                assertTrue(condition: toDateTime(text: "2022-01-02T02:00:01") < toDateTime(text: "2022-01-02T02:00:02"))
                assertEquals(expected: toDuration(text: "PT90S"), actual: toDuration(text: "PT1M30S"))
                assertTrue(condition: toSeconds(value: 90) > toSeconds(value: 89))
            }

            test "a value that violates a validation annotation is rejected" {
                @@expectThrows(invalid-argument-error)
                Limited{count: 0}
            }

            test "a type literal is passed as class" {
                assertEquals(expected: 1, actual: Numbers.count(kind: Circle))
            }

            test "dates compare" {
                let day = DEFAULT(Numbers).day
                let other = DEFAULT(Numbers).day
                if day is not null {
                    if other is not null {
                        assertTrue(condition: day < other)
                    }
                }
            }
            """;

    private static List<GeneratedFile> generateWide() {
        final String spec = spec("").replace(SCHEMA, WIDE_SCHEMA).replace(INSTANCES, WIDE_INSTANCES.replace("\\\n", ""))
                .replace("```tests\n```", "```tests\n" + WIDE_TESTS + "```");
        final ValidationReport report = new MetaLang().validate(Map.of("f/b.md", spec));
        assertThat(report.diagnostics()).filteredOn(d -> d.severity() == Severity.ERROR).isEmpty();
        return new JavaGenerator(JavaGeneratorConfig.DEFAULT).generate(LinkedModel.of(report.model()));
    }

    @Test
    void numbersOfEveryKindShouldCompileAndRun() throws Exception {
        final List<GeneratedFile> files = generateWide();
        final String java = file(files, "/org/hiero/b/BSpecTest.java");
        assertThat(java)
                .contains("        final int bigger = checked(integer(n.i()).add(integer(1)), \"int32\").intValueExact();\n")
                .contains("n.echoUnsigned(checked(unsigned(n.u()).add(unsigned(1L)), \"uint64\").longValue())")
                .contains("n.echoSmall(checked(integer(n.small()).negate().add(integer(3L)), \"int8\").byteValueExact())")
                .contains("assertNotEquals(integer(n.i()), unsigned(n.u()));")
                .contains("assertTrue((n.shape() instanceof Circle) && !(!(n.shape() instanceof Circle)));")
                .contains("assertTrue((-n.d() < 0.0) && ((n.d() / 3.0) > 0.0));")
                .contains("assertTrue(n.m().multiply(n.m()).compareTo(n.m().negate()) >= 0);")
                .contains("assertInstanceOf(Circle.class, n.shape());")
                .contains("assertEquals(1, Numbers.count(Circle.class));")
                .contains("assertThrows(IllegalArgumentException.class, () -> new Limited(0));")
                .contains("assertEquals(toDuration(\"PT90S\"), toDuration(\"PT1M30S\"));")
                .contains("Map.ofEntries(Map.entry(1L, 1L)")
                .contains("    /// Deprecated: replaced\n")
                .contains("    void test1CallALater() {\n")
                .contains("        assertEquals(1, defaultNumbers().later().toCompletableFuture().join());\n")
                // the error of an @@async call is the cause of the CompletionException of join()
                .contains("""
                                assertThrows(IOException.class, () -> {
                                    try {
                                        defaultNumbers().readLater().toCompletableFuture().join();
                                    } catch (final CompletionException failed) {
                                        throw failed.getCause();
                                    }
                                });
                        """)
                .contains("    void test1CallALater2() throws Exception {\n        // when / then\n        assertEquals(")
                .contains("assertTrue(day.compareTo(other) < 0);")
                .contains("@SuppressWarnings(\"deprecation\")\n")
                .contains("    /// several\n")
                .contains("    void checkedCallsEverywhere() throws Exception {\n")
                .contains("n.echoShort(checked(integer(n.i()).subtract(integer(1)), \"uint8\").shortValueExact())")
                .contains("n.echoWide(checked(integer(n.i()).subtract(integer(1)), \"uint128\"))")
                .contains("    void anOddName() {\n")
                .contains("    void test() {\n")
                .contains("        // when\n        // and then\n")
                .contains("n.echoHuge(integer(n.i()).subtract(integer(1)))")
                .contains("n.echoLong(checked(integer(n.i()).subtract(integer(1)), \"int64\").longValueExact())")
                .contains("    private static Box<Circle> defaultBoxCircle() {\n")
                .contains("    private static Box<String> defaultBox() {\n");
        final GeneratedJava.Compilation modules = GeneratedJava.compile(files, temp);
        assertThat(modules.diagnostics()).isEmpty();
        final GeneratedJava.Compilation tests = GeneratedJava.compileTests(files, modules, temp);
        assertThat(tests.diagnostics()).isEmpty();
        final GeneratedJava.TestRun run = GeneratedJava.runTests(files.stream()
                .filter(f -> f.path().endsWith("SpecTest.java")).toList(), tests);
        assertThat(run.unexpected()).isEmpty();
    }

    @Test
    void aTypeWithTheNameOfTheTestClassShouldPreventIt() {
        final String spec = spec("test \"t\" {\n    assertTrue(condition: true)\n}\n")
                .replace("abstraction Shape {}", "abstraction Shape {}\n\nASpecTest {\n    @@immutable x: int32\n}");
        final ValidationReport report = new MetaLang().validate(Map.of("f/a.md", spec));
        assertThat(new JavaGenerator(JavaGeneratorConfig.DEFAULT).generate(LinkedModel.of(report.model())))
                .noneMatch(f -> f.path().endsWith("/src/test/java/org/hiero/a/ASpecTest.java"));
    }
}
