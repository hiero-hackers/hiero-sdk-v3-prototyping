import { ContractId } from './Address.js';
import { PublicKey } from './Keys.js';
export type PublicKeyAuthority = {
    kind: 'PublicKeyAuthority';
    publicKey: PublicKey;
};
export type ContractAuthority = {
    kind: 'ContractAuthority';
    contractId: ContractId;
    delegatable: boolean;
};
export type AuthorityList = {
    kind: 'AuthorityList';
    children: Authority[];
    threshold: number;
};
export type Authority = PublicKeyAuthority | ContractAuthority | AuthorityList;
export declare function evaluateAuthority(auth: Authority): number;
