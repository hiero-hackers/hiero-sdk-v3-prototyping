package org.hiero.sdk.v3.metalang.generator.java;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hiero.sdk.v3.metalang.MetaLang;
import org.hiero.sdk.v3.metalang.TestSpecs;
import org.hiero.sdk.v3.metalang.ast.Annotation;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.model.FieldDefinition;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.hiero.sdk.v3.metalang.model.QualifiedName;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;

class RecordGeneratorTest {

    private static final SourceLocation AT = new SourceLocation("x.md", 1, 1);

    private static LinkedModel model(final String schema) {
        return LinkedModel.of(new MetaLang().validate(Map.of("f/a.md", TestSpecs.markdown(schema))).model());
    }

    private static List<String> generatedTypes(final String schema) {
        return new JavaGenerator().generate(model(schema)).stream().map(GeneratedFile::path)
                .filter(p -> !p.endsWith("module-info.java") && !p.endsWith("package-info.java"))
                .map(p -> p.substring(p.lastIndexOf('/') + 1, p.length() - ".java".length()))
                .toList();
    }

    private static String source(final String schema, final String type) {
        return new JavaGenerator().generate(model(schema)).stream().filter(f -> f.path().endsWith("/" + type + ".java"))
                .findFirst().orElseThrow().content();
    }

    @Nested
    class WhichTypesBecomeRecords {

        @Test
        void shouldOnlyGenerateRecordsForTypesWithImmutableAttributes() {
            assertThat(generatedTypes("""
                    namespace a
                    Immutable { @@immutable a: int32 }
                    Mutable { a: int32 }
                    PartlyMutable { @@immutable a: int32
                        b: int32 }
                    NoAttributes { void run() }
                    Empty { }
                    abstraction Abstract { @@immutable a: int32 }
                    """)).containsExactly("Immutable");
        }

        @Test
        void shouldUseInheritedAttributesAndRequireThemToBeImmutable() {
            // WHEN
            final String schema = """
                    namespace a
                    abstraction Named { @@immutable name: string }
                    abstraction Counted { count: int32 }
                    Person extends Named { @@immutable age: int32 }
                    Thing extends Counted { @@immutable label: string }
                    Tag extends Named { }
                    """;

            // THEN
            assertThat(generatedTypes(schema)).containsExactlyInAnyOrder("Person", "Tag");
            assertThat(source(schema, "Person")).contains("public record Person(@NonNull String name, int age)"
                    + " /* implements org.hiero.a.Named (enabled as soon as abstractions are generated) */ {");
            assertThat(new JavaGenerator().deferredRecords(model(schema))).isEmpty();
        }

        @Test
        void shouldNotUseRecordsForTypesThatExtendOrAreExtendedByComplexTypes() {
            assertThat(generatedTypes("""
                    namespace a
                    Base { @@immutable a: int32 }
                    Derived extends Base { @@immutable b: int32 }
                    Other { @@immutable base: int32 }
                    """)).containsExactly("Other");
        }

        @Test
        void shouldNotUseRecordsForTypesThatInheritAFinalMethod() {
            // an abstraction with a @@finalMethod becomes an abstract class, which a record cannot extend
            assertThat(generatedTypes("""
                    namespace a
                    abstraction Entity { @@immutable id: int64
                        @@finalMethod bool same(other: Entity) }
                    Product extends Entity { @@immutable name: string }
                    Own { @@immutable id: int64
                        @@finalMethod bool same(other: Own) }
                    """)).containsExactly("Own");
        }

        @Test
        void shouldNotTreatTypeArgumentsOfSupertypesAsExtended() {
            assertThat(generatedTypes("""
                    namespace a
                    abstraction Box<$$T> { }
                    Content { @@immutable a: int32 }
                    ContentBox extends Box<Content> { @@immutable content: Content }
                    """)).containsExactlyInAnyOrder("Content", "ContentBox");
        }
    }

    @Nested
    class DeferredRecords {

        @Test
        void shouldDeferRecordsThatReferToTypesThatAreNotGeneratedYetTransitively() {
            // GIVEN
            final LinkedModel model = model("""
                    namespace a
                    abstraction Shape { }
                    Mutable { a: int32 }
                    Uses { @@immutable shape: Shape }
                    UsesUses { @@immutable uses: Uses }
                    InMethod { @@immutable a: int32
                        Mutable convert() }
                    InParameter { @@immutable a: int32
                        void take(m: Mutable) }
                    InBound<$$T extends Shape> { @@immutable a: $$T }
                    InMethodBound { @@immutable a: int32
                        @@static $$T pick<$$T extends Shape>() }
                    InTypeArgument { @@immutable shapes: list<Shape> }
                    InWildcard { @@immutable holder: Holder<ANY extends Shape> }
                    Holder<$$T> { @@immutable value: $$T }
                    SelfReference { @@immutable @@nullable next: SelfReference }
                    UsesEnum { @@immutable color: Color }
                    enum Color { RED }
                    """);

            // WHEN
            final Map<QualifiedName, String> deferred = new JavaGenerator().deferredRecords(model);
            final List<String> generated = new JavaGenerator().generate(model).stream().map(GeneratedFile::path)
                    .filter(p -> p.endsWith(".java")).map(p -> p.substring(p.lastIndexOf('/') + 1)).toList();

            // THEN
            assertThat(deferred).containsOnlyKeys(new QualifiedName("a", "Uses"), new QualifiedName("a", "UsesUses"),
                    new QualifiedName("a", "InMethod"), new QualifiedName("a", "InParameter"),
                    new QualifiedName("a", "InBound"), new QualifiedName("a", "InMethodBound"),
                    new QualifiedName("a", "InTypeArgument"), new QualifiedName("a", "InWildcard"));
            assertThat(deferred.get(new QualifiedName("a", "Uses")))
                    .isEqualTo("refers to a.Shape (abstraction, not generated yet)");
            assertThat(deferred.get(new QualifiedName("a", "UsesUses")))
                    .isEqualTo("refers to a.Uses (record, not generated yet)");
            assertThat(deferred.get(new QualifiedName("a", "InMethod")))
                    .isEqualTo("refers to a.Mutable (class, not generated yet)");
            assertThat(generated).contains("Holder.java", "SelfReference.java", "UsesEnum.java", "Color.java");
        }

        @Test
        void shouldDeferRecordsWithTypesThatHaveNoJavaMappingYet() {
            // GIVEN
            final LinkedModel model = model("""
                    namespace a
                    Callback { @@immutable run: function<void run()> }
                    Stream { @@immutable a: int32
                        @@streaming int8 items() }
                    UsesCallback { @@immutable callback: Callback }
                    """);

            // WHEN / THEN
            assertThat(new JavaGenerator().deferredRecords(model)).containsExactly(
                    Map.entry(new QualifiedName("a", "Callback"), "Type 'function<void run()>' has no Java mapping yet"),
                    Map.entry(new QualifiedName("a", "Stream"), "Type '@@streaming int8' has no Java mapping yet"),
                    Map.entry(new QualifiedName("a", "UsesCallback"),
                            "refers to a.Callback (record, not generated yet)"));
            assertThat(new JavaGenerator().generate(model)).noneMatch(f -> f.content().contains("public record"));
        }
    }

    @Nested
    class Source {

        @Test
        void shouldCopyNullableCollectionsOnlyIfPresent() {
            assertThat(source("""
                    namespace a
                    Bag { @@immutable @@nullable items: list<string>
                        @@immutable @@nullable tags: set<string>
                        @@immutable @@nullable labels: map<string, int32>
                        @@immutable @@nullable name: string }
                    """, "Bag"))
                    .contains("        items = items == null ? null : List.copyOf(items);\n")
                    .contains("        tags = tags == null ? null : Set.copyOf(tags);\n")
                    .contains("        labels = labels == null ? null : Map.copyOf(labels);\n")
                    .doesNotContain("requireNonNull")
                    .doesNotContain("@throws");
        }

        @Test
        void shouldNotGenerateAConstructorWithoutChecks() {
            assertThat(source("namespace a\nPoint { @@immutable x: int32\n @@immutable @@nullable label: string }",
                    "Point")).isEqualTo("""
                    // Generated by metalang from the Hiero SDK V3 specs. Do not edit.

                    package org.hiero.a;

                    import org.jspecify.annotations.Nullable;

                    public record Point(int x, @Nullable String label) {
                    }
                    """);
        }

        @Test
        void shouldEscapeKeywordsAndDocumentComponentsAndDeprecation() {
            // WHEN
            final String java = source("""
                    namespace a
                    // A thing.
                    @@deprecated
                    Thing {
                        @@immutable default: string   // the default
                        // @@immutable at line start
                        @@immutable @@nullable @@deprecated old: bytes
                    }
                    """, "Thing");

            // THEN
            assertThat(java).contains("/// A thing.\n///\n/// @param default_ the default\n/// @param old @@immutable at line start\n")
                    .contains("@Deprecated\npublic record Thing(@NonNull String default_, byte @Nullable [] old) {")
                    .contains("        old = old == null ? null : old.clone();\n")
                    .contains("    @Override\n    @Deprecated\n    public byte @Nullable [] old() {\n"
                            + "        return old == null ? null : old.clone();\n    }")
                    .contains("            && Objects.equals(default_, other.default_)\n"
                            + "            && Arrays.equals(old, other.old);");
        }

        @Test
        void shouldCompareGenericRecordsWithBytesAndPrimitiveComponents() {
            assertThat(source("""
                    namespace a
                    Sample<$$T> { @@immutable data: bytes
                        @@immutable ratio: double
                        @@immutable flag: bool
                        @@immutable value: $$T
                        bool equals(other: ANY)
                        int32 hashCode()
                        @@static string toString() }
                    """, "Sample"))
                    .contains("public record Sample<T>(byte @NonNull [] data, double ratio, boolean flag, @NonNull T value)")
                    .contains("    @Override\n    public boolean equals(final @Nullable Object other) {\n"
                            + "        throw new UnsupportedOperationException")
                    .contains("    @Override\n    public int hashCode() {\n        throw")
                    .contains("    public static @NonNull String toString() {")
                    .doesNotContain("instanceof Sample<?>")
                    .doesNotContain("Arrays.hashCode");
            assertThat(source("""
                    namespace a
                    Sample<$$T> { @@immutable data: bytes
                        @@immutable ratio: double
                        @@immutable flag: bool }
                    """, "Sample"))
                    .contains("return obj instanceof Sample<?> other\n"
                            + "            && Arrays.equals(data, other.data)\n"
                            + "            && Double.compare(ratio, other.ratio) == 0\n"
                            + "            && flag == other.flag;")
                    .contains("return \"Sample[data=\" + \"byte[\" + data.length + \"]\" + \", ratio=\" + ratio + \", flag=\" + flag + \"]\";");
        }

        @Test
        void shouldGenerateComparisonsForAllNumericTypes() {
            assertThat(source("""
                    namespace a
                    Limits { @@immutable @@min(1) small: int8
                        @@immutable @@max(5) big: uint64
                        @@immutable @@min(0) huge: int256
                        @@immutable @@max(1.5) ratio: double
                        @@immutable @@min(0) amount: decimal
                        @@immutable @@max(30) wait: duration
                        @@immutable @@nullable @@maxLength(3) code: string
                        @@immutable @@maxSize(4) data: bytes }
                    """, "Limits"))
                    .contains("if (small < (byte) 1) {")
                    .contains("if (big > 5L) {")
                    .contains("if (huge.compareTo(new BigInteger(\"0\")) < 0) {")
                    .contains("if (ratio > 1.5) {")
                    .contains("if (amount.compareTo(new BigDecimal(\"0\")) < 0) {")
                    .contains("if (wait.compareTo(Duration.ofMillis(30L)) > 0) {")
                    .contains("if (code != null && code.length() > 3) {")
                    .contains("if (data.length > 4) {");
        }
    }

    @Nested
    class Constraints {

        private FieldDefinition field(final Type type, final String annotation, final Literal argument) {
            return new FieldDefinition("f", type, new QualifiedName("a", "T"),
                    List.of(new Annotation(annotation, List.of(argument), true, AT)), "", AT);
        }

        @Test
        void shouldRejectConstraintsWithoutJavaCheck() {
            final Type string = new Type.BasicType(BuiltinType.lookup("string").orElseThrow(), List.of());
            final Literal one = new Literal.NumberLiteral("1", AT);
            final Imports imports = new Imports("a");
            assertThatThrownBy(() -> JavaConstraints.of(field(string, "min", one), "", imports))
                    .isInstanceOf(JavaTypes.UnsupportedTypeException.class)
                    .hasMessage("Type '@@min on string' has no Java mapping yet");
            assertThatThrownBy(() -> JavaConstraints.of(field(string, "minSize", one), "", imports))
                    .hasMessage("Type 'size of string' has no Java mapping yet");
            final Type declared = new Type.DeclaredType(new QualifiedName("a", "X"), List.of());
            assertThatThrownBy(() -> JavaConstraints.of(field(declared, "max", new Literal.NameLiteral("A", AT)), "",
                    imports)).hasMessage("Type '@@max on a.X' has no Java mapping yet");
            assertThatThrownBy(() -> JavaConstraints.of(field(declared, "maxSize", one), "", imports))
                    .hasMessage("Type 'size of a.X' has no Java mapping yet");
        }

        @Test
        void shouldIgnoreAnnotationsWithoutCheckAndUseNonStringPatternsAsWritten() {
            final Type string = new Type.BasicType(BuiltinType.lookup("string").orElseThrow(), List.of());
            final JavaConstraints.Checks checks = JavaConstraints.of(new FieldDefinition("zipCode", string,
                    new QualifiedName("a", "T"), List.of(new Annotation("immutable", List.of(), false, AT),
                    new Annotation("pattern", List.of(new Literal.NameLiteral("X", AT)), true, AT)), "", AT), "",
                    new Imports("a"));
            assertThat(checks.constants()).containsExactly(
                    "private static final Pattern ZIP_CODE_PATTERN = Pattern.compile(\"X\");");
            assertThat(checks.statements()).contains("throw new IllegalArgumentException(\"zipCode must match the"
                    + " pattern X\");");
        }
    }

    /**
     * Compiles the records of the golden spec and checks their behaviour.
     */
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class Behaviour {

        private ClassLoader loader;

        @BeforeAll
        void compile(@TempDir final Path directory) throws Exception {
            final Path spec = Path.of(RecordGeneratorTest.class.getResource("/model-golden/spec").toURI());
            final GeneratedJava.Compilation compilation = GeneratedJava.compile(new JavaGenerator().generate(
                    LinkedModel.of(new MetaLang().validate(spec).model())), directory);
            assertThat(compilation.diagnostics()).isEmpty();
            loader = compilation.classLoader();
        }

        private Class<?> type(final String name) throws ClassNotFoundException {
            return loader.loadClass("org.hiero." + name);
        }

        private Object create(final String name, final Object... arguments) throws Throwable {
            final Constructor<?> constructor = canonical(type(name), arguments.length);
            try {
                return constructor.newInstance(arguments);
            } catch (final InvocationTargetException e) {
                throw e.getCause();
            }
        }

        private Constructor<?> canonical(final Class<?> type, final int parameters) {
            for (final Constructor<?> constructor : type.getConstructors()) {
                if (constructor.getParameterCount() == parameters) {
                    return constructor;
                }
            }
            throw new IllegalArgumentException("No constructor with " + parameters + " parameters");
        }

        private Object get(final Object record, final String component) throws Exception {
            return record.getClass().getMethod(component).invoke(record);
        }

        private Object money() throws Throwable {
            return create("shop.money.Money", BigInteger.TEN, null);
        }

        private Object category() throws Exception {
            return type("shop.Category").getEnumConstants()[0];
        }

        private Object cart(final List<String> items, final int quantity, final Integer discount) throws Throwable {
            return create("shop.Cart", items, Set.of(), Map.of(), quantity, discount, false, category(), money());
        }

        @Test
        void shouldCheckNullsAndStringConstraints() throws Throwable {
            assertThat(get(create("shop.Address", "Main St 1", null, null, "https://maps.example.org"), "street"))
                    .isEqualTo("Main St 1");
            assertThatThrownBy(() -> create("shop.Address", null, null, null, "https://maps.example.org"))
                    .isInstanceOf(NullPointerException.class).hasMessage("street must not be null");
            assertThatThrownBy(() -> create("shop.Address", "", null, null, "https://maps.example.org"))
                    .isInstanceOf(IllegalArgumentException.class).hasMessage("street must be at least 1 characters long");
            assertThatThrownBy(() -> create("shop.Address", "x".repeat(81), null, null, "https://maps.example.org"))
                    .hasMessage("street must be at most 80 characters long");
            assertThat(create("shop.Address", "a", "12345", null, "https://maps.example.org")).isNotNull();
            assertThatThrownBy(() -> create("shop.Address", "a", "1234a", null, "https://maps.example.org"))
                    .hasMessage("zip must match the pattern ^[0-9]{5}$");
        }

        @Test
        void shouldCheckUrlsWithUri() throws Throwable {
            assertThat(create("shop.Address", "a", null, "https://example.org/shop", "https://maps.example.org"))
                    .isNotNull();
            assertThatThrownBy(() -> create("shop.Address", "a", null, "/relative", "https://maps.example.org"))
                    .hasMessage("website must be an absolute URL with a host: /relative");
            assertThatThrownBy(() -> create("shop.Address", "a", null, null, "https://exa mple.org"))
                    .hasMessage("map must be a valid URL: https://exa mple.org")
                    .hasCauseInstanceOf(URISyntaxException.class);
            assertThatThrownBy(() -> create("shop.Address", "a", null, null, "mailto:shop@example.org"))
                    .hasMessage("map must be an absolute URL with a host: mailto:shop@example.org");
        }

        @Test
        void shouldCopyCollectionsAndCheckSizesAndNumbers() throws Throwable {
            // GIVEN
            final List<String> items = new ArrayList<>(List.of("apple"));

            // WHEN
            final Object cart = cart(items, 2, null);
            items.add("pear");

            // THEN
            assertThat(get(cart, "items")).isEqualTo(List.of("apple"));
            assertThatThrownBy(() -> ((List<?>) get(cart, "items")).clear())
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> cart(List.of(), 1, null)).hasMessage("items must contain at least 1 element(s)");
            assertThatThrownBy(() -> cart(java.util.Collections.nCopies(101, "x"), 1, null))
                    .hasMessage("items must contain at most 100 element(s)");
            assertThatThrownBy(() -> cart(List.of("a"), 0, null)).hasMessage("quantity must be at least 1");
            assertThatThrownBy(() -> cart(List.of("a"), 11, null)).hasMessage("quantity must be at most 10");
            assertThatThrownBy(() -> cart(List.of("a"), 1, -1)).hasMessage("discount must be at least 0");
            assertThat(get(cart(List.of("a"), 1, 0), "discount")).isEqualTo(0);
            assertThatThrownBy(() -> create("shop.money.Money", new BigInteger("-1000000000000000001"), null))
                    .hasMessage("cents must be at least -1000000000000000000");
        }

        @Test
        void shouldOfferAConstructorWithDefaults() throws Throwable {
            // WHEN
            final Object cart = create("shop.Cart", List.of("a"), Map.of(), null, true, category(), money());

            // THEN
            assertThat(get(cart, "coupons")).isEqualTo(Set.of());
            assertThat(get(cart, "quantity")).isEqualTo(1);
            assertThat(get(cart, "legacy")).isEqualTo(true);
            assertThat(type("shop.Cart").getMethod("legacy").isAnnotationPresent(Deprecated.class)).isTrue();
        }

        @Test
        void shouldCopyBytesAndCompareThemByContent() throws Throwable {
            // GIVEN
            final byte[] content = {1, 2};

            // WHEN
            final Object attachment = create("shop.Attachment", content, null, 0.5, null);
            content[0] = 9;

            // THEN
            assertThat((byte[]) get(attachment, "content")).containsExactly(1, 2);
            ((byte[]) get(attachment, "content"))[1] = 9;
            assertThat((byte[]) get(attachment, "content")).containsExactly(1, 2);
            final Object same = create("shop.Attachment", new byte[] {1, 2}, null, 0.5, null);
            assertThat(attachment).isEqualTo(same).hasSameHashCodeAs(same)
                    .hasToString("Attachment[content=byte[2], checksum=null, weight=0.5, count=null]");
            assertThat(attachment).isNotEqualTo(create("shop.Attachment", new byte[] {1, 2}, new byte[] {3}, 0.5,
                    null)).isNotEqualTo("other");
            final Object withChecksum = create("shop.Attachment", new byte[] {1}, new byte[] {3}, 0.5, 2);
            assertThat((byte[]) get(withChecksum, "checksum")).containsExactly(3);
            assertThat(withChecksum).hasToString("Attachment[content=byte[1], checksum=byte[1], weight=0.5, count=2]");
            assertThatThrownBy(() -> create("shop.Attachment", new byte[0], null, 0.5, null))
                    .hasMessage("content must contain at least 1 element(s)");
        }

        @Test
        void shouldGenerateMethodsAsStubs() throws Throwable {
            // GIVEN
            final Object hash = create("shop.Hash", (Object) new byte[] {1});
            final Method sum = type("shop.Cart").getMethod("sum");

            // WHEN / THEN
            assertThatThrownBy(hash::toString).isInstanceOf(UnsupportedOperationException.class)
                    .hasMessage("Not implemented yet: Hash.toString");
            assertThatThrownBy(() -> sum.invoke(cart(List.of("a"), 1, null)))
                    .hasCauseInstanceOf(UnsupportedOperationException.class);
        }
    }
}
