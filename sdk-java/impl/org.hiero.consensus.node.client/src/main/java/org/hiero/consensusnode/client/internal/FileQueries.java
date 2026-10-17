package org.hiero.consensusnode.client.internal;

import com.hederahashgraph.api.proto.java.FileGetContentsQuery;
import com.hederahashgraph.api.proto.java.FileGetContentsResponse;
import com.hederahashgraph.api.proto.java.FileGetInfoQuery;
import com.hederahashgraph.api.proto.java.FileGetInfoResponse;
import com.hederahashgraph.api.proto.java.Query;
import org.hiero.consensusnode.queries.files.FileContents;
import org.hiero.consensusnode.queries.files.FileContentsQuery;
import org.hiero.consensusnode.queries.files.FileInfo;
import org.hiero.consensusnode.queries.files.FileInfoQuery;

/// The query types of files.
public final class FileQueries {

    /// `FileInfoQuery`: `FileService.getFileInfo`.
    public static final QueryKind<FileInfoQuery, FileInfo> FILE_INFO = new QueryKind<>(
            "proto.FileService", "getFileInfo",
            (query, header) -> Query.newBuilder().setFileGetInfo(FileGetInfoQuery.newBuilder().setHeader(header)
                    .setFileID(Protobuf.toFileId(query.fileId()))).build(),
            response -> response.getFileGetInfo().getHeader(),
            (query, response) -> fileInfo(response.getFileGetInfo().getFileInfo()));

    /// `FileContentsQuery`: `FileService.getFileContent`.
    public static final QueryKind<FileContentsQuery, FileContents> FILE_CONTENTS = new QueryKind<>(
            "proto.FileService", "getFileContent",
            (query, header) -> Query.newBuilder().setFileGetContents(FileGetContentsQuery.newBuilder()
                    .setHeader(header).setFileID(Protobuf.toFileId(query.fileId()))).build(),
            response -> response.getFileGetContents().getHeader(),
            (query, response) -> {
                final FileGetContentsResponse.FileContents contents = response.getFileGetContents()
                        .getFileContents();
                return new FileContents(contents.hasFileID() ? Protobuf.fromProto(contents.getFileID())
                        : query.fileId(), contents.getContents().toByteArray());
            });

    private FileQueries() {
    }

    private static FileInfo fileInfo(final FileGetInfoResponse.FileInfo info) {
        return new FileInfo(Protobuf.fromProto(info.getFileID()), info.getSize(),
                Protobuf.fromProto(info.getExpirationTime()), info.getDeleted(),
                info.hasKeys() ? FileKinds.fromKeyList(info.getKeys()) : null,
                info.getMemo().isEmpty() ? null : info.getMemo());
    }
}
