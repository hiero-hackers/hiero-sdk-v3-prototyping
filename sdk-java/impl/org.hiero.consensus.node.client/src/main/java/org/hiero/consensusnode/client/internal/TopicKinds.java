package org.hiero.consensusnode.client.internal;

import com.google.protobuf.StringValue;
import com.hederahashgraph.api.proto.java.ConsensusCreateTopicTransactionBody;
import com.hederahashgraph.api.proto.java.ConsensusDeleteTopicTransactionBody;
import com.hederahashgraph.api.proto.java.ConsensusUpdateTopicTransactionBody;
import com.hederahashgraph.api.proto.java.TransactionBody;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import org.hiero.authority.Authority;
import org.hiero.consensusnode.transactions.topics.TopicCreateReceipt;
import org.hiero.consensusnode.transactions.topics.TopicCreateTransaction;
import org.hiero.consensusnode.transactions.topics.TopicDeleteReceipt;
import org.hiero.consensusnode.transactions.topics.TopicDeleteTransaction;
import org.hiero.consensusnode.transactions.topics.TopicUpdateReceipt;
import org.hiero.consensusnode.transactions.topics.TopicUpdateTransaction;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;

/// The transaction types of topics.
public final class TopicKinds {

    /// HAPI requires an auto-renew period for a new topic; the default when the transaction sets none (the default
    /// of the Hiero SDKs).
    private static final Duration DEFAULT_AUTO_RENEW_PERIOD = Duration.ofDays(90);

    /// The address of a topic the receipt names none of (a failed create).
    private static final Address NO_TOPIC = new Address(0L, 0L, "", 0L);

    /// `TopicCreateTransaction`: `ConsensusService.createTopic`.
    public static final TransactionKind<TopicCreateReceipt, TopicCreateTransaction> TOPIC_CREATE = new TransactionKind<>(
            TopicCreateTransaction.class, "proto.ConsensusService", "createTopic", TransactionBody.DataCase.CONSENSUSCREATETOPIC,
            TopicKinds::topicCreate,
            (id, status, rate, nextRate, receipt) -> new TopicCreateReceipt(id, status, rate, nextRate,
                    receipt.hasTopicID() ? Protobuf.fromProto(receipt.getTopicID()) : NO_TOPIC));

    /// `TopicUpdateTransaction`: `ConsensusService.updateTopic`.
    public static final TransactionKind<TopicUpdateReceipt, TopicUpdateTransaction> TOPIC_UPDATE = new TransactionKind<>(
            TopicUpdateTransaction.class, "proto.ConsensusService", "updateTopic", TransactionBody.DataCase.CONSENSUSUPDATETOPIC,
            TopicKinds::topicUpdate,
            (id, status, rate, nextRate, receipt) -> new TopicUpdateReceipt(id, status, rate, nextRate));

    /// `TopicDeleteTransaction`: `ConsensusService.deleteTopic`.
    public static final TransactionKind<TopicDeleteReceipt, TopicDeleteTransaction> TOPIC_DELETE = new TransactionKind<>(
            TopicDeleteTransaction.class, "proto.ConsensusService", "deleteTopic", TransactionBody.DataCase.CONSENSUSDELETETOPIC,
            (transaction, body) -> body.setConsensusDeleteTopic(ConsensusDeleteTopicTransactionBody.newBuilder()
                    .setTopicID(Protobuf.toTopicId(transaction.topicId()))),
            (id, status, rate, nextRate, receipt) -> new TopicDeleteReceipt(id, status, rate, nextRate));

    /// All transaction types of this class.
    public static final List<TransactionKind<?, ?>> ALL = List.of(TOPIC_CREATE, TOPIC_UPDATE, TOPIC_DELETE);

    private TopicKinds() {
    }

    private static void topicCreate(final TopicCreateTransaction transaction, final TransactionBody.Builder body) {
        final Duration autoRenewPeriod = transaction.autoRenewPeriod();
        final ConsensusCreateTopicTransactionBody.Builder create = ConsensusCreateTopicTransactionBody.newBuilder()
                .setAutoRenewPeriod(Protobuf.toProto(autoRenewPeriod == null ? DEFAULT_AUTO_RENEW_PERIOD
                        : autoRenewPeriod));
        final Authority adminAuthority = transaction.adminAuthority();
        if (adminAuthority != null) {
            create.setAdminKey(Protobuf.toProto(adminAuthority));
        }
        final Authority submitAuthority = transaction.submitAuthority();
        if (submitAuthority != null) {
            create.setSubmitKey(Protobuf.toProto(submitAuthority));
        }
        final String memo = transaction.topicMemo();
        if (memo != null) {
            create.setMemo(memo);
        }
        final AccountId autoRenewAccount = transaction.autoRenewAccount();
        if (autoRenewAccount != null) {
            create.setAutoRenewAccount(Protobuf.toProto(autoRenewAccount));
        }
        body.setConsensusCreateTopic(create);
    }

    private static void topicUpdate(final TopicUpdateTransaction transaction, final TransactionBody.Builder body) {
        final ConsensusUpdateTopicTransactionBody.Builder update = ConsensusUpdateTopicTransactionBody.newBuilder()
                .setTopicID(Protobuf.toTopicId(transaction.topicId()));
        final Authority adminAuthority = transaction.adminAuthority();
        if (adminAuthority != null) {
            update.setAdminKey(Protobuf.toProto(adminAuthority));
        }
        final Authority submitAuthority = transaction.submitAuthority();
        if (submitAuthority != null) {
            update.setSubmitKey(Protobuf.toProto(submitAuthority));
        }
        final String memo = transaction.topicMemo();
        if (memo != null) {
            update.setMemo(StringValue.of(memo));
        }
        final ZonedDateTime expirationTime = transaction.expirationTime();
        if (expirationTime != null) {
            update.setExpirationTime(Protobuf.toProto(expirationTime));
        }
        final Duration autoRenewPeriod = transaction.autoRenewPeriod();
        if (autoRenewPeriod != null) {
            update.setAutoRenewPeriod(Protobuf.toProto(autoRenewPeriod));
        }
        final AccountId autoRenewAccount = transaction.autoRenewAccount();
        if (autoRenewAccount != null) {
            update.setAutoRenewAccount(Protobuf.toProto(autoRenewAccount));
        }
        body.setConsensusUpdateTopic(update);
    }
}
