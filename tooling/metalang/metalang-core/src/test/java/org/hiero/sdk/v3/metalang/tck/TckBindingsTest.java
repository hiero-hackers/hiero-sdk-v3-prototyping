package org.hiero.sdk.v3.metalang.tck;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.hiero.sdk.v3.metalang.diagnostic.Diagnostic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TckBindingsTest {

    @Test
    void shouldResolveTransactionsQueriesAndCommonParameters() {
        // WHEN
        final TckBindings.Bindings bindings = TckFixtures.resolve("""
                binding createOrder -> OrderTransaction {
                    customer = customer : string | buyer : string
                    items = each lines.item -> Item {
                        name = name : string
                        gift = ^gift : bool
                    }
                    unsupported discount "no discounts"
                    result orderId = receipt.orderId : string
                    result detail.note = receipt.detail.note : string
                }
                binding getOrder -> OrderQuery {
                    id = id : string
                    result note = response.note : string
                }
                common commonTransactionParams -> OrderTransaction {
                    memo = memo : string
                }
                """);

        // THEN
        assertThat(bindings.diagnostics()).isEmpty();
        assertThat(bindings.hasErrors()).isFalse();
        assertThat(bindings.bindings()).extracting(TckBindings.Resolved::kind)
                .containsExactly(TckBindings.Kind.TRANSACTION, TckBindings.Kind.QUERY);
        final TckBindings.Resolved create = bindings.bindings().getFirst();
        assertThat(create.file()).isEqualTo("shop.md");
        assertThat(create.resultType().name().name()).isEqualTo("OrderReceipt");
        assertThat(create.members()).hasSize(5);
        final TckBindings.Value customer = (TckBindings.Value) create.members().getFirst();
        assertThat(customer.sources()).extracting(TckBindings.Source::converter)
                .containsExactly(Converter.STRING, Converter.STRING);
        final TckBindings.Elements items = (TckBindings.Elements) create.members().get(1);
        assertThat(items.elementType().name().name()).isEqualTo("Item");
        assertThat(items.members()).hasSize(2);
        assertThat(create.members().get(2)).isInstanceOf(TckBindings.Unsupported.class);
        final TckBindings.Result detail = (TckBindings.Result) create.members().get(4);
        assertThat(detail.fields()).extracting(f -> f.name()).containsExactly("detail", "note");
        assertThat(bindings.bindings().get(1).resultType().name().name()).isEqualTo("Detail");
        assertThat(bindings.common()).hasValueSatisfying(c -> {
            assertThat(c.kind()).isEqualTo(TckBindings.Kind.COMMON);
            assertThat(c.resultType()).isNull();
        });
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', quoteCharacter = '"', value = {
            "binding a -> Unknown { }|tck.unknown-type|Type 'Unknown' is not imported or does not exist",
            "binding a -> shop.Unknown { }|tck.unknown-type|Type 'shop.Unknown' is not imported or does not exist",
            "binding a -> Plain { x = x : string }|tck.kind|Type 'Plain' is neither a transaction nor a query",
            "binding a -> OrderQuery { id = id : string  foo = foo : string }|tck.unknown-attribute|"
                    + "Type 'shop.OrderQuery' has no attribute 'foo'",
            "binding a -> OrderQuery { id = id : string  id = other : string }|tck.duplicate|"
                    + "Attribute 'id' is bound twice",
            "binding a -> OrderQuery { }|tck.required|Required attribute 'id' of 'shop.OrderQuery' gets no value",
            "binding a -> OrderQuery { id = ^id : string }|tck.parent|"
                    + "'^id' refers to the enclosing element, but 'id' is not inside 'each'",
            "binding a -> OrderQuery { id = id : nope }|tck.converter|Unknown converter 'nope'",
            "binding a -> OrderQuery { id = id : status }|tck.converter|"
                    + "Converter 'status' cannot convert JSON into a value",
            "binding a -> OrderQuery { id = id : bool }|tck.converter|Converter 'bool' is for bool, not for 'string'",
            "binding a -> OrderQuery { id = id : string  result x = response.note : evmAccountId }|tck.converter|"
                    + "Converter 'evmAccountId' cannot convert a value into JSON",
            "binding a -> OrderQuery { id = id : string  result x = receipt.note : string }|tck.result|"
                    + "A result of a query starts with 'response.', not 'receipt.note'",
            "binding a -> OrderQuery { id = id : string  result x = response : string }|tck.result|"
                    + "A result of a query starts with 'response.', not 'response'",
            "binding a -> OrderQuery { id = id : string  result x = ^response.note : string }|tck.result|"
                    + "A result of a query starts with 'response.', not '^response.note'",
            "binding a -> OrderQuery { id = id : string  result x = response.foo : string }|tck.unknown-attribute|"
                    + "Type 'shop.Detail' has no attribute 'foo'",
            "binding a -> OrderQuery { id = id : string  result x = response.note.size : string }|tck.result|"
                    + "'response.note.size' continues after a value that has no attributes",
            "binding a -> OrderQuery { id = id : string  result x = response.note : string  "
                    + "result x = response.note : string }|tck.duplicate|Result 'x' is bound twice",
            "common c -> OrderTransaction { customer = customer : string }|tck.immutable|"
                    + "Attribute 'customer' of 'shop.OrderTransaction' is immutable; the common transaction "
                    + "parameters can only set mutable attributes",
            "common c -> OrderTransaction { result x = receipt.orderId : string }|tck.result|"
                    + "The common transaction parameters have no result",
            "binding a -> OrderTransaction { customer = c : string  names = each n -> Item { } }|tck.converter|"
                    + "Attribute 'names' is no list of a declared type; 'each' needs one",
            "binding a -> OrderTransaction { customer = c : string  note = each n -> Item { } }|tck.converter|"
                    + "Attribute 'note' is no list of a declared type; 'each' needs one",
            "binding a -> OrderTransaction { customer = c : string  items = each n -> Plain { x = x : string } }|"
                    + "tck.converter|Attribute 'items' holds 'shop.Item', not 'shop.Plain'",
            "binding a -> OrderTransaction { customer = c : string  items = each n -> Missing { } }|"
                    + "tck.unknown-type|Type 'Missing' is not imported or does not exist",
            "binding a -> OrderTransaction { customer = c : string  items = each n -> Item { } }|"
                    + "tck.required|Required attribute 'name' of 'shop.Item' gets no value",
    })
    void problemsShouldBeReported(final String binding, final String rule, final String message) {
        // WHEN
        final TckBindings.Bindings bindings = TckFixtures.resolve(binding + "\n");

        // THEN
        assertThat(bindings.hasErrors()).isTrue();
        assertThat(bindings.diagnostics()).extracting(Diagnostic::ruleId, Diagnostic::message)
                .contains(org.assertj.core.groups.Tuple.tuple(rule, message));
    }

    @Test
    void duplicatesAcrossFilesAndUnknownImportsShouldBeReported() {
        // WHEN
        final TckBindings.Bindings bindings = TckBindings.resolve(Map.of(
                "a.md", TckFixtures.file("""
                        binding getOrder -> OrderQuery { id = id : string }
                        common c -> OrderTransaction { }
                        """),
                "b.md", """
                        ```bindings
                        requires {OrderQuery, Missing} from shop
                        requires {OrderTransaction} from shop
                        binding getOrder -> OrderQuery { id = id : string }
                        common c -> OrderTransaction { }
                        binding broken
                        ```
                        """), TckFixtures.MODEL);

        // THEN
        assertThat(bindings.diagnostics()).extracting(Diagnostic::message).containsExactlyInAnyOrder(
                "Type 'shop.Missing' does not exist",
                "The common transaction parameters are bound twice",
                "Method 'getOrder' is bound twice",
                "expected '->', found 'end of block'");
        assertThat(bindings.bindings()).hasSize(1);
    }

    @Test
    void shouldReadTheMarkdownFilesOfADirectory(@TempDir final Path directory) throws Exception {
        // GIVEN
        Files.createDirectories(directory.resolve("sub"));
        Files.writeString(directory.resolve("sub/shop.md"), TckFixtures.file("""
                binding getOrder -> OrderQuery { id = id : string }
                """));
        Files.writeString(directory.resolve("notes.txt"), "binding x -> Y { }");

        // WHEN
        final TckBindings.Bindings bindings = TckBindings.read(directory, TckFixtures.MODEL);

        // THEN
        assertThat(bindings.diagnostics()).isEmpty();
        assertThat(bindings.bindings()).singleElement().satisfies(b -> assertThat(b.file()).isEqualTo("sub/shop.md"));
    }
}
