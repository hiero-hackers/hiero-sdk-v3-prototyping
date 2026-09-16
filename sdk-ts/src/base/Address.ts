export type BaseAddress = {
  shard: bigint;
  realm: bigint;
  checksum: string;
};

export type Address = BaseAddress & {
  kind: 'Address';
  num: bigint;
};

export type ContractId = BaseAddress & (
  | { kind: 'ContractId'; type: 'num'; num: bigint }
  | { kind: 'ContractId'; type: 'evmAddress'; evmAddress: Uint8Array }
);

export type AccountId = BaseAddress & (
  | { kind: 'AccountId'; type: 'num'; num: bigint }
  | { kind: 'AccountId'; type: 'evmAddress'; evmAddress: Uint8Array }
  | { kind: 'AccountId'; type: 'alias'; alias: Uint8Array }
);

export type EvmCapableAddress = ContractId | AccountId;
export type AnyAddress = Address | EvmCapableAddress;

function toHex(bytes: Uint8Array): string {
  return Array.from(bytes).map(b => b.toString(16).padStart(2, '0')).join('');
}

export function formatAddress(addr: AnyAddress): string {
  switch (addr.kind) {
    case 'Address':
      return `${addr.shard}.${addr.realm}.${addr.num}`;
    case 'ContractId':
      if (addr.type === 'num') {
        return `${addr.shard}.${addr.realm}.${addr.num}`;
      } else {
        return `${addr.shard}.${addr.realm}.0x${toHex(addr.evmAddress)}`;
      }
    case 'AccountId':
      if (addr.type === 'num') {
        return `${addr.shard}.${addr.realm}.${addr.num}`;
      } else if (addr.type === 'evmAddress') {
        return `${addr.shard}.${addr.realm}.0x${toHex(addr.evmAddress)}`;
      } else {
        return `${addr.shard}.${addr.realm}.${toHex(addr.alias)}`;
      }
  }
}
