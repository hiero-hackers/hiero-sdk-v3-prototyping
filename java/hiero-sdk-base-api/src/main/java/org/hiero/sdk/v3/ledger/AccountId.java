// Source: spec/base/ledger.md (ledger#AccountId).

package org.hiero.sdk.v3.ledger;

import java.util.Arrays;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/** Immutable account identifier with exactly one numeric, EVM, or key-alias selector. */
public final class AccountId extends EvmCapableAddress {
    private final byte @Nullable [] alias;

    /** Creates an account identifier and defensively owns its optional alias. */
    public AccountId(
            long shard,
            long realm,
            @Nullable Long num,
            String checksum,
            @Nullable EvmAddress evmAddress,
            byte @Nullable [] alias) {
        super(shard, realm, checksum, num, evmAddress);
        int selectors = (num == null ? 0 : 1) + (evmAddress == null ? 0 : 1) + (alias == null ? 0 : 1);
        if (selectors != 1) {
            throw new IllegalArgumentException("exactly one of num, evmAddress, and alias is required");
        }
        this.alias = alias == null ? null : alias.clone();
    }

    /** Builds an EVM-form identifier without parsing or provider selection. */
    public static AccountId fromEvmAddress(long shard, long realm, EvmAddress address) {
        return new AccountId(shard, realm, null, "", Objects.requireNonNull(address, "address"), null);
    }

    /** Returns a defensive copy of the optional serialized public-key alias. */
    public byte @Nullable [] alias() { return alias == null ? null : alias.clone(); }

    /** Returns the canonical numeric, EVM-field, or base32-hex-alias representation. */
    @Override
    public String toString() {
        String selector;
        if (num() != null) selector = num().toString();
        else if (evmAddress() != null) selector = evmAddress().toString();
        else selector = base32Hex(Objects.requireNonNull(alias));
        return shard() + "." + realm() + "." + selector;
    }

    private static String base32Hex(byte[] value) {
        final String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUV";
        StringBuilder result = new StringBuilder((value.length * 8 + 4) / 5);
        int buffer = 0;
        int bits = 0;
        for (byte element : value) {
            buffer = (buffer << 8) | Byte.toUnsignedInt(element);
            bits += 8;
            while (bits >= 5) {
                bits -= 5;
                result.append(alphabet.charAt((buffer >>> bits) & 31));
            }
        }
        if (bits > 0) result.append(alphabet.charAt((buffer << (5 - bits)) & 31));
        return result.toString();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof AccountId value
                && shard() == value.shard() && realm() == value.realm()
                && Objects.equals(num(), value.num()) && checksum().equals(value.checksum())
                && Objects.equals(evmAddress(), value.evmAddress()) && Arrays.equals(alias, value.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shard(), realm(), num(), checksum(), evmAddress(), Arrays.hashCode(alias));
    }
}
