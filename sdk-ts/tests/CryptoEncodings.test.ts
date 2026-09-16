import { describe, it, expect } from 'vitest';
import { unwrapDer, wrapDer } from '../src/base/internal/crypto/DerEncoding.js';
import { encodePem, decodePem } from '../src/base/internal/crypto/PemEncoding.js';
import { KeyAlgorithm, KeyType, KeyContainer } from '../src/base/Keys.js';

describe('Crypto Encodings Exhaustive Tests', () => {
  it('unwrapDer should throw illegal-format for short buffers', () => {
    const shortBuffer = new Uint8Array([0x30, 0x2e]);
    expect(() => unwrapDer(shortBuffer, KeyAlgorithm.ED25519, KeyType.PRIVATE, KeyContainer.PKCS8)).toThrow('illegal-format');
  });

  it('unwrapDer should throw illegal-format for wrong KeyType/Container combos', () => {
    const buf = new Uint8Array(32);
    expect(() => unwrapDer(buf, KeyAlgorithm.ED25519, KeyType.PRIVATE, KeyContainer.SPKI)).toThrow('illegal-format');
    expect(() => unwrapDer(buf, KeyAlgorithm.ED25519, KeyType.PUBLIC, KeyContainer.PKCS8)).toThrow('illegal-format');
  });

  it('wrapDer should throw illegal-format for wrong KeyType/Container combos', () => {
    const buf = new Uint8Array(32);
    expect(() => wrapDer(buf, KeyAlgorithm.ED25519, KeyType.PRIVATE, KeyContainer.SPKI)).toThrow('illegal-format');
    expect(() => wrapDer(buf, KeyAlgorithm.ED25519, KeyType.PUBLIC, KeyContainer.PKCS8)).toThrow('illegal-format');
  });

  it('decodePem should throw illegal-format for missing headers', () => {
    expect(() => decodePem('base64stringonly', KeyType.PRIVATE)).toThrow('illegal-format');
    expect(() => decodePem('-----BEGIN PUBLIC KEY-----\nbase64\n-----END PUBLIC KEY-----', KeyType.PRIVATE)).toThrow('illegal-format');
  });
});
