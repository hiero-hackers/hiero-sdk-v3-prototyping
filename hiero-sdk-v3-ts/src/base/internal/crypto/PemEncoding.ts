import { KeyType } from '../../Keys.js';

function toBase64(bytes: Uint8Array): string {
  if (typeof Buffer !== 'undefined') {
    return Buffer.from(bytes).toString('base64');
  }
  let binary = '';
  for (let i = 0; i < bytes.length; i++) {
    binary += String.fromCharCode(bytes[i]);
  }
  return btoa(binary);
}

function fromBase64(b64: string): Uint8Array {
  if (typeof Buffer !== 'undefined') {
    return new Uint8Array(Buffer.from(b64, 'base64'));
  }
  const binary = atob(b64);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i++) {
    bytes[i] = binary.charCodeAt(i);
  }
  return bytes;
}

export function encodePem(derBytes: Uint8Array, keyType: KeyType): string {
  const b64 = toBase64(derBytes);
  // Split into 64-character lines
  const lines = b64.match(/.{1,64}/g) || [];
  
  if (keyType === KeyType.PRIVATE) {
    return `-----BEGIN PRIVATE KEY-----\n${lines.join('\n')}\n-----END PRIVATE KEY-----`;
  } else {
    return `-----BEGIN PUBLIC KEY-----\n${lines.join('\n')}\n-----END PUBLIC KEY-----`;
  }
}

export function decodePem(pem: string, keyType: KeyType): Uint8Array {
  // Enforce correct headers based on keyType
  const isPrivate = pem.includes('BEGIN PRIVATE KEY') || pem.includes('BEGIN EC PRIVATE KEY');
  const isPublic = pem.includes('BEGIN PUBLIC KEY');

  if (keyType === KeyType.PRIVATE && !isPrivate) throw new Error('illegal-format');
  if (keyType === KeyType.PUBLIC && !isPublic) throw new Error('illegal-format');

  const b64 = pem
    .replace(/-----BEGIN (.*)-----|-----END (.*)-----/g, "")
    .replace(/Proc-Type:.*/g, "")
    .replace(/DEK-Info:.*/g, "")
    .replace(/\s/g, "");

  return fromBase64(b64);
}
