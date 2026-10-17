package org.hiero.enterprise.service.token;

import org.hiero.authority.Authority;
import org.hiero.common.Page;
import org.hiero.enterprise.service.ServiceException;
import org.hiero.enterprise.service.internal.Unbound;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.mirrornode.token.Balance;
import org.hiero.mirrornode.token.Token;
import org.hiero.mirrornode.token.TokenInfo;
import org.jspecify.annotations.Nullable;

public class FungibleTokenService {

    /// Creates a new `FungibleTokenService` that is not bound to a session: every method fails with `ServiceException`.
    /// A usable service is created with `TokenFactory.createService(session)`.
    public FungibleTokenService() {
    }

    /// Create a fungible token and return its id. The treasury defaults to the operator account; a supply authority
    /// enables minting and burning. The same as createToken with TokenCreateParams that have only these values.
    ///
    /// @throws ServiceException if a service error occurs
    public Address createToken(final String name, final String symbol) {
        throw Unbound.error("FungibleTokenService", "TokenFactory");
    }

    /// @throws ServiceException if a service error occurs
    public Address createToken(final String name, final String symbol, final Authority supplyAuthority) {
        throw Unbound.error("FungibleTokenService", "TokenFactory");
    }

    /// @throws ServiceException if a service error occurs
    public Address createToken(final String name, final String symbol, final AccountId treasuryAccount, final Authority supplyAuthority) {
        throw Unbound.error("FungibleTokenService", "TokenFactory");
    }

    /// Create a fungible token with the given parameters and return its id: the general form of the methods above,
    /// for every option of a new token.
    ///
    /// @throws ServiceException if a service error occurs
    public Address createToken(final TokenCreateParams params) {
        throw Unbound.error("FungibleTokenService", "TokenFactory");
    }

    /// Associate an account with one or more tokens so it can hold them
    ///
    /// @throws ServiceException if a service error occurs
    public void associateToken(final AccountId accountId, final Address... tokenIds) {
        throw Unbound.error("FungibleTokenService", "TokenFactory");
    }

    /// Remove the association between an account and one or more tokens
    ///
    /// @throws ServiceException if a service error occurs
    public void dissociateToken(final AccountId accountId, final Address... tokenIds) {
        throw Unbound.error("FungibleTokenService", "TokenFactory");
    }

    /// Mint new units into the treasury; returns the new total supply
    ///
    /// @throws ServiceException if a service error occurs
    public long mintToken(final Address tokenId, final long amount) {
        throw Unbound.error("FungibleTokenService", "TokenFactory");
    }

    /// Burn units from the treasury; returns the new total supply
    ///
    /// @throws ServiceException if a service error occurs
    public long burnToken(final Address tokenId, final long amount) {
        throw Unbound.error("FungibleTokenService", "TokenFactory");
    }

    /// Transfer units from the operator account to a recipient
    ///
    /// @throws ServiceException if a service error occurs
    public void transferToken(final Address tokenId, final AccountId toAccountId, final long amount) {
        throw Unbound.error("FungibleTokenService", "TokenFactory");
    }

    /// Transfer units from a specific account to a recipient
    ///
    /// @throws ServiceException if a service error occurs
    public void transferToken(final Address tokenId, final AccountId fromAccountId, final AccountId toAccountId, final long amount) {
        throw Unbound.error("FungibleTokenService", "TokenFactory");
    }

    /// Return the full token information for the given token id
    ///
    /// @throws ServiceException if a service error occurs
    public @Nullable TokenInfo findById(final Address tokenId) {
        throw Unbound.error("FungibleTokenService", "TokenFactory");
    }

    /// Return all tokens that the given account is associated with
    ///
    /// @throws ServiceException if a service error occurs
    public Page<Token> findByAccount(final AccountId accountId) {
        throw Unbound.error("FungibleTokenService", "TokenFactory");
    }

    /// Return the balance of the given token held by every account that holds it
    ///
    /// @throws ServiceException if a service error occurs
    public Page<Balance> getBalances(final Address tokenId) {
        throw Unbound.error("FungibleTokenService", "TokenFactory");
    }

    /// Return the balance of the given token for a specific account
    ///
    /// @throws ServiceException if a service error occurs
    public Page<Balance> getBalancesForAccount(final Address tokenId, final AccountId accountId) {
        throw Unbound.error("FungibleTokenService", "TokenFactory");
    }
}
