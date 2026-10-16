package org.hiero.consensusnode.transactions.accounts;


/// What a spender is granted on an NFT collection.
public sealed interface NftGrant permits NftSerialGrant, NftCollectionGrant {
}
