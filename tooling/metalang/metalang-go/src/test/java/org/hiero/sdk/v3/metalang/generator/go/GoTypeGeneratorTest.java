package org.hiero.sdk.v3.metalang.generator.go;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.hiero.sdk.v3.metalang.MetaLang;
import org.hiero.sdk.v3.metalang.TestSpecs;
import org.hiero.sdk.v3.metalang.ValidationReport;
import org.hiero.sdk.v3.metalang.diagnostic.Severity;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class GoTypeGeneratorTest {

    /** One attribute per entry of the type mapping table in the guideline. */
    private static final String EVERY_TYPE = """
            namespace a
            abstraction Unit {
                @@immutable symbol: string
            }
            Leaf {
                @@immutable @@nullable note: string
                @@immutable data: bytes
                @@immutable tags: list<string>
                @@immutable labels: set<string>
                @@immutable sizes: map<string, int32>
                @@immutable when: zonedDateTime
                @@immutable delay: seconds
                @@immutable big: uint256
                @@immutable small: int24
                @@immutable ratio: double
                @@immutable flag: bool
                @@immutable kind: type<Unit>
                @@immutable unit: Unit
                @@immutable handler: function<void run(x: int32)>
                @@immutable anything: ANY
                @@immutable others: set<Leaf>
                @@immutable @@nullable maybeUnit: Unit
            }
            """;

    private static LinkedModel model(final String schema) {
        return model(Map.of("f/a.md", schema));
    }

    private static LinkedModel model(final Map<String, String> schemas) {
        final Map<String, String> documents = new TreeMap<>();
        schemas.forEach((file, schema) -> documents.put(file, TestSpecs.markdown(schema)));
        final ValidationReport report = new MetaLang().validate(documents);
        assertThat(report.diagnostics()).filteredOn(d -> d.severity() == Severity.ERROR).isEmpty();
        return LinkedModel.of(report.model());
    }

    private static String generate(final String schema, final String path) {
        final List<GeneratedFile> files = new GoGenerator().generate(model(schema));
        return files.stream().filter(f -> f.path().equals(path)).findFirst()
                .orElseThrow(() -> new AssertionError("no " + path + " in "
                        + files.stream().map(GeneratedFile::path).toList())).content();
    }

    @Nested
    class TypeMapping {

        @Test
        void shouldMapEveryBasicTypeToItsGoForm() {
            // WHEN
            final String go = generate(EVERY_TYPE, "a/leaf.go");

            // THEN the attributes carry the types of the guideline's mapping table
            assertThat(go).contains("""
                    type Leaf struct {
                    \tnote      *string
                    \tdata      []byte
                    \ttags      []string
                    \tlabels    map[string]struct{}
                    \tsizes     map[string]int32
                    \twhen      time.Time
                    \tdelay     time.Duration
                    \tbig       *big.Int
                    \tsmall     int32
                    \tratio     float64
                    \tflag      bool
                    \tkind      reflect.Type
                    \tunit      Unit
                    \thandler   func(int32)
                    \tanything  any
                    \tothers    []Leaf
                    \tmaybeUnit Unit
                    }
                    """);
        }

        @Test
        void shouldPointerOnlyWhatIsNotAlreadyNilable() {
            // GIVEN two nullable attributes, one of them an interface
            final String go = generate(EVERY_TYPE, "a/leaf.go");

            // THEN a string needs the pointer to carry "absent", an interface is nil on its own
            assertThat(go).contains("\tnote      *string").contains("\tmaybeUnit Unit");
        }

        @Test
        void shouldUseASliceForASetOfAnIncomparableElement() {
            // GIVEN Leaf has slice attributes, which makes it incomparable, and a set<string> which does not
            final String go = generate(EVERY_TYPE, "a/leaf.go");

            // THEN only the comparable element becomes a map; Go rejects a map keyed by a struct with a slice
            assertThat(go).contains("labels    map[string]struct{}").contains("others    []Leaf");
        }

        @Test
        void shouldWidenAnIntegerToTheNextWidthGoHas() {
            final String go = generate("""
                    namespace a
                    Widths {
                        @@immutable a: int8
                        @@immutable b: int12
                        @@immutable c: uint48
                        @@immutable d: int64
                        @@immutable e: int100
                    }
                    """, "a/widths.go");

            assertThat(go).contains("\ta int8\n\tb int16\n\tc uint64\n\td int64\n\te *big.Int\n");
        }

        @Test
        void shouldCopyEveryValueTheCallerCouldOtherwiseReachInto() {
            // GIVEN slices, bytes and maps are reference types in Go
            final String go = generate(EVERY_TYPE, "a/leaf.go");

            // THEN they are cloned on the way in and on the way out, so @@immutable holds
            assertThat(go).contains("data:      slices.Clone(data),")
                    .contains("sizes:     maps.Clone(sizes),")
                    .contains("\treturn slices.Clone(l.data)\n")
                    .contains("\treturn maps.Clone(l.sizes)\n")
                    // a value type needs no copy
                    .contains("\treturn l.when\n");
        }
    }

    @Nested
    class Abstractions {

        @Test
        void shouldGenerateAnInterfaceWithAGetterPerAttribute() {
            final String go = generate("""
                    namespace a
                    // A shape.
                    abstraction Shape {
                        // The name of the shape.
                        @@immutable name: string
                        @@immutable @@nullable note: string
                    }
                    """, "a/shape.go");

            assertThat(go).contains("""
                    // Shape a shape.
                    type Shape interface {
                    \t// Name returns the name of the shape.
                    \tName() string

                    \t// Note returns the note.
                    \tNote() *string
                    }
                    """);
        }

        @Test
        void shouldEmbedAnAbstractionSupertypeIntoTheInterface() {
            final String go = generate("""
                    namespace a
                    abstraction Top { @@immutable id: int32 }
                    abstraction Middle extends Top { @@immutable name: string }
                    """, "a/middle.go");

            assertThat(go).contains("type Middle interface {\n\tTop\n");
        }

        @Test
        void shouldGiveASealedAbstractionAnUnexportedMarkerItsSubtypesImplement() {
            final LinkedModel model = model("""
                    namespace a
                    @@sealed(Leaf)
                    abstraction Tree { @@immutable name: string }
                    Leaf extends Tree { }
                    """);
            final List<GeneratedFile> files = new GoGenerator().generate(model);
            final String tree = files.stream().filter(f -> f.path().equals("a/tree.go")).findFirst()
                    .orElseThrow().content();
            final String leaf = files.stream().filter(f -> f.path().equals("a/leaf.go")).findFirst()
                    .orElseThrow().content();

            // only this package can implement an unexported method - Go's sealed hierarchy
            assertThat(tree).contains("\tisTree()\n");
            assertThat(leaf).contains("func (l Leaf) isTree() {}");
        }

        @Test
        void shouldCarryInheritedAttributesInEverySubtype() {
            // GIVEN an abstraction with state, inherited across packages
            final LinkedModel model = model(Map.of(
                    "f/a.md", "namespace a\nabstraction Top { @@immutable id: int32 }\n",
                    "f/b.md", "namespace b\nrequires {Top} from a\nSub extends Top { @@immutable name: string }\n"));

            // WHEN
            final String go = new GoGenerator().generate(model).stream()
                    .filter(f -> f.path().equals("b/sub.go")).findFirst().orElseThrow().content();

            // THEN the subtype carries the inherited attribute itself: an unexported base struct could not be
            // embedded from another package, and the fields have to stay unexported
            assertThat(go).contains("type Sub struct {\n\tid   int32\n\tname string\n}")
                    .contains("func NewSub(id int32, name string) Sub")
                    .contains("func (s Sub) ID() int32");
        }
    }

    @Nested
    class Generics {

        @Test
        void shouldDeclareATypeParameterWithItsBound() {
            final String go = generate("""
                    namespace a
                    abstraction Unit { @@immutable symbol: string }
                    Holder<$$U extends Unit> { @@immutable unit: $$U }
                    Box<$$T> { @@immutable value: $$T }
                    """, "a/holder.go");

            assertThat(go).contains("type Holder[U Unit] struct {\n\tunit U\n}")
                    .contains("func NewHolder[U Unit](unit U) Holder[U]")
                    .contains("func (h Holder[U]) Unit() U");
        }

        @Test
        void shouldDropASelfTypeParameter() {
            // GIVEN the F-bounded self type the specs use for chaining setters
            final String go = generate("""
                    namespace a
                    abstraction Unit { @@immutable symbol: string }
                    abstraction Token<$$Self extends Token<$$Self, $$Unit>, $$Unit extends Unit> {
                        @@immutable unit: $$Unit
                    }
                    Wallet { @@immutable token: Token<ANY, ANY> }
                    """, "a/wallet.go");

            // THEN Go neither declares nor passes it: it carries nothing a Go signature could use, and it is
            // what makes Token<ANY, ANY> nameable in Go at all
            assertThat(go).contains("\ttoken Token[Unit]\n");
        }

        @Test
        void shouldRenameATypeParameterThatWouldShadowADeclaredType() {
            final String go = generate("""
                    namespace a
                    abstraction Receipt { @@immutable status: int32 }
                    Response<$$Receipt extends Receipt> { @@immutable receipt: $$Receipt }
                    """, "a/response.go");

            // `type Response[Receipt Receipt]` is rejected: "cannot use a type parameter as constraint"
            assertThat(go).contains("type Response[ReceiptT Receipt] struct {\n\treceipt ReceiptT\n}")
                    .contains("func (r Response[ReceiptT]) Receipt() ReceiptT");
        }

        @Test
        void shouldWidenAConcreteBoundAndSayWhyInTheDocComment() {
            final String go = generate("""
                    namespace a
                    Base { @@immutable id: int32 }
                    Holder<$$B extends Base> { @@immutable value: $$B }
                    """, "a/holder.go");

            // Go has no subtyping for structs, so a type set of one struct would admit that struct only
            assertThat(go).contains("unconstrained here")
                    .contains("type Holder[B any] struct {");
        }
    }

    @Nested
    class Gaps {

        private static Map<String, String> deferred(final String schema) {
            final GoGenerator generator = new GoGenerator();
            generator.generate(model(schema));
            final Map<String, String> reasons = new TreeMap<>();
            generator.deferred().forEach((name, reason) -> reasons.put(name.toString(), reason));
            return reasons;
        }

        @Test
        void shouldDeferATypeWhoseMapKeyGoCannotUse() {
            // GIVEN a key type that is not comparable in Go, because it has a slice attribute
            final Map<String, String> deferred = deferred("""
                    namespace a
                    Key { @@immutable parts: list<string> }
                    Holder { @@immutable byKey: map<Key, int32> }
                    """);

            // THEN the type is left out with the reason, rather than emitted as code that does not compile
            assertThat(deferred).containsOnlyKeys("a.Holder");
            assertThat(deferred.get("a.Holder")).contains("is not comparable");
        }

        @Test
        void shouldDeferATypeThatNeedsAThirdPartyModule() {
            final Map<String, String> deferred = deferred("""
                    namespace a
                    Money { @@immutable amount: decimal }
                    Tagged { @@immutable id: uuid }
                    """);

            assertThat(deferred).containsOnlyKeys("a.Money", "a.Tagged");
            assertThat(deferred.get("a.Money")).contains("shopspring/decimal");
            assertThat(deferred.get("a.Tagged")).contains("google/uuid");
        }

        @Test
        void shouldDeferEveryTypeThatRefersToADeferredOne() {
            // GIVEN a chain: Money cannot be generated, Wallet holds one, Owner holds a Wallet
            final Map<String, String> deferred = deferred("""
                    namespace a
                    Money { @@immutable amount: decimal }
                    Wallet { @@immutable money: Money }
                    Owner { @@immutable wallet: Wallet }
                    Unrelated { @@immutable name: string }
                    """);

            // THEN the gap propagates to a fixed point, and nothing else is affected
            assertThat(deferred).containsOnlyKeys("a.Money", "a.Wallet", "a.Owner");
            assertThat(deferred.get("a.Owner")).isEqualTo("it references a.Wallet, which cannot be generated");
        }

        @Test
        void shouldDeferAnEnumWhoseValuesDoNotMatchItsAttributes() {
            // GIVEN a spec the validator rejects; --fail-on=never lets the generator see it anyway
            final ValidationReport report = new MetaLang().validate(Map.of("f/a.md", TestSpecs.markdown("""
                    namespace a
                    enum Color(code: int32) {
                        RED
                    }
                    """)));
            assertThat(report.diagnostics()).isNotEmpty();

            // WHEN
            final GoGenerator generator = new GoGenerator();
            generator.generate(LinkedModel.of(report.model()));

            // THEN it is reported rather than crashing the generator
            assertThat(generator.deferred().values()).singleElement()
                    .asString().isEqualTo("the value RED gives 0 argument(s) for 1 attribute(s)");
        }

        @Test
        void shouldGenerateTheTypesThatHaveNoGap() {
            final LinkedModel model = model("""
                    namespace a
                    Money { @@immutable amount: decimal }
                    Plain { @@immutable name: string }
                    """);

            assertThat(new GoGenerator().generate(model)).extracting(GeneratedFile::path)
                    .contains("a/plain.go").doesNotContain("a/money.go");
        }
    }

    @Nested
    class Streaming {

        @Test
        void shouldMapAStreamResultToTheRangeOverFuncForm() {
            final String go = generate("""
                    namespace a
                    Source { @@immutable @@nullable items: streamResult<string> }
                    """, "a/source.go");

            // the form a caller ranges over, with the error travelling per item
            assertThat(go).contains("\titems iter.Seq2[string, error]\n").contains("\t\"iter\"\n");
        }
    }

    @Nested
    class Wildcards {

        @Test
        void shouldUseTheBoundOfAWildcardThatCarriesOne() {
            final String go = generate("""
                    namespace a
                    abstraction Unit { @@immutable symbol: string }
                    Holder<$$U extends Unit> { @@immutable unit: $$U }
                    Wallet { @@immutable holders: list<Holder<ANY extends Unit>> }
                    """, "a/wallet.go");

            assertThat(go).contains("\tholders []Holder[Unit]\n");
        }

        @Test
        void shouldUseTheTopTypeForAWildcardWithNoBoundAnywhere() {
            final String go = generate("""
                    namespace a
                    Box<$$T> { @@immutable value: $$T }
                    Wallet { @@immutable boxes: list<Box<ANY>> }
                    """, "a/wallet.go");

            assertThat(go).contains("\tboxes []Box[any]\n");
        }

        @Test
        void shouldSeeASelfTypeThroughACollection() {
            // the bound mentions the parameter inside a list, which still makes it a self type
            final String go = generate("""
                    namespace a
                    abstraction Node<$$Self extends Node<$$Self>> { @@immutable children: list<$$Self> }
                    Tree { @@immutable root: Node<ANY> }
                    """, "a/tree.go");

            assertThat(go).contains("\troot Node\n");
        }
    }

    @Nested
    class Enumerations {

        @Test
        void shouldGenerateADefinedIntegerTypeWithoutAttributes() {
            final String go = generate("""
                    namespace a
                    // A level.
                    enum Level {
                        // The low one.
                        LOW
                        HIGH
                    }
                    """, "a/level.go");

            assertThat(go).contains("""
                    // Level a level.
                    type Level int

                    const (
                    \t// LevelLow the low one.
                    \tLevelLow Level = iota
                    \tLevelHigh
                    )
                    """)
                    .contains("func (l Level) String() string {\n\tswitch l {\n\tcase LevelLow:\n\t\treturn \"LOW\"")
                    .contains("func LevelValues() []Level {\n\treturn []Level{LevelLow, LevelHigh}\n}")
                    .contains("func LevelValueOf(name string) (Level, error) {");
        }

        @Test
        void shouldGenerateAStructWithValuesWhenTheEnumHasAttributes() {
            final String go = generate("""
                    namespace a
                    enum Container { PKCS8 SPKI }
                    enum Format(container: Container, code: int32) {
                        PKCS8_WITH_DER(Container.PKCS8, 1)
                        SPKI_WITH_DER(SPKI, 2)
                    }
                    """, "a/format.go");

            // Go has no constant struct, so the values are variables with unexported fields
            assertThat(go).contains("""
                    type Format struct {
                    \tname      string
                    \tcontainer Container
                    \tcode      int32
                    }

                    var (
                    \tFormatPKCS8WithDER = Format{"PKCS8_WITH_DER", ContainerPKCS8, 1}
                    \tFormatSPKIWithDER  = Format{"SPKI_WITH_DER", ContainerSPKI, 2}
                    )
                    """)
                    .contains("func (f Format) Container() Container {")
                    .contains("func (f Format) String() string {\n\treturn f.name\n}");
        }

        @Test
        void shouldRenderABooleanAttributeOfAnEnum() {
            final String go = generate("""
                    namespace a
                    enum Mode(fast: bool, label: string) {
                        QUICK(true, "q")
                        SLOW(false, "s")
                    }
                    """, "a/mode.go");

            assertThat(go).contains("\tModeQuick = Mode{\"QUICK\", true, \"q\"}\n")
                    .contains("\tModeSlow  = Mode{\"SLOW\", false, \"s\"}\n");
        }

        @Test
        void shouldMarkADeprecatedValueTheWayGoVetRecognises() {
            final String go = generate("""
                    namespace a
                    enum Level {
                        LOW
                        // Use LOW instead.
                        @@deprecated HIGH
                    }
                    """, "a/level.go");

            assertThat(go).contains("""
                    \t// LevelHigh use LOW instead.
                    \t//
                    \t// Deprecated: this declaration should no longer be used.
                    \tLevelHigh
                    """);
        }
    }
}
