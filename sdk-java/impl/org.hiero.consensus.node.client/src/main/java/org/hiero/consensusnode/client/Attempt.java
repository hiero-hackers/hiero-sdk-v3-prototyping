package org.hiero.consensusnode.client;

import java.time.Duration;
import java.util.Objects;
import org.hiero.ledger.ConsensusNode;
import org.jspecify.annotations.Nullable;

/// One attempt to send a request to a consensus node.
///
/// @param number the number of the attempt within its request, 1 for the first one
/// @param node the node the attempt was sent to
/// @param result what the attempt got from the node
/// @param outcome how the SDK classifies the result
/// @param duration the time the attempt took
public record Attempt(
        int number,
        ConsensusNode node,
        AttemptResult result,
        AttemptOutcome outcome,
        Duration duration) {

    /// Creates a new `Attempt`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public Attempt {
        if (number < 1) {
            throw new IllegalArgumentException("number must be at least 1");
        }
        Objects.requireNonNull(node, "node must not be null");
        Objects.requireNonNull(result, "result must not be null");
        Objects.requireNonNull(outcome, "outcome must not be null");
        Objects.requireNonNull(duration, "duration must not be null");
    }

    /// Returns a builder for a new `Attempt`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `Attempt`.
    ///
    /// `build()` creates the `Attempt` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Integer number;
        private @Nullable ConsensusNode node;
        private @Nullable AttemptResult result;
        private @Nullable AttemptOutcome outcome;
        private @Nullable Duration duration;

        private Builder() {
            // created by Attempt.builder()
        }

        /// Sets the `number`.
        ///
        /// the number of the attempt within its request, 1 for the first one
        ///
        /// @param number the value
        /// @return this builder
        public Builder number(final int number) {
            this.number = number;
            return this;
        }

        /// Sets the `node`.
        ///
        /// the node the attempt was sent to
        ///
        /// @param node the value
        /// @return this builder
        public Builder node(final ConsensusNode node) {
            this.node = node;
            return this;
        }

        /// Sets the `result`.
        ///
        /// what the attempt got from the node
        ///
        /// @param result the value
        /// @return this builder
        public Builder result(final AttemptResult result) {
            this.result = result;
            return this;
        }

        /// Sets the `outcome`.
        ///
        /// how the SDK classifies the result
        ///
        /// @param outcome the value
        /// @return this builder
        public Builder outcome(final AttemptOutcome outcome) {
            this.outcome = outcome;
            return this;
        }

        /// Sets the `duration`.
        ///
        /// the time the attempt took
        ///
        /// @param duration the value
        /// @return this builder
        public Builder duration(final Duration duration) {
            this.duration = duration;
            return this;
        }

        /// Creates the `Attempt` of the values set.
        ///
        /// @return the new `Attempt`
        /// @throws NullPointerException if a required value is not set
        /// @throws IllegalArgumentException if a value violates its constraints
        public Attempt build() {
            return new Attempt(
                    Objects.requireNonNull(number, "number must not be null"),
                    Objects.requireNonNull(node, "node must not be null"),
                    Objects.requireNonNull(result, "result must not be null"),
                    Objects.requireNonNull(outcome, "outcome must not be null"),
                    Objects.requireNonNull(duration, "duration must not be null"));
        }
    }
}
