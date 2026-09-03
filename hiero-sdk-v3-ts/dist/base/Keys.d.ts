export declare enum KeyType {
    PUBLIC = "PUBLIC",
    PRIVATE = "PRIVATE"
}
export declare enum KeyAlgorithm {
    ED25519 = "ED25519",
    ECDSA = "ECDSA"
}
export declare enum RawFormat {
    STRING = "STRING",
    BYTES = "BYTES"
}
export interface KeyEncoding {
    readonly rawFormat: RawFormat;
    decode(keyType: KeyType, value: string): Uint8Array;
}
export interface KeyContainer {
    supportsType(type: KeyType): boolean;
}
export interface ByteImportEncoding {
    decode(value: string): Uint8Array;
}
export interface KeyFormat {
    readonly container: KeyContainer;
    readonly encoding: KeyEncoding;
    supportsType(type: KeyType): boolean;
    decode(keyType: KeyType, value: string): Uint8Array;
}
export interface Key {
    readonly bytes: Uint8Array;
    readonly algorithm: KeyAlgorithm;
    readonly type: KeyType;
    toRawBytes(): Uint8Array;
    toBytes(container: KeyFormat): Uint8Array;
    toString(container: KeyFormat): string;
}
export interface PublicKey extends Key {
    readonly type: KeyType.PUBLIC;
    verify(message: Uint8Array, signature: Uint8Array): boolean;
}
export interface PrivateKey extends Key {
    readonly type: KeyType.PRIVATE;
    sign(message: Uint8Array): Uint8Array;
    createPublicKey(): PublicKey;
}
export interface KeyPair {
    readonly publicKey: PublicKey;
    readonly privateKey: PrivateKey;
}
export declare function generatePrivateKey(algorithm: KeyAlgorithm): PrivateKey;
export declare function generatePublicKey(algorithm: KeyAlgorithm): PublicKey;
export declare function createPrivateKeyFromRawBytes(algorithm: KeyAlgorithm, rawBytes: Uint8Array): PrivateKey;
export declare function createPublicKeyFromRawBytes(algorithm: KeyAlgorithm, rawBytes: Uint8Array): PublicKey;
export declare function createPrivateKey(value: string | Uint8Array, container?: KeyFormat): PrivateKey;
export declare function createPublicKey(value: string | Uint8Array, container?: KeyFormat): PublicKey;
