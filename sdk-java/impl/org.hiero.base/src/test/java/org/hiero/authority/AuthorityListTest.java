package org.hiero.authority;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `AuthorityList`, generated from the specs.
@DisplayName("AuthorityList")
class AuthorityListTest {

    @Test
    @DisplayName("creates an AuthorityList and returns the values")
    @Tag("testId:authority.AuthorityList/create")
    void shouldCreateAndReturnTheValues() {
        final List<Authority> children = childrenValue();
        final int threshold = thresholdValue();
        final AuthorityList subject = new AuthorityList(children, threshold);
        assertValue(children, subject.children());
        assertValue(threshold, subject.threshold());
    }

    @Test
    @DisplayName("rejects null for children")
    @Tag("testId:authority.AuthorityList/children/rejects-null")
    void shouldRejectNullChildren() {
        assertThrows(NullPointerException.class, () -> new AuthorityList((List<Authority>) null, thresholdValue()));
    }

    @Test
    @DisplayName("children: accepts the minimum number of elements")
    @Tag("testId:authority.AuthorityList/children/accepts-at-minimum-size")
    void shouldAcceptChildrenAtMinimumSize() {
        final List<Authority> value = List.of(new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey()));
        final AuthorityList subject = new AuthorityList(value, thresholdValue());
        assertValue(value, subject.children());
    }

    @Test
    @DisplayName("children: rejects fewer elements than the minimum")
    @Tag("testId:authority.AuthorityList/children/rejects-below-minimum-size")
    void shouldRejectChildrenBelowMinimumSize() {
        assertThrows(IllegalArgumentException.class, () -> new AuthorityList(List.of(), thresholdValue()));
    }

    @Test
    @DisplayName("the constructor copies children; the accessor returns an unmodifiable collection")
    @Tag("testId:authority.AuthorityList/children/copies-in")
    void shouldCopyChildrenIn() {
        final List<Authority> value = new ArrayList<>(childrenValue());
        final List<Authority> expected = List.copyOf(value);
        final AuthorityList subject = new AuthorityList(value, thresholdValue());
        value.clear();
        assertValue(expected, subject.children());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.children()).clear());
    }

    @Test
    @DisplayName("threshold: accepts the minimum (1)")
    @Tag("testId:authority.AuthorityList/threshold/accepts-at-minimum")
    void shouldAcceptThresholdAtMinimum() {
        final int value = 1;
        final AuthorityList subject = new AuthorityList(childrenValue(), value);
        assertValue(value, subject.threshold());
    }

    @Test
    @DisplayName("threshold: accepts the maximum (2147483647)")
    @Tag("testId:authority.AuthorityList/threshold/accepts-at-maximum")
    void shouldAcceptThresholdAtMaximum() {
        final int value = 2147483647;
        final AuthorityList subject = new AuthorityList(childrenValue(), value);
        assertValue(value, subject.threshold());
    }

    @Test
    @DisplayName("threshold: rejects a value below the minimum (0)")
    @Tag("testId:authority.AuthorityList/threshold/rejects-below-minimum")
    void shouldRejectThresholdBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new AuthorityList(childrenValue(), 0));
    }

    private static List<Authority> childrenValue() {
        return List.of(new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey()));
    }

    private static int thresholdValue() {
        return 1;
    }

    private static AuthorityList create() {
        return new AuthorityList(childrenValue(), thresholdValue());
    }
}
