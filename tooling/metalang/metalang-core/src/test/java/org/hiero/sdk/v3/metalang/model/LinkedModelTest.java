package org.hiero.sdk.v3.metalang.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.hiero.sdk.v3.metalang.TestSpecs;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class LinkedModelTest {

    private static LinkedModel link(final String... schemas) {
        return LinkedModel.of(TestSpecs.validate(schemas).model());
    }

    private static TypeDefinition type(final LinkedModel model, final String namespace, final String name) {
        return model.type(namespace, name).orElseThrow();
    }

    private static List<String> fieldTypes(final TypeDefinition type) {
        return type.fields().stream().map(f -> f.name() + ": " + f.type().text()).toList();
    }

    @Nested
    class Resolution {

        @Test
        void shouldResolveEveryKindOfTypeReference() {
            // GIVEN
            final LinkedModel model = link("""
                    namespace a
                    requires {B} from b
                    G<$$T> {
                        @@immutable basic: list<map<string, int64>>
                        @@immutable imported: B
                        @@immutable qualified: b.B
                        @@immutable local: G<B>
                        @@immutable variable: $$T
                        @@immutable wildcard: G<ANY extends B>
                        @@immutable any: ANY
                        @@immutable callback: function<void onEvent(e: $$T, more: B...)>
                        @@immutable token: type<B>
                    }
                    """, "namespace b\nB {}");

            // WHEN
            final List<String> fields = fieldTypes(type(model, "a", "G"));

            // THEN
            assertThat(fields).containsExactly(
                    "basic: list<map<string, int64>>",
                    "imported: b.B",
                    "qualified: b.B",
                    "local: a.G<b.B>",
                    "variable: $$T",
                    "wildcard: a.G<ANY extends b.B>",
                    "any: ANY",
                    "callback: function<void onEvent(e: $$T, more: b.B...)>",
                    "token: type<b.B>");
        }

        @Test
        void shouldRepresentUnresolvableReferencesInsteadOfFailing() {
            // GIVEN
            final LinkedModel model = link("namespace a\nX { @@immutable m: Missing\n@@immutable t: $$Undeclared }");

            // THEN
            assertThat(type(model, "a", "X").fields()).extracting(FieldDefinition::type)
                    .containsExactly(new Type.UnresolvedType("Missing"), new Type.UnresolvedType("$$Undeclared"));
            assertThat(new Type.UnresolvedType("Missing").text()).isEqualTo("?Missing");
        }

        @Test
        void shouldDistinguishTypeVariablesByOwner() {
            // GIVEN a type parameter and a method type parameter with the same name
            final LinkedModel model = link("namespace a\nG<$$T> {\n@@immutable v: $$T\n@@finalMethod $$T m<$$T>(x: $$T) }");
            final TypeDefinition g = type(model, "a", "G");

            // WHEN
            final Type fieldVariable = g.fields().getFirst().type();
            final MethodDefinition method = g.methods().getFirst();

            // THEN
            assertThat(fieldVariable).isEqualTo(new Type.TypeVariable("$$T", "a.G"));
            assertThat(method.returnType()).isEqualTo(new Type.TypeVariable("$$T", "a.G#m($$T)"));
            assertThat(method.returnType()).isNotEqualTo(fieldVariable);
            assertThat(method.typeParameters()).extracting(TypeParameterDefinition::name).containsExactly("$$T");
        }

        @Test
        void shouldLinkFunctionsConstantsAndBounds() {
            // GIVEN
            final LinkedModel model = link("""
                    namespace a
                    abstraction R {}
                    Box<$$T extends R> {}
                    constant MAX: int32 = 1
                    @@static Box<$$X> load<$$X extends R>(kind: type<Box<$$X>>)
                    """);

            // THEN
            assertThat(model.constants()).singleElement().satisfies(c -> {
                assertThat(c.name()).isEqualTo(new QualifiedName("a", "MAX"));
                assertThat(c.type().text()).isEqualTo("int32");
                assertThat(c.value()).isInstanceOf(Literal.NumberLiteral.class);
            });
            final MethodDefinition load = model.functions().getFirst().method();
            assertThat(model.functions().getFirst().namespace()).isEqualTo("a");
            assertThat(load.declaringType()).isNull();
            assertThat(load.returnType().text()).isEqualTo("a.Box<$$X>");
            assertThat(load.typeParameters().getFirst().bound()).isEqualTo(
                    new Type.DeclaredType(new QualifiedName("a", "R"), List.of()));
            assertThat(type(model, "a", "Box").typeParameters().getFirst().bound().text()).isEqualTo("a.R");
        }
    }

    @Nested
    class EffectiveMembers {

        @Test
        void shouldInheritFieldsWithSubstitutedTypeArgumentsAcrossSeveralLevels() {
            // GIVEN
            final LinkedModel model = link("""
                    namespace a
                    abstraction P<$$T> { @@immutable @@nullable value: $$T
                                         @@immutable id: int8 }
                    abstraction Q<$$U> extends P<list<$$U>> { @@immutable extra: $$U }
                    C extends Q<string> { @@immutable own: bool }
                    """);

            // WHEN
            final TypeDefinition c = type(model, "a", "C");

            // THEN inherited first (in extends order), then own; declaring types are kept
            assertThat(fieldTypes(c)).containsExactly("value: list<string>", "id: int8", "extra: string", "own: bool");
            assertThat(c.fields()).extracting(f -> f.declaringType().name()).containsExactly("P", "P", "Q", "C");
            assertThat(((TypeDefinition.ComplexTypeDefinition) c).declaredFields()).extracting(FieldDefinition::name)
                    .containsExactly("own");
        }

        @Test
        void shouldReplaceOverriddenFieldsInPlace() {
            final LinkedModel model = link("""
                    namespace a
                    abstraction P { @@immutable @@nullable a: int8
                                    @@immutable b: int8 }
                    C extends P { @@immutable c: int8
                                  @@immutable @@override a: int8 }
                    """);
            final TypeDefinition c = type(model, "a", "C");
            assertThat(c.fields()).extracting(FieldDefinition::name).containsExactly("a", "b", "c");
            assertThat(c.field("a").orElseThrow().declaringType().name()).isEqualTo("C");
            assertThat(model.inheritedField(c.name(), "a").orElseThrow().declaringType().name()).isEqualTo("P");
            assertThat(model.inheritedField(c.name(), "c")).isEmpty();
        }

        @Test
        void shouldMergeDiamondInheritanceOnce() {
            final LinkedModel model = link("""
                    namespace a
                    abstraction Root { @@immutable id: int8
                                       void ping() }
                    abstraction Left extends Root {}
                    abstraction Right extends Root {}
                    Both extends Left, Right {}
                    """);
            final TypeDefinition both = type(model, "a", "Both");
            assertThat(both.fields()).extracting(FieldDefinition::name).containsExactly("id");
            assertThat(both.methods()).extracting(MethodDefinition::name).containsExactly("ping");
        }

        @Test
        void shouldInheritMethodsWithSubstitutionButNotStaticOnes() {
            // GIVEN
            final LinkedModel model = link("""
                    namespace a
                    abstraction Token<$$Self extends Token<$$Self, $$Unit>, $$Unit> {
                        $$Self to(target: $$Unit)
                        @@finalMethod $$X convert<$$X>(value: $$X, unit: $$Unit)
                        @@static Token<ANY, ANY> parse(text: string)
                        void plain()
                    }
                    enum Unit { A }
                    Coin extends Token<Coin, Unit> {
                        void plain()
                        int64 extra()
                    }
                    """);

            // WHEN
            final TypeDefinition coin = type(model, "a", "Coin");

            // THEN
            assertThat(coin.methods()).extracting(m -> m.returnType().text() + " " + m.signature()).containsExactly(
                    "a.Coin to(a.Unit)",
                    "$$X convert($$X,a.Unit)",
                    "void plain()",
                    "int64 extra()");
            assertThat(coin.methods(("plain"))).singleElement()
                    .satisfies(m -> assertThat(m.declaringType().name()).isEqualTo("Coin"));
            assertThat(coin.methods("convert").getFirst().typeParameters()).hasSize(1);
            assertThat(type(model, "a", "Token").methods()).extracting(MethodDefinition::name)
                    .contains("parse");
        }

        @Test
        void shouldTolerateInheritanceCyclesAndRawOrInvalidSupertypes() {
            // GIVEN
            final LinkedModel model = link("""
                    namespace a
                    A extends B { @@immutable a: int8 }
                    B extends A { @@immutable b: int8 }
                    abstraction G<$$T> { @@immutable g: $$T }
                    Raw extends G {}
                    enum E { V }
                    ExtendsEnum extends E, string, Missing {}
                    """);

            // THEN no endless recursion; raw supertypes keep their type variables
            assertThat(type(model, "a", "A").fields()).extracting(FieldDefinition::name).contains("a");
            assertThat(fieldTypes(type(model, "a", "Raw"))).containsExactly("g: $$T");
            assertThat(type(model, "a", "ExtendsEnum").fields()).isEmpty();
        }
    }

    @Nested
    class Enums {

        @Test
        void shouldLinkAttributesValuesAndInheritedMethods() {
            // GIVEN
            final LinkedModel model = link("""
                    namespace a
                    abstraction Unit { @@immutable symbol: string
                                       string display() }
                    enum HbarUnit(symbol: string, @@min(1) factor: int64) extends Unit {
                        TINYBAR("tℏ", 1) // smallest unit
                        HBAR("ℏ", 100_000_000)
                        int64 toTinybars()
                    }
                    """);

            // WHEN
            final TypeDefinition.EnumDefinition unit = (TypeDefinition.EnumDefinition) type(model, "a", "HbarUnit");

            // THEN
            assertThat(unit.attributes()).extracting(p -> p.name() + ": " + p.type().text())
                    .containsExactly("symbol: string", "factor: int64");
            assertThat(unit.fields()).extracting(FieldDefinition::name).containsExactly("symbol", "factor");
            assertThat(unit.field("factor").orElseThrow().hasAnnotation("min")).isTrue();
            assertThat(unit.values()).extracting(EnumValueDefinition::name).containsExactly("TINYBAR", "HBAR");
            assertThat(unit.values().getFirst().arguments()).extracting(Literal::text)
                    .containsExactly("\"tℏ\"", "1");
            assertThat(unit.values().getFirst().documentation()).isEqualTo("smallest unit");
            assertThat(unit.methods()).extracting(MethodDefinition::name).containsExactly("display", "toTinybars");
            assertThat(unit.typeParameters()).isEmpty();
            assertThat(model.inheritedField(unit.name(), "symbol")).isPresent();
        }
    }

    @Nested
    class Namespaces {

        @Test
        void shouldDescribeNamespacesWithSourcesAndRequiredNamespaces() {
            // GIVEN namespace a is declared in two files, imports c (unused), uses b qualified and inherits from d
            final LinkedModel model = LinkedModel.of(new org.hiero.sdk.v3.metalang.MetaLang().validate(java.util.Map.of(
                    "f/a1.md", "## Description\n\nPart one.\n\n## API Schema\n```\nnamespace a\n"
                            + "requires {C} from c\nrequires {D} from d\nX extends D { @@immutable b: b.B }\n```\n",
                    "f/a2.md", "## API Schema\n```\nnamespace a\nY {}\n```\n",
                    "f/b.md", TestSpecs.markdown("namespace b\nB {}\n"),
                    "f/c.md", TestSpecs.markdown("namespace c\nC {}\n"),
                    "f/d.md", TestSpecs.markdown("namespace d\nrequires {E} from e\nabstraction D { @@immutable e: E }\n"),
                    "f/e.md", TestSpecs.markdown("namespace e\nE {}\n"))).model());

            // WHEN
            final NamespaceDefinition a = model.namespaces().stream().filter(n -> n.name().equals("a"))
                    .findFirst().orElseThrow();

            // THEN e is only reached through the inherited field of D and is therefore not required directly
            assertThat(model.namespaces()).extracting(NamespaceDefinition::name).containsExactly("a", "b", "c", "d", "e");
            assertThat(a.requiredNamespaces()).containsExactly("b", "c", "d");
            assertThat(a.sources()).containsExactly(new NamespaceDefinition.Source("f/a1.md", "Part one."),
                    new NamespaceDefinition.Source("f/a2.md", ""));
            assertThat(model.types("a")).extracting(t -> t.name().name()).containsExactly("X", "Y");
        }
    }

    @Nested
    class Api {

        private final LinkedModel model = link("""
                namespace a
                abstraction Root {}
                Mid extends Root {}
                Leaf extends Mid {}
                G<$$T> {}
                """, "namespace b\nOther {}");

        @Test
        void shouldListTypesInQualifiedNameOrder() {
            assertThat(model.types()).extracting(t -> t.name().toString())
                    .containsExactly("a.G", "a.Leaf", "a.Mid", "a.Root", "b.Other");
        }

        @Test
        void shouldAnswerSubtypeQueries() {
            final QualifiedName leaf = new QualifiedName("a", "Leaf");
            final QualifiedName root = new QualifiedName("a", "Root");
            assertThat(model.isSubtypeOf(leaf, root)).isTrue();
            assertThat(model.isSubtypeOf(leaf, leaf)).isTrue();
            assertThat(model.isSubtypeOf(root, leaf)).isFalse();
            assertThat(model.isSubtypeOf(new QualifiedName("x", "Unknown"), root)).isFalse();
        }

        @Test
        void shouldResolveDefinitionsOfDeclaredTypes() {
            final TypeDefinition g = type(model, "a", "G");
            assertThat(g.asType().text()).isEqualTo("a.G<$$T>");
            assertThat(model.definition(g.asType())).isSameAs(g);
            assertThatThrownBy(() -> model.definition(new Type.DeclaredType(new QualifiedName("x", "Y"), List.of())))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThat(new QualifiedName("a", "G")).isLessThan(new QualifiedName("b", "A"));
        }
    }
}
