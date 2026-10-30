package org.hiero.mirrornode.account;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.Address;
import org.hiero.mirrornode.common.TimestampRange;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `Hook`, generated from the specs.
@DisplayName("Hook")
class HookTest {

    @Test
    @DisplayName("creates a Hook and returns the values")
    @Tag("testId:mirrornode.account.Hook/create")
    void shouldCreateAndReturnTheValues() {
        final @Nullable Authority adminKey = adminKeyValue();
        final Address contractId = contractIdValue();
        final @Nullable ZonedDateTime createdTimestamp = createdTimestampValue();
        final boolean deleted = deletedValue();
        final HookExtensionPoint extensionPoint = extensionPointValue();
        final long hookId = hookIdValue();
        final Address ownerId = ownerIdValue();
        final @Nullable TimestampRange timestampRange = timestampRangeValue();
        final HookType type = typeValue();
        final Hook subject = new Hook(adminKey, contractId, createdTimestamp, deleted, extensionPoint, hookId, ownerId, timestampRange, type);
        assertValue(adminKey, subject.adminKey());
        assertValue(contractId, subject.contractId());
        assertValue(createdTimestamp, subject.createdTimestamp());
        assertValue(deleted, subject.deleted());
        assertValue(extensionPoint, subject.extensionPoint());
        assertValue(hookId, subject.hookId());
        assertValue(ownerId, subject.ownerId());
        assertValue(timestampRange, subject.timestampRange());
        assertValue(type, subject.type());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.account.Hook/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final @Nullable Authority adminKey = adminKeyValue();
        final Address contractId = contractIdValue();
        final @Nullable ZonedDateTime createdTimestamp = createdTimestampValue();
        final boolean deleted = deletedValue();
        final HookExtensionPoint extensionPoint = extensionPointValue();
        final long hookId = hookIdValue();
        final Address ownerId = ownerIdValue();
        final @Nullable TimestampRange timestampRange = timestampRangeValue();
        final HookType type = typeValue();
        final Hook subject = Hook.builder()
                .adminKey(adminKey)
                .contractId(contractId)
                .createdTimestamp(createdTimestamp)
                .deleted(deleted)
                .extensionPoint(extensionPoint)
                .hookId(hookId)
                .ownerId(ownerId)
                .timestampRange(timestampRange)
                .type(type)
                .build();
        assertValue(adminKey, subject.adminKey());
        assertValue(contractId, subject.contractId());
        assertValue(createdTimestamp, subject.createdTimestamp());
        assertValue(deleted, subject.deleted());
        assertValue(extensionPoint, subject.extensionPoint());
        assertValue(hookId, subject.hookId());
        assertValue(ownerId, subject.ownerId());
        assertValue(timestampRange, subject.timestampRange());
        assertValue(type, subject.type());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.account.Hook/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final Hook subject = Hook.builder()
                .contractId(contractIdValue())
                .deleted(deletedValue())
                .extensionPoint(extensionPointValue())
                .hookId(hookIdValue())
                .ownerId(ownerIdValue())
                .type(typeValue())
                .build();
        assertNull(subject.adminKey());
        assertNull(subject.createdTimestamp());
        assertNull(subject.timestampRange());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.account.Hook/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> Hook.builder().build());
    }

    @Test
    @DisplayName("rejects null for contractId")
    @Tag("testId:mirrornode.account.Hook/contractId/rejects-null")
    void shouldRejectNullContractId() {
        assertThrows(NullPointerException.class, () -> new Hook(adminKeyValue(), (Address) null, createdTimestampValue(), deletedValue(), extensionPointValue(), hookIdValue(), ownerIdValue(), timestampRangeValue(), typeValue()));
    }

    @Test
    @DisplayName("rejects null for extensionPoint")
    @Tag("testId:mirrornode.account.Hook/extensionPoint/rejects-null")
    void shouldRejectNullExtensionPoint() {
        assertThrows(NullPointerException.class, () -> new Hook(adminKeyValue(), contractIdValue(), createdTimestampValue(), deletedValue(), (HookExtensionPoint) null, hookIdValue(), ownerIdValue(), timestampRangeValue(), typeValue()));
    }

    @Test
    @DisplayName("hookId: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.account.Hook/hookId/accepts-at-minimum")
    void shouldAcceptHookIdAtMinimum() {
        final long value = -9223372036854775808L;
        final Hook subject = new Hook(adminKeyValue(), contractIdValue(), createdTimestampValue(), deletedValue(), extensionPointValue(), value, ownerIdValue(), timestampRangeValue(), typeValue());
        assertValue(value, subject.hookId());
    }

    @Test
    @DisplayName("hookId: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.account.Hook/hookId/accepts-at-maximum")
    void shouldAcceptHookIdAtMaximum() {
        final long value = 9223372036854775807L;
        final Hook subject = new Hook(adminKeyValue(), contractIdValue(), createdTimestampValue(), deletedValue(), extensionPointValue(), value, ownerIdValue(), timestampRangeValue(), typeValue());
        assertValue(value, subject.hookId());
    }

    @Test
    @DisplayName("rejects null for ownerId")
    @Tag("testId:mirrornode.account.Hook/ownerId/rejects-null")
    void shouldRejectNullOwnerId() {
        assertThrows(NullPointerException.class, () -> new Hook(adminKeyValue(), contractIdValue(), createdTimestampValue(), deletedValue(), extensionPointValue(), hookIdValue(), (Address) null, timestampRangeValue(), typeValue()));
    }

    @Test
    @DisplayName("rejects null for type")
    @Tag("testId:mirrornode.account.Hook/type/rejects-null")
    void shouldRejectNullType() {
        assertThrows(NullPointerException.class, () -> new Hook(adminKeyValue(), contractIdValue(), createdTimestampValue(), deletedValue(), extensionPointValue(), hookIdValue(), ownerIdValue(), timestampRangeValue(), (HookType) null));
    }

    private static Authority adminKeyValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static Address contractIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static ZonedDateTime createdTimestampValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static boolean deletedValue() {
        return true;
    }

    private static HookExtensionPoint extensionPointValue() {
        return HookExtensionPoint.ACCOUNT_ALLOWANCE_HOOK;
    }

    private static long hookIdValue() {
        return 1L;
    }

    private static Address ownerIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static TimestampRange timestampRangeValue() {
        return new TimestampRange(ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), null);
    }

    private static HookType typeValue() {
        return HookType.EVM;
    }

    private static Hook create() {
        return new Hook(adminKeyValue(), contractIdValue(), createdTimestampValue(), deletedValue(), extensionPointValue(), hookIdValue(), ownerIdValue(), timestampRangeValue(), typeValue());
    }
}
