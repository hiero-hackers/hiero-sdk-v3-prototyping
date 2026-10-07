import { of as authorityOf, PublicKeyAuthority, type Authority } from "@hiero/base/authority";
import { Hbar, HbarUnit } from "@hiero/base/hedera";
import { createPrivateKey, createPublicKey, KeyFormat, type PrivateKey } from "@hiero/base/keys";
import { AccountId, Address, EvmAddress } from "@hiero/base/ledger";
import type { NativeToken, NativeTokenUnit } from "@hiero/base/nativeToken";
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

    // --- used by the runtime itself --------------------------------------------------------------

    /** A DER-encoded private key (for `setup` and signers). */
    privateKey(json: unknown): PrivateKey {
        return createPrivateKey(KeyFormat.PKCS8_WITH_DER, this.hex(json));
    }
}
