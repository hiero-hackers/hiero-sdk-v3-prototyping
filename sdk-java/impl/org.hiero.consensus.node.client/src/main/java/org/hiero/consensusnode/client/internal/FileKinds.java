package org.hiero.consensusnode.client.internal;

import com.google.protobuf.ByteString;
import com.google.protobuf.StringValue;
import com.hederahashgraph.api.proto.java.FileAppendTransactionBody;
import com.hederahashgraph.api.proto.java.FileCreateTransactionBody;
import com.hederahashgraph.api.proto.java.FileDeleteTransactionBody;
import com.hederahashgraph.api.proto.java.FileUpdateTransactionBody;
import com.hederahashgraph.api.proto.java.Key;
import com.hederahashgraph.api.proto.java.KeyList;
import com.hederahashgraph.api.proto.java.Timestamp;
import com.hederahashgraph.api.proto.java.TransactionBody;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import org.hiero.authority.Authority;
import org.hiero.authority.AuthorityList;
import org.hiero.consensusnode.transactions.files.FileAppendReceipt;
import org.hiero.consensusnode.transactions.files.FileAppendTransaction;
import org.hiero.consensusnode.transactions.files.FileCreateReceipt;
import org.hiero.consensusnode.transactions.files.FileCreateTransaction;
import org.hiero.consensusnode.transactions.files.FileDeleteReceipt;
import org.hiero.consensusnode.transactions.files.FileDeleteTransaction;
import org.hiero.consensusnode.transactions.files.FileUpdateReceipt;
import org.hiero.consensusnode.transactions.files.FileUpdateTransaction;
import org.hiero.ledger.Address;
import org.jspecify.annotations.Nullable;

/// The transaction types of files.
public final class FileKinds {

    /// The most bytes one `FileAppend` transaction carries - the chunk size of the Hiero SDKs. A packed transaction has
    /// one transaction id, so a larger append (several chunks, each with its own transaction id) cannot be packed.
    public static final int MAX_APPEND_CHUNK_SIZE = 4096;

    /// The lifetime of a new file without an expiration time, counted from the valid start of the transaction - the
    /// default of the Hiero SDKs (90 days).
    private static final Duration DEFAULT_FILE_LIFETIME = Duration.ofDays(90);

    /// The address of a file the receipt names none of (a failed create).
    private static final Address NO_FILE = new Address(0L, 0L, "", 0L);

    /// `FileCreateTransaction`: `FileService.createFile`.
    public static final TransactionKind<FileCreateReceipt, FileCreateTransaction> FILE_CREATE = new TransactionKind<>(
            FileCreateTransaction.class, "proto.FileService", "createFile", TransactionBody.DataCase.FILECREATE,
            FileKinds::fileCreate,
            (id, status, rate, nextRate, receipt) -> new FileCreateReceipt(id, status, rate, nextRate,
                    receipt.hasFileID() ? Protobuf.fromProto(receipt.getFileID()) : NO_FILE));

    /// `FileAppendTransaction`: `FileService.appendContent`.
    public static final TransactionKind<FileAppendReceipt, FileAppendTransaction> FILE_APPEND = new TransactionKind<>(
            FileAppendTransaction.class, "proto.FileService", "appendContent", TransactionBody.DataCase.FILEAPPEND,
            FileKinds::fileAppend,
            (id, status, rate, nextRate, receipt) -> new FileAppendReceipt(id, status, rate, nextRate));

    /// `FileUpdateTransaction`: `FileService.updateFile`.
    public static final TransactionKind<FileUpdateReceipt, FileUpdateTransaction> FILE_UPDATE = new TransactionKind<>(
            FileUpdateTransaction.class, "proto.FileService", "updateFile", TransactionBody.DataCase.FILEUPDATE,
            FileKinds::fileUpdate,
            (id, status, rate, nextRate, receipt) -> new FileUpdateReceipt(id, status, rate, nextRate));

    /// `FileDeleteTransaction`: `FileService.deleteFile`.
    public static final TransactionKind<FileDeleteReceipt, FileDeleteTransaction> FILE_DELETE = new TransactionKind<>(
            FileDeleteTransaction.class, "proto.FileService", "deleteFile", TransactionBody.DataCase.FILEDELETE,
            (transaction, body) -> body.setFileDelete(FileDeleteTransactionBody.newBuilder()
                    .setFileID(Protobuf.toFileId(transaction.fileId()))),
            (id, status, rate, nextRate, receipt) -> new FileDeleteReceipt(id, status, rate, nextRate));

    /// All transaction types of this class.
    public static final List<TransactionKind<?, ?>> ALL = List.of(FILE_CREATE, FILE_APPEND, FILE_UPDATE, FILE_DELETE);

    private FileKinds() {
    }

    /// Maps the write-access authority of a file to the `KeyList` of HAPI, whose keys must all sign. An all-must-sign
    /// `AuthorityList` becomes the list of its children, any other authority a list of one key.
    ///
    /// @param authority the authority
    /// @return the key list
    static KeyList toKeyList(final Authority authority) {
        final KeyList.Builder keys = KeyList.newBuilder();
        if (authority instanceof AuthorityList(List<Authority> children, int threshold)
                && threshold == children.size()) {
            children.forEach(child -> keys.addKeys(Protobuf.toProto(child)));
        } else {
            keys.addKeys(Protobuf.toProto(authority));
        }
        return keys.build();
    }

    /// Maps the `KeyList` of a file back to its authority: no key is no authority (an immutable file), one key is that
    /// key's authority, several keys an all-must-sign `AuthorityList`.
    ///
    /// @param keys the key list
    /// @return the authority, `null` for an empty list
    static @Nullable Authority fromKeyList(final KeyList keys) {
        final List<Key> list = keys.getKeysList();
        if (list.isEmpty()) {
            return null;
        }
        if (list.size() == 1) {
            return Protobuf.fromProto(list.getFirst());
        }
        final List<Authority> children = list.stream().map(Protobuf::fromProto).toList();
        return new AuthorityList(children, children.size());
    }

    private static void fileCreate(final FileCreateTransaction transaction, final TransactionBody.Builder body) {
        final FileCreateTransactionBody.Builder create = FileCreateTransactionBody.newBuilder()
                .setContents(ByteString.copyFrom(transaction.contents()));
        final Authority authority = transaction.authority();
        if (authority != null) {
            create.setKeys(toKeyList(authority));
        }
        final ZonedDateTime expirationTime = transaction.expirationTime();
        if (expirationTime != null) {
            create.setExpirationTime(Protobuf.toProto(expirationTime));
        } else {
            final Timestamp validStart = body.getTransactionID().getTransactionValidStart();
            create.setExpirationTime(Timestamp.newBuilder()
                    .setSeconds(validStart.getSeconds() + DEFAULT_FILE_LIFETIME.toSeconds()));
        }
        final String memo = transaction.fileMemo();
        if (memo != null) {
            create.setMemo(memo);
        }
        body.setFileCreate(create);
    }

    private static void fileAppend(final FileAppendTransaction transaction, final TransactionBody.Builder body) {
        final byte[] contents = transaction.contents();
        if (contents.length > MAX_APPEND_CHUNK_SIZE) {
            throw new IllegalArgumentException("The contents of a FileAppendTransaction are " + contents.length
                    + " bytes; appends of more than " + MAX_APPEND_CHUNK_SIZE + " bytes need several transactions"
                    + " (chunks), which cannot be packed into one PackedTransaction yet");
        }
        body.setFileAppend(FileAppendTransactionBody.newBuilder()
                .setFileID(Protobuf.toFileId(transaction.fileId()))
                .setContents(ByteString.copyFrom(contents)));
    }

    private static void fileUpdate(final FileUpdateTransaction transaction, final TransactionBody.Builder body) {
        final FileUpdateTransactionBody.Builder update = FileUpdateTransactionBody.newBuilder()
                .setFileID(Protobuf.toFileId(transaction.fileId()));
        final byte[] contents = transaction.contents();
        if (contents != null) {
            update.setContents(ByteString.copyFrom(contents));
        }
        final Authority authority = transaction.authority();
        if (authority != null) {
            update.setKeys(toKeyList(authority));
        }
        final ZonedDateTime expirationTime = transaction.expirationTime();
        if (expirationTime != null) {
            update.setExpirationTime(Protobuf.toProto(expirationTime));
        }
        final String memo = transaction.fileMemo();
        if (memo != null) {
            update.setMemo(StringValue.of(memo));
        }
        body.setFileUpdate(update);
    }
}
