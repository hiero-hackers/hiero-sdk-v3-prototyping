import { secp256k1 } from '@noble/curves/secp256k1.js';
import { KeyAlgorithm, KeyType } from '../Keys.js';
export class EcdsaPublicKey {
    bytes;
    type = KeyType.PUBLIC;
    algorithm = KeyAlgorithm.ECDSA;
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
        return secp256k1.verify(signature, message, this.bytes);
    }
}
export class EcdsaPrivateKey {
    bytes;
    type = KeyType.PRIVATE;
    algorithm = KeyAlgorithm.ECDSA;
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
        return secp256k1.sign(message, this.bytes);
    }
    createPublicKey() {
        const pubBytes = secp256k1.getPublicKey(this.bytes, true); // compressed
        return new EcdsaPublicKey(pubBytes);
    }
}
