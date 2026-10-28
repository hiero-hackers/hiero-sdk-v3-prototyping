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

const P3 = 26 * 26 * 26; // 17576
const P5 = 26 * 26 * 26 * 26 * 26; // 11881376
const M = 1000003n;
const W = 31n;

/**
 * Calculates the canonical 5-character HIP-15 checksum for an entity ID (e.g. "0.0.123") on a given ledger ID.
 */
export function calculateHip15Checksum(entity: string, ledgerId: Uint8Array): string {
    const digits: bigint[] = [];
    for (let i = 0; i < entity.length; i++) {
        digits.push(entity[i] === '.' ? 10n : BigInt(entity[i]!));
    }
    const ledger = new Uint8Array(ledgerId.length + 6);
    ledger.set(ledgerId, 0);

    let sd0 = 0n;
    let sd1 = 0n;
    let sd = 0n;
    for (let i = 0; i < digits.length; i++) {
        sd = (W * sd + digits[i]!) % BigInt(P3);
        if (i % 2 === 0) {
            sd0 = (sd0 + digits[i]!) % 11n;
        } else {
            sd1 = (sd1 + digits[i]!) % 11n;
        }
    }

    let sh = 0n;
    for (let i = 0; i < ledger.length; i++) {
        sh = (W * sh + BigInt(ledger[i]!)) % BigInt(P5);
    }

    const lenMod5 = BigInt(digits.length % 5);
    const c = (((lenMod5 * 11n + sd0) * 11n + sd1) * BigInt(P3) + sd + sh) % BigInt(P5);
    let cp = (c * M) % BigInt(P5);

    const chars: string[] = [];
    for (let i = 0; i < 5; i++) {
        chars.push(String.fromCharCode(97 + Number(cp % 26n)));
        cp = cp / 26n;
    }
    return chars.reverse().join('');
}
