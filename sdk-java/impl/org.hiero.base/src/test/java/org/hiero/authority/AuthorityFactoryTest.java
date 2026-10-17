package org.hiero.authority;

import static org.junit.jupiter.api.Assertions.*;

import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.keys.PublicKey;
import org.hiero.ledger.ContractId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `AuthorityFactory`, generated from the specs.
@DisplayName("AuthorityFactory")
class AuthorityFactoryTest {

    @Test
    @DisplayName("of(children) can be called and returns a value")
    @Tag("testId:authority/of/call")
    void shouldCallOf() {
        assertNotNull(AuthorityFactory.of(childrenValue()));
    }

    @Test
    @DisplayName("of(threshold, children) can be called and returns a value")
    @Tag("testId:authority/of/call-2")
    void shouldCallOf2() {
        assertNotNull(AuthorityFactory.of(thresholdValue(), childrenValue()));
    }

    @Test
    @DisplayName("of: threshold accepts the minimum (1)")
    @Tag("testId:authority/of/threshold/accepts-at-minimum")
    void shouldAcceptThresholdAtMinimumInOf() {
        assertNotNull(AuthorityFactory.of(1, childrenValue()));
    }

    @Test
    @DisplayName("of: threshold accepts the maximum (2147483647)")
    @Tag("testId:authority/of/threshold/accepts-at-maximum")
    void shouldAcceptThresholdAtMaximumInOf() {
        assertNotNull(AuthorityFactory.of(2147483647, childrenValue()));
    }

    @Test
    @DisplayName("of: threshold rejects a value below the minimum (0)")
    @Tag("testId:authority/of/threshold/rejects-below-minimum")
    void shouldRejectThresholdBelowMinimumInOf() {
        assertThrows(IllegalArgumentException.class, () -> AuthorityFactory.of(0, childrenValue()));
    }

    @Test
    @DisplayName("of(publicKey) can be called and returns a value")
    @Tag("testId:authority/of/call-3")
    void shouldCallOf3() {
        assertNotNull(AuthorityFactory.of(publicKeyValue()));
    }

    @Test
    @DisplayName("of rejects null for publicKey")
    @Tag("testId:authority/of/publicKey/rejects-null")
    void shouldRejectNullPublicKeyInOf() {
        assertThrows(NullPointerException.class, () -> AuthorityFactory.of((PublicKey) null));
    }

    @Test
    @DisplayName("ofContract(contractId) can be called and returns a value")
    @Tag("testId:authority/ofContract/call")
    void shouldCallOfContract() {
        assertNotNull(AuthorityFactory.ofContract(contractIdValue()));
    }

    @Test
    @DisplayName("ofContract rejects null for contractId")
    @Tag("testId:authority/ofContract/contractId/rejects-null")
    void shouldRejectNullContractIdInOfContract() {
        assertThrows(NullPointerException.class, () -> AuthorityFactory.ofContract((ContractId) null));
    }

    @Test
    @DisplayName("ofDelegatable(contractId) can be called and returns a value")
    @Tag("testId:authority/ofDelegatable/call")
    void shouldCallOfDelegatable() {
        assertNotNull(AuthorityFactory.ofDelegatable(contractIdValue()));
    }

    @Test
    @DisplayName("ofDelegatable rejects null for contractId")
    @Tag("testId:authority/ofDelegatable/contractId/rejects-null")
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
