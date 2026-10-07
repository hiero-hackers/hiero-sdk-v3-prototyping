package org.hiero.sdk.v3.metalang.generator.go;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GoNamesTest {

    @ParameterizedTest
    @CsvSource({
            // the table of guidelines/api-best-practices-go.md, "Names"
            "AccountId,        AccountID",
            "transactionId,    TransactionID",
            "evmAddress,       EVMAddress",
            "restBaseUrl,      RESTBaseURL",
            "nativeToken,      NativeToken",
            "shard,            Shard",
            "toStringWithChecksum, ToStringWithChecksum",
            // a true initialism stays upper case, Ed25519 does not - Go spells it as crypto/ed25519 does
            "ECDSA,            ECDSA",
            "ED25519,          Ed25519",
            "PKCS8_WITH_DER,   PKCS8WithDER",
            "SPKI_WITH_PEM,    SPKIWithPEM",
            "not-found-error,  NotFoundError",
    })
    void shouldCapitaliseEveryWordAndKeepInitialismsUpperCase(final String meta, final String go) {
        assertThat(GoNames.exported(meta)).isEqualTo(go);
    }

    @ParameterizedTest
    @CsvSource({
            "transactionId, transactionID",
            "evmAddress,    evmAddress",
            "accountId,     accountID",
            "shard,         shard",
            // a leading initialism must not be exported by accident
            "urlPrefix,     urlPrefix",
            // a Go keyword is escaped
            "type,          type_",
            "range,         range_",
    })
    void shouldLowerTheFirstWordOfAnUnexportedName(final String meta, final String go) {
        assertThat(GoNames.unexported(meta)).isEqualTo(go);
    }

    @ParameterizedTest
    @CsvSource({
            "nativeToken,                            nativetoken",
            "ledger,                                 ledger",
            "consensusnode.transactions.accounts,    consensusnode/transactions/accounts",
            "ledger.config,                          ledger/config",
    })
    void shouldMapNamespacesToLowercasePackagesAndDirectories(final String namespace, final String expected) {
        assertThat(GoNames.directory(namespace)).isEqualTo(expected);
    }

    @Test
    void shouldPrefixAnEnumConstantWithItsType() {
        assertThat(GoNames.enumConstant("KeyAlgorithm", "ED25519")).isEqualTo("KeyAlgorithmEd25519");
        assertThat(GoNames.enumConstant("KeyAlgorithm", "ECDSA")).isEqualTo("KeyAlgorithmECDSA");
        assertThat(GoNames.enumConstant("KeyFormat", "PKCS8_WITH_DER")).isEqualTo("KeyFormatPKCS8WithDER");
    }

    @Test
    void shouldNameAnErrorTypeAfterItsIdentifier() {
        assertThat(GoNames.errorType("not-found-error")).isEqualTo("NotFoundError");
        assertThat(GoNames.errorType("illegal-format")).isEqualTo("IllegalFormatError");
    }

    @Test
    void shouldEscapeOnlyReservedWords() {
        assertThat(GoNames.identifier("account")).isEqualTo("account");
        assertThat(GoNames.identifier("func")).isEqualTo("func_");
        assertThat(GoNames.identifier("string")).isEqualTo("string_");
    }

    @Test
    void shouldPassAnEmptyNameThrough() {
        // the guard that keeps unexported() off an empty word list
        assertThat(GoNames.unexported("")).isEmpty();
        assertThat(GoNames.exported("")).isEmpty();
    }

    @Test
    void shouldNeverStartAWordWithADigit() {
        // a digit belongs to the word it follows - otherwise Ed25519 and PKCS8 would fall apart
        assertThat(GoNames.words("ED25519")).containsExactly("ED25519");
        assertThat(GoNames.words("base64")).containsExactly("BASE64");
        assertThat(GoNames.words("secp256k1")).containsExactly("SECP256K1");
        assertThat(GoNames.exported("sha384Digest")).isEqualTo("Sha384Digest");
    }

    @Test
    void shouldSplitIdentifiersIntoWords() {
        assertThat(GoNames.words("restBaseUrl")).containsExactly("REST", "BASE", "URL");
        assertThat(GoNames.words("PKCS8_WITH_DER")).containsExactly("PKCS8", "WITH", "DER");
        assertThat(GoNames.words("")).isEmpty();
    }
}
