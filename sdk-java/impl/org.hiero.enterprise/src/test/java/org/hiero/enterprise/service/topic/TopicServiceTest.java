package org.hiero.enterprise.service.topic;

import static org.junit.jupiter.api.Assertions.*;

import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.enterprise.service.ServiceException;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of `TopicService`, generated from the specs.
@DisplayName("TopicService")
class TopicServiceTest {

    @Test
    @DisplayName("creates a TopicService and returns the values")
    void shouldCreateAndReturnTheValues() {
        final TopicService subject = new TopicService();
    }

    @Test
    @DisplayName("createTopic() can be called and returns a value (only ServiceException may occur)")
    void shouldCallCreateTopic() throws Exception {
        try {
            assertNotNull(create().createTopic());
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("createTopic(memo) can be called and returns a value (only ServiceException may occur)")
    void shouldCallCreateTopic2() throws Exception {
        try {
            assertNotNull(create().createTopic(memoValue()));
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("createTopic(adminAuthority, memo) can be called and returns a value (only ServiceException may occur)")
    void shouldCallCreateTopic3() throws Exception {
        try {
            assertNotNull(create().createTopic(adminAuthorityValue(), memoValue()));
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("createTopic(params) can be called and returns a value (only ServiceException may occur)")
    void shouldCallCreateTopic4() throws Exception {
        try {
            assertNotNull(create().createTopic(paramsValue()));
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("findById(topicId) can be called (only ServiceException may occur)")
    void shouldCallFindById() throws Exception {
        try {
            create().findById(topicIdValue());
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("getAll() can be called and returns a value (only ServiceException may occur)")
    void shouldCallGetAll() throws Exception {
        try {
            assertNotNull(create().getAll());
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("deleteTopic(topicId) can be called (only ServiceException may occur)")
    void shouldCallDeleteTopic() throws Exception {
        try {
            create().deleteTopic(topicIdValue());
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("submitMessage(topicId, message) can be called and returns a value (only ServiceException may occur)")
    void shouldCallSubmitMessage() throws Exception {
        try {
            assertNotNull(create().submitMessage(topicIdValue(), messageValue()));
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("getMessages(topicId) can be called and returns a value (only ServiceException may occur)")
    void shouldCallGetMessages() throws Exception {
        try {
            assertNotNull(create().getMessages(topicIdValue()));
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("getMessageBySequenceNumber(topicId, sequenceNumber) can be called and returns a value (only ServiceException may occur)")
    void shouldCallGetMessageBySequenceNumber() throws Exception {
        try {
            assertNotNull(create().getMessageBySequenceNumber(topicIdValue(), sequenceNumberValue()));
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("subscribe(topicId) can be called and returns a value")
    void shouldCallSubscribe() {
        assertNotNull(create().subscribe(topicIdValue()));
    }

    private static TopicService create() {
        return new TopicService();
    }

    private static String memoValue() {
        return "value";
    }

    private static Authority adminAuthorityValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static TopicCreateParams paramsValue() {
        return new TopicCreateParams(null, null, null, null, null);
    }

    private static Address topicIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static byte[] messageValue() {
        return new byte[] {1, 2, 3};
    }

    private static long sequenceNumberValue() {
        return 1L;
    }
}
