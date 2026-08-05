// src/base/keys.ts

import { Buffer } from 'node:buffer';
import { PrivateKeyImpl, PublicKeyImpl, determineAlgorithm } from './internal/keys-impl.js';

/**
 * All key types.
 */
export enum KeyType {
    /** A public key */
    PUBLIC = 'PUBLIC',
    /** A private key */
    PRIVATE = 'PRIVATE'
}

/**
 * All supported algorithms.
 */
export enum KeyAlgorithm {
    /** Edwards-curve Digital Signature Algorithm */
    ED25519 = 'ED25519',
    /** Elliptic Curve Digital Signature Algorithm (secp256k1 curve) */
    ECDSA = 'ECDSA'
}

/**
 * Key import/export formats.
 */
export enum RawFormat {
    /** String representation of the bytes in the specified encoding */
    STRING = 'STRING',
    /** Raw bytes */
    BYTES = 'BYTES'
}

/**
 * Supported encodings that can be used to import/export a container format
 */
export class KeyEncoding {
    static readonly DER = new KeyEncoding('DER', RawFormat.BYTES);
    static readonly PEM = new KeyEncoding('PEM', RawFormat.STRING);

    private constructor(
        public readonly name: string,
        public readonly rawFormat: RawFormat
    ) {
        Object.freeze(this);
    }

    decode(keyType: KeyType, value: string): Uint8Array {
        if (this.name === 'DER') {
            // DER encoded in hex string (remove spaces if any)
            const cleanHex = value.replace(/\s+/g, '');
            return Buffer.from(cleanHex, 'hex');
        } else if (this.name === 'PEM') {
            // PEM is base64 between headers
            const lines = value.split('\n').map(l => l.trim());
            const base64 = lines.filter(l => l.length > 0 && !l.startsWith('-----')).join('');
            return Buffer.from(base64, 'base64');
        }
        throw new Error('Unsupported encoding');
    }
}

/**
 * Supported container formats
 */
export class KeyContainer {
    static readonly PKCS8 = new KeyContainer('PKCS8', [KeyType.PRIVATE]);
    static readonly SPKI = new KeyContainer('SPKI', [KeyType.PUBLIC]);

    private constructor(
        public readonly name: string,
        private readonly supportedTypes: KeyType[]
    ) {
        Object.freeze(this);
    }

    supportsType(type: KeyType): boolean {
        return this.supportedTypes.includes(type);
    }
}

/**
 * Encoding information for import / export
 */
export class ByteImportEncoding {
    static readonly HEX = new ByteImportEncoding('HEX');
    static readonly BASE64 = new ByteImportEncoding('BASE64');

    private constructor(public readonly name: string) {
        Object.freeze(this);
    }

    decode(value: string): Uint8Array {
        if (this.name === 'HEX') {
            return Buffer.from(value.replace(/\s+/g, ''), 'hex');
        } else if (this.name === 'BASE64') {
            return Buffer.from(value, 'base64');
        }
        throw new Error('Unsupported encoding');
    }
}

/**
 * Combined container format and encoding
 */
export class KeyFormat {
    static readonly PKCS8_WITH_DER = new KeyFormat(KeyContainer.PKCS8, KeyEncoding.DER);
    static readonly SPKI_WITH_DER = new KeyFormat(KeyContainer.SPKI, KeyEncoding.DER);
    static readonly PKCS8_WITH_PEM = new KeyFormat(KeyContainer.PKCS8, KeyEncoding.PEM);
    static readonly SPKI_WITH_PEM = new KeyFormat(KeyContainer.SPKI, KeyEncoding.PEM);

    private constructor(
        public readonly container: KeyContainer,
        public readonly encoding: KeyEncoding
    ) {
        Object.freeze(this);
    }

    supportsType(type: KeyType): boolean {
        return this.container.supportsType(type);
    }

    decode(keyType: KeyType, value: string): Uint8Array {
        return this.encoding.decode(keyType, value);
    }
}

/**
 * Error thrown when an illegal format is encountered
 */
export class IllegalFormatError extends Error {
    constructor(message: string) {
        super(message);
        this.name = 'IllegalFormatError';
    }
}

/**
 * Abstract key definition.
 * 
 * A cryptographic key is defined by a byte sequence and a cryptographic algorithm.
 * It is independent of whether the key is a public or private key.
 */
export interface Key {
    /** The raw bytes of the key */
    readonly bytes: Uint8Array;
    /** The algorithm of the key */
    readonly algorithm: KeyAlgorithm;
    /** The type of the key */
    readonly type: KeyType;

    /**
     * Returns the key in the RAW encoding.
     * @returns The raw bytes of the key.
     */
    toRawBytes(): Uint8Array;
    
    /**
     * Convert to bytes using specified container format.
     * @param container - The container format to export to.
     * @returns The formatted bytes of the key.
     * @throws {IllegalFormatError} If container.format is not BYTES or doesn't support this key type.
     */
    toBytes(container: KeyFormat): Uint8Array;
    
    /**
     * Convert to string using specified container format.
     * @param container - The container format to export to.
     * @returns The formatted string representation of the key.
     * @throws {IllegalFormatError} If container.format is not STRING or doesn't support this key type.
     */
    toString(container: KeyFormat): string;
}

/**
 * Public key definition.
 * 
 * A public key can be used to verify signatures.
 */
export interface PublicKey extends Key {
    /**
     * Verify a signature using this public key.
     * @param message - The message bytes to verify.
     * @param signature - The signature bytes to verify against.
     * @returns True if the signature is valid for the message and the public key, false otherwise.
     */
    verify(message: Uint8Array, signature: Uint8Array): boolean;
}

/**
 * Private key definition.
 * 
 * A private key can be used to sign messages. A private key is normally generated as a random key for a specific algorithm.
 */
export interface PrivateKey extends Key {
    /**
     * Sign a message with this private key.
     * @param message - The message bytes to sign.
     * @returns The signature for the message.
     */
    sign(message: Uint8Array): Uint8Array;
    
    /**
     * Derive the corresponding public key.
     * @returns A new PublicKey instance derived from this private key.
     */
    createPublicKey(): PublicKey;
}

/**
 * A key pair containing both the public and private keys.
 */
export interface KeyPair {
    /** The public key of the key pair */
    readonly publicKey: PublicKey;
    /** The private key of the key pair */
    readonly privateKey: PrivateKey;
}

// ============================================================================
// Factory methods
// ============================================================================

/**
 * Generate a new private key based on a specific algorithm.
 * @param algorithm - The algorithm to generate the key for.
 * @returns A new randomly generated PrivateKey.
 */
export function generatePrivateKey(algorithm: KeyAlgorithm): PrivateKey {
    if (!algorithm) throw new TypeError('algorithm must be defined');
    return PrivateKeyImpl.generate(algorithm);
}

/**
 * Generate a new public key based on a specific algorithm.
 * @param algorithm - The algorithm to generate the key for.
 * @returns A new randomly generated PublicKey.
 */
export function generatePublicKey(algorithm: KeyAlgorithm): PublicKey {
    if (!algorithm) throw new TypeError('algorithm must be defined');
    return PrivateKeyImpl.generate(algorithm).createPublicKey();
}

/**
 * Read a key based on a specific algorithm from a byte array.
 * @param algorithm - The specific key algorithm.
 * @param rawBytes - The raw bytes of the key.
 * @returns A restored PrivateKey.
 * @throws {IllegalFormatError} If the format is invalid.
 */
export function createPrivateKey(algorithm: KeyAlgorithm, rawBytes: Uint8Array): PrivateKey;
/**
 * Read a key based on a specific algorithm from a string.
 * @param algorithm - The specific key algorithm.
 * @param encoding - The ByteImportEncoding (e.g. HEX, BASE64).
 * @param value - The encoded string.
 * @returns A restored PrivateKey.
 * @throws {IllegalFormatError} If the format is invalid.
 */
export function createPrivateKey(algorithm: KeyAlgorithm, encoding: ByteImportEncoding, value: string): PrivateKey;
/**
 * Read a key based on a specific format (container & encoding) from a string.
 * @param container - The KeyFormat combining container and encoding.
 * @param value - The encoded string.
 * @returns A restored PrivateKey.
 * @throws {IllegalFormatError} If container.format is not STRING or does not support PRIVATE keys.
 */
export function createPrivateKey(container: KeyFormat, value: string): PrivateKey;
/**
 * Read a key based on a specific format (container & encoding) from a byte array.
 * @param container - The KeyFormat combining container and encoding.
 * @param value - The encoded bytes.
 * @returns A restored PrivateKey.
 * @throws {IllegalFormatError} If container.format is not BYTES or does not support PRIVATE keys.
 */
export function createPrivateKey(container: KeyFormat, value: Uint8Array): PrivateKey;
/**
 * Read a key based on our preferred format (container & encoding) from a string.
 * Reads the string as PKCS#8 PEM.
 * @param value - The PEM encoded string.
 * @returns A restored PrivateKey.
 * @throws {IllegalFormatError} If the format is invalid.
 */
export function createPrivateKey(value: string): PrivateKey;
export function createPrivateKey(...args: any[]): PrivateKey {
    if (args.length === 2 && Object.values(KeyAlgorithm).includes(args[0]) && args[1] instanceof Uint8Array) {
        const [algorithm, rawBytes] = args as [KeyAlgorithm, Uint8Array];
        return new PrivateKeyImpl(rawBytes, algorithm);
    }
    
    if (args.length === 3 && Object.values(KeyAlgorithm).includes(args[0]) && args[1] instanceof ByteImportEncoding && typeof args[2] === 'string') {
        const [algorithm, encoding, value] = args as [KeyAlgorithm, ByteImportEncoding, string];
        return new PrivateKeyImpl(encoding.decode(value), algorithm);
    }
    
    if (args.length === 2 && args[0] instanceof KeyFormat && typeof args[1] === 'string') {
        const [container, value] = args as [KeyFormat, string];
        if (container.encoding.rawFormat !== RawFormat.STRING) throw new IllegalFormatError('Container format is not STRING');
        if (!container.supportsType(KeyType.PRIVATE)) throw new IllegalFormatError('Format does not support PRIVATE keys');
        const decoded = container.decode(KeyType.PRIVATE, value);
        return new PrivateKeyImpl(decoded, determineAlgorithm(decoded, KeyType.PRIVATE));
    }
    
    if (args.length === 2 && args[0] instanceof KeyFormat && args[1] instanceof Uint8Array) {
        const [container, value] = args as [KeyFormat, Uint8Array];
        if (container.encoding.rawFormat !== RawFormat.BYTES) throw new IllegalFormatError('Container format is not BYTES');
        if (!container.supportsType(KeyType.PRIVATE)) throw new IllegalFormatError('Format does not support PRIVATE keys');
        return new PrivateKeyImpl(value, determineAlgorithm(value, KeyType.PRIVATE));
    }
    
    if (args.length === 1 && typeof args[0] === 'string') {
        const [value] = args as [string];
        return createPrivateKey(KeyFormat.PKCS8_WITH_PEM, value);
    }
    
    throw new TypeError('Invalid arguments for createPrivateKey');
}

/**
 * Read a key based on a specific algorithm from a byte array.
 * @param algorithm - The specific key algorithm.
 * @param rawBytes - The raw bytes of the key.
 * @returns A restored PublicKey.
 * @throws {IllegalFormatError} If the format is invalid.
 */
export function createPublicKey(algorithm: KeyAlgorithm, rawBytes: Uint8Array): PublicKey;
/**
 * Read a key based on a specific algorithm from a string.
 * @param algorithm - The specific key algorithm.
 * @param encoding - The ByteImportEncoding (e.g. HEX, BASE64).
 * @param value - The encoded string.
 * @returns A restored PublicKey.
 * @throws {IllegalFormatError} If the format is invalid.
 */
export function createPublicKey(algorithm: KeyAlgorithm, encoding: ByteImportEncoding, value: string): PublicKey;
/**
 * Read a key based on a specific format (container & encoding) from a string.
 * @param container - The KeyFormat combining container and encoding.
 * @param value - The encoded string.
 * @returns A restored PublicKey.
 * @throws {IllegalFormatError} If container.format is not STRING or does not support PUBLIC keys.
 */
export function createPublicKey(container: KeyFormat, value: string): PublicKey;
/**
 * Read a key based on a specific format (container & encoding) from a byte array.
 * @param container - The KeyFormat combining container and encoding.
 * @param value - The encoded bytes.
 * @returns A restored PublicKey.
 * @throws {IllegalFormatError} If container.format is not BYTES or does not support PUBLIC keys.
 */
export function createPublicKey(container: KeyFormat, value: Uint8Array): PublicKey;
/**
 * Read a key based on our preferred format (container & encoding) from a string.
 * Reads the string as SPKI PEM.
 * @param value - The PEM encoded string.
 * @returns A restored PublicKey.
 * @throws {IllegalFormatError} If the format is invalid.
 */
export function createPublicKey(value: string): PublicKey;
export function createPublicKey(...args: any[]): PublicKey {
    if (args.length === 2 && Object.values(KeyAlgorithm).includes(args[0]) && args[1] instanceof Uint8Array) {
        const [algorithm, rawBytes] = args as [KeyAlgorithm, Uint8Array];
        return new PublicKeyImpl(rawBytes, algorithm);
    }
    
    if (args.length === 3 && Object.values(KeyAlgorithm).includes(args[0]) && args[1] instanceof ByteImportEncoding && typeof args[2] === 'string') {
        const [algorithm, encoding, value] = args as [KeyAlgorithm, ByteImportEncoding, string];
        return new PublicKeyImpl(encoding.decode(value), algorithm);
    }
    
    if (args.length === 2 && args[0] instanceof KeyFormat && typeof args[1] === 'string') {
        const [container, value] = args as [KeyFormat, string];
        if (container.encoding.rawFormat !== RawFormat.STRING) throw new IllegalFormatError('Container format is not STRING');
        if (!container.supportsType(KeyType.PUBLIC)) throw new IllegalFormatError('Format does not support PUBLIC keys');
        const decoded = container.decode(KeyType.PUBLIC, value);
        return new PublicKeyImpl(decoded, determineAlgorithm(decoded, KeyType.PUBLIC));
    }
    
    if (args.length === 2 && args[0] instanceof KeyFormat && args[1] instanceof Uint8Array) {
        const [container, value] = args as [KeyFormat, Uint8Array];
        if (container.encoding.rawFormat !== RawFormat.BYTES) throw new IllegalFormatError('Container format is not BYTES');
        if (!container.supportsType(KeyType.PUBLIC)) throw new IllegalFormatError('Format does not support PUBLIC keys');
        return new PublicKeyImpl(value, determineAlgorithm(value, KeyType.PUBLIC));
    }
    
    if (args.length === 1 && typeof args[0] === 'string') {
        const [value] = args as [string];
        return createPublicKey(KeyFormat.SPKI_WITH_PEM, value);
    }
    
    throw new TypeError('Invalid arguments for createPublicKey');
}
