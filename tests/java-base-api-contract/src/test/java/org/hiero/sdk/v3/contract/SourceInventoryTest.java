package org.hiero.sdk.v3.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class SourceInventoryTest {

    private static final List<String> SOURCE_TYPES = List.of(
            "authority.Authority", "authority.AuthorityList", "authority.ContractAuthority",
            "authority.PublicKeyAuthority", "common.Page", "grpc.MethodDescriptor",
            "hedera.Hbar", "hedera.HbarUnit", "hedera.HederaNetworkSetting",
            "keys.ByteImportEncoding", "keys.Key", "keys.KeyAlgorithm", "keys.KeyContainer",
            "keys.KeyEncoding", "keys.KeyFormat", "keys.KeyPair", "keys.KeyType", "keys.PrivateKey",
            "keys.PublicKey", "keys.RawFormat", "ledger.AccountId", "ledger.Address",
            "ledger.BaseAddress", "ledger.ConsensusNode", "ledger.ContractId", "ledger.EvmAddress",
            "ledger.EvmCapableAddress", "ledger.IpAddress", "ledger.MirrorNode", "ledger.Network",
            "ledger.TransactionId", "ledger.config.NetworkSetting", "nativetoken.ExchangeRate",
            "nativetoken.NativeToken", "nativetoken.NativeTokenUnit", "solo.SoloNetworkSetting",
            "token.TokenSupplyType", "token.TokenType");

    @Test
    void exposesEverySourceDeclaredTypeAndConstant() throws Exception {
        for (var type : SOURCE_TYPES) {
            Class.forName("org.hiero.sdk.v3." + type);
        }
        assertEquals(38, SOURCE_TYPES.size());

        var constants = List.of(
                "org.hiero.sdk.v3.hedera.HederaConstants#HEDERA_MAINNET_IDENTIFIER",
                "org.hiero.sdk.v3.hedera.HederaConstants#HEDERA_TESTNET_IDENTIFIER",
                "org.hiero.sdk.v3.solo.SoloConstants#SOLO_IDENTIFIER",
                "org.hiero.sdk.v3.ledger.LedgerConstants#ZERO_ADDRESS",
                "org.hiero.sdk.v3.ledger.LedgerConstants#ZERO_ACCOUNT_ID",
                "org.hiero.sdk.v3.ledger.LedgerConstants#ZERO_CONTRACT_ID");
        for (var identity : constants) {
            var separator = identity.indexOf('#');
            Class.forName(identity.substring(0, separator)).getField(identity.substring(separator + 1));
        }
    }

    @Test
    void manifestAccountsForMembersConstraintsAndErrors() throws Exception {
        var manifest = ContractTestSupport.manifest();
        assertTrue(manifest.contains("@@oneOf"));
        assertTrue(manifest.contains("illegal-format"));
        assertTrue(manifest.contains("mirror-node-error"));
        assertTrue(manifest.contains("not-found-error"));
    }
}
