import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import * as grpc from '@grpc/grpc-js';
import { NodeChannel } from '../src/base/internal/channel/NodeChannel.js';

vi.mock('@grpc/grpc-js', () => {
  const mockClientInstance = {
    waitForReady: vi.fn(),
    makeUnaryRequest: vi.fn(),
    close: vi.fn()
  };
  
  class Client {
    constructor() {
      // track calls if needed, or just let it construct
    }
    waitForReady = mockClientInstance.waitForReady;
    makeUnaryRequest = mockClientInstance.makeUnaryRequest;
    close = mockClientInstance.close;
  }

  class Metadata {
    constructor() {}
  }

  return {
    Client,
    credentials: {
      createInsecure: vi.fn(() => 'insecure')
    },
    Metadata,
    mockClientInstance
  };
});

describe('NodeChannel Exhaustive Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('4.2 Test lazy client initialization', async () => {
    const channel = new NodeChannel('test.com', 1000);
    // @ts-ignore
    const unaryClient = channel._createUnaryClient('TestService');
    
    expect(grpc.credentials.createInsecure).not.toHaveBeenCalled();
    
    // @ts-ignore
    const mockClient = grpc.mockClientInstance;
    // @ts-ignore
    mockClient.waitForReady.mockImplementationOnce((deadline, cb) => cb(null));
    // @ts-ignore
    mockClient.makeUnaryRequest.mockImplementationOnce((path, serialize, deserialize, buffer, meta, options, cb) => {
      cb(null, Buffer.from([0xFF]));
    });

    const callback = vi.fn();
    unaryClient({ name: 'Method' }, new Uint8Array([0x01]), callback);

    await new Promise(process.nextTick); // let promise chain resolve
    
    // Client should be instantiated exactly once
    expect(grpc.credentials.createInsecure).toHaveBeenCalledTimes(1); // 1 in test setup, 1 inside _initializeClient
  });

  it('4.3 Test deadline math and success callback', async () => {
    const channel = new NodeChannel('test.com', 2000);
    // @ts-ignore
    const unaryClient = channel._createUnaryClient('TestService');
    
    // @ts-ignore
    const mockClient = grpc.mockClientInstance;
    // @ts-ignore
    mockClient.waitForReady.mockImplementation((deadline, cb) => cb(null));
    // @ts-ignore
    mockClient.makeUnaryRequest.mockImplementation((path, serialize, deserialize, buffer, meta, options, cb) => {
      // options.deadline should be a Date roughly 2000ms in future
      expect(options.deadline.getTime() - Date.now()).toBeGreaterThan(1000);
      expect(options.deadline.getTime() - Date.now()).toBeLessThanOrEqual(2000);
      expect(path).toBe('/proto.TestService/Method');
      expect(buffer).toEqual(Buffer.from([0x01]));
      cb(null, Buffer.from([0x02]));
    });

    const callback = vi.fn();
    unaryClient({ name: 'Method' }, new Uint8Array([0x01]), callback);

    await new Promise(process.nextTick);

    expect(callback).toHaveBeenCalledWith(null, new Uint8Array([0x02]));
  });

  it('4.4 Test makeUnaryRequest errors trigger callback with error', async () => {
    const channel = new NodeChannel('test.com', 1000);
    // @ts-ignore
    const unaryClient = channel._createUnaryClient('TestService');
    
    // @ts-ignore
    const mockClient = grpc.mockClientInstance;
    // @ts-ignore
    mockClient.waitForReady.mockImplementation((deadline, cb) => cb(null));
    // @ts-ignore
    mockClient.makeUnaryRequest.mockImplementation((path, serialize, deserialize, buffer, meta, options, cb) => {
      cb(new Error('grpc error'));
    });

    const callback = vi.fn();
    unaryClient({ name: 'Method' }, new Uint8Array([0x01]), callback);

    await new Promise(process.nextTick);

    expect(callback).toHaveBeenCalledWith(expect.any(Error));
    expect(callback.mock.calls[0][0].message).toContain('grpc error');
  });
  
  it('4.5 Test waitForReady timeout mapping', async () => {
    const channel = new NodeChannel('test.com', 1000);
    // @ts-ignore
    const unaryClient = channel._createUnaryClient('TestService');
    
    // @ts-ignore
    const mockClient = grpc.mockClientInstance;
    // @ts-ignore
    mockClient.waitForReady.mockImplementation((deadline, cb) => cb(new Error('waitForReady timeout')));

    const callback = vi.fn();
    unaryClient({ name: 'Method' }, new Uint8Array([0x01]), callback);

    await new Promise(process.nextTick);

    expect(callback).toHaveBeenCalledWith(expect.any(Error));
    expect(callback.mock.calls[0][0].message).toContain('timeout');
  });

  it('4.6 Test close terminates client', async () => {
    const channel = new NodeChannel('test.com', 1000);
    // @ts-ignore
    const unaryClient = channel._createUnaryClient('TestService');
    
    // @ts-ignore
    const mockClient = grpc.mockClientInstance;
    // @ts-ignore
    mockClient.waitForReady.mockImplementation((deadline, cb) => cb(null));
    // @ts-ignore
    mockClient.makeUnaryRequest.mockImplementation((path, serialize, deserialize, buffer, meta, options, cb) => {
      cb(null, Buffer.from([0x02]));
    });

    const callback = vi.fn();
    unaryClient({ name: 'Method' }, new Uint8Array([0x01]), callback);
    await new Promise(process.nextTick);

    channel.close();
    expect(mockClient.close).toHaveBeenCalled();
  });
});
