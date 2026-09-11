import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { DefaultHttpClient } from '../src/base/internal/http/DefaultHttpClient.js';
import { HttpConfiguration, HttpMethod } from '../src/base/Http.js';

describe('DefaultHttpClient Exhaustive Tests', () => {
  let originalFetch: any;

  beforeEach(() => {
    originalFetch = global.fetch;
    // @ts-ignore
    global.fetch = vi.fn();
  });

  afterEach(() => {
    global.fetch = originalFetch;
    vi.restoreAllMocks();
  });

  const baseConfig: HttpConfiguration = {
    defaultHeaders: new Map(),
    connectTimeout: 5000,
    defaultRequestTimeout: 5000
  };

  it('1.1 should test GET behavior without body', async () => {
    const mockResponse = new Response(new Uint8Array([1, 2, 3]).buffer);
    vi.mocked(fetch).mockResolvedValueOnce(mockResponse);

    const client = new DefaultHttpClient(baseConfig);
    const request: HttpRequest = {
      method: HttpMethod.GET,
      url: 'http://test.com',
      headers: new Map(),
      body: null,
      timeout: null
    };

    const result = await client.execute(request);
    expect(new Uint8Array(result.body)).toEqual(new Uint8Array([1, 2, 3]));
    expect(fetch).toHaveBeenCalledWith('http://test.com', expect.objectContaining({
      method: 'GET'
    }));
  });

  it('1.1 should test POST behavior with body payloads', async () => {
    const mockResponse = new Response(new Uint8Array([4, 5, 6]).buffer);
    vi.mocked(fetch).mockResolvedValueOnce(mockResponse);

    const client = new DefaultHttpClient(baseConfig);
    const request: HttpRequest = {
      method: HttpMethod.POST,
      url: 'http://test.com',
      headers: new Map(),
      body: new Uint8Array([7, 8]),
      timeout: null
    };

    const result = await client.execute(request);
    expect(fetch).toHaveBeenCalledWith('http://test.com', expect.objectContaining({
      method: 'POST'
    }));
    const fetchArgs = vi.mocked(fetch).mock.calls[0][1];
    expect(fetchArgs?.body).toEqual(new Uint8Array([7, 8]));
  });

  it('1.2 should blend default headers with request-specific headers', async () => {
    const mockResponse = new Response(new Uint8Array([1]).buffer, {
      headers: new Headers({ 'Response-Header': 'val' })
    });
    vi.mocked(fetch).mockResolvedValueOnce(mockResponse);

    const client = new DefaultHttpClient({
      ...baseConfig,
      defaultHeaders: new Map([['X-Default', 'def']])
    });

    const request: HttpRequest = {
      method: HttpMethod.GET,
      url: 'http://test.com',
      headers: new Map([['X-Request', 'req']]),
      body: null,
      timeout: null
    };

    await client.execute(request);
    
    const fetchArgs = vi.mocked(fetch).mock.calls[0][1];
    const fetchHeaders = fetchArgs?.headers as Headers;
    expect(fetchHeaders.get('X-Default')).toBe('def');
    expect(fetchHeaders.get('X-Request')).toBe('req');
  });

  it('1.3 should throw client-closed-error when executing on a closed client', async () => {
    const client = new DefaultHttpClient(baseConfig);
    client.close();

    const request: HttpRequest = {
      method: HttpMethod.GET,
      url: 'http://test.com',
      headers: new Map(),
      body: null,
      timeout: null
    };

    await expect(client.execute(request)).rejects.toThrow('client-closed-error');
  });

  it('1.4 should throw client-closed-error for in-flight requests when close() is called', async () => {
    vi.mocked(fetch).mockImplementation((url, init) => {
      return new Promise((resolve, reject) => {
        if (init?.signal?.aborted) {
          reject(new DOMException('abort', 'AbortError'));
          return;
        }
        init?.signal?.addEventListener('abort', () => {
          reject(new DOMException('abort', 'AbortError'));
        });
      });
    });

    const client = new DefaultHttpClient(baseConfig);
    const request: HttpRequest = {
      method: HttpMethod.GET,
      url: 'http://test.com',
      headers: new Map(),
      body: null,
      timeout: null
    };

    const executePromise = client.execute(request);
    
    client.close();
    
    await expect(executePromise).rejects.toThrow('client-closed-error');
  });

  it('1.5 should map timeout-error when AbortController aborts due to timeout', async () => {
    vi.useFakeTimers();

    vi.mocked(fetch).mockImplementation((url, init) => {
      return new Promise((resolve, reject) => {
        if (init?.signal?.aborted) {
          const err = new Error('abort');
          err.name = 'AbortError';
          reject(err);
          return;
        }
        init?.signal?.addEventListener('abort', () => {
          const err = new Error('abort');
          err.name = 'AbortError';
          reject(err);
        });
      });
    });

    const client = new DefaultHttpClient({
      ...baseConfig,
      defaultRequestTimeout: 1000
    });
    
    const request: HttpRequest = {
      method: HttpMethod.GET,
      url: 'http://test.com',
      headers: new Map(),
      body: null,
      timeout: null
    };

    const executePromise = client.execute(request);
    
    vi.advanceTimersByTime(1500);

    await expect(executePromise).rejects.toThrow('timeout-error');

    vi.useRealTimers();
  });

  it('1.6 should map connection-error for generic fetch rejections', async () => {
    vi.mocked(fetch).mockRejectedValueOnce(new Error('Network offline'));

    const client = new DefaultHttpClient(baseConfig);
    const request: HttpRequest = {
      method: HttpMethod.GET,
      url: 'http://test.com',
      headers: new Map(),
      body: null,
      timeout: null
    };

    await expect(client.execute(request)).rejects.toThrow('connection-error');
  });
});
