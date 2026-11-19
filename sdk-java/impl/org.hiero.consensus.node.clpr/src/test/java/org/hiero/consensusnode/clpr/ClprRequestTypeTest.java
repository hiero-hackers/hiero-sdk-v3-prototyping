package org.hiero.consensusnode.clpr;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `ClprRequestType`, generated from the specs.
@DisplayName("ClprRequestType")
class ClprRequestTypeTest {

    @Test
    @DisplayName("the constants have the attribute values of the specs")
    @Tag("testId:consensusnode.clpr.ClprRequestType/constants")
    @Tag("hip:1535")
    void shouldProvideTheAttributesOfTheConstants() {
        assertValue(117, ClprRequestType.CLPR_UPDATE_LEDGER_CONFIGURATION.code());
        assertValue(118, ClprRequestType.CLPR_REGISTER_CHANNEL.code());
        assertValue(119, ClprRequestType.CLPR_COMPLETE_CHANNEL.code());
        assertValue(120, ClprRequestType.CLPR_CLOSE_CHANNEL.code());
        assertValue(121, ClprRequestType.CLPR_GET_LEDGER_CONFIGURATION.code());
        assertValue(122, ClprRequestType.CLPR_SUBMIT_BUNDLE.code());
        assertValue(123, ClprRequestType.CLPR_REDACT_MESSAGE.code());
        assertValue(124, ClprRequestType.CLPR_REGISTER_CONNECTOR.code());
        assertValue(125, ClprRequestType.CLPR_DEREGISTER_CONNECTOR.code());
        assertValue(126, ClprRequestType.CLPR_COMPLETE_CONNECTOR.code());
        assertValue(127, ClprRequestType.CLPR_ENDPOINT_PUBLICATION.code());
        assertValue(128, ClprRequestType.CLPR_GET_ENDPOINT_MANIFEST.code());
    }
}
