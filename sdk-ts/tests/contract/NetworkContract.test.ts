import { describe, it, expect } from 'vitest';
import { createNetwork } from '../../src/index.js';
import type { NativeTokenUnit } from '../../src/index.js';

describe('NetworkContract', () => {
    const dummyUnit: NativeTokenUnit = Object.freeze({
        symbol: 'ℏ',
        baseUnitFactor: 100_000_000n
    });

    describe('Scenario: Network identity state is observed', () => {
        it('returns immutable id, optional name, and nativeTokenUnit instance', () => {
            const rawId = new Uint8Array([0x00, 0x00, 0x00, 0x01]);
            const network = createNetwork(rawId, 'mainnet', dummyUnit);

            expect(network.id).toEqual(rawId);
            expect(network.name).toBe('mainnet');
            expect(network.nativeTokenUnit).toBe(dummyUnit);
            expect(Object.isFrozen(network)).toBe(true);
        });

        it('supports null name when network is unnamed', () => {
            const rawId = new Uint8Array([0x02]);
            const network = createNetwork(rawId, null, dummyUnit);

            expect(network.id).toEqual(rawId);
            expect(network.name).toBeNull();
            expect(network.nativeTokenUnit).toBe(dummyUnit);
        });
    });

    describe('Scenario: Caller mutates input or output byte array', () => {
        it('prevents external modification when input buffer is mutated', () => {
            const rawId = new Uint8Array([1, 2, 3, 4]);
            const network = createNetwork(rawId, 'testnet', dummyUnit);

            // Mutate input array
            rawId[0] = 99;

            expect(network.id[0]).toBe(1);
        });

        it('prevents external modification when returned buffer is mutated', () => {
            const rawId = new Uint8Array([10, 20, 30, 40]);
            const network = createNetwork(rawId, 'testnet', dummyUnit);

            const retrievedId = network.id;
            retrievedId[0] = 99;

            expect(network.id[0]).toBe(10);
        });
    });

    describe('Null and undefined validation', () => {
        it('rejects undefined for name (must use null for absence)', () => {
            const rawId = new Uint8Array([1]);
            // @ts-expect-error undefined is rejected by TypeScript and runtime
            expect(() => createNetwork(rawId, undefined, dummyUnit)).toThrow(TypeError);
        });

        it('rejects null or undefined for id', () => {
            // @ts-expect-error null id
            expect(() => createNetwork(null, 'test', dummyUnit)).toThrow(TypeError);
            // @ts-expect-error undefined id
            expect(() => createNetwork(undefined, 'test', dummyUnit)).toThrow(TypeError);
        });

        it('rejects non-Uint8Array for id', () => {
            // @ts-expect-error invalid type
            expect(() => createNetwork('not-a-uint8array' as any, 'test', dummyUnit)).toThrow(TypeError);
        });

        it('rejects null or undefined for nativeTokenUnit', () => {
            const rawId = new Uint8Array([1]);
            // @ts-expect-error null unit
            expect(() => createNetwork(rawId, 'test', null as any)).toThrow(TypeError);
            // @ts-expect-error undefined unit
            expect(() => createNetwork(rawId, 'test', undefined as any)).toThrow(TypeError);
        });
    });

    describe('Synchronous contract execution', () => {
        it('provides immediate synchronous property access without promises', () => {
            const rawId = new Uint8Array([42]);
            const network = createNetwork(rawId, 'solo', dummyUnit);

            expect(network.id).toBeInstanceOf(Uint8Array);
            expect(network.name).toBe('solo');
            expect(network.nativeTokenUnit).toEqual(dummyUnit);
        });
    });
});
