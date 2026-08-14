// src/base/authority.ts

import { PublicKey } from './keys.js';
import { ContractId } from './ledger.js';

/**
 * Authorization requirement. Pure data sum type — no behavior methods. 
 * Immutable value type with structural equality.
 */
export type Authority = PublicKeyAuthority | ContractAuthority | AuthorityList;

/**
 * Leaf: satisfied by a signature from this public key.
 */
export interface PublicKeyAuthority {
    readonly kind: 'PublicKeyAuthority';
    readonly publicKey: PublicKey;
}

/**
 * Leaf: satisfied by the named contract being the active executing frame (no signature).
 */
export interface ContractAuthority {
    readonly kind: 'ContractAuthority';
    readonly contractId: ContractId;
    /** false → ContractID; true → DelegatableContractID */
    readonly delegatable: boolean;
}

/**
 * Composition: at least `threshold` of `children` must be satisfied.
 * n-of-n is `threshold == children.length`; m-of-n is any smaller threshold.
 */
export interface AuthorityList {
    readonly kind: 'AuthorityList';
    /** never empty */
    readonly children: readonly Authority[];
    /** invariant: 1 ≤ threshold ≤ children.length */
    readonly threshold: number;
}

export const Authority = {
    /**
     * Create an authority requirement.
     */
    of: function(first: PublicKey | number | Authority, second?: Authority, ...rest: Authority[]): Authority {
        if (typeof first === 'number') {
            const threshold = first;
            if (!second) {
                throw new Error("AuthorityList children cannot be empty");
            }
            const children = [second, ...rest];
            if (threshold < 1 || threshold > children.length) {
                throw new Error("Threshold must be between 1 and children.length");
            }
            return Object.freeze({
                kind: 'AuthorityList',
                children: Object.freeze(children),
                threshold
            });
        }

        // Check if first is an Authority by checking for the 'kind' property
        if (typeof (first as any).kind === 'string') {
            // It's the n-of-n composition
            const children = second ? [first as Authority, second, ...rest] : [first as Authority];
            return Object.freeze({
                kind: 'AuthorityList',
                children: Object.freeze(children),
                threshold: children.length
            });
        }

        // Otherwise, it must be a PublicKey
        return Object.freeze({
            kind: 'PublicKeyAuthority',
            publicKey: first as PublicKey
        });
    } as {
        /**
         * Single-key leaf.
         */
        (publicKey: PublicKey): PublicKeyAuthority;
        /**
         * m-of-n composition (multi-signature): at least `threshold` of `children` must be satisfied.
         */
        (threshold: number, first: Authority, ...rest: Authority[]): AuthorityList;
        /**
         * n-of-n composition ("all must sign"): threshold is set to children.length.
         */
        (first: Authority, ...rest: Authority[]): AuthorityList;
    },

    /**
     * Plain contract leaf (HAPI ContractID).
     */
    ofContract(contractId: ContractId): ContractAuthority {
        return Object.freeze({
            kind: 'ContractAuthority',
            contractId,
            delegatable: false
        });
    },

    /**
     * Delegatable contract leaf (HAPI DelegatableContractID): authority usable via delegatecall.
     */
    ofDelegatable(contractId: ContractId): ContractAuthority {
        return Object.freeze({
            kind: 'ContractAuthority',
            contractId,
            delegatable: true
        });
    }
};
