package org.hiero.sdk.v3.contract;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import org.hiero.sdk.v3.authority.Authority;
import org.hiero.sdk.v3.common.Page;
import org.hiero.sdk.v3.keys.KeyFactory;
import org.hiero.sdk.v3.ledger.LedgerFactory;
import org.hiero.sdk.v3.ledger.config.NetworkSettingRegistry;
import org.hiero.sdk.v3.nativetoken.NativeTokenOperations;
import org.junit.jupiter.api.Test;

class ApiShapeContractTest {
    private static final Set<String> EXPECTED_EXPORTS = Set.of(
            "org.hiero.sdk.v3.authority",
            "org.hiero.sdk.v3.common",
            "org.hiero.sdk.v3.grpc",
            "org.hiero.sdk.v3.hedera",
            "org.hiero.sdk.v3.keys",
            "org.hiero.sdk.v3.ledger",
            "org.hiero.sdk.v3.ledger.config",
            "org.hiero.sdk.v3.nativetoken",
            "org.hiero.sdk.v3.proto",
            "org.hiero.sdk.v3.solo",
            "org.hiero.sdk.v3.token");

    @Test
    void shouldExportExactlyTheElevenBasePackages() {
        final ModuleDescriptor descriptor = ModuleFinder.of(ContractTestSupport.JAVA_ROOT.resolve(
                        "hiero-sdk-base-api/target/hiero-sdk-base-api-0.1.0-SNAPSHOT.jar"))
                .find("org.hiero.sdk.v3.base")
                .orElseThrow()
                .descriptor();
        assertThat(descriptor.name()).isEqualTo("org.hiero.sdk.v3.base");
        final Set<String> exports = descriptor.exports().stream().map(ModuleDescriptor.Exports::source).collect(Collectors.toSet());
        assertThat(exports).isEqualTo(EXPECTED_EXPORTS);
    }

    @Test
    void shouldExposeAsyncContractsWithoutExecutionInfrastructure() throws NoSuchMethodException {
        assertThat(Page.class.getMethod("next").getReturnType()).isEqualTo(CompletionStage.class);
        assertThat(Page.class.getMethod("first").getReturnType()).isEqualTo(CompletionStage.class);
        assertThat(Arrays.stream(Page.class.getDeclaredFields()).map(Field::getType))
                .noneMatch(type -> type.getName().contains("Executor") || type.getName().contains("Scheduler"));
    }

    @Test
    void shouldKeepOperationalCompanionsAsBodyFreeContracts() {
        for (Class<?> companion : ListHolder.OPERATIONAL_COMPANIONS) {
            assertThat(companion.isInterface()).isTrue();
            assertThat(companion.getDeclaredMethods()).allMatch(method -> Modifier.isAbstract(method.getModifiers()));
        }
    }

    @Test
    void shouldExposeOnlyApprovedTypesInPublicSignatures() throws IOException, ClassNotFoundException {
        for (Class<?> type : ContractTestSupport.apiTypes()) {
            for (Method method : type.getDeclaredMethods()) {
                if (Modifier.isPublic(method.getModifiers()) || Modifier.isProtected(method.getModifiers())) {
                    assertApproved(method.getReturnType());
                    Arrays.stream(method.getParameterTypes()).forEach(ApiShapeContractTest::assertApproved);
                    Arrays.stream(method.getExceptionTypes()).forEach(ApiShapeContractTest::assertApproved);
                }
            }
            for (Constructor<?> constructor : type.getDeclaredConstructors()) {
                if (Modifier.isPublic(constructor.getModifiers()) || Modifier.isProtected(constructor.getModifiers())) {
                    Arrays.stream(constructor.getParameterTypes()).forEach(ApiShapeContractTest::assertApproved);
                }
            }
        }
    }

    private static void assertApproved(final Class<?> type) {
        final Class<?> elementType = type.isArray() ? type.getComponentType() : type;
        final String packageName = elementType.isPrimitive() ? "java.lang" : elementType.getPackageName();
        assertThat(packageName)
                .as("public signature package for %s", type.getTypeName())
                .matches(name -> name.startsWith("java.") || name.startsWith("org.hiero.sdk.v3"));
    }

    private static final class ListHolder {
        private static final java.util.List<Class<?>> OPERATIONAL_COMPANIONS = java.util.List.of(
                KeyFactory.class,
                LedgerFactory.class,
                NetworkSettingRegistry.class,
                NativeTokenOperations.class);

        private ListHolder() {}
    }
}
