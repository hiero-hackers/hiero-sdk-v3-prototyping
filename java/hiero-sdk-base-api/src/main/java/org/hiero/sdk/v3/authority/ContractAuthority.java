// Source: spec/base/authority.md (authority#ContractAuthority).

package org.hiero.sdk.v3.authority;

import java.util.Objects;
import org.hiero.sdk.v3.ledger.ContractId;

/** Contract authorization leaf. */
public record ContractAuthority(ContractId contractId, boolean delegatable) implements Authority {
    /** Validates the contract identifier. */ public ContractAuthority { Objects.requireNonNull(contractId, "contractId"); }
}
