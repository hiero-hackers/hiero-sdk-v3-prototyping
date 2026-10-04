import { createServer, type Server } from "node:http";
import type { AddressInfo } from "node:net";
import type { Handler, JsonObject, TckServer } from "@hiero/tck-contract";
import { RpcError } from "./RpcError.js";
import { RuntimeSession } from "./RuntimeSession.js";

/**
 * The JSON-RPC 2.0 server that the TCK test driver talks to (by default on port 8544). Every request carries the
 * `sessionId` of its test file, so each test file gets its own client ({@link RuntimeSession}).
 */
export class JsonRpcServer implements TckServer {

    readonly #handlers: ReadonlyMap<string, Handler>;
    readonly #sessions = new Map<string, RuntimeSession>();
    #server: Server | null = null;

    constructor(handlers: ReadonlyMap<string, Handler>) {
        this.#handlers = new Map(handlers);
    }

    async handle(request: string): Promise<string> {
        let id: unknown = null;
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
            const handler = typeof method === "string" ? this.#handlers.get(method) : undefined;
            if (handler === undefined) {
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
                return error(id, e.code, e.message, e.data);
            }
            const cause = e instanceof Error ? `${e.name}: ${e.message}` : String(e);
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
        return new Promise((resolve) => {
            server.listen(port, () => resolve((server.address() as AddressInfo).port));
        });
    }

    stop(): Promise<void> {
        const server = this.#server;
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
