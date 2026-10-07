/**
 * The concrete `PackedTransaction`: the serialized bodies per target node plus the signatures collected so far.
 *
 * Not public API — the directory is absent from the `exports` map of the package.
 */
import { create, toBinary } from "@bufbuild/protobuf";
import type { ConsensusNode, TransactionId } from "@hiero/base/ledger";
import type { Account } from "../consensusnode/client/Account.js";
import type { HieroClient } from "../consensusnode/client/HieroClient.js";
import { NodeSignature } from "../consensusnode/client/NodeSignature.js";
import type { TransactionSigner } from "../consensusnode/client/TransactionSigner.js";
import { NodeBody } from "../consensusnode/transactions/NodeBody.js";
import type { PackedTransaction } from "../consensusnode/transactions/PackedTransaction.js";
import type { Receipt } from "../consensusnode/transactions/Receipt.js";
import { Response } from "../consensusnode/transactions/Response.js";
import type { Transaction } from "../consensusnode/transactions/Transaction.js";
import { ClientRuntime, type ReceiptFactory } from "./client.js";
import { signaturePair } from "./protobuf.js";
import { SignatureMapSchema } from "./proto/services/basic_types_pb.js";
import { SignedTransactionSchema } from "./proto/services/transaction_contents_pb.js";
import { TransactionSchema, type Transaction as ProtoTransaction } from "./proto/services/transaction_pb.js";

/** Whether a value is a `TransactionSigner` and not an `Account`. */
function isSigner(value: unknown): value is TransactionSigner {
    return typeof (value as TransactionSigner | undefined)?.signTransaction === "function";
}

/** A packed transaction of one target node. */
export class DefaultPackedTransaction<ReceiptT extends Receipt,
    TransactionT extends Transaction<ReceiptT, TransactionT>> implements PackedTransaction<ReceiptT, TransactionT> {

    readonly #transactionId: TransactionId;
    readonly #signatures: ReadonlyArray<NodeSignature>;
    readonly #bodies: ReadonlyArray<NodeBody>;
    readonly #node: ConsensusNode;
    readonly #method: string;
    readonly #receipts: ReceiptFactory;

    maxAttempts: number | null = null;
    maxBackoff: bigint | null = null;
    minBackoff: bigint | null = null;
    attemptTimeout: bigint | null = null;

    constructor(transactionId: TransactionId, signatures: ReadonlyArray<NodeSignature>,
                bodies: ReadonlyArray<NodeBody>, node: ConsensusNode, method: string, receipts: ReceiptFactory) {
        this.#transactionId = transactionId;
        this.#signatures = signatures;
        this.#bodies = bodies;
        this.#node = node;
        this.#method = method;
        this.#receipts = receipts;
    }

    get transactionId(): TransactionId {
        return this.#transactionId;
    }

    get nodeSignatures(): ReadonlyArray<NodeSignature> {
        return this.#signatures;
    }

    signableBodies(): ReadonlyArray<NodeBody> {
        return this.#bodies;
    }

    sign(...args: any[]): PackedTransaction<ReceiptT, TransactionT> {
        const [first] = args as [unknown];
        if (Array.isArray(first)) {
            return this.#with(first as ReadonlyArray<NodeSignature>);
        }
        const signer: TransactionSigner = isSigner(first)
            ? first
            : {
                signTransaction: (bytes: Uint8Array, node) => {
                    const account = first as Account;
                    return new NodeSignature({
                        node,
                        publicKey: account.privateKey.createPublicKey(),
                        signature: account.privateKey.sign(bytes),
                    });
                },
            };
        return this.#with(this.#bodies.map(body => signer.signTransaction(body.bytes, body.node)));
    }

    #with(signatures: ReadonlyArray<NodeSignature>): DefaultPackedTransaction<ReceiptT, TransactionT> {
        return new DefaultPackedTransaction(this.#transactionId, [...this.#signatures, ...signatures],
            this.#bodies, this.#node, this.#method, this.#receipts);
    }

    toBytes(): Uint8Array {
        return toBinary(TransactionSchema, this.#proto());
    }

    async submit(client: HieroClient<never>): Promise<Response<ReceiptT>> {
        await ClientRuntime.of(client).submit(this.#node, this.#method, this.#proto(), this.#transactionId,
            this.#receipts);
        return new Response<ReceiptT>({ transactionId: this.#transactionId });
    }

    #proto(): ProtoTransaction {
        const target = this.#node.account.toString();
        const body = this.#bodies.find(candidate => candidate.node.toString() === target);
        if (body === undefined) {
            throw new RangeError(`The transaction was not packed for node ${target}`);
        }
        const sigPairs = this.#signatures
            .filter(signature => signature.node.toString() === target)
            .map(signature => signaturePair(signature.publicKey, signature.signature));
        const signed = create(SignedTransactionSchema, {
            bodyBytes: body.bytes,
            sigMap: create(SignatureMapSchema, { sigPair: sigPairs }),
        });
        return create(TransactionSchema, {
            signedTransactionBytes: toBinary(SignedTransactionSchema, signed),
        });
    }
}
