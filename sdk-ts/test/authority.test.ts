import { describe, it, expect } from 'vitest';
import { Authority } from '../src/base/authority.js';
import { generatePrivateKey, KeyAlgorithm } from '../src/base/keys.js';
import { ContractId } from '../src/base/ledger.js';

describe('Authority', () => {
    it('should create a PublicKeyAuthority', () => {
        const privKey = generatePrivateKey(KeyAlgorithm.ED25519);
        const pubKey = privKey.createPublicKey();
        const auth = Authority.of(pubKey);
        
        expect(auth.kind).toBe('PublicKeyAuthority');
        if (auth.kind === 'PublicKeyAuthority') {
            expect(auth.publicKey).toBe(pubKey);
        }
    });

    it('should create a ContractAuthority', () => {
        const contractId = ContractId.fromString("0.0.1234");
        const auth = Authority.ofContract(contractId);
        
        expect(auth.kind).toBe('ContractAuthority');
        if (auth.kind === 'ContractAuthority') {
            expect(auth.contractId).toBe(contractId);
            expect(auth.delegatable).toBe(false);
        }
    });

    it('should create a delegatable ContractAuthority', () => {
        const contractId = ContractId.fromString("0.0.1234");
        const auth = Authority.ofDelegatable(contractId);
        
        expect(auth.kind).toBe('ContractAuthority');
        if (auth.kind === 'ContractAuthority') {
            expect(auth.contractId).toBe(contractId);
            expect(auth.delegatable).toBe(true);
        }
    });

    it('should create an n-of-n AuthorityList', () => {
        const priv1 = generatePrivateKey(KeyAlgorithm.ED25519);
        const auth1 = Authority.of(priv1.createPublicKey());
        
        const priv2 = generatePrivateKey(KeyAlgorithm.ED25519);
        const auth2 = Authority.of(priv2.createPublicKey());
        
        const authList = Authority.of(auth1, auth2);
        
        expect(authList.kind).toBe('AuthorityList');
        if (authList.kind === 'AuthorityList') {
            expect(authList.children.length).toBe(2);
            expect(authList.children[0]).toBe(auth1);
            expect(authList.children[1]).toBe(auth2);
            expect(authList.threshold).toBe(2);
        }
    });

    it('should create an m-of-n AuthorityList', () => {
        const priv1 = generatePrivateKey(KeyAlgorithm.ED25519);
        const auth1 = Authority.of(priv1.createPublicKey());
        
        const priv2 = generatePrivateKey(KeyAlgorithm.ED25519);
        const auth2 = Authority.of(priv2.createPublicKey());
        
        const priv3 = generatePrivateKey(KeyAlgorithm.ED25519);
        const auth3 = Authority.of(priv3.createPublicKey());
        
        const authList = Authority.of(2, auth1, auth2, auth3);
        
        expect(authList.kind).toBe('AuthorityList');
        if (authList.kind === 'AuthorityList') {
            expect(authList.children.length).toBe(3);
            expect(authList.threshold).toBe(2);
        }
    });

    it('should throw an error for invalid threshold in AuthorityList', () => {
        const priv1 = generatePrivateKey(KeyAlgorithm.ED25519);
        const auth1 = Authority.of(priv1.createPublicKey());
        
        expect(() => Authority.of(0, auth1)).toThrowError();
        expect(() => Authority.of(2, auth1)).toThrowError();
    });
});
