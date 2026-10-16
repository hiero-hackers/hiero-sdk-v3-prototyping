package org.hiero.sdk.v3.metalang.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.hiero.sdk.v3.metalang.MetaLang;
import org.hiero.sdk.v3.metalang.ValidationReport;
import org.hiero.sdk.v3.metalang.diagnostic.Diagnostic;
import org.hiero.sdk.v3.metalang.source.MarkdownSchemaExtractor;
import org.junit.jupiter.api.Test;

/**
 * The resolution of the tests in the specs: types of literals and operators, variables, built-in functions, narrowing
 * of nullable values and the ids of the tests.
 */
class TestResolverTest {

    private static final String SCHEMA = """
            namespace shop

            enum Size { SMALL, LARGE }

            Item {
                @@immutable name: string
                @@immutable count: uint64
                @@immutable size: Size
                @@immutable @@nullable note: string
                @@immutable tags: list<string>

                @@throws(illegal-format) @@static Item parse(text: string)
                Item withCount(count: uint64)
            }
            """;

    private static String spec(final String tests) {
        return spec("", tests);
    }

    private static String spec(final String values, final String tests) {
        return """
                # Shop

                ## Description

                Text.

                ## API Schema

                ```
                %s```

                ## Default Instances for Testing

                ```
                instance Item = Item{name: "apple", count: 3, size: Size.SMALL, tags: ["red"]}
                %s```

                ## Testing

                ```tests
                %s```

                ## Questions & Comments
                """.formatted(SCHEMA, values, tests);
    }

    private static ValidationReport validate(final String tests) {
        return new MetaLang().validate(Map.of("shop.md", spec(tests)));
    }

    private static List<TestDefinition> tests(final String tests) {
        final ValidationReport report = validate(tests);
        assertThat(report.diagnostics()).filteredOn(d -> d.ruleId().startsWith("test.")).isEmpty();
        return LinkedModel.of(report.model()).tests();
    }

    private static List<String> rules(final String tests) {
        return validate(tests).diagnostics().stream().map(Diagnostic::ruleId).filter(r -> r.startsWith("test."))
                .toList();
    }

    private static ValidationReport validate(final String values, final String tests) {
        return new MetaLang().validate(Map.of("shop.md", spec(values, tests)));
    }

    private static List<String> rules(final String values, final String tests) {
        return validate(values, tests).diagnostics().stream().map(Diagnostic::ruleId)
                .filter(r -> r.startsWith("test.")).toList();
    }

    private static InstanceExpression.Call assertion(final TestDefinition test, final int index) {
        return (InstanceExpression.Call) ((TestDefinition.Evaluate) test.body().get(index)).expression();
    }

    @Test
    void namedTestValuesShouldBeVariablesOfTheTestsOfTheirSpec() {
        final ValidationReport report = validate("""
                // the names to try
                values NAMES: list<string> = ["a", "b"]
                values ITEMS: list<Item> = [DEFAULT(Item), Item.parse(text: "x")]
                """, """
                test "uses them" {
                    for name in NAMES {
                        assertTrue(condition: length(value: name) == 1)
                    }
                    assertEquals(expected: 2, actual: length(value: ITEMS))
                }
                """);
        assertThat(report.diagnostics()).filteredOn(d -> d.ruleId().startsWith("test.")).isEmpty();
        final LinkedModel model = LinkedModel.of(report.model());
        assertThat(model.testValues()).extracting(TestValuesDefinition::name).containsExactly("NAMES", "ITEMS");
        final TestValuesDefinition names = model.testValues().getFirst();
        assertThat(names.type().text()).isEqualTo("list<string>");
        assertThat(names.namespace()).isEqualTo("shop");
        assertThat(names.specName()).isEqualTo("shop");
        assertThat(names.documentation()).isEqualTo("the names to try");
        final TestDefinition.For loop = (TestDefinition.For) model.tests().getFirst().body().getFirst();
        assertThat(loop.iterable()).isEqualTo(new InstanceExpression.Variable("NAMES", names.type(), false));
    }

    @Test
    void namedTestValuesShouldUseTheStringEscapesOfTheTestLanguage() {
        final LinkedModel model = LinkedModel.of(validate("values TEXTS: list<string> = [\"a\\nb\", \"\\u{1F600}\"]\n",
                """
                test "uses them" {
                    assertEquals(expected: 3, actual: length(value: TEXTS[0]))
                }
                """).model());
        final InstanceExpression.ListValue texts = (InstanceExpression.ListValue) model.testValues().getFirst()
                .value();
        assertThat(texts.items()).extracting(i -> ((org.hiero.sdk.v3.metalang.ast.Literal.StringLiteral)
                ((InstanceExpression.Value) i).literal()).value()).containsExactly("a\nb", "\uD83D\uDE00");
    }

    @Test
    void namedTestValuesShouldBeReported() {
        final String uses = """
                test "uses them" {
                    for name in NAMES {
                        assertTrue(condition: length(value: name) == 1)
                    }
                }
                """;
        assertThat(rules("values NAMES: list<string> = [\"a\"]\nvalues NAMES: list<string> = []\n", uses))
                .containsExactly("test.duplicate-values");
        assertThat(rules("values NAMES: list<string> = [1]\n", uses)).contains("test.invalid");
        assertThat(rules("values NAMES: list<string> = [\"a\"]\nvalues NOTE: string = DEFAULT(Item).note\n",
                uses)).containsExactly("test.nullable");
        assertThat(rules("values NAMES: list<string> = [\"a\"]\nvalues OTHER: list<string> = []\n", uses))
                .containsExactly("test.unused-values");
        assertThat(rules("values NAMES: list<string> = [\"a\"]\n", """
                test "binds the name again" {
                    let NAMES = 1
                    assertEquals(expected: 1, actual: NAMES)
                }
                """)).contains("test.duplicate-name");
    }

    @Test
    void namedTestValuesWithoutTestsShouldBeReportedAsUnused() {
        assertThat(rules("values NAMES: list<string> = [\"a\"]\n", "")).containsExactly("test.unused-values");
    }

    @Test
    void aViolatedValidationAnnotationShouldBeAnInvalidArgumentError() {
        final String schema = """
                namespace v

                Limited {
                    @@immutable @@min(1) count: int32
                }

                @@oneOf(a, b)
                Choice {
                    @@immutable @@nullable a: string
                    @@immutable @@nullable b: string
                }

                @@oneOf(a, b)
                Pending {
                    @@nullable a: string
                    @@nullable b: string
                }

                Plain {
                    @@immutable count: int32
                }

                Service {
                    @@static int32 take(@@maxLength(3) text: string)
                    @@static int32 free(text: string)
                }
                """;
        final java.util.function.Function<String, List<String>> rules = tests -> new MetaLang().validate(Map.of(
                "v.md", "# V\n\n## Description\n\nText.\n\n## API Schema\n\n```\n" + schema + "```\n\n## Testing\n\n"
                        + "```tests\n" + tests + "```\n\n## Questions & Comments\n")).diagnostics().stream()
                .map(Diagnostic::ruleId).filter(r -> r.startsWith("test.")).toList();
        // a construction or call checked by a validation annotation or an immutable @@oneOf group
        assertThat(rules.apply("""
                test "rejected" {
                    @@expectThrows(invalid-argument-error)
                    Limited{count: 0}
                    @@expectThrows(invalid-argument-error)
                    Choice{a: "x", b: "y"}
                    @@expectThrows(invalid-argument-error)
                    Service.take(text: "four")
                }
                """)).isEmpty();
        // nothing checks these values: no validation annotation, a mutable group, no annotated parameter
        for (final String statement : List.of("Plain{count: 0}", "Pending{a: \"x\", b: \"y\"}",
                "Service.free(text: \"four\")")) {
            assertThat(rules.apply("test \"rejected\" {\n    @@expectThrows(invalid-argument-error)\n    "
                    + statement + "\n}\n")).as(statement).containsExactly("test.undeclared-error");
        }
    }

    @Test
    void testsShouldGetIdNamespaceAndSpecName() {
        final TestDefinition test = tests("""
                @@disabled("not yet")
                test "fromString Reads: the number!" {
                    assertTrue(condition: true)
                }
                """).getFirst();
        assertThat(test.id()).isEqualTo("shop/fromstring-reads-the-number");
        assertThat(test.namespace()).isEqualTo("shop");
        assertThat(test.specName()).isEqualTo("shop");
        assertThat(test.annotation("disabled")).isPresent();
    }

    @Test
    void aLiteralShouldTakeTheTypeTheOtherArgumentBindsTheTypeParameterTo() {
        final TestDefinition test = tests("""
                test "count" {
                    let item = DEFAULT(Item)
                    assertEquals(expected: 3, actual: item.count)
                    assertEquals(expected: Size.SMALL, actual: item.size)
                }
                """).getFirst();
        final InstanceExpression.Call equals = assertion(test, 1);
        assertThat(equals.namespace()).isEqualTo(Builtins.NAMESPACE);
        assertThat(equals.arguments().get("expected").type().text()).isEqualTo("uint64");
        assertThat(equals.arguments().get("actual")).isInstanceOf(InstanceExpression.Access.class);
        assertThat(assertion(test, 2).arguments().get("expected").type().text()).isEqualTo("shop.Size");
    }

    @Test
    void variablesShouldResolveWithTheirAttributesAndMethods() {
        final TestDefinition test = tests("""
                test "variables" {
                    let item = Item.parse(text: "apple")
                    let other: Item = item.withCount(count: 4)
                    assertEquals(expected: item.name, actual: other.name)
                    assertEquals(expected: 1, actual: length(value: item.tags))
                }
                """).getFirst();
        final TestDefinition.Let other = (TestDefinition.Let) test.body().get(1);
        assertThat(other.value()).isInstanceOf(InstanceExpression.MethodCall.class);
        assertThat(other.type().text()).isEqualTo("shop.Item");
        final InstanceExpression.Call length = (InstanceExpression.Call) assertion(test, 3).arguments().get("actual");
        assertThat(length.method().parameters().getFirst().type().text()).isEqualTo("list<$$T>");
        assertThat(length.type().text()).isEqualTo("int64");
    }

    @Test
    void integerArithmeticShouldNotOverflow() {
        final TestDefinition test = tests("""
                test "arithmetic" {
                    let item = DEFAULT(Item)
                    let sum = item.count + 1
                    assertTrue(condition: sum > 3 && -sum < 0)
                    assertEquals(expected: "ab", actual: "a" + "b")
                }
                """).getFirst();
        final TestDefinition.Let sum = (TestDefinition.Let) test.body().get(1);
        assertThat(sum.type().text()).isEqualTo("int256");
        final InstanceExpression.Binary add = (InstanceExpression.Binary) sum.value();
        assertThat(add.right().type().text()).isEqualTo("uint64");
        assertThat(assertion(test, 3).arguments().get("actual").type().text()).isEqualTo("string");
    }

    @Test
    void loopsShouldIterateListsWithTheElementType() {
        final TestDefinition test = tests("""
                test "sizes" {
                    for size in [Size.SMALL, Size.LARGE] {
                        assertNotNull(actual: size)
                    }
                    for i in range(start: 0, end: 2) {
                        assertTrue(condition: i >= 0)
                    }
                }
                """).getFirst();
        final TestDefinition.For sizes = (TestDefinition.For) test.body().getFirst();
        assertThat(sizes.element().text()).isEqualTo("shop.Size");
        assertThat(((TestDefinition.For) test.body().get(1)).element().text()).isEqualTo("int64");
    }

    @Test
    void aNullableVariableShouldBeUsableAfterItIsChecked() {
        assertThat(rules("""
                test "note" {
                    let note = DEFAULT(Item).note
                    if note is not null {
                        assertEquals(expected: 4, actual: length(value: note))
                    }
                    assertNotNull(actual: note)
                    assertTrue(condition: length(value: note) > 0)
                }
                """)).isEmpty();
        assertThat(rules("""
                test "note" {
                    let note = DEFAULT(Item).note
                    assertTrue(condition: length(value: note) > 0)
                }
                """)).containsExactly("test.nullable");
    }

    @Test
    void expectThrowsShouldNameAnErrorOfTheCall() {
        final TestDefinition test = tests("""
                test "parse" {
                    @@expectThrows(illegal-format)
                    Item.parse(text: "")
                }
                """).getFirst();
        assertThat(((TestDefinition.Evaluate) test.body().getFirst()).expectedError()).isEqualTo("illegal-format");
        // only a call can be annotated: an annotated `let` is no syntax of the language
        assertThat(validate("test \"t\" {\n    @@expectThrows(illegal-format)\n    let x = 1\n}\n").diagnostics())
                .extracting(Diagnostic::ruleId).contains("syntax.error");
    }

    @Test
    void misplacedAnnotationsShouldBeReported() {
        assertThat(rules("@@disabled\ntest \"t\" {\n    assertTrue(condition: true)\n}\n")).containsExactly("test.invalid");
        assertThat(rules("@@immutable\ntest \"t\" {\n    assertTrue(condition: true)\n}\n"))
                .containsExactly("test.invalid");
        assertThat(rules("test \"t\" {\n    @@expectThrows(illegal-format) @@expectThrows(illegal-format)\n"
                + "    Item.parse(text: \"\")\n}\n")).containsExactly("test.invalid");
    }

    @Test
    void mapsListsAndIndexesShouldBeTyped() {
        final TestDefinition test = tests("""
                test "collections" {
                    let counts = {"a": 1, "b": 2}
                    let tags = DEFAULT(Item).tags
                    assertEquals(expected: 1, actual: counts["a"])
                    assertEquals(expected: "red", actual: tags[0])
                    assertTrue(condition: containsKey(value: counts, key: "b"))
                }
                """).getFirst();
        assertThat(((TestDefinition.Let) test.body().getFirst()).type().text()).isEqualTo("map<string, int64>");
        assertThat(assertion(test, 3).arguments().get("actual").type().text()).isEqualTo("string");
    }

    @Test
    void wrongTypesShouldBeReported() {
        assertThat(rules("test \"t\" {\n    assertTrue(condition: 1)\n}\n")).containsExactly("test.invalid");
        assertThat(rules("test \"t\" {\n    let d = 1.5\n    assertTrue(condition: d / 2.0 > \"x\")\n}\n"))
                .containsExactly("test.invalid");
        assertThat(rules("test \"t\" {\n    for x in DEFAULT(Item) {\n        fail(message: \"x\")\n    }\n}\n"))
                .containsExactly("test.invalid");
        assertThat(rules("test \"t\" {\n    assertTrue(condition: DEFAULT(Item) is Size)\n}\n"))
                .containsExactly("test.invalid");
    }

    @Test
    void theBuiltInsOfTheGuidelineShouldBeTheBundledOnes() throws IOException {
        final Path guideline = Path.of(System.getProperty("spec.root", "../../../spec"))
                .resolve("../guidelines/test-language.md");
        final MarkdownSchemaExtractor extractor = new MarkdownSchemaExtractor();
        final String inGuideline = extractor.codeBlocks(Files.readString(guideline)).stream()
                .filter(b -> b.content().startsWith("namespace testing")).findFirst().orElseThrow().content();
        final String bundled = extractor.extract("testing.md", Builtins.markdown()).schema().orElseThrow().text();
        assertThat(bundled).isEqualTo(inGuideline);
        assertThat(Builtins.functions()).extracting(f -> f.method().name()).contains("assertEquals", "length", "hex");
    }
}
