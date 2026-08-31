package org.hiero.sdk.v3.fixture;

import org.hiero.sdk.v3.hedera.Hbar;
import org.hiero.sdk.v3.hedera.HbarUnit;
import org.hiero.sdk.v3.ledger.AccountId;
import org.hiero.sdk.v3.ledger.Address;
import org.hiero.sdk.v3.ledger.ConsensusNode;
import org.hiero.sdk.v3.ledger.IpAddress;
import org.hiero.sdk.v3.ledger.Network;
import org.hiero.sdk.v3.ledger.config.NetworkSetting;
import org.hiero.sdk.v3.token.TokenSupplyType;
import org.hiero.sdk.v3.token.TokenType;

public final class LedgerAndTokenConsumer {
    Network<HbarUnit> network = new Network<>(new byte[] {1}, "test", HbarUnit.TINYBAR);
    Address account = new Address(0, 0, 3, "");
    AccountId payer = new AccountId(0, 0, 3L, "", null, null);
    ConsensusNode node = new ConsensusNode(new IpAddress(new byte[] {127, 0, 0, 1}), 50211, payer);
    TokenType tokenType = TokenType.FUNGIBLE_COMMON;
    TokenSupplyType supplyType = TokenSupplyType.FINITE;
    NetworkSetting setting;
    Hbar hbar;
}
