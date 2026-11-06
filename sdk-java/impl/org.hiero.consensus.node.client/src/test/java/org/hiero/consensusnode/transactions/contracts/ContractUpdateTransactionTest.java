package org.hiero.consensusnode.transactions.contracts;

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
import org.hiero.ledger.ContractId;
import org.hiero.ledger.IpAddress;
import org.hiero.ledger.MirrorNode;
import org.hiero.ledger.Network;
import org.hiero.ledger.config.NetworkSetting;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `ContractUpdateTransaction`, generated from the specs.
@DisplayName("ContractUpdateTransaction")
class ContractUpdateTransactionTest {

    @Test
    @DisplayName("creates a ContractUpdateTransaction and returns the values")
    @Tag("testId:consensusnode.transactions.contracts.ContractUpdateTransaction/create")
    void shouldCreateAndReturnTheValues() {
        final ContractId contractId = contractIdValue();
        final @Nullable Authority adminAuthority = adminAuthorityValue();
        final @Nullable String contractMemo = contractMemoValue();
        final @Nullable ZonedDateTime expirationTime = expirationTimeValue();
        final @Nullable Duration autoRenewPeriod = autoRenewPeriodValue();
        final @Nullable AccountId autoRenewAccount = autoRenewAccountValue();
        final @Nullable Integer maxAutomaticTokenAssociations = maxAutomaticTokenAssociationsValue();
        final @Nullable AccountId stakedAccountId = stakedAccountIdValue();
        final @Nullable Long stakedNodeId = (Long) null;
        final @Nullable Boolean declineStakingReward = declineStakingRewardValue();
        final ContractUpdateTransaction subject = new ContractUpdateTransaction(contractId, adminAuthority, contractMemo, expirationTime, autoRenewPeriod, autoRenewAccount, maxAutomaticTokenAssociations, stakedAccountId, stakedNodeId, declineStakingReward);
        assertValue(contractId, subject.contractId());
        assertValue(adminAuthority, subject.adminAuthority());
        assertValue(contractMemo, subject.contractMemo());
        assertValue(expirationTime, subject.expirationTime());
        assertValue(autoRenewPeriod, subject.autoRenewPeriod());
        assertValue(autoRenewAccount, subject.autoRenewAccount());
        assertValue(maxAutomaticTokenAssociations, subject.maxAutomaticTokenAssociations());
        assertValue(stakedAccountId, subject.stakedAccountId());
        assertValue(stakedNodeId, subject.stakedNodeId());
        assertValue(declineStakingReward, subject.declineStakingReward());
        assertNull(subject.maxTransactionFee());
        assertNull(subject.validDuration());
        assertNull(subject.memo());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:consensusnode.transactions.contracts.ContractUpdateTransaction/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final ContractId contractId = contractIdValue();
        final @Nullable Authority adminAuthority = adminAuthorityValue();
        final @Nullable String contractMemo = contractMemoValue();
        final @Nullable ZonedDateTime expirationTime = expirationTimeValue();
        final @Nullable Duration autoRenewPeriod = autoRenewPeriodValue();
        final @Nullable AccountId autoRenewAccount = autoRenewAccountValue();
        final @Nullable Integer maxAutomaticTokenAssociations = maxAutomaticTokenAssociationsValue();
        final @Nullable AccountId stakedAccountId = stakedAccountIdValue();
        final @Nullable Long stakedNodeId = (Long) null;
        final @Nullable Boolean declineStakingReward = declineStakingRewardValue();
        final ContractUpdateTransaction subject = ContractUpdateTransaction.builder()
                .contractId(contractId)
                .adminAuthority(adminAuthority)
                .contractMemo(contractMemo)
                .expirationTime(expirationTime)
                .autoRenewPeriod(autoRenewPeriod)
                .autoRenewAccount(autoRenewAccount)
                .maxAutomaticTokenAssociations(maxAutomaticTokenAssociations)
                .stakedAccountId(stakedAccountId)
                .stakedNodeId(stakedNodeId)
                .declineStakingReward(declineStakingReward)
                .build();
        assertValue(contractId, subject.contractId());
        assertValue(adminAuthority, subject.adminAuthority());
        assertValue(contractMemo, subject.contractMemo());
        assertValue(expirationTime, subject.expirationTime());
        assertValue(autoRenewPeriod, subject.autoRenewPeriod());
        assertValue(autoRenewAccount, subject.autoRenewAccount());
        assertValue(maxAutomaticTokenAssociations, subject.maxAutomaticTokenAssociations());
        assertValue(stakedAccountId, subject.stakedAccountId());
        assertValue(stakedNodeId, subject.stakedNodeId());
        assertValue(declineStakingReward, subject.declineStakingReward());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:consensusnode.transactions.contracts.ContractUpdateTransaction/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final ContractUpdateTransaction subject = ContractUpdateTransaction.builder()
                .contractId(contractIdValue())
                .stakedAccountId(stakedAccountIdValue())
                .build();
        assertNull(subject.adminAuthority());
        assertNull(subject.contractMemo());
        assertNull(subject.expirationTime());
        assertNull(subject.autoRenewPeriod());
        assertNull(subject.autoRenewAccount());
        assertNull(subject.maxAutomaticTokenAssociations());
        assertValue(stakedAccountIdValue(), subject.stakedAccountId());
        assertNull(subject.stakedNodeId());
        assertNull(subject.declineStakingReward());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:consensusnode.transactions.contracts.ContractUpdateTransaction/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> ContractUpdateTransaction.builder().build());
    }

    @Test
    @DisplayName("rejects null for contractId")
    @Tag("testId:consensusnode.transactions.contracts.ContractUpdateTransaction/contractId/rejects-null")
    void shouldRejectNullContractId() {
        assertThrows(NullPointerException.class, () -> new ContractUpdateTransaction((ContractId) null, adminAuthorityValue(), contractMemoValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue()));
    }

    @Test
    @DisplayName("contractMemo: accepts more elements than the maximum")
    @Tag("testId:consensusnode.transactions.contracts.ContractUpdateTransaction/contractMemo/accepts-at-maximum-bytes")
    void shouldAcceptContractMemoAtMaximumBytes() {
        final @Nullable String value = "ääääääääääääääääääääääääääääääääääääääääääääääääää";
        final ContractUpdateTransaction subject = new ContractUpdateTransaction(contractIdValue(), adminAuthorityValue(), value, expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue());
        assertValue(value, subject.contractMemo());
    }

    @Test
    @DisplayName("contractMemo: rejects more elements than the maximum")
    @Tag("testId:consensusnode.transactions.contracts.ContractUpdateTransaction/contractMemo/rejects-above-maximum-bytes")
    void shouldRejectContractMemoAboveMaximumBytes() {
        assertThrows(IllegalArgumentException.class, () -> new ContractUpdateTransaction(contractIdValue(), adminAuthorityValue(), "ääääääääääääääääääääääääääääääääääääääääääääääääääa", expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue()));
    }

    @Test
    @DisplayName("maxAutomaticTokenAssociations: accepts the minimum (-2147483648)")
    @Tag("testId:consensusnode.transactions.contracts.ContractUpdateTransaction/maxAutomaticTokenAssociations/accepts-at-minimum")
    void shouldAcceptMaxAutomaticTokenAssociationsAtMinimum() {
        final @Nullable Integer value = Integer.MIN_VALUE;
        final ContractUpdateTransaction subject = new ContractUpdateTransaction(contractIdValue(), adminAuthorityValue(), contractMemoValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), value, stakedAccountIdValue(), (Long) null, declineStakingRewardValue());
        assertValue(value, subject.maxAutomaticTokenAssociations());
    }

    @Test
    @DisplayName("maxAutomaticTokenAssociations: accepts the maximum (2147483647)")
    @Tag("testId:consensusnode.transactions.contracts.ContractUpdateTransaction/maxAutomaticTokenAssociations/accepts-at-maximum")
    void shouldAcceptMaxAutomaticTokenAssociationsAtMaximum() {
        final @Nullable Integer value = 2147483647;
        final ContractUpdateTransaction subject = new ContractUpdateTransaction(contractIdValue(), adminAuthorityValue(), contractMemoValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), value, stakedAccountIdValue(), (Long) null, declineStakingRewardValue());
        assertValue(value, subject.maxAutomaticTokenAssociations());
    }

    @Test
    @DisplayName("stakedNodeId: accepts the minimum (-9223372036854775808)")
    @Tag("testId:consensusnode.transactions.contracts.ContractUpdateTransaction/stakedNodeId/accepts-at-minimum")
    void shouldAcceptStakedNodeIdAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final ContractUpdateTransaction subject = new ContractUpdateTransaction(contractIdValue(), adminAuthorityValue(), contractMemoValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue(), (AccountId) null, value, declineStakingRewardValue());
        assertValue(value, subject.stakedNodeId());
    }

    @Test
    @DisplayName("stakedNodeId: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.transactions.contracts.ContractUpdateTransaction/stakedNodeId/accepts-at-maximum")
    void shouldAcceptStakedNodeIdAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final ContractUpdateTransaction subject = new ContractUpdateTransaction(contractIdValue(), adminAuthorityValue(), contractMemoValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue(), (AccountId) null, value, declineStakingRewardValue());
        assertValue(value, subject.stakedNodeId());
    }

    @Test
    @DisplayName("rejects stakedAccountId and stakedNodeId together (@@oneOrNoneOf)")
    @Tag("testId:consensusnode.transactions.contracts.ContractUpdateTransaction/stakedAccountId-stakedNodeId/rejects-two")
    void shouldRejectTwoOfStakedAccountIdOrStakedNodeId() {
        assertThrows(IllegalArgumentException.class, () -> new ContractUpdateTransaction(contractIdValue(), adminAuthorityValue(), contractMemoValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), stakedNodeIdValue(), declineStakingRewardValue()));
    }

    @Test
    @DisplayName("setMaxTransactionFee changes maxTransactionFee and returns the object")
    @Tag("testId:consensusnode.transactions.contracts.ContractUpdateTransaction/maxTransactionFee/set")
    void shouldSetMaxTransactionFee() {
        final ContractUpdateTransaction subject = create();
        final @Nullable NativeToken<?, ?> value = maxTransactionFeeOtherValue();
        assertSame(subject, subject.setMaxTransactionFee(value));
        assertValue(value, subject.maxTransactionFee());
    }

    @Test
    @DisplayName("setMaxTransactionFee accepts null")
    @Tag("testId:consensusnode.transactions.contracts.ContractUpdateTransaction/maxTransactionFee/set-null")
    void shouldSetMaxTransactionFeeToNull() {
        final ContractUpdateTransaction subject = create();
        subject.setMaxTransactionFee((NativeToken<?, ?>) null);
        assertNull(subject.maxTransactionFee());
    }

    @Test
    @DisplayName("setValidDuration changes validDuration and returns the object")
    @Tag("testId:consensusnode.transactions.contracts.ContractUpdateTransaction/validDuration/set")
    void shouldSetValidDuration() {
        final ContractUpdateTransaction subject = create();
        final @Nullable Duration value = validDurationOtherValue();
        assertSame(subject, subject.setValidDuration(value));
        assertValue(value, subject.validDuration());
    }

    @Test
    @DisplayName("setValidDuration accepts null")
    @Tag("testId:consensusnode.transactions.contracts.ContractUpdateTransaction/validDuration/set-null")
    void shouldSetValidDurationToNull() {
        final ContractUpdateTransaction subject = create();
        subject.setValidDuration((Duration) null);
        assertNull(subject.validDuration());
    }

    @Test
    @DisplayName("setMemo changes memo and returns the object")
    @Tag("testId:consensusnode.transactions.contracts.ContractUpdateTransaction/memo/set")
    void shouldSetMemo() {
        final ContractUpdateTransaction subject = create();
        final @Nullable String value = memoOtherValue();
        assertSame(subject, subject.setMemo(value));
        assertValue(value, subject.memo());
    }

    @Test
    @DisplayName("setMemo accepts null")
    @Tag("testId:consensusnode.transactions.contracts.ContractUpdateTransaction/memo/set-null")
    void shouldSetMemoToNull() {
        final ContractUpdateTransaction subject = create();
        subject.setMemo((String) null);
        assertNull(subject.memo());
    }

    @Test
    @DisplayName("setMemo accepts more elements than the maximum")
    @Tag("testId:consensusnode.transactions.contracts.ContractUpdateTransaction/memo/set-accepts-at-maximum-bytes")
    void shouldSetMemoAtMaximumBytes() {
        final ContractUpdateTransaction subject = create();
        final @Nullable String value = "ääääääääääääääääääääääääääääääääääääääääääääääääää";
        subject.setMemo(value);
        assertValue(value, subject.memo());
    }

    @Test
    @DisplayName("setMemo rejects more elements than the maximum and keeps the value")
    @Tag("testId:consensusnode.transactions.contracts.ContractUpdateTransaction/memo/set-rejects-above-maximum-bytes")
    void shouldNotSetMemoAboveMaximumBytes() {
        final ContractUpdateTransaction subject = create();
        assertThrows(IllegalArgumentException.class, () -> subject.setMemo("ääääääääääääääääääääääääääääääääääääääääääääääääääa"));
        assertValue(null, subject.memo());
    }

    @Test
    @DisplayName("pack(payer, nodes) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.contracts.ContractUpdateTransaction/pack/call")
    void shouldCallPack() {
        assertNotNull(create().pack(payerValue(), nodesValue()));
    }

    @Test
    @DisplayName("signWithOperator(client) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.contracts.ContractUpdateTransaction/signWithOperator/call")
    void shouldCallSignWithOperator() {
        assertNotNull(create().signWithOperator(clientValue()));
    }

    @Test
    @DisplayName("sign(payer, nodes) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.contracts.ContractUpdateTransaction/sign/call")
    void shouldCallSign() {
        assertNotNull(create().sign(payerValue(), nodesValue()));
    }

    @Test
    @DisplayName("sign(payerId, signer, nodes) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.contracts.ContractUpdateTransaction/sign/call-2")
    void shouldCallSign2() {
        assertNotNull(create().sign(payerIdValue(), signerValue(), nodesValue()));
    }

    @Test
    @DisplayName("signWithOperatorAndSubmit(client) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.contracts.ContractUpdateTransaction/signWithOperatorAndSubmit/call")
    void shouldCallSignWithOperatorAndSubmit() {
        assertNotNull(create().signWithOperatorAndSubmit(clientValue()));
    }

    @Test
    @DisplayName("packForBatch(payer, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.contracts.ContractUpdateTransaction/packForBatch/call")
    void shouldCallPackForBatch() {
        assertNotNull(create().packForBatch(payerValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatchWithOperator(client, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.contracts.ContractUpdateTransaction/signForBatchWithOperator/call")
    void shouldCallSignForBatchWithOperator() {
        assertNotNull(create().signForBatchWithOperator(clientValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatch(payer, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.contracts.ContractUpdateTransaction/signForBatch/call")
    void shouldCallSignForBatch() {
        assertNotNull(create().signForBatch(payerValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatch(payerId, signer, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.contracts.ContractUpdateTransaction/signForBatch/call-2")
    void shouldCallSignForBatch2() {
        assertNotNull(create().signForBatch(payerIdValue(), signerValue(), batchKeyValue()));
    }

    private static ContractId contractIdValue() {
        return new ContractId(0L, 0L, "", 1001L, null);
    }

    private static Authority adminAuthorityValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static String contractMemoValue() {
        return "value";
    }

    private static ZonedDateTime expirationTimeValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static Duration autoRenewPeriodValue() {
        return Duration.ofSeconds(1L);
    }

    private static AccountId autoRenewAccountValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static int maxAutomaticTokenAssociationsValue() {
        return 1;
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

    private static ContractUpdateTransaction create() {
        return new ContractUpdateTransaction(contractIdValue(), adminAuthorityValue(), contractMemoValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue());
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
