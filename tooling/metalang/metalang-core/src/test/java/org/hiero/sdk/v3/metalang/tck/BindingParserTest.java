package org.hiero.sdk.v3.metalang.tck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.hiero.sdk.v3.metalang.diagnostic.Diagnostic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BindingParserTest {

    @Test
    void shouldParseAllMemberForms() {
        // WHEN
        final BindingParser.Result result = BindingParser.parse("shop.md", """
                Text before.

                ```java
                binding ignored -> Ignored { }
                ```

                ```bindings
                requires {A, B} from shop.orders

                // Creates an order.
                // Second line.
                binding createOrder -> shop.OrderTransaction {
                    customer = customer : string | ^name : string
                    items = each lines.item -> Item {
                        name = name : string
                    }
                    result order.id = receipt.orderId : string
                    unsupported memo "no memo"
                    unsupported result total "no total"
                }

                common commonTransactionParams -> Transaction {
                    // a comment inside
                    memo = memo : string
                }
                ```
                """);

        // THEN
        assertThat(result.diagnostics()).isEmpty();
        assertThat(result.requires()).singleElement().satisfies(r -> {
            assertThat(r.names()).containsExactly("A", "B");
            assertThat(r.namespace()).isEqualTo("shop.orders");
        });
        assertThat(result.bindings()).hasSize(2);
        final Binding binding = result.bindings().getFirst();
        assertThat(binding.common()).isFalse();
        assertThat(binding.name()).isEqualTo("createOrder");
        assertThat(binding.type()).isEqualTo("shop.OrderTransaction");
        assertThat(binding.documentation()).isEqualTo("Creates an order.\nSecond line.");
        assertThat(binding.location().line()).isEqualTo(12);
        assertThat(binding.members()).hasSize(5);
        final Binding.Assignment customer = (Binding.Assignment) binding.members().getFirst();
        assertThat(customer.sources()).extracting(s -> s.path().toString())
                .containsExactly("customer", "^name");
        assertThat(customer.sources().get(1).path().parent()).isTrue();
        final Binding.Each each = (Binding.Each) binding.members().get(1);
        final Binding.Path source = each.generators().getFirst().path();
        assertThat(each.generators().getFirst().name()).isNull();
        assertThat(source.first()).isEqualTo("lines");
        assertThat(source.last()).isEqualTo("item");
        assertThat(source.rest().toString()).isEqualTo("item");
        assertThat(each.type()).isEqualTo("Item");
        assertThat(each.members()).hasSize(1);
        final Binding.Result order = (Binding.Result) binding.members().get(2);
        assertThat(order.name().segments()).containsExactly("order", "id");
        assertThat(order.converter()).isEqualTo("string");
        assertThat(binding.members().get(3)).isEqualTo(new Binding.Unsupported(false, "memo", "no memo",
                binding.members().get(3).location()));
        assertThat(((Binding.Unsupported) binding.members().get(4)).result()).isTrue();
        final Binding common = result.bindings().get(1);
        assertThat(common.common()).isTrue();
        assertThat(common.documentation()).isEmpty();
    }

    @Test
    void shouldParseVariantsNestedObjectsRequestsGeneratorsAndConstants() {
        // WHEN
        final BindingParser.Result result = BindingParser.parse("shop.md", """
                ```bindings
                // Mints.
                binding mint
                    when metadata -> A {
                        x = "ft" : string | y : string
                        result r = receipt : string
                    }
                    when serials -> B { }
                    else -> C {
                        grant = when serials -> G { } else -> H { }
                        endpoint = proxy -> E { }
                        plain = -> E { }
                        inner = request scheduled
                        list = each pending, serial in serialNumbers?, token in tokens -> P { }
                    }
                binding single when a -> A { }
                ```
                """);

        // THEN
        assertThat(result.diagnostics()).isEmpty();
        assertThat(result.bindings()).extracting(Binding::name).containsExactly("mint", "mint", "mint", "single");
        assertThat(result.bindings()).extracting(b -> String.valueOf(b.when()))
                .containsExactly("metadata", "serials", "null", "a");
        assertThat(result.bindings()).extracting(Binding::type).containsExactly("A", "B", "C", "A");
        assertThat(result.bindings()).extracting(Binding::documentation).containsExactly("Mints.", "Mints.",
                "Mints.", "");
        final Binding.Assignment x = (Binding.Assignment) result.bindings().getFirst().members().getFirst();
        assertThat(x.sources().getFirst().literal()).isEqualTo("ft");
        assertThat(x.sources().getFirst().path()).isNull();
        assertThat(x.sources()).extracting(Binding.Source::toString).containsExactly("\"ft\"", "y");
        assertThat(((Binding.Result) result.bindings().getFirst().members().get(1)).value().segments())
                .containsExactly("receipt");
        final List<Binding.Member> members = result.bindings().get(2).members();
        final Binding.Construct grant = (Binding.Construct) members.getFirst();
        assertThat(grant.path()).isNull();
        assertThat(grant.variants()).extracting(v -> String.valueOf(v.when()), Binding.Variant::type)
                .containsExactly(org.assertj.core.groups.Tuple.tuple("serials", "G"),
                        org.assertj.core.groups.Tuple.tuple("null", "H"));
        assertThat(((Binding.Construct) members.get(1)).path()).hasToString("proxy");
        assertThat(((Binding.Construct) members.get(2)).path()).isNull();
        assertThat(((Binding.Request) members.get(3)).path()).hasToString("scheduled");
        final Binding.Each list = (Binding.Each) members.get(4);
        assertThat(list.generators()).containsExactly(
                new Binding.Generator(null, new Binding.Path(false, List.of("pending")), false),
                new Binding.Generator("serial", new Binding.Path(false, List.of("serialNumbers")), true),
                new Binding.Generator("token", new Binding.Path(false, List.of("tokens")), false));
    }

    @Test
    void undocumentedResultsAndAcceptedValuesShouldBeParsed() {
        // WHEN
        final BindingParser.Result result = BindingParser.parse("shop.md", """
                ```bindings
                binding createOrder -> shop.OrderTransaction {
                    undocumented result transactionId = receipt.transactionId : transactionId
                    result orderId = receipt.orderId : string
                    unsupported validate "no validation" unless false
                    unsupported currency "no currency" unless "EUR"
                    unsupported memo "no memo"
                }
                ```
                """);

        // THEN
        assertThat(result.diagnostics()).isEmpty();
        final List<Binding.Member> members = result.bindings().getFirst().members();
        assertThat(((Binding.Result) members.get(0)).undocumented()).isTrue();
        assertThat(((Binding.Result) members.get(1)).undocumented()).isFalse();
        assertThat(((Binding.Unsupported) members.get(2)).unless()).isEqualTo(Boolean.FALSE);
        assertThat(((Binding.Unsupported) members.get(3)).unless()).isEqualTo("EUR");
        assertThat(((Binding.Unsupported) members.get(4)).unless()).isNull();
    }

    @Test
    void sourcesAreEitherPathsOrConstants() {
        assertThatThrownBy(() -> new Binding.Source(null, null, "string"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Binding.Each("a", List.of(), "T", List.of(),
                new org.hiero.sdk.v3.metalang.diagnostic.SourceLocation("f", 1, 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void attributesMayBeNamedLikeKeywords() {
        // WHEN
        final BindingParser.Result result = BindingParser.parse("shop.md", """
                ```bindings
                binding b -> T {
                    result = result : string
                    unsupported = unsupported : string
                    each = each : string
                    when = when : string
                    request = request : string
                    unsupported result "reason"
                }
                ```
                """);

        // THEN
        assertThat(result.diagnostics()).isEmpty();
        assertThat(result.bindings().getFirst().members()).hasSize(6);
        assertThat(result.bindings().getFirst().members().get(5))
                .isEqualTo(new Binding.Unsupported(false, "result", "reason",
                        result.bindings().getFirst().members().get(5).location()));
    }

    @ParameterizedTest
    @CsvSource(delimiter = ';', value = {
            "foo;expected 'requires', 'binding' or 'common', found 'foo'",
            "requires A from b;expected '{', found 'A'",
            "requires {A} b;expected 'from', found 'b'",
            "requires {A} from;expected a name, found 'end of block'",
            "binding b T {};expected '->', found 'T'",
            "binding b -> T { x = y };expected ':', found '}'",
            "binding b -> T { unsupported x y };expected the reason as string, found 'y'",
            "binding b -> T { x = \"y\" };expected ':', found '}'",
            "binding b when -> T { };expected a name, found '->'",
            "binding b when a -> T { } else T { };expected '->', found 'T'",
            "binding b -> T { x = each a, b in -> T { } };expected a name, found '->'",
            "binding b -> T { x = y : string \"oops };expected a name, found 'unterminated string'",
    })
    void syntaxErrorsShouldBeReported(final String block, final String message) {
        // WHEN
        final List<Diagnostic> diagnostics = BindingParser.parse("shop.md", "```bindings\n" + block + "\n```\n")
                .diagnostics();

        // THEN
        assertThat(diagnostics).singleElement().satisfies(d -> {
            assertThat(d.ruleId()).isEqualTo("tck.syntax");
            assertThat(d.message()).isEqualTo(message);
            assertThat(d.location().file()).isEqualTo("shop.md");
        });
    }

    @Test
    void aSyntaxErrorShouldOnlyEndItsBlock() {
        // WHEN
        final BindingParser.Result result = BindingParser.parse("shop.md", """
                ```bindings
                binding a -> T { x }
                ```

                ```bindings
                binding b -> T { }
                ```
                """);

        // THEN
        assertThat(result.diagnostics()).hasSize(1);
        assertThat(result.bindings()).extracting(Binding::name).containsExactly("b");
    }
}
