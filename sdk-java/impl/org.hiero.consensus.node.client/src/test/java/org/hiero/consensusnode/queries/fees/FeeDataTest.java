package org.hiero.consensusnode.queries.fees;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `FeeData`, generated from the specs.
@DisplayName("FeeData")
class FeeDataTest {

    @Test
    @DisplayName("creates a FeeData and returns the values")
    @Tag("testId:consensusnode.queries.fees.FeeData/create")
    void shouldCreateAndReturnTheValues() {
        final FeeComponents nodeData = nodeDataValue();
        final FeeComponents networkData = networkDataValue();
        final FeeComponents serviceData = serviceDataValue();
        final FeeSubType subType = subTypeValue();
        final FeeData subject = new FeeData(nodeData, networkData, serviceData, subType);
        assertValue(nodeData, subject.nodeData());
        assertValue(networkData, subject.networkData());
        assertValue(serviceData, subject.serviceData());
        assertValue(subType, subject.subType());
    }

    @Test
    @DisplayName("rejects null for nodeData")
    @Tag("testId:consensusnode.queries.fees.FeeData/nodeData/rejects-null")
    void shouldRejectNullNodeData() {
        assertThrows(NullPointerException.class, () -> new FeeData((FeeComponents) null, networkDataValue(), serviceDataValue(), subTypeValue()));
    }

    @Test
    @DisplayName("rejects null for networkData")
    @Tag("testId:consensusnode.queries.fees.FeeData/networkData/rejects-null")
    void shouldRejectNullNetworkData() {
        assertThrows(NullPointerException.class, () -> new FeeData(nodeDataValue(), (FeeComponents) null, serviceDataValue(), subTypeValue()));
    }

    @Test
    @DisplayName("rejects null for serviceData")
    @Tag("testId:consensusnode.queries.fees.FeeData/serviceData/rejects-null")
    void shouldRejectNullServiceData() {
        assertThrows(NullPointerException.class, () -> new FeeData(nodeDataValue(), networkDataValue(), (FeeComponents) null, subTypeValue()));
    }

    @Test
    @DisplayName("rejects null for subType")
    @Tag("testId:consensusnode.queries.fees.FeeData/subType/rejects-null")
    void shouldRejectNullSubType() {
        assertThrows(NullPointerException.class, () -> new FeeData(nodeDataValue(), networkDataValue(), serviceDataValue(), (FeeSubType) null));
    }

    private static FeeComponents nodeDataValue() {
        return new FeeComponents(1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L);
    }

    private static FeeComponents networkDataValue() {
        return new FeeComponents(1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L);
    }

    private static FeeComponents serviceDataValue() {
        return new FeeComponents(1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L);
    }

    private static FeeSubType subTypeValue() {
        return BasicFeeSubType.DEFAULT;
    }

    private static FeeData create() {
        return new FeeData(nodeDataValue(), networkDataValue(), serviceDataValue(), subTypeValue());
    }
}
