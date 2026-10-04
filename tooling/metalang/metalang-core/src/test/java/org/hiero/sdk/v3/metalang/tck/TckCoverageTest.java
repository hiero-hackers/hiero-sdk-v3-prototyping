package org.hiero.sdk.v3.metalang.tck;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TckCoverageTest {

    @Test
    void methodsShouldBeReadFromBothHeadingForms() {
        // WHEN
        final List<TckSpecifications.Method> methods = TckSpecifications.parse("crypto/Order.md", """
                ## Method Name

                `createOrder`

                ### Input Parameters

                | Parameter Name          | Type   |
                |-------------------------|--------|
                | `customer`              | string |
                | commonTransactionParams | object |
                |                         |        |

                ### Output Parameters

                | Parameter Name | Type   |
                |----------------|--------|
                | orderId        | string |

                ### `getOrder`

                #### Input Parameters

                | Parameter Name | Type   |
                |----------------|--------|
                | id             | string |

                ### Method Name

                Not a name.
                """);

        // THEN
        assertThat(methods).containsExactly(
                new TckSpecifications.Method("createOrder", "crypto/Order.md",
                        List.of("customer", "commonTransactionParams"), List.of("orderId")),
                new TckSpecifications.Method("getOrder", "crypto/Order.md", List.of("id"), List.of()));
    }

    @Test
    void theCommonParametersShouldBeReadFromTheirTable() {
        // WHEN
        final List<TckSpecifications.Method> methods = TckSpecifications.parse(
                "common/CommonTransactionParameters.md", """
                        ### `ignored`

                        ## Parameter Object Definition

                        | Parameter Name | Type   |
                        |----------------|--------|
                        | memo           | string |
                        | signers        | list   |
                        """);

        // THEN
        assertThat(methods).containsExactly(new TckSpecifications.Method(TckSpecifications.COMMON,
                "common/CommonTransactionParameters.md", List.of("memo", "signers"), List.of()));
    }

    @Test
    void theFirstSpecificationOfAMethodShouldWin(@TempDir final Path directory) throws Exception {
        // GIVEN
        Files.createDirectories(directory.resolve("b"));
        Files.writeString(directory.resolve("a.md"), "### `getOrder`\n");
        Files.writeString(directory.resolve("b/a.md"), "### `getOrder`\n### `ping`\n");
        Files.writeString(directory.resolve("b/c.txt"), "### `other`\n");

        // WHEN
        final Map<String, TckSpecifications.Method> methods = TckSpecifications.read(directory);

        // THEN
        assertThat(methods.keySet()).containsExactly("getOrder", "ping");
        assertThat(methods.get("getOrder").file()).isEqualTo("a.md");
    }

    @Test
    void theCoverageShouldCompareParametersAndResults() {
        // GIVEN
        final TckBindings.Bindings bindings = TckFixtures.resolve("""
                binding createOrder -> OrderTransaction {
                    customer = customer : string
                    items = each lines.item -> Item {
                        name = name : string
                    }
                    unsupported discount "no discounts"
                    unsupported result total "no totals"
                    result orderId = receipt.orderId : string
                    result extra = receipt.count : int64
                }
                binding notInTck -> OrderQuery { id = id : string }
                common c -> OrderTransaction {
                    memo = memo : string
                }
                """);
        final Map<String, TckSpecifications.Method> methods = Map.of(
                "createOrder", new TckSpecifications.Method("createOrder", "o.md",
                        List.of("customer", "lines", "discount", "missingParameter", "commonTransactionParams"),
                        List.of("orderId", "total", "missingResult")),
                "setup", new TckSpecifications.Method("setup", "u.md", List.of("operatorAccountId"), List.of()),
                "getOrder", new TckSpecifications.Method("getOrder", "o.md", List.of("id"), List.of()),
                TckSpecifications.COMMON, new TckSpecifications.Method(TckSpecifications.COMMON, "c.md",
                        List.of("memo", "signers", "transactionId"), List.of()));

        // WHEN
        final TckCoverage.Report report = TckCoverage.check(bindings, methods);

        // THEN
        assertThat(report.bound()).containsExactly("createOrder", "setup");
        assertThat(report.unbound()).containsExactly("getOrder");
        assertThat(report.findings()).containsExactly(
                "Method notInTck (shop.md) does not exist in the TCK",
                "Parameter missingParameter of method createOrder has no binding",
                "Parameter transactionId of the common transaction parameters has no binding",
                "Result extra of method createOrder does not exist in the TCK",
                "Result missingResult of method createOrder has no binding");
    }

    @Test
    void unknownCommonParametersShouldBeReported() {
        // GIVEN
        final TckBindings.Bindings bindings = TckFixtures.resolve("""
                common c -> OrderTransaction {
                    memo = memo : string
                }
                """);

        // WHEN
        final TckCoverage.Report report = TckCoverage.check(bindings, Map.of(TckSpecifications.COMMON,
                new TckSpecifications.Method(TckSpecifications.COMMON, "c.md", List.of(), List.of())));

        // THEN
        assertThat(report.findings()).containsExactly(
                "Parameter memo of the common transaction parameters does not exist in the TCK");
        assertThat(TckCoverage.check(bindings, Map.of()).findings()).isEmpty();
    }
}
