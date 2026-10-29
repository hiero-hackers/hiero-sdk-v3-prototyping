package org.hiero.consensusnode.queries.accounts;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.Set;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.client.ClientFactory;
import org.hiero.consensusnode.client.ExecutionSettings;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.client.RetryPolicy;
import org.hiero.hedera.HbarUnit;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.ConsensusNode;
import org.hiero.ledger.ContractId;
import org.hiero.ledger.IpAddress;
import org.hiero.ledger.MirrorNode;
import org.hiero.ledger.Network;
import org.hiero.ledger.config.NetworkSetting;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `AccountBalanceQuery`, generated from the specs.
@DisplayName("AccountBalanceQuery")
class AccountBalanceQueryTest {

    @Test
    @DisplayName("creates an AccountBalanceQuery and returns the values")
    @Tag("testId:consensusnode.queries.accounts.AccountBalanceQuery/create")
    void shouldCreateAndReturnTheValues() {
        final @Nullable AccountId accountId = accountIdValue();
        final @Nullable ContractId contractId = (ContractId) null;
        final AccountBalanceQuery subject = new AccountBalanceQuery(accountId, contractId);
        assertValue(accountId, subject.accountId());
        assertValue(contractId, subject.contractId());
        assertNull(subject.retryPolicy());
        assertNull(subject.attemptTimeout());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:consensusnode.queries.accounts.AccountBalanceQuery/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final @Nullable AccountId accountId = accountIdValue();
        final @Nullable ContractId contractId = (ContractId) null;
        final AccountBalanceQuery subject = AccountBalanceQuery.builder()
                .accountId(accountId)
                .contractId(contractId)
                .build();
        assertValue(accountId, subject.accountId());
        assertValue(contractId, subject.contractId());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:consensusnode.queries.accounts.AccountBalanceQuery/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final AccountBalanceQuery subject = AccountBalanceQuery.builder()
                .accountId(accountIdValue())
                .build();
        assertValue(accountIdValue(), subject.accountId());
        assertNull(subject.contractId());
    }

    @Test
    @DisplayName("rejects accountId and contractId together (@@oneOf)")
    @Tag("testId:consensusnode.queries.accounts.AccountBalanceQuery/accountId-contractId/rejects-two")
    void shouldRejectTwoOfAccountIdOrContractId() {
        assertThrows(IllegalArgumentException.class, () -> new AccountBalanceQuery(accountIdValue(), contractIdValue()));
    }

    @Test
    @DisplayName("rejects none of accountId, contractId (@@oneOf)")
    @Tag("testId:consensusnode.queries.accounts.AccountBalanceQuery/accountId-contractId/rejects-none")
    void shouldRejectNoneOfAccountIdOrContractId() {
        assertThrows(IllegalArgumentException.class, () -> new AccountBalanceQuery((AccountId) null, (ContractId) null));
    }

    @Test
    @DisplayName("setRetryPolicy changes retryPolicy and returns the object")
    @Tag("testId:consensusnode.queries.accounts.AccountBalanceQuery/retryPolicy/set")
    void shouldSetRetryPolicy() {
        final AccountBalanceQuery subject = create();
        final @Nullable RetryPolicy value = retryPolicyOtherValue();
        assertSame(subject, subject.setRetryPolicy(value));
        assertValue(value, subject.retryPolicy());
    }

    @Test
    @DisplayName("setRetryPolicy accepts null")
    @Tag("testId:consensusnode.queries.accounts.AccountBalanceQuery/retryPolicy/set-null")
    void shouldSetRetryPolicyToNull() {
        final AccountBalanceQuery subject = create();
        subject.setRetryPolicy((RetryPolicy) null);
        assertNull(subject.retryPolicy());
    }

    @Test
    @DisplayName("setAttemptTimeout changes attemptTimeout and returns the object")
    @Tag("testId:consensusnode.queries.accounts.AccountBalanceQuery/attemptTimeout/set")
    void shouldSetAttemptTimeout() {
        final AccountBalanceQuery subject = create();
        final @Nullable Duration value = attemptTimeoutOtherValue();
        assertSame(subject, subject.setAttemptTimeout(value));
        assertValue(value, subject.attemptTimeout());
    }

    @Test
    @DisplayName("setAttemptTimeout accepts null")
    @Tag("testId:consensusnode.queries.accounts.AccountBalanceQuery/attemptTimeout/set-null")
    void shouldSetAttemptTimeoutToNull() {
        final AccountBalanceQuery subject = create();
        subject.setAttemptTimeout((Duration) null);
        assertNull(subject.attemptTimeout());
    }

    @Test
    @DisplayName("submit(client) can be called and returns a value")
    @Tag("testId:consensusnode.queries.accounts.AccountBalanceQuery/submit/call")
    void shouldCallSubmit() {
        assertNotNull(create().submit(clientValue()));
    }

    private static AccountId accountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static ContractId contractIdValue() {
        return new ContractId(0L, 0L, "", 1001L, null);
    }

    private static AccountBalanceQuery create() {
        return new AccountBalanceQuery(accountIdValue(), (ContractId) null);
    }

    private static RetryPolicy retryPolicyOtherValue() {
        return ExecutionSettings.createDefault().retryPolicy();
    }

    private static Duration attemptTimeoutOtherValue() {
        return Duration.ofSeconds(2L);
    }

    private static HieroClient<?> clientValue() {
        return ClientFactory.createClient(new NetworkSetting(new Network<>(new byte[] {(byte) 3}, "local", HbarUnit.HBAR), Set.of(new ConsensusNode(new IpAddress(new byte[] {(byte) 127, (byte) 0, (byte) 0, (byte) 1}), 35211, new AccountId(0L, 0L, "", 3L, null, null))), Set.of(new MirrorNode("http://127.0.0.1:38081"))), new Account(new AccountId(0L, 0L, "", 1001L, null, null), KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c")));
    }
}
