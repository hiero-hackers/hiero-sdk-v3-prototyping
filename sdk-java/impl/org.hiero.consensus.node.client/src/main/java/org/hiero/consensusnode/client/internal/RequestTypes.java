package org.hiero.consensusnode.client.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import org.hiero.consensusnode.client.RequestTypeConflictException;
import org.hiero.consensusnode.requestTypes.RequestType;
import org.hiero.consensusnode.transactions.spi.RequestTypeSupport;
import org.hiero.consensusnode.transactions.spi.UnknownRequestTypeException;
import org.jspecify.annotations.Nullable;

/// The request type of a HAPI `HederaFunctionality` code: the request type of the registered `RequestTypeSupport` that
/// names it. The support of `BasicRequestType` is always registered (first); the others come from the `ServiceLoader`.
/// The supports are loaded once; two request types with the same code are a conflict, which every later call reports
/// again.
public final class RequestTypes {

    private static final Object LOCK = new Object();

    private static @Nullable Map<Integer, RequestType> byCode;

    private static @Nullable Set<RequestTypeSupport> supports;

    private static @Nullable RequestTypeConflictException conflict;

    private RequestTypes() {
    }

    /// Loads the registered supports, if not done yet.
    ///
    /// @throws RequestTypeConflictException if two request types name the same code
    public static void load() {
        requestTypes();
    }

    /// Returns the registered supports.
    ///
    /// @return the supports
    /// @throws RequestTypeConflictException if two request types name the same code
    public static Set<RequestTypeSupport> supports() {
        requestTypes();
        return supports;
    }

    /// Returns the request type of a code.
    ///
    /// @param code the HAPI code
    /// @return the request type
    /// @throws UnknownRequestTypeException if no registered support names the code
    /// @throws RequestTypeConflictException if two request types name the same code
    public static RequestType ofCode(final int code) {
        return find(code).orElseThrow(() -> new UnknownRequestTypeException("No request type is known for the code "
                + code + "; register a RequestTypeSupport that names it", null));
    }

    /// Returns the request type of a code, if a registered support names it.
    ///
    /// @param code the HAPI code
    /// @return the request type, empty if no registered support names the code
    /// @throws RequestTypeConflictException if two request types name the same code
    public static Optional<RequestType> find(final int code) {
        return Optional.ofNullable(requestTypes().get(code));
    }

    /// The name of a request type: the name of its enum constant, or `REQUEST_TYPE_<code>` for a request type that is
    /// no enum constant.
    ///
    /// @param requestType the request type
    /// @return the name
    public static String name(final RequestType requestType) {
        return requestType instanceof Enum<?> constant ? constant.name() : "REQUEST_TYPE_" + requestType.code();
    }

    private static Map<Integer, RequestType> requestTypes() {
        synchronized (LOCK) {
            if (conflict != null) {
                throw conflict;
            }
            if (byCode == null) {
                final Map<Integer, RequestType> requestTypes = new HashMap<>();
                final Set<RequestTypeSupport> loaded = new LinkedHashSet<>();
                // the request types of the protocol first, then those of the other modules; one support per class
                final List<RequestTypeSupport> candidates = new ArrayList<>();
                candidates.add(new BasicRequestTypeSupport());
                ServiceLoader.load(RequestTypeSupport.class).forEach(candidates::add);
                final Set<Class<?>> classes = new HashSet<>();
                for (final RequestTypeSupport support : candidates) {
                    if (!classes.add(support.getClass())) {
                        continue;
                    }
                    loaded.add(support);
                    for (final RequestType requestType : support.getRequestTypes()) {
                        final RequestType other = requestTypes.putIfAbsent(requestType.code(), requestType);
                        if (other != null) {
                            conflict = new RequestTypeConflictException("The request type code " + requestType.code()
                                    + " is named twice: " + name(other) + " (" + other.getClass().getName() + ") and "
                                    + name(requestType) + " (" + requestType.getClass().getName() + ")", null);
                            throw conflict;
                        }
                    }
                }
                byCode = Map.copyOf(requestTypes);
                supports = Set.copyOf(loaded);
            }
            return byCode;
        }
    }
}
