package org.hiero.sdk.v3.metalang.generator.ts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

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
 * The TypeScript tests generated from the {@code tests} blocks of the specs: they compile with the strict
 * configuration against the generated packages and the test support package, and they behave as the test language
 * defines when they run.
 */
class TsSpecTestGeneratorTest {

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
                assertEquals(expected: Point{x: 2, big: 18446744073709551615, label: "aℏ", data: [1, 2],
                        tags: ["a", "b"]}, actual: p)
                assertNotEquals(unexpected: Point{x: 3, big: 1, label: "", data: [], tags: []}, actual: p)
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

            test "an enum constant is an instance of its enum" {
                assertInstanceOf(expected: Color, actual: Color.RED)
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

            test "named test values without TypeScript form" {
                for holder in HOLDERS {
                    assertNotNull(actual: holder)
                }
            }

            test "strings, bytes and lists of the test language" {
                assertEquals(expected: "ℏb", actual: substring(value: "aℏbc", start: 1, end: 3))
                assertTrue(condition: contains(value: "abc", part: "b") && endsWith(value: "abc", suffix: "c"))
                assertEquals(expected: "xxx", actual: repeat(value: "x", count: 3))
                assertEquals(expected: hex(text: "02 03"), actual: slice(value: hex(text: "010203"), start: 1, end: 3))
                assertTrue(condition: contains(value: hex(text: "010203"), part: hex(text: "0203")))
                assertTrue(condition: startsWith(value: hex(text: "0102"), prefix: hex(text: "01"))
                        && endsWith(value: hex(text: "0102"), suffix: hex(text: "02")))
                assertEquals(expected: repeat(value: hex(text: "ab"), count: 2), actual: hex(text: "ABab"))
                assertEquals(expected: utf8(text: "hi"), actual: base64(text: "aGk="))
                assertEquals(expected: "0aff", actual: toHex(value: hex(text: "0AFF")))
                assertEquals(expected: ["b"], actual: slice(value: ["a", "b"], start: 1, end: 2))
                let letters: list<string> = ["a", "b"]
                assertTrue(condition: contains(value: letters, element: "b"))
                assertEquals(expected: [0, 1, 2], actual: range(start: 0, end: 3))
                assertEquals(expected: 2, actual: length(value: values(value: {"x": 1, "y": 2})))
                assertEquals(expected: ["x"], actual: keys(value: {"x": 1}))
            }

            test "numbers of the test language" {
                let minusFive: int64 = -5
                let one: int64 = 1
                let two: int64 = 2
                let half: double = -1.5
                let first: double = 1.0
                let second: double = 2.0
                assertEquals(expected: 5, actual: abs(value: minusFive))
                assertEquals(expected: 1, actual: min(a: one, b: two))
                assertEquals(expected: 2, actual: max(a: one, b: two))
                assertApprox(expected: 1.5, actual: abs(value: half), tolerance: 0.0)
                assertTrue(condition: approx(actual: min(a: first, b: second), expected: 1.0, tolerance: 0.0)
                        && max(a: first, b: second) == 2.0)
                assertTrue(condition: not(value: isNaN(value: 1.0)))
                assertEquals(expected: "-1234", actual: toString(value: -1234))
                assertEquals(expected: 42, actual: toInt(text: "42"))
                assertEquals(expected: 3.0, actual: toDouble(value: 3))
                assertEquals(expected: toDecimal(value: 0.5), actual: divide(dividend: toDecimal(value: 1),
                        divisor: toDecimal(value: 2), scale: 1))
                assertEquals(expected: "0.5", actual: toString(value: toDecimal(value: 0.5)))
                assertEquals(expected: toDecimal(value: 2), actual: abs(value: toDecimal(value: -2)))
                assertEquals(expected: toDecimal(value: 1), actual: min(a: toDecimal(value: 1), b: toDecimal(value: 2)))
                assertEquals(expected: toDecimal(value: 2), actual: max(a: toDecimal(value: 1), b: toDecimal(value: 2)))
                assertEquals(expected: 0.5, actual: toDouble(value: toDecimal(value: 0.5)))
            }
            """;

    @TempDir
    Path temp;

    private static String spec(final String schema, final String instances, final String tests) {
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
                """.formatted(schema, instances, tests);
    }

    private static List<GeneratedFile> generate(final String schema, final String instances, final String tests) {
        final ValidationReport report = new MetaLang().validate(Map.of("f/a.md", spec(schema, instances, tests)));
        assertThat(report.diagnostics()).filteredOn(d -> d.severity() == Severity.ERROR).isEmpty();
        return new TsGenerator().generate(LinkedModel.of(report.model()));
    }

    private static String file(final List<GeneratedFile> files, final String suffix) {
        return files.stream().filter(f -> f.path().endsWith(suffix)).findFirst()
                .orElseThrow(() -> new AssertionError("no " + suffix)).content();
    }

    @Test
    void theTestsShouldCompileAndBehaveAsTheTestLanguageDefines() throws Exception {
        assumeTrue(GeneratedTs.available(), "node and sdk-ts/node_modules (npm install) are needed");
        final List<GeneratedFile> files = generate(SCHEMA, INSTANCES, TESTS);
        assertThat(GeneratedTs.build(files, temp, "@hiero")).isEmpty();
        final GeneratedTs.TestRun run = GeneratedTs.test(temp);
        assertThat(run.failures()).isEmpty();
        // the expected error and the method call reach a method stub; the disabled test does not run
        assertThat(run.stubs()).contains("a method stub", "an expected error");
        assertThat(run.stubs()).doesNotContain("a record returns its values", "integers are exact", "control flow",
                "maps and indexes", "named test values", "strings, bytes and lists of the test language",
                "numbers of the test language");
    }

    @Test
    void theTestFileShouldLookLikeTheOtherGeneratedTests() {
        final String ts = file(generate(SCHEMA, INSTANCES, TESTS), "/src/a/a.spec.test.ts");
        assertThat(ts)
                .startsWith(TsGenerator.HEADER + "//\n// Tests of the spec `a`, generated from the `tests` blocks of its"
                        + " `## Testing` section.\n//\n// Not generated:\n"
                        + "// - values `HOLDERS`: no value of a.Holder\n"
                        + "// - `named test values without TypeScript form`: the values HOLDERS have no TypeScript form\n")
                .contains("import assert from \"node:assert/strict\";\nimport { describe, test } from \"node:test\";\n")
                .contains("from \"@hiero/test-support\";\n")
                .contains("/**\n * labels of one letter\n */\nfunction LABELS(): ReadonlyArray<string> {\n"
                        + "    return [\"a\", \"b\"];\n}\n")
                .contains("describe(\"a\", () => {\n    // `a/a-record-returns-its-values`\n    //\n"
                        + "    // the attributes of a record\n    test(\"a record returns its values\", () => {\n"
                        + "        // given\n        const p: Point = defaultPoint();\n\n        // then\n")
                .contains("        assertEquals(2, p.x); // an int32\n")
                .contains("assertTrue(equal(-7n / 2n, -3n) && equal(-7n % 2n, -1n));")
                .contains("assertTrue((int(p.x) + 1n) > int(p.x));")
                .contains("} else if (t !== null) {")
                .contains("assertTrue(Color.RED !== Color.GREEN);")
                .contains("const m: ReadonlyMap<string, bigint> = new Map<string, bigint>([[\"one\", 1n], [\"two\", 2n]]);")
                .contains("assertEquals(\"b\", get(defaultPoint().tags, 1n));")
                .contains("assert.throws(() => Point.parse(\"\"), RangeError);")
                .contains("test(\"a disabled test\", { skip: \"not yet\" }, () => {")
                .contains("assertInstanceOf(Color, Color.RED);")
                .contains("for (const label of LABELS()) {")
                .contains("test.todo(\"not generated: `named test values without TypeScript form`")
                .contains("\nfunction defaultPoint(): Point {\n    return new Point({ x: 2, big: 18446744073709551615n,")
                .contains("assertApprox(1.5, absDouble(half), 0.0);")
                .contains("assertEquals(\"-1234\", text(-1234n));")
                .contains("assertEquals(decimal(toDecimal(2n)), decimal(absDecimal(toDecimal(-2n))));")
                .contains("assertEquals(0.5, toDoubleDecimal(toDecimalDouble(0.5)));")
                .doesNotContain("HOLDERS =");
    }

    @Test
    void onlyAPackageWithTestsShouldDependOnTheTestSupport() {
        final List<GeneratedFile> files = generate(SCHEMA, INSTANCES, TESTS);
        assertThat(file(files, "packages/f/package.json")).contains("  \"devDependencies\": {\n"
                + "    \"@hiero/test-support\": \"0.1.0-SNAPSHOT\"\n  }\n}\n");
        assertThat(file(files, "packages/f/tsconfig.json")).contains("{ \"path\": \"../../../test-support\" }");
        assertThat(files.stream().filter(f -> f.path().equals("package.json")).findFirst().orElseThrow().content())
                .contains("\"../test-support\"");
        // the contract tests use it as well (assertValue); a package without any test does not
        final List<GeneratedFile> withoutSpecTests = new TsGenerator().generate(LinkedModel.of(new MetaLang()
                .validate(Map.of("f/a.md", spec(SCHEMA, INSTANCES.replaceAll("(?m)^(//|values).*\\n", ""), "")
                        .replace("```tests\n```\n", ""))).model()));
        assertThat(withoutSpecTests).noneMatch(f -> f.path().endsWith(".spec.test.ts"));
        assertThat(file(withoutSpecTests, "packages/f/package.json")).contains("@hiero/test-support");
        final List<GeneratedFile> withoutTests = new TsGenerator().generate(LinkedModel.of(new MetaLang()
                .validate(Map.of("f/a.md", "# A\n\n## Description\n\nText.\n\n## API Schema\n\n```\nnamespace a\n"
                        + "abstraction Shape {}\n```\n\n## Questions & Comments\n")).model()));
        assertThat(withoutTests).noneMatch(f -> f.content().contains("test-support"));
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

            Numbers {
                @@immutable small: int8
                @@immutable i: int32
                @@immutable u: uint64
                @@immutable d: double
                @@immutable m: decimal
                @@immutable b: bytes
                @@immutable l: list<string>
                @@immutable s: set<string>
                @@immutable shape: Shape
                @@immutable box: Box<string>
                @@immutable @@nullable day: date

                int32 echo(value: int32)
                uint64 echoUnsigned(value: uint64)
                int8 echoSmall(value: int8)
                int256 echoHuge(value: int256)
                @@async int32 later()
                @@async @@throws(illegal-format) int32 fails()
                @@throws(not-found-error) int32 find()
                @@static int32 count(kind: type)
            }
            """;

    private static final String WIDE_INSTANCES = """
            instance Numbers = Numbers{small: 1, i: 2, u: 3, d: 1.5, m: 2.5, b: [1], l: ["x"], s: ["y"], \
            shape: Circle{r: 1}, box: Box<string>{content: "c"}}
            """;

    private static final String WIDE_TESTS = """
            test "doubles and decimals" {
                let n = DEFAULT(Numbers)
                assertApprox(expected: 3.0, actual: n.d * 2.0, tolerance: 0.001)
                assertTrue(condition: -n.d < 0.0 && n.d / 3.0 > 0.0)
                assertEquals(expected: toDecimal(value: 5), actual: n.m + n.m)
                assertTrue(condition: n.m * n.m >= -n.m)
                assertTrue(condition: n.m - n.m <= n.m && n.m == n.m)
            }

            test "values change their kind" {
                let n = DEFAULT(Numbers)
                let bigger: int32 = n.i + 1
                assertEquals(expected: 3, actual: n.echo(value: n.i + 1))
                assertEquals(expected: 4, actual: n.echoUnsigned(value: n.u + 1))
                assertEquals(expected: 2, actual: n.echoSmall(value: -n.small + 3))
                assertEquals(expected: 3, actual: bigger)
                assertEquals(expected: 1, actual: n.echoHuge(value: n.i - 1))
                assertNotEquals(unexpected: n.i, actual: n.u)
            }

            test "values are joined" {
                let n = DEFAULT(Numbers)
                assertEquals(expected: hex(text: "0102"), actual: n.b + hex(text: "02"))
                assertEquals(expected: ["x", "y"], actual: n.l + ["y"])
                assertTrue(condition: n.shape is Circle && not(value: n.shape is not Circle))
                assertInstanceOf(expected: Circle, actual: n.shape)
                assertEquals(expected: "c", actual: n.box.content)
                assertTrue(condition: contains(value: n.s, element: "y") && n.s == ["y"])
                assertEquals(expected: 11, actual: length(value: {1: 1, 2: 2, 3: 3, 4: 4, 5: 5, 6: 6, 7: 7, 8: 8,
                        9: 9, 10: 10, 11: 11}))
            }

            @@deprecated("replaced")
            test "async calls are awaited" {
                assertEquals(expected: 1, actual: DEFAULT(Numbers).later())
            }

            test "an asynchronous error is awaited" {
                @@expectThrows(illegal-format)
                DEFAULT(Numbers).fails()
            }

            test "a custom error is expected" {
                @@expectThrows(not-found-error)
                DEFAULT(Numbers).find()
            }

            test "an abstraction has no type at runtime" {
                assertTrue(condition: DEFAULT(Numbers).shape is Shape)
            }

            test "literals, defaults and parentheses" {
                let n = DEFAULT(Numbers)
                assertEquals(expected: 2, actual: {"a": 2}["a"])
                assertNotNull(actual: DEFAULT(string))
                assertNotNull(actual: DEFAULT(Box<string>).content)
                assertTrue(condition: (n.i > 0 && n.i < 9) || n.day is null)
                assertTrue(condition: -(-n.i) > 0 && (n.shape is Circle) == true)
            }

            test "type literals are classes" {
                assertEquals(expected: 1, actual: Numbers.count(kind: Circle))
            }

            test "an interface has no class at runtime" {
                assertEquals(expected: 1, actual: Numbers.count(kind: Shape))
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

    @Test
    void numbersOfEveryKindShouldCompileAndRun() throws Exception {
        final List<GeneratedFile> files = generate(WIDE_SCHEMA, WIDE_INSTANCES.replace("\\\n", ""), WIDE_TESTS);
        final String ts = file(files, "/src/b/a.spec.test.ts");
        assertThat(ts)
                .contains("const bigger: number = checkedNumber(int(n.i) + 1n, \"int32\");")
                .contains("n.echoUnsigned(checked(n.u + 1n, \"uint64\"))")
                .contains("n.echoSmall(checkedNumber((-int(n.small)) + 3n, \"int8\"))")
                .contains("n.echoHuge(int(n.i) - 1n)")
                .contains("assertTrue((n.shape instanceof Circle) && !(!(n.shape instanceof Circle)));")
                .contains("assertTrue(((-n.d) < 0.0) && ((n.d / 3.0) > 0.0));")
                .contains("assertTrue(compareDecimal(multiplyDecimal(n.m, n.m), negateDecimal(n.m)) >= 0);")
                .contains("equal(decimal(n.m), decimal(n.m))")
                .contains("assertInstanceOf(Circle, n.shape);")
                .contains("    // Deprecated: replaced\n")
                .contains("test(\"async calls are awaited\", async () => {\n"
                        + "        assertEquals(1, (await defaultNumbers().later()));\n")
                .contains("await assert.rejects(async () => (await defaultNumbers().fails()), RangeError);")
                .contains("assertTrue(compare(day, other) < 0);")
                .contains("concat(n.b, hex(\"02\"))")
                .contains("assertEquals(1, Numbers.count(Circle));")
                .contains("// - `an interface has no class at runtime`: the type b.Shape has no class at runtime\n")
                .contains("assert.throws(() => defaultNumbers().find(), NotFoundError);")
                .contains("// - `an abstraction has no type at runtime`: 'is b.Shape': only a class has a type at runtime\n")
                .contains("assertEquals(2n, get(new Map<string, bigint>([[\"a\", 2n]]), \"a\"));")
                .contains("function defaultBox(): Box<string> {")
                .contains("assertTrue(((int(n.i) > 0n) && (int(n.i) < 9n)) || (n.day === null));");
        assumeTrue(GeneratedTs.available(), "node and sdk-ts/node_modules (npm install) are needed");
        assertThat(GeneratedTs.build(files, temp, "@hiero")).isEmpty();
        final GeneratedTs.TestRun run = GeneratedTs.test(temp);
        assertThat(run.failures()).isEmpty();
        assertThat(run.stubs()).contains("values change their kind", "async calls are awaited",
                "an asynchronous error is awaited");
        assertThat(run.stubs()).contains("a custom error is expected");
        assertThat(run.stubs()).doesNotContain("doubles and decimals", "values are joined", "dates compare",
                "literals, defaults and parentheses");
    }

    @Test
    void theFileNameShouldFollowTheSpecFile() {
        assertThat(TsSpecTestGenerator.fileName("native-token")).isEqualTo("native-token.spec.test.ts");
    }
}
