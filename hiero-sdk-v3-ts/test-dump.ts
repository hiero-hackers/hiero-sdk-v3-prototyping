import { secp256k1 } from '@noble/curves/secp256k1.js';
import { randomBytes } from './src/base/crypto/random.js';
const priv = randomBytes(32);
const sig = secp256k1.sign(new Uint8Array(32), priv);
console.log(Object.keys(Object.getPrototypeOf(sig)));
