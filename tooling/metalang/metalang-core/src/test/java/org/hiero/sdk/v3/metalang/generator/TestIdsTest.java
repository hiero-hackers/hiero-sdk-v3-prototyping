package org.hiero.sdk.v3.metalang.generator;

import static org.assertj.core.api.Assertions.assertThat;

import org.hiero.sdk.v3.metalang.model.QualifiedName;
import org.junit.jupiter.api.Test;

class TestIdsTest {

    @Test
    void contractTestIdsShouldBeBuiltFromTheScopeTheElementAndTheCheck() {
        final String scope = TestIds.scope(new QualifiedName("ledger", "AccountId"));
        assertThat(scope).isEqualTo("ledger.AccountId");
        assertThat(TestIds.contract(scope, TestIds.CREATE)).isEqualTo("ledger.AccountId/create");
        assertThat(TestIds.contract(scope, "num", TestIds.accepts("AtMinimum")))
                .isEqualTo("ledger.AccountId/num/accepts-at-minimum");
        assertThat(TestIds.contract("keys", "createPrivateKey", "value", TestIds.rejects("a value below the minimum")))
                .isEqualTo("keys/createPrivateKey/value/rejects-a-value-below-the-minimum");
        assertThat(TestIds.tag("keys/a-b")).isEqualTo("testId:keys/a-b");
    }

    @Test
    void camelCaseAndTextShouldGiveTheSameKebabCase() {
        assertThat(TestIds.kebab("AtMaximumBytes")).isEqualTo("at-maximum-bytes");
        assertThat(TestIds.kebab("at maximum bytes")).isEqualTo("at-maximum-bytes");
        assertThat(TestIds.kebab("AboveMaximumSize2")).isEqualTo("above-maximum-size2");
        assertThat(TestIds.kebab("-NotMatchingThePattern-")).isEqualTo("not-matching-the-pattern");
    }
}
