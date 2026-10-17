package org.hiero.enterprise.service.contract;

import static org.junit.jupiter.api.Assertions.*;

import org.hiero.authority.AuthorityFactory;
import org.hiero.consensusnode.client.ClientFactory;
import org.hiero.consensusnode.transactions.TransactionsFactory;
import org.hiero.consensusnode.transactions.spi.SpiFactory;
import org.hiero.enterprise.service.ServiceFactory;
import org.hiero.enterprise.service.account.AccountFactory;
import org.hiero.enterprise.service.file.FileFactory;
import org.hiero.enterprise.service.nft.NftFactory;
import org.hiero.enterprise.service.token.TokenFactory;
import org.hiero.enterprise.service.topic.TopicFactory;
import org.hiero.http.HttpFactory;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.config.ConfigFactory;
import org.hiero.mirrornode.MirrornodeFactory;
import org.hiero.mirrornode.network.NetworkFactory;
import org.hiero.mirrornode.transaction.TransactionFactory;
import org.junit.jupiter.api.DisplayName;

/// Tests of `Param`, generated from the specs.
///
/// Not generated:
/// - no valid value for `supplier` of Param: constructor, attribute and method tests
@DisplayName("Param")
class ParamTest {

    private static String valueValue() {
        return "value";
    }

    private static String nativeTypeValue() {
        return "value";
    }
}
