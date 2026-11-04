package org.hiero.evm;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `AbiEvent`, generated from the specs.
@DisplayName("AbiEvent")
class AbiEventTest {

    @Test
    @DisplayName("creates an AbiEvent and returns the values")
    @Tag("testId:evm.AbiEvent/create")
    void shouldCreateAndReturnTheValues() {
        final String name = nameValue();
        final List<AbiEventParameter> parameters = parametersValue();
        final boolean anonymous = anonymousValue();
        final AbiEvent subject = new AbiEvent(name, parameters, anonymous);
        assertValue(name, subject.name());
        assertValue(parameters, subject.parameters());
        assertValue(anonymous, subject.anonymous());
    }

    @Test
    @DisplayName("creates an AbiEvent with the default values")
    @Tag("testId:evm.AbiEvent/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final AbiEvent subject = new AbiEvent(nameValue());
        assertValue(List.of(), subject.parameters());
        assertValue(false, subject.anonymous());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:evm.AbiEvent/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final String name = nameValue();
        final List<AbiEventParameter> parameters = parametersValue();
        final boolean anonymous = anonymousValue();
        final AbiEvent subject = AbiEvent.builder()
                .name(name)
                .parameters(parameters)
                .anonymous(anonymous)
                .build();
        assertValue(name, subject.name());
        assertValue(parameters, subject.parameters());
        assertValue(anonymous, subject.anonymous());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:evm.AbiEvent/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final AbiEvent subject = AbiEvent.builder()
                .name(nameValue())
                .build();
        assertValue(List.of(), subject.parameters());
        assertValue(false, subject.anonymous());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:evm.AbiEvent/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> AbiEvent.builder().build());
    }

    @Test
    @DisplayName("rejects null for name")
    @Tag("testId:evm.AbiEvent/name/rejects-null")
    void shouldRejectNullName() {
        assertThrows(NullPointerException.class, () -> new AbiEvent((String) null, parametersValue(), anonymousValue()));
    }

    @Test
    @DisplayName("name: rejects a value that does not match the pattern")
    @Tag("testId:evm.AbiEvent/name/rejects-not-matching-the-pattern")
    void shouldRejectNameNotMatchingThePattern() {
        assertThrows(IllegalArgumentException.class, () -> new AbiEvent("", parametersValue(), anonymousValue()));
    }

    @Test
    @DisplayName("rejects null for parameters")
    @Tag("testId:evm.AbiEvent/parameters/rejects-null")
    void shouldRejectNullParameters() {
        assertThrows(NullPointerException.class, () -> new AbiEvent(nameValue(), (List<AbiEventParameter>) null, anonymousValue()));
    }

    @Test
    @DisplayName("the constructor copies parameters; the accessor returns an unmodifiable collection")
    @Tag("testId:evm.AbiEvent/parameters/copies-in")
    void shouldCopyParametersIn() {
        final List<AbiEventParameter> value = new ArrayList<>(parametersValue());
        final List<AbiEventParameter> expected = List.copyOf(value);
        final AbiEvent subject = new AbiEvent(nameValue(), value, anonymousValue());
        value.clear();
        assertValue(expected, subject.parameters());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.parameters()).clear());
    }

    @Test
    @DisplayName("signature() can be called and returns a value")
    @Tag("testId:evm.AbiEvent/signature/call")
    void shouldCallSignature() {
        assertNotNull(create().signature());
    }

    @Test
    @DisplayName("topic() can be called and returns a value")
    @Tag("testId:evm.AbiEvent/topic/call")
    void shouldCallTopic() {
        assertNotNull(create().topic());
    }

    @Test
    @DisplayName("decodeLog(topics, data) can be called and returns a value (only IllegalArgumentException may occur)")
    @Tag("testId:evm.AbiEvent/decodeLog/call")
    void shouldCallDecodeLog() throws Exception {
        try {
            assertNotNull(create().decodeLog(topicsValue(), dataValue()));
        } catch (final Exception e) {
            if (!(e instanceof IllegalArgumentException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("parse(signature) can be called and returns a value (only IllegalArgumentException may occur)")
    @Tag("testId:evm.AbiEvent/parse/call")
    void shouldCallParse() throws Exception {
        try {
            assertNotNull(AbiEvent.parse(signatureValue()));
        } catch (final Exception e) {
            if (!(e instanceof IllegalArgumentException)) {
                throw e;
            }
        }
    }

    private static String nameValue() {
        return "a";
    }

    private static List<AbiEventParameter> parametersValue() {
        return List.of(new AbiEventParameter(new AbiUintType(256), true));
    }

    private static boolean anonymousValue() {
        return true;
    }

    private static AbiEvent create() {
        return new AbiEvent(nameValue(), parametersValue(), anonymousValue());
    }

    private static List<byte[]> topicsValue() {
        return List.of(new byte[] {1, 2, 3});
    }

    private static byte[] dataValue() {
        return new byte[] {1, 2, 3};
    }

    private static String signatureValue() {
        return "value";
    }
}
