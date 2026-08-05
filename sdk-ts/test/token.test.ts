import { describe, it, expect } from 'vitest';
import { TokenType, TokenSupplyType } from '../src/base/token.js';

describe('Token API', () => {
    describe('TokenType', () => {
        it('should have correctly mapped enum values', () => {
            expect(TokenType.FUNGIBLE_COMMON).toBe('FUNGIBLE_COMMON');
            expect(TokenType.NON_FUNGIBLE_UNIQUE).toBe('NON_FUNGIBLE_UNIQUE');
        });
    });

    describe('TokenSupplyType', () => {
        it('should have correctly mapped enum values', () => {
            expect(TokenSupplyType.INFINITE).toBe('INFINITE');
            expect(TokenSupplyType.FINITE).toBe('FINITE');
        });
    });
});
