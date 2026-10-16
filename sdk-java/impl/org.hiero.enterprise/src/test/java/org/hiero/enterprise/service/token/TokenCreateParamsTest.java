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
import org.junit.jupiter.api.Test;

/// Tests of `TokenCreateParams`, generated from the specs.
@DisplayName("TokenCreateParams")
class TokenCreateParamsTest {

    @Test
    @DisplayName("creates a TokenCreateParams and returns the values")
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
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> TokenCreateParams.builder().build());
    }

    @Test
    @DisplayName("rejects null for name")
    void shouldRejectNullName() {
        assertThrows(NullPointerException.class, () -> new TokenCreateParams((String) null, symbolValue(), treasuryAccountIdValue(), decimalsValue(), initialSupplyValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue()));
    }

    @Test
    @DisplayName("name: accepts a value with the maximum length")
    void shouldAcceptNameAtMaximumLength() {
        final String value = "valueaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        final TokenCreateParams subject = new TokenCreateParams(value, symbolValue(), treasuryAccountIdValue(), decimalsValue(), initialSupplyValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue());
        assertValue(value, subject.name());
    }

    @Test
    @DisplayName("name: rejects a value longer than the maximum length")
    void shouldRejectNameAboveMaximumLength() {
        assertThrows(IllegalArgumentException.class, () -> new TokenCreateParams("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", symbolValue(), treasuryAccountIdValue(), decimalsValue(), initialSupplyValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue()));
    }

    @Test
    @DisplayName("rejects null for symbol")
    void shouldRejectNullSymbol() {
        assertThrows(NullPointerException.class, () -> new TokenCreateParams(nameValue(), (String) null, treasuryAccountIdValue(), decimalsValue(), initialSupplyValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue()));
    }

    @Test
    @DisplayName("symbol: accepts a value with the maximum length")
    void shouldAcceptSymbolAtMaximumLength() {
        final String value = "valueaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        final TokenCreateParams subject = new TokenCreateParams(nameValue(), value, treasuryAccountIdValue(), decimalsValue(), initialSupplyValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue());
        assertValue(value, subject.symbol());
    }

    @Test
    @DisplayName("symbol: rejects a value longer than the maximum length")
    void shouldRejectSymbolAboveMaximumLength() {
        assertThrows(IllegalArgumentException.class, () -> new TokenCreateParams(nameValue(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", treasuryAccountIdValue(), decimalsValue(), initialSupplyValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue()));
    }

    @Test
    @DisplayName("decimals: accepts the minimum (-2147483648)")
    void shouldAcceptDecimalsAtMinimum() {
        final int value = Integer.MIN_VALUE;
        final TokenCreateParams subject = new TokenCreateParams(nameValue(), symbolValue(), treasuryAccountIdValue(), value, initialSupplyValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue());
        assertValue(value, subject.decimals());
    }

    @Test
    @DisplayName("decimals: accepts the maximum (2147483647)")
    void shouldAcceptDecimalsAtMaximum() {
        final int value = 2147483647;
        final TokenCreateParams subject = new TokenCreateParams(nameValue(), symbolValue(), treasuryAccountIdValue(), value, initialSupplyValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue());
        assertValue(value, subject.decimals());
    }

    @Test
    @DisplayName("initialSupply: accepts the minimum (-9223372036854775808)")
    void shouldAcceptInitialSupplyAtMinimum() {
        final long value = -9223372036854775808L;
        final TokenCreateParams subject = new TokenCreateParams(nameValue(), symbolValue(), treasuryAccountIdValue(), decimalsValue(), value, supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue());
        assertValue(value, subject.initialSupply());
    }

    @Test
    @DisplayName("initialSupply: accepts the maximum (9223372036854775807)")
    void shouldAcceptInitialSupplyAtMaximum() {
        final long value = 9223372036854775807L;
        final TokenCreateParams subject = new TokenCreateParams(nameValue(), symbolValue(), treasuryAccountIdValue(), decimalsValue(), value, supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue());
        assertValue(value, subject.initialSupply());
    }

    @Test
    @DisplayName("rejects null for supplyType")
    void shouldRejectNullSupplyType() {
        assertThrows(NullPointerException.class, () -> new TokenCreateParams(nameValue(), symbolValue(), treasuryAccountIdValue(), decimalsValue(), initialSupplyValue(), (TokenSupplyType) null, maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue()));
    }

    @Test
    @DisplayName("maxSupply: accepts the minimum (1)")
    void shouldAcceptMaxSupplyAtMinimum() {
        final @Nullable Long value = 1L;
        final TokenCreateParams subject = new TokenCreateParams(nameValue(), symbolValue(), treasuryAccountIdValue(), decimalsValue(), initialSupplyValue(), supplyTypeValue(), value, freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue());
        assertValue(value, subject.maxSupply());
    }

    @Test
    @DisplayName("maxSupply: accepts the maximum (9223372036854775807)")
    void shouldAcceptMaxSupplyAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final TokenCreateParams subject = new TokenCreateParams(nameValue(), symbolValue(), treasuryAccountIdValue(), decimalsValue(), initialSupplyValue(), supplyTypeValue(), value, freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue());
        assertValue(value, subject.maxSupply());
    }

    @Test
    @DisplayName("maxSupply: rejects a value below the minimum (0)")
    void shouldRejectMaxSupplyBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new TokenCreateParams(nameValue(), symbolValue(), treasuryAccountIdValue(), decimalsValue(), initialSupplyValue(), supplyTypeValue(), 0L, freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue()));
    }

    @Test
    @DisplayName("tokenMemo: accepts a value with the maximum length")
    void shouldAcceptTokenMemoAtMaximumLength() {
        final @Nullable String value = "valueaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        final TokenCreateParams subject = new TokenCreateParams(nameValue(), symbolValue(), treasuryAccountIdValue(), decimalsValue(), initialSupplyValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), value, metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue());
        assertValue(value, subject.tokenMemo());
    }

    @Test
    @DisplayName("tokenMemo: rejects a value longer than the maximum length")
    void shouldRejectTokenMemoAboveMaximumLength() {
        assertThrows(IllegalArgumentException.class, () -> new TokenCreateParams(nameValue(), symbolValue(), treasuryAccountIdValue(), decimalsValue(), initialSupplyValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue()));
    }

    @Test
    @DisplayName("rejects null for metadata")
    void shouldRejectNullMetadata() {
        assertThrows(NullPointerException.class, () -> new TokenCreateParams(nameValue(), symbolValue(), treasuryAccountIdValue(), decimalsValue(), initialSupplyValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), (byte[]) null, expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue()));
    }

    @Test
    @DisplayName("the constructor copies metadata; the accessor returns a copy")
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
