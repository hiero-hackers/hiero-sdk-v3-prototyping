import type { NativeTokenUnit } from "@hiero/base/nativeToken";
import type { HieroClient } from "@hiero/consensus-node-client/consensusnode/client";
import type { Session } from "@hiero/tck-contract";
import { RpcError } from "./RpcError.js";

/** The state of one test file of the TCK: the client created by `setup`. */
export class RuntimeSession implements Session {

    readonly id: string;
    #client: HieroClient<NativeTokenUnit> | null = null;

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

    /** Returns the session of the runtime behind a session of the contract. */
    static of(session: Session): RuntimeSession {
        if (session instanceof RuntimeSession) {
            return session;
        }
        throw new TypeError(`Session ${session.id} was not created by this runtime`);
    }
}
