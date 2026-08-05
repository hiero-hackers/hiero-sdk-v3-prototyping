package org.hiero.sdk.v3.contract;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import javax.tools.ToolProvider;
import org.hiero.sdk.v3.hedera.HbarUnit;
import org.hiero.sdk.v3.ledger.AccountId;
import org.hiero.sdk.v3.ledger.Address;
import org.hiero.sdk.v3.ledger.ConsensusNode;
import org.hiero.sdk.v3.ledger.ContractId;
import org.hiero.sdk.v3.ledger.EvmAddress;
import org.hiero.sdk.v3.ledger.IpAddress;
import org.hiero.sdk.v3.ledger.LedgerConstants;
import org.hiero.sdk.v3.ledger.Network;
import org.junit.jupiter.api.Test;

class LedgerValueContractTest {

    @Test
    void ownsByteValuesAndUsesContentEquality() {
        var source = new byte[20];
        source[0] = 7;
        var address = new EvmAddress(source);
        source[0] = 9;
        assertEquals(7, address.bytes()[0]);
        var returned = address.bytes();
        returned[0] = 3;
        assertEquals(7, address.bytes()[0]);
        assertEquals(address, new EvmAddress(address.bytes()));

        var networkBytes = new byte[] {1, 2};
        var network = new Network<>(networkBytes, null, HbarUnit.TINYBAR);
        networkBytes[0] = 4;
        assertArrayEquals(new byte[] {1, 2}, network.id());
    }

    @Test
    void enforcesLengthsSelectorsRangesAndSentinels() {
        assertThrows(IllegalArgumentException.class, () -> new EvmAddress(new byte[19]));
        assertThrows(IllegalArgumentException.class, () -> new IpAddress(new byte[16]));
        var evm = new EvmAddress(new byte[20]);
        assertThrows(IllegalArgumentException.class, () -> new ContractId(0, 0, null, "", null));
        assertThrows(IllegalArgumentException.class, () -> new ContractId(0, 0, 1L, "", evm));
        assertThrows(IllegalArgumentException.class, () -> new AccountId(0, 0, null, "", null, null));
        assertThrows(IllegalArgumentException.class, () -> new ConsensusNode(
                new IpAddress(new byte[4]), 65_536, new AccountId(0, 0, 1L, "", null, null)));

        assertEquals(Long.valueOf(0), LedgerConstants.ZERO_ADDRESS.num());
        assertEquals(Long.valueOf(0), LedgerConstants.ZERO_ACCOUNT_ID.num());
        assertEquals(Long.valueOf(0), LedgerConstants.ZERO_CONTRACT_ID.num());
        assertNotEquals(LedgerConstants.ZERO_ACCOUNT_ID, new AccountId(0, 0, null, "", evm, null));
    }

    @Test
    void preservesAllAuthoritativeHbarMetadata() {
        assertEquals("tℏ", HbarUnit.TINYBAR.symbol());
        assertEquals(1L, HbarUnit.TINYBAR.baseUnitFactor());
        assertEquals("μℏ", HbarUnit.MICROBAR.symbol());
        assertEquals(100L, HbarUnit.MICROBAR.baseUnitFactor());
        assertEquals(100_000L, HbarUnit.MILLIBAR.baseUnitFactor());
        assertEquals(100_000_000L, HbarUnit.HBAR.baseUnitFactor());
        assertEquals(100_000_000_000L, HbarUnit.KILOBAR.baseUnitFactor());
        assertEquals(100_000_000_000_000L, HbarUnit.MEGABAR.baseUnitFactor());
        assertEquals(100_000_000_000_000_000L, HbarUnit.GIGABAR.baseUnitFactor());
    }

    @Test
    void positiveFixtureCompilesAndNegativeFixturesDoNot() throws Exception {
        var compiler = ToolProvider.getSystemJavaCompiler();
        var root = ContractTestSupport.repositoryRoot();
        var output = root.resolve("tests/java-base-api-contract/target/ledger-fixtures");
        Files.createDirectories(output);
        var positive = root.resolve("tests/java-base-api-contract/src/test/resources/fixtures/positive/LedgerAndTokenConsumer.java");
        assertEquals(0, compiler.run(null, null, null, "--release", "21", "-proc:none", "-d", output.toString(), positive.toString()));
        for (var name : new String[] {"InvalidHierarchy.java", "InvalidGenericBound.java"}) {
            var negative = root.resolve("tests/java-base-api-contract/src/test/resources/fixtures/negative/ledger/" + name);
            assertNotEquals(0, compiler.run(null, null, null, "--release", "21", "-proc:none", "-d", output.toString(), negative.toString()));
        }
    }
}
