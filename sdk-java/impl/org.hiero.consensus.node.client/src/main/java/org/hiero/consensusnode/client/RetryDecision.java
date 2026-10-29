package org.hiero.consensusnode.client;


/// What happens after an attempt that did not succeed.
public sealed interface RetryDecision permits RetryOnSameNode, RetryOnOtherNode, GiveUp {
}
