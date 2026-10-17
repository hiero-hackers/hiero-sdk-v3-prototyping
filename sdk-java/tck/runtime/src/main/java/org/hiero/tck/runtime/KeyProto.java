package org.hiero.tck.runtime;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.hiero.authority.Authority;
import org.hiero.authority.AuthorityList;
import org.hiero.authority.ContractAuthority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.keys.PublicKey;
import org.hiero.ledger.ContractId;

/**
 * The protobuf {@code Key} of HAPI, which the TCK uses for key lists and threshold keys, read and written by hand.
 * {@code Authority} has no byte form in the API (see the {@code ## Questions & Comments} of {@code authority.md}), and
 * the protobuf classes of the SDK are not public API, so the runtime of the TCK encodes the few messages itself:
 * {@code Key} (1 contractID, 2 ed25519, 5 thresholdKey, 6 keyList, 7 ECDSA_secp256k1, 8 delegatable_contract_id),
 * {@code ThresholdKey} (1 threshold, 2 keys), {@code KeyList} (1 keys) and {@code ContractID} (1 shard, 2 realm,
 * 3 contract number).
 */
final class KeyProto {

    private static final int VARINT = 0;
    private static final int LENGTH_DELIMITED = 2;

    private KeyProto() {
    }

    /**
     * Reads a {@code Key}.
     *
     * @param bytes the serialized message
     * @return the authority
     * @throws IllegalArgumentException if the bytes are no key the API has an authority for
     */
    static Authority decode(final byte[] bytes) {
        final Reader reader = new Reader(bytes);
        Authority result = null;
        while (reader.hasMore()) {
            final int tag = reader.varintInt();
            final int field = tag >>> 3;
            if ((tag & 7) != LENGTH_DELIMITED) {
                throw new IllegalArgumentException("Unexpected wire type in a Key: " + tag);
            }
            final byte[] value = reader.bytes();
            result = switch (field) {
                case 1 -> new ContractAuthority(contractId(value), false);
                case 2 -> new PublicKeyAuthority(KeysFactory.createPublicKey(KeyAlgorithm.ED25519, value));
                case 5 -> thresholdKey(value);
                case 6 -> {
                    final List<Authority> keys = keyList(value);
                    yield new AuthorityList(keys, keys.size());
                }
                case 7 -> new PublicKeyAuthority(KeysFactory.createPublicKey(KeyAlgorithm.ECDSA, value));
                case 8 -> new ContractAuthority(contractId(value), true);
                default -> throw new IllegalArgumentException("A Key of field " + field + " has no authority");
            };
        }
        if (result == null) {
            throw new IllegalArgumentException("The bytes hold no Key");
        }
        return result;
    }

    /**
     * Writes a {@code Key}.
     *
     * @param authority the authority
     * @return the serialized message
     */
    static byte[] encode(final Authority authority) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        switch (authority) {
            case PublicKeyAuthority(PublicKey publicKey) ->
                    field(out, publicKey.algorithm() == KeyAlgorithm.ECDSA ? 7 : 2, publicKey.toRawBytes());
            case AuthorityList(List<Authority> children, int threshold) -> {
                final ByteArrayOutputStream list = new ByteArrayOutputStream();
                children.forEach(child -> field(list, 1, encode(child)));
                if (threshold == children.size()) {
                    field(out, 6, list.toByteArray());
                } else {
                    final ByteArrayOutputStream thresholdKey = new ByteArrayOutputStream();
                    varintField(thresholdKey, 1, threshold);
                    field(thresholdKey, 2, list.toByteArray());
                    field(out, 5, thresholdKey.toByteArray());
                }
            }
            case ContractAuthority(ContractId contractId, boolean delegatable) -> {
                final ByteArrayOutputStream id = new ByteArrayOutputStream();
                varintField(id, 1, contractId.shard());
                varintField(id, 2, contractId.realm());
                final Long num = contractId.num();
                if (num == null) {
                    throw new IllegalArgumentException("A contract key needs a contract number");
                }
                varintField(id, 3, num);
                field(out, delegatable ? 8 : 1, id.toByteArray());
            }
        }
        return out.toByteArray();
    }

    private static Authority thresholdKey(final byte[] bytes) {
        final Reader reader = new Reader(bytes);
        int threshold = 0;
        List<Authority> keys = List.of();
        while (reader.hasMore()) {
            final int tag = reader.varintInt();
            switch (tag >>> 3) {
                case 1 -> threshold = reader.varintInt();
                case 2 -> keys = keyList(reader.bytes());
                default -> reader.skip(tag & 7);
            }
        }
        return new AuthorityList(keys, threshold);
    }

    private static List<Authority> keyList(final byte[] bytes) {
        final Reader reader = new Reader(bytes);
        final List<Authority> keys = new ArrayList<>();
        while (reader.hasMore()) {
            final int tag = reader.varintInt();
            if (tag >>> 3 == 1) {
                keys.add(decode(reader.bytes()));
            } else {
                reader.skip(tag & 7);
            }
        }
        return keys;
    }

    private static ContractId contractId(final byte[] bytes) {
        final Reader reader = new Reader(bytes);
        long shard = 0;
        long realm = 0;
        long num = 0;
        while (reader.hasMore()) {
            final int tag = reader.varintInt();
            switch (tag >>> 3) {
                case 1 -> shard = reader.varint();
                case 2 -> realm = reader.varint();
                case 3 -> num = reader.varint();
                default -> reader.skip(tag & 7);
            }
        }
        return new ContractId(shard, realm, "", num, null);
    }

    private static void field(final ByteArrayOutputStream out, final int field, final byte[] value) {
        varint(out, (long) field << 3 | LENGTH_DELIMITED);
        varint(out, value.length);
        out.writeBytes(value);
    }

    private static void varintField(final ByteArrayOutputStream out, final int field, final long value) {
        if (value != 0) {
            varint(out, (long) field << 3 | VARINT);
            varint(out, value);
        }
    }

    private static void varint(final ByteArrayOutputStream out, final long value) {
        long rest = value;
        while ((rest & ~0x7FL) != 0) {
            out.write((int) (rest & 0x7F | 0x80));
            rest >>>= 7;
        }
        out.write((int) rest);
    }

    /** Reads the wire format. */
    private static final class Reader {

        private final byte[] bytes;
        private int position;

        Reader(final byte[] bytes) {
            this.bytes = bytes;
        }

        boolean hasMore() {
            return position < bytes.length;
        }

        long varint() {
            long result = 0;
            for (int shift = 0; shift < 64; shift += 7) {
                if (position >= bytes.length) {
                    throw new IllegalArgumentException("Truncated varint");
                }
                final byte b = bytes[position++];
                result |= (long) (b & 0x7F) << shift;
                if ((b & 0x80) == 0) {
                    return result;
                }
            }
            throw new IllegalArgumentException("Malformed varint");
        }

        int varintInt() {
            return (int) varint();
        }

        byte[] bytes() {
            final int length = varintInt();
            if (length < 0 || position + length > bytes.length) {
                throw new IllegalArgumentException("Truncated field");
            }
            final byte[] value = java.util.Arrays.copyOfRange(bytes, position, position + length);
            position += length;
            return value;
        }

        void skip(final int wireType) {
            switch (wireType) {
                case VARINT -> varint();
                case LENGTH_DELIMITED -> bytes();
                case 1 -> position += 8;
                case 5 -> position += 4;
                default -> throw new IllegalArgumentException("Unsupported wire type " + wireType);
            }
        }
    }
}
