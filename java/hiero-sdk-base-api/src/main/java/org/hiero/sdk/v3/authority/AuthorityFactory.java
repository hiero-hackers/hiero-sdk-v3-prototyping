// Generated from spec/base/authority.md (authority#AuthorityFactory); DO NOT EDIT.

package org.hiero.sdk.v3.authority;

import org.hiero.sdk.v3.keys.PublicKey;
import org.hiero.sdk.v3.ledger.ContractId;

/** Body-free blessed Authority construction contract. */
public interface AuthorityFactory {
    Authority of(Authority... children);
    Authority of(int threshold, Authority... children);
    Authority of(PublicKey publicKey);
    Authority ofContract(ContractId contractId);
    Authority ofDelegatable(ContractId contractId);
}
