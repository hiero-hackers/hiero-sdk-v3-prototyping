package org.hiero.mirrornode.internal;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.hiero.authority.Authority;
import org.hiero.authority.AuthorityFactory;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.ledger.ContractId;
import org.hiero.ledger.EvmAddress;
import org.hiero.ledger.TransactionId;
import org.hiero.mirrornode.internal.Json.JsonObject;
import org.jspecify.annotations.Nullable;

/// The conversions of the value formats of the Mirror Node REST API: entity ids, timestamps, transaction ids, Base64,
/// Base32 and keys.
public final class Values {

    /// Stands in for the open end of a timestamp range (`to: null`, the current state) where the API type requires an
    /// end: the latest representable time.
    public static final ZonedDateTime OPEN_END = ZonedDateTime.of(LocalDateTime.MAX, ZoneOffset.UTC);

    private static final Pattern TIMESTAMP = Pattern.compile("(\\d+)(?:\\.(\\d{1,9}))?");
    private static final Pattern MIRROR_TRANSACTION_ID = Pattern.compile("(\\d+\\.\\d+\\.\\d+)-(\\d+)-(\\d+)");
    private static final String BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    private Values() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /// Parses a timestamp of the form "seconds.nanoseconds" (the nanoseconds as a decimal fraction).
    ///
    /// @param text the timestamp
    /// @return the time in UTC, or `null` if `text` is `null`
    public static @Nullable ZonedDateTime timestamp(final @Nullable String text) {
        if (text == null) {
            return null;
        }
        final Matcher matcher = TIMESTAMP.matcher(text.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("'" + text + "' is no timestamp of the form seconds.nanoseconds");
        }
        final String fraction = matcher.group(2) == null ? "0" : (matcher.group(2) + "00000000").substring(0, 9);
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(matcher.group(1)),
                Long.parseLong(fraction)), ZoneOffset.UTC);
    }

    /// Returns the required timestamp member `name` of `json`.
    ///
    /// @param json the object
    /// @param name the member name
    /// @return the time
    public static ZonedDateTime requiredTimestamp(final JsonObject json, final String name) {
        final ZonedDateTime time = timestamp(json.optString(name));
        if (time == null) {
            throw new IllegalArgumentException("member '" + name + "' is missing");
        }
        return time;
    }

    /// Returns the start of the timestamp range member `name` ({from, to}) of `json`.
    ///
    /// @param json the object
    /// @param name the member name of the range
    /// @return the start of the range
    public static ZonedDateTime rangeFrom(final JsonObject json, final String name) {
        return requiredTimestamp(json.object(name), "from");
    }

    /// Returns the end of the timestamp range member `name` of `json`, or `OPEN_END` if the range is open.
    ///
    /// @param json the object
    /// @param name the member name of the range
    /// @return the end of the range
    public static ZonedDateTime rangeTo(final JsonObject json, final String name) {
        final ZonedDateTime to = timestamp(json.object(name).optString("to"));
        return to == null ? OPEN_END : to;
    }

    /// Returns a time given as integer seconds since the epoch.
    ///
    /// @param seconds the seconds
    /// @return the time in UTC
    public static ZonedDateTime epochSeconds(final long seconds) {
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(seconds), ZoneOffset.UTC);
    }

    /// Returns a time given as integer nanoseconds since the epoch.
    ///
    /// @param nanoseconds the nanoseconds, or `null`
    /// @return the time in UTC, or `null`
    public static @Nullable ZonedDateTime epochNanoseconds(final @Nullable Long nanoseconds) {
        if (nanoseconds == null) {
            return null;
        }
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(Math.floorDiv(nanoseconds, 1_000_000_000L),
                Math.floorMod(nanoseconds, 1_000_000_000L)), ZoneOffset.UTC);
    }

    /// Parses an account id ("shard.realm.num"), or returns `null` for `null`.
    ///
    /// @param text the id
    /// @return the account id
    public static @Nullable AccountId optAccount(final @Nullable String text) {
        return text == null || text.isEmpty() ? null : AccountId.fromString(text);
    }

    /// Returns the required account id member `name` of `json`.
    ///
    /// @param json the object
    /// @param name the member name
    /// @return the account id
    public static AccountId account(final JsonObject json, final String name) {
        return AccountId.fromString(json.string(name));
    }

    /// Returns the required entity id member `name` of `json` as an `Address`.
    ///
    /// @param json the object
    /// @param name the member name
    /// @return the address
    public static Address address(final JsonObject json, final String name) {
        return Address.fromString(json.string(name));
    }

    /// Parses an entity id as an `Address`, or returns `null` for `null`.
    ///
    /// @param text the id
    /// @return the address
    public static @Nullable Address optAddress(final @Nullable String text) {
        return text == null || text.isEmpty() ? null : Address.fromString(text);
    }

    /// Parses an EVM address ("0x" and 40 hex digits), or returns `null` for `null` or an empty text.
    ///
    /// @param text the address
    /// @return the EVM address
    public static @Nullable EvmAddress optEvmAddress(final @Nullable String text) {
        return text == null || text.isEmpty() ? null : EvmAddress.fromString(text.toLowerCase(Locale.ROOT));
    }

    /// Decodes Base64, or returns `null` for `null`.
    ///
    /// @param text the Base64 text
    /// @return the bytes
    public static byte @Nullable [] optBase64(final @Nullable String text) {
        return text == null ? null : Base64.getDecoder().decode(text);
    }

    /// Decodes Base64; `null` is the empty array.
    ///
    /// @param text the Base64 text
    /// @return the bytes
    public static byte[] base64(final @Nullable String text) {
        final byte[] bytes = optBase64(text);
        return bytes == null ? new byte[0] : bytes;
    }

    /// Decodes RFC 4648 Base32 without padding (the form of a HIP-32 account alias), or returns `null` for `null`.
    ///
    /// @param text the Base32 text
    /// @return the bytes
    public static byte @Nullable [] optBase32(final @Nullable String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        final String normalized = text.toUpperCase(Locale.ROOT).replace("=", "");
        final byte[] result = new byte[normalized.length() * 5 / 8];
        int buffer = 0;
        int bits = 0;
        int index = 0;
        for (final char c : normalized.toCharArray()) {
            final int value = BASE32_ALPHABET.indexOf(c);
            if (value < 0) {
                throw new IllegalArgumentException("'" + text + "' is no Base32 text");
            }
            buffer = (buffer << 5) | value;
            bits += 5;
            if (bits >= 8) {
                bits -= 8;
                result[index++] = (byte) (buffer >> bits);
            }
        }
        return result;
    }

    /// Parses a transaction id of the Mirror Node REST API ("0.0.1234-1586567700-453054000").
    ///
    /// @param text the transaction id
    /// @param nonce the nonce of the transaction; 0 for a transaction that is no child transaction
    /// @return the transaction id
    public static TransactionId transactionId(final String text, final int nonce) {
        final Matcher matcher = MIRROR_TRANSACTION_ID.matcher(text);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("'" + text + "' is no transaction id of the form shard.realm.num-s-n");
        }
        return transactionId(matcher.group(1), matcher.group(2) + "." + matcher.group(3), nonce);
    }

    /// Creates a transaction id of a payer account and a valid start timestamp ("seconds.nanoseconds").
    ///
    /// @param account the payer account ("shard.realm.num")
    /// @param validStart the valid start timestamp
    /// @param nonce the nonce; 0 for a transaction that is no child transaction
    /// @return the transaction id
    public static TransactionId transactionId(final String account, final String validStart, final int nonce) {
        final ZonedDateTime start = timestamp(validStart);
        if (start == null) {
            throw new IllegalArgumentException("the valid start is missing");
        }
        return TransactionId.fromString(account + "@" + start.toEpochSecond() + "." + start.getNano()
                + (nonce > 0 ? "/" + nonce : ""));
    }

    /// Formats a transaction id the way the Mirror Node REST API expects it in a path
    /// ("0.0.1234-1586567700-453054000").
    ///
    /// @param transactionId the transaction id
    /// @return the text
    public static String mirrorTransactionId(final TransactionId transactionId) {
        final AccountId account = transactionId.accountId();
        final Instant start = transactionId.validStart().toInstant();
        return account.shard() + "." + account.realm() + "." + account.num() + "-" + start.getEpochSecond() + "-"
                + String.format(Locale.ROOT, "%09d", start.getNano());
    }

    /// Converts a key of the Mirror Node REST API ({"_type": …, "key": …}) to an `Authority`.
    ///
    /// @param key the key, or `null`
    /// @return the authority, or `null` if there is no key (or an empty key list, the key of an immutable entity)
    public static @Nullable Authority authority(final @Nullable JsonObject key) {
        if (key == null || !key.has("key")) {
            return null;
        }
        final String type = key.string("_type");
        final byte[] bytes = HexFormat.of().parseHex(key.string("key").toLowerCase(Locale.ROOT));
        return switch (type) {
            case "ED25519" -> AuthorityFactory.of(KeysFactory.createPublicKey(KeyAlgorithm.ED25519, bytes));
            case "ECDSA_SECP256K1" -> AuthorityFactory.of(KeysFactory.createPublicKey(KeyAlgorithm.ECDSA, bytes));
            case "ProtobufEncoded" -> new ProtobufKey(bytes).authority();
            default -> throw new IllegalArgumentException("unknown key type '" + type + "'");
        };
    }

    /// Converts a list of keys to authorities, leaving out the keys without authority.
    ///
    /// @param keys the keys
    /// @return the authorities
    public static List<Authority> authorities(final List<JsonObject> keys) {
        final List<Authority> result = new ArrayList<>();
        for (final JsonObject key : keys) {
            final Authority authority = authority(key);
            if (authority != null) {
                result.add(authority);
            }
        }
        return List.copyOf(result);
    }

    /// A decoder of the HAPI `Key` message (`basic_types.proto`), for keys the REST API returns protobuf-encoded:
    /// key lists, threshold keys and contract ids.
    private static final class ProtobufKey {

        private final byte[] bytes;
        private int position;
        private final int end;

        ProtobufKey(final byte[] bytes) {
            this(bytes, 0, bytes.length);
        }

        private ProtobufKey(final byte[] bytes, final int start, final int end) {
            this.bytes = bytes;
            this.position = start;
            this.end = end;
        }

        /// Decodes a `Key`; `null` for an empty key list.
        @Nullable Authority authority() {
            Authority result = null;
            while (position < end) {
                final long tag = varint();
                final int field = (int) (tag >>> 3);
                final int wireType = (int) (tag & 7);
                if (wireType != 2) {
                    skip(wireType);
                    continue;
                }
                final int length = (int) varint();
                final ProtobufKey content = new ProtobufKey(bytes, position, position + length);
                final byte[] value = Arrays.copyOfRange(bytes, position, position + length);
                position += length;
                result = switch (field) {
                    case 1 -> AuthorityFactory.ofContract(content.contractId());
                    case 2 -> AuthorityFactory.of(KeysFactory.createPublicKey(KeyAlgorithm.ED25519, value));
                    case 5 -> content.thresholdKey();
                    case 6 -> content.keyList();
                    case 7 -> AuthorityFactory.of(KeysFactory.createPublicKey(KeyAlgorithm.ECDSA, value));
                    case 8 -> AuthorityFactory.ofDelegatable(content.contractId());
                    default -> throw new IllegalArgumentException("unsupported key (Key field " + field + ")");
                };
            }
            return result;
        }

        private @Nullable Authority thresholdKey() {
            int threshold = 0;
            List<Authority> children = List.of();
            while (position < end) {
                final long tag = varint();
                final int field = (int) (tag >>> 3);
                if (field == 1 && (tag & 7) == 0) {
                    threshold = (int) varint();
                } else if (field == 2 && (tag & 7) == 2) {
                    final int length = (int) varint();
                    final ProtobufKey list = new ProtobufKey(bytes, position, position + length);
                    position += length;
                    children = list.children();
                } else {
                    skip((int) (tag & 7));
                }
            }
            if (children.isEmpty()) {
                return null;
            }
            return AuthorityFactory.of(threshold, children.toArray(Authority[]::new));
        }

        private @Nullable Authority keyList() {
            final List<Authority> children = children();
            if (children.isEmpty()) {
                return null;
            }
            return AuthorityFactory.of(children.toArray(Authority[]::new));
        }

        private List<Authority> children() {
            final List<Authority> children = new ArrayList<>();
            while (position < end) {
                final long tag = varint();
                if ((tag >>> 3) == 1 && (tag & 7) == 2) {
                    final int length = (int) varint();
                    final Authority child = new ProtobufKey(bytes, position, position + length).authority();
                    position += length;
                    if (child != null) {
                        children.add(child);
                    }
                } else {
                    skip((int) (tag & 7));
                }
            }
            return children;
        }

        private ContractId contractId() {
            long shard = 0;
            long realm = 0;
            Long num = null;
            EvmAddress evmAddress = null;
            while (position < end) {
                final long tag = varint();
                final int field = (int) (tag >>> 3);
                if (field == 1 && (tag & 7) == 0) {
                    shard = varint();
                } else if (field == 2 && (tag & 7) == 0) {
                    realm = varint();
                } else if (field == 3 && (tag & 7) == 0) {
                    num = varint();
                } else if (field == 4 && (tag & 7) == 2) {
                    final int length = (int) varint();
                    evmAddress = EvmAddress.fromBytes(Arrays.copyOfRange(bytes, position, position + length));
                    position += length;
                } else {
                    skip((int) (tag & 7));
                }
            }
            if (evmAddress != null) {
                return ContractId.fromEvmAddress(shard, realm, evmAddress);
            }
            return new ContractId(shard, realm, "", num == null ? 0L : num, null);
        }

        private long varint() {
            long result = 0;
            for (int shift = 0; shift < 64; shift += 7) {
                if (position >= end) {
                    throw new IllegalArgumentException("truncated protobuf key");
                }
                final byte b = bytes[position++];
                result |= (long) (b & 0x7F) << shift;
                if ((b & 0x80) == 0) {
                    return result;
                }
            }
            throw new IllegalArgumentException("malformed varint in protobuf key");
        }

        private void skip(final int wireType) {
            switch (wireType) {
                case 0 -> varint();
                case 1 -> position += 8;
                case 2 -> {
                    final int length = (int) varint();
                    position += length;
                }
                case 5 -> position += 4;
                default -> throw new IllegalArgumentException("unsupported wire type " + wireType + " in protobuf key");
            }
            if (position > end) {
                throw new IllegalArgumentException("truncated protobuf key");
            }
        }
    }
}
