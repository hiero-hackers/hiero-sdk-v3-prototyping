// Generated from spec/base/ledger.md (ledger#BaseAddress); DO NOT EDIT.

package org.hiero.sdk.v3.ledger;

import org.jspecify.annotations.Nullable;

/** Base data contract for identifiers in shard/realm space. */
public sealed interface BaseAddress permits Address, EvmCapableAddress {
    /** Returns the non-negative shard. */ long shard();
    /** Returns the non-negative realm. */ long realm();
    /** Returns the checksum, or an empty string. */ String checksum();
    /** Returns the optional numeric selector. */ @Nullable Long num();
}
