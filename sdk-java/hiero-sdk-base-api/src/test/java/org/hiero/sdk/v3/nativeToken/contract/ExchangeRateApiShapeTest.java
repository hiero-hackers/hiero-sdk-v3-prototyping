package org.hiero.sdk.v3.nativeToken.contract;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Modifier;
import java.time.ZonedDateTime;
import java.util.Arrays;
import org.hiero.sdk.v3.nativeToken.ExchangeRate;
import org.junit.jupiter.api.Test;

class ExchangeRateApiShapeTest {
    @Test
    void hasAbstractPublicTypeAndProtectedConstructor() throws ReflectiveOperationException {
        assertThat(ExchangeRate.class.getModifiers()).matches(Modifier::isPublic).matches(Modifier::isAbstract);
        assertThat(ExchangeRate.class
                        .getDeclaredConstructor(ZonedDateTime.class, double.class)
                        .getModifiers())
                .matches(Modifier::isProtected);
    }

    @Test
    void stateIsPrivateFinalWithPrimitiveMappings() {
        assertThat(Arrays.stream(ExchangeRate.class.getDeclaredFields()))
                .hasSize(2)
                .allSatisfy(field -> assertThat(field.getModifiers())
                        .matches(Modifier::isPrivate)
                        .matches(Modifier::isFinal));
    }

    @Test
    void accessorsAndExpirationHaveTheRequiredSignatures() throws ReflectiveOperationException {
        final var expiration = ExchangeRate.class.getMethod("expirationTime");
        assertThat(expiration.getReturnType()).isEqualTo(ZonedDateTime.class);
        assertThat(expiration.getModifiers()).matches(Modifier::isFinal);

        final var rate = ExchangeRate.class.getMethod("exchangeRateInUsdCents");
        assertThat(rate.getReturnType()).isEqualTo(double.class);
        assertThat(rate.getModifiers()).matches(Modifier::isFinal);

        final var expired = ExchangeRate.class.getMethod("isExpired");
        assertThat(expired.getReturnType()).isEqualTo(boolean.class);
        assertThat(expired.getModifiers()).matches(Modifier::isAbstract);
    }

    @Test
    void hasNoSetters() {
        assertThat(Arrays.stream(ExchangeRate.class.getDeclaredMethods()).map(m -> m.getName()))
                .noneMatch(name -> name.startsWith("set") || name.startsWith("with"));
    }
}
