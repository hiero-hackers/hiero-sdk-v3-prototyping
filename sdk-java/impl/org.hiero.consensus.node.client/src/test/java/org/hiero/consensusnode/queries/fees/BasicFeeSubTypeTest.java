package org.hiero.consensusnode.queries.fees;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `BasicFeeSubType`, generated from the specs.
@DisplayName("BasicFeeSubType")
class BasicFeeSubTypeTest {

    @Test
    @DisplayName("the constants have the attribute values of the specs")
    @Tag("testId:consensusnode.queries.fees.BasicFeeSubType/constants")
    void shouldProvideTheAttributesOfTheConstants() {
        assertValue(0, BasicFeeSubType.DEFAULT.code());
        assertValue(1, BasicFeeSubType.TOKEN_FUNGIBLE_COMMON.code());
        assertValue(2, BasicFeeSubType.TOKEN_NON_FUNGIBLE_UNIQUE.code());
        assertValue(3, BasicFeeSubType.TOKEN_FUNGIBLE_COMMON_WITH_CUSTOM_FEES.code());
        assertValue(4, BasicFeeSubType.TOKEN_NON_FUNGIBLE_UNIQUE_WITH_CUSTOM_FEES.code());
        assertValue(5, BasicFeeSubType.SCHEDULE_CREATE_CONTRACT_CALL.code());
        assertValue(6, BasicFeeSubType.TOPIC_CREATE_WITH_CUSTOM_FEES.code());
        assertValue(7, BasicFeeSubType.SUBMIT_MESSAGE_WITH_CUSTOM_FEES.code());
        assertValue(8, BasicFeeSubType.CRYPTO_TRANSFER_WITH_HOOKS.code());
    }
}
