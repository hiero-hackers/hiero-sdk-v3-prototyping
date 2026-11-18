package org.hiero.sdk.v3.ledger.contract;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import org.hiero.sdk.v3.ledger.BaseAddress;
import org.hiero.sdk.v3.ledger.EvmAddress;
import org.junit.jupiter.api.Test;

class EvmAddressApiShapeTest {
    @Test
    void isAStandaloneFinalRecord() {
        assertThat(EvmAddress.class.isRecord()).isTrue();
        assertThat(EvmAddress.class.getModifiers()).matches(Modifier::isPublic).matches(Modifier::isFinal);
        assertThat(BaseAddress.class.isAssignableFrom(EvmAddress.class)).isFalse();
        assertThat(EvmAddress.class.getInterfaces()).isEmpty();
    }

    @Test
    void stateIsPrivateFinalAndThereAreNoSetters() {
        assertThat(Arrays.stream(EvmAddress.class.getDeclaredFields()).filter(f -> !Modifier.isStatic(f.getModifiers())))
                .hasSize(1)
                .allSatisfy(f -> assertThat(f.getModifiers()).matches(Modifier::isPrivate).matches(Modifier::isFinal));
        assertThat(Arrays.stream(EvmAddress.class.getDeclaredMethods()).map(m -> m.getName()))
                .noneMatch(name -> name.startsWith("set") || name.startsWith("with"));
    }

    @Test
    void factoriesAndAccessorsHaveTheRequiredSignatures() throws ReflectiveOperationException {
        final var fromBytes = EvmAddress.class.getMethod("fromBytes", byte[].class);
        final var fromString = EvmAddress.class.getMethod("fromString", String.class);
        for (final var factory : new java.lang.reflect.Method[] {fromBytes, fromString}) {
            assertThat(factory.getReturnType()).isEqualTo(EvmAddress.class);
            assertThat(factory.getModifiers()).matches(Modifier::isStatic).matches(Modifier::isPublic);
        }
        assertThat(EvmAddress.class.getMethod("bytes").getReturnType()).isEqualTo(byte[].class);
        assertThat(EvmAddress.class.getMethod("toString").getReturnType()).isEqualTo(String.class);
    }

    @Test
    void publicSignaturesExposeOnlyApiTypes() {
        for (final var method : EvmAddress.class.getDeclaredMethods()) {
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            assertThat(method.getReturnType().getName()).doesNotContain("Keccak");
            assertThat(Arrays.stream(method.getParameterTypes()).map(Class::getName)).noneMatch(n -> n.contains("Keccak"));
        }
    }
}
