/**
 * The concrete types behind the interfaces of `ledger` that the specs declare but do not provide: a transaction id.
 *
 * Not public API — the directory is absent from the `exports` map of the package.
 */
import type { AccountId } from "../ledger/AccountId.js";
import type { TransactionId } from "../ledger/TransactionId.js";

/** The valid start is dated back, so that a clock ahead of the node does not cause INVALID_TRANSACTION_START. */
const BACKDATE_MILLIS = 10_000;

/** Keeps the generated instants strictly increasing within this process. */
let lastMillis = 0;

/** The transaction id of the consensus node: the payer plus the instant from which the transaction is valid. */
export class DefaultTransactionId implements TransactionId {

    readonly accountId: AccountId;
    readonly validStart: Date;
    readonly nonce: number | null;

    constructor(accountId: AccountId, validStart: Date, nonce: number | null = null) {
        this.accountId = accountId;
        this.validStart = validStart;
        this.nonce = nonce;
        Object.freeze(this);
    }

    /** Generates an id for a payer, with a valid start that is unique within this process. */
    static generate(payer: AccountId): DefaultTransactionId {
        const now = Date.now() - BACKDATE_MILLIS;
        lastMillis = now <= lastMillis ? lastMillis + 1 : now;
        return new DefaultTransactionId(payer, new Date(lastMillis));
    }

    /** The seconds and nanoseconds of the valid start, as the wire format needs them. */
    get seconds(): bigint {
        return BigInt(Math.floor(this.validStart.getTime() / 1000));
    }

    get nanos(): number {
        return (this.validStart.getTime() % 1000) * 1_000_000;
    }

    toString(): string {
        const nanos = String(this.nanos).padStart(9, "0");
        return `${this.accountId.toString()}@${this.seconds}.${nanos}${this.nonce === null ? "" : `/${this.nonce}`}`;
    }

    toStringWithChecksum(): string {
        return this.toString();
    }
}
