package org.hiero.mirrornode.nft;

import static org.hiero.sdk.testing.Testing.*;

import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of the spec `mirror-node-nft`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("mirror-node-nft")
class MirrorNodeNftSpecTest {

    /// `mirrornode.nft/nfts-of-the-same-type-with-different-serial-numbers-are-different`
    ///
    /// Source: this spec (an NFT is identified by the token id of its type plus its serial number).
    @Test
    @DisplayName("NFTs of the same type with different serial numbers are different")
    void nFTsOfTheSameTypeWithDifferentSerialNumbersAreDifferent() {
        // given
        final Address token = Address.fromString("0.0.5005");
        final AccountId owner = defaultAccountId();
        final byte[] metadata = utf8("metadata");

        // when
        final Nft first = new Nft(token, 1L, owner, metadata);
        final Nft second = new Nft(token, 2L, owner, metadata);

        // then
        assertEquals(token, first.tokenId());
        assertEquals(1L, first.serial());
        assertEquals(metadata, first.metadata());
        assertEquals(new Nft(token, 1L, owner, metadata), first);
        assertNotEquals(first, second);
    }

    private static AccountId defaultAccountId() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }
}
