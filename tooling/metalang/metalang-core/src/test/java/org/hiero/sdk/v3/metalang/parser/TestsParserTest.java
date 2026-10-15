package org.hiero.sdk.v3.metalang.parser;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.hiero.sdk.v3.metalang.MetaLang;
import org.hiero.sdk.v3.metalang.TestSpecs;
import org.hiero.sdk.v3.metalang.ValidationReport;
import org.hiero.sdk.v3.metalang.ast.Annotation;
import org.hiero.sdk.v3.metalang.ast.Expression;
import org.hiero.sdk.v3.metalang.ast.Expression.Operator;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.ast.Statement;
import org.hiero.sdk.v3.metalang.ast.TestCase;
import org.hiero.sdk.v3.metalang.ast.TypeRef;
import org.hiero.sdk.v3.metalang.diagnostic.Diagnostic;
import org.hiero.sdk.v3.metalang.source.SchemaSource;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * The parser of {@code tests} blocks (see {@code guidelines/test-language.md}): tests, statements, operators with their
 * precedence, and the syntax errors.
 */
class TestsParserTest {

    private final SchemaParser parser = new SchemaParser();

    private List<TestCase> parse(final String text) {
        final SchemaParser.TestsResult result = parser.parseTests(SchemaSource.ofPlainText("t.md", text));
        assertThat(result.diagnostics()).as("syntax errors").isEmpty();
        return result.tests();
    }

    private List<Diagnostic> errors(final String text) {
        final SchemaParser.TestsResult result = parser.parseTests(SchemaSource.ofPlainText("t.md", text));
        assertThat(result.tests()).isEmpty();
        return result.diagnostics();
    }

    /** The value of {@code let e = <expression>} in a test. */
    private Expression expression(final String expression) {
        final Statement statement = parse("test \"t\" {\n    let e = " + expression + "\n}").getFirst().body()
                .getFirst();
        return ((Statement.Let) statement).value();
    }

    private static String name(final Expression expression) {
        return ((Literal.NameLiteral) ((Expression.Value) expression).literal()).text();
    }

    @Nested
    class Tests {

        @Test
        void testsShouldHaveNameAnnotationsDocumentationAndLocation() {
            final List<TestCase> tests = parse("""
                    // parses an account id
                    @@disabled("not yet")
                    test "fromString reads the number" {
                    }

                    test "second" {
                        let x = 1
                    }
                    """);
            assertThat(tests).hasSize(2);
            final TestCase first = tests.getFirst();
            assertThat(first.name()).isEqualTo("fromString reads the number");
            assertThat(first.annotations()).extracting(Annotation::name).containsExactly("disabled");
            assertThat(first.documentation()).isEqualTo("parses an account id");
            assertThat(first.body()).isEmpty();
            assertThat(first.location().line()).isEqualTo(3);
            assertThat(tests.get(1).body()).hasSize(1);
        }

        @Test
        void anEmptyBlockShouldHaveNoTests() {
            assertThat(parse("// nothing yet\n")).isEmpty();
        }

        @Test
        void escapesInTheNameShouldBeResolved() {
            assertThat(parse("test \"a \\\"quoted\\\" name\" {}").getFirst().name()).isEqualTo("a \"quoted\" name");
        }
    }

    @Nested
    class Statements {

        @Test
        void letShouldBindWithAndWithoutType() {
            final List<Statement> body = parse("""
                    test "t" {
                        let id = AccountId.fromString(value: "0.0.1")
                        let key: PublicKey = DEFAULT(PublicKey);
                    }
                    """).getFirst().body();
            final Statement.Let inferred = (Statement.Let) body.get(0);
            assertThat(inferred.name()).isEqualTo("id");
            assertThat(inferred.type()).isNull();
            assertThat(inferred.value()).isInstanceOf(Expression.Call.class);
            final Statement.Let typed = (Statement.Let) body.get(1);
            assertThat(typed.type()).isEqualTo(new TypeRef.Named("PublicKey", List.of(), typed.type().location()));
            assertThat(typed.value()).isInstanceOf(Expression.Default.class);
        }

        @Test
        void callsShouldBeStatementsAndCarryTheirAnnotations() {
            final List<Statement> body = parse("""
                    test "t" {
                        assertEquals(expected: 1, actual: n)
                        @@expectThrows(illegal-format)
                        AccountId.fromString(value: "account")
                    }
                    """).getFirst().body();
            assertThat(body).hasSize(2);
            final Statement.Evaluate assertion = (Statement.Evaluate) body.get(0);
            assertThat(assertion.annotations()).isEmpty();
            assertThat(((Expression.Call) assertion.expression()).name()).isEqualTo("assertEquals");
            final Statement.Evaluate throwing = (Statement.Evaluate) body.get(1);
            assertThat(throwing.annotations()).singleElement().satisfies(a -> {
                assertThat(a.name()).isEqualTo("expectThrows");
                assertThat(a.arguments()).singleElement()
                        .isEqualTo(new Literal.NameLiteral("illegal-format", a.arguments().getFirst().location()));
            });
            assertThat(((Expression.Call) throwing.expression()).name()).isEqualTo("AccountId.fromString");
            assertThat(throwing.location().line()).isEqualTo(3);
        }

        @Test
        void commentsShouldBeKeptAsStatementsInOrder() {
            final List<Statement> body = parse("""
                    test "t" { // not trailing: behind the brace
                        // given
                        let x = 1 // one
                        /* when
                           and more */
                        assertEquals(expected: 1, actual: x)
                        if x == 1 {
                            // then
                        }
                        // the end
                    }
                    """).getFirst().body();
            assertThat(body).extracting(s -> s instanceof Statement.Comment c ? (c.trailing() ? "~" : "") + c.text()
                    : s.getClass().getSimpleName()).containsExactly("not trailing: behind the brace", "given", "Let",
                    "~one", "when", "and more", "Evaluate", "If", "the end");
            assertThat(((Statement.If) body.get(7)).then()).singleElement()
                    .satisfies(c -> assertThat(((Statement.Comment) c).text()).isEqualTo("then"));
        }

        @Test
        void ifShouldChainElseIfAsNestedIf() {
            final Statement.If statement = (Statement.If) parse("""
                    test "t" {
                        if a {
                            fail(message: "a")
                        } else if b {
                            fail(message: "b")
                        } else {
                            fail(message: "c")
                            fail(message: "d")
                        }
                    }
                    """).getFirst().body().getFirst();
            assertThat(name(statement.condition())).isEqualTo("a");
            assertThat(statement.then()).hasSize(1);
            final Statement.If elseIf = (Statement.If) statement.otherwise().getFirst();
            assertThat(statement.otherwise()).hasSize(1);
            assertThat(name(elseIf.condition())).isEqualTo("b");
            assertThat(elseIf.otherwise()).hasSize(2);
        }

        @Test
        void ifWithoutElseShouldHaveNoOtherwise() {
            final Statement.If statement = (Statement.If) parse("test \"t\" { if a == b { fail(message: \"x\") } }")
                    .getFirst().body().getFirst();
            assertThat(statement.condition()).isInstanceOf(Expression.Binary.class);
            assertThat(statement.otherwise()).isEmpty();
        }

        @Test
        void conditionsEndingInANameShouldNotBeReadAsConstruction() {
            // `KeyEncoding.DER {` could start a struct literal; the block content decides
            final Statement.If statement = (Statement.If) parse("""
                    test "t" {
                        if format.encoding == KeyEncoding.DER {
                            let x = 1
                        }
                        for key in keys {
                            assertTrue(condition: key.valid)
                        }
                    }
                    """).getFirst().body().getFirst();
            final Expression.Binary condition = (Expression.Binary) statement.condition();
            assertThat(name(condition.right())).isEqualTo("KeyEncoding.DER");
        }

        @Test
        void forShouldBindTheElement() {
            final Statement.For loop = (Statement.For) parse("""
                    test "t" {
                        for i in range(start: 0, end: 3) {
                            assertTrue(condition: i >= 0)
                        }
                    }
                    """).getFirst().body().getFirst();
            assertThat(loop.variable()).isEqualTo("i");
            assertThat(((Expression.Call) loop.iterable()).name()).isEqualTo("range");
            assertThat(loop.body()).hasSize(1);
        }

        @Test
        void statementsShouldNestArbitrarily() {
            final Statement.For loop = (Statement.For) parse("""
                    test "t" {
                        for a in as {
                            if a is not null {
                                for b in a.items {
                                    let c = b
                                }
                            }
                        }
                    }
                    """).getFirst().body().getFirst();
            final Statement.If inner = (Statement.If) loop.body().getFirst();
            assertThat(((Statement.For) inner.then().getFirst()).body().getFirst()).isInstanceOf(Statement.Let.class);
        }
    }

    @Nested
    class Operators {

        @Test
        void multiplicationShouldBindTighterThanAddition() {
            final Expression.Binary sum = (Expression.Binary) expression("a + b * c");
            assertThat(sum.operator()).isEqualTo(Operator.ADD);
            assertThat(((Expression.Binary) sum.right()).operator()).isEqualTo(Operator.MULTIPLY);
        }

        @Test
        void operatorsOfOneLevelShouldBeLeftAssociative() {
            final Expression.Binary difference = (Expression.Binary) expression("a - b - c");
            assertThat(difference.operator()).isEqualTo(Operator.SUBTRACT);
            assertThat(name(difference.right())).isEqualTo("c");
            assertThat(((Expression.Binary) difference.left()).operator()).isEqualTo(Operator.SUBTRACT);
        }

        @Test
        void theLevelsShouldFollowTheGuideline() {
            // ||  <  &&  <  ==  <  is  <  <  <  +  <  *  <  unary minus  <  postfix
            final Expression.Binary or = (Expression.Binary) expression("a || b && c == d + e * -f.g");
            assertThat(or.operator()).isEqualTo(Operator.OR);
            final Expression.Binary and = (Expression.Binary) or.right();
            assertThat(and.operator()).isEqualTo(Operator.AND);
            final Expression.Binary equal = (Expression.Binary) and.right();
            assertThat(equal.operator()).isEqualTo(Operator.EQUAL);
            final Expression.Binary add = (Expression.Binary) equal.right();
            assertThat(add.operator()).isEqualTo(Operator.ADD);
            final Expression.Binary multiply = (Expression.Binary) add.right();
            assertThat(multiply.operator()).isEqualTo(Operator.MULTIPLY);
            final Expression.Negate negate = (Expression.Negate) multiply.right();
            assertThat(name(negate.operand())).isEqualTo("f.g");
        }

        @Test
        void comparisonShouldBindTighterThanEquality() {
            final Expression.Binary equal = (Expression.Binary) expression("a < b == c >= d");
            assertThat(equal.operator()).isEqualTo(Operator.EQUAL);
            assertThat(((Expression.Binary) equal.left()).operator()).isEqualTo(Operator.LESS);
            assertThat(((Expression.Binary) equal.right()).operator()).isEqualTo(Operator.GREATER_OR_EQUAL);
        }

        @ParameterizedTest
        @ValueSource(strings = {"*", "/", "%", "+", "-", "<", "<=", ">", ">=", "==", "!=", "&&", "||"})
        void everyBinaryOperatorShouldBeRecognised(final String symbol) {
            final Expression.Binary binary = (Expression.Binary) expression("a " + symbol + " b");
            assertThat(binary.operator().symbol()).isEqualTo(symbol);
            assertThat(binary.location().column()).isEqualTo(15);
        }

        @Test
        void isShouldCheckForNullAndForTypes() {
            final Expression.IsNull isNull = (Expression.IsNull) expression("x is null");
            assertThat(isNull.negated()).isFalse();
            assertThat(((Expression.IsNull) expression("x is not null")).negated()).isTrue();
            final Expression.IsType isType = (Expression.IsType) expression("x is not PublicKey");
            assertThat(isType.negated()).isTrue();
            assertThat(isType.type()).isInstanceOf(TypeRef.Named.class);
            final Expression.Binary and = (Expression.Binary) expression("x is null && y is Key");
            assertThat(and.left()).isInstanceOf(Expression.IsNull.class);
            assertThat(and.right()).isInstanceOf(Expression.IsType.class);
        }

        @Test
        void aNegatedNumberShouldStayALiteral() {
            final Expression.Value value = (Expression.Value) expression("-7");
            assertThat(((Literal.NumberLiteral) value.literal()).text()).isEqualTo("-7");
            final Expression.Binary difference = (Expression.Binary) expression("a -1");
            assertThat(difference.operator()).isEqualTo(Operator.SUBTRACT);
        }

        @Test
        void parenthesesShouldGroupWithoutANodeOfTheirOwn() {
            final Expression.Binary product = (Expression.Binary) expression("(a + b) * c");
            assertThat(product.operator()).isEqualTo(Operator.MULTIPLY);
            assertThat(((Expression.Binary) product.left()).operator()).isEqualTo(Operator.ADD);
        }

        @Test
        void indexesMapsAndListsShouldBeExpressions() {
            final Expression.Index index = (Expression.Index) expression("keys(value: m)[0].bytes[1]");
            assertThat(index.target()).isInstanceOf(Expression.Access.class);
            final Expression.MapValue map = (Expression.MapValue) ((Expression.Binary) expression(
                    "{\"a\": 1, \"b\": 2,} == {}")).left();
            assertThat(map.entries()).hasSize(2);
            assertThat(((Expression.MapValue) ((Expression.Binary) expression("{} == m")).left()).entries()).isEmpty();
            assertThat(((Expression.ListValue) ((Expression.Binary) expression("[1, 2] + [3]")).left()).items())
                    .hasSize(2);
        }

        @Test
        void keywordsShouldStayUsableAsNames() {
            // `a.b` of names is a qualified name; whether it is an attribute of a variable is resolved later
            final Expression.Call not = (Expression.Call) expression("not(value: is.in && test.let)");
            assertThat(not.name()).isEqualTo("not");
            final Expression.Binary and = (Expression.Binary) not.arguments().getFirst().value();
            assertThat(name(and.left())).isEqualTo("is.in");
            assertThat(((Expression.Access) ((Expression.MethodCall) expression("f().in.is()")).target()).attribute())
                    .isEqualTo("in");
        }
    }

    @Nested
    class Errors {

        @ParameterizedTest
        @ValueSource(strings = {
            "test {}",
            "test \"t\"",
            "test \"t\" { let = 1 }",
            "test \"t\" { let x 1 }",
            "test \"t\" { if { } }",
            "test \"t\" { for x { } }",
            "test \"t\" { a + }",
            "test \"t\" { !a }",
            "test \"t\" { x = 1 }",
            "test \"t\" { while a { } }",
            "test \"t\" { a }",
            "test \"t\" { if a { a.b } }",
            "test \"t\" { for x in xs { 1 + 2 } }",
            "test \"t\" { if a { } else { DEFAULT } }",
            "let x = 1",
        })
        void invalidTestsShouldBeSyntaxErrors(final String text) {
            assertThat(errors(text)).isNotEmpty().allMatch(d -> d.ruleId().startsWith("syntax."));
        }

        @Test
        void aSubtractionWithoutSpacesShouldExplainItself() {
            assertThat(errors("test \"t\" {\n    let x = n-1\n}")).first().satisfies(d -> {
                assertThat(d.message()).contains("'n-1' is read as one error id");
                assertThat(d.location().line()).isEqualTo(2);
            });
        }
    }

    @Nested
    class InASpec {

        private static final String SPEC = """
                # Title

                ## Description

                Text.

                ## API Schema

                ```
                namespace a
                X { @@immutable in: int32 }
                ```

                ## Default Instances for Testing

                ```
                instance X = X{in: 1 + 2}
                ```

                ## Testing

                Prose is ignored, and so are code blocks of other languages.

                ```java
                not a test
                ```

                ```tests
                test "first" {
                    assertEquals(expected: 1, actual: DEFAULT(X).in)
                }
                ```

                ```tests
                test "second" {
                }
                ```

                ## Questions & Comments
                """;

        @Test
        void testsOfAllBlocksOfTheTestingSectionShouldBeAttachedToTheSchema() {
            final ValidationReport report = new MetaLang().validate(Map.of("a.md", SPEC));
            assertThat(report.model().files().getFirst().tests()).extracting(TestCase::name)
                    .containsExactly("first", "second");
            assertThat(report.model().files().getFirst().tests().getFirst().location().line()).isEqualTo(29);
        }

        @Test
        void operatorsShouldBeRejectedInDefaultInstances() {
            final ValidationReport report = new MetaLang().validate(Map.of("a.md", SPEC));
            assertThat(report.byRule("instance.invalid")).singleElement()
                    .satisfies(d -> assertThat(d.message()).contains("only allowed in tests"));
        }

        @Test
        void syntaxErrorsInATestsBlockShouldPointIntoTheMarkdownFile() {
            final ValidationReport report = new MetaLang().validate(Map.of("a.md",
                    SPEC.replace("test \"second\" {", "test \"second\" { let")));
            assertThat(report.byRule("syntax.error")).isNotEmpty()
                    .allSatisfy(d -> assertThat(d.location().line()).isBetween(34, 36));
            assertThat(report.model().files().getFirst().tests()).extracting(TestCase::name).containsExactly("first");
        }

        @Test
        void aSpecWithoutTestsShouldHaveNone() {
            final ValidationReport report = new MetaLang().validate(Map.of("a.md",
                    TestSpecs.markdown("namespace a\nX {}")));
            assertThat(report.model().files().getFirst().tests()).isEmpty();
        }
    }
}
