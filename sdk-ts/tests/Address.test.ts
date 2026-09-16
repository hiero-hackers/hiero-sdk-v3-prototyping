import { describe, it, expect } from 'vitest';
import { Address, ContractId, AccountId, formatAddress } from '../src/base/Address.js';

describe('Address hierarchy', () => {
  it('should format Address correctly', () => {
    const addr: Address = { kind: 'Address', shard: 0n, realm: 0n, num: 123n, checksum: '' };
    expect(formatAddress(addr)).toBe('0.0.123');
  });

  it('should format ContractId with evmAddress correctly', () => {
    const evm = new Uint8Array([0x12, 0x34]);
    const contract: ContractId = { kind: 'ContractId', type: 'evmAddress', evmAddress: evm, shard: 0n, realm: 0n, checksum: '' };
    expect(formatAddress(contract)).toBe('0.0.0x1234');
  });
  
  it('should format AccountId with alias correctly', () => {
    const alias = new Uint8Array([0xab, 0xcd]);
    const account: AccountId = { kind: 'AccountId', type: 'alias', alias: alias, shard: 0n, realm: 0n, checksum: '' };
    expect(formatAddress(account)).toBe('0.0.abcd');
  });
});
