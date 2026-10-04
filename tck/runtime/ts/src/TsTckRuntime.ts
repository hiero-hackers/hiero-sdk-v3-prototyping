import type { NodeSignature, TransactionSigner } from "@hiero/consensus-node-client/consensusnode/client";
import { NodeSignature as Signature } from "@hiero/consensus-node-client/consensusnode/client";
import type { Query, QueryResponse } from "@hiero/consensus-node-client/consensusnode/queries";
import type { PackedTransaction, Receipt, Transaction } from "@hiero/consensus-node-client/consensusnode/transactions";
import type { PrivateKey } from "@hiero/base/keys";
import type { Handler, JsonObject, Session, Source, TckRuntime, TckServer } from "@hiero/tck-contract";
import { JsonRpcServer } from "./JsonRpcServer.js";
import { RpcError } from "./RpcError.js";
import { RuntimeSession } from "./RuntimeSession.js";
import { TsConverters } from "./TsConverters.js";
import { Utilities } from "./Utilities.js";

/**
 * The HAPI code of `SUCCESS`. `BasicTransactionStatus` has no such constant yet (a gap of the specs), so the code is
 * used.
 */
const SUCCESS = 22;

/**
 * The TypeScript runtime of the TCK server: access to the JSON parameters, the execution flow (a transaction is signed
 * by the operator and the additional signers, sent and answered with its receipt; a query is sent and answered with
 * the value of its response) and the JSON-RPC server with the runtime methods `setup`, `reset` and `generateKey`.
 */
export class TsTckRuntime implements TckRuntime {

    readonly converters: TsConverters = new TsConverters();

    // --- JSON parameters -------------------------------------------------------------------------

    value<T>(json: JsonObject, sources: ReadonlyArray<Source<T>>): T | null {
        for (const source of sources) {
            const value = get(json, source.path);
            if (value !== null && value !== undefined) {
                return source.converter(value);
            }
        }
        return null;
    }

    required<T>(value: T | null, parameters: string): T {
        if (value === null || value === undefined) {
            throw RpcError.internal("the API requires a value for " + parameters);
        }
        return value;
    }

    or<T>(value: T | null, defaultValue: T): T {
        return value === null || value === undefined ? defaultValue : value;
    }

    object(json: JsonObject, path: string): JsonObject | null {
        const value = get(json, path);
        return value !== null && typeof value === "object" && !Array.isArray(value) ? value as JsonObject : null;
    }

    objects(json: JsonObject, path: string): ReadonlyArray<JsonObject> {
        const value = get(json, path);
        return Array.isArray(value)
            ? value.filter((e): e is JsonObject => e !== null && typeof e === "object" && !Array.isArray(e)) : [];
    }

    unsupported(json: JsonObject, name: string, reason: string): void {
        if (json[name] !== null && json[name] !== undefined) {
            throw RpcError.gap(`${name}: ${reason}`);
        }
    }

    // --- execution -------------------------------------------------------------------------------

    async transaction<R extends Receipt>(session: Session, transaction: Transaction<R, any>,
                                         common: JsonObject | null): Promise<R> {
        const client = RuntimeSession.of(session).client;
        let packed: PackedTransaction<R, any> = transaction.signWithOperator(client);
        const signers = common === null ? undefined : common["signers"];
        if (Array.isArray(signers)) {
            for (const signer of signers) {
                packed = packed.sign(signerOf(this.converters.privateKey(signer)));
            }
        }
        const response = await packed.submit(client);
        const receipt = await response.queryReceipt();
        if (receipt.status.code !== SUCCESS) {
            throw new RpcError(RpcError.HIERO_ERROR, "Hiero error", { status: this.converters.statusJson(receipt.status) });
        }
        return receipt;
    }

    async query<T>(session: Session, query: Query<any, QueryResponse<T>>): Promise<T> {
        return (await query.submit(RuntimeSession.of(session).client)).value;
    }

    put<T>(result: Record<string, unknown>, path: string, value: T | null | undefined,
           converter: (value: T) => unknown): void {
        if (value === null || value === undefined) {
            return;
        }
        const segments = path.split(".");
        let target = result;
        for (const segment of segments.slice(0, -1)) {
            const next = target[segment];
            if (next === null || typeof next !== "object") {
                target[segment] = {};
            }
            target = target[segment] as Record<string, unknown>;
        }
        target[segments[segments.length - 1] ?? path] = converter(value);
    }

    // --- server ----------------------------------------------------------------------------------

    server(methods: ReadonlyMap<string, Handler>): TckServer {
        const all = new Map(methods);
        const utilities = new Utilities(this, this.converters);
        all.set("setup", (params, session) => utilities.setup(params, session));
        all.set("reset", (params, session) => utilities.reset(params, session));
        all.set("generateKey", (params, session) => utilities.generateKey(params, session));
        return new JsonRpcServer(all);
    }
}

/** Returns the value at a path (`a.b`) of a JSON object. */
function get(json: JsonObject, path: string): unknown {
    let current: unknown = json;
    for (const segment of path.split(".")) {
        if (current === null || typeof current !== "object" || Array.isArray(current)) {
            return undefined;
        }
        current = (current as Record<string, unknown>)[segment];
    }
    return current;
}

/** A signer for a bare private key: signs the body of every node with the key. */
function signerOf(key: PrivateKey): TransactionSigner {
    return {
        signTransaction: (bytes, node): NodeSignature => new Signature({
            node, publicKey: key.createPublicKey(), signature: key.sign(bytes),
        }),
    };
}
