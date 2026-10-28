package org.hiero.sdk.v3.nativeToken.contract;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import org.hiero.sdk.v3.nativeToken.NativeToken;
import org.hiero.sdk.v3.nativeToken.NativeTokenUnit;
import org.junit.jupiter.api.Test;

class NativeTokenApiShapeTest {
    @Test
    void unitIsAnInterfaceWithOnlyReadAccessors() throws ReflectiveOperationException {
        assertThat(NativeTokenUnit.class.isInterface()).isTrue();
        assertThat(NativeTokenUnit.class.getMethod("symbol").getReturnType()).isEqualTo(String.class);
        assertThat(NativeTokenUnit.class.getMethod("baseUnitFactor").getReturnType()).isEqualTo(long.class);
        assertThat(NativeTokenUnit.class.getDeclaredMethods()).hasSize(2);
    }

    @Test
    void tokenHasRecursiveBoundsAndProtectedConstructor() throws ReflectiveOperationException {
        assertThat(NativeToken.class.getModifiers()).matches(Modifier::isPublic).matches(Modifier::isAbstract);

        final TypeVariable<?>[] parameters = NativeToken.class.getTypeParameters();
        assertThat(parameters).hasSize(2);
        final var selfBound = (ParameterizedType) parameters[0].getBounds()[0];
        assertThat(selfBound.getRawType()).isEqualTo(NativeToken.class);
        assertThat(selfBound.getActualTypeArguments()).containsExactly(parameters[0], parameters[1]);
        assertThat(parameters[1].getBounds()).containsExactly(NativeTokenUnit.class);

        final var constructor = NativeToken.class.getDeclaredConstructor(long.class, NativeTokenUnit.class);
        assertThat(constructor.getModifiers()).matches(Modifier::isProtected);
    }

    @Test
    void tokenStateIsFinalAndPrivate() {
        assertThat(Arrays.stream(NativeToken.class.getDeclaredFields()))
                .allSatisfy(field -> assertThat(field.getModifiers())
                        .matches(Modifier::isPrivate)
                        .matches(Modifier::isFinal));
    }

    @Test
    void tokenMethodsHaveTheRequiredSignatures() throws ReflectiveOperationException {
        final var amount = NativeToken.class.getMethod("amount");
        assertThat(amount.getReturnType()).isEqualTo(long.class);
        assertThat(amount.getModifiers()).matches(Modifier::isFinal);

        final var unit = NativeToken.class.getMethod("unit");
        assertThat(unit.getGenericReturnType().getTypeName()).isEqualTo("UNIT");
        assertThat(unit.getModifiers()).matches(Modifier::isFinal);

        final var to = NativeToken.class.getMethod("to", NativeTokenUnit.class);
        assertThat(to.getGenericReturnType().getTypeName()).isEqualTo("SELF");
        assertThat(to.getModifiers()).matches(Modifier::isAbstract);

        final var toBase = NativeToken.class.getMethod("toBaseUnits");
        assertThat(toBase.getReturnType()).isEqualTo(long.class);
        assertThat(toBase.getModifiers()).matches(Modifier::isAbstract);
    }
}
