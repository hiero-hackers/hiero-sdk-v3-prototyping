import { describe, it, expect } from 'vitest';
import { createNetwork } from '../../src/index.js';
import type { Network, NativeTokenUnit } from '../../src/index.js';

describe('PublicApiShape', () => {
    it('should allow consumers to define custom NativeTokenUnit and bind it to Network', () => {
        interface HbarUnit extends NativeTokenUnit {
            readonly symbol: string;
            readonly baseUnitFactor: bigint;
            readonly isTinybar: boolean;
        }

        const tinybarUnit: HbarUnit = {
            symbol: 'tℏ',
            baseUnitFactor: 1n,
            isTinybar: true
        };

        const idBytes = new Uint8Array([0x01, 0x02, 0x03]);
        const network: Network<HbarUnit> = createNetwork(idBytes, 'testnet', tinybarUnit);

        expect(network.id).toEqual(idBytes);
        expect(network.name).toBe('testnet');
        expect(network.nativeTokenUnit.symbol).toBe('tℏ');
        expect(network.nativeTokenUnit.baseUnitFactor).toBe(1n);
        expect(network.nativeTokenUnit.isTinybar).toBe(true);
    });

    it('should export types correctly from base namespace', async () => {
        const baseExports = await import('../../src/base/index.js');
        expect(typeof baseExports.createNetwork).toBe('function');
    });
});
