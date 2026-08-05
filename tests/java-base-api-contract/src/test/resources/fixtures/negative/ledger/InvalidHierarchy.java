package org.hiero.sdk.v3.fixture.negative;

import org.hiero.sdk.v3.ledger.Address;
import org.hiero.sdk.v3.ledger.ContractId;

final class InvalidHierarchy {
    Address address = new ContractId(0, 0, 1L, "", null);
}
