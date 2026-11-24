package org.hiero.consensusnode.client;

import java.util.Objects;
import org.hiero.keys.PrivateKey;
import org.hiero.ledger.AccountId;

/// An account that pays for and signs requests: an account id together with the signer that signs for it. An account
/// whose authority needs several keys is signed for by several `Account`s with the same id and different signers.
///
/// @param accountId the id of the account
/// @param signer signs for the account
public record Account(AccountId accountId, TransactionSigner signer) {

    /// Creates a new `Account`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public Account {
        Objects.requireNonNull(accountId, "accountId must not be null");
        Objects.requireNonNull(signer, "signer must not be null");
    }

    /// Creates an account that signs with the given private key held in memory; the same as
    /// `Account{accountId, TransactionSigner.of(privateKey)}`.
    public static Account of(final AccountId accountId, final PrivateKey privateKey) {
        return new Account(accountId, TransactionSigner.of(privateKey));
    }
}
