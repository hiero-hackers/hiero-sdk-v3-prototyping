import { describe, it, expect } from 'vitest';
import { TokenType, TokenSupplyType } from '../src/base/Token.js';

describe('Token enums', () => {
  it('should have correct values', () => {
    expect(TokenType.FUNGIBLE_COMMON).toBe('FUNGIBLE_COMMON');
    expect(TokenSupplyType.FINITE).toBe('FINITE');
  });
});
