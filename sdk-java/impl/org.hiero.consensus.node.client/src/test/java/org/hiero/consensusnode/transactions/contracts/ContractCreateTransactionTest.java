package org.hiero.consensusnode.transactions.contracts;

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
import org.hiero.ledger.Address;
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

/// Tests of `ContractCreateTransaction`, generated from the specs.
@DisplayName("ContractCreateTransaction")
class ContractCreateTransactionTest {

    @Test
    @DisplayName("creates a ContractCreateTransaction and returns the values")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateTransaction/create")
    void shouldCreateAndReturnTheValues() {
        final byte @Nullable [] bytecode = bytecodeValue();
        final @Nullable Address bytecodeFileId = (Address) null;
        final long gas = gasValue();
        final byte[] constructorParameters = constructorParametersValue();
        final @Nullable NativeToken<?, ?> initialBalance = initialBalanceValue();
        final @Nullable Authority adminAuthority = adminAuthorityValue();
        final @Nullable String contractMemo = contractMemoValue();
        final @Nullable Duration autoRenewPeriod = autoRenewPeriodValue();
        final @Nullable AccountId autoRenewAccount = autoRenewAccountValue();
        final @Nullable Integer maxAutomaticTokenAssociations = maxAutomaticTokenAssociationsValue();
        final @Nullable AccountId stakedAccountId = stakedAccountIdValue();
        final @Nullable Long stakedNodeId = (Long) null;
        final boolean declineStakingReward = declineStakingRewardValue();
        final ContractCreateTransaction subject = new ContractCreateTransaction(bytecode, bytecodeFileId, gas, constructorParameters, initialBalance, adminAuthority, contractMemo, autoRenewPeriod, autoRenewAccount, maxAutomaticTokenAssociations, stakedAccountId, stakedNodeId, declineStakingReward);
        assertValue(bytecode, subject.bytecode());
        assertValue(bytecodeFileId, subject.bytecodeFileId());
        assertValue(gas, subject.gas());
        assertValue(constructorParameters, subject.constructorParameters());
        assertValue(initialBalance, subject.initialBalance());
        assertValue(adminAuthority, subject.adminAuthority());
        assertValue(contractMemo, subject.contractMemo());
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
    @DisplayName("creates a ContractCreateTransaction with the default values")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateTransaction/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final ContractCreateTransaction subject = new ContractCreateTransaction(bytecodeValue(), (Address) null, gasValue(), initialBalanceValue(), adminAuthorityValue(), contractMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), (Long) null);
        assertValue(new byte[0], subject.constructorParameters());
        assertValue(false, subject.declineStakingReward());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateTransaction/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final byte @Nullable [] bytecode = bytecodeValue();
        final @Nullable Address bytecodeFileId = (Address) null;
        final long gas = gasValue();
        final byte[] constructorParameters = constructorParametersValue();
        final @Nullable NativeToken<?, ?> initialBalance = initialBalanceValue();
        final @Nullable Authority adminAuthority = adminAuthorityValue();
        final @Nullable String contractMemo = contractMemoValue();
        final @Nullable Duration autoRenewPeriod = autoRenewPeriodValue();
        final @Nullable AccountId autoRenewAccount = autoRenewAccountValue();
        final @Nullable Integer maxAutomaticTokenAssociations = maxAutomaticTokenAssociationsValue();
        final @Nullable AccountId stakedAccountId = stakedAccountIdValue();
        final @Nullable Long stakedNodeId = (Long) null;
        final boolean declineStakingReward = declineStakingRewardValue();
        final ContractCreateTransaction subject = ContractCreateTransaction.builder()
                .bytecode(bytecode)
                .bytecodeFileId(bytecodeFileId)
                .gas(gas)
                .constructorParameters(constructorParameters)
                .initialBalance(initialBalance)
                .adminAuthority(adminAuthority)
                .contractMemo(contractMemo)
                .autoRenewPeriod(autoRenewPeriod)
                .autoRenewAccount(autoRenewAccount)
                .maxAutomaticTokenAssociations(maxAutomaticTokenAssociations)
                .stakedAccountId(stakedAccountId)
                .stakedNodeId(stakedNodeId)
                .declineStakingReward(declineStakingReward)
                .build();
        assertValue(bytecode, subject.bytecode());
        assertValue(bytecodeFileId, subject.bytecodeFileId());
        assertValue(gas, subject.gas());
        assertValue(constructorParameters, subject.constructorParameters());
        assertValue(initialBalance, subject.initialBalance());
        assertValue(adminAuthority, subject.adminAuthority());
        assertValue(contractMemo, subject.contractMemo());
        assertValue(autoRenewPeriod, subject.autoRenewPeriod());
        assertValue(autoRenewAccount, subject.autoRenewAccount());
        assertValue(maxAutomaticTokenAssociations, subject.maxAutomaticTokenAssociations());
        assertValue(stakedAccountId, subject.stakedAccountId());
        assertValue(stakedNodeId, subject.stakedNodeId());
        assertValue(declineStakingReward, subject.declineStakingReward());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateTransaction/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final ContractCreateTransaction subject = ContractCreateTransaction.builder()
                .bytecode(bytecodeValue())
                .gas(gasValue())
                .stakedAccountId(stakedAccountIdValue())
                .build();
        assertValue(bytecodeValue(), subject.bytecode());
        assertNull(subject.bytecodeFileId());
        assertValue(new byte[0], subject.constructorParameters());
        assertNull(subject.initialBalance());
        assertNull(subject.adminAuthority());
        assertNull(subject.contractMemo());
        assertNull(subject.autoRenewPeriod());
        assertNull(subject.autoRenewAccount());
        assertNull(subject.maxAutomaticTokenAssociations());
        assertValue(stakedAccountIdValue(), subject.stakedAccountId());
        assertNull(subject.stakedNodeId());
        assertValue(false, subject.declineStakingReward());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateTransaction/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> ContractCreateTransaction.builder().build());
    }

    @Test
    @DisplayName("the constructor copies bytecode; the accessor returns a copy")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateTransaction/bytecode/copies-in")
    void shouldCopyBytecodeIn() {
        final byte[] value = bytecodeValue();
        final byte[] expected = value.clone();
        final ContractCreateTransaction subject = new ContractCreateTransaction(value, (Address) null, gasValue(), constructorParametersValue(), initialBalanceValue(), adminAuthorityValue(), contractMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.bytecode());
        final byte[] returned = subject.bytecode();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.bytecode());
    }

    @Test
    @DisplayName("gas: accepts the minimum (0)")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateTransaction/gas/accepts-at-minimum")
    void shouldAcceptGasAtMinimum() {
        final long value = 0L;
        final ContractCreateTransaction subject = new ContractCreateTransaction(bytecodeValue(), (Address) null, value, constructorParametersValue(), initialBalanceValue(), adminAuthorityValue(), contractMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue());
        assertValue(value, subject.gas());
    }

    @Test
    @DisplayName("gas: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateTransaction/gas/accepts-at-maximum")
    void shouldAcceptGasAtMaximum() {
        final long value = 9223372036854775807L;
        final ContractCreateTransaction subject = new ContractCreateTransaction(bytecodeValue(), (Address) null, value, constructorParametersValue(), initialBalanceValue(), adminAuthorityValue(), contractMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue());
        assertValue(value, subject.gas());
    }

    @Test
    @DisplayName("gas: rejects a value below the minimum (-1)")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateTransaction/gas/rejects-below-minimum")
    void shouldRejectGasBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new ContractCreateTransaction(bytecodeValue(), (Address) null, -1L, constructorParametersValue(), initialBalanceValue(), adminAuthorityValue(), contractMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue()));
    }

    @Test
    @DisplayName("rejects null for constructorParameters")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateTransaction/constructorParameters/rejects-null")
    void shouldRejectNullConstructorParameters() {
        assertThrows(NullPointerException.class, () -> new ContractCreateTransaction(bytecodeValue(), (Address) null, gasValue(), (byte[]) null, initialBalanceValue(), adminAuthorityValue(), contractMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue()));
    }

    @Test
    @DisplayName("the constructor copies constructorParameters; the accessor returns a copy")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateTransaction/constructorParameters/copies-in")
    void shouldCopyConstructorParametersIn() {
        final byte[] value = constructorParametersValue();
        final byte[] expected = value.clone();
        final ContractCreateTransaction subject = new ContractCreateTransaction(bytecodeValue(), (Address) null, gasValue(), value, initialBalanceValue(), adminAuthorityValue(), contractMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.constructorParameters());
        final byte[] returned = subject.constructorParameters();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.constructorParameters());
    }

    @Test
    @DisplayName("contractMemo: accepts more elements than the maximum")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateTransaction/contractMemo/accepts-at-maximum-bytes")
    void shouldAcceptContractMemoAtMaximumBytes() {
        final @Nullable String value = "ääääääääääääääääääääääääääääääääääääääääääääääääää";
        final ContractCreateTransaction subject = new ContractCreateTransaction(bytecodeValue(), (Address) null, gasValue(), constructorParametersValue(), initialBalanceValue(), adminAuthorityValue(), value, autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue());
        assertValue(value, subject.contractMemo());
    }

    @Test
    @DisplayName("contractMemo: rejects more elements than the maximum")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateTransaction/contractMemo/rejects-above-maximum-bytes")
    void shouldRejectContractMemoAboveMaximumBytes() {
        assertThrows(IllegalArgumentException.class, () -> new ContractCreateTransaction(bytecodeValue(), (Address) null, gasValue(), constructorParametersValue(), initialBalanceValue(), adminAuthorityValue(), "ääääääääääääääääääääääääääääääääääääääääääääääääääa", autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue()));
    }

    @Test
    @DisplayName("maxAutomaticTokenAssociations: accepts the minimum (-2147483648)")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateTransaction/maxAutomaticTokenAssociations/accepts-at-minimum")
    void shouldAcceptMaxAutomaticTokenAssociationsAtMinimum() {
        final @Nullable Integer value = Integer.MIN_VALUE;
        final ContractCreateTransaction subject = new ContractCreateTransaction(bytecodeValue(), (Address) null, gasValue(), constructorParametersValue(), initialBalanceValue(), adminAuthorityValue(), contractMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue(), value, stakedAccountIdValue(), (Long) null, declineStakingRewardValue());
        assertValue(value, subject.maxAutomaticTokenAssociations());
    }

    @Test
    @DisplayName("maxAutomaticTokenAssociations: accepts the maximum (2147483647)")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateTransaction/maxAutomaticTokenAssociations/accepts-at-maximum")
    void shouldAcceptMaxAutomaticTokenAssociationsAtMaximum() {
        final @Nullable Integer value = 2147483647;
        final ContractCreateTransaction subject = new ContractCreateTransaction(bytecodeValue(), (Address) null, gasValue(), constructorParametersValue(), initialBalanceValue(), adminAuthorityValue(), contractMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue(), value, stakedAccountIdValue(), (Long) null, declineStakingRewardValue());
        assertValue(value, subject.maxAutomaticTokenAssociations());
    }

    @Test
    @DisplayName("stakedNodeId: accepts the minimum (-9223372036854775808)")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateTransaction/stakedNodeId/accepts-at-minimum")
    void shouldAcceptStakedNodeIdAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final ContractCreateTransaction subject = new ContractCreateTransaction(bytecodeValue(), (Address) null, gasValue(), constructorParametersValue(), initialBalanceValue(), adminAuthorityValue(), contractMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue(), (AccountId) null, value, declineStakingRewardValue());
        assertValue(value, subject.stakedNodeId());
    }

    @Test
    @DisplayName("stakedNodeId: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateTransaction/stakedNodeId/accepts-at-maximum")
    void shouldAcceptStakedNodeIdAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final ContractCreateTransaction subject = new ContractCreateTransaction(bytecodeValue(), (Address) null, gasValue(), constructorParametersValue(), initialBalanceValue(), adminAuthorityValue(), contractMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue(), (AccountId) null, value, declineStakingRewardValue());
        assertValue(value, subject.stakedNodeId());
    }

    @Test
    @DisplayName("rejects bytecode and bytecodeFileId together (@@oneOf)")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateTransaction/bytecode-bytecodeFileId/rejects-two")
    void shouldRejectTwoOfBytecodeOrBytecodeFileId() {
        assertThrows(IllegalArgumentException.class, () -> new ContractCreateTransaction(bytecodeValue(), bytecodeFileIdValue(), gasValue(), constructorParametersValue(), initialBalanceValue(), adminAuthorityValue(), contractMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue()));
    }

    @Test
    @DisplayName("rejects none of bytecode, bytecodeFileId (@@oneOf)")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateTransaction/bytecode-bytecodeFileId/rejects-none")
    void shouldRejectNoneOfBytecodeOrBytecodeFileId() {
        assertThrows(IllegalArgumentException.class, () -> new ContractCreateTransaction((byte[]) null, (Address) null, gasValue(), constructorParametersValue(), initialBalanceValue(), adminAuthorityValue(), contractMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue()));
    }

    @Test
    @DisplayName("rejects stakedAccountId and stakedNodeId together (@@oneOrNoneOf)")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateTransaction/stakedAccountId-stakedNodeId/rejects-two")
    void shouldRejectTwoOfStakedAccountIdOrStakedNodeId() {
        assertThrows(IllegalArgumentException.class, () -> new ContractCreateTransaction(bytecodeValue(), (Address) null, gasValue(), constructorParametersValue(), initialBalanceValue(), adminAuthorityValue(), contractMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), stakedNodeIdValue(), declineStakingRewardValue()));
    }

    @Test
    @DisplayName("setMaxTransactionFee changes maxTransactionFee and returns the object")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateTransaction/maxTransactionFee/set")
    void shouldSetMaxTransactionFee() {
        final ContractCreateTransaction subject = create();
        final @Nullable NativeToken<?, ?> value = maxTransactionFeeOtherValue();
        assertSame(subject, subject.setMaxTransactionFee(value));
        assertValue(value, subject.maxTransactionFee());
    }

    @Test
    @DisplayName("setMaxTransactionFee accepts null")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateTransaction/maxTransactionFee/set-null")
    void shouldSetMaxTransactionFeeToNull() {
        final ContractCreateTransaction subject = create();
        subject.setMaxTransactionFee((NativeToken<?, ?>) null);
        assertNull(subject.maxTransactionFee());
    }

    @Test
    @DisplayName("setValidDuration changes validDuration and returns the object")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateTransaction/validDuration/set")
    void shouldSetValidDuration() {
        final ContractCreateTransaction subject = create();
        final @Nullable Duration value = validDurationOtherValue();
        assertSame(subject, subject.setValidDuration(value));
        assertValue(value, subject.validDuration());
    }

    @Test
    @DisplayName("setValidDuration accepts null")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateTransaction/validDuration/set-null")
    void shouldSetValidDurationToNull() {
        final ContractCreateTransaction subject = create();
        subject.setValidDuration((Duration) null);
        assertNull(subject.validDuration());
    }

    @Test
    @DisplayName("setMemo changes memo and returns the object")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateTransaction/memo/set")
    void shouldSetMemo() {
        final ContractCreateTransaction subject = create();
        final @Nullable String value = memoOtherValue();
        assertSame(subject, subject.setMemo(value));
        assertValue(value, subject.memo());
    }

    @Test
    @DisplayName("setMemo accepts null")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateTransaction/memo/set-null")
    void shouldSetMemoToNull() {
        final ContractCreateTransaction subject = create();
        subject.setMemo((String) null);
        assertNull(subject.memo());
    }

    @Test
    @DisplayName("setMemo accepts more elements than the maximum")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateTransaction/memo/set-accepts-at-maximum-bytes")
    void shouldSetMemoAtMaximumBytes() {
        final ContractCreateTransaction subject = create();
        final @Nullable String value = "ääääääääääääääääääääääääääääääääääääääääääääääääää";
        subject.setMemo(value);
        assertValue(value, subject.memo());
    }

    @Test
    @DisplayName("setMemo rejects more elements than the maximum and keeps the value")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateTransaction/memo/set-rejects-above-maximum-bytes")
    void shouldNotSetMemoAboveMaximumBytes() {
        final ContractCreateTransaction subject = create();
        assertThrows(IllegalArgumentException.class, () -> subject.setMemo("ääääääääääääääääääääääääääääääääääääääääääääääääääa"));
        assertValue(null, subject.memo());
    }

    @Test
    @DisplayName("pack(payer, nodes) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateTransaction/pack/call")
    void shouldCallPack() {
        assertNotNull(create().pack(payerValue(), nodesValue()));
    }

    @Test
    @DisplayName("signWithOperator(client) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateTransaction/signWithOperator/call")
    void shouldCallSignWithOperator() {
        assertNotNull(create().signWithOperator(clientValue()));
    }

    @Test
    @DisplayName("sign(payer, nodes) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateTransaction/sign/call")
    void shouldCallSign() {
        assertNotNull(create().sign(payerValue(), nodesValue()));
    }

    @Test
    @DisplayName("sign(payerId, signer, nodes) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateTransaction/sign/call-2")
    void shouldCallSign2() {
        assertNotNull(create().sign(payerIdValue(), signerValue(), nodesValue()));
    }

    @Test
    @DisplayName("signWithOperatorAndSubmit(client) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateTransaction/signWithOperatorAndSubmit/call")
    void shouldCallSignWithOperatorAndSubmit() {
        assertNotNull(create().signWithOperatorAndSubmit(clientValue()));
    }

    @Test
    @DisplayName("packForBatch(payer, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateTransaction/packForBatch/call")
    void shouldCallPackForBatch() {
        assertNotNull(create().packForBatch(payerValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatchWithOperator(client, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateTransaction/signForBatchWithOperator/call")
    void shouldCallSignForBatchWithOperator() {
        assertNotNull(create().signForBatchWithOperator(clientValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatch(payer, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateTransaction/signForBatch/call")
    void shouldCallSignForBatch() {
        assertNotNull(create().signForBatch(payerValue(), batchKeyValue()));
    }

    @Test
    @DisplayName("signForBatch(payerId, signer, batchKey) can be called and returns a value")
    @Tag("testId:consensusnode.transactions.contracts.ContractCreateTransaction/signForBatch/call-2")
    void shouldCallSignForBatch2() {
        assertNotNull(create().signForBatch(payerIdValue(), signerValue(), batchKeyValue()));
    }

    private static byte[] bytecodeValue() {
        return new byte[] {1, 2, 3};
    }

    private static Address bytecodeFileIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static long gasValue() {
        return 1L;
    }

    private static byte[] constructorParametersValue() {
        return new byte[] {1, 2, 3};
    }

    private static NativeToken<?, ?> initialBalanceValue() {
        return new Hbar(1L, HbarUnit.TINYBAR);
    }

    private static Authority adminAuthorityValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static String contractMemoValue() {
        return "value";
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

    private static ContractCreateTransaction create() {
        return new ContractCreateTransaction(bytecodeValue(), (Address) null, gasValue(), constructorParametersValue(), initialBalanceValue(), adminAuthorityValue(), contractMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue());
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
