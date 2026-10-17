package org.hiero.enterprise.service.contract;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hiero.authority.Authority;
import org.hiero.authority.AuthorityFactory;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.consensusnode.client.ClientFactory;
import org.hiero.consensusnode.transactions.TransactionsFactory;
import org.hiero.consensusnode.transactions.spi.SpiFactory;
import org.hiero.enterprise.service.ServiceFactory;
import org.hiero.enterprise.service.account.AccountFactory;
import org.hiero.enterprise.service.file.FileFactory;
import org.hiero.enterprise.service.nft.NftFactory;
import org.hiero.enterprise.service.token.TokenFactory;
import org.hiero.enterprise.service.topic.TopicFactory;
import org.hiero.hedera.Hbar;
import org.hiero.hedera.HbarUnit;
import org.hiero.http.HttpFactory;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.ledger.config.ConfigFactory;
import org.hiero.mirrornode.MirrornodeFactory;
import org.hiero.mirrornode.network.NetworkFactory;
import org.hiero.mirrornode.transaction.TransactionFactory;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `ContractCreateParams`, generated from the specs.
@DisplayName("ContractCreateParams")
class ContractCreateParamsTest {

    @Test
    @DisplayName("creates a ContractCreateParams and returns the values")
    @Tag("testId:enterprise.service.contract.ContractCreateParams/create")
    void shouldCreateAndReturnTheValues() {
        final @Nullable Address fileId = fileIdValue();
        final byte @Nullable [] bytecode = (byte[]) null;
        final List<Param<?, ?>> constructorParams = constructorParamsValue();
        final @Nullable Long gas = gasValue();
        final @Nullable NativeToken<?, ?> initialBalance = initialBalanceValue();
        final @Nullable Authority adminAuthority = adminAuthorityValue();
        final @Nullable String contractMemo = contractMemoValue();
        final @Nullable Duration autoRenewPeriod = autoRenewPeriodValue();
        final @Nullable AccountId autoRenewAccount = autoRenewAccountValue();
        final @Nullable Integer maxAutomaticTokenAssociations = maxAutomaticTokenAssociationsValue();
        final @Nullable AccountId stakedAccountId = stakedAccountIdValue();
        final @Nullable Long stakedNodeId = (Long) null;
        final boolean declineStakingReward = declineStakingRewardValue();
        final ContractCreateParams subject = new ContractCreateParams(fileId, bytecode, constructorParams, gas, initialBalance, adminAuthority, contractMemo, autoRenewPeriod, autoRenewAccount, maxAutomaticTokenAssociations, stakedAccountId, stakedNodeId, declineStakingReward);
        assertValue(fileId, subject.fileId());
        assertValue(bytecode, subject.bytecode());
        assertValue(constructorParams, subject.constructorParams());
        assertValue(gas, subject.gas());
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
    @DisplayName("creates a ContractCreateParams with the default values")
    @Tag("testId:enterprise.service.contract.ContractCreateParams/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final ContractCreateParams subject = new ContractCreateParams(fileIdValue(), (byte[]) null, gasValue(), initialBalanceValue(), adminAuthorityValue(), contractMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), (Long) null);
        assertValue(List.of(), subject.constructorParams());
        assertValue(false, subject.declineStakingReward());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:enterprise.service.contract.ContractCreateParams/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final @Nullable Address fileId = fileIdValue();
        final byte @Nullable [] bytecode = (byte[]) null;
        final List<Param<?, ?>> constructorParams = constructorParamsValue();
        final @Nullable Long gas = gasValue();
        final @Nullable NativeToken<?, ?> initialBalance = initialBalanceValue();
        final @Nullable Authority adminAuthority = adminAuthorityValue();
        final @Nullable String contractMemo = contractMemoValue();
        final @Nullable Duration autoRenewPeriod = autoRenewPeriodValue();
        final @Nullable AccountId autoRenewAccount = autoRenewAccountValue();
        final @Nullable Integer maxAutomaticTokenAssociations = maxAutomaticTokenAssociationsValue();
        final @Nullable AccountId stakedAccountId = stakedAccountIdValue();
        final @Nullable Long stakedNodeId = (Long) null;
        final boolean declineStakingReward = declineStakingRewardValue();
        final ContractCreateParams subject = ContractCreateParams.builder()
                .fileId(fileId)
                .bytecode(bytecode)
                .constructorParams(constructorParams)
                .gas(gas)
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
        assertValue(fileId, subject.fileId());
        assertValue(bytecode, subject.bytecode());
        assertValue(constructorParams, subject.constructorParams());
        assertValue(gas, subject.gas());
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
    @Tag("testId:enterprise.service.contract.ContractCreateParams/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final ContractCreateParams subject = ContractCreateParams.builder()
                .fileId(fileIdValue())
                .stakedAccountId(stakedAccountIdValue())
                .build();
        assertValue(fileIdValue(), subject.fileId());
        assertNull(subject.bytecode());
        assertValue(List.of(), subject.constructorParams());
        assertNull(subject.gas());
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
    @DisplayName("the constructor copies bytecode; the accessor returns a copy")
    @Tag("testId:enterprise.service.contract.ContractCreateParams/bytecode/copies-in")
    void shouldCopyBytecodeIn() {
        final byte[] value = bytecodeValue();
        final byte[] expected = value.clone();
        final ContractCreateParams subject = new ContractCreateParams((Address) null, value, constructorParamsValue(), gasValue(), initialBalanceValue(), adminAuthorityValue(), contractMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.bytecode());
        final byte[] returned = subject.bytecode();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.bytecode());
    }

    @Test
    @DisplayName("rejects null for constructorParams")
    @Tag("testId:enterprise.service.contract.ContractCreateParams/constructorParams/rejects-null")
    void shouldRejectNullConstructorParams() {
        assertThrows(NullPointerException.class, () -> new ContractCreateParams(fileIdValue(), (byte[]) null, (List<Param<?, ?>>) null, gasValue(), initialBalanceValue(), adminAuthorityValue(), contractMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue()));
    }

    @Test
    @DisplayName("the constructor copies constructorParams; the accessor returns an unmodifiable collection")
    @Tag("testId:enterprise.service.contract.ContractCreateParams/constructorParams/copies-in")
    void shouldCopyConstructorParamsIn() {
        final List<Param<?, ?>> value = new ArrayList<>(constructorParamsValue());
        final List<Param<?, ?>> expected = List.copyOf(value);
        final ContractCreateParams subject = new ContractCreateParams(fileIdValue(), (byte[]) null, value, gasValue(), initialBalanceValue(), adminAuthorityValue(), contractMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue());
        value.clear();
        assertValue(expected, subject.constructorParams());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.constructorParams()).clear());
    }

    @Test
    @DisplayName("gas: accepts the minimum (1)")
    @Tag("testId:enterprise.service.contract.ContractCreateParams/gas/accepts-at-minimum")
    void shouldAcceptGasAtMinimum() {
        final @Nullable Long value = 1L;
        final ContractCreateParams subject = new ContractCreateParams(fileIdValue(), (byte[]) null, constructorParamsValue(), value, initialBalanceValue(), adminAuthorityValue(), contractMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue());
        assertValue(value, subject.gas());
    }

    @Test
    @DisplayName("gas: accepts the maximum (9223372036854775807)")
    @Tag("testId:enterprise.service.contract.ContractCreateParams/gas/accepts-at-maximum")
    void shouldAcceptGasAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final ContractCreateParams subject = new ContractCreateParams(fileIdValue(), (byte[]) null, constructorParamsValue(), value, initialBalanceValue(), adminAuthorityValue(), contractMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue());
        assertValue(value, subject.gas());
    }

    @Test
    @DisplayName("gas: rejects a value below the minimum (0)")
    @Tag("testId:enterprise.service.contract.ContractCreateParams/gas/rejects-below-minimum")
    void shouldRejectGasBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new ContractCreateParams(fileIdValue(), (byte[]) null, constructorParamsValue(), 0L, initialBalanceValue(), adminAuthorityValue(), contractMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue()));
    }

    @Test
    @DisplayName("contractMemo: accepts more elements than the maximum")
    @Tag("testId:enterprise.service.contract.ContractCreateParams/contractMemo/accepts-at-maximum-bytes")
    void shouldAcceptContractMemoAtMaximumBytes() {
        final @Nullable String value = "ääääääääääääääääääääääääääääääääääääääääääääääääää";
        final ContractCreateParams subject = new ContractCreateParams(fileIdValue(), (byte[]) null, constructorParamsValue(), gasValue(), initialBalanceValue(), adminAuthorityValue(), value, autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue());
        assertValue(value, subject.contractMemo());
    }

    @Test
    @DisplayName("contractMemo: rejects more elements than the maximum")
    @Tag("testId:enterprise.service.contract.ContractCreateParams/contractMemo/rejects-above-maximum-bytes")
    void shouldRejectContractMemoAboveMaximumBytes() {
        assertThrows(IllegalArgumentException.class, () -> new ContractCreateParams(fileIdValue(), (byte[]) null, constructorParamsValue(), gasValue(), initialBalanceValue(), adminAuthorityValue(), "ääääääääääääääääääääääääääääääääääääääääääääääääääa", autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue()));
    }

    @Test
    @DisplayName("maxAutomaticTokenAssociations: accepts the minimum (-2147483648)")
    @Tag("testId:enterprise.service.contract.ContractCreateParams/maxAutomaticTokenAssociations/accepts-at-minimum")
    void shouldAcceptMaxAutomaticTokenAssociationsAtMinimum() {
        final @Nullable Integer value = Integer.MIN_VALUE;
        final ContractCreateParams subject = new ContractCreateParams(fileIdValue(), (byte[]) null, constructorParamsValue(), gasValue(), initialBalanceValue(), adminAuthorityValue(), contractMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue(), value, stakedAccountIdValue(), (Long) null, declineStakingRewardValue());
        assertValue(value, subject.maxAutomaticTokenAssociations());
    }

    @Test
    @DisplayName("maxAutomaticTokenAssociations: accepts the maximum (2147483647)")
    @Tag("testId:enterprise.service.contract.ContractCreateParams/maxAutomaticTokenAssociations/accepts-at-maximum")
    void shouldAcceptMaxAutomaticTokenAssociationsAtMaximum() {
        final @Nullable Integer value = 2147483647;
        final ContractCreateParams subject = new ContractCreateParams(fileIdValue(), (byte[]) null, constructorParamsValue(), gasValue(), initialBalanceValue(), adminAuthorityValue(), contractMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue(), value, stakedAccountIdValue(), (Long) null, declineStakingRewardValue());
        assertValue(value, subject.maxAutomaticTokenAssociations());
    }

    @Test
    @DisplayName("stakedNodeId: accepts the minimum (-9223372036854775808)")
    @Tag("testId:enterprise.service.contract.ContractCreateParams/stakedNodeId/accepts-at-minimum")
    void shouldAcceptStakedNodeIdAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final ContractCreateParams subject = new ContractCreateParams(fileIdValue(), (byte[]) null, constructorParamsValue(), gasValue(), initialBalanceValue(), adminAuthorityValue(), contractMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue(), (AccountId) null, value, declineStakingRewardValue());
        assertValue(value, subject.stakedNodeId());
    }

    @Test
    @DisplayName("stakedNodeId: accepts the maximum (9223372036854775807)")
    @Tag("testId:enterprise.service.contract.ContractCreateParams/stakedNodeId/accepts-at-maximum")
    void shouldAcceptStakedNodeIdAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final ContractCreateParams subject = new ContractCreateParams(fileIdValue(), (byte[]) null, constructorParamsValue(), gasValue(), initialBalanceValue(), adminAuthorityValue(), contractMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue(), (AccountId) null, value, declineStakingRewardValue());
        assertValue(value, subject.stakedNodeId());
    }

    @Test
    @DisplayName("rejects fileId and bytecode together (@@oneOf)")
    @Tag("testId:enterprise.service.contract.ContractCreateParams/fileId-bytecode/rejects-two")
    void shouldRejectTwoOfFileIdOrBytecode() {
        assertThrows(IllegalArgumentException.class, () -> new ContractCreateParams(fileIdValue(), bytecodeValue(), constructorParamsValue(), gasValue(), initialBalanceValue(), adminAuthorityValue(), contractMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue()));
    }

    @Test
    @DisplayName("rejects none of fileId, bytecode (@@oneOf)")
    @Tag("testId:enterprise.service.contract.ContractCreateParams/fileId-bytecode/rejects-none")
    void shouldRejectNoneOfFileIdOrBytecode() {
        assertThrows(IllegalArgumentException.class, () -> new ContractCreateParams((Address) null, (byte[]) null, constructorParamsValue(), gasValue(), initialBalanceValue(), adminAuthorityValue(), contractMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue()));
    }

    @Test
    @DisplayName("rejects stakedAccountId and stakedNodeId together (@@oneOrNoneOf)")
    @Tag("testId:enterprise.service.contract.ContractCreateParams/stakedAccountId-stakedNodeId/rejects-two")
    void shouldRejectTwoOfStakedAccountIdOrStakedNodeId() {
        assertThrows(IllegalArgumentException.class, () -> new ContractCreateParams(fileIdValue(), (byte[]) null, constructorParamsValue(), gasValue(), initialBalanceValue(), adminAuthorityValue(), contractMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), stakedNodeIdValue(), declineStakingRewardValue()));
    }

    private static Address fileIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static byte[] bytecodeValue() {
        return new byte[] {1, 2, 3};
    }

    private static List<Param<?, ?>> constructorParamsValue() {
        return List.of(ContractFactory.int256(new BigInteger("1")));
    }

    private static long gasValue() {
        return 1L;
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

    private static ContractCreateParams create() {
        return new ContractCreateParams(fileIdValue(), (byte[]) null, constructorParamsValue(), gasValue(), initialBalanceValue(), adminAuthorityValue(), contractMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue());
    }

    private static ContractCreateParams defaultSubject() {
        return new ContractCreateParams(new Address(0L, 0L, "", 1001L), null, List.of(), null, null, null, null, null, null, null, null, null, false);
    }
}
