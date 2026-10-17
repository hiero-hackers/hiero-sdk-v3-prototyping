package org.hiero.consensusnode.transactions.accounts;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `NftAllowance`, generated from the specs.
@DisplayName("NftAllowance")
class NftAllowanceTest {

    @Test
    @DisplayName("creates a NftAllowance and returns the values")
    @Tag("testId:consensusnode.transactions.accounts.NftAllowance/create")
    void shouldCreateAndReturnTheValues() {
        final Address tokenId = tokenIdValue();
        final AccountId ownerAccountId = ownerAccountIdValue();
        final AccountId spenderAccountId = spenderAccountIdValue();
        final NftGrant grant = grantValue();
        final @Nullable AccountId delegatingSpender = delegatingSpenderValue();
        final NftAllowance subject = new NftAllowance(tokenId, ownerAccountId, spenderAccountId, grant, delegatingSpender);
        assertValue(tokenId, subject.tokenId());
        assertValue(ownerAccountId, subject.ownerAccountId());
        assertValue(spenderAccountId, subject.spenderAccountId());
        assertValue(grant, subject.grant());
        assertValue(delegatingSpender, subject.delegatingSpender());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:consensusnode.transactions.accounts.NftAllowance/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final Address tokenId = tokenIdValue();
        final AccountId ownerAccountId = ownerAccountIdValue();
        final AccountId spenderAccountId = spenderAccountIdValue();
        final NftGrant grant = grantValue();
        final @Nullable AccountId delegatingSpender = delegatingSpenderValue();
        final NftAllowance subject = NftAllowance.builder()
                .tokenId(tokenId)
                .ownerAccountId(ownerAccountId)
                .spenderAccountId(spenderAccountId)
                .grant(grant)
                .delegatingSpender(delegatingSpender)
                .build();
        assertValue(tokenId, subject.tokenId());
        assertValue(ownerAccountId, subject.ownerAccountId());
        assertValue(spenderAccountId, subject.spenderAccountId());
        assertValue(grant, subject.grant());
        assertValue(delegatingSpender, subject.delegatingSpender());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:consensusnode.transactions.accounts.NftAllowance/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final NftAllowance subject = NftAllowance.builder()
                .tokenId(tokenIdValue())
                .ownerAccountId(ownerAccountIdValue())
                .spenderAccountId(spenderAccountIdValue())
                .grant(grantValue())
                .build();
        assertNull(subject.delegatingSpender());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:consensusnode.transactions.accounts.NftAllowance/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> NftAllowance.builder().build());
    }

    @Test
    @DisplayName("rejects null for tokenId")
    @Tag("testId:consensusnode.transactions.accounts.NftAllowance/tokenId/rejects-null")
    void shouldRejectNullTokenId() {
        assertThrows(NullPointerException.class, () -> new NftAllowance((Address) null, ownerAccountIdValue(), spenderAccountIdValue(), grantValue(), delegatingSpenderValue()));
    }

    @Test
    @DisplayName("rejects null for ownerAccountId")
    @Tag("testId:consensusnode.transactions.accounts.NftAllowance/ownerAccountId/rejects-null")
    void shouldRejectNullOwnerAccountId() {
        assertThrows(NullPointerException.class, () -> new NftAllowance(tokenIdValue(), (AccountId) null, spenderAccountIdValue(), grantValue(), delegatingSpenderValue()));
    }

    @Test
    @DisplayName("rejects null for spenderAccountId")
    @Tag("testId:consensusnode.transactions.accounts.NftAllowance/spenderAccountId/rejects-null")
    void shouldRejectNullSpenderAccountId() {
        assertThrows(NullPointerException.class, () -> new NftAllowance(tokenIdValue(), ownerAccountIdValue(), (AccountId) null, grantValue(), delegatingSpenderValue()));
    }

    @Test
    @DisplayName("rejects null for grant")
    @Tag("testId:consensusnode.transactions.accounts.NftAllowance/grant/rejects-null")
    void shouldRejectNullGrant() {
        assertThrows(NullPointerException.class, () -> new NftAllowance(tokenIdValue(), ownerAccountIdValue(), spenderAccountIdValue(), (NftGrant) null, delegatingSpenderValue()));
    }

    private static Address tokenIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static AccountId ownerAccountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static AccountId spenderAccountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static NftGrant grantValue() {
        return new NftCollectionGrant(true);
    }

    private static AccountId delegatingSpenderValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static NftAllowance create() {
        return new NftAllowance(tokenIdValue(), ownerAccountIdValue(), spenderAccountIdValue(), grantValue(), delegatingSpenderValue());
    }
}
