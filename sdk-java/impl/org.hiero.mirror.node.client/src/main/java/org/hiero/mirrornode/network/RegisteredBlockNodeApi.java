package org.hiero.mirrornode.network;


/// An API that a block node offers on an endpoint.
public enum RegisteredBlockNodeApi {

    OTHER,
    STATUS,
    PUBLISH,
    SUBSCRIBE_STREAM,
    STATE_PROOF,
    UNRECOGNIZED;
}
