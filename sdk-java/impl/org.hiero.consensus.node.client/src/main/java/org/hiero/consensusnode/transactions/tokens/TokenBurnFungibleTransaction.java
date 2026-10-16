package org.hiero.consensusnode.transactions.tokens;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletionStage;
import org.hiero.authority.Authority;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.client.TransactionSigner;
import org.hiero.consensusnode.transactions.PackedTransaction;
import org.hiero.consensusnode.transactions.Response;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// Burns units of a FUNGIBLE_COMMON token held by the treasury.
public final class TokenBurnFungibleTransaction extends TokenBurnTransaction<TokenBurnFungibleTransaction> {

    private final long amount;

    /// Creates a new `TokenBurnFungibleTransaction`.
    ///
    /// @throws IllegalArgumentException if a value violates its constraints
    public TokenBurnFungibleTransaction(final Address tokenId, final long amount) {
        super(tokenId);
        if (amount < 1L) {
            throw new IllegalArgumentException("amount must be at least 1");
        }
        this.amount = amount;
    }

    /// amount to burn, in the smallest indivisible unit
    public long amount() {
        return amount;
    }

    @Override
    public PackedTransaction<TokenBurnReceipt, TokenBurnFungibleTransaction> pack(final Account payer, final List<AccountId> nodes) {
        throw new UnsupportedOperationException("Not implemented yet: TokenBurnFungibleTransaction.pack");
    }

    @Override
    public PackedTransaction<TokenBurnReceipt, TokenBurnFungibleTransaction> signWithOperator(final HieroClient<?> client) {
        throw new UnsupportedOperationException("Not implemented yet: TokenBurnFungibleTransaction.signWithOperator");
    }

    @Override
    public PackedTransaction<TokenBurnReceipt, TokenBurnFungibleTransaction> sign(final Account payer, final List<AccountId> nodes) {
        throw new UnsupportedOperationException("Not implemented yet: TokenBurnFungibleTransaction.sign");
    }

    @Override
    public PackedTransaction<TokenBurnReceipt, TokenBurnFungibleTransaction> sign(final AccountId payerId, final TransactionSigner signer, final List<AccountId> nodes) {
        throw new UnsupportedOperationException("Not implemented yet: TokenBurnFungibleTransaction.sign");
    }

    @Override
    public CompletionStage<Response<TokenBurnReceipt>> signWithOperatorAndSubmit(final HieroClient<?> client) {
        throw new UnsupportedOperationException("Not implemented yet: TokenBurnFungibleTransaction.signWithOperatorAndSubmit");
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
    public PackedTransaction<TokenBurnReceipt, TokenBurnFungibleTransaction> packForBatch(final Account payer, final Authority batchKey) {
        throw new UnsupportedOperationException("Not implemented yet: TokenBurnFungibleTransaction.packForBatch");
    }

    /// Packs this transaction for a batch with the client's operator as payer and adds the operator's
    /// signature. The batch counterpart of signWithOperator(client).
    @Override
    public PackedTransaction<TokenBurnReceipt, TokenBurnFungibleTransaction> signForBatchWithOperator(final HieroClient<?> client, final Authority batchKey) {
        throw new UnsupportedOperationException("Not implemented yet: TokenBurnFungibleTransaction.signForBatchWithOperator");
    }

    /// Packs this transaction for a batch with the given Account as payer and adds its signature. The
    /// batch counterpart of sign(payer, nodes).
    @Override
    public PackedTransaction<TokenBurnReceipt, TokenBurnFungibleTransaction> signForBatch(final Account payer, final Authority batchKey) {
        throw new UnsupportedOperationException("Not implemented yet: TokenBurnFungibleTransaction.signForBatch");
    }

    /// Packs this transaction for a batch with payerId as payer and signs it with the given
    /// TransactionSigner (HSM, hardware wallet, paymaster). The batch counterpart of
    /// sign(payerId, signer, nodes).
    @Override
    public PackedTransaction<TokenBurnReceipt, TokenBurnFungibleTransaction> signForBatch(final AccountId payerId, final TransactionSigner signer, final Authority batchKey) {
        throw new UnsupportedOperationException("Not implemented yet: TokenBurnFungibleTransaction.signForBatch");
    }

    @Override
    public String toString() {
        return "TokenBurnFungibleTransaction[maxTransactionFee=" + maxTransactionFee()
                + ", validDuration=" + validDuration()
                + ", memo=" + memo()
                + ", tokenId=" + tokenId()
                + ", amount=" + amount()
                + "]";
    }
}
