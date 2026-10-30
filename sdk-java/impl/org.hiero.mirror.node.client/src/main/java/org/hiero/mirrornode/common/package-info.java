/// Value types shared by several Mirror Node queries: the HBAR, fungible-token, NFT and staking-reward transfers that
/// make up a transaction, and the fixed custom fees that can be attached to tokens and topics. Transfer amounts are
/// signed: a negative amount is debited from the account, a positive amount is credited to it. HBAR amounts are given in
/// tinybars, token amounts in the token's smallest unit.
///
/// Also shared: the sort order of a list query (`Order`) and the period an entity or a value is valid for
/// (`TimestampRange`).
package org.hiero.mirrornode.common;
