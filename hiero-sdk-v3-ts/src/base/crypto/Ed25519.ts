import { ed25519 } from '@noble/curves/ed25519.js';
import { KeyAlgorithm, KeyType, PrivateKey, PublicKey, KeyFormat } from '../Keys.js';

export class Ed25519PublicKey implements PublicKey {
  readonly type = KeyType.PUBLIC;
  readonly algorithm = KeyAlgorithm.ED25519;

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
    return ed25519.verify(signature, message, this.bytes);
  }
}

export class Ed25519PrivateKey implements PrivateKey {
  readonly type = KeyType.PRIVATE;
  readonly algorithm = KeyAlgorithm.ED25519;

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
    return ed25519.sign(message, this.bytes);
  }
  createPublicKey(): PublicKey {
    const pubBytes = ed25519.getPublicKey(this.bytes);
    return new Ed25519PublicKey(pubBytes);
  }
}
