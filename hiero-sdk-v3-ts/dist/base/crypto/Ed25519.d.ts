import { KeyAlgorithm, KeyType, PrivateKey, PublicKey, KeyFormat } from '../Keys.js';
export declare class Ed25519PublicKey implements PublicKey {
    readonly bytes: Uint8Array;
    readonly type = KeyType.PUBLIC;
    readonly algorithm = KeyAlgorithm.ED25519;
    constructor(bytes: Uint8Array);
    toRawBytes(): Uint8Array;
    toBytes(container: KeyFormat): Uint8Array;
    toString(container: KeyFormat): string;
    verify(message: Uint8Array, signature: Uint8Array): boolean;
}
export declare class Ed25519PrivateKey implements PrivateKey {
    readonly bytes: Uint8Array;
    readonly type = KeyType.PRIVATE;
    readonly algorithm = KeyAlgorithm.ED25519;
    constructor(bytes: Uint8Array);
    toRawBytes(): Uint8Array;
    toBytes(container: KeyFormat): Uint8Array;
    toString(container: KeyFormat): string;
    sign(message: Uint8Array): Uint8Array;
    createPublicKey(): PublicKey;
}
