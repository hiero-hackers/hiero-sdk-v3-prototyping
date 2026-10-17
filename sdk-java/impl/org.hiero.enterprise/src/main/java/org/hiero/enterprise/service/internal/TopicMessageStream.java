package org.hiero.enterprise.service.internal;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import org.hiero.common.Page;
import org.hiero.enterprise.service.ServiceException;
import org.hiero.enterprise.service.topic.TopicMessage;
import org.hiero.ledger.Address;
import org.hiero.sdk.common.HieroStream;

/// The live messages of a topic, read by polling the Mirror Node. Every poll reads the messages of the topic and
/// delivers those with a sequence number above the last delivered one, in the order of their sequence numbers. The
/// stream never ends by itself; `close()` ends it. A failing read of the Mirror Node is a terminal error.
public final class TopicMessageStream implements HieroStream<TopicMessage> {

    private static final Duration POLL_INTERVAL = Duration.ofSeconds(1);

    private final DefaultSession session;

    private final Address topicId;

    private final Deque<TopicMessage> buffer = new ArrayDeque<>();

    private final AtomicBoolean iterated = new AtomicBoolean();

    private volatile boolean closed;

    private long lastSequenceNumber;

    /// Creates the stream of the messages of a topic.
    ///
    /// @param session the session whose Mirror Node is polled
    /// @param topicId the topic
    public TopicMessageStream(final DefaultSession session, final Address topicId) {
        this.session = Objects.requireNonNull(session, "session must not be null");
        this.topicId = Objects.requireNonNull(topicId, "topicId must not be null");
    }

    @Override
    public Iterator<TopicMessage> iterator() {
        if (!iterated.compareAndSet(false, true)) {
            throw new IllegalStateException("The stream can only be iterated once");
        }
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                while (buffer.isEmpty() && !closed) {
                    poll();
                    if (buffer.isEmpty() && !closed) {
                        pause();
                    }
                }
                return !buffer.isEmpty() && !closed;
            }

            @Override
            public TopicMessage next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("The stream is closed");
                }
                final TopicMessage message = buffer.removeFirst();
                lastSequenceNumber = message.sequenceNumber();
                return message;
            }
        };
    }

    private void poll() {
        Page<org.hiero.mirrornode.topic.TopicMessage> page =
                DefaultSession.await(session.mirrorNode().topic().getMessages(topicId));
        final long after = buffer.isEmpty() ? lastSequenceNumber : buffer.getLast().sequenceNumber();
        final List<TopicMessage> found = new ArrayList<>();
        while (true) {
            page.data().stream().filter(message -> message.sequenceNumber() > after)
                    .map(Topics::toMessage).forEach(found::add);
            if (!page.hasNext() || closed) {
                break;
            }
            page = DefaultSession.await(page.next());
        }
        found.sort(Comparator.comparingLong(TopicMessage::sequenceNumber));
        buffer.addAll(found);
    }

    private void pause() {
        try {
            Thread.sleep(POLL_INTERVAL);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            closed = true;
            throw new ServiceException("Interrupted while waiting for topic messages", e);
        }
    }

    @Override
    public void close() {
        closed = true;
    }
}
