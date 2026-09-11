export enum KeyType {
  PUBLIC = 'PUBLIC',
  PRIVATE = 'PRIVATE'
}

export enum KeyAlgorithm {
  ED25519 = 'ED25519',
  ECDSA = 'ECDSA'
}

export enum RawFormat {
  STRING = 'STRING',
  BYTES = 'BYTES'
}

export interface KeyEncoding {
  readonly rawFormat: RawFormat;
  decode(keyType: KeyType, value: string): Uint8Array;
}

export enum KeyContainer {
  PKCS8 = 'PKCS8',
  SPKI = 'SPKI'
}

export interface ByteImportEncoding {
  decode(value: string): Uint8Array;
}

export interface KeyFormat {
  readonly container: KeyContainer;
  readonly encoding: KeyEncoding;
  supportsType(type: KeyType): boolean;
  decode(keyType: KeyType, value: string): Uint8Array;
}

export interface Key {
  readonly bytes: Uint8Array;
  readonly algorithm: KeyAlgorithm;
  readonly type: KeyType;

  toRawBytes(): Uint8Array;
  toBytes(container: KeyFormat): Uint8Array;
  toString(container: KeyFormat): string;
}

export interface PublicKey extends Key {
  readonly type: KeyType.PUBLIC;
  verify(message: Uint8Array, signature: Uint8Array): boolean;
}

export interface PrivateKey extends Key {
  readonly type: KeyType.PRIVATE;
  sign(message: Uint8Array): Uint8Array;
  createPublicKey(): PublicKey;
}

export interface KeyPair {
  readonly publicKey: PublicKey;
  readonly privateKey: PrivateKey;
}

import { randomBytes } from "./internal/crypto/random.js";
import { Ed25519PrivateKey, Ed25519PublicKey } from './internal/crypto/Ed25519.js';
import { EcdsaPrivateKey, EcdsaPublicKey } from './internal/crypto/Ecdsa.js';
import { decodePem } from './internal/crypto/PemEncoding.js';
import { unwrapDer } from './internal/crypto/DerEncoding.js';

export const DER_ENCODING: KeyEncoding = {
  rawFormat: RawFormat.BYTES,
  decode: (type, value) => {
    // DER is bytes, so decode from string doesn't make sense per se, but let's assume hex if requested
    const arr = new Uint8Array(value.length / 2);
    for (let i = 0; i < arr.length; i++) {
      arr[i] = parseInt(value.substring(i * 2, i * 2 + 2), 16);
    }
    return arr;
  }
};

export const PEM_ENCODING: KeyEncoding = {
  rawFormat: RawFormat.STRING,
  decode: (type, value) => decodePem(value, type)
};

export const PKCS8_WITH_DER: KeyFormat = {
  container: KeyContainer.PKCS8,
  encoding: DER_ENCODING,
  supportsType: (type) => type === KeyType.PRIVATE,
  decode: (type, value) => DER_ENCODING.decode(type, value)
};

export const SPKI_WITH_DER: KeyFormat = {
  container: KeyContainer.SPKI,
  encoding: DER_ENCODING,
  supportsType: (type) => type === KeyType.PUBLIC,
  decode: (type, value) => DER_ENCODING.decode(type, value)
};

export const PKCS8_WITH_PEM: KeyFormat = {
  container: KeyContainer.PKCS8,
  encoding: PEM_ENCODING,
  supportsType: (type) => type === KeyType.PRIVATE,
  decode: (type, value) => PEM_ENCODING.decode(type, value)
};

export const SPKI_WITH_PEM: KeyFormat = {
  container: KeyContainer.SPKI,
  encoding: PEM_ENCODING,
  supportsType: (type) => type === KeyType.PUBLIC,
  decode: (type, value) => PEM_ENCODING.decode(type, value)
};

export function generatePrivateKey(algorithm: KeyAlgorithm): PrivateKey {
  if (algorithm === KeyAlgorithm.ED25519) {
    return new Ed25519PrivateKey(randomBytes(32));
  } else {
    return new EcdsaPrivateKey(randomBytes(32));
  }
}

export function generatePublicKey(algorithm: KeyAlgorithm): PublicKey {
  return generatePrivateKey(algorithm).createPublicKey();
}

export function createPrivateKeyFromRawBytes(algorithm: KeyAlgorithm, rawBytes: Uint8Array): PrivateKey {
  if (algorithm === KeyAlgorithm.ED25519) {
    return new Ed25519PrivateKey(rawBytes);
  } else {
    return new EcdsaPrivateKey(rawBytes);
  }
}

export function createPublicKeyFromRawBytes(algorithm: KeyAlgorithm, rawBytes: Uint8Array): PublicKey {
  if (algorithm === KeyAlgorithm.ED25519) {
    return new Ed25519PublicKey(rawBytes);
  } else {
    return new EcdsaPublicKey(rawBytes);
  }
}

function tryUnwrapDer(data: Uint8Array, type: KeyType, container: KeyContainer): { raw: Uint8Array, alg: KeyAlgorithm } {
  try {
    return { raw: unwrapDer(data, KeyAlgorithm.ED25519, type, container), alg: KeyAlgorithm.ED25519 };
  } catch (e) {}
  try {
    return { raw: unwrapDer(data, KeyAlgorithm.ECDSA, type, container), alg: KeyAlgorithm.ECDSA };
  } catch (e) {}
  throw new Error('illegal-format');
}

export function createPrivateKey(value: string | Uint8Array, container?: KeyFormat): PrivateKey {
  if (typeof value === 'string') {
    const format = container ?? PKCS8_WITH_PEM;
    if (format.encoding.rawFormat !== RawFormat.STRING) throw new Error('illegal-format');
    const derBytes = format.decode(KeyType.PRIVATE, value);
    const { raw, alg } = tryUnwrapDer(derBytes, KeyType.PRIVATE, format.container);
    return createPrivateKeyFromRawBytes(alg, raw);
  } else {
    const format = container ?? PKCS8_WITH_DER;
    if (format.encoding.rawFormat !== RawFormat.BYTES) throw new Error('illegal-format');
    const { raw, alg } = tryUnwrapDer(value, KeyType.PRIVATE, format.container);
    return createPrivateKeyFromRawBytes(alg, raw);
  }
}

export function createPublicKey(value: string | Uint8Array, container?: KeyFormat): PublicKey {
  if (typeof value === 'string') {
    const format = container ?? SPKI_WITH_PEM;
    if (format.encoding.rawFormat !== RawFormat.STRING) throw new Error('illegal-format');
    const derBytes = format.decode(KeyType.PUBLIC, value);
    const { raw, alg } = tryUnwrapDer(derBytes, KeyType.PUBLIC, format.container);
    return createPublicKeyFromRawBytes(alg, raw);
  } else {
    const format = container ?? SPKI_WITH_DER;
    if (format.encoding.rawFormat !== RawFormat.BYTES) throw new Error('illegal-format');
    const { raw, alg } = tryUnwrapDer(value, KeyType.PUBLIC, format.container);
    return createPublicKeyFromRawBytes(alg, raw);
  }
}
