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
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// The receiver accepts pending airdrops, auto-associating and crediting the tokens. Each receiver
/// must sign. Protocol cap 10 per transaction; no duplicates.
public final class TokenClaimAirdropTransaction extends Transaction<TokenClaimAirdropReceipt, TokenClaimAirdropTransaction> {

    private final List<PendingAirdrop> pendingAirdrops;

    /// Creates a new `TokenClaimAirdropTransaction`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public TokenClaimAirdropTransaction(final List<PendingAirdrop> pendingAirdrops) {
        super();
        Objects.requireNonNull(pendingAirdrops, "pendingAirdrops must not be null");
        if (pendingAirdrops.size() < 1) {
            throw new IllegalArgumentException("pendingAirdrops must contain at least 1 element(s)");
        }
        if (pendingAirdrops.size() > 10) {
            throw new IllegalArgumentException("pendingAirdrops must contain at most 10 element(s)");
        }
        this.pendingAirdrops = List.copyOf(pendingAirdrops);
    }

    /// Returns the `pendingAirdrops`.
    public List<PendingAirdrop> pendingAirdrops() {
        return pendingAirdrops;
    }

    @Override
    public PackedTransaction<TokenClaimAirdropReceipt, TokenClaimAirdropTransaction> pack(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(TokenKinds.TOKEN_CLAIM_AIRDROP).pack(this, payer, nodes);
    }

    @Override
    public PackedTransaction<TokenClaimAirdropReceipt, TokenClaimAirdropTransaction> signWithOperator(final HieroClient<?> client) {
        return new Packing<>(TokenKinds.TOKEN_CLAIM_AIRDROP).signWithOperator(this, client);
    }

    @Override
    public PackedTransaction<TokenClaimAirdropReceipt, TokenClaimAirdropTransaction> sign(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(TokenKinds.TOKEN_CLAIM_AIRDROP).sign(this, payer, nodes);
    }

    @Override
    public PackedTransaction<TokenClaimAirdropReceipt, TokenClaimAirdropTransaction> sign(final AccountId payerId, final TransactionSigner signer, final List<AccountId> nodes) {
        return new Packing<>(TokenKinds.TOKEN_CLAIM_AIRDROP).sign(this, payerId, signer, nodes);
    }

    @Override
    public CompletionStage<Response<TokenClaimAirdropReceipt>> signWithOperatorAndSubmit(final HieroClient<?> client) {
        return new Packing<>(TokenKinds.TOKEN_CLAIM_AIRDROP).signWithOperatorAndSubmit(this, client);
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
    public PackedTransaction<TokenClaimAirdropReceipt, TokenClaimAirdropTransaction> packForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(TokenKinds.TOKEN_CLAIM_AIRDROP).packForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with the client's operator as payer and adds the operator's
    /// signature. The batch counterpart of signWithOperator(client).
    @Override
    public PackedTransaction<TokenClaimAirdropReceipt, TokenClaimAirdropTransaction> signForBatchWithOperator(final HieroClient<?> client, final Authority batchKey) {
        return new Packing<>(TokenKinds.TOKEN_CLAIM_AIRDROP).signForBatchWithOperator(this, client, batchKey);
    }

    /// Packs this transaction for a batch with the given Account as payer and adds its signature. The
    /// batch counterpart of sign(payer, nodes).
    @Override
    public PackedTransaction<TokenClaimAirdropReceipt, TokenClaimAirdropTransaction> signForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(TokenKinds.TOKEN_CLAIM_AIRDROP).signForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with payerId as payer and signs it with the given
    /// TransactionSigner (HSM, hardware wallet, paymaster). The batch counterpart of
    /// sign(payerId, signer, nodes).
    @Override
    public PackedTransaction<TokenClaimAirdropReceipt, TokenClaimAirdropTransaction> signForBatch(final AccountId payerId, final TransactionSigner signer, final Authority batchKey) {
        return new Packing<>(TokenKinds.TOKEN_CLAIM_AIRDROP).signForBatch(this, payerId, signer, batchKey);
    }

    @Override
    public String toString() {
        return "TokenClaimAirdropTransaction[maxTransactionFee=" + maxTransactionFee()
                + ", validDuration=" + validDuration()
                + ", memo=" + memo()
                + ", pendingAirdrops=" + pendingAirdrops()
                + "]";
    }
}
