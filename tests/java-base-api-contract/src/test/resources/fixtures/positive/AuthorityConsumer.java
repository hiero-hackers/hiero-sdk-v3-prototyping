package org.hiero.sdk.v3.fixture;

import org.hiero.sdk.v3.authority.Authority;
import org.hiero.sdk.v3.authority.AuthorityList;
import org.hiero.sdk.v3.authority.ContractAuthority;
import org.hiero.sdk.v3.authority.PublicKeyAuthority;

public final class AuthorityConsumer {
    int depth(Authority authority) {
        return switch (authority) {
            case PublicKeyAuthority ignored -> 1;
            case ContractAuthority ignored -> 1;
            case AuthorityList list -> 1 + list.children().stream().mapToInt(this::depth).max().orElse(0);
        };
    }
}
