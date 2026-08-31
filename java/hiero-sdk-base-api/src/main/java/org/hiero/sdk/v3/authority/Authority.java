// Source: spec/base/authority.md (authority#Authority).

package org.hiero.sdk.v3.authority;

import java.util.List;
import java.util.Objects;
import org.hiero.sdk.v3.keys.PublicKey;
import org.hiero.sdk.v3.ledger.ContractId;

/** Closed immutable authorization-requirement sum. */
public sealed interface Authority permits PublicKeyAuthority, ContractAuthority, AuthorityList {
    /** Creates an n-of-n composition. */
    static Authority of(Authority... children) {
        Objects.requireNonNull(children, "children");
        return new AuthorityList(List.of(children), children.length);
    }

    /** Creates an m-of-n composition. */
    static Authority of(int threshold, Authority... children) {
        Objects.requireNonNull(children, "children");
        return new AuthorityList(List.of(children), threshold);
    }

    /** Creates a public-key leaf. */
    static Authority of(PublicKey publicKey) {
        return new PublicKeyAuthority(Objects.requireNonNull(publicKey, "publicKey"));
    }

    /** Creates a direct-contract leaf. */
    static Authority ofContract(ContractId contractId) {
        return new ContractAuthority(Objects.requireNonNull(contractId, "contractId"), false);
    }

    /** Creates a delegatable-contract leaf. */
    static Authority ofDelegatable(ContractId contractId) {
        return new ContractAuthority(Objects.requireNonNull(contractId, "contractId"), true);
    }
}
