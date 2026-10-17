package org.hiero.consensusnode.transactions.tokens;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.client.ClientFactory;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.client.TransactionSigner;
import org.hiero.hedera.Hbar;
import org.hiero.hedera.HbarUnit;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.ConsensusNode;
import org.hiero.ledger.IpAddress;
import org.hiero.ledger.MirrorNode;
import org.hiero.ledger.Network;
import org.hiero.ledger.config.NetworkSetting;
import org.hiero.nativeToken.NativeToken;
import org.hiero.token.TokenSupplyType;
import org.hiero.token.TokenType;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `TokenCreateTransaction`, generated from the specs.
@DisplayName("TokenCreateTransaction")
class TokenCreateTransactionTest {

    @Test
    @DisplayName("creates a TokenCreateTransaction and returns the values")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/create")
    void shouldCreateAndReturnTheValues() {
        final String name = nameValue();
        final String symbol = symbolValue();
        final TokenType tokenType = tokenTypeValue();
        final int decimals = decimalsValue();
        final long initialSupply = initialSupplyValue();
        final AccountId treasuryAccountId = treasuryAccountIdValue();
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
        final TokenCreateTransaction subject = new TokenCreateTransaction(name, symbol, tokenType, decimals, initialSupply, treasuryAccountId, supplyType, maxSupply, freezeDefault, tokenMemo, metadata, expirationTime, autoRenewPeriod, autoRenewAccount, adminAuthority, supplyAuthority, kycAuthority, freezeAuthority, wipeAuthority, pauseAuthority, feeScheduleAuthority, metadataAuthority);
        assertValue(name, subject.name());
        assertValue(symbol, subject.symbol());
        assertValue(tokenType, subject.tokenType());
        assertValue(decimals, subject.decimals());
        assertValue(initialSupply, subject.initialSupply());
        assertValue(treasuryAccountId, subject.treasuryAccountId());
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
        assertNull(subject.maxTransactionFee());
        assertNull(subject.validDuration());
        assertNull(subject.memo());
    }

    @Test
    @DisplayName("creates a TokenCreateTransaction with the default values")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final TokenCreateTransaction subject = new TokenCreateTransaction(nameValue(), symbolValue(), tokenTypeValue(), treasuryAccountIdValue(), maxSupplyValue(), tokenMemoValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue());
        assertValue(0, subject.decimals());
        assertValue(0L, subject.initialSupply());
        assertValue(TokenSupplyType.INFINITE, subject.supplyType());
        assertValue(false, subject.freezeDefault());
        assertValue(new byte[0], subject.metadata());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final String name = nameValue();
        final String symbol = symbolValue();
        final TokenType tokenType = tokenTypeValue();
        final int decimals = decimalsValue();
        final long initialSupply = initialSupplyValue();
        final AccountId treasuryAccountId = treasuryAccountIdValue();
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
        final TokenCreateTransaction subject = TokenCreateTransaction.builder()
                .name(name)
                .symbol(symbol)
                .tokenType(tokenType)
                .decimals(decimals)
                .initialSupply(initialSupply)
                .treasuryAccountId(treasuryAccountId)
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
        assertValue(tokenType, subject.tokenType());
        assertValue(decimals, subject.decimals());
        assertValue(initialSupply, subject.initialSupply());
        assertValue(treasuryAccountId, subject.treasuryAccountId());
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
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final TokenCreateTransaction subject = TokenCreateTransaction.builder()
                .name(nameValue())
                .symbol(symbolValue())
                .tokenType(tokenTypeValue())
                .treasuryAccountId(treasuryAccountIdValue())
                .build();
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
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> TokenCreateTransaction.builder().build());
    }

    @Test
    @DisplayName("rejects null for name")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/name/rejects-null")
    void shouldRejectNullName() {
        assertThrows(NullPointerException.class, () -> new TokenCreateTransaction((String) null, symbolValue(), tokenTypeValue(), decimalsValue(), initialSupplyValue(), treasuryAccountIdValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue()));
    }

    @Test
    @DisplayName("name: accepts more elements than the maximum")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/name/accepts-at-maximum-bytes")
    void shouldAcceptNameAtMaximumBytes() {
        final String value = "ääääääääääääääääääääääääääääääääääääääääääääääääää";
        final TokenCreateTransaction subject = new TokenCreateTransaction(value, symbolValue(), tokenTypeValue(), decimalsValue(), initialSupplyValue(), treasuryAccountIdValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue());
        assertValue(value, subject.name());
    }

    @Test
    @DisplayName("name: rejects more elements than the maximum")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/name/rejects-above-maximum-bytes")
    void shouldRejectNameAboveMaximumBytes() {
        assertThrows(IllegalArgumentException.class, () -> new TokenCreateTransaction("ääääääääääääääääääääääääääääääääääääääääääääääääääa", symbolValue(), tokenTypeValue(), decimalsValue(), initialSupplyValue(), treasuryAccountIdValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue()));
    }

    @Test
    @DisplayName("rejects null for symbol")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/symbol/rejects-null")
    void shouldRejectNullSymbol() {
        assertThrows(NullPointerException.class, () -> new TokenCreateTransaction(nameValue(), (String) null, tokenTypeValue(), decimalsValue(), initialSupplyValue(), treasuryAccountIdValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue()));
    }

    @Test
    @DisplayName("symbol: accepts more elements than the maximum")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/symbol/accepts-at-maximum-bytes")
    void shouldAcceptSymbolAtMaximumBytes() {
        final String value = "ääääääääääääääääääääääääääääääääääääääääääääääääää";
        final TokenCreateTransaction subject = new TokenCreateTransaction(nameValue(), value, tokenTypeValue(), decimalsValue(), initialSupplyValue(), treasuryAccountIdValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue());
        assertValue(value, subject.symbol());
    }

    @Test
    @DisplayName("symbol: rejects more elements than the maximum")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/symbol/rejects-above-maximum-bytes")
    void shouldRejectSymbolAboveMaximumBytes() {
        assertThrows(IllegalArgumentException.class, () -> new TokenCreateTransaction(nameValue(), "ääääääääääääääääääääääääääääääääääääääääääääääääääa", tokenTypeValue(), decimalsValue(), initialSupplyValue(), treasuryAccountIdValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue()));
    }

    @Test
    @DisplayName("rejects null for tokenType")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/tokenType/rejects-null")
    void shouldRejectNullTokenType() {
        assertThrows(NullPointerException.class, () -> new TokenCreateTransaction(nameValue(), symbolValue(), (TokenType) null, decimalsValue(), initialSupplyValue(), treasuryAccountIdValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue()));
    }

    @Test
    @DisplayName("decimals: accepts the minimum (-2147483648)")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/decimals/accepts-at-minimum")
    void shouldAcceptDecimalsAtMinimum() {
        final int value = Integer.MIN_VALUE;
        final TokenCreateTransaction subject = new TokenCreateTransaction(nameValue(), symbolValue(), tokenTypeValue(), value, initialSupplyValue(), treasuryAccountIdValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue());
        assertValue(value, subject.decimals());
    }

    @Test
    @DisplayName("decimals: accepts the maximum (2147483647)")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/decimals/accepts-at-maximum")
    void shouldAcceptDecimalsAtMaximum() {
        final int value = 2147483647;
        final TokenCreateTransaction subject = new TokenCreateTransaction(nameValue(), symbolValue(), tokenTypeValue(), value, initialSupplyValue(), treasuryAccountIdValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue());
        assertValue(value, subject.decimals());
    }

    @Test
    @DisplayName("initialSupply: accepts the minimum (-9223372036854775808)")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/initialSupply/accepts-at-minimum")
    void shouldAcceptInitialSupplyAtMinimum() {
        final long value = -9223372036854775808L;
        final TokenCreateTransaction subject = new TokenCreateTransaction(nameValue(), symbolValue(), tokenTypeValue(), decimalsValue(), value, treasuryAccountIdValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue());
        assertValue(value, subject.initialSupply());
    }

    @Test
    @DisplayName("initialSupply: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/initialSupply/accepts-at-maximum")
    void shouldAcceptInitialSupplyAtMaximum() {
        final long value = 9223372036854775807L;
        final TokenCreateTransaction subject = new TokenCreateTransaction(nameValue(), symbolValue(), tokenTypeValue(), decimalsValue(), value, treasuryAccountIdValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue());
        assertValue(value, subject.initialSupply());
    }

    @Test
    @DisplayName("rejects null for treasuryAccountId")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/treasuryAccountId/rejects-null")
    void shouldRejectNullTreasuryAccountId() {
        assertThrows(NullPointerException.class, () -> new TokenCreateTransaction(nameValue(), symbolValue(), tokenTypeValue(), decimalsValue(), initialSupplyValue(), (AccountId) null, supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue()));
    }

    @Test
    @DisplayName("rejects null for supplyType")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/supplyType/rejects-null")
    void shouldRejectNullSupplyType() {
        assertThrows(NullPointerException.class, () -> new TokenCreateTransaction(nameValue(), symbolValue(), tokenTypeValue(), decimalsValue(), initialSupplyValue(), treasuryAccountIdValue(), (TokenSupplyType) null, maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue()));
    }

    @Test
    @DisplayName("maxSupply: accepts the minimum (1)")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/maxSupply/accepts-at-minimum")
    void shouldAcceptMaxSupplyAtMinimum() {
        final @Nullable Long value = 1L;
        final TokenCreateTransaction subject = new TokenCreateTransaction(nameValue(), symbolValue(), tokenTypeValue(), decimalsValue(), initialSupplyValue(), treasuryAccountIdValue(), supplyTypeValue(), value, freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue());
        assertValue(value, subject.maxSupply());
    }

    @Test
    @DisplayName("maxSupply: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/maxSupply/accepts-at-maximum")
    void shouldAcceptMaxSupplyAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final TokenCreateTransaction subject = new TokenCreateTransaction(nameValue(), symbolValue(), tokenTypeValue(), decimalsValue(), initialSupplyValue(), treasuryAccountIdValue(), supplyTypeValue(), value, freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue());
        assertValue(value, subject.maxSupply());
    }

    @Test
    @DisplayName("maxSupply: rejects a value below the minimum (0)")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/maxSupply/rejects-below-minimum")
    void shouldRejectMaxSupplyBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new TokenCreateTransaction(nameValue(), symbolValue(), tokenTypeValue(), decimalsValue(), initialSupplyValue(), treasuryAccountIdValue(), supplyTypeValue(), 0L, freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue()));
    }

    @Test
    @DisplayName("tokenMemo: accepts more elements than the maximum")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/tokenMemo/accepts-at-maximum-bytes")
    void shouldAcceptTokenMemoAtMaximumBytes() {
        final @Nullable String value = "ääääääääääääääääääääääääääääääääääääääääääääääääää";
        final TokenCreateTransaction subject = new TokenCreateTransaction(nameValue(), symbolValue(), tokenTypeValue(), decimalsValue(), initialSupplyValue(), treasuryAccountIdValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), value, metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue());
        assertValue(value, subject.tokenMemo());
    }

    @Test
    @DisplayName("tokenMemo: rejects more elements than the maximum")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/tokenMemo/rejects-above-maximum-bytes")
    void shouldRejectTokenMemoAboveMaximumBytes() {
        assertThrows(IllegalArgumentException.class, () -> new TokenCreateTransaction(nameValue(), symbolValue(), tokenTypeValue(), decimalsValue(), initialSupplyValue(), treasuryAccountIdValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), "ääääääääääääääääääääääääääääääääääääääääääääääääääa", metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue()));
    }

    @Test
    @DisplayName("rejects null for metadata")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/metadata/rejects-null")
    void shouldRejectNullMetadata() {
        assertThrows(NullPointerException.class, () -> new TokenCreateTransaction(nameValue(), symbolValue(), tokenTypeValue(), decimalsValue(), initialSupplyValue(), treasuryAccountIdValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), (byte[]) null, expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue()));
    }

    @Test
    @DisplayName("metadata: accepts the maximum number of elements")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/metadata/accepts-at-maximum-size")
    void shouldAcceptMetadataAtMaximumSize() {
        final byte[] value = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100};
        final TokenCreateTransaction subject = new TokenCreateTransaction(nameValue(), symbolValue(), tokenTypeValue(), decimalsValue(), initialSupplyValue(), treasuryAccountIdValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), value, expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue());
        assertValue(value, subject.metadata());
    }

    @Test
    @DisplayName("metadata: rejects more elements than the maximum")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/metadata/rejects-above-maximum-size")
    void shouldRejectMetadataAboveMaximumSize() {
        assertThrows(IllegalArgumentException.class, () -> new TokenCreateTransaction(nameValue(), symbolValue(), tokenTypeValue(), decimalsValue(), initialSupplyValue(), treasuryAccountIdValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 1}, expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue()));
    }

    @Test
    @DisplayName("the constructor copies metadata; the accessor returns a copy")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/metadata/copies-in")
    void shouldCopyMetadataIn() {
        final byte[] value = metadataValue();
        final byte[] expected = value.clone();
        final TokenCreateTransaction subject = new TokenCreateTransaction(nameValue(), symbolValue(), tokenTypeValue(), decimalsValue(), initialSupplyValue(), treasuryAccountIdValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), value, expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.metadata());
        final byte[] returned = subject.metadata();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.metadata());
    }

    @Test
    @DisplayName("setMaxTransactionFee changes maxTransactionFee and returns the object")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/maxTransactionFee/set")
    void shouldSetMaxTransactionFee() {
        final TokenCreateTransaction subject = create();
        final @Nullable NativeToken<?, ?> value = maxTransactionFeeOtherValue();
        assertSame(subject, subject.setMaxTransactionFee(value));
        assertValue(value, subject.maxTransactionFee());
    }

    @Test
    @DisplayName("setMaxTransactionFee accepts null")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/maxTransactionFee/set-null")
    void shouldSetMaxTransactionFeeToNull() {
        final TokenCreateTransaction subject = create();
        subject.setMaxTransactionFee((NativeToken<?, ?>) null);
        assertNull(subject.maxTransactionFee());
    }

    @Test
    @DisplayName("setValidDuration changes validDuration and returns the object")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/validDuration/set")
    void shouldSetValidDuration() {
        final TokenCreateTransaction subject = create();
        final @Nullable Duration value = validDurationOtherValue();
        assertSame(subject, subject.setValidDuration(value));
        assertValue(value, subject.validDuration());
    }

    @Test
    @DisplayName("setValidDuration accepts null")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/validDuration/set-null")
    void shouldSetValidDurationToNull() {
        final TokenCreateTransaction subject = create();
        subject.setValidDuration((Duration) null);
        assertNull(subject.validDuration());
    }

    @Test
    @DisplayName("setMemo changes memo and returns the object")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/memo/set")
    void shouldSetMemo() {
        final TokenCreateTransaction subject = create();
        final @Nullable String value = memoOtherValue();
        assertSame(subject, subject.setMemo(value));
        assertValue(value, subject.memo());
    }

    @Test
    @DisplayName("setMemo accepts null")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/memo/set-null")
    void shouldSetMemoToNull() {
        final TokenCreateTransaction subject = create();
        subject.setMemo((String) null);
        assertNull(subject.memo());
    }

    @Test
    @DisplayName("setMemo accepts more elements than the maximum")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/memo/set-accepts-at-maximum-bytes")
    void shouldSetMemoAtMaximumBytes() {
        final TokenCreateTransaction subject = create();
        final @Nullable String value = "ääääääääääääääääääääääääääääääääääääääääääääääääää";
        subject.setMemo(value);
        assertValue(value, subject.memo());
    }

    @Test
    @DisplayName("setMemo rejects more elements than the maximum and keeps the value")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/memo/set-rejects-above-maximum-bytes")
    void shouldNotSetMemoAboveMaximumBytes() {
        final TokenCreateTransaction subject = create();
        assertThrows(IllegalArgumentException.class, () -> subject.setMemo("ääääääääääääääääääääääääääääääääääääääääääääääääääa"));
        assertValue(null, subject.memo());
    }

    @Test
    @DisplayName("pack(payer, nodes) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/pack/call")
    void shouldCallPack() {
        assertNotNull(create().pack(payerValue(), nodesValue()));
    }

    @Test
    @DisplayName("signWithOperator(client) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/signWithOperator/call")
    void shouldCallSignWithOperator() {
        assertNotNull(create().signWithOperator(clientValue()));
    }

    @Test
    @DisplayName("sign(payer, nodes) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/sign/call")
    void shouldCallSign() {
        assertNotNull(create().sign(payerValue(), nodesValue()));
    }

    @Test
    @DisplayName("sign(payerId, signer, nodes) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/sign/call-2")
    void shouldCallSign2() {
        assertNotNull(create().sign(payerIdValue(), signerValue(), nodesValue()));
    }

    @Test
    @DisplayName("signWithOperatorAndSubmit(client) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/signWithOperatorAndSubmit/call")
    void shouldCallSignWithOperatorAndSubmit() {
        assertNotNull(create().signWithOperatorAndSubmit(clientValue()));
    }

    @Test
    @DisplayName("packForBatch(payer, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/packForBatch/call")
    void shouldCallPackForBatch() {
        assertNotNull(create().packForBatch(payerValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatchWithOperator(client, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/signForBatchWithOperator/call")
    void shouldCallSignForBatchWithOperator() {
        assertNotNull(create().signForBatchWithOperator(clientValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatch(payer, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/signForBatch/call")
    void shouldCallSignForBatch() {
        assertNotNull(create().signForBatch(payerValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatch(payerId, signer, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.tokens.TokenCreateTransaction/signForBatch/call-2")
    void shouldCallSignForBatch2() {
        assertNotNull(create().signForBatch(payerIdValue(), signerValue(), batchKeyValue()));
    }

    private static String nameValue() {
        return "value";
    }

    private static String symbolValue() {
        return "value";
    }

    private static TokenType tokenTypeValue() {
        return TokenType.FUNGIBLE_COMMON;
    }

    private static int decimalsValue() {
        return 1;
    }

    private static long initialSupplyValue() {
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

    private static TokenCreateTransaction create() {
        return new TokenCreateTransaction(nameValue(), symbolValue(), tokenTypeValue(), decimalsValue(), initialSupplyValue(), treasuryAccountIdValue(), supplyTypeValue(), maxSupplyValue(), freezeDefaultValue(), tokenMemoValue(), metadataValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), adminAuthorityValue(), supplyAuthorityValue(), kycAuthorityValue(), freezeAuthorityValue(), wipeAuthorityValue(), pauseAuthorityValue(), feeScheduleAuthorityValue(), metadataAuthorityValue());
    }

    private static NativeToken<?, ?> maxTransactionFeeOtherValue() {
        return new Hbar(2L, HbarUnit.TINYBAR);
    }

    private static Duration validDurationOtherValue() {
        return Duration.ofSeconds(2L);
    }

    private static String memoOtherValue() {
        return "value1";
    }

    private static Account payerValue() {
        return new Account(new AccountId(0L, 0L, "", 1001L, null, null), KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c"));
    }

    private static List<AccountId> nodesValue() {
        return List.of(new AccountId(0L, 0L, "", 1001L, null, null));
    }

    private static HieroClient<?> clientValue() {
        return ClientFactory.createClient(new NetworkSetting(new Network<>(new byte[] {(byte) 3}, "local", HbarUnit.HBAR), Set.of(new ConsensusNode(new IpAddress(new byte[] {(byte) 127, (byte) 0, (byte) 0, (byte) 1}), 35211, new AccountId(0L, 0L, "", 3L, null, null))), Set.of(new MirrorNode("http://127.0.0.1:38081"))), new Account(new AccountId(0L, 0L, "", 1001L, null, null), KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c")));
    }

    private static AccountId payerIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static TransactionSigner signerValue() {
        return ClientFactory.createClient(new NetworkSetting(new Network<>(new byte[] {(byte) 3}, "local", HbarUnit.HBAR), Set.of(new ConsensusNode(new IpAddress(new byte[] {(byte) 127, (byte) 0, (byte) 0, (byte) 1}), 35211, new AccountId(0L, 0L, "", 3L, null, null))), Set.of(new MirrorNode("http://127.0.0.1:38081"))), new Account(new AccountId(0L, 0L, "", 1001L, null, null), KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c"))).transactionSigner();
    }

    private static Authority batchKeyValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }
}
