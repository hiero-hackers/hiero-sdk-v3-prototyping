package org.hiero.consensusnode.client;


/// What one attempt got from the node: an answer or a transport failure.
public sealed interface AttemptResult permits NodeAnswer, TransportFailure {
}
