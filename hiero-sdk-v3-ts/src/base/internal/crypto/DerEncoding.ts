import { KeyAlgorithm, KeyType, KeyContainer, KeyFormat } from '../../Keys.js';

function decodeHex(hex: string): Uint8Array {
  const arr = new Uint8Array(hex.length / 2);
  for (let i = 0; i < arr.length; i++) {
    arr[i] = parseInt(hex.substring(i * 2, i * 2 + 2), 16);
  }
  return arr;
}

const ED25519_PRIV = decodeHex("302e020100300506032b657004220420");
const ED25519_PUB = decodeHex("302a300506032b6570032100");
const ECDSA_PRIV = decodeHex("3030020100300706052b8104000a04220420");
const ECDSA_PRIV_2 = decodeHex("30540201010420");
const ECDSA_PUB = decodeHex("3036301006072a8648ce3d020106052b8104000a032200");
const ECDSA_PUB_LEGACY = decodeHex("302d300706052b8104000a032200");

function startsWith(data: Uint8Array, prefix: Uint8Array): boolean {
  if (data.length < prefix.length) return false;
  for (let i = 0; i < prefix.length; i++) {
    if (data[i] !== prefix[i]) return false;
  }
  return true;
}

export function unwrapDer(data: Uint8Array, algorithm: KeyAlgorithm, keyType: KeyType, container: KeyContainer): Uint8Array {
  if (keyType === KeyType.PRIVATE && container !== KeyContainer.PKCS8) {
    throw new Error('illegal-format'); // Private keys only support PKCS8
  }
  if (keyType === KeyType.PUBLIC && container !== KeyContainer.SPKI) {
    throw new Error('illegal-format'); // Public keys only support SPKI
  }

  if (algorithm === KeyAlgorithm.ED25519) {
    if (keyType === KeyType.PRIVATE) {
      if (!startsWith(data, ED25519_PRIV)) throw new Error('illegal-format');
      return data.subarray(ED25519_PRIV.length);
    } else {
      if (!startsWith(data, ED25519_PUB)) throw new Error('illegal-format');
      return data.subarray(ED25519_PUB.length);
    }
  } else {
    // ECDSA
    if (keyType === KeyType.PRIVATE) {
      if (startsWith(data, ECDSA_PRIV)) return data.subarray(ECDSA_PRIV.length);
      if (startsWith(data, ECDSA_PRIV_2)) {
        // Legacy parsing - grab 32 bytes after prefix
        return data.subarray(ECDSA_PRIV_2.length, ECDSA_PRIV_2.length + 32);
      }
      throw new Error('illegal-format');
    } else {
      if (startsWith(data, ECDSA_PUB)) return data.subarray(ECDSA_PUB.length, ECDSA_PUB.length + 33);
      if (startsWith(data, ECDSA_PUB_LEGACY)) return data.subarray(ECDSA_PUB_LEGACY.length);
      throw new Error('illegal-format');
    }
  }
}

export function wrapDer(rawBytes: Uint8Array, algorithm: KeyAlgorithm, keyType: KeyType, container: KeyContainer): Uint8Array {
  if (keyType === KeyType.PRIVATE && container !== KeyContainer.PKCS8) throw new Error('illegal-format');
  if (keyType === KeyType.PUBLIC && container !== KeyContainer.SPKI) throw new Error('illegal-format');

  let prefix: Uint8Array;
  if (algorithm === KeyAlgorithm.ED25519) {
    prefix = keyType === KeyType.PRIVATE ? ED25519_PRIV : ED25519_PUB;
  } else {
    prefix = keyType === KeyType.PRIVATE ? ECDSA_PRIV : ECDSA_PUB_LEGACY;
  }

  const result = new Uint8Array(prefix.length + rawBytes.length);
  result.set(prefix, 0);
  result.set(rawBytes, prefix.length);
  return result;
}
