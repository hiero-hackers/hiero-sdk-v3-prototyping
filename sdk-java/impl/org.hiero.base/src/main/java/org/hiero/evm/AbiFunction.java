package org.hiero.evm;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import org.hiero.evm.internal.AbiCodec;
import org.hiero.evm.internal.AbiSignatures;
import org.hiero.keys.internal.Secp256k1;
import org.jspecify.annotations.Nullable;

/// A function of a contract: its name and the types of its arguments and results.
///
/// @param inputs the types of the arguments
/// @param outputs the types of the results
public record AbiFunction(String name, List<AbiType> inputs, List<AbiType> outputs) {

    private static final Pattern NAME_PATTERN = Pattern.compile("[A-Za-z_$][A-Za-z0-9_$]*");

    /// Creates a new `AbiFunction`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public AbiFunction {
        Objects.requireNonNull(name, "name must not be null");
        if (!NAME_PATTERN.matcher(name).find()) {
            throw new IllegalArgumentException("name must match the pattern [A-Za-z_$][A-Za-z0-9_$]*");
        }
        Objects.requireNonNull(inputs, "inputs must not be null");
        inputs = List.copyOf(inputs);
        Objects.requireNonNull(outputs, "outputs must not be null");
        outputs = List.copyOf(outputs);
    }

    /// Creates a new `AbiFunction` with the default value of `inputs`, `outputs`.
    public AbiFunction(final String name) {
        this(name, List.of(), List.of());
    }

    /// The signature: the name and the canonical names of the argument types, e.g. "transfer(address,uint256)".
    public String signature() {
        return name + AbiCodec.names(inputs);
    }

    /// The selector: the first 4 bytes of the Keccak-256 hash of the signature.
    public byte[] selector() {
        return Arrays.copyOf(Secp256k1.keccak256(signature().getBytes(StandardCharsets.UTF_8)), 4);
    }

    /// The call data of a call: the selector, followed by the encoded arguments. Throws if the arguments do not have the
    /// types of `inputs` or a value does not fit its type.
    ///
    /// @throws IllegalArgumentException if an invalid argument error occurs
    public byte[] encodeCall(final List<AbiValue> arguments) {
        final byte[] selector = selector();
        final byte[] encoded = AbiCodec.encode(inputs, arguments);
        final byte[] data = Arrays.copyOf(selector, selector.length + encoded.length);
        System.arraycopy(encoded, 0, data, selector.length, encoded.length);
        return data;
    }

    /// Decodes the arguments of call data. Throws if the data does not start with the selector or is no valid encoding
    /// of the arguments.
    ///
    /// @throws IllegalArgumentException if an illegal format error occurs
    public List<AbiValue> decodeCall(final byte[] data) {
        final byte[] selector = selector();
        if (data.length < selector.length || !Arrays.equals(selector, Arrays.copyOf(data, selector.length))) {
            throw new IllegalArgumentException("The call data does not start with the selector of " + signature());
        }
        return AbiCodec.decode(inputs, Arrays.copyOfRange(data, selector.length, data.length));
    }

    /// Decodes the result of a call, as values of the types of `outputs`. Throws if the data is no valid encoding of
    /// them.
    ///
    /// @throws IllegalArgumentException if an illegal format error occurs
    public List<AbiValue> decodeResult(final byte[] data) {
        return AbiCodec.decode(outputs, data);
    }

    /// Reads a function from its signature, optionally followed by " returns " and the output types in parentheses:
    /// "balanceOf(address) returns (uint256)". Throws for text that is no such signature.
    ///
    /// @throws IllegalArgumentException if an illegal format error occurs
    public static AbiFunction parse(final String signature) {
        return AbiSignatures.parseFunction(signature);
    }

    /// Returns a builder for a new `AbiFunction`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `AbiFunction`.
    ///
    /// `build()` creates the `AbiFunction` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable String name;
        private List<AbiType> inputs = List.of();
        private List<AbiType> outputs = List.of();

        private Builder() {
            // created by AbiFunction.builder()
        }

        /// Sets the `name`.
        ///
        /// @param name the value
        /// @return this builder
        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        /// Sets the `inputs`.
        ///
        /// the types of the arguments
        ///
        /// @param inputs the value
        /// @return this builder
        public Builder inputs(final List<AbiType> inputs) {
            this.inputs = inputs;
            return this;
        }

        /// Sets the `outputs`.
        ///
        /// the types of the results
        ///
        /// @param outputs the value
        /// @return this builder
        public Builder outputs(final List<AbiType> outputs) {
            this.outputs = outputs;
            return this;
        }

        /// Creates the `AbiFunction` of the values set.
        ///
        /// @return the new `AbiFunction`
        /// @throws NullPointerException if a required value is not set
        /// @throws IllegalArgumentException if a value violates its constraints
        public AbiFunction build() {
            return new AbiFunction(Objects.requireNonNull(name, "name must not be null"), inputs, outputs);
        }
    }
}
