package org.hiero.sdk.v3.metalang.generator.java;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hiero.sdk.v3.metalang.MetaLang;
import org.hiero.sdk.v3.metalang.TestSpecs;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.generator.GenerationException;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.hiero.sdk.v3.metalang.model.QualifiedName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ClassGeneratorTest {

    @TempDir
    Path temp;

    private static LinkedModel model(final String schema) {
        return LinkedModel.of(new MetaLang().validate(Map.of("f/a.md", TestSpecs.markdown(schema))).model());
    }

    private static JavaGeneratorConfig interfaces(final String... names) {
        return new JavaGeneratorConfig(Set.of(names).stream().map(n -> new QualifiedName("a", n))
                .collect(java.util.stream.Collectors.toSet()));
    }

    private static String source(final List<GeneratedFile> files, final String type) {
        return files.stream().filter(f -> f.path().endsWith("/src/main/java/org/hiero/a/" + type + ".java")
                || f.path().endsWith("/" + type + ".java") && f.path().contains("/src/main/java/")).findFirst()
                .orElseThrow().content();
    }

    /** The first line of the type declaration of every generated type, by name. */
    private static Map<String, String> headers(final List<GeneratedFile> files) {
        final Map<String, String> headers = new java.util.TreeMap<>();
        for (final GeneratedFile file : files) {
            if (file.path().endsWith(".java") && !file.path().endsWith("-info.java")
                    && file.path().contains("/src/main/java/")) {
                final String name = file.path().substring(file.path().lastIndexOf('/') + 1,
                        file.path().length() - ".java".length());
                headers.put(name, file.content().lines().filter(l -> l.startsWith("public ")).findFirst()
                        .orElseThrow());
            }
        }
        return headers;
    }

    private List<GeneratedFile> compiled(final String schema, final JavaGeneratorConfig config) throws IOException {
        final List<GeneratedFile> files = new JavaGenerator(config).generate(model(schema));
        final GeneratedJava.Compilation compilation = GeneratedJava.compile(files, temp);
        assertThat(compilation.diagnostics()).isEmpty();
        assertThat(compilation.success()).isTrue();
        return files;
    }

    @Nested
    class Classification {

        @Test
        void shouldUseAbstractClassesForAbstractionsWithAttributes() throws IOException {
            // WHEN
            final Map<String, String> headers = headers(compiled("""
                    namespace a
                    abstraction WithAttribute { @@immutable a: int32 }
                    abstraction Inherits extends WithAttribute { }
                    abstraction Plain { int32 compute() }
                    abstraction Final { @@finalMethod int32 id() }
                    Concrete extends Inherits, Plain { @@immutable b: int32 }
                    """, JavaGeneratorConfig.DEFAULT));

            // THEN
            assertThat(headers).containsEntry("WithAttribute", "public abstract class WithAttribute {")
                    .containsEntry("Inherits", "public abstract class Inherits extends WithAttribute {")
                    .containsEntry("Plain", "public interface Plain {")
                    .containsEntry("Final", "public abstract class Final {")
                    .containsEntry("Concrete", "public class Concrete extends Inherits implements Plain {");
        }

        @Test
        void shouldUseInterfacesWhereJavaHasNoMultipleInheritanceOrEnumsExtendThem() throws IOException {
            // WHEN
            final Map<String, String> headers = headers(compiled("""
                    namespace a
                    abstraction Coded { @@immutable code: int32 }
                    enum Status(code: int32) extends Coded { OK(0) }
                    abstraction Left { @@immutable left: int32 }
                    abstraction Right { @@immutable right: int32 }
                    Both extends Left, Right { }
                    abstraction Root { @@immutable root: int32 }
                    abstraction Middle extends Root { }
                    @@sealed(Leaf)
                    abstraction Sealed extends Middle { }
                    Leaf extends Sealed { }
                    Concrete { @@immutable c: int32 }
                    abstraction Mixed { @@immutable m: int32 }
                    Other extends Concrete, Mixed { }
                    """, JavaGeneratorConfig.DEFAULT));

            // THEN
            assertThat(headers).containsEntry("Coded", "public interface Coded {")
                    .containsEntry("Status", "public enum Status implements Coded {")
                    // none of the two becomes a class
                    .containsEntry("Left", "public interface Left {")
                    .containsEntry("Right", "public interface Right {")
                    .containsEntry("Both", "public record Both(int left, int right) implements Left, Right {")
                    // a class hierarchy that is not affected stays a class hierarchy
                    .containsEntry("Sealed", "public abstract sealed class Sealed extends Middle permits Leaf {")
                    .containsEntry("Leaf", "public final class Leaf extends Sealed {")
                    // a concrete supertype keeps its class, the abstraction gives way
                    .containsEntry("Mixed", "public interface Mixed {")
                    .containsEntry("Other", "public class Other extends Concrete implements Mixed {");
        }

        @Test
        void shouldResolveConflictsWithTheConfiguration() throws IOException {
            // WHEN the configuration makes one of the two an interface
            final Map<String, String> headers = headers(compiled("""
                    namespace a
                    abstraction Left { @@immutable left: int32 }
                    abstraction Right { @@immutable right: int32 }
                    abstraction Top { @@immutable top: int32 }
                    abstraction Below extends Top { }
                    Both extends Left, Right { }
                    """, interfaces("Right", "Below")));

            // THEN the other one stays an abstract class; an interface makes its supertypes interfaces
            assertThat(headers).containsEntry("Left", "public abstract class Left {")
                    .containsEntry("Right", "public interface Right {")
                    .containsEntry("Both", "public class Both extends Left implements Right {")
                    .containsEntry("Below", "public interface Below extends Top {")
                    .containsEntry("Top", "public interface Top {");
        }

        @Test
        void shouldRejectConfigurationsThatDoNotMatchTheModel() {
            final LinkedModel model = model("""
                    namespace a
                    abstraction Final { @@finalMethod int32 id() }
                    Concrete { @@immutable a: int32 }
                    """);
            assertThatThrownBy(() -> new JavaGenerator(interfaces("Final", "Concrete", "Missing")).generate(model))
                    .isInstanceOfSatisfying(GenerationException.class, e -> assertThat(e.problems())
                            .containsExactly("java.interfaces: a.Concrete is no abstraction",
                                    "java.interfaces: a.Final has a @@finalMethod and must be an abstract class",
                                    "java.interfaces: unknown type a.Missing"));
        }

        @Test
        void shouldDeferTypesThatCannotExtendAFinalMethodClass() {
            // GIVEN abstractions with @@finalMethod, which must stay classes
            final Map<QualifiedName, String> deferred = new JavaGenerator().deferredTypes(model("""
                    namespace a
                    abstraction One { @@finalMethod int32 one() }
                    abstraction Two { @@finalMethod int32 two() }
                    Both extends One, Two { }
                    enum E extends One { A }
                    abstraction Sub extends One { }
                    """));

            // THEN
            assertThat(deferred).containsEntry(new QualifiedName("a", "Both"),
                    "extends the classes [a.One, a.Two]; Java has no multiple inheritance")
                    .containsEntry(new QualifiedName("a", "E"), "an enum cannot extend the class a.One");
            assertThat(deferred).doesNotContainKey(new QualifiedName("a", "Sub"));
        }

        @Test
        void shouldDeferCovariantAsyncOverrides() {
            assertThat(new JavaGenerator().deferredTypes(model("""
                    namespace a
                    Response { @@immutable code: int32 }
                    Paid extends Response { @@immutable cost: int64 }
                    abstraction Query { @@async Response submit(retries: int32) }
                    abstraction PaidQuery extends Query { @@async Paid submit(retries: int32) }
                    abstraction Other extends Query { @@async Response submit(retries: int32)
                        @@async Paid submit(retries: int32, fast: bool) }
                    """))).containsExactly(Map.entry(new QualifiedName("a", "PaidQuery"),
                    "overrides the @@async method a.Query.submit with the return type a.Paid instead of a.Response;"
                            + " CompletionStage<T> is invariant in Java"));
        }
    }

    @Nested
    class Source {

        @Test
        void shouldGenerateAbstractClassesWithStateAndSelfTypedSetters() throws IOException {
            // WHEN
            final List<GeneratedFile> files = compiled("""
                    namespace a
                    abstraction Receipt { }
                    // A transaction.
                    abstraction Transaction<$$Receipt extends Receipt, $$Self extends Transaction<$$Receipt, $$Self>> {
                        @@nullable memo: string                 // the memo
                        @@default(3) @@min(1) attempts: int32
                        @@immutable @@minSize(1) nodes: list<string>
                        $$Receipt execute()
                        @@finalMethod int32 size()
                        @@static int32 limit()
                    }
                    MyReceipt extends Receipt { @@immutable id: int64 }
                    @@finalType
                    MyTransaction extends Transaction<MyReceipt, MyTransaction> { @@immutable amount: int64 }
                    """, JavaGeneratorConfig.DEFAULT);

            // THEN
            assertThat(source(files, "Transaction"))
                    .contains("public abstract class Transaction<ReceiptT extends Receipt, "
                            + "Self extends Transaction<ReceiptT, Self>> {")
                    .contains("    private @Nullable String memo;\n    private int attempts = 3;\n"
                            + "    private final List<String> nodes;\n")
                    .contains("    protected Transaction(final List<String> nodes) {\n"
                            + "        Objects.requireNonNull(nodes, \"nodes must not be null\");\n")
                    .contains("        this.nodes = List.copyOf(nodes);\n")
                    .contains("    @SuppressWarnings(\"unchecked\")\n    public Self setAttempts(final int attempts) {\n"
                            + "        if (attempts < 1) {\n")
                    .contains("        return (Self) this;\n")
                    .contains("    public abstract ReceiptT execute();\n")
                    .contains("    public final int size() {\n        throw new UnsupportedOperationException")
                    .contains("    public static int limit() {")
                    .doesNotContain("equals")
                    .doesNotContain("toString");
            assertThat(source(files, "MyTransaction"))
                    .contains("public final class MyTransaction extends Transaction<MyReceipt, MyTransaction> {")
                    .contains("    public MyTransaction(final List<String> nodes, final long amount) {\n"
                            + "        super(nodes);\n")
                    .contains("    @Override\n    public MyReceipt execute() {")
                    .doesNotContain("size()")
                    .doesNotContain("limit()")
                    .doesNotContain("public boolean equals")
                    .contains("    public String toString() {\n        return \"MyTransaction[memo=\" + memo()\n");
        }

        @Test
        void shouldOverrideInheritedSettersCovariantlyAndNarrowNullability() throws IOException {
            // WHEN
            final List<GeneratedFile> files = compiled("""
                    namespace a
                    abstraction Base {
                        @@nullable label: string
                        @@immutable @@nullable id: int64
                    }
                    abstraction Middle extends Base { }
                    Leaf extends Middle { @@immutable @@override id: int64 }
                    """, JavaGeneratorConfig.DEFAULT);

            // THEN
            assertThat(source(files, "Middle"))
                    .contains("    @Override\n    public Middle setLabel(final @Nullable String label) {\n"
                            + "        super.setLabel(label);\n        return this;\n");
            assertThat(source(files, "Leaf"))
                    .contains("    public Leaf(final Long id) {\n        super(id);\n"
                            + "        Objects.requireNonNull(id, \"id must not be null\");\n")
                    .contains("    @Override\n    public Long id() {\n        return super.id();\n")
                    .contains("    public Leaf setLabel(");
        }

        @Test
        void shouldGenerateValueClassesWithDefaultsAndBytes() throws IOException {
            // WHEN
            final List<GeneratedFile> files = compiled("""
                    namespace a
                    abstraction Key { @@immutable bytes: bytes
                        @@immutable @@nullable @@default(1) version: int32 }
                    PublicKey extends Key { @@immutable @@nullable label: string }
                    Service { list<string> names() }
                    """, JavaGeneratorConfig.DEFAULT);

            // THEN
            assertThat(source(files, "Key"))
                    .contains("        this.bytes = bytes.clone();\n")
                    .contains("    public byte[] bytes() {\n        return bytes.clone();\n");
            assertThat(source(files, "PublicKey"))
                    .contains("    public PublicKey(final byte[] bytes, final @Nullable String label) {\n"
                            + "        this(bytes, 1, label);\n")
                    .contains("            && Arrays.equals(bytes(), other.bytes())\n")
                    .contains("        return Objects.hash(Arrays.hashCode(bytes()), version(), label());\n")
                    .contains("\"PublicKey[bytes=\" + \"byte[\" + bytes().length + \"]\"\n");
            assertThat(source(files, "Service")).contains("public class Service {\n")
                    .contains("    public Service() {\n    }\n")
                    .doesNotContain("toString");
        }

        @Test
        void shouldCompareEveryFieldWithTheOperatorItsJavaTypeNeeds() throws IOException {
            // GIVEN one field per Java type that `equals` has to treat differently
            // (an abstraction as superclass: a type with only immutable fields would become a record)
            final List<GeneratedFile> files = compiled("""
                    namespace a
                    abstraction Value { @@immutable tiny: int8 }
                    Primitives extends Value {
                        @@immutable small: int16
                        @@immutable medium: int32
                        @@immutable big: int64
                        @@immutable ratio: double
                        @@immutable flag: bool
                        @@immutable label: string
                        @@immutable payload: bytes
                        @@immutable huge: int128
                    }
                    """, JavaGeneratorConfig.DEFAULT);

            // THEN primitives are compared with ==, double with Double.compare (NaN and -0.0),
            // arrays with Arrays.equals and everything else with Objects.equals
            assertThat(source(files, "Primitives"))
                    .contains("            && tiny() == other.tiny()\n")
                    .contains("            && small() == other.small()\n")
                    .contains("            && medium() == other.medium()\n")
                    .contains("            && big() == other.big()\n")
                    .contains("            && Double.compare(ratio(), other.ratio()) == 0\n")
                    .contains("            && flag() == other.flag()\n")
                    .contains("            && Objects.equals(label(), other.label())\n")
                    .contains("            && Arrays.equals(payload(), other.payload())\n")
                    .contains("            && Objects.equals(huge(), other.huge())");
        }

        @Test
        void shouldUseNonSealedForExtensibleSubclassesOfSealedTypes() throws IOException {
            assertThat(headers(compiled("""
                    namespace a
                    @@sealed(Open, Closed)
                    abstraction Shape { @@immutable size: int32 }
                    Open extends Shape { }
                    Closed extends Shape { }
                    Sub extends Open { }
                    """, JavaGeneratorConfig.DEFAULT)))
                    .containsEntry("Shape", "public abstract sealed class Shape permits Open, Closed {")
                    .containsEntry("Open", "public non-sealed class Open extends Shape {")
                    .containsEntry("Closed", "public final class Closed extends Shape {")
                    .containsEntry("Sub", "public class Sub extends Open {");
        }
    }

    /**
     * Compiles a class hierarchy and calls it.
     */
    @Nested
    class Behaviour {

        private ClassLoader loader() throws IOException {
            final GeneratedJava.Compilation compilation = GeneratedJava.compile(new JavaGenerator().generate(model("""
                    namespace a
                    abstraction Builder<$$Self extends Builder<$$Self>> {
                        @@nullable @@maxLength(5) memo: string
                        @@immutable @@minSize(1) nodes: list<string>
                    }
                    @@finalType
                    Payment extends Builder<Payment> { @@immutable amount: int64 }
                    abstraction Value { @@immutable data: bytes }
                    @@finalType
                    Hash extends Value { @@immutable @@nullable label: string }
                    """)), temp);
            assertThat(compilation.diagnostics()).isEmpty();
            return compilation.classLoader();
        }

        private Object create(final Class<?> type, final Object... arguments) throws Throwable {
            try {
                return type.getConstructors()[0].newInstance(arguments);
            } catch (final InvocationTargetException e) {
                throw e.getCause();
            }
        }

        private Object call(final Object target, final String method, final Class<?> parameter, final Object value)
                throws Throwable {
            try {
                return target.getClass().getMethod(method, parameter).invoke(target, value);
            } catch (final InvocationTargetException e) {
                throw e.getCause();
            }
        }

        @Test
        void shouldCheckValuesAndChainSetters() throws Throwable {
            // GIVEN
            final Class<?> payment = loader().loadClass("org.hiero.a.Payment");
            final java.util.List<String> nodes = new java.util.ArrayList<>(List.of("node"));

            // WHEN
            final Object instance = create(payment, nodes, 5L);
            nodes.add("other");

            // THEN
            assertThat(Modifier.isFinal(payment.getModifiers())).isTrue();
            assertThat(payment.getMethod("nodes").invoke(instance)).isEqualTo(List.of("node"));
            assertThat(call(instance, "setMemo", String.class, "hi")).isSameAs(instance);
            assertThat(payment.getMethod("memo").invoke(instance)).isEqualTo("hi");
            assertThatThrownBy(() -> call(instance, "setMemo", String.class, "too long"))
                    .isInstanceOf(IllegalArgumentException.class).hasMessage("memo must be at most 5 characters long");
            assertThatThrownBy(() -> create(payment, List.of(), 5L))
                    .hasMessage("nodes must contain at least 1 element(s)");
            assertThatThrownBy(() -> create(payment, null, 5L))
                    .isInstanceOf(NullPointerException.class).hasMessage("nodes must not be null");
            assertThat(instance).hasToString("Payment[memo=hi, nodes=[node], amount=5]")
                    .isNotEqualTo(create(payment, List.of("node"), 5L));
        }

        @Test
        void shouldCompareValueClassesByContent() throws Throwable {
            // GIVEN
            final Class<?> hash = loader().loadClass("org.hiero.a.Hash");
            final byte[] data = {1, 2};

            // WHEN
            final Object instance = create(hash, data, null);
            data[0] = 9;

            // THEN
            assertThat((byte[]) hash.getMethod("data").invoke(instance)).containsExactly(1, 2);
            assertThat(instance).isEqualTo(create(hash, new byte[] {1, 2}, null))
                    .hasSameHashCodeAs(create(hash, new byte[] {1, 2}, null))
                    .isNotEqualTo(create(hash, new byte[] {1, 2}, "x"))
                    .isNotEqualTo("other")
                    .hasToString("Hash[data=byte[2], label=null]");
            assertThat(instance.equals(instance)).isTrue();
        }
    }
}
