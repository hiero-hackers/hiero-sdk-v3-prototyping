package org.hiero.sdk.v3.ledger.contract;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import org.hiero.sdk.v3.ledger.Address;
import org.hiero.sdk.v3.ledger.BaseAddress;
import org.hiero.sdk.v3.ledger.Network;
import org.junit.jupiter.api.Test;

class AddressApiShapeTest {
    @Test
    void baseIsAbstractAndAddressIsFinalSubtype() throws ReflectiveOperationException {
        assertThat(BaseAddress.class.getModifiers()).matches(Modifier::isPublic).matches(Modifier::isAbstract);
        assertThat(BaseAddress.class.isSealed()).isFalse();
        assertThat(Address.class.getModifiers()).matches(Modifier::isPublic).matches(Modifier::isFinal);
        assertThat(Address.class.getSuperclass()).isEqualTo(BaseAddress.class);
        assertThat(BaseAddress.class
                        .getDeclaredConstructor(long.class, long.class, Long.class, String.class)
                        .getModifiers())
                .matches(Modifier::isProtected);
    }

    @Test
    void stateIsPrivateFinal() {
        assertThat(Arrays.stream(BaseAddress.class.getDeclaredFields()))
                .allSatisfy(f -> assertThat(f.getModifiers()).matches(Modifier::isPrivate).matches(Modifier::isFinal));
        assertThat(Arrays.stream(Address.class.getDeclaredFields()).filter(f -> !Modifier.isStatic(f.getModifiers())))
                .allSatisfy(f -> assertThat(f.getModifiers()).matches(Modifier::isPrivate).matches(Modifier::isFinal));
    }

    @Test
    void numIsBoxedOnBothAndOverriddenOnAddress() throws ReflectiveOperationException {
        assertThat(BaseAddress.class.getMethod("num").getReturnType()).isEqualTo(Long.class);
        assertThat(Address.class.getDeclaredMethod("num").getReturnType()).isEqualTo(Long.class);
        assertThat(BaseAddress.class.getMethod("shard").getReturnType()).isEqualTo(long.class);
        assertThat(BaseAddress.class.getMethod("realm").getReturnType()).isEqualTo(long.class);
        assertThat(BaseAddress.class.getMethod("checksum").getReturnType()).isEqualTo(String.class);
    }

    @Test
    void operationsHaveTheRequiredSignatures() throws ReflectiveOperationException {
        final var validate = BaseAddress.class.getMethod("validateChecksum", Network.class);
        assertThat(validate.getReturnType()).isEqualTo(boolean.class);
        assertThat(validate.getModifiers()).matches(Modifier::isAbstract);
        assertThat(BaseAddress.class.getMethod("toStringWithChecksum").getReturnType()).isEqualTo(String.class);

        final var parse = Address.class.getMethod("fromString", String.class);
        assertThat(parse.getReturnType()).isEqualTo(Address.class);
        assertThat(parse.getModifiers()).matches(Modifier::isStatic).matches(Modifier::isPublic);
    }

    @Test
    void zeroAddressIsAPublicStaticFinalConstant() throws ReflectiveOperationException {
        final var field = Address.class.getField("ZERO_ADDRESS");
        assertThat(field.getType()).isEqualTo(Address.class);
        assertThat(field.getModifiers()).matches(Modifier::isPublic).matches(Modifier::isStatic).matches(Modifier::isFinal);
    }
}
