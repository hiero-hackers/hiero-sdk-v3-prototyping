package org.hiero.mirrornode.common;

import static org.hiero.sdk.testing.Testing.*;

import java.util.List;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `mirror-node-common`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("mirror-node-common")
class MirrorNodeCommonSpecTest {

    /// `mirrornode.common/an-hbar-transfer-keeps-its-account-its-signed-amount-and-its-approval-flag`
    ///
    /// Source: the Python SDK (hbar_transfer_test: test_hbar_transfer_constructor); this spec (a negative amount is debited,
    /// a positive one credited).
    @Test
    @DisplayName("an HBAR transfer keeps its account, its signed amount and its approval flag")
    @Tag("testId:mirrornode.common/an-hbar-transfer-keeps-its-account-its-signed-amount-and-its-approval-flag")
    void anHBARTransferKeepsItsAccountItsSignedAmountAndItsApprovalFlag() {
        // given
        final AccountId account = defaultAccountId();

        // when
        final Transfer debit = new Transfer(account, -1000L, false);
        final Transfer credit = new Transfer(account, 1000L, true);

        // then
        assertEquals(account, debit.account());
        assertEquals(-1000L, debit.amount());
        assertFalse(debit.isApproval());
        assertEquals(1000L, credit.amount());
        assertTrue(credit.isApproval());
        assertNotEquals(debit, credit);
    }

    /// `mirrornode.common/a-token-transfer-keeps-its-token-account-amount-and-approval-flag`
    ///
    /// Source: the Python SDK (token_transfer_test: test_token_transfer_constructor).
    @Test
    @DisplayName("a token transfer keeps its token, account, amount and approval flag")
    @Tag("testId:mirrornode.common/a-token-transfer-keeps-its-token-account-amount-and-approval-flag")
    void aTokenTransferKeepsItsTokenAccountAmountAndApprovalFlag() {
        // given
        final Address token = Address.fromString("0.0.5005");
        final AccountId account = defaultAccountId();

        // when
        final TokenTransfer transfer = new TokenTransfer(token, account, -10L, false);

        // then
        assertEquals(token, transfer.tokenId());
        assertEquals(account, transfer.account());
        assertEquals(-10L, transfer.amount());
        assertFalse(transfer.isApproval());
        assertEquals(new TokenTransfer(token, account, -10L, false), transfer);
    }

    /// `mirrornode.common/an-nft-transfer-keeps-its-sender-receiver-token-serial-number-and-approval-flag`
    ///
    /// Source: the Python SDK (token_nft_transfer_test: test_token_nft_transfer_constructor, approved and not approved).
    @Test
    @DisplayName("an NFT transfer keeps its sender, receiver, token, serial number and approval flag")
    @Tag("testId:mirrornode.common/an-nft-transfer-keeps-its-sender-receiver-token-serial-number-and-approval-flag")
    void anNFTTransferKeepsItsSenderReceiverTokenSerialNumberAndApprovalFlag() {
        // given
        final NftTransferParties parties = new NftTransferParties(AccountId.fromString("0.0.1001"), AccountId.fromString("0.0.1002"), Address.fromString("0.0.5005"));
        for (final boolean approved : List.of(false, true)) {
            // when
            final NftTransfer transfer = new NftTransfer(approved, parties, 1L);

            // then
            assertEquals(approved, transfer.isApproval());
            assertEquals(parties, transfer.parties());
            assertEquals(1L, transfer.serialNumber());
        }
    }

    /// `mirrornode.common/an-nft-transfer-can-be-created-without-its-parties`
    ///
    /// Source: this spec (`parties` is @@nullable).
    @Test
    @DisplayName("an NFT transfer can be created without its parties")
    @Tag("testId:mirrornode.common/an-nft-transfer-can-be-created-without-its-parties")
    void anNFTTransferCanBeCreatedWithoutItsParties() {
        // when
        final NftTransfer transfer = new NftTransfer(false, null, 1L);

        // then
        assertNull(transfer.parties());
    }

    /// `mirrornode.common/a-fixed-fee-in-hbar-has-no-denominating-token-and-a-fixed-fee-in-a-token-has-one`
    ///
    /// Source: the Java SDK (CustomFixedFeeTest: amount 4, collector 1.2.3, denominating token 5.6.7); this spec (the
    /// denominating token is absent if the fee is charged in HBAR).
    @Test
    @DisplayName("a fixed fee in HBAR has no denominating token and a fixed fee in a token has one")
    @Tag("testId:mirrornode.common/a-fixed-fee-in-hbar-has-no-denominating-token-and-a-fixed-fee-in-a-token-has-one")
    void aFixedFeeInHBARHasNoDenominatingTokenAndAFixedFeeInATokenHasOne() {
        // given
        final AccountId collector = AccountId.fromString("1.2.3");
        final Address token = Address.fromString("5.6.7");

        // when
        final FixedFee inHbar = new FixedFee(4L, collector, null);
        final FixedFee inToken = new FixedFee(4L, collector, token);

        // then
        assertEquals(4L, inHbar.amount());
        assertEquals(collector, inHbar.collectorAccountId());
        assertNull(inHbar.denominatingTokenId());
        assertEquals(token, inToken.denominatingTokenId());
    }

    private static AccountId defaultAccountId() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }
}
