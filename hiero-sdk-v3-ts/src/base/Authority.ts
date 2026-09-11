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

export function evaluateAuthority(auth: Authority): number {
  switch (auth.kind) {
    case 'PublicKeyAuthority':
      return 1;
    case 'ContractAuthority':
      return 2;
    case 'AuthorityList':
      return auth.children.reduce((acc, child) => acc + evaluateAuthority(child), 0);
  }
}
