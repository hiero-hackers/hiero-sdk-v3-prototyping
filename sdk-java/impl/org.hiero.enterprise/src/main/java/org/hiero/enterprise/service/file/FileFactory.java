package org.hiero.enterprise.service.file;

import org.hiero.enterprise.service.Session;
import org.hiero.enterprise.service.internal.DefaultSession;
import org.hiero.enterprise.service.internal.DefaultFileService;

/// Factory methods of the package `org.hiero.enterprise.service.file`.
public final class FileFactory {

    /// Creates the service for the given session. With a framework integration the service is usually obtained via
    /// dependency injection instead.
    public static FileService createService(final Session session) {
        return new DefaultFileService(DefaultSession.of(session));
    }

    private FileFactory() {
        throw new UnsupportedOperationException("Factory class cannot be instantiated");
    }
}
