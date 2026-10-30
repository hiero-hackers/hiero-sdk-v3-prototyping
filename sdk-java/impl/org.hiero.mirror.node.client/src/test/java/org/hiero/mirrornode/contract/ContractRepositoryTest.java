package org.hiero.mirrornode.contract;

import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.ContractId;
import org.hiero.ledger.EvmAddress;
import org.hiero.mirrornode.common.Order;
import org.jspecify.annotations.Nullable;
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
    @DisplayName("findAll(contractId, limit, order) can be called and returns a value")
    @Tag("testId:mirrornode.contract.ContractRepository/findAll/call-2")
    void shouldCallFindAll2() {
        assertNotNull(create().findAll(contractIdValue(), limitValue(), orderValue()));
    }

    @Test
    @DisplayName("findById(contractId) can be called and returns a value")
    @Tag("testId:mirrornode.contract.ContractRepository/findById/call")
    void shouldCallFindById() {
        assertNotNull(create().findById(contractIdValue2()));
    }

    @Test
    @DisplayName("findById(contractId, timestamp) can be called and returns a value")
    @Tag("testId:mirrornode.contract.ContractRepository/findById/call-2")
    void shouldCallFindById2() {
        assertNotNull(create().findById(contractIdValue2(), timestampValue()));
    }

    @Test
    @DisplayName("findResults(contractId) can be called and returns a value")
    @Tag("testId:mirrornode.contract.ContractRepository/findResults/call")
    void shouldCallFindResults() {
        assertNotNull(create().findResults(contractIdValue2()));
    }

    @Test
    @DisplayName("findResults(contractId, blockHash, blockNumber, from, internal, limit, order, timestamp, transactionIndex) can be called and returns a value")
    @Tag("testId:mirrornode.contract.ContractRepository/findResults/call-2")
    void shouldCallFindResults2() {
        assertNotNull(create().findResults(contractIdValue2(), blockHashValue(), blockNumberValue(), fromValue(), internalValue(), limitValue(), orderValue(), timestampValue(), transactionIndexValue()));
    }

    @Test
    @DisplayName("findState(contractId) can be called and returns a value")
    @Tag("testId:mirrornode.contract.ContractRepository/findState/call")
    void shouldCallFindState() {
        assertNotNull(create().findState(contractIdValue2()));
    }

    @Test
    @DisplayName("findState(contractId, limit, order, slot, timestamp) can be called and returns a value")
    @Tag("testId:mirrornode.contract.ContractRepository/findState/call-2")
    void shouldCallFindState2() {
        assertNotNull(create().findState(contractIdValue2(), limitValue(), orderValue(), slotValue(), timestampValue()));
    }

    @Test
    @DisplayName("findResult(contractId, timestamp) can be called and returns a value")
    @Tag("testId:mirrornode.contract.ContractRepository/findResult/call")
    void shouldCallFindResult() {
        assertNotNull(create().findResult(contractIdValue2(), timestampValue2()));
    }

    @Test
    @DisplayName("findResult(contractId, timestamp, hbar) can be called and returns a value")
    @Tag("testId:mirrornode.contract.ContractRepository/findResult/call-2")
    void shouldCallFindResult2() {
        assertNotNull(create().findResult(contractIdValue2(), timestampValue2(), hbarValue()));
    }

    @Test
    @DisplayName("findAllResults() can be called and returns a value")
    @Tag("testId:mirrornode.contract.ContractRepository/findAllResults/call")
    void shouldCallFindAllResults() {
        assertNotNull(create().findAllResults());
    }

    @Test
    @DisplayName("findAllResults(from, blockHash, blockNumber, hbar, internal, limit, order, timestamp, transactionIndex) can be called and returns a value")
    @Tag("testId:mirrornode.contract.ContractRepository/findAllResults/call-2")
    void shouldCallFindAllResults2() {
        assertNotNull(create().findAllResults(fromValue(), blockHashValue(), blockNumberValue(), hbarValue(), internalValue(), limitValue(), orderValue(), timestampValue(), transactionIndexValue()));
    }

    @Test
    @DisplayName("findResultByTransaction(transactionIdOrHash) can be called and returns a value")
    @Tag("testId:mirrornode.contract.ContractRepository/findResultByTransaction/call")
    void shouldCallFindResultByTransaction() {
        assertNotNull(create().findResultByTransaction(transactionIdOrHashValue()));
    }

    @Test
    @DisplayName("findResultByTransaction(transactionIdOrHash, hbar, nonce) can be called and returns a value")
    @Tag("testId:mirrornode.contract.ContractRepository/findResultByTransaction/call-2")
    void shouldCallFindResultByTransaction2() {
        assertNotNull(create().findResultByTransaction(transactionIdOrHashValue(), hbarValue(), nonceValue()));
    }

    @Test
    @DisplayName("findActions(transactionIdOrHash) can be called and returns a value")
    @Tag("testId:mirrornode.contract.ContractRepository/findActions/call")
    void shouldCallFindActions() {
        assertNotNull(create().findActions(transactionIdOrHashValue()));
    }

    @Test
    @DisplayName("findActions(transactionIdOrHash, index, limit, order) can be called and returns a value")
    @Tag("testId:mirrornode.contract.ContractRepository/findActions/call-2")
    void shouldCallFindActions2() {
        assertNotNull(create().findActions(transactionIdOrHashValue(), indexValue(), limitValue(), orderValue()));
    }

    @Test
    @DisplayName("findOpcodes(transactionIdOrHash) can be called and returns a value")
    @Tag("testId:mirrornode.contract.ContractRepository/findOpcodes/call")
    void shouldCallFindOpcodes() {
        assertNotNull(create().findOpcodes(transactionIdOrHashValue()));
    }

    @Test
    @DisplayName("findOpcodes(transactionIdOrHash, stack, memory, storage) can be called and returns a value")
    @Tag("testId:mirrornode.contract.ContractRepository/findOpcodes/call-2")
    void shouldCallFindOpcodes2() {
        assertNotNull(create().findOpcodes(transactionIdOrHashValue(), stackValue(), memoryValue(), storageValue()));
    }

    @Test
    @DisplayName("findLogs(contractId) can be called and returns a value")
    @Tag("testId:mirrornode.contract.ContractRepository/findLogs/call")
    void shouldCallFindLogs() {
        assertNotNull(create().findLogs(contractIdValue2()));
    }

    @Test
    @DisplayName("findLogs(contractId, index, limit, order, timestamp, topic0, topic1, topic2, topic3) can be called and returns a value")
    @Tag("testId:mirrornode.contract.ContractRepository/findLogs/call-2")
    void shouldCallFindLogs2() {
        assertNotNull(create().findLogs(contractIdValue2(), indexValue(), limitValue(), orderValue(), timestampValue(), topic0Value(), topic1Value(), topic2Value(), topic3Value()));
    }

    @Test
    @DisplayName("findAllLogs() can be called and returns a value")
    @Tag("testId:mirrornode.contract.ContractRepository/findAllLogs/call")
    void shouldCallFindAllLogs() {
        assertNotNull(create().findAllLogs());
    }

    @Test
    @DisplayName("findAllLogs(index, limit, order, timestamp, topic0, topic1, topic2, topic3, transactionHash) can be called and returns a value")
    @Tag("testId:mirrornode.contract.ContractRepository/findAllLogs/call-2")
    void shouldCallFindAllLogs2() {
        assertNotNull(create().findAllLogs(indexValue(), limitValue(), orderValue(), timestampValue(), topic0Value(), topic1Value(), topic2Value(), topic3Value(), transactionHashValue()));
    }

    @Test
    @DisplayName("call(request) can be called and returns a value")
    @Tag("testId:mirrornode.contract.ContractRepository/call/call")
    void shouldCallCall() {
        assertNotNull(create().call(requestValue()));
    }

    private static ContractRepository create() {
        return new ContractRepository();
    }

    private static List<String> contractIdValue() {
        return List.of("value");
    }

    private static int limitValue() {
        return 1;
    }

    private static Order orderValue() {
        return Order.ASC;
    }

    private static ContractId contractIdValue2() {
        return new ContractId(0L, 0L, "", 1001L, null);
    }

    private static List<String> timestampValue() {
        return List.of("value");
    }

    private static List<String> blockHashValue() {
        return List.of("value");
    }

    private static List<String> blockNumberValue() {
        return List.of("value");
    }

    private static AccountId fromValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static boolean internalValue() {
        return true;
    }

    private static int transactionIndexValue() {
        return 1;
    }

    private static List<String> slotValue() {
        return List.of("value");
    }

    private static ZonedDateTime timestampValue2() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static boolean hbarValue() {
        return true;
    }

    private static String transactionIdOrHashValue() {
        return "value";
    }

    private static int nonceValue() {
        return 1;
    }

    private static List<String> indexValue() {
        return List.of("value");
    }

    private static boolean stackValue() {
        return true;
    }

    private static boolean memoryValue() {
        return true;
    }

    private static boolean storageValue() {
        return true;
    }

    private static List<String> topic0Value() {
        return List.of("value");
    }

    private static List<String> topic1Value() {
        return List.of("value");
    }

    private static List<String> topic2Value() {
        return List.of("value");
    }

    private static List<String> topic3Value() {
        return List.of("value");
    }

    private static List<String> transactionHashValue() {
        return List.of("value");
    }

    private static ContractCallRequest requestValue() {
        return new ContractCallRequest(null, null, null, null, null, null, new EvmAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20}), null);
    }
}
