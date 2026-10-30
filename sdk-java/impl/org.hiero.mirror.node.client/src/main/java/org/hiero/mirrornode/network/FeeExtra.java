package org.hiero.mirrornode.network;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/// An extra fee of a fee component, e.g. for additional signatures.
///
/// @param charged number of charged items: max(0, count - included)
/// @param count number of items in the transaction
/// @param feePerUnit price of one item in tinycents
/// @param included number of items included for free
/// @param name name of the extra fee in the fee schedule
/// @param subtotal charged × feePerUnit, in tinycents
public record FeeExtra(
        long charged,
        long count,
        long feePerUnit,
        long included,
        String name,
        long subtotal) {

    /// Creates a new `FeeExtra`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public FeeExtra {
        Objects.requireNonNull(name, "name must not be null");
    }

    /// Returns a builder for a new `FeeExtra`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `FeeExtra`.
    ///
    /// `build()` creates the `FeeExtra` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Long charged;
        private @Nullable Long count;
        private @Nullable Long feePerUnit;
        private @Nullable Long included;
        private @Nullable String name;
        private @Nullable Long subtotal;

        private Builder() {
            // created by FeeExtra.builder()
        }

        /// Sets the `charged`.
        ///
        /// number of charged items: max(0, count - included)
        ///
        /// @param charged the value
        /// @return this builder
        public Builder charged(final long charged) {
            this.charged = charged;
            return this;
        }

        /// Sets the `count`.
        ///
        /// number of items in the transaction
        ///
        /// @param count the value
        /// @return this builder
        public Builder count(final long count) {
            this.count = count;
            return this;
        }

        /// Sets the `feePerUnit`.
        ///
        /// price of one item in tinycents
        ///
        /// @param feePerUnit the value
        /// @return this builder
        public Builder feePerUnit(final long feePerUnit) {
            this.feePerUnit = feePerUnit;
            return this;
        }

        /// Sets the `included`.
        ///
        /// number of items included for free
        ///
        /// @param included the value
        /// @return this builder
        public Builder included(final long included) {
            this.included = included;
            return this;
        }

        /// Sets the `name`.
        ///
        /// name of the extra fee in the fee schedule
        ///
        /// @param name the value
        /// @return this builder
        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        /// Sets the `subtotal`.
        ///
        /// charged × feePerUnit, in tinycents
        ///
        /// @param subtotal the value
        /// @return this builder
        public Builder subtotal(final long subtotal) {
            this.subtotal = subtotal;
            return this;
        }

        /// Creates the `FeeExtra` of the values set.
        ///
        /// @return the new `FeeExtra`
        /// @throws NullPointerException if a required value is not set
        public FeeExtra build() {
            return new FeeExtra(
                    Objects.requireNonNull(charged, "charged must not be null"),
                    Objects.requireNonNull(count, "count must not be null"),
                    Objects.requireNonNull(feePerUnit, "feePerUnit must not be null"),
                    Objects.requireNonNull(included, "included must not be null"),
                    Objects.requireNonNull(name, "name must not be null"),
                    Objects.requireNonNull(subtotal, "subtotal must not be null"));
        }
    }
}
