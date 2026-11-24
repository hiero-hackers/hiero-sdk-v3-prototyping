package org.hiero.consensusnode.client.internal;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.hiero.consensusnode.client.NodeBody;
import org.hiero.consensusnode.client.NodeSignature;
import org.hiero.consensusnode.client.TransactionSigner;
import org.hiero.keys.PrivateKey;
import org.jspecify.annotations.Nullable;

/// The `TransactionSigner` of a private key held in memory, created by `TransactionSigner.of(privateKey)` (rule
/// private-key-signer): its public key is derived from the private key, it signs the body bytes with the private key,
/// and two signers of equal private keys are equal. Signing needs no I/O, so the signature is complete when it is
/// returned.
public final class PrivateKeySigner extends TransactionSigner {

    private final PrivateKey privateKey;

    /// Creates the signer of a private key.
    ///
    /// @param privateKey the key
    /// @throws NullPointerException if `privateKey` is `null`
    public PrivateKeySigner(final PrivateKey privateKey) {
        super(Objects.requireNonNull(privateKey, "privateKey must not be null").createPublicKey());
        this.privateKey = privateKey;
    }

    @Override
    public CompletionStage<NodeSignature> signTransaction(final NodeBody body) {
        Objects.requireNonNull(body, "body must not be null");
        return CompletableFuture.completedFuture(new NodeSignature(body.node(), publicKey(),
                privateKey.sign(body.bytes())));
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof PrivateKeySigner other && privateKey.equals(other.privateKey);
    }

    @Override
    public int hashCode() {
        return privateKey.hashCode();
    }

    /// The public key only: the private key must not show up in logs.
    @Override
    public String toString() {
        return "PrivateKeySigner[publicKey=" + publicKey() + "]";
    }
}
