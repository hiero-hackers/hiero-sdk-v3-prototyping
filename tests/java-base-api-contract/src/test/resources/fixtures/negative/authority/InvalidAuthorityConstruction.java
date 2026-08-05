package org.hiero.sdk.v3.fixture.negative;

import java.util.List;
import org.hiero.sdk.v3.authority.AuthorityList;

final class InvalidAuthorityConstruction {
    AuthorityList empty() { return new AuthorityList(List.of(), 1); }
    AuthorityList zero(List<org.hiero.sdk.v3.authority.Authority> children) { return new AuthorityList(children, 0); }
}
