package org.hiero.mirrornode.contract;

import static org.junit.jupiter.api.Assertions.*;

import org.hiero.ledger.ContractId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `ContractRepository`, generated from the specs.
@DisplayName("ContractRepository")
class ContractRepositoryTest {

    @Test
    @DisplayName("creates a ContractRepository and returns the values")
    @Tag("testId:mirrornode.contract.ContractRepository/create")
    void shouldCreateAndReturnTheValues() {
        final ContractRepository subject = new ContractRepository();
    }

    @Test
    @DisplayName("findAll() can be called and returns a value")
    @Tag("testId:mirrornode.contract.ContractRepository/findAll/call")
    void shouldCallFindAll() {
        assertNotNull(create().findAll());
    }

    @Test
    @DisplayName("findById(contractId) can be called and returns a value")
    @Tag("testId:mirrornode.contract.ContractRepository/findById/call")
    void shouldCallFindById() {
        assertNotNull(create().findById(contractIdValue()));
    }

    private static ContractRepository create() {
        return new ContractRepository();
    }

    private static ContractId contractIdValue() {
        return new ContractId(0L, 0L, "", 1001L, null);
    }
}
