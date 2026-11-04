package org.hiero.evm;

import static org.hiero.sdk.testing.Testing.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `evm`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("evm")
class EvmSpecTest {

    /// EIP-55, section "Test Cases": addresses in their EIP-55 form (all caps, all lower and mixed case).
    private static List<String> EIP55_ADDRESSES() {
        return List.of("0x52908400098527886E0F7030069857D2E4169EE7", "0x8617E340B3D01FA5F11F306F4090FD50E238070D", "0xde709f2102306220921060314715629080e2fb77", "0x27b1fdb04752bbc536007a920d24acb045561c26", "0x5aAeb6053F3E94C9b9A09f33669435E7Ef1BeAed", "0xfB6916095ca1df60bB79Ce92cE3Ea74c37c5d359", "0xdbF03B407c01E7cD3CBea99509d93f8DDDC8C6FB", "0xD1220A0cf47c7B9Be7A2E6BA89F429762e7b9aDb");
    }

    /// `evm/an-evm-address-is-read-from-40-hex-digits-with-or-without-0x`
    ///
    /// Source: the Swift, Rust, Python and C++ SDKs.
    @Test
    @DisplayName("an EVM address is read from 40 hex digits with or without 0x")
    @Tag("testId:evm/an-evm-address-is-read-from-40-hex-digits-with-or-without-0x")
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

    /// `evm/an-evm-address-of-the-wrong-length-or-with-other-characters-is-rejected`
    ///
    /// Source: the Swift, Rust, Python and C++ SDKs.
    @Test
    @DisplayName("an EVM address of the wrong length or with other characters is rejected")
    @Tag("testId:evm/an-evm-address-of-the-wrong-length-or-with-other-characters-is-rejected")
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

    /// `evm/an-evm-address-is-the-same-in-upper-and-lower-case`
    ///
    /// Source: the Swift, Java, Rust and Go SDKs (hex digits in either case denote the same address).
    @Test
    @DisplayName("an EVM address is the same in upper and lower case")
    @Tag("testId:evm/an-evm-address-is-the-same-in-upper-and-lower-case")
    void anEVMAddressIsTheSameInUpperAndLowerCase() {
        // when
        final EvmAddress upper = EvmAddress.fromString("0x1234567890ABCDEF1234567890ABCDEF12345678");
        final EvmAddress lower = EvmAddress.fromString("1234567890abcdef1234567890abcdef12345678");

        // then
        assertEquals(lower, upper);
    }

    /// `evm/an-evm-address-of-39-or-42-digits-with-a-misplaced-or-doubled-0x-or-of-22-bytes-is-rejected`
    ///
    /// Source: the C++ SDK (39 digits, 0x inside the digits, 22 bytes) and the Python SDK (42 digits).
    @Test
    @DisplayName("an EVM address of 39 or 42 digits, with a misplaced or doubled 0x, or of 22 bytes is rejected")
    @Tag("testId:evm/an-evm-address-of-39-or-42-digits-with-a-misplaced-or-doubled-0x-or-of-22-bytes-is-rejected")
    void anEVMAddressOf39Or42DigitsWithAMisplacedOrDoubled0xOrOf22BytesIsRejected() {
        for (final String text : List.of("303132333435363738396162636465666768696", "1234567890abcdef1234567890abcdef1234567890", "3031323334353637383961626364650x66676869", "0x0x1234567890abcdef1234567890abcdef12345678")) {
            // when / then
            assertThrows(IllegalArgumentException.class, () -> EvmAddress.fromString(text));
        }

        // when / then
        assertThrows(IllegalArgumentException.class, () -> EvmAddress.fromBytes(repeat(hex("00"), 22L)));
    }

    /// `evm/an-evm-address-is-written-in-its-eip-55-form`
    ///
    /// Source: EIP-55, section "Test Cases", written from the bytes of the address.
    @Test
    @DisplayName("an EVM address is written in its EIP-55 form")
    @Tag("testId:evm/an-evm-address-is-written-in-its-eip-55-form")
    void anEVMAddressIsWrittenInItsEIP55Form() {
        for (final String checksummed : EIP55_ADDRESSES()) {
            // when: built from the bytes, so the case of the text plays no part
            final EvmAddress evm = EvmAddress.fromBytes(hex(substring(checksummed, 2L, 42L)));

            // then
            assertEquals(checksummed, evm.toString());
            assertEquals(evm, EvmAddress.fromString(checksummed));
        }
    }

    /// `evm/an-evm-address-in-mixed-case-that-is-not-its-eip-55-form-is-rejected`
    ///
    /// Source: EIP-55 (mixed case carries a checksum); rejected by ethers.js, web3.js and viem. The second address is the
    /// one of the Go SDK's mirror node tests, which is not in EIP-55 form.
    @Test
    @DisplayName("an EVM address in mixed case that is not its EIP-55 form is rejected")
    @Tag("testId:evm/an-evm-address-in-mixed-case-that-is-not-its-eip-55-form-is-rejected")
    void anEVMAddressInMixedCaseThatIsNotItsEIP55FormIsRejected() {
        for (final String text : List.of("0x5aaeb6053F3E94C9b9A09f33669435E7Ef1BeAed", "7F2c5e0f4a3b9E8d1C6A5b4E3f2D1c0B9a8E7F6d")) {
            // when / then
            assertThrows(IllegalArgumentException.class, () -> EvmAddress.fromString(text));
        }
    }
}
