package org.hiero.sdk.v3.ledger.contract;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.hiero.sdk.v3.ledger.Address;
import org.hiero.sdk.v3.ledger.BaseAddress;
import org.hiero.sdk.v3.ledger.Network;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AddressApiShapeTest {
    @Test
    void hierarchySupportsFutureSelectorsButNumericAddressIsFinal() throws Exception {
        // GIVEN / WHEN / THEN
        assertThat(BaseAddress.class.getModifiers()).matches(Modifier::isPublic).matches(Modifier::isAbstract);
        assertThat(BaseAddress.class.isSealed()).isFalse();
        assertThat(Address.class.getSuperclass()).isEqualTo(BaseAddress.class);
        assertThat(Address.class.getModifiers()).matches(Modifier::isPublic).matches(Modifier::isFinal);
        final Constructor<?> base = BaseAddress.class.getDeclaredConstructor(long.class, long.class, Long.class,
                String.class);
        assertThat(BaseAddress.class.getDeclaredConstructors()).containsExactly(base);
        assertThat(base.getModifiers()).matches(Modifier::isProtected);
        final Constructor<?> concrete = Address.class.getConstructor(long.class, long.class, long.class, String.class);
        assertThat(Address.class.getDeclaredConstructors()).containsExactly(concrete);
        assertThat(concrete.getModifiers()).matches(Modifier::isPublic);
    }

    @Test
    void baseOwnsAllImmutableInstanceStateWithoutChildShadowing() throws Exception {
        // GIVEN / WHEN / THEN
        assertThat(Arrays.stream(BaseAddress.class.getDeclaredFields()).map(field -> field.getName()))
                .containsExactlyInAnyOrder("shard", "realm", "num", "checksum");
        assertThat(Arrays.stream(BaseAddress.class.getDeclaredFields())).allSatisfy(field ->
                assertThat(field.getModifiers()).matches(Modifier::isPrivate).matches(Modifier::isFinal));
        assertThat(Arrays.stream(Address.class.getDeclaredFields()))
                .allSatisfy(field -> assertThat(field.getModifiers()).matches(Modifier::isStatic).matches(Modifier::isFinal));
        for (final String name : List.of("shard", "realm", "checksum", "toStringWithChecksum")) {
            assertThat(BaseAddress.class.getMethod(name).getModifiers()).matches(Modifier::isFinal);
        }
        assertThat(BaseAddress.class.getMethod("shard").getReturnType()).isEqualTo(long.class);
        assertThat(BaseAddress.class.getMethod("realm").getReturnType()).isEqualTo(long.class);
        assertThat(BaseAddress.class.getMethod("checksum").getReturnType()).isEqualTo(String.class);
    }

    @Test
    void selectorNarrowsNullabilityWithoutChangingBoxedReturnType() throws Exception {
        // GIVEN
        final Method base = BaseAddress.class.getDeclaredMethod("num");
        final Method concrete = Address.class.getDeclaredMethod("num");
        // WHEN / THEN
        assertThat(base.getReturnType()).isEqualTo(Long.class);
        assertThat(concrete.getReturnType()).isEqualTo(Long.class);
        assertThat(base.getAnnotatedReturnType().isAnnotationPresent(Nullable.class)).isTrue();
        assertThat(concrete.getAnnotatedReturnType().isAnnotationPresent(NonNull.class)).isTrue();
        assertThat(BaseAddress.class.getDeclaredConstructors()[0].getAnnotatedParameterTypes()[2]
                .isAnnotationPresent(Nullable.class)).isTrue();
    }

    @Test
    void factorySentinelAndChecksumContractsArePublicWithoutExtraApi() throws Exception {
        // GIVEN
        final Method parser = Address.class.getMethod("fromString", String.class);
        final Method validator = BaseAddress.class.getMethod("validateChecksum", Network.class);
        // WHEN / THEN
        assertThat(parser.getModifiers()).matches(Modifier::isPublic).matches(Modifier::isStatic);
        assertThat(parser.getReturnType()).isEqualTo(Address.class);
        assertThat(validator.getReturnType()).isEqualTo(boolean.class);
        assertThat(validator.getGenericParameterTypes()[0].getTypeName())
                .isEqualTo("org.hiero.sdk.v3.ledger.Network<?>");
        assertThat(validator.getModifiers()).matches(Modifier::isAbstract);
        assertThat(BaseAddress.class.getDeclaredMethod("toString").getModifiers()).matches(Modifier::isAbstract);
        assertThat(Address.class.getField("ZERO_ADDRESS").getModifiers())
                .matches(Modifier::isPublic).matches(Modifier::isStatic).matches(Modifier::isFinal);
        assertThat(Address.class.getField("ZERO_ADDRESS").getType()).isEqualTo(Address.class);
        assertThat(publicMethodNames(BaseAddress.class)).containsExactlyInAnyOrder("shard", "realm", "num", "checksum",
                "validateChecksum", "toString", "toStringWithChecksum");
        assertThat(publicMethodNames(Address.class)).containsExactlyInAnyOrder("num", "fromString", "validateChecksum",
                "toString", "equals", "hashCode");
    }

    @Test
    void nullabilityExportsAndSignaturesUseOnlyPublicContracts() {
        // GIVEN / WHEN / THEN
        assertThat(Address.class.getPackage().isAnnotationPresent(NullMarked.class)).isTrue();
        assertThat(Address.class.getModule()).isSameAs(Network.class.getModule());
        assertThat(Address.class.getModule().isExported("org.hiero.sdk.v3.ledger")).isTrue();
        final Set<Class<?>> allowed = Set.of(long.class, boolean.class, int.class, Long.class, String.class,
                Object.class, Network.class, Address.class);
        for (final Class<?> type : List.of(BaseAddress.class, Address.class)) {
            for (final Constructor<?> constructor : type.getDeclaredConstructors()) {
                for (int index = 0; index < constructor.getParameterCount(); index++) {
                    if (!constructor.getParameterTypes()[index].isPrimitive()) {
                        assertThat(constructor.getAnnotatedParameterTypes()[index].getAnnotations()).isNotEmpty();
                    }
                }
            }
            for (final Method method : type.getDeclaredMethods()) {
                if (!Modifier.isPublic(method.getModifiers())) {
                    continue;
                }
                assertThat(allowed).contains(method.getReturnType());
                assertThat(Arrays.asList(method.getParameterTypes())).allMatch(allowed::contains);
                if (!method.getReturnType().isPrimitive()) {
                    assertThat(method.getAnnotatedReturnType().getAnnotations()).as(method.toString()).isNotEmpty();
                }
                for (final java.lang.reflect.AnnotatedType parameter : method.getAnnotatedParameterTypes()) {
                    assertThat(parameter.getAnnotations()).as(method.toString()).isNotEmpty();
                }
            }
        }
    }

    private static Set<String> publicMethodNames(final Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods()).filter(method -> Modifier.isPublic(method.getModifiers()))
                .map(Method::getName).collect(Collectors.toSet());
    }
}
