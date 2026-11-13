package org.hiero.consensusnode.client;

import java.util.Objects;
import java.util.concurrent.CompletionStage;
import org.hiero.keys.PublicKey;
import org.hiero.sdk.annotation.ThreadSafe;

/// Signs transactions on behalf of the operator. Implement it to sign with a key that is not held in memory,
/// for example in a cloud KMS, an HSM or a remote signing service; see "External signing" for the contract.
public abstract class TransactionSigner {

    private final PublicKey publicKey;

    /// Creates a new `TransactionSigner`.
    ///
    /// @throws NullPointerException if a required value is `null`
    protected TransactionSigner(final PublicKey publicKey) {
        Objects.requireNonNull(publicKey, "publicKey must not be null");
        this.publicKey = publicKey;
    }

    /// the public key that verifies every signature of this signer
    public PublicKey publicKey() {
        return publicKey;
    }

    /// Signs the TransactionBody addressed to body.node. Called once per target node, possibly concurrently for
    /// different nodes.
    @ThreadSafe
    public abstract CompletionStage<NodeSignature> signTransaction(final NodeBody body);
}
