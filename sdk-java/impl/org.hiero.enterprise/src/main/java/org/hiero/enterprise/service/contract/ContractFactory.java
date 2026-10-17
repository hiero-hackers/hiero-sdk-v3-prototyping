package org.hiero.enterprise.service.contract;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.hiero.enterprise.service.Session;
import org.hiero.enterprise.service.internal.Abi;
import org.hiero.enterprise.service.internal.DefaultSession;
import org.hiero.enterprise.service.internal.DefaultSmartContractService;
import org.hiero.ledger.EvmAddress;

/// Factory methods of the package `org.hiero.enterprise.service.contract`.
public final class ContractFactory {

    /// Wraps a text as Solidity `string` (encoded in UTF-8).
    ///
    /// @param value the value
    /// @return the parameter
    /// @throws NullPointerException if `value` is `null`
    public static Param<String, ?> ofString(final String value) {
        Objects.requireNonNull(value, "value must not be null");
        return Abi.param(value, "string", Abi.encodeString(value));
    }

    /// Wraps the UTF-8 bytes of a text as Solidity `bytes`.
    ///
    /// @param value the value
    /// @return the parameter
    /// @throws NullPointerException if `value` is `null`
    public static Param<String, ?> ofBytes(final String value) {
        Objects.requireNonNull(value, "value must not be null");
        return Abi.param(value, "bytes", Abi.encodeDynamic(value.getBytes(StandardCharsets.UTF_8)));
    }

    /// Wraps the UTF-8 bytes of a text (at most 23) as Solidity `bytes23`, right-padded with zeros.
    ///
    /// @param value the value
    /// @return the parameter
    /// @throws NullPointerException if `value` is `null`
    /// @throws IllegalArgumentException if the value does not fit into the Solidity type
    public static Param<String, ?> ofBytes23(final String value) {
        Objects.requireNonNull(value, "value must not be null");
        return Abi.param(value, "bytes23", Abi.encodeFixedBytes(value.getBytes(StandardCharsets.UTF_8), 23));
    }

    /// Wraps bytes as Solidity `bytes`.
    ///
    /// @param value the value
    /// @return the parameter
    /// @throws NullPointerException if `value` is `null`
    public static Param<byte[], ?> ofBytes(final byte[] value) {
        Objects.requireNonNull(value, "value must not be null");
        return Abi.param(value, "bytes", Abi.encodeDynamic(value));
    }

    /// Wraps at most 23 bytes as Solidity `bytes23`, right-padded with zeros.
    ///
    /// @param value the value
    /// @return the parameter
    /// @throws NullPointerException if `value` is `null`
    /// @throws IllegalArgumentException if the value does not fit into the Solidity type
    public static Param<byte[], ?> ofBytes23(final byte[] value) {
        Objects.requireNonNull(value, "value must not be null");
        return Abi.param(value, "bytes23", Abi.encodeFixedBytes(value, 23));
    }

    /// Wraps the hex form (`0x...`) of a 20-byte EVM address as Solidity `address`.
    ///
    /// @param value the value
    /// @return the parameter
    /// @throws NullPointerException if `value` is `null`
    /// @throws IllegalArgumentException if the value does not fit into the Solidity type
    public static Param<String, ?> ofAddress(final String value) {
        Objects.requireNonNull(value, "value must not be null");
        final EvmAddress address;
        try {
            address = EvmAddress.fromString(value);
        } catch (final RuntimeException e) {
            throw new IllegalArgumentException("value is not the hex form of an EVM address: " + value, e);
        }
        return Abi.param(value, "address", Abi.encodeAddress(address.bytes()));
    }

    /// Wraps an EVM address as Solidity `address`.
    ///
    /// @param value the value
    /// @return the parameter
    /// @throws NullPointerException if `value` is `null`
    public static Param<EvmAddress, ?> ofAddress(final EvmAddress value) {
        Objects.requireNonNull(value, "value must not be null");
        return Abi.param(value, "address", Abi.encodeAddress(value.bytes()));
    }

    /// Wraps a boolean as Solidity `bool`.
    ///
    /// @param value the value
    /// @return the parameter
    public static Param<Boolean, ?> ofBool(final boolean value) {
        return Abi.param(value, "bool", Abi.encodeBool(value));
    }

    /// Wraps a number from 0 to 255 as Solidity `uint8`.
    ///
    /// @param value the value
    /// @return the parameter
    /// @throws IllegalArgumentException if the value does not fit into the Solidity type
    public static Param<Short, ?> uint8(final short value) {
        return Abi.param(value, "uint8", Abi.encodeUnsigned(BigInteger.valueOf(value), 8));
    }

    /// Wraps a number from -128 to 127 as Solidity `int8`.
    ///
    /// @param value the value
    /// @return the parameter
    public static Param<Byte, ?> int8(final byte value) {
        return Abi.param(value, "int8", Abi.encodeSigned(BigInteger.valueOf(value), 8));
    }

    /// Wraps a number from 0 to 2^256-1 as Solidity `uint256`.
    ///
    /// @param value the value
    /// @return the parameter
    /// @throws NullPointerException if `value` is `null`
    /// @throws IllegalArgumentException if the value does not fit into the Solidity type
    public static Param<BigInteger, ?> uint256(final BigInteger value) {
        return Abi.param(value, "uint256", Abi.encodeUnsigned(value, 256));
    }

    /// Wraps a number from -2^255 to 2^255-1 as Solidity `int256`.
    ///
    /// @param value the value
    /// @return the parameter
    /// @throws NullPointerException if `value` is `null`
    /// @throws IllegalArgumentException if the value does not fit into the Solidity type
    public static Param<BigInteger, ?> int256(final BigInteger value) {
        return Abi.param(value, "int256", Abi.encodeSigned(value, 256));
    }

    /// Creates the service for the given session. With a framework integration the service is usually obtained via
    /// dependency injection instead.
    public static SmartContractService createService(final Session session) {
        return new DefaultSmartContractService(DefaultSession.of(session));
    }

    private ContractFactory() {
        throw new UnsupportedOperationException("Factory class cannot be instantiated");
    }
}
