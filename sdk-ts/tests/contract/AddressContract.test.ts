import { describe, it, expect } from 'vitest';
import {
    Address,
    ZERO_ADDRESS,
    IllegalFormatError,
    createNetwork
} from '../../src/index.js';
import type { NativeTokenUnit } from '../../src/index.js';

describe('AddressContract', () => {
    const dummyUnit: NativeTokenUnit = Object.freeze({
        symbol: 'ℏ',
        baseUnitFactor: 100_000_000n
    });

    const mainnet = createNetwork(new Uint8Array([0]), 'mainnet', dummyUnit);
    const testnet = createNetwork(new Uint8Array([1]), 'testnet', dummyUnit);
    const previewnet = createNetwork(new Uint8Array([2]), 'previewnet', dummyUnit);
    const customNet = createNetwork(new Uint8Array([12, 31, 0, 2]), 'custom', dummyUnit);

    describe('Scenario: Address state is observed', () => {
        it('returns immutable shard, realm, num, and checksum without allowing modification', () => {
            const addr = new Address(0, 0, 123, 'vfmkw');

            expect(addr.shard).toBe(0n);
            expect(addr.realm).toBe(0n);
            expect(addr.num).toBe(123n);
            expect(addr.checksum).toBe('vfmkw');
            expect(Object.isFrozen(addr)).toBe(true);

            // Mutating properties is ignored or throws in strict mode
            // @ts-expect-error read-only property
            expect(() => { (addr as any).num = 456n; }).toThrow();
        });
    });

    describe('Scenario: Address is parsed from string', () => {
        it('parses shard.realm.num without checksum', () => {
            const addr = Address.fromString('10.50.25050');

            expect(addr.shard).toBe(10n);
            expect(addr.realm).toBe(50n);
            expect(addr.num).toBe(25050n);
            expect(addr.checksum).toBe('');
            expect(addr.toString()).toBe('10.50.25050');
            expect(addr.toStringWithChecksum()).toBe('10.50.25050');
        });

        it('parses shard.realm.num with checksum', () => {
            const addr = Address.fromString('0.0.123-vfmkw');

            expect(addr.shard).toBe(0n);
            expect(addr.realm).toBe(0n);
            expect(addr.num).toBe(123n);
            expect(addr.checksum).toBe('vfmkw');
            expect(addr.toString()).toBe('0.0.123');
            expect(addr.toStringWithChecksum()).toBe('0.0.123-vfmkw');
        });
    });

    describe('Scenario: Invalid address parsing fails', () => {
        const invalidFormats = [
            '',
            '0.0',
            '0.0.',
            '0.0.a',
            'a.b.c',
            '0.0.-1',
            '-1.-1.-1',
            '1.2.3.4',
            '8901000',
            '8.901000',
            '.8.90.1000.',
            '8..901000',
            '890.1000.',
            '0.0.1!',
            'o.o.e',
            'abc',
            '0.0.123#vfmkw',
            '0.0.123 - vfmkw',
            '0.0.123vfmkw',
            '0.0.123-vfm-kw',
            ' 0.0.100 ',
            '0.0.123-abc',
            '0.0.123-ABCDE'
        ];

        it.each(invalidFormats)('rejects invalid address format: "%s"', (text) => {
            expect(() => Address.fromString(text)).toThrow(IllegalFormatError);
        });

        it('rejects non-string inputs', () => {
            // @ts-expect-error non-string input
            expect(() => Address.fromString(null as any)).toThrow(IllegalFormatError);
            // @ts-expect-error non-string input
            expect(() => Address.fromString(undefined as any)).toThrow(IllegalFormatError);
            // @ts-expect-error non-string input
            expect(() => Address.fromString(123 as any)).toThrow(IllegalFormatError);
        });
    });

    describe('Scenario: Checksum is validated', () => {
        it('validates correct checksums on corresponding networks', () => {
            const mainnetAddr = Address.fromString('0.0.123-vfmkw');
            expect(mainnetAddr.validateChecksum(mainnet)).toBe(true);
            expect(mainnetAddr.validateChecksum(testnet)).toBe(false);

            const testnetAddr = Address.fromString('0.0.123-esxsf');
            expect(testnetAddr.validateChecksum(testnet)).toBe(true);
            expect(testnetAddr.validateChecksum(mainnet)).toBe(false);

            const previewnetAddr = Address.fromString('0.0.123-ogizo');
            expect(previewnetAddr.validateChecksum(previewnet)).toBe(true);
            expect(previewnetAddr.validateChecksum(mainnet)).toBe(false);

            const zeroOnMainnet = Address.fromString('0.0.0-uvnqa');
            expect(zeroOnMainnet.validateChecksum(mainnet)).toBe(true);
            expect(zeroOnMainnet.validateChecksum(testnet)).toBe(false);

            const customAddr = Address.fromString('0.0.255-wypmn');
            expect(customAddr.validateChecksum(customNet)).toBe(true);
            expect(customAddr.validateChecksum(mainnet)).toBe(false);
        });

        it('returns true when address has no checksum', () => {
            const noChecksumAddr = Address.fromString('0.0.123');
            expect(noChecksumAddr.validateChecksum(mainnet)).toBe(true);
            expect(noChecksumAddr.validateChecksum(testnet)).toBe(true);
            expect(noChecksumAddr.validateChecksum(previewnet)).toBe(true);
        });

        it('rejects null or undefined network in validateChecksum()', () => {
            const addr = Address.fromString('0.0.123-vfmkw');
            // @ts-expect-error null network
            expect(() => addr.validateChecksum(null as any)).toThrow(TypeError);
        });
    });

    describe('Scenario: Sentinel zero address', () => {
        it('represents 0.0.0 with empty checksum', () => {
            expect(ZERO_ADDRESS.shard).toBe(0n);
            expect(ZERO_ADDRESS.realm).toBe(0n);
            expect(ZERO_ADDRESS.num).toBe(0n);
            expect(ZERO_ADDRESS.checksum).toBe('');
            expect(ZERO_ADDRESS.toString()).toBe('0.0.0');
            expect(ZERO_ADDRESS.toStringWithChecksum()).toBe('0.0.0');
        });
    });
});
