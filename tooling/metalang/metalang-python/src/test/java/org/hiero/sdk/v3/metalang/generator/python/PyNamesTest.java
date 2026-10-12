package org.hiero.sdk.v3.metalang.generator.python;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PyNamesTest {

    @ParameterizedTest
    @CsvSource({
            "evmAddress, evm_address",
            "restBaseUrl, rest_base_url",
            "HTTPClient, http_client",
            "AccountId, account_id",
            "getConsensusNodes, get_consensus_nodes",
            "ecdsaSecp256k1, ecdsa_secp256k1",
            "PKCS8_WITH_DER, pkcs8_with_der",
            "shard, shard"})
    void namesShouldBeSnakeCase(final String name, final String snake) {
        assertThat(PyNames.snake(name)).isEqualTo(snake);
    }

    @Test
    void keywordsShouldGetATrailingUnderscore() {
        assertThat(PyNames.identifier("from")).isEqualTo("from_");
        assertThat(PyNames.identifier("lambda")).isEqualTo("lambda_");
        // soft keywords stay usable
        assertThat(PyNames.identifier("type")).isEqualTo("type");
        assertThat(PyNames.identifier("match")).isEqualTo("match");
        assertThat(PyNames.isKeyword("None")).isTrue();
        assertThat(PyNames.isKeyword("none")).isFalse();
    }

    @Test
    void packagesAndModulesShouldFollowPep8() {
        assertThat(PyNames.packagePath("consensusnode.transactions.accounts"))
                .isEqualTo("consensusnode/transactions/accounts");
        assertThat(PyNames.packagePath("nativeToken")).isEqualTo("native_token");
        assertThat(PyNames.module("AccountId")).isEqualTo("_account_id");
        assertThat(PyNames.words("HTTPClient")).containsExactly("HTTP", "Client");
    }
}
