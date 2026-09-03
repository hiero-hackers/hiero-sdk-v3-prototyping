import { secp256k1 } from '@noble/curves/secp256k1.js';
import { KeyAlgorithm, KeyType, PrivateKey, PublicKey, KeyFormat } from '../Keys.js';

export class EcdsaPublicKey implements PublicKey {
  readonly type = KeyType.PUBLIC;
  readonly algorithm = KeyAlgorithm.ECDSA;

  constructor(public readonly bytes: Uint8Array) {}

  toRawBytes(): Uint8Array {
    return this.bytes;
  }
  toBytes(container: KeyFormat): Uint8Array {
    return container.decode(this.type, this.toString(container)); // stub
  }
  toString(container: KeyFormat): string {
    return Array.from(this.bytes).map(b => b.toString(16).padStart(2, '0')).join(''); // stub
  }
  verify(message: Uint8Array, signature: Uint8Array): boolean {
    return secp256k1.verify(signature, message, this.bytes);
  }
}

export class EcdsaPrivateKey implements PrivateKey {
  readonly type = KeyType.PRIVATE;
  readonly algorithm = KeyAlgorithm.ECDSA;

  constructor(public readonly bytes: Uint8Array) {}

  toRawBytes(): Uint8Array {
    return this.bytes;
  }
  toBytes(container: KeyFormat): Uint8Array {
    return container.decode(this.type, this.toString(container)); // stub
  }
  toString(container: KeyFormat): string {
    return Array.from(this.bytes).map(b => b.toString(16).padStart(2, '0')).join(''); // stub
  }
  sign(message: Uint8Array): Uint8Array {
    return secp256k1.sign(message, this.bytes);
  }
  createPublicKey(): PublicKey {
    const pubBytes = secp256k1.getPublicKey(this.bytes, true); // compressed
    return new EcdsaPublicKey(pubBytes);
  }
}
