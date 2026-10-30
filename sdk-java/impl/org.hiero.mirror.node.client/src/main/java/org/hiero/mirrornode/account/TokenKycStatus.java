package org.hiero.mirrornode.account;


/// Whether KYC of an account is granted for a token.
public enum TokenKycStatus {

    /// the token has no KYC key
    NOT_APPLICABLE,
    GRANTED,
    REVOKED;
}
