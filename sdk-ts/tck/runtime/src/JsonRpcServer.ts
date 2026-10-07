import { createServer, type Server } from "node:http";
import type { AddressInfo } from "node:net";
import type { Handler, JsonObject, TckServer } from "@hiero/tck-contract";
import { RpcError } from "./RpcError.js";
import { ServerLog } from "./ServerLog.js";
import { RuntimeSession } from "./RuntimeSession.js";

/**
 * The JSON-RPC 2.0 server that the TCK test driver talks to (by default on port 8544). Every request carries the
 * `sessionId` of its test file, so each test file gets its own client ({@link RuntimeSession}).
 */
export class JsonRpcServer implements TckServer {

    readonly #handlers: ReadonlyMap<string, Handler>;
    readonly #sessions = new Map<string, RuntimeSession>();
    readonly #log = new ServerLog();
    #server: Server | null = null;

    constructor(handlers: ReadonlyMap<string, Handler>) {
        this.#handlers = new Map(handlers);
    }

    async handle(request: string): Promise<string> {
        let id: unknown = null;
        let name: string | null = null;
        try {
            let message: unknown;
            try {
                message = JSON.parse(request);
            } catch {
                return error(null, RpcError.INVALID_REQUEST, "Invalid Request", {});
            }
            if (message === null || typeof message !== "object" || Array.isArray(message)) {
                return error(null, RpcError.INVALID_REQUEST, "Invalid Request", {});
            }
            const call = message as Record<string, unknown>;
            id = call["id"] ?? null;
            const method = call["method"];
            name = typeof method === "string" ? method : null;
            const handler = name !== null ? this.#handlers.get(name) : undefined;
            if (handler === undefined) {
                this.#log.failed(name, RpcError.METHOD_NOT_FOUND, "no binding for this TCK method", null);
                return error(id, RpcError.METHOD_NOT_FOUND, "Method not found", { method: String(method) });
            }
            const params: JsonObject = call["params"] !== null && typeof call["params"] === "object"
                ? call["params"] as JsonObject : {};
            const sessionId = String(params["sessionId"] ?? "");
            let session = this.#sessions.get(sessionId);
            if (session === undefined) {
                session = new RuntimeSession(sessionId);
                this.#sessions.set(sessionId, session);
            }
            const result = await handler(params, session);
            return JSON.stringify({ jsonrpc: "2.0", id, result });
        } catch (e) {
            if (e instanceof RpcError) {
                this.#log.failed(name, e.code, String(e.data["message"]), e);
                return error(id, e.code, e.message, e.data);
            }
            const cause = e instanceof Error ? `${e.name}: ${e.message}` : String(e);
            this.#log.failed(name, RpcError.INTERNAL_ERROR, cause, e);
            return error(id, RpcError.INTERNAL_ERROR, "Internal error", { message: cause });
        }
    }

    start(port: number): Promise<number> {
        const server = createServer((request, response) => {
            const chunks: Buffer[] = [];
            request.on("data", (chunk: Buffer) => chunks.push(chunk));
            request.on("end", () => {
                void this.handle(Buffer.concat(chunks).toString("utf8")).then((body) => {
                    response.writeHead(200, { "Content-Type": "application/json" });
                    response.end(body);
                });
            });
        });
        this.#server = server;
        // the generated main never calls stop(): the server runs until it is killed. A listener replaces Node's
        // default handler, so the process has to exit itself.
        for (const signal of ["SIGTERM", "SIGINT"] as const) {
            process.once(signal, () => {
                this.#log.summary();
                process.exit(signal === "SIGTERM" ? 143 : 130);
            });
        }
        return new Promise((resolve) => {
            server.listen(port, () => resolve((server.address() as AddressInfo).port));
        });
    }

    stop(): Promise<void> {
        const server = this.#server;
        this.#log.summary();
        return new Promise((resolve) => {
            if (server === null) {
                resolve();
            } else {
                server.close(() => resolve());
            }
        });
    }
}

function error(id: unknown, code: number, message: string, data: Readonly<Record<string, unknown>>): string {
    return JSON.stringify({ jsonrpc: "2.0", id, error: { code, message, data } });
}
