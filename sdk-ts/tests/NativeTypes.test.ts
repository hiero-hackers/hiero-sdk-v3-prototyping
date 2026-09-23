import { describe, it, expect } from 'vitest';
import { parseUint64 } from '../src/base/NativeTypes.js';

describe('NativeTypes', () => {
  it('should parse safe numbers to BigInt', () => {
    expect(parseUint64(42)).toBe(42n);
  });
  
  it('should parse large numbers from strings without precision loss', () => {
    const largeStr = '9007199254740993'; // Number.MAX_SAFE_INTEGER + 2
    expect(parseUint64(largeStr)).toBe(9007199254740993n);
  });
});
