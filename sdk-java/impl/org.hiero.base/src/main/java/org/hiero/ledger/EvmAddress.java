package org.hiero.ledger;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Objects;
import org.hiero.keys.internal.Secp256k1;
import org.jspecify.annotations.Nullable;

/// 20-byte EVM address — a flat, single-value identifier for EVM-side entities. NOT a
/// (shard, realm, num) tuple; therefore cannot extend BaseAddress. Used as the EVM-form
/// selector inside ContractId and AccountId, and as a stand-alone value type wherever an EVM
/// address appears on its own.
///
/// @param bytes 20 raw bytes (network byte order)
public record EvmAddress(byte[] bytes) {

    /// Creates a new `EvmAddress`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public EvmAddress {
        Objects.requireNonNull(bytes, "bytes must not be null");
        bytes = bytes.clone();
        if (bytes.length < 20) {
            throw new IllegalArgumentException("bytes must contain at least 20 element(s)");
        }
        if (bytes.length > 20) {
            throw new IllegalArgumentException("bytes must contain at most 20 element(s)");
        }
    }

    /// 20 raw bytes (network byte order)
    ///
    /// Returns a copy of the `bytes` array.
    @Override
    public byte[] bytes() {
        return bytes.clone();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof EvmAddress other
            && Arrays.equals(bytes, other.bytes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(bytes));
    }

    /// The EIP-55 form: "0x" and the 40 hex digits, a letter in upper case where EIP-55's
    /// checksum (the Keccak-256 hash of the lowercase digits) asks for it.
    @Override
    public String toString() {
        // EIP-55: a letter is upper case if the matching nibble of the Keccak-256 hash of the lowercase digits is 8 or more
        final String hex = HexFormat.of().formatHex(bytes);
        final String hash = HexFormat.of().formatHex(Secp256k1.keccak256(hex.getBytes(StandardCharsets.US_ASCII)));
        final StringBuilder text = new StringBuilder("0x");
        for (int i = 0; i < hex.length(); i++) {
            final char c = hex.charAt(i);
            text.append(Character.digit(hash.charAt(i), 16) >= 8 ? Character.toUpperCase(c) : c);
        }
        return text.toString();
    }

    /// Parses an EvmAddress from its hex-string form. Accepts both "0xabc…" and bare "abc…"
    /// (40 hex chars). Digits all in lower or all in upper case are accepted as they are; digits
    /// in mixed case must be the EIP-55 form, otherwise the text is rejected as mistyped. Throws
    /// on anything else (wrong length, non-hex characters).
    ///
    /// @throws IllegalArgumentException if an illegal format error occurs
    public static EvmAddress fromString(final String value) {
        final String hex = value.startsWith("0x") ? value.substring(2) : value;
        if (!hex.matches("[0-9a-fA-F]{40}")) {
            throw new IllegalArgumentException("'" + value + "' is no EVM address of 40 hex digits");
        }
        final EvmAddress address = new EvmAddress(HexFormat.of().parseHex(hex));
        // EIP-55: digits in mixed case carry a checksum and must be the EIP-55 form; all lower or all upper carry none
        final boolean mixedCase = !hex.equals(hex.toLowerCase(java.util.Locale.ROOT))
                && !hex.equals(hex.toUpperCase(java.util.Locale.ROOT));
        if (mixedCase && !address.toString().equals("0x" + hex)) {
            throw new IllegalArgumentException("'" + value + "' is not in its EIP-55 form, the address is mistyped");
        }
        return address;
    }

    /// Wraps raw bytes. `value.length` must equal 20; otherwise throws.
    ///
    /// @throws IllegalArgumentException if an illegal format error occurs
    public static EvmAddress fromBytes(final byte[] value) {
        if (value.length != 20) {
            throw new IllegalArgumentException("An EVM address has 20 bytes, not " + value.length);
        }
        return new EvmAddress(value);
    }
}
