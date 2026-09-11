import { describe, it, expect } from 'vitest';
import { NativeTokenUnit, NativeToken, ExchangeRate } from '../src/base/NativeToken.js';

describe('NativeToken', () => {
  it('should compile ExchangeRate properly', () => {
    const rate: ExchangeRate = {
      expirationTime: new Date(Date.now() - 1000),
      exchangeRateInUsdCents: 1.05,
      isExpired: function() { return new Date() > this.expirationTime; }
    };
    expect(rate.isExpired()).toBe(true);
  });
});
