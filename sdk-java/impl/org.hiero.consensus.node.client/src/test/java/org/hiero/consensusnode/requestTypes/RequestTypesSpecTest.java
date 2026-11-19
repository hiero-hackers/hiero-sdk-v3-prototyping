package org.hiero.consensusnode.requestTypes;

import static org.hiero.sdk.testing.Testing.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `request-types`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("request-types")
class RequestTypesSpecTest {

    /// `consensusnode.requestTypes/the-basic-request-types-have-the-codes-of-the-protocol`
    ///
    /// Source: HederaFunctionality of the HAPI (basic_types.proto).
    @Test
    @DisplayName("the basic request types have the codes of the protocol")
    @Tag("testId:consensusnode.requestTypes/the-basic-request-types-have-the-codes-of-the-protocol")
    void theBasicRequestTypesHaveTheCodesOfTheProtocol() {
        // when / then
        assertEquals(0, BasicRequestType.NONE.code());
        assertEquals(1, BasicRequestType.CRYPTO_TRANSFER.code());
        assertEquals(23, BasicRequestType.FILE_GET_CONTENTS.code());
        assertEquals(27, BasicRequestType.CRYPTO_CREATE.code());
        assertEquals(50, BasicRequestType.CONSENSUS_CREATE_TOPIC.code());
        assertEquals(56, BasicRequestType.TOKEN_CREATE.code());
        assertEquals(108, BasicRequestType.ATOMIC_BATCH.code());
        assertEquals(116, BasicRequestType.MIGRATION_ROOT_HASH_VOTE.code());
    }
}
