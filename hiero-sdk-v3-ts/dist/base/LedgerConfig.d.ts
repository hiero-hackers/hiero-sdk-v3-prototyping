import { NativeTokenUnit } from './NativeToken.js';
import { AccountId } from './Address.js';
export interface Network<Unit extends NativeTokenUnit> {
    readonly id: Uint8Array;
    readonly name: string | null;
    readonly nativeTokenUnit: Unit;
}
export interface IpAddress {
    readonly bytes: Uint8Array;
    toString(): string;
}
export interface ConsensusNode {
    readonly ip: IpAddress;
    readonly port: number;
    readonly account: AccountId;
}
export interface MirrorNode {
    readonly restBaseUrl: string;
}
export interface NetworkSetting {
    readonly network: Network<any>;
    readonly getConsensusNodes: Set<ConsensusNode>;
    readonly getMirrorNodes: Set<MirrorNode>;
}
export interface TransactionId {
    readonly accountId: AccountId;
    readonly validStart: Date;
    readonly nonce: number | null;
    toString(): string;
    toStringWithChecksum(): string;
}
