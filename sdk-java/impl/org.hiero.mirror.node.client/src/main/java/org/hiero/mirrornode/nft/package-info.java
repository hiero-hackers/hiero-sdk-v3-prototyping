/// Query NFTs and NFT types (collections) from the Mirror Node. Use the `NftRepository` (available as
/// `MirrorNodeClient.nft`) to find NFTs by owner, by type or by type and serial number, to read the transfer history of
/// an NFT, and to look up the `NftMetadata` (name, symbol and treasury account) of NFT types. An individual `Nft` is
/// identified by the token id of its type plus its serial number and carries its owner, its spenders and its metadata
/// bytes. A burned NFT has no owner.
package org.hiero.mirrornode.nft;
