package org.hiero.consensusnode.queries.fees;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `FeeComponents`, generated from the specs.
@DisplayName("FeeComponents")
class FeeComponentsTest {

    @Test
    @DisplayName("creates a FeeComponents and returns the values")
    @Tag("testId:consensusnode.queries.fees.FeeComponents/create")
    void shouldCreateAndReturnTheValues() {
        final long minimumFee = minimumFeeValue();
        final long maximumFee = maximumFeeValue();
        final long constantFee = constantFeeValue();
        final long transactionBandwidthByte = transactionBandwidthByteValue();
        final long transactionVerification = transactionVerificationValue();
        final long transactionRamByteHour = transactionRamByteHourValue();
        final long transactionStorageByteHour = transactionStorageByteHourValue();
        final long contractTransactionGas = contractTransactionGasValue();
        final long transferVolume = transferVolumeValue();
        final long responseMemoryByte = responseMemoryByteValue();
        final long responseDiskByte = responseDiskByteValue();
        final FeeComponents subject = new FeeComponents(minimumFee, maximumFee, constantFee, transactionBandwidthByte, transactionVerification, transactionRamByteHour, transactionStorageByteHour, contractTransactionGas, transferVolume, responseMemoryByte, responseDiskByte);
        assertValue(minimumFee, subject.minimumFee());
        assertValue(maximumFee, subject.maximumFee());
        assertValue(constantFee, subject.constantFee());
        assertValue(transactionBandwidthByte, subject.transactionBandwidthByte());
        assertValue(transactionVerification, subject.transactionVerification());
        assertValue(transactionRamByteHour, subject.transactionRamByteHour());
        assertValue(transactionStorageByteHour, subject.transactionStorageByteHour());
        assertValue(contractTransactionGas, subject.contractTransactionGas());
        assertValue(transferVolume, subject.transferVolume());
        assertValue(responseMemoryByte, subject.responseMemoryByte());
        assertValue(responseDiskByte, subject.responseDiskByte());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:consensusnode.queries.fees.FeeComponents/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final long minimumFee = minimumFeeValue();
        final long maximumFee = maximumFeeValue();
        final long constantFee = constantFeeValue();
        final long transactionBandwidthByte = transactionBandwidthByteValue();
        final long transactionVerification = transactionVerificationValue();
        final long transactionRamByteHour = transactionRamByteHourValue();
        final long transactionStorageByteHour = transactionStorageByteHourValue();
        final long contractTransactionGas = contractTransactionGasValue();
        final long transferVolume = transferVolumeValue();
        final long responseMemoryByte = responseMemoryByteValue();
        final long responseDiskByte = responseDiskByteValue();
        final FeeComponents subject = FeeComponents.builder()
                .minimumFee(minimumFee)
                .maximumFee(maximumFee)
                .constantFee(constantFee)
                .transactionBandwidthByte(transactionBandwidthByte)
                .transactionVerification(transactionVerification)
                .transactionRamByteHour(transactionRamByteHour)
                .transactionStorageByteHour(transactionStorageByteHour)
                .contractTransactionGas(contractTransactionGas)
                .transferVolume(transferVolume)
                .responseMemoryByte(responseMemoryByte)
                .responseDiskByte(responseDiskByte)
                .build();
        assertValue(minimumFee, subject.minimumFee());
        assertValue(maximumFee, subject.maximumFee());
        assertValue(constantFee, subject.constantFee());
        assertValue(transactionBandwidthByte, subject.transactionBandwidthByte());
        assertValue(transactionVerification, subject.transactionVerification());
        assertValue(transactionRamByteHour, subject.transactionRamByteHour());
        assertValue(transactionStorageByteHour, subject.transactionStorageByteHour());
        assertValue(contractTransactionGas, subject.contractTransactionGas());
        assertValue(transferVolume, subject.transferVolume());
        assertValue(responseMemoryByte, subject.responseMemoryByte());
        assertValue(responseDiskByte, subject.responseDiskByte());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:consensusnode.queries.fees.FeeComponents/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final FeeComponents subject = FeeComponents.builder()
                .minimumFee(minimumFeeValue())
                .maximumFee(maximumFeeValue())
                .constantFee(constantFeeValue())
                .transactionBandwidthByte(transactionBandwidthByteValue())
                .transactionVerification(transactionVerificationValue())
                .transactionRamByteHour(transactionRamByteHourValue())
                .transactionStorageByteHour(transactionStorageByteHourValue())
                .contractTransactionGas(contractTransactionGasValue())
                .transferVolume(transferVolumeValue())
                .responseMemoryByte(responseMemoryByteValue())
                .responseDiskByte(responseDiskByteValue())
                .build();
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:consensusnode.queries.fees.FeeComponents/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> FeeComponents.builder().build());
    }

    @Test
    @DisplayName("minimumFee: accepts the minimum (0)")
    @Tag("testId:consensusnode.queries.fees.FeeComponents/minimumFee/accepts-at-minimum")
    void shouldAcceptMinimumFeeAtMinimum() {
        final long value = 0L;
        final FeeComponents subject = new FeeComponents(value, maximumFeeValue(), constantFeeValue(), transactionBandwidthByteValue(), transactionVerificationValue(), transactionRamByteHourValue(), transactionStorageByteHourValue(), contractTransactionGasValue(), transferVolumeValue(), responseMemoryByteValue(), responseDiskByteValue());
        assertValue(value, subject.minimumFee());
    }

    @Test
    @DisplayName("minimumFee: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.queries.fees.FeeComponents/minimumFee/accepts-at-maximum")
    void shouldAcceptMinimumFeeAtMaximum() {
        final long value = 9223372036854775807L;
        final FeeComponents subject = new FeeComponents(value, maximumFeeValue(), constantFeeValue(), transactionBandwidthByteValue(), transactionVerificationValue(), transactionRamByteHourValue(), transactionStorageByteHourValue(), contractTransactionGasValue(), transferVolumeValue(), responseMemoryByteValue(), responseDiskByteValue());
        assertValue(value, subject.minimumFee());
    }

    @Test
    @DisplayName("minimumFee: rejects a value below the minimum (-1)")
    @Tag("testId:consensusnode.queries.fees.FeeComponents/minimumFee/rejects-below-minimum")
    void shouldRejectMinimumFeeBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new FeeComponents(-1L, maximumFeeValue(), constantFeeValue(), transactionBandwidthByteValue(), transactionVerificationValue(), transactionRamByteHourValue(), transactionStorageByteHourValue(), contractTransactionGasValue(), transferVolumeValue(), responseMemoryByteValue(), responseDiskByteValue()));
    }

    @Test
    @DisplayName("maximumFee: accepts the minimum (0)")
    @Tag("testId:consensusnode.queries.fees.FeeComponents/maximumFee/accepts-at-minimum")
    void shouldAcceptMaximumFeeAtMinimum() {
        final long value = 0L;
        final FeeComponents subject = new FeeComponents(minimumFeeValue(), value, constantFeeValue(), transactionBandwidthByteValue(), transactionVerificationValue(), transactionRamByteHourValue(), transactionStorageByteHourValue(), contractTransactionGasValue(), transferVolumeValue(), responseMemoryByteValue(), responseDiskByteValue());
        assertValue(value, subject.maximumFee());
    }

    @Test
    @DisplayName("maximumFee: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.queries.fees.FeeComponents/maximumFee/accepts-at-maximum")
    void shouldAcceptMaximumFeeAtMaximum() {
        final long value = 9223372036854775807L;
        final FeeComponents subject = new FeeComponents(minimumFeeValue(), value, constantFeeValue(), transactionBandwidthByteValue(), transactionVerificationValue(), transactionRamByteHourValue(), transactionStorageByteHourValue(), contractTransactionGasValue(), transferVolumeValue(), responseMemoryByteValue(), responseDiskByteValue());
        assertValue(value, subject.maximumFee());
    }

    @Test
    @DisplayName("maximumFee: rejects a value below the minimum (-1)")
    @Tag("testId:consensusnode.queries.fees.FeeComponents/maximumFee/rejects-below-minimum")
    void shouldRejectMaximumFeeBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new FeeComponents(minimumFeeValue(), -1L, constantFeeValue(), transactionBandwidthByteValue(), transactionVerificationValue(), transactionRamByteHourValue(), transactionStorageByteHourValue(), contractTransactionGasValue(), transferVolumeValue(), responseMemoryByteValue(), responseDiskByteValue()));
    }

    @Test
    @DisplayName("constantFee: accepts the minimum (0)")
    @Tag("testId:consensusnode.queries.fees.FeeComponents/constantFee/accepts-at-minimum")
    void shouldAcceptConstantFeeAtMinimum() {
        final long value = 0L;
        final FeeComponents subject = new FeeComponents(minimumFeeValue(), maximumFeeValue(), value, transactionBandwidthByteValue(), transactionVerificationValue(), transactionRamByteHourValue(), transactionStorageByteHourValue(), contractTransactionGasValue(), transferVolumeValue(), responseMemoryByteValue(), responseDiskByteValue());
        assertValue(value, subject.constantFee());
    }

    @Test
    @DisplayName("constantFee: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.queries.fees.FeeComponents/constantFee/accepts-at-maximum")
    void shouldAcceptConstantFeeAtMaximum() {
        final long value = 9223372036854775807L;
        final FeeComponents subject = new FeeComponents(minimumFeeValue(), maximumFeeValue(), value, transactionBandwidthByteValue(), transactionVerificationValue(), transactionRamByteHourValue(), transactionStorageByteHourValue(), contractTransactionGasValue(), transferVolumeValue(), responseMemoryByteValue(), responseDiskByteValue());
        assertValue(value, subject.constantFee());
    }

    @Test
    @DisplayName("constantFee: rejects a value below the minimum (-1)")
    @Tag("testId:consensusnode.queries.fees.FeeComponents/constantFee/rejects-below-minimum")
    void shouldRejectConstantFeeBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new FeeComponents(minimumFeeValue(), maximumFeeValue(), -1L, transactionBandwidthByteValue(), transactionVerificationValue(), transactionRamByteHourValue(), transactionStorageByteHourValue(), contractTransactionGasValue(), transferVolumeValue(), responseMemoryByteValue(), responseDiskByteValue()));
    }

    @Test
    @DisplayName("transactionBandwidthByte: accepts the minimum (0)")
    @Tag("testId:consensusnode.queries.fees.FeeComponents/transactionBandwidthByte/accepts-at-minimum")
    void shouldAcceptTransactionBandwidthByteAtMinimum() {
        final long value = 0L;
        final FeeComponents subject = new FeeComponents(minimumFeeValue(), maximumFeeValue(), constantFeeValue(), value, transactionVerificationValue(), transactionRamByteHourValue(), transactionStorageByteHourValue(), contractTransactionGasValue(), transferVolumeValue(), responseMemoryByteValue(), responseDiskByteValue());
        assertValue(value, subject.transactionBandwidthByte());
    }

    @Test
    @DisplayName("transactionBandwidthByte: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.queries.fees.FeeComponents/transactionBandwidthByte/accepts-at-maximum")
    void shouldAcceptTransactionBandwidthByteAtMaximum() {
        final long value = 9223372036854775807L;
        final FeeComponents subject = new FeeComponents(minimumFeeValue(), maximumFeeValue(), constantFeeValue(), value, transactionVerificationValue(), transactionRamByteHourValue(), transactionStorageByteHourValue(), contractTransactionGasValue(), transferVolumeValue(), responseMemoryByteValue(), responseDiskByteValue());
        assertValue(value, subject.transactionBandwidthByte());
    }

    @Test
    @DisplayName("transactionBandwidthByte: rejects a value below the minimum (-1)")
    @Tag("testId:consensusnode.queries.fees.FeeComponents/transactionBandwidthByte/rejects-below-minimum")
    void shouldRejectTransactionBandwidthByteBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new FeeComponents(minimumFeeValue(), maximumFeeValue(), constantFeeValue(), -1L, transactionVerificationValue(), transactionRamByteHourValue(), transactionStorageByteHourValue(), contractTransactionGasValue(), transferVolumeValue(), responseMemoryByteValue(), responseDiskByteValue()));
    }

    @Test
    @DisplayName("transactionVerification: accepts the minimum (0)")
    @Tag("testId:consensusnode.queries.fees.FeeComponents/transactionVerification/accepts-at-minimum")
    void shouldAcceptTransactionVerificationAtMinimum() {
        final long value = 0L;
        final FeeComponents subject = new FeeComponents(minimumFeeValue(), maximumFeeValue(), constantFeeValue(), transactionBandwidthByteValue(), value, transactionRamByteHourValue(), transactionStorageByteHourValue(), contractTransactionGasValue(), transferVolumeValue(), responseMemoryByteValue(), responseDiskByteValue());
        assertValue(value, subject.transactionVerification());
    }

    @Test
    @DisplayName("transactionVerification: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.queries.fees.FeeComponents/transactionVerification/accepts-at-maximum")
    void shouldAcceptTransactionVerificationAtMaximum() {
        final long value = 9223372036854775807L;
        final FeeComponents subject = new FeeComponents(minimumFeeValue(), maximumFeeValue(), constantFeeValue(), transactionBandwidthByteValue(), value, transactionRamByteHourValue(), transactionStorageByteHourValue(), contractTransactionGasValue(), transferVolumeValue(), responseMemoryByteValue(), responseDiskByteValue());
        assertValue(value, subject.transactionVerification());
    }

    @Test
    @DisplayName("transactionVerification: rejects a value below the minimum (-1)")
    @Tag("testId:consensusnode.queries.fees.FeeComponents/transactionVerification/rejects-below-minimum")
    void shouldRejectTransactionVerificationBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new FeeComponents(minimumFeeValue(), maximumFeeValue(), constantFeeValue(), transactionBandwidthByteValue(), -1L, transactionRamByteHourValue(), transactionStorageByteHourValue(), contractTransactionGasValue(), transferVolumeValue(), responseMemoryByteValue(), responseDiskByteValue()));
    }

    @Test
    @DisplayName("transactionRamByteHour: accepts the minimum (0)")
    @Tag("testId:consensusnode.queries.fees.FeeComponents/transactionRamByteHour/accepts-at-minimum")
    void shouldAcceptTransactionRamByteHourAtMinimum() {
        final long value = 0L;
        final FeeComponents subject = new FeeComponents(minimumFeeValue(), maximumFeeValue(), constantFeeValue(), transactionBandwidthByteValue(), transactionVerificationValue(), value, transactionStorageByteHourValue(), contractTransactionGasValue(), transferVolumeValue(), responseMemoryByteValue(), responseDiskByteValue());
        assertValue(value, subject.transactionRamByteHour());
    }

    @Test
    @DisplayName("transactionRamByteHour: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.queries.fees.FeeComponents/transactionRamByteHour/accepts-at-maximum")
    void shouldAcceptTransactionRamByteHourAtMaximum() {
        final long value = 9223372036854775807L;
        final FeeComponents subject = new FeeComponents(minimumFeeValue(), maximumFeeValue(), constantFeeValue(), transactionBandwidthByteValue(), transactionVerificationValue(), value, transactionStorageByteHourValue(), contractTransactionGasValue(), transferVolumeValue(), responseMemoryByteValue(), responseDiskByteValue());
        assertValue(value, subject.transactionRamByteHour());
    }

    @Test
    @DisplayName("transactionRamByteHour: rejects a value below the minimum (-1)")
    @Tag("testId:consensusnode.queries.fees.FeeComponents/transactionRamByteHour/rejects-below-minimum")
    void shouldRejectTransactionRamByteHourBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new FeeComponents(minimumFeeValue(), maximumFeeValue(), constantFeeValue(), transactionBandwidthByteValue(), transactionVerificationValue(), -1L, transactionStorageByteHourValue(), contractTransactionGasValue(), transferVolumeValue(), responseMemoryByteValue(), responseDiskByteValue()));
    }

    @Test
    @DisplayName("transactionStorageByteHour: accepts the minimum (0)")
    @Tag("testId:consensusnode.queries.fees.FeeComponents/transactionStorageByteHour/accepts-at-minimum")
    void shouldAcceptTransactionStorageByteHourAtMinimum() {
        final long value = 0L;
        final FeeComponents subject = new FeeComponents(minimumFeeValue(), maximumFeeValue(), constantFeeValue(), transactionBandwidthByteValue(), transactionVerificationValue(), transactionRamByteHourValue(), value, contractTransactionGasValue(), transferVolumeValue(), responseMemoryByteValue(), responseDiskByteValue());
        assertValue(value, subject.transactionStorageByteHour());
    }

    @Test
    @DisplayName("transactionStorageByteHour: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.queries.fees.FeeComponents/transactionStorageByteHour/accepts-at-maximum")
    void shouldAcceptTransactionStorageByteHourAtMaximum() {
        final long value = 9223372036854775807L;
        final FeeComponents subject = new FeeComponents(minimumFeeValue(), maximumFeeValue(), constantFeeValue(), transactionBandwidthByteValue(), transactionVerificationValue(), transactionRamByteHourValue(), value, contractTransactionGasValue(), transferVolumeValue(), responseMemoryByteValue(), responseDiskByteValue());
        assertValue(value, subject.transactionStorageByteHour());
    }

    @Test
    @DisplayName("transactionStorageByteHour: rejects a value below the minimum (-1)")
    @Tag("testId:consensusnode.queries.fees.FeeComponents/transactionStorageByteHour/rejects-below-minimum")
    void shouldRejectTransactionStorageByteHourBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new FeeComponents(minimumFeeValue(), maximumFeeValue(), constantFeeValue(), transactionBandwidthByteValue(), transactionVerificationValue(), transactionRamByteHourValue(), -1L, contractTransactionGasValue(), transferVolumeValue(), responseMemoryByteValue(), responseDiskByteValue()));
    }

    @Test
    @DisplayName("contractTransactionGas: accepts the minimum (0)")
    @Tag("testId:consensusnode.queries.fees.FeeComponents/contractTransactionGas/accepts-at-minimum")
    void shouldAcceptContractTransactionGasAtMinimum() {
        final long value = 0L;
        final FeeComponents subject = new FeeComponents(minimumFeeValue(), maximumFeeValue(), constantFeeValue(), transactionBandwidthByteValue(), transactionVerificationValue(), transactionRamByteHourValue(), transactionStorageByteHourValue(), value, transferVolumeValue(), responseMemoryByteValue(), responseDiskByteValue());
        assertValue(value, subject.contractTransactionGas());
    }

    @Test
    @DisplayName("contractTransactionGas: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.queries.fees.FeeComponents/contractTransactionGas/accepts-at-maximum")
    void shouldAcceptContractTransactionGasAtMaximum() {
        final long value = 9223372036854775807L;
        final FeeComponents subject = new FeeComponents(minimumFeeValue(), maximumFeeValue(), constantFeeValue(), transactionBandwidthByteValue(), transactionVerificationValue(), transactionRamByteHourValue(), transactionStorageByteHourValue(), value, transferVolumeValue(), responseMemoryByteValue(), responseDiskByteValue());
        assertValue(value, subject.contractTransactionGas());
    }

    @Test
    @DisplayName("contractTransactionGas: rejects a value below the minimum (-1)")
    @Tag("testId:consensusnode.queries.fees.FeeComponents/contractTransactionGas/rejects-below-minimum")
    void shouldRejectContractTransactionGasBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new FeeComponents(minimumFeeValue(), maximumFeeValue(), constantFeeValue(), transactionBandwidthByteValue(), transactionVerificationValue(), transactionRamByteHourValue(), transactionStorageByteHourValue(), -1L, transferVolumeValue(), responseMemoryByteValue(), responseDiskByteValue()));
    }

    @Test
    @DisplayName("transferVolume: accepts the minimum (0)")
    @Tag("testId:consensusnode.queries.fees.FeeComponents/transferVolume/accepts-at-minimum")
    void shouldAcceptTransferVolumeAtMinimum() {
        final long value = 0L;
        final FeeComponents subject = new FeeComponents(minimumFeeValue(), maximumFeeValue(), constantFeeValue(), transactionBandwidthByteValue(), transactionVerificationValue(), transactionRamByteHourValue(), transactionStorageByteHourValue(), contractTransactionGasValue(), value, responseMemoryByteValue(), responseDiskByteValue());
        assertValue(value, subject.transferVolume());
    }

    @Test
    @DisplayName("transferVolume: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.queries.fees.FeeComponents/transferVolume/accepts-at-maximum")
    void shouldAcceptTransferVolumeAtMaximum() {
        final long value = 9223372036854775807L;
        final FeeComponents subject = new FeeComponents(minimumFeeValue(), maximumFeeValue(), constantFeeValue(), transactionBandwidthByteValue(), transactionVerificationValue(), transactionRamByteHourValue(), transactionStorageByteHourValue(), contractTransactionGasValue(), value, responseMemoryByteValue(), responseDiskByteValue());
        assertValue(value, subject.transferVolume());
    }

    @Test
    @DisplayName("transferVolume: rejects a value below the minimum (-1)")
    @Tag("testId:consensusnode.queries.fees.FeeComponents/transferVolume/rejects-below-minimum")
    void shouldRejectTransferVolumeBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new FeeComponents(minimumFeeValue(), maximumFeeValue(), constantFeeValue(), transactionBandwidthByteValue(), transactionVerificationValue(), transactionRamByteHourValue(), transactionStorageByteHourValue(), contractTransactionGasValue(), -1L, responseMemoryByteValue(), responseDiskByteValue()));
    }

    @Test
    @DisplayName("responseMemoryByte: accepts the minimum (0)")
    @Tag("testId:consensusnode.queries.fees.FeeComponents/responseMemoryByte/accepts-at-minimum")
    void shouldAcceptResponseMemoryByteAtMinimum() {
        final long value = 0L;
        final FeeComponents subject = new FeeComponents(minimumFeeValue(), maximumFeeValue(), constantFeeValue(), transactionBandwidthByteValue(), transactionVerificationValue(), transactionRamByteHourValue(), transactionStorageByteHourValue(), contractTransactionGasValue(), transferVolumeValue(), value, responseDiskByteValue());
        assertValue(value, subject.responseMemoryByte());
    }

    @Test
    @DisplayName("responseMemoryByte: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.queries.fees.FeeComponents/responseMemoryByte/accepts-at-maximum")
    void shouldAcceptResponseMemoryByteAtMaximum() {
        final long value = 9223372036854775807L;
        final FeeComponents subject = new FeeComponents(minimumFeeValue(), maximumFeeValue(), constantFeeValue(), transactionBandwidthByteValue(), transactionVerificationValue(), transactionRamByteHourValue(), transactionStorageByteHourValue(), contractTransactionGasValue(), transferVolumeValue(), value, responseDiskByteValue());
        assertValue(value, subject.responseMemoryByte());
    }

    @Test
    @DisplayName("responseMemoryByte: rejects a value below the minimum (-1)")
    @Tag("testId:consensusnode.queries.fees.FeeComponents/responseMemoryByte/rejects-below-minimum")
    void shouldRejectResponseMemoryByteBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new FeeComponents(minimumFeeValue(), maximumFeeValue(), constantFeeValue(), transactionBandwidthByteValue(), transactionVerificationValue(), transactionRamByteHourValue(), transactionStorageByteHourValue(), contractTransactionGasValue(), transferVolumeValue(), -1L, responseDiskByteValue()));
    }

    @Test
    @DisplayName("responseDiskByte: accepts the minimum (0)")
    @Tag("testId:consensusnode.queries.fees.FeeComponents/responseDiskByte/accepts-at-minimum")
    void shouldAcceptResponseDiskByteAtMinimum() {
        final long value = 0L;
        final FeeComponents subject = new FeeComponents(minimumFeeValue(), maximumFeeValue(), constantFeeValue(), transactionBandwidthByteValue(), transactionVerificationValue(), transactionRamByteHourValue(), transactionStorageByteHourValue(), contractTransactionGasValue(), transferVolumeValue(), responseMemoryByteValue(), value);
        assertValue(value, subject.responseDiskByte());
    }

    @Test
    @DisplayName("responseDiskByte: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.queries.fees.FeeComponents/responseDiskByte/accepts-at-maximum")
    void shouldAcceptResponseDiskByteAtMaximum() {
        final long value = 9223372036854775807L;
        final FeeComponents subject = new FeeComponents(minimumFeeValue(), maximumFeeValue(), constantFeeValue(), transactionBandwidthByteValue(), transactionVerificationValue(), transactionRamByteHourValue(), transactionStorageByteHourValue(), contractTransactionGasValue(), transferVolumeValue(), responseMemoryByteValue(), value);
        assertValue(value, subject.responseDiskByte());
    }

    @Test
    @DisplayName("responseDiskByte: rejects a value below the minimum (-1)")
    @Tag("testId:consensusnode.queries.fees.FeeComponents/responseDiskByte/rejects-below-minimum")
    void shouldRejectResponseDiskByteBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new FeeComponents(minimumFeeValue(), maximumFeeValue(), constantFeeValue(), transactionBandwidthByteValue(), transactionVerificationValue(), transactionRamByteHourValue(), transactionStorageByteHourValue(), contractTransactionGasValue(), transferVolumeValue(), responseMemoryByteValue(), -1L));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:consensusnode.queries.fees.FeeComponents/value-semantics")
    void shouldCompareByValue() {
        final FeeComponents subject = create();
        final FeeComponents other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static long minimumFeeValue() {
        return 1L;
    }

    private static long maximumFeeValue() {
        return 1L;
    }

    private static long constantFeeValue() {
        return 1L;
    }

    private static long transactionBandwidthByteValue() {
        return 1L;
    }

    private static long transactionVerificationValue() {
        return 1L;
    }

    private static long transactionRamByteHourValue() {
        return 1L;
    }

    private static long transactionStorageByteHourValue() {
        return 1L;
    }

    private static long contractTransactionGasValue() {
        return 1L;
    }

    private static long transferVolumeValue() {
        return 1L;
    }

    private static long responseMemoryByteValue() {
        return 1L;
    }

    private static long responseDiskByteValue() {
        return 1L;
    }

    private static FeeComponents create() {
        return new FeeComponents(minimumFeeValue(), maximumFeeValue(), constantFeeValue(), transactionBandwidthByteValue(), transactionVerificationValue(), transactionRamByteHourValue(), transactionStorageByteHourValue(), contractTransactionGasValue(), transferVolumeValue(), responseMemoryByteValue(), responseDiskByteValue());
    }
}
