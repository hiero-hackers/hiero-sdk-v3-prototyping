package org.hiero.mirrornode.contract;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.hiero.ledger.EvmAddress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `AuthorizationListEntry`, generated from the specs.
@DisplayName("AuthorizationListEntry")
class AuthorizationListEntryTest {

    @Test
    @DisplayName("creates an AuthorizationListEntry and returns the values")
    @Tag("testId:mirrornode.contract.AuthorizationListEntry/create")
    void shouldCreateAndReturnTheValues() {
        final EvmAddress address = addressValue();
        final String chainId = chainIdValue();
        final long nonce = nonceValue();
        final String r = rValue();
        final String s = sValue();
        final String yParity = yParityValue();
        final AuthorizationListEntry subject = new AuthorizationListEntry(address, chainId, nonce, r, s, yParity);
        assertValue(address, subject.address());
        assertValue(chainId, subject.chainId());
        assertValue(nonce, subject.nonce());
        assertValue(r, subject.r());
        assertValue(s, subject.s());
        assertValue(yParity, subject.yParity());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.contract.AuthorizationListEntry/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final EvmAddress address = addressValue();
        final String chainId = chainIdValue();
        final long nonce = nonceValue();
        final String r = rValue();
        final String s = sValue();
        final String yParity = yParityValue();
        final AuthorizationListEntry subject = AuthorizationListEntry.builder()
                .address(address)
                .chainId(chainId)
                .nonce(nonce)
                .r(r)
                .s(s)
                .yParity(yParity)
                .build();
        assertValue(address, subject.address());
        assertValue(chainId, subject.chainId());
        assertValue(nonce, subject.nonce());
        assertValue(r, subject.r());
        assertValue(s, subject.s());
        assertValue(yParity, subject.yParity());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.contract.AuthorizationListEntry/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final AuthorizationListEntry subject = AuthorizationListEntry.builder()
                .address(addressValue())
                .chainId(chainIdValue())
                .nonce(nonceValue())
                .r(rValue())
                .s(sValue())
                .yParity(yParityValue())
                .build();
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.contract.AuthorizationListEntry/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> AuthorizationListEntry.builder().build());
    }

    @Test
    @DisplayName("rejects null for address")
    @Tag("testId:mirrornode.contract.AuthorizationListEntry/address/rejects-null")
    void shouldRejectNullAddress() {
        assertThrows(NullPointerException.class, () -> new AuthorizationListEntry((EvmAddress) null, chainIdValue(), nonceValue(), rValue(), sValue(), yParityValue()));
    }

    @Test
    @DisplayName("rejects null for chainId")
    @Tag("testId:mirrornode.contract.AuthorizationListEntry/chainId/rejects-null")
    void shouldRejectNullChainId() {
        assertThrows(NullPointerException.class, () -> new AuthorizationListEntry(addressValue(), (String) null, nonceValue(), rValue(), sValue(), yParityValue()));
    }

    @Test
    @DisplayName("nonce: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.contract.AuthorizationListEntry/nonce/accepts-at-minimum")
    void shouldAcceptNonceAtMinimum() {
        final long value = -9223372036854775808L;
        final AuthorizationListEntry subject = new AuthorizationListEntry(addressValue(), chainIdValue(), value, rValue(), sValue(), yParityValue());
        assertValue(value, subject.nonce());
    }

    @Test
    @DisplayName("nonce: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.contract.AuthorizationListEntry/nonce/accepts-at-maximum")
    void shouldAcceptNonceAtMaximum() {
        final long value = 9223372036854775807L;
        final AuthorizationListEntry subject = new AuthorizationListEntry(addressValue(), chainIdValue(), value, rValue(), sValue(), yParityValue());
        assertValue(value, subject.nonce());
    }

    @Test
    @DisplayName("rejects null for r")
    @Tag("testId:mirrornode.contract.AuthorizationListEntry/r/rejects-null")
    void shouldRejectNullR() {
        assertThrows(NullPointerException.class, () -> new AuthorizationListEntry(addressValue(), chainIdValue(), nonceValue(), (String) null, sValue(), yParityValue()));
    }

    @Test
    @DisplayName("rejects null for s")
    @Tag("testId:mirrornode.contract.AuthorizationListEntry/s/rejects-null")
    void shouldRejectNullS() {
        assertThrows(NullPointerException.class, () -> new AuthorizationListEntry(addressValue(), chainIdValue(), nonceValue(), rValue(), (String) null, yParityValue()));
    }

    @Test
    @DisplayName("rejects null for yParity")
    @Tag("testId:mirrornode.contract.AuthorizationListEntry/yParity/rejects-null")
    void shouldRejectNullYParity() {
        assertThrows(NullPointerException.class, () -> new AuthorizationListEntry(addressValue(), chainIdValue(), nonceValue(), rValue(), sValue(), (String) null));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.contract.AuthorizationListEntry/value-semantics")
    void shouldCompareByValue() {
        final AuthorizationListEntry subject = create();
        final AuthorizationListEntry other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static EvmAddress addressValue() {
        return new EvmAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
    }

    private static String chainIdValue() {
        return "value";
    }

    private static long nonceValue() {
        return 1L;
    }

    private static String rValue() {
        return "value";
    }

    private static String sValue() {
        return "value";
    }

    private static String yParityValue() {
        return "value";
    }

    private static AuthorizationListEntry create() {
        return new AuthorizationListEntry(addressValue(), chainIdValue(), nonceValue(), rValue(), sValue(), yParityValue());
    }
}
