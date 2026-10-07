/**
 * A JSON-RPC error with code and data. The codes follow the TCK conventions: {@link RpcError.METHOD_NOT_FOUND} for an
 * unknown method (the TCK skips its tests), {@link RpcError.HIERO_ERROR} for a request the network rejected,
 * {@link RpcError.INTERNAL_ERROR} for everything the SDK rejected before sending (or could not do).
 */
export class RpcError extends Error {

    /** The error code of a request the network rejected (precheck or receipt status). */
    static readonly HIERO_ERROR = -32001;
    /** The error code of a request the SDK rejected or could not process. */
    static readonly INTERNAL_ERROR = -32603;
    /** The error code of an unknown method. */
    static readonly METHOD_NOT_FOUND = -32601;
    /** The error code of a message that is no JSON-RPC request. */
    static readonly INVALID_REQUEST = -32600;

    readonly code: number;
    readonly data: Readonly<Record<string, unknown>>;

    constructor(code: number, message: string, data: Readonly<Record<string, unknown>>) {
        super(message);
        this.code = code;
        this.data = data;
    }

    /** Returns an internal error with `data.message`. */
    static internal(message: string): RpcError {
        return new RpcError(RpcError.INTERNAL_ERROR, "Internal error", { message });
    }

    /** Returns the error for something the API cannot do yet (a gap of the specs). */
    static gap(message: string): RpcError {
        return RpcError.internal("not supported by the API: " + message);
    }
}
