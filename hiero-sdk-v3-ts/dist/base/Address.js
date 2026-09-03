function toHex(bytes) {
    return Array.from(bytes).map(b => b.toString(16).padStart(2, '0')).join('');
}
export function formatAddress(addr) {
    switch (addr.kind) {
        case 'Address':
            return `${addr.shard}.${addr.realm}.${addr.num}`;
        case 'ContractId':
            if (addr.type === 'num') {
                return `${addr.shard}.${addr.realm}.${addr.num}`;
            }
            else {
                return `${addr.shard}.${addr.realm}.0x${toHex(addr.evmAddress)}`;
            }
        case 'AccountId':
            if (addr.type === 'num') {
                return `${addr.shard}.${addr.realm}.${addr.num}`;
            }
            else if (addr.type === 'evmAddress') {
                return `${addr.shard}.${addr.realm}.0x${toHex(addr.evmAddress)}`;
            }
            else {
                return `${addr.shard}.${addr.realm}.${toHex(addr.alias)}`;
            }
    }
}
