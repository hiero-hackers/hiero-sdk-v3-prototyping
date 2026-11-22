package org.hiero.sdk.v3.ledger.contract;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import org.hiero.sdk.v3.ledger.EvmAddress;
import org.hiero.sdk.v3.ledger.Network;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EvmAddressApiShapeTest {
    @Test
    void standaloneRecordOwnsOnlyFinalByteStateWithCheckedCanonicalConstruction() throws Exception {
        // GIVEN
        final Constructor<?> constructor = EvmAddress.class.getDeclaredConstructor(byte[].class);
        // WHEN / THEN
        assertThat(EvmAddress.class.isRecord()).isTrue();
        assertThat(EvmAddress.class.getSuperclass()).isEqualTo(Record.class);
        assertThat(EvmAddress.class.getModifiers()).matches(Modifier::isPublic).matches(Modifier::isFinal);
        assertThat(EvmAddress.class.getDeclaredConstructors()).containsExactly(constructor);
        assertThat(constructor.getModifiers()).matches(Modifier::isPublic);
        assertThat(EvmAddress.class.getRecordComponents()).hasSize(1);
        assertThat(EvmAddress.class.getRecordComponents()[0].getName()).isEqualTo("bytes");
        assertThat(EvmAddress.class.getDeclaredField("bytes").getType()).isEqualTo(byte[].class);
        assertThat(Arrays.stream(EvmAddress.class.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers())))
                .singleElement().satisfies(field -> assertThat(field.getModifiers())
                        .matches(Modifier::isPrivate).matches(Modifier::isFinal));
    }

    @Test
    void factoriesAndPublicSignaturesExposeOnlyTheValueAndJdkTypes() throws Exception {
        // GIVEN
        final Set<Class<?>> allowed = Set.of(byte[].class, String.class, EvmAddress.class, Object.class,
                boolean.class, int.class);
        // WHEN / THEN
        assertThat(Arrays.stream(EvmAddress.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers())).map(Method::getName))
                .containsExactlyInAnyOrder("bytes", "fromBytes", "fromString", "toString", "equals", "hashCode");
        for (final String factory : Set.of("fromBytes", "fromString")) {
            final Class<?> input = factory.equals("fromBytes") ? byte[].class : String.class;
            final Method method = EvmAddress.class.getMethod(factory, input);
            assertThat(method.getReturnType()).isEqualTo(EvmAddress.class);
            assertThat(method.getModifiers()).matches(Modifier::isPublic).matches(Modifier::isStatic);
        }
        for (final Method method : EvmAddress.class.getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers())) {
                assertThat(allowed).contains(method.getReturnType());
                assertThat(Arrays.asList(method.getParameterTypes())).allMatch(allowed::contains);
                assertThat(method.getExceptionTypes()).isEmpty();
            }
        }
        assertThat(EvmAddress.class.getFields()).isEmpty();
        assertThat(EvmAddress.class.getDeclaredClasses()).isEmpty();
    }

    @Test
    void requiredReferencesAreExplicitAndExistingLedgerExportIsSufficient() throws Exception {
        // GIVEN / WHEN / THEN
        assertThat(EvmAddress.class.getModule()).isSameAs(Network.class.getModule());
        assertThat(EvmAddress.class.getModule().isExported("org.hiero.sdk.v3.ledger")).isTrue();
        assertThat(EvmAddress.class.getPackage().isAnnotationPresent(NullMarked.class)).isTrue();
        assertThat(EvmAddress.class.getDeclaredConstructor(byte[].class).getAnnotatedParameterTypes()[0]
                .isAnnotationPresent(NonNull.class)).isTrue();
        for (final Method method : EvmAddress.class.getDeclaredMethods()) {
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            if (!method.getReturnType().isPrimitive()) {
                assertThat(method.getAnnotatedReturnType().isAnnotationPresent(NonNull.class))
                        .as(method.getName()).isTrue();
            }
            for (final java.lang.reflect.AnnotatedType parameter : method.getAnnotatedParameterTypes()) {
                assertThat(parameter.isAnnotationPresent(method.getName().equals("equals")
                        ? Nullable.class : NonNull.class)).as(method.getName()).isTrue();
            }
        }
    }
}
