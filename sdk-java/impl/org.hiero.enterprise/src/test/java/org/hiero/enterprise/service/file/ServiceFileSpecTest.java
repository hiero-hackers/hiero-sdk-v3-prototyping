package org.hiero.enterprise.service.file;

import static org.hiero.sdk.testing.Testing.*;

import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.keys.PrivateKey;
import org.hiero.keys.PublicKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of the spec `service-file`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("service-file")
class ServiceFileSpecTest {

    /// `enterprise.service.file/file-parameters-without-values-have-empty-contents-and-nothing-else`
    ///
    /// Source: this spec (@@default and @@nullable of FileCreateParams, "Nothing is required"); hiero-enterprise-java
    /// (ProtocolLayerDataCreationTests, testFileCreateRequestCreation: empty contents without an expiration time and a
    /// memo are accepted).
    @Test
    @DisplayName("file parameters without values have empty contents and nothing else")
    void fileParametersWithoutValuesHaveEmptyContentsAndNothingElse() {
        // when
        final FileCreateParams params = defaultFileCreateParams();

        // then
        assertEquals(0L, length(params.contents()));
        assertNull(params.authority());
        assertNull(params.expirationTime());
        assertNull(params.fileMemo());
    }

    /// `enterprise.service.file/file-parameters-keep-the-values-they-are-created-with`
    ///
    /// Source: the FileCreateTransaction unit tests of the Java (FileCreateTransactionTest), Go
    /// (file_create_transaction_unit_test.go), C++ (FileCreateTransactionUnitTests) and Python
    /// (file_create_transaction_test.py, test_constructor_with_parameters) SDKs: every value that is set is read back
    /// unchanged.
    @Test
    @DisplayName("file parameters keep the values they are created with")
    void fileParametersKeepTheValuesTheyAreCreatedWith() {
        // given
        final byte[] contents = utf8("Hello, Hiero!");
        final Authority authority = defaultAuthority();

        // when
        final FileCreateParams params = new FileCreateParams(contents, authority, null, "fileMemo");

        // then
        assertEquals(contents, params.contents());
        assertEquals(authority, params.authority());
        assertEquals("fileMemo", params.fileMemo());
    }

    /// `enterprise.service.file/a-file-memo-of-100-characters-is-accepted`
    ///
    /// Source: this spec (@@maxLength(100) of fileMemo); HAPI limits a memo to 100 bytes, which ASCII text of 100
    /// characters is.
    @Test
    @DisplayName("a file memo of 100 characters is accepted")
    void aFileMemoOf100CharactersIsAccepted() {
        // given
        final String memo = repeat("a", 100L);

        // when
        final FileCreateParams params = new FileCreateParams(new byte[0], null, null, memo);

        // then
        assertEquals(memo, params.fileMemo());
    }

    private static FileCreateParams defaultFileCreateParams() {
        return new FileCreateParams(new byte[0], null, null, null);
    }

    private static Authority defaultAuthority() {
        return new PublicKeyAuthority(defaultPublicKey());
    }

    private static PublicKey defaultPublicKey() {
        return defaultPrivateKey().createPublicKey();
    }

    private static PrivateKey defaultPrivateKey() {
        return KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c");
    }
}
