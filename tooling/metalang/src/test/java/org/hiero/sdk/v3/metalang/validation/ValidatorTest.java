package org.hiero.sdk.v3.metalang.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hiero.sdk.v3.metalang.TestSpecs.diagnostics;
import static org.hiero.sdk.v3.metalang.TestSpecs.ruleIds;

import java.util.List;
import org.hiero.sdk.v3.metalang.diagnostic.Diagnostic;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Rule-by-rule tests of the validator. Every rule has at least one test that triggers it and one that
 * shows a compliant variant does not.
 */
class ValidatorTest {

    /** Rule ids that are expected for a schema that is otherwise clean (mutable fields are INFO only). */
    private static List<String> rules(final String... schemas) {
        return ruleIds(schemas).stream().filter(r -> !r.equals("field.mutable")).toList();
    }

    @Test
    void shouldAcceptCleanSpec() {
        // GIVEN a spec that follows every guideline rule
        final String schema = """
                namespace orders
                requires {Address} from ledger

                constant MAX_ORDERS: int32 = 100

                // An order
                @@finalType
                Order extends Base<int64> {
                    @@immutable account: Address
                    @@immutable @@min(1) amount: int64
                    @@immutable @@maxLength(100) @@pattern("^[a-z]+$") note: string
                    @@immutable @@default([]) items: list<string>
                    @@immutable status: OrderStatus

                    @@async @@throws(not-found-error) @@nullable Details fetchDetails(apiKey: string)
                    @@streaming streamResult<Details> subscribe(@@nullable retry: Retry)
                    void addSigners(signers: Address...)
                }

                abstraction Base<$$T> {
                    @@immutable @@nullable value: $$T
                }

                Details {
                }

                Retry {
                    @@immutable @@default(3) maxAttempts: int32
                }

                enum OrderStatus {
                    PENDING
                    COMPLETED
                }
                """;
        final String ledger = """
                namespace ledger
                Address {
                    @@immutable num: int64
                }
                """;

        // WHEN
        final List<String> rules = ruleIds(schema, ledger);

        // THEN
        assertThat(rules).isEmpty();
    }

    @Nested
    class SyntaxVariants {

        @Test
        void shouldReportTypeKeyword() {
            assertThat(rules("namespace a\ntype Foo {}")).containsExactly("syntax.type-keyword");
        }


        @Test
        void shouldReportNonClassicMethodForms() {
            assertThat(rules("namespace a\nFoo { copy(): Foo\n reset() }"))
                    .containsExactlyInAnyOrder("syntax.trailing-return-type", "syntax.missing-return-type");
        }

        @Test
        void shouldReportNonClassicMethodFormsInEnums() {
            assertThat(rules("namespace a\nenum E { A\n label(): string }")).containsExactly("syntax.trailing-return-type");
        }

        @Test
        void shouldReportAnnotationInComment() {
            // GIVEN
            final String schema = """
                    namespace a
                    // @@sealed closes the set (prose mention, not reported)
                    // @@throws(not-found-error) if missing
                    Foo {
                        // @@deprecated
                    }
                    """;

            // THEN
            assertThat(rules(schema)).containsExactly("syntax.annotation-in-comment", "syntax.annotation-in-comment");
        }
    }

    @Nested
    class NamespaceFunctions {

        @Test
        void shouldRequireStatic() {
            // GIVEN
            final String schema = """
                    namespace a
                    Foo {}
                    @@static Foo create()
                    @@throws(parse-error)
                    Foo parse(value: string)
                    """;

            // THEN
            assertThat(diagnostics(schema)).extracting(Diagnostic::ruleId, Diagnostic::message).containsExactly(
                    org.assertj.core.groups.Tuple.tuple("function.not-static",
                            "Add @@static to namespace-level function 'parse'"));
        }

        @Test
        void shouldNotAllowInstanceOnlyAnnotations() {
            assertThat(rules("namespace a\n@@static @@streaming int8 f()\n@@static @@threadSafe int8 g()"))
                    .containsExactlyInAnyOrder("annotation.target", "method.streaming-static", "annotation.target");
        }

        @Test
        void shouldReportUseSiteBounds() {
            // GIVEN
            final String schema = """
                    namespace a
                    abstraction Receipt {}
                    Response<$$R extends Receipt> {}
                    @@static Response<$$R extends Receipt> load(id: string)
                    """;

            // THEN the use-site bound declares $$R for the function, so only the bound is reported
            assertThat(rules(schema)).containsExactly("syntax.use-site-bound");
        }
    }

    @Nested
    class Naming {

        @Test
        void shouldReportEveryNamingViolation() {
            // GIVEN
            final String schema = """
                    namespace my_ns
                    constant maxValue: int32 = 1
                    order<$$t> {
                        @@immutable Amount: int32
                        @@throws(NotFound) void Do_It(Param: string, cb: function<void On_Event()>)
                    }
                    enum Status { pending }
                    """;

            // THEN
            assertThat(rules(schema)).containsExactlyInAnyOrder("naming.namespace", "naming.constant",
                    "naming.type", "naming.generic", "naming.member", "naming.error-id", "naming.member",
                    "naming.member", "naming.member", "naming.enum-value");
        }

        @Test
        void shouldAcceptConformingNames() {
            assertThat(rules("namespace mirrornode.account\nconstant MAX_A1: int8 = 1\n"
                    + "enum Abc1 { VALUE_ONE }")).isEmpty();
        }
    }

    @Nested
    class Namespaces {

        @Test
        void shouldReportDuplicateDeclarationsAcrossFilesOfOneNamespace() {
            assertThat(rules("namespace a\nFoo {}", "namespace a\nFoo {}"))
                    .containsExactlyInAnyOrder("namespace.split", "namespace.duplicate-declaration");
        }

        @Test
        void shouldNotTreatOverloadedFunctionsAsDuplicateDeclarations() {
            assertThat(rules("namespace a\n@@static int32 f(a: int32)\n@@static int32 f(a: string)"))
                    .isEmpty();
        }

        @Test
        void shouldReportInvalidRequires() {
            // GIVEN
            final String schema = """
                    namespace a
                    requires {Foo} from missing
                    requires {Bar} from b
                    requires {Baz} from b
                    requires {Self} from a
                    Self {}
                    """;

            // THEN
            assertThat(rules(schema, "namespace b\nBaz {}")).containsExactlyInAnyOrder(
                    "requires.unknown-namespace", "requires.unknown-type", "requires.duplicate-namespace",
                    "requires.self", "requires.unused", "requires.unused", "requires.unused", "requires.unused");
        }

        @Test
        void shouldMentionWhereAnUnknownImportedTypeIsDeclared() {
            final List<Diagnostic> diagnostics = diagnostics("namespace a\nrequires {Baz} from b\nX { @@immutable z: Baz }",
                    "namespace b\nY {}", "namespace c\nBaz {}");
            assertThat(diagnostics).filteredOn(d -> d.ruleId().equals("requires.unknown-type"))
                    .extracting(Diagnostic::message)
                    .containsExactly("Namespace 'b' declares no type 'Baz' (declared in c)");
        }
    }

    @Nested
    class TypeReferences {

        @Test
        void shouldResolveLocalImportedWildcardAndQualifiedTypes() {
            // GIVEN
            final String schema = """
                    namespace a
                    requires {B} from b
                    requires {*} from c
                    X {
                        @@immutable b: B
                        @@immutable c: C
                        @@immutable d: d.D
                        @@immutable x: X
                    }
                    """;

            // THEN
            assertThat(rules(schema, "namespace b\nB {}", "namespace c\nC {}", "namespace d\nD {}")).isEmpty();
        }

        @Test
        void shouldReportUnknownAndNotImportedTypes() {
            // GIVEN
            final String schema = """
                    namespace a
                    X {
                        @@immutable a: Missing
                        @@immutable b: B
                        @@immutable c: int
                        @@immutable d: nowhere.D
                        @@immutable e: b.Missing
                        @@immutable f: timespan
                    }
                    """;

            // THEN
            final List<Diagnostic> diagnostics = diagnostics(schema, "namespace b\nB {}");
            assertThat(diagnostics).extracting(Diagnostic::ruleId).containsExactly(
                    "type.unknown", "type.not-imported", "type.unknown", "type.unknown", "type.unknown", "type.unknown");
            assertThat(diagnostics.get(1).message()).contains("requires {B} from b");
            assertThat(diagnostics.get(2).message()).contains("int32");
        }

        @Test
        void shouldReportImportedButUndeclaredType() {
            assertThat(diagnostics("namespace a\nrequires {B} from b\nX { @@immutable b: B }", "namespace b\nC {}"))
                    .extracting(Diagnostic::message)
                    .contains("Type 'B' is imported from 'b' but not declared there");
        }

        @Test
        void shouldReportAmbiguousImports() {
            assertThat(rules("namespace a\nrequires {*} from b\nrequires {*} from c\nX { @@immutable v: V }",
                    "namespace b\nV {}", "namespace c\nV {}")).containsExactly("type.ambiguous");
        }

        @Test
        void shouldReportUnnecessaryQualification() {
            assertThat(rules("namespace a\nrequires {B} from b\nX { @@immutable b: b.B\n@@immutable c: B }",
                    "namespace b\nB {}")).containsExactly("type.unnecessary-qualification");
        }

        @Test
        void shouldReportWrongArity() {
            // GIVEN
            final String schema = """
                    namespace a
                    G<$$T> {}
                    X {
                        @@immutable a: list
                        @@immutable b: map<string>
                        @@immutable c: string<int8>
                        @@immutable d: G
                        @@immutable e: X<int8>
                        @@immutable ok: G<map<string, list<int8>>>
                    }
                    """;

            // THEN
            assertThat(rules(schema)).containsExactly(
                    "type.arity", "type.arity", "type.arity", "type.arity", "type.arity");
        }

        @ParameterizedTest
        @ValueSource(strings = {"int4", "uint512", "int0"})
        void shouldReportInvalidIntegerWidth(final String type) {
            assertThat(rules("namespace a\nX { @@immutable v: " + type + " }")).containsExactly("type.int-width");
        }

        @ParameterizedTest
        @ValueSource(strings = {"int8", "uint256", "int64", "uint32"})
        void shouldAcceptValidIntegerWidth(final String type) {
            assertThat(rules("namespace a\nX { @@immutable v: " + type + " }")).isEmpty();
        }

        @Test
        void shouldReportStandaloneAnyButAcceptWildcard() {
            assertThat(rules("namespace a\nG<$$T> {}\nX { @@immutable a: ANY\n@@immutable b: G<ANY>\n"
                    + "@@immutable c: G<ANY extends X> }")).containsExactly("type.any-standalone");
        }

        @Test
        void shouldReportVoidOutsideReturnType() {
            assertThat(rules("namespace a\nX { @@immutable a: list<void>\nvoid m(p: void)\n"
                    + "void n(cb: function<void run()>) }")).containsExactly("type.unknown", "type.unknown");
        }

        @Test
        void shouldReportStreamResultOutsideStreamingReturn() {
            assertThat(rules("namespace a\nX { @@immutable a: streamResult<int8>\n"
                    + "streamResult<int8> m()\n@@streaming streamResult<int8> s()\n"
                    + "@@streaming list<streamResult<int8>> t() }"))
                    .containsExactly("type.stream-result-outside-streaming", "type.stream-result-outside-streaming",
                            "type.stream-result-outside-streaming");
        }

        @Test
        void shouldReportUndeclaredGenerics() {
            assertThat(rules("namespace a\nG<$$T> { @@immutable a: $$T\n@@immutable b: $$U\n"
                    + "$$V m(cb: function<$$T run()>)\n@@finalMethod $$W n<$$W>(v: $$T) }"))
                    .containsExactly("generic.undeclared", "generic.undeclared");
        }

        @Test
        void shouldAcceptTypedTypeTokens() {
            assertThat(rules("namespace a\nB {}\nX { void m(t: type, u: type<B>, v: type<B, B>) }"))
                    .containsExactly("type.arity");
        }

        @Test
        void shouldResolveTypesInsideFunctionTypes() {
            assertThat(rules("namespace a\nX { void m(cb: function<Missing run(v: Other)>) }"))
                    .containsExactly("type.unknown", "type.unknown");
        }

        @Test
        void shouldCountTypesUsedInSealedAndStructsAsImportUsages() {
            // GIVEN
            final String schema = """
                    namespace a
                    requires {Leaf} from b
                    requires {Address} from c
                    @@sealed(Leaf) abstraction Root {}
                    constant ZERO: Address = Address{num: 0}
                    """;

            // THEN no requires.unused (other findings are irrelevant here)
            assertThat(rules(schema, "namespace b\nLeaf {}", "namespace c\nAddress { @@immutable num: int64 }")).doesNotContain("requires.unused");
        }
    }

    @Nested
    class Annotations {

        @Test
        void shouldReportUnknownAnnotationsWithSuggestion() {
            // WHEN
            final List<Diagnostic> diagnostics = diagnostics(
                    "namespace a\nX { @@positiveValue @@immutable a: int32\n@@foo @@immutable b: int32 }");

            // THEN
            assertThat(diagnostics).extracting(Diagnostic::ruleId)
                    .containsExactly("annotation.unknown", "annotation.unknown");
            assertThat(diagnostics.getFirst().message()).contains("@@min(1)");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "@@async X {}",
                "X { @@static @@immutable a: int32 }",
                "X { @@immutable void m() }",
                "X { void m(@@deprecated a: int32) }",
                "@@immutable constant C: int32 = 1",
                "@@finalType enum E { A }",
                "enum E { @@nullable A }",
                "@@threadSafe @@static int32 f()"
        })
        void shouldReportAnnotationOnWrongTarget(final String declaration) {
            assertThat(rules("namespace a\n" + declaration)).contains("annotation.target");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "X { @@immutable @@min a: int32 }",
                "X { @@immutable @@min(\"1\") a: int32 }",
                "X { @@immutable @@maxLength(-1) a: string }",
                "X { @@immutable @@maxLength(1.5) a: string }",
                "X { @@immutable @@pattern(1) a: string }",
                "X { @@immutable @@pattern(\"[a-\") a: string }",
                "X { @@immutable @@default(1, 2) a: int32 }",
                "X { @@threadSafe(a, b) void m() }",
                "X { @@threadSafe(\"a\") void m() }",
                "X { @@throws void m() }",
                "X { @@throws(\"x\") void m() }",
                "@@oneOf(a) X { @@nullable a: int32 }",
                "@@oneOf(a, \"b\") X { @@nullable a: int32 }"
        })
        void shouldReportInvalidArguments(final String declaration) {
            assertThat(rules("namespace a\n" + declaration)).contains("annotation.arguments");
        }

        @Test
        void shouldWarnAboutEmptyParenthesesOnAnnotationsWithoutArguments() {
            // GIVEN
            final String schema = """
                    namespace a
                    X {
                        @@immutable() @@nullable() a: int32
                        @@threadSafe() void m()
                        @@threadSafe(group) @@async void n()
                    }
                    """;

            // THEN
            assertThat(diagnostics(schema)).extracting(Diagnostic::ruleId, Diagnostic::severity)
                    .containsOnly(org.assertj.core.groups.Tuple.tuple("annotation.empty-parentheses",
                            org.hiero.sdk.v3.metalang.diagnostic.Severity.WARNING))
                    .hasSize(3);
        }

        @Test
        void shouldReportEmptyParenthesesAsArgumentErrorWhereArgumentsAreRequired() {
            assertThat(rules("namespace a\nX { @@immutable @@min() a: int32 }")).containsExactly("annotation.arguments");
        }

        @Test
        void shouldReportDuplicateAnnotations() {
            assertThat(rules("namespace a\nX { @@immutable @@immutable a: int32 }"))
                    .containsExactly("annotation.duplicate");
        }

        @Test
        void shouldCheckValidationAnnotationsAgainstTypes() {
            // GIVEN
            final String schema = """
                    namespace a
                    X {
                        @@immutable @@min(1) a: string
                        @@immutable @@max(1) b: decimal
                        @@immutable @@min(1) c: seconds
                        @@immutable @@minLength(1) d: int32
                        @@immutable @@minSize(1) e: list<int32>
                        @@immutable @@maxSize(1) f: bytes
                        @@immutable @@pattern("x") g: int32
                        @@immutable @@urlPattern h: X
                        @@immutable @@min(1) i: Missing
                        void m(@@maxLength(2) name: string, @@minSize(1) values: string...)
                    }
                    """;

            // THEN
            // a (string), d (int32), g (int32), h (X) and i (unresolved type) do not fit their annotation
            assertThat(rules(schema)).containsExactly(
                    "annotation.value-type", "annotation.value-type", "annotation.value-type",
                    "annotation.value-type", "annotation.value-type", "type.unknown");
        }

        @Test
        void shouldAcceptSizeAnnotationsOnListsSetsMapsBytesAndVarargs() {
            // GIVEN
            final String schema = """
                    namespace a
                    X {
                        @@immutable @@minSize(1) @@maxSize(10) a: list<int8>
                        @@immutable @@maxSize(10) b: set<int8>
                        @@immutable @@minSize(0) c: map<string, int8>
                        @@immutable @@minSize(20) @@maxSize(20) d: bytes
                        void m(@@minSize(1) signers: string...)
                    }
                    """;

            // THEN
            assertThat(rules(schema)).isEmpty();
        }

        @Test
        void shouldSuggestTheMatchingAnnotationForWrongSizeOrLengthUsage() {
            // GIVEN
            final String schema = """
                    namespace a
                    X {
                        @@immutable @@minLength(1) a: list<int8>
                        @@immutable @@maxLength(4) b: bytes
                        @@immutable @@minSize(1) c: string
                        @@immutable @@maxSize(1) d: int32
                        void m(@@minLength(1) values: string...)
                    }
                    """;

            // WHEN
            final List<Diagnostic> diagnostics = diagnostics(schema);

            // THEN
            assertThat(diagnostics).extracting(Diagnostic::ruleId).containsOnly("annotation.value-type");
            assertThat(diagnostics).extracting(Diagnostic::message).containsExactly(
                    "@@minLength requires a string type, not 'list<int8>'; use @@minSize",
                    "@@maxLength requires a string type, not 'bytes'; use @@maxSize",
                    "@@minSize requires a list, set, map, bytes or varargs type, not 'string'; use @@minLength",
                    "@@maxSize requires a list, set, map, bytes or varargs type, not 'int32'",
                    "@@minLength requires a string type, not 'string...'; use @@minSize");
        }

        @Test
        void shouldAllowThreadSafeOnTypes() {
            // GIVEN
            final String schema = """
                    namespace a
                    @@threadSafe abstraction Session { void close() }
                    @@threadSafe(cache) Cache { void read() }
                    @@threadSafe enum Mode { A
                        int8 code() }
                    """;

            // THEN
            assertThat(rules(schema)).isEmpty();
        }

        @Test
        void shouldReportMethodLevelThreadSafeInsideThreadSafeType() {
            // GIVEN
            final String schema = """
                    namespace a
                    @@threadSafe abstraction Session {
                        @@threadSafe void close()
                        void open()
                    }
                    @@threadSafe enum Mode { A
                        @@threadSafe(x) int8 code() }
                    Plain { @@threadSafe void run() }
                    """;

            // THEN
            assertThat(rules(schema)).containsExactly("annotation.redundant-thread-safe",
                    "annotation.redundant-thread-safe");
        }

        @Test
        void shouldCheckDefaultValues() {
            // GIVEN
            final String schema = """
                    namespace a
                    enum Kind { A }
                    Point { @@immutable x: int32 }
                    X {
                        @@immutable @@default(0) i: int32
                        @@immutable @@default(-1) u: uint32
                        @@immutable @@default(1.5) f: int32
                        @@immutable @@default(1.5) d: double
                        @@immutable @@default("s") s: string
                        @@immutable @@default(1) s2: string
                        @@immutable @@default(true) b: bool
                        @@immutable @@default(yes) b2: bool
                        @@immutable @@default([]) l: list<int8>
                        @@immutable @@default([]) by: bytes
                        @@immutable @@default(A) k: Kind
                        @@immutable @@default(Kind.A) k2: Kind
                        @@immutable @@default(B) k3: Kind
                        @@immutable @@default(0) k4: Kind
                        @@immutable @@default(null) n: string
                        @@immutable @@nullable @@default(null) n2: string
                        @@immutable @@default(0) p: Point
                        @@immutable @@default(Point{x: 1}) p2: Point
                        @@immutable @@default(Kind{x: 1}) p3: Point
                        @@immutable @@default(Point{y: 1}) p4: Point
                        @@immutable @@default(Point{x: "1"}) p5: Point
                        @@immutable @@default(1) any: type
                        @@immutable @@default("2024-01-01") date: date
                        @@immutable @@default(5) dur: seconds
                        @@immutable @@default(500) ms: duration
                    }
                    """;

            // WHEN
            final List<Diagnostic> diagnostics = diagnostics(schema);

            // THEN
            assertThat(diagnostics).extracting(Diagnostic::ruleId).containsOnly("default.value-type");
            // schema line n is Markdown line n + 9; the findings are u, f, s2, b2, k3, k4, n, p, p3, p4, p5
            assertThat(diagnostics).extracting(d -> d.location().line() - 9).containsExactly(
                    6, 7, 10, 12, 17, 18, 19, 21, 23, 24, 25);
        }

        @Test
        void shouldCheckConstantValues() {
            // GIVEN
            final String schema = """
                    namespace a
                    G<$$T> { @@immutable v: $$T }
                    constant A: string = 1
                    constant B: string = "ok"
                    constant C: G<int8> = G{v: 1}
                    constant D: Missing = 1
                    """;

            // THEN
            assertThat(rules(schema)).containsExactly("constant.value-type", "type.unknown");
        }
    }

    @Nested
    class OneOf {

        @Test
        void shouldAcceptValidOneOf() {
            assertThat(rules("namespace a\n@@oneOf(a, b) X { @@immutable @@nullable a: int32\n"
                    + "@@immutable @@nullable @@default(1) b: int32 }")).isEmpty();
        }

        @Test
        void shouldReportOneOfViolations() {
            // GIVEN
            final String schema = """
                    namespace a
                    abstraction P { @@nullable inherited: int32 }
                    @@oneOrNoneOf(a, b, c, inherited, missing)
                    X extends P {
                        @@immutable a: int32
                        @@nullable @@default(1) b: int32
                        @@nullable @@default(2) c: int32
                    }
                    """;

            // THEN
            assertThat(rules(schema)).containsExactlyInAnyOrder("oneof.unknown-field", "oneof.not-nullable",
                    "oneof.mixed-immutability", "oneof.multiple-defaults");
        }
    }

    @Nested
    class Inheritance {

        @Test
        void shouldReportInvalidSupertypes() {
            // GIVEN
            final String schema = """
                    namespace a
                    enum E { A }
                    @@finalType F {}
                    C {}
                    abstraction G<$$T> extends $$T {}
                    X extends string, E, F, Missing {}
                    enum E2 extends C { A }
                    """;

            // THEN
            assertThat(rules(schema)).containsExactlyInAnyOrder("extends.invalid", "extends.multiple",
                    "extends.invalid", "extends.invalid", "final.extended", "type.unknown", "extends.invalid");
        }

        @Test
        void shouldReportFinalAbstraction() {
            assertThat(rules("namespace a\n@@finalType abstraction A {}")).containsExactly("final.on-abstraction");
        }

        @Test
        void shouldReportCycles() {
            assertThat(rules("namespace a\nA extends B {}\nB extends A {}\nC extends C {}\nD extends A {}"))
                    .containsExactly("extends.cycle", "extends.cycle", "extends.cycle");
        }

        @Test
        void shouldAcceptValidSealedHierarchy() {
            // GIVEN the sealed subtypes extend the abstraction; a subtype may live in the same namespace
            final String schema = """
                    namespace a
                    @@sealed(Circle, Square) abstraction Shape {}
                    @@finalType Circle extends Shape {}
                    @@finalType Square extends Shape {}
                    """;

            // THEN
            assertThat(rules(schema)).isEmpty();
        }

        @Test
        void shouldReportSealedViolations() {
            // GIVEN a subtype from another namespace may be listed if it is visible via requires
            final String schema = """
                    namespace a
                    requires {Remote} from b
                    @@sealed(Circle, Missing, Other, Remote) abstraction Shape {}
                    @@sealed(X) Concrete {}
                    Circle extends Shape {}
                    Other {}
                    Triangle extends Shape {}
                    """;

            // THEN
            assertThat(rules(schema, "namespace b\nrequires {Shape} from a\nRemote extends Shape {}"))
                    .containsExactlyInAnyOrder("sealed.unknown-subtype", "sealed.subtype-not-extending",
                            "sealed.not-abstraction", "sealed.unknown-subtype", "sealed.unlisted-subtype");
        }

        @Test
        void shouldAcceptValidOverride() {
            // GIVEN
            final String schema = """
                    namespace a
                    requires {Num} from b
                    W<$$T> {}
                    abstraction Identifier { @@immutable @@nullable num: Num
                                             @@immutable @@nullable wrapped: W<Num> }
                    @@finalType
                    NumericIdentifier extends Identifier { @@immutable @@override num: b.Num
                                                           @@immutable @@override wrapped: W<b.Num> }
                    """;

            // THEN
            assertThat(rules(schema, "namespace b\nNum {}"))
                    .containsExactly("type.unnecessary-qualification", "type.unnecessary-qualification");
        }

        @Test
        void shouldReportOverrideViolations() {
            // GIVEN
            final String schema = """
                    namespace a
                    abstraction G<$$T> { @@immutable @@nullable g: $$T }
                    abstraction P extends G<int8> {
                        @@immutable @@nullable a: int64
                        @@immutable @@nullable b: int64
                        @@nullable c: int64
                        @@immutable d: int64
                        @@immutable @@nullable e: W<int8>
                        @@immutable @@nullable f: Q
                    }
                    W<$$T> {}
                    abstraction Q {}
                    abstraction R {}
                    C extends P {
                        @@immutable @@override a: int32
                        @@immutable b: int64
                        @@immutable @@override c: int64
                        @@immutable @@override d: int64
                        @@immutable @@override e: W<int16>
                        @@immutable @@override f: R
                        @@immutable @@override g: int8
                        @@immutable @@override @@nullable x: int8
                    }
                    """;

            // THEN
            assertThat(rules(schema)).containsExactlyInAnyOrder("override.type-mismatch", "override.missing",
                    "override.immutability-mismatch", "override.not-narrowing", "override.type-mismatch",
                    "override.type-mismatch", "override.no-parent-field");
        }
    }

    @Nested
    class Members {

        @Test
        void shouldReportDuplicates() {
            // GIVEN
            final String schema = """
                    namespace a
                    X {
                        @@immutable a: int32
                        @@immutable a: int32
                        void m(p: int32, p: string)
                        int32 m(q: int32, r: string)
                        void m(q: int32)
                    }
                    @@static int32 f(a: int32)
                    @@static int64 f(b: int32)
                    """;

            // THEN
            assertThat(rules(schema)).containsExactlyInAnyOrder("member.duplicate-field",
                    "member.duplicate-parameter", "member.duplicate-method", "member.duplicate-method");
        }

        @Test
        void shouldReportVarargsViolations() {
            assertThat(rules("namespace a\nX { void a(x: int8..., y: int8)\nvoid b(x: int8..., y: int8...)\n"
                    + "void c(@@nullable x: int8...)\nvoid d(cb: function<void f(x: int8..., y: int8)>) }"))
                    .containsExactlyInAnyOrder("varargs.not-last", "varargs.not-last", "varargs.multiple",
                            "varargs.nullable", "varargs.not-last");
        }

        @Test
        void shouldReportStreamingCombinations() {
            assertThat(rules("namespace a\nX { @@async @@streaming int8 a()\n@@static @@streaming int8 b() }"))
                    .containsExactlyInAnyOrder("method.async-and-streaming", "method.streaming-static");
        }

        @Test
        void shouldReportNullableCollections() {
            assertThat(rules("namespace a\nX { @@immutable @@nullable a: list<int8>\n"
                    + "@@immutable @@nullable b: map<string, int8>\n@@immutable @@nullable c: string\n"
                    + "@@nullable set<int8> m(@@nullable p: list<int8>) }\n@@nullable @@static list<int8> f()"))
                    .containsExactly("collection.nullable", "collection.nullable", "collection.nullable",
                            "collection.nullable", "collection.nullable");
        }

        @Test
        void shouldReportMutableFieldsOfComplexTypesOnly() {
            assertThat(ruleIds("namespace a\nX { a: int32 }\nenum E { A\n@@immutable v: int8 }"))
                    .containsExactlyInAnyOrder("field.mutable", "enum.body-attribute");
        }
    }

    @Nested
    class GenericMethods {

        @Test
        void shouldAcceptStaticAndFinalGenericMethodsAndFunctions() {
            // GIVEN
            final String schema = """
                    namespace a
                    abstraction Receipt {}
                    abstraction Response<$$R extends Receipt> {}
                    abstraction Obj<$$T> {
                        @@finalMethod $$U convert<$$U>(x: $$T)
                        @@static Obj<$$V> of<$$V>(value: $$V)
                    }
                    @@static Response<$$R> load<$$R extends Receipt>(kind: type<Response<$$R>>)
                    """;

            // THEN
            assertThat(rules(schema)).isEmpty();
        }

        @Test
        void shouldRequireFinalMethodOnGenericInstanceMethods() {
            assertThat(diagnostics("namespace a\nabstraction Obj { $$U convert<$$U>(x: $$U) }"))
                    .extracting(Diagnostic::ruleId, Diagnostic::message)
                    .containsExactly(org.assertj.core.groups.Tuple.tuple("generic.method-not-final",
                            "Generic instance method 'convert' must be annotated with @@finalMethod (or be @@static)"));
        }

        @Test
        void shouldReportDuplicateAndShadowingTypeParameters() {
            // GIVEN
            final String schema = """
                    namespace a
                    G<$$T, $$T> {
                        @@finalMethod $$T m<$$T>()
                        @@static void n<$$U, $$U>()
                    }
                    @@static void f<$$A, $$A>()
                    """;

            // THEN
            assertThat(diagnostics(schema)).extracting(Diagnostic::ruleId).containsOnly("generic.duplicate").hasSize(4);
        }

        @Test
        void shouldReportUseSiteBoundsAsErrors() {
            // WHEN
            final List<Diagnostic> diagnostics = diagnostics(
                    "namespace a\nabstraction R {}\nG<$$T> {}\n@@static G<$$X extends R> f()");

            // THEN
            assertThat(diagnostics).extracting(Diagnostic::ruleId).containsExactly("syntax.use-site-bound");
            assertThat(diagnostics.getFirst().severity()).isEqualTo(org.hiero.sdk.v3.metalang.diagnostic.Severity.ERROR);
        }

        @Test
        void shouldCheckBoundsAndNamesOfMethodTypeParameters() {
            assertThat(rules("namespace a\n@@static void f<$$t extends Missing>()"))
                    .containsExactlyInAnyOrder("naming.generic", "type.unknown");
        }

        @Test
        void shouldReportOverriddenFinalMethods() {
            // GIVEN
            final String schema = """
                    namespace a
                    abstraction Base {
                        @@finalMethod int8 size()
                        int8 other()
                    }
                    abstraction Middle extends Base {}
                    Leaf extends Middle {
                        int8 size()
                        int8 other()
                    }
                    """;

            // THEN
            assertThat(diagnostics(schema)).extracting(Diagnostic::ruleId, Diagnostic::message).containsExactly(
                    org.assertj.core.groups.Tuple.tuple("method.final-overridden",
                            "'size()' is @@finalMethod in 'Base' and must not be re-declared"));
        }

        @Test
        void shouldForbidFinalMethodsFromTwoUnrelatedAbstractions() {
            // GIVEN A and B both declare @@finalMethod (classes in Java); C chains them, D mixes unrelated ones
            final String schema = """
                    namespace a
                    abstraction A { @@finalMethod int8 a() }
                    abstraction B { @@finalMethod int8 b() }
                    abstraction Chained extends A { @@finalMethod int8 c() }
                    abstraction Plain { int8 p() }
                    C extends Chained, A, Plain {}
                    D extends A, B {}
                    E extends D {}
                    """;

            // WHEN
            final List<Diagnostic> diagnostics = diagnostics(schema).stream()
                    .filter(d -> !d.ruleId().equals("extends.multiple"))
                    .toList();

            // THEN only D introduces the conflict
            assertThat(diagnostics).extracting(Diagnostic::ruleId, Diagnostic::message).containsExactly(
                    org.assertj.core.groups.Tuple.tuple("method.final-multiple-inheritance",
                            "'D' inherits @@finalMethod methods from both 'A' and 'B', which would require multiple"
                                    + " class inheritance"));
        }

        @Test
        void shouldReportRedundantFinalMethod() {
            // GIVEN
            final String schema = """
                    namespace a
                    abstraction A { @@static @@finalMethod int8 a() }
                    @@finalType F { @@finalMethod int8 b() }
                    enum E { V
                        @@finalMethod int8 c() }
                    """;

            // THEN
            assertThat(rules(schema)).containsExactly("method.final-redundant", "method.final-redundant",
                    "method.final-redundant");
        }

        @Test
        void shouldNotAllowFinalMethodOnFunctionsOrFields() {
            assertThat(rules("namespace a\n@@static @@finalMethod int8 f()\nX { @@finalMethod @@immutable a: int8 }"))
                    .containsExactly("annotation.target", "annotation.target");
        }
    }

    @Nested
    class ValuesAndConstraints {

        @Test
        void shouldCheckIntegerRanges() {
            // GIVEN
            final String schema = """
                    namespace a
                    constant OK_MIN: int8 = -128
                    constant OK_MAX: int8 = 127
                    constant TOO_SMALL: int8 = -129
                    constant TOO_BIG: int8 = 128
                    constant U_MAX: uint8 = 255
                    constant U_TOO_BIG: uint8 = 256
                    constant BIG: int256 = 100_000_000_000_000_000_000_000_000
                    enum E(a: int16) { V(40000) }
                    """;

            // WHEN
            final List<Diagnostic> diagnostics = diagnostics(schema);

            // THEN
            assertThat(diagnostics).extracting(Diagnostic::ruleId).containsExactly("constant.value-type",
                    "constant.value-type", "constant.value-type", "enum.argument-type");
            assertThat(diagnostics.getFirst().message()).isEqualTo("Value -129 is outside the range of 'int8' (-128..127)");
        }

        @Test
        void shouldReportContradictoryBoundsForFieldsParametersAndAttributes() {
            // GIVEN
            final String schema = """
                    namespace a
                    X {
                        @@immutable @@min(10) @@max(1) a: int32
                        @@immutable @@minLength(5) @@maxLength(2) b: string
                        @@immutable @@minSize(3) @@maxSize(1) c: list<int8>
                        @@immutable @@min(1) @@max(1) ok: int32
                        void m(@@min(2) @@max(1) p: int8)
                    }
                    enum E(@@min(3) @@max(2) v: int8) { A(3) }
                    """;

            // THEN
            assertThat(rules(schema)).containsOnly("annotation.contradictory-bounds", "value.constraint-violation")
                    .filteredOn("annotation.contradictory-bounds"::equals).hasSize(5);
        }

        @Test
        void shouldCheckDefaultsAndEnumArgumentsAgainstConstraints() {
            // GIVEN
            final String schema = """
                    namespace a
                    X {
                        @@immutable @@min(5) @@default(1) a: int32
                        @@immutable @@max(5) @@default(9) b: int32
                        @@immutable @@minLength(3) @@default("ab") c: string
                        @@immutable @@maxLength(2) @@default("tℏtℏ") d: string
                        @@immutable @@minSize(1) @@default([]) e: list<int8>
                        @@immutable @@pattern("^[0-9]+$") @@default("12a") f: string
                        @@immutable @@urlPattern @@default("/relative") g: string
                        @@immutable @@urlPattern @@default("https://example.com/x") ok1: string
                        @@immutable @@pattern("^[0-9]+$") @@default("123") ok2: string
                        @@immutable @@maxLength(2) @@default("tℏ") ok3: string
                        @@immutable @@nullable @@min(5) @@default(null) ok4: int32
                        @@immutable @@pattern("[") @@default("x") badRegex: string
                    }
                    enum E(@@maxLength(1) symbol: string) { A("x")
                     B("xy") }
                    """;

            // WHEN
            final List<Diagnostic> diagnostics = diagnostics(schema);

            // THEN 7 field defaults and one enum argument violate their constraints; the broken regex is only
            // reported once (as annotation.arguments)
            assertThat(diagnostics).extracting(Diagnostic::ruleId).containsExactlyInAnyOrder(
                    "value.constraint-violation", "value.constraint-violation", "value.constraint-violation",
                    "value.constraint-violation", "value.constraint-violation", "value.constraint-violation",
                    "value.constraint-violation", "value.constraint-violation", "annotation.arguments");
            assertThat(diagnostics).extracting(Diagnostic::message)
                    .contains("@@default of 'a': Value 1 violates @@min(5)",
                            "@@default of 'g': Value \"/relative\" is not an absolute URL (@@urlPattern)",
                            "'B', attribute 'symbol': Value \"xy\" violates @@maxLength(1)");
        }

        @Test
        void shouldCheckStructLiteralsForMissingFieldsAndAbstractions() {
            // GIVEN
            final String schema = """
                    namespace a
                    abstraction Base { @@immutable @@nullable id: int8 }
                    P extends Base { @@immutable x: int8
                                     @@immutable y: int8
                                     @@immutable @@default(0) z: int8
                                     @@immutable @@nullable w: int8 }
                    constant OK: P = P{x: 1, y: 2}
                    constant MISSING: P = P{x: 1}
                    constant ABSTRACT: Base = Base{}
                    """;

            // WHEN
            final List<Diagnostic> diagnostics = diagnostics(schema);

            // THEN
            assertThat(diagnostics).extracting(Diagnostic::message).containsExactly(
                    "Missing value for field 'y' of 'P'", "'Base' is an abstraction and has no literal form");
        }

        @Test
        void shouldReportDuplicateAnnotationArguments() {
            assertThat(diagnostics("namespace a\n@@sealed(Y, Y) abstraction X {}\nY extends X {}\n"
                    + "Z { @@throws(a-error, b-error, a-error) void m() }"))
                    .extracting(Diagnostic::message)
                    .containsExactly("@@sealed lists 'Y' twice", "@@throws lists 'a-error' twice");
        }
    }

    @Nested
    class GenericBounds {

        @Test
        void shouldAcceptArgumentsThatSatisfyBounds() {
            // GIVEN direct match, subtype, F-bounded self type, generic parameter, wildcard and unresolved argument
            final String schema = """
                    namespace a
                    abstraction B {}
                    abstraction Sub extends B {}
                    G<$$T extends B> {}
                    abstraction Self<$$S extends Self<$$S>> {}
                    Concrete extends Self<Concrete> {}
                    H<$$U extends B> { @@immutable g: G<$$U> }
                    X {
                        @@immutable a: G<B>
                        @@immutable b: G<Sub>
                        @@immutable c: G<ANY>
                        @@immutable d: G<ANY extends Sub>
                    }
                    """;

            // THEN
            assertThat(rules(schema)).isEmpty();
        }

        @Test
        void shouldReportArgumentsThatViolateBounds() {
            // GIVEN
            final String schema = """
                    namespace a
                    abstraction B {}
                    Other {}
                    G<$$T extends B> {}
                    abstraction Self<$$S extends Self<$$S>> {}
                    Wrong extends Self<Other> {}
                    X {
                        @@immutable a: G<Other>
                        @@immutable b: G<int8>
                        @@immutable c: list<G<string>>
                    }
                    """;

            // THEN
            assertThat(diagnostics(schema)).extracting(Diagnostic::message).containsExactly(
                    "'Other' does not satisfy '$$S extends Self<$$S>' of 'Self'",
                    "'Other' does not satisfy '$$T extends B' of 'G'",
                    "'int8' does not satisfy '$$T extends B' of 'G'",
                    "'string' does not satisfy '$$T extends B' of 'G'");
        }

        @Test
        void shouldSubstituteTypeArgumentsWhenComparingOverriddenFields() {
            // GIVEN P<$$T> -> Q<$$U> extends P<list<$$U>> -> concrete types binding $$U = int8
            final String schema = """
                    namespace a
                    abstraction P<$$T> { @@immutable @@nullable v: $$T }
                    abstraction Q<$$U> extends P<list<$$U>> {}
                    Ok extends Q<int8> { @@immutable @@override v: list<int8> }
                    Wrong extends Q<int8> { @@immutable @@override v: list<string> }
                    Direct extends P<int8> { @@immutable @@override v: string }
                    """;

            // WHEN
            final List<Diagnostic> diagnostics = diagnostics(schema);

            // THEN
            assertThat(diagnostics).extracting(Diagnostic::message).containsExactly(
                    "'list<string>' differs from the inherited type 'list<int8>' (P)",
                    "'string' differs from the inherited type 'int8' (P)");
        }

        @Test
        void shouldSubstituteTypeArgumentsForInheritedEnumAttributes() {
            final String schema = """
                    namespace a
                    abstraction Coded<$$C> { @@immutable code: $$C }
                    enum Ok(code: int32) extends Coded<int32> { A(1) }
                    enum Wrong(code: string) extends Coded<int32> { A("1") }
                    """;
            assertThat(diagnostics(schema)).extracting(Diagnostic::message)
                    .containsExactly("'string' differs from the inherited type 'int32' (Coded)");
        }
    }

    @Nested
    class Enums {

        @Test
        void shouldReportEnumViolations() {
            // GIVEN
            final String schema = """
                    namespace a
                    abstraction Unit { @@immutable symbol: string }
                    enum Empty {}
                    enum E extends Unit {
                        A
                        A
                        mutable: int8
                        list<E> values()
                    }
                    """;

            // THEN
            assertThat(rules(schema)).containsExactlyInAnyOrder("enum.empty", "enum.duplicate-value",
                    "enum.body-attribute", "enum.explicit-values-method", "enum.inherited-attribute-missing");
        }

        @Test
        void shouldAcceptEnumWithAttributeList() {
            // GIVEN
            final String schema = """
                    namespace a
                    abstraction Unit { @@immutable symbol: string }
                    enum Container { PKCS8, SPKI }
                    enum HbarUnit(symbol: string, @@min(1) factor: int64, container: Container,
                                  @@nullable @@maxLength(10) note: string) extends Unit {
                        TINYBAR("tℏ", 1, PKCS8, null)
                        HBAR("ℏ", 100_000_000, Container.SPKI, "main unit"),
                    }
                    """;

            // THEN
            assertThat(rules(schema)).isEmpty();
        }

        @Test
        void shouldReportWrongArgumentCountsAndTypes() {
            // GIVEN
            final String schema = """
                    namespace a
                    enum Plain { A("x") }
                    enum Unit(symbol: string, factor: int64) {
                        TOO_FEW("x")
                        NO_ARGS
                        WRONG_TYPES(1, "x")
                        NOT_NULLABLE(null, 1)
                    }
                    """;

            // WHEN
            final List<Diagnostic> diagnostics = diagnostics(schema);

            // THEN
            assertThat(diagnostics).extracting(Diagnostic::ruleId).containsExactly("enum.argument-count",
                    "enum.argument-count", "enum.argument-count", "enum.argument-type", "enum.argument-type",
                    "enum.argument-type");
            assertThat(diagnostics.get(3).message())
                    .isEqualTo("'WRONG_TYPES', attribute 'symbol': Value 1 is not a valid 'string'");
        }

        @Test
        void shouldReportInvalidAttributeLists() {
            // GIVEN
            final String schema = """
                    namespace a
                    abstraction Unit { @@immutable symbol: string }
                    enum Dup(a: int8, a: int8, b: int8...) { V(1, 1, 1) }
                    enum Mismatch(symbol: int32) extends Unit { V(1) }
                    enum Annotated(@@immutable Bad_Name: Missing) { V(1) }
                    """;

            // THEN
            assertThat(rules(schema)).containsExactlyInAnyOrder("enum.attribute-invalid", "enum.attribute-invalid",
                    "enum.attribute-type-mismatch", "annotation.target", "naming.member", "type.unknown");
        }

        @Test
        void shouldCountAttributeTypesAsImportUsages() {
            assertThat(rules("namespace a\nrequires {C} from b\nenum E(c: C) { V(X) }", "namespace b\nenum C { X }"))
                    .isEmpty();
        }

        @Test
        void shouldReportEnumWithOnlyACommentAsEmpty() {
            assertThat(rules("namespace a\nenum E { // not complete yet\n }")).containsExactly("enum.empty");
        }
    }
}
