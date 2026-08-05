package org.hiero.sdk.v3.codegen.mapping;

import java.util.List;
import org.hiero.sdk.v3.codegen.render.JavaSourceFile;

public final class CommonMapping {
    public List<JavaSourceFile> sourceFiles() {
        return List.of(
                MappingSources.type("common", "Page", """
                        /** An immutable page of asynchronously navigable mirror-node data. */
                        public interface Page<T> {
                            /** Returns an immutable ordered data snapshot. */
                            List<T> data();
                            /** Returns the number of elements in this page. */
                            int size();
                            /** Returns the zero-based page index. */
                            int pageIndex();
                            /** Returns whether a next page exists. */
                            boolean hasNext();
                            /** Returns whether this is the first page. */
                            boolean isFirst();
                            /** Returns the next page without blocking; mirror-node failures complete exceptionally. */
                            CompletionStage<Page<T>> next();
                            /** Returns the first page without blocking; mirror-node failures complete exceptionally. */
                            CompletionStage<Page<T>> first();
                        }
                        """, "java.util.List", "java.util.concurrent.CompletionStage"),
                MappingSources.type("common", "MirrorNodeException", """
                        /** Indicates a terminal mirror-node page retrieval failure. */
                        public class MirrorNodeException extends RuntimeException {
                            /** Creates an exception with a message. */
                            public MirrorNodeException(String message) { super(message); }
                            /** Creates an exception with a message and cause. */
                            public MirrorNodeException(String message, Throwable cause) { super(message, cause); }
                        }
                        """));
    }
}
