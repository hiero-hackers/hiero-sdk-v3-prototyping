package org.hiero.mirrornode.nft;

import static org.hiero.sdk.testing.Testing.*;

import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `mirror-node-nft`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("mirror-node-nft")
class MirrorNodeNftSpecTest {

    /// `mirrornode.nft/nfts-of-the-same-type-with-different-serial-numbers-are-different`
    ///
    /// Source: this spec (an NFT is identified by the token id of its type plus its serial number).
    @Test
    @DisplayName("NFTs of the same type with different serial numbers are different")
    @Tag("testId:mirrornode.nft/nfts-of-the-same-type-with-different-serial-numbers-are-different")
    void nFTsOfTheSameTypeWithDifferentSerialNumbersAreDifferent() {
        // given
        final Address token = Address.fromString("0.0.5005");
        final AccountId owner = defaultAccountId();
        final byte[] metadata = utf8("metadata");

        // when
        final Nft first = new Nft(token, 1L, owner, metadata, null, null, null, null, false);
        final Nft second = new Nft(token, 2L, owner, metadata, null, null, null, null, false);

        // then
        assertEquals(token, first.tokenId());
        assertEquals(1L, first.serialNumber());
        assertEquals(metadata, first.metadata());
        assertEquals(new Nft(token, 1L, owner, metadata, null, null, null, null, false), first);
        assertNotEquals(first, second);
    }

    /// `mirrornode.nft/a-burned-nft-has-no-owner`
    ///
    /// Source: the mirror node REST API (`Nft.account_id` is a nullable `EntityId`: a burned NFT has no owner).
    @Test
    @DisplayName("a burned NFT has no owner")
    @Tag("testId:mirrornode.nft/a-burned-nft-has-no-owner")
    void aBurnedNFTHasNoOwner() {
        // when
        final Nft burned = new Nft(Address.fromString("0.0.5005"), 1L, null, null, null, null, null, null, true);

        // then
        assertNull(burned.accountId());
        assertNull(burned.spender());
        assertTrue(burned.deleted());
    }

    private static AccountId defaultAccountId() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }
}
