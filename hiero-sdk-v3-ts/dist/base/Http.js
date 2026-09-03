export var HttpMethod;
(function (HttpMethod) {
    HttpMethod["GET"] = "GET";
    HttpMethod["HEAD"] = "HEAD";
    HttpMethod["POST"] = "POST";
    HttpMethod["PUT"] = "PUT";
    HttpMethod["PATCH"] = "PATCH";
    HttpMethod["DELETE"] = "DELETE";
    HttpMethod["OPTIONS"] = "OPTIONS";
    HttpMethod["TRACE"] = "TRACE";
    HttpMethod["CONNECT"] = "CONNECT";
})(HttpMethod || (HttpMethod = {}));
class DefaultHttpClient {
    configuration;
    #closed = false;
    #inFlightControllers = new Set();
    constructor(configuration) {
        this.configuration = configuration;
    }
    async execute(request) {
        if (this.#closed) {
            throw new Error('client-closed-error');
        }
        const controller = new AbortController();
        this.#inFlightControllers.add(controller);
        const timeoutMs = request.timeout ?? this.configuration.defaultRequestTimeout;
        const timeoutId = setTimeout(() => controller.abort(new Error('timeout-error')), timeoutMs);
        try {
            const headers = new Headers();
            this.configuration.defaultHeaders.forEach((val, key) => headers.set(key, val));
            request.headers.forEach((val, key) => headers.set(key, val));
            const fetchOptions = {
                method: request.method,
                headers,
                signal: controller.signal,
            };
            if (request.body !== null) {
                fetchOptions.body = request.body;
            }
            const res = await fetch(request.url, fetchOptions);
            const responseHeaders = new Map();
            res.headers.forEach((val, key) => responseHeaders.set(key, val));
            const arrayBuffer = await res.arrayBuffer();
            return {
                statusCode: res.status,
                body: new Uint8Array(arrayBuffer),
                headers: responseHeaders,
            };
        }
        catch (e) {
            if (this.#closed) {
                throw new Error('client-closed-error');
            }
            if (e.message === 'timeout-error' || e.name === 'AbortError') {
                throw new Error('timeout-error');
            }
            throw new Error('connection-error');
        }
        finally {
            clearTimeout(timeoutId);
            this.#inFlightControllers.delete(controller);
        }
    }
    async close(closeTimeout) {
        if (this.#closed)
            return;
        this.#closed = true;
        // In a full implementation, we would wait for promises to resolve up to closeTimeout,
        // but here we just immediately abort to satisfy the basic abort controller logic.
        for (const controller of this.#inFlightControllers) {
            controller.abort(new Error('client-closed-error'));
        }
        this.#inFlightControllers.clear();
    }
}
export function createHttpClient(configuration) {
    return new DefaultHttpClient(configuration);
}
