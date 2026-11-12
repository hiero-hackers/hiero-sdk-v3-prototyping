package org.hiero.blocknode;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `BasicBlockItemKind`, generated from the specs.
@DisplayName("BasicBlockItemKind")
class BasicBlockItemKindTest {

    @Test
    @DisplayName("the constants have the attribute values of the specs")
    @Tag("testId:blocknode.BasicBlockItemKind/constants")
    void shouldProvideTheAttributesOfTheConstants() {
        assertValue(1, BasicBlockItemKind.BLOCK_HEADER.code());
        assertValue(2, BasicBlockItemKind.EVENT_HEADER.code());
        assertValue(3, BasicBlockItemKind.ROUND_HEADER.code());
        assertValue(4, BasicBlockItemKind.SIGNED_TRANSACTION.code());
        assertValue(5, BasicBlockItemKind.TRANSACTION_RESULT.code());
        assertValue(6, BasicBlockItemKind.TRANSACTION_OUTPUT.code());
        assertValue(7, BasicBlockItemKind.STATE_CHANGES.code());
        assertValue(8, BasicBlockItemKind.FILTERED_SINGLE_ITEM.code());
        assertValue(9, BasicBlockItemKind.BLOCK_PROOF.code());
        assertValue(10, BasicBlockItemKind.RECORD_FILE.code());
        assertValue(11, BasicBlockItemKind.TRACE_DATA.code());
        assertValue(12, BasicBlockItemKind.BLOCK_FOOTER.code());
        assertValue(19, BasicBlockItemKind.REDACTED_ITEM.code());
    }
}
