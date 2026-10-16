package org.hiero.ledger.internal;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.hiero.ledger.BaseAddress;
import org.hiero.ledger.EvmAddress;
import org.hiero.ledger.Network;
import org.jspecify.annotations.Nullable;

/// The text form of the entity ids (`shard.realm.num`, `shard.realm.0x<40 hex>`, each with an optional
/// `-<checksum>`) and their HIP-15 checksum.
public final class EntityIds {

    private static final Pattern ENTITY_ID =
            Pattern.compile("(\\d+)\\.(\\d+)\\.(?:(\\d+)|0x([0-9a-fA-F]{40}))(?:-([a-z]{5}))?");

    private static final int P3 = 26 * 26 * 26;
    private static final int P5 = 26 * 26 * 26 * 26 * 26;
    private static final int M = 1_000_003;
    private static final int W = 31;

    private EntityIds() {
    }

    /// A parsed entity id.
    ///
    /// @param shard      the shard
    /// @param realm      the realm
    /// @param num        the entity number, `null` for the EVM form
    /// @param evmAddress the EVM address, `null` for the numeric form
    /// @param checksum   the checksum, empty if the text has none
    public record Parsed(long shard, long realm, @Nullable Long num, @Nullable EvmAddress evmAddress,
                         String checksum) {
    }

    /// Parses an entity id.
    ///
    /// @param text    the text
    /// @param evmForm whether the EVM form `shard.realm.0x<40 hex>` is allowed
    /// @return the parts
    /// @throws IllegalArgumentException if the text is no entity id
    public static Parsed parse(final String text, final boolean evmForm) {
        final Matcher matcher = ENTITY_ID.matcher(text);
        if (!matcher.matches() || (!evmForm && matcher.group(4) != null)) {
            throw new IllegalArgumentException("'" + text + "' is no entity id of the form shard.realm."
                    + (evmForm ? "num or shard.realm.0x<40 hex digits>" : "num") + ", optionally with -checksum");
        }
        final long shard = Long.parseLong(matcher.group(1));
        final long realm = Long.parseLong(matcher.group(2));
        final String checksum = matcher.group(5) == null ? "" : matcher.group(5);
        if (matcher.group(3) != null) {
            return new Parsed(shard, realm, Long.parseLong(matcher.group(3)), null, checksum);
        }
        return new Parsed(shard, realm, null, new EvmAddress(HexFormat.of().parseHex(matcher.group(4))), checksum);
    }

    /// The canonical text form without checksum.
    ///
    /// @param address    the entity id
    /// @param evmAddress its EVM address, used if it has no number
    /// @return the text
    public static String toString(final BaseAddress address, final @Nullable EvmAddress evmAddress) {
        final Long num = address.num();
        final String entity = num != null ? Long.toUnsignedString(num)
                : evmAddress != null ? "0x" + HexFormat.of().formatHex(evmAddress.bytes()) : "?";
        return Long.toUnsignedString(address.shard()) + "." + Long.toUnsignedString(address.realm()) + "." + entity;
    }

    /// The text form with the `-<checksum>` suffix, if the id carries a checksum.
    ///
    /// @param address    the entity id
    /// @param evmAddress its EVM address, used if it has no number
    /// @return the text
    public static String toStringWithChecksum(final BaseAddress address, final @Nullable EvmAddress evmAddress) {
        final String text = toString(address, evmAddress);
        return address.checksum().isEmpty() ? text : text + "-" + address.checksum();
    }

    /// Validates the checksum of an id against a network. An id without checksum, or one in EVM form (HIP-15 defines
    /// no checksum for it), has nothing to contradict and is valid.
    ///
    /// @param address the entity id
    /// @param network the network
    /// @return whether the checksum is the one of the id on that network
    public static boolean validateChecksum(final BaseAddress address, final Network<?> network) {
        if (address.checksum().isEmpty() || address.num() == null) {
            return true;
        }
        return address.checksum().equals(checksum(toString(address, null), network.id()));
    }

    /// The HIP-15 checksum of `shard.realm.num` on a ledger.
    ///
    /// @param entity   the id as `shard.realm.num`
    /// @param ledgerId the id of the ledger
    /// @return the five lowercase letters
    public static String checksum(final String entity, final byte[] ledgerId) {
        final byte[] ascii = entity.getBytes(StandardCharsets.US_ASCII);
        final int[] digits = new int[ascii.length];
        for (int i = 0; i < ascii.length; i++) {
            digits[i] = ascii[i] == '.' ? 10 : ascii[i] - '0';
        }
        final byte[] ledger = new byte[ledgerId.length + 6];
        System.arraycopy(ledgerId, 0, ledger, 0, ledgerId.length);
        long sd0 = 0;
        long sd1 = 0;
        long sd = 0;
        for (int i = 0; i < digits.length; i++) {
            sd = (W * sd + digits[i]) % P3;
            if (i % 2 == 0) {
                sd0 = (sd0 + digits[i]) % 11;
            } else {
                sd1 = (sd1 + digits[i]) % 11;
            }
        }
        long sh = 0;
        for (final byte b : ledger) {
            sh = (W * sh + (b & 0xff)) % P5;
        }
        final long c = ((((digits.length % 5) * 11L + sd0) * 11 + sd1) * P3 + sd + sh) % P5;
        long cp = (c * M) % P5;
        final char[] letters = new char[5];
        for (int i = 4; i >= 0; i--) {
            letters[i] = (char) ('a' + cp % 26);
            cp /= 26;
        }
        return new String(letters);
    }
}
