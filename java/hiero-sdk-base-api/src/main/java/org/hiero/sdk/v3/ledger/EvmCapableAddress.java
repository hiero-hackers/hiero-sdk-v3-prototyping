// Generated from spec/base/ledger.md (ledger#EvmCapableAddress); DO NOT EDIT.

package org.hiero.sdk.v3.ledger;

import org.jspecify.annotations.Nullable;

/** Base data contract for numeric or EVM-capable identifiers. */
public sealed interface EvmCapableAddress extends BaseAddress permits ContractId, AccountId {
    /** Returns the optional EVM selector. */ @Nullable EvmAddress evmAddress();
}
