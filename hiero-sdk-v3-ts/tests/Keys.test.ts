import { describe, it, expect } from 'vitest';
import { KeyAlgorithm, KeyFormat, PKCS8_WITH_PEM, SPKI_WITH_PEM, PKCS8_WITH_DER, SPKI_WITH_DER, generatePrivateKey, createPrivateKey, createPublicKey } from '../src/base/Keys.js';

describe('Keys Format Cryptography', () => {
  it('Ed25519 should export and import PEM correctly', () => {
    const privKey = generatePrivateKey(KeyAlgorithm.ED25519);
    const pubKey = privKey.createPublicKey();

    const privPem = privKey.toString(PKCS8_WITH_PEM);
    const pubPem = pubKey.toString(SPKI_WITH_PEM);
    
    expect(privPem).toContain('BEGIN PRIVATE KEY');
    expect(pubPem).toContain('BEGIN PUBLIC KEY');

    const importedPriv = createPrivateKey(privPem);
    const importedPub = createPublicKey(pubPem);

    expect(importedPriv.algorithm).toBe(KeyAlgorithm.ED25519);
    expect(importedPub.algorithm).toBe(KeyAlgorithm.ED25519);
    expect(importedPriv.toRawBytes()).toEqual(privKey.toRawBytes());
    expect(importedPub.toRawBytes()).toEqual(pubKey.toRawBytes());
  });

  it('ECDSA should export and import PEM correctly', () => {
    const privKey = generatePrivateKey(KeyAlgorithm.ECDSA);
    const pubKey = privKey.createPublicKey();

    const privPem = privKey.toString(PKCS8_WITH_PEM);
    const pubPem = pubKey.toString(SPKI_WITH_PEM);
    
    const importedPriv = createPrivateKey(privPem, PKCS8_WITH_PEM);
    const importedPub = createPublicKey(pubPem, SPKI_WITH_PEM);

    expect(importedPriv.algorithm).toBe(KeyAlgorithm.ECDSA);
    expect(importedPub.algorithm).toBe(KeyAlgorithm.ECDSA);
    expect(importedPriv.toRawBytes()).toEqual(privKey.toRawBytes());
    expect(importedPub.toRawBytes()).toEqual(pubKey.toRawBytes());
  });

  it('Ed25519 should export and import DER correctly', () => {
    const privKey = generatePrivateKey(KeyAlgorithm.ED25519);
    const pubKey = privKey.createPublicKey();

    const privDer = privKey.toBytes(PKCS8_WITH_DER);
    const pubDer = pubKey.toBytes(SPKI_WITH_DER);

    const importedPriv = createPrivateKey(privDer, PKCS8_WITH_DER);
    const importedPub = createPublicKey(pubDer, SPKI_WITH_DER);

    expect(importedPriv.toRawBytes()).toEqual(privKey.toRawBytes());
    expect(importedPub.toRawBytes()).toEqual(pubKey.toRawBytes());
  });
});

  it('should verify signature tampering', () => {
    const privKey = generatePrivateKey(KeyAlgorithm.ED25519);
    const pubKey = privKey.createPublicKey();
    const msg = new Uint8Array([1, 2, 3]);
    const sig = privKey.sign(msg);
    
    expect(pubKey.verify(msg, sig)).toBe(true);

    // Tamper sig
    const badSig = new Uint8Array(sig);
    badSig[0] ^= 1;
    expect(pubKey.verify(msg, badSig)).toBe(false);
  });

  it('should throw illegal-format for mismatched formats', () => {
    const privKey = generatePrivateKey(KeyAlgorithm.ED25519);
    const pubKey = privKey.createPublicKey();
    
    // Try to export private key with SPKI
    expect(() => privKey.toString(SPKI_WITH_PEM)).toThrow('illegal-format');
    expect(() => privKey.toBytes(SPKI_WITH_DER)).toThrow('illegal-format');

    // Try to export public key with PKCS8
    expect(() => pubKey.toString(PKCS8_WITH_PEM)).toThrow('illegal-format');
    expect(() => pubKey.toBytes(PKCS8_WITH_DER)).toThrow('illegal-format');

    // Try to parse private key from public PEM
    const pubPem = pubKey.toString(SPKI_WITH_PEM);
    expect(() => createPrivateKey(pubPem)).toThrow('illegal-format');
  });

  it('should throw illegal-format for malformed PEM', () => {
    expect(() => createPrivateKey('-----BEGIN PRIVATE KEY-----\ngarbage\n-----END PRIVATE KEY-----')).toThrow('illegal-format');
    expect(() => createPrivateKey('invalid text entirely')).toThrow('illegal-format');
  });
  
  it('should throw illegal-format for malformed DER or bad hex', () => {
    const badBytes = new Uint8Array([0, 1, 2]);
    expect(() => createPrivateKey(badBytes, PKCS8_WITH_DER)).toThrow('illegal-format');
    
    const badHex = "ZZZZZZ";
    // Using string without PEM container implies PEM_ENCODING.decode -> which fails headers
    expect(() => createPrivateKey(badHex)).toThrow('illegal-format');
  });

  it('should throw illegal-format for cross format decoding', () => {
    const privKey = generatePrivateKey(KeyAlgorithm.ED25519);
    const pemStr = privKey.toString(PKCS8_WITH_PEM);
    
    // try to decode string using DER (which expects BYTES)
    expect(() => createPrivateKey(pemStr, PKCS8_WITH_DER)).toThrow('illegal-format');
  });
