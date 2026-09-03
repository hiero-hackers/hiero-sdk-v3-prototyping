import { ed25519 } from '@noble/curves/ed25519.js';
import { KeyAlgorithm, KeyType } from '../Keys.js';
export class Ed25519PublicKey {
    bytes;
    type = KeyType.PUBLIC;
    algorithm = KeyAlgorithm.ED25519;
    constructor(bytes) {
        this.bytes = bytes;
    }
    toRawBytes() {
        return this.bytes;
    }
    toBytes(container) {
        return container.decode(this.type, this.toString(container)); // stub
    }
    toString(container) {
        return Array.from(this.bytes).map(b => b.toString(16).padStart(2, '0')).join(''); // stub
    }
    verify(message, signature) {
        return ed25519.verify(signature, message, this.bytes);
    }
}
export class Ed25519PrivateKey {
    bytes;
    type = KeyType.PRIVATE;
    algorithm = KeyAlgorithm.ED25519;
    constructor(bytes) {
        this.bytes = bytes;
    }
    toRawBytes() {
        return this.bytes;
    }
    toBytes(container) {
        return container.decode(this.type, this.toString(container)); // stub
    }
    toString(container) {
        return Array.from(this.bytes).map(b => b.toString(16).padStart(2, '0')).join(''); // stub
    }
    sign(message) {
        return ed25519.sign(message, this.bytes);
    }
    createPublicKey() {
        const pubBytes = ed25519.getPublicKey(this.bytes);
        return new Ed25519PublicKey(pubBytes);
    }
}
