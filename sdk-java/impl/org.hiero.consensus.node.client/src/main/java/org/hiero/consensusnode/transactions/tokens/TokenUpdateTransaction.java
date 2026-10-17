package org.hiero.consensusnode.transactions.tokens;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import org.hiero.authority.Authority;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.client.TransactionSigner;
import org.hiero.consensusnode.transactions.PackedTransaction;
import org.hiero.consensusnode.transactions.Response;
import org.hiero.consensusnode.transactions.Transaction;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// Updates one or more of a token's mutable fields. Every nullable field is "leave unchanged"
/// when null. `tokenType`, `supplyType`, `decimals`, and `initialSupply` are NOT updatable — they
/// are fixed at create.
///
/// Rotating a key field to a new key requires the new key to also sign (anti-spoofing). A key
/// field that was unset at create cannot be added here (the network rejects it with
/// `KEY_NOT_PROVIDED`). Clearing a key field to permanently disable the corresponding capability
/// (HIP-540) is not supported yet.
public final class TokenUpdateTransaction extends Transaction<TokenUpdateReceipt, TokenUpdateTransaction> {

    private final Address tokenId;
    private final @Nullable String name;
    private final @Nullable String symbol;
    private final @Nullable AccountId treasuryAccountId;
    private final @Nullable String tokenMemo;
    private final byte @Nullable [] metadata;
    private final @Nullable ZonedDateTime expirationTime;
    private final @Nullable Duration autoRenewPeriod;
    private final @Nullable AccountId autoRenewAccount;
    private final @Nullable Authority adminAuthority;
    private final @Nullable Authority supplyAuthority;
    private final @Nullable Authority kycAuthority;
    private final @Nullable Authority freezeAuthority;
    private final @Nullable Authority wipeAuthority;
    private final @Nullable Authority pauseAuthority;
    private final @Nullable Authority feeScheduleAuthority;
    private final @Nullable Authority metadataAuthority;

    /// Creates a new `TokenUpdateTransaction`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public TokenUpdateTransaction(
            final Address tokenId,
            final @Nullable String name,
            final @Nullable String symbol,
            final @Nullable AccountId treasuryAccountId,
            final @Nullable String tokenMemo,
            final byte @Nullable [] metadata,
            final @Nullable ZonedDateTime expirationTime,
            final @Nullable Duration autoRenewPeriod,
            final @Nullable AccountId autoRenewAccount,
            final @Nullable Authority adminAuthority,
            final @Nullable Authority supplyAuthority,
            final @Nullable Authority kycAuthority,
            final @Nullable Authority freezeAuthority,
            final @Nullable Authority wipeAuthority,
            final @Nullable Authority pauseAuthority,
            final @Nullable Authority feeScheduleAuthority,
            final @Nullable Authority metadataAuthority) {
        super();
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        this.tokenId = tokenId;
        if (name != null && name.getBytes(StandardCharsets.UTF_8).length > 100) {
            throw new IllegalArgumentException("name must be at most 100 bytes long in UTF-8");
        }
        this.name = name;
        if (symbol != null && symbol.getBytes(StandardCharsets.UTF_8).length > 100) {
            throw new IllegalArgumentException("symbol must be at most 100 bytes long in UTF-8");
        }
        this.symbol = symbol;
        this.treasuryAccountId = treasuryAccountId;
        if (tokenMemo != null && tokenMemo.getBytes(StandardCharsets.UTF_8).length > 100) {
            throw new IllegalArgumentException("tokenMemo must be at most 100 bytes long in UTF-8");
        }
        this.tokenMemo = tokenMemo;
        if (metadata != null && metadata.length > 100) {
            throw new IllegalArgumentException("metadata must contain at most 100 element(s)");
        }
        this.metadata = metadata == null ? null : metadata.clone();
        this.expirationTime = expirationTime;
        this.autoRenewPeriod = autoRenewPeriod;
        this.autoRenewAccount = autoRenewAccount;
        this.adminAuthority = adminAuthority;
        this.supplyAuthority = supplyAuthority;
        this.kycAuthority = kycAuthority;
        this.freezeAuthority = freezeAuthority;
        this.wipeAuthority = wipeAuthority;
        this.pauseAuthority = pauseAuthority;
        this.feeScheduleAuthority = feeScheduleAuthority;
        this.metadataAuthority = metadataAuthority;
    }

    /// the token being updated
    public Address tokenId() {
        return tokenId;
    }

    /// Returns the `name`.
    public @Nullable String name() {
        return name;
    }

    /// Returns the `symbol`.
    public @Nullable String symbol() {
        return symbol;
    }

    /// when set, replaces the treasury; both the OLD and the NEW treasury MUST sign
    public @Nullable AccountId treasuryAccountId() {
        return treasuryAccountId;
    }

    /// Returns the `tokenMemo`.
    public @Nullable String tokenMemo() {
        return tokenMemo;
    }

    /// when set, replaces the token-level metadata
    public byte @Nullable [] metadata() {
        return metadata == null ? null : metadata.clone();
    }

    /// Returns the `expirationTime`.
    public @Nullable ZonedDateTime expirationTime() {
        return expirationTime;
    }

    /// Returns the `autoRenewPeriod`.
    public @Nullable Duration autoRenewPeriod() {
        return autoRenewPeriod;
    }

    /// when set, replaces the auto-renew account; the new account MUST also sign
    public @Nullable AccountId autoRenewAccount() {
        return autoRenewAccount;
    }

    /// Key rotations — each requires the new key to also sign (anti-spoofing). A key that was
    /// unset at create cannot be set here.
    public @Nullable Authority adminAuthority() {
        return adminAuthority;
    }

    /// Returns the `supplyAuthority`.
    public @Nullable Authority supplyAuthority() {
        return supplyAuthority;
    }

    /// Returns the `kycAuthority`.
    public @Nullable Authority kycAuthority() {
        return kycAuthority;
    }

    /// Returns the `freezeAuthority`.
    public @Nullable Authority freezeAuthority() {
        return freezeAuthority;
    }

    /// Returns the `wipeAuthority`.
    public @Nullable Authority wipeAuthority() {
        return wipeAuthority;
    }

    /// Returns the `pauseAuthority`.
    public @Nullable Authority pauseAuthority() {
        return pauseAuthority;
    }

    /// Returns the `feeScheduleAuthority`.
    public @Nullable Authority feeScheduleAuthority() {
        return feeScheduleAuthority;
    }

    /// Returns the `metadataAuthority`.
    public @Nullable Authority metadataAuthority() {
        return metadataAuthority;
    }

    @Override
    public PackedTransaction<TokenUpdateReceipt, TokenUpdateTransaction> pack(final Account payer, final List<AccountId> nodes) {
        throw new UnsupportedOperationException("Not implemented yet: TokenUpdateTransaction.pack");
    }

    @Override
    public PackedTransaction<TokenUpdateReceipt, TokenUpdateTransaction> signWithOperator(final HieroClient<?> client) {
        throw new UnsupportedOperationException("Not implemented yet: TokenUpdateTransaction.signWithOperator");
    }

    @Override
    public PackedTransaction<TokenUpdateReceipt, TokenUpdateTransaction> sign(final Account payer, final List<AccountId> nodes) {
        throw new UnsupportedOperationException("Not implemented yet: TokenUpdateTransaction.sign");
    }

    @Override
    public PackedTransaction<TokenUpdateReceipt, TokenUpdateTransaction> sign(final AccountId payerId, final TransactionSigner signer, final List<AccountId> nodes) {
        throw new UnsupportedOperationException("Not implemented yet: TokenUpdateTransaction.sign");
    }

    @Override
    public CompletionStage<Response<TokenUpdateReceipt>> signWithOperatorAndSubmit(final HieroClient<?> client) {
        throw new UnsupportedOperationException("Not implemented yet: TokenUpdateTransaction.signWithOperatorAndSubmit");
    }

    /// Packs this transaction as the *inner* transaction of a BatchTransaction (HIP-551), without
    /// signing it. Unlike pack(payer, nodes), this produces a single TransactionBody addressed to no
    /// consensus node (nodeAccountID = 0.0.0) instead of one body per target node — an inner batch
    /// transaction is never submitted to a node on its own; it is embedded into
    /// BatchTransaction.innerTransactions and executed by the network as part of the batch. The
    /// returned PackedTransaction still carries its own TransactionId (payer + validStart); further
    /// signatures use PackedTransaction.sign(...) / signableBodies().
    ///
    /// batchKey names the Authority that must sign the *outer* BatchTransaction for this inner
    /// transaction to execute — the inner author's controlled opt-in to being batched. Whether a
    /// transaction type may be batched at all is decided by the network.
    @Override
    public PackedTransaction<TokenUpdateReceipt, TokenUpdateTransaction> packForBatch(final Account payer, final Authority batchKey) {
        throw new UnsupportedOperationException("Not implemented yet: TokenUpdateTransaction.packForBatch");
    }

    /// Packs this transaction for a batch with the client's operator as payer and adds the operator's
    /// signature. The batch counterpart of signWithOperator(client).
    @Override
    public PackedTransaction<TokenUpdateReceipt, TokenUpdateTransaction> signForBatchWithOperator(final HieroClient<?> client, final Authority batchKey) {
        throw new UnsupportedOperationException("Not implemented yet: TokenUpdateTransaction.signForBatchWithOperator");
    }

    /// Packs this transaction for a batch with the given Account as payer and adds its signature. The
    /// batch counterpart of sign(payer, nodes).
    @Override
    public PackedTransaction<TokenUpdateReceipt, TokenUpdateTransaction> signForBatch(final Account payer, final Authority batchKey) {
        throw new UnsupportedOperationException("Not implemented yet: TokenUpdateTransaction.signForBatch");
    }

    /// Packs this transaction for a batch with payerId as payer and signs it with the given
    /// TransactionSigner (HSM, hardware wallet, paymaster). The batch counterpart of
    /// sign(payerId, signer, nodes).
    @Override
    public PackedTransaction<TokenUpdateReceipt, TokenUpdateTransaction> signForBatch(final AccountId payerId, final TransactionSigner signer, final Authority batchKey) {
        throw new UnsupportedOperationException("Not implemented yet: TokenUpdateTransaction.signForBatch");
    }

    /// Returns a builder for a new `TokenUpdateTransaction`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `TokenUpdateTransaction`.
    ///
    /// `build()` creates the `TokenUpdateTransaction` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Address tokenId;
        private @Nullable String name;
        private @Nullable String symbol;
        private @Nullable AccountId treasuryAccountId;
        private @Nullable String tokenMemo;
        private byte @Nullable [] metadata;
        private @Nullable ZonedDateTime expirationTime;
        private @Nullable Duration autoRenewPeriod;
        private @Nullable AccountId autoRenewAccount;
        private @Nullable Authority adminAuthority;
        private @Nullable Authority supplyAuthority;
        private @Nullable Authority kycAuthority;
        private @Nullable Authority freezeAuthority;
        private @Nullable Authority wipeAuthority;
        private @Nullable Authority pauseAuthority;
        private @Nullable Authority feeScheduleAuthority;
        private @Nullable Authority metadataAuthority;

        private Builder() {
            // created by TokenUpdateTransaction.builder()
        }

        /// Sets the `tokenId`.
        ///
        /// the token being updated
        ///
        /// @param tokenId the value
        /// @return this builder
        public Builder tokenId(final Address tokenId) {
            this.tokenId = tokenId;
            return this;
        }

        /// Sets the `name`.
        ///
        /// @param name the value
        /// @return this builder
        public Builder name(final @Nullable String name) {
            this.name = name;
            return this;
        }

        /// Sets the `symbol`.
        ///
        /// @param symbol the value
        /// @return this builder
        public Builder symbol(final @Nullable String symbol) {
            this.symbol = symbol;
            return this;
        }

        /// Sets the `treasuryAccountId`.
        ///
        /// when set, replaces the treasury; both the OLD and the NEW treasury MUST sign
        ///
        /// @param treasuryAccountId the value
        /// @return this builder
        public Builder treasuryAccountId(final @Nullable AccountId treasuryAccountId) {
            this.treasuryAccountId = treasuryAccountId;
            return this;
        }

        /// Sets the `tokenMemo`.
        ///
        /// @param tokenMemo the value
        /// @return this builder
        public Builder tokenMemo(final @Nullable String tokenMemo) {
            this.tokenMemo = tokenMemo;
            return this;
        }

        /// Sets the `metadata`.
        ///
        /// when set, replaces the token-level metadata
        ///
        /// @param metadata the value
        /// @return this builder
        public Builder metadata(final byte @Nullable [] metadata) {
            this.metadata = metadata;
            return this;
        }

        /// Sets the `expirationTime`.
        ///
        /// @param expirationTime the value
        /// @return this builder
        public Builder expirationTime(final @Nullable ZonedDateTime expirationTime) {
            this.expirationTime = expirationTime;
            return this;
        }

        /// Sets the `autoRenewPeriod`.
        ///
        /// @param autoRenewPeriod the value
        /// @return this builder
        public Builder autoRenewPeriod(final @Nullable Duration autoRenewPeriod) {
            this.autoRenewPeriod = autoRenewPeriod;
            return this;
        }

        /// Sets the `autoRenewAccount`.
        ///
        /// when set, replaces the auto-renew account; the new account MUST also sign
        ///
        /// @param autoRenewAccount the value
        /// @return this builder
        public Builder autoRenewAccount(final @Nullable AccountId autoRenewAccount) {
            this.autoRenewAccount = autoRenewAccount;
            return this;
        }

        /// Sets the `adminAuthority`.
        ///
        /// Key rotations — each requires the new key to also sign (anti-spoofing). A key that was
        /// unset at create cannot be set here.
        ///
        /// @param adminAuthority the value
        /// @return this builder
        public Builder adminAuthority(final @Nullable Authority adminAuthority) {
            this.adminAuthority = adminAuthority;
            return this;
        }

        /// Sets the `supplyAuthority`.
        ///
        /// @param supplyAuthority the value
        /// @return this builder
        public Builder supplyAuthority(final @Nullable Authority supplyAuthority) {
            this.supplyAuthority = supplyAuthority;
            return this;
        }

        /// Sets the `kycAuthority`.
        ///
        /// @param kycAuthority the value
        /// @return this builder
        public Builder kycAuthority(final @Nullable Authority kycAuthority) {
            this.kycAuthority = kycAuthority;
            return this;
        }

        /// Sets the `freezeAuthority`.
        ///
        /// @param freezeAuthority the value
        /// @return this builder
        public Builder freezeAuthority(final @Nullable Authority freezeAuthority) {
            this.freezeAuthority = freezeAuthority;
            return this;
        }

        /// Sets the `wipeAuthority`.
        ///
        /// @param wipeAuthority the value
        /// @return this builder
        public Builder wipeAuthority(final @Nullable Authority wipeAuthority) {
            this.wipeAuthority = wipeAuthority;
            return this;
        }

        /// Sets the `pauseAuthority`.
        ///
        /// @param pauseAuthority the value
        /// @return this builder
        public Builder pauseAuthority(final @Nullable Authority pauseAuthority) {
            this.pauseAuthority = pauseAuthority;
            return this;
        }

        /// Sets the `feeScheduleAuthority`.
        ///
        /// @param feeScheduleAuthority the value
        /// @return this builder
        public Builder feeScheduleAuthority(final @Nullable Authority feeScheduleAuthority) {
            this.feeScheduleAuthority = feeScheduleAuthority;
            return this;
        }

        /// Sets the `metadataAuthority`.
        ///
        /// @param metadataAuthority the value
        /// @return this builder
        public Builder metadataAuthority(final @Nullable Authority metadataAuthority) {
            this.metadataAuthority = metadataAuthority;
            return this;
        }

        /// Creates the `TokenUpdateTransaction` of the values set.
        ///
        /// @return the new `TokenUpdateTransaction`
        /// @throws NullPointerException if a required value is not set
        /// @throws IllegalArgumentException if a value violates its constraints
        public TokenUpdateTransaction build() {
            return new TokenUpdateTransaction(
                    Objects.requireNonNull(tokenId, "tokenId must not be null"),
                    name,
                    symbol,
                    treasuryAccountId,
                    tokenMemo,
                    metadata,
                    expirationTime,
                    autoRenewPeriod,
                    autoRenewAccount,
                    adminAuthority,
                    supplyAuthority,
                    kycAuthority,
                    freezeAuthority,
                    wipeAuthority,
                    pauseAuthority,
                    feeScheduleAuthority,
                    metadataAuthority);
        }
    }

    @Override
    public String toString() {
        return "TokenUpdateTransaction[maxTransactionFee=" + maxTransactionFee()
                + ", validDuration=" + validDuration()
                + ", memo=" + memo()
                + ", tokenId=" + tokenId()
                + ", name=" + name()
                + ", symbol=" + symbol()
                + ", treasuryAccountId=" + treasuryAccountId()
                + ", tokenMemo=" + tokenMemo()
                + ", metadata=" + (metadata() == null ? "null" : "byte[" + metadata().length + "]")
                + ", expirationTime=" + expirationTime()
                + ", autoRenewPeriod=" + autoRenewPeriod()
                + ", autoRenewAccount=" + autoRenewAccount()
                + ", adminAuthority=" + adminAuthority()
                + ", supplyAuthority=" + supplyAuthority()
                + ", kycAuthority=" + kycAuthority()
                + ", freezeAuthority=" + freezeAuthority()
                + ", wipeAuthority=" + wipeAuthority()
                + ", pauseAuthority=" + pauseAuthority()
                + ", feeScheduleAuthority=" + feeScheduleAuthority()
                + ", metadataAuthority=" + metadataAuthority()
                + "]";
    }
}
