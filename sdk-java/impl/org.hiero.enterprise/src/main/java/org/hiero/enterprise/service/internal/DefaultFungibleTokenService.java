package org.hiero.enterprise.service.internal;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.hiero.authority.Authority;
import org.hiero.common.Page;
import org.hiero.consensusnode.transactions.accounts.TokenTransfer;
import org.hiero.consensusnode.transactions.accounts.TransferTransaction;
import org.hiero.consensusnode.transactions.tokens.TokenAssociateTransaction;
import org.hiero.consensusnode.transactions.tokens.TokenBurnFungibleTransaction;
import org.hiero.consensusnode.transactions.tokens.TokenCreateTransaction;
import org.hiero.consensusnode.transactions.tokens.TokenDissociateTransaction;
import org.hiero.consensusnode.transactions.tokens.TokenMintFungibleTransaction;
import org.hiero.enterprise.service.token.FungibleTokenService;
import org.hiero.enterprise.service.token.TokenCreateParams;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.mirrornode.token.Balance;
import org.hiero.mirrornode.token.Token;
import org.hiero.mirrornode.token.TokenInfo;
import org.hiero.token.TokenType;
import org.jspecify.annotations.Nullable;

/// The `FungibleTokenService` of a session. The short forms of `createToken` make the operator account the supply
/// authority, so that the service can mint and burn the new token.
public final class DefaultFungibleTokenService extends FungibleTokenService {

    private final DefaultSession session;

    /// Creates the service of a session.
    ///
    /// @param session the session
    public DefaultFungibleTokenService(final DefaultSession session) {
        this.session = Objects.requireNonNull(session, "session must not be null");
    }

    @Override
    public Address createToken(final String name, final String symbol) {
        return createToken(name, symbol, session.operatorAuthority());
    }

    @Override
    public Address createToken(final String name, final String symbol, final Authority supplyAuthority) {
        return createToken(name, symbol, session.operatorAccountId(), supplyAuthority);
    }

    @Override
    public Address createToken(final String name, final String symbol, final AccountId treasuryAccount,
            final Authority supplyAuthority) {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(symbol, "symbol must not be null");
        Objects.requireNonNull(treasuryAccount, "treasuryAccount must not be null");
        Objects.requireNonNull(supplyAuthority, "supplyAuthority must not be null");
        return createToken(TokenCreateParams.builder().name(name).symbol(symbol).treasuryAccountId(treasuryAccount)
                .supplyAuthority(supplyAuthority).build());
    }

    @Override
    public Address createToken(final TokenCreateParams params) {
        Objects.requireNonNull(params, "params must not be null");
        final AccountId treasury = params.treasuryAccountId() == null
                ? session.operatorAccountId() : params.treasuryAccountId();
        return session.execute(new TokenCreateTransaction(params.name(), params.symbol(), TokenType.FUNGIBLE_COMMON,
                params.decimals(), params.initialSupply(), treasury, params.supplyType(), params.maxSupply(),
                params.freezeDefault(), params.tokenMemo(), params.metadata(), params.expirationTime(),
                params.autoRenewPeriod(), params.autoRenewAccount(), params.adminAuthority(),
                params.supplyAuthority(), params.kycAuthority(), params.freezeAuthority(), params.wipeAuthority(),
                params.pauseAuthority(), params.feeScheduleAuthority(), params.metadataAuthority())).tokenId();
    }

    @Override
    public void associateToken(final AccountId accountId, final Address... tokenIds) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        session.execute(new TokenAssociateTransaction(accountId, tokens(tokenIds)));
    }

    @Override
    public void dissociateToken(final AccountId accountId, final Address... tokenIds) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        session.execute(new TokenDissociateTransaction(accountId, tokens(tokenIds)));
    }

    @Override
    public long mintToken(final Address tokenId, final long amount) {
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        return session.execute(new TokenMintFungibleTransaction(tokenId, amount)).totalSupply();
    }

    @Override
    public long burnToken(final Address tokenId, final long amount) {
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        return session.execute(new TokenBurnFungibleTransaction(tokenId, amount)).totalSupply();
    }

    @Override
    public void transferToken(final Address tokenId, final AccountId toAccountId, final long amount) {
        transferToken(tokenId, session.operatorAccountId(), toAccountId, amount);
    }

    @Override
    public void transferToken(final Address tokenId, final AccountId fromAccountId, final AccountId toAccountId,
            final long amount) {
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        Objects.requireNonNull(fromAccountId, "fromAccountId must not be null");
        Objects.requireNonNull(toAccountId, "toAccountId must not be null");
        session.execute(new TransferTransaction(List.of(), List.of(
                new TokenTransfer(tokenId, fromAccountId, -amount, null),
                new TokenTransfer(tokenId, toAccountId, amount, null)), List.of()));
    }

    @Override
    public @Nullable TokenInfo findById(final Address tokenId) {
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        return session.read(mirror -> mirror.token().findById(tokenId));
    }

    @Override
    public Page<Token> findByAccount(final AccountId accountId) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        return session.read(mirror -> mirror.token().findByAccount(accountId));
    }

    @Override
    public Page<Balance> getBalances(final Address tokenId) {
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        return session.read(mirror -> mirror.token().getBalances(tokenId));
    }

    @Override
    public Page<Balance> getBalancesForAccount(final Address tokenId, final AccountId accountId) {
        Objects.requireNonNull(tokenId, "tokenId must not be null");
        Objects.requireNonNull(accountId, "accountId must not be null");
        return session.read(mirror -> mirror.token().getBalancesForAccount(tokenId, accountId));
    }

    /// The tokens of a varargs parameter as a set.
    ///
    /// @param tokenIds the tokens
    /// @return the set
    static Set<Address> tokens(final Address... tokenIds) {
        Objects.requireNonNull(tokenIds, "tokenIds must not be null");
        return Set.copyOf(Arrays.asList(tokenIds));
    }
}
