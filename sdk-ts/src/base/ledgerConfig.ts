import type { Network, ConsensusNode, MirrorNode } from './ledger.js';

/**
 * Error thrown when a network setting is not found.
 */
export class NotFoundError extends Error {
    constructor(message: string) {
        super(message);
        this.name = 'NotFoundError';
    }
}

/**
 * The full configuration to connect to a specific network.
 * @see {@link file://../../spec/base/ledger-config.md}
 */
export interface NetworkSetting {
    readonly ledger: Network<any>;
    readonly getConsensusNodes: ReadonlySet<ConsensusNode>;
    readonly getMirrorNodes: ReadonlySet<MirrorNode>;
}

export const NetworkSetting = {
    _registry: new Map<string, NetworkSetting>(),
    
    /**
     * Method to register a network configuration.
     * @param identifier - The unique identifier for the network setting.
     * @param setting - The network setting to register.
     */
    registerNetworkSetting(identifier: string, setting: NetworkSetting): void {
        this._registry.set(identifier, setting);
    },
    
    /**
     * Retrieves a registered network setting by its identifier.
     * @param identifier - The unique identifier for the network setting.
     * @returns The matched network setting.
     * @throws {NotFoundError} If no network with that identifier exists.
     */
    getNetworkSetting(identifier: string): NetworkSetting {
        const setting = this._registry.get(identifier);
        if (!setting) {
            throw new NotFoundError(`Network setting not found for identifier: ${identifier}`);
        }
        return setting;
    }
};
