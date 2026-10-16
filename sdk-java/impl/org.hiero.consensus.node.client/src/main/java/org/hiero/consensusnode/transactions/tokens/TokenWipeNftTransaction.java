package org.hiero.consensusnode.transactions.tokens;

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
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// Wipes NON_FUNGIBLE_UNIQUE serials held by the account.
public final class TokenWipeNftTransaction extends TokenWipeTransaction<TokenWipeNftTransaction> {

    private final List<Long> serials;

    /// Creates a new `TokenWipeNftTransaction`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public TokenWipeNftTransaction(final Address tokenId, final AccountId accountId, final List<Long> serials) {
        super(tokenId, accountId);
        Objects.requireNonNull(serials, "serials must not be null");
        if (serials.size() < 1) {
            throw new IllegalArgumentException("serials must contain at least 1 element(s)");
        }
        if (serials.size() > 10) {
            throw new IllegalArgumentException("serials must contain at most 10 element(s)");
        }
        this.serials = List.copyOf(serials);
    }

    /// serials held by accountId; the protocol caps the list at 10 per transaction
    public List<Long> serials() {
        return serials;
    }

    @Override
    public PackedTransaction<TokenWipeReceipt, TokenWipeNftTransaction> pack(final Account payer, final List<AccountId> nodes) {
        throw new UnsupportedOperationException("Not implemented yet: TokenWipeNftTransaction.pack");
    }

    @Override
    public PackedTransaction<TokenWipeReceipt, TokenWipeNftTransaction> signWithOperator(final HieroClient<?> client) {
        throw new UnsupportedOperationException("Not implemented yet: TokenWipeNftTransaction.signWithOperator");
    }

    @Override
    public PackedTransaction<TokenWipeReceipt, TokenWipeNftTransaction> sign(final Account payer, final List<AccountId> nodes) {
        throw new UnsupportedOperationException("Not implemented yet: TokenWipeNftTransaction.sign");
    }

    @Override
    public PackedTransaction<TokenWipeReceipt, TokenWipeNftTransaction> sign(final AccountId payerId, final TransactionSigner signer, final List<AccountId> nodes) {
        throw new UnsupportedOperationException("Not implemented yet: TokenWipeNftTransaction.sign");
    }

    @Override
    public CompletionStage<Response<TokenWipeReceipt>> signWithOperatorAndSubmit(final HieroClient<?> client) {
        throw new UnsupportedOperationException("Not implemented yet: TokenWipeNftTransaction.signWithOperatorAndSubmit");
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
    public PackedTransaction<TokenWipeReceipt, TokenWipeNftTransaction> packForBatch(final Account payer, final Authority batchKey) {
        throw new UnsupportedOperationException("Not implemented yet: TokenWipeNftTransaction.packForBatch");
    }

    /// Packs this transaction for a batch with the client's operator as payer and adds the operator's
    /// signature. The batch counterpart of signWithOperator(client).
    @Override
    public PackedTransaction<TokenWipeReceipt, TokenWipeNftTransaction> signForBatchWithOperator(final HieroClient<?> client, final Authority batchKey) {
        throw new UnsupportedOperationException("Not implemented yet: TokenWipeNftTransaction.signForBatchWithOperator");
    }

    /// Packs this transaction for a batch with the given Account as payer and adds its signature. The
    /// batch counterpart of sign(payer, nodes).
    @Override
    public PackedTransaction<TokenWipeReceipt, TokenWipeNftTransaction> signForBatch(final Account payer, final Authority batchKey) {
        throw new UnsupportedOperationException("Not implemented yet: TokenWipeNftTransaction.signForBatch");
    }

    /// Packs this transaction for a batch with payerId as payer and signs it with the given
    /// TransactionSigner (HSM, hardware wallet, paymaster). The batch counterpart of
    /// sign(payerId, signer, nodes).
    @Override
    public PackedTransaction<TokenWipeReceipt, TokenWipeNftTransaction> signForBatch(final AccountId payerId, final TransactionSigner signer, final Authority batchKey) {
        throw new UnsupportedOperationException("Not implemented yet: TokenWipeNftTransaction.signForBatch");
    }

    @Override
    public String toString() {
        return "TokenWipeNftTransaction[maxTransactionFee=" + maxTransactionFee()
                + ", validDuration=" + validDuration()
                + ", memo=" + memo()
                + ", tokenId=" + tokenId()
                + ", accountId=" + accountId()
                + ", serials=" + serials()
                + "]";
    }
}
