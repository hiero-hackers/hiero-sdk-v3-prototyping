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

    /// HIP-15, section "Specification": the inputs that must be rejected (all but "0.0.123-abcde", which is well formed and
    /// fails only the checksum validation), checked against its regex.
    private static final List<String> HIP15_REJECTED_IDS = List.of("0.00.123", "0.0.0123-vfmkw", "0.0.123-VFMKW", "0.0.123-vFmKw", "0.0.123#vfmkw", "0.0.123vfmkw", "0.0.123 - vfmkw", "0.123", "0.0.123.", "0.0.123-vf", "0.0.123-vfm-kw", "0.0.123-vfmkwxxxx");

    /// Checksums that are not five lowercase letters (HIP-15); rejected by the Java, Go, Rust and Swift SDKs.
    private static final List<String> MALFORMED_CHECKSUMS = List.of("0.0.123-ntjl", "0.0.123-ntjl1", "0.0.123-");

    /// Text that is no entity id: from the JavaScript, Python, C++, Java, Rust and Swift SDKs, the C++ SDK (misplaced dots
    /// and characters) and the Python SDK (malformed checksum separators, whitespace).
    private static final List<String> MALFORMED_ENTITY_IDS = List.of("", "0.0", "0.0.", "0.0.a", "a.b.c", "0.0.-1", "-1.-1.-1", "1.2.3.4", "8901000", "8.901000", ".8.90.1000.", "8..901000", "890.1000.", "0.0.1!", "o.o.e", "abc", "0.0.123#vfmkw", "0.0.123 - vfmkw", "0.0.123vfmkw", "0.0.123-vfm-kw", " 0.0.100 ");

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
        for (final String text : MALFORMED_CHECKSUMS) {
            // when / then
            assertThrows(IllegalArgumentException.class, () -> Address.fromString(text));
            assertThrows(IllegalArgumentException.class, () -> AccountId.fromString(text));
            assertThrows(IllegalArgumentException.class, () -> ContractId.fromString(text));
        }
    }

    /// `ledger/text-that-is-no-entity-id-is-rejected`
    ///
    /// Source: the JavaScript, Python, C++, Java, Rust and Swift SDKs; the C++ and Python SDKs.
    @Test
    @DisplayName("text that is no entity id is rejected")
    void textThatIsNoEntityIdIsRejected() {
        for (final String text : MALFORMED_ENTITY_IDS) {
            // when / then
            assertThrows(IllegalArgumentException.class, () -> Address.fromString(text));
            assertThrows(IllegalArgumentException.class, () -> AccountId.fromString(text));
            assertThrows(IllegalArgumentException.class, () -> ContractId.fromString(text));
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

    /// `ledger/the-hip-15-checksums-of-the-sdk-tables-are-valid-on-their-ledger`
    ///
    /// Source: HIP-15; the checksum tables of the Rust and Swift SDKs (0.0.0 to 0.0.255 per ledger, here the first, second
    /// and last entry and the entries for a ledger id of four bytes), and the vectors of the Swift (0.0.1126123), Python
    /// (0.0.100), JavaScript (0.0.321) and Go (0.0.3) SDKs.
    @Test
    @DisplayName("the HIP-15 checksums of the SDK tables are valid on their ledger")
    void theHIP15ChecksumsOfTheSDKTablesAreValidOnTheirLedger() {
        // given
        final Network<HbarUnit> mainnet = new Network<>(new byte[] {(byte) 0}, "mainnet", HbarUnit.HBAR);
        final Network<HbarUnit> testnet = new Network<>(new byte[] {(byte) 1}, "testnet", HbarUnit.HBAR);
        final Network<HbarUnit> previewnet = new Network<>(new byte[] {(byte) 2}, "previewnet", HbarUnit.HBAR);
        final Network<HbarUnit> custom = new Network<>(new byte[] {(byte) 12, (byte) 31, (byte) 0, (byte) 2}, "custom", HbarUnit.HBAR);

        // when / then
        for (final String onMainnet : List.of("0.0.0-uvnqa", "0.0.1-dfkxr", "0.0.255-hpphi", "0.0.100-hhghj", "0.0.1126123-ycfte")) {
            assertTrue(AccountId.fromString(onMainnet).validateChecksum(mainnet), onMainnet);
        }
        for (final String onTestnet : List.of("0.0.0-eiyxj", "0.0.1-mswfa", "0.0.3-dmqui", "0.0.255-rdaor", "0.0.321-uhcxp")) {
            assertTrue(AccountId.fromString(onTestnet).validateChecksum(testnet), onTestnet);
        }
        for (final String onPreviewnet : List.of("0.0.0-nwkes", "0.0.1-wghmj", "0.0.255-aqlwa")) {
            assertTrue(AccountId.fromString(onPreviewnet).validateChecksum(previewnet), onPreviewnet);
        }
        for (final String onCustom : List.of("0.0.1-solcw", "0.0.255-wypmn")) {
            assertTrue(AccountId.fromString(onCustom).validateChecksum(custom), onCustom);
        }
    }

    /// `ledger/the-hip-15-checksum-of-an-address-and-a-contract-id-is-valid-only-on-its-ledger`
    ///
    /// Source: HIP-15 (the checksum belongs to every entity id); the token, topic, schedule and contract id tests of all SDKs.
    @Test
    @DisplayName("the HIP-15 checksum of an address and a contract id is valid only on its ledger")
    void theHIP15ChecksumOfAnAddressAndAContractIdIsValidOnlyOnItsLedger() {
        // given
        final Network<HbarUnit> mainnet = new Network<>(new byte[] {(byte) 0}, "mainnet", HbarUnit.HBAR);
        final Network<HbarUnit> previewnet = new Network<>(new byte[] {(byte) 2}, "previewnet", HbarUnit.HBAR);

        // when
        final Address address = Address.fromString("0.0.123-vfmkw");
        final ContractId contract = ContractId.fromString("0.0.123-ogizo");

        // then
        assertTrue(address.validateChecksum(mainnet));
        assertFalse(address.validateChecksum(previewnet));
        assertTrue(contract.validateChecksum(previewnet));
        assertFalse(contract.validateChecksum(mainnet));
    }

    /// `ledger/tostring-omits-the-checksum-and-tostringwithchecksum-keeps-it`
    ///
    /// Source: the Java, Rust, Swift, JavaScript and Python SDKs (the canonical string form carries no checksum).
    @Test
    @DisplayName("toString omits the checksum and toStringWithChecksum keeps it")
    void toStringOmitsTheChecksumAndToStringWithChecksumKeepsIt() {
        // given
        final AccountId id = AccountId.fromString("0.0.123-esxsf");

        // when / then
        assertEquals("0.0.123", id.toString());
        assertEquals("0.0.123-esxsf", id.toStringWithChecksum());
    }

    /// `ledger/an-evm-address-is-the-same-in-upper-and-lower-case`
    ///
    /// Source: the Swift, Java, Rust and Go SDKs (hex digits in either case denote the same address).
    @Test
    @DisplayName("an EVM address is the same in upper and lower case")
    void anEVMAddressIsTheSameInUpperAndLowerCase() {
        // when
        final EvmAddress upper = EvmAddress.fromString("0x1234567890ABCDEF1234567890ABCDEF12345678");
        final EvmAddress lower = EvmAddress.fromString("1234567890abcdef1234567890abcdef12345678");

        // then
        assertEquals(lower, upper);
    }

    /// `ledger/an-evm-address-of-39-or-42-digits-with-a-misplaced-or-doubled-0x-or-of-22-bytes-is-rejected`
    ///
    /// Source: the C++ SDK (39 digits, 0x inside the digits, 22 bytes) and the Python SDK (42 digits).
    @Test
    @DisplayName("an EVM address of 39 or 42 digits, with a misplaced or doubled 0x, or of 22 bytes is rejected")
    void anEVMAddressOf39Or42DigitsWithAMisplacedOrDoubled0xOrOf22BytesIsRejected() {
        for (final String text : List.of("303132333435363738396162636465666768696", "1234567890abcdef1234567890abcdef1234567890", "3031323334353637383961626364650x66676869", "0x0x1234567890abcdef1234567890abcdef12345678")) {
            // when / then
            assertThrows(IllegalArgumentException.class, () -> EvmAddress.fromString(text));
        }

        // when / then
        assertThrows(IllegalArgumentException.class, () -> EvmAddress.fromBytes(repeat(hex("00"), 22L)));
    }

    /// `ledger/a-contract-id-and-an-account-id-of-an-evm-address-are-read-from-and-written-as-shard-realm-address`
    ///
    /// Source: HIP-583 (the EVM-address form of an id is shard.realm and the address without "0x", in lower case:
    /// 0.0.b794f5ea0ba39494ce839613fffba74279579268); the Java, Rust, JavaScript, Go and Python SDKs write it so.
    @Test
    @DisplayName("a contract id and an account id of an EVM address are read from and written as shard.realm.<address>")
    void aContractIdAndAnAccountIdOfAnEVMAddressAreReadFromAndWrittenAsShardRealmAddress() {
        // given
        final EvmAddress address = EvmAddress.fromString("b794f5ea0ba39494ce839613fffba74279579268");

        // when
        final ContractId contract = ContractId.fromString("1.2.b794f5ea0ba39494ce839613fffba74279579268");
        final AccountId account = AccountId.fromString("0.0.b794f5ea0ba39494ce839613fffba74279579268");

        // then
        assertEquals(ContractId.fromEvmAddress(1L, 2L, address), contract);
        assertEquals("1.2.b794f5ea0ba39494ce839613fffba74279579268", contract.toString());
        assertEquals(address, account.evmAddress());
        assertNull(account.num());
        assertEquals("0.0.b794f5ea0ba39494ce839613fffba74279579268", account.toString());
    }

    /// `ledger/an-id-with-0x-or-upper-case-hex-digits-in-its-evm-address-form-is-rejected`
    ///
    /// Source: HIP-583 and HIP-15 (no other formats are accepted): the "0x" belongs to the stand-alone Ethereum address,
    /// not to an id, and an id is written in lower case.
    @Test
    @DisplayName("an id with 0x or upper case hex digits in its EVM-address form is rejected")
    void anIdWith0xOrUpperCaseHexDigitsInItsEVMAddressFormIsRejected() {
        for (final String text : List.of("1.2.0xb794f5ea0ba39494ce839613fffba74279579268", "1.2.B794F5EA0BA39494CE839613FFFBA74279579268")) {
            // when / then
            assertThrows(IllegalArgumentException.class, () -> ContractId.fromString(text));
        }
    }

    /// `ledger/an-account-id-of-a-long-zero-evm-address-keeps-the-address-and-has-no-number`
    ///
    /// Source: the Java, Go and Swift SDKs (an account id built from an EVM address keeps that address, even a long-zero
    /// one, and has no number; the JavaScript SDK decodes a long-zero address to its number instead). Exactly one of num,
    /// evmAddress and alias is set.
    @Test
    @DisplayName("an account id of a long-zero EVM address keeps the address and has no number")
    void anAccountIdOfALongZeroEVMAddressKeepsTheAddressAndHasNoNumber() {
        // given
        final EvmAddress address = EvmAddress.fromString("0x00000000000000000000000000000000000004d2");

        // when
        final AccountId id = AccountId.fromEvmAddress(1L, 2L, address);

        // then
        assertNull(id.num());
        assertEquals(address, id.evmAddress());
        assertEquals(1L, id.shard());
        assertEquals(2L, id.realm());
    }

    /// `ledger/the-nanoseconds-of-a-transaction-id-are-a-number-of-nanoseconds`
    ///
    /// Source: the Rust and Swift SDKs (the digits after the dot are the nanoseconds as a number) and the Java and Go SDKs
    /// (nanoseconds with a leading zero).
    @Test
    @DisplayName("the nanoseconds of a transaction id are a number of nanoseconds")
    void theNanosecondsOfATransactionIdAreANumberOfNanoseconds() {
        // when
        final TransactionId short_ = TransactionId.fromString("0.0.31415@1641088801.2");
        final TransactionId padded = TransactionId.fromString("0.0.31415@1641088801.000000002");
        final TransactionId leadingZero = TransactionId.fromString("0.0.4163533@1681876267.054802581");

        // then
        assertEquals(padded, short_);
        assertEquals("0.0.4163533@1681876267.054802581", leadingZero.toString());
    }

    /// `ledger/a-transaction-id-without-a-valid-start-of-seconds-and-nanoseconds-is-rejected`
    ///
    /// Source: the Python and Java SDKs.
    @Test
    @DisplayName("a transaction id without a valid start of seconds and nanoseconds is rejected")
    void aTransactionIdWithoutAValidStartOfSecondsAndNanosecondsIsRejected() {
        for (final String text : List.of("invalid_string", "0.0.123@12345", "0.0.123@1234567890.1234567890")) {
            // when / then
            assertThrows(IllegalArgumentException.class, () -> TransactionId.fromString(text));
        }
    }

    /// `ledger/generated-transaction-ids-keep-their-payer-and-differ`
    ///
    /// Source: the C++ SDK (100,000 ids), the Go SDK (concurrently) and the Python SDK (the payer is kept).
    @Test
    @DisplayName("generated transaction ids keep their payer and differ")
    void generatedTransactionIdsKeepTheirPayerAndDiffer() {
        // given
        final Address payer = defaultAddress();

        // when
        final TransactionId first = TransactionId.generateTransactionId(payer);
        final TransactionId second = TransactionId.generateTransactionId(payer);

        // then
        assertEquals(AccountId.fromString("0.0.1001"), first.accountId());
        assertEquals(AccountId.fromString("0.0.1001"), second.accountId());
        assertNotEquals(first, second);
    }

    /// `ledger/the-inputs-that-hip-15-rejects-are-rejected`
    ///
    /// Source: HIP-15, section "Specification": the inputs that must be rejected (all but "0.0.123-abcde", which is well
    /// formed and fails only the checksum validation), checked against its regex.
    @Test
    @DisplayName("the inputs that HIP-15 rejects are rejected")
    void theInputsThatHIP15RejectsAreRejected() {
        for (final String text : HIP15_REJECTED_IDS) {
            // when / then
            assertThrows(IllegalArgumentException.class, () -> Address.fromString(text));
            assertThrows(IllegalArgumentException.class, () -> AccountId.fromString(text));
            assertThrows(IllegalArgumentException.class, () -> ContractId.fromString(text));
        }

        // when / then: the checksum of another address does not validate
        assertFalse(Address.fromString("0.0.123-abcde").validateChecksum(new Network<>(new byte[] {(byte) 0}, "mainnet", HbarUnit.HBAR)));
    }

    /// `ledger/an-evm-address-is-written-in-its-eip-55-form`
    ///
    /// Source: EIP-55, section "Test Cases" (all caps, all lower and mixed case), written from the bytes of the address.
    @Test
    @DisplayName("an EVM address is written in its EIP-55 form")
    void anEVMAddressIsWrittenInItsEIP55Form() {
        for (final String checksummed : List.of("0x52908400098527886E0F7030069857D2E4169EE7", "0x8617E340B3D01FA5F11F306F4090FD50E238070D", "0xde709f2102306220921060314715629080e2fb77", "0x27b1fdb04752bbc536007a920d24acb045561c26", "0x5aAeb6053F3E94C9b9A09f33669435E7Ef1BeAed", "0xfB6916095ca1df60bB79Ce92cE3Ea74c37c5d359", "0xdbF03B407c01E7cD3CBea99509d93f8DDDC8C6FB", "0xD1220A0cf47c7B9Be7A2E6BA89F429762e7b9aDb")) {
            // when: built from the bytes, so the case of the text plays no part
            final EvmAddress evm = EvmAddress.fromBytes(hex(substring(checksummed, 2L, 42L)));

            // then
            assertEquals(checksummed, evm.toString());
            assertEquals(evm, EvmAddress.fromString(checksummed));
        }
    }

    /// `ledger/an-evm-address-in-mixed-case-that-is-not-its-eip-55-form-is-rejected`
    ///
    /// Source: EIP-55 (mixed case carries a checksum); rejected by ethers.js, web3.js and viem. The second address is the
    /// one of the Go SDK's mirror node tests, which is not in EIP-55 form.
    @Test
    @DisplayName("an EVM address in mixed case that is not its EIP-55 form is rejected")
    void anEVMAddressInMixedCaseThatIsNotItsEIP55FormIsRejected() {
        for (final String text : List.of("0x5aaeb6053F3E94C9b9A09f33669435E7Ef1BeAed", "7F2c5e0f4a3b9E8d1C6A5b4E3f2D1c0B9a8E7F6d")) {
            // when / then
            assertThrows(IllegalArgumentException.class, () -> EvmAddress.fromString(text));
        }
    }

    /// `ledger/a-transaction-id-is-written-with-9-digits-of-nanoseconds`
    ///
    /// Source: the Java, JavaScript and Go SDKs (the nanoseconds are written with 9 digits, the nonce follows after "/").
    @Test
    @DisplayName("a transaction id is written with 9 digits of nanoseconds")
    void aTransactionIdIsWrittenWith9DigitsOfNanoseconds() {
        // when
        final TransactionId id = TransactionId.fromString("0.0.31415@1641088801.2/4");

        // then
        assertEquals("0.0.31415@1641088801.000000002/4", id.toString());
    }

    /// `ledger/an-ipv4-address-is-read-only-from-a-dotted-quad-of-numbers-up-to-255`
    ///
    /// Source: the C++ SDK (127.0.0.1); the rejections follow from "only dotted-quad IPv4" of this spec (a port or a leading
    /// zero is no dotted quad; the Swift SDK cannot read "127.0.0.1:50211" either).
    @Test
    @DisplayName("an IPv4 address is read only from a dotted quad of numbers up to 255")
    void anIPv4AddressIsReadOnlyFromADottedQuadOfNumbersUpTo255() {
        // when
        final IpAddress localhost = IpAddress.fromString("127.0.0.1");

        // then
        assertEquals(IpAddress.fromBytes(hex("7f000001")), localhost);
        assertEquals("127.0.0.1", localhost.toString());
        for (final String text : List.of("256.0.0.1", "1.2.3", "127.0.0.1:50211", "01.2.3.4")) {
            // when / then
            assertThrows(IllegalArgumentException.class, () -> IpAddress.fromString(text));
        }
    }

    private static Address defaultAddress() {
        return new Address(0L, 0L, "", 1001L);
    }
}
