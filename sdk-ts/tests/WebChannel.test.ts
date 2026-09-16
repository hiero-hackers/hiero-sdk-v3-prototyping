import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { WebChannel, encodeRequest, decodeUnaryResponse } from '../src/base/internal/channel/WebChannel.js';

describe('WebChannel Exhaustive Tests', () => {
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

  it('3.2 encodeRequest should prefix 0x00 and 4-byte length', () => {
    const data = new Uint8Array([0x01, 0x02, 0x03]);
    const encoded = encodeRequest(data);
    
    expect(encoded.length).toBe(3 + 5);
    expect(encoded[0]).toBe(0x00); // flag
    // 4 byte length (big endian)
    expect(encoded[1]).toBe(0);
    expect(encoded[2]).toBe(0);
    expect(encoded[3]).toBe(0);
    expect(encoded[4]).toBe(3);
    // data
    expect(encoded[5]).toBe(0x01);
  });

  it('3.3 decodeUnaryResponse should parse valid gRPC-Web frame', () => {
    const frame = new Uint8Array([0x00, 0x00, 0x00, 0x00, 0x02, 0x0A, 0x0B]);
    const decoded = decodeUnaryResponse(frame);
    expect(decoded).toEqual(new Uint8Array([0x0A, 0x0B]));
  });

  it('3.4 decodeUnaryResponse should throw if no data frame', () => {
    const frame = new Uint8Array([0x80, 0x00, 0x00, 0x00, 0x00]); // Only trailers
    expect(() => decodeUnaryResponse(frame)).toThrow('No grpc-web data frame found');
  });

  it('3.5 _createUnaryClient should parse fetch response and route to callback', async () => {
    const frame = new Uint8Array([0x00, 0x00, 0x00, 0x00, 0x01, 0xFF]);
    const mockResponse = new Response(frame.buffer);
    vi.mocked(fetch).mockResolvedValueOnce(mockResponse);

    const channel = new WebChannel('test.com');
    // @ts-ignore - access protected method for testing
    const unaryClient = channel._createUnaryClient('TestService');
    
    const callback = vi.fn();
    unaryClient({ name: 'TestMethod' }, new Uint8Array([0x01]), callback);

    // wait for fetch promise to resolve
    await new Promise(process.nextTick);
    await new Promise(process.nextTick);

    expect(fetch).toHaveBeenCalled();
    expect(callback).toHaveBeenCalledWith(null, new Uint8Array([0xFF]));
  });

  it('3.6 _createUnaryClient should route !response.ok to error', async () => {
    const mockResponse = new Response(null, { status: 500 });
    vi.mocked(fetch).mockResolvedValueOnce(mockResponse);

    const channel = new WebChannel('test.com');
    // @ts-ignore
    const unaryClient = channel._createUnaryClient('TestService');
    
    const callback = vi.fn();
    unaryClient({ name: 'TestMethod' }, new Uint8Array([0x01]), callback);

    await new Promise(process.nextTick);

    expect(callback).toHaveBeenCalledWith(expect.any(Error));
    expect(callback.mock.calls[0][0].message).toContain('HTTP 500');
  });

  it('3.7 _createUnaryClient should route AbortError to timeout', async () => {
    vi.useFakeTimers();
    vi.mocked(fetch).mockImplementation((url, init) => {
      return new Promise((resolve, reject) => {
        init?.signal?.addEventListener('abort', () => {
          const err = new Error('abort');
          err.name = 'AbortError';
          reject(err);
        });
      });
    });

    // 1000ms deadline
    const channel = new WebChannel('test.com', 1000);
    // @ts-ignore
    const unaryClient = channel._createUnaryClient('TestService');
    
    const callback = vi.fn();
    unaryClient({ name: 'TestMethod' }, new Uint8Array([0x01]), callback);

    vi.advanceTimersByTime(1500);
    await new Promise(process.nextTick);

    expect(callback).toHaveBeenCalledWith(expect.any(Error));
    expect(callback.mock.calls[0][0].message).toContain('timeout');
    vi.useRealTimers();
  });

  it('3.8 should lazily instantiate crypto proto service', () => {
    const channel = new WebChannel('test.com');
    const cryptoService = channel.crypto;
    expect(cryptoService).toBeDefined();
    
    // Test caching
    const cryptoService2 = channel.crypto;
    expect(cryptoService).toBe(cryptoService2);
  });
});
