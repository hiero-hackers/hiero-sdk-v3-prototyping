package org.hiero.mirrornode.token;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.Address;
import org.hiero.token.TokenType;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `Token`, generated from the specs.
@DisplayName("Token")
class TokenTest {

    @Test
    @DisplayName("creates a Token and returns the values")
    @Tag("testId:mirrornode.token.Token/create")
    void shouldCreateAndReturnTheValues() {
        final @Nullable Address tokenId = tokenIdValue();
        final String name = nameValue();
        final String symbol = symbolValue();
        final TokenType type = typeValue();
        final long decimals = decimalsValue();
        final byte @Nullable [] metadata = metadataValue();
        final @Nullable Authority adminKey = adminKeyValue();
        final Token subject = new Token(tokenId, name, symbol, type, decimals, metadata, adminKey);
        assertValue(tokenId, subject.tokenId());
        assertValue(name, subject.name());
        assertValue(symbol, subject.symbol());
        assertValue(type, subject.type());
        assertValue(decimals, subject.decimals());
        assertValue(metadata, subject.metadata());
        assertValue(adminKey, subject.adminKey());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.token.Token/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final @Nullable Address tokenId = tokenIdValue();
        final String name = nameValue();
        final String symbol = symbolValue();
        final TokenType type = typeValue();
        final long decimals = decimalsValue();
        final byte @Nullable [] metadata = metadataValue();
        final @Nullable Authority adminKey = adminKeyValue();
        final Token subject = Token.builder()
                .tokenId(tokenId)
                .name(name)
                .symbol(symbol)
                .type(type)
                .decimals(decimals)
                .metadata(metadata)
                .adminKey(adminKey)
                .build();
        assertValue(tokenId, subject.tokenId());
        assertValue(name, subject.name());
        assertValue(symbol, subject.symbol());
        assertValue(type, subject.type());
        assertValue(decimals, subject.decimals());
        assertValue(metadata, subject.metadata());
        assertValue(adminKey, subject.adminKey());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.token.Token/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final Token subject = Token.builder()
                .name(nameValue())
                .symbol(symbolValue())
                .type(typeValue())
                .decimals(decimalsValue())
                .build();
        assertNull(subject.tokenId());
        assertNull(subject.metadata());
        assertNull(subject.adminKey());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.token.Token/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> Token.builder().build());
    }

    @Test
    @DisplayName("rejects null for name")
    @Tag("testId:mirrornode.token.Token/name/rejects-null")
    void shouldRejectNullName() {
        assertThrows(NullPointerException.class, () -> new Token(tokenIdValue(), (String) null, symbolValue(), typeValue(), decimalsValue(), metadataValue(), adminKeyValue()));
    }

    @Test
    @DisplayName("rejects null for symbol")
    @Tag("testId:mirrornode.token.Token/symbol/rejects-null")
    void shouldRejectNullSymbol() {
        assertThrows(NullPointerException.class, () -> new Token(tokenIdValue(), nameValue(), (String) null, typeValue(), decimalsValue(), metadataValue(), adminKeyValue()));
    }

    @Test
    @DisplayName("rejects null for type")
    @Tag("testId:mirrornode.token.Token/type/rejects-null")
    void shouldRejectNullType() {
        assertThrows(NullPointerException.class, () -> new Token(tokenIdValue(), nameValue(), symbolValue(), (TokenType) null, decimalsValue(), metadataValue(), adminKeyValue()));
    }

    @Test
    @DisplayName("decimals: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.token.Token/decimals/accepts-at-minimum")
    void shouldAcceptDecimalsAtMinimum() {
        final long value = -9223372036854775808L;
        final Token subject = new Token(tokenIdValue(), nameValue(), symbolValue(), typeValue(), value, metadataValue(), adminKeyValue());
        assertValue(value, subject.decimals());
    }

    @Test
    @DisplayName("decimals: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.token.Token/decimals/accepts-at-maximum")
    void shouldAcceptDecimalsAtMaximum() {
        final long value = 9223372036854775807L;
        final Token subject = new Token(tokenIdValue(), nameValue(), symbolValue(), typeValue(), value, metadataValue(), adminKeyValue());
        assertValue(value, subject.decimals());
    }

    @Test
    @DisplayName("the constructor copies metadata; the accessor returns a copy")
    @Tag("testId:mirrornode.token.Token/metadata/copies-in")
    void shouldCopyMetadataIn() {
        final byte[] value = metadataValue();
        final byte[] expected = value.clone();
        final Token subject = new Token(tokenIdValue(), nameValue(), symbolValue(), typeValue(), decimalsValue(), value, adminKeyValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.metadata());
        final byte[] returned = subject.metadata();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.metadata());
    }

    private static Address tokenIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static String nameValue() {
        return "value";
    }

    private static String symbolValue() {
        return "value";
    }

    private static TokenType typeValue() {
        return TokenType.FUNGIBLE_COMMON;
    }

    private static long decimalsValue() {
        return 1L;
    }

    private static byte[] metadataValue() {
        return new byte[] {1, 2, 3};
    }

    private static Authority adminKeyValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static Token create() {
        return new Token(tokenIdValue(), nameValue(), symbolValue(), typeValue(), decimalsValue(), metadataValue(), adminKeyValue());
    }
}
