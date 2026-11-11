import { describe, it, expect } from 'vitest';
import { NativeToken } from '../../src/index.js';
import type { NativeTokenUnit } from '../../src/index.js';

describe('NativeTokenPublicApiShape', () => {
    interface TestUnit extends NativeTokenUnit {
        readonly symbol: string;
        readonly baseUnitFactor: bigint;
    }

    class TestToken extends NativeToken<TestToken, TestUnit> {
        constructor(amount: bigint | number, unit: TestUnit) {
            super(amount, unit);
        }

        protected create(amount: bigint, unit: TestUnit): TestToken {
            return new TestToken(amount, unit);
        }
    }

    it('should export NativeToken abstract class and allow concrete subclassing', () => {
        expect(typeof NativeToken).toBe('function');

        const unit: TestUnit = { symbol: 'ℏ', baseUnitFactor: 100_000_000n };
        const token = new TestToken(5, unit);

        expect(token).toBeInstanceOf(NativeToken);
        expect(token).toBeInstanceOf(TestToken);
        expect(typeof token.amount).toBe('bigint');
        expect(token.unit).toBe(unit);
        expect(typeof token.toBaseUnits).toBe('function');
        expect(typeof token.to).toBe('function');
    });

    it('should export NativeToken from base namespace', async () => {
        const baseExports = await import('../../src/base/index.js');
        expect(baseExports.NativeToken).toBeDefined();
    });
});
