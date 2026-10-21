import type { NativeTokenUnit } from '../nativeToken.js';
import type { Network } from '../ledger.js';

export class NetworkImpl<Unit extends NativeTokenUnit> implements Network<Unit> {
    readonly #id: Uint8Array;
    readonly #name: string | null;
    readonly #nativeTokenUnit: Unit;

    constructor(id: Uint8Array, name: string | null, nativeTokenUnit: Unit) {
        if (id === undefined || id === null) {
            throw new TypeError('Network id must not be null or undefined');
        }
        if (!(id instanceof Uint8Array)) {
            throw new TypeError('Network id must be a Uint8Array');
        }
        if (name === undefined) {
            throw new TypeError('Network name must not be undefined; use null for absence');
        }
        if (name !== null && typeof name !== 'string') {
            throw new TypeError('Network name must be a string or null');
        }
        if (nativeTokenUnit === undefined || nativeTokenUnit === null) {
            throw new TypeError('Network nativeTokenUnit must not be null or undefined');
        }

        // Defensive copy of the input byte array
        this.#id = new Uint8Array(id);
        this.#name = name;
        this.#nativeTokenUnit = nativeTokenUnit;

        Object.freeze(this);
    }

    get id(): Uint8Array {
        // Defensive copy on read so external callers cannot modify internal state
        return new Uint8Array(this.#id);
    }

    get name(): string | null {
        return this.#name;
    }

    get nativeTokenUnit(): Unit {
        return this.#nativeTokenUnit;
    }
}
