package org.hiero.sdk.v3.codegen.mapping;

import java.util.List;
import org.hiero.sdk.v3.codegen.render.JavaSourceFile;

public final class LedgerMapping {
    public List<JavaSourceFile> sourceFiles() {
        return List.of(
                MappingSources.type("ledger", "Network", """
                        /** Immutable identity and native-unit description of a network. */
                        public final class Network<Unit extends NativeTokenUnit> {
                            private final byte[] id;
                            private final @Nullable String name;
                            private final Unit nativeTokenUnit;
                            /** Creates a network value and copies its identifier. */
                            public Network(byte[] id, @Nullable String name, Unit nativeTokenUnit) {
                                this.id = Objects.requireNonNull(id, "id").clone();
                                this.name = name;
                                this.nativeTokenUnit = Objects.requireNonNull(nativeTokenUnit, "nativeTokenUnit");
                            }
                            /** Returns a copy of the network identifier. */
                            public byte[] id() { return id.clone(); }
                            /** Returns the optional display name. */
                            public @Nullable String name() { return name; }
                            /** Returns the network native-token unit. */
                            public Unit nativeTokenUnit() { return nativeTokenUnit; }
                            @Override public boolean equals(Object other) {
                                return other instanceof Network<?> value && Arrays.equals(id, value.id)
                                        && Objects.equals(name, value.name) && nativeTokenUnit.equals(value.nativeTokenUnit);
                            }
                            @Override public int hashCode() { return Objects.hash(Arrays.hashCode(id), name, nativeTokenUnit); }
                        }
                        """, "java.util.Arrays", "java.util.Objects", "org.hiero.sdk.v3.nativetoken.NativeTokenUnit",
                        "org.jspecify.annotations.Nullable"),
                MappingSources.type("ledger", "BaseAddress", """
                        /** Base data contract for identifiers in shard/realm space. */
                        public sealed interface BaseAddress permits Address, EvmCapableAddress {
                            /** Returns the non-negative shard. */ long shard();
                            /** Returns the non-negative realm. */ long realm();
                            /** Returns the checksum, or an empty string. */ String checksum();
                            /** Returns the optional numeric selector. */ @Nullable Long num();
                        }
                        """, "org.jspecify.annotations.Nullable"),
                MappingSources.type("ledger", "Address", """
                        /** Immutable numeric shard/realm address. */
                        public final class Address implements BaseAddress {
                            private final long shard;
                            private final long realm;
                            private final long num;
                            private final String checksum;
                            /** Creates a numeric address. */
                            public Address(long shard, long realm, long num, String checksum) {
                                if (shard < 0 || realm < 0 || num < 0) throw new IllegalArgumentException("address values must be non-negative");
                                this.shard = shard; this.realm = realm; this.num = num;
                                this.checksum = Objects.requireNonNull(checksum, "checksum");
                            }
                            @Override public long shard() { return shard; }
                            @Override public long realm() { return realm; }
                            @Override public String checksum() { return checksum; }
                            @Override public Long num() { return num; }
                            @Override public boolean equals(Object other) { return other instanceof Address value && shard == value.shard && realm == value.realm && num == value.num && checksum.equals(value.checksum); }
                            @Override public int hashCode() { return Objects.hash(shard, realm, num, checksum); }
                        }
                        """, "java.util.Objects"),
                MappingSources.type("ledger", "EvmAddress", byteValue("EvmAddress", 20), "java.util.Arrays", "java.util.Objects"),
                MappingSources.type("ledger", "EvmCapableAddress", """
                        /** Base data contract for numeric or EVM-capable identifiers. */
                        public sealed interface EvmCapableAddress extends BaseAddress permits ContractId, AccountId {
                            /** Returns the optional EVM selector. */ @Nullable EvmAddress evmAddress();
                        }
                        """, "org.jspecify.annotations.Nullable"),
                MappingSources.type("ledger", "ContractId", """
                        /** Immutable contract identifier with exactly one numeric or EVM selector. */
                        public final class ContractId implements EvmCapableAddress {
                            private final long shard; private final long realm; private final @Nullable Long num;
                            private final String checksum; private final @Nullable EvmAddress evmAddress;
                            /** Creates a contract identifier. */
                            public ContractId(long shard, long realm, @Nullable Long num, String checksum, @Nullable EvmAddress evmAddress) {
                                if (shard < 0 || realm < 0 || (num != null && num < 0)) throw new IllegalArgumentException("identifier values must be non-negative");
                                if ((num == null) == (evmAddress == null)) throw new IllegalArgumentException("exactly one of num and evmAddress is required");
                                this.shard = shard; this.realm = realm; this.num = num;
                                this.checksum = Objects.requireNonNull(checksum, "checksum"); this.evmAddress = evmAddress;
                            }
                            @Override public long shard() { return shard; }
                            @Override public long realm() { return realm; }
                            @Override public @Nullable Long num() { return num; }
                            @Override public String checksum() { return checksum; }
                            @Override public @Nullable EvmAddress evmAddress() { return evmAddress; }
                            @Override public boolean equals(Object other) { return other instanceof ContractId value && shard == value.shard && realm == value.realm && Objects.equals(num, value.num) && checksum.equals(value.checksum) && Objects.equals(evmAddress, value.evmAddress); }
                            @Override public int hashCode() { return Objects.hash(shard, realm, num, checksum, evmAddress); }
                        }
                        """, "java.util.Objects", "org.jspecify.annotations.Nullable"),
                MappingSources.type("ledger", "AccountId", """
                        /** Immutable account identifier with exactly one numeric, EVM, or key-alias selector. */
                        public final class AccountId implements EvmCapableAddress {
                            private final long shard; private final long realm; private final @Nullable Long num;
                            private final String checksum; private final @Nullable EvmAddress evmAddress; private final byte @Nullable [] alias;
                            /** Creates an account identifier and copies its alias. */
                            public AccountId(long shard, long realm, @Nullable Long num, String checksum, @Nullable EvmAddress evmAddress, byte @Nullable [] alias) {
                                if (shard < 0 || realm < 0 || (num != null && num < 0)) throw new IllegalArgumentException("identifier values must be non-negative");
                                var selectors = (num == null ? 0 : 1) + (evmAddress == null ? 0 : 1) + (alias == null ? 0 : 1);
                                if (selectors != 1) throw new IllegalArgumentException("exactly one of num, evmAddress, and alias is required");
                                this.shard = shard; this.realm = realm; this.num = num; this.checksum = Objects.requireNonNull(checksum, "checksum");
                                this.evmAddress = evmAddress; this.alias = alias == null ? null : alias.clone();
                            }
                            @Override public long shard() { return shard; }
                            @Override public long realm() { return realm; }
                            @Override public @Nullable Long num() { return num; }
                            @Override public String checksum() { return checksum; }
                            @Override public @Nullable EvmAddress evmAddress() { return evmAddress; }
                            /** Returns a copy of the optional key alias. */ public byte @Nullable [] alias() { return alias == null ? null : alias.clone(); }
                            @Override public boolean equals(Object other) { return other instanceof AccountId value && shard == value.shard && realm == value.realm && Objects.equals(num, value.num) && checksum.equals(value.checksum) && Objects.equals(evmAddress, value.evmAddress) && Arrays.equals(alias, value.alias); }
                            @Override public int hashCode() { return Objects.hash(shard, realm, num, checksum, evmAddress, Arrays.hashCode(alias)); }
                        }
                        """, "java.util.Arrays", "java.util.Objects", "org.jspecify.annotations.Nullable"),
                MappingSources.type("ledger", "TransactionId", """
                        /** Provider-owned transaction identifier data contract. */
                        public interface TransactionId {
                            /** Returns the payer account. */ AccountId accountId();
                            /** Returns the valid-start time. */ ZonedDateTime validStart();
                            /** Returns the optional internal-transaction nonce. */ @Nullable Integer nonce();
                        }
                        """, "java.time.ZonedDateTime", "org.jspecify.annotations.Nullable"),
                MappingSources.type("ledger", "IpAddress", byteValue("IpAddress", 4), "java.util.Arrays", "java.util.Objects"),
                MappingSources.type("ledger", "ConsensusNode", """
                        /** Consensus-node routing and fee identity. */
                        public record ConsensusNode(IpAddress ip, int port, AccountId account) {
                            /** Validates node values and unsigned-16-bit port semantics. */
                            public ConsensusNode {
                                Objects.requireNonNull(ip, "ip"); Objects.requireNonNull(account, "account");
                                if (port < 0 || port > 65_535) throw new IllegalArgumentException("port must be between 0 and 65535");
                            }
                        }
                        """, "java.util.Objects"),
                MappingSources.type("ledger", "MirrorNode", """
                        /** Mirror-node REST endpoint descriptor. */
                        public record MirrorNode(String restBaseUrl) {
                            /** Validates the endpoint text without parsing or network access. */
                            public MirrorNode { Objects.requireNonNull(restBaseUrl, "restBaseUrl"); }
                        }
                        """, "java.util.Objects"),
                MappingSources.type("ledger", "LedgerFactory", """
                        /** Body-free ledger value construction and parsing contracts. */
                        public interface LedgerFactory {
                            EvmAddress evmAddressFromString(String value);
                            EvmAddress evmAddressFromBytes(byte[] value);
                            ContractId contractIdFromString(String value);
                            ContractId contractIdFromEvmAddress(long shard, long realm, EvmAddress address);
                            AccountId accountIdFromString(String value);
                            AccountId accountIdFromEvmAddress(long shard, long realm, EvmAddress address);
                            IpAddress ipAddressFromString(String value);
                            IpAddress ipAddressFromBytes(byte[] value);
                            Address addressFromString(String value);
                            TransactionId generateTransactionId(Address accountId);
                            TransactionId transactionIdFromString(String value);
                        }
                        """),
                MappingSources.type("ledger", "LedgerOperations", """
                        /** Body-free checksum and formatting contracts. */
                        public interface LedgerOperations {
                            boolean validateChecksum(BaseAddress address, Network<?> network);
                            String toCanonicalString(BaseAddress address);
                            String toStringWithChecksum(BaseAddress address);
                            String toCanonicalString(EvmAddress address);
                            String toCanonicalString(TransactionId transactionId);
                            String toStringWithChecksum(TransactionId transactionId);
                            String toCanonicalString(IpAddress address);
                        }
                        """),
                MappingSources.type("ledger", "LedgerConstants", """
                        /** Structural ledger sentinel values. */
                        public final class LedgerConstants {
                            /** Numeric zero-address clear sentinel. */ public static final Address ZERO_ADDRESS = new Address(0, 0, 0, "");
                            /** Numeric zero-account clear sentinel. */ public static final AccountId ZERO_ACCOUNT_ID = new AccountId(0, 0, 0L, "", null, null);
                            /** Numeric zero-contract clear sentinel. */ public static final ContractId ZERO_CONTRACT_ID = new ContractId(0, 0, 0L, "", null);
                            private LedgerConstants() {}
                        }
                        """),
                MappingSources.type("ledger.config", "NetworkSetting", """
                        /** Immutable network configuration contract. */
                        public interface NetworkSetting {
                            /** Returns the configured network. */ Network<?> network();
                            /** Returns an immutable consensus-node snapshot. */ Set<ConsensusNode> getConsensusNodes();
                            /** Returns an immutable mirror-node snapshot. */ Set<MirrorNode> getMirrorNodes();
                        }
                        """, "java.util.Set", "org.hiero.sdk.v3.ledger.ConsensusNode",
                        "org.hiero.sdk.v3.ledger.MirrorNode", "org.hiero.sdk.v3.ledger.Network"),
                MappingSources.type("ledger.config", "NetworkSettingRegistry", """
                        /** Body-free network-setting registry contract with no global state. */
                        public interface NetworkSettingRegistry {
                            /** Registers a setting under an identifier. */ void registerNetworkSetting(String identifier, NetworkSetting setting);
                            /** Returns a setting or throws when the identifier is absent. */ NetworkSetting getNetworkSetting(String identifier) throws NoSuchElementException;
                        }
                        """, "java.util.NoSuchElementException"));
    }

    private static String byteValue(String name, int length) {
        return """
                /** Immutable fixed-length network-order byte value. */
                public final class %s {
                    private final byte[] bytes;
                    /** Creates a value and defensively copies the bytes. */
                    public %s(byte[] bytes) {
                        Objects.requireNonNull(bytes, "bytes");
                        if (bytes.length != %d) throw new IllegalArgumentException("bytes must contain exactly %d elements");
                        this.bytes = bytes.clone();
                    }
                    /** Returns a defensive copy. */ public byte[] bytes() { return bytes.clone(); }
                    @Override public boolean equals(Object other) { return other instanceof %s value && Arrays.equals(bytes, value.bytes); }
                    @Override public int hashCode() { return Arrays.hashCode(bytes); }
                }
                """.formatted(name, name, length, length, name);
    }
}
