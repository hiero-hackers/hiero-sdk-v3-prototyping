import { describe, it, expect } from 'vitest';
import {
    BaseAddress,
    Address,
    ZERO_ADDRESS,
    IllegalFormatError
} from '../../src/index.js';

describe('AddressPublicApiShape', () => {
    it('should export BaseAddress and Address classes with proper inheritance', () => {
        expect(typeof BaseAddress).toBe('function');
        expect(typeof Address).toBe('function');
        expect(typeof Address.fromString).toBe('function');

        const addr = Address.fromString('1.2.3');
        expect(addr).toBeInstanceOf(Address);
        expect(addr).toBeInstanceOf(BaseAddress);

        expect(typeof addr.shard).toBe('bigint');
        expect(typeof addr.realm).toBe('bigint');
        expect(typeof addr.num).toBe('bigint');
        expect(typeof addr.checksum).toBe('string');
        expect(typeof addr.toString).toBe('function');
        expect(typeof addr.toStringWithChecksum).toBe('function');
        expect(typeof addr.validateChecksum).toBe('function');
    });

    it('should export ZERO_ADDRESS as an immutable Address instance', () => {
        expect(ZERO_ADDRESS).toBeInstanceOf(Address);
        expect(ZERO_ADDRESS.shard).toBe(0n);
        expect(ZERO_ADDRESS.realm).toBe(0n);
        expect(ZERO_ADDRESS.num).toBe(0n);
        expect(ZERO_ADDRESS.checksum).toBe('');
        expect(Object.isFrozen(ZERO_ADDRESS)).toBe(true);
    });

    it('should export IllegalFormatError as an Error subclass', () => {
        const error = new IllegalFormatError('test');
        expect(error).toBeInstanceOf(Error);
        expect(error).toBeInstanceOf(IllegalFormatError);
        expect(error.name).toBe('IllegalFormatError');
    });

    it('should export all addressing types from base namespace', async () => {
        const baseExports = await import('../../src/base/index.js');
        expect(baseExports.BaseAddress).toBeDefined();
        expect(baseExports.Address).toBeDefined();
        expect(baseExports.ZERO_ADDRESS).toBeDefined();
        expect(baseExports.IllegalFormatError).toBeDefined();
    });
});
