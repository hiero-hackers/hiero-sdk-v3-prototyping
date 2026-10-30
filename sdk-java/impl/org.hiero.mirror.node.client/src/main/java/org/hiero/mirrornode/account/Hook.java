package org.hiero.mirrornode.account;

import java.time.ZonedDateTime;
import java.util.Objects;
import org.hiero.authority.Authority;
import org.hiero.ledger.Address;
import org.hiero.mirrornode.common.TimestampRange;
import org.jspecify.annotations.Nullable;

/// A hook of an account. GET /accounts/{id}/hooks.
///
/// @param adminKey the authority that may change the hook
/// @param contractId the contract that implements the hook
/// @param ownerId the entity that owns the hook
/// @param timestampRange the period this version of the hook is valid for
public record Hook(
        @Nullable Authority adminKey,
        Address contractId,
        @Nullable ZonedDateTime createdTimestamp,
        boolean deleted,
        HookExtensionPoint extensionPoint,
        long hookId,
        Address ownerId,
        @Nullable TimestampRange timestampRange,
        HookType type) {

    /// Creates a new `Hook`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public Hook {
        Objects.requireNonNull(contractId, "contractId must not be null");
        Objects.requireNonNull(extensionPoint, "extensionPoint must not be null");
        Objects.requireNonNull(ownerId, "ownerId must not be null");
        Objects.requireNonNull(type, "type must not be null");
    }

    /// Returns a builder for a new `Hook`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `Hook`.
    ///
    /// `build()` creates the `Hook` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Authority adminKey;
        private @Nullable Address contractId;
        private @Nullable ZonedDateTime createdTimestamp;
        private @Nullable Boolean deleted;
        private @Nullable HookExtensionPoint extensionPoint;
        private @Nullable Long hookId;
        private @Nullable Address ownerId;
        private @Nullable TimestampRange timestampRange;
        private @Nullable HookType type;

        private Builder() {
            // created by Hook.builder()
        }

        /// Sets the `adminKey`.
        ///
        /// the authority that may change the hook
        ///
        /// @param adminKey the value
        /// @return this builder
        public Builder adminKey(final @Nullable Authority adminKey) {
            this.adminKey = adminKey;
            return this;
        }

        /// Sets the `contractId`.
        ///
        /// the contract that implements the hook
        ///
        /// @param contractId the value
        /// @return this builder
        public Builder contractId(final Address contractId) {
            this.contractId = contractId;
            return this;
        }

        /// Sets the `createdTimestamp`.
        ///
        /// @param createdTimestamp the value
        /// @return this builder
        public Builder createdTimestamp(final @Nullable ZonedDateTime createdTimestamp) {
            this.createdTimestamp = createdTimestamp;
            return this;
        }

        /// Sets the `deleted`.
        ///
        /// @param deleted the value
        /// @return this builder
        public Builder deleted(final boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        /// Sets the `extensionPoint`.
        ///
        /// @param extensionPoint the value
        /// @return this builder
        public Builder extensionPoint(final HookExtensionPoint extensionPoint) {
            this.extensionPoint = extensionPoint;
            return this;
        }

        /// Sets the `hookId`.
        ///
        /// @param hookId the value
        /// @return this builder
        public Builder hookId(final long hookId) {
            this.hookId = hookId;
            return this;
        }

        /// Sets the `ownerId`.
        ///
        /// the entity that owns the hook
        ///
        /// @param ownerId the value
        /// @return this builder
        public Builder ownerId(final Address ownerId) {
            this.ownerId = ownerId;
            return this;
        }

        /// Sets the `timestampRange`.
        ///
        /// the period this version of the hook is valid for
        ///
        /// @param timestampRange the value
        /// @return this builder
        public Builder timestampRange(final @Nullable TimestampRange timestampRange) {
            this.timestampRange = timestampRange;
            return this;
        }

        /// Sets the `type`.
        ///
        /// @param type the value
        /// @return this builder
        public Builder type(final HookType type) {
            this.type = type;
            return this;
        }

        /// Creates the `Hook` of the values set.
        ///
        /// @return the new `Hook`
        /// @throws NullPointerException if a required value is not set
        public Hook build() {
            return new Hook(
                    adminKey,
                    Objects.requireNonNull(contractId, "contractId must not be null"),
                    createdTimestamp,
                    Objects.requireNonNull(deleted, "deleted must not be null"),
                    Objects.requireNonNull(extensionPoint, "extensionPoint must not be null"),
                    Objects.requireNonNull(hookId, "hookId must not be null"),
                    Objects.requireNonNull(ownerId, "ownerId must not be null"),
                    timestampRange,
                    Objects.requireNonNull(type, "type must not be null"));
        }
    }
}
