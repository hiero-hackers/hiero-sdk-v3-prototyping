package org.hiero.evm;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `AbiFunction`, generated from the specs.
@DisplayName("AbiFunction")
class AbiFunctionTest {

    @Test
    @DisplayName("creates an AbiFunction and returns the values")
    @Tag("testId:evm.AbiFunction/create")
    void shouldCreateAndReturnTheValues() {
        final String name = nameValue();
        final List<AbiType> inputs = inputsValue();
        final List<AbiType> outputs = outputsValue();
        final AbiFunction subject = new AbiFunction(name, inputs, outputs);
        assertValue(name, subject.name());
        assertValue(inputs, subject.inputs());
        assertValue(outputs, subject.outputs());
    }

    @Test
    @DisplayName("creates an AbiFunction with the default values")
    @Tag("testId:evm.AbiFunction/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final AbiFunction subject = new AbiFunction(nameValue());
        assertValue(List.of(), subject.inputs());
        assertValue(List.of(), subject.outputs());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:evm.AbiFunction/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final String name = nameValue();
        final List<AbiType> inputs = inputsValue();
        final List<AbiType> outputs = outputsValue();
        final AbiFunction subject = AbiFunction.builder()
                .name(name)
                .inputs(inputs)
                .outputs(outputs)
                .build();
        assertValue(name, subject.name());
        assertValue(inputs, subject.inputs());
        assertValue(outputs, subject.outputs());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:evm.AbiFunction/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final AbiFunction subject = AbiFunction.builder()
                .name(nameValue())
                .build();
        assertValue(List.of(), subject.inputs());
        assertValue(List.of(), subject.outputs());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:evm.AbiFunction/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> AbiFunction.builder().build());
    }

    @Test
    @DisplayName("rejects null for name")
    @Tag("testId:evm.AbiFunction/name/rejects-null")
    void shouldRejectNullName() {
        assertThrows(NullPointerException.class, () -> new AbiFunction((String) null, inputsValue(), outputsValue()));
    }

    @Test
    @DisplayName("name: rejects a value that does not match the pattern")
    @Tag("testId:evm.AbiFunction/name/rejects-not-matching-the-pattern")
    void shouldRejectNameNotMatchingThePattern() {
        assertThrows(IllegalArgumentException.class, () -> new AbiFunction("", inputsValue(), outputsValue()));
    }

    @Test
    @DisplayName("rejects null for inputs")
    @Tag("testId:evm.AbiFunction/inputs/rejects-null")
    void shouldRejectNullInputs() {
        assertThrows(NullPointerException.class, () -> new AbiFunction(nameValue(), (List<AbiType>) null, outputsValue()));
    }

    @Test
    @DisplayName("the constructor copies inputs; the accessor returns an unmodifiable collection")
    @Tag("testId:evm.AbiFunction/inputs/copies-in")
    void shouldCopyInputsIn() {
        final List<AbiType> value = new ArrayList<>(inputsValue());
        final List<AbiType> expected = List.copyOf(value);
        final AbiFunction subject = new AbiFunction(nameValue(), value, outputsValue());
        value.clear();
        assertValue(expected, subject.inputs());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.inputs()).clear());
    }

    @Test
    @DisplayName("rejects null for outputs")
    @Tag("testId:evm.AbiFunction/outputs/rejects-null")
    void shouldRejectNullOutputs() {
        assertThrows(NullPointerException.class, () -> new AbiFunction(nameValue(), inputsValue(), (List<AbiType>) null));
    }

    @Test
    @DisplayName("the constructor copies outputs; the accessor returns an unmodifiable collection")
    @Tag("testId:evm.AbiFunction/outputs/copies-in")
    void shouldCopyOutputsIn() {
        final List<AbiType> value = new ArrayList<>(outputsValue());
        final List<AbiType> expected = List.copyOf(value);
        final AbiFunction subject = new AbiFunction(nameValue(), inputsValue(), value);
        value.clear();
        assertValue(expected, subject.outputs());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.outputs()).clear());
    }

    @Test
    @DisplayName("signature() can be called and returns a value")
    @Tag("testId:evm.AbiFunction/signature/call")
    void shouldCallSignature() {
        assertNotNull(create().signature());
    }

    @Test
    @DisplayName("selector() can be called and returns a value")
    @Tag("testId:evm.AbiFunction/selector/call")
    void shouldCallSelector() {
        assertNotNull(create().selector());
    }

    @Test
    @DisplayName("encodeCall(arguments) can be called and returns a value (only IllegalArgumentException may occur)")
    @Tag("testId:evm.AbiFunction/encodeCall/call")
    void shouldCallEncodeCall() throws Exception {
        try {
            assertNotNull(create().encodeCall(argumentsValue()));
        } catch (final Exception e) {
            if (!(e instanceof IllegalArgumentException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("decodeCall(data) can be called and returns a value (only IllegalArgumentException may occur)")
    @Tag("testId:evm.AbiFunction/decodeCall/call")
    void shouldCallDecodeCall() throws Exception {
        try {
            assertNotNull(create().decodeCall(dataValue()));
        } catch (final Exception e) {
            if (!(e instanceof IllegalArgumentException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("decodeResult(data) can be called and returns a value (only IllegalArgumentException may occur)")
    @Tag("testId:evm.AbiFunction/decodeResult/call")
    void shouldCallDecodeResult() throws Exception {
        try {
            assertNotNull(create().decodeResult(dataValue()));
        } catch (final Exception e) {
            if (!(e instanceof IllegalArgumentException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("parse(signature) can be called and returns a value (only IllegalArgumentException may occur)")
    @Tag("testId:evm.AbiFunction/parse/call")
    void shouldCallParse() throws Exception {
        try {
            assertNotNull(AbiFunction.parse(signatureValue()));
        } catch (final Exception e) {
            if (!(e instanceof IllegalArgumentException)) {
                throw e;
            }
        }
    }

    private static String nameValue() {
        return "a";
    }

    private static List<AbiType> inputsValue() {
        return List.of(new AbiUintType(256));
    }

    private static List<AbiType> outputsValue() {
        return List.of(new AbiUintType(256));
    }

    private static AbiFunction create() {
        return new AbiFunction(nameValue(), inputsValue(), outputsValue());
    }

    private static List<AbiValue> argumentsValue() {
        return List.of(new AbiUint(new BigInteger("1"), 256));
    }

    private static byte[] dataValue() {
        return new byte[] {1, 2, 3};
    }

    private static String signatureValue() {
        return "value";
    }
}
