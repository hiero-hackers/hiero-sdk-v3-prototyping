package org.hiero.consensusnode.client.internal;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.hiero.consensusnode.client.NodeBody;
import org.hiero.consensusnode.client.NodeSignature;
import org.hiero.consensusnode.client.TransactionSigner;
import org.hiero.keys.PrivateKey;

/// The `TransactionSigner` of a private key held in memory: the default signer of a client, which signs with the
/// private key of the operator account. Signing needs no I/O, so the signature is complete when it is returned.
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
}
