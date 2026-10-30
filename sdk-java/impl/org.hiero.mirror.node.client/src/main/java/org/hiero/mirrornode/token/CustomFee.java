package org.hiero.mirrornode.token;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import org.hiero.mirrornode.common.FixedFee;
import org.jspecify.annotations.Nullable;

/// The custom fees of a token, grouped by kind.
///
/// @param createdTimestamp consensus time at which the fees were set
public record CustomFee(
        List<FixedFee> fixedFees,
        List<FractionalFee> fractionalFees,
        List<RoyaltyFee> royaltyFees,
        @Nullable ZonedDateTime createdTimestamp) {

    /// Creates a new `CustomFee`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public CustomFee {
        Objects.requireNonNull(fixedFees, "fixedFees must not be null");
        fixedFees = List.copyOf(fixedFees);
        Objects.requireNonNull(fractionalFees, "fractionalFees must not be null");
        fractionalFees = List.copyOf(fractionalFees);
        Objects.requireNonNull(royaltyFees, "royaltyFees must not be null");
        royaltyFees = List.copyOf(royaltyFees);
    }

    /// Creates a new `CustomFee` with the default value of `fixedFees`, `fractionalFees`, `royaltyFees`.
    public CustomFee(final @Nullable ZonedDateTime createdTimestamp) {
        this(List.of(), List.of(), List.of(), createdTimestamp);
    }

    /// Returns a builder for a new `CustomFee`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `CustomFee`.
    ///
    /// `build()` creates the `CustomFee` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private List<FixedFee> fixedFees = List.of();
        private List<FractionalFee> fractionalFees = List.of();
        private List<RoyaltyFee> royaltyFees = List.of();
        private @Nullable ZonedDateTime createdTimestamp;

        private Builder() {
            // created by CustomFee.builder()
        }

        /// Sets the `fixedFees`.
        ///
        /// @param fixedFees the value
        /// @return this builder
        public Builder fixedFees(final List<FixedFee> fixedFees) {
            this.fixedFees = fixedFees;
            return this;
        }

        /// Sets the `fractionalFees`.
        ///
        /// @param fractionalFees the value
        /// @return this builder
        public Builder fractionalFees(final List<FractionalFee> fractionalFees) {
            this.fractionalFees = fractionalFees;
            return this;
        }

        /// Sets the `royaltyFees`.
        ///
        /// @param royaltyFees the value
        /// @return this builder
        public Builder royaltyFees(final List<RoyaltyFee> royaltyFees) {
            this.royaltyFees = royaltyFees;
            return this;
        }

        /// Sets the `createdTimestamp`.
        ///
        /// consensus time at which the fees were set
        ///
        /// @param createdTimestamp the value
        /// @return this builder
        public Builder createdTimestamp(final @Nullable ZonedDateTime createdTimestamp) {
            this.createdTimestamp = createdTimestamp;
            return this;
        }

        /// Creates the `CustomFee` of the values set.
        ///
        /// @return the new `CustomFee`
        public CustomFee build() {
            return new CustomFee(fixedFees, fractionalFees, royaltyFees, createdTimestamp);
        }
    }
}
