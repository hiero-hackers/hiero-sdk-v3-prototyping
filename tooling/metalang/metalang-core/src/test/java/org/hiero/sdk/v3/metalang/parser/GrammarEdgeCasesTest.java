package org.hiero.sdk.v3.metalang.parser;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.hiero.sdk.v3.metalang.ast.Declaration;
import org.hiero.sdk.v3.metalang.ast.SchemaFile;
import org.hiero.sdk.v3.metalang.diagnostic.Diagnostic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Systematic grammar boundary tests: every construct the guideline allows must parse, every construct it does not
 * allow must be rejected with a syntax diagnostic (and never with an exception).
 */
class GrammarEdgeCasesTest {

    private final SchemaParser parser = new SchemaParser();

    static Stream<Arguments> validSchemas() {
        return Stream.of(
                Arguments.of("namespace only", "namespace a"),
                Arguments.of("nested generics closing with '>>>'", "namespace a\nX { f: map<string, list<list<int8>>> }"),
                Arguments.of("keywords as field names", "namespace a\nX { type: string\n from: string\n namespace: string\n"
                        + " requires: string\n constant: string\n enum: string\n abstraction: string\n extends: string\n"
                        + " function: string }"),
                Arguments.of("keyword as namespace segment", "namespace a.type.from"),
                Arguments.of("comment between annotation and declaration", "namespace a\n@@finalType // c\n/* d */ X {}"),
                Arguments.of("comment inside type arguments", "namespace a\nX { f: list</* c */ int8> }"),
                Arguments.of("multi-line block comment", "namespace a\n/*\n * a\n * b\n */\nX {}"),
                Arguments.of("comment at end of file without newline", "namespace a\nX {} // end"),
                Arguments.of("trailing comma in requires", "namespace a\nrequires {B, C,} from b"),
                Arguments.of("trailing comma in parameters", "namespace a\nX { void m(a: int8, b: int8,) }"),
                Arguments.of("trailing comma in struct literal", "namespace a\nconstant C: P = P{x: 1, y: 2,}"),
                Arguments.of("enum values separated by commas and semicolons", "namespace a\nenum E { A, B; C }"),
                Arguments.of("semicolons after members", "namespace a\nX { a: int8; void m(); }\nconstant C: int8 = 1;"),
                Arguments.of("empty bodies", "namespace a\nX {}\nabstraction A {}\nenum E { V }"),
                Arguments.of("unicode in strings", "namespace a\nconstant S: string = \"tℏ μℏ €\""),
                Arguments.of("escaped quote and backslash", "namespace a\nconstant S: string = \"a\\\"b\\\\c\""),
                Arguments.of("regex with backslashes", "namespace a\nX { @@pattern(\"^\\d+\\s*$\") a: string }"),
                Arguments.of("number formats", "namespace a\nconstant A: int64 = 100_000\nconstant B: int8 = -1\n"
                        + "constant C: double = 1.5\nconstant D: int8 = 0"),
                Arguments.of("annotation arguments of every kind", "namespace a\nX { @@x(not-found-error, name, Kind.A,"
                        + " \"s\", 1, [1, 2], P{a: 1}) void m() }"),
                Arguments.of("varargs of a function type", "namespace a\nX { void m(cb: function<void f()>...) }"),
                Arguments.of("nested function types", "namespace a\nX { void m(cb: function<function<int8 g()> f("
                        + "x: function<void h()>)>) }"),
                Arguments.of("generic method in every method form", "namespace a\nX { $$T a<$$T>()\n b<$$U>(): $$U\n"
                        + " c<$$V extends X>(v: $$V) }"),
                Arguments.of("generic namespace function", "namespace a\n@@static $$T f<$$T, $$U extends list<$$T>>()"),
                Arguments.of("enum with attributes, methods and generic method", "namespace a\n"
                        + "enum E(code: int32, @@nullable note: string) extends S { A(1, null)\n B(2, \"x\"),\n"
                        + " int8 m()\n @@finalMethod $$T g<$$T>(v: $$T) }"),
                Arguments.of("enum value arguments of every literal kind", "namespace a\n"
                        + "enum E(a: string, b: int8, c: list<int8>, d: P, e: Kind) { V(\"s\", -1, [1], P{x: 1}, Kind.A) }"),
                Arguments.of("tabs and CRLF line endings", "namespace a\r\n\tX {\r\n\t\ta: int8\r\n\t}\r\n"),
                Arguments.of("multiple supertypes over several lines", "namespace a\nX extends\n  A,\n  B<int8> {}"),
                Arguments.of("type keyword variant", "namespace a\ntype X { }"),
                Arguments.of("wildcards with bounds", "namespace a\nX { f: G<ANY, ANY extends Y> }"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("validSchemas")
    void shouldParseValidSchema(final String description, final String text) {
        // WHEN
        final ParseResult result = parser.parse("edge.md", text);

        // THEN
        assertThat(result.diagnostics()).as(description).isEmpty();
        assertThat(result.ast()).isPresent();
    }

    static Stream<Arguments> invalidSchemas() {
        return Stream.of(
                Arguments.of("empty text", ""),
                Arguments.of("only comments", "// nothing here\n/* still nothing */"),
                Arguments.of("missing namespace", "X {}"),
                Arguments.of("two namespace declarations", "namespace a\nnamespace b"),
                Arguments.of("empty requires list", "namespace a\nrequires {} from b"),
                Arguments.of("requires after a declaration", "namespace a\nX {}\nrequires {B} from b"),
                Arguments.of("dangling annotation at end of file", "namespace a\nX {}\n@@finalType"),
                Arguments.of("ANY as field name", "namespace a\nX { ANY: int8 }"),
                Arguments.of("void as field name", "namespace a\nX { void: int8 }"),
                Arguments.of("exponent number", "namespace a\nconstant C: double = 1e10"),
                Arguments.of("hex number", "namespace a\nconstant C: int8 = 0x1F"),
                Arguments.of("number starting with a dot", "namespace a\nconstant C: double = .5"),
                Arguments.of("non-ASCII identifier", "namespace a\nGröße {}"),
                Arguments.of("unterminated string", "namespace a\nconstant S: string = \"abc"),
                Arguments.of("line break inside a string", "namespace a\nconstant S: string = \"a\nb\""),
                Arguments.of("generic enum", "namespace a\nenum E<$$T> { A }"),
                Arguments.of("enum value with empty argument list", "namespace a\nenum E(a: int8) { A() }"),
                Arguments.of("placeholder in enum", "namespace a\nenum E { A\n... }"),
                Arguments.of("function attached from outside", "namespace a\nX {}\n@@static X X.of()"),
                Arguments.of("method without parentheses", "namespace a\nX { void m }"),
                Arguments.of("field without colon", "namespace a\nX { a int8 }"),
                Arguments.of("bare annotation prefix", "namespace a\nX { @@ a: int8 }"),
                Arguments.of("bare generic prefix", "namespace a\nX { a: $$ }"),
                Arguments.of("nested type declaration", "namespace a\nX { Y {} }"),
                Arguments.of("unclosed annotation arguments", "namespace a\nX { @@min(1 a: int8 }"),
                Arguments.of("unclosed struct literal", "namespace a\nconstant C: P = P{x: 1"),
                Arguments.of("unclosed block comment", "namespace a\nX {}\n/* never closed"),
                Arguments.of("illegal character", "namespace a\nX { a: int8 # }"),
                Arguments.of("type parameter without prefix", "namespace a\nX<T> {}"),
                Arguments.of("constant without value", "namespace a\nconstant C: int8"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidSchemas")
    void shouldRejectInvalidSchemaWithDiagnosticInsteadOfException(final String description, final String text) {
        // WHEN
        final ParseResult result = parser.parse("edge.md", text);

        // THEN
        assertThat(result.ast()).as(description).isEmpty();
        assertThat(result.diagnostics()).as(description).isNotEmpty()
                .allSatisfy(d -> assertThat(d.ruleId()).isEqualTo("syntax.error"));
        final int lineCount = (int) text.lines().count();
        assertThat(result.diagnostics()).extracting(d -> d.location().line())
                .allSatisfy(line -> assertThat(line).isBetween(1, Math.max(1, lineCount)));
    }

    @Test
    void shouldNotTreatHyphenatedNamesAsSubtractionOrSplitThem() {
        final SchemaFile file = parser.parse("a.md", "namespace a\nX { @@throws(a-b-1, c) void m() }").ast().orElseThrow();
        final Declaration.ComplexType type = (Declaration.ComplexType) file.declarations().getFirst();
        assertThat(type.methods().getFirst().annotation("throws").orElseThrow().arguments())
                .extracting(l -> l.text()).containsExactly("a-b-1", "c");
    }

    private static String nestedLists(final int depth) {
        return "list<".repeat(depth) + "int8" + ">".repeat(depth);
    }

    @Test
    void shouldAcceptNestingUpToTheLimitAndRejectDeeperNestingWithoutStackOverflow() {
        // the type body brace counts as one level, so the field type may use the remaining ones
        final int maxListDepth = SyntaxParser.MAX_NESTING - 1;
        assertThat(parser.parse("a.md", "namespace a\nX { f: " + nestedLists(maxListDepth) + " }").diagnostics())
                .isEmpty();

        final ParseResult tooDeep = parser.parse("a.md", "namespace a\nX { f: " + nestedLists(maxListDepth + 1) + " }");
        assertThat(tooDeep.ast()).isEmpty();
        assertThat(tooDeep.diagnostics()).extracting(Diagnostic::ruleId).containsExactly("syntax.nesting-too-deep");

        // far beyond the limit: still a diagnostic, never a StackOverflowError
        assertThat(parser.parse("a.md", "namespace a\nX { f: " + nestedLists(20_000) + " }").diagnostics())
                .extracting(Diagnostic::ruleId).containsExactly("syntax.nesting-too-deep");
    }

    @Test
    void shouldReportErrorsAtEndOfInputBehindTheLastCharacter() {
        // WHEN the input ends in the middle of a declaration
        final Diagnostic diagnostic = parser.parse("a.md", "namespace a\nX {\n").diagnostics().getFirst();

        // THEN the error points behind '{' in line 2, not at a (non-existing) line 3
        assertThat(diagnostic.location().line()).isEqualTo(2);
        assertThat(diagnostic.location().column()).isEqualTo(4);
    }

    @Test
    void shouldReportErrorForEmptySchemaInsideMarkdownAtTheOpeningFence() {
        // WHEN the schema block is unclosed and empty (opening fence in line 3 of the Markdown file)
        final Diagnostic diagnostic = parser.parse(new org.hiero.sdk.v3.metalang.source.SchemaSource("a.md", "", 3))
                .diagnostics().getFirst();

        // THEN
        assertThat(diagnostic.location().line()).isEqualTo(3);
    }

    @Test
    void shouldReportPositionOfFirstSyntaxErrorExactly() {
        // WHEN
        final Diagnostic first = parser.parse("a.md", "namespace a\nX {\n  a: int8\n  b int8\n}").diagnostics().getFirst();

        // THEN 'b int8' is a valid prefix of a method declaration 'b int8(...)', so the parser can only fail at the
        // closing brace in line 5, column 1, where it expects '(' (known limitation: errors can surface one token late)
        assertThat(first.location().line()).isEqualTo(5);
        assertThat(first.location().column()).isEqualTo(1);
        assertThat(first.message()).contains("'('");
    }
}
