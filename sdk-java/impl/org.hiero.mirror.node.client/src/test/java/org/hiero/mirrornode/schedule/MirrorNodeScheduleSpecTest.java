package org.hiero.mirrornode.schedule;

import static org.hiero.sdk.testing.Testing.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `mirror-node-schedule`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("mirror-node-schedule")
class MirrorNodeScheduleSpecTest {

    /// `mirrornode.schedule/a-schedule-signature-keeps-its-public-key-prefix-signature-and-type`
    ///
    /// Source: the examples of the ScheduleSignature schema of the mirror node OpenAPI definition (public key prefix
    /// "AAEBAwuqAwzB", signature "3q2+7wABAQMLqgMMwQ==", type ED25519).
    @Test
    @DisplayName("a schedule signature keeps its public key prefix, signature and type")
    @Tag("testId:mirrornode.schedule/a-schedule-signature-keeps-its-public-key-prefix-signature-and-type")
    void aScheduleSignatureKeepsItsPublicKeyPrefixSignatureAndType() {
        // given
        final byte[] prefix = base64("AAEBAwuqAwzB");
        final byte[] bytes = base64("3q2+7wABAQMLqgMMwQ==");

        // when
        final ScheduleSignature signature = new ScheduleSignature(ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), prefix, bytes, SignatureType.ED25519);

        // then
        assertEquals(prefix, signature.publicKeyPrefix());
        assertEquals(bytes, signature.signature());
        assertEquals(SignatureType.ED25519, signature.type());
    }

    /// `mirrornode.schedule/a-schedule-without-signatures-and-execution-has-an-empty-signature-list-and-no-execution-time`
    ///
    /// Source: the examples of the Schedule schema of the mirror node OpenAPI definition (memo "created on 02/10/2021",
    /// transaction body "Kd6tvu8=", not deleted, payer and creator "0.0.2"); this spec (`signatures` defaults to an empty
    /// list, a schedule that has not been executed has no execution time).
    @Test
    @DisplayName("a schedule without signatures and execution has an empty signature list and no execution time")
    @Tag("testId:mirrornode.schedule/a-schedule-without-signatures-and-execution-has-an-empty-signature-list-and-no-execution-time")
    void aScheduleWithoutSignaturesAndExecutionHasAnEmptySignatureListAndNoExecutionTime() {
        // given
        final AccountId account = AccountId.fromString("0.0.2");

        // when
        final Schedule schedule = new Schedule(null, ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), account, false, null, null, "created on 02/10/2021", account, Address.fromString("0.0.2"), List.of(), base64("Kd6tvu8="), false);

        // then
        assertEquals("created on 02/10/2021", schedule.memo());
        assertEquals(base64("Kd6tvu8="), schedule.transactionBody());
        assertEquals(account, schedule.payerAccountId());
        assertEquals(0L, length(schedule.signatures()));
        assertNull(schedule.executedTimestamp());
        assertNull(schedule.adminKey());
        assertFalse(schedule.deleted());
    }
}
