package org.hiero.evm.internal;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.hiero.evm.AbiAddress;
import org.hiero.evm.AbiAddressType;
import org.hiero.evm.AbiArray;
import org.hiero.evm.AbiArrayType;
import org.hiero.evm.AbiBool;
import org.hiero.evm.AbiBoolType;
import org.hiero.evm.AbiBytes;
import org.hiero.evm.AbiBytesType;
import org.hiero.evm.AbiFixedBytes;
import org.hiero.evm.AbiFixedBytesType;
import org.hiero.evm.AbiInt;
import org.hiero.evm.AbiIntType;
import org.hiero.evm.AbiString;
import org.hiero.evm.AbiStringType;
import org.hiero.evm.AbiTuple;
import org.hiero.evm.AbiTupleType;
import org.hiero.evm.AbiType;
import org.hiero.evm.AbiUint;
import org.hiero.evm.AbiUintType;
import org.hiero.evm.AbiValue;
import org.hiero.evm.EvmAddress;

/// The encoding of the contract ABI (Solidity documentation, "Contract ABI Specification", section "Formal
/// Specification of the Encoding"): every value is a sequence of 32-byte words; a tuple is a head of the static values
/// and of the offsets of the dynamic ones, followed by the dynamic values. Decoding is strict: data that is too short,
/// an offset outside the data, and a value with bits set that its type does not have are rejected.
public final class AbiCodec {

    private static final int WORD = 32;

    private AbiCodec() {
    }

    // ------------------------------------------------------------------------------------------- types

    /// The canonical name of a type, as used in a signature.
    ///
    /// @param type the type
    /// @return the name, e.g. `uint256` or `(address,uint256)[]`
    public static String name(final AbiType type) {
        return switch (type) {
            case AbiBoolType ignored -> "bool";
            case AbiAddressType ignored -> "address";
            case AbiUintType uint -> "uint" + uint.bits();
            case AbiIntType integer -> "int" + integer.bits();
            case AbiFixedBytesType fixed -> "bytes" + fixed.size();
            case AbiBytesType ignored -> "bytes";
            case AbiStringType ignored -> "string";
            case AbiArrayType array -> name(array.elementType()) + "[" + (array.length() == null ? "" : array.length())
                    + "]";
            case AbiTupleType tuple -> names(tuple.components());
        };
    }

    /// The names of types, separated by commas and in parentheses: `(address,uint256)`.
    ///
    /// @param types the types
    /// @return the names
    public static String names(final List<AbiType> types) {
        return "(" + String.join(",", types.stream().map(AbiCodec::name).toList()) + ")";
    }

    /// The type of a value.
    ///
    /// @param value the value
    /// @return its type
    public static AbiType typeOf(final AbiValue value) {
        return switch (value) {
            case AbiBool ignored -> new AbiBoolType();
            case AbiAddress ignored -> new AbiAddressType();
            case AbiUint uint -> new AbiUintType(uint.bits());
            case AbiInt integer -> new AbiIntType(integer.bits());
            case AbiFixedBytes fixed -> new AbiFixedBytesType((short) fixed.value().length);
            case AbiBytes ignored -> new AbiBytesType();
            case AbiString ignored -> new AbiStringType();
            case AbiArray array -> array.arrayType();
            case AbiTuple tuple -> new AbiTupleType(tuple.values().stream().map(AbiCodec::typeOf).toList());
        };
    }

    /// Whether an indexed event parameter of the type is stored as the Keccak-256 hash of its value instead of the
    /// value: every array, tuple, `bytes` and `string` (Solidity documentation, "Contract ABI Specification", section
    /// "Events").
    ///
    /// @param type the type
    /// @return whether a topic holds only the hash
    public static boolean indexedAsHash(final AbiType type) {
        return type instanceof AbiArrayType || type instanceof AbiTupleType || type instanceof AbiBytesType
                || type instanceof AbiStringType;
    }

    /// Whether a type is dynamic: its encoding is referenced by an offset instead of standing in the head.
    private static boolean dynamic(final AbiType type) {
        return switch (type) {
            case AbiBytesType ignored -> true;
            case AbiStringType ignored -> true;
            case AbiArrayType array -> array.length() == null || dynamic(array.elementType());
            case AbiTupleType tuple -> tuple.components().stream().anyMatch(AbiCodec::dynamic);
            default -> false;
        };
    }

    /// The number of bytes a value of the type takes in the head of a tuple.
    private static long headSize(final AbiType type) {
        if (dynamic(type)) {
            return WORD;
        }
        return switch (type) {
            case AbiArrayType array -> array.length() * headSize(array.elementType());
            case AbiTupleType tuple -> tuple.components().stream().mapToLong(AbiCodec::headSize).sum();
            default -> WORD;
        };
    }

    // ------------------------------------------------------------------------------------------- encoding

    /// Encodes values as a tuple.
    ///
    /// @param values the values
    /// @return the encoding
    /// @throws IllegalArgumentException if a value does not fit its type
    public static byte[] encode(final List<AbiValue> values) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        encodeTuple(values, out);
        return out.toByteArray();
    }

    /// Encodes values as a tuple, after checking that they have the given types.
    ///
    /// @param types  the types the values must have
    /// @param values the values
    /// @return the encoding
    /// @throws IllegalArgumentException if a value has another type or does not fit its type
    public static byte[] encode(final List<AbiType> types, final List<AbiValue> values) {
        if (types.size() != values.size()) {
            throw new IllegalArgumentException("Expected " + types.size() + " value(s) of the types " + names(types)
                    + " but got " + values.size());
        }
        for (int i = 0; i < types.size(); i++) {
            final AbiType actual = typeOf(values.get(i));
            if (!actual.equals(types.get(i))) {
                throw new IllegalArgumentException("Value " + i + " is a " + name(actual) + " but a "
                        + name(types.get(i)) + " is expected");
            }
        }
        return encode(values);
    }

    private static void encodeTuple(final List<AbiValue> values, final ByteArrayOutputStream out) {
        long headLength = 0;
        for (final AbiValue value : values) {
            headLength += headSize(typeOf(value));
        }
        final ByteArrayOutputStream tail = new ByteArrayOutputStream();
        for (final AbiValue value : values) {
            if (dynamic(typeOf(value))) {
                out.writeBytes(word(BigInteger.valueOf(headLength + tail.size())));
                encodeValue(value, tail);
            } else {
                encodeValue(value, out);
            }
        }
        out.writeBytes(tail.toByteArray());
    }

    private static void encodeValue(final AbiValue value, final ByteArrayOutputStream out) {
        switch (value) {
            case AbiBool bool -> out.writeBytes(word(bool.value() ? BigInteger.ONE : BigInteger.ZERO));
            case AbiAddress address -> out.writeBytes(word(new BigInteger(1, address.value().bytes())));
            case AbiUint uint -> {
                if (uint.value().bitLength() > uint.bits()) {
                    throw new IllegalArgumentException(uint.value() + " does not fit into uint" + uint.bits());
                }
                out.writeBytes(word(uint.value()));
            }
            case AbiInt integer -> {
                // a value of n bits in two's complement needs bitLength() < n
                if (integer.value().bitLength() >= integer.bits()) {
                    throw new IllegalArgumentException(integer.value() + " does not fit into int" + integer.bits());
                }
                out.writeBytes(signedWord(integer.value()));
            }
            case AbiFixedBytes fixed -> out.writeBytes(Arrays.copyOf(fixed.value(), WORD));
            case AbiBytes bytes -> encodeDynamicBytes(bytes.value(), out);
            case AbiString string -> encodeDynamicBytes(string.value().getBytes(StandardCharsets.UTF_8), out);
            case AbiArray array -> {
                final AbiArrayType type = array.arrayType();
                if (type.length() != null && type.length() != array.values().size()) {
                    throw new IllegalArgumentException("The array " + name(type) + " has " + array.values().size()
                            + " value(s)");
                }
                for (final AbiValue element : array.values()) {
                    if (!typeOf(element).equals(type.elementType())) {
                        throw new IllegalArgumentException("The array " + name(type) + " has a value of the type "
                                + name(typeOf(element)));
                    }
                }
                if (type.length() == null) {
                    out.writeBytes(word(BigInteger.valueOf(array.values().size())));
                }
                encodeTuple(array.values(), out);
            }
            case AbiTuple tuple -> encodeTuple(tuple.values(), out);
        }
    }

    private static void encodeDynamicBytes(final byte[] bytes, final ByteArrayOutputStream out) {
        out.writeBytes(word(BigInteger.valueOf(bytes.length)));
        out.writeBytes(Arrays.copyOf(bytes, (bytes.length + WORD - 1) / WORD * WORD));
    }

    /// A non-negative value as a word, left-padded with zeros.
    private static byte[] word(final BigInteger value) {
        final byte[] bytes = value.toByteArray();
        final byte[] word = new byte[WORD];
        final int length = Math.min(bytes.length, WORD);
        System.arraycopy(bytes, bytes.length - length, word, WORD - length, length);
        return word;
    }

    /// A signed value as a word in two's complement, sign-extended.
    private static byte[] signedWord(final BigInteger value) {
        final byte[] bytes = value.toByteArray();
        final byte[] word = new byte[WORD];
        Arrays.fill(word, value.signum() < 0 ? (byte) 0xff : 0);
        System.arraycopy(bytes, 0, word, WORD - bytes.length, bytes.length);
        return word;
    }

    // ------------------------------------------------------------------------------------------- decoding

    /// Decodes values of the given types from the encoding of a tuple.
    ///
    /// @param types the types
    /// @param data  the encoding
    /// @return the values
    /// @throws IllegalArgumentException if the data is no valid encoding of values of the types
    public static List<AbiValue> decode(final List<AbiType> types, final byte[] data) {
        return decodeTuple(types, data, 0);
    }

    private static List<AbiValue> decodeTuple(final List<AbiType> types, final byte[] data, final int start) {
        final List<AbiValue> values = new ArrayList<>(types.size());
        long position = start;
        for (final AbiType type : types) {
            if (dynamic(type)) {
                final long offset = readLength(data, position);
                values.add(decodeValue(type, data, checkedPosition(data, start + offset)));
                position += WORD;
            } else {
                values.add(decodeValue(type, data, checkedPosition(data, position)));
                position += headSize(type);
            }
        }
        return values;
    }

    private static AbiValue decodeValue(final AbiType type, final byte[] data, final int at) {
        return switch (type) {
            case AbiBoolType ignored -> {
                final BigInteger value = readUnsigned(data, at);
                if (value.compareTo(BigInteger.ONE) > 0) {
                    throw new IllegalArgumentException("The word at " + at + " is no bool");
                }
                yield new AbiBool(value.signum() == 1);
            }
            case AbiAddressType ignored -> {
                final BigInteger value = readUnsigned(data, at);
                if (value.bitLength() > 160) {
                    throw new IllegalArgumentException("The word at " + at + " is no address");
                }
                yield new AbiAddress(EvmAddress.fromBytes(Arrays.copyOfRange(word(value), 12, WORD)));
            }
            case AbiUintType uint -> {
                final BigInteger value = readUnsigned(data, at);
                if (value.bitLength() > uint.bits()) {
                    throw new IllegalArgumentException("The word at " + at + " is no uint" + uint.bits());
                }
                yield new AbiUint(value, uint.bits());
            }
            case AbiIntType integer -> {
                final BigInteger value = new BigInteger(readWord(data, at));
                if (value.bitLength() >= integer.bits()) {
                    throw new IllegalArgumentException("The word at " + at + " is no int" + integer.bits());
                }
                yield new AbiInt(value, integer.bits());
            }
            case AbiFixedBytesType fixed -> {
                final byte[] word = readWord(data, at);
                for (int i = fixed.size(); i < WORD; i++) {
                    if (word[i] != 0) {
                        throw new IllegalArgumentException("The word at " + at + " is no bytes" + fixed.size());
                    }
                }
                yield new AbiFixedBytes(Arrays.copyOf(word, fixed.size()));
            }
            case AbiBytesType ignored -> new AbiBytes(readDynamicBytes(data, at));
            case AbiStringType ignored -> new AbiString(utf8(readDynamicBytes(data, at), at));
            case AbiArrayType array -> {
                final long length;
                final int elements;
                if (array.length() == null) {
                    length = readLength(data, at);
                    elements = checkedPosition(data, at + (long) WORD);
                } else {
                    length = array.length();
                    elements = at;
                }
                // every element takes at least one word: a longer array cannot be in the data
                if (length * WORD > data.length - elements) {
                    throw new IllegalArgumentException("The array at " + at + " has more elements than the data");
                }
                yield new AbiArray(array, decodeTuple(Collections.nCopies((int) length, array.elementType()), data,
                        elements));
            }
            case AbiTupleType tuple -> new AbiTuple(decodeTuple(tuple.components(), data, at));
        };
    }

    private static byte[] readDynamicBytes(final byte[] data, final int at) {
        final long length = readLength(data, at);
        if (length > data.length - (at + (long) WORD)) {
            throw new IllegalArgumentException("The bytes at " + at + " are longer than the data");
        }
        return Arrays.copyOfRange(data, at + WORD, at + WORD + (int) length);
    }

    private static String utf8(final byte[] bytes, final int at) {
        try {
            return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString();
        } catch (final CharacterCodingException e) {
            throw new IllegalArgumentException("The string at " + at + " is no UTF-8", e);
        }
    }

    private static byte[] readWord(final byte[] data, final long at) {
        if (at < 0 || at + WORD > data.length) {
            throw new IllegalArgumentException("The data ends before the word at " + at);
        }
        return Arrays.copyOfRange(data, (int) at, (int) at + WORD);
    }

    private static BigInteger readUnsigned(final byte[] data, final long at) {
        return new BigInteger(1, readWord(data, at));
    }

    /// An offset or a length: a word that must be a position inside the data.
    private static long readLength(final byte[] data, final long at) {
        final BigInteger value = readUnsigned(data, at);
        if (value.compareTo(BigInteger.valueOf(data.length)) > 0) {
            throw new IllegalArgumentException("The offset or length at " + at + " points outside the data");
        }
        return value.longValueExact();
    }

    private static int checkedPosition(final byte[] data, final long position) {
        if (position > data.length) {
            throw new IllegalArgumentException("The position " + position + " is outside the data");
        }
        return (int) position;
    }
}
