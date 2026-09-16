import { ed25519 } from '@noble/curves/ed25519.js';
import { KeyAlgorithm, KeyType, PrivateKey, PublicKey, KeyFormat, KeyEncoding, RawFormat } from '../../Keys.js';
import { wrapDer } from './DerEncoding.js';
import { encodePem } from './PemEncoding.js';

export class Ed25519PublicKey implements PublicKey {
  readonly algorithm = KeyAlgorithm.ED25519;
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
    return ed25519.verify(signature, message, this.bytes);
  }
}

export class Ed25519PrivateKey implements PrivateKey {
  readonly algorithm = KeyAlgorithm.ED25519;
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
    return ed25519.sign(message, this.bytes);
  }
  createPublicKey(): PublicKey {
    return new Ed25519PublicKey(ed25519.getPublicKey(this.bytes));
  }
}
