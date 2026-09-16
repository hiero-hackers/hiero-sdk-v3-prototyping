import { describe, it, expect } from 'vitest';
import { StreamItem } from '../src/base/StreamItem.js';

describe('StreamItem exhaustiveness', () => {
  it('should compile exhaustive type checks', () => {
    const item: StreamItem<string, Error> = { ok: true, value: 'test' };
    
    // This function acts as a type test for exhaustiveness
    function assertExhaustive(i: StreamItem<string, Error>) {
      if (i.ok) {
        expect(i.value).toBe('test');
      } else {
        expect(i.error).toBeInstanceOf(Error);
      }
    }
    
    assertExhaustive(item);
  });
});
