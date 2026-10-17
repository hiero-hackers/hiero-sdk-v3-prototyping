package org.hiero.consensusnode.transactions.tokens;

import org.hiero.consensusnode.client.internal.TokenKinds;
import org.hiero.consensusnode.client.internal.Packing;
import java.time.Duration;
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

/// Revokes KYC from a specific account for a token. Requires the token's kycAuthority.
public final class TokenRevokeKycTransaction extends Transaction<TokenRevokeKycReceipt, TokenRevokeKycTransaction> {

    private final Address tokenId;
    private final AccountId accountId;

    /// Creates a new `TokenRevokeKycTransaction`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public TokenRevokeKycTransaction(final Address tokenId, final AccountId accountId) {
        super();
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        this.tokenId = tokenId;
        Objects.requireNonNull(accountId, "accountId must not be null");
        this.accountId = accountId;
    }

    /// Returns the `tokenId`.
    public Address tokenId() {
        return tokenId;
    }

    /// Returns the `accountId`.
    public AccountId accountId() {
        return accountId;
    }

    @Override
    public PackedTransaction<TokenRevokeKycReceipt, TokenRevokeKycTransaction> pack(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(TokenKinds.TOKEN_REVOKE_KYC).pack(this, payer, nodes);
    }

    @Override
    public PackedTransaction<TokenRevokeKycReceipt, TokenRevokeKycTransaction> signWithOperator(final HieroClient<?> client) {
        return new Packing<>(TokenKinds.TOKEN_REVOKE_KYC).signWithOperator(this, client);
    }

    @Override
    public PackedTransaction<TokenRevokeKycReceipt, TokenRevokeKycTransaction> sign(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(TokenKinds.TOKEN_REVOKE_KYC).sign(this, payer, nodes);
    }

    @Override
    public PackedTransaction<TokenRevokeKycReceipt, TokenRevokeKycTransaction> sign(final AccountId payerId, final TransactionSigner signer, final List<AccountId> nodes) {
        return new Packing<>(TokenKinds.TOKEN_REVOKE_KYC).sign(this, payerId, signer, nodes);
    }

    @Override
    public CompletionStage<Response<TokenRevokeKycReceipt>> signWithOperatorAndSubmit(final HieroClient<?> client) {
        return new Packing<>(TokenKinds.TOKEN_REVOKE_KYC).signWithOperatorAndSubmit(this, client);
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
    public PackedTransaction<TokenRevokeKycReceipt, TokenRevokeKycTransaction> packForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(TokenKinds.TOKEN_REVOKE_KYC).packForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with the client's operator as payer and adds the operator's
    /// signature. The batch counterpart of signWithOperator(client).
    @Override
    public PackedTransaction<TokenRevokeKycReceipt, TokenRevokeKycTransaction> signForBatchWithOperator(final HieroClient<?> client, final Authority batchKey) {
        return new Packing<>(TokenKinds.TOKEN_REVOKE_KYC).signForBatchWithOperator(this, client, batchKey);
    }

    /// Packs this transaction for a batch with the given Account as payer and adds its signature. The
    /// batch counterpart of sign(payer, nodes).
    @Override
    public PackedTransaction<TokenRevokeKycReceipt, TokenRevokeKycTransaction> signForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(TokenKinds.TOKEN_REVOKE_KYC).signForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with payerId as payer and signs it with the given
    /// TransactionSigner (HSM, hardware wallet, paymaster). The batch counterpart of
    /// sign(payerId, signer, nodes).
    @Override
    public PackedTransaction<TokenRevokeKycReceipt, TokenRevokeKycTransaction> signForBatch(final AccountId payerId, final TransactionSigner signer, final Authority batchKey) {
        return new Packing<>(TokenKinds.TOKEN_REVOKE_KYC).signForBatch(this, payerId, signer, batchKey);
    }

    @Override
    public String toString() {
        return "TokenRevokeKycTransaction[maxTransactionFee=" + maxTransactionFee()
                + ", validDuration=" + validDuration()
                + ", memo=" + memo()
                + ", tokenId=" + tokenId()
                + ", accountId=" + accountId()
                + "]";
    }
}
