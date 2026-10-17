package org.hiero.consensusnode.transactions.accounts;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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
import org.junit.jupiter.api.Test;

/// Tests of `AccountUpdateTransaction`, generated from the specs.
@DisplayName("AccountUpdateTransaction")
class AccountUpdateTransactionTest {

    @Test
    @DisplayName("creates an AccountUpdateTransaction and returns the values")
    void shouldCreateAndReturnTheValues() {
        final AccountId accountId = accountIdValue();
        final @Nullable Authority authority = authorityValue();
        final @Nullable String accountMemo = accountMemoValue();
        final @Nullable Boolean receiverSignatureRequired = receiverSignatureRequiredValue();
        final @Nullable Integer maxAutomaticTokenAssociations = maxAutomaticTokenAssociationsValue();
        final @Nullable Duration autoRenewPeriod = autoRenewPeriodValue();
        final @Nullable ZonedDateTime expirationTime = expirationTimeValue();
        final @Nullable AccountId stakedAccountId = stakedAccountIdValue();
        final @Nullable Long stakedNodeId = (Long) null;
        final @Nullable Boolean declineStakingReward = declineStakingRewardValue();
        final AccountUpdateTransaction subject = new AccountUpdateTransaction(accountId, authority, accountMemo, receiverSignatureRequired, maxAutomaticTokenAssociations, autoRenewPeriod, expirationTime, stakedAccountId, stakedNodeId, declineStakingReward);
        assertValue(accountId, subject.accountId());
        assertValue(authority, subject.authority());
        assertValue(accountMemo, subject.accountMemo());
        assertValue(receiverSignatureRequired, subject.receiverSignatureRequired());
        assertValue(maxAutomaticTokenAssociations, subject.maxAutomaticTokenAssociations());
        assertValue(autoRenewPeriod, subject.autoRenewPeriod());
        assertValue(expirationTime, subject.expirationTime());
        assertValue(stakedAccountId, subject.stakedAccountId());
        assertValue(stakedNodeId, subject.stakedNodeId());
        assertValue(declineStakingReward, subject.declineStakingReward());
        assertNull(subject.maxTransactionFee());
        assertNull(subject.validDuration());
        assertNull(subject.memo());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    void shouldBuildTheValuesOfTheConstructor() {
        final AccountId accountId = accountIdValue();
        final @Nullable Authority authority = authorityValue();
        final @Nullable String accountMemo = accountMemoValue();
        final @Nullable Boolean receiverSignatureRequired = receiverSignatureRequiredValue();
        final @Nullable Integer maxAutomaticTokenAssociations = maxAutomaticTokenAssociationsValue();
        final @Nullable Duration autoRenewPeriod = autoRenewPeriodValue();
        final @Nullable ZonedDateTime expirationTime = expirationTimeValue();
        final @Nullable AccountId stakedAccountId = stakedAccountIdValue();
        final @Nullable Long stakedNodeId = (Long) null;
        final @Nullable Boolean declineStakingReward = declineStakingRewardValue();
        final AccountUpdateTransaction subject = AccountUpdateTransaction.builder()
                .accountId(accountId)
                .authority(authority)
                .accountMemo(accountMemo)
                .receiverSignatureRequired(receiverSignatureRequired)
                .maxAutomaticTokenAssociations(maxAutomaticTokenAssociations)
                .autoRenewPeriod(autoRenewPeriod)
                .expirationTime(expirationTime)
                .stakedAccountId(stakedAccountId)
                .stakedNodeId(stakedNodeId)
                .declineStakingReward(declineStakingReward)
                .build();
        assertValue(accountId, subject.accountId());
        assertValue(authority, subject.authority());
        assertValue(accountMemo, subject.accountMemo());
        assertValue(receiverSignatureRequired, subject.receiverSignatureRequired());
        assertValue(maxAutomaticTokenAssociations, subject.maxAutomaticTokenAssociations());
        assertValue(autoRenewPeriod, subject.autoRenewPeriod());
        assertValue(expirationTime, subject.expirationTime());
        assertValue(stakedAccountId, subject.stakedAccountId());
        assertValue(stakedNodeId, subject.stakedNodeId());
        assertValue(declineStakingReward, subject.declineStakingReward());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    void shouldBuildWithoutTheOptionalValues() {
        final AccountUpdateTransaction subject = AccountUpdateTransaction.builder()
                .accountId(accountIdValue())
                .stakedAccountId(stakedAccountIdValue())
                .build();
        assertNull(subject.authority());
        assertNull(subject.accountMemo());
        assertNull(subject.receiverSignatureRequired());
        assertNull(subject.maxAutomaticTokenAssociations());
        assertNull(subject.autoRenewPeriod());
        assertNull(subject.expirationTime());
        assertValue(stakedAccountIdValue(), subject.stakedAccountId());
        assertNull(subject.stakedNodeId());
        assertNull(subject.declineStakingReward());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> AccountUpdateTransaction.builder().build());
    }

    @Test
    @DisplayName("rejects null for accountId")
    void shouldRejectNullAccountId() {
        assertThrows(NullPointerException.class, () -> new AccountUpdateTransaction((AccountId) null, authorityValue(), accountMemoValue(), receiverSignatureRequiredValue(), maxAutomaticTokenAssociationsValue(), autoRenewPeriodValue(), expirationTimeValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue()));
    }

    @Test
    @DisplayName("accountMemo: accepts more elements than the maximum")
    void shouldAcceptAccountMemoAtMaximumBytes() {
        final @Nullable String value = "ääääääääääääääääääääääääääääääääääääääääääääääääää";
        final AccountUpdateTransaction subject = new AccountUpdateTransaction(accountIdValue(), authorityValue(), value, receiverSignatureRequiredValue(), maxAutomaticTokenAssociationsValue(), autoRenewPeriodValue(), expirationTimeValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue());
        assertValue(value, subject.accountMemo());
    }

    @Test
    @DisplayName("accountMemo: rejects more elements than the maximum")
    void shouldRejectAccountMemoAboveMaximumBytes() {
        assertThrows(IllegalArgumentException.class, () -> new AccountUpdateTransaction(accountIdValue(), authorityValue(), "ääääääääääääääääääääääääääääääääääääääääääääääääääa", receiverSignatureRequiredValue(), maxAutomaticTokenAssociationsValue(), autoRenewPeriodValue(), expirationTimeValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue()));
    }

    @Test
    @DisplayName("maxAutomaticTokenAssociations: accepts the minimum (-2147483648)")
    void shouldAcceptMaxAutomaticTokenAssociationsAtMinimum() {
        final @Nullable Integer value = Integer.MIN_VALUE;
        final AccountUpdateTransaction subject = new AccountUpdateTransaction(accountIdValue(), authorityValue(), accountMemoValue(), receiverSignatureRequiredValue(), value, autoRenewPeriodValue(), expirationTimeValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue());
        assertValue(value, subject.maxAutomaticTokenAssociations());
    }

    @Test
    @DisplayName("maxAutomaticTokenAssociations: accepts the maximum (2147483647)")
    void shouldAcceptMaxAutomaticTokenAssociationsAtMaximum() {
        final @Nullable Integer value = 2147483647;
        final AccountUpdateTransaction subject = new AccountUpdateTransaction(accountIdValue(), authorityValue(), accountMemoValue(), receiverSignatureRequiredValue(), value, autoRenewPeriodValue(), expirationTimeValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue());
        assertValue(value, subject.maxAutomaticTokenAssociations());
    }

    @Test
    @DisplayName("stakedNodeId: accepts the minimum (-9223372036854775808)")
    void shouldAcceptStakedNodeIdAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final AccountUpdateTransaction subject = new AccountUpdateTransaction(accountIdValue(), authorityValue(), accountMemoValue(), receiverSignatureRequiredValue(), maxAutomaticTokenAssociationsValue(), autoRenewPeriodValue(), expirationTimeValue(), (AccountId) null, value, declineStakingRewardValue());
        assertValue(value, subject.stakedNodeId());
    }

    @Test
    @DisplayName("stakedNodeId: accepts the maximum (9223372036854775807)")
    void shouldAcceptStakedNodeIdAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final AccountUpdateTransaction subject = new AccountUpdateTransaction(accountIdValue(), authorityValue(), accountMemoValue(), receiverSignatureRequiredValue(), maxAutomaticTokenAssociationsValue(), autoRenewPeriodValue(), expirationTimeValue(), (AccountId) null, value, declineStakingRewardValue());
        assertValue(value, subject.stakedNodeId());
    }

    @Test
    @DisplayName("rejects stakedAccountId and stakedNodeId together (@@oneOrNoneOf)")
    void shouldRejectTwoOfStakedAccountIdOrStakedNodeId() {
        assertThrows(IllegalArgumentException.class, () -> new AccountUpdateTransaction(accountIdValue(), authorityValue(), accountMemoValue(), receiverSignatureRequiredValue(), maxAutomaticTokenAssociationsValue(), autoRenewPeriodValue(), expirationTimeValue(), stakedAccountIdValue(), stakedNodeIdValue(), declineStakingRewardValue()));
    }

    @Test
    @DisplayName("setMaxTransactionFee changes maxTransactionFee and returns the object")
    void shouldSetMaxTransactionFee() {
        final AccountUpdateTransaction subject = create();
        final @Nullable NativeToken<?, ?> value = maxTransactionFeeOtherValue();
        assertSame(subject, subject.setMaxTransactionFee(value));
        assertValue(value, subject.maxTransactionFee());
    }

    @Test
    @DisplayName("setMaxTransactionFee accepts null")
    void shouldSetMaxTransactionFeeToNull() {
        final AccountUpdateTransaction subject = create();
        subject.setMaxTransactionFee((NativeToken<?, ?>) null);
        assertNull(subject.maxTransactionFee());
    }

    @Test
    @DisplayName("setValidDuration changes validDuration and returns the object")
    void shouldSetValidDuration() {
        final AccountUpdateTransaction subject = create();
        final @Nullable Duration value = validDurationOtherValue();
        assertSame(subject, subject.setValidDuration(value));
        assertValue(value, subject.validDuration());
    }

    @Test
    @DisplayName("setValidDuration accepts null")
    void shouldSetValidDurationToNull() {
        final AccountUpdateTransaction subject = create();
        subject.setValidDuration((Duration) null);
        assertNull(subject.validDuration());
    }

    @Test
    @DisplayName("setMemo changes memo and returns the object")
    void shouldSetMemo() {
        final AccountUpdateTransaction subject = create();
        final @Nullable String value = memoOtherValue();
        assertSame(subject, subject.setMemo(value));
        assertValue(value, subject.memo());
    }

    @Test
    @DisplayName("setMemo accepts null")
    void shouldSetMemoToNull() {
        final AccountUpdateTransaction subject = create();
        subject.setMemo((String) null);
        assertNull(subject.memo());
    }

    @Test
    @DisplayName("setMemo accepts more elements than the maximum")
    void shouldSetMemoAtMaximumBytes() {
        final AccountUpdateTransaction subject = create();
        final @Nullable String value = "ääääääääääääääääääääääääääääääääääääääääääääääääää";
        subject.setMemo(value);
        assertValue(value, subject.memo());
    }

    @Test
    @DisplayName("setMemo rejects more elements than the maximum and keeps the value")
    void shouldNotSetMemoAboveMaximumBytes() {
        final AccountUpdateTransaction subject = create();
        assertThrows(IllegalArgumentException.class, () -> subject.setMemo("ääääääääääääääääääääääääääääääääääääääääääääääääääa"));
        assertValue(null, subject.memo());
    }

    @Test
    @DisplayName("pack(payer, nodes) can be called and returns a value")
    void shouldCallPack() {
        assertNotNull(create().pack(payerValue(), nodesValue()));
    }

    @Test
    @DisplayName("signWithOperator(client) can be called and returns a value")
    void shouldCallSignWithOperator() {
        assertNotNull(create().signWithOperator(clientValue()));
    }

    @Test
    @DisplayName("sign(payer, nodes) can be called and returns a value")
    void shouldCallSign() {
        assertNotNull(create().sign(payerValue(), nodesValue()));
    }

    @Test
    @DisplayName("sign(payerId, signer, nodes) can be called and returns a value")
    void shouldCallSign2() {
        assertNotNull(create().sign(payerIdValue(), signerValue(), nodesValue()));
    }

    @Test
    @DisplayName("signWithOperatorAndSubmit(client) can be called and returns a value")
    void shouldCallSignWithOperatorAndSubmit() {
        assertNotNull(create().signWithOperatorAndSubmit(clientValue()));
    }

    @Test
    @DisplayName("packForBatch(payer, batchKey) can be called and returns a value")
    void shouldCallPackForBatch() {
        assertNotNull(create().packForBatch(payerValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatchWithOperator(client, batchKey) can be called and returns a value")
    void shouldCallSignForBatchWithOperator() {
        assertNotNull(create().signForBatchWithOperator(clientValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatch(payer, batchKey) can be called and returns a value")
    void shouldCallSignForBatch() {
        assertNotNull(create().signForBatch(payerValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatch(payerId, signer, batchKey) can be called and returns a value")
    void shouldCallSignForBatch2() {
        assertNotNull(create().signForBatch(payerIdValue(), signerValue(), batchKeyValue()));
    }

    private static AccountId accountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static Authority authorityValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
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

    private static ZonedDateTime expirationTimeValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
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

    private static AccountUpdateTransaction create() {
        return new AccountUpdateTransaction(accountIdValue(), authorityValue(), accountMemoValue(), receiverSignatureRequiredValue(), maxAutomaticTokenAssociationsValue(), autoRenewPeriodValue(), expirationTimeValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue());
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
