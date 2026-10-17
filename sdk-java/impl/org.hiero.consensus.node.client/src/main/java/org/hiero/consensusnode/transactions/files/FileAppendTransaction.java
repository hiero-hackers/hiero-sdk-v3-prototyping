package org.hiero.consensusnode.transactions.files;

import org.hiero.consensusnode.client.internal.FileKinds;
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

/// Appends `contents` to the end of the file. The SDK transparently splits this into multiple
/// consensus transactions when `contents` exceeds the single-transaction body limit.
public final class FileAppendTransaction extends Transaction<FileAppendReceipt, FileAppendTransaction> {

    private final Address fileId;
    private final byte[] contents;

    /// Creates a new `FileAppendTransaction`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public FileAppendTransaction(final Address fileId, final byte[] contents) {
        super();
        Objects.requireNonNull(fileId, "fileId must not be null");
        this.fileId = fileId;
        Objects.requireNonNull(contents, "contents must not be null");
        this.contents = contents.clone();
    }

    /// Returns the `fileId`.
    public Address fileId() {
        return fileId;
    }

    /// bytes to append; large payloads are chunked by the SDK
    public byte[] contents() {
        return contents.clone();
    }

    @Override
    public PackedTransaction<FileAppendReceipt, FileAppendTransaction> pack(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(FileKinds.FILE_APPEND).pack(this, payer, nodes);
    }

    @Override
    public PackedTransaction<FileAppendReceipt, FileAppendTransaction> signWithOperator(final HieroClient<?> client) {
        return new Packing<>(FileKinds.FILE_APPEND).signWithOperator(this, client);
    }

    @Override
    public PackedTransaction<FileAppendReceipt, FileAppendTransaction> sign(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(FileKinds.FILE_APPEND).sign(this, payer, nodes);
    }

    @Override
    public PackedTransaction<FileAppendReceipt, FileAppendTransaction> sign(final AccountId payerId, final TransactionSigner signer, final List<AccountId> nodes) {
        return new Packing<>(FileKinds.FILE_APPEND).sign(this, payerId, signer, nodes);
    }

    @Override
    public CompletionStage<Response<FileAppendReceipt>> signWithOperatorAndSubmit(final HieroClient<?> client) {
        return new Packing<>(FileKinds.FILE_APPEND).signWithOperatorAndSubmit(this, client);
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
    public PackedTransaction<FileAppendReceipt, FileAppendTransaction> packForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(FileKinds.FILE_APPEND).packForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with the client's operator as payer and adds the operator's
    /// signature. The batch counterpart of signWithOperator(client).
    @Override
    public PackedTransaction<FileAppendReceipt, FileAppendTransaction> signForBatchWithOperator(final HieroClient<?> client, final Authority batchKey) {
        return new Packing<>(FileKinds.FILE_APPEND).signForBatchWithOperator(this, client, batchKey);
    }

    /// Packs this transaction for a batch with the given Account as payer and adds its signature. The
    /// batch counterpart of sign(payer, nodes).
    @Override
    public PackedTransaction<FileAppendReceipt, FileAppendTransaction> signForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(FileKinds.FILE_APPEND).signForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with payerId as payer and signs it with the given
    /// TransactionSigner (HSM, hardware wallet, paymaster). The batch counterpart of
    /// sign(payerId, signer, nodes).
    @Override
    public PackedTransaction<FileAppendReceipt, FileAppendTransaction> signForBatch(final AccountId payerId, final TransactionSigner signer, final Authority batchKey) {
        return new Packing<>(FileKinds.FILE_APPEND).signForBatch(this, payerId, signer, batchKey);
    }

    @Override
    public String toString() {
        return "FileAppendTransaction[maxTransactionFee=" + maxTransactionFee()
                + ", validDuration=" + validDuration()
                + ", memo=" + memo()
                + ", fileId=" + fileId()
                + ", contents=" + "byte[" + contents().length + "]"
                + "]";
    }
}
