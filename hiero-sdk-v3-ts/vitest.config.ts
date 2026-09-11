import { defineConfig } from 'vitest/config';

export default defineConfig({
  test: {
    coverage: {
      provider: 'v8',
      include: ['src/base/**/*.ts'],
      exclude: ['src/base/internal/proto/**'],
      all: true
    }
  }
});
