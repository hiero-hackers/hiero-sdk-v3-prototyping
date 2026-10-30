package org.hiero.mirrornode.schedule;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `ScheduleSignature`, generated from the specs.
@DisplayName("ScheduleSignature")
class ScheduleSignatureTest {

    @Test
    @DisplayName("creates a ScheduleSignature and returns the values")
    @Tag("testId:mirrornode.schedule.ScheduleSignature/create")
    void shouldCreateAndReturnTheValues() {
        final ZonedDateTime consensusTimestamp = consensusTimestampValue();
        final byte[] publicKeyPrefix = publicKeyPrefixValue();
        final byte[] signature = signatureValue();
        final SignatureType type = typeValue();
        final ScheduleSignature subject = new ScheduleSignature(consensusTimestamp, publicKeyPrefix, signature, type);
        assertValue(consensusTimestamp, subject.consensusTimestamp());
        assertValue(publicKeyPrefix, subject.publicKeyPrefix());
        assertValue(signature, subject.signature());
        assertValue(type, subject.type());
    }

    @Test
    @DisplayName("rejects null for consensusTimestamp")
    @Tag("testId:mirrornode.schedule.ScheduleSignature/consensusTimestamp/rejects-null")
    void shouldRejectNullConsensusTimestamp() {
        assertThrows(NullPointerException.class, () -> new ScheduleSignature((ZonedDateTime) null, publicKeyPrefixValue(), signatureValue(), typeValue()));
    }

    @Test
    @DisplayName("rejects null for publicKeyPrefix")
    @Tag("testId:mirrornode.schedule.ScheduleSignature/publicKeyPrefix/rejects-null")
    void shouldRejectNullPublicKeyPrefix() {
        assertThrows(NullPointerException.class, () -> new ScheduleSignature(consensusTimestampValue(), (byte[]) null, signatureValue(), typeValue()));
    }

    @Test
    @DisplayName("the constructor copies publicKeyPrefix; the accessor returns a copy")
    @Tag("testId:mirrornode.schedule.ScheduleSignature/publicKeyPrefix/copies-in")
    void shouldCopyPublicKeyPrefixIn() {
        final byte[] value = publicKeyPrefixValue();
        final byte[] expected = value.clone();
        final ScheduleSignature subject = new ScheduleSignature(consensusTimestampValue(), value, signatureValue(), typeValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.publicKeyPrefix());
        final byte[] returned = subject.publicKeyPrefix();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.publicKeyPrefix());
    }

    @Test
    @DisplayName("rejects null for signature")
    @Tag("testId:mirrornode.schedule.ScheduleSignature/signature/rejects-null")
    void shouldRejectNullSignature() {
        assertThrows(NullPointerException.class, () -> new ScheduleSignature(consensusTimestampValue(), publicKeyPrefixValue(), (byte[]) null, typeValue()));
    }

    @Test
    @DisplayName("the constructor copies signature; the accessor returns a copy")
    @Tag("testId:mirrornode.schedule.ScheduleSignature/signature/copies-in")
    void shouldCopySignatureIn() {
        final byte[] value = signatureValue();
        final byte[] expected = value.clone();
        final ScheduleSignature subject = new ScheduleSignature(consensusTimestampValue(), publicKeyPrefixValue(), value, typeValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.signature());
        final byte[] returned = subject.signature();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.signature());
    }

    @Test
    @DisplayName("rejects null for type")
    @Tag("testId:mirrornode.schedule.ScheduleSignature/type/rejects-null")
    void shouldRejectNullType() {
        assertThrows(NullPointerException.class, () -> new ScheduleSignature(consensusTimestampValue(), publicKeyPrefixValue(), signatureValue(), (SignatureType) null));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.schedule.ScheduleSignature/value-semantics")
    void shouldCompareByValue() {
        final ScheduleSignature subject = create();
        final ScheduleSignature other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static ZonedDateTime consensusTimestampValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static byte[] publicKeyPrefixValue() {
        return new byte[] {1, 2, 3};
    }

    private static byte[] signatureValue() {
        return new byte[] {1, 2, 3};
    }

    private static SignatureType typeValue() {
        return SignatureType.CONTRACT;
    }

    private static ScheduleSignature create() {
        return new ScheduleSignature(consensusTimestampValue(), publicKeyPrefixValue(), signatureValue(), typeValue());
    }
}
