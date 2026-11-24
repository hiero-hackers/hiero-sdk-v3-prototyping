package org.hiero.consensusnode.client;

import java.util.Objects;
import java.util.concurrent.CompletionStage;
import org.hiero.consensusnode.client.internal.PrivateKeySigner;
import org.hiero.keys.PrivateKey;
import org.hiero.keys.PublicKey;
import org.hiero.sdk.annotation.ThreadSafe;

/// Signs transactions for an account. Use `TransactionSigner.of(privateKey)` for a key held in memory, and implement it
/// to sign with a key that is not, for example in a cloud KMS, an HSM or a remote signing service; see "External
/// signing" for the contract.
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

    /// Creates a signer for the given private key held in memory. Its public key is derived from the private key; two
    /// signers of equal private keys are equal.
    public static TransactionSigner of(final PrivateKey privateKey) {
        return new PrivateKeySigner(privateKey);
    }
}
