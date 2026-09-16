import { describe, it, expect } from 'vitest';
import { Page } from '../src/base/Common.js';

describe('Common Primitives', () => {
  it('should compile Page interface correctly', () => {
    const page: Page<string> = {
      data: ['test'],
      size: 1,
      pageIndex: 0,
      hasNext: () => false,
      isFirst: () => true,
      next: async () => page,
      first: async () => page
    };
    expect(page.data[0]).toBe('test');
    expect(page.isFirst()).toBe(true);
  });
});
