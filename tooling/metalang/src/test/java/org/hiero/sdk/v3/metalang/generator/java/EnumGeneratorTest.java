package org.hiero.sdk.v3.metalang.generator.java;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import org.hiero.sdk.v3.metalang.MetaLang;
import org.hiero.sdk.v3.metalang.TestSpecs;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.generator.GenerationException;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.hiero.sdk.v3.metalang.model.QualifiedName;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EnumGeneratorTest {

    private static final SourceLocation AT = new SourceLocation("x.md", 1, 1);

    private static Type basic(final String name, final Type... arguments) {
        return new Type.BasicType(BuiltinType.lookup(name).orElseThrow(), List.of(arguments));
    }

    private static String enumSource(final String schema) {
        final List<GeneratedFile> files = new JavaGenerator().generate(LinkedModel.of(
                new MetaLang().validate(Map.of("f/a.md", TestSpecs.markdown(schema))).model()));
        return files.stream().filter(f -> f.path().endsWith("/E.java")).findFirst().orElseThrow().content();
    }

    @Nested
    class TypeMapping {

        @ParameterizedTest
        @CsvSource({
                "int8, false, byte", "int8, true, Byte", "uint8, false, byte", "int16, false, short",
                "uint32, true, Integer", "int64, false, long", "int128, false, BigInteger", "uint256, false, BigInteger",
                "double, false, double", "double, true, Double", "bool, false, boolean", "bool, true, Boolean",
                "decimal, false, BigDecimal", "string, false, String", "bytes, false, byte[]", "uuid, false, UUID",
                "date, false, LocalDate", "time, false, LocalTime", "dateTime, false, LocalDateTime",
                "zonedDateTime, false, ZonedDateTime", "seconds, false, Duration", "duration, false, Duration",
                "type, false, Class<?>"
        })
        void shouldMapBasicTypes(final String metaType, final boolean boxed, final String javaType) {
            assertThat(JavaTypes.type(basic(metaType), boxed, new Imports("p"))).isEqualTo(javaType);
        }

        @Test
        void shouldMapGenericAndDeclaredTypesWithImports() {
            // GIVEN
            final Imports imports = new Imports("org.hiero.a");
            final Type account = new Type.DeclaredType(new QualifiedName("ledger", "AccountId"), List.of());

            // THEN
            assertThat(JavaTypes.type(basic("list", basic("int64")), false, imports)).isEqualTo("List<Long>");
            assertThat(JavaTypes.type(basic("map", basic("string"), account), false, imports))
                    .isEqualTo("Map<String, AccountId>");
            assertThat(JavaTypes.type(basic("set", new Type.WildcardType(account)), false, imports))
                    .isEqualTo("Set<? extends AccountId>");
            assertThat(JavaTypes.type(basic("type", account), false, imports)).isEqualTo("Class<? extends AccountId>");
            assertThat(JavaTypes.type(new Type.TypeVariable("$$Receipt", "x"), false, imports)).isEqualTo("Receipt");
            assertThat(JavaTypes.type(new Type.AnyType(), false, imports)).isEqualTo("Object");
            assertThat(JavaTypes.type(new Type.WildcardType(null), false, imports)).isEqualTo("?");
            assertThat(JavaTypes.type(new Type.VoidType(), true, imports)).isEqualTo("Void");
            assertThat(imports.render()).isEqualTo("import java.util.List;\nimport java.util.Map;\nimport java.util.Set;\n"
                    + "import org.hiero.ledger.AccountId;\n");
        }

        @Test
        void shouldPlaceNullnessAnnotationsWithTypeUseSyntax() {
            final Imports imports = new Imports("p");
            assertThat(JavaTypes.declaration(basic("int32"), false, imports)).isEqualTo("int");
            assertThat(JavaTypes.declaration(basic("int32"), true, imports)).isEqualTo("@Nullable Integer");
            assertThat(JavaTypes.declaration(basic("bytes"), false, imports)).isEqualTo("byte @NonNull []");
            assertThat(JavaTypes.declaration(new Type.VoidType(), false, imports)).isEqualTo("void");
            assertThat(JavaTypes.annotate("java.math.BigDecimal", "NonNull")).isEqualTo("java.math.@NonNull BigDecimal");
            assertThat(JavaTypes.annotate("a.B<c.D>", "Nullable")).isEqualTo("a.@Nullable B<c.D>");
        }

        @Test
        void shouldRejectTypesWithoutJavaMappingYet() {
            final Imports imports = new Imports("p");
            assertThatThrownBy(() -> JavaTypes.type(new Type.FunctionType(new Type.VoidType(), "run", List.of()),
                    false, imports)).isInstanceOf(JavaTypes.UnsupportedTypeException.class);
            assertThatThrownBy(() -> JavaTypes.type(new Type.UnresolvedType("X"), false, imports))
                    .isInstanceOf(JavaTypes.UnsupportedTypeException.class);
            assertThatThrownBy(() -> JavaTypes.type(basic("streamResult", basic("int8")), false, imports))
                    .isInstanceOf(JavaTypes.UnsupportedTypeException.class);
        }
    }

    @Nested
    class ImportsAndNames {

        @Test
        void shouldUseQualifiedNamesOnClashesAndSkipJavaLangAndOwnPackage() {
            // GIVEN a file of type "List" in package p
            final Imports imports = new Imports("p", "List");

            // THEN
            assertThat(imports.use("java.util", "List")).isEqualTo("java.util.List");
            assertThat(imports.use("java.lang", "String")).isEqualTo("String");
            assertThat(imports.use("p", "Other")).isEqualTo("Other");
            assertThat(imports.use("a", "Other")).isEqualTo("a.Other");
            assertThat(imports.use("x", "Outer.Inner")).isEqualTo("Outer.Inner");
            assertThat(imports.render()).isEqualTo("import x.Outer;\n");
        }

        @Test
        void shouldEscapeJavaKeywords() {
            assertThat(JavaKeywords.identifier("default")).isEqualTo("default_");
            assertThat(JavaKeywords.identifier("value")).isEqualTo("value");
            assertThat(JavaKeywords.getter("default")).isEqualTo("getDefault");
        }
    }

    @Nested
    class Literals {

        private String literal(final Literal literal, final Type type) {
            return JavaLiterals.expression(literal, type, new Imports("p"));
        }

        @Test
        void shouldConvertNumbersAccordingToTheTargetType() {
            final Literal one = new Literal.NumberLiteral("1_000", AT);
            assertThat(literal(one, basic("int8"))).isEqualTo("(byte) 1_000");
            assertThat(literal(one, basic("int16"))).isEqualTo("(short) 1_000");
            assertThat(literal(one, basic("int32"))).isEqualTo("1_000");
            assertThat(literal(one, basic("int64"))).isEqualTo("1_000L");
            assertThat(literal(one, basic("uint256"))).isEqualTo("new BigInteger(\"1000\")");
            assertThat(literal(one, basic("decimal"))).isEqualTo("new BigDecimal(\"1000\")");
            assertThat(literal(one, basic("double"))).isEqualTo("1_000.0");
            assertThat(literal(new Literal.NumberLiteral("1.5", AT), basic("double"))).isEqualTo("1.5");
            assertThat(literal(one, basic("seconds"))).isEqualTo("Duration.ofSeconds(1_000L)");
            assertThat(literal(one, basic("duration"))).isEqualTo("Duration.ofMillis(1_000L)");
        }

        @Test
        void shouldConvertOtherLiterals() {
            final Type kind = new Type.DeclaredType(new QualifiedName("k", "Kind"), List.of());
            assertThat(literal(new Literal.StringLiteral("a\"b\\c\nd\re\tf", AT), basic("string")))
                    .isEqualTo("\"a\\\"b\\\\c\\nd\\re\\tf\"");
            assertThat(literal(new Literal.NameLiteral("true", AT), basic("bool"))).isEqualTo("true");
            assertThat(literal(new Literal.NameLiteral("null", AT), basic("string"))).isEqualTo("null");
            assertThat(literal(new Literal.NameLiteral("Kind.A", AT), kind)).isEqualTo("Kind.A");
            assertThat(literal(new Literal.ListLiteral(List.of(), AT), basic("bytes"))).isEqualTo("new byte[0]");
            assertThat(literal(new Literal.ListLiteral(List.of(new Literal.NumberLiteral("1", AT)), AT), basic("bytes")))
                    .isEqualTo("new byte[] {(byte) 1}");
            assertThat(literal(new Literal.ListLiteral(List.of(), AT), basic("map", basic("string"), basic("int8"))))
                    .isEqualTo("Map.of()");
            assertThat(literal(new Literal.ListLiteral(List.of(new Literal.NumberLiteral("2", AT)), AT),
                    basic("set", basic("int64")))).isEqualTo("Set.of(2L)");
        }

        @Test
        void shouldRejectLiteralsWithoutJavaFormYet() {
            final Literal struct = new Literal.StructLiteral("P", List.of(), AT);
            assertThatThrownBy(() -> literal(struct, basic("string")))
                    .isInstanceOf(JavaTypes.UnsupportedTypeException.class);
            assertThatThrownBy(() -> literal(struct, new Type.DeclaredType(new QualifiedName("a", "P"), List.of())))
                    .isInstanceOf(JavaTypes.UnsupportedTypeException.class);
        }
    }

    @Nested
    class Enums {

        @Test
        void shouldGenerateEnumWithoutValuesAndBytesAttributeWithDefensiveCopy() {
            assertThat(enumSource("namespace a\nenum E(data: bytes) { A([1, 2]) }"))
                    .contains("A(new byte[] {(byte) 1, (byte) 2});")
                    .contains("public byte @NonNull [] getData() {\n        return data.clone();\n    }");
            assertThat(enumSource("namespace a\nenum E { }")).contains("public enum E {\n\n    ;\n}");
        }

        @Test
        void shouldGenerateGenericAsyncDeprecatedAndVarargsMethods() {
            // WHEN
            final String java = enumSource("""
                    namespace a
                    abstraction Base {}
                    @@deprecated
                    enum E { A
                        @@async @@nullable string load()
                        @@async void fire()
                        @@finalMethod @@deprecated $$T convert<$$T extends Base>(value: $$T, tags: string...)
                    }
                    """);

            // THEN
            assertThat(java).contains("@Deprecated\npublic enum E {")
                    .contains("public @NonNull CompletionStage<String> load() {")
                    .contains("public @NonNull CompletionStage<Void> fire() {")
                    .contains("    @Deprecated\n    public <T extends Base> @NonNull T convert(final @NonNull T value, "
                            + "final @NonNull String... tags) {");
        }

        @Test
        void shouldReportEnumsThatCannotBeGeneratedYet() {
            // GIVEN a method with a function type and a streaming method
            final Map<String, String> documents = Map.of("f/a.md", TestSpecs.markdown("""
                    namespace a
                    enum E { A
                        void each(cb: function<void run()>)
                    }
                    enum S { A
                        @@streaming int8 items()
                    }
                    """));

            // WHEN / THEN
            assertThatThrownBy(() -> new JavaGenerator().generate(LinkedModel.of(new MetaLang().validate(documents)
                    .model()))).isInstanceOfSatisfying(GenerationException.class, e -> assertThat(e.problems())
                    .containsExactly("a.E: Type 'function<void run()>' has no Java mapping yet",
                            "a.S: Type '@@streaming int8' has no Java mapping yet"));
        }
    }
}
