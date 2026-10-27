package org.hiero.sdk.v3.nativeToken.contract;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.ZonedDateTime;
import java.util.Arrays;
import org.hiero.sdk.v3.nativeToken.ExchangeRate;
import org.hiero.sdk.v3.nativeToken.NativeToken;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExchangeRateApiShapeTest {
    @Test
    void abstractClassOwnsFinalStateAndOnlyProtectedConstruction() throws Exception {
        // GIVEN
        final Constructor<?> constructor = ExchangeRate.class.getDeclaredConstructor(ZonedDateTime.class, double.class);
        // WHEN / THEN
        assertThat(ExchangeRate.class.getModifiers()).matches(Modifier::isPublic).matches(Modifier::isAbstract);
        assertThat(ExchangeRate.class.getDeclaredConstructors()).containsExactly(constructor);
        assertThat(constructor.getModifiers()).matches(Modifier::isProtected);
        assertThat(ExchangeRate.class.getConstructors()).isEmpty();
        assertThat(Arrays.stream(ExchangeRate.class.getDeclaredFields()).map(field -> field.getName()))
                .containsExactlyInAnyOrder("expirationTime", "exchangeRateInUsdCents");
        assertThat(Arrays.stream(ExchangeRate.class.getDeclaredFields())).allSatisfy(field ->
                assertThat(field.getModifiers()).matches(Modifier::isPrivate).matches(Modifier::isFinal));
        assertThat(ExchangeRate.class.getDeclaredField("expirationTime").getType()).isEqualTo(ZonedDateTime.class);
        assertThat(ExchangeRate.class.getDeclaredField("exchangeRateInUsdCents").getType()).isEqualTo(double.class);
    }

    @Test
    void accessorsAreFinalAndExpirationEvaluationIsAbstract() throws Exception {
        // GIVEN / WHEN / THEN
        assertThat(Arrays.stream(ExchangeRate.class.getDeclaredMethods()).map(Method::getName))
                .containsExactlyInAnyOrder("expirationTime", "exchangeRateInUsdCents", "isExpired");
        assertMethod("expirationTime", ZonedDateTime.class, true);
        assertMethod("exchangeRateInUsdCents", double.class, true);
        assertMethod("isExpired", boolean.class, false);
    }

    @Test
    void expirationInputAndOutputHaveExplicitNullability() throws Exception {
        // GIVEN
        final Constructor<?> constructor = ExchangeRate.class.getDeclaredConstructor(ZonedDateTime.class, double.class);
        // WHEN / THEN
        assertThat(ExchangeRate.class.getPackage().isAnnotationPresent(NullMarked.class)).isTrue();
        assertThat(constructor.getAnnotatedParameterTypes()[0].isAnnotationPresent(NonNull.class)).isTrue();
        assertThat(ExchangeRate.class.getDeclaredMethod("expirationTime").getAnnotatedReturnType()
                .isAnnotationPresent(NonNull.class)).isTrue();
    }

    @Test
    void existingPackageExportExposesOnlyContractAndJdkSignatureTypes() {
        // GIVEN / WHEN / THEN
        assertThat(ExchangeRate.class.getModule()).isSameAs(NativeToken.class.getModule());
        assertThat(ExchangeRate.class.getModule().isExported(ExchangeRate.class.getPackageName())).isTrue();
        assertThat(ExchangeRate.class.getDeclaredClasses()).isEmpty();
        for (final Method method : ExchangeRate.class.getDeclaredMethods()) {
            assertThat(method.getReturnType().isPrimitive() || method.getReturnType() == ZonedDateTime.class).isTrue();
            assertThat(method.getParameterTypes()).isEmpty();
            assertThat(method.getExceptionTypes()).isEmpty();
        }
        assertThat(ExchangeRate.class.getDeclaredConstructors()[0].getParameterTypes())
                .containsExactly(ZonedDateTime.class, double.class);
    }

    private static void assertMethod(final String name, final Class<?> result, final boolean isFinal) throws Exception {
        final Method method = ExchangeRate.class.getDeclaredMethod(name);
        assertThat(method.getModifiers()).matches(Modifier::isPublic);
        assertThat(method.getReturnType()).isEqualTo(result);
        assertThat(Modifier.isFinal(method.getModifiers())).isEqualTo(isFinal);
        assertThat(Modifier.isAbstract(method.getModifiers())).isEqualTo(!isFinal);
    }
}
