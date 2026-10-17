package org.hiero.consensusnode.transactions.accounts;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.client.ClientFactory;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.client.TransactionSigner;
import org.hiero.hedera.Hbar;
import org.hiero.hedera.HbarUnit;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.ConsensusNode;
import org.hiero.ledger.IpAddress;
import org.hiero.ledger.MirrorNode;
import org.hiero.ledger.Network;
import org.hiero.ledger.config.NetworkSetting;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `AccountCreateTransaction`, generated from the specs.
@DisplayName("AccountCreateTransaction")
class AccountCreateTransactionTest {

    @Test
    @DisplayName("creates an AccountCreateTransaction and returns the values")
    @Tag("testId:consensusnode.transactions.accounts.AccountCreateTransaction/create")
    void shouldCreateAndReturnTheValues() {
        final Authority authority = authorityValue();
        final @Nullable NativeToken<?, ?> initialBalance = initialBalanceValue();
        final @Nullable String accountMemo = accountMemoValue();
        final boolean receiverSignatureRequired = receiverSignatureRequiredValue();
        final @Nullable Integer maxAutomaticTokenAssociations = maxAutomaticTokenAssociationsValue();
        final @Nullable Duration autoRenewPeriod = autoRenewPeriodValue();
        final @Nullable AccountId stakedAccountId = stakedAccountIdValue();
        final @Nullable Long stakedNodeId = (Long) null;
        final boolean declineStakingReward = declineStakingRewardValue();
        final byte @Nullable [] alias = aliasValue();
        final AccountCreateTransaction subject = new AccountCreateTransaction(authority, initialBalance, accountMemo, receiverSignatureRequired, maxAutomaticTokenAssociations, autoRenewPeriod, stakedAccountId, stakedNodeId, declineStakingReward, alias);
        assertValue(authority, subject.authority());
        assertValue(initialBalance, subject.initialBalance());
        assertValue(accountMemo, subject.accountMemo());
        assertValue(receiverSignatureRequired, subject.receiverSignatureRequired());
        assertValue(maxAutomaticTokenAssociations, subject.maxAutomaticTokenAssociations());
        assertValue(autoRenewPeriod, subject.autoRenewPeriod());
        assertValue(stakedAccountId, subject.stakedAccountId());
        assertValue(stakedNodeId, subject.stakedNodeId());
        assertValue(declineStakingReward, subject.declineStakingReward());
        assertValue(alias, subject.alias());
        assertNull(subject.maxTransactionFee());
        assertNull(subject.validDuration());
        assertNull(subject.memo());
    }

    @Test
    @DisplayName("creates an AccountCreateTransaction with the default values")
    @Tag("testId:consensusnode.transactions.accounts.AccountCreateTransaction/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final AccountCreateTransaction subject = new AccountCreateTransaction(authorityValue(), initialBalanceValue(), accountMemoValue(), maxAutomaticTokenAssociationsValue(), autoRenewPeriodValue(), stakedAccountIdValue(), (Long) null, aliasValue());
        assertValue(false, subject.receiverSignatureRequired());
        assertValue(false, subject.declineStakingReward());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:consensusnode.transactions.accounts.AccountCreateTransaction/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final Authority authority = authorityValue();
        final @Nullable NativeToken<?, ?> initialBalance = initialBalanceValue();
        final @Nullable String accountMemo = accountMemoValue();
        final boolean receiverSignatureRequired = receiverSignatureRequiredValue();
        final @Nullable Integer maxAutomaticTokenAssociations = maxAutomaticTokenAssociationsValue();
        final @Nullable Duration autoRenewPeriod = autoRenewPeriodValue();
        final @Nullable AccountId stakedAccountId = stakedAccountIdValue();
        final @Nullable Long stakedNodeId = (Long) null;
        final boolean declineStakingReward = declineStakingRewardValue();
        final byte @Nullable [] alias = aliasValue();
        final AccountCreateTransaction subject = AccountCreateTransaction.builder()
                .authority(authority)
                .initialBalance(initialBalance)
                .accountMemo(accountMemo)
                .receiverSignatureRequired(receiverSignatureRequired)
                .maxAutomaticTokenAssociations(maxAutomaticTokenAssociations)
                .autoRenewPeriod(autoRenewPeriod)
                .stakedAccountId(stakedAccountId)
                .stakedNodeId(stakedNodeId)
                .declineStakingReward(declineStakingReward)
                .alias(alias)
                .build();
        assertValue(authority, subject.authority());
        assertValue(initialBalance, subject.initialBalance());
        assertValue(accountMemo, subject.accountMemo());
        assertValue(receiverSignatureRequired, subject.receiverSignatureRequired());
        assertValue(maxAutomaticTokenAssociations, subject.maxAutomaticTokenAssociations());
        assertValue(autoRenewPeriod, subject.autoRenewPeriod());
        assertValue(stakedAccountId, subject.stakedAccountId());
        assertValue(stakedNodeId, subject.stakedNodeId());
        assertValue(declineStakingReward, subject.declineStakingReward());
        assertValue(alias, subject.alias());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:consensusnode.transactions.accounts.AccountCreateTransaction/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final AccountCreateTransaction subject = AccountCreateTransaction.builder()
                .authority(authorityValue())
                .stakedAccountId(stakedAccountIdValue())
                .build();
        assertNull(subject.initialBalance());
        assertNull(subject.accountMemo());
        assertValue(false, subject.receiverSignatureRequired());
        assertNull(subject.maxAutomaticTokenAssociations());
        assertNull(subject.autoRenewPeriod());
        assertValue(stakedAccountIdValue(), subject.stakedAccountId());
        assertNull(subject.stakedNodeId());
        assertValue(false, subject.declineStakingReward());
        assertNull(subject.alias());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:consensusnode.transactions.accounts.AccountCreateTransaction/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> AccountCreateTransaction.builder().build());
    }

    @Test
    @DisplayName("rejects null for authority")
    @Tag("testId:consensusnode.transactions.accounts.AccountCreateTransaction/authority/rejects-null")
    void shouldRejectNullAuthority() {
        assertThrows(NullPointerException.class, () -> new AccountCreateTransaction((Authority) null, initialBalanceValue(), accountMemoValue(), receiverSignatureRequiredValue(), maxAutomaticTokenAssociationsValue(), autoRenewPeriodValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue(), aliasValue()));
    }

    @Test
    @DisplayName("accountMemo: accepts more elements than the maximum")
    @Tag("testId:consensusnode.transactions.accounts.AccountCreateTransaction/accountMemo/accepts-at-maximum-bytes")
    void shouldAcceptAccountMemoAtMaximumBytes() {
        final @Nullable String value = "ääääääääääääääääääääääääääääääääääääääääääääääääää";
        final AccountCreateTransaction subject = new AccountCreateTransaction(authorityValue(), initialBalanceValue(), value, receiverSignatureRequiredValue(), maxAutomaticTokenAssociationsValue(), autoRenewPeriodValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue(), aliasValue());
        assertValue(value, subject.accountMemo());
    }

    @Test
    @DisplayName("accountMemo: rejects more elements than the maximum")
    @Tag("testId:consensusnode.transactions.accounts.AccountCreateTransaction/accountMemo/rejects-above-maximum-bytes")
    void shouldRejectAccountMemoAboveMaximumBytes() {
        assertThrows(IllegalArgumentException.class, () -> new AccountCreateTransaction(authorityValue(), initialBalanceValue(), "ääääääääääääääääääääääääääääääääääääääääääääääääääa", receiverSignatureRequiredValue(), maxAutomaticTokenAssociationsValue(), autoRenewPeriodValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue(), aliasValue()));
    }

    @Test
    @DisplayName("maxAutomaticTokenAssociations: accepts the minimum (-2147483648)")
    @Tag("testId:consensusnode.transactions.accounts.AccountCreateTransaction/maxAutomaticTokenAssociations/accepts-at-minimum")
    void shouldAcceptMaxAutomaticTokenAssociationsAtMinimum() {
        final @Nullable Integer value = Integer.MIN_VALUE;
        final AccountCreateTransaction subject = new AccountCreateTransaction(authorityValue(), initialBalanceValue(), accountMemoValue(), receiverSignatureRequiredValue(), value, autoRenewPeriodValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue(), aliasValue());
        assertValue(value, subject.maxAutomaticTokenAssociations());
    }

    @Test
    @DisplayName("maxAutomaticTokenAssociations: accepts the maximum (2147483647)")
    @Tag("testId:consensusnode.transactions.accounts.AccountCreateTransaction/maxAutomaticTokenAssociations/accepts-at-maximum")
    void shouldAcceptMaxAutomaticTokenAssociationsAtMaximum() {
        final @Nullable Integer value = 2147483647;
        final AccountCreateTransaction subject = new AccountCreateTransaction(authorityValue(), initialBalanceValue(), accountMemoValue(), receiverSignatureRequiredValue(), value, autoRenewPeriodValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue(), aliasValue());
        assertValue(value, subject.maxAutomaticTokenAssociations());
    }

    @Test
    @DisplayName("stakedNodeId: accepts the minimum (-9223372036854775808)")
    @Tag("testId:consensusnode.transactions.accounts.AccountCreateTransaction/stakedNodeId/accepts-at-minimum")
    void shouldAcceptStakedNodeIdAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final AccountCreateTransaction subject = new AccountCreateTransaction(authorityValue(), initialBalanceValue(), accountMemoValue(), receiverSignatureRequiredValue(), maxAutomaticTokenAssociationsValue(), autoRenewPeriodValue(), (AccountId) null, value, declineStakingRewardValue(), aliasValue());
        assertValue(value, subject.stakedNodeId());
    }

    @Test
    @DisplayName("stakedNodeId: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.transactions.accounts.AccountCreateTransaction/stakedNodeId/accepts-at-maximum")
    void shouldAcceptStakedNodeIdAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final AccountCreateTransaction subject = new AccountCreateTransaction(authorityValue(), initialBalanceValue(), accountMemoValue(), receiverSignatureRequiredValue(), maxAutomaticTokenAssociationsValue(), autoRenewPeriodValue(), (AccountId) null, value, declineStakingRewardValue(), aliasValue());
        assertValue(value, subject.stakedNodeId());
    }

    @Test
    @DisplayName("the constructor copies alias; the accessor returns a copy")
    @Tag("testId:consensusnode.transactions.accounts.AccountCreateTransaction/alias/copies-in")
    void shouldCopyAliasIn() {
        final byte[] value = aliasValue();
        final byte[] expected = value.clone();
        final AccountCreateTransaction subject = new AccountCreateTransaction(authorityValue(), initialBalanceValue(), accountMemoValue(), receiverSignatureRequiredValue(), maxAutomaticTokenAssociationsValue(), autoRenewPeriodValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue(), value);
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.alias());
        final byte[] returned = subject.alias();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.alias());
    }

    @Test
    @DisplayName("rejects stakedAccountId and stakedNodeId together (@@oneOrNoneOf)")
    @Tag("testId:consensusnode.transactions.accounts.AccountCreateTransaction/stakedAccountId-stakedNodeId/rejects-two")
    void shouldRejectTwoOfStakedAccountIdOrStakedNodeId() {
        assertThrows(IllegalArgumentException.class, () -> new AccountCreateTransaction(authorityValue(), initialBalanceValue(), accountMemoValue(), receiverSignatureRequiredValue(), maxAutomaticTokenAssociationsValue(), autoRenewPeriodValue(), stakedAccountIdValue(), stakedNodeIdValue(), declineStakingRewardValue(), aliasValue()));
    }

    @Test
    @DisplayName("setMaxTransactionFee changes maxTransactionFee and returns the object")
    @Tag("testId:consensusnode.transactions.accounts.AccountCreateTransaction/maxTransactionFee/set")
    void shouldSetMaxTransactionFee() {
        final AccountCreateTransaction subject = create();
        final @Nullable NativeToken<?, ?> value = maxTransactionFeeOtherValue();
        assertSame(subject, subject.setMaxTransactionFee(value));
        assertValue(value, subject.maxTransactionFee());
    }

    @Test
    @DisplayName("setMaxTransactionFee accepts null")
    @Tag("testId:consensusnode.transactions.accounts.AccountCreateTransaction/maxTransactionFee/set-null")
    void shouldSetMaxTransactionFeeToNull() {
        final AccountCreateTransaction subject = create();
        subject.setMaxTransactionFee((NativeToken<?, ?>) null);
        assertNull(subject.maxTransactionFee());
    }

    @Test
    @DisplayName("setValidDuration changes validDuration and returns the object")
    @Tag("testId:consensusnode.transactions.accounts.AccountCreateTransaction/validDuration/set")
    void shouldSetValidDuration() {
        final AccountCreateTransaction subject = create();
        final @Nullable Duration value = validDurationOtherValue();
        assertSame(subject, subject.setValidDuration(value));
        assertValue(value, subject.validDuration());
    }

    @Test
    @DisplayName("setValidDuration accepts null")
    @Tag("testId:consensusnode.transactions.accounts.AccountCreateTransaction/validDuration/set-null")
    void shouldSetValidDurationToNull() {
        final AccountCreateTransaction subject = create();
        subject.setValidDuration((Duration) null);
        assertNull(subject.validDuration());
    }

    @Test
    @DisplayName("setMemo changes memo and returns the object")
    @Tag("testId:consensusnode.transactions.accounts.AccountCreateTransaction/memo/set")
    void shouldSetMemo() {
        final AccountCreateTransaction subject = create();
        final @Nullable String value = memoOtherValue();
        assertSame(subject, subject.setMemo(value));
        assertValue(value, subject.memo());
    }

    @Test
    @DisplayName("setMemo accepts null")
    @Tag("testId:consensusnode.transactions.accounts.AccountCreateTransaction/memo/set-null")
    void shouldSetMemoToNull() {
        final AccountCreateTransaction subject = create();
        subject.setMemo((String) null);
        assertNull(subject.memo());
    }

    @Test
    @DisplayName("setMemo accepts more elements than the maximum")
    @Tag("testId:consensusnode.transactions.accounts.AccountCreateTransaction/memo/set-accepts-at-maximum-bytes")
    void shouldSetMemoAtMaximumBytes() {
        final AccountCreateTransaction subject = create();
        final @Nullable String value = "ääääääääääääääääääääääääääääääääääääääääääääääääää";
        subject.setMemo(value);
        assertValue(value, subject.memo());
    }

    @Test
    @DisplayName("setMemo rejects more elements than the maximum and keeps the value")
    @Tag("testId:consensusnode.transactions.accounts.AccountCreateTransaction/memo/set-rejects-above-maximum-bytes")
    void shouldNotSetMemoAboveMaximumBytes() {
        final AccountCreateTransaction subject = create();
        assertThrows(IllegalArgumentException.class, () -> subject.setMemo("ääääääääääääääääääääääääääääääääääääääääääääääääääa"));
        assertValue(null, subject.memo());
    }

    @Test
    @DisplayName("pack(payer, nodes) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.accounts.AccountCreateTransaction/pack/call")
    void shouldCallPack() {
        assertNotNull(create().pack(payerValue(), nodesValue()));
    }

    @Test
    @DisplayName("signWithOperator(client) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.accounts.AccountCreateTransaction/signWithOperator/call")
    void shouldCallSignWithOperator() {
        assertNotNull(create().signWithOperator(clientValue()));
    }

    @Test
    @DisplayName("sign(payer, nodes) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.accounts.AccountCreateTransaction/sign/call")
    void shouldCallSign() {
        assertNotNull(create().sign(payerValue(), nodesValue()));
    }

    @Test
    @DisplayName("sign(payerId, signer, nodes) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.accounts.AccountCreateTransaction/sign/call-2")
    void shouldCallSign2() {
        assertNotNull(create().sign(payerIdValue(), signerValue(), nodesValue()));
    }

    @Test
    @DisplayName("signWithOperatorAndSubmit(client) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.accounts.AccountCreateTransaction/signWithOperatorAndSubmit/call")
    void shouldCallSignWithOperatorAndSubmit() {
        assertNotNull(create().signWithOperatorAndSubmit(clientValue()));
    }

    @Test
    @DisplayName("packForBatch(payer, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.accounts.AccountCreateTransaction/packForBatch/call")
    void shouldCallPackForBatch() {
        assertNotNull(create().packForBatch(payerValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatchWithOperator(client, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.accounts.AccountCreateTransaction/signForBatchWithOperator/call")
    void shouldCallSignForBatchWithOperator() {
        assertNotNull(create().signForBatchWithOperator(clientValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatch(payer, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.accounts.AccountCreateTransaction/signForBatch/call")
    void shouldCallSignForBatch() {
        assertNotNull(create().signForBatch(payerValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatch(payerId, signer, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.accounts.AccountCreateTransaction/signForBatch/call-2")
    void shouldCallSignForBatch2() {
        assertNotNull(create().signForBatch(payerIdValue(), signerValue(), batchKeyValue()));
    }

    private static Authority authorityValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static NativeToken<?, ?> initialBalanceValue() {
        return new Hbar(1L, HbarUnit.TINYBAR);
    }

    private static String accountMemoValue() {
        return "value";
    }

    private static boolean receiverSignatureRequiredValue() {
        return true;
    }

    private static int maxAutomaticTokenAssociationsValue() {
        return 1;
    }

    private static Duration autoRenewPeriodValue() {
        return Duration.ofSeconds(1L);
    }

    private static AccountId stakedAccountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static long stakedNodeIdValue() {
        return 1L;
    }

    private static boolean declineStakingRewardValue() {
        return true;
    }

    private static byte[] aliasValue() {
        return new byte[] {1, 2, 3};
    }

    private static AccountCreateTransaction create() {
        return new AccountCreateTransaction(authorityValue(), initialBalanceValue(), accountMemoValue(), receiverSignatureRequiredValue(), maxAutomaticTokenAssociationsValue(), autoRenewPeriodValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue(), aliasValue());
    }

    private static NativeToken<?, ?> maxTransactionFeeOtherValue() {
        return new Hbar(2L, HbarUnit.TINYBAR);
    }

    private static Duration validDurationOtherValue() {
        return Duration.ofSeconds(2L);
    }

    private static String memoOtherValue() {
        return "value1";
    }

    private static Account payerValue() {
        return new Account(new AccountId(0L, 0L, "", 1001L, null, null), KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c"));
    }

    private static List<AccountId> nodesValue() {
        return List.of(new AccountId(0L, 0L, "", 1001L, null, null));
    }

    private static HieroClient<?> clientValue() {
        return ClientFactory.createClient(new NetworkSetting(new Network<>(new byte[] {(byte) 3}, "local", HbarUnit.HBAR), Set.of(new ConsensusNode(new IpAddress(new byte[] {(byte) 127, (byte) 0, (byte) 0, (byte) 1}), 35211, new AccountId(0L, 0L, "", 3L, null, null))), Set.of(new MirrorNode("http://127.0.0.1:38081"))), new Account(new AccountId(0L, 0L, "", 1001L, null, null), KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c")));
    }

    private static AccountId payerIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static TransactionSigner signerValue() {
        return ClientFactory.createClient(new NetworkSetting(new Network<>(new byte[] {(byte) 3}, "local", HbarUnit.HBAR), Set.of(new ConsensusNode(new IpAddress(new byte[] {(byte) 127, (byte) 0, (byte) 0, (byte) 1}), 35211, new AccountId(0L, 0L, "", 3L, null, null))), Set.of(new MirrorNode("http://127.0.0.1:38081"))), new Account(new AccountId(0L, 0L, "", 1001L, null, null), KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c"))).transactionSigner();
    }

    private static Authority batchKeyValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }
}
