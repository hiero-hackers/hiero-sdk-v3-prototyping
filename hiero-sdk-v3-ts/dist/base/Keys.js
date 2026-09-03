export var KeyType;
(function (KeyType) {
    KeyType["PUBLIC"] = "PUBLIC";
    KeyType["PRIVATE"] = "PRIVATE";
})(KeyType || (KeyType = {}));
export var KeyAlgorithm;
(function (KeyAlgorithm) {
    KeyAlgorithm["ED25519"] = "ED25519";
    KeyAlgorithm["ECDSA"] = "ECDSA";
})(KeyAlgorithm || (KeyAlgorithm = {}));
export var RawFormat;
(function (RawFormat) {
    RawFormat["STRING"] = "STRING";
    RawFormat["BYTES"] = "BYTES";
})(RawFormat || (RawFormat = {}));
import { randomBytes } from "./crypto/random.js";
import { Ed25519PrivateKey, Ed25519PublicKey } from './crypto/Ed25519.js';
import { EcdsaPrivateKey, EcdsaPublicKey } from './crypto/Ecdsa.js';
export function generatePrivateKey(algorithm) {
    if (algorithm === KeyAlgorithm.ED25519) {
        return new Ed25519PrivateKey(randomBytes(32));
    }
    else {
        return new EcdsaPrivateKey(randomBytes(32));
    }
}
export function generatePublicKey(algorithm) {
    return generatePrivateKey(algorithm).createPublicKey();
}
export function createPrivateKeyFromRawBytes(algorithm, rawBytes) {
    if (algorithm === KeyAlgorithm.ED25519) {
        return new Ed25519PrivateKey(rawBytes);
    }
    else {
        return new EcdsaPrivateKey(rawBytes);
    }
}
export function createPublicKeyFromRawBytes(algorithm, rawBytes) {
    if (algorithm === KeyAlgorithm.ED25519) {
        return new Ed25519PublicKey(rawBytes);
    }
    else {
        return new EcdsaPublicKey(rawBytes);
    }
}
export function createPrivateKey(value, container) {
    // stub implementation for abstract factory
    throw new Error('Not implemented completely');
}
export function createPublicKey(value, container) {
    // stub implementation for abstract factory
    throw new Error('Not implemented completely');
}
