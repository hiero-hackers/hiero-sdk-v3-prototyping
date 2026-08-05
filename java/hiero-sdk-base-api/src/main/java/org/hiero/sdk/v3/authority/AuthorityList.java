// Generated from spec/base/authority.md (authority#AuthorityList); DO NOT EDIT.

package org.hiero.sdk.v3.authority;

import java.util.List;

/** Immutable threshold composition of authorization requirements. */
public record AuthorityList(List<Authority> children, int threshold) implements Authority {
    /** Copies children and validates non-empty threshold bounds. */
    public AuthorityList {
        children = List.copyOf(children);
        if (children.isEmpty()) throw new IllegalArgumentException("children must not be empty");
        if (threshold < 1 || threshold > children.size()) throw new IllegalArgumentException("threshold must be between 1 and children.size()");
    }
}
