package org.hiero.sdk.v3.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.hiero.sdk.v3.authority.Authority;
import org.hiero.sdk.v3.authority.AuthorityList;
import org.hiero.sdk.v3.hedera.HbarUnit;
import org.hiero.sdk.v3.keys.KeyAlgorithm;
import org.hiero.sdk.v3.keys.KeyFormat;
import org.hiero.sdk.v3.keys.PublicKey;
import org.hiero.sdk.v3.ledger.AccountId;
import org.hiero.sdk.v3.ledger.Address;
import org.hiero.sdk.v3.ledger.ConsensusNode;
import org.hiero.sdk.v3.ledger.ContractId;
import org.hiero.sdk.v3.ledger.EvmAddress;
import org.hiero.sdk.v3.ledger.IpAddress;
import org.hiero.sdk.v3.ledger.Network;
import org.junit.jupiter.api.Test;

class StructuralValueContractTest {
    @Test
    void shouldRejectNullRangesLengthsAndSelectors() {
        assertThatThrownBy(() -> new Address(-1, 0, 1, "")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new IpAddress(new byte[3])).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TestEvmAddress(new byte[19])).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ContractId(0, 0, null, "", null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AccountId(0, 0, 1L, "", new TestEvmAddress(new byte[20]), null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ConsensusNode(IpAddress.fromBytes(new byte[4]), 65_536,
                new AccountId(0, 0, 1L, "", null, null))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldValidateAuthorityThresholdsAndExcludeEmptyLists() {
        final Authority leaf = Authority.of(new TestPublicKey(new byte[] {1}));
        assertThatThrownBy(Authority::of).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Authority.of(0, leaf)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Authority.of(2, leaf)).isInstanceOf(IllegalArgumentException.class);
        assertThat(Authority.of(leaf)).isEqualTo(new AuthorityList(List.of(leaf), 1));
    }

    @Test
    void shouldDefensivelyOwnArraysAndCollections() {
        final byte[] networkId = {1, 2, 3};
        final Network<HbarUnit> network = new Network<>(networkId, "test", HbarUnit.HBAR);
        networkId[0] = 9;
        final byte[] returned = network.id();
        returned[1] = 9;
        assertThat(network.id()).containsExactly(1, 2, 3);

        final List<Authority> mutable = new ArrayList<>();
        mutable.add(Authority.of(new TestPublicKey(new byte[] {4})));
        final AuthorityList list = new AuthorityList(mutable, 1);
        mutable.clear();
        assertThat(list.children()).hasSize(1);
        assertThatThrownBy(() -> list.children().clear()).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldProvideDeterministicFieldDerivedRepresentations() {
        assertThat(new Address(0, 1, 2, "abc").toString()).isEqualTo("0.1.2");
        assertThat(new Address(0, 1, 2, "abc").toStringWithChecksum()).isEqualTo("0.1.2-abc");
        assertThat(new ContractId(1, 2, 3L, "", null).toString()).isEqualTo("1.2.3");
        assertThat(new AccountId(1, 2, 3L, "", null, null).toString()).isEqualTo("1.2.3");
        assertThat(AccountId.fromEvmAddress(1, 2, new TestEvmAddress(new byte[20])).toString())
                .isEqualTo("1.2.0x0000000000000000000000000000000000000000");
        assertThat(ContractId.fromEvmAddress(1, 2, new TestEvmAddress(new byte[20])).toString())
                .isEqualTo("1.2.0x0000000000000000000000000000000000000000");
        assertThat(IpAddress.fromBytes(new byte[] {10, 0, 0, 7}).toString()).isEqualTo("10.0.0.7");
        assertThat(new AccountId(0, 0, null, "", null, new byte[] {(byte) 0xf8}).toString())
                .isEqualTo("0.0.V0");
        assertThat(HbarUnit.HBAR.symbol()).isEqualTo("ℏ");
        assertThat(HbarUnit.HBAR.baseUnitFactor()).isEqualTo(100_000_000L);
    }

    @Test
    void shouldProvideStructuralEqualityWithoutLeakingKeyMaterial() {
        final TestEvmAddress first = new TestEvmAddress(new byte[20]);
        final TestEvmAddress second = new TestEvmAddress(new byte[20]);
        assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);

        final TestPublicKey key = new TestPublicKey(new byte[] {(byte) 0xde, (byte) 0xad});
        assertThat(key.toString()).contains("algorithm=ED25519", "type=PUBLIC").doesNotContain("dead", "-34", "-83");
    }

    private static final class TestEvmAddress extends EvmAddress {
        private TestEvmAddress(final byte[] bytes) {
            super(bytes);
        }

        @Override
        public String toString() {
            return "0x" + java.util.HexFormat.of().formatHex(bytes());
        }
    }

    private static final class TestPublicKey extends PublicKey {
        private TestPublicKey(final byte[] bytes) {
            super(bytes, KeyAlgorithm.ED25519);
        }

        @Override public boolean verify(final byte[] message, final byte[] signature) { return false; }
        @Override public byte[] toRawBytes() { return bytes(); }
        @Override public byte[] toBytes(final KeyFormat container) { return bytes(); }
        @Override public String toString(final KeyFormat container) { return "redacted"; }
    }
}
