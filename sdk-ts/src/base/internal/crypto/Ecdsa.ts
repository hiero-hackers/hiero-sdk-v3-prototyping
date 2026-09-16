import { secp256k1 } from '@noble/curves/secp256k1.js';
import { KeyAlgorithm, KeyType, PrivateKey, PublicKey, KeyFormat, KeyEncoding, RawFormat } from '../../Keys.js';
import { wrapDer } from './DerEncoding.js';
import { encodePem } from './PemEncoding.js';

export class EcdsaPublicKey implements PublicKey {
  readonly algorithm = KeyAlgorithm.ECDSA;
  readonly type = KeyType.PUBLIC;

  constructor(public readonly bytes: Uint8Array) {}

  toRawBytes(): Uint8Array {
    return this.bytes;
  }
  toBytes(container: KeyFormat): Uint8Array {
    if (container.encoding.rawFormat !== RawFormat.BYTES) throw new Error('illegal-format');
    if (!container.supportsType(this.type)) throw new Error('illegal-format');
    return wrapDer(this.bytes, this.algorithm, this.type, container.container);
  }
  toString(container: KeyFormat): string {
    if (container.encoding.rawFormat !== RawFormat.STRING) throw new Error('illegal-format');
    if (!container.supportsType(this.type)) throw new Error('illegal-format');
    const der = wrapDer(this.bytes, this.algorithm, this.type, container.container);
    return encodePem(der, this.type);
  }
  verify(message: Uint8Array, signature: Uint8Array): boolean {
    return secp256k1.verify(signature, message, this.bytes);
  }
}

export class EcdsaPrivateKey implements PrivateKey {
  readonly algorithm = KeyAlgorithm.ECDSA;
  readonly type = KeyType.PRIVATE;

  constructor(public readonly bytes: Uint8Array) {}

  toRawBytes(): Uint8Array {
    return this.bytes;
  }
  toBytes(container: KeyFormat): Uint8Array {
    if (container.encoding.rawFormat !== RawFormat.BYTES) throw new Error('illegal-format');
    if (!container.supportsType(this.type)) throw new Error('illegal-format');
    return wrapDer(this.bytes, this.algorithm, this.type, container.container);
  }
  toString(container: KeyFormat): string {
    if (container.encoding.rawFormat !== RawFormat.STRING) throw new Error('illegal-format');
    if (!container.supportsType(this.type)) throw new Error('illegal-format');
    const der = wrapDer(this.bytes, this.algorithm, this.type, container.container);
    return encodePem(der, this.type);
  }
  sign(message: Uint8Array): Uint8Array {
    return secp256k1.sign(message, this.bytes);
  }
  createPublicKey(): PublicKey {
    return new EcdsaPublicKey(secp256k1.getPublicKey(this.bytes));
  }
}
