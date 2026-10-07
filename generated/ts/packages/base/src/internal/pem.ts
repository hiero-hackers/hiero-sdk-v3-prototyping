/**
 * The PEM envelope of a DER-encoded key. Not public API — the directory is absent from the `exports` map.
 */
import { KeyType } from "../keys/KeyType.js";

const LINE_LENGTH = 64;

function label(keyType: KeyType): string {
    return keyType === KeyType.PRIVATE ? "PRIVATE KEY" : "PUBLIC KEY";
}

/** Wraps DER bytes in a PEM envelope. */
export function toPem(keyType: KeyType, der: Uint8Array): string {
    const base64 = Buffer.from(der).toString("base64");
    const lines: string[] = [];
    for (let start = 0; start < base64.length; start += LINE_LENGTH) {
        lines.push(base64.slice(start, start + LINE_LENGTH));
    }
    return `-----BEGIN ${label(keyType)}-----\n${lines.join("\n")}\n-----END ${label(keyType)}-----\n`;
}

/**
 * Reads the DER bytes out of a PEM envelope.
 *
 * @throws RangeError if the text is no PEM envelope of that type
 */
export function fromPem(keyType: KeyType, pem: string): Uint8Array {
    const lines = pem.replaceAll("\r", "\n").split("\n").map(line => line.trim()).filter(line => line !== "");
    const header = `-----BEGIN ${label(keyType)}-----`;
    const footer = `-----END ${label(keyType)}-----`;
    if (lines.length < 2 || lines[0] !== header || lines[lines.length - 1] !== footer) {
        throw new RangeError(`Not a PEM envelope of type ${keyType.name}`);
    }
    const payload = lines.slice(1, -1).join("").replace(/[^A-Za-z0-9+/=]/g, "");
    return Uint8Array.from(Buffer.from(payload, "base64"));
}
