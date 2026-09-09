import { describe, it, expect } from 'vitest';
import { 
    Hbar, 
    HbarUnit, 
    HEDERA_MAINNET_IDENTIFIER, 
    HEDERA_TESTNET_IDENTIFIER 
} from '../src/base/hedera.js';

describe('Hedera API', () => {
    describe('Constants', () => {
        it('should have correct identifiers', () => {
            expect(HEDERA_MAINNET_IDENTIFIER).toBe('hedera-mainnet');
            expect(HEDERA_TESTNET_IDENTIFIER).toBe('hedera-testnet');
        });
    });

    describe('HbarUnit', () => {
        it('should have correct properties mapping to native token unit', () => {
            expect(HbarUnit.TINYBAR.symbol).toBe('tℏ');
            expect(HbarUnit.TINYBAR.baseUnitFactor).toBe(1n);

            expect(HbarUnit.HBAR.symbol).toBe('ℏ');
            expect(HbarUnit.HBAR.baseUnitFactor).toBe(100_000_000n);
            
            expect(HbarUnit.GIGABAR.symbol).toBe('Gℏ');
            expect(HbarUnit.GIGABAR.baseUnitFactor).toBe(100_000_000_000_000_000n);
        });
    });

    describe('Hbar', () => {
        it('should correctly default to HBAR unit', () => {
            const hbar = new Hbar(5n);
            expect(hbar.amount).toBe(5n);
            expect(hbar.unit).toBe(HbarUnit.HBAR);
            expect(hbar.toTinybars()).toBe(500_000_000n);
        });

        it('should correctly convert to base units (tinybars)', () => {
            const hbar = new Hbar(5n, HbarUnit.MILLIBAR);
            expect(hbar.toTinybars()).toBe(500_000n);
            expect(hbar.toBaseUnits()).toBe(500_000n);
        });

        it('should convert correctly between Hbar units', () => {
            const hbar = new Hbar(1n, HbarUnit.HBAR);
            const inMillibars = hbar.to(HbarUnit.MILLIBAR);
            
            expect(inMillibars.amount).toBe(1000n);
            expect(inMillibars.unit).toBe(HbarUnit.MILLIBAR);
            expect(inMillibars.toTinybars()).toBe(100_000_000n);
        });
        
        it('should handle division correctly, truncating fractionals', () => {
            const hbar = new Hbar(500_000_000n, HbarUnit.TINYBAR); // 5 Hbars exactly
            const inHbars = hbar.to(HbarUnit.HBAR);
            expect(inHbars.amount).toBe(5n);
            
            const imperfectHbar = new Hbar(550_000_000n, HbarUnit.TINYBAR); // 5.5 Hbars
            const inHbarsTruncated = imperfectHbar.to(HbarUnit.HBAR);
            expect(inHbarsTruncated.amount).toBe(5n); // integer division truncates fractionals
        });
    });
});
