package org.hiero.tck.runtime;

import java.util.Map;

/**
 * A JSON-RPC error with code and data. The codes follow the TCK conventions: {@link #METHOD_NOT_FOUND} for an unknown
 * method (the TCK skips its tests), {@link #HIERO_ERROR} for a request the network rejected, {@link #INTERNAL_ERROR}
 * for everything the SDK rejected before sending (or could not do).
 */
public final class RpcError extends RuntimeException {

    /** The error code of a request the network rejected (precheck or receipt status). */
    public static final int HIERO_ERROR = -32001;
    /** The error code of a request the SDK rejected or could not process. */
    public static final int INTERNAL_ERROR = -32603;
    /** The error code of an unknown method. */
    public static final int METHOD_NOT_FOUND = -32601;
    /** The error code of a message that is no JSON-RPC request. */
    public static final int INVALID_REQUEST = -32600;

    private static final long serialVersionUID = 1L;

    private final int code;
    private final transient Map<String, Object> data;

    /**
     * Creates an error.
     *
     * @param code    the JSON-RPC error code
     * @param message the message
     * @param data    the data object
     */
    public RpcError(final int code, final String message, final Map<String, Object> data) {
        super(message);
        this.code = code;
        this.data = Map.copyOf(data);
    }

    /**
     * Returns the error for something the API cannot do yet (a gap of the specs).
     *
     * @param message what is missing
     * @return the error
     */
    public static RpcError gap(final String message) {
        return internal("not supported by the API: " + message);
    }

    /**
     * Returns an internal error.
     *
     * @param message the message in {@code data.message}
     * @return the error
     */
    public static RpcError internal(final String message) {
        return new RpcError(INTERNAL_ERROR, "Internal error", Map.of("message", message));
    }

    /** Returns the error code. */
    public int code() {
        return code;
    }

    /** Returns the data object. */
    public Map<String, Object> data() {
        return data;
    }
}
