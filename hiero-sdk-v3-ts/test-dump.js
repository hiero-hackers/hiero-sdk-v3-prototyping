import { secp256k1 } from '@noble/curves/secp256k1.js';
const priv = new Uint8Array(32); priv.fill(1);
const sig = secp256k1.sign(new Uint8Array(32), priv);
console.log('is Uint8Array:', sig instanceof Uint8Array);
console.log('keys:', Object.getOwnPropertyNames(Object.getPrototypeOf(Object.getPrototypeOf(sig))));
