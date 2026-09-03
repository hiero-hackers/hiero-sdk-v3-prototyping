import { describe, it, expect } from 'vitest';
import { KeyAlgorithm, generatePrivateKey } from '../src/base/Keys.js';

describe('Keys Cryptography', () => {
  it('Ed25519 should generate, sign, and verify correctly', () => {
    const privKey = generatePrivateKey(KeyAlgorithm.ED25519);
    const pubKey = privKey.createPublicKey();

    const message = new TextEncoder().encode('Hello Hiero');
    const signature = privKey.sign(message);
    
    expect(pubKey.verify(message, signature)).toBe(true);
    
    // Test failure case
    const badMessage = new TextEncoder().encode('Hello Bad');
    expect(pubKey.verify(badMessage, signature)).toBe(false);
  });

  it('ECDSA should generate, sign, and verify correctly', () => {
    const privKey = generatePrivateKey(KeyAlgorithm.ECDSA);
    const pubKey = privKey.createPublicKey();

    const message = new TextEncoder().encode('Hello Hiero ECDSA');
    const signature = privKey.sign(message);
    
    expect(pubKey.verify(message, signature)).toBe(true);
    
    // Test failure case
    const badMessage = new TextEncoder().encode('Hello Bad ECDSA');
    expect(pubKey.verify(badMessage, signature)).toBe(false);
  });
});
