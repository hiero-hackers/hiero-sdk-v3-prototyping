package org.hiero.token;


/// Supply policy of a token: INFINITE → no protocol-enforced ceiling; FINITE → `totalSupply ≤
/// maxSupply` is enforced at every mint. Set when the token is created; cannot be changed by a
/// token update.
public enum TokenSupplyType {

    INFINITE,
    FINITE;
}
