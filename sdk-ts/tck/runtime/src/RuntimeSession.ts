import type { NativeTokenUnit } from "@hiero/base/nativeToken";
import type { HieroClient } from "@hiero/consensus-node-client/consensusnode/client";
import type { MirrorNodeClient } from "@hiero/mirror-node-client/mirrornode";
import type { Session } from "@hiero/tck-contract";
import { RpcError } from "./RpcError.js";

/** The state of one test file of the TCK: the client and the Mirror Node client created by `setup`. */
export class RuntimeSession implements Session {

    readonly id: string;
    #client: HieroClient<NativeTokenUnit> | null = null;
    #mirrorNode: MirrorNodeClient | null = null;

    constructor(id: string) {
        this.id = id;
    }

    /** The client; fails if `setup` was not called. */
    get client(): HieroClient<NativeTokenUnit> {
        if (this.#client === null) {
            throw RpcError.internal(`setup has not been called for session '${this.id}'`);
        }
        return this.#client;
    }

    set client(client: HieroClient<NativeTokenUnit> | null) {
        this.#client = client;
    }

    /** The Mirror Node client; fails if `setup` was not called. */
    get mirrorNode(): MirrorNodeClient {
        if (this.#mirrorNode === null) {
            throw RpcError.internal(`setup has not been called for session '${this.id}'`);
        }
        return this.#mirrorNode;
    }

    set mirrorNode(mirrorNode: MirrorNodeClient | null) {
        this.#mirrorNode = mirrorNode;
    }

    /** Returns the session of the runtime behind a session of the contract. */
    static of(session: Session): RuntimeSession {
        if (session instanceof RuntimeSession) {
            return session;
        }
        throw new TypeError(`Session ${session.id} was not created by this runtime`);
    }
}
