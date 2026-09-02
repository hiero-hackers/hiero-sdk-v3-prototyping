import { describe, it, expect } from 'vitest';
import { 
    KeyAlgorithm, 
    generatePrivateKey, 
    generatePublicKey,
    KeyFormat, 
    KeyType,
    createPrivateKey,
    createPublicKey,
    ByteImportEncoding,
    IllegalFormatError
} from '../src/base/keys.js';
import { Buffer } from 'node:buffer';

describe('Keys API', () => {
    describe('ED25519', () => {
        it('should generate, sign, and verify correctly', () => {
            const privateKey = generatePrivateKey(KeyAlgorithm.ED25519);
            expect(privateKey.algorithm).toBe(KeyAlgorithm.ED25519);
            expect(privateKey.type).toBe(KeyType.PRIVATE);
            
            const message = Buffer.from('hello world');
            const signature = privateKey.sign(message);
            
            const publicKey = privateKey.createPublicKey();
            expect(publicKey.type).toBe(KeyType.PUBLIC);
            expect(publicKey.verify(message, signature)).toBe(true);
            
            // Verify failure on wrong message
            const wrongMessage = Buffer.from('hello world 2');
            expect(publicKey.verify(wrongMessage, signature)).toBe(false);
        });
        
        it('should export and import using RAW bytes', () => {
            const privateKey = generatePrivateKey(KeyAlgorithm.ED25519);
            const rawBytes = privateKey.toRawBytes();
            
            const importedPriv = createPrivateKey(KeyAlgorithm.ED25519, rawBytes);
            const message = Buffer.from('test');
            const signature = importedPriv.sign(message);
            
            expect(importedPriv.createPublicKey().verify(message, signature)).toBe(true);
        });

        it('should export and import using PKCS8_WITH_PEM', () => {
            const privateKey = generatePrivateKey(KeyAlgorithm.ED25519);
            const pem = privateKey.toString(KeyFormat.PKCS8_WITH_PEM);
            expect(pem).toContain('-----BEGIN PRIVATE KEY-----');
            
            const imported = createPrivateKey(pem); // default is PKCS8_WITH_PEM
            expect(imported.algorithm).toBe(KeyAlgorithm.ED25519);
            expect(imported.toRawBytes()).toEqual(privateKey.toRawBytes());
        });
        
        it('should export and import using SPKI_WITH_PEM', () => {
            const privateKey = generatePrivateKey(KeyAlgorithm.ED25519);
            const publicKey = privateKey.createPublicKey();
            const pem = publicKey.toString(KeyFormat.SPKI_WITH_PEM);
            expect(pem).toContain('-----BEGIN PUBLIC KEY-----');
            
            const imported = createPublicKey(pem); // default is SPKI_WITH_PEM
            expect(imported.algorithm).toBe(KeyAlgorithm.ED25519);
            expect(imported.toRawBytes()).toEqual(publicKey.toRawBytes());
        });
        
        it('should export and import using HEX encoding directly', () => {
            const privateKey = generatePrivateKey(KeyAlgorithm.ED25519);
            const hex = Buffer.from(privateKey.toRawBytes()).toString('hex');
            
            const imported = createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, hex);
            expect(imported.toRawBytes()).toEqual(privateKey.toRawBytes());
        });
        
        it('should export and import using BASE64 encoding directly', () => {
            const publicKey = generatePrivateKey(KeyAlgorithm.ED25519).createPublicKey();
            const base64 = Buffer.from(publicKey.toRawBytes()).toString('base64');
            
            const imported = createPublicKey(KeyAlgorithm.ED25519, ByteImportEncoding.BASE64, base64);
            expect(imported.toRawBytes()).toEqual(publicKey.toRawBytes());
        });

        it('should generate public key directly', () => {
            const publicKey = generatePublicKey(KeyAlgorithm.ED25519);
            expect(publicKey.algorithm).toBe(KeyAlgorithm.ED25519);
            expect(publicKey.type).toBe(KeyType.PUBLIC);
        });

        it('should export and import using PKCS8_WITH_DER', () => {
            const privateKey = generatePrivateKey(KeyAlgorithm.ED25519);
            const derBytes = privateKey.toBytes(KeyFormat.PKCS8_WITH_DER);
            const imported = createPrivateKey(KeyFormat.PKCS8_WITH_DER, derBytes);
            expect(imported.toRawBytes()).toEqual(privateKey.toRawBytes());
        });
    });

    describe('ECDSA secp256k1', () => {
        it('should generate, sign, and verify correctly', () => {
            const privateKey = generatePrivateKey(KeyAlgorithm.ECDSA);
            expect(privateKey.algorithm).toBe(KeyAlgorithm.ECDSA);
            
            const message = Buffer.from('hello ecdsa');
            const signature = privateKey.sign(message);
            
            const publicKey = privateKey.createPublicKey();
            expect(publicKey.verify(message, signature)).toBe(true);
            
            // Verify failure on wrong message
            const wrongMessage = Buffer.from('wrong');
            expect(publicKey.verify(wrongMessage, signature)).toBe(false);
        });
        
        it('should export and import using PKCS8_WITH_DER', () => {
            const privateKey = generatePrivateKey(KeyAlgorithm.ECDSA);
            const derBytes = privateKey.toBytes(KeyFormat.PKCS8_WITH_DER);
            
            const imported = createPrivateKey(KeyFormat.PKCS8_WITH_DER, derBytes);
            expect(imported.algorithm).toBe(KeyAlgorithm.ECDSA);
            expect(imported.toRawBytes()).toEqual(privateKey.toRawBytes());
        });
        
        it('should export and import using SPKI_WITH_DER', () => {
            const privateKey = generatePrivateKey(KeyAlgorithm.ECDSA);
            const publicKey = privateKey.createPublicKey();
            const derBytes = publicKey.toBytes(KeyFormat.SPKI_WITH_DER);
            
            const imported = createPublicKey(KeyFormat.SPKI_WITH_DER, derBytes);
            expect(imported.algorithm).toBe(KeyAlgorithm.ECDSA);
            expect(imported.toRawBytes()).toEqual(publicKey.toRawBytes());
        });

        it('should export and import using PKCS8_WITH_PEM using 2-arg factory', () => {
            const privateKey = generatePrivateKey(KeyAlgorithm.ECDSA);
            const pem = privateKey.toString(KeyFormat.PKCS8_WITH_PEM);
            
            const imported = createPrivateKey(KeyFormat.PKCS8_WITH_PEM, pem);
            expect(imported.algorithm).toBe(KeyAlgorithm.ECDSA);
            expect(imported.toRawBytes()).toEqual(privateKey.toRawBytes());
        });

        it('should export and import using SPKI_WITH_PEM using 2-arg factory', () => {
            const publicKey = generatePublicKey(KeyAlgorithm.ECDSA);
            const pem = publicKey.toString(KeyFormat.SPKI_WITH_PEM);
            
            const imported = createPublicKey(KeyFormat.SPKI_WITH_PEM, pem);
            expect(imported.algorithm).toBe(KeyAlgorithm.ECDSA);
            expect(imported.toRawBytes()).toEqual(publicKey.toRawBytes());
        });
    });
    
    describe('Error Handling', () => {
        it('should throw IllegalFormatError on wrong container type for PrivateKey', () => {
            const privateKey = generatePrivateKey(KeyAlgorithm.ED25519);
            // SPKI is for public keys, should throw on private key
            expect(() => privateKey.toBytes(KeyFormat.SPKI_WITH_DER)).toThrow(IllegalFormatError);
            expect(() => privateKey.toString(KeyFormat.SPKI_WITH_PEM)).toThrow(IllegalFormatError);
        });

        it('should throw IllegalFormatError on wrong container type for PublicKey', () => {
            const publicKey = generatePrivateKey(KeyAlgorithm.ED25519).createPublicKey();
            // PKCS8 is for private keys, should throw on public key
            expect(() => publicKey.toBytes(KeyFormat.PKCS8_WITH_DER)).toThrow(IllegalFormatError);
            expect(() => publicKey.toString(KeyFormat.PKCS8_WITH_PEM)).toThrow(IllegalFormatError);
        });
        
        it('should throw IllegalFormatError when using toBytes with PEM (STRING format)', () => {
            const privateKey = generatePrivateKey(KeyAlgorithm.ED25519);
            expect(() => privateKey.toBytes(KeyFormat.PKCS8_WITH_PEM)).toThrow(IllegalFormatError);
        });
        
        it('should throw IllegalFormatError when using toString with DER (BYTES format)', () => {
            const privateKey = generatePrivateKey(KeyAlgorithm.ED25519);
            expect(() => privateKey.toString(KeyFormat.PKCS8_WITH_DER)).toThrow(IllegalFormatError);
        });
    });
});
