package org.hiero.consensusnode.queries.accounts;

import static org.hiero.sdk.testing.Testing.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import org.hiero.hedera.Hbar;
import org.hiero.hedera.HbarUnit;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.ContractId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of the spec `queries-accounts`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("queries-accounts")
class QueriesAccountsSpecTest {

    /// `consensusnode.queries.accounts/a-balance-query-for-an-account-has-no-contract-id`
    ///
    /// Source: the Python SDK (account_balance_query_test.py, test_build_account_balance_query and
    /// test_set_contract_id_method_chaining_resets_account_id) and the C++ SDK (AccountBalanceQueryUnitTests): a balance
    /// query targets either an account or a contract.
    @Test
    @DisplayName("a balance query for an account has no contract id")
    void aBalanceQueryForAnAccountHasNoContractId() {
        // given
        final AccountId account = defaultAccountId();

        // when
        final AccountBalanceQuery query = new AccountBalanceQuery(account, null);

        // then
        assertEquals(account, query.accountId());
        assertNull(query.contractId());
    }

    /// `consensusnode.queries.accounts/a-balance-query-for-a-contract-has-no-account-id`
    ///
    /// Source: the Python SDK (account_balance_query_test.py, test_build_account_balance_query_with_contract_id) and the C++
    /// SDK (AccountBalanceQueryUnitTests).
    @Test
    @DisplayName("a balance query for a contract has no account id")
    void aBalanceQueryForAContractHasNoAccountId() {
        // given
        final ContractId contract = defaultContractId();

        // when
        final AccountBalanceQuery query = new AccountBalanceQuery(null, contract);

        // then
        assertEquals(contract, query.contractId());
        assertNull(query.accountId());
    }

    /// `consensusnode.queries.accounts/the-info-and-records-queries-keep-their-account-id`
    ///
    /// Source: the Python SDK (account_info_query_test.py and account_records_query_test.py, test_constructor and
    /// test_get_method) and the Swift and Go SDKs (the get / set tests of AccountInfoQuery and AccountRecordsQuery).
    @Test
    @DisplayName("the info and records queries keep their account id")
    void theInfoAndRecordsQueriesKeepTheirAccountId() {
        // given
        final AccountId account = defaultAccountId();

        // when
        final AccountInfoQuery info = new AccountInfoQuery(account);
        final AccountRecordsQuery records = new AccountRecordsQuery(account);

        // then
        assertEquals(account, info.accountId());
        assertEquals(account, records.accountId());
    }

    /// `consensusnode.queries.accounts/an-account-info-has-these-defaults`
    ///
    /// Source: this spec (the defaults of AccountInfo); the Python SDK (account_info_test.py, the initialization of
    /// AccountInfo).
    @Test
    @DisplayName("an account info has these defaults")
    void anAccountInfoHasTheseDefaults() {
        // when
        final AccountInfo info = new AccountInfo(defaultAccountId(), null, defaultHbar(), null, null, ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), null, 0, false, 0L, false, null, null, false);

        // then
        assertEquals(0, info.maxAutomaticTokenAssociations());
        assertFalse(info.receiverSignatureRequired());
        assertEquals(0L, info.ownedNfts());
        assertFalse(info.deleted());
        assertFalse(info.declineStakingReward());
        assertNull(info.evmAddress());
        assertNull(info.authority());
        assertNull(info.accountMemo());
        assertNull(info.autoRenewPeriod());
        assertNull(info.stakedAccountId());
        assertNull(info.stakedNodeId());
    }

    /// `consensusnode.queries.accounts/account-records-without-records-have-an-empty-list`
    ///
    /// Source: this spec (an account without qualifying records yields an empty list); the Python SDK
    /// (account_records_query_test.py, test_account_record_query_empty_records).
    @Test
    @DisplayName("account records without records have an empty list")
    void accountRecordsWithoutRecordsHaveAnEmptyList() {
        // when
        final AccountRecords records = new AccountRecords(defaultAccountId(), List.of());

        // then
        assertEquals(defaultAccountId(), records.accountId());
        assertEquals(0L, length(records.records()));
    }

    private static AccountId defaultAccountId() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static ContractId defaultContractId() {
        return new ContractId(0L, 0L, "", 1001L, null);
    }

    private static Hbar defaultHbar() {
        return new Hbar(1L, HbarUnit.HBAR);
    }
}
