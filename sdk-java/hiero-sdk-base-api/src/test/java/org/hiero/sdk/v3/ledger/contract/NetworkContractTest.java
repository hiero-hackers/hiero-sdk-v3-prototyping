package org.hiero.sdk.v3.ledger.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import org.hiero.sdk.v3.ledger.Network;
import org.hiero.sdk.v3.nativeToken.NativeTokenUnit;
import org.junit.jupiter.api.Test;

class NetworkContractTest {
    private enum Unit implements NativeTokenUnit {
        BASE;

        @Override
        public String symbol() {
            return "b";
        }

        @Override
        public long baseUnitFactor() {
            return 1;
        }
    }

    private enum OtherUnit implements NativeTokenUnit {
        OTHER;

        @Override
        public String symbol() {
            return "o";
        }

        @Override
        public long baseUnitFactor() {
            return 10;
        }
    }

    @Test
    void exposesIdNameAndTypedUnit() {
        final Network<Unit> network = new Network<>(new byte[] {0, 1, 2}, "testnet", Unit.BASE);
        final Unit unit = network.nativeTokenUnit();

        assertThat(network.id()).containsExactly(0, 1, 2);
        assertThat(network.name()).isEqualTo("testnet");
        assertThat(unit).isSameAs(Unit.BASE);
        assertThat(network.id()).containsExactly(0, 1, 2);
    }

    @Test
    void copiesTheIdOnConstruction() {
        final var source = new byte[] {1, 2, 3};
        final var network = new Network<>(source, null, Unit.BASE);

        source[0] = 99;

        assertThat(network.id()).containsExactly(1, 2, 3);
    }

    @Test
    void returnsIndependentIdCopies() {
        final var network = new Network<>(new byte[] {1, 2, 3}, null, Unit.BASE);

        final var first = network.id();
        first[1] = 99;
        final var second = network.id();

        assertThat(second).containsExactly(1, 2, 3).isNotSameAs(first);
    }

    @Test
    void acceptsEmptyAndLeadingZeroIdsAndExtremeBytes() {
        assertThat(new Network<>(new byte[0], null, Unit.BASE).id()).isEmpty();
        assertThat(new Network<>(new byte[] {0, 0, 1}, null, Unit.BASE).id()).containsExactly(0, 0, 1);
        assertThat(new Network<>(new byte[] {-128, 127, -1}, null, Unit.BASE).id()).containsExactly(-128, 127, -1);
    }

    @Test
    void rejectsNullIdAndUnitWithDescriptiveMessages() {
        assertThatNullPointerException()
                .isThrownBy(() -> new Network<>(null, "n", Unit.BASE))
                .withMessage("id");
        assertThatNullPointerException()
                .isThrownBy(() -> new Network<Unit>(new byte[0], "n", null))
                .withMessage("nativeTokenUnit");
    }

    @Test
    void preservesAbsentEmptyAndUntrimmedNames() {
        assertThat(new Network<>(new byte[0], null, Unit.BASE).name()).isNull();
        assertThat(new Network<>(new byte[0], "", Unit.BASE).name()).isEmpty();
        assertThat(new Network<>(new byte[0], "  Main Net ", Unit.BASE).name()).isEqualTo("  Main Net ");
    }

    @Test
    void equalityUsesIdContentsNameAndUnit() {
        final var a = new Network<>(new byte[] {1, 2}, "n", Unit.BASE);

        assertThat(a).isEqualTo(new Network<>(new byte[] {1, 2}, "n", Unit.BASE));
        assertThat(a.hashCode()).isEqualTo(new Network<>(new byte[] {1, 2}, "n", Unit.BASE).hashCode());
        assertThat(a).isNotEqualTo(new Network<>(new byte[] {1, 3}, "n", Unit.BASE));
        assertThat(a).isNotEqualTo(new Network<>(new byte[] {1, 2}, null, Unit.BASE));
        assertThat(a).isNotEqualTo(new Network<>(new byte[] {1, 2}, "n", OtherUnit.OTHER));
    }

    @Test
    void toStringRendersIdAsHex() {
        assertThat(new Network<>(new byte[] {0, 15, -1}, "n", Unit.BASE).toString())
                .startsWith("Network[id=000fff, name=n, nativeTokenUnit=");
    }
}
