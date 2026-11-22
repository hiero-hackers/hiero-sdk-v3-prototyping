package org.hiero.sdk.v3.ledger.contract;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.List;
import org.hiero.sdk.v3.ledger.Network;
import org.hiero.sdk.v3.nativeToken.NativeTokenUnit;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NetworkApiShapeTest {
    @Test
    void publicRecordHasOnlyCanonicalConstructionAndImmutableState() throws Exception {
        // GIVEN
        final Constructor<?> constructor = Network.class.getDeclaredConstructor(byte[].class, String.class,
                NativeTokenUnit.class);
        // WHEN / THEN
        assertThat(Network.class.isRecord()).isTrue();
        assertThat(Network.class.getModifiers()).matches(Modifier::isPublic).matches(Modifier::isFinal);
        assertThat(Network.class.getDeclaredConstructors()).containsExactly(constructor);
        assertThat(constructor.getModifiers()).matches(Modifier::isPublic);
        assertThat(Arrays.stream(Network.class.getDeclaredFields()).map(field -> field.getName()))
                .containsExactlyInAnyOrder("id", "name", "nativeTokenUnit");
        assertThat(Arrays.stream(Network.class.getDeclaredFields())).allSatisfy(field ->
                assertThat(field.getModifiers()).matches(Modifier::isPrivate).matches(Modifier::isFinal));
        assertThat(Network.class.getDeclaredField("id").getType()).isEqualTo(byte[].class);
        assertThat(Network.class.getDeclaredField("name").getType()).isEqualTo(String.class);
    }

    @Test
    void genericBoundAndConstructionPreserveConcreteUnitType() throws Exception {
        // GIVEN
        final TypeVariable<?>[] parameters = Network.class.getTypeParameters();
        final Constructor<?> constructor = Network.class.getDeclaredConstructors()[0];
        // WHEN / THEN
        assertThat(parameters).extracting(TypeVariable::getName).containsExactly("UNIT");
        assertThat(parameters[0].getBounds()).containsExactly(NativeTokenUnit.class);
        assertThat(Network.class.getDeclaredField("nativeTokenUnit").getGenericType()).isEqualTo(parameters[0]);
        assertThat(Network.class.getDeclaredMethod("nativeTokenUnit").getGenericReturnType()).isEqualTo(parameters[0]);
        assertThat(constructor.getGenericParameterTypes()).containsExactly(byte[].class, String.class, parameters[0]);
    }

    @Test
    void signaturesAreReadOnlyAndContainOnlyPublicApiAndJdkTypes() throws Exception {
        // GIVEN / WHEN / THEN
        assertThat(Arrays.stream(Network.class.getDeclaredMethods()).map(Method::getName))
                .containsExactlyInAnyOrder("id", "name", "nativeTokenUnit", "equals", "hashCode", "toString");
        assertThat(Network.class.getDeclaredMethod("id").getReturnType()).isEqualTo(byte[].class);
        assertThat(Network.class.getDeclaredMethod("name").getReturnType()).isEqualTo(String.class);
        for (final Method method : Network.class.getDeclaredMethods()) {
            assertThat(method.getModifiers()).matches(Modifier::isPublic);
            assertPublicType(method.getReturnType());
            Arrays.stream(method.getParameterTypes()).forEach(NetworkApiShapeTest::assertPublicType);
            assertThat(method.getExceptionTypes()).isEmpty();
        }
        for (final String accessor : List.of("id", "name", "nativeTokenUnit")) {
            assertThat(Network.class.getDeclaredMethod(accessor).getParameterTypes()).isEmpty();
        }
        assertThat(Network.class.getDeclaredClasses()).isEmpty();
    }

    @Test
    void constructorAndAccessorsExpressRequiredAndNullableReferences() throws Exception {
        // GIVEN
        final Constructor<?> constructor = Network.class.getDeclaredConstructors()[0];
        // WHEN / THEN
        assertThat(Network.class.getPackage().isAnnotationPresent(NullMarked.class)).isTrue();
        assertThat(constructor.getAnnotatedParameterTypes()[0].isAnnotationPresent(NonNull.class)).isTrue();
        assertThat(constructor.getAnnotatedParameterTypes()[1].isAnnotationPresent(Nullable.class)).isTrue();
        assertThat(constructor.getAnnotatedParameterTypes()[2].isAnnotationPresent(NonNull.class)).isTrue();
        assertThat(Network.class.getDeclaredMethod("id").getAnnotatedReturnType()
                .isAnnotationPresent(NonNull.class)).isTrue();
        assertThat(Network.class.getDeclaredMethod("name").getAnnotatedReturnType()
                .isAnnotationPresent(Nullable.class)).isTrue();
        assertThat(Network.class.getDeclaredMethod("nativeTokenUnit").getAnnotatedReturnType()
                .isAnnotationPresent(NonNull.class)).isTrue();
        assertThat(Network.class.getDeclaredMethod("equals", Object.class).getAnnotatedParameterTypes()[0]
                .isAnnotationPresent(Nullable.class)).isTrue();
        assertThat(Network.class.getDeclaredMethod("toString").getAnnotatedReturnType()
                .isAnnotationPresent(NonNull.class)).isTrue();
    }

    @Test
    void ledgerPackageIsExportedByExistingApiModule() {
        // GIVEN / WHEN / THEN
        assertThat(Network.class.getModule()).isSameAs(NativeTokenUnit.class.getModule());
        assertThat(Network.class.getModule().isExported("org.hiero.sdk.v3.ledger")).isTrue();
    }

    private static void assertPublicType(final Class<?> type) {
        assertThat(type.isPrimitive() || type == byte[].class || type == String.class || type == Object.class
                || type == NativeTokenUnit.class).as(type.getTypeName()).isTrue();
    }
}
