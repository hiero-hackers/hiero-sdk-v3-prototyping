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

/// Wipes units of a FUNGIBLE_COMMON token from the account.
public final class TokenWipeFungibleTransaction extends TokenWipeTransaction<TokenWipeFungibleTransaction> {

    private final long amount;

    /// Creates a new `TokenWipeFungibleTransaction`.
    ///
    /// @throws IllegalArgumentException if a value violates its constraints
    public TokenWipeFungibleTransaction(final Address tokenId, final AccountId accountId, final long amount) {
        super(tokenId, accountId);
        if (amount < 1L) {
            throw new IllegalArgumentException("amount must be at least 1");
        }
        this.amount = amount;
    }

    /// amount to wipe, in the smallest indivisible unit
    public long amount() {
        return amount;
    }

    @Override
    public PackedTransaction<TokenWipeReceipt, TokenWipeFungibleTransaction> pack(final Account payer, final List<AccountId> nodes) {
        throw new UnsupportedOperationException("Not implemented yet: TokenWipeFungibleTransaction.pack");
    }

    @Override
    public PackedTransaction<TokenWipeReceipt, TokenWipeFungibleTransaction> signWithOperator(final HieroClient<?> client) {
        throw new UnsupportedOperationException("Not implemented yet: TokenWipeFungibleTransaction.signWithOperator");
    }

    @Override
    public PackedTransaction<TokenWipeReceipt, TokenWipeFungibleTransaction> sign(final Account payer, final List<AccountId> nodes) {
        throw new UnsupportedOperationException("Not implemented yet: TokenWipeFungibleTransaction.sign");
    }

    @Override
    public PackedTransaction<TokenWipeReceipt, TokenWipeFungibleTransaction> sign(final AccountId payerId, final TransactionSigner signer, final List<AccountId> nodes) {
        throw new UnsupportedOperationException("Not implemented yet: TokenWipeFungibleTransaction.sign");
    }

    @Override
    public CompletionStage<Response<TokenWipeReceipt>> signWithOperatorAndSubmit(final HieroClient<?> client) {
        throw new UnsupportedOperationException("Not implemented yet: TokenWipeFungibleTransaction.signWithOperatorAndSubmit");
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
    public PackedTransaction<TokenWipeReceipt, TokenWipeFungibleTransaction> packForBatch(final Account payer, final Authority batchKey) {
        throw new UnsupportedOperationException("Not implemented yet: TokenWipeFungibleTransaction.packForBatch");
    }

    /// Packs this transaction for a batch with the client's operator as payer and adds the operator's
    /// signature. The batch counterpart of signWithOperator(client).
    @Override
    public PackedTransaction<TokenWipeReceipt, TokenWipeFungibleTransaction> signForBatchWithOperator(final HieroClient<?> client, final Authority batchKey) {
        throw new UnsupportedOperationException("Not implemented yet: TokenWipeFungibleTransaction.signForBatchWithOperator");
    }

    /// Packs this transaction for a batch with the given Account as payer and adds its signature. The
    /// batch counterpart of sign(payer, nodes).
    @Override
    public PackedTransaction<TokenWipeReceipt, TokenWipeFungibleTransaction> signForBatch(final Account payer, final Authority batchKey) {
        throw new UnsupportedOperationException("Not implemented yet: TokenWipeFungibleTransaction.signForBatch");
    }

    /// Packs this transaction for a batch with payerId as payer and signs it with the given
    /// TransactionSigner (HSM, hardware wallet, paymaster). The batch counterpart of
    /// sign(payerId, signer, nodes).
    @Override
    public PackedTransaction<TokenWipeReceipt, TokenWipeFungibleTransaction> signForBatch(final AccountId payerId, final TransactionSigner signer, final Authority batchKey) {
        throw new UnsupportedOperationException("Not implemented yet: TokenWipeFungibleTransaction.signForBatch");
    }

    @Override
    public String toString() {
        return "TokenWipeFungibleTransaction[maxTransactionFee=" + maxTransactionFee()
                + ", validDuration=" + validDuration()
                + ", memo=" + memo()
                + ", tokenId=" + tokenId()
                + ", accountId=" + accountId()
                + ", amount=" + amount()
                + "]";
    }
}
