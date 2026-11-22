package org.hiero.sdk.v3.nativeToken.contract;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.List;
import org.hiero.sdk.v3.nativeToken.NativeToken;
import org.hiero.sdk.v3.nativeToken.NativeTokenUnit;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NativeTokenApiShapeTest {
    @Test
    void unitIsAnOpenInterfaceWithOnlyReadOnlyAccessors() throws Exception {
        // GIVEN / WHEN / THEN
        assertThat(NativeTokenUnit.class.isInterface()).isTrue();
        assertThat(NativeTokenUnit.class.getModifiers()).matches(Modifier::isPublic);
        assertThat(NativeTokenUnit.class.isSealed()).isFalse();
        assertThat(NativeTokenUnit.class.getDeclaredFields()).isEmpty();
        assertThat(NativeTokenUnit.class.getDeclaredConstructors()).isEmpty();
        assertThat(Arrays.stream(NativeTokenUnit.class.getDeclaredMethods()).map(Method::getName))
                .containsExactlyInAnyOrder("symbol", "baseUnitFactor");
        assertMethod(NativeTokenUnit.class, "symbol", String.class, false);
        assertMethod(NativeTokenUnit.class, "baseUnitFactor", long.class, false);
    }

    @Test
    void tokenOwnsImmutableStateThroughOnlyAProtectedConstructor() throws Exception {
        // GIVEN
        final Constructor<?> constructor = NativeToken.class.getDeclaredConstructor(long.class, NativeTokenUnit.class);
        // WHEN / THEN
        assertThat(NativeToken.class.getModifiers()).matches(Modifier::isPublic).matches(Modifier::isAbstract);
        assertThat(NativeToken.class.getDeclaredConstructors()).hasSize(1);
        assertThat(constructor.getModifiers()).matches(Modifier::isProtected);
        assertThat(NativeToken.class.getConstructors()).isEmpty();
        assertThat(Arrays.stream(NativeToken.class.getDeclaredFields()).map(field -> field.getName()))
                .containsExactlyInAnyOrder("amount", "unit");
        assertThat(Arrays.stream(NativeToken.class.getDeclaredFields())).allSatisfy(field ->
                assertThat(field.getModifiers()).matches(Modifier::isPrivate).matches(Modifier::isFinal));
        assertThat(NativeToken.class.getDeclaredField("amount").getType()).isEqualTo(long.class);
        assertThat(NativeToken.class.getDeclaredField("unit").getGenericType().getTypeName()).isEqualTo("UNIT");
        assertThat(Arrays.stream(NativeToken.class.getDeclaredMethods()).map(Method::getName))
                .containsExactlyInAnyOrder("amount", "unit", "to", "toBaseUnits");
        assertMethod(NativeToken.class, "amount", long.class, true);
        assertMethod(NativeToken.class, "unit", NativeTokenUnit.class, true);
        assertMethod(NativeToken.class, "toBaseUnits", long.class, false);
    }

    @Test
    void recursiveBoundsPreserveSelfAndAssociatedUnit() throws Exception {
        // GIVEN
        final TypeVariable<?>[] parameters = NativeToken.class.getTypeParameters();
        final Method conversion = assertMethod(NativeToken.class, "to", NativeToken.class, false, NativeTokenUnit.class);
        // WHEN / THEN
        assertThat(parameters).extracting(TypeVariable::getName).containsExactly("SELF", "UNIT");
        assertThat(parameters[0].getBounds()).extracting(type -> type.getTypeName())
                .containsExactly("org.hiero.sdk.v3.nativeToken.NativeToken<SELF, UNIT>");
        assertThat(parameters[1].getBounds()).containsExactly(NativeTokenUnit.class);
        assertThat(conversion.getGenericReturnType()).isEqualTo(parameters[0]);
        assertThat(conversion.getGenericParameterTypes()).containsExactly(parameters[1]);
        assertThat(NativeToken.class.getDeclaredMethod("unit").getGenericReturnType()).isEqualTo(parameters[1]);
        assertThat(NativeToken.class.getDeclaredConstructors()[0].getGenericParameterTypes())
                .containsExactly(long.class, parameters[1]);
    }

    @Test
    void referenceInputsAndOutputsHaveExplicitNullability() throws Exception {
        // GIVEN
        final Constructor<?> constructor = NativeToken.class.getDeclaredConstructor(long.class, NativeTokenUnit.class);
        final Method conversion = NativeToken.class.getDeclaredMethod("to", NativeTokenUnit.class);
        // WHEN / THEN
        assertThat(NativeToken.class.getPackage().isAnnotationPresent(NullMarked.class)).isTrue();
        assertNonNull(constructor.getAnnotatedParameterTypes()[1]);
        assertNonNull(conversion.getAnnotatedParameterTypes()[0]);
        assertNonNull(conversion.getAnnotatedReturnType());
        assertNonNull(NativeToken.class.getDeclaredMethod("unit").getAnnotatedReturnType());
        assertNonNull(NativeTokenUnit.class.getDeclaredMethod("symbol").getAnnotatedReturnType());
    }

    @Test
    void signaturesExposeOnlyJdkAndPublicNativeTokenTypes() {
        // GIVEN / WHEN / THEN
        for (final Class<?> type : List.of(NativeToken.class, NativeTokenUnit.class)) {
            assertThat(type.getModule()).isSameAs(NativeToken.class.getModule());
            assertThat(type.getModule().isExported(type.getPackageName())).isTrue();
            assertThat(type.getDeclaredClasses()).isEmpty();
            for (final Method method : type.getDeclaredMethods()) {
                assertPublicType(method.getReturnType());
                Arrays.stream(method.getParameterTypes()).forEach(NativeTokenApiShapeTest::assertPublicType);
                assertThat(method.getExceptionTypes()).isEmpty();
            }
            for (final Constructor<?> constructor : type.getDeclaredConstructors()) {
                Arrays.stream(constructor.getParameterTypes()).forEach(NativeTokenApiShapeTest::assertPublicType);
            }
        }
    }

    private static Method assertMethod(final Class<?> owner, final String name, final Class<?> result,
            final boolean isFinal, final Class<?>... parameters) throws Exception {
        final Method method = owner.getDeclaredMethod(name, parameters);
        assertThat(method.getReturnType()).isEqualTo(result);
        assertThat(method.getModifiers()).matches(Modifier::isPublic);
        assertThat(Modifier.isFinal(method.getModifiers())).isEqualTo(isFinal);
        assertThat(Modifier.isAbstract(method.getModifiers())).isEqualTo(!isFinal);
        return method;
    }

    private static void assertNonNull(final AnnotatedType type) {
        assertThat(type.isAnnotationPresent(NonNull.class)).as(type.toString()).isTrue();
    }

    private static void assertPublicType(final Class<?> type) {
        assertThat(type.isPrimitive() || type == String.class || type == NativeToken.class || type == NativeTokenUnit.class)
                .as(type.getTypeName()).isTrue();
    }
}
