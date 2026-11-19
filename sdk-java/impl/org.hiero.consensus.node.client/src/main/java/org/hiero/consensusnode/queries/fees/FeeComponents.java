package org.hiero.consensusnode.queries.fees;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/// The prices of the resources a request uses, in thousandths of a tinycent (1 tinycent = 10^-8 US cents). The fee is
/// the sum of each resource's use times its price, plus `constantFee`, bounded by `minimumFee` and `maximumFee`; it
/// is converted to the native token with the exchange rate of the network.
///
/// @param minimumFee the lowest total fee
/// @param maximumFee the highest total fee
/// @param constantFee the constant part of the fee
/// @param transactionBandwidthByte price per byte of the request
/// @param transactionVerification price per signature verification
/// @param transactionRamByteHour price per byte-hour of memory
/// @param transactionStorageByteHour price per byte-hour of storage
/// @param contractTransactionGas price per unit of gas of a contract execution
/// @param transferVolume price per unit of native token transferred
/// @param responseMemoryByte price per byte of the response read from memory
/// @param responseDiskByte price per byte of the response read from storage
public record FeeComponents(
        long minimumFee,
        long maximumFee,
        long constantFee,
        long transactionBandwidthByte,
        long transactionVerification,
        long transactionRamByteHour,
        long transactionStorageByteHour,
        long contractTransactionGas,
        long transferVolume,
        long responseMemoryByte,
        long responseDiskByte) {

    /// Creates a new `FeeComponents`.
    ///
    /// @throws IllegalArgumentException if a value violates its constraints
    public FeeComponents {
        if (minimumFee < 0L) {
            throw new IllegalArgumentException("minimumFee must be at least 0");
        }
        if (maximumFee < 0L) {
            throw new IllegalArgumentException("maximumFee must be at least 0");
        }
        if (constantFee < 0L) {
            throw new IllegalArgumentException("constantFee must be at least 0");
        }
        if (transactionBandwidthByte < 0L) {
            throw new IllegalArgumentException("transactionBandwidthByte must be at least 0");
        }
        if (transactionVerification < 0L) {
            throw new IllegalArgumentException("transactionVerification must be at least 0");
        }
        if (transactionRamByteHour < 0L) {
            throw new IllegalArgumentException("transactionRamByteHour must be at least 0");
        }
        if (transactionStorageByteHour < 0L) {
            throw new IllegalArgumentException("transactionStorageByteHour must be at least 0");
        }
        if (contractTransactionGas < 0L) {
            throw new IllegalArgumentException("contractTransactionGas must be at least 0");
        }
        if (transferVolume < 0L) {
            throw new IllegalArgumentException("transferVolume must be at least 0");
        }
        if (responseMemoryByte < 0L) {
            throw new IllegalArgumentException("responseMemoryByte must be at least 0");
        }
        if (responseDiskByte < 0L) {
            throw new IllegalArgumentException("responseDiskByte must be at least 0");
        }
    }

    /// Returns a builder for a new `FeeComponents`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `FeeComponents`.
    ///
    /// `build()` creates the `FeeComponents` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Long minimumFee;
        private @Nullable Long maximumFee;
        private @Nullable Long constantFee;
        private @Nullable Long transactionBandwidthByte;
        private @Nullable Long transactionVerification;
        private @Nullable Long transactionRamByteHour;
        private @Nullable Long transactionStorageByteHour;
        private @Nullable Long contractTransactionGas;
        private @Nullable Long transferVolume;
        private @Nullable Long responseMemoryByte;
        private @Nullable Long responseDiskByte;

        private Builder() {
            // created by FeeComponents.builder()
        }

        /// Sets the `minimumFee`.
        ///
        /// the lowest total fee
        ///
        /// @param minimumFee the value
        /// @return this builder
        public Builder minimumFee(final long minimumFee) {
            this.minimumFee = minimumFee;
            return this;
        }

        /// Sets the `maximumFee`.
        ///
        /// the highest total fee
        ///
        /// @param maximumFee the value
        /// @return this builder
        public Builder maximumFee(final long maximumFee) {
            this.maximumFee = maximumFee;
            return this;
        }

        /// Sets the `constantFee`.
        ///
        /// the constant part of the fee
        ///
        /// @param constantFee the value
        /// @return this builder
        public Builder constantFee(final long constantFee) {
            this.constantFee = constantFee;
            return this;
        }

        /// Sets the `transactionBandwidthByte`.
        ///
        /// price per byte of the request
        ///
        /// @param transactionBandwidthByte the value
        /// @return this builder
        public Builder transactionBandwidthByte(final long transactionBandwidthByte) {
            this.transactionBandwidthByte = transactionBandwidthByte;
            return this;
        }

        /// Sets the `transactionVerification`.
        ///
        /// price per signature verification
        ///
        /// @param transactionVerification the value
        /// @return this builder
        public Builder transactionVerification(final long transactionVerification) {
            this.transactionVerification = transactionVerification;
            return this;
        }

        /// Sets the `transactionRamByteHour`.
        ///
        /// price per byte-hour of memory
        ///
        /// @param transactionRamByteHour the value
        /// @return this builder
        public Builder transactionRamByteHour(final long transactionRamByteHour) {
            this.transactionRamByteHour = transactionRamByteHour;
            return this;
        }

        /// Sets the `transactionStorageByteHour`.
        ///
        /// price per byte-hour of storage
        ///
        /// @param transactionStorageByteHour the value
        /// @return this builder
        public Builder transactionStorageByteHour(final long transactionStorageByteHour) {
            this.transactionStorageByteHour = transactionStorageByteHour;
            return this;
        }

        /// Sets the `contractTransactionGas`.
        ///
        /// price per unit of gas of a contract execution
        ///
        /// @param contractTransactionGas the value
        /// @return this builder
        public Builder contractTransactionGas(final long contractTransactionGas) {
            this.contractTransactionGas = contractTransactionGas;
            return this;
        }

        /// Sets the `transferVolume`.
        ///
        /// price per unit of native token transferred
        ///
        /// @param transferVolume the value
        /// @return this builder
        public Builder transferVolume(final long transferVolume) {
            this.transferVolume = transferVolume;
            return this;
        }

        /// Sets the `responseMemoryByte`.
        ///
        /// price per byte of the response read from memory
        ///
        /// @param responseMemoryByte the value
        /// @return this builder
        public Builder responseMemoryByte(final long responseMemoryByte) {
            this.responseMemoryByte = responseMemoryByte;
            return this;
        }

        /// Sets the `responseDiskByte`.
        ///
        /// price per byte of the response read from storage
        ///
        /// @param responseDiskByte the value
        /// @return this builder
        public Builder responseDiskByte(final long responseDiskByte) {
            this.responseDiskByte = responseDiskByte;
            return this;
        }

        /// Creates the `FeeComponents` of the values set.
        ///
        /// @return the new `FeeComponents`
        /// @throws NullPointerException if a required value is not set
        /// @throws IllegalArgumentException if a value violates its constraints
        public FeeComponents build() {
            return new FeeComponents(
                    Objects.requireNonNull(minimumFee, "minimumFee must not be null"),
                    Objects.requireNonNull(maximumFee, "maximumFee must not be null"),
                    Objects.requireNonNull(constantFee, "constantFee must not be null"),
                    Objects.requireNonNull(transactionBandwidthByte, "transactionBandwidthByte must not be null"),
                    Objects.requireNonNull(transactionVerification, "transactionVerification must not be null"),
                    Objects.requireNonNull(transactionRamByteHour, "transactionRamByteHour must not be null"),
                    Objects.requireNonNull(transactionStorageByteHour, "transactionStorageByteHour must not be null"),
                    Objects.requireNonNull(contractTransactionGas, "contractTransactionGas must not be null"),
                    Objects.requireNonNull(transferVolume, "transferVolume must not be null"),
                    Objects.requireNonNull(responseMemoryByte, "responseMemoryByte must not be null"),
                    Objects.requireNonNull(responseDiskByte, "responseDiskByte must not be null"));
        }
    }
}
