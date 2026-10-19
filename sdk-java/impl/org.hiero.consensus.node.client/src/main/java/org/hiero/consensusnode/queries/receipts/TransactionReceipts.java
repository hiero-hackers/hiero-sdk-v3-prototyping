package org.hiero.consensusnode.queries.receipts;

import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/// The answer of a `TransactionReceiptQuery`.
///
/// @param receipt the receipt of the transaction
/// @param duplicates duplicate transactions, in consensus order
/// @param children child transactions, in consensus order
public record TransactionReceipts(
        UntypedReceipt receipt,
        List<UntypedReceipt> duplicates,
        List<UntypedReceipt> children) {

    /// Creates a new `TransactionReceipts`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public TransactionReceipts {
        Objects.requireNonNull(receipt, "receipt must not be null");
        Objects.requireNonNull(duplicates, "duplicates must not be null");
        duplicates = List.copyOf(duplicates);
        Objects.requireNonNull(children, "children must not be null");
        children = List.copyOf(children);
    }

    /// Creates a new `TransactionReceipts` with the default value of `duplicates`, `children`.
    public TransactionReceipts(final UntypedReceipt receipt) {
        this(receipt, List.of(), List.of());
    }

    /// Returns a builder for a new `TransactionReceipts`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `TransactionReceipts`.
    ///
    /// `build()` creates the `TransactionReceipts` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable UntypedReceipt receipt;
        private List<UntypedReceipt> duplicates = List.of();
        private List<UntypedReceipt> children = List.of();

        private Builder() {
            // created by TransactionReceipts.builder()
        }

        /// Sets the `receipt`.
        ///
        /// the receipt of the transaction
        ///
        /// @param receipt the value
        /// @return this builder
        public Builder receipt(final UntypedReceipt receipt) {
            this.receipt = receipt;
            return this;
        }

        /// Sets the `duplicates`.
        ///
        /// duplicate transactions, in consensus order
        ///
        /// @param duplicates the value
        /// @return this builder
        public Builder duplicates(final List<UntypedReceipt> duplicates) {
            this.duplicates = duplicates;
            return this;
        }

        /// Sets the `children`.
        ///
        /// child transactions, in consensus order
        ///
        /// @param children the value
        /// @return this builder
        public Builder children(final List<UntypedReceipt> children) {
            this.children = children;
            return this;
        }

        /// Creates the `TransactionReceipts` of the values set.
        ///
        /// @return the new `TransactionReceipts`
        /// @throws NullPointerException if a required value is not set
        public TransactionReceipts build() {
            return new TransactionReceipts(
                    Objects.requireNonNull(receipt, "receipt must not be null"),
                    duplicates,
                    children);
        }
    }
}
