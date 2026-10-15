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
                ```

                ## Testing

                ```tests
                %s```

                ## Questions & Comments
                """.formatted(SCHEMA, tests);
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

    private static InstanceExpression.Call assertion(final TestDefinition test, final int index) {
        return (InstanceExpression.Call) ((TestDefinition.Evaluate) test.body().get(index)).expression();
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
