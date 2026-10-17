import { AuthorityList, of as authorityOf, PublicKeyAuthority, type Authority } from "@hiero/base/authority";
import { Hbar, HbarUnit } from "@hiero/base/hedera";
import { createPrivateKey, createPublicKey, KeyFormat, type PrivateKey, type PublicKey } from "@hiero/base/keys";
import { AccountId, Address, EvmAddress, IpAddress, type TransactionId } from "@hiero/base/ledger";
import type { NativeToken, NativeTokenUnit } from "@hiero/base/nativeToken";
import { TokenSupplyType, TokenType } from "@hiero/base/token";
import type { TokenNftInfo } from "@hiero/consensus-node-client/consensusnode/queries/tokens";
import type { TransactionStatus } from "@hiero/consensus-node-client/consensusnode/transactions";
import { Duration } from "@hiero/support";
import type { Converters } from "@hiero/tck-contract";
import { RpcError } from "./RpcError.js";

/**
 * The converter catalogue for TypeScript: inbound converters turn a JSON value of the TCK into an API value
 * (`tinybar("100")`), outbound converters (`...Json`) turn an API value into the JSON of the TCK. The interface is
 * generated from the catalogue, so a converter that is added there must be implemented here.
 */
export class TsConverters implements Converters {

    // --- inbound ---------------------------------------------------------------------------------

    string(json: unknown): string {
        return String(json);
    }

    bool(json: unknown): boolean {
        return typeof json === "boolean" ? json : String(json) === "true";
    }

    int32(json: unknown): number {
        const value = Number(json);
        if (!Number.isInteger(value) || value < -2147483648 || value > 2147483647) {
            throw new RangeError(`not a 32-bit integer: ${String(json)}`);
        }
        return value;
    }

    int64(json: unknown): bigint {
        return BigInt(String(json));
    }

    tinybar(json: unknown): NativeToken<any, NativeTokenUnit> {
        return new Hbar({ amount: this.int64(json), unit: HbarUnit.TINYBAR });
    }

    seconds(json: unknown): Duration {
        return Duration.ofSeconds(Number(this.int64(json)));
    }

    timestamp(json: unknown): Date {
        return new Date(Number(this.int64(json)) * 1000);
    }

    accountId(json: unknown): AccountId {
        return AccountId.fromString(this.string(json));
    }

    evmAccountId(json: unknown): AccountId {
        return AccountId.fromEvmAddress(0n, 0n, EvmAddress.fromBytes(this.hex(json)));
    }

    address(json: unknown): Address {
        return Address.fromString(this.string(json));
    }

    /**
     * A key: a DER-encoded private key (its public key is used), a DER-encoded public key, or the protobuf `Key` of
     * a key list or threshold key.
     */
    key(json: unknown): Authority {
        const bytes = this.hex(json);
        try {
            return authorityOf(createPrivateKey(KeyFormat.PKCS8_WITH_DER, bytes).createPublicKey());
        } catch (notPrivate) {
            if (!(notPrivate instanceof RangeError)) {
                throw notPrivate;
            }
            try {
                return authorityOf(createPublicKey(KeyFormat.SPKI_WITH_DER, bytes));
            } catch (notPublic) {
                if (!(notPublic instanceof RangeError)) {
                    throw notPublic;
                }
                throw RpcError.gap("a key list or threshold key cannot be read: Authority has no fromBytes");
            }
        }
    }

    hex(json: unknown): Uint8Array {
        const text = this.string(json);
        return Uint8Array.from(Buffer.from(text.startsWith("0x") ? text.substring(2) : text, "hex"));
    }

    utf8(json: unknown): Uint8Array {
        return new TextEncoder().encode(this.string(json));
    }

    /** A JSON list of keys that must all sign; an empty list is no key (`null`: the attribute stays absent). */
    keyList(json: unknown): Authority {
        if (!Array.isArray(json)) {
            throw new TypeError(`not a list of keys: ${String(json)}`);
        }
        if (json.length === 0) {
            // the runtime treats a null value as absent; the contract has no nullable converters
            return null as unknown as Authority;
        }
        return authorityOf(...json.map((key: unknown) => this.key(key)));
    }

    /** `ft` / `nft`, or the name of the constant. */
    tokenType(json: unknown): TokenType {
        switch (this.string(json).toLowerCase()) {
            case "ft":
            case "fungible_common":
                return TokenType.FUNGIBLE_COMMON;
            case "nft":
            case "non_fungible_unique":
                return TokenType.NON_FUNGIBLE_UNIQUE;
            default:
                throw new RangeError(`not a token type: ${String(json)}`);
        }
    }

    tokenSupplyType(json: unknown): TokenSupplyType {
        const name = this.string(json).toUpperCase();
        const type = TokenSupplyType.values().find((value) => value.name === name);
        if (type === undefined) {
            throw new RangeError(`not a token supply type: ${String(json)}`);
        }
        return type;
    }

    nftIdToken(json: unknown): Address {
        return this.address(TsConverters.#nftIdPart(json, 0));
    }

    nftIdSerial(json: unknown): bigint {
        return this.int64(TsConverters.#nftIdPart(json, 1));
    }

    ipAddress(json: unknown): IpAddress {
        return IpAddress.fromBytes(this.hex(json));
    }

    static #nftIdPart(json: unknown, index: number): string {
        const parts = String(json).split("/");
        const part = parts[index];
        if (parts.length !== 2 || part === undefined) {
            throw new RangeError(`not an NFT ID (tokenId/serial): ${String(json)}`);
        }
        return part;
    }

    // --- outbound --------------------------------------------------------------------------------

    stringJson(value: string): unknown {
        return value;
    }

    boolJson(value: boolean): unknown {
        return value;
    }

    int32Json(value: number): unknown {
        return String(value);
    }

    int64Json(value: bigint): unknown {
        return value.toString();
    }

    tinybarJson(value: NativeToken<any, NativeTokenUnit>): unknown {
        return value.toBaseUnits().toString();
    }

    secondsJson(value: Duration): unknown {
        return String(value.toSeconds());
    }

    timestampJson(value: Date): unknown {
        return String(Math.trunc(value.getTime() / 1000));
    }

    accountIdJson(value: AccountId): unknown {
        return value.toString();
    }

    evmAddressJson(value: EvmAddress): unknown {
        return Buffer.from(value.bytes).toString("hex");
    }

    addressJson(value: Address): unknown {
        return value.toString();
    }

    keyJson(value: Authority): unknown {
        if (value instanceof PublicKeyAuthority) {
            return Buffer.from(value.publicKey.toBytes(KeyFormat.SPKI_WITH_DER)).toString("hex");
        }
        throw RpcError.gap("a key list or threshold key cannot be written: Authority has no toBytes");
    }

    hexJson(value: Uint8Array): unknown {
        return Buffer.from(value).toString("hex");
    }

    /** The name of a status: the name of the enum constant, otherwise the numeric code. */
    statusJson(value: TransactionStatus): unknown {
        const named = value as TransactionStatus & { readonly name?: unknown };
        return typeof named.name === "string" ? named.name : String(value.code);
    }

    numberJson(value: number): unknown {
        return value;
    }

    utf8Json(value: Uint8Array): unknown {
        return new TextDecoder().decode(value);
    }

    /** The keys of a list that must all sign, or the single key. */
    keyListJson(value: Authority): unknown {
        if (value instanceof AuthorityList && value.threshold === value.children.length) {
            return value.children.map((child) => this.keyJson(child));
        }
        return [this.keyJson(value)];
    }

    publicKeyJson(value: PublicKey): unknown {
        return Buffer.from(value.toBytes(KeyFormat.SPKI_WITH_DER)).toString("hex");
    }

    tokenTypeJson(value: TokenType): unknown {
        return value.name;
    }

    tokenSupplyTypeJson(value: TokenSupplyType): unknown {
        return value.name;
    }

    nftIdJson(value: TokenNftInfo): unknown {
        return `${value.tokenId.toString()}/${value.serial.toString()}`;
    }

    transactionIdJson(value: TransactionId): unknown {
        return value.toString();
    }

    // --- used by the runtime itself --------------------------------------------------------------

    /** A DER-encoded private key (for `setup` and signers). */
    privateKey(json: unknown): PrivateKey {
        return createPrivateKey(KeyFormat.PKCS8_WITH_DER, this.hex(json));
    }
}
