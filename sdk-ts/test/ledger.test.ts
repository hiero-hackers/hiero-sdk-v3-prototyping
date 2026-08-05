import { describe, it, expect } from 'vitest';
import { 
    Address,
    EvmAddress,
    ContractId,
    AccountId,
    TransactionId,
    IpAddress,
    ZERO_ADDRESS,
    ZERO_ACCOUNT_ID,
    ZERO_CONTRACT_ID
} from '../src/base/ledger.js';
import { IllegalFormatError } from '../src/base/keys.js';
import { Buffer } from 'node:buffer';

describe('Ledger API', () => {
    describe('Address', () => {
        it('should parse valid address string', () => {
            const addr = Address.fromString('0.0.1234');
            expect(addr.shard).toBe(0n);
            expect(addr.realm).toBe(0n);
            expect(addr.num).toBe(1234n);
            expect(addr.checksum).toBe('');
            expect(addr.toString()).toBe('0.0.1234');
        });

        it('should parse address string with checksum', () => {
            const addr = Address.fromString('0.0.1234-vmbkz');
            expect(addr.checksum).toBe('vmbkz');
            expect(addr.toStringWithChecksum()).toBe('0.0.1234-vmbkz');
        });
        
        it('should throw on invalid format', () => {
            expect(() => Address.fromString('0.0.abc')).toThrow(IllegalFormatError);
        });
    });

    describe('EvmAddress', () => {
        it('should parse valid hex strings', () => {
            const hex = '00112233445566778899aabbccddeeff00112233';
            const addr = EvmAddress.fromString(hex);
            expect(addr.toString()).toBe('0x' + hex);
            
            const addr2 = EvmAddress.fromString('0x' + hex);
            expect(addr2.toString()).toBe('0x' + hex);
        });

        it('should wrap 20 bytes successfully', () => {
            const bytes = new Uint8Array(20).fill(1);
            const addr = EvmAddress.fromBytes(bytes);
            expect(addr.bytes).toEqual(bytes);
        });

        it('should throw on invalid hex', () => {
            expect(() => EvmAddress.fromString('0xshort')).toThrow(IllegalFormatError);
        });
    });

    describe('ContractId', () => {
        it('should parse numeric ContractId', () => {
            const contractId = ContractId.fromString('0.0.5678');
            expect(contractId.num).toBe(5678n);
            expect(contractId.evmAddress).toBeNull();
        });

        it('should parse EVM ContractId', () => {
            const hex = '00112233445566778899aabbccddeeff00112233';
            const contractId = ContractId.fromString(`0.0.${hex}`);
            expect(contractId.num).toBeNull();
            expect(contractId.evmAddress).not.toBeNull();
            expect(contractId.evmAddress!.toString()).toBe('0x' + hex);
            expect(contractId.toString()).toBe(`0.0.0x${hex}`);
        });

        it('should create from EvmAddress directly', () => {
            const addr = EvmAddress.fromString('00112233445566778899aabbccddeeff00112233');
            const contractId = ContractId.fromEvmAddress(0n, 1n, addr);
            expect(contractId.realm).toBe(1n);
        });
    });

    describe('AccountId', () => {
        it('should parse numeric AccountId', () => {
            const accountId = AccountId.fromString('0.1.999');
            expect(accountId.shard).toBe(0n);
            expect(accountId.realm).toBe(1n);
            expect(accountId.num).toBe(999n);
            expect(accountId.alias).toBeNull();
            expect(accountId.evmAddress).toBeNull();
        });

        it('should parse alias AccountId', () => {
            const aliasHex = Buffer.from('myaliasbytes').toString('hex');
            const accountId = AccountId.fromString(`0.0.${aliasHex}`);
            expect(accountId.num).toBeNull();
            expect(accountId.evmAddress).toBeNull();
            expect(accountId.alias).toBeDefined();
            expect(Buffer.from(accountId.alias!).toString('utf-8')).toBe('myaliasbytes');
        });
    });

    describe('TransactionId', () => {
        it('should parse valid TransactionId string', () => {
            const txIdStr = '0.0.123-1627581720@500000000';
            const txId = TransactionId.fromString(txIdStr);
            expect(txId.accountId.num).toBe(123n);
            expect(txId.validStart.getTime()).toBeDefined();
        });

        it('should generate TransactionId from Address', () => {
            const addr = Address.fromString('0.0.999');
            const txId = TransactionId.generateTransactionId(addr);
            expect(txId.accountId.num).toBe(999n);
        });
    });

    describe('IpAddress', () => {
        it('should parse valid IPv4 strings', () => {
            const ip = IpAddress.fromString('192.168.1.1');
            expect(ip.bytes).toEqual(new Uint8Array([192, 168, 1, 1]));
            expect(ip.toString()).toBe('192.168.1.1');
        });

        it('should throw on out of bounds IPv4', () => {
            expect(() => IpAddress.fromString('256.0.0.1')).toThrow(IllegalFormatError);
        });
    });

    describe('Sentinels', () => {
        it('should have valid sentinels', () => {
            expect(ZERO_ADDRESS.num).toBe(0n);
            expect(ZERO_ACCOUNT_ID.num).toBe(0n);
            expect(ZERO_CONTRACT_ID.num).toBe(0n);
        });
    });
});
