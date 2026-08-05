package org.hiero.sdk.v3.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.util.List;
import javax.tools.ToolProvider;
import org.hiero.sdk.v3.authority.Authority;
import org.hiero.sdk.v3.authority.AuthorityList;
import org.hiero.sdk.v3.authority.ContractAuthority;
import org.hiero.sdk.v3.authority.PublicKeyAuthority;
import org.hiero.sdk.v3.keys.Key;
import org.hiero.sdk.v3.keys.KeyFactory;
import org.hiero.sdk.v3.keys.KeyFormatOperations;
import org.hiero.sdk.v3.keys.PrivateKey;
import org.hiero.sdk.v3.keys.PublicKey;
import org.junit.jupiter.api.Test;

class KeySecurityContractTest {

    @Test
    void authorityIsClosedAndEnforcesImmutableThresholdStructure() {
        assertTrue(Authority.class.isSealed());
        assertEquals(
                List.of(AuthorityList.class, ContractAuthority.class, PublicKeyAuthority.class),
                java.util.Arrays.stream(Authority.class.getPermittedSubclasses()).sorted(java.util.Comparator.comparing(Class::getName)).toList());
        assertThrows(IllegalArgumentException.class, () -> new AuthorityList(List.of(), 1));

        var leaf = new ContractAuthority(new org.hiero.sdk.v3.ledger.ContractId(0, 0, 1L, "", null), false);
        assertThrows(IllegalArgumentException.class, () -> new AuthorityList(List.of(leaf), 0));
        assertThrows(IllegalArgumentException.class, () -> new AuthorityList(List.of(leaf), 2));
        var mutable = new java.util.ArrayList<Authority>();
        mutable.add(leaf);
        var list = new AuthorityList(mutable, 1);
        mutable.clear();
        assertEquals(1, list.children().size());
        assertThrows(UnsupportedOperationException.class, () -> list.children().clear());
        assertEquals(list, new AuthorityList(List.of(leaf), 1));
    }

    @Test
    void keyOperationsRemainAbstractAndProviderOwned() {
        for (var type : List.of(Key.class, PublicKey.class, PrivateKey.class, KeyFactory.class, KeyFormatOperations.class)) {
            assertTrue(type.isInterface());
            for (var method : type.getDeclaredMethods()) {
                assertTrue(Modifier.isAbstract(method.getModifiers()));
                assertFalse(method.isDefault());
            }
        }
    }

    @Test
    void manifestRecordsByteOwnershipDeferralWithoutSecretMaterial() throws Exception {
        var manifest = ContractTestSupport.manifest();
        assertTrue(manifest.contains("DE-003"));
        assertTrue(manifest.contains("provider diagnostic redaction"));
        assertFalse(manifest.matches("(?is).*(password|passwd|api[_-]?key|secret)\\s*[:=].*"));
        assertFalse(manifest.contains(System.getProperty("user.home")));
    }

    @Test
    void fixturesProvePositiveUseAndRejectPrivateOrExternalAuthorityVariants() throws Exception {
        var compiler = ToolProvider.getSystemJavaCompiler();
        var root = ContractTestSupport.repositoryRoot();
        var output = root.resolve("tests/java-base-api-contract/target/key-fixtures");
        Files.createDirectories(output);
        for (var name : new String[] {"KeyContractsConsumer.java", "AuthorityConsumer.java"}) {
            var source = root.resolve("tests/java-base-api-contract/src/test/resources/fixtures/positive/" + name);
            assertEquals(0, compiler.run(null, null, null, "--release", "21", "-proc:none", "-d", output.toString(), source.toString()));
        }
        for (var name : new String[] {"PrivateKeyLeaf.java", "ExternalAuthority.java"}) {
            var source = root.resolve("tests/java-base-api-contract/src/test/resources/fixtures/negative/authority/" + name);
            assertNotEquals(0, compiler.run(null, null, null, "--release", "21", "-proc:none", "-d", output.toString(), source.toString()));
        }
    }

    @Test
    void apiSourcesContainNoCryptoProviderOrOperationalImplementation() throws Exception {
        var root = ContractTestSupport.repositoryRoot().resolve("java/hiero-sdk-base-api/src/main/java");
        try (var paths = Files.walk(root)) {
            for (var path : paths.filter(Files::isRegularFile).toList()) {
                var source = Files.readString(path);
                assertFalse(source.contains("java.security"), path.toString());
                assertFalse(source.contains("javax.crypto"), path.toString());
                assertFalse(source.contains("ServiceLoader"), path.toString());
            }
        }
    }
}
