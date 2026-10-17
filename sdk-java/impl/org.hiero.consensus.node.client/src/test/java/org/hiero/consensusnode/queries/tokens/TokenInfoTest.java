package org.hiero.consensusnode.queries.tokens;

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
import org.hiero.ledger.Address;
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
    @Tag("testId:consensusnode.queries.tokens.TokenInfo/create")
    void shouldCreateAndReturnTheValues() {
        final Address tokenId = tokenIdValue();
        final TokenType type = typeValue();
        final String name = nameValue();
        final String symbol = symbolValue();
        final int decimals = decimalsValue();
        final long totalSupply = totalSupplyValue();
        final AccountId treasuryAccountId = treasuryAccountIdValue();
        final TokenSupplyType supplyType = supplyTypeValue();
        final @Nullable Long maxSupply = maxSupplyValue();
        final @Nullable String tokenMemo = tokenMemoValue();
        final byte[] metadata = metadataValue();
        final ZonedDateTime expirationTime = expirationTimeValue();
        final @Nullable Duration autoRenewPeriod = autoRenewPeriodValue();
        final @Nullable AccountId autoRenewAccount = autoRenewAccountValue();
        final boolean deleted = deletedValue();
        final @Nullable Authority adminAuthority = adminAuthorityValue();
        final @Nullable Authority supplyAuthority = supplyAuthorityValue();
        final @Nullable Authority kycAuthority = kycAuthorityValue();
        final @Nullable Authority freezeAuthority = freezeAuthorityValue();
        final @Nullable Authority wipeAuthority = wipeAuthorityValue();
        final @Nullable Authority pauseAuthority = pauseAuthorityValue();
        final @Nullable Authority feeScheduleAuthority = feeScheduleAuthorityValue();
        final @Nullable Authority metadataAuthority = metadataAuthorityValue();
        final @Nullable Boolean defaultFreezeStatus = defaultFreezeStatusValue();
        final @Nullable Boolean defaultKycStatus = defaultKycStatusValue();
        final @Nullable Boolean paused = pausedValue();
        final TokenInfo subject = new TokenInfo(tokenId, type, name, symbol, decimals, totalSupply, treasuryAccountId, supplyType, maxSupply, tokenMemo, metadata, expirationTime, autoRenewPeriod, autoRenewAccount, deleted, adminAuthority, supplyAuthority, kycAuthority, freezeAuthority, wipeAuthority, pauseAuthority, feeScheduleAuthority, metadataAuthority, defaultFreezeStatus, defaultKycStatus, paused);
        assertValue(tokenId, subject.tokenId());
        assertValue(type, subject.type());
        assertValue(name, subject.name());
        assertValue(symbol, subject.symbol());
        assertValue(decimals, subject.decimals());
        assertValue(totalSupply, subject.totalSupply());
        assertValue(treasuryAccountId, subject.treasuryAccountId());
        assertValue(supplyType, subject.supplyType());
        assertValue(maxSupply, subject.maxSupply());
        assertValue(tokenMemo, subject.tokenMemo());
        assertValue(metadata, subject.metadata());
        assertValue(expirationTime, subject.expirationTime());
        assertValue(autoRenewPeriod, subject.autoRenewPeriod());
        assertValue(autoRenewAccount, subject.autoRenewAccount());
        assertValue(deleted, subject.deleted());
        assertValue(adminAuthority, subject.adminAuthority());
        assertValue(supplyAuthority, subject.supplyAuthority());
        assertValue(kycAuthority, subject.kycAuthority());
        assertValue(freezeAuthority, subject.freezeAuthority());
        assertValue(wipeAuthority, subject.wipeAuthority());
        assertValue(pauseAuthority, subject.pauseAuthority());
        assertValue(feeScheduleAuthority, subject.feeScheduleAuthority());
        assertValue(metadataAuthority, subject.metadataAuthority());
        assertValue(defaultFreezeStatus, subject.defaultFreezeStatus());
        assertValue(defaultKycStatus, subject.defaultKycStatus());
        assertValue(paused, subject.paused());
    }

    @Test
    @DisplayName("creates a TokenInfo with the default values")
    @Tag("testId:consensusnode.queries.tokens.TokenInfo/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final TokenInfo subject = new TokenInfo(tokenIdValue(), typeValue(), nameValue(), symbolValue(), totalSupplyValue(), treasuryAccountIdValue(), supplyTypeValue(), maxSupplyValue(), tokenMemoValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue(), defaultFreezeStatusValue(), defaultKycStatusValue(), pausedValue());
        assertValue(0, subject.decimals());
        assertValue(new byte[0], subject.metadata());
        assertValue(false, subject.deleted());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:consensusnode.queries.tokens.TokenInfo/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final Address tokenId = tokenIdValue();
        final TokenType type = typeValue();
        final String name = nameValue();
        final String symbol = symbolValue();
        final int decimals = decimalsValue();
        final long totalSupply = totalSupplyValue();
        final AccountId treasuryAccountId = treasuryAccountIdValue();
        final TokenSupplyType supplyType = supplyTypeValue();
        final @Nullable Long maxSupply = maxSupplyValue();
        final @Nullable String tokenMemo = tokenMemoValue();
        final byte[] metadata = metadataValue();
        final ZonedDateTime expirationTime = expirationTimeValue();
        final @Nullable Duration autoRenewPeriod = autoRenewPeriodValue();
        final @Nullable AccountId autoRenewAccount = autoRenewAccountValue();
        final boolean deleted = deletedValue();
        final @Nullable Authority adminAuthority = adminAuthorityValue();
        final @Nullable Authority supplyAuthority = supplyAuthorityValue();
        final @Nullable Authority kycAuthority = kycAuthorityValue();
        final @Nullable Authority freezeAuthority = freezeAuthorityValue();
        final @Nullable Authority wipeAuthority = wipeAuthorityValue();
        final @Nullable Authority pauseAuthority = pauseAuthorityValue();
        final @Nullable Authority feeScheduleAuthority = feeScheduleAuthorityValue();
        final @Nullable Authority metadataAuthority = metadataAuthorityValue();
        final @Nullable Boolean defaultFreezeStatus = defaultFreezeStatusValue();
        final @Nullable Boolean defaultKycStatus = defaultKycStatusValue();
        final @Nullable Boolean paused = pausedValue();
        final TokenInfo subject = TokenInfo.builder()
                .tokenId(tokenId)
                .type(type)
                .name(name)
                .symbol(symbol)
                .decimals(decimals)
                .totalSupply(totalSupply)
                .treasuryAccountId(treasuryAccountId)
                .supplyType(supplyType)
                .maxSupply(maxSupply)
                .tokenMemo(tokenMemo)
                .metadata(metadata)
                .expirationTime(expirationTime)
                .autoRenewPeriod(autoRenewPeriod)
                .autoRenewAccount(autoRenewAccount)
                .deleted(deleted)
                .adminAuthority(adminAuthority)
                .supplyAuthority(supplyAuthority)
                .kycAuthority(kycAuthority)
                .freezeAuthority(freezeAuthority)
                .wipeAuthority(wipeAuthority)
                .pauseAuthority(pauseAuthority)
                .feeScheduleAuthority(feeScheduleAuthority)
                .metadataAuthority(metadataAuthority)
                .defaultFreezeStatus(defaultFreezeStatus)
                .defaultKycStatus(defaultKycStatus)
                .paused(paused)
                .build();
        assertValue(tokenId, subject.tokenId());
        assertValue(type, subject.type());
        assertValue(name, subject.name());
        assertValue(symbol, subject.symbol());
        assertValue(decimals, subject.decimals());
        assertValue(totalSupply, subject.totalSupply());
        assertValue(treasuryAccountId, subject.treasuryAccountId());
        assertValue(supplyType, subject.supplyType());
        assertValue(maxSupply, subject.maxSupply());
        assertValue(tokenMemo, subject.tokenMemo());
        assertValue(metadata, subject.metadata());
        assertValue(expirationTime, subject.expirationTime());
        assertValue(autoRenewPeriod, subject.autoRenewPeriod());
        assertValue(autoRenewAccount, subject.autoRenewAccount());
        assertValue(deleted, subject.deleted());
        assertValue(adminAuthority, subject.adminAuthority());
        assertValue(supplyAuthority, subject.supplyAuthority());
        assertValue(kycAuthority, subject.kycAuthority());
        assertValue(freezeAuthority, subject.freezeAuthority());
        assertValue(wipeAuthority, subject.wipeAuthority());
        assertValue(pauseAuthority, subject.pauseAuthority());
        assertValue(feeScheduleAuthority, subject.feeScheduleAuthority());
        assertValue(metadataAuthority, subject.metadataAuthority());
        assertValue(defaultFreezeStatus, subject.defaultFreezeStatus());
        assertValue(defaultKycStatus, subject.defaultKycStatus());
        assertValue(paused, subject.paused());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:consensusnode.queries.tokens.TokenInfo/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final TokenInfo subject = TokenInfo.builder()
                .tokenId(tokenIdValue())
                .type(typeValue())
                .name(nameValue())
                .symbol(symbolValue())
                .totalSupply(totalSupplyValue())
                .treasuryAccountId(treasuryAccountIdValue())
                .supplyType(supplyTypeValue())
                .expirationTime(expirationTimeValue())
                .build();
        assertValue(0, subject.decimals());
        assertNull(subject.maxSupply());
        assertNull(subject.tokenMemo());
        assertValue(new byte[0], subject.metadata());
        assertNull(subject.autoRenewPeriod());
        assertNull(subject.autoRenewAccount());
        assertValue(false, subject.deleted());
        assertNull(subject.adminAuthority());
        assertNull(subject.supplyAuthority());
        assertNull(subject.kycAuthority());
        assertNull(subject.freezeAuthority());
        assertNull(subject.wipeAuthority());
        assertNull(subject.pauseAuthority());
        assertNull(subject.feeScheduleAuthority());
        assertNull(subject.metadataAuthority());
        assertNull(subject.defaultFreezeStatus());
        assertNull(subject.defaultKycStatus());
        assertNull(subject.paused());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:consensusnode.queries.tokens.TokenInfo/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> TokenInfo.builder().build());
    }

    @Test
    @DisplayName("rejects null for tokenId")
    @Tag("testId:consensusnode.queries.tokens.TokenInfo/tokenId/rejects-null")
    void shouldRejectNullTokenId() {
        assertThrows(NullPointerException.class, () -> new TokenInfo((Address) null, typeValue(), nameValue(), symbolValue(), decimalsValue(), totalSupplyValue(), treasuryAccountIdValue(), supplyTypeValue(), maxSupplyValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), deletedValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue(), defaultFreezeStatusValue(), defaultKycStatusValue(), pausedValue()));
    }

    @Test
    @DisplayName("rejects null for type")
    @Tag("testId:consensusnode.queries.tokens.TokenInfo/type/rejects-null")
    void shouldRejectNullType() {
        assertThrows(NullPointerException.class, () -> new TokenInfo(tokenIdValue(), (TokenType) null, nameValue(), symbolValue(), decimalsValue(), totalSupplyValue(), treasuryAccountIdValue(), supplyTypeValue(), maxSupplyValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), deletedValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue(), defaultFreezeStatusValue(), defaultKycStatusValue(), pausedValue()));
    }

    @Test
    @DisplayName("rejects null for name")
    @Tag("testId:consensusnode.queries.tokens.TokenInfo/name/rejects-null")
    void shouldRejectNullName() {
        assertThrows(NullPointerException.class, () -> new TokenInfo(tokenIdValue(), typeValue(), (String) null, symbolValue(), decimalsValue(), totalSupplyValue(), treasuryAccountIdValue(), supplyTypeValue(), maxSupplyValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), deletedValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue(), defaultFreezeStatusValue(), defaultKycStatusValue(), pausedValue()));
    }

    @Test
    @DisplayName("name: accepts more elements than the maximum")
    @Tag("testId:consensusnode.queries.tokens.TokenInfo/name/accepts-at-maximum-bytes")
    void shouldAcceptNameAtMaximumBytes() {
        final String value = "ääääääääääääääääääääääääääääääääääääääääääääääääää";
        final TokenInfo subject = new TokenInfo(tokenIdValue(), typeValue(), value, symbolValue(), decimalsValue(), totalSupplyValue(), treasuryAccountIdValue(), supplyTypeValue(), maxSupplyValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), deletedValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue(), defaultFreezeStatusValue(), defaultKycStatusValue(), pausedValue());
        assertValue(value, subject.name());
    }

    @Test
    @DisplayName("name: rejects more elements than the maximum")
    @Tag("testId:consensusnode.queries.tokens.TokenInfo/name/rejects-above-maximum-bytes")
    void shouldRejectNameAboveMaximumBytes() {
        assertThrows(IllegalArgumentException.class, () -> new TokenInfo(tokenIdValue(), typeValue(), "ääääääääääääääääääääääääääääääääääääääääääääääääääa", symbolValue(), decimalsValue(), totalSupplyValue(), treasuryAccountIdValue(), supplyTypeValue(), maxSupplyValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), deletedValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue(), defaultFreezeStatusValue(), defaultKycStatusValue(), pausedValue()));
    }

    @Test
    @DisplayName("rejects null for symbol")
    @Tag("testId:consensusnode.queries.tokens.TokenInfo/symbol/rejects-null")
    void shouldRejectNullSymbol() {
        assertThrows(NullPointerException.class, () -> new TokenInfo(tokenIdValue(), typeValue(), nameValue(), (String) null, decimalsValue(), totalSupplyValue(), treasuryAccountIdValue(), supplyTypeValue(), maxSupplyValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), deletedValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue(), defaultFreezeStatusValue(), defaultKycStatusValue(), pausedValue()));
    }

    @Test
    @DisplayName("symbol: accepts more elements than the maximum")
    @Tag("testId:consensusnode.queries.tokens.TokenInfo/symbol/accepts-at-maximum-bytes")
    void shouldAcceptSymbolAtMaximumBytes() {
        final String value = "ääääääääääääääääääääääääääääääääääääääääääääääääää";
        final TokenInfo subject = new TokenInfo(tokenIdValue(), typeValue(), nameValue(), value, decimalsValue(), totalSupplyValue(), treasuryAccountIdValue(), supplyTypeValue(), maxSupplyValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), deletedValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue(), defaultFreezeStatusValue(), defaultKycStatusValue(), pausedValue());
        assertValue(value, subject.symbol());
    }

    @Test
    @DisplayName("symbol: rejects more elements than the maximum")
    @Tag("testId:consensusnode.queries.tokens.TokenInfo/symbol/rejects-above-maximum-bytes")
    void shouldRejectSymbolAboveMaximumBytes() {
        assertThrows(IllegalArgumentException.class, () -> new TokenInfo(tokenIdValue(), typeValue(), nameValue(), "ääääääääääääääääääääääääääääääääääääääääääääääääääa", decimalsValue(), totalSupplyValue(), treasuryAccountIdValue(), supplyTypeValue(), maxSupplyValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), deletedValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue(), defaultFreezeStatusValue(), defaultKycStatusValue(), pausedValue()));
    }

    @Test
    @DisplayName("decimals: accepts the minimum (-2147483648)")
    @Tag("testId:consensusnode.queries.tokens.TokenInfo/decimals/accepts-at-minimum")
    void shouldAcceptDecimalsAtMinimum() {
        final int value = Integer.MIN_VALUE;
        final TokenInfo subject = new TokenInfo(tokenIdValue(), typeValue(), nameValue(), symbolValue(), value, totalSupplyValue(), treasuryAccountIdValue(), supplyTypeValue(), maxSupplyValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), deletedValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue(), defaultFreezeStatusValue(), defaultKycStatusValue(), pausedValue());
        assertValue(value, subject.decimals());
    }

    @Test
    @DisplayName("decimals: accepts the maximum (2147483647)")
    @Tag("testId:consensusnode.queries.tokens.TokenInfo/decimals/accepts-at-maximum")
    void shouldAcceptDecimalsAtMaximum() {
        final int value = 2147483647;
        final TokenInfo subject = new TokenInfo(tokenIdValue(), typeValue(), nameValue(), symbolValue(), value, totalSupplyValue(), treasuryAccountIdValue(), supplyTypeValue(), maxSupplyValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), deletedValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue(), defaultFreezeStatusValue(), defaultKycStatusValue(), pausedValue());
        assertValue(value, subject.decimals());
    }

    @Test
    @DisplayName("totalSupply: accepts the minimum (-9223372036854775808)")
    @Tag("testId:consensusnode.queries.tokens.TokenInfo/totalSupply/accepts-at-minimum")
    void shouldAcceptTotalSupplyAtMinimum() {
        final long value = -9223372036854775808L;
        final TokenInfo subject = new TokenInfo(tokenIdValue(), typeValue(), nameValue(), symbolValue(), decimalsValue(), value, treasuryAccountIdValue(), supplyTypeValue(), maxSupplyValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), deletedValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue(), defaultFreezeStatusValue(), defaultKycStatusValue(), pausedValue());
        assertValue(value, subject.totalSupply());
    }

    @Test
    @DisplayName("totalSupply: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.queries.tokens.TokenInfo/totalSupply/accepts-at-maximum")
    void shouldAcceptTotalSupplyAtMaximum() {
        final long value = 9223372036854775807L;
        final TokenInfo subject = new TokenInfo(tokenIdValue(), typeValue(), nameValue(), symbolValue(), decimalsValue(), value, treasuryAccountIdValue(), supplyTypeValue(), maxSupplyValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), deletedValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue(), defaultFreezeStatusValue(), defaultKycStatusValue(), pausedValue());
        assertValue(value, subject.totalSupply());
    }

    @Test
    @DisplayName("rejects null for treasuryAccountId")
    @Tag("testId:consensusnode.queries.tokens.TokenInfo/treasuryAccountId/rejects-null")
    void shouldRejectNullTreasuryAccountId() {
        assertThrows(NullPointerException.class, () -> new TokenInfo(tokenIdValue(), typeValue(), nameValue(), symbolValue(), decimalsValue(), totalSupplyValue(), (AccountId) null, supplyTypeValue(), maxSupplyValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), deletedValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue(), defaultFreezeStatusValue(), defaultKycStatusValue(), pausedValue()));
    }

    @Test
    @DisplayName("rejects null for supplyType")
    @Tag("testId:consensusnode.queries.tokens.TokenInfo/supplyType/rejects-null")
    void shouldRejectNullSupplyType() {
        assertThrows(NullPointerException.class, () -> new TokenInfo(tokenIdValue(), typeValue(), nameValue(), symbolValue(), decimalsValue(), totalSupplyValue(), treasuryAccountIdValue(), (TokenSupplyType) null, maxSupplyValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), deletedValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue(), defaultFreezeStatusValue(), defaultKycStatusValue(), pausedValue()));
    }

    @Test
    @DisplayName("maxSupply: accepts the minimum (-9223372036854775808)")
    @Tag("testId:consensusnode.queries.tokens.TokenInfo/maxSupply/accepts-at-minimum")
    void shouldAcceptMaxSupplyAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final TokenInfo subject = new TokenInfo(tokenIdValue(), typeValue(), nameValue(), symbolValue(), decimalsValue(), totalSupplyValue(), treasuryAccountIdValue(), supplyTypeValue(), value, tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), deletedValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue(), defaultFreezeStatusValue(), defaultKycStatusValue(), pausedValue());
        assertValue(value, subject.maxSupply());
    }

    @Test
    @DisplayName("maxSupply: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.queries.tokens.TokenInfo/maxSupply/accepts-at-maximum")
    void shouldAcceptMaxSupplyAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final TokenInfo subject = new TokenInfo(tokenIdValue(), typeValue(), nameValue(), symbolValue(), decimalsValue(), totalSupplyValue(), treasuryAccountIdValue(), supplyTypeValue(), value, tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), deletedValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue(), defaultFreezeStatusValue(), defaultKycStatusValue(), pausedValue());
        assertValue(value, subject.maxSupply());
    }

    @Test
    @DisplayName("rejects null for metadata")
    @Tag("testId:consensusnode.queries.tokens.TokenInfo/metadata/rejects-null")
    void shouldRejectNullMetadata() {
        assertThrows(NullPointerException.class, () -> new TokenInfo(tokenIdValue(), typeValue(), nameValue(), symbolValue(), decimalsValue(), totalSupplyValue(), treasuryAccountIdValue(), supplyTypeValue(), maxSupplyValue(), tokenMemoValue(), (byte[]) null, expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), deletedValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue(), defaultFreezeStatusValue(), defaultKycStatusValue(), pausedValue()));
    }

    @Test
    @DisplayName("the constructor copies metadata; the accessor returns a copy")
    @Tag("testId:consensusnode.queries.tokens.TokenInfo/metadata/copies-in")
    void shouldCopyMetadataIn() {
        final byte[] value = metadataValue();
        final byte[] expected = value.clone();
        final TokenInfo subject = new TokenInfo(tokenIdValue(), typeValue(), nameValue(), symbolValue(), decimalsValue(), totalSupplyValue(), treasuryAccountIdValue(), supplyTypeValue(), maxSupplyValue(), tokenMemoValue(), value, expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), deletedValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue(), defaultFreezeStatusValue(), defaultKycStatusValue(), pausedValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.metadata());
        final byte[] returned = subject.metadata();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.metadata());
    }

    @Test
    @DisplayName("rejects null for expirationTime")
    @Tag("testId:consensusnode.queries.tokens.TokenInfo/expirationTime/rejects-null")
    void shouldRejectNullExpirationTime() {
        assertThrows(NullPointerException.class, () -> new TokenInfo(tokenIdValue(), typeValue(), nameValue(), symbolValue(), decimalsValue(), totalSupplyValue(), treasuryAccountIdValue(), supplyTypeValue(), maxSupplyValue(), tokenMemoValue(), metadataValue(), (ZonedDateTime) null, autoRenewPeriodValue(), autoRenewAccountValue(), deletedValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue(), defaultFreezeStatusValue(), defaultKycStatusValue(), pausedValue()));
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

    private static int decimalsValue() {
        return 1;
    }

    private static long totalSupplyValue() {
        return 1L;
    }

    private static AccountId treasuryAccountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static TokenSupplyType supplyTypeValue() {
        return TokenSupplyType.INFINITE;
    }

    private static long maxSupplyValue() {
        return 1L;
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

    private static boolean deletedValue() {
        return true;
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

    private static boolean defaultFreezeStatusValue() {
        return true;
    }

    private static boolean defaultKycStatusValue() {
        return true;
    }

    private static boolean pausedValue() {
        return true;
    }

    private static TokenInfo create() {
        return new TokenInfo(tokenIdValue(), typeValue(), nameValue(), symbolValue(), decimalsValue(), totalSupplyValue(), treasuryAccountIdValue(), supplyTypeValue(), maxSupplyValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), deletedValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue(), defaultFreezeStatusValue(), defaultKycStatusValue(), pausedValue());
    }
}
