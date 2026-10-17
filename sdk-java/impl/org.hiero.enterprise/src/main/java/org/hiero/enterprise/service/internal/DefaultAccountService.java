package org.hiero.enterprise.service.internal;

import java.util.Objects;
import org.hiero.authority.Authority;
import org.hiero.common.Page;
import org.hiero.consensusnode.transactions.accounts.AccountCreateTransaction;
import org.hiero.consensusnode.transactions.accounts.AccountDeleteTransaction;
import org.hiero.consensusnode.transactions.accounts.AccountUpdateTransaction;
import org.hiero.enterprise.service.ServiceException;
import org.hiero.enterprise.service.account.AccountCreateParams;
import org.hiero.enterprise.service.account.AccountInformation;
import org.hiero.enterprise.service.account.AccountService;
import org.hiero.ledger.AccountId;
import org.hiero.mirrornode.account.AccountInfo;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// The `AccountService` of a session.
public final class DefaultAccountService extends AccountService {

    private final DefaultSession session;

    /// Creates the service of a session.
    ///
    /// @param session the session
    public DefaultAccountService(final DefaultSession session) {
        this.session = Objects.requireNonNull(session, "session must not be null");
    }

    @Override
    public AccountId createNewAccount(final Authority authority) {
        Objects.requireNonNull(authority, "authority must not be null");
        return createNewAccount(AccountCreateParams.builder().authority(authority).build());
    }

    @Override
    public AccountId createNewAccount(final Authority authority, final NativeToken<?, ?> initialBalance) {
        Objects.requireNonNull(authority, "authority must not be null");
        Objects.requireNonNull(initialBalance, "initialBalance must not be null");
        return createNewAccount(AccountCreateParams.builder().authority(authority).initialBalance(initialBalance)
                .build());
    }

    @Override
    public AccountId createNewAccount(final AccountCreateParams params) {
        Objects.requireNonNull(params, "params must not be null");
        return session.execute(new AccountCreateTransaction(params.authority(), params.initialBalance(),
                params.accountMemo(), params.receiverSignatureRequired(), params.maxAutomaticTokenAssociations(),
                params.autoRenewPeriod(), params.stakedAccountId(), params.stakedNodeId(),
                params.declineStakingReward(), params.alias())).accountId();
    }

    @Override
    public void updateAccountAuthority(final AccountId accountId, final Authority newAuthority,
            final Authority oldAuthority) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        Objects.requireNonNull(newAuthority, "newAuthority must not be null");
        Objects.requireNonNull(oldAuthority, "oldAuthority must not be null");
        session.execute(AccountUpdateTransaction.builder().accountId(accountId).authority(newAuthority).build());
    }

    @Override
    public void deleteAccount(final AccountId account) {
        Objects.requireNonNull(account, "account must not be null");
        session.execute(new AccountDeleteTransaction(account, session.operatorAccountId()));
    }

    @Override
    public AccountInformation findById(final AccountId account) {
        Objects.requireNonNull(account, "account must not be null");
        final AccountInfo info = session.read(mirror -> mirror.accounts().findById(account));
        if (info == null) {
            throw new ServiceException("Account " + account + " not found", null);
        }
        final Authority authority = info.authority();
        if (authority == null) {
            throw new ServiceException("Account " + account + " has no authority", null);
        }
        return new AccountInformation(info.accountId(), Balances.ofBaseUnits(session.network(), info.balance()),
                authority);
    }

    @Override
    public Page<AccountInformation> findAll() {
        return MappedPage.of(session.read(mirror -> mirror.accounts().findAll()), this::toInformation);
    }

    /// Accounts without an authority (e.g. hollow accounts) cannot be described by `AccountInformation`.
    private @Nullable AccountInformation toInformation(final AccountInfo info) {
        final Authority authority = info.authority();
        if (authority == null) {
            return null;
        }
        return new AccountInformation(info.accountId(), Balances.ofBaseUnits(session.network(), info.balance()),
                authority);
    }
}
