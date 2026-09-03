import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { createHttpClient, HttpConfiguration, HttpMethod, HttpRequest } from '../src/base/Http.js';

describe('HttpClient', () => {
  const config: HttpConfiguration = {
    defaultHeaders: new Map(),
    connectTimeout: 1000,
    defaultRequestTimeout: 1000
  };

  const req: HttpRequest = {
    method: HttpMethod.GET,
    url: 'https://example.com',
    body: null,
    timeout: 50,
    headers: new Map()
  };

  beforeEach(() => {
    // @ts-ignore
    global.fetch = vi.fn();
  });
  
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('handles successful responses', async () => {
    const mockResponse = {
      status: 200,
      headers: new Headers({ 'content-type': 'application/json' }),
      arrayBuffer: async () => new Uint8Array([1, 2, 3]).buffer
    };
    // @ts-ignore
    global.fetch.mockResolvedValueOnce(mockResponse);

    const client = createHttpClient(config);
    const res = await client.execute(req);

    expect(res.statusCode).toBe(200);
  });

  it('handles timeout errors', async () => {
    // @ts-ignore
    global.fetch.mockImplementationOnce((url, options) => {
      return new Promise((resolve, reject) => {
        const timeoutId = setTimeout(resolve, 200);
        if (options?.signal) {
          options.signal.addEventListener('abort', () => {
            clearTimeout(timeoutId);
            const err = new Error('The operation was aborted');
            err.name = 'AbortError';
            reject(err);
          });
        }
      });
    });

    const client = createHttpClient(config);
    await expect(client.execute(req)).rejects.toThrow('timeout-error');
  });
});
