package org.hiero.consensusnode.transactions.tokens;

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
import org.hiero.nativeToken.NativeToken;
import org.hiero.token.TokenSupplyType;
import org.hiero.token.TokenType;
import org.jspecify.annotations.Nullable;

/// Creates a new token. The token is identified by the `tokenId` returned in the receipt. All
/// eight key fields are optional and follow the rule that a key not set at create time can never
/// be added later (the network rejects it with `KEY_NOT_PROVIDED`); leaving e.g. `supplyAuthority`
/// unset permanently fixes the token's supply at `initialSupply`.
public final class TokenCreateTransaction extends Transaction<TokenCreateReceipt, TokenCreateTransaction> {

    private final String name;
    private final String symbol;
    private final TokenType tokenType;
    private final int decimals;
    private final long initialSupply;
    private final AccountId treasuryAccountId;
    private final TokenSupplyType supplyType;
    private final @Nullable Long maxSupply;
    private final boolean freezeDefault;
    private final @Nullable String tokenMemo;
    private final byte[] metadata;
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

    /// Creates a new `TokenCreateTransaction`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public TokenCreateTransaction(
            final String name,
            final String symbol,
            final TokenType tokenType,
            final int decimals,
            final long initialSupply,
            final AccountId treasuryAccountId,
            final TokenSupplyType supplyType,
            final @Nullable Long maxSupply,
            final boolean freezeDefault,
            final @Nullable String tokenMemo,
            final byte[] metadata,
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
        Objects.requireNonNull(name, "name must not be null");
        if (name.length() > 100) {
            throw new IllegalArgumentException("name must be at most 100 characters long");
        }
        this.name = name;
        Objects.requireNonNull(symbol, "symbol must not be null");
        if (symbol.length() > 100) {
            throw new IllegalArgumentException("symbol must be at most 100 characters long");
        }
        this.symbol = symbol;
        Objects.requireNonNull(tokenType, "tokenType must not be null");
        this.tokenType = tokenType;
        this.decimals = decimals;
        this.initialSupply = initialSupply;
        Objects.requireNonNull(treasuryAccountId, "treasuryAccountId must not be null");
        this.treasuryAccountId = treasuryAccountId;
        Objects.requireNonNull(supplyType, "supplyType must not be null");
        this.supplyType = supplyType;
        if (maxSupply != null && maxSupply < 1L) {
            throw new IllegalArgumentException("maxSupply must be at least 1");
        }
        this.maxSupply = maxSupply;
        this.freezeDefault = freezeDefault;
        if (tokenMemo != null && tokenMemo.length() > 100) {
            throw new IllegalArgumentException("tokenMemo must be at most 100 characters long");
        }
        this.tokenMemo = tokenMemo;
        Objects.requireNonNull(metadata, "metadata must not be null");
        this.metadata = metadata.clone();
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

    /// Creates a new `TokenCreateTransaction` with the default value of `decimals`, `initialSupply`, `supplyType`, `freezeDefault`, `metadata`.
    public TokenCreateTransaction(
            final String name,
            final String symbol,
            final TokenType tokenType,
            final AccountId treasuryAccountId,
            final @Nullable Long maxSupply,
            final @Nullable String tokenMemo,
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
        this(name, symbol, tokenType, 0, 0L, treasuryAccountId, TokenSupplyType.INFINITE, maxSupply, false, tokenMemo, new byte[0], expirationTime, autoRenewPeriod, autoRenewAccount, adminAuthority, supplyAuthority, kycAuthority, freezeAuthority, wipeAuthority, pauseAuthority, feeScheduleAuthority, metadataAuthority);
    }

    /// human-readable token name
    public String name() {
        return name;
    }

    /// short ticker / symbol
    public String symbol() {
        return symbol;
    }

    /// FUNGIBLE_COMMON | NON_FUNGIBLE_UNIQUE; cannot be changed later
    public TokenType tokenType() {
        return tokenType;
    }

    /// FUNGIBLE_COMMON only: precision of the smallest indivisible unit (e.g. 6 for USDC); MUST be 0 for NON_FUNGIBLE_UNIQUE
    public int decimals() {
        return decimals;
    }

    /// FUNGIBLE_COMMON only: amount minted to the treasury at create (in smallest unit); MUST be 0 for NON_FUNGIBLE_UNIQUE
    public long initialSupply() {
        return initialSupply;
    }

    /// account receiving `initialSupply` and all subsequent mints; MUST sign the create transaction
    public AccountId treasuryAccountId() {
        return treasuryAccountId;
    }

    /// INFINITE | FINITE; cannot be changed later
    public TokenSupplyType supplyType() {
        return supplyType;
    }

    /// FINITE only: protocol-enforced ceiling on totalSupply; unset (and only valid) when supplyType is INFINITE
    public @Nullable Long maxSupply() {
        return maxSupply;
    }

    /// when true, newly associated accounts start in the FROZEN state (require an explicit TokenUnfreeze before they can move balances); requires freezeAuthority to be set
    public boolean freezeDefault() {
        return freezeDefault;
    }

    /// short, human-readable label
    public @Nullable String tokenMemo() {
        return tokenMemo;
    }

    /// opaque token-level metadata (e.g. IPFS CID, HTTPS URL, JSON manifest)
    public byte[] metadata() {
        return metadata.clone();
    }

    /// when the token expires; SDK default applies when unset
    public @Nullable ZonedDateTime expirationTime() {
        return expirationTime;
    }

    /// renewal window applied when expiration approaches; protocol default applies when unset
    public @Nullable Duration autoRenewPeriod() {
        return autoRenewPeriod;
    }

    /// account paying for auto-renewal; MUST sign the create transaction if set and not equal to the treasury / payer
    public @Nullable AccountId autoRenewAccount() {
        return autoRenewAccount;
    }

    /// controls update / delete; unset → immutable token; MUST sign the create transaction when set (anti-spoofing)
    public @Nullable Authority adminAuthority() {
        return adminAuthority;
    }

    /// controls mint / burn; unset → fixed supply after creation
    public @Nullable Authority supplyAuthority() {
        return supplyAuthority;
    }

    /// controls KYC grant / revoke; unset → KYC disabled
    public @Nullable Authority kycAuthority() {
        return kycAuthority;
    }

    /// controls freeze / unfreeze; unset → no freeze
    public @Nullable Authority freezeAuthority() {
        return freezeAuthority;
    }

    /// controls wipe of non-treasury balances; unset → wipe disabled
    public @Nullable Authority wipeAuthority() {
        return wipeAuthority;
    }

    /// controls pause / unpause; unset → unpausable
    public @Nullable Authority pauseAuthority() {
        return pauseAuthority;
    }

    /// controls custom-fee schedule updates; unset → fees immutable
    public @Nullable Authority feeScheduleAuthority() {
        return feeScheduleAuthority;
    }

    /// controls per-serial NFT metadata updates (HIP-657); unset → metadata immutable
    public @Nullable Authority metadataAuthority() {
        return metadataAuthority;
    }

    @Override
    public PackedTransaction<TokenCreateReceipt, TokenCreateTransaction> pack(final Account payer, final List<AccountId> nodes) {
        throw new UnsupportedOperationException("Not implemented yet: TokenCreateTransaction.pack");
    }

    @Override
    public PackedTransaction<TokenCreateReceipt, TokenCreateTransaction> signWithOperator(final HieroClient<?> client) {
        throw new UnsupportedOperationException("Not implemented yet: TokenCreateTransaction.signWithOperator");
    }

    @Override
    public PackedTransaction<TokenCreateReceipt, TokenCreateTransaction> sign(final Account payer, final List<AccountId> nodes) {
        throw new UnsupportedOperationException("Not implemented yet: TokenCreateTransaction.sign");
    }

    @Override
    public PackedTransaction<TokenCreateReceipt, TokenCreateTransaction> sign(final AccountId payerId, final TransactionSigner signer, final List<AccountId> nodes) {
        throw new UnsupportedOperationException("Not implemented yet: TokenCreateTransaction.sign");
    }

    @Override
    public CompletionStage<Response<TokenCreateReceipt>> signWithOperatorAndSubmit(final HieroClient<?> client) {
        throw new UnsupportedOperationException("Not implemented yet: TokenCreateTransaction.signWithOperatorAndSubmit");
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
    public PackedTransaction<TokenCreateReceipt, TokenCreateTransaction> packForBatch(final Account payer, final Authority batchKey) {
        throw new UnsupportedOperationException("Not implemented yet: TokenCreateTransaction.packForBatch");
    }

    /// Packs this transaction for a batch with the client's operator as payer and adds the operator's
    /// signature. The batch counterpart of signWithOperator(client).
    @Override
    public PackedTransaction<TokenCreateReceipt, TokenCreateTransaction> signForBatchWithOperator(final HieroClient<?> client, final Authority batchKey) {
        throw new UnsupportedOperationException("Not implemented yet: TokenCreateTransaction.signForBatchWithOperator");
    }

    /// Packs this transaction for a batch with the given Account as payer and adds its signature. The
    /// batch counterpart of sign(payer, nodes).
    @Override
    public PackedTransaction<TokenCreateReceipt, TokenCreateTransaction> signForBatch(final Account payer, final Authority batchKey) {
        throw new UnsupportedOperationException("Not implemented yet: TokenCreateTransaction.signForBatch");
    }

    /// Packs this transaction for a batch with payerId as payer and signs it with the given
    /// TransactionSigner (HSM, hardware wallet, paymaster). The batch counterpart of
    /// sign(payerId, signer, nodes).
    @Override
    public PackedTransaction<TokenCreateReceipt, TokenCreateTransaction> signForBatch(final AccountId payerId, final TransactionSigner signer, final Authority batchKey) {
        throw new UnsupportedOperationException("Not implemented yet: TokenCreateTransaction.signForBatch");
    }

    /// Returns a builder for a new `TokenCreateTransaction`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `TokenCreateTransaction`.
    ///
    /// `build()` creates the `TokenCreateTransaction` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable String name;
        private @Nullable String symbol;
        private @Nullable TokenType tokenType;
        private int decimals = 0;
        private long initialSupply = 0L;
        private @Nullable AccountId treasuryAccountId;
        private TokenSupplyType supplyType = TokenSupplyType.INFINITE;
        private @Nullable Long maxSupply;
        private boolean freezeDefault = false;
        private @Nullable String tokenMemo;
        private byte[] metadata = new byte[0];
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
            // created by TokenCreateTransaction.builder()
        }

        /// Sets the `name`.
        ///
        /// human-readable token name
        ///
        /// @param name the value
        /// @return this builder
        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        /// Sets the `symbol`.
        ///
        /// short ticker / symbol
        ///
        /// @param symbol the value
        /// @return this builder
        public Builder symbol(final String symbol) {
            this.symbol = symbol;
            return this;
        }

        /// Sets the `tokenType`.
        ///
        /// FUNGIBLE_COMMON | NON_FUNGIBLE_UNIQUE; cannot be changed later
        ///
        /// @param tokenType the value
        /// @return this builder
        public Builder tokenType(final TokenType tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        /// Sets the `decimals`.
        ///
        /// FUNGIBLE_COMMON only: precision of the smallest indivisible unit (e.g. 6 for USDC); MUST be 0 for NON_FUNGIBLE_UNIQUE
        ///
        /// @param decimals the value
        /// @return this builder
        public Builder decimals(final int decimals) {
            this.decimals = decimals;
            return this;
        }

        /// Sets the `initialSupply`.
        ///
        /// FUNGIBLE_COMMON only: amount minted to the treasury at create (in smallest unit); MUST be 0 for NON_FUNGIBLE_UNIQUE
        ///
        /// @param initialSupply the value
        /// @return this builder
        public Builder initialSupply(final long initialSupply) {
            this.initialSupply = initialSupply;
            return this;
        }

        /// Sets the `treasuryAccountId`.
        ///
        /// account receiving `initialSupply` and all subsequent mints; MUST sign the create transaction
        ///
        /// @param treasuryAccountId the value
        /// @return this builder
        public Builder treasuryAccountId(final AccountId treasuryAccountId) {
            this.treasuryAccountId = treasuryAccountId;
            return this;
        }

        /// Sets the `supplyType`.
        ///
        /// INFINITE | FINITE; cannot be changed later
        ///
        /// @param supplyType the value
        /// @return this builder
        public Builder supplyType(final TokenSupplyType supplyType) {
            this.supplyType = supplyType;
            return this;
        }

        /// Sets the `maxSupply`.
        ///
        /// FINITE only: protocol-enforced ceiling on totalSupply; unset (and only valid) when supplyType is INFINITE
        ///
        /// @param maxSupply the value
        /// @return this builder
        public Builder maxSupply(final @Nullable Long maxSupply) {
            this.maxSupply = maxSupply;
            return this;
        }

        /// Sets the `freezeDefault`.
        ///
        /// when true, newly associated accounts start in the FROZEN state (require an explicit TokenUnfreeze before they can move balances); requires freezeAuthority to be set
        ///
        /// @param freezeDefault the value
        /// @return this builder
        public Builder freezeDefault(final boolean freezeDefault) {
            this.freezeDefault = freezeDefault;
            return this;
        }

        /// Sets the `tokenMemo`.
        ///
        /// short, human-readable label
        ///
        /// @param tokenMemo the value
        /// @return this builder
        public Builder tokenMemo(final @Nullable String tokenMemo) {
            this.tokenMemo = tokenMemo;
            return this;
        }

        /// Sets the `metadata`.
        ///
        /// opaque token-level metadata (e.g. IPFS CID, HTTPS URL, JSON manifest)
        ///
        /// @param metadata the value
        /// @return this builder
        public Builder metadata(final byte[] metadata) {
            this.metadata = metadata;
            return this;
        }

        /// Sets the `expirationTime`.
        ///
        /// when the token expires; SDK default applies when unset
        ///
        /// @param expirationTime the value
        /// @return this builder
        public Builder expirationTime(final @Nullable ZonedDateTime expirationTime) {
            this.expirationTime = expirationTime;
            return this;
        }

        /// Sets the `autoRenewPeriod`.
        ///
        /// renewal window applied when expiration approaches; protocol default applies when unset
        ///
        /// @param autoRenewPeriod the value
        /// @return this builder
        public Builder autoRenewPeriod(final @Nullable Duration autoRenewPeriod) {
            this.autoRenewPeriod = autoRenewPeriod;
            return this;
        }

        /// Sets the `autoRenewAccount`.
        ///
        /// account paying for auto-renewal; MUST sign the create transaction if set and not equal to the treasury / payer
        ///
        /// @param autoRenewAccount the value
        /// @return this builder
        public Builder autoRenewAccount(final @Nullable AccountId autoRenewAccount) {
            this.autoRenewAccount = autoRenewAccount;
            return this;
        }

        /// Sets the `adminAuthority`.
        ///
        /// controls update / delete; unset → immutable token; MUST sign the create transaction when set (anti-spoofing)
        ///
        /// @param adminAuthority the value
        /// @return this builder
        public Builder adminAuthority(final @Nullable Authority adminAuthority) {
            this.adminAuthority = adminAuthority;
            return this;
        }

        /// Sets the `supplyAuthority`.
        ///
        /// controls mint / burn; unset → fixed supply after creation
        ///
        /// @param supplyAuthority the value
        /// @return this builder
        public Builder supplyAuthority(final @Nullable Authority supplyAuthority) {
            this.supplyAuthority = supplyAuthority;
            return this;
        }

        /// Sets the `kycAuthority`.
        ///
        /// controls KYC grant / revoke; unset → KYC disabled
        ///
        /// @param kycAuthority the value
        /// @return this builder
        public Builder kycAuthority(final @Nullable Authority kycAuthority) {
            this.kycAuthority = kycAuthority;
            return this;
        }

        /// Sets the `freezeAuthority`.
        ///
        /// controls freeze / unfreeze; unset → no freeze
        ///
        /// @param freezeAuthority the value
        /// @return this builder
        public Builder freezeAuthority(final @Nullable Authority freezeAuthority) {
            this.freezeAuthority = freezeAuthority;
            return this;
        }

        /// Sets the `wipeAuthority`.
        ///
        /// controls wipe of non-treasury balances; unset → wipe disabled
        ///
        /// @param wipeAuthority the value
        /// @return this builder
        public Builder wipeAuthority(final @Nullable Authority wipeAuthority) {
            this.wipeAuthority = wipeAuthority;
            return this;
        }

        /// Sets the `pauseAuthority`.
        ///
        /// controls pause / unpause; unset → unpausable
        ///
        /// @param pauseAuthority the value
        /// @return this builder
        public Builder pauseAuthority(final @Nullable Authority pauseAuthority) {
            this.pauseAuthority = pauseAuthority;
            return this;
        }

        /// Sets the `feeScheduleAuthority`.
        ///
        /// controls custom-fee schedule updates; unset → fees immutable
        ///
        /// @param feeScheduleAuthority the value
        /// @return this builder
        public Builder feeScheduleAuthority(final @Nullable Authority feeScheduleAuthority) {
            this.feeScheduleAuthority = feeScheduleAuthority;
            return this;
        }

        /// Sets the `metadataAuthority`.
        ///
        /// controls per-serial NFT metadata updates (HIP-657); unset → metadata immutable
        ///
        /// @param metadataAuthority the value
        /// @return this builder
        public Builder metadataAuthority(final @Nullable Authority metadataAuthority) {
            this.metadataAuthority = metadataAuthority;
            return this;
        }

        /// Creates the `TokenCreateTransaction` of the values set.
        ///
        /// @return the new `TokenCreateTransaction`
        /// @throws NullPointerException if a required value is not set
        /// @throws IllegalArgumentException if a value violates its constraints
        public TokenCreateTransaction build() {
            return new TokenCreateTransaction(
                    Objects.requireNonNull(name, "name must not be null"),
                    Objects.requireNonNull(symbol, "symbol must not be null"),
                    Objects.requireNonNull(tokenType, "tokenType must not be null"),
                    decimals,
                    initialSupply,
                    Objects.requireNonNull(treasuryAccountId, "treasuryAccountId must not be null"),
                    supplyType,
                    maxSupply,
                    freezeDefault,
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
        return "TokenCreateTransaction[maxTransactionFee=" + maxTransactionFee()
                + ", validDuration=" + validDuration()
                + ", memo=" + memo()
                + ", name=" + name()
                + ", symbol=" + symbol()
                + ", tokenType=" + tokenType()
                + ", decimals=" + decimals()
                + ", initialSupply=" + initialSupply()
                + ", treasuryAccountId=" + treasuryAccountId()
                + ", supplyType=" + supplyType()
                + ", maxSupply=" + maxSupply()
                + ", freezeDefault=" + freezeDefault()
                + ", tokenMemo=" + tokenMemo()
                + ", metadata=" + "byte[" + metadata().length + "]"
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
