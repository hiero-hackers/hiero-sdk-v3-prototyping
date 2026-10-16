package org.hiero.authority;

import static org.junit.jupiter.api.Assertions.*;

import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.keys.PublicKey;
import org.hiero.ledger.ContractId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of `AuthorityFactory`, generated from the specs.
@DisplayName("AuthorityFactory")
class AuthorityFactoryTest {

    @Test
    @DisplayName("of(children) can be called and returns a value")
    void shouldCallOf() {
        assertNotNull(AuthorityFactory.of(childrenValue()));
    }

    @Test
    @DisplayName("of(threshold, children) can be called and returns a value")
    void shouldCallOf2() {
        assertNotNull(AuthorityFactory.of(thresholdValue(), childrenValue()));
    }

    @Test
    @DisplayName("of: threshold accepts the minimum (1)")
    void shouldAcceptThresholdAtMinimumInOf() {
        assertNotNull(AuthorityFactory.of(1, childrenValue()));
    }

    @Test
    @DisplayName("of: threshold accepts the maximum (2147483647)")
    void shouldAcceptThresholdAtMaximumInOf() {
        assertNotNull(AuthorityFactory.of(2147483647, childrenValue()));
    }

    @Test
    @DisplayName("of: threshold rejects a value below the minimum (0)")
    void shouldRejectThresholdBelowMinimumInOf() {
        assertThrows(IllegalArgumentException.class, () -> AuthorityFactory.of(0, childrenValue()));
    }

    @Test
    @DisplayName("of(publicKey) can be called and returns a value")
    void shouldCallOf3() {
        assertNotNull(AuthorityFactory.of(publicKeyValue()));
    }

    @Test
    @DisplayName("of rejects null for publicKey")
    void shouldRejectNullPublicKeyInOf() {
        assertThrows(NullPointerException.class, () -> AuthorityFactory.of((PublicKey) null));
    }

    @Test
    @DisplayName("ofContract(contractId) can be called and returns a value")
    void shouldCallOfContract() {
        assertNotNull(AuthorityFactory.ofContract(contractIdValue()));
    }

    @Test
    @DisplayName("ofContract rejects null for contractId")
    void shouldRejectNullContractIdInOfContract() {
        assertThrows(NullPointerException.class, () -> AuthorityFactory.ofContract((ContractId) null));
    }

    @Test
    @DisplayName("ofDelegatable(contractId) can be called and returns a value")
    void shouldCallOfDelegatable() {
        assertNotNull(AuthorityFactory.ofDelegatable(contractIdValue()));
    }

    @Test
    @DisplayName("ofDelegatable rejects null for contractId")
    void shouldRejectNullContractIdInOfDelegatable() {
        assertThrows(NullPointerException.class, () -> AuthorityFactory.ofDelegatable((ContractId) null));
    }

    private static Authority childrenValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static int thresholdValue() {
        return 1;
    }

    private static PublicKey publicKeyValue() {
        return KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey();
    }

    private static ContractId contractIdValue() {
        return new ContractId(0L, 0L, "", 1001L, null);
    }
}
