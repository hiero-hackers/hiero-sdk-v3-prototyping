package org.hiero.evm;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import org.hiero.evm.internal.AbiCodec;
import org.hiero.evm.internal.AbiSignatures;
import org.hiero.keys.internal.Secp256k1;
import org.jspecify.annotations.Nullable;

/// An event of a contract: its name and parameters.
///
/// @param anonymous true: the logs of the event have no topic with its signature
public record AbiEvent(String name, List<AbiEventParameter> parameters, boolean anonymous) {

    private static final Pattern NAME_PATTERN = Pattern.compile("[A-Za-z_$][A-Za-z0-9_$]*");

    /// Creates a new `AbiEvent`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public AbiEvent {
        Objects.requireNonNull(name, "name must not be null");
        if (!NAME_PATTERN.matcher(name).find()) {
            throw new IllegalArgumentException("name must match the pattern [A-Za-z_$][A-Za-z0-9_$]*");
        }
        Objects.requireNonNull(parameters, "parameters must not be null");
        parameters = List.copyOf(parameters);
    }

    /// Creates a new `AbiEvent` with the default value of `parameters`, `anonymous`.
    public AbiEvent(final String name) {
        this(name, List.of(), false);
    }

    /// The signature: the name and the canonical names of the parameter types, without "indexed", e.g.
    /// "Transfer(address,address,uint256)".
    public String signature() {
        return name + AbiCodec.names(parameters.stream().map(AbiEventParameter::abiType).toList());
    }

    /// The Keccak-256 hash of the signature: the first topic of every log of the event, unless it is anonymous.
    public byte[] topic() {
        return Secp256k1.keccak256(signature().getBytes(StandardCharsets.UTF_8));
    }

    /// Decodes a log of the event: the values of all parameters, in their order. The topics are the hash of the
    /// signature (unless the event is anonymous) and the indexed values; the data holds the values that are not indexed.
    /// An indexed value of a dynamic type (bytes, string, array, tuple) is stored only as the Keccak-256 hash of its
    /// value and is returned as that hash, an AbiFixedBytes of 32 bytes. Throws if the first topic is not the hash of
    /// the signature, the number of topics does not match, or the data is no valid encoding.
    ///
    /// @throws IllegalArgumentException if an illegal format error occurs
    public List<AbiValue> decodeLog(final List<byte[]> topics, final byte[] data) {
        final int indexed = (int) parameters.stream().filter(AbiEventParameter::indexed).count();
        final int first = anonymous ? 0 : 1;
        if (topics.size() != first + indexed) {
            throw new IllegalArgumentException("A log of " + signature() + " has " + (first + indexed) + " topic(s), not "
                    + topics.size());
        }
        if (!anonymous && !Arrays.equals(topic(), topics.getFirst())) {
            throw new IllegalArgumentException("The log is no log of " + signature());
        }
        final Iterator<AbiValue> unindexed = AbiCodec.decode(parameters.stream().filter(p -> !p.indexed())
                .map(AbiEventParameter::abiType).toList(), data).iterator();
        final List<AbiValue> values = new ArrayList<>(parameters.size());
        int next = first;
        for (final AbiEventParameter parameter : parameters) {
            if (!parameter.indexed()) {
                values.add(unindexed.next());
                continue;
            }
            final byte[] topic = topics.get(next++);
            if (topic.length != 32) {
                throw new IllegalArgumentException("A topic has 32 bytes, not " + topic.length);
            }
            values.add(AbiCodec.indexedAsHash(parameter.abiType()) ? new AbiFixedBytes(topic)
                    : AbiCodec.decode(List.of(parameter.abiType()), topic).getFirst());
        }
        return values;
    }

    /// Reads an event from its signature; an indexed parameter has " indexed" after its type:
    /// "Transfer(address indexed,address indexed,uint256)". Throws for text that is no such signature.
    ///
    /// @throws IllegalArgumentException if an illegal format error occurs
    public static AbiEvent parse(final String signature) {
        return AbiSignatures.parseEvent(signature);
    }

    /// Returns a builder for a new `AbiEvent`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `AbiEvent`.
    ///
    /// `build()` creates the `AbiEvent` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable String name;
        private List<AbiEventParameter> parameters = List.of();
        private boolean anonymous = false;

        private Builder() {
            // created by AbiEvent.builder()
        }

        /// Sets the `name`.
        ///
        /// @param name the value
        /// @return this builder
        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        /// Sets the `parameters`.
        ///
        /// @param parameters the value
        /// @return this builder
        public Builder parameters(final List<AbiEventParameter> parameters) {
            this.parameters = parameters;
            return this;
        }

        /// Sets the `anonymous`.
        ///
        /// true: the logs of the event have no topic with its signature
        ///
        /// @param anonymous the value
        /// @return this builder
        public Builder anonymous(final boolean anonymous) {
            this.anonymous = anonymous;
            return this;
        }

        /// Creates the `AbiEvent` of the values set.
        ///
        /// @return the new `AbiEvent`
        /// @throws NullPointerException if a required value is not set
        /// @throws IllegalArgumentException if a value violates its constraints
        public AbiEvent build() {
            return new AbiEvent(Objects.requireNonNull(name, "name must not be null"), parameters, anonymous);
        }
    }
}
