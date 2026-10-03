package org.hiero.sdk.v3.metalang.parser;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.hiero.sdk.v3.metalang.ast.Annotation;
import org.hiero.sdk.v3.metalang.ast.Declaration;
import org.hiero.sdk.v3.metalang.ast.EnumValue;
import org.hiero.sdk.v3.metalang.ast.Field;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.ast.Method;
import org.hiero.sdk.v3.metalang.ast.MethodSyntax;
import org.hiero.sdk.v3.metalang.ast.Parameter;
import org.hiero.sdk.v3.metalang.ast.Requires;
import org.hiero.sdk.v3.metalang.ast.SchemaFile;
import org.hiero.sdk.v3.metalang.ast.TypeRef;
import org.hiero.sdk.v3.metalang.diagnostic.Diagnostic;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;
import org.hiero.sdk.v3.metalang.source.SchemaSource;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SchemaParserTest {

    private final SchemaParser parser = new SchemaParser();

    private SchemaFile parse(final String text) {
        final ParseResult result = parser.parse("test.md", text);
        assertThat(result.diagnostics()).as("syntax diagnostics").isEmpty();
        return result.ast().orElseThrow();
    }

    private <T extends Declaration> T single(final String text, final Class<T> type) {
        final SchemaFile file = parse("namespace test\n" + text);
        assertThat(file.declarations()).hasSize(1);
        return type.cast(file.declarations().getFirst());
    }

    @Nested
    class NamespaceAndRequires {

        @Test
        void shouldParseQualifiedNamespace() {
            final SchemaFile file = parse("namespace consensusnode.transactions.accounts");
            assertThat(file.namespace()).isEqualTo("consensusnode.transactions.accounts");
            assertThat(file.location()).isEqualTo(new SourceLocation("test.md", 1, 1));
            assertThat(file.file()).isEqualTo("test.md");
        }

        @Test
        void shouldParseRequiresStatements() {
            // WHEN
            final SchemaFile file = parse("""
                    namespace a
                    requires {Address, Ledger,} from ledger
                    requires {*} from keys
                    """);

            // THEN
            assertThat(file.requires()).containsExactly(
                    new Requires("ledger", List.of("Address", "Ledger"), false, new SourceLocation("test.md", 2, 1)),
                    new Requires("keys", List.of(), true, new SourceLocation("test.md", 3, 1)));
        }

        @Test
        void shouldAllowKeywordsAsIdentifiers() {
            final SchemaFile file = parse("namespace a\nrequires {type} from from\nX { type: type\n from: string }");
            assertThat(file.requires().getFirst().types()).containsExactly("type");
            assertThat(file.types().getFirst().fields()).extracting(Field::name).containsExactly("type", "from");
        }
    }

    @Nested
    class ComplexTypes {

        @Test
        void shouldParseAbstractionWithGenericsAndSupertypes() {
            // WHEN
            final Declaration.ComplexType type = single("""
                    abstraction NativeToken<$$Self extends NativeToken<$$Self, $$Unit>, $$Unit> extends A, b.B {
                    }
                    """, Declaration.ComplexType.class);

            // THEN
            assertThat(type.abstraction()).isTrue();
            assertThat(type.typeKeyword()).isFalse();
            assertThat(type.typeParameters()).hasSize(2);
            assertThat(type.typeParameters().getFirst().bound().text()).isEqualTo("NativeToken<$$Self, $$Unit>");
            assertThat(type.typeParameters().get(1).boundOptional()).isEmpty();
            assertThat(type.supertypes()).extracting(TypeRef::text).containsExactly("A", "b.B");
        }

        @Test
        void shouldRecordTypeKeyword() {
            final Declaration.ComplexType type = single("type NodeBody { }", Declaration.ComplexType.class);
            assertThat(type.typeKeyword()).isTrue();
            assertThat(type.abstraction()).isFalse();
        }

        @Test
        void shouldParseFieldsWithAnnotations() {
            // WHEN
            final Declaration.ComplexType type = single("""
                    @@oneOf(email, phone)
                    Contact {
                        @@nullable @@immutable email: string;
                        @@pattern("^/[^\\s]*$") @@maxLength(100) phone: string
                    }
                    """, Declaration.ComplexType.class);

            // THEN
            final Annotation oneOf = type.annotation("oneOf").orElseThrow();
            assertThat(oneOf.arguments()).extracting(Literal::text).containsExactly("email", "phone");
            assertThat(type.fields()).extracting(Field::name).containsExactly("email", "phone");
            assertThat(type.fields().getFirst().hasAnnotation("nullable")).isTrue();
            assertThat(oneOf.parenthesized()).isTrue();
            assertThat(type.fields().getFirst().annotation("nullable").orElseThrow().parenthesized()).isFalse();
            final Literal pattern = type.fields().get(1).annotation("pattern").orElseThrow().arguments().getFirst();
            assertThat(pattern).isInstanceOf(Literal.StringLiteral.class);
            assertThat(((Literal.StringLiteral) pattern).value()).isEqualTo("^/[^\\s]*$");
        }

        @Test
        void shouldParseAllMethodForms() {
            // WHEN
            final Declaration.ComplexType type = single("""
                    Copyable {
                        @@async @@throws(not-found-error, parse-error) @@nullable Details fetch(apiKey: string)
                        copy(): Copyable
                        reset()
                        void close();
                    }
                    """, Declaration.ComplexType.class);

            // THEN
            assertThat(type.methods()).extracting(Method::syntax).containsExactly(
                    MethodSyntax.CLASSIC, MethodSyntax.TRAILING_RETURN, MethodSyntax.MISSING_RETURN,
                    MethodSyntax.CLASSIC);
            assertThat(type.methods()).extracting(m -> m.returnType().text())
                    .containsExactly("Details", "Copyable", "void", "void");
            assertThat(type.methods().getFirst().annotation("throws").orElseThrow().arguments())
                    .extracting(Literal::text).containsExactly("not-found-error", "parse-error");
            assertThat(type.methods().getFirst().signature()).isEqualTo("fetch(string)");
        }

        @Test
        void shouldParseMethodTypeParametersInAllMethodForms() {
            // WHEN
            final Declaration.ComplexType type = single("""
                    T {
                        @@finalMethod $$U a<$$U extends Base, $$V>(x: $$U, y: list<$$V>)
                        b<$$X>(): $$X
                        c<$$Y>(y: $$Y)
                        int8 d()
                    }
                    """, Declaration.ComplexType.class);

            // THEN
            assertThat(type.methods()).extracting(m -> m.typeParameters().size()).containsExactly(2, 1, 1, 0);
            assertThat(type.methods().getFirst().typeParameters().getFirst().bound().text()).isEqualTo("Base");
            assertThat(type.methods()).extracting(Method::isGeneric).containsExactly(true, true, true, false);
        }

        @Test
        void shouldParseGenericFunctions() {
            final SchemaFile file = parse("namespace a\n@@static Response<$$R> load<$$R extends R>(t: type<T<$$R>>)");
            final Method method = ((Declaration.Function) file.declarations().getFirst()).method();
            assertThat(method.typeParameters()).hasSize(1);
            assertThat(method.parameters().getFirst().type().text()).isEqualTo("type<T<$$R>>");
        }

        @Test
        void shouldParseParametersIncludingVarargsAndFunctionTypes() {
            // WHEN
            final Declaration.ComplexType type = single("""
                    T {
                        void m(@@nullable a: list<Key>, cb: function<void onEvent(event: Event)>, keys: Key...)
                    }
                    """, Declaration.ComplexType.class);

            // THEN
            final List<Parameter> parameters = type.methods().getFirst().parameters();
            assertThat(parameters).extracting(Parameter::text).containsExactly(
                    "a: list<Key>", "cb: function<void onEvent(event: Event)>", "keys: Key...");
            assertThat(parameters.get(2).varargs()).isTrue();
            assertThat(parameters.getFirst().hasAnnotation("nullable")).isTrue();
            assertThat(type.methods().getFirst().signature()).isEqualTo("m(list,function,Key)");
        }
    }

    @Nested
    class TypeReferences {

        private TypeRef fieldType(final String type) {
            return single("T { f: " + type + " }", Declaration.ComplexType.class).fields().getFirst().type();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "string", "int64", "list<map<string, list<int8>>>", "$$T", "ANY", "ns.sub.Type",
                "ContractParam<ANY>", "ContractParam<ANY extends Number>", "Response<$$R extends Receipt>",
                "function<void run()>", "function<R apply(a: A, b: B)>"
        })
        void shouldRoundTripTypeText(final String type) {
            assertThat(fieldType(type).text()).isEqualTo(type);
        }

        @Test
        void shouldBuildTypedNodes() {
            assertThat(fieldType("$$T")).isInstanceOf(TypeRef.GenericParameter.class);
            assertThat(fieldType("ANY")).isInstanceOf(TypeRef.Any.class);
            final TypeRef.Named named = (TypeRef.Named) fieldType("a.b.C<ANY, $$T extends D, E>");
            assertThat(named.isQualified()).isTrue();
            assertThat(named.simpleName()).isEqualTo("C");
            assertThat(named.arguments()).hasExactlyElementsOfTypes(
                    TypeRef.Wildcard.class, TypeRef.BoundedGeneric.class, TypeRef.Concrete.class);
            assertThat(named.arguments().getFirst().location().line()).isEqualTo(2);
        }

        @Test
        void shouldParseVoidReturnType() {
            final Method method = single("T { void m() }", Declaration.ComplexType.class).methods().getFirst();
            assertThat(method.returnType()).isInstanceOf(TypeRef.Void.class);
        }
    }

    @Nested
    class Enums {

        @Test
        void shouldParseValuesFieldsAndMethods() {
            // WHEN
            final Declaration.EnumType enumType = single("""
                    enum KeyEncoding extends Base {
                        DER, // Distinguished Encoding Rules
                        @@deprecated PEM
                        // not complete yet
                        @@immutable rawFormat: RawFormat
                        bytes decode(keyType: KeyType, value: string)
                        bool supportsType(type: KeyType)
                    }
                    """, Declaration.EnumType.class);

            // THEN
            assertThat(enumType.values()).extracting(EnumValue::name).containsExactly("DER", "PEM");
            assertThat(enumType.values().getFirst().documentation()).isEqualTo("Distinguished Encoding Rules");
            assertThat(enumType.values().get(1).hasAnnotation("deprecated")).isTrue();
            assertThat(enumType.fields()).extracting(Field::name).containsExactly("rawFormat");
            assertThat(enumType.methods()).extracting(Method::name).containsExactly("decode", "supportsType");
            assertThat(enumType.supertypes()).extracting(TypeRef::text).containsExactly("Base");
            assertThat(enumType.typeParameters()).isEmpty();
        }
    }

    @Test
    void shouldParseEnumAttributeListAndArguments() {
        // WHEN
        final Declaration.EnumType enumType = single("""
                enum HbarUnit(symbol: string, @@min(1) factor: int64) extends NativeTokenUnit {
                    TINYBAR("tℏ", 1),
                    HBAR("ℏ", 100_000_000) // main unit
                    int8 code()
                }
                """, Declaration.EnumType.class);

        // THEN
        assertThat(enumType.attributes()).extracting(Parameter::text)
                .containsExactly("symbol: string", "factor: int64");
        assertThat(enumType.attributes().get(1).hasAnnotation("min")).isTrue();
        assertThat(enumType.values()).extracting(v -> v.arguments().stream().map(Literal::text).toList())
                .containsExactly(List.of("\"tℏ\"", "1"), List.of("\"ℏ\"", "100_000_000"));
        assertThat(enumType.methods()).extracting(Method::name).containsExactly("code");
        assertThat(enumType.values().get(1).documentation()).isEqualTo("main unit");
    }

    @Nested
    class ConstantsAndFunctions {

        @Test
        void shouldParseAllLiteralKinds() {
            // WHEN
            final SchemaFile file = parse("""
                    namespace a
                    constant A: string = "x\\"y\\\\z"
                    constant B: int64 = -100_000
                    constant C: double = 1.5
                    constant D: list<int8> = [1, 2]
                    constant E: Address = Address{shard: 0, realm: 0, alias: null, nested: N{}}
                    constant F: Unit = Unit.HBAR;
                    """);

            // THEN
            final List<Literal> values = file.declarations().stream()
                    .map(d -> ((Declaration.Constant) d).value())
                    .toList();
            assertThat(values).hasExactlyElementsOfTypes(Literal.StringLiteral.class, Literal.NumberLiteral.class,
                    Literal.NumberLiteral.class, Literal.ListLiteral.class, Literal.StructLiteral.class,
                    Literal.NameLiteral.class);
            assertThat(((Literal.StringLiteral) values.getFirst()).value()).isEqualTo("x\"y\\z");
            assertThat(values.getFirst().text()).isEqualTo("\"x\\\"y\\\\z\"");
            assertThat(((Literal.NumberLiteral) values.get(1)).value()).isEqualByComparingTo("-100000");
            assertThat(values.get(3).text()).isEqualTo("[1, 2]");
            assertThat(values.get(4).text()).isEqualTo("Address{shard: 0, realm: 0, alias: null, nested: N{}}");
            assertThat(values.get(5).text()).isEqualTo("Unit.HBAR");
        }

        @Test
        void shouldParseNamespaceFunctions() {
            // WHEN
            final SchemaFile file = parse("""
                    namespace a
                    @@throws(illegal-format) @@static Authority of(children: Authority...)
                    """);

            // THEN
            final Declaration.Function function = (Declaration.Function) file.declarations().getFirst();
            assertThat(function.name()).isEqualTo("of");
            assertThat(function.hasAnnotation("throws")).isTrue();
            assertThat(function.location()).isEqualTo(function.method().location());
        }
    }

    @Nested
    class Comments {

        @Test
        void shouldAttachLeadingAndTrailingCommentsAsDocumentation() {
            // WHEN
            final Declaration.ComplexType type = single("""
                    // first line
                    /*
                     * second line
                     */
                    @@finalType
                    Foo { // trailing type comment
                        @@immutable a: int32 // doc of a
                        // doc of b
                        @@immutable b: int32
                    }
                    """, Declaration.ComplexType.class);

            // THEN
            assertThat(type.documentation()).isEqualTo("first line\nsecond line");
            assertThat(type.fields()).extracting(Field::documentation).containsExactly("doc of a", "doc of b");
        }

        @Test
        void shouldOnlyAttachTheCommentBlockDirectlyAboveTheDeclaration() {
            // WHEN a section comment is separated from the member comment by a blank line
            final Declaration.ComplexType type = single("""
                    // section comment for several members

                    // doc of m
                    X {
                        // section inside

                        /* doc of n, line 1
                           line 2 */
                        void n()
                    }
                    """, Declaration.ComplexType.class);

            // THEN
            assertThat(type.documentation()).isEqualTo("doc of m");
            assertThat(type.methods().getFirst().documentation()).isEqualTo("doc of n, line 1\nline 2");
        }

        @Test
        void shouldCollectAllComments() {
            final SchemaFile file = parse("namespace a // ns\n// standalone\n");
            assertThat(file.comments()).extracting(c -> c.text()).containsExactly("ns", "standalone");
        }
    }

    @Nested
    class Errors {

        @Test
        void shouldReportSyntaxErrorWithMarkdownLocationAndProduceNoAst() {
            // WHEN
            final ParseResult result = parser.parse(new SchemaSource("spec.md", "namespace a\nFoo {\n  x: \n}\n", 10));

            // THEN
            assertThat(result.ast()).isEmpty();
            assertThat(result.diagnostics()).isNotEmpty();
            final Diagnostic first = result.diagnostics().getFirst();
            assertThat(first.ruleId()).isEqualTo("syntax.error");
            assertThat(first.location().line()).isEqualTo(14);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "Foo {}",                       // missing namespace
                "namespace a\nFoo { x: int32",  // missing brace
                "namespace a\nFoo { # }",       // illegal character
                "namespace a\nenum E { bool m( }",
                "namespace a\nenum E { A\n... }",                              // placeholder is not valid syntax
                "namespace a\nenum E(a: int8) { A() }",                        // empty argument list
                "namespace a\nE {}\n@@static E E.fromString(value: string)"   // attached from outside
        })
        void shouldRejectInvalidInput(final String text) {
            assertThat(parser.parse("x.md", text).ast()).isEmpty();
        }
    }

    @Test
    void shouldUnquoteOnlyQuotesAndBackslashes() {
        assertThat(AstBuilder.unquote("\"a\\\"b\\\\c\\sd\\\"")).isEqualTo("a\"b\\c\\sd\\");
    }
}
