package org.hiero.consensusnode.transactions.accounts;


/// Rights to every current and future serial of the collection the owner holds (HAPI
/// `approved_for_all`).
///
/// @param approved false revokes an existing collection-wide grant
public record NftCollectionGrant(boolean approved) implements NftGrant {
}
