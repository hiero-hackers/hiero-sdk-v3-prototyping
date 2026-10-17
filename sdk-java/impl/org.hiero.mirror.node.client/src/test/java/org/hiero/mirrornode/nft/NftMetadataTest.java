package org.hiero.mirrornode.nft;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `NftMetadata`, generated from the specs.
@DisplayName("NftMetadata")
class NftMetadataTest {

    @Test
    @DisplayName("creates a NftMetadata and returns the values")
    @Tag("testId:mirrornode.nft.NftMetadata/create")
    void shouldCreateAndReturnTheValues() {
        final Address tokenId = tokenIdValue();
        final String name = nameValue();
        final String symbol = symbolValue();
        final AccountId treasuryAccountId = treasuryAccountIdValue();
        final NftMetadata subject = new NftMetadata(tokenId, name, symbol, treasuryAccountId);
        assertValue(tokenId, subject.tokenId());
        assertValue(name, subject.name());
        assertValue(symbol, subject.symbol());
        assertValue(treasuryAccountId, subject.treasuryAccountId());
    }

    @Test
    @DisplayName("rejects null for tokenId")
    @Tag("testId:mirrornode.nft.NftMetadata/tokenId/rejects-null")
    void shouldRejectNullTokenId() {
        assertThrows(NullPointerException.class, () -> new NftMetadata((Address) null, nameValue(), symbolValue(), treasuryAccountIdValue()));
    }

    @Test
    @DisplayName("rejects null for name")
    @Tag("testId:mirrornode.nft.NftMetadata/name/rejects-null")
    void shouldRejectNullName() {
        assertThrows(NullPointerException.class, () -> new NftMetadata(tokenIdValue(), (String) null, symbolValue(), treasuryAccountIdValue()));
    }

    @Test
    @DisplayName("rejects null for symbol")
    @Tag("testId:mirrornode.nft.NftMetadata/symbol/rejects-null")
    void shouldRejectNullSymbol() {
        assertThrows(NullPointerException.class, () -> new NftMetadata(tokenIdValue(), nameValue(), (String) null, treasuryAccountIdValue()));
    }

    @Test
    @DisplayName("rejects null for treasuryAccountId")
    @Tag("testId:mirrornode.nft.NftMetadata/treasuryAccountId/rejects-null")
    void shouldRejectNullTreasuryAccountId() {
        assertThrows(NullPointerException.class, () -> new NftMetadata(tokenIdValue(), nameValue(), symbolValue(), (AccountId) null));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.nft.NftMetadata/value-semantics")
    void shouldCompareByValue() {
        final NftMetadata subject = create();
        final NftMetadata other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
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

    private static AccountId treasuryAccountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static NftMetadata create() {
        return new NftMetadata(tokenIdValue(), nameValue(), symbolValue(), treasuryAccountIdValue());
    }
}
