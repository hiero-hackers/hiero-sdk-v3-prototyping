package org.hiero.sdk.v3.ledger.contract;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Modifier;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import org.hiero.sdk.v3.ledger.Network;
import org.hiero.sdk.v3.nativeToken.NativeTokenUnit;
import org.junit.jupiter.api.Test;

class NetworkApiShapeTest {
    @Test
    void isAFinalRecordBoundedByNativeTokenUnit() {
        assertThat(Network.class.isRecord()).isTrue();
        assertThat(Network.class.getModifiers()).matches(Modifier::isPublic).matches(Modifier::isFinal);

        final TypeVariable<?>[] parameters = Network.class.getTypeParameters();
        assertThat(parameters).hasSize(1);
        assertThat(parameters[0].getBounds()).containsExactly(NativeTokenUnit.class);
    }

    @Test
    void accessorsHaveTheRequiredTypes() throws ReflectiveOperationException {
        assertThat(Network.class.getMethod("id").getReturnType()).isEqualTo(byte[].class);
        assertThat(Network.class.getMethod("name").getReturnType()).isEqualTo(String.class);
        assertThat(Network.class.getMethod("nativeTokenUnit").getGenericReturnType().getTypeName())
                .isEqualTo("UNIT");
    }

    @Test
    void stateIsPrivateFinalAndThereAreNoSetters() {
        assertThat(Arrays.stream(Network.class.getDeclaredFields()))
                .hasSize(3)
                .allSatisfy(field -> assertThat(field.getModifiers())
                        .matches(Modifier::isPrivate)
                        .matches(Modifier::isFinal));
        assertThat(Arrays.stream(Network.class.getDeclaredMethods()).map(m -> m.getName()))
                .noneMatch(name -> name.startsWith("set") || name.startsWith("with"));
    }

    @Test
    void canonicalConstructorIsPublic() throws ReflectiveOperationException {
        assertThat(Network.class
                        .getDeclaredConstructor(byte[].class, String.class, NativeTokenUnit.class)
                        .getModifiers())
                .matches(Modifier::isPublic);
    }
}
