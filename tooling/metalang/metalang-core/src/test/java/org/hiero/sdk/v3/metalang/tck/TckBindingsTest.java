package org.hiero.sdk.v3.metalang.tck;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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

    @Test
    void shouldResolveVariantsNestedObjectsRequestsListsAndSetters() {
        // WHEN
        final TckBindings.Bindings bindings = TckFixtures.resolve("""
                binding createOrder
                    when detail -> OrderTransaction {
                        customer = customer : string | "anonymous" : string
                        tags     = tags : string
                        ids      = ids : int64
                        grant    = when serials -> SerialGrant { serials = serials : int64 }
                                   else -> AllGrant { all = all : bool }
                        detail   = detail -> Detail { note = note : string | ^fallback : string }
                        inner    = request inner
                        items    = each lines, item in names? -> Item {
                            name = item : string
                            gift = ^gift : bool
                        }
                        result orderId = receipt.orderId : string
                        result serials = receipt.serials : int64
                    }
                    else -> OrderTransaction {
                        customer = customer : string
                    }
                binding getOrder -> OrderQuery {
                    id         = id : string
                    maxPayment = maxPayment : int64
                }
                """);

        // THEN
        assertThat(bindings.diagnostics()).isEmpty();
        assertThat(bindings.bindings()).extracting(b -> b.binding().name())
                .containsExactly("createOrder", "createOrder", "getOrder");
        final List<TckBindings.Member> members = bindings.bindings().getFirst().members();
        final TckBindings.Value customer = (TckBindings.Value) members.getFirst();
        assertThat(customer.sources()).extracting(TckBindings.Source::literal).containsExactly(null, "anonymous");
        assertThat(customer.collection()).isEqualTo(TckBindings.Collection.NONE);
        assertThat(customer.setter()).isFalse();
        assertThat(((TckBindings.Value) members.get(1)).collection()).isEqualTo(TckBindings.Collection.SET);
        assertThat(((TckBindings.Value) members.get(2)).collection()).isEqualTo(TckBindings.Collection.LIST);
        final TckBindings.Nested grant = (TckBindings.Nested) members.get(3);
        assertThat(grant.path()).isNull();
        assertThat(grant.variants()).extracting(v -> v.type().name().name())
                .containsExactly("SerialGrant", "AllGrant");
        assertThat(((TckBindings.Nested) members.get(4)).path()).hasToString("detail");
        assertThat(((TckBindings.Request) members.get(5)).path()).hasToString("inner");
        assertThat(((TckBindings.Elements) members.get(6)).generators()).hasSize(2);
        assertThat(((TckBindings.Result) members.get(8)).collection()).isEqualTo(TckBindings.Collection.LIST);
        final TckBindings.Value payment = (TckBindings.Value) bindings.bindings().get(2).members().get(1);
        assertThat(payment.setter()).isTrue();
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
                    + "A result of a query starts with 'response', not 'receipt.note'",
            "binding a -> OrderQuery { id = id : string  result x = response : string }|tck.converter|"
                    + "Converter 'string' is for string, not for 'shop.Detail'",
            "binding a -> OrderQuery { id = id : string  result x = ^response.note : string }|tck.result|"
                    + "A result of a query starts with 'response', not '^response.note'",
            "binding a -> OrderTransaction { customer = c : string  result x = receipt.serials : string }|"
                    + "tck.converter|Converter 'string' is for string, not for 'int64'",
            "binding a when x -> OrderTransaction { customer = c : string } else -> OrderQuery { id = id : string }|"
                    + "tck.kind|The variants of method 'a' are not all transactions",
            "binding a -> OrderQuery { id = id : string } binding a when x -> OrderQuery { id = id : string }|"
                    + "tck.duplicate|Method 'a' is bound twice",
            "common c when x -> OrderTransaction { }|tck.syntax|The common transaction parameters have no variants",
            "binding a -> OrderTransaction { customer = request c }|tck.converter|"
                    + "Attribute 'customer' is no consensusnode.transactions.Transaction; 'request' builds one",
            "binding a -> OrderTransaction { customer = -> Item { name = n : string } }|tck.converter|"
                    + "Attribute 'customer' is no declared type; '->' creates one",
            "binding a -> OrderTransaction { customer = c : string  detail = -> Item { name = n : string } }|"
                    + "tck.converter|Attribute 'detail' holds 'shop.Detail', not 'shop.Item'",
            "binding a -> OrderTransaction { customer = c : string  detail = -> Missing { } }|"
                    + "tck.unknown-type|Type 'Missing' is not imported or does not exist",
            "binding a -> OrderTransaction { customer = c : string  detail = ^d -> Detail { note = n : string } }|"
                    + "tck.parent|'^d' refers to the enclosing element, but 'detail' is not inside 'each'",
            "binding a -> OrderTransaction { customer = c : string  "
                    + "detail = -> Detail { note = n : string  result x = receipt.orderId : string } }|"
                    + "tck.result|Only the binding itself has results",
            "binding a -> OrderTransaction { customer = c : string  items = each ^n -> Item { name = n : string } }|"
                    + "tck.parent|'^n' refers to the enclosing element, but 'items' is not inside 'each'",
            "binding a -> OrderTransaction { customer = c : string  "
                    + "items = each x in a, x in b -> Item { name = x : string } }|tck.duplicate|Name 'x' is bound twice",
            "binding a -> OrderTransaction { customer = c : string  detail = -> Detail { } }|tck.required|"
                    + "Required attribute 'note' of 'shop.Detail' gets no value",
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
