package org.hiero.enterprise.service.topic;

import java.time.Duration;
import org.hiero.authority.Authority;
import org.hiero.ledger.AccountId;
import org.jspecify.annotations.Nullable;

/// The parameters of a new topic: everything that can be set when it is created. Nothing is required: without values
/// the topic is public and cannot be changed.
///
/// @param adminAuthority Allows to update and delete the topic; without it the topic cannot be changed. It must sign the creation.
/// @param submitAuthority Allows to submit messages; without it anybody can submit messages.
/// @param autoRenewPeriod The renewal period applied when the expiration approaches; the network default if absent.
/// @param autoRenewAccount The account that pays for the renewal; it must sign the creation.
public record TopicCreateParams(
        @Nullable Authority adminAuthority,
        @Nullable Authority submitAuthority,
        @Nullable String topicMemo,
        @Nullable Duration autoRenewPeriod,
        @Nullable AccountId autoRenewAccount) {

    /// Creates a new `TopicCreateParams`.
    ///
    /// @throws IllegalArgumentException if a value violates its constraints
    public TopicCreateParams {
        if (topicMemo != null && topicMemo.length() > 100) {
            throw new IllegalArgumentException("topicMemo must be at most 100 characters long");
        }
    }

    /// Returns a builder for a new `TopicCreateParams`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `TopicCreateParams`.
    ///
    /// `build()` creates the `TopicCreateParams` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Authority adminAuthority;
        private @Nullable Authority submitAuthority;
        private @Nullable String topicMemo;
        private @Nullable Duration autoRenewPeriod;
        private @Nullable AccountId autoRenewAccount;

        private Builder() {
            // created by TopicCreateParams.builder()
        }

        /// Sets the `adminAuthority`.
        ///
        /// Allows to update and delete the topic; without it the topic cannot be changed. It must sign the creation.
        ///
        /// @param adminAuthority the value
        /// @return this builder
        public Builder adminAuthority(final @Nullable Authority adminAuthority) {
            this.adminAuthority = adminAuthority;
            return this;
        }

        /// Sets the `submitAuthority`.
        ///
        /// Allows to submit messages; without it anybody can submit messages.
        ///
        /// @param submitAuthority the value
        /// @return this builder
        public Builder submitAuthority(final @Nullable Authority submitAuthority) {
            this.submitAuthority = submitAuthority;
            return this;
        }

        /// Sets the `topicMemo`.
        ///
        /// @param topicMemo the value
        /// @return this builder
        public Builder topicMemo(final @Nullable String topicMemo) {
            this.topicMemo = topicMemo;
            return this;
        }

        /// Sets the `autoRenewPeriod`.
        ///
        /// The renewal period applied when the expiration approaches; the network default if absent.
        ///
        /// @param autoRenewPeriod the value
        /// @return this builder
        public Builder autoRenewPeriod(final @Nullable Duration autoRenewPeriod) {
            this.autoRenewPeriod = autoRenewPeriod;
            return this;
        }

        /// Sets the `autoRenewAccount`.
        ///
        /// The account that pays for the renewal; it must sign the creation.
        ///
        /// @param autoRenewAccount the value
        /// @return this builder
        public Builder autoRenewAccount(final @Nullable AccountId autoRenewAccount) {
            this.autoRenewAccount = autoRenewAccount;
            return this;
        }

        /// Creates the `TopicCreateParams` of the values set.
        ///
        /// @return the new `TopicCreateParams`
        /// @throws IllegalArgumentException if a value violates its constraints
        public TopicCreateParams build() {
            return new TopicCreateParams(
                    adminAuthority,
                    submitAuthority,
                    topicMemo,
                    autoRenewPeriod,
                    autoRenewAccount);
        }
    }
}
