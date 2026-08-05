import { 
    KeyType, 
    KeyAlgorithm, 
    RawFormat, 
    KeyFormat, 
    IllegalFormatError
} from '../keys.js';

import type {
    PublicKey,
    PrivateKey,
    KeyPair,
    Key
} from '../keys.js';

import { Buffer } from 'node:buffer';
import { ed25519 } from '@noble/curves/ed25519.js';
import { secp256k1 } from '@noble/curves/secp256k1.js';

const ED25519_PRIV_DER_PREFIX = Buffer.from('302e020100300506032b657004220420', 'hex');
const ED25519_PUB_DER_PREFIX = Buffer.from('302a300506032b6570032100', 'hex');
const ECDSA_PRIV_DER_PREFIX = Buffer.from('30300201010420', 'hex');
const ECDSA_PRIV_DER_SUFFIX = Buffer.from('a00706052b8104000a', 'hex');
const ECDSA_PUB_DER_PREFIX = Buffer.from('3056301006072a8648ce3d020106052b8104000a034200', 'hex');

export function determineAlgorithm(bytes: Uint8Array, type: KeyType): KeyAlgorithm {
    // If raw 32 bytes, assume ED25519 by default or it might be raw ECDSA. We'll default to ED25519 for raw 32 bytes
    if (bytes.length === 32) return KeyAlgorithm.ED25519;
    
    const hexBytes = Buffer.from(bytes).toString('hex');
    if (type === KeyType.PRIVATE) {
        if (hexBytes.startsWith('30300201010420')) return KeyAlgorithm.ECDSA;
        return KeyAlgorithm.ED25519;
    } else {
        if (hexBytes.startsWith('3056301006072a8648ce3d020106052b8104000a034200')) return KeyAlgorithm.ECDSA;
        return KeyAlgorithm.ED25519;
    }
}

export abstract class KeyImpl implements Key {
    readonly #bytes: Uint8Array;
    readonly #algorithm: KeyAlgorithm;
    readonly #type: KeyType;

    constructor(bytes: Uint8Array, algorithm: KeyAlgorithm, type: KeyType) {
        if (!bytes) throw new TypeError('bytes must be defined');
        if (!algorithm) throw new TypeError('algorithm must be defined');
        if (!type) throw new TypeError('type must be defined');

        this.#bytes = new Uint8Array(bytes);
        this.#algorithm = algorithm;
        this.#type = type;
    }

    get bytes(): Uint8Array {
        return new Uint8Array(this.#bytes);
    }

    get algorithm(): KeyAlgorithm {
        return this.#algorithm;
    }

    get type(): KeyType {
        return this.#type;
    }

    toRawBytes(): Uint8Array {
        return new Uint8Array(this.#bytes);
    }

    protected _getDerBytes(): Uint8Array {
        if (this.type === KeyType.PRIVATE) {
            if (this.algorithm === KeyAlgorithm.ED25519) {
                return Buffer.concat([ED25519_PRIV_DER_PREFIX, this.#bytes]);
            } else {
                return Buffer.concat([ECDSA_PRIV_DER_PREFIX, this.#bytes, ECDSA_PRIV_DER_SUFFIX]);
            }
        } else {
            if (this.algorithm === KeyAlgorithm.ED25519) {
                return Buffer.concat([ED25519_PUB_DER_PREFIX, this.#bytes]);
            } else {
                return Buffer.concat([ECDSA_PUB_DER_PREFIX, this.#bytes]);
            }
        }
    }

    toBytes(container: KeyFormat): Uint8Array {
        if (!container.supportsType(this.type)) {
            throw new IllegalFormatError(`Container format does not support key type: ${this.type}`);
        }
        if (container.encoding.rawFormat !== RawFormat.BYTES) {
            throw new IllegalFormatError('Container format is not BYTES');
        }
        if (container.encoding.name === 'DER') {
            return this._getDerBytes();
        }
        
        throw new Error('Unsupported encoding for toBytes');
    }

    toString(container: KeyFormat): string {
        if (!container.supportsType(this.type)) {
            throw new IllegalFormatError(`Container format does not support key type: ${this.type}`);
        }
        if (container.encoding.rawFormat !== RawFormat.STRING) {
            throw new IllegalFormatError('Container format is not STRING');
        }
        
        const derBytes = this._getDerBytes();
        
        if (container.encoding.name === 'PEM') {
            const b64 = Buffer.from(derBytes).toString('base64');
            const lines = [];
            for (let i = 0; i < b64.length; i += 64) {
                lines.push(b64.substring(i, i + 64));
            }
            const label = this.type === KeyType.PRIVATE ? 'PRIVATE KEY' : 'PUBLIC KEY';
            return `-----BEGIN ${label}-----\n${lines.join('\n')}\n-----END ${label}-----`;
        }
        
        throw new Error('Unsupported encoding for toString');
    }
}

export class PublicKeyImpl extends KeyImpl implements PublicKey {
    constructor(bytes: Uint8Array, algorithm: KeyAlgorithm) {
        // Strip DER if present (heuristic for ease of use)
        let rawBytes = bytes;
        if (algorithm === KeyAlgorithm.ED25519 && bytes.length > 32) {
             rawBytes = bytes.slice(bytes.length - 32);
        } else if (algorithm === KeyAlgorithm.ECDSA && bytes.length > 33) {
             rawBytes = bytes.slice(bytes.length - 33);
        }
        
        super(rawBytes, algorithm, KeyType.PUBLIC);
        Object.freeze(this);
    }

    verify(message: Uint8Array, signature: Uint8Array): boolean {
        if (!message) throw new TypeError('message must be defined');
        if (!signature) throw new TypeError('signature must be defined');
        
        if (this.algorithm === KeyAlgorithm.ED25519) {
            return ed25519.verify(signature, message, this.bytes);
        } else {
            return secp256k1.verify(signature, message, this.bytes);
        }
    }
}

export class PrivateKeyImpl extends KeyImpl implements PrivateKey {
    constructor(bytes: Uint8Array, algorithm: KeyAlgorithm) {
        // Extract raw key if DER encoded
        let rawBytes = bytes;
        if (algorithm === KeyAlgorithm.ED25519) {
            if (bytes.length === 48) {
                rawBytes = bytes.slice(16);
            } else if (bytes.length === 64) {
                rawBytes = bytes.slice(0, 32); // seed
            }
        } else if (algorithm === KeyAlgorithm.ECDSA) {
            if (bytes.length > 32) {
                // Heuristic: basic DER extraction for ECDSA
                rawBytes = bytes.slice(7, 39);
            }
        }
        
        super(rawBytes, algorithm, KeyType.PRIVATE);
        Object.freeze(this);
    }

    sign(message: Uint8Array): Uint8Array {
        if (!message) throw new TypeError('message must be defined');
        
        if (this.algorithm === KeyAlgorithm.ED25519) {
            return ed25519.sign(message, this.bytes);
        } else {
            return secp256k1.sign(message, this.bytes);
        }
    }

    createPublicKey(): PublicKey {
        if (this.algorithm === KeyAlgorithm.ED25519) {
            const pubBytes = ed25519.getPublicKey(this.bytes);
            return new PublicKeyImpl(pubBytes, KeyAlgorithm.ED25519);
        } else {
            const pubBytes = secp256k1.getPublicKey(this.bytes, true); // true for compressed
            return new PublicKeyImpl(pubBytes, KeyAlgorithm.ECDSA);
        }
    }

    static generate(algorithm: KeyAlgorithm): PrivateKey {
        if (algorithm === KeyAlgorithm.ED25519) {
            const privBytes = ed25519.utils.randomSecretKey();
            return new PrivateKeyImpl(privBytes, KeyAlgorithm.ED25519);
        } else {
            const privBytes = secp256k1.utils.randomSecretKey();
            return new PrivateKeyImpl(privBytes, KeyAlgorithm.ECDSA);
        }
    }
}

export class KeyPairImpl implements KeyPair {
    readonly #publicKey: PublicKey;
    readonly #privateKey: PrivateKey;

    constructor(publicKey: PublicKey, privateKey: PrivateKey) {
        if (!publicKey) throw new TypeError('publicKey must be defined');
        if (!privateKey) throw new TypeError('privateKey must be defined');

        this.#publicKey = publicKey;
        this.#privateKey = privateKey;
        Object.freeze(this);
    }

    get publicKey(): PublicKey {
        return this.#publicKey;
    }

    get privateKey(): PrivateKey {
        return this.#privateKey;
    }
}
