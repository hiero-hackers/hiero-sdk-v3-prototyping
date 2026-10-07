/**
 * The small part of DER the key formats need: definite-length TLVs, four fixed object identifiers and the two
 * structures PKCS#8 and SPKI are built from.
 *
 * Not public API — the directory is absent from the `exports` map of the package.
 */

/** `id-Ed25519` (RFC 8410), already DER-encoded. */
export const OID_ED25519 = Uint8Array.of(0x06, 0x03, 0x2b, 0x65, 0x70);

/** `id-ecPublicKey` (RFC 5480), already DER-encoded. */
export const OID_EC_PUBLIC_KEY = Uint8Array.of(0x06, 0x07, 0x2a, 0x86, 0x48, 0xce, 0x3d, 0x02, 0x01);

/** `secp256k1` (SEC 2), already DER-encoded. */
export const OID_SECP256K1 = Uint8Array.of(0x06, 0x05, 0x2b, 0x81, 0x04, 0x00, 0x0a);

/** A parsed tag-length-value triple. */
export interface Tlv {
    readonly tag: number;
    readonly content: Uint8Array;
    /** The offset just behind this TLV. */
    readonly next: number;
}

function concat(parts: ReadonlyArray<Uint8Array>): Uint8Array {
    const total = parts.reduce((sum, part) => sum + part.length, 0);
    const bytes = new Uint8Array(total);
    let offset = 0;
    for (const part of parts) {
        bytes.set(part, offset);
        offset += part.length;
    }
    return bytes;
}

function length(size: number): Uint8Array {
    if (size < 0x80) {
        return Uint8Array.of(size);
    }
    const digits: number[] = [];
    for (let rest = size; rest > 0; rest = Math.floor(rest / 256)) {
        digits.unshift(rest % 256);
    }
    return Uint8Array.of(0x80 | digits.length, ...digits);
}

/** Wraps content in a tag and its length. */
export function tlv(tag: number, content: Uint8Array): Uint8Array {
    return concat([Uint8Array.of(tag), length(content.length), content]);
}

/** A `SEQUENCE` of already-encoded parts. */
export function sequence(...parts: ReadonlyArray<Uint8Array>): Uint8Array {
    return tlv(0x30, concat(parts));
}

/** An `INTEGER` of a small non-negative value. */
export function integer(value: number): Uint8Array {
    return tlv(0x02, value < 0x80 ? Uint8Array.of(value) : Uint8Array.of(0x00, value));
}

/** An `OCTET STRING`. */
export function octetString(content: Uint8Array): Uint8Array {
    return tlv(0x04, content);
}

/** A `BIT STRING` with no unused bits. */
export function bitString(content: Uint8Array): Uint8Array {
    return tlv(0x03, concat([Uint8Array.of(0x00), content]));
}

/** A context-specific constructed tag, e.g. `[1]` of a SEC1 private key. */
export function context(number: number, content: Uint8Array): Uint8Array {
    return tlv(0xa0 | number, content);
}

/**
 * Reads the TLV at an offset.
 *
 * @throws RangeError if the bytes are no well-formed definite-length DER
 */
export function read(bytes: Uint8Array, offset = 0): Tlv {
    if (offset + 2 > bytes.length) {
        throw new RangeError("Truncated DER");
    }
    const tag = bytes[offset] as number;
    const first = bytes[offset + 1] as number;
    let size = first;
    let start = offset + 2;
    if (first >= 0x80) {
        const count = first & 0x7f;
        if (count === 0 || offset + 2 + count > bytes.length) {
            throw new RangeError("Unsupported DER length");
        }
        size = 0;
        for (let i = 0; i < count; i++) {
            size = size * 256 + (bytes[offset + 2 + i] as number);
        }
        start = offset + 2 + count;
    }
    if (start + size > bytes.length) {
        throw new RangeError("Truncated DER");
    }
    return { tag, content: bytes.subarray(start, start + size), next: start + size };
}

/** Reads every TLV of a content block. */
export function readAll(content: Uint8Array): ReadonlyArray<Tlv> {
    const items: Tlv[] = [];
    for (let offset = 0; offset < content.length;) {
        const item = read(content, offset);
        items.push(item);
        offset = item.next;
    }
    return items;
}

/** Whether two byte sequences are equal. */
export function equal(left: Uint8Array, right: Uint8Array): boolean {
    return left.length === right.length && left.every((byte, index) => byte === right[index]);
}

/** The DER encoding of a TLV, i.e. tag and length in front of the content again. */
export function encoded(item: Tlv): Uint8Array {
    return tlv(item.tag, item.content);
}
