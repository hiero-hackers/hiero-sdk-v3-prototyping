package org.hiero.sdk.v3.metalang.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.hiero.sdk.v3.metalang.MetaLang;
import org.hiero.sdk.v3.metalang.ValidationReport;
import org.hiero.sdk.v3.metalang.diagnostic.Diagnostic;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * The types the operators of the test language accept and produce, and the problems they report.
 */
class TestOperatorsTest {

    private static final String SPEC = """
            # Values

            ## Description

            Text.

            ## API Schema

            ```
            namespace values

            abstraction Shape {}

            enum Level(code: int32) { LOW(1), HIGH(2) }

            Circle extends Shape {
                @@immutable r: int32
            }

            Box<$$T> {
                @@immutable content: $$T
            }

            Values {
                @@immutable i: int32
                @@immutable u: uint64
                @@immutable d: double
                @@immutable m: decimal
                @@immutable s: string
                @@immutable b: bytes
                @@immutable l: list<string>
                @@immutable n: map<string, int32>
                @@immutable shape: Shape
                @@immutable box: Box<string>
                @@immutable @@nullable maybe: int32
                @@immutable @@nullable day: date
                @@immutable @@nullable other: date
                @@immutable @@nullable parent: Values
                @@immutable @@nullable flag: bool
            }
            ```

            ## Default Instances for Testing

            ```
            instance Values = Values{i: 1, u: 2, d: 1.5, m: 2.5, s: "s", b: [1], l: ["x"], n: [], \
            shape: Circle{r: 1}, box: Box<string>{content: "c"}}
            ```

            ## Testing

            ```tests
            %s```

            ## Questions & Comments
            """;

    private static ValidationReport validate(final String statements) {
        return new MetaLang().validate(Map.of("values.md", SPEC.replace("\\\n", "").formatted(
                "test \"t\" {\n    let v = DEFAULT(Values)\n" + statements.indent(4) + "}\n")));
    }

    private static List<String> rules(final String statements) {
        return validate(statements).diagnostics().stream().map(Diagnostic::ruleId).filter(r -> r.startsWith("test."))
                .toList();
    }

    private static Type typeOf(final String expression) {
        final ValidationReport report = validate("let x = " + expression + "\nassertNotNull(actual: x)");
        assertThat(report.diagnostics()).filteredOn(d -> d.ruleId().startsWith("test.")).isEmpty();
        final TestDefinition test = LinkedModel.of(report.model()).tests().getFirst();
        return ((TestDefinition.Let) test.body().get(1)).type();
    }

    @ParameterizedTest
    @CsvSource(delimiter = ';', value = {
        "v.i + v.u             ; int256",
        "-v.i                  ; int256",
        "v.i * 2 - 1           ; int256",
        "v.d * 2.0 / v.d       ; double",
        "-v.d                  ; double",
        "v.m + v.m * v.m       ; decimal",
        "-v.m                  ; decimal",
        "v.s + \"!\"           ; string",
        "v.b + hex(text: \"00\") ; bytes",
        "v.l + [\"y\"]         ; list<string>",
        "v.l[0]                ; string",
        "v.n[\"k\"]            ; int32",
        "{1: \"a\"}            ; map<int64, string>",
        "[1, v.i]              ; list<int32>",
        "[-1, 2]               ; list<int64>",
        "v.box.content         ; string",
        "v.i < 2 || v.d >= 1.0 ; bool",
        "v.shape is Circle     ; bool",
        "v.shape is not Circle ; bool",
        "v.parent is null      ; bool",
        "v.i != 3              ; bool",
        "not(value: v.i == 1)  ; bool",
        "v.i % 2 == 1          ; bool",
        "abs(value: -v.i)      ; int256",
        "toString(value: v.m)  ; string",
        "Level.HIGH.code       ; int32",
        "Level.HIGH.code + 1   ; int256",
    })
    void operatorsShouldHaveTheirTypes(final String expression, final String type) {
        assertThat(typeOf(expression).text()).isEqualTo(type);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "let x = -v.s",
        "let x = v.s - v.s",
        "let x = v.m / v.m",
        "let x = v.s < v.s",
        "let x = v.i + v.d",
        "let x = v.l + [1]",
        "let x = v.i && true",
        "let x = v.s[0]",
        "let x = v.l[\"a\"]",
        "let x = v.i is Circle",
        "let x = v.shape is Values",
        "let x = {}",
        "let x = []",
        "let x: Values = {\"a\": 1}",
        "let x = v.missing",
        "let x = v.s.missing()",
        "let x = DEFAULT",
        "assertInstanceOf(expected: Unknown, actual: v)",
        "let x = Level.HIGH.missing",
        "let x = Level.MIDDLE.code",
        "assertEquals(expected: v.s, actual: v.i)",
        "assertEquals(expected: 1, actual: \"a\", message: 2)",
        "let x = contains(value: v.l, element: 1)",
    })
    void wrongOperandsShouldBeInvalid(final String statement) {
        assertThat(rules(statement + "\nassertTrue(condition: true)")).contains("test.invalid");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "let x = v.maybe + 1",
        "let x = -v.maybe",
        "let x = v.maybe > 1",
        "let x = true && v.flag",
        "let x = v.parent.i",
        "let x = v.parent.toString()",
        "let x = v.day < v.other",
        "for c in v.parent.l {\n    fail(message: \"x\")\n}",
        "if v.parent.parent is null {\n    fail(message: \"x\")\n}\nlet y = v.parent[0]",
    })
    void nullableOperandsShouldBeReported(final String statement) {
        assertThat(rules(statement + "\nassertTrue(condition: true)")).contains("test.nullable");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        // narrowed in the else branch of `is null`
        "let p = v.parent\nif p is null {\n    fail(message: \"no parent\")\n} else {\n    assertEquals(expected: 1, actual: p.i)\n}",
        // a type literal
        "assertInstanceOf(expected: Circle, actual: v.shape)",
        // comparisons of the same temporal type, after narrowing
        "let a = v.day\nlet c = v.other\nif a is not null {\n    if c is not null {\n        assertTrue(condition: a < c)\n    }\n}",
        // the message of an assertion
        "assertTrue(condition: true, message: \"always\")",
        // a qualified built-in function
        "assertTrue(condition: testing.contains(value: v.l, element: \"x\"))",
        // a declared type
        "let w: Shape = v.shape\nassertNotNull(actual: w)",
    })
    void validTestsShouldResolve(final String statements) {
        assertThat(rules(statements)).isEmpty();
    }

    @org.junit.jupiter.api.Test
    void statementsShouldBeChecked() {
        assertThat(rules("for i in v.l {\n    let i = 1\n}\nassertTrue(condition: true)"))
                .contains("test.duplicate-name");
        assertThat(rules("for i in v.i {\n    fail(message: \"x\")\n}")).contains("test.invalid");
        assertThat(rules("if v.maybe {\n    fail(message: \"x\")\n}")).contains("test.invalid");
        assertThat(rules("if v.flag {\n    fail(message: \"x\")\n}")).contains("test.nullable");
        assertThat(rules("@@expectThrows(illegal-format)\nassertTrue(condition: true)"))
                .contains("test.undeclared-error");
        assertThat(rules("@@other\nassertTrue(condition: true)")).contains("test.invalid");
        assertThat(rules("@@expectThrows(\"x\")\nassertTrue(condition: true)")).contains("test.invalid");
    }
}
