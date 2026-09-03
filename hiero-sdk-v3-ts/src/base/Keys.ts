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

export interface KeyContainer {
  supportsType(type: KeyType): boolean;
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
import { randomBytes } from "./crypto/random.js";

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

import { ed25519 } from '@noble/curves/ed25519.js';
import { secp256k1 } from '@noble/curves/secp256k1.js';
import { Ed25519PrivateKey, Ed25519PublicKey } from './crypto/Ed25519.js';
import { EcdsaPrivateKey, EcdsaPublicKey } from './crypto/Ecdsa.js';

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

export function createPrivateKey(value: string | Uint8Array, container?: KeyFormat): PrivateKey {
  // stub implementation for abstract factory
  throw new Error('Not implemented completely');
}

export function createPublicKey(value: string | Uint8Array, container?: KeyFormat): PublicKey {
  // stub implementation for abstract factory
  throw new Error('Not implemented completely');
}
