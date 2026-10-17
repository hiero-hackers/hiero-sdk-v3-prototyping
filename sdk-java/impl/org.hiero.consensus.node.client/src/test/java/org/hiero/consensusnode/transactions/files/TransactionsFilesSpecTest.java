package org.hiero.consensusnode.transactions.files;

import static org.hiero.sdk.testing.Testing.*;

import java.time.ZonedDateTime;
import java.util.List;
import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.transactions.PackedTransaction;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.keys.PrivateKey;
import org.hiero.keys.PublicKey;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `transactions-files`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("transactions-files")
class TransactionsFilesSpecTest {

    /// The expiration time of the file create and update tests of the Java SDK (FileCreateTransactionTest and
    /// FileUpdateTransactionTest: Instant.ofEpochSecond(1554158728)) and the Swift SDK (FileCreateTransactionUnitTests and
    /// FileUpdateTransactionUnitTests: Timestamp(seconds: 1_554_158_728, subSecondNanos: 0)), whole seconds.
    private static ZonedDateTime SDK_TEST_EXPIRATION_TIME() {
        return toZonedDateTime("2019-04-01T22:45:28Z");
    }

    /// `consensusnode.transactions.files/a-file-create-transaction-without-attributes-has-empty-contents-and-nothing-else`
    ///
    /// Source: this spec (@@default([]) of contents, @@nullable).
    @Test
    @DisplayName("a file create transaction without attributes has empty contents and nothing else")
    @Tag("testId:consensusnode.transactions.files/a-file-create-transaction-without-attributes-has-empty-contents-and-nothing-else")
    void aFileCreateTransactionWithoutAttributesHasEmptyContentsAndNothingElse() {
        // when
        final FileCreateTransaction transaction = new FileCreateTransaction(new byte[0], null, null, null);

        // then
        assertEquals(0L, length(transaction.contents()));
        assertNull(transaction.authority());
        assertNull(transaction.expirationTime());
        assertNull(transaction.fileMemo());
    }

    /// `consensusnode.transactions.files/a-file-create-transaction-keeps-its-contents-authority-expiration-time-and-memo`
    ///
    /// Source: the Python SDK (test_constructor_with_parameters of FileCreateTransaction: "Test file content",
    /// "Test memo") and the Swift SDK (FileCreateTransactionUnitTests.test_GetSetContents and test_GetSetExpirationTime:
    /// Timestamp(seconds: 1_554_158_728) = 2019-04-01T22:45:28Z).
    @Test
    @DisplayName("a file create transaction keeps its contents, authority, expiration time and memo")
    @Tag("testId:consensusnode.transactions.files/a-file-create-transaction-keeps-its-contents-authority-expiration-time-and-memo")
    void aFileCreateTransactionKeepsItsContentsAuthorityExpirationTimeAndMemo() {
        // given
        final byte[] contents = utf8("Test file content");

        // when
        final FileCreateTransaction transaction = new FileCreateTransaction(contents, defaultAuthority(), SDK_TEST_EXPIRATION_TIME(), "Test memo");

        // then
        assertEquals(contents, transaction.contents());
        assertEquals(defaultAuthority(), transaction.authority());
        assertEquals(SDK_TEST_EXPIRATION_TIME(), transaction.expirationTime());
        assertEquals("Test memo", transaction.fileMemo());
    }

    /// `consensusnode.transactions.files/a-file-create-transaction-survives-tobytes-and-frombytes`
    ///
    /// Source: the Java SDK (FileCreateTransactionTest.shouldBytes and shouldBytesNoSetters; expiration time
    /// Instant.ofEpochSecond(1554158728) = 2019-04-01T22:45:28Z) and the Swift SDK
    /// (FileCreateTransactionUnitTests.test_ToFromBytes).
    @Test
    @DisplayName("a file create transaction survives toBytes and fromBytes")
    @Tag("testId:consensusnode.transactions.files/a-file-create-transaction-survives-tobytes-and-frombytes")
    void aFileCreateTransactionSurvivesToBytesAndFromBytes() {
        // given
        final Account payer = defaultAccount();
        final List<AccountId> nodes = List.of(AccountId.fromString("0.0.3"), AccountId.fromString("0.0.4"));
        final FileCreateTransaction withAttributes = new FileCreateTransaction(utf8("Test file content"), defaultAuthority(), SDK_TEST_EXPIRATION_TIME(), "Test memo");
        for (final FileCreateTransaction transaction : List.of(withAttributes, new FileCreateTransaction(new byte[0], null, null, null))) {
            // given
            final PackedTransaction<FileCreateReceipt, FileCreateTransaction> packed = transaction.pack(payer, nodes);

            // when
            final PackedTransaction<?, ?> restored = PackedTransaction.fromBytes(packed.toBytes());

            // then
            assertEquals(packed.toBytes(), restored.toBytes());
            assertEquals(packed.signableBodies(), restored.signableBodies());
        }
    }

    /// `consensusnode.transactions.files/a-file-append-transaction-keeps-its-file-and-contents-and-survives-tobytes-and-frombytes`
    ///
    /// Source: the Python SDK (test_constructor_with_parameters of FileAppendTransaction: file 0.0.12345,
    /// "Test append content"), the Java SDK (FileAppendTransactionTest.shouldBytes) and the Swift SDK
    /// (FileAppendTransactionUnitTests.test_GetSetFileId, test_GetSetContents and test_ToFromBytes).
    @Test
    @DisplayName("a file append transaction keeps its file and contents and survives toBytes and fromBytes")
    @Tag("testId:consensusnode.transactions.files/a-file-append-transaction-keeps-its-file-and-contents-and-survives-tobytes-and-frombytes")
    void aFileAppendTransactionKeepsItsFileAndContentsAndSurvivesToBytesAndFromBytes() {
        // given
        final Address fileId = Address.fromString("0.0.12345");
        final byte[] contents = utf8("Test append content");
        final FileAppendTransaction transaction = new FileAppendTransaction(fileId, contents);

        // when
        final PackedTransaction<FileAppendReceipt, FileAppendTransaction> packed = transaction.pack(defaultAccount(), List.of(AccountId.fromString("0.0.3")));
        final PackedTransaction<?, ?> restored = PackedTransaction.fromBytes(packed.toBytes());

        // then
        assertEquals(fileId, transaction.fileId());
        assertEquals(contents, transaction.contents());
        assertEquals(1L, length(packed.signableBodies()));
        assertEquals(packed.signableBodies(), restored.signableBodies());
    }

    /// `consensusnode.transactions.files/a-file-update-transaction-changes-nothing-but-what-it-names`
    ///
    /// Source: this spec (every nullable field of an update is "leave unchanged"); the Python SDK
    /// (test_constructor_without_parameters of FileUpdateTransaction).
    @Test
    @DisplayName("a file update transaction changes nothing but what it names")
    @Tag("testId:consensusnode.transactions.files/a-file-update-transaction-changes-nothing-but-what-it-names")
    void aFileUpdateTransactionChangesNothingButWhatItNames() {
        // when
        final FileUpdateTransaction transaction = new FileUpdateTransaction(defaultAddress(), null, null, null, null);

        // then
        assertEquals(defaultAddress(), transaction.fileId());
        assertNull(transaction.contents());
        assertNull(transaction.authority());
        assertNull(transaction.expirationTime());
        assertNull(transaction.fileMemo());
    }

    /// `consensusnode.transactions.files/a-file-update-transaction-keeps-its-contents-authority-expiration-time-and-memo`
    ///
    /// Source: the Python SDK (test_constructor_with_parameters of FileUpdateTransaction: "Updated content", "Updated memo")
    /// and the Swift SDK (FileUpdateTransactionUnitTests.test_GetSetExpirationTime); the expiration time of the Java SDK
    /// (FileUpdateTransactionTest: Instant.ofEpochSecond(1554158728) = 2019-04-01T22:45:28Z).
    @Test
    @DisplayName("a file update transaction keeps its contents, authority, expiration time and memo")
    @Tag("testId:consensusnode.transactions.files/a-file-update-transaction-keeps-its-contents-authority-expiration-time-and-memo")
    void aFileUpdateTransactionKeepsItsContentsAuthorityExpirationTimeAndMemo() {
        // given
        final byte[] contents = utf8("Updated content");

        // when
        final FileUpdateTransaction transaction = new FileUpdateTransaction(defaultAddress(), contents, defaultAuthority(), SDK_TEST_EXPIRATION_TIME(), "Updated memo");

        // then
        assertEquals(contents, transaction.contents());
        assertEquals(defaultAuthority(), transaction.authority());
        assertEquals(SDK_TEST_EXPIRATION_TIME(), transaction.expirationTime());
        assertEquals("Updated memo", transaction.fileMemo());
    }

    /// `consensusnode.transactions.files/a-file-update-transaction-survives-tobytes-and-frombytes`
    ///
    /// Source: the Python SDK (test_constructor_with_parameters of FileUpdateTransaction), the Java SDK
    /// (FileUpdateTransactionTest.shouldBytes: expiration time Instant.ofEpochSecond(1554158728) = 2019-04-01T22:45:28Z)
    /// and the Swift SDK (FileUpdateTransactionUnitTests.test_ToFromBytes).
    @Test
    @DisplayName("a file update transaction survives toBytes and fromBytes")
    @Tag("testId:consensusnode.transactions.files/a-file-update-transaction-survives-tobytes-and-frombytes")
    void aFileUpdateTransactionSurvivesToBytesAndFromBytes() {
        // given
        final PackedTransaction<FileUpdateReceipt, FileUpdateTransaction> packed = new FileUpdateTransaction(defaultAddress(), utf8("Updated content"), defaultAuthority(), SDK_TEST_EXPIRATION_TIME(), "Updated memo").pack(defaultAccount(), List.of(AccountId.fromString("0.0.3")));

        // when
        final PackedTransaction<?, ?> restored = PackedTransaction.fromBytes(packed.toBytes());

        // then
        assertEquals(packed.toBytes(), restored.toBytes());
        assertEquals(packed.signableBodies(), restored.signableBodies());
    }

    /// `consensusnode.transactions.files/a-file-delete-transaction-keeps-its-file-and-survives-tobytes-and-frombytes`
    ///
    /// Source: the Python SDK (test_constructor_with_file_id of FileDeleteTransaction), the Java SDK
    /// (FileDeleteTransactionTest.shouldBytes) and the Swift SDK (FileDeleteTransactionUnitTests.test_ToFromBytes).
    @Test
    @DisplayName("a file delete transaction keeps its file and survives toBytes and fromBytes")
    @Tag("testId:consensusnode.transactions.files/a-file-delete-transaction-keeps-its-file-and-survives-tobytes-and-frombytes")
    void aFileDeleteTransactionKeepsItsFileAndSurvivesToBytesAndFromBytes() {
        // given
        final FileDeleteTransaction transaction = new FileDeleteTransaction(defaultAddress());

        // when
        final PackedTransaction<FileDeleteReceipt, FileDeleteTransaction> packed = transaction.pack(defaultAccount(), List.of(AccountId.fromString("0.0.3")));
        final PackedTransaction<?, ?> restored = PackedTransaction.fromBytes(packed.toBytes());

        // then
        assertEquals(defaultAddress(), transaction.fileId());
        assertEquals(packed.signableBodies(), restored.signableBodies());
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

    private static Account defaultAccount() {
        return new Account(defaultAccountId(), defaultPrivateKey());
    }

    private static AccountId defaultAccountId() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static Address defaultAddress() {
        return new Address(0L, 0L, "", 1001L);
    }
}
