// Source: spec/base/token.md (token#TokenSupplyType).

package org.hiero.sdk.v3.token;

/** Token supply policy. */
public enum TokenSupplyType {
    /** No protocol-enforced maximum supply. */
    INFINITE,
    /** Supply is constrained by a declared maximum. */
    FINITE
}
