export type BaseAddress = {
    shard: bigint;
    realm: bigint;
    checksum: string;
};
export type Address = BaseAddress & {
    kind: 'Address';
    num: bigint;
};
export type ContractId = BaseAddress & ({
    kind: 'ContractId';
    type: 'num';
    num: bigint;
} | {
    kind: 'ContractId';
    type: 'evmAddress';
    evmAddress: Uint8Array;
});
export type AccountId = BaseAddress & ({
    kind: 'AccountId';
    type: 'num';
    num: bigint;
} | {
    kind: 'AccountId';
    type: 'evmAddress';
    evmAddress: Uint8Array;
} | {
    kind: 'AccountId';
    type: 'alias';
    alias: Uint8Array;
});
export type EvmCapableAddress = ContractId | AccountId;
export type AnyAddress = Address | EvmCapableAddress;
export declare function formatAddress(addr: AnyAddress): string;
