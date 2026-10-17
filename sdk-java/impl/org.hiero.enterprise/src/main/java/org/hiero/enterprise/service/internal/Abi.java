package org.hiero.enterprise.service.internal;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.hiero.enterprise.service.contract.Param;

/// The Solidity contract ABI encoding of the parameters of a function or constructor call.
///
/// Every value is encoded on its own when its `Param` is created (see `AbiParamSupplier`); a call puts the encodings
/// together: the head holds the encoding of every static value and the offset of every dynamic one, the tail holds the
/// encodings of the dynamic values.
public final class Abi {

    /// The size of an ABI word.
    static final int WORD = 32;

    private Abi() {
    }

    /// Creates a parameter.
    ///
    /// @param value    the value of the host language
    /// @param type     the Solidity type
    /// @param encoding the ABI encoding of the value
    /// @param <L>      the type of the value
    /// @return the parameter
    public static <L> Param<L, byte[]> param(final L value, final String type, final byte[] encoding) {
        Objects.requireNonNull(value, "value must not be null");
        return new Param<>(value, type, new AbiParamSupplier(encoding));
    }

    /// Encodes an unsigned integer of the given bit size.
    ///
    /// @param value the value
    /// @param bits  the size of the Solidity type (8 to 256)
    /// @return the word
    /// @throws IllegalArgumentException if the value is outside the range of the type
    public static byte[] encodeUnsigned(final BigInteger value, final int bits) {
        Objects.requireNonNull(value, "value must not be null");
        final BigInteger max = BigInteger.ONE.shiftLeft(bits).subtract(BigInteger.ONE);
        if (value.signum() < 0 || value.compareTo(max) > 0) {
            throw new IllegalArgumentException("value must be between 0 and " + max + " for uint" + bits);
        }
        return word(value);
    }

    /// Encodes a signed integer of the given bit size.
    ///
    /// @param value the value
    /// @param bits  the size of the Solidity type (8 to 256)
    /// @return the word
    /// @throws IllegalArgumentException if the value is outside the range of the type
    public static byte[] encodeSigned(final BigInteger value, final int bits) {
        Objects.requireNonNull(value, "value must not be null");
        final BigInteger min = BigInteger.ONE.shiftLeft(bits - 1).negate();
        final BigInteger max = BigInteger.ONE.shiftLeft(bits - 1).subtract(BigInteger.ONE);
        if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            throw new IllegalArgumentException("value must be between " + min + " and " + max + " for int" + bits);
        }
        return word(value);
    }

    /// Encodes a `bool`.
    ///
    /// @param value the value
    /// @return the word
    public static byte[] encodeBool(final boolean value) {
        return word(value ? BigInteger.ONE : BigInteger.ZERO);
    }

    /// Encodes an `address`: the 20 bytes, left-padded with zeros.
    ///
    /// @param address the 20 bytes of the address
    /// @return the word
    public static byte[] encodeAddress(final byte[] address) {
        Objects.requireNonNull(address, "address must not be null");
        if (address.length != 20) {
            throw new IllegalArgumentException("an address must have 20 bytes, not " + address.length);
        }
        final byte[] word = new byte[WORD];
        System.arraycopy(address, 0, word, WORD - address.length, address.length);
        return word;
    }

    /// Encodes a fixed-size byte array `bytesN`: the bytes, right-padded with zeros.
    ///
    /// @param value the bytes, at most `size`
    /// @param size  the size N of the Solidity type (1 to 32)
    /// @return the word
    /// @throws IllegalArgumentException if the value has more than `size` bytes
    public static byte[] encodeFixedBytes(final byte[] value, final int size) {
        Objects.requireNonNull(value, "value must not be null");
        if (value.length > size) {
            throw new IllegalArgumentException("value must have at most " + size + " bytes for bytes" + size
                    + ", not " + value.length);
        }
        return Arrays.copyOf(value, WORD);
    }

    /// Encodes a dynamic byte sequence (`bytes`, or the UTF-8 bytes of a `string`): the length, followed by the bytes
    /// right-padded with zeros to a multiple of 32.
    ///
    /// @param value the bytes
    /// @return the encoding
    public static byte[] encodeDynamic(final byte[] value) {
        Objects.requireNonNull(value, "value must not be null");
        final int padded = (value.length + WORD - 1) / WORD * WORD;
        final byte[] encoding = new byte[WORD + padded];
        System.arraycopy(word(BigInteger.valueOf(value.length)), 0, encoding, 0, WORD);
        System.arraycopy(value, 0, encoding, WORD, value.length);
        return encoding;
    }

    /// Encodes the UTF-8 bytes of a `string`.
    ///
    /// @param value the string
    /// @return the encoding
    public static byte[] encodeString(final String value) {
        Objects.requireNonNull(value, "value must not be null");
        return encodeDynamic(value.getBytes(StandardCharsets.UTF_8));
    }

    /// Returns true if values of the Solidity type are encoded in the tail of a call.
    ///
    /// @param type the Solidity type
    /// @return true for `string`, `bytes` and arrays without a fixed size
    public static boolean isDynamic(final String type) {
        return "string".equals(type) || "bytes".equals(type) || type.endsWith("[]");
    }

    /// Encodes the arguments of a call (or of a constructor), without a function selector.
    ///
    /// @param params the parameters, in order
    /// @return the encoding
    /// @throws IllegalArgumentException if a parameter was not created by `ContractFactory`
    public static byte[] encodeArguments(final List<? extends Param<?, ?>> params) {
        Objects.requireNonNull(params, "params must not be null");
        final ByteArrayOutputStream head = new ByteArrayOutputStream();
        final ByteArrayOutputStream tail = new ByteArrayOutputStream();
        final int headSize = params.size() * WORD;
        for (final Param<?, ?> param : params) {
            Objects.requireNonNull(param, "params must not contain null");
            final byte[] encoding = encodingOf(param);
            if (isDynamic(param.nativeType())) {
                head.writeBytes(word(BigInteger.valueOf((long) headSize + tail.size())));
                tail.writeBytes(encoding);
            } else {
                head.writeBytes(encoding);
            }
        }
        head.writeBytes(tail.toByteArray());
        return head.toByteArray();
    }

    /// Encodes a function call: the selector of the function followed by the arguments.
    ///
    /// @param functionName the name of the function
    /// @param params       the parameters, in order
    /// @return the call data
    /// @throws IllegalArgumentException if the name is blank or a parameter was not created by `ContractFactory`
    public static byte[] encodeCall(final String functionName, final List<? extends Param<?, ?>> params) {
        final byte[] selector = selector(signature(functionName, params));
        final byte[] arguments = encodeArguments(params);
        final byte[] call = Arrays.copyOf(selector, selector.length + arguments.length);
        System.arraycopy(arguments, 0, call, selector.length, arguments.length);
        return call;
    }

    /// The canonical signature of a function, e.g. `transfer(address,uint256)`.
    ///
    /// @param functionName the name of the function
    /// @param params       the parameters, in order
    /// @return the signature
    /// @throws IllegalArgumentException if the name is blank
    public static String signature(final String functionName, final List<? extends Param<?, ?>> params) {
        Objects.requireNonNull(functionName, "functionName must not be null");
        Objects.requireNonNull(params, "params must not be null");
        if (functionName.isBlank()) {
            throw new IllegalArgumentException("functionName must not be blank");
        }
        return params.stream().map(Param::nativeType).collect(Collectors.joining(",", functionName + "(", ")"));
    }

    /// The selector of a function: the first 4 bytes of the Keccak-256 hash of its signature.
    ///
    /// @param signature the canonical signature
    /// @return the 4 bytes
    public static byte[] selector(final String signature) {
        Objects.requireNonNull(signature, "signature must not be null");
        return Arrays.copyOf(Keccak256.hash(signature.getBytes(StandardCharsets.UTF_8)), 4);
    }

    private static byte[] encodingOf(final Param<?, ?> param) {
        if (param.supplier() instanceof AbiParamSupplier supplier) {
            return supplier.getNativeContractValue();
        }
        throw new IllegalArgumentException("The parameter " + param + " was not created by ContractFactory");
    }

    /// The 32-byte two's complement big-endian form of an integer that fits into 256 bits.
    private static byte[] word(final BigInteger value) {
        final byte[] raw = value.toByteArray();
        final byte[] word = new byte[WORD];
        if (value.signum() < 0) {
            Arrays.fill(word, (byte) 0xFF);
        }
        final int length = Math.min(raw.length, WORD);
        System.arraycopy(raw, raw.length - length, word, WORD - length, length);
        return word;
    }
}
