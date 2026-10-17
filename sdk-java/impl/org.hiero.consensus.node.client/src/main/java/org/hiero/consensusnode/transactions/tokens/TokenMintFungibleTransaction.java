package org.hiero.consensusnode.transactions.tokens;

import org.hiero.consensusnode.client.internal.TokenKinds;
import org.hiero.consensusnode.client.internal.Packing;
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

/// Mints units of a FUNGIBLE_COMMON token. The treasury account receives them.
public final class TokenMintFungibleTransaction extends TokenMintTransaction<TokenMintReceipt, TokenMintFungibleTransaction> {

    private final long amount;

    /// Creates a new `TokenMintFungibleTransaction`.
    ///
    /// @throws IllegalArgumentException if a value violates its constraints
    public TokenMintFungibleTransaction(final Address tokenId, final long amount) {
        super(tokenId);
        if (amount < 1L) {
            throw new IllegalArgumentException("amount must be at least 1");
        }
        this.amount = amount;
    }

    /// amount to mint, in the smallest indivisible unit
    public long amount() {
        return amount;
    }

    @Override
    public PackedTransaction<TokenMintReceipt, TokenMintFungibleTransaction> pack(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(TokenKinds.TOKEN_MINT_FUNGIBLE).pack(this, payer, nodes);
    }

    @Override
    public PackedTransaction<TokenMintReceipt, TokenMintFungibleTransaction> signWithOperator(final HieroClient<?> client) {
        return new Packing<>(TokenKinds.TOKEN_MINT_FUNGIBLE).signWithOperator(this, client);
    }

    @Override
    public PackedTransaction<TokenMintReceipt, TokenMintFungibleTransaction> sign(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(TokenKinds.TOKEN_MINT_FUNGIBLE).sign(this, payer, nodes);
    }

    @Override
    public PackedTransaction<TokenMintReceipt, TokenMintFungibleTransaction> sign(final AccountId payerId, final TransactionSigner signer, final List<AccountId> nodes) {
        return new Packing<>(TokenKinds.TOKEN_MINT_FUNGIBLE).sign(this, payerId, signer, nodes);
    }

    @Override
    public CompletionStage<Response<TokenMintReceipt>> signWithOperatorAndSubmit(final HieroClient<?> client) {
        return new Packing<>(TokenKinds.TOKEN_MINT_FUNGIBLE).signWithOperatorAndSubmit(this, client);
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
    public PackedTransaction<TokenMintReceipt, TokenMintFungibleTransaction> packForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(TokenKinds.TOKEN_MINT_FUNGIBLE).packForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with the client's operator as payer and adds the operator's
    /// signature. The batch counterpart of signWithOperator(client).
    @Override
    public PackedTransaction<TokenMintReceipt, TokenMintFungibleTransaction> signForBatchWithOperator(final HieroClient<?> client, final Authority batchKey) {
        return new Packing<>(TokenKinds.TOKEN_MINT_FUNGIBLE).signForBatchWithOperator(this, client, batchKey);
    }

    /// Packs this transaction for a batch with the given Account as payer and adds its signature. The
    /// batch counterpart of sign(payer, nodes).
    @Override
    public PackedTransaction<TokenMintReceipt, TokenMintFungibleTransaction> signForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(TokenKinds.TOKEN_MINT_FUNGIBLE).signForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with payerId as payer and signs it with the given
    /// TransactionSigner (HSM, hardware wallet, paymaster). The batch counterpart of
    /// sign(payerId, signer, nodes).
    @Override
    public PackedTransaction<TokenMintReceipt, TokenMintFungibleTransaction> signForBatch(final AccountId payerId, final TransactionSigner signer, final Authority batchKey) {
        return new Packing<>(TokenKinds.TOKEN_MINT_FUNGIBLE).signForBatch(this, payerId, signer, batchKey);
    }

    @Override
    public String toString() {
        return "TokenMintFungibleTransaction[maxTransactionFee=" + maxTransactionFee()
                + ", validDuration=" + validDuration()
                + ", memo=" + memo()
                + ", tokenId=" + tokenId()
                + ", amount=" + amount()
                + "]";
    }
}
