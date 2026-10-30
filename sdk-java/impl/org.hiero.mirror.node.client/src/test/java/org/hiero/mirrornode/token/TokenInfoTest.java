package org.hiero.mirrornode.token;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.mirrornode.common.FixedFee;
import org.hiero.token.TokenSupplyType;
import org.hiero.token.TokenType;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `TokenInfo`, generated from the specs.
@DisplayName("TokenInfo")
class TokenInfoTest {

    @Test
    @DisplayName("creates a TokenInfo and returns the values")
    @Tag("testId:mirrornode.token.TokenInfo/create")
    void shouldCreateAndReturnTheValues() {
        final Address tokenId = tokenIdValue();
        final TokenType type = typeValue();
        final String name = nameValue();
        final String symbol = symbolValue();
        final @Nullable String memo = memoValue();
        final long decimals = decimalsValue();
        final byte @Nullable [] metadata = metadataValue();
        final ZonedDateTime createdTimestamp = createdTimestampValue();
        final ZonedDateTime modifiedTimestamp = modifiedTimestampValue();
        final @Nullable ZonedDateTime expiryTimestamp = expiryTimestampValue();
        final TokenSupplyType supplyType = supplyTypeValue();
        final BigInteger initialSupply = initialSupplyValue();
        final BigInteger totalSupply = totalSupplyValue();
        final BigInteger maxSupply = maxSupplyValue();
        final AccountId treasuryAccountId = treasuryAccountIdValue();
        final @Nullable Boolean deleted = deletedValue();
        final CustomFee customFees = customFeesValue();
        final @Nullable Authority adminKey = adminKeyValue();
        final @Nullable AccountId autoRenewAccount = autoRenewAccountValue();
        final @Nullable Duration autoRenewPeriod = autoRenewPeriodValue();
        final @Nullable Authority feeScheduleKey = feeScheduleKeyValue();
        final boolean freezeDefault = freezeDefaultValue();
        final @Nullable Authority freezeKey = freezeKeyValue();
        final @Nullable Authority kycKey = kycKeyValue();
        final @Nullable Authority metadataKey = metadataKeyValue();
        final @Nullable Authority pauseKey = pauseKeyValue();
        final TokenPauseStatus pauseStatus = pauseStatusValue();
        final @Nullable Authority supplyKey = supplyKeyValue();
        final @Nullable Authority wipeKey = wipeKeyValue();
        final TokenInfo subject = new TokenInfo(tokenId, type, name, symbol, memo, decimals, metadata, createdTimestamp, modifiedTimestamp, expiryTimestamp, supplyType, initialSupply, totalSupply, maxSupply, treasuryAccountId, deleted, customFees, adminKey, autoRenewAccount, autoRenewPeriod, feeScheduleKey, freezeDefault, freezeKey, kycKey, metadataKey, pauseKey, pauseStatus, supplyKey, wipeKey);
        assertValue(tokenId, subject.tokenId());
        assertValue(type, subject.type());
        assertValue(name, subject.name());
        assertValue(symbol, subject.symbol());
        assertValue(memo, subject.memo());
        assertValue(decimals, subject.decimals());
        assertValue(metadata, subject.metadata());
        assertValue(createdTimestamp, subject.createdTimestamp());
        assertValue(modifiedTimestamp, subject.modifiedTimestamp());
        assertValue(expiryTimestamp, subject.expiryTimestamp());
        assertValue(supplyType, subject.supplyType());
        assertValue(initialSupply, subject.initialSupply());
        assertValue(totalSupply, subject.totalSupply());
        assertValue(maxSupply, subject.maxSupply());
        assertValue(treasuryAccountId, subject.treasuryAccountId());
        assertValue(deleted, subject.deleted());
        assertValue(customFees, subject.customFees());
        assertValue(adminKey, subject.adminKey());
        assertValue(autoRenewAccount, subject.autoRenewAccount());
        assertValue(autoRenewPeriod, subject.autoRenewPeriod());
        assertValue(feeScheduleKey, subject.feeScheduleKey());
        assertValue(freezeDefault, subject.freezeDefault());
        assertValue(freezeKey, subject.freezeKey());
        assertValue(kycKey, subject.kycKey());
        assertValue(metadataKey, subject.metadataKey());
        assertValue(pauseKey, subject.pauseKey());
        assertValue(pauseStatus, subject.pauseStatus());
        assertValue(supplyKey, subject.supplyKey());
        assertValue(wipeKey, subject.wipeKey());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.token.TokenInfo/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final Address tokenId = tokenIdValue();
        final TokenType type = typeValue();
        final String name = nameValue();
        final String symbol = symbolValue();
        final @Nullable String memo = memoValue();
        final long decimals = decimalsValue();
        final byte @Nullable [] metadata = metadataValue();
        final ZonedDateTime createdTimestamp = createdTimestampValue();
        final ZonedDateTime modifiedTimestamp = modifiedTimestampValue();
        final @Nullable ZonedDateTime expiryTimestamp = expiryTimestampValue();
        final TokenSupplyType supplyType = supplyTypeValue();
        final BigInteger initialSupply = initialSupplyValue();
        final BigInteger totalSupply = totalSupplyValue();
        final BigInteger maxSupply = maxSupplyValue();
        final AccountId treasuryAccountId = treasuryAccountIdValue();
        final @Nullable Boolean deleted = deletedValue();
        final CustomFee customFees = customFeesValue();
        final @Nullable Authority adminKey = adminKeyValue();
        final @Nullable AccountId autoRenewAccount = autoRenewAccountValue();
        final @Nullable Duration autoRenewPeriod = autoRenewPeriodValue();
        final @Nullable Authority feeScheduleKey = feeScheduleKeyValue();
        final boolean freezeDefault = freezeDefaultValue();
        final @Nullable Authority freezeKey = freezeKeyValue();
        final @Nullable Authority kycKey = kycKeyValue();
        final @Nullable Authority metadataKey = metadataKeyValue();
        final @Nullable Authority pauseKey = pauseKeyValue();
        final TokenPauseStatus pauseStatus = pauseStatusValue();
        final @Nullable Authority supplyKey = supplyKeyValue();
        final @Nullable Authority wipeKey = wipeKeyValue();
        final TokenInfo subject = TokenInfo.builder()
                .tokenId(tokenId)
                .type(type)
                .name(name)
                .symbol(symbol)
                .memo(memo)
                .decimals(decimals)
                .metadata(metadata)
                .createdTimestamp(createdTimestamp)
                .modifiedTimestamp(modifiedTimestamp)
                .expiryTimestamp(expiryTimestamp)
                .supplyType(supplyType)
                .initialSupply(initialSupply)
                .totalSupply(totalSupply)
                .maxSupply(maxSupply)
                .treasuryAccountId(treasuryAccountId)
                .deleted(deleted)
                .customFees(customFees)
                .adminKey(adminKey)
                .autoRenewAccount(autoRenewAccount)
                .autoRenewPeriod(autoRenewPeriod)
                .feeScheduleKey(feeScheduleKey)
                .freezeDefault(freezeDefault)
                .freezeKey(freezeKey)
                .kycKey(kycKey)
                .metadataKey(metadataKey)
                .pauseKey(pauseKey)
                .pauseStatus(pauseStatus)
                .supplyKey(supplyKey)
                .wipeKey(wipeKey)
                .build();
        assertValue(tokenId, subject.tokenId());
        assertValue(type, subject.type());
        assertValue(name, subject.name());
        assertValue(symbol, subject.symbol());
        assertValue(memo, subject.memo());
        assertValue(decimals, subject.decimals());
        assertValue(metadata, subject.metadata());
        assertValue(createdTimestamp, subject.createdTimestamp());
        assertValue(modifiedTimestamp, subject.modifiedTimestamp());
        assertValue(expiryTimestamp, subject.expiryTimestamp());
        assertValue(supplyType, subject.supplyType());
        assertValue(initialSupply, subject.initialSupply());
        assertValue(totalSupply, subject.totalSupply());
        assertValue(maxSupply, subject.maxSupply());
        assertValue(treasuryAccountId, subject.treasuryAccountId());
        assertValue(deleted, subject.deleted());
        assertValue(customFees, subject.customFees());
        assertValue(adminKey, subject.adminKey());
        assertValue(autoRenewAccount, subject.autoRenewAccount());
        assertValue(autoRenewPeriod, subject.autoRenewPeriod());
        assertValue(feeScheduleKey, subject.feeScheduleKey());
        assertValue(freezeDefault, subject.freezeDefault());
        assertValue(freezeKey, subject.freezeKey());
        assertValue(kycKey, subject.kycKey());
        assertValue(metadataKey, subject.metadataKey());
        assertValue(pauseKey, subject.pauseKey());
        assertValue(pauseStatus, subject.pauseStatus());
        assertValue(supplyKey, subject.supplyKey());
        assertValue(wipeKey, subject.wipeKey());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.token.TokenInfo/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final TokenInfo subject = TokenInfo.builder()
                .tokenId(tokenIdValue())
                .type(typeValue())
                .name(nameValue())
                .symbol(symbolValue())
                .decimals(decimalsValue())
                .createdTimestamp(createdTimestampValue())
                .modifiedTimestamp(modifiedTimestampValue())
                .supplyType(supplyTypeValue())
                .initialSupply(initialSupplyValue())
                .totalSupply(totalSupplyValue())
                .maxSupply(maxSupplyValue())
                .treasuryAccountId(treasuryAccountIdValue())
                .customFees(customFeesValue())
                .freezeDefault(freezeDefaultValue())
                .pauseStatus(pauseStatusValue())
                .build();
        assertNull(subject.memo());
        assertNull(subject.metadata());
        assertNull(subject.expiryTimestamp());
        assertNull(subject.deleted());
        assertNull(subject.adminKey());
        assertNull(subject.autoRenewAccount());
        assertNull(subject.autoRenewPeriod());
        assertNull(subject.feeScheduleKey());
        assertNull(subject.freezeKey());
        assertNull(subject.kycKey());
        assertNull(subject.metadataKey());
        assertNull(subject.pauseKey());
        assertNull(subject.supplyKey());
        assertNull(subject.wipeKey());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.token.TokenInfo/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> TokenInfo.builder().build());
    }

    @Test
    @DisplayName("rejects null for tokenId")
    @Tag("testId:mirrornode.token.TokenInfo/tokenId/rejects-null")
    void shouldRejectNullTokenId() {
        assertThrows(NullPointerException.class, () -> new TokenInfo((Address) null, typeValue(), nameValue(), symbolValue(), memoValue(), decimalsValue(), metadataValue(), createdTimestampValue(), modifiedTimestampValue(), expiryTimestampValue(), supplyTypeValue(), initialSupplyValue(), totalSupplyValue(), maxSupplyValue(), treasuryAccountIdValue(), deletedValue(), customFeesValue(), adminKeyValue(), autoRenewAccountValue(), autoRenewPeriodValue(), feeScheduleKeyValue(), freezeDefaultValue(), freezeKeyValue(), kycKeyValue(), metadataKeyValue(), pauseKeyValue(), pauseStatusValue(), supplyKeyValue(), wipeKeyValue()));
    }

    @Test
    @DisplayName("rejects null for type")
    @Tag("testId:mirrornode.token.TokenInfo/type/rejects-null")
    void shouldRejectNullType() {
        assertThrows(NullPointerException.class, () -> new TokenInfo(tokenIdValue(), (TokenType) null, nameValue(), symbolValue(), memoValue(), decimalsValue(), metadataValue(), createdTimestampValue(), modifiedTimestampValue(), expiryTimestampValue(), supplyTypeValue(), initialSupplyValue(), totalSupplyValue(), maxSupplyValue(), treasuryAccountIdValue(), deletedValue(), customFeesValue(), adminKeyValue(), autoRenewAccountValue(), autoRenewPeriodValue(), feeScheduleKeyValue(), freezeDefaultValue(), freezeKeyValue(), kycKeyValue(), metadataKeyValue(), pauseKeyValue(), pauseStatusValue(), supplyKeyValue(), wipeKeyValue()));
    }

    @Test
    @DisplayName("rejects null for name")
    @Tag("testId:mirrornode.token.TokenInfo/name/rejects-null")
    void shouldRejectNullName() {
        assertThrows(NullPointerException.class, () -> new TokenInfo(tokenIdValue(), typeValue(), (String) null, symbolValue(), memoValue(), decimalsValue(), metadataValue(), createdTimestampValue(), modifiedTimestampValue(), expiryTimestampValue(), supplyTypeValue(), initialSupplyValue(), totalSupplyValue(), maxSupplyValue(), treasuryAccountIdValue(), deletedValue(), customFeesValue(), adminKeyValue(), autoRenewAccountValue(), autoRenewPeriodValue(), feeScheduleKeyValue(), freezeDefaultValue(), freezeKeyValue(), kycKeyValue(), metadataKeyValue(), pauseKeyValue(), pauseStatusValue(), supplyKeyValue(), wipeKeyValue()));
    }

    @Test
    @DisplayName("rejects null for symbol")
    @Tag("testId:mirrornode.token.TokenInfo/symbol/rejects-null")
    void shouldRejectNullSymbol() {
        assertThrows(NullPointerException.class, () -> new TokenInfo(tokenIdValue(), typeValue(), nameValue(), (String) null, memoValue(), decimalsValue(), metadataValue(), createdTimestampValue(), modifiedTimestampValue(), expiryTimestampValue(), supplyTypeValue(), initialSupplyValue(), totalSupplyValue(), maxSupplyValue(), treasuryAccountIdValue(), deletedValue(), customFeesValue(), adminKeyValue(), autoRenewAccountValue(), autoRenewPeriodValue(), feeScheduleKeyValue(), freezeDefaultValue(), freezeKeyValue(), kycKeyValue(), metadataKeyValue(), pauseKeyValue(), pauseStatusValue(), supplyKeyValue(), wipeKeyValue()));
    }

    @Test
    @DisplayName("decimals: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.token.TokenInfo/decimals/accepts-at-minimum")
    void shouldAcceptDecimalsAtMinimum() {
        final long value = -9223372036854775808L;
        final TokenInfo subject = new TokenInfo(tokenIdValue(), typeValue(), nameValue(), symbolValue(), memoValue(), value, metadataValue(), createdTimestampValue(), modifiedTimestampValue(), expiryTimestampValue(), supplyTypeValue(), initialSupplyValue(), totalSupplyValue(), maxSupplyValue(), treasuryAccountIdValue(), deletedValue(), customFeesValue(), adminKeyValue(), autoRenewAccountValue(), autoRenewPeriodValue(), feeScheduleKeyValue(), freezeDefaultValue(), freezeKeyValue(), kycKeyValue(), metadataKeyValue(), pauseKeyValue(), pauseStatusValue(), supplyKeyValue(), wipeKeyValue());
        assertValue(value, subject.decimals());
    }

    @Test
    @DisplayName("decimals: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.token.TokenInfo/decimals/accepts-at-maximum")
    void shouldAcceptDecimalsAtMaximum() {
        final long value = 9223372036854775807L;
        final TokenInfo subject = new TokenInfo(tokenIdValue(), typeValue(), nameValue(), symbolValue(), memoValue(), value, metadataValue(), createdTimestampValue(), modifiedTimestampValue(), expiryTimestampValue(), supplyTypeValue(), initialSupplyValue(), totalSupplyValue(), maxSupplyValue(), treasuryAccountIdValue(), deletedValue(), customFeesValue(), adminKeyValue(), autoRenewAccountValue(), autoRenewPeriodValue(), feeScheduleKeyValue(), freezeDefaultValue(), freezeKeyValue(), kycKeyValue(), metadataKeyValue(), pauseKeyValue(), pauseStatusValue(), supplyKeyValue(), wipeKeyValue());
        assertValue(value, subject.decimals());
    }

    @Test
    @DisplayName("the constructor copies metadata; the accessor returns a copy")
    @Tag("testId:mirrornode.token.TokenInfo/metadata/copies-in")
    void shouldCopyMetadataIn() {
        final byte[] value = metadataValue();
        final byte[] expected = value.clone();
        final TokenInfo subject = new TokenInfo(tokenIdValue(), typeValue(), nameValue(), symbolValue(), memoValue(), decimalsValue(), value, createdTimestampValue(), modifiedTimestampValue(), expiryTimestampValue(), supplyTypeValue(), initialSupplyValue(), totalSupplyValue(), maxSupplyValue(), treasuryAccountIdValue(), deletedValue(), customFeesValue(), adminKeyValue(), autoRenewAccountValue(), autoRenewPeriodValue(), feeScheduleKeyValue(), freezeDefaultValue(), freezeKeyValue(), kycKeyValue(), metadataKeyValue(), pauseKeyValue(), pauseStatusValue(), supplyKeyValue(), wipeKeyValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.metadata());
        final byte[] returned = subject.metadata();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.metadata());
    }

    @Test
    @DisplayName("rejects null for createdTimestamp")
    @Tag("testId:mirrornode.token.TokenInfo/createdTimestamp/rejects-null")
    void shouldRejectNullCreatedTimestamp() {
        assertThrows(NullPointerException.class, () -> new TokenInfo(tokenIdValue(), typeValue(), nameValue(), symbolValue(), memoValue(), decimalsValue(), metadataValue(), (ZonedDateTime) null, modifiedTimestampValue(), expiryTimestampValue(), supplyTypeValue(), initialSupplyValue(), totalSupplyValue(), maxSupplyValue(), treasuryAccountIdValue(), deletedValue(), customFeesValue(), adminKeyValue(), autoRenewAccountValue(), autoRenewPeriodValue(), feeScheduleKeyValue(), freezeDefaultValue(), freezeKeyValue(), kycKeyValue(), metadataKeyValue(), pauseKeyValue(), pauseStatusValue(), supplyKeyValue(), wipeKeyValue()));
    }

    @Test
    @DisplayName("rejects null for modifiedTimestamp")
    @Tag("testId:mirrornode.token.TokenInfo/modifiedTimestamp/rejects-null")
    void shouldRejectNullModifiedTimestamp() {
        assertThrows(NullPointerException.class, () -> new TokenInfo(tokenIdValue(), typeValue(), nameValue(), symbolValue(), memoValue(), decimalsValue(), metadataValue(), createdTimestampValue(), (ZonedDateTime) null, expiryTimestampValue(), supplyTypeValue(), initialSupplyValue(), totalSupplyValue(), maxSupplyValue(), treasuryAccountIdValue(), deletedValue(), customFeesValue(), adminKeyValue(), autoRenewAccountValue(), autoRenewPeriodValue(), feeScheduleKeyValue(), freezeDefaultValue(), freezeKeyValue(), kycKeyValue(), metadataKeyValue(), pauseKeyValue(), pauseStatusValue(), supplyKeyValue(), wipeKeyValue()));
    }

    @Test
    @DisplayName("rejects null for supplyType")
    @Tag("testId:mirrornode.token.TokenInfo/supplyType/rejects-null")
    void shouldRejectNullSupplyType() {
        assertThrows(NullPointerException.class, () -> new TokenInfo(tokenIdValue(), typeValue(), nameValue(), symbolValue(), memoValue(), decimalsValue(), metadataValue(), createdTimestampValue(), modifiedTimestampValue(), expiryTimestampValue(), (TokenSupplyType) null, initialSupplyValue(), totalSupplyValue(), maxSupplyValue(), treasuryAccountIdValue(), deletedValue(), customFeesValue(), adminKeyValue(), autoRenewAccountValue(), autoRenewPeriodValue(), feeScheduleKeyValue(), freezeDefaultValue(), freezeKeyValue(), kycKeyValue(), metadataKeyValue(), pauseKeyValue(), pauseStatusValue(), supplyKeyValue(), wipeKeyValue()));
    }

    @Test
    @DisplayName("rejects null for initialSupply")
    @Tag("testId:mirrornode.token.TokenInfo/initialSupply/rejects-null")
    void shouldRejectNullInitialSupply() {
        assertThrows(NullPointerException.class, () -> new TokenInfo(tokenIdValue(), typeValue(), nameValue(), symbolValue(), memoValue(), decimalsValue(), metadataValue(), createdTimestampValue(), modifiedTimestampValue(), expiryTimestampValue(), supplyTypeValue(), (BigInteger) null, totalSupplyValue(), maxSupplyValue(), treasuryAccountIdValue(), deletedValue(), customFeesValue(), adminKeyValue(), autoRenewAccountValue(), autoRenewPeriodValue(), feeScheduleKeyValue(), freezeDefaultValue(), freezeKeyValue(), kycKeyValue(), metadataKeyValue(), pauseKeyValue(), pauseStatusValue(), supplyKeyValue(), wipeKeyValue()));
    }

    @Test
    @DisplayName("initialSupply: accepts the minimum (-57896044618658097711785492504343953926634992332820282019728792003956564819968)")
    @Tag("testId:mirrornode.token.TokenInfo/initialSupply/accepts-at-minimum")
    void shouldAcceptInitialSupplyAtMinimum() {
        final BigInteger value = new BigInteger("-57896044618658097711785492504343953926634992332820282019728792003956564819968");
        final TokenInfo subject = new TokenInfo(tokenIdValue(), typeValue(), nameValue(), symbolValue(), memoValue(), decimalsValue(), metadataValue(), createdTimestampValue(), modifiedTimestampValue(), expiryTimestampValue(), supplyTypeValue(), value, totalSupplyValue(), maxSupplyValue(), treasuryAccountIdValue(), deletedValue(), customFeesValue(), adminKeyValue(), autoRenewAccountValue(), autoRenewPeriodValue(), feeScheduleKeyValue(), freezeDefaultValue(), freezeKeyValue(), kycKeyValue(), metadataKeyValue(), pauseKeyValue(), pauseStatusValue(), supplyKeyValue(), wipeKeyValue());
        assertValue(value, subject.initialSupply());
    }

    @Test
    @DisplayName("initialSupply: accepts the maximum (57896044618658097711785492504343953926634992332820282019728792003956564819967)")
    @Tag("testId:mirrornode.token.TokenInfo/initialSupply/accepts-at-maximum")
    void shouldAcceptInitialSupplyAtMaximum() {
        final BigInteger value = new BigInteger("57896044618658097711785492504343953926634992332820282019728792003956564819967");
        final TokenInfo subject = new TokenInfo(tokenIdValue(), typeValue(), nameValue(), symbolValue(), memoValue(), decimalsValue(), metadataValue(), createdTimestampValue(), modifiedTimestampValue(), expiryTimestampValue(), supplyTypeValue(), value, totalSupplyValue(), maxSupplyValue(), treasuryAccountIdValue(), deletedValue(), customFeesValue(), adminKeyValue(), autoRenewAccountValue(), autoRenewPeriodValue(), feeScheduleKeyValue(), freezeDefaultValue(), freezeKeyValue(), kycKeyValue(), metadataKeyValue(), pauseKeyValue(), pauseStatusValue(), supplyKeyValue(), wipeKeyValue());
        assertValue(value, subject.initialSupply());
    }

    @Test
    @DisplayName("initialSupply: rejects a value below the minimum (-57896044618658097711785492504343953926634992332820282019728792003956564819969)")
    @Tag("testId:mirrornode.token.TokenInfo/initialSupply/rejects-below-minimum")
    void shouldRejectInitialSupplyBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new TokenInfo(tokenIdValue(), typeValue(), nameValue(), symbolValue(), memoValue(), decimalsValue(), metadataValue(), createdTimestampValue(), modifiedTimestampValue(), expiryTimestampValue(), supplyTypeValue(), new BigInteger("-57896044618658097711785492504343953926634992332820282019728792003956564819969"), totalSupplyValue(), maxSupplyValue(), treasuryAccountIdValue(), deletedValue(), customFeesValue(), adminKeyValue(), autoRenewAccountValue(), autoRenewPeriodValue(), feeScheduleKeyValue(), freezeDefaultValue(), freezeKeyValue(), kycKeyValue(), metadataKeyValue(), pauseKeyValue(), pauseStatusValue(), supplyKeyValue(), wipeKeyValue()));
    }

    @Test
    @DisplayName("initialSupply: rejects a value above the maximum (57896044618658097711785492504343953926634992332820282019728792003956564819968)")
    @Tag("testId:mirrornode.token.TokenInfo/initialSupply/rejects-above-maximum")
    void shouldRejectInitialSupplyAboveMaximum() {
        assertThrows(IllegalArgumentException.class, () -> new TokenInfo(tokenIdValue(), typeValue(), nameValue(), symbolValue(), memoValue(), decimalsValue(), metadataValue(), createdTimestampValue(), modifiedTimestampValue(), expiryTimestampValue(), supplyTypeValue(), new BigInteger("57896044618658097711785492504343953926634992332820282019728792003956564819968"), totalSupplyValue(), maxSupplyValue(), treasuryAccountIdValue(), deletedValue(), customFeesValue(), adminKeyValue(), autoRenewAccountValue(), autoRenewPeriodValue(), feeScheduleKeyValue(), freezeDefaultValue(), freezeKeyValue(), kycKeyValue(), metadataKeyValue(), pauseKeyValue(), pauseStatusValue(), supplyKeyValue(), wipeKeyValue()));
    }

    @Test
    @DisplayName("rejects null for totalSupply")
    @Tag("testId:mirrornode.token.TokenInfo/totalSupply/rejects-null")
    void shouldRejectNullTotalSupply() {
        assertThrows(NullPointerException.class, () -> new TokenInfo(tokenIdValue(), typeValue(), nameValue(), symbolValue(), memoValue(), decimalsValue(), metadataValue(), createdTimestampValue(), modifiedTimestampValue(), expiryTimestampValue(), supplyTypeValue(), initialSupplyValue(), (BigInteger) null, maxSupplyValue(), treasuryAccountIdValue(), deletedValue(), customFeesValue(), adminKeyValue(), autoRenewAccountValue(), autoRenewPeriodValue(), feeScheduleKeyValue(), freezeDefaultValue(), freezeKeyValue(), kycKeyValue(), metadataKeyValue(), pauseKeyValue(), pauseStatusValue(), supplyKeyValue(), wipeKeyValue()));
    }

    @Test
    @DisplayName("totalSupply: accepts the minimum (-57896044618658097711785492504343953926634992332820282019728792003956564819968)")
    @Tag("testId:mirrornode.token.TokenInfo/totalSupply/accepts-at-minimum")
    void shouldAcceptTotalSupplyAtMinimum() {
        final BigInteger value = new BigInteger("-57896044618658097711785492504343953926634992332820282019728792003956564819968");
        final TokenInfo subject = new TokenInfo(tokenIdValue(), typeValue(), nameValue(), symbolValue(), memoValue(), decimalsValue(), metadataValue(), createdTimestampValue(), modifiedTimestampValue(), expiryTimestampValue(), supplyTypeValue(), initialSupplyValue(), value, maxSupplyValue(), treasuryAccountIdValue(), deletedValue(), customFeesValue(), adminKeyValue(), autoRenewAccountValue(), autoRenewPeriodValue(), feeScheduleKeyValue(), freezeDefaultValue(), freezeKeyValue(), kycKeyValue(), metadataKeyValue(), pauseKeyValue(), pauseStatusValue(), supplyKeyValue(), wipeKeyValue());
        assertValue(value, subject.totalSupply());
    }

    @Test
    @DisplayName("totalSupply: accepts the maximum (57896044618658097711785492504343953926634992332820282019728792003956564819967)")
    @Tag("testId:mirrornode.token.TokenInfo/totalSupply/accepts-at-maximum")
    void shouldAcceptTotalSupplyAtMaximum() {
        final BigInteger value = new BigInteger("57896044618658097711785492504343953926634992332820282019728792003956564819967");
        final TokenInfo subject = new TokenInfo(tokenIdValue(), typeValue(), nameValue(), symbolValue(), memoValue(), decimalsValue(), metadataValue(), createdTimestampValue(), modifiedTimestampValue(), expiryTimestampValue(), supplyTypeValue(), initialSupplyValue(), value, maxSupplyValue(), treasuryAccountIdValue(), deletedValue(), customFeesValue(), adminKeyValue(), autoRenewAccountValue(), autoRenewPeriodValue(), feeScheduleKeyValue(), freezeDefaultValue(), freezeKeyValue(), kycKeyValue(), metadataKeyValue(), pauseKeyValue(), pauseStatusValue(), supplyKeyValue(), wipeKeyValue());
        assertValue(value, subject.totalSupply());
    }

    @Test
    @DisplayName("totalSupply: rejects a value below the minimum (-57896044618658097711785492504343953926634992332820282019728792003956564819969)")
    @Tag("testId:mirrornode.token.TokenInfo/totalSupply/rejects-below-minimum")
    void shouldRejectTotalSupplyBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new TokenInfo(tokenIdValue(), typeValue(), nameValue(), symbolValue(), memoValue(), decimalsValue(), metadataValue(), createdTimestampValue(), modifiedTimestampValue(), expiryTimestampValue(), supplyTypeValue(), initialSupplyValue(), new BigInteger("-57896044618658097711785492504343953926634992332820282019728792003956564819969"), maxSupplyValue(), treasuryAccountIdValue(), deletedValue(), customFeesValue(), adminKeyValue(), autoRenewAccountValue(), autoRenewPeriodValue(), feeScheduleKeyValue(), freezeDefaultValue(), freezeKeyValue(), kycKeyValue(), metadataKeyValue(), pauseKeyValue(), pauseStatusValue(), supplyKeyValue(), wipeKeyValue()));
    }

    @Test
    @DisplayName("totalSupply: rejects a value above the maximum (57896044618658097711785492504343953926634992332820282019728792003956564819968)")
    @Tag("testId:mirrornode.token.TokenInfo/totalSupply/rejects-above-maximum")
    void shouldRejectTotalSupplyAboveMaximum() {
        assertThrows(IllegalArgumentException.class, () -> new TokenInfo(tokenIdValue(), typeValue(), nameValue(), symbolValue(), memoValue(), decimalsValue(), metadataValue(), createdTimestampValue(), modifiedTimestampValue(), expiryTimestampValue(), supplyTypeValue(), initialSupplyValue(), new BigInteger("57896044618658097711785492504343953926634992332820282019728792003956564819968"), maxSupplyValue(), treasuryAccountIdValue(), deletedValue(), customFeesValue(), adminKeyValue(), autoRenewAccountValue(), autoRenewPeriodValue(), feeScheduleKeyValue(), freezeDefaultValue(), freezeKeyValue(), kycKeyValue(), metadataKeyValue(), pauseKeyValue(), pauseStatusValue(), supplyKeyValue(), wipeKeyValue()));
    }

    @Test
    @DisplayName("rejects null for maxSupply")
    @Tag("testId:mirrornode.token.TokenInfo/maxSupply/rejects-null")
    void shouldRejectNullMaxSupply() {
        assertThrows(NullPointerException.class, () -> new TokenInfo(tokenIdValue(), typeValue(), nameValue(), symbolValue(), memoValue(), decimalsValue(), metadataValue(), createdTimestampValue(), modifiedTimestampValue(), expiryTimestampValue(), supplyTypeValue(), initialSupplyValue(), totalSupplyValue(), (BigInteger) null, treasuryAccountIdValue(), deletedValue(), customFeesValue(), adminKeyValue(), autoRenewAccountValue(), autoRenewPeriodValue(), feeScheduleKeyValue(), freezeDefaultValue(), freezeKeyValue(), kycKeyValue(), metadataKeyValue(), pauseKeyValue(), pauseStatusValue(), supplyKeyValue(), wipeKeyValue()));
    }

    @Test
    @DisplayName("maxSupply: accepts the minimum (-57896044618658097711785492504343953926634992332820282019728792003956564819968)")
    @Tag("testId:mirrornode.token.TokenInfo/maxSupply/accepts-at-minimum")
    void shouldAcceptMaxSupplyAtMinimum() {
        final BigInteger value = new BigInteger("-57896044618658097711785492504343953926634992332820282019728792003956564819968");
        final TokenInfo subject = new TokenInfo(tokenIdValue(), typeValue(), nameValue(), symbolValue(), memoValue(), decimalsValue(), metadataValue(), createdTimestampValue(), modifiedTimestampValue(), expiryTimestampValue(), supplyTypeValue(), initialSupplyValue(), totalSupplyValue(), value, treasuryAccountIdValue(), deletedValue(), customFeesValue(), adminKeyValue(), autoRenewAccountValue(), autoRenewPeriodValue(), feeScheduleKeyValue(), freezeDefaultValue(), freezeKeyValue(), kycKeyValue(), metadataKeyValue(), pauseKeyValue(), pauseStatusValue(), supplyKeyValue(), wipeKeyValue());
        assertValue(value, subject.maxSupply());
    }

    @Test
    @DisplayName("maxSupply: accepts the maximum (57896044618658097711785492504343953926634992332820282019728792003956564819967)")
    @Tag("testId:mirrornode.token.TokenInfo/maxSupply/accepts-at-maximum")
    void shouldAcceptMaxSupplyAtMaximum() {
        final BigInteger value = new BigInteger("57896044618658097711785492504343953926634992332820282019728792003956564819967");
        final TokenInfo subject = new TokenInfo(tokenIdValue(), typeValue(), nameValue(), symbolValue(), memoValue(), decimalsValue(), metadataValue(), createdTimestampValue(), modifiedTimestampValue(), expiryTimestampValue(), supplyTypeValue(), initialSupplyValue(), totalSupplyValue(), value, treasuryAccountIdValue(), deletedValue(), customFeesValue(), adminKeyValue(), autoRenewAccountValue(), autoRenewPeriodValue(), feeScheduleKeyValue(), freezeDefaultValue(), freezeKeyValue(), kycKeyValue(), metadataKeyValue(), pauseKeyValue(), pauseStatusValue(), supplyKeyValue(), wipeKeyValue());
        assertValue(value, subject.maxSupply());
    }

    @Test
    @DisplayName("maxSupply: rejects a value below the minimum (-57896044618658097711785492504343953926634992332820282019728792003956564819969)")
    @Tag("testId:mirrornode.token.TokenInfo/maxSupply/rejects-below-minimum")
    void shouldRejectMaxSupplyBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new TokenInfo(tokenIdValue(), typeValue(), nameValue(), symbolValue(), memoValue(), decimalsValue(), metadataValue(), createdTimestampValue(), modifiedTimestampValue(), expiryTimestampValue(), supplyTypeValue(), initialSupplyValue(), totalSupplyValue(), new BigInteger("-57896044618658097711785492504343953926634992332820282019728792003956564819969"), treasuryAccountIdValue(), deletedValue(), customFeesValue(), adminKeyValue(), autoRenewAccountValue(), autoRenewPeriodValue(), feeScheduleKeyValue(), freezeDefaultValue(), freezeKeyValue(), kycKeyValue(), metadataKeyValue(), pauseKeyValue(), pauseStatusValue(), supplyKeyValue(), wipeKeyValue()));
    }

    @Test
    @DisplayName("maxSupply: rejects a value above the maximum (57896044618658097711785492504343953926634992332820282019728792003956564819968)")
    @Tag("testId:mirrornode.token.TokenInfo/maxSupply/rejects-above-maximum")
    void shouldRejectMaxSupplyAboveMaximum() {
        assertThrows(IllegalArgumentException.class, () -> new TokenInfo(tokenIdValue(), typeValue(), nameValue(), symbolValue(), memoValue(), decimalsValue(), metadataValue(), createdTimestampValue(), modifiedTimestampValue(), expiryTimestampValue(), supplyTypeValue(), initialSupplyValue(), totalSupplyValue(), new BigInteger("57896044618658097711785492504343953926634992332820282019728792003956564819968"), treasuryAccountIdValue(), deletedValue(), customFeesValue(), adminKeyValue(), autoRenewAccountValue(), autoRenewPeriodValue(), feeScheduleKeyValue(), freezeDefaultValue(), freezeKeyValue(), kycKeyValue(), metadataKeyValue(), pauseKeyValue(), pauseStatusValue(), supplyKeyValue(), wipeKeyValue()));
    }

    @Test
    @DisplayName("rejects null for treasuryAccountId")
    @Tag("testId:mirrornode.token.TokenInfo/treasuryAccountId/rejects-null")
    void shouldRejectNullTreasuryAccountId() {
        assertThrows(NullPointerException.class, () -> new TokenInfo(tokenIdValue(), typeValue(), nameValue(), symbolValue(), memoValue(), decimalsValue(), metadataValue(), createdTimestampValue(), modifiedTimestampValue(), expiryTimestampValue(), supplyTypeValue(), initialSupplyValue(), totalSupplyValue(), maxSupplyValue(), (AccountId) null, deletedValue(), customFeesValue(), adminKeyValue(), autoRenewAccountValue(), autoRenewPeriodValue(), feeScheduleKeyValue(), freezeDefaultValue(), freezeKeyValue(), kycKeyValue(), metadataKeyValue(), pauseKeyValue(), pauseStatusValue(), supplyKeyValue(), wipeKeyValue()));
    }

    @Test
    @DisplayName("rejects null for customFees")
    @Tag("testId:mirrornode.token.TokenInfo/customFees/rejects-null")
    void shouldRejectNullCustomFees() {
        assertThrows(NullPointerException.class, () -> new TokenInfo(tokenIdValue(), typeValue(), nameValue(), symbolValue(), memoValue(), decimalsValue(), metadataValue(), createdTimestampValue(), modifiedTimestampValue(), expiryTimestampValue(), supplyTypeValue(), initialSupplyValue(), totalSupplyValue(), maxSupplyValue(), treasuryAccountIdValue(), deletedValue(), (CustomFee) null, adminKeyValue(), autoRenewAccountValue(), autoRenewPeriodValue(), feeScheduleKeyValue(), freezeDefaultValue(), freezeKeyValue(), kycKeyValue(), metadataKeyValue(), pauseKeyValue(), pauseStatusValue(), supplyKeyValue(), wipeKeyValue()));
    }

    @Test
    @DisplayName("rejects null for pauseStatus")
    @Tag("testId:mirrornode.token.TokenInfo/pauseStatus/rejects-null")
    void shouldRejectNullPauseStatus() {
        assertThrows(NullPointerException.class, () -> new TokenInfo(tokenIdValue(), typeValue(), nameValue(), symbolValue(), memoValue(), decimalsValue(), metadataValue(), createdTimestampValue(), modifiedTimestampValue(), expiryTimestampValue(), supplyTypeValue(), initialSupplyValue(), totalSupplyValue(), maxSupplyValue(), treasuryAccountIdValue(), deletedValue(), customFeesValue(), adminKeyValue(), autoRenewAccountValue(), autoRenewPeriodValue(), feeScheduleKeyValue(), freezeDefaultValue(), freezeKeyValue(), kycKeyValue(), metadataKeyValue(), pauseKeyValue(), (TokenPauseStatus) null, supplyKeyValue(), wipeKeyValue()));
    }

    private static Address tokenIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static TokenType typeValue() {
        return TokenType.FUNGIBLE_COMMON;
    }

    private static String nameValue() {
        return "value";
    }

    private static String symbolValue() {
        return "value";
    }

    private static String memoValue() {
        return "value";
    }

    private static long decimalsValue() {
        return 1L;
    }

    private static byte[] metadataValue() {
        return new byte[] {1, 2, 3};
    }

    private static ZonedDateTime createdTimestampValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static ZonedDateTime modifiedTimestampValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static ZonedDateTime expiryTimestampValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static TokenSupplyType supplyTypeValue() {
        return TokenSupplyType.INFINITE;
    }

    private static BigInteger initialSupplyValue() {
        return new BigInteger("1");
    }

    private static BigInteger totalSupplyValue() {
        return new BigInteger("1");
    }

    private static BigInteger maxSupplyValue() {
        return new BigInteger("1");
    }

    private static AccountId treasuryAccountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static boolean deletedValue() {
        return true;
    }

    private static CustomFee customFeesValue() {
        return new CustomFee(List.of(new FixedFee(true, 1L, null, null)), List.of(new FractionalFee(1L, 1L, null, null, true, null, 1L, true)), List.of(new RoyaltyFee(1L, 1L, null, null, null, true)), null);
    }

    private static Authority adminKeyValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static AccountId autoRenewAccountValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static Duration autoRenewPeriodValue() {
        return Duration.ofSeconds(1L);
    }

    private static Authority feeScheduleKeyValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static boolean freezeDefaultValue() {
        return true;
    }

    private static Authority freezeKeyValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static Authority kycKeyValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static Authority metadataKeyValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static Authority pauseKeyValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static TokenPauseStatus pauseStatusValue() {
        return TokenPauseStatus.NOT_APPLICABLE;
    }

    private static Authority supplyKeyValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static Authority wipeKeyValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static TokenInfo create() {
        return new TokenInfo(tokenIdValue(), typeValue(), nameValue(), symbolValue(), memoValue(), decimalsValue(), metadataValue(), createdTimestampValue(), modifiedTimestampValue(), expiryTimestampValue(), supplyTypeValue(), initialSupplyValue(), totalSupplyValue(), maxSupplyValue(), treasuryAccountIdValue(), deletedValue(), customFeesValue(), adminKeyValue(), autoRenewAccountValue(), autoRenewPeriodValue(), feeScheduleKeyValue(), freezeDefaultValue(), freezeKeyValue(), kycKeyValue(), metadataKeyValue(), pauseKeyValue(), pauseStatusValue(), supplyKeyValue(), wipeKeyValue());
    }
}
