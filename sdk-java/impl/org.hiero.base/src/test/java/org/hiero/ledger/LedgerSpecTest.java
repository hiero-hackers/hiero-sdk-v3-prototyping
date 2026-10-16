package org.hiero.ledger;

import static org.hiero.sdk.testing.Testing.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.hiero.hedera.HbarUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of the spec `ledger`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("ledger")
class LedgerSpecTest {

    /// `ledger/the-hip-15-checksum-of-an-account-id-is-valid-only-on-its-ledger`
    ///
    /// Source: HIP-15; the vectors of the Java, JavaScript, Rust, Swift, Go and Python SDKs.
    @Test
    @DisplayName("the HIP-15 checksum of an account id is valid only on its ledger")
    void theHIP15ChecksumOfAnAccountIdIsValidOnlyOnItsLedger() {
        // given
        final Network<HbarUnit> mainnet = new Network<>(new byte[] {(byte) 0}, "mainnet", HbarUnit.HBAR);
        final Network<HbarUnit> testnet = new Network<>(new byte[] {(byte) 1}, "testnet", HbarUnit.HBAR);
        final Network<HbarUnit> previewnet = new Network<>(new byte[] {(byte) 2}, "previewnet", HbarUnit.HBAR);

        // when
        final AccountId onMainnet = AccountId.fromString("0.0.123-vfmkw");
        final AccountId onTestnet = AccountId.fromString("0.0.123-esxsf");
        final AccountId onPreviewnet = AccountId.fromString("0.0.123-ogizo");

        // then
        assertEquals("vfmkw", onMainnet.checksum());
        assertTrue(onMainnet.validateChecksum(mainnet));
        assertTrue(onTestnet.validateChecksum(testnet));
        assertTrue(onPreviewnet.validateChecksum(previewnet));
        assertFalse(onMainnet.validateChecksum(testnet));
        assertFalse(AccountId.fromString("0.0.123-ntjli").validateChecksum(previewnet));
    }

    /// `ledger/the-hip-15-checksum-depends-on-every-byte-of-the-ledger-id`
    ///
    /// Source: HIP-15; the vectors of the Python SDK (ledgers 0 to 3) and of the Rust and Swift SDKs (a ledger id of more
    /// than one byte).
    @Test
    @DisplayName("the HIP-15 checksum depends on every byte of the ledger id")
    void theHIP15ChecksumDependsOnEveryByteOfTheLedgerId() {
        // given
        final Network<HbarUnit> local = new Network<>(new byte[] {(byte) 3}, "local", HbarUnit.HBAR);
        final Network<HbarUnit> custom = new Network<>(new byte[] {(byte) 12, (byte) 31, (byte) 0, (byte) 2}, "custom", HbarUnit.HBAR);

        // when
        final Address one = Address.fromString("0.0.1-ftsts");
        final Address zero = Address.fromString("0.0.0-kenvf");

        // then
        assertTrue(one.validateChecksum(local));
        assertTrue(zero.validateChecksum(custom));
        assertFalse(zero.validateChecksum(local));
    }

    /// `ledger/a-malformed-checksum-is-rejected`
    ///
    /// Source: HIP-15 (five lowercase letters); rejected by the Java, Go, Rust and Swift SDKs.
    @Test
    @DisplayName("a malformed checksum is rejected")
    void aMalformedChecksumIsRejected() {
        for (final String text : List.of("0.0.123-ntjl", "0.0.123-ntjl1", "0.0.123-")) {
            // when / then
            assertThrows(IllegalArgumentException.class, () -> AccountId.fromString(text));
        }
    }

    /// `ledger/text-that-is-no-account-id-is-rejected`
    ///
    /// Source: the JavaScript, Python, C++, Java, Rust and Swift SDKs.
    @Test
    @DisplayName("text that is no account id is rejected")
    void textThatIsNoAccountIdIsRejected() {
        for (final String text : List.of("", "0.0", "0.0.", "0.0.a", "a.b.c", "0.0.-1", "-1.-1.-1", "1.2.3.4")) {
            // when / then
            assertThrows(IllegalArgumentException.class, () -> AccountId.fromString(text));
        }
    }

    /// `ledger/an-account-id-is-read-from-and-written-as-shard-realm-num`
    ///
    /// Source: all seven SDKs.
    @Test
    @DisplayName("an account id is read from and written as shard.realm.num")
    void anAccountIdIsReadFromAndWrittenAsShardRealmNum() {
        for (final String text : List.of("0.0.5005", "1.2.3", "10.50.25050")) {
            // when
            final AccountId id = AccountId.fromString(text);

            // then
            assertEquals(text, id.toString());
            assertNotNull(id.num());
            assertNull(id.evmAddress());
        }
    }

    /// `ledger/an-account-id-keeps-its-shard-realm-and-number`
    ///
    /// Source: the Rust and Swift SDKs.
    @Test
    @DisplayName("an account id keeps its shard, realm and number")
    void anAccountIdKeepsItsShardRealmAndNumber() {
        // when
        final AccountId id = AccountId.fromString("10.50.25050");

        // then
        assertEquals(10L, id.shard());
        assertEquals(50L, id.realm());
        assertEquals(25050L, id.num());
        assertEquals("", id.checksum());
    }

    /// `ledger/an-evm-address-is-read-from-40-hex-digits-with-or-without-0x`
    ///
    /// Source: the Swift, Rust, Python and C++ SDKs.
    @Test
    @DisplayName("an EVM address is read from 40 hex digits with or without 0x")
    void anEVMAddressIsReadFrom40HexDigitsWithOrWithout0x() {
        // given
        final byte[] bytes = hex("1234567890abcdef1234567890abcdef12345678");

        // when
        final EvmAddress withPrefix = EvmAddress.fromString("0x1234567890abcdef1234567890abcdef12345678");
        final EvmAddress withoutPrefix = EvmAddress.fromString("1234567890abcdef1234567890abcdef12345678");

        // then
        assertEquals(bytes, withPrefix.bytes());
        assertEquals(bytes, withoutPrefix.bytes());
        assertEquals(withPrefix, EvmAddress.fromBytes(bytes));
    }

    /// `ledger/an-evm-address-of-the-wrong-length-or-with-other-characters-is-rejected`
    ///
    /// Source: the Swift, Rust, Python and C++ SDKs.
    @Test
    @DisplayName("an EVM address of the wrong length or with other characters is rejected")
    void anEVMAddressOfTheWrongLengthOrWithOtherCharactersIsRejected() {
        for (final String text : List.of("", "0x1234", "0x0f0e0d0c0b0a09080706050403020100", "0xGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG")) {
            // when / then
            assertThrows(IllegalArgumentException.class, () -> EvmAddress.fromString(text));
        }
        for (final long size : List.of(19L, 21L)) {
            // when / then
            assertThrows(IllegalArgumentException.class, () -> EvmAddress.fromBytes(repeat(hex("00"), size)));
        }
    }

    /// `ledger/a-contract-id-of-an-evm-address-keeps-its-shard-realm-and-address`
    ///
    /// Source: the Java, Rust, Go and JavaScript SDKs. Following `@@oneOf(num, evmAddress)`, the number is absent.
    @Test
    @DisplayName("a contract id of an EVM address keeps its shard, realm and address")
    void aContractIdOfAnEVMAddressKeepsItsShardRealmAndAddress() {
        // given
        final EvmAddress address = EvmAddress.fromString("98329e006610472e6B372C080833f6D79ED833cf");

        // when
        final ContractId id = ContractId.fromEvmAddress(1L, 2L, address);

        // then
        assertEquals(1L, id.shard());
        assertEquals(2L, id.realm());
        assertEquals(address, id.evmAddress());
        assertNull(id.num());
    }

    /// `ledger/an-ipv4-address-is-written-as-dotted-quad-and-needs-four-bytes`
    ///
    /// Source: the dotted-quad notation of IPv4 (RFC 791); the C++ SDK.
    @Test
    @DisplayName("an IPv4 address is written as dotted quad and needs four bytes")
    void anIPv4AddressIsWrittenAsDottedQuadAndNeedsFourBytes() {
        // when
        final IpAddress address = IpAddress.fromBytes(new byte[] {(byte) 192, (byte) 168, (byte) 1, (byte) 1});

        // then
        assertEquals("192.168.1.1", address.toString());
        assertEquals(address, IpAddress.fromString("192.168.1.1"));
        for (final long size : List.of(3L, 5L)) {
            // when / then
            assertThrows(IllegalArgumentException.class, () -> IpAddress.fromBytes(repeat(hex("01"), size)));
        }
    }

    /// `ledger/a-transaction-id-is-read-from-and-written-as-account-seconds-nanos`
    ///
    /// Source: the Java, Rust and Swift SDKs.
    @Test
    @DisplayName("a transaction id is read from and written as account@seconds.nanos")
    void aTransactionIdIsReadFromAndWrittenAsAccountSecondsNanos() {
        // given
        final String text = "0.0.23847@1588539964.632521325";

        // when
        final TransactionId id = TransactionId.fromString(text);

        // then
        assertEquals(text, id.toString());
        assertEquals(AccountId.fromString("0.0.23847"), id.accountId());
        assertNull(id.nonce());
    }

    /// `ledger/a-transaction-id-keeps-its-nonce`
    ///
    /// Source: the Java, Rust and Swift SDKs.
    @Test
    @DisplayName("a transaction id keeps its nonce")
    void aTransactionIdKeepsItsNonce() {
        // given
        final String text = "0.0.23847@1588539964.632521325/4";

        // when
        final TransactionId id = TransactionId.fromString(text);

        // then
        assertEquals(text, id.toString());
        assertEquals(4, id.nonce());
    }

    /// `ledger/text-that-is-no-transaction-id-is-rejected`
    ///
    /// Source: the Rust, Swift, Java and Python SDKs.
    @Test
    @DisplayName("text that is no transaction id is rejected")
    void textThatIsNoTransactionIdIsRejected() {
        for (final String text : List.of("0.0.31415?1641088801.2", "0.0.31415/1641088801.2", "0.0.31415?scheduled/1412@1641088801.2", "0.0.23847.1588539964.632521325/4", "0.0.23847@1588539964/4", "0.0.123@abc.def")) {
            // when / then
            assertThrows(IllegalArgumentException.class, () -> TransactionId.fromString(text));
        }
    }
}
