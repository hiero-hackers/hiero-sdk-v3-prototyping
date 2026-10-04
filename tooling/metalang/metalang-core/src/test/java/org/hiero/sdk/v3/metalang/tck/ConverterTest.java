package org.hiero.sdk.v3.metalang.tck;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ConverterTest {

    @Test
    void convertersShouldBeFoundByTheirId() {
        assertThat(Converter.of("accountId")).contains(Converter.ACCOUNT_ID);
        assertThat(Converter.of("ACCOUNT_ID")).isEmpty();
        assertThat(Converter.EVM_ACCOUNT_ID.inbound()).isTrue();
        assertThat(Converter.EVM_ACCOUNT_ID.outbound()).isFalse();
        assertThat(Converter.SECONDS.typeNames()).isEqualTo("duration or seconds");
    }

    @Test
    void convertersShouldAcceptTheirTypes() {
        final var order = TckFixtures.MODEL.type(new org.hiero.sdk.v3.metalang.model.QualifiedName("shop",
                "OrderTransaction")).orElseThrow();
        final var receipt = TckFixtures.MODEL.type(new org.hiero.sdk.v3.metalang.model.QualifiedName("shop",
                "OrderReceipt")).orElseThrow();
        assertThat(Converter.STRING.accepts(order.field("customer").orElseThrow().type())).isTrue();
        assertThat(Converter.INT64.accepts(order.field("customer").orElseThrow().type())).isFalse();
        assertThat(Converter.INT64.accepts(receipt.field("count").orElseThrow().type())).isTrue();
        assertThat(Converter.ADDRESS.accepts(receipt.field("detail").orElseThrow().type())).isFalse();
        assertThat(Converter.STRING.accepts(order.field("items").orElseThrow().type())).isFalse();
    }
}
