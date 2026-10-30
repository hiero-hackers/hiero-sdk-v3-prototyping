package org.hiero.mirrornode.schedule;


/// The kind of key a signature was made with.
public enum SignatureType {

    CONTRACT,
    ED25519,
    RSA_3072,
    ECDSA_384,
    ECDSA_SECP256K1,
    UNKNOWN;
}
