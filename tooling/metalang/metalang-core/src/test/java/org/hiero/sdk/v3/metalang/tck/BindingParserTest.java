package org.hiero.sdk.v3.metalang.tck;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(each.source().first()).isEqualTo("lines");
        assertThat(each.source().last()).isEqualTo("item");
        assertThat(each.source().rest().toString()).isEqualTo("item");
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
    void attributesMayBeNamedLikeKeywords() {
        // WHEN
        final BindingParser.Result result = BindingParser.parse("shop.md", """
                ```bindings
                binding b -> T {
                    result = result : string
                    unsupported = unsupported : string
                    each = each : string
                    unsupported result "reason"
                }
                ```
                """);

        // THEN
        assertThat(result.diagnostics()).isEmpty();
        assertThat(result.bindings().getFirst().members()).hasSize(4);
        assertThat(result.bindings().getFirst().members().get(3))
                .isEqualTo(new Binding.Unsupported(false, "result", "reason",
                        result.bindings().getFirst().members().get(3).location()));
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
            "binding b -> T { x = \"y\" : string };expected a name, found 'y'",
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
