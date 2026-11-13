package org.hiero.consensusnode.client;

import static org.hiero.sdk.testing.Testing.*;

import java.math.BigInteger;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.hiero.consensusnode.queries.accounts.AccountBalanceQuery;
import org.hiero.consensusnode.status.BasicTransactionStatus;
import org.hiero.hedera.HbarUnit;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.keys.PrivateKey;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.ConsensusNode;
import org.hiero.ledger.IpAddress;
import org.hiero.ledger.MirrorNode;
import org.hiero.ledger.Network;
import org.hiero.ledger.config.NetworkSetting;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `client`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("client")
class ClientSpecTest {

    /// `consensusnode.client/a-client-keeps-the-operator-account-it-was-created-with`
    ///
    /// Source: the Go SDK (TestUnitClientFromConfigWithOperator), the Python SDK (client_test.py,
    /// test_for_testnet_then_set_operator) and the C++ SDK (ClientUnitTests): a client keeps its operator.
    @Test
    @DisplayName("a client keeps the operator account it was created with")
    @Tag("testId:consensusnode.client/a-client-keeps-the-operator-account-it-was-created-with")
    void aClientKeepsTheOperatorAccountItWasCreatedWith() {
        // given
        final Account operator = defaultAccount();

        // when
        final HieroClient<?> client = ClientFactory.createClient(defaultNetworkSetting(), operator);

        // then
        assertEquals(operator, client.operatorAccount());
        assertEquals(operator.accountId(), client.operatorAccount().accountId());
        assertEquals(operator.privateKey(), client.operatorAccount().privateKey());
    }

    /// `consensusnode.client/a-client-is-bound-to-the-network-of-its-network-setting`
    ///
    /// Source: the Java SDK (ClientTest.forMainnet, forNameReturnsCorrectNetwork), the Go SDK (TestUnitClientFromConfig)
    /// and the Python SDK (client_test.py, test_factory_basic_setup): a client is bound to the network it was created for.
    @Test
    @DisplayName("a client is bound to the network of its network setting")
    @Tag("testId:consensusnode.client/a-client-is-bound-to-the-network-of-its-network-setting")
    void aClientIsBoundToTheNetworkOfItsNetworkSetting() {
        // given
        final NetworkSetting setting = defaultNetworkSetting();

        // when
        final HieroClient<?> client = ClientFactory.createClient(setting, defaultAccount());

        // then
        assertEquals(setting.network(), client.network());
    }

    /// `consensusnode.client/the-default-signer-signs-with-the-private-key-of-the-operator-account`
    ///
    /// Source: this spec (by default the operator account's private key signs); the Java and JavaScript SDKs
    /// (Client.setOperator signs with the operator's private key) and the Go SDK (the operator's key signs).
    @Test
    @DisplayName("the default signer signs with the private key of the operator account")
    @Tag("testId:consensusnode.client/the-default-signer-signs-with-the-private-key-of-the-operator-account")
    void theDefaultSignerSignsWithThePrivateKeyOfTheOperatorAccount() {
        // given
        final Account operator = defaultAccount();
        final HieroClient<?> client = ClientFactory.createClient(defaultNetworkSetting(), operator);
        final AccountId node = AccountId.fromString("0.0.3");
        final byte[] transactionBytes = utf8("transaction body");

        // when
        final NodeSignature signature = client.transactionSigner().signTransaction(transactionBytes, node);

        // then
        assertEquals(node, signature.node());
        assertEquals(operator.privateKey().createPublicKey(), signature.publicKey());
        assertTrue(signature.publicKey().verify(transactionBytes, signature.signature()));
        assertFalse(signature.publicKey().verify(utf8("another body"), signature.signature()));
    }

    /// `consensusnode.client/a-client-created-with-a-signer-signs-with-that-signer`
    ///
    /// Source: the Java and JavaScript SDKs (Client.setOperatorWith: an operator whose transactions are signed by a
    /// given signer instead of its private key). The second key is the secp256k1 key of the tests of keys.md.
    @Test
    @DisplayName("a client created with a signer signs with that signer")
    @Tag("testId:consensusnode.client/a-client-created-with-a-signer-signs-with-that-signer")
    void aClientCreatedWithASignerSignsWithThatSigner() {
        // given
        final PrivateKey signerKey = KeysFactory.createPrivateKey(KeyAlgorithm.ECDSA, ByteImportEncoding.HEX, "8776c6b831a1b61ac10dac0304a2843de4716f54b1919bb91a2685d0fe3f3048");
        final Account signerAccount = new Account(AccountId.fromString("0.0.1002"), signerKey);
        final TransactionSigner signer = ClientFactory.createClient(defaultNetworkSetting(), signerAccount).transactionSigner();
        final byte[] transactionBytes = utf8("transaction body");

        // when
        final HieroClient<?> client = ClientFactory.createClient(defaultNetworkSetting(), defaultAccount(), signer);
        final NodeSignature signature = client.transactionSigner().signTransaction(transactionBytes, AccountId.fromString("0.0.3"));

        // then
        assertEquals(defaultAccount(), client.operatorAccount());
        assertEquals(signerKey.createPublicKey(), signature.publicKey());
        assertTrue(signature.publicKey().verify(transactionBytes, signature.signature()));
    }

    /// `consensusnode.client/the-default-retry-policy-decides-by-the-outcome-of-the-last-attempt`
    ///
    /// Source: this spec (`rule:retry-decision`): a node failure is retried on another node, a pending receipt on the
    /// same node, a rejection is not retried. The current SDKs differ here (see the Design Notes).
    @Test
    @DisplayName("the default retry policy decides by the outcome of the last attempt")
    @Tag("testId:consensusnode.client/the-default-retry-policy-decides-by-the-outcome-of-the-last-attempt")
    @Tag("rule:consensusnode.client/retry-decision")
    void theDefaultRetryPolicyDecidesByTheOutcomeOfTheLastAttempt() {
        // given
        final RetryPolicy policy = ExecutionSettings.createDefault().retryPolicy();
        final AccountBalanceQuery request = new AccountBalanceQuery(defaultAccountId(), null);
        final AttemptResult failure = new TransportFailure(TransportFailureKind.CONNECTION, "connection refused");
        final Duration firstWait = toDuration("PT0.25S");
        final RetryDecision onOtherNode = new RetryOnOtherNode(firstWait);
        final RetryDecision onSameNode = new RetryOnSameNode(firstWait);
        final RetryDecision giveUp = new GiveUp();
        for (final AttemptOutcome outcome : List.of(AttemptOutcome.NODE_UNAVAILABLE, AttemptOutcome.NODE_TIMEOUT, AttemptOutcome.NODE_BUSY, AttemptOutcome.WRONG_NODE)) {
            // when
            final RetryDecision decision = policy.decide(new RetryContext(request, List.of(new Attempt(1, defaultConsensusNode(), failure, outcome, toDuration("PT1S"))), toDuration("PT1S")));

            // then
            assertEquals(onOtherNode, decision);
        }

        // when
        final RetryDecision pending = policy.decide(new RetryContext(request, List.of(new Attempt(1, defaultConsensusNode(), new NodeAnswer(BasicTransactionStatus.OK), AttemptOutcome.PENDING, toDuration("PT1S"))), toDuration("PT1S")));
        final RetryDecision rejected = policy.decide(new RetryContext(request, List.of(new Attempt(1, defaultConsensusNode(), new NodeAnswer(BasicTransactionStatus.INVALID_TRANSACTION), AttemptOutcome.REJECTED, toDuration("PT1S"))), toDuration("PT1S")));

        // then
        assertEquals(onSameNode, pending);
        assertEquals(giveUp, rejected);
    }

    /// `consensusnode.client/the-default-retry-policy-waits-250-ms-doubled-up-to-8-s`
    ///
    /// Source: this spec (`rule:wait-between-attempts`, `rule:request-duration`): 250 ms before the second attempt,
    /// doubled up to 8 s, no random share; with 10 attempts the waits add up to 39.75 s.
    @Test
    @DisplayName("the default retry policy waits 250 ms doubled up to 8 s")
    @Tag("testId:consensusnode.client/the-default-retry-policy-waits-250-ms-doubled-up-to-8-s")
    @Tag("rule:consensusnode.client/wait-between-attempts")
    @Tag("rule:consensusnode.client/request-duration")
    void theDefaultRetryPolicyWaits250MsDoubledUpTo8S() {
        // given
        final RetryPolicy policy = ExecutionSettings.createDefault().retryPolicy();
        final AccountBalanceQuery request = new AccountBalanceQuery(defaultAccountId(), null);
        final AttemptResult busy = new NodeAnswer(BasicTransactionStatus.OK);
        final Duration second = toDuration("PT1S");
        final List<Attempt> attempts = List.of(new Attempt(1, defaultConsensusNode(), busy, AttemptOutcome.NODE_BUSY, second), new Attempt(2, defaultConsensusNode(), busy, AttemptOutcome.NODE_BUSY, second), new Attempt(3, defaultConsensusNode(), busy, AttemptOutcome.NODE_BUSY, second), new Attempt(4, defaultConsensusNode(), busy, AttemptOutcome.NODE_BUSY, second), new Attempt(5, defaultConsensusNode(), busy, AttemptOutcome.NODE_BUSY, second), new Attempt(6, defaultConsensusNode(), busy, AttemptOutcome.NODE_BUSY, second), new Attempt(7, defaultConsensusNode(), busy, AttemptOutcome.NODE_BUSY, second), new Attempt(8, defaultConsensusNode(), busy, AttemptOutcome.NODE_BUSY, second), new Attempt(9, defaultConsensusNode(), busy, AttemptOutcome.NODE_BUSY, second));
        final List<Duration> waits = List.of(toDuration("PT0.25S"), toDuration("PT0.5S"), toDuration("PT1S"), toDuration("PT2S"), toDuration("PT4S"), toDuration("PT8S"), toDuration("PT8S"), toDuration("PT8S"), toDuration("PT8S"));
        for (final long n : range(1L, 10L)) {
            // when
            final RetryDecision decision = policy.decide(new RetryContext(request, slice(attempts, 0L, n), second));

            // then: the wait before attempt n + 1
            final RetryDecision expected = new RetryOnOtherNode(get(waits, checked(integer(n).subtract(integer(1L)), "int64").longValueExact()));
            assertEquals(expected, decision, text(integer(n)));
        }
    }

    /// `consensusnode.client/the-default-retry-policy-gives-up-after-the-last-attempt`
    ///
    /// Source: this spec (`rule:give-up-after-max-attempts`): after attempt number maxAttempts the policy gives up,
    /// whatever the outcome; 10 attempts by default.
    @Test
    @DisplayName("the default retry policy gives up after the last attempt")
    @Tag("testId:consensusnode.client/the-default-retry-policy-gives-up-after-the-last-attempt")
    @Tag("rule:consensusnode.client/give-up-after-max-attempts")
    void theDefaultRetryPolicyGivesUpAfterTheLastAttempt() {
        // given
        final AccountBalanceQuery request = new AccountBalanceQuery(defaultAccountId(), null);
        final AttemptResult pending = new NodeAnswer(BasicTransactionStatus.OK);
        final Duration second = toDuration("PT1S");
        final RetryDecision onSameNode = new RetryOnSameNode(toDuration("PT0.5S"));
        final RetryDecision giveUp = new GiveUp();
        final RetryPolicy three = ExecutionSettings.createDefault(3, second).retryPolicy();

        // when
        final RetryDecision afterSecond = three.decide(new RetryContext(request, List.of(new Attempt(1, defaultConsensusNode(), pending, AttemptOutcome.PENDING, second), new Attempt(2, defaultConsensusNode(), pending, AttemptOutcome.PENDING, second)), second));
        final RetryDecision afterThird = three.decide(new RetryContext(request, List.of(new Attempt(1, defaultConsensusNode(), pending, AttemptOutcome.PENDING, second), new Attempt(2, defaultConsensusNode(), pending, AttemptOutcome.PENDING, second), new Attempt(3, defaultConsensusNode(), pending, AttemptOutcome.PENDING, second)), second));
        final RetryDecision afterTenth = ExecutionSettings.createDefault().retryPolicy().decide(new RetryContext(request, List.of(new Attempt(10, defaultConsensusNode(), pending, AttemptOutcome.NODE_BUSY, second)), second));

        // then
        assertEquals(onSameNode, afterSecond);
        assertEquals(giveUp, afterThird);
        assertEquals(giveUp, afterTenth);
    }

    /// `consensusnode.client/the-default-node-selector-avoids-a-node-that-failed`
    ///
    /// Source: this spec (`rule:node-avoidance`, `rule:node-choice`): after NODE_UNAVAILABLE, NODE_TIMEOUT or WRONG_NODE
    /// a node is avoided, so the choice among the candidates falls on the others. The choice among equal candidates is
    /// random, so it is repeated.
    @Test
    @DisplayName("the default node selector avoids a node that failed")
    @Tag("testId:consensusnode.client/the-default-node-selector-avoids-a-node-that-failed")
    @Tag("rule:consensusnode.client/node-avoidance")
    @Tag("rule:consensusnode.client/node-choice")
    void theDefaultNodeSelectorAvoidsANodeThatFailed() {
        // given
        final ConsensusNode a = defaultConsensusNode();
        final ConsensusNode b = new ConsensusNode(defaultIpAddress(), 35212, AccountId.fromString("0.0.4"));
        for (final AttemptOutcome outcome : List.of(AttemptOutcome.NODE_UNAVAILABLE, AttemptOutcome.NODE_TIMEOUT, AttemptOutcome.WRONG_NODE)) {
            // given
            final NodeSelector selector = ExecutionSettings.createDefault().nodeSelector();

            // when
            selector.attemptCompleted(new Attempt(1, a, new TransportFailure(TransportFailureKind.CONNECTION, "failed"), outcome, toDuration("PT1S")));

            // then
            for (final long i : range(0L, 20L)) {
                assertEquals(b, selector.select(List.of(a, b), List.of()), "choice " + text(integer(i)));
            }
        }
    }

    /// `consensusnode.client/the-default-node-selector-does-not-avoid-a-busy-node`
    ///
    /// Source: this spec (`rule:node-failures-in-a-row`, `rule:node-avoidance`): only NODE_UNAVAILABLE, NODE_TIMEOUT and
    /// WRONG_NODE make a node avoided; a busy node is chosen like any other. (That a retry on another node leaves out the
    /// node of the last attempt is part of the execution, which passes the remaining candidates to the selector.)
    @Test
    @DisplayName("the default node selector does not avoid a busy node")
    @Tag("testId:consensusnode.client/the-default-node-selector-does-not-avoid-a-busy-node")
    @Tag("rule:consensusnode.client/node-failures-in-a-row")
    @Tag("rule:consensusnode.client/node-avoidance")
    void theDefaultNodeSelectorDoesNotAvoidABusyNode() {
        // given
        final NodeSelector selector = ExecutionSettings.createDefault().nodeSelector();
        final ConsensusNode a = defaultConsensusNode();
        final ConsensusNode b = new ConsensusNode(defaultIpAddress(), 35212, AccountId.fromString("0.0.4"));
        final Duration second = toDuration("PT1S");

        // when: a is busy, b cannot be reached
        selector.attemptCompleted(new Attempt(1, a, new NodeAnswer(BasicTransactionStatus.OK), AttemptOutcome.NODE_BUSY, second));
        selector.attemptCompleted(new Attempt(1, b, new TransportFailure(TransportFailureKind.CONNECTION, "failed"), AttemptOutcome.NODE_UNAVAILABLE, second));

        // then
        for (final long i : range(0L, 20L)) {
            assertEquals(a, selector.select(List.of(a, b), List.of()), "choice " + text(integer(i)));
        }
    }

    /// `consensusnode.client/the-default-node-selector-picks-the-node-whose-avoidance-ends-first`
    ///
    /// Source: this spec (`rule:node-avoidance`, `rule:node-choice`): every failure doubles the time a node is avoided
    /// (8 s, 16 s, ...); when every candidate is avoided, the one whose avoidance ends first is chosen; an attempt with
    /// another outcome ends the avoidance.
    @Test
    @DisplayName("the default node selector picks the node whose avoidance ends first")
    @Tag("testId:consensusnode.client/the-default-node-selector-picks-the-node-whose-avoidance-ends-first")
    @Tag("rule:consensusnode.client/node-avoidance")
    @Tag("rule:consensusnode.client/node-choice")
    void theDefaultNodeSelectorPicksTheNodeWhoseAvoidanceEndsFirst() {
        // given
        final NodeSelector selector = ExecutionSettings.createDefault().nodeSelector();
        final ConsensusNode a = defaultConsensusNode();
        final ConsensusNode b = new ConsensusNode(defaultIpAddress(), 35212, AccountId.fromString("0.0.4"));
        final AttemptResult failure = new TransportFailure(TransportFailureKind.TIMEOUT, "timeout");
        final Duration second = toDuration("PT1S");

        // when: a is avoided for 8 s, b for 16 s
        selector.attemptCompleted(new Attempt(1, a, failure, AttemptOutcome.NODE_TIMEOUT, second));
        selector.attemptCompleted(new Attempt(1, b, failure, AttemptOutcome.NODE_TIMEOUT, second));
        selector.attemptCompleted(new Attempt(2, b, failure, AttemptOutcome.NODE_TIMEOUT, second));

        // then
        assertEquals(a, selector.select(List.of(a, b), List.of()));

        // when: a answers again, b still fails
        selector.attemptCompleted(new Attempt(1, a, new NodeAnswer(BasicTransactionStatus.OK), AttemptOutcome.SUCCESS, second));
        selector.attemptCompleted(new Attempt(1, a, failure, AttemptOutcome.NODE_TIMEOUT, second));

        // then: a is avoided for 8 s again, not 16 s, so it still ends first
        assertEquals(a, selector.select(List.of(a, b), List.of()));
    }

    private static Account defaultAccount() {
        return new Account(defaultAccountId(), defaultPrivateKey());
    }

    private static AccountId defaultAccountId() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static PrivateKey defaultPrivateKey() {
        return KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c");
    }

    private static NetworkSetting defaultNetworkSetting() {
        return new NetworkSetting(defaultNetwork(), Set.of(defaultConsensusNode()), Set.of(defaultMirrorNode()));
    }

    private static Network<?> defaultNetwork() {
        return new Network<>(new byte[] {(byte) 3}, "local", HbarUnit.HBAR);
    }

    private static ConsensusNode defaultConsensusNode() {
        return new ConsensusNode(defaultIpAddress(), 35211, new AccountId(0L, 0L, "", 3L, null, null));
    }

    private static IpAddress defaultIpAddress() {
        return new IpAddress(new byte[] {(byte) 127, (byte) 0, (byte) 0, (byte) 1});
    }

    private static MirrorNode defaultMirrorNode() {
        return new MirrorNode("http://127.0.0.1:38081");
    }
}
