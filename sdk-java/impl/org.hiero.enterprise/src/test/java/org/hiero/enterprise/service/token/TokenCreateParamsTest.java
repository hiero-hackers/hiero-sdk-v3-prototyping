package org.hiero.enterprise.service.token;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.AccountId;
import org.hiero.token.TokenSupplyType;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `TokenCreateParams`, generated from the specs.
@DisplayName("TokenCreateParams")
class TokenCreateParamsTest {

    @Test
    @DisplayName("creates a TokenCreateParams and returns the values")
    @Tag("testId:enterprise.service.token.TokenCreateParams/create")
    void shouldCreateAndReturnTheValues() {
        final String name = nameValue();
        final String symbol = symbolValue();
        final @Nullable AccountId treasuryAccountId = treasuryAccountIdValue();
        final int decimals = decimalsValue();
        final long initialSupply = initialSupplyValue();
        final TokenSupplyType supplyType = supplyTypeValue();
        final @Nullable Long maxSupply = maxSupplyValue();
        final boolean freezeDefault = freezeDefaultValue();
        final @Nullable String tokenMemo = tokenMemoValue();
        final byte[] metadata = metadataValue();
        final @Nullable ZonedDateTime expirationTime = expirationTimeValue();
        final @Nullable Duration autoRenewPeriod = autoRenewPeriodValue();
        final @Nullable AccountId autoRenewAccount = autoRenewAccountValue();
        final @Nullable Authority adminAuthority = adminAuthorityValue();
        final @Nullable Authority supplyAuthority = supplyAuthorityValue();
        final @Nullable Authority kycAuthority = kycAuthorityValue();
        final @Nullable Authority freezeAuthority = freezeAuthorityValue();
        final @Nullable Authority wipeAuthority = wipeAuthorityValue();
        final @Nullable Authority pauseAuthority = pauseAuthorityValue();
        final @Nullable Authority feeScheduleAuthority = feeScheduleAuthorityValue();
        final @Nullable Authority metadataAuthority = metadataAuthorityValue();
        final TokenCreateParams subject = new TokenCreateParams(name, symbol, treasuryAccountId, decimals, initialSupply, supplyType, maxSupply, freezeDefault, tokenMemo, metadata, expirationTime, autoRenewPeriod, autoRenewAccount, adminAuthority, supplyAuthority, kycAuthority, freezeAuthority, wipeAuthority, pauseAuthority, feeScheduleAuthority, metadataAuthority);
        assertValue(name, subject.name());
        assertValue(symbol, subject.symbol());
        assertValue(treasuryAccountId, subject.treasuryAccountId());
        assertValue(decimals, subject.decimals());
        assertValue(initialSupply, subject.initialSupply());
        assertValue(supplyType, subject.supplyType());
        assertValue(maxSupply, subject.maxSupply());
        assertValue(freezeDefault, subject.freezeDefault());
        assertValue(tokenMemo, subject.tokenMemo());
        assertValue(metadata, subject.metadata());
        assertValue(expirationTime, subject.expirationTime());
        assertValue(autoRenewPeriod, subject.autoRenewPeriod());
        assertValue(autoRenewAccount, subject.autoRenewAccount());
        assertValue(adminAuthority, subject.adminAuthority());
        assertValue(supplyAuthority, subject.supplyAuthority());
        assertValue(kycAuthority, subject.kycAuthority());
        assertValue(freezeAuthority, subject.freezeAuthority());
        assertValue(wipeAuthority, subject.wipeAuthority());
        assertValue(pauseAuthority, subject.pauseAuthority());
        assertValue(feeScheduleAuthority, subject.feeScheduleAuthority());
        assertValue(metadataAuthority, subject.metadataAuthority());
    }

    @Test
    @DisplayName("creates a TokenCreateParams with the default values")
    @Tag("testId:enterprise.service.token.TokenCreateParams/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final TokenCreateParams subject = new TokenCreateParams(nameValue(), symbolValue(), treasuryAccountIdValue(), maxSupplyValue(), tokenMemoValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue());
        assertValue(0, subject.decimals());
        assertValue(0L, subject.initialSupply());
        assertValue(TokenSupplyType.INFINITE, subject.supplyType());
        assertValue(false, subject.freezeDefault());
        assertValue(new byte[0], subject.metadata());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:enterprise.service.token.TokenCreateParams/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final String name = nameValue();
        final String symbol = symbolValue();
        final @Nullable AccountId treasuryAccountId = treasuryAccountIdValue();
        final int decimals = decimalsValue();
        final long initialSupply = initialSupplyValue();
        final TokenSupplyType supplyType = supplyTypeValue();
        final @Nullable Long maxSupply = maxSupplyValue();
        final boolean freezeDefault = freezeDefaultValue();
        final @Nullable String tokenMemo = tokenMemoValue();
        final byte[] metadata = metadataValue();
        final @Nullable ZonedDateTime expirationTime = expirationTimeValue();
        final @Nullable Duration autoRenewPeriod = autoRenewPeriodValue();
        final @Nullable AccountId autoRenewAccount = autoRenewAccountValue();
        final @Nullable Authority adminAuthority = adminAuthorityValue();
        final @Nullable Authority supplyAuthority = supplyAuthorityValue();
        final @Nullable Authority kycAuthority = kycAuthorityValue();
        final @Nullable Authority freezeAuthority = freezeAuthorityValue();
        final @Nullable Authority wipeAuthority = wipeAuthorityValue();
        final @Nullable Authority pauseAuthority = pauseAuthorityValue();
        final @Nullable Authority feeScheduleAuthority = feeScheduleAuthorityValue();
        final @Nullable Authority metadataAuthority = metadataAuthorityValue();
        final TokenCreateParams subject = TokenCreateParams.builder()
                .name(name)
                .symbol(symbol)
                .treasuryAccountId(treasuryAccountId)
                .decimals(decimals)
                .initialSupply(initialSupply)
                .supplyType(supplyType)
                .maxSupply(maxSupply)
                .freezeDefault(freezeDefault)
                .tokenMemo(tokenMemo)
                .metadata(metadata)
                .expirationTime(expirationTime)
                .autoRenewPeriod(autoRenewPeriod)
                .autoRenewAccount(autoRenewAccount)
                .adminAuthority(adminAuthority)
                .supplyAuthority(supplyAuthority)
                .kycAuthority(kycAuthority)
                .freezeAuthority(freezeAuthority)
                .wipeAuthority(wipeAuthority)
                .pauseAuthority(pauseAuthority)
                .feeScheduleAuthority(feeScheduleAuthority)
                .metadataAuthority(metadataAuthority)
                .build();
        assertValue(name, subject.name());
        assertValue(symbol, subject.symbol());
        assertValue(treasuryAccountId, subject.treasuryAccountId());
        assertValue(decimals, subject.decimals());
        assertValue(initialSupply, subject.initialSupply());
        assertValue(supplyType, subject.supplyType());
        assertValue(maxSupply, subject.maxSupply());
        assertValue(freezeDefault, subject.freezeDefault());
        assertValue(tokenMemo, subject.tokenMemo());
        assertValue(metadata, subject.metadata());
        assertValue(expirationTime, subject.expirationTime());
        assertValue(autoRenewPeriod, subject.autoRenewPeriod());
        assertValue(autoRenewAccount, subject.autoRenewAccount());
        assertValue(adminAuthority, subject.adminAuthority());
        assertValue(supplyAuthority, subject.supplyAuthority());
        assertValue(kycAuthority, subject.kycAuthority());
        assertValue(freezeAuthority, subject.freezeAuthority());
        assertValue(wipeAuthority, subject.wipeAuthority());
        assertValue(pauseAuthority, subject.pauseAuthority());
        assertValue(feeScheduleAuthority, subject.feeScheduleAuthority());
        assertValue(metadataAuthority, subject.metadataAuthority());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:enterprise.service.token.TokenCreateParams/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final TokenCreateParams subject = TokenCreateParams.builder()
                .name(nameValue())
                .symbol(symbolValue())
                .build();
        assertNull(subject.treasuryAccountId());
        assertValue(0, subject.decimals());
        assertValue(0L, subject.initialSupply());
        assertValue(TokenSupplyType.INFINITE, subject.supplyType());
        assertNull(subject.maxSupply());
        assertValue(false, subject.freezeDefault());
        assertNull(subject.tokenMemo());
        assertValue(new byte[0], subject.metadata());
        assertNull(subject.expirationTime());
        assertNull(subject.autoRenewPeriod());
        assertNull(subject.autoRenewAccount());
        assertNull(subject.adminAuthority());
        assertNull(subject.supplyAuthority());
        assertNull(subject.kycAuthority());
        assertNull(subject.freezeAuthority());
        assertNull(subject.wipeAuthority());
        assertNull(subject.pauseAuthority());
        assertNull(subject.feeScheduleAuthority());
        assertNull(subject.metadataAuthority());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:enterprise.service.token.TokenCreateParams/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> TokenCreateParams.builder().build());
    }

    @Test
    @DisplayName("rejects null for name")
    @Tag("testId:enterprise.service.token.TokenCreateParams/name/rejects-null")
    void shouldRejectNullName() {
        assertThrows(NullPointerException.class, () -> new TokenCreateParams((String) null, symbolValue(), treasuryAccountIdValue(), decimalsValue(), initialSupplyValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue()));
    }

    @Test
    @DisplayName("name: accepts more elements than the maximum")
    @Tag("testId:enterprise.service.token.TokenCreateParams/name/accepts-at-maximum-bytes")
    void shouldAcceptNameAtMaximumBytes() {
        final String value = "ääääääääääääääääääääääääääääääääääääääääääääääääää";
        final TokenCreateParams subject = new TokenCreateParams(value, symbolValue(), treasuryAccountIdValue(), decimalsValue(), initialSupplyValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue());
        assertValue(value, subject.name());
    }

    @Test
    @DisplayName("name: rejects more elements than the maximum")
    @Tag("testId:enterprise.service.token.TokenCreateParams/name/rejects-above-maximum-bytes")
    void shouldRejectNameAboveMaximumBytes() {
        assertThrows(IllegalArgumentException.class, () -> new TokenCreateParams("ääääääääääääääääääääääääääääääääääääääääääääääääääa", symbolValue(), treasuryAccountIdValue(), decimalsValue(), initialSupplyValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue()));
    }

    @Test
    @DisplayName("rejects null for symbol")
    @Tag("testId:enterprise.service.token.TokenCreateParams/symbol/rejects-null")
    void shouldRejectNullSymbol() {
        assertThrows(NullPointerException.class, () -> new TokenCreateParams(nameValue(), (String) null, treasuryAccountIdValue(), decimalsValue(), initialSupplyValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue()));
    }

    @Test
    @DisplayName("symbol: accepts more elements than the maximum")
    @Tag("testId:enterprise.service.token.TokenCreateParams/symbol/accepts-at-maximum-bytes")
    void shouldAcceptSymbolAtMaximumBytes() {
        final String value = "ääääääääääääääääääääääääääääääääääääääääääääääääää";
        final TokenCreateParams subject = new TokenCreateParams(nameValue(), value, treasuryAccountIdValue(), decimalsValue(), initialSupplyValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue());
        assertValue(value, subject.symbol());
    }

    @Test
    @DisplayName("symbol: rejects more elements than the maximum")
    @Tag("testId:enterprise.service.token.TokenCreateParams/symbol/rejects-above-maximum-bytes")
    void shouldRejectSymbolAboveMaximumBytes() {
        assertThrows(IllegalArgumentException.class, () -> new TokenCreateParams(nameValue(), "ääääääääääääääääääääääääääääääääääääääääääääääääääa", treasuryAccountIdValue(), decimalsValue(), initialSupplyValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue()));
    }

    @Test
    @DisplayName("decimals: accepts the minimum (-2147483648)")
    @Tag("testId:enterprise.service.token.TokenCreateParams/decimals/accepts-at-minimum")
    void shouldAcceptDecimalsAtMinimum() {
        final int value = Integer.MIN_VALUE;
        final TokenCreateParams subject = new TokenCreateParams(nameValue(), symbolValue(), treasuryAccountIdValue(), value, initialSupplyValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue());
        assertValue(value, subject.decimals());
    }

    @Test
    @DisplayName("decimals: accepts the maximum (2147483647)")
    @Tag("testId:enterprise.service.token.TokenCreateParams/decimals/accepts-at-maximum")
    void shouldAcceptDecimalsAtMaximum() {
        final int value = 2147483647;
        final TokenCreateParams subject = new TokenCreateParams(nameValue(), symbolValue(), treasuryAccountIdValue(), value, initialSupplyValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue());
        assertValue(value, subject.decimals());
    }

    @Test
    @DisplayName("initialSupply: accepts the minimum (-9223372036854775808)")
    @Tag("testId:enterprise.service.token.TokenCreateParams/initialSupply/accepts-at-minimum")
    void shouldAcceptInitialSupplyAtMinimum() {
        final long value = -9223372036854775808L;
        final TokenCreateParams subject = new TokenCreateParams(nameValue(), symbolValue(), treasuryAccountIdValue(), decimalsValue(), value, supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue());
        assertValue(value, subject.initialSupply());
    }

    @Test
    @DisplayName("initialSupply: accepts the maximum (9223372036854775807)")
    @Tag("testId:enterprise.service.token.TokenCreateParams/initialSupply/accepts-at-maximum")
    void shouldAcceptInitialSupplyAtMaximum() {
        final long value = 9223372036854775807L;
        final TokenCreateParams subject = new TokenCreateParams(nameValue(), symbolValue(), treasuryAccountIdValue(), decimalsValue(), value, supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue());
        assertValue(value, subject.initialSupply());
    }

    @Test
    @DisplayName("rejects null for supplyType")
    @Tag("testId:enterprise.service.token.TokenCreateParams/supplyType/rejects-null")
    void shouldRejectNullSupplyType() {
        assertThrows(NullPointerException.class, () -> new TokenCreateParams(nameValue(), symbolValue(), treasuryAccountIdValue(), decimalsValue(), initialSupplyValue(), (TokenSupplyType) null, maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue()));
    }

    @Test
    @DisplayName("maxSupply: accepts the minimum (1)")
    @Tag("testId:enterprise.service.token.TokenCreateParams/maxSupply/accepts-at-minimum")
    void shouldAcceptMaxSupplyAtMinimum() {
        final @Nullable Long value = 1L;
        final TokenCreateParams subject = new TokenCreateParams(nameValue(), symbolValue(), treasuryAccountIdValue(), decimalsValue(), initialSupplyValue(), supplyTypeValue(), value, freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue());
        assertValue(value, subject.maxSupply());
    }

    @Test
    @DisplayName("maxSupply: accepts the maximum (9223372036854775807)")
    @Tag("testId:enterprise.service.token.TokenCreateParams/maxSupply/accepts-at-maximum")
    void shouldAcceptMaxSupplyAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final TokenCreateParams subject = new TokenCreateParams(nameValue(), symbolValue(), treasuryAccountIdValue(), decimalsValue(), initialSupplyValue(), supplyTypeValue(), value, freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue());
        assertValue(value, subject.maxSupply());
    }

    @Test
    @DisplayName("maxSupply: rejects a value below the minimum (0)")
    @Tag("testId:enterprise.service.token.TokenCreateParams/maxSupply/rejects-below-minimum")
    void shouldRejectMaxSupplyBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new TokenCreateParams(nameValue(), symbolValue(), treasuryAccountIdValue(), decimalsValue(), initialSupplyValue(), supplyTypeValue(), 0L, freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue()));
    }

    @Test
    @DisplayName("tokenMemo: accepts more elements than the maximum")
    @Tag("testId:enterprise.service.token.TokenCreateParams/tokenMemo/accepts-at-maximum-bytes")
    void shouldAcceptTokenMemoAtMaximumBytes() {
        final @Nullable String value = "ääääääääääääääääääääääääääääääääääääääääääääääääää";
        final TokenCreateParams subject = new TokenCreateParams(nameValue(), symbolValue(), treasuryAccountIdValue(), decimalsValue(), initialSupplyValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), value, metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue());
        assertValue(value, subject.tokenMemo());
    }

    @Test
    @DisplayName("tokenMemo: rejects more elements than the maximum")
    @Tag("testId:enterprise.service.token.TokenCreateParams/tokenMemo/rejects-above-maximum-bytes")
    void shouldRejectTokenMemoAboveMaximumBytes() {
        assertThrows(IllegalArgumentException.class, () -> new TokenCreateParams(nameValue(), symbolValue(), treasuryAccountIdValue(), decimalsValue(), initialSupplyValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), "ääääääääääääääääääääääääääääääääääääääääääääääääääa", metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue()));
    }

    @Test
    @DisplayName("rejects null for metadata")
    @Tag("testId:enterprise.service.token.TokenCreateParams/metadata/rejects-null")
    void shouldRejectNullMetadata() {
        assertThrows(NullPointerException.class, () -> new TokenCreateParams(nameValue(), symbolValue(), treasuryAccountIdValue(), decimalsValue(), initialSupplyValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), (byte[]) null, expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue()));
    }

    @Test
    @DisplayName("metadata: accepts the maximum number of elements")
    @Tag("testId:enterprise.service.token.TokenCreateParams/metadata/accepts-at-maximum-size")
    void shouldAcceptMetadataAtMaximumSize() {
        final byte[] value = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100};
        final TokenCreateParams subject = new TokenCreateParams(nameValue(), symbolValue(), treasuryAccountIdValue(), decimalsValue(), initialSupplyValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), value, expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue());
        assertValue(value, subject.metadata());
    }

    @Test
    @DisplayName("metadata: rejects more elements than the maximum")
    @Tag("testId:enterprise.service.token.TokenCreateParams/metadata/rejects-above-maximum-size")
    void shouldRejectMetadataAboveMaximumSize() {
        assertThrows(IllegalArgumentException.class, () -> new TokenCreateParams(nameValue(), symbolValue(), treasuryAccountIdValue(), decimalsValue(), initialSupplyValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 1}, expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue()));
    }

    @Test
    @DisplayName("the constructor copies metadata; the accessor returns a copy")
    @Tag("testId:enterprise.service.token.TokenCreateParams/metadata/copies-in")
    void shouldCopyMetadataIn() {
        final byte[] value = metadataValue();
        final byte[] expected = value.clone();
        final TokenCreateParams subject = new TokenCreateParams(nameValue(), symbolValue(), treasuryAccountIdValue(), decimalsValue(), initialSupplyValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), value, expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.metadata());
        final byte[] returned = subject.metadata();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.metadata());
    }

    private static String nameValue() {
        return "value";
    }

    private static String symbolValue() {
        return "value";
    }

    private static AccountId treasuryAccountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static int decimalsValue() {
        return 1;
    }

    private static long initialSupplyValue() {
        return 1L;
    }

    private static TokenSupplyType supplyTypeValue() {
        return TokenSupplyType.INFINITE;
    }

    private static long maxSupplyValue() {
        return 1L;
    }

    private static boolean freezeDefaultValue() {
        return true;
    }

    private static String tokenMemoValue() {
        return "value";
    }

    private static byte[] metadataValue() {
        return new byte[] {1, 2, 3};
    }

    private static ZonedDateTime expirationTimeValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static Duration autoRenewPeriodValue() {
        return Duration.ofSeconds(1L);
    }

    private static AccountId autoRenewAccountValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static Authority adminAuthorityValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static Authority supplyAuthorityValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static Authority kycAuthorityValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static Authority freezeAuthorityValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static Authority wipeAuthorityValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static Authority pauseAuthorityValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static Authority feeScheduleAuthorityValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static Authority metadataAuthorityValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static TokenCreateParams create() {
        return new TokenCreateParams(nameValue(), symbolValue(), treasuryAccountIdValue(), decimalsValue(), initialSupplyValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue());
    }

    private static TokenCreateParams defaultSubject() {
        return new TokenCreateParams("Token Name", "TKN", null, 0, 0L, TokenSupplyType.INFINITE, null, false, null, new byte[0], null, null, null, null, null, null, null, null, null, null, null);
    }
}
