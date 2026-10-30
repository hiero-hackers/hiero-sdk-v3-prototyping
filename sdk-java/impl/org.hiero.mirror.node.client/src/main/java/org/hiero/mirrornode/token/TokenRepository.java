package org.hiero.mirrornode.token;

import java.util.List;
import java.util.concurrent.CompletionStage;
import org.hiero.common.Page;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.mirrornode.common.Order;
import org.hiero.token.TokenType;
import org.jspecify.annotations.Nullable;

public interface TokenRepository {

    /// The tokens of the network. Maps to GET /api/v1/tokens.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<Token>> findAll();

    /// The tokens of the network, filtered by associated account, name, public key, id and type. `name` (a partial or
    /// full name) cannot be combined with `accountId` or `tokenId`, and its results are not paginated.
    /// Maps to GET /api/v1/tokens.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<Token>> findAll(final List<String> accountId, final @Nullable Integer limit, final @Nullable String name, final @Nullable Order order, final @Nullable String publicKey, final List<String> tokenId, final List<TokenType> type);

    /// The tokens associated with the account. Maps to GET /api/v1/tokens.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<Token>> findByAccount(final AccountId accountId);

    /// The token, or null if the mirror node does not know it. Maps to GET /api/v1/tokens/{tokenId}.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<@Nullable TokenInfo> findById(final Address tokenId);

    /// The token as it was at the given consensus time (operators eq, lt and lte), or null if the mirror node does not
    /// know it. Maps to GET /api/v1/tokens/{tokenId}.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<@Nullable TokenInfo> findById(final Address tokenId, final List<String> timestamp);

    /// The balances of the token held by the accounts. Maps to GET /api/v1/tokens/{tokenId}/balances.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<Balance>> getBalances(final Address tokenId);

    /// The balances of the token, filtered by balance, account and public key, at the given consensus time.
    /// Maps to GET /api/v1/tokens/{tokenId}/balances.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<Balance>> getBalances(final Address tokenId, final List<String> accountBalance, final List<String> accountId, final @Nullable String accountPublicKey, final @Nullable Integer limit, final @Nullable Order order, final List<String> timestamp);

    /// The balance of the token held by the account. Maps to GET /api/v1/tokens/{tokenId}/balances.
    ///
    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    CompletionStage<Page<Balance>> getBalancesForAccount(final Address tokenId, final AccountId accountId);
}
