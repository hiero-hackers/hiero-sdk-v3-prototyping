/**
 * The state a `HieroClient` needs but cannot hold, and the gRPC calls to the consensus node.
 *
 * `HieroClient` is an immutable value of operator, `Network` and signer, and `Network` carries no `ConsensusNode` —
 * the `NetworkSetting` that names them reaches `createClient` and ends there. A submitted transaction has the same
 * problem: `Response` holds nothing but a transaction id, yet `queryReceipt()` has to reach the network. Both are
 * bridged by the registries below, keyed by the client and by the transaction id.
 *
 * Not public API — the directory is absent from the `exports` map of the package.
 */
import { fromBinary, toBinary, type DescMessage, type MessageShape } from "@bufbuild/protobuf";
import type { ConsensusNode } from "@hiero/base/ledger";
import type { NetworkSetting } from "@hiero/base/ledger/config";
import * as grpc from "@grpc/grpc-js";
import type { HieroClient } from "../consensusnode/client/HieroClient.js";
import type { Receipt } from "../consensusnode/transactions/Receipt.js";
import { HapiTransactionStatus } from "../consensusnode/transactions/HapiTransactionStatus.js";
import { QuerySchema, type Query } from "./proto/services/query_pb.js";
import { ResponseSchema, type Response as ProtoResponse } from "./proto/services/response_pb.js";
import { TransactionGetReceiptQuerySchema } from "./proto/services/transaction_get_receipt_pb.js";
import { QueryHeaderSchema, ResponseType } from "./proto/services/query_header_pb.js";
import { TransactionSchema, type Transaction as ProtoTransaction } from "./proto/services/transaction_pb.js";
import { TransactionResponseSchema } from "./proto/services/transaction_response_pb.js";
import type { TransactionReceipt } from "./proto/services/transaction_receipt_pb.js";
import { create } from "@bufbuild/protobuf";
import { transactionIdToProto } from "./protobuf.js";
import type { TransactionId } from "@hiero/base/ledger";

/** How long `queryReceipt` waits for consensus before it gives up. */
const RECEIPT_TIMEOUT_MILLIS = 30_000;

/** How long `queryReceipt` waits between two attempts. */
const RECEIPT_POLL_MILLIS = 500;

const CRYPTO_SERVICE = "proto.CryptoService";

/** Builds the typed receipt of a transaction from the protobuf receipt. */
export type ReceiptFactory = (id: TransactionId, status: HapiTransactionStatus,
                              receipt: TransactionReceipt) => Receipt;

interface Pending {
    readonly runtime: ClientRuntime;
    readonly node: ConsensusNode;
    readonly receipts: ReceiptFactory;
}

const byClient = new WeakMap<object, ClientRuntime>();
const byTransaction = new Map<string, Pending>();

/** A status that means "ask again", not "this is the answer". */
function pending(status: HapiTransactionStatus): boolean {
    return status === HapiTransactionStatus.UNKNOWN
        || status === HapiTransactionStatus.RECEIPT_NOT_FOUND
        || status === HapiTransactionStatus.BUSY
        || status === HapiTransactionStatus.PLATFORM_NOT_ACTIVE;
}

function sleep(millis: number): Promise<void> {
    return new Promise(resolve => setTimeout(resolve, millis));
}

/** The consensus nodes of a client's network and the gRPC channels to them. */
export class ClientRuntime {

    readonly #setting: NetworkSetting;
    readonly #nodes: ReadonlyArray<ConsensusNode>;
    readonly #clients = new Map<string, grpc.Client>();

    private constructor(setting: NetworkSetting) {
        this.#setting = setting;
        this.#nodes = [...setting.getConsensusNodes]
            .sort((left, right) => left.account.toString().localeCompare(right.account.toString()));
        if (this.#nodes.length === 0) {
            throw new RangeError("The network setting names no consensus node");
        }
    }

    /** Registers the runtime of a client. */
    static register(client: HieroClient<never> | object, setting: NetworkSetting): void {
        byClient.set(client, new ClientRuntime(setting));
    }

    /** The runtime of a client. */
    static of(client: object): ClientRuntime {
        const runtime = byClient.get(client);
        if (runtime === undefined) {
            throw new RangeError("The client was not created by createClient");
        }
        return runtime;
    }

    /** The network setting of the client. */
    get setting(): NetworkSetting {
        return this.#setting;
    }

    /** The node a transaction is sent to. */
    selectNode(): ConsensusNode {
        return this.#nodes[0] as ConsensusNode;
    }

    #channel(node: ConsensusNode): grpc.Client {
        const address = `${node.ip.toString()}:${node.port}`;
        let client = this.#clients.get(address);
        if (client === undefined) {
            client = new grpc.Client(address, grpc.credentials.createInsecure());
            this.#clients.set(address, client);
        }
        return client;
    }

    /** One unary call; the service contract is the method name, as in the Java runtime. */
    #call<Req extends DescMessage, Res extends DescMessage>(
        node: ConsensusNode, method: string, request: DescMessage, requestMessage: MessageShape<Req>,
        response: Res): Promise<MessageShape<Res>> {
        return new Promise((resolve, reject) => {
            this.#channel(node).makeUnaryRequest<MessageShape<Req>, MessageShape<Res>>(
                `/${CRYPTO_SERVICE}/${method}`,
                value => Buffer.from(toBinary(request as Req, value)),
                bytes => fromBinary(response, bytes),
                requestMessage,
                (error, value) => {
                    if (error !== null || value === undefined) {
                        reject(new Error(`${method} failed: ${error?.message ?? "no response"}`));
                    } else {
                        resolve(value);
                    }
                });
        });
    }

    /** Sends a signed transaction to a node and remembers how its receipt is read. */
    async submit(node: ConsensusNode, method: string, transaction: ProtoTransaction, id: TransactionId,
                 receipts: ReceiptFactory): Promise<void> {
        byTransaction.set(id.toString(), { runtime: this, node, receipts });
        const answer = await this.#call(node, method, TransactionSchema, transaction, TransactionResponseSchema);
        const precheck = HapiTransactionStatus.ofCode(answer.nodeTransactionPrecheckCode);
        if (precheck !== HapiTransactionStatus.OK) {
            byTransaction.delete(id.toString());
            throw new Error(`The node rejected the transaction: ${precheck.name}`);
        }
    }

    /** Queries the receipt of a submitted transaction, waiting for consensus. */
    static async queryReceipt(id: TransactionId): Promise<Receipt> {
        const waiting = byTransaction.get(id.toString());
        if (waiting === undefined) {
            throw new RangeError(`The transaction ${id.toString()} was not submitted by this client`);
        }
        const query: Query = create(QuerySchema, {
            query: {
                case: "transactionGetReceipt",
                value: create(TransactionGetReceiptQuerySchema, {
                    header: create(QueryHeaderSchema, { responseType: ResponseType.ANSWER_ONLY }),
                    transactionID: transactionIdToProto(id),
                }),
            },
        });
        const deadline = Date.now() + RECEIPT_TIMEOUT_MILLIS;
        let last = HapiTransactionStatus.UNKNOWN;
        while (Date.now() < deadline) {
            const answer: ProtoResponse = await waiting.runtime.#call(
                waiting.node, "getTransactionReceipts", QuerySchema, query, ResponseSchema);
            const receipt = answer.response.case === "transactionGetReceipt" ? answer.response.value : undefined;
            const precheck = HapiTransactionStatus.ofCode(receipt?.header?.nodeTransactionPrecheckCode ?? 0);
            if (precheck === HapiTransactionStatus.OK) {
                last = HapiTransactionStatus.ofCode(receipt?.receipt?.status ?? 0);
                if (!pending(last) && receipt?.receipt !== undefined) {
                    return waiting.receipts(id, last, receipt.receipt);
                }
            } else if (!pending(precheck)) {
                throw new Error(`The receipt query was rejected: ${precheck.name}`);
            } else {
                last = precheck;
            }
            await sleep(RECEIPT_POLL_MILLIS);
        }
        throw new Error(`No receipt for ${id.toString()} within ${RECEIPT_TIMEOUT_MILLIS / 1000} s, `
            + `last status ${last.name}`);
    }
}
