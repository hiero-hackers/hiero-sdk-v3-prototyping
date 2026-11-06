package org.hiero.enterprise.service.contract;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.util.List;
import org.hiero.enterprise.service.ServiceException;
import org.hiero.evm.AbiFunction;
import org.hiero.evm.AbiUint;
import org.hiero.evm.AbiUintType;
import org.hiero.evm.AbiValue;
import org.hiero.ledger.Address;
import org.hiero.ledger.ContractId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `SmartContractService`, generated from the specs.
@DisplayName("SmartContractService")
class SmartContractServiceTest {

    @Test
    @DisplayName("creates a SmartContractService and returns the values")
    @Tag("testId:enterprise.service.contract.SmartContractService/create")
    void shouldCreateAndReturnTheValues() {
        final SmartContractService subject = new SmartContractService();
    }

    @Test
    @DisplayName("createContract(fileId, constructorArguments) can be called and returns a value (only ServiceException may occur)")
    @Tag("testId:enterprise.service.contract.SmartContractService/createContract/call")
    void shouldCallCreateContract() throws Exception {
        try {
            assertNotNull(create().createContract(fileIdValue(), constructorArgumentsValue()));
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("createContract(contents, constructorArguments) can be called and returns a value (only ServiceException may occur)")
    @Tag("testId:enterprise.service.contract.SmartContractService/createContract/call-2")
    void shouldCallCreateContract2() throws Exception {
        try {
            assertNotNull(create().createContract(contentsValue(), constructorArgumentsValue()));
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("createContract(fileId, params) can be called and returns a value (only ServiceException may occur)")
    @Tag("testId:enterprise.service.contract.SmartContractService/createContract/call-3")
    void shouldCallCreateContract3() throws Exception {
        try {
            assertNotNull(create().createContract(fileIdValue(), paramsValue()));
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("createContract(contents, params) can be called and returns a value (only ServiceException may occur)")
    @Tag("testId:enterprise.service.contract.SmartContractService/createContract/call-4")
    void shouldCallCreateContract4() throws Exception {
        try {
            assertNotNull(create().createContract(contentsValue(), paramsValue()));
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("executeFunction(contractId, function, arguments) can be called and returns a value (only ServiceException may occur)")
    @Tag("testId:enterprise.service.contract.SmartContractService/executeFunction/call")
    void shouldCallExecuteFunction() throws Exception {
        try {
            assertNotNull(create().executeFunction(contractIdValue(), functionValue(), argumentsValue()));
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("callFunction(contractId, function, arguments) can be called and returns a value (only ServiceException may occur)")
    @Tag("testId:enterprise.service.contract.SmartContractService/callFunction/call")
    void shouldCallCallFunction() throws Exception {
        try {
            assertNotNull(create().callFunction(contractIdValue(), functionValue(), argumentsValue()));
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("estimateGas(contractId, function, arguments) can be called (only ServiceException may occur)")
    @Tag("testId:enterprise.service.contract.SmartContractService/estimateGas/call")
    void shouldCallEstimateGas() throws Exception {
        try {
            create().estimateGas(contractIdValue(), functionValue(), argumentsValue());
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("findById(contractId) can be called (only ServiceException may occur)")
    @Tag("testId:enterprise.service.contract.SmartContractService/findById/call")
    void shouldCallFindById() throws Exception {
        try {
            create().findById(contractIdValue());
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("findAll() can be called and returns a value (only ServiceException may occur)")
    @Tag("testId:enterprise.service.contract.SmartContractService/findAll/call")
    void shouldCallFindAll() throws Exception {
        try {
            assertNotNull(create().findAll());
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    private static SmartContractService create() {
        return new SmartContractService();
    }

    private static Address fileIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static AbiValue constructorArgumentsValue() {
        return new AbiUint(new BigInteger("1"), 256);
    }

    private static byte[] contentsValue() {
        return new byte[] {1, 2, 3};
    }

    private static ContractCreateParams paramsValue() {
        return new ContractCreateParams(List.of(), null, null, null, null, null, null, null);
    }

    private static ContractId contractIdValue() {
        return new ContractId(0L, 0L, "", 1001L, null);
    }

    private static AbiFunction functionValue() {
        return new AbiFunction("a", List.of(new AbiUintType(256)), List.of(new AbiUintType(256)));
    }

    private static AbiValue argumentsValue() {
        return new AbiUint(new BigInteger("1"), 256);
    }
}
