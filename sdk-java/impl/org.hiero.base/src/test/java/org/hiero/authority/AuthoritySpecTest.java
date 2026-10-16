package org.hiero.authority;

import static org.hiero.sdk.testing.Testing.*;

import java.util.List;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.keys.PublicKey;
import org.hiero.ledger.ContractId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of the spec `authority`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("authority")
class AuthoritySpecTest {

    /// Three distinct Ed25519 public keys: the public keys of RFC 8032, section 7.1 (TEST 1, TEST 2, TEST 3).
    private static List<PublicKey> PUBLIC_KEYS() {
        return List.of(KeysFactory.createPublicKey(KeyAlgorithm.ED25519, hex("d75a980182b10ab7d54bfed3c964073a0ee172f3daa62325af021a68f707511a")), KeysFactory.createPublicKey(KeyAlgorithm.ED25519, hex("3d4017c3e843895a92b70aa74d1b7ebc9c982ccf2ec4968cc0cd55f12af4660c")), KeysFactory.createPublicKey(KeyAlgorithm.ED25519, hex("fc51cd8e6218a1a38da47ed00230f0580816ed13ba3303ac5deb911548908025")));
    }

    /// `authority/a-single-key-becomes-a-public-key-authority-of-that-key`
    ///
    /// Source: this spec (of(publicKey) is the single-key leaf); the Java, Go, Python and C++ SDKs (a public key is a key).
    @Test
    @DisplayName("a single key becomes a public key authority of that key")
    void aSingleKeyBecomesAPublicKeyAuthorityOfThatKey() {
        // given
        final PublicKey publicKey = get(PUBLIC_KEYS(), 0L);
        final Authority leaf = new PublicKeyAuthority(publicKey);

        // when
        final Authority authority = AuthorityFactory.of(publicKey);

        // then
        assertInstanceOf(PublicKeyAuthority.class, authority);
        assertEquals(leaf, authority);
    }

    /// `authority/a-composition-without-a-threshold-requires-all-of-its-children`
    ///
    /// Source: this spec (n-of-n: threshold is set to children.size()); the Java, JavaScript, Go and Python SDKs (a key list
    /// without a threshold requires all of its keys).
    @Test
    @DisplayName("a composition without a threshold requires all of its children")
    void aCompositionWithoutAThresholdRequiresAllOfItsChildren() {
        // given
        final Authority child = AuthorityFactory.of(get(PUBLIC_KEYS(), 0L));
        final Authority allOfOne = new AuthorityList(List.of(child), 1);

        // when
        final Authority authority = AuthorityFactory.of(child);

        // then
        assertInstanceOf(AuthorityList.class, authority);
        assertEquals(allOfOne, authority);
    }

    /// `authority/a-composition-with-a-threshold-keeps-its-threshold-and-its-child`
    ///
    /// Source: this spec (m-of-n composition); the Go, Python, JavaScript and C++ SDKs (a key list keeps its threshold).
    @Test
    @DisplayName("a composition with a threshold keeps its threshold and its child")
    void aCompositionWithAThresholdKeepsItsThresholdAndItsChild() {
        // given
        final Authority child = AuthorityFactory.of(get(PUBLIC_KEYS(), 0L));
        final Authority oneOfOne = new AuthorityList(List.of(child), 1);

        // when
        final Authority authority = AuthorityFactory.of(1, child);

        // then
        assertEquals(oneOfOne, authority);
    }

    /// `authority/a-contract-leaf-is-delegatable-only-when-created-as-delegatable`
    ///
    /// Source: this spec (ofContract maps to HAPI ContractID, ofDelegatable to DelegatableContractID); the Go, Python and C++
    /// SDKs (a delegatable contract id becomes a key of its own kind).
    @Test
    @DisplayName("a contract leaf is delegatable only when created as delegatable")
    void aContractLeafIsDelegatableOnlyWhenCreatedAsDelegatable() {
        // given
        final ContractId contractId = defaultContractId();
        final Authority plainLeaf = new ContractAuthority(contractId, false);
        final Authority delegatableLeaf = new ContractAuthority(contractId, true);

        // when
        final Authority plain = AuthorityFactory.ofContract(contractId);
        final Authority delegatable = AuthorityFactory.ofDelegatable(contractId);

        // then
        assertEquals(plainLeaf, plain);
        assertEquals(delegatableLeaf, delegatable);
        assertNotEquals(plain, delegatable);
    }

    /// `authority/a-contract-authority-is-not-delegatable-by-default`
    ///
    /// Source: this spec (@@default(false) delegatable).
    @Test
    @DisplayName("a contract authority is not delegatable by default")
    void aContractAuthorityIsNotDelegatableByDefault() {
        // when
        final ContractAuthority authority = new ContractAuthority(defaultContractId(), false);

        // then
        assertFalse(authority.delegatable());
        assertEquals(AuthorityFactory.ofContract(defaultContractId()), authority);
    }

    /// `authority/authorities-are-equal-when-their-trees-match`
    ///
    /// Source: this spec (structural equality: two authorities are equal iff their trees match); the Python and C++ SDKs
    /// (equality of contract ids and key lists).
    @Test
    @DisplayName("authorities are equal when their trees match")
    void authoritiesAreEqualWhenTheirTreesMatch() {
        // given
        final Authority alice = AuthorityFactory.of(get(PUBLIC_KEYS(), 0L));
        final Authority bob = AuthorityFactory.of(get(PUBLIC_KEYS(), 1L));
        final Authority carol = AuthorityFactory.of(get(PUBLIC_KEYS(), 2L));
        final Authority dao = AuthorityFactory.ofContract(defaultContractId());

        // when
        final AuthorityList nested = new AuthorityList(List.of(alice, new AuthorityList(List.of(bob, carol, dao), 2)), 2);
        final AuthorityList same = new AuthorityList(List.of(AuthorityFactory.of(get(PUBLIC_KEYS(), 0L)), new AuthorityList(List.of(AuthorityFactory.of(get(PUBLIC_KEYS(), 1L)), AuthorityFactory.of(get(PUBLIC_KEYS(), 2L)), AuthorityFactory.ofContract(defaultContractId())), 2)), 2);

        // then
        assertEquals(same, nested);
        assertEquals(AuthorityFactory.of(get(PUBLIC_KEYS(), 0L)), alice);
        assertNotEquals(bob, alice);
        assertNotEquals(new AuthorityList(List.of(alice, bob), 1), new AuthorityList(List.of(alice, bob), 2));
        assertNotEquals(new AuthorityList(List.of(bob, alice), 2), new AuthorityList(List.of(alice, bob), 2));
        assertNotEquals(AuthorityFactory.of(1, alice), alice);
    }

    private static ContractId defaultContractId() {
        return new ContractId(0L, 0L, "", 1001L, null);
    }
}
