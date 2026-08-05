package org.hiero.sdk.v3.fixture;

import org.hiero.sdk.v3.authority.Authority;
import org.hiero.sdk.v3.common.Page;
import org.hiero.sdk.v3.grpc.MethodDescriptor;
import org.hiero.sdk.v3.hedera.Hbar;
import org.hiero.sdk.v3.keys.Key;
import org.hiero.sdk.v3.ledger.Network;
import org.hiero.sdk.v3.ledger.config.NetworkSetting;
import org.hiero.sdk.v3.nativetoken.NativeToken;
import org.hiero.sdk.v3.solo.SoloNetworkSetting;
import org.hiero.sdk.v3.token.TokenType;

public final class AllNamespacesConsumer {
    Page<String> page;
    MethodDescriptor descriptor;
    Hbar hbar;
    Key key;
    Network<?> network;
    NetworkSetting setting;
    NativeToken<?, ?> token;
    SoloNetworkSetting solo;
    TokenType tokenType;
    Authority authority;
}
