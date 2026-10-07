/**
 * The key operations behind `PrivateKey`, `PublicKey` and the factory functions: Ed25519 and ECDSA secp256k1 over
 * `@noble/curves`, PKCS#8 and SPKI over the small DER codec next to this file.
 *
 * Not public API — the directory is absent from the `exports` map of the package.
 *
 * Every failure is a `RangeError`: `TsConverters.key()` of the TCK runtime catches exactly that to fall back from
 * "this is a private key" to "this is a public key". Any other error type breaks that fallback.
 */
import { ed25519 } from "@noble/curves/ed25519.js";
import { secp256k1 } from "@noble/curves/secp256k1.js";
import { keccak_256 } from "@noble/hashes/sha3.js";
import { KeyAlgorithm } from "../keys/KeyAlgorithm.js";
import * as der from "./der.js";

/** The seed of an Ed25519 key and the scalar of an ECDSA key both have 32 bytes. */
const SECRET_LENGTH = 32;

/** A parsed key: its algorithm and its raw material. */
export interface RawKey {
    readonly algorithm: KeyAlgorithm;
    readonly bytes: Uint8Array;
}

function require32(bytes: Uint8Array, what: string): Uint8Array {
    if (bytes.length !== SECRET_LENGTH) {
        throw new RangeError(`${what} has ${SECRET_LENGTH} bytes, not ${bytes.length}`);
    }
    return bytes;
}

/** Creates a random private key of an algorithm. */
export function generate(algorithm: KeyAlgorithm): Uint8Array {
    return algorithm === KeyAlgorithm.ECDSA ? secp256k1.utils.randomSecretKey() : ed25519.utils.randomSecretKey();
}

/** Derives the raw public key of a raw private key. */
export function publicOf(algorithm: KeyAlgorithm, secret: Uint8Array): Uint8Array {
    return algorithm === KeyAlgorithm.ECDSA
        ? secp256k1.getPublicKey(require32(secret, "An ECDSA secp256k1 private key"), true)
        : ed25519.getPublicKey(require32(secret, "An Ed25519 private key"));
}

/**
 * Signs a message. Ed25519 signs the message itself; ECDSA signs its Keccak-256 digest with a deterministic nonce
 * and returns the 64 bytes `r‖s`, which is what the Hiero API expects.
 */
export function sign(algorithm: KeyAlgorithm, secret: Uint8Array, message: Uint8Array): Uint8Array {
    return algorithm === KeyAlgorithm.ECDSA
        ? secp256k1.sign(keccak_256(message), secret, { prehash: false })
        : ed25519.sign(message, secret);
}

/** Verifies a signature against a raw public key. */
export function verify(algorithm: KeyAlgorithm, publicKey: Uint8Array, message: Uint8Array,
                       signature: Uint8Array): boolean {
    try {
        return algorithm === KeyAlgorithm.ECDSA
            ? secp256k1.verify(signature, keccak_256(message), publicKey, { prehash: false })
            : ed25519.verify(signature, message, publicKey);
    } catch {
        return false;
    }
}

/** Encodes a raw private key as a PKCS#8 `PrivateKeyInfo`. */
export function toPkcs8(algorithm: KeyAlgorithm, secret: Uint8Array): Uint8Array {
    if (algorithm === KeyAlgorithm.ECDSA) {
        const sec1 = der.sequence(
            der.integer(1),
            der.octetString(secret),
            der.context(1, der.bitString(publicOf(algorithm, secret))));
        return der.sequence(
            der.integer(0),
            der.sequence(der.OID_EC_PUBLIC_KEY, der.OID_SECP256K1),
            der.octetString(sec1));
    }
    return der.sequence(
        der.integer(0),
        der.sequence(der.OID_ED25519),
        der.octetString(der.octetString(secret)));
}

/** Encodes a raw public key as an SPKI `SubjectPublicKeyInfo`. */
export function toSpki(algorithm: KeyAlgorithm, publicKey: Uint8Array): Uint8Array {
    return algorithm === KeyAlgorithm.ECDSA
        ? der.sequence(der.sequence(der.OID_EC_PUBLIC_KEY, der.OID_SECP256K1), der.bitString(publicKey))
        : der.sequence(der.sequence(der.OID_ED25519), der.bitString(publicKey));
}

/**
 * Reads a PKCS#8 `PrivateKeyInfo`. The algorithm comes from its object identifier, never from the length of the key
 * material — both algorithms use 32 bytes.
 *
 * @throws RangeError if the bytes are no PKCS#8 key of a supported algorithm
 */
export function fromPkcs8(bytes: Uint8Array): RawKey {
    const items = parts(bytes, 3, "PKCS#8 private key");
    const version = at(items, 0);
    const algorithm = at(items, 1);
    const key = at(items, 2);
    if (version.tag !== 0x02) {
        throw new RangeError("Not a PKCS#8 private key");
    }
    const identifiers = der.readAll(algorithm.content);
    const oid = identifiers[0];
    if (oid === undefined || key.tag !== 0x04) {
        throw new RangeError("Not a PKCS#8 private key");
    }
    if (der.equal(der.encoded(oid), der.OID_ED25519)) {
        const inner = der.read(key.content);
        return { algorithm: KeyAlgorithm.ED25519, bytes: require32(inner.content, "An Ed25519 private key") };
    }
    if (der.equal(der.encoded(oid), der.OID_EC_PUBLIC_KEY)) {
        requireSecp256k1(identifiers[1]);
        const sec1 = der.readAll(der.read(key.content).content);
        const secret = sec1[1];
        if (secret === undefined || secret.tag !== 0x04) {
            throw new RangeError("Malformed SEC1 private key");
        }
        return { algorithm: KeyAlgorithm.ECDSA, bytes: require32(secret.content, "An ECDSA private key") };
    }
    throw new RangeError("Unsupported PKCS#8 key algorithm");
}

/**
 * Reads an SPKI `SubjectPublicKeyInfo`.
 *
 * @throws RangeError if the bytes are no SPKI key of a supported algorithm
 */
export function fromSpki(bytes: Uint8Array): RawKey {
    const items = parts(bytes, 2, "SPKI public key");
    const algorithm = at(items, 0);
    const key = at(items, 1);
    if (key.tag !== 0x03 || key.content.length === 0) {
        throw new RangeError("Not an SPKI public key");
    }
    // the leading byte of a BIT STRING is the number of unused bits
    const material = key.content.subarray(1);
    const identifiers = der.readAll(algorithm.content);
    const oid = identifiers[0];
    if (oid === undefined) {
        throw new RangeError("Not an SPKI public key");
    }
    if (der.equal(der.encoded(oid), der.OID_ED25519)) {
        return { algorithm: KeyAlgorithm.ED25519, bytes: require32(material, "An Ed25519 public key") };
    }
    if (der.equal(der.encoded(oid), der.OID_EC_PUBLIC_KEY)) {
        requireSecp256k1(identifiers[1]);
        return { algorithm: KeyAlgorithm.ECDSA, bytes: compress(material) };
    }
    throw new RangeError("Unsupported SPKI key algorithm");
}

/** The item at an index; the callers have checked the length, this satisfies `noUncheckedIndexedAccess`. */
function at(items: ReadonlyArray<der.Tlv>, index: number): der.Tlv {
    const item = items[index];
    if (item === undefined) {
        throw new RangeError("Truncated DER structure");
    }
    return item;
}

function parts(bytes: Uint8Array, count: number, what: string): ReadonlyArray<der.Tlv> {
    let outer: der.Tlv;
    try {
        outer = der.read(bytes);
    } catch (malformed) {
        throw new RangeError(`Not a ${what}: ${String(malformed)}`);
    }
    if (outer.tag !== 0x30) {
        throw new RangeError(`Not a ${what}`);
    }
    const items = der.readAll(outer.content);
    if (items.length < count) {
        throw new RangeError(`Not a ${what}`);
    }
    return items;
}

function requireSecp256k1(parameters: der.Tlv | undefined): void {
    if (parameters === undefined || !der.equal(der.encoded(parameters), der.OID_SECP256K1)) {
        throw new RangeError("Unsupported elliptic curve, Hiero keys use secp256k1");
    }
}

/** Normalises an encoded secp256k1 point to its 33 compressed bytes. */
export function compress(point: Uint8Array): Uint8Array {
    try {
        return secp256k1.Point.fromBytes(point).toBytes(true);
    } catch (invalid) {
        throw new RangeError(`Not a point on secp256k1: ${String(invalid)}`);
    }
}
