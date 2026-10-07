import { HbarUnit } from "@hiero/base/hedera";
import { generatePrivateKey, KeyAlgorithm, KeyFormat } from "@hiero/base/keys";
import { ConsensusNode, IpAddress, MirrorNode, Network } from "@hiero/base/ledger";
import { NetworkSetting } from "@hiero/base/ledger/config";
import { Account, createClient } from "@hiero/consensus-node-client/consensusnode/client";
import type { JsonObject, Session } from "@hiero/tck-contract";
import { RpcError } from "./RpcError.js";
import { RuntimeSession } from "./RuntimeSession.js";
import type { TsConverters } from "./TsConverters.js";
import type { TsTckRuntime } from "./TsTckRuntime.js";

/**
 * The defaults of a local Solo network (Solo 0.63+, `solo one-shot single deploy`): used by `setup` for every value
 * the TCK does not send.
 */
export const SOLO = {
    nodeIp: "127.0.0.1:35211",
    nodeAccountId: "0.0.3",
    mirrorNodeRestUrl: "http://127.0.0.1:38081",
} as const;

/** The ledger ID of a local network (Solo). */
const LOCAL_LEDGER_ID = Uint8Array.of(3);

/** The runtime methods of the TCK that are not bound to a type of the API: `setup`, `reset` and `generateKey`. */
export class Utilities {

    readonly #runtime: TsTckRuntime;
    readonly #converters: TsConverters;

    constructor(runtime: TsTckRuntime, converters: TsConverters) {
        this.#runtime = runtime;
        this.#converters = converters;
    }

    /**
     * Creates the client of a session: the operator plus the network of the parameters (`nodeIp`, `nodeAccountId`)
     * and the mirror node REST API of the environment variable `MIRROR_NODE_REST_URL` (the TCK's variable); every
     * value that is not given is the one of a local Solo network.
     */
    async setup(params: JsonObject, session: Session): Promise<unknown> {
        const rt = this.#runtime;
        const c = this.#converters;
        const operator = new Account({
            accountId: rt.required(rt.value(params, [{ path: "operatorAccountId", converter: (json) => c.accountId(json) }]),
                "operatorAccountId"),
            privateKey: rt.required(rt.value(params, [{ path: "operatorPrivateKey", converter: (json) => c.privateKey(json) }]),
                "operatorPrivateKey"),
        });
        const [host, port] = c.string(params["nodeIp"] ?? SOLO.nodeIp).split(":");
        const node = new ConsensusNode({
            ip: IpAddress.fromString(host ?? ""),
            port: port === undefined ? 50211 : Number(port),
            account: c.accountId(params["nodeAccountId"] ?? SOLO.nodeAccountId),
        });
        // mirrorNetworkIp is the gRPC endpoint of the mirror node, the API uses its REST API: MIRROR_NODE_REST_URL
        const mirror = process.env["MIRROR_NODE_REST_URL"] ?? SOLO.mirrorNodeRestUrl;
        const setting = new NetworkSetting({
            network: new Network({ id: LOCAL_LEDGER_ID, name: "local", nativeTokenUnit: HbarUnit.TINYBAR }),
            getConsensusNodes: new Set([node]),
            getMirrorNodes: new Set([new MirrorNode({ restBaseUrl: mirror })]),
        });
        RuntimeSession.of(session).client = createClient(setting, operator);
        return { message: "Successfully setup client", status: "SUCCESS" };
    }

    /** Drops the client of a session. */
    async reset(_params: JsonObject, session: Session): Promise<unknown> {
        RuntimeSession.of(session).client = null;
        return { status: "SUCCESS" };
    }

    /**
     * Generates a key in the TCK representation (DER hex). Key lists and threshold keys need a byte encoding of
     * `Authority`, EVM addresses a derivation from the public key; the API has neither yet.
     */
    async generateKey(params: JsonObject, _session: Session): Promise<unknown> {
        const type = this.#converters.string(this.#runtime.required(params["type"] ?? null, "type"));
        const hex = (bytes: Uint8Array): string => Buffer.from(bytes).toString("hex");
        const algorithm = type.startsWith("ed25519") ? KeyAlgorithm.ED25519 : KeyAlgorithm.ECDSA;
        switch (type) {
            case "ed25519PrivateKey":
            case "ecdsaSecp256k1PrivateKey": {
                const key = hex(generatePrivateKey(algorithm).toBytes(KeyFormat.PKCS8_WITH_DER));
                return { key, privateKeys: [key] };
            }
            case "ed25519PublicKey":
            case "ecdsaSecp256k1PublicKey": {
                const key = generatePrivateKey(algorithm);
                return {
                    key: hex(key.createPublicKey().toBytes(KeyFormat.SPKI_WITH_DER)),
                    privateKeys: [hex(key.toBytes(KeyFormat.PKCS8_WITH_DER))],
                };
            }
            case "evmAddress":
                throw RpcError.gap("generateKey of type 'evmAddress': PublicKey has no EVM address");
            default:
                throw RpcError.gap(`generateKey of type '${type}': Authority has no toBytes`);
        }
    }
}
