import { describe, it, expect } from 'vitest';
import { Authority, evaluateAuthority } from '../src/base/Authority.js';
import { ContractId } from '../src/base/Address.js';

describe('Authority sum types', () => {
  it('should traverse recursive structure correctly', () => {
    const pkAuth: Authority = { kind: 'PublicKeyAuthority', publicKey: { bytes: new Uint8Array() } };
    const contract: ContractId = { kind: 'ContractId', type: 'num', num: 123n, shard: 0n, realm: 0n, checksum: '' };
    const contractAuth: Authority = { kind: 'ContractAuthority', contractId: contract, delegatable: false };
    const authList: Authority = { kind: 'AuthorityList', children: [pkAuth, contractAuth], threshold: 1 };
    
    // pk (1) + contract (2) = 3
    expect(evaluateAuthority(authList)).toBe(3);
  });
});
