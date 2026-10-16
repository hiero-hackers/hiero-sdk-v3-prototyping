package org.hiero.authority;

import java.util.List;
import org.hiero.keys.PublicKey;
import org.hiero.ledger.ContractId;

/// Factory methods of the package `org.hiero.authority`.
public final class AuthorityFactory {

    /// n-of-n composition ("all must sign"): threshold is set to children.size().
    public static Authority of(final Authority... children) {
        return of(children.length, children);
    }

    /// m-of-n composition (multi-signature): at least `threshold` of `children` must be satisfied.
    public static Authority of(final int threshold, final Authority... children) {
        // the upper bound (at most children.length) is not part of the spec yet, see `## Questions & Comments` of
        // authority.md; the network rejects a threshold above the number of keys
        if (threshold < 1) {
            throw new IllegalArgumentException("threshold must be at least 1, not " + threshold);
        }
        return new AuthorityList(List.of(children), threshold);
    }

    /// Single-key leaf.
    public static Authority of(final PublicKey publicKey) {
        return new PublicKeyAuthority(publicKey);
    }

    /// Plain contract leaf (HAPI ContractID).
    public static Authority ofContract(final ContractId contractId) {
        return new ContractAuthority(contractId, false);
    }

    /// Delegatable contract leaf (HAPI DelegatableContractID): authority usable via delegatecall.
    public static Authority ofDelegatable(final ContractId contractId) {
        return new ContractAuthority(contractId, true);
    }

    private AuthorityFactory() {
        throw new UnsupportedOperationException("Factory class cannot be instantiated");
    }
}
