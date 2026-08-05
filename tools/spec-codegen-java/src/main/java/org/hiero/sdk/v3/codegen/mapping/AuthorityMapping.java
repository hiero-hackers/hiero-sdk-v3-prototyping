package org.hiero.sdk.v3.codegen.mapping;

import java.util.List;
import org.hiero.sdk.v3.codegen.render.JavaSourceFile;

public final class AuthorityMapping {
    public List<JavaSourceFile> sourceFiles() {
        return List.of(
                MappingSources.type("authority", "Authority", """
                        /** Closed authorization requirement data sum. */
                        public sealed interface Authority permits PublicKeyAuthority, ContractAuthority, AuthorityList {}
                        """),
                MappingSources.type("authority", "PublicKeyAuthority", """
                        /** Public-key authorization leaf. */
                        public record PublicKeyAuthority(PublicKey publicKey) implements Authority {
                            /** Validates the public key. */ public PublicKeyAuthority { Objects.requireNonNull(publicKey, "publicKey"); }
                        }
                        """, "java.util.Objects", "org.hiero.sdk.v3.keys.PublicKey"),
                MappingSources.type("authority", "ContractAuthority", """
                        /** Contract authorization leaf. */
                        public record ContractAuthority(ContractId contractId, boolean delegatable) implements Authority {
                            /** Validates the contract identifier. */ public ContractAuthority { Objects.requireNonNull(contractId, "contractId"); }
                        }
                        """, "java.util.Objects", "org.hiero.sdk.v3.ledger.ContractId"),
                MappingSources.type("authority", "AuthorityList", """
                        /** Immutable threshold composition of authorization requirements. */
                        public record AuthorityList(List<Authority> children, int threshold) implements Authority {
                            /** Copies children and validates non-empty threshold bounds. */
                            public AuthorityList {
                                children = List.copyOf(children);
                                if (children.isEmpty()) throw new IllegalArgumentException("children must not be empty");
                                if (threshold < 1 || threshold > children.size()) throw new IllegalArgumentException("threshold must be between 1 and children.size()");
                            }
                        }
                        """, "java.util.List"),
                MappingSources.type("authority", "AuthorityFactory", """
                        /** Body-free blessed Authority construction contract. */
                        public interface AuthorityFactory {
                            Authority of(Authority... children);
                            Authority of(int threshold, Authority... children);
                            Authority of(PublicKey publicKey);
                            Authority ofContract(ContractId contractId);
                            Authority ofDelegatable(ContractId contractId);
                        }
                        """, "org.hiero.sdk.v3.keys.PublicKey", "org.hiero.sdk.v3.ledger.ContractId"));
    }
}
