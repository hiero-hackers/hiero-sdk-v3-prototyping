import type { 
    BaseAddress, 
    Address, 
    EvmAddress, 
    EvmCapableAddress, 
    ContractId, 
    AccountId, 
    TransactionId, 
    IpAddress,
    Network,
    ZonedDateTime
} from '../ledger.js';
import { IllegalFormatError } from '../keys.js';

export abstract class BaseAddressImpl implements BaseAddress {
    constructor(
        public readonly shard: bigint,
        public readonly realm: bigint,
        public readonly num: bigint | null,
        public readonly checksum: string
    ) {}

    validateChecksum(network: Network<any>): boolean {
        // Checksum validation stub
        return true;
    }

    abstract toString(): string;

    toStringWithChecksum(): string {
        const str = this.toString();
        if (this.checksum) {
            return `${str}-${this.checksum}`;
        }
        return str;
    }

    protected static parseBaseParts(value: string): { shard: bigint, realm: bigint, identifier: string, checksum: string } {
        const match = value.match(/^(\d+)\.(\d+)\.([a-zA-Z0-9]+)(?:-([a-z]{5}))?$/);
        if (!match) throw new IllegalFormatError('Invalid address format');
        return {
            shard: BigInt(match[1] as string),
            realm: BigInt(match[2] as string),
            identifier: match[3] as string,
            checksum: match[4] || ''
        };
    }
}

export class AddressImpl extends BaseAddressImpl implements Address {
    constructor(shard: bigint, realm: bigint, num: bigint, checksum: string = '') {
        super(shard, realm, num, checksum);
        Object.freeze(this);
    }

    declare public readonly num: bigint;

    toString(): string {
        return `${this.shard}.${this.realm}.${this.num}`;
    }

    static fromString(address: string): Address {
        const parts = this.parseBaseParts(address);
        if (!/^\d+$/.test(parts.identifier)) {
            throw new IllegalFormatError('Address must have a numeric selector');
        }
        return new AddressImpl(parts.shard, parts.realm, BigInt(parts.identifier), parts.checksum);
    }

    static createZero(): Address {
        return new AddressImpl(0n, 0n, 0n, '');
    }
}

export class EvmAddressImpl implements EvmAddress {
    constructor(public readonly bytes: Uint8Array) {
        if (bytes.length !== 20) throw new IllegalFormatError('EvmAddress must be exactly 20 bytes');
        Object.freeze(this);
    }

    toString(): string {
        return '0x' + Buffer.from(this.bytes).toString('hex');
    }

    static fromString(value: string): EvmAddress {
        if (value.startsWith('0x')) value = value.slice(2);
        if (value.length !== 40 || !/^[0-9a-fA-F]{40}$/.test(value)) {
            throw new IllegalFormatError('EvmAddress must be a 40-character hex string');
        }
        return new EvmAddressImpl(Buffer.from(value, 'hex'));
    }

    static fromBytes(value: Uint8Array): EvmAddress {
        return new EvmAddressImpl(value);
    }
}

export abstract class EvmCapableAddressImpl extends BaseAddressImpl implements EvmCapableAddress {
    constructor(
        shard: bigint,
        realm: bigint,
        num: bigint | null,
        public readonly evmAddress: EvmAddress | null,
        checksum: string
    ) {
        super(shard, realm, num, checksum);
    }
}

export class ContractIdImpl extends EvmCapableAddressImpl implements ContractId {
    constructor(
        shard: bigint,
        realm: bigint,
        num: bigint | null,
        evmAddress: EvmAddress | null,
        checksum: string = ''
    ) {
        super(shard, realm, num, evmAddress, checksum);
        if (num === null && evmAddress === null) {
            throw new Error('ContractId must have either a num or an evmAddress');
        }
        if (num !== null && evmAddress !== null) {
            throw new Error('ContractId cannot have both num and evmAddress initialized directly');
        }
        Object.freeze(this);
    }

    toString(): string {
        if (this.num !== null) return `${this.shard}.${this.realm}.${this.num}`;
        return `${this.shard}.${this.realm}.${this.evmAddress!.toString()}`;
    }

    static fromString(value: string): ContractId {
        const parts = this.parseBaseParts(value);
        if (/^\d+$/.test(parts.identifier)) {
            return new ContractIdImpl(parts.shard, parts.realm, BigInt(parts.identifier), null, parts.checksum);
        } else if (parts.identifier.length === 40 || parts.identifier.startsWith('0x')) {
            return new ContractIdImpl(parts.shard, parts.realm, null, EvmAddressImpl.fromString(parts.identifier), parts.checksum);
        }
        throw new IllegalFormatError('Invalid ContractId selector');
    }

    static fromEvmAddress(shard: bigint, realm: bigint, address: EvmAddress): ContractId {
        return new ContractIdImpl(shard, realm, null, address, '');
    }

    static createZero(): ContractId {
        return new ContractIdImpl(0n, 0n, 0n, null, '');
    }
}

export class AccountIdImpl extends EvmCapableAddressImpl implements AccountId {
    constructor(
        shard: bigint,
        realm: bigint,
        num: bigint | null,
        evmAddress: EvmAddress | null,
        public readonly alias: Uint8Array | null,
        checksum: string = ''
    ) {
        super(shard, realm, num, evmAddress, checksum);
        const nonNulls = [num, evmAddress, alias].filter(x => x !== null).length;
        if (nonNulls !== 1) {
            throw new Error('AccountId must have exactly one of num, evmAddress, or alias');
        }
        Object.freeze(this);
    }

    toString(): string {
        if (this.num !== null) return `${this.shard}.${this.realm}.${this.num}`;
        if (this.evmAddress !== null) return `${this.shard}.${this.realm}.${this.evmAddress.toString()}`;
        
        // Base32 encoding for aliases (mock implementation - assumes alias is HEX or handled elsewhere for now)
        // A true base32 implementation might be needed, but using hex for simplicity in this stub
        return `${this.shard}.${this.realm}.${Buffer.from(this.alias!).toString('hex')}`;
    }

    static fromString(value: string): AccountId {
        const parts = this.parseBaseParts(value);
        
        if (/^\d+$/.test(parts.identifier)) {
            return new AccountIdImpl(parts.shard, parts.realm, BigInt(parts.identifier), null, null, parts.checksum);
        } else if ((parts.identifier.length === 40 && /^[0-9a-fA-F]+$/.test(parts.identifier)) || parts.identifier.startsWith('0x')) {
            return new AccountIdImpl(parts.shard, parts.realm, null, EvmAddressImpl.fromString(parts.identifier), null, parts.checksum);
        } else {
            // Assume alias (e.g. public key base32/hex)
            return new AccountIdImpl(parts.shard, parts.realm, null, null, Buffer.from(parts.identifier, 'hex'), parts.checksum);
        }
    }

    static fromEvmAddress(shard: bigint, realm: bigint, address: EvmAddress): AccountId {
        return new AccountIdImpl(shard, realm, null, address, null, '');
    }

    static createZero(): AccountId {
        return new AccountIdImpl(0n, 0n, 0n, null, null, '');
    }
}

export class TransactionIdImpl implements TransactionId {
    constructor(
        public readonly accountId: AccountId,
        public readonly validStart: ZonedDateTime,
        public readonly nonce: number | null = null
    ) {
        Object.freeze(this);
    }

    toString(): string {
        const timeStr = `${this.validStart.getTime() / 1000}@${this.validStart.getTime() % 1000}`;
        const base = `${this.accountId.toString()}-${timeStr}`;
        if (this.nonce !== null) return `${base}-${this.nonce}`;
        return base;
    }

    toStringWithChecksum(): string {
        // Checksum stub
        return this.toString();
    }

    static generateTransactionId(accountId: Address | AccountId): TransactionId {
        let acc: AccountId;
        if ((accountId as any).alias !== undefined) {
            acc = accountId as AccountId;
        } else {
            acc = new AccountIdImpl(accountId.shard, accountId.realm, accountId.num, null, null, accountId.checksum);
        }
        return new TransactionIdImpl(acc, new Date());
    }

    static fromString(transactionId: string): TransactionId {
        // Basic parser for "accountId-validStartSeconds@validStartNanos[-nonce]"
        const parts = transactionId.split('-');
        if (parts.length < 2) throw new IllegalFormatError('Invalid TransactionId format');
        
        const accountId = AccountIdImpl.fromString(parts[0] as string);
        const timeParts = (parts[1] as string).split('@');
        if (timeParts.length !== 2) throw new IllegalFormatError('Invalid time format in TransactionId');
        
        const validStart = new Date(Number(timeParts[0]) * 1000 + Number(timeParts[1]));
        const nonce = parts.length === 3 ? parseInt(parts[2] as string, 10) : null;
        
        return new TransactionIdImpl(accountId, validStart, nonce);
    }
}

export class IpAddressImpl implements IpAddress {
    constructor(public readonly bytes: Uint8Array) {
        if (bytes.length !== 4) throw new IllegalFormatError('IpAddress must be 4 bytes');
        Object.freeze(this);
    }

    toString(): string {
        return `${this.bytes[0]}.${this.bytes[1]}.${this.bytes[2]}.${this.bytes[3]}`;
    }

    static fromString(value: string): IpAddress {
        const parts = value.split('.');
        if (parts.length !== 4) throw new IllegalFormatError('IpAddress must be IPv4 dotted-quad');
        
        const bytes = new Uint8Array(4);
        for (let i = 0; i < 4; i++) {
            const num = parseInt(parts[i] as string, 10);
            if (num < 0 || num > 255 || isNaN(num)) {
                throw new IllegalFormatError('IpAddress segments must be 0-255');
            }
            bytes[i] = num;
        }
        return new IpAddressImpl(bytes);
    }

    static fromBytes(value: Uint8Array): IpAddress {
        return new IpAddressImpl(value);
    }
}
