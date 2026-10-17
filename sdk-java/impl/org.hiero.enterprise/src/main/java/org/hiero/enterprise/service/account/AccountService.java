package org.hiero.enterprise.service.account;

import org.hiero.authority.Authority;
import org.hiero.common.Page;
import org.hiero.enterprise.service.ServiceException;
import org.hiero.enterprise.service.internal.Unbound;
import org.hiero.ledger.AccountId;
import org.hiero.nativeToken.NativeToken;

public class AccountService {

    /// Creates a new `AccountService` that is not bound to a session: every method fails with `ServiceException`.
    /// A usable service is created with `AccountFactory.createService(session)`.
    public AccountService() {
    }

    /// Creates an account with the given authority; the same as createNewAccount with AccountCreateParams that have
    /// only the authority.
    ///
    /// @throws ServiceException if a service error occurs
    public AccountId createNewAccount(final Authority authority) {
        throw Unbound.error("AccountService", "AccountFactory");
    }

    /// Creates an account with the given authority and initial balance; the same as createNewAccount with
    /// AccountCreateParams that have only these two values.
    ///
    /// @throws ServiceException if a service error occurs
    public AccountId createNewAccount(final Authority authority, final NativeToken<?, ?> initialBalance) {
        throw Unbound.error("AccountService", "AccountFactory");
    }

    /// Creates an account with the given parameters: the general form of the methods above, for every option of a
    /// new account.
    ///
    /// @throws ServiceException if a service error occurs
    public AccountId createNewAccount(final AccountCreateParams params) {
        throw Unbound.error("AccountService", "AccountFactory");
    }

    /// @throws ServiceException if a service error occurs
    public void updateAccountAuthority(final AccountId accountId, final Authority newAuthority, final Authority oldAuthority) {
        throw Unbound.error("AccountService", "AccountFactory");
    }

    /// @throws ServiceException if a service error occurs
    public void deleteAccount(final AccountId account) {
        throw Unbound.error("AccountService", "AccountFactory");
    }

    /// @throws ServiceException if a service error occurs
    public AccountInformation findById(final AccountId account) {
        throw Unbound.error("AccountService", "AccountFactory");
    }

    /// @throws ServiceException if a service error occurs
    public Page<AccountInformation> findAll() {
        throw Unbound.error("AccountService", "AccountFactory");
    }
}
